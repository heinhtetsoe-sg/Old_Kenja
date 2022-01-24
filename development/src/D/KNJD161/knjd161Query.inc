<?php

require_once('for_php7.php');

class knjd161Query extends Query {

    //年組取得（権限チェック）
    function getAuth($model, $year,$semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE, T1.HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            $query .= "LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "WHERE T1.YEAR='" .$year ."'";
            $query .= "  AND T1.SEMESTER='".$semester ."'";
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    }
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            }
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE, T1.HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            $query .= "LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "WHERE T1.YEAR='" .$year ."'";
            $query .= "  AND T1.SEMESTER='".$semester ."'";
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    }
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            }
            $query .= " AND (T1.TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR T1.TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR T1.TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //テスト種別取得
    function getTest($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || '  ' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $model->testTable ;
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND SEMESTER = '".$model->field["SEMESTER"] ."' ";
            if ($model->field["SEMESTER"] == "9") {
                $query .= "     AND TESTITEMCD = '00' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

}
?>
