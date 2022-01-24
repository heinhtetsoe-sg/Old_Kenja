/*
 * $Id: f8809d3e95273cf68dbb67561fe58cca6d6eefa7 $
 *
 * 作成日: 2012/12/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;


import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 単位認定一覧表 (KNJM831, KNJM832)
 */
public class KNJM831 {

    private static final Log log = LogFactory.getLog(KNJM831.class);
    
    private static final String RISHU = "1";
    private static final String KOUNIN = "2";
    private static final String ZOUTAN = "3";
    
    private static final String ZENKI = "ZENKI";
    private static final String KOUKI = "KOUKI";
    
    private static final String INOUTCD8 = "8"; // 聴講生
    private static final String INOUTCD9 = "9"; // 併修生

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
    
    
    private static String getDispNum(final String num, final String defaultVal) {
        if (num == null || !NumberUtils.isNumber(num)) {
            return defaultVal;
        }
        final BigDecimal bd = new BigDecimal(num);
        if (bd.setScale(0, BigDecimal.ROUND_UP).equals(bd.setScale(0, BigDecimal.ROUND_DOWN))) {
            // 切り上げでも切り下げでも値が変わらない = 小数点以下が0
            return bd.setScale(0).toString();
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final String form = "KNJM832".equals(_param._prgId) ? "KNJM832.frm" : "KNJM831.frm";
        final int maxLine = 40;
        final List subclassAlllist = getSubclassList(db2);
        final List subclassPageList = getPageList(subclassAlllist, maxLine);
        final String totalPage = String.valueOf(subclassPageList.size());
        
        for (int pi = 0; pi < subclassPageList.size(); pi++) {
            
            final Subclass subclass = (Subclass) subclassPageList.get(pi);
            log.debug(" subclass = " + subclass);

            svf.VrSetForm(form, 4);
            
            svf.VrsOut("PAGE", String.valueOf(pi + 1)); // ページ
            svf.VrsOut("TOTAL_PAGE", totalPage); // 総ページ数
            svf.VrsOut("SEMESTER1", (String) _param._semesterName.get("1"));
            svf.VrsOut("SEMESTER2", (String) _param._semesterName.get("2"));
            svf.VrsOut("SEMESTER3", (String) _param._semesterName.get("9"));
            
            final String suf1 = getMS932ByteLength(subclass._subclassname) > 20 ? "3" : getMS932ByteLength(subclass._subclassname) > 14 ? "2" : "1";
            svf.VrsOut("SUBCLASS_NAME" + suf1, subclass._subclassname); // 科目名
            if ("KNJM831".equals(_param._prgId)) {
                svf.VrsOut("CREDIT1", subclass._credits); // 単位
                svf.VrsOut("MUST_REP", subclass._repSeqAll); // 提出すべきレポート数
                svf.VrsOut("MUST_INTERVIEW", subclass._schSeqMin); // 必要面接時間
                svf.VrsOut("TEACHER", null); // 科目担当
            }
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year))+ "年度"); // 年度
            
