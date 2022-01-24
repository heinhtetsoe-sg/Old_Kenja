<?php
class knjd126sForm1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd126sForm1", "POST", "knjd126sindex.php", "", "knjd126sForm1");

        $opt=array();

        $extra = "onPaste=\"return show('copy2');\" ";
        $arg["data"]["TABLE_NAME"] = knjCreateTextBox($objForm, '', "TABLE_NAME", 20, 20, $extra);

        //hiddenを作成する
        makeHidden($objForm);

        makeBtn($objForm, $arg);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd126sForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "出 力", $extra);
    
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
}
?>
