<?php

require_once('for_php7.php');

class knjl302Query extends Query {

    //入試制度取得
    function get_apct_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT APPLICANTDIV FROM ENTEXAM_DESIRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	/* NO001 */
        return $query;
    }
    //入試区分取得
    function get_test_div($year)
    {
        $query  = " SELECT NAME AS NAME1,TESTDIV AS NAMECD2,SHOWDIV AS NAMESPARE2 ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$year."' AND ";
        $query .= " TESTDIV IN( SELECT DISTINCT TESTDIV FROM ENTEXAM_DESIRE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	/* NO001 */
        $query .= " ORDER BY SHOWORDER, TESTDIV ";
        return $query;
    }

    function getCsvSql($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T4.NAME AS TEST_NAME, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     SUBSTR(T1.EXAMNO,1,2) EXAMNO2, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T5.ABBV1 AS SEX_NAME, ";
        $query .= "     T1.FS_NAME,  ";
        $query .= "     T3.EXAMHALL_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT W1.TESTDIV, W1.EXAMNO, W2.NAME, W2.NAME_KANA, W2.SEX, W2.FS_NAME  ";
        $query .= "      FROM   ENTEXAM_DESIRE_DAT W1, ENTEXAM_APPLICANTBASE_DAT W2  ";
        $query .= "      WHERE  W1.ENTEXAMYEAR='{$model->ObjYear}'  ";
        $query .= "             AND W1.ENTEXAMYEAR=W2.ENTEXAMYEAR  ";
        $query .= "             AND W1.EXAMNO=W2.EXAMNO  ";
        $query .= "             AND W1.APPLICANTDIV='{$model->field["APDIV"]}' ";
        //'全て'以外の場合（入試区分）
        if ($model->field["TESTDV"] != "9") {
            $query .= "             AND W1.TESTDIV='{$model->field["TESTDV"]}' ";
        }
        $query .= "             AND W1.APPLICANT_DIV='1'  ";
        $query .= "             AND W1.EXAMINEE_DIV='1'  ";
        $query .= "             ) T1  ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR   = '{$model->ObjYear}' ";
        $query .= "                                    AND T2.APPLICANTDIV  = '{$model->field["APDIV"]}' ";
        $query .= "                                    AND T2.TESTDIV       = T1.TESTDIV ";
        $query .= "                                    AND T2.EXAMNO        = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_DAT T3 ON T2.TESTDIV = T3.TESTDIV ";
        $query .= "          AND T2.EXAM_TYPE = T3.EXAM_TYPE ";
        $query .= "          AND T2.RECEPTNO BETWEEN T3.S_RECEPTNO AND T3.E_RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_TESTDIV_MST T4 ON T4.ENTEXAMYEAR='{$model->ObjYear}' AND T4.TESTDIV=T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST T5 ON T5.NAMECD1='Z002' AND T5.NAMECD2=T1.SEX ";
        $query .= " ORDER BY  ";
        $query .= "     T1.TESTDIV, T1.EXAMNO ";
        return $query;
    }
}
?>
