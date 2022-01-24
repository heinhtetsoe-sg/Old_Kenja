<?php

require_once('for_php7.php');

class knjl383jquery extends Query
{

    //実行
    public function selectCsvStudentQuery($model)
    {
        $query  = "";
        //受付データ（判定偏差値）
        $query .= " WITH T_RECEPT AS ( ";
        $query .= "     SELECT  CASE WHEN TESTDIV='5' THEN '0' ELSE TESTDIV END AS TESTDIV, ";
        $query .= "             EXAMNO,JUDGE_DEVIATION ";
        $query .= "     FROM    ENTEXAM_RECEPT_DAT ";
        $query .= "     WHERE   ENTEXAMYEAR = '".$model->examyear."' ";
        $query .= "     AND     JUDGE_DEVIATION IS NOT NULL ";
        $query .= "     ) ";
        //入試区分がMAXのレコード抽出
        $query .= " , T_TESTDIV AS ( ";
        $query .= "     SELECT  EXAMNO,MAX(TESTDIV) AS TESTDIV ";
        $query .= "     FROM    T_RECEPT ";
        $query .= "     GROUP BY EXAMNO ";
        $query .= "     ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T2.JUDGE_DEVIATION ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1, ";
        $query .= "     T_RECEPT T2, ";
        $query .= "     T_TESTDIV T3 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->examyear."' AND ";
        $query .= "     T1.PROCEDUREDIV = '1' AND ";
        $query .= "     T1.ENTDIV       = '1' AND ";
        $query .= "     T1.EXAMNO       = T2.EXAMNO AND ";
        $query .= "     T2.EXAMNO       = T3.EXAMNO AND ";
        $query .= "     T2.TESTDIV      = T3.TESTDIV ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";
        return $query;
    }
}
