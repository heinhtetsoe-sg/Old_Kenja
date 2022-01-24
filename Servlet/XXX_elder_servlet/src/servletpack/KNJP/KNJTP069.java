/*
 * $Id: acb00001bbf64c91772bf1df65ae6a7ce05ac1c0 $
 *
 * 作成日: 2012/09/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 京都府修学金 継続申請書
 */
public class KNJTP069 {

    private static final Log log = LogFactory.getLog(KNJTP069.class);

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
    
    private void VrsOutDate(final String field1, final String data, final Vrw32alp svf) {
        if (null == data) {
            return;
        }
        svf.VrsOut(field1, _param._shugakuDate.formatDate(data, false));
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            final String sql = getTaiyoKeikakuSql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (null == rs.getString("ITEM")) {
                    break;
                }
                svf.VrSetForm("KNJTP069.frm", 1);

                svf.VrsOut("BANK_NAME", "京都府指定金融機関　殿");
                svf.VrsOut("DATE", _param._shugakuDate.nengo(_param._loginDate) + "    年    月    日");
                svf.VrsOut("ACCOUNT_NAME1", _param._suitouchouYakushokuName);
                svf.VrsOut("ACCOUNT_NAME2", _param._suitouchouName);

                svf.VrsOut("PAY_DAY", _param._shugakuDate.formatDate(_param._submitDate));
                svf.VrsOut("DATA_SEND_DAY", _param._shugakuDate.nengo(_param._loginDate) + "    年    月    日");
                
                svf.VrsOut("DATA_SEND_METHOD", "データ伝送");
                svf.VrsOut("CLIENT_CD", "1".equals(_param._shinseiDiv) ? "0001462902" : "0001463002");
                svf.VrsOut("PAY_METHOD", "1".equals(_param._shinseiDiv) ? "高等学校等修学資金貸付金口振" : "高等学校等修学支度金貸付金口振");
                
                final String[] itemcsv = StringUtils.split(rs.getString("ITEM"), ",");
                if (itemcsv.length >= 3) {
                    if (NumberUtils.isDigits(itemcsv[1])) {
                        svf.VrsOut("COUNT", itemcsv[1]);
                    }
                    if (NumberUtils.isDigits(itemcsv[2])) {
                        svf.VrsOut("MONEY", itemcsv[2]);
                    }
                }

                svf.VrEndPage();
                _hasData = true;
                break;
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    private String getTaiyoKeikakuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     ITEM ");
        stb.append(" FROM ");
        stb.append("     FURIKOMI_TAIYO_DAT ");
        stb.append(" WHERE ");
        stb.append("     S_KETTEI_DATE = '" + _param._ketteiDateFrom + "' ");
        stb.append("     AND E_KETTEI_DATE = '" + _param._ketteiDateTo + "' ");
        stb.append("     AND SHIKIN_SHUBETSU = '" + _param._shinseiDiv + "' ");
        stb.append("     AND S_SHITEI_YM = '" + _param._ketteiYmFrom + "' ");
        stb.append("     AND E_SHITEI_YM = '" + _param._ketteiYmTo + "' ");
        stb.append("     AND FURIKOMI_DATE = '" + _param._submitDate + "' ");
        stb.append("     AND DATA_DIV = '8' "); // 8:トレイラーレコード
        stb.append(" ORDER BY ");
        stb.append("     SEQ ");
        return stb.toString();
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67227 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _shinseiDiv;
        private final String _loginDate;
        private final String _ketteiDateFrom;
        private final String _ketteiDateTo;
        private final String _ketteiYmFrom;
        private final String _ketteiYmTo;
        private final String _submitDate;
        private String _suitouchouYakushokuName;
        private String _suitouchouName;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiDiv = request.getParameter("SHINSEI_DIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _ketteiDateFrom = _shugakuDate.d7toDateStr(request.getParameter("KETTEI_DATE_FROM"));
            _ketteiDateTo = _shugakuDate.d7toDateStr(request.getParameter("KETTEI_DATE_TO"));
            _ketteiYmFrom = _shugakuDate.d5toYmStr(request.getParameter("KETTEI_YM_FROM"));
            _ketteiYmTo = _shugakuDate.d5toYmStr(request.getParameter("KETTEI_YM_TO"));
            _submitDate = _shugakuDate.d7toDateStr(request.getParameter("SUBMIT_DATE"));
            setChijiName(db2);
        }

        private String setChijiName(DB2UDB db2) {
            String name = null;
            final String sql = " SELECT * FROM CHIJI_MST WHERE S_DATE = (SELECT MAX(S_DATE) FROM CHIJI_MST) ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _suitouchouYakushokuName = StringUtils.defaultString(rs.getString("SUITOUCHOU_YAKUSHOKU_NAME"));
                    _suitouchouName = StringUtils.defaultString(rs.getString("SUITOUCHOU_NAME"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }
    }
}

// eof

