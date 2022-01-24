<?php

require_once('for_php7.php');

class knjl316rQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $namecd1, $flg)
    {
        $query  = " SELECT DISTINCT ";
        if ($flg) {
            $query .= "     NAME1 AS VALUE ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE,";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' ";
        $query .= " AND NAMECD1 = '{$namecd1}' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMECD2 = '{$model->fixApplicantDiv}' ";
        }
        if ($flg) {
            if ($namecd1 == "L004") {
                $query .= " AND NAMECD2 = '{$model->field["TESTDIV"]}' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //課程学科
    function getCourseMajorcd($model, $flg)
    {
        $query  = " SELECT DISTINCT ";
        if ($flg) {
            $query .= "     L1.COURSENAME || L1.MAJORNAME AS VALUE ";
        } else {
            $query .= "     T1.COURSECD || T1.MAJORCD || ':' || L1.COURSENAME || L1.MAJORNAME AS LABEL, ";
            $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L1 ON T1.ENTEXAMYEAR = L1.YEAR ";
        $query .= "                                    AND T1.COURSECD = L1.COURSECD ";
        $query .= "                                    AND T1.MAJORCD  = L1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($flg) {
            $query .= " AND L1.COURSECD || L1.MAJORCD = '{$model->field["COURSE_MAJORCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //課程学科
    function getCsvQuery($model)
    {
        if ($model->field["COURSE_MAJORCD"] !== '9999') {
            $sub_coursecd = substr($model->field["COURSE_MAJORCD"], 0, 1);
            $sub_majorcd  = substr($model->field["COURSE_MAJORCD"], 1, 3);
        }
    
        $query  = " WITH BEFORE_OAGE_SEQ_DATA AS ( ";
        $query .= " SELECT ";
        $query .= "     L1.EXAMNO, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANT_BEFORE_DAT T1 ";
        $query .= "     LEFT JOIN (SELECT T1.EXAMNO, T1.REMARK1, T1.REMARK2 ";
        $query .= "                FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT T1 ";
        $query .= "                INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                                       AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                                       AND T2.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "                                                       AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "                WHERE T1.ENTEXAMYEAR = '{$model->ObjYear}'AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' AND T1.SEQ = '002' ";
        $query .= "                                                ) L1 ON L1.REMARK1 = T1.BEFORE_PAGE ";
        $query .= "                                                    AND L1.REMARK2 = T1.BEFORE_SEQ ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($model->field["COURSE_MAJORCD"] !== '9999') {
            $query .= " AND T1.BEFORE_COURSECD = '{$sub_coursecd}' ";
            $query .= " AND T1.BEFORE_MAJORCD = '{$sub_majorcd}' ";
        }
        $query .= " AND T1.BEFORE_COURSECD IS NOT NULL ";
        $query .= " AND T1.BEFORE_MAJORCD IS NOT NULL ";
        $query .= " AND T1.BEFORE_EXAMCOURSECD IS NOT NULL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     L1.NAISIN2, ";
        $query .= "     COUNT(*) AS CNT, ";
        $query .= "     SUM(L2.TOTAL4) AS TOTAL4_SUM ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1  ";
        $query .= "     INNER JOIN BEFORE_OAGE_SEQ_DATA L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                       AND L1.EXAMNO = T1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DAT L2 ON L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND L2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                    AND L2.TESTDIV = T1.TESTDIV ";
        $query .= "                                    AND L2.EXAM_TYPE = '1' ";
        $query .= "                                    AND L2.RECEPTNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND L2.TOTAL4 IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     L1.NAISIN2 ";
        $query .= " ORDER BY  ";
        $query .= "     L1.NAISIN2 DESC ";
        
        return $query;
    }
}
?>
