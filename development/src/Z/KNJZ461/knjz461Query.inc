<?php

require_once('for_php7.php');

class knjz461Query extends Query {
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
        $query .= "     NAME1 ";

        return $query;
    }

    //左フレームのリスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLUB_HOST_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->leftSchkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE SCHOOLCD  = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     HOSTCD ";

        return $query;
    }

    //CLUB_HOST_MSTから１レコード取得
    function getClubHostMst($model,$hostcd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLUB_HOST_MST ";
        $query .= " WHERE ";
        $query .= "     HOSTCD = '".$hostcd."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //レコードの存在チェック
    function checkClubHostMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLUB_HOST_MST ";
        $query .= " WHERE ";
        $query .= "     HOSTCD = '".$model->field["HOSTCD"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]       = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]    = $model->schkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]       = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]    = SCHOOLKIND;
        }
        $data["HOSTCD"][TEXT]       = $model->field["HOSTCD"];
        $data["HOSTNAME"][TEXT]     = $model->field["HOSTNAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "CLUB_HOST_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["HOSTNAME"][TEXT]     = $model->field["HOSTNAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     HOSTCD = '".$model->field["HOSTCD"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $where .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $where .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $where .= " AND SCHOOLCD = '".SCHOOLCD."' ";
            $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $query = Query::updateSQL($data, "CLUB_HOST_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CLUB_HOST_MST ";
        $query .= " WHERE ";
        $query .= "     HOSTCD = '".$model->field["HOSTCD"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $db->query($query);
        Query::dbCheckIn($db);
    }
}
?>