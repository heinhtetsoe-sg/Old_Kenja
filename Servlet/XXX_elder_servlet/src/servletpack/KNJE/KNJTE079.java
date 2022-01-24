/*
 * $Id: a9df89f0af0e11e87483c584640702795a524e77 $
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * 完納通知書
 */
public class KNJTE079 {

    private static final Log log = LogFactory.getLog(KNJTE079.class);

    private boolean _hasData;

    private Param _param;
    
    private static final String cmdPrintCsv = "printCsv";
    private static final String cmdPrint = "print";
    
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
        
        final List printList = getTaiyoList(db2);
        setTakashitsukeList(db2, printList);
        for (final Iterator it = printList.iterator(); it.hasNext();) {
            final Taiyo taiyo = (Taiyo) it.next();

            svf.VrSetForm("KNJTE079.frm", 1);
            // log.debug(" kojinNo = " + taiyo._kojinNo + ",  shuugakuNo = " + taiyo._shuugakuNo);

            svf.VrsOut("CERT_NO", _param.getBunshoBangou(_param)); // 文書番号
            svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._outputDate)); // 印刷日付

//            if ("1".equals(_param._addrDiv) || "3".equals(_param._addrDiv) && "1".equals(saikenJokyo._shiharaininKbn)) {
            svf.VrsOut("ZIP_NO", taiyo._zipcd); // 郵便番号
            svf.VrsOut("ADDRESS1", taiyo._addr1); // 住所
            VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(taiyo._addr2, 40, 2), svf);
            svf.VrsOut("NAME", taiyo._shuugakuName); // 氏名
