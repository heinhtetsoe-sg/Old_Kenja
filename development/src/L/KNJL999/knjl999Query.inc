<?php

require_once('for_php7.php');

class knjl999query extends Query {


    //マスタのSQL文作成(存在チェック用)
    function getMasterSQL($table, $where)
    {
        $query = "SELECT * FROM " . $table . " " . $where;

        return $query;
    }

    //FS_CDのMAX+1を取得
    function getMaxSQL($table)
    {
        $query = "SELECT CAST(MAX(FINSCHOOLCD) AS INTEGER)+1 AS FS_CD FROM " . $table;

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv(&$db, $data, $registercd)
    {
			$data1["FINSCHOOLCD"][TEXT] 		= $data["FS_CD"];
			$data1["FINSCHOOL_NAME"][TEXT] 		= $data["FINSCHOOL_NAME"];
			$data1["REGISTERCD"][TEXT] 			= $registercd;
			$data1["UPDATED"][NUMBER] 			= "sysdate()";

			$query = Query::insertSQL($data1, "FINSCHOOL_MST");

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
