<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knjm443wForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjm443wForm1", "POST", "knjm443windex.php", "", "knjm443wForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //ログイン年度
        $arg["data"]["CTRL_YEAR"] = $model->control["年度"];

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $query = knjm443wQuery::getSemeMst(CTRL_YEAR);
        $extra = "onchange=\"return btn_submit('knjm443w');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //テスト種別
        $model->field["TESTCD"] = ($model->field["TESTCD"] == "") ? "" : $model->field["TESTCD"];
        $query = knjm443wQuery::getTestcd(CTRL_YEAR, $model->field["SEMESTER"]);
        $extra = "onchange=\"return btn_submit('knjm443w');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], $extra, 1);

        //回数テキスト
        $arg["data"]["TEST_COUNT"] = knjCreateTextBox($objForm, $model->field["TEST_COUNT"], "TEST_COUNT", 2, 2, $extra);

        //科目コンボ
        $model->field["SUBCLASS"] = ($model->field["SUBCLASS"] == "") ? "" : $model->field["SUBCLASS"];
        $query = knjm443wQuery::getSubclassList($model);
        $extra = "onchange=\"return btn_submit('knjm443w');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASS", $model->field["SUBCLASS"], $extra, 1);

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm443wForm1.html", $arg); 
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "不合格通知票印刷", $extra);

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJM443W");
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "USE_CURRICULUMCD", $model->Properties["useCurriculumcd"]);
}

?>
