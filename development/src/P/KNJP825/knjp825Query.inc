<?php

require_once('for_php7.php');

class knjp825query extends Query {
    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //学籍番号
    function schregNoCntSql($schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //ヘッダー・レコードチェック
    function getRowCnt($directDebit) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_ZENGIN_HEADER_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND DIRECT_DEBIT = '{$directDebit}' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //ZENGIN関係削除
    function delZenginDat($table, $directDebit) {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND DIRECT_DEBIT = '{$directDebit}' ";

        return $query;
    }

    //データの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr) {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $dataRecord    = true;
        $dataRecordSeq = 1;

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //ヘッダー・レコード
            if ($data_arr[$i]["DATA_DIV"] == '1') {
                //COLLECT_ZENGIN_HEADER_RECORD_DAT
                $query = knjp825Query::delZenginDat("COLLECT_ZENGIN_HEADER_RECORD_DAT",  $data_arr[$i]["DIRECT_DEBIT"]);
                $db->query($query);

                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["DATA_DIV"][TEXT]     = $data_arr[$i]["DATA_DIV"];
                $data["TYPE_CD"][TEXT]      = $data_arr[$i]["TYPE_CD"];
                $data["CD_DIV"][TEXT]       = $data_arr[$i]["CD_DIV"];
                $data["CLIENT_CD"][TEXT]    = $data_arr[$i]["CLIENT_CD"];
                $data["CLIENT_NAME"][TEXT]  = $data_arr[$i]["CLIENT_NAME"];
                $data["DIRECT_DEBIT"][TEXT] = $data_arr[$i]["DIRECT_DEBIT"];
                $data["T_BANKCD"][TEXT]     = $data_arr[$i]["T_BANKCD"];
                $data["T_BANKNAME"][TEXT]   = $data_arr[$i]["T_BANKNAME"];
                $data["T_BRANCHCD"][TEXT]   = $data_arr[$i]["T_BRANCHCD"];
                $data["T_BRANCHNAME"][TEXT] = $data_arr[$i]["T_BRANCHNAME"];
                $data["DEPOSIT_DIV"][TEXT]  = $data_arr[$i]["DEPOSIT_DIV"];
                $data["T_ACCOUNTNO"][TEXT]  = $data_arr[$i]["T_ACCOUNTNO"];
                $data["DUMMY"][TEXT]        = $data_arr[$i]["DUMMY"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "SYSDATE()";

                $query = knjp825Query::insertSQL($data, "COLLECT_ZENGIN_HEADER_RECORD_DAT");//空白も追加するため自作関数
                $db->query($query);

            //データ・レコード
            } else if ($data_arr[$i]["DATA_DIV"] == '2') {
                //delete
                if ($dataRecord) {
                    $query = knjp825Query::delZenginDat("COLLECT_ZENGIN_DATA_RECORD_DAT",    $data_arr[$i]["DIRECT_DEBIT"]);
                    $db->query($query);
                    $dataRecord = false;
                }

                //insert
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["DIRECT_DEBIT"][TEXT]     = $data_arr[$i]["DIRECT_DEBIT"];
                $data["SEQ"][NUMBER]            = $dataRecordSeq;
                $data["DATA_DIV"][TEXT]         = $data_arr[$i]["DATA_DIV"];
                $data["BANKCD"][TEXT]           = $data_arr[$i]["BANKCD"];
                $data["BANKNAME"][TEXT]         = $data_arr[$i]["BANKNAME"];
                $data["BRANCHCD"][TEXT]         = $data_arr[$i]["BRANCHCD"];
                $data["BRANCHNAME"][TEXT]       = $data_arr[$i]["BRANCHNAME"];
                $data["DUMMY1"][TEXT]           = $data_arr[$i]["DUMMY1"];
                $data["DEPOSIT_DIV"][TEXT]      = $data_arr[$i]["DEPOSIT_DIV"];
                $data["ACCOUNTNO"][TEXT]        = $data_arr[$i]["ACCOUNTNO"];
                $data["ACCOUNTNAME"][TEXT]      = $data_arr[$i]["ACCOUNTNAME"];
                $data["PLAN_MONEY"][TEXT]       = $data_arr[$i]["PLAN_MONEY"];
                $data["NEW_CD"][TEXT]           = $data_arr[$i]["NEW_CD"];
                $data["CUSTOMER_CD"][TEXT]      = $data_arr[$i]["CUSTOMER_CD"];
                $data["RESULT_CD"][TEXT]        = $data_arr[$i]["RESULT_CD"];
                $data["DUMMY2"][TEXT]           = $data_arr[$i]["DUMMY2"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query = knjp825Query::insertSQL($data, "COLLECT_ZENGIN_DATA_RECORD_DAT");
                $db->query($query);

                $dataRecordSeq++;

            //トレーラ・レコード(COLLECT_ZENGIN_TRAILER_RECORD_DAT)
            } else if ($data_arr[$i]["DATA_DIV"] == '8') {
                //delete
                $query = knjp825Query::delZenginDat("COLLECT_ZENGIN_TRAILER_RECORD_DAT",     $data_arr[$i]["DIRECT_DEBIT"]);
                $db->query($query);

                //insert
                $data = array();
                $data["YEAR"][TEXT]                 = CTRL_YEAR;
                $data["DIRECT_DEBIT"][TEXT]         = $data_arr[$i]["DIRECT_DEBIT"];
                $data["DATA_DIV"][TEXT]             = $data_arr[$i]["DATA_DIV"];
                $data["TOTAL_CNT"][TEXT]            = $data_arr[$i]["TOTAL_CNT"];
                $data["TOTAL_MONEY"][TEXT]          = $data_arr[$i]["TOTAL_MONEY"];
                $data["TRANSFER_CNT"][TEXT]         = $data_arr[$i]["TRANSFER_CNT"];
                $data["TRANSFER_MONEY"][TEXT]       = $data_arr[$i]["TRANSFER_MONEY"];
                $data["NOT_TRANSFER_CNT"][TEXT]     = $data_arr[$i]["NOT_TRANSFER_CNT"];
                $data["NOT_TRANSFER_MONEY"][TEXT]   = $data_arr[$i]["NOT_TRANSFER_MONEY"];
                $data["DUMMY"][TEXT]                = $data_arr[$i]["DUMMY"];
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "SYSDATE()";

                $query = knjp825Query::insertSQL($data, "COLLECT_ZENGIN_TRAILER_RECORD_DAT");
                $db->query($query);

            //エンド・レコード(COLLECT_ZENGIN_END_RECORD_DAT)
            } else if ($data_arr[$i]["DATA_DIV"] == '9') {
                //delete
                $query = knjp825Query::delZenginDat("COLLECT_ZENGIN_END_RECORD_DAT",     $data_arr[$i]["DIRECT_DEBIT"]);
                $db->query($query);

                //insert
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["DIRECT_DEBIT"][TEXT] = $data_arr[$i]["DIRECT_DEBIT"];
                $data["DATA_DIV"][TEXT]     = $data_arr[$i]["DATA_DIV"];
                $data["DUMMY"][TEXT]        = $data_arr[$i]["DUMMY"];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "SYSDATE()";

                $query = knjp825Query::insertSQL($data, "COLLECT_ZENGIN_END_RECORD_DAT");
                $db->query($query);
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/
    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //insert文作成
    function insertSQL($arg, $table) {
        $fields = array_keys($arg);
        $sql = "insert into $table(" .implode($fields, ",") .") ";

        $sql .= "values(";
        $sp = "";
        foreach ($fields as $f) {
            $key = key($arg[$f]);
            if ($arg[$f][$key] == '') {
                $sql .= $sp ."NULL";
            } else {
                switch ($key) {
                    case TEXT:
                        $sql .= $sp ."'" .Query::addquote($arg[$f][$key]) ."'";
                        break;
                    case NUMBER:
                        $sql .= $sp .$arg[$f][$key];
                        break;
                    case FUNC:
                        $sql .= $sp .$arg[$f][$key];
                        break;
                    case DATE:
                        $sql .= $sp ."'" .Query::date2sql($arg[$f][$key]) ."'";
                        break;
                    default;
                        $sql .= $sp .$arg[$f][$key];
                        break;
                }
            }
            $sp = ",";
        }
        return $sql .= ")";
    }
}
?>
