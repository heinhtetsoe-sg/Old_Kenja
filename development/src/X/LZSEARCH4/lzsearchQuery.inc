<?php

require_once('for_php7.php');

class lzsearchQuery extends Query {

	//NAME_MST�����������
    function getNameMst($cd1,$cd2)
    {

		$query  = " SELECT DISTINCT T1.NAMECD2,T1.NAME1";
		$query .= " FROM NAME_MST T1,NAMEYEAR_DAT T2";
		$query .= " WHERE   T1.NAMECD1  = '" .$cd1 ."'";
		$query .= "     AND T2.NAMEYEAR = '" .$cd2 ."'";
		$query .= "     AND T2.NAMECD1  = T1.NAMECD1";
		$query .= "     AND T2.NAMECD2  = T1.NAMECD2";

		return $query;
    }

    function SearchStudent($fields)
    {
        $query  = "select * ";
        $query .= "	 from company_mst ";
        if (strlen($fields["company_name"]) && strlen($fields["shushoku_add"])){
            $query .= "	where company_name like '%".$fields["company_name"]."%'";
            $query .= "	and shushoku_add like '%".$fields["shushoku_add"]."%'";
		} else {
	        if (strlen($fields["company_name"]))
	            $query .= "	where company_name like '%".$fields["company_name"]."%'";
	        if (strlen($fields["shushoku_add"]))
	            $query .= "	where shushoku_add like '%".$fields["shushoku_add"]."%'";
		}
        $query .= " ORDER BY company_cd";

        return $query;
    }
}
?>
