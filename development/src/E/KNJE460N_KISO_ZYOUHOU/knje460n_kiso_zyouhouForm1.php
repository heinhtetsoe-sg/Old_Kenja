<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje460n_kiso_zyouhouForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje460n_kiso_zyouhouindex.php", "", "subform1");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        //年度の設定
        $model->field2["YEAR"] = ($model->cmd == "edit") ? $model->field2["YEAR"] : $model->exp_year;

        //学籍番号・生徒氏名表示
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;
        
        //タイトル
        $arg["data"]["TITLE"] = "具体的な支援、連携の記録画面";

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform1" || $model->cmd == "edit"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje460n_kiso_zyouhouQuery::getMainQuery($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field2;
            }
        } else {
            $Row =& $model->field2;
        }

        //更新年度コンボ
        $extra = "onChange=\"return btn_submit('edit');\"";
        $query = knje460n_kiso_zyouhouQuery::getYearCmb($model);
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field2["YEAR"], $extra, 1);

        //記入者
        if($Row["ENTRANT_NAME"] == ""){
            $Row["ENTRANT_NAME"] = $db->getOne(knje460n_kiso_zyouhouQuery::getStaffName($model));
        }
        $arg["data"]["ENTRANT_NAME"] = $Row["ENTRANT_NAME"];

        for ($cntidx = 1;$cntidx <= 6;$cntidx++) {
            for ($subidx = 1;$subidx <=3;$subidx++) {
                $idxStr = "ZYOUHOU".$cntidx."_".$subidx;
                $arg["data"][$idxStr] = getTextOrArea($objForm, $idxStr, $model->kiso_zyouhou_moji[$subidx], $model->kiso_zyouhou_gyou[$subidx], $Row[$idxStr], $model);
                setInputChkHidden($objForm, $idxStr, $model->kiso_zyouhou_moji[$subidx], $model->kiso_zyouhou_gyou[$subidx], $arg);
            }
        }

        $idxStr = "RENKEI_ZYOUHOU";
        $arg["data"][$idxStr] = getTextOrArea($objForm, $idxStr, $model->renkei_zyouhou_moji, $model->renkei_zyouhou_gyou, $Row[$idxStr], $model);
        setInputChkHidden($objForm, $idxStr, $model->renkei_zyouhou_moji, $model->renkei_zyouhou_gyou, $arg);

        Query::dbCheckIn($db);

        //更新ボタンを作成
        $extra = "onclick=\"return btn_submit('subform1_update');\"";
        $arg["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更新", $extra);
        
        //戻るボタンを作成する
        $link = REQUESTROOT."/E/KNJE390N/knje390nindex.php?cmd=subform3&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        //hidden
        $nx = 1;
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_COLCNT", $outcnt);
        knjCreateHidden($objForm, "HID_ROWCNT", "1");
        knjCreateHidden($objForm, "SELECT_COUNT", "6");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje460n_kiso_zyouhouForm1.html", $arg);
    }
}


//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px; overflow:auto;\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg) {
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    // KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    // KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    // KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
}
function getTextAreaComment($moji, $gyo) {
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}
?>

