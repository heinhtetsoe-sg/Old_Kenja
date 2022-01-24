<?php

require_once('for_php7.php');


class knja430sSearch
{
    function main(&$model){

        $objForm = new form;
        $arg = array();
        $arg["start"]   = $objForm->get_start("knja430sSearch", "POST", "knja430sindex.php", "", "knja430sSearch");

        //職員番号
        $extra = "";
        $arg["data"]["SEARCH_CODE"] = knjCreateTextBox($objForm, "", "SEARCH_CODE", 10, 8, $extra);

        //職員氏名
        $extra = "style=\"width:100%;\"";
        $arg["data"]["SEARCH_NAME"] = knjCreateTextBox($objForm, "", "SEARCH_NAME", 20, 10, $extra);

        //職員氏名かな
        $extra = "style=\"width:100%;\"";
        $arg["data"]["SEARCH_KANA"] = knjCreateTextBox($objForm, "", "SEARCH_KANA", 20, 10, $extra);

        //実行
        $extra = "onclick=\"return search_submit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "BTN_OK", "実 行", $extra);

        //閉じる
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "BTN_END", "閉じる", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja430sSearch.html", $arg);
    }
}
?>