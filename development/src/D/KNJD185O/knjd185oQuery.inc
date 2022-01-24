<?php

require_once('for_php7.php');


class knjd185oQuery extends Query {

    //対象外の生徒取得
    function getSchnoIdou($model)
    {
        $date = str_replace("/","-",$model->field["DATE"]);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //生徒リスト取得
    function getListStudent($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T1 ON T1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "           AND T2.GRADE = T1.GRADE ";
            $query .= "           AND T2.SCHOOL_KIND = 'H' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR='" .CTRL_YEAR."'";
            $query .= "     AND T1.SEMESTER='".$model->field["SEMESTER"] ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "           AND T2.GRADE = T1.GRADE ";
            $query .= "           AND T2.SCHOOL_KIND = 'H' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR='" .CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER='".$model->field["SEMESTER"] ."' ";
            $query .= "     AND (T1.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR T1.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR T1.TR_CD3 = '" .STAFFCD ."' ";
            $query .= "          OR T1.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR T1.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR T1.SUBTR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //学期名取得
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

}
?>
