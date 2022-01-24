// kanji=漢字
/*
 * $Id: 1188fed224b81158746a3f481bd904d27f610051 $
 *
 * 作成日: 2009/10/07 15:55:24 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJF.KNJF030C.HexamPhysicalAvgDat;
import servletpack.KNJF.KNJF030C.Param;
import servletpack.KNJF.KNJF030C.Student;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 1188fed224b81158746a3f481bd904d27f610051 $
 */
public class KNJF030C_TeikiKensinItiran extends KNJF030CAbstract {

    public KNJF030C_TeikiKensinItiran(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJF030C_TeikiKensinItiran.class");

    /**
     * {@inheritDoc}
     */
    protected boolean printMain(final List printStudents) throws SQLException {
        final Map physAvgMap;
        if (_param._isKumamoto) {
            physAvgMap = new TreeMap();
        } else {
            physAvgMap = HexamPhysicalAvgDat.getHexamPhysicalAvgMap(_db2, _param);
        }

        boolean hasData = false;
        String befHr = "";
        _svf.VrSetForm(!_param._isKumamoto ? F_TEIKI_KENSIN_ITIRAN : F_TEIKI_KENSIN_ITIRAN_KUMA, 4);
        for (final Iterator itStudent = printStudents.iterator(); itStudent.hasNext();) {
            final Student student = (Student) itStudent.next();
            final String gradeHr = student._grade + student._hrClass;
            if (!"".equals(befHr) && !befHr.equals(gradeHr)) {
                _svf.VrSetForm(!_param._isKumamoto ? F_TEIKI_KENSIN_ITIRAN : F_TEIKI_KENSIN_ITIRAN_KUMA, 4);
            }
            befHr = gradeHr;
            _svf.VrsOut("NENDO", _param.changePrintYear(_param._year));
            _svf.VrsOut("DATE", _param.changePrintDate(_param._ctrlDate));
            _svf.VrsOut("TIME", _param._time);
            _svf.VrsOut("SCHOOLNAME1", _param.schoolInfoVal(student._schoolKind, Param.SCHOOL_NAME1));
            _svf.VrsOut("STAFFNAME_SHOW", _param._staffName);
            _svf.VrsOut("HR_NAME", student._hrName);
            _svf.VrsOut("ATTENDNO", String.valueOf(Integer.parseInt(student._attendno)));
            _svf.VrsOut("ANNUAL", String.valueOf(Integer.parseInt(student._annual)));
            _svf.VrsOut("MAJOR", student._majorname);
            _svf.VrsOut("SCHREGNO", student._schregno);
            final String fieldNo = (17 < student._name.length()) ? "3" : (10 < student._name.length()) ? "2" : "";
            _svf.VrsOut("NAME" + fieldNo, student._name);
            _svf.VrsOut("BIRTHDAY", _param.changePrintDate(student._birthDay));
            setClubName(student);

            if (null != student._medexamDetDat) {
                final String detDate = _param.changePrintYear(student._medexamDetDat._detYear) + student._medexamDetDat._detMonth + "月";
                _svf.VrsOut("DET_DATE", detDate);
                _svf.VrsOut("HEIGHT", student._medexamDetDat._height);
                _svf.VrsOut("WEIGHT", student._medexamDetDat._weight);
                _svf.VrsOut("SITHEIGHT", student._medexamDetDat._sitheight);
                _svf.VrsOut("R_BAREVISION_MARK", student._medexamDetDat._rBarevisionMark);
                _svf.VrsOut("R_VISION_MARK", student._medexamDetDat._rVisionMark);
                _svf.VrsOut("L_BAREVISION_MARK", student._medexamDetDat._lBarevisionMark);
                _svf.VrsOut("L_VISION_MARK", student._medexamDetDat._lVisionMark);
                if (!_param._isKumamoto) {
                    _svf.VrsOut("BMI", calcHimando(student, physAvgMap, _param));
                } else {
                    _svf.VrsOut("BMI", student._medexamDetDat._bmi);
                }
                _svf.VrsOut("ANEMIA_REMARK", student._medexamDetDat._anemiaRemark);
                _svf.VrsOut("NEXT_CONSULTATION", "");
                _svf.VrsOut("DIAGNOSIS_NAME", student._medexamDetDat._diagnosisName);
                _svf.VrsOut("TB_FILMNO", student._medexamDetDat._tbFilmno);
                _svf.VrsOut("EYE_TEST_RESULT", student._medexamDetDat._eyeTestResult);

                setName("R_EAR", "F010", student._medexamDetDat._rEar, "NAME1", 0, "", "");
                setName("L_EAR", "F010", student._medexamDetDat._lEar, "NAME1", 0, "", "");
                setName("ALBUMINURIA1", "F020", student._medexamDetDat._albuminuria1cd, "NAME1", 0, "", "");
                setName("URICSUGAR1", "F019", student._medexamDetDat._uricsugar1cd, "NAME1", 0, "", "");
                setName("URICBLEED1", "F018", student._medexamDetDat._uricbleed1cd, "NAME1", 0, "", "");
                setName("HEART_MEDEXAM", "F080", student._medexamDetDat._heartMedexam, "NAME1", 0, "", "");
                setName("TB_NAME", "F120", student._medexamDetDat._tbNamecd, "NAME1", 0, "", "");
                setName("OTHERDISEASE", "F140", student._medexamDetDat._otherdiseasecd, "NAME1", 0, "", "");
                setName("DISEASE", "F050", student._medexamDetDat._eyediseasecd, "NAME1", 0, "", "");
                setName("MEDICAL_HISTORY1", "F143", student._medexamDetDat._medicalHistory1, "NAME1", 0, "", "");
                setName("MEDICAL_HISTORY2", "F143", student._medexamDetDat._medicalHistory2, "NAME1", 0, "", "");
                setName("MEDICAL_HISTORY3", "F143", student._medexamDetDat._medicalHistory3, "NAME1", 0, "", "");
                setName("GUIDE_DIV", "F141", student._medexamDetDat._guideDiv, "ABBV1", 0, "", "");
                setName("JOINING_SPORTS_CLUB", "F142", student._medexamDetDat._joiningSportsClub, "NAME1", 0, "", "");
                setName("MANAGEMENT", "F091", student._medexamDetDat._managementDiv, "NAME1", 0, "", "");
            }
            if (null != student._medexamToothDat) {
                final int reBaby = null != student._medexamToothDat._remainbabytooth ? Integer.parseInt(student._medexamToothDat._remainbabytooth) : 0;
                final int reAdult = null != student._medexamToothDat._remainadulttooth ? Integer.parseInt(student._medexamToothDat._remainadulttooth) : 0;
                final int treBaby = null != student._medexamToothDat._treatedbabytooth ? Integer.parseInt(student._medexamToothDat._treatedbabytooth) : 0;
                final int treAdult = null != student._medexamToothDat._treatedadulttooth ? Integer.parseInt(student._medexamToothDat._treatedadulttooth) : 0;
                _svf.VrsOut("UBAUMU", reBaby + reAdult + treBaby + treAdult > 0 ? "あり" : "なし");
                _svf.VrsOut("REMAIN_TOOTH", reBaby + reAdult > 0 ? "あり" : "なし");
                final int brackB = null != student._medexamToothDat._brackBabytooth ? Integer.parseInt(student._medexamToothDat._brackBabytooth) : 0;
                _svf.VrsOut("BRACK_BABYTOOTH", brackB > 0 ? "あり" : "なし");
                final int brackA = null != student._medexamToothDat._brackAdulttooth ? Integer.parseInt(student._medexamToothDat._brackAdulttooth) : 0;
                _svf.VrsOut("BRACK_ADULTTOOTH", brackA > 0 ? "あり" : "なし");
                final int lostA = null != student._medexamToothDat._lostadulttooth ? Integer.parseInt(student._medexamToothDat._lostadulttooth) : 0;
                _svf.VrsOut("LOSTADULTTOOTH", lostA > 0 ? "あり" : "なし");

                setName("JAWS_JOINT1", "F510", student._medexamToothDat._jawsJointcd, "NAME1", 6, "", "_1");
                setName("JAWS_JOINT2", "F511", student._medexamToothDat._jawsJointcd2, "NAME1", 6, "", "_1");
                setName("PLAQUE", "F520", student._medexamToothDat._plaquecd, "NAME1", 6, "", "_1");
                setName("GUM", "F513", student._medexamToothDat._gumcd, "NAME1", 6, "", "_1");
            }
            _svf.VrEndRecord();
            hasData = true;
        }
        return hasData;
    }

    private void setClubName(final Student student) throws SQLException {
        final String clubSql = getClubSql(student);
        int cnt = 1;
        PreparedStatement psClub = null;
        ResultSet rsClub = null;
        try {
            psClub = _db2.prepareStatement(clubSql);
            rsClub = psClub.executeQuery();
            final StringBuffer setClubName = new StringBuffer();
            String sep = "";
            while (rsClub.next()) {
                setClubName.append(sep + rsClub.getString("CLUBNAME"));
                sep = "/";
                cnt++;
                if (cnt > 2) {
                    break;
                }
            }
            _svf.VrsOut("CLUBNAME", setClubName.toString());
        } finally {
            DbUtils.closeQuietly(null, psClub, rsClub);
            _db2.commit();
        }
    }

    private String getClubSql(final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CLUBCD, ");
        stb.append("     T1.SDATE, ");
        stb.append("     L1.CLUBNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_CLUB_HIST_DAT T1 ");
        stb.append("     LEFT JOIN CLUB_MST L1 ON T1.CLUBCD = L1.CLUBCD ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = '" + student._schregno + "' ");
        stb.append("     AND T1.EDATE IS NULL ");
        stb.append(" ORDER BY ");
        stb.append("     T1.SDATE ");

        return stb.toString();
    }

    // 肥満度計算
    //  肥満度（過体重度）= 100 × (測定された体重 - 標準体重) / 標準体重
    private String calcHimando(final Student student, final Map physAvgMap, final Param param) {
        if (null == student._medexamDetDat._weight) {
            log.debug(" " + student._schregno + ", " + param._year + " 体重がnull");
            return null;
        }
        BigDecimal weightAvg = null;
        final boolean isUseMethod2 = true;
        if (isUseMethod2) {
            // final BigDecimal weightAvg1 = getWeightAvgMethod1(student, mdnd, param);
            final BigDecimal weightAvg2 = getWeightAvgMethod2(student, physAvgMap, param);
            // log.fatal(" (schregno, attendno, weight1, weight2) = (" + student._schregno + ", " + student._attendno + ", " + weightAvg1 + ", " + weightAvg2 + ")");
            log.fatal(" (schregno, attendno, weight2) = (" + student._schregno + ", " + student._attendno + ", " + weightAvg2 + ")");
            weightAvg = weightAvg2;
        } else {
            // weightAvg = null; getWeightAvgMethod0(student, mdnd, physAvgMap);
        }
        if (null == weightAvg) {
            return null;
        }
        final BigDecimal himando = new BigDecimal(100).multiply(new BigDecimal(Double.parseDouble(student._medexamDetDat._weight)).subtract(weightAvg)).divide(weightAvg, 1, BigDecimal.ROUND_HALF_UP);
        log.fatal(" himando = 100 * (" + student._medexamDetDat._weight + " - " + weightAvg + ") / " + weightAvg + " = " + himando);
        return himando.toString();
    }

    private BigDecimal getWeightAvgMethod2(final Student student, final Map physAvgMap, final Param param) {
        if (null == student._medexamDetDat._height) {
            log.debug(" " + student._schregno + ", " + param._year + " 身長がnull");
            return null;
        }
        if (null == student._birthDay) {
            log.debug(" " + student._schregno + ", " + param._year + " 生年月日がnull");
            return null;
        }
        // 日本小児内分泌学会 (http://jspe.umin.jp/)
        // http://jspe.umin.jp/ipp_taikaku.htm ２．肥満度 ２）性別・年齢別・身長別標準体重（５歳以降）のデータによる
        // ａ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_A
        // ｂ＝ HEXAM_PHYSICAL_AVG_DAT.STD_WEIGHT_KEISU_B　
        // 標準体重＝ａ×身長（cm）- ｂ 　 　
        final BigDecimal height = new BigDecimal(student._medexamDetDat._height);
        final String kihonDate = param._year + "-04-01";
        final int iNenrei = (int) getNenrei(student, kihonDate, param._year, param._year);
//        final int iNenrei = (int) getNenrei2(student, param._year, param._year);
        final HexamPhysicalAvgDat hpad = getPhysicalAvgDatNenrei(iNenrei, (List) physAvgMap.get(student._sexCd));
        if (null == hpad || null == hpad._stdWeightKeisuA || null == hpad._stdWeightKeisuB) {
            return null;
        }
        final BigDecimal a = hpad._stdWeightKeisuA;
        final BigDecimal b = hpad._stdWeightKeisuB;
        final BigDecimal avgWeight = a.multiply(height).subtract(b);
        log.fatal(" method2 avgWeight = " + a + " * " + height + " - " + b + " = " + avgWeight);
        return avgWeight;
    }

    // 学年から年齢を計算する
    private double getNenrei2(final Student student, final String year1, final String year2) {
        return 5.0 + Integer.parseInt(student._grade) - (StringUtils.isNumeric(year1) && StringUtils.isNumeric(year2) ? Integer.parseInt(year1) - Integer.parseInt(year2) : 0); // 1年生:6才、2年生:7才、...6年生:11才
    }

    // 生年月日と対象日付から年齢を計算する
    private double getNenrei(final Student student, final String date, final String year1, final String year2) {
        if (null == student._birthDay) {
            return getNenrei2(student, year1, year2);
        }
        final Calendar calBirthDate = Calendar.getInstance();
        calBirthDate.setTime(java.sql.Date.valueOf(student._birthDay));
        final int birthYear = calBirthDate.get(Calendar.YEAR);
        final int birthDayOfYear = calBirthDate.get(Calendar.DAY_OF_YEAR);

        final Calendar calTestDate = Calendar.getInstance();
        calTestDate.setTime(java.sql.Date.valueOf(date));
        final int testYear = calTestDate.get(Calendar.YEAR);
        final int testDayOfYear = calTestDate.get(Calendar.DAY_OF_YEAR);
        
        int nenreiYear = testYear - birthYear + (testDayOfYear - birthDayOfYear < 0 ? -1 : 0);
        final int nenreiDateOfYear = testDayOfYear - birthDayOfYear + (testDayOfYear - birthDayOfYear < 0 ? 365 : 0);
        final double nenrei = nenreiYear + nenreiDateOfYear / 365.0;
        return nenrei;
    }

    // 年齢の平均データを得る
    private HexamPhysicalAvgDat getPhysicalAvgDatNenrei(final int nenrei, final List physAvgList) {
        HexamPhysicalAvgDat tgt = null;
        for (final Iterator it = physAvgList.iterator(); it.hasNext();) {
            final HexamPhysicalAvgDat hpad = (HexamPhysicalAvgDat) it.next();
            if (hpad._nenrei <= nenrei) {
                tgt = hpad;
                if (hpad._nenreiYear == nenrei) {
                    break;
                }
            }
        }
        return tgt;
    }
}

// eof
