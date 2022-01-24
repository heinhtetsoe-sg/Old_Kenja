/*
 * $Id: 30aa18ec3a1327edb1eaf3059b628409e1cf9dbc $
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 住民票の写し等請求書
 */
public class KNJTE076 {

    private static final Log log = LogFactory.getLog(KNJTE076.class);

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
    
    private void VrsOutn(final String[] field1, final int j, final String[] data, final Vrw32alp svf) {
        if (null == data) {
            return;
        }
        for (int i = 0; i < Math.min(field1.length, data.length); i++) {
            svf.VrsOutn(field1[i], j, data[i]);
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

        final List list = getList(db2);
        
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Yakuba yakuba = (Yakuba) it.next();
            
            svf.VrSetForm("KNJTE076.frm", 1);
            
            svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._addTuchiDate)); // 日付
            svf.VrsOut("GOVERNER", "京都府教育庁指導部高校教育課長");

            for (int i = 0; i < yakuba._henreiList.size(); i++) {
                final Henrei henrei = (Henrei) yakuba._henreiList.get(i);
                
                svf.VrsOut("ZIP_NO", henrei._zipCd);
                svf.VrsOut("CERT_NO", _param.getBunshoBangou(_param)); // 証明書番号
                printAddress(svf, henrei._yakubaAddr1, henrei._yakubaAddr2);
//                svf.VrsOut("ADDRESS1", henrei._yakubaAddr1);
//                VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(henrei._yakubaAddr2, 40, 2), svf);
                svf.VrsOut("NAME", henrei._yakubaAtena); // 宛名
                
                final int l = i + 1;
                final int namelen = getMS932Length(henrei._nameKana);
                svf.VrsOutn("CLAIM_KANA" + (namelen > 30 ? "3" : namelen > 20 ? "2" : "1"), l, henrei._nameKana); // 氏名

                svf.VrsOutn("CLAIM_NAME1", l, henrei._nameKanji);

                svf.VrsOutn("CLAIM_ADDRESS1", l, henrei._addr1);
                VrsOutn(new String[]{"CLAIM_ADDRESS2", "CLAIM_ADDRESS3"}, l, KNJ_EditEdit.get_token(henrei._addr2, 40, 2), svf);

                svf.VrsOutn("CLAIM_BIRTHDAY", l, _param._shugakuDate.formatDate(henrei._birthday)); // 誕生日
            }
            svf.VrsOut("INQ_ZIP", "６０２‐８５７０"); // 問い合わせ先郵便番号
            svf.VrsOut("INQ_ADDRESS", "京都市上京区下立売通新町西入"); // 問い合わせ先住所
            svf.VrsOut("INQ_NAME", "京都府教育庁指導部高校教育課"); // 問い合わせ先課
            svf.VrsOut("INQ_NAME2", "修学支援担当"); // 問い合わせ担当
            svf.VrsOut("INQ_PHONE1", "０７５‐４１４‐５１５９"); // 問い合わせ先電話番号
            svf.VrEndPage();
            _hasData = true;
        }
    }
    
    private static int getMS932Length(final String s) {
    	return KNJ_EditEdit.getMS932ByteLength(s);
    }

    private List getList(final DB2UDB db2) {
        final List list = new ArrayList();
        final List henreiList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
             final String sql = sql();
             log.debug(" sql = " + sql);
             ps = db2.prepareStatement(sql);
             rs = ps.executeQuery();
             while (rs.next()) {
                 final String zipCd = rs.getString("ZIP_CD");
                 final String yakubaAddr1 = rs.getString("YAKUBA_ADDR1");
                 final String yakubaAddr2 = rs.getString("YAKUBA_ADDR2");
                 final String yakubaAtena = rs.getString("YAKUBA_ATENA");
                 final String nameKana = rs.getString("NAME_KANA");
                 final String nameKanji = rs.getString("NAME_KANJI");
                 final String birthday = rs.getString("BIRTHDAY");
                 final String addr1 = rs.getString("ADDR1");
                 final String addr2 = rs.getString("ADDR2");
                 final Henrei henrei = new Henrei(zipCd, yakubaAddr1, yakubaAddr2, yakubaAtena, nameKana, nameKanji, birthday, addr1, addr2);
                 henreiList.add(henrei);
             }
             
             for (final Iterator it = henreiList.iterator(); it.hasNext();) {
                 final Henrei henrei = (Henrei) it.next();

                 boolean isUniq = true;
                 for (final Iterator it2 = henreiList.iterator(); it2.hasNext();) {
                     final Henrei henrei0 = (Henrei) it2.next();
                     if (null == henrei0) {
                         continue;
                     }
                     if (henrei0 == henrei) {
                         break;
                     }
                     if (!henrei0.hasDifference(henrei)) {
                         isUniq = false;
                     }
                 }
                 if (!isUniq) {
                     continue;
                 }
                 Yakuba yakuba = getYakuba(list, henrei._zipCd);
                 if (null == yakuba) {
                     yakuba = new Yakuba(henrei._zipCd);
                     list.add(yakuba);
                 }
                 yakuba._henreiList.add(henrei);
             }
        } catch (Exception ex) {
             log.fatal("exception!", ex);
        } finally {
             DbUtils.closeQuietly(null, ps, rs);
             db2.commit();
        }
        return list;
    }
    
    private Yakuba getYakuba(final List list, final String yakubaCd) {
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Yakuba yakuba = (Yakuba) it.next();
            if (yakuba._yakubaCd.equals(yakubaCd) && yakuba._henreiList.size() < 3) {
                return yakuba;
            }
        }
        return null;
    }
    
    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     YAKUBA_CD, ");
        if ("csvKojin1".equals(_param._cmd) || "csvKojin2".equals(_param._cmd)) {
        } else {
            stb.append("     SHUUGAKU_NO, ");
        }
        stb.append("     ZIP_CD, ");
        stb.append("     YAKUBA_ADDR1, ");
        stb.append("     YAKUBA_ADDR2, ");
        if ("csvKojin1".equals(_param._cmd) || "csvKojin2".equals(_param._cmd)) {
            stb.append("     CASE WHEN YAKUBA_ATENA1 IS NOT NULL THEN YAKUBA_ATENA1 ");
            stb.append("          ELSE YAKUBA_ATENA2 ");
            stb.append("     END AS YAKUBA_ATENA, ");
        } else {
            stb.append("     CASE WHEN YAKUBA_ATENA1 IS NOT NULL THEN YAKUBA_ATENA1 ");
            stb.append("          WHEN YAKUBA_ATENA2 IS NOT NULL THEN YAKUBA_ATENA2 ");
            stb.append("          ELSE YAKUBA_ATENA3 ");
            stb.append("     END AS YAKUBA_ATENA, ");
        }
        stb.append("     NAME_KANA, ");
        stb.append("     NAME_KANJI, ");
        stb.append("     BIRTHDAY, ");
        stb.append("     ADDR1, ");
        stb.append("     ADDR2 ");
        stb.append(" FROM ");
        stb.append(" ( ");
        if ("csvKojin1".equals(_param._cmd) || "csvKojin2".equals(_param._cmd)) {
            stb.append(" SELECT ");
            stb.append("     CASE WHEN S2.CITY_CD IS NOT NULL THEN T3.YAKUBA_CD ");
            stb.append("          ELSE T2.YAKUBA_CD ");
            stb.append("     END AS YAKUBA_CD, ");
            stb.append("     CASE WHEN S2.CITY_CD IS NOT NULL THEN T3.ZIP_CD ");
            stb.append("          ELSE T2.ZIP_CD ");
            stb.append("     END AS ZIP_CD, ");
            stb.append("     CASE WHEN S2.CITY_CD IS NOT NULL THEN T3.YAKUBA_ADDR1 ");
            stb.append("          ELSE T2.YAKUBA_ADDR1 ");
            stb.append("     END AS YAKUBA_ADDR1, ");
            stb.append("     CASE WHEN S2.CITY_CD IS NOT NULL THEN T3.YAKUBA_ADDR2 ");
            stb.append("          ELSE T2.YAKUBA_ADDR2 ");
            stb.append("     END AS YAKUBA_ADDR2, ");
            stb.append("     CASE WHEN SUBSTR(T3.YAKUBA_CD,3,3) = '000' THEN CONCAT(CONCAT(T3.YAKUBA_ATENA,'　'),'知事') ");
            stb.append("          ELSE CONCAT(CONCAT(CONCAT(T3.YAKUBA_ATENA,'　'),RIGHT(T3.YAKUBA_NAME,3)),'長') ");
            stb.append("     END AS YAKUBA_ATENA1, ");
            stb.append("     CASE WHEN SUBSTR(T2.YAKUBA_CD,3,3) = '000' THEN CONCAT(CONCAT(T2.YAKUBA_ATENA,'　'),'知事') ");
            stb.append("          ELSE CONCAT(CONCAT(CONCAT(T2.YAKUBA_ATENA,'　'),RIGHT(T2.YAKUBA_NAME,3)),'長') ");
            stb.append("     END AS YAKUBA_ATENA2, ");
            stb.append("     CONCAT(CONCAT(V2.FAMILY_NAME_KANA,' '),V2.FIRST_NAME_KANA) AS NAME_KANA, ");
            stb.append("     CONCAT(CONCAT(V2.FAMILY_NAME,'　'),V2.FIRST_NAME) AS NAME_KANJI, ");
            stb.append("     V2.BIRTHDAY, ");
            stb.append("     CASE WHEN S2.ADDR1 IS NOT NULL THEN S2.ADDR1 ");
            stb.append("          ELSE V2.ADDR1 ");
            stb.append("     END AS ADDR1, ");
            stb.append("     CASE WHEN S2.ADDR2 IS NOT NULL THEN S2.ADDR2 ");
            stb.append("          ELSE V2.ADDR2 ");
            stb.append("     END AS ADDR2 ");
            stb.append(" FROM ");
            stb.append("     V_KOJIN_HIST_DAT V2 ");
            stb.append("     LEFT JOIN YAKUBA_DAT T2 ON V2.CITYCD = T2.YAKUBA_CD ");
            stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S2 ON V2.KOJIN_NO = S2.KOJIN_NO AND S2.SHUUGAKU_NO = '9999999' ");
            stb.append("     LEFT JOIN YAKUBA_DAT T3 ON S2.CITY_CD = T3.YAKUBA_CD ");
            stb.append(" WHERE ");
            stb.append("     V2.KOJIN_NO = '" + _param._arg + "' ");
            stb.append(" ) T1 ");
        } else {
            stb.append(" SELECT ");
            stb.append("     CASE WHEN S1.CITY_CD IS NOT NULL THEN T3.YAKUBA_CD ");
            stb.append("          WHEN S2.CITY_CD IS NOT NULL THEN T4.YAKUBA_CD ");
            stb.append("          ELSE T2.YAKUBA_CD ");
            stb.append("     END AS YAKUBA_CD, ");
            stb.append("     T1.SHUUGAKU_NO, ");
            stb.append("     CASE WHEN S1.CITY_CD IS NOT NULL THEN T3.ZIP_CD ");
            stb.append("          WHEN S2.CITY_CD IS NOT NULL THEN T4.ZIP_CD ");
            stb.append("          ELSE T2.ZIP_CD ");
            stb.append("     END AS ZIP_CD, ");
            stb.append("     CASE WHEN S1.CITY_CD IS NOT NULL THEN T3.YAKUBA_ADDR1 ");
            stb.append("          WHEN S2.CITY_CD IS NOT NULL THEN T4.YAKUBA_ADDR1 ");
            stb.append("          ELSE T2.YAKUBA_ADDR1 ");
            stb.append("     END AS YAKUBA_ADDR1, ");
            stb.append("     CASE WHEN S1.CITY_CD IS NOT NULL THEN T3.YAKUBA_ADDR2 ");
            stb.append("          WHEN S2.CITY_CD IS NOT NULL THEN T4.YAKUBA_ADDR2 ");
            stb.append("          ELSE T2.YAKUBA_ADDR2 ");
            stb.append("     END AS YAKUBA_ADDR2, ");
            stb.append("     CASE WHEN SUBSTR(T3.YAKUBA_CD,3,3) = '000' THEN CONCAT(CONCAT(T3.YAKUBA_ATENA,'　'),'知事') ");
            stb.append("          ELSE CONCAT(CONCAT(CONCAT(T3.YAKUBA_ATENA,'　'),RIGHT(T3.YAKUBA_NAME,3)),'長') ");
            stb.append("     END AS YAKUBA_ATENA1, ");
            stb.append("     CASE WHEN SUBSTR(T4.YAKUBA_CD,3,3) = '000' THEN CONCAT(CONCAT(T4.YAKUBA_ATENA,'　'),'知事') ");
            stb.append("          ELSE CONCAT(CONCAT(CONCAT(T4.YAKUBA_ATENA,'　'),RIGHT(T4.YAKUBA_NAME,3)),'長') ");
            stb.append("     END AS YAKUBA_ATENA2, ");
            stb.append("     CASE WHEN SUBSTR(T2.YAKUBA_CD,3,3) = '000' THEN CONCAT(CONCAT(T2.YAKUBA_ATENA,'　'),'知事') ");
            stb.append("          ELSE CONCAT(CONCAT(CONCAT(T2.YAKUBA_ATENA,'　'),RIGHT(T2.YAKUBA_NAME,3)),'長') ");
            stb.append("     END AS YAKUBA_ATENA3, ");
            stb.append("     CONCAT(CONCAT(V2.FAMILY_NAME_KANA,' '),V2.FIRST_NAME_KANA) AS NAME_KANA, ");
            stb.append("     CONCAT(CONCAT(V2.FAMILY_NAME,'　'),V2.FIRST_NAME) AS NAME_KANJI, ");
            stb.append("     V2.BIRTHDAY, ");
            stb.append("     CASE WHEN S1.ADDR1 IS NOT NULL THEN S1.ADDR1 ");
            stb.append("          WHEN S2.ADDR1 IS NOT NULL THEN S2.ADDR1 ");
            stb.append("          ELSE V2.ADDR1 ");
            stb.append("     END AS ADDR1, ");
            stb.append("     CASE WHEN S1.ADDR2 IS NOT NULL THEN S1.ADDR2 ");
            stb.append("          WHEN S2.ADDR2 IS NOT NULL THEN S2.ADDR2 ");
            stb.append("          ELSE V2.ADDR2 ");
            stb.append("     END AS ADDR2 ");
            stb.append(" FROM ");
            stb.append("     HENREI_DAT T1 ");
            stb.append("     LEFT JOIN V_KOJIN_SHUUGAKU_SHINSEI_HIST_DAT V1 ON T1.SHUUGAKU_NO = V1.SHUUGAKU_NO ");
            stb.append("     LEFT JOIN V_KOJIN_HIST_DAT V2 ON V1.KOJIN_NO = V2.KOJIN_NO ");
            stb.append("     LEFT JOIN SHINKENSHA_HIST_DAT V3 ON V1.RENTAI_CD = V3.SHINKEN_CD ");
            stb.append("     LEFT JOIN V_SAIKEN_JOKYO V4 ON V1.SHUUGAKU_NO = V4.SHUUGAKU_NO ");
            stb.append("     LEFT JOIN YAKUBA_DAT T2 ON V2.CITYCD = T2.YAKUBA_CD ");
            stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S1 ON V1.KOJIN_NO = S1.KOJIN_NO AND V1.SHUUGAKU_NO = S1.SHUUGAKU_NO ");
            stb.append("     LEFT JOIN SOUFU_ADDRESS_DAT S2 ON V1.KOJIN_NO = S2.KOJIN_NO AND S2.SHUUGAKU_NO = '9999999' ");
            stb.append("     LEFT JOIN YAKUBA_DAT T3 ON S1.CITY_CD = T3.YAKUBA_CD ");
            stb.append("     LEFT JOIN YAKUBA_DAT T4 ON S2.CITY_CD = T4.YAKUBA_CD ");
            stb.append(" WHERE ");
            stb.append("     (T1.SHUUGAKU_NO, T1.CHOTEI_KAISU, T1.HENREI_DATE, T1.HENREI_NAIYO) IN " + _param._arg + " ");
            stb.append(" ORDER BY ");
            stb.append("     T2.YAKUBA_CD, ");
            stb.append("     T1.SHUUGAKU_NO ");
            stb.append(" ) T1 ");
            stb.append(" ORDER BY ");
            stb.append("     YAKUBA_CD, ");
            stb.append("     SHUUGAKU_NO ");
        }
        return stb.toString();
    }
    
    private static class Yakuba {
        final String _yakubaCd;
        final List _henreiList = new ArrayList();
        Yakuba(final String yakubaCd) {
            _yakubaCd = yakubaCd;
        }
    }

    private static class Henrei {
        final String _zipCd;
        final String _yakubaAddr1;
        final String _yakubaAddr2;
        final String _yakubaAtena;
        final String _nameKana;
        final String _nameKanji;
        final String _birthday;
        final String _addr1;
        final String _addr2;

        Henrei(
                final String zipCd,
                final String yakubaAddr1,
                final String yakubaAddr2,
                final String yakubaAtena,
                final String nameKana,
                final String nameKanji,
                final String birthday,
                final String addr1,
                final String addr2
        ) {
            _zipCd = zipCd;
            _yakubaAddr1 = yakubaAddr1;
            _yakubaAddr2 = yakubaAddr2;
            _yakubaAtena = yakubaAtena;
            _nameKana = nameKana;
            _nameKanji = nameKanji;
            _birthday = birthday;
            _addr1 = addr1;
            _addr2 = addr2;
        }
        
        boolean hasDifference(final Henrei henrei) {
            if (null == henrei) {
                return false;
            }
            final boolean rtn = !StringUtils.defaultString(_zipCd).equals(StringUtils.defaultString(henrei._zipCd))
            || !StringUtils.defaultString(_yakubaAddr1).equals(StringUtils.defaultString(henrei._yakubaAddr1))
            || !StringUtils.defaultString(_yakubaAddr2).equals(StringUtils.defaultString(henrei._yakubaAddr2))
            || !StringUtils.defaultString(_yakubaAtena).equals(StringUtils.defaultString(henrei._yakubaAtena))
            || !StringUtils.defaultString(_nameKana).equals(StringUtils.defaultString(henrei._nameKana))
            || !StringUtils.defaultString(_nameKanji).equals(StringUtils.defaultString(henrei._nameKanji))
            || !StringUtils.defaultString(_birthday).equals(StringUtils.defaultString(henrei._birthday))
            || !StringUtils.defaultString(_addr1).equals(StringUtils.defaultString(henrei._addr1))
            || !StringUtils.defaultString(_addr2).equals(StringUtils.defaultString(henrei._addr2));
            return rtn;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67240 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _cmd;
        final String _addUkeG;
        final String _addUkeYY;
        final String _addUkeNo;
        final String _addUkeEdaban;
        final String _addTuchiDate;
        final String _arg;
        final ShugakuDate _shugakuDate;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _cmd = request.getParameter("cmd");
            if ("csvKojin1".equals(_cmd)) {
                _arg = request.getParameter("KOJIN_NO1"); // 個人番号指定
            } else if ("csvKojin2".equals(_cmd)) {
                _arg = request.getParameter("KOJIN_NO2"); // 個人番号指定
            } else {
                final String[] _chk = request.getParameterValues("CHK[]");
                final StringBuffer stbArg = new StringBuffer();
                stbArg.append("(");
                String union = "";
                for (int i = 0; i < _chk.length; i++) {
                    final String chk = _chk[i];
                    if (null == chk || chk.length() < 22) {
                        log.fatal("error parameter:" + chk);
                        continue;
                    }
                    stbArg.append(union);
                    // 修学生番号（7桁）＋回数（3桁）＋日付（日付形式で10桁）＋返戻内容（2桁）
                    final String shuugakuNo = chk.substring(0, 7); 
                    final String kaisu = chk.substring(7, 7 + 3);
                    final String date = chk.substring(10, 10 + 10);
                    final String henreiNaiyou = chk.substring(20, 20 + 2);
                    stbArg.append("VALUES('").append(shuugakuNo).append("', ").append(kaisu).append(", '").append(date).append("', '").append(henreiNaiyou).append("')");
                    union = " UNION ";
                }
                stbArg.append(")");
                _arg = stbArg.toString();
            }
            _addUkeG = request.getParameter("ADD_UKE_G");
            _addUkeYY = request.getParameter("ADD_UKE_YY");
            _addUkeNo = request.getParameter("ADD_UKE_NO");
            _addUkeEdaban = request.getParameter("ADD_UKE_EDABAN");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _addTuchiDate = _shugakuDate.d7toDateStr(request.getParameter("ADD_TUCHI_DATE"));
        }
        
        public String getBunshoBangou(final Param param) {
            try {
                final String wa = param._shugakuDate.getUkeYearNum(getAddUkeYear());
                final String bangou = (StringUtils.isBlank(_addUkeNo)) ? "" : String.valueOf(Integer.parseInt(_addUkeNo));
                final String edaban = (StringUtils.isBlank(_addUkeEdaban) || Integer.parseInt(_addUkeEdaban) == 1) ? "" : ("の" + Integer.parseInt(_addUkeEdaban));
                return wa + "教高第" + bangou + "号" + edaban;
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }
        
        private String getAddUkeYear() {
            final String nengoFlg = _addUkeG;
            final int nengoNen = Integer.parseInt(_addUkeYY);
            
            int nen = 1989; // default
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            try {
                for (final Iterator it = _shugakuDate._nameMstT001.iterator(); it.hasNext();) {
                    final Map m = (Map) it.next();
                    final String namecd2 = (String) m.get("NAMECD2");
                    if (namecd2.equals(nengoFlg)) {
                        final String abbv1 = (String) m.get("ABBV1");
                        if (null != abbv1) {
                            final Calendar dcal = Calendar.getInstance();
                            dcal.setTime(df.parse(abbv1.replace('/', '-')));
                            nen = dcal.get(Calendar.YEAR);
                        }
                        break;
                    }
                }
                return String.valueOf(nen + nengoNen - 1);
            } catch (Exception e) {
                log.error("format exception!", e);
            }
            return null;
        }
    }
}

// eof

