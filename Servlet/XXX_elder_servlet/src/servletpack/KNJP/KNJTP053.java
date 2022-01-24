/*
 * $Id: 5cd687a1f1909d83c7ce560043a5bb309184089d $
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.ShugakuDate;

/**
 * 京都府修学金 貸与予定通知書
 */
public class KNJTP053 {

    private static final Log log = LogFactory.getLog(KNJTP053.class);

    private boolean _hasData;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

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

        final List list = KojinTaiyoyoyakuHistDat.load(db2, _param);

        for (final Iterator taiyoIt = list.iterator(); taiyoIt.hasNext();) {
            final KojinTaiyoyoyakuHistDat taiyoyoyaku = (KojinTaiyoyoyakuHistDat) taiyoIt.next();

            printMain(svf, taiyoyoyaku);

            _hasData = true;
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

    private void VrsOutDate(final String field1, final String date, final Vrw32alp svf) {
        if (null == date) {
            return;
        }
        svf.VrsOut(field1, _param._shugakuDate.formatDate(date, false));
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

    private void printMain(final Vrw32alp svf, final KojinTaiyoyoyakuHistDat taiyoyoyaku) {
        svf.VrSetForm("KNJTP053.frm", 1);

        svf.VrsOut("ZIP_NO", taiyoyoyaku._zipcd);
        printAddress(svf, taiyoyoyaku._addr1, taiyoyoyaku._addr2);
//        svf.VrsOut("ADDRESS1", taiyoyoyaku._addr1);
//        VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(taiyoyoyaku._addr2, 40, 2), svf);

        svf.VrsOut("NAME", taiyoyoyaku.getName());

        svf.VrsOut("CERT_NO", taiyoyoyaku.getBunshoBangou(_param));
        VrsOutDate("DATE", taiyoyoyaku._ketteiDate, svf);

//        svf.VrsOut("GOVERNER", _param._chijiName);

        VrsOutDate("DAY1", taiyoyoyaku._yoyakuShinseiDate, svf);
        VrsOutDate("DAY2", _param._shinseishoDate, svf);
        svf.VrsOut("LOAN_APPLI_NO", taiyoyoyaku._shuugakuNo);

        svf.VrsOut("CHARGE", "高校教育課");
        svf.VrsOut("FIELD1", "(075)574-7518");

        svf.VrEndPage();
    }

    private static class KojinTaiyoyoyakuHistDat {
        final String _kojinNo;
        final String _shinseiYear;
        final String _shikinShousaiDiv;
        final String _ukeYear;
        final String _ukeNo;
        final String _ukeEdaban;
        final String _yoyakuShinseiDate;
        final String _shuugakuNo;
        final String _nenrei;
        final String _kikonFlg;
        final String _jSchoolCd;
        final String _jGradDiv;
        final String _jGradYm;
        final String _kibouHSchoolDiv;
        final String _yoyakuKibouGk;
        final String _sYoyakuKibouYm;
        final String _eYoyakuKibouYm;
        final String _shitakukinKibouFlg;
        final String _shinseiDiv;
        final String _rentaiCd;
        final String _shinken1Cd;
        final String _shinken2Cd;
        final String _shutaruCd;
        final String _shinseiKanryouFlg;
        final String _shinseiCancelFlg;
        final String _shinseiCancelDate;
        final String _ketteiDate;
        final String _ketteiFlg;
        final String _schoolName;

        List _taiyosetaiList = Collections.EMPTY_LIST;
        String _kojinFamilyName;
        String _kojinFirstName;
        String _kojinFamilyNameKana;
        String _kojinFirstNameKana;
        String _zipcd;
        String _addr1;
        String _addr2;
        String _telno1;
        String _telno2;

        KojinTaiyoyoyakuHistDat(
                final String kojinNo,
                final String shinseiYear,
                final String shikinShousaiDiv,
                final String ukeYear,
                final String ukeNo,
                final String ukeEdaban,
                final String yoyakuShinseiDate,
                final String shuugakuNo,
                final String nenrei,
                final String kikonFlg,
                final String jSchoolCd,
                final String jGradDiv,
                final String jGradYm,
                final String kibouHSchoolDiv,
                final String yoyakuKibouGk,
                final String sYoyakuKibouYm,
                final String eYoyakuKibouYm,
                final String shitakukinKibouFlg,
                final String shinseiDiv,
                final String rentaiCd,
                final String shinken1Cd,
                final String shinken2Cd,
                final String shutaruCd,
                final String shinseiKanryouFlg,
                final String shinseiCancelFlg,
                final String shinseiCancelDate,
                final String ketteiDate,
                final String ketteiFlg,
                final String schoolName
        ) {
            _kojinNo = kojinNo;
            _shinseiYear = shinseiYear;
            _shikinShousaiDiv = shikinShousaiDiv;
            _ukeYear = ukeYear;
            _ukeNo = ukeNo;
            _ukeEdaban = ukeEdaban;
            _yoyakuShinseiDate = yoyakuShinseiDate;
            _shuugakuNo = shuugakuNo;
            _nenrei = nenrei;
            _kikonFlg = kikonFlg;
            _jSchoolCd = jSchoolCd;
            _jGradDiv = jGradDiv;
            _jGradYm = jGradYm;
            _kibouHSchoolDiv = kibouHSchoolDiv;
            _yoyakuKibouGk = yoyakuKibouGk;
            _sYoyakuKibouYm = sYoyakuKibouYm;
            _eYoyakuKibouYm = eYoyakuKibouYm;
            _shitakukinKibouFlg = shitakukinKibouFlg;
            _shinseiDiv = shinseiDiv;
            _rentaiCd = rentaiCd;
            _shinken1Cd = shinken1Cd;
            _shinken2Cd = shinken2Cd;
            _shutaruCd = shutaruCd;
            _shinseiKanryouFlg = shinseiKanryouFlg;
            _shinseiCancelFlg = shinseiCancelFlg;
            _shinseiCancelDate = shinseiCancelDate;
            _ketteiDate = ketteiDate;
            _ketteiFlg = ketteiFlg;
            _schoolName = schoolName;
        }

        public String getBunshoBangou(final Param param) {
            try {
                final String wa = param._shugakuDate.getUkeYearNum(_ukeYear);
                final String bangou = (null ==_ukeNo) ? "" : String.valueOf(Integer.parseInt(_ukeNo));
                final String edaban = (null ==_ukeEdaban || Integer.parseInt(_ukeEdaban) == 1) ? "" : ("の" + Integer.parseInt(_ukeEdaban));
                return wa + "教高第" + bangou + "号" + edaban;
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return null;
        }

        public String getName() {
            return StringUtils.defaultString(_kojinFamilyName) + "　" + StringUtils.defaultString(_kojinFirstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_kojinFamilyNameKana) + "　" + StringUtils.defaultString(_kojinFirstNameKana);
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
                    final String shikinShousaiDiv = rs.getString("SHIKIN_SHOUSAI_DIV");
                    final String ukeYear = rs.getString("UKE_YEAR");
                    final String ukeNo = rs.getString("UKE_NO");
                    final String ukeEdaban = rs.getString("UKE_EDABAN");
                    final String yoyakuShinseiDate = rs.getString("YOYAKU_SHINSEI_DATE");
                    final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                    final String nenrei = rs.getString("NENREI");
                    final String kikonFlg = rs.getString("KIKON_FLG");
                    final String jSchoolCd = rs.getString("J_SCHOOL_CD");
                    final String jGradDiv = rs.getString("J_GRAD_DIV");
                    final String jGradYm = rs.getString("J_GRAD_YM");
                    final String kibouHSchoolDiv = rs.getString("KIBOU_H_SCHOOL_DIV");
                    final String yoyakuKibouGk = rs.getString("YOYAKU_KIBOU_GK");
                    final String sYoyakuKibouYm = rs.getString("S_YOYAKU_KIBOU_YM");
                    final String eYoyakuKibouYm = rs.getString("E_YOYAKU_KIBOU_YM");
                    final String shitakukinKibouFlg = rs.getString("SHITAKUKIN_KIBOU_FLG");
                    final String shinseiDiv = rs.getString("SHINSEI_DIV");
                    final String rentaiCd = rs.getString("RENTAI_CD");
                    final String shinken1Cd = rs.getString("SHINKEN1_CD");
                    final String shinken2Cd = rs.getString("SHINKEN2_CD");
                    final String shutaruCd = rs.getString("SHUTARU_CD");
                    final String shinseiKanryouFlg = rs.getString("SHINSEI_KANRYOU_FLG");
                    final String shinseiCancelFlg = rs.getString("SHINSEI_CANCEL_FLG");
                    final String shinseiCancelDate = rs.getString("SHINSEI_CANCEL_DATE");
                    final String ketteiDate = rs.getString("KETTEI_DATE");
                    final String ketteiFlg = rs.getString("KETTEI_FLG");
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final KojinTaiyoyoyakuHistDat kojintaiyoyoyakuhistdat = new KojinTaiyoyoyakuHistDat(kojinNo, shinseiYear, shikinShousaiDiv, ukeYear, ukeNo, ukeEdaban, yoyakuShinseiDate,
                            shuugakuNo, nenrei, kikonFlg, jSchoolCd, jGradDiv, jGradYm, kibouHSchoolDiv, yoyakuKibouGk, sYoyakuKibouYm, eYoyakuKibouYm, shitakukinKibouFlg, shinseiDiv, rentaiCd,
                            shinken1Cd, shinken2Cd, shutaruCd, shinseiKanryouFlg, shinseiCancelFlg, shinseiCancelDate, ketteiDate, ketteiFlg, schoolName);

                    list.add(kojintaiyoyoyakuhistdat);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (!list.isEmpty()) {
               setKojinHistDat(db2, param, list);
            }
            return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAIN AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.* ");
            stb.append("     FROM ");
            stb.append("         KOJIN_TAIYOYOYAKU_HIST_DAT T1 ");
            stb.append("     WHERE ");
            if ("1".equals(param._classDiv)) {
                stb.append("         T1.SHINSEI_YEAR = '" + param._shinseiYear + "' ");
                stb.append("         AND T1.SHUUGAKU_NO = '" + param._shuugakuNo + "'");
            } else if ("2".equals(param._classDiv)) {
                stb.append("         T1.SHINSEI_YEAR || '-' || T1.UKE_YEAR || '-' || T1.UKE_NO || '-' || T1.UKE_EDABAN = '" + param._uke + "' ");
                stb.append("         AND T1.J_SCHOOL_CD IN " + SQLUtils.whereIn(true, param._schoolSelected));
            }
            stb.append("         AND T1.SHIKIN_SHOUSAI_DIV = '01' ");
            stb.append("         AND T1.SHINSEI_CANCEL_FLG IS NULL ");
            stb.append("         AND T1.SHUUGAKU_NO IS NOT NULL ");
            stb.append("         AND T1.KETTEI_FLG = '1' ");
            stb.append("         AND T1.KETTEI_DATE IS NOT NULL ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   L3.NAME AS SCHOOL_NAME ");
            stb.append(" FROM MAIN T1 ");
            stb.append(" LEFT JOIN SCHOOL_DAT L3 ON L3.SCHOOLCD = T1.J_SCHOOL_CD ");
            stb.append(" ORDER BY ");
            stb.append("   T1.J_SCHOOL_CD, T1.SHUUGAKU_NO ");
            return stb.toString();
        }

        private static void setKojinHistDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set kojinNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinTaiyoyoyakuHistDat taiyoyoyaku = (KojinTaiyoyoyakuHistDat) it.next();
                kojinNos.add(taiyoyoyaku._kojinNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map nameMap = new HashMap();
            try {
                final String sql = sqlKojinHistDat(kojinNos);
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
                final KojinTaiyoyoyakuHistDat shinsei = (KojinTaiyoyoyakuHistDat) it.next();
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
            stb.append("     WHERE KOJIN_NO IN " + SQLUtils.whereIn(true, toArray(kojinNos)) + " ");
            stb.append("     GROUP BY KOJIN_NO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM KOJIN_HIST_DAT T1  ");
            stb.append(" INNER JOIN MAX_HIST T2 ON T2.KOJIN_NO = T1.KOJIN_NO AND T2.ISSUEDATE = T1.ISSUEDATE ");
            return stb.toString();
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
        log.fatal("$Revision: 67227 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _shinseiYear;
        private final String _loginDate;
        private final String _classDiv;
        private final String[] _schoolSelected;
        private final String _shuugakuNo;
        private final String _shinseishoDateD7;
        private final String _shinseishoDate;
        private final String _uke;
        private final String _chijiName;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiYear = request.getParameter("SHINSEI_YEAR");
            _loginDate = request.getParameter("LOGIN_DATE");
            _classDiv = request.getParameter("CLASS_DIV");
            _shinseishoDateD7 = request.getParameter("SHINSEISHO_DATE");
            _uke = request.getParameter("UKE");  // parameter UKE = SHINSEI_YEAR - UKE_YEAR - UKE_NO - UKE_EDABAN
            _schoolSelected = request.getParameterValues("SCHOOL_SELECTED");
            _shuugakuNo = request.getParameter("SHUUGAKU_NO");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _shinseishoDate = _shugakuDate.d7toDateStr(_shinseishoDateD7);
            _chijiName = _shugakuDate.getChijiName(db2);
        }
    }
}

// eof

