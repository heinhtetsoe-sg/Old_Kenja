<?php

require_once('for_php7.php');


class knjd295Query extends Query {

    //学年取得
    function getGrade()
    {
        $query  = " SELECT ";
        $query .= "     '新入生' AS LABEL, ";
        $query .= "     '0-' || GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND SCHOOL_KIND = 'H' ";
        $query .= "     AND GRADE IN (SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = 'H') ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '新' || RTRIM(CAST(INT(GRADE_CD) + 1 AS CHAR(2))) || '年生' AS LABEL, ";
        $query .= "     '1-' || GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    function getHrClass($model)
    {
        $dtypearry = explode("-", $model->field["DTGRADE"]);

        if ($dtypearry[0] == "0") {
            // 新入生
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
            $query .= "     T2.HR_NAME AS LABEL";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T1 ";
            $query .= "     INNER JOIN V_NAME_MST A023 ";
            $query .= "         ON A023.YEAR = T1.ENTERYEAR ";
            $query .= "         AND A023.NAMECD1 = 'A023' ";
            $query .= "         AND A023.NAME1 = 'H' ";
            $query .= "         AND A023.NAME2 = T1.GRADE ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT T2 ";
            $query .= "         ON T2.YEAR = T1.ENTERYEAR ";
            $query .= "         AND T2.SEMESTER = '1' ";
            $query .= "         AND T2.GRADE = T1.GRADE ";
            $query .= "         AND T2.HR_CLASS = T1.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T1.ENTERYEAR = '".(CTRL_YEAR + 1)."' ";
            $query .= "     AND T1.GRADE = '".$dtypearry[1]."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            //在校生
            $query  = " SELECT DISTINCT ";
            $query .= "     T3.GRADE || T3.HR_CLASS AS VALUE, ";
            $query .= "     T4.HR_NAME AS LABEL";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T3 ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT T4 ";
            $query .= "         ON T4.YEAR = T3.YEAR ";
            $query .= "         AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "         AND T4.GRADE = T3.GRADE ";
            $query .= "         AND T4.HR_CLASS = T3.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T3.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T3.GRADE = '".$dtypearry[1]."' ";
            $query .= "     AND '1' = '".$dtypearry[0]."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }
}
?>
