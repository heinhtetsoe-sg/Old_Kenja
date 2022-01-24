<?php

require_once('for_php7.php');

class knjh344Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh344Form1", "POST", "knjh344index.php", "", "knjh3344Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->control["年度"];

//CHOICEは不要。
//OUTPUTはGRADEに。
//ismeikeiは不要
//FORM、及びFORM1～FORM5まで、不要。
//FORM3_CLUB_CHECK、FORM3_GRD_CHECKも不要。
//FORM3_DORMITORY_CHECKも不要。
//CHANGENAMEも不要。
//btn_printも不要。
//CLUBNAMEも不要。
//FINSCHOOL_NAMEも不要。

        //校種コンボボックス
        $extra = "onchange=\"return btn_submit('knjh344');\"";
        $query = knjh344Query::getSchkind($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHKIND"], "SCHKIND", $extra, 1);

        //学期コンボボックス
        $extra = "onchange=\"return btn_submit('knjh344');\"";
        $query = knjh344Query::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        //出力対象一覧リストを作成する
        makeListToList($objForm, $arg, $db, $model);

        //次年度選択科目チェックボックス
        $check_selsubject_nextyear = ($model->cmd == "" || $model->field["SELSUBJECT_NEXTYEAR"] != "") ? "checked" : "";
        $arg["data"]["SELSUBJECT_NEXTYEAR"] = createCheckBox($objForm, "SELSUBJECT_NEXTYEAR", "1", $check_selsubject_nextyear." id=\"SELSUBJECT_NEXTYEAR\"", "");

        //評定平均チェックボックス
        $check_ratingave = ($model->cmd == "" || $model->field["RATINGAVE"] != "") ? "checked" : "";
        $arg["data"]["RATINGAVE"] = createCheckBox($objForm, "RATINGAVE", "1", $check_ratingave." id=\"RATINGAVE\"", "");

        //志望校チェックボックス
        $check_ambitionschool = ($model->cmd == "" || $model->field["AMBITIONSCHOOL"] != "") ? "checked" : "";
        $arg["data"]["AMBITIONSCHOOL"] = createCheckBox($objForm, "AMBITIONSCHOOL", "1", $check_ambitionschool." id=\"AMBITIONSCHOOL\"", "");

        //校外模試1チェックボックス
        $check_outside_trial1 = ($model->cmd == "" || $model->field["OUTSIDE_TRIALTEST1"] != "") ? "checked" : "";
        $arg["data"]["OUTSIDE_TRIALTEST1"] = createCheckBox($objForm, "OUTSIDE_TRIALTEST1", "1", $check_outside_trial1." id=\"OUTSIDE_TRIALTEST1\"  onchange=\"return chkboxchange('1');\"", "");

        //校外模試1チェックボックス
        $check_outside_trial2 = ($model->cmd == "" || $model->field["OUTSIDE_TRIALTEST2"] != "") ? "checked" : "";
        $arg["data"]["OUTSIDE_TRIALTEST2"] = createCheckBox($objForm, "OUTSIDE_TRIALTEST2", "1", $check_outside_trial2." id=\"OUTSIDE_TRIALTEST2\" onchange=\"return chkboxchange('2');\"", "");

//        //フォーム選択ラジオボタン 1:Ａ４横（性別なし） 2:Ａ４横（性別あり） 3:Ａ４縦（性別あり） 4:Ａ４縦（入学種別あり） 5:Ａ４横（５０人）
//        $opt_form = array(1, 2, 3, 4, 5);
//        $model->field["FORM"] = ($model->field["FORM"] == "") ? "1" : $model->field["FORM"];
//        createRadio($objForm, $arg, "FORM", $model->field["FORM"], "", $opt_form, get_count($opt_form));

//        //checkbox
//        $extra = "id = \"FORM3_CLUB_CHECK\"";
//        $extra .= $model->field["FORM3_CLUB_CHECK"] == "1" ? " checked " : "";
//        $arg["data"]["FORM3_CLUB_CHECK"] = knjCreateCheckBox($objForm, "FORM3_CLUB_CHECK", "1", $extra);
//
//        $extra = "id = \"FORM3_GRD_CHECK\"";
//        $extra .= $model->field["FORM3_GRD_CHECK"] == "1" ? " checked " : "";
//        $arg["data"]["FORM3_GRD_CHECK"] = knjCreateCheckBox($objForm, "FORM3_GRD_CHECK", "1", $extra);
//
//        if ($model->ismeikei) {
//            $arg["is_meikei"] = "ON";
//            $extra = "id = \"FORM3_DORMITORY_CHECK\"";
//            $extra .= (is_null($model->field["FORM3_DORMITORY_CHECK"]) || $model->field["FORM3_DORMITORY_CHECK"] == "1") ? " checked " : "";
//            $arg["data"]["FORM3_DORMITORY_CHECK"] = knjCreateCheckBox($objForm, "FORM3_DORMITORY_CHECK", "1", $extra);
//        }

        //模試選択1
        $extra = "onchange=\"return btn_submit('knjh344');\"";
        if ($check_outside_trial1 != "checked") {
            $extra .= " disabled";
        }
        $query = knjh344Query::getTrialList($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TRIALTEST1_NAME"], "TRIALTEST1_NAME", $extra, 1);

        //模試選択2
        $extra = "onchange=\"return btn_submit('knjh344');\"";
        if ($check_outside_trial2 != "checked") {
            $extra .= " disabled";
        }
        $query = knjh344Query::getTrialList($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TRIALTEST2_NAME"], "TRIALTEST2_NAME", $extra, 1);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh344Form1.html", $arg); 
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
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $result->free();
    }
    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//出力対象一覧リストを作成する
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $query = knjh344Query::getAuthClass($model);
    $result = $db->query($query);
    $optR = $optL = array();
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->selectdata && in_array($row["VALUE"], $selectdata)) {
            $optL[] = array("label" => $row["LABEL"],
                            "value" => $row["VALUE"]);
        } else {
            $optR[] = array("label" => $row["LABEL"],
                            "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $optR, $extra, 20);

    //出力対象リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $optL, $extra, 20);

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
   //CSVボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタン
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "PRGID", "KNJH344");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
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

?>
