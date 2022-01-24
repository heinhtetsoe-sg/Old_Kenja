<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjz420Query.inc 56591 2017-10-22 13:04:39Z maeshiro $
class knjz420Query extends Query {

	//１レコード取得
	function getRow($cd)
	{
		$db = Query::dbCheckOut();

		$query = "SELECT * FROM COMPANY_MST ".
				 " WHERE COMPANY_CD = '" .$cd ."'";

		$row = $db->getRow($query, DB_FETCHMODE_ASSOC);
		Query::dbCheckIn($db);
		return $row;
	}

	//一覧取得
	function getList()
	{

		$query  = " SELECT COMPANY_CD,COMPANY_NAME,SHUSHOKU_ADDR,SHIHONKIN,SONINZU,TONINZU, ";
		$query .= "        COMPANY_SORT || ':' || T2.NAME1 AS COMPANY_SORT, ";
		$query .= "        TARGET_SEX || ':' || T3.NAME1 AS TARGET_SEX, ";
		$query .= "        ZIPCD,ADDR1,ADDR2,TELNO,REMARK ";
		$query .= " FROM COMPANY_MST ";
		$query .= " LEFT JOIN V_NAME_MST T2 ON T2.YEAR = '".CTRL_YEAR."' ";
		$query .= "           AND T2.NAMECD1 = 'E008' AND T2.NAMECD2 = COMPANY_SORT ";
		$query .= " LEFT JOIN V_NAME_MST T3 ON T3.YEAR = '".CTRL_YEAR."' ";
		$query .= "           AND T3.NAMECD1 = 'E007' AND T3.NAMECD2 = TARGET_SEX ";
		$query .= " ORDER BY COMPANY_CD ";

		return $query;
	}

	//職種の取得
	function getCompanycd(){

		$query  = " SELECT NAMECD2 AS VALUE,NAMECD2 || ':' || NAME1 AS LABEL ";
		$query .= " FROM V_NAME_MST ";
		$query .= " WHERE YEAR='".CTRL_YEAR."' ";
		$query .= "       AND NAMECD1='E008' ";
		$query .= " ORDER BY NAMECD2";
		return $query;

	}

	//募集対象の取得
	function getTarget(){

		$query  = " SELECT NAMECD2 AS VALUE,NAMECD2 || ':' || NAME1 AS LABEL ";
		$query .= " FROM V_NAME_MST ";
		$query .= " WHERE YEAR='".CTRL_YEAR."' ";
		$query .= "       AND NAMECD1='E007' ";
		$query .= " ORDER BY NAMECD2";
		return $query;

	}

	//INSERT
	function &getInsertQuery($fields)
	{
		$db = Query::dbCheckOut();

		$data["COMPANY_CD"][TEXT]    = $fields["COMPANY_CD"];
		$data["COMPANY_NAME"][TEXT]  = $fields["COMPANY_NAME"];
		$data["SHUSHOKU_ADDR"][TEXT] = $fields["SHUSHOKU_ADDR"];
		$data["SHIHONKIN"][TEXT]     = $fields["SHIHONKIN"];
		$data["SONINZU"][NUMBER]     = $fields["SONINZU"];
		$data["TONINZU"][NUMBER]     = $fields["TONINZU"];
		$data["COMPANY_SORT"][TEXT]  = $fields["COMPANY_SORT"];
		$data["TARGET_SEX"][TEXT]    = $fields["TARGET_SEX"];
		$data["ZIPCD"][TEXT]         = $fields["ZIPCD"];
		$data["ADDR1"][TEXT]         = $fields["ADDR1"];
		$data["ADDR2"][TEXT]         = $fields["ADDR2"];
		$data["TELNO"][TEXT]         = $fields["TELNO"];
		$data["REMARK"][TEXT]        = $fields["REMARK"];
		$data["REGISTERCD"][TEXT]    = STAFFCD;
		$data["UPDATED"][NUMBER]     = "sysdate()";

		$query = Query::insertSQL($data, "COMPANY_MST");

		$db->query($query);

		Query::dbCheckIn($db);
		return;
	}

	//UPDATE
	function &getUpdateQuery($fields)
	{
		$db = Query::dbCheckOut();

		$data["COMPANY_CD"][TEXT]    = $fields["COMPANY_CD"];
		$data["COMPANY_NAME"][TEXT]  = $fields["COMPANY_NAME"];
		$data["SHUSHOKU_ADDR"][TEXT] = $fields["SHUSHOKU_ADDR"];
		$data["SHIHONKIN"][TEXT]     = $fields["SHIHONKIN"];
		$data["SONINZU"][NUMBER]     = $fields["SONINZU"];
		$data["TONINZU"][NUMBER]     = $fields["TONINZU"];
		$data["COMPANY_SORT"][TEXT]  = $fields["COMPANY_SORT"];
		$data["TARGET_SEX"][TEXT]    = $fields["TARGET_SEX"];
		$data["ZIPCD"][TEXT]         = $fields["ZIPCD"];
		$data["ADDR1"][TEXT]         = $fields["ADDR1"];
		$data["ADDR2"][TEXT]         = $fields["ADDR2"];
		$data["TELNO"][TEXT]         = $fields["TELNO"];
		$data["REMARK"][TEXT]        = $fields["REMARK"];
		$data["REGISTERCD"][TEXT]    = STAFFCD;
		$data["UPDATED"][NUMBER]     = "sysdate()";
		$where = " WHERE COMPANY_CD = '" .$fields["COMPANY_CD"]."'";

		$query = Query::updateSQL($data, "COMPANY_MST", $where);

		$db->query($query);
		Query::dbCheckIn($db);
		return;
	}

	//DELETE
	function &getDeleteQuery($fields)
	{
		$db = Query::dbCheckOut();

		$query = "DELETE FROM COMPANY_MST WHERE COMPANY_CD  = '" .$fields["COMPANY_CD"] ."'";

		$db->query($query);
		Query::dbCheckIn($db);
		
		return $result;
	}
}
?>
