<?php

require_once('for_php7.php');

class knjb080Query extends Query {

    //時間割パターンヘッダクエリ
    function getBscHdQuery()
    {
        $query  ="SELECT ";
        $query .="    t1.year, ";
        $query .="    t1.semester, ";
        $query .="    t2.semestername, ";
        $query .="    t1.bscseq, ";
        $query .="    t1.title ";
        $query .="FROM ";
        $query .="    sch_ptrn_hdat t1, ";
        $query .="    semester_mst t2 ";
        $query .="WHERE ";
        $query .="    t1.year='".CTRL_YEAR."' AND ";
        $query .="    t1.year=t2.year AND ";
        $query .="    t1.semester=t2.semester ";
        $query .="ORDER BY ";
        $query .="    t1.semester, ";
        $query .="    t1.bscseq ";

        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["BSCSEQ"].":".$row["TITLE"],
                           'value' => $row["YEAR"].",".$row["BSCSEQ"].",".$row["SEMESTER"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $row1;
    }

    //施設を取得
    function getFacility()
    {
        $query  = "SELECT ";
        $query .= "    faccd AS VALUE, ";
        $query .= "    faccd || '　' || facilityname AS LABEL ";
        $query .= "FROM ";
        $query .= "    v_facility_mst ";
        $query .= "WHERE ";
        $query .= "    year='".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    faccd ";

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
