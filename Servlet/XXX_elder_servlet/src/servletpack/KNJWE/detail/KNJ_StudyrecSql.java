// kanji=漢字
/*
 * $Id: 2e4d2a622a3ebf3d995a30b7ae1c03eb1fd87115 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2009-2015 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJWE.detail;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.WithusUtils;

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

//NO001 public class KNJ_StudyrecSql implements KNJ_ClassCode{
public class KNJ_StudyrecSql{

    private static final Log log = LogFactory.getLog(KNJ_StudyrecSql.class);

    final public String hyoutei;              //評定の読替え  １を２と評定
    final public String atype;                //特Ａ付き
    final public int stype;                   //総合的な学習の時間、留学単位、修得単位の集計区分
    final public boolean english;             //英語版
    final public boolean _isHosei;            //法政 // 08/03/06Add

    public String tname1 = null;        //05/07/25 SCHREG_STUDYREC_DAT
    public String tname2 = null;        //05/07/25 SCHREG_TRANSFER_DAT
    public String tname3 = null;        //05/07/25 SCHREG_REGD_DAT
    private KNJDefineCode definecode = new KNJDefineCode(); //各学校における定数等設定 05/07/08 Build  NO001

    public KNJ_StudyrecSql(){
        this("hyde", "hyde", 1, false, false);
    }

    public KNJ_StudyrecSql(String hyoutei,boolean english){
        this(hyoutei, "hyde", 1, english, false);
    }

    public KNJ_StudyrecSql(String hyoutei,String atype,int stype){
        this(hyoutei, atype, stype, false, false);
    }

    public KNJ_StudyrecSql( String hyoutei, boolean english, int stype ){    //--NO002
        this(hyoutei, "hyde", stype, english, false);
    }

    /**
     * コンストラクタ。
     * @param hyoutei on:評定１の読替有 off:評定１の読替無 hyde:出力無 grade:学年別評定有
     * @param atype on:特Ａ出力有 hyde:出力無
     * @param stype 1:合計のみ 2:合計&学年別 3:学年別のみ 0:出力無
     * @param english on:英語の出力
     * @param isHosei 法政の時、true
     */
    public KNJ_StudyrecSql(String hyoutei, String atype, int stype, boolean english, boolean isHosei){
        this.hyoutei = hyoutei;
        this.atype = atype;
        this.stype = stype;
        this.english = english;
        this._isHosei = isHosei;
    }

    public String getSchregStudyRec(final String certifkind) {
        final StringBuffer stb = new StringBuffer();

        StringBuffer where018 = new StringBuffer();
        if (certifkind.equals("018")) {
            String sep = "";
            where018.append(" IN (");
            for (final Iterator iter = WithusUtils.PHYSICAL_EDUCATIONS_NEW_LIST.iterator(); iter.hasNext();) {
                final String subClassCd = (String) iter.next();
                where018.append(sep + " ' " + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_NEW_CURRICULUM + subClassCd + " '");
                sep = ",";
            }
            for (final Iterator iter = WithusUtils.PHYSICAL_EDUCATIONS_LIST.iterator(); iter.hasNext();) {
                final String subClassCd = (String) iter.next();
                where018.append(sep + " ' " + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + WithusUtils.PHYSICAL_EDUCATION_CURRICULUM + subClassCd + " '");
                sep = ",";
            }
            where018.append(" ) ");
            stb.append(" WITH STUDY_REC AS ( ");
        }
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     CASE WHEN T1.CLASSNAME IS NOT NULL ");
        stb.append("          THEN T1.CLASSNAME ");
        stb.append("          ELSE CASE WHEN L1.CLASSORDERNAME1 IS NOT NULL ");
        stb.append("               THEN L1.CLASSORDERNAME1 ");
        stb.append("               ELSE L1.CLASSNAME ");
        stb.append("         END ");
        stb.append("     END AS CLASSNAME, ");
        stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L1.SHOWORDER2 ");
        stb.append("          ELSE 999 ");
        stb.append("     END AS CLASSORDER2, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ");
        stb.append("          THEN T1.SUBCLASSNAME ");
        stb.append("          ELSE CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
        stb.append("               THEN L2.SUBCLASSORDERNAME1 ");
        stb.append("               ELSE L2.SUBCLASSNAME ");
        stb.append("         END ");
        stb.append("     END AS SUBCLASSNAME, ");
        stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L2.SHOWORDER2 ");
        stb.append("          ELSE 999 ");
        stb.append("     END AS SUBCLASSORDER2, ");
        stb.append("     L2.SUBCLASSCD2, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.SCHOOLCD, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GET_CREDIT, ");
        stb.append("     T1.VALUATION ");
        stb.append(" FROM ");
        stb.append("     SCHREG_STUDYREC_DAT T1 ");
        stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
        stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = ? ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.CLASSCD, ");
        stb.append("     CASE WHEN L1.CLASSORDERNAME1 IS NOT NULL ");
        stb.append("          THEN L1.CLASSORDERNAME1 ");
        stb.append("          ELSE L1.CLASSNAME ");
        stb.append("     END AS CLASSNAME, ");
        stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L1.SHOWORDER2 ");
        stb.append("          ELSE 999 ");
        stb.append("     END AS CLASSORDER2, ");
        stb.append("     T1.CURRICULUM_CD, ");
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ");
        stb.append("          THEN T1.SUBCLASSNAME ");
        stb.append("          ELSE CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
        stb.append("               THEN L2.SUBCLASSORDERNAME1 ");
        stb.append("               ELSE L2.SUBCLASSNAME ");
        stb.append("         END ");
        stb.append("     END AS SUBCLASSNAME, ");
        stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
        stb.append("          THEN L2.SHOWORDER2 ");
        stb.append("          ELSE 999 ");
        stb.append("     END AS SUBCLASSORDER2, ");
        stb.append("     L2.SUBCLASSCD2, ");
        stb.append("     L3.SCHREGNO, ");
        stb.append("     CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) AS SCHOOLCD, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GET_CREDIT, ");
        stb.append("     T1.VALUATION ");
        stb.append(" FROM ");
        stb.append("     ANOTHER_SCHOOL_GETCREDITS_DAT T1 ");
        stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
        stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
        stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
        stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L3 ON T1.APPLICANTNO = L3.APPLICANTNO ");
        stb.append(" WHERE ");
        stb.append("     L3.SCHREGNO = ? ");
        if (certifkind.equals("018")) {
            stb.append(" ), MAIN_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     '2' AS ORDERCD, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     L1.CLASSNAME, ");
            stb.append("     999 AS CLASSORDER2, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     L2.SUBCLASSNAME, ");
            stb.append("     999 AS SUBCLASSORDER2, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     '0' AS SCHOOLCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.COMP_CREDIT AS GET_CREDIT, ");
            stb.append("     CAST(NULL AS SMALLINT) AS VALUATION ");
            stb.append(" FROM ");
            stb.append("     COMP_REGIST_DAT T1 ");
            stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
            stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = ? ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append("     AND T1.YEAR || T1.CLASSCD || T1.CURRICULUM_CD || CASE WHEN L2.SUBCLASSCD2 IS NOT NULL AND T1.CLASSCD = '" + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + "' ");
            stb.append("                                                           THEN L2.SUBCLASSCD2 ");
            stb.append("                                                           ELSE T1.SUBCLASSCD ");
            stb.append("                                                      END ");
            stb.append("         NOT IN (SELECT ");
            stb.append("                     T2.YEAR || T2.CLASSCD || T2.CURRICULUM_CD || T2.SUBCLASSCD ");
            stb.append("                 FROM ");
            stb.append("                     STUDY_REC T2 ");
            stb.append("                 WHERE ");
            stb.append("                     T2.SCHOOLCD = '0' ");
            stb.append("                ) ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '1' AS ORDERCD, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.CLASSNAME, ");
            stb.append("     T1.CLASSORDER2, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SUBCLASSNAME, ");
            stb.append("     T1.SUBCLASSORDER2, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SCHOOLCD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.GET_CREDIT, ");
            stb.append("     T1.VALUATION ");
            stb.append(" FROM ");
            stb.append("     STUDY_REC T1 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     MAIN_T ");
            stb.append(" WHERE ");
            stb.append("     CLASSCD || CURRICULUM_CD || SUBCLASSCD NOT " + where018.toString());
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     ORDERCD, ");
            stb.append("     '" + WithusUtils.PHYSICAL_EDUCATION_CLASS_CD + "' AS CLASSCD, ");
            stb.append("     '体育' AS CLASSNAME, ");
            stb.append("     MAX(CLASSORDER2) AS CLASSORDER2, ");
            stb.append("     '" + WithusUtils.PHYSICAL_EDUCATION_CURRICULUM + "' AS CURRICULUM_CD, ");
            stb.append("     '" + WithusUtils.PHYSICAL_EDUCATION_SUBCLASS_CD + "' AS SUBCLASSCD, ");
            stb.append("     '体育' AS SUBCLASSNAME, ");
            stb.append("     MAX(SUBCLASSORDER2) AS SUBCLASSORDER2, ");
            stb.append("     SCHREGNO, ");
            stb.append("     SCHOOLCD, ");
            stb.append("     YEAR, ");
            stb.append("     SUM(GET_CREDIT) AS GET_CREDIT, ");
            stb.append("     SUM(VALUATION) AS VALUATION ");
            stb.append(" FROM ");
            stb.append("     MAIN_T ");
            stb.append(" WHERE ");
            stb.append("     CLASSCD || CURRICULUM_CD || SUBCLASSCD " + where018.toString());
            stb.append(" GROUP BY ");
            stb.append("     ORDERCD, ");
            stb.append("     SCHREGNO, ");
            stb.append("     SCHOOLCD, ");
            stb.append("     YEAR ");
        }
        if (certifkind.equals("018")) {
            stb.append(" ORDER BY ");
            stb.append("     ORDERCD, ");
            stb.append("     CLASSORDER2, ");
            stb.append("     CLASSCD, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     SUBCLASSORDER2, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     YEAR ");
        } else {
            stb.append(" ORDER BY ");
            stb.append("     CLASSORDER2, ");
            stb.append("     CLASSCD, ");
            stb.append("     SUBCLASSORDER2, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     YEAR ");
        }

        return stb.toString();
    }

    /**
    *
    *  学習記録のSQL
    *  2005/07/08 Modify 評定読替元の科目を対象としない処理を追加
    *
    */
   public String pre_sql_new(final String selectDiv){
       final StringBuffer stb = new StringBuffer();

       stb.append(" WITH STUDY_REC AS ( ");
       stb.append(" SELECT ");
       stb.append("     T1.CLASSCD, ");
       stb.append("     CASE WHEN T1.CLASSNAME IS NOT NULL ");
       stb.append("          THEN T1.CLASSNAME ");
       stb.append("          ELSE CASE WHEN L1.CLASSORDERNAME1 IS NOT NULL ");
       stb.append("               THEN L1.CLASSORDERNAME1 ");
       stb.append("               ELSE L1.CLASSNAME ");
       stb.append("         END ");
       stb.append("     END AS CLASSNAME, ");
       stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
       stb.append("          THEN L1.SHOWORDER2 ");
       stb.append("          ELSE 999 ");
       stb.append("     END AS CLASSORDER2, ");
       stb.append("     T1.CURRICULUM_CD, ");
       stb.append("     L2.SUBCLASSCD2, ");
       stb.append("     T1.SUBCLASSCD, ");
       stb.append("     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ");
       stb.append("          THEN T1.SUBCLASSNAME ");
       stb.append("          ELSE CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
       stb.append("               THEN L2.SUBCLASSORDERNAME1 ");
       stb.append("               ELSE L2.SUBCLASSNAME ");
       stb.append("         END ");
       stb.append("     END AS SUBCLASSNAME, ");
       stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
       stb.append("          THEN L2.SHOWORDER2 ");
       stb.append("          ELSE 999999 ");
       stb.append("     END AS SUBCLASSORDER2, ");
       stb.append("     T1.SCHREGNO, ");
       stb.append("     T1.SCHOOLCD, ");
       stb.append("     T1.YEAR, ");
       stb.append("     T1.GET_CREDIT AS GET_CREDIT, ");
       stb.append("     T1.VALUATION AS VALUATION ");
       stb.append(" FROM ");
       stb.append("     SCHREG_STUDYREC_DAT T1 ");
       stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
       stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
       stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
       stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
       stb.append(" WHERE ");
       stb.append("     T1.SCHREGNO = ? ");

       stb.append(" UNION ");
       stb.append(" SELECT ");
       stb.append("     T1.CLASSCD, ");
       stb.append("     CASE WHEN L1.CLASSORDERNAME1 IS NOT NULL ");
       stb.append("          THEN L1.CLASSORDERNAME1 ");
       stb.append("          ELSE L1.CLASSNAME ");
       stb.append("     END AS CLASSNAME, ");
       stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
       stb.append("          THEN L1.SHOWORDER2 ");
       stb.append("          ELSE 999 ");
       stb.append("     END AS CLASSORDER2, ");
       stb.append("     T1.CURRICULUM_CD, ");
       stb.append("     L2.SUBCLASSCD2, ");
       stb.append("     T1.SUBCLASSCD, ");
       stb.append("     CASE WHEN T1.SUBCLASSNAME IS NOT NULL ");
       stb.append("          THEN T1.SUBCLASSNAME ");
       stb.append("          ELSE CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
       stb.append("               THEN L2.SUBCLASSORDERNAME1 ");
       stb.append("               ELSE L2.SUBCLASSNAME ");
       stb.append("         END ");
       stb.append("     END AS SUBCLASSNAME, ");
       stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
       stb.append("          THEN L2.SHOWORDER2 ");
       stb.append("          ELSE 999 ");
       stb.append("     END AS SUBCLASSORDER2, ");
       stb.append("     L3.SCHREGNO, ");
       stb.append("     CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) AS SCHOOLCD, ");
       stb.append("     T1.YEAR, ");
       stb.append("     T1.GET_CREDIT, ");
       stb.append("     T1.VALUATION ");
       stb.append(" FROM ");
       stb.append("     ANOTHER_SCHOOL_GETCREDITS_DAT T1 ");
       stb.append("     LEFT JOIN CLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
       stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
       stb.append("          AND T1.CURRICULUM_CD = L2.CURRICULUM_CD ");
       stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
       stb.append("     LEFT JOIN SCHREG_BASE_MST L3 ON T1.APPLICANTNO = L3.APPLICANTNO ");
       stb.append(" WHERE ");
       stb.append("     L3.SCHREGNO = ? ");
       stb.append("     AND ( ");
       stb.append("          (CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) = '2' ");
       stb.append("           OR ");
       stb.append("           CAST(CAST(T1.GET_METHOD AS SMALLINT) + 1 AS CHAR(1)) = '3' ");
       stb.append("          ) ");
       stb.append("          OR ");
       stb.append("          ( ");
       stb.append("           VALUE(T1.GET_CREDIT, 0) > 0 ");
       stb.append("          ) ");
       stb.append("         ) ");
       stb.append(" ), ANNUAL_T AS ( ");
       stb.append(" SELECT ");
       stb.append("     row_number() over (order by T1.YEAR) AS ANNUAL, ");
       stb.append("     T1.YEAR ");
       stb.append(" FROM ");
       stb.append("     STUDY_REC T1 ");
       stb.append(" WHERE ");
       stb.append("    (T1.SCHOOLCD = '0' ");
       stb.append("    AND VALUE(T1.GET_CREDIT, 0) > 0) ");
       stb.append("    OR T1.SCHOOLCD <> '0' ");
       stb.append(" GROUP BY ");
       stb.append("     T1.YEAR ");
       stb.append(" ) ");
       if (selectDiv.equals("TITLE")) {
           stb.append(" SELECT ");
           stb.append("     ANNUAL, ");
           stb.append("     YEAR ");
           stb.append(" FROM ");
           stb.append("     ANNUAL_T ");
           stb.append(" ORDER BY ");
           stb.append("     YEAR ");
       } else {
           stb.append(" , FUKUSU_T AS ( ");
           stb.append(" SELECT ");
           stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
           stb.append("          THEN L2.CLASSCD ");
           stb.append("          ELSE T1.CLASSCD ");
           stb.append("     END AS CLASSCD, ");
           stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
           stb.append("          THEN L2.CLASSNAME ");
           stb.append("          ELSE T1.CLASSNAME ");
           stb.append("     END AS CLASSNAME, ");
           stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L2.SHOWORDER2 ");
           stb.append("          ELSE T1.CLASSORDER2 ");
           stb.append("     END AS CLASSORDER2, ");
           stb.append("     MAX(T1.CURRICULUM_CD) AS CURRICULUM_CD, ");
           stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSCD ");
           stb.append("          ELSE T1.SUBCLASSCD ");
           stb.append("     END AS SUBCLASSCD, ");
           stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
           stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
           stb.append("               THEN L1.SUBCLASSNAME ");
           stb.append("               ELSE T1.SUBCLASSNAME ");
           stb.append("               END ");
           stb.append("     END AS SUBCLASSNAME, ");
           stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L1.SHOWORDER2 ");
           stb.append("          ELSE T1.SUBCLASSORDER2 ");
           stb.append("     END AS SUBCLASSORDER2, ");
           stb.append("     T1.SCHREGNO, ");
           stb.append("     MAX(T1.SCHOOLCD) AS SCHOOLCD, ");
           stb.append("     L3.ANNUAL, ");
           stb.append("     T1.YEAR, ");
           stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
           stb.append("     ROUND(AVG(FLOAT(T1.VALUATION)) * 10, -1) / 10 AS VALUATION ");
           stb.append(" FROM ");
           stb.append("     STUDY_REC T1 ");
           stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
           stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
           stb.append("          AND T1.SUBCLASSCD2 = L1.SUBCLASSCD ");
           stb.append("     LEFT JOIN CLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ");
           stb.append("     LEFT JOIN ANNUAL_T L3 ON T1.YEAR = L3.YEAR ");
           stb.append(" WHERE ");
           stb.append("    (T1.SCHOOLCD = '0' ");
           stb.append("    AND VALUE(T1.GET_CREDIT, 0) > 0) ");
           stb.append("    OR T1.SCHOOLCD <> '0' ");
           stb.append(" GROUP BY ");
           stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
           stb.append("          THEN L2.CLASSCD ");
           stb.append("          ELSE T1.CLASSCD ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
           stb.append("          THEN L2.CLASSNAME ");
           stb.append("          ELSE T1.CLASSNAME ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L2.SHOWORDER2 ");
           stb.append("          ELSE T1.CLASSORDER2 ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSCD ");
           stb.append("          ELSE T1.SUBCLASSCD ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
           stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
           stb.append("               THEN L1.SUBCLASSNAME ");
           stb.append("               ELSE T1.SUBCLASSNAME ");
           stb.append("               END ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L1.SHOWORDER2 ");
           stb.append("          ELSE T1.SUBCLASSORDER2 ");
           stb.append("     END, ");
           stb.append("     T1.SCHREGNO, ");
           stb.append("     L3.ANNUAL, ");
           stb.append("     T1.YEAR ");
           stb.append(" HAVING ");
           stb.append("     COUNT(*) > 1 ");
           stb.append("     AND SUM(CASE WHEN VALUE(T1.VALUATION, 0) > 1 THEN 1 ELSE 0 END) > 0 ");
           stb.append(" ) ");

           stb.append(" , MAIN_T AS ( ");
           stb.append(" SELECT ");
           stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
           stb.append("          THEN L2.CLASSCD ");
           stb.append("          ELSE T1.CLASSCD ");
           stb.append("     END AS CLASSCD, ");
           stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
           stb.append("          THEN L2.CLASSNAME ");
           stb.append("          ELSE T1.CLASSNAME ");
           stb.append("     END AS CLASSNAME, ");
           stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L2.SHOWORDER2 ");
           stb.append("          ELSE T1.CLASSORDER2 ");
           stb.append("     END AS CLASSORDER2, ");
           stb.append("     MAX(T1.CURRICULUM_CD) AS CURRICULUM_CD, ");
           stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSCD ");
           stb.append("          ELSE T1.SUBCLASSCD ");
           stb.append("     END AS SUBCLASSCD, ");
           stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
           stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
           stb.append("               THEN L1.SUBCLASSNAME ");
           stb.append("               ELSE T1.SUBCLASSNAME ");
           stb.append("               END ");
           stb.append("     END AS SUBCLASSNAME, ");
           stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L1.SHOWORDER2 ");
           stb.append("          ELSE T1.SUBCLASSORDER2 ");
           stb.append("     END AS SUBCLASSORDER2, ");
           stb.append("     T1.SCHREGNO, ");
           stb.append("     MAX(T1.SCHOOLCD) AS SCHOOLCD, ");
           stb.append("     L3.ANNUAL, ");
           stb.append("     T1.YEAR, ");
           stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
           stb.append("     ROUND(AVG(FLOAT(T1.VALUATION)) * 10, -1) / 10 AS VALUATION ");
           stb.append(" FROM ");
           stb.append("     STUDY_REC T1 ");
           stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
           stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
           stb.append("          AND T1.SUBCLASSCD2 = L1.SUBCLASSCD ");
           stb.append("     LEFT JOIN CLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ");
           stb.append("     LEFT JOIN ANNUAL_T L3 ON T1.YEAR = L3.YEAR, ");
           stb.append("     FUKUSU_T ");
           stb.append(" WHERE ");
           stb.append("     FUKUSU_T.CLASSCD = CASE WHEN L2.CLASSCD IS NOT NULL ");
           stb.append("          THEN L2.CLASSCD ");
           stb.append("          ELSE T1.CLASSCD ");
           stb.append("     END ");
           stb.append("     AND FUKUSU_T.CLASSNAME = CASE WHEN L2.CLASSNAME IS NOT NULL ");
           stb.append("                                 THEN L2.CLASSNAME ");
           stb.append("                                 ELSE T1.CLASSNAME ");
           stb.append("                            END ");
           stb.append("     AND FUKUSU_T.CLASSORDER2 = CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
           stb.append("                                   THEN L2.SHOWORDER2 ");
           stb.append("                                   ELSE T1.CLASSORDER2 ");
           stb.append("                              END ");
           stb.append("     AND FUKUSU_T.CURRICULUM_CD = T1.CURRICULUM_CD ");
           stb.append("     AND FUKUSU_T.SUBCLASSCD = CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
           stb.append("                                  THEN L1.SUBCLASSCD ");
           stb.append("                                  ELSE T1.SUBCLASSCD ");
           stb.append("                             END ");
           stb.append("     AND FUKUSU_T.SUBCLASSNAME = CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
           stb.append("                                  THEN L1.SUBCLASSORDERNAME1 ");
           stb.append("                                  ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
           stb.append("                                            THEN L1.SUBCLASSNAME ");
           stb.append("                                            ELSE T1.SUBCLASSNAME ");
           stb.append("                                       END ");
           stb.append("                             END ");
           stb.append("     AND FUKUSU_T.SUBCLASSORDER2 = CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
           stb.append("                                      THEN L1.SHOWORDER2 ");
           stb.append("                                      ELSE T1.SUBCLASSORDER2 ");
           stb.append("                                 END ");
           stb.append("     AND FUKUSU_T.SCHREGNO = T1.SCHREGNO ");
           stb.append("     AND FUKUSU_T.ANNUAL = L3.ANNUAL ");
           stb.append("     AND FUKUSU_T.YEAR = T1.YEAR ");
           stb.append("     AND VALUE(T1.VALUATION, 0) > 1 ");
           stb.append("     AND ((T1.SCHOOLCD = '0' AND VALUE(T1.GET_CREDIT, 0) > 0) ");
           stb.append("           OR ");
           stb.append("          (T1.SCHOOLCD <> '0') ");
           stb.append("         )");
           stb.append(" GROUP BY ");
           stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
           stb.append("          THEN L2.CLASSCD ");
           stb.append("          ELSE T1.CLASSCD ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
           stb.append("          THEN L2.CLASSNAME ");
           stb.append("          ELSE T1.CLASSNAME ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L2.SHOWORDER2 ");
           stb.append("          ELSE T1.CLASSORDER2 ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSCD ");
           stb.append("          ELSE T1.SUBCLASSCD ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
           stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
           stb.append("               THEN L1.SUBCLASSNAME ");
           stb.append("               ELSE T1.SUBCLASSNAME ");
           stb.append("               END ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L1.SHOWORDER2 ");
           stb.append("          ELSE T1.SUBCLASSORDER2 ");
           stb.append("     END, ");
           stb.append("     T1.SCHREGNO, ");
           stb.append("     L3.ANNUAL, ");
           stb.append("     T1.YEAR ");

           stb.append(" UNION ALL ");
           stb.append(" SELECT ");
           stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
           stb.append("          THEN L2.CLASSCD ");
           stb.append("          ELSE T1.CLASSCD ");
           stb.append("     END AS CLASSCD, ");
           stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
           stb.append("          THEN L2.CLASSNAME ");
           stb.append("          ELSE T1.CLASSNAME ");
           stb.append("     END AS CLASSNAME, ");
           stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L2.SHOWORDER2 ");
           stb.append("          ELSE T1.CLASSORDER2 ");
           stb.append("     END AS CLASSORDER2, ");
           stb.append("     MAX(T1.CURRICULUM_CD) AS CURRICULUM_CD, ");
           stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSCD ");
           stb.append("          ELSE T1.SUBCLASSCD ");
           stb.append("     END AS SUBCLASSCD, ");
           stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
           stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
           stb.append("               THEN L1.SUBCLASSNAME ");
           stb.append("               ELSE T1.SUBCLASSNAME ");
           stb.append("               END ");
           stb.append("     END AS SUBCLASSNAME, ");
           stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L1.SHOWORDER2 ");
           stb.append("          ELSE T1.SUBCLASSORDER2 ");
           stb.append("     END AS SUBCLASSORDER2, ");
           stb.append("     T1.SCHREGNO, ");
           stb.append("     MAX(T1.SCHOOLCD) AS SCHOOLCD, ");
           stb.append("     L3.ANNUAL, ");
           stb.append("     T1.YEAR, ");
           stb.append("     SUM(T1.GET_CREDIT) AS GET_CREDIT, ");
           stb.append("     ROUND(AVG(FLOAT(T1.VALUATION)) * 10, -1) / 10 AS VALUATION ");
           stb.append(" FROM ");
           stb.append("     STUDY_REC T1 ");
           stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD = L1.CLASSCD ");
           stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
           stb.append("          AND T1.SUBCLASSCD2 = L1.SUBCLASSCD ");
           stb.append("     LEFT JOIN CLASS_MST L2 ON L1.CLASSCD = L2.CLASSCD ");
           stb.append("     LEFT JOIN ANNUAL_T L3 ON T1.YEAR = L3.YEAR ");
           stb.append(" WHERE ");
           stb.append("    (T1.SCHOOLCD = '0' ");
           stb.append("    AND VALUE(T1.GET_CREDIT, 0) > 0) ");
           stb.append("    OR T1.SCHOOLCD <> '0' ");
           stb.append(" GROUP BY ");
           stb.append("     CASE WHEN L2.CLASSCD IS NOT NULL ");
           stb.append("          THEN L2.CLASSCD ");
           stb.append("          ELSE T1.CLASSCD ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L2.CLASSNAME IS NOT NULL ");
           stb.append("          THEN L2.CLASSNAME ");
           stb.append("          ELSE T1.CLASSNAME ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L2.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L2.SHOWORDER2 ");
           stb.append("          ELSE T1.CLASSORDER2 ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSCD ");
           stb.append("          ELSE T1.SUBCLASSCD ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L1.SUBCLASSORDERNAME1 IS NOT NULL ");
           stb.append("          THEN L1.SUBCLASSORDERNAME1 ");
           stb.append("          ELSE CASE WHEN L1.SUBCLASSNAME IS NOT NULL ");
           stb.append("               THEN L1.SUBCLASSNAME ");
           stb.append("               ELSE T1.SUBCLASSNAME ");
           stb.append("               END ");
           stb.append("     END, ");
           stb.append("     CASE WHEN L1.SHOWORDER2 IS NOT NULL ");
           stb.append("          THEN L1.SHOWORDER2 ");
           stb.append("          ELSE T1.SUBCLASSORDER2 ");
           stb.append("     END, ");
           stb.append("     T1.SCHREGNO, ");
           stb.append("     L3.ANNUAL, ");
           stb.append("     T1.YEAR ");
           stb.append(" HAVING ");
           stb.append("     COUNT(*) = 1 ");
           stb.append("     OR SUM(CASE WHEN VALUE(T1.VALUATION, 0) > 1 THEN 1 ELSE 0 END) = 0 ");
           stb.append(" ) ");

           stb.append(" , SELECT_T AS ( ");
           stb.append(" SELECT ");
           stb.append("     T1.CLASSCD, ");
           stb.append("     T1.CLASSNAME, ");
           stb.append("     T1.CLASSORDER2, ");
           stb.append("     T1.CURRICULUM_CD, ");
           stb.append("     T1.SUBCLASSCD, ");
           stb.append("     T1.SUBCLASSNAME, ");
           stb.append("     T1.SUBCLASSORDER2, ");
           stb.append("     T1.SCHREGNO, ");
           stb.append("     T1.SCHOOLCD, ");
           stb.append("     T1.ANNUAL, ");
           stb.append("     T1.YEAR, ");
           stb.append("     T1.GET_CREDIT, ");
           stb.append("     T1.VALUATION ");
           stb.append(" FROM ");
           stb.append("     MAIN_T T1 ");
           stb.append(" ) ");

           stb.append(" , CURRICULUM_T AS ( ");
           stb.append(" SELECT ");
           stb.append("     T1.CLASSCD, ");
           stb.append("     MAX(T1.CURRICULUM_CD) AS CURRICULUM_CD, ");
           stb.append("     T1.SUBCLASSCD, ");
           stb.append("     T1.SCHREGNO ");
           stb.append(" FROM ");
           stb.append("     SELECT_T T1 ");
           stb.append(" GROUP BY ");
           stb.append("     T1.CLASSCD, ");
           stb.append("     T1.SUBCLASSCD, ");
           stb.append("     T1.SCHREGNO ");
           stb.append(" ) ");

           stb.append(" SELECT ");
           stb.append("     T1.CLASSCD, ");
           stb.append("     T1.CLASSNAME, ");
           stb.append("     T1.CLASSORDER2, ");
           stb.append("     T1.CURRICULUM_CD, ");
           stb.append("     T1.SUBCLASSCD, ");
           stb.append("     CASE WHEN L2.SUBCLASSORDERNAME1 IS NOT NULL ");
           stb.append("          THEN L2.SUBCLASSORDERNAME1 ");
           stb.append("          ELSE CASE WHEN L2.SUBCLASSNAME IS NOT NULL ");
           stb.append("               THEN L2.SUBCLASSNAME ");
           stb.append("               ELSE T1.SUBCLASSNAME ");
           stb.append("               END ");
           stb.append("     END AS SUBCLASSNAME, ");
           stb.append("     L2.SHOWORDER2, ");
           stb.append("     T1.SCHREGNO, ");
           stb.append("     T1.SCHOOLCD, ");
           stb.append("     T1.ANNUAL, ");
           stb.append("     T1.YEAR, ");
           stb.append("     T1.GET_CREDIT, ");
           stb.append("     T1.VALUATION ");
           stb.append(" FROM ");
           stb.append("     SELECT_T T1 ");
           stb.append("     LEFT JOIN CURRICULUM_T L1 ON T1.CLASSCD = L1.CLASSCD ");
           stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
           stb.append("          AND T1.SUBCLASSCD = L1.SUBCLASSCD ");
           stb.append("     LEFT JOIN SUBCLASS_MST L2 ON T1.CLASSCD = L2.CLASSCD ");
           stb.append("          AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ");
           stb.append("          AND T1.SUBCLASSCD = L2.SUBCLASSCD ");
           stb.append(" ORDER BY ");
           stb.append("     T1.CLASSORDER2, ");
           stb.append("     T1.CLASSCD, ");
           stb.append("     L2.SHOWORDER2, ");
           stb.append("     T1.SUBCLASSCD, ");
           stb.append("     T1.CURRICULUM_CD, ");
           stb.append("     T1.YEAR ");
       }
       log.debug(stb);
       return stb.toString();

   }//public pre_sql_newの括り

    /**
     *
     *  学習記録のSQL
     *  2005/07/08 Modify 評定読替元の科目を対象としない処理を追加
     *
     */
    public String pre_sql(){

        if( tname1 == null )setFieldName();   //使用テーブル名設定 05/07/25Build
        StringBuffer sql = new StringBuffer();

        try{

        //  評定１を２と判定
            String h_1_2 = null;
            String h_1_3 = null;
            if( hyoutei.equals("on") ){ //----->評定読み替えのON/OFF  評定１を２と読み替え
                h_1_2 = "CASE VALUE(T1.GRADES,0) WHEN 1 THEN 2 ELSE T1.GRADES END ";
                h_1_3 = "T1.CREDIT ";  //NO001
                //NO001 h_1_3 = "CASE WHEN VALUE(T1.GRADES,0)=1 AND VALUE(T1.CREDIT,0)=0 THEN T1.ADD_CREDIT ELSE T1.CREDIT END ";
            } else{
                h_1_2 = "T1.GRADES ";
                h_1_3 = "T1.CREDIT ";
            }

        //該当生徒の成績データ表
            if( 3 <= definecode.schoolmark.length()  &&  definecode.schoolmark.equals("KIN") )  //--NO001
                //近大付属は評価読替元科目はココで除外する  05/07/08
                sql.append( pre_sql_Replace() );
            else
                sql.append( pre_sql_Common() );  //--NO001

        //該当生徒の科目評定、修得単位及び教科評定平均
            sql.append( "SELECT ");
            sql.append(     "T2.SHOWORDER2 as CLASS_ORDER,");
            sql.append(     "T3.SHOWORDER2 as SUBCLASS_ORDER,");
            sql.append(     "T1.ANNUAL,T1.CLASSCD,");
            if (!english) {                     //----->教科名 英語/日本語
                sql.append( "CASE WHEN T1.CLASSNAME IS NOT NULL THEN T1.CLASSNAME ");
                sql.append(      "WHEN T2.CLASSORDERNAME1 IS NOT NULL THEN T2.CLASSORDERNAME1 ");
                sql.append(      "ELSE T2.CLASSNAME END AS CLASSNAME,");
            } else {
                sql.append( "T2.CLASSNAME_ENG AS CLASSNAME,");
            }
            sql.append(     "T1.SUBCLASSCD,");
            if (!english) {                     //----->科目名 英語/日本語
                sql.append( "CASE WHEN T1.SUBCLASSNAME IS NOT NULL THEN T1.SUBCLASSNAME ");
                sql.append(      "WHEN T3.SUBCLASSORDERNAME1 IS NOT NULL THEN T3.SUBCLASSORDERNAME1 ");
                sql.append(      "ELSE T3.SUBCLASSNAME END AS SUBCLASSNAME,");
            } else {
                sql.append( "T3.SUBCLASSNAME_ENG AS SUBCLASSNAME,");
            }
            if( hyoutei.equals("hyde") )        //----->評定の出力有無
                sql.append( "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,");
            else{
                sql.append( h_1_2 + " AS GRADES,");
                sql.append( "T5.AVG_GRADES,'' AS ASSESS_LEVEL,");
            }
            sql.append(     "T1.CREDIT AS GRADE_CREDIT,T4.CREDIT ");
            sql.append( "FROM ");
            sql.append(     "STUDYREC T1 ");
            sql.append(     "LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            sql.append(     "LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
                //  修得単位数の計
            sql.append(     "INNER JOIN(SELECT ");
            sql.append(             "CLASSCD,SUBCLASSCD,SUM(" + h_1_3 + ") AS CREDIT ");
            sql.append(         "FROM ");
            sql.append(             "STUDYREC T1 ");
            sql.append(         "WHERE ");
            sql.append(             "CLASSCD BETWEEN '"+KNJDefineCode.subject_D+"' AND '"+KNJDefineCode.subject_U+"' ");
            sql.append(         "GROUP BY ");
            sql.append(             "CLASSCD,SUBCLASSCD ");
            sql.append(     ")T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if( !(hyoutei.equals("hyde")) ){        //----->評定の出力有無
                //  各教科の評定平均値
                sql.append( "INNER JOIN(SELECT ");
                sql.append(         "CLASSCD,");
                sql.append(         "DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1),5,1) AS AVG_GRADES ");
                sql.append(     "FROM ");
                sql.append(         "STUDYREC T1 ");
                sql.append(     "WHERE ");
                sql.append(         "CLASSCD BETWEEN '"+KNJDefineCode.subject_D+"' AND '"+KNJDefineCode.subject_U+"' ");
                sql.append(     "GROUP BY ");
                sql.append(         "CLASSCD ");
                sql.append( ")T5 ON T5.CLASSCD = T1.CLASSCD ");
            }
            sql.append( "WHERE ");
            sql.append(     "T1.CLASSCD BETWEEN '"+KNJDefineCode.subject_D+"' AND '"+KNJDefineCode.subject_U+"' ");

        //  総合学習の修得単位数（学年別）
            if( stype>1 ){          //----->学年別
                sql.append("UNION SELECT ");
                sql.append(     "cast(null as smallint) as CLASS_ORDER,");
                sql.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
                sql.append(     "ANNUAL,'"+KNJDefineCode.subject_T+"' AS CLASSCD,'sogo' AS CLASSNAME,'"+KNJDefineCode.subject_T+"01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,");
                sql.append(     "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ");
                sql.append( "FROM ");
                sql.append(     "STUDYREC ");
                sql.append( "WHERE ");
                sql.append(     "CLASSCD = '"+KNJDefineCode.subject_T+"' ");
                sql.append( "GROUP BY ");
                sql.append(     "ANNUAL ");
            }
        //  総合学習の修得単位数（合計）
            sql.append( "UNION SELECT ");
            sql.append(     "cast(null as smallint) as CLASS_ORDER,");
            sql.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
            sql.append(         "'0' AS ANNUAL,'"+KNJDefineCode.subject_T+"' AS CLASSCD,");
            sql.append(         "'sogo' AS CLASSNAME,'"+KNJDefineCode.subject_T+"01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,");
            sql.append(         "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ");
            sql.append(     "FROM ");
            sql.append(         "STUDYREC ");
            sql.append(     "WHERE ");
            sql.append(         "CLASSCD = '"+KNJDefineCode.subject_T+"' ");

        //  留学中の修得単位数（学年別）
            if( stype>1 ){              //----->学年別/合計
                sql.append( "UNION SELECT ");
                sql.append(     "cast(null as smallint) as CLASS_ORDER,");
                sql.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
                sql.append(     "ANNUAL,'AA' AS CLASSCD,'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,");
                sql.append(     "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ");
                sql.append( "FROM ");
                sql.append(         "(SELECT ");
                sql.append(             "ABROAD_CREDITS,");
                sql.append(             "INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
                sql.append(         "FROM ");
                sql.append(              tname2 + " ");          //05/07/25Modify
                sql.append(         "WHERE ");
                sql.append(             "SCHREGNO =? AND TRANSFERCD = '1' ");
                sql.append(         ")ST1,");
                sql.append(         "(SELECT ");
                sql.append(             "ANNUAL,MAX(YEAR) AS YEAR ");
                sql.append(         "FROM ");
                sql.append(              tname3 + " ");          //05/07/25Modify
                sql.append(         "WHERE ");
                sql.append(             "SCHREGNO =? AND YEAR <=? ");
                sql.append(         "GROUP BY ");
                sql.append(             "ANNUAL ");
                sql.append(         ")ST2 ");
                sql.append( "WHERE ");
                sql.append(     "ST1.TRANSFER_YEAR <=? ");
                sql.append(     "and INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
                sql.append( "GROUP BY ");
                sql.append(     "ANNUAL ");
            }
        //  留学中の修得単位数（合計）
            sql.append(     "UNION SELECT ");
            sql.append(     "cast(null as smallint) as CLASS_ORDER,");
            sql.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
            sql.append(         "'0' AS ANNUAL,'AA' AS CLASSCD,'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,");
            sql.append(         "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ");
            sql.append(     "FROM ");
            sql.append(         "(SELECT ");
            sql.append(             "SCHREGNO,ABROAD_CREDITS,INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
            sql.append(         "FROM  ");
            sql.append(              tname2 + " ");        //05/07/25Modify
            sql.append(         "WHERE  ");
            sql.append(             "SCHREGNO =? AND TRANSFERCD = '1' ");
            sql.append(         ")ST1 ");
            sql.append(     "WHERE ");
            sql.append(         "TRANSFER_YEAR <=? ");

        //  修得単位数、評定平均（学年別）
            if( stype>1 ){          //----->学年別
                sql.append( "UNION SELECT ");
                sql.append(     "cast(null as smallint) as CLASS_ORDER,");
                sql.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
                sql.append(     "ANNUAL,'ZZ' AS CLASSCD,'total' AS CLASSNAME,");
                sql.append(     "'ZZZZ' AS SUBCLASSCD,'total' AS SUBCLASSNAME,");
                sql.append(     "0 AS GRADES,");
                if( hyoutei.equals("grade") )
                    sql.append( "ROUND(DECIMAL(AVG(FLOAT(" + h_1_2 + ")),5,2),1) AS AVG_GRADES,");
                else
                    sql.append( "0 AS AVG_GRADES,");
                sql.append(     "'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,");
                sql.append(     "SUM(" + h_1_3 + ") AS CREDIT ");
                sql.append( "FROM ");
                sql.append(     "STUDYREC T1 ");
                sql.append( "WHERE ");
                sql.append(     "CLASSCD BETWEEN '"+KNJDefineCode.subject_D+"' AND '"+KNJDefineCode.subject_U+"' ");
                sql.append( "GROUP BY ");
                sql.append(     "ANNUAL ");
            }

        //  全体の修得単位数・全体の評定平均値
            sql.append(     "UNION SELECT ");
            sql.append(     "cast(null as smallint) as CLASS_ORDER,");
            sql.append(     "cast(null as smallint) as SUBCLASS_ORDER,");
            sql.append(         "'0' AS ANNUAL,'ZZ' AS CLASSCD,'total' AS CLASSNAME,'ZZZZ' AS SUBCLASSCD,");
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
            sql.append(     "FROM ");
            sql.append(         "STUDYREC T1 ");
            if( atype.equals("on") )                //----->特Ａフラグ出力の有無
                sql.append(     "LEFT JOIN HEXAM_ENTREMARK_HDAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append(     "WHERE ");
            sql.append(         "CLASSCD BETWEEN '"+KNJDefineCode.subject_D+"' AND '"+KNJDefineCode.subject_U+"' ");
            sql.append( "ORDER BY CLASS_ORDER,CLASSCD,SUBCLASS_ORDER,SUBCLASSCD,ANNUAL");

        } catch( Exception ex ){
            log.error("pre_sql read error!" + ex );
            log.debug("studyrecsql="+sql.toString());
        }
log.debug("studyrecsql="+sql.toString());
        return sql.toString();
    }//public pre_sqlの括り


    /**
     *
     *  学習記録データ取得のSQL
     *  2005/07/08 Build 
     *
     */
    private String pre_sql_Common()
    {
log.debug("String pre_sql_Common() ");
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH T_STUDYREC AS(");
            stb.append("SELECT  T1.CLASSNAME, ");
            stb.append(        "T1.SUBCLASSNAME, ");
            stb.append(        "T1.SCHREGNO, ");
            stb.append(        "T1.YEAR, ");
            stb.append(        "T1.ANNUAL, ");
            stb.append(        "T1.SUBCLASSCD, ");
            stb.append(        "L2.SUBCLASSCD2, ");
            stb.append(        "T1.VALUATION AS GRADES ");
            // 08/03/06Add
            if (_isHosei) {
                stb.append(   ",CASE WHEN T1.CLASSCD = '87' THEN '90' ELSE T1.CLASSCD END AS CLASSCD ");
            } else {
                stb.append(   ",T1.CLASSCD ");
            }
            stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");  //NO001
            //NO001 stb.append(        "GET_CREDIT AS CREDIT,ADD_CREDIT ");
            stb.append("FROM   " + tname1 + " T1 ");                    //05/07/25Modify
            stb.append(        "LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD=T1.SUBCLASSCD ");
            stb.append("WHERE   T1.SCHREGNO = ? AND T1.YEAR <= ? AND ");
            stb.append(        "(T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D+"' AND '" + KNJDefineCode.subject_U+"' OR T1.CLASSCD = '" + KNJDefineCode.subject_T+"') ");
            stb.append(") , STUDYREC AS( ");
            stb.append(    "SELECT ");
            stb.append(        "T1.CLASSNAME, ");
            stb.append(        "T1.SUBCLASSNAME, ");
            stb.append(        "T1.SCHREGNO, ");
            stb.append(        "T1.YEAR, ");
            stb.append(        "T1.ANNUAL, ");
            stb.append(        "T1.CLASSCD , ");
            stb.append(        "T1.SUBCLASSCD, ");
            stb.append(        "T1.GRADES, ");
            stb.append(        "T1.CREDIT ");
            stb.append(    "FROM ");
            stb.append(        "T_STUDYREC T1 ");
            stb.append(    "WHERE ");
            stb.append(        "T1.SUBCLASSCD2 IS NULL ");
            stb.append(    "UNION ");
            stb.append(    "SELECT ");
            stb.append(        "MAX(T1.CLASSNAME) AS CLASSNAME, ");
            stb.append(        "MAX(T1.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append(        "T1.SCHREGNO, ");
            stb.append(        "T1.YEAR, ");
            stb.append(        "MAX(T1.ANNUAL) AS ANNUAL, ");
            stb.append(        "T1.CLASSCD, ");
            stb.append(        "T1.SUBCLASSCD2 AS SUBCLASSCD, ");
            stb.append(        "MAX(T1.GRADES) AS GRADES, ");
            stb.append(        "SUM(T1.CREDIT) AS CREDIT ");
            stb.append(    "FROM ");
            stb.append(        "T_STUDYREC T1 ");
            stb.append(    "WHERE ");
            stb.append(        "T1.SUBCLASSCD2 IS NOT NULL ");
            stb.append(    "GROUP BY ");
            stb.append(        "T1.SCHREGNO, ");
            stb.append(        "T1.YEAR, ");
            stb.append(        "T1.CLASSCD, ");
            stb.append(        "T1.SUBCLASSCD2 ");
            stb.append(") ");
        } catch( Exception ex ){
            log.error( "pre_sql_Common error!", ex );
        }

        return stb.toString();
    }


    /**
     *
     *  学習記録データ取得のSQL 評定読替科目を除外する記述を組み込む
     *  2005/07/08 Build 
     *
     */
    private String pre_sql_Replace()
    {
log.debug("String pre_sql_Replace() ");
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH STUDYREC AS(");
            stb.append("SELECT  T1.CLASSNAME, ");
            stb.append(        "T1.SUBCLASSNAME, ");
            stb.append(        "T1.SCHREGNO, ");
            stb.append(        "T1.YEAR, ");
            stb.append(        "T1.ANNUAL, ");
            stb.append(        "T1.CLASSCD, ");
            stb.append(        "CASE WHEN L2.SUBCLASSCD2 IS NOT NULL THEN L2.SUBCLASSCD2 ELSE T1.SUBCLASSCD END AS SUBCLASSCD, ");
            stb.append(        "T1.VALUATION AS GRADES ");
            stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");  //NO001
            //NO001 stb.append(        "GET_CREDIT AS CREDIT, ADD_CREDIT ");
            stb.append("FROM   " + tname1 + " T1 ");            //05/07/25Modify
            stb.append(        "LEFT JOIN SUBCLASS_MST L2 ON L2.SUBCLASSCD=T1.SUBCLASSCD ");
            stb.append("WHERE   T1.SCHREGNO = ? AND ");
            stb.append(        "T1.YEAR <= ? AND ");
            stb.append(        "(T1.CLASSCD BETWEEN '" + KNJDefineCode.subject_D + "' AND '" + KNJDefineCode.subject_U + "' OR T1.CLASSCD = '" + KNJDefineCode.subject_T + "') AND ");
            stb.append(        "NOT EXISTS(SELECT  'X' ");
            stb.append(                   "FROM    SUBCLASS_REPLACE_DAT T2 ");
            stb.append(                   "WHERE   T2.YEAR = T1.YEAR AND ");
            stb.append(                           "T2.ANNUAL = T1.ANNUAL AND ");
            stb.append(                           "T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ");
            stb.append(    ")");
        } catch( Exception ex ){
            log.error( "pre_sql_Replace error!", ex );
        }

        return stb.toString();
    }


    /**
     *  在校生用と卒業生用のテーブル名を設定
     *  2005/07/25 Build 在校生用と卒業生用で共有する
     */
    public void setFieldName()
    {
        tname1 = "SCHREG_STUDYREC_DAT";
        tname2 = "SCHREG_TRANSFER_DAT";
        tname3 = "SCHREG_REGD_DAT";
log.debug("table1->"+tname1+"   tname2->"+tname2+"   tname3->"+tname3);
    }

}
