<?php

require_once('for_php7.php');

class knjl351cQuery extends Query {

    //学校コードを取得：五条・和歌山を判断するため
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //入試制度取得
    function getApctDiv($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 IN( SELECT DISTINCT ";
        $query .= "                     APPLICANTDIV ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_RECEPT_DAT ";
        $query .= "                 WHERE ";	
        $query .= "                     ENTEXAMYEAR = '".$year."') ";	
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分取得
    function getTestDiv($namecd, $year)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd."' AND ";
        $query .= "     NAMECD2 IN( SELECT DISTINCT ";
        $query .= "                     TESTDIV ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_RECEPT_DAT ";
        $query .= "                 WHERE ";	
        $query .= "                     ENTEXAMYEAR = '".$year."' )";	
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
