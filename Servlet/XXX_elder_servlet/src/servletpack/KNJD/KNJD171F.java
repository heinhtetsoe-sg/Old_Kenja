/*
 * $Id: 864c58bece693465cf9c9a76601e7f399b5be374 $
 *
 * 作成日: 2011/01/12
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 学校教育システム 賢者 [成績管理] 中学通知票
 */

public class KNJD171F {

    private static final Log log = LogFactory.getLog(KNJD171F.class);

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

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List studentList = Student.getStudentList(db2, _param);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            log.debug(" student = " + student._schregno);
            printStudent(db2, svf, student);
            _hasData = true;
        }
    }

    private void VrsOutnRenban(final Vrw32alp svf, final String field, final List token) {
        if (null != token) {
            for (int i = 0 ; i < token.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) token.get(i));
            }
        }
    }

    /**
     * "w * h"サイズタイプのパラメータの[w, h]を整数値配列で返す
     * @param parameter サイズタイプのパラメータ文字列
     * @param defwh デフォルトの[w, h]
     * @return "w * h"サイズタイプのパラメータの[w, h]の整数値配列
     */
    private static int[] getParamSize(final String parameter, final int[] defwh) {
        final int[] rtn = {defwh[0], defwh[1]};
        String[] nums = StringUtils.split(parameter, " * ");
        if (!StringUtils.isBlank(parameter)) {
            try {
                rtn[0] = Integer.valueOf(nums[0]).intValue();
                rtn[1] = Integer.valueOf(nums[1]).intValue();
            } catch (Exception e) {
                log.error("Exception! param = " + parameter, e);
            }
        }
        return rtn;
    }

    private void printStudent(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form;
        if ("2".equals(_param._recordDiv)) {
            form = "KNJD171F_2.frm";
        } else {
            form = "KNJD171F_1.frm";
        }
        svf.VrSetForm(form, 1);

        printHeader(db2, svf, student);
        printSubclass(svf, student);
        printShukketsu(svf, student);
        printShoken(svf, student);

        svf.VrEndPage();
    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        svf.VrsOut("NENDO", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._ctrlYear)) + "年度"); // 年度
//      svf.VrsOut("CORP_NAME", StringUtils.defaultString(_param._certifSchoolDatRemark1) + "　" + StringUtils.defaultString(_param._certifSchoolDatRemark2));
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolDatSchoolName); // 学校名
//      svf.VrsOut("LOGO", _param.getImagePath());
        svf.VrsOut("HR_NAME", student._hrName); // クラス名
//        svf.VrsOut("ATTENDNO", student.getAttendnoStr());
        final int namelen = KNJ_EditEdit.getMS932ByteLength(student._name);
        svf.VrsOut("NAME" + (namelen > 20 ? "3" : namelen > 14 ? "2" : "1"), student._name);
