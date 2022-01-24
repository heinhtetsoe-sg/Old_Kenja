<?php

require_once('for_php7.php');


class knja143hQuery extends Query {

    //学期マスタ
    function getSemeMst($year,$semester)
    {
        $query  = "SELECT * FROM SEMESTER_MST WHERE YEAR='".$year."' AND SEMESTER='".$semester."' ";

        return $query;
    }

    //履修履歴
    function getRirekiCode($year)
    {
        $query  = " SELECT ";
        $query .= "     RIREKI_CODE AS VALUE, ";
        $query .= "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
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
        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model, $year,$semester)
    {
        $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
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
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //生徒取得
    function getSchno($model,$year,$semester)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "       T1.GRADE || T1.HR_CLASS || T1.SCHREGNO AS VALUE, ";
        $query .= "       L1.HR_NAME || '　' || T1.ATTENDNO || '番' || '　' ||  T2.NAME AS NAME, ";
        $query .= "       T1.GRADE || T1.HR_CLASS || T1.ATTENDNO AS ORDER ";
        $query .= "FROM   SCHREG_REGD_DAT T1 ";
        $query .= "       LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "            AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "            AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS ";
        $query .= "       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO   = T1.SCHREGNO ";
        if ($model->field["RISHUU"] == '1') {
            $query .= "       INNER JOIN SUBCLASS_STD_SELECT_RIREKI_DAT T3 ON "; 
            $query .= "       T1.YEAR   = T3.YEAR ";
            $query .= "       AND T1.SEMESTER   = T3.SEMESTER ";
            $query .= "       AND T1.SCHREGNO   = T3.SCHREGNO ";
            $query .= "       AND T3.RIREKI_CODE   = '".$model->field["RIREKI_CODE"]."' ";
        }
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        $query .= "       T1.SEMESTER   = '".$semester."' AND ";
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY ORDER ";

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

    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";
        
        return $query;
    }
}
?>

