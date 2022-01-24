<?php

require_once('for_php7.php');

class knjx030bQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX030B' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }


    //マスタ(存在チェック用)
    function getMasterCheck($table, $where)
    {
        $query = "SELECT UPDATED FROM " . $table . " " . $where;

        return $query;
    }


    //マスタ取得SQL
    function getMasterSQL($table, $where)
    {
        $query = "SELECT * FROM " . $table . " " . $where;

        return $query;
    }


    //マスタデータの削除
    function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM schreg_address_dat ";
        $query .= "where SCHREGNO = '".$data["SCHREGNO"]."' AND ISSUEDATE = '".$data["ISSUEDATE"]."'";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv(&$db, $data, $registercd)
    {

			$data1["SCHREGNO"][TEXT] 			= $data["SCHREGNO"];
			$data1["ISSUEDATE"][TEXT] 			= $data["ISSUEDATE"];
			$data1["EXPIREDATE"][TEXT] 			= $data["EXPIREDATE"];
			$data1["ZIPCD"][TEXT] 				= $data["ZIPCD"];
			$data1["AREACD"][TEXT] 				= $data["AREACD"];
			$data1["ADDR1"][TEXT] 				= $data["ADDR1"];
			$data1["ADDR2"][TEXT] 				= $data["ADDR2"];
			$data1["ADDR1_ENG"][TEXT] 			= $data["ADDR1_ENG"];
			$data1["ADDR2_ENG"][TEXT] 			= $data["ADDR2_ENG"];
			$data1["TELNO"][TEXT] 				= $data["TELNO"];
			$data1["FAXNO"][TEXT] 				= $data["FAXNO"];
			$data1["EMAIL"][TEXT] 				= $data["EMAIL"];
//			$data1["EMERGENCYCALL"][TEXT] 		= $data["EMERGENCYCALL"];  2005.10.18 m-yama
//			$data1["EMERGENCYTELNO"][TEXT] 		= $data["EMERGENCYTELNO"]; 2005.10.18 m-yama

			$data1["REGISTERCD"][TEXT] 			= $registercd;
			$data1["UPDATED"][NUMBER] 			= "sysdate()";

			$query = Query::insertSQL($data1, "schreg_address_dat");

            $result = $db->query($query);

    }


    //マスタの更新（ＣＳＶデータより読込）
    function updateQueryCsv(&$db, $data, $registercd)
    {

			$data1["SCHREGNO"][TEXT] 			= $data["SCHREGNO"];
			$data1["ISSUEDATE"][TEXT] 			= $data["ISSUEDATE"];
			$data1["EXPIREDATE"][TEXT] 			= $data["EXPIREDATE"];
			$data1["ZIPCD"][TEXT] 				= $data["ZIPCD"];
			$data1["AREACD"][TEXT] 				= $data["AREACD"];
			$data1["ADDR1"][TEXT] 				= $data["ADDR1"];
			$data1["ADDR2"][TEXT] 				= $data["ADDR2"];
			$data1["ADDR1_ENG"][TEXT] 			= $data["ADDR1_ENG"];
			$data1["ADDR2_ENG"][TEXT] 			= $data["ADDR2_ENG"];
			$data1["TELNO"][TEXT] 				= $data["TELNO"];
			$data1["FAXNO"][TEXT] 				= $data["FAXNO"];
			$data1["EMAIL"][TEXT] 				= $data["EMAIL"];
//			$data1["EMERGENCYCALL"][TEXT] 		= $data["EMERGENCYCALL"];  2005.10.18 m-yama
//			$data1["EMERGENCYTELNO"][TEXT] 		= $data["EMERGENCYTELNO"]; 2005.10.18 m-yama

			$data1["REGISTERCD"][TEXT] 			= $registercd;
			$data1["UPDATED"][NUMBER] 			= "sysdate()";

	      	$where  = " WHERE SCHREGNO = '" .$data["SCHREGNO"] ."' ";
			$where .= "and ISSUEDATE = '".$data["ISSUEDATE"]."'";
			$query = Query::updateSQL($data1, "schreg_address_dat", $where);

            $result = $db->query($query);

    }


    //エラーデータの削除
    function deleteQueryErr()
    {
        $query  = "DELETE FROM w_csvmsg_dat ";
        return $query;
    }


    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {

			$data1["MSGROW"][NUMBER] 		= $record_no;
			$data1["MSGREMARK"][TEXT] 		= $check_error;

			$query = Query::insertSQL($data1, "w_csvmsg_dat");

            $result = $db->query($query);

    }


    //CVS作成用のQUERY
    function selectCsvQuery()
    {
        $query  = "select * from w_csvmsg_dat";

        return $query;
    }


}
?>
