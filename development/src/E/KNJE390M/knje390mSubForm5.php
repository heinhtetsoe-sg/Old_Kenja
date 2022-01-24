<?php

require_once('for_php7.php');
class knje390mSubForm5
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform5", "POST", "knje390mindex.php", "", "subform5");

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
            $getMaxDate = $db->getOne(knje390mQuery::getMaxRecordData5Query($model));
            $model->record_date = $getMaxDate;
        }
        if ($model->cmd === 'subform5_change') {
            $model->record_date = $model->field5["RECORD_HISTORY"];
        }
        if ($model->record_date != "") {
            $recordDateArr = split("-", str_replace("/", "-", $model->record_date));
            $model->main_year = $recordDateArr[0];
        }
        $newflg = "";
        if (!$model->record_date) {
            $model->field5 = array();
            $newflg = "1";
        }
        knjCreateHidden($objForm, "NEW_FLG", $newflg);

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform5" || $model->cmd == "subform5A" || $model->cmd == "subform5_clear" || $model->cmd == "subform5_change") {
            if (isset($model->schregno) && !isset($model->warning) && $model->record_date != "") {
                $Row = $db->getRow(knje390mQuery::getSubQuery5($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field5;
            }
        } else {
            $Row =& $model->field5;
        }

        //作成履歴
        $extra = "onchange=\"return btn_submit('subform5_change')\"";
        $query = knje390mQuery::getRecordDate($model, "E");
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

        //障害名
        $arg["data"]["CHALLENGED_NAMES"] = getTextOrArea($objForm, "CHALLENGED_NAMES", 40, 2, $Row["CHALLENGED_NAMES"]);
        $arg["data"]["CHALLENGED_NAMES_SIZE"] = '<font size="2" color="red">(全角40文字X2行まで)</font>';
        //呼び名
        $arg["data"]["CHALLENGED_POPULAR_NAME"] = getTextOrArea($objForm, "CHALLENGED_POPULAR_NAME", 40, 2, $Row["CHALLENGED_POPULAR_NAME"]);
        $arg["data"]["CHALLENGED_POPULAR_NAME_SIZE"] = '<font size="2" color="red">(全角40文字X2行まで)</font>';
        //持病名
        $arg["data"]["CHRONIC_DISEASE"] = getTextOrArea($objForm, "CHRONIC_DISEASE", 40, 5, $Row["CHRONIC_DISEASE"]);
        $arg["data"]["CHRONIC_DISEASE_SIZE"] = '<font size="2" color="red">(全角40文字5行まで)</font>';

        //コミュニケーション(会話)の仕方
        $arg["data"]["HOW_COMMUNICATION"] = getTextOrArea($objForm, "HOW_COMMUNICATION", 40, 6, $Row["HOW_COMMUNICATION"]);
        $arg["data"]["HOW_COMMUNICATION_SIZE"] = '<font size="2" color="red">(全角40文字6行まで)</font>';

        //困った行動の対処法
        $arg["data"]["TROUBLED_BEHAVIOR_SUPPORT"] = getTextOrArea($objForm, "TROUBLED_BEHAVIOR_SUPPORT", 40, 7, $Row["TROUBLED_BEHAVIOR_SUPPORT"]);
        $arg["data"]["TROUBLED_BEHAVIOR_SUPPORT_SIZE"] = '<font size="1" color="red">(全角40文字7行まで)</font>';

        //不調のサイン
        $arg["data"]["BAD_CONDITION_SIGN"] = getTextOrArea($objForm, "BAD_CONDITION_SIGN", 40, 7, $Row["BAD_CONDITION_SIGN"]);
        $arg["data"]["BAD_CONDITION_SIGN_SIZE"] = '<font size="1" color="red">(全角40文字7行まで)</font>';

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
        View::toHTML($model, "knje390mSubForm5.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData, $Row)
{
    //コピーして新規作成ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform5_copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_rireki", "コピーして新規作成", $extra.$disabled);
    //障害名･診断名マスタ
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=challenged_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_statusname"] = knjCreateBtn($objForm, "btn_statusname", "障害名･診断名マスタ参照", $extra);
    //新規作成ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform5_formatnew');\"";
    $arg["button"]["btn_formatnew"] = knjCreateBtn($objForm, "btn_formatnew", "新規作成", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform5_updatemain');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform5_clear');\"";
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
