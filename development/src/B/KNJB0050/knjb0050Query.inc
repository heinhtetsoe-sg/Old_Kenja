<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjb0050Query.inc 56585 2017-10-22 12:47:53Z maeshiro $
class knjb0050Query extends Query {

    //時間割パターンヘッダクエリ
    function getBscHdQuery($model)
    {
        $query  = "SELECT ";
        $query .= " YEAR || ',' || char(BSCSEQ) AS VALUE, ";
        $query .= " YEAR || '年 Seq' || char(BSCSEQ) || ':' || TITLE AS LABEL ";
        $query .= "FROM ";
        $query .= " SCH_PTRN_HDAT ";
        $query .= "WHERE ";
        $query .= " YEAR = '" .$model->control[年度] ."' ";
        $query .= "ORDER BY YEAR,BSCSEQ";

        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $row1;
    }

    //所属クエリ
    function getSectQuery()
    {
        $query  = "SELECT ";
        $query .= " SECTIONCD AS VALUE, ";
        $query .= " SECTIONCD || '　' || SECTIONNAME AS LABEL ";
        $query .= "FROM ";
        $query .= " SECTION_MST ";
        $query .= "ORDER BY SECTIONCD";

        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $row1;
    }

}
?>
