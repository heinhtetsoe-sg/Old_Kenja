<?php

require_once('for_php7.php');


class knja142bQuery extends Query
{
    //年組取得（権限チェック）
    public function getAuth($model, $year, $semester)
    {
        $query  = "SELECT T1.GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR='" .$year ."'";
        $query .= "AND SEMESTER='".$semester ."'";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND (   TR_CD1 = '" .STAFFCD ."' ";
            $query .= "      OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "      OR TR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //生徒取得
    public function getSchno($model, $year, $semester)
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

    //学校名取得
    public function getSchoolName()
    {
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
