/*
 * $Id: 851cbde45081cce69a958d83eb2641f049e68fe6 $
 *
 * 作成日: 2017/06/26
 * 作成者: m-yamashiro
 *
 * Copyright(C) 2017-2019 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 学校教育システム 賢者 [成績管理]  成績判定会議資料
 */

public class KNJD620Q {

    private static final Log log = LogFactory.getLog(KNJD620Q.class);

    private static final String HR_CLASS_ALL = "99999";

    private static final String COURSECODE_SPORT = "3000";
    private static final String COURSECODE_ATHLETE = "4000";
    private static final String COURSECODE_NAISHIN = "8000";
    private static final String COURSECODE_GAISHIN = "9000";

    private static final String FUTSUKA = "1";
    private static final String NAISHIN = "2";
    private static final String GAISHIN = "3";
    private static final String BUNKEI = "4";
    private static final String RIKEI = "5";
    private static final String E_BUNKEI = "6";
    private static final String E_RIKEI = "7";
    private static final String H_BUNKEI = "8";
    private static final String H_RIKEI = "9";

    private static final String SEMEALL = "9";

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
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _hasData = false;

            _param = createParam(db2, request);

            outputCsv(db2, response);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private void outputCsv(final DB2UDB db2, final HttpServletResponse response) throws SQLException {
        final String filename = _param._ctrlYear + "年度　科目別赤点一覧.csv";
        CsvUtils.outputLines(log, response, filename, getCsvOutputLine(db2));
    }

    private List getCsvOutputLine(final DB2UDB db2) throws SQLException {
        final Map hrClassMap = getHrClassMap(db2);
        final Map courseOtherMap = getCourseOtherMap();
        final List studentList = getStudentList(db2);

        //欠点データセット
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            final Student student = (Student) itStudent.next();

            //年組にセット
            if (hrClassMap.containsKey(student._grade + student._hrClass)) {
                final HrClass hrClass = (HrClass) hrClassMap.get(student._grade + student._hrClass);
                setHrUnPass(student, hrClass);

                final HrClass hrClassAll = (HrClass) hrClassMap.get(HR_CLASS_ALL);
                setHrUnPass(student, hrClassAll);
            }

            //各コース等にセット
            final CourseOther courseOtherFutu = (CourseOther) courseOtherMap.get(FUTSUKA);
            setCourseOtherUnPass(student, courseOtherFutu);
            if (!"1".equals(student._inoutcd)) {
                final CourseOther courseOther = (CourseOther) courseOtherMap.get(NAISHIN);
                setCourseOtherUnPass(student, courseOther);
            } else {
                final CourseOther courseOther = (CourseOther) courseOtherMap.get(GAISHIN);
                setCourseOtherUnPass(student, courseOther);
            }
            if ("1".equals(student._bunFlg)) {
                final CourseOther courseOther = (CourseOther) courseOtherMap.get(BUNKEI);
                setCourseOtherUnPass(student, courseOther);
            }
            if ("1".equals(student._riFlg)) {
                final CourseOther courseOther = (CourseOther) courseOtherMap.get(RIKEI);
                setCourseOtherUnPass(student, courseOther);
            }
            if ("1".equals(student._bunFlg) && "1".equals(student._eFlg)) {
                final CourseOther courseOther = (CourseOther) courseOtherMap.get(E_BUNKEI);
                setCourseOtherUnPass(student, courseOther);
            }
            if ("1".equals(student._riFlg) && "1".equals(student._eFlg)) {
                final CourseOther courseOther = (CourseOther) courseOtherMap.get(E_RIKEI);
                setCourseOtherUnPass(student, courseOther);
            }
            if ("1".equals(student._bunFlg) && "1".equals(student._hFlg)) {
                final CourseOther courseOther = (CourseOther) courseOtherMap.get(H_BUNKEI);
                setCourseOtherUnPass(student, courseOther);
            }
            if ("1".equals(student._riFlg) && "1".equals(student._hFlg)) {
                final CourseOther courseOther = (CourseOther) courseOtherMap.get(H_RIKEI);
                setCourseOtherUnPass(student, courseOther);
            }
        }

