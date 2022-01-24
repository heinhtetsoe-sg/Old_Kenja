<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja127pSubForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knja127pindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //対象データ取得
        if ($model->cmd === 'subform1') {
            $Row = $db->getRow(knja127pQuery::getChallengedProfile($model), DB_FETCHMODE_ASSOC);
        }
        
        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //障害名・診断名
        $extra = "aria-label=\"障害名・診断名\"";
        $arg["data"]["CHALLENGED_NAMES"] = KnjCreateTextArea($objForm, "CHALLENGED_NAMES", 6, 41, "soft", $extra, $Row["CHALLENGED_NAMES"]);
        
        //障害の状態・特性
        $extra = "aria-label=\"障害の状態・特性\"";
        $arg["data"]["CHALLENGED_STATUS"] = KnjCreateTextArea($objForm, "CHALLENGED_STATUS", 8, 41, "soft", $extra, $Row["CHALLENGED_STATUS"]);

        //戻るボタン
        $extra = "onclick=\"parent.current_cursor_focus(); return parent.closeit()\" aria-label=\"戻る\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja127pSubForm1.html", $arg);
    }
}
?>
