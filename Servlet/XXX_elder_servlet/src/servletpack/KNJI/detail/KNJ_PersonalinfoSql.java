// kanji=漢字
/*
 * $Id: 837b7ca7e899d538eb4bb4eb2016066a715128d8 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJI.detail;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KNJ_PersonalinfoSql{

/*
 *   ＜ＫＮＪＺ＿ＰｅｒｓｏｎａｌｉｎｆｏＳｑｌ＞生徒個人情報の取得のＳＱＬ
 *
 *  2004/03/16 yamashiro・卒業生の住所をGRD_BASE_MSTから出力とする
 */

/*---------------------------------------------------------------------------------------------*
 *  生徒個人情報（日本語）:学籍データを基本とする
 *      ・クラス変数SWITCHにより照会項目を制御
 *
 *      ・t_switch -->  //各種設定  初期値は0
 *              1=1:卒業情報有り
 *              2=1:入学情報有り 
 *              3=1:課程・学科・コース情報有り
 *              4=1:住所情報有り
 *              5=1:卒業中学情報有り
 *              6=1:保護者情報有り
 *              7=1:任意の学期指定   7=0:該当年度のMAX学期
 *              8=1:英語名称有り
 *---------------------------------------------------------------------------------------------*/
    
    private static Log log = LogFactory.getLog(KNJ_PersonalinfoSql.class);
    
    public KNJ_PersonalinfoSql() {
        log.info("$Revision: 63245 $ $Date: 2018-11-07 13:26:47 +0900 (水, 07 11 2018) $");
    }
    
    public String sql_info_reg(String t_switch){

        if( t_switch.length()<8 ){
            StringBuffer stbx = new StringBuffer(t_switch);
            stbx.append("000000");
            t_switch = stbx.toString();
        }

        final StringBuffer sql = new StringBuffer();

        final String switch0 = t_switch.substring(0,1);
        final String switch1 = t_switch.substring(1,2);
        final String switch2 = t_switch.substring(2,3);
        final String switch3 = t_switch.substring(3,4);
        final String switch4 = t_switch.substring(4,5);
        final String switch5 = t_switch.substring(5,6);
        final String switch6 = t_switch.substring(6,7);
        final String switch7 = t_switch.substring(7,8);
        final String switch8 = (t_switch.length() < 9) ? "0" : t_switch.substring(8,9);
        final String switch9 = (t_switch.length() < 10) ? "0" : t_switch.substring(9,10);
        final String switch10 = (t_switch.length() < 11) ? "0" : t_switch.substring(10,11);
        final String sMajorname2 = (t_switch.length() < 12) ? "0" : t_switch.substring(11, 12);
        try{
            sql.append("SELECT ");
            sql.append("     BASE.NAME,");
            if( switch7.equals("1") ) {
                sql.append("     BASE.NAME_ENG,");
            }
            if( switch8.equals("1") ) {
                sql.append("     BASE.REAL_NAME,");
                sql.append("     BASE.REAL_NAME_KANA,");
            }
            sql.append("     BASE.NAME_KANA, ");
            sql.append("     BASE.BIRTHDAY,  ");
            sql.append("     BASE.SEX AS SEX_FLG, ");
            sql.append("     T7.ABBV1 AS SEX,");
            sql.append("     T13.BIRTHDAY_FLG, ");
            if( switch7.equals("1") ) {
                sql.append(" T7.ABBV2 AS SEX_ENG,");
                sql.append(" T7.ABBV3 AS SEX_ENG2,");
            }
            sql.append("     T1.GRADE, ");
            sql.append("     T1.ATTENDNO, ");
            sql.append("     T1.ANNUAL, ");
            sql.append("     T6.HR_NAME,");
            //課程・学科・コース
            if( !switch2.equals("0") ){
                sql.append(" T3.COURSECD, ");
                sql.append(" T3.COURSENAME, ");
                sql.append(" T4.MAJORCD, ");
                if (!sMajorname2.equals("0")) {
                    sql.append(" VALUE(T4.MAJORNAME2, T4.MAJORNAME) AS MAJORNAME, ");
                } else {
                    sql.append(" T4.MAJORNAME, ");
                }
                sql.append(" T5.COURSECODE, ");
                sql.append(" T5.COURSECODENAME, ");
                sql.append(" T3.COURSEABBV, ");
                sql.append(" T4.MAJORABBV,");
                if( switch7.equals("1") ) {
                    sql.append(" T3.COURSEENG, ");
                    sql.append(" T4.MAJORENG,");
                }
            }
            //卒業
            if( switch0.equals("1") ){
                sql.append(" EGHIST.GRD_DIV, ");
                sql.append(" EGHIST.GRD_DATE, ");
                sql.append(" CASE WHEN EGHIST.GRD_DATE IS NULL THEN ");
                sql.append("     CASE WHEN INT(T1.ANNUAL) < 3 THEN NULL ");
                sql.append("         ELSE RTRIM(CHAR(INT(T1.YEAR) + 1)) || '-' || RTRIM(CHAR(MONTH(T10.GRADUATE_DATE))) || '-01' END ");
                sql.append("             ELSE VARCHAR(EGHIST.GRD_DATE) END AS GRADU_DATE,");
                sql.append(" CASE WHEN EGHIST.GRD_DATE IS NULL THEN '卒業見込み' ELSE ");
                sql.append("     (SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A003' AND EGHIST.GRD_DIV = ST2.NAMECD2) END AS GRADU_NAME,");
                sql.append(" CASE WHEN EGHIST.GRD_DATE IS NOT NULL THEN (SELECT DISTINCT MAX(ANNUAL) FROM SCHREG_REGD_DAT ST1 ");
                sql.append("   WHERE ST1.YEAR = FISCALYEAR(EGHIST.GRD_DATE) AND ST1.SCHREGNO = T1.SCHREGNO) ");
                sql.append(" END AS GRADU_GRADE,");
            }
            //入学
            if( switch1.equals("1") ){
                sql.append(" EGHIST.ENT_DATE, ");
                sql.append(" EGHIST.ENT_DIV,");
                sql.append("(SELECT DISTINCT ANNUAL FROM GRD_REGD_DAT ST1 ");
                sql.append("  WHERE ST1.YEAR = FISCALYEAR(EGHIST.ENT_DATE) AND ST1.SCHREGNO = T1.SCHREGNO) ");
                sql.append("   AS ENTER_GRADE,");
                sql.append(" (SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A002' AND EGHIST.ENT_DIV = ST2.NAMECD2) AS ENTER_NAME,");
                
                sql.append(" (SELECT MIN(TBL1.ANNUAL) FROM GRD_REGD_DAT TBL1 WHERE TBL1.SCHREGNO=T1.SCHREGNO AND TBL1.ANNUAL = '04') AS ENTER_GRADE2,");
                sql.append(" (SELECT MIN(TBL2.YEAR)   FROM GRD_REGD_DAT TBL2 WHERE TBL2.SCHREGNO=T1.SCHREGNO AND TBL2.ANNUAL = '04') || '-04-01' AS ENT_DATE2,");
            }
            //住所 04/03/16変更
            if( !switch3.equals("0") ) {
                sql.append(" VALUE(BASE.CUR_ADDR1,'') || VALUE(BASE.CUR_ADDR2,'') AS ADDR,");
                sql.append(" BASE.CUR_ADDR1 AS ADDR1, ");
                sql.append(" BASE.CUR_ADDR2 AS ADDR2, ");
                sql.append(" BASE.CUR_TELNO AS TELNO, ");
                sql.append(" BASE.CUR_ZIPCD AS ZIPCD,");
                sql.append(" BASE.CUR_ADDR_FLG AS ADDR_FLG,");
                if( switch7.equals("1") ) {
                    sql.append(" VALUE(BASE.CUR_ADDR1_ENG,'') || VALUE(BASE.CUR_ADDR2_ENG,'') AS ADDR_ENG,");
                    sql.append(" BASE.CUR_ADDR1_ENG AS ADDR1_ENG, ");
                    sql.append(" BASE.CUR_ADDR2_ENG AS ADDR2_ENG,");
                }
            }
            //卒業中学情報
            if( switch4.equals("1") ) {
                sql.append(" EGHIST.FINISH_DATE,");
                sql.append(" (SELECT FINSCHOOL_NAME FROM FINSCHOOL_MST ST1 WHERE ST1.FINSCHOOLCD = EGHIST.FINSCHOOLCD) AS J_NAME,");
            }
            //保護者情報
            if( switch5.equals("1") ) {
                sql.append(" T12.GUARD_NAME,T12.GUARD_KANA,");
                sql.append(" VALUE(T12.GUARD_ADDR1,'') || VALUE(T12.GUARD_ADDR2,'') AS GUARD_ADDR,");
                sql.append(" T12.GUARD_ADDR1, ");
                sql.append(" T12.GUARD_ADDR2, ");
                sql.append(" T12.GUARD_ZIPCD,");
            }
            if ( switch8.equals("1") ) {
                sql.append(" (CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
                sql.append(" T11.NAME_OUTPUT_FLG, ");
            }
            if (switch9.equals("1")) {
                sql.append(" CASE WHEN T15.NAMECD1 IS NULL THEN 0 ELSE 1 END AS IN_DORMITORY, ");
                sql.append(" CASE WHEN T15.NAMECD1 IS NULL THEN '' ELSE T15.NAME2 END AS DORMITORY_NAME, ");
            }
            if (switch10.equals("1")) {
                sql.append(" T17.GRADE_CD, ");
            }
            sql.append(" BASE.NATIONALITY, ");
            sql.append(" NMA024.NAME1 AS NATIONALITY_NAME, ");
            sql.append(" NMA024.NAME2 AS NATIONALITY_NAME_ENG, ");
            sql.append(" REGDG.SCHOOL_KIND, ");
            sql.append(" T1.SCHREGNO ");
            sql.append(" FROM ");
            //学籍情報(??? or ????)
            sql.append(     "(   SELECT     * ");
            sql.append(         "FROM       GRD_REGD_DAT T1 ");
            sql.append(         "WHERE      T1.SCHREGNO=? AND T1.YEAR=? ");
            if(switch6.equals("1")) {                            //学期を特定
                sql.append(                 "AND T1.SEMESTER=? ");
            } else {                                                               //最終学期
                sql.append(                 "AND T1.SEMESTER=(SELECT MAX(SEMESTER) FROM GRD_REGD_DAT WHERE SCHREGNO = ? AND YEAR = ?)");
            }
            sql.append(     ") T1 ");
            sql.append(     "INNER JOIN GRD_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ");
            sql.append(                                 "AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ");
            sql.append(     "LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR AND REGDG.GRADE = T1.GRADE ");
            sql.append(     "LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = T1.SCHREGNO AND EGHIST.SCHOOL_KIND = REGDG.SCHOOL_KIND ");
            //卒業情報有りの場合
            //05/10/05 if( t_switch.substring(0,1).equals("1") )
            //05/10/05  sql.append( "INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ");
            //基礎情報
            sql.append(     "INNER JOIN GRD_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            sql.append(     "LEFT JOIN NAME_MST T7 ON NAMECD1='Z002' AND NAMECD2=BASE.SEX ");
            //課程、学科、コース
            if( !switch2.equals("0") ){
                sql.append( "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
                sql.append( "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
                sql.append( "LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ");
                sql.append(                         "AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')");
            }
            //生徒住所(??)
            if( !switch3.equals("0") ){
                sql.append( "LEFT JOIN GRD_ADDRESS_DAT AS T8 ");
                sql.append( "INNER JOIN(");
                sql.append(     "SELECT     MAX(ISSUEDATE) AS ISSUEDATE ");
                sql.append(     "FROM       GRD_ADDRESS_DAT ");
                sql.append(     "WHERE      SCHREGNO=? AND FISCALYEAR(ISSUEDATE) <=? ");
                sql.append( ")T9 ON T9.ISSUEDATE = T8.ISSUEDATE ON T8.SCHREGNO = T1.SCHREGNO ");
            }
            if ( switch8.equals("1") ) {
                sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = BASE.SCHREGNO AND T11.DIV = '01' ");
            }
            //保護者情報
            if( switch5.equals("1") ) {
                sql.append( "LEFT JOIN GRD_GUARDIAN_DAT T12 ON T12.SCHREGNO = BASE.SCHREGNO ");
            }

            //卒業情報有りの場合 05/10/05Modify
            if( switch0.equals("1") ) {
                sql.append( "LEFT JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ");
            }
            sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T13 ON T13.SCHREGNO = BASE.SCHREGNO AND T13.BIRTHDAY_FLG = '1' ");

            // 寮情報
            if (switch9.equals("1")) {
                sql.append("LEFT JOIN SCHREG_ENVIR_DAT T14 ON T14.SCHREGNO = BASE.SCHREGNO ");
                sql.append("LEFT JOIN NAME_MST T15 ON T15.NAMECD1 = 'H108' AND T15.NAMECD2 = T14.RESIDENTCD AND T15.NAMESPARE1 = '4' ");
            }
            if (switch10.equals("1")) {
                sql.append("LEFT JOIN NAME_MST T16 ON T16.NAMECD1 = 'A023' AND T1.GRADE BETWEEN T16.NAME2 AND T16.NAME3 ");
                sql.append("LEFT JOIN SCHREG_REGD_GDAT T17 ON T17.YEAR = T1.YEAR AND T17.GRADE = T1.GRADE AND T17.SCHOOL_KIND = T16.NAME1 ");
            }
            sql.append("LEFT JOIN NAME_MST NMA024 ON 'A024' = NMA024.NAMECD1 AND BASE.NATIONALITY = NMA024.NAMECD2 ");
        } catch( Exception ex ){
            System.out.println("[KNJ_PersonalinfoSql]sql_info_reg read error!");
            System.out.println( ex );
        }
//System.out.println("[KNJ_PersonalinfoSql]sql="+sql);

        return sql.toString();

    }//public sql_info_regの括り



}//クラスの括り
