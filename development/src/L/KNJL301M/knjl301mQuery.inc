<?php

require_once('for_php7.php');

class knjl301mQuery extends Query {
    //入試制度
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

    //CVS作成用のQUERY
    function selectCsvQuery($model, $sex="")
    {
        $query  = " SELECT ";
        $query .= "     INT(T1.EXAMNO) AS EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     CASE T1.ERACD WHEN '1' THEN 'M' WHEN '2' THEN 'T' WHEN '3' THEN 'S' WHEN '4' THEN 'H' ELSE '' END || BIRTH_Y || '.' || BIRTH_M || '.' || BIRTH_D AS BIRTHDAY, ";
        $query .= "     T1.FS_NAME, ";
        $query .= "     T2.NATPUBPRI_NAME, ";
        $query .= "     T3.AREA_DIV_NAME, ";
        $query .= "     T4.AREA_NAME, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T5.GNAME, ";
        $query .= "     T5.GKANA ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_NATPUBPRI_MST T2 ON T1.FS_NATPUBPRIDIV = T2.NATPUBPRI_CD ";
        $query .= "     LEFT JOIN ENTEXAM_AREA_DIV_MST T3 ON ";
        $query .= "             T1.FS_NATPUBPRIDIV = T3.NATPUBPRI_CD AND T1.FS_AREA_DIV = T3.AREA_DIV_CD ";
        $query .= "     LEFT JOIN ENTEXAM_AREA_MST T4 ON ";
        $query .= "             T1.FS_NATPUBPRIDIV = T4.NATPUBPRI_CD AND T1.FS_AREA_DIV = T4.AREA_DIV_CD AND T1.FS_AREA_CD = T4.AREA_CD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T5 ON ";
        $query .= "             T1.ENTEXAMYEAR = T5.ENTEXAMYEAR AND T1.EXAMNO = T5.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->test_year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        if($model->field["TAISHOU"] == "2"){
            $query .= "     AND T1.EXAMNO BETWEEN '".$model->field["S_EXAMNO"]."' AND '".$model->field["E_EXAMNO"]."' ";
        }
        if($sex){
            $query .= "     AND T1.SEX = '".$sex."' ";
        }
        $query .= " ORDER BY ";
        if($model->field["SYUTURYOKUJUN"] == "2"){
            $query .= "     T1.FS_NAME ";
        } else {
            $query .= "     EXAMNO ";
        }

        return $query;
    }
}
?>
