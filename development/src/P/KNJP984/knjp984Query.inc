<?php

require_once('for_php7.php');

class knjp984Query extends Query {

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

    //グループマスタ取得
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * " ;
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= " ORDER BY ";
        $query .= "     LEVY_GROUP_CD ";

        return $query;
    }
    
    //１レコード取得
    function getRow($model, $cd)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= "     AND LEVY_GROUP_CD = '" .$cd ."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    /**
     * INSERT
     */
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data["SCHOOLCD"][TEXT]          = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]       = $model->schoolKind;
        $data["LEVY_GROUP_CD"][TEXT]     = $fields["LEVY_GROUP_CD"];
        $data["LEVY_GROUP_NAME"][TEXT]   = $fields["LEVY_GROUP_NAME"];
        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $query = Query::insertSQL($data, "LEVY_GROUP_MST");
        
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data["LEVY_GROUP_NAME"][TEXT]  = $fields["LEVY_GROUP_NAME"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "   SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $where .= "   AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $where .= "   AND LEVY_GROUP_CD = '" .$fields["LEVY_GROUP_CD"] ."' ";

        $query = Query::updateSQL($data, "LEVY_GROUP_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    
    //設定している会計科目を取得
    function g_data_chk($model, $cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD       = '".sprintf("%012d", SCHOOLCD)."' ";
        $query .= "     AND SCHOOL_KIND    = '".$model->schoolKind."' ";
        $query .= "     AND LEVY_GROUP_CD = '" .$cd ."' ";

        return $query;
    }
    
    /**
     * DELETE
     */
    function &getDeleteQuery($model, $fields)
    {
        //設定している会計科目を取得
        $db = Query::dbCheckOut();
        $getCount = $db->getOne(knjp984Query::g_data_chk($model, $fields["LEVY_GROUP_CD"]));
        if ($getCount == 0) {
            $query  = " DELETE FROM ";
            $query .= "     LEVY_GROUP_MST ";
            $query .= " WHERE ";
            $query .= "     SCHOOLCD            = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND     = '".$model->schoolKind."' ";
            $query .= "     AND LEVY_GROUP_CD   = '" .$fields["LEVY_GROUP_CD"] ."' ";

            $db->query($query);
            $result = true;
        } else {
            $result = false;
        }

        Query::dbCheckIn($db);
        return $result;
    }
}
?>
