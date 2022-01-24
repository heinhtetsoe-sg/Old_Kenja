// kanji=漢字
/*
 * $Id: 3f5353e37ea88eaa2f1edbd248938681152ba8d4 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJE.detail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;

/**
 *
 *  [進路情報・調査書]出欠記録データSQL作成
 *  2005/07/10 yamashiro・休学日数を引かない授業日数を追加
 *  2005/07/25 yamashiro・在校生用と卒業生用のSQLを共通化するため使用テーブル名を変数に変更
 *  2005/10/26 yamashiro・メソッドpre_sql_Pattern2()を変更 => 授業日数は留学日数と休学日数を引いた値とする
 *
 */

public class KNJ_AttendrecSql {

    private static final Log log = LogFactory.getLog(KNJ_AttendrecSql.class);

    private KNJDefineSchool _defineschool;       //各学校における定数等設定 05/07/10 Build
    private boolean _isGrd;
    protected String tname1 = null;    //05/07/25 SCHREG_REGD_DAT
    
    public KNJ_AttendrecSql(final KNJDefineSchool defineschool, final boolean isGrd) {
        _defineschool = defineschool;
        _isGrd = isGrd;
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
    }
    
    public KNJ_AttendrecSql(final KNJDefineCode definecode, final boolean isGrd) {
        this((KNJDefineSchool) definecode, isGrd);
    }

    
    public KNJ_AttendrecSql() {
        this(new KNJDefineSchool(), false);
        log.debug("schoolmark = " + _defineschool.schoolmark);
    }

    /**
     *
     *  出欠記録取得のSQL
     *  2005/07/10 Build 旧pre_sqlメソッドを実行
     *
     */
    public String pre_sql(final String notPrintAnotherAttendrec) {
        String retval = null;
        try {
            retval = pre_sql_Pattern1(notPrintAnotherAttendrec);
        } catch (Exception ex) {
            log.error("definecode error!", ex);
        }
        return retval;
    }

    /**
     *  出欠記録取得のSQL
     */
    public String pre_sql() {
        return pre_sql(null);
    }

    /**
     *
     *  出欠記録取得のSQL
     *  2005/07/10 Build 旧pre_sqlメソッドを標準SQLとし、例外のSQLのメソッドを追加
     *
     */
    public String pre_sql(final DB2UDB db2, final String dummy, final String notPrintAnotherAttendrec)
    {
        String retval = null;
        try {
            if (null != _defineschool.schoolmark && _defineschool.schoolmark.length() > 0 && _defineschool.schoolmark.substring(0, 1).equals("K")) {
                retval = pre_sql_Pattern2(notPrintAnotherAttendrec);
            } else {
                retval = pre_sql_Pattern1(notPrintAnotherAttendrec);
            }

        } catch (Exception ex) {
            log.error( "definecode error!", ex);
        }

        return retval;
    }

    /**
     *
     *  出欠記録取得のSQL
     *  2005/07/10 Build 旧pre_sqlメソッドを標準SQLとし、例外のSQLのメソッドを追加
     *
     */
    public String pre_sql(final DB2UDB db2, final String year) {
        return pre_sql(db2, year, null);
    }


    /**
     *
     *  出欠記録(SCHREG_ATTENDREC_DAT)ＳＱＬの作成
     *
     *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
     */
    protected String pre_sql_Pattern1(final String notPrintAnotherAttendrec) {

        if (tname1 == null) setFieldName();   //使用テーブル名設定 05/07/25Build
        String sql = null;
        try{
            sql = "SELECT DISTINCT "
                    + "T1.YEAR,"
                    + "ANNUAL,"
                    + "VALUE(CLASSDAYS,0) AS CLASSDAYS,"                            //授業日数
                    + "CASE WHEN S1.SEM_OFFDAYS = '1' "
                         + "THEN VALUE(CLASSDAYS,0) "
                         + "ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) "
                         + "END AS ATTEND_1," //授業日数-休学日数:1
                    + "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR,"          //出停・忌引
                    + "VALUE(SUSPEND,0) AS SUSPEND,"                                //出停:2
                    + "VALUE(MOURNING,0) AS MOURNING,"                              //忌引:3
                    + "VALUE(ABROAD,0) AS ABROAD,"                                  //留学:4
                    + "CASE WHEN S1.SEM_OFFDAYS = '1' "
                         + "THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) "
                         + "ELSE VALUE(REQUIREPRESENT,0) "
                         + "END AS REQUIREPRESENT," //要出席日数:5
                    + "CASE WHEN S1.SEM_OFFDAYS = '1' "
                         + "THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) "
                         + "ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) "
                         + "END AS ATTEND_6," //病欠＋事故欠（届・無）:6
                    + "VALUE(PRESENT,0) AS PRESENT,"                                //出席日数:7
                    + "VALUE(MOURNING,0) + VALUE(SUSPEND,0) AS ATTEND_8 "           //忌引＋出停:8
                + "FROM "
                    + "("
                        + "SELECT "
                            + "SCHREGNO,"
                            + "YEAR,"
                            + "ANNUAL,"
                            + "SUM(CLASSDAYS) AS CLASSDAYS,"
                            + "SUM(OFFDAYS) AS OFFDAYS,"
                            + "SUM(ABSENT) AS ABSENT,"
                            + "SUM(SUSPEND) AS SUSPEND,"
                            + "SUM(MOURNING) AS MOURNING,"
                            + "SUM(ABROAD) AS ABROAD,"
                            + "SUM(REQUIREPRESENT) AS REQUIREPRESENT,"
                            + "SUM(SICK) AS SICK,"
                            + "SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE,"
                            + "SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,"
                            + "SUM(PRESENT) AS PRESENT "
                        + "FROM "
                            +  tname1 + " "             //05/07/25
                        + "WHERE "
                                + "SCHREGNO =? "
                            + "AND YEAR <=? ";
            if ("on".equals(notPrintAnotherAttendrec)) {
                       sql+=  "AND SCHOOLCD <> '1' ";
            }
                   sql+=  "GROUP BY "
                            + "SCHREGNO,"
                            + "ANNUAL,"
                            + "YEAR "
                    + ")T1 "
                    + "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR "
                + "ORDER BY "
                    + "T1.ANNUAL";

        } catch (Exception ex) {
            log.error("[KNJ_AttendrecSql]pre_sql_Pattern1 error! ", ex);
        }
        return sql;
    }


