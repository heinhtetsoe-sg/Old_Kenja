<?php

require_once('for_php7.php');

class knjj020Query extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
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
    
    //年度一覧取得
    function selectYearQuery()
    {
        $query  = "select distinct YEAR from CLUB_ADVISER_DAT ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " where ";
            $query .= "      SCHOOLCD    = '".SCHOOLCD."'";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."'";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " where ";
            $query .= "      SCHOOLCD    = '".SCHOOLCD."'";
            $query .= "  AND SCHOOL_KIND = '".$model->schKind."'";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " where ";
            $query .= "      SCHOOLCD    = '".SCHOOLCD."'";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."'";
        }
        $query .= " order by year desc";
        return $query;
    }

    //部・クラブ一覧取得
    function selectClub($model, $year) {
        $query  = " SELECT ";
        $query .= "     D.CLUBCD AS VALUE, ";
        $query .= "     D.CLUBCD || '　' || M.CLUBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_YDAT D ";
        $query .= " INNER JOIN ";
        $query .= "     CLUB_MST M ON D.CLUBCD = M.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     D.YEAR = '" .$year ."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "  AND D.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND D.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "  AND M.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND M.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "  AND D.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND D.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            $query .= "  AND M.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND M.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "  AND D.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND D.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "  AND M.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND M.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //部クラブ顧問一覧取得
    function selectQuery($model) {
        $year = $model->year;                      //処理年度
        $query  = " SELECT ";
        $query .= "     S.STAFFCD, ";
        $query .= "     S.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     CLUB_ADVISER_DAT U, ";
        $query .= "     V_STAFF_MST S ";
        $query .= " WHERE  ";
        $query .= "         S.STAFFCD = U.ADVISER ";
        $query .= "     AND U.CLUBCD  = '".$model->field["CLUBCD"] ."'";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "     AND U.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND U.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND U.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND U.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND U.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND U.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND S.YEAR    = '".$year."' ";
        $query .= "     AND U.YEAR    = S.YEAR ";
        $query .= " ORDER BY ";
        $query .= "     U.ADVISER ";

        return $query;
    }

    //グループに所属しない職員（職員一覧）
    function selectNoGroupQuery($model, $isGroup_array,$model2) {
        $year = $model->year;                      //処理年度
        $query  = " SELECT ";
        $query .= "     S.STAFFCD, ";
        $query .= "     S.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST S ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     CLUB_ADVISER_DAT D ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".$year."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "                     AND D.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "                     AND D.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "  AND D.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND D.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     AND D.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "                     AND D.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "                     AND D.CLUBCD  = '".$model->field["CLUBCD"]."' ";
        $query .= "                     AND D.ADVISER = S.STAFFCD ";
        $query .= "                 ) ";
        $query .= "     AND S.YEAR    = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     S.STAFFCD ";

        return $query;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery($model) {
        //グループコード配列に格納
        $array = explode (",", $model->selectdata);

        $year = $model->year;                      //処理年度
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付

        $query = "DELETE from CLUB_ADVISER_DAT where CLUBCD = '" .$model->field["CLUBCD"] ."'";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "  AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "  AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query.= "and YEAR = '".$year."'";
        $db->query($query);

        //利用者グループデータの登録
        if ($array[0] !="") {
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $query = "INSERT INTO CLUB_ADVISER_DAT(YEAR,SCHOOLCD,SCHOOL_KIND,CLUBCD,ADVISER,REGISTERCD,UPDATED)";
            } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
                $query = "INSERT INTO CLUB_ADVISER_DAT(YEAR,SCHOOLCD,SCHOOL_KIND,CLUBCD,ADVISER,REGISTERCD,UPDATED)";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query = "INSERT INTO CLUB_ADVISER_DAT(YEAR,SCHOOLCD,SCHOOL_KIND,CLUBCD,ADVISER,REGISTERCD,UPDATED)";
            } else {
                $query = "INSERT INTO CLUB_ADVISER_DAT(YEAR,CLUBCD,ADVISER,REGISTERCD,UPDATED)";
            }
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++) {
                $sp = ",";
                if ($i == get_count($array)-1) {
                    $sp = "";
                }
                $query.= "('" .$year ."'";                                    //処理年度
                if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                    $query.= ",'" .SCHOOLCD . "'";
                    $query.= ",'" .SCHOOLKIND. "'";
                } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
                    $query.= ",'" .SCHOOLCD . "'";
                    $query.= ",'" .$model->field["SCHKIND"]. "'";
                } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query.= ",'" .SCHOOLCD . "'";
                    $query.= ",'" .SCHOOLKIND. "'";
                }
                $query.= ",'" .$model->field["CLUBCD"] ."'";                  //部クラブ
                $query.= ",'" .$array[$i] ."'";                               //顧問
                $query.= ",'" .STAFFCD ."'";
                $query.= ",'" .$updated ."')" .$sp;                           //更新日付

            }
            $db->query($query);
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }

    function cntClubAdviserDatNextYear($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CLUB_ADVISER_DAT ";
        $query .= " WHERE  ";
        $query .= "         YEAR    = '".((int)$model->year + 1)."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $ret_val = $db->getOne($query);

        Query::dbCheckIn($db);

        return $ret_val;
    }

    //次年度へのコピー処理（部クラブコンボで「－全て－」を選択時）
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " INSERT INTO CLUB_ADVISER_DAT ";
        $query .= " SELECT ";

        $query .= "     CLUB.YEAR + 1 ";
        $query .= "     , CLUB.CLUBCD ";
        $query .= "     , CLUB.ADVISER ";
        $query .= "     , CLUB.CLUBDIV ";
        if($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     , CLUB.SCHOOLCD ";
            $query .= "     , CLUB.SCHOOL_KIND ";
        }
        $query .= "     , '".STAFFCD."' ";
        $query .= "     , sysdate() ";
        $query .= " FROM ";
        $query .= "     CLUB_ADVISER_DAT CLUB ";
        $query .= "     INNER JOIN V_STAFF_MST STF ";
        $query .= "         ON STF.YEAR = CLUB.YEAR + 1 ";
        $query .= "         AND STF.STAFFCD = CLUB.ADVISER ";
        $query .= " WHERE ";
        $query .= "     CLUB.YEAR = '".$model->year."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return true;
    }
}
?>
