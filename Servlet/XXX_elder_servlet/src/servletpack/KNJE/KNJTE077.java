/*
 * $Id: cdf31b1737c87831d3d5aaa5c9427d43d6498504 $
 *
 * 作成日: 2012/09/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 返還開始のお知らせ
 */
public class KNJTE077 {

    private static final Log log = LogFactory.getLog(KNJTE077.class);

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
        
        final List printList = getPrintListPage1(db2);
        for (final Iterator it = printList.iterator(); it.hasNext();) {
            final Kojin kojin = (Kojin) it.next();
            
            printMain1(svf, kojin);

            printMain2(svf, kojin);
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

    private void printMain1(final Vrw32alp svf, final Kojin kojin) {

        svf.VrSetForm("KNJTE077_1.frm", 1);
        svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._printDate)); // 印刷日付

        final SaikenJokyo sj0 = (SaikenJokyo) kojin._saikenJokyoList.get(0);
//        if ("1".equals(sj0._shiharaininKbn)) {
        svf.VrsOut("ZIP_NO", sj0._zipcd); // 郵便番号
        printAddress(svf, sj0._addr1, sj0._addr2);
//        svf.VrsOut("ADDRESS1", sj0._addr1); // 住所
//        VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(sj0._addr2, 40, 2), svf);
        svf.VrsOut("NAME", StringUtils.defaultString(sj0._kojinName)); // 氏名
//        } else if ("2".equals(sj0._shiharaininKbn)) {
//            svf.VrsOut("ZIP_NO", sj0._rentaiZipcd); // 郵便番号
//            svf.VrsOut("ADDRESS1", sj0._rentaiAddr1); // 住所
//            VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(sj0._rentaiAddr2, 40, 2), svf);
//            svf.VrsOut("NAME", sj0._rentaiName); // 氏名
//        }

        final StringBuffer stbNo = new StringBuffer();
        for (int i = 0; i < kojin._saikenJokyoList.size(); i++) {
            final SaikenJokyo sj = (SaikenJokyo) kojin._saikenJokyoList.get(i);
            stbNo.append("　(" + sj._shuugakuNo + ")");
        }        
        svf.VrsOut("NO", stbNo.toString());
        svf.VrsOut("NAME2", StringUtils.defaultString(sj0._rentaiName)); // 連帯保証人氏名
        // svf.VrsOut("GOVERNER", _param._chijiName); // 知事名
        final String nentuki;
        if ("1".equals(_param._output)) {
            nentuki = _param._firstChoteiYm;
        } else {
            nentuki = StringUtils.isBlank(_param._firstChoteiYm2) ? sj0._startChoteiYm : _param._firstChoteiYm2;
        }
        svf.VrsOut("DAY1", _param._shugakuDate.formatNentuki(nentuki)); // 年月

        for (int i = 0; i < kojin._saikenJokyoList.size(); i++) {
            final SaikenJokyo sj = (SaikenJokyo) kojin._saikenJokyoList.get(i);
            final int l = i + 1;
            svf.VrsOutn("P_NO", l, sj._shuugakuNo); // 修学生番号
            svf.VrsOutn("LOAN_MONEY", l, sj._henkanTotalGk); // 貸与金額
            svf.VrsOut("LOAN_KIND" + (l == 1 ? "" : String.valueOf(l)), sj._shikinShubetsuName); // 修学金種別
            svf.VrsOutn("COMP_MONEY", l, sj._gakuShuno); // 返済済金額
            svf.VrsOutn("BALANCE", l, sj._minouGk); // 残高
            svf.VrsOutn("RET_FROM", l, _param._shugakuDate.formatNentuki(sj._startChoteiYm)); // 返還期間開始
            svf.VrsOutn("RET_TO", l, _param._shugakuDate.formatNentuki(sj._endChoteiYm)); // 返還期間終了
            svf.VrsOutn("RET_PLAN", l, sj._henkanHohoName); // 返還方法
            svf.VrsOutn("PAY_PLAN", l, sj._shiharaiHohoName); // 支払方法
            if (!"2".equals(sj._shiharaiHoho)) { // 支払方法が"納入通知書"の場合、表示しない
                svf.VrsOutn("BANK_NAME1", l, sj._bankname); // 銀行名
                svf.VrsOutn("BRANCH_NAME1", l, sj._branchname); // 支店名
                svf.VrsOutn("AC_KIND", l, sj._yokinDivName); // 口座種別
                svf.VrsOutn("AC_NO", l, sj._accountNo); // 口座番号
                svf.VrsOutn("AC_NAME1", l, sj._kouzaMeigi); // 口座名義人
            }
            if ("1".equals(_param._output) && _param._firstChoteiYm.equals(sj._startChoteiYm) ||
                "2".equals(_param._output) && !StringUtils.isBlank(_param._firstChoteiYm2) && _param._firstChoteiYm2.equals(sj._startChoteiYm)) {
                svf.VrsOutn("TEXT1", l, "今回開始分"); //今回開始分 
            }
        }

