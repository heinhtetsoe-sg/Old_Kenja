<?php

require_once('for_php7.php');

class knjp983_2Query extends Query {

    //リスト取得
    function getList() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     TRADER_MST ";
        $query .= " ORDER BY  ";
        $query .= "     TRADER_CD ";

        return $query;
    }

    //１レコード取得
    function getRow($trader_cd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     TRADER_MST ";
        $query .= " WHERE ";
        $query .= "     TRADER_CD = '".$trader_cd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //年度データの確認
    function IsExisting($trader_cd) {
        $db    = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     TRADER_YDAT ";
        $query .= " WHERE ";
        $query .= "     TRADER_CD = '".$trader_cd."' ";

        $row = $db->getRow($query);
        Query::dbCheckIn($db);

        if (is_array($row)) {
            return true;
        } else {
            return false;
        }
    }

    //銀行コード
    function getBankCd($bankcd="") {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        if ($bankcd) {
            $query .= "     T1.BANKNAME ";
        } else {
            $query .= "     T1.BANKCD AS VALUE, ";
            $query .= "     T1.BANKCD || ':' || T1.BANKNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     BANK_MST T1 ";
        if ($bankcd) {
            $query .= " WHERE ";
            $query .= "     T1.BANKCD = '{$bankcd}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //支店コード
    function getBranchCd($bankcd, $branchcd="") {
        $query  = "";
        $query .= " SELECT ";
        if ($branchcd) {
            $query .= "     T1.BRANCHNAME ";
        } else {
            $query .= "     T1.BRANCHCD AS VALUE, ";
            $query .= "     T1.BRANCHCD || ':' || T1.BRANCHNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     BANK_MST T1 ";
        $query .= " WHERE ";
        $query .= "     BANKCD = '{$bankcd}' ";
        if ($branchcd) {
            $query .= " AND BRANCHCD = '{$branchcd}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //口座種別、支払い方法
    function getNameMst($namecd, $flg="") {
        $query  = "";
        $query .= " SELECT ";
        if ($flg) {
            $query .= "     T1.NAME1 ";
        } else {
            $query .= "     T1.NAMECD2 AS VALUE, ";
            $query .= "     T1.NAMECD2 || ':' || T1.NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd}' ";
        if (!$flg) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //UPDATE 
    function &getUpdateQuery($model, $div) {
        $db = Query::dbCheckOut();

        //小文字カナの大文字カナ、空白変換リスト
        $replace_of = array('ｧ','ｨ','ｩ','ｪ','ｫ','ｯ','ｬ','ｭ','ｮ','ヵ','ヶ','　');
        $replace_by = array('ｱ','ｲ','ｳ','ｴ','ｵ','ﾂ','ﾔ','ﾕ','ﾖ','ｶ','ｹ',' ');

        $data = array();
        if ($div == "add") {
            $data["TRADER_CD"][TEXT]    = $model->field["TRADER_CD"];
        }
        $data["TRADER_NAME"][TEXT]      = $model->field["TRADER_NAME"];
        $data["ZIPCD"][TEXT]            = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]            = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]            = $model->field["ADDR2"];
        $data["BANKCD"][TEXT]           = $model->field["BANKCD"];
        $data["BRANCHCD"][TEXT]         = $model->field["BRANCHCD"];
        $data["BANK_DEPOSIT_ITEM"][TEXT]= $model->field["BANK_DEPOSIT_ITEM"];
        $data["BANK_ACCOUNTNO"][TEXT]   = $model->field["BANK_ACCOUNTNO"];
        $data["ACCOUNTNAME"][TEXT]      = $model->field["ACCOUNTNAME"];
        $data["ACCOUNTNAME_KANA"][TEXT] = str_replace($replace_of, $replace_by, mb_convert_kana($model->field["ACCOUNTNAME_KANA"], "akh"));
        $data["PAY_DIV"][TEXT]          = $model->field["PAY_DIV"];
        
        
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($div == "add") {
            $query = Query::insertSQL($data, "TRADER_MST");
        } else {
            $where = " WHERE TRADER_CD = '".$model->field["TRADER_CD"]."' ";
            $query = Query::updateSQL($data, "TRADER_MST", $where);
        }

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     TRADER_MST ";
        $query .= " WHERE ";
        $query .= "     TRADER_CD = '".$model->field["TRADER_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
