// kanji=漢字
/*
 * $Id: 1b56e6b0b59d4b6dceed533509dcc4af61446309 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA.detail;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;

/**
 *
 *     ＜ＫＮＪ＿ＴｒａｎｓｆｅｒｒｅｃＳｑｌ＞ 生徒異動情報履歴取得ＳＱＬ
 *
 *  パラメーターの設定(setXXX)
 *      1.学籍番号 2.学籍番号 3.学籍番号 4.学籍番号 5.学籍番号 6.年度
 *
 *  2006/04/28 yamashiro ○取得列(フィールド)を追加　--NO001
 *                       ○卒業生台帳番号の列名を変更　--NO001
 */

public class KNJ_TransferRecSql {

    private static final Log log = LogFactory.getLog(KNJ_TransferRecSql.class);
    
    public KNJ_TransferRecSql() {
        log.debug(" $Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
    }

    public String sql_state () {
        return sql_state(new HashMap());
    }

    public String sql_state (final Map paramMap) {
        final SqlBase sqlobj;
        final KNJDefineCode definecode = new KNJDefineCode();
        if (definecode.schoolmark.equals("HIRO")) {
            sqlobj = new SqlHiro(paramMap);
        } else {
            sqlobj = new SqlCommon(paramMap);
        }
        return sqlobj.sql_state();
    }

    //--- 内部クラス -------------------------------------------------------
    private abstract class SqlBase {
        
        protected final Map _paramMap;

        SqlBase(final Map paramMap) {
            _paramMap = paramMap;
        }

        abstract String sql_state ();
    }
    
    //--- 内部クラス -------------------------------------------------------
    //標準版（東京都基準）
    private class SqlCommon extends SqlBase {

        SqlCommon(final Map paramMap) {
            super(paramMap);
        }

        String sql_state () {

            final StringBuffer sql = new StringBuffer();
            sql.append(      "SELECT ");
            sql.append(      "T1.YEAR,");
            sql.append(      "T1.SDATE,");
            sql.append(      "T1.EDATE,");
            sql.append(      "T1.REASON,");
            sql.append(      "T1.PLACE,");
            sql.append(      "T1.ADDR,");
            if ("1".equals(_paramMap.get("useAddrField2"))) {
                sql.append(      "T1.ADDR2,");
            }
            sql.append(      "T1.CERTIFNO,");      //NO001
            sql.append(      "T1.NAMECD2,");
            sql.append(      "T1.NAMECD1,");
            sql.append(      "T3.NAME1,");
            sql.append(      "CASE T2.SCHOOLDIV WHEN '0' THEN T4.GRADE ELSE T5.GRADE END AS GRADE, ");
            sql.append(      "CASE T2.SCHOOLDIV WHEN '0' THEN T4_2.GRADE_CD ELSE T5_2.GRADE_CD END AS GRADE_CD ");
            sql.append(  "FROM ");
            sql.append(      "(");
            sql.append(          "SELECT ");
            sql.append(              "FISCALYEAR(ENT_DATE) AS YEAR,");
            sql.append(              "ENT_DATE AS SDATE,");
            sql.append(              "ENT_DATE AS EDATE,");
            sql.append(              "ENT_REASON AS REASON,");//NO001 入学理由
            sql.append(              "ENT_SCHOOL AS PLACE,"); //NO001 入学学校
            sql.append(              "ENT_ADDR AS ADDR,");    //NO001 入学住所
            if ("1".equals(_paramMap.get("useAddrField2"))) {
                sql.append(              "ENT_ADDR2 AS ADDR2,");
            }
            sql.append(              "'' AS CERTIFNO,");      //NO001
            sql.append(              "ENT_DIV AS NAMECD2,");
            sql.append(              "'A002' AS NAMECD1 ");
            sql.append(          "FROM ");
            sql.append(              "SCHREG_BASE_MST ");
            sql.append(          "WHERE ");
            sql.append(              "SCHREGNO=? ");
            sql.append(          "UNION SELECT ");
            sql.append(              "FISCALYEAR(GRD_DATE) AS YEAR,");
            sql.append(              "GRD_DATE AS SDATE,");
            sql.append(              "GRD_DATE AS EDATE,");
            sql.append(              "GRD_REASON AS REASON,");//除籍(卒業)事由
            sql.append(              "GRD_SCHOOL AS PLACE,"); //NO001 除籍(卒業)学校
            sql.append(              "GRD_ADDR AS ADDR,");    //NO001 除籍(卒業)住所
            if ("1".equals(_paramMap.get("useAddrField2"))) {
                sql.append(              "GRD_ADDR2 AS ADDR2,");
            }
            sql.append(              "GRD_NO AS CERTIFNO,");  //NO001 卒業生台帳番号
            sql.append(              "GRD_DIV AS NAMECD2,");
            sql.append(              "'A003' AS NAMECD1 ");
            sql.append(          "FROM ");
            sql.append(              "SCHREG_BASE_MST ");
            sql.append(          "WHERE ");
            sql.append(              "SCHREGNO=? ");
            sql.append(          "UNION SELECT ");
            sql.append(              "FISCALYEAR(TRANSFER_SDATE) AS YEAR,");
            sql.append(              "TRANSFER_SDATE AS SDATE,");
            sql.append(              "TRANSFER_EDATE AS EDATE,");
            sql.append(              "TRANSFERREASON AS REASON,");
            sql.append(              "TRANSFERPLACE AS PLACE,");
            sql.append(              "TRANSFERADDR AS ADDR,");
            if ("1".equals(_paramMap.get("useAddrField2"))) {
                sql.append(              "CAST(NULL AS VARCHAR(1)) AS ADDR2,");
            }
            sql.append(              "'' AS CERTIFNO,");      //NO001
            sql.append(              "TRANSFERCD AS NAMECD2,");
            sql.append(              "'A004' AS NAMECD1 ");
            sql.append(          "FROM ");
            sql.append(              "SCHREG_TRANSFER_DAT ");
            sql.append(          "WHERE ");
            sql.append(              "SCHREGNO=? ");
            sql.append(      ")T1 ");
            sql.append(      "INNER JOIN SCHOOL_MST T2 ON T2.YEAR=T1.YEAR ");
            sql.append(      "INNER JOIN NAME_MST T3 ON T3.NAMECD1=T1.NAMECD1 AND T3.NAMECD2=T1.NAMECD2 ");
            sql.append(      "LEFT JOIN(");
            sql.append(          "SELECT ");
            sql.append(              "'0' AS SCHOOLDIV,");
            sql.append(              "YEAR,");
            sql.append(              "GRADE ");
            sql.append(          "FROM ");
            sql.append(              "V_REGDYEAR_GRADE_DAT ");     //学年制
            sql.append(          "WHERE ");
            sql.append(              "SCHREGNO=? ");
            sql.append(      ")T4 ON T4.YEAR=T2.YEAR AND T4.SCHOOLDIV=T2.SCHOOLDIV ");
            sql.append(      "LEFT JOIN SCHREG_REGD_GDAT T4_2 ON T4_2.YEAR = T4.YEAR AND T4_2.GRADE = T4.GRADE ");
            sql.append(      "LEFT JOIN(");
            sql.append(          "SELECT ");
            sql.append(              "'1' AS SCHOOLDIV,");
            sql.append(              "YEAR,");
            sql.append(              "GRADE ");
            sql.append(          "FROM ");
            sql.append(              "V_REGDYEAR_UNIT_DAT ");      //単位制
            sql.append(          "WHERE ");
            sql.append(              "SCHREGNO=? ");
            sql.append(      ")T5 ON T5.YEAR=T2.YEAR AND T5.SCHOOLDIV=T2.SCHOOLDIV ");
            sql.append(      "LEFT JOIN SCHREG_REGD_GDAT T5_2 ON T5_2.YEAR = T5.YEAR AND T5_2.GRADE = T5.GRADE ");
            sql.append(  "WHERE ");
            sql.append(      "T1.YEAR<=? ");
            sql.append(  "ORDER BY ");
            sql.append(      "NAMECD1,NAMECD2,SDATE");
        
            return sql.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    //広島版
    private class SqlHiro extends SqlBase {

        SqlHiro(final Map paramMap) {
            super(paramMap);
        }
        
        String sql_state () {
            
            String sql;
            sql = "SELECT "
                + "T1.YEAR,"
                + "T1.SDATE,"
                + "T1.EDATE,"
                + "T1.REASON,"
                + "T1.PLACE,"
                + "T1.ADDR,"
                + "T1.CERTIFNO,"        //NO001
                // 指導要録印刷プログラムでは、編入データを SCHREG_BASE_MST に在るものとして扱っているので、
                // ここで SCHREG_TRANSFER_DAT の内容を書き換えておく。
                + "CASE WHEN T1.NAMECD1 = 'A004' AND T1.NAMECD2 = '4' THEN '5' ELSE T1.NAMECD2 END AS NAMECD2,"
                + "CASE WHEN T1.NAMECD1 = 'A004' AND T1.NAMECD2 = '4' THEN 'A002' ELSE T1.NAMECD1 END AS NAMECD1,"
                + "T3.NAME1,"
                + "CASE T2.SCHOOLDIV WHEN '0' THEN T4.GRADE ELSE T5.GRADE END AS GRADE "
            + "FROM "
                + "("
                    + "SELECT "
                        + "FISCALYEAR(ENT_DATE) AS YEAR,"
                        + "ENT_DATE AS SDATE,"
                        + "ENT_DATE AS EDATE,"
                        + "'' AS REASON,"  //NO001 入学理由
                        + "'' AS PLACE,"   //NO001 入学学校
                        + "'' AS ADDR,"      //NO001 入学住所
                        + "'' AS CERTIFNO,"        //NO001
                        + "ENT_DIV AS NAMECD2,"
                        + "'A002' AS NAMECD1 "
                    + "FROM "
                        + "SCHREG_BASE_MST "
                    + "WHERE "
                        + "SCHREGNO=? "
                        + "AND ENT_DIV <> '5' "  // 編入は除外 広島では使用不可
                    + "UNION SELECT "
                        + "FISCALYEAR(GRD_DATE) AS YEAR,"
                        + "GRD_DATE AS SDATE,"
                        + "GRD_DATE AS EDATE,"
                        + "GRD_REASON AS REASON,"  //除籍(卒業)事由
                        + "'' AS PLACE,"   //NO001 除籍(卒業)学校
                        + "'' AS ADDR,"      //NO001 除籍(卒業)住所
                        + "GRD_NO AS CERTIFNO,"    //NO001 卒業生台帳番号
                        + "GRD_DIV AS NAMECD2,"
                        + "'A003' AS NAMECD1 "
                    + "FROM "
                        + "SCHREG_BASE_MST "
                    + "WHERE "
                        + "SCHREGNO=? "
                    + "UNION SELECT "
                        + "FISCALYEAR(TRANSFER_SDATE) AS YEAR,"
                        + "TRANSFER_SDATE AS SDATE,"
                        + "TRANSFER_EDATE AS EDATE,"
                        + "TRANSFERREASON AS REASON,"
                        + "TRANSFERPLACE AS PLACE,"
                        + "TRANSFERADDR AS ADDR,"
                        + "'' AS CERTIFNO,"        //NO001
                        + "TRANSFERCD AS NAMECD2,"
                        + "'A004' AS NAMECD1 "
                    + "FROM "
                        + "SCHREG_TRANSFER_DAT "
                    + "WHERE "
                        + "SCHREGNO=? "
                + ")T1 "
                + "INNER JOIN SCHOOL_MST T2 ON T2.YEAR=T1.YEAR "
                + "INNER JOIN NAME_MST T3 ON T3.NAMECD1=T1.NAMECD1 AND T3.NAMECD2=T1.NAMECD2 "
                + "LEFT JOIN("
                    + "SELECT "
                        + "'0' AS SCHOOLDIV,"
                        + "YEAR,"
                        + "GRADE "
                    + "FROM "
                        + "V_REGDYEAR_GRADE_DAT "       //学年制
                    + "WHERE "
                        + "SCHREGNO=? "
                + ")T4 ON T4.YEAR=T2.YEAR AND T4.SCHOOLDIV=T2.SCHOOLDIV "
                + "LEFT JOIN("
                    + "SELECT "
                        + "'1' AS SCHOOLDIV,"
                        + "YEAR,"
                        + "GRADE "
                    + "FROM "
                        + "V_REGDYEAR_UNIT_DAT "        //単位制
                    + "WHERE "
                        + "SCHREGNO=? "
                + ")T5 ON T5.YEAR=T2.YEAR AND T5.SCHOOLDIV=T2.SCHOOLDIV "
            + "WHERE "
                + "T1.YEAR<=? "
            + "ORDER BY "
                + "NAMECD1,NAMECD2,SDATE";
        
            return sql;
        }
    }
    
}
