/*
 * $Id: 8a66f64b0613cab7e3cc7a036af0dbb6184a8154 $
 *
 * 作成日: 2013/01/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * KNJTE082 宛名シール印刷
 */
public class KNJTE082 {

    private static final Log log = LogFactory.getLog(KNJTE082.class);

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

    private List getAddressList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                String name = rs.getString("NAME");
                if ("3".equals(_param._output) && null != name) {
                    name += "　様";
                }
                final Address address = new Address(zipcd, addr1, addr2, name);
                list.add(address);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }
    
    private static int getMS932Length(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }
        return 0;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJTE082.frm", 1);
        final List list = getAddressList(db2);
        log.debug(" size = " + list.size());
        // int page = 1;
        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            if (count >= 20) {
                svf.VrEndPage();
                // page += 1;
                count = 0;
            }
            count += 1;
            final Address address = (Address) list.get(i);
            
            final int col = (count % 4 == 0) ? 4 : count % 4;
            final int line = count / 4 + (count % 4 == 0 ? 0 : 1); 
            final int addr1len = getMS932Length(address._addr1);
            final int addr2len = getMS932Length(address._addr2);
            if (addr1len > 50 || addr2len > 50) {
                final List addrList = new ArrayList();
                final String[] addr1 = KNJ_EditEdit.get_token(address._addr1, 50, 2);
                final String[] addr2 = KNJ_EditEdit.get_token(address._addr2, 50, 2);
                if (null != addr1 && null != addr1[0]) addrList.add(addr1[0]);
                if (null != addr1 && null != addr1[1]) addrList.add(addr1[1]);
                if (null != addr2 && null != addr2[0]) addrList.add(addr2[0]);
                if (null != addr2 && null != addr2[1]) addrList.add(addr2[1]);
                for (int j = 0; j < addrList.size(); j++) {
                    svf.VrsOutn("ADDRESS" + col + "_" + (j + 1) + "_2", line, (String) addrList.get(j)); // 住所
                }
            } else {
                final String addrSuf = (addr1len > 40 || addr2len > 40) ? "2" : "1";
                svf.VrsOutn("ADDRESS" + col + "_1_" + addrSuf, line, address._addr1); // 住所
                svf.VrsOutn("ADDRESS" + col + "_2_" + addrSuf, line, address._addr2); // 住所
            }
            
            //log.debug(" p:l:c:c = " + page + ":" + line + ":" + col + ":" + count + "  " + address._name);
            
            if (null != address._zipcd) {
                svf.VrsOutn("ZIPCODE" + col, line, "〒" + address._zipcd); // 郵便番号
            }
            svf.VrsOutn("NAME" + col + "_" + (getMS932Length(address._name) > 36 ? "4" : getMS932Length(address._name) > 24  ? "3" : "1"), line, address._name); // 氏名
            if (!"3".equals(_param._output)) {
                String name2 = "";
                if ("1".equals(_param._output)) {
                    name2 = "1".equals(_param._jTantouDiv) ? "学校長" : "2".equals(_param._jTantouDiv) ? "修学支援担当者" : ""; 
                } else if ("2".equals(_param._output)) {
                    name2 = "1".equals(_param._hTantouDiv) ? "学校長" : "2".equals(_param._hTantouDiv) ? "修学支援担当者" : ""; 
                }
                svf.VrsOutn("NAME" + col + "_2", line, name2 + "　様"); // 氏名2
            }
            _hasData = true;
        }
        if (count > 0) {
            svf.VrEndPage();
        }
    }
    
    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        if ("3".equals(_param._output)) {
            if ("1".equals(_param._addrDiv)) {
                stb.append(" SELECT ");
                stb.append("     CASE ");
                stb.append("          WHEN S1.ZIP_CD IS NOT NULL THEN S1.ZIP_CD ");
                stb.append("          WHEN S2.ZIP_CD IS NOT NULL THEN S2.ZIP_CD ");
                stb.append("          ELSE V1.ZIPCD ");
                stb.append("     END AS ZIPCD, ");
                stb.append("     CASE ");
                stb.append("          WHEN S1.ADDR1 IS NOT NULL THEN S1.ADDR1 ");
                stb.append("          WHEN S2.ADDR1 IS NOT NULL THEN S2.ADDR1 ");
                stb.append("          ELSE V1.ADDR1 ");
                stb.append("     END AS ADDR1, ");
                stb.append("     CASE ");
                stb.append("          WHEN S1.ADDR2 IS NOT NULL THEN S1.ADDR2 ");
                stb.append("          WHEN S2.ADDR2 IS NOT NULL THEN S2.ADDR2 ");
                stb.append("          ELSE V1.ADDR2 ");
                stb.append("     END AS ADDR2, ");
                stb.append("     CASE ");
                stb.append("          WHEN S1.SOUFU_NAME IS NOT NULL THEN S1.SOUFU_NAME ");
                stb.append("          WHEN S2.SOUFU_NAME IS NOT NULL THEN S2.SOUFU_NAME ");
                stb.append("          ELSE CONCAT(CONCAT(V1.FAMILY_NAME,'　'), V1.FIRST_NAME) ");
                stb.append("     END AS NAME ");
                stb.append(" FROM ");
                stb.append("     V_KOJIN_SHUUGAKU_SHINSEI_HIST_DAT T1 ");
                stb.append("     INNER JOIN V_KOJIN_HIST_DAT V1 ON V1.KOJIN_NO = T1.KOJIN_NO ");
                stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S1 ON V1.KOJIN_NO = S1.KOJIN_NO AND T1.SHUUGAKU_NO = S1.SHUUGAKU_NO ");
                stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S2 ON V1.KOJIN_NO = S2.KOJIN_NO AND S2.SHUUGAKU_NO = '9999999' ");
                stb.append(" WHERE ");
                stb.append("     T1.SHUUGAKU_NO IN " + SQLUtils.whereIn(true, _param._shuugakuNos));
                stb.append(" ORDER BY ");
                stb.append("     T1.KOJIN_NO ");
            } else if ("2".equals(_param._addrDiv)) {
                stb.append(" SELECT ");
                stb.append("     V1.ZIPCD, ");
                stb.append("     V1.ADDR1, ");
                stb.append("     V1.ADDR2, ");
                stb.append("     CONCAT(CONCAT(V1.FAMILY_NAME, '　'), V1.FIRST_NAME) AS NAME ");
                stb.append(" FROM ");
                stb.append("     V_KOJIN_SHUUGAKU_SHINSEI_HIST_DAT T1 ");
                stb.append("     INNER JOIN SHINKENSHA_HIST_DAT V1 ON V1.SHINKEN_CD = T1.RENTAI_CD ");
                stb.append(" WHERE ");
                stb.append("     T1.SHUUGAKU_NO IN " + SQLUtils.whereIn(true, _param._shuugakuNos));
                stb.append(" ORDER BY ");
                stb.append("     T1.KOJIN_NO ");
            } else if ("3".equals(_param._addrDiv)) {
                stb.append(" SELECT ");
                stb.append("     CASE WHEN V1.SHIHARAININ_KBN = '2' THEN V5.ZIPCD ");
                stb.append("          WHEN S1.ZIP_CD IS NOT NULL THEN S1.ZIP_CD ");
                stb.append("          WHEN S2.ZIP_CD IS NOT NULL THEN S2.ZIP_CD ");
                stb.append("          ELSE V3.ZIPCD ");
                stb.append("     END AS ZIPCD, ");
                stb.append("     CASE WHEN V1.SHIHARAININ_KBN = '2' THEN V5.ADDR1 ");
                stb.append("          WHEN S1.ADDR1 IS NOT NULL THEN S1.ADDR1 ");
                stb.append("          WHEN S2.ADDR1 IS NOT NULL THEN S2.ADDR1 ");
                stb.append("          ELSE V3.ADDR1 ");
                stb.append("     END AS ADDR1, ");
                stb.append("     CASE WHEN V1.SHIHARAININ_KBN = '2' THEN V5.ADDR2 ");
                stb.append("          WHEN S1.ADDR2 IS NOT NULL THEN S1.ADDR2 ");
                stb.append("          WHEN S2.ADDR2 IS NOT NULL THEN S2.ADDR2 ");
                stb.append("          ELSE V3.ADDR2 ");
                stb.append("     END AS ADDR2, ");
                stb.append("     CASE WHEN V1.SHIHARAININ_KBN = '2' THEN CONCAT(CONCAT(V5.FAMILY_NAME,'　'),V5.FIRST_NAME) ");
                stb.append("          WHEN S1.SOUFU_NAME IS NOT NULL THEN S1.SOUFU_NAME ");
                stb.append("          WHEN S2.SOUFU_NAME IS NOT NULL THEN S2.SOUFU_NAME ");
                stb.append("          ELSE CONCAT(CONCAT(V3.FAMILY_NAME,'　'),V3.FIRST_NAME) ");
                stb.append("     END AS NAME ");
                stb.append(" FROM ");
                stb.append("     V_SAIKEN_JOKYO V1 ");
                stb.append("     LEFT JOIN V_KOJIN_SHUUGAKU_SHINSEI_HIST_DAT V2 ON V1.SHUUGAKU_NO = V2.SHUUGAKU_NO ");
                stb.append("     LEFT JOIN V_KOJIN_HIST_DAT V3 ON V2.KOJIN_NO = V3.KOJIN_NO  ");
                stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT V5 ON V2.RENTAI_CD = V5.SHINKEN_CD ");
                stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S1 ON V2.KOJIN_NO = S1.KOJIN_NO AND V1.SHUUGAKU_NO = S1.SHUUGAKU_NO ");
                stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S2 ON V2.KOJIN_NO = S2.KOJIN_NO AND S2.SHUUGAKU_NO = '9999999' ");
                stb.append(" WHERE ");
                stb.append("     V1.SHUUGAKU_NO IN " + SQLUtils.whereIn(true, _param._shuugakuNos));
                stb.append(" ORDER BY ");
                stb.append("     V1.KOJIN_NO ");
            }
        } else {
            stb.append(" SELECT ");
            stb.append("     ZIPCD, ");
            stb.append("     ADDR1, ");
            stb.append("     ADDR2, ");
            stb.append("     NAME ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_DAT ");
            stb.append(" WHERE ");
            if ("1".equals(_param._output)) {
                stb.append("     SCHOOL_TYPE = '3' ");
            } else { // if ("2".equals(_param._output))
                stb.append("     SCHOOL_TYPE = '4' ");
            }
            stb.append("     AND SCHOOLCD IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
            stb.append(" ORDER BY ");
            stb.append("     SCHOOLCD ");
        }
        return stb.toString();
    }

    private static class Address {
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _name;
        Address(
                final String zipcd,
                final String addr1,
                final String addr2,
                final String name
        ) {
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _name = name;
        }
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
        final String _output;
        final String _jTantouDiv;
        final String _hTantouDiv;
        final String[] _categorySelected;
        final String _addrDiv;
        final String _ctrlYear;
        final String _loginDate;
        String[] _shuugakuNos;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _output = request.getParameter("OUTPUT");
            _jTantouDiv = request.getParameter("J_TANTOU_DIV"); // OUTPUT=1のとき1:学校長 2:修学支援担当者
            _hTantouDiv = request.getParameter("H_TANTOU_DIV"); // OUTPUT=2のとき1:学校長 2:修学支援担当者
            _addrDiv = request.getParameter("ADDR_DIV"); //  OUTPUT=3のとき1:本人宛 2:連帯保証人宛 3:口座名義人宛
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _loginDate = request.getParameter("LOGIN_DATE");
            if ("3".equals(_output)) {
                final List shuugakuNoList = new ArrayList();
                for (int i = 1; i <= 20; i++) {
                    if (StringUtils.isBlank(request.getParameter("SHUUGAKU_NO" + String.valueOf(i)))) {
                        continue;
                    }
                    shuugakuNoList.add(request.getParameter("SHUUGAKU_NO" + String.valueOf(i)));
                }
                _shuugakuNos = new String[shuugakuNoList.size()];
                for (int i = 0; i < _shuugakuNos.length; i++) {
                    _shuugakuNos[i] = (String) shuugakuNoList.get(i);
                }
                _categorySelected = new String[] {};
            } else {
                _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            }
        }
    }
}

// eof