            for (final Iterator it = subclass._studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
//                if (INOUTCD8.equals(student._inoutcd)) {
//                    svf.VrsOut("ST_DIV", "聴講生"); // 生徒区分
//                }
                if (INOUTCD9.equals(student._inoutcd)) {
                    svf.VrsOut("ST_DIV", "併修生"); // 生徒区分
                }
                
                final String suf2 = getMS932ByteLength(student._name) > 20 ? "3" : getMS932ByteLength(student._name) > 14 ? "2" : "1";
                svf.VrsOut("NAME" + suf2, student._name); // 科目名
                svf.VrsOut("SCHREG_NO1", student._schregno); // 学籍番号

                if ("1".equals(student._baseRemark1)) {
                    svf.VrsOut("GRD_DIV", "卒予"); // 卒業予定
                }
                
                // 高認、増単は値が無い場合、ブランク表示しない
                final boolean isKouninZoutan = KOUNIN.equals(student._kind) || ZOUTAN.equals(student._kind);
                final boolean zenkisotsuTenTaigaku = "1".equals(student._grdDiv) && "1".equals(student._grdDateSemester) || "2".equals(student._grdDiv) || "3".equals(student._grdDiv);
                if (!isKouninZoutan) {
                    if (!KOUKI.equals(subclass._hokan)) {
                        svf.VrsOut("REP_SUC1", StringUtils.defaultString(student._repgou1Count, "0")); // レポート合格数
                        svf.VrsOut("INT_ATTEND1", getDispNum(student._at1shussekiCount, "0")); // 面接出席
                        svf.VrsOut("INT_BROAD1", getDispNum(student._at1housouCountOrg, null)); // 面接放送視聴
                        svf.VrsOut("SCORE1", student._sem1IntrValue); // 試験
                        svf.VrsOut("LEAD1", student._sem1TermValue); // 補充指導
                    }

                    if (_param.isPrintKouki() && !ZENKI.equals(subclass._hokan)) {
                        // 通年
                        svf.VrsOut("REP_SUC2", StringUtils.defaultString(student._repgou9Count, "0")); // レポート合格数
                        svf.VrsOut("INT_ATTEND2", getDispNum(student._at9shussekiCount, "0")); // 面接出席
                        svf.VrsOut("INT_BROAD2", getDispNum(student._at9housouCountOrg, null)); // 面接放送視聴

                        // 後期
                        svf.VrsOut("SCORE3", student._sem2IntrValue); // 試験
                        svf.VrsOut("LEAD3", student._sem2TermValue); // 補充指導
                    }

                    if (_param.isPrintGakunenmatsu()) {
                        // 学年末
                        svf.VrsOut("SCORE4", student._gradValue2); // 成績
                        svf.VrsOut("VALUE", student._gradValue); // 評定
                        svf.VrsOut("INTERVIEW", getDispNum(student._at9totalCount, "0")); // 面接時数
                    }
                }
                
                if (_param.isPrintGakunenmatsu()) {
                    // 学年末
                    String markFugoukaku = "否";
                    if (zenkisotsuTenTaigaku && null != student._grdDate) {
                        final Calendar grdDateCal = Calendar.getInstance();
                        grdDateCal.setTime(Date.valueOf(student._grdDate));
                        final Calendar loginDateCal = Calendar.getInstance();
                        loginDateCal.setTime(Date.valueOf(_param._loginDate));
                        if (grdDateCal.before(loginDateCal)) {
                            // 出力指定したログイン日付以前に転退学している生徒は「否」と出力せず、ブランクで出力する。
                            markFugoukaku = null; 
                        }
                    }

                    String mark = null;
                    if (null == student._getCredit || 0 == Integer.parseInt(student._getCredit)) {
                        mark = markFugoukaku;
                    } else if (1 <= Integer.parseInt(student._getCredit)) {
                        mark = "合";
                    }
                    svf.VrsOut("JUDGE", mark); // 合否
                }

                final StringBuffer remark = new StringBuffer();
                String space = "";
                if (zenkisotsuTenTaigaku && null != student._grdDivName) { // 転学 or 退学
                    remark.append(space).append((null == student._grdDate ? "" : KNJ_EditDate.h_format_JP(student._grdDate) + "付") + student._grdDivName); 
                    space = " ";
                }
                if (isKouninZoutan) {
                    String c = "";
                    if (KOUNIN.equals(student._kind)) {
                        c = "高認";
                    } else if (ZOUTAN.equals(student._kind)) {
                        c = "技能審査";
                    }
                    remark.append(space).append(c); 
                    space = " ";
                    if (_param.isPrintGakunenmatsu()) {
                        if (null == student._getCredit || 0 == Integer.parseInt(student._getCredit)) {
                        } else if (1 <= Integer.parseInt(student._getCredit)) {
                            remark.append(space).append(student._getCredit).append("単位"); 
                            space = " ";
                        }
                    }
                }
                if (getMS932ByteLength(remark.toString()) > 24) {
                    svf.VrsOut("REMARK2", remark.toString()); // 備考
                } else {
                    svf.VrsOut("REMARK", remark.toString()); // 備考
                }
                
                svf.VrEndRecord();
            }
            
            for (int j = subclass._studentList.size(); j < maxLine; j++) {
                svf.VrsOut("SCHREG_NO1", "\n"); // 備考
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }
    
