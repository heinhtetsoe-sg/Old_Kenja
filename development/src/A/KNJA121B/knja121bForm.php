<?php

require_once('for_php7.php');

class knja121bForm
{
    function main(&$model)
    {
        if (!$model->cmd) {
            $arg["jscript"] = "collHttps('".REQUESTROOT."', 'main')";
        }

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja121bForm", "POST", "knja121bindex.php", "", "knja121bForm");

        //hiddenを作成する
        makeHidden($objForm);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja121bForm.html", $arg);
    }
} 

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
}

?>
