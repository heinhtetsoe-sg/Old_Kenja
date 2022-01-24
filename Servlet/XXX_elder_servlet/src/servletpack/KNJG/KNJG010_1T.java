// kanji=漢字
/*
 * $Id: bf22b0f497b42eba9588f7d4d8f531ed6d76a6e2 $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJG;

import static servletpack.KNJZ.detail.KNJ_EditEdit.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.SvfForm.Field;
import servletpack.KNJZ.detail.SvfForm.ImageField;
import servletpack.KNJZ.detail.SvfForm.KoteiMoji;
import servletpack.KNJZ.detail.SvfForm.Line;


/*
 *
 *  学校教育システム 賢者 [事務管理]
 *
 *      卒業証明書（和）     Form-ID:KNJG010_1   証明書種別:001
 *      卒業証明書（英）     Form-ID:KNJG010_2   証明書種別:002
 *      卒業見込証明書（和） Form-ID:KNJG010_3   証明書種別:003
 *      卒業見込証明書（英） Form-ID:KNJG010_6   証明書種別:012
 *      在学証明書（和）     Form-ID:KNJG010_4   証明書種別:004
 *      在学証明書（英）     Form-ID:KNJG010_5   証明書種別:005
 *      在籍証明書（和）     Form-ID:KNJG010_7   証明書種別:013
 *      在籍証明書（英）     Form-ID:KNJG010_8   証明書種別:014
 *      修了証明書（和）     Form-ID:KNJG010_9   証明書種別:015
 *      修了証明書（英）     Form-ID:KNJG010_10  証明書種別:016
 *
 *      引数について  param[0] :学籍番号
 *                    param[2] :対象年度
 *                    param[3] :対象学期
 *                    param[8] :証明書日付
 *                    param[9] :証明書番号
 *                    param[11]:現年度
 *
 *    2005/01/26 yamashiro  東京都用に修正
 *    2005/10/22 m-yama /KNJZ/KNJ_Schoolinfoを/KNJG/KNJ_SchoolinfoSqlへ変更 NO001
 *    2005/10/22 m-yama 除籍通知書追加 NO002
 *    2005/10/22 m-yama 在学、終了の第*学年を単位制：*年次 その他：第*学年 NO003
 *
 */

public class KNJG010_1T {

    private static final Log log = LogFactory.getLog(KNJG010_1T.class);

    private static final String H = "H";
    private static final String J = "J";
    private static final String P = "P";
    private static final String K = "K";

    // 証明書種別（CERTIF_KIND）
    private enum CK {
          _1_SOTSU(1, H)                       // 卒業証明書
        , _2_SOTSU_ENG(2, H, true)            // 卒業証明書（英）
        , _3_SOTSUMI(3, H)                     // 卒業見込証明書
        , _4_ZAIGAKU(4, null)  // 在学証明書
        , _5_ZAIGAKU_ENG(5, null, true)          // 在学証明書（英）
        , _12_ZAIGAKU_J(12, J)                // 在学証明書（中学）
        , _13_ZAISEKI(13, null)  // 在籍証明書
        , _14_ZAISEKI_ENG(14, null, true)       // 在籍証明書（英）
        , _15_SHURYO(15, J)                   // 修了証明書
        , _16_SHURYO_ENG(16, J, true)        // 修了証明書（英）
        , _19_SOTSUMI_ENG(19, H, true)       // 英 卒業見込証明書
        , _20_JOSEKI(20, null)                   // 除籍証明書
        , _21_ZAIGAKU_J_ENG(21, J, true)     // 在学証明書（中学英）
        , _22_SOTSU_J(22, J)                  // 卒業証明書
        , _23_SOTSU_P(23, P)                  // 卒業証明書（智辯）
        , _24_ZAIGAKU_P(24, P)                // 在学証明書（智辯）
        , _31_ZAIRYOU(31, null)                  // 在寮証明書
        , _32_SOTSU_J_ENG(32, J, true)       // 卒業証明書（中学英）
        , _38_SHURYOMI(38, J)                 // 修了見込証明書
        , _39_SHURYOMI_ENG(39, J, true)      // 修了見込証明書（英）
        , _40_ZAIGAKU_K(40, K)               // 在学証明書（幼）
        , _41_ZAISEKI_K(41, K)               // 在籍証明書（幼）
        , _42_ZAIGAKU_P_ENG(42, P, true)    // 在学証明書（小学英）
        , _43_KYUGAKU_KYOKA(43, H)         // 休学許可書
        , _44_RYUGAKU_KYOKA(44, H)         // 留学許可書
        , _45_TAIGAKU_KYOKA(45, H)         // 退学許可書
        , _46_GAKUWARI_H(46, H)             // 学割（一般）高校
        , _47_GAKUWARI_H_CLUB(47, H)       // 学割（クラブ）高校
        , _48_GAKUWARI_J(48, J)             // 学割（一般）中学
        , _49_GAKUWARI_J_CLUB(49, J)       // 学割（クラブ）中学
        , _50_SOTSU_H(50, H)                // 卒業証明書（既卒）高校
        , _51_SOTSU_J(51, J)                // 卒業証明書（既卒）中学
        , _52_SUISEN_H(52, H)               // 推薦書 高校
        , _53_SUISEN_J(53, J)               // 推薦書 中学
        , _54_ZAIRYOU_J(54, J)              // 在寮証明書 中学
        , _56_SPORTS_SHINKOU_CENTER_KANYU_H(56, H)  // 日本スポーツ振興センター加入証 高校
        , _57_SPORTS_SHINKOU_CENTER_KANYU_J(57, J)  // 日本スポーツ振興センター加入証 中学
        ;

        final int _certifKind;
        final String _schoolKind;
        final boolean _isEnglish;
        CK(final int certifKind, final String schoolKind) {
            this(certifKind, schoolKind, false);
        }
        CK(final int certifKind, final String schoolKind, final boolean isEnglish) {
            _certifKind = certifKind;
            _schoolKind = schoolKind;
            _isEnglish = isEnglish;
        }

        // 卒業証を出力する
        boolean isPrintSotsugyo() {
            return in(_1_SOTSU, _22_SOTSU_J, _23_SOTSU_P, _50_SOTSU_H, _51_SOTSU_J);
        }

        // 卒業見込証明を出力する
        boolean isPrintSotsugyoMikomi() {
            return in(_3_SOTSUMI);
        }

        // 在学証明書を出力する
        boolean isPrintZaigaku() {
            return in(_4_ZAIGAKU, _12_ZAIGAKU_J, _24_ZAIGAKU_P, _40_ZAIGAKU_K);
        }

        // 修了証明書を出力する
        boolean isPrintShuryo() {
            return in(_15_SHURYO, _38_SHURYOMI);
        }

        // 在学証明書英文を出力する
        boolean isPrintZaigakuEng() {
            return in(_5_ZAIGAKU_ENG, _21_ZAIGAKU_J_ENG, _42_ZAIGAKU_P_ENG);
        }

        // 学割証を出力する
        boolean isPrintGakuwari() {
            return in(_46_GAKUWARI_H, _47_GAKUWARI_H_CLUB, _48_GAKUWARI_J, _49_GAKUWARI_J_CLUB);
        }

        // 在寮証明書を出力する
        boolean isPrintZairyou() {
            return in(_31_ZAIRYOU, _54_ZAIRYOU_J);
        }

        // 在籍証明書を出力する
        boolean isPrintZaiseki() {
            return in(_13_ZAISEKI, _41_ZAISEKI_K);
        }

        // 英文証明書を出力する
        boolean isPrintSotsugyoEng() {
            return in(_2_SOTSU_ENG, _14_ZAISEKI_ENG, _16_SHURYO_ENG, _19_SOTSUMI_ENG, _32_SOTSU_J_ENG, _39_SHURYOMI_ENG);
        }

        static CK getCK(final String certifKind) {
            final int certifKindInt = Integer.parseInt(certifKind);
            for (final CK ck : CK.values()) {
                if (ck._certifKind == certifKindInt) {
                    return ck;
                }
            }
            throw new IllegalArgumentException("不明な証明書種別 :" + certifKind);
        }
        boolean in(final CK ... cks) {
            return null != cks && ArrayUtils.contains(cks, this);
        }
    }

    private static final String YMD = "YMD";
    private static final String YM = "YM";

    private static final String MONTHFULL = "MMMM";

    private static final String SLASH_dd_MM_yyyy = "dd/MM/yyyy";
    private static final String MONTHFULL_dd_comma_SPC_yyyy = MONTHFULL + " d, yyyy";
    private static final String MONTHFULL_dd_comma_yyyy = MONTHFULL + " dd,yyyy";
    private static final String MONTHFULL_d_comma_yyyy = MONTHFULL + " d,yyyy";
    private static final String MONTHFULL_comma_yyyy_sapporo = MONTHFULL + ", yyyy";
    private static final String d_MONTHFULL_yyyy_sapporo = "d " + MONTHFULL + " yyyy";
    private static final String d_MONTHFULL_comma_yyyy = "d " + MONTHFULL + ", yyyy";
    private static final String d_MONTHFULL_comma_yyyy_meiji = "d " + MONTHFULL + ",yyyy";
    private static final String d_MONTHFULL_yyyy = "d " + MONTHFULL + " yyyy";

    private static final String MONTHFULL_comma_yyyy = MONTHFULL + ", yyyy";
    private static final String MONTHFULL_comma_yyyy_meiji = MONTHFULL + ",yyyy";
    private static final String MONTHFULL_yyyy = MONTHFULL + " yyyy";
    private static final String FROM_TO_MARK = "\uFF5E";
    private static int INVALID_X = 99999;

    private final String KW_SEITO_KATEIMEI = "[[生徒.課程名]]";
    private final String KW_SEITO_GAKKAMEI = "[[生徒.学科名]]";
    private final String KW_SEITO_NYUGAKUHIDUKE = "[[生徒.入学日付]]";
    private final String KW_SEITO_ZAISEKISHOMEISHOSOTSUGYOHIDUKE = "[[生徒.在籍証明書卒業日付]]";

    public Vrw32alp svf;       //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    public DB2UDB db2;                          //Databaseクラスを継承したクラス
    private boolean _hasData;

    private Param _param;

    public KNJG010_1T(final DB2UDB db2, final Vrw32alp svf, final KNJDefineSchool definecode) {
        log.fatal("$Revision: 77452 $ $Date: 2020-10-14 21:45:05 +0900 (水, 14 10 2020) $"); // CVSキーワードの取り扱いに注意

        this.db2 = db2;
        this.svf = svf;
        _param = new Param(db2, definecode);
    }

    /**
     *  PrepareStatement作成
     */
    public void pre_stat(final String ptype) {
        pre_stat(ptype, new HashMap());
    }

    /**
     *  PrepareStatement作成
     */
    public void pre_stat(final String ptype, final Map paramMap) {
        _param._isPrintGrd = "1".equals(paramMap.get("PRINT_GRD"));
        // 個人データ
        if (!_param._isPrintGrd) {
            if (log.isDebugEnabled()) {
                log.debug("USE KNJG010_1T");
            }
        }
    }

    /**
     *  PrepareStatement close
     */
    public void pre_stat_f() {
        _param.close();
    }

    /**
     *  過卒生対応年度取得
     */
    public static String b_year(final String pdate) {
        return defstr(nendoOfDate(pdate));
    }

