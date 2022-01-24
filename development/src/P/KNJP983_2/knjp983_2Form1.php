<?php

require_once('for_php7.php');

class knjp983_2Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjp983_2index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //リスト表示
        $query = knjp983_2Query::getList();
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             //レコードを連想配列のまま配列$arg["data"]に追加していく。
             array_walk($row, "htmlspecialchars_array");
             
             $row["BANKCD_NAME"] = $db->getOne(knjp983_2Query::getBankCd($row["BANKCD"]));
             $row["BRANCHCD_NAME"] = $db->getOne(knjp983_2Query::getBankCd($row["BANKCD"]));
             $row["BANK_DEPOSIT_ITEM_NAME"] = $db->getOne(knjp983_2Query::getNameMst("G203", "GET"));
             $row["PAY_DIV_NAME"] = $db->getOne(knjp983_2Query::getNameMst("G217", "GET"));

             $arg["data"][] = $row; 
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp983_2Form1.html", $arg); 
    }
}
?>
