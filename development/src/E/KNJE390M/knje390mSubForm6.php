<?php

require_once('for_php7.php');
class knje390mSubForm6
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform6", "POST", "knje390mindex.php", "", "subform6");

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
            $getMaxDate = $db->getOne(knje390mQuery::getMaxRecordData6Query($model));
            $model->record_date = $getMaxDate;
        }
        if ($model->cmd === 'subform6_change') {
            $model->record_date = $model->field6["RECORD_HISTORY"];
        }
        if ($model->record_date != "") {
            $recordDateArr = split("-", str_replace("/", "-", $model->record_date));
            $model->main_year = $recordDateArr[0];
        }
        $newflg = "";
        if (!$model->record_date) {
            $model->field6 = array();
            $newflg = "1";
        }
        knjCreateHidden($objForm, "NEW_FLG", $newflg);

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //種別
        $extra = "onchange=\"return btn_submit('subform6A')\"";
        $query = knje390mQuery::getNameMst("E042");
        makeCmb($objForm, $arg, $db, $query, "DATA_DIV", $model->field6["DATA_DIV"], $extra, 1, "");

        //警告メッセージを表示しない場合
        if ($model->cmd == "subform6" || $model->cmd == "subform6A" || $model->cmd == "subform6_clear" || $model->cmd == "subform6_change") {
            if (isset($model->schregno) && !isset($model->warning) && $model->record_date != "") {
                $Row = $db->getRow(knje390mQuery::getSubQuery6($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field6;
            }
        } else {
            $Row =& $model->field6;
        }

        //作成履歴
        $extra = "onchange=\"return btn_submit('subform6_change')\"";
        $query = knje390mQuery::getRecordDate($model, "F");
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

        $extra = "";
        //支援なしできること
        $arg["data"]["CAN_BE_NO_SUPPORT"] = getTextOrArea($objForm, "CAN_BE_NO_SUPPORT", 13, 5, $Row["CAN_BE_NO_SUPPORT"]);
        //一部支援できること
        $arg["data"]["CAN_BE_SOME_SUPPORT"] = getTextOrArea($objForm, "CAN_BE_SOME_SUPPORT", 13, 5, $Row["CAN_BE_SOME_SUPPORT"]);
        //手立て
        $arg["data"]["MEANS"] = getTextOrArea($objForm, "MEANS", 13, 5, $Row["MEANS"]);
        //短期目標
        $arg["data"]["SHORT_TERM_GOAL"] = getTextOrArea($objForm, "SHORT_TERM_GOAL", 13, 5, $Row["SHORT_TERM_GOAL"]);
        //将来に目指す
        $arg["data"]["GOAL_FUTURE"] = getTextOrArea($objForm, "GOAL_FUTURE", 13, 5, $Row["GOAL_FUTURE"]);
        $arg["data"]["TEXT_SIZE"] = '<font size="2" color="red">(全角13文字X5行まで)</font>';

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
        View::toHTML($model, "knje390mSubForm6.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainCountData, $Row)
{
    //コピーして新規作成ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform6_copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_rireki", "コピーして新規作成", $extra.$disabled);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform6_updatemain');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform6_clear');\"";
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
