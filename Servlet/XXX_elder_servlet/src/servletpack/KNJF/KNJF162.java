// kanji=漢字
/*
 * $Id: 776fc195974f7cdf00abc140fc478c3a60b5bdae $
 *
 * 作成日: 2010/06/08 13:38:26 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJF162 {

    private static final Log log = LogFactory.getLog("KNJF162.class");

    private boolean _hasData;

    Param _param;

    private static final String FORM1_PAGE1 = "KNJF162_1_1.frm";
    private static final String FORM1_PAGE2 = "KNJF162_1_2.frm";
    private static final String FORM1_PAGE3 = "KNJF162_1_3.frm";
    private static final String FORM1_PAGE4 = "KNJF162_1_4.frm";
    private static final String FORM2_PAGE1 = "KNJF162_2_1.frm";
    private static final String FORM2_PAGE2 = "KNJF162_2_2.frm";
    private static final String FORM2_PAGE3 = "KNJF162_2_3.frm";

    private static final String FORM1_PAGE1P = "KNJF162_1_1P.frm";
    private static final String FORM1_PAGE2P = "KNJF162_1_2P.frm";
    private static final String FORM1_PAGE3P = "KNJF162_1_3P.frm";
    private static final String FORM1_PAGE4P = "KNJF162_1_4P.frm";
    private static final String FORM2_PAGE1P = "KNJF162_2_1P.frm";
    private static final String FORM2_PAGE2P = "KNJF162_2_2P.frm";
    private static final String FORM2_PAGE3P = "KNJF162_2_3P.frm";

    private static final String SELECT_HR = "1"; //1:クラス 2:個人

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
            init(response, svf);

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
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws Exception {
        final List healthQuestionList = createHealthQuestion(db2);
        final List students = createStudents(db2);
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
        	if ("P".equals(student._schoolKind)) {
                if (_param.isGrade1()) {
                    //保健調査票（小１）
                    printForm1Page1(db2, svf, student);
                    printForm1Page2(svf, student);
                    printForm1Page3(svf, student);
                    printForm1Page4(svf, student, healthQuestionList);
                } else {
                    //保健調査票（小２〜小６）
                    printForm2Page1(db2, svf, student);
                    printForm2Page2(svf, student, healthQuestionList);
                    printForm2Page3(svf, student);
                }
        	} else {
                if (_param.isChuKouIkkan()) {
            		// 中学以降は中高一貫の学校の場合は、
                	// 中学１年生以外（高校１年生など）は中２～高３と印字するようにする。
                	if ("01".equals(_param._grade)) {
                        //保健調査票（中１） or （高１）
                        printForm1Page1(db2, svf, student);
                        printForm1Page2(svf, student);
                        printForm1Page3(svf, student);
                        printForm1Page4(svf, student, healthQuestionList);
                	} else {
                        //保健調査票（中２〜高３） or （中２〜中３） or （高２〜高３）
                        printForm2Page1(db2, svf, student);
                        printForm2Page2(svf, student, healthQuestionList);
                        printForm2Page3(svf, student);
                	}
                } else {
                    if (_param.isGrade1()) {
                        //保健調査票（中１） or （高１）
                        printForm1Page1(db2, svf, student);
                        printForm1Page2(svf, student);
                        printForm1Page3(svf, student);
                        printForm1Page4(svf, student, healthQuestionList);
                    } else {
                        //保健調査票（中２〜高３） or （中２〜中３） or （高２〜高３）
                        printForm2Page1(db2, svf, student);
                        printForm2Page2(svf, student, healthQuestionList);
                        printForm2Page3(svf, student);
                    }
                }
        	}
            _hasData = true;
        }
    }

    private int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    private void printForm1Page1(final DB2UDB db2, final Vrw32alp svf, final Student student) {
    	svf.VrSetForm("P".equals(student._schoolKind) ? FORM1_PAGE1P : FORM1_PAGE1, 1);
        svf.VrsOut("TITLE"      , _param.getTitle());
        svf.VrsOut("NENDO"      , _param.getTitle2());
        svf.VrsOut("SCHREGNO"   , student._schregno);
        svf.VrsOut("SCHOOL_KIND", student.getGradeName());
        svf.VrsOut("HR_NAME"    , student.getHrName());
        svf.VrsOut("ATTENDNO"   , student.getAttendNo());
        svf.VrsOut("NAME"       , student._name);
        svf.VrsOut("KANA"       , student._kana);
        putGengou1(db2, svf, "ERA_NAME");
        svf.VrEndPage();
    }

    private void printForm1Page2(final Vrw32alp svf, final Student student) throws SQLException {
        svf.VrSetForm("P".equals(student._schoolKind) ? FORM1_PAGE2P : FORM1_PAGE2, 1);
        svf.VrsOut("TITLE"      , _param.getTitle());
        svf.VrsOut("SCHREGNO"   , student._schregno);
        svf.VrsOut("SCHOOL_KIND", student.getGradeName());
        svf.VrsOut("HR_NAME"    , student.getHrName());
        svf.VrsOut("ATTENDNO"   , student.getAttendNo());
        svf.VrsOut("NAME"       , student._name);
        printAddressInfo(svf, student);
        printSchregEnvir(svf, student);
        printHealthNurseEnt(svf, student);
        svf.VrEndPage();
    }

    private void printForm1Page3(final Vrw32alp svf, final Student student) throws SQLException {
        svf.VrSetForm("P".equals(student._schoolKind) ? FORM1_PAGE3P : FORM1_PAGE3, 1);
        svf.VrsOut("TITLE"      , _param.getTitle());
        svf.VrsOut("SCHREGNO"   , student._schregno);
        printHealthBefSickrec(svf, student);
        printHealthAftSickrec(svf, student);
        printHealthInvestOther(svf, student);
        svf.VrEndPage();
    }

    private void printForm1Page4(final Vrw32alp svf, final Student student, final List healthQuestionList) throws SQLException {
        svf.VrSetForm("P".equals(student._schoolKind) ? FORM1_PAGE4P : FORM1_PAGE4, 1);
        svf.VrsOut("TITLE"      , _param.getTitle());
        svf.VrsOut("SCHREGNO"   , student._schregno);
        printHealthQuestion(svf, healthQuestionList);
        printSchregRela(svf, student);
        svf.VrEndPage();
    }

    private static int length(final String s) {
        return null == s ? 0 : s.length();
    }

    private void printForm2Page1(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("P".equals(student._schoolKind) ? FORM2_PAGE1P : FORM2_PAGE1, 1);
        svf.VrsOut("TITLE"      , _param.getTitle());
        svf.VrsOut("NENDO"      , _param._nendo);
        svf.VrsOut("SCHREGNO"   , student._schregno);
        svf.VrsOut("SCHOOL_KIND", student.getGradeName());
        svf.VrsOut("HR_NAME"    , student.getHrName());
        svf.VrsOut("ATTENDNO"   , student.getAttendNo());
        final String fieldNo = (17 < length(student._name)) ? "3" : (10 < length(student._name)) ? "2" : "";
        svf.VrsOut("NAME" + fieldNo, student._name);
        final String kanaField = getMS932ByteLength(student._kana) > 40 ? "KANA2" : "KANA";
        svf.VrsOut(kanaField    , student._kana);
        putGengou1(db2, svf, "ERA_NAME");
        svf.VrEndPage();
    }

    private void printForm2Page2(final Vrw32alp svf, final Student student, final List healthQuestionList) {
        svf.VrSetForm("P".equals(student._schoolKind) ? FORM2_PAGE2P : FORM2_PAGE2, 1);
        svf.VrsOut("TITLE"      , _param.getTitle());
        svf.VrsOut("SCHREGNO"   , student._schregno);
        printHealthNurseEnt(svf, student);
        printHealthQuestion(svf, healthQuestionList);
        svf.VrEndPage();
    }

    private void printForm2Page3(final Vrw32alp svf, final Student student) {
        svf.VrSetForm("P".equals(student._schoolKind) ? FORM2_PAGE3P : FORM2_PAGE3, 1);
        svf.VrsOut("TITLE"      , _param.getTitle());
        svf.VrsOut("SCHREGNO"   , student._schregno);
        printAddressInfo(svf, student);
        printSchregClubHist(svf, student);
        svf.VrEndPage();
    }

    private void printAddressInfo(final Vrw32alp svf, final Student student) {
        final AddressInfo addressInfo = student._addressInfo;
        if (null != addressInfo) {
            //緊急連絡先(中1)(中2〜高3)
            final String fieldNo = (17 < length(addressInfo._emergencyName)) ? "1_3" : (10 < length(addressInfo._emergencyName)) ? "1_2" : "";
            svf.VrsOut("EMERGENCYNAME" + fieldNo, addressInfo._emergencyName);
            svf.VrsOut("EMERGENCYRELA_NAME" , addressInfo._emergencyRelaName);
            svf.VrsOut("EMERGENCYCALL"      , addressInfo._emergencyCall);
            svf.VrsOut("EMERGENCYTELNO"     , addressInfo._emergencyTelno);
            final String fieldNo2 = (17 < length(addressInfo._emergencyName2)) ? "_3" : (10 < length(addressInfo._emergencyName2)) ? "_2" : "";
            svf.VrsOut("EMERGENCYNAME2" + fieldNo2, addressInfo._emergencyName2);
            svf.VrsOut("EMERGENCYRELA_NAME2", addressInfo._emergencyRelaName2);
            svf.VrsOut("EMERGENCYCALL2"     , addressInfo._emergencyCall2);
            svf.VrsOut("EMERGENCYTELNO2"    , addressInfo._emergencyTelno2);
            //現住所
            svf.VrsOut("ZIPCD"  , addressInfo.getZipcd());
            svf.VrsOut("ADDR1"  , addressInfo.getAddr1());
            svf.VrsOut("ADDR2"  , addressInfo.getAddr2());
            svf.VrsOut("TELNO"  , addressInfo._telno);
            //保護者
            svf.VrsOut("GUARD_NAME"     , addressInfo._guardName);
            svf.VrsOut("GUARD_KANA"     , addressInfo._guardKana);
            svf.VrsOut("RELATION"       , getNameMst("H201", addressInfo._relationship));
            if (!addressInfo.isSameAddress()) {
                svf.VrsOut("GUARD_ZIPCD", addressInfo.getGuardZipcd());
                svf.VrsOut("GUARD_ADDR1", addressInfo.getGuardAddr1());
                svf.VrsOut("GUARD_ADDR2", addressInfo.getGuardAddr2());
            }
            svf.VrsOut("GUARD_WORK_NAME", addressInfo._guardWorkName);
            svf.VrsOut("GUARD_TELNO"    , addressInfo._guardTelno);
        }
    }

    private void printSchregClubHist(final Vrw32alp svf, final Student student) {
        String clubname = "";
        String seq = "";
        for (final Iterator it = student._schregClubHistList.iterator(); it.hasNext();) {
            final SchregClubHist schregClubHist = (SchregClubHist) it.next();
            //5.部活(中2〜高3)
            clubname = clubname + seq + schregClubHist.getClubname();
            seq = "、";
        }
        svf.VrsOut("CLUBNAME"   , clubname);
    }

    private void printSchregEnvir(final Vrw32alp svf, final Student student) {
        final SchregEnvir schregEnvir = student._schregEnvir;
        if (null != schregEnvir) {
            //通学時間など
            svf.VrsOut("COMMUTE_HOURS"  , schregEnvir._commuteHours);
            svf.VrsOut("COMMUTE_MINUTES", schregEnvir._commuteMinutes);
            svf.VrsOut("ROSEN"          , schregEnvir._lineName);
            svf.VrsOut("JOSYA"          , schregEnvir._stationName);
        }
    }

    private void printSchregRela(final Vrw32alp svf, final Student student) {
        int gyo = 0;
        for (final Iterator it = student._schregRelaList.iterator(); it.hasNext();) {
            final SchregRela schregRela = (SchregRela) it.next();
            //9.家族
            gyo++;
            svf.VrsOutn("RELANAME"     , gyo  , schregRela._relaname);
            svf.VrsOutn("RELAKANA"     , gyo  , schregRela._relakana);
            svf.VrsOutn("RELATIONSHIP" , gyo  , getNameMst("H201", schregRela._relationship));
            svf.VrsOutn("RELABIRHTDAY" , gyo  , getDateWareki(schregRela._relabirthday));
        }
    }

    private void printHealthNurseEnt(final Vrw32alp svf, final Student student) {
        final HealthNurseEnt healthNurseEnt = student._healthNurseEnt;
        if (null != healthNurseEnt) {
            //保険証(中1)(中2〜高3)
            svf.VrsOut("INSURED_NAME"   , healthNurseEnt._insuredName);
            svf.VrsOut("INSURED_MARK"   , healthNurseEnt._insuredMark);
            svf.VrsOut("INSURED_NO"     , healthNurseEnt._insuredNo);
            svf.VrsOut("INSURANCE_NAME" , healthNurseEnt._insuranceName);
            svf.VrsOut("INSURANCE_NO"   , healthNurseEnt._insuranceNo);
            svf.VrsOut("VALID_DATE"     , getDateWareki(healthNurseEnt._validDate));
            svf.VrsOut("AUTHORIZE_DATE" , getDateWareki(healthNurseEnt._authorizeDate));
            svf.VrsOut("RELATIONSHIP"   , getNameMst("F240", healthNurseEnt._relationship));
        }
    }

    private void printHealthBefSickrec(final Vrw32alp svf, final Student student) {
        int gyo = 0;
        for (final Iterator it = student._healthBefSickrecList.iterator(); it.hasNext();) {
            final HealthBefSickrec healthBefSickrec = (HealthBefSickrec) it.next();
            //1.過去の入院歴
            gyo++;
            svf.VrsOut("BEF_DISEASE"   + gyo  , healthBefSickrec._disease);
            svf.VrsOut("BEF_S_YEAR"    + gyo  , healthBefSickrec._sYear);
            svf.VrsOut("BEF_S_MONTH"   + gyo  , healthBefSickrec._sMonth);
            svf.VrsOut("BEF_E_YEAR"    + gyo  , healthBefSickrec._eYear);
            svf.VrsOut("BEF_E_MONTH"   + gyo  , healthBefSickrec._eMonth);
            svf.VrsOut("BEF_SITUATION" + gyo  , healthBefSickrec._situation);
        }
    }

    private void printHealthAftSickrec(final Vrw32alp svf, final Student student) {
        int gyo = 0;
        for (final Iterator it = student._healthAftSickrecList.iterator(); it.hasNext();) {
            final HealthAftSickrec healthAftSickrec = (HealthAftSickrec) it.next();
            //2.現在治療中
            gyo++;
            svf.VrsOut("AFT_DISEASE"   + gyo  , healthAftSickrec._disease);
            svf.VrsOut("AFT_S_YEAR"    + gyo  , healthAftSickrec._sYear);
            svf.VrsOut("AFT_S_MONTH"   + gyo  , healthAftSickrec._sMonth);
            svf.VrsOut("AFT_E_YEAR"    + gyo  , healthAftSickrec._eYear);
            svf.VrsOut("AFT_E_MONTH"   + gyo  , healthAftSickrec._eMonth);
            svf.VrsOut("AFT_HOSPITAL"  + gyo  , healthAftSickrec._hospital);
            svf.VrsOut("AFT_DOCTOR"    + gyo  , healthAftSickrec._doctor);
            svf.VrsOut("AFT_TELNO"     + gyo  , healthAftSickrec._telno);
            svf.VrsOut("AFT_MEDICINE"  + gyo  , healthAftSickrec._medicine);
            svf.VrsOut("AFT_SITUATION" + gyo  , healthAftSickrec._situation);
        }
    }

    private void printHealthInvestOther(final Vrw32alp svf, final Student student) {
        final HealthInvestOther healthInvestOther = student._healthInvestOther;
        if (null != healthInvestOther) {
            //3.アレルギー
            svf.VrsOut("ALLERGY_MEDICINE"   , healthInvestOther._allergyMedicine);
            svf.VrsOut("ALLERGY_FOOD"       , healthInvestOther._allergyFood);
            svf.VrsOut("ALLERGY_OTHER"      , healthInvestOther._allergyOther);
            //4.血液型
            svf.VrsOut("BLOOD"  , healthInvestOther._blood);
            svf.VrsOut("RH"     , healthInvestOther._rh);
            //5.過去の病気・けが
            svf.VrsOut("MEASLES_AGE"            , healthInvestOther._measlesAge);
            svf.VrsOut("VARICELLA_AGE"          , healthInvestOther._varicellaAge);
            svf.VrsOut("TB_AGE"                 , healthInvestOther._tbAge);
            svf.VrsOut("INFECTION_AGE"          , healthInvestOther._infectionAge);
            svf.VrsOut("G_MEASLES_AGE"          , healthInvestOther._gMeaslesAge);
            svf.VrsOut("OTITIS_MEDIA_AGE"       , healthInvestOther._otitisMediaAge);
            svf.VrsOut("KAWASAKI_AGE"           , healthInvestOther._kawasakiAge);
            svf.VrsOut("MUMPS_AGE"              , healthInvestOther._mumpsAge);
            svf.VrsOut("HEART_DISEASE"          , healthInvestOther._heartDisease);
            svf.VrsOut("HEART_S_AGE"            , healthInvestOther._heartSAge);
            svf.VrsOut("HEART_SITUATION"        , getNameMst("F230", healthInvestOther._heartSituation));
            svf.VrsOut("HEART_E_AGE"            , healthInvestOther._heartEAge);
            svf.VrsOut("KIDNEY_DISEASE"         , healthInvestOther._kidneyDisease);
            svf.VrsOut("KIDNEY_S_AGE"           , healthInvestOther._kidneySAge);
            svf.VrsOut("KIDNEY_SITUATION"       , getNameMst("F230", healthInvestOther._kidneySituation));
            svf.VrsOut("KIDNEY_E_AGE"           , healthInvestOther._kidneyEAge);
            svf.VrsOut("ASTHMA_S_AGE"           , healthInvestOther._asthmaSAge);
            svf.VrsOut("ASTHMA_SITUATION"       , getNameMst("F230", healthInvestOther._asthmaSituation));
            svf.VrsOut("ASTHMA_E_AGE"           , healthInvestOther._asthmaEAge);
            svf.VrsOut("CONVULSIONS_S_AGE"      , healthInvestOther._convulsionsSAge);
            svf.VrsOut("CONVULSIONS_SITUATION"  , getNameMst("F230", healthInvestOther._convulsionsSituation));
            svf.VrsOut("CONVULSIONS_E_AGE"      , healthInvestOther._convulsionsEAge);
            svf.VrsOut("OTHER_DISEASE"          , healthInvestOther._otherDisease);
            //6.予防接種
            svf.VrsOut("TUBERCULIN"         , getNameMst("F231", healthInvestOther._tuberculin));
            svf.VrsOut("TUBERCULIN_YEAR"    , healthInvestOther._tuberculinYear);
            svf.VrsOut("TUBERCULIN_MONTH"   , healthInvestOther._tuberculinMonth);
            svf.VrsOut("TUBERCULIN_JUDGE"   , getNameMst("F232", healthInvestOther._tuberculinJudge));
            svf.VrsOut("G_MEASLES"         , getNameMst("F231", healthInvestOther._gMeasles));
            svf.VrsOut("G_MEASLES_YEAR"    , healthInvestOther._gMeaslesYear);
            svf.VrsOut("G_MEASLES_MONTH"   , healthInvestOther._gMeaslesMonth);
            svf.VrsOut("BCG"         , getNameMst("F231", healthInvestOther._bcg));
            svf.VrsOut("BCG_YEAR"    , healthInvestOther._bcgYear);
            svf.VrsOut("BCG_MONTH"   , healthInvestOther._bcgMonth);
            svf.VrsOut("VARICELLA"         , getNameMst("F231", healthInvestOther._varicella));
            svf.VrsOut("VARICELLA_YEAR"    , healthInvestOther._varicellaYear);
            svf.VrsOut("VARICELLA_MONTH"   , healthInvestOther._varicellaMonth);
            svf.VrsOut("POLIO"         , getNameMst("F231", healthInvestOther._polio));
            svf.VrsOut("POLIO_YEAR"    , healthInvestOther._polioYear);
            svf.VrsOut("POLIO_MONTH"   , healthInvestOther._polioMonth);
            svf.VrsOut("MUMPS"         , getNameMst("F231", healthInvestOther._mumps));
            svf.VrsOut("MUMPS_YEAR"    , healthInvestOther._mumpsYear);
            svf.VrsOut("MUMPS_MONTH"   , healthInvestOther._mumpsMonth);
            svf.VrsOut("ENCEPHALITIS"          , getNameMst("F231", healthInvestOther._encephalitis));
            svf.VrsOut("ENCEPHALITIS_YEAR1"    , healthInvestOther._encephalitisYear1);
            svf.VrsOut("ENCEPHALITIS_YEAR2"    , healthInvestOther._encephalitisYear2);
            svf.VrsOut("ENCEPHALITIS_YEAR3"    , healthInvestOther._encephalitisYear3);
            svf.VrsOut("ENCEPHALITIS_YEAR4"    , healthInvestOther._encephalitisYear4);
            svf.VrsOut("ENCEPHALITIS_YEAR5"    , healthInvestOther._encephalitisYear5);
            svf.VrsOut("ENCEPHALITIS_MONTH1"   , healthInvestOther._encephalitisMonth1);
            svf.VrsOut("ENCEPHALITIS_MONTH2"   , healthInvestOther._encephalitisMonth2);
            svf.VrsOut("ENCEPHALITIS_MONTH3"   , healthInvestOther._encephalitisMonth3);
            svf.VrsOut("ENCEPHALITIS_MONTH4"   , healthInvestOther._encephalitisMonth4);
            svf.VrsOut("ENCEPHALITIS_MONTH5"   , healthInvestOther._encephalitisMonth5);
            svf.VrsOut("MIXED"          , getNameMst("F231", healthInvestOther._mixed));
            svf.VrsOut("MIXED_YEAR1"    , healthInvestOther._mixedYear1);
            svf.VrsOut("MIXED_YEAR2"    , healthInvestOther._mixedYear2);
            svf.VrsOut("MIXED_YEAR3"    , healthInvestOther._mixedYear3);
            svf.VrsOut("MIXED_YEAR4"    , healthInvestOther._mixedYear4);
            svf.VrsOut("MIXED_YEAR5"    , healthInvestOther._mixedYear5);
            svf.VrsOut("MIXED_MONTH1"   , healthInvestOther._mixedMonth1);
            svf.VrsOut("MIXED_MONTH2"   , healthInvestOther._mixedMonth2);
            svf.VrsOut("MIXED_MONTH3"   , healthInvestOther._mixedMonth3);
            svf.VrsOut("MIXED_MONTH4"   , healthInvestOther._mixedMonth4);
            svf.VrsOut("MIXED_MONTH5"   , healthInvestOther._mixedMonth5);
            //7.麻疹（はしか）調査
            final String maru = "○";
            svf.VrsOut("MEASLES"        + healthInvestOther.getMeasles()  , maru);
            svf.VrsOut("CONFIRMATION"   + healthInvestOther.getMeasles()  , getNameMst("F235", healthInvestOther._confirmation));
            svf.VrsOut("MEASLES_TIMES"  , getNameMst("F233", healthInvestOther._measlesTimes));
            svf.VrsOut("MEASLES_YEAR1"  , healthInvestOther._measlesYear1);
            svf.VrsOut("MEASLES_YEAR2"  , healthInvestOther._measlesYear2);
            svf.VrsOut("MEASLES_YEAR3"  , healthInvestOther._measlesYear3);
            svf.VrsOut("MEASLES_MONTH1" , healthInvestOther._measlesMonth1);
            svf.VrsOut("MEASLES_MONTH2" , healthInvestOther._measlesMonth2);
            svf.VrsOut("MEASLES_MONTH3" , healthInvestOther._measlesMonth3);
            svf.VrsOut("VACCINE"        , getNameMst("F234", healthInvestOther._vaccine));
            svf.VrsOut("LOT_NO"         , healthInvestOther._lotNo);
            svf.VrsOut("A_MEASLES"      + healthInvestOther.getAMeasles()  , maru);
            svf.VrsOut("A_CONFIRMATION" + healthInvestOther.getAMeasles()  , getNameMst("F235", healthInvestOther._aConfirmation));
            svf.VrsOut("A_MEASLES_AGE"  , healthInvestOther._aMeaslesAge);
            svf.VrsOut("ANTIBODY"           + healthInvestOther.getAntibody()  , maru);
            svf.VrsOut("ANTIBODY_YEAR"      , healthInvestOther._antibodyYear);
            svf.VrsOut("ANTIBODY_MONTH"     , healthInvestOther._antibodyMonth);
            svf.VrsOut("ANTIBODY_POSITIVE"  , healthInvestOther.getAntibodyPositive());
        }
    }

    private void printHealthQuestion(final Vrw32alp svf, final List healthQuestionList) {
        int gyo = 0;
        for (final Iterator it = healthQuestionList.iterator(); it.hasNext();) {
            final HealthQuestion healthQuestion = (HealthQuestion) it.next();
            gyo++;
            //8.健康調査(中1)
            //2.健康調査(中2〜高3)
            if (gyo <= 22) {
                svf.VrsOutn("CONTENTS1"    , gyo        , healthQuestion._contents);
            } else {
                svf.VrsOutn("CONTENTS2"    , (gyo - 22) , healthQuestion._contents);
            }
        }
    }

    private String getNameMst(final String namecd1, final String namecd2) {
        if (null == namecd1 || null == namecd2) return "";
        final String namecd = namecd1 + "-" + namecd2;
        if (_param._nameMstMap.containsKey(namecd)) return (String) _param._nameMstMap.get(namecd);
        return "";
    }

    private String getDateWareki(final String date) {
        if (null == date || "".equals(date)) return "";
        return KNJ_EditDate.h_format_JP(date);
    }

    private String getDateSeireki(final String date) {
        if (null == date || "".equals(date)) return "";
        return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
    }

    private List createStudents(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        for (int i = 0; i < _param._classSelected.length; i++) {
            // 01001(年組) OR 20051015-02001004(学籍-年組番)
            final String classSelected = _param._classSelected[i];
            final String selected = _param._kubun.equals(SELECT_HR) ? classSelected : classSelected.substring(0,(classSelected).indexOf("-"));
            final String sql = sqlStudents(selected);
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String name = rs.getString("NAME");
                    final String kana = rs.getString("NAME_KANA");
                    final String grade = rs.getString("GRADE");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String gradeName = rs.getString("GRADE_NAME1");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_CLASS_NAME1");
                    final String attendNo = rs.getString("ATTENDNO");

                    final Student student = new Student(schregno, name, kana, grade, schoolKind, gradeName, hrClass, hrName, attendNo);
                    student.load(db2);
                    rtn.add(student);
                }
            } catch (final Exception ex) {
                log.error("生徒のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        return rtn;
    }

    private String sqlStudents(final String selected) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_KANA, ");
        stb.append("     T3.HR_CLASS_NAME1, ");
        stb.append("     T4.SCHOOL_KIND, ");
        stb.append("     T4.GRADE_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR ");
        stb.append("                                   AND T3.SEMESTER = T1.SEMESTER ");
        stb.append("                                   AND T3.GRADE = T1.GRADE ");
        stb.append("                                   AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR ");
        stb.append("                                   AND T4.GRADE = T1.GRADE ");
        stb.append(" WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        if (_param._kubun.equals(SELECT_HR)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + selected + "' ");
        } else {
            stb.append("     AND T1.SCHREGNO = '" + selected + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        private final String _schregno;
        private final String _name;
        private final String _kana;
        private final String _grade;
        private final String _schoolKind;
        private final String _gradeName;
        private final String _hrClass;
        private final String _hrName;
        private final String _attendNo;

        private AddressInfo _addressInfo;
        private SchregEnvir _schregEnvir;
        private HealthInvestOther _healthInvestOther;
        private HealthNurseEnt _healthNurseEnt;
        private List _healthBefSickrecList;
        private List _healthAftSickrecList;
        private List _schregRelaList;
        private List _schregClubHistList;

        public Student(
                final String schregno,
                final String name,
                final String kana,
                final String grade,
                final String schoolKind,
                final String gradeName,
                final String hrClass,
                final String hrName,
                final String attendNo
        ) {
            _schregno = schregno;
            _name = name;
            _kana = kana;
            _grade = grade;
            _schoolKind = schoolKind;
            _gradeName = gradeName;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendNo = attendNo;
        }

        private String getGradeName() {
            return (null == _gradeName) ? "" : _gradeName;
        }

        private String getHrName() {
            final String hrName;
            if (null != _hrName) {
                hrName = _hrName + "組";
            } else {
                hrName = (StringUtils.isNumeric(_hrClass) ? String.valueOf(Integer.parseInt(_hrClass)) : _hrClass) + "組";
            }
            return hrName;
        }

        private String getAttendNo() {
            return (null == _attendNo) ? "" : String.valueOf(Integer.parseInt(_attendNo));
        }

        private void load(final DB2UDB db2) throws SQLException {
            createAddressInfo(db2);
            createSchregEnvir(db2);
            createHealthInvestOther(db2);
            createHealthNurseEnt(db2);
            _healthBefSickrecList = createHealthBefSickrec(db2);
            _healthAftSickrecList = createHealthAftSickrec(db2);
            _schregRelaList = createSchregRela(db2);
            _schregClubHistList = createSchregClubHist(db2);
        }

        private void createAddressInfo(final DB2UDB db2) throws SQLException {
            final String sql = sqlAddressInfo();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _addressInfo = new AddressInfo(rs.getString("SCHREGNO"));
                    _addressInfo.setData(rs);
                }
            } catch (final Exception ex) {
                log.error("住所などのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlAddressInfo() {
            StringBuffer stb = new StringBuffer();
            //生徒の住所(最新)
            stb.append(" WITH MAX_ADDRESS AS ( ");
            stb.append("     SELECT ");
            stb.append("         L1.SCHREGNO, ");
            stb.append("         max(L1.ISSUEDATE) as ISSUEDATE ");
            stb.append("     FROM ");
            stb.append("         SCHREG_ADDRESS_DAT L1 ");
            stb.append("     WHERE ");
            stb.append("             L1.SCHREGNO    = '" + _schregno + "' ");
            stb.append("         AND L1.ISSUEDATE  <= date('" + _param._ctrlDate + "') ");
            stb.append("         AND L1.EXPIREDATE >= date('" + _param._ctrlDate + "') ");
            stb.append("     GROUP BY ");
            stb.append("         L1.SCHREGNO ");
            stb.append("     ) ");
            stb.append(" , T_ADDRESS AS ( ");
            stb.append("     SELECT ");
            stb.append("         L1.SCHREGNO, ");
            stb.append("         L1.ZIPCD, ");
            stb.append("         L1.ADDR1, ");
            stb.append("         L1.ADDR2, ");
            stb.append("         L1.TELNO ");
            stb.append("     FROM ");
            stb.append("         SCHREG_ADDRESS_DAT L1 ");
            stb.append("         INNER JOIN MAX_ADDRESS L2 ");
            stb.append("                  ON L2.SCHREGNO  = L1.SCHREGNO ");
            stb.append("                 AND L2.ISSUEDATE = L1.ISSUEDATE ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     T2.EMERGENCYNAME, ");
            stb.append("     T2.EMERGENCYRELA_NAME, ");
            stb.append("     T2.EMERGENCYCALL, ");
            stb.append("     T2.EMERGENCYTELNO, ");
            stb.append("     T2.EMERGENCYNAME2, ");
            stb.append("     T2.EMERGENCYRELA_NAME2, ");
            stb.append("     T2.EMERGENCYCALL2, ");
            stb.append("     T2.EMERGENCYTELNO2, ");
            stb.append("     L1.ZIPCD, ");
            stb.append("     L1.ADDR1, ");
            stb.append("     L1.ADDR2, ");
            stb.append("     L1.TELNO, ");
            stb.append("     L2.GUARD_NAME, ");
            stb.append("     L2.GUARD_KANA, ");
            stb.append("     L2.RELATIONSHIP, ");
            stb.append("     L2.GUARD_ZIPCD, ");
            stb.append("     L2.GUARD_ADDR1, ");
            stb.append("     L2.GUARD_ADDR2, ");
            stb.append("     L2.GUARD_WORK_NAME, ");
            stb.append("     L2.GUARD_TELNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_BASE_MST T2 ");
            stb.append("     LEFT JOIN T_ADDRESS L1 ON L1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     LEFT JOIN GUARDIAN_DAT L2 ON L2.SCHREGNO = T2.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T2.SCHREGNO = '" + _schregno + "' ");
            return stb.toString();
        }

        private List createSchregClubHist(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlSchregClubHist();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final SchregClubHist schregClubHist = new SchregClubHist(rs.getString("SCHREGNO"),
                                                                             rs.getString("CLUBCD"),
                                                                             rs.getString("CLUBNAME"),
                                                                             rs.getString("SDATE"));
                    rtn.add(schregClubHist);
                }
            } catch (final Exception ex) {
                log.error("部活のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlSchregClubHist() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CLUBCD, ");
            stb.append("     L1.CLUBNAME, ");
            stb.append("     T1.SDATE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_CLUB_HIST_DAT T1 ");
            stb.append("     INNER JOIN CLUB_MST L1 ON L1.CLUBCD = T1.CLUBCD ");
            stb.append("     INNER JOIN CLUB_YDAT L2 ON L2.YEAR = '" + _param._year + "' ");
            stb.append("                            AND L2.CLUBCD = L1.CLUBCD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SDATE DESC ");
            return stb.toString();
        }

        private void createSchregEnvir(final DB2UDB db2) throws SQLException {
            final String sql = sqlSchregEnvir();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _schregEnvir = new SchregEnvir(rs.getString("SCHREGNO"));
                    _schregEnvir.setData(rs);
                }
            } catch (final Exception ex) {
                log.error("通学時間などのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlSchregEnvir() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.COMMUTE_HOURS, ");
            stb.append("     T1.COMMUTE_MINUTES, ");
            stb.append("     T1.JOSYA_1, ");
            stb.append("     L2.LINE_NAME, ");
            stb.append("     L2.STATION_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_ENVIR_DAT T1 ");
            stb.append("     LEFT JOIN STATION_NETMST L2 ON L2.STATION_CD = T1.JOSYA_1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregno + "' ");
            return stb.toString();
        }

        private List createSchregRela(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlSchregRela();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final SchregRela schregRela = new SchregRela(rs.getString("SCHREGNO"),
                                                                 rs.getString("RELANO"),
                                                                 rs.getString("RELANAME"),
                                                                 rs.getString("RELAKANA"),
                                                                 rs.getString("RELATIONSHIP"),
                                                                 rs.getString("RELABIRTHDAY"));
                    rtn.add(schregRela);
                }
            } catch (final Exception ex) {
                log.error("家族のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlSchregRela() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     SCHREG_RELA_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     RELANO ");
            return stb.toString();
        }

        private void createHealthNurseEnt(final DB2UDB db2) throws SQLException {
            final String sql = sqlHealthNurseEnt();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _healthNurseEnt = new HealthNurseEnt(rs.getString("SCHREGNO"));
                    _healthNurseEnt.setData(rs);
                }
            } catch (final Exception ex) {
                log.error("保険証のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlHealthNurseEnt() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     HEALTH_NURSE_ENT_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + _schregno + "' ");
            return stb.toString();
        }

        private List createHealthBefSickrec(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlHealthBefSickrec();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final HealthBefSickrec healthBefSickrec = new HealthBefSickrec(rs.getString("SCHREGNO"),
                                                                                   rs.getString("SEQ"),
                                                                                   rs.getString("DISEASE"),
                                                                                   rs.getString("S_YEAR"),
                                                                                   rs.getString("S_MONTH"),
                                                                                   rs.getString("E_YEAR"),
                                                                                   rs.getString("E_MONTH"),
                                                                                   rs.getString("SITUATION"));
                    rtn.add(healthBefSickrec);
                }
            } catch (final Exception ex) {
                log.error("過去の入院歴のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlHealthBefSickrec() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     HEALTH_BEF_SICKREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SEQ ");
            return stb.toString();
        }

        private List createHealthAftSickrec(final DB2UDB db2) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlHealthAftSickrec();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final HealthAftSickrec healthAftSickrec = new HealthAftSickrec(rs.getString("SCHREGNO"),
                                                                                   rs.getString("SEQ"),
                                                                                   rs.getString("DISEASE"),
                                                                                   rs.getString("S_YEAR"),
                                                                                   rs.getString("S_MONTH"),
                                                                                   rs.getString("E_YEAR"),
                                                                                   rs.getString("E_MONTH"),
                                                                                   rs.getString("HOSPITAL"),
                                                                                   rs.getString("DOCTOR"),
                                                                                   rs.getString("TELNO"),
                                                                                   rs.getString("MEDICINE"),
                                                                                   rs.getString("SITUATION"));
                    rtn.add(healthAftSickrec);
                }
            } catch (final Exception ex) {
                log.error("現在治療中のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlHealthAftSickrec() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     HEALTH_AFT_SICKREC_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SEQ ");
            return stb.toString();
        }

        private void createHealthInvestOther(final DB2UDB db2) throws SQLException {
            final String sql = sqlHealthInvestOther();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _healthInvestOther = new HealthInvestOther(rs.getString("SCHREGNO"));
                    _healthInvestOther.setData(rs);
                }
            } catch (final Exception ex) {
                log.error("アレルギーなどのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String sqlHealthInvestOther() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     HEALTH_INVEST_OTHER_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + _schregno + "' ");
            return stb.toString();
        }

        public String toString() {
            return _schregno + ":" + _name;
        }
    }

    private class AddressInfo {
        private final String _schregno;
        //SCHREG_BASE_MST
        private String _emergencyName;
        private String _emergencyRelaName;
        private String _emergencyCall;
        private String _emergencyTelno;
        private String _emergencyName2;
        private String _emergencyRelaName2;
        private String _emergencyCall2;
        private String _emergencyTelno2;
        //SCHREG_ADDRESS_DAT
        private String _zipcd;
        private String _addr1;
        private String _addr2;
        private String _telno;
        //GUARDIAN_DAT
        private String _guardName;
        private String _guardKana;
        private String _relationship;
        private String _guardZipcd;
        private String _guardAddr1;
        private String _guardAddr2;
        private String _guardWorkName;
        private String _guardTelno;

        public AddressInfo(final String schregno) {
            _schregno = schregno;
        }

        private void setData(ResultSet rs) throws SQLException {
            _emergencyName      = rs.getString("EMERGENCYNAME");
            _emergencyRelaName  = rs.getString("EMERGENCYRELA_NAME");
            _emergencyCall      = rs.getString("EMERGENCYCALL");
            _emergencyTelno     = rs.getString("EMERGENCYTELNO");
            _emergencyName2     = rs.getString("EMERGENCYNAME2");
            _emergencyRelaName2 = rs.getString("EMERGENCYRELA_NAME2");
            _emergencyCall2     = rs.getString("EMERGENCYCALL2");
            _emergencyTelno2    = rs.getString("EMERGENCYTELNO2");
            _zipcd     = rs.getString("ZIPCD");
            _addr1     = rs.getString("ADDR1");
            _addr2     = rs.getString("ADDR2");
            _telno     = rs.getString("TELNO");
            _guardName      = rs.getString("GUARD_NAME");
            _guardKana      = rs.getString("GUARD_KANA");
            _relationship   = rs.getString("RELATIONSHIP");
            _guardZipcd     = rs.getString("GUARD_ZIPCD");
            _guardAddr1     = rs.getString("GUARD_ADDR1");
            _guardAddr2     = rs.getString("GUARD_ADDR2");
            _guardWorkName  = rs.getString("GUARD_WORK_NAME");
            _guardTelno     = rs.getString("GUARD_TELNO");
        }

        private String getAddr1() {
            return (null == _addr1) ? "" : _addr1;
        }

        private String getAddr2() {
            return (null == _addr2) ? "" : _addr2;
        }

        private String getZipcd() {
            return (null == _zipcd) ? "" : _zipcd;
        }

        private String getGuardAddr1() {
            return (null == _guardAddr1) ? "" : _guardAddr1;
        }

        private String getGuardAddr2() {
            return (null == _guardAddr2) ? "" : _guardAddr2;
        }

        private String getGuardZipcd() {
            return (null == _guardZipcd) ? "" : _guardZipcd;
        }

        private boolean isSameAddress() {
            return null != _guardAddr1 &&
            getZipcd().equals(getGuardZipcd()) &&
            getAddr1().equals(getGuardAddr1()) &&
            getAddr2().equals(getGuardAddr2());
        }
    }

    private class SchregClubHist {
        private final String _schregno;
        private final String _clubcd;
        private final String _clubname;
        private final String _sdate;

        public SchregClubHist(
                final String schregno,
                final String clubcd,
                final String clubname,
                final String sdate
        ) {
            _schregno = schregno;
            _clubcd = clubcd;
            _clubname = clubname;
            _sdate = sdate;
        }

        private String getClubname() {
            return (null == _clubname) ? "" : _clubname;
        }
    }

    private class SchregEnvir {
        private final String _schregno;

        private String _commuteHours;
        private String _commuteMinutes;
        private String _josya1;
        private String _lineName;
        private String _stationName;

        public SchregEnvir(final String schregno) {
            _schregno = schregno;
        }

        private void setData(ResultSet rs) throws SQLException {
            _commuteHours   = rs.getString("COMMUTE_HOURS");
            _commuteMinutes = rs.getString("COMMUTE_MINUTES");
            _josya1         = rs.getString("JOSYA_1");
            _lineName       = rs.getString("LINE_NAME");
            _stationName    = rs.getString("STATION_NAME");
        }
    }

    private class SchregRela {
        private final String _schregno;
        private final String _relano;

        private final String _relaname;
        private final String _relakana;
        private final String _relationship;
        private final String _relabirthday;

        public SchregRela(
                final String schregno,
                final String relano,
                final String relaname,
                final String relakana,
                final String relationship,
                final String relabirthday
        ) {
            _schregno = schregno;
            _relano = relano;
            _relaname = relaname;
            _relakana = relakana;
            _relationship = relationship;
            _relabirthday = relabirthday;
        }
    }

    private class HealthNurseEnt {
        private final String _schregno;

        private String _insuredName;
        private String _insuredMark;
        private String _insuredNo;
        private String _insuranceName;
        private String _insuranceNo;
        private String _validDate;
        private String _authorizeDate;
        private String _relationship;

        public HealthNurseEnt(final String schregno) {
            _schregno = schregno;
        }

        private void setData(ResultSet rs) throws SQLException {
            _insuredName    = rs.getString("INSURED_NAME");
            _insuredMark    = rs.getString("INSURED_MARK");
            _insuredNo      = rs.getString("INSURED_NO");
            _insuranceName  = rs.getString("INSURANCE_NAME");
            _insuranceNo    = rs.getString("INSURANCE_NO");
            _validDate      = rs.getString("VALID_DATE");
            _authorizeDate  = rs.getString("AUTHORIZE_DATE");
            _relationship   = rs.getString("RELATIONSHIP");
        }
    }

    private class HealthBefSickrec {
        private final String _schregno;
        private final String _seq;

        private final String _disease;
        private final String _sYear;
        private final String _sMonth;
        private final String _eYear;
        private final String _eMonth;
        private final String _situation;

        public HealthBefSickrec(
                final String schregno,
                final String seq,
                final String disease,
                final String sYear,
                final String sMonth,
                final String eYear,
                final String eMonth,
                final String situation
        ) {
            _schregno = schregno;
            _seq = seq;
            _disease = disease;
            _sYear = sYear;
            _sMonth = sMonth;
            _eYear = eYear;
            _eMonth = eMonth;
            _situation = situation;
        }
    }

    private class HealthAftSickrec {
        private final String _schregno;
        private final String _seq;

        private final String _disease;
        private final String _sYear;
        private final String _sMonth;
        private final String _eYear;
        private final String _eMonth;
        private final String _hospital;
        private final String _doctor;
        private final String _telno;
        private final String _medicine;
        private final String _situation;

        public HealthAftSickrec(
                final String schregno,
                final String seq,
                final String disease,
                final String sYear,
                final String sMonth,
                final String eYear,
                final String eMonth,
                final String hospital,
                final String doctor,
                final String telno,
                final String medicine,
                final String situation
        ) {
            _schregno = schregno;
            _seq = seq;
            _disease = disease;
            _sYear = sYear;
            _sMonth = sMonth;
            _eYear = eYear;
            _eMonth = eMonth;
            _hospital = hospital;
            _doctor = doctor;
            _telno = telno;
            _medicine = medicine;
            _situation = situation;
        }
    }

    private class HealthInvestOther {
        private final String _schregno;
        //3.アレルギー
        private String _allergyMedicine;
        private String _allergyFood;
        private String _allergyOther;
        //4.血液型
        private String _blood;
        private String _rh;
        //5.過去の病気・けが
        private String _measlesAge;
        private String _varicellaAge;
        private String _tbAge;
        private String _infectionAge;
        private String _gMeaslesAge;
        private String _otitisMediaAge;
        private String _kawasakiAge;
        private String _mumpsAge;
        private String _heartDisease;
        private String _heartSAge;
        private String _heartSituation;
        private String _heartEAge;
        private String _kidneyDisease;
        private String _kidneySAge;
        private String _kidneySituation;
        private String _kidneyEAge;
        private String _asthmaSAge;
        private String _asthmaSituation;
        private String _asthmaEAge;
        private String _convulsionsSAge;
        private String _convulsionsSituation;
        private String _convulsionsEAge;
        private String _otherDisease;
        //6.予防接種
        private String _tuberculin;
        private String _tuberculinYear;
        private String _tuberculinMonth;
        private String _tuberculinJudge;
        private String _gMeasles;
        private String _gMeaslesYear;
        private String _gMeaslesMonth;
        private String _bcg;
        private String _bcgYear;
        private String _bcgMonth;
        private String _varicella;
        private String _varicellaYear;
        private String _varicellaMonth;
        private String _polio;
        private String _polioYear;
        private String _polioMonth;
        private String _mumps;
        private String _mumpsYear;
        private String _mumpsMonth;
        private String _encephalitis;
        private String _encephalitisYear1;
        private String _encephalitisYear2;
        private String _encephalitisYear3;
        private String _encephalitisYear4;
        private String _encephalitisYear5;
        private String _encephalitisMonth1;
        private String _encephalitisMonth2;
        private String _encephalitisMonth3;
        private String _encephalitisMonth4;
        private String _encephalitisMonth5;
        private String _mixed;
        private String _mixedYear1;
        private String _mixedYear2;
        private String _mixedYear3;
        private String _mixedYear4;
        private String _mixedYear5;
        private String _mixedMonth1;
        private String _mixedMonth2;
        private String _mixedMonth3;
        private String _mixedMonth4;
        private String _mixedMonth5;
        //7.麻疹（はしか）調査
        private String _measles;
        private String _measlesTimes;
        private String _measlesYear1;
        private String _measlesYear2;
        private String _measlesYear3;
        private String _measlesMonth1;
        private String _measlesMonth2;
        private String _measlesMonth3;
        private String _vaccine;
        private String _lotNo;
        private String _confirmation;
        private String _aMeasles;
        private String _aMeaslesAge;
        private String _aConfirmation;
        private String _antibody;
        private String _antibodyYear;
        private String _antibodyMonth;
        private String _antibodyPositive;

        public HealthInvestOther(final String schregno) {
            _schregno = schregno;
        }

        private void setData(ResultSet rs) throws SQLException {
            _allergyMedicine = rs.getString("ALLERGY_MEDICINE");
            _allergyFood = rs.getString("ALLERGY_FOOD");
            _allergyOther = rs.getString("ALLERGY_OTHER");
            _blood = rs.getString("BLOOD");
            _rh = rs.getString("RH");
            _measlesAge = rs.getString("MEASLES_AGE");
            _varicellaAge = rs.getString("VARICELLA_AGE");
            _tbAge = rs.getString("TB_AGE");
            _infectionAge = rs.getString("INFECTION_AGE");
            _gMeaslesAge = rs.getString("G_MEASLES_AGE");
            _otitisMediaAge = rs.getString("OTITIS_MEDIA_AGE");
            _kawasakiAge = rs.getString("KAWASAKI_AGE");
            _mumpsAge = rs.getString("MUMPS_AGE");
            _heartDisease = rs.getString("HEART_DISEASE");
            _heartSAge = rs.getString("HEART_S_AGE");
            _heartSituation = rs.getString("HEART_SITUATION");
            _heartEAge = rs.getString("HEART_E_AGE");
            _kidneyDisease = rs.getString("KIDNEY_DISEASE");
            _kidneySAge = rs.getString("KIDNEY_S_AGE");
            _kidneySituation = rs.getString("KIDNEY_SITUATION");
            _kidneyEAge = rs.getString("KIDNEY_E_AGE");
            _asthmaSAge = rs.getString("ASTHMA_S_AGE");
            _asthmaSituation = rs.getString("ASTHMA_SITUATION");
            _asthmaEAge = rs.getString("ASTHMA_E_AGE");
            _convulsionsSAge = rs.getString("CONVULSIONS_S_AGE");
            _convulsionsSituation = rs.getString("CONVULSIONS_SITUATION");
            _convulsionsEAge = rs.getString("CONVULSIONS_E_AGE");
            _otherDisease = rs.getString("OTHER_DISEASE");
            _tuberculin = rs.getString("TUBERCULIN");
            _tuberculinYear = rs.getString("TUBERCULIN_YEAR");
            _tuberculinMonth = rs.getString("TUBERCULIN_MONTH");
            _tuberculinJudge = rs.getString("TUBERCULIN_JUDGE");
            _gMeasles = rs.getString("G_MEASLES");
            _gMeaslesYear = rs.getString("G_MEASLES_YEAR");
            _gMeaslesMonth = rs.getString("G_MEASLES_MONTH");
            _bcg = rs.getString("BCG");
            _bcgYear = rs.getString("BCG_YEAR");
            _bcgMonth = rs.getString("BCG_MONTH");
            _varicella = rs.getString("VARICELLA");
            _varicellaYear = rs.getString("VARICELLA_YEAR");
            _varicellaMonth = rs.getString("VARICELLA_MONTH");
            _polio = rs.getString("POLIO");
            _polioYear = rs.getString("POLIO_YEAR");
            _polioMonth = rs.getString("POLIO_MONTH");
            _mumps = rs.getString("MUMPS");
            _mumpsYear = rs.getString("MUMPS_YEAR");
            _mumpsMonth = rs.getString("MUMPS_MONTH");
            _encephalitis = rs.getString("ENCEPHALITIS");
            _encephalitisYear1 = rs.getString("ENCEPHALITIS_YEAR1");
            _encephalitisYear2 = rs.getString("ENCEPHALITIS_YEAR2");
            _encephalitisYear3 = rs.getString("ENCEPHALITIS_YEAR3");
            _encephalitisYear4 = rs.getString("ENCEPHALITIS_YEAR4");
            _encephalitisYear5 = rs.getString("ENCEPHALITIS_YEAR5");
            _encephalitisMonth1 = rs.getString("ENCEPHALITIS_MONTH1");
            _encephalitisMonth2 = rs.getString("ENCEPHALITIS_MONTH2");
            _encephalitisMonth3 = rs.getString("ENCEPHALITIS_MONTH3");
            _encephalitisMonth4 = rs.getString("ENCEPHALITIS_MONTH4");
            _encephalitisMonth5 = rs.getString("ENCEPHALITIS_MONTH5");
            _mixed = rs.getString("MIXED");
            _mixedYear1 = rs.getString("MIXED_YEAR1");
            _mixedYear2 = rs.getString("MIXED_YEAR2");
            _mixedYear3 = rs.getString("MIXED_YEAR3");
            _mixedYear4 = rs.getString("MIXED_YEAR4");
            _mixedYear5 = rs.getString("MIXED_YEAR5");
            _mixedMonth1 = rs.getString("MIXED_MONTH1");
            _mixedMonth2 = rs.getString("MIXED_MONTH2");
            _mixedMonth3 = rs.getString("MIXED_MONTH3");
            _mixedMonth4 = rs.getString("MIXED_MONTH4");
            _mixedMonth5 = rs.getString("MIXED_MONTH5");
            _measles = rs.getString("MEASLES");
            _measlesTimes = rs.getString("MEASLES_TIMES");
            _measlesYear1 = rs.getString("MEASLES_YEAR1");
            _measlesYear2 = rs.getString("MEASLES_YEAR2");
            _measlesYear3 = rs.getString("MEASLES_YEAR3");
            _measlesMonth1 = rs.getString("MEASLES_MONTH1");
            _measlesMonth2 = rs.getString("MEASLES_MONTH2");
            _measlesMonth3 = rs.getString("MEASLES_MONTH3");
            _vaccine = rs.getString("VACCINE");
            _lotNo = rs.getString("LOT_NO");
            _confirmation = rs.getString("CONFIRMATION");
            _aMeasles = rs.getString("A_MEASLES");
            _aMeaslesAge = rs.getString("A_MEASLES_AGE");
            _aConfirmation = rs.getString("A_CONFIRMATION");
            _antibody = rs.getString("ANTIBODY");
            _antibodyYear = rs.getString("ANTIBODY_YEAR");
            _antibodyMonth = rs.getString("ANTIBODY_MONTH");
            _antibodyPositive = rs.getString("ANTIBODY_POSITIVE");
        }

        private String getMeasles() {
            return (null == _measles) ? "" : _measles;
        }

        private String getAMeasles() {
            return (null == _aMeasles) ? "" : _aMeasles;
        }

        private String getAntibody() {
            return (null == _antibody) ? "" : _antibody;
        }

        private String getAntibodyPositive() {
            if ("1".equals(_antibodyPositive)) return "抗体ある";
            if ("2".equals(_antibodyPositive)) return "抗体ない";
            return "";
        }
    }

    private List createHealthQuestion(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlHealthQuestion();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final HealthQuestion healthQuestion = new HealthQuestion(rs.getString("QUESTIONCD"),
                                                                         rs.getString("CONTENTS"),
                                                                         rs.getString("SORT"));
                rtn.add(healthQuestion);
            }
        } catch (final Exception ex) {
            log.error("健康調査のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private String sqlHealthQuestion() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     QUESTIONCD, ");
        stb.append("     CONTENTS, ");
        stb.append("     SORT ");
        stb.append(" FROM ");
        stb.append("     HEALTH_QUESTION_MST ");
        stb.append(" ORDER BY ");
        stb.append("     smallint(SORT), ");
        stb.append("     smallint(QUESTIONCD) ");
        return stb.toString();
    }

    private class HealthQuestion {
        private final String _questioncd;

        private final String _contents;
        private final String _sort;

        public HealthQuestion(
                final String questioncd,
                final String contents,
                final String sort
        ) {
            _questioncd = questioncd;
            _contents = contents;
            _sort = sort;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77220 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _ctrlDate;
        private final String _nendo;
        private final String _grade;
        private final String _kubun;
        private final String[] _classSelected;
        private final Map _nameMstMap;
        private final String _gradeCd;
        private final String _schoolKind;
        private final String _nameSpare;
        private final String _use_prg_schoolkind;
        private final String _schKind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_year));
            _nendo = gengou + "年度";
            _kubun = request.getParameter("KUBUN");
            _grade = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _nameMstMap = createNameMstMap(db2);
            String[] rtn = getGradeCd(db2);
            _gradeCd = rtn[0];
            _schoolKind = rtn[1];
            _nameSpare = getNameSpare(db2);
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _schKind = "1".equals(_use_prg_schoolkind) ? getSchKind(db2) : _schoolKind;
        }

        private String getSchKind(final DB2UDB db2) throws SQLException {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAME1 AS VALUE, ");
            stb.append("     ABBV1 AS LABEL ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");
            stb.append("     AND NAMECD1 = 'A023' ");
            stb.append(" ORDER BY NAME1 ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtnStr = "";
            String sep = "";
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                	if (!"".equals(StringUtils.defaultString(rs.getString("VALUE"), ""))) {
                	    rtnStr += sep + "'" + rs.getString("VALUE") + "'";  //SQLのIN条件で使えるよう加工しているが、実際SQLでは未使用。
                	    sep = ",";
                	}
                }
            } catch (final Exception ex) {
                log.error("名称マスタのロードでエラー:" + stb.toString(), ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnStr;
        }
        private Map createNameMstMap(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            final String sql = sqlNameMst();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namecd = rs.getString("NAMECD1") + "-" + rs.getString("NAMECD2");
                    final String name = rs.getString("NAME1");

                    rtn.put(namecd, name);
                }
            } catch (final Exception ex) {
                log.error("名称マスタのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlNameMst() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD1, ");
            stb.append("     NAMECD2, ");
            stb.append("     NAME1, ");
            stb.append("     NAMESPARE1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' AND ");
            stb.append("     NAMECD1 in ('F230', ");
            stb.append("                 'F231', ");
            stb.append("                 'F232', ");
            stb.append("                 'F233', ");
            stb.append("                 'F234', ");
            stb.append("                 'F235', ");
            stb.append("                 'F240', ");
            stb.append("                 'H201') ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD1, ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }

        private String[] getGradeCd(final DB2UDB db2) throws SQLException {
            String[] rtn = new String[2];
            final String sql = sqlSchregRegdGdat();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn[0] = rs.getString("GRADE_CD");
                    rtn[1] = rs.getString("SCHOOL_KIND");
                }
            } catch (final Exception ex) {
                log.error("名称マスタのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String sqlSchregRegdGdat() {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE_CD, ");
            stb.append("     SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' AND ");
            stb.append("     GRADE = '" + _grade + "' ");
            return stb.toString();
        }

        private boolean isGrade1() {
            return "01".equals(_grade) || NumberUtils.isDigits(_gradeCd) && 1 == Integer.parseInt(_gradeCd);
        }

        private String getNameSpare(final DB2UDB db2) throws SQLException {
            String rtn = null;
            final String sql = " SELECT NAMESPARE2 FROM V_NAME_MST WHERE YEAR='" + _year + "' AND NAMECD1='Z010' AND NAMECD2='00'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("NAMESPARE2");
                }
            } catch (final Exception ex) {
                log.error("名称マスタの学年一貫区分のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getTitle() {
            String title = _nendo + "　保健調査票　（";

            if ("P".equals(_schoolKind)) {
            	if (isGrade1()) {
                    title += "小１用";
            	} else {
                    title += "小２～小６";
            	}
            } else if ("J".equals(_schoolKind)) {
                if (isChuKouIkkan()) {
                    if ("01".equals(_grade)) {
                        title += "中１用";
                    } else {
                        title += "中２～高３";
                    }
                } else {
                    if (isGrade1()) {
                        title += "中１用";
                    } else {
                        title += "中２～中３";
                    }
                }
            } else if ("H".equals(_schoolKind)) {
                if (isChuKouIkkan()) {
                    title += "中２～高３";
                } else {
                    if (isGrade1()) {
                        title += "高１用";
                    } else {
                        title += "高２～高３";
                    }
                }
            }
            title += "）";

            return  title;
        }

        /**
         * @return getTitle() の返り値にある　中１「用」　や　高１「用」　の　「用」　を削除した文言を返す。
         */
        private String getTitle2() {
            String title2 = _nendo + "　保健調査票　（";

            if ("P".equals(_schoolKind)) {
            	title2 += "小１";
            } else if ("J".equals(_schoolKind)) {
            	title2 += "中１";
            } else if ("H".equals(_schoolKind)) {
            	if (isChuKouIkkan()) {
                	title2 += "中１";
            	} else {
                	title2 += "高１";
            	}
            }
            title2 += "）";

            return  title2;
        }

        /**
         * 中高一貫かの判定を行う。
         * @return 中高一貫ならtrue
         */
        private boolean isChuKouIkkan() {
        	return ( (_schKind.indexOf('H') >= 0 && _schKind.indexOf('J') >= 0) && ("1".equals(_nameSpare) || "2".equals(_nameSpare)) ) ? true : false;
        }
    }

    private void putGengou1(final DB2UDB db2, final Vrw32alp svf, final String field) {
        //元号(記入項目用)
        if (!StringUtils.isEmpty(_param._ctrlDate)) {
            final String setDate = _param._ctrlDate.replace('/', '-');
            final String[] gengouArray = KNJ_EditDate.tate_format4(db2, setDate);
            svf.VrsOut(field, gengouArray[0]);
        }
    }

}

// eof
