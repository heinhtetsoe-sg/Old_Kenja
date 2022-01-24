<?php

require_once('for_php7.php');

class knja353Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knja353index.php", "", "main");

        $arg['YEAR'] = $model->year;

        //ボタン作成
        makeButton($objForm, $arg, $db, $model);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja353Form1.html", $arg);
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
