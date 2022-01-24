<?php

require_once('for_php7.php');

class knjz210Query extends Query {

    function getZ040()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'Z040' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //評定マスタ情報抽出
    function selectQuery($model)
    {
        $sep = $model->sepa;

        $query  =  " SELECT * FROM ASSESS_MST ";
        $query .=  " WHERE ASSESSCD = '".$sep."'";

        return $query;
    }

    //評定ヘッダデータ情報抽出
    function selectHdat()
    {
        return " SELECT * FROM ASSESS_HDAT ";
    }

    function getLevel()
    {
        $db = Query::dbCheckOut();
        
        $query = knjz210Query::selectHdat();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if($row["ASSESSCD"]=="2"){
//                $levelcnt[0] = $row["ASSESSLEVELCNT"];   //評価区分は3の時の上限値

                  $semes_assesscd = $db->getOne("SELECT semes_assesscd FROM school_mst WHERE year='".CTRL_YEAR."'");
                  if($semes_assesscd == 0){         //学校マスタの期末評価処理が==0
                      $levelcnt[0] = "100";
                  }else{
                      $levelcnt[0] = $row["ASSESSLEVELCNT"];
                  }

            }elseif($row["ASSESSCD"]=="3"){
                $levelcnt[1] = $row["ASSESSLEVELCNT"];   //評価区分は4の時の上限値

            }
        
        }
        Query::dbCheckIn($db);
        return $levelcnt;
    }
    
    //Insert作成
    function &getInsertQuery($model)
    {
        $org_levelcnt = knjz210Query::getLevel();
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $query  =  " DELETE FROM ASSESS_MST ";
        $query .=  " WHERE ASSESSCD = '";
        $query .=  $model->sepa ;
        $query .=  "'";

        $db->query($query);

        $data["ASSESSLEVELCNT"][NUMBER] = $model->level;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $where = "WHERE ASSESSCD = '".$model->sepa."'";
        
        $query = Query::updateSQL($data, "assess_hdat", $where);
        $db->query($query);
        
        $query = knjz210Query::selectHdat();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if($row["ASSESSCD"]=="2"){
//                $levelcnt2 = $row["ASSESSLEVELCNT"];   //評価区分は3の時の上限値

                  $semes_assesscd = $db->getOne("SELECT semes_assesscd FROM school_mst WHERE year='".CTRL_YEAR."'");
                  if($semes_assesscd == 0){         //学校マスタの期末評価処理が==0
                      $levelcnt2 = "100";
                  }else{
                      $levelcnt2 = $row["ASSESSLEVELCNT"];
                  }

            }elseif($row["ASSESSCD"]=="3"){
                $levelcnt3 = $row["ASSESSLEVELCNT"];   //評価区分は4の時の上限値
            }
        
        }
        
        $data = array();
        
        $ii = $model->level;
        
        for($i=1; $i<=$ii; $i++)
        {
            $ASSESSLOW                      = "ASSESSLOW".$i;
            $data["ASSESSCD"][TEXT]         = $model->sepa;
            $data["ASSESSLEVEL"][NUMBER]    = $i;
            $data["ASSESSMARK"][TEXT]       = $model->field["ASSESSMARK".$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;

            if($i == 1 && $model->sepa == 2){
                    $data["ASSESSLOW"][NUMBER]      = 0;
            }else{
                    $data["ASSESSLOW"][NUMBER]      = $model->field[$ASSESSLOW];
            }


            switch($model->sepa){
                case 1:
                    if($i == $ii){
                            $data["ASSESSHIGH"][NUMBER] = $model->field["ASSESSHIGH".$i];
                    }else{
                            $ASSESSHIGH = "ASSESSLOW".($i + 1);
                            $data["ASSESSHIGH"][NUMBER]     = ((int)$model->field[$ASSESSHIGH] - 1);
                    }       
                    break;
                case 2:
                    if($i == $ii){
                            $data["ASSESSHIGH"][NUMBER]     = 100;
                    }else{
                            $ASSESSHIGH = "ASSESSLOW".($i + 1);
                            $data["ASSESSHIGH"][NUMBER]     = ((int)$model->field[$ASSESSHIGH] - 1);
                    }
                    break;
                case 3:
                    if($i == $ii){
                            $data["ASSESSHIGH"][NUMBER] = $levelcnt2;
                    }else{
                            $ASSESSHIGH = "ASSESSLOW".($i + 1);
                            $data["ASSESSHIGH"][NUMBER] = ((int)$model->field[$ASSESSHIGH] - 1);
                    }
                    break;
                case 4:
                    if($i == $ii){
                            $data["ASSESSHIGH"][NUMBER] = $levelcnt3;
                    }else{
                            $ASSESSHIGH = "ASSESSLOW".($i + 1);
                            $data["ASSESSHIGH"][NUMBER] = ((int)$model->field[$ASSESSHIGH] - 0.1);
                    }
                    break;
                default:
            }

#            if($model->sepa == 1){
#                    if($i == $ii){
#                            $data["ASSESSHIGH"][NUMBER] = $model->field["ASSESSHIGH".$i];
#                    }else{
#                            $ASSESSHIGH = "ASSESSLOW".($i + 1);
#                            $data["ASSESSHIGH"][NUMBER]     = ($model->field[$ASSESSHIGH] - 1);
#                    }       
#            }elseif($model->sepa == 2){
#                    if($i == $ii){
#                            $data["ASSESSHIGH"][NUMBER]     = 100;
#                    }else{
#                            $ASSESSHIGH = "ASSESSLOW".($i + 1);
#                            $data["ASSESSHIGH"][NUMBER]     = ($model->field[$ASSESSHIGH] - 1);
#                    }
#            }elseif($model->sepa == 3){
#                    if($i == $ii){
#                            $data["ASSESSHIGH"][NUMBER] = $levelcnt2;
#                    }else{
#                            $ASSESSHIGH = "ASSESSLOW".($i + 1);
#                            $data["ASSESSHIGH"][NUMBER] = ($model->field[$ASSESSHIGH] - 1);
#                    }
#            }elseif($model->sepa == 4){
#                    if($i == $ii){
#                            $data["ASSESSHIGH"][NUMBER] = $levelcnt3;
#                    }else{
#                            $ASSESSHIGH = "ASSESSLOW".($i + 1);
#                            $data["ASSESSHIGH"][NUMBER] = ($model->field[$ASSESSHIGH] - 0.1);
#                    }
#            }

            $data["UPDATED"][FUNC]  = "sysdate()";

            $query = Query::insertSQL($data, "assess_mst");

            $db->query($query);
        }
        
        $data = array();
        $overBig = 1;
        /**
     * 期末評定の段階数と学年末評定の段階数を変える時、
       学年末評定の上限と帳票評定の上限を更新
     */
        if($model->sepa == 2){
        
            $query = knjz210Query::updateData($levelcnt2,3);
            $db->query($query);
            
            if($org_levelcnt[0] != $levelcnt2){
                $overBig = 3;
            }
        }elseif($model->sepa == 3){
        
            $query = knjz210Query::updateData($levelcnt3,4);
            $db->query($query);
            
            if($org_levelcnt[1] != $levelcnt3){
                $overBig = 4;
            }
        }
        
        $db->commit();
        Query::dbCheckIn($db);

        return $overBig;
    }
    
    function updateData($cnt,$num)
    {
        $data["ASSESSHIGH"][NUMBER] = $cnt;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $where  = "WHERE ASSESSCD = '".$num."' AND ASSESSLEVEL = ";
        $where .= "(SELECT MAX(ASSESSLEVEL) FROM assess_mst WHERE ASSESSCD = '".$num."' GROUP BY ASSESSCD)" ;
        $query = Query::updateSQL($data, "assess_mst", $where);
        
        return $query;
    }
}
?>
