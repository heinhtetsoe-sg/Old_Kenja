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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * クラス別希望状況一覧
 */
public class KNJB0021 {

    private static final Log log = LogFactory.getLog(KNJB0021.class);
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

            final List staffList = createStaffInfoData(db2);
            _param._periodList = createPeriodInfoData(db2);
            _param._weekList = createWeekInfoData();
            if (printMain(svf, staffList)) {
                _hasData = true;
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
    private boolean printMain(final Vrw32alp svf, final List staffList) throws Exception {
        boolean hasData = false;
        if (staffList.size() > 0) {
            boolean hasStfPossData = false; //"×"があるか
            for (final Iterator it = staffList.iterator(); it.hasNext();) {
                final Staff staff = (Staff) it.next();
                final int stfPossCnt = staff._stfPossMap.size();
                if (0 < stfPossCnt) {
                    hasStfPossData = true;
                }
                log.debug("stfPossCnt=" + stfPossCnt);
            }
            if (hasStfPossData) {
                if (printStudent(svf, staffList)) {
                    hasData = true;
                }
            }
        }
        return  hasData;
    }
    
    private boolean printStudent(final Vrw32alp svf, final List staffList) {
        boolean hasData = false;

        final int maxLine = 45;
        int line = 0;
        int pageKeep = 0;
        List pageList = new ArrayList();
        List curentPageStaffList = new ArrayList();
        for (final Iterator it = staffList.iterator(); it.hasNext();) {
            final Staff staff = (Staff) it.next();
            line++;
            final int page = (line % maxLine == 0) ? line / maxLine : line / maxLine + 1;
            if (pageKeep != page) {
                curentPageStaffList = new ArrayList();
                pageList.add(curentPageStaffList);
                pageKeep = page;
            }
            curentPageStaffList.add(staff);
        }
        
        for (final Iterator itPage = pageList.iterator(); itPage.hasNext();) {
            final List pageStaffList = (List) itPage.next();
            svf.VrSetForm("KNJB0021.frm", 4);
            svf.VrsOut("TITLE", (_param._sectionname == null ? "" : _param._sectionname) + "条件一覧");
            svf.VrsOut("NENDO", _param._year + "年度");
            svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
            for (final Iterator itw = _param._weekList.iterator(); itw.hasNext();) {
                final Week week = (Week) itw.next();
                svf.VrsOut("WEEK", week._dayname);
                int p = 0;
                for (final Iterator itp = _param._periodList.iterator(); itp.hasNext();) {
                    final Period period = (Period) itp.next();
                    p++;
                    svf.VrsOut("PERIOD" + String.valueOf(p), period._periodabbv);
                    int gyo = 0;
                    for (final Iterator itStaff = pageStaffList.iterator(); itStaff.hasNext();) {
                        final Staff staff = (Staff) itStaff.next();
                        gyo++;
                        svf.VrsOutn("STAFF_CD", gyo, staff._staffcd);
                        svf.VrsOutn("STAFF_NAME", gyo, staff._staffname);
                        if (staff._stfPossMap.containsKey(week._daycd + period._periodcd)) {
                            svf.VrsOutn("BAD" + String.valueOf(p), gyo, "×");
                            hasData = true;
                        }
                    }
                }
                svf.VrEndRecord();
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
    
    private List createStaffInfoData(final DB2UDB db2) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getStaffInfoSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Staff staffInfo = new Staff(
                        rs.getString("STAFFCD"),
                        rs.getString("STAFFNAME")
                        );
                rtnList.add(staffInfo);
                staffInfo._stfPossMap = staffInfo.createStfPossInfoData(db2, staffInfo._staffcd);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    
    private String getStaffInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.STAFFCD, ");
        stb.append("     T1.STAFFNAME, ");
        stb.append("     T2.SECTIONCD, ");
        stb.append("     T2.SECTIONNAME ");
        stb.append(" FROM ");
        stb.append("     V_STAFF_MST T1 ");
        stb.append("     LEFT JOIN V_SECTION_MST T2 ON T2.YEAR = T1.YEAR AND T2.SECTIONCD = T1.SECTIONCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        if (!"ALL".equals(_param._sectioncd)) {
            stb.append("     AND T1.SECTIONCD = '" + _param._sectioncd + "' ");
        }
        if (_param._printFukaOnly) {
            stb.append("     AND T1.STAFFCD IN (SELECT STAFFCD FROM STAFF_OPEIMPOSS_DAT WHERE YEAR = '" + _param._year + "' GROUP BY STAFFCD) ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.STAFFCD ");
        return stb.toString();
    }
    
    /** 職員マスタクラス */
    private class Staff {
        final String _staffcd;
        final String _staffname;
        Map _stfPossMap = new HashMap();
        
        Staff(
                final String staffcd,
                final String staffname
        ) {
            _staffcd = staffcd;
            _staffname = staffname;
        }
        
        private Map createStfPossInfoData(final DB2UDB db2, final String staffcd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtnMap = new HashMap();
            try {
                final String sql = getStfPossInfoSql(staffcd);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final StfPoss stfPossInfo = new StfPoss(
                            rs.getString("DAYCD"),
                            rs.getString("PERIODCD")
                            );
                    rtnMap.put(rs.getString("DAYCD") + rs.getString("PERIODCD"), stfPossInfo);
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnMap;
        }
    }
    
    private String getStfPossInfoSql(final String staffcd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.DAYCD, ");
        stb.append("     T1.PERIODCD ");
        stb.append(" FROM ");
        stb.append("     STAFF_OPEIMPOSS_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.STAFFCD = '" + staffcd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.DAYCD, ");
        stb.append("     T1.PERIODCD ");
        return stb.toString();
    }
    
    /** 教師稼動不可設定クラス */
    private class StfPoss {
        final String _daycd;
        final String _periodcd;
        
        StfPoss(
                final String daycd,
                final String periodcd
        ) {
            _daycd = daycd;
            _periodcd = periodcd;
        }
    }
    
    private List createWeekInfoData() throws Exception {
        final List rtnList = new ArrayList();

        final String[] name = {null, null, "月", "火", "水", "木", "金", "土"};
        final String[] abbv = {null, null, "月", "火", "水", "木", "金", "土"};
        for (int w = 2; w <= 7; w++) {
            final String daycd = String.valueOf(w);
            final String daycdname = name[w] + "曜日";
            final String daycdabbv = abbv[w];
            final Week weekInfo = new Week(
                    daycd,
                    daycdname,
                    daycdabbv
                    );
            rtnList.add(weekInfo);
        }

        return rtnList;
    }
    
    /** 曜日クラス */
    private class Week {
        final String _daycd;
        final String _dayname;
        final String _dayabbv;
        
        Week(
                final String daycd,
                final String dayname,
                final String dayabbv
        ) {
            _daycd = daycd;
            _dayname = dayname;
            _dayabbv = dayabbv;
        }
    }
    
    private List createPeriodInfoData(final DB2UDB db2) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getPeriodInfoSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Period periodInfo = new Period(
                        rs.getString("PERIODCD"),
                        rs.getString("PERIOD_NAME"),
                        rs.getString("PERIOD_ABBV")
                        );
                rtnList.add(periodInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    
    private String getPeriodInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH S_E_PERIODCD AS ( ");
        stb.append("     SELECT ");
        stb.append("         MIN(T2.S_PERIODCD) AS S_PERIODCD, ");
        stb.append("         MAX(T2.E_PERIODCD) AS E_PERIODCD ");
        stb.append("     FROM ");
        stb.append("         COURSE_YDAT T1 ");
        stb.append("         INNER JOIN COURSE_MST T2 ON T2.COURSECD = T1.COURSECD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.NAMECD2 AS PERIODCD, ");
        stb.append("     T1.NAME1 AS PERIOD_NAME, ");
        stb.append("     T1.ABBV1 AS PERIOD_ABBV ");
        stb.append(" FROM ");
        stb.append("     V_NAME_MST T1 ");
        stb.append("     INNER JOIN S_E_PERIODCD T2 ON T1.NAMECD2 BETWEEN T2.S_PERIODCD AND T2.E_PERIODCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.NAMECD1 = 'B001' ");
        stb.append(" ORDER BY ");
        stb.append("     PERIODCD ");
        return stb.toString();
    }
    
    /** 校時クラス */
    private class Period {
        final String _periodcd;
        final String _periodname;
        final String _periodabbv;
        
        Period(
                final String periodcd,
                final String periodname,
                final String periodabbv
        ) {
            _periodcd = periodcd;
            _periodname = periodname;
            _periodabbv = periodabbv;
        }
    }
    
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(db2, request);
    }

    private static class Param {

        final String _year;
        final String _sectioncd;
        final boolean _printFukaOnly;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        String _sectionname;
        List _periodList = new ArrayList();
        List _weekList = new ArrayList();
        
        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _sectioncd = request.getParameter("SECTIONCD");
            _printFukaOnly = "on".equals(request.getParameter("CHK_FUKA"));
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            try {
                _sectionname = "ALL".equals(_sectioncd) ? "職員" : setJobInfoData(db2, _year, _sectioncd);
            } catch (SQLException e) {
                // TODO 自動生成された catch ブロック
                log.debug("exception!", e);
            }
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
        }
        
        private String setJobInfoData(final DB2UDB db2, final String year, final String sectioncd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String name = null;
            try {
                final String sql = "SELECT SECTIONCD, SECTIONNAME FROM V_SECTION_MST WHERE YEAR = '" + year + "' AND SECTIONCD = '" + sectioncd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    name = rs.getString("SECTIONNAME");
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return name;
        }
    }
    
}// クラスの括り
