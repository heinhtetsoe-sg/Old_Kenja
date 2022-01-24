/*
 * $Id: 85963ad6264e7414aec3567a168458d430e6280b $
 *
 * 作成日: 2018/05/17
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

/**
 * 学校教育システム 賢者 [成績管理] 中学通知票
 */

public class KNJD185T {

    private static final Log log = LogFactory.getLog(KNJD185T.class);

    private boolean _hasData;
    private static final String ZENKI = "1";
    private static final String KOUKI = "2";

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List viewClassList = ViewClass.getViewClassList(db2, _param);

        final List studentList = getStudentList(db2);
        AttendSemesDat.setAttendSemesDatMap(db2, _param, studentList);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            student.load(db2, _param);

            // 表紙
            printSvfHyoshi(db2, svf, student);
            // 学習のようす等
            printSvfMainSeiseki(db2, svf, student, viewClassList);
            _hasData = true;
        }
    }

    private List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");

                final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final Student student = new Student(schregno, name, hrName, attendno);
                studentList.add(student);
            }

        } catch (SQLException e) {
            log.error("exception!!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }

    private String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1 ");
        stb.append("            , V_SEMESTER_GRADE_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
        stb.append("        AND T1.SEMESTER = '"+ param._semester +"' ");
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.GRADE = T2.GRADE ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
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
        stb.append("        T7.HR_NAME, ");
        stb.append("        T1.ATTENDNO, ");
        stb.append("        T5.NAME, ");
        stb.append("        T5.REAL_NAME, ");
        stb.append("        CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME ");
        stb.append("FROM    SCHNO_A T1 ");
        stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("        LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
        stb.append("        LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
        stb.append("ORDER BY ATTENDNO");
        return stb.toString();
    }

    protected void VrsOutRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOut(field, (String) list.get(i));
            }
        }
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOutn(field, i + 1, (String) list.get(i));
            }
        }
    }

    /**
     * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
     * @param param サイズタイプのパラメータ文字列
     * @param pos split後のインデクス (0:w, 1:h)
     * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
     */
    private static int getParamSizeNum(final String param, final int pos) {
        int num = -1;
        String[] nums = StringUtils.split(param, " * ");
        if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
            num = -1;
        } else {
            try {
                num = Integer.valueOf(nums[pos]).intValue();
            } catch (Exception e) {
                log.error("Exception!", e);
            }
        }
        return num;
    }

    private static String trimLeft(final String s) {
        if (null == s) {
            return null;
        }
        String rtn = s;
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch != ' ' && ch != '　') {
                rtn = s.substring(i);
                break;
            }
        }
        return rtn;
    }

    private String toZenkaku(final String semester) {
        if (StringUtils.isBlank(semester)) {
            return "";
        }
        final char n = semester.charAt(0);
        return String.valueOf((char) (0xFF10 + n - '0'));
    }

    /**
     * 表紙を印刷する
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvfHyoshi(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String formName = _param._semester.equals(_param._maxSemester) ? "KNJD185T_1_2.frm" : "KNJD185T_1_1.frm";
        svf.VrSetForm(formName, 1);

        //表
        svf.VrsOut("YEAR", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");

        svf.VrsOut("SCHOOL_LOGO", _param.getImagePath("SCHOOLLOGO"));
        svf.VrsOut("SCHOOL_PIC", _param.getImagePath("SCHOOLPIC"));

        svf.VrsOut("SCHOOL_NAME2", _param._certifSchoolSchoolName);

        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno;
        svf.VrsOut("HR_NAME", student._hrName + attendno + "番");

        String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "4" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 16 ? "2" : "1";
        svf.VrsOut("NAME1_" + nameField, student._name);


        //終了証
        String nameField2 = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "4" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 16 ? "2" : "1";
        svf.VrsOut("NAME2_" + nameField2, student._name);

        svf.VrsOut("GRADE", _param._gradeName2);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._descDate));

        svf.VrsOut("SCHOOL_NAME1", _param._certifSchoolRemark3);
        svf.VrsOut("PRESIDENT_NAME1", _param._certifSchoolPrincipalName);

        svf.VrEndPage();
    }

    /**
     * 学習のようす等を印刷する
     * @param db2
     * @param svf
     * @param student
     * @param viewClassList
     */
    private void printSvfMainSeiseki(final DB2UDB db2, final Vrw32alp svf, final Student student, final List viewClassList) {

        final String formName = "1".equals(_param._gradeCdStr) ? "KNJD185T_2_1.frm" : "KNJD185T_2_2.frm";
        svf.VrSetForm(formName, 4);

        printSvfStudent(svf, student);

        printSvfReportTotalstudytime(svf, student);

        printSvfReport(svf, student);

        printShukketsu(svf, student);

        printSvfViewRecord(svf, student, viewClassList);
    }

    private void printSvfStudent(final Vrw32alp svf, final Student student) {
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 32 ? "2" : "";
        svf.VrsOut("NAME" + nameField, student._name);
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno;
        svf.VrsOut("HR_NAME", student._hrName + " " + attendno + "番");

        final String principalField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 32 ? "2" : "1";
        svf.VrsOut("PRINCIPAL_NAME" + principalField, _param._certifSchoolPrincipalName);
        final String teacherField = KNJ_EditEdit.getMS932ByteLength(_param._tr1Name) > 32 ? "2" : "1";
        svf.VrsOut("TEACHER_NAME" + teacherField, _param._tr1Name);
}

    private static String notZero(int n) {
        return String.valueOf(n);
    }

    /**
     * 『出欠の記録』を印字する
     * @param svf
     * @param student
     */

    private void printShukketsu(final Vrw32alp svf, final Student student) {
        final String[] months = new String[] {"04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03"};
        final String[] monthsName = new String[] {"４月", "５月", "６月", "７月", "８月", "９月", "１０月", "１１月", "１２月", "１月", "２月", "３月"};

        int lessonTotal = 0;
        int suspendTotal = 0;
        int mlessonTotal = 0;
        int sickTotal = 0;
        int presentTotal = 0;
        int lateTotal = 0;
        int earlyTotal = 0;
        for (int i = 0; i < months.length; i++) {
            final AttendSemesDat attSemes = (AttendSemesDat) student._attendSemesMap.get(months[i]);
            final int j = i + 1;

            svf.VrsOutn("MONTH", j, monthsName[i]);
            if (null == attSemes) {
                continue;
            }
            svf.VrsOutn("LESSON", j, attendVal(attSemes._lesson));
            svf.VrsOutn("SUSPEND", j, attendVal(attSemes._suspend + attSemes._mourning + attSemes._virus + attSemes._koudome));
            svf.VrsOutn("MUST", j, attendVal(attSemes._mlesson));
            svf.VrsOutn("NOTICE", j, attendVal(attSemes._sick));
            svf.VrsOutn("ATTEND", j, attendVal(attSemes._present));
            svf.VrsOutn("LATE", j, attendVal(attSemes._late));
            svf.VrsOutn("EARLY", j, attendVal(attSemes._early));
            lessonTotal  += attSemes._lesson;
            suspendTotal += attSemes._suspend + attSemes._mourning + attSemes._virus + attSemes._koudome;
            mlessonTotal += attSemes._mlesson;
            sickTotal    += attSemes._sick;
            presentTotal += attSemes._present;
            lateTotal    += attSemes._late;
            earlyTotal   += attSemes._early;
        }
        svf.VrsOutn("MONTH", 13, "計");
        svf.VrsOutn("LESSON", 13, attendVal(lessonTotal));
        svf.VrsOutn("SUSPEND", 13, attendVal(suspendTotal));
        svf.VrsOutn("MUST", 13, attendVal(mlessonTotal));
        svf.VrsOutn("NOTICE", 13, attendVal(sickTotal));
        svf.VrsOutn("ATTEND", 13, attendVal(presentTotal));
        svf.VrsOutn("LATE", 13, attendVal(lateTotal));
        svf.VrsOutn("EARLY", 13, attendVal(earlyTotal));
    }

    private static String attendVal(int n) {
        return String.valueOf(n);
    }

    private String spacedName(final String name, final int max0) {
        final int max = max0 / 2; // 2行で1レコード
        final int spaceCount = (max - name.length()) / (name.length() + 1);
        final StringBuffer spacedName = new StringBuffer();
        for (int i = 0; i < name.length(); i++) {
            for (int j = 0; j < spaceCount; j++) {
                spacedName.append("　");
            }
            spacedName.append(name.charAt(i));
        }
        for (int j = 0; j < spaceCount; j++) {
            spacedName.append("　");
        }
        for (int i = 0; i < (max - spacedName.length()) / 2; i++) {
            spacedName.insert(0, "　");
            spacedName.append("　");
        }
        for (int i = 0; i < (max - spacedName.length()); i++) {
            spacedName.append("　");
        }
        return spacedName.toString();
    }

    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printSvfViewRecord(final Vrw32alp svf, final Student student, final List viewClassList) {

        for (final Iterator it = viewClassList.iterator(); it.hasNext();) {
            final ViewClass viewClass = (ViewClass) it.next();

            if ("1".equals(viewClass._electDiv) && !student.hasChairSubclass(viewClass._subclasscd)) { // 選択教科は講座名簿がある科目のみ表示対象
                continue;
            }

            final String viewNameField = viewClass.getViewSize() > 4 ? "1_" : "2_";
            for (int i = 0; i < viewClass.getViewSize(); i++) {
                final String checkKey1 = _param._grade + ZENKI + viewClass._subclasscd;
                final String setVal1 = _param._jviewInputMap.containsKey(checkKey1) ? "" : "／";

                final String checkKey2 = _param._grade + KOUKI + viewClass._subclasscd;
                final String setVal2 = _param._jviewInputMap.containsKey(checkKey2) ? "" : "／";

                svf.VrsOut("SUBJECTNAME" + viewNameField + "1", viewClass._subclassname);

                final String viewname = viewClass.getViewName(i);
                final String viewfield = "VIEWNAME" + viewNameField + (i + 1);
                svf.VrsOut(viewfield, viewname); // 観点名称

                final List viewRecordList = student.getViewList(viewClass._subclasscd, viewClass.getViewCd(i));
                for (final Iterator itv = viewRecordList.iterator(); itv.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) itv.next();
                    svf.VrsOut("VIEW" + viewNameField + viewRecord._semester + "_" + (i + 1), viewRecord._status); // 観点
                    if ("／".equals(setVal1)) {
                        svf.VrsOut("VIEW" + viewNameField + ZENKI + "_" + (i + 1), setVal1); // 観点
                    }
                    if ("／".equals(setVal2)) {
                        svf.VrsOut("VIEW" + viewNameField + KOUKI + "_" + (i + 1), setVal2); // 観点
                    }
                }

                final List viewValuationList = student.getValueList(viewClass._subclasscd);
                for (final Iterator itv = viewValuationList.iterator(); itv.hasNext();) {
                    final ViewValuation viewValuation = (ViewValuation) itv.next();
                    final String value;
                    if ("1".equals(viewClass._electDiv)) {
                        if (_param._d001Abbv1Map.containsKey(viewValuation._value)) {
                            value = (String) _param._d001Abbv1Map.get(viewValuation._value);
                        } else {
                            value = viewValuation._value;
                        }
                    } else {
                        value = viewValuation._value;
                    }
                    svf.VrsOut("RATE" + viewNameField + viewValuation._semester, value); // 評定
                    if ("／".equals(setVal1)) {
                        svf.VrsOut("RATE" + viewNameField + ZENKI, setVal1); // 観点
                    }
                    if ("／".equals(setVal2)) {
                        svf.VrsOut("RATE" + viewNameField + KOUKI, setVal2); // 観点
                    }
                }

            }
            svf.VrEndRecord();
        }
    }

    /**
     * 『特別活動等の記録』『部活動』『その他の活動』を印字する
     * @param svf
     * @param student
     */
    private void printSvfReport(final Vrw32alp svf, final Student student) {

        final int pcharsttSE = getParamSizeNum(_param._HREPORTREMARK_DETAIL_DAT_01_01_REMARK3_SIZE_J, 0);
        final int plinesttSE = getParamSizeNum(_param._HREPORTREMARK_DETAIL_DAT_01_01_REMARK3_SIZE_J, 1);
        final int charsttSE = (-1 == pcharsttSE || -1 == plinesttSE) ? 22 : pcharsttSE;
        final int linesttSE = (-1 == pcharsttSE || -1 == plinesttSE) ?  3 : plinesttSE;

        final int pcharsttOT = getParamSizeNum(_param._HREPORTREMARK_DETAIL_DAT_03_01_REMARK1_SIZE_J, 0);
        final int plinesttOT = getParamSizeNum(_param._HREPORTREMARK_DETAIL_DAT_03_01_REMARK1_SIZE_J, 1);
        final int charsttOT = (-1 == pcharsttOT || -1 == plinesttOT) ? 26 : pcharsttOT;
        final int linesttOT = (-1 == pcharsttOT || -1 == plinesttOT) ?  3 : plinesttOT;

        final int pcharsttML = getParamSizeNum(_param._HREPORTREMARK_DETAIL_DAT_04_01_REMARK1_SIZE_J, 0);
        final int plinesttML = getParamSizeNum(_param._HREPORTREMARK_DETAIL_DAT_04_01_REMARK1_SIZE_J, 1);
        final int charsttML = (-1 == pcharsttML || -1 == plinesttML) ? 46 : pcharsttML;
        final int linesttML = (-1 == pcharsttML || -1 == plinesttML) ?  2 : plinesttML;

        for (final Iterator it = student._hReportRemarkDetailDatList.iterator(); it.hasNext();) {
            final HReportRemarkDetailDat hReportRemarkDetailDat = (HReportRemarkDetailDat) it.next();
            final String div = hReportRemarkDetailDat._div;
            final String semester = hReportRemarkDetailDat._semester;
            final String code = hReportRemarkDetailDat._code;

            //特別活動
            if ("01".equals(div) && "01".equals(code)) {
                svf.VrsOut("HR_STUDY" + semester, StringUtils.defaultString(hReportRemarkDetailDat._remark1));
                svf.VrsOut("COMMITTEE" + semester, StringUtils.defaultString(hReportRemarkDetailDat._remark2));
                VrsOutnRenban(svf, "SCHOOL_EVENT" + semester, KNJ_EditKinsoku.getTokenList(hReportRemarkDetailDat._remark3, charsttSE * 2, linesttSE));
            }

            //部活動
            if ("02".equals(div) && "01".equals(code) && _param._semester.equals(semester)) {
                svf.VrsOut("CLUB", StringUtils.defaultString(hReportRemarkDetailDat._remark1));
                svf.VrsOut("CLUB_REC", StringUtils.defaultString(hReportRemarkDetailDat._remark2));
            }

            //その他の活動
            if ("03".equals(div) && "01".equals(code) && _param._semester.equals(semester)) {
                VrsOutnRenban(svf, "OTHER_ACT", KNJ_EditKinsoku.getTokenList(hReportRemarkDetailDat._remark1, charsttOT * 2, linesttOT));
            }
            //道徳欄
            if ("04".equals(div) && "01".equals(code)) {
                VrsOutnRenban(svf, "MORAL" + semester, KNJ_EditKinsoku.getTokenList(hReportRemarkDetailDat._remark1, charsttML * 2, linesttML));
            }
        }
        if (!_param._semester.equals(_param._knjSchoolMst._semesterDiv)) {
            svf.VrsOut("PARENTSTAMP", "保護者");
        }
    }

    /**
     * 『総合的な学習の時間』『通信欄』を印字する
     * @param svf
     * @param student
     */
    private void printSvfReportTotalstudytime(final Vrw32alp svf, final Student student) {

        final int pcharsttTS = getParamSizeNum(_param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J, 0);
        final int plinesttTS = getParamSizeNum(_param._HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J, 1);
        final int charsttTS = (-1 == pcharsttTS || -1 == plinesttTS) ? 15 : pcharsttTS;
        final int linesttTS = (-1 == pcharsttTS || -1 == plinesttTS) ?  1 : plinesttTS;

        final int pcharsttRE = getParamSizeNum(_param._HREPORTREMARK_DAT_REMARK1_SIZE_J, 0);
        final int plinesttRE = getParamSizeNum(_param._HREPORTREMARK_DAT_REMARK1_SIZE_J, 1);
        final int charsttRE = (-1 == pcharsttRE || -1 == plinesttRE) ? 15 : pcharsttRE;
        final int linesttRE = (-1 == pcharsttRE || -1 == plinesttRE) ?  1 : plinesttRE;

        final int pcharsttSP = getParamSizeNum(_param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J, 0);
        final int plinesttSP = getParamSizeNum(_param._HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J, 1);
        final int charsttSP = (-1 == pcharsttSP || -1 == plinesttSP) ? 22 : pcharsttSP;
        final int linesttSP = (-1 == pcharsttSP || -1 == plinesttSP) ?  5 : plinesttSP;

        final int pcharsttCM = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J, 0);
        final int plinesttCM = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_J, 1);
        final int charsttCM = (-1 == pcharsttCM || -1 == plinesttCM) ? 44 : pcharsttCM;
        final int linesttCM = (-1 == pcharsttCM || -1 == plinesttCM) ?  4 : plinesttCM;

        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
            final String semester = hReportRemarkDat._semester;

            VrsOutRenban(svf, "TOTAL_STUDY" + semester, KNJ_EditKinsoku.getTokenList(hReportRemarkDat._totalstudytime, charsttTS * 2, linesttTS));
            VrsOutnRenban(svf, "VIEW" + semester, KNJ_EditKinsoku.getTokenList(hReportRemarkDat._remark1, charsttRE * 2, linesttRE));
            VrsOutnRenban(svf, "TOTAL_ACT" + semester, KNJ_EditKinsoku.getTokenList(hReportRemarkDat._specialactremark, charsttSP * 2, linesttSP));
            VrsOutnRenban(svf, "COMM" + semester, KNJ_EditKinsoku.getTokenList(hReportRemarkDat._communication, charsttCM * 2, linesttCM));
        }

    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        List _viewRecordList = Collections.EMPTY_LIST; // 観点
        List _viewValuationList = Collections.EMPTY_LIST; // 評定
        Map _attendSemesMap = Collections.EMPTY_MAP; // 出欠の記録
        List _hReportRemarkDatList = Collections.EMPTY_LIST; // 所見
        List _hReportRemarkDetailDatList = Collections.EMPTY_LIST; // 所見(特別活動)
        List _chairSubclassList = Collections.EMPTY_LIST;

        public Student(final String schregno, final String name, final String hrName, final String attendno) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
        }

        public void load(final DB2UDB db2, final Param param) {
            _viewRecordList = ViewRecord.getViewRecordList(db2, param, _schregno);
            _viewValuationList = ViewValuation.getViewValuationList(db2, param, _schregno);
            _hReportRemarkDatList = HReportRemarkDat.getHReportRemarkDatList(db2, param, _schregno);
            _hReportRemarkDetailDatList = HReportRemarkDetailDat.getHReportRemarkDetailDatList(db2, param, _schregno);
            _chairSubclassList = ChairSubclass.load(db2, param, _schregno);
        }

        public boolean hasChairSubclass(final String subclasscd) {
            return null != ChairSubclass.getChairSubclass(_chairSubclassList, subclasscd);
        }

        /**
         * 観点コードの観点のリストを得る
         * @param subclasscd 科目コード
         * @param viewcd 観点コード
         * @return 観点コードの観点のリスト
         */
        public List getViewList(final String subclasscd, final String viewcd) {
            final List rtn = new ArrayList();
            if (null != viewcd) {
                for (Iterator it = _viewRecordList.iterator(); it.hasNext();) {
                    final ViewRecord viewRecord = (ViewRecord) it.next();
                    if (viewRecord._subclasscd.equals(subclasscd) && viewcd.equals(viewRecord._viewcd)) {
                        rtn.add(viewRecord);
                    }
                }
            }
            return rtn;
        }

        /**
         * 評定のリストを得る
         * @param subclasscd 評定の科目コード
         * @return 評定のリスト
         */
        public List getValueList(final String subclasscd) {
            final List rtn = new ArrayList();
            if (null != subclasscd) {
                for (Iterator it = _viewValuationList.iterator(); it.hasNext();) {
                    final ViewValuation viewValuation = (ViewValuation) it.next();
                    if (subclasscd.equals(viewValuation._subclasscd)) {
                        rtn.add(viewValuation);
                    }
                }
            }
            return rtn;
        }
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _subclasscd;
        final String _subclassname;
        final String _electDiv;
        final List _viewList;
        final List _valuationList;
        ViewClass(
                final String classcd,
                final String subclasscd,
                final String subclassname,
                final String electDiv) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _electDiv = electDiv;
            _viewList = new ArrayList();
            _valuationList = new ArrayList();
        }

        public void addView(final String viewcd, final String viewname) {
            _viewList.add(new String[]{viewcd, viewname});
        }

        public String getViewCd(final int i) {
            return ((String[]) _viewList.get(i))[0];
        }

        public String getViewName(final int i) {
            return ((String[]) _viewList.get(i))[1];
        }

        public int getViewSize() {
            return _viewList.size();
        }

        public static List getViewClassList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewClassSql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String electDiv = rs.getString("ELECTDIV");
                    final String viewcd = rs.getString("VIEWCD");
                    final String viewname = rs.getString("VIEWNAME");

                    ViewClass viewClass = null;
                    for (final Iterator it = list.iterator(); it.hasNext();) {
                        final ViewClass viewClass0 = (ViewClass) it.next();
                        if (viewClass0._subclasscd.equals(subclasscd)) {
                            viewClass = viewClass0;
                            break;
                        }
                    }

                    if (null == viewClass) {
                        viewClass = new ViewClass(classcd, subclasscd, subclassname, electDiv);
                        list.add(viewClass);
                    }

                    viewClass.addView(viewcd, viewname);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getViewClassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     VALUE(T4.ELECTDIV, '0') AS ELECTDIV, ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            }
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T3.CLASSCD < '90' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T4.ELECTDIV, '0'), ");
            stb.append("     VALUE(T3.SHOWORDER3, -1), ");
            stb.append("     T3.CLASSCD, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD ");
            return stb.toString();
        }
        public String toString() {
            return _subclasscd + ":" + _subclassname + " " + _electDiv;
        }
    }

    /**
     * 学習の記録（観点）
     */
    private static class ViewRecord {

        final String _semester;
        final String _viewcd;
        final String _status;
        final String _grade;
        final String _viewname;
        final String _classcd;
        final String _subclasscd;
        final String _classMstShoworder;
        final String _showorder;
        ViewRecord(
                final String semester,
                final String viewcd,
                final String status,
                final String grade,
                final String viewname,
                final String classcd,
                final String subclasscd,
                final String classMstShoworder,
                final String showorder) {
            _semester = semester;
            _viewcd = viewcd;
            _status = status;
            _grade = grade;
            _viewname = viewname;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classMstShoworder = classMstShoworder;
            _showorder = showorder;
        }

        public static List getViewRecordList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewRecordSql(param, schregno);
                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");
                    final String viewcd = rs.getString("VIEWCD");
                    final String status = rs.getString("STATUS");
                    final String grade = rs.getString("GRADE");
                    final String viewname = rs.getString("VIEWNAME");
                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String classMstShoworder = rs.getString("CLASS_MST_SHOWORDER");
                    final String showorder = rs.getString("SHOWORDER");

                    final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, grade, viewname, classcd, subclasscd, classMstShoworder, showorder);

                    list.add(viewRecord);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getViewRecordSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE   ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , T3.SCHREGNO ");
            stb.append("     , T3.STATUS ");
            stb.append("     , T4.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("     , T1.SUBCLASSCD ");
            }
            stb.append("     , T4.SHOWORDER AS CLASS_MST_SHOWORDER ");
            stb.append("     , T1.SHOWORDER ");
            stb.append(" FROM JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.CLASSCD = T1.CLASSCD  ");
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND  ");
                stb.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            }
            stb.append("         AND T3.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3.YEAR = T2.YEAR ");
            stb.append("         AND T3.SEMESTER <= '" + param._semester + "' ");
            stb.append("         AND T3.SCHREGNO = '" + schregno + "' ");
            stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T1.VIEWCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            }
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , VALUE(T4.SHOWORDER, 0) ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     , VALUE(T1.SHOWORDER, 0) ");
            stb.append("     , T1.VIEWCD ");
            return stb.toString();
        }
    }

    /**
     * 学習の記録（評定）
     */
    private static class ViewValuation {
        final String _semester;
        final String _classcd;
        final String _subclasscd;
        final String _subclassname;
        final String _value;
        ViewValuation(
                final String semester,
                final String classcd,
                final String subclasscd,
                final String subclassname,
                final String value) {
            _semester = semester;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _value = value;
        }

        public static List getViewValuationList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewValuationSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");
                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String value = rs.getString("VALUE");
                    final ViewValuation viewValuation = new ViewValuation(semester, classcd, subclasscd, subclassname, value);

                    list.add(viewValuation);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getViewValuationSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T2.SUBCLASSCD, ");
            }
            stb.append("     T4.SUBCLASSNAME, ");
            stb.append("     T2.SCORE AS VALUE ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT T2 ");
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.CLASSCD = T2.CLASSCD  ");
                stb.append("         AND T4.SCHOOL_KIND = T2.SCHOOL_KIND  ");
                stb.append("         AND T4.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            }
            stb.append("     LEFT JOIN CLASS_MST T5 ON T5.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T5.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            }
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T2.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T2.TESTKINDCD = '99' ");
            stb.append("     AND T2.TESTITEMCD = '00' ");
            stb.append("     AND (T2.SEMESTER < '9' AND T2.SCORE_DIV = '08' OR T2.SEMESTER = '9' AND T2.SCORE_DIV = '09') ");
            stb.append("     AND T2.SCHREGNO = '" + schregno + "' ");
            if ("Y".equals(param._d016Namespare1)) {
                stb.append("     AND NOT EXISTS ( ");
                stb.append("         SELECT 'X' ");
                stb.append("         FROM ");
                stb.append("             SUBCLASS_REPLACE_COMBINED_DAT L1 ");
                stb.append("         WHERE ");
                stb.append("             L1.YEAR = T2.YEAR ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("             AND L1.ATTEND_CLASSCD = T2.CLASSCD ");
                    stb.append("             AND L1.ATTEND_SCHOOL_KIND = T2.SCHOOL_KIND ");
                    stb.append("             AND L1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD ");
                }
                stb.append("             AND L1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append("     ) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T5.SHOWORDER3, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
            } else {
                stb.append("     T2.SUBCLASSCD ");
            }
            return stb.toString();
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
                sql.append(" WHERE YEAR = '" + param._year + "' ");
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
                        attendSemesDat._mlesson = mlesson;

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

        public HReportRemarkDat(
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

        public static List getHReportRemarkDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkSql(param, schregno);
                ps = db2.prepareStatement(sql);
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
                    list.add(hReportRemarkDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getHReportRemarkSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            return stb.toString();
        }
    }

    /**
     * 通知表所見(特別活動)
     */
    private static class HReportRemarkDetailDat {
        final String _semester;
        final String _div;
        final String _code;
        final String _remark1;
        final String _remark2;
        final String _remark3;

        public HReportRemarkDetailDat(
                final String semester,
                final String div,
                final String code,
                final String remark1,
                final String remark2,
                final String remark3
                ) {
            _semester = semester;
            _div = div;
            _code = code;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
        }

        public static List getHReportRemarkDetailDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getHReportRemarkDetailSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String div = rs.getString("DIV");
                    final String code = rs.getString("CODE");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");

                    final HReportRemarkDetailDat hReportRemarkDetailDat = new HReportRemarkDetailDat(semester, div, code, remark1, remark2, remark3);
                    list.add(hReportRemarkDetailDat);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getHReportRemarkDetailSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DETAIL_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.DIV IN ('01','02','03', '04') ");
            stb.append(" ORDER BY ");
            stb.append("     DIV, ");
            stb.append("     SEMESTER, ");
            stb.append("     CODE ");
            return stb.toString();
        }
    }

    private static class ChairSubclass {
        final String _subclasscd;
        final List _chaircdList;
        public ChairSubclass(final String subclasscd) {
            _subclasscd = subclasscd;
            _chaircdList = new ArrayList();
        }
        public static List load(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT DISTINCT ");
                sql.append("   T2.CHAIRCD, ");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD ");
                } else {
                    sql.append("     T2.SUBCLASSCD ");
                }
                sql.append(" FROM ");
                sql.append("   CHAIR_STD_DAT T1 ");
                sql.append("   INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
                sql.append("     AND T2.SEMESTER = T1.SEMESTER ");
                sql.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
                sql.append(" WHERE ");
                sql.append("     T1.YEAR = '" + param._year + "' ");
                sql.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
                sql.append("     AND T1.SCHREGNO = '" + schregno + "' ");
                sql.append("     AND '" + param._date + "' BETWEEN T1.APPDATE AND T1.APPENDDATE ");

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    ChairSubclass cs = getChairSubclass(list, rs.getString("SUBCLASSCD"));
                    if (null == cs) {
                        cs = new ChairSubclass(rs.getString("SUBCLASSCD"));
                        list.add(cs);
                    }
                    cs._chaircdList.add(rs.getString("CHAIRCD"));
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        public static ChairSubclass getChairSubclass(final List chairSubclassList, final String subclasscd) {
            ChairSubclass chairSubclass = null;
            for (final Iterator it = chairSubclassList.iterator(); it.hasNext();) {
                final ChairSubclass cs = (ChairSubclass) it.next();
                if (null != cs._subclasscd && cs._subclasscd.equals(subclasscd)) {
                    chairSubclass = cs;
                    break;
                }
            }
            return chairSubclass;
        }
    }

    /**
     * 表紙(観点の評価について)
     */
    private static class NMD029 {
        final String _namecd2;
        final String _name1;
        final String _name2;

        public NMD029(
                final String namecd2,
                final String name1,
                final String name2) {
            _namecd2 = namecd2;
            _name1 = name1;
            _name2 = name2;
        }

        public static List getNMD029List(final DB2UDB db2, final String year) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getNMD029Sql(year);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namecd2 = rs.getString("NAMECD2");
                    final String name1 = rs.getString("NAME1");
                    final String name2 = rs.getString("NAME2");

                    final NMD029 nmD029 = new NMD029(namecd2, name1, name2);
                    list.add(nmD029);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getNMD029Sql(final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    T1.NAMECD2, ");
            stb.append("    T1.NAME1, ");
            stb.append("    T1.NAME2 ");
            stb.append(" FROM V_NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + year + "' ");
            stb.append("    AND T1.NAMECD1 = 'D029' ");
            stb.append(" ORDER BY ");
            stb.append("    T1.NAMECD2 ");
            return stb.toString();
        }
    }

    /**
     * 表紙(選択科目の評価・評定について)
     */
    private static class NMD001 {
        final String _namecd2;
        final String _name1;
        final String _name2;
        final String _abbv1;

        public NMD001(
                final String namecd2,
                final String name1,
                final String name2,
                final String abbv1
        ) {
            _namecd2 = namecd2;
            _name1 = name1;
            _name2 = name2;
            _abbv1 = abbv1;
        }

        public static List getNMD001List(final DB2UDB db2, final String year) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getNMD001Sql(year);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namecd2 = rs.getString("NAMECD2");
                    final String name1 = rs.getString("NAME1");
                    final String name2 = rs.getString("NAME2");
                    final String abbv1 = rs.getString("ABBV1");

                    final NMD001 nmD001 = new NMD001(namecd2, name1, name2, abbv1);
                    list.add(nmD001);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getNMD001Sql(final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    T1.NAMECD2, ");
            stb.append("    T1.NAME1, ");
            stb.append("    T1.NAME2, ");
            stb.append("    T1.ABBV1 ");
            stb.append(" FROM V_NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + year + "' ");
            stb.append("    AND T1.NAMECD1 = 'D001' ");
            stb.append(" ORDER BY ");
            stb.append("    T1.NAMECD2 ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77481 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _maxSemester;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        final String _trCd1;
        final String _documentRoot;
        final String _imagePath;
        final String _extension;
        final String _descDate;

        final String _gradeCdStr;
        final String _gradeName2;
        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark3;
        final String _certifSchoolPrincipalName;
        final String _tr1Name;
        final String _certifSchoolJobName;
        final String _d016Namespare1;
        final Map _d001Abbv1Map;
        final Map _jviewInputMap;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        final String _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J;
        final String _HREPORTREMARK_DAT_REMARK1_SIZE_J;
        final String _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J;
        final String _HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_J;
        final String _HREPORTREMARK_DETAIL_DAT_01_01_REMARK2_SIZE_J;
        final String _HREPORTREMARK_DETAIL_DAT_01_01_REMARK3_SIZE_J;
        final String _HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE_J;
        final String _HREPORTREMARK_DETAIL_DAT_02_01_REMARK2_SIZE_J;
        final String _HREPORTREMARK_DETAIL_DAT_03_01_REMARK1_SIZE_J;
        final String _HREPORTREMARK_DETAIL_DAT_04_01_REMARK1_SIZE_J;
        final String _reportSpecialSize03_02;

       /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;

        final List _nmD029List;
        final List _nmD001List;

        final DecimalFormat _df02 = new DecimalFormat("00");

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _maxSemester = getMaxSemester(db2);
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");
            _trCd1 = request.getParameter("TR_CD1");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _imagePath = request.getParameter("IMAGEPATH");

            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _descDate = request.getParameter("DESC_DATE");

            final String gdatStr[] = getGradeCd(db2, _grade);
            _gradeCdStr = gdatStr[0];
            _gradeName2 = gdatStr[1];
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _tr1Name = getStaffname(db2, _trCd1);
            _certifSchoolJobName = getCertifSchoolDat(db2, "JOB_NAME");
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");
            _d001Abbv1Map = getNameMstMap(db2, _year, "D001", "ABBV1");

            _HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J"), "+", " ");                   //学習活動
            _HREPORTREMARK_DAT_REMARK1_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_REMARK1_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_REMARK1_SIZE_J"), "+", " ");                                        //観点
            _HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE_J"), "+", " ");             //活動の様子
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_J = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"), "+", " ");                      //通信欄
            _HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_J = null == request.getParameter("HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_J"), "+", " "); //学級活動
            _HREPORTREMARK_DETAIL_DAT_01_01_REMARK2_SIZE_J = null == request.getParameter("HREPORTREMARK_DETAIL_DAT_01_01_REMARK2_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DETAIL_DAT_01_01_REMARK2_SIZE_J"), "+", " "); //生徒会活動
            _HREPORTREMARK_DETAIL_DAT_01_01_REMARK3_SIZE_J = null == request.getParameter("HREPORTREMARK_DETAIL_DAT_01_01_REMARK3_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DETAIL_DAT_01_01_REMARK3_SIZE_J"), "+", " "); //学校行事
            _HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE_J = null == request.getParameter("HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE_J"), "+", " "); //部活動名
            _HREPORTREMARK_DETAIL_DAT_02_01_REMARK2_SIZE_J = null == request.getParameter("HREPORTREMARK_DETAIL_DAT_02_01_REMARK2_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DETAIL_DAT_02_01_REMARK2_SIZE_J"), "+", " "); //部活動の記録
            _HREPORTREMARK_DETAIL_DAT_03_01_REMARK1_SIZE_J = null == request.getParameter("HREPORTREMARK_DETAIL_DAT_03_01_REMARK1_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DETAIL_DAT_03_01_REMARK1_SIZE_J"), "+", " "); //その他の活動
        	_HREPORTREMARK_DETAIL_DAT_04_01_REMARK1_SIZE_J = null == request.getParameter("HREPORTREMARK_DETAIL_DAT_04_01_REMARK1_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DETAIL_DAT_04_01_REMARK1_SIZE_J"), "+", " "); //道徳欄
            _reportSpecialSize03_02 = null == request.getParameter("reportSpecialSize03_02") ? "" : StringUtils.replace(request.getParameter("reportSpecialSize03_02"), "+", " "); // 観点

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            //表紙
            _nmD029List = NMD029.getNMD029List(db2, _year);
            _nmD001List = NMD001.getNMD001List(db2, _year);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            _jviewInputMap = getJviewInputMap(db2);
        }

        private String getMaxSemester(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    MAX(SEMESTER) AS SEMESTER ");
                sql.append(" FROM ");
                sql.append("    SEMESTER_MST ");
                sql.append(" WHERE ");
                sql.append("    YEAR = '" + _year + "' ");
                sql.append("    AND SEMESTER <> '9' ");

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("SEMESTER");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        /**
         * 写真データ格納フォルダの取得
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
            } catch (SQLException e) {
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
                } catch (SQLException e) {
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

        private String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    " + field + " ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
                sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
                sql.append("   ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map getNameMstMap(final DB2UDB db2, final String year, final String namecd1, final String field) {
            Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    * ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtnMap.put(rs.getString("NAMECD2"), rs.getString(field));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }

        private String getStaffname(final DB2UDB db2, final String trCd1) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + trCd1 + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("STAFFNAME")) {
                        rtn = rs.getString("STAFFNAME");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        public String getImagePath(final String imageName) {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + imageName + "." + _extension;
            if (new java.io.File(path).exists()) {
                return path;
            }
            log.info(" not found : " + path);
            return null;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '103' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
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

        private String[] getGradeCd(final DB2UDB db2, final String grade) {
            String gradeCd = "";
            String gradeName2 = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String tmp = rs.getString("GRADE_CD");
                    if (NumberUtils.isDigits(tmp)) {
                        gradeCd = String.valueOf(Integer.parseInt(tmp));
                    }
                    gradeName2 = StringUtils.defaultString(rs.getString("GRADE_NAME2"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return new String[]{gradeCd, gradeName2};
        }

        private Map getJviewInputMap(final DB2UDB db2) {
            Map rtnMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append("     JVIEW_INPUT.GRADE || JVIEW_INPUT.SEMESTER || JVIEW_INPUT.CLASSCD || JVIEW_INPUT.SCHOOL_KIND || JVIEW_INPUT.CURRICULUM_CD || JVIEW_INPUT.SUBCLASSCD AS SET_KEY, ");
                } else {
                    stb.append("     JVIEW_INPUT.GRADE || JVIEW_INPUT.SEMESTER || JVIEW_INPUT.SUBCLASSCD AS SET_KEY, ");
                }
                stb.append("     COUNT(*) AS CNT ");
                stb.append(" FROM ");
                stb.append("     JVIEWSTAT_INPUTSEQ_DAT JVIEW_INPUT ");
                stb.append(" WHERE ");
                stb.append("     JVIEW_INPUT.YEAR = '" + _year + "' ");
                stb.append("     AND VALUE(JVIEW_INPUT.VIEWFLG, '0') = '1' ");
                stb.append(" GROUP BY ");
                stb.append("     JVIEW_INPUT.CLASSCD, ");
                stb.append("     JVIEW_INPUT.SCHOOL_KIND, ");
                stb.append("     JVIEW_INPUT.CURRICULUM_CD, ");
                stb.append("     JVIEW_INPUT.SUBCLASSCD, ");
                stb.append("     JVIEW_INPUT.GRADE, ");
                stb.append("     JVIEW_INPUT.SEMESTER ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnMap.put(rs.getString("SET_KEY"), rs.getString("CNT"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnMap;
        }
    }
}

// eof

