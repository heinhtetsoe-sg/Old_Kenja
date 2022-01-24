<?php

require_once('for_php7.php');

class knjl426Form1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl426index.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $arg["data"]["LAST_YEAR"]   = CTRL_YEAR;
        $arg["data"]["YEAR"]        = CTRL_YEAR + 1;

        //DB接続
        $db = Query::dbCheckOut();

        //実行
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["btn_exe"] = knjCreateBtn($objForm, "btn_exe", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL426");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl426Form1.html", $arg);
    }
}
