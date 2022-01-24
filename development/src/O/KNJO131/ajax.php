<?php

    $data = array();
    $schregno = $_POST["schregno"];
    
    
    $db = Query::dbCheckOut();
    
    //指導要録の取り込んだデータがあるかどうかチェック
    $query  = " SELECT ";
    $query .= "     COUNT(*) ";
    $query .= " FROM ";
    $query .= "     CE_APP_YOUROKU_SYUBETU_MST ";
    $query .= " WHERE ";
    $query .= "     SCHREGNO = '".$schregno."' ";
    
    $yourokuCnt = $db->getOne($query);
    
    //健康診断票情報の取込がすでにされているかチェック
    $query  = " SELECT ";
    $query .= "     COUNT(*) ";
    $query .= " FROM ";
    $query .= "     CE_APP_HOKEN_S_MST ";
    $query .= " WHERE ";
    $query .= "     SCHREGNO = '".$schregno."' ";
    
    $hokenCnt = $db->getOne($query);
    
    //学籍番号重複チェック
    $query  = " SELECT ";
    $query .= "     COUNT(*) ";
    $query .= " FROM ";
    $query .= "     SCHREG_BASE_MST ";
    $query .= " WHERE ";
    $query .= "     SCHREGNO = '".$schregno."' ";

    $baseCnt = $db->getOne($query);


    Query::dbCheckIn($db);
    
    if($yourokuCnt > 0){
        if($hokenCnt > 0){
            $type = '2';
        }else{
            $type = '1';
        }
    }else{
        if($baseCnt > 0){
            if($hokenCnt > 0){
                $type = '3';
            }else{
                $type = '4';
            }
        }else{
            $type = '5';
        }
    }

    echo $type;
    

?>