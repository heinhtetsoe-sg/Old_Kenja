// kanji=漢字
/*
 * $Id: 36efbe602af3fb1c09e415cfaa527ac315e240d6 $
 *
 * 作成日: 2009/04/10 18:40:23 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.util.ArrayList;
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

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 36efbe602af3fb1c09e415cfaa527ac315e240d6 $
 */
public class KNJD233A {

    private static final Log log = LogFactory.getLog("KNJD233A.class");

    private static final String FORM_NAME = "KNJD233A.frm";
    private static final String PRINT_ALL = "1";
    private static final String PRINT_BAD = "2";
    private boolean _hasData;

    Param _param;
    /**
     * KNJW.classから呼ばれる処理
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
            init(response, svf);

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
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final String nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        final int maskCnt = 7;
        for (final Iterator iter = _param._hrList.iterator(); iter.hasNext();) {
            final HrClass hrClass = (HrClass) iter.next();

            svf.VrSetForm(FORM_NAME, 4);

            svf.VrsOut("NENDO", nendo);
            svf.VrsOut("HR_NAME", hrClass._name);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
            svf.VrsOut("GRD_CREDIT", String.valueOf(_param._grdCredit));

            for (final Iterator stIter = hrClass._students.iterator(); stIter.hasNext();) {
                final Student student = (Student) stIter.next();
                int requireCnt = 1;
                if (_param._outputDiv.equals(PRINT_BAD) && student._requiredErrList.size() == 0) {
                    continue;
                }
                if (student._requiredErrList.size() > 0) {
                    setPrintBase(svf, maskCnt, student, true);
                    for (final Iterator itRequire = student._requiredErrList.iterator(); itRequire.hasNext();) {
                        if (requireCnt > 12) {
                            svf.VrEndRecord();
                            requireCnt = 1;
                            setPrintBase(svf, maskCnt, student, false);
                        }
                        final String className = (String) itRequire.next();
                        svf.VrsOut("CLASSNAME" + requireCnt, className);
                        requireCnt++;
                    }
                } else {
                    setPrintBase(svf, maskCnt, student, true);
                    svf.VrEndRecord();
                }
                _hasData = true;
                if (requireCnt > 1) {
                    svf.VrEndRecord();
                }
            }
        }
    }

    private void setPrintBase(final Vrw32alp svf, final int maskCnt, final Student student, final boolean printName) {
        for (int i = 1; i <= maskCnt; i++) {
            svf.VrsOut("NO" + i, student._attendNo);
        }
        if (printName) {
            svf.VrsOut("ATTENDNO", student._attendNo);
            svf.VrsOut("NAME", student._name);
            svf.VrsOut("ATTENDNO", student._attendNo);
            svf.VrsOut("C_CREDIT", String.valueOf(student._cCredit));
            svf.VrsOut("G_CREDIT", String.valueOf(student._gCredit));
            svf.VrsOut("E_CREDIT", String.valueOf(student._eCredit));
            svf.VrsOut("GRDE_CREDIT", String.valueOf(student._gradeCredit));
            svf.VrsOut("CREDIT", getCreditMark(student));
        }
    }

    /**
     * @return
     */
    private String getCreditMark(final Student student) {
        if (student._gradeCredit< _param._grdCredit) return "×";
        if (student._eCredit > 0) return "△";
        return "〇";
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74240 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String[] _hrClass;
        private final String _outputDiv;
        private final String _loginDate;
        private final int _grdCredit;
        private List _hrList = new ArrayList();
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameterValues("CLASS_SELECTED");
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _hrList = getHrClass(db2, _year, _semester, _hrClass);
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            final String schoolSql = "SELECT * FROM SCHOOL_MST WHERE YEAR = '" + _year + "'";
            ResultSet rs = null;
            db2.query(schoolSql);
            rs = db2.getResultSet();
            if (rs.next()) {
                _grdCredit = rs.getInt("GRAD_CREDITS");
            } else {
                _grdCredit = 0;
            }

            for (final Iterator iter = _hrList.iterator(); iter.hasNext();) {
                final HrClass hrClass = (HrClass) iter.next();
                hrClass.setStudents(db2, _year, _semester, _useCurriculumcd);
            }
        }

