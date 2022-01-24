<?php

require_once('for_php7.php');

class knja172Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja172Form1", "POST", "knja172index.php", "", "knja172Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //出力区分選択 (1:クラス別 2:都道府県別 3:市区町村別)
        $model->field["PRINT_RADIO"] = "1";
        knjCreateHidden($objForm, "PRINT_RADIO", $model->field["PRINT_RADIO"]);

        //クラス方式選択 (1:法定クラス 2:複式クラス)
        $opt = array(1, 2);
        $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
        $click = ($model->field["PRINT_RADIO"] == "1") ? " onClick=\"return btn_submit('knja172');\"" : "";
        $extra = array("id=\"HR_CLASS_TYPE1\"".$click, "id=\"HR_CLASS_TYPE2\"".$click);
        $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["PRINT_RADIO"] == "1" || $model->field["PRINT_RADIO"] == "2") {
            //学年コンボ表示
            $arg["grade"] = '1';

            //学年コンボ作成
            $query = knja172Query::getGrade($model);
            $extra = ($model->field["PRINT_RADIO"] == "1") ? "onchange=\"return btn_submit('knja172'), AllClearList();\"" : "";
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        }

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
        View::toHTML($model, "knja172Form1.html", $arg);
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    $list = array();
    $list["1"] = array('query' => knja172Query::getHrClass($model), 'label' => 'クラス');
    $list["2"] = array('query' => knja172Query::getPref($model),    'label' => '都道府県');
    $list["3"] = array('query' => knja172Query::getCity($model),    'label' => '市区町村');

    //一覧取得
    $result = $db->query($list[$model->field["PRINT_RADIO"]]["query"]);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //一覧リスト
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 20);
    //対象一覧リスト
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);
    //リストの項目名
    $arg["data"]["LIST_LABEL"] = $list[$model->field["PRINT_RADIO"]]["label"];

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
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
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
    knjCreateHidden($objForm, "PRGID", "KNJA172");
    knjCreateHidden($objForm, "cmd");
}
?>
