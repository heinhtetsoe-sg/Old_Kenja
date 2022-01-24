<?php

require_once('for_php7.php');

class knjd656bQuery extends Query {
    //学期取得
    function getSemester() {
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

    //年組取得（権限チェック）
    function getGradeHrClass($semester, $model, $div = "") {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE AS LABEL ";
        } else {
            $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        if ($div != "GRADE") {
            $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試グループ
    function getGroupcd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GROUPCD AS VALUE, ";
        $query .= "     T1.GROUPCD || ' ' || L1.GROUPNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_GROUP_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MOCK_GROUP_MST L1 ON  L1.GROUP_DIV   = T1.GROUP_DIV ";
        $query .= "                       AND L1.STF_AUTH_CD = T1.STF_AUTH_CD ";
        $query .= "                       AND L1.GROUPCD     = T1.GROUPCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '". CTRL_YEAR ."' AND ";
        $query .= "     T1.GROUP_DIV   = '{$model->group_div}' AND ";
        $query .= "     T1.TARGET_DIV  = '{$model->target_div}' AND ";
        $query .= "     T1.STF_AUTH_CD = '{$model->stf_auth_cd}' AND ";
        $query .= "     SUBSTR(T1.MOCK_TARGET_CD, 1, 1) = '4' ";

        return $query;
    }

    //模試グループ
    function getMockcd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.MOCK_TARGET_CD AS VALUE, ";
        $query .= "     T1.MOCK_TARGET_CD || ' ' || L1.MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_GROUP_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MOCK_MST L1 ON  L1.MOCKCD = T1.MOCK_TARGET_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '". CTRL_YEAR ."' AND ";
        $query .= "     T1.GROUP_DIV   = '{$model->group_div}' AND ";
        $query .= "     T1.TARGET_DIV  = '{$model->target_div}' AND ";
        $query .= "     T1.STF_AUTH_CD = '{$model->stf_auth_cd}' AND ";
        $query .= "     T1.GROUPCD     = '{$model->field["GROUPCD"]}' AND ";
        $query .= "     SUBSTR(T1.MOCK_TARGET_CD, 1, 1) = '4' ";

        return $query;
    }

    //対象生徒
    function getStudent($model, $seme) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '{$seme}' ";
        $query .= "     AND T2.GRADE || '-' || T2.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }
}
?>
