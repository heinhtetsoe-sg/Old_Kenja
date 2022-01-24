<?php

require_once('for_php7.php');

class knje360aSubform2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knje360aindex.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //対象データ存在チェック
        if (!$model->replaceItem) {
            $arg["jscript"] = "checkDataExist();";
        }

        //項目名表示
        $arg["data"]["LABEL"] = $model->replaceItemLabel;

        //変更データコンボ
        $namecd1 = ($model->replaceItem == "DECISION") ? "E005" : "E006";
        $query = knje360aQuery::getNameMst($namecd1);
        $extra = "";
        $arg["data"]["REP_VALUE"] = makeCmb($objForm, $arg, $db, $query, $model->replaceValue, "REP_VALUE", $extra, 1, $model, "BLANK");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "GET_ITEM", $model->replaceItem);
        knjCreateHidden($objForm, "GET_SEQ", $model->seq_list);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360aSubform2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $model, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array ("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //反映ボタン
    $extra = "onclick=\"return btn_submit()\"";
    $arg["button"]["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "反 映", $extra);

    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
?>
