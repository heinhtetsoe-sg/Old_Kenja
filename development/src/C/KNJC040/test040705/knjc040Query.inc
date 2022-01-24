<?php

require_once('for_php7.php');

class knjc040Query extends Query {

    //年組取得（権限チェック）
    function getAuth($year,$semester)
    {
		//参照・更新可
		if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."'";
			$query .= "AND SEMESTER='".$semester ."'";
		}
		//参照・更新可（制限付き）
		if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
			$query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
			$query .= "FROM SCHREG_REGD_HDAT ";
			$query .= "WHERE YEAR='" .$year ."' ";
			$query .= "AND SEMESTER='".$semester ."' ";
	    	$query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD2 = '" .STAFFCD ."' ";
	    	$query .= "	OR TR_CD3 = '" .STAFFCD ."') ";
		}

        return $query;
    }


    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM subclass_mst ".
                 " WHERE SUBCLASSCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //年度取得
    function getYear()
    {
        $db = Query::dbCheckOut();
        $query = "SELECT CTRL_CHAR1 AS YEAR ".
                    "FROM control_mst WHERE CTRL_CD1= 'B201' AND CTRL_CD2='1001'";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //年組取得
    function getHrclass($YEAR)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT GRADE AS VALUE,GRADE || '年' || HR_CLASS || '組' AS LABEL ".
                    "FROM SCHREG_REGD_DAT ".
                    "GROUP BY YEAR,GRADE,HR_CLASS ".
                    "HAVING YEAR='" .$YEAR ."'";
        $result = $db->query($query);
        for($i=0;$row[$i] = $result->fetchRow(DB_FETCHMODE_ASSOC);$i++)
            print($row[$i]["VALUE"]);
        $result->free();
        Query::dbCheckIn($db);
        return $row;
    }

    /**
     * INSERT
     */
    function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();
        
        $query = "INSERT INTO subclass_mst(SUBCLASSCD,SUBCLASSNAME,SUBCLASSABBV,";
        $query.= "ELECTSUBCLASSNAME,UPDATED ) ";
        $query.= "VALUES('" .$fields["SUBCLASSCD"] ."'";
        $query.= ",'" .$fields["SUBCLASSNAME"] ."'";
        $query.= ",'" .$fields["SUBCLASSABBV"] ."'";
        $query.= ",'" .$fields["ELECTSUBCLASSNAME"] ."'";
        $query.= ",sysdate())";
        
        $db->query($query);
        
        Query::dbCheckIn($db);
        return;
    }
    /**
     * UPDATE
     */
    function &getUpdateQuery($fields)
    {        
        $db = Query::dbCheckOut();
        
        $query = "UPDATE subclass_mst SET ";
        $query.= "SUBCLASSCD          = '" .$fields["SUBCLASSCD"] ."'";
        $query.= ",SUBCLASSNAME       = '" .$fields["SUBCLASSNAME"] ."'";
        $query.= ",SUBCLASSABBV       = '" .$fields["SUBCLASSABBV"] ."'";
        $query.= ",ELECTSUBCLASSNAME  = '" .$fields["ELECTSUBCLASSNAME"] ."'";
        $query.= ",UPDATED       = sysdate()";
        $query .= " WHERE SUBCLASSCD       = '" .$fields["SUBCLASSCD"] ."'";
        
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    /**
     * DELETE
     */
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM subclass_mst "
                ." WHERE SUBCLASSCD  = '" .$fields["SUBCLASSCD"] ."'";

        $db->query($query);
        Query::dbCheckIn($db);
        
        return $result;
    }
}
?>
