<?php

require_once('for_php7.php');

class knjz451Query extends Query {
    //処理年度
    function getExeYear() {
        $query  = " WITH EXE_YEAR (VALUE, LABEL) AS ( ";
        $query .= "     VALUES (".CTRL_YEAR.", '".CTRL_YEAR."年度'), (".(CTRL_YEAR + 1).", '".(CTRL_YEAR + 1)."年度')";
        $query .= " ) ";

        $query .=" SELECT ";
        $query .="     * ";
        $query .=" FROM ";
        $query .="     EXE_YEAR ";
        $query .=" ORDER BY ";
        $query .="     VALUE DESC ";

        return $query;
    }

    //左フレームのリスト
    function getList($model) {
        $query .= " SELECT ";
        $query .= "     QH.QUALIFIED_CD, ";
        $query .= "     HM.QUALIFIED_NAME, ";
        $query .= "     QH.LIMIT_MONTH, ";
        $query .= "     QH.SETUP_CNT, ";
        $query .= "     QD.SETUP_QUALIFIED_CD, ";
        $query .= "     DM.QUALIFIED_NAME AS SETUP_QUALIFIED_NAME ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_HDAT QH ";
        $query .= "     LEFT JOIN QUALIFIED_MST HM ON QH.QUALIFIED_CD = HM.QUALIFIED_CD ";
        $query .= "     LEFT JOIN QUALIFIED_SETUP_DAT QD ON QH.YEAR = QD.YEAR ";
        $query .= "          AND QH.QUALIFIED_CD = QD.QUALIFIED_CD ";
        $query .= "     LEFT JOIN QUALIFIED_MST DM ON QD.SETUP_QUALIFIED_CD = DM.QUALIFIED_CD ";
        $query .= " WHERE ";
        $query .= "     QH.YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     QH.QUALIFIED_CD, ";
        $query .= "     QD.SETUP_QUALIFIED_CD ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getGroupCnt($model, $qualifiedCd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_HDAT QH ";
        $query .= "     LEFT JOIN QUALIFIED_MST HM ON QH.QUALIFIED_CD = HM.QUALIFIED_CD ";
        $query .= "     LEFT JOIN QUALIFIED_SETUP_DAT QD ON QH.YEAR = QD.YEAR ";
        $query .= "          AND QH.QUALIFIED_CD = QD.QUALIFIED_CD ";
        $query .= "     LEFT JOIN QUALIFIED_MST DM ON QD.QUALIFIED_CD = DM.QUALIFIED_CD ";
        $query .= " WHERE ";
        $query .= "     QH.YEAR = '{$model->year}' ";
        $query .= "     AND QH.QUALIFIED_CD = '{$qualifiedCd}' ";

        return $query;
    }

    //QUALIFIED_SETUP_HDAT から１レコード取得
    function getQualifiedHdat($model) {
        $query .= " SELECT ";
        $query .= "     QH.QUALIFIED_CD, ";
        $query .= "     QH.LIMIT_MONTH, ";
        $query .= "     QH.SETUP_CNT ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_HDAT QH ";
        $query .= " WHERE ";
        $query .= "     QH.YEAR = '{$model->year}' ";
        $query .= "     AND QH.QUALIFIED_CD = '{$model->field["QUALIFIED_CD"]}' ";

        return $query;
    }

    function getQualifiedMst($model) {
        $query .= " SELECT ";
        $query .= "     T1.QUALIFIED_CD || ':' || T1.QUALIFIED_NAME AS LABEL, ";
        $query .= "     T1.QUALIFIED_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST T1 ";
        $query .= " WHERE ";
        $query .= "     CONDITION_DIV = '2' ";
        $query .= "     AND VALUE(SAKI_FLG, '0') = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getLeftList($model) {
        $query  = " SELECT ";
        $query .= "     CASE WHEN DM.SAKI_FLG = '1' ";
        $query .= "          THEN QD.SETUP_QUALIFIED_CD || ':●' || DM.QUALIFIED_NAME ";
        $query .= "          ELSE QD.SETUP_QUALIFIED_CD || ':' || DM.QUALIFIED_NAME ";
        $query .= "     END AS LABEL, ";
        $query .= "     QD.SETUP_QUALIFIED_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_DAT QD ";
        $query .= "     LEFT JOIN QUALIFIED_MST DM ON QD.SETUP_QUALIFIED_CD = DM.QUALIFIED_CD ";
        $query .= " WHERE ";
        $query .= "     QD.YEAR = '{$model->year}' ";
        $query .= "     AND QD.QUALIFIED_CD = '{$model->field["QUALIFIED_CD"]}' ";

        return $query;
    }

