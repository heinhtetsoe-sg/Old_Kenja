<?php

require_once('for_php7.php');

class knjl316Query extends Query {

    //入試制度取得
    function getApctDiv($namecd, $year) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 IN (SELECT DISTINCT ";
        $query .= "                     APPLICANTDIV ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR = '".$year."') ";

        return $query;
    }
}
?>
