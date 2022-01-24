<?php

require_once('for_php7.php');

class knjz417Query extends Query {
    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     T1.JOBTYPE_LCD, ";
        $query .= "     T1.JOBTYPE_LNAME, ";
        $query .= "     L1.JOBTYPE_MCD, ";
        $query .= "     L1.JOBTYPE_MNAME ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_L_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     JOBTYPE_M_MST L1 ON L1.JOBTYPE_LCD = T1.JOBTYPE_LCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.JOBTYPE_LCD, ";
        $query .= "     L1.JOBTYPE_MCD ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getJobtypeMCnt($jobtype_lcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_M_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '{$jobtype_lcd}' ";

        return $query;
    }

    //職業(大中)マスタより1レコード取得(右フレーム用)
    function getJobtype_L_M_Mst($jobtype_lcd, $jobtype_mcd) {
        $query  = " SELECT ";
        $query .= "     T1.JOBTYPE_LCD, ";
        $query .= "     T1.JOBTYPE_LNAME, ";
        $query .= "     T1.JOBTYPE_LNAME_KANA, ";
        $query .= "     L1.JOBTYPE_MCD, ";
        $query .= "     L1.JOBTYPE_MNAME, ";
        $query .= "     L1.JOBTYPE_MNAME_KANA ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_L_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     JOBTYPE_M_MST L1 ON L1.JOBTYPE_LCD = T1.JOBTYPE_LCD ";
        $query .= " WHERE ";
        $query .= "         T1.JOBTYPE_LCD = '{$jobtype_lcd}' ";
        $query .= "     AND L1.JOBTYPE_MCD = '{$jobtype_mcd}' ";

        return $query;
    }

    /**********/
    /* UPDATE */
    /**********/
    //職業(大)更新
    function getUpdateLQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     JOBTYPE_L_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '{$model->field["JOBTYPE_LCD"]}' ";

        $db->query($query);

        $data["JOBTYPE_LCD"][TEXT]        = $model->field["JOBTYPE_LCD"];
        $data["JOBTYPE_LNAME"][TEXT]      = $model->field["JOBTYPE_LNAME"];
        $data["JOBTYPE_LNAME_KANA"][TEXT] = $model->field["JOBTYPE_LNAME_KANA"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, "JOBTYPE_L_MST");
        $db->query($query);

        Query::dbCheckIn($db);
    }
    //職業(大)更新
    function getUpdateMQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     JOBTYPE_M_MST ";
        $query .= " WHERE ";
        $query .= "         JOBTYPE_LCD = '{$model->field["JOBTYPE_LCD"]}' ";
        $query .= "     AND JOBTYPE_MCD = '{$model->field["JOBTYPE_MCD"]}' ";

        $db->query($query);

        $data["JOBTYPE_LCD"][TEXT]        = $model->field["JOBTYPE_LCD"];
        $data["JOBTYPE_MCD"][TEXT]        = $model->field["JOBTYPE_MCD"];
        $data["JOBTYPE_MNAME"][TEXT]      = $model->field["JOBTYPE_MNAME"];
        $data["JOBTYPE_MNAME_KANA"][TEXT] = $model->field["JOBTYPE_MNAME_KANA"];
        $data["REGISTERCD"][TEXT]         = STAFFCD;
        $data["UPDATED"][NUMBER]          = "sysdate()";

        $query = Query::insertSQL($data, "JOBTYPE_M_MST");
        $db->query($query);

        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        //JOBTYPE_M_MSTは無条件に削除
        $query  = " DELETE FROM ";
        $query .= "     JOBTYPE_M_MST ";
        $query .= " WHERE ";
        $query .= "         JOBTYPE_LCD = '{$model->field["JOBTYPE_LCD"]}' ";
        $query .= "     AND JOBTYPE_MCD = '{$model->field["JOBTYPE_MCD"]}' ";

        $db->query($query);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_M_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '{$model->field["JOBTYPE_LCD"]}' ";

        //JOBTYPE_M_MST が0件になっていたら
        //JOBTYPE_L_MST も削除する
        $cnt = $db->getOne($query);
        if ($cnt <= 0) {
            $query  = " DELETE FROM ";
            $query .= "     JOBTYPE_L_MST ";
            $query .= " WHERE ";
            $query .= "     JOBTYPE_LCD = '{$model->field["JOBTYPE_LCD"]}' ";

            $db->query($query);
        }

        Query::dbCheckIn($db);
    }
}
?>
