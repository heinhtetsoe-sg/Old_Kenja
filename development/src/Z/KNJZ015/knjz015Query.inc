<?php

require_once('for_php7.php');

class knjz015Query extends Query {
/*****左画面*****/
    //リスト
    function getList($model) {
        if ($model->sort["SRT_S"] == 1) {
            $OrderBy1 = " ORDER BY SORT_SCHOOL_KIND ASC, A1.PROGRAMID,SORT_SELECT_SCHOOL_KIND ";
            $OrderBy2 = "";
        } elseif ($model->sort["SRT_S"] == -1) {
            $OrderBy1 = "ORDER BY SORT_SCHOOL_KIND DESC, A1.PROGRAMID,SORT_SELECT_SCHOOL_KIND ";
            $OrderBy2 = "";
        } elseif ($model->sort["SRT_P"] == 1) {
            $OrderBy2 = "ORDER BY A1.PROGRAMID ASC, SORT_SCHOOL_KIND,SORT_SELECT_SCHOOL_KIND ";
            $OrderBy1 = "";
        } elseif ($model->sort["SRT_P"] == -1) {
            $OrderBy2 = "ORDER BY A1.PROGRAMID DESC, SORT_SCHOOL_KIND,SORT_SELECT_SCHOOL_KIND ";
            $OrderBy1 = "";
        }
        $query  = " SELECT ";
        $query .= "     V2.ABBV1 AS SCHOOL_KIND_LABEL, ";
        $query .= "     A1.SCHOOL_KIND, ";
        $query .= "     A1.PROGRAMID, ";
        $query .= "     V1.ABBV1 AS SELECT_SCHOOL_KIND, ";
        $query .= "     V1.NAME1 AS SORT_SELECT_SCHOOL_KIND, ";
        $query .= "     V2.NAME1 AS SORT_SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_PRG_SCHOOLKIND_MST A1  ";
        $query .= " LEFT JOIN V_NAME_MST V1  ON V1.YEAR               = '".CTRL_YEAR."'  ";
        $query .= "                         AND V1.NAMECD1            = 'A023'  ";
        $query .= "                         AND A1.SELECT_SCHOOL_KIND = V1.NAME1 ";
        $query .= " LEFT JOIN V_NAME_MST V2  ON V2.YEAR        = '".CTRL_YEAR."'  ";
        $query .= "                         AND V2.NAMECD1     = 'A023'  ";
        $query .= "                         AND A1.SCHOOL_KIND = V2.NAME1 ";
        $query .= $OrderBy1.$OrderBy2;

        return $query;
    }

    //左画面のテーブルのROWSPANの値のためのカウント
    function getSelSchKindCnt($sch_kind, $prg_id) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_PRG_SCHOOLKIND_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_KIND = '{$sch_kind}' ";
        $query .= "     AND PROGRAMID   = '{$prg_id}' ";

        return $query;
    }

/*****右画面*****/
    //校種取得
    function getSchKind() {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //MENU_MSTにあるPRG取得
    function getPrgId() {
        $query  = " SELECT ";
        $query .= "     PROGRAMID AS LABEL, ";
        $query .= "     PROGRAMID AS VALUE ";
        $query .= " FROM ";
        $query .= "     MENU_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID LIKE 'KNJ%' ";
        $query .= " GROUP BY ";
        $query .= "     PROGRAMID ";
        $query .= " ORDER BY ";
        $query .= "     PROGRAMID ";

        return $query;
    }

    //１レコード取得
    function getRow($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND AS SCHKIND, ";
        $query .= "     PROGRAMID ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_PRG_SCHOOLKIND_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_KIND = '".$model->schkind."' ";
        $query .= "     AND PROGRAMID   = '".$model->prgId."' ";

        return $query;
    }

    //リストtoリスト
    //左リスト(更新対象)
    function selectLeftList($model) {
        $query  = " SELECT ";
        $query .= "     A1.SELECT_SCHOOL_KIND AS VALUE, ";
        $query .= "     V1.ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST V1 ";
        $query .= " LEFT JOIN ADMIN_CONTROL_PRG_SCHOOLKIND_MST A1  ";
        $query .= "                     ON A1.SELECT_SCHOOL_KIND = V1.NAME1 ";
        $query .= "                    AND A1.PROGRAMID = '".$model->field["PRGIDLIST"]."' ";
        $query .= " WHERE ";
        $query .= "         V1.YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND V1.NAMECD1     = 'A023' ";
        $query .= "     AND A1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     V1.NAME1 ";


        return $query;
    }

    //右リスト
    function selectRightList($model, $schKindArr) {
        $query  = " SELECT ";
        $query .= "     V1.NAME1 AS VALUE, ";
        $query .= "     V1.ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST V1 ";
        $query .= " LEFT JOIN ADMIN_CONTROL_PRG_SCHOOLKIND_MST A1  ";
        $query .= "                     ON A1.SELECT_SCHOOL_KIND = V1.NAME1 ";
        $query .= "                    AND A1.PROGRAMID = '".$model->field["PRGIDLIST"]."' ";
        $query .= " WHERE ";
        $query .= "         V1.YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND V1.NAMECD1 = 'A023' ";
        if ($schKindArr) {
            $query .= "     AND V1.NAME1 NOT IN ('".implode("','",$schKindArr)."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     V1.NAME1 ";

        return $query;
    }

    // UPDATE
    function &getUpdateQuery($model) {
        $selectdata = str_replace(",","','",$model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ADMIN_CONTROL_PRG_SCHOOLKIND_MST ";
        $query .= "  WHERE SCHOOL_KIND        = '".$model->field["SCHKIND"]."' ";
        $query .= "    AND PROGRAMID          = '".$model->field["PRGIDLIST"]."' ";

        $db->query($query);

        if($model->selectdata != "") {
            $selectdata = explode(",", $model->selectdata);

            $query  = " INSERT INTO ADMIN_CONTROL_PRG_SCHOOLKIND_MST ";
            $query .= "            (SCHOOL_KIND, ";
            $query .= "             PROGRAMID, ";
            $query .= "             SELECT_SCHOOL_KIND, ";
            $query .= "             REGISTERCD, ";
            $query .= "             UPDATED ) ";
            $query .= " VALUES ";

            for ($i=0; $i < get_count($selectdata); $i++) {
                $sp = ($i == get_count($selectdata)-1)? "" : "," ;

                $query .= "      ('".$model->field["SCHKIND"]."', ";
                $query .= "       '".$model->field["PRGIDLIST"]."', ";
                $query .= "       '".$selectdata[$i]."', ";
                $query .= "       '".STAFFCD."', ";
                $query .= "       sysdate() ) " .$sp;
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    // DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ADMIN_CONTROL_PRG_SCHOOLKIND_MST ";
        $query .= "  WHERE SCHOOL_KIND        = '".$model->field["SCHKIND"]."' ";
        $query .= "    AND PROGRAMID          = '".$model->field["PRGIDLIST"]."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
