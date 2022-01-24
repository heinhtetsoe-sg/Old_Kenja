<?php

require_once('for_php7.php');
class knje390mSubForm_Zittai
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subformZittai", "POST", "knje390mindex.php", "", "subformZittai");

        //DB接続
        $db = Query::dbCheckOut();

        //データがない場合は最新をセット
        if (!$model->main_year) {
            $model->main_year = CTRL_YEAR;//年度データはないが、念の為にセット
            if ($model->record_date != "") {
                $recordDateArr = split("-", str_replace("/", "-", $model->record_date));
                $model->main_year = $recordDateArr[0];
            }
        }
        //作成日付が設定されてない場合は最新の日付を取得
        if ($model->record_date == "") {
            //ログイン年度の最新データをセット
            $row = $db->getRow(knje390mQuery::getRecordDate($model, "I"), DB_FETCHMODE_ASSOC);
            $model->record_date = $row["VALUE"];
        }
        if ($model->cmd === 'subformZittai_change') {
            $model->record_date = $model->field9["RECORD_HISTORY"];
        }
        if ($model->record_date != "") {
            $recordDateArr = split("-", str_replace("/", "-", $model->record_date));
            $model->main_year = $recordDateArr[0];
        }
        $newflg = "";
        if (!$model->record_date) {
            $model->field9 = array();
            $newflg = "1";
        }
        knjCreateHidden($objForm, "NEW_FLG", $newflg);

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];
        //警告メッセージを表示しない場合
        if ($model->cmd == "subformZittai" || $model->cmd == "subformZittaiA" || $model->cmd == "subformZittai_clear" || $model->cmd == "subformZittai_copy" || $model->cmd == "subformZittai_change") {
            if (isset($model->schregno) && !isset($model->warning)) {

                /** record_dateがnullの時のエラー回避 */
                if(!$model->record_date == "") {
                    $Row = $db->getRow(knje390mQuery::getStatusSheetMainData($model), DB_FETCHMODE_ASSOC);
                    $arg["NOT_WARNING"] = 1;
                }
            } else {
                $Row =& $model->field9;
            }
        } else {
            $Row =& $model->field9;
        }

        //作成履歴
        $extra = "onchange=\"return btn_submit('subformZittai_change')\"";
        $query = knje390mQuery::getRecordDate($model, "I");
        $recDate = str_replace("/", "-", $Row["RECORD_DATE"]);
        makeCmb($objForm, $arg, $db, $query, "RECORD_HISTORY", $recDate, $extra, 1, 1);
        //登録されている最大作成年月日
        $recordDate = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "MAX_DATE", str_replace("/", "-", $recordDate["VALUE"]));

        //作成年月日
        if ($Row["RECORD_DATE"]) {
            $Row["RECORD_DATE"] = str_replace("-", "/", $Row["RECORD_DATE"]);
            $arg["data"]["RECORD_DATE"] = $Row["RECORD_DATE"];
            knjCreateHidden($objForm, "RECORD_DATE", $Row["RECORD_DATE"]);
        } else {
            $extra = "";
            $Row["RECORD_DATE"] = str_replace("-", "/", CTRL_DATE);
            $arg["data"]["RECORD_DATE"] = View::popUpCalendar($objForm, "RECORD_DATE", $Row["RECORD_DATE"]);
        }

        //担任名
        $arg["data"]["RECORD_STAFFNAME"] = getTextOrArea($objForm, "RECORD_STAFFNAME", 25, 1, $Row["RECORD_STAFFNAME"]);
        setInputChkHidden($objForm, "RECORD_STAFFNAME", 25, 1, $arg);

        //プロフィールの障害名等、障害の実態･特性の取込
        if ($model->cmd == "subformZittai_torikomi_main") {
            $getRow = $db->getRow(knje390mQuery::getSubQuery1($model, $model->field9["TORIKOMI_B_DATE"]), DB_FETCHMODE_ASSOC);
            $Row["CHALLENGED_NAMES"] = $getRow["CHALLENGED_NAMES"];
        }

        //障害名
        $extra = "style=\"height:85px;\"";
        $arg["data"]["CHALLENGED_NAMES"] = getTextOrArea($objForm, "CHALLENGED_NAMES", 40, 2, $Row["CHALLENGED_NAMES"]);
        setInputChkHidden($objForm, "CHALLENGED_NAMES", 40, 2, $arg);

        //基本情報データ日付コンボ
        $extra = "";
        $query = knje390mQuery::getTorikomiRecordDate($model, "B");
        makeCmb($objForm, $arg, $db, $query, "TORIKOMI_B_DATE", $model->field9["TORIKOMI_B_DATE"], $extra, 1, "");

        //実態表項目名設定取得
        $maxDataDiv = 0;
        $itemNameArrayYoko = array();
        $itemNameArrayTate = array();
        $query = knje390mQuery::getChallengedStatusSheetItemNameDat($model, "");
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row2["DATA_DIV"] == "0") {
                $itemNameArrayYoko["0"]["STATUS_NAME"] = $row2["STATUS_NAME"];
                $itemNameArrayYoko["0"]["GROWUP_NAME"] = $row2["GROWUP_NAME"];
            } else {
                $itemNameArrayTate[$row2["DATA_DIV"]] = $row2["DATA_DIV_NAME"];
            }
            $maxDataDiv = ($maxDataDiv < $row2["DATA_DIV"]) ? $row2["DATA_DIV"] : $maxDataDiv;
        }
        knjCreateHidden($objForm, "MAX_DATA_DIV", $maxDataDiv);
        //枠パターン(2枠)
        if ($itemNameArrayYoko["0"]["GROWUP_NAME"] != "") {
            $arg["WIN_PATTERN2"] = "1";
            $windowPattern = "2";
        } else {
            $arg["WIN_PATTERN1"] = "1";
            $windowPattern = "1";
        }

        //実態データ取得
        $itemData = array();
        if (isset($model->warning)) {
            $itemData = $Row;
            foreach ($itemNameArrayTate as $dataDiv => $tateName) {
                $itemData[$dataDiv]["STATUS"] = $Row["DIV{$dataDiv}_STATUS"];
                $itemData[$dataDiv]["GROWUP"] = $Row["DIV{$dataDiv}_GROWUP"];
            }
        } else {

            /** record_dateがnullの時のエラー回避 */
            if(!$model->record_date == "") {
                $query = knje390mQuery::getStatusSheetGSData($model);
                $result = $db->query($query);
                while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $itemData[$row3["DATA_DIV"]] = $row3;
                }
            }
        }
        if (isset($itemNameArrayYoko["0"])) {
            $arg["data"]["STATUS_NAME"] = $itemNameArrayYoko["0"]["STATUS_NAME"];
            $arg["data"]["GROWUP_NAME"] = $itemNameArrayYoko["0"]["GROWUP_NAME"];

            foreach ($itemNameArrayTate as $dataDiv => $tateName) {
                $itemData[$dataDiv]["DATA_DIV_NAME"] = $tateName;
                $statusFormName = "DIV".$dataDiv."_STATUS";
                $growupFormName = "DIV".$dataDiv."_GROWUP";

                if ($windowPattern == "2") {
                    $itemData[$dataDiv]["STATUS"]           = getTextOrArea($objForm, $statusFormName, 25, 30, $itemData[$dataDiv]["STATUS"]);
                    $itemData[$dataDiv]["GROWUP"]           = getTextOrArea($objForm, $growupFormName, 15, 30, $itemData[$dataDiv]["GROWUP"]);
                    $itemData[$dataDiv]["STATUS_COMMENT"]   = setInputChkHidden2($objForm, $statusFormName, 25, 30);
                    $itemData[$dataDiv]["GROWUP_COMMENT"]   = setInputChkHidden2($objForm, $growupFormName, 15, 30);

                    $itemData[$dataDiv]["btn_assess1"]      = makeBtnAssess($objForm, $model, "btn_assess1", $statusFormName);
                    $itemData[$dataDiv]["btn_assess2"]      = makeBtnAssess($objForm, $model, "btn_assess2", $growupFormName);
                } else {
                    $itemData[$dataDiv]["STATUS"]           = getTextOrArea($objForm, $statusFormName, 40, 30, $itemData[$dataDiv]["STATUS"]);
                    $itemData[$dataDiv]["STATUS_COMMENT"]   = setInputChkHidden2($objForm, $statusFormName, 40, 30);

                    $itemData[$dataDiv]["btn_assess1"]      = makeBtnAssess($objForm, $model, "btn_assess1", $statusFormName);
                }
                $arg["list"][] = $itemData[$dataDiv];
            }
        }

        /** record_dateがnullの時のエラー回避 */
        if(!$model->record_date == "") {
            //データをカウント
            $mainCountData = knje390mQuery::getCheckMainDataQuery($db, $model, "9");
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $mainCountData);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm_Zittai.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData)
{
    //データがない場合は、更新、取消、戻る以外は使用不可
    if ($mainCountData == 0) {
        $disabled = " disabled ";
    } else {
        $disabled = "";
    }

    //実習の記録ボタン
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subformZittaiJisshu&SCHREGNO=".$model->schregno;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_jisshu"] = KnjCreateBtn($objForm, "btn_jisshu", "実習の記録", $extra.$disabled);

    //障害名等マスタ
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=challenged_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_statusname"] = knjCreateBtn($objForm, "btn_statusname", "障害名等マスタ参照", $extra);
    //基本情報から取込ボタン
    $extra = "onclick=\"return btn_submit('subformZittai_torikomi_main');\"";
    $arg["button"]["btn_Btorikomi"] = KnjCreateBtn($objForm, "btn_Btorikomi", "基本情報から取込", $extra);

    //コピーして新規作成ボタンを作成する
    $extra = "onclick=\"return btn_submit('subformZittai_copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_rireki", "コピーして新規作成", $extra.$disabled);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subformZittai_updatemain');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subformZittai_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "useFinschoolcdFieldSize", $model->Properties["useFinschoolcdFieldSize"]);
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
        $opt[] = array('label' => $row1["LABEL"], 'value' => $row1["VALUE"]);
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

//ループ内で使用
function setInputChkHidden2(&$objForm, $setHiddenStr, $keta, $gyo)
{
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
    return getTextAreaComment($keta, $gyo);
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
function makeBtnAssess(&$objForm, $model, $name, $target)
{
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=assess_torikomi&SCHREGNO=".$model->schregno."&ASSESS_TORIKOMI_PARENT=SubForm_Zittai&ASSESS_TORIKOMI_TARGET={$target}', function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    return knjCreateBtn($objForm, "btn_assess1", "アセスメント表から取込", $extra);
}
