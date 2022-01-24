<?php

require_once('for_php7.php');

class knjj212Query extends Query {
    //年度取得
    function getYear($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //左フレームのリスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     EVA.YEAR, ";
        $query .= "     EVA.COMMITTEECD, ";
        $query .= "     EVA.NAME, ";
        $query .= "     EVA.NAME_KANA, ";
        $query .= "     G1.NAME AS POSITION_CD ";
        $query .= " FROM ";
        $query .= "     EVALUATION_COMMITTEE_DAT EVA ";
        $query .= "     LEFT JOIN NAME_MST N1 ";
        $query .= "           ON N1.NAMECD1       = 'J006' ";
        $query .= "          AND EVA.POSITION_DIV = N1.NAMECD2 ";
        $query .= "     LEFT JOIN GUARDIAN_COMMITTEE_MST G1 ";
        $query .= "           ON EVA.POSITION_DIV = G1.DIV ";
        $query .= "          AND EVA.POSITION_CD  = G1.EXECUTIVECD ";
        $query .= " WHERE ";
        $query .= "     EVA.YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     EVA.COMMITTEECD ";

        return $query;
    }

    //EVALUATION_COMMITTEE_DATから１レコード取得
    function getEvaluationCommitteeDat($model, $committeecd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     EVALUATION_COMMITTEE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->year}' ";
        $query .= "     AND COMMITTEECD = '{$committeecd}' ";

        return $query;
    }

    //委員会区分取得
    function getPositionDiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1    = 'J006' ";
        $query .= "     AND NAMESPARE1 = '1' ";

        return $query;
    }

    //役職名取得
    function getPositionCd($model, $div) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTIVECD AS VALUE, ";
        $query .= "     T1.EXECUTIVECD || ' ' || T1.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_COMMITTEE_MST T1, ";
        $query .= "     GUARDIAN_COMMITTEE_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "         T2.YEAR        = '{$model->year}' ";
        $query .= "     AND T2.DIV         = '{$div}' ";
        $query .= "     AND T2.DIV         = T1.DIV ";
        $query .= "     AND T2.EXECUTIVECD = T1.EXECUTIVECD ";
        $query .= " ORDER BY ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.EXECUTIVECD ";

        return $query;
    }

    //レコードの存在チェック
    function checkEvaluationCommitteeDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     EVALUATION_COMMITTEE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$model->year}' ";
        $query .= "     AND COMMITTEECD = '{$model->field["COMMITTEECD"]}' ";

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["YEAR"][TEXT]         = $model->year;
        $data["COMMITTEECD"][TEXT]  = $model->field["COMMITTEECD"];
        $data["NAME"][TEXT]         = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]    = $model->field["NAME_KANA"];
        $data["ZIPCD"][TEXT]        = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]        = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]        = $model->field["ADDR2"];
        $data["POSITION_DIV"][TEXT] = $model->field["POSITION_DIV"];
        $data["POSITION_CD"][TEXT]  = $model->field["POSITION_CD"];
        $data["REMARK"][TEXT]       = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "EVALUATION_COMMITTEE_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* UPDATE */
    /**********/
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["NAME"][TEXT]         = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]    = $model->field["NAME_KANA"];
        $data["ZIPCD"][TEXT]        = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]        = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]        = $model->field["ADDR2"];
        $data["POSITION_DIV"][TEXT] = $model->field["POSITION_DIV"];
        $data["POSITION_CD"][TEXT]  = $model->field["POSITION_CD"];
        $data["REMARK"][TEXT]       = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "         YEAR        = '".$model->year."' ";
        $where .= "     AND COMMITTEECD = '".$model->field["COMMITTEECD"]."' ";

        $query = Query::updateSQL($data, "EVALUATION_COMMITTEE_DAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     EVALUATION_COMMITTEE_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".$model->year."' ";
        $query .= "     AND COMMITTEECD = '".$model->field["COMMITTEECD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
/*************************************前年度よりコピー*************************************/
    //件数カウント
    function getCopyCount($year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EVALUATION_COMMITTEE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    //選択年度のEVALUATION_COMMITTEE_DATデータを消去
    function &getCopyDelEvaluation($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     EVALUATION_COMMITTEE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //選択前年度からのコピー(EVALUATION_COMMITTEE_DAT)
    function &getCopyEvaluation($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO EVALUATION_COMMITTEE_DAT ";
        $query .= " SELECT ";
        $query .= "     '{$model->year}' AS YEAR, ";
        $query .= "     COMMITTEECD, ";
        $query .= "     NAME, ";
        $query .= "     NAME_KANA, ";
        $query .= "     ZIPCD, ";
        $query .= "     ADDR1, ";
        $query .= "     ADDR2, ";
        $query .= "     POSITION_DIV, ";
        $query .= "     POSITION_CD, ";
        $query .= "     REMARK, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     EVALUATION_COMMITTEE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".($model->year-1)."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
   }

    //選択年度のGUARDIAN_COMMITTEE_YDATデータを消去
    function &getCopyDelGuardCom($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     GUARDIAN_COMMITTEE_YDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     AND DIV  = '{$model->field["POSITION_DIV"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //選択前年度からのコピー(GUARDIAN_COMMITTEE_YDAT)
    function &getCopyGuardCom($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO GUARDIAN_COMMITTEE_YDAT ";
        $query .= " SELECT ";
        $query .= "     '{$model->year}' AS YEAR, ";
        $query .= "     '{$model->field["POSITION_DIV"]}' AS DIV, ";
        $query .= "     EXECUTIVECD, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_COMMITTEE_YDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".($model->year-1)."' ";
        $query .= "     AND DIV  = '{$model->field["POSITION_DIV"]}' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
   }
}
?>
