<?php

require_once('for_php7.php');


class knjx400sForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjx400sForm1", "POST", "knjx400sindex.php", "", "knjx400sForm1");

        if ($model->existPem == "NONE") {
            $arg["jscript"] = "notPemClose();";
        }

        if (!strcmp($model->cmd, "")) {
            $arg["useApplet"] = "on";
            $arg["APP"]["RANDOM"] = $model->randm;
            $arg["APP"]["CRTEXT"] = $model->crtext;
            $arg["APP"]["APPHOST"] = '"../../..'.APPLET_ROOT.'/KNJX400S"';
        }
        if (!strcmp($model->cmd, "seqChk")) {
            $arg["useApplet"] = "on";
            $arg["APP"]["RANDOM"] = "";
            $arg["APP"]["CRTEXT"] = "";
            $arg["APP"]["APPHOST"] = '"../../..'.APPLET_ROOT.'/KNJX400S"';
        }

        if ($model->cmd == "") {
            $arg["marpColor"] = "red";
        } else {
            $arg["marpColor"] = "white";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjx400sForm1.html", $arg); 

    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

    //メニューリスト
    if (strcmp($model->gosign, "OK") == 0) {
        $callPrg = REQUESTROOT."/X/KNJX410/knjx410index.php?cmd=KNJX400S&SEND_PRGID=KNJX400S&SEND_AUTH=".AUTHORITY;
        $arg["jscript"] = "window.open('{$callPrg}','_self');";
    }
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJX400S");
    knjCreateHidden($objForm, "SIGNATURE", $model->signature);
    knjCreateHidden($objForm, "GOSIGN", $model->gosign);
    knjCreateHidden($objForm, "RANDOM", $model->randm);
    knjCreateHidden($objForm, "SEQNO", $model->seqno);
    knjCreateHidden($objForm, "CRTEXT", $model->crtext);
}

?>
