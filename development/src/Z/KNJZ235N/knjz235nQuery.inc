<?php

require_once('for_php7.php');

class knjz235nQuery extends Query {

    //校種を取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     ABBV3 ";

        return $query;
    }

    //学期情報を取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER = '9' ";

        return $query;
    }
    
    //学期情報を取得
    function getSemesterAll() {
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= "   , SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //パターン取得
    function getPatern() {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A035' ";
        $query .= "     AND NAMESPARE3 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //データ取得
    function getHreportConditionDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->schoolKind != "") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     GRADE       = '".sprintf("%02d", "")."' AND ";
        $query .= "     COURSECD    = '".sprintf("%01d", "")."' AND ";
        $query .= "     MAJORCD     = '".sprintf("%03d", "")."' AND ";
        $query .= "     COURSECODE  = '".sprintf("%04d", "")."' ";
        $query .= " ORDER BY ";
        $query .= "     INT(SEQ) ";

        return $query;
    }

    //データ有無チェック
    function checkExistData($model, $year, $seq, $flg="") {
        if ($model->pattern == 'D154N') {
            $seq = sprintf("%03d", intval($seq) + 50);
        }
        $query  = " SELECT ";
        if ($flg == "pre_data") {
            $query .= "     * ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' ";
        if ($model->schoolKind != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schoolKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($flg == "") {
            $query .= " AND GRADE       = '".sprintf("%02d", "")."' ";
            $query .= " AND COURSECD    = '".sprintf("%01d", "")."' ";
            $query .= " AND MAJORCD     = '".sprintf("%03d", "")."' ";
            $query .= " AND COURSECODE  = '".sprintf("%04d", "")."' ";
            $query .= " AND SEQ         = '".$seq."' ";
        }

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 1; $i <= 22; $i++) {
            $seq = sprintf("%03d", $i);

            //存在チェック
            $query = knjz235nQuery::checkExistData($model, CTRL_YEAR, $seq);
            $flg = ($db->getOne($query) > 0) ? "update" : "insert";
            //更新
            $db->query(knjz235nQuery::updateHreportConditionDat($model, $seq, $flg));
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //更新 -- HREPORT_CONDITION_DAT
    function updateHreportConditionDat($model, $seq, $flg) {
        if ($model->pattern == 'D154N') {
            $seq = sprintf("%03d", intval($seq) + 50);
        }
        $data = array();
        if ($flg == "insert") {
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            if ($model->schoolKind != "") {
                $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]  = $model->schoolKind;
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
            }
            $data["GRADE"][TEXT]        = sprintf("%02d", "");
            $data["COURSECD"][TEXT]     = sprintf("%01d", "");
            $data["MAJORCD"][TEXT]      = sprintf("%03d", "");
            $data["COURSECODE"][TEXT]   = sprintf("%04d", "");

            $data["SEQ"][TEXT]          = $seq;
        }

        if ($model->pattern == 'D154N') {
            $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq];
        } else {
            if ($seq == "006") {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq."1"];
                $data["REMARK2"][TEXT]  = $model->field["SEQ".$seq."2"];
                $data["REMARK3"][TEXT]  = $model->field["SEQ".$seq."3"];
                $data["REMARK4"][TEXT]  = $model->field["SEQ".$seq."4"];
            } else if ($seq == "015") {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq."1"];
                $data["REMARK2"][TEXT]  = $model->field["SEQ".$seq."2"];
            } else {
                $data["REMARK1"][TEXT]  = $model->field["SEQ".$seq];
            }
        }

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($flg == "insert") {
            $query = Query::insertSQL($data, "HREPORT_CONDITION_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
            if ($model->schoolKind != "") {
                $where .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
                $where .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $where .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
                $where .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
            }
            $where .= "     GRADE       = '".sprintf("%02d", "")."' AND ";
            $where .= "     COURSECD    = '".sprintf("%01d", "")."' AND ";
            $where .= "     MAJORCD     = '".sprintf("%03d", "")."' AND ";
            $where .= "     COURSECODE  = '".sprintf("%04d", "")."' AND ";

            $where .= "     SEQ         = '".$seq."' ";

            $query = Query::updateSQL($data, "HREPORT_CONDITION_DAT", $where);
        }

        return $query;
    }

    //前年度のデータを今年度にINSERT
    function &copyInsert($db, $model, $Row) {
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     HREPORT_CONDITION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOLCD    = '".$Row["SCHOOLCD"]."' AND ";
            $query .= "     SCHOOL_KIND = '".$Row["SCHOOL_KIND"]."' AND ";
        }
        $query .= "     GRADE       = '".$Row["GRADE"]."' AND ";
        $query .= "     COURSECD    = '".$Row["COURSECD"]."' AND ";
        $query .= "     MAJORCD     = '".$Row["MAJORCD"]."' AND ";
        $query .= "     COURSECODE  = '".$Row["COURSECODE"]."' AND ";
        $query .= "     SEQ         = '".$Row["SEQ"]."' ";
        $db->query($query);

        //追加
        $data = array();
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = $Row["SCHOOLCD"];
            $data["SCHOOL_KIND"][TEXT]  = $Row["SCHOOL_KIND"];
        }
        $data["GRADE"][TEXT]        = $Row["GRADE"];
        $data["COURSECD"][TEXT]     = $Row["COURSECD"];
        $data["MAJORCD"][TEXT]      = $Row["MAJORCD"];
        $data["COURSECODE"][TEXT]   = $Row["COURSECODE"];
        $data["SEQ"][TEXT]          = $Row["SEQ"];
        $data["REMARK1"][TEXT]      = $Row["REMARK1"];
        $data["REMARK2"][TEXT]      = $Row["REMARK2"];
        $data["REMARK3"][TEXT]      = $Row["REMARK3"];
        $data["REMARK4"][TEXT]      = $Row["REMARK4"];
        $data["REMARK5"][TEXT]      = $Row["REMARK5"];
        $data["REMARK6"][TEXT]      = $Row["REMARK6"];
        $data["REMARK7"][TEXT]      = $Row["REMARK7"];
        $data["REMARK8"][TEXT]      = $Row["REMARK8"];
        $data["REMARK9"][TEXT]      = $Row["REMARK9"];
        $data["REMARK10"][TEXT]     = $Row["REMARK10"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "HREPORT_CONDITION_DAT");
        $db->query($query);

        $db->commit();
    }
}
?>