//      svf.VrsOut("STAFF_NAME1" + (getMS932ByteCount(trimLeft(_param._certifSchoolDatPrincipalName)) > 20 ? "_2" : ""), trimLeft(_param._certifSchoolDatPrincipalName));
//      svf.VrsOut("STAFF_NAME2" + (getMS932ByteCount(_param._tr1Name) > 20 ? "_2" : ""), _param._tr1Name);
//      svf.VrsOut("STAFF_NAME3" + (getMS932ByteCount(_param._subtr1Name) > 20 ? "_2" : ""), _param._subtr1Name);
        final String staffname = StringUtils.defaultString(_param._tr1Name) + "　 " + StringUtils.defaultString(_param._subtr1Name);
        svf.VrsOut("TEACHER_NAME" + (KNJ_EditEdit.getMS932ByteLength(staffname) > 30 ? "2" : "1"), staffname); // 担任名

        final int maxLine = 3;
        for (int j = 0; j < maxLine; j++) {
            final int semesline = j + 1;
            final String semester = String.valueOf(semesline);
            final String semestername = (String) _param._semesternameMap.get(semester);
            svf.VrsOutn("SEMESTER1", semesline, semestername); // 学期
            svf.VrsOutn("SEMESTER2", semesline, semestername); // 学期
            svf.VrsOut("SEMESTER3_" + String.valueOf(semesline), semestername); // 学期
        }
    }

    private void printSubclass(final Vrw32alp svf, final Student student) {
        final int classMax;
        if ("2".equals(_param._recordDiv)) {
            classMax = 7;
        } else {
            classMax = 9;
        }
        final int maxSemes = 3;

        if ("2".equals(_param._recordDiv)) {
            final Map semesterTextMap = new HashMap();
            for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();

                if (_param._d059Name1List.contains(subclass._subclasscd)) {
                    // 科目の表には表示せずに、文言に表示する
                	it.remove();

                    // 文言に表示
                    for (int j = 1; j <= maxSemes; j++) {
                        final String semester = String.valueOf(j);

                        final String score = (String) subclass._semesterScoreMap.get(String.valueOf(j));
                        if (StringUtils.isBlank(score)) {
                            continue;
                        }

                        if (null == semesterTextMap.get(semester)) {
                            semesterTextMap.put(semester, new StringBuffer());
                        }

                        final StringBuffer stb = (StringBuffer) semesterTextMap.get(semester);
                        if (!StringUtils.isEmpty(stb.toString())) {
                            stb.append("　");
                        }
                        stb.append(StringUtils.defaultString(subclass._subclassname)).append(score);
                    }
                }
            }

            // 文言に表示
            for (int j = 1; j <= maxSemes; j++) {
                final String semester = String.valueOf(j);

                if (null == semesterTextMap.get(semester)) {
                    continue;
                }

                final StringBuffer stb = (StringBuffer) semesterTextMap.get(semester);

                final String[] rtn = KNJ_EditEdit.get_token(stb.toString(), 16, 2);

                if (null != rtn) {
                    for (int i = 0; i < Math.min(rtn.length, 2); i++) {
                        svf.VrsOutn("VAL8_" + String.valueOf(i + 1), j, rtn[i]); // 評価
                    }
                }
            }
        }

        int ci = 0;
        for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
            final String cn = String.valueOf(ci + 1);
            final Subclass subclass = (Subclass) it.next();

            log.debug(" subclass " + subclass._subclasscd + " = " + subclass._subclassname);

            if (KNJ_EditEdit.getMS932ByteLength(subclass._subclassname) > 4) {
                svf.VrsOut("CLASS" + cn + "_2", subclass._subclassname); // 教科名
            } else {
                svf.VrsOut("CLASS" + cn, subclass._subclassname); // 教科名
            }

            for (int j = 1; j <= maxSemes; j++) {
                final String semester = String.valueOf(j);
                svf.VrsOutn("VAL" + cn, j, (String) subclass._semesterScoreMap.get(String.valueOf(j))); // 評価
                if ("1".equals(_param._recordDiv)) {
                    svf.VrsOutn("MARK" + cn, j, (String) subclass._semesterSlumpMarkMap.get(String.valueOf(j))); // マーク
                }
                if ("2".equals(_param._recordDiv)) {
                    final String chairname1 = null == subclass._semesterChairnameMap.get(semester) || subclass._semesterChairnameMap.get(semester).toString().length() == 0 ? null : subclass._semesterChairnameMap.get(semester).toString().substring(0, 1);
                    svf.VrsOutn("CHAIR_NAME" + cn, j, chairname1); // 講座名
                }
            }
            ci += 1;
            if (ci >= classMax) {
                break;
            }
        }
    }

    private void printShoken(final Vrw32alp svf, final Student student) {

        // 生活記録評価
        for (final Iterator it = student._behaviorSemesDatList.iterator(); it.hasNext();) {
            final BehaviorSemesDat bsd = (BehaviorSemesDat) it.next();
            if (NumberUtils.isDigits(bsd._code)) {
                final String code = String.valueOf(Integer.parseInt(bsd._code));
                svf.VrsOut("LIFE_NAME" + code + "_" + (KNJ_EditEdit.getMS932ByteLength(bsd._codename) > 8 ? "2" : "1"), bsd._codename);

                if (NumberUtils.isDigits(bsd._semester)) {
                    final int isemester = Integer.parseInt(bsd._semester);

                    if ("1".equals(_param._knjdBehaviorsd_UseText)) {
                        svf.VrsOutn("LIFE_VAL" + code, isemester, bsd._namespare1);
                    } else {
                        if (null != bsd._record) {
                            svf.VrsOutn("LIFE_VAL" + code, isemester, "○");
                        }
                    }
                }
            }
        }

        if (_param._maxSemester.equals(_param._semester)) {
            final StringBuffer act = new StringBuffer();
            for (final Iterator it = student._clubCommitteeSikakuTextList.iterator(); it.hasNext();) {
                final String text = (String) it.next();
                if (StringUtils.isEmpty(text)) {
                    continue;
                }
                if (!StringUtils.isEmpty(act.toString())) {
                    act.append("\n");
                }
                act.append(text);
            }
            final String[] rtn = KNJ_EditEdit.get_token(act.toString(), 20, 9);
            if (null != rtn) {
                for (int j = 0; j < rtn.length; j++) {
                    svf.VrsOutn("FIELD2", j + 1, rtn[j]); //
                }
            }
        }
        // 通信欄
        final int[] commuMojisuGyo = getParamSize(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J, new int[] {43, 3});
        for (final Iterator it = student._hReportRemarkDatMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final String semester = (String) e.getKey();
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) e.getValue();

            final String[] token = KNJ_EditEdit.get_token(hReportRemarkDat._communication, commuMojisuGyo[0] * 2, commuMojisuGyo[1]);
            if (null != token) {
                VrsOutnRenban(svf, "COMM" + semester, Arrays.asList(token));
            }
        }
    }

    private void printShukketsu(final Vrw32alp svf, final Student student) {
        final String[] months = new String[] {"04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03"};
        final int ctrlYear = Integer.parseInt(_param._ctrlYear);

        for (int i = 0; i < months.length; i++) {
            final AttendSemesDat attSemes = (AttendSemesDat) student._attendSemesMap.get(months[i]);
            final int j = i + 1;

            if (null == attSemes) {

                final String month = months[i];
                final String year = String.valueOf(ctrlYear + (Integer.parseInt(month) < 4 ? 1 : 0));
                final String semesDate = year + "-" + month + "-" + _param._df02.format(Integer.parseInt("01"));
                if (semesDate.compareTo(_param._date) > 0) {
                    continue;
                }
                svf.VrsOutn("LESSON", j, "0");
                svf.VrsOutn("ATTEND", j, "0");
                svf.VrsOutn("ABSENCE", j, "0");
                svf.VrsOutn("LATE", j, "0");
                svf.VrsOutn("EARLY", j, "0");
                svf.VrsOutn("SUSPEND", j, "0");

                continue;
            }
            svf.VrsOutn("LESSON", j, attendVal(attSemes._lesson));
            svf.VrsOutn("ATTEND", j, attendVal(attSemes._present));
            svf.VrsOutn("ABSENCE", j, attendVal(attSemes._sick));
            svf.VrsOutn("LATE", j, attendVal(attSemes._late));
            svf.VrsOutn("EARLY", j, attendVal(attSemes._early));
            svf.VrsOutn("SUSPEND", j, attendVal(attSemes._suspend + attSemes._mourning + attSemes._virus + attSemes._koudome));
        }
    }

    private static String attendVal(int n) {
        return String.valueOf(n);
    }

    private static class Student {
        String _schregno;
        String _name;
        String _hrName;
        String _attendno;
        Map _attendSemesMap = Collections.EMPTY_MAP; // 出欠の記録
        List _behaviorSemesDatList = Collections.EMPTY_LIST; // 学校生活のようす
        Map _hReportRemarkDatMap = Collections.EMPTY_MAP; // 所見
        List _subclassList = Collections.EMPTY_LIST;
        List _clubCommitteeSikakuTextList = Collections.EMPTY_LIST;

        private String getAttendnoStr() {
            return ""; // null == _attendno ? "" : (NumberUtils.isDigits(_attendno)) ? String.valueOf(Integer.parseInt(_attendno)) : _attendno;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student();
                    student._schregno = rs.getString("SCHREGNO");
                    student._name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    student._hrName = "1".equals(param._printRegd) ? rs.getString("REGDH_HR_NAME") : rs.getString("HR_NAME");
                    student._attendno = rs.getString("ATTENDNO");

                    studentList.add(student);
                }

            } catch (Exception e) {
                log.error("exception!!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            AttendSemesDat.setAttendSemesDatMap(db2, param, studentList);
            BehaviorSemesDat.setBehaviourSemesDatList(db2, param, studentList);
            HReportRemarkDat.setHReportRemarkDat(db2, param, studentList);
            Subclass.setSubclassList(db2, param, studentList);
            setClubCommitteeSikakuList(db2, param, studentList);
            return studentList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, REGDH.HR_NAME AS REGDH_HR_NAME ");
            stb.append("    FROM    SCHREG_REGD_DAT T1 ");
            stb.append("    INNER JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("    LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ");
            stb.append("        AND T1.SEMESTER = REGD.SEMESTER ");
            stb.append("        AND T1.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("        AND REGD.SEMESTER = REGDH.SEMESTER ");
            stb.append("        AND REGD.GRADE = REGDH.GRADE ");
            stb.append("        AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("    WHERE   T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("        AND T1.SEMESTER = '"+ param._semester +"' ");
            stb.append("        AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("        AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
            //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
            stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
            //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
            stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                           AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append("    ) ");
            //メイン表
            stb.append("SELECT  T1.SCHREGNO, ");
            stb.append("        T1.REGDH_HR_NAME, ");
            stb.append("        T7.HR_NAME, ");
            stb.append("        T1.ATTENDNO, ");
            stb.append("        T5.NAME, ");
            stb.append("        T5.REAL_NAME, ");
            stb.append("        CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append("        LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._ctrlYear + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }

        private static void setClubCommitteeSikakuList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     '1_CLUB' AS DIV, ");
                stb.append("     '9' AS SEMESTER, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS SEMESTERNAME, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.CLUBCD AS ITEMCD, ");
                stb.append("     T2.CLUBNAME AS ITEMNAME, ");
                stb.append("     T3.NAME1 AS ITEMNAME2, ");
                stb.append("     ROW_NUMBER() OVER(ORDER BY T1.SDATE, T1.CLUBCD) AS ORDER ");
                stb.append(" FROM SCHREG_CLUB_HIST_DAT T1 ");
                stb.append(" INNER JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
                stb.append(" LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'J001' AND T3.NAMECD2 = T1.EXECUTIVECD ");
                stb.append(" WHERE ");
                stb.append("    '" + param._ctrlYear + "' BETWEEN FISCALYEAR(T1.SDATE) AND FISCALYEAR(VALUE(T1.EDATE, '9999-12-31')) ");
                stb.append("     AND T1.SCHREGNO = ? ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     '2_COMMITTEE' AS DIV, ");
                stb.append("     T3.SEMESTER, ");
                stb.append("     T3.SEMESTERNAME, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.COMMITTEE_FLG || '-' || T1.COMMITTEECD AS ITEMCD, ");
                stb.append("     T2.COMMITTEENAME AS ITEMNAME, ");
                stb.append("     T4.NAME1 AS ITEMNAME2, ");
                stb.append("     ROW_NUMBER() OVER(ORDER BY SEQ) AS ORDER ");
                stb.append(" FROM SCHREG_COMMITTEE_HIST_DAT T1 ");
                stb.append(" INNER JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
                stb.append("     AND T2.COMMITTEECD = T1.COMMITTEECD ");
                stb.append(" INNER JOIN V_SEMESTER_GRADE_MST T3 ON T3.YEAR = T1.YEAR ");
                stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
                stb.append("     AND T3.GRADE = '" + param._grade + "' ");
                stb.append(" LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'J002' AND T4.NAMECD2 = T1.EXECUTIVECD ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + param._ctrlYear + "' ");
                stb.append("    AND T1.SCHREGNO = ? ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("   '3_SIKAKU' AS DIV, ");
                stb.append("   '9' AS SEMESTER, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS SEMESTERNAME, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T2.QUALIFIED_CD AS ITEMCD, ");
                stb.append("   T1.CONTENTS AS ITEMNAME, ");
                stb.append("   T3.NAME1 AS ITEMNAME2, ");
                stb.append("   ROW_NUMBER() OVER(ORDER BY SEQ) AS ORDER ");
                stb.append(" FROM SCHREG_QUALIFIED_HOBBY_DAT T1 ");
                stb.append(" LEFT JOIN QUALIFIED_MST T2 ON T2.QUALIFIED_CD = T1.QUALIFIED_CD ");
                stb.append(" LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'H312' ");
                stb.append("     AND T3.NAMECD2 = T1.RANK ");
                stb.append(" WHERE ");
                stb.append("    T1.YEAR = '" + param._ctrlYear + "' ");
                stb.append("    AND T1.SCHREGNO = ? ");
                stb.append(" ORDER BY ");
                stb.append("     DIV, ORDER ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._clubCommitteeSikakuTextList = new ArrayList();

                    ps.setString(1, student._schregno);
                    ps.setString(2, student._schregno);
                    ps.setString(3, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String div = rs.getString("DIV");
                        final String itemname = rs.getString("ITEMNAME");
                        final String itemname2 = rs.getString("ITEMNAME2");

                        String text = null;
                        if ("3_SIKAKU".equals(div)) {
                            text = StringUtils.defaultString(itemname) + (StringUtils.isEmpty(itemname2) ? "" : "(" + StringUtils.defaultString(itemname2) + ")");
                        } else {
                            text = StringUtils.defaultString(itemname) + (StringUtils.isEmpty(itemname2) ? "" : " " + StringUtils.defaultString(itemname2));
                        }
                        if (null != text) {
                            student._clubCommitteeSikakuTextList.add(text);
                        }
                    }

                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _month;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _abroad;
        int _offdays;
//        int _kekkaJisu;
        int _virus;
        int _koudome;

        private AttendSemesDat(
                final String month
        ) {
            _month = month;
        }

        private void add(
                final AttendSemesDat o
        ) {
            _lesson += o._lesson;
            _suspend += o._suspend;
            _mourning += o._mourning;
            _mlesson += o._mlesson;
            _sick += o._sick;
            _absent += o._absent;
            _present += o._present;
            _late += o._late;
            _early += o._early;
            _abroad += o._abroad;
            _offdays += o._offdays;
            _virus += o._virus;
            _koudome += o._koudome;
        }

        private static void setAttendSemesDatMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT * ");
                sql.append(" FROM ATTEND_SEMES_DAT ");
                sql.append(" WHERE YEAR = '" + param._ctrlYear + "' ");
                sql.append("   AND SEMESTER <= '" + param._semester + "' ");
                sql.append("   AND SCHREGNO = ? ");
                sql.append(" ORDER BY SEMESTER,  INT(MONTH) + CASE WHEN MONTH < '04' THEN 12 ELSE 0 END ");

                ps = db2.prepareStatement(sql.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._attendSemesMap = new HashMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        if (!NumberUtils.isDigits(rs.getString("APPOINTED_DAY"))) {
                            log.warn("ATTEND_SEMES_DAT.APPOINTED_DAY = " + rs.getString("APPOINTED_DAY") + ", YEAR = " + rs.getString("YEAR") + ", SEMESTER = " + rs.getString("SEMESTER") + ", MONTH = " + rs.getString("MONTH") + ", SCHREGNO = " + rs.getString("SCHREGNO"));
                            continue;
                        }

                        final String month = rs.getString("MONTH");
                        final String year = String.valueOf(rs.getInt("YEAR") + (Integer.parseInt(month) < 4 ? 1 : 0));
                        final String semesDate = year + "-" + month + "-" + param._df02.format(Integer.parseInt(rs.getString("APPOINTED_DAY")));
                        if (semesDate.compareTo(param._date) > 0) {
                            break;
                        }

                        final int lesson0 = rs.getInt("LESSON");
                        final int abroad = rs.getInt("ABROAD");
                        final int offdays = rs.getInt("OFFDAYS");
                        final int lesson = lesson0 - abroad - offdays + ("1".equals(param._knjSchoolMst._semOffDays) ? offdays : 0);
                        final int suspend = rs.getInt("SUSPEND");
                        final int mourning = rs.getInt("MOURNING");
                        final int sick = rs.getInt("SICK") + rs.getInt("NOTICE") + rs.getInt("NONOTICE");
                        final int virus = "true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0;
                        final int koudome = "true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0;
                        final int mlesson = lesson - suspend - virus - koudome - mourning;
                        final int present = mlesson - sick;

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(month);
                        attendSemesDat._lesson = lesson;
                        attendSemesDat._abroad = abroad;
                        attendSemesDat._offdays = offdays;
                        attendSemesDat._suspend = suspend;
                        attendSemesDat._mourning = mourning;
                        attendSemesDat._sick = sick;
                        attendSemesDat._absent = rs.getInt("ABSENT");
                        attendSemesDat._present = present;
                        attendSemesDat._late = rs.getInt("LATE");
                        attendSemesDat._early = rs.getInt("EARLY");
                        attendSemesDat._virus = virus;
                        attendSemesDat._koudome = koudome;

                        if (null != student._attendSemesMap.get(month)) {
                            final AttendSemesDat before = (AttendSemesDat) student._attendSemesMap.get(month);
                            before.add(attendSemesDat);
                        } else {
                            student._attendSemesMap.put(month, attendSemesDat);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }

    /**
     * 学校生活のようす
     */
    private static class BehaviorSemesDat {

        final String _semester;
        final String _code;
        final String _codename;
        final String _record;
        final String _viewname;
        final String _namespare1;

        private BehaviorSemesDat(
                final String semester,
                final String code,
                final String codename,
                final String record,
                final String viewname,
                final String namespare1) {
            _semester = semester;
            _code = code;
            _codename = codename;
            _record = record;
            _viewname = viewname;
            _namespare1 = namespare1;
        }

        private static void setBehaviourSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getBehaviorSemesDatSql(param);
                //log.debug(" behavior sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._behaviorSemesDatList = new ArrayList();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String code = rs.getString("CODE");
                        final String codename = rs.getString("CODENAME");
                        final String record = rs.getString("RECORD");
                        final String viewname = rs.getString("VIEWNAME");
                        final String namespare1 = rs.getString("NAMESPARE1");
                        final BehaviorSemesDat behaviorSemesDat = new BehaviorSemesDat(semester, code, codename, record, viewname, namespare1);

                        student._behaviorSemesDatList.add(behaviorSemesDat);
                    }

                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String getBehaviorSemesDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CODE ");
            stb.append("    ,T1.CODENAME ");
            stb.append("    ,T1.VIEWNAME ");
            stb.append("    ,L1.SEMESTER ");
            stb.append("    ,L1.RECORD ");
            stb.append("    ,NMD036.NAMESPARE1 ");
            stb.append(" FROM ");
            stb.append("     BEHAVIOR_SEMES_MST T1 ");
            stb.append("     LEFT JOIN BEHAVIOR_SEMES_DAT L1 ON L1.YEAR = T1.YEAR ");
            stb.append("         AND L1.SEMESTER <= '" + param._semester + "' ");
            stb.append("         AND L1.SCHREGNO = ? ");
            stb.append("         AND L1.CODE = T1.CODE ");
            stb.append("     LEFT JOIN V_NAME_MST NMD036 ON NMD036.YEAR = T1.YEAR ");
            stb.append("         AND NMD036.NAMECD1 = 'D036' ");
            stb.append("         AND NMD036.NAMECD2 = L1.RECORD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.GRADE = '" + param._gradeCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CODE ");
            return stb.toString();
        }
    }

    /**
     * 通知表所見
     */
    private static class HReportRemarkDat {
        final String _semester;
        final String _totalstudytime;     // 総合的な学習の時間
        final String _specialactremark;   // 特別活動の記録・その他
        final String _communication;      // 担任からの所見
        final String _remark1;            // 特別活動の記録・学級活動
        final String _remark2;            // 特別活動の記録・生徒会活動
        final String _remark3;            // 特別活動の記録・学校行事
        final String _attendrecRemark;    // 出欠備考

        private HReportRemarkDat(
                final String semester,
                final String totalstudytime,
                final String specialactremark,
                final String communication,
                final String remark1,
                final String remark2,
                final String remark3,
                final String attendrecRemark) {
            _semester = semester;
            _totalstudytime = totalstudytime;
            _specialactremark = specialactremark;
            _communication = communication;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _attendrecRemark = attendrecRemark;
        }

        private static void setHReportRemarkDat(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._hReportRemarkDatMap = new HashMap();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String totalstudytime = rs.getString("TOTALSTUDYTIME");
                        final String specialactremark = rs.getString("SPECIALACTREMARK");
                        final String communication = rs.getString("COMMUNICATION");
                        final String remark1 = rs.getString("REMARK1");
                        final String remark2 = rs.getString("REMARK2");
                        final String remark3 = rs.getString("REMARK3");
                        final String attendrecRemark = rs.getString("ATTENDREC_REMARK");

                        final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudytime, specialactremark, communication,
                                remark1, remark2, remark3, attendrecRemark);
                        student._hReportRemarkDatMap.put(semester, hReportRemarkDat);

                    }

                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static String getHReportRemarkSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }
    }

    private static class Subclass {
        final String _subclasscd;
        final String _subclassname;
        final Map _semesterChaircdMap = new HashMap();
        final Map _semesterChairnameMap = new HashMap();
        final Map _semesterScoreMap = new HashMap();
        final Map _semesterSlumpMarkMap = new HashMap();

        Subclass(
            final String subclasscd,
            final String subclassname
        ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }

        public static void setSubclassList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._subclassList = new ArrayList();

                    ps.setString(1, student._schregno);
                    ps.setString(2, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String chaircd = rs.getString("CHAIRCD");
                        final String chairname = rs.getString("CHAIRNAME");
                        final String score = rs.getString("SCORE");
                        final String slumpMark = rs.getString("SLUMP_MARK");

                        Subclass subclass = null;
                        for (final Iterator sit = student._subclassList.iterator(); sit.hasNext();) {
                            final Subclass subclass1 = (Subclass) sit.next();
                            if (subclass1._subclasscd.equals(subclasscd)) {
                                subclass = subclass1;
                            }
                        }

                        if (null == subclass) {
                            subclass = new Subclass(subclasscd, subclassname);
                            student._subclassList.add(subclass);
                        }
                        subclass._semesterChaircdMap.put(semester, chaircd);
                        subclass._semesterChairnameMap.put(semester, chairname);
                        subclass._semesterScoreMap.put(semester, score);
                        subclass._semesterSlumpMarkMap.put(semester, slumpMark);
                    }
                }

                DbUtils.closeQuietly(rs);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CHAIRS AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T2.CHAIRNAME, ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM CHAIR_STD_DAT T1 ");
            stb.append(" INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append("     AND T2.CLASSCD <= '90' ");
            stb.append(" ), MAIN AS ( ");
            stb.append("     SELECT * ");
            stb.append("     FROM CHAIRS T1 ");
            stb.append("     WHERE (SEMESTER, CHAIRCD, SUBCLASSCD, SCHREGNO) IN ( ");
            stb.append("         SELECT T1.SEMESTER, MIN(T1.CHAIRCD), T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("         FROM CHAIRS T1 ");
            stb.append("         GROUP BY T1.SEMESTER, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     ) ");

            //成績不振科目データの表
            stb.append(" ) , RECORD_SLUMP AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            stb.append("            W3.SEMESTER, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN T4.NAME1 END AS SLUMP_MARK ");
            stb.append("    FROM    RECORD_SLUMP_SDIV_DAT W3 ");
            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON W1.YEAR = W3.YEAR ");
            stb.append("            AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("            AND W3.TESTKINDCD = W1.TESTKINDCD ");
            stb.append("            AND W3.TESTITEMCD = W1.TESTITEMCD ");
            stb.append("            AND W3.SCORE_DIV = W1.SCORE_DIV ");
            stb.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'D054' AND T4.NAMECD2 = W3.MARK ");
            stb.append("    WHERE   W3.YEAR = '" + param._ctrlYear + "' AND ");
            stb.append("            W3.TESTKINDCD = '99' AND ");
            stb.append("            W3.TESTITEMCD = '00' AND ");
            stb.append("            EXISTS(SELECT 'X' FROM CHAIRS W1 ");
            stb.append("                   WHERE (W3.SEMESTER = '9' OR W3.SEMESTER = W1.SEMESTER) AND W3.SCHREGNO = W1.SCHREGNO AND W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD = W1.SUBCLASSCD) ");

            stb.append(" ), MAIN_RECORD_RANK AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.SEMESTER, ");
            stb.append("         T1.SUBCLASSCD ");
            stb.append("     FROM MAIN T1 ");
            stb.append("     UNION ");
            stb.append("     SELECT ");
            stb.append("         TRNK.SCHREGNO, ");
            stb.append("         TRNK.SEMESTER, ");
            stb.append("         TRNK.CLASSCD || '-' || TRNK.SCHOOL_KIND || '-' || TRNK.CURRICULUM_CD || '-' || TRNK.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     FROM RECORD_RANK_SDIV_DAT TRNK ");
            stb.append("     WHERE TRNK.YEAR = '" + param._ctrlYear + "' ");
            stb.append("       AND TRNK.SEMESTER <= '" + param._scoreSemester + "' ");
            stb.append("       AND TRNK.TESTKINDCD = '99' ");
            stb.append("       AND TRNK.TESTITEMCD = '00' ");
            stb.append("       AND TRNK.SCHREGNO = ? ");
            stb.append("       AND TRNK.SCORE IS NOT NULL ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T0.SEMESTER, ");
            stb.append("     VALUE(T3.SHOWORDER3, 99) AS SHOWORDER3, ");
            stb.append("     T0.SUBCLASSCD, ");
            stb.append("     VALUE(T3.SUBCLASSORDERNAME2, T3.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T1.CHAIRNAME, ");
            stb.append("     TRNK.SCORE, ");
            stb.append("     TSLUMP.SLUMP_MARK ");
            stb.append(" FROM MAIN_RECORD_RANK T0 ");
            stb.append(" INNER JOIN SUBCLASS_MST T3 ON T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append(" LEFT JOIN MAIN T1 ON T1.SEMESTER = T0.SEMESTER AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT TRNK ON TRNK.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND TRNK.SEMESTER = T0.SEMESTER ");
            stb.append("     AND TRNK.TESTKINDCD = '99' ");
            stb.append("     AND TRNK.TESTITEMCD = '00' ");
            stb.append("     AND (TRNK.SEMESTER = '9' AND TRNK.SCORE_DIV = '09' OR TRNK.SEMESTER <> '9' AND TRNK.SCORE_DIV = '08') ");
            stb.append("     AND TRNK.CLASSCD || '-' || TRNK.SCHOOL_KIND || '-' || TRNK.CURRICULUM_CD || '-' || TRNK.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("     AND TRNK.SCHREGNO = T0.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_SLUMP TSLUMP ON TSLUMP.SEMESTER = T0.SEMESTER ");
            stb.append("     AND TSLUMP.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("     AND TSLUMP.SCHREGNO = T0.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T3.SHOWORDER3, 99), ");
            stb.append("     T0.SUBCLASSCD ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67772 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _semester;
        final String _scoreSemester;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String _recordDiv;
        final String _knjdBehaviorsd_UseText;
        final String[] _categorySelected;
        final String _trCd1;
        final String _subtrCd1;
        final String _printRegd;
//        final String _documentRoot;
//        final String _imagePath;
//        final String _extension;

        final String _gradeCd;
        final String _gradeCdStr;
        final String _certifSchoolDatSchoolName;
//        final String _certifSchoolDatRemark1;
//        final String _certifSchoolDatRemark2;
//        final String _certifSchoolDatPrincipalName;
//        final String _certifSchoolDatJobName;
        final String _tr1Name;
        final String _subtr1Name;
        final String _hyosiHrName;
        final Map _semesternameMap;
        final List _d059Name1List;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        String _maxSemester = "";

        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J;

       /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;

        final DecimalFormat _df02 = new DecimalFormat("00");

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _scoreSemester = "3".equals(_semester) ? "9" : _semester;
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _recordDiv = (!"001".equals(_gradeHrclass.substring(2)) && !"002".equals(_gradeHrclass.substring(2))) ? "2" : "1"; // getRecordDiv(db2);
            _knjdBehaviorsd_UseText = request.getParameter("knjdBehaviorsd_UseText");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _trCd1 = getRegdHdat(db2, "TR_CD1");
            _subtrCd1 = getRegdHdat(db2, "SUBTR_CD1");
            _printRegd = request.getParameter("PRINT_REGD");

//            _documentRoot = request.getParameter("DOCUMENTROOT");
//            _imagePath = request.getParameter("IMAGEPATH");
//
//            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
//            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
//            _descDate = request.getParameter("DESC_DATE");

            _gradeCd = getGradeCd(db2, _grade);
            _gradeCdStr = NumberUtils.isDigits(_gradeCd) ? String.valueOf(Integer.parseInt(_gradeCd)) : "";
            _certifSchoolDatSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
//            _certifSchoolDatRemark1 = getCertifSchoolDat(db2, "REMARK1");
//            _certifSchoolDatRemark2 = getCertifSchoolDat(db2, "REMARK2");
//            _certifSchoolDatPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
//            _certifSchoolDatJobName = getCertifSchoolDat(db2, "JOB_NAME");
            _tr1Name = getStaffname(db2, _trCd1);
            _subtr1Name = getStaffname(db2, _subtrCd1);
            _hyosiHrName = hankakuToZenkaku(_gradeCdStr) + "年 " + getHrClassName1(db2, _ctrlYear, _semester, _gradeHrclass) + "組";
            _semesternameMap = getSemesternameMap(db2);
            _d059Name1List = getNameMstD059Name1List(db2);

            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"), "+", " "); // 担任からの所見

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

        }

        private String getRegdHdat(final DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM SCHREG_REGD_HDAT WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "' AND GRADE || HR_CLASS = '" + _gradeHrclass + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        private String getHrClassName1(final DB2UDB db2, final String year, final String semester, final String gradeHrclass) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT HR_CLASS_NAME1 FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("HR_CLASS_NAME1")) {
                        rtn = rs.getString("HR_CLASS_NAME1");
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null == rtn) {
                try {
                    final String sql = " SELECT HR_CLASS FROM SCHREG_REGD_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE || HR_CLASS = '" + gradeHrclass + "' ";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        if (null == rtn && null != rs.getString("HR_CLASS")) {
                            rtn = NumberUtils.isDigits(rs.getString("HR_CLASS")) ? String.valueOf(Integer.parseInt(rs.getString("HR_CLASS"))) : rs.getString("HR_CLASS");
                        }
                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            if (null == rtn) {
                rtn = "";
            }
            return rtn;
        }

        private Map getSemesternameMap(final DB2UDB db2) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SEMESTER, SEMESTERNAME FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' ORDER BY SEMESTER DESC ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                    if (!"9".equals(rs.getString("SEMESTER")) && "".equals(_maxSemester)) {
                        _maxSemester = rs.getString("SEMESTER");
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getStaffname(final DB2UDB db2, final String staffcd) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + staffcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("STAFFNAME")) {
                        rtn = rs.getString("STAFFNAME");
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '103' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String hankakuToZenkaku(final String str) {
            if (null == str) {
                return null;
            }
            final String[] nums = new String[]{"０", "１", "２", "３", "４", "５", "６", "７", "８", "９"};
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < str.length(); i++) {
                final String s = String.valueOf(str.charAt(i));
                if (NumberUtils.isDigits(s)) {
                    final int j = Integer.parseInt(s);
                    stb.append(nums[j]);
                } else {
                    stb.append(s);
                }
            }
            return stb.toString();
        }

        private String getGradeCd(final DB2UDB db2, final String grade) {
            String gradeCd = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    gradeCd = rs.getString("GRADE_CD");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeCd;
        }


        private String getRecordDiv(final DB2UDB db2) {
            String recordDiv = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_FI_HDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND T1.SEMESTER = '" + _semester + "' ");
                stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    recordDiv = rs.getString("RECORD_DIV");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return recordDiv;
        }


        private List getNameMstD059Name1List(final DB2UDB db2) {
            List d059Name1List = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     NAME1 ");
                stb.append(" FROM ");
                stb.append("     V_NAME_MST T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND T1.NAMECD1 = 'D059' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    d059Name1List.add(rs.getString("NAME1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return d059Name1List;
        }
    }
}

// eof

