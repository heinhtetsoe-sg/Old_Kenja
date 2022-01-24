<?php

require_once('for_php7.php');
class knje390mSubForm4
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform4", "POST", "knje390mindex.php", "", "subform4");

        //DB接続
        $db = Query::dbCheckOut();

        //データがない場合は最新をセット
        if (!$model->main_year) {
            $model->main_year = CTRL_YEAR;//年度データはないが取込時に使用
            if ($model->record_date != "") {
                $recordDateArr = split("-", str_replace("/", "-", $model->record_date));
                $model->main_year = $recordDateArr[0];
            }
        }

        $recordDate = $db->getRow(knje390mQuery::getRecordDate($model, "D"), DB_FETCHMODE_ASSOC);
        if (!$model->record_date) {
            $model->record_date = $recordDate["VALUE"];
        }

        if ($model->cmd == "subform4_change") {
            $model->record_date = str_replace("/", "-", $model->field4["RECORD_HISTORY"]);
        }

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform4" || $model->cmd == "subform4A" || $model->cmd == "subform4_clear" || $model->cmd == "subform4_change") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery4($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field4;
            }
        } else {
            $Row =& $model->field4;
        }

        //データをカウント
        $mainCountData = knje390mQuery::getCheckMainDataQuery($db, $model, "4");

        //作成履歴
        $extra = "onchange=\"return btn_submit('subform4_change')\"";
        $query = knje390mQuery::getRecordDate($model, "D");
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
        if ($model->cmd == "subform4_torikomi_main") {
            $getRow = $db->getRow(knje390mQuery::getSubQuery1($model, $model->field4["TORIKOMI_B_DATE"]), DB_FETCHMODE_ASSOC);
            $Row["CHALLENGED_NAMES"] = $getRow["CHALLENGED_NAMES"];
            $Row["CHALLENGED_STATUS"] = $getRow["CHALLENGED_STATUS"];
        }
        //障害名･診断名
        $extra = "style=\"height:45px;\"";
        $arg["data"]["CHALLENGED_NAMES"] = getTextOrArea($objForm, "CHALLENGED_NAMES", 15, 3, $Row["CHALLENGED_NAMES"]);
        setInputChkHidden($objForm, "CHALLENGED_NAMES", 15, 3, $arg);

        //障害の実態･特性
        $extra = "style=\"height:110px;\"";
        $arg["data"]["CHALLENGED_STATUS"] = getTextOrArea($objForm, "CHALLENGED_STATUS", 15, 8, $Row["CHALLENGED_STATUS"]);
        setInputChkHidden($objForm, "CHALLENGED_STATUS", 15, 8, $arg);

        //実態・分析、課題・つけたい力
        $extra = "style=\"height:210px;\"";
        $arg["data"]["LIFESTYLE_STATUS"] = getTextOrArea($objForm, "LIFESTYLE_STATUS", 35, 15, $Row["LIFESTYLE_STATUS"]);
        setInputChkHidden($objForm, "LIFESTYLE_STATUS", 35, 15, $arg);
        $arg["data"]["SOCIALITY_STATUS"] = getTextOrArea($objForm, "SOCIALITY_STATUS", 35, 15, $Row["SOCIALITY_STATUS"]);
        setInputChkHidden($objForm, "SOCIALITY_STATUS", 35, 15, $arg);
        $arg["data"]["COMMUNICATION_STATUS"] = getTextOrArea($objForm, "COMMUNICATION_STATUS", 35, 15, $Row["COMMUNICATION_STATUS"]);
        setInputChkHidden($objForm, "COMMUNICATION_STATUS", 35, 15, $arg);
        $arg["data"]["PHYSICAL_ACTIVITY_STATUS"] = getTextOrArea($objForm, "PHYSICAL_ACTIVITY_STATUS", 35, 15, $Row["PHYSICAL_ACTIVITY_STATUS"]);
        setInputChkHidden($objForm, "PHYSICAL_ACTIVITY_STATUS", 35, 15, $arg);
        $arg["data"]["STUDY_STATUS"] = getTextOrArea($objForm, "STUDY_STATUS", 35, 15, $Row["STUDY_STATUS"]);
        setInputChkHidden($objForm, "STUDY_STATUS", 35, 15, $arg);
        $arg["data"]["INTERESTING_STATUS"] = getTextOrArea($objForm, "INTERESTING_STATUS", 35, 15, $Row["INTERESTING_STATUS"]);
        setInputChkHidden($objForm, "INTERESTING_STATUS", 35, 15, $arg);

        //希望・願い
        //本人
        $extra = "style=\"height:35px;\"";
        $arg["data"]["ONES_HOPE_PRESENT"] = getTextOrArea($objForm, "ONES_HOPE_PRESENT", 20, 2, $Row["ONES_HOPE_PRESENT"]);
        setInputChkHidden($objForm, "ONES_HOPE_PRESENT", 20, 2, $arg);
        $arg["data"]["ONES_HOPE_CAREER"] = getTextOrArea($objForm, "ONES_HOPE_CAREER", 20, 2, $Row["ONES_HOPE_CAREER"]);
        setInputChkHidden($objForm, "ONES_HOPE_CAREER", 20, 2, $arg);
        $arg["data"]["ONES_HOPE_AFTER_GRADUATION"] = getTextOrArea($objForm, "ONES_HOPE_AFTER_GRADUATION", 20, 2, $Row["ONES_HOPE_AFTER_GRADUATION"]);
        setInputChkHidden($objForm, "ONES_HOPE_AFTER_GRADUATION", 20, 2, $arg);

        //保護者
        $arg["data"]["GUARDIAN_HOPE_PRESENT"] = getTextOrArea($objForm, "GUARDIAN_HOPE_PRESENT", 25, 2, $Row["GUARDIAN_HOPE_PRESENT"]);
        setInputChkHidden($objForm, "GUARDIAN_HOPE_PRESENT", 25, 2, $arg);
        $arg["data"]["GUARDIAN_HOPE_CAREER"] = getTextOrArea($objForm, "GUARDIAN_HOPE_CAREER", 25, 2, $Row["GUARDIAN_HOPE_CAREER"]);
        setInputChkHidden($objForm, "GUARDIAN_HOPE_CAREER", 25, 2, $arg);
        $arg["data"]["GUARDIAN_HOPE_AFTER_GRADUATION"] = getTextOrArea($objForm, "GUARDIAN_HOPE_AFTER_GRADUATION", 25, 2, $Row["GUARDIAN_HOPE_AFTER_GRADUATION"]);
        setInputChkHidden($objForm, "GUARDIAN_HOPE_AFTER_GRADUATION", 25, 2, $arg);

        //引継ぎ事項
        $extra = "style=\"height:80px;\"";
        $arg["data"]["TAKEOVER"] = getTextOrArea($objForm, "TAKEOVER", 45, 5, $Row["TAKEOVER"]);
        setInputChkHidden($objForm, "TAKEOVER", 45, 5, $arg);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $mainCountData);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning)== 0 && $model->cmd !="subform1_clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="subform1_clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm4.html", $arg);
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

    //障害名等マスタ
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=challenged_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_statusname"] = knjCreateBtn($objForm, "btn_statusname", "障害名等マスタ参照", $extra);
    //基本情報から取込ボタン
    $extra = "onclick=\"return btn_submit('subform4_torikomi_main');\"";
    $arg["button"]["btn_Btorikomi"] = KnjCreateBtn($objForm, "btn_Btorikomi", "基本情報から取込", $extra);

    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform4_updatemain');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform4_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

    //発達検査ボタン
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform4_check&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "検 査", $extra.$disabled);

    //スムーズな以降に向けての支援ボタン
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform4_smooth&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace2"] = KnjCreateBtn($objForm, "btn_replace2", "スムーズな移行に向けての支援", $extra.$disabled);

    //進路指導計画ボタン
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform4_careerguidance&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace3"] = KnjCreateBtn($objForm, "btn_replace3", "進路指導計画", $extra.$disabled);

    //コピーして新規作成ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform4_copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "コピーして新規作成", $extra.$disabled);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
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
