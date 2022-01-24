<?php

require_once('for_php7.php');


class knjh568Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh568Form1", "POST", "knjh568index.php", "", "knjh568Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjh568Query::getSemester();
        if ($model->field["SELECT_DIV"] == "1" || $model->field["SELECT_DIV"] == "2" ) {
            $extra = "onchange=\"return btn_submit('knjh568'), AllClearList();\"";
        } else {
            $extra = "onchange=\"return btn_submit('knjh568');\"";
        }
        if ($model->field["SEMESTER"] == "" ){
            $model->field["SEMESTER"] = CTRL_SEMESTER;
        }
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //データ種別コンボ作成
        $query = knjh568Query::getDataDiv();
        $extra = "onchange=\"return btn_submit('knjh568')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYDIV", $model->field["PROFICIENCYDIV"], $extra, 1);

        //テスト名称コンボ作成
        $query = knjh568Query::getProName($model);
        $extra = "onchange=\"return btn_submit('knjh568')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYCD", $model->field["PROFICIENCYCD"], $extra, 1);

        //学年コンボを先に作成する為、この位置で初期値設定する。
        $model->field["SELECT_DIV"] = ($model->field["SELECT_DIV"] == "") ? "3" : $model->field["SELECT_DIV"];

        //学年コンボ作成
        $query = knjh568Query::getGradeHrClass($model->field["SEMESTER"], $model);
        if ($model->field["SELECT_DIV"] == "1" || $model->field["SELECT_DIV"] == "2") {
            $extra = "onchange=\"return btn_submit('chgGrade'), AllClearList();\"";
        } else {
            $extra = "onchange=\"return btn_submit('chgGrade');\"";
        }
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //校種
        $query = knjh568Query::getSchoolKind($model);
        $befSchoolKind = $model->selectSchoolKind;
        $model->selectSchoolKind = $db->getOne($query);
        if ($model->selectSchoolKind != "P") {
            $arg["UN_PRIMARY"] = "1";
        }
        if ($model->cmd == "chgGrade" && $befSchoolKind != $model->selectSchoolKind) {
            $model->field["SELECT_DIV"] = "3";
            $model->field["GROUP_DIV"] = "";
        }

        //選択区分ラジオボタン 1:クラス選択 2:コース選択 3:学年選択
        $opt_div = array(1, 2, 3);
        $extra  = array("id=\"SELECT_DIV1\" onclick=\"return btn_submit('knjh568')\"", "id=\"SELECT_DIV2\" onclick=\"return btn_submit('knjh568')\"", "id=\"SELECT_DIV3\" onclick=\"return btn_submit('knjh568')\"");
        $radioArray = knjCreateRadio($objForm, "SELECT_DIV", $model->field["SELECT_DIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["SELECT_DIV"] == 1 || $model->field["SELECT_DIV"] == 2) $arg["class_course"] = '1';
        if ($model->field["SELECT_DIV"] == 3) $arg["grade"] = '1';

        //上位出力人数
        $model->field["PRINT_COUNT"] = $model->field["PRINT_COUNT"] == '' ? "40" : $model->field["PRINT_COUNT"];
        $extra  = "onblur=\"this.value=toInteger(this.value)\" style=\"text-align: right;\" ";
        $arg["data"]["PRINT_COUNT"] = knjCreateTextBox($objForm, $model->field["PRINT_COUNT"], "PRINT_COUNT", 3, 3, $extra);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh568Form1.html", $arg); 
    }
}

function makeListToList(&$objForm, &$arg, $db, $model) {

    //対象一覧リストを作成する
    $query = ($model->field["SELECT_DIV"] == "1") ? knjh568Query::getHrClass($model) : knjh568Query::getCourse($model);
    $result = $db->query($query);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 15);

    $arg["data"]["NAME_LIST"] = ($model->field["SELECT_DIV"] == "1") ? 'クラス一覧' : 'コース一覧';

    //出力対象一覧リストを作成する
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 15);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'print');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "CHANGE", $model->field["SELECT_DIV"]);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJH568");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SUBCLASS_GROUP", $model->subclassGroup);
    knjCreateHidden($objForm, "useKnjd106cJuni1", $model->useKnjd106cJuni1);
    knjCreateHidden($objForm, "useKnjd106cJuni2", $model->useKnjd106cJuni2);
    knjCreateHidden($objForm, "useKnjd106cJuni3", $model->useKnjd106cJuni3);
    knjCreateHidden($objForm, "useRadioPattern", $model->setUseRadioPattern);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "SELECT_SCHOOLKIND", $model->selectSchoolKind);
}

?>
