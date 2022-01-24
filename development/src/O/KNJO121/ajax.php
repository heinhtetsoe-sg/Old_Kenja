<?php

    $data = array();
    $schregno = $_POST["schregno"];
    $grade = $_POST["grade"];
    $attendno = $_POST["attendno"];
    $radio = $_POST["radio"];
    $year = $_POST["year"];
    $semester = $_POST["semester"];
    
    //変数いじる
    $gaku = explode(",", $grade);   //[0]が学年[1]がクラス
    
    $db = Query::dbCheckOut();
    
    //すでに取り込んだデータがあるかどうかチェック
    $query  = " SELECT ";
    $query .= "     COUNT(*) ";
    $query .= " FROM ";
    $query .= "     CE_APP_GAKUREIBO_F_MST ";
    $query .= " WHERE ";
    $query .= "     SCHREGNO = '".$schregno."' ";
    
    $gakureiboCnt = $db->getOne($query);

    //すでに取り込んだデータがあるかどうかチェック
    $query  = " SELECT ";
    $query .= "     COUNT(*) ";
    $query .= " FROM ";
    $query .= "     CE_APP_YOUROKU_SYUBETU_MST ";
    $query .= " WHERE ";
    $query .= "     SCHREGNO = '".$schregno."' ";
    
    $yourokuCnt = $db->getOne($query);

    //年組番重複チェック
    $query  = " SELECT ";
    $query .= "     COUNT(*) ";
    $query .= " FROM ";
    $query .= "     schreg_regd_dat ";
    $query .= " WHERE ";
    $query .= "     grade    = '".$gaku[0]."' AND ";
    $query .= "     hr_class = '".$gaku[1]."' AND ";
    $query .= "     year     = '".$year."' AND ";
    $query .= "     semester = '".$semester."' AND ";
    $query .= "     attendno = '".sprintf("%03d",$attendno)."' ";
    if($yourokuCnt > 0 || $gakureiboCnt > 0){
        $query .= " AND ";
        $query .= "     SCHREGNO != '".$schregno."' ";
    }

    $gradeCnt = $db->getOne($query);
    
    //学籍番号重複チェック
    $query  = " SELECT ";
    $query .= "     COUNT(*) ";
    $query .= " FROM ";
    $query .= "     SCHREG_BASE_MST ";
    $query .= " WHERE ";
    $query .= "     SCHREGNO = '".$schregno."' ";

    $baseCnt = $db->getOne($query);


    Query::dbCheckIn($db);
    
    if($radio == "1"){
        //取込が要録のとき
        if($yourokuCnt > 0){
            if($gradeCnt > 0){
                $type = 3;
            }else{
                $type = 4;
            }
        }else{
            if($gakureiboCnt > 0){
                if($gradeCnt > 0){
                    $type = 3;
                }else{
                    $type = 6;
                }
            }else{
                if($baseCnt > 0){
                    $type = 2;
                }else{
                    if($gradeCnt > 0){
                        $type = 3;
                    }else{
                        $type = 1;
                    }
                }
            }
        }
    }else{
        //取込が学齢簿のとき
        if($gakureiboCnt > 0){
            if($gradeCnt > 0){
                $type = 3;
            }else{
                $type = 5;
            }
        }else{
            if($yourokuCnt > 0){
                if($gradeCnt > 0){
                    $type = 3;
                }else{
                    $type = 7;
                }
            }else{
                if($baseCnt > 0){
                    $type = 2;
                }else{
                    if($gradeCnt > 0){
                        $type = 3;
                    }else{
                        $type = 1;
                    }
                }
            }
        }
    }

    echo $type;
    

?>