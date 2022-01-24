<?php

require_once('for_php7.php');

class knji100cForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knji100cForm1", "POST", "knji100cindex.php", "", "knji100cForm1");

        $arg["jscript"] = "Page_jumper('".REQUESTROOT."', '{$model->selectSchoolKind}')";

        knjCreateHidden($objForm, "cmd");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knji100cForm1.html", $arg);
    }
}
?>
