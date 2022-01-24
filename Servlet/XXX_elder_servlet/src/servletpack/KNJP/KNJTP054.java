/*
 * $Id: bba6dda575dbf4b9d513a36ec77f46267e6c5ee8 $
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
 * 京都府修学金 貸与決定通知書（修学金/支度金）
 */
public class KNJTP054 {

    private static final Log log = LogFactory.getLog(KNJTP054.class);

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

        final List list = KojinShinseiHistDat.load(db2, _param);

        for (final Iterator taiyoIt = list.iterator(); taiyoIt.hasNext();) {
            final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) taiyoIt.next();

            if ("1".equals(_param._shinseiDiv)) {
                printShugakukin(svf, shinsei);
            } else if ("2".equals(_param._shinseiDiv)) {
                printShitakukin(svf, shinsei);
            }
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
        svf.VrSetForm("KNJTP054_1.frm", 1);

        svf.VrsOut("ZIP_NO", shinsei._zipcd);
        printAddress(svf, shinsei._addr1, shinsei._addr2);
//        svf.VrsOut("ADDRESS1", shinsei._addr1);
//        VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(shinsei._addr2, 40, 2), svf);
        svf.VrsOut("NAME", shinsei.getName());

        svf.VrsOut("CERT_NO", shinsei.getBunshoBangou(_param));
        VrsOutDate("DATE", shinsei._ketteiDate, svf);
//        svf.VrsOut("GOVERNER", _param._chijiName);
        VrsOutDate("DAY1", shinsei._shinseiDate, svf);
        svf.VrsOut("LOAN_APPLI_NO", shinsei._shuugakuNo);
        svf.VrsOut("LOAN_FMONTH1", _param._shugakuDate.formatNentuki(shinsei._sTaiyoYm));
        svf.VrsOut("LOAN_TMONTH1", _param._shugakuDate.formatNentuki(shinsei._eTaiyoYm));
        svf.VrsOut("LOAN_FMONTH2", _param._shugakuDate.formatNentuki(shinsei._sTaiyoYmBefore));
        svf.VrsOut("LOAN_TMONTH2", _param._shugakuDate.formatNentuki(shinsei._eTaiyoYmBefore));
        svf.VrsOut("LOAN_MONEY", shinsei._yoyakuKibouGk);

        svf.VrsOut("CHARGE", "高校教育課");
        svf.VrsOut("FIELD1", "(075)574-7518");

