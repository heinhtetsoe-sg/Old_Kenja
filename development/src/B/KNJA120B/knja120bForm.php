<?php
class knja120bForm
{
    function main(&$model)
    {
        if (!$model->cmd) {
            $arg["jscript"] = "collHttps('".REQUESTROOT."', 'main')";
        }

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja120bForm", "POST", "knja120bindex.php", "", "knja120bForm");

        //hiddenを作成する
        makeHidden($objForm);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja120bForm.html", $arg);
    }
} 

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
}

?>