        svf.VrEndPage();
        _hasData = true;
    }

    private void printMain2(final Vrw32alp svf, final Kojin kojin) {

        final List pageList = getPageList(kojin._shuugakuseiList, 4);
        
        for (final Iterator it = pageList.iterator(); it.hasNext();) {
            final List shuugakuseiList = (List) it.next();
            final TreeSet choteiYmSet = new TreeSet();
            for (final Iterator it2 = shuugakuseiList.iterator(); it2.hasNext();) {
                final Shuugakusei shuugakusei = (Shuugakusei) it2.next(); 
                for (Iterator it3 = shuugakusei._choteiDatList.iterator(); it3.hasNext();) {
                    final ChoteiDat choteiDat = (ChoteiDat) it3.next();
                    if ("1".equals(_param._output)) {
                        if (_param._firstChoteiYm.compareTo(choteiDat._choteiYm) > 0) {
                            continue;
                        }
                    } else if ("2".equals(_param._output)) {
                        if (!StringUtils.isBlank(_param._firstChoteiYm2) && _param._firstChoteiYm2.compareTo(choteiDat._choteiYm) > 0) {
                            continue;
                        }
                    }
                    choteiYmSet.add(choteiDat._choteiYm);
                }
            }
            svf.VrSetForm("KNJTE077_2.frm", 1);
            
            svf.VrsOut("INQ_NAME", "京都府教育庁指導部高校教育課"); // 問い合わせ先課
            svf.VrsOut("INQ_ZIP", "612-0064"); // 問い合わせ先郵便番号
            svf.VrsOut("INQ_ADDRESS", "京都市伏見区桃山毛利長門西町"); // 問い合わせ先住所
            svf.VrsOut("INQ_ADDRESS2", "京都府総合教育センター内"); // 問い合わせ先住所
            svf.VrsOut("INQ_PHONE1", "０７５‐５７４‐７５１８"); // 問い合わせ先電話番号
            svf.VrsOut("INQ_PHONE2", "０７５‐５７４‐７５３２"); // 問い合わせ先電話番号

            int line = 0;
            for (final Iterator its = choteiYmSet.iterator(); its.hasNext();) {
                final String choteiYm = (String) its.next();
                
                int choteiCountMax = 0;
                for (final Iterator it2 = shuugakuseiList.iterator(); it2.hasNext();) {
                    final Shuugakusei shuugakusei = (Shuugakusei) it2.next();
                    final int count = shuugakusei.getChoteiDatList(choteiYm).size();
                    choteiCountMax = Math.max(choteiCountMax, count);
                }
                
                for (int i = 0; i < choteiCountMax; i++) {
                    for (int j = 0; j < shuugakuseiList.size(); j++) {
                        final Shuugakusei shuugakusei = (Shuugakusei) shuugakuseiList.get(j);
                        final String col = String.valueOf(j + 1);
                        svf.VrsOutn("P_NO", j + 1, shuugakusei._shuugakuNo); // 修学生番号
                        final List choteiDatList = shuugakusei.getChoteiDatList(choteiYm);
                        if (choteiDatList.size() <= i) {
                            continue;
                        }
                        final ChoteiDat choteiDat = (ChoteiDat) choteiDatList.get(i);
                        final int pl = line + i + 1;
                        svf.VrsOutn("PAY_MONTH", pl, _param._shugakuDate.formatNentuki(choteiYm)); // 支払月
                        svf.VrsOutn("PAY_MONEY" + col, pl, choteiDat._henkanGk); // 支払金額
                        svf.VrsOutn("BALANCE" + col, pl, shuugakusei.getZandaka(choteiDat)); // 残高
                    }
                }
                line += choteiCountMax;
            }
            
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private List getPageList(final List list, final int size) {
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= size) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }

    public String getPage2Sql(final Collection kojinNoList) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     V1.KOJIN_NO, ");
        stb.append("     V1.SHUUGAKU_NO, ");
        stb.append("     V1.HENKAN_TOTAL_GK, ");
        stb.append("     T1.CHOTEI_YM, ");
        stb.append("     T1.HENKAN_GK ");
        stb.append(" FROM ");
        stb.append("     V_SAIKEN_JOKYO V1 ");
        stb.append("     LEFT JOIN CHOTEI_DAT T1 ON V1.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
        stb.append(" WHERE ");
        stb.append("     KOJIN_NO IN " + SQLUtils.whereIn(true, toArray(kojinNoList)) + " ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SHUUGAKU_NO, ");
        stb.append("     T1.CHOTEI_YM ");
        return stb.toString();
    }

    private Kojin getKojin(final String kojinNo, final List list) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Kojin kojin = (Kojin) it.next();
            if (kojin._kojinNo.equals(kojinNo)) {
                return kojin;
            }
        }
        return null;
    }
    
    private Shuugakusei getShuugakusei(final String shuugakuNo, final List list) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Shuugakusei shuugakusei = (Shuugakusei) it.next();
            if (shuugakusei._shuugakuNo.equals(shuugakuNo)) {
                return shuugakusei;
            }
        }
        return null;
    }

    private List getPrintListPage1(final DB2UDB db2) {
        final List printList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPage1Sql();
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String kojinNo = rs.getString("KOJIN_NO");
                final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                final String abbv3 = rs.getString("ABBV3");
                final String shikinShubetsuName = rs.getString("SHIKIN_SHUBETSU_NAME");
                final String shikinShubetsuName2 = rs.getString("SHIKIN_SHUBETSU_NAME2");
                final String henkanTotalGk = rs.getString("HENKAN_TOTAL_GK");
                final String gakuShuno = rs.getString("GAKU_SHUNO");
                final String minouGk = rs.getString("MINOU_GK");
                final String startChoteiYm = rs.getString("START_CHOTEI_YM");
                final String endChoteiYm = rs.getString("END_CHOTEI_YM");
                final String shiharaininKbn = rs.getString("SHIHARAININ_KBN");
                final String henkanHoho = rs.getString("HENKAN_HOHO");
                final String henkanHohoName = rs.getString("HENKAN_HOHO_NAME");
                final String shiharaiHoho = rs.getString("SHIHARAI_HOHO");
                final String shiharaiHohoName = rs.getString("SHIHARAI_HOHO_NAME");
                final String kojinName = rs.getString("KOJIN_NAME");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String rentaiName = rs.getString("RENTAI_NAME");
                final String rentaiZipcd = null; // rs.getString("RENTAI_ZIPCD");
                final String rentaiAddr1 = null; // rs.getString("RENTAI_ADDR1");
                final String rentaiAddr2 = null; // rs.getString("RENTAI_ADDR2");
                final String bankname = rs.getString("BANKNAME");
                final String branchname = rs.getString("BRANCHNAME");
                final String yokinDiv = rs.getString("YOKIN_DIV");
                final String yokinDivName = rs.getString("YOKIN_DIV_NAME");
                final String accountNo = rs.getString("ACCOUNT_NO");
                final String kouzaMeigi = rs.getString("KOUZA_MEIGI");
     
                final SaikenJokyo saikenJokyo = new SaikenJokyo(kojinNo, shuugakuNo, abbv3, shikinShubetsuName, shikinShubetsuName2, henkanTotalGk, gakuShuno, minouGk, startChoteiYm, endChoteiYm, shiharaininKbn, henkanHoho, henkanHohoName, shiharaiHoho, shiharaiHohoName, kojinName, zipcd, addr1, addr2, rentaiName, rentaiZipcd, rentaiAddr1, rentaiAddr2, bankname, branchname, yokinDiv, yokinDivName, accountNo, kouzaMeigi);
                Kojin kojin = getKojin(kojinNo, printList);
                if (null == kojin) {
                    kojin = new Kojin(kojinNo);
                    printList.add(kojin);
                }
                kojin._saikenJokyoList.add(saikenJokyo);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        
        try {
            final List kojinNoListList = getKojinNoListList(printList, 50);

            for (final Iterator it = kojinNoListList.iterator(); it.hasNext();) {
                final Collection kojinNoList = (Collection) it.next();

                final String sql = getPage2Sql(kojinNoList);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final Kojin kojin = getKojin(rs.getString("KOJIN_NO"), printList);
                    if (null == kojin) {
                        continue;
                    }
                    
                    Shuugakusei shuugakusei = getShuugakusei(rs.getString("SHUUGAKU_NO"), kojin._shuugakuseiList);
                    if (null == shuugakusei) {
                        shuugakusei = new Shuugakusei(rs.getString("SHUUGAKU_NO"));
                        kojin._shuugakuseiList.add(shuugakusei);
                    }
                    final ChoteiDat choteiDat = new ChoteiDat(rs.getString("HENKAN_TOTAL_GK"), rs.getString("CHOTEI_YM"), rs.getString("HENKAN_GK"));
                    shuugakusei._choteiDatList.add(choteiDat);
                }
            }

        } catch (SQLException e) {
            log.fatal("exception!", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return printList;
    }
    
    private List getKojinNoListList(final List printList, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = printList.iterator(); it.hasNext();) {
            final Kojin kojin = (Kojin) it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(kojin._kojinNo);
        }
        return rtn;
    }
    
    private String getPage1Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TBL_START_CHOTEI_YM AS ( ");
        stb.append("     SELECT MIN(CHOTEI_YM) AS START_CHOTEI_YM, MAX(CHOTEI_YM) AS END_CHOTEI_YM, SHUUGAKU_NO FROM CHOTEI_DAT WHERE TORIKESI_FLG = '0' AND FUNO_FLG = '0' GROUP BY SHUUGAKU_NO ");
        stb.append(" ) ");
        if ("1".equals(_param._output)) {
            stb.append(" , TBL_START_CHOTEI_YM2 AS ( ");
            stb.append("     SELECT ");
            stb.append("         KOJIN_NO ");
            stb.append("     FROM V_SAIKEN_JOKYO V1 ");
            stb.append("     INNER JOIN TBL_START_CHOTEI_YM W1 ON V1.SHUUGAKU_NO = W1.SHUUGAKU_NO ");
            stb.append("     WHERE ");
            stb.append("        W1.START_CHOTEI_YM = '" + _param._firstChoteiYm + "' ");
            stb.append(" ) ");
        }
        stb.append(" SELECT ");
        stb.append("     V1.KOJIN_NO, ");
        stb.append("     V1.SHUUGAKU_NO, ");
        stb.append("     T2.ABBV3, ");
        stb.append("     T4.ABBV1 AS SHIKIN_SHUBETSU_NAME, ");
        stb.append("     T4.ABBV2 AS SHIKIN_SHUBETSU_NAME2, ");
        stb.append("     HENKAN_TOTAL_GK, ");
        stb.append("     GAKU_SHUNO, ");
        stb.append("     HENKAN_TOTAL_GK - GAKU_SHUNO - MENJO_TOTAL_GK AS MINOU_GK, ");
        stb.append("     W1.START_CHOTEI_YM, ");
        stb.append("     W1.END_CHOTEI_YM, ");
        stb.append("     V1.SHIHARAININ_KBN, ");
        stb.append("     V1.HENKAN_HOHO, ");
        stb.append("     T5.NAME1 AS HENKAN_HOHO_NAME, ");
        stb.append("     V1.SHIHARAI_HOHO, ");
        stb.append("     T6.NAME1 AS SHIHARAI_HOHO_NAME, ");
        stb.append("     CASE WHEN S1.SOUFU_NAME IS NOT NULL THEN S1.SOUFU_NAME ");
        stb.append("              WHEN S2.SOUFU_NAME IS NOT NULL THEN S2.SOUFU_NAME ");
        stb.append("              ELSE CONCAT(CONCAT(V3.FAMILY_NAME,'　'),V3.FIRST_NAME) ");
        stb.append("     END AS KOJIN_NAME, ");
        stb.append("     CASE WHEN S1.ZIP_CD IS NOT NULL THEN S1.ZIP_CD ");
        stb.append("          WHEN S2.ZIP_CD IS NOT NULL THEN S2.ZIP_CD ");
        stb.append("          ELSE V3.ZIPCD ");
        stb.append("     END AS ZIPCD, ");
        stb.append("     CASE WHEN S1.ADDR1 IS NOT NULL THEN S1.ADDR1 ");
        stb.append("          WHEN S2.ADDR1 IS NOT NULL THEN S2.ADDR1 ");
        stb.append("          ELSE V3.ADDR1 ");
        stb.append("     END AS ADDR1, ");
        stb.append("     CASE WHEN S1.ADDR2 IS NOT NULL THEN S1.ADDR2 ");
        stb.append("          WHEN S2.ADDR2 IS NOT NULL THEN S2.ADDR2 ");
        stb.append("          ELSE V3.ADDR2 ");
        stb.append("     END AS ADDR2, ");
        stb.append("     CONCAT(CONCAT(V4.FAMILY_NAME,'　'),V4.FIRST_NAME) AS RENTAI_NAME, ");
//        stb.append("     V4.ZIPCD AS RENTAI_ZIPCD, ");
//        stb.append("     V4.ADDR1 AS RENTAI_ADDR1, ");
//        stb.append("     V4.ADDR2 AS RENTAI_ADDR2, ");
        stb.append("     V5.BANKNAME, ");
        stb.append("     V5.BRANCHNAME, ");
        stb.append("     V5.YOKIN_DIV, ");
        stb.append("     T3.ABBV1 AS YOKIN_DIV_NAME, ");
        stb.append("     V5.ACCOUNT_NO, ");
        stb.append("     CONCAT(CONCAT(BANK_MEIGI_SEI_KANA,' '),BANK_MEIGI_MEI_KANA) AS KOUZA_MEIGI ");
        stb.append(" FROM ");
        stb.append("     V_SAIKEN_JOKYO V1 ");
        stb.append("     LEFT JOIN TBL_START_CHOTEI_YM AS W1 ON V1.SHUUGAKU_NO = W1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN V_KOJIN_SHUUGAKU_SHINSEI_HIST_DAT V2 ON V1.SHUUGAKU_NO = V2.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT V3 ON V2.KOJIN_NO = V3.KOJIN_NO ");
        stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT V4 ON V2.RENTAI_CD = V4.SHINKEN_CD ");
        stb.append("     LEFT JOIN V_FURIKAE_KOUZA_NEWEST V5 ON V1.SHUUGAKU_NO = V5.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN NAME_MST T2 ON V2.SHIKIN_SHOUSAI_DIV = T2.NAMECD2 AND T2.NAMECD1 = 'T030' ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S1 ON V1.KOJIN_NO = S1.KOJIN_NO AND V1.SHUUGAKU_NO = S1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S2 ON V1.KOJIN_NO = S2.KOJIN_NO AND S2.SHUUGAKU_NO = '9999999' ");
        stb.append("     LEFT JOIN NAME_MST T3 ON V5.YOKIN_DIV = T3.NAMECD2 AND T3.NAMECD1 = 'T032' ");
        stb.append("     LEFT JOIN NAME_MST T4 ON T2.NAMESPARE3 = T4.NAMECD2 AND T4.NAMECD1 = 'T008' ");
        stb.append("     LEFT JOIN NAME_MST T5 ON V1.HENKAN_HOHO = T5.NAMECD2 AND T5.NAMECD1 = 'T016' ");
        stb.append("     LEFT JOIN NAME_MST T6 ON V1.SHIHARAI_HOHO = T6.NAMECD2 AND T6.NAMECD1 = 'T039' ");
        stb.append(" WHERE ");
        if ("1".equals(_param._output)) {
            stb.append("     V1.KOJIN_NO IN (SELECT KOJIN_NO FROM TBL_START_CHOTEI_YM2) ");
        } else if ("2".equals(_param._output)) {
            stb.append("     V1.KOJIN_NO = '" + _param._kojinNo + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     V1.KOJIN_NO, ");
        stb.append("     V1.SHUUGAKU_NO ");
        return stb.toString();
    }
    
    private static class Kojin {
        final String _kojinNo;
        final List _saikenJokyoList = new ArrayList();
        final List _shuugakuseiList = new ArrayList();
        Kojin(final String kojinNo) {
            _kojinNo = kojinNo;
        }
    }
    
    private static class Shuugakusei {
        final String _shuugakuNo;
        final List _choteiDatList = new ArrayList();
        Shuugakusei(final String shuugakuNo) {
            _shuugakuNo = shuugakuNo;
        }
        public String getZandaka(final ChoteiDat choteiDatTarget) {
            BigDecimal bdHenkanTotalGk = null;
            BigDecimal bdHenkanGk = new BigDecimal(0);
            for (final Iterator it = _choteiDatList.iterator(); it.hasNext();) {
                final ChoteiDat choteiDat = (ChoteiDat) it.next();
                if (null == bdHenkanTotalGk && null != choteiDat._henkanTotalGk) {
                    bdHenkanTotalGk = new BigDecimal(choteiDat._henkanTotalGk);
                }
                if (null != choteiDat._henkanGk) {
                    bdHenkanGk = bdHenkanGk.add(new BigDecimal(choteiDat._henkanGk));
                }
                if (choteiDat == choteiDatTarget) {
                    break;
                }
            }
            if (null != bdHenkanTotalGk && null != bdHenkanGk) {
                return bdHenkanTotalGk.subtract(bdHenkanGk).toString();
            } else if (null != bdHenkanTotalGk) {
                return bdHenkanTotalGk.toString();
            }
            return null;
        }
        List getChoteiDatList(final String choteiYm) {
            List list = new ArrayList();
            for (int i = 0; i < _choteiDatList.size(); i++) {
                final ChoteiDat choteiDat = (ChoteiDat) _choteiDatList.get(i);
                if (choteiDat._choteiYm.equals(choteiYm)) {
                    list.add(choteiDat);
                }
            }
            return list;
        }
    }
    
    private static class ChoteiDat {
        final String _henkanTotalGk;
        final String _choteiYm;
        final String _henkanGk;
        ChoteiDat(final String henkanTotalGk, final String choteiYm, final String henkanGk) {
            _henkanTotalGk = henkanTotalGk;
            _choteiYm = choteiYm;
            _henkanGk = henkanGk;
        }
    }
    
    private static class SaikenJokyo {
        final String _kojinNo;
        final String _shuugakuNo;
        final String _abbv3;
        final String _shikinShubetsuName;
        final String _shikinShubetsuName2;
        final String _henkanTotalGk;
        final String _gakuShuno;
        final String _minouGk;
        final String _startChoteiYm;
        final String _endChoteiYm;
        final String _shiharaininKbn;
        final String _henkanHoho;
        final String _henkanHohoName;
        final String _shiharaiHoho;
        final String _shiharaiHohoName;
        final String _kojinName;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _rentaiName;
        final String _rentaiZipcd;
        final String _rentaiAddr1;
        final String _rentaiAddr2;
        final String _bankname;
        final String _branchname;
        final String _yokinDiv;
        final String _yokinDivName;
        final String _accountNo;
        final String _kouzaMeigi;

        SaikenJokyo(
                final String kojinNo,
                final String shuugakuNo,
                final String abbv3,
                final String shikinShubetsuName,
                final String shikinShubetsuName2,
                final String henkanTotalGk,
                final String gakuShuno,
                final String minouGk,
                final String startChoteiYm,
                final String endChoteiYm,
                final String shiharaininKbn,
                final String henkanHoho,
                final String henkanHohoName,
                final String shiharaiHoho,
                final String shiharaiHohoName,
                final String kojinName,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String rentaiName,
                final String rentaiZipcd,
                final String rentaiAddr1,
                final String rentaiAddr2,
                final String bankname,
                final String branchname,
                final String yokinDiv,
                final String yokinDivName,
                final String accountNo,
                final String kouzaMeigi
        ) {
            _kojinNo = kojinNo;
            _shuugakuNo = shuugakuNo;
            _abbv3 = abbv3;
            _shikinShubetsuName = shikinShubetsuName;
            _shikinShubetsuName2 = shikinShubetsuName2;
            _henkanTotalGk = henkanTotalGk;
            _gakuShuno = gakuShuno;
            _minouGk = minouGk;
            _startChoteiYm = startChoteiYm;
            _endChoteiYm = endChoteiYm;
            _shiharaininKbn = shiharaininKbn;
            _henkanHoho = henkanHoho;
            _henkanHohoName = henkanHohoName;
            _shiharaiHoho = shiharaiHoho;
            _shiharaiHohoName = shiharaiHohoName;
            _kojinName = kojinName;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _rentaiName = rentaiName;
            _rentaiZipcd = rentaiZipcd;
            _rentaiAddr1 = rentaiAddr1;
            _rentaiAddr2 = rentaiAddr2;
            _bankname = bankname;
            _branchname = branchname;
            _yokinDiv = yokinDiv;
            _yokinDivName = yokinDivName;
            _accountNo = accountNo;
            _kouzaMeigi = kouzaMeigi;
        }
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
    
    
    private static void VrsOut(final String[] field1, final String[] data, final Vrw32alp svf) {
        if (null == data) {
            return;
        }
        for (int i = 0; i < Math.min(field1.length, data.length); i++) {
            svf.VrsOut(field1[i], data[i]);
        }
    }

    private static String[] toArray(final Collection col) {
        final String[] array = new String[col.size()];
        int i = 0;
        for (final Iterator it = col.iterator(); it.hasNext(); i++) {
            array[i] = (String) it.next();
        }
        return array;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73790 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginDate;
        private final String _output;
        private final String _firstChoteiYm;
        private final String _kojinNo;
        private final String _firstChoteiYm2;
        private final String _printDate;
        // private final String _chijiName;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate = request.getParameter("LOGIN_DATE");
            _output = request.getParameter("OUTPUT");
            _kojinNo = request.getParameter("KOJIN_NO");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _firstChoteiYm = _shugakuDate.d5toYmStr(request.getParameter("FIRST_CHOTEI_YM"));
            _firstChoteiYm2 = _shugakuDate.d5toYmStr(request.getParameter("FIRST_CHOTEI_YM2"));
            _printDate = _shugakuDate.d7toDateStr(request.getParameter("PRINT_DATE"));
            // _chijiName = getChijiName(db2);
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

