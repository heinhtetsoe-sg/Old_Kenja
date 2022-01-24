<?php

require_once('for_php7.php');

class knjd234eQuery extends Query {

    // //名称マスタ
    // function getNameMst($model, $namecd1) {
    //     $query .= " SELECT ";
    //     $query .= "     NAMECD2 AS VALUE, ";
    //     $query .= "     NAME1 AS LABEL ";
    //     $query .= " FROM ";
    //     $query .= "     V_NAME_MST ";
    //     $query .= " WHERE ";
    //     $query .= "     YEAR = '".CTRL_YEAR."' ";
    //     $query .= "     AND NAMECD1 = '{$namecd1}' ";
    //     $query .= " ORDER BY ";
    //     $query .= "     VALUE ";
    // 
    //     return $query;
    // }

    //学期を取得
    function getSemester($model, $setsemes="") {
        $query .= " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE, ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($setsemes != "") {
            $query .= "  AND SEMESTER = '".$setsemes."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学校校種取得
    function getSchoolkindQuery($grade) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.GRADE = '".$grade."' ";

        return $query;
    }

    // //テスト種別
    // function getTest($model) {
    //     $query  = " SELECT ";
    //     $query .= "     T1.YEAR, ";
    //     $query .= "     T1.SEMESTER, ";
    //     $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
    //     $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || L1.TESTITEMNAME AS LABEL ";
    //     $query .= " FROM ";
    //     $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
    //     $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
    //     $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
    //     $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
    //     $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
    //     $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
    //     $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR ";
    //     $query .= "                                   AND L2.GRADE = '".$model->field["GRADE"]."' ";
    //     $query .= "                                   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
    //     $query .= " WHERE ";
    //     $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
    //     $query .= "     AND T1.SEMESTER     = '{$model->semester}' ";
    //     $query .= "     AND T1.CLASSCD      = '00' ";
    //     $query .= "     AND T1.CURRICULUM_CD  = '00' ";
    //     $query .= "     AND T1.SUBCLASSCD  = '000000' ";
    //     $query .= " ORDER BY ";
    //     $query .= "     VALUE ";
    // 
    //     return $query;
    // }

    // //学期詳細マスタ(日付の取得)
    // function getSemesterDetailMst($semesterDetail) {
    //     $query  = " SELECT ";
    //     $query .= "     SEMESTER, ";
    //     $query .= "     SEMESTER_DETAIL, ";
    //     $query .= "     SEMESTERNAME, ";
    //     $query .= "     VALUE(SDATE, '9999-12-31') AS SDATE, ";
    //     $query .= "     VALUE(EDATE, '9999-12-31') AS EDATE ";
    //     $query .= " FROM ";
    //     $query .= "     SEMESTER_DETAIL_MST ";
    //     $query .= " WHERE ";
    //     $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
    //     $query .= "     SEMESTER_DETAIL = '{$semesterDetail}' ";
    // 
    //     return $query;
    // }

    // function get_semester_detail($model) {
    //     $query  = " SELECT ";
    //     $query .= "     SEMESTER_DETAIL ";
    //     $query .= " FROM ";
    //     $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
    //     $query .= " WHERE ";
    //     $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
    //     $query .= "     SEMESTER = '{$model->semester}' AND ";
    //     $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$model->test_cd}' ";
    // 
    //     return $query;
    // }

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || ':' || GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組リスト
    function getHrClassAuth($model, $year,$semester,$auth_user,$staffcd,$grade="")
    {
        //参照・更新可
        if ($auth_user == DEF_REFERABLE || $auth_user == DEF_UPDATABLE){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
            if ($grade != "") {
                $query .= " AND T1.GRADE='".$grade."' ";
            }
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加 naka 05/01/05
        if ($auth_user == DEF_REFER_RESTRICT || $auth_user == DEF_UPDATE_RESTRICT){
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE T1.YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= " AND (TR_CD1 = '" .$staffcd ."' ";
            $query .= " OR TR_CD2 = '" .$staffcd ."' ";
            $query .= " OR TR_CD3 = '" .$staffcd ."' ";
            $query .= " OR SUBTR_CD1 = '" .$staffcd ."' ";
            $query .= " OR SUBTR_CD2 = '" .$staffcd ."' ";
            $query .= " OR SUBTR_CD3 = '" .$staffcd ."') ";
            if ($grade != "") {
                $query .= " AND T1.GRADE='".$grade."' ";
            }
        }

        return $query;
    }
}
?>
