<?php

require_once('for_php7.php');

class knja033Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja033Form1", "POST", "knja033index.php", "", "knja033Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //処理学期チェック
        $seme_check = $db->getOne(knja033Query::getSemeName(CTRL_YEAR));
        if ($seme_check != CTRL_SEMESTER){
            $arg["Closing"] = "  closing_window(); " ;
        }

        //年度コンボボックス
        $query = knja033Query::getYear($model);
        $extra = "onchange=\"return btn_submit('knja033'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "THIS_YEAR", $model->field["THIS_YEAR"], $extra, 1);

        //学期
        $seme = $db->getRow(knja033Query::getSemeName($model->field["THIS_YEAR"]), DB_FETCHMODE_ASSOC);
        $arg["data"]["SEME_NAME"] = $seme["SEMESTERNAME"];
        knjCreateHidden($objForm, "MAX_SEMESTER", $seme["SEMESTER"]);

        //帳票選択ラジオボタン 1.進級生 2.卒業生 3.留年生
        $radioValue = array(1, 2, 3);
        $disable = 0;
        if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;
        if ($model->field["OUTPUT"] == 3) $disable = 1;
        $click = "onclick =\" return btn_submit('knja033');\"";
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $radioValue, get_count($radioValue));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($disable == 1) {
            $arg["remain"] = 1;
        } else {
            $arg["promotion"] = 1;

            //クラスリストを作成する
            makeClassList($objForm, $arg, $db, $model, $seme);
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja033Form1.html", $arg); 
    }
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

    if ($name == "THIS_YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラスのリストToリスト作成
function makeClassList(&$objForm, &$arg, $db, $model, $seme)
{
    $query = knja033Query::getHrClassAuth($model, $model->field["THIS_YEAR"], $seme["SEMESTER"], AUTHORITY, STAFFCD);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }

    $result->free();

    //クラス一覧を作成する
    $extra = "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 20);

    //出力対象一覧を作成する
    $extra = "multiple style=\"width:200px\" width:\"200px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", array(), $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA033");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "NEXT_YEAR", $model->field["THIS_YEAR"]+1);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "COUNTFLG", SCHOOLKIND);
    knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}

?>
