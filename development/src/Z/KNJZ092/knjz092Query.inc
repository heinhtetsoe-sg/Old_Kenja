<?php

require_once('for_php7.php');
class knjz092Query extends Query
{

    //一覧用データ取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.FINSCHOOL_TYPE || ':' || L2.NAME1 AS FINSCHOOL_TYPE, ";
        $query .= "     T1.FINSCHOOL_DISTCD || ':' || L3.NAME1 AS FINSCHOOL_DISTCD, ";
        $query .= "     T1.FINSCHOOL_DISTCD2 || ':' || L6.NAME1 AS FINSCHOOL_DISTCD2, ";
        $query .= "     T1.FINSCHOOL_DIV || ':' || L4.NAME1 AS FINSCHOOL_DIV, ";
        $query .= "     T1.FINSCHOOL_NAME, ";
        $query .= "     T1.FINSCHOOL_KANA, ";
        $query .= "     T1.FINSCHOOL_NAME_ABBV, ";
        $query .= "     T1.FINSCHOOL_KANA_ABBV, ";
        $query .= "     T1.PRINCNAME, ";
        $query .= "     T1.PRINCNAME_SHOW, ";
        $query .= "     T1.PRINCKANA, ";
        $query .= "     T1.DISTRICTCD || ':' || L5.NAME1 AS DISTRICTCD, ";
        $query .= "     T1.FINSCHOOL_ZIPCD, ";
        $query .= "     T1.FINSCHOOL_ADDR1, ";
        $query .= "     T1.FINSCHOOL_ADDR2, ";
        $query .= "     T1.FINSCHOOL_TELNO, ";
        $query .= "     T1.FINSCHOOL_FAXNO, ";
        $query .= "     T1.EDBOARDCD || ':' || L1.EDBOARDNAME AS EDBOARDCD, ";
        $query .= "     T1.REGISTERCD, ";
        $query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     FINHIGHSCHOOL_MST T1 ";
        $query .= " LEFT JOIN EDBOARD_MST L1 ON L1.EDBOARDCD = T1.EDBOARDCD ";
        $query .= " LEFT JOIN NAME_MST L2 ON T1.FINSCHOOL_TYPE     = L2.NAMECD2 AND L2.NAMECD1 = 'L019' ";
        $query .= " LEFT JOIN NAME_MST L3 ON T1.FINSCHOOL_DISTCD   = L3.NAMECD2 AND L3.NAMECD1 = 'L001' ";
        $query .= " LEFT JOIN NAME_MST L4 ON T1.FINSCHOOL_DIV      = L4.NAMECD2 AND L4.NAMECD1 = 'L015' ";
        $query .= " LEFT JOIN NAME_MST L5 ON T1.DISTRICTCD         = L5.NAMECD2 AND L5.NAMECD1 = 'Z003' ";
        $query .= " LEFT JOIN NAME_MST L6 ON T1.FINSCHOOL_DISTCD2  = L6.NAMECD2 AND L6.NAMECD1 = 'Z015' ";
        $query .= " ORDER BY ";
        $query .= "     FINSCHOOLCD ";

