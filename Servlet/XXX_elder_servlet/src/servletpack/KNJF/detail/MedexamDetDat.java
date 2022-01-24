// kanji=漢字
/*
 * $Id: d2cd59a177ab3e2c5788f14c0e65fe01bc400739 $
 *
 * 作成日: 2009/10/08 13:21:02 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF.detail;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: d2cd59a177ab3e2c5788f14c0e65fe01bc400739 $
 */
public class MedexamDetDat {

    private static final Log log = LogFactory.getLog("MedexamDetDat.class");
    protected final DB2UDB _db2;

    public String _height;
    public String _weight;
    public String _sitheight;
    public String _chest;
    public String _noPrintObesityIndex;
    public String _rVisionCantMeasure;
    public String _lVisionCantMeasure;
    public String _rBarevision;
    public String _rBarevisionMark;
    public String _lBarevision;
    public String _lBarevisionMark;
    public String _rVision;
    public String _rVisionMark;
    public String _lVision;
    public String _lVisionMark;
    public String _visionCantMeasure;
    /** F010 */
    public String _rEarCantMeasure;
    public String _rEar;
    public String _rEarDb;
    public String _rEarDb1000;
    public String _rEarDb4000;
    /** F010 */
    public String _lEarCantMeasure;
    public String _lEar;
    public String _lEarDb;
    public String _lEarDb1000;
    public String _lEarDb4000;
    /** F020 */
    public String _albuminuria1cd;
    /** F019 */
    public String _uricsugar1cd;
    /** F018 */
    public String _uricbleed1cd;
    /** F020 */
    public String _albuminuria2cd;
    /** F019 */
    public String _uricsugar2cd;
    /** F018 */
    public String _uricbleed2cd;
    public String _uricothertest;
    public String _uricothertestCd;
    /** F021 */
    public String _uriAdvisecd;
    /** F030 */
    public String _nutritioncd;
    public String _nutritioncdRemark;
    /** F040 */
    public String _spineribcd;
    public String _spineribcdRemark;
    /** F050 */
    public String _eyediseasecd;
    public String _eyediseasecd2;
    public String _eyediseasecd3;
    public String _eyediseasecd4;
    public String _eyediseasecd5;
    public String _eyeTestResult;
    public String _eyeTestResult2;
    public String _eyeTestResult3;
    /** F060 */
    public String _nosediseasecd;
    /** F061 */
    public String _nosediseasecd5;
    /** F062 */
    public String _nosediseasecd6;
    /** F063 */
    public String _nosediseasecd7;
    public String _nosediseasecdRemark;
    public String _nosediseasecdRemark1;
    public String _nosediseasecdRemark2;
    public String _nosediseasecdRemark3;
    /** F070 */
    public String _skindiseasecd;
    public String _skindiseasecdRemark;
    /** F080 */
    public String _heartMedexam;
    public String _heartMedexamRemark;
    public String _heartGraphNo;
    /** F090 */
    public String _heartdiseasecd;
    public String _heartdiseasecdRemark;
    /** F091 */
    public String _managementDiv;
    public String _managementDivName;
    public String _managementRemark;
    /** F130 */
    public String _tbDate;
    public String _tbReact;
    public String _tbResult;
    public String _tbBcgdate;
    public String _tbFilmdate;
    public String _tbFilmno;
    /** F100 */
    public String _tbRemarkcd;
    /** F110 */
    public String _tbOthertestcd;
    public String _tbOthertestRemark1;
    /** F120 */
    public String _tbNamecd;
    public String _tbNameRemark1;
    /** F130 */
    public String _tbAdvisecd;
    public String _tbAdviseRemark1;
    public String _anemiaRemark;
    public String _hemoglobin;
    public String _parasite;
    /** F140 */
    public String _otherdiseasecd;
    /** F145 */
    public String _otherAdvisecd;
    public String _otherRemark;
    public String _otherRemark2;
    public String _otherRemark3;
    /** F144 */
    public String _docCd;
    public String _docRemark;
    public String _docDate;
    /** F150 */
    public String _treatcd;
    public String _treatRemark1;
    public String _treatRemark2;
    public String _treatRemark3;
    public String _remark;
    public String _nutritionResult;
    public String _eyediseaseResult;
    public String _skindiseaseResult;
    public String _spineribResult;
    public String _nosediseaseResult;
    public String _otherdiseaseResult;
    public String _heartdiseaseResult;
    public String _bmi;
    public String _date;
    public String _detYear;
    public String _detMonth;
    /** F151 */
    public String _treatcd2;
    /** F141 */
    public String _guideDiv;
    /** F142 */
    public String _joiningSportsClub;
    public String _tbXRay;
    /** F143 */
    public String _medicalHistory1;
    /** F143 */
    public String _medicalHistory2;
    /** F143 */
    public String _medicalHistory3;
    public String _diagnosisName;

