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

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * クラス別希望状況一覧
 */
public class KNJB239 {

    private static final Log log = LogFactory.getLog(KNJB239.class);
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
                final List kouzaList = createKouzaInfoData(db2, _param._categorySelected[i]);
                if (printMain(svf, kouzaList)) { // 生徒出力のメソッド
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
    private boolean printMain(final Vrw32alp svf, final List kouzaList) throws Exception {
        boolean hasData = false;
        for (final Iterator it = kouzaList.iterator(); it.hasNext();) {
            final Kouza kouza = (Kouza) it.next();
            if (kouza._studentList.size() > 0 && kouza._schChrDatList.size() > 0) {
                if (printStudent(svf, kouza)) {
                    hasData = true;
                }
            }
        }
        return  hasData;
    }

    private boolean printStudent(final Vrw32alp svf, final Kouza kouza) {
        boolean hasData = false;

        String weekIsoKeep = "";
        List weekIsoList = new ArrayList();
        List curentWeekIsoSchChrDatList = new ArrayList();
        for (final Iterator it = kouza._schChrDatList.iterator(); it.hasNext();) {
            final SchChrDat schChrDat = (SchChrDat) it.next();
            if (!weekIsoKeep.equals(schChrDat._weekIso)) {
                curentWeekIsoSchChrDatList = new ArrayList();
                weekIsoList.add(curentWeekIsoSchChrDatList);
                weekIsoKeep = schChrDat._weekIso;
            }
            curentWeekIsoSchChrDatList.add(schChrDat);
        }

        for (final Iterator it2 = weekIsoList.iterator(); it2.hasNext();) {
            final List weekIsoSchChrDatList = (List) it2.next();

            svf.VrSetForm("KNJB239.frm", 4);

            svf.VrsOut("TITLE", "講座別　週単位　欠課状況一覧");
            svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
            svf.VrsOut("CHAIRCD", kouza._chaircd);
            svf.VrsOut("CHAIRNAME", kouza._chairname);
            svf.VrsOut("DATE_FROM_TO", _param.getDate(_param._dateFrom) + " \uFF5E " + _param.getDate(_param._dateTo));
            String staffM = "";
            String staffS = "";
            String seqM = "";
            String seqS = "";
            for (final Iterator it = kouza._staffList.iterator(); it.hasNext();) {
                final Staff staff = (Staff) it.next();
                if ("1".equals(staff._chargediv)) {
                    staffM += seqM + staff._staffname;
                    seqM = ",";
                } else {
                    staffS += seqS + staff._staffname;
                    seqS = ",";
                }
            }
            svf.VrsOut("MAIN_STAFF", staffM);
            svf.VrsOut("SUB_STAFF", staffS);
            final int kensuu = kouza._studentList.size();
            svf.VrsOut("SCH_CNT", String.valueOf(kensuu));

            int no = 0;
            for (final Iterator it = kouza._studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                no++;
                svf.VrsOut("NO", String.valueOf(no));
                svf.VrsOut("SCHREG_NO", student._schregno);
                svf.VrsOut("NAME" + ((30 < getMS932ByteLength(student._name)) ? "2" : "1"), student._name);
                final String attendno = (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) : "";
                svf.VrsOut("ATTEND_NO", student._hrNameAbbv + "-" + attendno);
                int len = 0;
                for (final Iterator it3 = weekIsoSchChrDatList.iterator(); it3.hasNext();) {
                    final SchChrDat schChrDat = (SchChrDat) it3.next();
                    len++;
                    svf.VrsOut("DATE" + len, schChrDat._month + "/" + schChrDat._day);
                    svf.VrsOut("WEEK" + len, "(" + KNJ_EditDate.h_format_W(schChrDat._executedate) + ")");
                    svf.VrsOut("PERIODCD" + len, schChrDat._periodname);
                    svf.VrsOut("EXECUTED" + len, schChrDat._executedivname);

                    if (schChrDat._attendDatMap.containsKey(student._schregno)) {
                        final AttendDat attendDat = (AttendDat) schChrDat._attendDatMap.get(student._schregno);
                        svf.VrsOut("DI_NAME" + len, attendDat._dicdname);
                    }
                }
                svf.VrEndRecord();
                hasData = true;
            }
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

    private List createKouzaInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getKouzaInfoSql(chaircd);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Kouza kouzaInfo = new Kouza(
                        rs.getString("SUBCLASSCD"),
                        rs.getString("SUBCLASSNAME"),
                        rs.getString("CHAIRCD"),
                        rs.getString("CHAIRNAME"),
                        rs.getString("TAKESEMES_NAME")
                        );
                rtnList.add(kouzaInfo);
                kouzaInfo._staffList = kouzaInfo.createStaffInfoData(db2, kouzaInfo._chaircd);
                kouzaInfo._studentList = kouzaInfo.createStudentInfoData(db2, kouzaInfo._chaircd);
                kouzaInfo._schChrDatList = kouzaInfo.createSchChrDatInfoData(db2, kouzaInfo._chaircd);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getKouzaInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.CHAIRNAME, ");
        stb.append("     T1.TAKESEMES, ");
        stb.append("     CASE WHEN T1.TAKESEMES = '0' THEN '通年' ELSE L1.SEMESTERNAME END AS TAKESEMES_NAME, ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append(" T1.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     T2.SUBCLASSNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1 ");
        stb.append("     INNER JOIN SUBCLASS_MST T2 ");
        stb.append("             ON  T2.SUBCLASSCD = T1.SUBCLASSCD ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.TAKESEMES ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        return stb.toString();
    }

    /** 講座データクラス */
    private class Kouza {
        final String _subclasscd;
        final String _subclassname;
        final String _chaircd;
        final String _chairname;
        final String _takesemesName;
        List _staffList = new ArrayList();
        List _studentList = new ArrayList();
        List _schChrDatList = new ArrayList();

        Kouza(
                final String subclasscd,
                final String subclassname,
                final String chaircd,
                final String chairname,
                final String takesemesName
        ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _chaircd = chaircd;
            _chairname = chairname;
            _takesemesName = takesemesName;
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

        /**
         * 生徒データ取得処理
         * @param db2           ＤＢ接続オブジェクト
         * @return              帳票出力対象データリスト
         * @throws Exception
         */
        private List createStudentInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getStudentInfoSql(chaircd);
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
                            rs.getString("CREDITS")
                    );
                    rtnList.add(studentInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnList;
        }

        private List createSchChrDatInfoData(final DB2UDB db2, final String chaircd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List rtnList = new ArrayList();
            try {
                final String sql = getSchChrDatInfoSql(chaircd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final SchChrDat schChrDatInfo = new SchChrDat(
                            rs.getString("MONTH"),
                            rs.getString("DAY"),
                            rs.getString("WEEK_ISO"),
                            rs.getString("EXECUTEDATE"),
                            rs.getString("PERIODCD"),
                            rs.getString("PERIOD_NAME"),
                            rs.getString("PERIOD_ABBV"),
                            rs.getString("EXECUTEDIV"),
                            rs.getString("EXECUTEDIV_NAME"),
                            rs.getString("EXECUTEDIV_ABBV")
                            );
                    rtnList.add(schChrDatInfo);
                    schChrDatInfo._attendDatMap = schChrDatInfo.createAttendDatInfoData(db2, schChrDatInfo._executedate, schChrDatInfo._periodcd, chaircd);
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

    /** 講座職員データクラス */
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

    private String getStudentInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.NAME, ");
        stb.append("     T3.SEX, ");
        stb.append("     N1.ABBV1 AS SEX_NAME, ");
        stb.append("     T5.HR_NAME, ");
        stb.append("     T5.HR_NAMEABBV, ");
        stb.append("     T4.ATTENDNO, ");
        stb.append("     L1.CREDITS ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT T1 ");
        stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T3.SEX ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T4 ");
        stb.append("             ON  T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("             AND T4.YEAR = T1.YEAR ");
        stb.append("             AND T4.SEMESTER = T1.SEMESTER ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T5 ");
        stb.append("             ON  T5.YEAR = T4.YEAR ");
        stb.append("             AND T5.SEMESTER = T4.SEMESTER ");
        stb.append("             AND T5.GRADE = T4.GRADE ");
        stb.append("             AND T5.HR_CLASS = T4.HR_CLASS ");
        stb.append("     LEFT JOIN CREDIT_MST L1  ");
        stb.append("             ON  L1.YEAR = T4.YEAR ");
        stb.append("             AND L1.GRADE = T4.GRADE ");
        stb.append("             AND L1.COURSECD = T4.COURSECD ");
        stb.append("             AND L1.MAJORCD = T4.MAJORCD ");
        stb.append("             AND L1.COURSECODE = T4.COURSECODE ");
        //教育課程対応
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '" + _param._subclasscd + "' ");
        } else {
            stb.append("         AND L1.SUBCLASSCD = '" + _param._subclasscd + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
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
        stb.append("     T4.GRADE, ");
        stb.append("     T4.HR_CLASS, ");
        stb.append("     T4.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒データクラス */
    private class Student {
        final String _schregno;
        final String _name;
        final String _sex;
        final String _sexname;
        final String _hrName;
        final String _hrNameAbbv;
        final String _attendno;
        final String _credits;

        Student(
                final String schregno,
                final String name,
                final String sex,
                final String sexname,
                final String hrName,
                final String hrNameAbbv,
                final String attendno,
                final String credits
        ) {
            _schregno = schregno;
            _name = name;
            _sex = sex;
            _sexname = sexname;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _attendno = attendno;
            _credits = credits;
        }
    }

    private String getSchChrDatInfoSql(final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     MONTH(T1.EXECUTEDATE) AS MONTH, ");
        stb.append("     DAY(T1.EXECUTEDATE) AS DAY, ");
        stb.append("     WEEK_ISO(T1.EXECUTEDATE) AS WEEK_ISO, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.EXECUTEDIV, ");
        stb.append("     N1.NAME1 AS PERIOD_NAME, ");
        stb.append("     N1.ABBV1 AS PERIOD_ABBV, ");
        stb.append("     N2.NAME1 AS EXECUTEDIV_NAME, ");
        stb.append("     N2.ABBV1 AS EXECUTEDIV_ABBV ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR_DAT T1 ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'B001' AND N1.NAMECD2 = T1.PERIODCD ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'C009' AND N2.NAMECD2 = T1.EXECUTEDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.EXECUTEDATE BETWEEN DATE('" + _param._dateFrom + "') AND DATE('" + _param._dateTo + "') ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD ");
        return stb.toString();
    }

    /** 通常時間割クラス */
    private class SchChrDat {
        final String _month;
        final String _day;
        final String _weekIso;
        final String _executedate;
        final String _periodcd;
        final String _periodname;
        final String _periodabbv;
        final String _executediv;
        final String _executedivname;
        final String _executedivabbv;
        Map _attendDatMap = new HashMap();

        SchChrDat(
                final String month,
                final String day,
                final String weekIso,
                final String executedate,
                final String periodcd,
                final String periodname,
                final String periodabbv,
                final String executediv,
                final String executedivname,
                final String executedivabbv
        ) {
            _month = month;
            _day = day;
            _weekIso = weekIso;
            _executedate = executedate;
            _periodcd = periodcd;
            _periodname = periodname;
            _periodabbv = periodabbv;
            _executediv = executediv;
            _executedivname = executedivname;
            _executedivabbv = executedivabbv;
        }

        private Map createAttendDatInfoData(final DB2UDB db2, final String executedate, final String periodcd, final String chaircd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtnMap = new HashMap();
            try {
                final String sql = getAttendDatInfoSql(executedate, periodcd, chaircd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final AttendDat attendDatInfo = new AttendDat(
                            rs.getString("SCHREGNO"),
                            rs.getString("ATTENDDATE"),
                            rs.getString("PERIODCD"),
                            rs.getString("DI_CD"),
                            rs.getString("DI_CD_NAME"),
                            rs.getString("DI_CD_ABBV")
                            );
                    rtnMap.put(rs.getString("SCHREGNO"), attendDatInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnMap;
        }
    }

    private String getAttendDatInfoSql(final String executedate, final String periodcd, final String chaircd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.DI_CD, ");
        stb.append("     N1.DI_NAME1 AS DI_CD_NAME, ");
        stb.append("     N1.DI_MARK AS DI_CD_ABBV ");
        stb.append(" FROM ");
        stb.append("     ATTEND_DAT T1 ");
        stb.append("     LEFT JOIN ATTEND_DI_CD_DAT N1 ON N1.YEAR = T1.YEAR AND N1.DI_CD = T1.DI_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.ATTENDDATE = '" + executedate + "' ");
        stb.append("     AND T1.PERIODCD = '" + periodcd + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chaircd + "' ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO ");
        return stb.toString();
    }

    /** 通常時間割クラス */
    private class AttendDat {
        final String _schregno;
        final String _attenddate;
        final String _periodcd;
        final String _dicd;
        final String _dicdname;
        final String _dicdabbv;

        AttendDat(
                final String schregno,
                final String attenddate,
                final String periodcd,
                final String dicd,
                final String dicdname,
                final String dicdabbv
        ) {
            _schregno = schregno;
            _attenddate = attenddate;
            _periodcd = periodcd;
            _dicd = dicd;
            _dicdname = dicdname;
            _dicdabbv = dicdabbv;
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
        final String _subclasscd;
        final String[] _categorySelected; //学年・組
        final String _dateFrom;
        final String _dateTo;
        final String _ctrlDate;
        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("SEMESTER");
            _subclasscd  = request.getParameter("SUBCLASSCD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _dateFrom = request.getParameter("DATE_FROM") == null ? null : request.getParameter("DATE_FROM").replace('/', '-');
            _dateTo = request.getParameter("DATE_TO") == null ? null : request.getParameter("DATE_TO").replace('/', '-');
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
        }
    }

}// クラスの括り
