/*
 * $Id: 0ef5eb90cd2719dbccf465119257bf25f91adcd5 $
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
 * 京都府修学金 修学資金貸付償還業務支出命令内訳書
 */
public class KNJTP129 {

    private static final Log log = LogFactory.getLog(KNJTP129.class);

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

        final List pageList = new ArrayList();
        List shinseiList0 = null;
        String furikomiDate = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
            if (null == furikomiDate || !furikomiDate.equals(shinsei._furikomiDate)) {
                shinseiList0 = new ArrayList();
                pageList.add(shinseiList0);
            }
            furikomiDate = shinsei._furikomiDate;
            shinseiList0.add(shinsei);
        }

        final int max = 12; // 1ページあたり行数
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
    }

    private void printShinsei(final Vrw32alp svf, final Collection shinseiList, int page, final int tpage, final int max) {
        svf.VrSetForm("KNJTP129.frm", 4);
        svf.VrsOut("YEAR", _param.formatNendo(_param._furikomiDate));

        svf.VrsOut("PAGE2", String.valueOf(tpage));
        int line = 0;
        BigDecimal totalKoufuShoriGk = new BigDecimal(0);
        BigDecimal totalFurikomiGk = new BigDecimal(0);
        BigDecimal totalShiharaizumiGk = new BigDecimal(0);
        int shoriCount = 0;
        final BigDecimal zero = new BigDecimal(0);
        for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
            final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();

            svf.VrsOut("PAYDAY", _param.formatDate(shinsei._furikomiDate));

            if (line % max == 0) {
                svf.VrsOut("PAGE", String.valueOf(page));
                page += 1;
            }

            svf.VrsOut("PNO", shinsei._shuugakuNo);
            svf.VrsOut("KIND", shinsei._rishiKoufuShinsei._shikinShubetsuName);

            if (null != shinsei._rishiKoufuShinsei && null != shinsei._rishiKoufuShinsei._shutaru) {
                final ShinkenshaHistDat shutaru = shinsei._rishiKoufuShinsei._shutaru;
                if (getMS932Length(shutaru.getName()) > 20) {
                    svf.VrsOut("NAME2", shutaru.getName());
                } else {
                    svf.VrsOut("NAME1", shutaru.getName());
                }
                svf.VrsOut("ZIP_NO", StringUtils.isBlank(shutaru._zipcd) ? "" : (""  +shutaru._zipcd));
                svf.VrsOut("ADDRESS1", shutaru._addr1);
                svf.VrsOut("ADDRESS2", shutaru._addr2);
            }

            final BigDecimal koufuSyoriGk = !NumberUtils.isDigits(shinsei._rishiKoufuShinsei._koufuShoriGk) ? zero : new BigDecimal(shinsei._rishiKoufuShinsei._koufuShoriGk);
            totalKoufuShoriGk = totalKoufuShoriGk.add(koufuSyoriGk);
            final BigDecimal furikomiGk = !NumberUtils.isDigits(shinsei._rishiKoufuShinsei._furikomiGk) ? zero : new BigDecimal(shinsei._rishiKoufuShinsei._furikomiGk);
            totalFurikomiGk = totalFurikomiGk.add(furikomiGk);
            final BigDecimal shiharaizumiGk = koufuSyoriGk.subtract(furikomiGk);
            totalShiharaizumiGk = totalShiharaizumiGk.add(shiharaizumiGk);

            svf.VrsOut("DECIDE_MONEY", koufuSyoriGk.toString());
            svf.VrsOut("THISTIME_MONEY", furikomiGk.toString());
            svf.VrsOut("ALREADY_MONEY", shiharaizumiGk.toString());

            shoriCount += 1;

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
            svf.VrsOut("CERTIF", "上記のとおり相違ないことを証明します。");
            svf.VrsOut("HOB_NAME", "京都府教育庁指導部高校教育課長");
            svf.VrsOut("STAMP_OUT", "○");
            svf.VrsOut("STAMP_IN", "印");
            svf.VrsOut("DATE", _param.formatDateBlankYearMonthDate(_param._date));
            svf.VrsOut("COUNT", String.valueOf(shoriCount));
            svf.VrsOut("ALL_DECIDE_MONEY", totalKoufuShoriGk.toString());
            svf.VrsOut("ALL_THISTIME_MONEY", totalFurikomiGk.toString());
            svf.VrsOut("ALL_ALREADY_MONEY", totalShiharaizumiGk.toString());
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

        final String _koufuSeq;
        final String _furikomiDate;

        KojinRishiKoufuShinseiDat _rishiKoufuShinsei = null;
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
                final String koufuSeq,
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

            _koufuSeq = koufuSeq;
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
            final int cmpSch = _furikomiDate.compareTo(o._furikomiDate);
            if (0 != cmpSch) {
                return cmpSch;
            }
            final int cmpSch2 = _shuugakuNo.compareTo(o._shuugakuNo);
            if (0 != cmpSch2) {
                return cmpSch2;
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

                     final String koufuSeq = rs.getString("KOUFU_SEQ");
                     final String furikomiDate = rs.getString("FURIKOMI_DATE");

                     final KojinShinseiHistDat shinsei = new KojinShinseiHistDat(kojinNo, shinseiYear, shikinShousaiDiv, issuedate, ukeYear, ukeNo, ukeEdaban, shinseiDate, shuugakuNo, nenrei, yoyakuKibouGk, sYoyakuKibouYm, eYoyakuKibouYm, sTaiyoYm, eTaiyoYm, shinseiDiv, keizokuKaisuu, heikyuuShougakuStatus1, heikyuuShougakuRemark1, heikyuuShougakuStatus2, heikyuuShougakuRemark2, heikyuuShougakuStatus3, heikyuuShougakuRemark3, shitakuCancelChokuFlg, shitakuCancelRiFlg, hSchoolCd, schoolName, katei, grade, entDate, hGradYm, shitakukinTaiyoDiv, heikyuuShitakuStatus1, heikyuuShitakuRemark1, heikyuuShitakuStatus2, heikyuuShitakuRemark2, heikyuuShitakuStatus3, heikyuuShitakuRemark3, yuushiFail, yuushiFailDiv, yuushiFailRemark, bankCd, yuushiCourseDiv, rentaiCd, shinken1Cd, shinken2Cd, shutaruCd, shinseiKanryouFlg, shinseiCancelFlg, ketteiDate, ketteiFlg,
                             koufuSeq, furikomiDate);
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
               setKojinRishiKoufuShinseiDat(db2, param, list);
               setKojinKouzaBankDat(db2, param, list);
               Collections.sort(list);
           }

           return list;
        }

        private static void setKojinRishiKoufuShinseiDat(final DB2UDB db2, final Param param, final List list) {
            final Collection keys = new HashSet();
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final String key = shinsei._shuugakuNo + "-" + shinsei._shinseiYear + "-" + shinsei._koufuSeq;
                keys.add(key);
            }
            final Map m = KojinRishiKoufuShinseiDat.load(db2, param, keys);
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final KojinShinseiHistDat shinsei = (KojinShinseiHistDat) it.next();
                final String key = shinsei._shuugakuNo + "-" + shinsei._shinseiYear + "-" + shinsei._koufuSeq;
                if (null != m.get(key)) {
                    shinsei._rishiKoufuShinsei = (KojinRishiKoufuShinseiDat) m.get(key);
                }
            }
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAIN AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.*, ");
            stb.append("         T2.KOUFU_SEQ, ");
            stb.append("         T2.SHIKIN_SHUBETSU, ");
            stb.append("         T3.FURIKOMI_DATE ");
            stb.append("     FROM ");
            stb.append("         KOJIN_SHINSEI_HIST_DAT T1 ");
            stb.append("         INNER JOIN KOJIN_RISHI_KOUFU_SHINSEI_DAT T2 ON T2.KOJIN_NO = T1.KOJIN_NO ");
            stb.append("           AND T2.SHINSEI_YEAR = T1.SHINSEI_YEAR ");
            stb.append("         INNER JOIN KOJIN_RISHI_FURIKOMI_DAT T3 ON T3.SHUUGAKU_NO = T2.SHUUGAKU_NO ");
            stb.append("           AND T3.SHINSEI_YEAR = T2.SHINSEI_YEAR ");
            stb.append("           AND T3.KOUFU_SEQ = T2.KOUFU_SEQ ");
            stb.append("           AND T3.FURIKOMI_DATE = '" + param._furikomiDate + "' ");
            stb.append("     WHERE ");
            stb.append("         (T1.SHIKIN_SHOUSAI_DIV = '04' OR T1.SHIKIN_SHOUSAI_DIV = '07') ");
            stb.append("         AND T1.H_SCHOOL_CD IN " + SQLUtils.whereIn(true, param._schoolSelected));
            stb.append("         AND T1.SHINSEI_CANCEL_FLG IS NULL ");
            stb.append("         AND T1.SHUUGAKU_NO IS NOT NULL ");
            stb.append("         AND T1.KETTEI_FLG = '1' ");
            stb.append("         AND T1.KETTEI_DATE IS NOT NULL ");
            stb.append("         AND T2.SHIKIN_SHUBETSU = '" + param._shikinShubetsu + "' ");
            stb.append("         AND T2.KOUFU_KETTEI_DATE BETWEEN '" + param._sKetteiDate + "' AND '" + param._eKetteiDate + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   NMT008.ABBV1 AS SHIKIN_SHUBETSU_NAME, ");
            stb.append("   L3.NAME AS SCHOOL_NAME ");
            stb.append(" FROM MAIN T1 ");
            stb.append(" LEFT JOIN SCHOOL_DAT L3 ON L3.SCHOOLCD = T1.H_SCHOOL_CD ");
            stb.append(" LEFT JOIN NAME_MST NMT008 ON NMT008.NAMECD1 = 'T008' AND NMT008.NAMECD2 = T1.SHIKIN_SHUBETSU ");
            return stb.toString();
        }


        private static void setKojinKouzaBankDat(final DB2UDB db2, final Param param, final Collection shinseiList) {
            final Set kojinNos = new HashSet();
            for (final Iterator it = shinseiList.iterator(); it.hasNext();) {
                final KojinShinseiHistDat taiyoyoyaku = (KojinShinseiHistDat) it.next();
                kojinNos.add(taiyoyoyaku._kojinNo);
            }

            final Map kouzaBankDatMap = KojinKouzaBankDat.load(db2, kojinNos);

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

        KojinRishiKoufuShinseiDat(
                final String shuugakuNo,
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
            _shuugakuNo = shuugakuNo;
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

        public static Map load(final DB2UDB db2, final Param param, final Collection keys) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                 final String sql = sql(keys);
//                 log.debug(" rishi koufu shinsei sql = " + sql);
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
            }
            return map;
        }

        private static Collection groupByLength(final Collection col, final int len) {
            final List rtn = new ArrayList();
            Collection col0 = null;
            for (final Iterator it = col.iterator(); it.hasNext();) {
                final String s = (String) it.next();
                if (null == col0 || col0.size() >= len) {
                    col0 = new HashSet();
                    rtn.add(col0);
                }
                col0.add(s);
            }
            return rtn;
        }

        private static void setShinkenshaHistDat(final DB2UDB db2, final Param param, final Map map) {
            final Collection shinkenCds = new HashSet();
            for (final Iterator it = map.values().iterator(); it.hasNext();) {
                final KojinRishiKoufuShinseiDat rishiKoufu = (KojinRishiKoufuShinseiDat) it.next();
                if (null != rishiKoufu._shutaruCd) {
                    shinkenCds.add(rishiKoufu._shutaruCd);
                }
            }
            final Collection groups = groupByLength(shinkenCds, 20); // SQLが長すぎる...
            for (final Iterator it1 = groups.iterator(); it1.hasNext();) {
                final Collection shinkenCds1 = (Collection) it1.next();
                final Map shinkenshaHists = ShinkenshaHistDat.load(db2, shinkenCds1);
                for (final Iterator it = map.values().iterator(); it.hasNext();) {
                    final KojinRishiKoufuShinseiDat rishiKoufu = (KojinRishiKoufuShinseiDat) it.next();
                    if (null != rishiKoufu._shutaruCd && null != shinkenshaHists.get(rishiKoufu._shutaruCd)) {
                        rishiKoufu._shutaru = (ShinkenshaHistDat) shinkenshaHists.get(rishiKoufu._shutaruCd);
                    }
                }
            }
        }

        public static String sql(final Collection keys) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     T2.FURIKOMI_DATE, ");
            stb.append("     T2.FURIKOMI_GK, ");
            stb.append("     NMT008.ABBV3 AS SHIKIN_SHUBETSU_NAME ");
            stb.append(" FROM KOJIN_RISHI_KOUFU_SHINSEI_DAT T1 ");
            stb.append(" INNER JOIN KOJIN_RISHI_FURIKOMI_DAT T2 ON T2.SHUUGAKU_NO = T1.SHUUGAKU_NO ");
            stb.append("     AND T2.SHINSEI_YEAR = T1.SHINSEI_YEAR ");
            stb.append("     AND T2.KOUFU_SEQ = T1.KOUFU_SEQ ");
            stb.append(" INNER JOIN NAME_MST NMT008 ON NMT008.NAMECD1 = 'T008' ");
            stb.append("     AND NMT008.NAMECD2 = T1.SHIKIN_SHUBETSU ");
            stb.append(" WHERE ");
            stb.append("      T1.SHUUGAKU_NO || '-' || T1.SHINSEI_YEAR || '-' || T1.KOUFU_SEQ IN " + SQLUtils.whereIn(true, toArray(keys)) + " ");
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

            _kouzaDivName = FURIKOMI.equals(_kouzaDiv) || "3".equals(_kouzaDiv) ? "振込" : FURIKAE.equals(_kouzaDiv) ? "振替" : "";
        }

        public String getMeigiName() {
            return StringUtils.defaultString(_bankMeigiSeiName) + "　" +  StringUtils.defaultString(_bankMeigiMeiName);
        }

        public String getMeigiKana() {
            return StringUtils.defaultString(_bankMeigiSeiKana) + "　" +  StringUtils.defaultString(_bankMeigiMeiKana);
        }

        public static Map load(final DB2UDB db2, final Collection kojinNos) {
            Map kojinkouzabankdatMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(kojinNos, "3");
//                log.debug(" kouza bank sql = " + sql);
                ps = db2.prepareStatement(sql);
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

        public static String sql(final Collection kojinNos, final String kouzaDiv) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_SDATE AS ( ");
            stb.append("   SELECT KOJIN_NO, TAISHOUSHA_DIV, KOUZA_DIV, MAX(S_DATE) AS S_DATE ");
            stb.append("   FROM KOJIN_KOUZA_BANK_DAT T1 ");
            stb.append("   WHERE KOJIN_NO IN " + SQLUtils.whereIn(true, toArray(kojinNos)) + " ");
            stb.append("     AND TAISHOUSHA_DIV = '1' ");
            stb.append("     AND KOUZA_DIV = '" + kouzaDiv + "' ");
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
                final String sql = sql(shinkenCdSet);
//                log.debug(" shinken sql = " + sql);
                ps = db2.prepareStatement(sql);
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
            stb.append(" SELECT * ");
            stb.append(" FROM SHINKENSHA_HIST_DAT T1 ");
            stb.append(" WHERE T1.SHINKEN_CD IN " + SQLUtils.whereIn(false, toArray(shinkenCdSet)) + " ");
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
        log.fatal("$Revision: 67229 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _shikinShubetsu;
        private final String _loginDate;
        private final String[] _schoolSelected;
        private final String _sKetteiDate;
        private final String _eKetteiDate;
        private final String _furikomiDate;
        private final String _date;
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _shikinShubetsu = request.getParameter("SHIKIN_SHUBETSU");
            _loginDate = request.getParameter("LOGIN_DATE");
            _shugakuDate = new ShugakuDate(db2);
            _schoolSelected = request.getParameterValues("SCHOOL_SELECTED");
            _sKetteiDate = _shugakuDate.d7toDateStr(request.getParameter("S_KETTEI_DATE"));
            _eKetteiDate = _shugakuDate.d7toDateStr(request.getParameter("E_KETTEI_DATE"));
            _furikomiDate = _shugakuDate.d7toDateStr(request.getParameter("FURIKOMI_DATE"));
            _date = _shugakuDate.d7toDateStr(request.getParameter("DATE"));
        }

        public String formatNendo(final String date) {
            final String[] rtn = _shugakuDate.nengoNenTukiHi(date.substring(0, 4) + "-04-01");
            return rtn[0] + rtn[1] + "年度";
        }

        public String formatDateBlankYearMonthDate(final String date) {
            final String[] rtn = _shugakuDate.nengoNenTukiHi(date);
            return rtn[0] + "    " + "年" + "    " + "月" + "    " + "日";
        }

        public String formatDate(final String date) {
            final String[] rtn = _shugakuDate.nengoNenTukiHi(date);
            return rtn[0] + " " + rtn[1] + "年" + rtn[2] + "月" + rtn[3] + "日";
        }
    }
}

// eof

