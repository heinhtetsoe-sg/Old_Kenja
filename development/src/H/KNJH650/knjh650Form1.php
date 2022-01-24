<?php

require_once('for_php7.php');

class knjh650Form1
{
    public function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh650index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $query = knjh650Query::getStdInfo($model);
        $stdInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $arg["HR_ATTEND"] = $stdInfo["HR_NAME"]." - ".$stdInfo["ATTENDNO"]."番";
        $arg["NAME"]      = $stdInfo["NAME"];
        $arg["BIRTHDAY"]  = str_replace('-', '/', $stdInfo["BIRTHDAY"]);

        //文理
        $query = knjh650Query::getNameMst("H316");
        $model->field["BUNRIDIV"] = $model->field["BUNRIDIV"] ? $model->field["BUNRIDIV"] : $stdInfo["BUNRIDIV"];
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["BUNRIDIV"], "BUNRIDIV", $extra, 1, "");

        //共通テスト欠席
        $model->field["CENTER_ABSENT"] = $model->field["CENTER_ABSENT"] ? $model->field["CENTER_ABSENT"] : $stdInfo["CENTER_ABSENT"];
        $extra = $model->field["CENTER_ABSENT"] == "1" ? " checked " : "";
        $arg["CENTER_ABSENT"] = knjCreateCheckBox($objForm, "CENTER_ABSENT", "1", $extra);

        //採点欠席
        $model->field["MARK_ABSENT"] = $model->field["MARK_ABSENT"] ? $model->field["MARK_ABSENT"] : $stdInfo["MARK_ABSENT"];
        $extra = $model->field["MARK_ABSENT"] == "1" ? " checked " : "";
        $arg["MARK_ABSENT"] = knjCreateCheckBox($objForm, "MARK_ABSENT", "1", $extra);

        //固定セット
        $model->subclassInfo = array();
        $model->subclassInfo["01"] = array("NAME" => "GAIKOKUGO", "WITH" => "95", "GOUKEI_NAME" => "英語合計");
        $model->subclassInfo["02"] = array("NAME" => "KOKUGO", "WITH" => "95", "GOUKEI_NAME" => "国語合計");
        $model->subclassInfo["03"] = array("NAME" => "SUUGAKU", "WITH" => "95", "GOUKEI_NAME" => "数学合計");
        $model->subclassInfo["04"] = array("NAME" => "SYAKAI", "WITH" => "75", "GOUKEI_NAME" => "社会合計");
        $model->subclassInfo["05"] = array("NAME" => "RIKA1", "WITH" => "75", "GOUKEI_NAME" => "理科合計");
        $model->subclassInfo["06"] = array("NAME" => "RIKA2", "WITH" => "75", "GOUKEI_NAME" => "理科合計");

        //データ表示
        setMeisaiData($objForm, $arg, $db, $model);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('updateMain');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "updEdit", "update");

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent2(0);";
        } elseif ($model->cmd == "clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"]  = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjh650Form1.html", $arg);
    }
}

