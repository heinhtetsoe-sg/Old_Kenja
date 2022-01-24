<?php
    $satno = $_POST["satno"];
    $tokuten = $_POST["tokuten"];
    $ctrlYear = $_POST["ctrlYear"];
    $staffcd = $_POST["staffcd"];
    $kamoku = $_POST["kamoku"];
    $kaisu = $_POST["kaisu"];
    
    $kyouka = array("1" => "ENGLISH", "2" => "JAPANESE", "3" => "MATH");
    $choice = $kyouka[$kamoku];
    
    if($kaisu < 2){
        $kaisu = "";
    }
    if($tokuten != "null"){
        $tokuten = "'".$tokuten."'";
    }
    $db = Query::dbCheckOut();
    
    $query  = " UPDATE ";
    $query .= "     SAT_EXAM_DAT ";
    $query .= " SET ";
    $query .= "     (SCORE_".$choice.$kaisu.", REGISTERCD, UPDATED) = (".$tokuten.", '".$staffcd."', current timestamp) ";
    $query .= " WHERE ";
    $query .= "     YEAR = '".$ctrlYear."' AND ";
    $query .= "     SAT_NO = '".$satno."' ";

    $db->query($query);
    
    Query::dbCheckIn($db);
    

?>
