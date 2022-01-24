<?php

require_once('for_php7.php');

class knjl675iForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl675iindex.php", "", "edit");

        //自身を呼び出す
        if (!$model->cmd) {
            $arg["jscript"] = "collHttps()";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["ENTEXAMYEAR"] = $model->year;

        //hidden作成
        makeHidden($objForm, $model);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl675iForm1.html", $arg);
    }
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "PRGID", "KNJL675I");
    knjCreateHidden($objForm, "cmd");
}
