<?php

require_once('for_php7.php');

class knjd621cQuery extends Query {

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

    //学年コンボ
    function getGrade($grade = "") {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($grade) {
            $query .= "     AND GRADE = '{$grade}' ";
        }
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
    function getMockcd($datadiv, $mockcd = "") {
        $query  = " SELECT ";
        $query .= "     MOCKNAME1 AS LABEL, ";
        $query .= "     MOCKCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     SUBSTR(MOCKCD, 1, 1) = '{$datadiv}' ";
        if ($mockcd) {
            $query .= "     AND MOCKCD = '{$mockcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト科目一覧
    function getSubclass($model) {
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
        $query .= "    T1.MOCK_SUBCLASS_CD || ':' || L1.SUBCLASS_NAME AS LABEL, ";
        $query .= "    T1.MOCK_SUBCLASS_CD AS VALUE ";
        $query .= "FROM ";
        $query .= "    MOCK_DAT T1 ";
        $query .= "    LEFT JOIN MOCK_SUBCLASS_MST L1 ON L1.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.MOCKCD = '".$model->field["MOCKCD"]."' AND ";
        $query .= "    T1.SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目名称取得
    function getSubclassMst($subclasscd)
    {
        $query  = "SELECT ";
        $query .= "    SUBCLASS_NAME ";
        $query .= "FROM ";
        $query .= "    MOCK_SUBCLASS_MST ";
        $query .= "WHERE ";
        $query .= "    MOCK_SUBCLASS_CD = '".$subclasscd."' ";

        return $query;
    }

    //CSVデータ取得
    function selectCsvQuery($model, $subclasscd) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE_RANK, ";
        $query .= "     L1.NAME_SHOW AS NAME, ";
        $query .= "     L3.HR_NAME, ";
        $query .= "     T1.SCORE ";
        if ($model->field["ASSESS_LEVEL"] == "1") {
            $query .= "     ,L4.ASSESSLEVEL ";
        }
        $query .= " FROM ";
        $query .= "     MOCK_RANK_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                        AND L2.YEAR = T1.YEAR ";
        $query .= "                        AND L2.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT L3 ON  L3.YEAR = T1.YEAR ";
        $query .= "                         AND L3.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "                         AND L3.GRADE = L2.GRADE ";
        $query .= "                         AND L3.HR_CLASS = L2.HR_CLASS ";
        if ($model->field["ASSESS_LEVEL"] == "1") {
            $query .= " LEFT JOIN ";
            $query .= "     MOCK_ASSESS_LEVEL_MST L4 ON  L4.YEAR = T1.YEAR ";
            $query .= "                         AND L4.MOCKCD = T1.MOCKCD ";
            $query .= "                         AND L4.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
            $query .= "                         AND L4.DIV = '1' "; //学年
            $query .= "                         AND L4.GRADE = L2.GRADE ";
            $query .= "                         AND L4.HR_CLASS = '000' ";
            $query .= "                         AND L4.COURSECD = '0' ";
            $query .= "                         AND L4.MAJORCD = '000' ";
            $query .= "                         AND L4.COURSECODE = '0000' ";
            $query .= "                         AND T1.SCORE BETWEEN L4.ASSESSLOW AND L4.ASSESSHIGH ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.MOCKCD = '{$model->field["MOCKCD"]}' AND ";
        $query .= "     T1.MOCK_SUBCLASS_CD = '{$subclasscd}' AND ";
        $query .= "     T1.MOCKDIV = '1' AND ";
        $query .= "     T1.GRADE_RANK <= {$model->field["RANK_RANGE"]} AND ";
        $query .= "     L2.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE_RANK, ";
        $query .= "     L2.HR_CLASS, ";
        $query .= "     L2.ATTENDNO ";

        return $query;
    }
}
?>
