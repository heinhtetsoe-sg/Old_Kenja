<?php

require_once('for_php7.php');

class knje041Form1 {
    function main(&$model) {
        //権限チェック
        if(AUTHORITY != DEF_UPDATABLE) {
            $arg["close"] = "closing_window();";
        }

        $objForm = new form;

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje041Form1.html", $arg);
    }
}
?>
