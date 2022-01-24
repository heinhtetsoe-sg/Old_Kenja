// kanji=漢字
/*
 * $Id: 2c7ccd0e7d58ae8f1de7a57ded9887955418c2a9 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE.detail;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;

/**
 *
 *  [進路情報・調査書]学習記録データSQL作成
 *
 *  2004/03/29 yamashiro・教科コード仕様の変更に伴う修正
 *  2005/07/08 yamashiro・評定読替科目を除外する記述を組み込む
 *  2005/07/25 yamashiro・在校生用と卒業生用のSQLを共通化するため使用テーブル名を変数に変更
 *  2006/04/14 yamashiro・教科コード設定をKNJ_ClassCodeからKNJDefineCodeへ変更  --NO001
 *                          => KNJ_ClassCodeの変数: subject_? -> definecode.subject_?
 *                      ・修得単位数の仕様変更  --NO001
 *                          修得単位数＝修得単位数＋増加単位数
 *                      ・東京都の仕様を挿入  --NO002
 *                          2005/10/24 yamasihro・東京都英語用コンストラクター追加
 *                      ・「評定読替科目を除外する」(2005/07/08 ) 処理は近大付属限定
 *  2008/03/06 nakamoto ・法政の場合、教科コード「87」は「90」と読み替えて「総合的な学習の時間」に単位を集計
 */

public class KNJ_StudyrecSql {

    private static final Log log = LogFactory.getLog(KNJ_StudyrecSql.class);
    
    private static final String  hyouteiDefault = "hyde";
    private static final String  atypeDefault = "hyde";
    private static final int     stypeDefault = 1;
    private static final boolean englishDefault = false;
    private static final boolean isHoseiDefault = false;
    private static final boolean isNotPrintMirishuDefault = false;
    
    private static String sogo = "'sogo'";
    private static String abroad = "'abroad'";
    private static String total = "'total'";
    private static String zenseki = "'zenseki'";
    private static String daiken = "'daiken'";
    private static String tokiwahr = "'tokiwahr'";
    private static String nishiyamahr = "'nishiyamahr'";
    
    public Map _config = new HashMap();
    public static String CONFIG_PRINT_GRD = "PRINT_GRD";
    /**
     * 2017年4月時点で、評定平均は使っていない。そもそも調査書以外の証明書で見たことない。処理消したい
     */
    public static String CONFIG_HYOUTEI = "HYOUTEI";
    public static String CONFIG_A_TYPE = "A_TYPE";
    public static String CONFIG_S_TYPE = "S_TYPE";
    public static String CONFIG_ENGLISH = "ENGLISH";
    public static String CONFIG_IS_HOSEI = "IS_HOSEI";// 法政 // 08/03/06Add
    public static String CONFIG_IS_NOT_PRINT_MIRISHU = "IS_NOT_PRINT_MIRISHU";
    public static String CONFIG_USE_CURRICULUM_CD = "USE_CURRICULUM_CD";
    public String tname1 = null;        //05/07/25 SCHREG_STUDYREC_DAT
    public String tname2 = null;        //05/07/25 SCHREG_TRANSFER_DAT
    public String tname3 = null;        //05/07/25 SCHREG_REGD_DAT
    private KNJDefineCode definecode = new KNJDefineCode(); //各学校における定数等設定 05/07/08 Build  NO001

    /**
     * @deprecated
     */
    public KNJ_StudyrecSql(final String hyoutei, final boolean english) {
        this(hyoutei, atypeDefault, stypeDefault, english, isHoseiDefault, isNotPrintMirishuDefault, null);
    }

    public KNJ_StudyrecSql(final String hyoutei, final boolean english, final String useCurriculumcd) {
        this(hyoutei, atypeDefault, stypeDefault, english, isHoseiDefault, isNotPrintMirishuDefault, useCurriculumcd);
    }

    /**
     * @deprecated
     */
    public KNJ_StudyrecSql(final String hyoutei, final String atype, final int stype) {
        this(hyoutei, atype, stype, englishDefault, isHoseiDefault, isNotPrintMirishuDefault, null);
    }

    public KNJ_StudyrecSql(final String hyoutei, final String atype, final int stype, final String useCurriculumcd) {
        this(hyoutei, atype, stype, englishDefault, isHoseiDefault, isNotPrintMirishuDefault, useCurriculumcd);
    }

    /**
     * @deprecated
     */
    public KNJ_StudyrecSql(final String hyoutei, final String atype, final int stype, final boolean english, final boolean isHosei, final boolean isNotPrintMirishu) {
        this(hyoutei, atype, stype, english, isHosei, isNotPrintMirishu, null);
    }

    /**
     * コンストラクタ。
     * @param hyoutei 評定の読替え  １を２と評定 on:評定１の読替有 off:評定１の読替無 hyde:出力無 grade:学年別評定有
     * @param atype 特Ａ付き on:特Ａ出力有 hyde:出力無
     * @param stype 総合的な学習の時間、留学単位、修得単位の集計区分 1:合計のみ 2:合計&学年別 3:学年別のみ 0:出力無
     * @param english on:英語の出力
     * @param isHosei 法政の時、true
     * @param isNotPrintMirishu trueなら未履修科目を表示しない
     * @param useCurriculumcd 教育課程コードを使用する
     */
    public KNJ_StudyrecSql(
            final String hyoutei,
            final String atype,
            final int stype,
            final boolean english,
            final boolean isHosei,
            final boolean isNotPrintMirishu,
            final String useCurriculumcd) {
        _config.put(CONFIG_HYOUTEI, hyoutei);
        _config.put(CONFIG_A_TYPE, atype);
        _config.put(CONFIG_S_TYPE, new Integer(stype));
        _config.put(CONFIG_ENGLISH, english ? "1" : null);
        _config.put(CONFIG_IS_HOSEI, isHosei ? "1" : null);
        _config.put(CONFIG_IS_NOT_PRINT_MIRISHU, isNotPrintMirishu ? "1" : null);
        _config.put(CONFIG_USE_CURRICULUM_CD, useCurriculumcd);
        log.debug(" $Revision: 63513 $ $Date: 2018-11-21 15:15:16 +0900 (水, 21 11 2018) $ ");
        log.debug(" config = " + _config);
    }
    
    private static String hyoutei(final Map config) {
        return getString(config, CONFIG_HYOUTEI);
    }
    
    private static String atype(final Map config) {
        return getString(config, CONFIG_A_TYPE);
    }
    
    private static int stype(final Map config) {
        return ((Integer) config.get(CONFIG_S_TYPE)).intValue();
    }
    
    private static boolean english(final Map config) {
        return "1".equals(config.get(CONFIG_ENGLISH));
    }
    
    private static boolean isHosei(final Map config) {
        return "1".equals(config.get(CONFIG_IS_HOSEI));
    }
    
    private static boolean isNotPrintMirishu(final Map config) {
        return "1".equals(config.get(CONFIG_IS_NOT_PRINT_MIRISHU));
    }
    
    private static String useCurriculumcd(final Map config) {
        return getString(config, CONFIG_USE_CURRICULUM_CD);
    }

    /**
     * @deprecated
     */
    public String pre_sql_new(final String schooldiv, final String zensekiSubclassCd, final boolean daiken_div_code, final String notUseClassMstSpecialDiv) {
        final Map paramMap = new HashMap();
        paramMap.put("schooldiv", schooldiv);
        paramMap.put("zensekiSubclassCd", zensekiSubclassCd);
        paramMap.put("daiken_div_code", String.valueOf(daiken_div_code));
        paramMap.put("notUseClassMstSpecialDiv", notUseClassMstSpecialDiv);
        paramMap.put("notUseStudyrecProvFlgDat", String.valueOf(true));
        return pre_sql_new(paramMap);
    }
    
    private static String getString(final Map paramMap, final String name) {
        return (String) paramMap.get(name);
    }
    
    private static String getString(final Map paramMap, final String name, final String def) {
        return null == getString(paramMap, name) ? def : getString(paramMap, name);
    }
    
    private static boolean asBoolean(final Map paramMap, final String name, final boolean def) {
        return Boolean.valueOf(getString(paramMap, name, String.valueOf(def))).booleanValue();
    }
    