    private List getPageList(final List subclassList, final int size) {
        final List pageList = new ArrayList();
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            Subclass current = (Subclass) it.next();
            final int subclassPage = current._studentList.size() / size + (current._studentList.size() % size == 0 ? 0 : 1);
            for (int i = 0; i < subclassPage; i++) {
                Subclass subclass = new Subclass(
                        current._year, current._classcd, current._schoolKind, current._curriculumCd, current._subclasscd, current._hokan, current._subclassname, current._repSeqAll, current._schSeqAll, current._schSeqMin);
                subclass._credits = current._credits;                
                final int subcCount = Math.min(current._studentList.size(), size);
                subclass._studentList.addAll(current._studentList.subList(0, subcCount));
                pageList.add(subclass);
                current._studentList = current._studentList.subList(subcCount, current._studentList.size());
            }
        }
        return pageList;
    }

    private List getSubclassList(final DB2UDB db2) {
        final List subclasslist = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
             final String sql = sql();
             log.debug(" sql = " + sql);
             ps = db2.prepareStatement(sql);
             rs = ps.executeQuery();
             while (rs.next()) {
                 
                 final String year = rs.getString("YEAR");
                 final String classcd = rs.getString("CLASSCD");
                 final String schoolKind = rs.getString("SCHOOL_KIND");
                 final String curriculumCd = rs.getString("CURRICULUM_CD");
                 final String subclasscd = rs.getString("SUBCLASSCD");
                 Subclass subclass = getSubclass(subclasslist, classcd, schoolKind, curriculumCd, subclasscd);
                 if (null == subclass) {
                     final String hokan = rs.getString("HOKAN");
                     final String subclassname = rs.getString("SUBCLASSNAME");
                     final String repSeqAll = rs.getString("REP_SEQ_ALL");
                     final String schSeqAll = rs.getString("SCH_SEQ_ALL");
                     final String schSeqMin = rs.getString("SCH_SEQ_MIN");
                     subclass = new Subclass(year, classcd, schoolKind, curriculumCd, subclasscd, hokan, subclassname, repSeqAll, schSeqAll, schSeqMin);
                     subclasslist.add(subclass);
                 }
                 if (null == subclass._credits && null != rs.getString("CREDITS")) {
                     subclass._credits = rs.getString("CREDITS");
                 }
                 
                 final String inoutcd = rs.getString("INOUTCD");
                 final String schregno = rs.getString("SCHREGNO");
                 final String name = rs.getString("NAME");
                 final String baseRemark1 = rs.getString("BASE_REMARK1");
                 final String at11Count = rs.getString("AT11_COUNT");
                 final String at13Count = rs.getString("AT13_COUNT");
                 final String at1shussekiCount = sum(new String[] {at11Count, at13Count});
                 final String at91Count = rs.getString("AT91_COUNT");
                 final String at93Count = rs.getString("AT93_COUNT");
                 final String at9shussekiCount = sum(new String[] {at91Count, at93Count});

                 String at1housouCountOrg = rs.getString("AT12_COUNT_ORG");
                 String at9housouCountOrg = rs.getString("AT92_COUNT_ORG");
                 String at1housouCount = rs.getString("AT12_COUNT");
                 String at9housouCount = rs.getString("AT92_COUNT");
                 if (NumberUtils.isNumber(subclass._schSeqMin)) {
                     // 最低出席回数 - スクーリング数（スクーリング種別2以外）の合計 = 不足のスクーリング数 (マイナスの場合は0とする)
                     //  不足のスクーリング数 >= 放送で認められる数 の場合は放送で認められる数を加算する。
                     //  不足のスクーリング数 <  放送で認められる数 の場合は不足のスクーリング数を加算する。
                     final int div6 = Integer.parseInt(subclass._schSeqMin) * Integer.parseInt(StringUtils.defaultString(_param._m020Name1, "6")) / 10;
                     final double at1shusseki = NumberUtils.isNumber(at1shussekiCount) ? Double.parseDouble(at1shussekiCount) : 0;
                     final double housouDeMitomerareruMax1 = Math.min(div6, Math.max(0.0, Integer.parseInt(subclass._schSeqMin) - at1shusseki)); // 放送で認められる最大数
                     if (NumberUtils.isNumber(at1housouCount) && Double.parseDouble(at1housouCount) > housouDeMitomerareruMax1) {
                         at1housouCount = String.valueOf(Integer.parseInt(subclass._schSeqMin) - housouDeMitomerareruMax1);
                     }
                     final double at9shusseki = NumberUtils.isNumber(at9shussekiCount) ? Double.parseDouble(at9shussekiCount) : 0;
                     final double housouDeMitomerareruMax9 = Math.min(div6, Math.max(0.0, Integer.parseInt(subclass._schSeqMin) - at9shusseki)); // 放送で認められる最大数
                     if (NumberUtils.isNumber(at9housouCount) && Double.parseDouble(at9housouCount) > housouDeMitomerareruMax9) {
                         at9housouCount = String.valueOf(housouDeMitomerareruMax9);
                     }
                 }
                 if (NumberUtils.isNumber(at1housouCountOrg) && 0.0 == Double.parseDouble(at1housouCountOrg)) at1housouCountOrg = null;
                 if (NumberUtils.isNumber(at9housouCountOrg) && 0.0 == Double.parseDouble(at9housouCountOrg)) at9housouCountOrg = null;
                 if (NumberUtils.isNumber(at1housouCount) && 0.0 == Double.parseDouble(at1housouCount)) at1housouCount = null;
                 if (NumberUtils.isNumber(at9housouCount) && 0.0 == Double.parseDouble(at9housouCount)) at9housouCount = null;

                 final String at9totalCount = sum(new String[] {at91Count, at9housouCount, at93Count});
                 final String repgou1Count = rs.getString("REPGOU1_COUNT");
                 final String repgou9Count = rs.getString("REPGOU9_COUNT");
                 final String sem1IntrValue = rs.getString("SEM1_INTR_VALUE");
                 final String sem1TermValue = rs.getString("SEM1_TERM_VALUE");
                 final String sem2IntrValue = rs.getString("SEM2_INTR_VALUE");
                 final String sem2TermValue = rs.getString("SEM2_TERM_VALUE");
                 final String gradValue2 = rs.getString("GRAD_VALUE2");
                 final String gradValue = rs.getString("GRAD_VALUE");
                 final String getCredit = rs.getString("GET_CREDIT");
                 final String grdDiv = rs.getString("GRD_DIV");
                 final String grdDate = rs.getString("GRD_DATE");
                 final String grdDivName;
                 if ("1".equals(rs.getString("GRD_DATE_SEMESTER")) && "1".equals(grdDiv)) {
                     grdDivName = StringUtils.defaultString(rs.getString("GRD_DATE_SEMESTERNAME")) + StringUtils.defaultString(rs.getString("GRD_DIV_NAME"));
                 } else {
                     grdDivName = rs.getString("GRD_DIV_NAME");
                 }
                 final String kind = rs.getString("KIND");
                 final Student student = new Student(inoutcd, schregno, name, baseRemark1, at1shussekiCount, at1housouCountOrg, repgou1Count, at9shussekiCount, at9housouCountOrg, at9totalCount, repgou9Count, sem1IntrValue, sem1TermValue, sem2IntrValue, sem2TermValue, gradValue2, gradValue, getCredit, grdDiv, grdDivName, grdDate, rs.getString("GRD_DATE_SEMESTER"), kind);
                 subclass._studentList.add(student);
             }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        return subclasslist;
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
    
    private Subclass getSubclass(final List list, final String classcd, final String schoolKind, final String curriculumCd, final String subclassCd) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (subclass._classcd.equals(classcd) && subclass._schoolKind.equals(schoolKind) && subclass._curriculumCd.equals(curriculumCd) && subclass._subclasscd.equals(subclassCd)) {
                return subclass;
            }
        }
        return null;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CHAIR AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.SEMESTER, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         CASE WHEN (T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHOOL_KIND) ");
        stb.append("                    IN (SELECT NAME1, NAME2, NAME3, VALUE(ABBV1, 'H') FROM V_NAME_MST WHERE YEAR = T1.YEAR AND NAMECD1 = 'M015') THEN '" + ZENKI + "' ");
        stb.append("              WHEN (T1.CLASSCD, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHOOL_KIND) ");
        stb.append("                    IN (SELECT NAME1, NAME2, NAME3, VALUE(ABBV1, 'H') FROM V_NAME_MST WHERE YEAR = T1.YEAR AND NAMECD1 = 'M016') THEN '" + KOUKI + "' ");
        stb.append("         END AS HOKAN, ");
        stb.append("         T2.REP_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_ALL, ");
        stb.append("         T2.SCH_SEQ_MIN, ");
        stb.append("         MAX(T6.CREDITS) AS CREDITS, ");
        stb.append("         T3.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , T5.COURSECD ");
            stb.append("       , T5.MAJORCD ");
            stb.append("       , T5.COURSECODE ");
        }
        stb.append("     FROM ");
        stb.append("         CHAIR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_CORRES_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("     INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("         AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T3.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T3.SCHREGNO ");
        stb.append("         AND T5.YEAR = T3.YEAR ");
        stb.append("         AND T5.SEMESTER = T3.SEMESTER ");
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
        stb.append("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
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
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , T5.COURSECD ");
            stb.append("       , T5.MAJORCD ");
            stb.append("       , T5.COURSECODE ");
        }

        stb.append(" ), CHAIR2 AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T1.HOKAN, ");
        stb.append("         T1.REP_SEQ_ALL, ");
        stb.append("         T1.SCH_SEQ_ALL, ");
        stb.append("         T1.SCH_SEQ_MIN, ");
        stb.append("         T1.CREDITS, ");
        stb.append("         T1.SCHREGNO ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , T1.COURSECD ");
            stb.append("       , T1.MAJORCD ");
            stb.append("       , T1.COURSECODE ");
        }
        stb.append("     FROM ");
        stb.append("         CHAIR T1 ");
        
        stb.append(" ), KOUGAI AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
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
        stb.append("                 GROUP BY ");
        stb.append("                     SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        stb.append("               ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("                   AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
        stb.append("         AND T1.KOUNIN = '1' ");
        stb.append(" UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         T1.YEAR, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
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
        stb.append("                     GROUP BY ");
        stb.append("                         SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD ");
        stb.append("                   ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("                       AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
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
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         T3.COURSECD, ");
            stb.append("         T3.MAJORCD, ");
            stb.append("         T3.COURSECODE, ");
        }
        stb.append("         COUNT(*) AS COUNT ");
        stb.append("     FROM ");
        stb.append("         MAX_REP_PRESENT T1 ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         INNER JOIN REP_STANDARDDATE_COURSE_DAT T3 ON T3.YEAR = T1.YEAR ");
        } else {
            stb.append("         INNER JOIN REP_STANDARDDATE_DAT T3 ON T3.YEAR = T1.YEAR ");
        }
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
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("       , T3.COURSECD ");
            stb.append("       , T3.MAJORCD ");
            stb.append("       , T3.COURSECODE ");
        }

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
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         AND REPGOU1.COURSECD = T1.COURSECD ");
            stb.append("         AND REPGOU1.MAJORCD = T1.MAJORCD ");
            stb.append("         AND REPGOU1.COURSECODE = T1.COURSECODE ");
        }

        stb.append("     LEFT JOIN REP_GOUKAKU_COUNT REPGOU9 ON REPGOU9.YEAR = T1.YEAR ");
        stb.append("         AND REPGOU9.CLASSCD = T1.CLASSCD ");
        stb.append("         AND REPGOU9.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND REPGOU9.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND REPGOU9.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("         AND REPGOU9.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND REPGOU9.SEMESTER = '9' ");
        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append("         AND REPGOU9.COURSECD = T1.COURSECD ");
            stb.append("         AND REPGOU9.MAJORCD = T1.MAJORCD ");
            stb.append("         AND REPGOU9.COURSECODE = T1.COURSECODE ");
        }

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
        stb.append("     CAST(NULL AS VARCHAR(1)) AS HOKAN, ");
        stb.append("     CAST(NULL AS SMALLINT) AS REP_SEQ_ALL, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SCH_SEQ_ALL, ");
        stb.append("     CAST(NULL AS SMALLINT) AS SCH_SEQ_MIN, ");
        stb.append("     CAST(NULL AS SMALLINT) AS CREDITS, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     CAST(NULL AS VARCHAR(1)) AS COURSECD, ");
        stb.append("     CAST(NULL AS VARCHAR(3)) AS MAJORCD, ");
        stb.append("     CAST(NULL AS VARCHAR(4)) AS COURSECODE, ");

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
        stb.append("     T4.INOUTCD, ");
        stb.append("     T4.NAME, ");
        stb.append("     T7.BASE_REMARK1, ");
        stb.append("     T8.GRD_DIV, ");
        stb.append("     NMA003.NAME1 AS GRD_DIV_NAME, ");
        stb.append("     T8.GRD_DATE, ");
        stb.append("     T9.SEMESTER AS GRD_DATE_SEMESTER, ");
        stb.append("     T9.SEMESTERNAME AS GRD_DATE_SEMESTERNAME, ");
        stb.append("     SUBM.SUBCLASSNAME ");
        stb.append(" FROM SUBCLASS T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN (SELECT T5.SCHREGNO, T5.YEAR, MAX(T5.GRADE) AS GRADE ");
        stb.append("                FROM SCHREG_REGD_DAT T5 ");
        stb.append("                GROUP BY T5.SCHREGNO, T5.YEAR) T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T5.YEAR = T1.YEAR ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T6 ON T6.YEAR = T5.YEAR ");
        stb.append("         AND T6.GRADE = T5.GRADE ");
        stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST T7 ON T7.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T7.YEAR = T1.YEAR ");
        stb.append("         AND T7.BASE_SEQ = '001' ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T8 ON T8.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T8.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T1.CLASSCD ");
        stb.append("         AND SUBM.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND SUBM.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND SUBM.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN SEMESTER_MST T9 ON T9.YEAR = T1.YEAR ");
        stb.append("         AND T9.SEMESTER <> '9' ");
        stb.append("         AND T8.GRD_DATE BETWEEN T9.SDATE AND T9.EDATE ");
        stb.append("     LEFT JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' ");
        stb.append("         AND NMA003.NAMECD2 = T8.GRD_DIV ");
        stb.append(" WHERE ");
        stb.append("     VALUE(T4.INOUTCD, '') <> '" + INOUTCD8 + "' "); // 聴講生は対象外
        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     CASE WHEN T4.INOUTCD = '" + INOUTCD9 + "' THEN 2 ELSE 1 END, ");
        stb.append("     SUBSTR(T1.SCHREGNO, 1, 4) DESC, SUBSTR(T1.SCHREGNO, 5, 4) ");
        return stb.toString();
    }
    
    private static class Subclass {
        final String _year;
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _hokan;
        final String _subclassname;
        final String _repSeqAll;
        final String _schSeqAll;
        final String _schSeqMin;
        String _credits;
        List _studentList = new ArrayList();

        Subclass(
                final String year,
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String hokan,
                final String subclassname,
                final String repSeqAll,
                final String schSeqAll,
                final String schSeqMin
        ) {
            _year = year;
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _hokan = hokan;
            _subclassname = subclassname;
            _repSeqAll = repSeqAll;
            _schSeqAll = schSeqAll;
            _schSeqMin = schSeqMin;
        }
        
        public String toString() {
            return "Subclass(" + _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd + " : " + _subclassname + ", hokan = " + _hokan + ")";
        }
    }
    
    private static class Student {
        final String _inoutcd;
        final String _schregno;
        final String _name;
        final String _baseRemark1;
        final String _at1shussekiCount;
        final String _at1housouCountOrg;
        final String _repgou1Count;
        final String _at9shussekiCount;
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
        final String _grdDiv;
        final String _grdDivName;
        final String _grdDate;
        final String _grdDateSemester;
        final String _kind;

        Student(
                final String inoutcd,
                final String schregno,
                final String name,
                final String baseRemark1,
                final String at1shussekiCount,
                final String at1housouCountOrg,
                final String repgou1Count,
                final String at9shussekiCount,
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
                final String grdDiv,
                final String grdDivName,
                final String grdDate,
                final String grdDateSemester,
                final String kind
        ) {
            _inoutcd = inoutcd;
            _schregno = schregno;
            _name = name;
            _baseRemark1 = baseRemark1;
            _at1shussekiCount = at1shussekiCount;
            _at1housouCountOrg = at1housouCountOrg;
            _repgou1Count = repgou1Count;
            _at9shussekiCount = at9shussekiCount;
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
            _grdDiv = grdDiv;
            _grdDivName = grdDivName;
            _grdDate = grdDate;
            _grdDateSemester = grdDateSemester;
            _kind = kind;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74303 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _prgId;
        final String _loginDate;
        final Map _semesterName;
        final String _m020Name1;
        final String[] _categorySelected; // 科目コード
        private final String _useRepStandarddateCourseDat;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _prgId = request.getParameter("PRGID");
            _loginDate = request.getParameter("LOGIN_DATE");
            _categorySelected = request.getParameterValues("category_selected");
            _useRepStandarddateCourseDat = request.getParameter("useRepStandarddateCourseDat");
            _semesterName = getSemesterNameMap(db2);
            _m020Name1 = getNameMstM020Name1(db2);
        }
        
        private boolean isPrintKouki() {
            return "2".equals(_semester) || "9".equals(_semester);
        }
        
        private boolean isPrintGakunenmatsu() {
            return "9".equals(_semester);
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

        private Map getSemesterNameMap(final DB2UDB db2) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ";
                log.debug(" sql =" + sql);
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
    }
}

// eof

