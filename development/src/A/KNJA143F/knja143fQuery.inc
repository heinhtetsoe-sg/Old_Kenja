<?php

require_once('for_php7.php');


class knja143fQuery extends Query {

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

    //年組取得（権限チェック）
    function getAuth($model, $year,$semester)
    {
        $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
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
        $query .= "AND T1.SEMESTER='".$semester ."'";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (T1.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR T1.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR T1.TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒取得
    function getSchno($model,$year,$semester)
    {
        $query  = "SELECT DISTINCT T1.SCHREGNO, ";
        if ($model->Properties["PRINT_ORDER_TYPE"] === "1") {
            $query .= "       L1.HR_NAME || '　' || T1.ATTENDNO || '番' || '　' ||  T2.NAME AS NAME ";
            $query .= "FROM   SCHREG_REGD_DAT T1 ";
            $query .= "       LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ";
            $query .= "            AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "            AND L1.GRADE || L1.HR_CLASS = T1.GRADE || T1.HR_CLASS, ";
        } else {
            $query .= "       T1.SCHREGNO || '　' || T2.NAME AS NAME ";
            $query .= "FROM   SCHREG_REGD_DAT T1, ";
        }
        $query .= "       SCHREG_BASE_MST T2 ";
        if ($model->field["RISHUU"] == '1') {
            $query .= "       ,SUBCLASS_STD_SELECT_RIREKI_DAT T3 "; 
        }
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        $query .= "       T1.SEMESTER   = '".$semester."' AND ";
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "       T2.SCHREGNO   = T1.SCHREGNO ";
        if ($model->field["RISHUU"] == '1') {
            $query .= "       AND T1.YEAR   = T3.YEAR ";
            $query .= "       AND T1.SEMESTER   = T3.SEMESTER ";
            $query .= "       AND T1.SCHREGNO   = T3.SCHREGNO ";
            $query .= "       AND T3.RIREKI_CODE   = '".$model->field["RIREKI_CODE"]."' ";
        }
        $query .= "ORDER BY T1.SCHREGNO ";

        return $query;
    }

}
?>

