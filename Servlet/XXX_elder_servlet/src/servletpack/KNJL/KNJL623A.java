/*
 * $Id: ccd0902e99a05a3b9f68b85daf3079188b595636 $
 *
 * 作成日: 2018/01/10
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJL623A{

    private static final Log log = LogFactory.getLog(KNJL623A.class);

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
        final String fmtname = "KNJL623A.frm";
        svf.VrSetForm(fmtname, 1);
        PreparedStatement ps_hall = null;
        ResultSet rs_hall = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        DecimalFormat df1 = new DecimalFormat("00");

        try {
        	final String hallsql = hallsql();
            log.debug(" hallsql =" + hallsql);
            ps_hall = db2.prepareStatement(hallsql);
            rs_hall = ps_hall.executeQuery();

            while (rs_hall.next()) {
            	final String exHallCd = rs_hall.getString("EXAMHALLCD");
                final int capaCnt = rs_hall.getInt("CAPA_CNT");
                final int maxLine = 5;

                if (capaCnt <= 0) {
                	continue;
                }
                int lineCnt = 1;
                int pageCnt = 1;
                int colCnt = 1;
                for (int cnt = 1;cnt <= capaCnt;cnt++) {
                    final String putStr1 = exHallCd + " " + df1.format(cnt);
                    final String putStr2 = exHallCd + df1.format(cnt);
                    if (lineCnt > maxLine) {
                        lineCnt = 1;
                        if (2 == colCnt) {
                            svf.VrEndPage();
                            pageCnt = pageCnt + 1;
                        	colCnt = 1;
                        }else {
                        	colCnt = 2;
                        }
                    }
                    svf.VrsOutn("HALLNO" + colCnt, lineCnt, putStr1);
                    svf.VrsOutn("BARCODE" + colCnt, lineCnt, putStr2);

                    lineCnt++;
                    _hasData = true;
                }

                if (_hasData) {
                    svf.VrEndPage();
                }
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String hallsql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     HALL.EXAMHALL_NAME, ");
        stb.append("     HALL.CAPA_CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_HALL_YDAT HALL ");
        stb.append(" WHERE ");
        stb.append("     HALL.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND HALL.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND HALL.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND HALL.EXAMHALLCD IN " + SQLUtils.whereIn(true, _param._leftlist));
        stb.append(" ORDER BY HALL.EXAMHALLCD ASC");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71723 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginDate;
        private final String _loginYear;
        private final String[] _leftlist;
        final String _applicantdivName;
        final String _testdivAbbv1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _leftlist = request.getParameterValues("LEFT_LIST");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testDiv));
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

    }
}

// eof

