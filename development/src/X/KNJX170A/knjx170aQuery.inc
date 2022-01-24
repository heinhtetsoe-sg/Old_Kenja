<?php

require_once('for_php7.php');
class knjx170aquery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX170A' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度一覧
    public function getSelectYear()
    {
        $query  = "SELECT DISTINCT YEAR ";
        $query .= "FROM V_ELECTCLASS_MST ";
        $query .= "ORDER BY YEAR DESC";

        return $query;
    }

    //実行
    public function insertQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $data = array();

            $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
            $data["GROUPCD"][TEXT]      = $data_arr[$i]["GROUPCD"];
            $data["GROUPNAME"][TEXT]    = $data_arr[$i]["GROUPNAME"];
            $data["GROUPABBV"][TEXT]    = $data_arr[$i]["GROUPABBV"];
            $data["REMARK"][TEXT]       = $data_arr[$i]["REMARK"];
            $data["SHOWORDER"][NUMBER]  = $data_arr[$i]["SHOWORDER"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";

            $query = Query::insertSQL($data, "ELECTCLASS_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除
    public function deleteQuery($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $count = get_count($data_arr);
        for ($i=0; $i<$count; $i++) {
            $query  = " DELETE FROM ELECTCLASS_DAT ";
            $query .= " WHERE YEAR = '".$data_arr[$i]["YEAR"]."' AND GROUPCD = '".$data_arr[$i]["GROUPCD"]."' ";
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //重複チェックSQL
    public function dataCntSql($year, $groupcd)
    {
        $query  = "SELECT COUNT(*) FROM ELECTCLASS_DAT WHERE YEAR = '".$year."' AND GROUPCD = '".$groupcd."'";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //データ出力用CVS作成用
    public function getselectDataSQL($year, $lastColumn)
    {
        $query  = "SELECT ";
        $query .= "    T1.YEAR, ";
        $query .= "    T1.GROUPCD, ";
        $query .= "    T1.GROUPNAME, ";
        $query .= "    T1.GROUPABBV, ";
        $query .= "    T1.REMARK, ";
        $query .= "    T1.SHOWORDER, ";
        $query .= "    '".$lastColumn."' AS ".$lastColumn." ";
        $query .= "FROM ";
        $query .= "    V_ELECTCLASS_MST T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".$year."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.GROUPCD ";

        return $query;
    }
}
