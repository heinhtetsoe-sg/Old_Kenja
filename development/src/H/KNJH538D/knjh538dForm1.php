<?php

require_once('for_php7.php');


//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjh538dForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjh538dindex.php", "", "main");

        //プログラム呼び出し（権限とプログラムIDのみ渡す）
        if ($model->cmd == 'main') {
            $link = REQUESTROOT."/H/KNJH538A/knjh538aindex.php?cmd=main&SEND_PRGID=KNJH538D&SEND_AUTH=".$model->auth;
            $arg["reload"] = " Page_jumper('{$link}') ";
        }

        /* hidden作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        
        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjh538dForm1.html", $arg);
    }
}


?>
