<?php

require_once('for_php7.php');

class knjoSearch
{
    function main(&$model) {

        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjoSearch", "POST", "index.php", "", "knjoSearch");

        $db = Query::dbCheckOut();

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjosearchQuery::getSchkind($model);
            $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\" onChange=\"return btn_submit('search_view');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schkind, $extra, 1);
        }

        $query = knjosearchQuery::getGradeHrClass($model);
        $result = $db->query($query);
        $row1[] = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //漢字姓名
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["KANJI"] = knjCreateTextBox($objForm, $val, "KANJI", 20, 10, $extra);

        //かな姓名
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["KANA"] = knjCreateTextBox($objForm, $val, "KANA", 20, 20, $extra);

        //実行ボタン
        $extra = "onclick=\"return search_submit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "BTN_OK", "実行", $extra);

        //閉じるボタン
        $extra = "onclick=\"closeWin(); window.opener.close()\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "BTN_END", "閉じる", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        $js = "var w;\n";
        $js .= "w = setInterval('window.focus()', 50);\n";
        $js .= "setInterval('observeDisp()', 5000);\n";
        $arg["JAVASCRIPT"] = $js;
        View::toHTML($model, "knjoSearch.html", $arg);
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

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>