<?php

require_once('for_php7.php');

class knjh336Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //権限チェック
        authCheck($arg);

        //ＤＢ接続
        $db = Query::dbCheckOut();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = "現在年度・学期：" .CTRL_YEAR ."年度" .CTRL_SEMESTERNAME ."　模試データＣＳＶ処理";
        
        $model->z010Name1 = $db->getOne(knjh336Query::getZ010name1());
        if ($model->z010Name1 == 'musashinohigashi') {
            $arg["notUseMockDat"] = "1";
        } else {
            $arg["useMockDat"] = "1";
        }

        //radio  1:得点 2:総点
        $opt = array(1, 2);
        $model->field["PUTDATA"] = ($model->z010Name1 == 'musashinohigashi') ? "2" : (($model->field["PUTDATA"] == "") ? "1" : $model->field["PUTDATA"]);
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"PUTDATA{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "PUTDATA", $model->field["PUTDATA"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //PUTDATA使用
        if ($model->Properties["useMockSchregDat"] == "1") {
            $arg["useMockSchregDat"] = "1";
        }

        //テンプレートデータ種別コンボボックス
        $query = knjh336Query::getMockDiv();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["HEAD_DATA_DIV"], "HEAD_DATA_DIV", $extra, 1);

        //処理名コンボボックス
        $data = array("更新", "削除");
        $extra = "";
        makeKoteiCmb($objForm, $arg, "SHORI_MEI", $data, $model->field["SHORI_MEI"], $extra, 1);

        //更新データ種別コンボボックス
        $query = knjh336Query::getMockDiv();
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["INS_DATA_DIV"], "INS_DATA_DIV", $extra, 1);

        //更新テスト名称コンボ
        $query = knjh336Query::getMockTargetMst($model->field["INS_DATA_DIV"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["INS_MOCK_TARGET"], "INS_MOCK_TARGET", $extra, 1);

        //年度・学期コンボ
        $query = knjh336Query::getYearSem();
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1);

        //年組一覧コンボボックス作成
        $query = knjh336Query::getGradeClass($model);
        $extra = ($model->field["OUT_DATA_DIV"] == "3") ? "disabled" : "";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        //出力データ種別コンボボックス
        $query = knjh336Query::getMockDiv();
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["OUT_DATA_DIV"], "OUT_DATA_DIV", $extra, 1);

        //出力テスト名称コンボ
        $query = knjh336Query::getMockTargetMst($model->field["OUT_DATA_DIV"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["OUT_MOCK_TARGET"], "OUT_MOCK_TARGET", $extra, 1);

        //ヘッダ有チェックボックス
        $check_header = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra = " id=\"HEADER\"";
        $arg["data"]["HEADER"] = createCheckBox($objForm, "HEADER", "on", $check_header.$extra, "");

        //出力取込種別ラジオボタン  1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ファイルからの取り込み
        $arg["FILE"] = createFile($objForm, "FILE", "", 4096000);

        //ボタン作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        $objForm->ae(createHidden("cmd"));

        //ＤＢ切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjh336index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh336Form1.html", $arg);
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
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
