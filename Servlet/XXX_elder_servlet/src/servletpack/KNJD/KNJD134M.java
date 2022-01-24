/*
 * 作成日: 2021/02/26
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD134M {

    private static final Log log = LogFactory.getLog(KNJD134M.class);

    private boolean _hasData;

    private Param _param;

    private static final String SEMEALL = "9";

    private static final String SOUGOUTEKINA_TANKYUNO_ZIKAN = "90";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJD134M.frm", 1);

        final List<Student> studentList = getStudentList(db2);

        for (Student student : studentList) {
            svf.VrsOutn("NENDO", 1, _param._gengou + "　" + _param._gradeName);
            svf.VrsOutn("TITLE", 1, StringUtils.defaultString(student._generalSubclassName) + "　評価表");
            svf.VrsOutn("NAME",  1, _param._gradeName + "　" + student._hrClassName + "　" + student._name);
            if (student._isGeneralExists) {
                String[] generalEvaluation = KNJ_EditEdit.get_token(student._generalEvaluation, 76, 2);
                if (generalEvaluation != null) {
                    for (int i = 0; i < generalEvaluation.length; i++) {
                        svf.VrsOutn("REMARK" + (i + 1), 1, generalEvaluation[i]);
                    }
                }
            }

            svf.VrsOutn("NENDO", 2, _param._gengou + "　" + _param._gradeName);
            svf.VrsOutn("TITLE", 2, "道徳　評価表");
            svf.VrsOutn("NAME",  2, _param._gradeName + "　" + student._hrClassName + "　" + student._name);
            String[] moralityEvaluation = KNJ_EditEdit.get_token(student._moralityEvaluation, 76, 2);
            if (moralityEvaluation != null) {
                if (moralityEvaluation != null) {
                    for (int i = 0; i < moralityEvaluation.length; i++) {
                        svf.VrsOutn("REMARK" + (i + 1), 2, moralityEvaluation[i]);
                    }
                }
            }
            _hasData = true;
            svf.VrEndPage();
        }
    }

    private List<Student> getStudentList(final DB2UDB db2) {
        final List<Student> studentList = new ArrayList<Student>();

        PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql = getStudentSql();
        log.debug(" sql =" + sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final boolean isGeneralExists = "1".equals(rs.getString("IS_GENERAL_EXISTS")) ? true : false;
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String name = rs.getString("NAME");
                final String gradeCd = rs.getString("GRADE_CD");
                final String hrClassName = rs.getString("HR_CLASS_NAME1");
                final String totalStudyVal= rs.getString("TOTALSTUDYVAL");
                final String remark1 = rs.getString("REMARK1");

                final Student student = new Student(isGeneralExists, subclassName, name, gradeCd, hrClassName, totalStudyVal, remark1);

                studentList.add(student);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MIN_SUBCLASS AS ( ");
        stb.append("     SELECT ");
        stb.append("         CLASSCD, ");
        stb.append("         SCHOOL_KIND, ");
        stb.append("         CURRICULUM_CD, ");
        stb.append("         MIN(SUBCLASSCD) AS SUBCLASSCD ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_MST ");
        stb.append("     GROUP BY ");
        stb.append("         CLASSCD, ");
        stb.append("         SCHOOL_KIND, ");
        stb.append("         CURRICULUM_CD ");
        stb.append(" ), ");
        stb.append(" SUBCLASS AS ( ");
        stb.append("     SELECT ");
        stb.append("         MST.CLASSCD, ");
        stb.append("         MST.SCHOOL_KIND, ");
        stb.append("         MST.CURRICULUM_CD, ");
        stb.append("         MST.SUBCLASSCD, ");
        stb.append("         MST.SUBCLASSNAME ");
        stb.append("     FROM ");
        stb.append("         SUBCLASS_MST MST ");
        stb.append("         INNER JOIN MIN_SUBCLASS MIN_MST ON ");
        stb.append("                    MIN_MST.CLASSCD       = MST.CLASSCD ");
        stb.append("                AND MIN_MST.SCHOOL_KIND   = MST.SCHOOL_KIND ");
        stb.append("                AND MIN_MST.CURRICULUM_CD = MST.CURRICULUM_CD ");
        stb.append("                AND MIN_MST.SUBCLASSCD    = MST.SUBCLASSCD ");
        stb.append(" ), ");
        stb.append(" CHAIR_SUBCLASS AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         STD.YEAR, ");
        stb.append("         STD.SCHREGNO, ");
        stb.append("         SUBCLASS.SUBCLASSNAME ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STD_DAT STD ");
        stb.append("         INNER JOIN CHAIR_DAT DAT ON ");
        stb.append("               DAT.YEAR     = STD.YEAR ");
        stb.append("           AND DAT.SEMESTER = STD.SEMESTER ");
        stb.append("           AND DAT.CHAIRCD  = STD.CHAIRCD ");
        stb.append("           AND DAT.CLASSCD = '" + SOUGOUTEKINA_TANKYUNO_ZIKAN + "' ");
        stb.append("         INNER JOIN SUBCLASS ON ");
        stb.append("               SUBCLASS.CLASSCD       = DAT.CLASSCD ");
        stb.append("           AND SUBCLASS.SCHOOL_KIND   = DAT.SCHOOL_KIND ");
        stb.append("           AND SUBCLASS.CURRICULUM_CD = DAT.CURRICULUM_CD ");
        stb.append("           AND SUBCLASS.SUBCLASSCD    = DAT.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         STD.APPDATE BETWEEN DATE('" + _param._semeAllSDate + "') AND DATE('" + _param._semeAllEDate + "') ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     CASE WHEN CHAIR_SUBCLASS.SCHREGNO IS NULL THEN '0' ELSE '1' END AS IS_GENERAL_EXISTS, ");
        stb.append("     CHAIR_SUBCLASS.SUBCLASSNAME, ");
        stb.append("     GDAT.GRADE_CD, ");
        stb.append("     HDAT.HR_CLASS_NAME1, ");
        stb.append("     BASE.NAME, ");
        stb.append("     HTRR.TOTALSTUDYVAL, ");
        stb.append("     HTRR_D00.REMARK1 ");

        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT DAT ");

        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON ");
        stb.append("               BASE.SCHREGNO = DAT.SCHREGNO ");

        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON ");
        stb.append("               HDAT.YEAR     = DAT.YEAR ");
        stb.append("           AND HDAT.SEMESTER = DAT.SEMESTER ");
        stb.append("           AND HDAT.GRADE    = DAT.GRADE ");
        stb.append("           AND HDAT.HR_CLASS = DAT.HR_CLASS ");

        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON ");
        stb.append("               GDAT.YEAR  = HDAT.YEAR ");
        stb.append("           AND GDAT.GRADE = HDAT.GRADE ");

        stb.append("     LEFT JOIN CHAIR_SUBCLASS ON ");
        stb.append("               CHAIR_SUBCLASS.YEAR        = DAT.YEAR ");
        stb.append("           AND CHAIR_SUBCLASS.SCHREGNO    = DAT.SCHREGNO ");

        stb.append("     LEFT JOIN HTRAINREMARK_DAT HTRR ON ");
        stb.append("               HTRR.YEAR     = DAT.YEAR ");
        stb.append("           AND HTRR.SCHREGNO = DAT.SCHREGNO ");

        stb.append("     LEFT JOIN HTRAINREMARK_DETAIL2_DAT HTRR_D00 ON ");
        stb.append("               HTRR_D00.YEAR       = DAT.YEAR ");
        stb.append("           AND HTRR_D00.SCHREGNO   = DAT.SCHREGNO ");
        stb.append("           AND HTRR_D00.HTRAIN_SEQ = '004' ");

        stb.append(" WHERE ");
        stb.append("     DAT.YEAR     = '" + _param._ctrlYear     + "' AND ");
        stb.append("     DAT.SEMESTER = '" + _param._ctrlSemester + "' AND ");
        stb.append(SQLUtils.whereIn(true, "DAT.SCHREGNO", _param._schregnos));
        stb.append(" ORDER BY ");
        stb.append("     DAT.GRADE, ");
        stb.append("     DAT.HR_CLASS, ");
        stb.append("     DAT.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final boolean _isGeneralExists;
        final String _generalSubclassName;
        final String _name;
        final String _gradeCd;
        final String _hrClassName;
        final String _generalEvaluation;
        final String _moralityEvaluation;

        private Student(
            final boolean isGeneralExists,
            final String generalSubclassName,
            final String name,
            final String gradeCd,
            final String hrClassName,
            final String generalEvaluation,
            final String moralityEvaluation
        ) {
            _isGeneralExists     = isGeneralExists;
            _name                = name;
            _gradeCd             = gradeCd;
            _hrClassName         = hrClassName;
            _generalSubclassName = generalSubclassName;
            _generalEvaluation   = generalEvaluation;
            _moralityEvaluation  = moralityEvaluation;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _gradeHrClass;
        final String[] _categorySelected;
        final String _grade;
        final String _hrClass;
        final String[] _schregnos;
        final String _gengou;
        final Map<String, String> _gradeRow;
        final String _schoolKind;
        final String _schoolKindName;
        final String _gradeCd;
        final String _gradeName;
        final String _hrClassName;
        final String _semeAllSDate;
        final String _semeAllEDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear         = request.getParameter("CTRL_YEAR");
            _ctrlSemester     = request.getParameter("CTRL_SEMESTER");
            _gradeHrClass     = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _grade            = _gradeHrClass.substring(0, 2);
            _hrClass          = _gradeHrClass.substring(2, 3);
            _schregnos        = getSchregnos();
            _gengou           = StringUtils.defaultString(KNJ_EditDate.gengou(db2, Integer.parseInt(_ctrlYear))) + "年度";
            _gradeRow         = getGradeRow(db2);
            _schoolKind       = _gradeRow.get("SCHOOL_KIND");
            _schoolKindName   = "J".equals(_schoolKind) ? "中" : "H".equals(_schoolKind) ? "高" : "";
            _gradeCd          = _gradeRow.get("GRADE_CD");
            _gradeName        = _schoolKindName + Integer.parseInt(_gradeCd);
            _hrClassName      = getHrClassName(db2);
            Map<String, String> semeAllInfo = getSemeAllInfo(db2);
            _semeAllSDate     = semeAllInfo.get("SDATE");
            _semeAllEDate     = semeAllInfo.get("EDATE");
        }

        private String[] getSchregnos() {
            List<String> rtn = new ArrayList<String>();

            for (String row : _categorySelected) {
                String[] sprow = StringUtils.split(row, "-");
                rtn.add(sprow[0]);
            }

            return rtn.toArray(new String[]{});
        }

        private Map getGradeRow(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE_CD, ");
            stb.append("     SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR      = '" + _ctrlYear + "' ");
            stb.append("     AND GRADE = '" + _grade    + "' ");

            Map<String, String> _gradeRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));
            return _gradeRow;
        }

        private String getHrClassName(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     HR_CLASS_NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR     = '" + _ctrlYear     + "' ");
            stb.append(" AND SEMESTER = '" + _ctrlSemester + "' ");
            stb.append(" AND GRADE    = '" + _grade        + "' ");
            stb.append(" AND HR_CLASS = '" + _hrClass      + "' ");

            Map<String, String> _hrClassRow = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));
            return _hrClassRow.get("HR_CLASS_NAME1");
        }

        private Map<String, String> getSemeAllInfo(final DB2UDB db2) {
            final String sql = "SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + SEMEALL + "'";

            Map<String, String> semeAllInfo = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            return semeAllInfo;
        }
    }
}

// eof
