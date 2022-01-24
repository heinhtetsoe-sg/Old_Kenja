<?php

require_once('for_php7.php');

class knjd661Query extends Query {
    //テスト種別取得
    function getDataDiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'H308' AND ";
        $query .= "     NAMECD2 = '4' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称取得
    function getMockName($model) {
        $query  = " WITH MOCK_AND_AVG AS ( ";
        $query .= " SELECT ";
        $query .= "     MOCKCD, ";
        $query .= "     MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     SUBSTR(T1.MOCKCD,1,1) || '99999999' AS MOCKCD, ";
        $query .= "     L1.NAME1 || '年間平均' AS MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "     MOCK_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H308' AND L1.NAMECD2 = SUBSTR(T1.MOCKCD,1,1) ";
        $query .= " GROUP BY ";
        $query .= "     SUBSTR(T1.MOCKCD,1,1), ";
        $query .= "     L1.NAME1 ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     MOCKCD AS VALUE, ";
        $query .= "     MOCKCD || ':' || MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_AND_AVG ";
        $query .= " WHERE ";
        $query .= "     SUBSTR(MOCKCD,1,1) = '{$model->field["DATA_DIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
