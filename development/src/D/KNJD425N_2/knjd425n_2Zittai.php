<?php

require_once('for_php7.php');
class knjd425n_2Zittai
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("zittai", "POST", "knjd425n_2index.php", "", "zittai");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        $arg["TITLE"] = "年度 : ".$model->exp_year."&nbsp;&nbsp;&nbsp;".$model->schregno." : ".$model->name;

        //実態＆支援のREMARK内容を取得し連想配列に保持
        $query = knjd425n_2Query::getRemarkZittaiAndSien($model);
        $result = $db->query($query);
        $remarkArray = array();
        while ($remarkRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $remarkArray[$remarkRow["SUBCLASSCD"]]["STATUS"]      = $remarkRow["STATUS"];
            $remarkArray[$remarkRow["SUBCLASSCD"]]["FUTURE_CARE"] = $remarkRow["FUTURE_CARE"];
        }

        //改めて入力項目を設定
        $query = knjd425n_2Query::getDetailRemark($model);
        $result = $db->query($query);
        $outcnt = 0;
        $wpRow = $db->getRow(knjd425n_2Query::getWindowPatternDiv($model), DB_FETCHMODE_ASSOC);
        $pattern2Flg = ($wpRow["SHEET_PATTERN"] == "2");
        $arg["PATTERN2"] = $pattern2Flg;
        if ($pattern2Flg) {

        }
/*
        $arg["data"]["TITLE1"] = $wpRow["STATUS_NAME"];
        if ($pattern2Flg) {
            $arg["data"]["TITLE2"] = $wpRow["GROWUP_NAME"];
        }
*/
        $arg["data"]["TITLE1"] = "実態";
        if ($pattern2Flg) {
            $arg["data"]["TITLE2"] = "支援";
        }

        $rows = 10;
        $cols = 90;
        $count = 1;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SUBCLASSNAME"] !== null && $row["SUBCLASSNAME"] !== "") {
                $setbuf = array();
                $setbuf["REMARKTITLE"] = $row["SUBCLASSNAME"];
                $extra = " readonly ";
                $setbuf["REMARK1"] = knjCreateTextArea($objForm, "REMARK1_0_".$outcnt, $rows, $cols, "", $extra, $remarkArray[$row["SUBCLASSCD"]]["STATUS"]);
                if ($pattern2Flg) {
                    $setbuf["REMARK2"] = knjCreateTextArea($objForm, "REMARK2_0_".$outcnt, $rows, $cols, "", $extra, $remarkArray[$row["SUBCLASSCD"]]["FUTURE_CARE"]);
                }
                $arg["list"][] = $setbuf;
                $outcnt++;
            }
            $count++;
        }
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
        View::toHTML($model, "knjd425n_2Zittai.html", $arg);
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

