// kanji=漢字
/*
 * $Id: a5d3024499f66e9111bb7b8c68686c528a67baa4 $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 学校情報取得ＳＱＬ
 */
// 2005/11/18 yamashiro 学校名の取得年度を追加( <=t_switchの３番目のスイッチ )

public class KNJ_SchoolinfoSql {

    private static final Log log = LogFactory.getLog(KNJ_SchoolinfoSql.class);
    private static final String key = "FRED";
    final String _switch;

    public KNJ_SchoolinfoSql(final String t_switch) {
        _switch = t_switch + "00000";
    }

    public String pre_sql() {
        return pre_sql(new HashMap());
    }

    /**
     *  学校情報
     *      ・クラス変数SWITCHにより、検索を制御
     *
     *      ・t_switch -->  //各種設定  初期値は0
     *              1番目  1:校長検索
     *              2番目  1:担当者をJOBCDで検索 2:担当者をSTAFFCDで検索
     *              3番目  1:任意の年度による学校名の取得
     */
    public String pre_sql(final Map paramMap) {

        final StringBuffer sql = new StringBuffer();
        final String s0 = _switch.substring(0,1);
        final String s1 = _switch.substring(1,2);
        final String s2 = _switch.substring(2,3);

        final String schoolMstSchoolKind = (String) paramMap.get("schoolMstSchoolKind");

        try{
            sql.append(" SELECT ");
            sql.append("     T1.YEAR ");
            sql.append("     ,T1.FOUNDEDYEAR");
            sql.append("     ,T1.CLASSIFICATION");
            sql.append("     ,T1.PRESENT_EST");
            sql.append("     ,T1.SCHOOLNAME1");
            sql.append("     ,T1.SCHOOLNAME2");
            sql.append("     ,T1.SCHOOLNAME3");
            sql.append("     ,T1.SCHOOLZIPCD");
            sql.append("     ,T1.SCHOOLADDR1");
            sql.append("     ,T1.SCHOOLADDR2");
            sql.append("     ,T1.SCHOOLNAME_ENG");
            sql.append("     ,T1.SCHOOLADDR1_ENG");
            sql.append("     ,T1.SCHOOLADDR2_ENG");
            sql.append("     ,T1.SCHOOLTELNO");
            sql.append("     ,T1.SEMESTERDIV");
            sql.append("     ,T1.SCHOOLDIV ");
            if (!"0".equals(s0)) {
            	sql.append(",T2.JOBCD AS PRINCIPAL_JOBCD");
                sql.append(",T2.STAFFCD AS PRINCIPAL_STAFFCD");
                sql.append(",T2.STAFFNAME AS PRINCIPAL_NAME");
                sql.append(",T2.STAFFNAME_ENG AS PRINCIPAL_NAME_ENG");
                sql.append(",T2.JOBNAME AS PRINCIPAL_JOBNAME ");
            }
            if (!"0".equals(s1)) {
                sql.append(",T3.STAFFCD AS STAFF2_STAFFCD");
                sql.append(",T3.STAFFNAME AS STAFF2_NAME");
                sql.append(",T3.STAFFNAME_ENG AS STAFF2_NAME_ENG");
                sql.append(",T3.JOBNAME AS STAFF2_JOBNAME ");
            }
            if ("1".equals(s2)) {      //任意の年度のSCHOO_MSTの表
                sql.append(",T4.CLASSIFICATION AS T4CLASSIFICATION ");     //県立、私立など
                sql.append(",T4.SCHOOLNAME1 AS T4SCHOOLNAME1");
                sql.append(",T4.SCHOOLNAME2 AS T4SCHOOLNAME2");
                sql.append(",T4.SCHOOLNAME3 AS T4SCHOOLNAME3 ");
                sql.append(",T4.SCHOOLNAME_ENG AS T4SCHOOLNAME_ENG ");
                sql.append(",T4.SEMESTERDIV AS T4SEMESTERDIV ");
                sql.append(",T4.SCHOOLDIV AS T4SCHOOLDIV ");               //0：学年制／1：単位制
            }
            sql.append(" FROM ");
                //  学校
            sql.append("     (");
            sql.append("         SELECT ");
            sql.append("             '" + key + "' AS KEY,");
            sql.append("             YEAR,");
            sql.append("             FOUNDEDYEAR,");
            sql.append("             CLASSIFICATION,");
            sql.append("             PRESENT_EST,");
            sql.append("             SCHOOLNAME1,");
            sql.append("             SCHOOLNAME2,");
            sql.append("             SCHOOLNAME3,");
            sql.append("             SCHOOLZIPCD,");
            sql.append("             SCHOOLADDR1,");
            sql.append("             SCHOOLADDR2,");
            sql.append("             SCHOOLNAME_ENG,");
            sql.append("             SCHOOLADDR1_ENG,");
            sql.append("             SCHOOLADDR2_ENG,");
            sql.append("             SCHOOLTELNO,");
            sql.append("             SEMESTERDIV,");
            sql.append("             SCHOOLDIV ");
            sql.append("         FROM ");
            sql.append("             SCHOOL_MST W1 ");
            sql.append("         WHERE ");
            sql.append("             YEAR=? ");
            if (null != schoolMstSchoolKind) { sql.append(" AND SCHOOL_KIND = '" + schoolMstSchoolKind + "' "); }
            sql.append("     )T1 ");
            if ("1".equals(s0)) {      //JOBCD(校長)で検索
                //  校長
                sql.append(" LEFT JOIN(");
                sql.append("     SELECT ");
                sql.append("         '" + key + "' AS KEY,");
                sql.append("         W2.JOBCD,");
                sql.append("         W1.STAFFCD,");
                sql.append("         STAFFNAME,");
                sql.append("         STAFFNAME_ENG,");
                sql.append("         JOBNAME ");
                sql.append("     FROM ");
                sql.append("         STAFF_YDAT W1 ");
                sql.append("         INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
                sql.append("         LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
                sql.append("     WHERE ");
                sql.append("         W1.YEAR=? AND (W2.JOBCD = '0001' OR W2.JOBCD = '0005') ");
                sql.append(" )T2 ON T1.KEY = T2.KEY ");
            }
            if ("1".equals(s1)) {      //JOBCDで検索
                //  担当教諭
                sql.append(" LEFT JOIN(");
                sql.append("     SELECT ");
                sql.append("         '" + key + "' AS KEY,");
                sql.append("         W1.STAFFCD,");
                sql.append("         STAFFNAME,");
                sql.append("         STAFFNAME_ENG,");
                sql.append("         JOBNAME ");
                sql.append("     FROM ");
                sql.append("         STAFF_YDAT W1 ");
                sql.append("         INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
                sql.append("         LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
                sql.append("     WHERE ");
                sql.append("         W1.YEAR =? AND W1.JOBCD =? ");
                sql.append(" )T3 ON T1.KEY = T3.KEY ");
            } else if ("2".equals(s1)) {      //STAFFCDで検索
                //  担当教諭
                sql.append(" LEFT JOIN(");
                sql.append("     SELECT ");
                sql.append("         '" + key + "' AS KEY,");
                sql.append("         W2.STAFFCD,");
                sql.append("         STAFFNAME,");
                sql.append("         STAFFNAME_ENG,");
                sql.append("         JOBNAME ");
                sql.append("     FROM ");
                sql.append("         STAFF_MST W2 ");
                sql.append("         LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
                sql.append("     WHERE ");
                sql.append("         W2.STAFFCD =? ");
                sql.append(" )T3 ON T1.KEY = T3.KEY ");
            }
            if ("1".equals(s2)) {      //任意の年度のSCHOO_MSTの表
                sql.append(" LEFT JOIN(");
                sql.append("     SELECT  '" + key + "' AS KEY ");
                sql.append("            ,CLASSIFICATION ");     //県立、私立など
                sql.append("            ,SCHOOLNAME1");
                sql.append("            ,SCHOOLNAME2");
                sql.append("            ,SCHOOLNAME3 ");
                sql.append("            ,SCHOOLNAME_ENG ");
                sql.append("            ,SEMESTERDIV ");
                sql.append("            ,SCHOOLDIV ");          //0：学年制／1：単位制
                sql.append("     FROM    SCHOOL_MST W1 ");
                sql.append("     WHERE   YEAR = ? ");
                if (null != schoolMstSchoolKind) { sql.append(" AND SCHOOL_KIND = '" + schoolMstSchoolKind + "' "); }
                sql.append(" )T4 ON T1.KEY = T4.KEY ");
            }
            if ("1".equals(s0)) {
            	sql.append(" ORDER BY T2.JOBCD,T2.STAFFCD ");
            }
        } catch (Exception ex) {
            log.error("pre_sql error! sql = " + sql.toString(), ex);
        }

        return sql.toString();

    }//pre_sqlの括り


}//クラスの括り
