<?php

require_once('for_php7.php');


class knjm503Query extends Query {

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
        $query .= "  AND SEMESTER <> '9' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($year, $semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."' ";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //生徒取得
    function getSchreg($model)
    {
        $query  = " SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, ";
        $query .= "        SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     (((SCHREG_REGD_DAT.YEAR) = '".CTRL_YEAR."') ";
        $query .= "       AND ((SCHREG_REGD_DAT.SEMESTER) = '".CTRL_SEMESTER. "') ";
        $query .= "       AND ((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."')) ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    //日付取得
    function getSemeSdate($model)
    {
        $query  = " SELECT ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";

        return $query;
    }

}
?>
