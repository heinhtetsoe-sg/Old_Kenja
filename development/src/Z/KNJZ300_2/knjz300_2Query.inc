<?php

require_once('for_php7.php');

class knjz300_2Query extends Query {

    //職員が所属するグループ
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.GROUPCD, ";
        $query .= "     T2.GROUPNAME ";
        $query .= " FROM ";
        $query .= "     USERGROUP_DAT T1, ";
        $query .= "     USERGROUP_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->post_year."' ";
        $query .= " AND T1.STAFFCD      = '".$model->userscd."' ";
        $query .= " AND T1.GROUPCD      = T2.GROUPCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY T1.GROUPCD ";

        return $query;
    }

    function selectNameQuery($cd,$p_year)
    {
        $query =  "SELECT STAFFNAME FROM V_STAFF_MST WHERE STAFFCD ='";
        $query .= $cd;
        $query .= "' AND YEAR ='";
        $query .= $p_year;
        $query .= "'";

        return $query;
    }

    //グループに所属しない職員
    function selectNoGroupQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.GROUPCD, ";
        $query .= "     T2.GROUPNAME ";
        $query .= " FROM ";
        $query .= "     USERGROUP_MST T2 ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT * FROM ";
        $query .= "                     USERGROUP_DAT T1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR         = '".$model->post_year."' ";
        $query .= "                 AND T1.STAFFCD      = '".$model->userscd."' ";
        $query .= "                 AND T1.GROUPCD      = T2.GROUPCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                 AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= "                 AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ) ";
            $query .= " AND T2.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else {
            $query .= "               ) ";
        }
        $query .= " ORDER BY T2.GROUPCD ";

        return $query;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {        
        //グループコード配列に格納
        $array = explode (",", $model->selectdata);
        
        $Fld = $school_kind = "";
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        $query = "DELETE FROM USERGROUP_DAT WHERE YEAR = '".$model->post_year."' AND STAFFCD = '" .$model->userscd ."'";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";

            //追加用
            $Fld = "SCHOOLCD,SCHOOL_KIND,";
            $school_kind = ",'".sprintf("%012d", SCHOOLCD)."','".SCHOOLKIND."'";
        }

         $db->query($query);

        if($array[0] != "")
        {
            //利用者グループデータの登録
            $query = "INSERT INTO USERGROUP_DAT(YEAR,".$Fld."GROUPCD,STAFFCD,REGISTERCD,UPDATED)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('".$model->post_year."'";            //年度
                $query.= $school_kind;                          //学校コード、校種
                $query.= ",'".$array[$i]."'";                   //スタッフコード
                $query.= ",'".$model->userscd."'";              //スタッフコード
                $query.= ",'".STAFFCD."'";                      //登録者コード
                $query.= ",'" .$updated ."')" .$sp;             //更新日付
            }
             $db->query($query);
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);

        return;
    }
}
?>
