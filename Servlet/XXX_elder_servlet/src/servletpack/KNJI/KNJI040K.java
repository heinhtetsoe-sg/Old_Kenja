/*
 * $Id: 7ce089242d8acf8ddaf83a8218227634a861f7aa $
 *
 * 作成日: 2017/12/14
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJI;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJI040K {

    private static final Log log = LogFactory.getLog(KNJI040K.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJI040K.frm", 1);
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            svf.VrsOut("TITLE", "給付型奨学生選抜資料");
            svf.VrsOut("NAME", student._year + "年度 " + student._grade + student._hrClass + " " + student._attendNo + "　　" + student._name + "　　" + student._sex);
            svf.VrsOutn("ITEM4", 1, "学年成績");
            svf.VrsOutn("ITEM4", 2, "評定");

            int totalScore = 0;
            int totalAssess = 0;
            int totalCnt = 0;
            int yearCnt = 1;
            for (Iterator itYear = student._yearList.iterator(); itYear.hasNext();) {
                final YearData yearData = (YearData) itYear.next();
                svf.VrsOutn("ITEM1", yearCnt, yearData._year + " 科目(類型)");
                svf.VrsOutn("ITEM2", yearCnt, yearData._year + " 学年成績");
                svf.VrsOutn("ITEM3", yearCnt, yearData._year + " 評定");

                int subclassCnt = 1;
                for (Iterator itSubclass = yearData._subclassList.iterator(); itSubclass.hasNext();) {
                    final SubclassData subclassData = (SubclassData) itSubclass.next();
                    svf.VrsOutn("SUBCLASS_NAME" + subclassCnt, yearCnt, subclassData._subclassName);
                    svf.VrsOutn("SCORE" + subclassCnt, yearCnt, subclassData._gradeRecord);
                    svf.VrsOutn("VALUE" + subclassCnt, yearCnt, subclassData._assess);
                    totalScore += null != subclassData._gradeRecord && !"".equals(subclassData._gradeRecord) ? Integer.parseInt(subclassData._gradeRecord) : 0;
                    totalAssess += null != subclassData._assess && !"".equals(subclassData._assess) ? Integer.parseInt(subclassData._assess) : 0;
                    totalCnt++;
                    subclassCnt++;
                }

                int attendCnt = 1;
                int totalSick = 0;
                int totalLate = 0;
                int totalEaly = 0;
                for (Iterator itAttend = yearData._attendList.iterator(); itAttend.hasNext();) {
                    final AttendData attendData = (AttendData) itAttend.next();
                    svf.VrsOutn("YEAR" + yearCnt, attendCnt, yearData._year);
                    svf.VrsOutn("MONTH" + yearCnt, attendCnt, attendData._month);
                    svf.VrsOutn("ABSENCE" + yearCnt, attendCnt, attendData._sick);
                    svf.VrsOutn("LATE" + yearCnt, attendCnt, attendData._late);
                    svf.VrsOutn("EARLY" + yearCnt, attendCnt, attendData._ealy);
                    totalSick += null != attendData._sick && !"".equals(attendData._sick) ? Integer.parseInt(attendData._sick) : 0;
                    totalLate += null != attendData._late && !"".equals(attendData._late) ? Integer.parseInt(attendData._late) : 0;
                    totalEaly += null != attendData._ealy && !"".equals(attendData._ealy) ? Integer.parseInt(attendData._ealy) : 0;
                    svf.VrsOutn("ABSENCE" + yearCnt, 13, String.valueOf(totalSick));
                    svf.VrsOutn("LATE" + yearCnt, 13, String.valueOf(totalLate));
                    svf.VrsOutn("EARLY" + yearCnt, 13, String.valueOf(totalEaly));
                    attendCnt++;
                }
                yearCnt++;
            }

            svf.VrsOutn("TOTAL", 1, String.valueOf(totalScore));
            svf.VrsOutn("TOTAL", 2, String.valueOf(totalAssess));

            final BigDecimal setScore = new BigDecimal(totalScore).divide(new BigDecimal(totalCnt), 1, BigDecimal.ROUND_HALF_UP);
            final BigDecimal setAssess = new BigDecimal(totalAssess).divide(new BigDecimal(totalCnt), 1, BigDecimal.ROUND_HALF_UP);
            svf.VrsOutn("AVERAGE", 1, setScore.toString());
            svf.VrsOutn("AVERAGE", 2, setAssess.toString());

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            for (int schCnt = 0; schCnt < _param._chkData.length; schCnt++) {
                final String chkVal = _param._chkData[schCnt];
                final String[] studentInfo = StringUtils.split(chkVal, ",");
                final String schregNo = studentInfo[0];
                final String year = studentInfo[1];
                final String semester = studentInfo[2];
                final String grade = studentInfo[3];
                final String studentSql = getStudentInfoSql(year, semester, grade, schregNo);
                log.debug(" studentSql =" + studentSql);
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setSchregNo = rs.getString("SCHREGNO");
                    final String setYear = rs.getString("YEAR");
                    final String setGrade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");

                    final Student student = new Student(setSchregNo, setYear, setGrade, hrClass, hrName, attendNo, name, sex);
                    student.setYearList(db2);
                    retList.add(student);
                }
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentInfoSql(final String year, final String semester, final String grade, final String schregNo) {
        final StringBuffer stb = new StringBuffer();
        if (_param._ctrlYear.equals(year)) {
            stb.append(" SELECT ");
            stb.append("     REGD.YEAR, ");
            stb.append("     REGD.SEMESTER, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGH.HR_NAME, ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA as KANA, ");
            stb.append("     BASE.SEX as SEXNUM, ");
            stb.append("     CASE BASE.SEX WHEN Z002.NAMECD2 THEN Z002.NAME2 ELSE BASE.SEX END as SEX, ");
            stb.append("     BASE.BIRTHDAY ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
            stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
            stb.append("          AND REGD.GRADE = REGH.GRADE ");
            stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
            stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + semester + "' ");
            stb.append("     AND REGD.GRADE = '" + grade + "' ");
            stb.append("     AND REGD.SCHREGNO = '" + schregNo + "' ");
        } else {
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGH.HR_NAME, ");
            stb.append("     REGD.SEMESTER, ");
            stb.append("     BASE.NAME , ");
            stb.append("     REGD.YEAR, ");
            stb.append("     BASE.NAME_KANA AS KANA, ");
            stb.append("     BASE.SEX AS SEXNUM, ");
            stb.append("     CASE BASE.SEX WHEN Z002.NAMECD2 THEN Z002.NAME2 ELSE BASE.SEX END AS SEX, ");
            stb.append("     BASE.BIRTHDAY, ");
            stb.append("     BASE.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     GRD_BASE_MST BASE ");
            stb.append("     LEFT OUTER JOIN ");
            stb.append("         (SELECT NAME2, NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'Z002') Z002 ON BASE.SEX = Z002.NAMECD2 ");
            if ("0".equals(_param._schooldiv)) {//学年制
                stb.append("      ,V_GRD_REGDYEAR_GRADE_DAT REGD ");
            }
            if ("1".equals(_param._schooldiv)) {//単位制
                stb.append("      ,V_GRD_REGDYEAR_UNIT_DAT REGD ");
            }
            stb.append("      ,GRD_REGD_HDAT REGH ");
            stb.append(" WHERE ");
            stb.append("     BASE.SCHREGNO = '" + schregNo + "' ");
            stb.append("     AND REGD.YEAR = '" + year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + semester + "' ");
            stb.append("     AND REGD.GRADE = '" + grade + "' ");
            stb.append("     AND BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     AND REGH.YEAR = REGD.YEAR ");
            stb.append("     AND REGH.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND REGH.GRADE = REGD.GRADE ");
            stb.append("     AND REGH.HR_CLASS = REGD.HR_CLASS ");
        }
        return stb.toString();
    }

    private class Student {
        final String _schregNo;
        final String _year;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _sex;
        final List _yearList;
        public Student(
                final String schregNo,
                final String year,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendNo,
                final String name,
                final String sex
        ) {
            _schregNo = schregNo;
            _year = year;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _yearList = new ArrayList();
        }

        private void setYearList(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String yearSql = getYearSql();
                log.debug(" yearSql =" + yearSql);
                ps = db2.prepareStatement(yearSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setYear = rs.getString("YEAR");
                    final String setGrade = rs.getString("GRADE");

                    final YearData yearData = new YearData(setYear, setGrade);
                    yearData.setSubclassList(db2, _schregNo);
                    yearData.setAttendList(db2, _schregNo);
                    _yearList.add(yearData);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getYearSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MAX(REGD.YEAR) AS YEAR, ");
            stb.append("     REGD.GRADE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + _schregNo + "' ");
            stb.append(" GROUP BY ");
            stb.append("     REGD.GRADE ");
            stb.append(" ORDER BY ");
            stb.append("     YEAR ");
            return stb.toString();
        }
    }

    private class YearData {
        final String _year;
        final String _grade;
        final List _subclassList;
        final List _attendList;
        public YearData(
                final String year,
                final String grade
        ) {
            _year = year;
            _grade = grade;
            _subclassList = new ArrayList();
            _attendList = new ArrayList();
        }

        private void setSubclassList(final DB2UDB db2, final String schregNo) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String subclassSql = getSubclassSql(schregNo);
                log.debug(" subclassSql =" + subclassSql);
                ps = db2.prepareStatement(subclassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String gradeRecord = StringUtils.defaultString(rs.getString("GRADE_RECORD"));
                    final String assess = StringUtils.defaultString(rs.getString("ASSESS"));

                    final SubclassData subclassData = new SubclassData(classCd, schoolKind, curriculumCd, subclassCd, subclassName, gradeRecord, assess);
                    _subclassList.add(subclassData);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSubclassSql(final String schregNo) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SUB_T AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     CASE WHEN SUBREP.COMBINED_CLASSCD IS NULL ");
            stb.append("          THEN REC.CLASSCD ");
            stb.append("          ELSE SUBREP.COMBINED_CLASSCD ");
            stb.append("     END AS CLASSCD, ");
            stb.append("     CASE WHEN SUBREP.COMBINED_SCHOOL_KIND IS NULL ");
            stb.append("          THEN REC.SCHOOL_KIND ");
            stb.append("          ELSE SUBREP.COMBINED_SCHOOL_KIND ");
            stb.append("     END AS SCHOOL_KIND, ");
            stb.append("     CASE WHEN SUBREP.COMBINED_CURRICULUM_CD IS NULL ");
            stb.append("          THEN REC.CURRICULUM_CD ");
            stb.append("          ELSE SUBREP.COMBINED_CURRICULUM_CD ");
            stb.append("     END AS CURRICULUM_CD, ");
            stb.append("     CASE WHEN SUBREP.COMBINED_SUBCLASSCD IS NULL ");
            stb.append("          THEN REC.SUBCLASSCD ");
            stb.append("          ELSE SUBREP.COMBINED_SUBCLASSCD ");
            stb.append("     END AS SUBCLASSCD, ");
            stb.append("     CASE WHEN SUBREP.COMBINED_SUBCLASSCD IS NULL ");
            stb.append("          THEN SUB_M.SUBCLASSNAME ");
            stb.append("          ELSE SUBREP_M.SUBCLASSNAME ");
            stb.append("     END AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     KIN_RECORD_DAT REC ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUB_M ON REC.CLASSCD = SUB_M.CLASSCD ");
            stb.append("          AND REC.SCHOOL_KIND = SUB_M.SCHOOL_KIND ");
            stb.append("          AND REC.CURRICULUM_CD = SUB_M.CURRICULUM_CD ");
            stb.append("          AND REC.SUBCLASSCD = SUB_M.SUBCLASSCD ");
            stb.append("     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT SUBREP ON REC.YEAR = SUBREP.YEAR ");
            stb.append("          AND REC.CLASSCD = SUBREP.ATTEND_CLASSCD ");
            stb.append("          AND REC.SCHOOL_KIND = SUBREP.ATTEND_SCHOOL_KIND ");
            stb.append("          AND REC.CURRICULUM_CD = SUBREP.ATTEND_CURRICULUM_CD ");
            stb.append("          AND REC.SUBCLASSCD = SUBREP.ATTEND_SUBCLASSCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBREP_M ON SUBREP.COMBINED_CLASSCD = SUBREP_M.CLASSCD ");
            stb.append("          AND SUBREP.COMBINED_SCHOOL_KIND = SUBREP_M.SCHOOL_KIND ");
            stb.append("          AND SUBREP.COMBINED_CURRICULUM_CD = SUBREP_M.CURRICULUM_CD ");
            stb.append("          AND SUBREP.COMBINED_SUBCLASSCD = SUBREP_M.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     REC.YEAR = '" + _year + "' ");
            stb.append("     AND REC.SCHREGNO = '" + schregNo + "' ");
            stb.append("     AND REC.CLASSCD < '90' ");
            stb.append("     AND REC.CLASSCD || '-' || REC.SCHOOL_KIND || '-' || REC.CURRICULUM_CD || '-' || REC.SUBCLASSCD NOT IN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'D065') ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     SUB_T.CLASSCD, ");
            stb.append("     SUB_T.SCHOOL_KIND, ");
            stb.append("     SUB_T.CURRICULUM_CD, ");
            stb.append("     SUB_T.SUBCLASSCD, ");
            stb.append("     SUB_T.SUBCLASSNAME, ");
            stb.append("     REC.GRADE_RECORD, ");
            stb.append("     CASE REC.JUDGE_PATTERN ");
            stb.append("          WHEN 'A' THEN REC.A_PATTERN_ASSESS ");
            stb.append("          WHEN 'B' THEN REC.B_PATTERN_ASSESS ");
            stb.append("          WHEN 'C' THEN REC.C_PATTERN_ASSESS ");
            stb.append("          ELSE '' ");
            stb.append("     END AS ASSESS ");
            stb.append(" FROM ");
            stb.append("     SUB_T ");
            stb.append("     LEFT JOIN KIN_RECORD_DAT REC ON REC.YEAR = '" + _year + "' ");
            stb.append("          AND REC.CLASSCD = SUB_T.CLASSCD ");
            stb.append("          AND REC.SCHOOL_KIND = SUB_T.SCHOOL_KIND ");
            stb.append("          AND REC.CURRICULUM_CD = SUB_T.CURRICULUM_CD ");
            stb.append("          AND REC.SUBCLASSCD = SUB_T.SUBCLASSCD ");
            stb.append("          AND REC.SCHREGNO = '" + schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SUB_T.CLASSCD, ");
            stb.append("     SUB_T.SCHOOL_KIND, ");
            stb.append("     SUB_T.CURRICULUM_CD, ");
            stb.append("     SUB_T.SUBCLASSCD ");
            return stb.toString();
        }

        private void setAttendList(final DB2UDB db2, final String schregNo) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String attendSql = getAttendSql(schregNo);
                log.debug(" attendSql =" + attendSql);
                ps = db2.prepareStatement(attendSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String month = rs.getString("MONTH");
                    final String sick = rs.getString("SICK");
                    final String late = rs.getString("LATE");
                    final String ealy = rs.getString("EARLY");

                    final AttendData attendData = new AttendData(month, sick, late, ealy);
                    _attendList.add(attendData);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getAttendSql(final String schregNo) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     ATTENDS.MONTH, ");
            stb.append("     VALUE(ATTENDS.SICK, 0) + VALUE(ATTENDS.NOTICE, 0) + VALUE(ATTENDS.NONOTICE, 0) AS SICK, ");
            stb.append("     ATTENDS.LATE, ");
            stb.append("     ATTENDS.EARLY ");
            stb.append(" FROM ");
            stb.append("     ATTEND_SEMES_DAT ATTENDS ");
            stb.append(" WHERE ");
            stb.append("     ATTENDS.COPYCD = '0' ");
            stb.append("     AND ATTENDS.YEAR = '" + _year + "' ");
            stb.append("     AND ATTENDS.SCHREGNO = '" + schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     CASE WHEN ATTENDS.MONTH < '04' ");
            stb.append("          THEN 1 ");
            stb.append("          ELSE 0 ");
            stb.append("     END, ");
            stb.append("     ATTENDS.MONTH ");

            return stb.toString();
        }
    }

    private class SubclassData {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final String _gradeRecord;
        final String _assess;
        public SubclassData(
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclassCd,
                final String subclassName,
                final String gradeRecord,
                final String assess
        ) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _gradeRecord = gradeRecord;
            _assess = assess;
        }
    }

    private class AttendData {
        final String _month;
        final String _sick;
        final String _late;
        final String _ealy;
        public AttendData(
                final String month,
                final String sick,
                final String late,
                final String ealy
        ) {
            _month = month;
            _sick = sick;
            _late = late;
            _ealy = ealy;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57530 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String[] _chkData;
        private final String _schooldiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _chkData = request.getParameterValues("chk");
            _schooldiv = request.getParameter("schooldiv");
        }

    }
}

// eof
