<?php

require_once('for_php7.php');

class knjl360wForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl360wForm1", "POST", "knjl360windex.php", "", "knjl360wForm1");

        //権限チェック
        $adminFlg = knjl360wQuery::getAdminFlg();
        if (AUTHORITY != DEF_UPDATABLE|| $adminFlg != "1") {
            $arg["jscript"] = "OnAuthError();";
        } else {
            $arg["jscript"] = "collHttps('".REQUESTROOT."', 'KNJL360W')";
        }

        knjCreateHidden($objForm, "cmd");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl360wForm1.html", $arg);
    }
}
?>
