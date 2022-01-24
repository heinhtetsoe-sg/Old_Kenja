/*
 * $Id: 0634ea1e83f380deb3338ec7cefac45e7d86156b $
 *
 * 作成日: 2012/07/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD176D {

    private static final Log log = LogFactory.getLog(KNJD176D.class);
    
    private static final String SEME9 = "9";
    
    private static final String _333333 = "333333";
    private static final String _555555 = "555555";
    private static final String _999999 = "999999";

    private boolean _hasData;

    Param _param;

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
        
        final List students = Student.getStudents(db2, _param);
        setData(db2, students);

        final SvfForm form;
        if (_param._isJunior) {
            form = new FormJunior(svf);
        } else {
            form = new FormHigh(svf);
        }
        
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            form.print(student);
        }
    }

    private void setData(final DB2UDB db2, final List students) {
        setAttendance(db2, students);
        setRecord(db2, students);
        setCredit(db2, students);
        SchregClubHistDat.set(db2, _param, students);
        SchregCommitteeHistDat.set(db2, _param, students);
        SchregQualifiedHobbyDat.set(db2, _param, students);
    }
    
    /**
     * 学籍番号の生徒を得る
     * @param schregno 学籍番号
     * @param students 生徒のリスト
     * @return 学籍番号の生徒
     */
    private Student getStudent(final String schregno, final List students) {
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        return null;
    }
    
    private void setAttendance(final DB2UDB db2, final List students) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            _param._attendParamMap.put("grade", _param._gradeHrClass.substring(0, 2));
            _param._attendParamMap.put("hrClass", _param._gradeHrClass.substring(2, 5));
            String sql;
            sql = AttendAccumulate.getAttendSemesSql(
                    _param._year,
                    _param._semester,
                    null,
                    _param._date,
                    _param._attendParamMap
            );
            
            //log.debug(" attend semes sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student student = getStudent(rs.getString("SCHREGNO"), students);
                if (student == null) {
                    continue;
                }
                final String lesson = rs.getString("LESSON");
                final String mourning = rs.getString("MOURNING");
                final String suspend = String.valueOf(rs.getInt("SUSPEND") + rs.getInt("VIRUS") + rs.getInt("KOUDOME"));
                final String abroad = rs.getString("TRANSFER_DATE");
                final String mlesson = rs.getString("MLESSON");
                final String absence = rs.getString("SICK");
                final String attend = rs.getString("PRESENT");
                final String late = rs.getString("LATE");
                final String early = rs.getString("EARLY");
                    
                final Attendance attendance = new Attendance(lesson, mourning, suspend, abroad, mlesson, absence, attend, late, early);
                // log.debug("   schregno = " + student._schregno + " , attendance = " + attendance);
                student.setAttendance(rs.getString("SEMESTER"), attendance);
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    private void setRecord(final DB2UDB db2, final List students) {
        final String sqlRecord = sqlRecord(_param);
        log.debug("setScoreValue sql = " + sqlRecord);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlRecord);
            rs = ps.executeQuery();
            while(rs.next()) {
                final Student student = getStudent(rs.getString("SCHREGNO"), students);
                if (student == null) {
                    continue;
                }
                final String rsSubclassCd = rs.getString("SUBCLASSCD");
                final String testKindCd = rs.getString("TESTKINDCD");
                if (_999999.equals(rsSubclassCd) || _555555.equals(rsSubclassCd) || _333333.equals(rsSubclassCd)) {
                    final Rank rank = new Rank(rsSubclassCd, testKindCd, rs.getString("GRADE_RANK"), rs.getString("GRADE_COUNT"));
                    student.getSubclassTestkindRankMap(rsSubclassCd).put(testKindCd, rank);
                    continue;
                }
                final String subclassCd;
                if ("1".equals(_param._useCurriculumcd)){
                    subclassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rsSubclassCd;
                } else {
                    subclassCd = rsSubclassCd;
                }
                final Subclass subclass = (Subclass) _param._subclassMap.get(subclassCd);
                if (null == subclass) {
                    continue;
                }
                
                final String value = rs.getString("VALUE");
                final Record sv = new Record(subclass, testKindCd, value, rs.getString("COMP_CREDIT"), rs.getString("RECORD_SCORE_DAT_SCORE"));
                student.getSubclassTestkindRecordMap(subclass).put(testKindCd, sv);
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    private String sqlRecord(final Param param) {
        
        final StringBuffer stb = new StringBuffer();
        //対象生徒
        stb.append("WITH SCHNO AS( ");
        stb.append(" SELECT ");
        stb.append("    W1.SCHREGNO, ");
        stb.append("    W1.GRADE, ");
        stb.append("    W1.SEMESTER, ");
        stb.append("    W1.HR_CLASS ");
        stb.append(" FROM ");
        stb.append("    SCHREG_REGD_DAT W1 ");
        stb.append(" WHERE ");
        stb.append("    W1.YEAR = '" + param._year + "' ");
        stb.append("    AND W1.SEMESTER = '" + param.getRegdSemester() + "' ");
        stb.append("    AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrClass + "' ");
        stb.append(" ), RECORD_VALUE AS ( ");
        stb.append(" SELECT ");
        stb.append("    T1.YEAR, ");
        stb.append("    T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
        }
        stb.append("    T1.SUBCLASSCD, T1.SEMESTER, ");
        stb.append("    T1.TESTKINDCD, T1.TESTITEMCD,  T1.SCORE AS VALUE, ");
        stb.append("    T1.SCORE, T1.AVG, T1.GRADE_RANK, T2.COUNT AS GRADE_COUNT ");
        stb.append(" FROM    RECORD_RANK_DAT T1");
        stb.append("  LEFT JOIN RECORD_AVERAGE_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("     AND T2.TESTITEMCD = T1.TESTITEMCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     AND T2.AVG_DIV = '1' ");
        stb.append("     AND T2.GRADE = '" + _param._gradeHrClass.substring(0, 2) + "' ");
        stb.append("     AND T2.HR_CLASS = '000' ");
        stb.append("     AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '00000000' ");
        stb.append(" WHERE T1.YEAR = '" + param._year + "' ");
        if (!_param._lastSemester.equals(param._semester)) {
            stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
        }
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '9900' ");
        stb.append("     AND EXISTS (SELECT 'X' FROM SCHNO WHERE T1.SCHREGNO = SCHREGNO) ");
        stb.append(" ) ");
        stb.append(" SELECT");
        stb.append("    T1.SCHREGNO");
        stb.append("    ,T1.SUBCLASSCD");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       ,T1.CLASSCD ,T1.SCHOOL_KIND ,T1.CURRICULUM_CD ");
        }
        stb.append("    ,T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS TESTKINDCD ");
        stb.append("    ,T1.SCORE, T1.AVG ");
        stb.append("    ,T4.COMP_CREDIT ");
        stb.append("    ,T4.VALUE AS RECORD_SCORE_DAT_SCORE ");
        stb.append("    ,T1.VALUE, T1.GRADE_RANK, T1.GRADE_COUNT ");
        stb.append(" FROM    RECORD_VALUE T1");
        stb.append(" LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("        AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("        AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("        AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append(" LEFT JOIN CLASS_MST T3 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("        T3.CLASSCD = T1.CLASSCD ");
            stb.append("        AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        } else {
            stb.append("        T3.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
        }
        stb.append(" LEFT JOIN RECORD_SCORE_DAT T4 ON T4.YEAR = T1.YEAR ");
        stb.append("        AND T4.SEMESTER = T1.SEMESTER ");
        stb.append("        AND T4.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("        AND T4.TESTITEMCD = T1.TESTITEMCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("        AND T4.CLASSCD = T1.CLASSCD ");
            stb.append("        AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("        AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("        AND T4.SCORE_DIV = '00' ");
        stb.append("        AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("        AND T4.SCHREGNO = T1.SCHREGNO ");

        return stb.toString();
    }
    
    private void setCredit(final DB2UDB db2, final List students) {
        final String sqlCredit = sqlCredit(_param);
//        log.debug("setCredit sql = " + sqlCredit);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sqlCredit);
            rs = ps.executeQuery();
            while(rs.next()) {
                final Student student = getStudent(rs.getString("SCHREGNO"), students);
                if (student == null) {
                    continue;
                }
                final String rsSubclassCd = rs.getString("SUBCLASSCD");
                if (_999999.equals(rsSubclassCd) || _555555.equals(rsSubclassCd) || _333333.equals(rsSubclassCd)) {
                    continue;
                }
                final String subclassCd;
                if ("1".equals(_param._useCurriculumcd)){
                    subclassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rsSubclassCd;
                } else {
                    subclassCd = rsSubclassCd;
                }
                final Subclass subclass = (Subclass) _param._subclassMap.get(subclassCd);
                if (null == subclass) {
                    continue;
                }
                student._creditMap.put(subclass, rs.getString("CREDITS"));
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    private String sqlCredit(final Param param) {
        
        final StringBuffer stb = new StringBuffer();
        //対象生徒
        stb.append("WITH SCHNO AS( ");
        stb.append(" SELECT ");
        stb.append("    W1.SCHREGNO, ");
        stb.append("    W1.YEAR, ");
        stb.append("    W1.GRADE, ");
        stb.append("    W1.COURSECD, ");
        stb.append("    W1.MAJORCD, ");
        stb.append("    W1.COURSECODE ");
        stb.append(" FROM ");
        stb.append("    SCHREG_REGD_DAT W1 ");
        stb.append(" WHERE ");
        stb.append("    W1.YEAR = '" + param._year + "' ");
        stb.append("    AND W1.SEMESTER = '" + param.getRegdSemester() + "' ");
        stb.append("    AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrClass + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("    T2.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
        }
        stb.append("    T1.SUBCLASSCD, ");
        stb.append("    T1.CREDITS ");
        stb.append(" FROM    CREDIT_MST T1");
        stb.append(" INNER JOIN SCHNO T2 ON T2.YEAR = T1.YEAR ");
        stb.append("   AND T2.COURSECD = T1.COURSECD ");
        stb.append("   AND T2.GRADE = T1.GRADE ");
        stb.append("   AND T2.MAJORCD = T1.MAJORCD ");
        stb.append("   AND T2.COURSECODE = T1.COURSECODE ");
        return stb.toString();
    }
    
    static class Student {
        
        String _attendNo;
        String _grade;
        
        String _courseCd;
        String _courseName;
        
        String _majorCd;
        String _majorName;
        
        String _courseCode;
        String _courseCodeName;
        
        String _gradeName;
        String _hrName;
        String _hrNameAbbv;
        String _hrStaffName;
        
        String _name;
        
        final String _schregno;
        
        final Map _attendMap = new HashMap();
        
        final Map _subclassRecordMap = new TreeMap();
        
        final Map _subclassRankMap = new TreeMap();
        
        final Map _creditMap = new HashMap();
        
//        final Map _attendRemark1 = new HashMap();
        
        final List _clubHistDat = new ArrayList();
        final List _committeeHistDat = new ArrayList();
        final List _qualifiedHobbyDat = new ArrayList();
        
        public Student(final String schregno) {
            _schregno = schregno;
        }
        
        public void setAttendance(final String semester, final Attendance a) {
            _attendMap.put(semester, a);
        }

        public Map getSubclassTestkindRecordMap(final Subclass subclass) {
            if (null == _subclassRecordMap.get(subclass)) {
                _subclassRecordMap.put(subclass, new HashMap());
            }
            return (Map) _subclassRecordMap.get(subclass);
        }
        
        public Map getSubclassTestkindRankMap(final String subclassCd) {
            if (null == _subclassRankMap.get(subclassCd)) {
                _subclassRankMap.put(subclassCd, new HashMap());
            }
            return (Map) _subclassRankMap.get(subclassCd);
        }

        // -----
        
        public static List getStudents(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs1 = null;
            List students = new ArrayList();
            try {
                final String sqlRegdData = sqlRegdData(param);
                ps = db2.prepareStatement(sqlRegdData);
                rs1 = ps.executeQuery();
                
                while( rs1.next() ){
                    
                    Student student = new Student(rs1.getString("SCHREGNO"));
                    students.add(student);
                    
                    student._grade = rs1.getString("GRADE");
                    student._courseCd = rs1.getString("COURSECD");
                    student._majorCd = rs1.getString("MAJORCD");
                    student._courseCode = rs1.getString("COURSECODE");
                    
                    student._courseName = rs1.getString("COURSENAME");
                    student._majorName = rs1.getString("MAJORNAME");
                    student._courseCodeName = StringUtils.defaultString(rs1.getString("COURSECODENAME"), "");
                    student._hrName = StringUtils.defaultString(rs1.getString("HR_NAME"), "");
                    student._hrNameAbbv = StringUtils.defaultString(rs1.getString("HR_NAMEABBV"), "");
                    student._attendNo = "" + (null == rs1.getString("ATTENDNO") || !NumberUtils.isDigits(rs1.getString("ATTENDNO")) ? "" : String.valueOf(Integer.parseInt(rs1.getString("ATTENDNO"))));
                    final String name = StringUtils.defaultString(rs1.getString("NAME"), "");
                    // final String realName = StringUtils.defaultString(rs1.getString("REAL_NAME"), "");
                    student._name = name; //"1".equals(rs1.getString("USE_REAL_NAME")) ? realName : name;
                    student._hrStaffName = rs1.getString("STAFFNAME");
                    
                    //log.debug("対象の生徒" + student);
                }
            } catch (Exception ex) { 
                log.error("printSvfMain read error! ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs1);
                db2.commit();
            }
            
            return students;
        }
        
        private static String sqlRegdData (final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH SCHNO_A AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.GRADE, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1,V_SEMESTER_GRADE_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.SEMESTER = '"+ param.getRegdSemester() +"' ");
            stb.append(        "AND T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + param._gradeHrClass + "' ");
            stb.append(        "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(               "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
            stb.append(               "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(              "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(    ") ");
            
            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, T2.HR_NAMEABBV, ");
            stb.append(        "T5.NAME, T5.REAL_NAME, CASE WHEN T7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append(        "T3.COURSENAME, T4.MAJORNAME, T6.COURSECODENAME, ");
            stb.append(        "T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, ");
            stb.append(        "T8.STAFFNAME ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append(        "INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + param._year + "' AND ");
            stb.append(                                          "T2.SEMESTER = T1.SEMESTER AND ");
            stb.append(                                          "T2.GRADE || T2.HR_CLASS = '" + param._gradeHrClass + "' ");
            stb.append(        "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append(        "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            stb.append(        "LEFT JOIN COURSECODE_MST T6 ON T6.COURSECODE = T1.COURSECODE ");
            stb.append(        "LEFT JOIN SCHREG_NAME_SETUP_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.DIV = '03' ");
            stb.append(        "LEFT JOIN STAFF_MST T8 ON T8.STAFFCD = T2.TR_CD1 ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
        }

        public List getRemark1(final int byteLength, final int maxlines, final int len) {
            final List texts = new ArrayList();
            final String joinStr = " ／ ";
            for (final Iterator it = _clubHistDat.iterator(); it.hasNext();) {
                final SchregClubHistDat schd = (SchregClubHistDat) it.next();
                final String text = StringUtils.join(new Object[]{schd._clubname, formatDate(schd._sdate) + "〜" + formatDate(schd._edate), schd._executivename}, joinStr);
                texts.add(text);
            }
            for (final Iterator it = _committeeHistDat.iterator(); it.hasNext();) {
                final SchregCommitteeHistDat schd = (SchregCommitteeHistDat) it.next();
                final String name = StringUtils.isEmpty(schd._committeename) ? schd._chargename : schd._committeename;
                final String text = StringUtils.join(new Object[]{schd._semestername, name, schd._executivename}, joinStr);
                texts.add(text);
            }
            for (final Iterator it = _qualifiedHobbyDat.iterator(); it.hasNext();) {
                final SchregQualifiedHobbyDat sqhd = (SchregQualifiedHobbyDat) it.next();
                final String text = StringUtils.join(new Object[]{formatDate(sqhd._regddate), sqhd._conditionName, sqhd._contents, sqhd._remark}, joinStr);
                texts.add(text);
            }
            final List lines = new ArrayList();
            for (final Iterator it = texts.iterator(); it.hasNext();) {
                final String text = (String) it.next();
                final List textList = retDivideString(text, byteLength, len);
                if (!textList.isEmpty() && lines.size() + textList.size() <= maxlines) {
                    lines.addAll(textList);
                    for (int j = textList.size(); j < len; j++) {
                        lines.add("");
                    }
                }
            }
//            log.debug(" lines = " + lines);
            return lines;
        }
        
        private String formatDate(final String sdate) {
            String formatted = "";
            if (null != sdate && sdate.length() == 10) {
                try {
                    final Date date = java.sql.Date.valueOf(sdate);
                    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                    formatted = sdf.format(date);
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            return formatted;
        }
            
    }
    
    private static class SchregClubHistDat {
        final String _clubcd;
        final String _clubname;
        final String _sdate;
        final String _edate;
        final String _executivecd;
        final String _executivename;

        SchregClubHistDat(final String clubcd, final String clubname, final String sdate, final String edate,
                final String executivecd, final String executivename) {
            _clubcd = clubcd;
            _clubname = clubname;
            _sdate = sdate;
            _edate = edate;
            _executivecd = executivecd;
            _executivename = executivename;
        }

        public static void set(final DB2UDB db2, final Param param, final List students) { 
            PreparedStatement ps = null; 
            ResultSet rs = null; 
            try { 
                final String sql = sql(param);
                ps = db2.prepareStatement(sql); 
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery(); 
                    while (rs.next()) {
                        final String clubcd = rs.getString("CLUBCD");
                        final String clubname = StringUtils.defaultString(rs.getString("CLUBNAME"));
                        final String sdate = StringUtils.defaultString(rs.getString("SDATE"));
                        final String edate = StringUtils.defaultString(rs.getString("EDATE"));
                        final String executivecd = rs.getString("EXECUTIVECD");
                        final String executivename = StringUtils.defaultString(rs.getString("EXECUTIVENAME"));
                        final SchregClubHistDat schregclubhistdat = new SchregClubHistDat(clubcd, clubname, sdate, edate, executivecd, executivename);
                        student._clubHistDat.add(schregclubhistdat);
                    }
                }
            } catch (Exception ex) { 
                log.error("exception!", ex);
            } finally { 
                DbUtils.closeQuietly(null, ps, rs); 
                db2.commit(); 
            }
        }
        
        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("  T1.CLUBCD, T2.CLUBNAME, T1.SDATE, T1.EDATE, T1.EXECUTIVECD, T3.NAME1 AS EXECUTIVENAME ");
            stb.append("FROM ");
            stb.append("  SCHREG_CLUB_HIST_DAT T1 ");
            stb.append("  LEFT JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ");
            stb.append("  LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'J001' ");
            stb.append("    AND T1.EXECUTIVECD = T3.NAMECD2 ");
            stb.append("WHERE ");
            stb.append("  T1.SCHREGNO = ? ");
            stb.append("  AND FISCALYEAR(T1.SDATE) <= '" + param._year + "' ");
            stb.append("  AND (T1.EDATE IS NULL OR FISCALYEAR(VALUE(T1.EDATE, '9999-12-31')) >= '" + param._year + "') ");
            stb.append("ORDER BY ");
            stb.append("  T1.SDATE, T1.CLUBCD ");
            return stb.toString();
        }
    }
    
    private static class SchregCommitteeHistDat {
        final String _semester;
        final String _committeeFlg;
        final String _committeeFlgName;
        final String _committeecd;
        final String _committeename;
        final String _chargename;
        final String _executivecd;
        final String _executivename;
        final String _semestername;

        SchregCommitteeHistDat(
                final String semester,
                final String committeeFlg,
                final String committeeFlgName,
                final String committeecd,
                final String committeename,
                final String chargename,
                final String executivecd,
                final String executivename,
                final String semestername
         ) { 
            _semester = semester;
            _committeeFlg = committeeFlg;
            _committeeFlgName = committeeFlgName;
            _committeecd = committeecd;
            _committeename = committeename;
            _chargename = chargename;
            _executivecd = executivecd;
            _executivename = executivename;
            _semestername = semestername;
         } 

         public static void set(final DB2UDB db2, final Param param, final List students) { 
             PreparedStatement ps = null; 
             ResultSet rs = null; 
             try { 
                 final String sql = sql(param);
                 ps = db2.prepareStatement(sql);
                 for (Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery(); 
                    while (rs.next()) { 
                        final String semester = rs.getString("SEMESTER");
                        final String committeeFlg = rs.getString("COMMITTEE_FLG");
                        final String committeeFlgName = StringUtils.defaultString(rs.getString("COMMITTEE_FLG_NAME"));
                        final String committeecd = rs.getString("COMMITTEECD");
                        final String committeename = StringUtils.defaultString(rs.getString("COMMITTEENAME"));
                        final String chargename = StringUtils.defaultString(rs.getString("CHARGENAME"));
                        final String executivecd = rs.getString("EXECUTIVECD");
                        final String executivename = StringUtils.defaultString(rs.getString("EXECUTIVENAME"));
                        final String semestername = StringUtils.defaultString(rs.getString("SEMESTERNAME"));
                        final SchregCommitteeHistDat schregcommitteehistdat = new SchregCommitteeHistDat(semester, committeeFlg, committeeFlgName, committeecd, committeename, chargename, executivecd, executivename, semestername);
                        student._committeeHistDat.add(schregcommitteehistdat);
                    } 
                }
             } catch (Exception ex) { 
                 log.fatal("exception!", ex);
             } finally { 
                 DbUtils.closeQuietly(null, ps, rs); 
                 db2.commit(); 
             }
         }
         
         private static String sql(final Param param) {
             final StringBuffer stb = new StringBuffer();
             stb.append("SELECT T1.SEMESTER, T1.COMMITTEE_FLG, ");
             stb.append("    T4.NAME1 AS COMMITTEE_FLG_NAME, ");
             stb.append("    T1.COMMITTEECD, ");
             stb.append("    T2.COMMITTEENAME, T1.CHARGENAME, T1.EXECUTIVECD, T3.NAME1 AS EXECUTIVENAME, ");
             stb.append("    CASE WHEN '9' = T5.SEMESTER THEN '通年' ELSE T5.SEMESTERNAME END AS SEMESTERNAME ");
             stb.append("FROM SCHREG_COMMITTEE_HIST_DAT T1 ");
             stb.append("LEFT JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
             stb.append("    AND T2.COMMITTEECD = T1.COMMITTEECD ");
             stb.append("LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'J002' ");
             stb.append("    AND T3.NAMECD2 = T1.EXECUTIVECD ");
             stb.append("LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'J003' ");
             stb.append("    AND T4.NAMECD2 = T1.COMMITTEE_FLG ");
             stb.append("LEFT JOIN V_SEMESTER_GRADE_MST T5 ON T5.YEAR = T1.YEAR ");
             stb.append("    AND T5.SEMESTER = T1.SEMESTER ");
             stb.append("    AND T5.GRADE = '" + param._gradeHrClass.substring(0, 2) + "' ");
             stb.append("WHERE ");
             stb.append("    T1.YEAR = '" + param._year +"' ");
             stb.append("    AND T1.SCHREGNO = ? ");
             stb.append("ORDER BY ");
             stb.append("    T1.GRADE, T1.SEMESTER, T1.SEQ ");
             return stb.toString();
         }
    }
    
    private static class SchregQualifiedHobbyDat {
        final String _regddate;
        final String _conditionDiv;
        final String _conditionName;
        final String _contents;
        final String _remark;

        SchregQualifiedHobbyDat(
                final String regddate,
                final String conditionDiv,
                final String conditionName,
                final String contents,
                final String remark
         ) { 
            _regddate = regddate;
            _conditionDiv = conditionDiv;
            _conditionName = conditionName;
            _contents = contents;
            _remark = remark;
         } 

         public static void set(final DB2UDB db2, final Param param, final List students) { 
             PreparedStatement ps = null; 
             ResultSet rs = null;
             try {
                 final String sql = sql(param);
                 ps = db2.prepareStatement(sql);
                 for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery(); 
                    while (rs.next()) { 
                        final String regddate = rs.getString("REGDDATE");
                        final String conditionDiv = rs.getString("CONDITION_DIV");
                        final String conditionName = rs.getString("CONDITION_NAME");
                        final String contents = rs.getString("CONTENTS");
                        final String remark = rs.getString("REMARK");
                        final SchregQualifiedHobbyDat schregqualifiedhobbydat = new SchregQualifiedHobbyDat(regddate, conditionDiv, conditionName, contents, remark);
                        student._qualifiedHobbyDat.add(schregqualifiedhobbydat);
                    } 
                }
             } catch (Exception ex) { 
                 log.error("exception!", ex);
             } finally { 
                 DbUtils.closeQuietly(null, ps, rs); 
                 db2.commit(); 
             }
         }

         private static String sql(final Param param) {
             final StringBuffer stb = new StringBuffer();
             stb.append("SELECT ");
             stb.append("    T1.REGDDATE, ");
             stb.append("    T1.CONDITION_DIV, ");
             stb.append("    T2.NAME1 AS CONDITION_NAME, ");
             stb.append("    T1.CONTENTS, ");
             stb.append("    T1.REMARK ");
             stb.append("FROM ");
             stb.append("    SCHREG_QUALIFIED_HOBBY_DAT T1 ");
             stb.append("    LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'H311' ");
             stb.append("        AND T2.NAMECD2 = T1.CONDITION_DIV ");
             stb.append("WHERE ");
             stb.append("    T1.SCHREGNO = ? ");
             stb.append("    AND FISCALYEAR(T1.REGDDATE) = '" + param._year + "' ");
             stb.append("ORDER BY ");
             stb.append("    T1.REGDDATE ");
             return stb.toString();
         }
    }
    
    static class Subclass implements Comparable {
        final String _classCd;
        final String _className;
        final String _subclassCd;
        final String _subclassName;
        final Integer _classOrder;
        final Integer _subclassOrder;
        public Subclass(final String classCd,
                final String className,
                final String subclassCd,
                final String subclassName,
                final Integer classOrder,
                final Integer subclassOrder) {
            _classCd = classCd;
            _className = className;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _classOrder = classOrder;
            _subclassOrder = subclassOrder;
        }
        public int compareTo(final Object o) {
            final Subclass other = (Subclass) o;
            int rtn;
            rtn = _classOrder.compareTo(other._classOrder);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _classCd.compareTo(other._classCd);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclassOrder.compareTo(other._subclassOrder);
            if (0 != rtn) {
                return rtn;
            }
            rtn = _subclassCd.compareTo(other._subclassCd);
            return rtn;
        }
        public String toString() {
            return "Subclass(" + _classCd + ":" +_subclassCd + ":" + _className + ":"+ _subclassName + ")";
        }
    }
    
    static class Record {
        final Subclass _subclass;
        final String _testKindcd;
        final String _value;
        final String _credit;
        final String _recordScoreDatScore;
        
        public Record(
                final Subclass subclass,
                final String testKindcd,
                final String value,
                final String credit,
                final String recordScoreDatScore) {
            _subclass = subclass;
            _testKindcd = testKindcd;
            _value = value;
            _credit = credit;
            _recordScoreDatScore = recordScoreDatScore;
        }
        
        public String toString() {
            return "[value = " + _value + "]";
        }
    }
    
    static class Rank {
        final String _subclassCd;
        final String _testKindcd;
        final String _gradeRank;
        final String _count;
        public Rank(final String subclassCd, final String testKindcd, final String gradeRank, final String count) {
            _subclassCd = subclassCd;
            _testKindcd = testKindcd;
            _gradeRank = gradeRank;
            _count = count;
        }
    }

    /**
     * 1日出欠データ
     */
    static class Attendance {
        
        final String _lesson;
        /** 忌引 */
        final String _mourning;
        /** 出停 */
        final String _suspend;
        /** 留学 */
        final String _abroad;
        /** 出席すべき日数 */
        final String _mlesson;
        /** 公欠 */
        final String _absence;
        final String _attend;
        /** 遅刻 */
        final String _late;
        /** 早退 */
        final String _leave;
        
        public Attendance(
                final String lesson,
                final String mourning,
                final String suspend,
                final String abroad,
                final String mlesson,
                final String absence,
                final String attend,
                final String late,
                final String leave
        ) {
            _lesson = lesson;
            _mourning = mourning;
            _suspend = suspend;
            _abroad = abroad;
            _mlesson = mlesson;
            _absence = absence;
            _attend = attend;
            _late = late;
            _leave = leave;
        }
        
        public String suspendMourning() {
            if (!NumberUtils.isDigits(_suspend) && !NumberUtils.isDigits(_mourning)) {
                return null;
            }
            final int susp = NumberUtils.isDigits(_suspend) ? Integer.parseInt(_suspend) : 0;
            final int mourn = NumberUtils.isDigits(_mourning) ? Integer.parseInt(_mourning) : 0;
            return String.valueOf(susp + mourn);
        }
        
        public String toString() {
            return "[lesson=" + _lesson + 
            ",mlesson=" + _mlesson + 
            ",mourning=" + _mourning + 
            ",suspend=" + _suspend + 
            ",abroad=" + _abroad +
            ",absence=" + _absence + 
            ",attend=" + _attend + 
            ",late=" + _late +
            ",leave=" + _leave;
        }
    }
    
    private static List retDivideString(final String s, final int byteLength, final int line) {
        if (null == s) {
            return Collections.EMPTY_LIST;
        }
        return Arrays.asList(KNJ_EditEdit.get_token(s, byteLength, line));
    }
    
    private static int retMS932ByteLength(final String s) {
        int len = 0;
        if (null != s) {
            try {
                len = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }
    
    private abstract class SvfForm {
        final Vrw32alp _svf;
        SvfForm(final Vrw32alp svf) {
            _svf = svf;
        }
        public void svfVrsOutRenban(final String field, final List list) {
            for (int j = 0; j < list.size(); j++) {
                final String s = (String) list.get(j);
                _svf.VrsOut(field + (j + 1), s);
            }
        }
        public abstract void print(final Student student);
    }
    
    private class FormJunior extends SvfForm {
        FormJunior(final Vrw32alp svf) {
            super(svf);
        }
        public void print(final Student student) {
            _svf.VrSetForm("KNJD176D_2.frm", 4);
            printHeader(student);
            printAttendance(student);
            printRemark(student);
            printRecord(student);
        }

        private void printHeader(final Student student) {
            _svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
            _svf.VrsOut("HR_NAME", student._hrName);
            _svf.VrsOut("ATTENDNO", student._attendNo);
            _svf.VrsOut("NAME", student._name);
            
            _svf.VrsOut("SCHOOLNAME", _param._schoolName);
            if (retMS932ByteLength(student._hrStaffName) > 20) {
                _svf.VrsOut("TR_NAME2", student._hrStaffName);
            } else {
                _svf.VrsOut("TR_NAME1", student._hrStaffName);
            }
        }
        
        private void printRemark(final Student student) {
            svfVrsOutRenban("MESSAGE1_", student.getRemark1(34, 18, 2));
        }
        
        private void printRecord(final Student student) {
            final String[] testKindCds = new String[]{"19900", "29900", "99900"};
            for (final Iterator it = student._subclassRankMap.keySet().iterator(); it.hasNext();) {
                final String subclassCd = (String) it.next();
                final Map rankMap = student.getSubclassTestkindRankMap(subclassCd);
                for (int i = 0; i < testKindCds.length; i++) {
                    final Rank rank = (Rank) rankMap.get(testKindCds[i]);
                    if (null != rank) {
                        if (_999999.equals(subclassCd)) {
                            final String data = StringUtils.defaultString(rank._gradeRank, "") + "/" + StringUtils.defaultString(rank._count, "");
                            _svf.VrsOut("RANK" + String.valueOf(i + 1) + "_3", data);
                        } else if (_333333.equals(subclassCd)) {
                            _svf.VrsOut("RANK" + String.valueOf(i + 1) + "_1", rank._gradeRank);
                        } else if (_555555.equals(subclassCd)) {
                            _svf.VrsOut("RANK" + String.valueOf(i + 1) + "_2", rank._gradeRank);
                        }
                    }
                }
            }
            int line = 0;
            for (final Iterator it = student._subclassRecordMap.keySet().iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                line += 1;

                if (3 >= subclass._className.length()) {
                    _svf.VrAttribute("CLASS1", "Size=10.0,Keta=6,Y=720");
                }
                _svf.VrsOut("CLASS1", subclass._className);

                final String fSubclass = (retMS932ByteLength(subclass._subclassName) > 6) ? "SUBCLASS1_1" : "SUBCLASS1_2";
                _svf.VrsOut(fSubclass, subclass._subclassName);
                
                final Map subclassTestkindMap = student.getSubclassTestkindRecordMap(subclass);
                for (int i = 0; i < testKindCds.length; i++) {
                    final Record rec = (Record) subclassTestkindMap.get(testKindCds[i]);
                    if (null != rec) {
                        final String j = String.valueOf(i + 1);
                        _svf.VrsOut("RECORD" + j, rec._value);
                    }
                }
                _svf.VrEndRecord();
                _hasData = true;
            }
        }
        
        private void printAttendance(final Student student) {
            for (final Iterator it = student._attendMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final int i = SEME9.equals(semester) ? 4 : Integer.parseInt(semester);
                final Attendance att = (Attendance) student._attendMap.get(semester);
                _svf.VrsOutn("LESSON", i, att._lesson);
                _svf.VrsOutn("KIBIKI", i, att.suspendMourning());
                _svf.VrsOutn("PRESENT", i, att._mlesson);
                _svf.VrsOutn("ATTEND", i, att._attend);
                _svf.VrsOutn("ABSENCE", i, att._absence);
                _svf.VrsOutn("LATE", i, att._late);
                _svf.VrsOutn("LEAVE", i, att._leave);
            }
//            for (final Iterator it = student._attendRemark1.keySet().iterator(); it.hasNext();) {
//                final String semester = (String) it.next();
//                final int i = SEME9.equals(semester) ? 4 : Integer.parseInt(semester);
//                final String remark = (String) student._attendRemark1.get(semester);
//                svrVrsOutnRenban("MESSAGE1_", i, remark, 12, 9);
//            }
        }
    }

    private class FormHigh extends SvfForm {
        FormHigh(final Vrw32alp svf) {
            super(svf);
        }
        public void print(final Student student) {
            _svf.VrSetForm("KNJD176D_1.frm", 1);
            printHeader(student);
            printAttendance(student);
            printRemark(student);
            printRecord(student);
            _svf.VrEndPage();
            _hasData = true;
        }

        private void printHeader(final Student student) {
            _svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
            _svf.VrsOut("HR_NAME", student._hrName);
            _svf.VrsOut("ATTENDNO", student._attendNo);
            _svf.VrsOut("NAME", student._name);
            
            _svf.VrsOut("SCHOOLNAME", _param._schoolName);
            _svf.VrsOut("STAFFNAME", "　　　　　 " + student._hrStaffName);
            _svf.VrsOut("JOBNAME", "担任氏名:");
        }
        
        private void printRemark(final Student student) {
            svfVrsOutRenban("REMARK", student.getRemark1(68, 9, 1));
        }
        
        private void printRecord(final Student student) {
            final String[] testKindCds = new String[]{"19900", "29900", "99900"};
            int line = 0;
            // final int maxLine = 35;
            for (final Iterator it = student._subclassRecordMap.keySet().iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                line += 1;
                final String fClass = (retMS932ByteLength(subclass._className) > 6) ? "CLASS1" : "CLASS2";
                _svf.VrsOutn(fClass, line, subclass._className);

                final String fSubclass = (retMS932ByteLength(subclass._subclassName) > 20) ? "SUBCLASS1" : "SUBCLASS2";
                _svf.VrsOutn(fSubclass, line, subclass._subclassName);
                
                final String credit = (String) student._creditMap.get(subclass);
                if (null != credit) {
                    _svf.VrsOutn("CREDIT", line, credit); // 単位マスタの単位
                }
                
                final Map subclassTestkindMap = student.getSubclassTestkindRecordMap(subclass);
                for (int i = 0; i < testKindCds.length; i++) {
                    final Record rec = (Record) subclassTestkindMap.get(testKindCds[i]);
                    if (null != rec) {
                        final String j = String.valueOf(i + 1);
                        _svf.VrsOutn("RECORD" + j, line, rec._value);
                        if ("99900".equals(testKindCds[i])) {
                            if (!StringUtils.isEmpty(rec._credit)) {
                                _svf.VrsOutn("CREDIT", line, rec._credit); // RECORD_SCORE_DATの99900のCOMP_CREDITで上書き
                            }
                            _svf.VrsOutn("GRADING3", line, rec._recordScoreDatScore); // 評定
                        }
                    }
                }
            }
        }
        
        private void printAttendance(final Student student) {
            for (final Iterator it = student._attendMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final int i = SEME9.equals(semester) ? 4 : Integer.parseInt(semester);
                final Attendance att = (Attendance) student._attendMap.get(semester);
                _svf.VrsOutn("LESSON", i, att._lesson);
                _svf.VrsOutn("KIBIKI", i, att.suspendMourning());
                _svf.VrsOutn("PRESENT", i, att._mlesson);
                _svf.VrsOutn("ATTEND", i, att._attend);
                _svf.VrsOutn("ABSENCE", i, att._absence);
                _svf.VrsOutn("LATE", i, att._late);
                _svf.VrsOutn("LEAVE", i, att._leave);
            }
//            for (final Iterator it = student._attendRemark1.keySet().iterator(); it.hasNext();) {
//                final String semester = (String) it.next();
//                final int i = SEME9.equals(semester) ? 4 : Integer.parseInt(semester);
//                final String remark = (String) student._attendRemark1.get(semester);
//                svrVrsOutnRenban("MESSAGE1_", i, remark, 12, 9);
//            }
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _gradeHrClass;
        private final String _date;
        
        private final String[] _categorySelected;
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;
        
        private final Map _subclassMap;
        
        private String _lastSemester;
        private Map _semestername;
        
        final boolean _isJunior;
        private String _schoolName;
        private String _staffName;
        private Map _attendParamMap = new HashMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _date = request.getParameter("DATE").replace('/', '-');
            _categorySelected = request.getParameterValues("category_selected");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            
            _subclassMap = getSubclassMap(db2);
            _isJunior = setJunior(db2);
            setCertifSchool(db2);
            loadAttendAccumulateArgument(db2);
            
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }
        
        private boolean setJunior(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean isJunior = false;
            final String grade = _gradeHrClass.substring(0, 2);
            try {
                final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    isJunior = "J".equals(rs.getString("SCHOOL_KIND"));
                }
            } catch (final Exception ex) {
                log.error("学期マスタのロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return isJunior;
        }
        
        private Map getSubclassMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND AS CLASSCD ");
            } else {
                stb.append("    T3.CLASSCD AS CLASSCD ");
            }
            stb.append("    ,VALUE(T1.SUBCLASSORDERNAME2, SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append("    ,VALUE(T3.CLASSORDERNAME2, CLASSNAME) AS CLASSNAME ");
            stb.append("    ,VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_ORDER ");
            stb.append("    ,VALUE(T3.SHOWORDER3, 999) AS CLASS_ORDER ");
            stb.append(" FROM SUBCLASS_MST T1 ");
            stb.append(" LEFT JOIN CLASS_MST T3 ON ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("        T3.CLASSCD = T1.CLASSCD ");
                stb.append("        AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            } else {
                stb.append("        T3.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            Map subclassMap = new TreeMap();
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final Subclass subclass = new Subclass(rs.getString("CLASSCD"),
                            rs.getString("CLASSNAME"), subclasscd, rs.getString("SUBCLASSNAME"),
                            (Integer) rs.getObject("CLASS_ORDER"), (Integer) rs.getObject("SUBCLASS_ORDER"));
                    subclassMap.put(subclasscd, subclass);
                }
                
            } catch (final Exception ex) {
                log.error("科目マスタのロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return subclassMap;
        }
        
        public String getRegdSemester() {
            return _semester;
        }
        
        public void setCertifSchool(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String ckcd = _isJunior ? "103" : "104";
                final String sql = " SELECT * FROM CERTIF_SCHOOL_DAT T1 WHERE T1.YEAR ='" + _year + "' AND T1.CERTIF_KINDCD = '" + ckcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                }
            } catch (final Exception ex) {
                log.error("科目マスタのロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public void loadAttendAccumulateArgument(final DB2UDB db2) {
            try {
                loadSemester(db2, _year);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }

        }
        
        /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2, String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _semestername = new HashMap();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    _semestername.put(semester, name);
                    if (!SEME9.equals(semester)) {
                        _lastSemester = semester;
                    }
                }
            } catch (final Exception ex) {
                log.error("学期マスタのロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   V_SEMESTER_GRADE_MST"
                + " where"
                + "   YEAR='" + year + "'"
                + "   AND GRADE ='" + _gradeHrClass.substring(0, 2) + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }
    }
}

// eof