        //行List
        final List lines = new ArrayList();
        //列List
        List line = new ArrayList();

        //見出し
        line.add("科目別赤点一覧表");
        line.add(_param._ctrlYear + "年度");
        line.add(_param._gradeName);
        line.add("第" + getKi() + "期生");
        line.add(_param._testName);
        line.add(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        lines.add(line);

        //タイトル クラスとコース
        line = setOutPutTitle(hrClassMap, courseOtherMap);
        lines.add(line);

        //赤点
        line = setOutPutUnPass(hrClassMap, courseOtherMap);
        lines.add(line);

        //科目
        for (Iterator itSubclass = _param._subclassMap.keySet().iterator(); itSubclass.hasNext();) {
            final String subKey = (String) itSubclass.next();
            final Subclass subclass = (Subclass) _param._subclassMap.get(subKey);
            line = setOutPutUnPassData(hrClassMap, courseOtherMap, subclass, subKey);
            lines.add(line);
        }

        return lines;
    }

    private List setOutPutUnPassData(Map hrClassMap, Map courseOtherMap, Subclass subclass, String subKey) {
        final List line = new ArrayList();
        line.add(subclass._subclassname);
        int hrTotalCnt = 0;
        for (Iterator itHrClass = hrClassMap.keySet().iterator(); itHrClass.hasNext();) {
            final String hrClassKey = (String) itHrClass.next();
            if (HR_CLASS_ALL.equals(hrClassKey)) {
                continue;
            }
            final HrClass hrClass = (HrClass) hrClassMap.get(hrClassKey);
            if (!hrClass._subclassMap.containsKey(subKey)) {
                line.add("");
                continue;
            } else {
                final Subclass getSubclass = (Subclass) hrClass._subclassMap.get(subKey);
                if (getSubclass._unPassCnt > 0) {
                    line.add(String.valueOf(getSubclass._unPassCnt));
                } else {
                    line.add("");
                }
                hrTotalCnt += getSubclass._unPassCnt;
            }
        }
        //全体は横計
        if (hrTotalCnt > 0) {
            line.add(String.valueOf(hrTotalCnt));
        } else {
            line.add("");
        }

        for (Iterator itCourseOther = courseOtherMap.keySet().iterator(); itCourseOther.hasNext();) {
            final String courseOtherKey = (String) itCourseOther.next();
            final CourseOther courseOther = (CourseOther) courseOtherMap.get(courseOtherKey);
            if (!courseOther._subclassMap.containsKey(subKey)) {
                line.add("");
                continue;
            } else {
                final Subclass getSubclass = (Subclass) courseOther._subclassMap.get(subKey);
                if (getSubclass._unPassCnt > 0) {
                    line.add(String.valueOf(getSubclass._unPassCnt));
                } else {
                    line.add("");
                }
            }
        }
        return line;
    }

    private String getKi() {
        final int souritsuYear = Integer.parseInt(_param._foundedyear);
        final int loginYear = Integer.parseInt(_param._ctrlYear);
        final int intGrade = Integer.parseInt(_param._gradeCd);
        final int gradYear = intGrade == 3 ? loginYear : intGrade == 2 ? loginYear + 1 : loginYear + 2;
        final int retInt = gradYear - souritsuYear - 1;

        return String.valueOf(retInt);
    }

    private List setOutPutUnPass(final Map hrClassMap, final Map courseOtherMap) {
        List line;
        line = new ArrayList();
        line.add("赤点数");

        /* クラス全体用 */
        int allUnPass = 0;

        for (Iterator itHrClass = hrClassMap.keySet().iterator(); itHrClass.hasNext();) {
            final String hrClassKey = (String) itHrClass.next();
            final HrClass hrClass = (HrClass) hrClassMap.get(hrClassKey);

            if (!HR_CLASS_ALL.equals(hrClassKey)) {
                allUnPass += hrClass._unPassCnt;
                line.add(String.valueOf(hrClass._unPassCnt));
            } else {
                //全体は横計
                line.add(String.valueOf(allUnPass));
            }
        }
        for (Iterator itCourseOther = courseOtherMap.keySet().iterator(); itCourseOther.hasNext();) {
            final String courseOtherKey = (String) itCourseOther.next();
            final CourseOther courseOther = (CourseOther) courseOtherMap.get(courseOtherKey);
            line.add(String.valueOf(courseOther._unPassCnt));
        }
        return line;
    }