    /**
     *
     *  出欠記録(SCHREG_ATTENDREC_DAT)ＳＱＬの作成
     *  2005/07/10 Build 授業日数は休学日数を引かない日数とする
     *  2005/10/26 Modify 授業日数は留学日数と休学日数を引いた値とする
     *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
     */
    protected String pre_sql_Pattern2(final String notPrintAnotherAttendrec) {

        if (tname1 == null) setFieldName();   //使用テーブル名設定 05/07/25Build
        String sql = null;
        try{
            sql = "SELECT DISTINCT "
                    + "T1.YEAR,"
                    + "ANNUAL,"
                    + "VALUE(CLASSDAYS,0)                      AS CLASSDAYS,"           //授業日数
                    + "CASE WHEN S1.SEM_OFFDAYS = '1' "
                         + "THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) "
                         + "ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) "
                         + "END AS ATTEND_1," //授業日数-休学日数:1
                    + "VALUE(SUSPEND,0) + VALUE(MOURNING,0)    AS SUSP_MOUR,"           //出停・忌引
                    + "VALUE(SUSPEND,0)                        AS SUSPEND,"             //出停:2
                    + "VALUE(MOURNING,0)                       AS MOURNING,"            //忌引:3
                    + "VALUE(ABROAD,0)                         AS ABROAD,"              //留学:4
                    + "CASE WHEN S1.SEM_OFFDAYS = '1' "
                         + "THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) "
                         + "ELSE VALUE(REQUIREPRESENT,0) "
                         + "END AS REQUIREPRESENT," //要出席日数:5
                    + "CASE WHEN S1.SEM_OFFDAYS = '1' "
                         + "THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) "
                         + "ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) "
                         + "END AS ATTEND_6," //病欠＋事故欠（届・無）:6
                    + "VALUE(PRESENT,0)                        AS PRESENT,"             //出席日数:7
                    + "VALUE(MOURNING,0) + VALUE(SUSPEND,0)    AS ATTEND_8 "            //忌引＋出停:8
                + "FROM "
                    + "("
                        + "SELECT "
                            + "SCHREGNO,"
                            + "YEAR,"
                            + "ANNUAL,"
                            + "SUM(CLASSDAYS)        AS CLASSDAYS,"
                            + "SUM(OFFDAYS)          AS OFFDAYS,"
                            + "SUM(ABSENT)           AS ABSENT,"
                            + "SUM(SUSPEND)          AS SUSPEND,"
                            + "SUM(MOURNING)         AS MOURNING,"
                            + "SUM(ABROAD)           AS ABROAD,"
                            + "SUM(REQUIREPRESENT)   AS REQUIREPRESENT,"
                            + "SUM(SICK)             AS SICK,"
                            + "SUM(ACCIDENTNOTICE)   AS ACCIDENTNOTICE,"
                            + "SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,"
                            + "SUM(PRESENT)          AS PRESENT "
                        + "FROM "
                            +  tname1 + " "                   //05/07/25
                        + "WHERE "
                                + "SCHREGNO =? "
                            + "AND YEAR <=? ";
            if ("on".equals(notPrintAnotherAttendrec)) {
                sql+=  "AND SCHOOLCD <> '1' ";
            }

                   sql+=  "GROUP BY "
                            + "SCHREGNO,"
                            + "ANNUAL,"
                            + "YEAR "
                    + ")T1 "
                    + "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR "
                + "ORDER BY "
                    + "T1.ANNUAL";

        } catch (Exception ex) {
            log.error("[KNJ_AttendrecSql]pre_sql_Pattern2 error! ", ex);
        }
        return sql;
    }

