<?php

require_once('for_php7.php');

class knjf073Query extends Query {

    //CVS作成用のQUERY
    function selectCsvQuery() {

        $query .= " WITH REGD AS ( ";
        $query .= "   SELECT T1.SCHREGNO, T1.YEAR, T1.GRADE ";
        $query .= "   FROM SCHREG_REGD_DAT T1 ";
        $query .= "   WHERE T1.YEAR = '".CTRL_YEAR."' AND  T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ), GRADES AS ( ";
        $query .= "   SELECT T1.GRADE, T1.GRADE_NAME1, COUNT(T2.SCHREGNO) AS COUNT ";
        $query .= "   FROM SCHREG_REGD_GDAT T1 ";
        $query .= "   INNER JOIN REGD T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
        $query .= "   GROUP BY T1.GRADE, T1.GRADE_NAME1 ";
        $query .= " ), MEDEXAM_TOOTH AS ( ";
        $query .= "   SELECT T2.GRADE, T1.* ";
        $query .= "   FROM V_MEDEXAM_TOOTH_DAT T1 ";
        $query .= "   INNER JOIN MEDEXAM_HDAT HD ON HD.SCHREGNO = T1.SCHREGNO AND HD.YEAR = T1.YEAR AND HD.TOOTH_DATE IS NOT NULL ";
        $query .= "   INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR ";
        $query .= " ), STAT AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     COUNT(*) AS TOTAL, ";
        $query .= "     SUM(CASE WHEN VALUE(REMAINBABYTOOTH, 0) + VALUE(REMAINADULTTOOTH, 0)  + VALUE(REMAINBABYTOOTH, 0) + VALUE(REMAINADULTTOOTH, 0) > 0 THEN 1 ELSE 0 END) AS COUNT_STD_MUSHIBA, ";
        $query .= "     SUM(VALUE(REMAINBABYTOOTH, 0) + VALUE(REMAINADULTTOOTH, 0)) AS COUNT_MI, ";
        $query .= "     SUM(VALUE(TREATEDBABYTOOTH, 0) + VALUE(TREATEDADULTTOOTH, 0)) AS COUNT_ZUMI, ";
        $query .= "     SUM(VALUE(LOSTADULTTOOTH, 0)) AS COUNT_LOST, ";
        $query .= "     SUM(CASE WHEN VALUE(BRACK_ADULTTOOTH, 0) > 0 THEN 1 ELSE 0 END) AS COUNT_STD_CO, ";
        $query .= "     SUM(CASE WHEN DENTISTREMARK_GO IS NOT NULL THEN 1 ELSE 0 END) AS COUNT_STD_GO, ";
        $query .= "     SUM(CASE WHEN DENTISTREMARK_G IS NOT NULL THEN 1 ELSE 0 END) AS COUNT_STD_G ";
        $query .= "   FROM MEDEXAM_TOOTH T1 ";
        $query .= "   GROUP BY T1.GRADE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.GRADE_NAME1, ";
        $query .= "   VALUE(T1.COUNT, 0) AS SEITO_SU, ";
        $query .= "   VALUE(COUNT_STD_MUSHIBA, 0) AS COUNT_STD_MUSHIBA, ";
        $query .= "   VALUE(COUNT_MI, 0) AS COUNT_MI, ";
        $query .= "   VALUE(COUNT_ZUMI, 0) AS COUNT_ZUMI, ";
        $query .= "   VALUE(COUNT_LOST, 0) AS COUNT_LOST, ";
        $query .= "   VALUE(COUNT_STD_CO, 0) AS COUNT_STD_CO, ";
        $query .= "   VALUE(COUNT_STD_GO, 0) AS COUNT_STD_GO, ";
        $query .= "   VALUE(COUNT_STD_G, 0) AS COUNT_STD_G ";
        $query .= " FROM GRADES T1 ";
        $query .= " LEFT JOIN STAT T2 ON T2.GRADE = T1.GRADE ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "   '99' AS GRADE, ";
        $query .= "   '合計' AS GRADE_NAME1, ";
        $query .= "   SUM(VALUE(T1.COUNT, 0)) AS SEITO_SU, ";
        $query .= "   SUM(VALUE(COUNT_STD_MUSHIBA, 0)) AS COUNT_STD_MUSHIBA, ";
        $query .= "   SUM(VALUE(COUNT_MI, 0)) AS COUNT_MI, ";
        $query .= "   SUM(VALUE(COUNT_ZUMI, 0)) AS COUNT_ZUMI, ";
        $query .= "   SUM(VALUE(COUNT_LOST, 0)) AS COUNT_LOST, ";
        $query .= "   SUM(VALUE(COUNT_STD_CO, 0)) AS COUNT_STD_CO, ";
        $query .= "   SUM(VALUE(COUNT_STD_GO, 0)) AS COUNT_STD_GO, ";
        $query .= "   SUM(VALUE(COUNT_STD_G, 0)) AS COUNT_STD_G ";
        $query .= " FROM GRADES T1 ";
        $query .= " LEFT JOIN STAT T2 ON T2.GRADE = T1.GRADE ";
        $query .= " ORDER BY GRADE ";

        return $query;
    }
}
?>
