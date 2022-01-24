<?php

require_once('for_php7.php');


class knjg100Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model) {
        $query  = " SELECT ";
        $query .= "    REGD_G.GRADE_NAME1 AS LABEL, ";
        $query .= "    REGD_G.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT REGD_G ";
        $query .= " WHERE ";
        $query .= "         REGD_G.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND REGD_G.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getHrClassAuth($grade="", $model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H ";
            $query .= " WHERE ";
            $query .= "     REGD_H.YEAR = '" .CTRL_YEAR ."' ";
            $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER ."'";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                              REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                              AND REGD_G.SCHOOL_KIND = '" .$model->field["SCHKIND"] ."') ";
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                              REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                              AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            if ($grade) {
                $query .= "  AND REGD_H.GRADE = '{$grade}'";
            }
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加 naka 05/01/05
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= " REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD_H";
            $query .= " WHERE ";
            $query .= "     REGD_H.YEAR = '" .CTRL_YEAR ."' ";
            $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER ."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                              REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                              AND REGD_G.SCHOOL_KIND = '" .$model->field["SCHKIND"] ."') ";
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                              REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                              AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            if ($grade) {
                $query .= "  AND REGD_H.GRADE = '{$grade}'";
            }
            $query .= " AND (REGD_H.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "   OR REGD_H.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "   OR REGD_H.TR_CD3 = '" .STAFFCD ."' ";
            $query .= "   OR REGD_H.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "   OR REGD_H.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "   OR REGD_H.SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    function getClassFormation($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     '新入生' || (CASE WHEN T3.GRADE_NAME1 IS NOT NULL THEN '(' || T3.GRADE_NAME1 || ')' ELSE '' END)AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT T1 ";
        $query .= "     INNER JOIN FRESHMAN_DAT T2 ON T2.ENTERYEAR = T1.YEAR ";
        $query .= "                                AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR ";
        $query .= "                                AND T3.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->setYear."' ";
        $query .= "     AND T1.SEMESTER = '1'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND T3.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($grade) {
            $query .= "  AND T1.GRADE = '{$grade}'";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //生徒データ
    function getSchno($model)
    {
        if ($model->field["KUBUN"] == 2) {
            $query  = "SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW,T2.GRADE,T2.HR_CLASS,T3.HR_NAME ";
            $query .= "FROM SCHREG_BASE_MST T1,SCHREG_REGD_DAT T2,SCHREG_REGD_HDAT T3 ";
            $query .= "WHERE T2.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "      T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "      T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
            $query .= "      T2.SCHREGNO = T1.SCHREGNO AND ";
            $query .= "      T2.YEAR = T3.YEAR AND T2.SEMESTER = T3.SEMESTER AND ";
            $query .= "      T2.GRADE = T3.GRADE AND T2.HR_CLASS = T3.HR_CLASS ";
            $query .= "ORDER BY T2.ATTENDNO ";
        } else {
            $query  = " SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     '01' AS GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     CASE WHEN ATTENDNO IS NOT NULL THEN ATTENDNO ELSE ' 　 ' END AS ATTENDNO, ";
            $query .= "     NAME AS NAME_SHOW, ";
            $query .= "     CASE WHEN HR_CLASS IS NOT NULL THEN '01年' || HR_CLASS || '組' ELSE '　　 　　' END AS HR_NAME ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTERYEAR = '".$model->setYear."' ";
            $query .= " ORDER BY ";
            $query .= "     HR_CLASS, ";
            $query .= "     ATTENDNO, ";
            $query .= "     NAME_KANA ";
        }

        return $query;
    }

}
?>
