<?php

require_once('for_php7.php');


class knjm443wQuery extends Query {

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

    //テスト種別
    function getTestcd($year, $semester)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS VALUE ";
        $query .= "   , T2.TESTITEMNAME AS LABEL ";
        $query .= " FROM NAME_MST T1 ";
        $query .= " INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$semester."' ";
        $query .= "     AND T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV = T1.NAMESPARE1 ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'M002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //科目取得
    function getSubclassList($model) {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T2.SUBCLASSCD      = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                          AND T2.CLASSCD         = T3.CLASSCD ";
            $query .= "                          AND T2.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                          AND T2.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        if ($model->field["SEMESTER"] == "2") {
            $query .= "     (T1.SEMESTER = '".$model->field["SEMESTER"]."' OR ";
            $query .= "      T1.SEMESTER = '1') AND ";
        } else {
            $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        }
        $query .= "     T1.CHAIRCD = T2.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    

}
?>

