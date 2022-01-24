<?php

require_once('for_php7.php');

    $satno = $_POST["satno"];
    $mode = $_POST["mode"];
    $staffcd = $_POST["staffcd"];
    $ctrlYear = $_POST["ctrlYear"];
    
    $kamoku = $_POST["kamoku"];
    
    $kyouka = array("1" => "ENGLISH", "2" => "JAPANESE", "3" => "MATH");
    $choice = $kyouka[$kamoku];
    
    $db = Query::dbCheckOut();

    $query  = " UPDATE SAT_EXAM_DAT  ";
    if($mode == "0"){
        $query .= "    SET (SCORE_{$choice}, ";
        $query .= "         ABSENCE_{$choice}, ";
        $query .= "         SCORE_TOTAL, ";
        $query .= "         AVERAGE, ";
        $query .= "         RANK, ";
        $query .= "         DEVIATION, ";
        $query .= "         RANK_ALL, ";
        $query .= "         AREA_RANK_{$choice}, ";
        $query .= "         AREA_RANK_TOTAL, ";
        $query .= "         ALL_RANK_{$choice}, ";
        $query .= "         ALL_RANK_TOTAL, ";
        $query .= "         COMMENTNO, ";
        $query .= "         JUDGE_SAT, ";
        $query .= "         SCORE_{$choice}2, ";
        $query .= "         COMMENT_{$choice}, ";
        $query .= "         REGISTERCD, ";
        $query .= "         UPDATED) = ";
        $query .= "    (null, ";
        $query .= "     '".$mode."', ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     '".$staffcd."', ";
        $query .= "     sysdate()) ";
    }else{
        $query .= "    SET (ABSENCE_{$choice}, REGISTERCD, UPDATED) = ('".$mode."', '".$staffcd."', sysdate() ) ";
    }
    $query .= " WHERE ";
    $query .= "    SAT_NO = '".$satno."' ";
    $query .= " AND ";
    $query .= "    YEAR = '".$ctrlYear."' ";
    
    $db->query($query);
    

    Query::dbCheckIn($db);
    

?>