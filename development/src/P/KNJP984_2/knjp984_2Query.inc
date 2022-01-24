<?php

require_once('for_php7.php');

class knjp984_2Query extends Query {

    //グループ一覧(コンボ)取得
    function selectGroupQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     LEVY_GROUP_CD AS VALUE, ";
        $query .= "     LEVY_GROUP_CD || ':' ||LEVY_GROUP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //グループに所属する会計科目
    function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     S.LEVY_L_CD, ";
        $query .= "     S.LEVY_L_NAME ";
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_DAT U, ";
        $query .= "     LEVY_L_MST S ";
        $query .= " WHERE ";
        $query .= "     U.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND U.SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND S.SCHOOLCD = U.SCHOOLCD  ";
        $query .= "     AND S.SCHOOL_KIND = U.SCHOOL_KIND  ";
        $query .= "     AND S.LEVY_L_CD = U.LEVY_L_CD  ";
        $query .= "     AND U.LEVY_GROUP_CD = '".$model->field["LEVY_GROUP_CD"]."' ";
        $query .= "     AND S.YEAR = '".$model->field["YEAR"]."'  ";
        $query .= "     AND U.YEAR = S.YEAR ";
        $query .= " ORDER BY ";
        $query .= "     U.LEVY_L_CD ";

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
        $query .= "     T1.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "     AND T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND NOT EXISTS(SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         LEVY_GROUP_DAT T2 ";
        $query .= "                     WHERE ";
        $query .= "                         T2.SCHOOLCD = T1.SCHOOLCD ";
        $query .= "                         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                         AND T2.YEAR = T1.YEAR ";
        $query .= "                         AND T2.LEVY_L_CD = T1.LEVY_L_CD  ";
        $query .= "                     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.LEVY_L_CD ";

        return $query;
    }

    //コンボボックス内年度取得
    function selectYearQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     '".$model->year."' AS LABEL, ";
        $query .= "     '".$model->year."' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= "     AND LEVY_GROUP_CD = '".$model->field["LEVY_GROUP_CD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }
    
    //選択した科目コードがグループに所属かをカウント
    function getCountCheckQuery($model)
    {
        //グループコード配列に格納
        $levyLcdArray = array();
        $levyLcdArray = explode (",", $model->selectdata);
    
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS COUNT ";
        $query .= " FROM ";
        $query .= "     LEVY_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= "     AND LEVY_GROUP_CD <> '".$model->field["LEVY_GROUP_CD"]."' ";
        //選択した生徒のみをチェック
        $query .= " AND LEVY_L_CD IN ( ";
        $count = 0;
        $conma = "";
        foreach ($levyLcdArray as $levy_l_cd) {
            $query .= " $conma'".$levy_l_cd."' ";
            $conma = ",";
            $count++;
        }
        $query .= " ) ";

        return $query;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {        
        //グループコード配列に格納
        $levyLcdArray = array();
        $levyLcdArray = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time()); //更新日付
        
        $query  = " DELETE FROM ";
        $query .= "     LEVY_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD          = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND   = '".$model->schoolKind."'";
        $query .= "     AND LEVY_GROUP_CD = '" .$model->field["LEVY_GROUP_CD"] ."' ";
        $query .= "     AND YEAR = '".$model->field["YEAR"]."'";
        $db->query($query);

        //利用者グループデータの登録
        if ($levyLcdArray[0] !="") {
            $query  = " INSERT INTO LEVY_GROUP_DAT( ";
            $query .= " SCHOOLCD, ";
            $query .= " SCHOOL_KIND, ";
            $query .= " YEAR, ";
            $query .= " LEVY_GROUP_CD, ";
            $query .= " LEVY_L_CD, ";
            $query .= " REGISTERCD, ";
            $query .= " UPDATED ";
            $query .= " ) ";
            $query .= "VALUES";
            for ($i = 0; $i < get_count($levyLcdArray); $i++){
                $sp = ",";
                if ($i == get_count($levyLcdArray)-1){
                    $sp = "";
                }
                $query.= "('" .(sprintf("%012d", SCHOOLCD)) ."'";
                $query.= ",'" .$model->schoolKind ."'";
                $query.= ",'" .$model->field["YEAR"] ."'";
                $query.= ",'" .$model->field["LEVY_GROUP_CD"] ."'";
                $query.= ",'" .$levyLcdArray[$i] ."'";
                $query.= ",'" .STAFFCD ."'";
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
