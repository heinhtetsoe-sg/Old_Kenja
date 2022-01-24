<?php

require_once('for_php7.php');

class knjp737Query extends Query {
    //年度取得
    function getYear($model) {
        $query  = " WITH CTRL_T (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLECT_CSV_INFO_DAT ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CTRL_T ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //列番号情報取得
    function getRowNoData($model, $flg) {
        $query  = " SELECT ";
        if ($flg == "min") {
            $query .= "     MIN(CSV_INFO.ROW_NO) ";
        } else if ($flg == "cnt") {
            $query .= "     COUNT(CSV_INFO.ROW_NO) ";
        } else {
            $query .= "     CSV_INFO.ROW_NO, ";
            $query .= "     CSV_INFO.GRP_CD ";
        }
        $query .= " FROM ";
        $query .= "     COLLECT_CSV_INFO_DAT CSV_INFO ";
        $query .= " WHERE ";
        $query .= "     CSV_INFO.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     CSV_INFO.SCHOOL_KIND = '".$model->school_kind."' AND ";
        $query .= "     CSV_INFO.YEAR        = '".$model->year."' ";

        return $query;
    }

    //列番号情報取得
    function getHeadName($model) {
        $query  = " SELECT ";
        $query .= "     CSV_HEAD.ROW_NO, ";
        $query .= "     CSV_HEAD.HEAD_NAME ";
        $query .= " FROM ";
        $query .= "     COLLECT_CSV_HEAD_CAPTURE_DAT CSV_HEAD ";
        $query .= " WHERE ";
        $query .= "     CSV_HEAD.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     CSV_HEAD.SCHOOL_KIND = '".$model->school_kind."' AND ";
        $query .= "     CSV_HEAD.YEAR        = '".$model->year."' ";

        return $query;
    }

    //グループ
    function getCsvGrp($model) {
        $query  = " SELECT ";
        $query .= "     CSV_GRP.GRP_CD AS VALUE, ";
        $query .= "     CSV_GRP.GRP_CD || ':' || CSV_GRP.GRP_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COLLECT_CSV_GRP_MST CSV_GRP ";
        $query .= " WHERE ";
        $query .= "     CSV_GRP.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     CSV_GRP.SCHOOL_KIND = '".$model->school_kind."' AND ";
        $query .= "     CSV_GRP.YEAR        = '".$model->year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //新規 -- COLLECT_CSV_HEAD_CAPTURE_DAT
    function csvHeadDelIns($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjp737Query::getCsvHeadDelSql($model);
        $db->query($query);

        foreach ($model->dataArr as $key => $val) {
            $data = array();
            $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]      = $model->school_kind;
            $data["YEAR"][TEXT]             = $model->year;
            $data["ROW_NO"][TEXT]           = $key + 1;
            $data["HEAD_NAME"][TEXT]        = $val;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "COLLECT_CSV_HEAD_CAPTURE_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //削除 -- COLLECT_CSV_HEAD_CAPTURE_DAT
    function getCsvHeadDelSql($model) {

        $query  = " DELETE FROM ";
        $query .= "     COLLECT_CSV_HEAD_CAPTURE_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
        $query .= "     YEAR        = '".$model->year."' ";

        return $query;
    }

    //更新処理
    function &getInsertQuery($model) {
        //削除 -- COLLECT_CSV_INFO_DAT
        knjp737Query::getDeleteQuery($model);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新 -- COLLECT_CSV_INFO_DAT
        if ($model->row_no_cnt > 0 && $model->s_row_no > 0) {
            for ($i = $model->s_row_no; $i < ($model->row_no_cnt + $model->s_row_no); $i++) {
                $data = array();
                $data["SCHOOLCD"][TEXT]         = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]      = $model->school_kind;
                $data["YEAR"][TEXT]             = $model->year;
                $data["ROW_NO"][TEXT]           = $i;
                $data["GRP_CD"][TEXT]           = $model->field["GRP_CD_".$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "COLLECT_CSV_INFO_DAT");
                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //削除 -- COLLECT_CSV_INFO_DAT
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     COLLECT_CSV_INFO_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
        $query .= "     YEAR        = '".$model->year."' ";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

/**** 前年度コピー処理 ****/
    //今年度のデータが存在するかチェック
    function CheckData($model, $tableNm, $year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM {$tableNm} ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND YEAR        = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO COLLECT_CSV_HEAD_CAPTURE_DAT ";
        $query .= "   (SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         '".$model->year."', ";
        $query .= "         ROW_NO, ";
        $query .= "         HEAD_NAME, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_CSV_HEAD_CAPTURE_DAT ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND YEAR        = '".($model->year - 1)."' ";
        $query .= "     ) ";
        $db->query($query);

        $query  = " INSERT INTO COLLECT_CSV_INFO_DAT ";
        $query .= "   (SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         '".$model->year."', ";
        $query .= "         ROW_NO, ";
        $query .= "         GRP_CD, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_CSV_INFO_DAT ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND YEAR        = '".($model->year - 1)."' ";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
