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
public class KNJB213 {

    private static final Log log = LogFactory.getLog(KNJB213.class);
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
            if (printStudent(svf, staffList)) {
                hasData = true;
            }
        }
        return  hasData;
    }

    private boolean printStudent(final Vrw32alp svf, final List staffList) {
        boolean hasData = false;

        svf.VrSetForm("KNJB213.frm", 4);

        svf.VrsOut("TITLE", _param._year + "年度　" + _param._jobname + "一覧");
        svf.VrsOut("PRINT_DATE", _param._ctrlDate == null ? null : _param._ctrlDate.replace('-', '/'));
        for (final Iterator it = staffList.iterator(); it.hasNext();) {
            final Staff staff = (Staff) it.next();
            svf.VrsOut("STAFF_CD", staff.getStaffCd());
            svf.VrsOut("STAFF_NAME", staff._staffname);
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
        stb.append("     T2.JOBCD, ");
        stb.append("     T2.JOBNAME ");
        stb.append(" FROM ");
        stb.append("     V_STAFF_MST T1 ");
        stb.append("     LEFT JOIN V_JOB_MST T2 ON T2.YEAR = T1.YEAR AND T2.JOBCD = T1.JOBCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.JOBCD = '" + _param._jobcd + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.STAFFCD ");
        return stb.toString();
    }

    /** 職員マスタクラス */
    private class Staff {
        final String _staffcd;
        final String _staffname;

        Staff(
                final String staffcd,
                final String staffname
        ) {
            _staffcd = staffcd;
            _staffname = staffname;
        }

        private String getStaffCd() {
            String retStr = "";
            if (null != _param._simo && !"".equals(_param._simo)) {
                int len = _staffcd.length();
                final int intSimo = Integer.parseInt(_param._simo);
                retStr = _staffcd.substring(len - intSimo, len);
                for (int umeCnt = 0; umeCnt < len; umeCnt++) {
                    retStr = _param._fuseji + retStr;
                }
                final int retStrLen = retStr.length();
                retStr = retStr.substring(intSimo, retStrLen);
                return retStr;
            } else {
                return _staffcd;
            }
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
        final String _jobcd;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _simo;
        final String _fuseji;
        String _jobname;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _jobcd = request.getParameter("JOBCD");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _simo = request.getParameter("simo");
            _fuseji = request.getParameter("fuseji");
            try {
                _jobname = setJobInfoData(db2, _year, _jobcd);
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

        private String setJobInfoData(final DB2UDB db2, final String year, final String jobcd) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String name = null;
            try {
                final String sql = "SELECT JOBCD, JOBNAME FROM V_JOB_MST WHERE YEAR = '" + year + "' AND JOBCD = '" + jobcd + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    name = rs.getString("JOBNAME");
                }
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return name;
        }
    }

}// クラスの括り
