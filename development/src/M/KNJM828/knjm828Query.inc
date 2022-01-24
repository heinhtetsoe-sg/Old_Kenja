<?php

require_once('for_php7.php');


class knjm828Query extends Query {


    //年度取得
    function getYear()
    {
        $query  = "  SELECT DISTINCT ";
        $query .= "      YEAR || '年度' AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= "  FROM ";
        $query .= "      SCHREG_REGD_HDAT ";
        $query .= "  ORDER BY  ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //学期マスタ
    function getSemeMst($year)
    {
        $query  = "  SELECT DISTINCT";
        $query .= "      SEMESTERNAME LABEL, ";
        $query .= "      SEMESTER AS VALUE    ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '{$year}' ";
        $query .= "  AND SEMESTER <> '9' ";

        return $query;
    }

    //CSV出力
    function getCsvQuery($model) {

        $query  = " WITH SUBCLASS AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "       T1.YEAR, T1.SEMESTER, T1.SCHREGNO, T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD ";
        $query .= "   FROM ";
        $query .= "    CHAIR_STD_DAT T1 ";
        $query .= "   INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "   INNER JOIN SUBCLASS_MST T4 ON T4.CLASSCD = T2.CLASSCD ";
        $query .= "       AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "       AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "       AND T4.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "   INNER JOIN RECORD_CHKFIN_DAT T5 ON T5.YEAR = T1.YEAR ";
        $query .= "       AND T5.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T5.CLASSCD = T2.CLASSCD ";
        $query .= "       AND T5.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "       AND T5.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "       AND T5.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "       AND T5.CHAIRCD = T2.CHAIRCD ";
        $query .= "   WHERE T1.YEAR = '{$model->field["YEAR"]}' AND T1.SEMESTER = '{$model->field["GAKKI"]}' ";
        $query .= " ) SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     FISCALYEAR(T3.ENT_DATE) AS ENT_YEAR, ";
        $query .= "     CASE WHEN T1.ATTENDNO IS NOT NULL THEN INT(T1.ATTENDNO) END AS ATTENDNO, ";
        $query .= "     T3.NAME, ";
        $query .= "     T2.SUBCLASSCD ";
        $query .= " FROM SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SUBCLASS T2 ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE T1.YEAR = '{$model->field["YEAR"]}' AND T1.SEMESTER = '{$model->field["GAKKI"]}' ";
        $query .= " ORDER BY T1.SCHREGNO ";
        return $query;
    }

}
?>

