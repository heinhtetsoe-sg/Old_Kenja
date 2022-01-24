<?php

require_once('for_php7.php');

class knja430sForm
{
    function main(&$model)
    {
        if (!$model->cmd) {
            $arg["jscript"] = "collHttps('".REQUESTROOT."', 'main')";
        }

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja430sForm", "POST", "knja430sindex.php", "", "knja430sForm");

        //hiddenを作成する
        makeHidden($objForm);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja430sForm.html", $arg);
    }
} 

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
}

?>
