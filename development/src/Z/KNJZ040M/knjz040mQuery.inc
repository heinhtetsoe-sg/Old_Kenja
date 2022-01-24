<?php

require_once('for_php7.php');

class knjz040mQuery extends Query {
    //左フレームのリスト
    function getList() {
        $query  = " SELECT ";
        $query .= "     T1.NATPUBPRI_CD, ";
        $query .= "     T1.NATPUBPRI_NAME, ";
        $query .= "     T1.NATPUBPRI_ABBV, ";
        $query .= "     L1.AREA_DIV_CD, ";
        $query .= "     L1.AREA_DIV_NAME, ";
        $query .= "     L1.AREA_DIV_ABBV, ";
        $query .= "     L2.AREA_CD, ";
        $query .= "     L2.AREA_NAME, ";
        $query .= "     L2.AREA_ABBV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NATPUBPRI_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_AREA_DIV_MST L1 ON L1.NATPUBPRI_CD = T1.NATPUBPRI_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_AREA_MST L2 ON  L2.NATPUBPRI_CD = T1.NATPUBPRI_CD ";
        $query .= "                         AND L2.AREA_DIV_CD = L1.AREA_DIV_CD ";
        $query .= " ORDER BY ";
        $query .= "     T1.NATPUBPRI_CD, ";
        $query .= "     L1.AREA_DIV_CD, ";
        $query .= "     L2.AREA_CD ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getNatpubpriCnt($natpubpri_cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NATPUBPRI_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_AREA_DIV_MST L1 ON L1.NATPUBPRI_CD = T1.NATPUBPRI_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_AREA_MST L2 ON  L2.NATPUBPRI_CD = T1.NATPUBPRI_CD ";
        $query .= "                         AND L2.AREA_DIV_CD = L1.AREA_DIV_CD ";
        $query .= " WHERE ";
        $query .= "     T1.NATPUBPRI_CD = '{$natpubpri_cd}' ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getAreaDivCnt($natpubpri_cd, $area_div_cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NATPUBPRI_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_AREA_DIV_MST L1 ON L1.NATPUBPRI_CD = T1.NATPUBPRI_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_AREA_MST L2 ON  L2.NATPUBPRI_CD = T1.NATPUBPRI_CD ";
        $query .= "                         AND L2.AREA_DIV_CD = L1.AREA_DIV_CD ";
        $query .= " WHERE ";
        $query .= "     T1.NATPUBPRI_CD = '{$natpubpri_cd}' AND ";
        $query .= "     L1.AREA_DIV_CD  = '{$area_div_cd}' ";

        return $query;
    }

    //１レコード取得
    function get_natpubpri_area_div_area($model) {
        $query  = " SELECT ";
        $query .= "     T1.NATPUBPRI_CD, ";
        $query .= "     T1.NATPUBPRI_NAME, ";
        $query .= "     T1.NATPUBPRI_ABBV, ";
        $query .= "     L1.AREA_DIV_CD, ";
        $query .= "     L1.AREA_DIV_NAME, ";
        $query .= "     L1.AREA_DIV_ABBV, ";
        $query .= "     L2.AREA_CD, ";
        $query .= "     L2.AREA_NAME, ";
        $query .= "     L2.AREA_ABBV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NATPUBPRI_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_AREA_DIV_MST L1 ON L1.NATPUBPRI_CD = T1.NATPUBPRI_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_AREA_MST L2 ON  L2.NATPUBPRI_CD = T1.NATPUBPRI_CD ";
        $query .= "                         AND L2.AREA_DIV_CD = L1.AREA_DIV_CD ";
        $query .= " WHERE ";
        $query .= "     VALUE(T1.NATPUBPRI_CD, '') = '{$model->natpubpri_cd}' AND ";
        $query .= "     VALUE(L1.AREA_DIV_CD, '')  = '{$model->area_div_cd}' AND ";
        $query .= "     VALUE(L2.AREA_CD, '')      = '{$model->area_cd}' ";

        return $query;
    }

    /************************************************/
    /* 更新の時にレコードが存在するのかチェックする */
    /************************************************/
    function getEntexamNatpubpriMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NATPUBPRI_MST ";
        $query .= " WHERE ";
        $query .= "     NATPUBPRI_CD = '{$model->field["NATPUBPRI_CD"]}' ";

