<?php

require_once('for_php7.php');
class knje390mSubForm1_8
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1_8", "POST", "knje390mindex.php", "", "subform1_8");

        //DB接続
        $db = Query::dbCheckOut();
        
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform1_training") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
        }
        //訓練機関情報取得
        if ($model->cmd == "subform1_training_set" || $model->cmd == "subform1_training") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery1TrainingGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //訓練機関
        $extra = "style=\"height:60px; overflow:auto;\"";
        $arg["data"]["REMARK1"] = getTextOrArea($objForm, "REMARK1", 40, 4, $Row["REMARK1"], $model);
        setInputChkHidden($objForm, "REMARK1", 40, 4, $arg);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm1_8.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //更新ボタン
    $extra = "onclick=\"return btn_submit('training1_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('subform1_training_set');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform1A');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    // knjCreateHidden($objForm, "RECORD_DIV", $Row["RECORD_DIV"]);
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $extra = "style=\"overflow-y:scroll\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2), "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg)
{
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
}

function getTextAreaComment($moji, $gyo)
{
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}
?>

