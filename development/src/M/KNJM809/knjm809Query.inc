<?php

require_once('for_php7.php');

class knjm809Query extends Query {

    //年度取得
    function getYear($model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getSchno($model, $year, $semester)
    {
        $query .= "  SELECT ";
        $query .= "      T1.SCHREGNO AS VALUE, ";
        $query .= "      CASE WHEN T2.SCHREGNO IS NOT NULL AND T2.JUDGE_RESULT = '2' AND T2.DECISION_DATE IS NOT NULL THEN '承' ";
        $query .= "           WHEN T2.SCHREGNO IS NOT NULL AND T2.JUDGE_RESULT = '3' AND T2.DECISION_DATE IS NOT NULL THEN '不' ";
        $query .= "           ELSE '　' ";
        $query .= "      END || '　' || T1.SCHREGNO || '　' ||  T3.NAME_SHOW AS LABEL  ";
        $query .= "  FROM   ";
        $query .= "      SCHREG_REGD_DAT T1 ";
        $query .= "  INNER JOIN (SELECT SCHREGNO, YEAR, MAX(REGISTER_DATE) AS REGISTER_DATE ";
        $query .= "              FROM SCHREG_TEXTBOOK_FREE_APPLY_DAT ";
        $query .= "              GROUP BY SCHREGNO, YEAR ) TT1 ON TT1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                              AND TT1.YEAR = T1.YEAR ";
        $query .= "  INNER JOIN SCHREG_TEXTBOOK_FREE_APPLY_DAT T2 ON T2.SCHREGNO = TT1.SCHREGNO ";
        $query .= "                                              AND T2.YEAR = TT1.YEAR ";
        $query .= "                                              AND T2.REGISTER_DATE = TT1.REGISTER_DATE ";
        $query .= "  INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "  WHERE  ";
        $query .= "      T1.YEAR = '" .$year ."' ";
        $query .= "  AND T1.SEMESTER   = '".$semester."' ";
        if ($model->field["OUTPUT"] == "1") {
            $query .= "  AND T2.JUDGE_RESULT = '2' ";
            $query .= "  AND T2.DECISION_DATE IS NOT NULL ";
        } else if ($model->field["OUTPUT"] == "2") {
            $query .= "  AND T2.JUDGE_RESULT = '3' ";
            $query .= "  AND T2.DECISION_DATE IS NOT NULL ";
        } else if ($model->field["OUTPUT"] == "3") {
            $query .= "  AND (T2.JUDGE_RESULT = '2' OR T2.JUDGE_RESULT = '3') ";
            $query .= "  AND T2.DECISION_DATE IS NOT NULL ";
        }

        return $query;
    }
}
?>
