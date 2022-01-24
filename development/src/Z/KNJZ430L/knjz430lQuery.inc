<?php

require_once('for_php7.php');

class knjz430lQuery extends Query {
    //データ状況取得
    function getkekka($year, $year_add, $table) {
        $query  = " WITH NEXTYEAR_TBL AS ( ";
        $query .= "     SELECT ";
        $query .= "         'tbljoin' AS JOINCD, ";
        $query .= "         CASE WHEN COUNT(*) > 0 THEN '次年度データあり' ELSE '' END AS KEKKA ";
        $query .= "     FROM ";
        $query .= "         {$table} ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".$year_add."' ";
        $query .= " ), YEAR_TBL AS ( ";
        $query .= "     SELECT ";
        $query .= "         'tbljoin' AS JOINCD, ";
        $query .= "         CASE WHEN COUNT(*) = 0 THEN '今年度データなし' ELSE '' END AS KEKKA ";
        $query .= "     FROM ";
        $query .= "         {$table} ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".$year."' ";
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

    //テーブル存在チェック
    function cnt_table($table_name) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME = '{$table_name}' ";

        return $query;
    }

    //フィールド取得
    function getFieldQuery($table, $year) {
        $query  = " SELECT * FROM {$table} WHERE YEAR = '".$year."' FETCH FIRST 1 ROWS ONLY ";
        return $query;
    }

    //更新
    function getInsertQuery($model, $year, $year_add, $table, $field) {
        $query  = " INSERT INTO {$table} ";
        $query .= " SELECT ";
        $query .= "     $field ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }
}
?>
