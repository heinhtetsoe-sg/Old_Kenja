<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knja041Form1
{
    public function main($model)
    {
        $objForm = new form();

        //DB接続
        $db  = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        //動作条件チェック
        } elseif ($db->getOne(knja041Query::ChecktoStart()) == "0") {
            $arg["check"] = "Show_ErrMsg(1);";
        }
        //最終学期チェック
        $school = $db->getRow(knja041Query::getSchoolMst($model), DB_FETCHMODE_ASSOC);
        if (CTRL_SEMESTER != $school["SEMESTERDIV"]) {
            $arg["max_semes_cl"] = " max_semes_cl(); ";
        }

        //対象年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        $arg["data"]["NEWYEAR"]  = $model->new_year;

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力
        $opt_shubetsu = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"]=="") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        createRadio($objForm, $arg, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));

        //処理名コンボボックス
        $opt_shori   = array();
        $opt_shori[] = array("label" => "更新","value" => "1");
        $arg["data"]["SHORI_MEI"] = createCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", 1);

        //ヘッダ有無
        $extra = "checked id=\"HEADERCHECK\"";
        $arg["data"]["HEADERCHECK"] = createCheckBox($objForm, "HEADERCHECK", "1", $extra, "");

        //ファイルからの取り込み
        $arg["data"]["FILE"] = createFile($objForm, "FILE", 2048000);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knja041index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja041Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["button"]["BTN_OK"] = createBtn($objForm, "btn_ok", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["BTN_CLEAR"] = createBtn($objForm, "btn_cancel", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm)
{
    $objForm->ae(createHiddenAe("cmd"));
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    for ($i = 1; $i <= $count; $i++) {
        if (is_array($extra)) {
            $ext = $extra[$i-1];
        } else {
            $ext = $extra;
        }
        
        $objForm->ae(array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $ext,
                            "multiple"  => $multi));

        $arg["data"][$name.$i]  = $objForm->ge($name, $i);
    }
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae(array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{
    $objForm->ae(array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}

//File作成
function createFile(&$objForm, $name, $size, $extra = "")
{
    $objForm->add_element(array("type"      => "file",
                                "name"      => $name,
                                "size"      => $size,
                                "extrahtml" => $extra ));

    return $objForm->ge($name);
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
