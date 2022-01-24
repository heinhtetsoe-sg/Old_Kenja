<?php

require_once('for_php7.php');

class knjd193Query extends Query {
    //学期を取得
    function getSemester() {
        $query .= " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        $semester = ($model->semester == "9") ? CTRL_SEMESTER : $model->semester;
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query .= " SELECT ";
            $query .= "     RTRIM(CHAR(INT(T1.GRADE))) || '年生' AS LABEL, ";
            $query .= "     T1.GRADE AS VALUE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_GDAT L1 ON  L1.YEAR = T1.YEAR ";
            $query .= "                         AND L1.GRADE = T1.GRADE ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."'  AND ";
            $query .= "     T1.SEMESTER = '{$semester}' AND ";
            $query .= "     ( ";
            $query .= "     L1.SCHOOL_KIND = 'H' OR ";
            $query .= "     L1.SCHOOL_KIND = 'J' ";
            $query .= "     ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.GRADE ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " SELECT ";
            $query .= "     RTRIM(CHAR(INT(T1.GRADE))) || '年生' AS LABEL, ";
            $query .= "     T1.GRADE AS VALUE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_GDAT L1 ON  L1.YEAR = T1.YEAR ";
            $query .= "                         AND L1.GRADE = T1.GRADE ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."'  AND ";
            $query .= "     T1.SEMESTER = '{$semester}' AND ";
            $query .= "     ( ";
            $query .= "     TR_CD1    = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= "     OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "     OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR SUBTR_CD3 = '" .STAFFCD ."') AND ";
            $query .= "     ( ";
            $query .= "     L1.SCHOOL_KIND = 'H' OR ";
            $query .= "     L1.SCHOOL_KIND = 'J' ";
            $query .= "     ) ";
            $query .= " GROUP BY ";
            $query .= "     T1.GRADE ";
        }

        return $query;
    }

    //テスト種別取得
    function GetName($seme) {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD||TESTITEMCD AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER   = '$seme' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= " ORDER BY ";
        $query .= "     TESTKINDCD, ";
        $query .= "     TESTITEMCD ";

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        $semester = ($model->semester == "9") ? CTRL_SEMESTER : $model->semester;
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".CTRL_YEAR."'      AND ";
        $query .= "     T1.SEMESTER             = '{$semester}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '{$model->grade}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //SCHOOL_KINDを取得
    function getSchoolKing($model) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE = '{$model->grade}' ";

        return $query;
    }
}
?>
