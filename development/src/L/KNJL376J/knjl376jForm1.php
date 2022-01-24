<?php

require_once('for_php7.php');


class knjl376jForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl376jForm1", "POST", "knjl376jindex.php", "", "knjl376jForm1");

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    $arg["data"]["PRINT"] = createCheckBox($objForm, "PRINT", "1", "checked");

    //ボタン作成
    makeBtn($objForm, $arg, $model);

    //hiddenを作成する
    makeHidden($objForm, $model);

    $arg["finish"]  = $objForm->get_finish();

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl376jForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //印刷ボタンを作成する
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタンを作成する
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("YEAR", $model->ObjYear));
    $objForm->ae(createHiddenAe("SEMESTER", CTRL_SEMESTER));
    $objForm->ae(createHiddenAe("LOGIN_DATE", CTRL_DATE));
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("PRGID", "KNJL376J"));
    $objForm->ae(createHiddenAe("cmd"));
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi = "")
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
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

?>
