<?php

require_once('for_php7.php');


class knja143wQuery extends Query {

    //学期マスタ
    function getSemeMst($year,$semester)
    {
        $query  = "SELECT * FROM SEMESTER_MST WHERE YEAR='".$year."' AND SEMESTER='".$semester."' ";

        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得(最大GRADE取得用)
    function getMaxGrade($model, $year, $grade)
    {
        $query  = "SELECT T1.GRADE, T1.GRADE_CD, T3.GRADE AS MAX_GRADE, T3.GRADE_CD AS MAX_GRADE_CD ";
        $query .= "FROM SCHREG_REGD_GDAT T1 ";
        $query .= "INNER JOIN (SELECT YEAR, SCHOOL_KIND, MAX(GRADE) AS MAX_GRADE ";
        $query .= "            FROM SCHREG_REGD_GDAT ";
        $query .= "            GROUP BY YEAR, SCHOOL_KIND) T2 ON T2.YEAR = T1.YEAR  ";
        $query .= "    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T2.YEAR AND T3.GRADE = T2.MAX_GRADE ";
        $query .= "WHERE T1.YEAR='" .$year ."'";
        $query .= "  AND T1.GRADE ='" .$grade."'";
        $query .= "  AND T1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model, $year,$semester, $grade, $labelflg)
    {
        if ($labelflg == 0) {
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        } else {
            $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS NAME ";
        }
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR='" .$year ."'";
        $query .= "AND SEMESTER='".$semester ."'";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " AND GRADE = '".$model->field["GRADE"]. "' ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //生徒取得
    function getSchno($model,$year,$semester)
    {
        $query  = "SELECT DISTINCT T1.SCHREGNO AS VALUE, ";
        $query .= "       L1.HR_NAME || '　' || T1.ATTENDNO || '番' || '　' ||  T2.NAME AS NAME ";
        $query .= "FROM   SCHREG_REGD_DAT T1 ";
        $query .= "       LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "            AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "            AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS ";
        $query .= "       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        $query .= "       T1.SEMESTER   = '".$semester."' AND ";
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY T1.SCHREGNO ";

        return $query;
    }
    
    //職員取得
    function getStaff($year)
    {
        $query  = " SELECT ";
        $query .= "     STAFFCD AS VALUE, ";
        $query .= "     STAFFCD || '　' || STAFFNAME AS NAME ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " ORDER BY ";
        $query .= "     STAFFCD ";

        return $query;
    }
}
?>

