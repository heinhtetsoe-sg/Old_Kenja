/*
 * $Id: b1576ba6b8d4e6e556121744f3c823890a5c8873 $
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 返還整理帳・返還計画表
 */
public class KNJTE047 {

    private static final Log log = LogFactory.getLog(KNJTE047.class);

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
            final SaikenJokyo saikenJokyo = (SaikenJokyo) it.next();
            
            printMain1(svf, saikenJokyo);

            printMain2(svf, db2, saikenJokyo._shuugakuNo);
        }
    }
    
    private final String diffYearMonth(final String ym1, final String ym2) {
        if (null == ym1 || ym1.length() < 7 || !NumberUtils.isDigits(ym1.substring(0, 4)) || !NumberUtils.isDigits(ym1.substring(5, 7)) ||
                null == ym2 || ym2.length() < 7 || !NumberUtils.isDigits(ym2.substring(0, 4)) || !NumberUtils.isDigits(ym2.substring(5, 7))) {
            return null;
        }
        final int year1 = Integer.parseInt(ym1.substring(0, 4));
        final int month1 = Integer.parseInt(ym1.substring(5, 7));
        int year2 = Integer.parseInt(ym2.substring(0, 4));
        int month2 = Integer.parseInt(ym2.substring(5, 7)) + 1;
        if (month1 > month2) {
            year2 -= 1;
            month2 += 12;
        }
        final String nen = year2 - year1 == 0 ? "  " : _param._shugakuDate.keta(year2 - year1, 2);
        final String tuki = month2 - month1 == 0 ? "  " : _param._shugakuDate.keta(month2 - month1, 2);
        return nen + "年　" + tuki + "箇月";
    }
    
    private void svfPrintAddress(final String[] field, final String address1, final String address2, final Vrw32alp svf) {
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
    }

    private void printMain1(final Vrw32alp svf, final SaikenJokyo saikenJokyo) {

        svf.VrSetForm("KNJTE047.frm", 1);

        if ("1".equals(_param._addrDiv) || "3".equals(_param._addrDiv) && "1".equals(saikenJokyo._shiharaininKbn) || "4".equals(_param._addrDiv)) {
            svf.VrsOut("ZIP_NO", saikenJokyo._zipcd); // 郵便番号
            svfPrintAddress(new String[]{"ADDRESSS1_2", "ADDRESSS1_3", "ADDRESSS2_2", "ADDRESSS2_3"}, saikenJokyo._addr1, saikenJokyo._addr2, svf);
            svf.VrsOut("NAME", saikenJokyo._kojinName); // 氏名
        } else if ("2".equals(_param._addrDiv) || "3".equals(_param._addrDiv) && "2".equals(saikenJokyo._shiharaininKbn)) {
            svf.VrsOut("ZIP_NO", saikenJokyo._rentaiZipcd); // 郵便番号
            svfPrintAddress(new String[]{"ADDRESSS1_2", "ADDRESSS1_3", "ADDRESSS2_2", "ADDRESSS2_3"}, saikenJokyo._rentaiAddr1, saikenJokyo._rentaiAddr2, svf);
            svf.VrsOut("NAME", saikenJokyo._rentaiName); // 氏名
        }
        svf.VrsOut("APPLI_NO", saikenJokyo._shuugakuNo); // 修学生番号

        svf.VrsOut("NAME2", saikenJokyo._kojinName);
        if ("4".equals(_param._addrDiv)) {
            svf.VrsOut("NAME3", saikenJokyo._shinken1Name);
        }

        svf.VrsOut("LOAN_DECIDE_DAY", _param._shugakuDate.formatDate(saikenJokyo._firstKetteiDate)); // 貸与決定日
        svf.VrsOut("LOAN_MONEY", saikenJokyo._henkanTotalGk); // 貸与金額
        svf.VrsOut("EXEMP_MONEY", saikenJokyo._menjoTotalGk); // 免除金額
        svf.VrsOut("LOAN_FROM", _param._shugakuDate.formatNentuki(saikenJokyo._sYm)); // 当初貸与期間開始
        svf.VrsOut("LOAN_TO", _param._shugakuDate.formatNentuki(saikenJokyo._eYm)); // 当初貸与期間終了
        svf.VrsOut("RET_PERIOD", diffYearMonth(saikenJokyo._startChoteiYm, saikenJokyo._endChoteiYm)); // 返還期間年月
        svf.VrsOut("RET_FROM", _param._shugakuDate.formatNentuki(saikenJokyo._startChoteiYm)); // 返還期間開始
        svf.VrsOut("RET_TO", _param._shugakuDate.formatNentuki(saikenJokyo._endChoteiYm)); // 返還期間終了
        if (!"2".equals(saikenJokyo._shiharaiHoho)) {
            svf.VrsOut("BANK_NAME", saikenJokyo._bankname); // 銀行名
            svf.VrsOut("BRANCH_NAME", saikenJokyo._branchname); // 支店名
            svf.VrsOut("AC_KIND", saikenJokyo._yokinDivName); // 口座種別
            svf.VrsOut("AC_NO", saikenJokyo._accountNo); // 口座番号
            svf.VrsOut("AC_NAME", saikenJokyo._kouzaMeigi); // 口座名義人
        }

        svf.VrsOut("INQ_ZIP", "612-0064");
        svf.VrsOut("INQ_ADDRESS", "京都市伏見区桃山毛利長門西町");
        svf.VrsOut("INQ_NAME1", "京都府総合教育センター内");
        //svf.VrsOut("INQ_PHONE1", "");
        svf.VrsOut("INQ_NAME2", "京都府教育庁指導部高校教育課");
        svf.VrsOut("INQ_PHONE2", "075-574-7518");

        svf.VrEndPage();
        _hasData = true;
    }

    private void printMain2(final Vrw32alp svf, final DB2UDB db2, final String shuugakuNo) {

        final List printList = getPrintListPage2(db2, shuugakuNo);
        
        svf.VrSetForm("KNJTE048.frm", 4);
        
        final int columnMaxLine = 60;
        final int maxLine = columnMaxLine * 2;
        final String totalPage = String.valueOf(printList.size() / maxLine + (printList.size() % maxLine == 0 ? 0 : 1)); 
        int line = 0;
        int column = 1;
        int page = 1;
        BigDecimal henkanGkTotal = new BigDecimal(0);
        boolean hasHenkanGk = false;
        BigDecimal shutoTotalGkTotal = new BigDecimal(0);
        boolean hasShutoTotalGk = false;
        boolean hasLine = false;
        
        for (final Iterator it = printList.iterator(); it.hasNext();) {
            final Map m = (Map) it.next();
            if (line > 0) {
                svf.VrEndRecord();
            }
            line += 1;
            if (line > maxLine) {
                page += 1;
                line -= maxLine;
            }
            column = (line > columnMaxLine) ? 2 : 1;
            final String henkanGk = (String) m.get("HENKAN_GK");
            final String shunoTotalGk = (String) m.get("SHUNO_TOTAL_GK");

            svf.VrsOut("PAGE", String.valueOf(page)); // ページ
            svf.VrsOut("TOTAL_PAGE", totalPage);
            svf.VrsOut("FIELD1", _param._shugakuDate.formatDate(_param._loginDate)); // 日付
            svf.VrsOut("APPLI_NO", (String) m.get("SHUUGAKU_NO")); // 修学生番号
            
            if (null != m.get("CHOTEI_KAISU")) {
                svf.VrsOut("NO", String.valueOf(Integer.valueOf((String) m.get("CHOTEI_KAISU")))); // 回数
            }
            svf.VrsOut("RECEPT_NO", _param._shugakuDate.formatNentuki((String) m.get("CHOTEI_YM"))); // 対象年月
            svf.VrsOut("RET_MONEY", henkanGk); //返還額
            svf.VrsOut("ST_DAY", _param._shugakuDate.formatDate((String) m.get("SHUNO_DATE"))); // 収納日
            svf.VrsOut("ST_MONEY", shunoTotalGk); // 返納額
            
            if (null != henkanGk) {
                henkanGkTotal = henkanGkTotal.add(new BigDecimal(henkanGk));
                hasHenkanGk = true;
            }
            if (null != shunoTotalGk) {
                shutoTotalGkTotal = shutoTotalGkTotal.add(new BigDecimal(shunoTotalGk));
                hasShutoTotalGk = true;
            }
            hasLine = true;
        }
        if (hasHenkanGk) {
            svf.VrsOut("TOTAL_RET_MONEY" + column, henkanGkTotal.toString());
        }
        if (hasShutoTotalGk) {
            svf.VrsOut("TOTAL_ST_MONEY" + column, shutoTotalGkTotal.toString());
        }
        if (!hasLine) {
            svf.VrsOut("RECEPT_NO", "\n");
        }
        svf.VrEndRecord();
        if (0 != line % maxLine) {
            for (int i = 0; i < (maxLine - line % maxLine); i++) {
                svf.VrsOut("RECEPT_NO", "\n");
                svf.VrEndRecord();
            }
        }
        _hasData = true;

    }

    private List getPrintListPage2(final DB2UDB db2, final String shuugakuNo) {
        final List printList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPage2Sql(shuugakuNo);
//            log.debug(" page2 sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final Map m = new HashMap();
                m.put("SHUUGAKU_NO", rs.getString("SHUUGAKU_NO"));
                m.put("CHOTEI_KAISU", rs.getString("CHOTEI_KAISU"));
                m.put("CHOTEI_YM", rs.getString("CHOTEI_YM"));
                m.put("HENKAN_GK", rs.getString("HENKAN_GK"));
                m.put("SHUNO_DATE", rs.getString("SHUNO_DATE"));
                m.put("SHUNO_TOTAL_GK", rs.getString("SHUNO_TOTAL_GK"));
                printList.add(m);
            }

        } catch (SQLException e) {
            log.fatal("exception!", e);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return printList;
    }
    
    public String getPage2Sql(final String shuugakuNo) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SHUUGAKU_NO, ");
        stb.append("     CHOTEI_KAISU, ");
        stb.append("     CHOTEI_YM, ");
        stb.append("     HENKAN_GK, ");
        stb.append("     SHUNO_DATE, ");
        stb.append("     SHUNO_TOTAL_GK ");
        stb.append(" FROM ");
        stb.append("     V_CHOTEI_NOUFU_ADD ");
        stb.append(" WHERE ");
        stb.append("     SHUUGAKU_NO = '" + shuugakuNo + "' ");
        stb.append(" ORDER BY ");
        stb.append("     CHOTEI_KAISU ");
        return stb.toString();
    }
    

    private List getPrintListPage1(final DB2UDB db2) {
        final List printList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPage1Sql();
            log.info(" sql = "  + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String kojinNo = rs.getString("KOJIN_NO");
                final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                final String henkanTotalGk = rs.getString("HENKAN_TOTAL_GK");
                final String menjoTotalGk = rs.getString("MENJO_TOTAL_GK");
                final String shiharaininKbn = rs.getString("SHIHARAININ_KBN");
                final String firstChoteiYm = rs.getString("FIRST_CHOTEI_YM");
                final String startChoteiYm = rs.getString("START_CHOTEI_YM");
                final String endChoteiYm = rs.getString("END_CHOTEI_YM");
                final String kojinName = rs.getString("KOJIN_NAME");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String rentaiName = rs.getString("RENTAI_NAME");
                final String rentaiZipcd = rs.getString("RENTAI_ZIPCD");
                final String rentaiAddr1 = rs.getString("RENTAI_ADDR1");
                final String rentaiAddr2 = rs.getString("RENTAI_ADDR2");
                final String bankname = rs.getString("BANKNAME");
                final String branchname = rs.getString("BRANCHNAME");
                final String yokinDiv = rs.getString("YOKIN_DIV");
                final String yokinDivName = rs.getString("YOKIN_DIV_NAME");
                final String accountNo = rs.getString("ACCOUNT_NO");
                final String kouzaMeigi = rs.getString("KOUZA_MEIGI");
                final String eYuyoYm = rs.getString("E_YUYO_YM");
                final String firstKetteiDate = rs.getString("FIRST_KETTEI_DATE");
                final String sYm = rs.getString("S_YM");
                final String eYm = rs.getString("E_YM");
                final String shiharaiHoho = rs.getString("SHIHARAI_HOHO");
                final String shinken1Name = rs.getString("SHINKEN1_NAME");
                
                final SaikenJokyo saikenJokyo = new SaikenJokyo(kojinNo, shuugakuNo, henkanTotalGk, menjoTotalGk, shiharaininKbn, firstChoteiYm, startChoteiYm, endChoteiYm, kojinName, zipcd, addr1, addr2, rentaiName, rentaiZipcd, rentaiAddr1, rentaiAddr2, bankname, branchname, yokinDiv, yokinDivName, accountNo, kouzaMeigi, eYuyoYm, firstKetteiDate, sYm, eYm, shiharaiHoho, shinken1Name);
                printList.add(saikenJokyo);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return printList;
    }
    
    private String getPage1Sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append("  FIRST_SHINSEI_YEAR_TAIYO_YM AS ( ");
        stb.append("         SELECT DISTINCT ");
        stb.append("             T1.SHUUGAKU_NO, T1.SHINSEI_YEAR, T1.KETTEI_DATE ");
        stb.append("         FROM ");
        stb.append("             KOJIN_SHINSEI_HIST_DAT T1 ");
        stb.append("         INNER JOIN (SELECT SHUUGAKU_NO, MIN(SHINSEI_YEAR) AS SHINSEI_YEAR ");
        stb.append("                     FROM KOJIN_SHINSEI_HIST_DAT T2 ");
        stb.append("                     GROUP BY SHUUGAKU_NO) T2 ON T2.SHUUGAKU_NO = T1.SHUUGAKU_NO AND T2.SHINSEI_YEAR = T1.SHINSEI_YEAR ");
        stb.append("         GROUP BY ");
        stb.append("             T1.SHUUGAKU_NO, T1.SHINSEI_YEAR, T1.KETTEI_DATE ");
        stb.append(" ),  ");
        stb.append("  TAIYO_YM AS ( ");
        stb.append("   SELECT ");
        stb.append("     SHUUGAKU_NO, ");
        stb.append("     MIN(YEAR || '-' || MONTH) AS S_YM, ");
        stb.append("     MAX(YEAR || '-' || MONTH) AS E_YM ");
        stb.append("   FROM ");
        stb.append("     TAIYO_KEIKAKU_DAT ");
        stb.append("   WHERE ");
        stb.append("     FURIKOMI_DATE IS NOT NULL ");
        stb.append("     AND VALUE(SHISHUTSU_GK, 0) <> 0 ");
        stb.append("     AND VALUE(KARI_TEISHI_FLG, '0') <> '1' ");
        stb.append("     AND VALUE(TEISHI_FLG, '0') <> '1' ");
        stb.append("   GROUP BY ");
        stb.append("     SHUUGAKU_NO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     V1.KOJIN_NO, ");
        stb.append("     V1.SHUUGAKU_NO, ");
        stb.append("     HENKAN_TOTAL_GK, ");
        stb.append("     MENJO_TOTAL_GK, ");
        stb.append("     V1.SHIHARAININ_KBN, ");
        stb.append("     V1.FIRST_CHOTEI_YM, ");
        stb.append("     W1.START_CHOTEI_YM, ");
        stb.append("     W1.END_CHOTEI_YM, ");
        stb.append("     CASE WHEN S1.SOUFU_NAME IS NOT NULL THEN S1.SOUFU_NAME");
        stb.append("              WHEN S2.SOUFU_NAME IS NOT NULL THEN S2.SOUFU_NAME");
        stb.append("              ELSE CONCAT(CONCAT(V3.FAMILY_NAME,'　'),V3.FIRST_NAME)");
        stb.append("     END AS KOJIN_NAME,");
        stb.append("     case when S1.zip_cd is not null then S1.zip_cd ");
        stb.append("          when S2.zip_cd is not null then S2.zip_cd ");
        stb.append("          else V3.zipcd ");
        stb.append("     end as zipcd, ");
        stb.append("     case when S1.addr1 is not null then S1.addr1 ");
        stb.append("          when S2.addr1 is not null then S2.addr1 ");
        stb.append("          else V3.addr1 ");
        stb.append("     end as addr1, ");
        stb.append("     case when S1.addr2 is not null then S1.addr2 ");
        stb.append("          when S2.addr2 is not null then S2.addr2 ");
        stb.append("          else V3.addr2 ");
        stb.append("     end as addr2, ");
        stb.append("     CONCAT(CONCAT(V4.FAMILY_NAME,'　'),V4.FIRST_NAME) as RENTAI_NAME, ");
        stb.append("     V4.ZIPCD AS RENTAI_ZIPCD, ");
        stb.append("     V4.ADDR1 AS RENTAI_ADDR1, ");
        stb.append("     V4.ADDR2 AS RENTAI_ADDR2, ");
        stb.append("     V5.BANKNAME, ");
        stb.append("     V5.BRANCHNAME, ");
        stb.append("     V5.YOKIN_DIV, ");
        stb.append("     T22.ABBV1 AS YOKIN_DIV_NAME, ");
        stb.append("     V5.ACCOUNT_NO, ");
        stb.append("     CONCAT(CONCAT(BANK_MEIGI_SEI_KANA,' '),BANK_MEIGI_MEI_KANA) AS KOUZA_MEIGI, ");
        stb.append("     T1.E_YUYO_YM, ");
        stb.append("     CONCAT(CONCAT(V6.FAMILY_NAME,'　'),V6.FIRST_NAME) AS SHINKEN1_NAME, ");
        stb.append("     V1.FIRST_HENKAN_GK, ");
        stb.append("     case when V1.henkan_total_gk = V1.FIRST_HENKAN_GK then 0 ");
        stb.append("          else V1.LAST_HENKAN_GK ");
        stb.append("     end as last_henkan_gk, ");
        stb.append("     T2.NAME1 AS HENKAN_HOHO_NAME, ");
        stb.append("     V1.SHIKIN_SHUBETSU, ");
        stb.append("     T3.KETTEI_DATE AS FIRST_KETTEI_DATE, ");
        stb.append("     T4.S_YM, ");
        stb.append("     T4.E_YM, ");
        stb.append("     V1.SHIHARAI_HOHO ");
        stb.append(" FROM ");
        stb.append("     v_saiken_jokyo V1 ");
        stb.append("     left join (select min(chotei_ym) as start_chotei_ym, max(chotei_ym) as end_chotei_ym,shuugaku_no from chotei_dat where torikesi_flg = '0' and funo_flg = '0' group by shuugaku_no) as W1 on V1.shuugaku_no = W1.shuugaku_no ");
        stb.append("     left join v_kojin_shuugaku_shinsei_hist_dat V2 on V1.shuugaku_no = V2.shuugaku_no ");
        stb.append("     left join v_kojin_hist_dat V3 on V2.kojin_no = V3.kojin_no ");
        stb.append("     left join shinkensha_hist_dat V4 on v2.rentai_cd = V4.shinken_cd ");
        stb.append("     left join v_furikae_kouza_newest V5 on V1.shuugaku_no = V5.shuugaku_no ");
        stb.append("     left join v_shinkensha_hist_dat V6 on v2.shinken1_cd = V6.shinken_cd ");
        stb.append("     left join (select max(yuyo_seq) as max_yuyo_seq,shuugaku_no from yuyo_dat where yuyo_kekka_cd = '2' and upd_kubun = '1' group by shuugaku_no) as W2 on V1.shuugaku_no = W2.shuugaku_no ");
        stb.append("     left join yuyo_dat t1 on W2.shuugaku_no = T1.shuugaku_no and W2.max_yuyo_seq = T1.yuyo_seq ");
        stb.append("     left join name_mst t2 on V1.henkan_hoho = t2.namecd2 and t2.namecd1 = 'T016' ");
        stb.append("     left join soufu_address_dat S1 on V1.kojin_no = S1.kojin_no and V1.shuugaku_no = S1.shuugaku_no ");
        stb.append("     left join soufu_address_dat S2 on V1.kojin_no = S2.kojin_no and S2.shuugaku_no = '9999999' ");
        stb.append("     LEFT JOIN NAME_MST T22 ON T22.NAMECD1 = 'T032' AND T22.NAMECD2 = V5.YOKIN_DIV ");
        stb.append("     LEFT JOIN FIRST_SHINSEI_YEAR_TAIYO_YM T3 ON T3.SHUUGAKU_NO = V1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN TAIYO_YM T4 ON T4.SHUUGAKU_NO = V1.SHUUGAKU_NO ");
        stb.append(" WHERE ");
        if ("1".equals(_param._output)) {
            stb.append("     V1.FIRST_CHOTEI_YM = '" + _param._firstChoteiYm + "' ");
        } else if ("2".equals(_param._output)) {
            stb.append("     T1.E_YUYO_YM = '" + _param._eYuyoYm + "' ");
        } else if ("3".equals(_param._output)) {
            stb.append("     V1.shuugaku_no in " + SQLUtils.whereIn(true, _param._listcheck) + " ");
        } else if ("4".equals(_param._output)) {
            stb.append("     V1.shuugaku_no in " + SQLUtils.whereIn(true, _param._shuugakuNoList) + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     V1.kojin_no, ");
        stb.append("     V1.shuugaku_no ");
        return stb.toString();
    }
    
    private static class SaikenJokyo {
        final String _kojinNo;
        final String _shuugakuNo;
        final String _henkanTotalGk;
        final String _menjoTotalGk;
        final String _shiharaininKbn;
        final String _firstChoteiYm;
        final String _startChoteiYm;
        final String _endChoteiYm;
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
        final String _eYuyoYm;
        final String _firstKetteiDate;
        final String _sYm;
        final String _eYm;
        final String _shiharaiHoho;
        final String _shinken1Name;
        SaikenJokyo(
                final String kojinNo,
                final String shuugakuNo,
                final String henkanTotalGk,
                final String menjoTotalGk,
                final String shiharaininKbn,
                final String firstChoteiYm,
                final String startChoteiYm,
                final String endChoteiYm,
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
                final String kouzaMeigi,
                final String eYuyoYm,
                final String firstKetteiDate,
                final String sYm,
                final String eYm,
                final String shiharaiHoho,
                final String shinken1Name
        ) {
            _kojinNo = kojinNo;
            _shuugakuNo = shuugakuNo;
            _henkanTotalGk = henkanTotalGk;
            _menjoTotalGk = menjoTotalGk;
            _shiharaininKbn = shiharaininKbn;
            _firstChoteiYm = firstChoteiYm;
            _startChoteiYm = startChoteiYm;
            _endChoteiYm = endChoteiYm;
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
            _eYuyoYm = eYuyoYm;
            _firstKetteiDate = firstKetteiDate;
            _sYm = sYm;
            _eYm = eYm;
            _shiharaiHoho = shiharaiHoho;
            _shinken1Name = shinken1Name;
        }
    }

    private static int getMS932Length(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
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
        log.fatal("$Revision: 67186 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginDate;
        private final String _output;
        private final String _firstChoteiYm;
        private final String _eYuyoYm;
        private final String _shuugakuNo;
        private final String[] _listcheck;
        private final String[] _shuugakuNoList;
        private final String _addrDiv;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate = request.getParameter("LOGIN_DATE");
            _output = request.getParameter("OUTPUT");
            _shuugakuNo = request.getParameter("SHUUGAKU_NO");
            _listcheck = request.getParameterValues("LISTCHECK[]");
            _shuugakuNoList = StringUtils.split(request.getParameter("SHUUGAKU_NO_LIST"), ",");
            _addrDiv = request.getParameter("ADDR_DIV");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _firstChoteiYm = _shugakuDate.d5toYmStr(request.getParameter("FIRST_CHOTEI_YM"));
            _eYuyoYm = _shugakuDate.d5toYmStr(request.getParameter("E_YUYO_YM"));
        }
    }
}

// eof

