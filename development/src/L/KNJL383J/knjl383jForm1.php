<?php

require_once('for_php7.php');


class knjl383jForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl383jindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度学期表示
        $arg["YEAR"] = $model->examyear;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        $objForm->ae(createHiddenAe("cmd"));

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl383jForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行
    $arg["btn_exec"] = createBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('exec');\"");
    //終了
    $arg["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae(array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}
