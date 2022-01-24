<?php

require_once('for_php7.php');
class knja171Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja171Form1", "POST", "knja171index.php", "", "knja171Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->control["年度"];

        //学期コンボボックス
        $extra = "onchange=\"return btn_submit('knja171');\"";
        $query = knja171Query::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["OUTPUT"], "OUTPUT", $extra, 1);

        //出力指定ラジオボタン 1:クラス 2:個人
        if ($model->Properties["dispMTokuHouJituGrdMixChkRad"] == "1") {
            $arg["dispJituGrdMix"] = "1";
            $opt_choice = array(1, 2, 3);
            $model->field["CHOICE"] = ($model->field["CHOICE"] == "") ? "1" : $model->field["CHOICE"];
            $extra = "onclick =\" return btn_submit('knja171');\"";
            createRadio($objForm, $arg, "CHOICE", $model->field["CHOICE"], $extra, $opt_choice, get_count($opt_choice));

            //学年混合(チェックボックス)
            $extra  = $model->field["GAKUNEN_KONGOU"] == "1" ? "checked" : "";
            $extra .= " id=\"GAKUNEN_KONGOU\" onclick=\"return btn_submit('knja171');\"";
            if ($model->field["CHOICE"] != "1") {
                $extra .= "disabled ";
            }
            $arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");
        } else {
            $arg["nodispJituGrdMix"] = "1";
            $opt_choice = array(1, 2);
            $model->field["CHOICE"] = ($model->field["CHOICE"] == "") ? "1" : $model->field["CHOICE"];
            $extra = "onclick =\" return btn_submit('knja171');\"";
            createRadio($objForm, $arg, "CHOICE", $model->field["CHOICE"], $extra, $opt_choice, get_count($opt_choice));
        }

        //出力指定により処理が変わる
        if ($model->field["CHOICE"] == "2") {
            $arg["gr_class"] = "ON";
            //クラスコンボボックス
            $extra = "onChange=\"return btn_submit('knja171');\"";
            $query = knja171Query::getAuthClass($model);
            makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);
        }

        //出力対象一覧リストを作成する
        makeListToList($objForm, $arg, $db, $model);

        //電話番号チェックボックス
        $check_tel = ($model->cmd == "" || $model->field["TEL"] != "") ? "checked" : "";
        //$arg["data"]["TEL"] = createCheckBox($objForm, "TEL", "1", $check_tel." id=\"TEL\"", "");
        $arg["data"]["TEL"] = knjCreateCheckBox($objForm, "TEL", "1", $check_tel." id=\"TEL\"");

        //急用電話番号チェックボックス
        $check_etel = ($model->cmd == "" || $model->field["E_TEL"] != "") ? "checked" : "";
        //$arg["data"]["E_TEL"] = createCheckBox($objForm, "E_TEL", "1", $check_etel." id=\"E_TEL\"", "");
        $arg["data"]["E_TEL"] = knjCreateCheckBox($objForm, "E_TEL", "1", $check_etel." id=\"E_TEL\"");

        //性別チェックボックス
        $check_sex = ($model->cmd == "" || $model->field["SEX"] != "") ? "checked" : "";
        //$arg["data"]["SEX"] = createCheckBox($objForm, "SEX", "1", $check_sex." id=\"SEX\"", "");
        $arg["data"]["SEX"] = knjCreateCheckBox($objForm, "SEX", "1", $check_sex." id=\"SEX\"");

        //生年月日チェックボックス
        $check_birth = ($model->cmd == "" || $model->field["BIRTHDAY"] != "") ? "checked" : "";
        //$arg["data"]["BIRTHDAY"] = createCheckBox($objForm, "BIRTHDAY", "1", $check_birth." id=\"BIRTHDAY\"", "");
        $arg["data"]["BIRTHDAY"] = knjCreateCheckBox($objForm, "BIRTHDAY", "1", $check_birth." id=\"BIRTHDAY\"");

        //フォーム選択ラジオボタン 1:Ａ４横（性別なし） 2:Ａ４横（性別あり） 3:Ａ４縦（性別あり） 4:Ａ４縦（入学種別あり） 5:Ａ４横（５０人）
        $opt_form = array(1, 2, 3, 4, 5);
        $model->field["FORM"] = ($model->field["FORM"] == "") ? "1" : $model->field["FORM"];
        createRadio($objForm, $arg, "FORM", $model->field["FORM"], "", $opt_form, get_count($opt_form));

        //checkbox
        $extra = "id = \"FORM3_CLUB_CHECK\"";
        $extra .= $model->field["FORM3_CLUB_CHECK"] == "1" ? " checked " : "";
        $arg["data"]["FORM3_CLUB_CHECK"] = knjCreateCheckBox($objForm, "FORM3_CLUB_CHECK", "1", $extra);

        $extra = "id = \"FORM3_GRD_CHECK\"";
        $extra .= $model->field["FORM3_GRD_CHECK"] == "1" ? " checked " : "";
        $arg["data"]["FORM3_GRD_CHECK"] = knjCreateCheckBox($objForm, "FORM3_GRD_CHECK", "1", $extra);

        if ($model->ismeikei) {
            $arg["is_meikei"] = "ON";
            $extra = "id = \"FORM3_DORMITORY_CHECK\"";
            $extra .= (is_null($model->field["FORM3_DORMITORY_CHECK"]) || $model->field["FORM3_DORMITORY_CHECK"] == "1") ? " checked " : "";
            $arg["data"]["FORM3_DORMITORY_CHECK"] = knjCreateCheckBox($objForm, "FORM3_DORMITORY_CHECK", "1", $extra);
        }

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja171Form1.html", $arg);
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
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        if ($name == "OUTPUT") {
            $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
        } else {
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        }
        $result->free();
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//出力対象一覧リストを作成する
function makeListToList(&$objForm, &$arg, $db, $model)
{
    if ($model->field["CHOICE"] == "1" || $model->field["CHOICE"] == "3") {
        $arg["CHANGENAME"] = "クラス";
        $query = knja171Query::getAuthClass($model);
    } else {
        $schName = "";
        //テーブルの有無チェック
        $query = knja171Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && (($model->field["CHOICE"] == "2" && $model->field["GRADE_HR_CLASS"]) || ($model->Properties["use_prg_schoolkind"] == "1"))) {
            //生徒項目名取得
            $schName = $db->getOne(knja171Query::getSchName($model));
        }
        $model->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        $arg["CHANGENAME"] = $model->sch_label;
        $query = knja171Query::getAuthStudent($model);
    }

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
    //$arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);
    $arg["button"]["btn_rights"] = KnjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    //$arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    //$arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    //$arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //印刷ボタン
    //$arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //CSVボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    //$arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタン
    //$arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "PRGID", "KNJA171");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "useClubMultiSchoolKind", $model->Properties["useClubMultiSchoolKind"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "KNJA171_UseFixedTitile", $model->Properties["KNJA171_UseFixedTitile"]);
    knjCreateHidden($objForm, "dispMTokuHouJituGrdMixChkRad", $model->Properties["dispMTokuHouJituGrdMixChkRad"]);
    knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);
    knjCreateHidden($objForm, "useFi_Hrclass", $model->Properties["useFi_Hrclass"]);
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    $radioArray = knjCreateRadio($objForm, $name, $value, $extra, $multi, $count);
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }
    // for ($i = 1; $i <= $count; $i++) {
    //     $objForm->ae( array("type"      => "radio",
    //                         "name"      => $name,
    //                         "value"     => $value,
    //                         "extrahtml" => $extra."id=".$name.$i."",
    //                         "multiple"  => $multi));
    //     $arg["data"][$name.$i] = $objForm->ge($name, $i);
    // }
}

// //チェックボックス作成
// function createCheckBox(&$objForm, $name, $value, $extra, $multi)
// {
//     $objForm->ae( array("type"      => "checkbox",
//                         "name"      => $name,
//                         "value"     => $value,
//                         "extrahtml" => $extra,
//                         "multiple"  => $multi));
//
//     return $objForm->ge($name);
// }

// //ボタン作成
// function createBtn(&$objForm, $name, $value, $extra)
// {
//     $objForm->ae( array("type"        => "button",
//                         "name"        => $name,
//                         "extrahtml"   => $extra,
//                         "value"       => $value ) );
//     return $objForm->ge($name);
// }

// //コンボ作成
// function createCombo(&$objForm, $name, $value, $options, $extra, $size)
// {
//     $objForm->ae( array("type"      => "select",
//                         "name"      => $name,
//                         "size"      => $size,
//                         "value"     => $value,
//                         "extrahtml" => $extra,
//                         "options"   => $options));
//     return $objForm->ge($name);
// }
