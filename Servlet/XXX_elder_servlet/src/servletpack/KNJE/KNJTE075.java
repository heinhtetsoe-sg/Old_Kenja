/*
 * $Id: 8208047214a766d5160deb42f222e6345da3eea2 $
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 滞納状況通知書
 */
public class KNJTE075 {

    private static final Log log = LogFactory.getLog(KNJTE075.class);

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
    
    private void VrsOut(final String[] field1, final String[] data, final Vrw32alp svf) {
        if (null == data) {
            return;
        }
        for (int i = 0; i < Math.min(field1.length, data.length); i++) {
            svf.VrsOut(field1[i], data[i]);
        }
    }
    
    private void printAddress(final Vrw32alp svf, final String addr1, final String addr2) {
        final String[] addr1a = KNJ_EditEdit.get_token(addr1, 50, 2);
        final String[] addr2a = KNJ_EditEdit.get_token(addr2, 50, 2);
        final List addr = new ArrayList();
        if (null != addr1a && !StringUtils.isBlank(addr1a[0])) addr.add(addr1a[0]);
        if (null != addr1a && !StringUtils.isBlank(addr1a[1])) addr.add(addr1a[1]);
        if (null != addr2a && !StringUtils.isBlank(addr2a[0])) addr.add(addr2a[0]);
        if (null != addr2a && !StringUtils.isBlank(addr2a[1])) addr.add(addr2a[1]);
        final String[] fieldsNo = new String[] {"1_2", "1_3", "2_2", "2_3"};
        for (int j = 0; j < addr.size(); j++) {
            svf.VrsOut("ADDRESSS" + fieldsNo[j], (String) addr.get(j));
        }
    }
	
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        if (null == _param._shuugakuNoStr) {
            return;
        }
        
        final List list = getSaikenJokyoList(db2);
        
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final SaikenJokyo saikenJokyo = (SaikenJokyo) it.next();
            svf.VrSetForm("KNJTE075.frm", 1);

            svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._outputDate));
            svf.VrsOut("GOVERNER", _param._chijiName);

            svf.VrsOut("ZIP_NO", saikenJokyo._zipcd);
            printAddress(svf, saikenJokyo._addr1, saikenJokyo._addr2);
