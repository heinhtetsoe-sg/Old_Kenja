<?php

require_once('for_php7.php');

class knjc032dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc032dForm1", "POST", "knjc032dindex.php", "", "knjc032dForm1");

        $arg["jscript"] = "collHttps('".REQUESTROOT."', 'KNJC032D')";

        knjCreateHidden($objForm, "cmd");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc032dForm1.html", $arg);
    }
}
?>
