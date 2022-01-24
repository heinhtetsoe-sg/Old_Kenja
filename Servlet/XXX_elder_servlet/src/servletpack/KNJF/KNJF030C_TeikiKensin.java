// kanji=漢字
/*
 * $Id: 5b2931510e102ced60968adbcd909b4387b11f61 $
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJF.KNJF030C.HexamPhysicalAvgDat;
import servletpack.KNJF.KNJF030C.Param;
import servletpack.KNJF.KNJF030C.Student;
import servletpack.KNJF.detail.MedexamDetDat;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 5b2931510e102ced60968adbcd909b4387b11f61 $
 */
public class KNJF030C_TeikiKensin extends KNJF030CAbstract {

    public KNJF030C_TeikiKensin(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) {
        super(param, db2, svf);
    }

    private static final Log log = LogFactory.getLog("KNJF030C_TeikiKensin.class");

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
        _svf.VrSetForm(_param._isKumamoto ? F_TEIKI_KENSIN_KUMA : _param._isMusashinoHigashi ? F_TEIKI_KENSIN_MUSASHI : F_TEIKI_KENSIN, 1);
        for (final Iterator itStudent = printStudents.iterator(); itStudent.hasNext();) {
            printTitle(TEIKI_KENSIN_DOC);
            final Student student = (Student) itStudent.next();
            if (null == _param._teikiKensinDatePhp) {
                _svf.VrsOut("DATE", _param.changePrintDate(_param._ctrlDate));
            } else {
                _svf.VrsOut("DATE", _param.changePrintDate(_param._teikiKensinDate));
            }
            _svf.VrsOut("SCHOOLNAME1", StringUtils.defaultString(_param.schoolInfoVal(student._schoolKind, Param.SCHOOL_NAME1)) + "  保健室");
            if (_param._isMusashinoHigashi) {
                _svf.VrsOut("PRINCIPAL_NAME", StringUtils.defaultString(_param.schoolInfoVal(student._schoolKind, Param.PRINCIPAL_JOBNAME)) + "　" + StringUtils.defaultString(_param.schoolInfoVal(student._schoolKind, Param.PRINCIPAL_NAME)));
            } else {
                _svf.VrsOut("PRINCIPAL_NAME", _param.schoolInfoVal(student._schoolKind, Param.PRINCIPAL_NAME));
                _svf.VrsOut("PRINCIPAL_JOBNAME", _param.schoolInfoVal(student._schoolKind, Param.PRINCIPAL_JOBNAME));
            }
            _svf.VrsOut("HR_NAME", student._hrName);
            _svf.VrsOut("ATTENDNO", String.valueOf(Integer.parseInt(student._attendno)) + "番");
            if (_param._isMusashinoHigashi) {
                _svf.VrsOut("NAME", student._name);
                if ("H".equals(student._schoolKind)) {
                    _svf.VrsOut("MAJOR", student._majorname);
                }
            } else {
                final String fieldNo = (17 < student._name.length()) ? "3" : (10 < student._name.length()) ? "2" : "";
                _svf.VrsOut("NAME" + fieldNo, student._name);
                _svf.VrsOut("MAJOR", student._majorname);
            }

            if (null != student._medexamDetDat) {
                final String detMonth = null != student._medexamDetDat._detMonth ? student._medexamDetDat._detMonth + "月" : "";
                final String detDate = _param.changePrintYear(student._medexamDetDat._detYear) + detMonth;
                _svf.VrsOut("DET_DATE", detDate);
                _svf.VrsOut("HEIGHT", student._medexamDetDat._height);
                _svf.VrsOut("WEIGHT", student._medexamDetDat._weight);
                _svf.VrsOut("SITHEIGHT", student._medexamDetDat._sitheight);
                // 視力
                _svf.VrsOut("R_BAREVISION_MARK", student._medexamDetDat._rBarevisionMark);
                _svf.VrsOut("R_VISION_MARK", student._medexamDetDat._rVisionMark);
                _svf.VrsOut("L_BAREVISION_MARK", student._medexamDetDat._lBarevisionMark);
                _svf.VrsOut("L_VISION_MARK", student._medexamDetDat._lVisionMark);
                // BMI
                if (!_param._isKumamoto) {
                    if (_param._isMusashinoHigashi && ("K".equals(student._schoolKind) || "H".equals(student._schoolKind))) {
                        _svf.VrsOut("BMI_SLASH", "／");
                    } else {
                        _svf.VrsOut("BMI", calcHimando(student, physAvgMap, _param));
                    }
                } else {
                    _svf.VrsOut("BMI", student._medexamDetDat._bmi);
                }
                if (_param._tbPrint) {
                    // 結核検査(X線)
                    if (_param._isMusashinoHigashi) {
                        final String tbRemark = getNameWithNamespare2("F100", student._medexamDetDat._tbRemarkcd, "NAME1", "");
                        final String tbXRay = StringUtils.defaultString(student._medexamDetDat._tbXRay);
                        final String tbName = getNameWithNamespare2("F120", student._medexamDetDat._tbNamecd, "NAME1", "");
                        final String tbAdvice = getNameWithNamespare2("F130", student._medexamDetDat._tbAdvisecd, "NAME1", "");
                        _svf.VrsOut("TB_X_RAY", concatWith(new String[] {tbRemark, tbXRay, tbName, tbAdvice}, " "));
                    } else {
                        _svf.VrsOut("TB_X_RAY", getNameWithNamespare2("F100", student._medexamDetDat._tbRemarkcd, "NAME1", ""));
                        _svf.VrsOut("TB_X_RAY_REMARK", null != student._medexamDetDat._tbXRay ? "(" + student._medexamDetDat._tbXRay + ")" : "");
                    }
                }
                // 聴力
                if ("2".equals(_param._printKenkouSindanIppan)) {
                    final String setRyear = "99".equals(student._medexamDetDat._rEar) ? "(" + student._medexamDetDat._rEarDb + ")" : "";
                    final String setLyear = "99".equals(student._medexamDetDat._lEar) ? "(" + student._medexamDetDat._lEarDb + ")" : "";
                    _svf.VrsOut("R_EAR", getNameWithNamespare2("F010", student._medexamDetDat._rEar, "NAME1", setRyear));
                    _svf.VrsOut("L_EAR", getNameWithNamespare2("F010", student._medexamDetDat._lEar, "NAME1", setLyear));
                } else {
                    _svf.VrsOut("R_EAR", getNameWithNamespare2("F010", student._medexamDetDat._rEar, "NAME1", ""));
                    _svf.VrsOut("L_EAR", getNameWithNamespare2("F010", student._medexamDetDat._lEar, "NAME1", ""));
                }
                // 尿検査
                _svf.VrsOut("ALBUMINURIA1", getNameWithNamespare2("F020", student._medexamDetDat._albuminuria1cd, "NAME1", ""));
                _svf.VrsOut("URICSUGAR1", getNameWithNamespare2("F019", student._medexamDetDat._uricsugar1cd, "NAME1", ""));
                _svf.VrsOut("URICBLEED1", getNameWithNamespare2("F018", student._medexamDetDat._uricbleed1cd, "NAME1", ""));
                _svf.VrsOut("ALBUMINURIA2", getNameWithNamespare2("F020", student._medexamDetDat._albuminuria2cd, "NAME1", ""));
                _svf.VrsOut("URICSUGAR2", getNameWithNamespare2("F019", student._medexamDetDat._uricsugar2cd, "NAME1", ""));
                _svf.VrsOut("URICBLEED2", getNameWithNamespare2("F018", student._medexamDetDat._uricbleed2cd, "NAME1", ""));
                if (_param._heartMedexamPrint) {
                    // 心電図検査
                    if (_param._isMusashinoHigashi) {
                        _svf.VrsOut("HEART_MEDEXAM", getTextOrName(student._medexamDetDat._heartMedexamRemark, "F080", student._medexamDetDat._heartMedexam, "NAME1", ""));
                    } else {
                        String name = getName("F080", student._medexamDetDat._heartMedexam, "NAME1");
                        String remark = null != student._medexamDetDat._managementRemark ? "(" + student._medexamDetDat._managementRemark + ")" : "";
                        _svf.VrsOut("HEART_MEDEXAM", name + remark);
                    }
                }
                _svf.VrsOut("OTHERDISEASE", getNameWithNamespare2("F144", student._medexamDetDat._docCd, "NAME1", ""));
                _svf.VrsOut("REMARK", null != student._medexamDetDat._docRemark ? "(" + student._medexamDetDat._docRemark + ")": "");
                // 眼科
                if (_param._isMusashinoHigashi) {
                    _svf.VrsOut("EYEDISEASE", getTextOrName(student._medexamDetDat._eyeTestResult, "F050", student._medexamDetDat._eyediseasecd, "NAME1", ""));
                } else {
                    _svf.VrsOut("EYEDISEASE", getNameWithNamespare2("F050", student._medexamDetDat._eyediseasecd, "NAME1", ""));
                    _svf.VrsOut("EYE_TEST_RESULT", null != student._medexamDetDat._eyeTestResult ? "(" + student._medexamDetDat._eyeTestResult + ")" : "");
                }
                // 内科検診
                _svf.VrsOut("NUTRITIONCD", getTextOrName(student._medexamDetDat._nutritioncdRemark, "F030", student._medexamDetDat._nutritioncd, "NAME1", "")); // 内科検診 栄養状態
                if (_param._isMusashinoHigashi) {
                    _svf.VrsOut("SPINERIBCD", getTextOrName(student._medexamDetDat._spineribcdRemark, "F040", student._medexamDetDat._spineribcd, "NAME1", "")); // 内科検診 脊柱 胸郭 四肢
                } else {
                    _svf.VrsOut("SPINERIBCD", getNameWithNamespare2("F040", student._medexamDetDat._spineribcd, "NAME1", prependString("/", student._medexamDetDat._spineribcdRemark))); // 内科検診 脊柱 胸郭 四肢
                }
                _svf.VrsOut("SKINDISEASECD", getTextOrName(student._medexamDetDat._skindiseasecdRemark, "F070", student._medexamDetDat._skindiseasecd, "NAME1", "")); // 内科検診 皮膚疾患
                // 耳鼻咽頭疾患
                _svf.VrsOut("NOSEDISEASE1", getNoseDisease(student._medexamDetDat));
            }
            if (null != student._medexamToothDat) {
                final String remain = add(student._medexamToothDat._remainbabytooth, student._medexamToothDat._remainadulttooth);
                if (_param._isMusashinoHigashi) {
                    _svf.VrsOut("REMAIN_TOOTH", remain);
                } else {
                    _svf.VrsOut("REMAIN_TOOTH", toInt(remain, -1) == -1 ? "" : toInt(remain, -1) > 0 ? "あり" : "なし");
                }
                if (_param._isMusashinoHigashi) {
                    _svf.VrsOut("BRACK_BABYTOOTH", student._medexamToothDat._brackBabytooth);
                } else {
                    _svf.VrsOut("BRACK_BABYTOOTH", null == student._medexamToothDat._brackBabytooth ? "" : toInt(student._medexamToothDat._brackBabytooth, 0) > 0 ? "あり" : "なし");
                }
                if (_param._isMusashinoHigashi) {
                    _svf.VrsOut("BRACK_ADULTTOOTH", student._medexamToothDat._brackAdulttooth);
                } else {
                    _svf.VrsOut("BRACK_ADULTTOOTH", null == student._medexamToothDat._brackAdulttooth ? "" : toInt(student._medexamToothDat._brackAdulttooth, 0) > 0 ? "あり" : "なし");
                }
                if (_param._isMusashinoHigashi) {
                    _svf.VrsOut("TREATEDADULTTOOTH", add(student._medexamToothDat._treatedbabytooth, student._medexamToothDat._treatedadulttooth));
                } else {
                    _svf.VrsOut("TREATEDADULTTOOTH", null == student._medexamToothDat._treatedadulttooth ? "" : toInt(student._medexamToothDat._treatedadulttooth, 0) > 0 ? "あり" : "なし");
                }
                if (_param._isMusashinoHigashi) {
                    _svf.VrsOut("LOSTADULTTOOTH", student._medexamToothDat._lostadulttooth);
                } else {
                    _svf.VrsOut("LOSTADULTTOOTH", null == student._medexamToothDat._lostadulttooth ? "" : toInt(student._medexamToothDat._lostadulttooth, 0) > 0 ? "あり" : "なし");
                }
                _svf.VrsOut("THOROUGH_TOOTH", null == student._medexamToothDat._checkAdulttooth ? "" : toInt(student._medexamToothDat._checkAdulttooth, 0) > 0 ? "あり" : "なし");

                setName("JAWS_JOINT1", 10, "_1", getNameWithNamespare2("F510", student._medexamToothDat._jawsJointcd, "NAME1", ""));
                setName("JAWS_JOINT2", 10, "_1", getNameWithNamespare2("F511", student._medexamToothDat._jawsJointcd2, "NAME1", ""));
                setName("PLAQUE", 10, "1_1", getNameWithNamespare2("F520", student._medexamToothDat._plaquecd, "NAME1", ""));
                setName("GUM", 10, "1_1", getNameWithNamespare2("F513", student._medexamToothDat._gumcd, "NAME1", ""));
                setName("TARTAR", 10, "1_1", getNameWithNamespare2("F521", student._medexamToothDat._calculuscd, "NAME1", ""));

                String setToothOther = "";
                if ("99".equals(student._medexamToothDat._otherdiseasecd)) {
                    setToothOther = StringUtils.defaultString(student._medexamToothDat._otherdisease, "");
                } else {
                    setToothOther += student._medexamToothDat._otherdiseasecd == null ? "" : getNameWithNamespare2("F530", student._medexamToothDat._otherdiseasecd, "NAME1", "");
                }
                final String setToothOther2 = student._medexamToothDat._otherdiseasecd2 == null ? "" : getNameWithNamespare2("F531", student._medexamToothDat._otherdiseasecd2, "NAME1", StringUtils.defaultString(student._medexamToothDat._otherdisease2));
                final String sep = setToothOther.length() > 0 && setToothOther2.length() > 0 ? "/" : "";
                setToothOther += sep + setToothOther2;
                final String setField = setToothOther.length() > 10 ? "1_1" : "";
                _svf.VrsOut("TOOTH_OTHER" + setField, setToothOther);
            }
            if (!_param._isChukyo) { // 中京は表示しない
                _svf.VrsOut("NOTICE", "注3）聴力検査は、1年生と3年生実施。");
            }
            _svf.VrEndPage();
            hasData = true;
        }
        return hasData;
    }

    private String getNoseDisease(final MedexamDetDat medexamDetDat) throws SQLException {
        String noseDisease = concatWith(new String[] {
                getTextOrNameWithNamespare2(medexamDetDat._nosediseasecdRemark1, "F061", medexamDetDat._nosediseasecd5, "NAME1", "")
              , getTextOrNameWithNamespare2(medexamDetDat._nosediseasecdRemark2, "F062", medexamDetDat._nosediseasecd6, "NAME1", "")
              , getTextOrNameWithNamespare2(medexamDetDat._nosediseasecdRemark3, "F063", medexamDetDat._nosediseasecd7, "NAME1", "")
              , medexamDetDat._nosediseasecdRemark
        }, "、");
        if (StringUtils.isEmpty(noseDisease)) {
            noseDisease = getNameWithNamespare2("F060", medexamDetDat._nosediseasecd, "NAME1", "");
        }
        return noseDisease;
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

    private int toInt(final String string, final int defaultValue) {
        if (!NumberUtils.isDigits(string)) {
            return defaultValue;
        }
        return Integer.parseInt(string);
    }

    /**
     * 引数を数値文字列として加算した結果の文字列(両方数値でないならnull)を返す。
     * @param n1 数値文字列1
     * @param n2 数値文字列2
     * @return 加算した結果の文字列
     */
    private String add(final String s1, final String s2) {
        if (!NumberUtils.isDigits(s1)) { return s2; }
        if (!NumberUtils.isDigits(s2)) { return s1; }
        return String.valueOf(Integer.parseInt(s1) + Integer.parseInt(s2));
    }

    /**
     * 配列内のブランクでない文字列をコンマで連結する
     * @param array 配列
     * @param comma コンマ
     * @return 連結した文字列
     */
    private String concatWith(final String[] array, final String comma) {
        final StringBuffer stb = new StringBuffer();
        if (null != array) {
            for (int i = 0; i < array.length; i++) {
                if (null == array[i]) {
                    continue;
                }
                if (stb.length() > 0 && array[i].length() > 0) {
                    stb.append(comma);
                }
                stb.append(array[i]);
            }
        }
        return stb.toString();
    }

    /**
     * 対象文字列がnullでない場合、前に置く文字列と連結して返す
     * @param prepend 前に置く文字列
     * @param s 対象文字列
     * @return 連結した文字列
     */
    private String prependString(final String prepend, final String s) {
        if (null == s) {
            return "";
        }
        return prepend + s;
    }

    private void setName(final String fieldName, final int lineLength, final String fieldNameOver, final String retStr) {
        if (lineLength > 0 && retStr.length() > lineLength) {
            _svf.VrsOut(fieldName + fieldNameOver, retStr);
        } else {
            _svf.VrsOut(fieldName, retStr);
        }
    }

    private String getName(final String nameCd1, final String nameCd2, final String useFieldName, final String plusString) throws SQLException {
        final String sql = "SELECT VALUE(" + useFieldName + ", '') AS LABEL, NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
        ResultSet rs = null;
        PreparedStatement ps = null;
        String retStr = "";
        try {
            ps = _db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                retStr = (!NumberUtils.isDigits(nameCd2) ? "" : rs.getString("LABEL")) + plusString;
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            _db2.commit();
        }
        return retStr;
    }

    private String getNameWithNamespare2(final String nameCd1, final String nameCd2, final String useFieldName, final String plusString) throws SQLException {
        String retStr = getName(nameCd1, nameCd2, useFieldName, plusString);

        final String namespare2 = getName(nameCd1, nameCd2, "NAMESPARE2", "");
        if ("1".equals(_param._kenkouSindanIppanNotPrintNameMstComboNamespare2Is1) && "1".equals(namespare2)) {
            retStr = "";
        }
        return retStr;
    }

    private String getTextOrName(final String text, final String nameCd1, final String nameCd2, final String useFieldName, final String plusString) throws SQLException {
        String retStr = getName(nameCd1, nameCd2, useFieldName, plusString);

        final String namespare2 = getName(nameCd1, nameCd2, "NAMESPARE2", "");
        if ("1".equals(_param._kenkouSindanIppanNotPrintNameMstComboNamespare2Is1) && "1".equals(namespare2)) {
            if (!StringUtils.isEmpty(text)) {
                // テキストのみ
                return text;
            }
            retStr = StringUtils.defaultString(retStr);
        } else {
            if (!StringUtils.isEmpty(text)) {
                // コンボの内容+テキストのみ
                retStr += "(" + text + ")";
            }
        }
        return retStr;
    }

    private String getTextOrNameWithNamespare2(final String text, final String nameCd1, final String nameCd2, final String useFieldName, final String plusString) throws SQLException {
        String retStr = getNameWithNamespare2(nameCd1, nameCd2, useFieldName, plusString);

        final String namespare2 = getName(nameCd1, nameCd2, "NAMESPARE2", "");
        if ("1".equals(_param._kenkouSindanIppanNotPrintNameMstComboNamespare2Is1) && "1".equals(namespare2)) {
            if (!StringUtils.isEmpty(text)) {
                // テキストのみ
                return text;
            }
            retStr = StringUtils.defaultString(retStr);
        } else {
            if (!StringUtils.isEmpty(text)) {
                // コンボの内容+テキストのみ
                retStr += "(" + text + ")";
            }
        }
        return retStr;
    }

}

// eof