        return $query ;
    }

    //１レコード取得
    public function getRow($cd)
    {
        $query  = " SELECT * FROM FINHIGHSCHOOL_MST WHERE FINSCHOOLCD = '".$cd ."'";

        return $query;
    }

    //名称取得
    public function getNameMst($namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教育委員会コード取得
    public function getEdboardMst()
    {
        $query  = " SELECT ";
        $query .= "     EDBOARDCD || ' ' || EDBOARDNAME AS LABEL, ";
        $query .= "     EDBOARDCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     EDBOARD_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSV書出し
    public function getCsvData()
    {
        $query  = " SELECT ";
        $query .= "     FINSCHOOLCD, ";
        $query .= "     FINSCHOOL_TYPE, ";
        $query .= "     FINSCHOOL_DISTCD, ";
        $query .= "     FINSCHOOL_DISTCD2, ";
        $query .= "     FINSCHOOL_DIV, ";
        $query .= "     FINSCHOOL_NAME, ";
        $query .= "     FINSCHOOL_KANA, ";
        $query .= "     FINSCHOOL_NAME_ABBV, ";
        $query .= "     FINSCHOOL_KANA_ABBV, ";
        $query .= "     PRINCNAME, ";
        $query .= "     PRINCNAME_SHOW, ";
        $query .= "     PRINCKANA, ";
        $query .= "     DISTRICTCD, ";
        $query .= "     FINSCHOOL_ZIPCD, ";
        $query .= "     FINSCHOOL_ADDR1, ";
        $query .= "     FINSCHOOL_ADDR2, ";
        $query .= "     FINSCHOOL_TELNO, ";
        $query .= "     FINSCHOOL_FAXNO, ";
        $query .= "     EDBOARDCD, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     FINHIGHSCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     FINSCHOOLCD ";

        return $query;
    }

    //年度データの確認
    public function isExisting($db, $finschoolcd)
    {
        $query = "SELECT * FROM FINSCHOOL_YDAT WHERE FINSCHOOLCD = '".$finschoolcd."'";
        $row   = $db->getRow($query);

        if (is_array($row)) {
            return true;
        } else {
            return false;
        }
    }

    //CSV取り込み時の入力チェック、名称マスターに登録されているコードなのか。
    public function checkNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' AND ";
        $query .= "     NAMECD2 = '{$namecd2}' ";

        return $query;
    }

    //CSV取り込み時の入力チェック、教育委員会マスターに登録されているコードなのか。
    public function checkEdboardcd($edboardcd)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     EDBOARD_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARDCD = '{$edboardcd}' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //--- INSERT
    public function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["FINSCHOOLCD"][TEXT]         = $fields["FINSCHOOLCD"];
        $data["FINSCHOOL_TYPE"][TEXT]      = $fields["FINSCHOOL_TYPE"];
        $data["FINSCHOOL_DISTCD"][TEXT]    = $fields["FINSCHOOL_DISTCD"];
        $data["FINSCHOOL_DISTCD2"][TEXT]   = $fields["FINSCHOOL_DISTCD2"];
        $data["FINSCHOOL_DIV"][TEXT]       = $fields["FINSCHOOL_DIV"];
        $data["FINSCHOOL_NAME"][TEXT]      = $fields["FINSCHOOL_NAME"];
        $data["FINSCHOOL_KANA"][TEXT]      = $fields["FINSCHOOL_KANA"];
        $data["FINSCHOOL_NAME_ABBV"][TEXT] = $fields["FINSCHOOL_NAME_ABBV"];
        $data["FINSCHOOL_KANA_ABBV"][TEXT] = $fields["FINSCHOOL_KANA_ABBV"];
        $data["PRINCNAME"][TEXT]           = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]      = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]           = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]          = $fields["DISTRICTCD"];
        $data["FINSCHOOL_ZIPCD"][TEXT]     = $fields["FINSCHOOL_ZIPCD"];
        $data["FINSCHOOL_ADDR1"][TEXT]     = $fields["FINSCHOOL_ADDR1"];
        $data["FINSCHOOL_ADDR2"][TEXT]     = $fields["FINSCHOOL_ADDR2"];
        $data["FINSCHOOL_TELNO"][TEXT]     = $fields["FINSCHOOL_TELNO"];
        $data["FINSCHOOL_FAXNO"][TEXT]     = $fields["FINSCHOOL_FAXNO"];
        $data["EDBOARDCD"][TEXT]           = $fields["EDBOARDCD"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "SYSDATE()";

        $query = Query::insertSQL($data, "FINHIGHSCHOOL_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    public function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["FINSCHOOLCD"][TEXT]         = $fields["FINSCHOOLCD"];
        $data["FINSCHOOL_TYPE"][TEXT]      = $fields["FINSCHOOL_TYPE"];
        $data["FINSCHOOL_DISTCD"][TEXT]    = $fields["FINSCHOOL_DISTCD"];
        $data["FINSCHOOL_DISTCD2"][TEXT]   = $fields["FINSCHOOL_DISTCD2"];
        $data["FINSCHOOL_DIV"][TEXT]       = $fields["FINSCHOOL_DIV"];
        $data["FINSCHOOL_NAME"][TEXT]      = $fields["FINSCHOOL_NAME"];
        $data["FINSCHOOL_KANA"][TEXT]      = $fields["FINSCHOOL_KANA"];
        $data["FINSCHOOL_NAME_ABBV"][TEXT] = $fields["FINSCHOOL_NAME_ABBV"];
        $data["FINSCHOOL_KANA_ABBV"][TEXT] = $fields["FINSCHOOL_KANA_ABBV"];
        $data["PRINCNAME"][TEXT]           = $fields["PRINCNAME"];
        $data["PRINCNAME_SHOW"][TEXT]      = $fields["PRINCNAME_SHOW"];
        $data["PRINCKANA"][TEXT]           = $fields["PRINCKANA"];
        $data["DISTRICTCD"][TEXT]          = $fields["DISTRICTCD"];
        $data["FINSCHOOL_ZIPCD"][TEXT]     = $fields["FINSCHOOL_ZIPCD"];
        $data["FINSCHOOL_ADDR1"][TEXT]     = $fields["FINSCHOOL_ADDR1"];
        $data["FINSCHOOL_ADDR2"][TEXT]     = $fields["FINSCHOOL_ADDR2"];
        $data["FINSCHOOL_TELNO"][TEXT]     = $fields["FINSCHOOL_TELNO"];
        $data["FINSCHOOL_FAXNO"][TEXT]     = $fields["FINSCHOOL_FAXNO"];
        $data["EDBOARDCD"][TEXT]           = $fields["EDBOARDCD"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][FUNC]             = "SYSDATE()";

        $where = "WHERE FINSCHOOLCD = '".$fields["FINSCHOOLCD"]."'";
        $query = Query::updateSQL($data, "FINHIGHSCHOOL_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    public function &getDeleteQuery($db, $fields)
    {
        $query  = " DELETE FROM FINHIGHSCHOOL_MST ";
        $query .= " WHERE FINSCHOOLCD  = '".$fields["FINSCHOOLCD"]."' ";

        $db->query($query);
        return true;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $data = array();
        $db->autoCommit(false);

        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["FINSCHOOLCD"][TEXT]         = $data_arr[$i]["FINSCHOOLCD"];
            $data["FINSCHOOL_TYPE"][TEXT]      = $data_arr[$i]["FINSCHOOL_TYPE"];
            $data["FINSCHOOL_DISTCD"][TEXT]    = $data_arr[$i]["FINSCHOOL_DISTCD"];
            $data["FINSCHOOL_DISTCD2"][TEXT]   = $data_arr[$i]["FINSCHOOL_DISTCD2"];
            $data["FINSCHOOL_DIV"][TEXT]       = $data_arr[$i]["FINSCHOOL_DIV"];
            $data["FINSCHOOL_NAME"][TEXT]      = $data_arr[$i]["FINSCHOOL_NAME"];
            $data["FINSCHOOL_KANA"][TEXT]      = $data_arr[$i]["FINSCHOOL_KANA"];
            $data["FINSCHOOL_NAME_ABBV"][TEXT] = $data_arr[$i]["FINSCHOOL_NAME_ABBV"];
            $data["FINSCHOOL_KANA_ABBV"][TEXT] = $data_arr[$i]["FINSCHOOL_KANA_ABBV"];
            $data["PRINCNAME"][TEXT]           = $data_arr[$i]["PRINCNAME"];
            $data["PRINCNAME_SHOW"][TEXT]      = $data_arr[$i]["PRINCNAME_SHOW"];
            $data["PRINCKANA"][TEXT]           = $data_arr[$i]["PRINCKANA"];
            $data["DISTRICTCD"][TEXT]          = $data_arr[$i]["DISTRICTCD"];
            $data["FINSCHOOL_ZIPCD"][TEXT]     = $data_arr[$i]["FINSCHOOL_ZIPCD"];
            $data["FINSCHOOL_ADDR1"][TEXT]     = $data_arr[$i]["FINSCHOOL_ADDR1"];
            $data["FINSCHOOL_ADDR2"][TEXT]     = $data_arr[$i]["FINSCHOOL_ADDR2"];
            $data["FINSCHOOL_TELNO"][TEXT]     = $data_arr[$i]["FINSCHOOL_TELNO"];
            $data["FINSCHOOL_FAXNO"][TEXT]     = $data_arr[$i]["FINSCHOOL_FAXNO"];
            $data["EDBOARDCD"][TEXT]           = $data_arr[$i]["EDBOARDCD"];
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM FINHIGHSCHOOL_MST WHERE FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}'")) {
                $query = Query::insertSQL($data, "FINHIGHSCHOOL_MST");
            } else {
                $where  = "WHERE ";
                $where .= "     FINSCHOOLCD = '{$data_arr[$i]["FINSCHOOLCD"]}' ";

                $query = Query::updateSQL($data, "FINHIGHSCHOOL_MST", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();

        return $cnt;
    }
}
