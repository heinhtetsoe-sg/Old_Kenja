/*
 * $Id: 8dca308079fe0a5fccfa45a0bd6ca712e423b913 $
 *
 * 作成日: 2017/03/16
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


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

public class KNJL373Q {

    private static final Log log = LogFactory.getLog(KNJL373Q.class);

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
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL373Q.frm", 4);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String cntSql = sql("CNT");
            log.debug(" cntSql =" + cntSql);
            ps = db2.prepareStatement(cntSql);
            rs = ps.executeQuery();
            rs.next();
            final int totalCnt = rs.getInt("CNT");
            final int pageSu = totalCnt / 30;
            final int pageAmari = totalCnt % 30;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sql("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            final String nendo = _param._ctrlYear + "年度 ";
            svf.VrsOut("TITLE", nendo + "　駿高実戦模試欠席者一覧 ");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._ctrlDate));

            int pageCnt = 1;
            int maxLine = 30;
            int lineCnt = 1;
            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                final String placearea = rs.getString("PLACEAREA");
                final String satNo = rs.getString("SAT_NO");
                final String name1 = rs.getString("NAME1");
                final String finschoolNameAbbv = rs.getString("FINSCHOOL_NAME_ABBV");
                final String groupname = rs.getString("GROUPNAME");
                final String cnt = rs.getString("CNT");

                svf.VrsOut("NOTICE", cnt);

                svf.VrsOut("PLACE", placearea);
                svf.VrsOut("EXAM_NO", satNo);
                svf.VrsOut("NAME", name1);
                svf.VrsOut("ATTEND_SCHOOL_NAME", finschoolNameAbbv);
                svf.VrsOut("GROUP_NAME", groupname);

                svf.VrEndRecord();
                lineCnt++;
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sql(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("CNT".equals(selectDiv)) {
            stb.append("     COUNT(*) AS CNT ");
        } else {
            stb.append("     t1.YEAR, ");
            stb.append("     t1.PLACECD, ");
            stb.append("     t2.PLACEAREA, ");
            stb.append("     t1.SAT_NO, ");
            stb.append("     t1.NAME1, ");
            stb.append("     t1.SCHOOLCD, ");
            stb.append("     t3.FINSCHOOL_NAME_ABBV, ");
            stb.append("     t1.GROUPCD, ");
            stb.append("     t4.GROUPNAME, ");
            stb.append("     t5.CNT ");
        }
        stb.append(" FROM ");
        stb.append("     ( ");
        stb.append("     SELECT ");
        stb.append("         YEAR, ");
        stb.append("         SAT_NO, ");
        stb.append("         NAME1, ");
        stb.append("         PLACECD, ");
        stb.append("         SCHOOLCD, ");
        stb.append("         GROUPCD ");
        stb.append("     FROM ");
        stb.append("         SAT_APP_FORM_MST ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._ctrlYear + "' AND ");
        stb.append("         ABSENCE = 0 ");
        stb.append("     ) t1 ");
        stb.append("     left join SAT_EXAM_PLACE_DAT t2 on t1.PLACECD = t2.PLACECD and t2.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     left join FINSCHOOL_MST t3 on t1.SCHOOLCD = t3.FINSCHOOLCD and t3.FINSCHOOL_TYPE = '3' ");
        stb.append("     left join SAT_GROUP_DAT t4 on t1.GROUPCD = t4.GROUPCD and t4.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     left join (SELECT ");
        stb.append("                     YEAR, ");
        stb.append("                     COUNT(*) as CNT ");
        stb.append("                 FROM ");
        stb.append("                     SAT_APP_FORM_MST ");
        stb.append("                 WHERE ");
        stb.append("                     YEAR = '" + _param._ctrlYear + "' AND ");
        stb.append("                     ABSENCE = 0 ");
        stb.append("                 GROUP BY ");
        stb.append("                     YEAR ");
        stb.append("                 ) t5 on t1.YEAR = t5.YEAR ");
        if (!"CNT".equals(selectDiv)) {
            stb.append(" ORDER BY ");
            stb.append("     t1.PLACECD, ");
            stb.append("     t1.SAT_NO ");
        }
        return stb.toString();
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
        final String _radio;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _staffcd;
        final String _printLogStaffcd;
        final String _printLogRemoteAddr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _radio              = request.getParameter("Radio");
            _ctrlYear           = request.getParameter("CTRL_YEAR");
            _ctrlSemester       = request.getParameter("CTRL_SEMESTER");
            _ctrlDate           = request.getParameter("CTRL_DATE");
            _staffcd            = request.getParameter("staffcd");
            _printLogStaffcd    = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
        }

    }
}

// eof

