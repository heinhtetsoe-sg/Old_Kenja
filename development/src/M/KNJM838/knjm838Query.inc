<?php

require_once('for_php7.php');


class knjm838Query extends Query {


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

    //前期成績会議日付
    function getNameMstM019Date($year)
    {
        $query  = "  SELECT ";
        $query .= "      NAME1 ";
        $query .= "  FROM ";
        $query .= "      NAME_MST ";
        $query .= "  WHERE  ";
        $query .= "      NAMECD1 = 'M019' ";
        $query .= "      AND NAMECD2 = '".$year."' ";

        return $query;
    }

}
?>

