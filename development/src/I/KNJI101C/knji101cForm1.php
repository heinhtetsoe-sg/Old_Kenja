<?php

require_once('for_php7.php');

class knji101cForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knji101cForm1", "POST", "knji101cindex.php", "", "knji101cForm1");

        //画面移動
        $arg["reload"] = "parent.location.href='../KNJI100C_01/knji100c_01index.php?cmd=edit&SEND_PRGID=".PROGRAMID."&SEND_AUTH=".AUTHORITY."';";

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knji101cForm1.html", $arg); 
    }
}
?>
