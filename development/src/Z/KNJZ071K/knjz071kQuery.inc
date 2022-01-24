<?php
class knjz071kQuery extends Query {

    //課程年度一覧取得
    function selectSmstQuery($model,$sqlflg = "")
    {
        $tblname = "";
        if ($sqlflg == "OK") {
            $tblname = "V_EXPENSE_S_MST T1";
        } else {
            $tblname = "EXPENSE_S_MST T1";
        }

        $query  = "SELECT T1.EXPENSE_S_CD, T1.EXPENSE_S_NAME ";
        $query .= " FROM ".$tblname." ";
        $query .= " WHERE T1.YEAR = '".CTRL_YEAR."' ";
        if ($sqlflg == "OK") {
            $query .= "       AND T1.EXPENSE_M_CD = '".$model->expense_m_cd."' ";
        } else {
            $query .= "       AND NOT EXISTS (SELECT 'X' ";
            $query .= "                       FROM V_EXPENSE_S_MST T2 ";
            $query .= "                       WHERE T2.YEAR = '".CTRL_YEAR."'";
            $query .= "                             AND T2.EXPENSE_M_CD = '".$model->expense_m_cd."' ";
            $query .= "                             AND T2.EXPENSE_S_CD = T1.EXPENSE_S_CD) ";
        }
        $query .= " ORDER BY T1.EXPENSE_S_CD";

        return $query;
    }

    //中分類マスタ取得
    function selectMmstQuery()
    {
        $query  = "SELECT EXPENSE_M_CD,EXPENSE_M_NAME FROM EXPENSE_M_MST";
        $query .= " WHERE YEAR = '" .CTRL_YEAR. "' AND EXPENSE_S_EXIST_FLG = '1' ORDER BY EXPENSE_M_CD";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model)
    {
        //課程コード配列に格納
        $array = explode (",", $model->selectdata);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "DELETE FROM EXPENSE_MS_MST WHERE YEAR = '" .CTRL_YEAR."' ";
        $query .= " AND EXPENSE_M_CD = '".$model->expense_m_cd."'";
        $db->query($query);

        $sp = "";
        $query = "INSERT INTO EXPENSE_MS_MST";
        $query.= " VALUES";
        for ($i = 0; $i < count($array); $i++){
            $query.= $sp;
            $query.= "('".CTRL_YEAR."'";             //年度
            $query.= ",'".$model->expense_m_cd."'";  //中分類コード
            $query.= ",'".$array[$i] ."'";           //小分類コード
            $query.= ",'".STAFFCD."'";              //STAFFCD
            $query.= ",sysdate())";                  //更新日付
            $sp = ",";
        }
        if ($array[0] != "") {
            $db->query($query);
        }
        $db->commit(); // ﾄﾗﾝｻﾞｸｼｮﾝをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
