/*
 * $Id: e34ad968a8ca798b0b7a9d2c22c8cb69fe798e24 $
 *
 * 作成日: 2012/12/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CustomerBarcode;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 督促状
 */
public class KNJTE067 {

    private static final Log log = LogFactory.getLog(KNJTE067.class);

    private boolean _hasData;
    
    private static DecimalFormat df2 = new DecimalFormat("00");

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
            if (null != _param) {
                for (final Iterator it = _param._psMap.values().iterator(); it.hasNext();) {
                    final PreparedStatement ps = (PreparedStatement) it.next();
                    DbUtils.closeQuietly(ps);
                }
            }

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

    private List getShuugakuseiList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        String tokusokuDate = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     V_CHOTEI_NOUFU_ADD  ");
            stb.append(" WHERE ");
            stb.append("     TOKUSOKU_DATE = '" + _param._outputDate + "' ");
            stb.append("     AND CHOTEI_YM = '" + _param._choteiYm + "' ");

            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            
            if (rs.next()) {
                if (0 != rs.getInt("COUNT")) { // すでに督促ずみ
                    tokusokuDate = _param._outputDate;
                    log.fatal("指定督促日付(" + _param._outputDate + ")のデータがあります。");
                }
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            final String sql = sql(tokusokuDate);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String kojinNo = rs.getString("KOJIN_NO");
                Kojin kojin = getKojin(list, kojinNo);
                if (null == kojin) {
                    kojin = new Kojin(kojinNo, rs.getString("ZIPCD"),  rs.getString("NAME"),  rs.getString("ADDR1"),  rs.getString("ADDR2"));
                    list.add(kojin);
                }
                kojin._shugakuseiList.add(new Shuugakusei(rs.getString("SHUUGAKU_NO"), rs.getString("SHIKIN_SHUBETSU"), rs.getString("CHOTEI_KAISU"), rs.getString("CHOTEI_YM"),  rs.getString("NOUFU_KIGEN_ORG"),  rs.getString("MINOU_GK")));
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }
    
