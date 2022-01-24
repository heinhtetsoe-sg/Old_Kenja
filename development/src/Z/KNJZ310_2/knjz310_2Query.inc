<?php

require_once('for_php7.php');

class knjz310_2Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //グループに所属する職員
    function selectQuery($model)
    {
        $year = $model->year;                      //処理年度 NO001
        $query =  "select s.STAFFCD,s.STAFFNAME_SHOW from usergroup_dat u,v_staff_mst s ";
        $query .= "where s.STAFFCD = u.STAFFCD ";
        $query .= "and u.GROUPCD = '".$model->GROUPCD."'";
        $query .= "and s.YEAR = '".$year."' ";
        $query .= "and u.YEAR = s.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND u.SCHOOLCD      = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND u.SCHOOL_KIND   = '".SCHOOLKIND."' ";
        }
        $query .= "order by u.STAFFCD";

        return $query;
    }
    //グループ一覧取得
    function selectGroupQuery($model)
    {
        $query  = " select GROUPCD,GROUPNAME from usergroup_mst ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " order by GROUPCD desc ";
        return $query;
    }
    //グループに所属しない職員
    function selectNoGroupQuery($isGroup_array,$model)
    {
        $year = $model->year;                      //処理年度 NO001
        $query  = "select s.STAFFCD,s.STAFFNAME_SHOW from v_staff_mst s,user_mst u ";
        $query .= "where s.STAFFCD = u.STAFFCD ";
        $query .= "and not exists(select 'X' ";
        $query .= "                 from USERGROUP_DAT d ";
        $query .= "                 where year = '".$year."'";
        $query .= "                 and d.groupcd = '".$model->GROUPCD."' ";
        $query .= "                 and d.staffcd = u.staffcd ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                 AND d.SCHOOLCD      = u.SCHOOLCD ";
            $query .= "                 AND d.SCHOOL_KIND   = u.SCHOOL_KIND) ";
            $query .= " AND u.SCHOOLCD      = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND u.SCHOOL_KIND   = '".SCHOOLKIND."' ";
        } else {
            $query .= "               ) ";
        }
        $query .= "and s.YEAR = '".$year."' order by u.STAFFCD ";
        return $query;
    }

    //コンボボックス内年度取得 NO001
    function selectYearQuery($model)
    {
        $query  = " SELECT DISTINCT YEAR FROM USERGROUP_DAT WHERE GROUPCD = '".$model->GROUPCD."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY YEAR DESC";
        return $query;
    }

    //職員情報詳細
    function getStfDetailFields($model, $staffcd) {
        $query  = "  SELECT ";
        $query .= "    FIELD1 ";
        $query .= "    , FIELD2 ";
        $query .= "    , FIELD3 ";
        $query .= "    , FIELD4 ";
        $query .= "    , FIELD5 ";
        $query .= "  FROM ";
        $query .= "    STAFF_DETAIL_MST ";
        $query .= "  WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND STAFFCD = '".$staffcd."' ";
        $query .= "    AND STAFF_SEQ = '010' ";

        return $query;
    }

    //校種略称取得
    function getSchKindAbbv($model, $name1) {
        $query  = "  SELECT ";
        $query .= "    ABBV1 ";
        $query .= "  FROM ";
        $query .= "    NAME_MST ";
        $query .= "  WHERE ";
        $query .= "    NAMECD1 = 'A023' ";
        $query .= "    AND NAME1 = '".$name1."' ";

        return $query;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {        
        //グループコード配列に格納
        $array = explode (",", $model->selectdata);
        
//        $year = CTRL_YEAR;                      //処理年度
        $year = $model->year;                     //処理年度 NO001
        $Fld = $school_kind = "";
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query = "DELETE from usergroup_dat where GROUPCD = '" .$model->GROUPCD ."'";
        $query.= "and YEAR = '".$year."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";

            //追加用
            $Fld = "SCHOOLCD,SCHOOL_KIND,";
            $school_kind = ",'".sprintf("%012d", SCHOOLCD)."','".SCHOOLKIND."'";
        }
        $db->query($query);

        //利用者グループデータの登録
        if ($array[0] !="") {
            $query = "INSERT INTO usergroup_dat(YEAR,".$Fld."GROUPCD,STAFFCD,REGISTERCD,UPDATED)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$year ."'";                                    //処理年度
                $query.= $school_kind;                                        //学校コード、校種
                $query.= ",'" .$model->GROUPCD ."'";                          //グループ
                $query.= ",'" .$array[$i] ."'";                               //スタッフコード
                $query.= ",'" .STAFFCD ."'";
                $query.= ",'" .$updated ."')" .$sp;                           //更新日付

            }
            $db->query($query);
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
