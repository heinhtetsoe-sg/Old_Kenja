<?php

require_once('for_php7.php');

class knjp701Query extends Query {

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
        $query .= "     COLLECT_L_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= "     AND YEAR          = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     COLLECT_L_CD ";

        return $query;
    }


    //１レコード取得
    function getRow($model, $collect_l_cd)
    {
       $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_L_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND  = '".$model->schoolKind."'";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND COLLECT_L_CD = '".$collect_l_cd."'";

       $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
       Query::dbCheckIn($db);
       return $Row;
    }

    //項目マスタに登録済みデータ取得
    function getCollectMcd($model, $collect_l_cd) {
       $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_M_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD     = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND  = '".$model->schoolKind."'";
        $query .= "     AND YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND COLLECT_L_CD = '".$collect_l_cd."'";

       $countM = $db->getOne($query);
       Query::dbCheckIn($db);
       return $countM;
    }

    //INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]          = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]       = $model->schoolKind;
        $data["YEAR"][TEXT]              = CTRL_YEAR;
        $data["COLLECT_L_CD"][TEXT]      = sprintf("%02d",$fields["COLLECT_L_CD"]);
        $data["COLLECT_L_NAME"][TEXT]    = $fields["COLLECT_L_NAME"];
        $data["COLLECT_L_ABBV"][TEXT]    = $fields["COLLECT_L_ABBV"];
        $data["LEVY_FLG"][TEXT]          = $fields["LEVY_FLG"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_L_MST");
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["COLLECT_L_NAME"][TEXT]    = $fields["COLLECT_L_NAME"];
        $data["COLLECT_L_ABBV"][TEXT]    = $fields["COLLECT_L_ABBV"];
        $data["LEVY_FLG"][TEXT]          = $fields["LEVY_FLG"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ";
        $where .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
        $where .= "   AND COLLECT_L_CD  = '".sprintf("%02d",$fields["COLLECT_L_CD"])."' ";
        $where .= "   AND YEAR          = '".CTRL_YEAR."' ";

        $query = Query::updateSQL($data, "COLLECT_L_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM COLLECT_L_MST ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND   = '".$model->schoolKind."' ";
        $query .= "   AND COLLECT_L_CD  = '".sprintf("%02d",$fields["COLLECT_L_CD"])."' ";
        $query .= "   AND YEAR          = '".CTRL_YEAR."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }
}
?>
