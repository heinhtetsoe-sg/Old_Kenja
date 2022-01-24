<?php

require_once('for_php7.php');

class knjl141mquery extends Query {
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
        $query  = " SELECT ";
        $query .= "     T1.SUC_COURSECD   ||  ";
        $query .= "     T1.SUC_MAJORCD    || ";
        $query .= "     ':'               || ";
        $query .= "     L3.COURSENAME     || ";
        $query .= "     L4.MAJORNAME      || ";
        $query .= "     ' '               || ";
        $query .= "     L2.EXAMCOURSE_NAME AS LABEL, ";
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
        $query .= "     ENTEXAM_COURSE_MST L2 ON  L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                           AND L2.TESTDIV = T1.TESTDIV ";
        $query .= "                           AND L2.COURSECD = T1.SUC_COURSECD ";
        $query .= "                           AND L2.MAJORCD = T1.SUC_MAJORCD ";
        $query .= "                           AND L2.EXAMCOURSECD = T1.SUC_COURSECODE ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST L3 ON L3.COURSECD = T1.SUC_COURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST L4 ON  L4.COURSECD = T1.SUC_COURSECD ";
        $query .= "                  AND L4.MAJORCD  = T1.SUC_MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= " AND T1.PROCEDUREDIV = '1' ";
        $query .= " AND T1.ENTDIV = '1' ";
        $query .= " AND L1.NAMESPARE1 IS NOT NULL ";
        $query .= " AND L2.EXAMCOURSE_NAME IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     T1.SUC_COURSECD, ";
        $query .= "     T1.SUC_MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     L2.EXAMCOURSE_NAME, ";
        $query .= "     L3.COURSENAME, ";
        $query .= "     L4.MAJORNAME ";
        $query .= " ORDER BY ";
        $query .= "     T1.SUC_COURSECD, ";
        $query .= "     T1.SUC_MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     L2.EXAMCOURSE_NAME ";

        return $query;
    }

    function selectCvsQuery($model, $kamokuSuu) {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.EXAMNO AS SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     SEX, ";
        $query .= "     T1.SUC_COURSECD AS COURSECD, ";
        $query .= "     T1.SUC_MAJORCD AS MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE AS COURSECODE, ";
        for ($i = 1; $i <= $kamokuSuu; $i++) { //科目の数だけループ
            $j = $i + 1;
            $query .= "     '' AS SCORE_L{$i}, ";
        }
        $query .= "     '' AS TOTAL2, ";
        for ($i = 1; $i <= $kamokuSuu; $i++) { //科目の数だけループ
            $j = $i + 1;
            $query .= "     M{$j}.SCORE AS SCORE_M{$i}, ";
        }
        $query .= "     M1.TOTAL4, ";
        $query .= "     'DUMMY' AS DUMMY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_RECEPT_DAT L1 ON  L1.ENTEXAMYEAR   = T1.ENTEXAMYEAR ";
        $query .= "                           AND L1.APPLICANTDIV  = T1.APPLICANTDIV ";
        $query .= "                           AND L1.TESTDIV       = T1.TESTDIV ";
        $query .= "                           AND L1.EXAM_TYPE     = '1' ";
        $query .= "                           AND L1.EXAMNO        = T1.EXAMNO ";
        for ($i = 1; $i <= $kamokuSuu; $i++) { //科目の数だけループ
            $j = $i + 1;
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_SCORE_DAT L{$j} ON  L{$j}.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
            $query .= "                             AND L{$j}.APPLICANTDIV   = T1.APPLICANTDIV ";
            $query .= "                             AND L{$j}.TESTDIV        = T1.TESTDIV ";
            $query .= "                             AND L{$j}.EXAM_TYPE      = L1.EXAM_TYPE ";
            $query .= "                             AND L{$j}.RECEPTNO       = L1.RECEPTNO ";
            $query .= "                             AND L{$j}.TESTSUBCLASSCD = '{$i}' ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_RECEPT_DAT M1 ON  M1.ENTEXAMYEAR   = T1.ENTEXAMYEAR ";
        $query .= "                           AND M1.APPLICANTDIV  = T1.APPLICANTDIV ";
        $query .= "                           AND M1.TESTDIV       = T1.TESTDIV ";
        $query .= "                           AND M1.EXAM_TYPE     = '1' ";
        $query .= "                           AND M1.EXAMNO        = T1.EXAMNO ";
        for ($i = 1; $i <= $kamokuSuu; $i++) { //科目の数だけループ
            $j = $i + 1;
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_SCORE_DAT M{$j} ON  M{$j}.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
            $query .= "                             AND M{$j}.APPLICANTDIV   = T1.APPLICANTDIV ";
            $query .= "                             AND M{$j}.TESTDIV        = T1.TESTDIV ";
            $query .= "                             AND M{$j}.EXAM_TYPE      = M1.EXAM_TYPE ";
            $query .= "                             AND M{$j}.RECEPTNO       = M1.RECEPTNO ";
            $query .= "                             AND M{$j}.TESTSUBCLASSCD = '{$i}' ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT N3 ON  N3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND N3.EXAMNO      = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST N4 ON  N4.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                 AND N4.NAMECD1 = 'L013' ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        $query .= "   AND T1.SUC_COURSECD = '{$model->suc_coursecd}' ";
        $query .= "   AND T1.SUC_MAJORCD  = '{$model->suc_majorcd}' ";
        $query .= "   AND N4.NAMESPARE1 IS NOT NULL ";

        if ($model->suc_coursecode != 'DUMMY') {
            $query .= "   AND T1.SUC_COURSECODE = '{$model->suc_coursecode}' ";
        }

        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }
}
?>
