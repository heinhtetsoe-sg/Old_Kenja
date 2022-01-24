<?php

require_once('for_php7.php');

class knja224cForm1 {
    function main(&$model){
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja224cForm1", "POST", "knja224cindex.php", "", "knja224cForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        if ($model->schoolName == 'MUSASHI') {
            $arg["is_musashi"] = 1;
            knjCreateHidden($objForm, "OUTPUT", 'musashi');
        } else if ($model->schoolName == 'CHIBEN') {
            knjCreateHidden($objForm, "OUTPUT", 'chiben');
        } else {
            $arg["is_not_musashi"] = 1;
        }

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //extra
        $extraCheck  = ($model->field["KARA"]) ? "checked" : "";
        $extraInt    = " onblur=\"this.value=toInteger(this.value)\";";
        $extraRight  = " STYLE=\"text-align: right\"";

        //空行チェックボックス
        $model->field["KARA"] = ($model->field["KARA"]) ? $model->field["KARA"] : "1";
        $arg["data"]["KARA"] = createCheckBox($objForm, "KARA", $model->field["KARA"], $extraCheck." id=\"KARA\"", "");

        //名票ラジオボタン 1:漢字 2:漢字・かな
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"", "id=\"OUTPUT5\"", "id=\"OUTPUT6\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学籍番号を表記するcheckbox
        $extra  = $model->field["PRINT_SCHREGNO"] ? " checked " : "";
        $extra .= "id=\"PRINT_SCHREGNO\"";
        $arg["data"]["PRINT_SCHREGNO"] = knjCreateCheckBox($objForm, "PRINT_SCHREGNO", "1", $extra);

        //出力件数
        $model->field["KENSUU"] = ($model->field["KENSUU"]) ? $model->field["KENSUU"] : 1;
        $arg["data"]["KENSUU"] = createTextBox($objForm, $model->field["KENSUU"], "KENSUU", 3, 2, $extraInt.$extraRight);

        //名前無しcheckbox
        if ($model->schoolName === 'tokiwa' ) {
            $extra  = ($model->field["NAME_NASI"]) ? " checked " : "";
        } else {
            $extra  = ($model->field["NAME_NASI"] || $model->cmd == "") ? " checked " : "";
        }
        $extra .= "id=\"NAME_NASI\"";
        $arg["data"]["NAME_NASI"] = knjCreateCheckBox($objForm, "NAME_NASI", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja224cForm1.html", $arg);
    }
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knja224cQuery::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px; height:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = createCombo($objForm, "CLASS_NAME", "", $row1, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:230px; height:230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = createCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //ＣＳＶ出力ボタン
    $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return btn_submit('csv');\"");
    //終了ボタン
    $arg["button"]["btn_end"]   = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg)
{
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("PRGID", "KNJA224C"));
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("selectdata"));
    $objForm->ae(createHiddenAe("GAKKI", CTRL_SEMESTER));
    $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
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
