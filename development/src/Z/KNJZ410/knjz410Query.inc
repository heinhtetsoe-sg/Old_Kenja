<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjz410Query.inc 56591 2017-10-22 13:04:39Z maeshiro $
class knjz410Query extends Query {

	//１レコード取得
	function getRow($cd)
	{
		$db = Query::dbCheckOut();

		$query = "SELECT * FROM COLLEGE_MST ".
				 " WHERE SCHOOL_CD = '" .$cd ."'";

		$row = $db->getRow($query, DB_FETCHMODE_ASSOC);
		Query::dbCheckIn($db);
		return $row;
	}

	//一覧取得
	function getList()
	{

		$query  = " SELECT SCHOOL_CD,SCHOOL_NAME,BUNAME,KANAME,SCHOOL_SORT || ':' || T2.NAME1 AS SCHOOL_SORT, ";
		$query .= "        BUNYA || ':' || T3.NAME1 AS BUNYA,AREA_NAME,ZIPCD,ADDR1,ADDR2,TELNO,GREDES ";
		$query .= " FROM COLLEGE_MST ";
		$query .= " LEFT JOIN V_NAME_MST T2 ON T2.YEAR = '".CTRL_YEAR."' ";
		$query .= "           AND T2.NAMECD1 = 'E001' AND T2.NAMECD2 = SCHOOL_SORT ";
		$query .= " LEFT JOIN V_NAME_MST T3 ON T3.YEAR = '".CTRL_YEAR."' ";
		$query .= "           AND T3.NAMECD1 = 'E009' AND T3.NAMECD2 = BUNYA ";
		$query .= " ORDER BY SCHOOL_CD ";

		return $query;
	}

	//学校の取得
	function getSchoolcd(){

		$query  = " SELECT NAMECD2 AS VALUE,NAMECD2 || ':' || NAME1 AS LABEL ";
		$query .= " FROM V_NAME_MST ";
		$query .= " WHERE YEAR='".CTRL_YEAR."' ";
		$query .= "       AND NAMECD1='E001' ";
		$query .= "       AND NAMECD2 in ('01','02','03','04') ";
		$query .= " ORDER BY NAMECD2";
		return $query;

	}

	//分野の取得
	function getBunya(){

		$query  = " SELECT NAMECD2 AS VALUE,NAMECD2 || ':' || NAME1 AS LABEL ";
		$query .= " FROM V_NAME_MST ";
		$query .= " WHERE YEAR='".CTRL_YEAR."' ";
		$query .= "       AND NAMECD1='E009' ";
		$query .= " ORDER BY NAMECD2";
		return $query;

	}

	//INSERT
	function &getInsertQuery($fields)
	{
		$db = Query::dbCheckOut();

		$data["SCHOOL_CD"][TEXT]   = $fields["SCHOOL_CD"];
		$data["SCHOOL_NAME"][TEXT] = $fields["SCHOOL_NAME"];
		$data["BUNAME"][TEXT]      = $fields["BUNAME"];
		$data["KANAME"][TEXT]      = $fields["KANAME"];
		$data["SCHOOL_SORT"][TEXT] = $fields["SCHOOL_SORT"];
		$data["BUNYA"][TEXT]       = $fields["BUNYA"];
		$data["AREA_NAME"][TEXT]   = $fields["AREA_NAME"];
		$data["ZIPCD"][TEXT]       = $fields["ZIPCD"];
		$data["ADDR1"][TEXT]       = $fields["ADDR1"];
		$data["ADDR2"][TEXT]       = $fields["ADDR2"];
		$data["TELNO"][TEXT]       = $fields["TELNO"];
		$data["GREDES"][TEXT]      = $fields["GREDES"];
		$data["REGISTERCD"][TEXT]  = STAFFCD;
		$data["UPDATED"][NUMBER]   = "sysdate()";

		$query = Query::insertSQL($data, "COLLEGE_MST");

		$db->query($query);

		Query::dbCheckIn($db);
		return;
	}

	//UPDATE
	function &getUpdateQuery($fields)
	{
		$db = Query::dbCheckOut();

		$data["SCHOOL_CD"][TEXT]   = $fields["SCHOOL_CD"];
		$data["SCHOOL_NAME"][TEXT] = $fields["SCHOOL_NAME"];
		$data["BUNAME"][TEXT]      = $fields["BUNAME"];
		$data["KANAME"][TEXT]      = $fields["KANAME"];
		$data["SCHOOL_SORT"][TEXT] = $fields["SCHOOL_SORT"];
		$data["BUNYA"][TEXT]       = $fields["BUNYA"];
		$data["AREA_NAME"][TEXT]   = $fields["AREA_NAME"];
		$data["ZIPCD"][TEXT]       = $fields["ZIPCD"];
		$data["ADDR1"][TEXT]       = $fields["ADDR1"];
		$data["ADDR2"][TEXT]       = $fields["ADDR2"];
		$data["TELNO"][TEXT]       = $fields["TELNO"];
		$data["GREDES"][TEXT]      = $fields["GREDES"];
		$data["REGISTERCD"][TEXT]  = STAFFCD;
		$data["UPDATED"][NUMBER]   = "sysdate()";
		$where = " WHERE SCHOOL_CD = '" .$fields["SCHOOL_CD"]."'";

		$query = Query::updateSQL($data, "COLLEGE_MST", $where);

		$db->query($query);
		Query::dbCheckIn($db);
		return;
	}

	//DELETE
	function &getDeleteQuery($fields)
	{
		$db = Query::dbCheckOut();

		$query = "DELETE FROM COLLEGE_MST WHERE SCHOOL_CD  = '" .$fields["SCHOOL_CD"] ."'";

		$db->query($query);
		Query::dbCheckIn($db);
		
		return $result;
	}
}
?>
