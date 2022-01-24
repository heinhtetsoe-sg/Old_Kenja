<?php

require_once('for_php7.php');

class knjp708Query extends Query {

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

    //減免マスタ取得
    function getScholarshipMst($model) {
        $query  = " SELECT ";
        $query .= "    SCHOLARSHIP || ':' || SCHOLARSHIP_NAME AS LABEL, ";
        $query .= "    SCHOLARSHIP AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOLARSHIP_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ取得
    function selectQuery($model) {
        $query  = "  SELECT ";
        $query .= "    DIV_MST.SCHOOLCD, ";
        $query .= "    DIV_MST.SCHOOL_KIND, ";
        $query .= "    DIV_MST.YEAR, ";
        $query .= "    DIV_MST.REDUCTION_DIV_CD, ";
        $query .= "    DIV_MST.REDUCTION_DIV_NAME, ";
        $query .= "    DIV_MST.SCHOLARSHIP, ";
        $query .= "    DIV_MST.SEMESTER_DIV, ";
        $query .= "    DIV_MST.VALID_MONTH, ";
        $query .= "    DIV_MST.REDUCTION_DIV, ";
        $query .= "    DIV_MST.REDUCTION_TIMING, ";
        $query .= "    SEM.SEMESTERNAME, ";
        $query .= "    SHIP.SCHOLARSHIP_ABBV, ";
        $query .= "    RST.MONEY_DIV, ";
        $query .= "    RST.NUMERATOR, ";
        $query .= "    RST.DENOMINATOR, ";
        $query .= "    RST.MONEY, ";
        $query .= "    P007.NAME1 AS COLLECT_NAME ";
        $query .= "  FROM ";
        $query .= "    REDUCTION_SCHOOL_MST DIV_MST ";
        $query .= "    LEFT JOIN SEMESTER_MST SEM ";
        $query .= "      ON DIV_MST.YEAR = SEM.YEAR ";
        $query .= "      AND DIV_MST.SEMESTER_DIV = SEM.SEMESTER ";
        $query .= "    LEFT JOIN SCHOLARSHIP_MST SHIP ";
        $query .= "      ON DIV_MST.SCHOOLCD = SHIP.SCHOOLCD ";
        $query .= "      AND DIV_MST.SCHOOL_KIND = SHIP.SCHOOL_KIND ";
        $query .= "      AND DIV_MST.YEAR = SHIP.YEAR ";
        $query .= "      AND DIV_MST.SCHOLARSHIP = SHIP.SCHOLARSHIP ";
        $query .= "    LEFT JOIN REDUCTION_SCHOOL_TARGET_MST RST ";
        $query .= "      ON  DIV_MST.SCHOOLCD = RST.SCHOOLCD ";
        $query .= "  	   AND DIV_MST.SCHOOL_KIND = RST.SCHOOL_KIND ";
        $query .= "  	   AND DIV_MST.YEAR = RST.YEAR ";
        $query .= "  	   AND DIV_MST.REDUCTION_DIV_CD = RST.REDUCTION_DIV_CD ";
        $query .= "    LEFT JOIN V_NAME_MST P007 ";
        $query .= "    	 ON P007.YEAR = RST.YEAR ";
        $query .= "  	   AND P007.NAMECD1 = 'P007' ";
        $query .= "  	   AND P007.NAMECD2 = RST.REDUCTION_TARGET ";
        $query .= "  WHERE ";
        $query .= "    DIV_MST.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND DIV_MST.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "    AND DIV_MST.YEAR = '".CTRL_YEAR."' ";
        $query .= "  ORDER BY ";
        $query .= "    REDUCTION_DIV_CD, REDUCTION_TARGET ";

        return $query;
    }

    //左フレームのテーブルのROWSPANの値のためのカウント
    function getCollectMCnt($model, $cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_TARGET_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD                 = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND          = '".$model->schoolKind."'";
        $query .= "     AND YEAR                 = '". CTRL_YEAR ."' ";
        $query .= "     AND REDUCTION_DIV_CD     = '".$cd."'";

        return $query;
    }

    //学期取得
    function getSemester($model) {
        $query  = " WITH T_8 (VALUE, LABEL) AS (VALUES('8', '8:単年')), ";
        $query .= "      T_9 (VALUE, LABEL) AS (VALUES('9', '9:通年')) ";
        $query .= " SELECT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     T_8 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     VALUE, ";
        $query .= "     LABEL ";
        $query .= " FROM ";
        $query .= "     T_9 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     SEM.SEMESTER AS VALUE, ";
        $query .= "     SEM.SEMESTER || ':' || SEM.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST SEM ";
        $query .= " WHERE ";
        $query .= "         SEM.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEM.SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $reductionDivCd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     REDUCTION_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD         = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND      = '".$model->schoolKind."'";
        $query .= "     AND YEAR             = '".CTRL_YEAR."'";
        $query .= "     AND REDUCTION_DIV_CD = '".$reductionDivCd."'";

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    //学納金区分取得
    function getReductionCollectM($model) {
        $query  = " SELECT ";
        $query .= "     P007.NAMECD2 AS REDUCTION_TARGET, ";
        $query .= "     NAME1 AS COLLECT_NAME, ";
        $query .= "     TARGET_M.MONEY_DIV, ";
        $query .= "     TARGET_M.NUMERATOR, ";
        $query .= "     TARGET_M.DENOMINATOR, ";
        $query .= "     TARGET_M.MONEY ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST P007 ";
        $query .= "     LEFT JOIN REDUCTION_SCHOOL_TARGET_MST TARGET_M ";
        $query .= "           ON TARGET_M.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "          AND TARGET_M.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "          AND P007.YEAR = TARGET_M.YEAR ";
        $query .= "          AND TARGET_M.REDUCTION_DIV_CD = '".$model->reductionDivCd."' ";
        $query .= "          AND P007.NAMECD2 = TARGET_M.REDUCTION_TARGET ";
        $query .= " WHERE ";
        $query .= "     P007.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'P007' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }
    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["REDUCTION_DIV_CD"][TEXT]     = sprintf("%02d",$fields["REDUCTION_DIV_CD"]);
        $data["REDUCTION_DIV_NAME"][TEXT]   = $fields["REDUCTION_DIV_NAME"];
        $data["SCHOLARSHIP"][TEXT]          = $fields["SCHOLARSHIP"];
        $data["SEMESTER_DIV"][TEXT]         = $fields["SEMESTER_DIV"];
        $data["REDUCTION_DIV"][TEXT]        = '1';
        $data["MONEY_DIV"][TEXT]            = $fields["MONEY_DIV"];
        $data["NUMERATOR"][NUMBER]          = $fields["NUMERATOR"];
        $data["DENOMINATOR"][NUMBER]        = $fields["DENOMINATOR"];
        $data["MONEY"][NUMBER]              = $fields["MONEY"];
        $data["REDUCTION_TIMING"][TEXT]     = $fields["REDUCTION_TIMING"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "REDUCTION_SCHOOL_MST");
        $db->query($query);

        //「REDUCTION_SCHOOL_TARGET_MST」への挿入処理
        foreach ($model->rowIdArr as $rowId) {
            knjp708Query::getUpdateKoumokuQuery($db, $model, $rowId);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //「REDUCTION_SCHOOL_MST」への更新処理
        $data["REDUCTION_DIV_NAME"][TEXT]   = $fields["REDUCTION_DIV_NAME"];
        $data["SCHOLARSHIP"][TEXT]          = $fields["SCHOLARSHIP"];
        $data["SEMESTER_DIV"][TEXT]         = $fields["SEMESTER_DIV"];
        $data["REDUCTION_DIV"][TEXT]        = '1';
        $data["REDUCTION_TIMING"][TEXT]     = $fields["REDUCTION_TIMING"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ";
        $where .= "   SCHOOLCD              = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "   AND SCHOOL_KIND       = '".$model->schoolKind."' ";
        $where .= "   AND YEAR              = '".CTRL_YEAR."'";
        $where .= "   AND REDUCTION_DIV_CD  = '".$fields["REDUCTION_DIV_CD"]."'";

        $query = Query::updateSQL($data, "REDUCTION_SCHOOL_MST", $where);
        $db->query($query);


        //「REDUCTION_SCHOOL_TARGET_MST」への更新処理
        foreach ($model->rowIdArr as $rowId) {
            knjp708Query::getUpdateKoumokuQuery($db, $model, $rowId);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //減免対象項目の更新
    function getUpdateKoumokuQuery($db, $model, $rowId) {

        //DELETE処理
        $query = "  DELETE ";
        $query .= "  FROM ";
        $query .= "    REDUCTION_SCHOOL_TARGET_MST ";
        $query .= "  WHERE ";
        $query .= "    SCHOOLCD             = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "    AND SCHOOL_KIND      = '".$model->schoolKind."' ";
        $query .= "    AND YEAR             = '".CTRL_YEAR."' ";
        $query .= "    AND REDUCTION_DIV_CD = '".$model->field["REDUCTION_DIV_CD"]."' ";
        $query .= "    AND REDUCTION_TARGET = '".$rowId."' ";

        $db->query($query);
        $query = "";

        //金額が入力されていない場合はレコードを挿入しない(DELETEのみ行う)
        if ($model->field["MONEY_DIV".$rowId] == "1") {
            if (!trim($model->field["NUMERATOR".$rowId]) || !trim($model->field["DENOMINATOR".$rowId])) return;        
        }
        if ($model->field["MONEY_DIV".$rowId] == "2") {
            if (!trim($model->field["MONEY".$rowId])) return;
        }

        //INSERT処理
        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]          = $model->schoolKind;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["REDUCTION_DIV_CD"][TEXT]     = sprintf("%02d",$model->field["REDUCTION_DIV_CD"]);
        $data["REDUCTION_TARGET"][TEXT]     = $rowId;
        $data["MONEY_DIV"][TEXT]            = $model->field["MONEY_DIV".$rowId];
        $data["NUMERATOR"][NUMBER]          = $model->field["NUMERATOR".$rowId];
        $data["DENOMINATOR"][NUMBER]        = $model->field["DENOMINATOR".$rowId];
        $data["MONEY"][NUMBER]              = $model->field["MONEY".$rowId];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "REDUCTION_SCHOOL_TARGET_MST");

        $db->query($query);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM REDUCTION_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD                  = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND           = '".$model->schoolKind."' ";
        $query .= "   AND YEAR                  = '".CTRL_YEAR."'";
        $query .= "   AND REDUCTION_DIV_CD      = '".$model->field["REDUCTION_DIV_CD"]."'";
        $db->query($query);
        $query = "";

        //「REDUCTION_SCHOOL_TARGET_MST」の削除処理
        $query  = " DELETE FROM REDUCTION_SCHOOL_TARGET_MST";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD                  = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "   AND SCHOOL_KIND           = '".$model->schoolKind."' ";
        $query .= "   AND YEAR                  = '".CTRL_YEAR."' ";
        $query .= "   AND REDUCTION_DIV_CD      = '".$model->field["REDUCTION_DIV_CD"]."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

/**** 前年度コピー処理 ****/
    //今年度のデータが存在するかチェック
    function CheckData($model, $year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM REDUCTION_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD        = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND     = '".$model->schoolKind."'";
        $query .= "     AND YEAR            = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO REDUCTION_SCHOOL_MST ";
        $query .= "           ( SCHOOLCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             YEAR, ";
        $query .= "             REDUCTION_DIV_CD, ";
        $query .= "             REDUCTION_DIV_NAME, ";
        $query .= "             SCHOLARSHIP, ";
        $query .= "             SEMESTER_DIV, ";
        $query .= "             VALID_MONTH, ";
        $query .= "             REDUCTION_DIV, ";
        $query .= "             MONEY_DIV, ";
        $query .= "             NUMERATOR, ";
        $query .= "             DENOMINATOR, ";
        $query .= "             MONEY, ";
        $query .= "             REDUCTION_TIMING, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         '".$model->year."', ";
        $query .= "         REDUCTION_DIV_CD, ";
        $query .= "         REDUCTION_DIV_NAME, ";
        $query .= "         SCHOLARSHIP, ";
        $query .= "         SEMESTER_DIV, ";
        $query .= "         VALID_MONTH, ";
        $query .= "         '1', ";
        $query .= "         MONEY_DIV, ";
        $query .= "         NUMERATOR, ";
        $query .= "         DENOMINATOR, ";
        $query .= "         MONEY, ";
        $query .= "         REDUCTION_TIMING, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         REDUCTION_SCHOOL_MST ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "         AND YEAR        = '".($model->year - 1)."' ";
        $query .= "     ) ";
        $db->query($query);

        $query = "";

        //「REDUCTION_SCHOOL_TARGET_MST」へのコピー処理
        $query  = " INSERT INTO REDUCTION_SCHOOL_TARGET_MST ";
        $query .= "           ( SCHOOLCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             YEAR, ";
        $query .= "             REDUCTION_DIV_CD, ";
        $query .= "             REDUCTION_TARGET, ";
        $query .= "             MONEY_DIV, ";
        $query .= "             NUMERATOR, ";
        $query .= "             DENOMINATOR, ";
        $query .= "             MONEY, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "  (SELECT ";
        $query .= "     RST.SCHOOLCD, ";
        $query .= "     RST.SCHOOL_KIND, ";
        $query .= "     '".$model->year."', ";
        $query .= "     RST.REDUCTION_DIV_CD, ";
        $query .= "     RST.REDUCTION_TARGET, ";
        $query .= "     RST.MONEY_DIV, ";
        $query .= "     RST.NUMERATOR, ";
        $query .= "     RST.DENOMINATOR, ";
        $query .= "     RST.MONEY, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= "  FROM ";
        $query .= "     REDUCTION_SCHOOL_TARGET_MST RST ";
        $query .= "  WHERE ";
        $query .= "       RST.SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "       AND RST.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "       AND RST.YEAR        = '".($model->year - 1)."' ";
        $query .= "  ) ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
