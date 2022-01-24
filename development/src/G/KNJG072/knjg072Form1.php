<?php

require_once('for_php7.php');


class knjg072Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjg072Form1", "POST", "knjg072index.php", "", "knjg072Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期名
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //年組
        $query = knjg072Query::getHrClass($model);
        $extra = "onChange=\"return btn_submit('knjg072')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "BLANK");

        //生徒
        $query = knjg072Query::getSchreg($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHREGNO"], "SCHREGNO", $extra, 1, "BLANK");

        //宛名１
        $extra = "";
        $arg["data"]["SEND_TO1"] = knjCreateTextBox($objForm, $model->field["SEND_TO1"], "SEND_TO1", 40, 40, $extra);

        //宛名２
        $extra = "";
        $arg["data"]["SEND_TO2"] = knjCreateTextBox($objForm, $model->field["SEND_TO2"], "SEND_TO2", 40, 40, $extra);

        //発行日
        $setDate = str_replace("-", "/", $model->field["SEND_DATE"] ? $model->field["SEND_DATE"] : CTRL_DATE);
        $arg["data"]["SEND_DATE"]=View::popUpCalendar($objForm, "SEND_DATE", $setDate, "");

        //ボタンを作成する
        makeBtn($objForm, $arg);

        //履歴作成
        makeHist($objForm, $arg, $db);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "print") {
            $arg["printOut"] = "newwin('" . SERVLET_URL . "');";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg072Form1.html", $arg); 

    }

}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "更新／プレビュー／印刷", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//履歴作成
function makeHist(&$objForm, &$arg, $db) {
    $query = knjg072Query::getSportPrintHist();
    $result = $db->query($query);
    $i = 1;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setHist["KOUBAN"] = $i;
        $setHist["SEND_DATE"] = str_replace("-", "/", $row["SEND_DATE"]);
        $setHist["NAME"] = $row["NAME"];
        $setHist["SEND_TO1"] = $row["SEND_TO1"];
        $setHist["SEND_TO2"] = $row["SEND_TO2"];
        $arg["data2"][] = $setHist;
        $i++;
    }
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "PROGRAMID");
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "sendSchreg");
}

?>
