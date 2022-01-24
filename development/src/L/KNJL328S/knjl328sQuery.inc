<?php

require_once('for_php7.php');

class knjl328sQuery extends Query {
    //�ŏI�w�����擾
    function getSemesterLast() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER <> '9' ";

        $db = Query::dbCheckOut();
        $rtnVal = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnVal;
    }
    //�������x
    function getApplicantdiv($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     L1.NAMECD2 || ' ' || L1.NAME1 AS LABEL, ";
        $query .= "     L1.NAMECD2 AS VALUE, ";
        $query .= "     L1.NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "                 AND L1.NAMECD1 = 'L003' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->test_year}' ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMESPARE2, ";
        $query .= "     L1.NAMECD2 ";

        return $query;
    }
}
?>
