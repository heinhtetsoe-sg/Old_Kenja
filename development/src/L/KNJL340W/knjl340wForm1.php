<?php

require_once('for_php7.php');

class knjl340wForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl340wForm1", "POST", "knjl340windex.php", "", "knjl340wForm1");

        //権限チェック
        $adminFlg = knjl340wQuery::getAdminFlg();
        if (AUTHORITY != DEF_UPDATABLE || $adminFlg != "1") {
            $arg["jscript"] = "OnAuthError();";
        } else {
            $arg["jscript"] = "collHttps('".REQUESTROOT."', 'KNJL340W')";
        }

        knjCreateHidden($objForm, "cmd");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl340wForm1.html", $arg);
    }
}
?>
