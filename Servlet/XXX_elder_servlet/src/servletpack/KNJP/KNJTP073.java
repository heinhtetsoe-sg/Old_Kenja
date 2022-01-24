/*
 * $Id: aeb02543395da696de7f24375dcaa70b29ae52f1 $
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
 * 京都府修学金 貸与決定通知書（修学金/支度金）
 */
public class KNJTP073 {

    private static final Log log = LogFactory.getLog(KNJTP073.class);

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

    private String getNenreiStr(final String nenrei) {
        return "   ( " + StringUtils.defaultString(nenrei, "  ") + " 歳)";
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List list = KojinShinseiHistDat.load(db2, _param);

        for (final Iterator taiyoIt = list.iterator(); taiyoIt.hasNext();) {
            final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) taiyoIt.next();

            log.debug(" kojinNo = " + shinsei._kojinNo);

            if ("1".equals(_param._shinseiDiv)) {
                printShugakukin(svf, shinsei);
            } else if ("2".equals(_param._shinseiDiv)) {
                printShitakukin(svf, shinsei);
            }
        }
    }

    private String digitToZenkaku(final String s) {
        if (null == s) {
            return s;
        }
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (Character.isDigit(ch)) {
                final char[] zenDigit = {'０', '１', '２', '３', '４', '５', '６', '７', '８', '９'};
                stb.append(zenDigit[(int) (ch - 0x30)]);
            } else {
                stb.append(ch);
            }
        }
        return stb.toString();
    }

    private void VrsOut(final String[] field1, final String[] data, final Vrw32alp svf) {
        if (null == data) {
            return;
        }
        for (int i = 0; i < Math.min(field1.length, data.length); i++) {
            svf.VrsOut(field1[i], data[i]);
        }
    }

    private void VrsOutDate(final String field1, final String data, final Vrw32alp svf) {
        if (null == data) {
            return;
        }
        svf.VrsOut(field1, _param._shugakuDate.formatDate(data, false));
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

    private void printShugakukin(final Vrw32alp svf, final KojinShinseiHistDat shinsei) {
        svf.VrSetForm("KNJTP073.frm", 1);

        if (null != shinsei._shutaru) {
            svf.VrsOut("ZIP_NO", shinsei._shutaru._zipcd);
            printAddress(svf, shinsei._shutaru._addr1, shinsei._shutaru._addr2);
//            svf.VrsOut("ADDRESS1", shinsei._shutaru._addr1);
//            VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(shinsei._shutaru._addr2, 40, 2), svf);
            svf.VrsOut("NAME", shinsei._shutaru.getName());
        }

        svf.VrsOut("CERT_NO", shinsei.getBunshoBangou(_param));
        VrsOutDate("DATE", shinsei._ketteiDate, svf);
        svf.VrsOut("APPLI_NO", shinsei._shuugakuNo);
        svf.VrsOut("GOVERNER", _param._chijiName);
        VrsOutDate("DAY1", shinsei._shinseiDate, svf);
        svf.VrsOut("SCHOOL_NAME", shinsei._schoolName);
        svf.VrsOut("NAME2", shinsei.getName());
        svf.VrsOut("LOAN_METHOD", shinsei._yuushiCourseDivName);
        if ("2".equals(shinsei._yuushiCourseDiv) || "4".equals(shinsei._yuushiCourseDiv)) {
            if (null != shinsei._grade) {
                String g = shinsei._grade;
                if (NumberUtils.isDigits(g)) {
                    g = String.valueOf(Integer.parseInt(g));
                }
                svf.VrsOut("INTEREST", "（入学後第" + StringUtils.defaultString(digitToZenkaku(g), "　") + "年度分）");
            }
        }
        svf.VrsOut("LOAN_MONEY", shinsei._kingaku);

        svf.VrEndPage();
        _hasData = true;
    }

    private void printShitakukin(final Vrw32alp svf, final KojinShinseiHistDat shinsei) {
        svf.VrSetForm("KNJTP074.frm", 1);

        if (null != shinsei._shutaru) {
            svf.VrsOut("ZIP_NO", shinsei._shutaru._zipcd);
            printAddress(svf, shinsei._shutaru._addr1, shinsei._shutaru._addr2);
//            svf.VrsOut("ADDRESS1", shinsei._shutaru._addr1);
//            VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(shinsei._shutaru._addr2, 40, 2), svf);
            svf.VrsOut("NAME", shinsei._shutaru.getName());
        }

        svf.VrsOut("CERT_NO", shinsei.getBunshoBangou(_param));
        VrsOutDate("DATE", shinsei._ketteiDate, svf);
        svf.VrsOut("APPLI_NO", shinsei._shuugakuNo);
//        svf.VrsOut("GOVERNER", _param._chijiName);
        VrsOutDate("DAY1", shinsei._shinseiDate, svf);

        svf.VrsOut("SCHOOL_NAME", shinsei._schoolName);
        svf.VrsOut("NAME2", shinsei.getName());
        if ("1".equals(shinsei._schoolDistcd) || "2".equals(shinsei._schoolDistcd)) {
            svf.VrsOut("PREPARE_METHOD", "国公立");
            svf.VrsOut("LOAN_MONEY", "50000");
        } else if ("3".equals(shinsei._schoolDistcd)) {
            svf.VrsOut("PREPARE_METHOD", "私立");
            svf.VrsOut("LOAN_MONEY", "250000");
        }

        svf.VrEndPage();
        _hasData = true;
    }

    private static class KojinShinseiHistDat {
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
        final String _schoolDistcd;
        final String _schoolDistcdName;
        final String _katei;
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

        final String _yuushiCourseDivName;
        final String _kingaku;

        ShinkenshaHistDat _shutaru = null;

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
                final String schoolDistcd,
                final String schoolDistcdName,
                final String katei,
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
                final String yuushiCourseDivName,
                final String kingaku
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
            _schoolDistcd = schoolDistcd;
            _schoolDistcdName = schoolDistcdName;
            _katei = katei;
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
            _yuushiCourseDivName = yuushiCourseDivName;
            _kingaku = kingaku;
        }

        public String getName() {
            return StringUtils.defaultString(_kojinFamilyName) + "　" +  StringUtils.defaultString(_kojinFirstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_kojinFamilyNameKana) + "　" +  StringUtils.defaultString(_kojinFirstNameKana);
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
                    final String schoolDistcd = rs.getString("SCHOOL_DISTCD");
                    final String schoolDistcdName = rs.getString("SCHOOL_DISTCD_NAME");
                    final String katei = rs.getString("KATEI");
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
                    final String yuushiCourseDivName = rs.getString("YUUSHI_COURSE_DIV_NAME");
                    final String kingaku = rs.getString("KINGAKU");

                    final KojinShinseiHistDat shinsei = new KojinShinseiHistDat(kojinNo, shinseiYear, shikinShousaiDiv, issuedate, ukeYear, ukeNo, ukeEdaban, shinseiDate, shuugakuNo, nenrei, yoyakuKibouGk, sYoyakuKibouYm, eYoyakuKibouYm, sTaiyoYm, eTaiyoYm, shinseiDiv, keizokuKaisuu, heikyuuShougakuStatus1, heikyuuShougakuRemark1, heikyuuShougakuStatus2, heikyuuShougakuRemark2, heikyuuShougakuStatus3, heikyuuShougakuRemark3, shitakuCancelChokuFlg, shitakuCancelRiFlg, hSchoolCd, schoolName, schoolDistcd, schoolDistcdName, katei, grade, entDate, hGradYm, shitakukinTaiyoDiv, heikyuuShitakuStatus1, heikyuuShitakuRemark1, heikyuuShitakuStatus2, heikyuuShitakuRemark2, heikyuuShitakuStatus3, heikyuuShitakuRemark3, yuushiFail, yuushiFailDiv, yuushiFailRemark, bankCd, yuushiCourseDiv, rentaiCd, shinken1Cd, shinken2Cd, shutaruCd, shinseiKanryouFlg, shinseiCancelFlg, ketteiDate, ketteiFlg, yuushiCourseDivName, kingaku);
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
               setShutaru(db2, param, list);
           }
           return list;
        }

        private static void setShutaru(final DB2UDB db2, final Param param, final List list) {
            final Collection shinkenCds = new HashSet();
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                if (null != shinsei._shutaruCd) {
                    shinkenCds.add(shinsei._shutaruCd);
                }
            }
            final Map shinkenshaHists = ShinkenshaHistDat.load(db2, shinkenCds);
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                if (null != shinsei._shutaruCd) {
                    shinsei._shutaru = (ShinkenshaHistDat) shinkenshaHists.get(shinsei._shutaruCd);
                }
            }
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAIN AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.* ");
            stb.append("     FROM ");
            stb.append("         KOJIN_SHINSEI_HIST_DAT T1 ");
            stb.append("     WHERE ");
            if ("2".equals(param._classDiv)) {
                stb.append("         T1.SHINSEI_YEAR  || '-' || T1.UKE_YEAR || '-' || T1.UKE_NO || '-' || T1.UKE_EDABAN = '" + param._uke + "' ");
                stb.append("         AND T1.H_SCHOOL_CD IN " + SQLUtils.whereIn(true, param._schoolSelected));
            } else if ("1".equals(param._classDiv)) {
                stb.append("         T1.SHINSEI_YEAR = '" + param._shinseiYear + "' ");
                stb.append("         AND T1.SHUUGAKU_NO = '" + param._shuugakuNo + "' ");
            }
            if ("1".equals(param._shinseiDiv)) {
                stb.append("         AND T1.SHIKIN_SHOUSAI_DIV = '07' ");
            } else if ("2".equals(param._shinseiDiv)) {
                stb.append("         AND T1.SHIKIN_SHOUSAI_DIV = '04' ");
            }
            stb.append("         AND T1.SHINSEI_CANCEL_FLG IS NULL ");
            stb.append("         AND T1.SHUUGAKU_NO IS NOT NULL ");
            stb.append("         AND T1.KETTEI_FLG = '1' ");
            stb.append("         AND T1.KETTEI_DATE IS NOT NULL ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   L3.NAME AS SCHOOL_NAME, ");
            stb.append("   L3.SCHOOL_DISTCD, ");
            stb.append("   NMT003.NAME1 AS SCHOOL_DISTCD_NAME, ");
            stb.append("   NMT026.NAME1 AS YUUSHI_COURSE_DIV_NAME, ");
            stb.append("   NMT026.NAMESPARE1 AS KINGAKU ");
            stb.append(" FROM MAIN T1 ");
            stb.append(" LEFT JOIN SCHOOL_DAT L3 ON L3.SCHOOLCD = T1.H_SCHOOL_CD ");
            stb.append(" LEFT JOIN NAME_MST NMT003 ON NMT003.NAMECD1 = 'T003' AND NMT003.NAMECD2 = L3.SCHOOL_DISTCD ");
            stb.append(" LEFT JOIN NAME_MST NMT026 ON NMT026.NAMECD1 = 'T026' AND NMT026.NAMECD2 = T1.YUUSHI_COURSE_DIV ");
            stb.append(" ORDER BY ");
            stb.append("   T1.H_SCHOOL_CD, T1.KATEI, T1.SHUUGAKU_NO ");
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
        log.fatal("$Revision: 67227 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _shinseiDiv;
        private final String _shinseiYear;
        private final String _uke;
        private final String _classDiv;
        private final String _shuugakuNo;
        private final String _loginDate;
        private final String[] _schoolSelected;
        private final String _chijiName;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiDiv = request.getParameter("SHINSEI_DIV");
            _shinseiYear = request.getParameter("SHINSEI_YEAR");
            _uke = request.getParameter("UKE");   // parameter UKE = SHINSEI_YEAR - UKE_YEAR - UKE_NO - UKE_EDABAN
            _classDiv = request.getParameter("CLASS_DIV");
            _shuugakuNo = request.getParameter("SHUUGAKU_NO");
            _loginDate = request.getParameter("CTRL_DATE");
            _schoolSelected = request.getParameterValues("SCHOOL_SELECTED");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _chijiName = _shugakuDate.getChijiName2(db2);
        }
    }
}

// eof

