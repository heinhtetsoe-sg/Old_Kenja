<?php

require_once('for_php7.php');

class knjz462Query extends Query {
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
        $query .= "     T1.*, ";
        $query .= "     T2.ITEMNAME ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_KIND_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CLUB_ITEM_MST T2 ON T2.ITEMCD = T1.ITEMCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T2.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " where T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " where T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.ITEMCD, ";
        $query .= "     T1.KINDCD ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getItemCnt($model, $itemcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_KIND_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$itemcd."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //１レコード取得
    function getClubItemKindMst($model, $itemcd, $kindcd) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.ITEMNAME ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_KIND_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     CLUB_ITEM_MST T2 ON T2.ITEMCD = T1.ITEMCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T2.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ITEMCD = '".$itemcd."' AND ";
        $query .= "     T1.KINDCD = '".$kindcd."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //レコードの存在チェック
    function checkClubItemMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$model->field["ITEMCD"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //レコードの存在チェック
    function checkClubItemKindMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_KIND_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$model->field["ITEMCD"]."' AND ";
        $query .= "     KINDCD = '".$model->field["KINDCD"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
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
    //CLUB_ITEM_MST を更新
    function &getInsertItemQuery($model) {
        $db = Query::dbCheckOut();

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]       = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]    = $model->schkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]       = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]    = SCHOOLKIND;
        }
        $data["ITEMCD"][TEXT]       = $model->field["ITEMCD"];
        $data["ITEMNAME"][TEXT]     = $model->field["ITEMNAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "CLUB_ITEM_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //CLUB_ITEM_KIND_MST を更新
    function &getInsertKindQuery($model) {
        $db = Query::dbCheckOut();

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]       = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]    = $model->schkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]       = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["ITEMCD"][TEXT]       = $model->field["ITEMCD"];
        $data["KINDCD"][TEXT]       = $model->field["KINDCD"];
        $data["KINDNAME"][TEXT]     = $model->field["KINDNAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "CLUB_ITEM_KIND_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    //CLUB_ITEM_MST を更新
    function &getUpdateItemQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["ITEMNAME"][TEXT]     = $model->field["ITEMNAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ITEMCD = '".$model->field["ITEMCD"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $where .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $where .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $where .= " AND SCHOOLCD = '".SCHOOLCD."' ";
            $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }


        $query = Query::updateSQL($data, "CLUB_ITEM_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //CLUB_ITEM_KIND_MST を更新
    function &getUpdateKindQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["KINDNAME"][TEXT]     = $model->field["KINDNAME"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ITEMCD = '".$model->field["ITEMCD"]."' AND ";
        $where .= "     KINDCD = '".$model->field["KINDCD"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $where .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $where .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $where .= " AND SCHOOLCD = '".SCHOOLCD."' ";
            $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query = Query::updateSQL($data, "CLUB_ITEM_KIND_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        //CLUB_ITEM_KIND_MST から削除
        $query  = " DELETE FROM ";
        $query .= "     CLUB_ITEM_KIND_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$model->field["ITEMCD"]."' AND ";
        $query .= "     KINDCD = '".$model->field["KINDCD"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_KIND_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$model->field["ITEMCD"]."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        //CLUB_ITEM_KIND_MST が0件になっていたら
        //CLUB_ITEM_MST も削除する
        $cnt = $db->getOne($query);
        if ($cnt <= 0) {
            $query  = " DELETE FROM ";
            $query .= "     CLUB_ITEM_MST ";
            $query .= " WHERE ";
            $query .= "     ITEMCD = '".$model->field["ITEMCD"]."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
                $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOLCD = '".SCHOOLCD."' ";
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }

            $db->query($query);
        }

        Query::dbCheckIn($db);
    }
}
?>
