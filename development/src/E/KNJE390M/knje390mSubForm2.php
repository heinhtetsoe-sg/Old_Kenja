<?php

require_once('for_php7.php');
class knje390mSubForm2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knje390mindex.php", "", "subform2");

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
        //新規作成時は全て項目をNULLにする
        if ($model->cmd === 'subform2_formatnew') {
            $model->record_date = "";
            $model->field2 = array();
            $newflg = "1";
        }

        //通常の場合は最新版を表示
        if ($model->record_date == "" && $model->field2["RECORD_DATE"] == "" && $model->cmd !== 'subform2_formatnew') {
            //ログイン年度の最新データをセット
            $getMaxDate = $db->getOne(knje390mQuery::getMaxRecordDataQuery($model));
            $model->record_date = $getMaxDate;
            $newflg = "";
        }

        knjCreateHidden($objForm, "NEW_FLG", $newflg);

        if ($model->cmd == "subform2_change") {
            $model->record_date = str_replace("/", "-", $model->field2["RECORD_HISTORY"]);
        }
        if ($model->record_date != "") {
            $recordDateArr = split("-", str_replace("/", "-", $model->record_date));
            $model->main_year = $recordDateArr[0];
        }

        //年度表示
        $arg["data"]["NENDO"] = $model->main_year.'年度';

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //項目名取得
        $label = array();
        $model->subFrm2_maxItemCnt = 0;
        $query = knje390mQuery::getChallengedAssessmentStatusGrowupDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["DATA_DIV"] == "0") {
                $label["SHEET_PATTERN"] = $row["SHEET_PATTERN"];
                $label["STATUS_NAME"] = $row["STATUS_NAME"];
                $label["GROWUP_NAME"] = $row["GROWUP_NAME"];
            } else {
                $label["DATA_DIV_NAME".$row["DATA_DIV"]] = $row["DATA_DIV_NAME"];
                $model->subFrm2_maxItemCnt = $row["DATA_DIV"];
            }
        }
        $result->free();
        knjCreateHidden($objForm, "MAX_DATA_DIV", $model->subFrm2_maxItemCnt);

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform2" || $model->cmd == "subform2A" || $model->cmd == "subform2_clear" || $model->cmd == "subform2_change") {
            if (isset($model->schregno) && !isset($model->warning) && $model->record_date != "") {
                $Row = $db->getRow(knje390mQuery::getSubQuery2($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field2;
            }
        } else {
            $Row =& $model->field2;
        }

        //データをカウント
        $mainCountData = knje390mQuery::getCheckMainDataQuery($db, $model, "2");

        //作成履歴
        $extra = "onchange=\"return btn_submit('subform2_change')\"";
        $query = knje390mQuery::getRecordDate($model, "A");
        makeCmb($objForm, $arg, $db, $query, "RECORD_HISTORY", $Row["RECORD_DATE"], $extra, 1, 1);

        //作成年月日
        $extra = "";
        $Row["RECORD_DATE"] = str_replace("-", "/", $Row["RECORD_DATE"]);
        $arg["data"]["RECORD_DATE"] = $Row["RECORD_DATE"];
        if ($mainCountData == 0 || $model->cmd == "subform2_formatnew") {
            $Row["RECORD_DATE"] = str_replace("-", "/", CTRL_DATE);
            $arg["data"]["RECORD_DATE"] = View::popUpCalendar($objForm, "RECORD_DATE", $Row["RECORD_DATE"]);
        } else {
            KnjCreateHidden($objForm, "RECORD_DATE", $model->record_date);
        }

        //作成者
        if ($Row["RECORD_STAFFNAME"] === null) {
            $Row["RECORD_STAFFNAME"] = $db->getOne(knje390mQuery::getStaffName($model));
        }
        $arg["data"]["RECORD_STAFFNAME"] = getTextOrArea($objForm, "RECORD_STAFFNAME", 25, 1, $Row["RECORD_STAFFNAME"]);
        $arg["data"]["RECORD_STAFFNAME_COMMENT"] = getTextAreaComment(25, 1);

        //読込年度
        $query = knje390mQuery::getMainYear($model, "A");
        makeCmb($objForm, $arg, $db, $query, "YOMIKOMI_YEAR", $model->field2["YOMIKOMI_YEAR"], $extra, 1, "");

        //基本情報データ日付コンボ
        $extra = "";
        $query = knje390mQuery::getTorikomiRecordDate($model, "B");
        makeCmb($objForm, $arg, $db, $query, "TORIKOMI_B_DATE", $model->field2["TORIKOMI_B_DATE"], $extra, 1, "");
        
        //プロフィールの障害名等、障害の実態･特性の取込
        if ($model->cmd == "subform2_torikomi_main") {
            $getRow = $db->getRow(knje390mQuery::getSubQuery1($model, $model->field2["TORIKOMI_B_DATE"]), DB_FETCHMODE_ASSOC);
            $Row["CHALLENGED_NAMES"] = $getRow["CHALLENGED_NAMES"];
            $Row["CHALLENGED_STATUS"] = $getRow["CHALLENGED_STATUS"];
        }
        //障害名等
        $arg["data"]["CHALLENGED_NAMES"] = getTextOrArea($objForm, "CHALLENGED_NAMES", 40, 2, $Row["CHALLENGED_NAMES"]);
        setInputChkHidden($objForm, "CHALLENGED_NAMES", 40, 2, $arg);

        //障害の実態･特性
        $arg["data"]["CHALLENGED_STATUS"] = getTextOrArea($objForm, "CHALLENGED_STATUS", 40, 3, $Row["CHALLENGED_STATUS"]);
        setInputChkHidden($objForm, "CHALLENGED_STATUS", 40, 3, $arg);

        //項目名（横）
        $arg["data"]["STATUS_NAME"] = $label["STATUS_NAME"];
        $arg["data"]["GROWUP_NAME"] = $label["GROWUP_NAME"];
        $arg["data"]["isGroupColumn"] = $label["SHEET_PATTERN"] == "2" ? "1" : "";
        $arg["data"]["COLSPAN"] = $label["SHEET_PATTERN"] == "2" ? "2" : "1";

        if ($model->subFrm2_maxItemCnt > 0) {
            for ($i = 1; $i <= $model->subFrm2_maxItemCnt; $i++) {
                $setTmp = array();

                //項目名（縦）
                $setTmp["DATA_DIV_NAME"] = $label["DATA_DIV_NAME".$i];

                //テキスト（左）
                $moji = 25;
                $gyou = 30;
                if (!$arg["data"]["isGroupColumn"]) {
                    // 1枠表示時の 文字数と行数
                    $moji = 40;
                    $gyou = 30;
                }
                $setTmp["STATUS"]       = getTextOrArea($objForm, "DIV".$i."_STATUS", $moji, $gyou, $Row["DIV".$i."_STATUS"]);
                $setTmp["STATUS_COMMENT"] = getTextAreaComment($moji, $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_KETA", $moji*2);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_GYO", $gyou);
                KnjCreateHidden($objForm, "DIV".$i."_STATUS_STAT", "statusarea_DIV".$i."_STATUS");

                //テキスト（右）
                $setTmp["GROWUP"]       = getTextOrArea($objForm, "DIV".$i."_GROWUP", 15, 30, $Row["DIV".$i."_GROWUP"]);
                $setTmp["GROWUP_COMMENT"] = getTextAreaComment(15, 30);
                KnjCreateHidden($objForm, "DIV".$i."_GROWUP_KETA", 15*2);
                KnjCreateHidden($objForm, "DIV".$i."_GROWUP_GYO", 30);
                KnjCreateHidden($objForm, "DIV".$i."_GROWUP_STAT", "statusarea_DIV".$i."_GROWUP");

                $setTmp["CNT"] = $i;

                $arg["data2"][] = $setTmp;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $mainCountData, $Row);
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
        View::toHTML($model, "knje390mSubForm2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData, $Row)
{
    //データがないまたは作成年月日が指定していない場合は、更新、取消、戻る以外は使用不可
    if ($mainCountData == 0) {
        $disabled = " disabled ";
    } else {
        if ($model->record_date != "" && $Row["RECORD_DATE"] != "") {
            $disabled = "";
        } else {
            $disabled = " disabled ";
        }
    }

    //障害名等マスタ
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=challenged_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_statusname"] = knjCreateBtn($objForm, "btn_statusname", "障害名等マスタ参照", $extra);
    //基本情報から取込ボタン
    $extra = "onclick=\"return btn_submit('subform2_torikomi_main');\"";
    $arg["button"]["btn_Btorikomi"] = KnjCreateBtn($objForm, "btn_Btorikomi", "基本情報から取込", $extra);

    //新規作成ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform2_formatnew');\"";
    $arg["button"]["btn_formatnew"] = knjCreateBtn($objForm, "btn_formatnew", "新規作成", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform2_updatemain');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform2_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

    //教科等の実態ボタン
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform2_actual&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_actual"] = KnjCreateBtn($objForm, "btn_replace", "教科等の実態", $extra.$disabled);
    //検査ボタン
    $link = REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=subform2_check&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "検　査", $extra.$disabled);

    //コピーして新規作成ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform2_copy');\"";
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
