package servletpack.KNJB;

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
public class KNJB256 {

    private static final Log log = LogFactory.getLog(KNJB256.class);
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
                final List hrClassList = createHrClassInfoData(db2, _param._categorySelected[i]);
                if (printMain(svf, hrClassList)) { // 生徒出力のメソッド
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
    private boolean printMain(final Vrw32alp svf, final List hrClassList) throws Exception {
        boolean hasData = false;
        for (final Iterator it = hrClassList.iterator(); it.hasNext();) {
            final HrClass hrClass = (HrClass) it.next();
            if (hrClass._studentList.size() > 0 && hrClass._semeMonthList.size() > 0) {
                if (printStudent(svf, hrClass)) {
                    hasData = true;
                }
            }
        }
        return  hasData;
    }
    
    private boolean printStudent(final Vrw32alp svf, final HrClass hrClass) {
        boolean hasData = false;

        svf.VrSetForm("KNJB256.frm", 4);
        
        svf.VrsOut("TITLE", _param._semestername + "　出欠集計一覧表");
        svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
        svf.VrsOut("TR_NAME", hrClass.getTrName());
        svf.VrsOut("SUBTR_NAME", hrClass.getSubTrName());
        svf.VrsOut("HR_NAME", hrClass._hrName);

        int no = 0;
        for (final Iterator it = hrClass._studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            no++;
            svf.VrsOut("NO", String.valueOf(no));
            svf.VrsOut("SCHREGNO", student._schregno);
            svf.VrsOut("NAME" + ((30 < getMS932ByteLength(student._name)) ? "_2" : ""), student._name);
            svf.VrsOut("SEX", student._sexname);
            final String attendno = (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : "";
            svf.VrsOut("ATTENDNO", attendno);
            int len = 0;
            for (final Iterator it2 = hrClass._semeMonthList.iterator(); it2.hasNext();) {
                final SemeMonth semeMonth = (SemeMonth) it2.next();
                len++;
                svf.VrsOut("SEMESTER" + len, semeMonth._semestername);
                svf.VrsOut("MONTH" + len, semeMonth._month + "月");
                
                if (semeMonth._attendMap.containsKey(student._schregno)) {
                    final Attend attend = (Attend) semeMonth._attendMap.get(student._schregno);
                    svf.VrsOut("KESSEKI" + len, attend._sick);
                    svf.VrsOut("LATE" + len, attend._late);
                    svf.VrsOut("EARLY" + len, attend._early);
                    svf.VrsOut("ABSENT" + len, attend._absent);
                    final int suspend = (NumberUtils.isDigits(attend._suspend)) ? Integer.parseInt(attend._suspend) : 0;
                    final int mourning = (NumberUtils.isDigits(attend._mourning)) ? Integer.parseInt(attend._mourning) : 0;
                    svf.VrsOut("SUSPEND" + len, String.valueOf(suspend + mourning));
                }
            }
            svf.VrEndRecord();
            hasData = true;
        }
        
        if (hasData) {
            int len = 0;
            for (final Iterator it2 = hrClass._semeMonthList.iterator(); it2.hasNext();) {
                final SemeMonth semeMonth = (SemeMonth) it2.next();
                len++;
                if (semeMonth._attendMap.containsKey("TOTAL")) {
                    final Attend total = (Attend) semeMonth._attendMap.get("TOTAL");
                    svf.VrsOut("TOTAL_KESSEKI" + len, total._sick);
                    svf.VrsOut("TOTAL_LATE" + len, total._late);
                    svf.VrsOut("TOTAL_EARLY" + len, total._early);
                    svf.VrsOut("TOTAL_ABSENT" + len, total._absent);
                    final int suspend = (NumberUtils.isDigits(total._suspend)) ? Integer.parseInt(total._suspend) : 0;
                    final int mourning = (NumberUtils.isDigits(total._mourning)) ? Integer.parseInt(total._mourning) : 0;
                    svf.VrsOut("TOTAL_SUSPEND" + len, String.valueOf(suspend + mourning));
                }
            }
            svf.VrEndRecord();
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

    private List createHrClassInfoData(final DB2UDB db2, final String gradehrclass) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getHrClassInfoSql(gradehrclass);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final HrClass hrClassInfo = new HrClass(
                        rs.getString("GRADE"),
                        rs.getString("HR_CLASS"),
                        rs.getString("HR_NAME"),
                        rs.getString("HR_NAMEABBV"),
                        rs.getString("TR_NAME1"),
                        rs.getString("TR_NAME2"),
                        rs.getString("TR_NAME3"),
                        rs.getString("SUBTR_NAME1"),
                        rs.getString("SUBTR_NAME2"),
                        rs.getString("SUBTR_NAME3")
                );
                rtnList.add(hrClassInfo);
                hrClassInfo._studentList = hrClassInfo.createStudentInfoData(db2, hrClassInfo);
                hrClassInfo._semeMonthList = hrClassInfo.createSemeMonthInfoData(db2, hrClassInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    
    private String getHrClassInfoSql(final String gradehrclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     T1.HR_NAMEABBV, ");
        stb.append("     L1.STAFFNAME AS TR_NAME1, ");
        stb.append("     L2.STAFFNAME AS TR_NAME2, ");
        stb.append("     L3.STAFFNAME AS TR_NAME3, ");
        stb.append("     L4.STAFFNAME AS SUBTR_NAME1, ");
        stb.append("     L5.STAFFNAME AS SUBTR_NAME2, ");
        stb.append("     L6.STAFFNAME AS SUBTR_NAME3 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.TR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST L2 ON L2.STAFFCD = T1.TR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST L3 ON L3.STAFFCD = T1.TR_CD3 ");
        stb.append("     LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.SUBTR_CD1 ");
        stb.append("     LEFT JOIN STAFF_MST L5 ON L5.STAFFCD = T1.SUBTR_CD2 ");
        stb.append("     LEFT JOIN STAFF_MST L6 ON L6.STAFFCD = T1.SUBTR_CD3 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '" + gradehrclass + "' ");
        return stb.toString();
    }
    
    /** 年組 */
    private class HrClass {
        final String _grade;
        final String _hrclass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _trName1;
        final String _trName2;
        final String _trName3;
        final String _subtrName1;
        final String _subtrName2;
        final String _subtrName3;
        List _studentList = new ArrayList();
        List _semeMonthList = new ArrayList();
        
        HrClass(
                final String grade,
                final String hrclass,
                final String hrName,
                final String hrNameAbbv,
                final String trName1,
                final String trName2,
                final String trName3,
                final String subtrName1,
                final String subtrName2,
                final String subtrName3
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
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

        private List createStudentInfoData(final DB2UDB db2, final HrClass hrClass) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getStudentInfoSql(hrClass);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student studentInfo = new Student(
                            rs.getString("SCHREGNO"),
                            rs.getString("NAME"),
                            rs.getString("SEX"),
                            rs.getString("SEX_NAME"),
                            rs.getString("ATTENDNO")
                    );
                    rtnList.add(studentInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }

        private List createSemeMonthInfoData(final DB2UDB db2, final HrClass hrClass) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getSemeMonthInfoSql(hrClass);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final SemeMonth semeMonthInfo = new SemeMonth(
                            rs.getString("SEMESTER"),
                            rs.getString("SEMESTERNAME"),
                            rs.getString("MONTH"),
                            rs.getString("CNT_JISSI"),
                            rs.getString("CNT_KYUKOU"),
                            rs.getString("CNT_MI")
                            );
                    rtnList.add(semeMonthInfo);
                    semeMonthInfo._attendMap = semeMonthInfo.createAttendInfoData(db2, semeMonthInfo.getSemMonthInState(), hrClass);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }
    }
    
    private String getStudentInfoSql(final HrClass hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.SEX, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        stb.append("     T1.ATTENDNO ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T2.SEX ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + hrClass._grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + hrClass._hrclass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }
    
    /** 生徒データクラス */
    private class Student {
        final String _schregno;
        final String _name;
        final String _sex;
        final String _sexname;
        final String _attendno;
        
        Student(
                final String schregno,
                final String name,
                final String sex,
                final String sexname,
                final String attendno
        ) {
            _schregno = schregno;
            _name = name;
            _sex = sex;
            _sexname = sexname;
            _attendno = attendno;
        }
    }
    
    private String getSemeMonthInfoSql(final HrClass hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     S1.SEMESTERNAME, ");
        stb.append("     MONTH(T1.EXECUTEDATE) AS MONTH, ");
        stb.append("     CASE WHEN MONTH(T1.EXECUTEDATE) <= 3 THEN MONTH(T1.EXECUTEDATE) + 12 ELSE MONTH(T1.EXECUTEDATE) END AS MONTH_ORDER, ");
        stb.append("     COUNT(CASE WHEN T1.EXECUTEDIV = '1' THEN '1' END) AS CNT_JISSI, ");
        stb.append("     COUNT(CASE WHEN T1.EXECUTEDIV = '2' THEN '2' END) AS CNT_KYUKOU, ");
        stb.append("     COUNT(CASE WHEN T1.EXECUTEDIV = '1' OR T1.EXECUTEDIV = '2' THEN NULL ELSE '0' END) AS CNT_MI ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_STD_DAT T2 ");
        stb.append("             ON  T2.YEAR = T1.YEAR ");
        stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("             AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T3 ");
        stb.append("             ON  T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("             AND T3.YEAR = T2.YEAR ");
        stb.append("             AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("             AND T3.GRADE = '" + hrClass._grade + "' ");
        stb.append("             AND T3.HR_CLASS = '" + hrClass._hrclass + "' ");
        stb.append("     LEFT JOIN SEMESTER_MST S1 ON S1.YEAR = T1.YEAR AND S1.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("     T1.EXECUTEDATE BETWEEN DATE('" + _param._dateFrom + "') AND DATE('" + _param._dateTo + "') ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     S1.SEMESTERNAME, ");
        stb.append("     MONTH(T1.EXECUTEDATE), ");
        stb.append("     CASE WHEN MONTH(T1.EXECUTEDATE) <= 3 THEN MONTH(T1.EXECUTEDATE) + 12 ELSE MONTH(T1.EXECUTEDATE) END ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     MONTH_ORDER ");
        return stb.toString();
    }
    
    /** 学期・月クラス */
    private class SemeMonth {
        final String _semester;
        final String _semestername;
        final String _month;
        final String _jissiCnt;
        final String _kyukouCnt;
        final String _miCnt;
        Map _attendMap = new HashMap();
        
        SemeMonth(
                final String semester,
                final String semestername,
                final String month,
                final String jissiCnt,
                final String kyukouCnt,
                final String miCnt
        ) {
            _semester = semester;
            _semestername = semestername;
            _month = month;
            _jissiCnt = jissiCnt;
            _kyukouCnt = kyukouCnt;
            _miCnt = miCnt;
        }

        private String getSemMonthInState() {
            final String month2 = _month.length() == 1 ? "0" + _month : _month;
            final String semMonthInState = "('" + _semester + month2 + "')";
            return semMonthInState;
        }

        private Map createAttendInfoData(final DB2UDB db2, final String semMonthInState, final HrClass hrClass) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtnMap = new HashMap();
            final Map paramMap = new HashMap();
            paramMap.put("useCurriculumcd", _param._useCurriculumcd);
            paramMap.put("useVirus", _param._useVirus);
            paramMap.put("useKoudome", _param._useKoudome);
            try {
                final String sql = AttendAccumulate.getAttendSemesSql(
                        true,
                        _param._definecode,
                        _param._knjSchoolMst,
                        _param._year,
                        _param.SSEMESTER,
                        _param._semester,
                        semMonthInState,
                        _param._periodInState,
                        null,
                        null,
                        null,
                        null,
                        hrClass._grade,
                        hrClass._hrclass,
                        null,
                        "SCHREGNO",
                        paramMap
                );
                ps = db2.prepareStatement(sql);

                int cnt = 0;
                int lesson = 0, sick = 0, late = 0, early = 0, absent = 0, suspend = 0, mourning = 0;

                rs = ps.executeQuery();
                while (rs.next()) {
                    final Attend attendInfo = new Attend(
                            rs.getString("LESSON"),
                            rs.getString("SICK"),
                            rs.getString("LATE"),
                            rs.getString("EARLY"),
                            rs.getString("ABSENT"),
                            rs.getString("SUSPEND"),
                            rs.getString("MOURNING")
                            );
                    rtnMap.put(rs.getString("SCHREGNO"), attendInfo);
                    
                    lesson += Integer.parseInt(attendInfo._lesson);
                    sick += Integer.parseInt(attendInfo._sick);
                    late += Integer.parseInt(attendInfo._late);
                    early += Integer.parseInt(attendInfo._early);
                    absent += Integer.parseInt(attendInfo._absent);
                    suspend += Integer.parseInt(attendInfo._suspend);
                    mourning += Integer.parseInt(attendInfo._mourning);
                    
                    cnt++;
                }

                if (cnt > 0) {
                    final Attend total = new Attend(String.valueOf(lesson), String.valueOf(sick), String.valueOf(late), String.valueOf(early), String.valueOf(absent), String.valueOf(suspend), String.valueOf(mourning));
                    rtnMap.put("TOTAL", total);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnMap;
        }
    }
    
    /** 出欠クラス */
    private class Attend {
        final String _lesson;
        final String _sick;
        final String _late;
        final String _early;
        final String _absent;
        final String _suspend;
        final String _mourning;
        
        Attend(
                final String lesson,
                final String sick,
                final String late,
                final String early,
                final String absent,
                final String suspend,
                final String mourning
        ) {
            _lesson = lesson;
            _sick = sick;
            _late = late;
            _early = early;
            _absent = absent;
            _suspend = suspend;
            _mourning = mourning;
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
        private String _semestername;
        final String _grade;
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
        
        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("SEMESTER");
            _grade  = request.getParameter("GRADE");
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
                _periodInState = AttendAccumulate.getPeiodValue(db2, _definecode, _year, SSEMESTER, _semester);
                _semestername = setSemesterName(db2);
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

        private String setSemesterName(DB2UDB db2) {
            String name = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }
    }
    
}// クラスの括り
