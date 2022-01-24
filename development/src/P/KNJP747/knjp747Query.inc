<?php

require_once('for_php7.php');

class knjp747Query extends Query {
    /**********/
    /* 左画面 */
    /**********/
    //データ取得
    function getList($model, $schoolKind='') {
        $query  = " SELECT ";
        if ($schoolKind != '') {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     CASE WHEN MAIN.SCHOOL_KIND = '99' THEN '共通' ELSE A023.ABBV1 END AS SCHOOL_KIND_NAME, ";
            $query .= "     MAIN.* ";
        }
        $query .= " FROM ";
        $query .= "     COLLECT_SCHOOL_BANK_MST MAIN ";
        $query .= "     LEFT JOIN V_NAME_MST A023 ON A023.YEAR    = MAIN.YEAR";
        $query .= "                              AND A023.NAMECD1 = 'A023' ";
        $query .= "                              AND A023.NAME1   = MAIN.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "         MAIN.SCHOOLCD = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND MAIN.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND MAIN.SEQ      = '{$model->seq}' ";
        if ($schoolKind != '') {
            $query .= "     AND MAIN.SCHOOL_KIND = '$schoolKind' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     MAIN.SCHOOL_KIND, ";
            $query .= "     MAIN.FORMAT_DIV ";
        }

        return $query;
    }

    /**********/
    /* 右画面 */
    /**********/
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $schoolKind, $formatDiv) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLECT_SCHOOL_BANK_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND SCHOOL_KIND = '".$schoolKind."'";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND FORMAT_DIV  = '".$formatDiv."'";
        $query .= "     AND SEQ         = '{$model->seq}' ";

        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $Row;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHOOLCD"][TEXT]             = sprintf("%012d", SCHOOLCD);
        $data["SCHOOL_KIND"][TEXT]          = $fields["SCHOOL_KIND"];
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["FORMAT_DIV"][TEXT]           = $fields["FORMAT_DIV"];
        $data["SEQ"][TEXT]                  = $model->seq;
        $data["SHUBETSU_CD"][TEXT]          = $fields["SHUBETSU_CD"];
        $data["ACCOUNT_CD"][TEXT]           = $fields["ACCOUNT_CD"];
        $data["ACCOUNTNAME_KANA"][TEXT]     = $fields["ACCOUNTNAME_KANA"];
        $data["BANK_CD"][TEXT]              = $fields["BANK_CD"];
        $data["BANKNAME_KANA"][TEXT]        = $fields["BANKNAME_KANA"];
        $data["BRANCHCD"][TEXT]             = $fields["BRANCHCD"];
        $data["ACCOUNTNO"][TEXT]            = $fields["ACCOUNTNO"];
        $data["BANK_TRANSFER_FEE"][NUMBER]  = $fields["BANK_TRANSFER_FEE"];
        $data["JC_CD"][TEXT]                = $fields["JC_CD"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "COLLECT_SCHOOL_BANK_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SHUBETSU_CD"][TEXT]          = $fields["SHUBETSU_CD"];
        $data["ACCOUNT_CD"][TEXT]           = $fields["ACCOUNT_CD"];
        $data["ACCOUNTNAME_KANA"][TEXT]     = $fields["ACCOUNTNAME_KANA"];
        $data["BANK_CD"][TEXT]              = $fields["BANK_CD"];
        $data["BANKNAME_KANA"][TEXT]        = $fields["BANKNAME_KANA"];
        $data["BRANCHCD"][TEXT]             = $fields["BRANCHCD"];
        $data["ACCOUNTNO"][TEXT]            = $fields["ACCOUNTNO"];
        $data["BANK_TRANSFER_FEE"][NUMBER]  = $fields["BANK_TRANSFER_FEE"];
        $data["JC_CD"][TEXT]                = $fields["JC_CD"];

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ";
        $where .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $where .= "     AND SCHOOL_KIND = '".$fields["SCHOOL_KIND"]."' ";
        $where .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $where .= "     AND FORMAT_DIV  = '".$fields["FORMAT_DIV"]."' ";
        $where .= "     AND SEQ         = '".$model->seq."' ";

        $query = Query::updateSQL($data, "COLLECT_SCHOOL_BANK_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM COLLECT_SCHOOL_BANK_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "     AND SCHOOL_KIND = '".$fields["SCHOOL_KIND"]."' ";
        $query .= "     AND YEAR        = '".CTRL_YEAR."' ";
        $query .= "     AND FORMAT_DIV  = '".$fields["FORMAT_DIV"]."' ";
        $query .= "     AND SEQ         = '".$model->seq."' ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return ;
    }

/**** 前年度コピー処理 ****/
    //今年度のデータが存在するかチェック
    function CheckData($model, $year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM COLLECT_SCHOOL_BANK_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."'";
        $query .= "     AND YEAR        = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO COLLECT_SCHOOL_BANK_MST ";
        $query .= "   (SELECT ";
        $query .= "         SCHOOLCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         FORMAT_DIV, ";
        $query .= "         SEQ, ";
        $query .= "         SHUBETSU_CD, ";
        $query .= "         CODE_DIV, ";
        $query .= "         ACCOUNT_CD, ";
        $query .= "         ACCOUNTNAME_KANA, ";
        $query .= "         BANK_CD, ";
        $query .= "         BANKNAME_KANA, ";
        $query .= "         BRANCHCD, ";
        $query .= "         BRANCHNAME_KANA, ";
        $query .= "         DEPOSIT_TYPE, ";
        $query .= "         ACCOUNTNO, ";
        $query .= "         SCHOOLZIPCD, ";
        $query .= "         SCHOOLADDR1, ";
        $query .= "         SCHOOLADDR2, ";
        $query .= "         SCHOOLTELNO, ";
        $query .= "         SCHOOLFAXNO, ";
        $query .= "         BANK_TRANSFER_FEE, ";
        $query .= "         JC_CD, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         COLLECT_SCHOOL_BANK_MST ";
        $query .= "     WHERE ";
        $query .= "             SCHOOLCD    = '".(sprintf("%012d", SCHOOLCD))."' ";
        $query .= "         AND YEAR        = '".(CTRL_YEAR - 1)."' ";
        $query .= "     ) ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
