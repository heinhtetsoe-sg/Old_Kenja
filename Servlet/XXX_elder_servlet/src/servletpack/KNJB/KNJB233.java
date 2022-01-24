package servletpack.KNJB;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 * クラス別希望状況一覧
 */
public class KNJB233 {

    private static final Log log = LogFactory.getLog(KNJB233.class);
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

            //1:全て 2:職員別
            if ("1".equals(_param._form)) {
                final List schChrDatList = createSchChrDatInfoData(db2, "STAFFCD_ALL");
                if (schChrDatList.size() > 0) {
                    if (printStaffAll(svf, schChrDatList)) {
                        _hasData = true;
                    }
                }
            } else {
                for (int i = 0; i < _param._categorySelected.length; i++) {
                    final List schChrDatList = createSchChrDatInfoData(db2, _param._categorySelected[i]);
                    if (schChrDatList.size() > 0) {
                        if (printStaff(svf, schChrDatList)) {
                            _hasData = true;
                        }
                    }
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
    
    private boolean printStaffAll(final Vrw32alp svf, final List schChrDatList) {
        boolean hasData = false;

        svf.VrSetForm("KNJB233_1.frm", 4);

        for (final Iterator it = schChrDatList.iterator(); it.hasNext();) {
            final SchChrDat schChrDat = (SchChrDat) it.next();

            final String subTitle = _param._year + "年度　" + schChrDat._month + "月分";
            svf.VrsOut("TITLE", "講座予実績　未登録データ一覧");
            svf.VrsOut("SUB_TITLE", subTitle);
            svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
            final int kensuu = schChrDatList.size();
            svf.VrsOut("UNREGIST", "未登録データ件数：　" + String.valueOf(kensuu));

            svf.VrsOut("DATE", schChrDat._day);
            svf.VrsOut("PERIOD", schChrDat._periodabbv);
            svf.VrsOut("CHAIRCD", schChrDat._chaircd);
            svf.VrsOut("CHAIR_NAME" + ((28 < getMS932ByteLength(schChrDat._chairname)) ? "2" : "1"), schChrDat._chairname);
            svf.VrsOut("TEACHER_NAME" + ((14 < getMS932ByteLength(schChrDat._staffname)) ? "2" : "1"), schChrDat._staffname);

            svf.VrEndRecord();
            hasData = true;
        }

        return hasData;
    }
    
    private boolean printStaff(final Vrw32alp svf, final List schChrDatList) {
        boolean hasData = false;

        svf.VrSetForm("KNJB233_2.frm", 4);

        for (final Iterator it = schChrDatList.iterator(); it.hasNext();) {
            final SchChrDat schChrDat = (SchChrDat) it.next();

            final String subTitle = _param._year + "年度　" + schChrDat._month + "月分";
            svf.VrsOut("TITLE", subTitle + "　講座予実績　未登録データ一覧");
            svf.VrsOut("STAFF_NO", schChrDat._staffcd);
            svf.VrsOut("STAFF_NAME", schChrDat._staffname);
            svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
            final int kensuu = schChrDatList.size();
            svf.VrsOut("UNREGIST", "未登録データ件数：　" + String.valueOf(kensuu));

            svf.VrsOut("DATE", schChrDat._day + "(" + KNJ_EditDate.h_format_W(schChrDat._executedate) + ")");
            svf.VrsOut("PERIOD", schChrDat._periodabbv);
            svf.VrsOut("CHAIRCD", schChrDat._chaircd);
            svf.VrsOut("CHAIR_NAME" + ((36 < getMS932ByteLength(schChrDat._chairname)) ? "2" : "1"), schChrDat._chairname);

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
    
    private List createSchChrDatInfoData(final DB2UDB db2, final String staffcd) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtnList = new ArrayList();
        try {
            final String sql = getSchChrDatInfoSql(staffcd);
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
                        rs.getString("CHAIRCD"),
                        rs.getString("CHAIRNAME"),
                        rs.getString("STAFFCD"),
                        rs.getString("STAFFNAME"),
                        rs.getString("EXECUTEDIV"),
                        rs.getString("EXECUTEDIV_NAME"),
                        rs.getString("EXECUTEDIV_ABBV")
                        );
                rtnList.add(schChrDatInfo);
            }
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
        return rtnList;
    }
    
    private String getSchChrDatInfoSql(final String staffcd) {
        final StringBuffer stb = new StringBuffer();
        //1:全て 2:職員別
        if ("1".equals(_param._form)) {
            stb.append(" WITH MIN_CHAIR_STF AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         MIN(T1.STAFFCD) AS STAFFCD ");
            stb.append("     FROM ");
            stb.append("         CHAIR_STF_DAT T1 ");
            stb.append("         INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("         AND T1.CHARGEDIV = 1 ");
            stb.append("     GROUP BY ");
            stb.append("         T1.CHAIRCD ");
            stb.append(" ) ");
        }
        stb.append(" SELECT ");
        stb.append("     MONTH(T1.EXECUTEDATE) AS MONTH, ");
        stb.append("     DAY(T1.EXECUTEDATE) AS DAY, ");
        stb.append("     WEEK_ISO(T1.EXECUTEDATE) AS WEEK_ISO, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.EXECUTEDIV, ");
        stb.append("     T2.STAFFCD, ");
        stb.append("     T3.STAFFNAME, ");
        stb.append("     T4.CHAIRNAME, ");
        stb.append("     N1.NAME1 AS PERIOD_NAME, ");
        stb.append("     N1.ABBV1 AS PERIOD_ABBV, ");
        stb.append("     N2.NAME1 AS EXECUTEDIV_NAME, ");
        stb.append("     N2.ABBV1 AS EXECUTEDIV_ABBV ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_DAT T4 ");
        stb.append("             ON  T4.YEAR = T1.YEAR ");
        stb.append("             AND T4.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T4.CHAIRCD = T1.CHAIRCD ");
        //1:全て 2:職員別
        if ("1".equals(_param._form)) {
            stb.append("     INNER JOIN MIN_CHAIR_STF T2 ON T2.CHAIRCD = T1.CHAIRCD ");
        } else {
            stb.append("     INNER JOIN CHAIR_STF_DAT T2 ");
            stb.append("             ON  T2.YEAR = T1.YEAR ");
            stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("             AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("             AND T2.STAFFCD = '" + staffcd + "' ");
        }
        stb.append("     INNER JOIN STAFF_MST T3 ON T3.STAFFCD = T2.STAFFCD ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'B001' AND N1.NAMECD2 = T1.PERIODCD ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'C009' AND N2.NAMECD2 = T1.EXECUTEDIV ");
        stb.append(" WHERE ");
        stb.append("     T1.EXECUTEDATE BETWEEN DATE('" + _param._dateFrom + "') AND DATE('" + _param._dateTo + "') ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND VALUE(T1.EXECUTEDIV, '0') = '0' "); //0:未
        stb.append(" ORDER BY ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.CHAIRCD ");
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
        final String _chaircd;
        final String _chairname;
        final String _staffcd;
        final String _staffname;
        final String _executediv;
        final String _executedivname;
        final String _executedivabbv;
        
        SchChrDat(
                final String month,
                final String day,
                final String weekIso,
                final String executedate,
                final String periodcd,
                final String periodname,
                final String periodabbv,
                final String chaircd,
                final String chairname,
                final String staffcd,
                final String staffname,
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
            _chaircd = chaircd;
            _chairname = chairname;
            _staffcd = staffcd;
            _staffname = staffname;
            _executediv = executediv;
            _executedivname = executedivname;
            _executedivabbv = executedivabbv;
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
        final String _month;
        final String _form;
        final String[] _categorySelected; //学年・組
        final String _dateFrom;
        final String _dateTo;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        
        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year      = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("SEMESTER");
            _month = request.getParameter("MONTH");
            _form = request.getParameter("FORM");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _dateFrom = request.getParameter("DATE_FROM") == null ? null : request.getParameter("DATE_FROM").replace('/', '-');
            _dateTo = request.getParameter("DATE_TO") == null ? null : request.getParameter("DATE_TO").replace('/', '-');
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

        private String getDate(final String date) {
            if (date == null) {
                return null;
            }
            return KNJ_EditDate.h_format_S(date, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(date));
        }
    }
    
}// クラスの括り
