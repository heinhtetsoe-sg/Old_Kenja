<?php

require_once('for_php7.php');

class knjb213Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb213Form1", "POST", "knjb213index.php", "", "knjb213Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $opt = array();
        $opt[] = array('label' => CTRL_YEAR, 'value' => CTRL_YEAR);
        $opt[] = array('label' => CTRL_YEAR+1, 'value' => CTRL_YEAR+1);
        $model->field["YEAR"] = $model->field["YEAR"] ? $model->field["YEAR"] : CTRL_YEAR;
        $extra = "onchange=\"return btn_submit('knjb213');\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        //学期コンボ
        $query = knjb213Query::getJobMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "JOBCD", $model->field["JOBCD"], $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb213Form1.html", $arg);
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
    knjCreateHidden($objForm, "PRGID", "KNJB213");
    knjCreateHidden($objForm, "cmd");
    list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
    knjCreateHidden($objForm, "simo", $simo);
    knjCreateHidden($objForm, "fuseji", $fuseji);
}
?>
