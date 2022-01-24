<?php

require_once('for_php7.php');

class knjwfin_searchQuery extends Query {
    //校種コンボ
    function getFinschoolType() {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.FINSCHOOL_TYPE || ' ' || NAME1 AS LABEL, ";
        $query .= "     T1.FINSCHOOL_TYPE AS VALUE ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD1 = 'L019' ";
        $query .= "                 AND L1.NAMECD2 = T1.FINSCHOOL_TYPE ";
        $query .= " WHERE ";
        $query .= "     T1.FINSCHOOL_TYPE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.FINSCHOOL_TYPE ";

        return $query;
    }

    //検索結果
    function getSchoolList($model){
        $query .= " SELECT ";
        $query .= "     FINSCHOOLCD || ' | ' || ";
        $query .= "     VALUE(L1.NAME1, '　') || ' | ' || ";
        $query .= "     VALUE(FINSCHOOL_NAME, '　') || ' | ' || ";
        $query .= "     VALUE(FINSCHOOL_ADDR1, '　') || ' | ' || ";
        $query .= "     VALUE(FINSCHOOL_ADDR2, '　') || ' | ' || ";
        $query .= "     VALUE(L2.NAME1, '　') || ' | ' || ";
        $query .= "     VALUE(L3.NAME1, '　') AS LABEL ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD1 = 'L019' ";
        $query .= "                 AND L1.NAMECD2 = T1.FINSCHOOL_TYPE ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD1 = 'Z003' ";
        $query .= "                 AND L2.NAMECD2 = T1.DISTRICTCD ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                 AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= " WHERE ";
        $query .= "         FINSCHOOLCD IS NOT NULL ";
        if ($model->field["FINSCHOOL_TYPE"] != '') {
            $query .= "     AND FINSCHOOL_TYPE = '{$model->field["FINSCHOOL_TYPE"]}' ";
        }
        if ($model->field["FINSCHOOL_NAME"] != '') {
            $query .= "     AND FINSCHOOL_NAME LIKE '%{$model->field["FINSCHOOL_NAME"]}%' ";
        }
        if ($model->field["FINSCHOOL_KANA"] != '') {
            $query .= "     AND FINSCHOOL_KANA LIKE '%{$model->field["FINSCHOOL_KANA"]}%' ";
        }
        $query .= " ORDER BY ";
        $query .= "     FINSCHOOL_TYPE, ";
        $query .= "     FINSCHOOLCD ";
        
        return $query;
    }
}
?>
