<?php

require_once('for_php7.php');

class knjf175bQuery extends Query {
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
        $query .= "     NAME1 ";

        return $query;
    }

    //学校マスタ学校名
    function getSchoolMst($model, $year) {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR='{$year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        return $query;
    }
 
    //学期名取得
    function getSemeName() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //印影カウント
    function getStampCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PRG_STAMP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '9' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '000000000000' ";
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '000000000000' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND PROGRAMID   = 'KNJF175B' ";

        return $query;
    }

    //印影名称取得
    function getStampName($model) {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     TITLE ";
        $query .= " FROM ";
        $query .= "     PRG_STAMP_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER    = '9' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '000000000000' ";
            $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '000000000000' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND PROGRAMID   = 'KNJF175B' ";

        return $query;
    }
}
?>