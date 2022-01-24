<?php

require_once('for_php7.php');

class knjd233vQuery extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //卒業可能学年
    function getGraduate($model, $grade)
    {
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     CASE WHEN T1.GRADE BETWEEN T2.NAMESPARE2 AND T2.NAMESPARE3 THEN 1 END AS IS_GRD ";
        $query .= " FROM SCHREG_REGD_GDAT T1 ";
        $query .= " INNER JOIN NAME_MST T2 ON T2.NAMECD1 = 'A023' ";
        $query .= "     AND T2.NAME1 = T1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".$grade."' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getClass($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
                $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
                $query .= "     ON REGD.YEAR = T1.YEAR ";
                $query .= "     AND REGD.SEMESTER = T1.SEMESTER ";
                $query .= "     AND REGD.GRADE = T1.GRADE ";
                $query .= "     AND REGD.HR_CLASS = T1.HR_CLASS ";
                $query .= "     AND REGD.COURSECD || '-' || REGD.MAJORCD = '".$model->field["MAJOR"]."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."'";
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= " GROUP BY ";
            $query .= "     T1.GRADE || T1.HR_CLASS , ";
            $query .= "     T1.HR_NAME ";
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE || T1.HR_CLASS ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
                $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
                $query .= "     ON REGD.YEAR = T1.YEAR ";
                $query .= "     AND REGD.SEMESTER = T1.SEMESTER ";
                $query .= "     AND REGD.GRADE = T1.GRADE ";
                $query .= "     AND REGD.HR_CLASS = T1.HR_CLASS ";
                $query .= "     AND REGD.COURSECD || '-' || REGD.MAJORCD = '".$model->field["MAJOR"]."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."'";
            $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
            $query .= "     AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD3 = '" .STAFFCD ."') ";
            $query .= " GROUP BY ";
            $query .= "     T1.GRADE || T1.HR_CLASS , ";
            $query .= "     T1.HR_NAME ";
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE || T1.HR_CLASS ";
        }
        return $query;
    }

    function getCourseMajor($model) {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE, ";
        $query .= "     A043.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST VCM ";
        $query .= "     LEFT JOIN NAME_MST A043 ON NAMECD1 = 'A043' ";
        $query .= "          AND VCM.COURSECD || VCM.MAJORCD = A043.NAME2 || A043.NAME3 ";
        $query .= " WHERE ";
        $query .= "     VCM.YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= " WHERE ";
        $query .= "     VALUE(NAME1, 'H') = '".SCHOOLKIND."' ";
        return $query;
    }

}
?>
