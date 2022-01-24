<?php

require_once('for_php7.php');

class knja139bquery extends Query {
    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND GRADE IN (  SELECT ";
            $query .= "                         GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_HDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                        (TR_CD1 = '".STAFFCD."' OR ";
            $query .= "                         TR_CD2 = '".STAFFCD."' OR ";
            $query .= "                         TR_CD3 = '".STAFFCD."')) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //種別
    function getA040($div = "") {
        $query  = " SELECT ";
        if ($div == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A040' ";
        if ($div != "cnt") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //所見取得
    function getHtrainremarkTempDat($model, $pattern_cd, $flg) {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' AND ";
        $query .= "     DATA_DIV    = '".$model->field["DATA_DIV"]."' AND ";
        $query .= "     PATTERN_CD  = '".$pattern_cd."' ";

        return $query;
    }

    //データ数
    function getDataCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE       = '{$model->field["GRADE"]}' ";
        $query .= "     AND DATA_DIV    = '{$model->field["DATA_DIV"]}' ";

        return $query;
    }

    //更新処理
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= "     AND DATA_DIV = '{$model->field["DATA_DIV"]}' ";

        $db->query($query);

        for ($i = 0; $i < $model->field["DATA_CNT"]; $i++) {
            //データセット
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["GRADE"][TEXT]        = $model->field["GRADE"];
            $data["DATA_DIV"][TEXT]     = $model->field["DATA_DIV"];
            $data["PATTERN_CD"][TEXT]   = $model->pattern_cd[$i];
            $data["REMARK"][TEXT]       = $model->fields[$i]["REMARK"];
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "HTRAINREMARK_TEMP_DAT");

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //削除
    function getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' AND ";
        $query .= "     DATA_DIV    = '".$model->field["DATA_DIV"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //データ取得 -- HTRAINREMARK_TEMP_DAT
    function checkHtrainremarkTempDat($model, $year, $flg) {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     GRADE IN (  SELECT ";
        $query .= "                     GRADE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_GDAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                 AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "                 AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "              ) ";

        return $query;
    }

    //事前チェック -- SCHREG_REGD_GDAT
    function checkSchregRegdGdat() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //コピー -- HTRAINREMARK_TEMP_DAT
    function &copyUpdateQuery($Row) {
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["GRADE"][TEXT]            = $Row["GRADE"];
        $data["DATA_DIV"][TEXT]         = $Row["DATA_DIV"];
        $data["PATTERN_CD"][TEXT]       = $Row["PATTERN_CD"];
        $data["REMARK"][TEXT]           = $Row["REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "HTRAINREMARK_TEMP_DAT");
        return $query;
    }
}
?>
