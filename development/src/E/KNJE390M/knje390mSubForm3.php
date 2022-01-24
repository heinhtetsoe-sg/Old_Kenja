<?php

require_once('for_php7.php');
class knje390mSubForm3
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3", "POST", "knje390mindex.php", "", "subform3");

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

        if ($model->cmd == "subform3_change") {
            $model->record_date = str_replace("/", "-", $model->field3["RECORD_HISTORY"]);
        }

        //年度表示
        $arg["data"]["NENDO"] = $model->main_year.'年度';

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform3" || $model->cmd == "subform3A" || $model->cmd == "subform3_clear" || $model->cmd == "subform3_change") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery3($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field3;
            }
        } else {
            $Row =& $model->field3;
        }
        
        //データをカウント
        $mainCountData = knje390mQuery::getCheckMainDataQuery($db, $model, "3");

        //作成履歴
        $extra = "onchange=\"return btn_submit('subform3_change')\"";
        $query = knje390mQuery::getRecordDate($model, "C");
        makeCmb($objForm, $arg, $db, $query, "RECORD_HISTORY", $Row["RECORD_DATE"], $extra, 1, 1);
        $recordDate = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //作成年月日
        $extra = "";
        $Row["RECORD_DATE"] = str_replace("-", "/", $Row["RECORD_DATE"]);
        $arg["data"]["RECORD_DATE"] = $Row["RECORD_DATE"];
        if ($mainCountData == 0) {
            $Row["RECORD_DATE"] = str_replace("-", "/", CTRL_DATE);
            $arg["data"]["RECORD_DATE"] = View::popUpCalendar($objForm, "RECORD_DATE", $Row["RECORD_DATE"]);
        } else {
            KnjCreateHidden($objForm, "RECORD_DATE", $model->record_date);
        }
        
        //作成者
        $extra = "";
        if ($Row["RECORD_STAFFNAME"] === null) {
            $Row["RECORD_STAFFNAME"] = $db->getOne(knje390mQuery::getStaffName($model));
        }
        $arg["data"]["RECORD_STAFFNAME"] = getTextOrArea($objForm, "RECORD_STAFFNAME", 25, 1, $Row["RECORD_STAFFNAME"]);
        setInputChkHidden($objForm, "RECORD_STAFFNAME", 25, 1, $arg);
        
        //基本情報データ日付コンボ
        $extra = "";
        $query = knje390mQuery::getTorikomiRecordDate($model, "B");
        makeCmb($objForm, $arg, $db, $query, "TORIKOMI_B_DATE", $model->field4["TORIKOMI_B_DATE"], $extra, 1, "");
        
        //基本情報の障害名･診断名、障害の実態･特性の取込
        if ($model->cmd == "subform3_torikomi_main") {
            $getRow = $db->getRow(knje390mQuery::getSubQuery1($model, $model->field4["TORIKOMI_B_DATE"]), DB_FETCHMODE_ASSOC);
            $Row["CHALLENGED_NAMES"] = $getRow["CHALLENGED_NAMES"];
            $Row["CHALLENGED_STATUS"] = $getRow["CHALLENGED_STATUS"];
        }

        //障害名･診断名
        $extra = "style=\"height:85px;\"";
        $arg["data"]["CHALLENGED_NAMES"] = getTextOrArea($objForm, "CHALLENGED_NAMES", 40, 2, $Row["CHALLENGED_NAMES"]);
        setInputChkHidden($objForm, "CHALLENGED_NAMES", 40, 2, $arg);

        //支援計画 願い
        $extra = "";
        $query = knje390mQuery::getChallengedSupportplanKindNameDat($model, "01");
        $result = $db->query($query);
        $i = 0;
        $maxDataDivHope = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["KIND_SEQ"] == "000") {
                //項目名（左・右）
                $arg["data"]["STATUS_NAME1"] = $row["STATUS_NAME1"];
                $arg["data"]["STATUS_NAME2"] = $row["STATUS_NAME2"];
            } else {
                $setTmp = array();
                //項目名（縦）
                $setTmp["KIND_NAME"] = $row["KIND_NAME"];

                //所見の取得
                $div = sprintf("%02d", $i);
                $record = $db->getRow(knje390mQuery::getSchregChallengedSupportplanRecordDat($model, $div), DB_FETCHMODE_ASSOC);

                //所見（左）
                if (!isset($model->warning)) {
                    $Row["DIV".$i."_HOPE1"] = $record["HOPE1"];
                }
                $moji = 25;
                $gyou = 3;
                $setTmp["HOPE1"] = getTextOrArea($objForm, "DIV".$i."_HOPE1", $moji, $gyou, $Row["DIV".$i."_HOPE1"]);
                $setTmp["HOPE1_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_HOPE1_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_HOPE1_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_HOPE1_STAT", "statusarea_DIV".$i."_HOPE1");

                //所見（右）
                if (!isset($model->warning)) {
                    $Row["DIV".$i."_HOPE2"] = $record["HOPE2"];
                }
                $moji = 25;
                $gyou = 3;
                $setTmp["HOPE2"] = getTextOrArea($objForm, "DIV".$i."_HOPE2", $moji, $gyou, $Row["DIV".$i."_HOPE2"]);
                $setTmp["HOPE2_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_HOPE2_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_HOPE2_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_HOPE2_STAT", "statusarea_DIV".$i."_HOPE2");
                $arg["data1"][] = $setTmp;
            }
            $maxDataDivHope = $row["KIND_SEQ"];
            $i++;
        }
        knjCreateHidden($objForm, "MAX_DATA_DIV_HOPE", (int)$maxDataDivHope); //支援計画 希望 項目数

        //支援計画 願い
        $extra = "";
        $query = knje390mQuery::getChallengedSupportplanKindNameDat($model, "02");
        $result = $db->query($query);
        $i = 0;
        $maxDataDivGoals = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["KIND_SEQ"] != "000") {
                $setTmp = array();
                //項目名（縦）
                $setTmp["KIND_NAME"] = $row["KIND_NAME"];

                //所見の取得
                $div = sprintf("%02d", $i);
                $record = $db->getRow(knje390mQuery::getSchregChallengedSupportplanRecordDat($model, $div), DB_FETCHMODE_ASSOC);

                //所見
                if (!isset($model->warning)) {
                    $Row["DIV".$i."_GOALS"] = $record["GOALS"];
                }
                $moji = 50;
                $gyou = 3;
                $setTmp["GOALS"] = getTextOrArea($objForm, "DIV".$i."_GOALS", $moji, $gyou, $Row["DIV".$i."_GOALS"]);
                $setTmp["GOALS_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_GOALS_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_GOALS_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_GOALS_STAT", "statusarea_DIV".$i."_GOALS");
                $arg["data2"][] = $setTmp;
            }
            $maxDataDivGoals = $row["KIND_SEQ"];
            $i++;
        }
        knjCreateHidden($objForm, "MAX_DATA_DIV_GOALS", (int)$maxDataDivGoals); //支援計画 目標 項目数

        //合理的配慮
        $moji = 55;
        $gyou = 15;
        $arg["data"]["REASONABLE_ACCOMMODATION"] = getTextOrArea($objForm, "REASONABLE_ACCOMMODATION", $moji, $gyou, $Row["REASONABLE_ACCOMMODATION"]);
        setInputChkHidden($objForm, "REASONABLE_ACCOMMODATION", $moji, $gyou, $arg);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $mainCountData);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning)== 0 && $model->cmd !="subform1_clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="subform3_clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm3.html", $arg);
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

    //具体的な支援、連携の記録
    $extra = "onclick=\"return btn_submit('subform3ConcreteSupport');\"";
    $arg["button"]["btn_support"] = KnjCreateBtn($objForm, "btn_support", "具体的な支援、連携の記録", $extra.$disabled);

    //障害名等マスタ
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=challenged_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_statusname"] = knjCreateBtn($objForm, "btn_statusname", "障害名等マスタ参照", $extra);
    //基本情報から取込ボタン
    $extra = "onclick=\"return btn_submit('subform3_torikomi_main');\"";
    $arg["button"]["btn_Btorikomi"] = KnjCreateBtn($objForm, "btn_Btorikomi", "基本情報から取込", $extra);

    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform3_updatemain');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform3_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

    //コピーして新規作成ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform3_copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "コピーして新規作成", $extra.$disabled);
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
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
