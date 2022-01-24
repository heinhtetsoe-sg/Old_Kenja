<?php

require_once('for_php7.php');

class knjz066Query extends Query {
    //学部取得
    function getSchoolKind($name1="") {
        $query  = " SELECT ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME2 AS GRADE_FROM, ";
        $query .= "     NAME3 AS GRADE_TO ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($name1) {
            $query .= "     AND NAME1 = '".$name1."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //状態区分取得
    function getCondition() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A033' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ取得
    function getGradeKindAssessDat($model, $flg) {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     GRADE_KIND_ASSESS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     CONDITION   = '".$model->condition."' ";
        if ($flg != "cnt") {
            $query .= "     AND ASSESSLEVEL = ".$flg." ";
        }

        return $query;
    }
    
    function getGradeName1($grade){
        $query .= " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";
        
        return $query;
    }

    //更新
    function &getInsertQuery($model) {
        //削除
        knjz066Query::getDeleteQuery($model);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新
        if ($model->levelcnt > 0) {
            for ($i = 1; $i <= $model->levelcnt; $i++) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SCHOOL_KIND"][TEXT]      = $model->school_kind;
                $data["GRADE"][TEXT]            = $model->grade;
                $data["CONDITION"][TEXT]        = $model->condition;
                $data["ASSESSLEVEL"][NUMBER]    = $i;
                $data["ASSESSMARK"][TEXT]       = $model->field["ASSESSMARK_".$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "GRADE_KIND_ASSESS_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     GRADE_KIND_ASSESS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     CONDITION   = '".$model->condition."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //データの存在チェック（コピー用）
    function chkCopyData($year, $flg) {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     GRADE_KIND_ASSESS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        return $query;
    }

    //コピー
    function &getCopyQuery($Row) {
        $data = array();
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["SCHOOL_KIND"][TEXT]      = $Row["SCHOOL_KIND"];
        $data["GRADE"][TEXT]            = $Row["GRADE"];
        $data["CONDITION"][TEXT]        = $Row["CONDITION"];
        $data["ASSESSLEVEL"][NUMBER]    = $Row["ASSESSLEVEL"];
        $data["ASSESSMARK"][TEXT]       = $Row["ASSESSMARK"];
        $data["ASSESSLOW"][TEXT]        = $Row["ASSESSLOW"];
        $data["ASSESSHIGH"][TEXT]       = $Row["ASSESSHIGH"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "GRADE_KIND_ASSESS_DAT");

        return $query;
    }
}
?>
