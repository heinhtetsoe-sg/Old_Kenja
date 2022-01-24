<?php

require_once('for_php7.php');

class knjp980Query extends Query {

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
        $query .= "     NAMECD2 ";

        return $query;
    }

    //データ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     LEVY_L_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= "     AND YEAR = '".$model->year."'";
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
        $query .= "     SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND  = '".$model->schoolKind."'";
        $query .= "     AND YEAR = '".$model->year."'";
        $query .= "     AND LEVY_L_CD = '".$levy_l_cd."'";

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]            = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]         = $model->schoolKind;
        $data["YEAR"][TEXT]                = $model->year;
        $data["LEVY_L_CD"][TEXT]           = sprintf("%02d",$model->field["LEVY_L_CD"]);
        $data["LEVY_L_NAME"][TEXT]         = $model->field["LEVY_L_NAME"];
        $data["LEVY_L_ABBV"][TEXT]         = $model->field["LEVY_L_ABBV"];

        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

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

        $data["LEVY_L_NAME"][TEXT]         = $model->field["LEVY_L_NAME"];
        $data["LEVY_L_ABBV"][TEXT]         = $model->field["LEVY_L_ABBV"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "sysdate()";

        $where  = " WHERE ";
        $where .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
        $where .= "   AND YEAR          = '".$model->year."' ";
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
        $query .= " WHERE ";
        $query .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
        $query .= "   AND YEAR          = '".$model->year."' ";
        $query .= "   AND LEVY_L_CD     = '".sprintf("%02d",$model->field["LEVY_L_CD"])."'";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
}
?>
