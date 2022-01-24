<?php

require_once('for_php7.php');

class knjb0021Query extends Query {
    //所属取得
    function getSectionMst($model) {
        $query  = " WITH T_COMMON (VALUE,LABEL) AS ( ";
        $query .= "     VALUES('ALL','すべて表示') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     SECTIONCD AS VALUE, ";
        $query .= "     SECTIONCD || ':' || SECTIONNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SECTION_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     T_COMMON ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
