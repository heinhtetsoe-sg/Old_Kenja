<?php

require_once('for_php7.php');

class knjh718Query extends Query
{
    //入試区分取得
    public function getTestId()
    {
        $query  = " SELECT ";
        $query .= "   T1.TESTDIV || '-' || T1.TESTID AS VALUE, ";
        $query .= "   T1.TESTNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   ACADEMICTEST_MST T1 ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "   VALUE ";

        return $query;
    }
}
