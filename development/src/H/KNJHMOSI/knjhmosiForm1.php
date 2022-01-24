<?php

require_once('for_php7.php');

class knjhmosiform1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjhmosiindex.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //学籍基礎マスタより名前を取得
        $nameArray = $db->getRow(knjhmosiQuery::getName($model), DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $model->year."年度　".$nameArray["HR_NAME"]."　".$nameArray["ATTENDNO"]."番　氏名：".$nameArray["NAME"];

        //checkbox
        $extra = "id=\"ANIME\" ";
        $arg["ANIME"] = knjCreateCheckBox($objForm, "ANIME", "1", $extra);

        $arg["HOGE_DISP"] = "style=\"display: block\"";
        $arg["LINE_DISP"] = "style=\"display: none\"";
        $arg["BEZI_DISP"] = "style=\"display: none\"";
        $arg["BAR_DISP"] = "style=\"display: none\"";
        $arg["RADAR_DISP"] = "style=\"display: none\"";

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model, $testAry["SCORE"], $testAry["DEVIATION"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTMLChart($model, "knjhmosiForm1.html", $arg);
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //チャート
    $extra = "onclick=\" return createChart('line');\"";
    $arg["LINE"] = knjCreateBtn($objForm, "LINE", "折れ線", $extra);
    //チャート
    $extra = "onclick=\" return createChart('bezi2');\"";
    $arg["BEZI"] = knjCreateBtn($objForm, "BEZI", "折れ線(ベジェ)", $extra);
    //チャート
    $extra = "onclick=\" return createChart('bar');\"";
    $arg["BAR"] = knjCreateBtn($objForm, "BAR", "棒", $extra);

    //チャート
    $extra = "onclick=\" return createRadarChart('line');\"";
    $arg["LINE2"] = knjCreateBtn($objForm, "LINE2", "折れ線", $extra);
    //チャート
    $extra = "onclick=\" return createRadarChart('bezi');\"";
    $arg["BEZI2"] = knjCreateBtn($objForm, "BEZI2", "折れ線(ベジェ)", $extra);
    //チャート
    $extra = "onclick=\" return createRadarChart('bar');\"";
    $arg["BAR2"] = knjCreateBtn($objForm, "BAR2", "棒", $extra);
    //チャート
    $extra = "onclick=\" return createRadarChart('radar');\"";
    $arg["RADAR2"] = knjCreateBtn($objForm, "RADAR2", "レーダー", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["BTN_END"] = knjCreateBtn($objForm, "BTN_END", "戻 る", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model, $score, $deviation)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

    knjCreateHidden($objForm, "hogeLabel", "2007,2008,2009,2010,2011,2012,2013");
    knjCreateHidden($objForm, "hogeval0", "60,55,77,68,78");
    knjCreateHidden($objForm, "hogeval1", "70,30,80,50,46");
    knjCreateHidden($objForm, "hogeval2", "10,30,50,70,80");
    knjCreateHidden($objForm, "hogeCnt", 3);
    knjCreateHidden($objForm, "hogeHanrei", "国語,数学,社会");

    knjCreateHidden($objForm, "cccVal0", "年度,2007,2008,2009,2010,2011,2012,2013");
    knjCreateHidden($objForm, "cccVal1", "国語,60,55,77,68,78");
    knjCreateHidden($objForm, "cccVal2", "数学,70,30,80,50,46");
    knjCreateHidden($objForm, "cccVal3", "社会,10,30,50,70,80");
    knjCreateHidden($objForm, "cccCnt", 4);

}

?>
