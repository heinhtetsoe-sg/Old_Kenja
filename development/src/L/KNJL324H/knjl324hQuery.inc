<?php

require_once('for_php7.php');

class knjl324hQuery extends Query {

    //入試制度取得
    function get_apct_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT APPLICANTDIV FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //入試区分取得
    function get_test_div($namecd,$year,$applicantdiv)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2,NAMESPARE1,NAMESPARE2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT TESTDIV FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."' AND APPLICANTDIV = '".$applicantdiv."')";	
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //合格者取得（CSV用）
    function getNo($model)
    {
        $query  = " SELECT TESTDIV, JUDGEDIV, EXAMNO ";
        $query .= " FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ";
        $query .= " ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= " APPLICANTDIV = '".$model->field["APDIV"]."' AND ";
	    //入試区分が全て以外
		if ($model->field["TESTDV"] != "0") 
	        $query .= " TESTDIV = '".$model->field["TESTDV"]."' AND ";
        if ($this->field["APDIV"] == "1" || $this->field["APDIV"] == "2") {
            $query .= " JUDGEDIV in ('1','3') ";
        } else {
            $query .= " JUDGEDIV = '1' ";
        }
        $query .= " ORDER BY TESTDIV, JUDGEDIV, EXAMNO ";

        return $query;
    }

    //入試区分・入試区分名称・入試日付を取得（CSV用）
    function getNameMst($model)
    {
        if ($model->field["APDIV"] == "1") {
            $query  = " SELECT NAMECD2 AS TEST_CD, NAME1 AS TEST_NAME ";
            $query .= " FROM V_NAME_MST ";
            $query .= " WHERE YEAR = '".$model->ObjYear."' AND NAMECD1 = 'L004' ";
            $query .= " ORDER BY NAMECD2 ";
        } else {
            $query  = " SELECT '1' AS TEST_CD, ABBV1 AS TEST_NAME ";
            $query .= " FROM V_NAME_MST ";
            $query .= " WHERE YEAR = '".$model->ObjYear."' AND NAMECD1 = 'L003' AND NAMECD2 = '".$model->field["APDIV"]."' ";
        }
        return $query;
    }
}
?>
