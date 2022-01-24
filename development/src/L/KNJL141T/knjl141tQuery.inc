<?php

require_once('for_php7.php');

class knjl141tquery extends Query {
    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //科目数取得
    function getKamokuSuu() {
        $query = "SELECT COUNT(*) FROM NAME_MST WHERE NAMECD1 = 'L009'";
        return $query;
    }

    //科目名取得
    function getKmaokuMei() {
        $query = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L009' ORDER BY NAMECD2";
        return $query;
    }


    //合格コースコンボ
    function getGoukaku($model) {
        $query  = " SELECT distinct ";
        $query .= "     T1.SUC_COURSECD   ||  ";
        $query .= "     T1.SUC_MAJORCD    || ";
        $query .= "     ':'               || ";
        $query .= "     L3.COURSENAME     || ";
        $query .= "     case when L5.MAJORSCD = '0' then L4.MAJORLNAME else L5.MAJORSNAME end AS LABEL, ";
        $query .= "     T1.SUC_COURSECD   || ':' || ";
        $query .= "     T1.SUC_MAJORCD    || ':' || ";
        $query .= "     T1.SUC_COURSECODE || ':' || ";
        $query .= "     T1.TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                 AND L1.NAMECD1 = 'L013' ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST L3 ON L3.COURSECD = T1.SUC_COURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_MAJORL_MST L4 ON  L4.MAJORLCD = substr(T1.SUC_MAJORCD,1,2) ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_MAJORS_MST L5 ON  L5.MAJORLCD = substr(T1.SUC_MAJORCD,1,2) ";
        $query .= "                           AND L5.MAJORSCD = substr(T1.SUC_MAJORCD,3,1) ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= " AND T1.PROCEDUREDIV = '1' ";
        $query .= " AND T1.ENTDIV = '1' ";
        $query .= " AND L1.NAMESPARE1 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function selectCvsQuery($model, $kamokuSuu) {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.EXAMNO AS SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     SEX, ";
        $query .= "     N5.MAIN_COURSECD AS COURSECD, ";
        $query .= "     N5.MAIN_MAJORCD AS MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE AS COURSECODE, ";
        for ($i = 1; $i <= $kamokuSuu; $i++) { //科目の数だけループ
            $j = $i + 1;
            $query .= "     '' AS SCORE_L{$i}, ";
        }
        $query .= "     '' AS TOTAL2, ";
        for ($i = 1; $i <= $kamokuSuu; $i++) { //科目の数だけループ
            $j = $i + 1;
            $query .= "     '' AS SCORE_M{$i}, ";
        }
        $query .= "     '' AS TOTAL4, ";
        $query .= "     'DUMMY' AS DUMMY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST N4 ON  N4.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                 AND N4.NAMECD1 = 'L013' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_MAJORS_MST N5 ON  N5.MAJORLCD = substr(T1.SUC_MAJORCD,1,2) ";
        $query .= "                           AND N5.MAJORSCD = substr(T1.SUC_MAJORCD,3,1) ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        $query .= "   AND N4.NAMESPARE1 IS NOT NULL ";
        $query .= "   AND T1.SUC_COURSECD = '{$model->suc_coursecd}' ";
        $query .= "   AND T1.SUC_MAJORCD  = '{$model->suc_majorcd}' ";

        if ($model->suc_coursecode != 'DUMMY') {
            $query .= "   AND T1.SUC_COURSECODE = '{$model->suc_coursecode}' ";
        }

        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }
}
?>
