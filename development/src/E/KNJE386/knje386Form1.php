<?php

require_once('for_php7.php');

class knje386Form1 {
    function main(&$model) {

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //タイトルの表示
        $arg["data"]["TITLE"] = "総学力点算出";

        //リストTOリストを作成
        makeListToList($objForm, $arg, $model);

        //学年コンボ
        $query = knje386Query::getGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //TOEFL基準日
        $arg["data"]["TEST_DATE"] = View::popUpCalendar($objForm, "TEST_DATE", $model->field["TEST_DATE"]);

        //ボタン作成
        //実行ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "実 行", $extra);

        if ($model->parentPrgId) {
            //戻るボタン
            $link = REQUESTROOT."/E/KNJE387/knje387index.php";
            $extra = "onclick=\"document.location.href='$link'\"";
            $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
        } else {
            //終了ボタン
            $extra = "onclick=\"closeWin();\"";
            $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        }

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJE386");
        knjCreateHidden($objForm, "CATEGORY_SELECTED_DATA");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knje386index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje386Form1.html", $arg);
    }
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

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//リストToリスト
function makeListToList(&$objForm, &$arg, $model) {

    $scoreNameList = array(
        "CLASS_SCORE"      => "教科点",
        "ABILITY_SCORE"    => "学力点",
        "TOEFL_SCORE"      => "TOEFL",
        "QUALIFIED_SCORE"  => "資格点",
        "ADJUSTMENT_SCORE" => "加減点",
    );

    //選択した得点一覧を設定(左)
    $leftList = array();
    if ($model->selected[0]) {
        foreach ($model->selected as $leftValue) {
            $leftList[]  = array('label' => $scoreNameList[$leftValue], 'value' => $leftValue);
            unset($scoreNameList[$leftValue]);
        }
    }

    //未選択の得点一覧を設定(右)
    $rightList = array();
    foreach ($scoreNameList as $key => $scoreName) {
        $rightList[] = array('label' => $scoreName, 'value' => $key);
    }

    //得点一覧作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

    //選択した得点一覧作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
?>
