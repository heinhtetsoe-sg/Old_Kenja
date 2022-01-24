<?php

require_once('for_php7.php');


class knjb0220Query extends Query {

    //履修設定区分
    function getKubun()
    {
        $query  = " WITH VAL_T(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('0', '全て') ";
        $query .= "     UNION ";
        $query .= "     VALUES('1', '履修条件') ";
        $query .= "     UNION ";
        $query .= "     VALUES('2', '選択履修条件') ";
        $query .= "     UNION ";
        $query .= "     VALUES('3', '必履修条件') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     VAL_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
