<?php

require_once("for_php7.php");

class knjm836wQuery extends Query
{


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
    function getTestcd($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS VALUE ";
        $query .= "   , T2.TESTITEMNAME AS LABEL ";
        $query .= " FROM NAME_MST T1 ";
        $query .= " INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$model->field["GAKKI"]."' ";
        $query .= "     AND T2.SEMESTER || '-' || T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV = T1.NAMESPARE1 ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'M002' ";
        if ($model->Properties["knjm836wPrintGakkiSeiseki"] == "1") {
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     '990008' AS VALUE ";
            $query .= "   , '学期成績' AS LABEL ";
            $query .= " FROM SYSIBM.SYSDUMMY1 T1 ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }
}
