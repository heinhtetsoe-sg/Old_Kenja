/*
 * $Id: 82f42125cd2adb238067f0e177411a5872d1adad $
 *
 * 作成日: 2012/12/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 口座振替FD送付書
 */
public class KNJTE051 {

    private static final Log log = LogFactory.getLog(KNJTE051.class);

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
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            String shugakukinCount = null;
            String shitakukinCount = null;
            String shugakukinItakuCd = null;
            String shitakukinItakuCd = null;
            String shugakukinHenkanGk = null;
            String shitakukinHenkanGk = null;
            String bankname = null;
            String filename = null;
            
            while (rs.next()) {
                bankname = rs.getString("BANKNAME");
                filename = rs.getString("FILENAME");
                if ("1".equals(rs.getString("SHIKIN_SHUBETSU"))) {
                    shugakukinCount = rs.getString("COUNT");
                    shugakukinItakuCd = rs.getString("SHUGAKU_ITAKU_CD");
                    shugakukinHenkanGk = rs.getString("HENKAN_GK");
                } else if ("2".equals(rs.getString("SHIKIN_SHUBETSU"))) {
                    shitakukinCount = rs.getString("COUNT");
                    shitakukinItakuCd = rs.getString("SHITAKU_ITAKU_CD");
                    shitakukinHenkanGk = rs.getString("HENKAN_GK");
                }
                _hasData = true;
            }
            
            if (_hasData) {
                final String form = "KNJTE051.frm";
                svf.VrSetForm(form, 1);

                svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._tsuuchiDate, false));

                svf.VrsOut("NAME", bankname); // 御中
                svf.VrsOut("TELNO", "(075)574-7518");
                svf.VrsOut("CHARGE", ""); // 担当

                svf.VrsOut("PAYDAY", _param._shugakuDate.formatDate(_param._furikomiDate, false));
                svf.VrsOut("FILENAME", filename);
                svf.VrsOut("MONEY_SUM1", shugakukinCount);
                svf.VrsOut("MONEY1", shugakukinHenkanGk);
                svf.VrsOut("CODE1", shugakukinItakuCd);
                svf.VrsOut("MONEY_SUM2", shitakukinCount);
                svf.VrsOut("MONEY2", shitakukinHenkanGk);
                svf.VrsOut("CODE2", shitakukinItakuCd);

                svf.VrEndPage();
            }
            
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
	}

	private String sql() {
	    final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN AS ( ");
	    stb.append("  SELECT ");
	    stb.append("       V1.BANKCD, ");
	    stb.append("       T1.SHIKIN_SHUBETSU, ");
        stb.append("       MAX(T2.NAME1) AS SHUGAKU_ITAKU_CD, ");
        stb.append("       MAX(T2.NAME2) AS SHITAKU_ITAKU_CD, ");
        stb.append("       MAX(T2.NAME3) AS FILENAME, ");
	    stb.append("       COUNT(*) AS COUNT, ");
	    stb.append("       SUM(HENKAN_GK) AS HENKAN_GK ");
	    stb.append("   FROM ");
	    stb.append("       V_CHOTEI_NOUFU V1 ");
        stb.append("       LEFT JOIN SAIKEN_DAT T1 ON V1.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
        stb.append("       LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'T044' AND T2.NAMECD2 = V1.BANKCD ");
	    stb.append("   WHERE ");
        stb.append("       V1.SHUNO_FLG = '0' AND ");
	    stb.append("       V1.NOUFU_KIGEN_ORG = '" + _param._furikomiDate + "' AND ");
	    stb.append("       V1.BANKCD = '" + _param._bankcd + "' AND ");
	    stb.append("       V1.SHIHARAI_HOHO_CD = '1' AND ");
	    stb.append("       V1.TORIKESI_FLG = '0'  ");
	    stb.append("   GROUP BY ");
	    stb.append("       V1.BANKCD, ");
	    stb.append("       T1.SHIKIN_SHUBETSU ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("      T1.BANKCD, ");
        stb.append("      T1.SHIKIN_SHUBETSU, ");
        stb.append("      T1.SHUGAKU_ITAKU_CD, ");
        stb.append("      T1.SHITAKU_ITAKU_CD, ");
        stb.append("      T1.FILENAME, ");
        stb.append("      T1.COUNT, ");
        stb.append("      T1.HENKAN_GK, ");
        stb.append("      T3.BANKNAME ");
        stb.append(" FROM ");
        stb.append("      MAIN T1 ");
        stb.append("      LEFT JOIN (SELECT BANKCD, MIN(BANKNAME) AS BANKNAME ");
        stb.append("                 FROM BANK_MST T3 ");
        stb.append("                 GROUP BY BANKCD) T3 ON T3.BANKCD = T1.BANKCD ");
	    return stb.toString();
	}

	/** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67236 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }
    
    /** パラメータクラス */
    private class Param {
        final String _bankcd;
        final String _furikomiDate;
        final String _tsuuchiDate;
        final String _prgid;
        final String _chijiName;
        final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _prgid = request.getParameter("PRGID");
            _bankcd = request.getParameter("BANKCD");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _furikomiDate = _shugakuDate.d7toDateStr(request.getParameter("FURIKOMI_DATE"));
            _tsuuchiDate = _shugakuDate.d7toDateStr(request.getParameter("TSUUCHI_DATE"));
            _chijiName = _shugakuDate.getChijiName3(db2);
        }
    }
}

// eof

