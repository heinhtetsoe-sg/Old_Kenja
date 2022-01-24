<?php

require_once('for_php7.php');

class knjc037Query extends Query {

    //学期取得
    function getSelectSeme() {
        $year     = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //年組取得
    function getNenKumi($model) {
        $year     = CTRL_YEAR;
        $semester = $model->field["GAKKI"];
        $staffcd  = STAFFCD;

        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     GRADE || ':' || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR = '{$year}' ";
            $query .= "     AND SEMESTER = '{$semester}' ";
            $query .= " ORDER BY ";
            $query .= "     GRADE, HR_CLASS ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     GRADE || ':' || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR = '{$year}' ";
            $query .= "     AND SEMESTER = '{$semester}' ";
            $query .= "     AND (TR_CD1  = '{$staffcd}' ";
            $query .= "     OR TR_CD2    = '{$staffcd}' ";
            $query .= "     OR TR_CD3    = '{$staffcd}') ";
            $query .= " ORDER BY ";
            $query .= "     GRADE, HR_CLASS ";
        }

        return $query;
    }

    //チェックのための学期開始日、終了日取得
    function getSemester($gakki) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$gakki}' ";

        return $query;
    }

    //リスト作成
    function getSeitoItiran($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["GAKKI"]}' AND ";
        $query .= "     T1.GRADE    = '{$model->field["GRADE"]}' AND ";
        $query .= "     T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        $query .= " ORDER BY ";
        $query .= "     INT(T1.ATTENDNO), ";
        $query .= "     INT(T1.SCHREGNO) ";

        return $query;
    }
}
?>