function setMeisaiData(&$objForm, &$arg, $db, &$model)
{
    $query = knjh650Query::getScore($model);
    $result = $db->query($query);
    $model->scoreData = array();
    $model->totalScoreData = array();
    $model->englishCnt = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->scoreData[$row["CENTER_SUBCLASS_CD"]] = $row;
        $model->totalScoreData[$row["CENTER_CLASS_CD"]]["SCORE"] += $row["SCORE"];
        $model->totalScoreData[$row["CENTER_CLASS_CD"]]["PERFECT"] += $row["PERFECT"];
        $model->totalScoreData[$row["CENTER_CLASS_CD"]]["DAIITI"] += $row["DAIITI_SENTAKU"] == "1" ? 1 : 0;
        if ($row["CENTER_CLASS_CD"] == "01" && strlen($row["SCORE"]) > 0) {
            $model->englishCnt++;
        }
    }
    $result->free();

    //英語は２つ得点あれば200点換算
    if ($model->englishCnt >= 2) {
        $model->totalScoreData["01"]["SCORE"] = (int)$model->totalScoreData["01"]["SCORE"] * 0.8;
    }

    $query = knjh650Query::getSubclass($model);
    $result = $db->query($query);
    $renban = 1;
    $subclassCnt = 0;
    $checkSubclassCnt = 0;
    $befClas = "";
    $maxCnt = 0;
    $model->subclassCdArray = array();
    $focusCnt = 1;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->subclassCdArray[$row["CENTER_SUBCLASS_CD"]] = $row;
        if ($befClas != "" && $befClas != $row["CENTER_CLASS_CD"]) {
            $arg[$model->subclassInfo[$befClas]["NAME"]."_WITH"] = 50 + (int)$model->subclassInfo[$befClas]["WITH"] * $subclassCnt;
            $arg[$model->subclassInfo[$befClas]["NAME"]."_COL"] = $subclassCnt;
            if ($row["CENTER_CLASS_CD"] == "02") {
                $checkSubclassCnt = 0;
            }
            $maxCnt = $maxCnt > $checkSubclassCnt ? $maxCnt : $checkSubclassCnt;
            if ($row["CENTER_CLASS_CD"] != "06") {
                $checkSubclassCnt = 0;
            }
            $subclassCnt = 0;
            if ($row["CENTER_CLASS_CD"] == "02") {
                $setData["RENBAN"] = sprintf("%02d", $renban);
                $setData["PERFECT"] = $model->totalScoreData[$row["CENTER_CLASS_CD"]]["PERFECT"];
                $setData["SCORE"] = $model->totalScoreData[$row["CENTER_CLASS_CD"]]["SCORE"];
                $arg["KOKUGOZENNTAI"] = $setData;
                $renban++;
            }
        }
        $setData = $row;
        $setData["RENBAN"] = sprintf("%02d", $renban);
        $model->subclassInfo[$row["CENTER_CLASS_CD"]]["SUBCLASS_INFO"] = $row;

        //得点
        $nextFocus = (int)$focusCnt + 1;
        $extra = "style=\"text-align:right\" onkeydown=\"nextFocus({$nextFocus})\" onblur=\"this.value=toInteger(this.value); checkPerfect(this, {$model->scoreData[$row["CENTER_SUBCLASS_CD"]]["PERFECT"]});\"";
        $setData["SCORE"] = knjCreateTextBox($objForm, $model->scoreData[$row["CENTER_SUBCLASS_CD"]]["SCORE"], "SCORE_{$row["CENTER_SUBCLASS_CD"]}", 3, 3, $extra);
        //フォーカス移動
        knjCreateHidden($objForm, "NEXT_FOCUS{$focusCnt}", "SCORE_{$row["CENTER_SUBCLASS_CD"]}");
        $focusCnt++;

        //checkbox
        $extra = " id=\"CHECK_{$row["CENTER_SUBCLASS_CD"]}\" onClick=\"chengeChecked(this, 'CHECK_{$row["CENTER_CLASS_CD"]}', '{$row["CENTER_SUBCLASS_CD"]}')\" ";
        $checkd = $model->scoreData[$row["CENTER_SUBCLASS_CD"]]["DAIITI_SENTAKU"] == "1" ? " checked " : "";
        $setData["CHECK"] = knjCreateCheckBox($objForm, "CHECK_{$row["CENTER_SUBCLASS_CD"]}", "1", $extra.$checkd);

        $arg["CLASS".$row["CENTER_CLASS_CD"]][] = $setData;
        $befClas = $row["CENTER_CLASS_CD"];
        $subclassCnt++;
        $checkSubclassCnt++;
        $renban++;
    }
    $result->free();

    //最後の科目(RIKA2)
    $arg[$model->subclassInfo[$befClas]["NAME"]."_COL"] = $subclassCnt;

    $arg["ZENTAI_WITH"] = (int)$maxCnt * 100;
    $setSize = $maxCnt > 10 ? 82 : 83;
    $arg["GOUKEI_MARGIN"] = (int)$maxCnt * (int)$setSize;

    //合計欄
    $query = knjh650Query::getTotalScore($model);
    $result = $db->query($query);
    $setTotal = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setTotal[$row["CENTER_CLASS_CD"]] = $row;
    }
    $result->free();

    $setScore = 0;
    $setGoukei = 0;
    $setGoukeiData = array();
    foreach ($model->subclassInfo as $key => $val) {
        $setScore += (int)$setTotal[$key]["SCORE"];
        $setGoukei += (int)$setTotal[$key]["SCORE"];
        if ($key == "05") {
            continue;
        }
        $setGoukeiData["NAME"] = $val["GOUKEI_NAME"];
        $setGoukeiData["SCORE"] = $setScore;
        $arg["GOUKEI"][] = $setGoukeiData;
        $setScore = 0;
    }
    $setGoukeiData["NAME"] = "総合計";
    $setGoukeiData["SCORE"] = $setGoukei;
    $arg["GOUKEI"][] = $setGoukeiData;
}

//makeCmb
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
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
//得点
function setScore(&$objForm, &$arg, $name, $dataArray)
{
    $totalScore = 0;
    foreach ($dataArray as $key => $val) {
        $extra = "onblur=\"this.value=toInteger(this.value);\" class=\"scoreText\"";

        //得点
        $scoreField = $name."_SCORE".((int)$key+1);
        $arg[$scoreField] = knjCreateTextBox($objForm, $val["SCORE"], $scoreField, 3, 3, $extra);
        //合計
        $totalScore += (int)$val["SCORE"];
        //checkbox
        $checkField = $name."_CHECK".((int)$key+1);
        $extra = "";
        $arg[$checkField] = knjCreateCheckBox($objForm, $checkField, "1", $extra);
    }
    if ($name == "GAIKOKUGO" && strlen($dataArray[1]["SCORE"]) > 0) {
        $totalScore = $totalScore * 0.8;
    }
    $arg[$name."ZENNTAI_SCORE"] = $totalScore;
    $arg[$name."_TOTAL_SCORE"] = $totalScore;
    return $totalScore;
}
