<?php

require_once('for_php7.php');

class knjxSearch_Student
{
    function main(&$model) {

        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxSearch_Student", "POST", "index.php", "", "knjxSearch_Student");

        //DB接続
        $db = Query::dbCheckOut();

        foreach ($model->searchItem as $key => $val) {
            $arg[$key] = $val;
        }

        //学籍番号
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["SCHREGNO"] = knjCreateTextBox($objForm, "", "SCHREGNO", 8, 8, $extra);

        //年組コンボ
        $query = knjxSearch_StudentQuery::getRegdHdat();
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "BLANK");

        //出席番号
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["ATTENDNO"] = knjCreateTextBox($objForm, "", "ATTENDNO", 3, 3, $extra);

        //課程学科コンボ
        $query = knjxSearch_StudentQuery::getCourseMajor();
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSEMAJOR"], "COURSEMAJOR", $extra, 1, "BLANK");

        //コースコンボ
        $query = knjxSearch_StudentQuery::getCourseCode();
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSECODE"], "COURSECODE", $extra, 1, "BLANK");

        //氏名
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, "", "NAME", 20, 10, $extra);

        //氏名表示
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["NAMESHOW"] = knjCreateTextBox($objForm, "", "NAMESHOW", 20, 10, $extra);

        //かな
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
        foreach ($model->searchItem as $key => $val) {
            if (!$val) {
                //hidden
                knjCreateHidden($objForm, $key);
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        $js = "var w;\n";
        $js .= "w = setInterval('window.focus()', 50);\n";
        $js .= "setInterval('observeDisp()', 5000);\n";
        $arg["JAVASCRIPT"] = $js;

        View::toHTML($model, "knjxSearch_Student.html", $arg);
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
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>