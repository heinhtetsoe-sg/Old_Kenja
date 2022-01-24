<?php

require_once('for_php7.php');
class knje390mSubForm8
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform8", "POST", "knje390mindex.php", "", "subform8");

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

        //作成日付が設定されてない場合は最新の日付を取得
        if ($model->record_date == "") {
            //ログイン年度の最新データをセット
            $getMaxDate = $db->getOne(knje390mQuery::getMaxRecordData8Query($model));
            $model->record_date = $getMaxDate;
        }
        if ($model->cmd === 'subform8_change') {
            $model->record_date = $model->field8["RECORD_HISTORY"];
        }

        if ($model->record_date != "") {
            $recordDateArr = split("-", str_replace("/", "-", $model->record_date));
            $model->main_year = $recordDateArr[0];
        }
        $newflg = "";
        if (!$model->record_date) {
            $model->field8 = array();
            $newflg = "1";
        }
        knjCreateHidden($objForm, "NEW_FLG", $newflg);

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform8" || $model->cmd == "subform8A" || $model->cmd == "subform8_clear" || $model->cmd === 'subform8_change') {
            if (isset($model->schregno) && !isset($model->warning) && $model->record_date != "") {
                $Row = $db->getRow(knje390mQuery::getSubQuery8($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field8;
            }
        } else {
            $Row =& $model->field8;
        }

        //作成履歴
        $extra = "onchange=\"return btn_submit('subform8_change')\"";
        $query = knje390mQuery::getRecordDate($model, "H");
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

        //記入者
        $extra = "";
        $arg["data"]["WRITER"] = getTextOrArea($objForm, "WRITER", 18, 1, $Row["WRITER"]);
        $arg["data"]["WRITER_SIZE"] = '<font size="2" color="red">(全角18文字まで)</font>';
        //会議名
        $extra = "";
        $arg["data"]["MEETING_NAME"] = getTextOrArea($objForm, "MEETING_NAME", 40, 1, $Row["MEETING_NAME"]);
        $arg["data"]["MEETING_NAME_SIZE"] = '<font size="2" color="red">(全角40文字まで)</font>';
        //打合せ日時
        $Row["MEETING_DATE"] = str_replace("-", "/", $Row["MEETING_DATE"]);
        $arg["data"]["MEETING_DATE"] = View::popUpCalendar($objForm, "MEETING_DATE", $Row["MEETING_DATE"]);
        //開始時間
        $extra = "";
        $query = knje390mQuery::getTimeHour();
        makeCmb($objForm, $arg, $db, $query, "MEETING_SHOUR", $Row["MEETING_SHOUR"], $extra, 1, 1);
        $query = knje390mQuery::getTimeMinutes();
        makeCmb($objForm, $arg, $db, $query, "MEETING_SMINUTES", $Row["MEETING_SMINUTES"], $extra, 1, 1);
        //終了時間
        $query = knje390mQuery::getTimeHour();
        makeCmb($objForm, $arg, $db, $query, "MEETING_EHOUR", $Row["MEETING_EHOUR"], $extra, 1, 1);
        $query = knje390mQuery::getTimeMinutes();
        makeCmb($objForm, $arg, $db, $query, "MEETING_EMINUTES", $Row["MEETING_EMINUTES"], $extra, 1, 1);
        //打合せ場所
        $extra = "";
        $arg["data"]["MEETING_PALCE"] = getTextOrArea($objForm, "MEETING_PALCE", 18, 1, $Row["MEETING_PALCE"]);
        $arg["data"]["MEETING_PALCE_SIZE"] = '<font size="1" color="red">(全角18文字まで)</font>';
        //参加者
        $extra = "";
        $arg["data"]["MEETING_PARTICIPANT"] = getTextOrArea($objForm, "MEETING_PARTICIPANT", 40, 2, $Row["MEETING_PARTICIPANT"]);
        $arg["data"]["MEETING_PARTICIPANT_SIZE"] = '<font size="2" color="red">(全角40文字X2行まで)</font>';
        
        $extra = "";
        //問題事象
        $arg["data"]["PROBLEM_EVENT_DOCUMENTS"] = getTextOrArea($objForm, "PROBLEM_EVENT_DOCUMENTS", 40, 10, $Row["PROBLEM_EVENT_DOCUMENTS"]);
        //事象分析
        $arg["data"]["PROBLEM_ANALYSIS_DOCUMENTS"] = getTextOrArea($objForm, "PROBLEM_ANALYSIS_DOCUMENTS", 40, 10, $Row["PROBLEM_ANALYSIS_DOCUMENTS"]);
        //課題対応
        $arg["data"]["PROBLEM_DEAL_DOCUMENTS"] = getTextOrArea($objForm, "PROBLEM_DEAL_DOCUMENTS", 40, 10, $Row["PROBLEM_DEAL_DOCUMENTS"]);
        //指導者の役割等
        $arg["data"]["STAFF_ROLE_DOCUMENTS"] = getTextOrArea($objForm, "STAFF_ROLE_DOCUMENTS", 40, 10, $Row["STAFF_ROLE_DOCUMENTS"]);
        //保護者連携
        $arg["data"]["GUARDIAN_COOPERATION_DOCUMENTS"] = getTextOrArea($objForm, "GUARDIAN_COOPERATION_DOCUMENTS", 40, 10, $Row["GUARDIAN_COOPERATION_DOCUMENTS"]);
        //福祉連携
        $arg["data"]["WELFARE_COOPERATION_DOCUMENTS"] = getTextOrArea($objForm, "WELFARE_COOPERATION_DOCUMENTS", 40, 10, $Row["WELFARE_COOPERATION_DOCUMENTS"]);
        //その他
        $arg["data"]["OTHERS_REMARK_DOCUMENTS"] = getTextOrArea($objForm, "OTHERS_REMARK_DOCUMENTS", 40, 10, $Row["OTHERS_REMARK_DOCUMENTS"]);
        $arg["data"]["DOCUMENTS_SIZE"] = '<font size="2" color="red">(全角40文字X10行まで)</font>';

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
        View::toHTML($model, "knje390mSubForm8.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData, $Row)
{
    //コピーして新規作成ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform8_copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_rireki", "コピーして新規作成", $extra.$disabled);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform8_updatemain');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform8_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
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
