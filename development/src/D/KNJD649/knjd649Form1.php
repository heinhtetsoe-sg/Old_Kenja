<?php

require_once('for_php7.php');


class knjd649Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd649Form1", "POST", "knjd649index.php", "", "knjd649Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //データ種別コンボ作成
        $query = knjd649Query::getDataDiv();
        $extra = "onchange=\"return btn_submit('knjd649')\"";
        makeCmb($objForm, $arg, $db, $query, "DATA_DIV", $model->field["DATA_DIV"], $extra, 1);

        //テスト名称コンボ作成
        $query = knjd649Query::getMockName($model);
        makeCmb($objForm, $arg, $db, $query, "MOCKCD", $model->field["MOCKCD"], "", 1);

        //選択区分ラジオボタン 1:クラス選択 2:コース選択 3:学年選択
        $opt_div = array(1, 2, 3);
        $model->field["SELECT_DIV"] = ($model->field["SELECT_DIV"] == "") ? "1" : $model->field["SELECT_DIV"];
        $extra  = array("id=\"SELECT_DIV1\" onclick=\"return btn_submit('knjd649')\"", "id=\"SELECT_DIV2\" onclick=\"return btn_submit('knjd649')\"", "id=\"SELECT_DIV3\" onclick=\"return btn_submit('knjd649')\"");
        $radioArray = knjCreateRadio($objForm, "SELECT_DIV", $model->field["SELECT_DIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["SELECT_DIV"] == 1 || $model->field["SELECT_DIV"] == 2) $arg["class_course"] = '1';
        if ($model->field["SELECT_DIV"] == 3) $arg["grade"] = '1';

        //学年コンボ作成
        $query = knjd649Query::getGrade();
        $extra = "onchange=\"return btn_submit('knjd649'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

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
        View::toHTML($model, "knjd649Form1.html", $arg); 
    }
}

function makeListToList(&$objForm, &$arg, $db, $model) {

    //対象一覧リストを作成する
    $query = ($model->field["SELECT_DIV"] == "1") ? knjd649Query::getHrClass($model) : knjd649Query::getCourse($model);
    $result = $db->query($query);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 15);

    //対象一覧リスト名
    $arg["data"]["NAME_LIST"] = ($model->field["SELECT_DIV"] == "1") ? 'クラス一覧' : 'コース一覧';

    //出力対象一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
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
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //ＣＳＶボタンを作成する
    $extra = "onclick=\"return btn_submit('csv');\"";
//    $extra = "disabled";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "CHANGE", $model->field["SELECT_DIV"]);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD649");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "cmd");
}

?>
