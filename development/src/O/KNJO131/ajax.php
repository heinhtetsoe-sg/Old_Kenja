<?php

    $data = array();
    $schregno = $_POST["schregno"];
    
    
    $db = Query::dbCheckOut();
    
    //�w���v�^�̎�荞�񂾃f�[�^�����邩�ǂ����`�F�b�N
    $query  = " SELECT ";
    $query .= "     COUNT(*) ";
    $query .= " FROM ";
    $query .= "     CE_APP_YOUROKU_SYUBETU_MST ";
    $query .= " WHERE ";
    $query .= "     SCHREGNO = '".$schregno."' ";
    
    $yourokuCnt = $db->getOne($query);
    
    //���N�f�f�[���̎捞�����łɂ���Ă��邩�`�F�b�N
    $query  = " SELECT ";
    $query .= "     COUNT(*) ";
    $query .= " FROM ";
    $query .= "     CE_APP_HOKEN_S_MST ";
    $query .= " WHERE ";
    $query .= "     SCHREGNO = '".$schregno."' ";
    
    $hokenCnt = $db->getOne($query);
    
    //�w�Дԍ��d���`�F�b�N
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