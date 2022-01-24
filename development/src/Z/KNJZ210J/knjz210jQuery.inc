<?php

require_once('for_php7.php');

class knjz210jQuery extends Query {
    //観点情報を取得(固定)
    function getTitleData() {
        $query  = " SELECT ";
        $query .= "     '01' AS NAMECD1, ";
        $query .= "     '①' AS NAME1 ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '02' AS NAMECD1, ";
        $query .= "     '②' AS NAME1 ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '03' AS NAMECD1, ";
        $query .= "     '③' AS NAME1 ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '04' AS NAMECD1, ";
        $query .= "     '④' AS NAME1 ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '05' AS NAMECD1, ";
        $query .= "     '⑤' AS NAME1 ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '06' AS NAMECD1, ";
        $query .= "     '⑥' AS NAME1 ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD1 ";
        return $query;
    }

    //チェック用データ数
    function getJviewCnt($year) {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEW_CNT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    //各データ取得
    function getJviewCntMst($model, $setScore) {
        
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     JVIEW_CNT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCORE = {$setScore} ";

        return $query;
    }

    //削除
    function getDeleteQuery() {

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     JVIEW_CNT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //更新
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjz210jQuery::getDeleteQuery();
        $db->query($query);

        $scoreCnt=1;
        foreach ($model->updData as $updKey => $updVal) {
            $data["YEAR"][TEXT]        = CTRL_YEAR;
            $data["SCORE"][NUMBER]     = $updVal["SCORE"];
            $data["JVIEW1"][TEXT]      = $updVal["JVIEW1"];
            $data["JVIEW2"][TEXT]      = $updVal["JVIEW2"];
            $data["JVIEW3"][TEXT]      = $updVal["JVIEW3"];
            $data["JVIEW4"][TEXT]      = $updVal["JVIEW4"];
            $data["JVIEW5"][TEXT]      = $updVal["JVIEW5"];
            $data["JVIEW6"][TEXT]      = $updVal["JVIEW6"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "JVIEW_CNT_MST");
            $db->query($query);
            $scoreCnt++;
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //コピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjz210jQuery::getDeleteQuery();
        $db->query($query);

        $query  = " INSERT INTO ";
        $query .= "     JVIEW_CNT_MST ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."', ";
        $query .= "     SCORE, ";
        $query .= "     JVIEW1, ";
        $query .= "     JVIEW2, ";
        $query .= "     JVIEW3, ";
        $query .= "     JVIEW4, ";
        $query .= "     JVIEW5, ";
        $query .= "     JVIEW6, ";
        $query .= "     JVIEW7, ";
        $query .= "     JVIEW8, ";
        $query .= "     JVIEW9, ";
        $query .= "     JVIEW10, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     JVIEW_CNT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR - 1)."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
