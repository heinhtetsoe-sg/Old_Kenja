// kanji=漢字
/*
 * $Id: ceac3ea9617f2a0563519ae5a4ed817aa6f94066 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE.detail;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;

/**
 *
 *  [進路情報・調査書]成績概評人数データSQL作成
 *  2005/07/08 yamashiro・グループの段階別人数SQL取得のメソッドを追加
 *  2005/07/25 yamashiro・在校生用と卒業生用のSQLを共通化するため使用テーブル名を変数に変更
 *  2006/04/14 yamashiro・KNJDefineCodeクラスは変数のみ使用する( => defineCodeメソッドを使用しない )  --NO001
 */

public class KNJ_GeneviewmbrSql{

    private static final Log log = LogFactory.getLog(KNJ_GeneviewmbrSql.class);

    private KNJDefineSchool _definecode;  //各学校における定数等設定 05/07/08 Build  NO001 Modify
    protected String tname1 = null;    //05/07/25 SCHREG_REGD_DAT
    
    public KNJ_GeneviewmbrSql() {
        this(new KNJDefineSchool());
    }

    public KNJ_GeneviewmbrSql(KNJDefineSchool definecode) {
        _definecode= definecode;
        log.debug(" KNJE.detail.KNJ_GeneviewmbrSql $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
    }

    /**
     *
     *  段階別人数取得のSQL
     *  2005/07/08 Build 旧pre_sqlメソッドを実行
     *
     */
    public String pre_sql(Map paramap)
    {
        String retval = null;
        try {
            if (tname1 == null) {
                setFieldName();   //使用テーブル名設定 05/07/25Build   NO001 Modify
            }
            if (null != _definecode && null != _definecode.schoolmark && _definecode.schoolmark.equals("KIN")) {
                log.debug("String pre_sql_Group()");
                retval = pre_sql_Group();
            } else {
                log.debug("String pre_sql_Common()");
                retval = pre_sql_Common(paramap);
            }
        } catch (Exception ex) {
            log.error( "definecode error!", ex );
        }
        log.debug("sql=" + retval);
        return retval;
    }

    public String pre_sql() {
        return pre_sql(new HashMap());
    }

    /**
     *
     *  標準の段階別人数取得のSQL
     *  2005/07/08 Modify 旧pre_sqlメソッドをRENAME
     *
     */
    public String pre_sql_Common(final Map paramap)
    {
        final String gaihyouGakkaBetu = null == paramap ? null : (String) paramap.get("gaihyouGakkaBetu");
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append(        "A_MEMBER AS MEMBER5,");
        stb.append(        "B_MEMBER AS MEMBER4,");
        stb.append(        "C_MEMBER AS MEMBER3,");
        stb.append(        "D_MEMBER AS MEMBER2,");
        stb.append(        "E_MEMBER AS MEMBER1,");
        stb.append(        "COURSE_MEMBER AS MEMBER0,");
        stb.append(        "GRADE_MEMBER AS MEMBER6 ");
        stb.append("FROM ");
        stb.append(        "GENEVIEWMBR_DAT T1,");
        stb.append(        tname1 + " T2 "); //05/07/25
        if ("2".equals(gaihyouGakkaBetu)) {
            stb.append("  INNER JOIN COURSE_GROUP_CD_DAT T3 ON T3.YEAR = T2.YEAR ");
            stb.append("    AND T3.GRADE = T2.GRADE ");
            stb.append("    AND T3.COURSECD = T2.COURSECD ");
            stb.append("    AND T3.MAJORCD = T2.MAJORCD ");
            stb.append("    AND T3.COURSECODE = T2.COURSECODE ");
        }
        stb.append("WHERE ");
        stb.append(        "T2.SCHREGNO =? ");
        stb.append(        "AND T2.YEAR =? ");
        stb.append(        "AND T2.SEMESTER =? ");
        stb.append(        "AND T1.YEAR = T2.YEAR ");
        stb.append(        "AND T1.GRADE = T2.GRADE ");
        if ("1".equals(gaihyouGakkaBetu)) {
            stb.append(        "AND T1.COURSECD = T2.COURSECD ");
            stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(        "AND VALUE(T1.COURSECODE,'0000') = VALUE(T2.COURSECODE,'0000')");
        } else if ("2".equals(gaihyouGakkaBetu)) {
            stb.append(        "AND T1.COURSECD = '0' ");
            stb.append(        "AND T1.MAJORCD = T3.GROUP_CD ");
            stb.append(        "AND T1.COURSECODE = '0000' ");
        } else {
            stb.append(        "AND T1.COURSECD = T2.COURSECD ");
            stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(        "AND T1.COURSECODE = '0000' ");
        }
        return stb.toString();
    }


    /**
     *
     *  グループの段階別人数取得のSQL  COURSE_GROUP_DATを使用してグループ化
     *  2004/07/08 Build
     *
     */
    public String pre_sql_Group()
    {
        StringBuffer stb = new StringBuffer();
        try{
            stb.append("WITH COURSE_GROUP_W AS(");  //NO001 COURSE_GROUP名のTABLEが存在するのでRENAME
            stb.append(    "SELECT  COURSE_SEQ, GRADE, YEAR ");
            stb.append(    "FROM    COURSE_GROUP_DAT T1 ");
            stb.append(    "WHERE   EXISTS(SELECT  'X' ");
            stb.append(                   "FROM   " + tname1 + " T2 ");      //05/07/25
            stb.append(                   "WHERE   T2.SCHREGNO = ? AND ");
            stb.append(                           "T2.YEAR = ? AND ");
            stb.append(                           "T2.SEMESTER = ? AND ");
            stb.append(                           "T1.YEAR = T2.YEAR AND ");
            stb.append(                           "T1.GRADE = T2.GRADE AND ");
            stb.append(                           "T1.HR_CLASS = T2.HR_CLASS) ");
            stb.append(    ") ");
            
            stb.append("SELECT  SUM(A_MEMBER) AS MEMBER5, ");
            stb.append(        "SUM(B_MEMBER) AS MEMBER4, ");
            stb.append(        "SUM(C_MEMBER) AS MEMBER3, ");
            stb.append(        "SUM(D_MEMBER) AS MEMBER2, ");
            stb.append(        "SUM(E_MEMBER) AS MEMBER1, ");
            stb.append(        "SUM(COURSE_MEMBER) AS MEMBER0, ");
            stb.append(        "SUM(GRADE_MEMBER)  AS MEMBER6 ");
            stb.append("FROM    GENEVIEWMBR_DAT T1, ");
            stb.append(        "COURSE_GROUP_W T2 ");
            stb.append("WHERE   T1.YEAR = T2.YEAR AND ");
            stb.append(        "T1.GRADE = T2.GRADE AND ");
            stb.append(        "T1.COURSECODE = T2.COURSE_SEQ");

        } catch (Exception ex) {
            log.error("[KNJ_GeneviewmbrSql]pre_sql_Group error! ", ex );
        }
        return stb.toString();
    }


    /**
     *  在校生用と卒業生用のテーブル名を設定
     *  2005/07/25 Build 在校生用と卒業生用で共有する
     */
    public void setFieldName()
    {
        tname1 = "SCHREG_REGD_DAT";
    }
}
