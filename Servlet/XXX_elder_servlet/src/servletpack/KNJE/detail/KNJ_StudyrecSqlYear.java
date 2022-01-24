// kanji=漢字
/*
 * $Id: 3c1f731d03e01c191b751d53f189a92f42d483f0 $
 *
 * 作成日: 2006/09/06
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE.detail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;

/**
 * 学習記録データSQL作成
 * ○String hyouteiで評定の表記方法を設定。"grade"は学年別評定有。"on"は評定'１'の読替有。"off"は評定'１'の読替無。"hyde"は無。
 * ○String atypeで特Ａ付きの表記方法を設定。"on"は有。"hyde"(未使用)は無。
 * ○int stypeは学年別集計方法を設定。'1'より大きい場合、総合的な学習の時間・留学・教科の各修得単位と教科評定の学年別集計有。
 * ○boolean englishがTrueの場合は英語の名称。
 * ○int _daiken_div_codeは大検の表示方法を設定。'1'の場合は教科と同様に処理(別枠としない)。
 * ○int _shidouyourokuが'1'の場合は指導要録。
 */

public class KNJ_StudyrecSqlYear {

    private static final Log log = LogFactory.getLog(KNJ_StudyrecSqlYear.class);

    public String hyoutei;              //評定の読替え  １を２と評定
    public String atype;                //特Ａ付き
    public int stype;                   //総合的な学習の時間、留学単位、修得単位の集計区分
    public boolean english;             //英語版
    public String tname1 = null;        //SCHREG_STUDYREC_DAT
    public String tname2 = null;        //SCHREG_TRANSFER_DAT
    public String tname3 = null;        //SCHREG_REGD_DAT
    public KNJDefineCode definecode; //各学校における定数等設定
    public int _daiken_div_code;  // 大検の集計方法
    public int _shidouyouroku;  // 指導要録の集計方法
    public String _useCurriculumcd; // 教育課程コード

    /**
     * コンストラクタ。
     * @param hyoutei:評定の表記方法を設定。"grade"は学年別評定有。"on"は評定'１'の読替有。"off"は評定'１'の読替無。"hyde"は無。
     * @param atype:特Ａ付きの表記方法を設定。"on"は有。"hyde"(未使用)は無。
     * @param stype:学年別集計方法を設定。'1'より大きい場合、総合的な学習の時間・留学・教科の各修得単位と教科評定の学年別集計有。
     * @param english:Trueの場合は英語の名称。
     * @param definecode
     * @param _daiken_div_code:大検の表示方法を設定。'1'の場合は教科と同様に処理(別枠としない)。
     * @param _shidouyouroku:'1'の場合は指導要録。
     */
    public KNJ_StudyrecSqlYear(
            final String hyoutei,
            final String atype,
            final int stype,
            final boolean english,
            final KNJDefineCode definecode,
            final int _daiken_div_code,
            final int _shidouyouroku,
            final String useCurriculumcd
    ) {
        this.hyoutei = hyoutei;
        this.atype = atype;
        this.stype = stype;
        this.english = english;
        this.definecode = definecode;
        this._daiken_div_code = _daiken_div_code;
        this._shidouyouroku = _shidouyouroku;
        _useCurriculumcd = useCurriculumcd;
        log.fatal(" $Revision: 56595 $");
    }

