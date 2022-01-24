<?php

require_once('for_php7.php');

// kanji=漢字

class knjl350jQuery extends Query{

    //入試区分取得
    function getTestdiv($namecd,$model)
    {
        $query  = " SELECT DISTINCT NAME1 AS LABEL, ";
        $query .= "                 NAMECD2 AS VALUE, ";
        $query .= "                 NAMESPARE2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$model->ObjYear ."' AND ";
        $query .= " NAMECD1 = '".$namecd."' ";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

}
?>
