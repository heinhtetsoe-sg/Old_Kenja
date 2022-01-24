<?php

require_once('for_php7.php');

class knjg070Query extends Query {
    //年度取得
    function getYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR || '年度' AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR <= '". CTRL_YEAR ."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        $query .= " FETCH FIRST 2 ROWS ONLY ";

        return $query;
    }

    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //対象者一覧(転入生)
    function getCategoryName($model) {
        $sdate = str_replace("/", "-", $model->field["SDATE"]);
        $edate = str_replace("/", "-", $model->field["EDATE"]);

        $query  = " WITH REGD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->field["YEAR"]}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO || ' ' || T1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " INNER JOIN ";
        $query .= "     REGD L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENT_DIV IN ('4', '5') AND ";
        $query .= "     T1.ENT_DATE BETWEEN '{$sdate}' AND ";
        $query .= "                         '{$edate}' ";
        $query .= " ORDER BY ";
        $query .= "     INT(VALUE) ";

        return $query;
    }
}
?>
