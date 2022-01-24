<?php

require_once('for_php7.php');

class knjl305Query extends Query {

    //入試制度取得
    function get_apct_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT APPLICANTDIV FROM ENTEXAM_RECEPT_DAT ";
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
        $query .= " TESTDIV IN( SELECT DISTINCT TESTDIV FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	/* NO001 */
        $query .= " ORDER BY SHOWORDER, TESTDIV ";
        return $query;
    }
    //受験型取得
    function get_exam_div($namecd1, $namecd2)
    {
        $query  = " SELECT NAME1";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd1."' AND ";
        $query .= " NAMECD2 = '".$namecd2."' ";
        return $query;
    }
    //試験会場名称取得
    function get_hall_data($testdv,$exam_typ)
    {
        $query  = " SELECT EXAMHALL_NAME,EXAM_TYPE,EXAMHALLCD as VALUE ";
        $query .= " FROM ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
		if($exam_typ == 0){
	        $query .= " TESTDIV = '".$testdv."'";
		}else {
	        $query .= " TESTDIV = '".$testdv."' AND ";
    	    $query .= " EXAM_TYPE = '".$exam_typ."'";
		}
        return $query;
    }
}
?>