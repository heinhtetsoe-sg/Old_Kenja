<?php

require_once('for_php7.php');

class knja171mForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja171mForm1", "POST", "knja171mindex.php", "", "knja171mForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $model->field["YEAR"] = ($model->field["YEAR"] == "") ? "CTRL_YEAR" : $model->field["YEAR"];
        $extra = "onchange=\"return btn_submit('knja171m');\"";
        $query = knja171mQuery::getYear($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1);

        //出力指定ラジオボタン 1:クラス 2:個人
        $opt_choice = array(1, 2);
        $model->field["CHOICE"] = ($model->field["CHOICE"] == "") ? "1" : $model->field["CHOICE"];
        $extra = "onclick =\" return btn_submit('knja171m');\"";
        createRadio($objForm, $arg, "CHOICE", $model->field["CHOICE"], $extra, $opt_choice, get_count($opt_choice));

        //学期コンボボックス
        $extra = "onchange=\"return btn_submit('knja171m');\"";
        $query = knja171mQuery::getSemester($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //履修登録者チェックボックス
        if ($model->field["RISHUUCHECK"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= "onClick=\"btn_submit('knja171m')\" id=\"RISHUUCHECK\"";
        $arg["data"]["RISHUUCHECK"] = knjCreateCheckBox($objForm, "RISHUUCHECK", "1", $extra);

        //出力指定により処理が変わる
        if ($model->field["CHOICE"] == "2") {
            $arg["gr_class"] = "ON";
            //クラスコンボボックス
            $extra = "onChange=\"return btn_submit('knja171m');\"";
            $query = knja171mQuery::getAuthClass($model);
            makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);
        }

        //出力対象一覧リストを作成する
        makeListToList($objForm, $arg, $db, $model);

        //対象者
        $opt = array(1, 2, 3, 4);
        $model->field["TAISHOUSHA"] = ($model->field["TAISHOUSHA"] == "") ? "1" : $model->field["TAISHOUSHA"];
        $extra = array("id=\"TAISHOUSHA1\"", "id=\"TAISHOUSHA2\"", "id=\"TAISHOUSHA3\"", "id=\"TAISHOUSHA4\"");
        $radioArray = knjCreateRadio($objForm, "TAISHOUSHA", $model->field["TAISHOUSHA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //checkbox
        $extra = "id=\"KAIPAGE_OFF\"";
        $arg["data"]["KAIPAGE_OFF"] = knjCreateCheckBox($objForm, "KAIPAGE_OFF", "1", $extra);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja171mForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    if ($query) {
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        if ($name == "SEMESTER") {
            $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
        } else {
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        }
        $result->free();
    }
    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//出力対象一覧リストを作成する
function makeListToList(&$objForm, &$arg, $db, $model)
{
    if ($model->field["CHOICE"] == "1") {
        $arg["CHANGENAME"] = "クラス";
        $query = knja171mQuery::getAuthClass($model);
    } else {
        $arg["CHANGENAME"] = "生徒";
        $query = knja171mQuery::getAuthStudent($model);
    }

    //一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    makeCmb($objForm, $arg, $db, $query, $model->field["DUMMY"], "CATEGORY_NAME", $extra, 15);

    //出力対象リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    makeCmb($objForm, $arg, $db, "", $model->field["DUMMY"], "CATEGORY_SELECTED", $extra, 15);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //終了ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $db, $model)
{
    $objForm->ae(createHiddenAe("PRGID", "KNJA171M"));
    $objForm->ae(createHiddenAe("CTRL_YEAR", CTRL_YEAR));
    $objForm->ae(createHiddenAe("CTRL_SEMESTER", CTRL_SEMESTER));
    $objForm->ae(createHiddenAe("LOGIN_DATE", CTRL_DATE));
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("cmd"));
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    for ($i = 1; $i <= $count; $i++) {
        $objForm->ae( array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $extra."id=".$name.$i."",
                            "multiple"  => $multi));
        $arg["data"][$name.$i] = $objForm->ge($name, $i);
    }
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
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
    $objForm->ae( array("type"        => "button",
                        "name"        => $name,
                        "extrahtml"   => $extra,
                        "value"       => $value ) );
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
