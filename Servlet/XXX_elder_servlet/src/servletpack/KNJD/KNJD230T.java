// kanji=漢字
/*
 * $Id: 216abe8971bcb11bcbc76b76f7e5b327fe15e272 $
 *
 * 作成日: 2010/03/05
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.BufferedOutputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] KNJD230T 成績一覧表（年度末）
 */

public class KNJD230T {

    private static final Log log = LogFactory.getLog(KNJD230T.class);
    
    private Param _param;

    /**
     * KNJD.classから最初に起動されるクラス
     * 
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception IO例外
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        final Vrw32alp svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean nonedata = false;
        DB2UDB db2 = null;
        BufferedOutputStream outstrm = null;

        try {
            // print svf設定
            response.setContentType("application/pdf");
            outstrm = new BufferedOutputStream(response.getOutputStream());
            svf.VrInit();
            svf.VrSetSpoolFileStream(outstrm);
            svf.VrSetForm("KNJD230T.frm", 1);

            // ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (db2 == null) {
                log.error("db error");
                return;
            }
            db2.open();

            // パラメータの取得
            _param = createParam(request);
            _param.load(db2);

            // 印刷処理
            nonedata = printSvf(db2, svf);
        } catch (Exception ex) {
            log.debug("exception!", ex);
        }

        if (!nonedata) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note", "note");
            svf.VrEndPage();
        }
        svf.VrQuit();
        outstrm.close();
        svf.close();

        db2.commit();
        db2.close();
    }
    
    private boolean printSvf(final DB2UDB db2, final Vrw32alp svf) {
        
        final List list = createDataList(db2);
        if (list.size() == 0) {
            return false;
        }
        return printList(svf, list);
    }
    
    private void printHeader(final Vrw32alp svf, final int page, final int maxPage) {
        svf.VrsOut("PAGE", String.valueOf(page) + "/" + String.valueOf(maxPage));
        svf.VrsOut("YEAR", KNJ_EditDate.h_format_JP_N(_param._year + "-01-01") + "度");
        svf.VrsOut("PRINT_DAY", KNJ_EditDate.h_format_JP(_param._loginDate));
        svf.VrsOut("GRADE", _param.getGradeName());
    }

    private boolean printList(final Vrw32alp svf, final List list) {
        
        final int columnMax = 4; // 1ページあたりの列数
        final int rowMax = 60; // 1ページあたりの行数
        //int countPerPage = rowMax * columnMax;
        
        boolean nonedata = false;
        boolean newPage = false;
        int page = 1;
        final int maxPage = (list.size() % (columnMax * rowMax) == 0) ?
                list.size() / (columnMax * rowMax) :
                    list.size() / (columnMax * rowMax) + 1;
        
        int row = 1;
        int column = 1;
        for (Iterator it = list.iterator(); it.hasNext();) {
            nonedata = true;
            newPage = false;
            printHeader(svf, page, maxPage);
            
            final OutputData outputData = (OutputData) it.next();
            log.debug(outputData);
            outputData.output(svf, column, row);
            
            row += 1;
            
            if (row > rowMax) { // 列を変更する
                row = 1;
                column += 1;
            }
            if (column > columnMax) { // 改ページ
                svf.VrEndPage();
                column = 1;
                page += 1;
                newPage = true;
            }
        }
        if (!newPage) {
            svf.VrEndPage();
        }

        return nonedata;
    }

    private List createDataList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        final String sql = sqlData();
        log.debug("set Data sql = " + sql);
        try {
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            while(rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendNo = rs.getString("ATTENDNO");
                final String courseName = rs.getString("COURSENAME");
                final String name = rs.getString("NAME");
                final Student student = findStudent(studentList, schregno, hrClass, attendNo, courseName, name);
                final String subClassCd = rs.getString("SUBCLASSCD");
                final String compCredit = rs.getString("COMP_CREDIT");
                final String getCredit = rs.getString("GET_CREDIT");
                final String remark = rs.getString("REMARK");
                
                String gradeValue = "";
                if (rs.getString("GRAD_VALUE") != null) {
                    gradeValue = rs.getString("GRAD_VALUE");
                } else if (rs.getString("GRAD_VALUE_DI") != null) {
                    gradeValue = "(" + rs.getString("GRAD_VALUE_DI") + ")";
                }
                    
                final SubClass subClass = new SubClass(subClassCd, remark, compCredit, getCredit, gradeValue);
                student.addSubClass(subClass);
            }
        } catch (SQLException e) {
            log.debug("exception!", e);
        }
        
        try {
            final Map hasuuMap = AttendAccumulate.getHasuuMap(_param._attendSemAllMap, _param._semesterStartDate, _param._attendDate);
            final boolean semesFlg = ((Boolean) hasuuMap.get("semesFlg")).booleanValue();

            for (int i = 0; i < _param._hrClasses.length; i++) {
                final String hrClass = _param._hrClasses[i].substring(2, 5);
                final String attendSql = AttendAccumulate.getAttendSubclassSql(
                        semesFlg,
                        _param._defineCode,
                        _param._defineSchool,
                        _param._knjSchoolMst,
                        _param._year,
                        _param.SSEMESTER,
                        _param._semester,
                        (String) hasuuMap.get("attendSemesInState"),
                        _param._periodInState,
                        (String) hasuuMap.get("befDayFrom"),
                        (String) hasuuMap.get("befDayTo"),
                        (String) hasuuMap.get("aftDayFrom"),
                        (String) hasuuMap.get("aftDayTo"),
                        _param._grade,
                        hrClass,
                        null,
                        "1",
                        _param._useCurriculumcd,
                        _param._useVirus,
                        _param._useKoudome);
                log.debug("attendSql = " + attendSql);
                final PreparedStatement ps = db2.prepareStatement(attendSql);
                final ResultSet rs = ps.executeQuery(); 
                while(rs.next()) {
                    String semester = rs.getString("SEMESTER");
                    if (!_param._semester.equals(semester)) { // 表示対象でない
                        continue;
                    }
                    
                    BigDecimal absent = rs.getBigDecimal("ABSENT_SEM");
                    if (absent.intValue() == 0) { // 欠課数が0なら表示しない
                        continue;
                    }
                    
                    final String schregno = rs.getString("SCHREGNO");
                    Student student = null;
                    for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        final Student st = (Student) it.next();
                        if (st._schregno.equals(schregno)) {
                            student = st;
                            break;
                        }
                    }
                    if (student == null) { // 対象の生徒ではない
                        continue;
                    }
                    final SubClass subClass = student.getSubClass(rs.getString("SUBCLASSCD"));
                    if (subClass == null) { // 対象の科目ではない
                        continue;
                    }
                    
                    if (subClass._absent != null) {
                        log.debug(student._name + " , " + subClass + " + " + absent);
                        absent = absent.add(subClass._absent);
                    }
                    subClass.setAbsent(absent);
                }
                
                DbUtils.closeQuietly(null, ps, rs);
            }
            
        } catch (Exception e) {
            log.error("exception!", e);
        }
        
        final List rtnList = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            Student student = (Student) it.next();
            rtnList.addAll(student.getOutputDataList());
        }
        
        return rtnList;
    }
    
    /**
     * 学籍番号=schregno、HR=hrClass、出席番号=attendNo、名前=nameの生徒をリストから得る。生徒がリストに無い場合は作成する。
     * @param list 生徒のリスト
     * @param hrClass HR
     * @param schregno 学籍番号
     * @param attendNo 出席番号
     * @param name 名前
     * @param createNew 新規作成フラグ
     * @return 学籍番号=schregno、HR=hrClass、出席番号=attendNo、名前=nameの生徒
     */
    private final Student findStudent(final List list, final String schregno, final String hrClass, final String attendNo, final String courseName, final String name) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._schregno.equals(schregno)) {
                return student;
            }
        }
        
        final Student student = new Student(schregno, hrClass, attendNo, courseName, name);
        list.add(student);
        
        return student;
    }

    /* 対象データのsql */
    private String sqlData() {
        StringBuffer stb = new StringBuffer();
        stb.append(" with t_chair as ( ");
        stb.append("     SELECT ");
        stb.append("         w1.year, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     w1.classcd || '-' || w1.school_kind || '-' || w1.curriculum_cd || '-' || ");
        }
        stb.append("         w1.subclasscd AS subclasscd, ");
        stb.append("         w2.schregno ");
        stb.append("     FROM ");
        stb.append("         chair_dat w1, ");
        stb.append("         chair_std_dat w2 ");
        stb.append("     WHERE ");
        stb.append("         w1.year='" + _param._year + "' AND ");
        stb.append("        (w1.subclasscd not like '9%' OR w1.subclasscd like '90%') AND ");
        stb.append("         w2.year=w1.year AND ");
        stb.append("         w2.semester=w1.semester AND ");
        stb.append("         w2.chaircd=w1.chaircd ");
        stb.append("     GROUP BY ");
        stb.append("         w1.year, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     w1.classcd || '-' || w1.school_kind || '-' || w1.curriculum_cd || '-' || ");
        }
        stb.append("         w1.subclasscd, ");
        stb.append("         w2.schregno ");
        stb.append("     ) ");

        stb.append(" SELECT");
        stb.append("    T1.SCHREGNO,");
        stb.append("    T1.SUBCLASSCD,");
        stb.append("    T2.HR_CLASS,");
        stb.append("    T2.ATTENDNO,");
        stb.append("    T2.COURSECD,");
        stb.append("    L1.COURSENAME,");
        stb.append("    T4.NAME,");
        stb.append("    (SELECT N1.NAME1 FROM NAME_MST N1 WHERE N1.NAMECD1 = 'D022' AND N1.NAMECD2 = L2.RECOGNITION_FLG) AS REMARK,");
        stb.append("    L3.COMP_CREDIT,");
        stb.append("    L3.GET_CREDIT,");
        stb.append("    L3.GRAD_VALUE_DI,");
        stb.append("    L3.GRAD_VALUE");
        stb.append(" FROM");
        stb.append("    t_chair T1");
        stb.append("    INNER JOIN SCHREG_REGD_DAT T2 ON");
        stb.append("        T2.YEAR = T1.YEAR");
        stb.append("        AND T2.SEMESTER = '" + _param._loginSemester + "'");
        stb.append("        AND T2.SCHREGNO = T1.SCHREGNO");
        stb.append("    INNER JOIN SCHREG_BASE_MST T4 ON");
        stb.append("        T4.SCHREGNO = T1.SCHREGNO");
        stb.append("    LEFT JOIN COURSE_MST L1 ON");
        stb.append("        L1.COURSECD = T2.COURSECD");
        stb.append("    LEFT JOIN RECORD_REMARK_DAT L2 ON");
        stb.append("        L2.YEAR = T1.YEAR");
        stb.append("        AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
        }
        stb.append(             " L2.SUBCLASSCD = T1.SUBCLASSCD");
        stb.append("        AND L2.SCHREGNO = T1.SCHREGNO");
        stb.append("    LEFT JOIN RECORD_DAT L3 ON");
        stb.append("        L3.YEAR = T1.YEAR");
        stb.append("        AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || ");
        }
        stb.append(             "L3.SUBCLASSCD = T1.SUBCLASSCD");
        stb.append("        AND L3.SCHREGNO = T1.SCHREGNO");
        stb.append(" WHERE");
        stb.append("    T2.YEAR = '" + _param._year + "'");
        stb.append("    AND T2.GRADE = '" + _param._grade + "' ");
        stb.append("    AND T2.GRADE || T2.HR_CLASS IN " + SQLUtils.whereIn(true, _param._hrClasses));
        stb.append(" ORDER BY");
        stb.append("    T2.HR_CLASS, T2.ATTENDNO");
        return stb.toString();
    }
    
    Param createParam(final HttpServletRequest req) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        for (final Enumeration en = req.getParameterNames(); en.hasMoreElements();) {
            String parameterName = (String) en.nextElement();
            log.debug("parameter " + parameterName + " = " + req.getParameter(parameterName));
        }
        final String year = req.getParameter("YEAR");
        final String semester = req.getParameter("SEMESTER");
        final String testKindCd = req.getParameter("TESTKINDCD");
        final String grade = req.getParameter("GRADE");
        final String[] hrClasses = req.getParameterValues("CLASS_SELECTED");
        final String loginSemester = req.getParameter("CTRL_SEMESTER");
        final String loginDate = req.getParameter("LOGIN_DATE");
        final String attendDate = req.getParameter("DATE");
        final boolean useTestitemMstCountflgNew = "TESTITEM_MST_COUNTFLG_NEW".equals(req.getParameter("COUNTFLG"));
        final String useCurriculumcd = req.getParameter("useCurriculumcd");
        final String useVirus = req.getParameter("useVirus");
        final String useKoudome = req.getParameter("useKoudome");

        return new Param(year, semester, testKindCd, grade, hrClasses, loginSemester, loginDate, attendDate, useTestitemMstCountflgNew, useCurriculumcd, useVirus, useKoudome);
    }
        
    private class Param {
        final String _year;
        final String _semester;
        final String _testKindCd;
        final String _loginSemester;
        final String _loginDate;
        final String _attendDate;
        final String _grade;
        final String[] _hrClasses;
        final Map _hrClassMap;
        final Map _subClassMap;

        final boolean _useTestitemMstCountflgNew;
        final String SSEMESTER = "1";
        
        final KNJDefineCode _defineCode = new KNJDefineCode();
        final KNJDefineSchool _defineSchool = new KNJDefineSchool();
        
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;

        String _semesterName;
        String _gradeName;
        String _testItemName;

        KNJSchoolMst _knjSchoolMst;
        Map _attendSemAllMap;
        String _periodInState;
        String _semesterStartDate;

        public Param(
                final String year,
                final String semester,
                final String testKindCd,
                final String grade,
                final String[] hrClassCds,
                final String loginSemester,
                final String loginDate,
                final String attendDate,
                final boolean useTestitemMstCountflgNew,
                final String useCurriculumcd,
                final String useVirus,
                final String useKoudome) {
            _year = year;
            _semester = semester;
            _testKindCd = testKindCd;
            _grade = grade;
            _hrClasses = hrClassCds;
            _loginSemester = loginSemester;
            _loginDate = loginDate;
            _attendDate = attendDate;
            
            _hrClassMap = new HashMap();
            _subClassMap = new HashMap();
            
            _useTestitemMstCountflgNew = useTestitemMstCountflgNew; 
            _useCurriculumcd = useCurriculumcd;
            _useVirus = useVirus;
            _useKoudome = useKoudome;
        }
        
        private void load(final DB2UDB db2) {
            try {
                String z010 = getZ010(db2);
                
                _defineCode.defineCode(db2, _year);
                _defineSchool.defineCode(db2, _year);         //各学校における定数等設定
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
                
                setSemesterStartDate(db2, SSEMESTER);
                _attendSemAllMap = AttendAccumulate.getAttendSemesMap(db2, z010, _year);
                _periodInState = AttendAccumulate.getPeiodValue(db2, _defineCode, _year, SSEMESTER, _semester);

                setSemesterName(db2);
                setHomeRoomMap(db2);
                setTestItemName(db2);
                setSubClassMap(db2);
                setGradeName(db2);
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }

        private void setSemesterName(DB2UDB db2) {
            final String sql = sqlSemester();
            log.debug("set Semester sql = " + sql);
            try {
                db2.query(sql);
                ResultSet rs = db2.getResultSet();
                if (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex){
                log.debug("exception! sql = " + sql, ex);
            }
        }

        private String sqlSemester() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT");
            stb.append("     T1.SEMESTER");
            stb.append("    ,T1.SEMESTERNAME");
            stb.append(" FROM");
            stb.append("     SEMESTER_MST T1");
            stb.append(" WHERE");
            stb.append("     T1.YEAR = '" + _year + "'");
            stb.append("     AND T1.SEMESTER = '" + _semester + "'");
            return stb.toString();
        }

        private String getZ010(DB2UDB db2) {
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010'";
                db2.query(sql);
                ResultSet rs = db2.getResultSet();
                if (rs.next()) {
                   return rs.getString("NAME1"); 
                }
            } catch (Exception ex) {
                log.debug("exception!", ex);
            }
            return null;
        }

        private void setHomeRoomMap(final DB2UDB db2) {
            final String sql = sqlHomeRoom();
            log.debug("set HomeRoom sql = " + sql);
            try {
                db2.query(sql);
                ResultSet rs = db2.getResultSet();
                while (rs.next()) {
                    String hrClass = rs.getString("HR_CLASS");
                    String abbv = rs.getString("HR_NAMEABBV");
                    log.debug("HR " + hrClass + " = " + abbv);
                    _hrClassMap.put(hrClass, abbv);
                }
            } catch (SQLException ex){
                log.debug("exception! sql = " + sql, ex);
            }
        }

        private String sqlHomeRoom() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT");
            stb.append("     T1.HR_CLASS");
            stb.append("    ,T1.HR_NAMEABBV");
            stb.append(" FROM");
            stb.append("     SCHREG_REGD_HDAT T1");
            stb.append(" WHERE");
            stb.append("     T1.YEAR = '" + _year + "'");
            stb.append("     AND T1.SEMESTER = '" + _loginSemester + "'");
            stb.append("     AND T1.GRADE = '" + _grade + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _hrClasses));
            return stb.toString();
        }
        
        public String getSubClassAbbv(final String subClassCd) {
            return (String) _subClassMap.get(subClassCd);
        }

        private void setSubClassMap(final DB2UDB db2) {
            final String sql = sqlSubClass();
            try {
                db2.query(sql);
                ResultSet rs = db2.getResultSet();
                while (rs.next()) {
                    String hrClass = rs.getString("SUBCLASSCD");
                    String abbv = rs.getString("SUBCLASSABBV");
                    _subClassMap.put(hrClass, abbv);
                }
            } catch (SQLException ex){
                log.debug("exception! sql = " + sql, ex);
            }
        }
        
        private String sqlSubClass() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T1.SUBCLASSCD AS SUBCLASSCD");
            stb.append("    ,T1.SUBCLASSABBV");
            stb.append(" FROM");
            stb.append("     SUBCLASS_MST T1");
            return stb.toString();
        }

        public String getTestItemName() {
            return _semesterName + _testItemName;
        }
        
        public String getTargetValueField() {
            final String sem = "SEM" + _semester + "_";
            String item = null;
            final String testKind = _testKindCd.substring(0, 2);
            if ("01".equals(testKind)) {
                item = "INTR_";
            } else if ("02".equals(testKind)) {
                item = "TERM_";
            }
            return sem + item + "VALUE";
        }

        private void setTestItemName(final DB2UDB db2) {
            final String sql = sqlTestitemMstCountflg();
            log.debug("set TestItemName sql = " + sql);
            try {
                db2.query(sql);
                ResultSet rs = db2.getResultSet();
                if (rs.next()) {
                    _testItemName = rs.getString("TESTITEMNAME");
                }
            } catch (SQLException ex){
                log.debug("exception! sql = " + sql, ex);
            }
        }
        
        
        private String sqlTestitemMstCountflg() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT");
            stb.append("     T1.TESTITEMNAME");
            stb.append(" FROM");
            stb.append("     " + getTestItemMstCountFlgTableName() + " T1");
            stb.append(" WHERE");
            stb.append("     T1.TESTKINDCD || T1.TESTITEMCD = '" + _testKindCd + "'");
            stb.append("     AND T1.YEAR = '" + _year + "'");
            if (_useTestitemMstCountflgNew) {
                stb.append("     AND T1.SEMESTER = '" + _semester + "'");
            }
            return stb.toString();
        }

        private String getTestItemMstCountFlgTableName() {
            if (_useTestitemMstCountflgNew) {
                return "TESTITEM_MST_COUNTFLG_NEW";
            } else{
                return "TESTITEM_MST_COUNTFLG";
            }
        }
        
        public String getHrClassAbbv(String hrClass) {
            return (String) _hrClassMap.get(hrClass);
        }
        
        private void setSemesterStartDate(final DB2UDB db2, final String semester) 
        throws Exception {
            String sql = "SELECT SDATE FROM SEMESTER_MST WHERE SEMESTER = '" + semester + "' AND YEAR = '" + _year + "' ";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            if (rs.next()) {
                _semesterStartDate = rs.getString("SDATE");
            }
        }
        
        private void setGradeName(final DB2UDB db2) 
        throws Exception {
            String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            if (rs.next()) {
                _gradeName = rs.getString("GRADE_NAME1");
            }
        }
        
        public String getGradeName() {
            return _gradeName;
        }
    }
    
    private class Student {
        final String _schregno;
        final String _hrClass;
        final String _attendNo;
        final String _name;
        final String _courseName;
        final Map _subClassDataMap;
        public Student(
                final String schregno,
                final String hrClass,
                final String attendNo,
                final String courseName,
                final String name) {
            _schregno = schregno;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _name = name;
            _courseName = courseName;
            _subClassDataMap = new TreeMap();
        }
        
        public String getHrClassAttendNoString() {
            return _param.getHrClassAbbv(_hrClass) + "-" + _attendNo;
        }
        
        public void addSubClass(final SubClass subClass) {
            if (_subClassDataMap.get(subClass._cd) != null) {
                throw new IllegalArgumentException(Student.this.toString() + subClass);
            }
            _subClassDataMap.put(subClass._cd, subClass);
        }
        
        public SubClass getSubClass(final String subClassCd) {
            return (SubClass) _subClassDataMap.get(subClassCd);
        }
        
        public String toString() {
            final StringBuffer stb = new StringBuffer();
            stb.append(getHrClassAttendNoString() + _name);
            for (Iterator it = _subClassDataMap.values().iterator(); it.hasNext();) {
                SubClass subClass = (SubClass) it.next();
                stb.append(subClass.toString());
            }
            return stb.toString();
        }

        /**
         * 表示用データのリストを得る
         * @return 表示用データのリスト
         */
        public List getOutputDataList() {
            List rtn = new ArrayList();
            boolean first = true;
            for (Iterator it = _subClassDataMap.keySet().iterator(); it.hasNext();) {
                String subClassCd = (String) it.next();
                SubClass subClass = (SubClass) _subClassDataMap.get(subClassCd);
                final String hrClassAttendNo = first ? getHrClassAttendNoString() : ""; // 組番号と生徒氏名は最初の科目だけ表示する
                final String name = first ? _name : "";
                final String courseName = first ? _courseName : "";
                rtn.add(new OutputData(hrClassAttendNo, name, courseName, _schregno, subClass));
                first = false;
            }
            
            return rtn;
        }
    }
    
    /** 科目欄のデータを保持 */
    private class SubClass {
        final String _cd;
        final String _remark;
        final String _compCredit;
        final String _getCredit;
        final String _gradValue;
        BigDecimal _absent;

        public SubClass(
                final String cd,
                final String remark,
                final String compCredit,
                final String getCredit,
                final String gradeValue) {
            _cd = cd;
            _remark = remark;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _gradValue = gradeValue;
            _absent = null;
        }

        public void setAbsent(final BigDecimal absent) {
            _absent = absent;
        }

        public String getAbsent() {
            if (_absent == null) {
                return "";
            } else {
                return _absent.toString();
            }
        }
        
        public String toString() {
            return "[" + _param.getSubClassAbbv(_cd) + "(" + _cd + ") 履修 = " + _compCredit + " 修得 = " + _getCredit + " 評定 = " + _gradValue + " 欠課 = " + _absent + "]";
        }
    }
    
    /** 表示用データ */
    private class OutputData {
        final String _hrClassAttendNo;
        final String _name;
        final String _courseName;
        final String _schregno;
        final SubClass _subClass;
        public OutputData(
                final String hrClassAttendNo,
                final String name,
                final String courseName,
                final String schregno,
                final SubClass subClass) {
            _hrClassAttendNo = hrClassAttendNo;
            _name = name;
            _courseName = courseName;
            _schregno = schregno;
            _subClass = subClass;
        }
        
        public void output(final Vrw32alp svf, final int column, final int row) {
            svf.VrsOutn("HR_CLASS_" + column, row, _hrClassAttendNo);
            svf.VrsOutn("NAME_" + column, row, _name);
            svf.VrsOutn("COURSE_" + column, row, _courseName);
            svf.VrsOutn("SUBCLASS_" + column, row, _param.getSubClassAbbv(_subClass._cd));
            svf.VrsOutn("RATE_" + column, row, _subClass._gradValue);
            svf.VrsOutn("ABSENT_" + column,  row, _subClass.getAbsent());
            svf.VrsOutn("COMP_CREDIT_" + column, row, _subClass._compCredit);
            svf.VrsOutn("GET_CREDIT_" + column, row, _subClass._getCredit);
            svf.VrsOutn("TAKESEMES_" + column, row, "通年");
            svf.VrsOutn("REMARK_" + column, row, _subClass._remark);
        }
        
        public String toString() {
            StringBuffer stb = new StringBuffer();
            stb.append("[");
            stb.append(_hrClassAttendNo + " , ");
            stb.append(_name + " , ");
            stb.append(" (" + _schregno + ") , ");
            stb.append(_subClass);
            stb.append("]");
            return stb.toString();
        }
    }
}
