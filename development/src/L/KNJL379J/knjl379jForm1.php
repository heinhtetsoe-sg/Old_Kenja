<?php

require_once('for_php7.php');

class knjl379jForm1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl379jForm1", "POST", "knjl379jindex.php", "", "knjl379jForm1");

        //DB接続
        $db = Query::dbCheckOut();

        $opt=array();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //ボタンを作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl379jForm1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //csvボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "ＣＳＶ出力", "onclick=\"return btn_submit('csv');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $db, $model)
{
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("cmd"));
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
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
