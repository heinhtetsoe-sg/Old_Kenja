<?php

require_once('for_php7.php');

class knjxSearch7
{
    function main(&$model){

        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxSearch7", "POST", "index.php", "", "knjxSearch7");

        //DB接続
        $db = Query::dbCheckOut();

        //卒業年度コンボ
        $result = $db->query(knjxsearch7Query::GetYear($model->control["年度"],$model->control["学期"], $model));
        $opt[] = array("label" => "　　　　", "value" => "");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["GRADUATE_YEAR"],
                           "value" => $row["GRADUATE_YEAR"]);
        }
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\" onchange=\"return btn_submit('search_view2');\"";
        $arg["data"]["GRADUATE_YEAR"] = knjCreateCombo($objForm, "GRADUATE_YEAR", $model->search_fields["graduate_year"], $opt, $extra, 1);

        //最終（現）組コンボボックス
        $query = knjxsearch7Query::getHrclass($model->search_fields["graduate_year"], $model);
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        makeCmb($objForm, $arg, $db, $query, "GRADUATE_CLASS", $model->search_fields["graduate_class"], $extra, 1);

        //漢字姓名
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["KANJI"] = knjCreateTextBox($objForm, "", "KANJI", 20, 10, $extra);

        //かな姓名
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["KANA"] = knjCreateTextBox($objForm, "", "KANA", 20, 20, $extra);

        //実行ボタン
        $extra = "onclick=\"return search_submit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "BTN_OK", "実行", $extra);

        //閉じるボタン
        $extra = "onclick=\"closeWin(); window.opener.close()\"";
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
        View::toHTML($model, "knjxSearch7.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array("label" => "   ", "value" => "");
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