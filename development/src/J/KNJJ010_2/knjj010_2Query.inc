<?php

require_once('for_php7.php');

class knjj010_2Query extends Query {

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

    //１レコード取得
    function getRow($model, $cd)
    {
        $query  = " SELECT * FROM club_mst ".
                  " WHERE CLUBCD = '" .$cd ."'";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //登録種目データ取得
    function getClubItemDat($model, $clubcd)
    {
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query  = " SELECT ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.ITEMCD || ' ' || T2.ITEMNAME AS LABEL, ";
            $query .= "     T1.SCHOOL_KIND || '-' || T1.ITEMCD AS VALUE, ";
            $query .= "     T2.ITEMNAME ";
            $query .= " FROM ";
            $query .= "     CLUB_ITEM_DAT T1 ";
            $query .= "     LEFT JOIN CLUB_ITEM_MST T2 ON T1.ITEMCD = T2.ITEMCD ";
            $query .= "       AND T2.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= "       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= " WHERE ";
            $query .= "     T1.CLUBCD = '".$clubcd."' ";
            $query .= "     AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            if (is_array($model->selectItem) && get_count($model->selectItem) > 0 && $model->selectItem[0] != "") {
                $query .= " AND T1.SCHOOL_KIND || '-' || T1.ITEMCD NOT IN ( '".implode("','", $model->selectItem)."') ";
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     T3.SCHOOL_KIND || '-' || T3.ITEMCD || ' ' || T3.ITEMNAME AS LABEL, ";
                $query .= "     T3.SCHOOL_KIND || '-' || T3.ITEMCD AS VALUE, ";
                $query .= "     T3.ITEMNAME ";
                $query .= " FROM ";
                $query .= "     CLUB_ITEM_MST T3 ";
                $query .= " WHERE ";
                $query .= "     T3.SCHOOL_KIND || '-' || T3.ITEMCD IN ( '".implode("','", $model->selectItem)."') ";
            }
        } else {
            $query  = " SELECT ";
            $query .= "     T1.ITEMCD || ' ' || T2.ITEMNAME AS LABEL, ";
            $query .= "     T1.ITEMCD AS VALUE, ";
            $query .= "     T2.ITEMNAME ";
            $query .= " FROM ";
            $query .= "     CLUB_ITEM_DAT T1 ";
            $query .= "     LEFT JOIN CLUB_ITEM_MST T2 ON T1.ITEMCD = T2.ITEMCD ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "  AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "  AND T2.SCHOOL_KIND = '".$model->schKind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "  AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "  AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.CLUBCD = '".$clubcd."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "  AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "  AND T1.SCHOOL_KIND = '".$model->schKind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "  AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "  AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //種目マスタ取得
    function getClubItemMst($model, $clubcd)
    {
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query  = " SELECT ";
            $query .= "     SCHOOL_KIND || '-' || ITEMCD || ' ' || ITEMNAME AS LABEL, ";
            $query .= "     SCHOOL_KIND || '-' || ITEMCD AS VALUE ";
            $query .= " FROM ";
            $query .= "     CLUB_ITEM_MST ";
            $query .= " WHERE ";
            $query .= "    ITEMCD NOT IN ( SELECT ";
            $query .= "                             ITEMCD ";
            $query .= "                         FROM ";
            $query .= "                             CLUB_ITEM_DAT  ";
            $query .= "                         WHERE ";
            $query .= "                             CLUBCD = '".$clubcd."' ";
            $query .= "                             AND SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "                             AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                             ) ";
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind_CI."' ";
            if (is_array($model->selectItem) && get_count($model->selectItem) > 0 && $model->selectItem[0] != "") {
                $query .= " AND SCHOOL_KIND || '-' || ITEMCD NOT IN ( '".implode("','", $model->selectItem)."') ";
            }
        } else {
            $query  = " SELECT ";
            $query .= "     ITEMCD || ' ' || ITEMNAME AS LABEL, ";
            $query .= "     ITEMCD AS VALUE ";
            $query .= " FROM ";
            $query .= "     CLUB_ITEM_MST ";
            $query .= " WHERE ";
            $query .= "     ITEMCD NOT IN ( SELECT ";
            $query .= "                             ITEMCD ";
            $query .= "                         FROM ";
            $query .= "                             CLUB_ITEM_DAT  ";
            $query .= "                         WHERE ";
            $query .= "                             CLUBCD = '".$clubcd."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "                         AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "                         AND SCHOOL_KIND = '".$model->schKind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "                         AND SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "                         AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "                             ) ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "  AND SCHOOL_KIND = '".$model->schKind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度データの確認
    function IsExisting($model, $clubcd)
    {
        $db    = Query::dbCheckOut();
        $query = "select * from club_ydat where clubcd = '".$clubcd."'"; 
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $row   = $db->getRow($query);
        Query::dbCheckIn($db);
        
        if (is_array($row)){
            return true;
        } else {
            return false;
        }       
    }
    
    //--- INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]      = $model->schKind;
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
        }
        $data["CLUBCD"][TEXT]           = $fields["CLUBCD"];
        $data["CLUBNAME"][TEXT]         = $fields["CLUBNAME"];
        $data["SDATE"][TEXT]            = str_replace("/","-",$fields["SDATE"]);
        $data["ACTIVITY_PLACE"][TEXT]   = $fields["ACTIVITY_PLACE"];
        $data["CLUBROOM_ASSIGN"][TEXT]  = $fields["CLUBROOM_ASSIGN"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "club_mst");
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    
    //--- UPDATE 
    function &getUpdateQuery($model, $fields)
    {        
        $db = Query::dbCheckOut();
        
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]      = $model->schKind;
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
            $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
        }
        $data["CLUBCD"][TEXT]           = $fields["CLUBCD"];
        $data["CLUBNAME"][TEXT]         = $fields["CLUBNAME"];
        $data["SDATE"][TEXT]            = str_replace("/","-",$fields["SDATE"]);
        $data["ACTIVITY_PLACE"][TEXT]   = $fields["ACTIVITY_PLACE"];
        $data["CLUBROOM_ASSIGN"][TEXT]  = $fields["CLUBROOM_ASSIGN"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where = " WHERE CLUBCD = '" .$fields["CLUBCD"]."'";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $where .= " AND SCHOOLCD    = '".SCHOOLCD."'";
            $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."'";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $where .= " AND SCHOOLCD    = '".SCHOOLCD."'";
            $where .= " AND SCHOOL_KIND = '".$model->schKind."'";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $where .= " AND SCHOOLCD    = '".SCHOOLCD."'";
            $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."'";
        }
        $query = Query::updateSQL($data, "club_mst", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM club_mst "
                ." WHERE CLUBCD  = '" .$fields["CLUBCD"]."'";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."'";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."'";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."'";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."'";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."'";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."'";
        }

        $db->query($query);

        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query = "DELETE FROM CLUB_DETAIL_DAT "
                    ." WHERE CLUBCD  = '" .$fields["CLUBCD"]."' ";
            $query .= " AND SEQ         = '001' ";
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";

            $db->query($query);
        }

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //--- UPDATE 
    function &getUpdateSubQuery1($model)
    {
        $item = explode(",",implode(",",$model->selectItem));
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = " DELETE FROM CLUB_ITEM_DAT WHERE CLUBCD = '".$model->clubcd."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."'";
            //削除対象データを校種制限しない。全校種が対象。
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."'";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."'";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."'";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."'";
        }
        $db->query($query);

        if($item[0] != ""){
            //追加
            for($i=0; $i < get_count($model->selectItem) ; $i++){
                $itemwk = $item[$i];
                if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                    $itemsubwk = explode("-", $itemwk);
                    $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]      = $itemsubwk[0];
                    $itemwk = $itemsubwk[1];
                } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]      = $model->schKind;
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
                }
                $data["CLUBCD"][TEXT]       = $model->clubcd;        
                $data["ITEMCD"][TEXT]       = $itemwk;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "CLUB_ITEM_DAT");
                $db->query($query);
            }
        }
        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }
}
?>
