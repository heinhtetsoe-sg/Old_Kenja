<?php

require_once('for_php7.php');
class knje390mSubAssessTorikomi
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

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //アセスメント表項目名設定取得
        $query = knje390mQuery::getChallengedAssessmentStatusGrowupDat($model);
        $result = $db->query($query);
        $itemNameArrayYoko = array();
        $itemNameArrayTate = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["DATA_DIV"] == "0") {
                $itemNameArrayYoko["0"]["SHEET_PATTERN"] = $row["SHEET_PATTERN"];
                $itemNameArrayYoko["0"]["STATUS_NAME"] = $row["STATUS_NAME"];
                $itemNameArrayYoko["0"]["GROWUP_NAME"] = $row["GROWUP_NAME"];
            } else {
                $itemNameArrayTate[$row["DATA_DIV"]] = $row["DATA_DIV_NAME"];
            }
        }

        //枠パターン(2枠)
        if ($itemNameArrayYoko["0"]["SHEET_PATTERN"] == "2") {
            $arg["WIN_PATTERN2"] = "1";
            $windowPattern = "2";
        } else {
            $arg["WIN_PATTERN1"] = "1";
            $windowPattern = "1";
        }
        knjCreateHidden($objForm, "SHEET_PATTERN", $itemNameArrayYoko["0"]["SHEET_PATTERN"]);

        //アセスメント表データ取得
        $dataCount = 0;
        $itemData = array();
        $query = knje390mQuery::getAssessmentGSData($model);
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $itemData[$row2["DATA_DIV"]] = $row2;
        }
        if (isset($itemNameArrayYoko["0"])) {
            $arg["data"]["STATUS_NAME"] = $itemNameArrayYoko["0"]["STATUS_NAME"];
            $arg["data"]["GROWUP_NAME"] = $itemNameArrayYoko["0"]["GROWUP_NAME"];

            $extra = "";
            $arg["data"]["CHECK_YOKO1"] = knjCreateCheckBox($objForm, "CHECK_YOKO", "STATUS", $extra, "1");
            $arg["data"]["CHECK_YOKO2"] = knjCreateCheckBox($objForm, "CHECK_YOKO", "GROWUP", $extra, "1");

            foreach ($itemNameArrayTate as $dataDiv => $tateName) {
                $itemData[$dataDiv]["DATA_DIV_NAME"] = $tateName;
                $statusFormName = "DIV".$dataDiv."_STATUS";
                $growupFormName = "DIV".$dataDiv."_GROWUP";

                $extra = "";
                $itemData[$dataDiv]["CHECK_TATE"]           = knjCreateCheckBox($objForm, "CHECK_TATE", $dataDiv, $extra, "1");

                if ($windowPattern == "2") {
                    $itemData[$dataDiv]["STATUS"]           = getTextOrArea($objForm, $statusFormName, 25, 30, $itemData[$dataDiv]["STATUS"], $model);
                    $itemData[$dataDiv]["GROWUP"]           = getTextOrArea($objForm, $growupFormName, 15, 30, $itemData[$dataDiv]["GROWUP"], $model);
                } else {
                    $itemData[$dataDiv]["STATUS"]           = getTextOrArea($objForm, $statusFormName, 40, 30, $itemData[$dataDiv]["STATUS"], $model);
                }
                $arg["list"][] = $itemData[$dataDiv];

                $dataCount++;
            }
        }

        //クリアボタンを作成する
        $extra = "onclick=\"return btn_submit('".$dataCount."')\"";
        $arg["button"]["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $mainExistsFlg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubAssessTorikomi.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $mainExistsFlg)
{
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
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
    knjCreateHidden($objForm, "ASSESS_TORIKOMI_PARENT", $model->parantGamen);
    knjCreateHidden($objForm, "ASSESS_TORIKOMI_TARGET", $model->torikomiTarget);
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
        $extra = " readonly style=\"height:".$height."px; overflow:auto;\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = " readonly onkeypress=\"btn_keypress();\" id=\"".$name."\"";
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