        private List getHrClass(final DB2UDB db2, final String year, final String semester, final String[] hrClass) throws SQLException {
            final List rtnList = new ArrayList();
            final StringBuffer stb = new StringBuffer();
            stb.append("(");
            String sep = "";
            for (int i = 0; i < hrClass.length; i++) {
                final String gradeHrClass =hrClass[i];
                stb.append(sep + "'" + gradeHrClass + "'");
                sep = ",";
            }
            stb.append(")");
            final String hrClassSql = getHrClassSql(year, semester, stb.toString());
            ResultSet rs = null;
            try {
                db2.query(hrClassSql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClassCd = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrAbbv = rs.getString("HR_NAMEABBV");
                    final HrClass hr = new HrClass(grade, hrClassCd, hrName, hrAbbv);
                    rtnList.add(hr);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtnList;
        }

        private String getHrClassSql(final String year, final String semester, final String hrClass) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME, ");
            stb.append("     T1.HR_NAMEABBV ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + hrClass + " ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS ");

            return stb.toString();
        }
    }

    private class HrClass {
        final String _grade;
        final String _hrClass;
        final String _name;
        final String _abbv;
        List _students = new ArrayList();

        public HrClass(
                final String grade,
                final String hrclass,
                final String hrName,
                final String hrAbbv
        ) {
            _grade = grade;
            _hrClass = hrclass;
            _name = hrName;
            _abbv = hrAbbv;
        }

        public void setStudents(final DB2UDB db2, final String year, final String semester, final String useCurriculumcd) throws SQLException {
            final String studentSql = getStudentSql(year, semester, _grade, _hrClass, useCurriculumcd);
            final PreparedStatement ps = db2.prepareStatement(studentSql);
            final ResultSet rs = ps.executeQuery();
            try {
                while (rs.next()) {
                    final String schregNo = rs.getString("SCHREGNO");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final String name = rs.getString("NAME");
                    final int cCredit = rs.getInt("C_CREDIT");
                    final int gCredit = rs.getInt("G_CREDIT");
                    final int eCredit = rs.getInt("E_CREDIT");
                    final int gradeCredit = rs.getInt("GRADECREDIT");
                    final Student student = new Student(schregNo, attendNo, courseCd, majorCd, courseCode, name, cCredit, gCredit, eCredit, gradeCredit);
                    student.setPrintData(db2, useCurriculumcd);
                    _students.add(student);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getStudentSql(final String year, final String semester, final String grade, final String hrClass, final String useCurriculumcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSECODE, ");
            stb.append("     L1.NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' ");
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
            stb.append(" ), SCH_SUB AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.CHAIRCD, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("     L1.CLASSCD, ");
                stb.append("     L1.SCHOOL_KIND, ");
                stb.append("     L1.CURRICULUM_CD, ");
            }
            stb.append("     L1.SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT T1 ");
            stb.append("     LEFT JOIN CHAIR_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
            stb.append("          AND T1.CHAIRCD = L1.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND EXISTS( ");
            stb.append("             SELECT ");
            stb.append("                 'x' ");
            stb.append("             FROM ");
            stb.append("                 SCH_T E1 ");
            stb.append("             WHERE ");
            stb.append("                 T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("         ) ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.CHAIRCD, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("     L1.CLASSCD, ");
                stb.append("     L1.SCHOOL_KIND, ");
                stb.append("     L1.CURRICULUM_CD, ");
            }
            stb.append("     L1.SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), RECORD AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     SUM(L1.COMP_CREDIT) AS COMP_CREDIT, ");
            stb.append("     SUM(L1.GET_CREDIT) AS GET_CREDIT, ");
            stb.append("     SUM(L1.ADD_CREDIT) AS ADD_CREDIT ");
            stb.append(" FROM ");
            stb.append("     SCH_SUB T1 ");
            stb.append("     LEFT JOIN RECORD_DAT L1 ON T1.YEAR = L1.YEAR ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("          AND T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
                stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
            }
            stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND EXISTS( ");
            stb.append("             SELECT ");
            stb.append("                 'x' ");
            stb.append("             FROM ");
            stb.append("                 SCH_T E1 ");
            stb.append("             WHERE ");
            stb.append("                 T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("         ) ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD ");
            stb.append(" ), RECORD_GA AS ( ");
            stb.append("    SELECT ");
            stb.append("      SCHREGNO ");
            stb.append("      , SUM(GET_CREDIT) AS GET_CREDIT ");
            stb.append("      , SUM(ADD_CREDIT) AS ADD_CREDIT ");
            stb.append("    FROM ");
            stb.append("      RECORD ");
            stb.append("    GROUP BY ");
            stb.append("      SCHREGNO ");
            stb.append(" ), CREDIT AS ( ");
            
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     SUM(L2.CREDITS) AS C_CREDIT ");
            stb.append(" FROM ");
            stb.append("     SCH_SUB T1 ");
            stb.append("     LEFT JOIN SCH_T L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     LEFT JOIN CREDIT_MST L2 ON T1.YEAR = L2.YEAR ");
            stb.append("          AND L1.COURSECD = L2.COURSECD ");
            stb.append("          AND L1.MAJORCD = L2.MAJORCD ");
            stb.append("          AND L1.GRADE = L2.GRADE ");
            stb.append("          AND L1.COURSECODE = L2.COURSECODE ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("          AND T1.CLASSCD = L2.CLASSCD ");
                stb.append("          AND T1.SCHOOL_KIND = L2.SCHOOL_KIND ");
                stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
            }
            stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     EXISTS( ");
            stb.append("         SELECT ");
            stb.append("             'x' ");
            stb.append("         FROM ");
            stb.append("             RECORD E1 ");
            stb.append("         WHERE ");
            stb.append("             T1.SCHREGNO = E1.SCHREGNO ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("             AND T1.CLASSCD = E1.CLASSCD ");
                stb.append("             AND T1.SCHOOL_KIND = E1.SCHOOL_KIND ");
                stb.append("             AND T1.CURRICULUM_CD = E1.CURRICULUM_CD ");
            }
            stb.append("             AND T1.SUBCLASSCD = E1.SUBCLASSCD ");
            stb.append("     ) ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD ");
            stb.append(" ), REC_C AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(COMP_CREDIT) AS COMP_CREDIT, ");
            stb.append("     SUM(GET_CREDIT) AS GET_CREDIT, ");
            stb.append("     SUM(ADD_CREDIT) AS ADD_CREDIT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append(" ), CRE_C AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(C_CREDIT) AS C_CREDIT ");
            stb.append(" FROM ");
            stb.append("     CREDIT ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append(" ), STUDY_O AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(T1.COMP_CREDIT) AS COMP_CREDIT, ");
            stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
            stb.append("     SUM(T1.ADD_CREDIT) AS ADD_CREDIT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR < '" + year + "' ");
            stb.append("     AND EXISTS( ");
            stb.append("             SELECT ");
            stb.append("                 'x' ");
            stb.append("             FROM ");
            stb.append("                 SCH_T E1 ");
            stb.append("             WHERE ");
            stb.append("                 T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("         ) ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), QUALIFIED AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(T1.CREDITS) AS Q_CREDITS ");
            stb.append(" FROM ");
            stb.append("     SCHREG_QUALIFIED_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + year + "' ");
            stb.append("     AND EXISTS( ");
            stb.append("             SELECT ");
            stb.append("                 'x' ");
            stb.append("             FROM ");
            stb.append("                 SCH_T E1 ");
            stb.append("             WHERE ");
            stb.append("                 T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("         ) ");
            stb.append("     AND T1.CONDITION_DIV IN ('1', '2') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), TRANSFER_O AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(T1.ABROAD_CREDITS) AS T_CREDITS_O ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TRANSFER_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     EXISTS( ");
            stb.append("         SELECT ");
            stb.append("             'x' ");
            stb.append("         FROM ");
            stb.append("             SCH_T E1 ");
            stb.append("         WHERE ");
            stb.append("             T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("     ) ");
            stb.append("     AND T1.TRANSFERCD IN ('1') ");
            stb.append("     AND " + year + " >  ");
            stb.append("         CASE WHEN MONTH(T1.TRANSFER_SDATE) < 3 ");
            stb.append("              THEN YEAR(T1.TRANSFER_SDATE) - 1 ");
            stb.append("              ELSE YEAR(T1.TRANSFER_SDATE) ");
            stb.append("         END ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), TRANSFER_N AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(T1.ABROAD_CREDITS) AS T_CREDITS_N ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TRANSFER_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     EXISTS( ");
            stb.append("         SELECT ");
            stb.append("             'x' ");
            stb.append("         FROM ");
            stb.append("             SCH_T E1 ");
            stb.append("         WHERE ");
            stb.append("             T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("     ) ");
            stb.append("     AND T1.TRANSFERCD IN ('1') ");
            stb.append("     AND " + year + " =  ");
            stb.append("         CASE WHEN MONTH(T1.TRANSFER_SDATE) < 3 ");
            stb.append("              THEN YEAR(T1.TRANSFER_SDATE) - 1 ");
            stb.append("              ELSE YEAR(T1.TRANSFER_SDATE) ");
            stb.append("         END ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     VALUE (STUDY_O.COMP_CREDIT, 0) ");
            stb.append("         + VALUE (CRE_C.C_CREDIT, 0) ");
            stb.append("         + VALUE (QUALIFIED.Q_CREDITS, 0) ");
            stb.append("         + VALUE (TRANSFER_N.T_CREDITS_N, 0) ");
            stb.append("         + VALUE (TRANSFER_O.T_CREDITS_O, 0) AS C_CREDIT, ");
            stb.append("     VALUE(STUDY_O.GET_CREDIT, 0) ");
            stb.append("         + VALUE(STUDY_O.ADD_CREDIT, 0) ");
            stb.append("         + VALUE (RECORD_GA.GET_CREDIT, 0) ");
            stb.append("         + VALUE (RECORD_GA.ADD_CREDIT, 0) ");
            stb.append("         + VALUE (QUALIFIED.Q_CREDITS, 0) AS G_CREDIT, ");
            stb.append("     VALUE (CRE_C.C_CREDIT, 0) ");
            stb.append("         + VALUE (QUALIFIED.Q_CREDITS, 0) AS E_CREDIT, ");
            stb.append("     VALUE(STUDY_O.GET_CREDIT, 0) ");
            stb.append("         + VALUE(STUDY_O.ADD_CREDIT, 0) ");
            stb.append("         + VALUE (RECORD_GA.GET_CREDIT, 0) ");
            stb.append("         + VALUE (RECORD_GA.ADD_CREDIT, 0) ");
            stb.append("         + VALUE (QUALIFIED.Q_CREDITS, 0) ");
            stb.append("         + VALUE (CRE_C.C_CREDIT, 0) ");
            stb.append("         + VALUE (QUALIFIED.Q_CREDITS, 0)  AS GRADECREDIT ");
            stb.append(" FROM ");
            stb.append("     SCH_T T1 ");
            stb.append("     LEFT JOIN STUDY_O ON T1.SCHREGNO = STUDY_O.SCHREGNO ");
            stb.append("     LEFT JOIN QUALIFIED ON T1.SCHREGNO = QUALIFIED.SCHREGNO ");
            stb.append("     LEFT JOIN TRANSFER_N ON T1.SCHREGNO = TRANSFER_N.SCHREGNO ");
            stb.append("     LEFT JOIN TRANSFER_O ON T1.SCHREGNO = TRANSFER_O.SCHREGNO ");
            stb.append("     LEFT JOIN REC_C ON T1.SCHREGNO = REC_C.SCHREGNO ");
            stb.append("     LEFT JOIN CRE_C ON T1.SCHREGNO = CRE_C.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_GA ON T1.SCHREGNO = RECORD_GA.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");

            return stb.toString();
        }

        public String toString() {
            return "学年：" + _grade +
                    " クラス：" + _hrClass +
                    " 名称：" + _name;
        }
    }

    private class Student {
        final String _schregNo;
        final String _attendNo;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _name;
        final int _cCredit;
        final int _gCredit;
        final int _eCredit;
        final int _gradeCredit;
        List _requiredErrList = new ArrayList();

        public Student(
                final String schregNo,
                final String attendNo,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String name,
                final int cCredit,
                final int gCredit,
                final int eCredit,
                final int gradeCredit
        ) {
            _schregNo = schregNo;
            _attendNo = attendNo;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _name = name;
            _cCredit = cCredit;
            _gCredit = gCredit;
            _eCredit = eCredit;
            _gradeCredit = gradeCredit;
        }

        public void setPrintData(final DB2UDB db2, final String useCurriculumcd) throws SQLException {
            final String errQuery = getErrQuery(useCurriculumcd);
            ResultSet rs = null;
            try {
                db2.query(errQuery);
                rs = db2.getResultSet();
                while (rs.next()) {
                    _requiredErrList.add(rs.getString("CLASSNAME"));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
        }

        private String getErrQuery(final String useCurriculumcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("     T1.SCHOOL_KIND, ");
            }
            stb.append("     L1.CLASSNAME ");
            stb.append(" FROM ");
            stb.append("     CLASS_REQUIRED_DAT T1 ");
            stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("     AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append("     AND T1.ERR_FLG = '0' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD ");
            if ("1".equals(useCurriculumcd)) {
                stb.append("    , T1.SCHOOL_KIND ");
            }

            return stb.toString();
        }

        public String toString() {
            return _attendNo + "番：" + _name;
        }
    }
}
 // KNJWP133

// eof
