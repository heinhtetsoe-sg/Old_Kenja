<?php
class knje372mForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //権限チェック
        authCheck($arg);

        //ＤＢ接続
        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = CTRL_YEAR ."年度";

        //登録日
        $model->field["TOROKU_DATE"] = ($model->field["TOROKU_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $model->field["TOROKU_DATE"]);
        $arg["data"]["TOROKU_DATE"] = View::popUpCalendar($objForm, "TOROKU_DATE", $model->field["TOROKU_DATE"]);

        //ボタン作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "STAFF_AUTH", AUTHORITY);
        knjCreateHidden($objForm, "PASS_AUTH", DEF_UPDATABLE);

        //ＤＢ切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knje372mindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje372mForm1.html", $arg);
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg) {
    //実行
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('execute');\"");
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}