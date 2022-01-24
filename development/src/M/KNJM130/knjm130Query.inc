<?php

require_once('for_php7.php');


class knjm130Query extends Query {

    //講座
    function GetChair($model)
    {
		$query  = "WITH chairtable AS (SELECT ";
		$query .= "    CHAIRCD ";
		$query .= "FROM ";
		$query .= "    SCH_CHR_T_DAT ";
		$query .= "WHERE ";
		$query .= "    YEAR = '".$model->field["YEAR"]."' AND ";
		$query .= "    CHAIRCD NOT LIKE '92%' ";
		$query .= "GROUP BY ";
		$query .= "    CHAIRCD ";
		$query .= "), ";
		$query .= "chnm AS (SELECT ";
		$query .= "    CHAIRCD,MAX(CHAIRNAME) AS CHAIRNAME ";
		$query .= "FROM ";
		$query .= "    CHAIR_DAT ";
		$query .= "WHERE ";
		$query .= "    YEAR = '".$model->field["YEAR"]."' AND ";
		$query .= "    CHAIRCD NOT LIKE '92%' ";
		$query .= "GROUP BY ";
		$query .= "    CHAIRCD ";
		$query .= ") ";
		$query .= "SELECT ";
		$query .= "    t1.CHAIRCD, ";
		$query .= "    t2.CHAIRNAME ";
		$query .= "FROM ";
		$query .= "    chairtable t1 left join chnm t2 ON t1.CHAIRCD = t2.CHAIRCD ";
		$query .= "ORDER BY t1.CHAIRCD ";

        return $query;
    }

}
?>