    /**
     * 学習記録のSQL
     */
    public String pre_sql() {

        if( tname1 == null )setFieldName();   //使用テーブル名設定
        StringBuffer sql = new StringBuffer();
        // 評定１を２と判定
        String h_1_2 = null;
        String h_1_3 = null;
        if( hyoutei.equals("on") ){ //----->評定読み替えのON/OFF  評定１を２と読み替え
            h_1_2 = "CASE VALUE(T1.GRADES,0) WHEN 1 THEN 2 ELSE T1.GRADES END ";
            h_1_3 = "T1.CREDIT ";
        } else{
            h_1_2 = "T1.GRADES ";
            h_1_3 = "T1.CREDIT ";
        }

        // 該当生徒の成績データ表
        if (_shidouyouroku == 1) {
            sql.append(pre_sqlShidouyouroku());  // 指導要録仕様の学習記録データの抽出
        } else {
            sql.append(pre_sqlChosasho());  // 調査書仕様の学習記録データの抽出
        }
//        sql.append("WITH STUDYREC AS(");
//        if( 3 <= definecode.schoolmark.length()  &&  definecode.schoolmark.equals("KIN") )
//            //近大付属は評価読替元科目はココで除外する
//            sql.append( pre_sql_Replace() );
//        else
//            sql.append( pre_sql_Common() );
//        sql.append(    ")");

        // 該当生徒の科目評定、修得単位及び教科評定平均
        sql.append( "SELECT ");
        if (definecode.schooldiv.equals("1")) {
            sql.append(     "T1.YEAR AS ANNUAL,");
        } else {
            sql.append(     "T1.ANNUAL AS ANNUAL,");
        }
        sql.append(     "T1.CLASSCD,");
        if ("1".equals(_useCurriculumcd)) {
            sql.append("       T1.SCHOOL_KIND, ");
            sql.append("       T1.CURRICULUM_CD, ");
        }
        if (!english) {                      //----->教科名 英語/日本語
            sql.append( "CASE WHEN T1.CLASSNAME IS NOT NULL THEN T1.CLASSNAME ELSE T2.CLASSNAME END AS CLASSNAME,");
        } else {
            sql.append( "CASE WHEN T1.CLASSNAME_ENG IS NOT NULL THEN T1.CLASSNAME_ENG ELSE T2.CLASSNAME_ENG END AS CLASSNAME,");
        }
        sql.append(     "T1.SUBCLASSCD,");
        if (!english) {                      //----->科目名 英語/日本語
            sql.append( "CASE WHEN T1.SUBCLASSNAME IS NOT NULL THEN T1.SUBCLASSNAME ");
            sql.append( "     WHEN T3.SUBCLASSORDERNAME1 IS NOT NULL THEN T3.SUBCLASSORDERNAME1 ELSE T3.SUBCLASSNAME END AS SUBCLASSNAME,");
        } else {
            sql.append( "CASE WHEN T1.SUBCLASSNAME_ENG IS NOT NULL THEN T1.SUBCLASSNAME_ENG ELSE T3.SUBCLASSNAME_ENG END AS CLASSNAME,");
        }
        if (hyoutei.equals("hyde")) {        //----->評定の出力有無
            sql.append( "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,");
        } else {
            sql.append( h_1_2 + " AS GRADES,");
            sql.append( "T5.AVG_GRADES,'' AS ASSESS_LEVEL,");
        }
        sql.append(     "T1.CREDIT AS GRADE_CREDIT,T4.CREDIT ");
        sql.append("    ,T1.SCHOOLCD ");
        if (_shidouyouroku == 1) {
            sql.append(",CASE WHEN T2.SHOWORDER IS NOT NULL THEN T2.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");  // 表示順教科
            sql.append(",CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERSUBCLASS ");  // 表示順科目
        }
        sql.append( "FROM ");
        sql.append(     "STUDYREC T1 ");
        sql.append(     "INNER JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
        if ("1".equals(_useCurriculumcd)) {
            sql.append("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            sql.append("       AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
//        sql.append(     "INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
        //  修得単位数の計
        sql.append(     "INNER JOIN(SELECT ");
        sql.append(             "CLASSCD");
        if ("1".equals(_useCurriculumcd)) {
            sql.append("       ,SCHOOL_KIND");
            sql.append("       ,CURRICULUM_CD");
        }
        sql.append(             ",SUBCLASSCD,SUM(" + h_1_3 + ") AS CREDIT ");
        sql.append(         "FROM ");
        sql.append(             "STUDYREC T1 ");
        sql.append(         "WHERE ");
        sql.append(             "CLASSCD BETWEEN '"+KNJDefineCode.subject_D+"' AND '"+KNJDefineCode.subject_U+"' ");
        sql.append(         "GROUP BY ");
        sql.append(             "CLASSCD,");
        if ("1".equals(_useCurriculumcd)) {
            sql.append("       SCHOOL_KIND,");
            sql.append("       CURRICULUM_CD,");
        }
        sql.append(             "SUBCLASSCD ");
        sql.append(     ")T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_useCurriculumcd)) {
            sql.append("       AND T4.CLASSCD = T1.CLASSCD ");
            sql.append("       AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            sql.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        if( !(hyoutei.equals("hyde")) ){        //----->評定の出力有無
            //  各教科の評定平均値
            sql.append( "INNER JOIN(SELECT ");
            sql.append(         "CLASSCD,");
            if ("1".equals(_useCurriculumcd)) {
                sql.append("       SCHOOL_KIND, ");
                sql.append("       CURRICULUM_CD, ");
            }
            sql.append(         "DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1),5,1) AS AVG_GRADES ");
            sql.append(     "FROM ");
            sql.append(         "STUDYREC T1 ");
            sql.append(     "WHERE ");
            sql.append(         "CLASSCD BETWEEN '"+KNJDefineCode.subject_D+"' AND '"+KNJDefineCode.subject_U+"' ");
            sql.append(     "GROUP BY ");
            sql.append(         "CLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                sql.append("       ,SCHOOL_KIND");
                sql.append("       ,CURRICULUM_CD");
            }
            sql.append( ")T5 ON T5.CLASSCD = T1.CLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                sql.append("       AND T5.CLASSCD = T1.CLASSCD ");
                sql.append("       AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
                sql.append("       AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
        }
        sql.append(     "LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_useCurriculumcd)) {
            sql.append("       AND T3.CLASSCD = T1.CLASSCD ");
            sql.append("       AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            sql.append("       AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        sql.append( "WHERE ");
        sql.append(     "T1.CLASSCD BETWEEN '"+KNJDefineCode.subject_D+"' AND '"+KNJDefineCode.subject_U+"' ");

        // 総合学習の修得単位数（学年別）
        if( stype>1 ){          //----->学年別
            sql.append("UNION SELECT ");
            if (definecode.schooldiv.equals("1")) {
                sql.append( "YEAR AS ANNUAL,");
            } else {
                sql.append( "ANNUAL AS ANNUAL,");
            }           
            sql.append("'"+KNJDefineCode.subject_T+"' AS CLASSCD,");
            if ("1".equals(_useCurriculumcd)) {
                sql.append("       '"+KNJDefineCode.subject_T+"' AS SCHOOL_KIND, ");
                sql.append("       '"+KNJDefineCode.subject_T+"' AS CURRICULUM_CD, ");
            }
            sql.append("'sogo' AS CLASSNAME,'"+KNJDefineCode.subject_T+"01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,");
            sql.append(     "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ");
            sql.append("   ,'0' AS SCHOOLCD ");
            if (_shidouyouroku == 1) {
                sql.append(",CASE WHEN T2.SHOWORDER IS NOT NULL THEN T2.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");  // 表示順教科
                sql.append(",CASE WHEN T2.SHOWORDER IS NOT NULL THEN 0 ELSE 0 END AS SHOWORDERSUBCLASS ");  // 表示順科目
            }
            sql.append( "FROM ");
            sql.append(     "STUDYREC T1 ");
            if (_shidouyouroku == 1) {
                if ("1".equals(_useCurriculumcd)) {
                    sql.append( "LEFT JOIN ( ");
                    sql.append( " SELECT CLASSCD, MIN(SHOWORDER) AS SHOWORDER FROM CLASS_MST GROUP BY CLASSCD ");
                    sql.append(" ) T2 ON T2.CLASSCD = T1.CLASSCD ");
                } else {
                    sql.append( "LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                }
            }
            sql.append( "WHERE ");
            sql.append(     "T1.CLASSCD = '"+KNJDefineCode.subject_T+"' ");
            sql.append( "GROUP BY ");
            if (definecode.schooldiv.equals("1")) {
                sql.append( "YEAR ");
            } else {
                sql.append( "ANNUAL ");
            }           
            if (_shidouyouroku == 1) {
                sql.append(",T2.SHOWORDER ");
            }
        }

        // 総合学習の修得単位数（合計）
        sql.append( "UNION SELECT ");
        sql.append(         "'0' AS ANNUAL,'"+KNJDefineCode.subject_T+"' AS CLASSCD,");
        if ("1".equals(_useCurriculumcd)) {
            sql.append("       '"+KNJDefineCode.subject_T+"' AS SCHOOL_KIND,");
            sql.append("       '"+KNJDefineCode.subject_T+"' AS CURRICULUM_CD,");
        }
        sql.append(         "'sogo' AS CLASSNAME,'"+KNJDefineCode.subject_T+"01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,");
        sql.append(         "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ");
        sql.append("       ,'0' AS SCHOOLCD ");
        if (_shidouyouroku == 1) {
            sql.append(    ",CASE WHEN T2.SHOWORDER IS NOT NULL THEN T2.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");  // 表示順教科
            sql.append(    ",CASE WHEN T2.SHOWORDER IS NOT NULL THEN 0 ELSE 0 END AS SHOWORDERSUBCLASS ");  // 表示順科目
        }
        sql.append(     "FROM ");
        sql.append(         "STUDYREC T1 ");
        if (_shidouyouroku == 1) {
            if ("1".equals(_useCurriculumcd)) {
                sql.append( "LEFT JOIN ( ");
                sql.append( " SELECT CLASSCD, MIN(SHOWORDER) AS SHOWORDER FROM CLASS_MST GROUP BY CLASSCD ");
                sql.append(" ) T2 ON T2.CLASSCD = T1.CLASSCD ");
            } else {
                sql.append( "LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            }
        }
        sql.append(     "WHERE ");
        sql.append(         "T1.CLASSCD = '"+KNJDefineCode.subject_T+"' ");
        if (_shidouyouroku == 1) {
            sql.append( "GROUP BY T2.SHOWORDER ");
        }
        
        // 留学中の修得単位数（学年別）
        sql.append(pre_sqlAbraod());

        // 修得単位数、評定平均（学年別）
        if( stype>1 ){          //----->学年別
            sql.append( "UNION SELECT ");
            if (definecode.schooldiv.equals("1")) {
                sql.append( "YEAR AS ANNUAL,");
            } else {
                sql.append( "ANNUAL AS ANNUAL,");
            }           
            sql.append(     "'ZZ' AS CLASSCD,");
            if ("1".equals(_useCurriculumcd)) {
                sql.append("       'ZZ' AS SCHOOL_KIND,");
                sql.append("       'ZZ' AS CURRICULUM_CD,");
            }
            sql.append(     "'total' AS CLASSNAME,");
            sql.append(     "'ZZZZ' AS SUBCLASSCD,'total' AS SUBCLASSNAME,");
            sql.append(     "0 AS GRADES,");
            if( hyoutei.equals("grade") )
                sql.append( "ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1) AS AVG_GRADES,");
            else
                sql.append( "0 AS AVG_GRADES,");
            sql.append(     "'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,");
            sql.append(     "SUM(" + h_1_3 + ") AS CREDIT ");
            sql.append("   ,'0' AS SCHOOLCD ");
            if (_shidouyouroku == 1) {
                sql.append(",0 AS SHOWORDERCLASS ");  // 表示順教科
                sql.append(",0 AS SHOWORDERSUBCLASS ");  // 表示順科目
            }
            sql.append( "FROM ");
            sql.append(     "STUDYREC T1 ");
            sql.append( "WHERE ");
            sql.append(     "CLASSCD BETWEEN '"+KNJDefineCode.subject_D+"' AND '"+KNJDefineCode.subject_U+"' ");
            sql.append( "GROUP BY ");
            if (definecode.schooldiv.equals("1")) {
                sql.append( "YEAR ");
            } else {
                sql.append( "ANNUAL ");
            }           
        }

        // 全体の修得単位数・全体の評定平均値
        sql.append(     "UNION SELECT ");
        sql.append(         "'0' AS ANNUAL,'ZZ' AS CLASSCD,");
        if ("1".equals(_useCurriculumcd)) {
            sql.append("       'ZZ' AS SCHOOL_KIND,");
            sql.append("       'ZZ' AS CURRICULUM_CD,");
        }
        sql.append(         "'total' AS CLASSNAME,'ZZZZ' AS SUBCLASSCD,");
        if( atype.equals("on") )                //----->特Ａフラグ出力の有無
            sql.append(     "CASE VALUE(MAX(T2.COMMENTEX_A_CD),'0') WHEN '1' THEN '○' ELSE '  ' END AS SUBCLASSNAME,");
        else
            sql.append(     "'total' AS SUBCLASSNAME,");
        sql.append(         "0 AS GRADES,");

        if( hyoutei.equals("hyde") ){           //----->評定の出力有無
            sql.append(     "0 AS AVG_GRADES,");
            sql.append(     "'' AS ASSESS_LEVEL,");
        } else{
            sql.append(     "ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1) AS AVG_GRADES,");
            sql.append(     "(SELECT    ST2.ASSESSMARK ");
            sql.append(      "FROM      ASSESS_MST ST2 ");
            sql.append(      "WHERE     ST2.ASSESSCD='4' ");
            sql.append(                 "AND DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1),5,1) ");
            sql.append(                         "BETWEEN ST2.ASSESSLOW AND ST2.ASSESSHIGH) AS ASSESS_LEVEL,");
        }
        sql.append(         "0 AS GRADE_CREDIT,");
        sql.append(         "SUM(" + h_1_3 + ") AS CREDIT ");
        sql.append("       ,'0' AS SCHOOLCD ");
        if (_shidouyouroku == 1) {
            sql.append(",0 AS SHOWORDERCLASS ");  // 表示順教科
            sql.append(",0 AS SHOWORDERSUBCLASS ");  // 表示順科目
        }
        sql.append(     "FROM ");
        sql.append(         "STUDYREC T1 ");
        if( atype.equals("on") )                //----->特Ａフラグ出力の有無
            sql.append(     "LEFT JOIN HEXAM_ENTREMARK_HDAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        sql.append(     "WHERE ");
        sql.append(         "CLASSCD BETWEEN '"+KNJDefineCode.subject_D+"' AND '"+KNJDefineCode.subject_U+"' ");

        // 前籍校における修得単位（レコードがある場合のみ）
        if (_shidouyouroku == 0) {
            sql.append("     UNION SELECT"); 
            sql.append("         '0' AS ANNUAL");
            sql.append("        ,'ZB' AS CLASSCD");
            if ("1".equals(_useCurriculumcd)) {
                sql.append("       'ZB' AS SCHOOL_KIND,");
                sql.append("       'ZB' AS CURRICULUM_CD,");
            }
            sql.append("        ,'zenseki' AS CLASSNAME");
            sql.append("        ,'ZZZB' AS SUBCLASSCD");
            sql.append("        ,'zenseki' AS SUBCLASSNAME");
            sql.append("        ,0 AS GRADES");
            sql.append("        ,0 AS AVG_GRADES");
            sql.append("        ,'' ASSESS_LEVEL");
            sql.append("        ,0 AS GRADE_CREDIT");
            sql.append("        ,S1.CREDIT ");
            sql.append("       ,'1' AS SCHOOLCD ");
            sql.append("     FROM(");
            sql.append("         SELECT SCHREGNO,SUM(T1.CREDIT ) AS CREDIT ");
            sql.append("         FROM(SELECT T1.SCHREGNO,CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT");
            sql.append("              FROM   SCHREG_STUDYREC_DAT T1");
            sql.append("              WHERE  T1.SCHREGNO = ? AND (T1.SCHOOLCD = '1' OR T1.YEAR = '0')");
            sql.append("         )T1");
            sql.append("         GROUP BY T1.SCHREGNO");
            sql.append("         HAVING T1.SCHREGNO IS NOT NULL");
            sql.append("     )S1");
        }
        
        // 大検における認定単位（レコードがある場合のみ）
        if (_shidouyouroku == 0) {
            sql.append("     UNION SELECT"); 
            sql.append("         '0' AS ANNUAL");
            sql.append("        ,'ZA' AS CLASSCD");
            if ("1".equals(_useCurriculumcd)) {
                sql.append("       'ZA' AS SCHOOL_KIND,");
                sql.append("       'ZA' AS CURRICULUM_CD,");
            }
            sql.append("        ,'daiken' AS CLASSNAME");
            sql.append("        ,'ZZZA' AS SUBCLASSCD");
            sql.append("        ,'daiken' AS SUBCLASSNAME");
            sql.append("        ,0 AS GRADES");
            sql.append("        ,0 AS AVG_GRADES");
            sql.append("        ,'' ASSESS_LEVEL");
            sql.append("        ,0 AS GRADE_CREDIT");
            sql.append("        ,S1.CREDIT ");
            sql.append("       ,'2' AS SCHOOLCD ");
            sql.append("     FROM(");
            sql.append("         SELECT SCHREGNO,SUM(T1.CREDIT ) AS CREDIT ");
            sql.append("         FROM(SELECT T1.SCHREGNO,CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT");
            sql.append("              FROM   SCHREG_STUDYREC_DAT T1");
            sql.append("              WHERE  T1.SCHREGNO = ? AND T1.SCHOOLCD = '2'");
            sql.append("         )T1");
            sql.append("         GROUP BY T1.SCHREGNO");
            sql.append("         HAVING T1.SCHREGNO IS NOT NULL");
            sql.append("     )S1");
        }
        
        sql.append(" ORDER BY ");
        if (_shidouyouroku == 1) {
            sql.append(" SHOWORDERCLASS,");
        }
        sql.append(" CLASSCD,");
        if ("1".equals(_useCurriculumcd)) {
            sql.append("  SCHOOL_KIND,");
            sql.append("  CURRICULUM_CD,");
        }
        if (_shidouyouroku == 1) {
            sql.append(" SHOWORDERSUBCLASS,");
        }
        sql.append(" SUBCLASSCD, ");
        sql.append(" ANNUAL");
        return sql.toString();
    }

    /**
     * @return 留学単位のSQLを戻します。
     */
    protected String pre_sqlAbraod() {
        StringBuffer stb = new StringBuffer();
        if( stype>1 ){              //----->学年別/合計
            stb.append( "UNION SELECT ");
            if (definecode.schooldiv.equals("1")) {
                stb.append( "YEAR AS ANNUAL,");
            } else {
                stb.append( "ANNUAL AS ANNUAL,");
            }           
            stb.append(     "'AA' AS CLASSCD, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("       'AA' AS SCHOOL_KIND,");
                stb.append("       'AA' AS CURRICULUM_CD,");
            }
            stb.append(     "'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,");
            stb.append(     "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ");
            stb.append("   ,'0' AS SCHOOLCD ");
            if (_shidouyouroku == 1) {
                stb.append(",0 AS SHOWORDERCLASS ");  // 表示順教科
                stb.append(",0 AS SHOWORDERSUBCLASS ");  // 表示順科目
            }
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
            if (definecode.schooldiv.equals("1")) {
                stb.append(         "YEAR ");
            } else {
                stb.append(         "ANNUAL,MAX(YEAR) AS YEAR ");
            }           
            stb.append(         "FROM ");
            stb.append(              tname3 + " ");
            stb.append(         "WHERE ");
            stb.append(             "SCHREGNO =? AND YEAR <=? ");
            stb.append(         "GROUP BY ");
            if (definecode.schooldiv.equals("1")) {
                stb.append(         "YEAR ");
            } else {
                stb.append(         "ANNUAL ");
            }           
            stb.append(         ")ST2 ");
            stb.append( "WHERE ");
            stb.append(     "ST1.TRANSFER_YEAR <=? ");
            stb.append(     "and INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
            stb.append( "GROUP BY ");
            if (definecode.schooldiv.equals("1")) {
                stb.append( "YEAR ");
            } else {
                stb.append( "ANNUAL ");
            }           
        }
        // 留学中の修得単位数（合計）
        stb.append(     "UNION SELECT ");
        stb.append(         "'0' AS ANNUAL,'AA' AS CLASSCD,'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,");
        stb.append(         "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ");
        stb.append("       ,'0' AS SCHOOLCD ");
        if (_shidouyouroku == 1) {
            stb.append(",0 AS SHOWORDERCLASS ");  // 表示順教科
            stb.append(",0 AS SHOWORDERSUBCLASS ");  // 表示順科目
        }
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
        return stb.toString();
    }

    /*
     *  調査書における学習記録データ取得のSQL
     */
     protected String pre_sqlChosasho() {
         if (log.isDebugEnabled()) { log.debug("String pre_sqlChosasho()"); }
         StringBuffer stb = new StringBuffer();
         stb.append(" WITH STUDYREC AS(");
         if (definecode.schoolmark.equals("KIN") || definecode.schoolmark.equals("KINJUNIOR")) {
             stb.append(pre_sql_Replace());
         } else {
             stb.append(pre_sql_Common());
         }
         stb.append(" )");
         return stb.toString();
     }
     
   /*
    *  指導要録における学習記録データ取得のSQL
    */
    protected String pre_sqlShidouyouroku() {
       if (log.isDebugEnabled()) { log.debug("String pre_sqlShidouyouroku()"); }
       StringBuffer stb = new StringBuffer();
       stb.append(" WITH DATA AS(");
       if (definecode.schoolmark.equals("KIN") || definecode.schoolmark.equals("KINJUNIOR")) {
           stb.append(pre_sql_Replace());
       } else {
           stb.append(pre_sql_Common());
       }
       stb.append(" )");
       stb.append(" ,STUDYREC AS(");
       stb.append(" SELECT SCHREGNO");
       stb.append("       ,CASE WHEN INT(ANNUAL) = 0 THEN '00' WHEN INT(YEAR) = 0 THEN '00' ELSE YEAR END AS YEAR");
       stb.append("       ,CASE WHEN INT(ANNUAL) = 0 THEN '00' WHEN INT(YEAR) = 0 THEN '00' ELSE ANNUAL END AS ANNUAL");
       stb.append("       ,CLASSCD");
       if ("1".equals(_useCurriculumcd)) {
           stb.append("       ,SCHOOL_KIND");
           stb.append("       ,CURRICULUM_CD");
       }
       stb.append("       ,SUBCLASSCD");
       stb.append("       ,ROUND(AVG(FLOAT(GRADES)),0) AS GRADES");
       stb.append("       ,SUM(CREDIT) AS CREDIT");
       stb.append("       ,MIN(CLASSNAME) AS CLASSNAME");
       stb.append("       ,MIN(CLASSNAME_ENG) AS CLASSNAME_ENG");
       stb.append("       ,MIN(SUBCLASSNAME) AS SUBCLASSNAME");
       stb.append("       ,MIN(SUBCLASSNAME_ENG) AS SUBCLASSNAME_ENG");
       stb.append("       ,'0' AS SCHOOLCD");
       stb.append(" FROM DATA");
       stb.append(" GROUP BY SCHREGNO,YEAR,ANNUAL,CLASSCD");
       if ("1".equals(_useCurriculumcd)) {
           stb.append("       ,SCHOOL_KIND");
           stb.append("       ,CURRICULUM_CD");
       }
       stb.append("       ,SUBCLASSCD ");
       stb.append(" )");
       return stb.toString();
   }

   /*
     *
     *  学習記録データ取得のSQL
     *  2005/07/08 Build 
     *
     */
    protected String pre_sql_Common() {
        log.debug("String pre_sql_Common() ");
        StringBuffer stb = new StringBuffer();
        stb.append("SELECT  SCHREGNO, YEAR, ANNUAL, CLASSCD ");
        if ("1".equals(_useCurriculumcd)) {
            stb.append("       ,SCHOOL_KIND");
            stb.append("       ,CURRICULUM_CD");
        }
        stb.append(        "SUBCLASSCD, VALUATION AS GRADES ");
        stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
        stb.append("       ,CLASSNAME,CLASSNAME_ENG,SUBCLASSNAME,SUBCLASSNAME_ENG ");
        stb.append("       ,SCHOOLCD ");
        stb.append("FROM   " + tname1 + " T1 ");
        stb.append("WHERE   T1.SCHREGNO = ? AND YEAR <= ?");
        stb.append("    AND (CLASSCD BETWEEN '" + KNJDefineCode.subject_D+"' AND '" + KNJDefineCode.subject_U+"' OR CLASSCD = '" + KNJDefineCode.subject_T+"') ");
        if (_shidouyouroku == 0) {
            stb.append("AND T1.SCHOOLCD = '0' ");  // 本校区分が本校のみ。
        }
        if (_daiken_div_code == 1) {
            stb.append("UNION ");
            stb.append("SELECT  SCHREGNO, YEAR, ANNUAL, CLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("       ,SCHOOL_KIND");
                stb.append("       ,CURRICULUM_CD");
            }
            stb.append(       ", SUBCLASSCD, VALUATION AS GRADES ");
            stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
            stb.append("       ,CLASSNAME,CLASSNAME_ENG,SUBCLASSNAME,SUBCLASSNAME_ENG ");
            stb.append("       ,SCHOOLCD ");
            stb.append("FROM   " + tname1 + " T1 ");
            stb.append("WHERE   T1.SCHREGNO = ? AND YEAR <= ?");
            stb.append("    AND T1.SCHOOLCD = '2' ");  // 本校区分が資格
            stb.append("    AND T1.GET_CREDIT IS NOT NULL ");
        }
        return stb.toString();
    }

    /*
     *
     *  学習記録データ取得のSQL 評定読替科目を除外する記述を組み込む
     *  2005/07/08 Build 
     *
     */
    protected String pre_sql_Replace() {
        log.debug("String pre_sql_Replace() ");
        StringBuffer stb = new StringBuffer();
        stb.append("SELECT  SCHREGNO, YEAR, ANNUAL, CLASSCD ");
        if ("1".equals(_useCurriculumcd)) {
            stb.append("       ,SCHOOL_KIND");
            stb.append("       ,CURRICULUM_CD");
        }
        stb.append(       ",SUBCLASSCD, VALUATION AS GRADES ");
        stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
        stb.append("       ,CLASSNAME,CLASSNAME_ENG,SUBCLASSNAME,SUBCLASSNAME_ENG ");
        stb.append("       ,SCHOOLCD ");
        stb.append("FROM   " + tname1 + " T1 ");
        stb.append("WHERE   T1.SCHREGNO = ? AND ");
        stb.append(        "YEAR <= ? AND ");
        stb.append(        "(CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR CLASSCD = '" + KNJDefineCode.subject_T + "') AND ");
        stb.append(        "NOT EXISTS(SELECT  'X' ");
        stb.append(                   "FROM    SUBCLASS_REPLACE_COMBINED_DAT T2 ");
        stb.append(                   "WHERE   T2.YEAR = T1.YEAR AND ");
        if ("1".equals(_useCurriculumcd)) {
            stb.append(                           "T2.ATTEND_CLASSCD = T1.CLASSCD AND ");
            stb.append(                           "T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ");
            stb.append(                           "T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ");
        }
        stb.append(                           "T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
        return stb.toString();
    }

    /**
     * 在校生用と卒業生用のテーブル名を設定
     */
    public void setFieldName() {
        tname1 = "SCHREG_STUDYREC_DAT";
        tname2 = "SCHREG_TRANSFER_DAT";
        tname3 = "SCHREG_REGD_DAT";
        log.debug("table1->"+tname1+"   tname2->"+tname2+"   tname3->"+tname3);
    }
}
