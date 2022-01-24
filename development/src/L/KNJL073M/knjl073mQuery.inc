<?php

require_once('for_php7.php');

class knjl073mQuery extends Query {
    function selectQuery($model) {
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SUB_ORDER ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_RECEPT_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                           AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                           AND L1.TESTDIV = T1.TESTDIV ";
        $query .= "                           AND L1.EXAMNO  = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '{$model->test_year}' AND ";
        $query .= "     T1.APPLICANTDIV = '1' AND ";
        $query .= "     T1.JUDGEMENT    IN ('2', '3') AND ";
        $query .= "     L1.JUDGEDIV     = '5' ";
        $query .= " ORDER BY ";
        $query .= "     L1.TOTAL_RANK4, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    function updateEntexamApplicantbaseDat($model) {
        $db = Query::dbCheckOut();
        foreach ($model->field as $key => $val) {
            if (!preg_match('/^SUB_ORDER_/', $key)) continue;
            list($dummy, $dummy, $examno) = preg_split("/_/", $key);
            if (strlen($val)) {
                $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                $query .= "   SET JUDGEMENT      = '2' ";
                $query .= "      ,SUB_ORDER      = '{$val}' ";
                $query .= "      ,REGISTERCD     = '".STAFFCD."' ";
                $query .= "      ,UPDATED        = SYSDATE() ";
                $query .= " WHERE ENTEXAMYEAR  = '{$model->test_year}' ";
                $query .= "   AND APPLICANTDIV = '1' ";
                $query .= "   AND EXAMNO       = '{$examno}' ";
            } else {
                $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                $query .= "   SET JUDGEMENT      = '3' ";
                $query .= "      ,SUB_ORDER      = NULL ";
                $query .= "      ,REGISTERCD     = '".STAFFCD."'";
                $query .= "      ,UPDATED        = SYSDATE()";
                $query .= " WHERE ENTEXAMYEAR  = '{$model->test_year}' ";
                $query .= "   AND APPLICANTDIV = '1' ";
                $query .= "   AND EXAMNO       = '{$examno}' ";
            }

            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
