<?php

require_once('for_php7.php');

class knjwpri_searchQuery extends Query {

    //検索結果
    function getPriSchoolList($model){
        $query .= " SELECT ";
        $query .= "     L1.PRISCHOOLCD || ' | ' || ";
        $query .= "     VALUE(T1.PRISCHOOL_CLASS_CD, '@@@@@@@') || ' | ' || ";
        $query .= "     VALUE(L1.PRISCHOOL_NAME, '　') || ' | ' || ";
        $query .= "     VALUE(T1.PRISCHOOL_NAME, '　') AS LABEL ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST L1 ";
        $query .= " LEFT JOIN ";
        $query .= "     PRISCHOOL_CLASS_MST T1 ON L1.PRISCHOOLCD = T1.PRISCHOOLCD ";
        $query .= " WHERE ";
        $query .= "         L1.PRISCHOOLCD IS NOT NULL ";
        if ($model->field["PRISCHOOL_NAME"] != '') {
            $query .= "     AND L1.PRISCHOOL_NAME LIKE '%{$model->field["PRISCHOOL_NAME"]}%' ";
        }
        if ($model->field["PRISCHOOL_CLASS_NAME"] != '') {
            $query .= "     AND T1.PRISCHOOL_NAME LIKE '%{$model->field["PRISCHOOL_CLASS_NAME"]}%' ";
        }
        $query .= " ORDER BY ";
        $query .= "     L1.PRISCHOOLCD, ";
        $query .= "     T1.PRISCHOOL_CLASS_CD ";

        return $query;
    }
}
?>
