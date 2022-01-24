<?php

require_once('for_php7.php');

/********************************************************************/
/* 対外模試データCSV出力                            山城 2005/10/17 */
/*                                                                  */
/* 変更履歴                                                         */
/*  ・NO001 テンプレート出力値を変更。              山城 2006/02/07 */
/********************************************************************/

//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knje101Form1
{
    public function main($model)
    {
        $objForm = new form();

        //ＤＢ接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //処理名コンボボックス
        $data = array("更新", "削除");
        $extra = "";
        makeKoteiCmb($objForm, $arg, "SHORI_MEI", $data, $model->field["SHORI_MEI"], $extra, 1);

        //年度・学期コンボ
        $query = knje101Query::getYearSem();
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1);

        //年組一覧コンボ作成
        $query = knje101Query::getGradeClass($model);
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        //テスト種別コンボ作成
        $query = knje101Query::getShamexamcd($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["SHAMEXAMCD"], "SHAMEXAMCD", $extra, 1, "ALL");

        //ヘッダ有チェックボックス
        $check_header = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $arg["data"]["HEADER"] = createCheckBox($objForm, "HEADER", "on", $check_header, "");

        //出力取込種別ラジオボタン
        $opt_shubetsu[0] = 1;        //ヘッダ出力
        $opt_shubetsu[1] = 2;        //データ取込
        $opt_shubetsu[2] = 3;        //エラー出力
        $opt_shubetsu[3] = 4;        //データ出力

        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        createRadio($objForm, $arg, "OUTPUT", $model->field["OUTPUT"], "", $opt_shubetsu, get_count($opt_shubetsu));

        //ファイルからの取り込み
        $arg["FILE"] = createFile($objForm, "FILE", "", 4096000);

        //ボタン作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        $objForm->ae(createHidden("cmd"));

        //ＤＢ切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje101index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje101Form1.html", $arg);
    }
}

//固定コンボ作成
function makeKoteiCmb(&$objForm, &$arg, $name, $data, &$value, $extra, $size)
{
    $opt = array();
    $serch = array();
    foreach ($data as $key => $val) {
        $opt[]   = array("label" => $val, "value" => ($key + 1));
        $serch[] = ($key + 1);
    }
    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "ALL") {
        $opt[] = array("label" => "(全て出力)",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }
    if ($name == "YEAR") {
        $value = ($value && in_array($value, $serch)) ? $value : CTRL_YEAR.CTRL_SEMESTER;
    } else {
        $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //実行
    $arg["btn_exec"] = createBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('execute');\"");
    //終了
    $arg["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
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

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    $objForm->ae(array("type"      => "radio",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));
    for ($i = 1; $i <= $count; $i++) {
        $arg["data"][$name.$i] = $objForm->ge($name, $i);
    }
}

//File作成
function createFile(&$objForm, $name, $extra, $size)
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
    $objForm->ae(array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ));
    return $objForm->ge($name);
}

//Hidden作成
function createHidden($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}
