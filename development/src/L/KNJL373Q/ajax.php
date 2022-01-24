<?php

require_once('for_php7.php');


    $satno = $_POST["satno"];
    $mode = $_POST["mode"];
    $staffcd = $_POST["staffcd"];
    
    $db = Query::dbCheckOut();

    $query  = " UPDATE SAT_EXAM_DAT  ";
    if($mode == "0"){
        $query .= "    SET (SCORE_ENGLISH, ";
        $query .= "         SCORE_MATH, ";
        $query .= "         SCORE_JAPANESE, ";
        $query .= "         ABSENCE,  ";
        $query .= "         ABSENCE_ENGLISH, ";
        $query .= "         ABSENCE_MATH, ";
        $query .= "         ABSENCE_JAPANESE, ";
        $query .= "         SCORE_TOTAL, ";
        $query .= "         AVERAGE, ";
        $query .= "         RANK, ";
        $query .= "         DEVIATION, ";
        $query .= "         RANK_ALL, ";
        $query .= "         AREA_RANK_ENGLISH, ";
        $query .= "         AREA_RANK_MATH, ";
        $query .= "         AREA_RANK_JAPANESE, ";
        $query .= "         AREA_RANK_TOTAL, ";
        $query .= "         ALL_RANK_ENGLISH, ";
        $query .= "         ALL_RANK_MATH, ";
        $query .= "         ALL_RANK_JAPANESE, ";
        $query .= "         ALL_RANK_TOTAL, ";
        $query .= "         RANK_HOPE1, ";
        $query .= "         RANK_HOPE2, ";
        $query .= "         RANK_HOPE3, ";
        $query .= "         COMMENTNO, ";
        $query .= "         JUDGE_SAT, ";
        $query .= "         REWARD, ";
        $query .= "         SCORE_ENGLISH2, ";
        $query .= "         SCORE_MATH2, ";
        $query .= "         SCORE_JAPANESE2, ";
        $query .= "         COMMENT_ENGLISH, ";
        $query .= "         COMMENT_MATH, ";
        $query .= "         COMMENT_JAPANESE, ";
        $query .= "         REGISTERCD, ";
        $query .= "         UPDATED) = ";
        $query .= "    (null, ";
        $query .= "     null, ";
        $query .= "     null, ";
        $query .= "     '".$mode."',  ";
        $query .= "     '".$mode."', ";
        $query .= "     '".$mode."', ";
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
        $query .= "    SET (ABSENCE, ABSENCE_ENGLISH, ABSENCE_MATH, ABSENCE_JAPANESE, REGISTERCD, UPDATED) = ('".$mode."','".$mode."','".$mode."','".$mode."', '".$staffcd."', sysdate() ) ";
    }
    $query .= " WHERE ";
    $query .= "    SAT_NO = '".$satno."' ";
    
    $db->query($query);
    
    
    $query  = " UPDATE SAT_APP_FORM_MST  ";
    $query .= "    SET (ABSENCE, REGISTERCD, UPDATED) = ('".$mode."', '".$staffcd."', sysdate() ) ";
    $query .= " WHERE ";
    $query .= "    SAT_NO = '".$satno."' ";

    $db->query($query);

    Query::dbCheckIn($db);
    

?>