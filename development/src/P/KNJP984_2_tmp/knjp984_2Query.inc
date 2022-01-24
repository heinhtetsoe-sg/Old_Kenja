<?php

require_once('for_php7.php');

class knjp984_2Query extends Query {

    //グループに所属する会計科目
    function selectQuery($model)
    {
        $query .= " SELECT ";
        $query .= "     S.LEVY_L_CD, ";
        $query .= "     S.LEVY_L_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_DAT U, ";
        $query .= "     LEVY_L_MST S ";
        $query .= " WHERE ";
        $query .= "     S.LEVY_L_CD = U.LEVY_L_CD  ";
        $query .= " AND U.LEVY_GROUP_CD = '".$model->groupcd."' ";
        $query .= " AND S.YEAR = '".$model->year."'  ";
        $query .= " AND U.YEAR = S.YEAR ";
        $query .= " ORDER BY ";
        $query .= "     U.LEVY_L_CD ";

        return $query;
    }
    //グループ一覧取得
    function selectGroupQuery()
    {
        $query  = " SELECT ";
        $query .= "     LEVY_GROUP_CD, ";
        $query .= "     LEVY_GROUP_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_MST ";
        $query .= " ORDER BY ";
        $query .= "     LEVY_GROUP_CD DESC ";
        return $query;
    }
    //グループに所属しない会計科目(グループに所属している会計科目は表示しない)
    function selectNoGroupQuery($isGroup_array,$model)
    {
        $query  = " SELECT ";
        $query .= "     T1.LEVY_L_CD, ";
        $query .= "     T1.LEVY_L_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_L_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= " AND NOT EXISTS(SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     LEVY_GROUP_DAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T2.YEAR = T1.YEAR ";
        $query .= "                 AND T2.LEVY_L_CD = T1.LEVY_L_CD  ";
        //$query .= "                 AND T2.LEVY_GROUP_CD = '".$model->groupcd."' ";
        $query .= "                 ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.LEVY_L_CD ";

        return $query;
    }

    //コンボボックス内年度取得
    function selectYearQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     LEVY_GROUP_CD = '".$model->groupcd."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC ";
        
        return $query;
    }
    
    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {        
        //グループコード配列に格納
        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query  = " DELETE FROM ";
        $query .= "     LEVY_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     LEVY_GROUP_CD = '" .$model->groupcd ."' ";
        $query .= " AND YEAR = '".$model->year."'";
        $db->query($query);

        //利用者グループデータの登録
        if ($array[0] !="") {
            $query  = " INSERT INTO LEVY_GROUP_DAT( ";
            $query .= " YEAR, ";
            $query .= " LEVY_GROUP_CD, ";
            $query .= " LEVY_L_CD, ";
            $query .= " REGISTERCD, ";
            $query .= " UPDATED ";
            $query .= " ) ";
            $query .= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->year ."'";
                $query.= ",'" .$model->groupcd ."'";
                $query.= ",'" .$array[$i] ."'";
                $query.= ",'" .LEVY_L_CD ."'";
                $query.= ",'" .$updated ."')" .$sp;
            }
            $db->query($query);
        }
        $db->commit(); // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
