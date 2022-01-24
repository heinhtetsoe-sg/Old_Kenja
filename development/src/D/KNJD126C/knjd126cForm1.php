<?php

require_once('for_php7.php');

class knjd126cForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd126cindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["IBYEAR"] = $model->field["IBYEAR"];

        /* 学年 */
        $query = knjd126cQuery::getGrade($model);
        $extra = "onChange = \"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["IBGRADE"], "IBGRADE", $extra, 1, "BLANK");

        /* 使用可能学期 */
        $query = knjd126cQuery::getAdminControlDat($model);
        $result = $db->query($query);
        $model->adminCtrl = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->adminCtrl[$row["CONTROL_CODE"]] = $row["CONTROL_CODE"];
        }
        $result->free();

        /* IBコース */
        $query = knjd126cQuery::getIbCourse();
        $extra = "onChange = \"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["IBPRG_COURSE"], "IBPRG_COURSE", $extra, 1, "BLANK");

        /* IB科目 */
        $query = knjd126cQuery::getIbSubclass($model);
        $extra = "onChange = \"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["IBSUBCLASSCD"], "IBSUBCLASSCD", $extra, 1, "BLANK");

        //タイトル(横)
        $titleYoko = setTitleYoko($objForm, $arg, $db, $model);

        if ($model->field["IBEVAL_DIV2"] !== "" && $model->field["IBEVAL_DIV2"] !== "") {
            //科目コンボ数
            $query = knjd126cQuery::getSubclass($model, "CNT");
            $model->subclassCnt = $db->getOne($query);

            $setSubChair = "";
            for ($subCnt = 1; $subCnt <= $model->subclassCnt; $subCnt++) {
                /* 科目 */
                $query = knjd126cQuery::getSubclass($model);
                $extra = "onChange = \"btn_submit('main')\"";
                $retSubclass = makeCmb2($objForm, $arg, $db, $query, $model->field["SUBCLASSCD".$subCnt], "SUBCLASSCD".$subCnt, $extra, 1, "BLANK");

                /* 講座 */
                $query = knjd126cQuery::getChair($model, $model->field["SUBCLASSCD".$subCnt]);
                $extra = "onChange = \"btn_submit('main')\"";
                $retChair = makeCmb2($objForm, $arg, $db, $query, $model->field["CHAIRCD".$subCnt], "CHAIRCD".$subCnt, $extra, 1, "BLANK");

                $setSubChair .= "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;科目：".$retSubclass."&nbsp;&nbsp;&nbsp;講座：".$retChair."<BR>";
            }
            $arg["SUBCLASS_CHAIR"] = $setSubChair;

            //タイトル(縦)
            $titleTate = setTitleTate($objForm, $arg, $db, $model);

            /* 編集対象データリスト */
            makeDataList($objForm, $arg, $db, $model, $titleYoko, $titleTate);
        }

        /* ボタン作成 */
        makeButton($objForm, $arg, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "subclassCnt", $model->subclassCnt);


        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjd126cForm1.html", $arg);
    }
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
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//makeCmb2
function makeCmb2(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//タイトル(横)
function setTitleYoko(&$objForm, &$arg, $db, &$model) {
    $subInstate = "";
    $subSep = "";
    for ($subCnt = 1; $subCnt <= $model->subclassCnt; $subCnt++) {
        /* 指定科目 */
        if ($model->field["SUBCLASSCD".$subCnt]) {
            $subInstate .= $subSep."'{$model->field["SUBCLASSCD".$subCnt]}'";
            $subSep = ",";
        }
    }

    $query = knjd126cQuery::getIbUnit($model, $subInstate);
    $result = $db->query($query);
    $titleYoko = array();
    $titleYokoCnt = 0;
    $titleYokoBefSeq = "";
    $viewArray = array("①", "②", "③", "④", "⑤");
    $viewCnt = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($titleYokoBefSeq != "" && $titleYokoBefSeq != $row["IBSEQ"]) {
            $titleYokoCnt++;
            $viewCnt = 0;
        }
        $titleYokoBefSeq = $row["IBSEQ"];
        $titleYoko[$titleYokoCnt]["IBSEQ"] = $row["IBSEQ"];
        $titleYoko[$titleYokoCnt]["IBYEAR"] = $row["IBYEAR"];
        $titleYoko[$titleYokoCnt]["IBGRADE"] = $row["IBGRADE"];
        $titleYoko[$titleYokoCnt]["SEMESTERCD"] = $row["SEMESTER"];
        $titleYoko[$titleYokoCnt]["SEMESTER"] = $row["SEMESTERNAME"];
        $titleYoko[$titleYokoCnt]["SUBCLASS"] = $row["SUBCLASSNAME"];
        $titleYoko[$titleYokoCnt]["EXECUTED"] = $row["EXECUTED"];
        $titleYoko[$titleYokoCnt]["SUBCLASSCD"] = $row["KEY_SUBCLASSCD"];
        $titleYoko[$titleYokoCnt]["VIEWCD"][] = $row["VIEWCD"];
        $titleYoko[$titleYokoCnt]["VIEW"][] = $viewArray[$viewCnt];
        $titleYoko[$titleYokoCnt]["VIEWNAME"][] = $row["VIEWNAME"];
        $viewCnt++;
    }
    $result->free();

    $titleYokoCnt = $viewCnt > 0 ? $titleYokoCnt + 1 : $titleYokoCnt;

    $titleYoko[$titleYokoCnt]["IBSEQ"] = "総合値";
    $titleYoko[$titleYokoCnt]["IBYEAR"] = $model->field["IBYEAR"];
    $titleYoko[$titleYokoCnt]["IBGRADE"] = $model->field["IBGRADE"];
    $titleYoko[$titleYokoCnt]["SEMESTER"] = $model->control["学期名"][9];
    $titleYoko[$titleYokoCnt]["SEMESTERCD"] = "8";
    $titleYoko[$titleYokoCnt]["SUBCLASS"] = "";
    $titleYoko[$titleYokoCnt]["SUBCLASSCD"] = "99-9-9-999999";
    $titleYoko[$titleYokoCnt]["VIEWCD"][] = $model->control["学期名"][9]."総合";
    $titleYokoCnt++;
    $titleYoko[$titleYokoCnt]["IBSEQ"] = "IBGrade";
    $titleYoko[$titleYokoCnt]["IBYEAR"] = $model->field["IBYEAR"];
    $titleYoko[$titleYokoCnt]["IBGRADE"] = $model->field["IBGRADE"];
    $titleYoko[$titleYokoCnt]["SEMESTER"] = "";
    $titleYoko[$titleYokoCnt]["SEMESTERCD"] = "9";
    $titleYoko[$titleYokoCnt]["SUBCLASS"] = "";
    $titleYoko[$titleYokoCnt]["SUBCLASSCD"] = "99-9-9-999999";
    $titleYoko[$titleYokoCnt]["VIEWCD"][] = "IBGrade";
    $titleYokoCnt++;


    $setTotalWidth = 0;
    $komaHaba = 50;
    $komaHaba2 = 100;
    $komaHaba3 = 100;
    $trStData = "<tr class=\"no_search\" align=\"center\" nowrap>";
    $trEdData = "</tr>";
    $setSeme = "";
    $setsubclass = "";
    $setHyouka = "";
    $setView = "";
    foreach ($titleYoko as $key => $val) {
        if ($val["SEMESTERCD"] == "1" || $val["SEMESTERCD"] == "8" || $val["SEMESTERCD"] == "9") {
            $setWith = $val["SEMESTERCD"] == "9" ? $komaHaba3 : $komaHaba2;
            $setSeme .= "<td width=\"{$setWith}\" height=\"35\">{$val["SEMESTER"]}</td>";
            $setsubclass .= "<td width=\"{$setWith}\" height=\"35\">{$val["SUBCLASS"]}</td>";
            $setUnit = $val["IBSEQ"];
            $setHyouka .= "<td width=\"{$komaHaba2}\" height=\"35\">{$setUnit}</td>";
            $setView .= "<td width=\"{$komaHaba2}\" height=\"35\"></td>";
        } else {
            $setCol = get_count($val["VIEW"]);
            $setWith = $komaHaba * $setCol;
            $setSeme .= "<td width=\"{$setWith}\" height=\"35\" colspan=\"{$setCol}\">{$val["SEMESTER"]}</td>";
            $setsubclass .= "<td width=\"{$setWith}\" height=\"35\" colspan=\"{$setCol}\">{$val["SUBCLASS"]}</td>";
            $setUnit = $model->field["IBPRG_COURSE"] == "M" ? "Unit".$val["IBSEQ"] : "Task".$val["IBSEQ"];
            $setHyouka .= "<td width=\"{$komaHaba}\" height=\"35\" colspan=\"{$setCol}\">{$setUnit}</td>";
            foreach ($val["VIEW"] as $viewKey => $viewVal) {
                //チップヘルプ
                $setView .= "<td width=\"{$komaHaba}\" height=\"35\" onMouseOver=\"ViewcdMousein(event, '{$val["VIEWNAME"][$viewKey]}')\" onMouseOut=\"ViewcdMouseout()\">{$viewVal}</td>";
            }
        }
        $setTotalWidth += $setWith;
    }
    $arg["TITLE_TOTALWIDTH"] = $setTotalWidth;
    $arg["TITLE"]["SEMESTER"] = $trStData.$setSeme.$trEdData;
    $arg["TITLE"]["SUBCLASS"] = $trStData.$setsubclass.$trEdData;
    $arg["TITLE"]["HYOUKA"] = $trStData.$setHyouka.$trEdData;
    $arg["TITLE"]["VIEW"] = $trStData.$setView.$trEdData;

    return $titleYoko;
}

