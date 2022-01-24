<?php

require_once('for_php7.php');


class knjp310Query extends Query {

    //エラーデータ取得
    function geterrcd()
    {

        $query  = " SELECT ";
        $query .= "     COLNAME, ";
        $query .= "     ERR_MSG ";
        $query .= " FROM ";
        $query .= "     APPL_RESULT_ERR_DATA ";
        $query .= " WHERE ";
        $query .= "     PROCESSCD = '2' AND ";
        $query .= "     FILE_LINE_NUMBER = 0000 ";

        return $query;
    }

    //申込み名称取得
    function getappli($fieldnm,$cd)
    {
        $query  = " SELECT APPLICATIONNAME ";
        $query .= " FROM ";
        $query .= "      APPLICATION_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND ".$fieldnm." = '".$cd."' ";

        return $query;
    }

    //申込み名称取得
    function getschool()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLNAME1 like '%近畿%中学%' ";

        return $query;
    }

}
?>
