<?php

require_once('for_php7.php');

class knjj144eQuery extends Query {

    function deleteMarathonEventRankDat($db) {
        $query  = "   DELETE FROM MARATHON_EVENT_RANK_DAT ";
        $query .= "   WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEQ  = '00' ";

        $db->query($query);
    }

    function insertMarathonEventRankDat($db) {
        $query  = "   INSERT INTO MARATHON_EVENT_RANK_DAT ";
        $query .= "   SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     '00' AS SEQ, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.SEX, ";
        $query .= "     T1.TIME_H, ";
        $query .= "     T1.TIME_M, ";
        $query .= "     T1.TIME_S, ";
        $query .= "     CAST(NULL AS VARCHAR(2)) AS ATTEND_CD, ";
        $query .= "     RANK() OVER(PARTITION BY T2.GRADE  ORDER BY T1.TIME_H, T1.TIME_M, T1.TIME_S) AS GRADE_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T2.GRADE, T3.SEX ORDER BY T1.TIME_H, T1.TIME_M, T1.TIME_S) AS GRADE_RANK_SEX, ";
        $query .= "     RANK() OVER(ORDER BY T1.TIME_H, T1.TIME_M, T1.TIME_S) AS SCHOOL_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T3.SEX ORDER BY T1.TIME_H, T1.TIME_M, T1.TIME_S) AS SCHOOL_RANK_SEX, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= "   FROM ";
        $query .= "     MARATHON_EVENT_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ";
        $query .= "       ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.ATTEND_CD IS NULL ";
        $query .= "     AND T1.TIME_H IS NOT NULL ";
        $query .= "     AND T1.TIME_M IS NOT NULL ";
        $query .= "     AND T1.TIME_S IS NOT NULL ";
        $query .= "   UNION ";
        $query .= "   SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     '00' AS SEQ, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.SEX, ";
        $query .= "     CAST(NULL AS SMALLINT) AS TIME_H, ";
        $query .= "     CAST(NULL AS SMALLINT) AS TIME_M, ";
        $query .= "     CAST(NULL AS SMALLINT) AS TIME_S, ";
        $query .= "     T1.ATTEND_CD, ";
        $query .= "     CAST(NULL AS SMALLINT) AS GRADE_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS GRADE_RANK_SEX, ";
        $query .= "     CAST(NULL AS SMALLINT) AS SCHOOL_RANK, ";
        $query .= "     CAST(NULL AS SMALLINT) AS SCHOOL_RANK_SEX, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= "   FROM ";
        $query .= "     MARATHON_EVENT_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ";
        $query .= "       ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.ATTEND_CD IS NOT NULL ";

        $db->query($query);
    }

}
?>
