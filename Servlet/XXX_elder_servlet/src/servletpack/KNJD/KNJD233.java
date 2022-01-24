// kanji=漢字
/*
 * $Id: 2a9675cae46fcd6e6fb4ccbc4972941cce9149cc $
 *
 * 作成日: 2007/07/02 17:19:09 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2007-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 2a9675cae46fcd6e6fb4ccbc4972941cce9149cc $
 */
public class KNJD233 {

    private static final String FORM_NAME = "KNJD233.frm";

    private static final String OUTPUT_PASS   = "1";
    private static final String OUTPUT_UNPASS = "2";
    private static final String OUTPUT_ALL    = "3";

    private static final String TITLE_PASS   = "卒業見込み";
    private static final String TITLE_UNPASS = "卒業見込みが立たない者";

    private static final int GAKUNEN = 0;
    private static final int TANI    = 1;

    private static final String DIV_HONKOU = "0";

    private static final String SOUGOU_CLASS = "90";

    private static final Log log = LogFactory.getLog(KNJD233.class);

    boolean _hasData;
    private Param _param;

    /**
     * KNJD.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
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
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (null != svf) {
                svf.VrQuit();
            }
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 70206 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }
    
    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _gengou;
        final String _semester;
        final String _semesterName;
        final String _grade;
        final String _outputDiv;
        final String _gradeInState;
        final String[] _hrClass;
        final String _date;
        final int _gradCredits;
        final int _taniOrGakunen;
        final String _outputOrder; // 1:出力順、2:前年度履修単位順、3:今年度修得単位
        /** 名称マスタ（学校等） */
        private String _z010Name1; //学校
        private String _z010NameSpare1; //record_score_dat使用フラグ
        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_year));
            _gengou = gengou + "年度";
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeInState = getGradeInState(db2, _year, _grade);
            _hrClass = request.getParameterValues("CLASS_SELECTED");
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            stb.append(KNJ_EditDate.h_format_JP(db2, new SimpleDateFormat("yyyy-MM-dd").format(date)));
            _date = stb.toString();

            // DBより取得
            final int[] credits = getGradCredits(db2, _year);
            _taniOrGakunen = credits[0];
            _gradCredits = credits[1];
            _outputOrder = request.getParameter("SYUTURYOKUJUN");
            _semesterName = getSemesterName(db2, _year, _semester, _grade);
            setNameMst(db2);
            log.debug("卒業単位=" + _gradCredits);
        }

        private String getGradeInState(final DB2UDB db2, final String year, final String grade) throws Exception {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT "
                + "    GRADE "
                + "FROM "
                + "    SCHREG_REGD_GDAT "
                + "WHERE "
                + "    YEAR = '" + year + "' "
                + "    AND GRADE < '" + grade + "' "
                + "    AND SCHOOL_KIND IN (SELECT "
                + "          SCHOOL_KIND "
                + "      FROM "
                + "          SCHREG_REGD_GDAT "
                + "      WHERE "
                + "          YEAR = '" + year + "' "
                + "          AND GRADE = '" + grade + "' "
                + "  ) "
                + "ORDER BY "
                + "    GRADE ";

            String comma = "";
            StringBuffer gradeInState = new StringBuffer();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    gradeInState.append(comma).append("'" + rs.getString("GRADE") + "'");
                    comma = ",";
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if ("".equals(comma)) {
                gradeInState.append("''");
            }
            //log.debug(" gradeInState = "  + gradeInState);
            return gradeInState.toString();
        }


        private int[] getGradCredits(final DB2UDB db2, final String year) throws Exception {
            final int[] rtnVal = {0, 0};

            final String sql = "SELECT "
                + "    SCHOOLDIV, "
                + "    VALUE(GRAD_CREDITS, 0) AS GRAD_CREDITS "
                + "FROM "
                + "    SCHOOL_MST "
                + "WHERE "
                + "    YEAR = '" + year + "' ";

            PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();

            try {
                if (rs.next()) {
                    rtnVal[0] = rs.getInt("SCHOOLDIV");
                    rtnVal[1] = rs.getInt("GRAD_CREDITS");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return rtnVal;
        }

        private void setNameMst(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlNameMst("Z010", "00"));
                rs = ps.executeQuery();
                if (rs.next()) {
                    _z010Name1 = rs.getString("NAME1");
                    _z010NameSpare1 = rs.getString("NAMESPARE1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("学校=" + _z010Name1 + "、成績テーブル=" + getRecordTable());
        }

        private String sqlNameMst(final String namecd1, final String namecd2) {
            final String sql = " SELECT "
                + "     * "
                + " FROM "
                + "     NAME_MST "
                + " WHERE "
                + "         NAMECD1 = '" + namecd1 + "' "
                + "     AND NAMECD2 = '" + namecd2 + "'";
            return sql;
        }

        private String getRecordTable() {
            return isUseRecordScoreDat() ? "RECORD_SCORE_DAT" : "RECORD_DAT";
        }

        /**
         * record_score_dat使用か?。
         * @return is not nullならtrue
         */
        private boolean isUseRecordScoreDat() {
            return _z010NameSpare1 != null;
        }
        
        private String getSemesterName(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String grade
                ) throws Exception {
            String rtnVal = "";
            
            if ("9".equals(semester)) {
                return rtnVal;
            }
            
            final String sql = "SELECT SEMESTERNAME FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE = '" + grade + "' ";
            PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            try {
                if (rs.next()) {
                    rtnVal = rs.getString("SEMESTERNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnVal;
        }
    }

    /** 印刷処理メイン */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final List printData = new ArrayList();
        for (int i = 0; i < _param._hrClass.length; i++) {
            log.debug(" hrClass = " + _param._hrClass[i]);
            printData.add(getStudentList(db2, _param, _param._hrClass[i]));
        }

        for (final Iterator it = printData.iterator(); it.hasNext();) {

            final List studentList = (List) it.next();
            
            final List gradPass = new ArrayList();
            final List gradUnPass = new ArrayList();

            for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
                final Student student = (Student) stit.next();
                if (_param._gradCredits <= student._totalCreditsInt) {
                    gradPass.add(student);
                } else {
                    gradUnPass.add(student);
                }
            }

            if (!_param._outputDiv.equals(OUTPUT_UNPASS)) {
                if (_hasData == false) {
                    _hasData = 0 < gradPass.size();
                }
                svf.VrSetForm(FORM_NAME, 4);
                printStudent(svf, gradPass, TITLE_PASS);
            }
            if (!_param._outputDiv.equals(OUTPUT_PASS)) {
                if (_hasData == false) {
                    _hasData = 0 < gradUnPass.size();
                }
                svf.VrSetForm(FORM_NAME, 4);
                printStudent(svf, gradUnPass, TITLE_UNPASS);
            }
            if (log.isDebugEnabled()) {
                debugData(gradPass, gradUnPass);
            }
        }
    }

    private void printStudent(final Vrw32alp svf, final List gradPass, final String title) {
        final List lineList = new ArrayList();
        for (int i = 0; i < gradPass.size(); i++) {
            final Student student = (Student) gradPass.get(i);
            
            lineList.add(new Line(student, (UnPassSubclass) student._subclassCreditList.get(0)));
            for (int j = 1; j < student._subclassCreditList.size(); j++) {
                lineList.add(new Line(null, (UnPassSubclass) student._subclassCreditList.get(j)));
            }
        }
        int studentCount = 1;
        for (int i = 0; i < lineList.size(); i++) {
            final Line pline = (Line) lineList.get(i);
            
            svf.VrsOut("NENDO", _param._gengou);
            svf.VrsOut("SEMESTER", _param._semesterName);
            svf.VrsOut("TITLE", title);
            svf.VrsOut("DATE", _param._date);

            if (null != pline._student) {
                Student student = pline._student;
                
                svf.VrsOut("NUMBER1", String.valueOf(studentCount++));
                svf.VrsOut("GRADE_CLASS_ATTENDNO1", student._homeRoomAtend);
                svf.VrsOut("SCHREGNO1", student._schregno);
                if (null != student._name) {
                    svf.VrsOut("NAME1_" + ((20 < KNJ_EditEdit.getMS932ByteLength(student._name)) ? "2" : "1"), student._name);
                }
                svf.VrsOut("BEF_CREDIT1", student._zensekiCredits);
                svf.VrsOut("PRE_CREDIT1", student._lastYearCredits);
                svf.VrsOut("C_CREDIT1", student._thisYearGetAddCredits);
                svf.VrsOut("A_CREDIT1", student._thisYearNinteiCredits);
                svf.VrsOut("TOTAL_C_CREDIT1", student._totalCredits);
            }
            if (null != pline._unPassSubclass) {
                final UnPassSubclass unPassSubclass = pline._unPassSubclass;
                svf.VrsOut("SUBCLASS1_" + ((40 < KNJ_EditEdit.getMS932ByteLength(unPassSubclass._name)) ? "2" : "1"), unPassSubclass._name);
                svf.VrsOut("PRE_N_CREDIT1", unPassSubclass._credit);
            }
            svf.VrEndRecord();
        }
        if (lineList.size() % 50 != 0) {
            for (int i = lineList.size() % 50; i < 50; i++) {
                svf.VrEndRecord();
            }
        }
    }

    private static void debugData(final List gradPass, List gradUnPass) {
        for (final Iterator itPass = gradPass.iterator(); itPass.hasNext();) {
            final Student student = (Student) itPass.next();
            log.debug(student);
        }
        for (final Iterator itUnPass = gradUnPass.iterator(); itUnPass.hasNext();) {
            final Student student = (Student) itUnPass.next();
            log.debug(student);
        }
    }

    private static List getStudentList(final DB2UDB db2, final Param param, final String hrClass) throws Exception {
        final List studentList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String studentSql = getStudentSql(hrClass, param);
            ps = db2.prepareStatement(studentSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String zensekiCredit = rs.getString("ZENSEKI_CREDIT");
                final String masterCreditLastYear = rs.getString("MASTERCREDIT_UNTIL_LAST_YEAR");
                final String masterCreditThisYear = rs.getString("MASTERCREDIT_THIS_YEAR");
                final String abroadCreditLastYear = rs.getString("ABROAD_CREDIT_UNTIL_LAST_YEAR");
                final String abroadCreditThisYear = rs.getString("ABROAD_CREDIT_THIS_YEAR");
                final String quolifiedCredit = rs.getString("CREDIT_THIS_YEAR");
                final Student student = new Student(
                        rs.getString("HOMEROOMATEND"),
                        rs.getString("SCHREGNO"),
                        rs.getString("NAME")
                );
                
                student._zensekiCredits = zensekiCredit;
                student._lastYearCredits = add(masterCreditLastYear, abroadCreditLastYear);
                student._thisYearGetAddCredits = add(masterCreditThisYear, abroadCreditThisYear);
                student._thisYearNinteiCredits = quolifiedCredit;
                student._totalCredits = add(zensekiCredit,
                        add(masterCreditLastYear, add(abroadCreditThisYear, add(masterCreditThisYear, add(abroadCreditLastYear, quolifiedCredit)))));

                student._totalCreditsInt = toInt(student._totalCredits);

                studentList.add(student);
            }
            DbUtils.closeQuietly(null, ps, rs);

            final String sql = getCreditsSql(param);
            ps = db2.prepareStatement(sql);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                
                final List unfinishCreditList = new ArrayList();
                String unfinishCreditUntilLastYearTotal = null;
                while (rs.next()) {
                    final String unfinishCreditUntilLastYear = rs.getString("UNFINISHCREDIT_UNTIL_LAST_YEAR");
                    unfinishCreditList.add(new UnPassSubclass(rs.getString("SUBCLASSNAME"), unfinishCreditUntilLastYear));
                    unfinishCreditUntilLastYearTotal = add(unfinishCreditUntilLastYearTotal, unfinishCreditUntilLastYear);
                }
                
                student._subclassCreditList.add(new UnPassSubclass("", unfinishCreditUntilLastYearTotal));
                student._subclassCreditList.addAll(unfinishCreditList);
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            DbUtils.closeQuietly(ps);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return studentList;
    }

    private static String add(final String s1, final String s2) {
        if (null == s1 && null == s2) {
            return null;
        }
        return String.valueOf(toInt(s1) + toInt(s2));
    }

    private static int toInt(final String s) {
        return null == s ? 0 : Integer.parseInt(s);
    }
    
    private static String getCreditsSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_INFO_LAST_YEAR AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T1.GRADE, ");
        if (GAKUNEN == param._taniOrGakunen) {
            stb.append("     MAX(T1.YEAR) AS YEAR ");
        } else {
            stb.append("     T1.YEAR ");
        }
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        if (GAKUNEN == param._taniOrGakunen) {
            stb.append("     INNER JOIN (SELECT ");
            stb.append("                        SCHREGNO, ");
            stb.append("                    GRADE, ");
            stb.append("                    MAX(YEAR || SEMESTER) AS YEAR_SEM ");
            stb.append("                FROM ");
            stb.append("                    SCHREG_REGD_DAT ");
            stb.append("                WHERE ");
            stb.append("                    YEAR < '" + param._year + "' ");
            stb.append("                        AND GRADE IN (" + param._gradeInState + ") ");
            stb.append("                GROUP BY ");
            stb.append("                    SCHREGNO, ");
            stb.append("                    GRADE) E1 ON T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("                AND T1.GRADE = E1.GRADE ");
            stb.append("                AND T1.YEAR || T1.SEMESTER = E1.YEAR_SEM ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR < '" + param._year + "' ");
        stb.append("     AND T1.SCHREGNO = ? ");
        if (GAKUNEN == param._taniOrGakunen) {
            stb.append("     AND T1.GRADE IN (" + param._gradeInState + ") ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        if (TANI == param._taniOrGakunen) {
            stb.append("     T1.YEAR, ");
        }
        stb.append("     T1.GRADE ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     T1.SUBCLASSCD, ");
        }
        stb.append("     L2.SUBCLASSNAME, ");
        stb.append("     SUM(VALUE(L1.CREDITS, 0)) AS UNFINISHCREDIT_UNTIL_LAST_YEAR ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append("     LEFT JOIN SCH_INFO_LAST_YEAR T2 ON T1.YEAR = T2.YEAR AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     LEFT JOIN CREDIT_MST L1 ON L1.YEAR = T1.YEAR ");
        stb.append("          AND L1.COURSECD = T2.COURSECD ");
        stb.append("          AND L1.MAJORCD = T2.MAJORCD ");
        stb.append("          AND L1.GRADE = T2.GRADE ");
        stb.append("          AND L1.COURSECODE = T2.COURSECODE ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("          AND L1.CLASSCD = T1.CLASSCD ");
            stb.append("          AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("          AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("          AND L2.CLASSCD = T1.CLASSCD ");
            stb.append("          AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD = '" + DIV_HONKOU + "' ");
        stb.append("     AND T1.YEAR < '" + param._year + "' ");
        stb.append("     AND T1.YEAR = T2.YEAR AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
        stb.append("     AND VALUE(T1.COMP_CREDIT, 0) = 0 ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD IN (SELECT ");
            stb.append("                               CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD ");
        } else {
            stb.append("     AND T1.SUBCLASSCD IN (SELECT ");
            stb.append("                               SUBCLASSCD ");
        }
        stb.append("                           FROM ");
        stb.append("                               SUBCLASS_MST ");
        stb.append("                           WHERE ");
        stb.append("                               ELECTDIV <> '1' ");
        stb.append("                          ) ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     , T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ");
        } else {
            stb.append("     , T1.SUBCLASSCD, ");
        }
        stb.append("     L2.SUBCLASSNAME ");
        stb.append(" ORDER BY ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
        } else {
            stb.append("     T1.SUBCLASSCD ");
        }
        return stb.toString();
    }

    private static String getStudentSql(final String hrClass, final Param param) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCH_INFO AS ( ");
        stb.append(" SELECT ");
        stb.append("     L1.HR_NAMEABBV || '-' || T1.ATTENDNO AS HOMEROOMATEND, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     L2.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ");
        stb.append("          AND L1.SEMESTER = T1.SEMESTER ");
        stb.append("          AND L1.GRADE = T1.GRADE AND L1.HR_CLASS = T1.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + hrClass + "' ");

        stb.append(" ), SCH_INFO_LAST_YEAR AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T1.GRADE, ");
        if (GAKUNEN == param._taniOrGakunen) {
            stb.append("     MAX(T1.YEAR) AS YEAR ");
        } else {
            stb.append("     T1.YEAR ");
        }
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        if (GAKUNEN == param._taniOrGakunen) {
            stb.append("     INNER JOIN (SELECT ");
            stb.append("                        SCHREGNO, ");
            stb.append("                    GRADE, ");
            stb.append("                    MAX(YEAR) AS YEAR ");
            stb.append("                FROM ");
            stb.append("                    SCHREG_REGD_DAT ");
            stb.append("                WHERE ");
            stb.append("                    YEAR < '" + param._year + "' ");
            stb.append("                        AND GRADE IN (" + param._gradeInState + ") ");
            stb.append("                GROUP BY ");
            stb.append("                    SCHREGNO, ");
            stb.append("                    GRADE) E1 ON T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("                AND T1.GRADE = E1.GRADE ");
            stb.append("                AND T1.YEAR = E1.YEAR ");
            
            stb.append("     INNER JOIN (SELECT ");
            stb.append("                        SCHREGNO, ");
            stb.append("                    GRADE, ");
            stb.append("                    YEAR, ");
            stb.append("                    MAX(SEMESTER) AS SEMESTER ");
            stb.append("                FROM ");
            stb.append("                    SCHREG_REGD_DAT ");
            stb.append("                WHERE ");
            stb.append("                    YEAR < '" + param._year + "' ");
            stb.append("                        AND GRADE IN (" + param._gradeInState + ") ");
            stb.append("                GROUP BY ");
            stb.append("                    SCHREGNO, ");
            stb.append("                    YEAR, ");
            stb.append("                    GRADE) E1_2 ON E1.SCHREGNO = E1_2.SCHREGNO ");
            stb.append("                AND E1.GRADE = E1_2.GRADE ");
            stb.append("                AND E1.YEAR = E1_2.YEAR ");
            stb.append("                AND T1.SEMESTER = E1_2.SEMESTER ");
        }        
        stb.append("     INNER JOIN (SELECT ");
        stb.append("                        SCHREGNO, ");
        stb.append("                    YEAR, ");
        stb.append("                    MAX(SEMESTER) AS SEMESTER ");
        stb.append("                FROM ");
        stb.append("                    SCHREG_REGD_DAT ");
        stb.append("                WHERE ");
        stb.append("                    YEAR < '" + param._year + "' ");
        stb.append("                GROUP BY ");
        stb.append("                    SCHREGNO, ");
        stb.append("                    YEAR) E2 ON T1.SCHREGNO = E2.SCHREGNO ");
        stb.append("                AND T1.YEAR = E2.YEAR ");
        stb.append("                AND T1.SEMESTER = E2.SEMESTER ");
        stb.append("     INNER JOIN (SELECT SCHREGNO FROM SCH_INFO GROUP BY SCHREGNO) E3 ON E3.SCHREGNO = T1.SCHREGNO ");
              stb.append(" WHERE ");
        stb.append("     T1.YEAR < '" + param._year + "' ");
        if (GAKUNEN == param._taniOrGakunen) {
            stb.append("     AND T1.GRADE IN (" + param._gradeInState + ") ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        if (TANI == param._taniOrGakunen) {
            stb.append("     T1.YEAR, ");
        }
        stb.append("     T1.GRADE ");

        stb.append(" ), STUDYREC_ZENSEKI AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     CASE WHEN SUM(T1.GET_CREDIT) IS NULL AND SUM(T1.ADD_CREDIT) IS NULL THEN NULL ");
        stb.append("      ELSE VALUE(SUM(T1.GET_CREDIT), 0) + VALUE(SUM(T1.ADD_CREDIT), 0) END AS ZENSEKI_CREDIT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD = '1' ");
        stb.append("     AND EXISTS (SELECT 'X' FROM SCH_INFO T3 WHERE T3.SCHREGNO = T1.SCHREGNO) ");
        stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");

        stb.append(" ), STUDYREC_THIS_YEAR AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     SUM(VALUE(T1.GET_CREDIT, 0) + VALUE(T1.ADD_CREDIT, 0)) AS MASTERCREDIT_UNTIL_LAST_YEAR ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append("     LEFT JOIN SCH_INFO_LAST_YEAR T2 ON T1.YEAR = T2.YEAR AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD = '" + DIV_HONKOU + "' ");
        stb.append("     AND T1.YEAR < '" + param._year + "' ");
        stb.append("     AND T1.YEAR || T1.SCHREGNO = T2.YEAR || T2.SCHREGNO ");
        stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");

        stb.append(" ), ABROAD_THIS AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(VALUE(ABROAD_CREDITS, 0)) AS ABROAD_CREDITS ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TRANSFER_DAT ");
        stb.append(" WHERE ");
        stb.append("   TRANSFERCD = '1' ");
        stb.append("     AND FISCALYEAR(TRANSFER_SDATE) = '" + param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");

        stb.append(" ), ABROAD_LAST AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(VALUE(ABROAD_CREDITS, 0)) AS ABROAD_CREDITS ");
        stb.append(" FROM ");
        stb.append("     SCHREG_TRANSFER_DAT ");
        stb.append(" WHERE ");
        stb.append("   TRANSFERCD = '1' ");
        stb.append("     AND FISCALYEAR(TRANSFER_SDATE) < '" + param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");

        stb.append(" ), T_RECORD AS ( ");
        if (param.isUseRecordScoreDat()) {
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(VALUE(GET_CREDIT, 0) + VALUE(ADD_CREDIT, 0)) AS MASTERCREDIT_THIS_YEAR ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '9' ");
            stb.append("     AND TESTKINDCD = '99' ");
            stb.append("     AND TESTITEMCD = '00' ");
            stb.append("     AND SCORE_DIV = '00' ");
            stb.append("     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO) ");
            stb.append("     AND substr(SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
        } else {
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(VALUE(GET_CREDIT, 0) + VALUE(ADD_CREDIT, 0)) AS MASTERCREDIT_THIS_YEAR ");
            stb.append(" FROM ");
            stb.append("     RECORD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO) ");
            stb.append("     AND substr(SUBCLASSCD, 1, 2) <= '" + SOUGOU_CLASS + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
        }
        stb.append(" ), T_QUALIFIED AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(VALUE(CREDITS, 0)) AS CREDIT_THIS_YEAR ");
        stb.append(" FROM ");
        stb.append("     SCHREG_QUALIFIED_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + param._year + "' ");
        stb.append("     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO) ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");

        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.HOMEROOMATEND, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");

        stb.append("     BSD.ZENSEKI_CREDIT, ");
        stb.append("     SSD.MASTERCREDIT_UNTIL_LAST_YEAR, ");
        stb.append("     ABL.ABROAD_CREDITS AS ABROAD_CREDIT_UNTIL_LAST_YEAR, ");
        stb.append("     REC.MASTERCREDIT_THIS_YEAR, ");
        stb.append("     ABT.ABROAD_CREDITS AS ABROAD_CREDIT_THIS_YEAR, ");
        stb.append("     SQD.CREDIT_THIS_YEAR ");

        stb.append(" FROM ");
        stb.append("     SCH_INFO T1 ");
        stb.append("     LEFT JOIN STUDYREC_THIS_YEAR SSD ON SSD.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN T_RECORD REC ON REC.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN T_QUALIFIED SQD ON SQD.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN ABROAD_THIS ABT ON ABT.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN ABROAD_LAST ABL ON ABL.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN STUDYREC_ZENSEKI BSD ON BSD.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        if ("2".equals(param._outputOrder)) {
            stb.append("     VALUE(SSD.MASTERCREDIT_UNTIL_LAST_YEAR, 0) + VALUE(ABL.ABROAD_CREDITS, 0) DESC, ");
        } else if ("3".equals(param._outputOrder)) {
            stb.append("     VALUE(REC.MASTERCREDIT_THIS_YEAR, 0) + VALUE(ABT.ABROAD_CREDITS, 0) DESC, ");
        }
        stb.append("     T1.HOMEROOMATEND ");

        return stb.toString();
    }

    /** 生徒 */
    private static class Student {
        final String _homeRoomAtend;
        final String _schregno;
        final String _name;
        final List _subclassCreditList = new ArrayList();
        String _zensekiCredits;
        String _lastYearCredits;
        String _thisYearGetAddCredits;
        String _thisYearNinteiCredits;
        String _totalCredits;
        int _totalCreditsInt;

        public Student(
                final String homeRoomAtend,
                final String schregno,
                final String name
        ) {
            _homeRoomAtend = homeRoomAtend;
            _schregno = schregno;
            _name = name;
        }

        public String toString() {
            final String info =
                               " 出席番号：" + _homeRoomAtend
                             + " 学籍番号：" + _schregno
                             + " 氏名：" + _name
                             + " 単位１：" + _lastYearCredits
                             + " 単位２：" + _thisYearGetAddCredits
                             + " 単位３：" + _thisYearNinteiCredits
                             + " 合計単位：" + _totalCredits + "\n";

            final StringBuffer stb = new StringBuffer();
            for (final Iterator it = _subclassCreditList.iterator(); it.hasNext();) {
                final UnPassSubclass unPassSubclass = (UnPassSubclass) it.next();
                stb.append(unPassSubclass + "\n");
            }
            return info + stb.toString();
        }

    }

    /** 未履修科目 */
    private static class UnPassSubclass {
        private final String _name;
        private final String _credit;
        UnPassSubclass(final String name, final String credit) {
            _name = name;
            _credit = credit;
        }

        public String toString() {
            return "科目名：" + _name + " 単位：" + _credit;
        }
    }
    
    private static class Line {
        final Student _student;
        final UnPassSubclass _unPassSubclass;
        Line(final Student student, final UnPassSubclass unPassSubclass) {
            _student = student;
            _unPassSubclass = unPassSubclass;
        }
    }
    
}
 // KNJD233

// eof
