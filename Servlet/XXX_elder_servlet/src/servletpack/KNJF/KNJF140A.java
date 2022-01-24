// kanji=漢字
/*
 * $Id: 9480a72955fccc1f3b337d894cb9d77c2a4a236e $
 *
 * 作成日: 2011/03/08 16:58:43 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.util.ArrayList;
import java.util.List;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 9480a72955fccc1f3b337d894cb9d77c2a4a236e $
 */
public class KNJF140A extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJF140A.class");

    private boolean _hasData;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void xls_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        _param = createParam(_db2, request);

        //テンプレートの読込&初期化
        setIniTmpBook(_param._templatePath);

        //ヘッダデータ取得
        _headList = getHeadData();

        //出力データ取得
        _dataList = getXlsDataList();

        outPutXls(response, _param._header);
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        if ("1".equals(_param._csvCd)) {
            retList.add("※CSVCD");
            retList.add("※年度");
            retList.add("※健診実施日");
            retList.add("※学籍番号");
            retList.add("身長");
            retList.add("体重");
            retList.add("座高");
            retList.add("胸囲");
            retList.add("視力右裸眼(数字)");
            retList.add("視力右裸眼(文字)");
            retList.add("視力左裸眼(数字)");
            retList.add("視力左裸眼(文字)");
            retList.add("視力右矯正(数字)");
            retList.add("視力右矯正(文字)");
            retList.add("視力左矯正(数字)");
            retList.add("視力左矯正(文字)");
            retList.add("聴力右状態コード");
            retList.add("聴力右DB");
            retList.add("聴力左状態コード");
            retList.add("聴力左DB");
            retList.add("一次尿蛋白コード");
            retList.add("一次尿糖コード");
            retList.add("一次尿潜血コード");
            retList.add("二次尿蛋白コード");
            retList.add("二次尿糖コード");
            retList.add("二次尿潜血コード");
            retList.add("尿その他の検査");
            retList.add("尿指導区分コード");
            retList.add("栄養状態コード");
            retList.add("脊柱胸郭コード");
            retList.add("脊柱胸郭所見");
            retList.add("目疾病及異常コード");
            retList.add("眼科所見");
            retList.add("耳鼻咽頭疾患コード");
            retList.add("耳鼻咽頭疾患所見");
            retList.add("皮膚疾患コード");
            retList.add("心臓臨床医学的検査コード");
            retList.add("心臓臨床医学的検査");
            retList.add("心臓疾病及異常コード");
            retList.add("心臓疾病及異常所見");
            retList.add("心臓管理区分");
            retList.add("結核ツ反実施日");
            retList.add("結核ツ反応");
            retList.add("結核ツ反判定コード");
            retList.add("結核BCG検査日");
            retList.add("結核撮影日付");
            retList.add("結核フィルム番号");
            retList.add("結核所見コード");
            retList.add("結核その他検査コード");
            retList.add("結核病名コード");
            retList.add("結核指導区分コード");
            retList.add("結核所見(X線)");
            retList.add("貧血所見");
            retList.add("貧血ヘモグロビン値");
            retList.add("寄生虫卵");
            retList.add("その他疾病及異常コード");
            retList.add("その他疾病指導区分コード");
            retList.add("学校医コード");
            retList.add("学校医所見");
            retList.add("学校医日付");
            retList.add("事後処置コード");
            retList.add("備考");
            retList.add("指導区分");
            retList.add("運動部活動");
            retList.add("既往症1");
            retList.add("既往症2");
            retList.add("既往症3");
            retList.add("診断名");
        } else {
            retList.add("※CSVCD");
            retList.add("※年度");
            retList.add("※健診実施日");
            retList.add("※学籍番号");
            if ("2".equals(_param._printKenkouSindanIppan)) {
            	retList.add("歯列顎関節コード");
            	retList.add("咬合顎関節コード");
            } else {
            	retList.add("歯列咬合顎関節コード");
            }
            retList.add("顎間接コード");
            retList.add("歯垢状態コード");
            retList.add("歯肉状態コード");
            retList.add("歯石沈着");
            retList.add("矯正の有無");
            retList.add("乳歯 上 右E");
            retList.add("乳歯 上 右D");
            retList.add("乳歯 上 右C");
            retList.add("乳歯 上 右B");
            retList.add("乳歯 上 右A");
            retList.add("乳歯 上 左A");
            retList.add("乳歯 上 左B");
            retList.add("乳歯 上 左C");
            retList.add("乳歯 上 左D");
            retList.add("乳歯 上 左E");
            retList.add("乳歯 下 右E");
            retList.add("乳歯 下 右D");
            retList.add("乳歯 下 右C");
            retList.add("乳歯 下 右B");
            retList.add("乳歯 下 右A");
            retList.add("乳歯 下 左A");
            retList.add("乳歯 下 左B");
            retList.add("乳歯 下 左C");
            retList.add("乳歯 下 左D");
            retList.add("乳歯 下 左E");
            retList.add("乳歯現在数");
            retList.add("乳歯未処置数");
            retList.add("乳歯処置数");
            retList.add("要注意乳歯数");
            retList.add("永久歯 上 右8");
            retList.add("永久歯 上 右7");
            retList.add("永久歯 上 右6");
            retList.add("永久歯 上 右5");
            retList.add("永久歯 上 右4");
            retList.add("永久歯 上 右3");
            retList.add("永久歯 上 右2");
            retList.add("永久歯 上 右1");
            retList.add("永久歯 上 左1");
            retList.add("永久歯 上 左2");
            retList.add("永久歯 上 左3");
            retList.add("永久歯 上 左4");
            retList.add("永久歯 上 左5");
            retList.add("永久歯 上 左6");
            retList.add("永久歯 上 左7");
            retList.add("永久歯 上 左8");
            retList.add("永久歯 下 右8");
            retList.add("永久歯 下 右7");
            retList.add("永久歯 下 右6");
            retList.add("永久歯 下 右5");
            retList.add("永久歯 下 右4");
            retList.add("永久歯 下 右3");
            retList.add("永久歯 下 右2");
            retList.add("永久歯 下 右1");
            retList.add("永久歯 下 左1");
            retList.add("永久歯 下 左2");
            retList.add("永久歯 下 左3");
            retList.add("永久歯 下 左4");
            retList.add("永久歯 下 左5");
            retList.add("永久歯 下 左6");
            retList.add("永久歯 下 左7");
            retList.add("永久歯 下 左8");
            retList.add("永久歯数");
            retList.add("永久歯未処置数");
            retList.add("永久歯処置数");
            retList.add("永久歯喪失数");
            retList.add("要観察歯数");
            retList.add("要精検歯数");
            if ("2".equals(_param._printKenkouSindanIppan)) {
                retList.add("その他疾病及び異常");
                retList.add("学校歯科医所見 CO");
                retList.add("学校歯科医所見 GO");
                retList.add("学校歯科医所見 G");
                retList.add("学校医処理");
                retList.add("学校歯科医所見日付");       	
            } else {
            	retList.add("その他疾病及異常コード");
                retList.add("その他疾病及び異常");
                retList.add("学校歯科医所見コード");
                retList.add("学校医処理");
                retList.add("学校歯科医所見日付");
                retList.add("学校歯科医事後処置コード");
            }
            retList.add("学校歯科医事後処置");
        }
        return retList;
    }

    protected String[] getCols() {
        final String[] cols1 = {"CSVCD",
                "YEAR",
                "DATE",
                "SCHREGNO",
                "HEIGHT",
                "WEIGHT",
                "SITHEIGHT",
                "CHEST",
                "R_BAREVISION",
                "R_BAREVISION_MARK",
                "L_BAREVISION",
                "L_BAREVISION_MARK",
                "R_VISION",
                "R_VISION_MARK",
                "L_VISION",
                "L_VISION_MARK",
                "R_EAR",
                "R_EAR_DB",
                "L_EAR",
                "L_EAR_DB",
                "ALBUMINURIA1CD",
                "URICSUGAR1CD",
                "URICBLEED1CD",
                "ALBUMINURIA2CD",
                "URICSUGAR2CD",
                "URICBLEED2CD",
                "URICOTHERTEST",
                "URI_ADVISECD",
                "NUTRITIONCD",
                "SPINERIBCD",
                "SPINERIBCD_REMARK",
                "EYEDISEASECD",
                "EYE_TEST_RESULT",
                "NOSEDISEASECD",
                "NOSEDISEASECD_REMARK",
                "SKINDISEASECD",
                "HEART_MEDEXAM",
                "HEART_MEDEXAM_REMARK",
                "HEARTDISEASECD",
                "HEARTDISEASECD_REMARK",
                "MANAGEMENT_DIV",
                "TB_DATE",
                "TB_REACT",
                "TB_RESULT",
                "TB_BCGDATE",
                "TB_FILMDATE",
                "TB_FILMNO",
                "TB_REMARKCD",
                "TB_OTHERTESTCD",
                "TB_NAMECD",
                "TB_ADVISECD",
                "TB_X_RAY",
                "ANEMIA_REMARK",
                "HEMOGLOBIN",
                "PARASITE",
                "OTHERDISEASECD",
                "OTHER_ADVISECD",
                "DOC_CD",
                "DOC_REMARK",
                "DOC_DATE",
                "TREATCD",
                "REMARK",
                "GUIDE_DIV",
                "JOINING_SPORTS_CLUB",
                "MEDICAL_HISTORY1",
                "MEDICAL_HISTORY2",
                "MEDICAL_HISTORY3",
                "DIAGNOSIS_NAME",};

        final List cols2 = new ArrayList();
        cols2.add("CSVCD");
        cols2.add("YEAR");
        cols2.add("TOOTH_DATE");
        cols2.add("SCHREGNO");
        cols2.add("JAWS_JOINTCD");
        if ("2".equals(_param._printKenkouSindanIppan)) {
            cols2.add("JAWS_JOINTCD3");
        }
        cols2.add("JAWS_JOINTCD2");
        cols2.add("PLAQUECD");
        cols2.add("GUMCD");
        cols2.add("CALCULUS");
        cols2.add("ORTHODONTICS");
        cols2.add("UP_R_BABY5");
        cols2.add("UP_R_BABY4");
        cols2.add("UP_R_BABY3");
        cols2.add("UP_R_BABY2");
        cols2.add("UP_R_BABY1");
        cols2.add("UP_L_BABY1");
        cols2.add("UP_L_BABY2");
        cols2.add("UP_L_BABY3");
        cols2.add("UP_L_BABY4");
        cols2.add("UP_L_BABY5");
        cols2.add("LW_R_BABY5");
        cols2.add("LW_R_BABY4");
        cols2.add("LW_R_BABY3");
        cols2.add("LW_R_BABY2");
        cols2.add("LW_R_BABY1");
        cols2.add("LW_L_BABY1");
        cols2.add("LW_L_BABY2");
        cols2.add("LW_L_BABY3");
        cols2.add("LW_L_BABY4");
        cols2.add("LW_L_BABY5");
        cols2.add("BABYTOOTH");
        cols2.add("REMAINBABYTOOTH");
        cols2.add("TREATEDBABYTOOTH");
        cols2.add("BRACK_BABYTOOTH");
        cols2.add("UP_R_ADULT8");
        cols2.add("UP_R_ADULT7");
        cols2.add("UP_R_ADULT6");
        cols2.add("UP_R_ADULT5");
        cols2.add("UP_R_ADULT4");
        cols2.add("UP_R_ADULT3");
        cols2.add("UP_R_ADULT2");
        cols2.add("UP_R_ADULT1");
        cols2.add("UP_L_ADULT1");
        cols2.add("UP_L_ADULT2");
        cols2.add("UP_L_ADULT3");
        cols2.add("UP_L_ADULT4");
        cols2.add("UP_L_ADULT5");
        cols2.add("UP_L_ADULT6");
        cols2.add("UP_L_ADULT7");
        cols2.add("UP_L_ADULT8");
        cols2.add("LW_R_ADULT8");
        cols2.add("LW_R_ADULT7");
        cols2.add("LW_R_ADULT6");
        cols2.add("LW_R_ADULT5");
        cols2.add("LW_R_ADULT4");
        cols2.add("LW_R_ADULT3");
        cols2.add("LW_R_ADULT2");
        cols2.add("LW_R_ADULT1");
        cols2.add("LW_L_ADULT1");
        cols2.add("LW_L_ADULT2");
        cols2.add("LW_L_ADULT3");
        cols2.add("LW_L_ADULT4");
        cols2.add("LW_L_ADULT5");
        cols2.add("LW_L_ADULT6");
        cols2.add("LW_L_ADULT7");
        cols2.add("LW_L_ADULT8");
        cols2.add("ADULTTOOTH");
        cols2.add("REMAINADULTTOOTH");
        cols2.add("TREATEDADULTTOOTH");
        cols2.add("LOSTADULTTOOTH");
        cols2.add("BRACK_ADULTTOOTH");
        cols2.add("CHECKADULTTOOTH");
        if ("2".equals(_param._printKenkouSindanIppan)) {
            cols2.add("OTHERDISEASE");
            cols2.add("DENTISTREMARK_CO");
            cols2.add("DENTISTREMARK_GO");
            cols2.add("DENTISTREMARK_G");
            cols2.add("DENTISTREMARK");
            cols2.add("DENTISTREMARKDATE");           
        } else {
            cols2.add("OTHERDISEASECD");
            cols2.add("OTHERDISEASE");
            cols2.add("DENTISTREMARKCD");
            cols2.add("DENTISTREMARK");
            cols2.add("DENTISTREMARKDATE");
            cols2.add("DENTISTTREATCD");
        }
        cols2.add("DENTISTTREAT");
        return "1".equals(_param._csvCd) ? cols1 : (String[]) cols2.toArray(new String[] {});
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();

        if ("1".equals(_param._csvCd)) { //一般
            stb.append(" SELECT ");
            stb.append("     '1' AS CSVCD, ");
            stb.append("     T2.YEAR, ");
            stb.append("     T2.DATE, ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     HEIGHT, ");
            stb.append("     WEIGHT, ");
            stb.append("     SITHEIGHT, ");
            stb.append("     CHEST, ");
            stb.append("     R_BAREVISION, ");
            stb.append("     R_BAREVISION_MARK, ");
            stb.append("     L_BAREVISION, ");
            stb.append("     L_BAREVISION_MARK, ");
            stb.append("     R_VISION, ");
            stb.append("     R_VISION_MARK, ");
            stb.append("     L_VISION, ");
            stb.append("     L_VISION_MARK, ");
            stb.append("     R_EAR, ");
            stb.append("     R_EAR_DB, ");
            stb.append("     L_EAR, ");
            stb.append("     L_EAR_DB, ");
            stb.append("     ALBUMINURIA1CD, ");
            stb.append("     URICSUGAR1CD, ");
            stb.append("     URICBLEED1CD, ");
            stb.append("     ALBUMINURIA2CD, ");
            stb.append("     URICSUGAR2CD, ");
            stb.append("     URICBLEED2CD, ");
            stb.append("     URICOTHERTEST, ");
            stb.append("     URI_ADVISECD, ");
            stb.append("     NUTRITIONCD, ");
            stb.append("     SPINERIBCD, ");
            stb.append("     SPINERIBCD_REMARK, ");
            stb.append("     EYEDISEASECD, ");
            stb.append("     EYE_TEST_RESULT, ");
            stb.append("     NOSEDISEASECD, ");
            stb.append("     NOSEDISEASECD_REMARK, ");
            stb.append("     SKINDISEASECD, ");
            stb.append("     HEART_MEDEXAM, ");
            stb.append("     HEART_MEDEXAM_REMARK, ");
            stb.append("     HEARTDISEASECD, ");
            stb.append("     HEARTDISEASECD_REMARK, ");
            stb.append("     MANAGEMENT_DIV, ");
            stb.append("     TB_DATE, ");
            stb.append("     TB_REACT, ");
            stb.append("     TB_RESULT, ");
            stb.append("     TB_BCGDATE, ");
            stb.append("     TB_FILMDATE, ");
            stb.append("     TB_FILMNO, ");
            stb.append("     TB_REMARKCD, ");
            stb.append("     TB_OTHERTESTCD, ");
            stb.append("     TB_NAMECD, ");
            stb.append("     TB_ADVISECD, ");
            stb.append("     TB_X_RAY, ");
            stb.append("     ANEMIA_REMARK, ");
            stb.append("     HEMOGLOBIN, ");
            stb.append("     PARASITE, ");
            stb.append("     OTHERDISEASECD, ");
            stb.append("     OTHER_ADVISECD, ");
            stb.append("     DOC_CD, ");
            stb.append("     DOC_REMARK, ");
            stb.append("     DOC_DATE, ");
            stb.append("     TREATCD, ");
            stb.append("     REMARK, ");
            stb.append("     GUIDE_DIV, ");
            stb.append("     JOINING_SPORTS_CLUB, ");
            stb.append("     MEDICAL_HISTORY1, ");
            stb.append("     MEDICAL_HISTORY2, ");
            stb.append("     MEDICAL_HISTORY3, ");
            stb.append("     DIAGNOSIS_NAME, ");
            stb.append("     'DUMMY' AS DUMMY ");
            stb.append(" FROM MEDEXAM_DET_DAT T1, ");
        } else { //歯・口腔
            stb.append(" SELECT ");
            stb.append("     '2' AS CSVCD, ");
            stb.append("     T2.YEAR, ");
            stb.append("     T2.TOOTH_DATE, ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     JAWS_JOINTCD, ");
            if ("2".equals(_param._printKenkouSindanIppan)) {
            	stb.append("     JAWS_JOINTCD3, ");
            }
            stb.append("     JAWS_JOINTCD2, ");
            stb.append("     PLAQUECD, ");
            stb.append("     GUMCD, ");
            stb.append("     CALCULUS, ");
            stb.append("     ORTHODONTICS, ");
            stb.append("     UP_R_BABY5, ");
            stb.append("     UP_R_BABY4, ");
            stb.append("     UP_R_BABY3, ");
            stb.append("     UP_R_BABY2, ");
            stb.append("     UP_R_BABY1, ");
            stb.append("     UP_L_BABY1, ");
            stb.append("     UP_L_BABY2, ");
            stb.append("     UP_L_BABY3, ");
            stb.append("     UP_L_BABY4, ");
            stb.append("     UP_L_BABY5, ");
            stb.append("     LW_R_BABY5, ");
            stb.append("     LW_R_BABY4, ");
            stb.append("     LW_R_BABY3, ");
            stb.append("     LW_R_BABY2, ");
            stb.append("     LW_R_BABY1, ");
            stb.append("     LW_L_BABY1, ");
            stb.append("     LW_L_BABY2, ");
            stb.append("     LW_L_BABY3, ");
            stb.append("     LW_L_BABY4, ");
            stb.append("     LW_L_BABY5, ");
            stb.append("     BABYTOOTH, ");
            stb.append("     REMAINBABYTOOTH, ");
            stb.append("     TREATEDBABYTOOTH, ");
            stb.append("     BRACK_BABYTOOTH, ");
            stb.append("     UP_R_ADULT8, ");
            stb.append("     UP_R_ADULT7, ");
            stb.append("     UP_R_ADULT6, ");
            stb.append("     UP_R_ADULT5, ");
            stb.append("     UP_R_ADULT4, ");
            stb.append("     UP_R_ADULT3, ");
            stb.append("     UP_R_ADULT2, ");
            stb.append("     UP_R_ADULT1, ");
            stb.append("     UP_L_ADULT1, ");
            stb.append("     UP_L_ADULT2, ");
            stb.append("     UP_L_ADULT3, ");
            stb.append("     UP_L_ADULT4, ");
            stb.append("     UP_L_ADULT5, ");
            stb.append("     UP_L_ADULT6, ");
            stb.append("     UP_L_ADULT7, ");
            stb.append("     UP_L_ADULT8, ");
            stb.append("     LW_R_ADULT8, ");
            stb.append("     LW_R_ADULT7, ");
            stb.append("     LW_R_ADULT6, ");
            stb.append("     LW_R_ADULT5, ");
            stb.append("     LW_R_ADULT4, ");
            stb.append("     LW_R_ADULT3, ");
            stb.append("     LW_R_ADULT2, ");
            stb.append("     LW_R_ADULT1, ");
            stb.append("     LW_L_ADULT1, ");
            stb.append("     LW_L_ADULT2, ");
            stb.append("     LW_L_ADULT3, ");
            stb.append("     LW_L_ADULT4, ");
            stb.append("     LW_L_ADULT5, ");
            stb.append("     LW_L_ADULT6, ");
            stb.append("     LW_L_ADULT7, ");
            stb.append("     LW_L_ADULT8, ");
            stb.append("     ADULTTOOTH, ");
            stb.append("     REMAINADULTTOOTH, ");
            stb.append("     TREATEDADULTTOOTH, ");
            stb.append("     LOSTADULTTOOTH, ");
            stb.append("     BRACK_ADULTTOOTH, ");
            stb.append("     CHECKADULTTOOTH, ");
            if ("2".equals(_param._printKenkouSindanIppan)) {
	            stb.append("     OTHERDISEASE, ");
	            stb.append("     DENTISTREMARK_CO, ");
	            stb.append("     DENTISTREMARK_GO, ");
	            stb.append("     DENTISTREMARK_G, ");
	            stb.append("     DENTISTREMARK, ");
	            stb.append("     DENTISTREMARKDATE, ");
            } else {
	            stb.append("     OTHERDISEASECD, ");
	            stb.append("     OTHERDISEASE, ");
	            stb.append("     DENTISTREMARKCD, ");
	            stb.append("     DENTISTREMARK, ");
	            stb.append("     DENTISTREMARKDATE, ");
	            stb.append("     DENTISTTREATCD, ");
            }
            stb.append("     DENTISTTREAT, ");
            stb.append("     'DUMMY' AS DUMMY ");
            stb.append(" FROM MEDEXAM_TOOTH_DAT T1, ");
        }

        stb.append("     MEDEXAM_HDAT T2, ");
        stb.append("     SCHREG_REGD_DAT T3 ");
        stb.append(" WHERE T3.YEAR || T3.SEMESTER = '" + _param._yearSem + "' ");
        if (_param._gradeHrClass != null && !"".equals(_param._gradeHrClass)) {
            stb.append("AND T3.GRADE || T3.HR_CLASS = '" + _param._gradeHrClass + "' ");
        }
        stb.append("    AND T3.YEAR     = T2.YEAR ");
        stb.append("    AND T3.SCHREGNO = T2.SCHREGNO ");
        stb.append("    AND T1.YEAR     = T2.YEAR ");
        stb.append("    AND T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("ORDER BY T2.SCHREGNO");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _yearSem;
        private final String _gradeHrClass;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _csvCd;
        private final String _printKenkouSindanIppan;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _csvCd = request.getParameter("CSVCD");
            _printKenkouSindanIppan = request.getParameter("PRINTKENKOUSINDANIPPAN");
            
        }

    }
}

// eof
