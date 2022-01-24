package servletpack.KNJWG.detail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  学校情報取得ＳＱＬ
 *
 *  2005/10/22 yamashiro 東京都専用とし’/KNJWG/detail/’配下へ置く => ’/KNJZ/detail/’へは共通の同プログラムを置く
 *  2005/10/25 yamashiro メソッドString pre_sqlを変更 => SCHOOL_MSTの学校名、STAFF_MST,JOB_MSTの名称取得を追加
 */

public class KNJ_SchoolinfoSql{

    private static final Log log = LogFactory.getLog(KNJ_SchoolinfoSql.class);

    String t_switch;
    private boolean _hasCertifSchoolName;

    public KNJ_SchoolinfoSql(){
        this.t_switch = "00000";
    }

    public KNJ_SchoolinfoSql(String t_switch){
        StringBuffer stb = new StringBuffer(t_switch);
        stb.append("00000");
        this.t_switch = stb.toString();
    }

/*-----------------------------------------------------------------*
 *  学校情報
 *      ・クラス変数SWITCHにより、検索を制御
 *
 *      ・t_switch -->  //各種設定  初期値は0
 *              1=1:校長検索
 *              2=1:担当者をJOBCDで検索 2=2:担当者をSTAFFCDで検索
 *-----------------------------------------------------------------*/
    public String pre_sql(){

        StringBuffer sql = new StringBuffer();

        try{
            sql.append( "SELECT ");
            sql.append(     "T1.YEAR,T1.FOUNDEDYEAR,T1.CLASSIFICATION,T1.PRESENT_EST,");
            sql.append(     "T1.SCHOOLNAME1,T1.SCHOOLNAME2,T1.SCHOOLNAME3,");
            sql.append(     "T1.SYOSYO_NAME,T1.SYOSYO_NAME2,T1.CERTIF_NO,");
            sql.append(     "T1.SCHOOLZIPCD,T1.SCHOOLADDR1,T1.SCHOOLADDR2,");
            sql.append(     "T1.SCHOOLNAME_ENG,T1.SCHOOLADDR1_ENG,T1.SCHOOLADDR2_ENG,T1.SCHOOLTELNO,");
            sql.append(     "T1.SEMESTERDIV,T1.SCHOOLDIV ");
            sql.append(    ",T1.PRINCIPAL_NAME AS PRINCIPAL_NAME ");    //05/10/25 Move
            sql.append(    ",T1.JOB_NAME AS PRINCIPAL_JOBNAME ");       //05/10/25 Move
            sql.append(    ",T1.COMMONSCHOOLNAME1 ");                   //05/10/25 Build
            sql.append(    ",T1.REMARK1,T1.REMARK2,T1.REMARK3,T1.REMARK4,T1.REMARK5 ");  // 予備１〜予備３
          /* ***
            if( !t_switch.substring(0,1).equals("0") ){
                sql.append( ",T1.PRINCIPAL_NAME AS PRINCIPAL_NAME,");
                sql.append( "T2.STAFFNAME_ENG AS PRINCIPAL_NAME_ENG,");
                sql.append( "T1.JOB_NAME AS PRINCIPAL_JOBNAME ");
            }
          *** */
            if( t_switch.substring(0,1).equals("1") ){      //JOBCD(校長)で検索  05/10/25 Build
                sql.append( ",T2.STAFFNAME AS STAFF_PRINCIPANAME ");
                sql.append( ",T2.STAFFNAME_ENG AS PRINCIPAL_NAME_ENG ");
                sql.append( ",T2.JOBNAME AS STAFF_JOBNAME ");
            }
            if( !t_switch.substring(1,2).equals("0") ){
                sql.append( ",T3.STAFFNAME AS STAFF2_NAME ");
                sql.append( ",T3.STAFFNAME_ENG AS STAFF2_NAME_ENG ");
                sql.append( ",T3.JOBNAME AS STAFF2_JOBNAME ");
            }
            if( t_switch.substring(2,3).equals("1") ){      //任意の年度のSCHOO_MSTの表
                sql.append(",T4.CLASSIFICATION AS T4CLASSIFICATION ");     //県立、私立など
                sql.append(",T4.SCHOOLNAME1 AS T4SCHOOLNAME1, T4.SCHOOLNAME2 AS T4SCHOOLNAME2, T4.SCHOOLNAME3 AS T4SCHOOLNAME3 ");
                sql.append(",T4.SCHOOLNAME_ENG AS T4SCHOOLNAME_ENG ");
                sql.append(",T4.SEMESTERDIV AS T4SEMESTERDIV ");
                sql.append(",T4.SCHOOLDIV AS T4SCHOOLDIV ");               //0：学年制／1：単位制
            }

            sql.append( "FROM ");
                //  学校
            sql.append(     "(");
            sql.append(         "SELECT ");
            sql.append(             "'FRED' AS KEY,W1.YEAR,W1.FOUNDEDYEAR,W1.CLASSIFICATION,W1.PRESENT_EST,");
            sql.append(             "W2.SCHOOL_NAME AS SCHOOLNAME1,W1.SCHOOLNAME2,W1.SCHOOLNAME3,");
            sql.append(             "W1.SCHOOLZIPCD,W1.SCHOOLADDR1,W1.SCHOOLADDR2,");
            sql.append(             "W1.SCHOOLNAME_ENG,W1.SCHOOLADDR1_ENG,W1.SCHOOLADDR2_ENG,W1.SCHOOLTELNO,");
            sql.append(             "W1.SEMESTERDIV,W1.SCHOOLDIV, ");
            sql.append(             "W2.SYOSYO_NAME,W2.SYOSYO_NAME2,W2.CERTIF_NO,W2.JOB_NAME,W2.PRINCIPAL_NAME ");
            sql.append(            ",W1.SCHOOLNAME1 AS COMMONSCHOOLNAME1 ");    //05/10/25 BUILD
            sql.append(            ",W2.REMARK1,W2.REMARK2,W2.REMARK3,W2.REMARK4,W2.REMARK5 ");  // 予備１〜予備３
            sql.append(         "FROM ");
            sql.append(             "(SELECT * FROM SCHOOL_MST WHERE YEAR = ?) W1 ");
            sql.append(             "LEFT JOIN CERTIF_SCHOOL_DAT W2 ON W2.CERTIF_KINDCD = ? AND W1.YEAR = W2.YEAR");
            sql.append(     ")T1 ");
            if( t_switch.substring(0,1).equals("1") ){      //JOBCD(校長)で検索
                //  校長
                sql.append( "LEFT JOIN(");
                sql.append(     "SELECT ");
                sql.append(         "'FRED' AS KEY,STAFFNAME,STAFFNAME_ENG,JOBNAME ");
                sql.append(     "FROM ");
                sql.append(         "STAFF_YDAT W1 ");
                sql.append(         "INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
                sql.append(         "LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
                sql.append(     "WHERE ");
                sql.append(         "W1.YEAR=? AND W2.JOBCD='0001' ");
                sql.append( ")T2 ON T1.KEY = T2.KEY ");
            }
            if( t_switch.substring(1,2).equals("1") ){      //JOBCDで検索
                //  担当教諭
                sql.append( "LEFT JOIN(");
                sql.append(     "SELECT ");
                sql.append(         "'FRED' AS KEY,STAFFNAME,STAFFNAME_ENG,JOBNAME ");
                sql.append(     "FROM ");
                sql.append(         "STAFF_YDAT W1 ");
                sql.append(         "INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ");
                sql.append(         "LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
                sql.append(     "WHERE ");
                //sql.append(           "W1.YEAR =? AND W1.JOBCD =? ");
                sql.append(         "W1.YEAR =? AND W2.JOBCD =? ");    //05/10/24 東京都のDBに合わせて修正
                sql.append( ")T3 ON T1.KEY = T3.KEY ");
            }
            if( t_switch.substring(1,2).equals("2") ){      //STAFFCDで検索
                //  担当教諭
                sql.append( "LEFT JOIN(");
                sql.append(     "SELECT ");
                sql.append(         "'FRED' AS KEY,STAFFNAME,STAFFNAME_ENG,JOBNAME ");
                sql.append(     "FROM ");
                sql.append(         "STAFF_MST W2 ");
                sql.append(         "LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ");
                sql.append(     "WHERE ");
                sql.append(         "W2.STAFFCD =? ");
                sql.append( ")T3 ON T1.KEY = T3.KEY ");
            }
            if( t_switch.substring(2,3).equals("1") ){      //任意の年度のSCHOO_MSTの表
                sql.append( "LEFT JOIN(");
                sql.append(     "SELECT  'FRED' AS KEY ");
                sql.append(            ",CLASSIFICATION ");     //県立、私立など
                if (_hasCertifSchoolName) {
                    sql.append(        ",CASE WHEN W2.REMARK4 IS NOT NULL THEN W2.REMARK4 ELSE W1.SCHOOLNAME1 END AS SCHOOLNAME1");
                } else {
                    sql.append(        ",SCHOOLNAME1");
                }
                sql.append(            ",SCHOOLNAME2, SCHOOLNAME3 ");
                sql.append(            ",SCHOOLNAME_ENG ");
                sql.append(            ",SEMESTERDIV ");
                sql.append(            ",SCHOOLDIV ");          //0：学年制／1：単位制
                sql.append(     "FROM ");
                sql.append(         "(SELECT * FROM SCHOOL_MST WHERE YEAR = ?) W1 ");
                if (_hasCertifSchoolName) {
                    sql.append(      "LEFT JOIN CERTIF_SCHOOL_DAT W2 ON W2.CERTIF_KINDCD = ? AND W1.YEAR = W2.YEAR");
                }
                sql.append( ")T4 ON T1.KEY = T4.KEY ");
            }
        } catch( Exception ex ){
            log.error("[KNJ_SchoolinfoSql]pre_sql error!" + ex );
        }
//log.debug(sql);
        return sql.toString();
    }

    /**
     * @param hasCertifSchoolName 設定する hasCertifSchoolName。
     */
    public void setHasCertifSchoolName(boolean hasCertifSchoolName) {
        _hasCertifSchoolName = hasCertifSchoolName;
    }


}