//            } else if ("2".equals(_param._addrDiv) || "3".equals(_param._addrDiv) && "2".equals(saikenJokyo._shiharaininKbn)) {
//                svf.VrsOut("ZIP_NO", saikenJokyo._rentaiZipcd); // 郵便番号
//                svf.VrsOut("ADDRESS1", saikenJokyo._rentaiAddr1); // 住所
//                VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(saikenJokyo._rentaiAddr2, 40, 2), svf);
//                svf.VrsOut("NAME", saikenJokyo._rentaiName); // 氏名
//            }

            svf.VrsOut("APPLI_NO", taiyo._shuugakuNo); // 修学生番号
            svf.VrsOut("LOAN_MONEY", taiyo._henkanTotalGk); // 借用金額
            svf.VrsOut("PAY_MONEY", taiyo._noufuGk); // 返還済額
            
            for (int i = 0; i < Math.min(2, taiyo._taKashitsukeList.size()); i++) {
                svf.VrsOut("SUB_TITLE", "返還中の他貸付金");
                final TaKashitsuke tk = (TaKashitsuke) taiyo._taKashitsukeList.get(i);
                svf.VrsOutn("APPLI_NO2", (i + 1), tk._shuugakuNo);
                svf.VrsOutn("LOAN_NAME", (i + 1), tk._t008abbv2);
                svf.VrsOutn("LOAN_MONEY2", (i + 1), tk._henkanTotalGk);
                svf.VrsOutn("PAY_MONEY2", (i + 1), tk._gakuShuno);
                svf.VrsOutn("BALANCE2", (i + 1), tk._gakuMinou);
            }
            
            svf.VrsOut("DAY1", _param._shugakuDate.nengoNenTukiFromDate(taiyo._kannoDate)); // 日付
            svf.VrsOut("GOVERNER", "京都府教育庁指導部高校教育課長");
            svf.VrsOut("KIND_NAME", taiyo._t008abbv2);

            if (null != taiyo._henkanTotalGk) {
                final BigDecimal bdHenkanTotalGk = new BigDecimal(taiyo._henkanTotalGk);
                BigDecimal bdNoufuGk = new BigDecimal(0);
                if (null != taiyo._noufuGk) {
                    bdNoufuGk = new BigDecimal(taiyo._noufuGk);
                }
                svf.VrsOut("BALANCE", bdHenkanTotalGk.subtract(bdNoufuGk).toString()); // 残高
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void setTakashitsukeList(final DB2UDB db2, final List printList) {
        final Set kojinNoSet = new HashSet();
        for (final Iterator it = printList.iterator(); it.hasNext();) {
            final Taiyo taiyo = (Taiyo) it.next();
            kojinNoSet.add(taiyo._kojinNo);
        }
        log.fatal(" kojinNo count = " + kojinNoSet.size());
        int i = 0;
        for (final Iterator it = getCountList(kojinNoSet, 50).iterator(); it.hasNext();) {
            final List list = (List) it.next();
            final Map kojinNoTakashitsukeMap = (Map) getTaKashitsukeMap(db2, list);
            log.fatal(" takashitsuke " + (i * 50) + " - " + (i * 50 + list.size()));
            for (final Iterator it2 = kojinNoTakashitsukeMap.keySet().iterator(); it2.hasNext();) {
                final String kojinNo = (String) it2.next();
                final List takashitsukeList = (List) kojinNoTakashitsukeMap.get(kojinNo);
                if (null == takashitsukeList || takashitsukeList.size() <= 1) {
                    continue;
                }
                
                for (final Iterator it3 = printList.iterator(); it3.hasNext();) {
                    final Taiyo taiyo = (Taiyo) it3.next();
                    if (kojinNo.equals(taiyo._kojinNo)) {
                        taiyo._taKashitsukeList = new ArrayList();
                        for (final Iterator it4 = takashitsukeList.iterator(); it4.hasNext();) {
                            final TaKashitsuke takashitsuke = (TaKashitsuke) it4.next();
                            if (takashitsuke._shuugakuNo.equals(taiyo._shuugakuNo)) {
                                continue;
                            }
                            taiyo._taKashitsukeList.add(takashitsuke);
                        }
                    }
                }
            }
            i += 1;
        }
    }
    
    private Map getTaKashitsukeMap(final DB2UDB db2, final Collection col) {
        final Map kojinNoTakashitsuke = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT T1.KOJIN_NO, T1.SHUUGAKU_NO, T1.HENKAN_TOTAL_GK, T1.GAKU_SHUNO, T1.GAKU_MINOU, T2.ABBV2 AS T008ABBV2 ");
            sql.append(" FROM V_SAIKEN_JOKYO T1 ");
            sql.append(" LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'T008' AND T2.NAMECD2 = T1.SHIKIN_SHUBETSU ");
            sql.append(" WHERE T1.KOJIN_NO IN " + SQLUtils.whereIn(true, toArray(col))  + " ");
            sql.append(" AND VALUE(T1.GAKU_MINOU, 0) <> 0 ");
            sql.append(" ORDER BY T1.SHUUGAKU_NO ");
            log.debug(" sql takashitsuke = " + sql.toString());
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String kojinNo = rs.getString("KOJIN_NO");
                final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                final String henkanTotalGk = rs.getString("HENKAN_TOTAL_GK");
                final String gakuShuno = rs.getString("GAKU_SHUNO");
                final String gakuMinou = rs.getString("GAKU_MINOU");
                final String t008abbv2 = rs.getString("T008ABBV2");

                final TaKashitsuke takashitsuke = new TaKashitsuke(shuugakuNo, henkanTotalGk, gakuShuno, gakuMinou, t008abbv2);
                
                if (null == kojinNoTakashitsuke.get(kojinNo)) {
                    kojinNoTakashitsuke.put(kojinNo, new ArrayList());
                }
                final List list = (List) kojinNoTakashitsuke.get(kojinNo); 
                list.add(takashitsuke);
            }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        return kojinNoTakashitsuke;
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
    
    private List getCountList(final Collection col, final int n) {
        final List rtn = new ArrayList();
        List current = new ArrayList();
        rtn.add(current);
        for (final Iterator it = col.iterator(); it.hasNext();) {
            Object o = it.next();
            if (current.size() >= n) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private List getTaiyoList(final DB2UDB db2) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String taiyoSql = getTaiyoSql();
            log.fatal(" taiyoSql = " + taiyoSql);
            ps = db2.prepareStatement(taiyoSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String kojinNo = rs.getString("KOJIN_NO");
                final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                final String zipcd = rs.getString("ZIPCD");
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                final String shuugakuName = rs.getString("SHUUGAKU_NAME");
                final String shiharaininName = rs.getString("SHIHARAININ_NAME");
                final String abbv3 = rs.getString("ABBV3");
                final String henkanTotalGk = rs.getString("HENKAN_TOTAL_GK");
                final String menjoTotalGk = rs.getString("MENJO_TOTAL_GK");
                final String noufuGk = rs.getString("NOUFU_GK");
                final String kannoDate = rs.getString("KANNO_DATE");
                final String t008abbv2 = rs.getString("T008ABBV2");

                final Taiyo taiyo = new Taiyo(kojinNo, shuugakuNo, zipcd, addr1, addr2, shuugakuName, shiharaininName, abbv3, henkanTotalGk, menjoTotalGk, noufuGk, kannoDate, t008abbv2);
                list.add(taiyo);
            }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        return list;
    }
    
    private String getTaiyoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     V2.KOJIN_NO, ");
        stb.append("     V2.SHUUGAKU_NO, ");
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
        stb.append("     CASE WHEN S1.SOUFU_NAME IS NOT NULL THEN S1.SOUFU_NAME ");
        stb.append("          WHEN S2.SOUFU_NAME IS NOT NULL THEN S2.SOUFU_NAME ");
        stb.append("          ELSE CONCAT(CONCAT(V3.FAMILY_NAME,'　'),V3.FIRST_NAME) ");
        stb.append("     END AS SHUUGAKU_NAME, ");
        stb.append("     CASE WHEN V1.SHIHARAININ_KBN = '1' THEN ");
        stb.append("       CONCAT(CONCAT(V3.FAMILY_NAME,'　'),V3.FIRST_NAME) ");
        stb.append("     ELSE ");
        stb.append("       CONCAT(CONCAT(V4.FAMILY_NAME,'　'),V4.FIRST_NAME) ");
        stb.append("     END AS SHIHARAININ_NAME, ");
        stb.append("     T3.ABBV3, ");
        stb.append("     V1.HENKAN_TOTAL_GK, ");
        stb.append("     V1.MENJO_TOTAL_GK, ");
        stb.append("     NOUFU_GK, ");
        stb.append("     KANNO_DATE, ");
        stb.append("     T5.ABBV2 AS T008ABBV2 ");
        stb.append(" FROM ");
        stb.append("     KOJIN_TAIYO_DAT  T1 ");
        stb.append("     LEFT JOIN V_SAIKEN_JOKYO V1 ON T1.SHUUGAKU_NO = V1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN (SELECT MAX(SHUNO_DATE) AS KANNO_DATE,SUM(HAKKO_TOTAL_GK) AS NOUFU_GK,SHUUGAKU_NO FROM NOUFU_DAT GROUP BY SHUUGAKU_NO) AS W1 ON T1.SHUUGAKU_NO = W1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN V_KOJIN_SHUUGAKU_SHINSEI_HIST_DAT V2 ON T1.SHUUGAKU_NO = V2.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN V_KOJIN_HIST_DAT V3 ON T1.KOJIN_NO = V3.KOJIN_NO  ");
        stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT V4 ON V2.RENTAI_CD = V4.SHINKEN_CD ");
        stb.append("     LEFT JOIN NAME_MST T3 ON V2.SHIKIN_SHOUSAI_DIV = T3.NAMECD2 AND T3.NAMECD1 = 'T030' ");
        stb.append("     LEFT JOIN SAIKEN_DAT T4 ON T1.SHUUGAKU_NO = T4.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S1 ON T1.KOJIN_NO = S1.KOJIN_NO AND T1.SHUUGAKU_NO = S1.SHUUGAKU_NO ");
        stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S2 ON T1.KOJIN_NO = S2.KOJIN_NO AND S2.SHUUGAKU_NO = '9999999' ");
        stb.append("     LEFT JOIN NAME_MST T5 ON T5.NAMECD2 = T3.ABBV3 AND T5.NAMECD1 = 'T008' ");
        stb.append(" WHERE ");
        stb.append("     T1.SOUGOU_STATUS_FLG = '5' AND ");
        stb.append("     KANNO_DATE BETWEEN '" + _param._sKannoYm + "-01' AND '" + _param.getYmd(_param._eKannoYm) + "' AND ");
        if (cmdPrintCsv.equals(_param._cmd) || !StringUtils.isBlank(_param._updTm)) {
            stb.append("     T4.UKE_YEAR || T4.UKE_NO || T4.UKE_EDABAN = '" + _param.getUkeno()  + "' AND T4.UPDATED = '" + _param._updTm + "' ");
        } else { // if (cmdPrint.equals(param._cmd)) {
            stb.append("     T4.UKE_NO IS NULL ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.KOJIN_NO, ");
        stb.append("     T1.SHUUGAKU_NO ");
        return stb.toString();
    }

    private static class Taiyo {
        final String _kojinNo;
        final String _shuugakuNo;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _shuugakuName;
        final String _shiharaininName;
        final String _abbv3;
        final String _henkanTotalGk;
        final String _menjoTotalGk;
        final String _noufuGk;
        final String _kannoDate;
        final String _t008abbv2;
        List _taKashitsukeList = Collections.EMPTY_LIST;

        Taiyo(
                final String kojinNo,
                final String shuugakuNo,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String shuugakuName,
                final String shiharaininName,
                final String abbv3,
                final String henkanTotalGk,
                final String menjoTotalGk,
                final String noufuGk,
                final String kannoDate,
                final String t008abbv2
        ) {
            _kojinNo = kojinNo;
            _shuugakuNo = shuugakuNo;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _shuugakuName = shuugakuName;
            _shiharaininName = shiharaininName;
            _abbv3 = abbv3;
            _henkanTotalGk = henkanTotalGk;
            _menjoTotalGk = menjoTotalGk;
            _noufuGk = noufuGk;
            _kannoDate = kannoDate;
            _t008abbv2 = t008abbv2;
        }
    }
    
    private static class TaKashitsuke {
        final String _shuugakuNo;
        final String _henkanTotalGk;
        final String _gakuShuno;
        final String _gakuMinou;
        final String _t008abbv2;
        public TaKashitsuke(final String shuugakuNo, final String henkanTotalGk, final String gakuShuno, final String gakuMinou, final String t008abbv2) {
            _shuugakuNo = shuugakuNo;
            _henkanTotalGk = henkanTotalGk;
            _gakuShuno = gakuShuno;
            _gakuMinou = gakuMinou;
            _t008abbv2 = t008abbv2;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67239 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _loginDate;
        final String _ukeYear;
        final String _ukeNo;
        final String _ukeEdaban;
        final String _sKannoYm;
        final String _eKannoYm;
        final String _outputDate;
        final String _updTm;
        final String _cmd; // 'print':印刷のみ、'printCsv':印刷更新
        final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate = request.getParameter("LOGIN_DATE");
            _ukeYear = request.getParameter("UKE_YEAR");
            _ukeNo = request.getParameter("UKE_NO");
            _ukeEdaban = request.getParameter("UKE_EDABAN");
            _updTm = request.getParameter("UPD_TM");
            _cmd = request.getParameter("cmd");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _sKannoYm = _shugakuDate.d5toYmStr(request.getParameter("S_KANNO_YM"));
            _eKannoYm = _shugakuDate.d5toYmStr(request.getParameter("E_KANNO_YM"));
            _outputDate = _shugakuDate.d7toDateStr(request.getParameter("OUTPUT_DATE"));
        }
        
        public String getBunshoBangou(final Param param) {
            if (null == getUkeno()) {
                return " 教高第 号";
            }
            try {
                final String wa = param._shugakuDate.getUkeYearNum(_ukeYear);
                final String bangou = StringUtils.isBlank(_ukeNo) ? "" : String.valueOf(Integer.parseInt(_ukeNo));
                final String edaban = (StringUtils.isBlank(_ukeEdaban) || Integer.parseInt(_ukeEdaban) == 1) ? "" : ("の" + Integer.parseInt(_ukeEdaban));
                return wa + "教高第" + bangou + "号" + edaban;
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }
        
        public String getUkeno() {
            if (StringUtils.isBlank(_ukeYear) || StringUtils.isBlank(_ukeNo)) {
                return null;
            }
            final DecimalFormat df4 = new DecimalFormat("0000");
            final DecimalFormat df3 = new DecimalFormat("000");
            return df4.format(Integer.parseInt(_ukeYear)) + df4.format(Integer.parseInt(_ukeNo)) + df3.format(StringUtils.isBlank(_ukeEdaban) ? 1 : Integer.parseInt(_ukeEdaban));
        }
        
        private String getYmd(final String ym) {
            final int year = Integer.parseInt(ym.substring(0, 4));
            final int month = Integer.parseInt(ym.substring(5, 7));
            final Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.DATE, -1);
            final int maxDateOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            final DecimalFormat df = new DecimalFormat("00");
            return ym + "-" + df.format(maxDateOfMonth);
        }
    }
}

// eof

