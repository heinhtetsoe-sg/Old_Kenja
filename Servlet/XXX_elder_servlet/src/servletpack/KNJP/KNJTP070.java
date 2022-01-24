/*
 * $Id: 45fa9d7da081d4b62da7e4e5813696a65e00cf9d $
 *
 * 作成日: 2012/09/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
 * 京都府修学金 継続申請書
 */
public class KNJTP070 {

    private static final Log log = LogFactory.getLog(KNJTP070.class);

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

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List list = KojinShinseiHistDat.load(db2, _param);

        final List schoolList = new ArrayList();
        List shinseiList0 = null;
        String schoolcd = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
            if (null == schoolcd || !schoolcd.equals(shinsei._hSchoolCd)) {
                shinseiList0 = new ArrayList();
                schoolList.add(shinseiList0);
            }
            schoolcd = shinsei._hSchoolCd;
            shinseiList0.add(shinsei);
        }

        for (final Iterator it = schoolList.iterator(); it.hasNext();) {
            final List shinseiList = (List) it.next();

            for (final Iterator taiyoIt = shinseiList.iterator(); taiyoIt.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) taiyoIt.next();

                printShinseisho(svf, shinsei);

                printTokuyaku(svf, shinsei);

                _hasData = true;
            }
        }
    }

    private String getNenreiStr(final String nenrei) {
        return "   ( " + StringUtils.defaultString(nenrei, "  ") + " 歳)";
    }

    // 46桁, 56桁用に分割
    private String[] divideAddr(final String a1, final String a2) {
        int dividx1 = getdividx(a1, 46);
        int dividx2 = getdividx(a2, 56);
        final String[] rtn;
        if (-1 == dividx1 && -1 == dividx2) { // 両方それぞれのスペースに収まる
            rtn = new String[] {a1, a2};
        } else if (-1 < dividx1) { // 住所1がサイズオーバー
            rtn = new String[] {a1.substring(0, dividx1), a1.substring(dividx1) + StringUtils.defaultString(a2)};
        } else {
            // (-1 < dividx2) // 住所2がサイズオーバー
            final String total = StringUtils.defaultString(a1) + StringUtils.defaultString(a2);
            final int dividxt = getdividx(total, 46);
            rtn = new String[] {total.substring(0, dividxt), total.substring(dividxt)};
        }
        return rtn;
    }

    private int getdividx(final String a, final int limit) {
        int divi = -1;
        int bytesize = 0;
        for (int i = 0, len = a.length(); i < len; i++) {
            try {
                bytesize += String.valueOf(a.charAt(i)).getBytes("MS932").length;
            } catch (Exception e) {
                bytesize += 3;
            }
            if (bytesize > limit) {
                divi = i;
                break;
            }
        }
        return divi;
    }
    private void printShinseisho(final Vrw32alp svf, final KojinShinseiHistDat shinsei) {
        svf.VrSetForm("KNJTP070_1.frm", 1);

        svf.VrAttribute("SLASH1", "UnderLine=(0,3,5), Keta=8");

        svf.VrsOut("PNO", shinsei._shuugakuNo);
        svf.VrsOut("NO", shinsei._hSchoolCd);
        svf.VrsOut("KANA1", shinsei.getKana());
        svf.VrsOut("NAME1", shinsei.getName());
        svf.VrsOut("BIRTHDAY1", "　　　   年    月    日 (    歳)");

        svf.VrsOut("ZIPNO1", shinsei._zipcd);
        final String[] addr = divideAddr(StringUtils.defaultString(shinsei._addr1), StringUtils.defaultString(shinsei._addr2));
        for (int i = 0; i < addr.length; i++) {
            svf.VrsOut("ADDRESS1_" + (i + 1), addr[i]);
        }
        svf.VrsOut("TELNO1_1", shinsei._telno1);
        svf.VrsOut("TELNO1_2", shinsei._telno2);

        VrsOut(new String[] {"SCHOOL_NAME1", "SCHOOL_NAME2"}, KNJ_EditEdit.get_token(shinsei._schoolName, 20, 2), svf);
        VrsOut(new String[] {"COURSE_NAME1", "COURSE_NAME2"}, KNJ_EditEdit.get_token(shinsei._kateiName, 10, 2), svf);
        svf.VrsOut("COURSECODE_NAME", "");
        svf.VrsOut("GRADE", "");

        svf.VrsOut("LOAN_HOPE_MONEY", "");
        svf.VrsOut("LOAN_HOPE_TFDAY", _param._shugakuDate.formatNentuki(String.valueOf(Integer.parseInt(_param._shinseiYear) + 1) + "-04"));
        svf.VrsOut("LOAN_HOPE_TTDAY", _param._shugakuDate.formatNentuki(shinsei._eYoyakuKibouYm));
        svf.VrsOut("ANOTHER_SCHOLAR1", "");
        svf.VrsOut("ANOTHER_SCHOLAR2", "");

        if (null != shinsei._rentai) {
            svf.VrsOut("KANA2", shinsei._rentai.getKana());
            svf.VrsOut("NAME2", shinsei._rentai.getName());
            svf.VrsOut("BIRTHDAY2", "　　　   年    月    日 (    歳)");
            svf.VrsOut("ZIPNO2", shinsei._rentai._zipcd);
            final String[] raddr = divideAddr(StringUtils.defaultString(shinsei._rentai._addr1), StringUtils.defaultString(shinsei._rentai._addr2));
            for (int i = 0; i < raddr.length; i++) {
                svf.VrsOut("ADDRESS2_" + (i + 1), raddr[i]);
            }
            svf.VrsOut("TELNO2_1", shinsei._rentai._telno1);
            svf.VrsOut("TELNO2_2", shinsei._rentai._telno2);
        }

        int setaiC = 1;
        svf.VrsOut("STANDARD_DAY", "(" + _param._shugakuDate.formatDate(_param._shinseiYear + "-04-01", false) + "現在)");
        for (final Iterator it = shinsei._taiyosetaiList.iterator(); it.hasNext();) {
            final KojinTaiyoSetaiDat taiyosetai = (KojinTaiyoSetaiDat) it.next();
            svf.VrsOutn("NAME3", setaiC, taiyosetai.getName());
            svf.VrsOutn("RELATION", setaiC, taiyosetai._tsuzukigaraName);
            setaiC += 1;
        }

        svf.VrsOut("GOVERNER", "京都府知事　様");

        svf.VrEndPage();
    }

    private void printTokuyaku(final Vrw32alp svf, final KojinShinseiHistDat shinsei) {
        svf.VrSetForm("KNJTP070_2.frm", 1);
        svf.VrsOut("SCHOOL_CD", shinsei._hSchoolCd);
        svf.VrsOut("SCHOOL_NAME", shinsei._schoolName);
        svf.VrsOut("COURSE", shinsei._kateiName);
        if (!StringUtils.isBlank(shinsei.getName())) {
            svf.VrsOut("NAME1", shinsei.getName() + "　様");
        }
        svf.VrsOut("PNO", shinsei._shuugakuNo);

        // 支度金
        final String hyphen = "\u2015"; // ハイフン
        final DecimalFormat df = new DecimalFormat("#,###");
        final Map shikinShubetsu2Map = getMappedMap(shinsei._taiyoYdatShikinshubetsuShinseiyearShoritotalgkMap, "2");
        BigDecimal shishutsuShoriTotalGk2 = null;
        for (final Iterator it = shikinShubetsu2Map.values().iterator(); it.hasNext();) { // 1レコードのみのはず
            final String shishutsuShoriTotalGk = (String) it.next();
            if (NumberUtils.isNumber(shishutsuShoriTotalGk)) {
                if (null == shishutsuShoriTotalGk2) {
                    shishutsuShoriTotalGk2 = new BigDecimal(0);
                }
                shishutsuShoriTotalGk2 = shishutsuShoriTotalGk2.add(new BigDecimal(shishutsuShoriTotalGk)); // 合計しておく
            }
        }
        String ATTR_CENTER = "Hensyu=3"; // 中央
        String ATTR_RIGHT = "Hensyu=1"; // 右寄せ
        String attr;
        String val;
        if (null != shishutsuShoriTotalGk2) {
            attr = ATTR_RIGHT;
            val = toZenkaku(df.format(shishutsuShoriTotalGk2));
        } else {
            attr = ATTR_CENTER;
            val = hyphen;
        }
        svf.VrAttribute("SHITAKU", attr);
        svf.VrsOut("SHITAKU", val);

        // 修学金
        final int maxSize = 4;
        final TreeMap shikinShubetsu1Map = (TreeMap) getMappedMap(shinsei._taiyoYdatShikinshubetsuShinseiyearShoritotalgkMap, "1");
        final List shugakuYearList = new ArrayList(new TreeSet(shikinShubetsu1Map.keySet()));
        int startKaisu = 1;
        if (shugakuYearList.size() > maxSize) {
            final int delCount = shugakuYearList.size() - maxSize;
            for (int i = 0; i < delCount; i++) {
                shugakuYearList.remove(0);
            }
            startKaisu += delCount;
        }
        for (int i = 0; i < maxSize; i++) {
            svf.VrsOutn("SHUGAKU_KAISU", i + 1, toZenkaku(String.valueOf(((startKaisu + i) % 10))));
            attr = ATTR_CENTER;
            val = hyphen;
            if (i < shugakuYearList.size()) {
                final Integer year = (Integer) shugakuYearList.get(i);
                final String shishutsuShoriTotalGk = (String) shikinShubetsu1Map.get(year);
                if (NumberUtils.isNumber(shishutsuShoriTotalGk)) {
                    attr = ATTR_RIGHT;
                    val = toZenkaku(df.format(new BigDecimal(shishutsuShoriTotalGk)));
                }
            }
            svf.VrAttributen("SHUGAKU", i + 1, attr);
            svf.VrsOutn("SHUGAKU", i + 1, val);
        }
        svf.VrEndPage();
    }

    private String toZenkaku(final String num) {
        final StringBuffer stb = new StringBuffer();
        if (null != num) {
            for (int i = 0; i < num.length(); i++) {
                switch (num.charAt(i)) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    stb.append((char) ('０' + num.charAt(i) - '0'));
                    break;
                case ',':
                    stb.append('，');
                    break;
                default:
                    stb.append(num.charAt(i));
                }
            }
        }
        return stb.toString();
    }

    private static class KojinShinseiHistDat implements Comparable {
        final String _kojinNo;
        final String _shinseiYear;
        final String _shikinShousaiDiv;
        final String _issuedate;
        final String _ukeYear;
        final String _ukeNo;
        final String _ukeEdaban;
        final String _shinseiDate;
        final String _shuugakuNo;
        final String _nenrei;
        final String _yoyakuKibouGk;
        final String _sYoyakuKibouYm;
        final String _eYoyakuKibouYm;
        final String _sTaiyoYm;
        final String _eTaiyoYm;
        final String _shinseiDiv;
        final String _setaiSeq;
        final String _keizokuKaisuu;
        final String _heikyuuShougakuStatus1;
        final String _heikyuuShougakuRemark1;
        final String _heikyuuShougakuStatus2;
        final String _heikyuuShougakuRemark2;
        final String _heikyuuShougakuStatus3;
        final String _heikyuuShougakuRemark3;
        final String _shitakuCancelChokuFlg;
        final String _shitakuCancelRiFlg;
        final String _hSchoolCd;
        final String _schoolName;
        final String _katei;
        final String _kateiName;
        final String _grade;
        final String _entDate;
        final String _hGradYm;
        final String _shitakukinTaiyoDiv;
        final String _heikyuuShitakuStatus1;
        final String _heikyuuShitakuRemark1;
        final String _heikyuuShitakuStatus2;
        final String _heikyuuShitakuRemark2;
        final String _heikyuuShitakuStatus3;
        final String _heikyuuShitakuRemark3;
        final String _yuushiFail;
        final String _yuushiFailDiv;
        final String _yuushiFailRemark;
        final String _bankCd;
        final String _yuushiCourseDiv;
        final String _rentaiCd;
        final String _shinken1Cd;
        final String _shinken2Cd;
        final String _shutaruCd;
        final String _shinseiKanryouFlg;
        final String _shinseiCancelFlg;
        final String _ketteiDate;
        final String _ketteiFlg;
        final String _shuugakuMonthCount;
        final Map _taiyoYdatShikinshubetsuShinseiyearShoritotalgkMap = new TreeMap();

        ShinkenshaHistDat _rentai = null;
        List _taiyosetaiList = Collections.EMPTY_LIST;
        String _kojinFamilyName;
        String _kojinFirstName;
        String _kojinFamilyNameKana;
        String _kojinFirstNameKana;
        String _kojinBirthday;
        String _zipcd;
        String _addr1;
        String _addr2;
        String _telno1;
        String _telno2;

        KojinShinseiHistDat(
                final String kojinNo,
                final String shinseiYear,
                final String shikinShousaiDiv,
                final String issuedate,
                final String ukeYear,
                final String ukeNo,
                final String ukeEdaban,
                final String shinseiDate,
                final String shuugakuNo,
                final String nenrei,
                final String yoyakuKibouGk,
                final String sYoyakuKibouYm,
                final String eYoyakuKibouYm,
                final String sTaiyoYm,
                final String eTaiyoYm,
                final String shinseiDiv,
                final String setaiSeq,
                final String keizokuKaisuu,
                final String heikyuuShougakuStatus1,
                final String heikyuuShougakuRemark1,
                final String heikyuuShougakuStatus2,
                final String heikyuuShougakuRemark2,
                final String heikyuuShougakuStatus3,
                final String heikyuuShougakuRemark3,
                final String shitakuCancelChokuFlg,
                final String shitakuCancelRiFlg,
                final String hSchoolCd,
                final String schoolName,
                final String katei,
                final String kateiName,
                final String grade,
                final String entDate,
                final String hGradYm,
                final String shitakukinTaiyoDiv,
                final String heikyuuShitakuStatus1,
                final String heikyuuShitakuRemark1,
                final String heikyuuShitakuStatus2,
                final String heikyuuShitakuRemark2,
                final String heikyuuShitakuStatus3,
                final String heikyuuShitakuRemark3,
                final String yuushiFail,
                final String yuushiFailDiv,
                final String yuushiFailRemark,
                final String bankCd,
                final String yuushiCourseDiv,
                final String rentaiCd,
                final String shinken1Cd,
                final String shinken2Cd,
                final String shutaruCd,
                final String shinseiKanryouFlg,
                final String shinseiCancelFlg,
                final String ketteiDate,
                final String ketteiFlg,
                final String shuugakuMonthCount
        ) {
            _kojinNo = kojinNo;
            _shinseiYear = shinseiYear;
            _shikinShousaiDiv = shikinShousaiDiv;
            _issuedate = issuedate;
            _ukeYear = ukeYear;
            _ukeNo = ukeNo;
            _ukeEdaban = ukeEdaban;
            _shinseiDate = shinseiDate;
            _shuugakuNo = shuugakuNo;
            _nenrei = nenrei;
            _yoyakuKibouGk = yoyakuKibouGk;
            _sYoyakuKibouYm = sYoyakuKibouYm;
            _eYoyakuKibouYm = eYoyakuKibouYm;
            _sTaiyoYm = sTaiyoYm;
            _eTaiyoYm = eTaiyoYm;
            _shinseiDiv = shinseiDiv;
            _setaiSeq = setaiSeq;
            _keizokuKaisuu = keizokuKaisuu;
            _heikyuuShougakuStatus1 = heikyuuShougakuStatus1;
            _heikyuuShougakuRemark1 = heikyuuShougakuRemark1;
            _heikyuuShougakuStatus2 = heikyuuShougakuStatus2;
            _heikyuuShougakuRemark2 = heikyuuShougakuRemark2;
            _heikyuuShougakuStatus3 = heikyuuShougakuStatus3;
            _heikyuuShougakuRemark3 = heikyuuShougakuRemark3;
            _shitakuCancelChokuFlg = shitakuCancelChokuFlg;
            _shitakuCancelRiFlg = shitakuCancelRiFlg;
            _hSchoolCd = hSchoolCd;
            _schoolName = schoolName;
            _katei = katei;
            _kateiName = kateiName;
            _grade = grade;
            _entDate = entDate;
            _hGradYm = hGradYm;
            _shitakukinTaiyoDiv = shitakukinTaiyoDiv;
            _heikyuuShitakuStatus1 = heikyuuShitakuStatus1;
            _heikyuuShitakuRemark1 = heikyuuShitakuRemark1;
            _heikyuuShitakuStatus2 = heikyuuShitakuStatus2;
            _heikyuuShitakuRemark2 = heikyuuShitakuRemark2;
            _heikyuuShitakuStatus3 = heikyuuShitakuStatus3;
            _heikyuuShitakuRemark3 = heikyuuShitakuRemark3;
            _yuushiFail = yuushiFail;
            _yuushiFailDiv = yuushiFailDiv;
            _yuushiFailRemark = yuushiFailRemark;
            _bankCd = bankCd;
            _yuushiCourseDiv = yuushiCourseDiv;
            _rentaiCd = rentaiCd;
            _shinken1Cd = shinken1Cd;
            _shinken2Cd = shinken2Cd;
            _shutaruCd = shutaruCd;
            _shinseiKanryouFlg = shinseiKanryouFlg;
            _shinseiCancelFlg = shinseiCancelFlg;
            _ketteiDate = ketteiDate;
            _ketteiFlg = ketteiFlg;
            _shuugakuMonthCount = shuugakuMonthCount;
        }

        public String getName() {
            return StringUtils.defaultString(_kojinFamilyName) + "　" +  StringUtils.defaultString(_kojinFirstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_kojinFamilyNameKana) + "　" +  StringUtils.defaultString(_kojinFirstNameKana);
        }

        public int compareTo(final Object o0) {
            final KojinShinseiHistDat o = (KojinShinseiHistDat) o0;
            int cmp;
            cmp = _hSchoolCd.compareTo(o._hSchoolCd);
            if (0 != cmp) {
                return cmp;
            }
            cmp = null == _katei ? 1 : (null == o._katei) ? -1 : _katei.compareTo(o._katei);
            if (0 != cmp) {
                return cmp;
            }
            cmp = _shuugakuNo.compareTo(o._shuugakuNo);
            return cmp;
        }

        public static List load(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List list = new ArrayList();
            try {
                 ps = db2.prepareStatement(sql(param));
                 rs = ps.executeQuery();
                 while (rs.next()) {
                     final String kojinNo = rs.getString("KOJIN_NO");
                     final String shinseiYear = rs.getString("SHINSEI_YEAR");
                     final String shikinShousaiDiv = rs.getString("SHIKIN_SHOUSAI_DIV");
                     final String issuedate = rs.getString("ISSUEDATE");
                     final String ukeYear = rs.getString("UKE_YEAR");
                     final String ukeNo = rs.getString("UKE_NO");
                     final String ukeEdaban = rs.getString("UKE_EDABAN");
                     final String shinseiDate = rs.getString("SHINSEI_DATE");
                     final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                     final String nenrei = rs.getString("NENREI");
                     final String yoyakuKibouGk = rs.getString("YOYAKU_KIBOU_GK");
                     final String sYoyakuKibouYm = rs.getString("S_YOYAKU_KIBOU_YM");
                     final String eYoyakuKibouYm = rs.getString("E_YOYAKU_KIBOU_YM");
                     final String sTaiyoYm = rs.getString("S_TAIYO_YM");
                     final String eTaiyoYm = rs.getString("E_TAIYO_YM");
                     final String shinseiDiv = rs.getString("SHINSEI_DIV");
                     final String setaiSeq = rs.getString("SETAI_SEQ");
                     final String keizokuKaisuu = rs.getString("KEIZOKU_KAISUU");
                     final String heikyuuShougakuStatus1 = rs.getString("HEIKYUU_SHOUGAKU_STATUS1");
                     final String heikyuuShougakuRemark1 = rs.getString("HEIKYUU_SHOUGAKU_REMARK1");
                     final String heikyuuShougakuStatus2 = rs.getString("HEIKYUU_SHOUGAKU_STATUS2");
                     final String heikyuuShougakuRemark2 = rs.getString("HEIKYUU_SHOUGAKU_REMARK2");
                     final String heikyuuShougakuStatus3 = rs.getString("HEIKYUU_SHOUGAKU_STATUS3");
                     final String heikyuuShougakuRemark3 = rs.getString("HEIKYUU_SHOUGAKU_REMARK3");
                     final String shitakuCancelChokuFlg = rs.getString("SHITAKU_CANCEL_CHOKU_FLG");
                     final String shitakuCancelRiFlg = rs.getString("SHITAKU_CANCEL_RI_FLG");
                     final String hSchoolCd = rs.getString("H_SCHOOL_CD");
                     final String schoolName = rs.getString("SCHOOL_NAME");
                     final String katei = rs.getString("KATEI");
                     final String kateiName = rs.getString("KATEI_NAME");
                     final String grade = rs.getString("GRADE");
                     final String entDate = rs.getString("ENT_DATE");
                     final String hGradYm = rs.getString("H_GRAD_YM");
                     final String shitakukinTaiyoDiv = rs.getString("SHITAKUKIN_TAIYO_DIV");
                     final String heikyuuShitakuStatus1 = rs.getString("HEIKYUU_SHITAKU_STATUS1");
                     final String heikyuuShitakuRemark1 = rs.getString("HEIKYUU_SHITAKU_REMARK1");
                     final String heikyuuShitakuStatus2 = rs.getString("HEIKYUU_SHITAKU_STATUS2");
                     final String heikyuuShitakuRemark2 = rs.getString("HEIKYUU_SHITAKU_REMARK2");
                     final String heikyuuShitakuStatus3 = rs.getString("HEIKYUU_SHITAKU_STATUS3");
                     final String heikyuuShitakuRemark3 = rs.getString("HEIKYUU_SHITAKU_REMARK3");
                     final String yuushiFail = rs.getString("YUUSHI_FAIL");
                     final String yuushiFailDiv = rs.getString("YUUSHI_FAIL_DIV");
                     final String yuushiFailRemark = rs.getString("YUUSHI_FAIL_REMARK");
                     final String bankCd = rs.getString("BANK_CD");
                     final String yuushiCourseDiv = rs.getString("YUUSHI_COURSE_DIV");
                     final String rentaiCd = rs.getString("RENTAI_CD");
                     final String shinken1Cd = rs.getString("SHINKEN1_CD");
                     final String shinken2Cd = rs.getString("SHINKEN2_CD");
                     final String shutaruCd = rs.getString("SHUTARU_CD");
                     final String shinseiKanryouFlg = rs.getString("SHINSEI_KANRYOU_FLG");
                     final String shinseiCancelFlg = rs.getString("SHINSEI_CANCEL_FLG");
                     final String ketteiDate = rs.getString("KETTEI_DATE");
                     final String ketteiFlg = rs.getString("KETTEI_FLG");
                     final String shuugakuMonthCount = rs.getString("SHUUGAKU_MONTH_COUNT");

                     final KojinShinseiHistDat shinsei = new KojinShinseiHistDat(kojinNo, shinseiYear, shikinShousaiDiv, issuedate, ukeYear, ukeNo, ukeEdaban, shinseiDate, shuugakuNo, nenrei, yoyakuKibouGk, sYoyakuKibouYm, eYoyakuKibouYm, sTaiyoYm, eTaiyoYm, shinseiDiv, setaiSeq, keizokuKaisuu, heikyuuShougakuStatus1, heikyuuShougakuRemark1, heikyuuShougakuStatus2, heikyuuShougakuRemark2, heikyuuShougakuStatus3, heikyuuShougakuRemark3, shitakuCancelChokuFlg, shitakuCancelRiFlg, hSchoolCd, schoolName, katei, kateiName, grade, entDate, hGradYm, shitakukinTaiyoDiv, heikyuuShitakuStatus1, heikyuuShitakuRemark1, heikyuuShitakuStatus2, heikyuuShitakuRemark2, heikyuuShitakuStatus3, heikyuuShitakuRemark3, yuushiFail, yuushiFailDiv, yuushiFailRemark, bankCd, yuushiCourseDiv, rentaiCd, shinken1Cd, shinken2Cd, shutaruCd, shinseiKanryouFlg, shinseiCancelFlg, ketteiDate, ketteiFlg, shuugakuMonthCount);
                     list.add(shinsei);
                 }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
           } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
           }
           if (!list.isEmpty()) {
               Collections.sort(list);
               setKojinHistDat(db2, param, list);
               setRentai(db2, param, list);
               setTaiyoSetaiList(db2, param, list);
               setTaiyoYdat(db2, param, list);
           }
           return list;
        }

        private static void setRentai(final DB2UDB db2, final Param param, final List list) {
            Collection rentaiCds = new HashSet();
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                if (null != shinsei._rentaiCd) {
                    rentaiCds.add(shinsei._rentaiCd);
                }
            }

            final Map shinkenshaHists = ShinkenshaHistDat.load(db2, rentaiCds, param._shinseiYear);
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                if (null != shinsei._rentaiCd) {
                    shinsei._rentai = (ShinkenshaHistDat) shinkenshaHists.get(shinsei._rentaiCd);
                }
            }
        }

        private static void setTaiyoSetaiList(final DB2UDB db2, final Param param, final List list) {
            final Collection keys = new HashSet();
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                if (null != shinsei._kojinNo && null != shinsei._setaiSeq) {
                    keys.add(shinsei._kojinNo + "-" + shinsei._setaiSeq);
                }
            }
            final Map taiyosetaiDat = KojinTaiyoSetaiDat.load(db2, param, keys);
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                if (null != shinsei._kojinNo && null != shinsei._setaiSeq && null != taiyosetaiDat.get(shinsei._kojinNo + "-" + shinsei._setaiSeq)) {
                    shinsei._taiyosetaiList = (List) taiyosetaiDat.get(shinsei._kojinNo + "-" + shinsei._setaiSeq);
                }
            }
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAIN AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.*, ");
            stb.append("         T2.KATEI_NAME, ");
            stb.append("         T2.SHUUGAKU_MONTH_COUNT, ");
            stb.append("         FISCALYEAR(DATE(T1.H_GRAD_YM || '-01')) AS H_GRAD_YM_NENDO, ");
            stb.append("         FISCALYEAR(DATE(T1.E_YOYAKU_KIBOU_YM || '-01')) AS E_YOYAKU_KIBOU_YM_NENDO ");
            stb.append("     FROM ");
            stb.append("         KOJIN_SHINSEI_HIST_DAT T1 ");
            stb.append("         LEFT JOIN KATEI_MST T2 ON T2.KATEI = T1.KATEI ");
            stb.append("     WHERE ");
            stb.append("         T1.SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            stb.append("         AND (T1.SHIKIN_SHOUSAI_DIV = '02' OR T1.SHIKIN_SHOUSAI_DIV = '08') ");
            if ("1".equals(param._classDiv)) {
                stb.append("         AND T1.SHUUGAKU_NO = '" + param._shuugakuno + "' ");
            } else {
                stb.append("         AND T1.H_SCHOOL_CD IN " + SQLUtils.whereIn(true, param._classSelected));
            }
            stb.append("         AND T1.SHINSEI_CANCEL_FLG IS NULL ");
            stb.append("         AND T1.SHUUGAKU_NO IS NOT NULL ");
            stb.append("         AND T1.KETTEI_FLG = '1' ");
            stb.append("         AND T1.KETTEI_DATE IS NOT NULL ");
            stb.append("         AND T1.SHORI_JYOUKYOU <> '6' ");
            stb.append(" ), TAIYO_KEIKAKU AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.SHUUGAKU_NO, T1.SHINSEI_YEAR AS YEAR, COUNT(*) AS COUNT ");
            stb.append("     FROM ");
            stb.append("         TAIYO_KEIKAKU_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         EXISTS (SELECT 'X' FROM MAIN T2 WHERE T2.KOJIN_NO = T1.KOJIN_NO) ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SHUUGAKU_NO, T1.SHINSEI_YEAR ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT ");
            stb.append("         T1.SHUUGAKU_NO, 'ALL' AS YEAR, COUNT(*) AS COUNT ");
            stb.append("     FROM ");
            stb.append("         TAIYO_KEIKAKU_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         EXISTS (SELECT 'X' FROM MAIN T2 WHERE T2.KOJIN_NO = T1.KOJIN_NO) ");
            stb.append("         AND T1.TEISHI_FLG <> '1' ");
            stb.append("     GROUP BY ");
            stb.append("         T1.SHUUGAKU_NO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   L3.NAME AS SCHOOL_NAME ");
            stb.append(" FROM MAIN T1 ");
            stb.append(" LEFT JOIN TAIYO_KEIKAKU L1 ON L1.SHUUGAKU_NO = T1.SHUUGAKU_NO AND L1.YEAR = T1.H_GRAD_YM_NENDO ");
            stb.append(" LEFT JOIN TAIYO_KEIKAKU L2 ON L2.SHUUGAKU_NO = T1.SHUUGAKU_NO AND L2.YEAR = 'ALL'AND L1.COUNT > T1.SHUUGAKU_MONTH_COUNT ");
            stb.append(" LEFT JOIN SCHOOL_DAT L3 ON L3.SCHOOLCD = T1.H_SCHOOL_CD ");
            stb.append(" LEFT JOIN TAIYO_KEIKAKU L4 ON L4.SHUUGAKU_NO = T1.SHUUGAKU_NO AND L4.YEAR = T1.E_YOYAKU_KIBOU_YM_NENDO ");
            stb.append(" WHERE ");
            stb.append("     L1.SHUUGAKU_NO IS NULL AND  ");
            stb.append("     L2.SHUUGAKU_NO IS NULL AND  ");
            stb.append("     L4.SHUUGAKU_NO IS NULL ");
            return stb.toString();
        }

        private static void setKojinHistDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set kojinNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat taiyoyoyaku = (KojinShinseiHistDat) it.next();
                kojinNos.add(taiyoyoyaku._kojinNo);
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
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final Map name = (Map) nameMap.get(shinsei._kojinNo);
                if (null != name) {
                    shinsei._kojinFamilyName = (String) name.get("FAMILY_NAME");
                    shinsei._kojinFirstName = (String) name.get("FIRST_NAME");
                    shinsei._kojinFamilyNameKana = (String) name.get("FAMILY_NAME_KANA");
                    shinsei._kojinFirstNameKana = (String) name.get("FIRST_NAME_KANA");
                    shinsei._kojinBirthday = (String) name.get("BIRTHDAY");

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

        private static void setTaiyoYdat(final DB2UDB db2, final Param param, final List list) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlKojinTaiyoYDat();
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = list.iterator(); it.hasNext();) {
                    final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                    ps.setString(1, shinsei._kojinNo);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        getMappedMap(shinsei._taiyoYdatShikinshubetsuShinseiyearShoritotalgkMap, rs.getString("SHIKIN_SHUBETSU")).put(Integer.valueOf(rs.getString("SHINSEI_YEAR")), rs.getString("SHISHUTSU_SHORI_TOTAL_GK"));
                    }
                    DbUtils.closeQuietly(rs);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }


        private static String sqlKojinTaiyoYDat() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT KOJIN_NO, SHUUGAKU_NO, SHINSEI_YEAR, SHIKIN_SHUBETSU, SHISHUTSU_SHORI_TOTAL_GK ");
            stb.append(" FROM KOJIN_TAIYO_YDAT  ");
            stb.append(" WHERE KOJIN_NO = ? ");
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

        public String getBirthdayStr() {
            return StringUtils.defaultString(_familyNameKana) + "　" +  StringUtils.defaultString(_firstNameKana);
        }

        public static Map load(final DB2UDB db2, final Collection shinkenCdSet, final String year) {
            Map map = new HashMap();
            if (shinkenCdSet.isEmpty()) {
                return map;
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                 ps = db2.prepareStatement(sql(shinkenCdSet, year));
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
                     final ShinkenshaHistDat shinkensha = new ShinkenshaHistDat(shinkenCd, issuedate,familyName, firstName, familyNameKana, firstNameKana,
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

        public static String sql(final Collection shinkenCdSet, final String year) {
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

    private static class KojinTaiyoSetaiDat {
        final String _kojinNo;
        final String _shinseiYear;
        final String _seq;
        final String _familyName;
        final String _firstName;
        final String _firstNameKana;
        final String _familyNameKana;
        final String _tsuzukigaraCd;
        final String _nenrei;
        final String _kyoudaiKojinNo;
        final String _shotokuCd;
        final String _shotokuGk;
        final String _koujoFuboIgai;
        final String _koujoHoken;
        final String _koujoShotoku;
        final String _ninteiGk;
        final String _shutaruFlg;
        final String _setainushiFlg;
        final String _remark;
        final String _reason;
        final String _shikinDiv;
        final String _tsuzukigaraName;

        KojinTaiyoSetaiDat(
                final String kojinNo,
                final String shinseiYear,
                final String seq,
                final String familyName,
                final String firstName,
                final String firstNameKana,
                final String familyNameKana,
                final String tsuzukigaraCd,
                final String nenrei,
                final String kyoudaiKojinNo,
                final String shotokuCd,
                final String shotokuGk,
                final String koujoFuboIgai,
                final String koujoHoken,
                final String koujoShotoku,
                final String ninteiGk,
                final String shutaruFlg,
                final String setainushiFlg,
                final String remark,
                final String reason,
                final String shikinDiv,
                final String tsuzukigaraName
        ) {
            _kojinNo = kojinNo;
            _shinseiYear = shinseiYear;
            _seq = seq;
            _familyName = familyName;
            _firstName = firstName;
            _firstNameKana = firstNameKana;
            _familyNameKana = familyNameKana;
            _tsuzukigaraCd = tsuzukigaraCd;
            _nenrei = nenrei;
            _kyoudaiKojinNo = kyoudaiKojinNo;
            _shotokuCd = shotokuCd;
            _shotokuGk = shotokuGk;
            _koujoFuboIgai = koujoFuboIgai;
            _koujoHoken = koujoHoken;
            _koujoShotoku = koujoShotoku;
            _ninteiGk = ninteiGk;
            _shutaruFlg = shutaruFlg;
            _setainushiFlg = setainushiFlg;
            _remark = remark;
            _reason = reason;
            _shikinDiv = shikinDiv;
            _tsuzukigaraName = tsuzukigaraName;
        }

        public String getName() {
            return StringUtils.defaultString(_familyName) + "　" +  StringUtils.defaultString(_firstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_familyNameKana) + "　" +  StringUtils.defaultString(_firstNameKana);
        }

        public static Map load(final DB2UDB db2, final Param param, final Collection kojinNos) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map m = new HashMap();
            try {
                ps = db2.prepareStatement(sql(param, kojinNos));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kojinNo = rs.getString("KOJIN_NO");
                    final String shinseiYear = rs.getString("SHINSEI_YEAR");
                    final String setaiSeq = rs.getString("SETAI_SEQ");
                    final String seq = rs.getString("SEQ");
                    final String familyName = rs.getString("FAMILY_NAME");
                    final String firstName = rs.getString("FIRST_NAME");
                    final String firstNameKana = rs.getString("FIRST_NAME_KANA");
                    final String familyNameKana = rs.getString("FAMILY_NAME_KANA");
                    final String tsuzukigaraCd = rs.getString("TSUZUKIGARA_CD");
                    final String nenrei = rs.getString("NENREI");
                    final String kyoudaiKojinNo = rs.getString("KYOUDAI_KOJIN_NO");
                    final String shotokuCd = rs.getString("SHOTOKU_CD");
                    final String shotokuGk = rs.getString("SHOTOKU_GK");
                    final String koujoFuboIgai = rs.getString("KOUJO_FUBO_IGAI");
                    final String koujoHoken = rs.getString("KOUJO_HOKEN");
                    final String koujoShotoku = rs.getString("KOUJO_SHOTOKU");
                    final String ninteiGk = rs.getString("NINTEI_GK");
                    final String shutaruFlg = rs.getString("SHUTARU_FLG");
                    final String setainushiFlg = rs.getString("SETAINUSHI_FLG");
                    final String remark = rs.getString("REMARK");
                    final String reason = rs.getString("REASON");
                    final String shikinDiv = rs.getString("SHIKIN_DIV");
                    final String tsuzukigaraName = rs.getString("TSUZUKIGARA_NAME");
                    final KojinTaiyoSetaiDat kojintaiyosetaidat = new KojinTaiyoSetaiDat(kojinNo, shinseiYear, seq, familyName, firstName, firstNameKana, familyNameKana, tsuzukigaraCd, nenrei, kyoudaiKojinNo, shotokuCd, shotokuGk, koujoFuboIgai, koujoHoken, koujoShotoku, ninteiGk, shutaruFlg, setainushiFlg, remark, reason, shikinDiv, tsuzukigaraName);
                    if (null == m.get(kojinNo + "-" + setaiSeq)) {
                        m.put(kojinNo + "-" + setaiSeq, new ArrayList());
                    }
                    final List setaiList = (List) m.get(kojinNo + "-" + setaiSeq);
                    setaiList.add(kojintaiyosetaidat);
                }
           } catch (Exception ex) {
                log.fatal("exception!", ex);
           } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
           }
           return m;
        }

        public static String sql(final Param param, final Collection kojinNos) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.*, NMT006.NAME1 AS TSUZUKIGARA_NAME ");
            stb.append(" FROM ");
            stb.append("     KOJIN_TAIYO_SETAI_DAT T1 ");
            stb.append("     LEFT JOIN NAME_MST NMT006 ON NMT006.NAMECD1 = 'T006' AND NMT006.NAMECD2 = T1.TSUZUKIGARA_CD ");
            stb.append(" WHERE ");
            stb.append("     (T1.KOJIN_NO || '-' || CHAR(T1.SETAI_SEQ)) IN " + SQLUtils.whereIn(true, toArray(kojinNos)));
            stb.append("     AND T1.SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            stb.append(" ORDER BY ");
            stb.append("     CASE T1.TSUZUKIGARA_CD WHEN '05' THEN '04' "); // 05:姉と04:兄を年齢順にソート
            stb.append("                            WHEN '07' THEN '06' "); // 07:妹と06:弟を年齢順にソート
            stb.append("     ELSE T1.TSUZUKIGARA_CD END, ");
            stb.append("     VALUE(NENREI, 0) DESC, ");
            stb.append("     T1.FIRST_NAME_KANA, ");
            stb.append("     T1.SEQ ");
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
        private final String _ctrlDate;
        private final String _classDiv;
        private final String[] _classSelected;
        private final String _shuugakuno;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiYear = request.getParameter("SHINSEI_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _classDiv = request.getParameter("CLASS_DIV");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _shuugakuno = request.getParameter("SHUUGAKUNO");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
        }
    }
}

// eof

