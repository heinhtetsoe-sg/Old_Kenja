<?php

require_once('for_php7.php');

class knjx120query extends Query {


    //CVS作成用のQUERY
    function selectCsvQuery($model)
    {
		if($model->field["OUTPUT"]=="1")
		{
	        $query  = "select * from chair_dat";
		}
		if($model->field["OUTPUT"]=="2")
		{
	        $query  = "select * from chair_stf_dat";
		}
		if($model->field["OUTPUT"]=="3")
		{
	        $query  = "select * from chair_cls_dat";
		}
		if($model->field["OUTPUT"]=="4")
		{
	        $query  = "select * from chair_std_dat";
		}
		if($model->field["OUTPUT"]=="5")
		{
	        $query  = "select EXECUTEDATE,";
    	    $query .= "		CASE PERIODCD ";
        	$query .= "		WHEN '0' THEN '0' ";
	        $query .= "		WHEN '1' THEN 'A' ";
    	    $query .= "		WHEN '2' THEN '1' ";
        	$query .= "		WHEN '3' THEN '2' ";
	        $query .= "		WHEN '4' THEN '3' ";
    	    $query .= "		WHEN '5' THEN '4' ";
        	$query .= "		WHEN '6' THEN '5' ";
	        $query .= "		WHEN '7' THEN '6' ";
    	    $query .= "		WHEN '8' THEN '7' ";
        	$query .= "		WHEN '9' THEN 'P' ";
	        $query .= "		END AS PERIODCD,";
			$query .= "CHAIRCD,EXECUTED,DATADIV,YEAR,ATTESTOR,REGISTERCD,UPDATED from sch_chr_dat";
		}
		if($model->field["OUTPUT"]=="6")
		{
	        $query  = "select EXECUTEDATE,";
    	    $query .= "		CASE PERIODCD ";
        	$query .= "		WHEN '0' THEN '0' ";
	        $query .= "		WHEN '1' THEN 'A' ";
    	    $query .= "		WHEN '2' THEN '1' ";
        	$query .= "		WHEN '3' THEN '2' ";
	        $query .= "		WHEN '4' THEN '3' ";
    	    $query .= "		WHEN '5' THEN '4' ";
        	$query .= "		WHEN '6' THEN '5' ";
	        $query .= "		WHEN '7' THEN '6' ";
    	    $query .= "		WHEN '8' THEN '7' ";
        	$query .= "		WHEN '9' THEN 'P' ";
	        $query .= "		END AS PERIODCD,";
			$query .= "CHAIRCD,FACCD,YEAR,REGISTERCD,UPDATED from sch_fac_dat";
		}

        return $query;
    }


}
?>
