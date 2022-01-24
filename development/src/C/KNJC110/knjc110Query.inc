<?php

require_once('for_php7.php');

class knjc110Query extends Query {
    //対象者リスト
    function getCategoryNames($model) {
        $query  = " SELECT ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO AS VALUE, ";
        $query .= "     SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         (((SCHREG_REGD_DAT.YEAR)     = '{$model->control["年度"]}') ";
        $query .= "     AND  ((SCHREG_REGD_DAT.SEMESTER) = '{$model->control["学期"]}') ";
        $query .= "     AND  ((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS) = '{$model->field["GRADE_HR_CLASS"]}')) ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }


    //年組取得（権限チェック）
    function getAuth($year,$semester) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR   = '{$year}' ";
            $query .= "AND SEMESTER = '{$semester}' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR    = '{$year}' ";
            $query .= " AND SEMESTER = '{$semester}' ";
            $query .= " AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "      OR TR_CD2 = '".STAFFCD."' ";
            $query .= "      OR TR_CD3 = '".STAFFCD."') ";
        }

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth2($year) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR = '{$year}' ";
            $query .= "GROUP BY GRADE || HR_CLASS ,HR_NAME ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR = '{$year}' ";
            $query .= " AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "      OR TR_CD2 = '".STAFFCD."' ";
            $query .= "      OR TR_CD3 = '".STAFFCD."') ";
            $query .= "GROUP BY GRADE || HR_CLASS ,HR_NAME ";
        }

        return $query;
    }

    //ABSENT_COV(学期で清算 or 年間で清算のフラグ)取得
    function getAbsent() {
        $query  = " SELECT ";
        $query .= "     ABSENT_COV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }
}
?>
