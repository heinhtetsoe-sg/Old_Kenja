<?php

require_once('for_php7.php');

class knjd131mForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd131mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期コンボ
        $query = knjd131mQuery::getSemester();
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            for ($idx = 1; $idx <= 6; $idx++) {
                $model->field["SCHOOLEVENT_NAME{$idx}"] = "";
                $model->field["SCHOOLEVENT_ATTEND{$idx}"] = "";
            }

            $row = $db->getRow(knjd131mQuery::getHreportremarkDat($model, $model->schregno), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //画面切替
        if ($model->semester == "1" || $model->semester == "2") {
            $arg["semester_1_2"] = 1;
        }
        if ($model->semester == "3") {
            $arg["semester_3"] = 1;
        }

        //備考
        $arg["data"]["COMMUNICATION"] = getTextOrArea($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $row["COMMUNICATION"], $model);
        setInputChkHidden($objForm, "COMMUNICATION", $model->getPro["COMMUNICATION"]["moji"], $model->getPro["COMMUNICATION"]["gyou"], $arg);

        $optSchoolevent = array();
        $optSchoolevent[] = array('label' => "", 'value' => "");
        foreach ($model->arySchooleventAttend as $key => $val) {
            $optSchoolevent[] = array('label' => $val, 'value' => $key);
        }
        $extra = "";

        //学校行事の記録
        $arySpecialactremark = explode("\r\n", $row["SPECIALACTREMARK"]);
        $idx = 1;
        foreach ($arySpecialactremark as $val) {
            list($schooleventName, $schooleventAttend) = explode("　", $val);
            $model->field["SCHOOLEVENT_NAME{$idx}"] = $schooleventName;
            $keyAttend = array_search($schooleventAttend, $model->arySchooleventAttend);
            if (!$keyAttend) {
                $keyAttend = "";
            }
            $model->field["SCHOOLEVENT_ATTEND{$idx}"] = $keyAttend;
            $idx++;
        }

        for ($idx = 1; $idx <= 6; $idx++) {
            $arg["data"]["SCHOOLEVENT_IDX{$idx}"] = $idx;
            //学校行事の記録 名称
            $arg["data"]["SCHOOLEVENT_NAME{$idx}"] = getTextOrArea($objForm, "SCHOOLEVENT_NAME{$idx}", $model->getPro["SCHOOLEVENT_NAME{$idx}"]["moji"], $model->getPro["SCHOOLEVENT_NAME{$idx}"]["gyou"], $model->field["SCHOOLEVENT_NAME{$idx}"], $model);
            setInputChkHidden($objForm, "SCHOOLEVENT_NAME{$idx}", $model->getPro["SCHOOLEVENT_NAME{$idx}"]["moji"], $model->getPro["SCHOOLEVENT_NAME{$idx}"]["gyou"], $arg);

            //学校行事の記録 参加
            $arg["data"]["SCHOOLEVENT_ATTEND{$idx}"] = knjCreateCombo($objForm, "SCHOOLEVENT_ATTEND{$idx}", $model->field["SCHOOLEVENT_ATTEND{$idx}"], $optSchoolevent, $extra, 1);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden作成
        makeHidden($objForm, $model);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } elseif ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        $arg["finish"] = $objForm->get_finish();

        View::toHTML5($model, "knjd131mForm1.html", $arg);
    }
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db)
{
    //更新ボタン
    $disable = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "" : "disabled";
    $extra = $disable." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //更新後前後の生徒へ
    if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
    } else {
        $extra = "disabled style=\"width:130px\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
    }

    //取消ボタン
    $extra = " onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"btnEnd();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model)
{
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
        $extra = "style=\"height:".$height."px;\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        // $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), ($moji * 2), $extra);
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
