/*
 * $Id: 40ced49afa76d934fc866f9c6c9d9ec5da9829cb $
 *
 * 作成日: 2012/12/06
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 通知表
 */
public class KNJM501M {

    private static final Log log = LogFactory.getLog(KNJM501M.class);
    
    private static final String RISHU = "1";
    private static final String KOUNIN = "2";
    private static final String ZOUTAN = "3";
    
    private static final String SEME1 = "1";
    private static final String SEME2 = "2";
    private static final String SEMEALL = "9";
    
    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    private static Student getStudent(final List list, final String schregno) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }
    
    private static int getMS932ByteLength(final String name) {
        int len = 0;
        if (null != name) {
            try {
                len = name.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }
    
    private static String getDispNum(final String num, final String defStr) {
        if (num == null || !NumberUtils.isNumber(num)) {
            return defStr;
        }
        final BigDecimal bd = new BigDecimal(num);
        if (bd.setScale(0, BigDecimal.ROUND_UP).equals(bd.setScale(0, BigDecimal.ROUND_DOWN))) {
            // 切り上げでも切り下げでも値が変わらない = 小数点以下が0
            return bd.setScale(0).toString();
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private static String getDispNum(final String num) {
        return getDispNum(num, "0");
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String form = SEME2.equals(_param._semester) ? "KNJM501M_2.frm" : "KNJM501M_1.frm";
        
        final List studentList = getStudentList(db2);
        for (int line = 0; line < studentList.size(); line++) {
            final Student student = (Student) studentList.get(line);
            svf.VrSetForm(form, 4);
            
            final String title = "成績通知表" + (SEME1.equals(_param._semester) ? "（" + StringUtils.defaultString((String) _param._semesterName.get(SEME1)) + "）" : "");
            svf.VrsOut("TITLE", title); // タイトル
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度"); // 年度
            svf.VrsOut("SCHREG_NO", student._schregno); // 学籍番号
            
            final String suf1 = getMS932ByteLength(student._name) > 30 ? "3" : getMS932ByteLength(student._name) > 20 ? "2" : "1";
            svf.VrsOut("NAME" + suf1, student._name); // 氏名
            
            svf.VrsOut("SEMESTER_NAME1", (String) _param._semesterName.get(SEME1));
            svf.VrsOut("SEMESTER_NAME2", (String) _param._semesterName.get(SEME2));
            svf.VrsOut("SEMESTER_NAME3", (String) _param._semesterName.get(SEMEALL));
            svf.VrsOutn("SEMESTER_NAME4", 1, (String) _param._semesterName.get(SEME1));
            svf.VrsOutn("SEMESTER_NAME4", 2, (String) _param._semesterName.get(SEMEALL));
            svf.VrsOut("SCHOOL_NAME", _param._certifSchoolRemark3);

            for (int i = 1; i <= 3; i++) {
                final String remarkId = String.valueOf(6 + i);
                final String remark = (String) _param._hreportRemarkTDat.get(remarkId);
                svf.VrsOut("TEXT" + String.valueOf(i), remark); // 文言
            }

            boolean printSem1 = true;
            if (SEME2.equals(_param._semester) && SEME2.equals(student._entDateSemester) &&
                    (null == student.getAttendNissu(SEME1, null) && null == getDispNum(student._at93Sem1, null) && null == getDispNum(student._at94Sem1, null) && null == getDispNum(student._atTotalSem1, null))) {
                // 後期に出力した際、入学日付が今年度後期かつ前期の出欠の値が0の生徒は前期の出欠欄はブランクで表示する
                printSem1 = false;
            }
            if (printSem1) {
                svf.VrsOutn("ATTEND_SUM", 1, student.getAttendNissu(SEME1)); // 出校日数
                svf.VrsOutn("SP_HR", 1, getDispNum(student._at93Sem1)); // ホームルーム
                svf.VrsOutn("SP_EVENT", 1, getDispNum(student._at94Sem1)); // 行事
                svf.VrsOutn("SP_TOTAL", 1, getDispNum(student._atTotalSem1)); // 計
            }
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._tkijun)); // 日付

            if (SEME1.equals(_param._semester)) {
                svf.VrsOut("STAFFNAME" + (getMS932ByteLength(_param._trcd1Staffname) > 20 ? "2" : "1"), _param._trcd1Staffname); // 担任名
            } else {
                svf.VrsOutn("ATTEND_SUM", 2, student.getAttendNissu(SEMEALL)); // 出校日数
                svf.VrsOutn("SP_HR", 2, getDispNum(student._at93Sem9)); // ホームルーム
                svf.VrsOutn("SP_EVENT", 2, getDispNum(student._at94Sem9)); // 行事
                svf.VrsOutn("SP_TOTAL", 2, getDispNum(student._atTotalSem9)); // 計

                svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(_param._output)); // 印刷日付
                svf.VrsOut("SCHOOL_NAME2", StringUtils.defaultString(_param._certifSchoolSchoolName) + StringUtils.defaultString(_param._certifSchoolJobName)); // 学校名
                svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolPrincipalName); // 学校長名
                svf.VrsOut("JOB_NAME", "担任"); // 担任
                svf.VrsOut("TEACHER_NAME", _param._trcd1Staffname); // 担任名
            }

            int shutokuKamokuCount = 0;
            int shutokuKamokuTanni = 0;
            for (int i = 0; i < student._subclassList.size(); i++) {
                final Subclass subclass = (Subclass) student._subclassList.get(i);
                if (null == subclass._getCredit || 0 == Integer.parseInt(subclass._getCredit)) {
                    continue;
                }
                shutokuKamokuCount += 1;
                shutokuKamokuTanni += Integer.parseInt(subclass._getCredit);
            }
            final String shutokuText;
            if (0 == shutokuKamokuTanni) {
                shutokuText = null;
            } else {
                shutokuText = "あなたは今年度" + shutokuKamokuCount + "科目 "  + shutokuKamokuTanni + "単位を修得したので通知します。";
            }
            
            for (int i = 0; i < student._subclassList.size(); i++) {
                final Subclass subclass = (Subclass) student._subclassList.get(i);

                final String suf2 = getMS932ByteLength(subclass._subclassname) > 30 ? "3" : getMS932ByteLength(subclass._subclassname) > 20 ? "2" : "1";
                svf.VrsOut("SUBCLASS" + suf2, subclass._subclassname); // 科目
                
                // log.debug(" subclass " + StringUtils.defaultString(subclass._zenkiFlg, " ") + " : " + StringUtils.defaultString(subclass._koukiFlg, " ") + " : " + subclass._subclassname);

                svf.VrsOut("CREDIT", subclass._credits); // 単位数
                svf.VrsOut("REPORT", subclass._repSeqAll); // レポート数
                svf.VrsOut("REQ_TIME", subclass._schSeqMin); // 必要時数

                if (!"1".equals(subclass._koukiFlg)) {
                    if (RISHU.equals(subclass._kind)) {
                        svf.VrsOut("REP_SUC1", getRepNum(subclass._repgou1Count)); // レポート合格数
                        svf.VrsOut("INT_TIME1", getDispNum(subclass._at1shussekiCount)); // 面接時数
                    }
                    svf.VrsOut("SCORE1", subclass._sem1IntrValue); // 成績
                    svf.VrsOut("LEADED_SCORE1", subclass._sem1TermValue); // 補指後の成績
                }

                if (!SEME1.equals(_param._semester) || "1".equals(subclass._zenkiFlg)) {
                    svf.VrsOut("SCORE2", subclass._sem2IntrValue); // 成績
                    svf.VrsOut("LEADED_SCORE2", subclass._sem2TermValue); // 補指後の成績

                    if (RISHU.equals(subclass._kind)) {
                        svf.VrsOut("REP_SUC3", getRepNum(subclass._repgou9Count)); // レポート合格数
                        svf.VrsOut("INT_TIME3", getDispNum(subclass._at9totalCount)); // 面接時数
                    }
                    svf.VrsOut("SCORE3", subclass._gradValue2); // 成績
                    svf.VrsOut("VALUE", subclass._gradValue); // 評定
                    String gouhi = null;
                    if (null == subclass._getCredit || 0 == Integer.parseInt(subclass._getCredit)) {
                    	gouhi = "否";
                    } else if (1 <= Integer.parseInt(subclass._getCredit)) {
                    	gouhi = "合";
                    }
                    if (null != gouhi) {
                    	svf.VrsOut("GET", gouhi); // 合否
                    }
                    svf.VrsOut("GET_CREDIT", subclass._getCredit); // 修得単位
                }

                StringBuffer remark = new StringBuffer();
                String space = "";
                if (SEME1.equals(_param._semester) && null != subclass._at1housouCountOrg) {
                    remark.append(space).append("放送視聴：" + getDispNum(subclass._at1housouCountOrg));
                    space = " ";
//                } else if (!SEME1.equals(_param._semester) && null != subclass._at9housouCountOrg) {
//                    remark.append(space).append("放送視聴：" + getDispNum(subclass._at9housouCountOrg));
//                    space = " ";
                }
                if (KOUNIN.equals(subclass._kind)) {
                    remark.append(space).append("高認"); 
                    space = " ";
                } else if (ZOUTAN.equals(subclass._kind)) {
                    remark.append(space).append("技能審査"); 
                    space = " ";
                }
                svf.VrsOut("REMARK", remark.toString()); // 備考
                
                if (!SEME1.equals(_param._semester)) {
                    svf.VrsOut("FIELD1", shutokuText);
                }
                svf.VrEndRecord();
            }
            for (int i = student._subclassList.size(); i < 11; i++) {
                svf.VrsOut("SUBCLASS1", "\n");
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }
    
    private static String getRepNum(final String count) {
        if (null == count || !NumberUtils.isDigits(count)) {
            return "0";
        }
        return count;
    }

    private List getStudentList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(getSchregSql());
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student student = new Student(rs.getString("SCHREGNO"));
                student._inoutcd = rs.getString("INOUTCD");
                student._name = rs.getString("NAME");
                student._baseRemark1 = rs.getString("BASE_REMARK1");
                student._at93Sem1 = rs.getString("AT93SEME1");
                student._at94Sem1 = rs.getString("AT94SEME1");
                student._atTotalSem1 = rs.getString("ATTOTALSEME1");
                student._at93Sem9 = rs.getString("AT93SEME9");
                student._at94Sem9 = rs.getString("AT94SEME9");
                student._atTotalSem9 = rs.getString("ATTOTALSEME9");
                student._entDateSemester = rs.getString("ENT_DATE_SEMESTER");
                list.add(student);
            }
       } catch (Exception ex) {
            log.fatal("exception!", ex);
       } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
       }

       try {
           final String schAttendSql = getSchAttendSql();
//           log.debug(" schattend sql = " + schAttendSql);
           ps = db2.prepareStatement(schAttendSql);
           rs = ps.executeQuery();
           while (rs.next()) {
               final Student student = getStudent(list, rs.getString("SCHREGNO"));
               if (null == student) {
                   continue;
               }
               student._attendList.add(new Attend(rs.getString("SEMESTER"), rs.getString("SCHOOLINGKINDCD"), rs.getString("NAMESPARE1"), rs.getString("EXECUTEDATE"), rs.getString("PERIODCD"), rs.getBigDecimal("CREDIT_TIME")));
           }
      
       } catch (Exception ex) {
           log.fatal("exception!", ex);
       } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
       }

       try {
            final String subclassSql = getSubclassSql();
            // log.debug(" subclass sql = " + subclassSql);
            ps = db2.prepareStatement(subclassSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student student = getStudent(list, rs.getString("SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String year = rs.getString("YEAR");
                final String chaircd = rs.getString("CHAIRCD");
                final String classcd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String repSeqAll = rs.getString("REP_SEQ_ALL");
                final String schSeqAll = rs.getString("SCH_SEQ_ALL");
                final String schSeqMin = rs.getString("SCH_SEQ_MIN");
                final String credits = rs.getString("CREDITS");
                
                final String at11Count = rs.getString("AT11_COUNT");
                final String at13Count = rs.getString("AT13_COUNT");
                final String at1shussekiCount = sum(new String[] {at11Count, at13Count}); // 前期の面接時数はスクーリング種別=2(放送)は含めない。
                final String at91Count = rs.getString("AT91_COUNT");
                final String at93Count = rs.getString("AT93_COUNT");
                final String at9shussekiCount = sum(new String[] {at91Count, at93Count});

                String at1housouCountOrg = rs.getString("AT12_COUNT_ORG");
                String at9housouCountOrg = rs.getString("AT92_COUNT_ORG");
                String at1housouCount = rs.getString("AT12_COUNT");
                String at9housouCount = rs.getString("AT92_COUNT");
                if (NumberUtils.isNumber(schSeqMin)) {
                    // 最低出席回数 - スクーリング数（スクーリング種別2以外）の合計 = 不足のスクーリング数 (マイナスの場合は0とする)
                    //  不足のスクーリング数 >= 放送で認められる数 の場合は放送で認められる数を加算する。
                    //  不足のスクーリング数 <  放送で認められる数 の場合は不足のスクーリング数を加算する。
                    final int div6 = Integer.parseInt(schSeqMin) * Integer.parseInt(StringUtils.defaultString(_param._m020Name1, "6")) / 10;
                    final double at1shusseki = NumberUtils.isNumber(at1shussekiCount) ? Double.parseDouble(at1shussekiCount) : 0;
                    final double housouDeMitomerareruMax1 = Math.min(div6, Math.max(0.0, Integer.parseInt(schSeqMin) - at1shusseki)); // 放送で認められる最大数
                    if (NumberUtils.isNumber(at1housouCount) && Double.parseDouble(at1housouCount) > housouDeMitomerareruMax1) {
                        at1housouCount = String.valueOf(Integer.parseInt(schSeqMin) - housouDeMitomerareruMax1);
                    }
                    final double at9shusseki = NumberUtils.isNumber(at9shussekiCount) ? Double.parseDouble(at9shussekiCount) : 0;
                    final double housouDeMitomerareruMax9 = Math.min(div6, Math.max(0.0, Integer.parseInt(schSeqMin) - at9shusseki)); // 放送で認められる最大数
                    if (NumberUtils.isNumber(at9housouCount) && Double.parseDouble(at9housouCount) > housouDeMitomerareruMax9) {
                        at9housouCount = String.valueOf(housouDeMitomerareruMax9);
                    }
                }
                if (NumberUtils.isNumber(at1housouCountOrg) && 0.0 == Double.parseDouble(at1housouCountOrg)) at1housouCountOrg = null;
                if (NumberUtils.isNumber(at9housouCountOrg) && 0.0 == Double.parseDouble(at9housouCountOrg)) at9housouCountOrg = null;
                if (NumberUtils.isNumber(at1housouCount) && 0.0 == Double.parseDouble(at1housouCount)) at1housouCount = null;
                if (NumberUtils.isNumber(at9housouCount) && 0.0 == Double.parseDouble(at9housouCount)) at9housouCount = null;
                
                //log.debug(" (at1h, at9h) = (" + at1housouCountOrg + " => " + at1housouCount + ", " + at9housouCountOrg + " => " + at9housouCount + ")");

                final String at9totalCount = sum(new String[] {at91Count, at9housouCount, at93Count});
                final String repgou1Count = rs.getString("REPGOU1_COUNT");
                final String repgou9Count = rs.getString("REPGOU9_COUNT");
                // log.debug(" subclass " + subclasscd + " 前期面接時数:" + ArrayUtils.toString(new String[] {at11Count, at13Count}) + ", 通年面接時数:" + ArrayUtils.toString(new String[] {at91Count, at9housouCount, at93Count}) + " : " + subclassname);
                final String sem1IntrValue = rs.getString("SEM1_INTR_VALUE");
                final String sem1TermValue = rs.getString("SEM1_TERM_VALUE");
                final String sem2IntrValue = rs.getString("SEM2_INTR_VALUE");
                final String sem2TermValue = rs.getString("SEM2_TERM_VALUE");
                final String gradValue2 = rs.getString("GRAD_VALUE2");
                final String gradValue = rs.getString("GRAD_VALUE");
                final String getCredit = rs.getString("GET_CREDIT");
                final String zenkiFlg = rs.getString("ZENKI_FLG");
                final String koukiFlg = rs.getString("KOUKI_FLG");
                final String kind = rs.getString("KIND");
                final Subclass subclass = new Subclass(year, chaircd, classcd, schoolKind, curriculumCd, subclasscd, subclassname, repSeqAll, schSeqAll, schSeqMin, credits, at1housouCountOrg, at1shussekiCount, repgou1Count, at9housouCountOrg, at9totalCount, repgou9Count, sem1IntrValue, sem1TermValue, sem2IntrValue, sem2TermValue, gradValue2, gradValue, getCredit, zenkiFlg, koukiFlg, kind);
                student._subclassList.add(subclass);
            }
       } catch (Exception ex) {
            log.fatal("exception!", ex);
       } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
       }
       return list;
    }
    
    private static String sum(final String[] nums) {
        String sum = null;
        for (int i = 0; i < nums.length; i++) {
            if (null == sum) {
                sum = nums[i];
            } else {
                sum = String.valueOf((Double.parseDouble(NumberUtils.isNumber(sum) ? sum : "0.0")) +
                                     (Double.parseDouble(NumberUtils.isNumber(nums[i]) ? nums[i] : "0.0")));
            }
        }
        return sum;
    }
    
    private String getSchregSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("    WITH SCHREGNOS AS ( ");
        stb.append("        SELECT ");
        stb.append("            T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, ");
        stb.append("            T3.SEMESTER AS ENT_DATE_SEMESTER ");
        stb.append("        FROM ");
        stb.append("            SCHREG_REGD_DAT T1 ");
        stb.append("            LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("            LEFT JOIN SEMESTER_MST T3 ON T3.YEAR = T1.YEAR ");
        stb.append("                AND T3.SEMESTER <> '9' ");
        stb.append("                AND T2.ENT_DATE BETWEEN T3.SDATE AND T3.EDATE ");
        stb.append("        WHERE ");
        stb.append("            T1.YEAR = '" + _param._year + "' ");
        stb.append("            AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("            AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
        stb.append("            AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("    ), SPECIAL_ATTEND AS ( ");
        stb.append("        SELECT ");
        stb.append("            VALUE(SEMESTER, '9') AS SEMESTER, SCHREGNO, CLASSCD, SUM(CREDIT_TIME) AS CREDIT_TIME ");
        stb.append("        FROM ");
        stb.append("            SPECIALACT_ATTEND_DAT T1 ");
        stb.append("        WHERE ");
        stb.append("            YEAR = '" + _param._year + "' ");
        stb.append("            AND CLASSCD IN ('93', '94') ");
        stb.append("            AND ATTENDDATE <= '" + _param._tkijun + "' ");
        stb.append("            AND EXISTS (SELECT 'X' FROM SCHREGNOS WHERE SCHREGNO = T1.SCHREGNO) ");
        stb.append("        GROUP BY ");
        stb.append("            GROUPING SETS((SCHREGNO, CLASSCD), (SEMESTER, SCHREGNO, CLASSCD)) ");
        stb.append("    ) ");
        stb.append("    SELECT ");
        stb.append("        T1.SCHREGNO, ");
        stb.append("        T1.ENT_DATE_SEMESTER, ");
        stb.append("        T4.INOUTCD, ");
        stb.append("        T4.NAME, ");
        stb.append("        T7.BASE_REMARK1, ");
        stb.append("        AT93SEM1.CREDIT_TIME AS AT93SEME1, ");
        stb.append("        AT94SEM1.CREDIT_TIME AS AT94SEME1, ");
        stb.append("        CASE WHEN AT93SEM1.CREDIT_TIME IS NULL AND AT94SEM1.CREDIT_TIME IS NULL THEN CAST(NULL AS DECIMAL(5,1)) ELSE ");
        stb.append("            VALUE(AT93SEM1.CREDIT_TIME, 0.0) + VALUE(AT94SEM1.CREDIT_TIME, 0.0) ");
        stb.append("        END AS ATTOTALSEME1, ");
        stb.append("        AT93SEM9.CREDIT_TIME AS AT93SEME9, ");
        stb.append("        AT94SEM9.CREDIT_TIME AS AT94SEME9, ");
        stb.append("        CASE WHEN AT93SEM9.CREDIT_TIME IS NULL AND AT94SEM9.CREDIT_TIME IS NULL THEN CAST(NULL AS DECIMAL(5,1)) ELSE ");
        stb.append("            VALUE(AT93SEM9.CREDIT_TIME, 0.0) + VALUE(AT94SEM9.CREDIT_TIME, 0.0) ");
        stb.append("        END AS ATTOTALSEME9 ");
        stb.append("    FROM ");
        stb.append("        SCHREGNOS T1 ");
        stb.append("        INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("        LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T7 ON T7.SCHREGNO = T1.SCHREGNO ");
        stb.append("            AND T7.YEAR = T1.YEAR ");
        stb.append("            AND T7.BASE_SEQ = '001' ");
        stb.append("        LEFT JOIN SPECIAL_ATTEND AT93SEM1 ON AT93SEM1.SEMESTER = '1' AND AT93SEM1.SCHREGNO = T1.SCHREGNO AND AT93SEM1.CLASSCD = '93' ");
        stb.append("        LEFT JOIN SPECIAL_ATTEND AT94SEM1 ON AT94SEM1.SEMESTER = '1' AND AT94SEM1.SCHREGNO = T1.SCHREGNO AND AT94SEM1.CLASSCD = '94' ");
        stb.append("        LEFT JOIN SPECIAL_ATTEND AT93SEM9 ON AT93SEM9.SEMESTER = '9' AND AT93SEM9.SCHREGNO = T1.SCHREGNO AND AT93SEM9.CLASSCD = '93' ");
        stb.append("        LEFT JOIN SPECIAL_ATTEND AT94SEM9 ON AT94SEM9.SEMESTER = '9' AND AT94SEM9.SCHREGNO = T1.SCHREGNO AND AT94SEM9.CLASSCD = '94' ");
        stb.append("    ORDER BY ");
        stb.append("        T1.SCHREGNO ");
        return stb.toString();
    }
    
    private String getSchAttendSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.SCHOOLINGKINDCD, ");
        stb.append("         T4.NAMESPARE1, ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CREDIT_TIME ");
        stb.append("     FROM ");
        stb.append("         SCH_ATTEND_DAT T1 ");
        stb.append("         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER <> '9' ");
        stb.append("             AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("         INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("             AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("             AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("         INNER JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001' ");
        stb.append("             AND T4.NAMECD2 = T1.SCHOOLINGKINDCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.EXECUTEDATE <= '" + _param._skijun + "' ");
        stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("         AND T4.NAMESPARE1 = '1' ");
        stb.append(" UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.SEMESTER, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         CAST(NULL AS VARCHAR(1)) AS SCHOOLINGKINDCD, ");
        stb.append("         CAST(NULL AS VARCHAR(1)) AS NAMESPARE1, ");
        stb.append("         T1.ATTENDDATE AS EXECUTEDATE, ");
        stb.append("         T1.PERIODF AS PERIODCD, ");
        stb.append("         T1.CREDIT_TIME ");
        stb.append("     FROM ");
        stb.append("         SPECIALACT_ATTEND_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.ATTENDDATE <= '" + _param._tkijun + "' ");
        stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     SCHREGNO, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOLINGKINDCD, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS NAMESPARE1, ");
        stb.append("     INPUT_DATE AS EXECUTEDATE, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS PERIODCD, ");
        stb.append("     CAST(NULL AS DECIMAL(5, 1)) AS CREDIT_TIME ");
        stb.append(" FROM ");
        stb.append("     TEST_ATTEND_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND INPUT_DATE <= '" + _param._skijun + "' ");
        stb.append("     AND SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     SCHREGNO, ");
        stb.append("     INPUT_DATE ");
        return stb.toString();
    }

    private String getSubclassSql() {
        final StringBuffer stb = new StringBuffer();
        
        stb.append(" WITH SCHREGNOS(SCHREGNO) AS ( ");
        String union = "";
        for (int i = 0; i < _param._categorySelected.length; i++) {
            stb.append(union);
            stb.append(" VALUES(CAST('" + _param._categorySelected[i] + "' AS VARCHAR(8)))");
            union = " UNION ALL ";
        }
        stb.append(" ), CHAIR AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.SEMESTER, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         CASE WHEN (T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHOOL_KIND) ");
        stb.append("                IN (SELECT NAME1, NAME2, NAME3, VALUE(ABBV1, 'H') FROM NAME_MST WHERE NAMECD1 = 'M015') THEN '1' ");
        stb.append("         END AS ZENKI_FLG, ");
        stb.append("         CASE WHEN (T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHOOL_KIND) ");
        stb.append("                IN (SELECT NAME1, NAME2, NAME3, VALUE(ABBV1, 'H') FROM NAME_MST WHERE NAMECD1 = 'M016') THEN '1' ");
        stb.append("         END AS KOUKI_FLG, ");
        stb.append("         T2.REP_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_MIN, ");
        stb.append("         MAX(T6.CREDITS) AS CREDITS, ");
        stb.append("         T3.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         CHAIR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_CORRES_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("     INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO ");
        stb.append("         AND T5.YEAR = T1.YEAR ");
        stb.append("         AND T5.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN CREDIT_MST T6 ON T6.YEAR = T1.YEAR ");
        stb.append("         AND T6.COURSECD = T5.COURSECD ");
        stb.append("         AND T6.GRADE = T5.GRADE ");
        stb.append("         AND T6.MAJORCD = T5.MAJORCD ");
        stb.append("         AND T6.COURSECODE = T5.COURSECODE ");
        stb.append("         AND T6.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.SEMESTER <= '" + _param._semester + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("         AND EXISTS (SELECT 'X' FROM SCHREGNOS WHERE T3.SCHREGNO = SCHREGNO) ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.SEMESTER, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T2.REP_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_MIN, ");
        stb.append("         T3.SCHREGNO ");
        
        stb.append(" ), CHAIR2 AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.ZENKI_FLG, ");
        stb.append("         T1.KOUKI_FLG, ");
        stb.append("         T1.REP_SEQ_ALL, ");
        stb.append("         T1.SCH_SEQ_ALL, ");
        stb.append("         T1.SCH_SEQ_MIN, ");
        stb.append("         T1.CREDITS, ");
        stb.append("         T1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         CHAIR T1 ");
        
        stb.append(" ), KOUGAI AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T6.CREDITS, "); // 高認の場合、単位マスタの単位
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T2.CREDITS AS GET_CREDIT, ");
        stb.append("         '" + KOUNIN + "' AS KIND ");
        stb.append("     FROM ");
        stb.append("         SCH_COMP_DETAIL_DAT T1 ");
        stb.append("     LEFT JOIN ( SELECT ");
        stb.append("                     SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SUM(CREDITS) AS CREDITS ");
        stb.append("                 FROM SCHREG_QUALIFIED_DAT T2 ");
        stb.append("                 WHERE YEAR = '" + _param._year + "' ");
        stb.append("                       AND CONDITION_DIV = '3' ");
        stb.append("                       AND EXISTS (SELECT 'X' FROM SCHREGNOS WHERE SCHREGNO = T2.SCHREGNO) ");
        stb.append("                 GROUP BY ");
        stb.append("                     SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        stb.append("               ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("                   AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T5.YEAR = T1.YEAR ");
        stb.append("         AND T5.SEMESTER = '" + _param._semester + "' ");
        stb.append("     LEFT JOIN CREDIT_MST T6 ON T6.YEAR = T1.YEAR ");
        stb.append("         AND T6.COURSECD = T5.COURSECD ");
        stb.append("         AND T6.GRADE = T5.GRADE ");
        stb.append("         AND T6.MAJORCD = T5.MAJORCD ");
        stb.append("         AND T6.COURSECODE = T5.COURSECODE ");
        stb.append("         AND T6.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("         AND EXISTS (SELECT 'X' FROM SCHREGNOS WHERE SCHREGNO = T1.SCHREGNO) ");
        stb.append("         AND T1.KOUNIN = '1' ");
        stb.append(" UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.ADD_CREDIT AS CREDITS, "); // 増単の場合、ADD_CREDIT
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T2.CREDITS AS GET_CREDIT, ");
        stb.append("         '" + ZOUTAN + "' AS KIND ");
        stb.append("     FROM ");
        stb.append("         SCH_COMP_DETAIL_DAT T1 ");
        stb.append("         LEFT JOIN ( SELECT ");
        stb.append("                         SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SUM(CREDITS) AS CREDITS ");
        stb.append("                     FROM SCHREG_QUALIFIED_DAT T2 ");
        stb.append("                     WHERE YEAR = '" + _param._year + "' ");
        stb.append("                           AND CONDITION_DIV = '1' ");
        stb.append("                           AND EXISTS (SELECT 'X' FROM SCHREGNOS WHERE SCHREGNO = T2.SCHREGNO) ");
        stb.append("                     GROUP BY ");
        stb.append("                         SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        stb.append("                   ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("                       AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("         AND EXISTS (SELECT 'X' FROM SCHREGNOS WHERE SCHREGNO = T1.SCHREGNO) ");
        stb.append("         AND T1.KOUNIN IS NULL ");
        
        stb.append(" ), ATTEND_ALL AS ( ");
        stb.append("   SELECT  ");
        stb.append("       T1.YEAR,  ");
        stb.append("       T2.SEMESTER,  ");
        stb.append("       T3.CLASSCD,  ");
        stb.append("       T3.SCHOOL_KIND,  ");
        stb.append("       T3.CURRICULUM_CD,  ");
        stb.append("       T3.SUBCLASSCD,  ");
        stb.append("       T1.SCHREGNO,  ");
        stb.append("       T1.SCHOOLINGKINDCD,  ");
        stb.append("       T4.NAMESPARE1,  ");
        stb.append("       T1.EXECUTEDATE,  ");
        stb.append("       T1.PERIODCD,  ");
        stb.append("       T1.CREDIT_TIME, ");
        stb.append("       T3.SCH_SEQ_MIN ");
        stb.append("   FROM  ");
        stb.append("       SCH_ATTEND_DAT T1  ");
        stb.append("       INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR  ");
        stb.append("           AND T2.SEMESTER <> '9'  ");
        stb.append("           AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE  ");
        stb.append("       INNER JOIN CHAIR T3 ON T3.YEAR = T1.YEAR  ");
        stb.append("           AND T3.SEMESTER = T2.SEMESTER  ");
        stb.append("           AND T3.CHAIRCD = T1.CHAIRCD  ");
        stb.append("           AND T3.SCHREGNO = T1.SCHREGNO  ");
        stb.append("       LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'M001'  ");
        stb.append("           AND T4.NAMECD2 = T1.SCHOOLINGKINDCD  ");

        stb.append(" ), ATTEND_KIND1 AS ( ");
        stb.append("     SELECT  ");
        stb.append("         1 AS KIND, ");
        stb.append("         YEAR, VALUE(SEMESTER, '9') AS SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         MAX(SCH_SEQ_MIN) AS SCH_SEQ_MIN, ");
        stb.append("         COUNT(DISTINCT EXECUTEDATE) AS JISU1, ");
        stb.append("         COUNT(DISTINCT EXECUTEDATE) AS JISU2 ");
        stb.append("     FROM ATTEND_ALL ");
        stb.append("     WHERE NAMESPARE1 = '1' AND SCHOOLINGKINDCD = '1' ");
        stb.append("     GROUP BY ");
        stb.append("         GROUPING SETS ((YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO), ");
        stb.append("                        (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO)) ");
        stb.append(" ), ATTEND_KIND2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         2 AS KIND, ");
        stb.append("         YEAR, VALUE(SEMESTER, '9') AS SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         SCH_SEQ_MIN, ");
        stb.append("         SCH_SEQ_MIN * INT(VALUE(L1.NAME1,'6')) / 10 AS LIMIT, ");
        stb.append("         SUM(CREDIT_TIME) AS JISU1, ");
        stb.append("         VALUE(INT(MIN(SCH_SEQ_MIN * INT(VALUE(L1.NAME1,'6')) / 10, SUM(CREDIT_TIME))), 0) AS JISU2 ");
        stb.append("     FROM ATTEND_ALL ");
        stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'M020' AND L1.NAMECD2 = '01'");
        stb.append("     WHERE SCHOOLINGKINDCD = '2' ");
        stb.append("     GROUP BY ");
        stb.append("         GROUPING SETS ((YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, SCH_SEQ_MIN, L1.NAME1), ");
        stb.append("                        (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, SCH_SEQ_MIN, L1.NAME1)) ");
        stb.append(" ), ATTEND_KIND3 AS ( ");
        stb.append("     SELECT  ");
        stb.append("         3 AS KIND, ");
        stb.append("         YEAR, VALUE(SEMESTER, '9') AS SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, ");
        stb.append("         MAX(SCH_SEQ_MIN) AS SCH_SEQ_MIN, ");
        stb.append("         SUM(CREDIT_TIME) AS JISU1, ");
        stb.append("         SUM(CREDIT_TIME) AS JISU2 ");
        stb.append("     FROM ATTEND_ALL ");
        stb.append("     WHERE NAMESPARE1 = '1' AND SCHOOLINGKINDCD <> '1' ");
        stb.append("     GROUP BY ");
        stb.append("         GROUPING SETS ((YEAR, SEMESTER, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, SCH_SEQ_MIN), ");
        stb.append("                        (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO, SCH_SEQ_MIN)) ");

        stb.append(" ), MAX_REPRESENT_SEQ AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         MAX(T1.REPRESENT_SEQ) AS REPRESENT_SEQ ");
        stb.append("     FROM ");
        stb.append("         REP_PRESENT_DAT T1 ");
        stb.append("     INNER JOIN CHAIR T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO ");
        stb.append(" ), MAX_RECEIPT_DATE AS ( ");
        stb.append("     SELECT  ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.REPRESENT_SEQ, ");
        stb.append("         MAX(T1.RECEIPT_DATE) AS RECEIPT_DATE ");
        stb.append("     FROM ");
        stb.append("         REP_PRESENT_DAT T1 ");
        stb.append("     INNER JOIN MAX_REPRESENT_SEQ T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.REPRESENT_SEQ ");
        stb.append(" ), MAX_REP_PRESENT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.STANDARD_SEQ, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.REPRESENT_SEQ, ");
        stb.append("         T1.RECEIPT_DATE ");
        stb.append("     FROM ");
        stb.append("         REP_PRESENT_DAT T1 ");
        stb.append("     INNER JOIN MAX_RECEIPT_DATE T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.REPRESENT_SEQ = T1.REPRESENT_SEQ ");
        stb.append("         AND T2.RECEIPT_DATE = T1.RECEIPT_DATE ");
        stb.append("     INNER JOIN NAME_MST T3 ON T3.NAMECD1 = 'M003' ");
        stb.append("         AND T3.NAMECD2 = T1.GRAD_VALUE ");
        stb.append("     WHERE ");
        stb.append("         T3.NAMESPARE1 = '1' ");
        stb.append(" ), REP_GOUKAKU_COUNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         COUNT(*) AS COUNT ");
        stb.append("     FROM ");
        stb.append("         MAX_REP_PRESENT T1 ");
        stb.append("         INNER JOIN REP_STANDARDDATE_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("             AND T3.CLASSCD = T1.CLASSCD ");
        stb.append("             AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("             AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("             AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             AND T3.STANDARD_SEQ = T1.STANDARD_SEQ ");
        stb.append("         INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append("             AND T3.STANDARD_DATE BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("     GROUP BY ");
        stb.append("         T1.YEAR, ");
        stb.append("         T2.SEMESTER, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.SCHREGNO ");
        stb.append(" ), SUBCLASS AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     '" + RISHU + "' AS KIND, ");
        stb.append("     AT11.JISU2 AS AT11_COUNT, ");
        stb.append("     AT21.JISU1 AS AT12_COUNT_ORG, ");
        stb.append("     AT21.JISU2 AS AT12_COUNT, ");
        stb.append("     AT31.JISU2 AS AT13_COUNT, ");
        stb.append("     REPGOU1.COUNT AS REPGOU1_COUNT, ");
        stb.append("     AT19.JISU2 AS AT91_COUNT, ");
        stb.append("     AT29.JISU1 AS AT92_COUNT_ORG, ");
        stb.append("     AT29.JISU2 AS AT92_COUNT, ");
        stb.append("     AT39.JISU2 AS AT93_COUNT, ");
        stb.append("     REPGOU9.COUNT AS REPGOU9_COUNT, ");
        stb.append("     T2.SEM1_INTR_VALUE, ");
        stb.append("     T2.SEM1_TERM_VALUE, ");
        stb.append("     T2.SEM2_INTR_VALUE, ");
        stb.append("     T2.SEM2_TERM_VALUE, ");
        stb.append("     T2.GRAD_VALUE2, ");
        stb.append("     T2.GRAD_VALUE, ");
        stb.append("     T2.GET_CREDIT ");
        stb.append(" FROM ");
        stb.append("     CHAIR2 T1 ");
        stb.append("     LEFT JOIN RECORD_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN REP_GOUKAKU_COUNT REPGOU1 ON REPGOU1.YEAR = T1.YEAR ");
        stb.append("         AND REPGOU1.CLASSCD = T1.CLASSCD ");
        stb.append("         AND REPGOU1.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND REPGOU1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND REPGOU1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND REPGOU1.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND REPGOU1.SEMESTER = '1' ");
        stb.append("     LEFT JOIN REP_GOUKAKU_COUNT REPGOU9 ON REPGOU9.YEAR = T1.YEAR ");
        stb.append("         AND REPGOU9.CLASSCD = T1.CLASSCD ");
        stb.append("         AND REPGOU9.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND REPGOU9.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND REPGOU9.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND REPGOU9.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND REPGOU9.SEMESTER = '9' ");

        stb.append("     LEFT JOIN ATTEND_KIND1 AT11 ON AT11.YEAR = T1.YEAR ");
        stb.append("         AND AT11.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT11.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT11.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT11.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT11.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT11.SEMESTER = '1' ");
        stb.append("     LEFT JOIN ATTEND_KIND2 AT21 ON AT21.YEAR = T1.YEAR ");
        stb.append("         AND AT21.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT21.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT21.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT21.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT21.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT21.SEMESTER = '1' ");
        stb.append("     LEFT JOIN ATTEND_KIND3 AT31 ON AT31.YEAR = T1.YEAR ");
        stb.append("         AND AT31.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT31.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT31.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT31.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT31.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT31.SEMESTER = '1' ");
        stb.append("     LEFT JOIN ATTEND_KIND1 AT19 ON AT19.YEAR = T1.YEAR ");
        stb.append("         AND AT19.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT19.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT19.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT19.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT19.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT19.SEMESTER = '9' ");
        stb.append("     LEFT JOIN ATTEND_KIND2 AT29 ON AT29.YEAR = T1.YEAR ");
        stb.append("         AND AT29.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT29.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT29.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT29.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT29.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT29.SEMESTER = '9' ");
        stb.append("     LEFT JOIN ATTEND_KIND3 AT39 ON AT39.YEAR = T1.YEAR ");
        stb.append("         AND AT39.CLASSCD = T1.CLASSCD ");
        stb.append("         AND AT39.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND AT39.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND AT39.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND AT39.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND AT39.SEMESTER = '9' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS CHAIRCD, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS ZENKI_FLG, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS KOUKI_FLG, ");
        stb.append("     CAST(NULL AS SMALLINT) AS REP_SEQ_ALL, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SCH_SEQ_ALL, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SCH_SEQ_MIN, ");
        stb.append("     T1.CREDITS, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.KIND, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT11_COUNT, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT12_COUNT_ORG, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT12_COUNT, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT13_COUNT, ");
        stb.append("     CAST(NULL AS INT) AS REPGOU1_COUNT, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT91_COUNT, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT92_COUNT_ORG, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT92_COUNT, ");
        stb.append("     CAST(NULL AS DECIMAL) AS AT93_COUNT, ");
        stb.append("     CAST(NULL AS INT) AS REPGOU9_COUNT, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SEM1_INTR_VALUE, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SEM1_TERM_VALUE, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SEM2_INTR_VALUE, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SEM2_TERM_VALUE, ");
        stb.append("     CAST(NULL AS SMALLINT) AS GRAD_VALUE2, ");
        stb.append("     CAST(NULL AS SMALLINT) AS GRAD_VALUE, ");
        stb.append("     T1.GET_CREDIT ");
        stb.append("     FROM ");
        stb.append("         KOUGAI T1 ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     T8.SUBCLASSNAME ");
        stb.append(" FROM SUBCLASS T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST T8 ON T8.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T8.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T8.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T8.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.KIND ");
        return stb.toString();
    }
    
    private static class Subclass {
        final String _year;
        final String _chaircd;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final String _repSeqAll;
        final String _schSeqAll;
        final String _schSeqMin;
        final String _credits;
        final String _at1housouCountOrg;
        final String _at1shussekiCount;
        final String _repgou1Count;
        final String _at9housouCountOrg;
        final String _at9totalCount;
        final String _repgou9Count;
        final String _sem1IntrValue;
        final String _sem1TermValue;
        final String _sem2IntrValue;
        final String _sem2TermValue;
        final String _gradValue2;
        final String _gradValue;
        final String _getCredit;
        final String _zenkiFlg;
        final String _koukiFlg;
        final String _kind;

        Subclass(
                final String year,
                final String chaircd,
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final String repSeqAll,
                final String schSeqAll,
                final String schSeqMin,
                final String credits,
                final String at1housouCountOrg,
                final String at1shussekiCount,
                final String repgou1Count,
                final String at9housouCountOrg,
                final String at9totalCount,
                final String repgou9Count,
                final String sem1IntrValue,
                final String sem1TermValue,
                final String sem2IntrValue,
                final String sem2TermValue,
                final String gradValue2,
                final String gradValue,
                final String getCredit,
                final String zenkiFlg,
                final String koukiFlg,
                final String kind
        ) {
            _year = year;
            _chaircd = chaircd;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _repSeqAll = repSeqAll;
            _schSeqAll = schSeqAll;
            _schSeqMin = schSeqMin;
            _credits = credits;
            _at1housouCountOrg = at1housouCountOrg;
            _at1shussekiCount = at1shussekiCount;
            _repgou1Count = repgou1Count;
            _at9housouCountOrg = at9housouCountOrg;
            _at9totalCount = at9totalCount;
            _repgou9Count = repgou9Count;
            _sem1IntrValue = sem1IntrValue;
            _sem1TermValue = sem1TermValue;
            _sem2IntrValue = sem2IntrValue;
            _sem2TermValue = sem2TermValue;
            _gradValue2 = gradValue2;
            _gradValue = gradValue;
            _getCredit = getCredit;
            _zenkiFlg = zenkiFlg;
            _koukiFlg = koukiFlg;
            _kind = kind;
        }
    }

    private static class Student {
        final String _schregno;
        String _inoutcd;
        String _name;
        String _baseRemark1;
        String _at93Sem1;
        String _at94Sem1;
        String _atTotalSem1;
        String _at93Sem9;
        String _at94Sem9;
        String _atTotalSem9;
        String _entDateSemester;

        final List _subclassList = new ArrayList();
        final List _attendList = new ArrayList();

        Student(
                final String schregno
        ) {
            _schregno = schregno;
        }
        
        public String getAttendNissu(final String semester) {
            return getAttendNissu(semester, "0");
        }
        
        public String getAttendNissu(final String semester, final String defStr) {
            final Set set = new HashSet();
            for (final Iterator it = _attendList.iterator(); it.hasNext();) {
                final Attend at = (Attend) it.next();
                if (!SEMEALL.equals(semester) && !semester.equals(at._semester)) { // 9学期の場合はすべてが対象
                    continue;
                }
                set.add(at._executedate);
            }
            return set.isEmpty() ? defStr : String.valueOf(set.size());
        }
    }
    
    private static class Attend {
        final String _semester;
        final String _schoolingkindcd;
        final String _namespare1;
        final String _executedate;
        final String _periodcd;
        final BigDecimal _creditTime;
        public Attend(final String semester, final String schoolingkindcd, final String namespare1, final String executedate, final String periodcd, final BigDecimal creditTime) {
            _semester = semester;
            _schoolingkindcd = schoolingkindcd;
            _namespare1 = namespare1;
            _executedate = executedate;
            _periodcd = periodcd;
            _creditTime = creditTime;
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 62700 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _rkijun;
        final String _skijun;
        final String _tkijun;
        final String _trcd1Staffname;
        final String _output;
        final Map _hreportRemarkTDat;
        final Map _semesterName;
        final String _m020Name1;
        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private String _certifSchoolRemark3;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _rkijun = request.getParameter("RKIJUN").replace('/', '-');
            _skijun = request.getParameter("SKIJUN").replace('/', '-');
            _tkijun = request.getParameter("TKIJUN").replace('/', '-');
            _output = request.getParameter("OUTPUT").replace('/', '-');
            _trcd1Staffname = getTrcd1Staffname(db2);
            _hreportRemarkTDat = getHreportRemarkTDatMap(db2);
            _semesterName = getSemesterNameMap(db2);
            _m020Name1 = getNameMstM020Name1(db2);
            setCertifSchoolName(db2);
        }
        
        private String getNameMstM020Name1(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'M020' AND NAMECD2 = '01' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (NumberUtils.isDigits(rs.getString("NAME1"))) {
                        rtn = rs.getString("NAME1");
                    }
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            //log.debug(" m020 = " + rtn);
            return rtn;
        }
        
        private String getTrcd1Staffname(DB2UDB db2) {
            String trcd1Staffname = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT STAFFNAME FROM SCHREG_REGD_HDAT, STAFF_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND GRADE || HR_CLASS = '" + _gradeHrclass + "' AND TR_CD1 = STAFFCD ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    trcd1Staffname = rs.getString("STAFFNAME");
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return trcd1Staffname;
        }

        private void setCertifSchoolName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '100' ";
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _certifSchoolSchoolName = rs.getString("SCHOOL_NAME");
                    _certifSchoolJobName = rs.getString("JOB_NAME");
                    _certifSchoolPrincipalName = rs.getString("PRINCIPAL_NAME");
                    _certifSchoolRemark3 = rs.getString("REMARK3");
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private Map getSemesterNameMap(final DB2UDB db2) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        private Map getHreportRemarkTDatMap(final DB2UDB db2) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT REMARKID, REMARK FROM HREPORTREMARK_T_DAT WHERE REMARKID IN ('7', '8', '9') ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("REMARKID"), rs.getString("REMARK"));
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof

