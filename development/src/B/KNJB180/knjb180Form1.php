<?php

require_once('for_php7.php');

class knjb180Form1 {
    function main($model) {
        $objForm = new form;
        //DB接続
        $db = Query::dbCheckOut();

        //ラジオボタン
        makeRadio($objForm, $arg, $model);
        //テキストボックス
        makeText($objForm, $arg, $model);
        //ボタン
        makeBtn($objForm, $arg, $model);
        //Hidden
        makeHidden($objForm, $arg, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["start"]   = $objForm->get_start("main", "POST", "knjb180index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjb180Form1.html", $arg); 
    }
}
/****************************************** 以下関数 *****************************************************/
////////////////////
//ラジオボタン作成//
////////////////////
function makeRadio(&$objForm, &$arg, $model) {
    //出力取込種別ラジオボタン（1:定時制/2:通信制）
    $opt = array(1, 2);
    if ($model->obj_radio == '5') { //一度ラジオの値を変換した為元に戻す必要がある4:定時制 5:通信制
        $model->obj_radio = '2';
    } else {
        $model->obj_radio = '1';
    }
    $extra = "";
    $radioArray = knjCreateRadio($objForm, "RADIO", $model->obj_radio, $extra, $opt, get_count($opt));
    foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;
}

////////////////////////
//テキストボックス作成//
////////////////////////
function makeText(&$objForm, &$arg, $model) {
    //年度(4)
    $extra = "";
    $arg["data"]["YEAR"] = knjCreateTextBox($objForm, $model->obj_year, "YEAR", 10, 4, $extra);

    //学校番号(5)
    $extra = "";
    $arg["data"]["SCHOOLCD"] = knjCreateTextBox($objForm, $model->obj_schoolcd, "SCHOOLCD", 10, 5, $extra);

    //課程(3)
    $extra = "";
    $arg["data"]["MAJORCD"] = knjCreateTextBox($objForm, $model->obj_majorcd, "MAJORCD", 10, 3, $extra);

    //債権種別(3)
    $extra = "";
    $arg["data"]["SAIKEN"] = knjCreateTextBox($objForm, $model->obj_saiken, "SAIKEN", 10, 3, $extra);
}

//////////////
//ボタン作成//
//////////////
function makeBtn(&$objForm, &$arg, $model) {
    //チェック情報出力ボタン
    $arg["button"]["BTN_CHECK"] = knjCreateBtn($objForm, "btn_check", "チェック情報出力", "onclick=\"return btn_submit('check');\"");

    //ＣＳＶ出力ボタン
    $arg["button"]["BTN_OUTPUT1"] = knjCreateBtn($objForm, "btn_output", "単位/科目情報出力", "onclick=\"return btn_submit('output1');\"");

    //ＣＳＶ出力ボタン
    $arg["button"]["BTN_OUTPUT2"] = knjCreateBtn($objForm, "btn_output", "生徒情報出力", "onclick=\"return btn_submit('output2');\"");

    //終了ボタン
    $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_cancel", "終  了", "onclick=\"closeWin();\"");
}

//////////////
//Hidden作成//
//////////////
function makeHidden(&$objForm, &$arg, $model) {
    //hidden
    knjCreateHidden($objForm, "cmd");
}
?>
