<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd131wSubForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("form3", "POST", "knjd131windex.php", "", "form3");

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        $result = $db->query(knjd131wQuery::getAttendRemark($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"][] = $row;
        }

        /**********/
        /* ボタン */
        /**********/
        //戻る
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd131wSubForm1.html", $arg);
    }
}
?>