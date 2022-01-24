<?php

require_once('for_php7.php');

class knjl076pquery extends Query {
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

    //CSVデータ出力
    function selectCsvQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.JUDGEDIV, ";
        $query .= "     L1.NAME1 AS JUDGEDIV_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ";
        $query .= "          ON T1.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO       = T2.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1      = 'L013' ";
        $query .= "         AND T1.JUDGEDIV     = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        if ($model->field["JUDGEDIV"] != "ALL") {
            $query .= " AND T1.JUDGEDIV     = '".$model->field["JUDGEDIV"]."' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["SORT"] == "2") {
            $query .= "     VALUE(T1.JUDGEDIV,'99'), ";
        }
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
?>