    /**
    *
    *  学習記録のSQL
    *  2005/07/08 Modify 評定読替元の科目を対象としない処理を追加
    *
    */
   public String pre_sql_new(final Map paramMap) {

       if (tname1 == null) {
           setFieldName();   //使用テーブル名設定 05/07/25Build
       }
       
       final String schregno = null != paramMap.get("SCHREGNO") ? ("'" + (String) paramMap.get("SCHREGNO") + "'") : "?";
       final String paramYear = getString(paramMap, "YEAR");
       final String schooldiv = getString(paramMap, "schooldiv");
       final String zensekiSubclassCd = getString(paramMap, "zensekiSubclassCd");
       final boolean daiken_div_code = Boolean.valueOf(getString(paramMap, "daiken_div_code", "false")).booleanValue();
       final String _useCurriculumcd = useCurriculumcd(_config);
       final String notUseClassMstSpecialDiv = getString(paramMap, "notUseClassMstSpecialDiv");
       final boolean notUseStudyrecProvFlgDat = null != paramMap.get("notUseStudyrecProvFlgDat");
       final boolean notPrintAnotherStudyrec = null != paramMap.get("notPrintAnotherStudyrec");
       final String _hyoutei = hyoutei(_config);
       final String _atype = atype(_config);
       final int _stype = stype(_config);
       final boolean _english = english(_config);
       final boolean isEnglish = null != paramMap.get("isEnglish");
       final boolean withSubclassnameJp = null != paramMap.get("WithSubclassnameJp");
       final String excludeGradeSchoolKind = (String) paramMap.get("excludeGradeSchoolKind");

       final String z010Name1 = getString(paramMap, "z010Name1");
       final boolean isTokiwa = Boolean.valueOf(getString(paramMap, "isTokiwa", "false")).booleanValue();
       final boolean isNishiyama = "nishiyama".equals(z010Name1);
       final boolean isKyoto = "kyoto".equals(z010Name1);
       final List ryunenYearList = (List) paramMap.get("ryunenYearList");
       final StringBuffer ryunenYearSqlNotIn = new StringBuffer();
       if (null != ryunenYearList && !ryunenYearList.isEmpty()) {
           ryunenYearSqlNotIn.append(" NOT IN (");
           String comma = "";
           for (final Iterator it = ryunenYearList.iterator(); it.hasNext();) {
               final String year = (String) it.next();
               ryunenYearSqlNotIn.append(comma).append("'").append(year).append("'");
               comma = ", ";
           }
           ryunenYearSqlNotIn.append(" )");
       }
       final boolean isOutputDebug = "1".equals(paramMap.get("outputDebug"));
       if (isOutputDebug) {
           log.info("paramMap = " + paramMap);
       }

       String lastLineClasscd = getString(paramMap, "lastLineClasscd"); // LHR等、最後の行に表示する教科のコード
       boolean isSubclassContainLastLineClass = false; //表示する行を取得
       boolean isHyoteiHeikinLastLineClass = false; // lastLineClasscd教科に評定を入力し評定平均を表示する
       boolean isTotalContainLastLineClass = false;  // 'total'にlastLineClasscd教科を含める
       if (null != lastLineClasscd) {
           isSubclassContainLastLineClass = asBoolean(paramMap, "isSubclassContainLastLineClass", true);
           isHyoteiHeikinLastLineClass = asBoolean(paramMap, "isHyoteiHeikinLastLineClass", true);
           isTotalContainLastLineClass = asBoolean(paramMap, "isTotalContainLastLineClass", true);
       } else if (isTokiwa) {
           lastLineClasscd = "94";
           isSubclassContainLastLineClass = false;
           isHyoteiHeikinLastLineClass = false;
           isTotalContainLastLineClass = false;
       } else if (isNishiyama) {
           lastLineClasscd = "94";
           isSubclassContainLastLineClass = true;
           isHyoteiHeikinLastLineClass = true;
           isTotalContainLastLineClass = true;
       }
       
       final boolean isPrintChairSubclass = Boolean.valueOf(getString(paramMap, "isPrintChairSubclass", "false")).booleanValue();
       final String notContainTotalYears = getString(paramMap, "notContainTotalYears", "('99999999')");
       final String yearAsInteger;
       final String year;
       if (null == paramYear) {
           yearAsInteger = "?";
           year = "?";
       } else {
           yearAsInteger = isPrintChairSubclass ? String.valueOf(Integer.parseInt(paramYear) - 1)  : paramYear;
           year = "'" + yearAsInteger + "'";
       }
       final boolean useD065 = null != getString(paramMap, "useD065");
       final String schoolMstSchoolKind = getString(paramMap, "schoolMstSchoolKind");
       
       final StringBuffer sql = new StringBuffer();

       //  評定１を２と判定
       String h_1_2 = null;
       String h_1_3 = null;
//       if( _hyoutei.equals("on") ){ //----->評定読み替えのON/OFF  評定１を２と読み替え
//           h_1_2 = "CASE VALUE(T1.GRADES,0) WHEN 1 THEN 2 ELSE T1.GRADES END ";
//           h_1_3 = "T1.CREDIT ";  //NO001
//           //NO001 h_1_3 = "CASE WHEN VALUE(T1.GRADES,0)=1 AND VALUE(T1.CREDIT,0)=0 THEN T1.ADD_CREDIT ELSE T1.CREDIT END ";
//       } else{
           h_1_2 = "T1.GRADES ";
           h_1_3 = "T1.CREDIT ";
//       }

       //該当生徒の成績データ表
       final boolean isKindai = 3 <= definecode.schoolmark.length()  &&  definecode.schoolmark.equals("KIN");
       if (isKindai) {  //--NO001
           //近大付属は評価読替元科目はココで除外する  05/07/08
           sql.append("WITH STUDYREC AS(");
           sql.append("SELECT  T1.CLASSNAME, ");
           sql.append("        T1.SUBCLASSNAME, ");
           sql.append("        T1.SCHREGNO, ");
           sql.append("        T1.YEAR, ");
           sql.append("        T1.ANNUAL, ");
           sql.append("        T1.CLASSCD, ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        T1.SCHOOL_KIND, ");
               sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASS_SCHK, ");
               sql.append("        T1.CURRICULUM_CD, ");
           }
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS RAW_SUBCLASSCD, ");
               sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
           }
           sql.append("        VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, ");
           sql.append("        T1.VALUATION AS GRADES ");
           sql.append("       ,CASE WHEN T1.ADD_CREDIT IS NOT NULL OR T1.GET_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) END AS CREDIT, ");
           if (useD065) {
               sql.append("        NMD065.NAME1 AS D065FLG ");
           } else {
               sql.append("        CAST(NULL AS VARCHAR(1)) AS D065FLG ");
           }
           sql.append("FROM   " + tname1 + " T1 ");
           sql.append("        LEFT JOIN SUBCLASS_MST L2 ON ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
           }
           sql.append("                L2.SUBCLASSCD = ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
           }
           sql.append("               T1.SUBCLASSCD ");
           if (notUseStudyrecProvFlgDat) {
           } else {
               sql.append("        LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ON L3.SCHOOLCD = T1.SCHOOLCD ");
               sql.append("            AND L3.YEAR = T1.YEAR ");
               sql.append("            AND L3.SCHREGNO = T1.SCHREGNO ");
               sql.append("            AND L3.CLASSCD = T1.CLASSCD ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                   sql.append("            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
               }
               sql.append("            AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
               sql.append("            AND L3.PROV_FLG = '1' ");
           }
           if (useD065) {
               sql.append("   LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 = ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
               }
               sql.append("       T1.SUBCLASSCD ");
           }
           sql.append("WHERE   T1.SCHREGNO = " + schregno + " AND ");
           sql.append("        T1.YEAR <= " + year + " AND ");
           sql.append("        (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "') AND ");
           sql.append("        NOT EXISTS(SELECT  'X' ");
           sql.append("                   FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
           sql.append("                   WHERE   T2.YEAR = T1.YEAR AND ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
           }
           sql.append("                           T2.ATTEND_SUBCLASSCD = ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
           }
           sql.append("                           T1.SUBCLASSCD) ");
           if (notUseStudyrecProvFlgDat) {
           } else {
               sql.append("         AND L3.SUBCLASSCD IS NULL ");
           }
           if (notPrintAnotherStudyrec) {
               sql.append("         AND T1.SCHOOLCD <> '1' ");
           }
           sql.append("    )");
       } else {
           sql.append("WITH T_STUDYREC AS(");
           sql.append("SELECT  T1.SCHOOLCD, ");
           sql.append("        T1.CLASSNAME, ");
           sql.append("        T1.SUBCLASSNAME, ");
           sql.append("        T1.SCHREGNO, ");
           sql.append("        T1.YEAR, ");
           sql.append("        T1.ANNUAL, ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        T1.SCHOOL_KIND, ");
               sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASS_SCHK, ");
               sql.append("        T1.CURRICULUM_CD, ");
           }
           sql.append("        VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS RAW_SUBCLASSCD, ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
           }
           sql.append("        T1.SUBCLASSCD AS SUBCLASSCD, ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        L2.SUBCLASSCD2 AS RAW_SUBCLASSCD2, ");
               sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
           }
           sql.append("        L2.SUBCLASSCD2 AS SUBCLASSCD2, ");
           sql.append("        T1.VALUATION AS GRADES ");
           if (isHosei(_config)) {
               sql.append("   ,CASE WHEN T1.CLASSCD = '87' THEN '90' ELSE T1.CLASSCD END AS CLASSCD ");
           } else {
               sql.append("   ,T1.CLASSCD ");
           }
           sql.append("       ,CASE WHEN T1.ADD_CREDIT IS NOT NULL OR T1.GET_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) END AS CREDIT ");
           if (useD065) {
               sql.append("   ,NMD065.NAME1 AS D065FLG ");
           } else {
               sql.append("   ,CAST(NULL AS VARCHAR(1)) AS D065FLG ");
           }
           sql.append("FROM   " + tname1 + " T1 ");
           sql.append("        LEFT JOIN SUBCLASS_MST L2 ON ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
           }
           sql.append("            L2.SUBCLASSCD=");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
           }
           sql.append("            T1.SUBCLASSCD ");
           if (notUseStudyrecProvFlgDat) {
           } else {
               sql.append("        LEFT JOIN STUDYREC_PROV_FLG_DAT L3 ON L3.SCHOOLCD = T1.SCHOOLCD ");
               sql.append("            AND L3.YEAR = T1.YEAR ");
               sql.append("            AND L3.SCHREGNO = T1.SCHREGNO ");
               sql.append("            AND L3.CLASSCD = T1.CLASSCD ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("            AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                   sql.append("            AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
               }
               sql.append("            AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
               sql.append("            AND L3.PROV_FLG = '1' ");
           }
           if (useD065) {
               sql.append("   LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 = T1.SUBCLASSCD ");
           }
           sql.append("WHERE   T1.SCHREGNO = " + schregno + " AND T1.YEAR <= " + year + " AND ");
           sql.append("        (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "' ");
           if (null != lastLineClasscd) {
               sql.append("     OR T1.CLASSCD = '" + lastLineClasscd + "' "); // 特別活動 ホームルーム
           }
           sql.append("        ) ");
           if (ryunenYearSqlNotIn.length() > 0) {
               sql.append("    AND T1.YEAR " + ryunenYearSqlNotIn);
           }
           if (isNotPrintMirishu(_config)) {
               sql.append("        AND VALUE(T1.COMP_CREDIT, 0) <> 0 ");
           }
           if (notUseStudyrecProvFlgDat) {
           } else {
               sql.append("         AND L3.SUBCLASSCD IS NULL ");
           }
           if (notPrintAnotherStudyrec) {
               sql.append("         AND T1.SCHOOLCD <> '1' ");
           }
           if (null != excludeGradeSchoolKind) {
        	   // 指定校種以外の学年を対象外とする
        	   sql.append(" AND T1.ANNUAL NOT IN (SELECT DISTINCT GRADE FROM SCHREG_REGD_GDAT WHERE SCHOOL_KIND <> '" + excludeGradeSchoolKind + "') ");
           }
           sql.append(") , T_STUDYREC2 AS( ");
           sql.append("    SELECT ");
           sql.append("        T1.* ");
           sql.append("    FROM ");
           sql.append("        T_STUDYREC T1 ");
           if (schooldiv.equals("1")) {
               if (daiken_div_code) {
                   sql.append(" WHERE  T1.SCHOOLCD = '0'");
               } else {
                   sql.append(" WHERE  T1.SCHOOLCD = '0'");
                   sql.append("     OR (T1.SCHOOLCD = '2' AND T1.CREDIT IS NOT NULL)");
               }
               if (null != zensekiSubclassCd) {
                   sql.append("     OR ((T1.SCHOOLCD = '1' OR T1.YEAR = '0') ");
                   if ("1".equals(_useCurriculumcd)) {
                       sql.append("         AND T1.RAW_SUBCLASSCD <> '" + zensekiSubclassCd + "')");
                   } else {
                       sql.append("         AND T1.SUBCLASSCD <> '" + zensekiSubclassCd + "')");
                   }
               } else {
                   sql.append("     OR (T1.SCHOOLCD = '1' OR T1.YEAR = '0')");
               }
           }
           // 同一年度同一科目の場合単位は合計とします。
           //「0:平均」「1:重み付け」は「評定がNULL／ゼロ以外」
           final String gradesCase = "case when 0 < T1.GRADES then GRADES end";
           final String creditCase = "case when 0 < T1.GRADES then CREDIT end";
           
           sql.append(") , STUDYREC0 AS( ");
           sql.append("    SELECT ");
           sql.append("        T1.SCHOOLCD, ");
           sql.append("        T1.CLASSNAME, ");
           sql.append("        T1.SUBCLASSNAME, ");
           sql.append("        T1.SCHREGNO, ");
           sql.append("        T1.YEAR, ");
           sql.append("        T1.ANNUAL, ");
           sql.append("        T1.CLASSCD , ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        T1.SCHOOL_KIND, ");
               sql.append("        T1.CLASS_SCHK, ");
               sql.append("        T1.CURRICULUM_CD, ");
           }
           sql.append("        T1.RAW_SUBCLASSCD, ");
           sql.append("        T1.SUBCLASSCD, ");
           sql.append("        T1.GRADES, ");
           sql.append("        T1.CREDIT, ");
           sql.append("        T1.D065FLG ");
           sql.append("    FROM ");
           sql.append("        T_STUDYREC2 T1 ");
           sql.append("    WHERE ");
           sql.append("        T1.SUBCLASSCD2 IS NULL ");
           sql.append("    UNION ALL ");
           sql.append("    SELECT ");
           sql.append("        T1.SCHOOLCD, ");
           sql.append("        T1.CLASSNAME, ");
           sql.append("        T1.SUBCLASSNAME, ");
           sql.append("        T1.SCHREGNO, ");
           sql.append("        T1.YEAR, ");
           sql.append("        T1.ANNUAL, ");
           sql.append("        T1.CLASSCD , ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        T1.SCHOOL_KIND, ");
               sql.append("        T1.CLASS_SCHK, ");
               sql.append("        T1.CURRICULUM_CD, ");
           }
           sql.append("        T1.RAW_SUBCLASSCD, ");
           sql.append("        T1.SUBCLASSCD2 AS SUBCLASSCD, ");
           sql.append("        T1.GRADES, ");
           sql.append("        T1.CREDIT, ");
           sql.append("        T1.D065FLG ");
           sql.append("    FROM ");
           sql.append("        T_STUDYREC2 T1 ");
           sql.append("    WHERE ");
           sql.append("        T1.SUBCLASSCD2 IS NOT NULL ");
           
           sql.append(") , STUDYREC AS( ");
           sql.append("    SELECT ");
           sql.append("        MIN(T1.SCHOOLCD) AS SCHOOLCD, ");
           sql.append("        MAX(T1.CLASSNAME) AS CLASSNAME, ");
           sql.append("        MAX(T1.SUBCLASSNAME) AS SUBCLASSNAME, ");
           sql.append("        T1.SCHREGNO, ");
           sql.append("        T1.YEAR, ");
           sql.append("        MAX(T1.ANNUAL) AS ANNUAL, ");
           sql.append("        T1.CLASSCD, ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        T1.SCHOOL_KIND, ");
               sql.append("        MAX(T1.CLASS_SCHK) AS CLASS_SCHK, ");
               sql.append("        T1.CURRICULUM_CD, ");
           }
           sql.append("        T1.RAW_SUBCLASSCD, ");
           sql.append("        T1.SUBCLASSCD AS SUBCLASSCD, ");
           sql.append("       case when COUNT(*) = 1 then MAX(T1.GRADES) ");//１レコードの場合、評定はそのままの値。
           sql.append("            when SC.GVAL_CALC = '0' then ROUND(AVG(FLOAT("+gradesCase+")),0)");
           sql.append("            when SC.GVAL_CALC = '1' and 0 < SUM("+creditCase+") then ROUND(FLOAT(SUM(("+gradesCase+") * T1.CREDIT)) / SUM("+creditCase+"),0)");
           sql.append("            else MAX(T1.GRADES) end AS GRADES,");
           sql.append("        SUM(T1.CREDIT) AS CREDIT, ");
           sql.append("        MAX(D065FLG) AS D065FLG ");
           sql.append("    FROM ");
           sql.append("        STUDYREC0 T1 ");
           sql.append("        LEFT JOIN SCHOOL_MST SC ON SC.YEAR = T1.YEAR ");
           if (null != schoolMstSchoolKind) {
               sql.append("        AND SC.SCHOOL_KIND = '" + schoolMstSchoolKind + "' ");
           }
           if (useD065) {
               sql.append("   LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 = T1.SUBCLASSCD ");
           }
           sql.append("    GROUP BY ");
           sql.append("        T1.SCHREGNO, ");
           sql.append("        T1.YEAR, ");
           sql.append("        T1.CLASSCD, ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        T1.SCHOOL_KIND, ");
               sql.append("        T1.CURRICULUM_CD, ");
           }
           sql.append("        T1.RAW_SUBCLASSCD, ");
           sql.append("        T1.SUBCLASSCD, ");
           sql.append("       SC.GVAL_CALC ");
           sql.append(") ");
       }
       
       if ("0".equals(schooldiv)) {
           sql.append(" , DROP_YEAR AS(");
           sql.append("        SELECT DISTINCT YEAR");
           sql.append("        FROM SCHREG_REGD_DAT T1");
           sql.append("        WHERE SCHREGNO IN (SELECT DISTINCT SCHREGNO FROM STUDYREC) ");
           sql.append("        AND T1.YEAR NOT IN (SELECT MAX(YEAR) FROM SCHREG_REGD_DAT T2 WHERE SCHREGNO IN (SELECT DISTINCT SCHREGNO FROM STUDYREC) AND YEAR <= (SELECT MAX(YEAR) FROM STUDYREC) GROUP BY GRADE)");
           sql.append(" ) ");
       }

       final String groupByColumn = "1".equals(schooldiv) ? " YEAR " : " ANNUAL ";
       sql.append(", MAIN AS ( ");
       //該当生徒の科目評定、修得単位及び教科評定平均
       sql.append(" SELECT ");
       sql.append("     T2.SHOWORDER2 as CLASS_ORDER,");
       sql.append("     T3.SHOWORDER2 as SUBCLASS_ORDER,");
       sql.append("     T1.YEAR,");
       sql.append("     T1." + groupByColumn + " AS ANNUAL,");
       if ("1".equals(_useCurriculumcd)) {
           sql.append("        T1.SCHOOL_KIND, ");
           sql.append("        T1.CLASS_SCHK, ");
           sql.append("        T1.CURRICULUM_CD, ");
       }
       sql.append("     T1.CLASSCD,");
       if (_english || isEnglish) {                     //----->教科名 英語/日本語
           sql.append(" T2.CLASSNAME_ENG AS CLASSNAME,");
           if (withSubclassnameJp) {
               sql.append(" VALUE(T1.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME) AS CLASSNAME_JP,");
           }
       } else {
           sql.append(" VALUE(T1.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME) AS CLASSNAME,");
       }
       sql.append("     T1.SUBCLASSCD,");
       sql.append("     T1.RAW_SUBCLASSCD, ");
       if (_english || isEnglish) {                     //----->科目名 英語/日本語
           sql.append(" T3.SUBCLASSNAME_ENG AS SUBCLASSNAME,");
           if (withSubclassnameJp) {
               sql.append(" VALUE(T1.SUBCLASSNAME, T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME_JP,");
           }
       } else {
           sql.append(" VALUE(T1.SUBCLASSNAME, T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME,");
       }
       if (hyouteiDefault.equals(_hyoutei)) {        //----->評定の出力有無
           sql.append(" 0 AS GRADES,");
           sql.append(" 0 AS AVG_GRADES,");
           sql.append(" '' AS ASSESS_LEVEL,");
       } else {
           sql.append( h_1_2 + " AS GRADES,");
           sql.append(" T5.AVG_GRADES,");
           sql.append(" '' AS ASSESS_LEVEL,");
       }
       sql.append("     T1.CREDIT AS GRADE_CREDIT,");
       sql.append("     T4.CREDIT, ");
       sql.append("     T1.D065FLG ");
       sql.append(" FROM ");
       sql.append("     STUDYREC T1 ");
       sql.append("     LEFT JOIN CLASS_MST T2 ON ");
       if ("1".equals(_useCurriculumcd)) {
           sql.append("     T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
       } else {
           sql.append("     T2.CLASSCD = T1.CLASSCD ");
       }
       sql.append("     LEFT JOIN SUBCLASS_MST T3 ON ");
       if ("1".equals(_useCurriculumcd)) {
           sql.append("        T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
       }
       sql.append("         T3.SUBCLASSCD = T1.SUBCLASSCD ");
       //  修得単位数の計
       sql.append("     LEFT JOIN(SELECT ");
       sql.append("             CLASSCD,SUBCLASSCD,SUM(" + h_1_3 + ") AS CREDIT ");
       sql.append("         FROM ");
       sql.append("             STUDYREC T1 ");
       sql.append("         WHERE ");
       sql.append("             (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
       if (null != lastLineClasscd && isSubclassContainLastLineClass) {
           sql.append("           OR T1.CLASSCD = '" + lastLineClasscd + "'");
       }
       sql.append("             )");
       sql.append("             AND YEAR NOT IN " + notContainTotalYears);
       if ("0".equals(schooldiv)) {
           sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
       }
       sql.append("         GROUP BY ");
       sql.append("             CLASSCD,SUBCLASSCD ");
       sql.append("     )T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
       if (!(hyouteiDefault.equals(_hyoutei))) {        //----->評定の出力有無
           //  各教科の評定平均値
           sql.append(" LEFT JOIN(SELECT ");
           sql.append("         CLASSCD,");
           sql.append("         DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1),5,1) AS AVG_GRADES ");
           sql.append("     FROM ");
           sql.append("         STUDYREC T1 ");
           sql.append("     WHERE ");
           sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
           if (null != lastLineClasscd && isHyoteiHeikinLastLineClass) {
               sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
           }
           sql.append("      )");
           sql.append("      AND T1.D065FLG IS NULL ");
           sql.append("     GROUP BY ");
           sql.append("         CLASSCD ");
           sql.append(" )T5 ON T5.CLASSCD = T1.CLASSCD ");
       }
       sql.append(" WHERE ");
       sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
       if (null != lastLineClasscd && isHyoteiHeikinLastLineClass) {
           sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
       }
       sql.append("      )");

       //  総合学習の修得単位数（学年別）
       if (_stype > 1) {          //----->学年別
           sql.append("UNION SELECT ");
           sql.append("     cast(null as smallint) as CLASS_ORDER,");
           sql.append("     cast(null as smallint) as SUBCLASS_ORDER,");
           sql.append("     cast(null as varchar(4)) as YEAR,");
           sql.append("     " + groupByColumn + " AS ANNUAL,");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("        CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ");
               sql.append("        MAX(CLASS_SCHK) AS CLASS_SCHK, ");
               sql.append("        CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ");
           }
           sql.append("     '" + KNJDefineCode.subject_T + "' AS CLASSCD,");
           sql.append("     " + sogo + " AS CLASSNAME,");
           if (withSubclassnameJp) {
               sql.append("     " + sogo + " AS CLASSNAME_JP,");
           }
           sql.append("     '" + KNJDefineCode.subject_T + "01' AS SUBCLASSCD,");
           sql.append("     '" + KNJDefineCode.subject_T + "01' AS RAW_SUBCLASSCD,");
           sql.append("     " + sogo + " AS SUBCLASSNAME,");
           if (withSubclassnameJp) {
               sql.append("     " + sogo + " AS SUBCLASSNAME_JP,");
           }
           sql.append("     0 AS GRADES,");
           sql.append("     0 AS AVG_GRADES,");
           sql.append("     '' AS ASSESS_LEVEL,");
           sql.append("     0 AS GRADE_CREDIT,");
           sql.append("     SUM(CREDIT) AS CREDIT, ");
           sql.append("     MAX(D065FLG) AS D065FLG ");
           sql.append(" FROM ");
           if (isKindai) {
               sql.append("     STUDYREC ");
           } else {
               sql.append("     T_STUDYREC ");
           }
           sql.append(" WHERE ");
           sql.append("     CLASSCD = '" + KNJDefineCode.subject_T + "' ");
           sql.append("     AND YEAR NOT IN " + notContainTotalYears);
           if ("0".equals(schooldiv)) {
               sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
           }
           sql.append(" GROUP BY " + groupByColumn);
       }
       //  総合学習の修得単位数（合計）
       sql.append(" UNION SELECT ");
       sql.append("     cast(null as smallint) as CLASS_ORDER,");
       sql.append("     cast(null as smallint) as SUBCLASS_ORDER,");
       sql.append("     cast(null as varchar(4)) as YEAR,");
       sql.append("         '000' AS ANNUAL,");
       if ("1".equals(_useCurriculumcd)) {
           sql.append("         '" + KNJDefineCode.subject_T + "' AS SCHOOL_KIND, ");
           sql.append("         '" + KNJDefineCode.subject_T + "' AS CLASS_SCHK, ");
           sql.append("         '" + KNJDefineCode.subject_T + "' AS CURRICULUM_CD, ");
       }
       sql.append("         '" + KNJDefineCode.subject_T + "' AS CLASSCD,");
       sql.append("         " + sogo + " AS CLASSNAME,");
       if (withSubclassnameJp) {
           sql.append("     " + sogo + " AS CLASSNAME_JP,");
       }
       sql.append("         '" + KNJDefineCode.subject_T + "01' AS SUBCLASSCD,");
       sql.append("         '" + KNJDefineCode.subject_T + "01' AS RAW_SUBCLASSCD,");
       sql.append("         " + sogo + " AS SUBCLASSNAME,");
       if (withSubclassnameJp) {
           sql.append("     " + sogo + " AS SUBCLASSNAME_JP,");
       }
       sql.append("         0 AS GRADES, ");
       sql.append("         0 AS AVG_GRADES, ");
       sql.append("         '' AS ASSESS_LEVEL,");
       sql.append("         0 AS GRADE_CREDIT,");
       sql.append("         SUM(CREDIT) AS CREDIT, ");
       sql.append("         MAX(D065FLG) AS D065FLG ");
       sql.append("     FROM ");
       if (isKindai) {
           sql.append("     STUDYREC ");
       } else {
           sql.append("     T_STUDYREC ");
       }
       sql.append("     WHERE ");
       sql.append("         CLASSCD = '" + KNJDefineCode.subject_T + "' ");
       sql.append("         AND YEAR NOT IN " + notContainTotalYears);
       if ("0".equals(schooldiv)) {
           sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
       }

       //  留学中の修得単位数（学年別）
       if (_stype > 1) {              //----->学年別/合計
           sql.append(" UNION SELECT ");
           sql.append("     cast(null as smallint) as CLASS_ORDER,");
           sql.append("     cast(null as smallint) as SUBCLASS_ORDER,");
           sql.append("     cast(null as varchar(4)) as YEAR,");
           sql.append("     " + groupByColumn + " AS ANNUAL,");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("         'AA' AS SCHOOL_KIND, ");
               sql.append("         'AA' AS CLASS_SCHK, ");
               sql.append("         'AA' AS CURRICULUM_CD, ");
           }
           sql.append("     'AA' AS CLASSCD,");
           sql.append("     " + abroad + " AS CLASSNAME,");
           if (withSubclassnameJp) {
               sql.append("     " + abroad + " AS CLASSNAME_JP,");
           }
           sql.append("     'AAAA' AS SUBCLASSCD,");
           sql.append("     'AAAA' AS RAW_SUBCLASSCD,");
           sql.append("     " + abroad + " AS SUBCLASSNAME,");
           if (withSubclassnameJp) {
               sql.append("     " + abroad + " AS SUBCLASSNAME_JP,");
           }
           sql.append("     0 AS GRADES,");
           sql.append("     0 AS AVG_GRADES,");
           sql.append("     '' AS ASSESS_LEVEL,");
           sql.append("     0 AS GRADE_CREDIT,");
           sql.append("    SUM(ABROAD_CREDITS) AS CREDIT, ");
           sql.append("    CAST(NULL AS VARCHAR(1)) AS D065FLG ");
           sql.append(" FROM ");
           sql.append("         (SELECT ");
           sql.append("             ABROAD_CREDITS,");
           sql.append("             INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
           sql.append("         FROM ");
           sql.append(              tname2 + " ");
           sql.append("         WHERE ");
           sql.append("             SCHREGNO = " + schregno + " AND TRANSFERCD = '1' ");
           sql.append("             AND FISCALYEAR(TRANSFER_SDATE) NOT IN " + notContainTotalYears);
           if ("0".equals(schooldiv)) {
               sql.append("     AND FISCALYEAR(TRANSFER_SDATE) NOT IN (SELECT YEAR FROM DROP_YEAR) ");
           }
           sql.append("         )ST1,");
           sql.append("         (SELECT ");
           if ("1".equals(schooldiv)) {
               sql.append("         YEAR ");
           } else {
               sql.append("         ANNUAL,MAX(YEAR) AS YEAR ");
           }
           sql.append("         FROM ");
           sql.append(              tname3 + " ");
           sql.append("         WHERE ");
           sql.append("             SCHREGNO = " + schregno + " AND YEAR <= " + year + " ");
           sql.append(" GROUP BY " + groupByColumn);
           sql.append("         )ST2 ");
           sql.append(" WHERE ");
           sql.append("     ST1.TRANSFER_YEAR <= " + yearAsInteger + " ");
           sql.append("     and INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
           sql.append(" GROUP BY " + groupByColumn);
       }
       //  留学中の修得単位数（合計）
       sql.append("     UNION SELECT ");
       sql.append("     cast(null as smallint) as CLASS_ORDER,");
       sql.append("     cast(null as smallint) as SUBCLASS_ORDER,");
       sql.append("     cast(null as varchar(4)) as YEAR,");
       sql.append("         '000' AS ANNUAL,");
       if ("1".equals(_useCurriculumcd)) {
           sql.append("         'AA' AS SCHOOL_KIND, ");
           sql.append("         'AA' AS CLASS_SCHK, ");
           sql.append("         'AA' AS CURRICULUM_CD, ");
       }
       sql.append("         'AA' AS CLASSCD,");
       sql.append("         " + abroad + " AS CLASSNAME,");
       if (withSubclassnameJp) {
           sql.append("     " + abroad + " AS CLASSNAME_JP,");
       }
       sql.append("         'AAAA' AS SUBCLASSCD,");
       sql.append("         'AAAA' AS RAW_SUBCLASSCD,");
       sql.append("         " + abroad + " AS SUBCLASSNAME,");
       if (withSubclassnameJp) {
           sql.append("     " + abroad + " AS SUBCLASSNAME_JP,");
       }
       sql.append("         0 AS GRADES, ");
       sql.append("         0 AS AVG_GRADES, ");
       sql.append("          '' AS ASSESS_LEVEL,");
       sql.append("         0 AS GRADE_CREDIT,");
       sql.append("         SUM(ABROAD_CREDITS) AS CREDIT, ");
       sql.append("         CAST(NULL AS VARCHAR(1)) AS D065FLG ");
       sql.append("     FROM ");
       sql.append("         (SELECT ");
       sql.append("             SCHREGNO,ABROAD_CREDITS,INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
       sql.append("         FROM  ");
       sql.append(              tname2 + " ");
       sql.append("         WHERE  ");
       sql.append("             SCHREGNO =" + schregno + " AND TRANSFERCD = '1' ");
       sql.append("             AND FISCALYEAR(TRANSFER_SDATE) NOT IN " + notContainTotalYears);
       if ("0".equals(schooldiv)) {
           sql.append("     AND FISCALYEAR(TRANSFER_SDATE) NOT IN (SELECT YEAR FROM DROP_YEAR) ");
       }
       sql.append("         )ST1 ");
       sql.append("     WHERE ");
       sql.append("         TRANSFER_YEAR <= " + yearAsInteger + " ");
       
       if (isTokiwa) {
           // 常盤ホームルーム(教科コード94)
           sql.append(" UNION SELECT ");
           sql.append("     cast(null as smallint) as CLASS_ORDER,");
           sql.append("     cast(null as smallint) as SUBCLASS_ORDER,");
           sql.append("     cast(null as varchar(4)) as YEAR,");
           sql.append("     " + groupByColumn + " AS ANNUAL,");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("         'ZZ' AS SCHOOL_KIND, ");
               sql.append("         'ZZ' AS CLASS_SCHK, ");
               sql.append("         'ZZ' AS CURRICULUM_CD, ");
           }
           sql.append("     " + tokiwahr + " AS CLASSCD, ");
           sql.append("     " + tokiwahr + " AS CLASSNAME,");
           if (withSubclassnameJp) {
               sql.append("     " + tokiwahr + " AS CLASSNAME_JP,");
           }
           sql.append("     " + tokiwahr + " AS SUBCLASSCD, ");
           sql.append("     " + tokiwahr + " AS RAW_SUBCLASSCD, ");
           sql.append("     " + tokiwahr + " AS SUBCLASSNAME,");
           if (withSubclassnameJp) {
               sql.append("     " + tokiwahr + " AS SUBCLASSNAME_JP,");
           }
           sql.append("     0 AS GRADES,");
           sql.append("     0 AS AVG_GRADES,");
           sql.append("     '' AS ASSESS_LEVEL,");
           sql.append("     0 AS GRADE_CREDIT,");
           sql.append("     SUM(T1.CREDIT) AS CREDIT, ");
           sql.append("     CAST(NULL AS VARCHAR(1)) AS D065FLG ");
           sql.append(" FROM ");
           sql.append("     T_STUDYREC T1 ");
           sql.append(" WHERE ");
           sql.append("     CLASSCD = '94' ");
           sql.append("     AND YEAR NOT IN " + notContainTotalYears);
           if ("0".equals(schooldiv)) {
               sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
           }
           sql.append(" GROUP BY " + groupByColumn);
       }

       //  修得単位数、評定平均（学年別）
       if (_stype > 1) {          //----->学年別
           sql.append(" UNION SELECT ");
           sql.append("     cast(null as smallint) as CLASS_ORDER,");
           sql.append("     cast(null as smallint) as SUBCLASS_ORDER,");
           sql.append("     cast(null as varchar(4)) as YEAR,");
           sql.append("     " + groupByColumn + " AS ANNUAL,");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("         'ZZ' AS SCHOOL_KIND, ");
               sql.append("         'ZZ' AS CLASS_SCHK, ");
               sql.append("         'ZZ' AS CURRICULUM_CD, ");
           }
           sql.append("     'ZZ' AS CLASSCD,");
           sql.append("     " + total + " AS CLASSNAME,");
           if (withSubclassnameJp) {
               sql.append("     " + total + " AS CLASSNAME_JP,");
           }
           sql.append("     'ZZZZ' AS SUBCLASSCD,");
           sql.append("     'ZZZZ' AS RAW_SUBCLASSCD,");
           sql.append("     " + total + " AS SUBCLASSNAME,");
           if (withSubclassnameJp) {
               sql.append("     " + total + " AS SUBCLASSNAME_JP,");
           }
           sql.append("     0 AS GRADES,");
           if (_hyoutei.equals("grade")) {
               sql.append(" ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1) AS AVG_GRADES,");
           } else {
               sql.append(" 0 AS AVG_GRADES,");
           }
           sql.append("     '' AS ASSESS_LEVEL, ");
           sql.append("     0 AS GRADE_CREDIT,");
           sql.append("     SUM(" + h_1_3 + ") AS CREDIT, ");
           sql.append("     CAST(NULL AS VARCHAR(1)) AS D065FLG ");
           sql.append(" FROM ");
           if (isKindai) {
               sql.append("     STUDYREC T1 ");
           } else {
               sql.append("     T_STUDYREC T1 ");
           }
           sql.append(" WHERE ");
           sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
           if (null != lastLineClasscd && isTotalContainLastLineClass) {
               sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
           }
           sql.append("      )");
           sql.append("     AND YEAR NOT IN " + notContainTotalYears);
           if ("0".equals(schooldiv)) {
               sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
           }
           sql.append(" GROUP BY " + groupByColumn);
       }

       //  全体の修得単位数・全体の評定平均値
       sql.append("     UNION SELECT ");
       sql.append("     cast(null as smallint) as CLASS_ORDER,");
       sql.append("     cast(null as smallint) as SUBCLASS_ORDER,");
       sql.append("     cast(null as varchar(4)) as YEAR,");
       sql.append("         '000' AS ANNUAL,");
       if ("1".equals(_useCurriculumcd)) {
           sql.append("         'ZZ' AS SCHOOL_KIND, ");
           sql.append("         'ZZ' AS CLASS_SCHK, ");
           sql.append("         'ZZ' AS CURRICULUM_CD, ");
       }
       sql.append("         'ZZ' AS CLASSCD,");
       sql.append("         " + total + " AS CLASSNAME,");
       if (withSubclassnameJp) {
           sql.append("     " + total + " AS CLASSNAME_JP,");
       }
       sql.append("         'ZZZZ' AS SUBCLASSCD,");
       sql.append("         'ZZZZ' AS RAW_SUBCLASSCD,");
       if (_atype.equals("on")) {                //----->特Ａフラグ出力の有無
           sql.append("     CASE VALUE(MAX(T2.COMMENTEX_A_CD),'0') WHEN '1' THEN '○' ELSE '  ' END AS SUBCLASSNAME,");
       } else {
           sql.append("     " + total + " AS SUBCLASSNAME,");
       }
       if (withSubclassnameJp) {
           sql.append("     " + total + " AS SUBCLASSNAME_JP,");
       }
       sql.append("         0 AS GRADES,");

       if (_hyoutei.equals(hyouteiDefault)) {           //----->評定の出力有無
           sql.append("     0 AS AVG_GRADES,");
           sql.append("     '' AS ASSESS_LEVEL,");
       } else {
           sql.append("     ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1) AS AVG_GRADES,");
           sql.append("     (SELECT    ST2.ASSESSMARK ");
           sql.append("      FROM      ASSESS_MST ST2 ");
           sql.append("      WHERE     ST2.ASSESSCD='4' ");
           sql.append("                 AND DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1),5,1) ");
           sql.append("                         BETWEEN ST2.ASSESSLOW AND ST2.ASSESSHIGH) AS ASSESS_LEVEL,");
       }
       sql.append("         0 AS GRADE_CREDIT,");
       sql.append("         SUM(" + h_1_3 + ") AS CREDIT, ");
       sql.append("         CAST(NULL AS VARCHAR(1)) AS D065FLG ");
       sql.append("     FROM ");
       if (isKindai) {
           sql.append("     STUDYREC T1 ");
       } else {
           sql.append("     T_STUDYREC T1 ");
       }
       if (_atype.equals("on")) {                //----->特Ａフラグ出力の有無
           sql.append("     LEFT JOIN HEXAM_ENTREMARK_HDAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
       }
       sql.append("     WHERE ");
       sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
       if (null != lastLineClasscd && isTotalContainLastLineClass) {
           sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
       }
       sql.append("      )");
       sql.append("         AND YEAR NOT IN " + notContainTotalYears);
       if ("0".equals(schooldiv)) {
           sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
       }

       // 前籍校における修得単位（レコードがある場合のみ）
       if ("1".equals(schooldiv) && null != zensekiSubclassCd) {
           if (_stype > 1) {          //----->学年別
               sql.append(" UNION SELECT"); 
               sql.append("      cast(null as smallint) AS CLASS_ORDER ");  // 表示順教科
               sql.append("    , cast(null as smallint) AS SUBCLASS_ORDER ");  // 表示順科目
               sql.append("    , cast(null as varchar(4)) as YEAR");
               sql.append("    , S1.ANNUAL");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("         , 'ZB' AS SCHOOL_KIND ");
                   sql.append("         , 'ZB' AS CLASS_SCHK ");
                   sql.append("         , 'ZB' AS CURRICULUM_CD ");
               }
               sql.append("    , 'ZB' AS CLASSCD");
               sql.append("    , " + zenseki + " AS CLASSNAME");
               if (withSubclassnameJp) {
                   sql.append("     " + zenseki + " AS CLASSNAME_JP,");
               }
               sql.append("    , 'ZZZB' AS SUBCLASSCD");
               sql.append("    , 'ZZZB' AS RAW_SUBCLASSCD");
               sql.append("    , " + zenseki + " AS SUBCLASSNAME");
               if (withSubclassnameJp) {
                   sql.append("     " + zenseki + " AS SUBCLASSNAME_JP,");
               }
               sql.append("    , 0 AS GRADES");
               sql.append("    , 0 AS AVG_GRADES");
               sql.append("    , '' ASSESS_LEVEL");
               sql.append("    , 0 AS GRADE_CREDIT");
               sql.append("    , S1.CREDIT ");
               sql.append("    , CAST(NULL AS VARCHAR(1)) AS D065FLG ");
               sql.append(" FROM(");
               sql.append("      SELECT T1.SCHREGNO,SUM(T1.CREDIT ) AS CREDIT, T1.ANNUAL ");
               sql.append("      FROM(");
               sql.append("           SELECT SCHREGNO, CREDIT");
               sql.append("            , YEAR AS ANNUAL ");
               sql.append("           FROM ");
               if (isKindai) {
                   sql.append("     STUDYREC ");
               } else {
                   sql.append("     T_STUDYREC ");
               }
               sql.append("           WHERE ((SCHOOLCD = '1' OR YEAR = '0') ");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("                  AND RAW_SUBCLASSCD = '" + zensekiSubclassCd + "'");
               } else {
                   sql.append("                  AND SUBCLASSCD = '" + zensekiSubclassCd + "'");
               }
               sql.append("                 )");
               sql.append("                 AND YEAR NOT IN " + notContainTotalYears);
               if ("0".equals(schooldiv)) {
                   sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
               }
               sql.append("      )T1");
               sql.append("      GROUP BY T1.SCHREGNO, T1.ANNUAL");
               sql.append("      HAVING T1.SCHREGNO IS NOT NULL");
               sql.append(" )S1 ");
           }
           //----->合計
           sql.append(" UNION SELECT"); 
           sql.append("      cast(null as smallint) AS CLASS_ORDER ");  // 表示順教科
           sql.append("    , cast(null as smallint) AS SUBCLASS_ORDER ");  // 表示順科目
           sql.append("    , cast(null as varchar(4)) as YEAR ");
           sql.append("    , '000' AS ANNUAL");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("         , 'ZB' AS SCHOOL_KIND ");
               sql.append("         , 'ZB' AS CLASS_SCHK ");
               sql.append("         , 'ZB' AS CURRICULUM_CD ");
           }
           sql.append("    , 'ZB' AS CLASSCD");
           sql.append("    , " + zenseki + " AS CLASSNAME");
           if (withSubclassnameJp) {
               sql.append("     " + zenseki + " AS CLASSNAME_JP,");
           }
           sql.append("    , 'ZZZB' AS SUBCLASSCD");
           sql.append("    , 'ZZZB' AS RAW_SUBCLASSCD");
           sql.append("    , " + zenseki + " AS SUBCLASSNAME");
           if (withSubclassnameJp) {
               sql.append("     " + zenseki + " AS SUBCLASSNAME_JP,");
           }
           sql.append("    , 0 AS GRADES");
           sql.append("    , 0 AS AVG_GRADES");
           sql.append("    , '' ASSESS_LEVEL");
           sql.append("    , 0 AS GRADE_CREDIT");
           sql.append("    , S1.CREDIT ");
           sql.append("    , CAST(NULL AS VARCHAR(1)) AS D065FLG ");
           sql.append(" FROM(");
           sql.append("      SELECT SCHREGNO,SUM(T1.CREDIT ) AS CREDIT ");
           sql.append("      FROM(");
           sql.append("           SELECT T1.SCHREGNO, CREDIT");
           sql.append("           FROM ");
           if (isKindai) {
               sql.append("     STUDYREC T1 ");
           } else {
               sql.append("     T_STUDYREC T1 ");
           }
           sql.append("           WHERE ((T1.SCHOOLCD = '1' OR T1.YEAR = '0') ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("                  AND T1.RAW_SUBCLASSCD = '" + zensekiSubclassCd + "'");
           } else {
               sql.append("                  AND T1.SUBCLASSCD = '" + zensekiSubclassCd + "'");
           }
           sql.append("                 )");
           sql.append("                 AND YEAR NOT IN " + notContainTotalYears);
           if ("0".equals(schooldiv)) {
               sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
           }
           sql.append("      )T1");
           sql.append("      GROUP BY T1.SCHREGNO");
           sql.append("      HAVING T1.SCHREGNO IS NOT NULL");
           sql.append(" )S1 ");
       }
           
       // 大検における認定単位（レコードがある場合のみ）
       if ("1".equals(schooldiv) && daiken_div_code) {
           if (_stype > 1) {          //----->学年別
               sql.append(" UNION SELECT"); 
               sql.append("      cast(null as smallint) AS CLASS_ORDER ");  // 表示順教科
               sql.append("    , cast(null as smallint) AS SUBCLASS_ORDER ");  // 表示順科目
               sql.append("    , cast(null as varchar(4)) as YEAR ");
               sql.append("    , S1.ANNUAL");
               if ("1".equals(_useCurriculumcd)) {
                   sql.append("         , 'ZA' AS SCHOOL_KIND ");
                   sql.append("         , 'ZA' AS CLASS_SCHK ");
                   sql.append("         , 'ZA' AS CURRICULUM_CD ");
               }
               sql.append("    , 'ZA' AS CLASSCD");
               sql.append("    , " + daiken + " AS CLASSNAME");
               if (withSubclassnameJp) {
                   sql.append("     " + daiken + " AS CLASSNAME_JP,");
               }
               sql.append("    , 'ZZZA' AS SUBCLASSCD");
               sql.append("    , 'ZZZA' AS RAW_SUBCLASSCD");
               sql.append("    , " + daiken + " AS SUBCLASSNAME");
               if (withSubclassnameJp) {
                   sql.append("     " + daiken + " AS SUBCLASSNAME_JP,");
               }
               sql.append("    , 0 AS GRADES");
               sql.append("    , 0 AS AVG_GRADES");
               sql.append("    , '' ASSESS_LEVEL");
               sql.append("    , 0 AS GRADE_CREDIT");
               sql.append("    , S1.CREDIT ");
               sql.append("    , CAST(NULL AS VARCHAR(1)) AS D065FLG ");
               sql.append(" FROM(");
               sql.append("      SELECT T1.SCHREGNO,SUM(T1.CREDIT ) AS CREDIT, T1.ANNUAL ");
               sql.append("      FROM(");
               sql.append("           SELECT SCHREGNO");
               sql.append("            , CREDIT");
               sql.append("            , YEAR AS ANNUAL ");
               sql.append("           FROM ");
               if (isKindai) {
                   sql.append("     STUDYREC ");
               } else {
                   sql.append("     T_STUDYREC ");
               }
               sql.append("           WHERE SCHOOLCD = '2'");
               sql.append("                 AND YEAR NOT IN " + notContainTotalYears);
               if ("0".equals(schooldiv)) {
                   sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
               }
               sql.append("      )T1");
               sql.append("      GROUP BY T1.SCHREGNO, T1.ANNUAL");
               sql.append("      HAVING T1.SCHREGNO IS NOT NULL");
               sql.append(" )S1 ");
           }
           //----->合計
           sql.append(" UNION SELECT"); 
           sql.append("      cast(null as smallint) AS CLASS_ORDER ");  // 表示順教科
           sql.append("    , cast(null as smallint) AS SUBCLASS_ORDER ");  // 表示順科目
           sql.append("    , cast(null as varchar(4)) as YEAR ");
           sql.append("    , '000' AS ANNUAL");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("         , 'ZB' AS SCHOOL_KIND ");
               sql.append("         , 'ZB' AS CLASS_SCHK ");
               sql.append("         , 'ZB' AS CURRICULUM_CD ");
           }
           sql.append("    , 'ZA' AS CLASSCD");
           sql.append("    , " + daiken + " AS CLASSNAME");
           if (withSubclassnameJp) {
               sql.append("     " + daiken + " AS CLASSNAME_JP,");
           }
           sql.append("    , 'ZZZA' AS SUBCLASSCD");
           sql.append("    , 'ZZZA' AS RAW_SUBCLASSCD");
           sql.append("    , " + daiken + " AS SUBCLASSNAME");
           if (withSubclassnameJp) {
               sql.append("     " + daiken + " AS SUBCLASSNAME_JP,");
           }
           sql.append("    , 0 AS GRADES");
           sql.append("    , 0 AS AVG_GRADES");
           sql.append("    , '' ASSESS_LEVEL");
           sql.append("    , 0 AS GRADE_CREDIT");
           sql.append("    , S1.CREDIT ");
           sql.append("    , CAST(NULL AS VARCHAR(1)) AS D065FLG ");
           sql.append(" FROM(");
           sql.append("      SELECT SCHREGNO,SUM(T1.CREDIT ) AS CREDIT ");
           sql.append("      FROM(");
           sql.append("           SELECT T1.SCHREGNO, CREDIT");
           sql.append("           FROM ");
           if (isKindai) {
               sql.append("     STUDYREC T1 ");
           } else {
               sql.append("     T_STUDYREC T1 ");
           }
           sql.append("           WHERE T1.SCHOOLCD = '2'");
           sql.append("                 AND YEAR NOT IN " + notContainTotalYears);
           if ("0".equals(schooldiv)) {
               sql.append("     AND YEAR NOT IN (SELECT YEAR FROM DROP_YEAR) ");
           }
           sql.append("      )T1");
           sql.append("      GROUP BY T1.SCHREGNO");
           sql.append("      HAVING T1.SCHREGNO IS NOT NULL");
           sql.append(" )S1 ");
       }
       sql.append(") ");
       if (isPrintChairSubclass) {
           sql.append(" , CHAIR_STD AS ( ");
           sql.append("     SELECT ");
           sql.append("         T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T3.ANNUAL, ");
           sql.append("         T2.CLASSCD, ");
           sql.append("         T2.SCHOOL_KIND, ");
           sql.append("         T2.CURRICULUM_CD, ");
           sql.append("         T2.SUBCLASSCD ");
           sql.append("     FROM CHAIR_STD_DAT T1 ");
           sql.append("     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
           sql.append("         AND T2.SEMESTER = T1.SEMESTER ");
           sql.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
           sql.append("     INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
           sql.append("         AND T3.YEAR = T1.YEAR ");
           sql.append("         AND T3.SEMESTER = T1.SEMESTER ");
           sql.append("     WHERE ");
           sql.append("         T1.YEAR = '" + paramYear + "' ");
           sql.append("         AND T1.SCHREGNO = " + schregno + " ");
           sql.append(" ) ");
           sql.append(" , MAX_SEMESTER_THIS_YEAR AS ( ");
           sql.append("     SELECT ");
           sql.append("         SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER ");
           sql.append("     FROM CHAIR_STD ");
           sql.append("     GROUP BY ");
           sql.append("         SCHREGNO, YEAR ");
           sql.append(" ) ");
           sql.append(" , CREDIT_MST_CREDITS AS ( ");
           sql.append("     SELECT DISTINCT ");
           sql.append("         T1.YEAR, T1.SCHREGNO, T2.ANNUAL, ");
           sql.append("         T1.CLASSCD, ");
           sql.append("         T1.SCHOOL_KIND, ");
           sql.append("         T1.CURRICULUM_CD, ");
           sql.append("         T1.SUBCLASSCD, ");
           sql.append("         T3.CREDITS ");
           sql.append("     FROM CHAIR_STD T1 ");
           sql.append("     INNER JOIN MAX_SEMESTER_THIS_YEAR SEM ON SEM.SCHREGNO = T1.SCHREGNO ");
           sql.append("         AND SEM.YEAR = T1.YEAR ");
           sql.append("     LEFT JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
           sql.append("         AND T2.YEAR = T1.YEAR ");
           sql.append("         AND T2.SEMESTER = SEM.SEMESTER ");
           sql.append("     LEFT JOIN CREDIT_MST T3 ON T3.YEAR = T1.YEAR ");
           sql.append("         AND T3.COURSECD = T2.COURSECD ");
           sql.append("         AND T3.MAJORCD = T2.MAJORCD ");
           sql.append("         AND T3.GRADE = T2.GRADE ");
           sql.append("         AND T3.COURSECODE = T2.COURSECODE ");
           sql.append("         AND T3.CLASSCD = T1.CLASSCD ");
           sql.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
           sql.append("         AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
           sql.append("         AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
           sql.append(" ) ");
           sql.append(" , CHAIR_STD_COMBINED AS ( ");
           sql.append("     SELECT ");
           sql.append("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
           sql.append("            T1.CLASSCD, ");
           sql.append("            T1.SCHOOL_KIND, ");
           sql.append("            T1.CURRICULUM_CD, ");
           sql.append("            T1.SUBCLASSCD, ");
           sql.append("            T5.CREDITS ");
           sql.append("     FROM CHAIR_STD T1 ");
           sql.append("     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T3 ON T3.YEAR = T1.YEAR ");
           sql.append("         AND T3.COMBINED_CLASSCD = T1.CLASSCD ");
           sql.append("         AND T3.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
           sql.append("         AND T3.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
           sql.append("         AND T3.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
           sql.append("     LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T4 ON T4.YEAR = T1.YEAR ");
           sql.append("         AND T4.ATTEND_CLASSCD = T1.CLASSCD ");
           sql.append("         AND T4.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
           sql.append("         AND T4.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
           sql.append("         AND T4.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
           sql.append("     LEFT JOIN CREDIT_MST_CREDITS T5 ON T5.YEAR = T1.YEAR ");
           sql.append("         AND T5.SCHREGNO = T1.SCHREGNO ");
           sql.append("         AND T5.CLASSCD = T1.CLASSCD ");
           sql.append("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
           sql.append("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
           sql.append("         AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
           sql.append("     WHERE ");
           sql.append("         T3.COMBINED_SUBCLASSCD IS NULL ");
           sql.append("         AND T4.ATTEND_SUBCLASSCD IS NULL ");
           sql.append("     UNION ");
           sql.append("     SELECT ");
           sql.append("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
           sql.append("            T3.COMBINED_CLASSCD AS CLASSCD, ");
           sql.append("            T3.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
           sql.append("            T3.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
           sql.append("            T3.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
           sql.append("            CASE WHEN '2' = MAX(T3.CALCULATE_CREDIT_FLG) THEN SUM(T5.CREDITS) ");
           sql.append("                 ELSE MAX(T6.CREDITS) ");
           sql.append("            END AS CREDITS ");
           sql.append("     FROM CHAIR_STD T1 ");
           sql.append("     INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T3 ON T3.YEAR = T1.YEAR ");
           sql.append("         AND T3.ATTEND_CLASSCD = T1.CLASSCD ");
           sql.append("         AND T3.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
           sql.append("         AND T3.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
           sql.append("         AND T3.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
           sql.append("     LEFT JOIN CREDIT_MST_CREDITS T5 ON T5.YEAR = T1.YEAR ");
           sql.append("         AND T5.SCHREGNO = T1.SCHREGNO ");
           sql.append("         AND T5.CLASSCD = T3.ATTEND_CLASSCD ");
           sql.append("         AND T5.SCHOOL_KIND = T3.ATTEND_SCHOOL_KIND ");
           sql.append("         AND T5.CURRICULUM_CD = T3.ATTEND_CURRICULUM_CD ");
           sql.append("         AND T5.SUBCLASSCD = T3.ATTEND_SUBCLASSCD ");
           sql.append("     LEFT JOIN CREDIT_MST_CREDITS T6 ON T6.YEAR = T1.YEAR ");
           sql.append("         AND T6.SCHREGNO = T1.SCHREGNO ");
           sql.append("         AND T6.CLASSCD = T3.COMBINED_CLASSCD ");
           sql.append("         AND T6.SCHOOL_KIND = T3.COMBINED_SCHOOL_KIND ");
           sql.append("         AND T6.CURRICULUM_CD = T3.COMBINED_CURRICULUM_CD ");
           sql.append("         AND T6.SUBCLASSCD = T3.COMBINED_SUBCLASSCD ");
           sql.append("     GROUP BY ");
           sql.append("            T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
           sql.append("            T3.COMBINED_CLASSCD, ");
           sql.append("            T3.COMBINED_SCHOOL_KIND, ");
           sql.append("            T3.COMBINED_CURRICULUM_CD, ");
           sql.append("            T3.COMBINED_SUBCLASSCD ");
           sql.append(" ) ");
           sql.append(" , CHAIR_STD_SUBCLASSCD2 AS ( ");
           sql.append("     SELECT ");
           sql.append("         T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
           sql.append("         T1.CLASSCD, ");
           sql.append("         T1.SCHOOL_KIND, ");
           sql.append("         T1.CURRICULUM_CD, ");
           sql.append("         T1.SUBCLASSCD, ");
           sql.append("         T1.CREDITS ");
           sql.append("     FROM CHAIR_STD_COMBINED T1 ");
           sql.append("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
           sql.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
           sql.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
           sql.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
           sql.append("         AND T2.SUBCLASSCD2 IS NULL ");
           sql.append("     UNION ");
           sql.append("     SELECT ");
           sql.append("         T1.YEAR, T1.SCHREGNO, T1.ANNUAL, ");
           sql.append("         T1.CLASSCD, ");
           sql.append("         T1.SCHOOL_KIND, ");
           sql.append("         T1.CURRICULUM_CD, ");
           sql.append("         T2.SUBCLASSCD2 AS SUBCLASSCD, ");
           sql.append("         T6.CREDITS ");
           sql.append("     FROM CHAIR_STD_COMBINED T1 ");
           sql.append("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
           sql.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
           sql.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
           sql.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
           sql.append("         AND T2.SUBCLASSCD2 IS NOT NULL ");
           sql.append("     LEFT JOIN CREDIT_MST_CREDITS T6 ON T6.YEAR = T1.YEAR ");
           sql.append("         AND T6.SCHREGNO = T1.SCHREGNO ");
           sql.append("         AND T6.CLASSCD = T1.CLASSCD ");
           sql.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
           sql.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
           sql.append("         AND T6.SUBCLASSCD = T2.SUBCLASSCD2 ");
           sql.append(" ) ");
           sql.append(" , CHAIR_STD_SUBCLASS AS (");
           sql.append(" SELECT ");
           sql.append("     T1.YEAR, T1.SCHREGNO, T1.ANNUAL ");
           sql.append("   , T1.CLASSCD ");
           sql.append("   , T1.SCHOOL_KIND ");
           sql.append("   , T1.CURRICULUM_CD ");
           sql.append("   , T1.SUBCLASSCD ");
           sql.append("   , T1.CREDITS");
           if (isEnglish) {
               sql.append("   , T2.CLASSNAME_ENG AS CLASSNAME ");
               sql.append("   , T3.SUBCLASSNAME_ENG AS SUBCLASSNAME ");
               if (withSubclassnameJp) {
                   sql.append("   , T2.CLASSNAME AS CLASSNAME_JP ");
                   sql.append("   , VALUE(T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME_JP ");
               }
           } else {
               sql.append("   , T2.CLASSNAME ");
               sql.append("   , VALUE(T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME ");
           }
           sql.append("   , T2.SHOWORDER AS SHOWORDERCLASS"); // 表示順教科
           sql.append("   , T3.SHOWORDER AS SHOWORDERSUBCLASS"); // 表示順科目
           sql.append("   , value(T2.SPECIALDIV, '0') AS SPECIALDIV"); // 専門教科
           sql.append(" FROM CHAIR_STD_SUBCLASSCD2 T1 ");
           sql.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
           if ("1".equals(_useCurriculumcd)) {
               sql.append("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
           }
           sql.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
           sql.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
           sql.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
           sql.append("     AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
           sql.append(" ) ");
           sql.append(" , CHAIR_STD_SUBCLASS_MAIN AS (");
           sql.append(" SELECT ");
           sql.append("     YEAR, SCHREGNO, ANNUAL ");
           sql.append("   , CLASSCD ");
           sql.append("   , SCHOOL_KIND ");
           sql.append("   , CURRICULUM_CD ");
           sql.append("   , CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD  AS SUBCLASSCD ");
           sql.append("   , CREDITS");
           sql.append("   , CLASSNAME ");
           sql.append("   , SUBCLASSNAME ");
           if (withSubclassnameJp) {
               sql.append("   , CLASSNAME_JP ");
               sql.append("   , SUBCLASSNAME_JP ");
           }
           sql.append("   , SHOWORDERCLASS");
           sql.append("   , SHOWORDERSUBCLASS");
           sql.append("   , SPECIALDIV");
           sql.append(" FROM CHAIR_STD_SUBCLASS T1 ");
           sql.append(" WHERE ");
           sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
           if (null != lastLineClasscd) {
               sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
           }
           sql.append("      )");
           sql.append(" UNION ALL ");
           sql.append(" SELECT ");
           sql.append("     YEAR, SCHREGNO, ANNUAL ");
           sql.append("   , '" + KNJDefineCode.subject_T + "' AS CLASSCD ");
           sql.append("   , SCHOOL_KIND ");
           sql.append("   , '" + KNJDefineCode.subject_T + "' AS CURRICULUM_CD ");
           sql.append("   , '" + KNJDefineCode.subject_T + "01' AS SUBCLASSCD ");
           sql.append("   , SUM(CREDITS) AS CREDITS ");
           sql.append("   , " + sogo + " AS CLASSNAME ");
           sql.append("   , " + sogo + " AS SUBCLASSNAME ");
           if (withSubclassnameJp) {
               sql.append("   , " + sogo + " AS CLASSNAME_JP ");
               sql.append("   , " + sogo + " AS SUBCLASSNAME_JP ");
           }
           sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERCLASS");
           sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERSUBCLASS");
           sql.append("   , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV");
           sql.append(" FROM CHAIR_STD_SUBCLASS T1 ");
           sql.append(" WHERE ");
           sql.append("     CLASSCD = '" + KNJDefineCode.subject_T + "' ");
           sql.append(" GROUP BY ");
           sql.append("     YEAR, SCHREGNO, ANNUAL, SCHOOL_KIND ");
           sql.append(" UNION ALL ");
           sql.append(" SELECT ");
           sql.append("     YEAR, SCHREGNO, ANNUAL ");
           sql.append("   , 'ZZ' AS CLASSCD ");
           sql.append("   , SCHOOL_KIND ");
           sql.append("   , 'ZZZZ' AS CURRICULUM_CD ");
           sql.append("   , 'ZZZZ' AS SUBCLASSCD ");
           sql.append("   , SUM(CREDITS) AS CREDITS ");
           sql.append("   , " + total + " AS CLASSNAME ");
           sql.append("   , " + total + " AS SUBCLASSNAME ");
           if (withSubclassnameJp) {
               sql.append("   , " + total + " AS CLASSNAME_JP ");
               sql.append("   , " + total + " AS SUBCLASSNAME_JP ");
           }
           sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERCLASS");
           sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERSUBCLASS");
           sql.append("   , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV");
           sql.append(" FROM CHAIR_STD_SUBCLASS T1 ");
           sql.append(" WHERE ");
           sql.append("     (T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
           if (null != lastLineClasscd && isTotalContainLastLineClass) {
               sql.append("   OR T1.CLASSCD = '" + lastLineClasscd + "'");
           }
           sql.append("      )");
           sql.append(" GROUP BY ");
           sql.append("     YEAR, SCHREGNO, ANNUAL, SCHOOL_KIND ");
           if (isTokiwa) {
               // 常盤ホームルーム(教科コード94)
               sql.append(" UNION ALL ");
               sql.append(" SELECT ");
               sql.append("     YEAR, SCHREGNO, ANNUAL ");
               sql.append("   , " + tokiwahr + " AS CLASSCD ");
               sql.append("   , SCHOOL_KIND ");
               sql.append("   , " + tokiwahr + " AS CURRICULUM_CD ");
               sql.append("   , " + tokiwahr + " AS SUBCLASSCD ");
               sql.append("   , SUM(CREDITS) AS CREDITS ");
               sql.append("   , " + tokiwahr + " AS CLASSNAME ");
               sql.append("   , " + tokiwahr + " AS SUBCLASSNAME ");
               if (withSubclassnameJp) {
                   sql.append("   , " + tokiwahr + " AS CLASSNAME_JP ");
                   sql.append("   , " + tokiwahr + " AS SUBCLASSNAME_JP ");
               }
               sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERCLASS");
               sql.append("   , CAST(NULL AS SMALLINT) AS SHOWORDERSUBCLASS");
               sql.append("   , CAST(NULL AS VARCHAR(1)) AS SPECIALDIV");
               sql.append(" FROM CHAIR_STD_SUBCLASS T1 ");
               sql.append(" WHERE ");
               sql.append("     CLASSCD = '94' ");
               sql.append(" GROUP BY ");
               sql.append("     YEAR, SCHREGNO, ANNUAL, SCHOOL_KIND ");
           }
           sql.append(" ) ");
       }
       sql.append(" SELECT"); 
       sql.append("   'STUDYREC' AS STUDY_FLAG ");
       sql.append("  ,T1.CLASS_ORDER ");  // 表示順教科
       sql.append("  ,T1.SUBCLASS_ORDER ");  // 表示順科目
       sql.append("  ,T1.YEAR");
       sql.append("  ,T1.ANNUAL");
       sql.append("  ,T1.CLASSCD");
       sql.append("  ,T1.CLASSNAME");
       if (withSubclassnameJp) {
           sql.append("   , CLASSNAME_JP ");
       }
       sql.append("  ,T1.SUBCLASSCD");
       sql.append("  ,T1.SUBCLASSNAME");
       if (withSubclassnameJp) {
           sql.append("   , SUBCLASSNAME_JP ");
       }
       sql.append("  ,T1.GRADES");
       sql.append("  ,T1.AVG_GRADES");
       sql.append("  ,T1.ASSESS_LEVEL");
       sql.append("  ,T1.GRADE_CREDIT");
       sql.append("  ,T1.CREDIT ");
       sql.append("  ,VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV ");
       sql.append("  ,T1.D065FLG ");
       sql.append(" FROM ");
       sql.append("    MAIN T1 ");
       sql.append("    LEFT JOIN CLASS_MST T2 ON ");
       if ("1".equals(_useCurriculumcd)) {
           sql.append("  T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
       } else {
           sql.append("  T2.CLASSCD = T1.CLASSCD ");
       }
       if (isPrintChairSubclass) {
           sql.append(" UNION ALL ");
           sql.append(" SELECT");
           sql.append("   'CHAIR_SUBCLASS' AS STUDY_FLAG ");
           sql.append("  ,T2.SHOWORDERCLASS AS CLASS_ORDER ");  // 表示順教科
           sql.append("  ,T2.SHOWORDERSUBCLASS AS SUBCLASS_ORDER ");  // 表示順科目
           sql.append("  ,T2.YEAR");
           sql.append("  ,T2.ANNUAL");
           sql.append("  ,T2.CLASSCD");
           sql.append("  ,T2.CLASSNAME");
           if (withSubclassnameJp) {
               sql.append("   , CLASSNAME_JP ");
           }
           sql.append("  ,T2.SUBCLASSCD AS SUBCLASSCD");
           sql.append("  ,T2.SUBCLASSNAME");
           if (withSubclassnameJp) {
               sql.append("   , SUBCLASSNAME_JP ");
           }
           sql.append("  ,CAST(NULL AS SMALLINT) AS GRADES");
           sql.append("  ,CAST(NULL AS DECIMAL(5,1)) AS AVG_GRADES");
           sql.append("  ,CAST(NULL AS VARCHAR(1)) AS ASSESS_LEVEL");
           sql.append("  ,T2.CREDITS AS GRADE_CREDIT");
           sql.append("  ,CAST(NULL AS SMALLINT) AS CREDIT ");
           sql.append("  ,T2.SPECIALDIV ");
           sql.append("  ,CAST(NULL AS VARCHAR(1)) AS D065FLG ");
           sql.append(" FROM ");
           sql.append("    CHAIR_STD_SUBCLASS_MAIN T2 ");
       }
       sql.append(" ORDER BY ");
       sql.append("   CASE WHEN D065FLG IS NOT NULL THEN 999 ELSE 0 END");
       if (!"1".equals(notUseClassMstSpecialDiv)) {
           sql.append("   ,SPECIALDIV ");
       }
       sql.append("  ,CLASS_ORDER ");
       sql.append("  ,CLASSCD ");
       sql.append("  ,SUBCLASS_ORDER ");
       if (isKyoto) {
           sql.append("  ,RAW_SUBCLASSCD ");
       }
       sql.append("  ,SUBCLASSCD ");
       sql.append("  ,YEAR ");
       sql.append("  ,ANNUAL");

       return sql.toString();
   }//public pre_sql_newの括り

   public String pre_sql() {
       return pre_sql(new HashMap());
   }

   /**
     *
     *  学習記録のSQL
     *
     */
    public String pre_sql(final Map paramMap) {
        final String _hyoutei = hyoutei(_config);
        final String _atype = atype(_config);
        final int _stype = stype(_config);
        final String _useCurriculumcd = useCurriculumcd(_config);
        final boolean useD065 = null != getString(paramMap, "useD065");
        final boolean withSubclassnameJp = null != paramMap.get("WithSubclassnameJp");

        if (tname1 == null) {
            setFieldName();   //使用テーブル名設定 05/07/25Build
        }
        final StringBuffer stb = new StringBuffer();
        try{
        //  評定１を２と判定
            String h_1_2 = null;
            String h_1_3 = null;
            if (_hyoutei.equals("on")) { //----->評定読み替えのON/OFF  評定１を２と読み替え
                h_1_2 = "CASE VALUE(T1.GRADES,0) WHEN 1 THEN 2 ELSE T1.GRADES END ";
                h_1_3 = "T1.CREDIT ";  //NO001
                //NO001 h_1_3 = "CASE WHEN VALUE(T1.GRADES,0)=1 AND VALUE(T1.CREDIT,0)=0 THEN T1.ADD_CREDIT ELSE T1.CREDIT END ";
            } else {
                h_1_2 = "T1.GRADES ";
                h_1_3 = "T1.CREDIT ";
            }
            
            final String targetSchoolKind = "1".equals(_useCurriculumcd) ? (String) paramMap.get("TARGET_SCHOOL_KIND") : null;
            final boolean notUseStudyrecProvFlgDat = null != paramMap.get("notUseStudyrecProvFlgDat");
            final boolean isKindai = 3 <= definecode.schoolmark.length()  &&  definecode.schoolmark.equals("KIN");
            if (isKindai) {
                stb.append("WITH STUDYREC AS(");
                stb.append("SELECT  T1.CLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.ANNUAL, ");
                stb.append(        "T1.CLASSCD, ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASS_SCHK, ");
                }
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append(        "VALUE(L2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD, ");
                stb.append(        "T1.VALUATION AS GRADES ");
                stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
                if (useD065) {
                    stb.append("        ,NMD065.NAME1 AS D065FLG ");
                } else {
                    stb.append("        ,CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                }
                stb.append("FROM   " + tname1 + " T1 ");
                stb.append(        "LEFT JOIN SUBCLASS_MST L2 ON ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
                }
                stb.append(            "L2.SUBCLASSCD=");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append(            "T1.SUBCLASSCD ");
                if (notUseStudyrecProvFlgDat) {
                } else {
                    stb.append(        "LEFT JOIN STUDYREC_PROV_FLG_DAT TPROV ON TPROV.SCHOOLCD = T1.SCHOOLCD ");
                    stb.append(        "    AND TPROV.YEAR = T1.YEAR ");
                    stb.append(        "    AND TPROV.SCHREGNO = T1.SCHREGNO ");
                    if ("1".equals(_useCurriculumcd)) {
                        stb.append(        "    AND TPROV.CLASSCD = T1.CLASSCD ");
                        stb.append(        "    AND TPROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                        stb.append(        "    AND TPROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                    }
                    stb.append(        "    AND TPROV.SUBCLASSCD = T1.SUBCLASSCD ");
                    stb.append(        "    AND TPROV.PROV_FLG = '1' ");
                }
                if (useD065) {
                    stb.append("   LEFT JOIN V_NAME_MST NMD065 ON NMD065.YEAR = T1.YEAR AND NMD065.NAMECD1 = 'D065' AND NMD065.NAME1 ");
                    if ("1".equals(_useCurriculumcd)) {
                        stb.append(" = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                    } else {
                        stb.append(" = T1.SUBCLASSCD ");
                    }
                }
                stb.append("WHERE   T1.SCHREGNO = ? AND ");
                stb.append(        "T1.YEAR <= ? AND ");
                stb.append(        "(T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "') AND ");
                stb.append(        "NOT EXISTS(SELECT  'X' ");
                stb.append(                   "FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
                stb.append(                   "WHERE   T2.YEAR = T1.YEAR AND ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append(                           " T2.ATTEND_SUBCLASSCD = ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append(                           " T1.SUBCLASSCD) ");
                if (notUseStudyrecProvFlgDat) {
                } else {
                    stb.append(        "    AND TPROV.SUBCLASSCD IS NULL ");
                }
                stb.append(    ")");

            } else {
                stb.append("WITH T_STUDYREC AS(");
                stb.append("SELECT  T1.CLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.ANNUAL, ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append(        "T1.SUBCLASSCD AS SUBCLASSCD, ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append(        "L2.SUBCLASSCD2 AS SUBCLASSCD2, ");
                stb.append(        "T1.VALUATION AS GRADES ");
                // 08/03/06Add
                if (isHosei(_config)) {
                    stb.append(   ",CASE WHEN T1.CLASSCD = '87' THEN '90' ELSE T1.CLASSCD END AS CLASSCD ");
                    if ("1".equals(_useCurriculumcd)) {
                        stb.append(   ",CASE WHEN T1.CLASSCD = '87' THEN '90' ELSE T1.CLASSCD END || '-' || T1.SCHOOL_KIND AS CLASS_SCHK ");
                    }
                } else {
                    stb.append(   ",T1.CLASSCD ");
                    if ("1".equals(_useCurriculumcd)) {
                        stb.append(   ",T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASS_SCHK ");
                    }
                }
                stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
                stb.append("       ,CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                //NO001 stb.append(        "GET_CREDIT AS CREDIT,ADD_CREDIT ");
                stb.append("FROM   " + tname1 + " T1 ");
                stb.append(        "LEFT JOIN SUBCLASS_MST L2 ON ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || ");
                }
                stb.append(            "L2.SUBCLASSCD=");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append(            "T1.SUBCLASSCD ");
                if (notUseStudyrecProvFlgDat) {
                } else {
                    stb.append(        "LEFT JOIN STUDYREC_PROV_FLG_DAT TPROV ON TPROV.SCHOOLCD = T1.SCHOOLCD ");
                    stb.append(        "    AND TPROV.YEAR = T1.YEAR ");
                    stb.append(        "    AND TPROV.SCHREGNO = T1.SCHREGNO ");
                    if ("1".equals(_useCurriculumcd)) {
                        stb.append(        "    AND TPROV.CLASSCD = T1.CLASSCD ");
                        stb.append(        "    AND TPROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                        stb.append(        "    AND TPROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                    }
                    stb.append(        "    AND TPROV.SUBCLASSCD = T1.SUBCLASSCD ");
                    stb.append(        "    AND TPROV.PROV_FLG = '1' ");
                }
                stb.append("WHERE   T1.SCHREGNO = ? AND T1.YEAR <= ? AND ");
                stb.append(        "(T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "') ");
                if (null != targetSchoolKind) {
                    stb.append(        "AND T1.SCHOOL_KIND = '" + targetSchoolKind + "' ");
                }
                if (isNotPrintMirishu(_config)) {
                    stb.append(        "AND VALUE(T1.COMP_CREDIT, 0) <> 0 ");
                }
                if (notUseStudyrecProvFlgDat) {
                } else {
                    stb.append(        "    AND TPROV.SUBCLASSCD IS NULL ");
                }
                stb.append(") , STUDYREC0 AS( ");
                stb.append(    "SELECT ");
                stb.append(        "T1.CLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.ANNUAL, ");
                stb.append(        "T1.CLASSCD , ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "T1.CLASS_SCHK , ");
                }
                stb.append(        "T1.SUBCLASSCD, ");
                stb.append(        "T1.GRADES, ");
                stb.append(        "T1.CREDIT ");
                stb.append("        ,D065FLG ");
                stb.append(    "FROM ");
                stb.append(        "T_STUDYREC T1 ");
                stb.append(    "WHERE ");
                stb.append(        "T1.SUBCLASSCD2 IS NULL ");
                stb.append(    "UNION ALL ");
                stb.append(    "SELECT ");
                stb.append(        "MAX(T1.CLASSNAME) AS CLASSNAME, ");
                stb.append(        "MAX(T1.SUBCLASSNAME) AS SUBCLASSNAME, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "MAX(T1.ANNUAL) AS ANNUAL, ");
                stb.append(        "T1.CLASSCD, ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "MAX(T1.CLASS_SCHK) AS CLASS_SCHK, ");
                }
                stb.append(        "T1.SUBCLASSCD2 AS SUBCLASSCD, ");
                stb.append(        "MAX(T1.GRADES) AS GRADES, ");
                stb.append(        "SUM(T1.CREDIT) AS CREDIT ");
                stb.append("        ,MAX(D065FLG) AS D065FLG ");
                stb.append(    "FROM ");
                stb.append(        "T_STUDYREC T1 ");
                stb.append(    "WHERE ");
                stb.append(        "T1.SUBCLASSCD2 IS NOT NULL ");
                stb.append(    "GROUP BY ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.CLASSCD, ");
                stb.append(        "T1.SUBCLASSCD2 ");
                stb.append(") , STUDYREC AS( ");
                stb.append(    "SELECT ");
                stb.append(        "T1.CLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.ANNUAL, ");
                stb.append(        "T1.CLASSCD , ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "T1.CLASS_SCHK , ");
                }
                stb.append(        "T1.SUBCLASSCD, ");
                stb.append("        D065FLG, ");
                stb.append(        "MAX(T1.GRADES) AS GRADES, ");
                stb.append(        "SUM(T1.CREDIT) AS CREDIT ");
                stb.append(    "FROM ");
                stb.append(        "STUDYREC0 T1 ");
                stb.append(    "GROUP BY ");
                stb.append(        "T1.CLASSNAME, ");
                stb.append(        "T1.SUBCLASSNAME, ");
                stb.append(        "T1.SCHREGNO, ");
                stb.append(        "T1.YEAR, ");
                stb.append(        "T1.ANNUAL, ");
                stb.append(        "T1.CLASSCD , ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(        "T1.CLASS_SCHK , ");
                }
                stb.append(        "T1.SUBCLASSCD, ");
                stb.append(        "T1.D065FLG ");
                stb.append(") ");
            }

            //該当生徒の科目評定、修得単位及び教科評定平均
            stb.append( "SELECT ");
            stb.append(     "VALUE(T2.SPECIALDIV, '0') as SPECIALDIV,");
            stb.append(     "T2.SHOWORDER2 as CLASS_ORDER,");
            stb.append(     "T3.SHOWORDER2 as SUBCLASS_ORDER,");
            stb.append(     "T1.ANNUAL,");
            stb.append(     "T1.YEAR, ");
            stb.append(     "T1.CLASSCD,");
            if (!english(_config)) {                     //----->教科名 英語/日本語
                stb.append( "VALUE(T1.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME) AS CLASSNAME,");
            } else {
                stb.append( "T2.CLASSNAME_ENG AS CLASSNAME,");
                if (withSubclassnameJp) {
                    stb.append( "VALUE(T1.CLASSNAME, T2.CLASSORDERNAME1, T2.CLASSNAME) AS CLASSNAME_JP,");
                }
            }
            stb.append(     "T1.SUBCLASSCD,");
            if (!english(_config)) {                     //----->科目名 英語/日本語
                stb.append( "VALUE(T1.SUBCLASSNAME, T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME,");
            } else {
                stb.append( "T3.SUBCLASSNAME_ENG AS SUBCLASSNAME,");
                if (withSubclassnameJp) {
                    stb.append( "VALUE(T1.SUBCLASSNAME, T3.SUBCLASSORDERNAME1, T3.SUBCLASSNAME) AS SUBCLASSNAME_JP,");
                }
            }
            if (_hyoutei.equals(hyouteiDefault)) {        //----->評定の出力有無
                stb.append( "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,");
            } else {
                stb.append( h_1_2 + " AS GRADES,");
                stb.append( "T5.AVG_GRADES,'' AS ASSESS_LEVEL,");
            }
            stb.append(     "T1.CREDIT AS GRADE_CREDIT,T4.CREDIT ");
            stb.append(     ", D065FLG ");
            stb.append( "FROM ");
            stb.append(     "STUDYREC T1 ");
            stb.append(     "LEFT JOIN CLASS_MST T2 ON ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(        " T2.CLASSCD || '-' || T2.SCHOOL_KIND = T1.CLASS_SCHK ");
            } else {
                stb.append(        " T2.CLASSCD = T1.CLASSCD ");
            }
            stb.append(     "LEFT JOIN SUBCLASS_MST T3 ON ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(        " T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append(            " T3.SUBCLASSCD = T1.SUBCLASSCD ");
                //  修得単位数の計
            stb.append(     "INNER JOIN(SELECT ");
            stb.append(             "CLASSCD,SUBCLASSCD,SUM(" + h_1_3 + ") AS CREDIT ");
            stb.append(         "FROM ");
            stb.append(             "STUDYREC T1 ");
            stb.append(         "WHERE ");
            stb.append(             "CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
            stb.append(         "GROUP BY ");
            stb.append(             "CLASSCD,SUBCLASSCD ");
            stb.append(     ")T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if (_hyoutei.equals(hyouteiDefault)) {        //----->評定の出力有無
            } else {
                //  各教科の評定平均値
                stb.append( "INNER JOIN(SELECT ");
                stb.append(         "CLASSCD,");
                stb.append(         "DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1),5,1) AS AVG_GRADES ");
                stb.append(     "FROM ");
                stb.append(         "STUDYREC T1 ");
                stb.append(     "WHERE ");
                stb.append(         "CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
                stb.append(     "GROUP BY ");
                stb.append(         "CLASSCD ");
                stb.append( ")T5 ON T5.CLASSCD = T1.CLASSCD ");
            }
            stb.append( "WHERE ");
            stb.append(     "T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");

        //  総合学習の修得単位数（学年別）
            if (_stype > 1) {          //----->学年別
                stb.append("UNION SELECT ");
                stb.append(     "'9999' as SPECIALDIV,");
                stb.append(     "cast(null as smallint) as CLASS_ORDER,");
                stb.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
                stb.append(     "ANNUAL,");
                stb.append(     "CAST(NULL AS VARCHAR(1)) AS YEAR,");
                stb.append(     "'" + KNJDefineCode.subject_T + "' AS CLASSCD," + sogo + " AS CLASSNAME,");
                if (withSubclassnameJp) {
                    stb.append(     "" + sogo + " AS CLASSNAME_JP,");
                }
                stb.append(     "'" + KNJDefineCode.subject_T + "01' AS SUBCLASSCD," + sogo + " AS SUBCLASSNAME,");
                if (withSubclassnameJp) {
                    stb.append(     "" + sogo + " AS SUBCLASSNAME_JP,");
                }
                stb.append(     "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT, MAX(D065FLG) AS D065FLG ");
                stb.append( "FROM ");
                stb.append(     "STUDYREC ");
                stb.append( "WHERE ");
                stb.append(     "CLASSCD = '" + KNJDefineCode.subject_T + "' ");
                stb.append( "GROUP BY ");
                stb.append(     "ANNUAL ");
            }
        //  総合学習の修得単位数（合計）
            stb.append( "UNION SELECT ");
            stb.append(     "'9999' as SPECIALDIV,");
            stb.append(     "cast(null as smallint) as CLASS_ORDER,");
            stb.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
            stb.append(         "'0' AS ANNUAL,");
            stb.append(         "CAST(NULL AS VARCHAR(1)) AS YEAR,");
            stb.append(         "'" + KNJDefineCode.subject_T + "' AS CLASSCD,");
            stb.append(         "" + sogo + " AS CLASSNAME, ");
            if (withSubclassnameJp) {
                stb.append(     "" + sogo + " AS CLASSNAME_JP,");
            }
            stb.append(         "'" + KNJDefineCode.subject_T + "01' AS SUBCLASSCD," + sogo + " AS SUBCLASSNAME,");
            if (withSubclassnameJp) {
                stb.append(     "" + sogo + " AS SUBCLASSNAME_JP,");
            }
            stb.append(         "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT, MAX(D065FLG) AS D065FLG ");
            stb.append(     "FROM ");
            stb.append(         "STUDYREC ");
            stb.append(     "WHERE ");
            stb.append(         "CLASSCD = '" + KNJDefineCode.subject_T + "' ");

        //  留学中の修得単位数（学年別）
            if (_stype > 1) {              //----->学年別/合計
                stb.append( "UNION SELECT ");
                stb.append(     "'9999' as SPECIALDIV,");
                stb.append(     "cast(null as smallint) as CLASS_ORDER,");
                stb.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
                stb.append(     "ANNUAL,");
                stb.append(     "CAST(NULL AS VARCHAR(1)) AS YEAR,");
                stb.append(     "'AA' AS CLASSCD," + abroad + " AS CLASSNAME,");
                if (withSubclassnameJp) {
                    stb.append(     "" + abroad + " AS CLASSNAME_JP,");
                }
                stb.append(     "'AAAA' AS SUBCLASSCD," + abroad + " AS SUBCLASSNAME,");
                if (withSubclassnameJp) {
                    stb.append(     "" + abroad + " AS SUBCLASSNAME_JP,");
                }
                stb.append(     "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT, CAST(NULL AS VARCHAR(1)) AS D065FLG ");
                stb.append( "FROM ");
                stb.append(         "(SELECT ");
                stb.append(             "ABROAD_CREDITS,");
                stb.append(             "INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
                stb.append(         "FROM ");
                stb.append(              tname2 + " ");
                stb.append(         "WHERE ");
                stb.append(             "SCHREGNO =? AND TRANSFERCD = '1' ");
                stb.append(         ")ST1,");
                stb.append(         "(SELECT ");
                stb.append(             "ANNUAL,MAX(YEAR) AS YEAR ");
                stb.append(         "FROM ");
                stb.append(              tname3 + " ");
                stb.append(         "WHERE ");
                stb.append(             "SCHREGNO =? AND YEAR <=? ");
                stb.append(         "GROUP BY ");
                stb.append(             "ANNUAL ");
                stb.append(         ")ST2 ");
                stb.append( "WHERE ");
                stb.append(     "ST1.TRANSFER_YEAR <=? ");
                stb.append(     "and INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
                stb.append( "GROUP BY ");
                stb.append(     "ANNUAL ");
            }
            //  留学中の修得単位数（合計）
            stb.append(     "UNION SELECT ");
            stb.append(     "'9999' as SPECIALDIV,");
            stb.append(     "cast(null as smallint) as CLASS_ORDER,");
            stb.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
            stb.append(         "'0' AS ANNUAL,");
            stb.append(         "CAST(NULL AS VARCHAR(1)) AS YEAR,");
            stb.append(         "'AA' AS CLASSCD," + abroad + " AS CLASSNAME,");
            if (withSubclassnameJp) {
                stb.append(     "" + abroad + " AS CLASSNAME_JP,");
            }
            stb.append(         "'AAAA' AS SUBCLASSCD," + abroad + " AS SUBCLASSNAME,");
            if (withSubclassnameJp) {
                stb.append(     "" + abroad + " AS SUBCLASSNAME_JP,");
            }
            stb.append(         "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT, CAST(NULL AS VARCHAR(1)) AS D065FLG ");
            stb.append(     "FROM ");
            stb.append(         "(SELECT ");
            stb.append(             "SCHREGNO,ABROAD_CREDITS,INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
            stb.append(         "FROM  ");
            stb.append(              tname2 + " ");
            stb.append(         "WHERE  ");
            stb.append(             "SCHREGNO =? AND TRANSFERCD = '1' ");
            stb.append(         ")ST1 ");
            stb.append(     "WHERE ");
            stb.append(         "TRANSFER_YEAR <=? ");

            //  修得単位数、評定平均（学年別）
            if (_stype > 1) {          //----->学年別
                stb.append( "UNION SELECT ");
                stb.append(     "'9999' as SPECIALDIV,");
                stb.append(     "cast(null as smallint) as CLASS_ORDER,");
                stb.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
                stb.append(     "ANNUAL,");
                stb.append(     "CAST(NULL AS VARCHAR(1)) AS YEAR,");
                stb.append(     "'ZZ' AS CLASSCD," + total + " AS CLASSNAME,");
                if (withSubclassnameJp) {
                    stb.append(     "" + total + " AS SUBCLASSNAME_JP,");
                }
                stb.append(     "'ZZZZ' AS SUBCLASSCD," + total + " AS SUBCLASSNAME,");
                if (withSubclassnameJp) {
                    stb.append(     "" + total + " AS SUBCLASSNAME_JP,");
                }
                stb.append(     "0 AS GRADES,");
                if (_hyoutei.equals("grade")) {
                    stb.append( "ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1) AS AVG_GRADES,");
                } else {
                    stb.append( "0 AS AVG_GRADES,");
                }
                stb.append(     "'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,");
                stb.append(     "SUM(" + h_1_3 + ") AS CREDIT, MAX(D065FLG) AS D065FLG ");
                stb.append( "FROM ");
                stb.append(     "STUDYREC T1 ");
                stb.append( "WHERE ");
                stb.append(     "CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
                stb.append( "GROUP BY ");
                stb.append(     "ANNUAL ");
            }

            //  全体の修得単位数・全体の評定平均値
            stb.append(     "UNION SELECT ");
            stb.append(     "'9999' as SPECIALDIV,");
            stb.append(     "cast(null as smallint) as CLASS_ORDER,");
            stb.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
            stb.append(         "'0' AS ANNUAL,");
            stb.append(         "CAST(NULL AS VARCHAR(1)) AS YEAR,");
            stb.append(         "'ZZ' AS CLASSCD," + total + " AS CLASSNAME,");
            if (withSubclassnameJp) {
                stb.append(     "" + total + " AS CLASSNAME_JP,");
            }
            stb.append(         "'ZZZZ' AS SUBCLASSCD,");
            if (_atype.equals("on")) {                //----->特Ａフラグ出力の有無
                stb.append(     "CASE VALUE(MAX(T2.COMMENTEX_A_CD),'0') WHEN '1' THEN '○' ELSE '  ' END AS SUBCLASSNAME,");
            } else {
                stb.append(     " " + total + " AS SUBCLASSNAME,");
            }
            if (withSubclassnameJp) {
                stb.append(     "" + total + " AS SUBCLASSNAME_JP,");
            }
            stb.append(         "0 AS GRADES,");

            if (_hyoutei.equals(hyouteiDefault)) {           //----->評定の出力有無
                stb.append(     "0 AS AVG_GRADES,");
                stb.append(     "'' AS ASSESS_LEVEL,");
            } else {
                stb.append(     "ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1) AS AVG_GRADES,");
                stb.append(     "(SELECT    ST2.ASSESSMARK ");
                stb.append(      "FROM      ASSESS_MST ST2 ");
                stb.append(      "WHERE     ST2.ASSESSCD='4' ");
                stb.append(                 "AND DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1),5,1) ");
                stb.append(                         "BETWEEN ST2.ASSESSLOW AND ST2.ASSESSHIGH) AS ASSESS_LEVEL,");
            }
            stb.append(         "0 AS GRADE_CREDIT,");
            stb.append(         "SUM(" + h_1_3 + ") AS CREDIT, MAX(D065FLG) AS D065FLG ");
            stb.append(     "FROM ");
            stb.append(         "STUDYREC T1 ");
            if (_atype.equals("on")) {                //----->特Ａフラグ出力の有無
                stb.append(     "LEFT JOIN HEXAM_ENTREMARK_HDAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            }
            stb.append(     "WHERE ");
            stb.append(         "CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' ");
            stb.append( "ORDER BY SPECIALDIV, CLASS_ORDER, CLASSCD, SUBCLASS_ORDER, SUBCLASSCD, ANNUAL");

        } catch (Exception ex) {
            log.error("pre_sql read error!", ex);
            log.debug("studyrecsql = " + stb.toString());
        }
        return stb.toString();
    }//public pre_sqlの括り

    /**
     *  在校生用と卒業生用のテーブル名を設定
     *  2005/07/25 Build 在校生用と卒業生用で共有する
     */
    public void setFieldName() {
        if ("1".equals(_config.get(CONFIG_PRINT_GRD))) {
            tname1 = "GRD_STUDYREC_DAT";
            tname2 = "GRD_TRANSFER_DAT";
            tname3 = "GRD_REGD_DAT";
        } else {
            tname1 = "SCHREG_STUDYREC_DAT";
            tname2 = "SCHREG_TRANSFER_DAT";
            tname3 = "SCHREG_REGD_DAT";
        }
        log.debug("table1->"+tname1+"   tname2->"+tname2+"   tname3->"+tname3);
    }
}
