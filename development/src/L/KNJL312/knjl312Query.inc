<?php

require_once('for_php7.php');

class knjl312Query extends Query {

    //入試制度取得
    function get_apct_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT APPLICANTDIV FROM ENTEXAM_SCORE_DAT ";
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
        $query .= " TESTDIV IN( SELECT DISTINCT TESTDIV FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$year."')";	/* NO001 */
        $query .= " ORDER BY SHOWORDER, TESTDIV ";
        return $query;
    }
    //受験型名称取得
    function get_exam_div($namecd1, $namecd2)
    {
        $query  = " SELECT NAME1 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd1."' AND ";
        $query .= " NAMECD2 = '".$namecd2."' ";
        return $query;
    }
    //受験科目取得
    function get_subclas_div($namecd,$model)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM NAME_MST ";
        $query .= " WHERE ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 IN( SELECT DISTINCT TESTSUBCLASSCD FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->ObjYear."' ";	/* NO001 */
        $query .= " AND TESTDIV = '" .$model->field["TESTDV"]. "' ";	/* NO001 */
		if ($model->field["EXAM_TYPE"] != 0){
	        $query .= " AND EXAM_TYPE = '" .$model->field["EXAM_TYPE"]. "' ";	/* NO001 */
		}
        $query .= " )";	/* NO001 */
        return $query;
    }
    //試験会場名称取得
    function get_hall_data($testdv,$exam_typ,$subclass)
    {
        $query  = " SELECT EXAMHALL_NAME,EXAM_TYPE,EXAMHALLCD as VALUE ";
        $query .= " FROM ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= " TESTDIV = '".$testdv."' ";
		if($exam_typ == 0){
			if ($subclass > 2){
	        	$query .= " AND EXAM_TYPE = '2' ";
			}
		}else {
    	    $query .= " AND EXAM_TYPE = '".$exam_typ."' ";
		}
        return $query;
    }
}
?>
