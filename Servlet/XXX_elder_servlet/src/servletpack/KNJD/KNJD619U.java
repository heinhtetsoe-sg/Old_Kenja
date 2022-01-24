/*
 * $Id: 29346f214896ebad986e377328c0548b0a55e083 $
 *
 * 作成日: 2017/08/16
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD619U {

    private static final Log log = LogFactory.getLog(KNJD619U.class);

    private static final String TEST_HYOUTEI = "9990009";
    private static final String TEST_GOUKEI = "9990008";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final List gradeCourseList = getGradeCourseList(db2);
        for (Iterator itGradeCourse = gradeCourseList.iterator(); itGradeCourse.hasNext();) {
            final GradeCourse gradeCourse = (GradeCourse) itGradeCourse.next();
            for (Iterator itSubclass = gradeCourse._subclassMap.keySet().iterator(); itSubclass.hasNext();) {
                svf.VrSetForm("KNJD619U.frm", 1);
                final String subclassKey = (String) itSubclass.next();
                final Subclass subclass = (Subclass) gradeCourse._subclassMap.get(subclassKey);
                setTitle(svf, gradeCourse, subclass);
                printAssess(svf, subclass);
                printStudent(svf, gradeCourse, subclass);
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private void printStudent(final Vrw32alp svf, final GradeCourse gradeCourse, final Subclass subclass) {
        final int maxTableCnt = 4;
        final int maxLineCnt = 50;
        int tableCnt = 1;
        int lineCnt = 1;
        for (Iterator itStudent = subclass._studentList.iterator(); itStudent.hasNext();) {
            if (lineCnt > maxLineCnt) {
                tableCnt++;
                lineCnt = 1;
            }
            if (tableCnt > maxTableCnt) {
                svf.VrEndPage();
                tableCnt = 1;
                lineCnt = 1;
                setTitle(svf, gradeCourse, subclass);
                printAssess(svf, subclass);
            }
            final Student student = (Student) itStudent.next();
            svf.VrsOutn("NO" + tableCnt, lineCnt, student._rank);
            final String nameField = getMS932ByteLength(student._name) > 30 ? "_3" : getMS932ByteLength(student._name) > 18 ? "_2" : "_1";
            svf.VrsOutn("NAME" + tableCnt + nameField, lineCnt, student._name);
            svf.VrsOutn("HR_NAME" + tableCnt, lineCnt, student._hrName + "-" + Integer.parseInt(student._attendno));
            svf.VrsOutn("DIV" + tableCnt, lineCnt, student._hyoutei);
            svf.VrsOutn("TOTAL" + tableCnt, lineCnt, null == student._goukei || "".equals(student._goukei) ? "欠席" : student._goukei);
            lineCnt++;
        }
    }

    private void printAssess(final Vrw32alp svf, final Subclass subclass) {
        int assessLine = 1;
        svf.VrsOut("DIV_NAME_ALL", _param._assessTitleJP);
        for (Iterator itAssess = subclass._assessMstList.iterator(); itAssess.hasNext();) {
            final AssessMst assessMst = (AssessMst) itAssess.next();
            svf.VrsOutn("DIV_ALL", assessLine, assessMst._assessMark);
            svf.VrsOutn("NUM", assessLine, String.valueOf(assessMst._cnt));
            svf.VrsOutn("F_SCORE", assessLine, assessMst._assessLow);
            svf.VrsOutn("T_SCORE", assessLine, assessMst._assessHigh);
            svf.VrsOutn("AVE", assessLine, assessMst.getAvg());
            assessLine++;
        }
    }

    private void setTitle(final Vrw32alp svf, final GradeCourse gradeCourse, final Subclass subclass) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._ctrlYear + "-04-01") + "度" + "　" + gradeCourse._gradeName + "　" + subclass._subclassName + "　" + _param._assessTitleJP + "一覧表");
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private List getGradeCourseList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();

        for (int i = 0; i < _param._categorySelected.length; i++) {
            final String paramGradeCourse = _param._categorySelected[i];
            final String[] gradeCourseArray = StringUtils.split(paramGradeCourse, "_");
            final String gradeCourseSql = getCourseSql(gradeCourseArray);
            PreparedStatement psCourse = null;
            ResultSet rsCourse = null;
            psCourse = db2.prepareStatement(gradeCourseSql);
            rsCourse = psCourse.executeQuery();
            rsCourse.next();
            final GradeCourse gradeCourse = new GradeCourse(gradeCourseArray[0], rsCourse.getString("GRADE_NAME1"), gradeCourseArray[1], gradeCourseArray[2], gradeCourseArray[3], rsCourse.getString("MAJORNAME"), rsCourse.getString("COURSECODENAME"));

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(gradeCourseArray);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String attendno = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String hrName = rs.getString("HR_NAME");
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String rank = rs.getString("COURSE_RANK");
                    final String hyoutei = rs.getString("HYOUTEI");
                    final String goukei = rs.getString("GOUKEI");
                    final Student student = new Student(schregno, grade, hrClass, attendno, name, hrName, rank, hyoutei, goukei);

                    final String subclassKey = classCd + schoolKind + curriculumCd + subclassCd;
                    Subclass subclass = null;
                    if (gradeCourse._subclassMap.containsKey(subclassKey)) {
                        subclass = (Subclass) gradeCourse._subclassMap.get(subclassKey);
                    } else {
                        subclass = new Subclass(db2, classCd, schoolKind, curriculumCd, subclassCd, subclassName);
                        gradeCourse._subclassMap.put(subclassKey, subclass);
                    }
                    subclass._studentList.add(student);
                    subclass.setAssessData(hyoutei, goukei);
                }
                retList.add(gradeCourse);

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, psCourse, rsCourse);
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return retList;
    }

    private String getCourseSql(final String[] gradeCourseArray) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GDAT.GRADE_NAME1, ");
        stb.append("     MAJOR.COURSECD, ");
        stb.append("     MAJOR.MAJORCD, ");
        stb.append("     COURSECODE.COURSECODE, ");
        stb.append("     VALUE(MAJOR.MAJORNAME, '') AS MAJORNAME, ");
        stb.append("     VALUE(COURSECODE.COURSECODENAME, '') AS COURSECODENAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_GDAT GDAT, ");
        stb.append("     MAJOR_MST MAJOR, ");
        stb.append("     COURSECODE_MST COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     GDAT.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND GDAT.GRADE = '" + gradeCourseArray[0] + "' ");
        stb.append("     AND MAJOR.COURSECD = '" + gradeCourseArray[1] + "' ");
        stb.append("     AND MAJOR.MAJORCD = '" + gradeCourseArray[2] + "' ");
        stb.append("     AND COURSECODE.COURSECODE = '" + gradeCourseArray[3] + "' ");
        return stb.toString();
    }

    private String getStudentSql(final String[] gradeCourseArray) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     RECORD09.CLASSCD, ");
        stb.append("     RECORD09.SCHOOL_KIND, ");
        stb.append("     RECORD09.CURRICULUM_CD, ");
        stb.append("     RECORD09.SUBCLASSCD, ");
        stb.append("     SUBCLASS.SUBCLASSNAME, ");
        stb.append("     RECORD08.COURSE_RANK, ");
        stb.append("     VALUE(RECORD09.SCORE, 0) AS HYOUTEI, ");
        stb.append("     RECORD08.SCORE AS GOUKEI ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT RECORD09 ON REGD.YEAR = RECORD09.YEAR ");
        stb.append("          AND RECORD09.SEMESTER || RECORD09.TESTKINDCD || RECORD09.TESTITEMCD || RECORD09.SCORE_DIV = '" + TEST_HYOUTEI + "' ");
        stb.append("          AND REGD.SCHREGNO = RECORD09.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT RECORD08 ON REGD.YEAR = RECORD08.YEAR ");
        stb.append("          AND RECORD08.SEMESTER || RECORD08.TESTKINDCD || RECORD08.TESTITEMCD || RECORD08.SCORE_DIV = '" + TEST_GOUKEI + "' ");
        stb.append("          AND RECORD09.CLASSCD = RECORD08.CLASSCD ");
        stb.append("          AND RECORD09.SCHOOL_KIND = RECORD08.SCHOOL_KIND ");
        stb.append("          AND RECORD09.CURRICULUM_CD = RECORD08.CURRICULUM_CD ");
        stb.append("          AND RECORD09.SUBCLASSCD = RECORD08.SUBCLASSCD ");
        stb.append("          AND RECORD09.SCHREGNO = RECORD08.SCHREGNO ");
        stb.append("     INNER JOIN SUBCLASS_MST SUBCLASS ON RECORD09.CLASSCD = SUBCLASS.CLASSCD ");
        stb.append("          AND RECORD09.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ");
        stb.append("          AND RECORD09.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
        stb.append("          AND RECORD09.SUBCLASSCD = SUBCLASS.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND REGD.GRADE = '" + gradeCourseArray[0] + "' ");
        stb.append("     AND REGD.COURSECD = '" + gradeCourseArray[1] + "' ");
        stb.append("     AND REGD.MAJORCD = '" + gradeCourseArray[2] + "' ");
        stb.append("     AND REGD.COURSECODE = '" + gradeCourseArray[3] + "' ");
        stb.append(" ORDER BY ");
        stb.append("     RECORD09.CLASSCD, ");
        stb.append("     RECORD09.SCHOOL_KIND, ");
        stb.append("     RECORD09.CURRICULUM_CD, ");
        stb.append("     RECORD09.SUBCLASSCD, ");
        stb.append("     RECORD09.COURSE_RANK, ");
        stb.append("     VALUE(RECORD08.SCORE, -1) DESC, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    /** コースクラス */
    private class GradeCourse {
        final String _grade;
        final String _gradeName;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _majorName;
        final String _coursecodeName;
        Map _subclassMap = new TreeMap();
        public GradeCourse(
                final String grade,
                final String gradeName,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final String majorName,
                final String coursecodeName
        ) {
            _grade = grade;
            _gradeName = gradeName;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _majorName = majorName;
            _coursecodeName = coursecodeName;
        }
    }

    private class Subclass {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        List _studentList = new ArrayList();
        final List _assessMstList;
        public Subclass(
                final DB2UDB db2,
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclassCd,
                final String subclassName
                ) throws SQLException {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _assessMstList = _param.getAssessMstList(db2);
        }

        public void setAssessData(final String assessLevel, final String score) {
            if ("0".equals(assessLevel) || null == score) {
                return;
            }
            for (Iterator iterator = _assessMstList.iterator(); iterator.hasNext();) {
                final AssessMst assessMst = (AssessMst) iterator.next();
                if (assessMst._assessLevel.equals(assessLevel)) {
                    assessMst._cnt++;
                    assessMst._totalScore += Integer.parseInt(score);
                    if ("".equals(assessMst._assessHigh)) {
                        assessMst._assessHigh = score;
                    }
                    assessMst._assessLow = score;
                }
            }
        }

    }

    /** 生徒クラス */
    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _name;
        final String _hrName;
        final String _rank;
        final String _hyoutei;
        final String _goukei;
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String name,
                final String hrName,
                final String rank,
                final String hyoutei,
                final String goukei
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _name = name;
            _hrName = hrName;
            _rank = rank;
            _hyoutei = hyoutei;
            _goukei = goukei;
        }
    }

    private class AssessMst {
        final String _assessLevel;
        final String _assessMark;
        final String _assessLowMst;
        final String _assessHighMst;
        int _cnt = 0;
        int _totalScore = 0;
        String _assessLow = "";
        String _assessHigh = "";
        public AssessMst(
                final String assessLevel,
                final String assessMark,
                final String assessLow,
                final String assessHigh
        ) throws SQLException {
            _assessLevel = assessLevel;
            _assessMark = assessMark;
            _assessLowMst = assessLow;
            _assessHighMst = assessHigh;
        }

        public String getAvg() {
            if (_cnt == 0) {
                return "";
            }
            BigDecimal bdScore = new BigDecimal(Double.parseDouble(String.valueOf(_totalScore)) / Double.parseDouble(String.valueOf(_cnt)));

            return String.valueOf(bdScore.setScale(1, BigDecimal.ROUND_HALF_UP));
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65987 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String[] _categorySelected;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _usecurriculumcd;
        final String _usePrgSchoolkind;
        final String _selectschoolkind;
        final String _useschoolKindfield;
        final String _paraSchoolkind;
        final String _schoolcd;
        final String _printLogStaffcd;
        final List _assessMstList;
        final String _assessTitleJP;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _selectschoolkind = request.getParameter("selectSchoolKind");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _paraSchoolkind = request.getParameter("SCHOOLKIND");
            _schoolcd = request.getParameter("SCHOOLCD");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _assessMstList = getAssessMstList(db2);
            _assessTitleJP = KNJ_EditEdit.convertKansuuji(_assessMstList.size()) + "段階";
        }

        private List getAssessMstList(final DB2UDB db2) throws SQLException {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getAssessMst();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String assessLevel = rs.getString("ASSESSLEVEL");
                    final String assessMark = rs.getString("ASSESSMARK");
                    final String assessLow = rs.getString("ASSESSLOW");
                    final String assessHigh = rs.getString("ASSESSHIGH");

                    final AssessMst assessMst = new AssessMst(assessLevel, assessMark, assessLow, assessHigh);
                    retList.add(assessMst);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }

        private String getAssessMst() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     ASSESSLEVEL, ");
            stb.append("     ASSESSMARK, ");
            stb.append("     ASSESSLOW, ");
            stb.append("     ASSESSHIGH ");
            stb.append(" FROM ");
            stb.append("     ASSESS_MST ");
            stb.append(" WHERE ");
            stb.append("     ASSESSCD = '3' ");
            stb.append(" ORDER BY ");
            stb.append("     ASSESSLEVEL DESC ");
            return stb.toString();
        }

        private String[] getSchoolKind(final DB2UDB db2, final String grade) throws SQLException {
            String[] retStr = {"", "", ""};
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSchoolKind(grade);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr[0] = rs.getString("GRADE_NAME1");
                    retStr[1] = rs.getString("SCHOOL_KIND");
                    retStr[2] = rs.getString("ABBV1");
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSchoolKind(final String grade) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GDAT.GRADE_NAME1, ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     A023.ABBV1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT GDAT ");
            stb.append("     LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
            stb.append("          AND GDAT.SCHOOL_KIND = A023.NAME1 ");
            stb.append(" WHERE ");
            stb.append("     GDAT.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND GDAT.GRADE = '" + grade + "' ");
            return stb.toString();
        }

    }
}

// eof

