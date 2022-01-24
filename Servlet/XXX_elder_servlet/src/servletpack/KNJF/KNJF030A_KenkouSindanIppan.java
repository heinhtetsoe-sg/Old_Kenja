// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2009/11/07 15:27:39 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJF.KNJF030C.Param;
import servletpack.KNJF.KNJF030C.Student;
import servletpack.KNJF.detail.MedexamDetDat;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id$
 */
public class KNJF030A_KenkouSindanIppan extends KNJF030CAbstract {

    private static final Log log = LogFactory.getLog("KNJF030A_KenkouSindanIppan.class");

    private boolean hasData = false;

    private String FORM_KNJF030A_1_5 = "KNJF030A_1_5.frm";
    private String FORM_KNJF030A_1_8 = "KNJF030A_1_8.frm";
    private String FORM_KNJF030A_1_9 = "KNJF030A_1_9.frm";
    private String FORM_KNJF030A_1_5G = "KNJF030A_1_5G.frm";
    private String FORM_KNJF030A_1P_5 = "KNJF030A_1P_5.frm";
    private String FORM_KNJF030A_1P_5G = "KNJF030A_1P_5G.frm";

    private KNJF030AParam _paramA;

    public KNJF030A_KenkouSindanIppan(
            final Param param,
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws SQLException {
        super(param, db2, svf);
        _paramA = new KNJF030AParam(_db2);
        log.fatal("$Revision: 76834 $ $Date: 2020-09-14 13:58:20 +0900 (月, 14 9 2020) $"); // CVSキーワードの取り扱いに注意
    }

    /**
     * {@inheritDoc}
     */
    protected boolean printMain(final List printStudents) throws SQLException {
        hasData = false;
        for (final Iterator itStudent = printStudents.iterator(); itStudent.hasNext();) {
            final Student student = (Student) itStudent.next();
            log.debug(" schregno = " + student._schregno);
            printOut(_db2, student);
        }
        if (_paramA._isOutputDebug) {
            log.info(" hasData = " + hasData);
        }
        return hasData;
    }

    private static int getMS932ByteLength(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private static String getKakko(final String value) {
        return null == value ? "" : "(" + value + ")";
    }

    private static int getMaxMS932ByteLength(final String[] strs) {
        int max = 0;
        for (int i = 0; i < strs.length; i++) {
            max = Math.max(max, getMS932ByteLength(strs[i]));
        }
        return max;
    }

    /**
     * 文字列をMS932にエンコードして指定分割バイト数で分割する
     * @param str 文字列
     * @param splitByte MS932にエンコードした場合の分割バイト数
     * @return 分割された文字列
     */
    private static String[] getMS932ByteToken(final String str, final int[] splitByte) {
        if (null == str) {
            return new String[] {};
        }
        final List tokenList = new ArrayList();
        StringBuffer token = new StringBuffer();
        int splitByteIdx = 0;
        int currentSplitByte = splitByte.length - 1 < splitByteIdx ? 99999999 : splitByte[splitByteIdx];
        for (int i = 0; i < str.length(); i++) {
            final String ch = String.valueOf(str.charAt(i));
            if (getMS932ByteLength(token.toString() + ch) > currentSplitByte) {
                tokenList.add(token.toString());
                token = new StringBuffer();
                splitByteIdx += 1;
                currentSplitByte = splitByte.length - 1 < splitByteIdx ? 99999999 : splitByte[splitByteIdx];
            }
            token.append(String.valueOf(ch));
        }
        if (token.length() != 0) {
            tokenList.add(token.toString());
        }
        final String[] array = new String[tokenList.size()];
        for (int i = 0; i < tokenList.size(); i++) {
            array[i] = (String) tokenList.get(i);
        }
        return array;
    }

    private static String append(final String a, final String b) {
        if (StringUtils.isEmpty(a)) {
            return "";
        }
        return a + StringUtils.defaultString(b);
    }

    private static class HexamPhysicalAvgDat {
        final String _sex;
        final int _nenreiYear;
        final int _nenreiMonth;
        final double _nenrei;
        final BigDecimal _heightAvg;
        final BigDecimal _heightSd;
        final BigDecimal _weightAvg;
        final BigDecimal _weightSd;
        final BigDecimal _stdWeightKeisuA;
        final BigDecimal _stdWeightKeisuB;

        HexamPhysicalAvgDat(
            final String sex,
            final int nenreiYear,
            final int nenreiMonth,
            final BigDecimal heightAvg,
            final BigDecimal heightSd,
            final BigDecimal weightAvg,
            final BigDecimal weightSd,
            final BigDecimal stdWeightKeisuA,
            final BigDecimal stdWeightKeisuB
        ) {
            _sex = sex;
            _nenreiYear = nenreiYear;
            _nenreiMonth = nenreiMonth;
            _nenrei = _nenreiYear + (_nenreiMonth / 12.0);
            _heightAvg = heightAvg;
            _heightSd = heightSd;
            _weightAvg = weightAvg;
            _weightSd = weightSd;
            _stdWeightKeisuA = stdWeightKeisuA;
            _stdWeightKeisuB = stdWeightKeisuB;
        }

        public static Map getHexamPhysicalAvgMap(final DB2UDB db2, final Param param) {
            final Map m = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String sex = rs.getString("SEX");
                    final int nenreiYear = rs.getInt("NENREI_YEAR");
                    final int nenreiMonth = rs.getInt("NENREI_MONTH");
                    // if (ageMonth % 3 != 0) { continue; }
                    final BigDecimal heightAvg = rs.getBigDecimal("HEIGHT_AVG");
                    final BigDecimal heightSd = rs.getBigDecimal("HEIGHT_SD");
                    final BigDecimal weightAvg = rs.getBigDecimal("WEIGHT_AVG");
                    final BigDecimal weightSd = rs.getBigDecimal("WEIGHT_SD");
                    final BigDecimal stdWeightKeisuA = rs.getBigDecimal("STD_WEIGHT_KEISU_A");
                    final BigDecimal stdWeightKeisuB = rs.getBigDecimal("STD_WEIGHT_KEISU_B");
                    final HexamPhysicalAvgDat testheightweight = new HexamPhysicalAvgDat(sex, nenreiYear, nenreiMonth, heightAvg, heightSd, weightAvg, weightSd, stdWeightKeisuA, stdWeightKeisuB);
                    if (null == m.get(rs.getString("SEX"))) {
                        m.put(rs.getString("SEX"), new ArrayList());
                    }
                    final List list = (List) m.get(rs.getString("SEX"));
                    list.add(testheightweight);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return m;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH MAX_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MAX(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR <= '" + param._year + "' ");
            stb.append(" ), MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(YEAR) AS YEAR ");
            stb.append("   FROM ");
            stb.append("       HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR >= '" + param._year + "' ");
            stb.append(" ), MAX_MIN_YEAR AS ( ");
            stb.append("   SELECT ");
            stb.append("       MIN(T1.YEAR) AS YEAR ");
            stb.append("   FROM ( ");
            stb.append("       SELECT YEAR FROM MAX_YEAR T1 ");
            stb.append("       UNION ");
            stb.append("       SELECT YEAR FROM MIN_YEAR T1 ");
            stb.append("   ) T1 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SEX, ");
            stb.append("     T1.NENREI_YEAR, ");
            stb.append("     T1.NENREI_MONTH, ");
            stb.append("     T1.HEIGHT_AVG, ");
            stb.append("     T1.HEIGHT_SD, ");
            stb.append("     T1.WEIGHT_AVG, ");
            stb.append("     T1.WEIGHT_SD, ");
            stb.append("     T1.STD_WEIGHT_KEISU_A, ");
            stb.append("     T1.STD_WEIGHT_KEISU_B ");
            stb.append(" FROM ");
            stb.append("    HEXAM_PHYSICAL_AVG_DAT T1 ");
            stb.append("    INNER JOIN MAX_MIN_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEX, T1.NENREI_YEAR, T1.NENREI_MONTH ");
            return stb.toString();
        }
    }

    private static class Util {
        // 肥満度計算
        //  肥満度（過体重度）= 100 × (測定された体重 - 標準体重) / 標準体重
        public static String calcHimando(final RegdData student, final DB2UDB db2, final Param param) {
            if (null == param._physAvgMap) {
                param._physAvgMap = HexamPhysicalAvgDat.getHexamPhysicalAvgMap(db2, param);
            }
            final String weight = student._medexamDetDat._weight;
            if (null == weight) {
                log.debug(" " + student._schregNo + ", " + param._year + " 体重がnull");
                return null;
            }
            BigDecimal weightAvg = null;
            final boolean isUseMethod2 = true;
            if (isUseMethod2) {
                // final BigDecimal weightAvg1 = getWeightAvgMethod1(student, mdnd, param);
                final BigDecimal weightAvg2 = getWeightAvgMethod2(student, param._physAvgMap, param);
                // log.fatal(" (schregno, attendno, weight1, weight2) = (" + rs.getString("SCHREGNO") + ", " + rs.getString("ATTENDNO") + ", " + weightAvg1 + ", " + weightAvg2 + ")");
                log.fatal(" (schregno, attendno, weight2) = (" + student._schregNo + ", " + student._attendNo + ", " + weightAvg2 + ")");
                weightAvg = weightAvg2;
            } else {
                // weightAvg = null; getWeightAvgMethod0(student, mdnd, physAvgMap);
            }
            if (null == weightAvg) {
                return null;
            }
            final BigDecimal himando = new BigDecimal(100).multiply(new BigDecimal(Double.parseDouble(weight)).subtract(weightAvg)).divide(weightAvg, 1, BigDecimal.ROUND_HALF_UP);
            log.fatal(" himando = 100 * (" + weight + " - " + weightAvg + ") / " + weightAvg + " = " + himando);
            return himando.toString();
        }

        private static BigDecimal getWeightAvgMethod2(final RegdData student, final Map physAvgMap, final Param param) {
            final String schregno = student._schregNo;
            if (null == student._medexamDetDat._height) {
                log.debug(" " + schregno + ", " + param._year + " 身長がnull");
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
        private static double getNenrei2(final RegdData student, final String year1, final String year2) throws NumberFormatException {
            return 15.0 + Integer.parseInt(student._grade) - (StringUtils.isNumeric(year1) && StringUtils.isNumeric(year2) ? Integer.parseInt(year1) - Integer.parseInt(year2) : 0);
        }

        // 生年月日と対象日付から年齢を計算する
        private static double getNenrei(final RegdData student, final String date, final String year1, final String year2) throws NumberFormatException {
            if (null == student._birthDay) {
                return getNenrei2(student, year1, year2);
            }
            final Calendar calBirthDate = Calendar.getInstance();
            calBirthDate.setTime(Date.valueOf(student._birthDay));
            final int birthYear = calBirthDate.get(Calendar.YEAR);
            final int birthDayOfYear = calBirthDate.get(Calendar.DAY_OF_YEAR);

            final Calendar calTestDate = Calendar.getInstance();
            calTestDate.setTime(Date.valueOf(date));
            final int testYear = calTestDate.get(Calendar.YEAR);
            final int testDayOfYear = calTestDate.get(Calendar.DAY_OF_YEAR);

            int nenreiYear = testYear - birthYear + (testDayOfYear - birthDayOfYear < 0 ? -1 : 0);
            final int nenreiDateOfYear = testDayOfYear - birthDayOfYear + (testDayOfYear - birthDayOfYear < 0 ? 365 : 0);
            final double nenrei = nenreiYear + nenreiDateOfYear / 365.0;
            return nenrei;
        }

        // 年齢の平均データを得る
        private static HexamPhysicalAvgDat getPhysicalAvgDatNenrei(final int nenrei, final List physAvgList) {
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

    /**
     * 配列内のブランクでない文字列をコンマで連結する
     * @param array 配列
     * @param comma コンマ
     * @return 連結した文字列
     */
    private static String concatWith(final String[] array, final String comma) {
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

    private void svfVrsOutArray(final String[] field, final String[] tokens) {
        for (int i = 0; i < field.length; i++) {
            if (i >= tokens.length) {
                break;
            }
            _svf.VrsOut(field[i], tokens[i]);
        }
    }

    private String formatDate(final DB2UDB db2, final String date) {
        if (_paramA._isSeireki) {
            return KNJ_EditDate.h_format_SeirekiJP(date);
        }
        return KNJ_EditDate.h_format_JP(db2, date);
    }

    private boolean isArrayBlank(final String[] array) {
        return null == array || array.length == 0;
    }

    private void printOut(final DB2UDB db2, final Student student) throws SQLException {
        final String[] printSchoolKind;
        if (_paramA._isChiyodaKudan) {
            printSchoolKind = new String[] {"JH"};
        } else if ("1".equals(_param._use_prg_schoolkind)) {
            if ("1".equals(_param._useForm7_JH_Ippan) && (isArrayBlank(_param._selectSchoolKind) || ArrayUtils.contains(_param._selectSchoolKind, "J") || ArrayUtils.contains(_param._selectSchoolKind, "H"))) {
                printSchoolKind = new String[] {"JH"};
            } else if ("1".equals(_param._useForm9_PJ_Ippan) && (isArrayBlank(_param._selectSchoolKind) || ArrayUtils.contains(_param._selectSchoolKind, "P") || ArrayUtils.contains(_param._selectSchoolKind, "J"))) {
                printSchoolKind = new String[] {"PJ"};
            } else {
                printSchoolKind = isArrayBlank(_param._selectSchoolKind) ? new String[] {"K", "P", "J", "H", "A"} : _param._selectSchoolKind;
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            if ("1".equals(_param._useForm7_JH_Ippan) && ("J".equals(_param._SCHOOLKIND) || "H".equals(_param._SCHOOLKIND))) {
                printSchoolKind = new String[] {"JH"};
            } else if ("1".equals(_param._useForm9_PJ_Ippan) && ("P".equals(_param._SCHOOLKIND) || "J".equals(_param._SCHOOLKIND))) {
                printSchoolKind = new String[] {"PJ"};
            } else {
                printSchoolKind = new String[] {_param._SCHOOLKIND};
            }
        } else {
            if ("1".equals(_param._useForm7_JH_Ippan)) {
                printSchoolKind = new String[] {"K", "P", "JH", "A"};
            } else if ("1".equals(_param._useForm9_PJ_Ippan)) {
                printSchoolKind = new String[] {"K", "PJ", "H", "A"};
            } else {
                printSchoolKind = new String[] {"K", "P", "J", "H", "A"};
            }
        }
        if (_paramA._isOutputDebug) {
            log.info(" printSchoolKind = " + ArrayUtils.toString(printSchoolKind));
        }
        for (int ki = 0; ki < printSchoolKind.length; ki++) {
            final String schoolKind = printSchoolKind[ki];
            final boolean is7nenYou = "JH".equals(schoolKind);
            final boolean is9nenYou = "PJ".equals(schoolKind);
            final List regdDataList = RegdData.getRegdDataList(_db2, student, schoolKind, _param);
            if (regdDataList.size() == 0) {
                continue;
            }
            final String form = getForm(schoolKind);
            if (_paramA._isOutputDebug) {
                log.info(" schoolKind = " + schoolKind + ", form = " + form);
            }
            _paramA.setForm(_svf, form);
            final String title =  is9nenYou ? "児童生徒健康診断票" : "K".equals(schoolKind) ? "園児健康診断票" : "P".equals(schoolKind) ? "児童健康診断票" : "生徒健康診断票";

            int dataCnt = 1;
            if ("2".equals(_param._printKenkouSindanIppan)) {
                // 熊本は入学学年の列から表示
                final RegdData regdData0 = (RegdData) regdDataList.get(0);
                if (NumberUtils.isDigits(regdData0._entYearGradeCd)) {
                    for (int cd = 1; cd < Integer.parseInt(regdData0._entYearGradeCd); cd++) {
                        _svf.VrsOut("SCHREGNO", regdData0._schregNo);   //改ページ用
                        _svf.VrEndRecord();
                        dataCnt += 1;
                    }
                }
            }
            for (final Iterator itPrint = regdDataList.iterator(); itPrint.hasNext();) {
                final RegdData regdData = (RegdData) itPrint.next();
                if (is9nenYou || _paramA._isMusashinohigashi) { // 指定学年まで空列
                    if (NumberUtils.isDigits(regdData._gradeCd)) {
                        final int line = (is9nenYou && "J".equals(regdData._schoolKind) ? 6 : 0) + Integer.parseInt(regdData._gradeCd);
                        for (int i = dataCnt; i < line; i++) {
                            _svf.VrsOut("SCHREGNO", regdData._schregNo);   //改ページ用 レコードの範囲外
                            _svf.VrEndRecord();
                            hasData = true;
                            if (_paramA._isOutputDebug) {
                                log.info(" line(empty1) : " + dataCnt);
                            }
                            dataCnt++;
                        }
                    }
                }
                if (is7nenYou) { // 指定学年まで空列
                    if (NumberUtils.isDigits(regdData._gradeCd)) {
                        final int line = ("H".equals(regdData._schoolKind) ? 3 : 0) + Integer.parseInt(regdData._gradeCd);
                        for (int i = dataCnt; i < line; i++) {
                            _svf.VrsOut("SCHREGNO", regdData._schregNo);   //改ページ用 レコードの範囲外
                            _svf.VrEndRecord();
                            hasData = true;
                            if (_paramA._isOutputDebug) {
                                log.info(" line(empty1) : " + dataCnt);
                            }
                            dataCnt++;
                        }
                    }
                }
                final boolean isKekkaInsatsu = "1".equals(_param._otherInjiParam.get("OUTPUTA"));
                if (isKekkaInsatsu && null == regdData._hasData) {
                    if ("2".equals(_param._printKenkouSindanIppan)) {
                        int rtn = _svf.VrsOut("SCHREGNO", regdData._schregNo);   //改ページ用 レコードの範囲外 // 50.143では動作しない...
                        if (_paramA._isOutputDebug) {
                            log.info(" line(empty2_2) : " + dataCnt + " / VrsOut = " + rtn);
                        }
                        dataCnt++;
                        _svf.VrEndRecord();
                        hasData = true;
                    }
                } else {
                    _svf.VrsOut("SCHREGNO", regdData._schregNo);   //改ページ用 レコードの範囲外
                    if ("on".equals(_param._printSchregNo1)) {
                        _svf.VrsOut("SCHREGNO2", regdData._schregNo);
                    }
                    _svf.VrsOut("NAME_SHOW" + (getMS932ByteLength(student._name) > 24 ? "_2" : ""), student._name);
                    _svf.VrsOut("SEX", student._sex);
                    _svf.VrsOut("BIRTHDAY", formatDate(db2, student._birthDay));
                    if (is7nenYou) {
                        _svf.VrsOut("SCHOOL_NAME1", _param.schoolInfoVal("J", Param.SCHOOL_NAME2));
                        _svf.VrsOut("SCHOOL_NAME2", _param.schoolInfoVal("H", Param.SCHOOL_NAME2));
                    } else {
                        _svf.VrsOut("SCHOOL_NAME", _param.schoolInfoVal(_paramA._isChiyodaKudan ? "H" : schoolKind, Param.SCHOOL_NAME2));
                    }
                    _svf.VrsOut("TITLE",  title);
                    if ("1".equals(_param._printKenkouSindanIppan)) {
                        // 病名のタイトル
                        _svf.VrsOut("DISEASE_NAME_TITLE", _param._isMiyagiken ? "病名" : "疾病及び異常");
                    }

                    if (isKekkaInsatsu) {
                        printMedexam(db2, _svf, schoolKind, form, student, regdData);
                        if (null != regdData._medexamDetDat._date) {
                            _svf.VrsOutn("GRADE", dataCnt, regdData._gradeName);
                            _svf.VrsOutn(null != regdData._hrClassName && regdData._hrClassName.length() > 4 ? "HR_NAME2_1" : "HR_NAME1", dataCnt, regdData._hrClassName);
                            _svf.VrsOutn("ATTENDNO", dataCnt, regdData._attendNo);
                            final String hrname = is9nenYou ? regdData._hrClassName : regdData._hrName;
                            if (_paramA.hrnameKeta > 0 && getMS932ByteLength(hrname) > _paramA.hrnameKeta && _paramA.hrnameC2_1Keta > _paramA.hrnameKeta) {
                                _svf.VrsOutn("HR_NAMEC2_1", dataCnt, hrname);
                            } else {
                                _svf.VrsOutn("HR_NAME", dataCnt, hrname);
                            }
                            if (is9nenYou) {
                                _svf.VrsOutn("YEAR", dataCnt, regdData._gradeName);
                            } else {
                                _svf.VrsOutn("YEAR", dataCnt, _paramA.nendo(db2, regdData._year));
                            }
                            _svf.VrsOut("M_DATE", _paramA.nendo(db2, regdData._year));
                            _svf.VrsOut("AGE", regdData._age);        //４月１日現在の年齢
                        }
                        dataCnt++;
                    }
                    _svf.VrEndRecord();
                    hasData = true;
                }
            }
        }
    }

    private void printMedexam(final DB2UDB db2, final Vrw32alp _svf, final String schoolKind, final String form, final Student student, final RegdData regdData) throws SQLException {
        MedexamDetDat medexamDetDat = regdData._medexamDetDat;
        _svf.VrsOut("HEIGHT", medexamDetDat._height);
        _svf.VrsOut("WEIGHT", medexamDetDat._weight);
        _svf.VrsOut("OBESITY", Util.calcHimando(regdData, db2, _param)); // 肥満度
        if (!"J".equals(schoolKind)) {
            _svf.VrsOut("SIT_HEIGHT", medexamDetDat._sitheight);
        }
        _svf.VrsOut("BUST", medexamDetDat._chest);
        if (!"Ritsumeikan".equals(_param._namemstZ010Name1) || !"P".equals(regdData._schoolKind)) {  //立命館小学校"以外"は栄養状態を出力。
            setName1WithSlash(db2, "NUTRITION", "F030", medexamDetDat._nutritioncd, "SLASH_NUTRITION");
        }
        if ("2".equals(_param._printKenkouSindanIppan)) {
            if (isSonota(db2, "F040", medexamDetDat._spineribcd)) {
                // その他はコード名称を表示しない
                _svf.VrsOut("SPINERIBCD_REMARK2_1", medexamDetDat._spineribcdRemark);
            } else {
                setName1WithSlash(db2, "SPINERIB", "F040", medexamDetDat._spineribcd, "SLASH_SPINERIB");
            }
        } else {
            if (_paramA._isMusashinohigashi && _paramA.spineribsKeta > 0) {
                final String text = append(getName(db2, "F040", medexamDetDat._spineribcd, "NAME1"), " ") + StringUtils.defaultString(medexamDetDat._spineribcdRemark);
                final int keta = getMS932ByteLength(text);
                if (keta <= _paramA.spineribsKeta) {
                    _svf.VrsOut("SPINERIBS", text);
                } else if (keta <= _paramA.spineribs2Keta) {
                    _svf.VrsOut("SPINERIBS2", text);
                } else { // 16 * 4
                    _svf.VrsOut("SPINERIBS3", text);
                }
            } else {
                setName1WithSlash(db2, "SPINERIB", "F040", medexamDetDat._spineribcd, "SLASH_SPINERIB");
                _svf.VrsOut("SPINERIBCD_REMARK", getKakko(medexamDetDat._spineribcdRemark));
            }
        }
        String str2 = "";
        String rBarevisionMark = medexamDetDat._rBarevisionMark;
        String lBarevisionMark = medexamDetDat._lBarevisionMark;
        if (!"2".equals(_param._printKenkouSindanIppan) && "1".equals(medexamDetDat._visionCantMeasure)) {
            str2 = "_2";
            rBarevisionMark = "測定不能";
            lBarevisionMark = "測定不能";
        }

        if ("1".equals(_param._knjf030PrintVisionNumber)) {
            _svf.VrsOut("R_BAREVISION" + str2, medexamDetDat._rBarevision);
            _svf.VrsOut("R_VISION", medexamDetDat._rVision);
            _svf.VrsOut("L_BAREVISION" + str2, medexamDetDat._lBarevision);
            _svf.VrsOut("L_VISION", medexamDetDat._lVision);
        } else {
            _svf.VrsOut("R_BAREVISION" + str2, rBarevisionMark);
            _svf.VrsOut("R_VISION", medexamDetDat._rVisionMark);
            _svf.VrsOut("L_BAREVISION" + str2, lBarevisionMark);
            _svf.VrsOut("L_VISION", medexamDetDat._lVisionMark);
        }

        // 眼の疾病及び異常
        if ("2".equals(_param._printKenkouSindanIppan)) {
            if (isSonota(db2, "F050", medexamDetDat._eyediseasecd)) {
                // その他はコード名称を表示しない
                setSonotaText3(
                        "EYE_TEST_RESULT1_2", "EYE_TEST_RESULT2_2", "EYE_TEST_RESULT3_2",
                        30, "EYE_TEST_RESULT1", "EYE_TEST_RESULT2", "EYE_TEST_RESULT3",
                        medexamDetDat._eyeTestResult, medexamDetDat._eyeTestResult2, medexamDetDat._eyeTestResult3);
            } else {
                setName1WithSlash(db2, "EYEDISEASE", "F050", medexamDetDat._eyediseasecd, "SLASH_EYEDISEASE");
                _svf.VrsOut("EYE_TEST_RESULT", getKakko(medexamDetDat._eyeTestResult));
            }
        } else {
            if (_paramA._isMusashinohigashi && _paramA.eyediseasesKeta > 0) {
                final String text = append(getName(db2, "F050", medexamDetDat._eyediseasecd, "NAME1"), " ") + StringUtils.defaultString(medexamDetDat._eyeTestResult);
                final int keta = getMS932ByteLength(text);
                if (keta <= _paramA.eyediseasesKeta) {
                    _svf.VrsOut("EYEDISEASES", text);
                } else if (keta <= _paramA.eyediseases2Keta) {
                    _svf.VrsOut("EYEDISEASES2", text);
                } else { // 16 * 4
                    _svf.VrsOut("EYEDISEASES3", text);
                }
            } else {
                setName1WithSlash(db2, "EYEDISEASE", "F050", medexamDetDat._eyediseasecd, "SLASH_EYEDISEASE");
                _svf.VrsOut("EYE_TEST_RESULT", getKakko(medexamDetDat._eyeTestResult));
            }
        }

        // 聴力
        if ("2".equals(_param._printKenkouSindanIppan)) {
            setNameWithSlash(db2, "R_EAR", "F010", medexamDetDat._rEar, "04".equals(medexamDetDat._rEar) ? "(" + medexamDetDat._rEarDb + ")" : "", "SLASH_R_EAR");
            setNameWithSlash(db2, "L_EAR", "F010", medexamDetDat._lEar, "04".equals(medexamDetDat._lEar) ? "(" + medexamDetDat._lEarDb + ")" : "", "SLASH_L_EAR");
        } else {
            if (_param._isMiyagiken) {

                if (_paramA.nameMstNamespare1Is1(db2, "F010", medexamDetDat._rEar)) {
                    printSlash("SLASH_R_EAR");
                } else {
                    if ("02".equals(medexamDetDat._rEar)) {
                        final String r1000Hz = (StringUtils.isEmpty(medexamDetDat._rEarDb))     ? "" : "1kHz(" + medexamDetDat._rEarDb +     ")";
                        final String r4000Hz = (StringUtils.isEmpty(medexamDetDat._rEarDb4000)) ? "" : "4kHz(" + medexamDetDat._rEarDb4000 + ")";
                        final String rEar = "○ " + r1000Hz + " " + r4000Hz;
                        if (FORM_KNJF030A_1_8.equals(form)) {
                            _svf.VrsOut("R_EAR_DB", "○");
                            _svf.VrsOut("R_EAR",    r1000Hz);
                            _svf.VrsOut("R_EAR_2",  r4000Hz);
                        } else {
                            _svf.VrsOut("R_EAR", rEar);
                        }
                    }
                }
                if (_paramA.nameMstNamespare1Is1(db2, "F010", medexamDetDat._lEar)) {
                    printSlash("SLASH_L_EAR");
                } else {
                    if ("02".equals(medexamDetDat._lEar)) {
                        final String l1000Hz = (StringUtils.isEmpty(medexamDetDat._lEarDb))     ? "" : "1kHz(" + medexamDetDat._lEarDb +     ")";
                        final String l4000Hz = (StringUtils.isEmpty(medexamDetDat._lEarDb4000)) ? "" : "4kHz(" + medexamDetDat._lEarDb4000 + ")";
                        final String lEar = "○ " + l1000Hz + " " + l4000Hz;
                        if (FORM_KNJF030A_1_8.equals(form)) {
                            _svf.VrsOut("L_EAR_DB", "○");
                            _svf.VrsOut("L_EAR",    l1000Hz);
                            _svf.VrsOut("L_EAR_2",  l4000Hz);
                        } else {
                            _svf.VrsOut("L_EAR", lEar);
                        }
                    }
                }
            } else {
                _svf.VrsOut("R_EAR_DB", medexamDetDat._rEarDb);
                setName1WithSlash(db2, "R_EAR", "F010", medexamDetDat._rEar, "SLASH_R_EAR");
                _svf.VrsOut("L_EAR_DB", medexamDetDat._lEarDb);
                setName1WithSlash(db2, "L_EAR", "F010", medexamDetDat._lEar, "SLASH_L_EAR");
            }
        }

        // 耳鼻咽喉疾患
        if ("2".equals(_param._printKenkouSindanIppan)) {
            if (isSonota(db2, "F060", medexamDetDat._nosediseasecd)) {
                // その他はコード名称を表示しない
                setSonotaText3(
                        "NOSEDISEASECD_REMARK1_2", "NOSEDISEASECD_REMARK2_2", "NOSEDISEASECD_REMARK3_2",
                        30, "NOSEDISEASECD_REMARK1", "NOSEDISEASECD_REMARK2", "NOSEDISEASECD_REMARK3",
                        medexamDetDat._nosediseasecdRemark, medexamDetDat._nosediseasecdRemark2, medexamDetDat._nosediseasecdRemark3);
            } else {
                setName1WithSlash(db2, "NOSEDISEASE", "F060", medexamDetDat._nosediseasecd, "SLASH_NOSEDISEASE");
                _svf.VrsOut("NOSEDISEASECD_REMARK", getKakko(medexamDetDat._nosediseasecdRemark));
            }
        } else {
//                            setName1WithSlash("NOSEDISEASE", "F060", regdData._medexamDetDat._nosediseasecd, "SLASH_NOSEDISEASE");
//                            _svf.VrsOut("NOSEDISEASECD_REMARK", getKakko(regdData._medexamDetDat._nosediseasecdRemark));
            if (_paramA._isMusashinohigashi && _paramA.nosediseasesKeta > 0) {
                final String text = getNoseDisease2(db2, medexamDetDat);
                final int keta = getMS932ByteLength(text);
                if (keta <= _paramA.nosediseasesKeta) {
                    _svf.VrsOut("NOSEDISEASES", text);
                } else if (keta <= _paramA.nosediseases2Keta) {
                    _svf.VrsOut("NOSEDISEASES2", text);
                } else { // 16 * 4
                    _svf.VrsOut("NOSEDISEASES3", text);
                }
            } else {
                if (_paramA.nameMstNamespare1Is1(db2, "F060", medexamDetDat._nosediseasecd)) {
                    setName1WithSlash(db2, "NOSEDISEASE", "F060", medexamDetDat._nosediseasecd, "SLASH_NOSEDISEASE");
                } else {
                    final String nosedisease = getNoseDisease(db2, medexamDetDat);
                    final List token = KNJ_EditKinsoku.getTokenList(nosedisease, _paramA.nosediseaseKeta);
                    final String[] fields = {"NOSEDISEASE", "NOSEDISEASE2", "NOSEDISEASE3"};

                    for (int j = 0; j < Math.min(token.size(), fields.length); j++) {
                        _svf.VrsOut(fields[j], (String) token.get(j));
                    }
                }
            }
        }

        // 皮膚疾患
        if (isSonota(db2, "F070", medexamDetDat._skindiseasecd)) {
            // その他はコード名称を表示しない
            if (getMS932ByteLength(medexamDetDat._skindiseasecdRemark) > 20 && _paramA.skindisease2_1Keta > 20) {
                _svf.VrsOut("SKINDISEASE2_1", medexamDetDat._skindiseasecdRemark);
            } else {
                _svf.VrsOut("SKINDISEASE", medexamDetDat._skindiseasecdRemark);
            }
        } else {
            setName1WithSlash(db2, "SKINDISEASE", "F070", medexamDetDat._skindiseasecd, "SLASH_SKINDISEASE");
        }

        // 結核
        // 撮影日
        _svf.VrsOut("PHOTO_DATE", formatDate(db2, medexamDetDat._tbFilmdate));
        // 画像番号
        _svf.VrsOut("FILMNO", medexamDetDat._tbFilmno);
        // 所見
        setName1WithSlash(db2, "VIEWS1_1", "F100", medexamDetDat._tbRemarkcd, "SLASH_VIEWS1_1");
        if ((_param._isMusashinoHigashi || _param._isMiyagiken) && !_paramA.nameMstNamespare1Is1(db2, "F100", medexamDetDat._tbRemarkcd)) {
              final int xlen = KNJ_EditEdit.getMS932ByteLength(medexamDetDat._tbXRay);
               final String xfield = xlen > 20 ? "TB_X_RAY" : "VIEWS1_2";
            _svf.VrsOut(0 < _paramA.tbXRay1Keta ? "TB_X_RAY1" : xfield, (_param._isMusashinoHigashi ? medexamDetDat._tbXRay : getKakko(medexamDetDat._tbXRay)));
        }
        if ("2".equals(_param._printKenkouSindanIppan)) {
            // その他の検査
            if (isSonota(db2, "F110", medexamDetDat._tbOthertestcd)) {
                // その他はコード名称を表示しない
                _svf.VrsOut("OTHERS2_1", medexamDetDat._tbOthertestRemark1);
            } else {
                boolean printFlg = true;
                if (isGrade23(db2, _param._year, student._grade)) {
                    final String abbv2 = getName(db2, "F110", medexamDetDat._tbOthertestcd, "ABBV2");
                    if ("1".equals(abbv2)) {
                        printFlg = false;
                    }
                }
                if (printFlg) {
                    setName1WithSlash(db2, "OTHERS", "F110", medexamDetDat._tbOthertestcd, null);
                }
            }
            // 病名
            if (isSonota(db2, "F120", medexamDetDat._tbNamecd)) {
                // その他はコード名称を表示しない
                _svf.VrsOut("DISEASE_NAME2_1", medexamDetDat._tbNameRemark1);
            } else {
                boolean printFlg = true;
                if (isGrade23(db2, _param._year, student._grade)) {
                    final String abbv2 = getName(db2, "F120", medexamDetDat._tbNamecd, "ABBV2");
                    if ("1".equals(abbv2)) {
                        printFlg = false;
                    }
                }
                if (printFlg) {
                    setName1WithSlash(db2, "DISEASE_NAME", "F120", medexamDetDat._tbNamecd, "SLASH_DISEASE_NAME");
                }
            }
            // 指導区分
            if (isSonota(db2, "F130", medexamDetDat._tbAdvisecd)) {
                // その他はコード名称を表示しない
                _svf.VrsOut("GUIDANCE2_1", medexamDetDat._tbAdviseRemark1);
            } else {
                setName1WithSlash(db2, "GUIDANCE", "F130", medexamDetDat._tbAdvisecd, null);
            }
        } else {
            // その他の検査
            setName1WithSlash(db2, "OTHERS", "F110", medexamDetDat._tbOthertestcd, "SLASH_OTHERS");
            // 病名
            if (isSonota(db2, "F120", medexamDetDat._tbNamecd)) {
                // その他はコード名称を表示しない
                _svf.VrsOut("DISEASE_NAME2", medexamDetDat._tbNameRemark1);
            } else {
                final String slashDisease = (_param._isMiyagiken) ? "SLASH_DISEASE": "SLASH_DISEASE_NAME";
                setName1WithSlash(db2, "DISEASE_NAME", "F120", medexamDetDat._tbNamecd, slashDisease);
            }
            // 指導区分
            final String guidance = ("KINDAI".equals(_param._namemstZ010Name1)) ? "GUIDANCE_2" : "GUIDANCE";
            setName1WithSlash(db2, guidance, "F130", medexamDetDat._tbAdvisecd, null);
        }

        // 臨床医学検査(心電図)
        setHeartMedExam(db2, medexamDetDat._heartMedexamRemark, medexamDetDat._heartMedexam, form);
        // 疾病及び異常
        setHeartdiseasecd(db2, medexamDetDat._heartdiseasecdRemark, medexamDetDat._heartdiseasecd, form);
        if("KINDAI".equals(_param._namemstZ010Name1)) {
            setName1WithSlash(db2, "MANAGEMENT_2", "F091", medexamDetDat._managementDiv, "SLASH_MANAGEMENT");
            if(!_paramA.nameMstNamespare1Is1(db2, "F091", medexamDetDat._managementDiv)){
                _svf.VrsOut("MANAGEMENT2", medexamDetDat._managementRemark);
            }
        } else {
            _svf.VrsOut("MANAGEMENT", medexamDetDat._managementDivName);
            _svf.VrsOut("MANAGEMENT2", medexamDetDat._managementRemark);
        }
        _svf.VrsOut("BMI", medexamDetDat._bmi);
        setName1WithSlash(db2, "ALBUMINURIA", "F020", medexamDetDat._albuminuria1cd, null);
        setName1WithSlash(db2, "URICSUGAR", "F019", medexamDetDat._uricsugar1cd, null);
        setName1WithSlash(db2, "URICBLEED", "F018", medexamDetDat._uricbleed1cd, null);
        setName1WithSlash(db2, "ALBUMINURIA2", "F020", medexamDetDat._albuminuria2cd, null);
        setName1WithSlash(db2, "URICSUGAR2", "F019", medexamDetDat._uricsugar2cd, null);
        setName1WithSlash(db2, "URICBLEED2", "F018", medexamDetDat._uricbleed2cd, null);
        if ("2".equals(_param._printKenkouSindanIppan)) {
            final String urineOthers = getName(db2, "F022", medexamDetDat._uricothertestCd, "NAME1");
            _svf.VrsOut(getMS932ByteLength(urineOthers) > 20 ? "URINE_OTHERS1" : "URINE_OTHERS", urineOthers);
        } else {
            if ("KINDAI".equals(_param._namemstZ010Name1) || "KINJUNIOR".equals(_param._namemstZ010Name1)) {
                //近大or近大中なら指導区分を出力
                setName1WithSlash(db2, "URINE_OTHERS", "F021", medexamDetDat._uriAdvisecd, "SLASH_URINE_OTHERS");
            } else {
                _svf.VrsOut("URINE_OTHERS" + (getMS932ByteLength(medexamDetDat._uricothertest) > 20 ? "1" : ""), medexamDetDat._uricothertest);
            }
        }
        setName1WithSlash(db2, "URI_ADVISECD", "F021", medexamDetDat._uriAdvisecd, null);
        _svf.VrsOut("ANEMIA", medexamDetDat._anemiaRemark);
        _svf.VrsOut("HEMOGLOBIN", medexamDetDat._hemoglobin);
        setName("PARASITE", "F023", medexamDetDat._parasite, "NAME1", 0, "", "");
        // その他の疾病及び異常
        if ("2".equals(_param._printKenkouSindanIppan)) {
            if (isSonota(db2, "F140", medexamDetDat._otherdiseasecd)) {
                setSonotaText3(
                        "OTHERDISEASE_REMARK1_2", "OTHERDISEASE_REMARK2_2", "OTHERDISEASE_REMARK3_2",
                        30, "OTHERDISEASE_REMARK1", "OTHERDISEASE_REMARK2", "OTHERDISEASE_REMARK3",
                        medexamDetDat._otherRemark, medexamDetDat._otherRemark2, medexamDetDat._otherRemark3);
            } else {
                setName1WithSlash(db2, "OTHERDISEASE", "F140", medexamDetDat._otherdiseasecd, "SLASH_OTHERDISEASE");
            }
        } else {
            if (isSonota(db2, "F140", medexamDetDat._otherdiseasecd)) {
                // その他はコード名称を表示しない
                _svf.VrsOut("OTHERDISEASE2", medexamDetDat._otherRemark2);
            } else {
                setName1WithSlash(db2, "OTHERDISEASE", "F140", medexamDetDat._otherdiseasecd, "SLASH_OTHERDISEASE");
            }
        }
        if ("1".equals(_param._printKenkouSindanIppan)) {
            if("KINDAI".equals(_param._namemstZ010Name1)) {
                setName1WithSlash(db2, "OTHER_ADVISE_2", "F145", medexamDetDat._otherAdvisecd, "SLASH_OTHER_ADVISE");
                if(!_paramA.nameMstNamespare1Is1(db2, "F145", medexamDetDat._otherAdvisecd)){
                    _svf.VrsOut("OTHER_ADVISECD2", medexamDetDat._otherRemark);
                }
            } else {
                setName1WithSlash(db2, "OTHER_ADVISE", "F145", medexamDetDat._otherAdvisecd, null);
                _svf.VrsOut("OTHER_ADVISECD2", medexamDetDat._otherRemark);
            }
        }

        if (null != medexamDetDat._docCd) {
            setName1WithSlash(db2, "VIEWS2_1", "F144", medexamDetDat._docCd, null);
        } else if (null != _paramA._yearF242name1Map.get(regdData._year) && null != medexamDetDat._docDate) {
            _svf.VrsOut("VIEWS2_1", (String) _paramA._yearF242name1Map.get(regdData._year));
        } else if (getMS932ByteLength(medexamDetDat._docRemark) <= _paramA.views2_1Keta) {
            _svf.VrsOut("VIEWS2_1", medexamDetDat._docRemark);
        } else {
            _svf.VrsOut("VIEWS2_2", medexamDetDat._docRemark);
        }
        // 学校医
        _svf.VrsOut("DOC_REMARK", getKakko(medexamDetDat._docRemark));
        // 月日
        _svf.VrsOut("DOC_DATE", formatDate(db2, medexamDetDat._docDate));
        if (null != medexamDetDat._docRemark && null != medexamDetDat._docDate && null != _param._printStamp) {
            _svf.VrsOut("STAFFBTMC", _param.getStampImageFile(regdData._year));
        }
        // 事後措置
        if ("2".equals(_param._printKenkouSindanIppan)) {
            if (isSonota(db2, "F150", medexamDetDat._treatcd)) {
                setSonotaText3(
                        "DOC_TREAT_REMARK1_2", "DOC_TREAT_REMARK2_2", "DOC_TREAT_REMARK3_2",
                        30, "DOC_TREAT_REMARK1", "DOC_TREAT_REMARK2", "DOC_TREAT_REMARK3",
                        medexamDetDat._treatRemark1, medexamDetDat._treatRemark2, medexamDetDat._treatRemark3);
            } else {
                final String docTreat1 = ("KINDAI".equals(_param._namemstZ010Name1)) ? "DOC_TREAT1_2" : "DOC_TREAT1";
                setName1WithSlash(db2, docTreat1, "F150", medexamDetDat._treatcd, "SLASH_DOC_TREAT");
            }
        } else {
            if (isSonota(db2, "F150", medexamDetDat._treatcd)) {
                final String setTreatStr = getName(db2, "F151", medexamDetDat._treatcd2, "NAME1") + " " + medexamDetDat._treatRemark1;
                final List token = KNJ_EditKinsoku.getTokenList(setTreatStr, _paramA.docTreatKeta);
                final String docTreat1 = ("KINDAI".equals(_param._namemstZ010Name1)) ? "DOC_TREAT1_2" : "DOC_TREAT1";
                final String docTreat2 = ("KINDAI".equals(_param._namemstZ010Name1)) ? "DOC_TREAT2_2" : "DOC_TREAT2";
                final String[] fields = {docTreat1, docTreat2};

                for (int j = 0; j < Math.min(token.size(), fields.length); j++) {
                    _svf.VrsOut(fields[j], (String) token.get(j));
                }
            } else {
                final String docTreat1 = ("KINDAI".equals(_param._namemstZ010Name1)) ? "DOC_TREAT1_2" : "DOC_TREAT1";
                final String docTreat2 = ("KINDAI".equals(_param._namemstZ010Name1)) ? "DOC_TREAT2_2" : "DOC_TREAT2";
                setName1WithSlash(db2, docTreat1, "F150", medexamDetDat._treatcd, "SLASH_DOC_TREAT");
                setName1WithSlash(db2, docTreat2, "F151", medexamDetDat._treatcd2, "SLASH_DOC_TREAT");
            }
        }
        // 備考
        int maxNoteKeta;
        if (_paramA.note1Keta1Only > 0) {
            maxNoteKeta = _paramA.note1Keta1Only;
        } else {
            maxNoteKeta = FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form) || FORM_KNJF030A_1_8.equals(form) ? 14 : 20;
        }
        String sports = "";
        if("KINDAI".equals(_param._namemstZ010Name1) && "1".equals(getSchregBaseDetail(db2, student._schregno, "008","BASE_REMARK1"))) {
            sports = "独立行政法人日本スポーツ振興センター加入済み";
            sports += StringUtils.repeat(" ", maxNoteKeta - getMS932ByteLength(sports));
        }
        String zakou = "";
        if (null != medexamDetDat._sitheight) {
            zakou = "座高（" + medexamDetDat._sitheight + "cm）";
            zakou += StringUtils.repeat(" ", maxNoteKeta - getMS932ByteLength(zakou));
        }
        String paras = "";
        if (_param._isMusashinoHigashi && null != getName("F023", medexamDetDat._parasite, "NAME1")) {
            paras = getName("F023", medexamDetDat._parasite, "NAME1");
            paras += StringUtils.repeat(" ", maxNoteKeta - getMS932ByteLength(paras));
        }
        String note1 = sports + zakou + paras + StringUtils.defaultString(medexamDetDat._remark);
        String noteField = "NOTE1";
        if (getMS932ByteLength(note1) > _paramA.note1Keta && _paramA.note2_1Keta > 0) { // 文字数多いフィールドに切替
            noteField = "NOTE2_1";
            sports = "";
            if("KINDAI".equals(_param._namemstZ010Name1) && "1".equals(getSchregBaseDetail(db2, student._schregno, "008","BASE_REMARK1"))) {
                sports = "独立行政法人日本スポーツ振興センター加入済み";
                sports += StringUtils.repeat(" ", _paramA.note2_1Keta1Only - getMS932ByteLength(sports));
            }
            zakou = "";
            if (null != medexamDetDat._sitheight) {
                zakou = "座高（" + medexamDetDat._sitheight + "cm）";
                zakou += StringUtils.repeat(" ", _paramA.note2_1Keta1Only - getMS932ByteLength(zakou));
            }
            paras = "";
            if (_param._isMusashinoHigashi && null != getName("F023", medexamDetDat._parasite, "NAME1")) {
                paras = getName("F023", medexamDetDat._parasite, "NAME1");
                paras += StringUtils.repeat(" ", _paramA.note2_1Keta1Only - getMS932ByteLength(paras));
            }
            note1 = sports + zakou + paras + StringUtils.defaultString(medexamDetDat._remark);
        }
        _svf.VrsOut(noteField,  note1);
    }

    private String getForm(final String schoolKind) {
        final String form;
        if (_paramA._isChiyodaKudan) {
            form = "KNJF030A_1_6G.frm";
        } else if ("JH".equals(schoolKind)) {
            form = "KNJF030A_1_6.frm";
        } else if ("PJ".equals(schoolKind) && ("1".equals(_param._useParasite_P) || "1".equals(_param._useParasite_J))) {
            form = FORM_KNJF030A_1P_5;
        } else if ("PJ".equals(schoolKind)) {
            form = (_param._isMiyagiken) ? FORM_KNJF030A_1_8: FORM_KNJF030A_1_5;
        } else if (("K".equals(schoolKind) || "P".equals(schoolKind)) && "1".equals(_param._useParasite_P)) {
            form = "KNJF030A_1P_2.frm";
        } else if (("K".equals(schoolKind) || "P".equals(schoolKind))) {
            form = "KNJF030A_1_2.frm";
        } else if ("J".equals(schoolKind) && "1".equals(_param._useParasite_J)) {
            form =  "KNJF030A_1PJ.frm";
        } else if ("J".equals(schoolKind)) {
            form = (_param._isMiyagiken) ? "KNJF030A_1_7J.frm": "KNJF030A_1J.frm";
        } else if ("H".equals(schoolKind) && "1".equals(_param._useParasite_H)) {
            if ("2".equals(_param._printKenkouSindanIppan)) {
                form = F_KENKOU_SINDAN_IPPAN_A_PKUMA;
            } else if ("3".equals(_param._printKenkouSindanIppan)) {
                form = F_KENKOU_SINDAN_IPPAN_A_P4;
            } else {
                if ("1".equals(_param._useForm5_H_Ippan)) {
                    form = FORM_KNJF030A_1P_5G;
                } else {
                    form = F_KENKOU_SINDAN_IPPAN_A_P;
                }
            }
        } else if ("2".equals(_param._printKenkouSindanIppan)) {
            form = F_KENKOU_SINDAN_IPPAN_A_KUMA;
        } else if ("3".equals(_param._printKenkouSindanIppan)) {
            form = F_KENKOU_SINDAN_IPPAN_A_4;
        } else {
            if ("1".equals(_param._useForm5_H_Ippan)) {
                form = FORM_KNJF030A_1_5G;
            } else {
                form = (_param._isMiyagiken) ? "KNJF030A_1_7.frm": (_param._isTokiwa) ? FORM_KNJF030A_1_9: F_KENKOU_SINDAN_IPPAN_A;
            }
        }
        return form;
    }

    /**
     * data1、data2、data3が全て文字の大きい各フィールドに収まるならすべて文字の大きい各フィールドに印字する<br>
     * それ以外はすべて文字の小さい各フィールドに印字する
     * @param field1_1
     * @param field2_1
     * @param field3_1
     * @param keta
     * @param field1_2
     * @param field2_2
     * @param field3_2
     * @param data1
     * @param data2
     * @param data3
     */
    private void setSonotaText3(final String field1_1, final String field2_1, final String field3_1, final int keta,
            final String field1_2, final String field2_2, final String field3_2,
            final String data1, final String data2, final String data3) {
        final String[] field_1 = new String[] {field1_1, field2_1, field3_1};
        final String[] field_2 = new String[] {field1_2, field2_2, field3_2};
        final String[] data = new String[] {data1, data2, data3};

        if (0 < keta && keta < getMaxMS932ByteLength(data)) {
            svfVrsOutArray(field_2, data); // 文字の大きいフィールド
        } else {
            svfVrsOutArray(field_1, data); // 文字の小さいフィールド
        }
    }

    private void printSlash(final String field) {
        final String slashStr  = (_param._isMiyagiken || _param._isKumamoto) ? "slash_bs.jpg": "slash.jpg";
        final String slashFile = _param.getImageFile(slashStr);
        if (null != slashFile) {
            _svf.VrsOut(field, slashFile);
        }
    }

    private void setHeartMedExam(final DB2UDB db2, final String heartMedexamRemark, final String heartMedexam, final String form) throws SQLException {
//        log.debug(" heartMedexamRemark = [" + heartMedexamRemark + "] keta = [" + getMS932ByteLength(heartMedexamRemark) + "], heartMedexam = " + heartMedexam);
        if ("1".equals(_param._useKnjf030AHeartBiko)) {
            if (null == heartMedexamRemark) {
                setName1WithSlash(db2, "HEART_MEDEXAM", "F080", heartMedexam, "SLASH_HEART_MEDEXAM");
            } else {
                final int remarklen = getMS932ByteLength(heartMedexamRemark);
                final int[] fieldlen;
                final String[] fieldname;
                final int ketaHEART_MEDEXAM;
                final int ketaHEART_MEDEXAM_2;
                if (FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form) || FORM_KNJF030A_1_8.equals(form)) {
                    ketaHEART_MEDEXAM = 12 + 12; // リンクフィールド
                    ketaHEART_MEDEXAM_2 = 11 + 11; // リンクフィールド
                    final int ketaHEART_MEDEXAM2_2 = 21 * 2; // リンクフィールド
                    final int ketaHEART_MEDEXAM2_3 = 21 * 2; // リンクフィールド
                    if (remarklen <= ketaHEART_MEDEXAM + ketaHEART_MEDEXAM_2) {
                        fieldlen = new int[] {ketaHEART_MEDEXAM, ketaHEART_MEDEXAM_2};
                        fieldname = new String[] {"HEART_MEDEXAM", "HEART_MEDEXAM_2"};
                    } else {
                        fieldlen = new int[] {ketaHEART_MEDEXAM2_2, ketaHEART_MEDEXAM2_3};
                        fieldname = new String[] {"HEART_MEDEXAM2_2", "HEART_MEDEXAM2_3"};
                    }
                    svfVrsOutArray(fieldname, getMS932ByteToken(heartMedexamRemark, fieldlen));
                } else {
                    ketaHEART_MEDEXAM = 20;
                    ketaHEART_MEDEXAM_2 = 22;
                    final int ketaHEART_MEDEXAM2_1 = 26;
                    final int ketaHEART_MEDEXAM2_2 = 26;
                    final int ketaHEART_MEDEXAM2_3 = 26;
                    if (remarklen <= ketaHEART_MEDEXAM + ketaHEART_MEDEXAM_2) {
                        fieldlen = new int[] {ketaHEART_MEDEXAM, ketaHEART_MEDEXAM_2};
                        fieldname = new String[] {"HEART_MEDEXAM", "HEART_MEDEXAM_2"};
                    } else if (remarklen <= ketaHEART_MEDEXAM2_1 + ketaHEART_MEDEXAM2_2 + ketaHEART_MEDEXAM2_3) {
                        fieldlen = new int[] {ketaHEART_MEDEXAM2_1, ketaHEART_MEDEXAM2_2, ketaHEART_MEDEXAM2_3};
                        fieldname = new String[] {"HEART_MEDEXAM2_1", "HEART_MEDEXAM2_2", "HEART_MEDEXAM2_3"};
                    } else {
                        fieldlen = null;
                        fieldname = null;
                    }
                    if (null != fieldlen && null != fieldname) {
                        svfVrsOutArray(fieldname, getMS932ByteToken(heartMedexamRemark, fieldlen));
                    } else {
                        _svf.VrsOut("HEARTDISEASECD_REMARK", heartMedexamRemark); // 42 * 2
                    }
                }
            }
        } else {
            final String remark = _param._isMusashinoHigashi ? StringUtils.defaultString(heartMedexamRemark) : getKakko(heartMedexamRemark);
            final int remarklen = getMS932ByteLength(remark);
            // log.debug(" heartmedexam remarklen = " + remarklen);
            final int[] fieldlen;
            final String[] fieldname;
            final int ketaHEART_MEDEXAM_2;
            if (FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form) || FORM_KNJF030A_1_8.equals(form)) {
                setName1WithSlash(db2, "HEART_MEDEXAM", "F080", heartMedexam, "SLASH_HEART_MEDEXAM");
                ketaHEART_MEDEXAM_2 = 11 + 11; // リンクフィールド
                final int ketaHEART_MEDEXAM2_2 = 21 * 2; // リンクフィールド
                final int ketaHEART_MEDEXAM2_3 = 21 * 2; // リンクフィールド
                if (remarklen <= ketaHEART_MEDEXAM_2) {
                    fieldlen = new int[] {ketaHEART_MEDEXAM_2};
                    fieldname = new String[] {"HEART_MEDEXAM_2"};
                } else {
                    fieldlen = new int[] {ketaHEART_MEDEXAM2_2, ketaHEART_MEDEXAM2_3};
                    fieldname = new String[] {"HEART_MEDEXAM2_2", "HEART_MEDEXAM2_3"};
                }
                svfVrsOutArray(fieldname, getMS932ByteToken(heartMedexamRemark, fieldlen));
            } else {
                final String cdfield;
                if (remarklen <= 22) {
                    cdfield = "HEART_MEDEXAM";
                    fieldlen = new int[] {22};
                    fieldname = new String[] {"HEART_MEDEXAM_2"};
                } else if (remarklen <= 26 * 2) {
                    cdfield = "HEART_MEDEXAM2_1";
                    fieldlen = new int[] {26, 26};
                    fieldname = new String[] {"HEART_MEDEXAM2_2", "HEART_MEDEXAM2_3"};
                } else {
                    cdfield = "HEART_MEDEXAM2_1";
                    fieldlen = null;
                    fieldname = null;
                }
                setName1WithSlash(db2, cdfield, "F080", heartMedexam, "SLASH_HEART_MEDEXAM");
                if (null != fieldlen && null != fieldname) {
                    svfVrsOutArray(fieldname, getMS932ByteToken(remark, fieldlen));
                } else {
                    _svf.VrsOut("HEARTDISEASECD_REMARK", remark); // 42 * 2
                }
            }
        }
    }

    private void setHeartdiseasecd(final DB2UDB db2, final String heartdiseasecdRemark, final String heartdiseasecd, final String form) throws SQLException {
//        log.debug(" heartdiseasecdRemark = [" + heartdiseasecdRemark + "] keta = [" + getMS932ByteLength(heartdiseasecdRemark) + "], heartdiseasecd = " + heartdiseasecd);
        if ("2".equals(_param._printKenkouSindanIppan)) {
            if (FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form) || FORM_KNJF030A_1_8.equals(form)) {
                if (isSonota(db2, "F090", heartdiseasecd)) {
                    final int ketaHEARTDISEASE1 = 12 + 12; // リンクフィールド
                    final int ketaHEARTDISEASE1_2 = 11 + 11; // リンクフィールド
                    final int[] fieldlen = new int[] {ketaHEARTDISEASE1, ketaHEARTDISEASE1_2};
                    final String[] fieldname = new String[] {"HEARTDISEASE1", "HEARTDISEASE1_2"};
                    svfVrsOutArray(fieldname, getMS932ByteToken(heartdiseasecdRemark, fieldlen));
                } else {
                    setName1WithSlash(db2, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                }
            } else {
                if (isSonota(db2, "F090", heartdiseasecd)) {
                    _svf.VrsOut("HEARTDISEASE_REMARK1", heartdiseasecdRemark);
                } else {
                    setName1WithSlash(db2, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                }
            }
        } else if ("1".equals(_param._useKnjf030AHeartBiko)) {
            if (null == heartdiseasecdRemark) {
                setName1WithSlash(db2, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
            } else {
                final int remarklen = getMS932ByteLength(heartdiseasecdRemark);
                final int ketaHEARTDISEASE1;
                final int ketaHEARTDISEASE1_2;
                if (FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form) || FORM_KNJF030A_1_8.equals(form)) {
                    ketaHEARTDISEASE1 = 12 + 12; // リンクフィールド
                    ketaHEARTDISEASE1_2 = 11 + 11; // リンクフィールド
                    final int[] fieldlen = new int[] {ketaHEARTDISEASE1, ketaHEARTDISEASE1_2};
                    final String[] fieldname = new String[] {"HEARTDISEASE1", "HEARTDISEASE1_2"};
                    svfVrsOutArray(fieldname, getMS932ByteToken(heartdiseasecdRemark, fieldlen));
                } else {
                    ketaHEARTDISEASE1 = 20;
                    ketaHEARTDISEASE1_2 = 22;
                    final int ketaHEARTDISEASE2_1 = 26;
                    final int ketaHEARTDISEASE2_2 = 26;
                    final int ketaHEARTDISEASE2_3 = 26;
                    final int[] fieldlen;
                    final String[] fieldname;
                    if (remarklen <= ketaHEARTDISEASE1 + ketaHEARTDISEASE1_2) {
                        fieldlen = new int[] {ketaHEARTDISEASE1, ketaHEARTDISEASE1_2};
                        fieldname = new String[] {"HEARTDISEASE1", "HEARTDISEASE1_2"};
                    } else if (remarklen <= ketaHEARTDISEASE2_1 + ketaHEARTDISEASE2_2 + ketaHEARTDISEASE2_3) {
                        fieldlen = new int[] {ketaHEARTDISEASE2_1, ketaHEARTDISEASE2_2, ketaHEARTDISEASE2_3};
                        fieldname = new String[] {"HEARTDISEASE2_1", "HEARTDISEASE2_2", "HEARTDISEASE2_3"};
                    } else {
                        fieldlen = null;
                        fieldname = null;
                    }
                    if (null != fieldlen && null != fieldname) {
                        svfVrsOutArray(fieldname, getMS932ByteToken(heartdiseasecdRemark, fieldlen));
                    } else {
                        _svf.VrsOut("HEARTDISEASE2", heartdiseasecdRemark); // 42
                    }
                }
            }
        } else {
            final String kakkoHeartdiseasecdRemark = getKakko(heartdiseasecdRemark);
            final int remarklen = getMS932ByteLength(kakkoHeartdiseasecdRemark);
            final int ketaHEARTDISEASE1;
            final int ketaHEARTDISEASE1_2;
            final int[] fieldlen;
            if (FORM_KNJF030A_1_5.equals(form) || FORM_KNJF030A_1P_5.equals(form) || FORM_KNJF030A_1_8.equals(form)) {
                ketaHEARTDISEASE1 = 12 + 12; // リンクフィールド
                ketaHEARTDISEASE1_2 = 11 + 11; // リンクフィールド
                final int ketaHEARTDISEASE2_2 = 21 + 21; // リンクフィールド
                setName1WithSlash(db2, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                if (remarklen <= ketaHEARTDISEASE1_2) {
                    fieldlen = new int[] {ketaHEARTDISEASE1_2};
                    svfVrsOutArray(new String[] {"HEARTDISEASE1_2"}, getMS932ByteToken(kakkoHeartdiseasecdRemark, fieldlen));
                } else {
                    fieldlen = new int[] {ketaHEARTDISEASE2_2};
                    svfVrsOutArray(new String[] {"HEARTDISEASE2_2"}, getMS932ByteToken(kakkoHeartdiseasecdRemark, fieldlen));
                }
            } else {
                ketaHEARTDISEASE1 = 22;
                final int ketaHEARTDISEASE2_2 = 26;
                final int ketaHEARTDISEASE2_3 = 26;
                if (remarklen <= ketaHEARTDISEASE1) {
                    setName1WithSlash(db2, "HEARTDISEASE1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                    fieldlen = new int[] {ketaHEARTDISEASE1};
                    svfVrsOutArray(new String[] {"HEARTDISEASE1_2"}, getMS932ByteToken(kakkoHeartdiseasecdRemark, fieldlen));
                } else {
                    setName1WithSlash(db2, "HEARTDISEASE2_1", "F090", heartdiseasecd, "SLASH_HEARTDISEASE");
                    if (remarklen <= ketaHEARTDISEASE2_2 + ketaHEARTDISEASE2_3) {
                        fieldlen = new int[] {ketaHEARTDISEASE2_2, ketaHEARTDISEASE2_3};
                        svfVrsOutArray(new String[] {"HEARTDISEASE2_2", "HEARTDISEASE2_3"}, getMS932ByteToken(kakkoHeartdiseasecdRemark, fieldlen));
                    } else {
                        _svf.VrsOut("HEARTDISEASE2", kakkoHeartdiseasecdRemark); // 42
                    }
                }
            }
        }
    }

    private boolean isGrade23(
            final DB2UDB db2,
            final String year,
            final String grade
            ) throws SQLException {
        return _paramA.isGrade23(db2, year, grade);
    }

    /**
     * コードが「その他」（名称マスタの予備2が'1'）ならtrue、それ以外はfalse
     */
    private boolean isSonota(
            final DB2UDB db2,
            final String nameCd1,
            final String nameCd2
    ) throws SQLException {
        return _paramA.nameMstNamespare2Is1(db2, nameCd1, nameCd2);
    }

    private void setName1WithSlash(
            final DB2UDB db2,
            final String fieldName,
            final String nameCd1,
            final String nameCd2,
            final String slashField
    ) throws SQLException {
        if (null != slashField && _paramA.nameMstNamespare1Is1(db2, nameCd1, nameCd2)) {
            printSlash(slashField);
        } else if ("1".equals(_param._kenkouSindanIppanNotPrintNameMstComboNamespare2Is1) && _paramA.nameMstNamespare2Is1(db2, nameCd1, nameCd2)) {
            if (_paramA._isOutputDebug) {
                log.info(" 印字なし(NAMESPARE2=1): NAME_MST." + nameCd1 + "." + nameCd2);
            }
        } else {
            _svf.VrsOut(fieldName, getName(db2, nameCd1, nameCd2, "NAME1"));
        }
    }

    private void setNameWithSlash(
            final DB2UDB db2,
            final String fieldName,
            final String nameCd1,
            final String nameCd2,
            final String plusString,
            final String slashField
    ) throws SQLException {
        if (null != slashField && _paramA.nameMstNamespare1Is1(db2, nameCd1, nameCd2)) {
            printSlash(slashField);
        } else {
            _svf.VrsOut(fieldName, getNamePlusString(db2, nameCd1, nameCd2, "NAME1", plusString));
        }
    }

    private String getNamePlusString(final DB2UDB db2, final String nameCd1, final String nameCd2, final String useFieldName, final String plusString) throws SQLException {
        String retStr = "";
        if (null == nameCd2) {
        } else if ("1".equals(_param._kenkouSindanIppanNotPrintNameMstComboNamespare2Is1) && _paramA.nameMstNamespare2Is1(db2, nameCd1, nameCd2)) {
            if (_paramA._isOutputDebug) {
                log.info(" 印字なし(NAMESPARE2=1): NAME_MST." + nameCd1 + "." + nameCd2);
            }
        } else {
            final String nameMstKey = _paramA.nameMstKey(nameCd1, nameCd2, useFieldName);
            if (_paramA._dataCache.containsKey(nameMstKey)) {
                final String val = (String) _paramA._dataCache.get(nameMstKey);
                return val + StringUtils.defaultString(plusString);
            }
            final String sql = "SELECT " + useFieldName + " FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
            ResultSet rs = null;
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String val = StringUtils.defaultString(rs.getString(useFieldName));
                    retStr = val + StringUtils.defaultString(plusString);
                    _paramA._dataCache.put(nameMstKey, val);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return retStr;
    }

    private String getName(final DB2UDB db2, final String nameCd1, final String nameCd2, final String useFieldName) throws SQLException {
        return getNamePlusString(db2, nameCd1, nameCd2, useFieldName, "");
    }

    private String getNoseDisease(final DB2UDB db2, final MedexamDetDat medexamDetDat) throws SQLException {
        String noseDisease = concatWith(new String[] {
                getTextOrName(medexamDetDat._nosediseasecdRemark1, db2, "F061", medexamDetDat._nosediseasecd5, "NAME1")
              , getTextOrName(medexamDetDat._nosediseasecdRemark2, db2, "F062", medexamDetDat._nosediseasecd6, "NAME1")
              , getTextOrName(medexamDetDat._nosediseasecdRemark3, db2, "F063", medexamDetDat._nosediseasecd7, "NAME1")
              , medexamDetDat._nosediseasecdRemark
        }, "、");
        if (StringUtils.isEmpty(noseDisease)) {
            noseDisease = getName(db2, "F060", medexamDetDat._nosediseasecd, "NAME1");
        }
        return noseDisease;
    }

    /**
     * textがnullでなければtext、nullなら名称マスタの名称フィールドの値を返す
     * @param text
     * @param db2
     * @param nameCd1 名称マスタ名称コード1
     * @param nameCd2 名称マスタ名称コード2
     * @param useFieldName 名称マスタの名称フィールド
     * @return textがnullでなければtext、nullなら名称マスタの名称フィールドの値
     * @throws SQLException
     */
    private String getTextOrName(final String text, final DB2UDB db2, final String nameCd1, final String nameCd2, final String useFieldName) throws SQLException {
        if (null != text && text.length() > 0) {
            return text;
        }
        return getName(db2, nameCd1, nameCd2, useFieldName);
    }

    private String getNoseDisease2(final DB2UDB db2, final MedexamDetDat medexamDetDat) throws SQLException {
        String noseDisease = concatWith(new String[] {
                getNamePlusString(db2, "F060", medexamDetDat._nosediseasecd, "NAME1", "") + StringUtils.defaultString(medexamDetDat._nosediseasecdRemark)
              , getNamePlusString(db2, "F061", medexamDetDat._nosediseasecd5, "NAME1", "") + StringUtils.defaultString(medexamDetDat._nosediseasecdRemark1)
              , getNamePlusString(db2, "F062", medexamDetDat._nosediseasecd6, "NAME1", "") + StringUtils.defaultString(medexamDetDat._nosediseasecdRemark2)
              , getNamePlusString(db2, "F063", medexamDetDat._nosediseasecd7, "NAME1", "") + StringUtils.defaultString(medexamDetDat._nosediseasecdRemark3)
        }, "、");
        return noseDisease;
    }

    private static class RegdData {
        final String _schregNo;
        final String _year;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _hrClassName;
        final String _gradeName;
        final String _gradeCd;
        final String _age;
        final String _birthDay;
        final String _sexCd;
        final String _hasData;
        final String _entYearGradeCd;
        final String _schoolKind;
        MedexamDetDat _medexamDetDat = null;

        /**
         * コンストラクタ。
         */
        public RegdData(
                final String schregNo,
                final String year,
                final String semester,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String hrClassName,
                final String gradeName,
                final String gradeCd,
                final String age,
                final String birthDay,
                final String sexCd,
                final String hasData,
                final String entYearGradeCd,
                final String schoolKind
        ) {
            _schregNo = schregNo;
            _year = year;
            _semester = semester;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _hrClassName = hrClassName;
            _gradeName = gradeName;
            _gradeCd = gradeCd;
            _age = age;
            _birthDay = birthDay;
            _sexCd = sexCd;
            _hasData = hasData;
            _entYearGradeCd = entYearGradeCd;
            _schoolKind = schoolKind;
        }

        private static List getRegdDataList(final DB2UDB db2, final Student student, final String gradeFlg, final Param param) throws SQLException {
            final List rtnList = new ArrayList();
            final String regdSql = getRegdSql(student, gradeFlg, param);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(regdSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo = rs.getString("SCHREGNO");
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrClassName = rs.getString("HR_CLASS_NAME1");
                    final String gradeName = rs.getString("GRADE_NAME1");
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String age = rs.getString("AGE");
                    final String birthDay = rs.getString("BIRTHDAY");
                    final String sexCd = rs.getString("SEX_CD");
                    final String hasData = rs.getString("HAS_DATA");
                    final String entYearGradeCd = rs.getString("ENT_YEAR_GRADE_CD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final RegdData regdData = new RegdData(schregNo, year, semester, grade, hrClass, attendNo,
                            hrName, hrClassName, gradeName, gradeCd, age, birthDay, sexCd, hasData, entYearGradeCd, schoolKind);
                    rtnList.add(regdData);
                }
                for (final Iterator it = rtnList.iterator(); it.hasNext();) {
                    final RegdData regdData = (RegdData) it.next();

                    regdData._medexamDetDat = new MedexamDetDat(db2, regdData._year, regdData._schregNo, param._printKenkouSindanIppan);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnList;
        }

        private static String getRegdSql(final Student student, final String schoolKinds, final Param param) {

            final StringBuffer schoolKindIn = new StringBuffer();
            schoolKindIn.append(" IN ('");
            for (int i = 0; i < schoolKinds.length(); i++) {
                schoolKindIn.append(schoolKinds.charAt(i)).append("', '");
            }
            schoolKindIn.append("')");

            final StringBuffer stb = new StringBuffer();
            //在籍（現在年度）
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            stb.append("        YEAR, ");
            stb.append("        SEMESTER, ");
            stb.append("        GRADE, ");
            stb.append("        HR_CLASS, ");
            stb.append("        ATTENDNO ");
            stb.append("    FROM ");
            stb.append("        SCHREG_REGD_DAT ");
            stb.append("    WHERE ");
            stb.append("        YEAR = '" + param._year + "' ");
            stb.append("        AND SEMESTER = '" + param._semester + "' ");
            stb.append("        AND SCHREGNO = '" + student._schregno + "' ");
            stb.append("    ) ");
            //現在年度以外の学期を取得
            stb.append(",SCHNO_MIN AS ( ");
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            stb.append("        YEAR, ");
            stb.append("        MIN(SEMESTER) AS SEMESTER ");
            stb.append("    FROM ");
            stb.append("        SCHREG_REGD_DAT W1 ");
            stb.append("    WHERE ");
            stb.append("        EXISTS( ");
            stb.append("            SELECT ");
            stb.append("                'X' ");
            stb.append("            FROM ");
            stb.append("                SCHNO W2 ");
            stb.append("            WHERE ");
            stb.append("                W2.SCHREGNO = W1.SCHREGNO ");
            stb.append("                AND W2.YEAR <> W1.YEAR ");
            stb.append("        ) ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ");
            stb.append("        YEAR ");
            stb.append("    ) ");
            //在籍（現在年度以外）
            stb.append(",SCHNO_ALL AS ( ");
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            stb.append("        YEAR, ");
            stb.append("        SEMESTER, ");
            stb.append("        GRADE, ");
            stb.append("        HR_CLASS, ");
            stb.append("        ATTENDNO ");
            stb.append("    FROM ");
            stb.append("        SCHREG_REGD_DAT W1 ");
            stb.append("    WHERE ");
            stb.append("        EXISTS( ");
            stb.append("            SELECT ");
            stb.append("                'X' ");
            stb.append("            FROM ");
            stb.append("                SCHNO_MIN W2 ");
            stb.append("            WHERE ");
            stb.append("                W2.SCHREGNO = W1.SCHREGNO ");
            stb.append("                AND W2.YEAR = W1.YEAR ");
            stb.append("                AND W2.SEMESTER=W1.SEMESTER ");
            stb.append("        ) ");
            stb.append("    UNION ");
            stb.append("    SELECT ");
            stb.append("        SCHREGNO, ");
            stb.append("        YEAR, ");
            stb.append("        SEMESTER, ");
            stb.append("        GRADE, ");
            stb.append("        HR_CLASS, ");
            stb.append("        ATTENDNO ");
            stb.append("    FROM ");
            stb.append("        SCHNO W1 ");
            stb.append("    ) ");

            //メイン
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     T2.HR_NAME, ");
            stb.append("     T2.HR_CLASS_NAME1, ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     GDAT.GRADE_NAME1, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     CASE WHEN T3.BIRTHDAY IS NOT NULL ");
            stb.append("          THEN YEAR(T2.YEAR || '-04-01' - BIRTHDAY) ");
            stb.append("     END AS AGE, ");
            stb.append("     T3.BIRTHDAY AS BIRTHDAY, ");
            stb.append("     T3.SEX AS SEX_CD, ");
            stb.append("     CASE WHEN MED.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("     END AS HAS_DATA, ");
            stb.append("     T_ENT_YEAR_GRADE_CD.GRADE_CD AS ENT_YEAR_GRADE_CD ");
            stb.append(" FROM ");
            stb.append("     SCHNO_ALL T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("           AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("           AND T2.GRADE = T1.GRADE ");
            stb.append("           AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST VN ON VN.NAMECD1 = 'A023' ");
            stb.append("          AND VN.NAME1 " + schoolKindIn + " ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
            stb.append("           AND GDAT.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN MEDEXAM_DET_DAT MED ON MED.YEAR = T1.YEAR ");
            stb.append("           AND MED.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN (SELECT I1.SCHREGNO, I2.SCHOOL_KIND, MAX(I2.GRADE_CD) AS GRADE_CD  ");
            stb.append("                FROM SCHREG_REGD_DAT I1 ");
            stb.append("                INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR AND I2.GRADE = I1.GRADE ");
            stb.append("                INNER JOIN SCHREG_ENT_GRD_HIST_DAT I3 ON I3.SCHREGNO = I1.SCHREGNO ");
            stb.append("                    AND I3.SCHOOL_KIND = I2.SCHOOL_KIND ");
            stb.append("                WHERE FISCALYEAR(I3.ENT_DATE) = I1.YEAR ");
            stb.append("                GROUP BY I1.SCHREGNO, I2.SCHOOL_KIND ");
            stb.append("               ) T_ENT_YEAR_GRADE_CD ON T_ENT_YEAR_GRADE_CD.SCHREGNO = T1.SCHREGNO AND T_ENT_YEAR_GRADE_CD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("    T1.GRADE BETWEEN VN.NAME2 AND VN.NAME3 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR ");

            return stb.toString();
        }
    }

    String getSchregBaseDetail(final DB2UDB db2, final String schregno, final String seq, final String fieldname) throws SQLException {
        final String sql = "SELECT " + fieldname + " FROM SCHREG_BASE_DETAIL_MST WHERE SCHREGNO = '" + schregno + "' AND BASE_SEQ = '" + seq + "'";
        ResultSet rs = null;
        PreparedStatement ps = null;
        String rtn = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                rtn = rs.getString(fieldname);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }

    private static class KNJF030AParam {

        private String _z010;
        private boolean _isMusashinohigashi;
        private boolean _isChiyodaKudan;
        private boolean _isOutputDebug;
        private final Map _nameMstCache = new HashMap();
        private final Map _dataCache = new HashMap();
        private final Map _nendoCache = new HashMap();

        private String _currentForm;
        final boolean _isSeireki;
        private Map _yearF242name1Map;

        private int hrnameKeta = 0;
        private int hrnameC2_1Keta = 0;
        private int nosediseaseKeta = 20;
        private int nosedisease2Keta = 0;
        private int note1Keta1Only = 0;
        private int note2_1Keta1Only = 0;
        private int note1Keta = 0;
        private int note2_1Keta = 0;
        private int skindisease2_1Keta = 0;
        private int spineribsKeta = 99;
        private int spineribs2Keta = 99;
        private int spineribs3Keta = 99;
        private int eyediseasesKeta = 99;
        private int eyediseases2Keta = 99;
        private int eyediseases3Keta = 99;
        private int nosediseasesKeta = 99;
        private int nosediseases2Keta = 99;
        private int nosediseases3Keta = 99;
        private int docTreatKeta = 20;
        private int tbXRay1Keta = 0;
        private int views2_1Keta = 0;

        KNJF030AParam(final DB2UDB db2) throws SQLException {
            _z010 = getNameMst(db2, "Z010", "00", "NAME1");
            _yearF242name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT YEAR, NAME1 FROM V_NAME_MST WHERE NAMECD1 = 'F242' AND NAMECD2 = '01' "), "YEAR", "NAME1");
            _isSeireki = "2".equals(getNameMst(db2, "Z012", "00", "NAME1"));
            _isMusashinohigashi = "musashinohigashi".equals(_z010);
            _isChiyodaKudan = "chiyoda".equals(_z010);
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            if (_isOutputDebug) {
                log.info(" outputDebug.");
            }
        }

        private String nendo(final DB2UDB db2, final String year) {
            if (!_nendoCache.containsKey(year)) {
                String rtn = "";
                if (StringUtils.isBlank(year)) {
                    rtn = "";
                } else if (_isSeireki) {
                    rtn = year + "年度";
                } else {
                    rtn = KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度";
                }
                _nendoCache.put(year, rtn);
            }
            return (String) _nendoCache.get(year);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            try {
                return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJF030' AND NAME = '" + propName + "' "));
            } catch (Throwable t) {
                log.error("error : " + t.getMessage());
            }
            return null;
        }

        public void setForm(final Vrw32alp svf, final String form) {
            svf.VrSetForm(form, 4);
            if (null != _currentForm && _currentForm.equals(form)) {
                return;
            }
            log.fatal(" form = " + form);
            _currentForm = form;
            try {
                int keta;
                final Map fieldnameMap = SvfField.getSvfFormFieldInfoMapGroupByName(svf);
                hrnameKeta = getFieldLength(fieldnameMap, "HR_NAME");
                hrnameC2_1Keta = getFieldLength(fieldnameMap, "HR_NAMEC2_1");
                keta = getFieldLength(fieldnameMap, "NOSEDISEASE");
                if (keta > 0) {
                    nosediseaseKeta = keta;
                }
                nosedisease2Keta = getFieldLength(fieldnameMap, "NOSEDISEASE2");
                note1Keta1Only = getFieldKeta(fieldnameMap, "NOTE1");
                note2_1Keta1Only = getFieldKeta(fieldnameMap, "NOTE2_1");
                note1Keta = getFieldLength(fieldnameMap, "NOTE1");
                note2_1Keta = getFieldLength(fieldnameMap, "NOTE2_1");
                skindisease2_1Keta = getFieldLength(fieldnameMap, "SKINDISEASE2_1");
                docTreatKeta = getFieldLength(fieldnameMap, "DOC_TREAT1");
                tbXRay1Keta = getFieldLength(fieldnameMap, "TB_X_RAY1");
                views2_1Keta = getFieldLength(fieldnameMap, "VIEWS2_1");
                if (_isMusashinohigashi) {
                    spineribsKeta = getFieldLength(fieldnameMap, "SPINERIBS");
                    spineribs2Keta = getFieldLength(fieldnameMap, "SPINERIBS2");
                    spineribs3Keta = getFieldLength(fieldnameMap, "SPINERIBS3");
                    eyediseasesKeta = getFieldLength(fieldnameMap, "EYEDISEASES");
                    eyediseases2Keta = getFieldLength(fieldnameMap, "EYEDISEASES2");
                    eyediseases3Keta = getFieldLength(fieldnameMap, "EYEDISEASES3");
                    nosediseasesKeta = getFieldLength(fieldnameMap, "NOSEDISEASES");
                    nosediseases2Keta = getFieldLength(fieldnameMap, "NOSEDISEASES2");
                    nosediseases3Keta = getFieldLength(fieldnameMap, "NOSEDISEASES3");
                    log.info(" nosediseaseKeta = " + nosediseaseKeta);
                    log.info(" keta SPINERTIBS = " + spineribsKeta + ", " + spineribs2Keta + ", " + spineribs3Keta);
                    log.info(" keta EYEDISEASES = " + eyediseasesKeta + ", " + eyediseases2Keta + ", " + eyediseases3Keta);
                    log.info(" keta NOSEDISEASES = " + nosediseasesKeta + ", " + nosediseases2Keta + ", " + nosediseases3Keta);
                }
            } catch (Exception e) {
                log.warn("exception!", e);
            } catch (Throwable t) {
                log.warn("not found class SvfField");
            }
        }

        // リンクフィールドを含む桁数
        private int getFieldLength(final Map fieldnameMap, final String fieldname) {
            int rtn = 0;
            final SvfField field = (SvfField) fieldnameMap.get(fieldname);
            if (null == field) {
                log.info("not found svf field: " + fieldname);
            } else {
                rtn = field._fieldLength;
            }
            return rtn;
        }

        // リンクフィールドを含まない桁数
        private int getFieldKeta(final Map fieldnameMap, final String fieldname) {
            int rtn = 0;
            final SvfField field = (SvfField) fieldnameMap.get(fieldname);
            if (null == field) {
                log.info("not found svf field: " + fieldname);
            } else {
                rtn = Integer.parseInt((String) field.getAttributeMap().get(SvfField.AttributeKeta));
            }
            return rtn;
        }

        // 名称マスタの名称予備1が1ならtrue、それ以外はfalse
        boolean nameMstNamespare1Is1(final DB2UDB db2, final String nameCd1, final String nameCd2) throws SQLException {
            return "1".equals(getNameMst(db2, nameCd1, nameCd2, "NAMESPARE1"));
        }

        // 名称マスタの名称予備2が1ならtrue、それ以外はfalse
        boolean nameMstNamespare2Is1(final DB2UDB db2, final String nameCd1, final String nameCd2) throws SQLException {
            return "1".equals(getNameMst(db2, nameCd1, nameCd2, "NAMESPARE2"));
        }

        private String nameMstKey(final String nameCd1, final String nameCd2, final String fieldname) {
            return "NAME_MST." + nameCd1 + "." + nameCd2 + "." + fieldname;
        }

        String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2, final String fieldname) throws SQLException {
            if (null == nameCd2) {
                return null;
            }
            final String cacheKey = nameMstKey(nameCd1, nameCd2, fieldname);
            if (_nameMstCache.containsKey(cacheKey)) {
                return (String) _nameMstCache.get(cacheKey);
            }
            final String sql = "SELECT " + fieldname + " FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'";
            ResultSet rs = null;
            PreparedStatement ps = null;
            String rtn = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(fieldname);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _nameMstCache.put(cacheKey, rtn);
            return rtn;
        }
        private boolean isGrade23(final DB2UDB db2, final String year, final String grade) {
            final String sql = " SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "' ";
            final String gradeCd = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            final boolean res = gradeCd == null ? false : ("2".equals(String.valueOf(Integer.parseInt(gradeCd))) || "3".equals(String.valueOf(Integer.parseInt(gradeCd))));
            return res;
        }
    }
}

// eof
