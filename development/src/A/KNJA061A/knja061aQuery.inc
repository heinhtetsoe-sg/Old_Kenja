<?php

require_once('for_php7.php');


class knja061aQuery extends Query {

    //名称マスタ取得
    function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1   AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
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

    //学年取得
    function getGrade($model) {

        $query = "";
        $query .= " WITH GRADUATE AS ( ";
        $query .= "   SELECT ";
        $query .= "       NAMESPARE2 AS GRADE ";
        $query .= "   FROM ";
        $query .= "       NAME_MST ";
        $query .= "   WHERE ";
        $query .= "         NAMECD1 = 'A023' ";
        $query .= "     AND NAME1 = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        //卒業生は除く
        $query .= "     INNER JOIN GRADUATE ON T1.GRADE < GRADUATE.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程取得
    function getCourse($year) {

        $query  = " SELECT ";
        $query .= "     COURSECD AS VALUE, ";
        $query .= "     COURSENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;

    }

    //年組取得（権限チェック）
    function getHrClass($year, $semester, $model)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->field["DISP"] == 1) {
            $query .= " AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象生徒
    function getStudent($model, $seme)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO || '番　' || T1.NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "         ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_SEND_ADDRESS_DAT S1 ON S1.SCHREGNO = T2.SCHREGNO AND S1.DIV = '1' ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$seme."' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";

        //性別
        if ($model->field["SEX"] == "1" || $model->field["SEX"] == "2") {
            $query .= "     AND T1.SEX = '".$model->field["SEX"]."' ";
        }
        //通学＆寮生
        if ($model->field["OUTPUT"] == "1") {
            $query .= "     AND T1.SCHREGNO IN ( ";
            $query .= "             SELECT SCHREGNO FROM SCHREG_DOMITORY_HIST_DAT ";
            $query .= "         ) ";
        } else if ($model->field["OUTPUT"] == "2") {
            $query .= "     AND T1.SCHREGNO NOT IN ( ";
            $query .= "             SELECT SCHREGNO FROM SCHREG_DOMITORY_HIST_DAT ";
            $query .= "         ) ";
        }

        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //新入生
    function getFreshman($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCHREGNO || '　' || T1.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     FRESHMAN_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTERYEAR = '".(CTRL_YEAR + 1)."' ";

        //課程
        if ($model->field["COURSECD"]) {
            $query .= "     AND T1.COURSECD = '".$model->field["COURSECD"]."' ";
        }
        //性別
        if ($model->field["SEX"] == "1" || $model->field["SEX"] == "2") {
            $query .= "     AND T1.SEX = '".$model->field["SEX"]."' ";
        }

        $query .= " ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";

        return $query;
    }
}
?>
