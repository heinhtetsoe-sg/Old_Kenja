<?php

require_once('for_php7.php');

// kanji=漢字
class knja142aQuery extends Query {
    
    //学期マスタ
    function getSemeMst($year,$semester) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$year."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";

        return $query;
    }

    //期限コンボ
    function getTerm() {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 =  'A049' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //経路コンボ
    function getRosen() {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENVIR.ROSEN_1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_ENVIR_DAT ENVIR ON ENVIR.SCHREGNO = REGD.SCHREGNO ";
        $query .= "         AND ENVIR.ROSEN_1 IS NOT NULL ";
        $query .= "         AND ENVIR.FLG_1 = '2' ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     ENVIR.ROSEN_1 ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model,$year,$semester)
    {
            if ($model->field["OUTPUT"] == '1') {
                $query  = "SELECT DISTINCT T1.GRADE || T1.HR_CLASS AS VALUE, T2.HR_NAME AS LABEL ";
                $query .= "  FROM CLASS_FORMATION_DAT T1 ";
                $query .= "  INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ";
                $query .= "      AND T2.SEMESTER = T1.SEMESTER ";
                $query .= "      AND T2.GRADE = T1.GRADE ";
                $query .= "      AND T2.HR_CLASS = T1.HR_CLASS ";
                $query .= "  INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
                if ($model->Properties["useSchool_KindField"] == "1") {
                    $query .= "     AND T3.SCHOOL_KIND IN ('H','J') ";
                }
                $query .= "  INNER JOIN NAME_MST T4 ON T4.NAMECD1 = 'A023' AND T4.NAME1 = T3.SCHOOL_KIND AND T4.NAME2 = T1.GRADE ";
            } else {
                $query  = "SELECT DISTINCT T1.GRADE || T1.HR_CLASS AS VALUE, T1.HR_NAME AS LABEL ";
                $query .= "  FROM SCHREG_REGD_HDAT T1 ";
                if ($model->Properties["useSchool_KindField"] == "1") {
                    $query .= "  INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE AND T2.SCHOOL_KIND IN ('H','J') ";
                }
                if ($model->field["DISP"] == 2) {
                    $query .= "       INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ";
                    $query .= "           AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ";
                    $query .= "       INNER JOIN SCHREG_ENVIR_DAT ENVIR ON ENVIR.SCHREGNO = T3.SCHREGNO ";
                    $query .= "           AND ENVIR.ROSEN_1 = '".$model->rosenMap[$model->field["ROSEN"]]."' ";
                }
            }
            $query .= " WHERE T1.YEAR = '" .$year ."' ";
            $query .= "   AND T1.SEMESTER = '".$semester ."' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "  AND (TR_CD1 = '" .STAFFCD ."' ";
                $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
                $query .= "    OR TR_CD3 = '" .STAFFCD ."' ";
                $query .= "    OR SUBTR_CD1 = '" .STAFFCD ."' ";
                $query .= "    OR SUBTR_CD2 = '" .STAFFCD ."' ";
                $query .= "    OR SUBTR_CD3 = '" .STAFFCD ."') ";
            }
            $query .= "ORDER BY 1 ";

        return $query;
    }

    //生徒取得
    function getSchno2($model,$year,$semester)
    {
        if ($model->field["OUTPUT"] == '1') {
            $query  = "SELECT T1.SCHREGNO,T1.ATTENDNO,T1.GRADE,T1.HR_CLASS,VALUE(T20.NAME, T2.NAME) AS NAME,T3.HR_NAME ";
            $query .= "FROM   CLASS_FORMATION_DAT T1 ";
            $query .= "       LEFT JOIN FRESHMAN_DAT T20 ON T20.ENTERYEAR = T1.YEAR AND T20.SCHREGNO = T1.SCHREGNO ";
            $query .= "       LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ";
            $query .= "       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR AND T3.SEMESTER=T1.SEMESTER AND ";
            $query .= "                                        T3.GRADE=T1.GRADE AND T3.HR_CLASS=T1.HR_CLASS ";
        } else {
            $query  = "SELECT T1.SCHREGNO,T1.ATTENDNO,T1.GRADE,T1.HR_CLASS,T2.NAME,T3.HR_NAME ";
            $query .= "FROM   SCHREG_REGD_DAT T1 ";
            $query .= "       LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ";
            $query .= "       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T1.YEAR AND T3.SEMESTER=T1.SEMESTER AND ";
            $query .= "                                        T3.GRADE=T1.GRADE AND T3.HR_CLASS=T1.HR_CLASS ";
            $query .= "       INNER JOIN SCHREG_ENVIR_DAT ENVIR ON ENVIR.SCHREGNO = T1.SCHREGNO ";
            $query .= "           AND ENVIR.ROSEN_1 = '".$model->rosenMap[$model->field["ROSEN"]]."' ";
        }
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        $query .= "       T1.SEMESTER   = '".$semester."' AND ";
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY T1.ATTENDNO ";

        return $query;
    }

}
?>
