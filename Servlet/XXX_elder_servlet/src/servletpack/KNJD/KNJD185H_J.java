/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 4329dda9a2f3ca3ae8f0daf3933688448c6aa021 $
 *
 * 作成日: 2018/05/22
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
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD185H_J {

    private static final Log log = LogFactory.getLog(KNJD185H_J.class);

    private boolean _hasData;

    private Param _param;

    KNJEditString knjobj = new KNJEditString();

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

        final List studentList = getStudentList(db2);
        AttendSemesDat.setAttendSemesDatList(db2, _param, studentList);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            student.load(db2, _param);

            // 学習のようす等
            printSvfMainSeiseki(db2, svf, student);
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
                final String staffName = rs.getString("STAFFNAME");
                final Student student = new Student(schregno, name, hrName, attendno, staffName);
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
        stb.append("        AND T1.SEMESTER = '"+ param.getRegdSemester() +"' ");
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.GRADE = T2.GRADE ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        if ("1".equals(_param._disp)) {
            stb.append("        AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        } else {
            stb.append("        AND T1.SCHREGNO || '-' || T1.GRADE || T1.HR_CLASS || T1.ATTENDNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        }
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
        stb.append(" SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T7.HR_NAME, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    COURSECODE.COURSECODENAME, ");
        stb.append("    T5.NAME, ");
        stb.append("    T5.REAL_NAME, ");
        stb.append("    STAFF.STAFFNAME, ");
        stb.append("    CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME ");
        stb.append(" FROM ");
        stb.append("    SCHNO_A T1 ");
        stb.append("    INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
        stb.append("    LEFT JOIN STAFF_MST STAFF ON T7.TR_CD1 = STAFF.STAFFCD ");
        stb.append("    LEFT JOIN COURSECODE_MST COURSECODE ON T1.COURSECODE = COURSECODE.COURSECODE ");
        stb.append(" ORDER BY ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T1.ATTENDNO ");
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

    /**
     * 学習のようす等を印刷する
     * @param db2
     * @param svf
     * @param student
     * @param viewClassList
     */
    private void printSvfMainSeiseki(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        final String formName = "KNJD185H_1.frm";
        svf.VrSetForm(formName, 1);

        printSvfStudent(db2, svf, student);

        printSvfRecord(svf, student);

        printShukketsu(svf, student);

        printSvfAttendRemark(svf, student);

        printSvfHReport(svf, student);

        printSvfHReportDetail(svf, student);

        svf.VrEndPage();
    }

    private void printSvfStudent(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrsOut("TITLE", "成績通知表（" + KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度）");

        svf.VrsOut("SCHREGNO", student._schregno);

        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name);

        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno;
        svf.VrsOut("HR_NAME", student._hrName + attendno + "番");

        final String principalField = KNJ_EditEdit.getMS932ByteLength(_param._certifSchoolPrincipalName) > 32 ? "2" : "1";
        svf.VrsOut("PRINCIPAL_NAME" + principalField, _param._certifSchoolPrincipalName);

        final String prtTeacherName;
        if ("2".equals(_param._printTeacherType)) {
        	svf.VrsOut("JOB_NAME2", "学年主任");
        	prtTeacherName = _param._gradeChiefTeacherName;
        } else {
        	svf.VrsOut("JOB_NAME2", "担任");
        	prtTeacherName = student._staffName;
        }
        final String teacherField = KNJ_EditEdit.getMS932ByteLength(prtTeacherName) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(prtTeacherName) > 20 ? "2" : "1";
        svf.VrsOut("TEACHER_NAME" + teacherField, prtTeacherName);
    }

    /**
     * 『出欠の記録』を印字する
     * @param svf
     * @param student
     */

    private void printShukketsu(final Vrw32alp svf, final Student student) {
        for (final Iterator it = student._attendSemesDatList.iterator(); it.hasNext();) {
            final AttendSemesDat attendSemesDat = (AttendSemesDat) it.next();
            if (!NumberUtils.isDigits(attendSemesDat._semester)) {
                continue;
            }

            final int j;
            if ("9".equals(attendSemesDat._semester)) {
                continue;
            } else {
                j = Integer.parseInt(attendSemesDat._semester);
            }
            svf.VrsOutn("ATTEND" + attendSemesDat._semester, 1, notZero(attendSemesDat._lesson));
            svf.VrsOutn("ATTEND" + attendSemesDat._semester, 2, notZero(attendSemesDat._suspend + attendSemesDat._mourning + attendSemesDat._virus + attendSemesDat._koudome));
            svf.VrsOutn("ATTEND" + attendSemesDat._semester, 3, notZero(attendSemesDat._mlesson));
            svf.VrsOutn("ATTEND" + attendSemesDat._semester, 4, notZero(attendSemesDat._sick));
            svf.VrsOutn("ATTEND" + attendSemesDat._semester, 5, notZero(attendSemesDat._present));
            svf.VrsOutn("ATTEND" + attendSemesDat._semester, 6, notZero(attendSemesDat._late));
            svf.VrsOutn("ATTEND" + attendSemesDat._semester, 7, notZero(attendSemesDat._early));
        }
    }

    private static String notZero(int n) {
        return String.valueOf(n);
    }

    /**
     * 『出欠備考』を印字する
     * @param svf
     * @param student
     */
    private void printSvfAttendRemark(final Vrw32alp svf, final Student student) {

        final int charsttAR = 20;
        final int linesttAR = 10;

        String setRemark = "";
        for (final Iterator it = student._attendSemesRemarkList.iterator(); it.hasNext();) {
            final AttendSemesRemark attendSemesRemark = (AttendSemesRemark) it.next();
            final String semester = attendSemesRemark._semester;

            if (semester.equals(_param.getDataSemester())) {
                setRemark += StringUtils.defaultString(attendSemesRemark._remark1);
            }
        }
        //通信欄
        VrsOutnRenban(svf, "ATTEND_REMARK", knjobj.retDividString(setRemark, charsttAR * 2, linesttAR));

    }

    /**
     * 学習の記録を印字する
     * @param svf
     * @param student
     */
    private void printSvfRecord(final Vrw32alp svf, final Student student) {

        int line = 0;
        String befClassCd = "";
        for (final Iterator itv = student._valuationList.iterator(); itv.hasNext();) {
            final Valuation valuation = (Valuation) itv.next();
            if (!befClassCd.equals(valuation._subclasscd)) {
                line++;
            }
            final String value;
            value = valuation._value;
            final String classNameField = KNJ_EditEdit.getMS932ByteLength(valuation._subclassname) > 14 ? "2" : "1";
            svf.VrsOutn("CLASS_NAME" + classNameField, line, valuation._subclassname);
            svf.VrsOutn("DIV" + valuation._semester, line, value);
            befClassCd = valuation._subclasscd;
        }
    }

    /**
     * 『特別活動等の記録』を印字する
     * @param svf
     * @param student
     */
    private void printSvfHReportDetail(final Vrw32alp svf, final Student student) {

        final int pcharsttSE = getParamSizeNum(_param._HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE, 0);
        final int plinesttSE = getParamSizeNum(_param._HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE, 1);
        final int charsttSE = (-1 == pcharsttSE || -1 == plinesttSE) ? 30 : pcharsttSE;
        final int linesttSE = (-1 == pcharsttSE || -1 == plinesttSE) ?  7 : plinesttSE;

        final int pcharsttSE2 = getParamSizeNum(_param._HREPORTREMARK_DETAIL_DAT_01_02_REMARK1_SIZE, 0);
        final int plinesttSE2 = getParamSizeNum(_param._HREPORTREMARK_DETAIL_DAT_01_02_REMARK1_SIZE, 1);
        final int charsttSE2 = (-1 == pcharsttSE2 || -1 == plinesttSE2) ? 20 : pcharsttSE2;
        final int linesttSE2 = (-1 == pcharsttSE2 || -1 == plinesttSE2) ?  7 : plinesttSE2;

        for (final Iterator it = student._hReportRemarkDetailDatList.iterator(); it.hasNext();) {
            final HReportRemarkDetailDat hReportRemarkDetailDat = (HReportRemarkDetailDat) it.next();
            final String div = hReportRemarkDetailDat._div;
            final String semester = hReportRemarkDetailDat._semester;
            final String code = hReportRemarkDetailDat._code;

            //特別活動 委員会・係・部活動の成果
            if ("01".equals(div) && "01".equals(code) && _param.getDataSemester().equals(semester)) {
                VrsOutnRenban(svf, "ACT1", knjobj.retDividString(hReportRemarkDetailDat._remark1, charsttSE * 2, linesttSE));
            }

            //特別活動 資格取得・検定・表彰など
            if ("01".equals(div) && "02".equals(code) && _param.getDataSemester().equals(semester)) {
                VrsOutnRenban(svf, "ACT2", knjobj.retDividString(hReportRemarkDetailDat._remark1, charsttSE2 * 2, linesttSE2));
            }
        }
    }

    /**
     * 『通信欄』を印字する
     * @param svf
     * @param student
     */
    private void printSvfHReport(final Vrw32alp svf, final Student student) {

        final int pcharsttCM = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE, 0);
        final int plinesttCM = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE, 1);
        final int charsttCM = (-1 == pcharsttCM || -1 == plinesttCM) ? 30 : pcharsttCM;
        final int linesttCM = (-1 == pcharsttCM || -1 == plinesttCM) ?  3 : plinesttCM;

        for (final Iterator it = student._hReportRemarkDatList.iterator(); it.hasNext();) {
            final HReportRemarkDat hReportRemarkDat = (HReportRemarkDat) it.next();
            final String semester = hReportRemarkDat._semester;

            //通信欄
            VrsOutnRenban(svf, "COMM", knjobj.retDividString(hReportRemarkDat._communication, charsttCM * 2, linesttCM));
        }

    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _staffName;
        List _valuationList = Collections.EMPTY_LIST; // 評定
        List _attendSemesDatList = Collections.EMPTY_LIST; // 出欠の記録
        List _attendSemesRemarkList = Collections.EMPTY_LIST; // 出欠の記録
        List _hReportRemarkDatList = Collections.EMPTY_LIST; // 所見
        List _hReportRemarkDetailDatList = Collections.EMPTY_LIST; // 所見(特別活動)

        public Student(final String schregno, final String name, final String hrName, final String attendno, final String staffName) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _staffName = staffName;
        }

        public void load(final DB2UDB db2, final Param param) {
            _valuationList = Valuation.getValuationList(db2, param, _schregno);
            _attendSemesRemarkList = AttendSemesRemark.getHReportRemarkDatList(db2, param, _schregno);
            _hReportRemarkDatList = HReportRemarkDat.getHReportRemarkDatList(db2, param, _schregno);
            _hReportRemarkDetailDatList = HReportRemarkDetailDat.getHReportRemarkDetailDatList(db2, param, _schregno);
        }

        /**
         * 評定のリストを得る
         * @param subclasscd 評定の科目コード
         * @return 評定のリスト
         */
        public List getValueList(final String subclasscd) {
            final List rtn = new ArrayList();
            if (null != subclasscd) {
                for (Iterator it = _valuationList.iterator(); it.hasNext();) {
                    final Valuation viewValuation = (Valuation) it.next();
                    if (subclasscd.equals(viewValuation._subclasscd)) {
                        rtn.add(viewValuation);
                    }
                }
            }
            return rtn;
        }
    }

    /**
     * 学習の記録（評定）
     */
    private static class Valuation {
        final String _semester;
        final String _classcd;
        final String _className;
        final String _subclasscd;
        final String _subclassname;
        final String _value;
        Valuation(
                final String semester,
                final String classcd,
                final String className,
                final String subclasscd,
                final String subclassname,
                final String value) {
            _semester = semester;
            _classcd = classcd;
            _className = className;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _value = value;
        }

        public static List getValuationList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getValuationSql(param, schregno);
                log.debug(sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");
                    final String classcd = rs.getString("CLASSCD");
                    final String className = rs.getString("CLASSNAME");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String value = rs.getString("VALUE");
                    final Valuation valuation = new Valuation(semester, classcd, className, subclasscd, subclassname, value);

                    list.add(valuation);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getValuationSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T2.CLASSCD AS CLASSCD, ");
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CASE WHEN T4.SUBCLASSORDERNAME2 IS NOT NULL THEN T4.SUBCLASSORDERNAME2 ELSE T4.SUBCLASSNAME END AS SUBCLASSNAME, ");
            stb.append("     T5.CLASSNAME, ");
            stb.append("     T2.SCORE AS VALUE ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T2 ");
            stb.append("     INNER JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("         AND T4.CLASSCD = T2.CLASSCD  ");
            stb.append("         AND T4.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("         AND T4.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("     LEFT JOIN CLASS_MST T5 ON T5.CLASSCD = SUBSTR(T2.SUBCLASSCD, 1, 2) ");
            stb.append("         AND T5.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T2.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND T2.TESTKINDCD = '99' ");
            stb.append("     AND T2.TESTITEMCD = '00' ");
            stb.append("     AND T2.SCORE_DIV = '09' ");
            stb.append("     AND T2.SCHREGNO = '" + schregno + "' ");
            if ("Y".equals(param._d016Namespare1)) {
                stb.append("     AND NOT EXISTS ( ");
                stb.append("         SELECT 'X' ");
                stb.append("         FROM ");
                stb.append("             SUBCLASS_REPLACE_COMBINED_DAT L1 ");
                stb.append("         WHERE ");
                stb.append("             L1.YEAR = T2.YEAR ");
                stb.append("             AND L1.ATTEND_CLASSCD = T2.CLASSCD ");
                stb.append("             AND L1.ATTEND_SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("             AND L1.ATTEND_CURRICULUM_CD = T2.CURRICULUM_CD ");
                stb.append("             AND L1.ATTEND_SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append("     ) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T5.SHOWORDER3, ");
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND, ");
            stb.append("     T4.SHOWORDER3, ");
            stb.append("     T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
            return stb.toString();
        }
    }
    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {

        final String _semester;
        int _lesson;
        int _suspend;
        int _mourning;
        int _mlesson;
        int _sick;
        int _absent;
        int _present;
        int _late;
        int _early;
        int _transferDate;
        int _offdays;
        int _kekkaJisu;
        int _virus;
        int _koudome;

        public AttendSemesDat(
                final String semester
        ) {
            _semester = semester;
        }

        public void add(
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
            _transferDate += o._transferDate;
            _offdays += o._offdays;
            _kekkaJisu += o._kekkaJisu;
            _virus += o._virus;
            _koudome += o._koudome;
        }

        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        null,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    student._attendSemesDatList = new ArrayList();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {

                        final String semester = rs.getString("SEMESTER");
                        final int lesson = rs.getInt("LESSON");
                        final int suspend = rs.getInt("SUSPEND");
                        final int mourning = rs.getInt("MOURNING");
                        final int mlesson = rs.getInt("MLESSON");
                        final int sick = rs.getInt("SICK");
                        final int absent = rs.getInt("ABSENT");
                        final int present = rs.getInt("PRESENT");
                        final int late = rs.getInt("LATE");
                        final int early = rs.getInt("EARLY");
                        final int transferDate = rs.getInt("TRANSFER_DATE");
                        final int offdays = rs.getInt("OFFDAYS");
                        final int kekkaJisu = rs.getInt("KEKKA_JISU");
                        final int virus = rs.getInt("VIRUS");
                        final int koudome = rs.getInt("KOUDOME");

                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester);
                        attendSemesDat._lesson = lesson;
                        attendSemesDat._suspend = suspend;
                        attendSemesDat._mourning = mourning;
                        attendSemesDat._mlesson = mlesson;
                        attendSemesDat._sick = sick;
                        attendSemesDat._absent = absent;
                        attendSemesDat._present = present;
                        attendSemesDat._late = late;
                        attendSemesDat._early = early;
                        attendSemesDat._transferDate = transferDate;
                        attendSemesDat._offdays = offdays;
                        attendSemesDat._kekkaJisu = kekkaJisu;
                        attendSemesDat._virus = virus;
                        attendSemesDat._koudome = koudome;

                        student._attendSemesDatList.add(attendSemesDat);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }

    /**
     * 出欠備考
     */
    private static class AttendSemesRemark {
        final String _semester;
        final String _remark1;
        final String _remark2;
        final String _remark3;

        public AttendSemesRemark(
                final String semester,
                final String remark1,
                final String remark2,
                final String remark3) {
            _semester = semester;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
        }

        public static List getHReportRemarkDatList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getAttendSemesRemarkSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");

                    final AttendSemesRemark attendSemesRemark = new AttendSemesRemark(semester, remark1, remark2, remark3);
                    list.add(attendSemesRemark);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getAttendSemesRemarkSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     ATTEND_SEMES_REMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.COPYCD = '0' ");
            stb.append("     AND T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param.getDataSemester() + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     CASE WHEN INT(MONTH) < 4 THEN INT(MONTH) + 12 ELSE INT(MONTH) END ");
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
            stb.append("     AND T1.SEMESTER = '" + param.getDataSemester() + "' ");
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
            stb.append("     AND T1.SEMESTER = '" + param.getDataSemester() + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.DIV IN ('01','02') ");
            stb.append(" ORDER BY ");
            stb.append("     DIV, ");
            stb.append("     SEMESTER, ");
            stb.append("     CODE ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75221 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _disp;
        final String _year;
        final String _semester;
        final String _maxSemester;
        final String _date;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        final String _documentRoot;
        final String _imagePath;
        final String _extension;
        final Map _attendParamMap;
        final String _printTeacherType;

        final String _certifSchoolSchoolName;
        final String _certifSchoolRemark3;
        final String _certifSchoolPrincipalName;
        final String _certifSchoolJobName;
        final String _d016Namespare1;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE;
        final String _HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE;
        final String _HREPORTREMARK_DETAIL_DAT_01_02_REMARK1_SIZE;
        final String _HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE;
        final String _reportSpecialSize03_02;

        final String _gradeChiefTeacherName;

       /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;

        final DecimalFormat _df02 = new DecimalFormat("00");

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _disp = request.getParameter("DISP");
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _maxSemester = request.getParameter("MAX_SEMESTER");
            _date = request.getParameter("PRINT_DATE").replace('/', '-');
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = getGrade(request.getParameter("GRADE"));
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _imagePath = request.getParameter("IMAGEPATH");
            _printTeacherType = request.getParameter("PRT_TEACHER");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子

            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _certifSchoolJobName = getCertifSchoolDat(db2, "JOB_NAME");
            _d016Namespare1 = getNameMst(db2, _year, "D016", "01", "NAMESPARE1");

            _HREPORTREMARK_DAT_COMMUNICATION_SIZE = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_J"), "+", " ");                      //通信欄
            _HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE = null == request.getParameter("HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DETAIL_DAT_01_01_REMARK1_SIZE_J"), "+", " "); //学級活動
            _HREPORTREMARK_DETAIL_DAT_01_02_REMARK1_SIZE = null == request.getParameter("HREPORTREMARK_DETAIL_DAT_01_02_REMARK1_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DETAIL_DAT_01_02_REMARK1_SIZE_J"), "+", " "); //部活動名
            _HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE = null == request.getParameter("HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE_J") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DETAIL_DAT_02_01_REMARK1_SIZE_J"), "+", " "); //部活動の記録
            _reportSpecialSize03_02 = null == request.getParameter("reportSpecialSize03_02") ? "" : StringUtils.replace(request.getParameter("reportSpecialSize03_02"), "+", " "); // 観点

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            if ("2".equals(_printTeacherType)) {
            	_gradeChiefTeacherName = getChiefTeacherName(db2);
            } else {
            	_gradeChiefTeacherName = "";
            }
        }

        private String getGrade(final String grade) {
            if ("1".equals(_disp)) {
                return grade;
            } else {
                return null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            }
        }

        private String getDataSemester() {
            if ("9".equals(_semester)) {
                return _maxSemester;
            } else {
                return _semester;
            }
        }

        private String getRegdSemester() {
            if ("9".equals(_semester)) {
                return _ctrlSemester;
            } else {
                return _semester;
            }
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

        public String getImagePath() {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOLLOGO." + _extension;
            if (new java.io.File(path).exists()) {
                return path;
            }
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

        private String getChiefTeacherName(final DB2UDB db2) {
        	StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   T2.STAFFNAME ");
        	stb.append(" FROM ");
        	stb.append("   STAFF_DETAIL_MST T1 ");
        	stb.append("   LEFT JOIN STAFF_MST T2 ");
        	stb.append("     ON T2.STAFFCD = T1.STAFFCD ");
        	stb.append(" WHERE ");
        	stb.append("   T1.YEAR = '" + _year + "' ");
        	stb.append("   AND T1.STAFF_SEQ IN ('005', '006', '007') ");  //学年主任の条件
        	stb.append("   AND T1.FIELD1 = '0200' ");                     //学年主任の条件
        	stb.append("   AND T1.FIELD2 = '" + _grade + "' ");
        	stb.append(" ORDER BY ");
        	stb.append("   T1.STAFFCD ");
        	stb.append(" FETCH FIRST 1 ROWS ONLY ");  //重複していたら、STAFFCDの若い人を1人取る。

        	String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("STAFFNAME")) {
                    	retStr = rs.getString("STAFFNAME");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        	return retStr;
        }

    }
}

// eof
