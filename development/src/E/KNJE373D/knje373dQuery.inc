<?php

require_once('for_php7.php');

class knje373dQuery extends Query
{
    //CSV出力データ
    public function getCsvQuery()
    {
        $query  = " WITH GET_ADDR AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "         , T1.ADDR1 ";
        $query .= "         , T1.ADDR2 ";
        $query .= "         , T1.ZIPCD ";
        $query .= "         , T1.TELNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT T1 ";
        $query .= "         INNER JOIN ( ";
        $query .= "             SELECT ";
        $query .= "                 MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "                 , SCHREGNO ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_ADDRESS_DAT ";
        $query .= "             GROUP BY ";
        $query .= "                 SCHREGNO ";
        $query .= "         ) T2 ";
        $query .= "             ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND T2.ISSUEDATE = T1.ISSUEDATE ";
        $query .= " ), NEW_ZIPCD_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         NEW_ZIPCD, ";
        $query .= "         PREF ";
        $query .= "     FROM ";
        $query .= "         ZIPCD_MST ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     '' AS KOKYAKU_NO, ";
        $query .= "     '' AS NYUSHI_CD, ";
        $query .= "     '' AS JYUKEN_NO, ";
        $query .= "     CASE ";
        $query .= "         WHEN AFT.HOPE_CONFIRM_FLG1 = '1' THEN SUBSTR(AFT.HOPE_CD1,1,2) ";
        $query .= "         WHEN AFT.HOPE_CONFIRM_FLG2 = '1' THEN SUBSTR(AFT.HOPE_CD2,1,2) ";
        $query .= "         WHEN AFT.HOPE_CONFIRM_FLG3 = '1' THEN SUBSTR(AFT.HOPE_CD3,1,2) ";
        $query .= "         ELSE '' ";
        $query .= "     END AS NYUSHI_GAKUBU, ";
        $query .= "     CASE ";
        $query .= "         WHEN AFT.HOPE_CONFIRM_FLG1 = '1' THEN SUBSTR(AFT.HOPE_CD1,3,2) ";
        $query .= "         WHEN AFT.HOPE_CONFIRM_FLG2 = '1' THEN SUBSTR(AFT.HOPE_CD2,3,2) ";
        $query .= "         WHEN AFT.HOPE_CONFIRM_FLG3 = '1' THEN SUBSTR(AFT.HOPE_CD3,3,2) ";
        $query .= "         ELSE '' ";
        $query .= "     END AS NYUSHI_GAKKA1, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     to_char(BASE.BIRTHDAY, 'YYYY/MM/DD') AS BIRTHDAY, ";
        $query .= "     BASE.SEX, ";
        $query .= "     '0' AS GAIKOKUSEKI_DIV, ";
        $query .= "     '28528F' AS FINSCHOOLCD, ";
        $query .= "     to_char(BASE.GRD_DATE, 'YYYY/MM/DD') AS GRD_DATE, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     PREFM.PREF_CD AS PREF_CD, ";
        $query .= "     '' AS SHITYOSON, ";
        $query .= "     '' AS CHIMEI, ";
        $query .= "     '' AS TATEMONO, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     GUAR.GUARD_NAME, ";
        $query .= "     GUAR.GUARD_KANA, ";
        $query .= "     right('00' || BASE.NATIONALITY, 2) AS NATIONALITY, ";
        $query .= "     AFT.SCHREGNO, ";
        $query .= "     ADDR.ADDR1, ";
        $query .= "     ADDR.ADDR2 ";
        $query .= " FROM ";
        $query .= "     AFT_RECOMMENDATION_INFO_DAT AFT ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "              ON REGD.YEAR      = AFT.YEAR ";
        $query .= "             AND REGD.SCHREGNO  = AFT.SCHREGNO ";
        $query .= "             AND REGD.SEMESTER  = (SELECT ";
        $query .= "                                       MAX(SEMESTER) ";
        $query .= "                                   FROM ";
        $query .= "                                       SCHREG_REGD_DAT REGD2 ";
        $query .= "                                   WHERE ";
        $query .= "                                           REGD2.YEAR     = REGD.YEAR ";
        $query .= "                                       AND REGD2.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                  ) ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "             ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN GET_ADDR ADDR ";
        $query .= "             ON ADDR.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN GUARDIAN_DAT GUAR ";
        $query .= "             ON GUAR.SCHREGNO = REGD.SCHREGNO ";

        $query .= "     LEFT JOIN NEW_ZIPCD_T ZIPM ";
        $query .= "             ON ZIPM.NEW_ZIPCD = ADDR.ZIPCD ";
        $query .= "     LEFT JOIN PREF_MST PREFM ";
        $query .= "             ON PREFM.PREF_NAME = ZIPM.PREF ";

        $query .= " WHERE ";
        $query .= "         AFT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND VALUE(AFT.DONT_RECOMMENDATION_FLG,'') != '1' "; //非推薦フラグ
        $query .= " ORDER BY ";
        $query .= "     AFT.SCHREGNO ";

        return $query;
    }
}