//            svf.VrsOut("ADDRESS1", saikenJokyo._addr1);
//            VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(saikenJokyo._addr2, 40, 2), svf);
            svf.VrsOut("NAME", saikenJokyo._rentaiName);
            
            svf.VrsOut("P_NO", saikenJokyo._shuugakuNo);
            svf.VrsOut("P_NAME", saikenJokyo._shuugakuName);
            svf.VrsOut("LOAN_NAME", "京都府高等学校等修学資金返還金");
            svf.VrsOut("LOAN_KIND", saikenJokyo._shikinShubetsuName);
            svf.VrsOut("NONPAY_FROM", _param._shugakuDate.formatNentuki(saikenJokyo._minChoteiYm));
            svf.VrsOut("NONPAY_TO", _param._shugakuDate.formatNentuki(saikenJokyo._maxChoteiYm));
            svf.VrsOut("NONPAY_PERIOD", "（" + (NumberUtils.isNumber(saikenJokyo._minouMonth) ? _param._shugakuDate.keta(Integer.parseInt(saikenJokyo._minouMonth), 3) : "   ") + "箇月分）");
            svf.VrsOut("NONPAY_MONEY", saikenJokyo._minouGk);

            svf.VrsOut("INQ_NAME", "京都府教育庁指導部高校教育課");
            svf.VrsOut("INQ_ADDRESS", "〒612-0064");
            svf.VrsOut("INQ_ADDRESS2", "京都市伏見区桃山毛利長門西町");
            svf.VrsOut("INQ_ADDRESS3", "京都府総合教育センター内");
            svf.VrsOut("INQ_PHONE", "０７５‐５７４‐７５１８");

            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private List getSaikenJokyoList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String shinkenCd = rs.getString("SHINKEN_CD");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String rentaiName = rs.getString("RENTAI_NAME");
                final String kojinNo = rs.getString("KOJIN_NO");
                final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                final String shuugakuName = rs.getString("SHUUGAKU_NAME");
                final String namespare3 = rs.getString("NAMESPARE3");
                final String minChoteiYm = rs.getString("MIN_CHOTEI_YM");
                final String maxChoteiYm = rs.getString("MAX_CHOTEI_YM");
                final String minouMonth = rs.getString("MINOU_MONTH");
                final String minouGk = rs.getString("MINOU_GK");
                final String shiharaiHoho = rs.getString("SHIHARAI_HOHO");
                final String shikinShubetsuName = rs.getString("SHIKIN_SHUBETSU_NAME");
                
                final SaikenJokyo saikenjokyo = new SaikenJokyo(shinkenCd, zipcd, addr1, addr2, rentaiName, kojinNo, shuugakuNo, shuugakuName, namespare3, minChoteiYm, maxChoteiYm, minouMonth, minouGk, shiharaiHoho, shikinShubetsuName);
                list.add(saikenjokyo);
            }
       } catch (Exception ex) {
            log.fatal("exception!", ex);
       } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
       }
       return list;
    }
    
    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     V3.SHINKEN_CD, ");
        stb.append("     V3.ZIPCD, ");
        stb.append("     V3.ADDR1, ");
        stb.append("     V3.ADDR2, ");
        stb.append("     CONCAT(CONCAT(V3.FAMILY_NAME,'　'),V3.FIRST_NAME) AS RENTAI_NAME, ");
        stb.append("     V1.KOJIN_NO, ");
        stb.append("     X.SHUUGAKU_NO, ");
        stb.append("     CONCAT(CONCAT(V2.FAMILY_NAME,'　'),V2.FIRST_NAME) AS SHUUGAKU_NAME, ");
        stb.append("     T1.ABBV3, ");
        stb.append("     T1.NAMESPARE3, ");
        stb.append("     X.MIN_CHOTEI_YM, ");
        stb.append("     X.MAX_CHOTEI_YM, ");
        stb.append("     X.MINOU_MONTH, ");
        stb.append("     X.MINOU_GK, ");
        stb.append("     V4.SHIHARAI_HOHO, ");
        stb.append("     T2.ABBV1 AS SHIKIN_SHUBETSU_NAME ");
        stb.append(" FROM ");
        stb.append("     (SELECT ");
        stb.append("         SHUUGAKU_NO, ");
        stb.append("         MIN(CHOTEI_YM) AS MIN_CHOTEI_YM, ");
        stb.append("         MAX(CHOTEI_YM) AS MAX_CHOTEI_YM, ");
        stb.append("         COUNT(*) AS MINOU_MONTH, ");
        stb.append("         SUM(MINOU_GK) AS MINOU_GK ");
        stb.append("     FROM ");
        stb.append("         V_CHOTEI_NOUFU_ADD ");
        stb.append("     WHERE ");
        stb.append("         NOUFU_KIGEN IS NOT NULL AND ");
        stb.append("         MINOU_GK > 0 AND ");
        stb.append("         TOKUSOKU_DATE IS NOT NULL AND ");
        stb.append("         SHUUGAKU_NO IN " + _param._shuugakuNoStr + " ");
        stb.append("     GROUP BY ");
        stb.append("         SHUUGAKU_NO ");
        stb.append("     ) AS X ");
        stb.append("     LEFT JOIN V_KOJIN_SHUUGAKU_SHINSEI_HIST_DAT V1 ON X.SHUUGAKU_NO = V1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT V2 ON V1.KOJIN_NO = V2.KOJIN_NO ");
        stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT V3 ON V1.RENTAI_CD = V3.SHINKEN_CD ");
        stb.append("     LEFT JOIN NAME_MST T1 ON V1.SHIKIN_SHOUSAI_DIV = T1.NAMECD2 AND T1.NAMECD1 = 'T030' ");
        stb.append("     LEFT JOIN V_SAIKEN_JOKYO V4 ON X.SHUUGAKU_NO = V4.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN NAME_MST T2 ON T1.NAMESPARE3 = T2.NAMECD2 AND T2.NAMECD1 = 'T008' ");
        stb.append(" ORDER BY ");
        stb.append("     X.SHUUGAKU_NO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73790 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    
    private static class SaikenJokyo {
        final String _shinkenCd;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _rentaiName;
        final String _kojinNo;
        final String _shuugakuNo;
        final String _shuugakuName;
        final String _namespare3;
        final String _minChoteiYm;
        final String _maxChoteiYm;
        final String _minouMonth;
        final String _minouGk;
        final String _shiharaiHoho;
        final String _shikinShubetsuName;

        SaikenJokyo(
                final String shinkenCd,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String rentaiName,
                final String kojinNo,
                final String shuugakuNo,
                final String shuugakuName,
                final String namespare3,
                final String minChoteiYm,
                final String maxChoteiYm,
                final String minouMonth,
                final String minouGk,
                final String shiharaiHoho,
                final String shikinShubetsuName
        ) {
            _shinkenCd = shinkenCd;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _rentaiName = rentaiName;
            _kojinNo = kojinNo;
            _shuugakuNo = shuugakuNo;
            _shuugakuName = shuugakuName;
            _namespare3 = namespare3;
            _minChoteiYm = minChoteiYm;
            _maxChoteiYm = maxChoteiYm;
            _minouMonth = minouMonth;
            _minouGk = minouGk;
            _shiharaiHoho = shiharaiHoho;
            _shikinShubetsuName = shikinShubetsuName;
        }
    }

    /** パラメータクラス */
    private class Param {
        final String _shuugakuNoDiv;
        final String _shuugakuNoStr;
        final String _outputDate;
        final String _chijiName;
        final ShugakuDate _shugakuDate;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shuugakuNoDiv = request.getParameter("SHUUGAKU_NO_DIV");
            if ("2".equals(_shuugakuNoDiv)) {
                final String[] splits = StringUtils.split(request.getParameter("SHUUGAKU_NO_LIST"), ",");
                if (null == splits) {
                    _shuugakuNoStr = null;
                } else {
                    final StringBuffer stb = new StringBuffer();
                    String comma = "";
                    stb.append("(");
                    for (int i = 0; i < splits.length; i++) {
                        stb.append(comma);
                        stb.append("'").append(splits[i]).append("'");
                        comma = ",";
                    }
                    stb.append(")");
                    _shuugakuNoStr = stb.toString();
                }
            } else {
                if (StringUtils.isBlank(request.getParameter("SHUUGAKU_NO"))) {
                    _shuugakuNoStr = null;
                } else {
                    final StringBuffer stb = new StringBuffer();
                    stb.append("(");
                    stb.append("'").append(request.getParameter("SHUUGAKU_NO")).append("'");
                    stb.append(")");
                    _shuugakuNoStr = stb.toString();
                }
            }
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _outputDate = _shugakuDate.d7toDateStr(request.getParameter("OUTPUT_DATE"));
            _chijiName = getChijiName(db2);
        }
        
        
        private String getChijiName(DB2UDB db2) {
            String name = null;
            final String sql = " SELECT VALUE(CHIJI_YAKUSHOKU_NAME, '') || '　　' || VALUE(CHIJI_NAME, '') AS CHIJI_NAME FROM CHIJI_MST WHERE S_DATE = (SELECT MAX(S_DATE) FROM CHIJI_MST) ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    name = rs.getString("CHIJI_NAME");
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

