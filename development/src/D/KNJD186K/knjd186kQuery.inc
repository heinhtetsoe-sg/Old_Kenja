<?php

require_once('for_php7.php');

class knjd186kQuery extends Query {

    //IBコース一覧取得
    function getIBPrgCourse($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A034' AND ";
        $query .= "     NAMECD2 <> '0' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }    

    //MAX学期を取得
    function getMaxSemester() {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    //学期を取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER IN ('1', '9') ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学年一覧取得
    function getIBGrade($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                            AND L1.NAMECD1 = 'A034' ";
        $query .= "                            AND L1.NAME1 = '".$model->ibprg_course."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.GRADE BETWEEN L1.NAME2 AND L1.NAME3 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得(権限チェック)
    function getAuth($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR          = '".CTRL_YEAR."' ";
        if ($model->semester == '9') {
            $query .= "     AND T1.SEMESTER  = '". CTRL_SEMESTER ."' ";
        } else {
            $query .= "     AND T1.SEMESTER  = '".$model->semester."' ";
        }
        $query .= "     AND T2.YEAR          = T1.YEAR ";
        $query .= "     AND T2.GRADE         = T1.GRADE ";
        $query .= "     AND T2.GRADE         = '".$model->grade."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   T1.TR_CD1    = '" .STAFFCD ."' ";
            $query .= "          OR T1.TR_CD2    = '" .STAFFCD ."' ";
            $query .= "          OR T1.TR_CD3    = '" .STAFFCD ."' ";
            $query .= "          OR T1.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR T1.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR T1.SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒取得
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO || '　' || T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = '".CTRL_YEAR."' AND ";
        if ($model->semester == '9') {
            $query .= "     T1.SEMESTER         = '". CTRL_SEMESTER ."' AND ";
        } else {
            $query .= "     T1.SEMESTER         = '".$model->semester."' AND ";
        }
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->hrClass."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }
}
?>
