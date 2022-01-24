<?php

require_once('for_php7.php');


class knjx240Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjx240Form1", "POST", "knjx240index.php", "", "knjx240Form1");

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $semester);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx240Form1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //指導要録(通年)
    $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA120X/knja120xindex.php?";
    $extra .= "&cmd=";
    $extra .= "&AUTH=".$model->auth;
    $extra .= "&CALLID=KNJX240";
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["SIDOU_YOUROKU_TUNEN"] = knjCreateBtn($objForm, "SIDOU_YOUROKU_TUNEN", "指導要録(通年)", $extra);

    //指導要録(年度)
    $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJA120AX/knja120axindex.php?";
    $extra .= "&cmd=";
    $extra .= "&AUTH=".$model->auth;
    $extra .= "&CALLID=KNJX240";
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["SIDOU_YOUROKU_NENDO"] = knjCreateBtn($objForm, "SIDOU_YOUROKU_NENDO", "指導要録(年度)", $extra);

    //調査書(進学用)
    $extra  = " onClick=\" wopen('".REQUESTROOT."/E/KNJE011X/knje011xindex.php?";
    $extra .= "&cmd=";
    $extra .= "&AUTH=".$model->auth;
    $extra .= "&CALLID=KNJX240";
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["TYOUSASYO_SIN"] = knjCreateBtn($objForm, "TYOUSASYO_SIN", "調査書(進学用)", $extra);

    //調査書(就職用)
    $extra  = " onClick=\" wopen('".REQUESTROOT."/E/KNJE020X/knje020xindex.php?";
    $extra .= "&cmd=";
    $extra .= "&AUTH=".$model->auth;
    $extra .= "&CALLID=KNJX240";
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["data"]["TYOUSASYO_SYU"] = knjCreateBtn($objForm, "TYOUSASYO_SYU", "調査書(就職用)", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $semester){

    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJX240");
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}
?>
