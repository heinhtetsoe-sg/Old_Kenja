<?php

require_once('for_php7.php');

class knjb170Form1 {
    function main($model) {
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjb170index.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjb170Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //ラジオボタン
        makeRadio($objForm, $arg, $model);
        //テキストボックス
        makeText($objForm, $arg, $model);
        //ボタン
        makeBtn($objForm, $arg, $db, $model);
        //Hidden
        makeHidden($objForm, $arg, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjb170Form1.html", $arg); 
    }
}
/****************************************** 以下関数 *****************************************************/
////////////////////
//ラジオボタン作成//
////////////////////
function makeRadio(&$objForm, &$arg, $model) {
    //出力取込種別ラジオボタン（1:単位情報/2:生徒情報）
    $opt = array(1, 2);
    $model->obj_radio = ($model->obj_radio) ? $model->obj_radio : "1";
    $extra = array("id=\"RADIO1\" onclick=\"txt_disable(this);\"", "id=\"RADIO2\" onclick=\"txt_disable(this);\"");
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
function makeBtn(&$objForm, &$arg, $db, $model) {
    //チェック情報出力ボタン
    $arg["button"]["BTN_CHECK"] = knjCreateBtn($objForm, "btn_check", "チェック情報出力", "onclick=\"return btn_submit('check');\"");

    //実行ボタン
    if ($model->Properties["useXLS"]) {
        $model->schoolCd = $db->getOne(knjb170Query::getSchoolCd());
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
        $printDisp = "エクセル出力";
        $arg["data"]["TITLE"] = "エクセル";
    } else {
        $extra = "onclick=\"return btn_submit('output');\"";
        $printDisp = "ＣＳＶ出力";
        $arg["data"]["TITLE"] = "ＣＳＶ";
    }
    $arg["button"]["BTN_OUTPUT"] = knjCreateBtn($objForm, "btn_exec", $printDisp, $extra);

    //終了ボタン
    $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_cancel", "終  了", "onclick=\"closeWin();\"");
}

//////////////
//Hidden作成//
//////////////
function makeHidden(&$objForm, &$arg, $model) {
    //hidden
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJB170");
    knjCreateHidden($objForm, "TEMPLATE_PATH");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}
?>
