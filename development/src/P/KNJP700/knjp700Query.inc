<?php

require_once('for_php7.php');

class knjp700Query extends Query {

    //対応者を取得
    function getkekka($year, $year_add, $table)
    {
        $query  = " WITH NEXTYEAR_TBL AS ( ";
        $query .= " SELECT ";
        $query .= "     'tbljoin' AS JOINCD, ";
        $query .= "     CASE WHEN COUNT(*) > 0 THEN '次年度データあり' ELSE '' END AS KEKKA ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year_add."' ";
        $query .= " ), YEAR_TBL AS (SELECT ";
        $query .= "     'tbljoin' AS JOINCD, ";
        $query .= "     CASE WHEN COUNT(*) = 0 THEN '今年度データなし' ELSE '' END AS KEKKA ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CASE WHEN T1.KEKKA = '' AND T2.KEKKA = '' ";
        $query .= "          THEN '' ";
        $query .= "          ELSE CASE WHEN T1.KEKKA != '' ";
        $query .= "                    THEN T1.KEKKA ";
        $query .= "                    ELSE T2.KEKKA END ";
        $query .= "     END AS KEKKA ";
        $query .= " FROM ";
        $query .= "     YEAR_TBL T1 ";
        $query .= "     LEFT JOIN NEXTYEAR_TBL T2 ON T1.JOINCD = T2.JOINCD ";

        return $query;
    }

    function getFieldQuery($table, $year)
    {
        $query  = " SELECT * FROM $table WHERE YEAR = '".$year."' ";
        return $query;
    }

    function getInsertQuery($year, $year_add, $table, $field, $setField) {
        if ($table == "REDUCTION_MST" || $table == "REDUCTION_COUNTRY_MST") {
            $query  = " INSERT INTO {$table} ";
            $query .= "     ( ";
            $query .= "      {$setField} ";
            $query .= "     ) ";
            $query .= " (SELECT ";
            $query .= "      {$field} ";
            $query .= "  FROM ";
            $query .= "      {$table} ";
            $query .= "  WHERE ";
            $query .= "     YEAR = '{$year}' ";
            $query .= "  ) ";
        } else {
            $query  = " INSERT INTO $table ";
            $query .= " SELECT ";
            $query .= "     $field ";
            $query .= " FROM ";
            $query .= "     $table ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".$year."' ";
        }

        return $query;
    }

    function cnt_table($table_name) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME = '{$table_name}' ";

        return $query;
    }

    function getCount($year_add, $table) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year_add}' ";

        return $query;
    }
}
?>