    /**
     * コンストラクタ。
     */
    public MedexamDetDat(final DB2UDB db2, final String year, final String schregNo, final String ippan) throws SQLException {
        _db2 = db2;
        final String MdSql = getMdSql(year, schregNo, ippan);
        PreparedStatement psMd = null;
        ResultSet rsMd = null;
        Map findResultStr = new LinkedMap();
        try {
            psMd = _db2.prepareStatement(MdSql);
            rsMd = psMd.executeQuery();
            findDataExist(rsMd, "R_EAR_DB_1000", findResultStr);
            findDataExist(rsMd, "L_EAR_DB_1000", findResultStr);
            findDataExist(rsMd, "HEART_GRAPH_NO", findResultStr);
            while (rsMd.next()) {
                _height = rsMd.getString("HEIGHT");
                _weight = rsMd.getString("WEIGHT");
                _sitheight = rsMd.getString("SITHEIGHT");
                _chest = rsMd.getString("CHEST");
                _noPrintObesityIndex = rsMd.getString("NO_PRINT_OBESITY_INDEX");
                _rVisionCantMeasure = rsMd.getString("R_VISION_CANTMEASURE");
                _lVisionCantMeasure = rsMd.getString("L_VISION_CANTMEASURE");
                _rBarevision = rsMd.getString("R_BAREVISION");
                _rBarevisionMark = rsMd.getString("R_BAREVISION_MARK");
                _lBarevision = rsMd.getString("L_BAREVISION");
                _lBarevisionMark = rsMd.getString("L_BAREVISION_MARK");
                _rVision = rsMd.getString("R_VISION");
                _rVisionMark = rsMd.getString("R_VISION_MARK");
                _lVision = rsMd.getString("L_VISION");
                _lVisionMark = rsMd.getString("L_VISION_MARK");
                _visionCantMeasure = rsMd.getString("VISION_CANTMEASURE");
                _rEarCantMeasure = rsMd.getString("R_EAR_CANTMEASURE");
                _rEar = rsMd.getString("R_EAR");
                _rEarDb = rsMd.getString("R_EAR_DB");
                if (findResultStr.containsKey("R_EAR_DB_1000") && ((Boolean)findResultStr.get("R_EAR_DB_1000")).booleanValue()) {
                    _rEarDb1000 = rsMd.getString("R_EAR_DB_1000");
                } else {
                	_rEarDb1000 = null;
                }
                _rEarDb4000 = rsMd.getString("R_EAR_DB_4000");
                _lEarCantMeasure = rsMd.getString("L_EAR_CANTMEASURE");
                _lEar = rsMd.getString("L_EAR");
                _lEarDb = rsMd.getString("L_EAR_DB");
                if (findResultStr.containsKey("L_EAR_DB_1000") && ((Boolean)findResultStr.get("L_EAR_DB_1000")).booleanValue()) {
                    _lEarDb1000 = rsMd.getString("L_EAR_DB_1000");
                } else {
                	_lEarDb1000 = null;
                }
                _lEarDb4000 = rsMd.getString("L_EAR_DB_4000");
                _albuminuria1cd = rsMd.getString("ALBUMINURIA1CD");
                _uricsugar1cd = rsMd.getString("URICSUGAR1CD");
                _uricbleed1cd = rsMd.getString("URICBLEED1CD");
                _albuminuria2cd = rsMd.getString("ALBUMINURIA2CD");
                _uricsugar2cd = rsMd.getString("URICSUGAR2CD");
                _uricbleed2cd = rsMd.getString("URICBLEED2CD");
                _uricothertest = rsMd.getString("URICOTHERTEST");
                _uricothertestCd = rsMd.getString("URICOTHERTESTCD");
                _uriAdvisecd = rsMd.getString("URI_ADVISECD");
                _nutritioncd = rsMd.getString("NUTRITIONCD");
                _nutritioncdRemark = rsMd.getString("NUTRITIONCD_REMARK");
                _spineribcd = rsMd.getString("SPINERIBCD");
                _spineribcdRemark = rsMd.getString("SPINERIBCD_REMARK");
                _eyediseasecd = rsMd.getString("EYEDISEASECD");
                _eyediseasecd2 = rsMd.getString("EYEDISEASECD2");
                _eyediseasecd3 = rsMd.getString("EYEDISEASECD3");
                _eyediseasecd4 = rsMd.getString("EYEDISEASECD4");
                _eyediseasecd5 = rsMd.getString("EYEDISEASECD5");
                _eyeTestResult = rsMd.getString("EYE_TEST_RESULT");
                _eyeTestResult2 = rsMd.getString("EYE_TEST_RESULT2");
                _eyeTestResult3 = rsMd.getString("EYE_TEST_RESULT3");
                _nosediseasecd = rsMd.getString("NOSEDISEASECD");
                _nosediseasecd5 = rsMd.getString("NOSEDISEASECD5");
                _nosediseasecd6 = rsMd.getString("NOSEDISEASECD6");
                _nosediseasecd7 = rsMd.getString("NOSEDISEASECD7");
                _nosediseasecdRemark = rsMd.getString("NOSEDISEASECD_REMARK");
                _nosediseasecdRemark1 = rsMd.getString("NOSEDISEASECD_REMARK1");
                _nosediseasecdRemark2 = rsMd.getString("NOSEDISEASECD_REMARK2");
                _nosediseasecdRemark3 = rsMd.getString("NOSEDISEASECD_REMARK3");
                _skindiseasecd = rsMd.getString("SKINDISEASECD");
                _skindiseasecdRemark = rsMd.getString("SKINDISEASECD_REMARK");
                _heartMedexam = rsMd.getString("HEART_MEDEXAM");
                _heartMedexamRemark = rsMd.getString("HEART_MEDEXAM_REMARK");
                if (findResultStr.containsKey("HEART_GRAPH_NO") && ((Boolean)findResultStr.get("HEART_GRAPH_NO")).booleanValue()) {
                    _heartGraphNo = rsMd.getString("HEART_GRAPH_NO");
                } else {
                    _heartGraphNo = null;
                }
                _heartdiseasecd = rsMd.getString("HEARTDISEASECD");
                _heartdiseasecdRemark = rsMd.getString("HEARTDISEASECD_REMARK");
                _managementDiv = rsMd.getString("MANAGEMENT_DIV");
                _managementDivName = rsMd.getString("MANAGEMENT_DIV_NAME");
                _managementRemark =  rsMd.getString("MANAGEMENT_REMARK");
                _tbDate = rsMd.getString("TB_DATE");
                _tbReact = rsMd.getString("TB_REACT");
                _tbResult = rsMd.getString("TB_RESULT");
                _tbBcgdate = rsMd.getString("TB_BCGDATE");
                _tbFilmdate = rsMd.getString("TB_FILMDATE");
                _tbFilmno = rsMd.getString("TB_FILMNO");
                _tbRemarkcd = rsMd.getString("TB_REMARKCD");
                _tbOthertestcd = rsMd.getString("TB_OTHERTESTCD");
                _tbOthertestRemark1 = rsMd.getString("TB_OTHERTEST_REMARK1");
                _tbNamecd = rsMd.getString("TB_NAMECD");
                _tbNameRemark1 = rsMd.getString("TB_NAME_REMARK1");
                _tbAdvisecd = rsMd.getString("TB_ADVISECD");
                _tbAdviseRemark1 = rsMd.getString("TB_ADVISE_REMARK1");
                _tbXRay = rsMd.getString("TB_X_RAY");
                _anemiaRemark = rsMd.getString("ANEMIA_REMARK");
                _hemoglobin = rsMd.getString("HEMOGLOBIN");
                _parasite = rsMd.getString("PARASITE");
                _otherdiseasecd = rsMd.getString("OTHERDISEASECD");
                _otherAdvisecd = rsMd.getString("OTHER_ADVISECD");
                _otherRemark = rsMd.getString("OTHER_REMARK");
                _otherRemark2 = rsMd.getString("OTHER_REMARK2");
                _otherRemark3 = rsMd.getString("OTHER_REMARK3");
                _docCd = rsMd.getString("DOC_CD");
                _docRemark = rsMd.getString("DOC_REMARK");
                _docDate = rsMd.getString("DOC_DATE");
                _treatcd = rsMd.getString("TREATCD");
                _treatcd2 = rsMd.getString("TREATCD2");
                _treatRemark1 = rsMd.getString("TREAT_REMARK1");
                _treatRemark2 = rsMd.getString("TREAT_REMARK2");
                _treatRemark3 = rsMd.getString("TREAT_REMARK3");
                _remark = rsMd.getString("REMARK");
                _nutritionResult = rsMd.getString("NUTRITION_RESULT");
                _eyediseaseResult = rsMd.getString("EYEDISEASE_RESULT");
                _skindiseaseResult = rsMd.getString("SKINDISEASE_RESULT");
                _spineribResult = rsMd.getString("SPINERIB_RESULT");
                _nosediseaseResult = rsMd.getString("NOSEDISEASE_RESULT");
                _otherdiseaseResult = rsMd.getString("OTHERDISEASE_RESULT");
                _heartdiseaseResult = rsMd.getString("HEARTDISEASE_RESULT");
                _bmi = rsMd.getString("BMI") == null ? "" : rsMd.getBigDecimal("BMI").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                _date = rsMd.getString("DET_DATE");
                _detYear = rsMd.getString("DET_YEAR");
                _detMonth = rsMd.getString("DET_MONTH");
                _guideDiv = rsMd.getString("GUIDE_DIV");
                _joiningSportsClub = rsMd.getString("JOINING_SPORTS_CLUB");
                _medicalHistory1 = rsMd.getString("MEDICAL_HISTORY1");
                _medicalHistory2 = rsMd.getString("MEDICAL_HISTORY2");
                _medicalHistory3 = rsMd.getString("MEDICAL_HISTORY3");
                _diagnosisName = rsMd.getString("DIAGNOSIS_NAME");
            }
        } finally {
            DbUtils.closeQuietly(null, psMd, rsMd);
            _db2.commit();
        }
    }

