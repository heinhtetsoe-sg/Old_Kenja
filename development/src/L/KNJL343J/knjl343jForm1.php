<?php

require_once('for_php7.php');


class knjl343jForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl343jForm1", "POST", "knjl343jindex.php", "", "knjl343jForm1");

    $opt=array();

    $arg["TOP"]["YEAR"] = $model->ObjYear;

	//通知日付
	$value = isset($model->field["DATE"])?$model->field["DATE"]:str_replace("-","/",$model->control["学籍処理日"]);
	$arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $value);

    //印刷範囲ラジオボタン 1:手続き済全員 2:受験者指定
    $opt_print = array(1, 2);
    $model->field["PRINT_RANGE"] = ($model->field["PRINT_RANGE"] == "") ? "1" : $model->field["PRINT_RANGE"];
    $extra = "onclick=\"return btn_submit('knjl343j');\"";
    createRadio($objForm, $arg, "PRINT_RANGE", $model->field["PRINT_RANGE"], $extra, $opt_print, get_count($opt_print));

    //受験番号
    $disp = ($model->field["PRINT_RANGE"] == 2) ? "" : "disabled";
    $extra = "$disp onblur=\"this.value=toInteger(this.value)\"";
    $value = $model->field["EXAMNO"] ? $model->field["EXAMNO"] : "";
    $arg["data"]["EXAMNO"] = createTextBox($objForm, $value, "EXAMNO", 5, 5, $extra);

    //ボタン作成
    makeBtn($objForm, $arg, $model);

    //hiddenを作成する
    makeHidden($objForm, $model);

    $arg["finish"]  = $objForm->get_finish();

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl343jForm1.html", $arg); 
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
    $objForm->ae(createHiddenAe("PRGID", "KNJL343J"));
    $objForm->ae(createHiddenAe("DOCUMENTROOT", DOCUMENTROOT));
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("PRINT", $model->field["PRINT_RANGE"]));    //判定用
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    $objForm->ae( array("type"      => "radio",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));
    for ($i = 1; $i <= $count; $i++) {
        $arg["data"][$name.$i] = $objForm->ge($name, $i);
    }
}

//テキスト作成
function createTextBox(&$objForm, $data, $name, $size, $maxlen, $extra)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $data,
                        "extrahtml" => $extra) );
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
