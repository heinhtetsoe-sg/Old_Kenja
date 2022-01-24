<?php

require_once('for_php7.php');

class knjmp980Query extends Query {

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     LEVY_L_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."'";
        $query .= " ORDER BY ";
        $query .= "     LEVY_L_CD ";

        return $query;
    }


    //１レコード取得
    function getRow($model, $levy_l_cd)
    {
       $db = Query::dbCheckOut();

       $query  = " SELECT ";
       $query .= "     * ";
       $query .= " FROM ";
       $query .= "     LEVY_L_MST ";
       $query .= " WHERE ";
       $query .= "     YEAR = '".$model->year."'";
       $query .= " AND LEVY_L_CD = '".$levy_l_cd."'";

       $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
       Query::dbCheckIn($db);
       return $Row;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["YEAR"][TEXT]           = $model->year;
        $data["LEVY_L_CD"][TEXT]      = sprintf("%02d",$model->field["LEVY_L_CD"]);
        $data["LEVY_L_NAME"][TEXT]    = $model->field["LEVY_L_NAME"];
        $data["LEVY_L_ABBV"][TEXT]    = $model->field["LEVY_L_ABBV"];
        
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_L_MST");
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["LEVY_L_NAME"][TEXT]    = $model->field["LEVY_L_NAME"];
        $data["LEVY_L_ABBV"][TEXT]    = $model->field["LEVY_L_ABBV"];
        
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE YEAR  = '".$model->year."'";
        $where .= "   AND LEVY_L_CD  = '".sprintf("%02d",$model->field["LEVY_L_CD"])."'";

        $query = Query::updateSQL($data, "LEVY_L_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM LEVY_L_MST ";
        $query .= " WHERE YEAR  = '".$model->year."' ";
        $query .= "   AND LEVY_L_CD  = '".sprintf("%02d",$model->field["LEVY_L_CD"])."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
    
    //今年度のデータが存在するかチェック
    function CheckData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM LEVY_L_MST WHERE YEAR = '".$year."'";
        $row = $db->getOne($query);
        
        Query::dbCheckIn($db);
        return $row;
    }
    
    //前年度コピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO LEVY_L_MST ";
        $query .= "           ( YEAR, ";
        $query .= "             LEVY_L_CD, ";
        $query .= "             LEVY_L_NAME, ";
        $query .= "             LEVY_L_ABBV, ";
        $query .= "             registercd, ";
        $query .= "             updated ) ";
        $query .= "   (SELECT ";
        $query .= "         '".$model->year."', ";
        $query .= "         T1.LEVY_L_CD, ";
        $query .= "         T1.LEVY_L_NAME, ";
        $query .= "         T1.LEVY_L_ABBV, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         LEVY_L_MST T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.year = '".($model->year - 1)."'";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
