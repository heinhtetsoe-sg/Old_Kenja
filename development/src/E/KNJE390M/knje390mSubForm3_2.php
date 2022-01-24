<?php

require_once('for_php7.php');
class knje390mSubForm3_2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3ConcreteSupport", "POST", "knje390mindex.php", "", "subform3ConcreteSupport");

        //DB接続
        $db = Query::dbCheckOut();

        //データがない場合は最新をセット
        if (!$model->main_year) {
            $model->main_year = CTRL_YEAR;
            if ($model->record_date != "") {
                $recordDateArr = split("-", str_replace("/", "-", $model->record_date));
                $model->main_year = $recordDateArr[0];
            }
        }

        $recordDate = $db->getRow(knje390mQuery::getRecordDate($model, "C"), DB_FETCHMODE_ASSOC);
        if (!$model->record_date) {
            $model->record_date = $recordDate["VALUE"];
        }

        //年度表示
        $arg["data"]["NENDO"] = $model->main_year.'年度';

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //項目名取得
        $label = array();
        $maxDataDiv = 0;
        $query = knje390mQuery::getChallengedSupportplanKindNameDat($model, "03");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["KIND_SEQ"] == "000") {
                $label["STATUS_NAME1"]  = $row["STATUS_NAME1"];
                $label["STATUS_NAME2"]  = $row["STATUS_NAME2"];
                $label["STATUS_NAME3"]  = $row["STATUS_NAME3"];
            } else {
                $label["KIND_NAME".$row["KIND_SEQ"]] = $row["KIND_NAME"];
            }
            
            $maxDataDiv = $row["KIND_SEQ"];
        }
        $result->free();
        knjCreateHidden($objForm, "MAX_DATA_DIV", (int)$maxDataDiv); //支援計画 具体的な支援 項目数

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform3ConcreteSupport" || $model->cmd == "subform3ConcreteSupportA" || $model->cmd == "subform3ConcreteSupport_clear") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery3ConcreteSupport($model, $maxDataDiv), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field3;
            }
        } else {
            $Row =& $model->field3;
        }
        
        //作成年月日
        KnjCreateHidden($objForm, "RECORD_DATE", $model->record_date);

        //項目名（横）
        $arg["data"]["STATUS_NAME1"]    = $label["STATUS_NAME1"];
        $arg["data"]["STATUS_NAME2"]    = $label["STATUS_NAME2"];
        $arg["data"]["STATUS_NAME3"]    = $label["STATUS_NAME3"];

        if ($maxDataDiv > 0) {
            for ($i = 1; $i <= $maxDataDiv; $i++) {
                $setTmp = array();

                //項目名（縦）
                $seq = sprintf("%03d", $i);
                $extra = "";
                $setTmp["KIND_NAME"] = $label["KIND_NAME".$seq];

                //extra
                $extra = "style=\"height:210px;\"";

                //テキスト（左）
                $moji = 15;
                $gyou = 15;
                $setTmp["STATUS"] = getTextOrArea($objForm, "DIV".$i."_STATUS", $moji, $gyou, $Row["DIV".$i."_STATUS"]);
                $setTmp["STATUS_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_STAT", "statusarea_DIV".$i."_STATUS");
                //テキスト（中）
                $moji = 12;
                $gyou = 15;
                $setTmp["STATUS2"] = getTextOrArea($objForm, "DIV".$i."_STATUS2", $moji, $gyou, $Row["DIV".$i."_STATUS2"]);
                $setTmp["STATUS2_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS2_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS2_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS2_STAT", "statusarea_DIV".$i."_STATUS2");
                //テキスト（右）
                $moji = 12;
                $gyou = 15;
                $setTmp["STATUS3"] = getTextOrArea($objForm, "DIV".$i."_STATUS3", $moji, $gyou, $Row["DIV".$i."_STATUS3"]);
                $setTmp["STATUS3_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS3_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS3_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS3_STAT", "statusarea_DIV".$i."_STATUS3");

                $setTmp["CNT"] = $i;

                $arg["data2"][] = $setTmp;
            }
        }
        
        //評価・連携の記録 項目名
        $query = knje390mQuery::getChallengedSupportplanKindNameDat($model, "04");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["KIND_SEQ"] == "000") {
                $label["RECORD_NAME"]  = $row["STATUS_NAME1"];
            }
        }
        $result->free();
        $arg["data"]["RECORD_NAME"]    = $label["RECORD_NAME"];

        //評価・連携の記録
        $arg["data"]["RECORD"] = getTextOrArea($objForm, "RECORD", 55, 15, $Row["RECORD"]);
        setInputChkHidden($objForm, "RECORD", 55, 15, $arg);

        //データをカウント
        $mainCountData = knje390mQuery::getCheckMainDataQuery($db, $model, "3");

        //ボタン作成
        makeBtn($objForm, $arg, $model, $mainCountData);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        if (get_count($model->warning)== 0 && $model->cmd !="subform1_clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="subform1_clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm3_2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData)
{
    //データがない場合は、更新、取消、戻る以外は使用不可
    if ($mainCountData == 0) {
        $disabled = "disabled";
    } else {
        $disabled = "";
    }

    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform3ConcreteSupport_updatemain');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform3ConcreteSupport_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('subform3');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
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
    $result1->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val)
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