    private static String nendoOfDate(final String pdate) {
        String nendo = null;
        if (pdate != null) {
            final String nen = pdate.substring(0, 4);
            nendo = nen;
            final String tsuki = pdate.substring(5, 7);
            if (Arrays.asList("01", "02", "03").contains(tsuki)) {
                nendo = String.valueOf(Integer.parseInt(nen) - 1);
            }
        }
        return nendo;

    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static class Utils {

        /**
         * 単語途中でなるべく区切らないように分割
         *
         * <pre>
         * splitBySizeWithSpace("abcde fghij klmnop", 13) = {"abcde fghij ", "klmnop"}
         * splitBySizeWithSpace("abcde fghij klmnop", 10) = {"abcde", "fghij", "klmnop"}
         * splitBySizeWithSpace("abcde fghij klmnop", 7) =  {"abcde", "fghij", "klmnop"}
         * </pre>
         *
         * @param s 文字列
         * @param keta 桁
         * @return 文字列を分割した配列
         */
        private static List<String> splitBySizeWithSpace(final String s, int... ketas) {
            if (null == s) {
                return new ArrayList<String>();
            } else if (s.length() <= ketas[0]) {
                return new ArrayList<String>(Arrays.asList(s));
            }
            final List<String> split = new ArrayList<String>();
            int nextidx = ketas[0];
            int beforeidx = 0;
            while (nextidx < s.length()) {
                int idxSpace = -1;
                boolean isCheckSpace = false;
                if (s.charAt(nextidx) != ' ') { // 単語途中で区切りがスペースでなければ、前方探索
                    isCheckSpace = true;
                    searchBackward:
                    for (int i = nextidx - 1; i > beforeidx; i--) {
                        if (s.charAt(i) == ' ') {
                            idxSpace = i;
                            break searchBackward;
                        }
                    }
                    //log.info("  idxSpace = " + idxSpace + " at " + s + " ( " + idx + " = " + s.charAt(idx) + ")");
                }
                if (idxSpace != -1) {
                    split.add(s.substring(beforeidx, idxSpace));
                    beforeidx = idxSpace + 1;
                } else {
                    // スペースがなければ指定区切りまでを追加
                    if (isCheckSpace) {
                        //log.info(" no space-char in [" + beforeidx + ", " + idx + "] string [" + s.substring(beforeidx, idx) + "]  full [" + s + "]");
                    }
                    split.add(s.substring(beforeidx, nextidx));
                    beforeidx = nextidx;
                }
                while (s.charAt(beforeidx) == ' ' && beforeidx + 1 < s.length()) {
                    beforeidx += 1;
                }
                nextidx = beforeidx + ketas[split.size() < ketas.length ? split.size() : ketas.length - 1];
                if (s.length() <= nextidx) {
                    split.add(s.substring(beforeidx));
                    break;
                }
            }
            return split;
        }

        static String join(final Collection<String> list, final String comma) {
            final StringBuffer stb = new StringBuffer();
            String comma0 = "";
            for (final String s : list) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                stb.append(comma0).append(s);
                comma0 = comma;
            }
            return stb.toString();
        }

        static String prepend(final String prep, final Object o) {
            return null == o || StringUtils.isBlank(o.toString()) ? "" : prep + o.toString();
        }

        static String append(final Object o, final String app) {
            return null == o || StringUtils.isBlank(o.toString()) ? "" : o.toString() + app;
        }

        /**
         * 間にインサート
         * @param o1
         * @param ins
         * @param o2
         * @return
         */
        static String aidaNiInsert(final Object o1, final String ins, final Object o2) {
            final String s1 = null == o1 || StringUtils.isBlank(o1.toString()) ? "" : o1.toString();
            final String s2 = null == o2 || StringUtils.isBlank(o2.toString()) ? "" : o2.toString();
            if (StringUtils.isBlank(s1)) {
                return s2;
            } else if (StringUtils.isBlank(s2)) {
                return s1;
            }
            return s1 + ins + s2;
        }

        static String addKatei(final String courseName) {
            if (StringUtils.isBlank(courseName)) {
                return courseName;
            }
            if (courseName.endsWith("課程")) {
                return courseName;
            }
            return courseName + "課程";
        }

        static String[] gengoNenTsukiHi(final DB2UDB db2, final String dateStr) {
            final String[] rtn = new String[5];
            try {
                Date dat = getDateObject(dateStr);
                String hdate = null;
                if (null != dat) {
                    final Calendar cal = new GregorianCalendar(new Locale("ja","JP"));
                    cal.setTime(dat);
                    hdate = KNJ_EditDate.gengou(db2, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE));
                }
                if (null != hdate) {
                    rtn[0] = hdate.substring(0, hdate.indexOf("年"));
                    int numIdx = -1;
                    for (int i = rtn[0].length() - 1; i >= 0; i--) {
                        final char ch = rtn[0].charAt(i);
                        if ('0' <= ch && ch <= '9' || ch == '元') {
                            numIdx = i;
                            continue;
                        } else {
                            break;
                        }
                    }
                    if (-1 != numIdx) {
                        rtn[1] = rtn[0].substring(numIdx);
                        rtn[0] = rtn[0].substring(0, numIdx);
                    }
                    rtn[2] = hdate.substring(hdate.indexOf("年") + 1, hdate.indexOf("月"));
                    rtn[3] = hdate.substring(hdate.indexOf("月") + 1, hdate.indexOf("日"));
                    rtn[4] = "parsed";
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        /*----------------------------------------------------------------------------------------------*
         * 日付の編集(日本)
         * ※使い方
         *   String dat = h_format_JP("2002-10-27")     :平成14年10月27日
         *   String dat = h_format_JP("2002/10/27")     :平成14年10月27日
         *----------------------------------------------------------------------------------------------*/
        /**
         * 日付文字列を日本語にフォーマットした値を得る
         * @param dateStr 日付文字列 (例:2001-01-01 or 2001/01/01)
         * @param addSpace スペースを追加するか
         * @param printDefaultFormat 日付がとれない際にデフォルト日付文言を返すか
         * @return フォーマットした日付
         */
        static String h_format_JP(final DB2UDB db2, final String dateStr, final boolean addSpace, final boolean printDefaultFormat) {
            String hdate = "";
            final String[] gengoNenTsukiHi = Utils.gengoNenTsukiHi(db2, dateStr);
            final String space = addSpace ? " " : "";
            if ("parsed".equals(gengoNenTsukiHi[4])) {
                //log.info(" nenkumi " + ArrayUtils.toString(gengoNenTsukiHi));
                final String gengo = gengoNenTsukiHi[0];
                final String nen = gengoNenTsukiHi[1];
                final String tsuki = gengoNenTsukiHi[2];
                final String hi = gengoNenTsukiHi[3];
                hdate = gengo + space + nen + space + "年" + space + tsuki + space + "月" + space + hi + space + "日";
            } else if (printDefaultFormat) {
                final Date d = new Date();
                final String[] p = Utils.gengoNenTsukiHi(db2, new SimpleDateFormat("yyyy-MM-dd").format(d));
                final String gengo = p[0];
                hdate = gengo + space + "  " + space + "年" + space + "  " + space + "月" + space + "  " + space + "日";
            }
            return hdate;
        }//String h_format_JPの括り

        /**
         * 日付文字列を日本語（漢字）にフォーマットした値を得る
         * @param dateStr 日付文字列 (例:2001-01-01 or 2001/01/01)
         * @param addSpace スペースを追加するか
         * @param printDefaultFormat 日付がとれない際にデフォルト日付文言を返すか
         * @return フォーマットした値 (例：平成十二年一月一日)
         */
        static String formatDateKanji(final DB2UDB db2, final String dateStr, final boolean addSpace, final boolean printDefaultFormat) {
            String hdate = "";
            final String[] gengoNenTsukiHi = Utils.gengoNenTsukiHi(db2, dateStr);
            final String space = addSpace ? " " : "";
            if ("parsed".equals(gengoNenTsukiHi[4])) {
                //log.info(" nenkumi " + ArrayUtils.toString(gengoNenTsukiHi));
                final String gengo = StringUtils.trim(gengoNenTsukiHi[0]);
                final String nen = digitsToKanji(gengoNenTsukiHi[1], "　");
                final String tsuki = digitsToKanji(gengoNenTsukiHi[2], "　");
                final String hi = digitsToKanji(gengoNenTsukiHi[3], "　");
                //log.info(" parsed " + ArrayUtils.toString(new String[] {gengo, nen, tsuki, hi}));
                hdate = gengo + space + nen + space + "年" + space + tsuki + space + "月" + space + hi + space + "日";
            } else if (printDefaultFormat) {
                final Date d = new Date();
                final String[] p = Utils.gengoNenTsukiHi(db2, new SimpleDateFormat("yyyy-MM-dd").format(d));
                final String gengo = p[0];
                hdate = gengo + space + "  " + space + "年" + space + "  " + space + "月" + space + "  " + space + "日";
            }
            //log.info(" dateStr " + dateStr + " -> " + hdate);
            return hdate;
        }

        /**
         * 数字のみの文字列を漢字に変換する 4桁まで
         *  例： 1 -> 一
         *      10 -> 十
         *     510 -> 五百十
         *    3012 -> 三千十二
         *    9090 -> 九千九十
         *    7070 -> 七千七十
         * @param numStr 数字のみ(0123456789)の文字列
         * @param def 代替文字列
         * @return
         */
        static String digitsToKanji(final String numStr, final String def) {
            if (!NumberUtils.isDigits(numStr)) {
                if (StringUtils.isBlank(numStr)) {
                    return StringUtils.defaultString(def);
                } else {
                    return StringUtils.defaultString(numStr);
                }
            }
            final int maxKeta = 4; // とりあえず4桁まで
            if (numStr.length() > maxKeta) {
                return def;
            }
            final Map kanji = new HashMap();
            kanji.put("0", "〇");
            kanji.put("1", "一");
            kanji.put("2", "二");
            kanji.put("3", "三");
            kanji.put("4", "四");
            kanji.put("5", "五");
            kanji.put("6", "六");
            kanji.put("7", "七");
            kanji.put("8", "八");
            kanji.put("9", "九");
            final Map rankKanji = new HashMap();
            rankKanji.put(new Integer(4), "千");
            rankKanji.put(new Integer(3), "百");
            rankKanji.put(new Integer(2), "十");
            rankKanji.put(new Integer(1), "");
            final StringBuffer stb = new StringBuffer();
            for (int i = numStr.length() - 1; i >= 0; i--) {
                final int n = Integer.parseInt(String.valueOf(numStr.charAt(numStr.length() - i - 1)));
                final Integer kurai = new Integer(i + 1);
                String add;
                if (n == 0) {
                    if ("0".equals(numStr)) {
                        add = "" + kanji.get(String.valueOf(n));
                    } else {
                        add = "";
                    }
                } else if (n == 1 && kurai.intValue() != 1) {
                    add = "" + rankKanji.get(kurai);
                } else {
                    add = "" + kanji.get(String.valueOf(n)) + rankKanji.get(kurai);
                }
                stb.append(add);
            }
            return stb.toString();
        }

        static String getSeirekiFlgDateString(final DB2UDB db2, final PrintData printData, final String date, final String format, final String defString, final boolean numZenkaku) {
            return getSeirekiFlgDateString(db2, printData, date, format, defString, numZenkaku, false);
        }

        static String getSeirekiFlgDateString(final DB2UDB db2, final PrintData printData, final String date, final String format, final String defString, final boolean numZenkaku, final boolean addSpace) {
            if (date == null || 0 == date.length()) {
                return defString;
            }
            String rtn;
            if (YM.equals(format)) {
                // YM
                if (printData._seirekiFlg) {
                    rtn = date.substring(0, 4) + "年" + KNJ_EditDate.h_format_S(date, "M") + "月";
                } else {
                    rtn = KNJ_EditDate.h_format_JP_M(db2, date);
                }

            } else {
                // YMD
                if (printData._seirekiFlg) {
                    rtn = date.substring(0, 4) + "年" + h_format_JP_MD(date);  // 証明日付
                } else {
                    rtn = Utils.h_format_JP(db2, date, addSpace, false);  // 証明日付
                }
            }
            if (numZenkaku) {
                rtn = PrintData.hankakuToZenkaku(rtn);
            }
            return rtn;
        }

        static String getBirthdayFormattedString(final DB2UDB db2, Param param, final PrintData printData, final String rsBirthday, final String birthdayFlg, final boolean addSpace, final boolean printDefaultFormat) {
            if (null == rsBirthday || rsBirthday.length() == 0) {
                return null;
            }
            final boolean notAddUmare = param._z010.in(Z010.jyoto);
            String birthday;
            if (printData._seirekiFlg || (!printData._seirekiFlg && "1".equals(birthdayFlg))) {
                birthday = rsBirthday.substring(0, 4) + "年" + h_format_JP_MD(rsBirthday);
            } else {
                birthday = Utils.h_format_JP(db2, rsBirthday, addSpace, printDefaultFormat);
            }
            if (!notAddUmare) {
                if (null != birthday) {
                    birthday += "生";
                }
            }
            return birthday;
        }

        static Date getDateObject(final String dateStr) {
            SimpleDateFormat sdf = new SimpleDateFormat();
            Date dat = null;
            try {
                sdf.applyPattern("yyyy-MM-dd");
                dat = sdf.parse(dateStr);
            } catch (Exception e) {
                try {
                    sdf.applyPattern("yyyy/MM/dd");
                    dat = sdf.parse(dateStr);
                } catch (Exception e2) {
                }
            }
            return dat;
        }

        static Calendar getCalendar(final String dateStr) {
            Date d = null;
            if (null != dateStr) {
                d = getDateObject(dateStr);
                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                return cal;
            }
            return null;
        }

        static Calendar parseMonthSlashDayOfMonth(final String year, final String monthSlashDayOfMonth) {
            final SimpleDateFormat sdf = new SimpleDateFormat();
            Calendar cal = null;
            if (null != monthSlashDayOfMonth) {
                try {
                    sdf.applyPattern("yyyy/MM/dd");
                    Date d = sdf.parse(year + "/" + monthSlashDayOfMonth);
                    cal = Calendar.getInstance();
                    cal.setTime(d);
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            return cal;
        }

        static String h_format_JP_MD(final String dateStr) {
            String hdate = "";
            try {
                Date dat = getDateObject(dateStr);
                if (null != dat) {
                    hdate = new SimpleDateFormat("M月d日", new Locale("ja","JP")).format(dat);
                }
            } catch (Exception e) {
                hdate = "";
            }
            return hdate;
        }

        /**
         *  日付の編集（ブランク挿入）
         *  ○引数について >> １番目は編集対象日付「平成18年1月1日」、２番目は元号取得用年度
         *  ○戻り値について >> 「平成3年1月1日」-> 「平成 3年 1月 1日」
         */
        static String setDateFormat(final DB2UDB db2, final String nendo) {
            StringBuffer stb = new StringBuffer();
            stb.append(KNJ_EditDate.gengou(db2, Integer.parseInt(nendo), 4, 1));
            if (2 < stb.length()) {
                stb.delete(2, stb.length());
            }
            stb.append("  年  月  日");
            return stb.toString();
        }

        static String getNumSuffix(final String num) {
            String sfx = "";
            if (NumberUtils.isDigits(num)) {
                final int inum = Integer.parseInt(num);
                if (inum >= 0) {
                    switch (inum % 10) {
                    case 1: sfx = "st"; break;
                    case 2: sfx = "nd"; break;
                    case 3: sfx = "rd"; break;
                    default: sfx = "th"; break;
                    }
                }
            }
            return sfx;
        }

        static String sishagonyu(final double val) {
            return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        static String append(final String s, final String add) {
            if (StringUtils.isBlank(s)) {
                return "";
            }
            return s + add;
        }

        static int toInt(final String str, final int def) {
            return NumberUtils.isNumber(str) ? new BigDecimal(str).intValue() : def;
        }

        static double toDouble(final String str, final double def) {
            return NumberUtils.isNumber(str) ? new BigDecimal(str).doubleValue() : def;
        }

        static List<String> concat(final List<String> list, final String elem) {
            final List<String> rtn = new ArrayList<String>(list);
            rtn.add(elem);
            return rtn;
        }

        static String kakko(final String s) {
            if (StringUtils.isBlank(s)) {
                return s;
            }
            return "(" + s + ")";
        }

        static String mkString(final TreeMap<String, String> map, final String comma) {
            final List<String> list = new ArrayList<String>();
            for (final Map.Entry<String, String> e : map.entrySet()) {
                if (StringUtils.isEmpty(e.getKey()) || StringUtils.isEmpty(e.getValue())) {
                    continue;
                }
                list.add(e.getKey() + "=" + e.getValue());
            }
            return mkString(list, comma);
        }

        static String mkString(final Collection<String> list, final String comma) {
            return join(list, comma);
        }

        static int mmToDot(final String mm) {
            final BigDecimal dpi = new BigDecimal("400");
            final BigDecimal mmPerInch = new BigDecimal("25.4");
            final int dot = new BigDecimal(mm).multiply(dpi).divide(mmPerInch, 1, BigDecimal.ROUND_HALF_UP).intValue();
            return dot;
        }

        static BigDecimal dotToMm(final String dot) {
            final BigDecimal dpi = new BigDecimal("400");
            final BigDecimal mmPerInch = new BigDecimal("25.4");
            final BigDecimal mm = new BigDecimal(dot).multiply(mmPerInch).divide(dpi, 1, BigDecimal.ROUND_HALF_UP);
            return mm;
        }
    }

    /**
     *  各種証明書印刷処理
     *      証書種別番号からフォームの枝番を取出したものをココでの通番号とする
     */
    public boolean printSvfMain(final String[] parama, final String ctrlYear) {
        PrintData printData = null;
        try {
            printData = new PrintData(parama, ctrlYear, _param, db2);
            if (_param._isOutputDebug) {
                log.info("pdiv = " + printData._certifKind + " , ck = " + printData._ck);
            }
        } catch (Exception e) {
            if (null == printData) {
                log.error(" output null : ", e);
            } else {
                log.warn(" output null : " + (null == printData));
            }
            return false;
        }

        _hasData = false;
        if (null == printData._personalInfo) {
            log.warn(" no personal info : schregno = " + printData._schregno);
        } else {
            printPage(db2, printData);
        }
        return _hasData;
    }

    private static String debugMapToStr(final String debugText, final Map map0) {
        final Map m = new HashMap();
        m.putAll(map0);
        for (final Iterator it = m.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            if (e.getKey() instanceof Integer) {
                it.remove();
            }
            final String key = (String) e.getKey();
            if (Utils.toInt(key.replaceAll("^_+", ""), 0) >= 1) {
                it.remove();
            }
        }
        final Map map = new TreeMap(m);
        final StringBuffer stb = new StringBuffer();
        stb.append(defstr(debugText) + " [\n");
        final List keys = new ArrayList(map.keySet());
        try {
            Collections.sort(keys);
        } catch (Exception e) {
        }
        for (int i = 0; i < keys.size(); i++) {
            final Object key = keys.get(i);
            stb.append(i == 0 ? "   " : " , ").append(key).append(": ").append(map.get(key)).append("\n");
        }
        stb.append("]");
        return stb.toString();
    }

    /*
     *  証明日付の出力
     */
    private String US_format(final String[] divs) {
        final String format;
        if (ArrayUtils.contains(divs, YM)) {
            if (_param._z010.in(Z010.Osakatoin, Z010.Hirokoudai, Z010.ChiyodaKudan, Z010.Nagisa)) {
                format = MONTHFULL_comma_yyyy;
            } else if (ArrayUtils.contains(divs, "1")) {
                format = MONTHFULL_comma_yyyy;
            } else if (_param._z010.in(Z010.Sundaikoufu, Z010.Rakunan, Z010.Meikei)) {
                format = MONTHFULL_comma_yyyy;
            } else if (_param._z010.in(Z010.Meiji)) {
                format = MONTHFULL_comma_yyyy_meiji;
            } else {
                format = MONTHFULL_yyyy;
            }
        } else {
            if (_param._z010.in(Z010.Nagisa, Z010.jyoto)) {
                format = MONTHFULL_dd_comma_SPC_yyyy;
            } else if (_param._z010.in(Z010.Hirokoudai, Z010.ChiyodaKudan)) {
                format = d_MONTHFULL_comma_yyyy;
            } else if (_param._z010.in(Z010.Osakatoin)) {
                format = MONTHFULL_d_comma_yyyy;
            } else if (ArrayUtils.contains(divs, "1")) {
                format = MONTHFULL_dd_comma_yyyy;
            } else if (_param._z010.in(Z010.Sundaikoufu, Z010.Rakunan, Z010.Meikei)) {
                format = MONTHFULL_d_comma_yyyy;
            } else if (_param._z010.in(Z010.Meiji)) {
                format = d_MONTHFULL_comma_yyyy_meiji;
            } else {
                format = d_MONTHFULL_yyyy;
            }
        }
        return format;
    }

    private String dateBlankString() {
        final StringBuffer stb = new StringBuffer();
        final String s = US_format(null);
        for (char ch : s.toCharArray()) {
            if (ch == ',') {
                stb.append(ch);
            } else {
                stb.append(' ');
            }
        }
        return stb.toString();
    }

    /*
     *  証明日付の出力
     */
    private String getSvfDate_US_M(final String date, final String def) {
        final String format;
        if (_param._z010.in(Z010.Meiji)) {
            format = d_MONTHFULL_comma_yyyy_meiji.substring(2);
        } else if (_param._z010.in(Z010.Meikei)) {
            format = MONTHFULL_d_comma_yyyy;
        } else {
            format = d_MONTHFULL_yyyy.substring(2);
        }
        return _param.formatDateUs(date, format, def);
    }

    private static String defstr(final String ... s) {
        if (null == s) {
            return "";
        }
        if (s.length == 1) {
            return StringUtils.defaultString(s[0]);
        }
        for (final String v : s) {
            if (null != v) {
                return v;
            }
        }
        return "";
    }

    private void printPage(final DB2UDB db2, final PrintData printData) {
        final Form form = new Form(svf, _param);

        if (printData._ck.isPrintSotsugyo()) {
            printSotsugyo(db2, form, printData); // form _1
        } else if (printData._ck.isPrintSotsugyoMikomi()) {
            printSotsugyoMikomi(db2, form, printData); // form _3
        } else if (printData._ck.isPrintZaigaku()) {
            printZaigaku(db2, form, printData); // form _4
        } else if (printData._ck.isPrintShuryo()) {
            printShuryo(db2, form, printData); // form _9
        } else if (printData._ck.isPrintZaigakuEng()) {
            printZaigakuEng(form, printData); // form _5
        } else if (printData._ck.isPrintGakuwari()) {
            printGakuwari(db2, form, printData); // form _46
        } else if (printData._ck.isPrintZairyou()) {
            printZairyou(db2, form, printData); // form _31
        } else if (printData._ck.isPrintZaiseki()) {
            printZaiseki(db2, form, printData); // form _7
        } else if (printData._ck.isPrintSotsugyoEng()) {
            printSotsugyoEng(form, printData); // form _2, _6, _8, _10
        } else {
            switch (printData._ck) {
            case _20_JOSEKI:
                printJoseki(db2, form, printData); // form _11
                break;
            case _43_KYUGAKU_KYOKA:
            case _44_RYUGAKU_KYOKA:
            case _45_TAIGAKU_KYOKA:
                printKyokasho(db2, form, printData); // form _43, _44, _45
                break;
            case _56_SPORTS_SHINKOU_CENTER_KANYU_H:
            case _57_SPORTS_SHINKOU_CENTER_KANYU_J:
                printSportsShinkouCenterKanyusho(db2, form, printData);
                break;
            case _52_SUISEN_H:
            case _53_SUISEN_J:
                printBlank(form, printData);
                break;
            default:
            }
        }
        form.VrEndPage();
    }

    private static class Attribute {

        private static final String MUHENSHU = "Hensyu=0";
        private static final String MIGITSUME = "Hensyu=1"; // 右詰め
        private static final String HIDARITSUME = "Hensyu=2"; // 左詰め
        private static final String CENTERING = "Hensyu=3"; // 中央寄せ
        private static final String KINTOUWARI = "Hensyu=4"; // 均等割

        public static String setX(int x) {
            return "X=" + x;
        }

        public static String setY(int y) {
            return "Y=" + y;
        }

        public static String charSize(final double size) {
            return "Size=" + Utils.sishagonyu(size);
        }

        public static String setKeta(final int keta) {
            return "Keta=" + String.valueOf(keta);
        }

        public static String plusX(final String fieldname, final int dx, final Form form) {
            return form.attributeIntPlus(fieldname, "X", dx);
        }

        public static String plusY(final String fieldname, final int dy, final Form form) {
            return form.attributeIntPlus(fieldname, "Y", dy);
        }

    }

    private static class Form {
        private static final String KNJG010_1KYOTO = "KNJG010_1.frm";

        final Vrw32alp _svf;
        final Param _param;
        String _currentForm;
        private Map<String, List<String>> _attributeMap = new HashMap<String, List<String>>();

        Form(final Vrw32alp svf, final Param param) {
            _svf = svf;
            _param = param;
        }

        final Map<String, SvfForm> _formSvfFormMap = new HashMap<String, SvfForm>();
        final Map<String, Map<String, SvfField>> _formnameSvfFormInfoMapMap = new HashMap<String, Map<String, SvfField>>();

        private void setForm0(final String form) {
            _svf.VrSetForm(form, 1);
            log.info(" setForm " + form);
            _currentForm = form;
            try {
                if (!_formnameSvfFormInfoMapMap.containsKey(_currentForm)) {
                    _formnameSvfFormInfoMapMap.put(_currentForm, SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
                }
            } catch (Throwable t) {
                log.warn("error:" + t);
            }
            try {
                final String path = _svf.getPath(_currentForm);
                final File formFile = new File(path);
                SvfForm svfForm = null;
                if (formFile.exists()) {
                    svfForm = new SvfForm(formFile);
                    if (!svfForm.readFile()) {
                        svfForm = null;
                    }
                } else {
                    throw new FileNotFoundException(_currentForm);
                }
                _formSvfFormMap.put(_currentForm, svfForm);
            } catch (Throwable t) {
                log.warn("error:" + t);
            }
        }

        public void addAttributes(final Map<String, List<String>> fieldnameAttributesMap) {
            for (final Map.Entry<String, List<String>> e : fieldnameAttributesMap.entrySet()) {
                addAttribute(e.getKey(), e.getValue());
            }
        }

        public void addAttribute(final String fieldname, final List<String> attributes) {
            final SvfField field = getField(fieldname);
            if (null == field || null == attributes) {
                return;
            }
            getMappedList(_attributeMap, fieldname).addAll(attributes);
        }

        public void addAttribute(final String fieldname, final String ...attributes) {
            final SvfField field = getField(fieldname);
            if (null == field || null == attributes) {
                return;
            }
            getMappedList(_attributeMap, fieldname).addAll(Arrays.asList(attributes));
        }
        public void setForm(final String form, final PrintData printData) {
            setForm0(form);
            setConfigForm(printData);
        }

        public SvfForm getSvfForm() {
            return _formSvfFormMap.get(_currentForm);
        }

        private void setConfigForm(final PrintData printData) {
            if (!_param._svfForms.containsKey(_currentForm)) {
                final String path = _svf.getPath(_currentForm);

                final File formFile = new File(path);

                final SvfForm svfForm = new SvfForm(formFile);
                _param._svfForms.put(_currentForm, svfForm);

                if (!svfForm.readFile()) {
                    log.warn("couldn't read form file : " + formFile);
                }
            }
            final TreeMap<String, String> modifyFlgMap = getModifyFlgMap(printData, _param._svfForms.get(_currentForm));

            String modifyFlg = Utils.mkString(modifyFlgMap, "|");
            if (!StringUtils.isBlank(modifyFlg)) {
                modifyFlg = printData._formname + ":" + modifyFlg;
            }
            if (_param._isOutputDebug) {
                log.info(" config form key = " + modifyFlg);
            }
            if (modifyFlgMap.isEmpty()) {
                return;
            }
            if (!_param._createdFormFiles.containsKey(modifyFlg)) {
                final SvfForm svfForm = _param._svfForms.get(_currentForm);
                if (null == svfForm) {
                    log.info(" svfForm null : " + _currentForm);
                } else {
                    modifyForm(printData, modifyFlgMap, svfForm);

                    try {
                        File newFormFile = svfForm.writeTempFile();

                        _param._createdFormFiles.put(modifyFlg, newFormFile.exists() ? newFormFile : null);

                    } catch (Exception e) {
                        log.error("exception!", e);
                    }
                }
            }
            final File newFormFile = _param._createdFormFiles.get(modifyFlg);
            if (null != newFormFile) {
                printData._formname = newFormFile.getName();
                setForm0(printData._formname);
            }
        }

        final String FLG_MOVE_STAMP = "MOVE_STAMP";
        final String FLG_MOVE_STAMP_POS_ADD = "MOVE_STAMP_POS_ADD";
        final String FLG_EXTEND_GRDDATE_SIZE_KEY = "EXTEND_GRDDATE_SIZE";
        final String FLG_RESIZE_STAMP = "FLG_RESIZE_STAMP";
        final String FLG_FORM_COLOR = "FLG_FORM_COLOR";
        final String FLG_BASE_FORM = "FLG_BASE_FORM";
        final String FLG_REITAKU_ENG_FOOTER_002 = "FLG_REITAKU_ENG_FOOTER_002";
        final String FLG_REITAKU_ENG_FOOTER_005 = "FLG_REITAKU_ENG_FOOTER_005";
        final String FLG_REITAKU_ENG_TEXT_J_005 = "FLG_REITAKU_ENG_TEXT_J_005";
        final String FLG_REITAKU_ENG_FOOTER_019 = "FLG_REITAKU_ENG_FOOTER_019";
        final String FLG_KWANSEI_ENG_HEIGHT_005_H = "FLG_KWANSEI_ENG_HEIGHT_005_H";
        final String FLG_KWANSEI_ENG_HEIGHT_014_H = "FLG_KWANSEI_ENG_HEIGHT_014_H";
        final String FLG_KWANSEI_ENG_HEIGHT_014_J = "FLG_KWANSEI_ENG_HEIGHT_014_J";
        final String FLG_KWANSEI_ENG_HEIGHT_021_J = "FLG_KWANSEI_ENG_HEIGHT_021_J";
        final String FLG_JYOTO_SOTSUMI_ENG_NOTE_KETA = "FLG_JYOTO_SOTSUMI_ENG_NOTE_KETA";
        final String FLG_RYUKEI_HEADER  = "FLG_RYUKEI_HEADER";
        private void modifyForm(final PrintData printData, final TreeMap<String, String> modifyFlgMap, final SvfForm svfForm) {
            // フィールド変更
            if (modifyFlgMap.containsKey(FLG_EXTEND_GRDDATE_SIZE_KEY)) {
                final int keta = 12 * 2;

                final Field grdDateField = svfForm.getField("MAIN1").copyTo("GRD_DATE")
                        .setX(0)
                        .setEndX(1050)
                        .setFieldLength(keta);

                svfForm.addField(grdDateField);
            }

            // フォームをカラーに変更
            if (modifyFlgMap.containsKey(FLG_FORM_COLOR)) {
                try {
                    svfForm.setColor(true);
                } catch (Throwable t) {
                    log.info("throw ", t);
                }
            }

            // 印影サイズ変更、印影位置移動、印影位置加算
            if (modifyFlgMap.containsKey(FLG_RESIZE_STAMP) || modifyFlgMap.containsKey(FLG_MOVE_STAMP) || modifyFlgMap.containsKey(FLG_MOVE_STAMP_POS_ADD)) {

                ImageField stamp = svfForm.getImageField("STAMP");
                if (null == stamp) {
                    log.warn("!! no field STAMP : " + _currentForm);
                } else {
                    ImageField newStamp = stamp;
                    String dotX = null, dotY = null;
                    if (modifyFlgMap.containsKey(FLG_MOVE_STAMP)) {
                        final String xy = modifyFlgMap.get(FLG_MOVE_STAMP);
                        final int comma = xy.indexOf(",");
                        final String xStr = xy.substring(0, comma);
                        final String yStr = xy.substring(comma + 1);
                        if (NumberUtils.isNumber(xStr)) {
                            final int stampx = (int) Double.parseDouble(xStr);
                            dotX = String.valueOf(stampx);
                        }
                        if (NumberUtils.isNumber(yStr)) {
                            final int stampy = (int) Double.parseDouble(yStr);
                            dotY = String.valueOf(stampy);
                        }
                    } else if (modifyFlgMap.containsKey(FLG_MOVE_STAMP_POS_ADD)) {
                        final String addxy = modifyFlgMap.get(FLG_MOVE_STAMP_POS_ADD);
                        final int comma = addxy.indexOf(",");
                        final String xStr = addxy.substring(0, comma);
                        final String yStr = addxy.substring(comma + 1);
                        if (NumberUtils.isNumber(xStr)) {
                            final int stampaddx = (int) Double.parseDouble(xStr);
                            dotX = String.valueOf(newStamp._point._x + stampaddx);
                        }
                        if (NumberUtils.isNumber(yStr)) {
                            final int stampaddy = (int) Double.parseDouble(yStr);
                            dotY = String.valueOf(newStamp._point._y + stampaddy);
                        }
                    }

                    final int x = newStamp._point._x;
                    final int y = newStamp._point._y;
                    final int endX = newStamp._endX;
                    final int endY = y + newStamp._height;
                    final int l = NumberUtils.isNumber(printData._stampSizeMm) ? Utils.mmToDot(printData._stampSizeMm) : newStamp._height;
                    final int newX;
                    final int newEndX;
                    if (NumberUtils.isNumber(dotX)) {
                        newX = Utils.toInt(dotX, 0);
                        newEndX = newX + l;
                    } else {
                        final int centerX = (x + endX) / 2;
                        newX = centerX - l / 2;
                        newEndX = centerX + l / 2;
                    }
                    final int newY;
                    if (NumberUtils.isNumber(dotY)) {
                        newY = Utils.toInt(dotY, 0);
                    } else {
                        final int centerY = (y + endY) / 2;
                        newY = centerY - l / 2;
                    }
                    final int newHeight = l;

                    newStamp = newStamp.setX(newX).setY(newY).setEndX(newEndX).setHeight(newHeight);
                    final BigDecimal xmm = Utils.dotToMm(String.valueOf(x));
                    final BigDecimal ymm = Utils.dotToMm(String.valueOf(y));
                    final BigDecimal hmm = Utils.dotToMm(String.valueOf(newStamp._height));
                    final BigDecimal newXmm = Utils.dotToMm(String.valueOf(newX));
                    final BigDecimal newYmm = Utils.dotToMm(String.valueOf(newY));
                    final BigDecimal newHmm = Utils.dotToMm(String.valueOf(l));
                    log.info("move stamp (x=" + x + "(" + xmm + "mm), y=" + y + "(" + ymm + "mm), len = " + newStamp._height + "(" + hmm + "mm)) ");
                    log.info("        to (x=" + newX + "(" + newXmm + "mm), y=" + newY + "(" + newYmm + "mm), len = " + l + "(" + newHmm + "mm))");

                    if (stamp != newStamp) {
                        svfForm.move(stamp, newStamp);
                    }
                }
            }

            if (modifyFlgMap.containsKey(FLG_BASE_FORM)) {
                if ("KNJG010_1KYOTO.frm".equals(modifyFlgMap.get(FLG_BASE_FORM))) {
                    svfForm.addField(new SvfForm.Field(null, "REMARK", SvfForm.Font.Mincho, 68, 2971, false, new SvfForm.Point(138, 4118), 150, "備考"));
                }
            }

            if (modifyFlgMap.containsKey(FLG_REITAKU_ENG_FOOTER_002) || modifyFlgMap.containsKey(FLG_REITAKU_ENG_FOOTER_005) || modifyFlgMap.containsKey(FLG_REITAKU_ENG_FOOTER_019)) {
                final SvfForm.Field STAFFNAME = svfForm.getField("STAFFNAME");
                Line line = svfForm.getNearestUpperLine(STAFFNAME.getPoint().setX((STAFFNAME.getPoint()._x + STAFFNAME._endX) / 2));
                svfForm.removeLine(line);
                if (modifyFlgMap.containsKey(FLG_REITAKU_ENG_FOOTER_002)) {
                    final int x = 1000;
                    svfForm.addLine(new SvfForm.Line(line.getPoint().setX(x), line.getPoint().setX(2500)));
                    // フッターの校長名、職名、学校名を中央に移動
                    for (final String fieldname : Arrays.asList("STAFFNAME", "JOBNAME1", "JOBNAME2", "SCHOOLNAME2_1", "SCHOOLNAME2_2")) {
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        svfForm.removeField(field);
                        svfForm.addField(field.setX(x));
                    }

                    // 右にSEAL欄とその上に線を追加
                    svfForm.addLine(new SvfForm.Line(line.getPoint().setX(2700), line.getPoint().setX(3100)));
                    svfForm.addKoteiMoji(new KoteiMoji("SEAL", STAFFNAME.getPoint().setX(2700), STAFFNAME._charPoint10));

                } else if (modifyFlgMap.containsKey(FLG_REITAKU_ENG_FOOTER_005)) {

                    if (modifyFlgMap.containsKey(FLG_REITAKU_ENG_TEXT_J_005)) {
                        final SvfForm.Field NOTE2 = svfForm.getField("NOTE2");
                        if (null != NOTE2) {
                            svfForm.removeField(NOTE2);
                            svfForm.addField(NOTE2.setHenshuShiki(StringUtils.replace(NOTE2._editEqn, "High School", "Junior High School")).setFieldLength(NOTE2._fieldLength + 7));
                        }
                    }

                    final int x = 1100;
                    // 日付、シグネチャを中央に移動
                    final int diff = x - svfForm.getKoteiMojiListWithText("Date").get(0)._point._x;
                    final int dy1 = -50;
                    for (final String text : Arrays.asList("Date", "Signature")) {
                        for (final SvfForm.KoteiMoji moji : svfForm.getKoteiMojiListWithText(text)) {
                            svfForm.removeKoteiMoji(moji);
                            svfForm.addKoteiMoji(moji.addX(diff).setEndX(moji._endX + diff).addY(dy1));
                        }
                    }
                    final SvfForm.Field DATE = svfForm.getField("DATE");
                    svfForm.removeField(DATE);
                    svfForm.addField(DATE.addX(diff).addY(dy1));

                    final int dy2 = 30;
                    // フッターの校長名、職名、学校名を中央に移動
                    for (final String fieldname : Arrays.asList("STAFFNAME", "JOBNAME_JP", "STAFFNAME_JP")) {
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        svfForm.removeField(field);
                        svfForm.addField(field.setX(x).addY(dy2));
                    }

                    // JOBNAME2に"Principal"をセット
                    final SvfForm.Field JOBNAME2 = svfForm.getField("JOBNAME2");
                    svfForm.removeField(JOBNAME2);
                    svfForm.addField(JOBNAME2.setX(x).setCharPoint10(130).addY(dy2 + 10).setHenshuShiki("\"\"Principal\"\""));

                    // SCHOOLNAME_JPを下に移動
                    final SvfForm.Field SCHOOLNAME_JP = svfForm.getField("SCHOOLNAME_JP");
                    svfForm.removeField(SCHOOLNAME_JP);
                    svfForm.addField(SCHOOLNAME_JP.setX(x).setCharPoint10(130).addY(dy2 + 40));

                    line = line.addY(dy2);
                    svfForm.addLine(new SvfForm.Line(line.getPoint().setX(x), line.getPoint().setX(2500)));

                    // 右にSEAL欄とその上に線を追加
                    svfForm.addLine(new SvfForm.Line(line.getPoint().setX(2700), line.getPoint().setX(3100)));
                    svfForm.addKoteiMoji(new KoteiMoji("SEAL", STAFFNAME.getPoint().setX(2700).addY(dy2), STAFFNAME._charPoint10));
                } else if (modifyFlgMap.containsKey(FLG_REITAKU_ENG_FOOTER_019)) {
                    final int x = 1050;
                    // 固定文言カット
                    for (final KoteiMoji moji : svfForm.getKoteiMojiListWithText("TO WHOM IT MAY CONCERN:")) {
                        svfForm.removeKoteiMoji(moji);
                    }
                    // "Principal"を移動
                    for (final KoteiMoji moji : svfForm.getKoteiMojiListWithText("Principal")) {
                        svfForm.move(moji, moji.setPoint(moji._point.setX(x)).setBold(false));
                    }

                    // フィールド削除
                    for (final String fieldname : Arrays.asList("SCHOOLNAME3_1", "SCHOOLNAME3_2", "SCHOOLADDRESS1", "SCHOOLADDRESS2")) {
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        if (null != field) {
                            svfForm.removeField(field);
                        }
                    }
                    final int dy2 = 20;
                    // フッターの校長名、職名、学校名を中央に移動
                    for (final String fieldname : Arrays.asList("STAFFNAME", "SCHOOLNAME2_1", "SCHOOLNAME2_2")) {
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        svfForm.removeField(field);
                        svfForm.addField(field.setX(x).addY(dy2));
                    }
                    line = line.addY(dy2 + 20);
                    svfForm.addLine(new SvfForm.Line(line.getPoint().setX(x), line.getPoint().setX(2400)));

                    // 右にSEAL欄とその上に線を追加
                    svfForm.addLine(new SvfForm.Line(line.getPoint().setX(2600), line.getPoint().setX(3000)));
                    svfForm.addKoteiMoji(new KoteiMoji("SEAL", STAFFNAME.getPoint().setX(2600).addY(dy2), STAFFNAME._charPoint10));
                }
            }

            if (modifyFlgMap.containsKey(FLG_KWANSEI_ENG_HEIGHT_005_H) || modifyFlgMap.containsKey(FLG_KWANSEI_ENG_HEIGHT_014_H) || modifyFlgMap.containsKey(FLG_KWANSEI_ENG_HEIGHT_014_J) || modifyFlgMap.containsKey(FLG_KWANSEI_ENG_HEIGHT_021_J)) {
                final int y = 300;
                final int maxY = 4500;
                final int startY;
                final int startY2;
                if (modifyFlgMap.containsKey(FLG_KWANSEI_ENG_HEIGHT_014_H)) {
                    startY = 350;
                    startY2 = 860;
                } else if (modifyFlgMap.containsKey(FLG_KWANSEI_ENG_HEIGHT_014_J)) {
                    startY = 330;
                    startY2 = 1050;
                } else if (modifyFlgMap.containsKey(FLG_KWANSEI_ENG_HEIGHT_021_J)) {
                    startY2 = 1050;
                    startY = 400;
                } else { // modifyFlgMap.containsKey(FLG_KWANSEI_ENG_STARTY_H_005) {
                    startY = 400;
                    startY2 = 850;
                }
                final double rate = (maxY - startY2) * 1.0 / (maxY - startY);
                // プレ印刷用に発行番号以外の印字位置変更
                for (final SvfForm.Field field : svfForm.getElementList(SvfForm.Field.class)) {
                    if ("CERTIF_NAME".equals(field._fieldname)) {
                        svfForm.removeField(field);
                        svfForm.addField(field.addY(-30));
                    } else if (y <= field._position._y) {
                        svfForm.removeField(field);
                        svfForm.addField(field.setY(startY2 + (int) ((field._position._y - startY) * rate)));
                    }
                }
                for (final SvfForm.KoteiMoji moji : svfForm.getElementList(SvfForm.KoteiMoji.class)) {
                    if (y <= moji._point._y) {
                        svfForm.removeKoteiMoji(moji);
                        svfForm.addKoteiMoji(moji.setY(startY2 + (int) ((moji._point._y - startY) * rate)));
                    }
                }
                for (final SvfForm.Line line : svfForm.getElementList(SvfForm.Line.class)) {
                    if (y <= line._start._y) {
                        svfForm.removeLine(line);
                        svfForm.addLine(line.setY(startY2 + (int) ((line._start._y - startY) * rate + 15)));
                    }
                }
                for (final SvfForm.ImageField image : svfForm.getElementList(SvfForm.ImageField.class)) {
                    if (y <= image._point._y) {
                        svfForm.removeImageField(image);;
                        svfForm.addImageField(image.setY(startY2 + (int) ((image._point._y - startY) * rate)));
                    }
                }
            }

            if (modifyFlgMap.containsKey(FLG_JYOTO_SOTSUMI_ENG_NOTE_KETA)) {
                for (final String fieldname : Arrays.asList("NOTE1", "NOTE2", "NOTE3", "NOTE4")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                        svfForm.addField(field.addX(100).setFieldLength(field._fieldLength - 4).setEndX(field._endX));
                    }
                }
            }
            if (modifyFlgMap.containsKey(FLG_RYUKEI_HEADER)) {
                final int x = new BigDecimal(Utils.mmToDot("35.0")).subtract(svfForm.getAdjtX()).intValue();
                final int y = new BigDecimal(Utils.mmToDot("12.0")).subtract(svfForm.getAdjtY()).intValue();
                final int width = 160;
                final int charPoint9p = 90;
                final SvfForm.Font font = SvfForm.Font.Mincho;
                for (final SvfForm.Field f : Arrays.asList(
                        new SvfForm.Field(null, "HEADER_SCHREGNO", font, 8, x + width, false, new SvfForm.Point(x, y), charPoint9p, "ヘッダ学籍番号")
                      , new SvfForm.Field(null, "HEADER_NAME", font, 30, x + 50 + width, false, new SvfForm.Point(x + 50, y + 80), charPoint9p, "ヘッダ氏名")
                      , new SvfForm.Field(null, "HEADER_DATE", font, 16, x + width, false, new SvfForm.Point(x, y + 80 * 2), charPoint9p, "ヘッダ日付")
                        )) {
                    svfForm.addField(f);
                }
            }
        }

        private TreeMap<String, String> getModifyFlgMap(final PrintData printData, final SvfForm svfForm) {
            final TreeMap<String, String> modifyFlgMap = new TreeMap<String, String>();
            String propX = null;
            String propY = null;
            if (printData._ck.isPrintSotsugyo()) {
                propX = printData.getProperty(Property.stampPositionXmmSotsugyoShomeisho);
                propY = printData.getProperty(Property.stampPositionYmmSotsugyoShomeisho);
            } else if (printData._ck.isPrintSotsugyoMikomi()) {
                propX = printData.getProperty(Property.stampPositionXmmSotsugyoMikomiShomeisho);
                propY = printData.getProperty(Property.stampPositionYmmSotsugyoMikomiShomeisho);
            } else if (printData._ck.isPrintZaigaku()) {
                propX = printData.getProperty(Property.stampPositionXmmZaigakuShomeisho);
                propY = printData.getProperty(Property.stampPositionYmmZaigakuShomeisho);
            } else if (printData._ck.isPrintZaiseki()) {
                propX = printData.getProperty(Property.stampPositionXmmZaisekiShomeisho);
                propY = printData.getProperty(Property.stampPositionYmmZaisekiShomeisho);
            } else if (printData._ck.in(CK._2_SOTSU_ENG)) {
                propX = printData.getProperty(Property.stampPositionXmmSotsugyoShomeishoEng);
                propY = printData.getProperty(Property.stampPositionYmmSotsugyoShomeishoEng);
            } else if (printData._ck.in(CK._5_ZAIGAKU_ENG)) {
                propX = printData.getProperty(Property.stampPositionXmmZaigakuShomeishoEng);
                propY = printData.getProperty(Property.stampPositionYmmZaigakuShomeishoEng);
            } else if (printData._ck.in(CK._19_SOTSUMI_ENG)) {
                propX = printData.getProperty(Property.stampPositionXmmSotsugyoMikomiShomeishoEng);
                propY = printData.getProperty(Property.stampPositionYmmSotsugyoMikomiShomeishoEng);
            }

            if (NumberUtils.isNumber(propX) || NumberUtils.isNumber(propY)) {
                int ignoreFormChouseiX = 0;
                int ignoreFormChouseiY = 0;
                if ("1".equals(printData.getProperty(Property.stampPositionIgnoreSvfInjiIchiChousei))) {
                    ignoreFormChouseiX = svfForm.getAdjtX().intValue();
                    ignoreFormChouseiY = svfForm.getAdjtY().intValue();
                    log.info(" ignoreFormChousei x = " + ignoreFormChouseiX + " (" + Utils.dotToMm(String.valueOf(ignoreFormChouseiX)) + "), y = " + ignoreFormChouseiY + " (" + Utils.dotToMm(String.valueOf(ignoreFormChouseiY)) + ")");
                }
                final String xDot = NumberUtils.isNumber(propX) ? String.valueOf(Utils.mmToDot(propX) - ignoreFormChouseiX) : "NA";
                final String yDot = NumberUtils.isNumber(propY) ? String.valueOf(Utils.mmToDot(propY) - ignoreFormChouseiY) : "NA";
                modifyFlgMap.put(FLG_MOVE_STAMP, xDot + "," + yDot);
            } else if (_param._z010.isKaichiSpec()) {
                if (printData._ck.isPrintSotsugyo()) {
                    modifyFlgMap.put(FLG_EXTEND_GRDDATE_SIZE_KEY, "1");
                }
            } else if (_param._z010.in(Z010.Sakae)) {
                if (printData._ck.isPrintZaigaku()) {
                    modifyFlgMap.put(FLG_MOVE_STAMP_POS_ADD, "0,125");
                }
            } else if (_param._z010.in(Z010.Matsudo)) {
                if (printData._ck.isPrintSotsugyo()) {
                    modifyFlgMap.put(FLG_MOVE_STAMP, "2384,3584");
                } else if (printData._ck.isPrintSotsugyoMikomi()) {
                    modifyFlgMap.put(FLG_MOVE_STAMP, "2384,3584");
                } else if (printData._ck.isPrintZaigakuEng()) {
                    modifyFlgMap.put(FLG_MOVE_STAMP, "2704,3512");
                }
            } else if (_param._z010.in(Z010.Ritsumeikan)) {
                if (printData._isFormForStamp) {
                    modifyFlgMap.put(FLG_MOVE_STAMP_POS_ADD, Utils.mmToDot("20") + "," + Utils.mmToDot("20")); // 20mm右, 20mm下
                }
            }
            if (NumberUtils.isNumber(printData._stampSizeMm)) {
                modifyFlgMap.put(FLG_RESIZE_STAMP, "1");
            }

//            if (printData._isPrintStamp && null != printData._setCertifSchoolstampImagePath) {
//                modifyFlgMap.put(FLG_FORM_COLOR, "1");
//            }

            if (Arrays.asList("KNJG010_1.frm", Form.KNJG010_1KYOTO).contains(printData._formname)) {
//    			fields.add(new SvfForm.Field(null, "SYOSYO_NAME", mincho, 100, 2975, false, pt(753, 137), 80, "証書名").setMaskEnabled(SvfForm.Field.MaskFlag.MASK));
//    			fields.add(new SvfForm.Field(null, "CERTIF_NO", mincho, 8, 2158, false, pt(1980, 255), 80, "証明書番号").setMaskEnabled(SvfForm.Field.MaskFlag.MASK));
//    			fields.add(new SvfForm.Field(null, "SYOSYO_NAME2", mincho, 20, 2632, false, pt(2188, 255), 80, "年度、学校略称").setMaskEnabled(SvfForm.Field.MaskFlag.MASK));
//    			fields.add(new SvfForm.Field(null, "CERTIF_NAME", mincho, 130, 4452, false, pt(119, 382), 120, "証書名、証書番号等編集").setHenshuShiki("SYOSYO_NAME + CERTIF_NO + SYOSYO_NAME2"));
//    			fields.add(new SvfForm.Field(null, "TITLE", mincho, 14, 2074, false, pt(1056, 899), 261, "本文").setBold(true));
//    			fields.add(new SvfForm.Field(null, "TITLE_NAME", mincho, 8, 1637, false, pt(1424, 1479), 96, "生年月日").setHenshuShiki("\\"氏　　名\\""));
//    			fields.add(new SvfForm.Field(null, "BIRTHDAY", mincho, 22, 2368, false, pt(1733, 1654), 104, "生年月日"));
//    			fields.add(new SvfForm.Field(null, "TITLE_BIRTHDAY", mincho, 8, 1637, false, pt(1424, 1657), 96, "生年月日").setHenshuShiki("\\"生年月日\\""));
//        		fields.add(new SvfForm.Field(null, "POINT", mincho, 2, 1974, false, pt(1930, 2077), 80, "句点").setMaskEnabled(SvfForm.Field.MaskFlag.MASK));
//        		fields.add(new SvfForm.Field(null, "MAIN1", mincho, 70, 4203, false, pt(315, 2221), 200, "本文"));
//        		fields.add(new SvfForm.Field(null, "MAIN2", mincho, 45, 2816, false, pt(316, 2492), 200, "本文").setHenshuShiki("MAIN2 + POINT"));
//        		fields.add(new SvfForm.Field(null, "DATE", mincho, 19, 1178, false, pt(397, 2998), 148, "証明日付"));
//        		fields.add(new SvfForm.Field(null, "SCHOOLNAME", mincho, 60, 3129, false, pt(629, 3484), 150, "学校名"));
//        		fields.add(new SvfForm.Field(null, "STAFFNAME", mincho, 60, 3129, false, pt(629, 3718), 150, "職員名"));
//        		fields.add(new SvfForm.Field(null, "JOBNAME", mincho, 60, 3129, false, pt(629, 3720), 150, "役職名"));
//    			fields.add(new SvfForm.Field(null, "NAME1", mincho, 24, 2779, false, pt(1733, 1464), 157, "氏名"));
//        		fields.add(new SvfForm.Field(null, "NAME2", mincho, 40, 2888, false, pt(1733, 1479), 104, "氏名"));
//        		fields.add(new SvfForm.Field(null, "NAME11", mincho, 24, 2779, false, pt(1733, 1552), 157, "氏名"));
//        		fields.add(new SvfForm.Field(null, "NAME12", mincho, 40, 2888, false, pt(1733, 1566), 104, "氏名"));

                // KNJG010_1.frm
//        		fields.add(new SvfForm.Field(null, "TITLE2", mincho, 24, 2541, false, pt(605, 892), 290, "本文").setBold(true));
//        		fields.add(new SvfForm.Field(null, "NAME3", mincho, 46, 2882, false, pt(1732, 1482), 90, "氏名"));
//        		fields.add(new SvfForm.Field(null, "NAME4", mincho, 50, 2884, false, pt(1732, 1484), 83, "氏名"));
//        		fields.add(new SvfForm.Field(null, "NAME5", mincho, 50, 2871, false, pt(1718, 1462), 83, "氏名").setLinkFieldname("NAME5_2"));
//        		fields.add(new SvfForm.Field(null, "NAME5_2", mincho, 50, 2871, false, pt(1718, 1504), 83, "氏名"));
//        		fields.add(new SvfForm.Field(null, "NAME13", mincho, 46, 2882, false, pt(1732, 1574), 90, "氏名"));
//        		fields.add(new SvfForm.Field(null, "NAME14", mincho, 50, 2884, false, pt(1732, 1576), 83, "氏名"));
//        		fields.add(new SvfForm.Field(null, "NAME15", mincho, 50, 2869, false, pt(1716, 1554), 83, "氏名").setLinkFieldname("NAME15_2"));
//        		fields.add(new SvfForm.Field(null, "NAME15_2", mincho, 50, 2868, false, pt(1716, 1596), 83, "氏名"));
//        		fields.add(new SvfForm.Field(null, "SCHOOL_ADDR", mincho, 69, 3121, false, pt(629, 3264), 130, "学校住所"));
//        		fields.add(new SvfForm.Field(null, "JOBNAME_S", mincho, 90, 3132, false, pt(632, 3745), 100, "役職名"));

                // KNJG010_1KYOTO.frm
//        		fields.add(new SvfForm.Field(null, "REMARK", mincho, 68, 2971, false, pt(138, 4118), 150, "備考"));
                if (_param._z010.in(Z010.Kyoto)) {
                    modifyFlgMap.put(FLG_BASE_FORM, "KNJG010_1KYOTO.frm");
                }
            }
            if (_param._z010.in(Z010.Reitaku)) {
                if (printData._ck.in(CK._2_SOTSU_ENG, CK._32_SOTSU_J_ENG)) {
                    modifyFlgMap.put(FLG_REITAKU_ENG_FOOTER_002, "1");
                } else if (printData._ck.in(CK._5_ZAIGAKU_ENG)) {
                    modifyFlgMap.put(FLG_REITAKU_ENG_FOOTER_005, "1");
                    if (J.equals(printData._certifKindSchoolKind)) {
                        modifyFlgMap.put(FLG_REITAKU_ENG_TEXT_J_005, "1");
                    }
                } else if (printData._ck.in(CK._19_SOTSUMI_ENG)) {
                    modifyFlgMap.put(FLG_REITAKU_ENG_FOOTER_019, "1");
                }
            }
            if (_param._z010.in(Z010.kwansei)) {
                if (printData._ck.in(CK._5_ZAIGAKU_ENG)) {
                    modifyFlgMap.put(FLG_KWANSEI_ENG_HEIGHT_005_H, "1");
                } else if (printData._ck.in(CK._14_ZAISEKI_ENG)) {
                    if ("J".equals(printData._certifKindSchoolKind)) {
                        modifyFlgMap.put(FLG_KWANSEI_ENG_HEIGHT_014_J, "1");
                    } else {
                        modifyFlgMap.put(FLG_KWANSEI_ENG_HEIGHT_014_H, "1");
                    }
                } else if (printData._ck.in(CK._21_ZAIGAKU_J_ENG)) {
                    modifyFlgMap.put(FLG_KWANSEI_ENG_HEIGHT_021_J, "1");
                }
            }
            if (_param._z010.in(Z010.jyoto)) {
                if (printData._ck.in(CK._19_SOTSUMI_ENG)) {
                    modifyFlgMap.put(FLG_JYOTO_SOTSUMI_ENG_NOTE_KETA, "1");
                }
            } else if (_param._z010.in(Z010.ryukei)) {
                if (printData._ck.isPrintSotsugyo() || printData._ck.isPrintZaigaku() || printData._ck.isPrintZaigakuEng() || printData._ck.isPrintSotsugyoMikomi() || printData._ck.in(CK._19_SOTSUMI_ENG, CK._2_SOTSU_ENG)) {
                    modifyFlgMap.put(FLG_RYUKEI_HEADER, "1");
                }
            }
            return modifyFlgMap;
        }

        public int getEndX(final String fieldname, final int defaultX2) {
            try {
                final SvfForm svfForm = _formSvfFormMap.get(_currentForm);
                if (null != svfForm) {
                    final Field field = svfForm.getField(fieldname);
                    if (null == field) {
                        log.warn(" not found field : " + fieldname);
                    } else {
                        return field._endX;
                    }
                }
            } catch (Throwable t) {
                log.error("exception : field = " + fieldname, t);
            }
            return defaultX2;
        }


        String attributeIntPlus(final String fieldname, final String intProperty, final int plus) {
            final SvfField field = getField(fieldname);
            if (null == field) {
                log.warn(" not found " + fieldname + " in " + currentFormInfoMap().keySet());
                return null;
            }
            final int propVal = Utils.toInt((String) field.getAttributeMap().get(intProperty), INVALID_X);
            return intProperty + "=" + String.valueOf(propVal + plus);
        }

        public int VrAttribute(final String fieldname, final String attribute) {
            if (null == fieldname) {
                return -1;
            }
            final SvfField field = getField(fieldname);
            if (null == field) {
                if (_param._isOutputDebugField) {
                    log.warn(" not found " + fieldname + " in " + currentFormInfoMap().keySet());
                } else if (_param._isOutputDebug) {
                    log.warn(" not found " + fieldname);
                }
                return -999999;
            }
            if (_param._isOutputDebugField) {
                log.info("VrAttribute(" + fieldname + ", " + (null == attribute ? null : "\"" + attribute + "\"") + ")");
            }
            return _svf.VrAttribute(fieldname, attribute);
        }

        public int VrsOut(final String fieldname, final String data) {
            final SvfField field = getField(fieldname);
            if (null == field) {
                if (_param._isOutputDebugField) {
                    log.warn(" not found " + fieldname + " in " + currentFormInfoMap().keySet());
                } else if (_param._isOutputDebug) {
                    log.warn(" not found " + fieldname);
                }
                return -999999;
            }
            if (_param._isOutputDebugField) {
                log.info("VrsOut(" + fieldname + ", " + (null == data ? null : "\"" + data + "\"") + ")");
            }
            return _svf.VrsOut(fieldname, data);
        }

        private int VrsOutSelectField(final List<String> fields, final String data) {
            return VrsOut(getFieldForData(fields, data), data);
        }

        public int VrImageOut(final String fieldname, final String path) {
            if (null == path) {
                return -1;
            }
            return VrsOut(fieldname, path);
        }

        public void VrEndPage() {
            if (_param._isOutputDebugField) {
                log.info("VrEndPage()");
            }
            _svf.VrEndPage();
        }

        private SvfField getField(final String fieldname) {
            if (null == fieldname) {
                return null;
            }
            final Map<String, SvfField> map = currentFormInfoMap();
            if (null == map || map.isEmpty()) {
                return null;
            }
            SvfField field = map.get(fieldname);
            return field;
        }

        private Map<String, SvfField> currentFormInfoMap() {
            final Map<String, SvfField> map = _formnameSvfFormInfoMapMap.get(_currentForm);
            if (null == map) {
                return Collections.emptyMap();
            }
            return map;
        }

        private int getFieldKeta(final String fieldname, final int defaultKeta) {
            final SvfField field = getField(fieldname);
            if (null != field) {
                final int keta = Utils.toInt((String) field.getAttributeMap().get(SvfField.AttributeKeta), defaultKeta);
                return keta;
            }
            return defaultKeta;
        }

        private int getFieldKeta(final String fieldname) {
            return getFieldKeta(fieldname, -1);
        }

        private String getFieldForData(final List<String> fields, final String data) {
            final int dataKeta = getMS932ByteLength(data);
            String formFieldName = null;
            String ketteiField = null;
            String kouho = null;
            for (int i = 0; i < fields.size(); i++) {
                final int fieldKeta = getFieldKeta(fields.get(i));
                if (0 < fieldKeta) {
                    if (dataKeta <= fieldKeta) {
                        ketteiField = fields.get(i);
                        break;
                    }
                    kouho = fields.get(i);
                }
            }
            if (null != ketteiField) {
                formFieldName = ketteiField;
            } else if (null != kouho) {
                formFieldName = kouho;
            }
            if (_param._isOutputDebug) {
                log.info(" fields " + fields + ", field = " + formFieldName + " (ketteiField = " + ketteiField + ", kouho = " + kouho + ", data = " + data + ", keta = " + dataKeta + ")");
            }
            return formFieldName;
        }
    }

    private void printPersonalInfoCommon(final Form form, final PrintData printData) {
        if (_param._z010.in(Z010.ryukei)) {
            if (printData._ck.isPrintSotsugyo() || printData._ck.isPrintZaigaku() || printData._ck.isPrintZaigakuEng() || printData._ck.isPrintSotsugyoMikomi() || printData._ck.in(CK._19_SOTSUMI_ENG, CK._2_SOTSU_ENG)) {
                form.VrsOut("HEADER_SCHREGNO", printData._schregno);
                if (null != printData._personalInfo) {
                    if (printData._ck._isEnglish) {
                        form.VrsOut("HEADER_NAME", printData._personalInfo.getNameEng(_param));
                    } else {
                        form.VrsOut("HEADER_NAME", printData._personalInfo._name);
                    }
                }
                if (printData._ck._isEnglish) {
                    form.VrsOut("HEADER_DATE", new SimpleDateFormat("yyyy/MM/dd").format(new Date()));
                } else {
                    form.VrsOut("HEADER_DATE", KNJ_EditDate.h_format_SeirekiJP(new SimpleDateFormat("yyyy-MM-dd").format(new Date())));
                }
            }
        }
    }

    private void printSchoolCommon(final Form form, final PrintData printData) {
        form.VrsOut("PHONE", printData._schoolMstSchooltelno);
        form.VrsOut("FAX", printData._schoolMstSchoolfaxno);

        if (null != printData._schoolInfo) {
            form.VrsOut("SYOSYO_NAME",  printData._schoolInfo._syosyoName);  //証書名
            form.VrsOut("SYOSYO_NAME2", printData._schoolInfo._syosyoNam2); //証書名２
            printData.setCertifNo(_param);
            form.VrsOut("CERTIF_NO", printData._certifNo);
            if (null != printData._certifNo) {
                form.VrsOut("NENDO_NAME",  printData._certifNo + defstr(printData._schoolInfo._syosyoName));  // 証書番号を出力
            }
        }

        if (_param._z010.in(Z010.Nagisa)) {
            if (printData._ck.isPrintSotsugyo() || printData._ck.isPrintZaigaku()) {
                form.VrAttribute("CERTIF_NAME", Attribute.charSize(14.0));
            }
        }
    }

    private void printSchoolEng(final Form form, final PrintData printData, final String certifDateFormat) {
        if (null == printData._schoolInfo) {
            return;
        }

        if (_param._z010.in(Z010.Sapporo)) {
            form.VrsOut("DATE", "Date: " + _param.formatDateUs(printData._certifDate, d_MONTHFULL_yyyy_sapporo, "     "));  // 証明日付
            form.VrsOut("JOBNAME", defstr(printData._schoolInfo._remark6, "Principal"));
            form.VrsOut("STAFFNAME", printData._schoolInfo._remark1);                //代表名（英）
            form.VrsOut("SCHOOLNAME", defstr(printData._schoolInfo._remark2) + Utils.prepend("　", printData._schoolInfo._remark3));                    //学校名称（英）
            return;
        }

        final boolean isZaigaku = printData._ck.isPrintZaigakuEng();

        // 証明日付
        if (!isZaigaku && _param._z010.in(Z010.Meiji)) {
            form.VrsOut("ISSUE_DATE_TITLE", "Date：");
        }
        String dateHeader = "";
        if (isZaigaku && _param._z010.in(Z010.Sundaikoufu)) {
            dateHeader = "Date of issue : ";
        }
        form.VrsOut("DATE", dateHeader + _param.formatDateUs(printData._certifDate, certifDateFormat, dateBlankString()));

        if (isZaigaku) {
            if (CK._5_ZAIGAKU_ENG == printData._ck) {
                form.VrsOut("HJDIV", "High School");
            } else {
                form.VrsOut("HJDIV", "Junior High School");
            }
//            if (_isMusashi) {
//                form.VrsOut("SCHOOLNAME1", param._schoolInfo._remark3);                    //学校名称（英）
//                form.VrsOut("SCHOOLNAME2_1", param._schoolInfo._remark3.toUpperCase());                    //学校名称（英）
//                form.VrsOut("SCHOOLNAME3_2", param._schoolInfo._remark3.toUpperCase());                    //学校名称（英）
//            } else if (schoolmarkTok()) {
            if (_param._z010.in(Z010.Sakae)) {
                form.VrsOut("SCHOOLNAME", printData._schoolInfo._remark2);                    //学校名称（英）
                form.VrsOut("SCHOOLNAME2", printData._schoolInfo._remark3);                    //学校名称（英）
            } else if (_param._z010.in(Z010.Meikei)) {
                form.VrsOut("SCHOOLNAME1", PrintData.trim(defstr(printData._schoolInfo._remark2) + Utils.prepend("　", printData._schoolInfo._remark3)));                    //学校名称（英）
                form.VrsOut("SCHOOLNAME2_1", defstr(printData._schoolInfo._remark2) + Utils.prepend("　", printData._schoolInfo._remark3));                    //学校名称（英）
            } else if (_param._z010.in(Z010.Kindai)) {
                form.VrsOut("SCHOOLNAME", printData._schoolInfo._schoolnameEng);                    //学校名称（英）
            } else {
                form.VrsOut("SCHOOLNAME", defstr(printData._schoolInfo._remark2) + Utils.prepend("　", printData._schoolInfo._remark3));                    //学校名称（英）
            }

            form.VrsOut("CORP_NAME", printData._schoolInfo._remark7);
        } else {
            final boolean isMieSotsugyoEng = _param._z010.in(Z010.Mieken) && printData._ck.in(CK._2_SOTSU_ENG);

            final boolean mieRemark2remark3isBlank = _param._z010.in(Z010.Mieken) && StringUtils.isBlank(printData._schoolInfo._remark2) && StringUtils.isBlank(printData._schoolInfo._remark3);
            final String schoolName;
            if (mieRemark2remark3isBlank) {
                schoolName = printData._schoolMstSchoolnameEng;
            } else {
                schoolName = defstr(printData._schoolInfo._remark2) + Utils.prepend("　", printData._schoolInfo._remark3);
            }
            final String schoolName1;
//        if (form2) {
//            schoolName1 = printData._schoolInfo._remark3;
//        } else if (schoolmarkTok()) {
            if (_param._z010.in(Z010.Kindai)) {
                schoolName1 = printData._schoolInfo._schoolnameEng;
            } else {
                if (isMieSotsugyoEng) {
                    if (!StringUtils.isBlank(printData._personalInfo._majorEng)) { // 学科名英字があれば学校名英字の代わりに出力
                        schoolName1 = printData._personalInfo._majorEng;
                    } else {
                        schoolName1 = schoolName;
                    }
                } else {
                    schoolName1 = schoolName;
                }
            }
            if (_param._z010 == Z010.Meiji && (printData._ck.in(CK._2_SOTSU_ENG, CK._19_SOTSUMI_ENG, CK._32_SOTSU_J_ENG))) {
                form.VrsOut("SCHOOLNAME1", PrintData.trim(schoolName1));                    //学校名称（英）
            } else {
                form.VrsOutSelectField(Arrays.asList("SCHOOLNAME1",  "SCHOOLNAME1_2", "SCHOOLNAME1_3"), schoolName1);                    //学校名称（英）
            }
            form.VrsOut("SCHOOLNAME2", schoolName1);                    //学校名称（英）
            if (mieRemark2remark3isBlank) {
                form.VrsOut("SCHOOLNAME3_1", schoolName);                         //学校名称（英）
            } else {
                form.VrsOut("SCHOOLNAME3_1", printData._schoolInfo._remark2);                         //学校名称（英）
                form.VrsOut("SCHOOLNAME3_2", printData._schoolInfo._remark3);                         //学校名称（英）
            }
            if (mieRemark2remark3isBlank) {
            } else {
                form.VrsOut("SCHOOLNAME2_1", printData._schoolInfo._remark2);                  //学校名（和）
                form.VrsOut("SCHOOLNAME2_2", printData._schoolInfo._remark3);                  //学校名（和）
            }
            if (_param._z010.in(Z010.Sakae)) {
                form.VrsOut("JOBNAME1", defstr(printData._schoolInfo._remark6, "Principal"));
                form.VrsOut("CORP_NAME", printData._schoolInfo._remark7);
            }
        }

        if (_param._z010.in(Z010.jyoto) && printData._ck.in(CK._2_SOTSU_ENG, CK._5_ZAIGAKU_ENG, CK._19_SOTSUMI_ENG)) {
            form.VrsOut("REMARK1", printData._schoolInfo._remark1);
            form.VrsOut("REMARK2", printData._schoolInfo._remark2);
            form.VrsOut("REMARK3", printData._schoolInfo._remark3);
            form.VrsOut("REMARK4", printData._schoolInfo._remark4);
            form.VrsOut("REMARK5", printData._schoolInfo._remark5);
            form.VrsOut("REMARK6", printData._schoolInfo._remark6);
        }

        if (isZaigaku) {
            form.VrsOut("SCHOOL_ADDRESS1", printData._schoolInfo._remark4);                        //学校名称（英）
            form.VrsOut("SCHOOL_ADDRESS2", printData._schoolInfo._remark5);                        //学校名称（英）
        } else {
            form.VrsOut("SCHOOLADDRESS1", printData._schoolInfo._remark4);                        //学校名称（英）
            form.VrsOut("SCHOOLADDRESS2", printData._schoolInfo._remark5);                        //学校名称（英）
            final boolean isMieSotsugyoEng = _param._z010 == Z010.Mieken && CK._2_SOTSU_ENG == printData._ck;
            if (isMieSotsugyoEng) {
                form.VrAttribute("SCHOOLADDRESS1", "Bold=0");                        //学校名称（英）
                form.VrAttribute("SCHOOLADDRESS2", "Bold=0");                        //学校名称（英）
            }
        }

        String principalJobnameDefault = "Principal";
        if (isZaigaku) {
            if (_param._z010.in(Z010.Sakae)) {
                principalJobnameDefault = "";
            }
        }
        final String jobname;
        if (_param._z010.in(Z010.jyoto)) {
            jobname = printData._schoolInfo._principalJobname;
        } else {
            jobname = defstr(printData._schoolInfo._remark6, principalJobnameDefault);
        }
        form.VrsOut("JOBNAME", jobname);

        final String staffName;
        if (_param._z010.in(Z010.jyoto)) {
            staffName = printData._schoolInfo._principalName;
        } else if (_param._z010 == Z010.Kindai) {
            staffName = printData._schoolInfo._principalStaffnameEng;
        } else {
            staffName = printData._schoolInfo._remark1;
        }
        form.VrsOut("STAFFNAME", staffName);                //代表名（英）
        form.VrsOut("SCHOOLNAME_JP", printData._schoolInfo._schoolname1);                  //学校名（和）

        boolean print2 = false;
        if (_param._z010 != Z010.Kindai) {
            print2 = true;
            if (!isZaigaku) {
                final boolean mieRemark2remark3isBlank = _param._z010 == Z010.Mieken && StringUtils.isBlank(printData._schoolInfo._remark2) && StringUtils.isBlank(printData._schoolInfo._remark3);
                if (mieRemark2remark3isBlank) {
                    print2 = false;
                }
            }
        }
        if (print2) {
            form.VrsOut("STAFFNAME_JP", defstr(printData._schoolInfo._principalName));            //校長名
            form.VrsOut("JOBNAME_JP", defstr(printData._schoolInfo._principalJobname)); //職名
        } else {
            form.VrsOut("STAFFNAME_JP", defstr(printData._schoolInfo._principalJobname) + defstr(printData._schoolInfo._principalName));   //校長名（和）
        }

        printStamp(form, printData);
        printPrincipalSignatureImage(form, printData);
    }

    private void printPrincipalSignatureImage(final Form form, final PrintData printData) {
        String signatureImagePath = null;
        if (printData._isPrintPrincipalSignature) {
            signatureImagePath = printData.getPrincipalSignatureImagePath(_param, printData._certifKindSchoolKind);
        }
        if (_param._isOutputDebug) {
            log.info(" signatureImagePath = " + signatureImagePath);
        }
        form.VrImageOut("SIGNATURE", signatureImagePath);
    }

    private void printSchoolJp(final DB2UDB db2, final Form form, final PrintData printData) {
        printStamp(form, printData);
        final PersonalInfo personalInfo = printData._personalInfo;
        if (null == printData._schoolInfo) {
            return;
        }
        final CK ck = printData._ck;
        final boolean isSapporoSpec = _param._z010.in(Z010.Sapporo) && ck.in(CK._1_SOTSU, CK._3_SOTSUMI, CK._4_ZAIGAKU, CK._12_ZAIGAKU_J, CK._15_SHURYO, CK._38_SHURYOMI);
        final boolean isNotPrintDate = ck.in(CK._43_KYUGAKU_KYOKA, CK._44_RYUGAKU_KYOKA, CK._45_TAIGAKU_KYOKA) || ck == CK._15_SHURYO && _param._z010.in(Z010.ChiyodaKudan);
        final boolean isKaichiSpec = _param._z010.isKaichiSpec() && ck.in(CK._1_SOTSU, CK._4_ZAIGAKU, CK._12_ZAIGAKU_J, CK._22_SOTSU_J);
        if (!isNotPrintDate) {
            if (ck.isPrintGakuwari()) {
                final Calendar cal = Utils.getCalendar(printData._certifDate);
                form.VrsOut("ISSUE_NEN", String.valueOf(cal.get(Calendar.YEAR))); // 発行日付 年
                form.VrsOut("ISSUE_MONTH", String.valueOf(cal.get(Calendar.MONTH) + 1)); // 発行日付 月
                form.VrsOut("ISSUE_DOM", String.valueOf(cal.get(Calendar.DAY_OF_MONTH))); // 発行日付 日
            } else {
                final String DATE = "DATE";
                String date = PrintData.printsvfDate(db2, printData, printData._certifDate, false, false);  // 証明日付
                if (null == date) {
                    form.VrAttribute("DATE", Attribute.MIGITSUME);
                    date = "　　　年　 月　 日";
                }

                List<String> dateAttribute = new ArrayList<String>();
                if (_param._z010.in(Z010.Meiji)) {
                    if (ck.in(CK._1_SOTSU, CK._3_SOTSUMI, CK._4_ZAIGAKU, CK._12_ZAIGAKU_J, CK._22_SOTSU_J)) {
                        dateAttribute.add(Attribute.setX(1980));
                    }
                } else if (_param._z010.in(Z010.Musashinohigashi) && H.equals(printData._certifKindSchoolKind)) {
                    if (ck.in(CK._1_SOTSU, CK._3_SOTSUMI, CK._15_SHURYO)) {
                        dateAttribute.add(Attribute.setX(430));
                    } else if (ck.in(CK._13_ZAISEKI, CK._4_ZAIGAKU)) {
                        dateAttribute.add(Attribute.setX(180));
                    }
                } else if (_param._z010.in(Z010.Seijyo) && ck.in(CK._1_SOTSU)) {
                    dateAttribute.add(Attribute.plusY(DATE, 150, form));
                } else if (_param._z010.in(Z010.ChiyodaKudan) && ck.in(CK._4_ZAIGAKU)) {
                    dateAttribute.add(Attribute.plusX(DATE, 250, form));
                } else if (_param._z010.in(Z010.Sakae) && ck.in(CK._12_ZAIGAKU_J)) {
                    dateAttribute.add(Attribute.plusX(DATE, 400, form));
                } else if (_param._z010.in(Z010.Nagisa) && (ck.isPrintSotsugyo() || ck.isPrintZaigaku())) {
                    dateAttribute.add(Attribute.setX(2050));
                    dateAttribute.add(Attribute.setY(510));
                } else if (isKaichiSpec) {
                    dateAttribute.add(Attribute.setX(1900));
                } else if (_param._z010.in(Z010.NichidaiNikou)) {
                    if (ck.isPrintSotsugyo() || ck.isPrintSotsugyoMikomi() || ck.isPrintZaigaku() || ck.isPrintZaiseki()) {
                        dateAttribute.add(Attribute.setX(1150)); // 画面中央
                    }
                } else if (_param._z010.in(Z010.Reitaku)) {
                    if (ck == CK._22_SOTSU_J) {
                        dateAttribute.add(Attribute.plusX(DATE, 20, form));
                    }
                } else if (_param._z010.in(Z010.suito) & ck.isPrintZaigaku()) {
                    dateAttribute.add(Attribute.setX(1650 - 382));
                }
                if (!dateAttribute.isEmpty()) {
                    form.VrAttribute(DATE, Utils.mkString(dateAttribute, ","));
                }
                form.VrsOut(DATE, date);
            }
        }

        // 学校住所
        if (_param._z010.in(Z010.Sundaikoufu, Z010.Meikei, Z010.Musashinohigashi, Z010.Reitaku) || ck.isPrintGakuwari()) {
            form.VrsOut("SCHOOL_ADDR",  printData._schoolInfo._remark2);
        }

        // 法人名
        final boolean isPrintCorp = _param._z010.in(Z010.Musashinohigashi) || ck.in(CK._1_SOTSU, CK._3_SOTSUMI, CK._4_ZAIGAKU, CK._12_ZAIGAKU_J, CK._22_SOTSU_J, CK._31_ZAIRYOU, CK._50_SOTSU_H, CK._54_ZAIRYOU_J) && !(ck.isPrintGakuwari() || isSapporoSpec);
        if (isPrintCorp) { // 武蔵野東、埼玉栄
            form.VrsOut("CORP_NAME", printData._schoolInfo._remark3);
        }

        // 学校名
        {
            final boolean isPrintSchoolname = !isSapporoSpec;
            if (isPrintSchoolname) {
                String fieldname = "SCHOOLNAME";
                if (ck.isPrintGakuwari()) {
                    fieldname = "SCHOOL_NAME";
                }
                form.VrsOut(fieldname, printData._schoolInfo._schoolname1); // 学校名

                String attribute = null;
                if (isKaichiSpec) {
                    attribute = Attribute.charSize(20.0);
                }
                if (null != attribute) {
                    form.VrAttribute(fieldname, attribute);
                }
            }
        }

        // 郵便番号
        if (ck.isPrintGakuwari() || _param._z010.in(Z010.Musashinohigashi) && !K.equals(personalInfo._schoolKind)) {
            String fieldname = "SCHOOL_ZIPCD";
            if (_param._z010.in(Z010.Musashinohigashi) && !K.equals(personalInfo._schoolKind)) {
                fieldname = "ZIP_NO";
            }
            form.VrsOut(fieldname, printData._schoolInfo._remark1); // 学校郵便番号
        }

        // 学校名2
        if (_param._z010.in(Z010.Musashinohigashi) && K.equals(personalInfo._schoolKind)) {
            form.VrsOut("SCHOOLNAME2",  printData._schoolInfo._remark4);
        }

        // 職名
        {
            String attribute = null;
            if (isKaichiSpec) {
                attribute = Attribute.charSize(22.0);
            }
            String fieldname = "JOBNAME";
            if (_param._z010.in(Z010.Kumamoto, Z010.tokiwa) && (printData._ck.isPrintSotsugyo() || printData._ck.isPrintSotsugyoMikomi() || printData._ck.isPrintZaigaku())) {
                fieldname = "JOBNAME_S";
            } else if (ck.isPrintGakuwari()) {
                fieldname = "PRINCIPAL_JOBNAME";
            }
            String jobname = printData._schoolInfo._principalJobname;
            if (isSapporoSpec) {
                jobname = "　　　　　　　" + defstr(printData._schoolInfo._principalJobname);
            }
            form.VrsOut(fieldname, jobname);
            if (null != attribute) {
                form.VrAttribute(fieldname, attribute);
            }
        }

        // 校長名
        {
            String attribute = null;
            if (isKaichiSpec) {
                attribute = Attribute.charSize(22.0);
            }
            String principalName = printData._schoolInfo._principalName;
            if (isSapporoSpec) {
                principalName = "　　　　　　　　　　　　　　　　　" + defstr(printData._schoolInfo._principalName);
            }
            String fieldname = "STAFFNAME";
            if (ck.isPrintGakuwari()) {
                fieldname = "PRINCIPAL";
            }
            form.VrsOut(fieldname, principalName);  // 校長名
            if (null != attribute) {
                form.VrAttribute(fieldname, attribute);
            }
        }
    }

    private void printStamp(final Form form, final PrintData printData) {
        if (printData._isFormForStamp) {
            String imagePath;
            if (null != printData._setCertifSchoolstampImagePath) {
                imagePath = printData._setCertifSchoolstampImagePath;
            } else {
                imagePath = printData.getCertifSchoolStampSchoolKindImagePath(_param, printData._certifKindSchoolKind);
            }
            if (printData._isPrintStamp) {
                if (_param._isOutputDebug) {
                    log.info(" stamp = " + imagePath);
                }
                form.VrImageOut("STAMP", imagePath);
            }
        }
    }

    private String getGradeEng2(final PrintData printData) {
        final int gradeInt = getPrintGradeInt(printData);
        final String sGrade = 0 == gradeInt ? " " : String.valueOf(gradeInt);
        final String s;
        if (_param._z010.in(Z010.Sundaikoufu, Z010.Osakatoin, Z010.Matsudo)) {
            s = sGrade + Utils.getNumSuffix(sGrade);
        } else if (_param._z010 == Z010.Meiji) {
            s = sGrade + " " + Utils.getNumSuffix(sGrade);
        } else if (_param._z010.in(Z010.Sapporo, Z010.Osakatoin)) {
            s = PrintData.yearTh(sGrade);
        } else {
            s = sGrade;
        }
        return s;
    }

    // 在学証明書(英)
    private void printZaigakuEng(final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final PersonalInfo personalInfo = printData._personalInfo;
        String filename;
        if (_param._z010.in(Z010.Sundaikoufu)) {
            filename = "KNJG010_5KOUFU.frm";
        } else if (_param._z010 == Z010.Sapporo) {
            filename = "KNJG010_5SAP.frm";
        } else if (_param._z010 == Z010.Meiji) {
            filename = "KNJG010_5MEIJI.frm";
        } else if (_param._z010 == Z010.Meikei) {
            filename = "KNJG010_5MEIKEI.frm";
        } else if (_param._z010 == Z010.Osakatoin) {
            filename = "KNJG010_5TOIN.frm";
        } else if (_param._z010 == Z010.Sakae) {
            filename = "KNJG010_5SAKAE.frm";
        } else if (_param._z010 == Z010.jyoto) {
            filename = "KNJG010_5_JYOTO.frm";
        } else {
            filename = "KNJG010_5.frm";
        }
        printData._formname = filename;
        form.setForm(printData._formname, printData);

        //学籍番号
        form.VrsOut("SCHREGNO", personalInfo._printSchregno);

        if (_param._z010.in(Z010.Sundaikoufu)) {
            final String schoolKindName;
            if (CK._21_ZAIGAKU_J_ENG == printData._ck) {
                schoolKindName = "junior high school";
            } else if (CK._42_ZAIGAKU_P_ENG == printData._ck) {
                schoolKindName = "elementaly school";
            } else {
                schoolKindName = "high school";
            }
            form.VrsOut("NOTE3", schoolKindName);
            form.VrsOut("NAME", personalInfo.getNameEng(_param));                               //氏名（英）
            form.VrsOut("GRADE", getGradeEng2(printData));                 //学年

        } else if (_param._z010 == Z010.Meiji) {
            final String grade = getGradeEng2(printData);
            form.VrsOut("TITLE", "CERTIFICATE OF ENROLLMENT");
            form.VrsOut("NOTE1", " This is to certify that the above mentioned student");
            if (P.equals(personalInfo._schoolKind)) {
                form.VrsOut("NOTE2", "is enrolled as a " + grade + " grade student in our elementary school.");
            } else {
                form.VrsOut("NOTE2", "is enrolled in our school in the " + grade + " grade of high school.");
            }
            form.VrsOut("COLON1", ":");
            form.VrsOut("COLON2", ":");
            form.VrsOut("COLON3", ":");
            form.VrsOut("NAME",     personalInfo.getNameEng(_param));                               //氏名（英）
        } else if (_param._z010 == Z010.Sapporo) {
            form.VrsOutSelectField(Arrays.asList("NAME1", "NAME2"), personalInfo.getNameEng(_param)); //氏名（英）
            if (CK._5_ZAIGAKU_ENG == printData._ck) {
                form.VrsOut("TITLE", "Proof of Enrolment (Higher Secondary)");
                form.VrsOut("MAIN1", "This document certifies that the abovementioned is enrolled as a full time");
                form.VrsOut("MAIN2", getGradeEng2(printData) + " year student of the Cosmo Science Division at Sapporo Kaisei");
                form.VrsOut("MAIN3", "Secondary School.");
            } else if (CK._21_ZAIGAKU_J_ENG == printData._ck) {
                form.VrsOut("TITLE", "Proof of Enrolment (Lower Secondary)");
                form.VrsOut("MAIN1", "This document certifies that the abovementioned is enrolled as a full time");
                form.VrsOut("MAIN2", getGradeEng2(printData) + " year student at Sapporo Kaisei Secondary School.");
            }
        } else if (_param._z010 == Z010.Sakae) {
            form.VrsOut("NAME",     personalInfo.getNameEng(_param));                               //氏名（英）
            form.VrsOut("NOTE1", " This is certify that mentioned above student");
            if (CK._21_ZAIGAKU_J_ENG == printData._ck) {
                form.VrsOut("NOTE2", "is in our school as " + getGradeEng2(printData) + " grade as of Junior High School.");
                form.VrAttribute("NOTE2", Attribute.plusX("NOTE2", -150, form));
            } else {
                form.VrsOut("NOTE2", "is in our school as " + getGradeEng2(printData) + " grade as of High School.");
            }

        } else if (_param._z010 == Z010.Meikei) {
            form.VrsOut("NAME",     personalInfo.getNameEng(_param));                               //氏名（英）
            form.VrsOut("GRADE",    getGradeEng2(printData));                 //学年
            form.VrsOut("YEAR_S",   getSvfDate_US_M(personalInfo._entDate, ""));   //入学日（英）

        } else if (_param._z010 == Z010.Osakatoin) {
            form.VrsOutSelectField(Arrays.asList("NAME", "NAME_2", "NAME_3", "NAME_4", "NAME_5"), defstr(personalInfo.getNameEng(_param)) + "(" + defstr(_param.z002(personalInfo._sex, "ABBV3")) + ")"); //氏名（英）+ 性別

            final String setGrade = PrintData.yearTh(printData._personalInfo.getGradeCd());
            final String note1 = "This is to certify that the student mentioned above is enrolled in the";
            final String note2;
            if (CK._21_ZAIGAKU_J_ENG == printData._ck) {
                note2 = setGrade + " year at Osaka Toin Junior High School.";
            } else {
                note2 = setGrade + " year at Osaka Toin Senior High School.";
            }
            form.VrsOut("NOTE1", note1);
            form.VrsOut("NOTE2", note2);

        } else if (_param._z010.in(Z010.jyoto)) {
            form.VrsOutSelectField(Arrays.asList("NAME"), defstr(personalInfo.getNameEng(_param))); //氏名（英）+ 性別

            form.VrsOut("ENT_DATE", _param.formatDateUs(personalInfo._entDate, US_format(null), ""));     // 入学日（英）

            final String setGrade = PrintData.yearTh(printData._personalInfo.getGradeCd());
            final StringBuffer s = new StringBuffer();
            s.append("enrolled as a " + setGrade + " year student in the " + defstr(personalInfo.getString("COURSEENG")) + " " + defstr(personalInfo.getString("MAJORENG")) + " ");
            s.append("at Jyoto High School attached to Fukuoka Institute of Technology.");

            final List<String> split = Utils.splitBySizeWithSpace(s.toString(), form.getFieldKeta("NOTE2"), form.getFieldKeta("NOTE3"), form.getFieldKeta("NOTE4"));
            for (int i = 0; i < split.size(); i++) {
                form.VrsOut("NOTE" + String.valueOf(i + 2), split.get(i));
            }

        } else if ("1".equals(printData._chutouKyoikuGakkouFlg)) {
            form.VrsOut("NAME",     personalInfo.getNameEng(_param));                               //氏名（英）
            form.VrAttribute("NOTE2", "Edit=");
            form.VrsOut("NOTE2",     "is in our school as " + defstr(getGradeEng2(printData)) + " grade as of Secondary School.");                               //氏名（英）
        } else {
            form.VrsOut("NAME",     personalInfo.getNameEng(_param));                               //氏名（英）
            form.VrsOut("GRADE",    getGradeEng2(printData));                 //学年
        }
        form.VrsOut("SEX",      _param.z002(personalInfo._sex, "ABBV3"));                               //性別（英）
        form.VrsOut("SEX_TITLE", "Sex：");                               //性別（英）


        final String birthDay;
        if (_param._z010 == Z010.Sapporo) {
            birthDay = "Date of birth: " + _param.formatDateUs(personalInfo._birthday, d_MONTHFULL_yyyy_sapporo, "     ");
        } else {
            birthDay = _param.formatDateUs(personalInfo._birthday, US_format(null), "");
        }
        form.VrsOut("BIRTHDAY", birthDay);     //生年月日（英）
        printPersonalInfoCommon(form, printData);
        printSchoolCommon(form, printData);
        printSchoolEng(form, printData, US_format(null));
        _hasData = true;
    }

    // 002 卒業証明書(英)、014 在籍証明書(英)、016 修了証明書(英)、019 卒業見込証明書(英)、039 修了見込証明書(英)
    private void printSotsugyoEng(final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final PersonalInfo personalInfo = printData._personalInfo;
        if (_param._z010.in(Z010.Sapporo) && printData._ck.in(CK._2_SOTSU_ENG, CK._19_SOTSUMI_ENG, CK._16_SHURYO_ENG, CK._39_SHURYOMI_ENG)) {
            String filename = null;
            if (printData._ck.in(CK._2_SOTSU_ENG, CK._19_SOTSUMI_ENG)) {  // 卒業証明書（英）
                filename = "KNJG010_2SAP.frm";
            } else if (printData._ck.in(CK._16_SHURYO_ENG, CK._39_SHURYOMI_ENG)) {  // 修了証明書（英）
                filename = "KNJG010_10SAP.frm";
            }
            printData._formname = filename;
            form.setForm(printData._formname, printData);
            final String formatted = _param.formatDateUs(personalInfo._graduDate, "d", "") + Utils.getNumSuffix(_param.formatDateUs(personalInfo._graduDate, "d", "")) + " of " + _param.formatDateUs(personalInfo._graduDate, MONTHFULL_comma_yyyy_sapporo, "");
            if (printData._ck.in(CK._19_SOTSUMI_ENG)) {
                form.VrsOut("TITLE", "Certificate of Expected Graduation");
                form.VrsOut("MAIN1", "This document certifies that the abovementioned is expected to graduate Sapporo");
                form.VrsOut("MAIN2", "Kaisei Secondary School, Cosmo Science Division on the " + formatted + ".");
            } else if (printData._ck.in(CK._2_SOTSU_ENG)) {
                form.VrsOut("TITLE", "Certificate of Graduation");
                form.VrsOut("MAIN1", "This document certifies that the abovementioned graduated Sapporo Kaisei");
                form.VrsOut("MAIN2", "Secondary School, Cosmo Science Division on the " + formatted + ".");
            } else if (printData._ck.in(CK._16_SHURYO_ENG)) {
                form.VrsOut("TITLE", "Certificate of Completion of Compulsory Studies");
                form.VrsOut("MAIN1", "This document certifies that the abovementioned has completed the compulsory");
                form.VrsOut("MAIN2", "education component of their secondary studies (Years 1-3) at Sapporo Kaisei");
                form.VrsOut("MAIN3", "High School, in the Cosmo Science Division on the " + formatted + ".");
            } else if (printData._ck.in(CK._39_SHURYOMI_ENG)) {
                form.VrsOut("TITLE", "Statement of Expected Completion");
                form.VrsOut("TITLE2", "of Compulsory Studies");
                form.VrsOut("MAIN1", "This document certifies that the abovementioned is expected to complete the");
                form.VrsOut("MAIN2", "national compulsory education component of their secondary studies (Years 1-3)");
                form.VrsOut("MAIN3", "at Sapporo Kaisei High School, in the Cosmo Science Division on the " + _param.formatDateUs(personalInfo._graduDate, "d", "") + Utils.getNumSuffix(_param.formatDateUs(personalInfo._graduDate, "d", "")) + " of ");
                form.VrsOut("MAIN4", _param.formatDateUs(personalInfo._graduDate, MONTHFULL_comma_yyyy_sapporo, "") + ".");
            }

            form.VrsOutSelectField(Arrays.asList("NAME1", "NAME2"), personalInfo.getNameEng(_param)); // 氏名（英）
            form.VrsOut("BIRTHDAY", "Date of birth: " + defstr(_param.formatDateUs(personalInfo._birthday, d_MONTHFULL_yyyy_sapporo, "     ")));
        } else if (_param._z010.in(Z010.Sundaikoufu) && H.equals(personalInfo._schoolKind) && printData._ck.in(CK._2_SOTSU_ENG, CK._19_SOTSUMI_ENG)) {
            String filename = null;
            if (printData._ck.in(CK._2_SOTSU_ENG)) {
                filename = "KNJG010_2SUNDAIKOUFU.frm";
            } else if (printData._ck.in(CK._19_SOTSUMI_ENG)) {
                filename = "KNJG010_6SUNDAIKOUFU.frm";
            }
            printData._formname = filename;
            form.setForm(printData._formname, printData);

            if (printData._ck.in(CK._2_SOTSU_ENG)) {

                form.VrsOut("TEXT1", "This is to certify that the person named above was"); // 本文
                form.VrsOut("TEXT2", "　　　　graduated with the following record."); // 本文

            } else if (printData._ck.in(CK._19_SOTSUMI_ENG)) {

                form.VrsOut("TEXT1", " This is to certify that the student named above is"); // 本文
                form.VrsOut("TEXT2", "expected to accquire all required credits and to"); // 本文
                form.VrsOut("TEXT3", "graduate in"); // 本文
            }

            form.VrsOut("NAME", personalInfo.getNameEng(_param)); // 氏名
            form.VrsOut("SEX", _param.z002(personalInfo._sex, "ABBV3")); // 性別
            form.VrsOut("BIRTHDAY", _param.formatDateUs(personalInfo._birthday, SLASH_dd_MM_yyyy, "")); // 誕生日
            form.VrsOut("SCHOOL_NAME1", printData._schoolInfo._schoolnameEng); // 学校名
            form.VrsOut("ENTERANCE", _param.formatDateUs(personalInfo._entDate, US_format(new String[] {"1"}), "")); // 入学日
            form.VrsOut("GRADUATION", _param.formatDateUs(personalInfo._graduDate, US_format("1".equals(printData._entGrdDateFormat) ? new String[] {"1", YM} : new String[] {"1"}), "")); // 卒業日

            form.VrsOut("ADDR1", printData._schoolInfo._remark4); // 住所
            form.VrsOut("ADDR2", printData._schoolInfo._remark5); // 住所

            form.VrsOut("SCHOOL_NAME_JP", printData._schoolInfo._schoolname1); // 学校名
            form.VrsOut("SCHOOL_NAME2_1", printData._schoolInfo._remark2); // 学校名
            form.VrsOut("SCHOOL_NAME2_2", printData._schoolInfo._remark3); // 学校名

            form.VrsOut("DATE", _param.formatDateUs(printData._certifDate, SLASH_dd_MM_yyyy, "")); // 日付
            printData.setCertifNo(_param);
            form.VrsOut("NO", printData._certifNo); // 番号
            _hasData = true;
            return;
        } else if (_param._z010.in(Z010.Rakunan) && printData._ck.in(CK._2_SOTSU_ENG)) {

            printData._formname = "KNJG010_2RAKU.frm";
            form.setForm(printData._formname, printData);

            String remark2 = null;
            if (null != printData._schoolInfo) {
                remark2 = printData._schoolInfo._remark2;
                form.VrsOut("SCHOOLNAME3_1", remark2); // 学校名
                form.VrsOut("SCHOOLADDRESS1", printData._schoolInfo._remark4);                        //学校名称（英）
                form.VrsOut("SCHOOLADDRESS2", printData._schoolInfo._remark5);                        //学校名称（英）
            }

            form.VrsOut("NAME", personalInfo.getNameEng(_param)); // 氏名
            form.VrsOut("SEX", _param.z002(personalInfo._sex, "ABBV3")); // 性別
            form.VrsOut("BIRTHDAY", _param.formatDateUs(personalInfo._birthday, US_format(null), "")); // 生年月日
            form.VrsOut("YEAR_G", _param.formatDateUs(personalInfo._graduDate, US_format("1".equals(printData._entGrdDateFormat) ? new String[] { YM } : null), "")); // 卒業日
            form.VrsOut("DATE", _param.formatDateUs(printData._certifDate, US_format(null), "")); // 作成日
            form.VrsOut("SCHOOLNAME1", "This is to certify that the above-mentioned person graduated from " + StringUtils.trim(defstr(remark2))); // 学校名
            form.VrsOut("STAFFNAME_JP", defstr(printData._schoolInfo._remark1)); // 職員名
            form.VrsOut("JOBNAME_JP", defstr(printData._schoolInfo._principalJobname)); // 役職

            printSchoolCommon(form, printData);
            _hasData = true;
            return;
        } else if (_param._z010.in(Z010.Sakae) && printData._ck.in(CK._19_SOTSUMI_ENG)) {

            printData._formname = "KNJG010_6SAKAE.frm";
            form.setForm(printData._formname, printData);

            final String name = personalInfo.getNameEng(_param);
            if (null != name) {
                final int spcIdx = name.indexOf(" ");
                if (spcIdx > 1) {
                    form.VrsOut("NAME1_1", name.substring(0, spcIdx)); //氏名（英）
                    form.VrsOut("NAME1_2", StringUtils.trim(name.substring(spcIdx))); //氏名（英）
                } else {
                    form.VrsOut("NAME1_3", name); //氏名（英）
                }

                form.VrsOutSelectField(Arrays.asList("NAME2_1", "NAME2_2", "NAME2_3"), name); //氏名（英）
            }

            form.VrsOut("BIRTHDAY", _param.formatDateUs(personalInfo._birthday, MONTHFULL_d_comma_yyyy, "")); // 生年月日
            form.VrsOut("SEX_MF", _param.z002(personalInfo._sex, "ABBV3")); // 性別
            form.VrsOut("COURSE_NAME", personalInfo.getString("MAJORENG"));   //入学日（英）

            form.VrsOut("PERIOD1",   _param.formatDateUs(personalInfo._entDate, MONTHFULL_d_comma_yyyy, ""));   //入学日（英）
            form.VrsOut("PERIOD2", _param.formatDateUs(personalInfo._graduDate, MONTHFULL_d_comma_yyyy, ""));   //卒業日（英）
            form.VrsOut("GRADUATION", _param.formatDateUs(personalInfo._graduDate, MONTHFULL_d_comma_yyyy, ""));   //卒業日（英）

            printSchoolCommon(form, printData);
            printSchoolEng(form, printData, MONTHFULL_d_comma_yyyy);
            _hasData = true;
            return;
        } else if (_param._z010.in(Z010.NichidaiNikou) && printData._ck.in(CK._2_SOTSU_ENG)) {

            printData._formname = "KNJG010_2NICHINI.frm";
            form.setForm(printData._formname, printData);

            final String name = personalInfo.getNameEng(_param);
            if (null != name) {
                final int spcIdx = name.indexOf(" ");
                if (spcIdx > 1) {
                    form.VrsOut("NAME1_1", name.substring(0, spcIdx)); //氏名（英）
                    form.VrsOut("NAME1_2", StringUtils.trim(name.substring(spcIdx))); //氏名（英）
                } else {
                    form.VrsOut("NAME1_3", name); //氏名（英）
                }

                form.VrsOutSelectField(Arrays.asList("NAME2_1", "NAME2_2", "NAME2_3"), name); //氏名（英）
            }

            form.VrsOut("BIRTHDAY", _param.formatDateUs(personalInfo._birthday, MONTHFULL_comma_yyyy, "")); // 生年月日
            form.VrsOut("SEX_MF", _param.z002(personalInfo._sex, "ABBV3")); // 性別
            form.VrsOut("COURSE_NAME", personalInfo.getString("MAJORENG"));   //入学日（英）

            form.VrsOut("PERIOD1",   _param.formatDateUs(personalInfo._entDate, MONTHFULL_comma_yyyy, ""));   //入学日（英）
            form.VrsOut("PERIOD2", _param.formatDateUs(personalInfo._graduDate, MONTHFULL_comma_yyyy, ""));   //卒業日（英）
            form.VrsOut("GRADUATION", _param.formatDateUs(personalInfo._graduDate, MONTHFULL_comma_yyyy, ""));   //卒業日（英）

            printSchoolCommon(form, printData);
            printSchoolEng(form, printData, MONTHFULL_d_comma_yyyy);
            _hasData = true;
            return;
        } else if (_param._z010.in(Z010.jyoto) && printData._ck.in(CK._2_SOTSU_ENG, CK._19_SOTSUMI_ENG)) {

            printData._formname = "KNJG010_2_JYOTO.frm";
            form.setForm(printData._formname, printData);

            form.VrsOut("NAME", personalInfo.getNameEng(_param)); // 氏名（英）
            if (printData._ck.in(CK._2_SOTSU_ENG)) {
                form.VrsOut("GRD_NO", "Diploma number: " + defstr(personalInfo.getString("BASE_GRD_NO"))); // 生年月日
            }

            form.VrsOut("BIRTHDAY", _param.formatDateUs(personalInfo._birthday, MONTHFULL_dd_comma_SPC_yyyy, "")); // 生年月日

            final StringBuffer s = new StringBuffer();
            if (printData._ck.in(CK._19_SOTSUMI_ENG)) {
                s.append("This is to certify that the person named above is expected to ");
                s.append("complete the " + defstr(personalInfo.getString("COURSEENG")) + " " + defstr(personalInfo.getString("MAJORENG")) + " at Jyoto High School ");
                s.append("affiliated with Fukuoka Institute of Technology, on " + defstr(_param.formatDateUs(personalInfo._graduDate, MONTHFULL_dd_comma_SPC_yyyy, "") + "."));

            } else { // if (printData._ck.in(CK._2_SOTSU_ENG)) {
                s.append("This is to certify that the person named above completed the " + defstr(personalInfo.getString("COURSEENG")) + " ");
                s.append(defstr(personalInfo.getString("MAJORENG")) + " at Jyoto High School affiliated with ");
                s.append("Fukuoka Institute of Technology, on " + defstr(_param.formatDateUs(personalInfo._graduDate, MONTHFULL_dd_comma_SPC_yyyy, "") + "."));
            }

            final List<String> split = Utils.splitBySizeWithSpace(s.toString(), form.getFieldKeta("NOTE1"), form.getFieldKeta("NOTE2"), form.getFieldKeta("NOTE3"), form.getFieldKeta("NOTE4"));
            for (int i = 0; i < split.size(); i++) {
                form.VrsOut("NOTE" + String.valueOf(i + 1), split.get(i));
            }

            printSchoolCommon(form, printData);
            printSchoolEng(form, printData, MONTHFULL_dd_comma_SPC_yyyy);
            _hasData = true;
            return;
        } else {
            String filename = null;
            if (printData._ck.in(CK._2_SOTSU_ENG)) {  // 卒業証明書（英）
                if (_param._z010.in(Z010.Osakatoin)) {
                    filename = "KNJG010_2TOIN.frm";
                } else if (_param._z010.in(Z010.Meiji)) {
                    filename = "KNJG010_2MEIJI.frm";
                } else if (_param._z010.in(Z010.Sakae)) {
                    filename = "KNJG010_2SAKAE.frm";
                } else if (_param._z010.in(Z010.Meikei)) {
                    filename = "KNJG010_2MEIKEI.frm";
                } else if (_param._z010.in(Z010.Nagisa)) {
                    filename = "KNJG010_2NAGISA.frm";
                } else if (_param._z010.in(Z010.Matsudo)) {
                    filename = "KNJG010_2MATSUDO.frm";
                } else {
                    filename = "KNJG010_2.frm";
                }
            } else if (printData._ck.in(CK._14_ZAISEKI_ENG)) {  // 在籍証明書（英）
                if (_param._z010 == Z010.Osakatoin) {
                    filename = "KNJG010_8TOIN.frm";
                } else {
                    filename = "KNJG010_8.frm";
                }
            } else if (printData._ck.in(CK._16_SHURYO_ENG, CK._39_SHURYOMI_ENG)) {  // 修了証明書（英）
                filename = "KNJG010_10.frm";
            } else if (printData._ck.in(CK._19_SOTSUMI_ENG)) {  // 卒業見込証明書（英）
                if (_param._z010.in(Z010.Osakatoin)) {
                    filename = "KNJG010_6TOIN.frm";
                } else if (_param._z010.in(Z010.Meikei)) {
                    filename = "KNJG010_6MEIKEI.frm";
                } else {
                    filename = "KNJG010_6.frm";
                }
            } else if (printData._ck.in(CK._32_SOTSU_J_ENG)) {  // 卒業証明書（英中）
                if (_param._z010.in(Z010.Osakatoin)) {
                    filename = "KNJG010_2TOIN.frm";
                } else if (_param._z010.in(Z010.Meiji)) {
                    filename = "KNJG010_2MEIJI.frm";
                } else if (_param._z010.in(Z010.Sakae)) {
                    filename = "KNJG010_2SAKAE.frm";
                } else if (_param._z010.in(Z010.ChiyodaKudan)) {
                    filename = "KNJG010_2JKUDAN.frm";
                } else {
                    filename = "KNJG010_2.frm";
                }
            }
            printData._formname = filename;
//            printData._printStamp = true;
            form.setForm(printData._formname, printData);

            //学籍番号
            form.VrsOut("SCHREGNO", personalInfo._printSchregno);

            if (_param._z010 == Z010.Osakatoin && printData._ck.in(CK._2_SOTSU_ENG, CK._14_ZAISEKI_ENG, CK._19_SOTSUMI_ENG, CK._32_SOTSU_J_ENG)) {
                form.VrsOutSelectField(Arrays.asList("NAME", "NAME_2", "NAME_3", "NAME_4", "NAME_5"), defstr(personalInfo.getNameEng(_param)) + "(" + defstr(_param.z002(personalInfo._sex, "ABBV3")) + ")");//氏名（英）+ 性別

                if (printData._ck.in(CK._19_SOTSUMI_ENG)) {
                    form.VrsOut("NOTE1", "This is to certify that the student mentioned above will");
                    form.VrsOut("NOTE2", "graduate from Osaka Toin Senior High School on");
                    form.VrsOut("NOTE3", _param.formatDateUs(personalInfo._graduDate, US_format(null), "") + ".");
                } else if (printData._ck.in(CK._14_ZAISEKI_ENG)) {
                    form.VrsOut("NOTE1", "This is to certify that the student mentioned above was enrolled in");
                    form.VrsOut("NOTE2", "Osaka Toin Senior High School with the following record.");
                    form.VrsOut("ENTRANCE_DAY",   _param.formatDateUs(personalInfo._entDate, US_format(null), ""));   //入学日（英）
                    form.VrsOut("ENROLL", _param.formatDateUs(personalInfo._entDate, US_format(null), "") + "―" + _param.formatDateUs(personalInfo._graduDate, US_format(null), ""));
                } else {
                    form.VrsOut("NOTE1", "This is to certify that the student mentioned above");
                    if (printData._ck.in(CK._32_SOTSU_J_ENG)) {
                        form.VrsOut("NOTE2", "successfully graduated from Osaka Toin Junior High School on");
                    } else {
                        form.VrsOut("NOTE2", "successfully graduated from Osaka Toin Senior High School on");
                    }
                    form.VrsOut("NOTE3", _param.formatDateUs(personalInfo._graduDate, US_format(null), "") + ".");
                    form.VrsOut("ENTRANCE_DAY",   _param.formatDateUs(personalInfo._entDate, "", ""));   //入学日（英）
                }
            } else if (_param._z010.in(Z010.Nagisa) && printData._ck.in(CK._2_SOTSU_ENG)) {
                form.VrsOut("NAME",     personalInfo.getNameEng(_param));                               //氏名（英）
                form.VrsOut("TEXT1", "  This is to certify that the person named below was graduated with"); // 本文
                form.VrsOut("TEXT2", "the following record."); // 本文

                form.VrsOut("ENTRANCE_DAY",   _param.formatDateUs(personalInfo._entDate, US_format(null), ""));   //入学日（英）
                form.VrsOut("GRADUATION",   _param.formatDateUs(personalInfo._graduDate, US_format(null), ""));   //入学日（英）

            } else if (_param._z010.in(Z010.ChiyodaKudan) && printData._ck.in(CK._32_SOTSU_J_ENG)) {
                form.VrsOut("NAME",     personalInfo.getNameEng(_param));                               //氏名（英）
                form.VrsOut("NOTE1", "  The person mentioned above completed the three year course at Kudan Secondary");
                form.VrsOut("NOTE2", "School in " + _param.formatDateUs(personalInfo._graduDate, MONTHFULL_comma_yyyy, "") + ".");
                form.VrsOut("NOTE3", "  I hereby certify the above statement to be true and correct.");
                form.VrsOut("ENTRANCE_DAY",   _param.formatDateUs(personalInfo._entDate, "", ""));   //入学日（英）
            } else {
                form.VrsOut("NAME",     personalInfo.getNameEng(_param));                               //氏名（英）
                final String timeStr = printData._isTeiji ? "part-time" : "full-time";
                form.VrsOut("FULL_PART", timeStr);
                form.VrsOut("ENTRANCE_DAY",   _param.formatDateUs(personalInfo._entDate, "", ""));   //入学日（英）
            }

            String gradeEng = null;
            final String grade = printData._personalInfo.getGradeCd();
            if (NumberUtils.isDigits(grade)) {
                gradeEng = Integer.parseInt(grade) + Utils.getNumSuffix(grade);
            } else {
                gradeEng = defstr(grade);
            }

            final String yearS;
            final String yearF;
            if (_param._z010 == Z010.Sakae) {
                yearS = _param.formatDateUs(personalInfo._entDate, US_format(null), "");
                yearF = _param.formatDateUs(personalInfo._graduDate, US_format(null), "");
            } else {
                yearS = getSvfDate_US_M(personalInfo._entDate, "");
                yearF = getSvfDate_US_M(personalInfo._graduDate, "");
            }

            String sexAbbv2 = _param.z002(personalInfo._sex, "ABBV2");
            if (printData._ck.in(CK._19_SOTSUMI_ENG) && _param._z010.in(Z010.Reitaku)) {
                sexAbbv2 = StringUtils.capitalize(sexAbbv2); // 先頭大文字
            }
            form.VrsOut("SEX", sexAbbv2);  //性別（英）
            form.VrsOut("SEX_MF", _param.z002(personalInfo._sex, "ABBV3")); //性別（英）
//            form.VrsOut("MR",       Utils.append(sexEng, " "));      //性別（英）
            form.VrsOut("BIRTHDAY", _param.formatDateUs(personalInfo._birthday, US_format(null), ""));     //生年月日（英）
            form.VrsOut("YEAR_S",   yearS); // 入学日（英）
            form.VrsOut("YEAR_F",   yearF); // 卒業日（英）
            form.VrsOut("YEAR_G",   _param.formatDateUs(personalInfo._graduDate, US_format("1".equals(printData._entGrdDateFormat) ? new String[] { YM } : null), ""));   //卒業日（英）
            form.VrsOut("YEAR_MONTH_G", _param.formatDateUs(personalInfo._graduDate, MONTHFULL_yyyy, ""));   //卒業日（英）
            form.VrsOut("GRADE",  gradeEng);                  //学年
        }
        if (_param._z010.in(Z010.Sakae)) {
            form.addAttribute("FAX_TITLE", Attribute.setX(INVALID_X));
            form.addAttribute("FAX", Attribute.setX(INVALID_X));
        } else if (_param._z010.in(Z010.Reitaku)) {
            form.addAttribute("SCHOOLNAME3_1", Attribute.setX(INVALID_X));
            form.addAttribute("SCHOOLNAME3_2", Attribute.setX(INVALID_X));
            form.addAttribute("SCHOOLADDRESS1", Attribute.setX(INVALID_X));
            form.addAttribute("SCHOOLADDRESS2", Attribute.setX(INVALID_X));
        }
        setAttribute(form);
        printPersonalInfoCommon(form, printData);
        printSchoolCommon(form, printData);

        printSchoolEng(form, printData, US_format(null));
        _hasData = true;
    }

    // 在籍証明書
    private void printZaiseki(final DB2UDB db2, final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final String NAME0 = "NAME0";
        final String NAME1 = "NAME1";
        final String NAME2 = "NAME2";
        final String MAIN1 = "MAIN1";
        final String MAIN2 = "MAIN2";
        final String MAIN3 = "MAIN3";
        final PersonalInfo personalInfo = printData._personalInfo;
        final String filename;
        if (_param._z010.in(Z010.Musashinohigashi) && printData._ck.in(CK._41_ZAISEKI_K)) {
            filename = "KNJG010_7_2MUSAHIGA.frm";
        } else if (_param._z010.in(Z010.Withus)) {
            filename = "KNJWG010_7.frm";
        } else if (_param._z010.in(Z010.Miyagiken)) {
            filename = "KNJG010_7MIYA.frm";
        } else if (_param._z010.in(Z010.Musashinohigashi)) {
            filename = "KNJG010_7MUSAHIGA.frm";
        } else if (_param._z010.in(Z010.Hirokoudai)) {
            filename = "KNJG010_7HIROKOUDAI.frm";
        } else if (_param._z010.in(Z010.Osakatoin)) {
            filename = "KNJG010_7TOIN.frm";
        } else if (_param._z010.in(Z010.Ritsumeikan)) {
            filename = "KNJG010_7KEISHO.frm";
        } else if (_param._z010.in(Z010.jyoto)) {
            filename = "KNJG010_7_JYOTO.frm";
        } else {
            filename = "KNJG010_7.frm";
        }
        printData._formname = filename;
        form.setForm(printData._formname, printData);

        //学籍番号
        form.VrsOut("SCHREGNO", personalInfo._printSchregno);

        // 在籍証明書(和)
        if (_param._z010.in(Z010.Ritsumeikan)) {
            form.VrsOut("KANA", personalInfo.getNameKana());
        }
        form.VrsOutSelectField(Arrays.asList(NAME0, NAME1,  NAME2), personalInfo.getPrintName(printData, _param));
        form.VrsOut("BIRTHDAY", Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  //生年月日

        final boolean numZenkaku = _param._z010.in(Z010.Hirokoudai);
        final String defString = _param._z010.in(Z010.Musashinohigashi) ? Utils.setDateFormat(db2, printData._ctrlYear) : null;
        final String dateFrom = Utils.getSeirekiFlgDateString(db2, printData, personalInfo._entDate, YMD, defString, numZenkaku);
        final String dateTo = Utils.getSeirekiFlgDateString(db2, printData, printData.getZaisekiShoumeiGrdDate(_param), YMD, defString, numZenkaku);
        final String katei = Utils.addKatei(personalInfo._coursename);
        final String gakka = defstr(personalInfo._majorname);
        if (_param._z010.in(Z010.Musashinohigashi)) {
            String main1;
            String main2;
            String main3;
            if (printData._ck.in(CK._41_ZAISEKI_K)) {
                form.VrsOut("NAME_TITLE", "園児名");

                main1 = "上記の園児は";
                main2 = defstr(dateFrom) + FROM_TO_MARK + defstr(dateTo) + "まで";
                main3 = "　　　　在園したことを証明します。";

            } else {
                form.VrsOut("NAME_TITLE", "生徒名");

                if (H.equals(printData._certifKindSchoolKind)) {
                    main1 = "　上記の者は本校 " + katei;
                    main2 = gakka + "に " + defstr(dateFrom) + "より" + defstr(dateTo) + "まで";
                    main3 = "在籍していたことを証明します。";

                    final int xplus = 800;
                    getMappedList(form._attributeMap, "NAME_TITLE").add(Attribute.plusX("NAME_TITLE", xplus, form));
                    getMappedList(form._attributeMap, NAME1).add(Attribute.plusX(NAME1, xplus, form));
                    getMappedList(form._attributeMap, NAME2).add(Attribute.plusX(NAME2, xplus, form));

                    final int mainXplus = -300;
                    getMappedList(form._attributeMap, MAIN1).add(Attribute.plusX(MAIN1, mainXplus, form));
                    getMappedList(form._attributeMap, MAIN2).add(Attribute.plusX(MAIN2, mainXplus, form));
                    getMappedList(form._attributeMap, MAIN3).add(Attribute.plusX(MAIN3, mainXplus, form));

                } else {
                    main1 = "　上記の者は本校 " + katei + "に";
                    main2 = defstr(dateFrom) + "より" + defstr(dateTo) + "まで";
                    main3 = "在籍していたことを証明します。";
                }
            }
            form.VrsOut(MAIN1, main1);
            form.VrsOut(MAIN2, main2);
            form.VrsOut(MAIN3, main3);

        } else if (_param._z010.in(Z010.Miyagiken)) {

            final String entGrade = defstr(NumberUtils.isDigits(personalInfo.getEnterGradeCd()) ? String.valueOf(Integer.parseInt(personalInfo.getEnterGradeCd())) : personalInfo.getEnterGradeCd());
            final String note1 = "　上の者は " + dateFrom + "本校" + katei + gakka + "第" + entGrade + "学年に入学し";
            final String note2 = dateTo + "まで在籍したことを証明する";
            final String note3 = "　";

            form.VrsOut("NOTE1", note1);
            form.VrsOut("NOTE2", note2);
            form.VrsOut("NOTE3", note3);

            final double defaultMain1pCharPoint = 20.0;
            final KNJSvfFieldModify modify1 = new KNJSvfFieldModify("NOTE1", 277, 2876, defaultMain1pCharPoint, 2162, 28, 64);
            double newCharPoint = modify1.getCharPoint(note1);
            if (Math.abs(defaultMain1pCharPoint - newCharPoint) > 0.1) {
                form.VrAttribute("NOTE1", Attribute.charSize(newCharPoint));
                form.VrAttribute("NOTE1", "Y=" + String.valueOf((int) modify1.getYPixel(0, KNJSvfFieldModify.charPointToPixel(newCharPoint))));

                form.VrAttribute("NOTE2", Attribute.charSize(newCharPoint));
                form.VrAttribute("NOTE2", "Y=" + String.valueOf((int) modify1.getYPixel(2, KNJSvfFieldModify.charPointToPixel(newCharPoint))));

                form.VrAttribute("NOTE3", Attribute.charSize(newCharPoint));
                form.VrAttribute("NOTE3", "Y=" + String.valueOf((int) modify1.getYPixel(4, KNJSvfFieldModify.charPointToPixel(newCharPoint))));
            }
        } else if (_param._z010.in(Z010.Hirokoudai)) {
            form.setForm(filename, printData);
            form.VrImageOut("SCHOOLLOGO", StringUtils.defaultString(printData._schoollogoHJpgImagePath, printData._schoollogoJpgImagePath));
            form.VrsOut("COURSE", defstr(personalInfo._coursename));
            form.VrsOut("SUBJECT", gakka);
            form.VrsOutSelectField(Arrays.asList(NAME1, NAME2), personalInfo.getPrintName(printData, _param)); //氏名
            form.VrsOut("BIRTHDAY", Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  //生年月日
            form.VrsOut("SDATE", dateFrom);         //入学日
            form.VrsOut("FDATE", dateTo);       //卒業日
        } else {
            final String[][] replaces = {
                    {KW_SEITO_KATEIMEI, katei}
                  , {KW_SEITO_GAKKAMEI, gakka}
                  , {KW_SEITO_NYUGAKUHIDUKE, dateFrom}
                  , {KW_SEITO_ZAISEKISHOMEISHOSOTSUGYOHIDUKE, dateTo}
            };
            final Map<String, String> fieldDataMap = new HashMap<String ,String>();

            final List<String> template = new ArrayList<String>();
            if (_param._z010.in(Z010.NichidaiNikou)) {
                template.add("　 　上記の者は本校に" + KW_SEITO_NYUGAKUHIDUKE + "より");
                template.add("　 " + KW_SEITO_ZAISEKISHOMEISHOSOTSUGYOHIDUKE + "まで在籍していたことを");
                template.add("　 証明します");
            } else if (_param._z010.in(Z010.Ritsumeikan)) {
                template.add("　 上記の者は、本校の" + KW_SEITO_KATEIMEI + " " + KW_SEITO_GAKKAMEI + " に");
                template.add("　 " + KW_SEITO_NYUGAKUHIDUKE + "より" + KW_SEITO_ZAISEKISHOMEISHOSOTSUGYOHIDUKE + "まで");
                template.add("　 在籍していたことを証明します。");
            } else if (_param._z010.in(Z010.Osakatoin)) {
                template.add("　上記の者は" + KW_SEITO_NYUGAKUHIDUKE + "から" + KW_SEITO_ZAISEKISHOMEISHOSOTSUGYOHIDUKE + "まで");
                template.add("本校に在籍したことを証明します。");
            } else if (_param._z010.in(Z010.Nishiyama)) {
                template.add("　上記の者は本校 " + KW_SEITO_KATEIMEI + KW_SEITO_GAKKAMEI + "に" + KW_SEITO_NYUGAKUHIDUKE);
                template.add("より" + KW_SEITO_ZAISEKISHOMEISHOSOTSUGYOHIDUKE + "まで在籍していたことを証明");
                template.add("する");
            } else if (_param._z010.in(Z010.jyoto)) {
                template.add("上記の者は　" + KW_SEITO_NYUGAKUHIDUKE + "　本校 ");
                template.add(KW_SEITO_GAKKAMEI + "に入学し、"+ KW_SEITO_ZAISEKISHOMEISHOSOTSUGYOHIDUKE + "　に");
                template.add("卒業したことを証明します。");

                fieldDataMap.put("TITLE", "証明書");
            } else {
                template.add("　上記の者は本校 " + KW_SEITO_KATEIMEI + KW_SEITO_GAKKAMEI + "に" + KW_SEITO_NYUGAKUHIDUKE);
                template.add("より" + KW_SEITO_ZAISEKISHOMEISHOSOTSUGYOHIDUKE + "まで在籍していたことを証明");
                template.add("します");
            }

            for (int i = 0; i < template.size(); i++) {
                String n = template.get(i);
                for (final String[] replace : replaces) {
                    n = StringUtils.replace(n, replace[0], replace[1]);
                }
                form.VrsOut("NOTE" + String.valueOf(i + 1), n);
            }

            for (final Map.Entry<String, String> e : fieldDataMap.entrySet()) {
                form.VrsOut(e.getKey(), e.getValue());
            }
        }

        setAttribute(form);

        printPersonalInfoCommon(form, printData);
        printSchoolCommon(form, printData);
        printSchoolJp(db2, form, printData);
        _hasData = true;
    }

    private int getPrintGradeInt(final PrintData printData) {
        final PersonalInfo pi = printData._personalInfo;
        int gradeInt = 0;
        final boolean isPrintGrade = "1".equals(printData._knjg010PrintRegdGradeAsGrade);
        final boolean isPrintGradeCd = "1".equals(printData._knjg010PrintGradeCdAsGrade);
        if (printData._ck.isPrintZaigaku()) {
            final String cd;
            if ("1".equals(printData._schoolMstSchooldiv)) {
                cd = pi._annual;
                if (NumberUtils.isDigits(cd)) {
                    gradeInt = Integer.parseInt(cd);
                }
            } else {
                if (isPrintGrade || _param._z010 == Z010.ChiyodaKudan) {
                    cd = pi.getGrade();
                } else {
                    cd = pi.getGradeCd();
                }
                if (NumberUtils.isDigits(cd)) {
                    if (isPrintGrade || _param._z010 == Z010.ChiyodaKudan) {
                        gradeInt = Integer.parseInt(cd);
                    } else if (isPrintGradeCd) {
                        gradeInt = Integer.parseInt(cd);
                    } else {
                        gradeInt = Integer.parseInt(cd);
                        if (_param._z010 == Z010.Chiben && gradeInt < 17 && gradeInt > 10) {
                            gradeInt -= 10;
                        } else if (_param._z010 != Z010.Sundaikoufu && printData._ikkanFlg && gradeInt > 3) {
                            gradeInt -= 3;
                        }
                    }
                }
            }
        } else {
            boolean sub = false;
            switch (printData._ck) {
            // 在学英語
            case _5_ZAIGAKU_ENG:
            case _21_ZAIGAKU_J_ENG:
            case _42_ZAIGAKU_P_ENG:
                final String grade;
                if (isPrintGrade) {
                    grade = pi.getGrade();
                } else if (_param._z010.in(Z010.Sapporo, Z010.Meiji, Z010.Sundaikoufu, Z010.Osakatoin, Z010.Sakae) || isPrintGradeCd) {
                    grade = pi.getGradeCd();
                } else {
                    grade = pi._annual;
                    sub = true;
                }
                if (NumberUtils.isDigits(grade)) {
                    gradeInt = Integer.parseInt(grade);
                }
                break;
            // 修了
            case _15_SHURYO:
            case _38_SHURYOMI:
            // 在籍
            case _13_ZAISEKI:
            case _41_ZAISEKI_K:
            // 除籍
            case _20_JOSEKI:
                final String gradeStr;
                if (isPrintGrade) {
                    gradeStr = pi.getGrade();
                } else if (_param._z010 == Z010.Sundaikoufu || isPrintGradeCd) {
                    gradeStr = pi.getGradeCd();
                } else {
                    gradeStr = pi._annual;
                }
                if (NumberUtils.isDigits(gradeStr)) {
                    gradeInt = Integer.parseInt(gradeStr);
                }
                sub = true;
                break;
            default:
            }
            if (sub) {
                if (_param._z010 == Z010.Chiben) {
                    if (gradeInt < 17 && gradeInt > 10) {
                        gradeInt -= 10;
                    } else if (gradeInt > 3) {
                        gradeInt -= 3;
                    }
                }
            }
        }
        return gradeInt;
    }


    private String getPrintGrade(final PrintData printData) {
        final int gradeInt = getPrintGradeInt(printData);
        final String sGradeInt = gradeInt == 0 ? " " : String.valueOf(gradeInt);
        final String nenji = sGradeInt + "年次";
        final String dai_gakunen = "第" + sGradeInt + "学年";

        String gradeStr = "";
        // 在学
        if (printData._ck.isPrintZaigaku()) {
            final boolean isJ = J.equals(printData._certifKindSchoolKind);
            final boolean isP = P.equals(printData._certifKindSchoolKind);
            final boolean isK = K.equals(printData._certifKindSchoolKind);
            final boolean isH = !isJ && !isP && !isK;
            if ("1".equals(printData._schoolMstSchooldiv) || _param._z010 == Z010.Sapporo && isH) {
                gradeStr = nenji;
            } else {
                if (_param._z010.in(Z010.Chukyo, Z010.Kyoai)) { // 鳥取 中京 共愛・在学証明書
                    gradeStr = "第 " + sGradeInt + " 学年";
                } else if (_param._z010 == Z010.Sapporo) { // 札幌
                    gradeStr = sGradeInt + "学年";
                } else {
                    gradeStr = dai_gakunen;
                }
            }
        } else {
            switch (printData._ck) {
            // 在学英語
            case _5_ZAIGAKU_ENG:
            case _21_ZAIGAKU_J_ENG:
            case _42_ZAIGAKU_P_ENG:
                gradeStr = getGradeEng2(printData);
                break;
            // 在籍
            case _13_ZAISEKI:
            case _41_ZAISEKI_K:
                gradeStr = sGradeInt;
                break;
            // 修了
            case _15_SHURYO:
            case _38_SHURYOMI:
                gradeStr = "1".equals(printData._schoolMstSchooldiv) ? nenji : dai_gakunen;
                break;
            // 除籍
            case _20_JOSEKI:
                gradeStr = sGradeInt;
                break;
            default:
            }
        }
        return gradeStr;
    }

    // 修了証明書
    private void printShuryo(final DB2UDB db2, final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final PersonalInfo personalInfo = printData._personalInfo;
        final String filename;
        if (_param._z010.in(Z010.Kyoai)) {
            filename = "KNJG010_9KYOAI.frm";
        } else if (_param._z010.in(Z010.Sapporo)) {
            filename = "KNJG010_9SAP.frm";
        } else if (_param._z010.in(Z010.ChiyodaKudan)) {
            filename = "KNJG010_9KUDAN.frm";
        } else if (_param._z010.in(Z010.Osakatoin)) {
            filename = "KNJG010_9TOIN.frm";
        } else if (_param._z010 == Z010.Musashinohigashi && H.equals(printData._certifKindSchoolKind)) {
            filename = "KNJG010_9MUSAHIGA.frm";
        } else {
            filename = "KNJG010_9.frm";
        }
        printData._formname = filename;
        form.setForm(printData._formname, printData);
        final String name = personalInfo.getPrintName(printData, _param);
        final String printBirthday = Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false);
        boolean isPrintSchoolCommon = true;
        if (_param._z010 == Z010.Sapporo) {
            String main1 = "";
            String main2 = "";
            if (printData._ck == CK._15_SHURYO) {
                svf.VrsOut("TITLE", "前期課程修了証明書");
                main1 = "上記の者は " + Utils.getSeirekiFlgDateString(db2, printData, personalInfo._graduDate, YMD, null, false) + " " + PrintData.trim(defstr(printData._schoolInfo._schoolname1)) + " 前期課程";
                main2 = "を修了した者であることを証明します";
            } else if (printData._ck == CK._38_SHURYOMI) {
                svf.VrsOut("TITLE", "前期課程修了見込証明書");
                main1 = "上記の者は " + Utils.getSeirekiFlgDateString(db2, printData, personalInfo._graduDate, YMD, null, false) + " " + PrintData.trim(defstr(printData._schoolInfo._schoolname1)) + " 前期課程";
                main2 = "を修了見込であることを証明します";
            }

            final int mainx1 = 316, mainx2 = 2828;
            final int defaultMain1pCharPoint = 20;
            final KNJSvfFieldModify modify = new KNJSvfFieldModify("MAIN1", mainx1, mainx2, defaultMain1pCharPoint, 2222, 5, 70);
            double newCharPoint = modify.getCharPoint(main1);
            final double rate = newCharPoint / defaultMain1pCharPoint;
            if (-1.0f != newCharPoint) {
                form.VrAttribute(modify._fieldname, Attribute.charSize(newCharPoint));
                form.VrAttribute(modify._fieldname, "Y=" + String.valueOf((int) modify.getYPixel(0, KNJSvfFieldModify.charPointToPixel(newCharPoint))));
            }

            if (rate < 0.99) {
                //log.debug(" rate = " + rate);
                final KNJSvfFieldModify modify2 = new KNJSvfFieldModify("MAIN2", mainx1, mainx2, defaultMain1pCharPoint, 2492, 5, 70);
                final double nCharPoint = modify2._charPoint * rate;
                final double nCharPixel = KNJSvfFieldModify.charPointToPixel(nCharPoint);
                form.VrAttribute(modify2._fieldname, Attribute.charSize(nCharPoint));
                form.VrAttribute(modify2._fieldname, "Y=" + String.valueOf((int) modify2.getYPixel(0, nCharPixel)));
            }

            svf.VrsOut("MAIN1", main1);
            svf.VrsOut("MAIN2", main2);
            form.VrsOutSelectField(Arrays.asList("NAME1", "NAME2"), name); // 氏名
            svf.VrsOut("BIRTHDAY", printBirthday);  // 証明日付
        } else if (_param._z010.in(Z010.Musashinohigashi) && H.equals(printData._certifKindSchoolKind)) {
            final String main1 = "　上記の者は本校" + StringUtils.replace(Utils.addKatei(personalInfo._coursename), "　", " ");
            final String main2 = getPrintGrade(printData) + "を修了したことを証明します。";

            svf.VrsOut("MAIN1", main1);
            svf.VrsOut("MAIN2", main2);
            svf.VrsOut("NAME_TITLE", "生徒名");
            form.VrsOutSelectField(Arrays.asList("NAME1", "NAME2"), name); // 氏名
            svf.VrsOut("BIRTHDAY", printBirthday);  // 証明日付

            final int xplus = 800;
            getMappedList(form._attributeMap, "NAME_TITLE").add(Attribute.plusX("NAME_TITLE", xplus, form));
            getMappedList(form._attributeMap, "NAME1").add(Attribute.plusX("NAME1", xplus, form));
            getMappedList(form._attributeMap, "NAME2").add(Attribute.plusX("NAME2", xplus, form));
        } else if (_param._z010.in(Z010.ChiyodaKudan)) {

            final int namelen = StringUtils.defaultString(name).length();
            // 氏名
            if (namelen > 14) {
                svf.VrsOut("NAME3", StringUtils.defaultString(name).substring(0, Math.min(namelen, 20)));
            } else if (namelen > 10) {
                svf.VrsOut("NAME2", name);
            } else {
                svf.VrsOut("NAME1", name);
            }
            svf.VrsOut("BIRTHDAY", Utils.append(Utils.formatDateKanji(db2, personalInfo._birthday, false, true), "生"));  // 生年月日
            svf.VrsOut("DATE", Utils.formatDateKanji(db2, printData._certifDate, false, true));  //
            isPrintSchoolCommon = false;
        } else {
            if (_param._z010.in(Z010.Osakatoin)) {
                getMappedList(form._attributeMap, "COMPLETION").add("Edit=");
                getMappedList(form._attributeMap, "COMPLETION").add(Attribute.plusX("COMPLETION", -100, form));
                svf.VrsOut("COMPLETION", "修了証明書");
            }

            // 修了証明書(和
            form.VrsOutSelectField(Arrays.asList("NAME1", "NAME2"), name);  // 氏名
            svf.VrsOut("BIRTHDAY", printBirthday);  // 生年月日
            svf.VrsOut("COURSE", Utils.addKatei(personalInfo._coursename));  // 課程
            svf.VrsOut("MAJOR", defstr(personalInfo._majorname));  // 学科

            svf.VrsOut("GRADE", getPrintGrade(printData));  // 学年
        }
        printPersonalInfoCommon(form, printData);
        if (isPrintSchoolCommon) {
            printSchoolCommon(form, printData);
        }
        setAttribute(form);
        printSchoolJp(db2, form, printData);
        _hasData = true;
    }

    // 除籍証明書
    private void printJoseki(final DB2UDB db2, final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final PersonalInfo personalInfo = printData._personalInfo;
        printData._formname = "KNJG010_11.frm";
        form.setForm(printData._formname, printData);
        // 除籍通知書(和)
        form.VrsOutSelectField(Arrays.asList("NAME1", "NAME2"), personalInfo.getPrintName(printData, _param));  // 氏名
        svf.VrsOut("SCHREGNO", printData._schregno); //学籍番号
        svf.VrsOut("BIRTHDAY", Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  //生年月日

        svf.VrsOut("GRADE",   getPrintGrade(printData));                     //学年
        svf.VrsOut("COURSE", defstr(personalInfo._coursename)); //課程
        svf.VrsOut("SUBJECT", defstr(personalInfo._majorname));   //学科

        final int len1 = getMS932ByteLength(personalInfo._addr1);
        final int len2 = getMS932ByteLength(personalInfo._addr2);
        final String sfx = ("1".equals(printData._useAddrField2) && (len1 > 50 || len2 > 50)) ? "3" : (len1 > 40 || len2 > 40) ? "2" : "1";
        svf.VrsOut("ADDRESS1_" + sfx, defstr(personalInfo._addr1));   //住所1
        svf.VrsOut("ADDRESS2_" + sfx, defstr(personalInfo._addr2));   //住所2
        printPersonalInfoCommon(form, printData);
        printSchoolCommon(form, printData);
        printSchoolJp(db2, form, printData);
        _hasData = true;
    }

    // 在寮証明書
    private void printZairyou(final DB2UDB db2, final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final PersonalInfo personalInfo = printData._personalInfo;

        final String name = personalInfo.getPrintName(printData, _param);

        // 在寮証明書
        if (!_param._z010.in(Z010.Sakae, Z010.Reitaku)) {
            if (!personalInfo._inDomitory) { // 非入寮者
                if (_param._isOutputDebug) {
                    log.info("非入寮者のため出力なし");
                }
                return;
            }
        }
        final String formname;
        final List<String> main;
        if (_param._z010.in(Z010.jyoto)) {
            formname = "KNJG031_JYOTO.frm";

            main = Collections.emptyList();
        } else if (_param._z010.in(Z010.Reitaku)) {
            formname = "KNJG031_REITAKU.frm";

            main = Arrays.asList(
                    "　上記の者は本校生徒寮に在寮中の"
                  , "生徒であることを証明します。"
            );
        } else if (_param._z010.in(Z010.Sakae)) {
            formname = "KNJG031_SAKAE.frm";

            main = Arrays.asList(
                    personalInfo._addr2
                  , Utils.kakko(defstr(personalInfo._addr1))
            );
        } else {
            formname = "KNJG031.frm";

            main = Arrays.asList(
                    "上記の者は本校" + defstr(personalInfo._dormitoryName)
                  , "所に在寮中のものであること"
                  , "を証明する"
            );
        }
        printData._formname = formname;

        form.setForm(printData._formname, printData);

        final String TITLE_NAME = "TITLE_NAME";
        final String TITLE_BIRTHDAY = "TITLE_BIRTHDAY";
        final String NAME1 = "NAME1";
        final String NAME2 = "NAME2";
        final String BIRTHDAY = "BIRTHDAY";
        final List<String> nameFields = Arrays.asList(NAME1, NAME2);
        form.VrsOutSelectField(nameFields, name); // 氏名

        form.VrsOut("HRNAME", personalInfo.getString("HR_NAME"));
        form.VrsOut(BIRTHDAY, Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  //生年月日
        form.VrsOut("ENT_DATE", Utils.getSeirekiFlgDateString(db2, printData, personalInfo._entDate, YMD, null, false));

        if (_param._z010.in(Z010.jyoto)) {
            form.VrsOut("DORM_NAME", defstr(PrintData.trim(printData._schoolInfo._schoolname1)) + " " + defstr(printData._personalInfo._dormitoryName));
            form.VrsOut("DORM_ADDRESS", defstr(printData._personalInfo._dormitoryAddr1) + defstr(printData._personalInfo._dormitoryAddr2));
        } else if (_param._z010.in(Z010.Reitaku)) {
            final List<String> name1Fields = Collections.emptyList();
            form.addAttributes(commonAttributes(Z010.Reitaku, form, TITLE_NAME, NAME1, TITLE_BIRTHDAY, BIRTHDAY, nameFields, name1Fields));
        }

        for (int i = 0, len = main.size(); i < len; i++) {
            form.VrsOut("MAIN" + String.valueOf(i + 1), main.get(i));
        }
        printPersonalInfoCommon(form, printData);
        printSchoolCommon(form, printData);
        printSchoolJp(db2, form, printData);

        setAttribute(form);

        _hasData = true;
    }


    // 休学、留学、退学許可書
    private void printKyokasho(final DB2UDB db2, final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final PersonalInfo personalInfo = printData._personalInfo;

        String formname = null;
        if (printData._ck == CK._43_KYUGAKU_KYOKA) {
            formname = "KNJG010_43.frm";
        } else if (printData._ck == CK._44_RYUGAKU_KYOKA) {
            formname = "KNJG010_44.frm";
        } else if (printData._ck == CK._45_TAIGAKU_KYOKA) {
            formname = "KNJG010_45.frm";
        }
        printData._formname = formname;
        form.setForm(printData._formname, printData);

        String seitoName = personalInfo.getPrintName(printData, _param);
        String nameSama = printData._ck == CK._45_TAIGAKU_KYOKA ? printData.getPrintGuardName() : seitoName;
        if (null != nameSama) {
            nameSama = nameSama + "　様";
        }
        form.VrsOutSelectField(Arrays.asList("NAME1", "NAME2"), nameSama); // 氏名

        svf.VrsOut("DATE", PrintData.printsvfDate(db2, printData, printData._certifDate, true, true)); // 作成日
        final String date = PrintData.printsvfDate(db2, printData, printData._certifDate, false, true);  // 証明日付

        String nenkumiban = "第　学年　組　　番";
        if (null != personalInfo) {
            String grade = printData._personalInfo.getString("GRADE");
            if (NumberUtils.isDigits(grade)) {
                grade = String.valueOf(Integer.parseInt(grade));
            }
            String hr = printData._personalInfo.getString("HR_CLASS_NAME1");
            String attendno = printData._personalInfo.getString("ATTENDNO");
            if (NumberUtils.isDigits(attendno)) {
                attendno = String.valueOf(Integer.parseInt(attendno));
            }
            nenkumiban = "第" + StringUtils.defaultString(grade, "　") + "学年 " + StringUtils.defaultString(hr, "　") + "組 " + StringUtils.defaultString(attendno, "  ") + "番";
        }
        final String birthdayStr = Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, true);
        if (printData._ck == CK._43_KYUGAKU_KYOKA) {

            svf.VrsOut("TITLE", "休学（更新）許可書");

            svf.VrsOut("MAIN1", date + "付けで申請のあった休学について、下記のとおり許可します。");
            svf.VrsOut("BOXTEXT1", nenkumiban);
            svf.VrsOut("BOXTEXT2", seitoName);
            svf.VrsOut("BIRTHDAY", birthdayStr);
            final String transferSdateStr = PrintData.printsvfDate(db2, printData, KnjDbUtils.getString(printData._transferDatMap, "TRANSFER_SDATE"), false, true);
            final String transferEdateStr = PrintData.printsvfDate(db2, printData, KnjDbUtils.getString(printData._transferDatMap, "TRANSFER_EDATE"), false, true);
            svf.VrsOut("BOXTEXT3", transferSdateStr + "から" + transferEdateStr + "まで");

        } else if (printData._ck == CK._44_RYUGAKU_KYOKA) {

            svf.VrsOut("TITLE", "留学（更新）許可書");

            svf.VrsOut("MAIN1", date + "付けで申請のあった留学（更新）について、下記のとおり許可します。");
            svf.VrsOut("BOXTEXT1", nenkumiban);
            svf.VrsOut("BOXTEXT2", seitoName);
            svf.VrsOut("BIRTHDAY", birthdayStr);
            svf.VrsOut("BOXTEXT3", KnjDbUtils.getString(printData._transferDatMap, "TRANSFERPLACE"));
            svf.VrsOut("BOXTEXT4", KnjDbUtils.getString(printData._transferDatMap, "TRANSFERADDR"));
            final String transferSdateStr = PrintData.printsvfDate(db2, printData, KnjDbUtils.getString(printData._transferDatMap, "TRANSFER_SDATE"), false, true);
            final String transferEdateStr = PrintData.printsvfDate(db2, printData, KnjDbUtils.getString(printData._transferDatMap, "TRANSFER_EDATE"), false, true);
            svf.VrsOut("BOXTEXT5", transferSdateStr + "から" + transferEdateStr + "まで");

        } else if (printData._ck == CK._45_TAIGAKU_KYOKA) {

            svf.VrsOut("TITLE", "退学許可書");

            svf.VrsOut("MAIN1", nenkumiban + " " + seitoName + "の退学を、" + date + "付けで許可する。");
        }
        printPersonalInfoCommon(form, printData);
        printSchoolJp(db2, form, printData);
        printSchoolCommon(form, printData);

        _hasData = true;
    }

    // 学割証明書
    private void printGakuwari(final DB2UDB db2, final Form form ,final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final PersonalInfo pi = printData._personalInfo;

        printData._formname = "KNJG010_46.frm";
        form.setForm(printData._formname, printData);

        form.VrsOutSelectField(Arrays.asList("NAME", "NAME2", "NAME3"), pi._name);
        form.VrsOut("SCHREGNO", pi._printSchregno);

        final String coursename;
        if (J.equals(printData._certifKindSchoolKind)) {
            coursename = "義務課程";
        } else {
            coursename = "高等課程";
        }
        form.VrsOut("COURSE", coursename); // 課程名
        form.VrsOut("MAJOR", pi._majorname); // 学科名
        if (NumberUtils.isDigits(pi.getGradeCd())) {
            form.VrsOut("GRADE", String.valueOf(Integer.parseInt(pi.getGradeCd()))); // 学年
        }
        form.VrsOut("AGE", pi.getAge(StringUtils.defaultString(printData._certifDate, printData._year + "-04-01"))); // 年齢

        printPersonalInfoCommon(form, printData);
        printSchoolCommon(form, printData);
        printSchoolJp(db2, form, printData);

        _hasData = true;
    }

    // ブランクページ
    private void printBlank(final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);

        printData._formname = "BLANK_A4_TATE.frm";
        form.setForm(printData._formname, printData);
        form.VrsOut("BLANK", "1");

        _hasData = true;
    }

    private String[] splitBySpace(final String name) {
        String name1 = name;
        String name2 = null;

        int spaceIndex = name1 != null ? name1.indexOf(' ') : -1;
        if (spaceIndex == -1) {
            spaceIndex = name1 != null ? name1.indexOf('　') : -1;
        }

        if (spaceIndex != -1 && spaceIndex != name1.length() - 1) {
            name2 = name1.substring(spaceIndex + 1);
            name1 = name1.substring(0, spaceIndex);
            return new String[] {name1, name2};
        }
        return new String[] {name1};
    }

    // 在学証明書(和)
    private void printZaigaku(final DB2UDB db2, final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final PersonalInfo personalInfo = printData._personalInfo;

        final String NAME0 = "NAME0";
        final String NAME1 = "NAME1";
        final String NAME2 = "NAME2";
        final String NAME3 = "NAME3";
        final String NAME4 = "NAME4";
        final String NAME5 = "NAME5";
        final String NAME11 = "NAME11";
        final String NAME12 = "NAME12";
        final String NAME13 = "NAME13";
        final String NAME14 = "NAME14";
        final String NAME15 = "NAME15";
        final String BIRTHDAY = "BIRTHDAY";
        final String TITLE_NAME = "TITLE_NAME";
        final String TITLE_BIRTHDAY = "TITLE_BIRTHDAY";
        final String MAIN1 = "MAIN1";
        final String MAIN2 = "MAIN2";
        final String MAIN3 = "MAIN3";

        final boolean isH = printData._ck.in(CK._4_ZAIGAKU);
        final boolean isJ = J.equals(printData._certifKindSchoolKind);
        final boolean isP = P.equals(printData._certifKindSchoolKind);
        final boolean isK = K.equals(printData._certifKindSchoolKind);

        final String courseName = Utils.addKatei(personalInfo._coursename);

        final String gakunen = getPrintGrade(printData);
        if (_param._z010.in(Z010.Chukyo, Z010.Kyoai, Z010.Chiben, Z010.Hirokoudai)) {
            if (_param._z010.in(Z010.Chukyo, Z010.Kyoai)) { // 鳥取 中京 共愛・在学証明書
                if (_param._z010.in(Z010.Kyoai) && isJ) {
                    printData._formname = "KNJG010_4KYOAI.frm";
                    form.setForm(printData._formname, printData);
                    form.VrsOut(MAIN1, "　上記の者は本校 " + gakunen + " に在学中である");
                    form.VrsOut(MAIN2, "ことを証明します");
                } else {
                    final String[] split = splitBySpace(personalInfo._majorname);
                    String major1 = personalInfo._majorname;
                    String major2 = null;

                    boolean useLine3 = false;
                    boolean nospace = false;

                    if (split.length > 1) {
                        major2 = split[1];
                        major1 = split[0];
                        if (major2.length() > 4) {
                            useLine3 = true;
                        }
                    } else {
                        nospace = true;
                    }

                    if (useLine3) {
                        printData._formname = _param._z010.in(Z010.Kyoai) ? "KNJG010_4KYOAI2.frm" : "KNJG010_4TORI2.frm"; // 3行フォーム
                        form.setForm(printData._formname, printData);

                        form.VrsOut(MAIN1, "　上記の者は本校 " + courseName + " " + major1);
                        form.VrsOut(MAIN2, major2 + " " + gakunen + " に在学中であることを");
                        form.VrsOut(MAIN3, "証明します");

                    } else if (nospace) {
                        printData._formname = _param._z010.in(Z010.Kyoai) ? "KNJG010_4KYOAI.frm" : "KNJG010_4TORI.frm"; // 2行フォーム
                        form.setForm(printData._formname, printData);
                        form.VrsOut(MAIN1, "　上記の者は本校 " + courseName + " " + major1 + " " + gakunen + " に");
                        form.VrsOut(MAIN2, "在学中であることを証明します");
                    } else {
                        printData._formname = _param._z010.in(Z010.Kyoai) ? "KNJG010_4KYOAI.frm" : "KNJG010_4TORI.frm"; // 2行フォーム
                        form.setForm(printData._formname, printData);
                        form.VrsOut(MAIN1, "　上記の者は本校 " + courseName + " " + major1 + " " + major2);
                        form.VrsOut(MAIN2, gakunen + " に在学中であることを証明します");
                    }
                }
                if (_param._z010.in(Z010.Kyoai)) {
                    form.VrAttribute(MAIN1, "X=204");
                    form.VrAttribute(MAIN2, "X=204");
                    form.VrAttribute(MAIN3, "X=204");
                }
                log.debug(" 在学証明書 form = " + printData._formname);
                form.VrsOutSelectField(Arrays.asList(NAME1,  NAME2), personalInfo.getPrintName(printData, _param)); // 氏名
                form.VrsOut(BIRTHDAY, Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  //生年月日
                form.VrsOut("ENT_DATE", Utils.getSeirekiFlgDateString(db2, printData, personalInfo._entDate, YMD, null, false));
            } else if (_param._z010.in(Z010.Chiben)) { // 智辯
                String filename = null;
                if (isH) {
                    filename = "KNJG010_4A.frm";  // 在学証明書（和･智辯）
                } else if (isJ) {
                    filename = "KNJG010_4A2.frm";  // 在学証明書（和･智辯）
                } else if (isP) {
                    filename = "KNJG010_4A2.frm";
                }
                printData._formname = filename;
                form.setForm(printData._formname, printData);

                form.VrsOut("COURSE", defstr(personalInfo._coursename));
                form.VrsOut("SUBJECT", defstr(personalInfo._majorname));
                form.VrsOut("GRADE", gakunen);
                form.VrsOutSelectField(Arrays.asList(NAME1,  NAME2), personalInfo.getPrintName(printData, _param)); // 氏名
                form.VrsOut(BIRTHDAY, Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  //生年月日
                form.VrsOut("ENT_DATE", Utils.getSeirekiFlgDateString(db2, printData, personalInfo._entDate, YMD, null, false));
            } else if (_param._z010.in(Z010.Hirokoudai)) {
                printData._formname = "KNJG010_4HIROKOUDAI.frm";  // 在学証明書
                form.setForm(printData._formname, printData);
                form.VrImageOut("SCHOOLLOGO", StringUtils.defaultString(printData._schoollogoHJpgImagePath, printData._schoollogoJpgImagePath));
                form.VrsOut("COURSE", defstr(personalInfo._coursename));
                form.VrsOut("SUBJECT", defstr(personalInfo._majorname));
                form.VrsOut("GRADE", gakunen);
                form.VrsOutSelectField(Arrays.asList(NAME1,  NAME2), personalInfo.getPrintName(printData, _param)); // 氏名
                form.VrsOut(BIRTHDAY, Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  //生年月日
                printSchoolCommon(form, printData);
                printSchoolJp(db2, form, printData);
                _hasData = true;
                return;
            }
        }
        String filename = null;
        if (isK) {
            filename = "KNJG010_4_2MUSAHIGA.frm";
        } else if (_param._z010.in(Z010.Ritsumeikan)) {
            filename = "KNJG010_4KEISHO.frm";
        } else if (_param._z010.in(Z010.Osakatoin)) {
            filename = "KNJG010_4TOIN.frm";
        } else if (_param._z010.in(Z010.Meikei)) {
            filename = "KNJG010_4MEIKEI.frm";
        } else if (_param._z010.in(Z010.Musashinohigashi)) {
            filename = "KNJG010_4MUSAHIGA.frm";
        } else if (_param._z010.in(Z010.Rakunan)) {
            filename = "KNJG010_4RAKUNAN.frm";
        } else if (_param._z010.in(Z010.Sapporo)) {
            filename = "KNJG010_4SAP.frm";
        } else if (_param._z010.in(Z010.Sanonihon)) {
            filename = "KNJG010_4SANONICHI.frm";
        } else if (_param._z010.in(Z010.jyoto)) {
            filename = "KNJG010_4_JYOTO.frm";
        } else if (isH) {
            filename = "KNJG010_4.frm";
//            printData._printStamp = true;
        } else if (isJ) {
            if ("1".equals(printData._schoolInfo._remark7)) {
                filename = "KNJG010_4A2.frm";  // 備考7='1'のときに使用
            } else {
                filename = "KNJG010_4.frm";
            }
//            printData._printStamp = true;
        } else if (isP) {
            filename = "KNJG010_4.frm";
        }
        printData._formname = filename;
        form.setForm(printData._formname, printData);

        //学籍番号
        form.VrsOut("SCHREGNO", personalInfo._printSchregno);

        final List<String> nameFields = Arrays.asList(NAME0, NAME1, NAME2, NAME3, NAME4, NAME5);
        form.VrsOutSelectField(nameFields, personalInfo.getPrintName(printData, _param)); // 氏名

        form.VrsOut(BIRTHDAY, Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  // 生年月日
        form.VrsOut("ENT_DATE", Utils.getSeirekiFlgDateString(db2, printData, personalInfo._entDate, YMD, null, false));

        if (isK) {
            final String divName = "園児";
            form.VrsOut(TITLE_NAME, divName + "名");
            form.VrsOut(MAIN1, "　上記の" + divName + "は、本園に在園している");
            form.VrsOut(MAIN2, "ことを証明いたします。");
            printSchoolCommon(form, printData);
            printSchoolJp(db2, form, printData);
            _hasData = true;
            return;
        }
        if (_param._z010.in(Z010.Musashinohigashi)) { // 武蔵野東
            final String divName = isP ? "児童" : "生徒";
            form.VrsOut(TITLE_NAME, divName + "名");
            form.VrsOut(MAIN1, "　上記の" + divName + "は、本校" + gakunen + "に在学して");
            form.VrsOut(MAIN2, "いる事を証明します。");
            if ("1".equals(personalInfo._baseDetailMstSeq008BaseRemark1)) {
                form.VrsOut(MAIN3, "※日本スポーツ振興センター加入済み");
            }

            if (H.equals(printData._certifKindSchoolKind)) {
                final int xplus = 200;
                getMappedList(form._attributeMap, TITLE_NAME).add(Attribute.plusX(TITLE_NAME, xplus, form));
                for (final String field : nameFields) {
                    getMappedList(form._attributeMap, field).add(Attribute.plusX(field, xplus, form));
                }
                getMappedList(form._attributeMap, BIRTHDAY).add(Attribute.plusX(BIRTHDAY, xplus + 200, form));
            } else if (P.equals(printData._certifKindSchoolKind)) {
                getMappedList(form._attributeMap, "CERTIF_NAME").add(Attribute.charSize(15.0));
            }
            setAttribute(form);

            printSchoolCommon(form, printData);
            printSchoolJp(db2, form, printData);
            _hasData = true;
            return;
        }

        final List<String> name1Fields = Arrays.asList(NAME11, NAME12, NAME13, NAME14, NAME15);
        if (personalInfo.isPrintNameBoth()) {
            form.VrsOutSelectField(name1Fields, "（" + personalInfo._name + "）"); //氏名
        }

        if (_param._z010.in(Z010.NichidaiNikou)) {
            form.VrsOut("HR_NAME", printData._personalInfo.getString("HR_NAME"));
        }

        // 「在学証明書」
        String titleField = "TITLE";

        final int defaultMain1pCharPoint = 20;
        final String title;
        final List<String> template = new ArrayList<String>();
        final int mainx1 = 316, mainx2 = 2828;
        final KNJSvfFieldModify MAIN1modify = new KNJSvfFieldModify(MAIN1, mainx1, mainx2, defaultMain1pCharPoint, 2222, 5, 70);
        double newCharPoint = 0.0;
        boolean modifyPoint = true;

        if (_param._z010.in(Z010.Sapporo)) { // 札幌
            if (isJ || isP) {
                title = "前期課程在学証明書";
                template.add("上記の者は " + PrintData.trim(printData._schoolInfo._schoolname1) + " 前期課程 " + gakunen + "に在学している者である");
                template.add("ことを証明します");
            } else {
                title = "後期課程在学証明書";
                final String coursename = defstr(personalInfo._coursename);
                final String majorname = Utils.append(personalInfo._majorname, " ");
                template.add("上記の者は " + PrintData.trim(printData._schoolInfo._schoolname1) + " " + coursename + majorname + " " + gakunen);
                template.add("に在学している者であることを証明します");
            }
            newCharPoint = MAIN1modify.getCharPoint(template.get(0));
            log.debug(" newCharPoint = " + newCharPoint);

        } else {
            // 賢者
            title = isK ? "在園証明書" : "在学証明書";

            if (_param._z010.in(Z010.Nagisa)) { // 広島なぎさ
                modifyPoint = false;
                newCharPoint = defaultMain1pCharPoint;

                template.add("　上記の者は本校" + gakunen + "に在学中であることを");
                template.add("証明する");

                form.VrsOut(TITLE_NAME, "氏名");
                getMappedList(form._attributeMap, TITLE_NAME).addAll(Arrays.asList(Attribute.charSize(15), Attribute.MIGITSUME, "Edit=", Attribute.plusX(TITLE_NAME, -75, form), Attribute.plusY(TITLE_NAME, -15, form))); // 左詰め編集式カット
                getMappedList(form._attributeMap, TITLE_BIRTHDAY).add("X=" + String.valueOf(INVALID_X));

            } else if (_param._z010.in(Z010.Meikei)) { // 茗溪
                modifyPoint = false;
                newCharPoint = defaultMain1pCharPoint;

                template.add("上記の者は、本学園" + gakunen + "に在学して");
                template.add("いることを証明します。");
                if (printData._personalInfo._inDomitory) {
                    template.add("なお、本学園寮に入寮しています。");
                }
            } else if (_param._z010.in(Z010.Ritsumeikan)) { // 立命館慶祥
                modifyPoint = false;
                newCharPoint = defaultMain1pCharPoint;

                final String honkou = " 上記の者は、本校";
                final String coursename = defstr(Utils.addKatei(personalInfo._coursename));
                final String majorname = Utils.append(personalInfo._majorname, " ");

                String headSpace = StringUtils.repeat("　", 1);

                template.add(headSpace + honkou + "の" + coursename + " " + majorname);
                template.add(headSpace + gakunen + "に在学中であることを証明します。");

                form.VrsOut("KANA", personalInfo.getNameKana());

            } else if (_param._z010.in(Z010.Kumamoto)) { // 熊本

                newCharPoint = defaultMain1pCharPoint;

                String headSpace = "";

                final String honkou = "上記の者は 本校 ";
                final String courseMajor = defstr(Utils.addKatei(personalInfo._coursename)) + " " + Utils.append(personalInfo._majorname, " ");

                if (getMS932ByteLength(honkou + courseMajor) >= 50) {
                    template.add(headSpace + honkou);
                    template.add(headSpace + courseMajor);
                    template.add(headSpace + gakunen + "に在学中であることを証明します。");
                } else {
                    template.add(headSpace + honkou + courseMajor);
                    template.add(headSpace + gakunen + "に在学中であることを証明します。");
                }

            } else if (_param._z010.in(Z010.Sakae) && isJ) { // 埼玉栄中学

                String headSpace = StringUtils.repeat("　", 4);

                newCharPoint = defaultMain1pCharPoint;
                template.add(headSpace + "上記の者は 本校 " + gakunen + "に");
                template.add(headSpace + "在学中であることを証明します。");

            } else if (_param._z010.in(Z010.Osakatoin)) { // 大阪桐蔭
                newCharPoint = defaultMain1pCharPoint;
                template.add("上記の者 本校 " + gakunen + "に在学中であることを証明します。");
                template.add("");

            } else if (_param._z010.in(Z010.Risshisha)) { // 立志舎
                newCharPoint = defaultMain1pCharPoint;

                String katei = personalInfo._majorname2;
                if (StringUtils.isBlank(katei)) {
                    katei = personalInfo._majorname;
                }
                final String[] split = splitBySpace(personalInfo._majorname2);
                if (split.length > 1) {
                    katei = split[0];
                }
                template.add("上記の者は本校" + defstr(katei) + "の課程に在学中であることを");
                template.add("証明します");

            } else if (_param._z010.in(Z010.Sundaikoufu) && (isJ || isP)) { // 駿台甲府

                template.add("上記の者は 本校 " + (gakunen + "に") + "在学中であることを");
                template.add("証明します。");
                newCharPoint = MAIN1modify.getCharPoint(template.get(0));
                log.debug(" newCharPoint = " + newCharPoint);

                // 住所
                form.VrsOut("TITLE_ADDR", "住所");
                if (getMS932ByteLength(personalInfo._addr1) > 50 || getMS932ByteLength(personalInfo._addr2) > 50) {
                    form.VrsOut("ADDR1_2", personalInfo._addr1);
                    form.VrsOut("ADDR2_2", personalInfo._addr2);
                } else {
                    form.VrsOut("ADDR1", personalInfo._addr1);
                    form.VrsOut("ADDR2", personalInfo._addr2);
                }
            } else if (_param._z010.in(Z010.NichidaiNikou)) { // 日大二校
                newCharPoint = defaultMain1pCharPoint;
                template.add("　上記の者は 本校に在学する生徒であることを");
                template.add("証明します。");

            } else {
                final String _改行候補1 = "{{改行候補1}}";
                final String _改行候補2 = "{{改行候補2}}";
                final String _改行候補3 = "{{改行候補3}}";
                String headSpace = "";
                final String katei = defstr(Utils.addKatei(personalInfo._coursename));
                final String gakka = Utils.append(personalInfo._majorname, _param._z010.in(Z010.jyoto) ? "" : " ");
                final String text;
                if (_param._z010.in(Z010.Nishiyama)) {
                    text = "上記の者は 本校 " + Utils.aidaNiInsert(katei, " ", gakka) + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明する。";
                } else if (_param._z010.in(Z010.Miyagiken)) {
                    headSpace = StringUtils.repeat("　", 3);
                    text = "上の者は 本校 " + gakka + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明する";
                } else if (_param._z010.in(Z010.Mieken)) {
                    headSpace = StringUtils.repeat("　", 3);
                    text = "　上記の者は 本校 " + gakka + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明する";

                    // 「名　　前」
                    form.VrAttribute(TITLE_NAME, "Edit="); // 編集式カット
                    form.VrsOut(TITLE_NAME, "名　　前");

                } else if (_param._z010.in(Z010.Kyoto)) {
                    headSpace = StringUtils.repeat("　", 2);
                    text = "　上記の者は 本校" + gakka + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明する。";

                    getMappedList(form._attributeMap, TITLE_NAME).addAll(Arrays.asList(Attribute.charSize(15.0), Attribute.plusX(TITLE_NAME, 0, form), Attribute.plusY(TITLE_NAME, -16, form)));
                    getMappedList(form._attributeMap, TITLE_BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(15.0), Attribute.plusX(TITLE_BIRTHDAY, 0, form), Attribute.plusY(TITLE_BIRTHDAY, -18, form)));
                    for (final String field : Utils.concat(nameFields, "NAME5_2")) {
                        getMappedList(form._attributeMap, field).add(Attribute.plusX(field, 160, form));
                    }
                    for (final String field : Utils.concat(name1Fields, "NAME15_2")) {
                        getMappedList(form._attributeMap, field).add(Attribute.plusX(field, 160, form));
                    }
                    getMappedList(form._attributeMap, BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(15.0), Attribute.plusX(BIRTHDAY, 160, form), Attribute.plusY(BIRTHDAY, -15, form), Attribute.MUHENSHU));

                } else if (_param._z010.in(Z010.ChiyodaKudan)) {
                    headSpace = StringUtils.repeat("　", 3);
                    if (isJ) {
                        text = "上記の者は 本校 前期課程" + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明します。";
                    } else {
                        text = "上記の者は 本校 後期課程" + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明します。";
                    }
                } else if (_param._z010.in(Z010.Seijyo)) {
                    if (isJ) {
                        text = "　上記のものは本校" + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明します。";
                    } else {
                        text = "上記のものは本校" + gakka + _改行候補2 + gakunen + " に" + _改行候補1 + "在学中であることを証明します。";
                    }

                    titleField = "TITLE2";

                    getMappedList(form._attributeMap, titleField).add(Attribute.setKeta(16));
                    getMappedList(form._attributeMap, titleField).add(Attribute.plusX(titleField, 350, form));
                    getMappedList(form._attributeMap, MAIN1).add(Attribute.KINTOUWARI);
                    getMappedList(form._attributeMap, MAIN1).add(Attribute.setKeta(36));
                    getMappedList(form._attributeMap, MAIN1).add(Attribute.setX(500));
                    getMappedList(form._attributeMap, MAIN2).add(Attribute.setKeta(36));
                    getMappedList(form._attributeMap, MAIN2).add(Attribute.setX(500));
                } else if (_param._z010.in(Z010.Rakunan)) {
                    if (isJ) {
                        headSpace = StringUtils.repeat("　", 5);
                        text = "上記の者は 本校 " + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明します。";
                    } else {
                        headSpace = StringUtils.repeat("　", 4);
                        text = "上記の者は 本校 " + gakka + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明します。";
                    }
                } else if (_param._z010.in(Z010.Meiji)) {
                    titleField = "TITLE2";
                    if (isJ || isP) {
                        headSpace = StringUtils.repeat("　", 4);
                        text = "上記の者は 本校 " + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明します。";
                    } else {
                        text = "上記の者は 本校 " + Utils.aidaNiInsert(katei, " ", gakka) + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明します。";
                    }

                    form.VrAttribute(TITLE_NAME, "Size=11"); // 元は9.6
                    form.VrAttribute(TITLE_BIRTHDAY, "Size=11"); // 元は9.6
                    form.VrAttribute(NAME1, "Size=17"); // 元は15.7
                    form.VrAttribute(NAME2, "Size=12"); // 元は10.4
                    form.VrAttribute(NAME11, "Size=17"); // 元は15.7
                    form.VrAttribute(NAME12, "Size=12"); // 元は10.4
                    form.VrAttribute(BIRTHDAY, "Size=12"); // 元は10.4

                } else if (_param._z010.in(Z010.Matsudo)) {
                    text = "上記の者は 本校 " + Utils.aidaNiInsert(katei, " ", gakka) + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明する。";
                } else if (_param._z010.isKaichiSpec()) {
                    if (isJ) {
                        headSpace = StringUtils.repeat("　", 5);
                        text = "上記の者は 本校 " + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明します。";
                    } else {
                        headSpace = StringUtils.repeat("　", 4);
                        text = "上記の者は 本校 " + gakka + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明します。";
                    }

                    getMappedList(form._attributeMap, TITLE_NAME).addAll(Arrays.asList(Attribute.charSize(16.0), Attribute.plusX(TITLE_NAME, -140, form), Attribute.plusY(TITLE_NAME, 0, form)));
                    getMappedList(form._attributeMap, TITLE_BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(16.0),Attribute.plusX(TITLE_BIRTHDAY, -140, form), Attribute.plusY(TITLE_BIRTHDAY, 0, form)));
                    getMappedList(form._attributeMap, NAME1).addAll(Arrays.asList(Attribute.charSize(22)));
                    getMappedList(form._attributeMap, BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(16.0)));

                } else if (_param._z010.in(Z010.jyoto)) {
                    text = "上記の者は 本校 " + gakunen + gakka + "に" + _改行候補1 + "在学中であることを証明する。";
                } else if (_param._z010.in(Z010.Reitaku)) {
                    if (isJ) {
                        text = "　上記の者は 本校 " + _改行候補3 + gakunen + "に" + _改行候補2 + "在学中であることを" + _改行候補1 + "証明します。";
                    } else {
                        text = "上記の者は 本校 " + Utils.aidaNiInsert(katei, " ", gakka) + _改行候補3 + gakunen + "に" + _改行候補2 + "在学中であることを" + _改行候補1 + "証明します。";
                    }
                } else if (_param._z010.in(Z010.suito)) {
                    headSpace = StringUtils.repeat("　", 1);
                    text = "　上記の者は、本校 " + gakunen + "に在学している" + _改行候補1 + "ことを証明いたします。";
                    form.VrsOut(TITLE_NAME, "名　前　");

                    getMappedList(form._attributeMap, TITLE_NAME).addAll(Arrays.asList(Attribute.charSize(15.0), Attribute.plusX(TITLE_NAME, -140, form), Attribute.plusY(TITLE_NAME, -15, form), Attribute.MUHENSHU, "Edit="));
                    getMappedList(form._attributeMap, TITLE_BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(15.0),Attribute.plusX(TITLE_BIRTHDAY, -140, form), Attribute.plusY(TITLE_BIRTHDAY, 0, form)));
                    getMappedList(form._attributeMap, BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(15.0)));

                } else {
                    text = "上記の者は 本校 " + Utils.aidaNiInsert(katei, " ", gakka) + _改行候補2 + gakunen + "に" + _改行候補1 + "在学中であることを証明します。";
                }

                if (_param._z010.in(Z010.Reitaku)) {
                    form.addAttributes(commonAttributes(Z010.Reitaku, form, TITLE_NAME, NAME1, TITLE_BIRTHDAY, BIRTHDAY, nameFields, name1Fields));
                }

                if (_param._z010.in(Z010.Sanonihon)) {
                    modifyPoint = false;
                }

                final List<String> _改行候補s = new ArrayList(Arrays.asList(_改行候補1, _改行候補2, _改行候補3));
                for (final Iterator<String> it = _改行候補s.iterator(); it.hasNext();) {
                    final String _改行候補 = it.next();
                    if (!text.contains(_改行候補)) {
                        it.remove();
                    }
                }

                setTemplateAndCharPoint:
                for (int i = 0; i < _改行候補s.size(); i++) {
                    final String _改行候補 = _改行候補s.get(i);
                    final String[] split1 = splitPat(text, _改行候補, "{{改行候補[0-9]+}}");

                    newCharPoint = MAIN1modify.getCharPoint(headSpace + split1[0]);
                    log.debug(" newCharPoint = " + newCharPoint);

                    if (newCharPoint < 15.0 && i < _改行候補s.size() - 1) {
                        continue;
                    }

                    template.add(headSpace + split1[0]);
                    template.add(headSpace + split1[1]);
                    newCharPoint = MAIN1modify.getCharPoint(template.get(0));
                    break setTemplateAndCharPoint;
                }
            }
        }

        if (_param._z010.in(Z010.Sakae)) {
            getMappedList(form._attributeMap, NAME1).addAll(Arrays.asList(Attribute.charSize(26.1), Attribute.plusY(NAME1, -40, form))); // タイトルと同じ
            getMappedList(form._attributeMap, BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(14.8), Attribute.plusY(BIRTHDAY, -15, form))); // 日付と同じ
        }

        double rate = newCharPoint / defaultMain1pCharPoint;
        if (modifyPoint && -1.0f != newCharPoint) {
            //log.debug(" modified charSize = " + newCharPixel);
            form.VrAttribute(MAIN1modify._fieldname, Attribute.charSize(newCharPoint));
            form.VrAttribute(MAIN1modify._fieldname, "Y=" + String.valueOf((int) MAIN1modify.getYPixel(0, KNJSvfFieldModify.charPointToPixel(newCharPoint))));
        }
        form.VrsOut(titleField, title);
        if (template.size() > 2) {
            form.VrsOut(MAIN1, template.get(0));
            form.VrsOut("MAIN2_2", template.get(1));
            form.VrsOut("MAIN3_2", template.get(2));
        } else {
            form.VrsOut(MAIN1, template.get(0));
            form.VrsOut(MAIN2, template.get(1));
        }
        if (_param._isOutputDebug) {
            log.info(" MAIN1 " + template.get(0));
            log.info(" MAIN2 " + template.get(1));
            if (template.size() > 2) {
                log.info(" MAIN3 " + StringUtils.defaultString(template.get(2)));
            }
        }

        if (modifyPoint && rate < 0.99) {
            //log.debug(" rate = " + rate);
            final KNJSvfFieldModify modify2 = new KNJSvfFieldModify(MAIN2, mainx1, mainx2, defaultMain1pCharPoint, 2492, 5, 70);
            final double nCharPoint = modify2._charPoint * rate;
            final double nCharPixel = KNJSvfFieldModify.charPointToPixel(nCharPoint);
            getMappedList(form._attributeMap, modify2._fieldname).addAll(Arrays.asList(Attribute.charSize(nCharPoint), Attribute.setY((int) modify2.getYPixel(0, nCharPixel))));
        }

        setAttribute(form);

        printPersonalInfoCommon(form, printData);
        printSchoolCommon(form, printData);
        printSchoolJp(db2, form, printData);

        _hasData = true;
    }

    private static String[] splitPat(final String s, final String splitString0, final String replacePattern0) {
        final String splitPattern = splitString0.replaceAll("\\{\\{", "\\\\{\\\\{").replaceAll("\\}\\}", "\\\\}\\\\}");
        final String replacePattern = replacePattern0.replaceAll("\\{\\{", "\\\\{\\\\{").replaceAll("\\}\\}", "\\\\}\\\\}");
        final String[] split = s.split(splitPattern);
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].replaceAll(replacePattern, "");
        }
        return split;
    }

    // 卒業証明書(和)
    private void printSotsugyo(final DB2UDB db2, final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final PersonalInfo personalInfo = printData._personalInfo;
        final String katei = Utils.addKatei(personalInfo._coursename);
        final String gakka = defstr(personalInfo._majorname);

        final String word_卒業 = "1".equals(printData._useShuryoShoumeisho) ? "修了" : "卒業";
        final String NAME0 = "NAME0";
        final String NAME1 = "NAME1";
        final String NAME2 = "NAME2";
        final String NAME3 = "NAME3";
        final String NAME4 = "NAME4";
        final String NAME5 = "NAME5";
        final String NAME11 = "NAME11";
        final String NAME12 = "NAME12";
        final String NAME13 = "NAME13";
        final String NAME14 = "NAME14";
        final String NAME15 = "NAME15";
        final String BIRTHDAY = "BIRTHDAY";
        String TITLE_NAME = "TITLE_NAME";
        final String TITLE_BIRTHDAY = "TITLE_BIRTHDAY";
        final String MAIN1 = "MAIN1";
        final String MAIN2 = "MAIN2";
        final String MAIN3 = "MAIN3";
        final String GRD_DATE = "GRD_DATE";
        if (false) {
        } else if (_param._z010.in(Z010.Chukyo, Z010.Kyoai)) {
            // 中京は鳥取用を使用する

            final String[] split = splitBySpace(gakka);

            // 卒業証明書
            final String format = "1".equals(printData._entGrdDateFormat) ? YM : YMD;
            final String graduation = Utils.getSeirekiFlgDateString(db2, printData, personalInfo._graduDate, format, null, false);

            printData._formname = _param._z010.in(Z010.Chukyo) ? "KNJG010_1CYUKYO.frm" : _param._z010.in(Z010.Kyoai) ? "KNJG010_1KYOAI.frm" : "KNJG010_1TORI.frm";
            form.setForm(printData._formname, printData);
            if (split.length == 2) {
                form.VrsOut(MAIN1, "　上記の者は" + graduation + katei + split[0]);
                form.VrsOut(MAIN2, split[1] + "を" + word_卒業 + "したことを証明します");
            } else {
                form.VrsOut(MAIN1, "　上記の者は " + graduation + " " + katei);
                form.VrsOut(MAIN2, gakka + " を" + word_卒業 + "したことを証明します");
            }

            form.VrsOutSelectField(Arrays.asList(NAME1, NAME2), personalInfo.getPrintName(printData, _param)); //氏名
            if (personalInfo.isPrintNameBoth()) {
                form.VrsOutSelectField(Arrays.asList(NAME11, NAME12), "（" + personalInfo._name + "）"); //氏名
            }

            form.VrsOut(BIRTHDAY, Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));
            form.VrsOut("ENT_DATE", Utils.getSeirekiFlgDateString(db2, printData, personalInfo._entDate, YMD, null, false));

        } else if (_param._z010.in(Z010.Hirokoudai)) {
            printData._formname = "KNJG010_1HIROKOUDAI.frm";  // 卒業証明書
            form.setForm(printData._formname, printData);
            form.VrImageOut("SCHOOLLOGO", StringUtils.defaultString(printData._schoollogoHJpgImagePath, printData._schoollogoJpgImagePath));
            final String format = "1".equals(printData._entGrdDateFormat) ? YM : YMD;
            final boolean graduDateStrZenkaku = true;
            final String graduDateStr = Utils.getSeirekiFlgDateString(db2, printData, personalInfo._graduDate, format, null, graduDateStrZenkaku);
            form.VrsOut("GRADU_DATE", graduDateStr);
            form.VrsOut("COURSE", defstr(personalInfo._coursename));
            form.VrsOut("SUBJECT", gakka);
            form.VrsOutSelectField(Arrays.asList(NAME1,  NAME2), personalInfo.getPrintName(printData, _param)); //氏名
            form.VrsOut(BIRTHDAY, Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  //生年月日
            printSchoolCommon(form, printData);
            printSchoolJp(db2, form, printData);
            _hasData = true;
            return;
        } else {

            final boolean isH = H.equals(printData._certifKindSchoolKind) || printData._ck.in(CK._1_SOTSU);
            final boolean isJ = J.equals(printData._certifKindSchoolKind);
            final boolean isP = P.equals(printData._certifKindSchoolKind);
            String filename = null;
            if (_param._z010.in(Z010.Meikei)) {
                filename = "KNJG010_1MEIKEI.frm";
            } else if (_param._z010.in(Z010.Sapporo)) {
                filename = "KNJG010_1SAP.frm";
            } else if (_param._z010.in(Z010.Musashinohigashi)) {
                filename = "KNJG010_1MUSAHIGA.frm";
            } else if (_param._z010.in(Z010.Rakunan)) {
                filename = "KNJG010_1RAKUNAN.frm";
            } else if (_param._z010.in(Z010.Kyoto)) {
                filename = Form.KNJG010_1KYOTO;
            } else if (_param._z010.in(Z010.Chiben)) {
                if (isH) {
                    filename = "KNJG010_1A.frm";
                } else if (isJ || isP) {
                    filename = "KNJG010_1A2.frm";
                }
            } else if (_param._z010.in(Z010.Osakatoin)) {
                filename = "KNJG010_1TOIN.frm";
            } else if (_param._z010.in(Z010.Sakae)) {
                filename = "KNJG010_1SAKAE.frm";
            } else if (_param._z010.in(Z010.ChiyodaKudan) && isJ) {
                filename = "KNJG010_1JKUDAN.frm";
            } else if (_param._z010.in(Z010.Sanonihon)) {
                filename = "KNJG010_1SANONICHI.frm";
            } else if (_param._z010.in(Z010.Ritsumeikan)) {
                filename = "KNJG010_1KEISHO.frm";
            } else if (_param._z010.in(Z010.jyoto)) {
                filename = "KNJG010_1_JYOTO.frm";
            } else {
                filename = "KNJG010_1.frm";
            }
            printData._formname = filename;
            form.setForm(printData._formname, printData);

            final int defaultMain1pCharPoint = 20;
            // TODO: isKutenNashi == trueにする
            final boolean isKutenNashi = _param._z010.in(Z010.Sapporo, Z010.Miyagiken, Z010.Mieken, Z010.Seijyo, Z010.Meikei, Z010.Nagisa, Z010.Higashiosaka, Z010.Musashinohigashi, Z010.jyoto);
            final String kuten = isKutenNashi ? "" : "。";

            String head = "";

            final List<String> template = new ArrayList();

            boolean changeFontSize = true;
            final boolean graduDateStrZenkaku = _param._z010.in(Z010.Seijyo);
            final boolean graduDateAddSpace = _param._z010.in(Z010.Nagisa);
            final String format = "1".equals(printData._entGrdDateFormat) ? YM : YMD;
            final List<String> nameFields = Arrays.asList(NAME0, NAME1, NAME2, NAME3, NAME4, NAME5);
            final List<String> name1Fields = Arrays.asList(NAME11,  NAME12, NAME13, NAME14, NAME15);
            final String graduDateStr = Utils.getSeirekiFlgDateString(db2, printData, personalInfo._graduDate, format, null, graduDateStrZenkaku, graduDateAddSpace);
            if (_param._z010.in(Z010.Sapporo)) {
                template.add("上記の者は " + graduDateStr + " " + PrintData.trim(defstr(printData._schoolInfo._schoolname1)) + " " + gakka);
                template.add("を" + word_卒業 + "した者であることを証明します");
            } else if (_param._z010.in(Z010.Musashinohigashi)) {
                form.VrsOut(TITLE_NAME, "生徒名");

                changeFontSize = false;
                template.add("上記の者は " + graduDateStr);
                String courseMajor;
                if (H.equals(personalInfo._schoolKind)) {
                    courseMajor = defstr(personalInfo._coursename) + gakka;
                } else {
                    courseMajor = gakka;
                }
                template.add("本校" + courseMajor);
                template.add("を" + word_卒業 + "したことを証明する。");

                if (H.equals(printData._certifKindSchoolKind)) {
                    final int xplus = 800;
                    getMappedList(form._attributeMap, TITLE_NAME).add(Attribute.plusX(TITLE_NAME, xplus, form));
                    getMappedList(form._attributeMap, NAME1).add(Attribute.plusX(NAME1, xplus, form));
                    getMappedList(form._attributeMap, NAME2).add(Attribute.plusX(NAME2, xplus, form));
                }

            } else if (_param._z010.in(Z010.Miyagiken)) {
                template.add("　上の者は " + graduDateStr + " 本校 " + gakka + " の課程を");
                template.add(word_卒業 + "したことを証明する");
            } else if (_param._z010.in(Z010.Mieken)) {
                head = "　　";
                form.VrAttribute(TITLE_NAME, "Edit="); // 編集式カット
                form.VrsOut(TITLE_NAME, "名　　前");
                template.add("上記の者は " + graduDateStr + " 本校 " + gakka + "");
                template.add("の課程を" + word_卒業 + "したことを証明する");
            } else if (_param._z010.in(Z010.Seijyo)) {
                if (isJ) {  // 卒業証明書
                    template.add("上記のものは本校を");
                    template.add(graduDateStr + word_卒業 + "したことを証明する");

                    final String attributeX = Attribute.plusX(MAIN1, 200, form);
                    final int keta = getMS932ByteLength(template.get(1));
                    getMappedList(form._attributeMap, MAIN1).addAll(Arrays.asList(Attribute.KINTOUWARI, "Keta=" + String.valueOf(keta), attributeX)); // 均等割
                    getMappedList(form._attributeMap, MAIN2).addAll(Arrays.asList(Attribute.KINTOUWARI, "Keta=" + String.valueOf(keta), attributeX)); // 均等割
                } else { // if (isH) {  // 卒業証明書（和）
                    String courseMajor = defstr(katei) + gakka;
                    template.add("　上記のものは本校" + courseMajor + "を");
                    template.add(graduDateStr + word_卒業 + "したことを証明する");

                    final String attributeX = Attribute.plusX(MAIN1, 200, form);
                    final int keta = getMS932ByteLength(template.get(1));
                    getMappedList(form._attributeMap, MAIN1).addAll(Arrays.asList("Keta=" + String.valueOf(keta), attributeX)); // 均等割
                    getMappedList(form._attributeMap, MAIN2).addAll(Arrays.asList(Attribute.KINTOUWARI, "Keta=" + String.valueOf(keta + 1), attributeX)); // 均等割
                }
            } else if (_param._z010.in(Z010.Meikei)) {
                changeFontSize = false;

                if (isJ) {
                    template.add("上記の者は" + graduDateStr + "、茗溪学園中学校");
                    template.add("を" + word_卒業 + "したことを証明します。");
                } else {
                    template.add("上記の者は" + graduDateStr + "、茗溪学園高等学校");
                    template.add(defstr(personalInfo._coursename) + gakka + "を" + word_卒業 + "したことを証明します。");
                }

                if (isH) {
                    getMappedList(form._attributeMap, MAIN2).add(Attribute.HIDARITSUME);
                }

            } else if (_param._z010.in(Z010.Osakatoin)) {
                changeFontSize = false;
                template.add("　上記の者は" + graduDateStr + " 本校" + Utils.prepend(" ", Utils.append(gakka, " ")) + "3ヶ年の課程を");
                template.add("修了したことを証明します");
            } else if (_param._z010.in(Z010.ChiyodaKudan) && isJ) {
                changeFontSize = false;
                template.add("　上記の者は　" + graduDateStr);
                template.add("本校において、義務教育の課程を修了した");
                template.add("ことを証明する。");
            } else if (_param._z010.in(Z010.Ritsumeikan)) {
                head = " ";
                template.add("上記の者は、" + graduDateStr + "、本校の" + katei);
                template.add(gakka + " を " + word_卒業 + "したことを証明します");
                changeFontSize = false;
            } else if (_param._z010.in(Z010.Nagisa)) {
                head = "　 ";
                template.add("　上記の者 " + graduDateStr + " 本校");
                template.add(gakka + "を" + word_卒業 + "したことを証明する");
                changeFontSize = false;

                form.VrsOut(TITLE_NAME, "氏名");
                getMappedList(form._attributeMap, TITLE_NAME).addAll(Arrays.asList(Attribute.charSize(15), Attribute.MIGITSUME, "Edit=", Attribute.plusX(TITLE_NAME, -75, form), Attribute.plusY(TITLE_NAME, -15, form))); // 左詰め編集式カット
                getMappedList(form._attributeMap, TITLE_BIRTHDAY).add(Attribute.setX(INVALID_X));

            } else if (_param._z010.in(Z010.NichidaiNikou)) {
                head = "　　";
                template.add("　上記の者は " + graduDateStr + " 本校の");
                template.add("課程を修了し卒業したことを証明する");
            } else if (_param._z010.in(Z010.Meiji)) {
                if (isJ) {
                    head = "　　　";
                    template.add("　上記の者は" + graduDateStr + " 本校を");
                    template.add(word_卒業 + "したことを証明します");
                } else {
                    final String courseMajor = Utils.prepend(" ", Utils.append(katei, " ") + Utils.append(gakka, " "));
                    template.add("　上記の者は" + graduDateStr + " 本校" + courseMajor + "を");
                    template.add(word_卒業 + "したことを証明します");
                }

                getMappedList(form._attributeMap, TITLE_NAME).add(Attribute.charSize(11)); // 元は9.6
                getMappedList(form._attributeMap, TITLE_BIRTHDAY).add(Attribute.charSize(11)); // 元は9.6
                getMappedList(form._attributeMap, NAME1).add(Attribute.charSize(17)); // 元は15.7
                getMappedList(form._attributeMap, NAME2).add(Attribute.charSize(12)); // 元は10.4
                getMappedList(form._attributeMap, NAME11).add(Attribute.charSize(17)); // 元は15.7
                getMappedList(form._attributeMap, NAME12).add(Attribute.charSize(12)); // 元は10.4
                getMappedList(form._attributeMap, BIRTHDAY).add(Attribute.charSize(12)); // 元は10.4

            } else if (_param._z010.in(Z010.ChiyodaKudan)) {
                template.add("　上記の者は、" + graduDateStr + " 本校所定の課程を");
                template.add(word_卒業 + "したことを証明します");

                if (printData._ck.in(CK._1_SOTSU)) {
                    form.VrsOut(TITLE_NAME, "氏名");
                    getMappedList(form._attributeMap, TITLE_NAME).addAll(Arrays.asList(Attribute.charSize(15), Attribute.MIGITSUME, "Edit=", Attribute.plusX(TITLE_NAME, -75, form), Attribute.plusY(TITLE_NAME, -15, form))); // 左詰め編集式カット
                    getMappedList(form._attributeMap, TITLE_BIRTHDAY).add(Attribute.setX(10000));
                    getMappedList(form._attributeMap, BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(15), Attribute.HIDARITSUME, Attribute.plusX(BIRTHDAY, -70, form)));
                }

            } else if (_param._z010.in(Z010.Sakae)) {
                if (isJ) {
                    head = "　　　";
                    template.add("　上記の者は" + graduDateStr + " 本校を");
                    template.add(word_卒業 + "したことを証明します");
                } else {
                    template.add("　上記の者は" + graduDateStr + " 本校" + Utils.prepend(" ", Utils.append(katei, " ") + Utils.append(gakka, " ")) + "を");
                    template.add(word_卒業 + "したことを証明します");
                }

                getMappedList(form._attributeMap, NAME1).addAll(Arrays.asList(Attribute.charSize(26.1), Attribute.plusY(NAME1, -40, form))); // タイトルと同じ
                getMappedList(form._attributeMap, BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(14.8), Attribute.plusY(BIRTHDAY, -15, form))); // 日付と同じ
            } else if (_param._z010.isKaichiSpec()) {
                // 卒業日付の割付幅を広く
                form.VrsOut(GRD_DATE, graduDateStr);
                form.VrAttribute(GRD_DATE, Attribute.KINTOUWARI);
                final int xGrdDate;
                if (isJ) {
                    head = "　";
                    template.add("　上記の者は " + StringUtils.repeat(" ", 19) + " 本校を");
                    template.add(word_卒業 + "したことを証明します");
                    xGrdDate = 1160;
                } else {
                    head = "　";
                    template.add("　上記の者は、" + StringUtils.repeat(" ", 19) + " 本校");
                    template.add(gakka + " を" + word_卒業 + "したことを証明します");
                    form.VrAttribute("X", Attribute.KINTOUWARI);
                    xGrdDate = 1210;
                }

                getMappedList(form._attributeMap, GRD_DATE).addAll(Arrays.asList(Attribute.KINTOUWARI, "X=" + String.valueOf(xGrdDate)));
                getMappedList(form._attributeMap, TITLE_NAME).addAll(Arrays.asList(Attribute.charSize(16.0), Attribute.plusX(TITLE_NAME, -140, form), Attribute.plusY(TITLE_NAME, 0, form)));
                getMappedList(form._attributeMap, TITLE_NAME).addAll(Arrays.asList(Attribute.charSize(16.0), Attribute.plusX(TITLE_NAME, -140, form), Attribute.plusY(TITLE_NAME, 0, form)));
                getMappedList(form._attributeMap, TITLE_BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(16.0),Attribute.plusX(TITLE_BIRTHDAY, -140, form), Attribute.plusY(TITLE_BIRTHDAY, 0, form)));
                getMappedList(form._attributeMap, NAME1).addAll(Arrays.asList(Attribute.charSize(22.0), Attribute.plusY(TITLE_NAME, -20, form)));
                getMappedList(form._attributeMap, BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(16.0)));
            } else if (_param._z010.in(Z010.Kyoto)) {
                final String spacedGakka = Utils.prepend(" ", Utils.append(gakka, " "));
                if (isJ) {
                    head = "　　　";
                }
                template.add("　上記の者は " + graduDateStr + " 本校" + spacedGakka + "を");
                template.add(word_卒業 + "したことを証明する");
                getMappedList(form._attributeMap, TITLE_NAME).addAll(Arrays.asList(Attribute.charSize(15.0), Attribute.plusX(TITLE_NAME, 0, form), Attribute.plusY(TITLE_NAME, -16, form)));
                getMappedList(form._attributeMap, TITLE_BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(15.0),Attribute.plusX(TITLE_BIRTHDAY, 0, form), Attribute.plusY(TITLE_BIRTHDAY, -18, form)));
                for (final String field : Utils.concat(nameFields, "NAME5_2")) {
                    getMappedList(form._attributeMap, field).add(Attribute.plusX(field, 160, form));
                }
                for (final String field : Utils.concat(name1Fields, "NAME15_2")) {
                    getMappedList(form._attributeMap, field).add(Attribute.plusX(field, 160, form));
                }
                getMappedList(form._attributeMap, BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(15.0), Attribute.plusX(BIRTHDAY, 160, form), Attribute.plusY(BIRTHDAY, -15, form), Attribute.MUHENSHU));
            } else if (_param._z010.in(Z010.Matsudo)) {
                if (isJ) {
                    head = "　　　";
                }
                template.add("　上記の者は " + graduDateStr + " 本校" + Utils.prepend(" ", Utils.append(gakka, " ")) + "を");
                template.add(word_卒業 + "したことを証明する");
            } else if (_param._z010.in(Z010.Nishiyama)) {
                if (isJ) {
                    head = "　　　";
                }
                template.add("　上記の者は" + graduDateStr + " 本校" + Utils.prepend(" ", Utils.append(gakka, " ")) + "を");
                template.add(word_卒業 + "したことを証明する");
            } else if (_param._z010.in(Z010.Hirokoudai)) {
                changeFontSize = false;
                template.add("　上記の者は" + graduDateStr + " 本校" + Utils.prepend(" ", Utils.append(gakka, " ")) + "を");
                template.add(word_卒業 + "したことを証明します");
            } else if (_param._z010.in(Z010.Reitaku)) {
                if (isJ) {
                    template.add("　上記の者は" + graduDateStr + " 本校 を" + word_卒業 + "したことを");
                    template.add("証明します");
                } else {
                    template.add("　上記の者は" + graduDateStr + " 本校" + Utils.prepend(" ", katei) + Utils.prepend(" ", Utils.append(gakka, " ")) + "を");
                    template.add(word_卒業 + "したことを証明します");
                }
                form.addAttributes(commonAttributes(Z010.Reitaku, form, TITLE_NAME, NAME1, TITLE_BIRTHDAY, BIRTHDAY, nameFields, name1Fields));
            } else if (_param._z010.in(Z010.jyoto)) {
                changeFontSize = false;
                form.VrsOut("GRD_NO", "卒業証書番号　" + StringUtils.defaultString(printData._personalInfo.getString("BASE_GRD_NO")));

                template.add("　上記の者は" + graduDateStr + " 本校");
                template.add(Utils.append(gakka, " ") + "を" + word_卒業 + "したことを証明する。");
            } else {
                // 賢者
                if (isJ) {
                    head = "　　　";
                }
                template.add("　上記の者は" + graduDateStr + " 本校" + Utils.prepend(" ", Utils.append(gakka, " ")) + "を");
                template.add(word_卒業 + "したことを証明します");
            }

            // タイトル
            {
                final String title;
                if (_param._z010 == Z010.ChiyodaKudan && isJ) {
                    title = "修了証明書";
                } else {
                    title = word_卒業 + "証明書";
                }
                String titleField = "TITLE";
                if (_param._z010.in(Z010.Meiji, Z010.Seijyo)) {
                    titleField = "TITLE2";
                    if (_param._z010 == Z010.Seijyo) {
                        getMappedList(form._attributeMap, titleField).addAll(Arrays.asList(Attribute.setKeta(16), Attribute.plusX(titleField, 350, form)));
                    }
                }
                form.VrsOut(titleField, title);
            }

            //学籍番号
            form.VrsOut("SCHREGNO", personalInfo._printSchregno);

            final String main1p = template.get(0);
            final String main2p = template.get(1);
            final String main3p = template.size() < 3 ? "" : template.get(2);

            final String main1 = head + main1p;
            final String main2 = head + main2p;
            int main2ModifyY = 0;
            form.VrsOut(MAIN1, main1);  //卒業年月
            form.VrsOut(MAIN2, main2);
            if (!StringUtils.isBlank(main3p)) {
                form.VrsOut("POINT", "");
                form.VrsOut(MAIN3, head + main3p + kuten);
                log.info(" main3p = " + main3p);
            } else {
                form.VrsOut("POINT", kuten);
            }

            final SvfField fieldMAIN1 = form.getField(MAIN1);
            final SvfField fieldMAIN2 = form.getField(MAIN2);
            if (changeFontSize && null != fieldMAIN1 && null != fieldMAIN2) {
                final int main1y = fieldMAIN1.y(); // 2222
                final int main1x = fieldMAIN1.x() /* 316 */, main1endX = Math.min(form.getEndX(MAIN1, 2828), form.getEndX(MAIN2, 2828));

                final KNJSvfFieldModify MAIN1modify = new KNJSvfFieldModify(MAIN1, main1x, main1endX, defaultMain1pCharPoint, main1y, 5, 70);
                double newCharPoint = MAIN1modify.getCharPoint(main1);
                final double rate = newCharPoint / defaultMain1pCharPoint;
                if (-1.0f != newCharPoint) {
                    if (_param._isOutputDebug) {
                        log.info(" modified charSize = " + newCharPoint);
                    }
                    getMappedList(form._attributeMap, MAIN1modify._fieldname).addAll(Arrays.asList(Attribute.charSize(newCharPoint), Attribute.setY((int) MAIN1modify.getYPixel(0, KNJSvfFieldModify.charPointToPixel(newCharPoint)))));
                }

                final int main2y = fieldMAIN2.y(); /* 2492 */
                if (rate < 0.99) {
                    if (_param._isOutputDebug) {
                        log.info(" rate = " + rate);
                    }
                    final KNJSvfFieldModify MAIN2modify = new KNJSvfFieldModify(MAIN2, main1x, main1endX, defaultMain1pCharPoint, main2y, 5, 70);
                    final double nCharPoint = MAIN2modify._charPoint * rate;
                    final double nCharPixel = KNJSvfFieldModify.charPointToPixel(nCharPoint);
                    final int yp = (int) MAIN2modify.getYPixel(0, nCharPixel) - MAIN2modify._ystart;
                    if (_param._isOutputDebug) {
                        log.info(" " + MAIN2 + " yp = " + yp + " (nCharPixel = " + nCharPixel + ")");
                    }
                    main2ModifyY += yp;
                    getMappedList(form._attributeMap, MAIN2modify._fieldname).add(Attribute.charSize(nCharPoint));
                }
                if (_param._isOutputDebug) {
                    log.info(" main1y " + main1y + ", main1x " + main1x + ", main1Endx " + main1endX + ", main2y " + main2y);
                }
            }
            if (main2ModifyY != 0) {
                getMappedList(form._attributeMap, MAIN2).add(Attribute.plusY(MAIN2, main2ModifyY, form));
            }
            if (_param._z010 != Z010.Sanonihon) {
                setAttribute(form);
            }
            if (_param._z010.in(Z010.Ritsumeikan)) {
                form.VrsOut("KANA", personalInfo.getNameKana());
            }
            form.VrsOutSelectField(nameFields, personalInfo.getPrintName(printData, _param)); //氏名
            if (personalInfo.isPrintNameBoth()) {
                form.VrsOutSelectField(name1Fields, "（" + personalInfo._name + "）"); //氏名
            }

            form.VrsOut(BIRTHDAY, Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  // 証明日付

            if (_param._z010.in(Z010.Chiben)) {
                form.VrsOut("GRADUATION", graduDateStr);  //卒業年月
            }

            form.VrsOut("REMARK", defstr(printData._schoolInfo._remark5));  //備考 (京都府)

        }
        printPersonalInfoCommon(form, printData);
        printSchoolCommon(form, printData);
        printSchoolJp(db2, form, printData);
        _hasData = true;
    }

    // 卒業見込証明書
    private void printSotsugyoMikomi(final DB2UDB db2, final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final PersonalInfo personalInfo = printData._personalInfo;
        final String katei = Utils.addKatei(personalInfo._coursename);
        final String gakka = defstr(personalInfo._majorname);

        final String sotsugyo = "1".equals(printData._useShuryoShoumeisho) ? "修了" : "卒業";
        final String NAME0 = "NAME0";
        final String NAME1 = "NAME1";
        final String NAME2 = "NAME2";
        final String NAME3 = "NAME3";
        final String NAME4 = "NAME4";
        final String NAME5 = "NAME5";
        final String NAME11 = "NAME11";
        final String NAME12 = "NAME12";
        final String NAME13 = "NAME13";
        final String NAME14 = "NAME14";
        final String NAME15 = "NAME15";
        final String BIRTHDAY = "BIRTHDAY";
        String TITLE_NAME = "TITLE_NAME";
        final String TITLE_BIRTHDAY = "TITLE_BIRTHDAY";
        final String MAIN1 = "MAIN1";
        final String MAIN2 = "MAIN2";
        final String MAIN3 = "MAIN3";
        if (false) {
        } else if (_param._z010.in(Z010.Chukyo, Z010.Kyoai)) {
            // 中京は鳥取用を使用する

            String major1 = gakka;
            String major2 = "";
            final String[] split = splitBySpace(gakka);

            final String FORM_3_TORI    = "KNJG010_3TORI.frm";

            boolean useTori1 = false;
            boolean useTori1_2 = false;
            int major2length = 0;

            if (split.length == 2) {  // 学科名にスペースがあれば
                major2 = split[1];
                major1 = split[0];
                major2length = major2.length();
            }
            if (major1.length() <= 7 && major2length <= 2) {
                useTori1 = true;
            } else if (major2length <= 3) {
                useTori1_2 = true;
            }

            final String graduation1 = Utils.getSeirekiFlgDateString(db2, printData, personalInfo._graduDate, YM, null, false);

            if (useTori1) {
                printData._formname = _param._z010.in(Z010.Kyoai) ? "KNJG010_3KYOAI.frm" : FORM_3_TORI;
                form.setForm(printData._formname, printData); // 2行フォーム
                final String major = major1 + Utils.prepend(" ", major2);
                form.VrsOut(MAIN1, "　上記の者は " + graduation1 + " " + katei + " " + major);
                form.VrsOut(MAIN2, " を" + sotsugyo + "する見込みであることを証明します");
            } else if (useTori1_2) {
                printData._formname = _param._z010.in(Z010.Kyoai) ? "KNJG010_3KYOAI.frm" : FORM_3_TORI;
                form.setForm(printData._formname, printData); // 2行フォーム
                form.VrsOut(MAIN1, "　上記の者は " + graduation1 + " " + katei + " " + major1);
                form.VrsOut(MAIN2, major2 + " を" + sotsugyo + "する見込みであることを証明します");
            } else {
                printData._formname = _param._z010.in(Z010.Kyoai) ? "KNJG010_3KYOAI2.frm" : "KNJG010_3TORI2.frm";
                form.setForm(printData._formname, printData); // 3行フォーム
                form.VrsOut(MAIN1, "　上記の者は " + graduation1 + " " + katei + " " + major1);
                form.VrsOut(MAIN2, major2 + " を" + sotsugyo + "する見込みであること");
                form.VrsOut("MAIN3", "を証明します");
            }

            form.VrsOutSelectField(Arrays.asList(NAME1, NAME2), personalInfo.getPrintName(printData, _param)); //氏名
            if (personalInfo.isPrintNameBoth()) {
                form.VrsOutSelectField(Arrays.asList(NAME11, NAME12), "（" + personalInfo._name + "）"); //氏名
            }

            form.VrsOut(BIRTHDAY, Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  // 証明日付
            form.VrsOut("ENT_DATE", Utils.getSeirekiFlgDateString(db2, printData, personalInfo._entDate, YMD, null, false));
            printData._setCertifSchoolstampImagePath = printData.getCertifSchoolStampSchoolKindImagePath(_param, "H");

        } else if (_param._z010.in(Z010.Hirokoudai)) {
            printData._formname = "KNJG010_3HIROKOUDAI.frm";  // 在学証明書
            form.setForm(printData._formname, printData);
            form.VrImageOut("SCHOOLLOGO", StringUtils.defaultString(printData._schoollogoHJpgImagePath, printData._schoollogoJpgImagePath));
            final String format = "1".equals(printData._entGrdDateFormat) ? YM : YMD;
            final boolean graduDateStrZenkaku = true;
            final String graduDateStr = Utils.getSeirekiFlgDateString(db2, printData, personalInfo._graduDate, format, null, graduDateStrZenkaku);
            form.VrsOut("GRADU_DATE", graduDateStr);
            form.VrsOut("COURSE", defstr(personalInfo._coursename));
            form.VrsOut("SUBJECT", gakka);
            form.VrsOutSelectField(Arrays.asList(NAME1, NAME2), personalInfo.getPrintName(printData, _param)); //氏名
            form.VrsOut(BIRTHDAY, Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  //生年月日
            printSchoolCommon(form, printData);
            printSchoolJp(db2, form, printData);
            _hasData = true;
            return;
        } else {

            String filename = null;
            if (_param._z010 == Z010.Meikei) {
                filename = "KNJG010_3MEIKEI.frm";
            } else if (_param._z010 == Z010.Sapporo) {
                filename = "KNJG010_1SAP.frm";
            } else if (_param._z010 == Z010.Musashinohigashi) {
                filename = "KNJG010_3MUSAHIGA.frm";
            } else if (_param._z010 == Z010.Kyoto) {
                filename = Form.KNJG010_1KYOTO;
            } else if (_param._z010 == Z010.Osakatoin) {
                filename = "KNJG010_3TOIN.frm";
            } else if (_param._z010 == Z010.Sakae) {
                filename = "KNJG010_3SAKAE.frm";
            } else if (_param._z010 == Z010.Sanonihon) {
                filename = "KNJG010_3SANONICHI.frm";
            } else if (_param._z010 == Z010.Ritsumeikan) {
                filename = "KNJG010_3KEISHO.frm";
            } else if (_param._z010 == Z010.jyoto) {
                filename = "KNJG010_3_JYOTO.frm";
            } else {
                filename = "KNJG010_3.frm";
            }
            printData._formname = filename;
            form.setForm(printData._formname, printData);

            //学籍番号
            form.VrsOut("SCHREGNO", personalInfo._printSchregno);

            final int defaultMain1pCharPoint = 20;
            final String format = "1".equals(printData._entGrdDateFormat) || _param._z010.in(Z010.Sakae, Z010.jyoto) ? YM : YMD;
            String kuten = "。";
            final boolean kutenNasi = _param._z010.in(Z010.Sapporo, Z010.Miyagiken, Z010.Mieken, Z010.Seijyo, Z010.Meikei, Z010.jyoto);
            if (kutenNasi) {
                kuten = "";
            }

            final String[][] replaces = {
                    {KW_SEITO_KATEIMEI, katei}
                  , {KW_SEITO_GAKKAMEI, gakka}
            };

            final List<String> templates = new ArrayList<String>();
            String head = "";
            String title = sotsugyo + "見込証明書";
            boolean changeFontSize = true;
            String titleField = "TITLE";
            if (_param._z010 == Z010.Meiji) {
                getMappedList(form._attributeMap, titleField).add(svfChangeCharSizeAttribute(20, 840, 899, 26.1, 29.0));
            }
            form.VrsOut(titleField, title);
            final boolean graduDateStrZenkaku = _param._z010 == Z010.Seijyo;
            String graduDateStr = Utils.getSeirekiFlgDateString(db2, printData, personalInfo._graduDate, format, null, graduDateStrZenkaku);
            String courseMajor;
            final List<String> nameFields = Arrays.asList(NAME0, NAME1, NAME2, NAME3, NAME4, NAME5);
            final List<String> name1Fields = Arrays.asList(NAME11, NAME12, NAME13, NAME14, NAME15);
            if (_param._z010 == Z010.Sapporo) {
                courseMajor = gakka;
                templates.add("上記の者は " + graduDateStr + " " + PrintData.trim(defstr(printData._schoolInfo._schoolname1)) + " " + courseMajor);
                templates.add("を" + sotsugyo + "見込であることを証明します");
            } else if (_param._z010 == Z010.Musashinohigashi) {
                form.VrsOut("NAME_TITLE", "生徒名");

                changeFontSize = false;
                kuten = "";
                templates.add("上記の者は " + graduDateStr);
                if (H.equals(personalInfo._schoolKind)) {
                    courseMajor = defstr(personalInfo._coursename) + gakka;
                } else {
                    courseMajor = gakka;
                }
                templates.add("本校" + courseMajor);
                templates.add("を修了見込であることを証明する。");
            } else if (_param._z010 == Z010.Miyagiken) {
                courseMajor = gakka;
                templates.add("　上の者は " + graduDateStr + " 本校 " + courseMajor + " の課程を");
                templates.add(sotsugyo + "見込みであることを証明する");
            } else if (_param._z010 == Z010.Mieken) {
                head = "　　";
                form.VrAttribute(TITLE_NAME, "Edit="); // 編集式カット
                form.VrsOut(TITLE_NAME, "名　　前");
                courseMajor = gakka;
                templates.add("上記の者は " + graduDateStr + " 本校 " + courseMajor + "");
                templates.add("の課程を" + sotsugyo + "見込であることを証明する");
            } else if (_param._z010 == Z010.Seijyo) {
                templates.add("　上記のものは、 " + graduDateStr + " 本校を");
                templates.add(sotsugyo + "する見込であることを証明します");
            } else if (_param._z010 == Z010.Meikei) {
                changeFontSize = false;

                head = "　 ";
                courseMajor = defstr(personalInfo._coursename) + gakka;
                templates.add("上記の者は" + graduDateStr + "、本学園高等学校");
                templates.add(courseMajor + "を" + sotsugyo + "見込であることを証明します。");
            } else if (_param._z010 == Z010.Sakae) {

                head = "";
                courseMajor = Utils.prepend(" ", Utils.append(katei, " ") + Utils.append(gakka, " "));
                templates.add("　上記の者は本校" + courseMajor + "を" + graduDateStr);
                templates.add(sotsugyo + "見込であることを証明します");

                getMappedList(form._attributeMap, NAME1).addAll(Arrays.asList(Attribute.charSize(26.1), Attribute.plusY(NAME1, -40, form))); // タイトルと同じ
                getMappedList(form._attributeMap, BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(14.8), Attribute.plusY(BIRTHDAY, -15, form))); // 日付と同じ
                getMappedList(form._attributeMap, MAIN1).add(Attribute.plusY(MAIN1, -50, form));
                getMappedList(form._attributeMap, MAIN3).add(Attribute.plusY(MAIN3, +50, form));
            } else if (_param._z010 == Z010.Osakatoin) {
                courseMajor = Utils.prepend(" ", Utils.append(gakka, " "));
                changeFontSize = false;
                templates.add("　上記の者は" + graduDateStr + " 本校" + courseMajor + "3ヶ年の課程を");
                templates.add("修了見込であることを証明します");
            } else if (_param._z010 == Z010.Ritsumeikan) {
                head = " ";
                templates.add("上記の者は、" + graduDateStr + "、本校の" + katei);
                templates.add(gakka + " を" + sotsugyo + "する見込みであることを証明します");
                changeFontSize = false;
            } else if (_param._z010.in(Z010.jyoto)) {
                templates.add("　上記の者は、" + graduDateStr + "に本校");
                templates.add(gakka + "の課程を卒業見込みであること");
                templates.add("を証明する。");
            } else {

                if (_param._z010.in(Z010.ChiyodaKudan)) {
                    templates.add("　上記の者は、" + graduDateStr + " 本校所定の課程を");
                } else if (_param._z010.in(Z010.Kyoto, Z010.Matsudo)) {
                    courseMajor = Utils.prepend(" ", Utils.append(gakka, " "));
                    templates.add("　上記の者は " + graduDateStr + " 本校" + courseMajor + "を");
                } else if (_param._z010.in(Z010.Meiji)) {
                    courseMajor = Utils.prepend(" ", Utils.append(katei, " ") + Utils.append(gakka, " "));
                    templates.add("　上記の者は" + graduDateStr + " 本校" + courseMajor + "を");
                } else if (_param._z010.in(Z010.NichidaiNikou)) {
                    templates.add("　上記の者は " + graduDateStr + " 本校の課程を修了し");
                } else if (_param._z010.in(Z010.Reitaku)) {
                    courseMajor = Utils.prepend(" ", Utils.append(katei, " ") + Utils.append(gakka, " "));
                    templates.add("　上記の者は" + graduDateStr + " 本校" + courseMajor + "を");
                } else {
                    courseMajor = Utils.prepend(" ", Utils.append(gakka, " "));
                    templates.add("　上記の者は" + graduDateStr + " 本校" + courseMajor + "を");
                }
                if (_param._z010.in(Z010.Nishiyama, Z010.Kyoto, Z010.Matsudo)) {
                    templates.add(sotsugyo + "する見込みであることを証明する");
                } else {
                    templates.add(sotsugyo + "する見込みであることを証明します");
                }
            }
            final String[] fields = {MAIN1, MAIN2};
            for (int i = 0; i < Math.min(templates.size(), Math.min(fields.length, 2)); i++) {
                String line = templates.get(i);
                for (final String[] replace : replaces) {
                    line = StringUtils.replace(line, replace[0], replace[1]);
                }
                form.VrsOut(fields[i], head + line);
            }
            int main2ModifyY = 0;
            if (templates.size() > 2) {
                form.VrsOut(MAIN3, head + templates.get(2) + kuten);
                log.info(" main3p = " + templates.get(2));
                if (!_param._z010.in(Z010.Ritsumeikan, Z010.jyoto)) {
                    main2ModifyY = -90;
                }
            } else {
                form.VrsOut("POINT", kuten);
            }

            final SvfField fieldMAIN1 = form.getField(MAIN1);
            final SvfField fieldMAIN2 = form.getField(MAIN2);
            if (changeFontSize && null != fieldMAIN1 && null != fieldMAIN2) {
                final int main1y = fieldMAIN1.y();
                final int main1x = fieldMAIN1.x(), main1endX = Math.min(form.getEndX(MAIN1, 2828), form.getEndX(MAIN2, 2828));

                final KNJSvfFieldModify MAIN1modify = new KNJSvfFieldModify(MAIN1, main1x, main1endX, defaultMain1pCharPoint, main1y, 5, 70);
                double newCharPoint = MAIN1modify.getCharPoint(templates.get(0));
                final double rate = newCharPoint / defaultMain1pCharPoint;
                if (-1.0f != newCharPoint) {
                    if (_param._isOutputDebug) {
                        log.info(" modified charSize = " + newCharPoint);
                    }
                    getMappedList(form._attributeMap, MAIN1modify._fieldname).addAll(Arrays.asList(Attribute.charSize(newCharPoint), Attribute.setY((int) MAIN1modify.getYPixel(0, KNJSvfFieldModify.charPointToPixel(newCharPoint)))));
                }

                final int main2y = fieldMAIN2.y(); /* 2492 */
                if (rate < 0.99) {
                    if (_param._isOutputDebug) {
                        log.info(" rate = " + rate);
                    }
                    final KNJSvfFieldModify MAIN2modify = new KNJSvfFieldModify(MAIN2, main1x, main1endX, defaultMain1pCharPoint, main2y, 5, 70);
                    final double nCharPoint = MAIN2modify._charPoint * rate;
                    final double nCharPixel = KNJSvfFieldModify.charPointToPixel(nCharPoint);
                    final int yp = (int) MAIN2modify.getYPixel(0, nCharPixel) - MAIN2modify._ystart;
                    if (_param._isOutputDebug) {
                        log.info(" " + MAIN2 + " yp = " + yp + " (nCharPixel = " + nCharPixel + ")");
                    }
                    main2ModifyY += yp;
                    getMappedList(form._attributeMap, MAIN2modify._fieldname).add(Attribute.charSize(nCharPoint));
                }
                if (_param._isOutputDebug) {
                    log.info(" main1y " + main1y + ", main1x " + main1x + ", main1Endx " + main1endX + ", main2y " + main2y);
                }
            }
            if (main2ModifyY != 0) {
                getMappedList(form._attributeMap, MAIN2).add(Attribute.plusY(MAIN2, main2ModifyY, form));
            }
            if (_param._z010 == Z010.Musashinohigashi && H.equals(printData._certifKindSchoolKind)) {
                final int xplus = 800;
                getMappedList(form._attributeMap, "NAME_TITLE").add(Attribute.plusX("NAME_TITLE", xplus, form));
                getMappedList(form._attributeMap, NAME1).add(Attribute.plusX(NAME1, xplus, form));
                getMappedList(form._attributeMap, NAME2).add(Attribute.plusX(NAME2, xplus, form));
            } else if (_param._z010 == Z010.Kyoto) {
                getMappedList(form._attributeMap, TITLE_NAME).addAll(Arrays.asList(Attribute.charSize(15.0), Attribute.plusX(TITLE_NAME, 0, form), Attribute.plusY(TITLE_NAME, -16, form)));
                getMappedList(form._attributeMap, TITLE_BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(15.0), Attribute.plusX(TITLE_BIRTHDAY, 0, form), Attribute.plusY(TITLE_BIRTHDAY, -18, form)));
                for (final String field : Utils.concat(nameFields, "NAME5_2")) {
                    getMappedList(form._attributeMap, field).add(Attribute.plusX(field, 160, form));
                }
                for (final String field : Utils.concat(name1Fields, "NAME15_2")) {
                    getMappedList(form._attributeMap, field).add(Attribute.plusX(field, 160, form));
                }
                getMappedList(form._attributeMap, BIRTHDAY).addAll(Arrays.asList(Attribute.charSize(15.0), Attribute.plusX(BIRTHDAY, 160, form), Attribute.plusY(BIRTHDAY, -15, form), Attribute.MUHENSHU));
            } else if (_param._z010 == Z010.Rakunan) {
                getMappedList(form._attributeMap, TITLE_NAME).addAll(Arrays.asList("Size=11.0", Attribute.plusX(TITLE_NAME, -60, form), Attribute.plusY(TITLE_NAME, -30, form)));
                getMappedList(form._attributeMap, TITLE_BIRTHDAY).addAll(Arrays.asList("Size=11.0",Attribute.plusX(TITLE_BIRTHDAY, -60, form), Attribute.plusY(TITLE_BIRTHDAY, 40, form)));
                getMappedList(form._attributeMap, NAME1).addAll(Arrays.asList("Size=20.0", Attribute.plusY(NAME1, -50, form)));
                getMappedList(form._attributeMap, NAME11).addAll(Arrays.asList("Size=20.0", Attribute.plusY(NAME11, -9, form)));
                getMappedList(form._attributeMap, BIRTHDAY).addAll(Arrays.asList("Size=14.8", Attribute.plusY(BIRTHDAY, 30, form), Attribute.KINTOUWARI));
            } else if (_param._z010.in(Z010.Reitaku)) {
                form.addAttributes(commonAttributes(Z010.Reitaku, form, TITLE_NAME, NAME1, TITLE_BIRTHDAY, BIRTHDAY, nameFields, name1Fields));
            }
            if (_param._z010 != Z010.Sanonihon) {
                setAttribute(form);
            }
            if (_param._z010 == Z010.Meiji) {
                form.VrAttribute(TITLE_NAME, "Size=11"); // 元は9.6
                form.VrAttribute(TITLE_BIRTHDAY, "Size=11"); // 元は9.6
                form.VrAttribute(NAME1, "Size=17"); // 元は15.7
                form.VrAttribute(NAME2, "Size=12"); // 元は10.4
                form.VrAttribute(NAME11, "Size=17"); // 元は15.7
                form.VrAttribute(NAME12, "Size=12"); // 元は10.4
                form.VrAttribute(BIRTHDAY, "Size=12"); // 元は10.4
            }
            if (_param._z010.in(Z010.Ritsumeikan)) {
                form.VrsOut("KANA", personalInfo.getNameKana());
            }
            form.VrsOutSelectField(nameFields, personalInfo.getPrintName(printData, _param)); //氏名
            if (personalInfo.isPrintNameBoth()) {
                form.VrsOutSelectField(name1Fields, "（" + personalInfo._name + "）"); //氏名
            }

            form.VrsOut(BIRTHDAY, Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  // 証明日付

            if (_param._z010 == Z010.Chiben) {
                form.VrsOut("GRADUATION", graduDateStr);  //卒業年月
            }
        }
        printPersonalInfoCommon(form, printData);
        printSchoolCommon(form, printData);
        printSchoolJp(db2, form, printData);
        _hasData = true;
    }

    private void setAttribute(final Form form) {
        for (final Map.Entry<String, List<String>> e  : form._attributeMap.entrySet()) {
            final String field = e.getKey();
            final List<String> attributeList = e.getValue();
            for (final String attribute : attributeList) {
                if (StringUtils.isEmpty(attribute)) {
                    continue;
                }
                final int setAttributeResult = form.VrAttribute(field, attribute);
                if (_param._isOutputDebug) {
                    log.info(" setting attribute :: \"" + field + "\" : \"" + attribute + "\" = " + setAttributeResult);
                }
            }
        }
    }

    // 日本スポーツ振興センター加入証明書
    private void printSportsShinkouCenterKanyusho(final DB2UDB db2, final Form form, final PrintData printData) {
        log.info(" certifKind = " + printData._certifKind);
        final PersonalInfo personalInfo = printData._personalInfo;

        printData._formname = "KNJG072_MEIKEI.frm";
        form.setForm(printData._formname, printData);

        String kuten = "。";
        final boolean kutenNasi = false;
        if (kutenNasi) {
            kuten = "";
        }

        form.VrsOut("TITLE", "日本スポーツ振興センター加入証明書");

        final String main1 = "上記の者は、" + KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(printData._year))  + "年度日本スポーツ振興センター";
        final String main2 = "に加入していることを証明します";
        form.VrsOut("MAIN1", main1);
        form.VrsOut("MAIN2", main2);
        form.VrsOut("POINT", kuten);

        form.VrsOutSelectField(Arrays.asList("NAME1", "NAME2"), personalInfo.getPrintName(printData, _param)); //氏名
        if (personalInfo.isPrintNameBoth()) {
            form.VrsOutSelectField(Arrays.asList("NAME11", "NAME12"), "（" + personalInfo._name + "）"); //氏名
        }

        form.VrsOut("BIRTHDAY", Utils.getBirthdayFormattedString(db2, _param, printData, personalInfo._birthday, personalInfo._birthdayFlg, false, false));  // 証明日付

        printPersonalInfoCommon(form, printData);
        printSchoolCommon(form, printData);
        printSchoolJp(db2, form, printData);
        _hasData = true;
    }

    /**
     * 文字ポイントを変更しフィールド位置を修正する
     * @param keta 桁
     * @param x フィールドの位置X
     * @param y フィールドの位置Y
     * @param charSize フィールドの文字ポイント
     * @param charSizeTarget 新しく設定する文字ポイント
     */
    private static String svfChangeCharSizeAttribute(final int keta, final int x, final int y, final double charSize, final double charSizeTarget) {
        final double charPixel = KNJSvfFieldModify.charPointToPixel(charSize);
        final double charPixel2 = KNJSvfFieldModify.charPointToPixel(charSizeTarget);

        final double xdiff = (charPixel2 * keta / 2) / 2 - (charPixel * keta / 2) / 2;
        final double ydiff = charPixel2 / 2 - charPixel / 2;

        final List<String> attribute = Arrays.asList("X=" + String.valueOf(Math.round(x - xdiff)), "Y=" + String.valueOf(Math.round(y - ydiff)), Attribute.charSize(charSizeTarget));
        return Utils.mkString(attribute, ",");
    }

    private Map<String, List<String>> commonAttributes(final Z010 z010, final Form form, final String TITLE_NAME, final String NAME1, final String TITLE_BIRTHDAY, final String BIRTHDAY, final List<String> nameFields, final List<String> name1Fields) {
        final Map<String, List<String>> attrs = new HashMap<String, List<String>>();
        if (z010.in(Z010.Reitaku)) {
            final int moveUp = 20;
            attrs.put(TITLE_NAME, Arrays.asList(Attribute.charSize(17.0), Attribute.plusX(TITLE_NAME, -230, form), Attribute.plusY(TITLE_NAME, -15-moveUp, form)));
            attrs.put(TITLE_BIRTHDAY, Arrays.asList(Attribute.charSize(17.0), Attribute.plusX(TITLE_BIRTHDAY, -230, form)));
            attrs.put(NAME1, Arrays.asList(Attribute.charSize(17.0), Attribute.setKeta(26)));

            for (final String nameField : nameFields) {
                form.addAttribute(nameField, Attribute.plusX(nameField, 20, form), Attribute.plusY(nameField, -moveUp, form));
                final SvfField field = form.getField(nameField);
                if (null != field) {
                    final String linkFieldname = (String) field.getAttributeMap().get(SvfField.AttributeLinkField);
                    if (null != linkFieldname) {
                        form.addAttribute(linkFieldname, Attribute.plusX(nameField, 20, form));
                    }
                }
            }
            for (final String nameField : name1Fields) {
                form.addAttribute(nameField, Attribute.plusX(nameField, 20, form), Attribute.plusY(nameField, -moveUp/2, form));
                final SvfField field = form.getField(nameField);
                if (null != field) {
                    final String linkFieldname = (String) field.getAttributeMap().get(SvfField.AttributeLinkField);
                    if (null != linkFieldname) {
                        form.addAttribute(linkFieldname, Attribute.plusX(nameField, 20, form));
                    }
                }
            }

            attrs.put(BIRTHDAY, Arrays.asList(Attribute.plusX(BIRTHDAY, 20, form), Attribute.charSize(17.0), Attribute.HIDARITSUME));
        }
        return attrs;
    }

    private static class KNJSvfFieldModify {

        private final String _fieldname;   //フィールド名
        private final int _x1;   //フィールドの開始X(ドット)
        private final int _x2;   //フィールドの終了X(ドット)
        private final double _charPoint; // 文字ポイント
        private final int _ystart;  //開始位置(ドット)
        private final int _minnum;  //最小設定文字数
        private final int _maxnum;  //最大設定文字数

        public KNJSvfFieldModify(final String fieldname, final int x1, final int x2, final double charPoint, final int ystart, final int minnum, final int maxnum) {
            _fieldname = fieldname;
            _x1 = x1;
            _x2 = x2;
            _charPoint = charPoint;
            _ystart = ystart;
            _minnum = minnum;
            _maxnum = maxnum;
        }

        //フィールドの高さ(ドット)
        private double height() {
            return KNJSvfFieldModify.charPointToPixel(_charPoint);
        }

        //フィールドの幅(ドット)
        private int width() {
            return _x2 - _x1;
        }

        /**
         *  ポイントの設定
         *  引数について  String str : 出力する文字列
         */
        public double getCharPoint(final String str) {
            final double charWidthPixel = getCharWidthPixel(width(), Math.min(Math.max(getMS932ByteLength(str), _minnum), _maxnum));
            //log.debug(" keta = " + keta + ", charWidthPixel = " + charWidthPixel);
            return Math.min(_charPoint, pixelToCharPoint(charWidthPixel));  //文字サイズ
        }

        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charPoint 文字サイズ
         * @return 文字サイズをピクセルに変換した値
         */
        public static double charPointToPixel(final double charPoint) {
            return charPoint * 400 / 72;
        }

        /**
         * ピクセルを文字サイズに変換した値を得る
         * @param charSize ピクセル
         * @return ピクセルを文字サイズに変換した値
         */
        public static double pixelToCharPoint(final double pixel) {
            return pixel * 72 / 400;
        }

        /**
         *  Ｙ軸の算出
         *  引数について  int hnum   : 出力位置(行)
         */
        public double getYPixel(final int hnum, final double charPixel) {
            double jiku = yOffsetPixel(height(), charPixel) + _ystart + height() * hnum;  //出力位置＋Ｙ軸の移動幅
            return jiku;
        }

        /**
         *  表示幅から一文字のサイズpixelを計算
         */
        private static double getCharWidthPixel(final long width, final int num) {
            final int mojisu = num / 2 + (num % 2 == 0 ? 0 : 1);
            final double charWidth = (double) width / (long) mojisu;
            return charWidth;
        }

        /**
         *  Ｙ軸の移動幅算出
         */
        private static double yOffsetPixel(final double height, final double charPixel) {
            return Math.round((height - charPixel) / 2);
        }

        public String toString() {
            return "KNJSvfFieldModify: width = "+ width() + " , height = " + height() + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 基準となる学校情報の出力処理クラス
     */
    private static class SchoolInfo {
        private final Map _map;
        static final SchoolInfo DEFAULT = new SchoolInfo(Collections.EMPTY_MAP);

        String _syosyoName;
        String _syosyoNam2;
        String _certifNo;
        String _principalJobname;
        String _principalName;
        String _remark1;
        String _remark2;
        String _remark3;
        String _remark4;
        String _remark5;
        String _remark6;
        String _remark7;
        String _remark8;
        String _remark9;
        String _remark10;
        String _schoolname1;
        String _schoolnameEng;
        String _principalStaffnameEng;

        public SchoolInfo(final Map rs) {
            _map = rs;
            try {
                _syosyoName = KnjDbUtils.getString(_map, "SYOSYO_NAME");
                _syosyoNam2 = KnjDbUtils.getString(_map, "SYOSYO_NAME2");
                _certifNo = KnjDbUtils.getString(_map, "CERTIF_NO");
                _principalJobname = KnjDbUtils.getString(_map, "JOB_NAME");
                _principalName = KnjDbUtils.getString(_map, "PRINCIPAL_NAME");
                _remark1 = KnjDbUtils.getString(_map, "REMARK1");
                _remark2 = KnjDbUtils.getString(_map, "REMARK2");
                _remark3 = KnjDbUtils.getString(_map, "REMARK3");
                _remark4 = KnjDbUtils.getString(_map, "REMARK4");
                _remark5 = KnjDbUtils.getString(_map, "REMARK5");
                _remark6 = KnjDbUtils.getString(_map, "REMARK6");
                _remark7 = KnjDbUtils.getString(_map, "REMARK7");
                _remark8 = KnjDbUtils.getString(_map, "REMARK8");
                _remark9 = KnjDbUtils.getString(_map, "REMARK9");
                _remark10 = KnjDbUtils.getString(_map, "REMARK10");
                _schoolname1 = KnjDbUtils.getString(_map, "SCHOOLNAME1");
                _schoolnameEng = KnjDbUtils.getString(_map, "SCHOOLNAME_ENG");

            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }

        public String toString() {
            return "SchoolInfo(" + _map + ")";
        }

        private static SchoolInfo getSchoolInfo(final DB2UDB db2, final PrintData printData, final Param param) {
            SchoolInfo schoolInfo = SchoolInfo.DEFAULT;
            final String psKey = "PS_KEY_SCHOOL" + printData._certifKindSchoolKind;
            if (null == param.getPs(psKey)) {
                // 学校データ
                final String sql = getSchoolInfoSql(param, printData);
                if (param._isOutputDebugQuery) {
                    log.info(" sqlSchool = " + sql);
                }
                param.setPs(db2, psKey, sql);
            }

            final Object[] args = {
                    printData._ctrlYear,  // 対象年度
                    printData._certifSchoolDatCertifKind,  // 証明書種別
            };
            if (param._isOutputDebugQuery) {
                log.info(" school sql arg = " + ArrayUtils.toString(args));
            }
            final List rowList = KnjDbUtils.query(db2, param.getPs(psKey), args);
            if (rowList.size() > 0) {
                schoolInfo = new SchoolInfo(KnjDbUtils.firstRow(rowList));

                final Object[] args2 = {
                        printData._certifDateYear,  // 現年度
                };
                schoolInfo._principalStaffnameEng = KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, pre_sql2(), args2)), "PRINCIPAL_STAFFNAME_ENG");
            }
            return schoolInfo;
        }

        private static String getSchoolInfoSql(final Param param, final PrintData printData) {

            final StringBuffer sql = new StringBuffer();

            final String q = "?";
            //  学校
            sql.append(" SELECT ");
            sql.append("    SCHM.YEAR");
            sql.append("   ,SCHM.SCHOOLNAME_ENG");
            sql.append("   ,CERT1.SCHOOL_NAME AS SCHOOLNAME1");
            sql.append("   ,CERT1.SYOSYO_NAME ");
            sql.append("   ,CERT1.SYOSYO_NAME2 ");
            sql.append("   ,CERT1.CERTIF_NO ");
            sql.append("   ,CERT1.JOB_NAME ");
            sql.append("   ,CERT1.PRINCIPAL_NAME ");
            sql.append("   ,CERT1.REMARK1");
            sql.append("   ,CERT1.REMARK2");
            sql.append("   ,CERT1.REMARK3");
            sql.append("   ,CERT1.REMARK4");
            sql.append("   ,CERT1.REMARK5");
            sql.append("   ,CERT1.REMARK6");
            sql.append("   ,CERT1.REMARK7");
            sql.append("   ,CERT1.REMARK8");
            sql.append("   ,CERT1.REMARK9");
            sql.append("   ,CERT1.REMARK10 ");
            sql.append(" FROM ");
            sql.append("     (SELECT * FROM SCHOOL_MST WHERE YEAR = " + q + " ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql.append(" AND SCHOOL_KIND = '" + printData._certifKindSchoolKind + "' ");
            }
            sql.append("     ) SCHM ");
            sql.append(" LEFT JOIN CERTIF_SCHOOL_DAT CERT1 ON CERT1.CERTIF_KINDCD = " + q + " AND SCHM.YEAR = CERT1.YEAR ");

            return sql.toString();
        }


        private static String pre_sql2() {

            final StringBuffer sql = new StringBuffer();

            final String q = "?";
            sql.append("     SELECT ");
            sql.append("           W1.STAFFCD");
            sql.append("         , W2.STAFFNAME_ENG AS PRINCIPAL_STAFFNAME_ENG ");
            sql.append("     FROM ");
            sql.append("         STAFF_YDAT W1 ");
            sql.append("         INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
            sql.append("     WHERE ");
            sql.append("         W1.YEAR = " + q + " AND W2.JOBCD = '0001' ");

            return sql.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * 基準となる個人情報の出力処理クラス
     */
    private static class PersonalInfo {
        final PrintData _printData;
        private Map _map;
        final String _schregno;
        final String _printSchregno;
        final String _name;
        final String _realName;
        final String _useRealName;
        final String _birthday;
        final String _birthdayFlg;
        final String _sex;
        final String _entDate;
        final String _grdDiv;
        final String _graduDate;
        final String _coursecd;
        final String _coursename;
        final String _majorcd;
        final String _majorname;
        final String _majorname2;
        final String _coursecode;
        final String _annual;
        final String _schoolKind;
        boolean _inDomitory;
        String _dormitoryName;
        String _dormitoryAddr1;
        String _dormitoryAddr2;
        String _addr1;
        String _addr2;
        final String _majorEng;
        String _baseDetailMstSeq008BaseRemark1;

        public PersonalInfo(final Map<String, String> rs, final PrintData printData) {
            _printData = printData;
            _map = rs;
            _schregno = getString(rs, "SCHREGNO");
            _graduDate = getString("GRADU_DATE");
            if ("1".equals(printData._certifSchoolOnly)) {
                _map = new HashMap();
            }
            _printSchregno = getString("SCHREGNO");
            _name = getString("NAME");
            _realName = getString("REAL_NAME");
            _useRealName = getString("USE_REAL_NAME");
            _birthday = getString("BIRTHDAY");
            _birthdayFlg = getString("BIRTHDAY_FLG");
            _sex = getString("SEX");
            _entDate = getString("ENT_DATE");
            _grdDiv = getString("GRD_DIV");
            _coursecd = getString("COURSECD");
            _coursename = getString("COURSENAME");
            _majorcd = getString("MAJORCD");
            _majorname = getString("MAJORNAME");
            _majorname2 = getString("MAJORNAME2");
            _coursecode = getString("COURSECODE");
            _annual = getString("ANNUAL");
            _schoolKind = getString("SCHOOL_KIND");
            _majorEng = getString("MAJORENG");
        }

        public String getAge(final String baseDate) {
            if (null == _birthday || null == baseDate) {
                return null;
            }
            final Calendar birthDayCal = Utils.getCalendar(_birthday);
            final Calendar baseDateCal = Utils.getCalendar(baseDate);
            final Calendar ageCal = Calendar.getInstance();
            final Date d = new Date();
            d.setTime(baseDateCal.getTime().getTime() - birthDayCal.getTime().getTime());
            ageCal.setTime(d);
            final int age = ageCal.get(Calendar.YEAR) - 1970;
            return String.valueOf(age);
        }

        public String getGrade() {
            return getString("GRADE");
        }

        public String getGradeCd() {
            if (_map.containsKey("GRADE_CD") && null != getString("GRADE_CD")) {
                return getString("GRADE_CD");
            }
            return getGrade();
        }

        public String getEnterGradeCd() {
            if (_map.containsKey("ENTER_GRADE_CD") && null != getString("ENTER_GRADE_CD")) {
                return getString("ENTER_GRADE_CD");
            }
            return getString("ENTER_GRADE");
        }

        public String getNameEng(final Param param) {
            if (param._z010 == Z010.Meikei) {
                return StringUtils.replaceOnce(getString("NAME_ENG"), " ", ", ");
            }
            return getString("NAME_ENG");
        }

        public String getString(final String field) {
            return getString(_map, field);
        }

        private String getString(final Map map, final String field) {
            try {
                if (StringUtils.isBlank(_printData._certifSchoolOnly) && (null == field || !map.containsKey(field))) {
                    // フィールド名が間違い
                    throw new RuntimeException("指定されたフィールドのデータがありません。フィールド名：'" + field + "' :" + map);
                }
            } catch (final Exception e) {
                log.error("exception!", e);
            }
            if (null == field) {
                return null;
            }
            return (String) map.get(field);
        }

        public String getPrintName(final PrintData printData, final Param param) {
            final boolean useRealName = ("1".equals(_useRealName) || "1".equals(printData._certifPrintRealName)) && null != _realName;
            final String rtn = useRealName ? _realName : _name;
            if (param._isOutputDebug) {
                log.info(" useRealName = " + _useRealName + ", " + rtn);
            }
            return rtn;
        }

        public boolean isPrintNameBoth() {
            return "1".equals(_useRealName) && "1".equals(getString("NAME_OUTPUT_FLG")) && null != _name;
        }

        public String getNameKana() {
            return defstr("1".equals(_useRealName) ? getString("REAL_NAME_KANA") : null, getString("NAME_KANA"));
        }

        public String toString() {
            return "PerosnalInfo(" + _map + ")";
        }

        private static PersonalInfo getPersonalInfo(final DB2UDB db2, final PrintData printData, final Param param) {
            PersonalInfo personalInfo = null;
            final boolean isPrintGrd = param._isPrintGrd;
            final String psKey = "PS_PERSONAL_" + printData._certifKindSchoolKind;
            if (null == param.getPs(psKey)) {
                String sql = getPersonalInfoSql(param, printData);
                if (param._isOutputDebugQuery) {
                    log.info(" personal info sql = " + sql);
                }
                param.setPs(db2, psKey, sql);
            }

            final String[] args = {
                    printData._schregno, //学籍番号
                    printData._year,     //対象年度
                    printData._semester, //対象学期
            };
            if (param._isOutputDebugQuery) {
                log.info(" personal sql args (grd = " + isPrintGrd + ") = " + ArrayUtils.toString(args));
            }
            final List<Map<String, String>> rowList = KnjDbUtils.query(db2, param.getPs(psKey), args);
            if (rowList.size() > 0) {
                final Map<String, String> row = KnjDbUtils.firstRow(rowList);
                if (param._isOutputDebug) {
                    log.info(debugMapToStr(" personal row = ", row));
                }
                personalInfo = new PersonalInfo(row, printData);
            }

            if (null != personalInfo) {

                final String psKeyAddr = "PS_KEY_ADDR";
                final Map<String, String> addrRow;
                if ("1".equals(printData._certifSchoolOnly)) {
                    // テンプレート出力
                } else {
                    if (param._isPrintGrd) {
                        if (null == param.getPs(psKeyAddr)) {
                            //住所
                            final StringBuffer sql = new StringBuffer();
                            sql.append(" SELECT ");
                            sql.append("     BASE.CUR_ADDR1 AS ADDR1 ");
                            sql.append("   , BASE.CUR_ADDR2 AS ADDR2 ");
                            sql.append(" FROM GRD_BASE_MST BASE ");
                            sql.append(" WHERE BASE.SCHREGNO = ? ");

                            param.setPs(db2, psKeyAddr, sql.toString());
                        }

                        addrRow = KnjDbUtils.lastRow(KnjDbUtils.query(db2, param.getPs(psKeyAddr), new Object[] {printData._schregno}));
                    } else {
                        if (null == param.getPs(psKeyAddr)) {
                            final StringBuffer sql = new StringBuffer();
                            sql.append(" SELECT T1.* ");
                            sql.append(" FROM SCHREG_ADDRESS_DAT T1 ");
                            sql.append(" INNER JOIN ( ");
                            sql.append("     SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE ");
                            sql.append("     FROM SCHREG_ADDRESS_DAT T1 ");
                            sql.append("     WHERE SCHREGNO = ? ");
                            sql.append("       AND FISCALYEAR(ISSUEDATE) <= ? ");
                            sql.append("     GROUP BY SCHREGNO ");
                            sql.append(" ) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ISSUEDATE = T1.ISSUEDATE ");
                            sql.append(" WHERE T1.SCHREGNO = T1.SCHREGNO ");

                            param.setPs(db2, psKeyAddr, sql.toString());
                        }

                        addrRow = KnjDbUtils.lastRow(KnjDbUtils.query(db2, param.getPs(psKeyAddr), new Object[] {printData._schregno, printData._year}));
                    }

                    if (!addrRow.isEmpty()) {
                        if (param._isOutputDebug) {
                            log.info(debugMapToStr(" address = ", addrRow));
                        }
                        personalInfo._addr1 = KnjDbUtils.getString(addrRow, "ADDR1");
                        personalInfo._addr2 = KnjDbUtils.getString(addrRow, "ADDR2");
                    }
                }

                printData.setSchoolMst(db2, personalInfo, param);
                printData.setNameZ001(db2, personalInfo, param);

                if ("1".equals(printData._certifSchoolOnly)) {
                    // テンプレート出力
                    personalInfo._inDomitory = true;
                } else {
                    if (printData._ck.isPrintZairyou()) {
                        if (param._z010.in(Z010.Sakae, Z010.Reitaku, Z010.jyoto)) {
                            boolean isTarget = true;
                            if (param._z010.in(Z010.Sakae, Z010.jyoto) && null == printData._certifDate) {
                                isTarget = false;
                            }
                            if (param._hasSCHREG_DOMITORY_HIST_DAT && isTarget) {
                                final String psKeyDom = "PS_KEY_DOMITORY";
                                if (null == param.getPs(psKeyDom)) {
                                    final StringBuffer sql = new StringBuffer();
                                    sql.append(" SELECT T1.*, T2.DOMI_NAME, T2.DOMI_ADDR1, T2.DOMI_ADDR2 ");
                                    sql.append(" FROM SCHREG_DOMITORY_HIST_DAT T1 ");
                                    sql.append(" INNER JOIN DOMITORY_MST T2 ON T2.DOMI_CD = T1.DOMI_CD ");
                                    sql.append(" WHERE T1.SCHREGNO = ? ");
                                    if (param._z010.in(Z010.Sakae, Z010.jyoto)) {
                                        sql.append("   AND '" + StringUtils.replace(printData._certifDate, "/", "-") + "' BETWEEN T1.DOMI_ENTDAY AND VALUE(T1.DOMI_OUTDAY, '9999-12-31') ");
                                    } else if (param._z010.in(Z010.Reitaku)) {
                                        sql.append("   AND '" + printData._year + "' BETWEEN FISCALYEAR(T1.DOMI_ENTDAY) AND FISCALYEAR(VALUE(T1.DOMI_OUTDAY, '9999-12-31')) ");
                                    }

                                    if (param._isOutputDebugQuery) {
                                        log.info(" dom sql = " + sql.toString());
                                    }

                                    param.setPs(db2, psKeyDom, sql.toString());
                                }
                                final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKeyDom), new Object[] {printData._schregno}));
                                personalInfo._inDomitory = !row.isEmpty();
                                personalInfo._dormitoryName = KnjDbUtils.getString(row, "DOMI_NAME");
                                personalInfo._dormitoryAddr1 = KnjDbUtils.getString(row, "DOMI_ADDR1");
                                personalInfo._dormitoryAddr2 = KnjDbUtils.getString(row, "DOMI_ADDR2");
                            }

                        } else {
                            final String psKeyDorm1 = "PS_KEY_DORM1";
                            if (null == param.getPs(psKeyDorm1)) {
                                // 寮情報
                                final StringBuffer sql = new StringBuffer();
                                sql.append(" SELECT ");
                                sql.append("     CASE WHEN NMH108.NAMECD1 IS NULL THEN 0 ELSE 1 END AS IN_DORMITORY ");
                                sql.append("   , CASE WHEN NMH108.NAMECD1 IS NULL THEN '' ELSE NMH108.NAME2 END AS DORMITORY_NAME ");
                                sql.append("   , CAST(NULL AS VARCHAR(1)) AS DOMI_ADDR1 ");
                                sql.append("   , CAST(NULL AS VARCHAR(1)) AS DOMI_ADDR2 ");
                                sql.append(" FROM SCHREG_ENVIR_DAT ENVIR ");
                                sql.append(" LEFT JOIN NAME_MST NMH108 ON NMH108.NAMECD1 = 'H108' AND NMH108.NAMECD2 = ENVIR.RESIDENTCD AND NMH108.NAMESPARE1 = '4' ");
                                sql.append(" WHERE ENVIR.SCHREGNO = ? ");

                                param.setPs(db2, psKeyDorm1, sql.toString());
                            }
                            for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKeyDorm1), new Object[] {printData._schregno})) {
                                if (param._isOutputDebug) {
                                    log.info(debugMapToStr(" dorm1 = ", row));
                                }
                                personalInfo._inDomitory = "1".equals(KnjDbUtils.getString(row, "IN_DORMITORY"));
                                personalInfo._dormitoryName = KnjDbUtils.getString(row, "DORMITORY_NAME");
                                personalInfo._dormitoryAddr1 = KnjDbUtils.getString(row, "DOMI_ADDR1");
                                personalInfo._dormitoryAddr2 = KnjDbUtils.getString(row, "DOMI_ADDR2");
                            }
                        }
                    }

                    final String psBaseDetail = "PS_BASE_DETAIL_MST";
                    if (null == param.getPs(psBaseDetail)) {
                        final StringBuffer sql = new StringBuffer();
                        sql.append(" SELECT T1.* ");
                        sql.append(" FROM SCHREG_BASE_DETAIL_MST T1 ");
                        sql.append(" WHERE T1.SCHREGNO = ? ");
                        sql.append("   AND T1.BASE_SEQ = ? ");

                        if (param._isOutputDebugQuery) {
                            log.info(" baseDetail sql = " + sql.toString());
                        }

                        param.setPs(db2, psBaseDetail, sql.toString());
                    }

                    personalInfo._baseDetailMstSeq008BaseRemark1 = KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psBaseDetail), new Object[] { printData._schregno, "008"})), "BASE_REMARK1");
                }
            }

            return personalInfo;
        }

        private static String getPersonalInfoSql(final Param param, final PrintData printData) {

            String schoolMstSchoolKind = null;
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                schoolMstSchoolKind = printData._certifKindSchoolKind;
            }
            log.info(" isGrd = " + param._isPrintGrd + ", SchoolMstSchoolKind = " + schoolMstSchoolKind);

            final StringBuffer sql = new StringBuffer();

            final String q = "?";

            sql.append("SELECT ");
            sql.append(" BASE.NAME,");
            sql.append(" BASE.NAME_ENG,");
            sql.append(" BASE.REAL_NAME,");
            sql.append(" BASE.REAL_NAME_KANA,");
            sql.append(" BASE.NAME_KANA,");
            sql.append(" BASE.BIRTHDAY, ");
            sql.append(" BASE.SEX, ");
            sql.append(" KGLED.BIRTHDAY_FLG, ");
            sql.append(" REGD.GRADE, ");
            sql.append(" REGD.ATTENDNO, ");
            sql.append(" REGDH.HR_NAME,");
            sql.append(" REGDH.HR_CLASS_NAME1,");
            sql.append(" REGD.ANNUAL, ");
            //課程・学科・コース
            sql.append(" CM.COURSECD, ");
            sql.append(" CM.COURSENAME, ");
            sql.append(" MAJ.MAJORCD, ");
            sql.append(" MAJ.MAJORNAME, ");
            if (param._hasMAJOR_MST_MAJORNAME2) {
                sql.append(" MAJ.MAJORNAME2, ");
            } else {
                sql.append(" CAST(NULL AS VARCHAR(1)) AS MAJORNAME2, ");
            }
            sql.append(" T5.COURSECODE, ");
            sql.append(" T5.COURSECODENAME, ");
            sql.append(" CM.COURSEABBV, ");
            sql.append(" MAJ.MAJORABBV,");
            sql.append(" CM.COURSEENG, ");
            sql.append(" MAJ.MAJORENG,");
            //卒業
            sql.append(" EGHIST.GRD_DIV, ");
            sql.append(" EGHIST.GRD_DATE, ");
            if ("1".equals(printData._certifSchoolOnly)) {
                sql.append(" T10.GRADUATE_DATE ");
            } else {
                sql.append(" CASE WHEN EGHIST.GRD_DATE IS NULL THEN ");
                if (param._isPrintGrd) {
                    sql.append("     CASE WHEN INT(REGD.ANNUAL) < 3 THEN NULL ");
                    sql.append("       ELSE ");
                    sql.append("         RTRIM(CHAR(INT(REGD.YEAR) + 1)) ");
                    sql.append("               || '-' || RTRIM(CHAR(MONTH(T10.GRADUATE_DATE))) || '-01' ");
                    sql.append("     END ");
                } else {
                    sql.append("         RTRIM(CHAR(INT(REGD.YEAR) + ");
                    sql.append("                           CASE WHEN NMA023.NAMESPARE2 IS NOT NULL THEN ");
                    sql.append("                                   INT(NMA023.NAMESPARE2) - INT(REGD.ANNUAL) + 1 ");
                    sql.append("                                ELSE (CASE REGD.ANNUAL WHEN '01' THEN 3 ");
                    sql.append("                                                     WHEN '02' THEN 2 ");
                    sql.append("                                                     ELSE 1 ");
                    sql.append("                                      END)");
                    sql.append("                           END ");
                    sql.append("                   )) ");
                    sql.append("               || '-' || RTRIM(CHAR(MONTH(T10.GRADUATE_DATE))) || '-'  || RTRIM(CHAR(DAY(T10.GRADUATE_DATE))) ");
                }
                sql.append(" ELSE VARCHAR(EGHIST.GRD_DATE) END ");
            }
            sql.append("   AS GRADU_DATE,");
            sql.append(" CASE WHEN EGHIST.GRD_DATE IS NULL THEN '卒業見込み' ");
            sql.append("  ELSE (SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A003' AND EGHIST.GRD_DIV = ST2.NAMECD2) END ");
            sql.append("  AS GRADU_NAME,");
            sql.append(" CASE WHEN EGHIST.GRD_DATE IS NOT NULL THEN (SELECT DISTINCT MAX(ANNUAL) ");
            sql.append("   FROM SCHREG_REGD_DAT ST1 ");
            sql.append("   WHERE ST1.YEAR = FISCALYEAR(EGHIST.GRD_DATE) ");
            sql.append("     AND ST1.SCHREGNO = REGD.SCHREGNO ");
            sql.append(" ) END AS GRADU_GRADE,");
            sql.append(" CASE WHEN EGHIST.GRD_DATE IS NOT NULL THEN (SELECT DISTINCT MAX(ST3.GRADE_CD) ");
            sql.append("   FROM SCHREG_REGD_DAT ST1 ");
            sql.append("   INNER JOIN SCHREG_REGD_GDAT ST3 ON ST3.YEAR = ST1.YEAR AND ST3.GRADE = ST1.GRADE ");
            sql.append("   WHERE ST1.YEAR = FISCALYEAR(EGHIST.GRD_DATE) ");
            sql.append("     AND ST1.SCHREGNO = REGD.SCHREGNO ");
            sql.append(" ) END AS GRADU_GRADE_CD,");
            sql.append(" EGHIST.GRD_NO, ");
            sql.append(" BASE.GRD_NO AS BASE_GRD_NO, ");
            //入学
            sql.append(" EGHIST.ENT_DATE, ");
            sql.append(" EGHIST.ENT_DIV,");
            sql.append(" (SELECT DISTINCT ANNUAL ");
            sql.append("   FROM SCHREG_REGD_DAT ST1 ");
            sql.append("  WHERE ST1.YEAR = FISCALYEAR(EGHIST.ENT_DATE) AND ST1.SCHREGNO = REGD.SCHREGNO ");
            sql.append("  ) AS ENTER_GRADE,");
            sql.append(" (SELECT MIN(ST3.GRADE_CD) ");
            sql.append("   FROM SCHREG_REGD_DAT ST1 ");
            sql.append("   INNER JOIN SCHREG_REGD_GDAT ST3 ON ST3.YEAR = ST1.YEAR AND ST3.GRADE = ST1.GRADE ");
            sql.append("  WHERE ST1.YEAR = FISCALYEAR(EGHIST.ENT_DATE) AND ST1.SCHREGNO = REGD.SCHREGNO ");
            sql.append("  ) AS ENTER_GRADE_CD,");
            sql.append(" (SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A002' AND EGHIST.ENT_DIV = ST2.NAMECD2) AS ENTER_NAME,");
            sql.append(" CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            sql.append(" T11.NAME_OUTPUT_FLG, ");
            sql.append(" REGDG.GRADE_CD, ");
            sql.append(" REGDG.SCHOOL_KIND, ");
            sql.append(" REGD.SCHREGNO ");
            sql.append(" FROM ");
            //学籍情報(??? or ????)
            if (param._isPrintGrd) {
                sql.append(" GRD_REGD_DAT REGD ");
            } else {
                sql.append(" SCHREG_REGD_DAT REGD ");
            }
            if (param._isPrintGrd) {
                sql.append("      INNER JOIN GRD_REGD_HDAT   REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            } else {
                sql.append("      LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            }
            sql.append(     "LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND REGDG.GRADE = REGD.GRADE ");

            sql.append(" LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = REGD.SCHREGNO AND EGHIST.SCHOOL_KIND = '" + printData._certifKindSchoolKind + "' ");
            if (param._isPrintGrd) {
                //卒業情報有りの場合
                sql.append(" LEFT JOIN SCHOOL_MST T10 ON T10.YEAR = REGD.YEAR ");
                if (!StringUtils.isBlank(schoolMstSchoolKind)) {
                    sql.append(" AND T10.SCHOOL_KIND = '" + schoolMstSchoolKind + "' ");
                }
                //基礎情報
                sql.append(" INNER JOIN GRD_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            } else {
                // 卒業情報有りの場合
                sql.append(" INNER JOIN SCHOOL_MST T10 ON T10.YEAR = REGD.YEAR ");
                if (!StringUtils.isBlank(schoolMstSchoolKind)) {
                    sql.append(" AND T10.SCHOOL_KIND = '" + schoolMstSchoolKind + "' ");
                }
                // 基礎情報
                sql.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            }
            sql.append("LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = EGHIST.FINSCHOOLCD ");
            sql.append("LEFT JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' AND NML001.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
            // 課程、学科、コース
            sql.append("LEFT JOIN COURSE_MST CM ON CM.COURSECD = REGD.COURSECD ");
            sql.append("LEFT JOIN MAJOR_MST MAJ ON MAJ.COURSECD = REGD.COURSECD AND MAJ.MAJORCD = REGD.MAJORCD ");
            sql.append("LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = REGD.YEAR AND VALUE(T5.COURSECODE,'0000') = VALUE(REGD.COURSECODE,'0000')");
            sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = BASE.SCHREGNO AND T11.DIV = '01' ");
            sql.append("LEFT JOIN NAME_MST NMA023 ON NMA023.NAMECD1 = 'A023' AND NMA023.NAME1 = REGDG.SCHOOL_KIND ");
            sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT KGLED ON KGLED.SCHREGNO = BASE.SCHREGNO AND KGLED.BIRTHDAY_FLG = '1' ");
            sql.append(" WHERE ");
            sql.append("        REGD.SCHREGNO = " + q + " ");
            sql.append("    AND REGD.YEAR = " + q + " ");
            sql.append("    AND REGD.SEMESTER = " + q + " ");

            return sql.toString();
        }
    }

    private static class PrintData {
        final String _schregno;
        final String _certifSchoolDatCertifKind;
        final String _certifKind;
        final String _year;        // 対象年度
        final String _semester;    // 対象学期
        final String _certifDate;
        String _certifNo;
        final String _certifDateYear;       // 現年度
        final String _documentRoot;
        final String _useAddrField2;
        final String _entGrdDateFormat;
        final boolean _isPrintStamp;
        final String _certifPrintRealName;
        final String _useShuryoShoumeisho;
        final String _chutouKyoikuGakkouFlg;
        final String _knjg010PrintRegdGradeAsGrade;
        final String _knjg010PrintGradeCdAsGrade;
        final String _certifSchoolOnly;
        final boolean _isPrintPrincipalSignature;
        final Param _param;
        String _schoollogoHJpgImagePath = null;
        String _schoollogoJpgImagePath = null;
        String _schoollogoImagePath = null;
        List<String> _certifSchoolstampImageExtensions = Collections.emptyList();
        final String _certifKindSchoolKind;
        String _formname;
        boolean _isFormForStamp = false;
        String _setCertifSchoolstampImagePath = null;

        final String _ctrlYear;
        String _hrname;
        boolean _isTeiji;
        boolean _seirekiFlg = false;
        boolean _ikkanFlg = false;
        String _schoolMstSchoolname1 = null;
        String _schoolMstSchoolnameEng = null;
        String _schoolMstSchooldiv = null;
        String _schoolMstSchooltelno = "";
        String _schoolMstSchoolfaxno = "";
        SchoolInfo _schoolInfo;
        final PersonalInfo _personalInfo;
        final CK _ck;
        Properties _prgInfoPropertiesFilePrperties = null;
        String _stampSizeMm;

        private Map<String, String> _transferDatMap = new HashMap<String, String>();
        private Map<String, String> _guardianDatMap = new HashMap<String, String>();

        public PrintData(final String[] arrayParam, final String ctrlYear, final Param param, final DB2UDB db2) {
            _certifSchoolOnly = arrayCheck(arrayParam, 23);
            String certifKind = arrayParam[1];
            _certifSchoolDatCertifKind = certifKind;
            _param = param;
            if (_param._delegeteCkMap.containsKey(CK.getCK(certifKind))) {
                certifKind = new DecimalFormat("000").format(_param._delegeteCkMap.get(CK.getCK(certifKind))._certifKind);
                log.info(" invoke " + certifKind + " instead of " + arrayParam[1]);
            }
            _certifKind = certifKind;
            _year = arrayParam[2];
            _semester = arrayParam[3];
            _ck = CK.getCK(_certifKind);
            if ("1".equals(_certifSchoolOnly)) {
                _schregno = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = ? AND SEMESTER = ? ORDER BY GRADE, HR_CLASS, ATTENDNO ", new Object[] {_year, _semester}));
            } else {
                _schregno = arrayParam[0];
            }
            _certifDate = arrayParam[8];
            _certifNo = arrayParam[9];
            _certifDateYear = arrayParam[11];
            _documentRoot = arrayParam[12];
            if (null != _documentRoot) {
                _prgInfoPropertiesFilePrperties = _param.getPrgInfoProperty(_documentRoot);
            }
            _useAddrField2 = arrayCheck(arrayParam, 16);
            _entGrdDateFormat = arrayCheck(arrayParam, 17);
            final String paramPrintStamp = arrayCheck(arrayParam, 18);
            final boolean isPrintStampAllways = _param._z010.in(Z010.Sakae, Z010.Ritsumeikan) || (_param._z010.in(Z010.ChiyodaKudan) && _ck.in(CK._22_SOTSU_J,  CK._32_SOTSU_J_ENG));
            _isPrintStamp = isPrintStampAllways || "1".equals(paramPrintStamp);
            _certifPrintRealName = defstr(arrayCheck(arrayParam, 19), getProperty(Property.certifPrintRealName));

            _useShuryoShoumeisho = defstr(arrayCheck(arrayParam, 20), getProperty(Property.useShuryoShoumeisho));
            _chutouKyoikuGakkouFlg = defstr(arrayCheck(arrayParam, 21), getProperty(Property.chutouKyoikuGakkouFlg));
            _knjg010PrintGradeCdAsGrade = defstr(arrayCheck(arrayParam, 22), getProperty(Property.knjg010PrintGradeCdAsGrade));
            _knjg010PrintRegdGradeAsGrade = getProperty(Property.knjg010PrintRegdGradeAsGrade);

            _ctrlYear = ctrlYear;
            _seirekiFlg = param._z010.in(Z010.jyoto) ? false : KNJ_EditDate.isSeireki(db2);
            _ikkanFlg = "1".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "))); //中高一貫
            _schoollogoImagePath = _param.getImagePath(_documentRoot, "SCHOOLLOGO", "." + _param._extension);
            if (_isPrintStamp) {
                final List<String> exts = new ArrayList<String>();
                exts.add(".bmp");
                if (!(isPrintStampAllways || _param._z010.in(Z010.Osakatoin))) {
                    exts.add(".jpg");
                }
                _certifSchoolstampImageExtensions = exts;
                _stampSizeMm = getProperty(Property.stampSizeMm);
            }
            _isPrintPrincipalSignature = (_param._z010.in(Z010.Osakatoin) && _isPrintStamp) || _param._z010.in(Z010.Hirokoudai) || (_param._z010.in(Z010.ChiyodaKudan) && _ck.in(CK._22_SOTSU_J,  CK._32_SOTSU_J_ENG)) || _param._z010.in(Z010.Meikei);
            _schoollogoHJpgImagePath = _param.getImagePath(_documentRoot, "SCHOOLLOGO_H", ".jpg");
            _schoollogoJpgImagePath = _param.getImagePath(_documentRoot, "SCHOOLLOGO", ".jpg");

            if (_ck == CK._43_KYUGAKU_KYOKA || _ck == CK._44_RYUGAKU_KYOKA) {
                String transfercd = null;
                if (_ck == CK._43_KYUGAKU_KYOKA) {
                    transfercd = "2";
                } else if (_ck == CK._44_RYUGAKU_KYOKA) {
                    transfercd = "1";
                }
                final StringBuffer sqlTransfer = new StringBuffer();
                sqlTransfer.append(" SELECT T1.*, ");
                sqlTransfer.append("        FISCALYEAR(TRANSFER_SDATE) AS SDATE_YEAR, ");
                sqlTransfer.append("        FISCALYEAR(VALUE(TRANSFER_EDATE, '9999-12-31')) AS EDATE_YEAR ");
                sqlTransfer.append(" FROM SCHREG_TRANSFER_DAT T1 ");
                sqlTransfer.append(" WHERE SCHREGNO = '" + _schregno + "' AND TRANSFERCD = '" + transfercd + "' ");
                sqlTransfer.append(" ORDER BY TRANSFER_SDATE DESC ");

                for (final Map row : KnjDbUtils.query(db2, sqlTransfer.toString())) {
                    final int transferSdateYear = Integer.parseInt(KnjDbUtils.getString(row, "SDATE_YEAR"));
                    if (transferSdateYear <= Integer.parseInt(_year)) {
                        _transferDatMap = row;
                        break;
                    }
                }
            }

            if (_ck == CK._45_TAIGAKU_KYOKA) {
                final StringBuffer sqlGuard = new StringBuffer();
                sqlGuard.append(" SELECT T1.GUARD_NAME ");
                sqlGuard.append("      , T1.GUARD_REAL_NAME ");
                sqlGuard.append("      , CASE WHEN T2.SCHREGNO IS NOT NULL THEN '1' END AS USE_GUARD_REAL_NAME ");
                sqlGuard.append("      , T2.GUARD_NAME_OUTPUT_FLG ");
                sqlGuard.append(" FROM GUARDIAN_DAT T1 ");
                sqlGuard.append(" LEFT JOIN GUARDIAN_NAME_SETUP_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                sqlGuard.append("     AND T2.DIV = '01' ");
                sqlGuard.append(" WHERE T1.SCHREGNO = '" + _schregno + "' ");

                _guardianDatMap = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sqlGuard.toString()));
            }
            final String loginYearSchoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT T2.SCHOOL_KIND FROM SCHREG_REGD_DAT T1 INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE WHERE T1.SCHREGNO = '" + _schregno  +"' AND T1.YEAR = '" + _year + "' AND T1.SEMESTER = '" + _semester + "' "));

            _certifKindSchoolKind = StringUtils.defaultString(_ck._schoolKind, loginYearSchoolKind);
            _personalInfo = PersonalInfo.getPersonalInfo(db2, this, _param);
            log.info(" schregno " + _schregno + ", certifKind = " + _ck + ", certifKindSchoolKind = " + _certifKindSchoolKind);
            _schoolInfo = SchoolInfo.getSchoolInfo(db2, this, _param);
            if (_param._isOutputDebug) {
                log.info(debugMapToStr(" school info = ", _schoolInfo._map));
            }

            setFormForStamp(_param, _ck);
        }

        private void setFormForStamp(final Param param, final CK ck) {
            _isFormForStamp = false;
            if (param._z010.in(Z010.Chukyo, Z010.Kyoai)) {
                _isFormForStamp = ck.isPrintSotsugyo() || ck.isPrintSotsugyoMikomi() || ck.isPrintZaigaku();
            } else if (param._z010.in(Z010.Sakae)) {
                _isFormForStamp = true;
            } else if (param._z010.in(Z010.Matsudo)) {
                _isFormForStamp = ck.isPrintSotsugyo() || ck.isPrintSotsugyoMikomi() || ck.isPrintZaigaku() || ck.isPrintZaigakuEng() || ck.isPrintSotsugyoEng();
            } else if (param._z010.in(Z010.Osakatoin)) {
                _isFormForStamp = ck.isPrintSotsugyo() || ck.isPrintSotsugyoMikomi() || ck.isPrintZaigaku() || ck.isPrintZaiseki() || ck.isPrintShuryo();
            } else if (param._z010.in(Z010.Ritsumeikan)) {
                _isFormForStamp = ck.isPrintSotsugyo() || ck.isPrintSotsugyoMikomi() || ck.isPrintZaigaku() || ck.isPrintZaiseki();
            } else if (param._z010.in(Z010.ChiyodaKudan)) {
                _isFormForStamp = _ck.in(CK._22_SOTSU_J,  CK._32_SOTSU_J_ENG);
            } else if (param._z010.in(Z010.aoyama)) {
                _isFormForStamp = ck.isPrintSotsugyo() || ck.isPrintSotsugyoMikomi() || ck.isPrintZaigaku() || ck.isPrintZaiseki();
            } else if (param._z010.in(Z010.Meikei)) {
                _isFormForStamp = ck.isPrintSotsugyo() || ck.isPrintZaigaku() || ck.in(CK._56_SPORTS_SHINKOU_CENTER_KANYU_H, CK._57_SPORTS_SHINKOU_CENTER_KANYU_J);
            } else if (ck.isPrintSotsugyo()) {
                if (param._z010.in(Z010.Hirokoudai, Z010.Sapporo, Z010.Musashinohigashi, Z010.Rakunan, Z010.Kyoto, Z010.Chiben, Z010.Sanonihon)) {
                } else {
                    _isFormForStamp = true;
                }
            } else if (ck.isPrintSotsugyoEng()) {
                if (param._z010.in(Z010.Sapporo) && ck.in(CK._2_SOTSU_ENG, CK._19_SOTSUMI_ENG, CK._16_SHURYO_ENG, CK._39_SHURYOMI_ENG)) {
                } else if (param._z010.in(Z010.Sundaikoufu)  && H.equals(_personalInfo._schoolKind) && ck.in(CK._2_SOTSU_ENG, CK._19_SOTSUMI_ENG)) {
                } else if (param._z010.in(Z010.Rakunan) && ck.in(CK._2_SOTSU_ENG)) {
                } else {
                    _isFormForStamp = true;
                }
            } else if (ck.isPrintZaigaku()) {
                if (param._z010.in(Z010.Hirokoudai) || K.equals(_certifKindSchoolKind) || param._z010.in(Z010.Musashinohigashi, Z010.Rakunan, Z010.Sapporo, Z010.Sanonihon)) {
                } else if (_ck.in(CK._4_ZAIGAKU, CK._12_ZAIGAKU_J)) {
                    _isFormForStamp = true;
                }
            } else if (ck.isPrintZaigakuEng()) {
                if (param._z010.in(Z010.Sundaikoufu, Z010.Sapporo, Z010.Meiji)) {
                } else {
                    _isFormForStamp = true;
                }
            } else {
                _isFormForStamp = true;
            }
            if (param._isOutputDebug) {
                if (_isFormForStamp) {
                    log.info(" _isFormForStamp = " + _isFormForStamp);
                }
            }
        }

        public String getProperty(final Property p) {
            final String name = p.name();
            if (null != _param._dbPrgInfoProperties) {
                if (_param._dbPrgInfoProperties.containsKey(name)) {
                    final String prop = _param._dbPrgInfoProperties.get(name);
                    if (_param._isOutputDebug) {
                        _param.logOnce("PrintData.getProperty :: db prop [" + name + "] = " + prop);
                    }
                    return prop;
                }
            }
            if (null != _prgInfoPropertiesFilePrperties) {
                if (!_prgInfoPropertiesFilePrperties.containsKey(name)) {
                    if (_param._isOutputDebug) {
                        _param.logOnce("PrintData.getProperty :: no prop [" + name + "]");
                    }
                    return null;
                }
                final String prop = _prgInfoPropertiesFilePrperties.getProperty(name);
                if (_param._isOutputDebug) {
                    _param.logOnce("PrintData.getProperty :: prop [" + name + "] = " + prop);
                }
                return prop;
            }
            return null;
        }

        private static String arrayCheck(final String[] array, final int i) {
            return array.length < i + 1  ? null : array[i];
        }

        public String getCertifSchoolStampSchoolKindImagePath(final Param param, final String schoolKind) {
            if (!_isPrintStamp) {
                return null;
            }
            String path = null;
            for (final String ext : _certifSchoolstampImageExtensions) {
                path = param.getImagePath(_documentRoot, "CERTIF_SCHOOLSTAMP_" + schoolKind, ext);
                if (null == path) {
                    path = param.getImagePath(_documentRoot, "SCHOOLSTAMP_" + schoolKind, ext);
                }
                if (null != path) {
                    break;
                }
            }
            return path;
        }

        public String getPrincipalSignatureImagePath(final Param param, final String schoolKind) {
            return param.getImagePath(_documentRoot, "PRINCIPAL_SIGNATURE_" + schoolKind, ".jpg");
        }

        public String getZaisekiShoumeiGrdDate(final Param param) {
            if (null != _personalInfo._graduDate && ("2".equals(_personalInfo._grdDiv) || "3".equals(_personalInfo._grdDiv))) {
                if (param._isOutputDebug) {
                    log.info("在籍証明書卒業日付の対象者は卒業生のみ: " + _schregno + " : grdDiv = " + _personalInfo._grdDiv);
                }
                return _personalInfo._graduDate;
            }
            String rtn = _personalInfo._graduDate;
            if (null != _schoolInfo._remark7) {
                Calendar cal = Utils.parseMonthSlashDayOfMonth(_ctrlYear, _schoolInfo._remark7);
                if (null != cal) {
                    final int setMonth = cal.get(Calendar.MONTH);
                    final Calendar grdDateCal = Utils.getCalendar(_personalInfo._graduDate);
                    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    if (null == grdDateCal) {
                        rtn = sdf.format(cal.getTime());
                    } else {
                        final int grdMonth = grdDateCal.get(Calendar.MONTH);
                        if (setMonth <= Calendar.MARCH && grdMonth > Calendar.MARCH) {
                            cal.set(Calendar.YEAR, grdDateCal.get(Calendar.YEAR) + 1);
                        } else if (setMonth > Calendar.MARCH && grdMonth <= Calendar.MARCH) {
                            cal.set(Calendar.YEAR, grdDateCal.get(Calendar.YEAR) - 1);
                        } else {
                            cal.set(Calendar.YEAR, grdDateCal.get(Calendar.YEAR));
                        }
                        rtn = sdf.format(cal.getTime());
                    }
                }
            }
            if (null == rtn) {
                rtn = _personalInfo._graduDate;
            }
            if (param._isOutputDebug) {
                log.info("在籍証明書卒業日付: REMARK7 = " + _schoolInfo._remark7 + ", " + _schregno + ", grdDate = " + _personalInfo._graduDate + ", 日付 = " + rtn);
            }

            return rtn;
        }

        public String getPrintGuardName() {
            try {
                final String useGuardRealName = KnjDbUtils.getString(_guardianDatMap, "USE_GUARD_REAL_NAME");
                final String guardRealName = KnjDbUtils.getString(_guardianDatMap, "GUARD_REAL_NAME");
                return "1".equals(useGuardRealName) && null != guardRealName ? guardRealName : KnjDbUtils.getString(_guardianDatMap, "GUARD_NAME");
            } catch (Throwable t) {
                log.error("exception!", t);
            }
            return null;
        }

        public void setSchoolMst(final DB2UDB db2, final PersonalInfo personalInfo, final Param param) {
            //  学校名の取得
            String sql = "SELECT SCHOOLNAME1, SCHOOLNAME_ENG, SCHOOLDIV, SCHOOLTELNO, SCHOOLFAXNO FROM SCHOOL_MST WHERE YEAR = '" + _certifDateYear + "'";
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql += " AND SCHOOL_KIND = '" + personalInfo._schoolKind + "' ";
            }
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if (param._isOutputDebug) {
                log.info(" school mst sql = " + sql);
                log.info(debugMapToStr(" school mst row = ", row));
            }

            _schoolMstSchoolname1 = KnjDbUtils.getString(row, "SCHOOLNAME1");
            _schoolMstSchoolnameEng = KnjDbUtils.getString(row, "SCHOOLNAME_ENG");
            _schoolMstSchooldiv = KnjDbUtils.getString(row, "SCHOOLDIV");
//            if ((o._isMusashi || o._z010 == Z010.Meiji) && _isEnglish) {
            if (param._z010 == Z010.Meiji && _ck._isEnglish) {
                _schoolMstSchooltelno = " +81-" + (defstr(KnjDbUtils.getString(row, "SCHOOLTELNO")).length() > 1 ? KnjDbUtils.getString(row, "SCHOOLTELNO").substring(1) : "");
                _schoolMstSchoolfaxno = " +81-" + (defstr(KnjDbUtils.getString(row, "SCHOOLFAXNO")).length() > 1 ? KnjDbUtils.getString(row, "SCHOOLFAXNO").substring(1) : "");
            } else {
                _schoolMstSchooltelno = KnjDbUtils.getString(row, "SCHOOLTELNO");
                _schoolMstSchoolfaxno = KnjDbUtils.getString(row, "SCHOOLFAXNO");
            }
        }

        private void setNameZ001(final DB2UDB db2, final PersonalInfo personalInfo, final Param param) {
            String sql =
                    " SELECT T1.NAMESPARE3 FROM NAME_MST T1 " +
                            " INNER JOIN SCHOOL_MST T2 ON T2.YEAR = '" + _year + "' AND T2.SCHOOLDIV = T1.NAMECD2 " +
                            " WHERE T1.NAMECD1 = 'Z001' ";
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                sql += " AND T2.SCHOOL_KIND = '" + personalInfo._schoolKind + "' ";
            }
            _isTeiji = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));
        }

        private void setCertifNo(final Param param) {
            //証書番号の印刷 0:あり,1:なし
            if ("0".equals(_schoolInfo._certifNo)) {
                if (null == _certifNo || "".equals(_certifNo)) {
                    _certifNo = "     "; // 証明書番号の印刷が0:あり かつ証明書番号が無い場合 5スペース挿入
                } else if (param._z010.in(Z010.Kyoto)) {
                    final int maxlen = 5;
                    _certifNo = trim(_certifNo);
                    final int len = _certifNo.length();
                    final int spc = (maxlen - len) / 2;
                    _certifNo = StringUtils.repeat(" ", maxlen - spc - len) + _certifNo + StringUtils.repeat(" ", spc);
                }
            } else {
                _certifNo = "     ";
            }
        }

        /**
         * 左右のスペース、全角スペースを除去した文字列を得る
         * @param s 文字列
         * @return 左右のスペース、全角スペースを除去した文字列
         */
        private static String trim(final String s) {
            if (null == s) {
                return s;
            }
            int si = 0;
            while (si < s.length() && (s.charAt(si) == ' ' || s.charAt(si) == '　')) {
                si++;
            }
            int ei = s.length() - 1;
            while (0 <= ei && (s.charAt(ei) == ' ' || s.charAt(ei) == '　')) {
                ei--;
            }
            if (si >= s.length() || ei < 0) {
                return "";
            }
            return s.substring(si, ei + 1);
        }

        /*
         *  証明日付の出力
         */
        private static String printsvfDate(
                final DB2UDB db2,
                final PrintData printData,
                final String date,
                final boolean addSpace,
                final boolean printDefaultFormat
        ) {
            String space = addSpace ? " " : "";
            if (printData._seirekiFlg) {
                if (date != null && 0 < date.length()) {
                    return date.substring(0, 4) + space + "年" + space + Utils.h_format_JP_MD(date);  // 証明日付
                } else if (printDefaultFormat) {
                    return "    年  月  日";
                }
            } else {
                return Utils.h_format_JP(db2, date, addSpace, printDefaultFormat);  // 証明日付
            }
            return null;
        }

        private static String yearTh(final String grade) {
            final String rtn;
            final String[] arr = {null, "first", "second", "third", "fourth", "fifth", "sixth"};
            if (NumberUtils.isDigits(grade) && Integer.parseInt(grade) > 0 && Integer.parseInt(grade) < arr.length) {
                rtn = arr[Integer.parseInt(grade)];
            } else {
                rtn = "     ";
            }
            return rtn;
        }

        private static String hankakuToZenkaku(final String name) {
            if (null == name) {
                return null;
            }
            final Map henkanMap = new HashMap();
            henkanMap.put("1", "１");
            henkanMap.put("2", "２");
            henkanMap.put("3", "３");
            henkanMap.put("4", "４");
            henkanMap.put("5", "５");
            henkanMap.put("6", "６");
            henkanMap.put("7", "７");
            henkanMap.put("8", "８");
            henkanMap.put("9", "９");
            henkanMap.put("0", "０");
            final StringBuffer sb = new StringBuffer();
            for (int i = 0; i < name.length(); i++) {
                final String key = name.substring(i, i + 1);
                final String val = henkanMap.containsKey(key) ? (String) henkanMap.get(key) : key;
                sb.append(val);
            }
            return sb.toString();
        }
    }

    private static enum Z010 {
        Kindai("近大", "KINDAI", "KINJUNIOR"),
        Withus("WITHUS"),
        Chiben("智辯", "CHIBEN"),
//        Tottori("tottori"),
        Chukyo("中京", "chukyo"),
//        Musashi("MUSASHI"),
        Kumamoto("熊本県", "kumamoto"),
        Kyoai("共愛", "kyoai"),
        tokiwa("常磐"),
        Kyoto("京都府", "kyoto"),
        Meiji("明治学園", "meiji"),
        Miyagiken("宮城県", "miyagiken"),
        Sapporo("札幌開成", "sapporo"),
        Musashinohigashi("武蔵野東", "musashinohigashi"),
        Nishiyama("京都西山", "nishiyama"),
        Mieken("三重県", "mieken"),
        Sundaikoufu("駿台甲府", "sundaikoufu"),
        Rakunan("洛南", "rakunan"),
        Seijyo("成城", "seijyo"),
        ChiyodaKudan("千代田九段", "chiyoda"),
        Hirokoudai("広工大", "hirokoudai"),
        Meikei("茗溪", "meikei"),
        Osakatoin("大阪桐蔭", "osakatoin"),
        Sakae("埼玉栄", "sakae"),
        Sanonihon("佐野日本", "sanonihon"),
        Ritsumeikan("立命館", "Keisho", "Moriyama", "Nagaokakyo", "Ritsumeikan"),
        Nagisa("広島なぎさ", "nagisa"),
        Risshisha("立志舎", "risshisha"),
        Higashiosaka("東大阪", "higashiosaka"),
        Matsudo("専修大松戸", "matsudo"),
        NichidaiNikou("日大二校", "nichi-ni"),
        Reitaku("麗澤", "reitaku"),
        KaichiIkkan("開智一貫部", "kikan"),
        KaichiSougou("開智総合部", "ksogo"),
        KaichiKoutou("開智高等部", "kkotou"),
        KaichiTushin("開智通信", "ktsushin"),
        kwansei("関西学院"),
        jyoto("福岡工業城東"),
        suito("大阪水都国際"),
        aoyama("青山学院"),
        ryukei("流通経済大学付属高校"),
        kenja("賢者", (String[]) null);

        final String _debug;
        final String[] _name1;
        Z010(final String debug, final String ... name1) {
            _debug = debug;
            _name1 = name1;
        }
        public static Z010 fromString(final String name1) {
            Z010 rtn = null;
            for (final Z010 v : Z010.values()) {
                if (null != v._name1 && v._name1.length == 0) {
                    if (v.name().equals(name1)) {
                        rtn = v;
                        break;
                    }
                } else {
                    if (ArrayUtils.contains(v._name1, name1)) {
                        rtn = v;
                        break;
                    }
                }
            }
            if (null == rtn) {
                rtn = kenja;
            }
            return rtn;
        }
        public boolean isKaichiSpec() {
            return this.in(KaichiIkkan, KaichiSougou, KaichiKoutou, KaichiTushin);
        }
        public boolean in(final Z010 ... z010s) {
            if (null != z010s) {
                for (final Z010 v : z010s) {
                    if (this == v) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    protected static enum Property {
        certifPrintRealName, // 設定によらず戸籍名を印字する
        useShuryoShoumeisho,
        chutouKyoikuGakkouFlg,
        knjg010PrintGradeCdAsGrade,
        knjg010PrintRegdGradeAsGrade,
        stampSizeMm, // 印影サイズ (単位:mm)
        stampPositionXmmSotsugyoShomeisho, // 卒業証明書印影位置X (単位:mm)
        stampPositionYmmSotsugyoShomeisho, // 卒業証明書印影位置Y (単位:mm)
        stampPositionXmmSotsugyoMikomiShomeisho, // 卒業証明書印影位置X (単位:mm)
        stampPositionYmmSotsugyoMikomiShomeisho, // 卒業証明書印影位置Y (単位:mm)
        stampPositionXmmZaigakuShomeisho, // 在学証明書印影位置X (単位:mm)
        stampPositionYmmZaigakuShomeisho, // 在学証明書印影位置Y (単位:mm)
        stampPositionXmmZaisekiShomeisho, // 在籍証明書印影位置X (単位:mm)
        stampPositionYmmZaisekiShomeisho, // 在籍証明書印影位置Y (単位:mm)
        stampPositionXmmSotsugyoShomeishoEng, // 卒業証明書印影位置X (単位:mm)
        stampPositionYmmSotsugyoShomeishoEng, // 卒業証明書印影位置X (単位:mm)
        stampPositionXmmZaigakuShomeishoEng, // 在学証明書印影位置X (単位:mm)
        stampPositionYmmZaigakuShomeishoEng, // 在学証明書印影位置Y (単位:mm)
        stampPositionXmmSotsugyoMikomiShomeishoEng, // 卒業証明書印影位置X (単位:mm)
        stampPositionYmmSotsugyoMikomiShomeishoEng, // 卒業証明書印影位置Y (単位:mm)
        stampPositionIgnoreSvfInjiIchiChousei, // フォーム設計の印字位置調整を無視
        ;

        final String _name;
        final boolean _preferPropertyFile;
        Property() {
            this(false, null);
        }
        Property(final boolean preferPropertyFile) {
            this(preferPropertyFile, null);
        }
        Property(final boolean preferPropertyFile, final String name) {
            _preferPropertyFile = preferPropertyFile;
            _name = null == name ? name() : name;
        }
    }

    private static class Param {
        private boolean _isPrintGrd;
        private String _z010name1;
        final Z010 _z010;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        final boolean _hasSCHREG_DOMITORY_HIST_DAT;
        final boolean _hasMAJOR_MST_MAJORNAME2;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugQuery;
        final boolean _isOutputDebugAll;
        final boolean _isOutputDebugField;
        final boolean _isOutputDebugSvfFormCreate;
        String _imagePath = null;
        String _extension = null;

        final SimpleDateFormat _sdfHyphen = new SimpleDateFormat("yyyy-MM-dd");
        final SimpleDateFormat _sdfSlash = new SimpleDateFormat("yyyy/MM/dd");
        final Locale localEnUS = new Locale("en","US");

        final KNJDefineSchool _definecode;  // 各学校における定数等設定

        final Map<String, Properties> _prgInfoPropertiesFilePrpertiesCache = new HashMap<String, Properties>();
        Map<String, String> _dbPrgInfoProperties;
        Set<String> _loggedSet;

        private final Map<String, String> _filenameCheck = new HashMap<String, String>();
        private final Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();
        private final Map<String, File> _createdFormFiles = new TreeMap<String, File>();
        private final Map<String, SvfForm> _svfForms = new HashMap<String, SvfForm>();
        final Map<String, Map<String, String>> _z002Map;
        final Map<CK, CK> _delegeteCkMap = new HashMap<CK, CK>();

        Param(final DB2UDB db2, final KNJDefineSchool definecode) {
            _definecode = definecode;
            _z010name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2= '00' "));
            _z010 = Z010.fromString(_z010name1);
            log.info(" z010 = " + _z010 + " (" + _z010name1 + ")");
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _hasSCHREG_DOMITORY_HIST_DAT = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_DOMITORY_HIST_DAT", null);
            _hasMAJOR_MST_MAJORNAME2 = KnjDbUtils.setTableColumnCheck(db2, "MAJOR_MST", "MAJORNAME2");
            final String[] outputDebug = StringUtils.split(KnjDbUtils.getDbPrginfoProperties(db2, "KNJG010_1T", "outputDebug"));
            _isOutputDebugAll = ArrayUtils.contains(outputDebug, "all");
            _isOutputDebug = _isOutputDebugAll || ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");
            _isOutputDebugField = ArrayUtils.contains(outputDebug, "field");
            _isOutputDebugSvfFormCreate = ArrayUtils.contains(outputDebug, "SvfFormCreate");
            setImagePath(db2);
            _z002Map = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, "SELECT * FROM NAME_MST WHERE NAMECD1 = 'Z002' "), "NAMECD2");

            if (KnjDbUtils.setTableColumnCheck(db2, "PRGINFO_PROPERTIES", "PROGRAMID")) {
                _dbPrgInfoProperties = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAME, VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJG010_1T' "), "NAME", "VALUE");
            }
            _loggedSet = new HashSet<String>();

            if (_z010.in(Z010.Reitaku)) {
                _delegeteCkMap.put(CK._14_ZAISEKI_ENG, CK._5_ZAIGAKU_ENG);
            }
        }

        public String z002(final String namecd2, final String field) {
            return KnjDbUtils.getString(_z002Map.get(namecd2), field);
        }

        public PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        public void setPs(final DB2UDB db2, final String psKey, final String sql) {
            try {
                PreparedStatement ps = db2.prepareStatement(sql);
                _psMap.put(psKey, ps);
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }

        public void close() {
            for (final PreparedStatement ps : _psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
            for (final File file : _createdFormFiles.values()) {
                try {
                    if (!_isOutputDebugSvfFormCreate) {
                        file.delete();
                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
        }

        public void logOnce(final String l) {
            if (_loggedSet.contains(l)) {
                return;
            }
            log.info(l);
            _loggedSet.add(l);
        }

        public String getImagePath(final String documentRoot, final String filename, final String extension) {
            final String path = documentRoot + "/" + (null == _imagePath ? "" : _imagePath + "/") + filename + extension;
            if (!_filenameCheck.containsKey(filename)) {
                final File file = new File(path);
                if (!file.exists() || _isOutputDebug) {
                    log.info(" file " + file.getPath() + " exists? " + file.exists());
                }
                if (!file.exists()) {
                    _filenameCheck.put(filename, null);
                } else {
                    _filenameCheck.put(filename, file.getPath());
                }
            }
            return _filenameCheck.get(filename);
        }

        public void setImagePath(final DB2UDB db2) {
            final Map controlMst = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' "));
            _imagePath = KnjDbUtils.getString(controlMst, "IMAGEPATH");
            _extension = KnjDbUtils.getString(controlMst, "EXTENSION");
        }

        private Date toDate(final String str) throws ParseException {
            Date ret = null;
            try {
                ret = _sdfHyphen.parse(str);
            } catch (ParseException e1) {
                try {
                    ret = _sdfSlash.parse(str);
                } catch (ParseException e2) {
                    throw e2;
                }
            }
            return ret;
        }

        public String formatDateUs(final String date, final String format, final String def) {
            String ret = null;
            if (!StringUtils.isBlank(date)) {
                try {
                    ret = new SimpleDateFormat(format, localEnUS).format(toDate(date));
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            if (null == ret) {
                ret = def;
            }
            return ret;
        }

        public Properties getPrgInfoProperty(final String documentroot) {
            if (!_prgInfoPropertiesFilePrpertiesCache.containsKey(documentroot)) {
                _prgInfoPropertiesFilePrpertiesCache.put(documentroot, loadPropertyFile(documentroot, "prgInfo.properties"));
            }
            return _prgInfoPropertiesFilePrpertiesCache.get(documentroot);
        }

        private Properties loadPropertyFile(final String documentroot, final String filename) {
            File file = null;
            if (null != documentroot) {
                file = new File(new File(documentroot).getParentFile().getAbsolutePath() + "/config/" + filename);
                if (_isOutputDebug) {
                    log.info("check prop : " + file.getAbsolutePath() + ", exists? " + file.exists());
                }
                if (!file.exists()) {
                    file = null;
                }
            }
            if (null == file) {
                file = new File(documentroot + "/" + filename);
            }
            if (!file.exists()) {
                if (_isOutputDebug) {
                    log.error("file not exists: " + file.getAbsolutePath());
                }
                return null;
            }
            if (_isOutputDebug) {
                log.info("file : " + file.getAbsolutePath() + ", " + file.length());
            }
            final Properties props = new Properties();
            FileReader r = null;
            try {
                r = new FileReader(file);
                props.load(r);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                if (null != r) {
                    try {
                        r.close();
                    } catch (Exception _ignored) {
                    }
                }
            }
            if (_isOutputDebug) {
                log.error(" loaded " + props.size() + ".");
                if (_isOutputDebugAll) {
                    for (final Map.Entry e : props.entrySet()) {
                        log.info(" prop " + e.getKey() + " = " + e.getValue());
                    }
                }
            }
            return props;
        }
    }
}