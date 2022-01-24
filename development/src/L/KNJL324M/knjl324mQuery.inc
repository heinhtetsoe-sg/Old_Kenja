<?php

require_once('for_php7.php');

class knjl324mQuery extends Query {
    //CSV�f���o���悤�̃N�G��
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.SUC_MAJORCD, ";
        $query .= "     L2.MAJORLNAME, ";
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
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //CSV�f���o���悤�̃N�G��(���i�Ґ��̃J�E���g)
    function selectMain_Cout_Query($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
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

        return $query;
    }
}
?>
