/*
 * $Id: 04f2fd1e590ac673c6d676985f2164ef86b84c4c $
 *
 * 作成日: 2012/09/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
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
 * 京都府修学金 特別融資利子補給金交付指令書
 */
public class KNJTP078 {

    private static final Log log = LogFactory.getLog(KNJTP078.class);

    private boolean _hasData;
    private NumberFormat _currencyFormat = new DecimalFormat("#,##0");

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List list = Kojin.load(db2, _param);

        for (int i = 0; i < list.size(); i++) {
            final Kojin shinsei = (Kojin) list.get(i);

            printMain78(svf, shinsei);
            _hasData = true;
        }
    }

    private void printMain78(final Vrw32alp svf, final Kojin kojin) {
        svf.VrSetForm("KNJTP078.frm", 1);

        final KojinRishiKoufuShinseiDat koufu = kojin._rishiKoufuShinsei;
        if (null != koufu) {
            svf.VrsOut("CERTIF_NO", koufu.getBunshoBangou(_param));
            if (null != koufu._shutaru) {
                svf.VrsOut("ZIP_NO", koufu._shutaru._zipcd);

                int lines1 = 0, lines2 = 0;
                final String[] token1 = KNJ_EditEdit.get_token(koufu._shutaru._addr1, 40, 2);
                if (null != token1) { for (int j = 0; j < token1.length; j++) { if (!StringUtils.isBlank(token1[j])) lines1 += 1; } }
                final String[] token2 = KNJ_EditEdit.get_token(koufu._shutaru._addr2, 40, 2);
                if (null != token2) { for (int j = 0; j < token2.length; j++) { if (!StringUtils.isBlank(token2[j])) lines2 += 1; } }
                if (lines1 <= 1 && lines2 <= 2) {
                    svf.VrsOut("ADDRESS1", koufu._shutaru._addr1);
                    VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(koufu._shutaru._addr2, 40, 2), svf);
                } else {
                    final String[] addr1 = KNJ_EditEdit.get_token(koufu._shutaru._addr1, 50, 2);
                    final String[] addr2 = KNJ_EditEdit.get_token(koufu._shutaru._addr2, 50, 2);
                    final List addr = new ArrayList();
                    if (null != addr1 && !StringUtils.isBlank(addr1[0])) addr.add(addr1[0]);
                    if (null != addr1 && !StringUtils.isBlank(addr1[1])) addr.add(addr1[1]);
                    if (null != addr2 && !StringUtils.isBlank(addr2[0])) addr.add(addr2[0]);
                    if (null != addr2 && !StringUtils.isBlank(addr2[1])) addr.add(addr2[1]);
                    final String[] fieldsNo = new String[] {"1_2", "1_3", "2_2", "2_3"};
                    for (int j = 0; j < addr.size(); j++) {
                        svf.VrsOut("ADDRESSS" + fieldsNo[j], (String) addr.get(j));
                    }
                }
                svf.VrsOut("NAME", koufu._shutaru.getName());
            }
            svf.VrsOut("DAY1", _param._shugakuDate.formatDate(koufu._shinseiDate, false));
            String shienShitaku = "";
            if ("1".equals(_param._shinseiDiv)) {
                shienShitaku = "支援";
            } else if ("2".equals(_param._shinseiDiv)) {
                shienShitaku = "支度金";
            }
            svf.VrsOut("TEXT1", "付けで申請の京都府修学" + shienShitaku + "特別融資利子補給金については、京 ");
            final String kingakuStr = !NumberUtils.isDigits(koufu._koufuShinseiGk) ? "" : _currencyFormat.format(Long.parseLong(koufu._koufuShinseiGk));
            final String[] tokens = getTokens("都府高校生等修学支援のための特別融資利子補給金交付要綱に基づき、次の条件を付けて、" + kingakuStr  + "円を", 78, 2);
            svf.VrsOut("TEXT2", tokens[0]);
            svf.VrsOut("TEXT4", StringUtils.defaultString(tokens[1]) + "交付するとともに確定します。");

            svf.VrsOut("FIELD1", _param._shugakuDate.formatDate(koufu._koufuKetteiDate, false));
//            svf.VrsOut("GOVERNER", _param._chijiName);
        }
        svf.VrEndPage();

        svf.VrSetForm("KNJTP078_2.frm", 1);
        if (null != koufu && null != koufu._shutaru) {
            svf.VrsOut("ZIPNO", koufu._shutaru._zipcd);

            int lines1 = 0, lines2 = 0;
            final String[] token1 = KNJ_EditEdit.get_token(koufu._shutaru._addr1, 40, 2);
            if (null != token1) { for (int j = 0; j < token1.length; j++) { if (!StringUtils.isBlank(token1[j])) lines1 += 1; } }
            final String[] token2 = KNJ_EditEdit.get_token(koufu._shutaru._addr2, 40, 2);
            if (null != token2) { for (int j = 0; j < token2.length; j++) { if (!StringUtils.isBlank(token2[j])) lines2 += 1; } }
            if (lines1 <= 1 && lines2 <= 1) {
                svf.VrsOut("ADDRESS1", koufu._shutaru._addr1);
                svf.VrsOut("ADDRESS2", koufu._shutaru._addr2);
            } else {
                final String[] addr1 = KNJ_EditEdit.get_token(koufu._shutaru._addr1, 50, 2);
                final String[] addr2 = KNJ_EditEdit.get_token(koufu._shutaru._addr2, 50, 2);
                final List addr = new ArrayList();
                if (null != addr1 && !StringUtils.isBlank(addr1[0])) addr.add(addr1[0]);
                if (null != addr1 && !StringUtils.isBlank(addr1[1])) addr.add(addr1[1]);
                if (null != addr2 && !StringUtils.isBlank(addr2[0])) addr.add(addr2[0]);
                if (null != addr2 && !StringUtils.isBlank(addr2[1])) addr.add(addr2[1]);
                final String[] fieldsNo = new String[] {"1_2", "1_3", "2_2", "2_3"};
                for (int j = 0; j < addr.size(); j++) {
                    svf.VrsOut("ADDRESSS" + fieldsNo[j], (String) addr.get(j));
                }
            }
            svf.VrsOut("NAME1", koufu._shutaru.getName());
        }
        svf.VrsOut("PNO", kojin._shuugakuNo);
        svf.VrEndPage();
    }

    private String[] getTokens(final String text, final int linebytemax, final int lines) {
        String[] tokens = new String[lines];
        for (int i = 0; i < lines; i++) {
            tokens[i] = "";
        }
        int cti = 0;
        int linebyteacc = 0;
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            final String ch = String.valueOf(text.charAt(i));
            if (linebyteacc + getMS932Length(ch) > linebytemax) {
                tokens[cti] = stb.toString();
                cti++;
                linebyteacc = 0;
                stb = new StringBuffer();
            }
            stb.append(ch);
            linebyteacc += getMS932Length(ch);
        }
        if (stb.length() != 0) {
            tokens[cti] = stb.toString();
        }
        return tokens;
    }

    private static class Kojin implements Comparable {
        final Param _param;
        final String _kojinNo;
        final String _shinseiYear;
        final String _shuugakuNo;

        final String _koufuSeq;

        KojinRishiKoufuShinseiDat _rishiKoufuShinsei = null;

        String _kojinFamilyName;
        String _kojinFirstName;
        String _kojinFamilyNameKana;
        String _kojinFirstNameKana;
        String _zipcd;
        String _addr1;
        String _addr2;
        String _telno1;
        String _telno2;

        Kojin(
                final Param param,
                final String kojinNo,
                final String shinseiYear,
                final String shuugakuNo,
                final String koufuSeq
        ) {
            _param = param;
            _kojinNo = kojinNo;
            _shinseiYear = shinseiYear;
            _shuugakuNo = shuugakuNo;

            _koufuSeq = koufuSeq;
        }

        public String getName() {
            return StringUtils.defaultString(_kojinFamilyName) + "　" +  StringUtils.defaultString(_kojinFirstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_kojinFamilyNameKana) + "　" +  StringUtils.defaultString(_kojinFirstNameKana);
        }

        public int compareTo(final Object o0) {
            final Kojin o = (Kojin) o0;
            int rtn;
            if ("2".equals(_param._output)) {
                rtn = _kojinNo.compareTo(o._kojinNo);
            } else {
                rtn = _shuugakuNo.compareTo(o._shuugakuNo);
            }
            if (0 != rtn) {
                return rtn;
            }
            rtn = _shinseiYear.compareTo(o._shinseiYear);
            if (0 != rtn) {
                return rtn;
            }
            return _koufuSeq.compareTo(o._koufuSeq);
        }

        public static List load(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List list = new ArrayList();
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                     final String kojinNo = rs.getString("KOJIN_NO");
                     final String shinseiYear = rs.getString("SHINSEI_YEAR");
                     final String shuugakuNo = rs.getString("SHUUGAKU_NO");

                     final String koufuSeq = rs.getString("KOUFU_SEQ");

                     final Kojin shinsei = new Kojin(param, kojinNo, shinseiYear, shuugakuNo, koufuSeq);
                     list.add(shinsei);
                 }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
           } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
           }
           if (!list.isEmpty()) {
               setKojinHistDat(db2, param, list);
               setKojinRishuKoufuShinseiDat(db2, param, list);
               Collections.sort(list);
           }

           return list;
        }

        private static void setKojinRishuKoufuShinseiDat(final DB2UDB db2, final Param param, final List list) {
            final Collection keys = new HashSet();
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Kojin shinsei = (Kojin) it.next();
                final String key = shinsei._shuugakuNo + "-" + shinsei._shinseiYear + "-" + shinsei._koufuSeq;
                keys.add(key);
            }
            final Map m = KojinRishiKoufuShinseiDat.load(db2, param, keys);
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Kojin shinsei = (Kojin) it.next();
                final String key = shinsei._shuugakuNo + "-" + shinsei._shinseiYear + "-" + shinsei._koufuSeq;
                if (null != m.get(key)) {
                    shinsei._rishiKoufuShinsei = (KojinRishiKoufuShinseiDat) m.get(key);
                }
            }
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT ");
            stb.append("         T1.*, ");
            stb.append("         T2.KOUFU_SEQ, ");
            stb.append("         T2.SHIKIN_SHUBETSU ");
            stb.append("     FROM ");
            stb.append("         KOJIN_RISHI_KOUFU_SHINSEI_DAT T2 ");
            stb.append("         INNER JOIN KOJIN_SHINSEI_HIST_DAT T1 ON T2.KOJIN_NO = T1.KOJIN_NO ");
            stb.append("           AND T2.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
            stb.append("           AND T2.SHINSEI_YEAR = T1.SHINSEI_YEAR ");
            stb.append("     WHERE ");
            stb.append("         T2.S_SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            if ("1".equals(param._shinseiDiv)) {
                stb.append("         AND T1.SHIKIN_SHOUSAI_DIV = '07' AND T2.SHIKIN_SHUBETSU = '1' ");
            } else if ("2".equals(param._shinseiDiv)) {
                stb.append("         AND T1.SHIKIN_SHOUSAI_DIV = '04' AND T2.SHIKIN_SHUBETSU = '2'  ");
            }
            if ("2".equals(param._classDiv)) {
                stb.append("         AND T1.H_SCHOOL_CD IN " + SQLUtils.whereIn(true, param._schoolSelected));
            } else if ("1".equals(param._classDiv)) {
                stb.append("         AND T1.SHUUGAKU_NO = '" + param._shugakuNo + "' ");
            }
            stb.append("         AND T1.SHINSEI_CANCEL_FLG IS NULL ");
            stb.append("         AND T1.SHUUGAKU_NO IS NOT NULL ");
            stb.append("         AND T1.KETTEI_FLG = '1' ");
            stb.append("         AND T1.KETTEI_DATE IS NOT NULL ");
            stb.append("         AND T2.KOUFU_KETTEI_DATE = '" + param._koufuDate + "' ");
            return stb.toString();
        }

        private static void setKojinHistDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set kojinNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final Kojin shinsei = (Kojin) it.next();
                kojinNos.add(shinsei._kojinNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map nameMap = new HashMap();
            try {
                final String sql = sqlKojinHistDat(kojinNos);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    final Map m = new HashMap();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        final String columnName = meta.getColumnLabel(i);
                        m.put(columnName, rs.getString(columnName));
                    }
                    nameMap.put(rs.getString("KOJIN_NO"), m);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final Kojin shinsei = (Kojin) it.next();
                final Map name = (Map) nameMap.get(shinsei._kojinNo);
                if (null != name) {
                    shinsei._kojinFamilyName = (String) name.get("FAMILY_NAME");
                    shinsei._kojinFirstName = (String) name.get("FIRST_NAME");
                    shinsei._kojinFamilyNameKana = (String) name.get("FAMILY_NAME_KANA");
                    shinsei._kojinFirstNameKana = (String) name.get("FIRST_NAME_KANA");

                    shinsei._zipcd = (String) name.get("ZIPCD");
                    shinsei._addr1 = (String) name.get("ADDR1");
                    shinsei._addr2 = (String) name.get("ADDR2");
                    shinsei._telno1 = (String) name.get("TELNO1");
                    shinsei._telno2 = (String) name.get("TELNO2");
                }
            }
        }

        private static String sqlKojinHistDat(final Collection kojinNos) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_HIST AS ( ");
            stb.append("     SELECT KOJIN_NO, MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("     FROM KOJIN_HIST_DAT  ");
            stb.append("     WHERE KOJIN_NO IN " + SQLUtils.whereIn(true, toArray(kojinNos)));
            stb.append("     GROUP BY KOJIN_NO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM KOJIN_HIST_DAT T1  ");
            stb.append(" INNER JOIN MAX_HIST T2 ON T2.KOJIN_NO = T1.KOJIN_NO AND T2.ISSUEDATE = T1.ISSUEDATE ");
            return stb.toString();
        }
    }

    private static class KojinRishiKoufuShinseiDat {
        final String _shuugakuNo;
        final String _shinseiYear;
        final String _koufuSeq;
        final String _shikinShubetsu;
        final String _shikinShubetsuName;
        final String _kojinNo;
        final String _sShinseiYear;
        final String _shutaruCd;
        final String _ukeYear;
        final String _ukeNo;
        final String _ukeEdaban;
        final String _shinseiDate;
        final String _yuushiCourseDiv;
        final String _kariireBankcd;
        final String _kariireGk;
        final String _kariireRitsu;
        final String _kariireDate;
        final String _sRishishiharaiDate;
        final String _eRishishiharaiDate;
        final String _remark;
        final String _koufuShinseiGk;
        final String _koufuShoriGk;
        final String _koufuKetteiDate;
        final String _koufuStatusFlg;
        final String _furikomiDate;
        final String _furikomiGk;

        ShinkenshaHistDat _shutaru = null;
        KojinRishiShousaiDat _rishiShousai = null;

        KojinRishiKoufuShinseiDat(
                final String shugakuNo,
                final String shinseiYear,
                final String koufuSeq,
                final String shikinShubetsu,
                final String shikinShubetsuName,
                final String kojinNo,
                final String sShinseiYear,
                final String shutaruCd,
                final String ukeYear,
                final String ukeNo,
                final String ukeEdaban,
                final String shinseiDate,
                final String yuushiCourseDiv,
                final String kariireBankcd,
                final String kariireGk,
                final String kariireRitsu,
                final String kariireDate,
                final String sRishishiharaiDate,
                final String eRishishiharaiDate,
                final String remark,
                final String koufuShinseiGk,
                final String koufuShoriGk,
                final String koufuKetteiDate,
                final String koufuStatusFlg,
                final String furikomiDate,
                final String furikomiGk
        ) {
            _shuugakuNo = shugakuNo;
            _shinseiYear = shinseiYear;
            _koufuSeq = koufuSeq;
            _shikinShubetsu = shikinShubetsu;
            _shikinShubetsuName = shikinShubetsuName;
            _kojinNo = kojinNo;
            _sShinseiYear = sShinseiYear;
            _shutaruCd = shutaruCd;
            _ukeYear = ukeYear;
            _ukeNo = ukeNo;
            _ukeEdaban = ukeEdaban;
            _shinseiDate = shinseiDate;
            _yuushiCourseDiv = yuushiCourseDiv;
            _kariireBankcd = kariireBankcd;
            _kariireGk = kariireGk;
            _kariireRitsu = kariireRitsu;
            _kariireDate = kariireDate;
            _sRishishiharaiDate = sRishishiharaiDate;
            _eRishishiharaiDate = eRishishiharaiDate;
            _remark = remark;
            _koufuShinseiGk = koufuShinseiGk;
            _koufuShoriGk = koufuShoriGk;
            _koufuKetteiDate = koufuKetteiDate;
            _koufuStatusFlg = koufuStatusFlg;
            _furikomiDate = furikomiDate;
            _furikomiGk = furikomiGk;
        }

        public String getBunshoBangou(final Param param) {
            try {
                final String wa = param._shugakuDate.getUkeYearNum(_ukeYear);
                final String bangou = (null ==_ukeNo) ? "" : String.valueOf(Integer.parseInt(_ukeNo));
                final String edaban = (null ==_ukeEdaban || Integer.parseInt(_ukeEdaban) == 1) ? "" : ("の" + Integer.parseInt(_ukeEdaban));
                return "京都府指令" + wa + "教高第" + bangou + "号" + edaban;
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }

        public static Map load(final DB2UDB db2, final Param param, final Collection keys) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                 final String sql = sql(keys);
                 log.debug(" rishi koufu shinsei sql = " + sql);
                 ps = db2.prepareStatement(sql);
                 rs = ps.executeQuery();
                 while (rs.next()) {

                     final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                     final String shinseiYear = rs.getString("SHINSEI_YEAR");
                     final String koufuSeq = rs.getString("KOUFU_SEQ");
                     final String shikinShubetsu = rs.getString("SHIKIN_SHUBETSU");
                     final String shikinShubetsuName = rs.getString("SHIKIN_SHUBETSU_NAME");
                     final String kojinNo = rs.getString("KOJIN_NO");
                     final String sShinseiYear = rs.getString("S_SHINSEI_YEAR");
                     final String shutaruCd = rs.getString("SHUTARU_CD");
                     final String ukeYear = rs.getString("UKE_YEAR");
                     final String ukeNo = rs.getString("UKE_NO");
                     final String ukeEdaban = rs.getString("UKE_EDABAN");
                     final String shinseiDate = rs.getString("SHINSEI_DATE");
                     final String yuushiCourseDiv = rs.getString("YUUSHI_COURSE_DIV");
                     final String kariireBankcd = rs.getString("KARIIRE_BANKCD");
                     final String kariireGk = rs.getString("KARIIRE_GK");
                     final String kariireRitsu = rs.getString("KARIIRE_RITSU");
                     final String kariireDate = rs.getString("KARIIRE_DATE");
                     final String sRishishiharaiDate = rs.getString("S_RISHISHIHARAI_DATE");
                     final String eRishishiharaiDate = rs.getString("E_RISHISHIHARAI_DATE");
                     final String remark = rs.getString("REMARK");
                     final String koufuShinseiGk = rs.getString("KOUFU_SHINSEI_GK");
                     final String koufuShoriGk = rs.getString("KOUFU_SHORI_GK");
                     final String koufuKetteiDate = rs.getString("KOUFU_KETTEI_DATE");
                     final String koufuStatusFlg = rs.getString("KOUFU_STATUS_FLG");
                     final String furikomiDate = rs.getString("FURIKOMI_DATE");
                     final String furikomiGk = rs.getString("FURIKOMI_GK");
                     final KojinRishiKoufuShinseiDat kojinrishikoufushinseidat = new KojinRishiKoufuShinseiDat(shuugakuNo, shinseiYear, koufuSeq, shikinShubetsu, shikinShubetsuName, kojinNo, sShinseiYear, shutaruCd, ukeYear, ukeNo, ukeEdaban, shinseiDate, yuushiCourseDiv, kariireBankcd, kariireGk, kariireRitsu, kariireDate, sRishishiharaiDate, eRishishiharaiDate, remark, koufuShinseiGk, koufuShoriGk, koufuKetteiDate, koufuStatusFlg, furikomiDate, furikomiGk);

                     final String key = shuugakuNo + "-" + shinseiYear + "-" + koufuSeq;
                     map.put(key, kojinrishikoufushinseidat);
                 }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (!map.isEmpty()) {
                setShinkenshaHistDat(db2, param, map);
                setRishiShousaiDat(db2, param, map);
            }
            return map;
        }

        private static void setRishiShousaiDat(final DB2UDB db2, final Param param, final Map map) {
            final Collection keyss = new HashSet();
            for (final Iterator it = map.values().iterator(); it.hasNext();) {
                final KojinRishiKoufuShinseiDat rishiKoufu = (KojinRishiKoufuShinseiDat) it.next();
                final String key = rishiKoufu._shuugakuNo + "-" + rishiKoufu._shinseiYear + "-" + rishiKoufu._koufuSeq;
                log.debug(" shousai key = " + key);
                keyss.add(key);
            }
            final Map rishiShousaiDat = KojinRishiShousaiDat.load(db2, param, keyss);
            for (final Iterator it = map.values().iterator(); it.hasNext();) {
                final KojinRishiKoufuShinseiDat rishiKoufu = (KojinRishiKoufuShinseiDat) it.next();
                final String key = rishiKoufu._shuugakuNo + "-" + rishiKoufu._shinseiYear + "-" + rishiKoufu._koufuSeq;
                rishiKoufu._rishiShousai = (KojinRishiShousaiDat) rishiShousaiDat.get(key);
            }
        }

        private static void setShinkenshaHistDat(final DB2UDB db2, final Param param, final Map map) {
            final Collection shinkenCds = new HashSet();
            for (final Iterator it = map.values().iterator(); it.hasNext();) {
                final KojinRishiKoufuShinseiDat rishiKoufu = (KojinRishiKoufuShinseiDat) it.next();
                if (null != rishiKoufu._shutaruCd) {
                    shinkenCds.add(rishiKoufu._shutaruCd);
                }
            }
            final Map shinkenshaHists = ShinkenshaHistDat.load(db2, shinkenCds);
            for (final Iterator it = map.values().iterator(); it.hasNext();) {
                final KojinRishiKoufuShinseiDat rishiKoufu = (KojinRishiKoufuShinseiDat) it.next();
                if (null != rishiKoufu._shutaruCd) {
                    rishiKoufu._shutaru = (ShinkenshaHistDat) shinkenshaHists.get(rishiKoufu._shutaruCd);
                }
            }
        }

        public static String sql(final Collection keys) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     T2.FURIKOMI_DATE, ");
            stb.append("     T2.FURIKOMI_GK, ");
            stb.append("     NMT008.NAME2 AS SHIKIN_SHUBETSU_NAME ");
            stb.append(" FROM KOJIN_RISHI_KOUFU_SHINSEI_DAT T1 ");
            stb.append(" LEFT JOIN KOJIN_RISHI_FURIKOMI_DAT T2 ON T2.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
            stb.append("     AND T2.SHINSEI_YEAR = T1.SHINSEI_YEAR ");
            stb.append("     AND T2.KOUFU_SEQ = T1.KOUFU_SEQ ");
            stb.append(" LEFT JOIN NAME_MST NMT008 ON NMT008.NAMECD1 = 'T008' ");
            stb.append("     AND NMT008.NAMECD2 = T1.SHIKIN_SHUBETSU ");
            stb.append(" WHERE ");
            stb.append("      T1.SHUUGAKU_NO || '-' || T1.SHINSEI_YEAR || '-' || T1.KOUFU_SEQ IN " + SQLUtils.whereIn(true, toArray(keys)) + " ");
            return stb.toString();
        }
    }

    private static class KojinRishiShousaiDat {
        final String _shuugakuNo;
        final String _shinseiYear;
        final String _koufuSeq;
        final String _shikinShubetsu;
        final String _henkanzumiDate;
        final String _rishiGk;
        final String _kariireZanGkMin;
        final String _kariireZanGkMax;
        final String _sKeisanDate;
        final String _eKeisanDate;
        final String _keisanNisuu;

        KojinRishiShousaiDat(
                final String shuugakuNo,
                final String shinseiYear,
                final String koufuSeq,
                final String shikinShubetsu,
                final String henkanzumiDate,
                final String rishiGk,
                final String kariireZanGkMin,
                final String kariireZanGkMax,
                final String sKeisanDate,
                final String eKeisanDate,
                final String keisanNisuu
        ) {
            _shuugakuNo = shuugakuNo;
            _shinseiYear = shinseiYear;
            _koufuSeq = koufuSeq;
            _shikinShubetsu = shikinShubetsu;
            _henkanzumiDate = henkanzumiDate;
            _rishiGk = rishiGk;
            _kariireZanGkMin = kariireZanGkMin;
            _kariireZanGkMax = kariireZanGkMax;
            _sKeisanDate = sKeisanDate;
            _eKeisanDate = eKeisanDate;
            _keisanNisuu = keisanNisuu;
        }

        public static Map load(final DB2UDB db2, final Param param, final Collection keys) {
            final Map m = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                 final String sql = sql(keys);
                 log.debug(" rishi koufu shousai sql = " + sql);
                 ps = db2.prepareStatement(sql);
                 rs = ps.executeQuery();
                 while (rs.next()) {
                     final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                     final String shinseiYear = rs.getString("SHINSEI_YEAR");
                     final String koufuSeq = rs.getString("KOUFU_SEQ");
                     final String shikinShubetsu = rs.getString("SHIKIN_SHUBETSU");
                     final String henkanzumiDate = rs.getString("HENKANZUMI_DATE");
                     final String rishiGk = rs.getString("RISHI_GK");
                     final String kariireZanGkMin = rs.getString("KARIIRE_ZAN_GK_MIN");
                     final String kariireZanGkMax = rs.getString("KARIIRE_ZAN_GK_MAX");
                     final String sKeisanDate = rs.getString("S_KEISAN_DATE");
                     final String eKeisanDate = rs.getString("E_KEISAN_DATE");
                     final String keisanNisuu = rs.getString("KEISAN_NISUU");

                     final KojinRishiShousaiDat kojinrishishousaidat = new KojinRishiShousaiDat(shuugakuNo, shinseiYear, koufuSeq, shikinShubetsu, henkanzumiDate, rishiGk, kariireZanGkMin, kariireZanGkMax, sKeisanDate, eKeisanDate, keisanNisuu);
                     final String key = shuugakuNo + "-" + shinseiYear + "-" + koufuSeq;
                     m.put(key, kojinrishishousaidat);
                 }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            return m;
        }

        public static String sql(final Collection keys) {
            final StringBuffer stb = new StringBuffer();
            stb.append("  SELECT  ");
            stb.append("      T1.SHUUGAKU_NO, ");
            stb.append("      T1.SHINSEI_YEAR, ");
            stb.append("      T1.KOUFU_SEQ, ");
            stb.append("      MAX(SHIKIN_SHUBETSU) AS SHIKIN_SHUBETSU,  ");
            stb.append("      MAX(HENKANZUMI_DATE) AS HENKANZUMI_DATE,  ");
            stb.append("      SUM(VALUE(RISHI_GK, 0)) AS RISHI_GK,  ");
            stb.append("      MIN(VALUE(KARIIRE_ZAN_GK, 0)) AS KARIIRE_ZAN_GK_MIN,  ");
            stb.append("      MAX(VALUE(KARIIRE_ZAN_GK, 0)) AS KARIIRE_ZAN_GK_MAX,  ");
            stb.append("      MIN(S_KEISAN_DATE) AS S_KEISAN_DATE,  ");
            stb.append("      MAX(E_KEISAN_DATE) AS E_KEISAN_DATE,  ");
            stb.append("      SUM(INT(VALUE(KEISAN_NISUU, '0'))) AS KEISAN_NISUU  ");
            stb.append("  FROM KOJIN_RISHI_SHOUSAI_DAT T1 ");
            stb.append("  WHERE  ");
            stb.append("      T1.SHUUGAKU_NO || '-' || T1.SHINSEI_YEAR || '-' || T1.KOUFU_SEQ IN " + SQLUtils.whereIn(true, toArray(keys)) + " ");
            stb.append("  GROUP BY ");
            stb.append("      T1.SHUUGAKU_NO, T1.SHINSEI_YEAR, T1.KOUFU_SEQ");
            return stb.toString();
        }
    }

    private static class ShinkenshaHistDat {

        final String _shinkenCd;
        final String _issuedate;
        final String _familyName;
        final String _firstName;
        final String _familyNameKana;
        final String _firstNameKana;
        final String _birthday;
        final String _shinseiNenrei;
        final String _zipcd;
        final String _citycd;
        final String _addr1;
        final String _addr2;
        final String _telno1;
        final String _telno2;

        ShinkenshaHistDat(
                final String shinkenCd,
                final String issuedate,
                final String familyName,
                final String firstName,
                final String familyNameKana,
                final String firstNameKana,
                final String birthday,
                final String shinseiNenrei,
                final String zipcd,
                final String citycd,
                final String addr1,
                final String addr2,
                final String telno1,
                final String telno2
        ) {
            _shinkenCd = shinkenCd;
            _issuedate = issuedate;
            _familyName = familyName;
            _firstName = firstName;
            _familyNameKana = familyNameKana;
            _firstNameKana = firstNameKana;
            _birthday = birthday;
            _shinseiNenrei = shinseiNenrei;
            _zipcd = zipcd;
            _citycd = citycd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno1 = telno1;
            _telno2 = telno2;
        }

        public String getName() {
            return StringUtils.defaultString(_familyName) + "　" +  StringUtils.defaultString(_firstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_familyNameKana) + "　" +  StringUtils.defaultString(_firstNameKana);
        }

        public static Map load(final DB2UDB db2, final Collection shinkenCdSet) {
            Map map = new HashMap();
            if (shinkenCdSet.isEmpty()) {
                return map;
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                 ps = db2.prepareStatement(sql(shinkenCdSet));
                 rs = ps.executeQuery();
                 while (rs.next()) {
                     final String shinkenCd = rs.getString("SHINKEN_CD");
                     final String issuedate = rs.getString("ISSUEDATE");
                     final String familyName = rs.getString("FAMILY_NAME");
                     final String firstName = rs.getString("FIRST_NAME");
                     final String familyNameKana = rs.getString("FAMILY_NAME_KANA");
                     final String firstNameKana = rs.getString("FIRST_NAME_KANA");
                     final String birthday = rs.getString("BIRTHDAY");
                     final String shinseiNenrei = rs.getString("SHINSEI_NENREI");
                     final String zipcd = rs.getString("ZIPCD");
                     final String citycd = rs.getString("CITYCD");
                     final String addr1 = rs.getString("ADDR1");
                     final String addr2 = rs.getString("ADDR2");
                     final String telno1 = rs.getString("TELNO1");
                     final String telno2 = rs.getString("TELNO2");
                     ShinkenshaHistDat shinkensha = new ShinkenshaHistDat(shinkenCd, issuedate,familyName, firstName, familyNameKana, firstNameKana,
                             birthday, shinseiNenrei, zipcd, citycd, addr1, addr2, telno1, telno2);
                     map.put(shinkenCd, shinkensha);
                 }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            return map;
        }

        public static String sql(final Collection shinkenCdSet) {
            final StringBuffer stb = new StringBuffer();
//            stb.append(" WITH MAX_DATE AS ( ");
//            stb.append("   SELECT SHINKEN_CD, MAX(ISSUEDATE) AS ISSUEDATE ");
//            stb.append("   FROM SHINKENSHA_HIST_DAT ");
//            stb.append("   WHERE SHINKEN_CD IN " + SQLUtils.whereIn(true, toArray(shinkenCdSet)) + " ");
//            stb.append("   GROUP BY SHINKEN_CD ");
//            stb.append(" ) ");
            stb.append(" SELECT * ");
            stb.append(" FROM SHINKENSHA_HIST_DAT T1 ");
            stb.append("   WHERE SHINKEN_CD IN " + SQLUtils.whereIn(true, toArray(shinkenCdSet)) + " ");
//            stb.append(" INNER JOIN MAX_DATE T2 ON T2.SHINKEN_CD = T1.SHINKEN_CD AND T2.ISSUEDATE = T1.ISSUEDATE ");
            return stb.toString();
        }
    }

    private static int getMS932Length(final String s) {
        return KNJ_EditEdit.getMS932ByteLength(s);
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
        log.fatal("$Revision: 67232 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _shinseiYear;
        private final String _loginDate;
        private final String _shinseiDiv;
        private final String _classDiv;
        private final String[] _schoolSelected;
        private final String _shugakuNo;
        private final String _output;
        private final String _chijiName;
        private final ShugakuDate _shugakuDate;
        private final String _koufuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiYear = request.getParameter("SHINSEI_YEAR"); // パラメータはS_SHINSEI_YEAR
            _loginDate = request.getParameter("LOGIN_DATE");
            _shinseiDiv = request.getParameter("SHINSEI_DIV");
            _classDiv = request.getParameter("CLASS_DIV");
            _schoolSelected = request.getParameterValues("SCHOOL_SELECTED");
            _shugakuNo = request.getParameter("KOJIN_NO");
            _output = request.getParameter("OUTPUT");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _koufuDate = _shugakuDate.d7toDateStr(request.getParameter("KOUFU_DATE"));
            _chijiName = _shugakuDate.getChijiName(db2);
        }
    }
}

// eof

