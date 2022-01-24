<?php

require_once('for_php7.php');


class knjm420Query extends Query {

    //講座
    function GetClass($model)
    {
		$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
		$query .= "FROM SCHREG_REGD_HDAT ";
		$query .= "WHERE YEAR='" .CTRL_YEAR ."'";
		$query .= "AND SEMESTER='".$model->semester ."'";
		$query .= " ORDER BY GRADE,HR_CLASS ";

        return $query;
    }

}
?>
