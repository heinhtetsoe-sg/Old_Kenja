<?php

require_once('for_php7.php');

class knjj195Form1 {

    function main(&$model) {

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('". SERVLET_URL ."');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //CSV出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "LOGIN_SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "LOGIN_SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "PRGID", "PROGRAMID");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjj195index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj195Form1.html", $arg);
    }
}
?>
