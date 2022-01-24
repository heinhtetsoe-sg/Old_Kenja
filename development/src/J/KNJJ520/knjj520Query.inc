<?php

require_once('for_php7.php');

class knjj520Query extends Query {

    //日付取得
    function getScoreDate($model, $div)
    {
        $query  = " SELECT ";
        if($div== "date"){
            $query .= "     MIN(DATE) AS DATE ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     SPORTS_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //データ取得
    function getSportsScoreDat($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ITEMCD, ";
        $query .= "     T1.ITEMNAME, ";
        $query .= "     T1.UNIT, ";
        $query .= "     T1.SHOWORDER, ";
        $query .= "     T2.RECORD ";
        $query .= " FROM ";
        $query .= "     SPORTS_ITEM_MST T1 ";
        $query .= "     LEFT JOIN SPORTS_SCORE_DAT T2 ON ";
        $query .= "         T1.ITEMCD   = T2.ITEMCD AND ";
        $query .= "         T2.YEAR     = '".$model->exp_year."' AND ";
        $query .= "         T2.SCHREGNO = '".$model->schregno."' ";
        $query .= " WHERE ";
        $query .= "     T1.ITEMCD < '900' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ITEMCD ";

        return $query;
    }

    //総合判定取得
    function getSportsScoreDat2($model)
    {
        $query  = " SELECT ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     SPORTS_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     ITEMCD      = '999' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $date = ($model->field["DATE"]) ? str_replace("/", "-", $model->field["DATE"]) : "";

        foreach ($model->data["ITEMCD"] as $code => $col) {
            $data["YEAR"][TEXT]             = $model->exp_year;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["ITEMCD"][TEXT]           = $col;
            $data["DATE"][TEXT]             = $date;
            $data["RECORD"][NUMBER]         = $model->fields["RECORD-".$col];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "SPORTS_SCORE_DAT"));
        }

        $db->commit();

        //総合判定
        $data["YEAR"][TEXT]             = $model->exp_year;
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["ITEMCD"][TEXT]           = '999';
        $data["DATE"][TEXT]             = $date;
        $data["VALUE"][TEXT]            = $model->field["VALUE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $db->query(Query::insertSQL($data, "SPORTS_SCORE_DAT"));
        $db->commit();

        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SPORTS_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return ;
    }
}
?>
