<?php

require_once('for_php7.php');


class knjf162Query extends Query {

    //学年取得
    function getSelectGrade($model) {
        $query  = " SELECT ";
        $query .= "    GRADE_NAME1 AS LABEL, ";
        $query .= "    GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getHrClassAuth($grade) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
            $query .= "  AND SEMESTER='".CTRL_SEMESTER ."'";
            $query .= "  AND GRADE='{$grade}'";
        }
        //参照・更新可（制限付き）//Modify 副担任コードの条件を追加 naka 05/01/05
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
            $query .= "  AND SEMESTER='".CTRL_SEMESTER ."' ";
            $query .= "  AND GRADE='{$grade}'";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "   OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "   OR TR_CD3 = '" .STAFFCD ."' ";
            $query .= "   OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "   OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "   OR SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        return $query;
    }

    //生徒データ
    function getSchno($model)
    {
        $query  = "SELECT T2.SCHREGNO,T2.ATTENDNO,T1.NAME_SHOW,T2.GRADE,T2.HR_CLASS,T3.HR_NAME ";
        $query .= "FROM SCHREG_BASE_MST T1,SCHREG_REGD_DAT T2,SCHREG_REGD_HDAT T3 ";
        $query .= "WHERE T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "      T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "      T2.GRADE || T2.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "      T2.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "      T2.YEAR = T3.YEAR AND T2.SEMESTER = T3.SEMESTER AND ";
        $query .= "      T2.GRADE = T3.GRADE AND T2.HR_CLASS = T3.HR_CLASS ";
        $query .= "ORDER BY T2.ATTENDNO ";

        return $query;
    }

    //名称マスタ
    function getNameMst($namecd1)
    {
        $query  = "SELECT NAMECD2 AS VALUE, NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= "FROM   V_NAME_MST ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' AND ";
        $query .= "       NAMECD1 = '".$namecd1."' ";
        $query .= "ORDER BY NAMECD2 ";

        return $query;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1 ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "    , SCHOOLCD ";
        }
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE   = '".$model->field["GRADE"]."' ";
            $query .= "                     ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE   = '".$model->field["GRADE"]."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";
    }
        return $query;
    }
}
?>
