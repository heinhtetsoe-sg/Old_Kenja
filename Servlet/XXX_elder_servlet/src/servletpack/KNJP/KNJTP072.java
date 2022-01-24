/*
 * $Id: 944ae5e045ec62dcee9c59d345716299ebd1be55 $
 * 修学資金特別融資申込資格審査計算書(審査計算書2)
 *
 * 作成日: 2012/09/08
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJTP072 {

    private static final Log log = LogFactory.getLog(KNJTP072.class);

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

            svf.VrSetForm("KNJTP072.frm", 1);

            printName(svf, kojin);
            
            printShutaru(svf, kojin);
            
            printJourei(svf, db2, kojin);

            svf.VrEndPage();
            _hasData = true;

        } catch (Exception ex) {
            log.debug("Exception:", ex);
        }
    }

    private void printName(final Vrw32alp svf, final Kojin kojin) {
        
        if (_param.isTaiyoyoyaku() && null != kojin._taiyoyoyaku) {
        } else if (!_param.isTaiyoyoyaku() && null != kojin._shinsei){
            final KojinShinseiHistDat shinsei = kojin._shinsei;
            if (null != shinsei._schoolDat) {
                final String[] schoolname = KNJ_EditEdit.get_token(shinsei._schoolDat._name, 40, 2);
                if (null != schoolname) {
                    for (int i = 0; i < schoolname.length; i++) {
                        svf.VrsOut("SCHOOL_NAME" + (i + 1), schoolname[i]);
                    }
                }
            }
            svf.VrsOut("GRADE", shinsei.getGradeStr());
        }
        
        final String[] name = KNJ_EditEdit.get_token(kojin._kojinHist.getName(), 40, 2);
        if (null != name) {
            for (int i = 0; i < name.length; i++) {
                svf.VrsOut("NAME1_" + (i + 1), name[i]);
            }
        }
    }
    

    private void printShutaru(final Vrw32alp svf, final Kojin kojin) {
        
        final KojinTaiyoShinsaKekkaDat shinsaKekka = kojin._shinsaKekka;
        
        ShinkenshaHistDat shutaru = null;
        if (_param.isTaiyoyoyaku() && null != kojin._taiyoyoyaku) {
            shutaru = kojin._taiyoyoyaku._shutaru;
        } else if (!_param.isTaiyoyoyaku() && null != kojin._shinsei){
            shutaru = kojin._shinsei._shutaru;
        }
        if (null != shutaru) {
            final String[] shutaruName = KNJ_EditEdit.get_token(shutaru.getName(), 40, 2);
            if (null != shutaruName) {
                for (int i = 0; i < shutaruName.length; i++) {
                    svf.VrsOut("NAME2_" + (i + 1), shutaruName[i]);
                }
            }
        }
        if (null != shinsaKekka) {
            svf.VrsOut("FAMILY1", shinsaKekka._sshotokuKijunSetaiCount);
            if (NumberUtils.isDigits(shinsaKekka._sshotokuKijunGk)) {
                svf.VrsOut("INCOME_STANDARD1", String.valueOf(Long.parseLong(shinsaKekka._sshotokuKijunGk) / 10000));
            }
            svf.VrsOutn("YEAR_INCOME", 1, shinsaKekka._syearSalaryGk);
            svf.VrsOutn("YEAR_INCOME", 2, shinsaKekka._syearNotsalaryGk);
            svf.VrsOutn("YEAR_INCOME", 3, shinsaKekka._syearKoujyoGk);
            svf.VrsOutn("YEAR_INCOME", 4, shinsaKekka._syearTokuKoujyoGk);
            svf.VrsOutn("YEAR_INCOME", 5, shinsaKekka._syearShotokuNinteiGk);

            svf.VrsOut("INCOME_STANDARD2", shinsaKekka._sshotokuKijunGk);
            svf.VrsOut("YEAR_INCOME_JUDGE", shinsaKekka._syearShotokuNinteiGk);
            
            String kekka = "";
            if ("1".equals(shinsaKekka._sninteiKekkaFlg)) {
                kekka = "以下";
            } else if ("2".equals(shinsaKekka._sninteiKekkaFlg)) {
                kekka = "超過";
            }
            svf.VrsOut("LOAN_STANDARD", kekka);
            
            if (NumberUtils.isDigits(shinsaKekka._sshotokuKijunGk) && NumberUtils.isDigits(shinsaKekka._syearShotokuNinteiGk)) {
                final long sagaku = Long.parseLong(shinsaKekka._sshotokuKijunGk) - Long.parseLong(shinsaKekka._syearShotokuNinteiGk);
                svf.VrsOut("DIFF", String.valueOf(sagaku));
            }
        }
    }
    
    private void printJourei(final Vrw32alp svf, final DB2UDB db2, final Kojin kojin) {
        
        final KojinTaiyoShinsaKekkaDat kekka = kojin._shinsaKekka;
        if (null != kekka) {
            svf.VrsOut("INCOME_STANDARD3", kekka._shotokuKijyunGk);
            svf.VrsOut("FAMILY2", kekka._setaiCount);
            svf.VrsOutn("ORD_INCOME_STANDARD", 1, kekka._taiyoSetaiKijunDatShotokuKijyunGk);
            
            String boshiFushiSetaiGk = "0";
            if (null != kekka._taiyoKasan2Dat) {
                svf.VrsOut("MF_FAMILY_INCOME", kekka._taiyoKasan2Dat._boshiFushiSetaiGk);
                if ("1".equals(kekka._boshiFushiSetaiFlg)) {
                    boshiFushiSetaiGk = kekka._taiyoKasan2Dat._boshiFushiSetaiGk;
                }
                svf.VrsOut("DISP_INCOME", kekka._taiyoKasan2Dat._shougaishaGk);
            }
            svf.VrsOutn("ORD_INCOME_STANDARD", 2, boshiFushiSetaiGk);
            // C.所得基準額
            svf.VrsOut("DISP_NUM", kekka._shougaishaCount);
            String shougaiGk = "0";
            if ("1".equals(kekka._shougaishaFlg)) {
                shougaiGk = kekka.shougaiGk();
            }
            svf.VrsOutn("ORD_INCOME_STANDARD", 3, shougaiGk);
            svf.VrsOutn("ORD_INCOME_STANDARD", 4, kekka._longRyouyoushaGk);
        }
        String kyuuchiKasanGk = "0"; 
        if (_param.isTaiyoyoyaku() && null != kojin._taiyoyoyaku) {
            if (null != kojin._taiyoyoyaku._shutaru) {
                svf.VrsOut("CITY_CD", kojin._taiyoyoyaku._shutaru._citycd);
                if (null != kekka) {
                    if ("1".equals(kekka._areaChousaFlg)) {
                        final String kyuuchiCd = getKyuuchiCd(db2, kojin._taiyoyoyaku._shinseiYear, kojin._taiyoyoyaku._shutaru._citycd);
                        kyuuchiKasanGk = kekka.getKyuuchiKasanGk(db2, kyuuchiCd);
                    }
                }
            }
        } else if (!_param.isTaiyoyoyaku() && null != kojin._shinsei){
            if (null != kojin._shinsei._shutaru) {
                svf.VrsOut("CITY_CD", kojin._shinsei._shutaru._citycd);
                if (null != kekka) {
                    if ("1".equals(kekka._areaChousaFlg)) {
                        final String kyuuchiCd = getKyuuchiCd(db2, kojin._shinsei._shinseiYear, kojin._shinsei._shutaru._citycd);
                        kyuuchiKasanGk = kekka.getKyuuchiKasanGk(db2, kyuuchiCd);
                    }
                }
            }
        }
        svf.VrsOutn("ORD_INCOME_STANDARD", 5, kyuuchiKasanGk);
        
        // D.総収入認定額
        int i = 1;
        for (final Iterator it = kojin._setaiList.iterator();it.hasNext();) {
            final KojinTaiyoSetaiDat setai = (KojinTaiyoSetaiDat) it.next();

            svf.VrsOutn("NAME3", i, setai.getName());
            svf.VrsOutn("RELATION", i, setai._tsuzukigaraName);
            svf.VrsOutn("INCOME_KIND", i, setai._shotokuCdName);
            svf.VrsOutn("INCOME", i, setai._shotokuGk);
            svf.VrsOutn("INCOME_DEDU", i, setai._koujoFuboIgai);
            svf.VrsOutn("SOCIAL_INSURANCE", i, setai._koujoHoken);
            svf.VrsOutn("INCOME_TAX", i, setai._koujoShotoku);
            svf.VrsOutn("INCOME_JUDGE", i, setai._ninteiGk);
            i += 1;
        }

        if (null != kekka) {
            svf.VrsOut("TOTAL_INCOME_JUDGE", kekka._ninteiSoushunyuGk);
            svf.VrsOut("TOTAL_INCOME_STANDARD", kekka._shotokuKijyunGk);
            
            String hanteiKekka = "";
            if ("1".equals(kekka._ninteiHanteiKekka)) {
                hanteiKekka = "以下";
            } else if ("2".equals(kekka._ninteiHanteiKekka)) {
                hanteiKekka = "超過";
            }
            svf.VrsOut("TOTAL_LOAN_STANDARD", hanteiKekka);
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
        KojinShinseiHistDat _shinsei = null;
        KojinTaiyoyoyakuHistDat _taiyoyoyaku = null;
        KojinTaiyoShinsaKekkaDat _shinsaKekka = null;
        List _setaiList = Collections.EMPTY_LIST;

        Kojin(final String kojinNo) {
            _kojinNo = kojinNo;
        }
        
        public void load(final DB2UDB db2, final Param param) {
            _kojinHist = KojinHistDat.load(db2, param);
            final String shinseiYear;
            final String shikinShousaiDiv;
            final String issuedate;
            final String setaiSeq;
            if (param.isTaiyoyoyaku()) {
                _taiyoyoyaku = KojinTaiyoyoyakuHistDat.load(db2, param);
                shinseiYear = _taiyoyoyaku._shinseiYear;
                shikinShousaiDiv = _taiyoyoyaku._shikinShousaiDiv;
                issuedate = _taiyoyoyaku._yoyakuShinseiDate;
                setaiSeq = _taiyoyoyaku._setaiSeq;
            } else {
                _shinsei = KojinShinseiHistDat.load(db2, param);
                shinseiYear = _shinsei._shinseiYear;
                shikinShousaiDiv = _shinsei._shikinShousaiDiv;
                issuedate = _shinsei._issuedate;
                setaiSeq = _shinsei._setaiSeq;
            }
            _shinsaKekka = KojinTaiyoShinsaKekkaDat.load(db2, param, shinseiYear, shikinShousaiDiv, issuedate, setaiSeq);
            _setaiList = KojinTaiyoSetaiDat.load(db2, _kojinNo, shinseiYear, setaiSeq);
        }
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
        final String _setaiSeq;
        final String _keizokuKaisuu;
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
        
        SchoolDat _schoolDat = null;
        
        ShinkenshaHistDat _rentai = null;
        ShinkenshaHistDat _shinken1 = null;
        ShinkenshaHistDat _shinken2 = null;
        ShinkenshaHistDat _shutaru = null;

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
           _setaiSeq = setaiSeq;
           _keizokuKaisuu = keizokuKaisuu;
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
        }
        
        public String getGradeStr() {
            return (NumberUtils.isDigits(_grade)) ? String.valueOf(Integer.parseInt(_grade)) : _grade;
        }
        
        public static KojinShinseiHistDat load(final DB2UDB db2, final Param param) {
            KojinShinseiHistDat shinsei = null;
            PreparedStatement ps = null; 
            ResultSet rs = null; 
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
                    shinsei = new KojinShinseiHistDat(kojinNo, shinseiYear, shikinShousaiDiv, issuedate, ukeYear,
                            ukeNo, ukeEdaban, shinseiDate, shuugakuNo, nenrei, yoyakuKibouGk, sYoyakuKibouYm, eYoyakuKibouYm, sTaiyoYm, eTaiyoYm, shinseiDiv, setaiSeq, keizokuKaisuu, shitakuCancelChokuFlg, shitakuCancelRiFlg, hSchoolCd, katei, grade, entDate, hGradYm, shitakukinTaiyoDiv, yuushiFail, yuushiFailDiv, yuushiFailRemark,
                            bankCd, yuushiCourseDiv, rentaiCd, shinken1Cd, shinken2Cd, shutaruCd, shinseiKanryouFlg, shinseiCancelFlg, ketteiDate, ketteiFlg);
                }
            } catch (Exception ex) { 
                log.fatal("exception!", ex); 
            } finally { 
                DbUtils.closeQuietly(null, ps, rs); 
                db2.commit(); 
            }
            if (null != shinsei) {
                shinsei._schoolDat = SchoolDat.load(db2, shinsei._hSchoolCd);
                
                shinsei._rentai = ShinkenshaHistDat.load(db2, shinsei._shinseiYear, shinsei._rentaiCd);
                shinsei._shinken1 = ShinkenshaHistDat.load(db2, shinsei._shinseiYear, shinsei._shinken1Cd);
                shinsei._shinken2 = ShinkenshaHistDat.load(db2, shinsei._shinseiYear, shinsei._shinken2Cd);
                if (param.needShikinShousai04Shutaru()) {
                    shinsei._shutaru = ShinkenshaHistDat.load(db2, shinsei._shinseiYear, getShikinShousai04ShutaruCd(db2, shinsei));
                } else {
                    shinsei._shutaru = ShinkenshaHistDat.load(db2, shinsei._shinseiYear, shinsei._shutaruCd);
                }
            }
            return shinsei;
        }
        
        private static String getShikinShousai04ShutaruCd(final DB2UDB db2, final KojinShinseiHistDat shinsei) {
            String shutaruCd = null;
            PreparedStatement ps = null; 
            ResultSet rs = null; 
            try { 
                String sql = "";
                sql += " SELECT SHUTARU_CD ";
                sql += " FROM KOJIN_SHINSEI_HIST_DAT ";
                sql += " WHERE SHINSEI_YEAR = '" + shinsei._shinseiYear + "' ";
                sql += "       AND KOJIN_NO = '" + shinsei._kojinNo + "' ";
                sql += "       AND SHIKIN_SHOUSAI_DIV = '04' ";
                sql += " ORDER BY ISSUEDATE ";
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql); 
                rs = ps.executeQuery(); 
                while (rs.next()) { 
                    shutaruCd = rs.getString("SHUTARU_CD");
                }
            } catch (Exception ex) { 
                log.fatal("exception!", ex); 
            } finally { 
                DbUtils.closeQuietly(null, ps, rs); 
                db2.commit(); 
            }
            log.debug(" shtuaruCd " + shinsei._shutaruCd + " => " + shutaruCd);
            return shutaruCd;
        }

        public static String sql(final Param param) { 
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX AS ( ");
            stb.append("   SELECT KOJIN_NO, SHINSEI_YEAR, SHIKIN_SHOUSAI_DIV, MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("   FROM KOJIN_SHINSEI_HIST_DAT ");
            stb.append("   WHERE KOJIN_NO = '" + param._kojinNo + "' ");
            stb.append("    AND SHINSEI_YEAR = '" + param._shinseiYear + "' ");
            stb.append("    AND SHIKIN_SHOUSAI_DIV = '" + param._shikinShousaiDiv + "' ");
            stb.append("   GROUP BY KOJIN_NO, SHINSEI_YEAR, SHIKIN_SHOUSAI_DIV ");
            stb.append(" ) ");
            stb.append(" SELECT T1.* ");
            stb.append(" FROM KOJIN_SHINSEI_HIST_DAT T1 ");
            stb.append(" INNER JOIN MAX T2 ON T2.KOJIN_NO = T1.KOJIN_NO AND T2.SHINSEI_YEAR = T1.SHINSEI_YEAR AND T2.SHIKIN_SHOUSAI_DIV = T1.SHIKIN_SHOUSAI_DIV ");
            stb.append("  AND T2.ISSUEDATE = T1.ISSUEDATE ");
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
               taiyoyoyaku._rentai = ShinkenshaHistDat.load(db2, param._shinseiYear, taiyoyoyaku._rentaiCd);
               taiyoyoyaku._shinken1 = ShinkenshaHistDat.load(db2, param._shinseiYear, taiyoyoyaku._shinken1Cd);
               taiyoyoyaku._shinken2 = ShinkenshaHistDat.load(db2, param._shinseiYear, taiyoyoyaku._shinken2Cd);
               taiyoyoyaku._shutaru = ShinkenshaHistDat.load(db2, param._shinseiYear, taiyoyoyaku._shutaruCd);
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

        public String getKyuuchiKasanGk(final DB2UDB db2, final String kyuuchiCd) {
            String kyuuchiKasanGk = null;
            String setaiCount = null;
//            if (!NumberUtils.isNumber(_setaiCount)) {
//                setaiCount = _areaChousaCount;
//            } else if (!NumberUtils.isNumber(_areaChousaCount)) {
//                setaiCount = _setaiCount;
//            } else {
//                setaiCount = String.valueOf(Integer.parseInt(_areaChousaCount) + Integer.parseInt(_setaiCount));
//            }
            setaiCount = _areaChousaCount;
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
             return "0";
         }
         
         public static KojinTaiyoShinsaKekkaDat load(final DB2UDB db2, final Param param, final String shinseiYearArg, final String shikinShousaiDiv, final String issuedate, final String setaiSeq) {
             KojinTaiyoShinsaKekkaDat ktskd = null;
             PreparedStatement ps = null;
             ResultSet rs = null;
             final String sql = sql(param._kojinNo, shinseiYearArg, shikinShousaiDiv, issuedate, param._kekkaDiv, setaiSeq);
             log.debug(" shinsa kekka sql = " + sql);
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
             if (null != issuedate) {
                 stb.append("     AND T1.ISSUEDATE = '" + issuedate + "' ");
             }
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

        public static List load(final DB2UDB db2, final String kojinNo, final String shinseiYear, final String setaiSeq) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(kojinNo, shinseiYear, setaiSeq));
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

        public static ShinkenshaHistDat load(final DB2UDB db2, final String year, final String paramShinkenCd) {
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
            log.debug(" shinken = " + shinkenshahistdat + ", " + paramShinkenCd );
            return shinkenshahistdat;
        }

        public static String sql(final String paramShinkenCd) {
            final StringBuffer stb = new StringBuffer();
//          stb.append(" WITH MAX_DATE AS ( ");
//          stb.append("   SELECT SHINKEN_CD, MAX(ISSUEDATE) AS ISSUEDATE ");
//          stb.append("   FROM SHINKENSHA_HIST_DAT ");
//          stb.append(" WHERE SHINKEN_CD = '" + paramShinkenCd + "' ");
//          stb.append("   GROUP BY SHINKEN_CD ");
//          stb.append(" ) ");
          stb.append(" SELECT * ");
          stb.append(" FROM SHINKENSHA_HIST_DAT T1 ");
          stb.append(" WHERE SHINKEN_CD = '" + paramShinkenCd + "' ");
//          stb.append(" INNER JOIN MAX_DATE T2 ON T2.SHINKEN_CD = T1.SHINKEN_CD AND T2.ISSUEDATE = T1.ISSUEDATE ");
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
        final String _registercd;
        final String _updated;
        final String _katei;
        final String _kateiName;
        final String _shuugakuMonthCount;

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
                final String registercd,
                final String updated,
                final String katei,
                final String kateiName,
                final String shuugakuMonthCount
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
            _registercd = registercd;
            _updated = updated;
            _katei = katei;
            _kateiName = kateiName;
            _shuugakuMonthCount = shuugakuMonthCount;
        }

        public static SchoolDat load(final DB2UDB db2, final String paramSchoolcd) {
            SchoolDat schooldat = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                 ps = db2.prepareStatement(sql(paramSchoolcd));
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
                     final String registercd = rs.getString("REGISTERCD");
                     final String updated = rs.getString("UPDATED");
                     final String katei = rs.getString("KATEI");
                     final String kateiName = rs.getString("KATEI_NAME");
                     final String shuugakuMonthCount = rs.getString("SHUUGAKU_MONTH_COUNT");
                     schooldat = new SchoolDat(schoolcd, schoolType, schoolDistcd, schoolDiv, name, kana, nameAbbv, kanaAbbv, districtcd, prefCd, zipcd,
                             addr1, addr2, telno, faxno, edboardcd, fuNaigaiDiv, haishiFlg, atenaInsatsuFlg, remark, registercd, updated, katei, kateiName, shuugakuMonthCount);
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
            stb.append(" SELECT T1.*, T2.KATEI, T3.KATEI_NAME, T3.SHUUGAKU_MONTH_COUNT ");
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

    

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67179 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _kojinNo;
        private final String _shinseiYear;
        private final String _shikinShousaiDiv;
        private final String _ukeYear;
        private final String _ukeNo;
        private final String _ukeEdaban;
        private final String _prgId;
        private final String _issueDate;
        
        private final String _kekkaDiv = "2";// "審査２"
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _prgId = request.getParameter("getPrgId");
            _kojinNo = request.getParameter("sendKojinNo");
            _shinseiYear = request.getParameter("sendShinseiYear");
            _shikinShousaiDiv = request.getParameter("sendShikinshousaiDiv");
            _ukeYear = request.getParameter("sendUkeYear");
            _ukeNo = request.getParameter("sendUkeNo");
            _ukeEdaban = request.getParameter("sendUkeEdaban");
            _issueDate = request.getParameter("sendIssueDate");
        }
        
        public boolean isTaiyoyoyaku() {
            return _prgId.equals("KNJTA020_01") || _prgId.equals("KNJTA020_09");
        }
        
        public boolean needShikinShousai04Shutaru() {
            // パラメータのSHIKIN_SHOUSAI_DIVのSHUTARU_CDはnullのため資金詳細04の主たるを取得
            return _prgId.equals("KNJTA020_02") || _prgId.equals("KNJTA020_08");
        }
        
        public String nendoToMaxDate() {
            return (Integer.parseInt(_shinseiYear) + 1) + "-03-31";
        }

        public String nendoToMinDate() {
            return _shinseiYear + "-04-01";
        }
    }
}

// eof

