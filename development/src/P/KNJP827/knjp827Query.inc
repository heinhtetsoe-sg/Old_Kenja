<?php

require_once('for_php7.php');

class knjp827query extends Query {
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

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //カウント
    function getCntSql($schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_WEB_DEPOSIT_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //かな取得
    function getAccName($schregno) {
        $query  = " SELECT ";
        $query .= "     case ";
        $query .= "         when GUARANTOR_KANA is not null then GUARANTOR_KANA ";
        $query .= "         else GUARD_KANA ";
        $query .= "     end kana ";
        $query .= " FROM ";
        $query .= "     GUARDIAN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //データの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        $seq = 1;   //通番
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //カウント
            $chkCnt = $db->getOne(knjp827Query::getCntSql($data_arr[$i]["SCHREGNO"]));

            //アカウント名取得
            $accName        = $db->getOne(knjp827Query::getAccName($data_arr[$i]["SCHREGNO"]));
            $setAccountName = str_replace(array(' ', '　'), '', mb_convert_kana($accName, "h"));

            //COLLECT_SGL_WEB_DEPOSIT_DAT
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEQ"][NUMBER]            = $seq;
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["ACCOUNTNAME"][TEXT]      = $setAccountName;
            $data["ENT_MONEY"][TEXT]        = $data_arr[$i]["ENT_MONEY"];
            $data["RESERVE_MONEY"][TEXT]    = $data_arr[$i]["RESERVE_MONEY"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $chkCnt) {
                $query = Query::insertSQL($data, "COLLECT_SGL_WEB_DEPOSIT_DAT");
            } else {
                $where  = " WHERE YEAR          = '".CTRL_YEAR."' ";
                $where .= "   AND SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "COLLECT_SGL_WEB_DEPOSIT_DAT", $where);
            }
            $db->query($query);

            $seq++;
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
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }
}
?>
