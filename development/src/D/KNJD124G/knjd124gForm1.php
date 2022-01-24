<?php

require_once('for_php7.php');


//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd124gForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjd124gindex.php", "", "main");

        //プログラム呼び出し（権限とプログラムIDのみ渡す）
        if ($model->cmd == 'main') {
            $link = REQUESTROOT."/D/KNJD124F/knjd124findex.php?cmd=main&SEND_PRGID=KNJD124G&SEND_AUTH=".$model->auth;
            $arg["reload"] = " Page_jumper('{$link}') ";
        }

        /* hidden作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        
        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjd124gForm1.html", $arg);
    }
}


?>
