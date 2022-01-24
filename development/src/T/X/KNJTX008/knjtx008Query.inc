<?php

require_once('for_php7.php');

class knjtx008Query extends Query {
    //科目コードコンボ
    function getPrefCd() {
        $query .= " SELECT ";
        $query .= "     PREF_CD AS VALUE, ";
        $query .= "     PREF_CD || '：' || PREF_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " ORDER BY ";
        $query .= "     PREF_CD ";

        return $query;
    }

    //左フレームのリスト
    function getList() {
        $query .= " SELECT ";
        $query .= "     L1.PREF_CD, ";
        $query .= "     L1.PREF_NAME, ";
        $query .= "     T1.CITY_CD, ";
        $query .= "     T1.CITY_NAME, ";
        $query .= "     T1.CITY_KANA, ";
        $query .= "     T1.CITY_FLG1, ";
        $query .= "     T1.CITY_FLG2, ";
        $query .= "     T1.CITY_FLG3, ";
        $query .= "     T1.CITY_FLG4, ";
        $query .= "     T1.CITY_FLG5 ";
        $query .= " FROM ";
        $query .= "     CITY_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     PREF_MST L1 ON L1.PREF_CD = T1.PREF_CD ";
        $query .= " ORDER BY ";
        $query .= "     T1.PREF_CD, ";
        $query .= "     T1.CITY_CD ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getPrefCnt($pref_cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CITY_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '{$pref_cd}' ";

        return $query;
    }

    //ATTEND_SUBCLASS_SPECIAL_DAT から１レコード取得
    function getCityMst($pref_cd, $city_cd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CITY_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '{$pref_cd}' AND ";
        $query .= "     CITY_CD = '{$city_cd}' ";

        return $query;
    }

    //更新の時にレコードが存在するのかチェックする
    function getCityMst_check($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CITY_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '{$model->field["PREF_CD"]}' AND ";
        $query .= "     CITY_CD = '{$model->field["CITY_CD"]}' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertMstQuery($model) {
        $db = Query::dbCheckOut();

        $data["PREF_CD"][TEXT]    = $model->field["PREF_CD"];
        $data["CITY_CD"][TEXT]    = $model->field["CITY_CD"];
        $data["CITY_NAME"][TEXT]  = $model->field["CITY_NAME"];
        $data["CITY_KANA"][TEXT]  = $model->field["CITY_KANA"];
        $data["CITY_FLG1"][TEXT]  = $model->field["CITY_FLG1"];
        $data["CITY_FLG2"][TEXT]  = $model->field["CITY_FLG2"];
        $data["CITY_FLG3"][TEXT]  = $model->field["CITY_FLG3"];
        $data["CITY_FLG4"][TEXT]  = $model->field["CITY_FLG4"];
        $data["CITY_FLG5"][TEXT]  = $model->field["CITY_FLG5"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $query = Query::insertSQL($data, "CITY_MST");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    //ATTEND_SUBCLASS_SPECIAL_MST を更新
    function &getUpdateMstQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["PREF_CD"][TEXT]    = $model->field["PREF_CD"];
        $data["CITY_CD"][TEXT]    = $model->field["CITY_CD"];
        $data["CITY_NAME"][TEXT]  = $model->field["CITY_NAME"];
        $data["CITY_KANA"][TEXT]  = $model->field["CITY_KANA"];
        $data["CITY_FLG1"][TEXT]  = $model->field["CITY_FLG1"];
        $data["CITY_FLG2"][TEXT]  = $model->field["CITY_FLG2"];
        $data["CITY_FLG3"][TEXT]  = $model->field["CITY_FLG3"];
        $data["CITY_FLG4"][TEXT]  = $model->field["CITY_FLG4"];
        $data["CITY_FLG5"][TEXT]  = $model->field["CITY_FLG5"];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][NUMBER]  = "sysdate()";

        $where  = " WHERE ";
        $where .= "     PREF_CD = '{$model->field["PREF_CD"]}' AND ";
        $where .= "     CITY_CD = '{$model->field["CITY_CD"]}' ";

        $query = Query::updateSQL($data, "CITY_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query .= " DELETE FROM ";
        $query .= "     CITY_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '{$model->field["PREF_CD"]}' AND ";
        $query .= "     CITY_CD = '{$model->field["CITY_CD"]}' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }
}
?>