    private Kojin getKojin(final List list, final String kojinNo) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Kojin kojin = (Kojin) it.next();
            if (kojin._kojinNo.equals(kojinNo)) {
                return kojin;
            }
        }
        return null;
    }
    
    
    private void svfPrintAddress(final DB2UDB db2, final String[] field, final String zipCd, final String address1, final String address2, final Vrw32alp svf) {
        final String[] addr1 = KNJ_EditEdit.get_token(address1, 50, 2);
        final String[] addr2 = KNJ_EditEdit.get_token(address2, 50, 2);
        final List addr = new ArrayList();
        if (null != addr1 && !StringUtils.isBlank(addr1[0])) addr.add(addr1[0]);
        if (null != addr1 && !StringUtils.isBlank(addr1[1])) addr.add(addr1[1]);
        if (null != addr2 && !StringUtils.isBlank(addr2[0])) addr.add(addr2[0]);
        if (null != addr2 && !StringUtils.isBlank(addr2[1])) addr.add(addr2[1]);
        for (int j = 0; j < Math.min(field.length, addr.size()); j++) {
            svf.VrsOut(field[j], (String) addr.get(j));
        }
        
        final String customerBarcode = CustomerBarcode.getCustomerBarcode(db2, _param._psMap, zipCd, StringUtils.defaultString(address1) + StringUtils.defaultString(address2));
        svf.VrsOut("CUSTOMER_BARCODE", customerBarcode);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List list = getShuugakuseiList(db2);
        
        int pageall = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Kojin kojin = (Kojin) it.next();
            
            for (int p = 0; p < kojin._shugakuseiList.size() / 2 + (kojin._shugakuseiList.size() % 2 != 0 ? 1 : 0); p++) {
                svf.VrSetForm("KNJTE067.frm", 1);
                pageall += 1;
                
                svf.VrsOut("STAMP", getImageFile("SAINYU_CHOSHUSHA.bmp", "image/stamp"));
                
                svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._outputDate, false));
                svf.VrsOut("ZIP_NO", kojin._zipCd);
                svfPrintAddress(db2, new String[]{"ADDRESSS1_2", "ADDRESSS1_3", "ADDRESSS2_2", "ADDRESSS2_3"}, kojin._zipCd, kojin._addr1, kojin._addr2, svf);
                svf.VrsOut("NAME", kojin._name);
                svf.VrsOut("GOVERNER", "京都府歳入徴収者　京都府知事");
                svf.VrsOut("AFFILI", "京都府教育庁　指導部　高校教育課");
                svf.VrsOut("NAME1", kojin._name);
                
                final String warekinendo = _param._shugakuDate.nengoNendo(_param._choteiYm + "-01", false)[1];
                svf.VrsOut("NENDO", "元".equals(warekinendo) ? "01" : NumberUtils.isDigits(warekinendo) ? df2.format(Integer.parseInt(warekinendo)) : warekinendo);
                svf.VrsOut("ACCOUNT", "01");
                svf.VrsOut("BUDGET1", "1");
                svf.VrsOut("SUBJECT", "14-03-09-02-0001-0001");

                final Shuugakusei s1 = (Shuugakusei) kojin._shugakuseiList.get(p * 2 + 0);
                svf.VrsOut("LIMIT_DAY1", _param._shugakuDate.formatDate(s1._noufuKigenOrg, false));
                svf.VrsOut("LIMIT_DAY2", _param._shugakuDate.formatDate(_param._noufuKigen, false));

                print(svf, "1", s1);
                boolean print1 = false;
                if (null != s1._shuugakuNo || null != s1._choteiKaisu) {
                    svf.VrsOut("REM", StringUtils.defaultString(s1._shuugakuNo) + " " + StringUtils.defaultString(s1._choteiKaisu) + "      " + String.valueOf(pageall));
                    print1 = true;
                    // log.debug(" kojinNo = " + kojin._kojinNo + ", shuugakuNo1 = " + s1._shuugakuNo);
                }

                if (kojin._shugakuseiList.size() > p * 2 + 1) {
                    final Shuugakusei s2 = (Shuugakusei) kojin._shugakuseiList.get(p * 2 + 1);
                    print(svf, "2", s2);
                    if (!print1 && null != s2._shuugakuNo && null != s2._choteiKaisu) {
                        svf.VrsOut("REM", StringUtils.defaultString(s2._shuugakuNo) + " " + StringUtils.defaultString(s2._choteiKaisu) + "      " + String.valueOf(pageall));
                    }
                    // log.debug(" kojinNo = " + kojin._kojinNo + ", shuugakuNo2 = " + s2._shuugakuNo);
                }
                svf.VrsOut("PRINT_DAY", _param._shugakuDate.formatDate(_param._tyuushakuDate, false));
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private void print(final Vrw32alp svf, final String i, final Shuugakusei shuugakusei) {
        svf.VrsOut("P_NO" + i, shuugakusei._shuugakuNo);
        svf.VrsOut("N_NO" + i, StringUtils.defaultString(shuugakusei._choteiKaisu) + StringUtils.defaultString(shuugakusei._shuugakuNo));
        svf.VrsOut("DEMAND" + i + "_1", "高等学校等修学資金返還金");
        final String mongon = ("1".equals(shuugakusei._shikinShubetsu)) ? "修学金" : "2".equals(shuugakusei._shikinShubetsu) ? "修学支度金" : ""; 
        svf.VrsOut("DEMAND" + i + "_2", "(" + mongon + ")(" + _param._shugakuDate.formatNentuki(shuugakusei._choteiYm) + "分)");
        svf.VrsOut("MONEY" + i, shuugakusei._minouGk);
    }
    
    private static class Kojin {
        final String _kojinNo;
        final String _zipCd;
        final String _name;
        final String _addr1;
        final String _addr2;
        final List _shugakuseiList = new ArrayList();
        Kojin(final String kojinNo, final String zipCd, final String name, final String addr1, final String addr2) {
            _kojinNo = kojinNo;
            _zipCd = zipCd;
            _name = name;
            _addr1 = addr1;
            _addr2 = addr2;
        }
    }

    private static class Shuugakusei {
        final String _shuugakuNo;
        final String _shikinShubetsu;
        final String _choteiKaisu;
        final String _choteiYm;
        final String _noufuKigenOrg;
        final String _minouGk;
        Shuugakusei(final String shuugakuNo, final String shikinShubetsu, final String choteiKaisu, final String choteiYm, final String noufuKigenOrg, final String minouGk) {
            _shuugakuNo = shuugakuNo;
            _choteiKaisu = choteiKaisu;
            _shikinShubetsu = shikinShubetsu;
            _choteiYm = choteiYm;
            _noufuKigenOrg = noufuKigenOrg;
            _minouGk = minouGk;
        }
    }

    public String sql(final String tokusokuDate) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     CASE WHEN S1.ZIP_CD IS NOT NULL THEN S1.ZIP_CD ");
        stb.append("          WHEN S2.ZIP_CD IS NOT NULL THEN S2.ZIP_CD ");
        stb.append("          ELSE V2.ZIPCD ");
        stb.append("     END AS ZIPCD, ");
        stb.append("     CASE WHEN S1.ADDR1 IS NOT NULL THEN S1.ADDR1 ");
        stb.append("          WHEN S2.ADDR1 IS NOT NULL THEN S2.ADDR1 ");
        stb.append("          ELSE V2.ADDR1 ");
        stb.append("     END AS ADDR1, ");
        stb.append("     CASE WHEN S1.ADDR2 IS NOT NULL THEN S1.ADDR2 ");
        stb.append("          WHEN S2.ADDR2 IS NOT NULL THEN S2.ADDR2 ");
        stb.append("          ELSE V2.ADDR2 ");
        stb.append("     END AS ADDR2, ");
        stb.append("     CASE WHEN S1.SOUFU_NAME IS NOT NULL THEN S1.SOUFU_NAME ");
        stb.append("              WHEN S2.SOUFU_NAME IS NOT NULL THEN S2.SOUFU_NAME ");
        stb.append("              ELSE CONCAT(CONCAT(V2.FAMILY_NAME,'　'),V2.FIRST_NAME) ");
        stb.append("     END AS NAME, ");
        stb.append("     T1.SHIKIN_SHUBETSU, ");
        stb.append("     V1.SHUUGAKU_NO, ");
        stb.append("     V1.CHOTEI_KAISU, ");
        stb.append("     V1.CHOTEI_YM, ");
        stb.append("     V1.NOUFU_KIGEN_ORG, ");
        stb.append("     V1.MINOU_GK, ");
        stb.append("     V1.TOKUSOKU_DATE, ");
        stb.append("     T1.KOJIN_NO ");
        stb.append(" FROM ");
        stb.append("     V_CHOTEI_NOUFU_ADD V1 ");
        stb.append("     LEFT JOIN SAIKEN_DAT T1 ON V1.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT V2 ON T1.KOJIN_NO = V2.KOJIN_NO ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S1 ON T1.KOJIN_NO = S1.KOJIN_NO AND T1.SHUUGAKU_NO = S1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S2 ON T1.KOJIN_NO = S2.KOJIN_NO AND S2.SHUUGAKU_NO = '9999999' ");
        
        stb.append("     left join (SELECT SHUGAKU_NO AS SHUUGAKU_NO, CHOTEI_KAISU, COUNT(*) AS SOKU_CNT FROM SHUNO_CONVENI_DAT WHERE D_DATA_CD = '01' GROUP BY SHUGAKU_NO, CHOTEI_KAISU) CVN on V1.SHUUGAKU_NO = CVN.SHUUGAKU_NO and V1.CHOTEI_KAISU = CVN.CHOTEI_KAISU ");

        stb.append(" WHERE ");
        stb.append("     (CHUUI_FLG IS NULL OR CHUUI_FLG = '02') AND ");
        stb.append("     VALUE(CVN.SOKU_CNT, 0) < 1 AND ");
        stb.append("     V1.TOKUSOKU_HORYU_FLG = '0' AND ");
        stb.append("     V1.NOUFU_KIGEN IS NOT NULL AND ");
        stb.append("     V1.MINOU_GK > 0 AND ");
        if (!StringUtils.isEmpty(_param._shuugakuNo)) {
            stb.append("     V1.SHUUGAKU_NO = '" + _param._shuugakuNo + "' AND ");
        } else {
            if (null == tokusokuDate) {
                stb.append("     V1.TOKUSOKU_DATE IS NULL AND ");
            } else {
                stb.append("     V1.TOKUSOKU_DATE = '" + tokusokuDate + "' AND ");
            }
        }
        stb.append("     V1.CHOTEI_YM = '" + _param._choteiYm + "' ");
        if (!StringUtils.isEmpty(_param._shuugakuNo) || "3".equals(_param._targetDiv)) { 
        } else if ("1".equals(_param._targetDiv)) {
            stb.append("     AND V1.SHIHARAI_HOHO_CD = '1' ");
        } else if ("2".equals(_param._targetDiv)) {
            stb.append("     AND V1.SHIHARAI_HOHO_CD = '2' ");
        }
        stb.append(" ORDER BY ");
        stb.append("    V2.KOJIN_NO, T1.SHUUGAKU_NO ");
        return stb.toString();
    }
    
    /**
     * 写真データファイルの取得
     */
    private String getImageFile(final String filename, final String imageDir) {
        if (null == _param._documentRoot) {
            return null;
        } // DOCUMENTROOT
        if (null == imageDir) {
            return null;
        }
        final StringBuffer stb = new StringBuffer();
        stb.append(_param._documentRoot);
        stb.append("/");
        stb.append(imageDir);
        stb.append("/");
        stb.append(filename);
        final File file1 = new File(stb.toString());
        if (!file1.exists()) {
            return null;
        } // 写真データ存在チェック用
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
    	log.fatal("$Revision: 73485 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _shuugakuNo;
        final String _choteiYm;
        final String _outputDate;
        final String _noufuKigen;
        final String _tyuushakuDate;
        final String _documentRoot;
        final String _targetDiv;
        private final ShugakuDate _shugakuDate;

        private Map _psMap = new HashMap();
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shuugakuNo = request.getParameter("SHUUGAKU_NO");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _choteiYm = _shugakuDate.d5toYmStr(request.getParameter("CHOTEI_YM"));
            _outputDate = _shugakuDate.d7toDateStr(request.getParameter("OUTPUT_DATE"));
            _noufuKigen = _shugakuDate.d7toDateStr(request.getParameter("NOUFU_KIGEN"));
            _tyuushakuDate = _shugakuDate.d7toDateStr(request.getParameter("TYUUSHAKU_DATE"));
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _targetDiv = request.getParameter("TARGET_DIV");
        }
    }
}

// eof