    /**
     *
     *  出欠記録取得TITLEのSQL
     *  2005/07/10 Build 旧pre_sqlメソッドを標準SQLとし、例外のSQLのメソッドを追加
     *
     */
    public String preSqlTitle(final DB2UDB db2, final String year)
    {
        String retval = null;
        try {

            if (_defineschool == null) {
                _defineschool = new KNJDefineSchool();
                _defineschool.defineCode(db2, year);
                log.debug("schoolmark = " + _defineschool.schoolmark);
            }

            if (_defineschool.schoolmark.substring(0, 1).equals("K")) {
                retval = preSqlPatternTitle2();
            } else {
                retval = preSqlPatternTitle1();
            }

        } catch (Exception ex) {
            log.error("definecode error!", ex);
        }

        return retval;
    }


    /**
     *
     *  出欠記録(SCHREG_ATTENDREC_DAT)TITLEＳＱＬの作成
     *
     */
    public String preSqlPatternTitle1() {

        if (tname1 == null) setFieldName();   //使用テーブル名設定 05/07/25Build
        String sql = null;
        try{
            sql = "SELECT DISTINCT "
                    + "YEAR,"
                    + "ANNUAL "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "SCHREGNO,"
                            + "YEAR,"
                            + "ANNUAL,"
                            + "SUM(CLASSDAYS) AS CLASSDAYS,"
                            + "SUM(OFFDAYS) AS OFFDAYS,"
                            + "SUM(ABSENT) AS ABSENT,"
                            + "SUM(SUSPEND) AS SUSPEND,"
                            + "SUM(MOURNING) AS MOURNING,"
                            + "SUM(ABROAD) AS ABROAD,"
                            + "SUM(REQUIREPRESENT) AS REQUIREPRESENT,"
                            + "SUM(SICK) AS SICK,"
                            + "SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE,"
                            + "SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,"
                            + "SUM(PRESENT) AS PRESENT "
                        + "FROM "
                            +  tname1 + " "             //05/07/25
                        + "WHERE "
                                + "SCHREGNO =? "
                            + "AND YEAR <=? "
                        + "GROUP BY "
                            + "SCHREGNO,"
                            + "ANNUAL,"
                            + "YEAR "
                    + ")T1 ";
            sql = sql
                + "ORDER BY "
                    + "T1.ANNUAL";

        } catch (Exception ex) {
            log.error("[KNJ_AttendrecSql]pre_sql_Pattern1 error! ", ex);
        }
        return sql;
    }


    /**
     *
     *  出欠記録(SCHREG_ATTENDREC_DAT)TITLEＳＱＬの作成
     */
    public String preSqlPatternTitle2() {

        if (tname1 == null) setFieldName();   //使用テーブル名設定 05/07/25Build
        String sql = null;
        try{
            sql = "SELECT DISTINCT "
                    + "YEAR,"
                    + "ANNUAL "
                + "FROM "
                    + "("
                        + "SELECT "
                            + "SCHREGNO,"
                            + "YEAR,"
                            + "ANNUAL,"
                            + "SUM(CLASSDAYS)        AS CLASSDAYS,"
                            + "SUM(OFFDAYS)          AS OFFDAYS,"
                            + "SUM(ABSENT)           AS ABSENT,"
                            + "SUM(SUSPEND)          AS SUSPEND,"
                            + "SUM(MOURNING)         AS MOURNING,"
                            + "SUM(ABROAD)           AS ABROAD,"
                            + "SUM(REQUIREPRESENT)   AS REQUIREPRESENT,"
                            + "SUM(SICK)             AS SICK,"
                            + "SUM(ACCIDENTNOTICE)   AS ACCIDENTNOTICE,"
                            + "SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,"
                            + "SUM(PRESENT)          AS PRESENT "
                        + "FROM "
                            +  tname1 + " "                   //05/07/25
                        + "WHERE "
                                + "SCHREGNO =? "
                            + "AND YEAR <=? "
                        + "GROUP BY "
                            + "SCHREGNO,"
                            + "ANNUAL,"
                            + "YEAR "
                    + ")T1 ";
            sql = sql
                + "ORDER BY "
                    + "T1.ANNUAL";

        } catch (Exception ex) {
            log.error("[KNJ_AttendrecSql]pre_sql_Pattern2 error! ", ex);
        }
        return sql;
    }


    /**
     *  在校生用と卒業生用のテーブル名を設定
     *  2005/07/25 Build 在校生用と卒業生用で共有する
     */
    public void setFieldName()
    {
        if (_isGrd) {
            tname1 = "GRD_ATTENDREC_DAT";
        } else {
            tname1 = "SCHREG_ATTENDREC_DAT";
        }
    }

}
