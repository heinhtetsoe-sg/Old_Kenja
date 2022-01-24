<?php

require_once('for_php7.php');

class knjd107Query extends Query {
    //学期を取得
    function getSemester() {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query .= " SELECT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_GDAT L1 ON  L1.YEAR = T1.YEAR ";
            $query .= "                         AND L1.GRADE = T1.GRADE ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."'  AND ";
            $query .= "     T1.SEMESTER = '{$model->semester}' AND ";
            $query .= "     ( ";
            $query .= "     L1.SCHOOL_KIND = 'H' OR ";
            $query .= "     L1.SCHOOL_KIND = 'J' ";
            $query .= "     ) ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " SELECT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_GDAT L1 ON  L1.YEAR = T1.YEAR ";
            $query .= "                         AND L1.GRADE = T1.GRADE ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."'  AND ";
            $query .= "     T1.SEMESTER = '{$model->semester}' AND ";
            $query .= "     ( ";
            $query .= "     TR_CD1    = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD3 = '" .STAFFCD ."') AND ";
            $query .= "     ( ";
            $query .= "     L1.SCHOOL_KIND = 'H' OR ";
            $query .= "     L1.SCHOOL_KIND = 'J' ";
            $query .= "     ) ";
        }

        return $query;
    }

    //学年名称
    function getSchregRegdGdat() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND (SCHOOL_KIND = 'H' ";
        $query .= "     OR  SCHOOL_KIND = 'J') ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".CTRL_YEAR."'      AND ";
        $query .= "     T1.SEMESTER             = '{$model->semester}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '{$model->hrClass}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";
        return $query;
    }

    //SCHOOL_KINDを取得
    function getSchoolKing($model) {
        $query .= " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE = '".substr($model->hrClass, 0, 2)."' ";

        return $query;
    }
}
?>
