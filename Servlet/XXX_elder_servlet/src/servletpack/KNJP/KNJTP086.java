/*
 * $Id: 0ce10f0381787e24453a0a7b5dc4c685d30c1f55 $
 * 修学資金貸与申請認定計算書(審査計算書1)
 *
 * 作成日: 2012/09/08
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.ShugakuDate;

public class KNJTP086 {

    private static final Log log = LogFactory.getLog(KNJTP086.class);

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        try {
            final Kojin kojin = new Kojin(_param._kojinNo);
            kojin.load(db2, _param);
            
            if (null == kojin._kojinHist) {
                return;
            }
            svf.VrSetForm("KNJTP086.frm", 1);

            printName(svf, kojin);

            printShotokuKijunGaku(svf, db2, kojin);

            printBank(svf, kojin);

            svf.VrEndPage();
            _hasData = true;

        } catch (Exception ex) {
            log.fatal("Exception:", ex);
        }
    }
    
    private String getShinkenCdForPrint(final String shinkenCd) {
        if (null == shinkenCd || shinkenCd.length() < 9) {
            return shinkenCd;
        }
        return shinkenCd.substring(0, 7) + "-" + shinkenCd.substring(7);
    }
    
    private String getNenreiStr(final String nenrei) {
        return "   ( " + StringUtils.defaultString(nenrei, "  ") + " 歳)";
    }
    
    private void printName(final Vrw32alp svf, final Kojin kojin) {
        svf.VrsOut("KANA1", kojin._kojinHist.getKana());
        svf.VrsOut("NAME1", kojin._kojinHist.getName());
        svf.VrsOut("PCODE1", kojin._kojinHist._kojinNo);
        String nenrei = getNenreiStr(null);
        if (_param.isTaiyoyoyaku() && null != kojin._taiyoyoyaku) {
            nenrei = getNenreiStr(kojin._taiyoyoyaku._nenrei);
        } else if (!_param.isTaiyoyoyaku() && null != kojin._shinsei){
            nenrei = getNenreiStr(kojin._shinsei._nenrei);
        }
        svf.VrsOut("BIRTHDAY1", _param._shugakuDate.formatDate(kojin._kojinHist._birthday) + nenrei);

        svf.VrsOut("ZIPNO1", kojin._kojinHist._zipcd);
        svf.VrsOut("ADDRESS1_1", kojin._kojinHist._addr1);
        svf.VrsOut("ADDRESS1_2", kojin._kojinHist._addr2);
        svf.VrsOut("CITY_CD1", kojin._kojinHist._citycd);

        svf.VrsOut("TELNO1_1", kojin._kojinHist._telno1);
        svf.VrsOut("TELNO1_2", kojin._kojinHist._telno2);

        svf.VrsOut("ATTEND_UNIT", kojin._kojinHist._tsuugakuDivName);
        
        String yoyakuKibouGk = null;
        String sYoyakuKibouYm = null;
        String eYoyakuKibouYm = null;

        ShinkenshaHistDat rentai = null;
        ShinkenshaHistDat shinken1 = null;
        ShinkenshaHistDat shinken2 = null;

        String shinseiyear = null;
        if (_param.isTaiyoyoyaku() && null != kojin._taiyoyoyaku) {
            final KojinTaiyoyoyakuHistDat taiyoyoyaku = kojin._taiyoyoyaku;
            shinseiyear = taiyoyoyaku._shinseiYear;

            yoyakuKibouGk = taiyoyoyaku._yoyakuKibouGk;
            sYoyakuKibouYm = taiyoyoyaku._sYoyakuKibouYm;
            eYoyakuKibouYm = taiyoyoyaku._eYoyakuKibouYm;

            rentai = taiyoyoyaku._rentai;
            shinken1 = taiyoyoyaku._shinken1;
            shinken2 = taiyoyoyaku._shinken2;
            
        } else if (!_param.isTaiyoyoyaku() && null != kojin._shinsei) {
            final KojinShinseiHistDat shinsei = kojin._shinsei;
            shinseiyear = shinsei._shinseiYear;
            if (null != shinsei._schoolDat) {
                svf.VrsOut("SCHOOL_NAME", shinsei._schoolDat._name);
            }
            svf.VrsOut("COURSE_NAME", shinsei._kateiName);
            svf.VrsOut("GRADE", shinsei.getGradeStr());

            yoyakuKibouGk = shinsei._yoyakuKibouGk;
            sYoyakuKibouYm = shinsei._sYoyakuKibouYm;
            eYoyakuKibouYm = shinsei._eYoyakuKibouYm;
            
            if (null != shinsei._heikyuuShougakuStatus1) {
                svf.VrsOut("SUPPLY_SITU", "受給していない");
                svf.VrsOut("ANOTHER_SCHOLAR1", shinsei._heikyuuShougakuRemark1);
            }
            if (null != shinsei._heikyuuShougakuStatus2) {
                svf.VrsOut("SUPPLY_SITU", "受給中");
                svf.VrsOut("ANOTHER_SCHOLAR1", shinsei._heikyuuShougakuRemark2);
            }
            if (null != shinsei._heikyuuShougakuStatus3) {
                svf.VrsOut("SUPPLY_SITU", "申請中");
                svf.VrsOut("ANOTHER_SCHOLAR1", shinsei._heikyuuShougakuRemark3);
            }
            rentai = shinsei._rentai;
            shinken1 = shinsei._shinken1;
            shinken2 = shinsei._shinken2;
        }

        svf.VrsOut("LOAN_HOPE_MONEY", yoyakuKibouGk);
        svf.VrsOut("LOAN_FDATE", _param._shugakuDate.formatNentuki(sYoyakuKibouYm));
        svf.VrsOut("LOAN_TDATE", _param._shugakuDate.formatNentuki(eYoyakuKibouYm));

        svf.VrsOut("BIRTHDAY2", _param._shugakuDate.formatDate(null) + getNenreiStr(null));
        if (null != rentai) {
            svf.VrsOut("KANA2", rentai.getKana());
            svf.VrsOut("NAME2", rentai.getName());
            svf.VrsOut("PCODE2", getShinkenCdForPrint(rentai._shinkenCd));
            final String rentaiNenrei = getNenreiStr(calcNenrei(rentai._birthday, shinseiyear));
            svf.VrsOut("BIRTHDAY2", _param._shugakuDate.formatDate(rentai._birthday) + rentaiNenrei);
            svf.VrsOut("TELNO2_1", rentai._telno1);
            svf.VrsOut("TELNO2_2", rentai._telno2);
            svf.VrsOut("ZIPNO2", rentai._zipcd);
            svf.VrsOut("ADDRESS2_1", rentai._addr1);
            svf.VrsOut("ADDRESS2_2", rentai._addr2);
            svf.VrsOut("CITY_CD2", rentai._citycd);
        }
        if (null != shinken1) {
            svf.VrsOut("KANA4", shinken1.getKana());
            svf.VrsOut("NAME4", shinken1.getName());
            svf.VrsOut("PCODE4", getShinkenCdForPrint(shinken1._shinkenCd));
        }
        if (null != shinken2) {
            svf.VrsOut("KANA5", shinken2.getKana());
            svf.VrsOut("NAME5", shinken2.getName());
        }
    }

    // 申請年度の4月1日時点の年齢（誕生日を迎える前の年齢）
    private String calcNenrei(final String birthday, final String shinseiyear) {
        if (null != birthday && null != shinseiyear) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(birthday));
            final int birthNendo = cal.get(Calendar.YEAR) - ((cal.get(Calendar.MONTH) <= Calendar.MARCH) ? 1 : 0);  
            return String.valueOf(Integer.parseInt(shinseiyear) - birthNendo - 1);
        }
        return null;
    }

    private void printShotokuKijunGaku(final Vrw32alp svf, final DB2UDB db2, final Kojin kojin) {
        // A.所得基準額
        final KojinTaiyoShinsaKekkaDat kekka = kojin._shinsaKekka;
        String kyuuchiKasanGk = "0";
        String boshiFushiSetaiGk = "0";
        String shougaiGk = "0";
        String shotokuKijunGk = null;
        if (null != kekka) {
            svf.VrsOut("FAMILY2", kekka._setaiCount);
            svf.VrsOutn("ORD_INCOME_STANDARD", 1, kekka._taiyoSetaiKijunDatShotokuKijyunGk); 

            if (null != kekka._taiyoKasan2Dat) {
                svf.VrsOut("MF_FAMILY_INCOME", kekka._taiyoKasan2Dat._boshiFushiSetaiGk);
                svf.VrsOut("DISP_INCOME", kekka._taiyoKasan2Dat._shougaishaGk);
                if ("1".equals(kekka._boshiFushiSetaiFlg)) {
                    boshiFushiSetaiGk = kekka._taiyoKasan2Dat._boshiFushiSetaiGk; // 母子・父子
                }
            }
            svf.VrsOutn("ORD_INCOME_STANDARD", 2, boshiFushiSetaiGk); // 母子・父子

            if ("1".equals(kekka._shougaishaFlg)) {
                shougaiGk = kekka.shougaiGk(); // 障害者
            }
            svf.VrsOutn("ORD_INCOME_STANDARD", 3, shougaiGk); // 障害者

            svf.VrsOut("DISP_NUM", kekka._shougaishaCount); // 障害者数
            svf.VrsOutn("ORD_INCOME_STANDARD", 4, kekka._longRyouyoushaGk); // 長期療養者
            
            // 地域調整
            if (_param.isTaiyoyoyaku() && null != kojin._taiyoyoyaku) {
                if (null != kojin._taiyoyoyaku._shinken1) {
                    svf.VrsOut("CITY_CD", kojin._taiyoyoyaku._shinken1._citycd);
                    final String kyuuchiCd = getKyuuchiCd(db2, kojin._taiyoyoyaku._shinseiYear, kojin._taiyoyoyaku._shinken1._citycd);
                    kyuuchiKasanGk = kekka.getKyuuchiKasanGk(db2, kyuuchiCd);
                }
            } else if (!_param.isTaiyoyoyaku() && null != kojin._shinsei) {
                if (null != kojin._shinsei._shinken1) {
                    svf.VrsOut("CITY_CD", kojin._shinsei._shinken1._citycd);
                    final String kyuuchiCd = getKyuuchiCd(db2, kojin._shinsei._shinseiYear, kojin._shinsei._shinken1._citycd);
                    kyuuchiKasanGk = kekka.getKyuuchiKasanGk(db2, kyuuchiCd);
                }
            }
            if ("1".equals(kekka._areaChousaFlg)) {
                svf.VrsOutn("ORD_INCOME_STANDARD", 5, kyuuchiKasanGk);
            } else {
                svf.VrsOutn("ORD_INCOME_STANDARD", 5, "0");
            }

            // 所得基準額
            shotokuKijunGk = kekka.shotokuKijunGk(boshiFushiSetaiGk, shougaiGk, kyuuchiKasanGk);
            svf.VrsOut("INCOME_STANDARD3", shotokuKijunGk);
        }
        
        // B.総収入認定額
        int i = 1;
        for (final Iterator it = kojin._setaiList.iterator();it.hasNext();) {
            final KojinTaiyoSetaiDat setai = (KojinTaiyoSetaiDat) it.next();

            svf.VrsOutn("NAME3", i, setai.getName());
            svf.VrsOutn("PNO", i, setai._kyoudaiKojinNo);
            svf.VrsOutn("RELATION", i, setai._tsuzukigaraName);
            svf.VrsOutn("OLD", i, setai._nenrei);
            svf.VrsOutn("INCOME_KIND", i, setai._shotokuCdName);
            svf.VrsOutn("INCOME", i, setai._shotokuGk);
            svf.VrsOutn("INCOME_DEDU", i, setai._koujoFuboIgai);
            svf.VrsOutn("SOCIAL_INSURANCE", i, setai._koujoHoken);
            svf.VrsOutn("INCOME_TAX", i, setai._koujoShotoku);
            svf.VrsOutn("INCOME_JUDGE", i, setai._ninteiGk);
            i += 1;
        }

        if (null != kekka) {
            final String B = kekka._ninteiSoushunyuGk;
            svf.VrsOut("TOTAL_INCOME_JUDGE", B);
            
            final String A = shotokuKijunGk;
            svf.VrsOut("TOTAL_INCOME_STANDARD", A);

            if (!StringUtils.isBlank(A) && !StringUtils.isBlank(B)) {
                if (toLong(B) <= toLong(A)) {
                    svf.VrsOut("TOTAL_COMPARE", "≦");
                } else {
                    svf.VrsOut("TOTAL_COMPARE", "≧");
                }
                String hanteiKekka = "";
                if (!"9".equals(kekka._setaiStatus) || toLong(B) <= toLong(A)) {
                    hanteiKekka = "審査ＯＫ";
                } else {
                    hanteiKekka = "審査ＮＧ";
                }
                svf.VrsOut("TOTAL_LOAN_STANDARD", hanteiKekka);
            }
        }
    }

    private static long toLong(final String s) {
        return Long.parseLong(StringUtils.defaultString(s, "0"));
    }

    private void printBank(final Vrw32alp svf, final Kojin kojin) {
        if (null != kojin._bank) {
            if (null != kojin._bank._bankMst) {
                svf.VrsOut("BANK_NAME", kojin._bank._bankMst._bankname);
                svf.VrsOut("BANK_CD", kojin._bank._bankMst._bankcd);
                svf.VrsOut("BRANCH_NAME", kojin._bank._bankMst._branchname);
                svf.VrsOut("BRANCH_CD", kojin._bank._bankMst._branchcd);
            }
            svf.VrsOut("ITEM", kojin._bank._yokinDivName);
            svf.VrsOut("AC_NUMBER", kojin._bank._accountNo);
            svf.VrsOut("AC_NAME", kojin._bank.getMeigiKana());
        }
    }
    
    public String getKyuuchiCd(final DB2UDB db2, final String year, final String citycd) {
        String kyuuchiCd = null;
        final String sql = " SELECT KYUUCHI_CD FROM KYUUCHI_DAT WHERE YEAR = '" + year + "' AND CITYCD = '" + citycd + "' ";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                kyuuchiCd = rs.getString("KYUUCHI_CD");
            }
            
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return kyuuchiCd;
    }

    private static class Kojin {
        final String _kojinNo;
        KojinHistDat _kojinHist = null;
        KojinTaiyoyoyakuHistDat _taiyoyoyaku = null;
        KojinShinseiHistDat _shinsei = null;
        KojinTaiyoShinsaKekkaDat _shinsaKekka = null;
        List _setaiList = Collections.EMPTY_LIST;
        KojinKouzaBankDat _bank = null;

        Kojin(final String kojinNo) {
            _kojinNo = kojinNo;
        }
        
        public void load(final DB2UDB db2, final Param param) {
            _kojinHist = KojinHistDat.load(db2, param);
            final String issuedate;
            final String setaiSeq;
            if (param.isTaiyoyoyaku()) {
                _taiyoyoyaku = KojinTaiyoyoyakuHistDat.load(db2, param);
                issuedate = _taiyoyoyaku._yoyakuShinseiDate;
                setaiSeq = _taiyoyoyaku._setaiSeq;
            } else {
                _shinsei = KojinShinseiHistDat.load(db2, param);
                issuedate = _shinsei._issuedate;
                setaiSeq = _shinsei._setaiSeq;
            }
            _shinsaKekka = KojinTaiyoShinsaKekkaDat.load(db2, param, issuedate, setaiSeq);
            _setaiList = KojinTaiyoSetaiDat.load(db2, param, setaiSeq);
            _bank = KojinKouzaBankDat.load(db2, param);
        }
    }
    
    private static class KojinShinseiHistDat { 
        final String _kojinNo;
        final String _shinseiYear;
        final String _ukeYear;
        final String _ukeNo;
        final String _ukeEdaban;
        final String _issuedate;
        final String _shinseiDate;
        final String _shuugakuNo;
        final String _nenrei;
        final String _yoyakuKibouGk;
        final String _sYoyakuKibouYm;
        final String _eYoyakuKibouYm;
        final String _sTaiyoYm;
        final String _eTaiyoYm;
        final String _shikinShousaiDiv;
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
        final String _katei;
        final String _grade;
        final String _entDate;
        final String _hGradYm;
        final String _shitakukinTaiyoDiv;
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
        final String _kateiName;
        
        SchoolDat _schoolDat = null;
        
        ShinkenshaHistDat _rentai = null;
        ShinkenshaHistDat _shinken1 = null;
        ShinkenshaHistDat _shinken2 = null;
        ShinkenshaHistDat _shutaru = null;

        KojinShinseiHistDat(
                final String kojinNo,
                final String shinseiYear,
                final String ukeYear,
                final String ukeNo,
                final String ukeEdaban,
                final String issuedate,
                final String shinseiDate,
                final String shuugakuNo,
                final String nenrei,
                final String yoyakuKibouGk,
                final String sYoyakuKibouYm,
                final String eYoyakuKibouYm,
                final String sTaiyoYm,
                final String eTaiyoYm,
                final String shikinShousaiDiv,
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
                final String katei,
                final String grade,
                final String entDate,
                final String hGradYm,
                final String shitakukinTaiyoDiv,
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
                final String kateiName
        ) { 
           _kojinNo = kojinNo;
           _shinseiYear = shinseiYear;
           _ukeYear = ukeYear;
           _ukeNo = ukeNo;
           _ukeEdaban = ukeEdaban;
           _issuedate = issuedate;
           _shinseiDate = shinseiDate;
           _shuugakuNo = shuugakuNo;
           _nenrei = nenrei;
           _yoyakuKibouGk = yoyakuKibouGk;
           _sYoyakuKibouYm = sYoyakuKibouYm;
           _eYoyakuKibouYm = eYoyakuKibouYm;
           _sTaiyoYm = sTaiyoYm;
           _eTaiyoYm = eTaiyoYm;
           _shikinShousaiDiv = shikinShousaiDiv;
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
           _katei = katei;
           _grade = grade;
           _entDate = entDate;
           _hGradYm = hGradYm;
           _shitakukinTaiyoDiv = shitakukinTaiyoDiv;
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
           _kateiName = kateiName;
        }
        
        public String getGradeStr() {
            return (NumberUtils.isDigits(_grade)) ? String.valueOf(Integer.parseInt(_grade)) : _grade;
        }
        
        public static KojinShinseiHistDat load(final DB2UDB db2, final Param param) {
            KojinShinseiHistDat shinsei = null;
            PreparedStatement ps = null; 
            ResultSet rs = null; 
            try { 
                final String sql = sql(param);
                ps = db2.prepareStatement(sql); 
                rs = ps.executeQuery(); 
                while (rs.next()) { 
                    final String kojinNo = rs.getString("KOJIN_NO");
                    final String shinseiYear = rs.getString("SHINSEI_YEAR");
                    final String ukeYear = rs.getString("UKE_YEAR");
                    final String ukeNo = rs.getString("UKE_NO");
                    final String ukeEdaban = rs.getString("UKE_EDABAN");
                    final String issuedate = rs.getString("ISSUEDATE");
                    final String shinseiDate = rs.getString("SHINSEI_DATE");
                    final String shuugakuNo = rs.getString("SHUUGAKU_NO");
                    final String nenrei = rs.getString("NENREI");
                    final String yoyakuKibouGk = rs.getString("YOYAKU_KIBOU_GK");
                    final String sYoyakuKibouYm = rs.getString("S_YOYAKU_KIBOU_YM");
                    final String eYoyakuKibouYm = rs.getString("E_YOYAKU_KIBOU_YM");
                    final String sTaiyoYm = rs.getString("S_TAIYO_YM");
                    final String eTaiyoYm = rs.getString("E_TAIYO_YM");
                    final String shikinShousaiDiv = rs.getString("SHIKIN_SHOUSAI_DIV");
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
                    final String katei = rs.getString("KATEI");
                    final String grade = rs.getString("GRADE");
                    final String entDate = rs.getString("ENT_DATE");
                    final String hGradYm = rs.getString("H_GRAD_YM");
                    final String shitakukinTaiyoDiv = rs.getString("SHITAKUKIN_TAIYO_DIV");
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
                    final String kateiName = rs.getString("KATEI_NAME");
                    shinsei = new KojinShinseiHistDat(kojinNo, shinseiYear, ukeYear, ukeNo, ukeEdaban,
                            issuedate, shinseiDate, shuugakuNo, nenrei, yoyakuKibouGk, sYoyakuKibouYm, eYoyakuKibouYm, sTaiyoYm, eTaiyoYm, shikinShousaiDiv, shinseiDiv, setaiSeq, keizokuKaisuu, heikyuuShougakuStatus1, heikyuuShougakuRemark1, heikyuuShougakuStatus2, heikyuuShougakuRemark2, heikyuuShougakuStatus3, heikyuuShougakuRemark3, shitakuCancelChokuFlg, shitakuCancelRiFlg, hSchoolCd, katei, grade, entDate, hGradYm, shitakukinTaiyoDiv, yuushiFail, yuushiFailDiv, yuushiFailRemark,
                            bankCd, yuushiCourseDiv, rentaiCd, shinken1Cd, shinken2Cd, shutaruCd, shinseiKanryouFlg, shinseiCancelFlg, ketteiDate, ketteiFlg, kateiName);
                }
            } catch (Exception ex) { 
                log.fatal("exception!", ex); 
            } finally { 
                DbUtils.closeQuietly(null, ps, rs); 
                db2.commit(); 
            }
            if (null != shinsei) {
                shinsei._schoolDat = SchoolDat.load(db2, shinsei._hSchoolCd);
                
                shinsei._rentai = ShinkenshaHistDat.load(db2, shinsei._rentaiCd);
                shinsei._shinken1 = ShinkenshaHistDat.load(db2, shinsei._shinken1Cd);
                shinsei._shinken2 = ShinkenshaHistDat.load(db2, shinsei._shinken2Cd);
                shinsei._shutaru = ShinkenshaHistDat.load(db2, shinsei._shutaruCd);
            }
            return shinsei;
        }

        public static String sql(final Param param) { 
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.*, T2.KATEI_NAME ");
            stb.append(" FROM KOJIN_SHINSEI_HIST_DAT T1 ");
            stb.append(" LEFT JOIN KATEI_MST T2 ON T2.KATEI = T1.KATEI ");
            stb.append(" WHERE KOJIN_NO = '" + param._kojinNo + "' ");
            stb.append("   AND SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            stb.append("   AND SHIKIN_SHOUSAI_DIV = '" + param._shikinShousaiDiv + "' ");
            stb.append("   AND UKE_YEAR = '" + param._ukeYear + "' ");
            stb.append("   AND UKE_NO = '" + param._ukeNo + "' ");
            stb.append("   AND UKE_EDABAN = '" + param._ukeEdaban + "' ");
            stb.append("   AND ISSUEDATE = '" + param._issueDate + "' ");
            return stb.toString();
        }
    }
    
    private static class KojinTaiyoyoyakuHistDat {
        final String _kojinNo;
        final String _shinseiYear;
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
        final String _shikinShousaiDiv;
        final String _shinseiDiv;
        final String _setaiSeq;
        final String _rentaiCd;
        final String _shinken1Cd;
        final String _shinken2Cd;
        final String _shutaruCd;
        final String _shinseiKanryouFlg;
        final String _shinseiCancelFlg;
        final String _shinseiCancelDate;
        final String _ketteiDate;
        final String _ketteiFlg;
        
        ShinkenshaHistDat _rentai = null;
        ShinkenshaHistDat _shinken1 = null;
        ShinkenshaHistDat _shinken2 = null;
        ShinkenshaHistDat _shutaru = null;

        KojinTaiyoyoyakuHistDat(
                final String kojinNo,
                final String shinseiYear,
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
                final String shikinShousaiDiv,
                final String shinseiDiv,
                final String setaiSeq,
                final String rentaiCd,
                final String shinken1Cd,
                final String shinken2Cd,
                final String shutaruCd,
                final String shinseiKanryouFlg,
                final String shinseiCancelFlg,
                final String shinseiCancelDate,
                final String ketteiDate,
                final String ketteiFlg
        ) {
            _kojinNo = kojinNo;
            _shinseiYear = shinseiYear;
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
            _shikinShousaiDiv = shikinShousaiDiv;
            _shinseiDiv = shinseiDiv;
            _setaiSeq = setaiSeq;
            _rentaiCd = rentaiCd;
            _shinken1Cd = shinken1Cd;
            _shinken2Cd = shinken2Cd;
            _shutaruCd = shutaruCd;
            _shinseiKanryouFlg = shinseiKanryouFlg;
            _shinseiCancelFlg = shinseiCancelFlg;
            _shinseiCancelDate = shinseiCancelDate;
            _ketteiDate = ketteiDate;
            _ketteiFlg = ketteiFlg;
        }

        public static KojinTaiyoyoyakuHistDat load(final DB2UDB db2, final Param param) {
            KojinTaiyoyoyakuHistDat taiyoyoyaku = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kojinNo = rs.getString("KOJIN_NO");
                    final String shinseiYear = rs.getString("SHINSEI_YEAR");
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
                    final String shikinShousaiDiv = rs.getString("SHIKIN_SHOUSAI_DIV");
                    final String shinseiDiv = rs.getString("SHINSEI_DIV");
                    final String setaiSeq = rs.getString("SETAI_SEQ");
                    final String rentaiCd = rs.getString("RENTAI_CD");
                    final String shinken1Cd = rs.getString("SHINKEN1_CD");
                    final String shinken2Cd = rs.getString("SHINKEN2_CD");
                    final String shutaruCd = rs.getString("SHUTARU_CD");
                    final String shinseiKanryouFlg = rs.getString("SHINSEI_KANRYOU_FLG");
                    final String shinseiCancelFlg = rs.getString("SHINSEI_CANCEL_FLG");
                    final String shinseiCancelDate = rs.getString("SHINSEI_CANCEL_DATE");
                    final String ketteiDate = rs.getString("KETTEI_DATE");
                    final String ketteiFlg = rs.getString("KETTEI_FLG");

                    taiyoyoyaku = new KojinTaiyoyoyakuHistDat(kojinNo, shinseiYear, ukeYear, ukeNo, ukeEdaban, yoyakuShinseiDate, shuugakuNo, nenrei, kikonFlg, jSchoolCd, jGradDiv, jGradYm, kibouHSchoolDiv, yoyakuKibouGk, sYoyakuKibouYm, eYoyakuKibouYm, shitakukinKibouFlg, shikinShousaiDiv, shinseiDiv, setaiSeq,
                            rentaiCd, shinken1Cd, shinken2Cd, shutaruCd, shinseiKanryouFlg, shinseiCancelFlg, shinseiCancelDate, ketteiDate, ketteiFlg);
                }
           } catch (Exception ex) {
                log.fatal("exception!", ex);
           } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
           }
           if (null != taiyoyoyaku) {
               taiyoyoyaku._rentai = ShinkenshaHistDat.load(db2, taiyoyoyaku._rentaiCd);
               taiyoyoyaku._shinken1 = ShinkenshaHistDat.load(db2, taiyoyoyaku._shinken1Cd);
               taiyoyoyaku._shinken2 = ShinkenshaHistDat.load(db2, taiyoyoyaku._shinken2Cd);
               taiyoyoyaku._shutaru = ShinkenshaHistDat.load(db2, taiyoyoyaku._shutaruCd);
           }
           return taiyoyoyaku;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT * ");
            stb.append(" FROM  ");
            stb.append(" KOJIN_TAIYOYOYAKU_HIST_DAT ");
            stb.append(" WHERE KOJIN_NO = '" + param._kojinNo + "' ");
            stb.append("   AND SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            stb.append("   AND UKE_YEAR = '" + param._ukeYear + "' ");
            stb.append("   AND UKE_NO = '" + param._ukeNo + "' ");
            stb.append("   AND UKE_EDABAN = '" + param._ukeEdaban + "' ");
            return stb.toString();
        }
    }


    private static class KojinTaiyoShinsaKekkaDat {
        final String _kojinNo;
        final String _shinseiYear;
        final String _kekkaDiv;
        final String _shinsaKekka;
        final String _sshotokuKijunSetaiCount;
        final String _sshotokuKijunGk;
        final String _syearSalaryGk;
        final String _syearNotsalaryGk;
        final String _syearKoujyoGk;
        final String _syearTokuKoujyoGk;
        final String _syearShotokuNinteiGk;
        final String _sninteiKekkaFlg;
        final String _setaiStatus;
        final String _setaiCount;
        final String _boshiFushiSetaiFlg;
        final String _juniorUnderFlg;
        final String _shougaishaFlg;
        final String _shougaishaCount;
        final String _kakuninReport;
        final String _rounenshaFlg;
        final String _rounenshaCount;
        final String _longRyouyoushaGk;
        final String _areaChousaFlg;
        final String _kyuuchi;
        final String _areaChousaCount;
        final String _shotokuKijyunGk;
        final String _ninteiSoushunyuGk;
        final String _taiyoHanteiKekka;
        final String _ninteiHanteiKekka;
        final String _tboshiFlg;
        final String _tsyougakuCount;
        final String _tchuugakuCount;
        final String _tkoukouJiCount;
        final String _tkoukouJigaiCount;
        final String _tkoukouShiJiCount;
        final String _tkoukouShiJigaiCount;
        final String _tsenmonKoJiCount;
        final String _tsenmonKoJigaiCount;
        final String _tsenmonShiJiCount;
        final String _tsenmonShiJigaiCount;
        final String _tdaigakuKoJiCount;
        final String _tdaigakuKoJigaiCount;
        final String _tdaigakuShiJiCount;
        final String _tdaigakuShiJigaiCount;
        final String _tsensyuKoKoJiCount;
        final String _tsensyuKoKoJigaiCount;
        final String _tsensyuKoShiJiCount;
        final String _tsensyuKoShiJigaiCount;
        final String _tsensyuSenKoJiCount;
        final String _tsensyuSenKoJigaiCount;
        final String _tsensyuSenShiJiCount;
        final String _tsensyuSenShiJigaiCount;
        final String _tshougaiCount;
        final String _tlongRyouyouGk;
        final String _tbekkyoGk;
        final String _tsaigaiGk;
        final String _tsonotaGk;
        final String _tsonotaRemark;
        final String _taiyoSetaiKijunDatShotokuKijyunGk;
        
        TaiyoKasan2Dat _taiyoKasan2Dat = null;

        KojinTaiyoShinsaKekkaDat(
                final String kojinNo,
                final String shinseiYear,
                final String kekkaDiv,
                final String shinsaKekka,
                final String sshotokuKijunSetaiCount,
                final String sshotokuKijunGk,
                final String syearSalaryGk,
                final String syearNotsalaryGk,
                final String syearKoujyoGk,
                final String syearTokuKoujyoGk,
                final String syearShotokuNinteiGk,
                final String sninteiKekkaFlg,
                final String setaiStatus,
                final String setaiCount,
                final String boshiFushiSetaiFlg,
                final String juniorUnderFlg,
                final String shougaishaFlg,
                final String shougaishaCount,
                final String kakuninReport,
                final String rounenshaFlg,
                final String rounenshaCount,
                final String longRyouyoushaGk,
                final String areaChousaFlg,
                final String kyuuchi,
                final String areaChousaCount,
                final String shotokuKijyunGk,
                final String ninteiSoushunyuGk,
                final String taiyoHanteiKekka,
                final String ninteiHanteiKekka,
                final String tboshiFlg,
                final String tsyougakuCount,
                final String tchuugakuCount,
                final String tkoukouJiCount,
                final String tkoukouJigaiCount,
                final String tkoukouShiJiCount,
                final String tkoukouShiJigaiCount,
                final String tsenmonKoJiCount,
                final String tsenmonKoJigaiCount,
                final String tsenmonShiJiCount,
                final String tsenmonShiJigaiCount,
                final String tdaigakuKoJiCount,
                final String tdaigakuKoJigaiCount,
                final String tdaigakuShiJiCount,
                final String tdaigakuShiJigaiCount,
                final String tsensyuKoKoJiCount,
                final String tsensyuKoKoJigaiCount,
                final String tsensyuKoShiJiCount,
                final String tsensyuKoShiJigaiCount,
                final String tsensyuSenKoJiCount,
                final String tsensyuSenKoJigaiCount,
                final String tsensyuSenShiJiCount,
                final String tsensyuSenShiJigaiCount,
                final String tshougaiCount,
                final String tlongRyouyouGk,
                final String tbekkyoGk,
                final String tsaigaiGk,
                final String tsonotaGk,
                final String tsonotaRemark,
                final String taiyoSetaiKijunDatShotokuKijyunGk
         ) {
            _kojinNo = kojinNo;
            _shinseiYear = shinseiYear;
            _kekkaDiv = kekkaDiv;
            _shinsaKekka = shinsaKekka;
            _sshotokuKijunSetaiCount = sshotokuKijunSetaiCount;
            _sshotokuKijunGk = sshotokuKijunGk;
            _syearSalaryGk = syearSalaryGk;
            _syearNotsalaryGk = syearNotsalaryGk;
            _syearKoujyoGk = syearKoujyoGk;
            _syearTokuKoujyoGk = syearTokuKoujyoGk;
            _syearShotokuNinteiGk = syearShotokuNinteiGk;
            _sninteiKekkaFlg = sninteiKekkaFlg;
            _setaiStatus = setaiStatus;
            _setaiCount = setaiCount;
            _boshiFushiSetaiFlg = boshiFushiSetaiFlg;
            _juniorUnderFlg = juniorUnderFlg;
            _shougaishaFlg = shougaishaFlg;
            _shougaishaCount = shougaishaCount;
            _kakuninReport = kakuninReport;
            _rounenshaFlg = rounenshaFlg;
            _rounenshaCount = rounenshaCount;
            _longRyouyoushaGk = longRyouyoushaGk;
            _areaChousaFlg = areaChousaFlg;
            _kyuuchi = kyuuchi;
            _areaChousaCount = areaChousaCount;
            _shotokuKijyunGk = shotokuKijyunGk;
            _ninteiSoushunyuGk = ninteiSoushunyuGk;
            _taiyoHanteiKekka = taiyoHanteiKekka;
            _ninteiHanteiKekka = ninteiHanteiKekka;
            _tboshiFlg = tboshiFlg;
            _tsyougakuCount = tsyougakuCount;
            _tchuugakuCount = tchuugakuCount;
            _tkoukouJiCount = tkoukouJiCount;
            _tkoukouJigaiCount = tkoukouJigaiCount;
            _tkoukouShiJiCount = tkoukouShiJiCount;
            _tkoukouShiJigaiCount = tkoukouShiJigaiCount;
            _tsenmonKoJiCount = tsenmonKoJiCount;
            _tsenmonKoJigaiCount = tsenmonKoJigaiCount;
            _tsenmonShiJiCount = tsenmonShiJiCount;
            _tsenmonShiJigaiCount = tsenmonShiJigaiCount;
            _tdaigakuKoJiCount = tdaigakuKoJiCount;
            _tdaigakuKoJigaiCount = tdaigakuKoJigaiCount;
            _tdaigakuShiJiCount = tdaigakuShiJiCount;
            _tdaigakuShiJigaiCount = tdaigakuShiJigaiCount;
            _tsensyuKoKoJiCount = tsensyuKoKoJiCount;
            _tsensyuKoKoJigaiCount = tsensyuKoKoJigaiCount;
            _tsensyuKoShiJiCount = tsensyuKoShiJiCount;
            _tsensyuKoShiJigaiCount = tsensyuKoShiJigaiCount;
            _tsensyuSenKoJiCount = tsensyuSenKoJiCount;
            _tsensyuSenKoJigaiCount = tsensyuSenKoJigaiCount;
            _tsensyuSenShiJiCount = tsensyuSenShiJiCount;
            _tsensyuSenShiJigaiCount = tsensyuSenShiJigaiCount;
            _tshougaiCount = tshougaiCount;
            _tlongRyouyouGk = tlongRyouyouGk;
            _tbekkyoGk = tbekkyoGk;
            _tsaigaiGk = tsaigaiGk;
            _tsonotaGk = tsonotaGk;
            _tsonotaRemark = tsonotaRemark;
            _taiyoSetaiKijunDatShotokuKijyunGk = taiyoSetaiKijunDatShotokuKijyunGk;
         }
        
        public String shotokuKijunGk(final String boshiFushiSetaiGk, final String shougaiGk, final String kyuuchiKasanGk) {
            // $setaiTotal + $boshiFushiTotal + $shougaiSyaTotal + $row["LONG_RYOUYOUSHA_GK"] + $areaTotal
            final long setaiTotal = toLong(_taiyoSetaiKijunDatShotokuKijyunGk);
            final long boshiFushiTotal = toLong(boshiFushiSetaiGk);
            final long shougaiShaTotal = toLong(shougaiGk);
            final long rowLongRyouyoushaGk = toLong(_longRyouyoushaGk);
            final long areaTotal = toLong(kyuuchiKasanGk);
            return String.valueOf(setaiTotal + boshiFushiTotal + shougaiShaTotal + rowLongRyouyoushaGk + areaTotal);
        }

         public String getKyuuchiKasanGk(final DB2UDB db2, final String kyuuchiCd) {
             String kyuuchiKasanGk = null;
             String setaiCount = _areaChousaCount;
             final String sql = " SELECT KYUUCHI_KASAN_GK FROM TAIYO_KASAN1_DAT WHERE YEAR = '" + _shinseiYear + "' AND SETAI_COUNT = " + setaiCount + " AND KYUUCHI_CD = '" + kyuuchiCd + "' ";
             PreparedStatement ps = null;
             ResultSet rs = null;
             try {
                 ps = db2.prepareStatement(sql);
                 rs = ps.executeQuery();
                 if (rs.next()) {
                     kyuuchiKasanGk = rs.getString("KYUUCHI_KASAN_GK");
                 }
                 
             } catch (final Exception e) {
                 log.error("exception!", e);
             } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
             }
             return kyuuchiKasanGk;
        }

        public String shougaiGk() {
             if (null != _taiyoKasan2Dat) {
                 if (NumberUtils.isDigits(_shougaishaCount) && NumberUtils.isDigits(_taiyoKasan2Dat._shougaishaGk)) {
                     return String.valueOf(Long.parseLong(_shougaishaCount) * Long.parseLong(_taiyoKasan2Dat._shougaishaGk)); 
                 }
             }
             return null;
         }
         
         public static KojinTaiyoShinsaKekkaDat load(final DB2UDB db2, final Param param, final String issuedate, final String setaiSeq) {
             KojinTaiyoShinsaKekkaDat ktskd = null;
             PreparedStatement ps = null;
             ResultSet rs = null;
             final String sql = sql(param._kojinNo, param._shinseiYear, param._shikinShousaiDiv, issuedate, param._kekkaDiv, setaiSeq);
             try {
                 ps = db2.prepareStatement(sql);
                 rs = ps.executeQuery();
                 while (rs.next()) {
                     final String kojinNo = rs.getString("KOJIN_NO");
                     final String shinseiYear = rs.getString("SHINSEI_YEAR");
                     final String kekkaDiv = rs.getString("KEKKA_DIV");
                     final String shinsaKekka = rs.getString("SHINSA_KEKKA");
                     final String sshotokuKijunSetaiCount = rs.getString("SSHOTOKU_KIJUN_SETAI_COUNT");
                     final String sshotokuKijunGk = rs.getString("SSHOTOKU_KIJUN_GK");
                     final String syearSalaryGk = rs.getString("SYEAR_SALARY_GK");
                     final String syearNotsalaryGk = rs.getString("SYEAR_NOTSALARY_GK");
                     final String syearKoujyoGk = rs.getString("SYEAR_KOUJYO_GK");
                     final String syearTokuKoujyoGk = rs.getString("SYEAR_TOKU_KOUJYO_GK");
                     final String syearShotokuNinteiGk = rs.getString("SYEAR_SHOTOKU_NINTEI_GK");
                     final String sninteiKekkaFlg = rs.getString("SNINTEI_KEKKA_FLG");
                     final String setaiStatus = rs.getString("SETAI_STATUS");
                     final String setaiCount = rs.getString("SETAI_COUNT");
                     final String boshiFushiSetaiFlg = rs.getString("BOSHI_FUSHI_SETAI_FLG");
                     final String juniorUnderFlg = rs.getString("JUNIOR_UNDER_FLG");
                     final String shougaishaFlg = rs.getString("SHOUGAISHA_FLG");
                     final String shougaishaCount = rs.getString("SHOUGAISHA_COUNT");
                     final String kakuninReport = rs.getString("KAKUNIN_REPORT");
                     final String rounenshaFlg = rs.getString("ROUNENSHA_FLG");
                     final String rounenshaCount = rs.getString("ROUNENSHA_COUNT");
                     final String longRyouyoushaGk = rs.getString("LONG_RYOUYOUSHA_GK");
                     final String areaChousaFlg = rs.getString("AREA_CHOUSA_FLG");
                     final String kyuuchi = rs.getString("KYUUCHI");
                     final String areaChousaCount = rs.getString("AREA_CHOUSA_COUNT");
                     final String shotokuKijyunGk = rs.getString("SHOTOKU_KIJYUN_GK");
                     final String ninteiSoushunyuGk = rs.getString("NINTEI_SOUSHUNYU_GK");
                     final String taiyoHanteiKekka = rs.getString("TAIYO_HANTEI_KEKKA");
                     final String ninteiHanteiKekka = rs.getString("NINTEI_HANTEI_KEKKA");
                     final String tboshiFlg = rs.getString("TBOSHI_FLG");
                     final String tsyougakuCount = rs.getString("TSYOUGAKU_COUNT");
                     final String tchuugakuCount = rs.getString("TCHUUGAKU_COUNT");
                     final String tkoukouJiCount = rs.getString("TKOUKOU_JI_COUNT");
                     final String tkoukouJigaiCount = rs.getString("TKOUKOU_JIGAI_COUNT");
                     final String tkoukouShiJiCount = rs.getString("TKOUKOU_SHI_JI_COUNT");
                     final String tkoukouShiJigaiCount = rs.getString("TKOUKOU_SHI_JIGAI_COUNT");
                     final String tsenmonKoJiCount = rs.getString("TSENMON_KO_JI_COUNT");
                     final String tsenmonKoJigaiCount = rs.getString("TSENMON_KO_JIGAI_COUNT");
                     final String tsenmonShiJiCount = rs.getString("TSENMON_SHI_JI_COUNT");
                     final String tsenmonShiJigaiCount = rs.getString("TSENMON_SHI_JIGAI_COUNT");
                     final String tdaigakuKoJiCount = rs.getString("TDAIGAKU_KO_JI_COUNT");
                     final String tdaigakuKoJigaiCount = rs.getString("TDAIGAKU_KO_JIGAI_COUNT");
                     final String tdaigakuShiJiCount = rs.getString("TDAIGAKU_SHI_JI_COUNT");
                     final String tdaigakuShiJigaiCount = rs.getString("TDAIGAKU_SHI_JIGAI_COUNT");
                     final String tsensyuKoKoJiCount = rs.getString("TSENSYU_KO_KO_JI_COUNT");
                     final String tsensyuKoKoJigaiCount = rs.getString("TSENSYU_KO_KO_JIGAI_COUNT");
                     final String tsensyuKoShiJiCount = rs.getString("TSENSYU_KO_SHI_JI_COUNT");
                     final String tsensyuKoShiJigaiCount = rs.getString("TSENSYU_KO_SHI_JIGAI_COUNT");
                     final String tsensyuSenKoJiCount = rs.getString("TSENSYU_SEN_KO_JI_COUNT");
                     final String tsensyuSenKoJigaiCount = rs.getString("TSENSYU_SEN_KO_JIGAI_COUNT");
                     final String tsensyuSenShiJiCount = rs.getString("TSENSYU_SEN_SHI_JI_COUNT");
                     final String tsensyuSenShiJigaiCount = rs.getString("TSENSYU_SEN_SHI_JIGAI_COUNT");
                     final String tshougaiCount = rs.getString("TSHOUGAI_COUNT");
                     final String tlongRyouyouGk = rs.getString("TLONG_RYOUYOU_GK");
                     final String tbekkyoGk = rs.getString("TBEKKYO_GK");
                     final String tsaigaiGk = rs.getString("TSAIGAI_GK");
                     final String tsonotaGk = rs.getString("TSONOTA_GK");
                     final String tsonotaRemark = rs.getString("TSONOTA_REMARK");
                     final String taiyoSetaiKijunDatShotokuKijyunGk = rs.getString("TAIYO_SETAI_KIJYUN_DAT_SHOTOKU_KIJYUN_GK");

                     ktskd = new KojinTaiyoShinsaKekkaDat(kojinNo, shinseiYear, kekkaDiv, shinsaKekka, sshotokuKijunSetaiCount, sshotokuKijunGk, syearSalaryGk, syearNotsalaryGk, syearKoujyoGk, syearTokuKoujyoGk, syearShotokuNinteiGk,
                             sninteiKekkaFlg, setaiStatus, setaiCount, boshiFushiSetaiFlg, juniorUnderFlg, shougaishaFlg, shougaishaCount, kakuninReport, rounenshaFlg, rounenshaCount, longRyouyoushaGk, areaChousaFlg, kyuuchi, areaChousaCount,
                             shotokuKijyunGk, ninteiSoushunyuGk, taiyoHanteiKekka, ninteiHanteiKekka,
                             tboshiFlg, tsyougakuCount, tchuugakuCount, tkoukouJiCount, tkoukouJigaiCount, tkoukouShiJiCount, tkoukouShiJigaiCount, tsenmonKoJiCount, tsenmonKoJigaiCount, tsenmonShiJiCount, tsenmonShiJigaiCount, tdaigakuKoJiCount, tdaigakuKoJigaiCount, tdaigakuShiJiCount, tdaigakuShiJigaiCount, tsensyuKoKoJiCount, tsensyuKoKoJigaiCount, tsensyuKoShiJiCount, tsensyuKoShiJigaiCount, tsensyuSenKoJiCount, tsensyuSenKoJigaiCount, tsensyuSenShiJiCount, tsensyuSenShiJigaiCount, tshougaiCount, tlongRyouyouGk, tbekkyoGk, tsaigaiGk, tsonotaGk, tsonotaRemark,
                             taiyoSetaiKijunDatShotokuKijyunGk);
                 }
             } catch (Exception ex) {
                 log.fatal("exception! sql= " + sql, ex);
             } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
             }
             if (null != ktskd) {
                 ktskd._taiyoKasan2Dat = TaiyoKasan2Dat.load(db2, param);
             }
             return ktskd;
         }

         public static String sql(final String kojinNo, final String shinseiYear, final String shikinShousaiDiv, final String issuedate, final String kekkaDiv, final String setaiSeq) {
             final StringBuffer stb = new StringBuffer();
             stb.append(" SELECT T1.*, T2.SETAI_KIJYUN_GK AS TAIYO_SETAI_KIJYUN_DAT_SHOTOKU_KIJYUN_GK ");
             stb.append(" FROM KOJIN_TAIYO_SHINSA_KEKKA_DAT T1 ");
             stb.append(" LEFT JOIN TAIYO_SETAI_KIJYUN_DAT T2 ON T2.YEAR = T1.SHINSEI_YEAR AND T2.SETAI_COUNT = T1.SETAI_COUNT ");
             stb.append(" WHERE ");
             stb.append("     T1.KOJIN_NO = '" + kojinNo + "' ");
             stb.append("     AND T1.SHINSEI_YEAR = '" + shinseiYear + "' ");
             stb.append("     AND T1.SHIKIN_SHOUSAI_DIV = '" + shikinShousaiDiv + "' ");
             stb.append("     AND T1.ISSUEDATE = '" + issuedate + "' ");
             stb.append("     AND T1.KEKKA_DIV = '" + kekkaDiv + "' ");
             stb.append("     AND T1.SETAI_SEQ = " + setaiSeq + " ");
             return stb.toString();
         }
    }

    private static class KojinHistDat {
        final String _kojinNo;
        final String _issuedate;
        final String _familyName;
        final String _firstName;
        final String _familyNameKana;
        final String _firstNameKana;
        final String _birthday;
        final String _kikonFlg;
        final String _jSchoolCd;
        final String _jGradDiv;
        final String _jGradYm;
        final String _zipcd;
        final String _citycd;
        final String _addr1;
        final String _addr2;
        final String _telno1;
        final String _telno2;
        final String _tsuugakuDiv;
        final String _tsuugakuDivName;
        final String _remark;

        KojinHistDat(
                final String kojinNo,
                final String issuedate,
                final String familyName,
                final String firstName,
                final String familyNameKana,
                final String firstNameKana,
                final String birthday,
                final String kikonFlg,
                final String jSchoolCd,
                final String jGradDiv,
                final String jGradYm,
                final String zipcd,
                final String citycd,
                final String addr1,
                final String addr2,
                final String telno1,
                final String telno2,
                final String tsuugakuDiv,
                final String tsuugakuDivName,
                final String remark
        ) {
            _kojinNo = kojinNo;
            _issuedate = issuedate;
            _familyName = familyName;
            _firstName = firstName;
            _familyNameKana = familyNameKana;
            _firstNameKana = firstNameKana;
            _birthday = birthday;
            _kikonFlg = kikonFlg;
            _jSchoolCd = jSchoolCd;
            _jGradDiv = jGradDiv;
            _jGradYm = jGradYm;
            _zipcd = zipcd;
            _citycd = citycd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno1 = telno1;
            _telno2 = telno2;
            _tsuugakuDiv = tsuugakuDiv;
            _tsuugakuDivName = tsuugakuDivName;
            _remark = remark;
        }

        public String getName() {
            return StringUtils.defaultString(_familyName) + "　" +  StringUtils.defaultString(_firstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_familyNameKana) + "　" +  StringUtils.defaultString(_firstNameKana);
        }

        public static KojinHistDat load(final DB2UDB db2, final Param param) {
            KojinHistDat kojinhistdat = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param._kojinNo));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kojinNo = rs.getString("KOJIN_NO");
                    final String issuedate = rs.getString("ISSUEDATE");
                    final String familyName = rs.getString("FAMILY_NAME");
                    final String firstName = rs.getString("FIRST_NAME");
                    final String familyNameKana = rs.getString("FAMILY_NAME_KANA");
                    final String firstNameKana = rs.getString("FIRST_NAME_KANA");
                    final String birthday = rs.getString("BIRTHDAY");
                    final String kikonFlg = rs.getString("KIKON_FLG");
                    final String jSchoolCd = rs.getString("J_SCHOOL_CD");
                    final String jGradDiv = rs.getString("J_GRAD_DIV");
                    final String jGradYm = rs.getString("J_GRAD_YM");
                    final String zipcd = rs.getString("ZIPCD");
                    final String citycd = rs.getString("CITYCD");
                    final String addr1 = rs.getString("ADDR1");
                    final String addr2 = rs.getString("ADDR2");
                    final String telno1 = rs.getString("TELNO1");
                    final String telno2 = rs.getString("TELNO2");
                    final String tsuugakuDiv = rs.getString("TSUUGAKU_DIV");
                    final String tsuugakuDivName =  rs.getString("TSUUGAKU_DIV_NAME");
                    final String remark = rs.getString("REMARK");
                    kojinhistdat = new KojinHistDat(kojinNo, issuedate, familyName, firstName, familyNameKana, firstNameKana,
                        birthday, kikonFlg, jSchoolCd, jGradDiv, jGradYm, zipcd, citycd, addr1, addr2, telno1, telno2, tsuugakuDiv, tsuugakuDivName, remark);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return kojinhistdat;
        }

        public static String sql(final String kojinNo) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX AS ( ");
            stb.append("   SELECT T1.KOJIN_NO, MAX(T1.ISSUEDATE) AS ISSUEDATE ");
            stb.append("   FROM KOJIN_HIST_DAT T1");
            stb.append("   WHERE T1.KOJIN_NO = '" + kojinNo + "' ");
            stb.append("   GROUP BY T1.KOJIN_NO ");
            stb.append(" ) ");
            stb.append(" SELECT T1.*, T007.NAME1 AS TSUUGAKU_DIV_NAME ");
            stb.append(" FROM KOJIN_HIST_DAT T1");
            stb.append(" INNER JOIN MAX T2 ON T2.KOJIN_NO = T1.KOJIN_NO AND T2.ISSUEDATE = T1.ISSUEDATE ");
            stb.append(" LEFT JOIN NAME_MST T007 ON T007.NAMECD1 = 'T007' AND T007.NAMECD2 = T1.TSUUGAKU_DIV ");
            return stb.toString();
        }
    }

    private static class KojinTaiyoSetaiDat {
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
        final String _shotokuCdName;

        KojinTaiyoSetaiDat(
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
                final String tsuzukigaraName,
                final String shotokuCdName
        ) {
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
            _shotokuCdName = shotokuCdName;
        }
        
        public String getName() {
            return StringUtils.defaultString(_familyName) + "　" +  StringUtils.defaultString(_firstName);
        }

        public String getKana() {
            return StringUtils.defaultString(_familyNameKana) + "　" +  StringUtils.defaultString(_firstNameKana);
        }

        public static List load(final DB2UDB db2, final Param param, final String setaiSeq) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param._kojinNo, param._shinseiYear, setaiSeq));
                rs = ps.executeQuery();
                while (rs.next()) {
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
                    final String shotokuCdName = rs.getString("SHOTOKU_CD_NAME");
                    final KojinTaiyoSetaiDat kojinTaiyoSetaiDat = new KojinTaiyoSetaiDat(seq, familyName, firstName, firstNameKana, familyNameKana,
                            tsuzukigaraCd, nenrei, kyoudaiKojinNo, shotokuCd, shotokuGk, koujoFuboIgai, koujoHoken, koujoShotoku, ninteiGk, shutaruFlg, setainushiFlg, remark, reason, shikinDiv,
                            tsuzukigaraName, shotokuCdName);
                    list.add(kojinTaiyoSetaiDat);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final String kojinNo, final String shinseiYear, final String setaiSeq) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.*, NMT006.NAME1 AS TSUZUKIGARA_NAME, NMT011.NAME1 AS SHOTOKU_CD_NAME ");
            stb.append(" FROM KOJIN_TAIYO_SETAI_DAT T1 ");
            stb.append(" LEFT JOIN NAME_MST NMT006 ON NMT006.NAMECD1 = 'T006' AND NMT006.NAMECD2 = T1.TSUZUKIGARA_CD ");
            stb.append(" LEFT JOIN NAME_MST NMT011 ON NMT011.NAMECD1 = 'T011' AND NMT011.NAMECD2 = T1.SHOTOKU_CD ");
            stb.append(" WHERE T1.KOJIN_NO = '" + kojinNo + "' AND T1.SHINSEI_YEAR = '" + shinseiYear + "' AND T1.SETAI_SEQ = " + setaiSeq + " ");
            stb.append(" ORDER BY ");
            stb.append("     CASE T1.TSUZUKIGARA_CD WHEN '05' THEN '04' "); // 05:姉と04:兄を年齢順にソート
            stb.append("                            WHEN '07' THEN '06' "); // 07:妹と06:弟を年齢順にソート
            stb.append("     ELSE T1.TSUZUKIGARA_CD END, ");
            stb.append("     VALUE(NENREI, 0) DESC, T1.FIRST_NAME_KANA, T1.SEQ ");
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

        public static ShinkenshaHistDat load(final DB2UDB db2, final String paramShinkenCd) {
            ShinkenshaHistDat shinkenshahistdat = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                 ps = db2.prepareStatement(sql(paramShinkenCd));
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
                     shinkenshahistdat = new ShinkenshaHistDat(shinkenCd, issuedate,familyName, firstName, familyNameKana, firstNameKana,
                             birthday, shinseiNenrei, zipcd, citycd, addr1, addr2, telno1, telno2);
                 }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            return shinkenshahistdat;
        }

        public static String sql(final String paramShinkenCd) {
            final StringBuffer stb = new StringBuffer();
//            stb.append(" WITH MAX_DATE AS ( ");
//            stb.append("   SELECT SHINKEN_CD, MAX(ISSUEDATE) AS ISSUEDATE ");
//            stb.append("   FROM SHINKENSHA_HIST_DAT ");
//            stb.append(" WHERE SHINKEN_CD = '" + paramShinkenCd + "' ");
//            stb.append("   GROUP BY SHINKEN_CD ");
//            stb.append(" ) ");
            stb.append(" SELECT * ");
            stb.append(" FROM SHINKENSHA_HIST_DAT T1 ");
            stb.append(" WHERE SHINKEN_CD = '" + paramShinkenCd + "' ");
//            stb.append(" INNER JOIN MAX_DATE T2 ON T2.SHINKEN_CD = T1.SHINKEN_CD AND T2.ISSUEDATE = T1.ISSUEDATE ");
            return stb.toString();
        }
    }
    
    private static class SchoolDat {
        final String _schoolcd;
        final String _schoolType;
        final String _schoolDistcd;
        final String _schoolDiv;
        final String _name;
        final String _kana;
        final String _nameAbbv;
        final String _kanaAbbv;
        final String _districtcd;
        final String _prefCd;
        final String _zipcd;
        final String _addr1;
        final String _addr2;
        final String _telno;
        final String _faxno;
        final String _edboardcd;
        final String _fuNaigaiDiv;
        final String _haishiFlg;
        final String _atenaInsatsuFlg;
        final String _remark;
        final String _katei;

        SchoolDat(
                final String schoolcd,
                final String schoolType,
                final String schoolDistcd,
                final String schoolDiv,
                final String name,
                final String kana,
                final String nameAbbv,
                final String kanaAbbv,
                final String districtcd,
                final String prefCd,
                final String zipcd,
                final String addr1,
                final String addr2,
                final String telno,
                final String faxno,
                final String edboardcd,
                final String fuNaigaiDiv,
                final String haishiFlg,
                final String atenaInsatsuFlg,
                final String remark,
                final String katei
        ) {
            _schoolcd = schoolcd;
            _schoolType = schoolType;
            _schoolDistcd = schoolDistcd;
            _schoolDiv = schoolDiv;
            _name = name;
            _kana = kana;
            _nameAbbv = nameAbbv;
            _kanaAbbv = kanaAbbv;
            _districtcd = districtcd;
            _prefCd = prefCd;
            _zipcd = zipcd;
            _addr1 = addr1;
            _addr2 = addr2;
            _telno = telno;
            _faxno = faxno;
            _edboardcd = edboardcd;
            _fuNaigaiDiv = fuNaigaiDiv;
            _haishiFlg = haishiFlg;
            _atenaInsatsuFlg = atenaInsatsuFlg;
            _remark = remark;
            _katei = katei;
        }

        public static SchoolDat load(final DB2UDB db2, final String paramSchoolcd) {
            SchoolDat schooldat = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                 final String sql = sql(paramSchoolcd);
                 ps = db2.prepareStatement(sql);
                 rs = ps.executeQuery();
                 while (rs.next()) {
                     final String schoolcd = rs.getString("SCHOOLCD");
                     final String schoolType = rs.getString("SCHOOL_TYPE");
                     final String schoolDistcd = rs.getString("SCHOOL_DISTCD");
                     final String schoolDiv = rs.getString("SCHOOL_DIV");
                     final String name = rs.getString("NAME");
                     final String kana = rs.getString("KANA");
                     final String nameAbbv = rs.getString("NAME_ABBV");
                     final String kanaAbbv = rs.getString("KANA_ABBV");
                     final String districtcd = rs.getString("DISTRICTCD");
                     final String prefCd = rs.getString("PREF_CD");
                     final String zipcd = rs.getString("ZIPCD");
                     final String addr1 = rs.getString("ADDR1");
                     final String addr2 = rs.getString("ADDR2");
                     final String telno = rs.getString("TELNO");
                     final String faxno = rs.getString("FAXNO");
                     final String edboardcd = rs.getString("EDBOARDCD");
                     final String fuNaigaiDiv = rs.getString("FU_NAIGAI_DIV");
                     final String haishiFlg = rs.getString("HAISHI_FLG");
                     final String atenaInsatsuFlg = rs.getString("ATENA_INSATSU_FLG");
                     final String remark = rs.getString("REMARK");
                     final String katei = rs.getString("KATEI");
                     schooldat = new SchoolDat(schoolcd, schoolType, schoolDistcd, schoolDiv, name, kana, nameAbbv, kanaAbbv, districtcd, prefCd, zipcd,
                             addr1, addr2, telno, faxno, edboardcd, fuNaigaiDiv, haishiFlg, atenaInsatsuFlg, remark, katei);
                 }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schooldat;
        }

        public static String sql(final String schoolcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.*, T2.KATEI, T3.SHUUGAKU_MONTH_COUNT ");
            stb.append(" FROM SCHOOL_DAT T1 ");
            stb.append(" LEFT JOIN SCHOOL_KATEI_DAT T2 ON T2.SCHOOLCD = T1.SCHOOLCD ");
            stb.append(" LEFT JOIN KATEI_MST T3 ON T3.KATEI = T2.KATEI ");
            stb.append(" WHERE T1.SCHOOLCD = '" + schoolcd + "'");
            return stb.toString();
        }
    }
    
    private static class TaiyoKasan2Dat {
        final String _year;
        final String _boshiFushiSetaiGk;
        final String _shougaishaGk;

        TaiyoKasan2Dat(
                final String year,
                final String boshiFushiSetaiGk,
                final String shougaishaGk
        ) {
            _year = year;
            _boshiFushiSetaiGk = boshiFushiSetaiGk;
            _shougaishaGk = shougaishaGk;
        }

        public static TaiyoKasan2Dat load(final DB2UDB db2, final Param param) {
            TaiyoKasan2Dat taiyokasan2dat = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                 ps = db2.prepareStatement(sql(param._shinseiYear));
                 rs = ps.executeQuery();
                 while (rs.next()) {
                     final String year = rs.getString("YEAR");
                     final String boshiFushiSetaiGk = rs.getString("BOSHI_FUSHI_SETAI_GK");
                     final String shougaishaGk = rs.getString("SHOUGAISHA_GK");
                     taiyokasan2dat = new TaiyoKasan2Dat(year, boshiFushiSetaiGk, shougaishaGk);
                 }
            } catch (Exception ex) {
                 log.fatal("exception!", ex);
            } finally {
                 DbUtils.closeQuietly(null, ps, rs);
                 db2.commit();
            }
            return taiyokasan2dat;
        }

        public static String sql(final String year) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT *  ");
            stb.append(" FROM TAIYO_KASAN2_DAT ");
            stb.append(" WHERE YEAR = '" + year + "' ");
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
        final String _yokinDivName;
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

        BankMst _bankMst = null;

        KojinKouzaBankDat(
                final String kojinNo,
                final String kouzaDiv,
                final String sDate,
                final String bankcd,
                final String branchcd,
                final String yokinDiv,
                final String yokinDivName,
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
            _yokinDivName = yokinDivName;
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
        }
        
        public String getMeigiKana() {
            return StringUtils.defaultString(_bankMeigiSeiKana) + "　" +  StringUtils.defaultString(_bankMeigiMeiKana);
        }
        
        public String getMeigiName() {
            return StringUtils.defaultString(_bankMeigiSeiName) + "　" +  StringUtils.defaultString(_bankMeigiMeiName);
        }

        public static KojinKouzaBankDat load(final DB2UDB db2, final Param param) {
            KojinKouzaBankDat kojinkouzabankdat = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param._kojinNo, param.nendoToMaxDate(), FURIKOMI));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kojinNo = rs.getString("KOJIN_NO");
                    final String kouzaDiv = rs.getString("KOUZA_DIV");
                    final String sDate = rs.getString("S_DATE");
                    final String bankcd = rs.getString("BANKCD");
                    final String branchcd = rs.getString("BRANCHCD");
                    final String yokinDiv = rs.getString("YOKIN_DIV");
                    final String yokinDivName = rs.getString("YOKIN_DIV_NAME");
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
                    kojinkouzabankdat = new KojinKouzaBankDat(kojinNo, kouzaDiv, sDate, bankcd, branchcd, yokinDiv, yokinDivName, accountNo,
                            bankMeigiSeiKana, bankMeigiMeiKana, bankMeigiSeiName, bankMeigiMeiName, zipcd, addr1, addr2, telno1, telno2);
                }
           } catch (Exception ex) {
               log.fatal("exception!", ex);
           } finally {
               DbUtils.closeQuietly(null, ps, rs);
               db2.commit();
           }
           if (null != kojinkouzabankdat) {
               kojinkouzabankdat._bankMst = BankMst.load(db2, kojinkouzabankdat._bankcd, kojinkouzabankdat._branchcd);
           }
           return kojinkouzabankdat;
        }

        public static String sql(final String kojinNo, final String date, final String kouzaDiv) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_SDATE AS ( ");
            stb.append("   SELECT KOJIN_NO, TAISHOUSHA_DIV, KOUZA_DIV, MAX(S_DATE) AS S_DATE ");
            stb.append("   FROM KOJIN_KOUZA_BANK_DAT T1 ");
            stb.append("   WHERE KOJIN_NO = '" + kojinNo + "' ");
            stb.append("     AND TAISHOUSHA_DIV = '1' ");
            stb.append("     AND KOUZA_DIV = '" + kouzaDiv + "' ");
            stb.append("     AND S_DATE <= '" + date + "' ");
            stb.append("   GROUP BY KOJIN_NO, TAISHOUSHA_DIV, KOUZA_DIV ");
            stb.append(" ) ");
            stb.append(" SELECT T1.*, ");
            stb.append("   NMT032.NAME1 AS YOKIN_DIV_NAME ");
            stb.append(" FROM ");
            stb.append("   KOJIN_KOUZA_BANK_DAT T1 ");
            stb.append("   INNER JOIN MAX_SDATE T2 ON T2.KOJIN_NO = T1.KOJIN_NO AND T2.S_DATE = T1.S_DATE AND T2.TAISHOUSHA_DIV = T1.TAISHOUSHA_DIV AND T2.KOUZA_DIV = T1.KOUZA_DIV ");
            stb.append("   LEFT JOIN NAME_MST NMT032 ON NMT032.NAMECD1 = 'T032' AND NMT032.NAMECD2 = T1.YOKIN_DIV ");
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

        public static BankMst load(final DB2UDB db2, final String bankCd, final String branchCd) {
            BankMst bank = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(bankCd, branchCd));
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
                    bank = new BankMst(bankcd, branchcd, bankname, banknameKana, banknameRomaji, branchname, branchnameKana, branchnameRomaji,
                            bankzipcd, bankaddr1, bankaddr2, banktelno, bankfaxno, shuugakukinItakuCd, shitakukinItakuCd, saifurikaeDiv, groupingDiv, manageBranchcd);
                }
           } catch (Exception ex) {
                log.fatal("exception!", ex);
           } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
           }
           return bank; 
        }

        public static String sql(final String bankCd, final String branchCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT * ");
            stb.append(" FROM BANK_MST ");
            stb.append(" WHERE BANKCD = '" + bankCd + "' AND BRANCHCD = '" + branchCd + "' ");
            return stb.toString();
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67181 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _prgId;
        private final String _kojinNo;
        private final String _shinseiYear;
        private final String _shikinShousaiDiv;
        private final String _ukeYear;
        private final String _ukeNo;
        private final String _ukeEdaban;
        private final String _issueDate;
        
        private final String _kekkaDiv = "1";// "審査１"
        private final ShugakuDate _shugakuDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _prgId = request.getParameter("getPrgId");
            _kojinNo = request.getParameter("sendKojinNo");
            _shinseiYear = request.getParameter("sendShinseiYear");
            _shikinShousaiDiv = request.getParameter("sendShikinshousaiDiv");
            _ukeYear = request.getParameter("sendUkeYear");
            _ukeNo = request.getParameter("sendUkeNo");
            _ukeEdaban = request.getParameter("sendUkeEdaban");
            _issueDate = request.getParameter("sendIssueDate");
            _shugakuDate = new ShugakuDate(db2);
            _shugakuDate._printBlank = true;
        }
        
        public boolean isTaiyoyoyaku() {
            return _prgId.equals("KNJTA020_01") || _prgId.equals("KNJTA020_09");
        }
        
        public String nendoToMaxDate() {
            return (Integer.parseInt(_shinseiYear) + 1) + "-03-31";
        }
    }
}

// eof

