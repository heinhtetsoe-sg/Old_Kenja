<?php

require_once('for_php7.php');

class knjz210hQuery extends Query {

    //評定マスタ情報抽出
    function selectQuery($model)
    {
        $cnt = ($model->sepa == 1) ? 10 : 5 ;

        $query  =  " SELECT ASSESSLEVEL,RATE,ASSESSLEVEL5,UPDATED ";
        $query .=  " FROM   RATEASSESS_MST ";
        $query .=  " WHERE  YEAR = '".CTRL_YEAR."' AND ASSESSLEVEL <= ".$cnt." ";
        $query .=  " ORDER BY ASSESSLEVEL DESC";

        return $query;
    }

    //Insert作成
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  =  " DELETE FROM RATEASSESS_MST ";
        $query .=  " WHERE  YEAR = '".CTRL_YEAR."' ";
        $db->query($query);

        //追加
        $cnt = ($model->sepa == 1) ? 10 : 5 ;
        for ($i=1; $i<=$cnt; $i++){

            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["ASSESSLEVEL"][NUMBER]    = $i;
            $data["RATE"][NUMBER]           = $model->field["RATE".$i];
            $data["ASSESSLEVEL5"][NUMBER]   = $model->field["ASSESSLEVEL5".$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "rateassess_mst");
            $db->query($query);

        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

}
?>
