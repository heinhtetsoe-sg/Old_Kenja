<?php

require_once('for_php7.php');


class knjl352jForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl352jForm1", "POST", "knjl352jindex.php", "", "knjl352jForm1");

    $opt=array();

    $arg["TOP"]["YEAR"] = $model->ObjYear;

	$db = Query::dbCheckOut();

    //入学区分コンボ
    $query = knjl352jQuery::getTestdiv("L004",$model);
    makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1);

    //印刷指定ラジオボタン 1:得点 2:偏差値
    $opt_output = array(1,2);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
    createRadio($objForm, $arg, "OUTPUT", $model->field["OUTPUT"], "", $opt_output, get_count($opt_output));

	Query::dbCheckIn($db);

    //ボタン作成
    makeBtn($objForm, $arg, $model);

    //hiddenを作成する
    makeHidden($objForm, $model);

    $arg["finish"]  = $objForm->get_finish();

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl352jForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
	$default_flg = false;
	$default     = 0 ;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($name == "TESTDIV") {
            if ($row["NAMESPARE2"] != 1 && !$default_flg){
                $default++;
            } else {
                $default_flg = true;
            }
        }
    }
    $result->free();
    if ($name == "TESTDIV") {
        if($default_flg){
            $opt[] = array("label" => "-- 全て --",  "value" => "0");
        } else {
            $opt[] = array("label" => "　　　",  "value" => "0");
        }
        $value = ($value == "") ? $opt[$default]["value"] : $value;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }


    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
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
    $objForm->ae(createHiddenAe("PRGID", "KNJL352J"));
    $objForm->ae(createHiddenAe("cmd"));
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

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
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
