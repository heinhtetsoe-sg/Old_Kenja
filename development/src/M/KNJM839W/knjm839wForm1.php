<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knjm839wForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjm839wForm1", "POST", "knjm839windex.php", "", "knjm839wForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //ログイン年度
        $arg["data"]["CTRL_YEAR"] = $model->control["年度"];

        //年度
        $opt = array();
        $query = knjm839wQuery::getYear();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["YEAR"] = ($model->field["YEAR"] != "") ? $model->field["YEAR"] : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('knjm839w');\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        //学期
        $opt = array();
        $query = knjm839wQuery::getSemeMst($model->field["YEAR"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["GAKKI"] = ($model->field["GAKKI"]) ? $model->field["GAKKI"] : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('knjm839w');\"";
        $arg["data"]["GAKKI"] = knjCreateCombo($objForm, "GAKKI", $model->field["GAKKI"], $opt, $extra, 1);

        //テスト種別
        $opt = array();
        $query = knjm839wQuery::getTestcd($model->field["YEAR"], $model->field["GAKKI"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["TESSTCD"] = ($model->field["TESTCD"]) ? $model->field["TESTCD"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["TESTCD"] = knjCreateCombo($objForm, "TESTCD", $model->field["TESTCD"], $opt, $extra, 1);

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm839wForm1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //CSVボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ", $extra);

    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJM839W");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
}

?>
