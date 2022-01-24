<?php

require_once('for_php7.php');

class knjl324jQuery extends Query 
{

    //入試区分取得
    function get_test_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2,NAMESPARE2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."' ";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }


    //CSVデータ
    function selectCsvQuery($model)
    {

        $query  = " WITH ";
        $query .= " SELECT_EXAMNO AS( ";
        $query .= "     SELECT ";
        $query .= "         row_number() over(ORDER BY EXAMNO) AS RECORD_CNT01, ";
        $query .= "         MOD((CASE WHEN MOD(row_number() over(),1)=0 THEN (row_number() over())-1 ";
        $query .= "                   WHEN MOD(row_number() over(),1)=1 THEN (row_number() over())-2 ";
        $query .= "                   ELSE (row_number() over()) END),3) AS REMAINDER01, ";
        $query .= "         EXAMNO   ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT ";
        $query .= "     WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' AND  ";
        $query .= "         TESTDIV      = '".$model->field["TESTDIV"]."' AND  ";
        $query .= "         JUDGEDIV     = '1'  ";
        $query .= "     ORDER BY ";
        $query .= "         EXAMNO ";
        $query .= " ),SELECT_LEFT AS( ";
        $query .= "     SELECT ";
        $query .= "         RECORD_CNT01, ";
        $query .= "         row_number() over() AS RECORD_CNT02, ";
        $query .= "         REMAINDER01, ";
        $query .= "         EXAMNO ";
        $query .= "     FROM ";
        $query .= "         SELECT_EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         REMAINDER01=0 ";
        $query .= "),SELECT_CENTER AS( ";
        $query .= "     SELECT ";
        $query .= "         RECORD_CNT01, ";
        $query .= "         row_number() over() AS RECORD_CNT02, ";
        $query .= "         REMAINDER01, ";
        $query .= "         EXAMNO ";
        $query .= "     FROM ";
        $query .= "         SELECT_EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         REMAINDER01=1  ";
        $query .= "),SELECT_RIGHT AS( ";
        $query .= "     SELECT ";
        $query .= "         RECORD_CNT01, ";
        $query .= "         row_number() over() AS RECORD_CNT02, ";
        $query .= "         REMAINDER01, ";
        $query .= "         EXAMNO ";
        $query .= "     FROM ";
        $query .= "         SELECT_EXAMNO ";
        $query .= "     WHERE ";
        $query .= "         REMAINDER01=2 ";
        $query .= ")  ";

        $query .= " SELECT ";
        $query .= "     L.RECORD_CNT01  AS L_RECORD_CNT01, ";
        $query .= "     L.RECORD_CNT02  AS L_RECORD_CNT02, ";
        $query .= "     L.REMAINDER01   AS L_REMAINDER01, ";
        $query .= "     L.EXAMNO        AS L_EXAMNO, ";
        $query .= "     C.RECORD_CNT01  AS C_RECORD_CNT01, ";
        $query .= "     C.RECORD_CNT02  AS C_RECORD_CNT02, ";
        $query .= "     C.REMAINDER01   AS C_REMAINDER01, ";
        $query .= "     C.EXAMNO        AS C_EXAMNO, ";
        $query .= "     R.RECORD_CNT01  AS R_RECORD_CNT01, ";
        $query .= "     R.RECORD_CNT02  AS R_RECORD_CNT02, ";
        $query .= "     R.REMAINDER01   AS R_REMAINDER01, ";
        $query .= "     R.EXAMNO        AS R_EXAMNO ";
        $query .= " FROM ";
        $query .= "     SELECT_LEFT AS L   ";
        $query .= "     LEFT OUTER JOIN SELECT_CENTER AS C ON L.RECORD_CNT02=C.RECORD_CNT02 ";
        $query .= "     LEFT OUTER JOIN SELECT_RIGHT AS R ON L.RECORD_CNT02=R.RECORD_CNT02 ";

        return $query;
    }

}
?>
