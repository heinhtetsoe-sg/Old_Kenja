package servletpack.KNJG.detail;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  学校情報取得ＳＱＬ
 *
 *  2005/10/22 yamashiro 東京都専用とし’/KNJG/detail/’配下へ置く => ’/KNJZ/detail/’へは共通の同プログラムを置く
 *  2005/10/25 yamashiro メソッドString pre_sqlを変更 => SCHOOL_MSTの学校名、STAFF_MST,JOB_MSTの名称取得を追加
 */

public class KNJ_SchoolinfoSql{

    private static final Log log = LogFactory.getLog(KNJ_SchoolinfoSql.class);
    private static final String key = "FRED";

    final String _switch;
    private boolean _hasCertifSchoolName;

    public KNJ_SchoolinfoSql(final String t_switch) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $");
        _switch = t_switch + "00000";
    }

    /*-----------------------------------------------------------------*
     *  学校情報
     *      ・クラス変数SWITCHにより、検索を制御
     *
     *      ・t_switch -->  //各種設定  初期値は0
     *              1=1:校長検索
     *              2=1:担当者をJOBCDで検索 2=2:担当者をSTAFFCDで検索
     *-----------------------------------------------------------------*/
    public String pre_sql() {
        return pre_sql(new HashMap());
    }

    public String pre_sql(final Map paramMap) {

        final boolean useZdetail = "1".equals(paramMap.get("useZdetail"));
        final String schoolMstSchoolKind = (String) paramMap.get("schoolMstSchoolKind");

        final String s0 = _switch.substring(0,1);
        final String s1 = _switch.substring(1,2);
        final String s2 = _switch.substring(2,3);
        final StringBuffer sql = new StringBuffer();
        
        final String q = "?";
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
            
            if (!"0".equals(s0)) {      //JOBCD(校長)で検索
                sql.append(" ,T2.STAFFCD AS STAFF_PRINCIPAL_STAFFCD ");
                sql.append(" ,T2.STAFFNAME AS STAFF_PRINCIPANAME ");
                sql.append(" ,T2.STAFFNAME_ENG AS PRINCIPAL_NAME_ENG ");
                sql.append(" ,T2.JOBNAME AS STAFF_JOBNAME ");
            }
            if(!"0".equals(s1)) {
                sql.append(" ,T3.STAFFCD AS STAFF2_STAFFCD ");
                sql.append(" ,T3.STAFFNAME AS STAFF2_NAME ");
                sql.append(" ,T3.STAFFNAME_ENG AS STAFF2_NAME_ENG ");
                sql.append(" ,T3.JOBNAME AS STAFF2_JOBNAME ");
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
            if (!useZdetail) {
                sql.append("    ,T1.SYOSYO_NAME");
                sql.append("    ,T1.SYOSYO_NAME2");
                sql.append("    ,T1.CERTIF_NO");
                sql.append("    ,T1.PRINCIPAL_NAME AS PRINCIPAL_NAME ");
                sql.append("    ,T1.JOB_NAME AS PRINCIPAL_JOBNAME ");
                sql.append("    ,T1.COMMONSCHOOLNAME1 ");
                sql.append("    ,T1.REMARK1");
                sql.append("    ,T1.REMARK2");
                sql.append("    ,T1.REMARK3");
                sql.append("    ,T1.REMARK4");
                sql.append("    ,T1.REMARK5 ");
                sql.append("    ,T1.REMARK6");
                sql.append("    ,T1.REMARK7");
                sql.append("    ,T1.REMARK8");
                sql.append("    ,T1.REMARK9");
                sql.append("    ,T1.REMARK10 ");
            }

            sql.append(" FROM ");
            //  学校
            sql.append("     (");
            sql.append("         SELECT ");
            sql.append("             '" + key + "' AS KEY ");
            sql.append("            ,W1.YEAR");
            sql.append("            ,W1.FOUNDEDYEAR");
            sql.append("            ,W1.CLASSIFICATION");
            sql.append("            ,W1.PRESENT_EST");
            sql.append("            ,W1.SCHOOLNAME2");
            sql.append("            ,W1.SCHOOLNAME3");
            sql.append("            ,W1.SCHOOLZIPCD");
            sql.append("            ,W1.SCHOOLADDR1");
            sql.append("            ,W1.SCHOOLADDR2");
            sql.append("            ,W1.SCHOOLNAME_ENG");
            sql.append("            ,W1.SCHOOLADDR1_ENG");
            sql.append("            ,W1.SCHOOLADDR2_ENG");
            sql.append("            ,W1.SCHOOLTELNO");
            sql.append("            ,W1.SEMESTERDIV");
            sql.append("            ,W1.SCHOOLDIV ");
            if (useZdetail) {
                sql.append("             ,SCHOOLNAME1 ");
                sql.append("         FROM ");
                sql.append("             SCHOOL_MST W1 ");
                sql.append("         WHERE ");
                sql.append("             YEAR=? ");
                if (null != schoolMstSchoolKind) { sql.append(" AND SCHOOL_KIND = '" + schoolMstSchoolKind + "' "); }
            } else {
                sql.append("            ,W2.SCHOOL_NAME AS SCHOOLNAME1");
                sql.append("            ,W2.SYOSYO_NAME ");
                sql.append("            ,W2.SYOSYO_NAME2 ");
                sql.append("            ,W2.CERTIF_NO ");
                sql.append("            ,W2.JOB_NAME");
                sql.append("            ,W2.PRINCIPAL_NAME ");
                sql.append("            ,W1.SCHOOLNAME1 AS COMMONSCHOOLNAME1 ");
                sql.append("            ,W3.REMARK1");
                sql.append("            ,W3.REMARK2");
                sql.append("            ,W3.REMARK3");
                sql.append("            ,W2.REMARK4");
                sql.append("            ,W2.REMARK5");
                sql.append("            ,W2.REMARK6");
                sql.append("            ,W2.REMARK7");
                sql.append("            ,W2.REMARK8");
                sql.append("            ,W2.REMARK9");
                sql.append("            ,W2.REMARK10 ");
                sql.append("         FROM ");
                sql.append("             (SELECT * FROM SCHOOL_MST WHERE YEAR = " + q + " ");
                if (null != schoolMstSchoolKind) { sql.append(" AND SCHOOL_KIND = '" + schoolMstSchoolKind + "' "); }
                sql.append("              ) W1 ");
                sql.append("             LEFT JOIN CERTIF_SCHOOL_DAT W2 ON W2.CERTIF_KINDCD = " + q + " AND W1.YEAR = W2.YEAR ");
                sql.append("             LEFT JOIN CERTIF_SCHOOL_DAT W3 ON W3.CERTIF_KINDCD = " + q + " AND W1.YEAR = W3.YEAR ");
            }
            sql.append("     )T1 ");
            if ("1".equals(s0)) {      //JOBCD(校長)で検索
                //  校長
                sql.append(" LEFT JOIN(");
                sql.append("     SELECT ");
                sql.append("         '" + key + "' AS KEY");
                sql.append("         ,W1.STAFFCD");
                sql.append("         ,STAFFNAME");
                sql.append("         ,STAFFNAME_ENG");
                sql.append("         ,JOBNAME ");
                sql.append("     FROM ");
                sql.append("         STAFF_YDAT W1 ");
                sql.append("         INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
                sql.append("         LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
                sql.append("     WHERE ");
                if (useZdetail) {
                    sql.append("         W1.YEAR=  " + q + " AND (W2.JOBCD = '0001' OR W2.JOBCD = '0005') ");
                } else {
                    sql.append("         W1.YEAR = " + q + " AND W2.JOBCD='0001' ");
                }
                sql.append(" )T2 ON T1.KEY = T2.KEY ");
            }
            if ("1".equals(s1)) {      //JOBCDで検索
                //  担当教諭
                sql.append(" LEFT JOIN(");
                sql.append("     SELECT ");
                sql.append("         '" + key + "' AS KEY");
                sql.append("         ,W1.STAFFCD");
                sql.append("         ,STAFFNAME");
                sql.append("         ,STAFFNAME_ENG");
                sql.append("         ,JOBNAME ");
                sql.append("     FROM ");
                sql.append("         STAFF_YDAT W1 ");
                sql.append("         INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
                sql.append("         LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
                sql.append("     WHERE ");
                sql.append("         W1.YEAR = " + q + " AND W2.JOBCD = " + q + " ");
                sql.append(" )T3 ON T1.KEY = T3.KEY ");
            } else if ("2".equals(s1)) {      //STAFFCDで検索
                //  担当教諭
                sql.append(" LEFT JOIN(");
                sql.append("     SELECT ");
                sql.append("         '" + key + "' AS KEY");
                sql.append("         ,W2.STAFFCD");
                sql.append("         ,STAFFNAME");
                sql.append("         ,STAFFNAME_ENG");
                sql.append("         ,JOBNAME ");
                sql.append("     FROM ");
                sql.append("         STAFF_MST W2 ");
                sql.append("         LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
                sql.append("     WHERE ");
                sql.append("         W2.STAFFCD = " + q + " ");
                sql.append(" )T3 ON T1.KEY = T3.KEY ");
            }
            
            if ("1".equals(s2)) {      //任意の年度のSCHOO_MSTの表
                if (useZdetail) {
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
                } else {
                    sql.append(" LEFT JOIN(");
                    sql.append("     SELECT  '" + key + "' AS KEY ");
                    sql.append("            ,CLASSIFICATION ");     //県立、私立など
                    if (_hasCertifSchoolName) {
                        sql.append("        ,CASE WHEN W2.REMARK4 IS NOT NULL THEN W2.REMARK4 ELSE W1.SCHOOLNAME1 END AS SCHOOLNAME1");
                    } else {
                        sql.append("        ,SCHOOLNAME1");
                    }
                    sql.append("            ,SCHOOLNAME2, SCHOOLNAME3 ");
                    sql.append("            ,SCHOOLNAME_ENG ");
                    sql.append("            ,SEMESTERDIV ");
                    sql.append("            ,SCHOOLDIV ");          //0：学年制／1：単位制
                    sql.append("     FROM ");
                    sql.append("         (SELECT * FROM SCHOOL_MST WHERE YEAR = " + q + "");
                    if (null != schoolMstSchoolKind) { sql.append(" AND SCHOOL_KIND = '" + schoolMstSchoolKind + "' "); }
                    sql.append(") W1 ");
                    if (_hasCertifSchoolName) {
                        sql.append("      LEFT JOIN CERTIF_SCHOOL_DAT W2 ON W2.CERTIF_KINDCD = ? AND W1.YEAR = W2.YEAR");
                    }
                    sql.append(" )T4 ON T1.KEY = T4.KEY ");
                }
            }

        } catch (Exception ex) {
            log.error("[KNJ_SchoolinfoSql]pre_sql error!", ex);
        }
        return sql.toString();
    }

    /**
     * @param hasCertifSchoolName 設定する hasCertifSchoolName。
     */
    public void setHasCertifSchoolName(boolean hasCertifSchoolName) {
        _hasCertifSchoolName = hasCertifSchoolName;
    }


}