        svf.VrEndPage();
        _hasData = true;
    }

    private void printShitakukin(final Vrw32alp svf, final KojinShinseiHistDat shinsei) {
        svf.VrSetForm("KNJTP054_2.frm", 1);

        svf.VrsOut("ZIPNO1", shinsei._zipcd);
        printAddress(svf, shinsei._addr1, shinsei._addr2);
//        svf.VrsOut("ADDRESS1", shinsei._addr1);
//        VrsOut(new String[]{"ADDRESS2", "ADDRESS3"}, KNJ_EditEdit.get_token(shinsei._addr2, 40, 2), svf);
        svf.VrsOut("NAME", shinsei.getName());

        svf.VrsOut("CERT_NO", shinsei.getBunshoBangou(_param));
        VrsOutDate("DATE", shinsei._ketteiDate, svf);
//        svf.VrsOut("GOVERNER", _param._chijiName);
        VrsOutDate("DAY1", shinsei._shinseiDate, svf);
        svf.VrsOut("LOAN_APPLI_NO", shinsei._shuugakuNo);
        svf.VrsOut("LOAN_MONEY", shinsei._yoyakuKibouGk);

        svf.VrsOut("CHARGE", "高校教育課");
        svf.VrsOut("FIELD1", "(075)574-7518");

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

        String _eTaiyoYmBefore;
        String _sTaiyoYmBefore;

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
                final String ketteiFlg
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

                     final KojinShinseiHistDat shinsei = new KojinShinseiHistDat(kojinNo, shinseiYear, shikinShousaiDiv, issuedate, ukeYear, ukeNo, ukeEdaban, shinseiDate, shuugakuNo, nenrei, yoyakuKibouGk, sYoyakuKibouYm, eYoyakuKibouYm, sTaiyoYm, eTaiyoYm, shinseiDiv, keizokuKaisuu, heikyuuShougakuStatus1, heikyuuShougakuRemark1, heikyuuShougakuStatus2, heikyuuShougakuRemark2, heikyuuShougakuStatus3, heikyuuShougakuRemark3, shitakuCancelChokuFlg, shitakuCancelRiFlg, hSchoolCd, schoolName, katei, grade, entDate, hGradYm, shitakukinTaiyoDiv, heikyuuShitakuStatus1, heikyuuShitakuRemark1, heikyuuShitakuStatus2, heikyuuShitakuRemark2, heikyuuShitakuStatus3, heikyuuShitakuRemark3, yuushiFail, yuushiFailDiv, yuushiFailRemark, bankCd, yuushiCourseDiv, rentaiCd, shinken1Cd, shinken2Cd, shutaruCd, shinseiKanryouFlg, shinseiCancelFlg, ketteiDate, ketteiFlg);
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
               if ("1".equals(param._shinseiDiv)) {
                   setBeforeTaiyoTerm(db2, param, list);
               }
           }
           return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAIN AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.* ");
            stb.append("     FROM ");
            stb.append("         KOJIN_SHINSEI_HIST_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            if ("2".equals(param._classDiv)) {
                stb.append("         AND T1.H_SCHOOL_CD IN " + SQLUtils.whereIn(true, param._schoolSelected));
            } else if ("1".equals(param._classDiv)) {
                stb.append("         AND T1.KOJIN_NO = '" + param._kojinNo + "' ");
            }
            if ("1".equals(param._shinseiDiv)) {
                stb.append("         AND T1.SHIKIN_SHOUSAI_DIV IN ('02', '08') ");
            } else if ("2".equals(param._shinseiDiv)) {
                stb.append("         AND T1.SHIKIN_SHOUSAI_DIV IN ('03', '05') ");
            }
            stb.append("         AND T1.SHINSEI_CANCEL_FLG IS NULL ");
            stb.append("         AND T1.SHUUGAKU_NO IS NOT NULL ");
            stb.append("         AND T1.KETTEI_FLG = '1' ");
            stb.append("         AND T1.KETTEI_DATE IS NOT NULL ");
            if ("2".equals(param._classDiv)) {
                stb.append("         AND T1.UKE_YEAR || '-' || T1.UKE_NO || '-' || T1.UKE_EDABAN = '" + param._uke + "' ");
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   L3.NAME AS SCHOOL_NAME ");
            stb.append(" FROM MAIN T1 ");
            stb.append(" LEFT JOIN SCHOOL_DAT L3 ON L3.SCHOOLCD = T1.H_SCHOOL_CD ");
            stb.append(" ORDER BY T1.H_SCHOOL_CD, T1.KATEI, T1.SHUUGAKU_NO ");
            return stb.toString();
        }

        private static void setBeforeTaiyoTerm(final DB2UDB db2, final Param param, final List shinseiList) {
            final Set kojinNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                kojinNos.add(shinsei._kojinNo);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map nameMap = new HashMap();
            try {
                final String sql = sqlBeforeTaiyoTerm(kojinNos, param._shinseiYear);
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
                    shinsei._sTaiyoYmBefore = (String) name.get("S_TAIYO_YM_BEFORE");
                    shinsei._eTaiyoYmBefore = (String) name.get("E_TAIYO_YM_BEFORE");
                }
            }
        }


        private static String sqlBeforeTaiyoTerm(final Collection kojinNos, final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_HIST AS ( ");
            stb.append("     SELECT KOJIN_NO, SHINSEI_YEAR, SHIKIN_SHOUSAI_DIV, MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("     FROM KOJIN_SHINSEI_HIST_DAT  ");
            stb.append("     WHERE KOJIN_NO IN " + SQLUtils.whereIn(true, toArray(kojinNos)) + " ");
            stb.append("           AND SHIKIN_SHOUSAI_DIV IN ('02', '08') ");
            stb.append("           AND SHINSEI_YEAR < '" + year + "' ");
            stb.append("     GROUP BY KOJIN_NO, SHINSEI_YEAR, SHIKIN_SHOUSAI_DIV ");
            stb.append(" ), MIN_MAX AS ( ");
            stb.append("     SELECT KOJIN_NO, MIN(SHINSEI_YEAR) AS SHINSEI_YEAR_MIN, MAX(SHINSEI_YEAR) AS SHINSEI_YEAR_MAX ");
            stb.append("     FROM MAX_HIST  ");
            stb.append("     GROUP BY KOJIN_NO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.KOJIN_NO, ");
            stb.append("     T2H.S_TAIYO_YM AS S_TAIYO_YM_BEFORE, ");
            stb.append("     T3H.E_TAIYO_YM AS E_TAIYO_YM_BEFORE ");
            stb.append(" FROM MIN_MAX T1  ");
            stb.append(" INNER JOIN MAX_HIST T2 ON T2.KOJIN_NO = T1.KOJIN_NO AND T2.SHINSEI_YEAR = T1.SHINSEI_YEAR_MIN ");
            stb.append(" INNER JOIN MAX_HIST T3 ON T3.KOJIN_NO = T1.KOJIN_NO AND T3.SHINSEI_YEAR = T1.SHINSEI_YEAR_MAX ");
            stb.append(" LEFT JOIN KOJIN_SHINSEI_HIST_DAT T2H ON T2H.KOJIN_NO = T2.KOJIN_NO ");
            stb.append("        AND T2H.SHINSEI_YEAR = T2.SHINSEI_YEAR AND T2H.SHIKIN_SHOUSAI_DIV = T2.SHIKIN_SHOUSAI_DIV AND T2H.ISSUEDATE = T2.ISSUEDATE ");
            stb.append(" LEFT JOIN KOJIN_SHINSEI_HIST_DAT T3H ON T3H.KOJIN_NO = T3.KOJIN_NO ");
            stb.append("        AND T3H.SHINSEI_YEAR = T3.SHINSEI_YEAR AND T3H.SHIKIN_SHOUSAI_DIV = T3.SHIKIN_SHOUSAI_DIV AND T3H.ISSUEDATE = T3.ISSUEDATE ");
            return stb.toString();
        }

        private static void setKojinHistDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set kojinNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
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
        private final String _shinseiDiv;
        private final String _shinseiYear;
        private final String _uke;
        private final String _classDiv;
        private final String _kojinNo;
        private final String _loginDate;
        private final String[] _schoolSelected;
        private final String _chijiName;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiDiv = request.getParameter("SHINSEI_DIV");
            _shinseiYear = request.getParameter("SHINSEI_YEAR");
            _uke = request.getParameter("UKE");
            _classDiv = request.getParameter("CLASS_DIV");
            _kojinNo = request.getParameter("KOJIN_NO");
            _loginDate = request.getParameter("CTRL_DATE");
            _schoolSelected = request.getParameterValues("SCHOOL_SELECTED");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _chijiName = _shugakuDate.getChijiName(db2);
        }
    }
}

// eof

