<?php

require_once('for_php7.php');
class knje390mSubForm1_3
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1_3", "POST", "knje390mindex.php", "", "subform1_3");

        //DB接続
        $db = Query::dbCheckOut();
        
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform1_medical") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
        }
        //医療情報取得
        if ($model->cmd == "subform1_medical_set") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery1MedicalGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        
        // //主治医、かかりつけ医、入院・既往歴の切換
        $Row["RECORD_DIV"] = ($Row["RECORD_DIV"] == "") ? "1" : $Row["RECORD_DIV"];

        //診療科名
        $query = knje390mQuery::getNamecdComboName();
        makeCmb($objForm, $arg, $db, $query, "NAMECD", $Row["NAMECD"], "", 1, 1);
        
        //医療機関名
        $extra = "";
        $arg["data"]["CENTER_NAME"] = getTextOrArea($objForm, "CENTER_NAME", 30, 1, $Row["CENTER_NAME"]);
        $arg["data"]["CENTER_NAME_SIZE"] = '<font size="2" color="red">(全角30文字まで)</font>';

        //通院の状況・特記事項
        $arg["data"]["ATTEND_STATUS"] = getTextOrArea($objForm, "ATTEND_STATUS", 10, 3, $Row["ATTEND_STATUS"]);
        $arg["data"]["ATTEND_STATUS_SIZE"] = '<font size="2" color="red">(全角10文字X3行まで)</font>';

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm1_3.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model)
{
    $retCnt = 0;
    $countdiv1 = 1;
    $countdiv2 = 1;
    $countdiv3 = 1;
    $query = knje390mQuery::getSubQuery1MedicalRecordList($model);
    $result = $db->query($query);
    $namecdName = "";
    $centercdName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($rowlist["RECORD_DIV"] === '1') {
            $rowlist["RECORD_DIV_NAME"] = '主治医'.$countdiv1;
            $countdiv1++;
        } elseif ($rowlist["RECORD_DIV"] === '2') {
            $rowlist["RECORD_DIV_NAME"] = 'かかりつけ医'.$countdiv2;
            $countdiv2++;
        } elseif ($rowlist["RECORD_DIV"] === '3') {
            $rowlist["RECORD_DIV_NAME"] = '入院・既往歴'.$countdiv3;
            $countdiv3++;
        }
        $namecdName = $db->getOne(knje390mQuery::getNamecdName($rowlist["NAMECD"]));
        $rowlist["CONTENTS_NAME1"] = $namecdName;
        $rowlist["CONTENTS_NAIYOU"] = $rowlist["CENTER_NAME"];

        $rowlist["SELECT_LINE_COLOR"] = ($rowlist["SCHREGNO"] == $model->schregno && $rowlist["RECORD_DIV"] == $model->getRecordDiv && $rowlist["RECORD_SEQ"] == $model->getRecordSeq) ? "#ccffcc" : "#ffffff";

        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
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
    //医療機関参照
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=medical_center&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_center"] = knjCreateBtn($objForm, "btn_center", "主要医療機関参照", $extra);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('medical1_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('medical1_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('medical1_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform1A');\"");
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $extra = "style=\"overflow-y:scroll;\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2), "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}
//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "RECORD_DIV", $Row["RECORD_DIV"]);
}
?>

