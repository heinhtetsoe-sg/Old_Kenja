<?php

//ファイルアップロードオブジェクト
require_once('for_php7.php');
require_once("csvfile.php");

class knjh720Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        $arg["start"] = $objForm->get_start("main", "POST", "knjh720index.php", "", "main");

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学力テスト区分コンボ
        $extra = "aria-label=\"学力テスト区分\" id=\"TESTDIV\" onChange=\"current_cursor('TESTDIV');btn_submit('testdiv');\"";
        $query = knjh720Query::getNameMst("H320");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //教科コンボボックス
        $extra = "aria-label=\"教科\" id=\"CLASSCD\" onChange=\"current_cursor('CLASSCD');btn_submit('classcd');\"";
        $query = knjh720Query::getClasscd();
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], $extra, 1, "blank");

        //科目コンボ
        $extra = "aria-label=\"科目\" id=\"SUBCLASSCD\" onChange=\"current_cursor('SUBCLASSCD');btn_submit('subclasscd');\"";
        $query = knjh720Query::getSubclassMst($model);
        makeCmbSubclass($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //教室コンボボックス
        $extra = "aria-label=\"教室\" id=\"FACCD\" onChange=\"current_cursor('FACCD');btn_submit('faccd');\"";
        $query = knjh720Query::getFacility($model);
        makeCmb($objForm, $arg, $db, $query, "FACCD", $model->field["FACCD"], $extra, 1, "blank");

        //入力回数ラジオボタン
        $opt = array(1, 2);
        $model->field["INPUT_NUM"] = ($model->field["INPUT_NUM"] == "") ? "1" : $model->field["INPUT_NUM"];
        $extra = array();
        foreach ($opt as $key => $val) {
            if ($val == 1) {
                array_push($extra, " aria-label=\"入力1回目\" id=\"INPUT_NUM{$val}\" onclick=\"changeInputNum();\"");
            } else {
                array_push($extra, " aria-label=\"入力2回目\" id=\"INPUT_NUM{$val}\" onclick=\"changeInputNum();\"");
            }
        }
        $radioArray = knjCreateRadio($objForm, "INPUT_NUM", $model->field["INPUT_NUM"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //明細データ作成
        makeMeisai($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjh720Form1.html", $arg);
    }
}

//明細データ
function makeMeisai(&$objForm, &$arg, $db, &$model)
{
    //初期化
    $model->data = array();
    $counter = 0;

    //一覧表示
    $query = knjh720Query::getScore($model);
    $result = $db->query($query);
    $scoreArray = array();
    $foot = array();

    $result = $db->query(knjh720Query::getScore($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号をHiddenで保持
        knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);

        //書出用CSVデータ
        $csv = array(CTRL_YEAR,
                     $model->field["TESTDIV"],
                     $model->field["CLASSCD"],
                     $model->field["SUBCLASSCD"],
                     $row["SCHREGNO"],
                     $row["NAME"]);

        if ($model->field["INPUT_NUM"] == "1") {
            $csv[] = $row["SCORE_1"];
            $csv[] = $row["ABSENCE_FLG_1"];
        } else {
            $csv[] = $row["SCORE_2"];
            $csv[] = $row["ABSENCE_FLG_2"];
        }

        //性別は男性に○をつける
        $row["SEX"] = $row["SEX"] == "1" ? "〇" : "";

        //満点
        $perfect = 100;

        //CSV取り込みデータがある場合は上書きする(表示のみで登録はしない)
        if ($model->dataArr) {
            for ($csvRow = 0; $csvRow < get_count($model->dataArr); $csvRow++) {
                if ($row["SCHREGNO"] == $model->dataArr[$csvRow]["SCHREGNO"]) {
                    if ($model->field["INPUT_NUM"] == "1") {
                        $row["SCORE_1"]       = $model->dataArr[$csvRow]["SCORE"];
                        $row["ABSENCE_FLG_1"] = $model->dataArr[$csvRow]["ABSENCE"];
                    } else {
                        $row["SCORE_2"]       = $model->dataArr[$csvRow]["SCORE"];
                        $row["ABSENCE_FLG_2"] = $model->dataArr[$csvRow]["ABSENCE"];
                    }
                }
            }
        }

        //素点1
        $col = "SCORE1";
        $extra  = " onKeyDown=\"keyChangeEntToTab2(this, '{$col}-', {$counter})\"; STYLE=\"text-align: right;\"";
        $extra .= " onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\"";
        $extra .= " onPaste=\"return showPaste(this);\" id=\"{$col}-{$counter}\"";
        if ($model->field["INPUT_NUM"] == "2") {
            //入力2回目の場合は入力不可にする
            $extra .= "disabled";
        }
        $row["SCORE1"] = knjCreateTextBox($objForm, $row["SCORE_1"], $col."-".$counter, 3, 3, $extra);
        //満点チェック用
        knjCreateHidden($objForm, $col."_PERFECT"."-".$counter, $perfect);

        if (is_numeric($row["SCORE_1"])) {
            if (empty($foot["MAX1"]) || $foot["MAX1"] < $row["SCORE_1"]) {
                $foot["MAX1"] = $row["SCORE_1"];
            }
            if (empty($foot["MIN1"]) || $row["SCORE_1"] < $foot["MIN1"]) {
                $foot["MIN1"] = $row["SCORE_1"];
            }
            $foot["SUM1"] += $row["SCORE_1"];
            $foot["CNT1"] ++;
        }

        //欠席1
        if ($model->field["INPUT_NUM"] == "2") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        if ($row["ABSENCE_FLG_1"] == "1") {
            $extra .= " checked";
            $foot["ABSENCE1"] ++;
        }
        $row["ABSENCE1"] = knjCreateCheckBox($objForm, "ABSENCE1-".$counter, "1", $extra);

        //素点2
        $col = "SCORE2";
        $extra  = " onKeyDown=\"keyChangeEntToTab2(this, '{$col}-', {$counter})\"; STYLE=\"text-align: right;\"";
        $extra .= " onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\"";
        $extra .= " onPaste=\"return showPaste(this);\" id=\"{$col}-{$counter}\"";
        if ($model->field["INPUT_NUM"] == "1") {
            //入力1回目の場合は入力不可にする
            $extra .= "disabled";
        }
        $row["SCORE2"] = knjCreateTextBox($objForm, $row["SCORE_2"], $col."-".$counter, 3, 3, $extra);
        //満点チェック用
        knjCreateHidden($objForm, $col."_PERFECT"."-".$counter, $perfect);

        if (is_numeric($row["SCORE_2"])) {
            if (empty($foot["MAX2"]) || $foot["MAX2"] < $row["SCORE_2"]) {
                $foot["MAX2"] = $row["SCORE_2"];
            }
            if (empty($foot["MIN2"]) || $row["SCORE_2"] < $foot["MIN2"]) {
                $foot["MIN2"] = $row["SCORE_2"];
            }
            $foot["SUM2"] += $row["SCORE_2"];
            $foot["CNT2"] ++;
        }

        //欠席2
        if ($model->field["INPUT_NUM"] == "1") {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        if ($row["ABSENCE_FLG_2"] == "1") {
            $extra .= " checked";
            $foot["ABSENCE2"] ++;
        }
        $row["ABSENCE2"] = knjCreateCheckBox($objForm, "ABSENCE2-".$counter, "1", $extra);

        //1回目・2回目の整合性チェック
        if ((isset($row["STAFF1"]) && isset($row["STAFF2"]))
            && (($row["SCORE_1"] != $row["SCORE_2"]) || ($row["ABSENCE_FLG_1"] != $row["ABSENCE_FLG_2"]))) {
            $unmatchFlg = 1;
        } else {
            $unmatchFlg = 0;
        }

        //"#ffc0cb"  RGB＝255:192:203
        $row["COLOR"] = $unmatchFlg == 1 ? "#ffc0cb" : "#ffffff";
        $counter++;
        $arg["data"][] = $row;
    }
    $result->free();
    //件数
    knjCreateHidden($objForm, "COUNTER", $counter);
    //平均
    if (0 < $foot["CNT1"]) {
        $arg["SCORE_AVG1"] = round(($foot["SUM1"] / $foot["CNT1"]) * 10) / 10;
    }
    if (0 < $foot["CNT2"]) {
        $arg["SCORE_AVG2"] = round(($foot["SUM2"] / $foot["CNT2"]) * 10) / 10;
    }
    //最高点
    $arg["SCORE_MAX1"] = $foot["MAX1"];
    $arg["SCORE_MAX2"] = $foot["MAX2"];
    //最低点
    $arg["SCORE_MIN1"] = $foot["MIN1"];
    $arg["SCORE_MIN2"] = $foot["MIN2"];
    //欠席者数
    $arg["TOTAL_ABSENCE1"] = $foot["ABSENCE1"];
    $arg["TOTAL_ABSENCE2"] = $foot["ABSENCE2"];

    //CSV
    //CSV取込、書出ラジオボタン
    $opt = array(1, 2);
    $extra = array();
    array_push($extra, " aria-label=\"取込\" id=\"RADIO1\" checked");
    array_push($extra, " aria-label=\"書出\" id=\"RADIO2\"");
    $radioArray = knjCreateRadio($objForm, "RADIO", "", $extra, $opt, count($opt));
    foreach ($radioArray as $key => $val) {
        $arg["CSV"][$key] = $val;
    }
    //ファイルからの取り込み
    $arg["CSV"]["FILE"] = knjCreateFile($objForm, "FILE", "", "4096000");
    //ヘッダ有チェックボックス
    if ($model->field["HEADER_CHECK"] == "on") {
        $check_header = "checked";
    } else {
        $check_header = ($model->cmd == "") ? "checked" : "";
    }
    $extra = " id=\"HEADER_CHECK\"";
    $arg["CSV"]["HEADER_CHECK"] = knjCreateCheckBox($objForm, "HEADER_CHECK", "on", $check_header.$extra, "");
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $cnt = 0;
    if ($blank == "blank") {
        $opt[] = array('label' => "", 'value' => "");
        $cnt++;
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//科目コンボ作成
function makeCmbSubclass(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $cnt = 0;
    if ($blank == "blank") {
        $opt[] = array('label' => "", 'value' => "");
        $cnt++;
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $label = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"]."：".$row["SUBCLASSNAME"];
        $opt[] = array('label' => $label,
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = " aria-label=\"更新\" id=\"btn_exec\" onclick=\"current_cursor('btn_exec');return btn_submit('exec');\"";
    $arg["CSV"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実行", $extra);
    //更新ボタン
    $extra = " aria-label=\"更新\" id=\"btn_update\" onclick=\"current_cursor('btn_update');return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = " aria-label=\"取消\" id=\"btn_reset\" onclick=\"current_cursor('btn_reset');return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = " aria-label=\"終了\" onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "STAFF", STAFFCD);
    //データ保持
    knjCreateHidden($objForm, "H_TESTDIV");
    knjCreateHidden($objForm, "H_CLASSCD");
    knjCreateHidden($objForm, "H_SUBCLASSCD");
    knjCreateHidden($objForm, "H_FACCD");
    //更新権限チェック
    knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
    knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}
