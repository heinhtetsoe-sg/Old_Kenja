<?php

require_once('for_php7.php');


class knja122s1Form1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja122s1Form1", "POST", "knja122s1index.php", "", "knja122s1Form1");

        if ($model->existPem == "NONE") {
            $arg["jscript"] = "notPemClose();";
        }
echo "cmd = ".$model->cmd."<hr>";
        if (!$model->cmd) {
            $arg["jscript"] = "collHttps('".REQUESTROOT."', 'https')";
            $model->getPem();
        }
        if ($model->cmd == "sslApplet") {
            $arg["useApplet"] = "on";
        }

        //パスワード
        $arg["data"]["PASSWD"] = knjCreatePassword($objForm, $model->passwd, "PASSWD", 40, 40, "");
        if ($model->cmd != "sslExe") {
            $arg["APP"]["PASS"]  = $model->cmd == "sslApplet" ? $model->passwd : "";
            $arg["APP"]["RANDM"] = $model->randm;
            $arg["APP"]["STAFF"] = STAFFCD;
            $arg["APP"]["SENDURL"] = $model->setUrl;
            $dbPas = str_replace("https://", "", $model->setUrl);
            $addrLen = strpos($dbPas, "/");
            $dbPas = substr($dbPas, 0, $addrLen);
            $arg["APP"]["DBNAME"] = "//".$dbPas."/".DB_DATABASE;
        }

        if ($model->cmd == "sslApplet") {
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
        View::toHTML($model, "knja122s1Form1.html", $arg); 

    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //所見処理
    if ($model->syomeiBtn) {
        $extra = "onclick=\"return btn_submit('knja122s12');\"";

        $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA120S/knja120sindex.php?EXE_TYPE=PRINCIPAL&setUrl=".$model->setUrl."'";
        $extra .= ",'SUBWIN',0,0,screen.availWidth,screen.availHeight);\"";

        $arg["button"]["btn_syoken"] = knjCreateBtn($objForm, "btn_syoken", "所見署名", $extra);
    }
    //認証
    $extra = "onclick=\"return btn_signSubmit('sslApplet', '".$model->randm."', '".STAFFCD."');\"";
    $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "認 証", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"deleteCookie(); closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJA122S1");
}

?>