//タイトル(縦)
function setTitleTate(&$objForm, &$arg, $db, &$model) {

    $chairInState = "";
    $sep = "";
    for ($subCnt = 1; $subCnt <= $model->subclassCnt; $subCnt++) {
        if ($model->field["CHAIRCD".$subCnt]) {
            $chairInState .= $sep."'{$model->field["CHAIRCD".$subCnt]}'";
            $sep = ",";
        }
    }

/*
    if (!$chairInState) {
        // 全講座
        $query = knjd126cQuery::getAllChair($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chairInState .= $sep."'{$row["CHAIRCD"]}'";
            $sep = ",";
        }
        $result->free();
    }
 */
    $model->schregNo = array();
    if ($chairInState) {
        $query = knjd126cQuery::getStudent($model, $chairInState);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $val) {
                $model->schregNo[$row["SCHREGNO"]][$key] = $val;
            }
        }
        $result->free();
    }

    $titleTate = array();
    $titleTateHenSyuu = array();
    $tateCnt = 0;

    $query = knjd126cQuery::getIbViewName($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $div12 = $row["IBEVAL_DIV1"].$row["IBEVAL_DIV2"];
        $div2Name = $row["Z036_NAME"];
        if ($row["IBEVAL_DIV1"] == "1") {
            $div2Name = $row["Z037_NAME"];
        } else if ($row["IBEVAL_DIV1"] == "2") {
            $div2Name = $row["Z038_NAME"];
        }
        $titleTateHenSyuu[$div12]["MARK"][] = $row["IBEVAL_MARK"];
        $titleTateHenSyuu[$div12]["DIV1_NAME"] = $row["Z035_NAME"];
        $titleTateHenSyuu[$div12]["DIV2_NAME"] = $div2Name;

        $titleTate[$tateCnt]["IBEVAL_MARK"] = $row["IBEVAL_MARK"];
        $titleTate[$tateCnt]["IBEVAL_DIV1"] = $row["IBEVAL_DIV1"];
        $titleTate[$tateCnt]["IBEVAL_DIV2"] = $row["IBEVAL_DIV2"];
        $tateCnt++;
    }
    $result->free();

    //合計
    $titleTate[$tateCnt]["IBEVAL_MARK"] = "合計値";
    $titleTate[$tateCnt]["IBEVAL_DIV1"] = "";
    $titleTate[$tateCnt]["IBEVAL_DIV2"] = "";
    $titleTateHenSyuu[$div12]["MARK"][] = "合計値";
    $titleTateHenSyuu[$div12]["DIV1_NAME"] = "";
    $titleTateHenSyuu[$div12]["DIV2_NAME"] = "";

    $trStData = "<tr bgcolor=\"#FFFFFF\" align=\"center\" nowrap>";
    $trEdData = "</tr>";
    $setCol = 0;
    $setDiv1 = "";
    $setDiv2 = "";
    $setView = "";
    $setHyouka = "";
    foreach ($model->schregNo as $schregNo => $schVal) {
        foreach ($titleTateHenSyuu as $hKey => $hVal) {
            $setCol = get_count($hVal["MARK"]) + 1;
            $setNenKumi = $schVal["HR_NAMEABBV"]."-".($schVal["ATTENDNO"] * 1)."<BR>(".$schregNo.")";
            $setDiv1 = "<td width=\"97px\" height=\"40\" rowspan=\"{$setCol}\">{$setNenKumi}</td>";
            $setDiv2 = "<td width=\"150px\" height=\"40\" rowspan=\"{$setCol}\">{$schVal["NAME"]}</td>";
            $setView = "";
            foreach ($hVal["MARK"] as $viewKey => $viewVal) {
                $setView .= $trStData;
                $setView .= "<td width=\"150px\" height=\"40\">{$viewVal}</td>";
                $setView .= $trEdData;
            }
            $setHyouka = $trStData.$setDiv1.$setDiv2.$setView.$trEdData;
            $arg["HYOUKA"]["IBEVAL_MARK"] .= $setHyouka;
        }
    }
    //入力完了
    $setNyuryoku = "<td width=\"450px\" height=\"40\" colspan=\"3\">入力完了</td>";
    $setHyouka = $trStData.$setNyuryoku.$trEdData;
    $arg["HYOUKA"]["IBEVAL_MARK"] .= $setHyouka;

    return $titleTate;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, &$model, $titleYoko, $titleTate) {

    $query = knjd126cQuery::getMaxSemester($model);
    $maxSeme = $db->getOne($query);

    $query = knjd126cQuery::getIbsubclassSelectDat($model);
    $notIbGradeCnt = $db->getOne($query);

    $ibGradeInputFlg = false;
    if ($notIbGradeCnt == 0 && $maxSeme == CTRL_SEMESTER) {
        $ibGradeInputFlg = true;
    }

    $query = knjd126cQuery::getIbsubclassUnitplanDat($model);
    $result = $db->query($query);
    $unitPlan = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setUnit = $row["IBYEAR"]."_".$row["IBGRADE"]."_".$row["IBEVAL_DIV1"]."_".$row["IBEVAL_DIV2"]."_".$row["IBEVAL_MARK"]."_".$row["IBSEQ"]."_".$row["VIEWCD"];
        $unitPlan[$setUnit] = $setUnit;
    }
    $result->free();

    $query = knjd126cQuery::getPerfectAll($model, $schregNo);
    $result = $db->query($query);
    $perfectAll = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $perfectKey = $row["IBEVAL_DIV1"]."-".$row["IBEVAL_DIV2"]."-".$row["IBEVAL_MARK"];
        $perfectAll[$perfectKey] = $row["IBPERFECT"];
    }
    $result->free();

    $setCnt = 0;
    $komaHaba = 50;
    $komaHaba2 = 95;
    $komaHaba3 = 110;
    $hiddenScoreName = "";
    $scoreNameSep = "";
    $hiddenSemeValueName = "";
    $semeValueNameSep = "";
    $hiddenGradeName = "";
    $gradeNameSep = "";
    foreach ($model->schregNo as $schregNo => $schVal) {

        $query = knjd126cQuery::getScoreAll($model, $schregNo);
        $result = $db->query($query);
        $scoreAll = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setScoreKey = $row["IBYEAR"]."-".$row["IBGRADE"]."-".$row["IBSEQ"]."-".$row["IBEVAL_DIV1"]."-".$row["IBEVAL_DIV2"]."-".$row["IBEVAL_MARK"]."-".$row["VIEWCD"];
            $scoreAll[$setScoreKey] = $row["SCORE"];
        }
        $result->free();

        $taskKei = array();
        $ibGradeFlg = false;
        foreach ($titleTate as $tkey => $tVal) {
            foreach ($titleYoko as $yKey => $yVal) {
                //textbox
                if ($yVal["SEMESTERCD"] == "1" || $yVal["SEMESTERCD"] == "8" || $yVal["SEMESTERCD"] == "9") {
                    $setVal = $schregNo."_".$yVal["IBYEAR"]."_".$yVal["IBGRADE"]."_".$tVal["IBEVAL_DIV1"]."_".$tVal["IBEVAL_DIV2"]."_".$tVal["IBEVAL_MARK"]."_".$yVal["SEMESTERCD"]."_0000";
                    if ($tVal["IBEVAL_MARK"] == "合計値") {
                        $setText = $taskKei[$yVal["IBSEQ"]];
                        $setVal = $schregNo."_".$yVal["IBSEQ"];
                        $setViewData[$setCnt]["SET_HYOUKA"] .= "<td width=\"{$komaHaba2}\" height=\"41\"><span id=\"{$setVal}\">{$setText}</span></td>";
                    } else if ($yVal["SEMESTERCD"] == "9") {
                        if (!$ibGradeFlg) {
                            $query = knjd126cQuery::getGradeVal($model, $schregNo, $yVal["IBYEAR"], $yVal["IBGRADE"], "0", "0", "00", $yVal["SEMESTERCD"], "0000");
                            $setScore = $db->getOne($query);

                            $setScoreName = "IBGRADE_".$setVal;
                            $ibTextRead = $ibGradeInputFlg ? "" : " readonly ";
                            $ibTextColor = $ibGradeInputFlg ? "" : " background-color:#999999; ";

                            $extra = " {$ibTextRead} onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right; {$ibTextColor}\"";
                            $setText = knjCreateTextBox($objForm, $setScore, $setScoreName, 2, 2, $extra);
                            $hiddenGradeName = $gradeNameSep.$setScoreName."@".$setScoreName;
                            $gradeNameSep = ":";
                            $rowSpan = get_count($titleTate) - 1;
                            $setViewData[$setCnt]["SET_HYOUKA"] .= "<td width=\"{$komaHaba3}\" rowspan=\"{$rowSpan}\">{$setText}</td>";
                            $ibGradeFlg = true;
                        }
                    } else {
                        $query = knjd126cQuery::getGradeVal($model, $schregNo, $yVal["IBYEAR"], $yVal["IBGRADE"], $tVal["IBEVAL_DIV1"], $tVal["IBEVAL_DIV2"], $tVal["IBEVAL_MARK"], $yVal["SEMESTERCD"], "0000");
                        $setScore = $db->getOne($query);

                        $perfectKey = $tVal["IBEVAL_DIV1"]."-".$tVal["IBEVAL_DIV2"]."-".$tVal["IBEVAL_MARK"];
                        $setPerfect = $perfectAll[$perfectKey];

                        $taskKei[$yVal["IBSEQ"]] = $taskKei[$yVal["IBSEQ"]] + $setScore;

                        $textRead =  "";
                        $textColor = "";
                        if ($yVal["SEMESTERCD"] == "8" || $model->adminCtrl[$yVal["SEMESTERCD"]] == "") {
                            $textRead = $ibGradeInputFlg ? "" : " readonly ";
                            $textColor = $ibGradeInputFlg ? "" : " background-color:#999999; ";
                        }

                        $extra = " STYLE=\"text-align:right;{$textColor}\" onChange=\"this.style.background='#ccffcc'\" onblur=\"checkScore(this, '{$setPerfect}')\"";
                        $setScoreName = "SEMEVAL_".$setVal;
                        $hiddenSemeValueName .= $semeValueNameSep.$setScoreName."@".$setScoreName;
                        $semeValueNameSep = ":";
                        $setText = knjCreateTextBox($objForm, $setScore, $setScoreName, 2, 2, $extra.$textRead);
                        $setViewData[$setCnt]["SET_HYOUKA"] .= "<td width=\"{$komaHaba2}\" height=\"40\">{$setText}</td>";
                    }
                } else {
                    foreach ($yVal["VIEWCD"] as $key => $val) {
                        $setVal = $schregNo."_".$yVal["IBYEAR"]."_".$yVal["IBGRADE"]."_".$tVal["IBEVAL_DIV1"]."_".$tVal["IBEVAL_DIV2"]."_".$tVal["IBEVAL_MARK"]."_".$yVal["IBSEQ"]."_".$val;
                        if ($tVal["IBEVAL_MARK"] == "合計値") {
                            $setText = $taskKei[$yVal["IBSEQ"]][$val];
                            $setVal = $schregNo."_".$yVal["IBSEQ"];
                            $setViewData[$setCnt]["SET_HYOUKA"] .= "<td width=\"{$komaHaba}\" height=\"41\"><span id=\"{$setVal}\">{$setText}</span></td>";
                        } else {
                            $setScoreKey = $yVal["IBYEAR"]."-".$yVal["IBGRADE"]."-".$yVal["IBSEQ"]."-".$tVal["IBEVAL_DIV1"]."-".$tVal["IBEVAL_DIV2"]."-".$tVal["IBEVAL_MARK"]."-".$val;
                            $setScore = $scoreAll[$setScoreKey];

                            $perfectKey = $tVal["IBEVAL_DIV1"]."-".$tVal["IBEVAL_DIV2"]."-".$tVal["IBEVAL_MARK"];
                            $setPerfect = $perfectAll[$perfectKey];

                            $taskKei[$yVal["IBSEQ"]][$val] = $taskKei[$yVal["IBSEQ"]][$val] + $setScore;

                            $textRead =  "";
                            $textColor = "";
                            if ($yVal["IBYEAR"] == $model->field["IBYEAR"]) {
                                if (!$unitPlan[$yVal["IBYEAR"]."_".$yVal["IBGRADE"]."_".$tVal["IBEVAL_DIV1"]."_".$tVal["IBEVAL_DIV2"]."_".$tVal["IBEVAL_MARK"]."_".$yVal["IBSEQ"]."_".$val]
                                    ||
                                    $model->adminCtrl[$yVal["SEMESTERCD"]] == ""
                                ) {
                                    $textRead = " readonly ";
                                    $textColor = " background-color:#999999; ";
                                }
                            } else {
                                $textRead = " readonly ";
                                $textColor = " background-color:#999999; ";
                            }

                            $extra = " STYLE=\"text-align:right;{$textColor}\" onChange=\"this.style.background='#ccffcc'\" onblur=\"checkScore(this, '{$setPerfect}')\"";
                            if ($yVal["SEMESTERCD"] == "1" || $yVal["SEMESTERCD"] == "8") {
                                $setScoreName = "SEMEVAL_".$setVal;
                                $hiddenSemeValueName .= $semeValueNameSep.$setScoreName."@".$setScoreName;
                                $semeValueNameSep = ":";
                            } else {
                                $setScoreName = "SCORE_".$setVal;
                                $model->scoreName[$setScoreName] = $setScoreName;
                                $hiddenScoreName .= $scoreNameSep.$setScoreName."@".$setScoreName;
                                $scoreNameSep = ":";
                            }
                            $setText = knjCreateTextBox($objForm, $setScore, $setScoreName, 2, 2, $extra.$textRead);
                            $setViewData[$setCnt]["SET_HYOUKA"] .= "<td width=\"{$komaHaba}\" height=\"40\">{$setText}</td>";
                        }
                    }
                }

            }
            $setCnt++;
        }
    }
    //hidden
    knjCreateHidden($objForm, "ScoreName", $hiddenScoreName);
    knjCreateHidden($objForm, "SemeValueName", $hiddenSemeValueName);
    knjCreateHidden($objForm, "GradeName", $hiddenGradeName);

    $hiddenChkFinName = "";
    $chkFinNameSep = "";
    foreach ($titleYoko as $yKey => $yVal) {
        if ($yVal["SEMESTERCD"] != "1" && $yVal["SEMESTERCD"] != "8" && $yVal["SEMESTERCD"] != "9") {
            $setVal = $yVal["IBYEAR"]."_".$yVal["IBGRADE"]."_".$yVal["IBSEQ"]."_0000";
            $setChkFinName = "CHKFIN_".$setVal;
            //checkbox
            $setCol = get_count($yVal["VIEW"]);
            $extra = $yVal["EXECUTED"] == "1" ? " checked " : "";
            $setChkFin = knjCreateCheckBox($objForm, $setChkFinName, "1", $extra);
            $setViewData[$setCnt]["SET_HYOUKA"] .= "<td width=\"{$komaHaba}\" height=\"40\" colspan=\"{$setCol}\">{$setChkFin}</td>";

            $hiddenChkFinName = $chkFinNameSep.$setChkFinName."@".$setChkFinName;
            $chkFinNameSep = ":";
        } else {
            $setViewData[$setCnt]["SET_HYOUKA"] .= "<td width=\"{$komaHaba}\" height=\"40\"></td>";
        }
    }
    knjCreateHidden($objForm, "ChkFinName", $hiddenChkFinName);

    $arg["IFRAME"] = View::setIframeJs();
    $arg["unit_data"] = $setViewData;

}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //保存ボタン
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeMethod();\"");
}
?>
