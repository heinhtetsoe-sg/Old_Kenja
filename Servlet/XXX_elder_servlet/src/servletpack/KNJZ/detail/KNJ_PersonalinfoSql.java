// kanji=漢字
/*
 * $Id: 6230b3f22adbac816ef53194c3232ce5a24c7887 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  生徒個人情報の取得のＳＱＬ
 *
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
 */
//  2004/03/19 yamashiro・卒業見込年月日の出力仕様を変更(現行は2年以下はブランク)
//                          1年生は3年後の卒業日付けの月を出力する。
//                          2学年は2年後の卒業日付けの月を出力する。
//                          3学年以上は、今年度の卒業日付けの月を出力する。
//  2004/09/22 yamashiro・入学(転入学)の学年は入学日ENTER_DATEより算出した年度の年次とする
//

public class KNJ_PersonalinfoSql {
    
    private static Log log = LogFactory.getLog(KNJ_PersonalinfoSql.class);
    
    public KNJ_PersonalinfoSql() {
        log.info("$Revision: 63246 $ $Date: 2018-11-07 13:27:31 +0900 (水, 07 11 2018) $");
    }

    public String sql_info_reg(String t_switch) {
        return sql_info_reg(t_switch, new HashMap());
    }

    public String sql_info_reg(String t_switch, Map paramMap) {

        if (t_switch.length() < 8) {
            final StringBuffer stbx = new StringBuffer(t_switch);
            stbx.append("000000");
            t_switch = stbx.toString();
        }
        paramMap = null == paramMap ? new HashMap() : paramMap;
        final boolean isGrd = "1".equals(paramMap.get("PRINT_GRD"));
        final String schoolMstSchoolKind = (String) paramMap.get("SCHOOL_MST_SCHOOL_KIND");
        log.info(" isGrd = " + isGrd + ", SchoolMstSchoolKind = " + schoolMstSchoolKind);

        final StringBuffer sql = new StringBuffer();

        final String q = "?";
        final String sGraduate  = t_switch.substring(0, 1);
        final String sEnter     = t_switch.substring(1, 2);
        final String sCourse    = t_switch.substring(2, 3);
        final String sAddress   = t_switch.substring(3, 4);
        final String sFinschool = t_switch.substring(4, 5);
        final String sGuardian  = t_switch.substring(5, 6);
        final String sSemes     = t_switch.substring(6, 7);
        final String sEnglish   = t_switch.substring(7, 8);
        final String sRealName  = (t_switch.length() < 9) ? "0" : t_switch.substring(8, 9);
        final String sDormitory = (t_switch.length() < 10) ? "0" : t_switch.substring(9, 10);
        //final String sGradeCd   = (t_switch.length() < 11) ? "0" : t_switch.substring(10, 11);
        final String sMajorname2 = (t_switch.length() < 12) ? "0" : t_switch.substring(11, 12);
        final String sSchoolKind = (t_switch.length() < 13) ? "0" : t_switch.substring(12, 13);
        final String sRegdDat   = (t_switch.length() < 14) ? "0" : t_switch.substring(13, 14);
        final boolean useCoursecodeAbbv = t_switch.length() >= 15 && "1".equals(t_switch.substring(14, 15));
        
        sql.append("SELECT ");
        sql.append("     BASE.NAME,");
        if( sEnglish.equals("1") ) {
            sql.append("     BASE.NAME_ENG,");
        }
        if( sRealName.equals("1") ) {
            sql.append("     BASE.REAL_NAME,");
            sql.append("     BASE.REAL_NAME_KANA,");
        }
        
        sql.append(" BASE.NAME_KANA,");
        sql.append(" BASE.BIRTHDAY, ");
        sql.append(" BASE.SEX AS SEX_FLG, ");
        sql.append(" NMZ002.ABBV1 AS SEX,");
        sql.append(" KGLED.BIRTHDAY_FLG, ");
        if (sEnglish.equals("1")) {
            sql.append(" NMZ002.ABBV2 AS SEX_ENG,");
            sql.append(" NMZ002.ABBV3 AS SEX_ENG2,");
        }
        sql.append("     T1.GRADE, ");
        sql.append("     T1.ATTENDNO, ");
        sql.append("     REGDH.HR_NAME,");
        if (isGrd) {
            sql.append("     T1.ANNUAL, ");
        } else {
            if ("0".equals(sRegdDat)) {
                sql.append(" T1.ANNUAL, ");
            } else if ("1".equals(sRegdDat)) {
                sql.append(" '01' AS ANNUAL, ");
            }
        }
        //課程・学科・コース
        if (!sCourse.equals("0")) {
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
            if (useCoursecodeAbbv) {
                sql.append(" T5.COURSECODEABBV1, ");
                sql.append(" T5.COURSECODEABBV2, ");
                sql.append(" T5.COURSECODEABBV3, ");
            }
            sql.append(" T3.COURSEABBV, ");
            sql.append(" T4.MAJORABBV,");
            if (sEnglish.equals("1")) {
                sql.append(" T3.COURSEENG, ");
                sql.append(" T4.MAJORENG,");
            }
        }
        //卒業
        if (sGraduate.equals("1")) {
            sql.append(" EGHIST.GRD_DIV, ");
            sql.append(" EGHIST.GRD_DATE, ");
            if (isGrd || !isGrd && "0".equals(sRegdDat)) {
            	sql.append(" CASE WHEN EGHIST.GRD_DATE IS NULL THEN ");
            	if (isGrd) {
                    sql.append("     CASE WHEN INT(T1.ANNUAL) < 3 THEN NULL ");
                    sql.append("       ELSE ");
                    sql.append("         RTRIM(CHAR(INT(T1.YEAR) + 1)) ");
                    sql.append("               || '-' || RTRIM(CHAR(MONTH(T10.GRADUATE_DATE))) || '-01' ");
                    sql.append("     END ");
            	} else {
                    sql.append("         RTRIM(CHAR(INT(T1.YEAR) + ");
                    sql.append("                           CASE WHEN NMA023.NAMESPARE2 IS NOT NULL THEN ");
                    sql.append("                                   INT(NMA023.NAMESPARE2) - INT(T1.ANNUAL) + 1 ");
                    sql.append("                                ELSE (CASE T1.ANNUAL WHEN '01' THEN 3 ");
                    sql.append("                                                     WHEN '02' THEN 2 ");
                    sql.append("                                                     ELSE 1 ");
                    sql.append("                                      END)");
                    sql.append("                           END ");
                    sql.append("                   )) ");
                    sql.append("               || '-' || RTRIM(CHAR(MONTH(T10.GRADUATE_DATE))) || '-'  || RTRIM(CHAR(DAY(T10.GRADUATE_DATE))) ");
            	}
            	sql.append(" ELSE VARCHAR(EGHIST.GRD_DATE) END AS GRADU_DATE,");
            	sql.append(" CASE WHEN EGHIST.GRD_DATE IS NULL THEN '卒業見込み' ");
            	sql.append("  ELSE (SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A003' AND EGHIST.GRD_DIV = ST2.NAMECD2) END ");
            	sql.append("  AS GRADU_NAME,");
            } else if ("1".equals(sRegdDat)) {
                sql.append(" CAST(NULL AS DATE) AS GRADU_DATE,");
                sql.append(" CAST(NULL AS VARCHAR(1)) AS GRADU_NAME,");
            }
            sql.append(" CASE WHEN EGHIST.GRD_DATE IS NOT NULL THEN (SELECT DISTINCT MAX(ANNUAL) ");
            sql.append("   FROM SCHREG_REGD_DAT ST1 ");
            sql.append("   WHERE ST1.YEAR = FISCALYEAR(EGHIST.GRD_DATE) ");
            sql.append("     AND ST1.SCHREGNO = T1.SCHREGNO ");
            sql.append(" ) END AS GRADU_GRADE,");
            sql.append(" CASE WHEN EGHIST.GRD_DATE IS NOT NULL THEN (SELECT DISTINCT MAX(ST3.GRADE_CD) ");
            sql.append("   FROM SCHREG_REGD_DAT ST1 ");
            sql.append("   INNER JOIN SCHREG_REGD_GDAT ST3 ON ST3.YEAR = ST1.YEAR AND ST3.GRADE = ST1.GRADE ");
            sql.append("   WHERE ST1.YEAR = FISCALYEAR(EGHIST.GRD_DATE) ");
            sql.append("     AND ST1.SCHREGNO = T1.SCHREGNO ");
            sql.append(" ) END AS GRADU_GRADE_CD,");
        }
        //入学
        if (sEnter.equals("1")) {
            sql.append(" EGHIST.ENT_DATE, ");
            sql.append(" EGHIST.ENT_DIV,");
            if (isGrd || !isGrd && "0".equals(sRegdDat)) {
            	sql.append(" (SELECT DISTINCT ANNUAL ");
            	sql.append("   FROM SCHREG_REGD_DAT ST1 ");
            	sql.append("  WHERE ST1.YEAR = FISCALYEAR(EGHIST.ENT_DATE) AND ST1.SCHREGNO = T1.SCHREGNO ");
            	sql.append("  ) AS ENTER_GRADE,");
            	sql.append(" (SELECT MIN(ST3.GRADE_CD) ");
            	sql.append("   FROM SCHREG_REGD_DAT ST1 ");
            	sql.append("   INNER JOIN SCHREG_REGD_GDAT ST3 ON ST3.YEAR = ST1.YEAR AND ST3.GRADE = ST1.GRADE ");
            	sql.append("  WHERE ST1.YEAR = FISCALYEAR(EGHIST.ENT_DATE) AND ST1.SCHREGNO = T1.SCHREGNO ");
            	sql.append("  ) AS ENTER_GRADE_CD,");
            	sql.append(" (SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A002' AND EGHIST.ENT_DIV = ST2.NAMECD2) AS ENTER_NAME,");
            	if (isGrd) {
            		sql.append(" (SELECT MIN(TBL1.ANNUAL) FROM GRD_REGD_DAT    TBL1 WHERE TBL1.SCHREGNO = T1.SCHREGNO AND TBL1.ANNUAL = '04') AS ENTER_GRADE2,");
            		sql.append(" (SELECT MIN(TBL2.YEAR)   FROM GRD_REGD_DAT    TBL2 WHERE TBL2.SCHREGNO = T1.SCHREGNO AND TBL2.ANNUAL = '04') || '-04-01' AS ENT_DATE2,");
            	} else {
                    sql.append(" (SELECT MIN(TBL1.ANNUAL) FROM SCHREG_REGD_DAT TBL1 WHERE TBL1.SCHREGNO = T1.SCHREGNO AND TBL1.ANNUAL = '04') AS ENTER_GRADE2,");
                    sql.append(" (SELECT MIN(TBL2.YEAR)   FROM SCHREG_REGD_DAT TBL2 WHERE TBL2.SCHREGNO = T1.SCHREGNO AND TBL2.ANNUAL = '04') || '-04-01' AS ENT_DATE2,");
            	}
            } else if ("1".equals(sRegdDat)) {
                sql.append(" '01' AS ENTER_GRADE,");
                sql.append(" '01' AS ENTER_GRADE_CD,");
                sql.append(" '入学' AS ENTER_NAME,");
                sql.append(" '01' AS ENTER_GRADE2,");
                sql.append(" CAST(NULL AS DATE) AS ENT_DATE2,");
            }
        }
        if (!sAddress.equals("0")) {
            if (isGrd) {
                //住所
                if (!sAddress.equals("0")) {
                    sql.append(" VALUE(BASE.CUR_ADDR1,'') || VALUE(BASE.CUR_ADDR2,'') AS ADDR,");
                    sql.append(" BASE.CUR_ADDR1 AS ADDR1, ");
                    sql.append(" BASE.CUR_ADDR2 AS ADDR2, ");
                    sql.append(" BASE.CUR_TELNO AS TELNO, ");
                    sql.append(" BASE.CUR_ZIPCD AS ZIPCD,");
                    sql.append(" BASE.CUR_ADDR_FLG AS ADDR_FLG,");
                    if( sEnglish.equals("1") ) {
                        sql.append(" VALUE(BASE.CUR_ADDR1_ENG,'') || VALUE(BASE.CUR_ADDR2_ENG,'') AS ADDR_ENG,");
                        sql.append(" BASE.CUR_ADDR1_ENG AS ADDR1_ENG, ");
                        sql.append(" BASE.CUR_ADDR2_ENG AS ADDR2_ENG,");
                    }
                }
            } else {
                if ("2".equals(sAddress)) {
                    sql.append(" CASE WHEN SEND_A.SEND_ADDR1 IS NOT NULL THEN VALUE(SEND_A.SEND_ADDR1,'') || VALUE(SEND_A.SEND_ADDR2,'') ");
                    sql.append("   ELSE VALUE(SCHADDR.ADDR1,'') || VALUE(SCHADDR.ADDR2,'') END AS ADDR,");
                    sql.append(" CASE WHEN SEND_A.SEND_ADDR1 IS NOT NULL THEN SEND_A.SEND_ADDR1 ELSE SCHADDR.ADDR1 END AS ADDR1, ");
                    sql.append(" CASE WHEN SEND_A.SEND_ADDR1 IS NOT NULL THEN SEND_A.SEND_ADDR2 ELSE SCHADDR.ADDR2 END AS ADDR2, ");
                    sql.append(" CASE WHEN SEND_A.SEND_ADDR1 IS NOT NULL THEN SEND_A.SEND_TELNO ELSE SCHADDR.TELNO END AS TELNO, ");
                    sql.append(" CASE WHEN SEND_A.SEND_ADDR1 IS NOT NULL THEN SEND_A.SEND_ZIPCD ELSE SCHADDR.ZIPCD END AS ZIPCD,");
                    sql.append(" CASE WHEN SEND_A.SEND_ADDR1 IS NOT NULL THEN SEND_A.SEND_ADDR_FLG ELSE SCHADDR.ADDR_FLG END AS ADDR_FLG,");
                } else {
                    sql.append(" VALUE(SCHADDR.ADDR1,'') || VALUE(SCHADDR.ADDR2,'') AS ADDR,");
                    sql.append(" SCHADDR.ADDR1, ");
                    sql.append(" SCHADDR.ADDR2, ");
                    sql.append(" SCHADDR.TELNO, ");
                    sql.append(" SCHADDR.ZIPCD,");
                    sql.append(" SCHADDR.ADDR_FLG,");
                }
                if (sEnglish.equals("1")) {
                    sql.append(" VALUE(SCHADDR.ADDR1_ENG,'') || VALUE(SCHADDR.ADDR2_ENG,'') AS ADDR_ENG,");
                    sql.append(" SCHADDR.ADDR1_ENG, ");
                    sql.append(" SCHADDR.ADDR2_ENG,");
                }
            }
        }
        // 卒業中学情報
        if (sFinschool.equals("1")) {
            sql.append(" EGHIST.FINISH_DATE,");
            sql.append(" FIN_S.FINSCHOOL_NAME AS J_NAME,");
            sql.append(" NML001.NAME1 AS INSTALLATION_DIV,");
        }
        if (isGrd) {
        } else {
            sql.append(" BASEHIST.NAME AS NAME_HIST_FIRST, ");
            sql.append(" BASEHIST_R.NAME AS NAME_WITH_RN_HIST_FIRST, ");
            if (sRealName.equals("1")) {
                sql.append(" BASEHIST_R.REAL_NAME AS REAL_NAME_HIST_FIRST, ");
            }
        }
        //保護者情報
        if( sGuardian.equals("1") ) {
            sql.append(" T12.GUARD_NAME,T12.GUARD_KANA,");
            if (isGrd) {
            } else {
                sql.append(" T12.GUARD_REAL_NAME, ");
                sql.append(" GRDNHIST.GUARD_NAME AS GUARD_NAME_HIST_FIRST, ");
                sql.append(" T12.GUARD_REAL_KANA, ");
                sql.append(" GRDNHIST_R.GUARD_REAL_NAME AS G_R_NAME_WITH_RN_HIST_FIRST, ");
                sql.append(" GRDNHIST_R.GUARD_NAME      AS G_NAME_WITH_RN_HIST_FIRST, ");
            }
            sql.append(" VALUE(T12.GUARD_ADDR1,'') || VALUE(T12.GUARD_ADDR2,'') AS GUARD_ADDR,");
            sql.append(" T12.GUARD_ADDR1, ");
            sql.append(" T12.GUARD_ADDR2, ");
            sql.append(" T12.GUARD_ZIPCD,");
        }
        if (sRealName.equals("1")) {
            sql.append(" (CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            sql.append(" T11.NAME_OUTPUT_FLG, ");
        }
        if (sDormitory.equals("1")) {
            sql.append(" CASE WHEN NMH108.NAMECD1 IS NULL THEN 0 ELSE 1 END AS IN_DORMITORY, ");
            sql.append(" CASE WHEN NMH108.NAMECD1 IS NULL THEN '' ELSE NMH108.NAME2 END AS DORMITORY_NAME, ");
        }
        if (isGrd || !isGrd && "0".equals(sRegdDat)) {
            sql.append(" BASE.NATIONALITY, ");
            sql.append(" NMA024.NAME1 AS NATIONALITY_NAME, ");
            sql.append(" NMA024.NAME2 AS NATIONALITY_NAME_ENG, ");
        } else {
            sql.append(" CAST(NULL AS VARCHAR(1)) AS NATIONALITY, ");
            sql.append(" CAST(NULL AS VARCHAR(1)) AS NATIONALITY_NAME, ");
            sql.append(" CAST(NULL AS VARCHAR(1)) AS NATIONALITY_NAME_ENG, ");
        }
        sql.append(" REGDG.GRADE_CD, ");
        sql.append(" REGDG.SCHOOL_KIND, ");
        sql.append(" T1.SCHREGNO ");
        sql.append(" FROM ");
        //学籍情報(??? or ????)
        sql.append("( ");
        sql.append(     "SELECT     T1.* ");
        if (isGrd) {
            sql.append("    FROM       GRD_REGD_DAT T1 ");
        } else {
            if ("0".equals(sRegdDat)) {
                sql.append("    FROM       SCHREG_REGD_DAT T1 ");
            } else if ("1".equals(sRegdDat)){
                sql.append("    FROM       CLASS_FORMATION_DAT T1 ");
                sql.append("    INNER JOIN FRESHMAN_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.ENTERYEAR = T1.YEAR ");
            }
        }
        sql.append(" WHERE      T1.SCHREGNO = " + q + " ");
        sql.append("        AND T1.YEAR = " + q + " ");
        if(sSemes.equals("1")) {
            //学期を特定
            sql.append(            "AND T1.SEMESTER=" + q + " ");
        } else {
            //最終学期
            if (isGrd) {
                sql.append("        AND T1.SEMESTER = (SELECT MAX(SEMESTER) FROM GRD_REGD_DAT WHERE SCHREGNO = " + q + " AND YEAR = " + q + ")");
            } else {
                if ("0".equals(sRegdDat)) {
                    sql.append("    AND T1.SEMESTER = (SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT WHERE SCHREGNO = " + q + " AND YEAR = " + q + ")");
                } else if ("1".equals(sRegdDat)){
                    sql.append("    AND T1.SEMESTER = (SELECT MAX(SEMESTER) FROM CLASS_FORMATION_DAT WHERE SCHREGNO = " + q + " AND YEAR = " + q + ")");
                }
            }
        }
        sql.append(") T1 ");
        if (isGrd) {
            sql.append("      INNER JOIN GRD_REGD_HDAT   REGDH ON REGDH.YEAR = T1.YEAR AND REGDH.SEMESTER = T1.SEMESTER AND REGDH.GRADE = T1.GRADE AND REGDH.HR_CLASS = T1.HR_CLASS ");
        } else {
            sql.append("      LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = T1.YEAR AND REGDH.SEMESTER = T1.SEMESTER AND REGDH.GRADE = T1.GRADE AND REGDH.HR_CLASS = T1.HR_CLASS ");
        }
        sql.append(     "LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR AND REGDG.GRADE = T1.GRADE ");
        if (isGrd) {
            sql.append(     "LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = T1.SCHREGNO AND EGHIST.SCHOOL_KIND = REGDG.SCHOOL_KIND ");
            //卒業情報有りの場合
            if (sGraduate.equals("1")) {
                sql.append( "LEFT JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ");
                if (!StringUtils.isBlank(schoolMstSchoolKind)) {
                    sql.append(" AND T10.SCHOOL_KIND = '" + schoolMstSchoolKind + "' ");
                }
            }
            //基礎情報
            sql.append(     "INNER JOIN GRD_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
        } else {
            if ("0".equals(sSchoolKind)) {
                sql.append("LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = T1.SCHREGNO AND EGHIST.SCHOOL_KIND = REGDG.SCHOOL_KIND ");
            } else {
                sql.append("LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = T1.SCHREGNO AND EGHIST.SCHOOL_KIND = '" + sSchoolKind + "' ");
            }
            // 卒業情報有りの場合
            if (sGraduate.equals("1")) {
                sql.append("INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ");
                if (!StringUtils.isBlank(schoolMstSchoolKind)) {
                    sql.append(" AND T10.SCHOOL_KIND = '" + schoolMstSchoolKind + "' ");
                }
            }
            // 基礎情報
            if ("0".equals(sRegdDat)) {
                sql.append("INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            } else if ("1".equals(sRegdDat)) {
                sql.append("INNER JOIN (SELECT ");
                sql.append("      F1.* ");
                sql.append("      , CAST(NULL AS VARCHAR(1)) AS NAME_ENG ");
                sql.append("      , CAST(NULL AS VARCHAR(1)) AS REAL_NAME ");
                sql.append("      , CAST(NULL AS VARCHAR(1)) AS REAL_NAME_KANA ");
                sql.append("      , CAST(NULL AS DATE) AS FINISH_DATE ");
                sql.append("     FROM FRESHMAN_DAT F1) BASE ON BASE.SCHREGNO = T1.SCHREGNO AND BASE.ENTERYEAR = T1.YEAR ");
            }
        }
        sql.append("LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = BASE.SEX ");
        sql.append("LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = EGHIST.FINSCHOOLCD ");
        sql.append("LEFT JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' AND NML001.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
        // 課程、学科、コース
        if (!sCourse.equals("0")) {
            sql.append("LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            sql.append("LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            sql.append("LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')");
        }
        //生徒住所(??)
        if (!sAddress.equals("0")) {
            if (isGrd) {
                sql.append( "LEFT JOIN GRD_ADDRESS_DAT AS SCHADDR ");
                sql.append( "INNER JOIN(");
                sql.append(     "SELECT     MAX(ISSUEDATE) AS ISSUEDATE ");
                sql.append(     "FROM       GRD_ADDRESS_DAT ");
                sql.append(     "WHERE      SCHREGNO=" + q + " AND FISCALYEAR(ISSUEDATE) <=" + q + " ");
                sql.append( ")T9 ON T9.ISSUEDATE = SCHADDR.ISSUEDATE ON SCHADDR.SCHREGNO = T1.SCHREGNO ");
            } else {
                if ("0".equals(sRegdDat)) {
                    sql.append("LEFT JOIN SCHREG_ADDRESS_DAT AS SCHADDR ");
                    sql.append("INNER JOIN(");
                    sql.append("SELECT     MAX(ISSUEDATE) AS ISSUEDATE ");
                    sql.append("FROM       SCHREG_ADDRESS_DAT ");
                    sql.append("WHERE      SCHREGNO=" + q + " AND FISCALYEAR(ISSUEDATE) <=" + q + " ");
                    sql.append(")T9 ON T9.ISSUEDATE = SCHADDR.ISSUEDATE ON SCHADDR.SCHREGNO = T1.SCHREGNO ");
                } else if ("1".equals(sRegdDat)) {
                    sql.append("LEFT JOIN (SELECT ");
                    sql.append("      F1.* ");
                    sql.append("      , CAST(NULL AS VARCHAR(1)) AS ADDR1_ENG ");
                    sql.append("      , CAST(NULL AS VARCHAR(1)) AS ADDR2_ENG ");
                    sql.append("     FROM FRESHMAN_DAT F1 ");
                    sql.append("WHERE      SCHREGNO=" + q + " AND FISCALYEAR(ISSUEDATE) <=" + q + " ");
                    sql.append(") SCHADDR ON T1.SCHREGNO = SCHADDR.SCHREGNO ");
                }
                if (sAddress.equals("2")) {
                    sql.append("LEFT JOIN SCHREG_SEND_ADDRESS_DAT AS SEND_A ON SEND_A.SCHREGNO = T1.SCHREGNO AND SEND_A.DIV = '1' ");
                }
            }
        }
        if ( sRealName.equals("1") ) {
            sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = BASE.SCHREGNO AND T11.DIV = '01' ");
        }
        //保護者情報
        if (sGuardian.equals("1") ) {
            if (isGrd) {
                sql.append( "LEFT JOIN GRD_GUARDIAN_DAT T12 ON T12.SCHREGNO = BASE.SCHREGNO ");
            } else {
                sql.append(" LEFT JOIN GUARDIAN_DAT T12 ON T12.SCHREGNO = BASE.SCHREGNO ");
            }
        }

        if (isGrd) {
        } else {
            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) BASEHISTMIN ON BASEHISTMIN.SCHREGNO = BASE.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT BASEHIST ON BASEHIST.SCHREGNO = BASEHISTMIN.SCHREGNO AND BASEHIST.ISSUEDATE = BASEHISTMIN.ISSUEDATE ");
            
            // 保護者履歴情報
            if (sGuardian.equals("1")) {
                sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_HIST_DAT WHERE GUARD_NAME_FLG = '1' ");
                sql.append("   GROUP BY SCHREGNO) GRDNHISTMIN ON GRDNHISTMIN.SCHREGNO = BASE.SCHREGNO ");
                sql.append("LEFT JOIN GUARDIAN_HIST_DAT GRDNHIST ON GRDNHIST.SCHREGNO = GRDNHISTMIN.SCHREGNO AND GRDNHIST.ISSUEDATE = GRDNHISTMIN.ISSUEDATE ");
            }
            // 生徒名履歴情報
            sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM SCHREG_BASE_HIST_DAT WHERE REAL_NAME_FLG = '1' ");
            sql.append("   GROUP BY SCHREGNO) BASEHISTMIN_R ON BASEHISTMIN_R.SCHREGNO = BASE.SCHREGNO ");
            sql.append("LEFT JOIN SCHREG_BASE_HIST_DAT BASEHIST_R ON BASEHIST_R.SCHREGNO = BASEHISTMIN_R.SCHREGNO AND BASEHIST_R.ISSUEDATE = BASEHISTMIN_R.ISSUEDATE ");
            
            // 保護者履歴情報
            if (sGuardian.equals("1")) {
                sql.append("LEFT JOIN (SELECT SCHREGNO, MIN(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_HIST_DAT WHERE GUARD_REAL_NAME_FLG = '1' ");
                sql.append("   GROUP BY SCHREGNO) GRDNHISTMIN_R ON GRDNHISTMIN_R.SCHREGNO = BASE.SCHREGNO ");
                sql.append("LEFT JOIN GUARDIAN_HIST_DAT GRDNHIST_R ON GRDNHIST_R.SCHREGNO = GRDNHISTMIN_R.SCHREGNO AND GRDNHIST_R.ISSUEDATE = GRDNHISTMIN_R.ISSUEDATE ");
            }
        }
        sql.append("LEFT JOIN NAME_MST NMA023 ON NMA023.NAMECD1 = 'A023' AND NMA023.NAME1 = REGDG.SCHOOL_KIND ");
        sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT KGLED ON KGLED.SCHREGNO = BASE.SCHREGNO AND KGLED.BIRTHDAY_FLG = '1' ");
        // 寮情報
        if (sDormitory.equals("1")) {
            sql.append("LEFT JOIN SCHREG_ENVIR_DAT ENVIR ON ENVIR.SCHREGNO = BASE.SCHREGNO ");
            sql.append("LEFT JOIN NAME_MST NMH108 ON NMH108.NAMECD1 = 'H108' AND NMH108.NAMECD2 = ENVIR.RESIDENTCD AND NMH108.NAMESPARE1 = '4' ");
        }
        if (isGrd || !isGrd && "0".equals(sRegdDat)) {
            sql.append("LEFT JOIN NAME_MST NMA024 ON 'A024' = NMA024.NAMECD1 AND BASE.NATIONALITY = NMA024.NAMECD2 ");
        }
        
        return sql.toString();
    }

    public String studentInfoSql(final boolean getAddr) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     L2.APPLICANTNO, ");
        stb.append("     L2.NAME, ");
        stb.append("     L2.NAME_KANA, ");
        stb.append("     T1.ANNUAL, ");
        stb.append("     L2.BIRTHDAY, ");
        stb.append("     N4.NAME2 AS SEX, ");
        stb.append("     T1.STUDENT_DIV, ");
        stb.append("     L2.ENT_DATE, ");
        stb.append("     L2.ENT_DIV, ");
        stb.append("     VALUE(N1.NAME1, '') AS ENT_NAME, ");
        stb.append("     L2.GRD_DATE, ");
        stb.append("     L2.GRD_DIV, ");
        stb.append("     VALUE(N2.NAME1, '') AS GRD_NAME, ");
        stb.append("     L2.GRD_SCHEDULE_DATE, ");
        stb.append("     VALUE(N3.NAME1, '') AS CURRICULUM_NAME, ");
        if (getAddr) {
            stb.append("     VALUE(SCH_ADDR.ZIPCD, '') AS ZIPCD, ");
            stb.append("     VALUE(PREF.PREF_NAME, '') AS PREF_NAME, ");
            stb.append("     VALUE(SCH_ADDR.ADDR1, '') AS ADDR1, ");
            stb.append("     VALUE(SCH_ADDR.ADDR2, '') AS ADDR2, ");
            stb.append("     VALUE(SCH_ADDR.ADDR3, '') AS ADDR3, ");
        }
        stb.append("     L6.SCHOOLNAME1, ");
        stb.append("     L1.HR_NAME, ");
        stb.append("     L1.HR_NAMEABBV, ");
        stb.append("     L3.COURSENAME, ");
        stb.append("     L4.MAJORNAME, ");
        stb.append("     L5.COURSECODENAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
        stb.append("          AND T1.GRADE = L1.GRADE ");
        stb.append("          AND T1.HR_CLASS = L1.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L2 ON T1.SCHREGNO = L2.SCHREGNO ");
        stb.append("     LEFT JOIN COURSE_MST L3 ON T1.COURSECD = L3.COURSECD ");
        stb.append("     LEFT JOIN MAJOR_MST L4 ON T1.COURSECD = L4.COURSECD ");
        stb.append("          AND T1.MAJORCD = L4.MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST L5 ON T1.COURSECODE = L5.COURSECODE ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'A002' ");
        stb.append("          AND L2.ENT_DIV = N1.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'A003' ");
        stb.append("          AND L2.GRD_DIV = N2.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'W002' ");
        stb.append("          AND L2.CURRICULUM_YEAR BETWEEN N3.NAMESPARE1 AND N3.NAMESPARE2 ");
        stb.append("     LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'Z002' ");
        stb.append("          AND L2.SEX = N4.NAMECD2 ");
        stb.append("     LEFT JOIN BELONGING_MST L6 ON T1.GRADE = L6.BELONGING_DIV ");
        // 生徒住所
        if (getAddr) {
            stb.append("     LEFT JOIN SCHREG_ADDRESS_DAT AS SCH_ADDR ");
            stb.append("     INNER JOIN(SELECT ");
            stb.append("                    MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("                FROM ");
            stb.append("                    SCHREG_ADDRESS_DAT ");
            stb.append("                WHERE ");
            stb.append("                    SCHREGNO = ? ");
            stb.append("                    AND FISCALYEAR(ISSUEDATE) <= ? ");
            stb.append("               ) SCH_ADDR2 ON SCH_ADDR2.ISSUEDATE = SCH_ADDR.ISSUEDATE ON SCH_ADDR.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN PREF_MST AS PREF ON SCH_ADDR.PREF_CD = PREF.PREF_CD ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = ? ");
        stb.append("     AND T1.YEAR = ? ");
        stb.append("     AND T1.SEMESTER = ? ");

        return stb.toString();
    }
}
