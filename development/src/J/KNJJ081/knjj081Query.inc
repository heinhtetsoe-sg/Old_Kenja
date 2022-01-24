<?php

require_once('for_php7.php');

class knjj081Query extends Query {

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
        $query  = "select distinct YEAR from COMMITTEE_ADVISER_DAT ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
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

    //委員会情報を取得
    function getCommiMst($model, $year) {
        $query  = " SELECT ";
        $query .= "     T3.COMMITTEE_FLG || '-' || T3.COMMITTEECD || ' ' || T3.COMMITTEENAME AS LABEL, ";
        $query .= "     T3.COMMITTEE_FLG || T3.COMMITTEECD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COMMITTEE_YDAT T2, ";
        $query .= "     COMMITTEE_MST T3 ";
        $query .= " WHERE ";
        $query .= "         T2.YEAR = '{$year}' ";
        $query .= "     AND T2.COMMITTEE_FLG = T3.COMMITTEE_FLG ";
        $query .= "     AND T2.COMMITTEECD = T3.COMMITTEECD ";
        $query .= "     AND T2.COMMITTEE_FLG = '1' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T2.SCHOOLCD     = T3.SCHOOLCD ";
            $query .= " AND T2.SCHOOL_KIND  = T3.SCHOOL_KIND ";
            $query .= " AND T2.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T2.SCHOOL_KIND  = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOLCD     = T3.SCHOOLCD ";
            $query .= " AND T2.SCHOOL_KIND  = T3.SCHOOL_KIND ";
            $query .= " AND T2.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //グループに所属する職員
    function selectQuery($model) {
        $year = $model->year;                      //処理年度
        $query  = " select ";
        $query .= "     s.STAFFCD, ";
        $query .= "     s.STAFFNAME_SHOW ";
        $query .= " from ";
        $query .= "     COMMITTEE_ADVISER_DAT u, ";
        $query .= "     v_staff_mst s ";
        $query .= " where ";
        $query .= "     s.STAFFCD = u.ADVISER ";
        $query .= " and u.COMMITTEE_FLG || u.COMMITTEECD = '".$model->field["COMMITTEECD"] ."'";
        $query .= " and s.YEAR = '".$year."' ";
        $query .= " and u.YEAR = s.YEAR ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND u.SCHOOLCD      = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND u.SCHOOL_KIND   = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND u.SCHOOLCD      = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND u.SCHOOL_KIND   = '".SCHOOLKIND."' ";
        }
        $query .= " order by ";
        $query .= "     u.ADVISER";
        return $query;
    }

    //グループに所属しない職員
    function selectNoGroupQuery($isGroup_array,$model) {
        $year = $model->year;                      //処理年度
        $query  = " select ";
        $query .= "     s.STAFFCD, ";
        $query .= "     s.STAFFNAME_SHOW ";
        $query .= " from ";
        $query .= "     v_staff_mst s ";
        $query .= " where ";
        $query .= "     not exists( select 'X' ";
        $query .= "                 from ";
        $query .= "                     COMMITTEE_ADVISER_DAT d ";
        $query .= "                 where ";
        $query .= "                     year = '".$year."' ";
        $query .= "                 and d.COMMITTEE_FLG || d.COMMITTEECD = '".$model->field["COMMITTEECD"] ."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                 AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "                 AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                 AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "                 AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "                 and d.ADVISER = s.staffcd) ";
        $query .= " and s.YEAR = '".$year."' ";
        $query .= " order by ";
        $query .= "     s.STAFFCD ";

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
        
        $query  = " DELETE from ";
        $query .= "     COMMITTEE_ADVISER_DAT ";
        $query .= " where ";
        $query .= "     COMMITTEE_FLG || COMMITTEECD = '".$model->field["COMMITTEECD"] ."'";
        $query .= " and YEAR = '".$year."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        //利用者グループデータの登録
        if ($array[0] !="") {
            for ($i = 0; $i < get_count($array); $i++){
                $data = array();
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]      = $model->field["SCHKIND"];
                } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
                }
                $data["YEAR"][TEXT]             = $year;
                $data["COMMITTEE_FLG"][TEXT]    = substr($model->field["COMMITTEECD"],0,1);
                $data["COMMITTEECD"][TEXT]      = substr($model->field["COMMITTEECD"],1);
                $data["ADVISER"][TEXT]          = $array[$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $db->query(Query::insertSQL($data, "COMMITTEE_ADVISER_DAT"));
            }
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
