<?php

require_once('for_php7.php');

class knja171fForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja171fForm1", "POST", "knja171findex.php", "", "knja171fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //クラス方式選択 (1:法定クラス 2:複式クラス)
        if ($model->Properties["useFi_Hrclass"] == "1") {
            $opt = array(1, 2);
            if ($model->Properties["useFi_Hrclass"] === '1') {
                if ($model->Properties["defaultHoutei"] == "1") {
                    $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
                } else {
                    $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "2" : $model->field["HR_CLASS_TYPE"];
                }
            } else {
                $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
            }
            $arg["useFi_Hrclass"] = 1;
        } else {
            if ($model->field["HR_CLASS_TYPE"] == "") $model->field["HR_CLASS_TYPE"] = "1";
            knjCreateHidden($objForm, "HR_CLASS_TYPE", "1");
        }
        $click = " onClick=\"return btn_submit('knja171f');\"";
        $extra = array("id=\"HR_CLASS_TYPE1\"".$click, "id=\"HR_CLASS_TYPE2\"".$click);
        $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学年コンボ作成
        $query = knja171fQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('knja171f'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //出力区分選択 (1:職員用 2:保護者用)
        $opt_div = array(1, 2);
        $model->field["PRINT_RADIO"] = ($model->field["PRINT_RADIO"] == "") ? "1" : $model->field["PRINT_RADIO"];
        $extra = array(" onclick=\"kubun();\" id=\"PRINT_RADIO1\"", " onclick=\"kubun();\" id=\"PRINT_RADIO2\"");
        $radioArray = knjCreateRadio($objForm, "PRINT_RADIO", $model->field["PRINT_RADIO"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //退学者・転学者・卒業生は除く
        $extra = ($model->field["JOSEKI"] == "1") ? "checked" : "";
        $extra .= " id=\"JOSEKI\"";
        $arg["data"]["JOSEKI"] = knjCreateCheckBox($objForm, "JOSEKI", "1", $extra, "");

        //保護者氏名、保護者ふりがなの表記しない
        $extra = ($model->field["NOT_PRINT_HOGOSHA_NAME"] == "1") ? "checked" : "";
        $extra .= " id=\"NOT_PRINT_HOGOSHA_NAME\"";
        $extra .= $model->field["PRINT_RADIO"] != '1' ? " disabled=\"disabled\"" : "";
        $arg["data"]["NOT_PRINT_HOGOSHA_NAME"] = knjCreateCheckBox($objForm, "NOT_PRINT_HOGOSHA_NAME", "1", $extra, "");

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja171fForm1.html", $arg);
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //一覧取得
    $result = $db->query(knja171fQuery::getHrClass($model));
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //一覧リスト
    $extra = "multiple style=\"height:240px; width:180px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 20);
    //対象一覧リスト
    $extra = "multiple style=\"height:240px; width:180px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //CSV出力ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA171F");
    knjCreateHidden($objForm, "useFi_Hrclass", $model->Properties["useFi_Hrclass"]);
    knjCreateHidden($objForm, "cmd");
}
?>
