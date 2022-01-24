/*
 * $Id: f3c269b86ca7ad908a65dee9c446d0f1035edab8 $
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
 * 京都府修学金 特別融資利子補給金支払・支出命令書
 */
public class KNJTP079 {

    private static final Log log = LogFactory.getLog(KNJTP079.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List list = KojinShinseiHistDat.load(db2, _param);

        final List pageList = new ArrayList();
        List shinseiList0 = null;
        String schoolcd = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
            if (null == schoolcd || !schoolcd.equals(shinsei._hSchoolCd)) {
                shinseiList0 = new ArrayList();
                pageList.add(shinseiList0);
            }
            schoolcd = shinsei._hSchoolCd;
            shinseiList0.add(shinsei);
        }

        final int max = 15; // 1ページあたり行数
        int tpage = 0; // 総ページ数
        for (int i = 0; i < pageList.size(); i++) {
            final int size = ((List) pageList.get(i)).size() + ((i == pageList.size() - 1) ? 2 : 1);
            final int p = size / max + (size % max == 0 ? 0 : 1);
            tpage += p;
        }

        int page = 1;
        for (int i = 0; i < pageList.size(); i++) {
            final List shinseiList = (List) pageList.get(i);

            printShinsei(svf, shinseiList, page, tpage, max);
            final int size = ((List) pageList.get(i)).size() + ((i == pageList.size() - 1) ? 2 : 1);
            final int p = size / max + (size % max == 0 ? 0 : 1);
            page += p;
        }

