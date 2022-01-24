<?php

require_once('for_php7.php');

class knjl322nQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学納金CSVヘッダー
    function getHeaderQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_PAYMENT_MONEY_YMST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND JUDGE_KIND = '0' ";

        return $query;
    }
        
    //CSVデータ
    function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T1.TESTDIV, T1.EXAMNO) AS ROWNO,  ";
        $query .= "     L004.NAME1 AS TESTDIV_NAME, ";
        $query .= "     L1_001.EXAMCOURSE_NAME,  ";
        $query .= "     CASE WHEN T1.ENTDIV = '1' THEN M1.MAJORNAME || '　' || L1_007.EXAMCOURSE_NAME ELSE NULL END AS ENTER_COURSEMAJORCODE_NAME, ";
        $query .= "     CASE WHEN T1.JUDGEMENT IN ('1', '3') AND T1.ENTDIV = '1' THEN L1.EXAMCOURSE_NAME ELSE NULL END AS SUC_EXAMCOURSE_NAME, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     Z002.NAME2 AS SEX_NAME, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     SCHOOL.FINSCHOOL_NAME_ABBV,  ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     CASE WHEN BD006.REMARK6 = '1' THEN '○' ELSE NULL END AS ONGAKU1_KIBOU, ";
        $query .= "     CASE WHEN BD006.REMARK7 = '1' THEN '○' ELSE NULL END AS BIJYUTU1_KIBOU, ";
        $query .= "     CASE WHEN BD006.REMARK8 = '1' THEN '○' ELSE NULL END AS SHODOU1_KIBOU, ";
        $query .= "     L012.NAME1 AS ENTDIV_NAME, ";
        $query .= "     T1.PROCEDUREDATE, ";
        $query .= "     L6.ENT_MONEY, ";
        $query .= "     L6.FAC_MONEY, ";
        $query .= "     L6.REMARK1_MONEY, ";
        $query .= "     L6.REMARK2_MONEY, ";
        $query .= "     L6.REMARK3_MONEY, ";
        $query .= "     VALUE(L6.ENT_MONEY , 0) + VALUE(L6.FAC_MONEY , 0) + VALUE(L6.REMARK1_MONEY , 0) + VALUE(L6.REMARK2_MONEY , 0) + VALUE(L6.REMARK3_MONEY , 0) AS TOTAL_MONEY ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L004 ON L004.NAMECD1 = 'L004' ";
        $query .= "                            AND L004.NAMECD2 = T1.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD001 ON BD001.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                                     AND BD001.EXAMNO = T1.EXAMNO  ";
        $query .= "                                                     AND BD001.SEQ = '001'  ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1_001 ON L1_001.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                        AND L1_001.APPLICANTDIV = T1.APPLICANTDIV  ";
        $query .= "                                        AND L1_001.TESTDIV = T1.TESTDIV  ";
        $query .= "                                        AND L1_001.COURSECD = BD001.REMARK8 ";
        $query .= "                                        AND L1_001.MAJORCD = BD001.REMARK9 ";
        $query .= "                                        AND L1_001.EXAMCOURSECD = BD001.REMARK10 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD007 ON BD007.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                                     AND BD007.EXAMNO = T1.EXAMNO  ";
        $query .= "                                                     AND BD007.SEQ = '007'  ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1_007 ON L1_007.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                        AND L1_007.APPLICANTDIV = T1.APPLICANTDIV  ";
        $query .= "                                        AND L1_007.TESTDIV = T1.TESTDIV  ";
        $query .= "                                        AND L1_007.COURSECD = BD007.REMARK1 ";
        $query .= "                                        AND L1_007.MAJORCD = BD007.REMARK2 ";
        $query .= "                                        AND L1_007.EXAMCOURSECD = BD007.REMARK3 ";
        $query .= "     LEFT JOIN COURSE_MST C1 ON L1_007.COURSECD = C1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST M1 ON L1_007.COURSECD = M1.COURSECD ";
        $query .= "                           AND L1_007.MAJORCD = M1.MAJORCD ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                    AND L1.APPLICANTDIV = T1.APPLICANTDIV  ";
        $query .= "                                    AND L1.TESTDIV = T1.TESTDIV  ";
        $query .= "                                    AND L1.COURSECD = T1.SUC_COURSECD ";
        $query .= "                                    AND L1.MAJORCD = T1.SUC_MAJORCD ";
        $query .= "                                    AND L1.EXAMCOURSECD = T1.SUC_COURSECODE ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND T1.SEX = Z002.NAMECD2  ";
        $query .= "     LEFT JOIN FINSCHOOL_MST SCHOOL ON T1.FS_CD = SCHOOL.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD006 ON BD006.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                                     AND BD006.EXAMNO = T1.EXAMNO  ";
        $query .= "                                                     AND BD006.SEQ = '006'  ";
        $query .= "     LEFT JOIN NAME_MST L012 ON L012.NAMECD1 = 'L012' ";
        $query .= "                            AND L012.NAMECD2 = T1.ENTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD009 ON BD009.ENTEXAMYEAR = T1.ENTEXAMYEAR  ";
        $query .= "                                                     AND BD009.EXAMNO = T1.EXAMNO  ";
        $query .= "                                                     AND BD009.SEQ = '009'  ";
        $query .= "     LEFT JOIN ENTEXAM_PAYMENT_MONEY_YMST L6 ON L6.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                            AND L6.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                            AND L6.JUDGE_KIND = VALUE(BD009.REMARK8, '0') ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "     AND T1.JUDGEMENT IN ('1', '3') ";
        }
        if ($model->field["OUTPUT"] == "3") {
            $query .= "     AND T1.JUDGEMENT = '2' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.TESTDIV,  ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
?>
