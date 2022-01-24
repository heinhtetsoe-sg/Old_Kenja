<?php

require_once('for_php7.php');

class knjj080Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //委員会年度一覧取得
    function selectQuery($model)
    {
        $query  = " select ";
        $query .= "     T1.COMMITTEE_FLG, ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     T1.COMMITTEENAME ";
        $query .= " from ";
        $query .= "     COMMITTEE_YDAT T2, ";
        $query .= "     COMMITTEE_MST T1 ";
        $query .= " where ";
        $query .= "     T2.COMMITTEE_FLG    = T1.COMMITTEE_FLG ";
        $query .= " and T2.COMMITTEECD      = T1.COMMITTEECD ";
        $query .= " and T2.YEAR             = '".$model->year."'"; 
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " order by ";
        $query .= "     T1.COMMITTEE_FLG, ";
        $query .= "     T1.COMMITTEECD ";

        return $query;
    }

    //年度一覧取得
    function selectYearQuery($model)
    {
        $query  = " select distinct ";
        $query .= "     YEAR ";
        $query .= " from ";
        $query .= "     COMMITTEE_YDAT ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " order by ";
        $query .= "     YEAR desc ";

        return $query;
    }
    
    //委員会一覧取得
    function selectClassQuery($commicd_array,$model)
    {
//NO001 del       $commicd = implode($commicd_array,"','");

//NO001     return "select COMMITTEE_FLG, COMMITTEECD, COMMITTEENAME from COMMITTEE_MST where COMMITTEE_FLG || COMMITTEECD not in ('".$commicd."') order by COMMITTEE_FLG, COMMITTEECD ";
        /* NO001↓ */
        $query  = " SELECT ";
        $query .= "     cm.COMMITTEE_FLG, ";
        $query .= "     cm.COMMITTEECD, ";
        $query .= "     cm.COMMITTEENAME ";
        $query .= " FROM ";
        $query .= "     COMMITTEE_MST cm ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT 'X' ";
        $query .= "                 FROM ";
        $query .= "                     COMMITTEE_YDAT cy";
        $query .= "                 WHERE ";
        $query .= "                     cy.year = '".$model->year."'";
        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                 AND cy.SCHOOLCD     = cm.SCHOOLCD ";
            $query .= "                 AND cy.SCHOOL_KIND  = cm.SCHOOL_KIND ";
        }
        $query .= "                 AND cy.COMMITTEE_FLG || cy.COMMITTEECD = cm.COMMITTEE_FLG || cm.COMMITTEECD)";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND cm.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND cm.SCHOOL_KIND  = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND cm.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND cm.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     cm.COMMITTEE_FLG, ";
        $query .= "     cm.COMMITTEECD ";

        return $query;
        /* NO001↑ */
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery(&$model)
    {        
        //委員会コードを配列に格納
        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $query  = " DELETE from ";
        $query .= "     COMMITTEE_YDAT ";
        $query .= " where ";
        $query .= "     YEAR = '" .$model->year ."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);
        
        if ($array[0] != ""){
            //委員会年度データの登録
            for ($i = 0; $i < get_count($array); $i++){
                $data = array();
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]      = $model->schKind;
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
                }
                $data["YEAR"][TEXT]             = $model->year;
                $data["COMMITTEE_FLG"][TEXT]    = substr($array[$i],0,1);
                $data["COMMITTEECD"][TEXT]      = substr($array[$i],1);
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $db->query(Query::insertSQL($data, "COMMITTEE_YDAT"));
            }
        } else {
            $model->year = "";
        }
        $db->commit(); // ﾄﾗﾝｻﾞｸｼｮﾝをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
