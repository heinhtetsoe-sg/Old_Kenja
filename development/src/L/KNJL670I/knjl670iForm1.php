<?php

require_once('for_php7.php');

class knjl670iForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl670iindex.php", "", "edit");

        //自身を呼び出す
        if (!$model->cmd) {
            // $arg["jscript"] = "collHttps('".REQUESTROOT."', 'https')";
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
        View::toHTML($model, "knjl670iForm1.html", $arg);
    }
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "PRGID", "KNJL670I");
    knjCreateHidden($objForm, "cmd");
}
