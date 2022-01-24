<?php

require_once('for_php7.php');


class knjb151tForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjb151tForm1", "POST", "knjb151tindex.php", "", "knjb151tForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;
        //学期テキストボックスを作成する
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //クラスコンボ作成
        makeClassCmb($objForm, $arg, $db, $model);
        //リスト作成
        makeListData($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg, $db, $model);

        //hiddenを作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb151tForm1.html", $arg); 
    }
}
//クラスコンボ作成
function makeClassCmb(&$objForm, &$arg, $db, &$model)
{
    $query = knjb151tQuery::getHrClass(CTRL_YEAR, CTRL_SEMESTER);
    $row1 = array();
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    $model->field["GRADE_HR_CLASS"] = (!isset($model->field["GRADE_HR_CLASS"])) ? $row1[0]["value"] : $model->field["GRADE_HR_CLASS"];

    $extra = "onchange=\"return btn_submit('knjb151t');\"";
    $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);
}

//リスト作成
function makeListData(&$objForm, &$arg, $db, $model)
{
    $opt1 = array();
    $query = knjb151tQuery::getSchregno(CTRL_YEAR, CTRL_SEMESTER, $model->field["GRADE_HR_CLASS"]);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt1[] = array('label' => $row["NAME"],
                        'value' => $row["SCHREGNO"]);
    }
    $result->free();

    //生徒一覧リストを作成する
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 25);

    //対象生徒一覧リストを作成する
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", array(), $extra, 25);

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

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJB151T");
    knjCreateHidden($objForm, "TEMPLATE_PATH");
    knjCreateHidden($objForm, "selectdata");
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{

    //印刷ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");

    //実行ボタン
    if ($model->Properties["useXLS"]) {
        $model->schoolCd = $db->getOne(knjb151tQuery::getSchoolCd());
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "', 'csv');\"";
        $setBtnName = "エクセル出力";
    } else {
        $extra = "onclick=\"return btn_submit('csv');\"";
        $setBtnName = "ＣＳＶ出力";
    }
    //セキュリティーチェック
    $securityCnt = $db->getOne(knjb151tQuery::getSecurityHigh());
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $setBtnName, $extra);
    }

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
