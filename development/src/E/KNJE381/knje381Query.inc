<?php

require_once('for_php7.php');

class knje381Query extends Query {
    //学校種別
    function getSchoolCategoryCd() {
        $query  = " SELECT ";
        $query .= "     SCHOOL_CATEGORY_CD AS VALUE, ";
        $query .= "     CATEGORY_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_SCHOOL_CATEGORY_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //東京都中分類
    function getTokyoMcd() {
        $query  = " SELECT ";
        $query .= "     SCHOOL_CATEGORY_CD, ";
        $query .= "     SCHOOL_CATEGORY_CD || '-' || TOKYO_L_CD || '-' || TOKYO_M_CD AS VALUE, ";
        $query .= "     TOKYO_M_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLEGE_FACULTY_TOKYO_M_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSV出力データ
    function getCsvQuery() {
        $query  = " WITH ADDRESS_DAT AS ( ";// 住所情報
        $query .= "     SELECT ";
        $query .= "         A1.SCHREGNO, ";
        $query .= "         case when A1.AREACD = '01' then A1.AREACD else '02' end as AREACD ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT A1 ";
        $query .= "     INNER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "         FROM ";
        $query .= "             SCHREG_ADDRESS_DAT ";
        $query .= "         GROUP BY ";
        $query .= "             SCHREGNO ";
        $query .= "         ) A2 ON A2.SCHREGNO  = A1.SCHREGNO ";
        $query .= "             AND A2.ISSUEDATE = A1.ISSUEDATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     case when ADDR.AREACD = '01' then ADDR.AREACD else '02' end as AREACD, ";
        $query .= "     SYSM.SCHOOL_CATEGORY_CD, ";
        $query .= "     SYSM.TOKYO_L_CD, ";
        $query .= "     SYSM.TOKYO_M_CD, ";
        $query .= "     COUNT(*) AS M_CNT ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT AFTG ";
        $query .= "     INNER JOIN ( ";// SEQのmaxを取得（進路決定が複数ある場合に一つに絞る）
        $query .= "                 SELECT ";
        $query .= "                     A1.SCHREGNO, ";
        $query .= "                     MAX(A1.SEQ) AS SEQ ";
        $query .= "                 FROM ";
        $query .= "                     AFT_GRAD_COURSE_DAT A1 ";
        $query .= "                     INNER JOIN ( ";// 登録日のmaxを取得（進路決定が複数ある場合に一つに絞る）
        $query .= "                                 SELECT ";
        $query .= "                                     SCHREGNO, ";
        $query .= "                                     MAX(TOROKU_DATE) AS TOROKU_DATE ";
        $query .= "                                 FROM ";
        $query .= "                                     AFT_GRAD_COURSE_DAT ";
        $query .= "                                 WHERE ";
        $query .= "                                         YEAR         = '".CTRL_YEAR."' ";
        $query .= "                                     AND SENKOU_KIND  = '0' "; // 受験先種別【0:学校 1:会社 2:家事手伝い】
        $query .= "                                     AND DECISION     = '1' "; // 受験結果  【1:合格 etc..】(E005)
        $query .= "                                     AND PLANSTAT     = '1' "; // 進路状況  【1:決定 etc..】(E006)
        $query .= "                                 GROUP BY ";
        $query .= "                                     SCHREGNO ";
        $query .= "                                 ) A2 ON A2.SCHREGNO    = A1.SCHREGNO ";
        $query .= "                                     AND A2.TOROKU_DATE = A1.TOROKU_DATE ";
        $query .= "                 GROUP BY ";
        $query .= "                     A1.SCHREGNO ";
        $query .= "                 ) A3 ON A3.SCHREGNO = AFTG.SCHREGNO ";
        $query .= "                     AND A3.SEQ      = AFTG.SEQ ";
        $query .= "     INNER JOIN COLLEGE_FACULTY_SYSTEM_MST SYSM ON AFTG.STAT_CD   = SYSM.SCHOOL_CD ";
        $query .= "                                               AND AFTG.FACULTYCD = SYSM.FACULTYCD ";
        $query .= "     LEFT JOIN ADDRESS_DAT ADDR ON AFTG.SCHREGNO = ADDR.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "     ADDR.AREACD, ";
        $query .= "     SYSM.SCHOOL_CATEGORY_CD, ";
        $query .= "     SYSM.TOKYO_L_CD, ";
        $query .= "     SYSM.TOKYO_M_CD ";
        $query .= " UNION ";
        //合計
        $query .= " SELECT ";
        $query .= "     '99' AS AREACD, ";
        $query .= "     SYSM.SCHOOL_CATEGORY_CD, ";
        $query .= "     SYSM.TOKYO_L_CD, ";
        $query .= "     SYSM.TOKYO_M_CD, ";
        $query .= "     COUNT(*) AS M_CNT ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT AFTG ";
        $query .= "     INNER JOIN ( ";
        $query .= "                 SELECT ";
        $query .= "                     A1.SCHREGNO, ";
        $query .= "                     MAX(A1.SEQ) AS SEQ ";
        $query .= "                 FROM ";
        $query .= "                     AFT_GRAD_COURSE_DAT A1 ";
        $query .= "                     INNER JOIN ( ";
        $query .= "                                 SELECT ";
        $query .= "                                     SCHREGNO, ";
        $query .= "                                     MAX(TOROKU_DATE) AS TOROKU_DATE ";
        $query .= "                                 FROM ";
        $query .= "                                     AFT_GRAD_COURSE_DAT ";
        $query .= "                                 WHERE ";
        $query .= "                                         YEAR         = '".CTRL_YEAR."' ";
        $query .= "                                     AND SENKOU_KIND  = '0' "; // 受験先種別【0:学校 1:会社 2:家事手伝い】
        $query .= "                                     AND DECISION     = '1' "; // 受験結果  【1:合格 etc..】(E005)
        $query .= "                                     AND PLANSTAT     = '1' "; // 進路状況  【1:決定 etc..】(E006)
        $query .= "                                 GROUP BY ";
        $query .= "                                     SCHREGNO ";
        $query .= "                                 ) A2 ON A2.SCHREGNO    = A1.SCHREGNO ";
        $query .= "                                     AND A2.TOROKU_DATE = A1.TOROKU_DATE ";
        $query .= "                 GROUP BY ";
        $query .= "                     A1.SCHREGNO ";
        $query .= "                 ) A3 ON A3.SCHREGNO = AFTG.SCHREGNO ";
        $query .= "                     AND A3.SEQ      = AFTG.SEQ ";
        $query .= "     INNER JOIN COLLEGE_FACULTY_SYSTEM_MST SYSM ON AFTG.STAT_CD   = SYSM.SCHOOL_CD ";
        $query .= "                                               AND AFTG.FACULTYCD = SYSM.FACULTYCD ";
        $query .= " GROUP BY ";
        $query .= "     SYSM.SCHOOL_CATEGORY_CD, ";
        $query .= "     SYSM.TOKYO_L_CD, ";
        $query .= "     SYSM.TOKYO_M_CD ";
        $query .= " ORDER BY ";
        $query .= "     AREACD, ";
        $query .= "     SCHOOL_CATEGORY_CD, ";
        $query .= "     TOKYO_L_CD, ";
        $query .= "     TOKYO_M_CD ";

        return $query;
    }
}
?>
