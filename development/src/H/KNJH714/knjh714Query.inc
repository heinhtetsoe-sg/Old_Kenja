<?php

require_once('for_php7.php');

class knjh714Query extends Query
{

    //テスト名称取得
    public function getTestName()
    {
        $query  = "";
        $query  = "   SELECT TESTID AS VALUE ";
        $query .= "        , TESTNAME AS LABEL ";
        $query .= "     FROM ACADEMICTEST_MST AM ";
        $query .= "    WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY TESTDIV ";
        $query .= "        , TESTID ";

        return $query;
    }

    //時限取得
    public function getPeriod($model)
    {
        $query  = "";
        $query  = "    SELECT ASD.PERIODID AS VALUE";
        $query .= "         , ABBV1 AS LABEL ";
        $query .= "      FROM ACADEMICTEST_STF_DAT ASD";
        $query .= " LEFT JOIN NAME_MST NM";
        $query .= "        ON NM.NAMECD1 = 'H321'";
        $query .= "       AND NM.NAMECD2 = ASD.PERIODID";
        $query .= "     WHERE ASD.TESTID = '".$model->field["TESTNAME"]."' ";
        $query .= "  GROUP BY ASD.PERIODID";
        $query .= "         , ABBV1";
        $query .= "  ORDER BY VALUE";

        return $query;
    }

    //施設一覧取得
    public function getSisetuList($model)
    {
        $query  = "    SELECT ASTD.FACCD VALUE ";
        $query .= "         , FM.FACILITYNAME AS LABEL ";
        $query .= "      FROM ACADEMICTEST_STF_DAT ASTD ";
        $query .= " LEFT JOIN FACILITY_MST FM ";
        $query .= "        ON FM.FACCD = ASTD.FACCD ";
        $query .= "     WHERE ASTD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "       AND ASTD.TESTID   = '".$model->field["TESTNAME"]."' ";
        $query .= "       AND ASTD.PERIODID = '".$model->field["PERIODID"]."' ";
        $query .= "  ORDER BY VALUE ";

        return $query;
    }
}
