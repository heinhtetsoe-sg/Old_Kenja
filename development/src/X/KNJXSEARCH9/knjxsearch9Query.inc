<?php

require_once('for_php7.php');
// kanji=����
// $Id: knjxsearch9Query.inc 56591 2017-10-22 13:04:39Z maeshiro $
class knjxsearch9Query extends Query {

    function SearchStudent($fields)
    {
        $query  = "SELECT * ";
        $query .= "	 FROM company_mst ";
        if (strlen($fields["company_name"]) && strlen($fields["shushoku_addr"])){
            $query .= "	WHERE company_name like '%".$fields["company_name"]."%'";
            $query .= "	and shushoku_addr like '%".$fields["shushoku_addr"]."%'";
		} else {
	        if (strlen($fields["company_name"]))
	            $query .= "	WHERE company_name like '%".$fields["company_name"]."%'";
	        if (strlen($fields["shushoku_addr"]))
	            $query .= "	WHERE shushoku_addr like '%".$fields["shushoku_addr"]."%'";
		}
        $query .= " ORDER BY company_cd";

        return $query;
    }
}
?>