        // 最後のページに総合計を表示
        if (_hasData) {
            svfPrintTotalGk(svf, pageList, Math.min(page, tpage), tpage);
        }
    }

    private void svfPrintTotalGk(final Vrw32alp svf, final List pageList, final int page, final int tpage) {
        svf.VrsOut("PAGE", String.valueOf(page));
        svf.VrsOut("PAGE2", String.valueOf(tpage));
        BigDecimal totalShugakukinGk = new BigDecimal(0);
        int shugakukinCount = 0;
        BigDecimal totalShitakukinGk = new BigDecimal(0);
        int shitakukinCount = 0;

        for (final Iterator pIt = pageList.iterator(); pIt.hasNext();) {
            final List shinseiList1 = (List) pIt.next();
            for (final Iterator sIt = shinseiList1.iterator(); sIt.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) sIt.next();
                if (NumberUtils.isDigits(shinsei._taiyoShishutuGk)) {
                    if ("1".equals(shinsei._shikinShubetsu)) {
                        totalShugakukinGk = totalShugakukinGk.add(new BigDecimal(shinsei._taiyoShishutuGk));
                        shugakukinCount += 1;
                    } else if ("2".equals(shinsei._shikinShubetsu)){
                        totalShitakukinGk = totalShitakukinGk.add(new BigDecimal(shinsei._taiyoShishutuGk));
                        shitakukinCount += 1;
                    }
                }
            }
        }
        svf.VrsOut("ACOUNT1", String.valueOf(shugakukinCount));
        svf.VrsOut("ATOTAL_PAY_MONEY", totalShugakukinGk.toString());
        svf.VrsOut("ACOUNT2", String.valueOf(shitakukinCount));
        svf.VrsOut("ATOTAL_MONEY", totalShitakukinGk.toString());
        svf.VrEndRecord();
    }

    private void printShinsei(final Vrw32alp svf, final Collection shinseiList, int page, final int tpage, final int max) {
        svf.VrSetForm("KNJTP079.frm", 4);
        svf.VrsOut("PAGE2", String.valueOf(tpage));
        int line = 0;
        BigDecimal totalTaiyoShishutuGk = new BigDecimal(0);
        int taiyoShishutuGkCount = 0;
        for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
            final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();

            if (line % max == 0) {
                svf.VrsOut("PAGE", String.valueOf(page));
                page += 1;
            }

            svf.VrsOut("SCHOOL_NAME", shinsei._schoolName);
            svf.VrsOut("PAYDAY", _param._shugakuDate.formatDate(shinsei._furikomiDate));
            svf.VrsOut("DATE", _param._shugakuDate.formatDate(_param._date));

            svf.VrsOut("PNO", shinsei._shuugakuNo);

            if (getMS932Length(shinsei.getName()) > 20) {
                svf.VrsOut("NAME2", shinsei.getName());
            } else {
                svf.VrsOut("NAME1", shinsei.getName());
            }

            svf.VrsOut("KIND", shinsei._shikinShubetsuName);

            svf.VrsOut("ZIP_NO", shinsei._zipcd);
            svf.VrsOut("ADDRESS1", shinsei._addr1);
            svf.VrsOut("ADDRESS2", shinsei._addr2);
            svf.VrsOut("PAY_FPERIOD", _param._shugakuDate.formatNentuki(shinsei._taiyoMinYm));
            svf.VrsOut("PAY_TPERIOD", _param._shugakuDate.formatNentuki(shinsei._taiyoMaxYm));
            svf.VrsOut("MONTH", shinsei._taiyoYmCount);
            svf.VrsOut("PAY_MONEY", shinsei._taiyoShishutuGk);

            if (NumberUtils.isDigits(shinsei._taiyoShishutuGk)) {
                totalTaiyoShishutuGk = totalTaiyoShishutuGk.add(new BigDecimal(shinsei._taiyoShishutuGk));
                taiyoShishutuGkCount += 1;
            }

            if (null != shinsei._kouzaBank) {
                if (null != shinsei._kouzaBank._bankMst) {
                    svf.VrsOut("BANK_NAME", shinsei._kouzaBank._bankMst._bankname);
                    svf.VrsOut("BRANCH_NAME", shinsei._kouzaBank._bankMst._branchname);
                }
                svf.VrsOut("ITEM", shinsei._kouzaBank._kouzaDivName);
                svf.VrsOut("AC_NUMBER", shinsei._kouzaBank._accountNo);
                svf.VrsOut("AC_NAME", shinsei._kouzaBank.getMeigiKana());
            }

            _hasData = true;

            svf.VrEndRecord();
            line += 1;
        }

        if (line > 0) {

            if (line % max == 0) {
                svf.VrsOut("PAGE", String.valueOf(page));
                page += 1;
            }

            svf.VrsOut("SCOUNT", String.valueOf(taiyoShishutuGkCount));
            svf.VrsOut("STOTAL_PAY_MONEY", totalTaiyoShishutuGk.toString());
            svf.VrEndRecord();
            line += 1;
        }
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

        final String _shikinShubetsu;
        final String _shikinShubetsuName;
        final String _taiyoMinYm;
        final String _taiyoMaxYm;
        final String _taiyoShishutuGk;
        final String _taiyoYmCount;
        final String _furikomiDate;

        KojinKouzaBankDat _kouzaBank = null;

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
                final String shikinShubetsu,
                final String shikinShubetsuName,
                final String taiyoMinYm,
                final String taiyoMaxYm,
                final String taiyoShishutuGk,
                final String taiyoYmCount,
                final String furikomiDate
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

            _shikinShubetsu = shikinShubetsu;
            _shikinShubetsuName = shikinShubetsuName;
            _taiyoMinYm = taiyoMinYm;
            _taiyoMaxYm = taiyoMaxYm;
            _taiyoShishutuGk = taiyoShishutuGk;
            _taiyoYmCount = taiyoYmCount;
            _furikomiDate = furikomiDate;
        }

        public String getName() {
            return StringUtils.defaultString(_kojinFamilyName) + "　" +  StringUtils.defaultString(_kojinFirstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_kojinFamilyNameKana) + "　" +  StringUtils.defaultString(_kojinFirstNameKana);
        }

        public int compareTo(final Object o0) {
            final KojinShinseiHistDat o = (KojinShinseiHistDat) o0;
            final int cmpSch = _hSchoolCd.compareTo(o._hSchoolCd);
            if (0 != cmpSch) {
                return cmpSch;
            }
            return _shuugakuNo.compareTo(o._shuugakuNo);
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

                     final String shikinShubetsu = rs.getString("SHIKIN_SHUBETSU");
                     final String shikinShubetsuName = rs.getString("SHIKIN_SHUBETSU_NAME");
                     final String taiyoMinYm = rs.getString("TAIYO_MIN_YM");
                     final String taiyoMaxYm = rs.getString("TAIYO_MAX_YM");
                     final String taiyoShishutuGk = rs.getString("TAIYO_SHISHUTSU_GK");
                     final String taiyoYmCount = rs.getString("TAIYO_YM_COUNT");
                     final String furikomiDate = rs.getString("FURIKOMI_DATE");

                     final KojinShinseiHistDat shinsei = new KojinShinseiHistDat(kojinNo, shinseiYear, shikinShousaiDiv, issuedate, ukeYear, ukeNo, ukeEdaban, shinseiDate, shuugakuNo, nenrei, yoyakuKibouGk, sYoyakuKibouYm, eYoyakuKibouYm, sTaiyoYm, eTaiyoYm, shinseiDiv, keizokuKaisuu, heikyuuShougakuStatus1, heikyuuShougakuRemark1, heikyuuShougakuStatus2, heikyuuShougakuRemark2, heikyuuShougakuStatus3, heikyuuShougakuRemark3, shitakuCancelChokuFlg, shitakuCancelRiFlg, hSchoolCd, schoolName, katei, grade, entDate, hGradYm, shitakukinTaiyoDiv, heikyuuShitakuStatus1, heikyuuShitakuRemark1, heikyuuShitakuStatus2, heikyuuShitakuRemark2, heikyuuShitakuStatus3, heikyuuShitakuRemark3, yuushiFail, yuushiFailDiv, yuushiFailRemark, bankCd, yuushiCourseDiv, rentaiCd, shinken1Cd, shinken2Cd, shutaruCd, shinseiKanryouFlg, shinseiCancelFlg, ketteiDate, ketteiFlg,
                             shikinShubetsu, shikinShubetsuName, taiyoMinYm, taiyoMaxYm, taiyoShishutuGk, taiyoYmCount, furikomiDate);
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
               setKojinKouzaBankDat(db2, param, list);
           }

           return list;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAIN AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.*, ");
            stb.append("         T2.NAMESPARE3 AS SHIKIN_SHUBETSU ");
            stb.append("     FROM ");
            stb.append("         KOJIN_SHINSEI_HIST_DAT T1 ");
            stb.append("         LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'T030' AND T2.NAMECD2 = T1.SHIKIN_SHOUSAI_DIV ");
            stb.append("     WHERE ");
            stb.append("         T1.SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            stb.append("         AND T1.SHIKIN_SHOUSAI_DIV <> '01' AND T1.SHIKIN_SHOUSAI_DIV <> '06' AND T1.SHIKIN_SHOUSAI_DIV <> '07' ");
            stb.append("         AND T1.H_SCHOOL_CD IN " + SQLUtils.whereIn(true, param._schoolSelected));
            stb.append("         AND T1.SHINSEI_CANCEL_FLG IS NULL ");
            stb.append("         AND T1.SHUUGAKU_NO IS NOT NULL ");
            stb.append("         AND T1.KETTEI_FLG = '1' ");
            stb.append("         AND T1.KETTEI_DATE IS NOT NULL ");
            stb.append(" ), TAIYO_KEIKAKU AS ( ");
            stb.append("   SELECT ");
            stb.append("       T1.SHUUGAKU_NO, ");
            stb.append("       T1.KOJIN_NO,  ");
            stb.append("       T1.FURIKOMI_DATE,  ");
            stb.append("       T1.SHIKIN_SHUBETSU, ");
            stb.append("       MIN(T1.YEAR || '-' || T1.MONTH) AS TAIYO_MIN_YM, ");
            stb.append("       MAX(T1.YEAR || '-' || T1.MONTH) AS TAIYO_MAX_YM, ");
            stb.append("       SUM(SHISHUTSU_GK) AS TAIYO_SHISHUTSU_GK, ");
            stb.append("       COUNT(*) AS TAIYO_YM_COUNT ");
            stb.append("   FROM ");
            stb.append("       TAIYO_KEIKAKU_DAT T1 ");
            stb.append("       INNER JOIN MAIN T2 ON T2.KOJIN_NO = T1.KOJIN_NO ");
            stb.append("           AND T2.SHIKIN_SHUBETSU = T1.SHIKIN_SHUBETSU ");
            stb.append("           AND T2.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
            stb.append("   WHERE ");
            stb.append("       T1.SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            stb.append("       AND T1.FURIKOMI_DATE = '" + param._shiteiDate + "' ");
            stb.append("       AND VALUE(T1.TEISHI_FLG, '0') = '0' AND VALUE(T1.KARI_TEISHI_FLG, '0') = '0' AND VALUE(T1.GENGAKU_KARI_TEISHI_FLG, '0') = '0' ");
            stb.append("       AND T1.SHISHUTSU_GK >= 0 ");
            stb.append("   GROUP BY ");
            stb.append("       T1.SHUUGAKU_NO, T1.KOJIN_NO, T1.FURIKOMI_DATE, T1.SHIKIN_SHUBETSU ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   L1.SHIKIN_SHUBETSU, ");
            stb.append("   NMT008.ABBV1 AS SHIKIN_SHUBETSU_NAME, ");
            stb.append("   L1.TAIYO_MIN_YM, ");
            stb.append("   L1.TAIYO_MAX_YM, ");
            stb.append("   L1.TAIYO_SHISHUTSU_GK, ");
            stb.append("   L1.TAIYO_YM_COUNT, ");
            stb.append("   L1.FURIKOMI_DATE, ");
            stb.append("   L3.NAME AS SCHOOL_NAME ");
            stb.append(" FROM MAIN T1 ");
            stb.append(" INNER JOIN TAIYO_KEIKAKU L1 ON L1.KOJIN_NO = T1.KOJIN_NO ");
            stb.append("     AND L1.SHIKIN_SHUBETSU = T1.SHIKIN_SHUBETSU ");
            stb.append("     AND L1.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
            stb.append(" LEFT JOIN SCHOOL_DAT L3 ON L3.SCHOOLCD = T1.H_SCHOOL_CD ");
            stb.append(" LEFT JOIN NAME_MST NMT008 ON NMT008.NAMECD1 = 'T008' AND NMT008.NAMECD2 = L1.SHIKIN_SHUBETSU ");
            return stb.toString();
        }


        private static void setKojinKouzaBankDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set kojinNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat taiyoyoyaku = (KojinShinseiHistDat) it.next();
                kojinNos.add(taiyoyoyaku._kojinNo);
            }

            final Map kouzaBankDatMap = KojinKouzaBankDat.load(db2, kojinNos, param._shinseiYear);

            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final KojinKouzaBankDat bank = (KojinKouzaBankDat) kouzaBankDatMap.get(shinsei._kojinNo);
                if (null != bank) {
                    shinsei._kouzaBank = (KojinKouzaBankDat) kouzaBankDatMap.get(shinsei._kojinNo);
                }
            }
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


    private static class KojinKouzaBankDat {
        private static final String FURIKOMI = "1";
        private static final String FURIKAE = "2";

        final String _kojinNo;
        final String _kouzaDiv;
        final String _sDate;
        final String _bankcd;
        final String _branchcd;
        final String _yokinDiv;
        final String _accountNo;
        final String _bankMeigiSeiKana;
        final String _bankMeigiMeiKana;
        final String _bankMeigiSeiName;
        final String _bankMeigiMeiName;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _telno1;
        final String _telno2;

        final String _kouzaDivName;
        BankMst _bankMst = null;

        KojinKouzaBankDat(
                final String kojinNo,
                final String kouzaDiv,
                final String sDate,
                final String bankcd,
                final String branchcd,
                final String yokinDiv,
                final String accountNo,
                final String bankMeigiSeiKana,
                final String bankMeigiMeiKana,
                final String bankMeigiSeiName,
                final String bankMeigiMeiName,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno1,
                final String telno2
        ) {
            _kojinNo = kojinNo;
            _kouzaDiv = kouzaDiv;
            _sDate = sDate;
            _bankcd = bankcd;
            _branchcd = branchcd;
            _yokinDiv = yokinDiv;
            _accountNo = accountNo;
            _bankMeigiSeiKana = bankMeigiSeiKana;
            _bankMeigiMeiKana = bankMeigiMeiKana;
            _bankMeigiSeiName = bankMeigiSeiName;
            _bankMeigiMeiName = bankMeigiMeiName;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno1 = telno1;
            _telno2 = telno2;

            _kouzaDivName = FURIKOMI.equals(_kouzaDiv) ? "振込" : FURIKAE.equals(_kouzaDiv) ? "振替" : "";
        }

        public String getMeigiName() {
            return StringUtils.defaultString(_bankMeigiSeiName) + "　" +  StringUtils.defaultString(_bankMeigiMeiName);
        }

        public String getMeigiKana() {
            return StringUtils.defaultString(_bankMeigiSeiKana) + "　" +  StringUtils.defaultString(_bankMeigiMeiKana);
        }

        public static Map load(final DB2UDB db2, final Collection kojinNos, final String year) {
            Map kojinkouzabankdatMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(kojinNos, (Integer.parseInt(year) + 1) + "-03-31", FURIKOMI));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kojinNo = rs.getString("KOJIN_NO");
                    final String kouzaDiv = rs.getString("KOUZA_DIV");
                    final String sDate = rs.getString("S_DATE");
                    final String bankcd = rs.getString("BANKCD");
                    final String branchcd = rs.getString("BRANCHCD");
                    final String yokinDiv = rs.getString("YOKIN_DIV");
                    final String accountNo = rs.getString("ACCOUNT_NO");
                    final String bankMeigiSeiKana = rs.getString("BANK_MEIGI_SEI_KANA");
                    final String bankMeigiMeiKana = rs.getString("BANK_MEIGI_MEI_KANA");
                    final String bankMeigiSeiName = rs.getString("BANK_MEIGI_SEI_NAME");
                    final String bankMeigiMeiName = rs.getString("BANK_MEIGI_MEI_NAME");
                    final String zipcd = rs.getString("ZIPCD");
                    final String addr1 = rs.getString("ADDR1");
                    final String addr2 = rs.getString("ADDR2");
                    final String telno1 = rs.getString("TELNO1");
                    final String telno2 = rs.getString("TELNO2");
                    final KojinKouzaBankDat kojinkouzabankdat = new KojinKouzaBankDat(kojinNo, kouzaDiv, sDate, bankcd, branchcd, yokinDiv, accountNo,
                            bankMeigiSeiKana, bankMeigiMeiKana, bankMeigiSeiName, bankMeigiMeiName, zipcd, addr1, addr2, telno1, telno2);
                    kojinkouzabankdatMap.put(kojinNo, kojinkouzabankdat);
                }
           } catch (Exception ex) {
               log.fatal("exception!", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
           setBankMst(db2, kojinkouzabankdatMap);
           return kojinkouzabankdatMap;
        }

        private static void setBankMst(final DB2UDB db2, final Map kojinkouzabankdatMap) {
            final Set bankCds = new HashSet();
            for (final Iterator it = kojinkouzabankdatMap.values().iterator(); it.hasNext();) {
                final KojinKouzaBankDat dbd = (KojinKouzaBankDat) it.next();
                bankCds.add(dbd._bankcd + "-" + dbd._branchcd);
            }

            final Map bankMstMap = BankMst.load(db2, bankCds);

            for (final Iterator it = kojinkouzabankdatMap.values().iterator(); it.hasNext();) {
                final KojinKouzaBankDat dbd = (KojinKouzaBankDat) it.next();
                if (null != bankMstMap.get(dbd._bankcd + "-" + dbd._branchcd)) {
                    dbd._bankMst = (BankMst) bankMstMap.get(dbd._bankcd + "-" + dbd._branchcd);
                }
            }


        }

        public static String sql(final Collection kojinNos, final String date, final String kouzaDiv) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_SDATE AS ( ");
            stb.append("   SELECT KOJIN_NO, TAISHOUSHA_DIV, KOUZA_DIV, MAX(S_DATE) AS S_DATE ");
            stb.append("   FROM KOJIN_KOUZA_BANK_DAT T1 ");
            stb.append("   WHERE KOJIN_NO IN " + SQLUtils.whereIn(true, toArray(kojinNos)) + " ");
            stb.append("     AND TAISHOUSHA_DIV = '1' ");
            stb.append("     AND KOUZA_DIV = '" + kouzaDiv + "' ");
            stb.append("     AND S_DATE <= '" + date + "' ");
            stb.append("   GROUP BY KOJIN_NO, TAISHOUSHA_DIV, KOUZA_DIV ");
            stb.append(" ) ");
            stb.append(" SELECT T1.* ");
            stb.append(" FROM ");
            stb.append("   KOJIN_KOUZA_BANK_DAT T1 ");
            stb.append("   INNER JOIN MAX_SDATE T2 ON T2.KOJIN_NO = T1.KOJIN_NO AND T2.S_DATE = T1.S_DATE AND T2.TAISHOUSHA_DIV = T1.TAISHOUSHA_DIV AND T2.KOUZA_DIV = T1.KOUZA_DIV ");
            return stb.toString();
        }
    }

    private static class BankMst {
        final String _bankcd;
        final String _branchcd;
        final String _bankname;
        final String _banknameKana;
        final String _banknameRomaji;
        final String _branchname;
        final String _branchnameKana;
        final String _branchnameRomaji;
        final String _bankzipcd;
        final String _bankaddr1;
        final String _bankaddr2;
        final String _banktelno;
        final String _bankfaxno;
        final String _shuugakukinItakuCd;
        final String _shitakukinItakuCd;
        final String _saifurikaeDiv;
        final String _groupingDiv;
        final String _manageBranchcd;

        BankMst(
                final String bankcd,
                final String branchcd,
                final String bankname,
                final String banknameKana,
                final String banknameRomaji,
                final String branchname,
                final String branchnameKana,
                final String branchnameRomaji,
                final String bankzipcd,
                final String bankaddr1,
                final String bankaddr2,
                final String banktelno,
                final String bankfaxno,
                final String shuugakukinItakuCd,
                final String shitakukinItakuCd,
                final String saifurikaeDiv,
                final String groupingDiv,
                final String manageBranchcd
        ) {
            _bankcd = bankcd;
            _branchcd = branchcd;
            _bankname = bankname;
            _banknameKana = banknameKana;
            _banknameRomaji = banknameRomaji;
            _branchname = branchname;
            _branchnameKana = branchnameKana;
            _branchnameRomaji = branchnameRomaji;
            _bankzipcd = bankzipcd;
            _bankaddr1 = bankaddr1;
            _bankaddr2 = bankaddr2;
            _banktelno = banktelno;
            _bankfaxno = bankfaxno;
            _shuugakukinItakuCd = shuugakukinItakuCd;
            _shitakukinItakuCd = shitakukinItakuCd;
            _saifurikaeDiv = saifurikaeDiv;
            _groupingDiv = groupingDiv;
            _manageBranchcd = manageBranchcd;
        }

        public static Map load(final DB2UDB db2, final Collection bankCds) {
            Map bankMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(bankCds));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String bankcd = rs.getString("BANKCD");
                    final String branchcd = rs.getString("BRANCHCD");
                    final String bankname = rs.getString("BANKNAME");
                    final String banknameKana = rs.getString("BANKNAME_KANA");
                    final String banknameRomaji = rs.getString("BANKNAME_ROMAJI");
                    final String branchname = rs.getString("BRANCHNAME");
                    final String branchnameKana = rs.getString("BRANCHNAME_KANA");
                    final String branchnameRomaji = rs.getString("BRANCHNAME_ROMAJI");
                    final String bankzipcd = rs.getString("BANKZIPCD");
                    final String bankaddr1 = rs.getString("BANKADDR1");
                    final String bankaddr2 = rs.getString("BANKADDR2");
                    final String banktelno = rs.getString("BANKTELNO");
                    final String bankfaxno = rs.getString("BANKFAXNO");
                    final String shuugakukinItakuCd = rs.getString("SHUUGAKUKIN_ITAKU_CD");
                    final String shitakukinItakuCd = rs.getString("SHITAKUKIN_ITAKU_CD");
                    final String saifurikaeDiv = rs.getString("SAIFURIKAE_DIV");
                    final String groupingDiv = rs.getString("GROUPING_DIV");
                    final String manageBranchcd = rs.getString("MANAGE_BRANCHCD");
                    final BankMst bank = new BankMst(bankcd, branchcd, bankname, banknameKana, banknameRomaji, branchname, branchnameKana, branchnameRomaji,
                            bankzipcd, bankaddr1, bankaddr2, banktelno, bankfaxno, shuugakukinItakuCd, shitakukinItakuCd, saifurikaeDiv, groupingDiv, manageBranchcd);
                    bankMap.put(bankcd + "-" + branchcd, bank);
                }
           } catch (Exception ex) {
                log.fatal("exception!", ex);
           } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
           }
           return bankMap;
        }

        public static String sql(final Collection bankCds) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT * ");
            stb.append(" FROM BANK_MST ");
            stb.append(" WHERE BANKCD || '-' || BRANCHCD IN " + SQLUtils.whereIn(true, toArray(bankCds)) + " ");
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
        log.fatal("$Revision: 67228 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _shinseiYear;
        private final String _loginDate;
        private final String[] _schoolSelected;
        private final String _date;
        private final String _shiteiDate;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shinseiYear = request.getParameter("SHINSEI_YEAR");
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolSelected = request.getParameterValues("SCHOOL_SELECTED");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
            _date = _shugakuDate.d7toDateStr(request.getParameter("DATE"));
            _shiteiDate = _shugakuDate.d7toDateStr(request.getParameter("SHITEI_DATE"));
        }
    }
}

// eof

