<?php

require_once('for_php7.php');

class knjh180Form1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //ファイルからの取り込み
        $extra = "style=\"width:300px\"";
        $arg["csvfile"] = knjCreateFile($objForm, "csvfile", $extra, 2048000);

        //CSV取込みボタンを作成する
        $extra = "{$disabled} onclick=\"return btn_submit('execute');\"";
        $arg["data"]["btn_execute"] = knjCreateBtn($objForm, 'btn_execute', '実 行', $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["data"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd", "");

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjh180index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh180Form1.html", $arg);
    }
}
?>
