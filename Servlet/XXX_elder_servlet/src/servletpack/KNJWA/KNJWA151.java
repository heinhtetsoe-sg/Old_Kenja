// kanji=漢字
/*
 * $Id: 8cc6813f67853c15309b1459da4c06e794f6ba2d $
 *
 * 作成日: 2007/11/20 17:20:00 - JST
 * 作成者: nakada
 *
 * Copyright(C) 2004-2007 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.WithusUtils;

/**
 * 履修修得済科目一覧
 * @author nakada
 * @version $Id: 8cc6813f67853c15309b1459da4c06e794f6ba2d $
 */
public class KNJWA151 {
    /* pkg */static final Log log = LogFactory.getLog(KNJWA151.class);

    private static final String FORM_FILE = "KNJWA151.frm";

    /*
     * 名称マスタキー（NAMECD1）
     */
    // 教育課程
    private static final String COURSE_OF_STUDY = "W002";
    // 必履記号
    private static final String REQUIRE = "Z011";

    /*
     * 伝票明細件数ＭＡＸ
     */
    /** 伝票明細件数ＭＡＸ */
    private static final int DETAILS_MAX = 60;

    private Form _form;
    private Vrw32alp _svf;

    private DB2UDB db2;

    private boolean _hasData;
    private String _stYear;     // 入学年度

    Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        dumpParam(request);
        _param = createParam(request);
        _form = new Form(FORM_FILE, response, _svf);
        db2 = null;

        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            if (openDb(db2)) {
                return;
            }

            _param.load(db2);

