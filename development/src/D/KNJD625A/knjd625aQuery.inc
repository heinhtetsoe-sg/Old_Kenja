<?php

require_once('for_php7.php');

class knjd625aQuery extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model)
    {
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    CAST(SMALLINT(GRADE) AS CHAR(1)) || '学年' AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$sem ."' ";
        $query .= "ORDER BY ";
        $query .= "    GRADE";

        return $query;
    }

    //テスト種別取得
    function getTestcd($model)
    {
        $query  = "SELECT ";
        $query .= "    TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "    TESTITEMNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"] ."' ";
        $query .= "    AND TESTKINDCD <> '99' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //科目一覧取得
    function getSubclass($model)
    {
        $nameCd1 = ($model->field["GRADE"] < "04") ? "('D003', 'D013')" : "('D002', 'D012')";
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.NAME1 AS VALUE, ";
        $query .= "     T1.NAME1 || '：' || VALUE(L1.SUBCLASSNAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON T1.NAME1 = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.NAMECD1 IN ".$nameCd1 ." ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAME1 ";

        return $query;
    }
}
?>
