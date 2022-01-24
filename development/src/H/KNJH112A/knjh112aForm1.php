<?php

require_once('for_php7.php');

class knjh112aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh112aForm1", "POST", "knjh112aindex.php", "", "knjh112aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //取得資格期間(開始)
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", str_replace('-', '/', CTRL_DATE));
        //取得資格期間(終了)
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", str_replace('-', '/', CTRL_DATE));

        //checkbox
        $extra = "id=\"SEITO_BETU\"". $model->field["SEITO_BETU"] == "1" ? " checked " : "";
        $arg["data"]["SEITO_BETU"] = knjCreateCheckBox($objForm, "SEITO_BETU", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh112aForm1.html", $arg);
    }
}
/*********************************************** 以下関数 *******************************************************/
/******************/
/* リストToリスト */
/******************/
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjh112aQuery::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

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

/**********/
/* ボタン */
/**********/
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //CSV出力
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタン
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

/**********/
/* hidden */
/**********/
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE",     CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
    knjCreateHidden($objForm, "PRGID",         "KNJH112A");
    knjCreateHidden($objForm, "STAFFCD",       STAFFCD);
    knjCreateHidden($objForm, "CHK_SDATE", (CTRL_YEAR)."/04/01");
    knjCreateHidden($objForm, "CHK_EDATE", (CTRL_YEAR+1)."/03/31");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "selectdata");
}
?>
