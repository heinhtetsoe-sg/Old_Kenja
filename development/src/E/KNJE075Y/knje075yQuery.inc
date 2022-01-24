<?php

require_once('for_php7.php');

class knje075yQuery extends Query {

    //学年取得
    function getGrade()
    {
        $query .= " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = 'J' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR = '".CTRL_YEAR."'";
            $query .= "AND SEMESTER = '".CTRL_SEMESTER."'";
            $query .= "AND GRADE='".$model->field["GRADE"]."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
            $query .= "AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "AND GRADE='".$model->field["GRADE"]."'";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒一覧
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO ";
        $query .= "     || ' ' ";
        $query .= "     || SCHREG_REGD_HDAT.HR_NAME ";
        $query .= "     || ATTENDNO ";
        $query .= "     || '番' ";
        $query .= "     || ' ' ";
        $query .= "     || NAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " INNER JOIN SCHREG_REGD_DAT ";
        $query .= "     ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT ";
        $query .= "     ON SCHREG_REGD_HDAT.YEAR     = SCHREG_REGD_DAT.YEAR ";
        $query .= "    AND SCHREG_REGD_HDAT.SEMESTER = SCHREG_REGD_DAT.SEMESTER ";
        $query .= "    AND SCHREG_REGD_HDAT.GRADE    = SCHREG_REGD_DAT.GRADE ";
        $query .= "    AND SCHREG_REGD_HDAT.HR_CLASS = SCHREG_REGD_DAT.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         (((SCHREG_REGD_DAT.YEAR)     = '{$model->control["年度"]}') ";
        $query .= "     AND  ((SCHREG_REGD_DAT.SEMESTER) = '{$model->control["学期"]}') ";
        $query .= "     AND  ((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS) = '{$model->field["GRADE_HR_CLASS"]}')) ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    //出力対象一覧
    function getList2($schregno, $model) {
        $query  = " SELECT ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO ";
        $query .= "     || ' ' ";
        $query .= "     || SCHREG_REGD_HDAT.HR_NAME ";
        $query .= "     || ATTENDNO ";
        $query .= "     || '番' ";
        $query .= "     || ' ' ";
        $query .= "     || NAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " INNER JOIN SCHREG_REGD_DAT ";
        $query .= "     ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT ";
        $query .= "     ON SCHREG_REGD_HDAT.YEAR     = SCHREG_REGD_DAT.YEAR ";
        $query .= "    AND SCHREG_REGD_HDAT.SEMESTER = SCHREG_REGD_DAT.SEMESTER ";
        $query .= "    AND SCHREG_REGD_HDAT.GRADE    = SCHREG_REGD_DAT.GRADE ";
        $query .= "    AND SCHREG_REGD_HDAT.HR_CLASS = SCHREG_REGD_DAT.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         (((SCHREG_REGD_DAT.YEAR)     = '{$model->control["年度"]}') ";
        $query .= "     AND  ((SCHREG_REGD_DAT.SEMESTER) = '{$model->control["学期"]}')) ";
        $query .= "     AND SCHREG_BASE_MST.SCHREGNO = '{$schregno}'  ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    function getSchoolName() {
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

}
?>
