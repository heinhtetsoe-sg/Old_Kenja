<?php

require_once('for_php7.php');

// kanji=漢字
class knja143iQuery extends Query {
    //年度コンボ
    function getYear($year) {
        $nextYear = (int)$year + 1;

        $query  = " WITH T_YEAR (VALUE) AS ( ";
        $query .= "     VALUES('{$year}') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('{$nextYear}') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     VALUE, ";
        $query .= "     VALUE || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     T_YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期コンボ
    function getSemester($year) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //月末日取得
    function getLastDay($sdate)
    {
        $query  = "VALUES LAST_DAY(DATE('".str_replace("/","-",$sdate)."'))";

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
        }
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        $query .= "       T1.SEMESTER   = '".$semester."' AND ";
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY T1.ATTENDNO ";

        return $query;
    }

}
?>
