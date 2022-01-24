<?php

require_once('for_php7.php');

class knjl324tQuery extends Query {
    function getSchoolName($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOLNAME1 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->test_year}' ";

        return $query;
    }

    //CSV吐き出しようのクエリ
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.SUC_MAJORCD, ";
        $query .= "     L2.MAJORLNAME, ";
        $query .= "     L2.MAJORSNAME, ";
        $query .= "     L2.MAJORLNAME || ' ' || L2.MAJORSNAME AS MAJOR_L_S_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                 AND L1.NAMECD1 = 'L013' ";
        $query .= " LEFT JOIN ";
        $query .= "     V_ENTEXAM_MAJOR_MST L2 ON L2.MAJORCD = T1.SUC_MAJORCD ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR = '{$model->test_year}' ";
        $query .= "     AND L1.NAMESPARE1 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SUC_MAJORCD, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
?>
