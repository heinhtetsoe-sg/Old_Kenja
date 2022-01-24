<?php

require_once('for_php7.php');

class knjl015mQuery extends Query {
    function getJudgementName($namecd2, $db) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L013' AND ";
        $query .= "     NAMECD2 = '{$namecd2}' ";

        return $db->getOne($query);
    }

    function getStudentSql($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     L1.GNAME, ";
        $query .= "     L1.GKANA, ";
        $query .= "     L2.NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND L1.EXAMNO      = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD1 = 'L013' ";
        $query .= "                 AND L2.NAMECD2 = T1.JUDGEMENT ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR = '{$model->test_year}' ";

        if ($model->field["JUDGEMENT1"] && !$model->field["JUDGEMENT2"]) {
            $query .= "     AND T1.JUDGEMENT = '1' ";
        }

        if (!$model->field["JUDGEMENT1"] && $model->field["JUDGEMENT2"]) {
            $query .= "     AND T1.JUDGEMENT = '4' ";
        }

        if ($model->field["JUDGEMENT1"] && $model->field["JUDGEMENT2"]) {
            $query .= "     AND (T1.JUDGEMENT= '1' OR T1.JUDGEMENT= '4') ";
        }

        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
?>
