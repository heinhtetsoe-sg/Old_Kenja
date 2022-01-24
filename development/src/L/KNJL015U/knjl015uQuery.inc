<?php

require_once('for_php7.php');

class knjl015uquery extends Query {
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

    //CSVデータ出力（願書）
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";//Modelで姓名に分割
        $query .= "     L2.EMERGENCYTELNO, ";
        $query .= "     T1.RECOM_EXAMNO ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L2 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L2.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L2.APPLICANTDIV ";
        $query .= "             AND T1.EXAMNO           = L2.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }
}
?>
