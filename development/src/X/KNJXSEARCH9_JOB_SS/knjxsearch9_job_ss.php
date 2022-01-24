<?php

require_once('for_php7.php');

class knjxsearch9_job_ss
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjxsearch9_job_ss", "POST", "index.php", "", "knjxsearch9_job_ss");

        //会社名
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["COMPANY_NAME"] = knjCreateTextBox($objForm, "", "COMPANY_NAME", 52, 80, $extra);

        //就業場所
        $extra = "onFocus=\"clearInterval(w)\" onBlur=\"w=setInterval('window.focus()',50);\"";
        $arg["data"]["SHUSHOKU_ADDR"] = knjCreateTextBox($objForm, "", "SHUSHOKU_ADDR", 52, 80, $extra);

        //実行ボタン
        $extra = "onclick=\"return search_submit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "BTN_OK", "実行", $extra);

        //閉じるボタン
        $extra = "onclick=\"closeWin(); window.opener.close()\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "BTN_END", "閉じる", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"] = $objForm->get_finish();

        $js  = "var w;\n";
        $js .= "w = setInterval('window.focus()', 50);\n";
        $js .= "setInterval('observeDisp()', 5000);\n";
        $arg["JAVASCRIPT"] = $js;
        View::toHTML($model, "knjxsearch9_job_ss.html", $arg);
    }
}
