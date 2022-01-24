<?php

require_once('for_php7.php');

class knjd425n_2Ziritu
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("ziritu", "POST", "knjd425n_2index.php", "", "ziritu");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        $arg["TITLE"] = "年度 : ".$model->exp_year."&nbsp;&nbsp;&nbsp;".$model->schregno." : ".$model->name;

        $query = knjd425n_2Query::getDetailRemark($model);
        $result = $db->query($query);
        $outcnt = 0;

        $arg["data"]["TITLE"] = "目指したい自立の姿";
        $rows = 10;
        $cols = 90;
        $query = knjd425n_2Query::getRemarkZiritu($model);
        $zirituRemark = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $extra = " readonly ";
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", 10, 90, "", $extra, $zirituRemark["SELFRELIANCE_GOAL"]);
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd425n_2Ziritu.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "GET_CMD", $model->cmd);
    knjCreateHidden($objForm, "TEXTBOX", $model->textBox);
}
?>

