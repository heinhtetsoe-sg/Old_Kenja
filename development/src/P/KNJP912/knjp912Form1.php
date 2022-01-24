<?php

require_once('for_php7.php');


class knjp912Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp912index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //対象の支出伝票を取得
        $extra = "";
        $query = knjp912Query::getOutgoRequestNo();
        makeCombo($objForm, $arg, $db, $query, $model->field["STATUS_REQUEST_NO"], "STATUS_REQUEST_NO", $extra, 1, "BLANK", $model);

        //返金処理日
        $model->field["HENKIN_DATE"] = ($model->field["HENKIN_DATE"]) ? $model->field["HENKIN_DATE"] : CTRL_DATE;
        $arg["data"]["HENKIN_DATE"] = View::popUpCalendar($objForm, "HENKIN_DATE",str_replace("-","/",$model->field["HENKIN_DATE"]),"");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp912Form1.html", $arg); 
    }
}
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model) {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "--全て--",
                        "value" => "all");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, &$model) {
    if ($model->getCount > 0) {
        $disabled = "disabled";
    }
    //実行
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "実 行", $extra.$disabled);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
