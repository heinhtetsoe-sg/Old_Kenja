<?php

require_once('for_php7.php');

class knjz286_2aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz286_2aindex.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //リスト表示
        $query = knjz286_2aQuery::getList();
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             //レコードを連想配列のまま配列$arg["data"]に追加していく。
             array_walk($row, "htmlspecialchars_array");
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
        View::toHTML($model, "knjz286_2aForm1.html", $arg); 
    }
}
?>