    private void setHrUnPass(final Student student, final HrClass hrClass) {
        final String allRecord = "99" + _param._schoolkind + "99999999";
        for (Iterator itRecord = student._recordMap.keySet().iterator(); itRecord.hasNext();) {
            final String recKey = (String) itRecord.next();
            final Score score = (Score) student._recordMap.get(recKey);
            if (!allRecord.equals(recKey)) {
                //赤点
                if (Integer.parseInt(_param._ketten) >= score._score) {
                    if (_param._subclassMap.containsKey(recKey)) {
                        hrClass._unPassCnt++;
                        if (hrClass._subclassMap.containsKey(recKey)) {
                            final Subclass subclass = (Subclass) hrClass._subclassMap.get(recKey);
                            subclass._unPassCnt++;
                        }
                    }
                }
                continue;
            }
        }
    }

    private void setCourseOtherUnPass(final Student student, final CourseOther courseOther) {
        if (COURSECODE_SPORT.equals(student._courseCode) || COURSECODE_ATHLETE.equals(student._courseCode)) {
            return;
        }
        final String allRecord = "99" + _param._schoolkind + "99999999";
        for (Iterator itRecord = student._recordMap.keySet().iterator(); itRecord.hasNext();) {
            final String recKey = (String) itRecord.next();
            final Score score = (Score) student._recordMap.get(recKey);
            if (!allRecord.equals(recKey)) {
                //赤点
                if (Integer.parseInt(_param._ketten) >= score._score) {
                    if (_param._subclassMap.containsKey(recKey)) {
                        courseOther._unPassCnt++;
                        if (courseOther._subclassMap.containsKey(recKey)) {
                            final Subclass subclass = (Subclass) courseOther._subclassMap.get(recKey);
                            subclass._unPassCnt++;
                        }
                    }
                }
                continue;
            }
        }
    }

    private List setOutPutTitle(final Map hrClassMap, final Map courseOtherMap) {
        final List line = new ArrayList();
        line.add("");
        for (Iterator itHrClass = hrClassMap.keySet().iterator(); itHrClass.hasNext();) {
            final String hrClassKey = (String) itHrClass.next();
            final HrClass hrClass = (HrClass) hrClassMap.get(hrClassKey);
            line.add(hrClass._abbv);
        }
        for (Iterator itCourseOther = courseOtherMap.keySet().iterator(); itCourseOther.hasNext();) {
            final String courseOtherKey = (String) itCourseOther.next();
            final CourseOther courseOther = (CourseOther) courseOtherMap.get(courseOtherKey);
            line.add(courseOther._abbv);
        }
        return line;
    }

