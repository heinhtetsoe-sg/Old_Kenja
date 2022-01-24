<?php

require_once('for_php7.php');

class knjz431Query extends Query {

    //教育委員会チェック
    function checkEdboard() {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //データ結果取得
    function getkekka($year, $year_add, $table) {
        $query  = " WITH NEXTYEAR_TBL AS ( ";
        $query .= "     SELECT ";
        $query .= "         'tbljoin' AS JOINCD, ";
        $query .= "         CASE WHEN COUNT(*) > 0 THEN '次年度データあり' ELSE '' END AS KEKKA ";
        $query .= "     FROM ";
        $query .= "         $table ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".$year_add."' ";
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

    //フィールド取得
    function getFieldQuery($table, $year) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //更新
    function getInsertQuery($year, $year_add, $table, $field) {
        $query  = " INSERT INTO $table ";
        $query .= " SELECT ";
        $query .= "     $field ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //学期マスタ更新
    function getInsertSemesterMstQuery($year, $semester, $db) {
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $semester;
        $data["SEMESTERNAME"][TEXT] = "　";
        $data["SDATE"][TEXT]        = $year."-04-01";
        $data["EDATE"][TEXT]        = ($year+1)."-03-31";
        $data["REGISTERCD"][TEXT]   = STAFFCD ;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "SEMESTER_MST");
        $db->query($query);        

        return;
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

    //次年度
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
