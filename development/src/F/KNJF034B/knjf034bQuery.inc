<?php

require_once('for_php7.php');


class knjf034bQuery extends Query {
    //生徒取得、（実クラス）特別支援学校対応
    function GetS_S_Students($model) {
        $query  = " SELECT ";
        $query .= "     L1.SCHREGNO AS SCHREGNO, ";
        $query .= "     L1.SCHREGNO || '　' || L1.GHR_ATTENDNO || '番' || '　' || T2.NAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "   SCHREG_BASE_MST T2,";
        $query .= "   SCHREG_REGD_GHR_DAT L1 ";
        $query .= " WHERE ";
        $query .= "         L1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND L1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND L1.GHR_CD   = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND L1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     L1.GHR_CD, ";
        $query .= "     L1.GHR_ATTENDNO ";

        return $query;
    }

    //生徒取得（FI）
    function getFiStudents($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO AS SCHREGNO, ";
        $query .= "     T2.SCHREGNO || '　' || T2.ATTENDNO || '番' || '　' || T1.NAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_FI_DAT T2 ";
        $query .= " WHERE ";
        $query .= "         T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //クラス生徒取得
    function getClassStudents($model)
    {
        $query  = " SELECT ";
        $query .= "     L1.SCHREGNO AS SCHREGNO, ";
        $query .= "     L1.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         L1.YEAR     ='" .$model->control["年度"] ."' ";
        $query .= "     AND L1.SEMESTER ='" .$model->control["学期"] ."' ";
        $query .= "     AND L1.GRADE || L1.HR_CLASS = '" .$model->field["GRADE_HR_CLASS"] ."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND L1.GRADE IN (SELECT ";
                $query .= "                              REGD_G.GRADE ";
                $query .= "                      FROM ";
                $query .= "                          SCHREG_REGD_GDAT REGD_G ";
                $query .= "                      WHERE ";
                $query .= "                          REGD_G.YEAR = '".$model->control["年度"] ."' ";
                $query .= "                          AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND L1.GRADE IN (SELECT ";
            $query .= "                              REGD_G.GRADE ";
            $query .= "                      FROM ";
            $query .= "                          SCHREG_REGD_GDAT REGD_G ";
            $query .= "                      WHERE ";
            $query .= "                          REGD_G.YEAR = '".$model->control["年度"] ."' ";
            $query .= "                          AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    //年組取得(実クラス)
    function getGhrCd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GHR_CD AS VALUE, ";
        $query .= "     T1.GHR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD2    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD3    = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GHR_CD ";

        return $query;
    }

    //FI複式年組コンボ
    function getFiGradeHrclass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.GRADE IN (SELECT ";
                $query .= "                          REGD_G.GRADE ";
                $query .= "                      FROM ";
                $query .= "                          SCHREG_REGD_GDAT REGD_G ";
                $query .= "                      WHERE ";
                $query .= "                              REGD_G.YEAR        = '".CTRL_YEAR."' ";
                $query .= "                          AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.GRADE IN (SELECT ";
            $query .= "                          REGD_G.GRADE ";
            $query .= "                      FROM ";
            $query .= "                          SCHREG_REGD_GDAT REGD_G ";
            $query .= "                      WHERE ";
            $query .= "                              REGD_G.YEAR        = '".CTRL_YEAR."' ";
            $query .= "                          AND REGD_G.SCHOOL_KIND = '".SCHOOLKIND."') ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD2    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD3    = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClassAuth($year, $semester, $auth_user, $staffcd, $model)
    {
        //参照・更新可
        if ($auth_user == DEF_REFERABLE || $auth_user == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "         REGD_H.YEAR     = '" .$year ."' ";
            $query .= "     AND REGD_H.SEMESTER = '".$semester ."'";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND REGD_H.GRADE IN (SELECT ";
                    $query .= "                              REGD_G.GRADE ";
                    $query .= "                          FROM ";
                    $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                          WHERE ";
                    $query .= "                                  REGD_G.YEAR        = '".$year ."' ";
                    $query .= "                              AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                              REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                                  REGD_G.YEAR        = '".$year ."' ";
                $query .= "                              AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加 naka 05/01/05
        if ($auth_user == DEF_REFER_RESTRICT || $auth_user == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "         REGD_H.YEAR     = '" .$year ."' ";
            $query .= "     AND REGD_H.SEMESTER = '".$semester ."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND REGD_H.GRADE IN (SELECT ";
                    $query .= "                              REGD_G.GRADE ";
                    $query .= "                          FROM ";
                    $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                          WHERE ";
                    $query .= "                                  REGD_G.YEAR        = '".$year ."' ";
                    $query .= "                              AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                              REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                                  REGD_G.YEAR        = '".$year ."' ";
                $query .= "                              AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            $query .= "     AND (REGD_H.TR_CD1    = '" .$staffcd ."' ";
            $query .= "       OR REGD_H.TR_CD2    = '" .$staffcd ."' ";
            $query .= "       OR REGD_H.TR_CD3    = '" .$staffcd ."' ";
            $query .= "       OR REGD_H.SUBTR_CD1 = '" .$staffcd ."' ";
            $query .= "       OR REGD_H.SUBTR_CD2 = '" .$staffcd ."' ";
            $query .= "       OR REGD_H.SUBTR_CD3 = '" .$staffcd ."') ";
        }

        return $query;
    }
}
?>