            for (int i = 0; i < _param._schregno.length; i++) {
                final String schregno = _param._schregno[i];
                log.debug(">>学籍番号=" + schregno);

                final Student student = createStudent(db2, schregno, i);
                printMain(student);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            _form.closeSvf();
            closeDb(db2);
        }
    }

    private void printMain(final Student student) throws SQLException {

        final int stYear = Integer.parseInt(_stYear);
        final Map nendoMap = printHeader(student, stYear);
        int printCnt = 1;
        int allAnotherCre = 0;
        Map allStudyMap = new HashMap();
        Map allRityuMap = new HashMap();
        for (final Iterator itCredit = student._getCreditsDat.iterator(); itCredit.hasNext();) {
            final LineData lineData = (LineData) itCredit.next();
            int totalAnotherCre = 0;
            Map totalStudyMap = new HashMap();
            Map totalRityuMap = new HashMap();

            for (final Iterator itPrint = lineData._printData.iterator(); itPrint.hasNext();) {
                printCnt = printCnt > 59 ? 1 : printCnt;
                final PrintData printData = (PrintData) itPrint.next();

                final String annual = null == (String) nendoMap.get(printData._year) ? "4" : (String) nendoMap.get(printData._year);
                if (printData._schoolCd.equals("0")) {

                    //入学年度以前は、除外
                    if (stYear > Integer.parseInt(printData._year)) {
                        continue;
                    }

                    if (!totalStudyMap.containsKey(annual)) {
                        totalStudyMap.put(annual, new Integer(printData._credit));
                    } else {
                        int creInt = ((Integer) totalStudyMap.get(annual)).intValue();
                        creInt += printData._credit;
                        totalStudyMap.put(annual, new Integer(creInt));
                    }
                    if (!allStudyMap.containsKey(annual)) {
                        allStudyMap.put(annual, new Integer(printData._credit));
                    } else {
                        int creInt = ((Integer) allStudyMap.get(annual)).intValue();
                        creInt += printData._credit;
                        allStudyMap.put(annual, new Integer(creInt));
                    }
                } else if (printData._schoolCd.equals("9")) {

                    //入学年度以前は、除外
                    if (stYear > Integer.parseInt(printData._year)) {
                        continue;
                    }

                    if (!totalRityuMap.containsKey(annual)) {
                        totalRityuMap.put(annual, new Integer(printData._credit));
                    } else {
                        int creInt = ((Integer) totalRityuMap.get(annual)).intValue();
                        creInt += printData._credit;
                        totalRityuMap.put(annual, new Integer(creInt));
                    }
                    if (!allRityuMap.containsKey(annual)) {
                        allRityuMap.put(annual, new Integer(printData._credit));
                    } else {
                        int creInt = ((Integer) allRityuMap.get(annual)).intValue();
                        creInt += printData._credit;
                        allRityuMap.put(annual, new Integer(creInt));
                    }
                } else {
                    totalAnotherCre += printData._credit;
                    allAnotherCre += printData._credit;
                }
            }
            _form._svf.VrsOut("CLASSNAME", lineData._className);
            if (null != lineData._subclassName) {
                final String field = lineData._subclassName.length() > 10 ? "2" : "1";
                _form._svf.VrsOut("SUBCLASSNAME" + field, lineData._subclassName);
            }
            final SubclassDetailsMst subclassDetailsMst = createSubclassDetailsMst(db2, lineData._classCd, _param._courseOfStudyString(_param._exeYear, "CURRICULUM"), lineData._subclassCd);
            _form._svf.VrsOut("MARK", _param._requireMapString(subclassDetailsMst._requireFlg));
            _form._svf.VrsOut("CREDIT1", subclassDetailsMst._credits);
            _form._svf.VrsOut("CREDIT2", subclassDetailsMst._credits);
            _form._svf.VrsOut("GET_CREDIT", String.valueOf(totalAnotherCre));
            printJikouCre(totalStudyMap, "COMP_CREDIT");
            printJikouCre(totalRityuMap, "TAKING_CREDIT");
            _form._svf.VrEndRecord();
            printCnt++;
            _hasData = true;
        }

        for (int i = printCnt; i < DETAILS_MAX; i++) {
            _form._svf.VrAttribute("CLASSNAME", "Meido=100");
            _form._svf.VrsOut("CLASSNAME", String.valueOf(i));
            _form._svf.VrEndRecord();
        }

        _form._svf.VrsOut("TOTAL_GET_CREDIT", String.valueOf(allAnotherCre));
        final int totalStudy = printJikouCre(allStudyMap, "TOTAL_COMP_CREDIT");
        final int totalRityu = printJikouCre(allRityuMap, "TOTAL_TAKING_CREDIT");
        _form._svf.VrEndRecord();

        _form._svf.VrsOut("SUM_TOTAL_CREDIT", String.valueOf(allAnotherCre + totalStudy));
        _form._svf.VrsOut("SUM_TAKING_CREDIT", "(" + String.valueOf(totalRityu) + ")");
        _form._svf.VrEndRecord();

    }

    private int printJikouCre(final Map printMap, final String fieldName) {
        int rtn = 0;
        for (final Iterator itStudy = printMap.keySet().iterator(); itStudy.hasNext();) {
            final String key = (String) itStudy.next();
            final Integer val = (Integer) printMap.get(key);
            rtn += val.intValue();
            String studyCre = "";
            if ("TAKING_CREDIT".equals(fieldName) || "TOTAL_TAKING_CREDIT".equals(fieldName)) {
                studyCre = null == val ? "" : "(" + val.toString() + ")";
            } else {
                studyCre = null == val ? "" : val.toString();
            }
            _form._svf.VrsOut(fieldName + key, studyCre);
        }
        return rtn;
    }

    private Map printHeader(final Student student, int stYear) {
        /* 学籍番号 */
        _form._svf.VrsOut("SCHREGNO", student._schregNo);
        /* 氏名 */
        _form._svf.VrsOut("NAME", student._name);
        /* 課程 */
        _form._svf.VrsOut("FULLCOURSE", _param._courseMapString(student._schregRegdDat._courseCd)
                + "課程"
                + _param._majorMapString(student._schregRegdDat._courseCd + student._schregRegdDat._majorCd)
                + _param._courseCodeMstMapString(student._schregRegdDat._coursecode));
        /* 教育課程 */
        _form._svf.VrsOut("COURSE", _param._courseOfStudyString(student._curriculumYear, "NAME"));
        /* 所属 */
        _form._svf.VrsOut("SCHOOLNAME", _param._belongingMapString(student._schregRegdDat._grade));
        /* 作成日 */
        _form._svf.VrsOut("DATE", getJDate(_param._loginDate));

        /* 年度 */
        final String year1 = String.valueOf(stYear);
        final String year2 = String.valueOf(stYear + 1);
        final String year3 = String.valueOf(stYear + 2);
        final String year4 = String.valueOf(stYear + 3);

        _form._svf.VrsOutn("NENDO", 1, KNJ_EditDate.h_format_JP_N(year1 + "/01/01") + "度");
        _form._svf.VrsOutn("NENDO", 2, KNJ_EditDate.h_format_JP_N(year2 + "/01/01") + "度");
        _form._svf.VrsOutn("NENDO", 3, KNJ_EditDate.h_format_JP_N(year3 + "/01/01") + "度");
        _form._svf.VrsOutn("NENDO", 4, KNJ_EditDate.h_format_JP_N(year4 + "/01/01") + "度");

        final Map nendoMap = new HashMap();
        nendoMap.put(year1, "1");
        nendoMap.put(year2, "2");
        nendoMap.put(year3, "3");
        nendoMap.put(year4, "4");

        return nendoMap;
    }

    private static String getJDate(String date) {
        try {
            final Calendar cal = KNJServletUtils.parseDate(date);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int dom = cal.get(Calendar.DAY_OF_MONTH);
            
            return nao_package.KenjaProperties.gengou(year, month, dom);

        } catch (final Exception e) {
            return null;
        }
    }

    private boolean openDb(final DB2UDB db2) {
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return true;
        }
        return false;
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    // ======================================================================
    private class Param {
        private final String _exeYear;
        private final String _year;
        private final String _semester;
        private final String _programId;
        private final String _dbName;
        private final String _loginDate;
        private final String[] _schregno;
        
        private Map _belongingMap;
        private Map _courseMap;        // 課程
        private Map _majorMap;         // 学科
        private Map _courseCodeMstMap; // コースコード
        private Map _subclassMstMap;   // 科目
        private Map _classMstMap;      // 教科
        private List _courseOfStudy;   // 教育課程
        private Map _requireMap;       // 必履修区分

        public Param(
                final String exeYear,
                final String year,
                final String semester,
                final String programId,
                final String dbName,
                final String loginDate,
                final String[] schregno
        ) {
            _exeYear = exeYear;
            _year = year;
            _semester = semester;
            _programId = programId;
            _dbName = dbName;
            _loginDate = loginDate;
            _schregno = schregno;
        }

        public void load(DB2UDB db2) throws SQLException {
            _belongingMap = createBelongingDat(db2);
            _courseMap = createCourseDat(db2);
            _majorMap = createMajorDat(db2);
            _courseCodeMstMap = createCourseCodeDat(db2);
            _subclassMstMap = createSubclassMst(db2);
            _classMstMap = createClassMst(db2);
            _courseOfStudy = createCourseOfStudy(db2);
            _requireMap = getNameMst(REQUIRE);

            return;
        }

        public String _belongingMapString(String code) {
            return (String) nvlT((String)_belongingMap.get(code));
        }

        public String _courseMapString(String code) {
            return (String) nvlT((String)_courseMap.get(code));
        }

        public String _majorMapString(String code) {
            return (String) nvlT((String)_majorMap.get(code));
        }

        public String _courseCodeMstMapString(String code) {
            return (String) nvlT((String)_courseCodeMstMap.get(code));
        }

        public String _subclassMstMapString(String code) {
            return (String) nvlT((String)_subclassMstMap.get(code));
        }

        public String _classMstMapString(String code) {
            return (String) nvlT((String)_classMstMap.get(code));
        }

        public String _courseOfStudyString(final String code, final String div) {
            for (int i = 0; i < _courseOfStudy.size(); i++) {
                CourseOfStudy courseOfStudy = (CourseOfStudy)_courseOfStudy.get(i);
                
                if (code.compareTo(courseOfStudy._stYear) >= 0 && code.compareTo(courseOfStudy._edYear) <= 0) {
                    if (div.equals("NAME")) {
                        return courseOfStudy._name;
                    } else {
                        return courseOfStudy._curriculumCd;
                    }
                }
            }

            return "";
        }

        public String _requireMapString(String code) {
            return nvlT((String)_requireMap.get(code));
        }

        private Map getNameMst(String nameCd1) throws SQLException {
            final String sql = sqlNameMst(nameCd1);
            final Map rtn = new HashMap();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("code");
                    final String name = rs.getString("name");
                    rtn.put(code, name);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private String sqlNameMst(String nameCd1) {
            return " select"
                    + "    NAMECD2 as code,"
                    + "    value(NAMESPARE1, '') as name"
                    + " from"
                    + "    NAME_MST"
                    + " where"
                    + "    nameCd1 = '" + nameCd1 + "'"
                    ;
        }
    }

    private Param createParam(final HttpServletRequest request) {
        final String exeYear = request.getParameter("EXE_YEAR");
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String programId = request.getParameter("PRGID");
        final String dbName = request.getParameter("DBNAME");
        final String loginDate = KNJ_EditDate.h_format_thi(request.getParameter("LOGIN_DATE"),0);
        final String[] schregno = request.getParameterValues("SCHREGNO");

        final Param param = new Param
        (
                exeYear,
                year,
                semester,
                programId,
                dbName,
                loginDate,
                schregno
        );
        return param;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    // ======================================================================

    private class Form {
        private Vrw32alp _svf;

        public Form(final String file,final HttpServletResponse response,
                final Vrw32alp svf) throws IOException {
            _svf = new Vrw32alp();

            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            _svf.VrSetForm(FORM_FILE, 4);
        }

        private void closeSvf() {
            if (!_hasData) {
                _svf.VrSetForm("MES001.frm", 0);
                _svf.VrsOut("note", "note");
                _svf.VrEndPage();
            }

            final int ret = _svf.VrQuit();
            log.info("===> VrQuit():" + ret);
        }
    }

    // ======================================================================
    /**
     * 学籍。学籍基礎マスタ。
     */
    private class Student {
        private final String _schregNo;
        private final String _name;
        private final String _applicantno;
        private final String _curriculumYear;
        private final String _entDate;

        private SchregRegdDat _schregRegdDat;           // 生徒。学籍在籍データ
        private List _getCreditsDat;

        Student(final String schregNo,
                final String name,
                final String applicantno,
                final String curriculumYear,
                final String entDate
        ) {
            _schregNo = schregNo;
            _name = name;
            _applicantno = applicantno;
            _curriculumYear = curriculumYear;
            _entDate = entDate;
        }

        public void load(DB2UDB db2) throws SQLException, Exception {
            _schregRegdDat = createSchregRegdDat(db2, _param._year, _param._semester, _schregNo, false);
            _getCreditsDat = createGetcreditsDat(db2, _schregNo);
        }
    }

    private Student createStudent(final DB2UDB db2, String schregno, final int i)
        throws SQLException, Exception {

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sqlStudents(schregno));
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregNo = rs.getString("schregNo");
                final String name = rs.getString("name");
                final String applicantno = rs.getString("applicantno");
                final String curriculumYear = rs.getString("curriculumYear");
                final String entDate = rs.getString("entDate");

                final Student studentDat = new Student(
                        schregNo,
                        name,
                        applicantno,
                        curriculumYear,
                        entDate
                );

                try {
                    final Calendar cal = KNJServletUtils.parseDate(entDate);
                    final int month = cal.get(Calendar.MONTH) + 1;
                    final int year = cal.get(Calendar.YEAR);
                    _stYear = month < 4 ? String.valueOf(year - 1) : String.valueOf(year);
                } catch (final Exception e) {
                    log.error(">>入学日付不正");
                    log.error(">>>学籍番号=" + schregNo);
                    log.error(">>>入学日付=" + entDate);

                    throw new Exception();
                }

                studentDat.load(db2);
                return studentDat;
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        log.debug(">>>SCHREG_BASE_MST に該当するものがありません。");
        throw new Exception();
    }

    private String sqlStudents(String schregno) {
        return " select"
                + "    SCHREGNO as schregNo,"
                + "    NAME as name,"
                + "    APPLICANTNO as applicantno,"
                + "    CURRICULUM_YEAR as curriculumYear,"
                + "    ENT_DATE as entDate"
                + " from"
                + "    SCHREG_BASE_MST"
                + " where" 
                + "    SCHREGNO = '" + schregno + "'";
    }

    // ======================================================================
    /**
     * 生徒。学籍在籍データ。
     */
    private class SchregRegdDat {
        private final String _year;
        private final String _grade;
        private final String _courseCd;      // 課程コード
        private final String _majorCd;       // 学科コード
        private final String _coursecode;

        SchregRegdDat(
                final String year,
                final String grade,
                final String courseCd,
                final String majorCd,
                final String coursecode
        ) {
            _year = year;
            _grade = grade;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _coursecode = coursecode;
        }

        public SchregRegdDat() {
            _year = "";
            _grade = "";
            _courseCd = "";
            _majorCd = "";
            _coursecode = "";
        }
    }

    public SchregRegdDat createSchregRegdDat(
            DB2UDB db2,
            String YEAR,
            String SEMESTER,
            String SCHREGNO,
            boolean st
    ) throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSchregRegdDat(YEAR, SEMESTER, SCHREGNO, st));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String year = rs.getString("year");
            final String grade = rs.getString("grade");
            final String courseCd = rs.getString("courseCd");
            final String majorCd = rs.getString("majorCd");
            final String coursecode = rs.getString("coursecode");

            final SchregRegdDat schregRegdDat = new SchregRegdDat(
                    year,
                    grade,
                    courseCd,
                    majorCd,
                    coursecode
            );
            return schregRegdDat;
        }
        return new SchregRegdDat();
    }

    private String sqlSchregRegdDat(String year, String semester, String schregNo, boolean st) {
        StringBuffer stb = new StringBuffer();

        stb.append(" select");
        stb.append("    YEAR as year,");
        stb.append("    GRADE as grade,");
        stb.append("    COURSECD as courseCd,");
        stb.append("    MAJORCD as majorCd,");
        stb.append("    COURSECODE as coursecode");
        stb.append(" from");
        stb.append("    SCHREG_REGD_DAT");
        stb.append(" where");

        stb.append("    SEMESTER = '" + semester + "'");
        stb.append(" and SCHREGNO = '" + schregNo + "'");

        if (!st) {
            stb.append(" and YEAR = '" + year + "'");
        }

        if (st) {
            stb.append(" order by YEAR");
        }

        return stb.toString();
    }

    public Map createCourseDat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlCourseDat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = nvlT(rs.getString("clubcd"));
            final String name = nvlT(rs.getString("name"));

            rtn.put(code, name);
        }

        return rtn;
    }

    private String sqlCourseDat() {
        return " select"
                + "    COURSECD as clubcd,"
                + "    COURSENAME as name"
                + " from"
                + "    COURSE_MST"
                ;
    }

    // ======================================================================
    /**
     * 学科データ。
     */
    public Map createMajorDat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlMajorDat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code1 = rs.getString("coursecd");
            final String code2 = rs.getString("majorcd");
            final String name = rs.getString("name");
            
            rtn.put(code1 + code2, name);
        }

        return rtn;
    }

    private String sqlMajorDat() {
        return " select"
                + "    COURSECD as coursecd,"
                + "    MAJORCD as majorcd,"
                + "    MAJORNAME as name"
                + " from"
                + "    MAJOR_MST"
                ;
    }

    // ======================================================================
    /**
     * コースコード　データ。
     */
    public Map createCourseCodeDat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlCourseCodeDat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = rs.getString("coursecode");
            final String name = rs.getString("name");
            
            rtn.put(code, name);
        }

        return rtn;
    }

    private String sqlCourseCodeDat() {
        return " select"
                + "    COURSECODE as coursecode,"
                + "    COURSECODENAME as name"
                + " from"
                + "    COURSECODE_MST"
                ;
    }

    // ======================================================================
    /**
     * 科目マスタ。
     */
    private Map createSubclassMst(final DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSubclassMst());
        rs = ps.executeQuery();

        while (rs.next()) {
            final String code1 = rs.getString("classcd");
            final String code2 = rs.getString("curriculumCd");
            final String code3 = rs.getString("subclasscd");
            final String name = rs.getString("subclassname");

            rtn.put(code1 + code2 + code3, name);
        }

        return rtn;
    }

    private String sqlSubclassMst() {
        return " select"
                + "    CLASSCD as classcd,"
                + "    CURRICULUM_CD as curriculumCd,"
                + "    SUBCLASSCD as subclasscd,"
                + "    SUBCLASSNAME as subclassname"
                + " from"
                + "    SUBCLASS_MST"
                ;
    }

    // ======================================================================
    /**
     * 教科マスタ。
     */
    private Map createClassMst(final DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlClassMst());
        rs = ps.executeQuery();

        while (rs.next()) {
            final String code = rs.getString("classcd");
            final String name = rs.getString("classname");

            rtn.put(code, name);
        }

        return rtn;
    }

    private String sqlClassMst() {
        return " select"
                + "    CLASSCD as classcd,"
                + "    CLASSNAME as classname"
                + " from"
                + "    CLASS_MST"
                ;
    }

    public List createGetcreditsDat(final DB2UDB db2, final String schregno) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlGetcreditsDat("SELECT", schregno));
        rs = ps.executeQuery();
        final List printList = new ArrayList();
        int classValution = 0;
        int classCnt = 0;
        int classField = 1;
        int totalCredit = 0;
        int totalClassCnt = 0;
        boolean hasData = false;
        String befClassCd = "";
        String befCurriCulum = "";
        String befSubclassCd = "";
        LineData lineData = null;
        while (rs.next()) {

            final int getCredit = null == rs.getString("GET_CREDIT") ? 0 : rs.getInt("GET_CREDIT");
            totalCredit += getCredit;

            if (!hasData || !befClassCd.equals(rs.getString("CLASSCD")) ||
                !befSubclassCd.equals(rs.getString("SUBCLASSCD")) ||
                !befCurriCulum.equals(rs.getString("CURRICULUM_CD"))
            ) {
                if (hasData &&
                    (!befClassCd.equals(rs.getString("CLASSCD")) ||
                     !befSubclassCd.equals(rs.getString("SUBCLASSCD")) ||
                     !befCurriCulum.equals(rs.getString("CURRICULUM_CD"))
                    )
                ) {
                    printList.add(lineData);
                }
                if (hasData && !befClassCd.equals(rs.getString("CLASSCD"))) {
                    classValution = 0;
                    classCnt = 0;
                    classField++;
                }
                lineData = new LineData(rs.getString("CLASSCD"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSCD"), rs.getString("SUBCLASSNAME"), 0);
            }
            if (rs.getInt("SCHOOLCD") < 2 && null != rs.getString("VALUATION")) {
                final int valuation = null == rs.getString("VALUATION") ? 0 : rs.getInt("VALUATION");
                classValution += valuation;
                classCnt++;
                totalClassCnt++;
            }
            lineData.setPrintData(getCredit, rs.getString("YEAR"), rs.getString("ANNUAL"), rs.getString("SCHOOLCD"));
            befClassCd = rs.getString("CLASSCD");
            befSubclassCd = rs.getString("SUBCLASSCD");
            befCurriCulum = rs.getString("CURRICULUM_CD");
            hasData = true;
        }
        if (hasData) {
            printList.add(lineData);
        }

        return printList;
    }

    private class LineData {
        final String _classCd;
        final String _className;
        final String _subclassCd;
        final String _subclassName;
        final List _printData;
        int _totalCredit = 0;
        public LineData(
                final String classCd,
                final String className,
                final String subclassCd,
                final String subclassName,
                final int totalCredit
        ) {
            _classCd = classCd;
            _className = className;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _printData = new ArrayList();
            _totalCredit += totalCredit;
        }
        private void setPrintData(final int credit, final String year, final String annual, final String schoolCd) {
            _totalCredit += credit;
            PrintData printData = new PrintData(year, annual, schoolCd, credit);
            _printData.add(printData);
        }
    }

    private class PrintData {
        final String _year;
        final String _annual;
        final String _schoolCd;
        final int _credit;

        public PrintData(final String year, final String annual, final String schoolCd, final int credit) {
            _year = year;
            _annual = annual;
            _schoolCd = schoolCd;
            _credit = credit;
        }
    }

    private String sqlGetcreditsDat(final String selectDiv, final String schregno) {
        final String taiikuNew = WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_CURRICULUM + WithusUtils.PHYSICAL_EDUCATION_SUBCLASS_CD;
        final String taiikuNew2 = WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_NEW_CURRICULUM + WithusUtils.PHYSICAL_EDUCATION_SUBCLASS_CD;
        final String taiikuNew3 = WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_NEW_CURRICULUM + WithusUtils.PHYSICAL_EDUCATION_SUBCLASS_CD_NEW;
        final String taiikuOld = WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_OLD_CURRICULUM + WithusUtils.PHYSICAL_EDUCATION_SUBCLASS_CD;
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH STUDY_REC AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     CASE WHEN T1.CLASSNAME IS NOT NULL ");
        stb.append("          THEN T1.CLASSNAME ");
        stb.append("          ELSE CASE WHEN L1.CLASSORDERNAME1 IS NOT NULL ");
        stb.append("               THEN L1.CLASSORDERNAME1 ");
        stb.append("               ELSE L1.CLASSNAME ");
        stb.append("         END ");
        stb.append("     END AS CLASSNAME, ");
        stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L1.SHOWORDER2 ");
        stb.append("          ELSE 999 ");
        stb.append("     END AS CLASSORDER2, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     L2.SUBCLASSCD2, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ");
        stb.append("          THEN T1.SUBCLASSNAME ");
        stb.append("          ELSE CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
        stb.append("               THEN L2.SUBCLASSORDERNAME1 ");
        stb.append("               ELSE L2.SUBCLASSNAME ");
        stb.append("         END ");
        stb.append("     END AS SUBCLASSNAME, ");
        stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L2.SHOWORDER2 ");
        stb.append("          ELSE 999999 ");
        stb.append("     END AS SUBCLASSORDER2, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.SCHOOLCD, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GET_CREDIT AS GET_CREDIT, ");
        stb.append("     T1.VALUATION AS VALUATION ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
        stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.CLASSCD || T1.CURRICULUM_CD || T1.SUBCLASSCD NOT IN ('" + taiikuNew + "', '" + taiikuNew2 + "', '" + taiikuNew3 + "', '" + taiikuOld + "') ");

        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     CASE WHEN L1.CLASSORDERNAME1 IS NOT NULL ");
        stb.append("          THEN L1.CLASSORDERNAME1 ");
        stb.append("          ELSE L1.CLASSNAME ");
        stb.append("     END AS CLASSNAME, ");
        stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L1.SHOWORDER2 ");
        stb.append("          ELSE 999 ");
        stb.append("     END AS CLASSORDER2, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     L2.SUBCLASSCD2, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
        stb.append("          THEN L2.SUBCLASSORDERNAME1 ");
        stb.append("          ELSE L2.SUBCLASSNAME ");
        stb.append("     END AS SUBCLASSNAME, ");
        stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L2.SHOWORDER2 ");
        stb.append("          ELSE 999999 ");
        stb.append("     END AS SUBCLASSORDER2, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     '0' AS SCHOOLCD, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GET_CREDIT AS GET_CREDIT, ");
        stb.append("     0 AS VALUATION ");
        stb.append(" FROM ");
        stb.append("     REC_CREDIT_ADMITS T1 ");
        stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
        stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.CLASSCD || T1.CURRICULUM_CD || T1.SUBCLASSCD NOT IN ('" + taiikuNew + "', '" + taiikuNew2 + "', '" + taiikuNew3 + "', '" + taiikuOld + "') ");
        stb.append("     AND T1.ADMITS_FLG = '1' ");
        stb.append("     AND NOT EXISTS( ");
        stb.append("                SELECT ");
        stb.append("                    'x' ");
        stb.append("                FROM ");
        stb.append("                    SCHREG_STUDYREC_DAT E1 ");
        stb.append("                WHERE ");
        stb.append("                    E1.SCHREGNO = '" + schregno + "' ");
        stb.append("                    AND E1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                    AND E1.CLASSCD = T1.CLASSCD ");
        stb.append("                    AND E1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("                    AND E1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("             ) ");

        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     CASE WHEN L1.CLASSORDERNAME1 IS NOT NULL ");
        stb.append("          THEN L1.CLASSORDERNAME1 ");
        stb.append("          ELSE L1.CLASSNAME ");
        stb.append("     END AS CLASSNAME, ");
        stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L1.SHOWORDER2 ");
        stb.append("          ELSE 999 ");
        stb.append("     END AS CLASSORDER2, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     L2.SUBCLASSCD2, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ");
        stb.append("          THEN T1.SUBCLASSNAME ");
        stb.append("          ELSE CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
        stb.append("               THEN L2.SUBCLASSORDERNAME1 ");
        stb.append("               ELSE L2.SUBCLASSNAME ");
        stb.append("         END ");
        stb.append("     END AS SUBCLASSNAME, ");
        stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L2.SHOWORDER2 ");
        stb.append("          ELSE 999 ");
        stb.append("     END AS SUBCLASSORDER2, ");
        stb.append("     L3.SCHREGNO, ");
        stb.append("     CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) AS SCHOOLCD, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GET_CREDIT, ");
        stb.append("     T1.VALUATION ");
        stb.append(" FROM ");
        stb.append("     ANOTHER_SCHOOL_GETCREDITS_DAT T1 ");
        stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
        stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L3 ON T1.APPLICANTNO = L3.APPLICANTNO ");
        stb.append(" WHERE ");
        stb.append("     L3.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND ( ");
        stb.append("          (CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) = '2' ");
        stb.append("           OR ");
        stb.append("           CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) = '3' ");
        stb.append("          ) ");
        stb.append("          OR ");
        stb.append("          ( ");
        stb.append("           VALUE(T1.GET_CREDIT, 0) > 0 ");
        stb.append("          ) ");
        stb.append("         ) ");

        stb.append(" ), MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     L1.CLASSNAME, ");
        stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L1.SHOWORDER2 ");
        stb.append("          ELSE 999 ");
        stb.append("     END AS CLASSORDER2, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     L2.SUBCLASSCD2, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     L2.SUBCLASSNAME, ");
        stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L2.SHOWORDER2 ");
        stb.append("          ELSE 999999 ");
        stb.append("     END AS SUBCLASSORDER2, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     '9' AS SCHOOLCD, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.COMP_CREDIT AS GET_CREDIT, ");
        stb.append("     CAST(NULL AS SMALLINT) AS VALUATION ");
        stb.append(" FROM ");
        stb.append("     COMP_REGIST_DAT T1 ");
        stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
        stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.YEAR || T1.CLASSCD || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
        stb.append("         NOT IN (SELECT ");
        stb.append("                     T2.YEAR || T2.CLASSCD || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
        stb.append("                 FROM ");
        stb.append("                     STUDY_REC T2 ");
        stb.append("                 WHERE ");
        stb.append("                     T2.SCHOOLCD = '0' ");
        stb.append("                ) ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     T1.CLASSNAME, ");
        stb.append("     T1.CLASSORDER2, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD2, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.SUBCLASSNAME, ");
        stb.append("     T1.SUBCLASSORDER2, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.SCHOOLCD, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GET_CREDIT, ");
        stb.append("     T1.VALUATION ");
        stb.append(" FROM ");
        stb.append("     STUDY_REC T1 ");
        stb.append(" ), ANNUAL_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     row_number() over (order by T1.YEAR) AS ANNUAL, ");
        stb.append("     T1.YEAR ");
        stb.append(" FROM ");
        stb.append("     MAIN_T T1 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR ");
        stb.append(" ) ");
        if (selectDiv.equals("TITLE")) {
            stb.append(" SELECT ");
            stb.append("     ANNUAL, ");
            stb.append("     YEAR ");
            stb.append(" FROM ");
            stb.append("     ANNUAL_T ");
            stb.append(" ORDER BY ");
            stb.append("     YEAR ");
        } else {
            stb.append(" SELECT ");
            stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
            stb.append("          THEN L2.CLASSCD ");
            stb.append("          ELSE T1.CLASSCD ");
            stb.append("     END AS CLASSCD, ");
            stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
            stb.append("          THEN L2.CLASSNAME ");
            stb.append("          ELSE T1.CLASSNAME ");
            stb.append("     END AS CLASSNAME, ");
            stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L2.SHOWORDER2 ");
            stb.append("          ELSE T1.CLASSORDER2 ");
            stb.append("     END AS CLASSORDER2, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SUBCLASSNAME, ");
            stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L1.SHOWORDER2 ");
            stb.append("          ELSE T1.SUBCLASSORDER2 ");
            stb.append("     END AS SUBCLASSORDER2, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SCHOOLCD, ");
            stb.append("     L3.ANNUAL, ");
            stb.append("     T1.YEAR, ");
            stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
            stb.append("     MAX(T1.VALUATION) AS VALUATION ");
            stb.append(" FROM ");
            stb.append("     MAIN_T T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
            stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD2 = L1.SUBCLASSCD ");
            stb.append("     LEFT JOIN CLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ");
            stb.append("     LEFT JOIN ANNUAL_T L3 ON T1.YEAR = L3.YEAR ");
            stb.append(" GROUP BY ");
            stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
            stb.append("          THEN L2.CLASSCD ");
            stb.append("          ELSE T1.CLASSCD ");
            stb.append("     END, ");
            stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
            stb.append("          THEN L2.CLASSNAME ");
            stb.append("          ELSE T1.CLASSNAME ");
            stb.append("     END, ");
            stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L2.SHOWORDER2 ");
            stb.append("          ELSE T1.CLASSORDER2 ");
            stb.append("     END, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SUBCLASSNAME, ");
            stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L1.SHOWORDER2 ");
            stb.append("          ELSE T1.SUBCLASSORDER2 ");
            stb.append("     END, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SCHOOLCD, ");
            stb.append("     L3.ANNUAL, ");
            stb.append("     T1.YEAR ");
            stb.append(" ORDER BY ");
            stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L2.SHOWORDER2 ");
            stb.append("          ELSE T1.CLASSORDER2 ");
            stb.append("     END, ");
            stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
            stb.append("          THEN L2.CLASSCD ");
            stb.append("          ELSE T1.CLASSCD ");
            stb.append("     END, ");
            stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
            stb.append("          THEN L1.SHOWORDER2 ");
            stb.append("          ELSE T1.SUBCLASSORDER2 ");
            stb.append("     END, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.YEAR ");
        }
        log.debug(stb);
        return stb.toString();
    }

    // ======================================================================
    /**
     * 科目詳細マスタ。
     */
    private class SubclassDetailsMst {
        private final String _credits;      // 単位数
        private final String _inoutDiv;     // 自校内外区分
        private final String _requireFlg;   // 必履修区分
        
        public SubclassDetailsMst() {
            _credits = "";
            _inoutDiv = "";
            _requireFlg = "";
       }

        SubclassDetailsMst(
                final String credits,
                final String inoutDiv,
                final String requireFlg
        ) {
            _credits = credits;
            _inoutDiv = inoutDiv;
            _requireFlg = requireFlg;
        }
    }

    public SubclassDetailsMst createSubclassDetailsMst(DB2UDB db2, String classcd, String curriculumCd, String subclasscde)
        throws SQLException {

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlSubclassDetailsMst(classcd, curriculumCd, subclasscde));
        rs = ps.executeQuery();
        while (rs.next()) {
            final String credits = rs.getString("credits");
            final String inoutDiv = rs.getString("inoutDiv");
            final String requireFlg = rs.getString("requireFlg");

            final SubclassDetailsMst subclassDetailsMst = new SubclassDetailsMst(
                    credits,
                    inoutDiv,
                    requireFlg
            );

            return subclassDetailsMst;
        }

        return new SubclassDetailsMst();
    }

    private String sqlSubclassDetailsMst(String classcd, String curriculumCd, String subclasscde) {
        return " select"
                + "    CREDITS as credits,"
                + "    INOUT_DIV as inoutDiv,"
                + "    REQUIRE_FLG as requireFlg"
                + " from"
                + "    SUBCLASS_DETAILS_MST"
                + " where"
                + "    YEAR = '" + _param._exeYear + "'"
                + " and CLASSCD = '" + classcd + "'"
                + " and CURRICULUM_CD = '" + curriculumCd + "'"
                + " and SUBCLASSCD = '" + subclasscde + "'"
                ;
    }

    // ======================================================================
    /**
     * 教育課程データ。
     */
    private class CourseOfStudy {
        private final String _curriculumCd;
        private final String _stYear;
        private final String _edYear;
        private final String _name;

        CourseOfStudy(
                final String curriculumCd,
                final String stYear,
                final String edYear,
                final String name
        ) {
            _curriculumCd = curriculumCd;
            _stYear = stYear;
            _edYear = edYear;
            _name = name;
        }
    }

    public List createCourseOfStudy(DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlCourseOfStudy());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String curriculumCd = rs.getString("curriculumCd");
            final String stYear = rs.getString("namespare1");
            final String edYear = rs.getString("namespare2");
            final String name = rs.getString("name");

            final CourseOfStudy courseOfStudy = new CourseOfStudy(
                    curriculumCd,
                    stYear,
                    edYear,
                    name
            );

            rtn.add(courseOfStudy);
        }

        if (rtn.isEmpty()) {
            log.debug(">>>ANOTHER_SCHOOL_GETCREDITS_DAT or COMP_REGIST_DAT に該当するものがありません。");
        }

        return rtn;
    }

    private String sqlCourseOfStudy() {
        return " select"
        + "    NAMECD2 as curriculumCd,"
        + "    NAME2 as name,"
        + "    NAMESPARE1 as namespare1,"
        + "    NAMESPARE2 as namespare2"
        + " from"
        + "    V_NAME_MST"
        + " where"
        + "    year = '" + _param._exeYear + "' AND"
        + "    nameCd1 = '" + COURSE_OF_STUDY + "'"
        ;
    }

    // ======================================================================
    /**
     * 所属データ。
     */
    public Map createBelongingDat(DB2UDB db2)
        throws SQLException {

        final Map rtn = new HashMap();

        PreparedStatement ps = null;
        ResultSet rs = null;

        ps = db2.prepareStatement(sqlBelongingDat());
        rs = ps.executeQuery();
        while (rs.next()) {
            final String code = rs.getString("belonging_div");
            final String name = rs.getString("schoolname1");

            rtn.put(code, name);
        }

        return rtn;
    }

    private String sqlBelongingDat() {
        return " select"
                + "    BELONGING_DIV as belonging_div,"
                + "    SCHOOLNAME1 as schoolname1"
                + " from"
                + "    BELONGING_MST"
                ;
    }

    /**
     * NULL値を""として返す。
     */
    private String nvlT(String val) {

        if (val == null) {
            return "";
        } else {
            return val;
        }
    }
} // KNJWA151

// eof
