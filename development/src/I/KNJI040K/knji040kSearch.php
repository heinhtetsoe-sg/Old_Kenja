<?php

require_once('for_php7.php');

class knji040kSearch {
    function main(&$model) {

        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knji040kSearch", "POST", "knji040kindex.php", "", "knji040kSearch");

        //DB接続
        $db = Query::dbCheckOut();

        //卒業年度コンボ
        $query = knji040kQuery::GetYear();
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\" onchange=\"return btn_submit('search_view2');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADUATE_YEAR", $model->search_fields["graduate_year"], $extra, 1);

        //卒業時組コンボボックス
        $query = knji040kQuery::getHrclass($model, $model->search_fields["graduate_year"]);
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        makeCmb($objForm, $arg, $db, $query, "GRADUATE_CLASS", $model->search_fields["graduate_class"], $extra, 1, "BLANK");

        //漢字姓名
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["LKANJI"] = knjCreateTextBox($objForm, $model->search_fields["lkanji"], "LKANJI", 20, 10, $extra);

        //かな姓名
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["LKANA"] = knjCreateTextBox($objForm, $model->search_fields["lkana"], "LKANA", 20, 20, $extra);

        //実行ボタン
        $extra = "onclick=\"return search_submit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "BTN_OK", "実行", $extra);

        //閉じるボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "BTN_END", "閉じる", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        $js = "var w;\n";
        $js .= "w = setInterval('window.focus()', 50);\n";
        $js .= "setInterval('observeDisp()', 5000);\n";
        $arg["JAVASCRIPT"] = $js;
        View::toHTML($model, "knji040kSearch.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "   ", "value" => "");
    }
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
?>
