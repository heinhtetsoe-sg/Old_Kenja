package servletpack.KNJB;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;


/**
 * クラス別希望状況一覧
 */
public class KNJB252 {

    private static final Log log = LogFactory.getLog(KNJB252.class);
    private boolean _hasData;
    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            final String dbName = request.getParameter("DBNAME");
            db2 = new DB2UDB(dbName, "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

        } catch (final Exception e) {
            log.error("Exception:", e);
            return;
        }
        
        _param = createParam(db2, request);

        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();

            if (svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            for (int i = 0; i < _param._categorySelected.length; i++) {

                // 生徒データを取得
                final List studentList = createStudentInfoData(db2, _param._categorySelected[i]);
                if (printMain(svf, studentList)) { // 生徒出力のメソッド
                    _hasData = true;
                }
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    /**
     * 生徒の出力
     */
    private boolean printMain(final Vrw32alp svf, final List studentList) throws Exception {
        boolean hasData = false;
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student._kouzaList.size() > 0) {
                if (printStudent(svf, student)) {
                    hasData = true;
                }
            }
        }
        return  hasData;
    }
    
    private boolean printStudent(final Vrw32alp svf, final Student student) {
        boolean hasData = false;

        svf.VrSetForm("KNJB252.frm", 4);
        svf.VrsOut("TITLE", "個人別　出席不良講座一覧");
        svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
        svf.VrsOut("DATE", _param.getDate(_param._dateFrom) + " \uFF5E " + _param.getDate(_param._dateTo));
        svf.VrsOut("NENDO", _param._year + "年度");

        svf.VrsOut("TR_NAME1" + ((30 < getMS932ByteLength(student.getTrName())) ? "_2" : ""), student.getTrName());
        svf.VrsOut("SUBTR_NAME1" + ((30 < getMS932ByteLength(student._subtrName1)) ? "_2" : ""), student._subtrName1);
        svf.VrsOut("SUBTR_NAME2" + ((30 < getMS932ByteLength(student._subtrName2)) ? "_2" : ""), student._subtrName2);
        svf.VrsOut("HR_NAME", student._hrName);
        final String attendno = (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : "";
        svf.VrsOut("ATTENDNO", attendno);
        svf.VrsOut("SCHREGNO", student._schregno);
        svf.VrsOut("NAME", student._name);

        for (final Iterator it = student._kouzaList.iterator(); it.hasNext();) {
            final Kouza kouza = (Kouza) it.next();
            svf.VrsOut("CHAIRNAME", kouza._chairname);
            svf.VrsOut("CREDIT", kouza._credits);
            String staffM = "";
            String seqM = "";
            for (final Iterator it2 = kouza._staffList.iterator(); it2.hasNext();) {
                final Staff staff = (Staff) it2.next();
                if ("1".equals(staff._chargediv)) {
                    staffM += seqM + staff._staffname;
                    seqM = ",";
                }
            }
            final String fieldLenM = (30 < getMS932ByteLength(staffM)) ? "_2" : "";
            svf.VrsOut("STAFFNAME" + fieldLenM, staffM);
            if (student._attendMap.containsKey(kouza._subclasscd)) {
                final Attend attend = (Attend) student._attendMap.get(kouza._subclasscd);
                svf.VrsOut("JISU", attend._lesson);
                svf.VrsOut("TOTAL_KETSUJI", attend._sick2);
                
                String judge = null;
                if (null != attend._sick2) {
                    if (0.0 < attend._absenceHighTyouka && attend._absenceHighTyouka < Double.parseDouble(attend._sick2)) {
                        judge = "C";
                    } else if (0.0 < attend._absenceHighTyui && attend._absenceHighTyui < Double.parseDouble(attend._sick2)) {
                        judge = "B";
                    }
                }
                if (null == judge && null != attend._lesson && Integer.parseInt(attend._lesson) > 0) {
                    judge = "A";
                }
                svf.VrsOut("JUDGE", judge);
            }
            svf.VrEndRecord();
            hasData = true;
        }

        return hasData;
    }
    
    private static int getMS932ByteLength(final String str) {
        int len = 0;
        if (null != str) {
            try {
                len = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return len;
    }

    private List createStudentInfoData(final DB2UDB db2, final String schregno) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getStudentInfoSql(schregno);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student studentInfo = new Student(
                        rs.getString("SCHREGNO"),
                        rs.getString("NAME"),
                        rs.getString("SEX"),
                        rs.getString("SEX_NAME"),
                        rs.getString("HR_NAME"),
                        rs.getString("HR_NAMEABBV"),
                        rs.getString("ATTENDNO"),
                        rs.getString("TR_NAME1"),
                        rs.getString("TR_NAME2"),
                        rs.getString("TR_NAME3"),
                        rs.getString("SUBTR_NAME1"),
                        rs.getString("SUBTR_NAME2"),
                        rs.getString("SUBTR_NAME3")
                );
                rtnList.add(studentInfo);
                studentInfo._kouzaList = studentInfo.createKouzaInfoData(db2, studentInfo._schregno);
                studentInfo._attendMap = studentInfo.createAttendInfoData(db2, studentInfo._schregno);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentInfoSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.SEX, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        stb.append("     T5.HR_NAME, ");
        stb.append("     T5.HR_NAMEABBV, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     L1.STAFFNAME AS TR_NAME1, ");
        stb.append("     L2.STAFFNAME AS TR_NAME2, ");
        stb.append("     L3.STAFFNAME AS TR_NAME3, ");
        stb.append("     L4.STAFFNAME AS SUBTR_NAME1, ");
        stb.append("     L5.STAFFNAME AS SUBTR_NAME2, ");
        stb.append("     L6.STAFFNAME AS SUBTR_NAME3 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T3.SEX ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("             ON  T5.YEAR = T1.YEAR ");
        stb.append("             AND T5.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T5.GRADE = T1.GRADE ");
        stb.append("             AND T5.HR_CLASS = T1.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T5.TR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST L2 ON L2.STAFFCD = T5.TR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST L3 ON L3.STAFFCD = T5.TR_CD3 ");
        stb.append("     LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T5.SUBTR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST L5 ON L5.STAFFCD = T5.SUBTR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST L6 ON L6.STAFFCD = T5.SUBTR_CD3 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }
    
    /** 生徒 */
    private class Student {
        final String _schregno;
        final String _name;
        final String _sex;
        final String _sexname;
        final String _hrName;
        final String _hrNameAbbv;
        final String _attendno;
        final String _trName1;
        final String _trName2;
        final String _trName3;
        final String _subtrName1;
        final String _subtrName2;
        final String _subtrName3;
        List _kouzaList = new ArrayList();
        Map _attendMap = new HashMap();
        
        Student(
                final String schregno,
                final String name,
                final String sex,
                final String sexname,
                final String hrName,
                final String hrNameAbbv,
                final String attendno,
                final String trName1,
                final String trName2,
                final String trName3,
                final String subtrName1,
                final String subtrName2,
                final String subtrName3
        ) {
            _schregno = schregno;
            _name = name;
            _sex = sex;
            _sexname = sexname;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _attendno = attendno;
            _trName1 = trName1;
            _trName2 = trName2;
            _trName3 = trName3;
            _subtrName1 = subtrName1;
            _subtrName2 = subtrName2;
            _subtrName3 = subtrName3;
        }

        private String getTrName() {
            String rtnName = "";
            String seq = "";
            if (_trName1 != null) {
                rtnName += seq + _trName1;
                seq = ",";
            }
            if (_trName2 != null) {
                rtnName += seq + _trName2;
                seq = ",";
            }
            if (_trName3 != null) {
                rtnName += seq + _trName3;
                seq = ",";
            }
            return rtnName;
        }

        private String getSubTrName() {
            String rtnName = "";
            String seq = "";
            if (_subtrName1 != null) {
                rtnName += seq + _subtrName1;
                seq = ",";
            }
            if (_subtrName2 != null) {
                rtnName += seq + _subtrName2;
                seq = ",";
            }
            if (_subtrName3 != null) {
                rtnName += seq + _subtrName3;
                seq = ",";
            }
            return rtnName;
        }

        private List createKouzaInfoData(final DB2UDB db2, final String schregno) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getKouzaInfoSql(schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Kouza kouzaInfo = new Kouza(
                            rs.getString("SUBCLASSCD"),
                            rs.getString("SUBCLASSNAME"),
                            rs.getString("CHAIRCD"),
                            rs.getString("CHAIRNAME"),
                            rs.getString("CREDITS")
                    );
                    rtnList.add(kouzaInfo);
                    kouzaInfo._staffList = kouzaInfo.createStaffInfoData(db2, kouzaInfo._chaircd);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }

        private Map createAttendInfoData(final DB2UDB db2, final String schregno) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtnMap = new HashMap();
            final Map paramMap = new HashMap();
            paramMap.put("useCurriculumcd", _param._useCurriculumcd);
            paramMap.put("useVirus", _param._useVirus);
            paramMap.put("useKoudome", _param._useKoudome);
            paramMap.put("DB2UDB", db2);
            try {        

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        ((Boolean) _param._hasuuMap.get("semesFlg")).booleanValue(),
                        _param._definecode,
                        _param._knjSchoolMst,
                        _param._year,
                        _param.SSEMESTER,
                        _param._semester,
                        (String) _param._hasuuMap.get("attendSemesInState"),
                        _param._periodInState,
                        (String) _param._hasuuMap.get("befDayFrom"),
                        (String) _param._hasuuMap.get("befDayTo"),
                        (String) _param._hasuuMap.get("aftDayFrom"),
                        (String) _param._hasuuMap.get("aftDayTo"),
                        null,
                        null,
                        schregno,
                        paramMap
                );
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    
                    final BigDecimal absenceHighTyouka = null == rs.getBigDecimal("ABSENCE_HIGH") ? new BigDecimal(0) : rs.getBigDecimal("ABSENCE_HIGH");
                    BigDecimal absenceHighTyui = absenceHighTyouka;
                    final String absenceWarn;
                    if ("1".equals(_param._semester)) {
                        absenceWarn = rs.getString("ABSENCE_WARN");
                    } else if ("2".equals(_param._semester)) {
                        absenceWarn = rs.getString("ABSENCE_WARN2");
                    } else {
                        absenceWarn = rs.getString("ABSENCE_WARN3");
                    }
                    if (null != absenceWarn && null != rs.getString("CREDITS")) {
                        absenceHighTyui = absenceHighTyui.subtract(new BigDecimal(absenceWarn).multiply(new BigDecimal(rs.getString("CREDITS"))));
                    }
                    
                    final Attend attendInfo = new Attend(
                            rs.getString("SUBCLASSCD"),
                            rs.getString("LESSON"),
                            rs.getString("SICK2"),
                            absenceHighTyouka.doubleValue(),
                            absenceHighTyui.doubleValue()
                    );
                    rtnMap.put(rs.getString("SUBCLASSCD"), attendInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnMap;
        }
    }

    private String getKouzaInfoSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T6.CHAIRNAME, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" T6.CLASSCD || '-' || T6.SCHOOL_KIND || '-' || T6.CURRICULUM_CD || '-' || T6.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append(" T6.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     T7.SUBCLASSNAME, ");
        stb.append("     C1.CREDITS ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_DAT T6 ");
        stb.append("             ON  T6.YEAR = T1.YEAR ");
        stb.append("             AND T6.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T6.CHAIRCD = T1.CHAIRCD ");
        stb.append("     INNER JOIN SUBCLASS_MST T7 ");
        stb.append("             ON  T7.SUBCLASSCD = T6.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T7.CLASSCD = T6.CLASSCD ");
            stb.append("         AND T7.SCHOOL_KIND = T6.SCHOOL_KIND ");
            stb.append("         AND T7.CURRICULUM_CD = T6.CURRICULUM_CD ");
        }
        stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T4 ");
        stb.append("             ON  T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T4.YEAR = T1.YEAR ");
        stb.append("             AND T4.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN CREDIT_MST C1  ");
        stb.append("             ON  C1.YEAR = T4.YEAR ");
        stb.append("             AND C1.GRADE = T4.GRADE ");
        stb.append("             AND C1.COURSECD = T4.COURSECD ");
        stb.append("             AND C1.MAJORCD = T4.MAJORCD ");
        stb.append("             AND C1.COURSECODE = T4.COURSECODE ");
        stb.append("             AND C1.SUBCLASSCD = T6.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND C1.CLASSCD = T6.CLASSCD ");
            stb.append("         AND C1.SCHOOL_KIND = T6.SCHOOL_KIND ");
            stb.append("         AND C1.CURRICULUM_CD = T6.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND (( ");
        stb.append("     CASE WHEN DATE('" + _param._dateFrom + "') BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("          THEN DATE('" + _param._dateFrom + "') ");
        stb.append("          ELSE T2.SDATE ");
        stb.append("     END ");
        stb.append("     BETWEEN T1.APPDATE AND T1.APPENDDATE) OR ( ");
        stb.append("     CASE WHEN DATE('" + _param._dateTo + "') BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("          THEN DATE('" + _param._dateTo + "') ");
        stb.append("          ELSE T2.EDATE ");
        stb.append("     END ");
        stb.append("     BETWEEN T1.APPDATE AND T1.APPENDDATE)) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.CHAIRCD ");
        return stb.toString();
    }
    
    /** 講座 */
    private class Kouza {
        final String _subclasscd;
        final String _subclassname;
        final String _chaircd;
        final String _chairname;
        final String _credits;
        List _staffList = new ArrayList();
        
        Kouza(
                final String subclasscd,
                final String subclassname,
                final String chaircd,
                final String chairname,
                final String credits
        ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _chaircd = chaircd;
            _chairname = chairname;
            _credits = credits;
        }
        
        private List createStaffInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getStaffInfoSql(chaircd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Staff staffInfo = new Staff(
                            rs.getString("STAFFCD"),
                            rs.getString("STAFFNAME"),
                            rs.getString("CHARGEDIV")
                            );
                    rtnList.add(staffInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }
    }
    
    private String getStaffInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.STAFFCD, ");
        stb.append("     T1.CHARGEDIV, ");
        stb.append("     T2.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STF_DAT T1 ");
        stb.append("     INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T2.STAFFCD ");
        return stb.toString();
    }
    
    /** 講座職員 */
    private class Staff {
        final String _staffcd;
        final String _staffname;
        final String _chargediv;
        
        Staff(
                final String staffcd,
                final String staffname,
                final String chargediv
        ) {
            _staffcd = staffcd;
            _staffname = staffname;
            _chargediv = chargediv;
        }
    }
    
    /** 出欠 */
    private class Attend {
        final String _subclasscd;
        final String _lesson;
        final String _sick2;
        final double _absenceHighTyouka;
        final double _absenceHighTyui;
        
        Attend(
                final String subclasscd,
                final String lesson,
                final String sick2,
                final double absenceHighTyouka,
                final double absenceHighTyui
        ) {
            _subclasscd = subclasscd;
            _lesson = lesson;
            _sick2 = sick2;
            _absenceHighTyouka = absenceHighTyouka;
            _absenceHighTyui = absenceHighTyui;
        }
    }
    
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _semester;
        final String _gradeHrclass;
        final String[] _categorySelected; //学年・組
        final String _dateFrom;
        final String _dateTo;
        final String _ctrlDate;
        final String _useCurriculumcd;
        final String _useVirus;
        final String _useKoudome;
        
        final String SSEMESTER = "1";
        private KNJSchoolMst _knjSchoolMst;
        private KNJDefineCode _definecode;
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        
        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("SEMESTER");
            _gradeHrclass  = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _dateFrom = request.getParameter("DATE_FROM") == null ? null : request.getParameter("DATE_FROM").replace('/', '-');
            _dateTo = request.getParameter("DATE_TO") == null ? null : request.getParameter("DATE_TO").replace('/', '-');
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
                _definecode = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, _definecode, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _dateFrom, _dateTo);
                //log.debug(_attendSemesMap);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
        }

        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = new KNJDefineCode();
            try {
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }
    }
    
}// クラスの括り