    private boolean findDataExist(ResultSet rsMd, final String findStr, final Map resultMap) throws SQLException {
        boolean bRet = false;
        ResultSetMetaData rsmdWk= rsMd.getMetaData();
        for (int cnt = 1;cnt <= rsmdWk.getColumnCount();cnt++) {
            final String colStr = rsmdWk.getColumnName(cnt);
            if (findStr.equals(colStr)) {
                bRet = true;
            }
        }
    	resultMap.put(findStr, new Boolean(bRet));
        return bRet;
    }

    private String getMdSql(final String year, final String schregNo, final String ippan) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     CASE WHEN VALUE(T1.HEIGHT,0) > 0 THEN ROUND(T1.WEIGHT/T1.HEIGHT/T1.HEIGHT*10000,1) END AS BMI, ");
        stb.append("     L2.NAME1 AS MANAGEMENT_DIV_NAME, ");
        stb.append("     L1.DATE AS DET_DATE, ");
        stb.append("     YEAR(L1.DATE) AS DET_YEAR, ");
        stb.append("     MONTH(L1.DATE) AS DET_MONTH ");
        stb.append(" FROM ");
        stb.append("     V_MEDEXAM_DET_DAT T1 ");
        stb.append("     LEFT JOIN MEDEXAM_HDAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'F091' ");
        stb.append("          AND L2.NAMECD2 = T1.MANAGEMENT_DIV ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregNo + "' ");
        return stb.toString();
    }
}

// eof
