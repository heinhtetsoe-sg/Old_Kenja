<?php

require_once('for_php7.php');

class knjd624cQuery extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //学年コンボ
    function getGrade() {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ種別コンボ
    function getDatadiv() {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        $query .= "     AND NAMECD1     = 'H308' ";
        $query .= "     AND NAMESPARE1  = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト名称コンボ
    function getMockcd($datadiv) {
        $query  = " SELECT ";
        $query .= "     MOCKNAME1 AS LABEL, ";
        $query .= "     MOCKCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     SUBSTR(MOCKCD, 1, 1) = '{$datadiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目一覧取得
    function getSubclass($model)
    {
        $query  = "WITH SCH_T AS ( ";
        $query .= "SELECT ";
        $query .= "    SCHREGNO ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "    GRADE = '".$model->field["GRADE"] ."' ";
        $query .= ") ";

        $query .= "SELECT DISTINCT ";
        $query .= "    T1.MOCK_SUBCLASS_CD AS VALUE, ";
        $query .= "    T1.MOCK_SUBCLASS_CD || ':' || VALUE(L1.SUBCLASS_NAME, '') AS LABEL ";
        $query .= "FROM ";
        $query .= "    MOCK_DAT T1 ";
        $query .= "    LEFT JOIN MOCK_SUBCLASS_MST L1 ON L1.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.MOCKCD = '".$model->field["MOCKCD"]."' AND ";
        $query .= "    T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";

        return $query;
    }
}
?>
