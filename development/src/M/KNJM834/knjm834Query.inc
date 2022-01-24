<?php

require_once('for_php7.php');


class knjm834Query extends Query {

    //学期マスタ
    function getSemeMst()
    {
        $query .= "  SELECT DISTINCT";
        $query .= "      SEMESTERNAME LABEL, ";
        $query .= "      SEMESTER AS VALUE    ";
        $query .= "  FROM ";
        $query .= "      SEMESTER_MST ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '" .CTRL_YEAR ."'";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($year,$semester)
    {
        $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR='" .$year ."'";
        if ($semester === '9') {
            $query .= "AND SEMESTER= '".CTRL_SEMESTER."'";
        } else {
            $query .= "AND SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒取得
    function getSchno($model,$year,$semester)
    {
        $query  = "SELECT T1.SCHREGNO, ";
        $query .= "       T1.SCHREGNO || '　' || T2.NAME AS NAME ";
        $query .= "FROM   SCHREG_REGD_DAT T1, ";
        $query .= "       SCHREG_BASE_MST T2 ";
        $query .= "WHERE  T1.YEAR       = '".$year."' AND ";
        if ($semester === '9') {
            $query .= " T1.SEMESTER= '".CTRL_SEMESTER."' AND ";
        } else {
            $query .= " T1.SEMESTER='".$semester ."' AND ";
        }
        $query .= "       T1.GRADE||T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "       T2.SCHREGNO   = T1.SCHREGNO ";
        $query .= "ORDER BY SUBSTR(T1.SCHREGNO, 1, 4) DESC, SUBSTR(T1.SCHREGNO, 5) ";

        return $query;
    }

}
?>