    function getRightList($model) {
        $query .= " SELECT ";
        $query .= "     CASE WHEN T1.SAKI_FLG = '1' ";
        $query .= "          THEN T1.QUALIFIED_CD || ':●' || T1.QUALIFIED_NAME ";
        $query .= "          ELSE T1.QUALIFIED_CD || ':' || T1.QUALIFIED_NAME ";
        $query .= "     END AS LABEL, ";
        $query .= "     T1.QUALIFIED_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST T1 ";
        $query .= " WHERE ";
        $query .= "     CONDITION_DIV = '2' ";
        $query .= "     AND QUALIFIED_CD <> '{$model->field["QUALIFIED_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //前年度コピーの時のチェック
    function cntQualifiedHdat($year) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertMstQuery($model) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = $model->year;
        $data["QUALIFIED_CD"][TEXT]     = $model->field["QUALIFIED_CD"];
        $data["LIMIT_MONTH"][NUMBER]    = $model->field["LIMIT_MONTH"];
        $data["SETUP_CNT"][NUMBER]      = $model->field["SETUP_CNT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "QUALIFIED_SETUP_HDAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //SUBCLASS_COMP_SELECT_DAT を更新
    function &getInsertDatQuery($model, $qualifiedCd) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                 = $model->year;
        $data["QUALIFIED_CD"][TEXT]         = $model->field["QUALIFIED_CD"];
        $data["SETUP_QUALIFIED_CD"][TEXT]   = $qualifiedCd;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "QUALIFIED_SETUP_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    //SUBCLASS_COMP_SELECT_MST を更新（グループコード"600"以上）
    function &getUpdateMstQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["LIMIT_MONTH"][NUMBER]    = $model->field["LIMIT_MONTH"];
        $data["SETUP_CNT"][NUMBER]      = $model->field["SETUP_CNT"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".$model->year."' AND ";
        $where .= "     QUALIFIED_CD = '".$model->field["QUALIFIED_CD"]."' ";

        $query = Query::updateSQL($data, "QUALIFIED_SETUP_HDAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    /* dat */
    function &getDeleteDatQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     QUALIFIED_SETUP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$model->year."' ";
        $query .= "     AND QUALIFIED_CD = '{$model->field["QUALIFIED_CD"]}' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    /* mst */
    function &getDeleteMstQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     QUALIFIED_SETUP_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$model->year."' ";
        $query .= "     AND QUALIFIED_CD = '{$model->field["QUALIFIED_CD"]}' ";

        $db->query($query);

        Query::dbCheckIn($db);
    }

    /********/
    /* COPY */
    /********/
    function getInsertCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $fromYear = $model->year - 1;
        $toYear   = $model->year;

        $query  = " INSERT INTO QUALIFIED_SETUP_DAT ";
        $query .= " SELECT ";
        $query .= "      '{$toYear}', ";
        $query .= "      QUALIFIED_CD, ";
        $query .= "      SETUP_QUALIFIED_CD, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE()";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$fromYear}' ";

        $db->query($query);

        $query  = " INSERT INTO QUALIFIED_SETUP_HDAT ";
        $query .= " SELECT ";
        $query .= "      '{$toYear}', ";
        $query .= "      QUALIFIED_CD, ";
        $query .= "      LIMIT_MONTH, ";
        $query .= "      SETUP_CNT, ";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE()";
        $query .= " FROM ";
        $query .= "     QUALIFIED_SETUP_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$fromYear}' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
