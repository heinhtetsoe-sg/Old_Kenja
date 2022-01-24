<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjxsearch8Query.inc 56591 2017-10-22 13:04:39Z maeshiro $
class knjxsearch8Query extends Query {

    function SearchStudent($fields)
    {
        $query  = "SELECT ";
        $query .= "	 school_cd,";
        $query .= "	 school_name,";
        $query .= "	 buname,";
        $query .= "	 kaname,";
        $query .= "	 zipcd,";
        $query .= "	 addr1,";
        $query .= "	 telno ";
        $query .= "	 FROM college_mst ";
        if (strlen($fields["school_name"]) && strlen($fields["buname"]) && strlen($fields["kaname"])){
	        $query .= "	WHERE school_name like '%".$fields["school_name"]."%'";
	        $query .= "	  and buname like '%".$fields["buname"]."%'";
    	    $query .= "	  and kaname like '%".$fields["kaname"]."%'";
		} else {
			$flag = 0;
	        if (strlen($fields["school_name"]) && strlen($fields["buname"])){
		        $query .= "	WHERE school_name like '%".$fields["school_name"]."%'";
	        	$query .= "	  and buname like '%".$fields["buname"]."%'";
				$flag = 1;
			}
        	if (strlen($fields["buname"]) && strlen($fields["kaname"])){
	        	$query .= "	WHERE buname like '%".$fields["buname"]."%'";
    		    $query .= "	  and kaname like '%".$fields["kaname"]."%'";
				$flag = 1;
			}
	        if (strlen($fields["school_name"]) && strlen($fields["kaname"])){
		        $query .= "	WHERE school_name like '%".$fields["school_name"]."%'";
    		    $query .= "	  and kaname like '%".$fields["kaname"]."%'";
				$flag = 1;
			}
	        if ($flag == 0){
		        if (strlen($fields["school_name"]))
			        $query .= "	WHERE school_name like '%".$fields["school_name"]."%'";
		        if (strlen($fields["buname"]))
			        $query .= "	WHERE buname like '%".$fields["buname"]."%'";
		        if (strlen($fields["kaname"]))
			        $query .= "	WHERE kaname like '%".$fields["kaname"]."%'";
			}
		}
        $query .= " ORDER BY school_cd";

        return $query;
    }
}
?>