        return $query;
    }
    function getEntexamAreaDivMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_AREA_DIV_MST ";
        $query .= " WHERE ";
        $query .= "     NATPUBPRI_CD = '{$model->field["NATPUBPRI_CD"]}' AND ";
        $query .= "     AREA_DIV_CD  = '{$model->field["AREA_DIV_CD"]}' ";

        return $query;
    }
    function getEntexamAreaMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_AREA_MST ";
        $query .= " WHERE ";
        $query .= "     NATPUBPRI_CD = '{$model->field["NATPUBPRI_CD"]}' AND ";
        $query .= "     AREA_DIV_CD  = '{$model->field["AREA_DIV_CD"]}' AND ";
        $query .= "     AREA_CD      = '{$model->field["AREA_CD"]}' ";

        return $query;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateMstQuery($model) {
        $db = Query::dbCheckOut();

        if ($this->field["NATPUBPRI_CD"]) {
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_NATPUBPRI_MST ";
            $query .= " WHERE ";
            $query .= "     NATPUBPRI_CD = '{$model->field["NATPUBPRI_CD"]}' ";

            $db->query($query);

            $data = array();
            $data["NATPUBPRI_CD"][TEXT]   = $model->field["NATPUBPRI_CD"];
            $data["NATPUBPRI_NAME"][TEXT] = $model->field["NATPUBPRI_NAME"];
            $data["NATPUBPRI_ABBV"][TEXT] = $model->field["NATPUBPRI_ABBV"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_NATPUBPRI_MST");
            $db->query($query);
        }

        if ($this->field["AREA_DIV_CD"]) {
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_AREA_DIV_MST ";
            $query .= " WHERE ";
            $query .= "     NATPUBPRI_CD = '{$model->field["NATPUBPRI_CD"]}' AND ";
            $query .= "     AREA_DIV_CD  = '{$model->field["AREA_DIV_CD"]}' ";

            $db->query($query);

            $data = array();
            $data["NATPUBPRI_CD"][TEXT]   = $model->field["NATPUBPRI_CD"];
            $data["AREA_DIV_CD"][TEXT]    = $model->field["AREA_DIV_CD"];
            $data["AREA_DIV_NAME"][TEXT]  = $model->field["AREA_DIV_NAME"];
            $data["AREA_DIV_ABBV"][TEXT]  = $model->field["AREA_DIV_ABBV"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_AREA_DIV_MST");
            $db->query($query);
        }

        if ($this->field["AREA_CD"]) {
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_AREA_MST ";
            $query .= " WHERE ";
            $query .= "     NATPUBPRI_CD = '{$model->field["NATPUBPRI_CD"]}' AND ";
            $query .= "     AREA_DIV_CD  = '{$model->field["AREA_DIV_CD"]}' AND ";
            $query .= "     AREA_CD      = '{$model->field["AREA_CD"]}' ";

            $db->query($query);

            $data = array();
            $data["NATPUBPRI_CD"][TEXT]   = $model->field["NATPUBPRI_CD"];
            $data["AREA_DIV_CD"][TEXT]    = $model->field["AREA_DIV_CD"];
            $data["AREA_CD"][TEXT]        = $model->field["AREA_CD"];
            $data["AREA_NAME"][TEXT]      = $model->field["AREA_NAME"];
            $data["AREA_ABBV"][TEXT]      = $model->field["AREA_ABBV"];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_AREA_MST");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        if ($this->field["AREA_CD"]) {
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_AREA_MST ";
            $query .= " WHERE ";
            $query .= "     NATPUBPRI_CD = '{$model->field["NATPUBPRI_CD"]}' AND ";
            $query .= "     AREA_DIV_CD  = '{$model->field["AREA_DIV_CD"]}' AND ";
            $query .= "     AREA_CD      = '{$model->field["AREA_CD"]}' ";

            $db->query($query);
        }

        if ($this->field["AREA_DIV_CD"]) {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_AREA_MST ";
            $query .= " WHERE ";
            $query .= "     NATPUBPRI_CD = '{$model->field["NATPUBPRI_CD"]}' AND ";
            $query .= "     AREA_DIV_CD  = '{$model->field["AREA_DIV_CD"]}' ";

            $cnt_entexam_area_mst = $db->getOne($query);
            if ($cnt_entexam_area_mst == 0) {
                $query  = " DELETE FROM ";
                $query .= "     ENTEXAM_AREA_DIV_MST ";
                $query .= " WHERE ";
                $query .= "     NATPUBPRI_CD = '{$model->field["NATPUBPRI_CD"]}' AND ";
                $query .= "     AREA_DIV_CD  = '{$model->field["AREA_DIV_CD"]}' ";

                $db->query($query);
            }
        }

        if ($this->field["NATPUBPRI_CD"]) {
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_AREA_DIV_MST ";
            $query .= " WHERE ";
            $query .= "     NATPUBPRI_CD = '{$model->field["NATPUBPRI_CD"]}' ";

            $cnt_entexam_area_div_mst = $db->getOne($query);
            if ($cnt_entexam_area_div_mst == 0) {
                $query  = " DELETE FROM ";
                $query .= "     ENTEXAM_NATPUBPRI_MST ";
                $query .= " WHERE ";
                $query .= "     NATPUBPRI_CD = '{$model->field["NATPUBPRI_CD"]}' ";

                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }
}
?>
