<?php

require_once('for_php7.php');

class knjp707Query extends Query {
    /************/
    /** 左画面 **/
    /************/
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     COLLECT_PATTERN_CD, ";
        $query .= "     COLLECT_PATTERN_NAME, ";
        $query .= "     DIRECT_DEBIT_DATE, ";
        $query .= "     CASE ";
        $query .= "         WHEN PAY_DIV = '1' THEN '口座引落' ";
        $query .= "         WHEN PAY_DIV = '2' THEN '口座引落2' ";
        $query .= "         WHEN PAY_DIV = '3' THEN '振込' ";
        $query .= "         WHEN PAY_DIV = '4' THEN '現金' ";
        $query .= "     END AS PAY_DIV ";
        $query .= " FROM ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     COLLECT_PATTERN_CD ";

        return $query;
    }

    /************/
    /** 右画面 **/
    /************/
    //１レコード取得
    function getRow($model, $intPatternCd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     COLLECT_DEFAULT_SETTINGS_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND T1.SCHOOL_KIND     = '".$model->schoolKind."' ";
        $query .= "     AND T1.YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND T1.COLLECT_PATTERN_CD = '".$intPatternCd."' ";

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    /**************/
    /** 更新処理 **/
    /**************/
    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["COLLECT_PATTERN_CD"][TEXT]   = $model->field["COLLECT_PATTERN_CD"];
        $data["COLLECT_PATTERN_NAME"][TEXT] = $model->field["COLLECT_PATTERN_NAME"];
        $data["DIRECT_DEBIT_DATE"][TEXT]    = $model->field["DIRECT_DEBIT_DATE"];
        foreach ($model->monthArray as $key => $val) {
            $setName = "COLLECT_MONTH_{$val}";
            $data[$setName][TEXT]               = $model->field[$setName];
        }
        $data["PAY_DIV"][TEXT]              = $model->field["PAY_DIV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_DEFAULT_SETTINGS_MST");
        $db->query($query);

        $db->commit();

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["COLLECT_PATTERN_NAME"][TEXT] = $model->field["COLLECT_PATTERN_NAME"];
        $data["DIRECT_DEBIT_DATE"][TEXT]    = $model->field["DIRECT_DEBIT_DATE"];
        foreach ($model->monthArray as $key => $val) {
            $setName = "COLLECT_MONTH_{$val}";
            $data[$setName][TEXT]               = $model->field[$setName];
        }
        $data["PAY_DIV"][TEXT]              = $model->field["PAY_DIV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "   AND SCHOOL_KIND     = '".$model->schoolKind."' ";
        $where .= "   AND YEAR            = '".CTRL_YEAR."' ";
        $where .= "   AND COLLECT_PATTERN_CD = '".$model->collectPatternCd."' ";

        $query = Query::updateSQL($data, "COLLECT_DEFAULT_SETTINGS_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND     = '".$model->schoolKind."' ";
        $query .= "     AND YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND COLLECT_PATTERN_CD = '".$model->collectPatternCd."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /****************/
    /** コピー処理 **/
    /****************/
    //今年度のデータが存在するかチェック(COLLECT_M_MST)
    function CheckData($model, $year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "   AND SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "   AND YEAR        = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

/** COLLECT_M_MST **/
        $query  = " INSERT INTO COLLECT_DEFAULT_SETTINGS_MST ";
        $query .= "           ( SCHOOLCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             YEAR, ";
        $query .= "             COLLECT_PATTERN_CD, ";
        $query .= "             COLLECT_PATTERN_NAME, ";
        $query .= "             DIRECT_DEBIT_DATE, ";
        foreach ($model->monthArray as $key => $val) {
            $query .= "             COLLECT_MONTH_{$val}, ";
        }
        $query .= "             PAY_DIV, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         T1.SCHOOLCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         T1.COLLECT_PATTERN_CD, ";
        $query .= "         T1.COLLECT_PATTERN_NAME, ";
        $query .= "         T1.DIRECT_DEBIT_DATE, ";
        foreach ($model->monthArray as $key => $val) {
            $query .= "         T1.COLLECT_MONTH_{$val}, ";
        }
        $query .= "         T1.PAY_DIV, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_DEFAULT_SETTINGS_MST T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "         AND T1.SCHOOL_KIND = '".$model->schoolKind."'";
        $query .= "         AND T1.YEAR        = '".(CTRL_YEAR - 1)."'";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
