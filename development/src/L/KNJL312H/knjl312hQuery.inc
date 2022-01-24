<?php

require_once('for_php7.php');

class knjl312hQuery extends Query {

    //入試制度取得
    function get_apct_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT APPLICANTDIV FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //入試区分取得
    function get_test_div($namecd,$year,$applicantdiv)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2,NAMESPARE2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT TESTDIV FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."' AND APPLICANTDIV = '".$applicantdiv."')";	
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //受験型取得
    function get_exam_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT EXAM_TYPE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //受験科目取得
    function get_subclas_div($namecd,$model)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$model->ObjYear."' AND ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT TESTSUBCLASSCD FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->ObjYear."' ";	
        $query .= " AND APPLICANTDIV = '" .$model->field["APDIV"]. "' ";	
        $query .= " AND TESTDIV = '" .$model->field["TESTDV"]. "' ";	
		if ($model->field["EXAM_TYPE"] != 0){
	        $query .= " AND EXAM_TYPE = '" .$model->field["EXAM_TYPE"]. "' ";	
		}
        $query .= " )";	
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //試験会場取得
    function get_hall_data($testdv,$exam_typ,$subclass)
    {
        $query  = " SELECT EXAMHALL_NAME,EXAM_TYPE,EXAMHALLCD as VALUE ";
        $query .= " FROM ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= " TESTDIV = '".$testdv."' ";
		if($exam_typ != 0){
    	    $query .= " AND EXAM_TYPE = '".$exam_typ."' ";
		}
        $query .= " ORDER BY EXAMHALLCD ";

        return $query;
    }
}
?>