    private Map getHrClassMap(final DB2UDB db2) throws SQLException {
        final Map retMap = new TreeMap();
        final String hrClassSql = getHrClassSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(hrClassSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String hrNameAbbv = rs.getString("HR_NAMEABBV");
                final HrClass hrClassObj = new HrClass(grade, hrClass, hrName, hrNameAbbv);
                final String hrKey = grade + hrClass;
                retMap.put(hrKey, hrClassObj);
            }
            final HrClass hrClassObj = new HrClass("99", "999", "全体", "全体");
            retMap.put(HR_CLASS_ALL, hrClassObj);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getHrClassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGDH.GRADE, ");
        stb.append("     REGDH.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGDH.HR_NAMEABBV ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("           AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("           AND REGD.GRADE = REGDH.GRADE ");
        stb.append("           AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semeFlg + "' ");
        stb.append("     AND REGD.GRADE = '" + _param._grade + "' ");
        stb.append("     AND REGD.COURSECD || '-' || REGD.MAJORCD = '" + _param._major + "' ");
        stb.append(" GROUP BY ");
        stb.append("     REGDH.GRADE, ");
        stb.append("     REGDH.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGDH.HR_NAMEABBV ");
        stb.append(" ORDER BY ");
        stb.append("     REGDH.GRADE, ");
        stb.append("     REGDH.HR_CLASS ");

        return stb.toString();
    }

    private Map getCourseOtherMap() throws SQLException {
        final Map retMap = new TreeMap();
        final CourseOther courseOther1 = new CourseOther("普通科", "普通科");
        retMap.put(FUTSUKA, courseOther1);
        final CourseOther courseOther2 = new CourseOther("内進", "内進");
        retMap.put(NAISHIN, courseOther2);
        final CourseOther courseOther3 = new CourseOther("外進", "外進");
        retMap.put(GAISHIN, courseOther3);
        final CourseOther courseOther4 = new CourseOther("文系", "文系");
        retMap.put(BUNKEI, courseOther4);
        final CourseOther courseOther5 = new CourseOther("理系", "理系");
        retMap.put(RIKEI, courseOther5);
        final CourseOther courseOther6 = new CourseOther("E文", "E文");
        retMap.put(E_BUNKEI, courseOther6);
        final CourseOther courseOther7 = new CourseOther("E理", "E理");
        retMap.put(E_RIKEI, courseOther7);
        final CourseOther courseOther8 = new CourseOther("H文", "H文");
        retMap.put(H_BUNKEI, courseOther8);
        final CourseOther courseOther9 = new CourseOther("H理", "H理");
        retMap.put(H_RIKEI, courseOther9);
        return retMap;
    }

    private List getStudentList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String studentSql = getStudentSql();
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;

        PreparedStatement psRecord = null;
        ResultSet rsRecord = null;
        final String sqlRecord = getRecordSql();
        log.debug(" sqlRecord = " + sqlRecord);

        try {
            psStudent = db2.prepareStatement(studentSql);
            rsStudent = psStudent.executeQuery();

            psRecord = db2.prepareStatement(sqlRecord);
            while (rsStudent.next()) {
                final String grade = rsStudent.getString("GRADE");
                final String hrClass = rsStudent.getString("HR_CLASS");
                final String attendno = rsStudent.getString("ATTENDNO");
                final String schregno = rsStudent.getString("SCHREGNO");
                final String inoutcd = rsStudent.getString("INOUTCD");
                final String courseCode = rsStudent.getString("COURSECODE");
                final String coursecodename = rsStudent.getString("COURSECODENAME");
                final String bunFlg = rsStudent.getString("BUN_FLG");
                final String riFlg = rsStudent.getString("RI_FLG");
                final String hrName = rsStudent.getString("HR_NAME");
                final String eFlg = rsStudent.getString("E_FLG");
                final String hFlg = rsStudent.getString("H_FLG");
                final String name = rsStudent.getString("NAME");
                final Student student = new Student(grade, hrClass, attendno, schregno, inoutcd, courseCode, coursecodename, bunFlg, riFlg, hrName, eFlg, hFlg, name);

                psRecord.setString(1, student._schregno);
                rsRecord = psRecord.executeQuery();
                while (rsRecord.next()) {
                    final String classcd = rsRecord.getString("CLASSCD");
                    final String schoolKind = rsRecord.getString("SCHOOL_KIND");
                    final String curriculumCd = rsRecord.getString("CURRICULUM_CD");
                    final String subclasscd = rsRecord.getString("SUBCLASSCD");
                    final String subKey = classcd + schoolKind + curriculumCd + subclasscd;
                    final int score = rsRecord.getInt("SCORE");
                    final double avg = rsRecord.getDouble("AVG");
                    final Score scoreObj = new Score(score, avg);
                    student._recordMap.put(subKey, scoreObj);
                }
                retList.add(student);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, psStudent, rsStudent);
            DbUtils.closeQuietly(null, psRecord, rsRecord);
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.SCHREGNO, ");
        stb.append("     VALUE(BASE.INOUTCD, '0') AS INOUTCD, ");
        stb.append("     REGD.COURSECODE, ");
        stb.append("     COURSE_M.COURSECODENAME, ");
        stb.append("     CASE WHEN COURSE_M.COURSECODENAME LIKE '%文%' ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '0' ");
        stb.append("     END AS BUN_FLG, ");
        stb.append("     CASE WHEN COURSE_M.COURSECODENAME LIKE '%理%' ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '0' ");
        stb.append("     END AS RI_FLG, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     CASE WHEN REGDH.HR_NAME LIKE '%E%' ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '0' ");
        stb.append("     END AS E_FLG, ");
        stb.append("     CASE WHEN REGDH.HR_NAME LIKE '%H%' ");
        stb.append("          THEN '1' ");
        stb.append("          ELSE '0' ");
        stb.append("     END AS H_FLG, ");
        stb.append("     BASE.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT  REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("           AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("           AND REGD.GRADE = REGDH.GRADE ");
        stb.append("           AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSE_M ON REGD.COURSECODE = COURSE_M.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semeFlg + "' ");
        stb.append("     AND REGD.GRADE = '" + _param._grade + "' ");
        stb.append("     AND REGD.COURSECD || '-' || REGD.MAJORCD = '" + _param._major + "' ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private String getRecordSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RANK_SDIV.CLASSCD, ");
        stb.append("     RANK_SDIV.SCHOOL_KIND, ");
        stb.append("     RANK_SDIV.CURRICULUM_CD, ");
        stb.append("     RANK_SDIV.SUBCLASSCD, ");
        stb.append("     RANK_SDIV.SCHREGNO, ");
        stb.append("     RANK_SDIV.SCORE, ");
        stb.append("     RANK_SDIV.AVG ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_SDIV_DAT RANK_SDIV ");
        stb.append(" WHERE ");
        stb.append("     RANK_SDIV.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND RANK_SDIV.SEMESTER = '" + _param._semeFlg + "' ");
        stb.append("     AND RANK_SDIV.TESTKINDCD || RANK_SDIV.TESTITEMCD || RANK_SDIV.SCORE_DIV = '" + _param._testkindcd + "' ");
        stb.append("     AND RANK_SDIV.SCHREGNO = ? ");
        return stb.toString();
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

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 60511 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _major;
        final String _grade;
        final String _testkindcd;
        final String _sdate;
        final String _date;
        final String _ketten;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _loginDate;
        final String _prgid;
        final String _cmd;
        final String _yearSdate;
        final String _semeSdate;
        final String _semeEdate;
        final String _semeFlg;
        final String _usecurriculumcd;
        final String _useclassdetaildat;
        final String _usevirus;
        final String _usekekkajisu;
        final String _usekekka;
        final String _uselatedetail;
        final String _usekoudome;
        final String _useSchregnoHyoji;
        final String _useSchoolDetailGcmDat;
        final String _useschoolKindfield;
        final String _schoolkind;
        final String _schoolcd;
        final String _usePrgSchoolkind;
        final String _selectschoolkind;
        final String _printLogStaffcd;
        final Map _subclassMap;
        final String _semesterName;
        final String _gradeName;
        String _gradeCd;
        final String _foundedyear;
        final String _testName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _major = request.getParameter("MAJOR");
            _grade = request.getParameter("GRADE");
            _testkindcd = request.getParameter("TESTKINDCD");
            _sdate = request.getParameter("SDATE");
            _date = request.getParameter("DATE");
            _ketten = request.getParameter("KETTEN");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _cmd = request.getParameter("cmd");
            _yearSdate = request.getParameter("YEAR_SDATE");
            _semeSdate = request.getParameter("SEME_SDATE");
            _semeEdate = request.getParameter("SEME_EDATE");
            _semeFlg = request.getParameter("SEME_FLG");
            _usecurriculumcd = request.getParameter("useCurriculumcd");
            _useclassdetaildat = request.getParameter("useClassDetailDat");
            _usevirus = request.getParameter("useVirus");
            _usekekkajisu = request.getParameter("useKekkaJisu");
            _usekekka = request.getParameter("useKekka");
            _uselatedetail = request.getParameter("useLatedetail");
            _usekoudome = request.getParameter("useKoudome");
            _useSchregnoHyoji = request.getParameter("use_SchregNo_hyoji");
            _useSchoolDetailGcmDat = request.getParameter("use_school_detail_gcm_dat");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _schoolkind = getSchoolKind(db2);
            _schoolcd = request.getParameter("SCHOOLCD");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _selectschoolkind = request.getParameter("selectSchoolKind");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _subclassMap = getSubclassMap(db2);
            _semesterName = getSemesterName(db2);
            _gradeName = getGradeName(db2);
            _foundedyear = getFoundedYear(db2);
            _testName = getTestName(db2);

        }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            final String semesterSql = getSemesterSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(semesterSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSemesterSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTERNAME ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");

            return stb.toString();
        }

        private String getGradeName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            final String gradeSql = getGradeSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(gradeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("GRADE_NAME1");
                    _gradeCd = rs.getString("GRADE_CD");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getGradeSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE_CD, ");
            stb.append("     GRADE_NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND GRADE = '" + _grade + "' ");

            return stb.toString();
        }

        private String getSchoolKind(DB2UDB db2) throws SQLException {
            String retStr = "";
            final String subSchoolKindSql = getSchoolKindSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subSchoolKindSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("SCHOOL_KIND");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getSchoolKindSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GDAT.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT GDAT ");
            stb.append(" WHERE ");
            stb.append("     GDAT.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND GDAT.GRADE = '" + _grade + "' ");
            return stb.toString();
        }

        private String getFoundedYear(final DB2UDB db2) throws SQLException {
            String retStr = "0";
            final String foundedYearSql = getFoundedYearSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(foundedYearSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("FOUNDEDYEAR");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getFoundedYearSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     FOUNDEDYEAR ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SCHOOL_KIND = '" + _schoolkind + "' ");

            return stb.toString();
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            final String testNameSql = getTestNameSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(testNameSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("TESTITEMNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getTestNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTITEMNAME ");
            stb.append(" FROM ");
            stb.append("     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SCHOOL_KIND = '" + _schoolkind + "' ");
            stb.append("     AND SEMESTER = '" + _semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testkindcd + "' ");
            stb.append("     AND GRADE = '" + _grade + "' ");
            stb.append("     AND COURSECD || '-' || MAJORCD = '" + _major + "' ");

            return stb.toString();
        }

        private Map getSubclassMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            final String subClassSql = getSubclassSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subClassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");
                    final Subclass subclass = new Subclass(classcd, schoolKind, curriculumCd, subclasscd, subclassname, subclassabbv);
                    final String subKey = classcd + schoolKind + curriculumCd + subclasscd;
                    retMap.put(subKey, subclass);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private String getSubclassSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SUBM.CLASSCD, ");
            stb.append("     SUBM.SCHOOL_KIND, ");
            stb.append("     SUBM.CURRICULUM_CD, ");
            stb.append("     SUBM.SUBCLASSCD, ");
            stb.append("     SUBM.SUBCLASSNAME, ");
            stb.append("     SUBM.SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN CHAIR_STD_DAT CSTD ON REGD.YEAR = CSTD.YEAR ");
            stb.append("           AND REGD.SEMESTER = CSTD.SEMESTER ");
            stb.append("           AND REGD.SCHREGNO = CSTD.SCHREGNO ");
            stb.append("     INNER JOIN CHAIR_DAT CHAIR ON CSTD.YEAR = CHAIR.YEAR ");
            stb.append("           AND CSTD.SEMESTER = CHAIR.SEMESTER ");
            stb.append("           AND CSTD.CHAIRCD = CHAIR.CHAIRCD ");
            stb.append("     INNER JOIN SUBCLASS_MST SUBM ON CHAIR.CLASSCD = SUBM.CLASSCD ");
            stb.append("           AND CHAIR.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stb.append("           AND CHAIR.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append("           AND CHAIR.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stb.append("           AND SUBM.CLASSCD <= '90' ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + _semeFlg + "' ");
            stb.append("     AND REGD.GRADE = '" + _grade + "' ");
            stb.append("     AND REGD.COURSECD || '-' || REGD.MAJORCD = '" + _major + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SUBM.CLASSCD, ");
            stb.append("     SUBM.SCHOOL_KIND, ");
            stb.append("     SUBM.CURRICULUM_CD, ");
            stb.append("     SUBM.SUBCLASSCD, ");
            stb.append("     SUBM.SUBCLASSNAME, ");
            stb.append("     SUBM.SUBCLASSABBV ");
            stb.append(" ORDER BY ");
            stb.append("     SUBM.CLASSCD, ");
            stb.append("     SUBM.SCHOOL_KIND, ");
            stb.append("     SUBM.CURRICULUM_CD, ");
            stb.append("     SUBM.SUBCLASSCD ");
            return stb.toString();
        }
    }

    /** SUBCLASSクラス */
    private class Subclass {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        int _unPassCnt = 0;

        public Subclass(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final String subclassabbv
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
        }

        public String toString() {
            return _classcd + _schoolKind + _curriculumCd + _subclasscd + "[" + _subclassname + "]" + " : " + " 赤点人数=" + _unPassCnt;
        }
    }

    /** 得点クラス */
    private class Score {
        final int _score;
        final double _avg;

        public Score(
                final int score,
                final double avg
        ) {
            _score = score;
            _avg = avg;
        }
    }

    /** HRクラス */
    private class HrClass {
        final String _grade;
        final String _hrClass;
        final String _name;
        final String _abbv;
        final Map _subclassMap;
        int _unPassCnt = 0;

        public HrClass(
                final String grade,
                final String hrClass,
                final String name,
                final String abbv
        ) throws SQLException {
            _grade = grade;
            _hrClass = hrClass;
            _name = name;
            _abbv = abbv;
            _subclassMap = setSubclassMap();
        }

        private Map setSubclassMap() {
            final Map retMap = new HashMap();
            for (Iterator itSubMap = _param._subclassMap.keySet().iterator(); itSubMap.hasNext();) {
                final String subKey = (String) itSubMap.next();
                final Subclass paraSub = (Subclass) _param._subclassMap.get(subKey);
                final Subclass setSub = new Subclass(paraSub._classcd, paraSub._schoolKind, paraSub._curriculumCd, paraSub._subclasscd, paraSub._subclassname, paraSub._subclassabbv);
                retMap.put(subKey, setSub);
            }
            return retMap;
        }

    }

    /** コース他クラス */
    private class CourseOther {
        final String _name;
        final String _abbv;
        final Map _subclassMap;
        int _unPassCnt = 0;

        public CourseOther(
                final String name,
                final String abbv
        ) {
            _name = name;
            _abbv = abbv;
            _subclassMap = setSubclassMap();
        }

        private Map setSubclassMap() {
            final Map retMap = new HashMap();
            for (Iterator itSubMap = _param._subclassMap.keySet().iterator(); itSubMap.hasNext();) {
                final String subKey = (String) itSubMap.next();
                final Subclass paraSub = (Subclass) _param._subclassMap.get(subKey);
                final Subclass setSub = new Subclass(paraSub._classcd, paraSub._schoolKind, paraSub._curriculumCd, paraSub._subclasscd, paraSub._subclassname, paraSub._subclassabbv);
                retMap.put(subKey, setSub);
            }
            return retMap;
        }

        public String toString() {
            return _name;
        }
    }

    /** Studentクラス */
    private class Student {
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _schregno;
        final String _inoutcd;
        final String _courseCode;
        final String _coursecodename;
        final String _bunFlg;
        final String _riFlg;
        final String _hrName;
        final String _eFlg;
        final String _hFlg;
        final String _name;
        final Map _recordMap;

        public Student(
                final String grade,
                final String hrClass,
                final String attendno,
                final String schregno,
                final String inoutcd,
                final String courseCode,
                final String coursecodename,
                final String bunFlg,
                final String riFlg,
                final String hrName,
                final String eFlg,
                final String hFlg,
                final String name
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _schregno = schregno;
            _inoutcd = inoutcd;
            _courseCode = courseCode;
            _coursecodename = coursecodename;
            _bunFlg = bunFlg;
            _riFlg = riFlg;
            _hrName = hrName;
            _eFlg = eFlg;
            _hFlg = hFlg;
            _name = name;
            _recordMap = new HashMap();
        }
    }

}

// eof

