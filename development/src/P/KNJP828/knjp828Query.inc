<?php

require_once('for_php7.php');

class knjp828query extends Query {
    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //カウント
    function getCntSql($schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_WEB_OUTPUT_CSV_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //COLLECT_SGL_WEB_OUTPUT_CSV_DAT
        $cnt = 0;
        $query = knjp828Query::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            //カウント
            $chkCnt = $db->getOne(knjp828Query::getCntSql($row["SCHREGNO"]));

            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SEQ"][NUMBER]                = $row['SEQ'];
            $data["ACCOUNTNAME"][TEXT]          = $row['ACCOUNTNAME'];
            $data["ACCOUNTNAME_KANJI"][TEXT]    = ''; //null
            $data["SCHREGNO"][TEXT]             = $row['SCHREGNO'];
            $data["PLAN_MONEY"][TEXT]           = $row['PLAN_MONEY'];
            $data["TOTAL_MONEY"][TEXT]          = $row['TOTAL_MONEY'];
            $data["BANKCD"][TEXT]               = ''; //null
            $data["BANKNAME"][TEXT]             = ''; //null
            $data["BANKNAME_KANJI"][TEXT]       = ''; //null
            $data["BRANCHCD"][TEXT]             = ''; //null
            $data["BRANCHNAME"][TEXT]           = ''; //null
            $data["BRANCHNAME_KANJI"][TEXT]     = ''; //null
            $data["DEPOSIT_DIV"][TEXT]          = ''; //null
            $data["ACCOUNTNO"][TEXT]            = ''; //null
            $data["RESULT_CD"][TEXT]            = $row['RESULT_CD'];
            $data["SUMMARY"][TEXT]              = ''; //null
            $data["TOROKUNO"][TEXT]             = ''; //null
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            if (1 > $chkCnt) {
                $query = Query::insertSQL($data, "COLLECT_SGL_WEB_OUTPUT_CSV_DAT");
            } else {
                $where  = " WHERE YEAR          = '".CTRL_YEAR."' ";
                $where .= "   AND SCHREGNO      = '".$row["SCHREGNO"]."' ";

                $query = Query::updateSQL($data, "COLLECT_SGL_WEB_OUTPUT_CSV_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //登録データ取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     ACCOUNTNAME, ";
        $query .= "     SCHREGNO, ";
        $query .= "     int(ENT_MONEY) + int(RESERVE_MONEY) as PLAN_MONEY, ";
        $query .= "     int(ENT_MONEY) + int(RESERVE_MONEY) as TOTAL_MONEY, ";
        $query .= "     '振替済' as RESULT_CD ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_WEB_DEPOSIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     ACCOUNTNAME, ";
        $query .= "     ACCOUNTNAME_KANJI, ";
        $query .= "     SCHREGNO, ";
        $query .= "     PLAN_MONEY, ";
        $query .= "     TOTAL_MONEY, ";
        $query .= "     BANKCD, ";
        $query .= "     BANKNAME, ";
        $query .= "     BANKNAME_KANJI, ";
        $query .= "     BRANCHCD, ";
        $query .= "     BRANCHNAME, ";
        $query .= "     BRANCHNAME_KANJI, ";
        $query .= "     DEPOSIT_DIV, ";
        $query .= "     ACCOUNTNO, ";
        $query .= "     RESULT_CD, ";
        $query .= "     SUMMARY, ";
        $query .= "     TOROKUNO ";
        $query .= " FROM ";
        $query .= "     COLLECT_SGL_WEB_OUTPUT_CSV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

}
?>
