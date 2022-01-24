<?php

require_once('for_php7.php');


class knjh561Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh561Form1", "POST", "knjh561index.php", "", "knjh561Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjh561Query::getSemester();
        if ($model->field["SELECT_DIV"] == "1" || $model->field["SELECT_DIV"] == "2" ) {
            $extra = "onchange=\"return btn_submit('knjh561'), AllClearList();\"";
        } else {
            $extra = "onchange=\"return btn_submit('knjh561');\"";
        }
        if ($model->field["SEMESTER"] == "" ){
            $model->field["SEMESTER"] = CTRL_SEMESTER;
        }
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //データ種別コンボ作成
        $query = knjh561Query::getDataDiv();
        $extra = "onchange=\"return btn_submit('knjh561')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYDIV", $model->field["PROFICIENCYDIV"], $extra, 1);

        //テスト名称コンボ作成
        $query = knjh561Query::getProName($model);
        $extra = "onchange=\"return btn_submit('knjh561')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYCD", $model->field["PROFICIENCYCD"], $extra, 1);

        //学年コンボを先に作成する為、この位置で初期値設定する。
        $model->field["SELECT_DIV"] = ($model->field["SELECT_DIV"] == "") ? "1" : $model->field["SELECT_DIV"];

        //学年コンボ作成
        $query = knjh561Query::getGradeHrClass($model->field["SEMESTER"], $model);
        if ($model->field["SELECT_DIV"] == "1" || $model->field["SELECT_DIV"] == "2") {
            $extra = "onchange=\"return btn_submit('chgGrade'), AllClearList();\"";
        } else {
            $extra = "onchange=\"return btn_submit('chgGrade');\"";
        }
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //校種
        $query = knjh561Query::getSchoolKind($model);
        $befSchoolKind = $model->selectSchoolKind;
        $model->selectSchoolKind = $db->getOne($query);
        if ($model->selectSchoolKind != "P") {
            $arg["UN_PRIMARY"] = "1";
        }
        if ($model->cmd == "chgGrade" && $befSchoolKind != $model->selectSchoolKind) {
            $model->field["SELECT_DIV"] = "1";
            $model->field["GROUP_DIV"] = "";
            $model->field["JUNI"] = "";
            $model->field["SORT"] = "";
        }

        //選択区分ラジオボタン 1:クラス選択 2:コース選択 3:学年選択
        $opt_div = array(1, 2, 3);
        $extra  = array("id=\"SELECT_DIV1\" onclick=\"return btn_submit('knjh561')\"", "id=\"SELECT_DIV2\" onclick=\"return btn_submit('knjh561')\"", "id=\"SELECT_DIV3\" onclick=\"return btn_submit('knjh561')\"");
        $radioArray = knjCreateRadio($objForm, "SELECT_DIV", $model->field["SELECT_DIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["SELECT_DIV"] == 1 || $model->field["SELECT_DIV"] == 2) $arg["class_course"] = '1';
        if ($model->field["SELECT_DIV"] == 3) $arg["grade"] = '1';

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //グループラジオボタン 各値 1:学年　2:クラス　3:コース　4:学科　5:コースグループ
        $set_group_div = "";
        $opt_group = array();
        $rankdiv_array = array();
        $check_array = array();
        //プロパティの値チェック
        $rankdiv_array = explode("-", $model->Properties["useRadioPattern"]);
        foreach($rankdiv_array as $key => $val) {
            if ($val != "1" && $val != "2" && $val != "3" && $val != "4" && $val != "5") {
                //値が不正の場合は下記をセット
                $model->Properties["useRadioPattern"] = "1-2";
            }
        }
        //プロパティの値をセット
        $rankdiv_array = explode("-", $model->Properties["useRadioPattern"]);
        $model->setUseRadioPattern = "";
        $setSep = "";
        foreach($rankdiv_array as $key => $val) {
            if ($model->selectSchoolKind == "P" && $val != "1") {
                continue;
            }
            $model->setUseRadioPattern .= $setSep.$val;
            $setSep = "-";
            $opt_group[(int)$key + 1] = $val;
            //5:コースグループの初期値をセット（プロパティusePerfectCourseGroup用）
            if ($val == "5") {
                $set_group_div = (int)$key + 1;
            }
        }
        $set_name_array = array();
        $set_name_array[1] = '学年';
        $set_name_array[2] = 'クラス';
        $set_name_array[3] = 'コース';
        $set_name_array[4] = '学科';
        $set_name_array[5] = 'コースグループ';

        //ラジオ作成
        if ($model->Properties["usePerfectCourseGroup"] === '1') {
            if ($set_group_div) {
                $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? $set_group_div : $model->field["GROUP_DIV"];
            } else {
                $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
            }
        } else {
            $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
        }
        $radioArray = array();
        $ret = array();
        for ($count = 1; $count <= get_count($opt_group); $count++) {
            $objForm->ae( array("type"      => "radio",
                                "name"      => "GROUP_DIV",
                                "value"     => $model->field["GROUP_DIV"],
                                "extrahtml" => "id=\"GROUP_DIV{$count}\"",
                                "multiple"  => $opt_group));
            $ret["GROUP_DIV".$count] = $objForm->ge("GROUP_DIV", $count);
            $arg["data"]["GROUP_DIV_NAME".$count] = $set_name_array[$opt_group[$count]].'　';
        }
        $radioArray = $ret;
        foreach($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //順位ラジオボタン 1:総合点 2:平均点 3:偏差値 4:傾斜総合点
        $opt_sort = array(1, 2, 3, 4);
        $model->field["JUNI"] = ($model->field["JUNI"] == "") ? "1" : $model->field["JUNI"];
        $extra = array("id=\"JUNI1\"", "id=\"JUNI2\"", "id=\"JUNI3\"", "id=\"JUNI4\"");
        $radioArray = knjCreateRadio($objForm, "JUNI", $model->field["JUNI"], $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        $disSort = ($model->field["OUTPUT_ASSESS_LEVEL"] == "1") ? " disabled" : "";
        //出力順ラジオボタン 1:５教科順 2:３教科順 3:クラス・出席番号順
        $opt_sort = array(1, 2, 3);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "3" : $model->field["SORT"];
        $extra = array("id=\"SORT1\"".$disSort, "id=\"SORT2\"".$disSort, "id=\"SORT3\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //テスト名称コンボ作成
        $query = knjh561Query::getProGroupNameCourseCount($model);
        $courseCount = $db->getOne($query);
        if ($courseCount == 1) {
            $name = $db->getOne(knjh561Query::getProGroupName($model, "5"));
            $arg["data"]["groupDiv5Name"] = ($name == "") ? "" : ($name."順");
            $name = $db->getOne(knjh561Query::getProGroupName($model, "3"));
            $arg["data"]["groupDiv3Name"] = ($name == "") ? "" : ($name."順");
        } else {
            $arg["data"]["groupDiv5Name"] = "５教科順";
            $arg["data"]["groupDiv3Name"] = "３教科順";
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh561Form1.html", $arg); 
    }
}

function makeListToList(&$objForm, &$arg, $db, $model) {

    //対象一覧リストを作成する
    $query = ($model->field["SELECT_DIV"] == "1") ? knjh561Query::getHrClass($model) : knjh561Query::getCourse($model);
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
    //CSVボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    //knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "CHANGE", $model->field["SELECT_DIV"]);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJH561");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SUBCLASS_GROUP", $model->subclassGroup);
    knjCreateHidden($objForm, "useKnjd106cJuni1", $model->useKnjd106cJuni1);
    knjCreateHidden($objForm, "useKnjd106cJuni2", $model->useKnjd106cJuni2);
    knjCreateHidden($objForm, "useKnjd106cJuni3", $model->useKnjd106cJuni3);
    knjCreateHidden($objForm, "FORM_GROUP_DIV");
    knjCreateHidden($objForm, "useRadioPattern", $model->setUseRadioPattern);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "SELECT_SCHOOLKIND", $model->selectSchoolKind);
    knjCreateHidden($objForm, "useFormNameH561", $model->Properties["useFormNameH561"]);
}

?>
