<?php

require_once('for_php7.php');

class knjl211cForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl211cForm1", "POST", "knjl211cindex.php", "", "knjl211cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl211cQuery::getApctDiv($model));

        //プレテスト区分
        $query = knjl211cQuery::getPreTestdiv($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PRE_TESTDIV", $model->pre_testdiv, $extra, 1, "");

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
        View::toHTML($model, "knjl211cForm1.html", $arg); 
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $opt = array();
    $opt[]= array('label' => '1 受験番号',       'value' => 'PRE_RECEPTNO');
    $opt[]= array('label' => '2 受付日付',       'value' => 'PRE_RECEPTDATE');
    $opt[]= array('label' => '3 受験型',         'value' => 'PRE_EXAM_TYPE');
    $opt[]= array('label' => '4 塾',             'value' => 'PS_CD');
    //バス情報表示
    if ($model->Properties["Pretest_bus_Not_Hyouji"] != "1") {
        $opt[]= array('label' => '5 五条駅',         'value' => 'STATIONDIV3');
        $opt[]= array('label' => '6 林間田園都市駅', 'value' => 'STATIONDIV1');
        $opt[]= array('label' => '7 福神駅',         'value' => 'STATIONDIV2');
    }

    $opt_mst = array('PRE_RECEPTNO'   => '1 受験番号',
                     'PRE_RECEPTDATE' => '2 受付日付',
                     'PRE_EXAM_TYPE'  => '3 受験型',
                     'PS_CD'          => '4 塾');
    //バス情報表示
    if ($model->Properties["Pretest_bus_Not_Hyouji"] != "1") {
        $opt_mst[]= array('STATIONDIV3' => '5 五条駅');
        $opt_mst[]= array('STATIONDIV1' => '6 林間田園都市駅');
        $opt_mst[]= array('STATIONDIV2' => '7 福神駅');
    }

    //項目一覧作成
    $opt_right = array();
    foreach ($opt as $val) {
        if (in_array($val['value'], $model->field["SORT_SELECTED_HIDDEN"])) {
            continue;
        }
        $opt_right[] = array('label' => $val['label'],
                             'value' => $val['value']);
    }

    $extra = "multiple style=\"width:150px\" width:\"150px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", isset($opt_right) ? $opt_right : array(), $extra, 10);

    //出力対象一覧作成
    $opt_left = array();
    foreach ($model->field["SORT_SELECTED_HIDDEN"] as $val) {
        $opt_left[] = array('label' => $opt_mst[$val],
                            'value' => $val);
    }

    $extra = "multiple style=\"width:150px\" width:\"150px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", isset($opt_left) ? $opt_left : array(), $extra, 10);

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
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $value_flg = false;
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //ＣＳＶ出力
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJL211C");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "Pretest_bus_Not_Hyouji", $model->Properties["Pretest_bus_Not_Hyouji"]);
}
?>
