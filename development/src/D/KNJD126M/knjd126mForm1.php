<?php

require_once('for_php7.php');

class knjd126mForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd126mindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* ログイン年度 */
        $arg["YEAR"] = CTRL_YEAR;

        /* ログイン学期 */
        $arg["LOGIN_SEMESTER"] = $model->control_data["学期名"][CTRL_SEMESTER];

        /**********/
        /* コンボ */
        /**********/
        $setupFlgCheck = " onclick=\"return setupFlgCheck();\" ";
        /* 年組 */
        $query = knjd126mQuery::selectGradeHrClass($model);
        $extra = "onChange=\"btn_submit('changeGradeHrClass')\";" .$setupFlgCheck;
        $retFirstVal = makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "BLANK");
        /* 教科名 */
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        if (!$grade) {
            list($grade, $hrClass) = explode("-", $retFirstVal);
        }

        $query = knjd126mQuery::selectSchoolKind($grade);
        $model->schooLkind = $db->getOne($query);
        if ($model->schooLkind == "H") {
            $arg["SUBCLASS_TITLE"] = "科目名";
        } else {
            $arg["SUBCLASS_TITLE"] = "教科名";
        }
        $query = knjd126mQuery::selectSubclasscd($model);
        $extra = "onChange=\"btn_submit('changeSubclasscd')\";" .$setupFlgCheck;
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        /* 講座 */
        $query = knjd126mQuery::getChair($model);
        $extra = "onChange=\"btn_submit('changeSubclasscd')\";" .$setupFlgCheck;
        makeCmb($objForm, $arg, $db, $query, $model->field["CHAIRCD"], "CHAIRCD", $extra, 1, "BLANK");

        /* 処理学期 */
        $query = knjd126mQuery::selectSemester($model);
        $extra = "onChange=\"btn_submit('changeSemester')\";" .$setupFlgCheck;
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "BLANK");

        //観点設定（テスト単元）
        $unitViewList = setViewData($objForm, $arg, $db, $model);

        //観点設定（学期）
        $semeViewList = setViewDataSeme($objForm, $arg, $db, $model, $unitViewList);

        //合計欄
        setTotal($objForm, $arg, $db, $model);

        //コンボ選択したか？
        $isSelectCmb = strlen($model->field["SEMESTER"]) && strlen($model->field["GRADE_HR_CLASS"]) && strlen($model->field["SUBCLASSCD"]);

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model, $unitViewList, $semeViewList, $isSelectCmb);

        /* ボタン作成 */
        makeButton($objForm, $arg, $isSelectCmb, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "setupFlg", $model->setupFlg); //編集フラグ 1:編集中

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjd126mForm1.html", $arg);
    }
}

//観点設定（テスト単元）
function setViewData(&$objForm, &$arg, $db, &$model) {
    //表サイズ
    $allWidth = 0;
    $squareSetSize = 130;

    //テスト単元・観点一覧取得
    $unitViewList = array();
    $result = $db->query(knjd126mQuery::getUnitViewList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $unitViewList[$row["SEQ"]][] = $row;
    }
    $result->free();

    $setUnitSort = "";  //1)テスト実施順
    $setUnitName = "";  //2)テスト単元名
    $setView = "";      //3)観点略称
    $setHigh = "";      //4)配点
    $setOmomi = "";     //5)重み
    $hidUnitInfo = "";
    $model->totalHaiten  = array();
    $model->totalOmomi   = array();
    $model->seqViewOmomi = array();
    $model->seqViewOmomiBase = array();
    $model->seqViewHaiten = array();
    foreach ($unitViewList as $seq => $viewList) {
        $unitSort = "";
        $unitName = "";
        $total = 0;
        $reccnt = 1;
        $viewSeqHaitenCalTotal = 0;
        $viewSeqOmomiTotal = 0;
        foreach ($viewList as $view) {
            //1)テスト実施順　2)テスト単元名
            $unitSort = $view["SORT"];
            $unitDate = $view["UNIT_TEST_DATE"] == "" ? "<br>" : " (".$view["UNIT_TEST_DATE"].")";
            $unitName = $view["UNIT_L_NAME"]."<DIV>".str_replace("-", "/", $unitDate)."</DIV>";
            //3)観点略称　4)配点
            $fontSizeS = (mb_strlen($view["VIEWABBV"]) > 16) ? "<font style=\"font-size:9pt\">": "";
            $fontSizeF = (mb_strlen($view["VIEWABBV"]) > 16) ? "</font>": "";
            $setView .= "<td width=\"120\" >".$fontSizeS.$view["VIEWABBV"].$fontSizeF."</td>";
            $setHigh .= "<td width=\"120\" height=\"25\" nowrap align=\"center\">".$view["UNIT_ASSESSHIGH"]."</td>";
            $omomi = $view["WEIGHTING_MST"];
            if ($model->cmd == "calc") {
                $omomi = $view["WEIGHTING_CALC"];
            }
            $color = "#ffffff";
            if(!$view["WEIGHTING_EXE"]){
                $color = "#ffffff";
            } else {
                if($view["WEIGHTING_MST"] <> $view["WEIGHTING_EXE"]) $color = "#ff99cc";
            }
            if ($model->cmd == "calc") {
                if($view["WEIGHTING_EXE"]){
                    if($view["WEIGHTING_CALC"] <> $view["WEIGHTING_EXE"]) $color = "#ff99cc";
                }
            }
            $extra = "STYLE=\"text-align:right; background-color:{$color}\" id=\"OMOMI".$view["SORT"]."_".$view["SEQ"]."_".$view["VIEWCD"]."\"";
            if ($model->cmd == "calc") {
                $setDefVal = $model->unitvals[$view["VIEWCD"]][$view["SORT"]][$view["SEQ"]] != "" ? $model->unitvals[$view["VIEWCD"]][$view["SORT"]][$view["SEQ"]] : $view["WEIGHTING"];
                $extra .= " onchange=\"return setupFlgCheck2(this, '{$setDefVal}', '{$model->d086}');\"";
            } else {
                $extra .= " onchange=\"return inputCheck2(this, '{$model->d086}');\"";
            }
            $setOmomiWk = knjCreateTextBox($objForm, $omomi, "OMOMI".$view["SORT"]."_".$view["SEQ"]."_".$view["VIEWCD"], 3, 3, $extra);
            if ($model->d086 == "1") {
                $setCalcRes = $omomi && $view["UNIT_ASSESSHIGH"] ? round((int)$view["UNIT_ASSESSHIGH"]*(int)$omomi/100.0, 0) : "<br>";
            } else {
                $setCalcRes = $omomi && $view["UNIT_ASSESSHIGH"] ? round((int)$view["UNIT_ASSESSHIGH"]*(int)$omomi/100.0, 1) : "<br>";
                $setCalcRes = strpos($setCalcRes, '.') === false ? $setCalcRes.".0" : $setCalcRes;
            }
            $setOmomi .= "<td width=\"120\" nowrap align=\center\">".$setOmomiWk."<DIV id=\"CALC".$view["SORT"]."_".$view["SEQ"]."_".$view["VIEWCD"]."\">".$setCalcRes."</DIV></td>";
            //3)合計　4)配点合計
            $total += $view["UNIT_ASSESSHIGH"];
            $model->seqViewOmomi[$view["SEQ"]."_".$view["VIEWCD"]] = $view["UNIT_ASSESSHIGH"];
            $model->seqViewOmomiBase[$view["SEQ"]."_".$view["VIEWCD"]] = $omomi;

            //最終行出力用
            $model->totalHaiten[$view["SEMESTER"]] += $view["UNIT_ASSESSHIGH"];
            $model->totalOmomi[$view["SEMESTER"]]  += $setCalcRes;
            $model->seqViewHaiten[$view["SEMESTER"]."_".$view["VIEWCD"]] += $setCalcRes;
            $viewSeqHaitenCalTotal += $setCalcRes;
            $viewSeqOmomiTotal += $omomi;
            $model->totalHaiten["9"] += $view["UNIT_ASSESSHIGH"];
            $model->totalOmomi["9"]  += $setCalcRes;
            $model->seqViewHaiten["9_".$view["VIEWCD"]] += $setCalcRes;

            //表示サイズ
            $hidUnitInfo .= $view["SORT"]."_".$view["SEQ"]."_".$view["VIEWCD"].",";
            $hidAssesHigh .= $view["UNIT_ASSESSHIGH"].",";
            $hidOmomi .= $view["WEIGHTING_EXE"].",";
            $allWidth += $squareSetSize;
            $reccnt++;
        }
        //1)テスト実施順　2)テスト単元名
        $viewCnt = get_count($viewList) + 1;
        $setUnitSort .= "<td colspan={$viewCnt} nowrap>".$unitSort."</td>";
        $setUnitName .= "<td colspan={$viewCnt} nowrap>".$unitName."</td>";
        //3)合計　4)配点合計
        $setView .= "<td width=\"120\">合計</td>";
        $setHigh .= "<td width=\"120\" height=\"25\" nowrap>".$total."</td>";
        $setOmomiWk = "<br>".$viewSeqHaitenCalTotal;
        $setOmomi .= "<td width=120 nowrap >$setOmomiWk</td>";
        $allWidth += $squareSetSize;
    }

    $arg["HEAD1"] = $setUnitSort;
    $arg["HEAD2"] = $setUnitName;
    $arg["HEAD3"] = $setView;
    $arg["HEAD4"] = $setHigh;
    $arg["HEAD5"] = $setOmomi;

    //サイズ調整（幅）
    $arg["widthHeader"] = $allWidth;
    $arg["widthMeisai"] = $allWidth;

    knjCreateHidden($objForm, "HID_UNITINFO", $hidUnitInfo);
    knjCreateHidden($objForm, "HID_ASSESHIGH", $hidAssesHigh);
    knjCreateHidden($objForm, "HID_OMOMI", $hidOmomi);
    return $unitViewList;
}

//観点設定（学期）
function setViewDataSeme(&$objForm, &$arg, $db, &$model, $unitViewList) {
    //表サイズ
    $allWidth = 0;
    $squareSetSize = 130;

    //学期・観点一覧取得
    $semeViewList = array();
    $result = $db->query(knjd126mQuery::getSemeViewList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $semeViewList[$row["SEMESTER"]][] = $row;
    }
    $result->free();

    $setSemeName = "";  //1)学期名
    $setKotei = "";     //2)固定文字
    $setView = "";      //3)観点略称
    $setHigh = "";      //4)配点
    $setOmomi = "";     //5)重み
    foreach ($semeViewList as $semester => $viewList) {
        $semeName = "";
        $kotei = "";
        $total = 0;
        $reccnt = 1;
        foreach ($viewList as $view) {

            //1)学期名　2)固定文字
            $semeName = $view["SEMESTERNAME"];
            $kotei = "観点";
            //3)観点略称　4)配点
            $fontSizeS = (mb_strlen($view["VIEWABBV"]) > 16) ? "<font style=\"font-size:9pt\">": "";
            $fontSizeF = (mb_strlen($view["VIEWABBV"]) > 16) ? "</font>": "";
            $setView .= "<td width=\"120\">".$fontSizeS.$view["VIEWABBV"].$fontSizeF."</td>";
            $setHigh .= "<td width=\"120\" align=\"center\" height=\"25\">".$view["UNIT_ASSESSHIGH"]."</td>";
            $setOmomiWk = $model->seqViewHaiten[$semester."_".$view["VIEWCD"]];
            //$setOmomi = "<td width=\"120\" align=\"center\">{rval data/OMOMI".$reccnt."}</td>";
            $setOmomi .= "<td width=\"120\" align=\"center\">".$setOmomiWk."</td>";

            //表示サイズ
            $allWidth += $squareSetSize;
            $reccnt++;
        }
        //1)学期名　2)固定文字
        $viewCnt = get_count($viewList);
        $setSemeName .= "<td colspan={$viewCnt} nowrap>".$semeName."</td>";
        $setKotei    .= "<td colspan={$viewCnt} nowrap>".$kotei."</td>";
    }

    $arg["HEAD1"] .= $setSemeName;
    $arg["HEAD2"] .= $setKotei;
    $arg["HEAD3"] .= $setView;
    $arg["HEAD4"] .= $setHigh;
    $arg["HEAD5"] .= $setOmomi;

    //サイズ調整（幅）
    $allWidth   += (get_count($unitViewList) == 0 && get_count($semeViewList) == 0) ? $squareSetSize : 0;
    $widthHeader = (get_count($unitViewList) == 0 && get_count($semeViewList) == 0) ? 0 : 50;
    $arg["widthHeader"] += $allWidth + $widthHeader;
    $arg["widthMeisai"] += $allWidth;

    return $semeViewList;
}

//合計欄
function setTotal(&$objForm, &$arg, $db, &$model) {
    //表サイズ
    $allWidth = 0;
    $squareSetSize = 80;

    //学期・観点一覧取得
    $model->semesterArray = array();
    $query = knjd126mQuery::selectSemester($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->semesterArray[$row["VALUE"]] = $row;
    }
    $result->free();
    $model->semesterArray["9"] = array("VALUE" => "9", "LABEL" => "学年");

    $setSemeName = "";  //1)学期名
    $setKotei = "";     //2)固定文字
    $setView = "";      //3)観点略称
    $setHigh = "";      //4)配点
    $setOmomi = "";     //5)重み
    foreach ($model->semesterArray as $semester => $semeVal) {
        $semeName = "";
        $kotei = "";
        $total = 0;

        //合計
        $semeName = $semeVal["LABEL"]."計";
        $semeNameArray = preg_split("//u", $semeName, -1, PREG_SPLIT_NO_EMPTY);
        $semeName = "";
        $semeNameBr = "";
        foreach ($semeNameArray as $nameKey => $nameVal) {
            $semeName .= $semeNameBr.$nameVal;
            $semeNameBr = "<br>";
        }
        $setSemeName .= "<td width=\"40\" rowspan=\"3\">".$semeName."</td>";
        $setHigh  .= "<td width=\"40\" align=\"center\" height=\"25\">".$model->totalHaiten[$semester]."</td>";
        $setOmomi .= "<td width=\"40\" align=\"center\">".$model->totalOmomi[$semester]."</td>";

        //合計評価・評定
        $setHyouka = $semester == "9" ? "評定" : "評価";
        $semeName = $semeVal["LABEL"].$setHyouka;
        $semeNameArray = preg_split("//u", $semeName, -1, PREG_SPLIT_NO_EMPTY);
        $semeName = "";
        $semeNameBr = "";
        foreach ($semeNameArray as $nameKey => $nameVal) {
            $semeName .= $semeNameBr.$nameVal;
            $semeNameBr = "<br>";
        }
        $setSemeName .= "<td width=\"40\" rowspan=\"3\">".$semeName."</td>";
        $setHigh  .= "<td width=\"40\" align=\"center\" height=\"25\"></td>";
        $setOmomi .= "<td width=\"40\" align=\"center\"></td>";

        //表示サイズ
        $allWidth += $squareSetSize;
    }

    $arg["TOTALHEAD1"] .= $setSemeName;
    $arg["TOTALHEAD2"] .= $setHigh;
    $arg["TOTALHEAD3"] .= $setOmomi;

    //サイズ調整（幅）
    $arg["widthHeader"] += $allWidth;
    $arg["widthMeisai"] += $allWidth;

    return $semeViewList;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, &$model, $unitViewList, $semeViewList, $isSelectCmb) {
    //SCOREデータ取得
    $getData = array();
    $getScoreOmomi = array();
    $query = knjd126mQuery::getScoreData($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $name = "SCORE_".$row["SEQ"]."_".$row["VIEWCD"];
        $getData[$row["SCHREGNO"]][$name] = $row["SCORE"];
        $omomi = get_count($model->unitvals) > 0 && get_count($model->unitvals[$unitSort]) > 0 ? $model->unitvals[$unitSort][($reccnt-1)] : "";
        $getScoreOmomi[$row["SCHREGNO"]][$name] = $omomi == "" ? "" : $row["SCORE"] * ($omomi / 100.0);
    }
    $result->free();

    $model->d029 = array();
    $query = knjd126mQuery::getD029();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->d029[$row["ABBV1"]]= $row["NAMESPARE2"];
    }
    $result->free();

    //STATUSデータ取得
    $getDataStatus = array();
    $model->dankaiHyouka = array();
    //「1:編集中」・・・集計ボタンを押した時
    if ($isSelectCmb && $model->cmd == "calc") {
        //観点データ（集計）取得・・・処理学期のみ
        $query = knjd126mQuery::getCalcJviewstatRecord($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $name = "STATUS_".$row["SEMESTER"]."_".$row["VIEWCD"];
            //到達度を括弧表示　例：A (62.5)
            $toutatudo = "";
            if (strlen($row["STATUS"]) && strlen($row["REMARK3"])) {
                $toutatudo = " (".$row["REMARK3"]."%)";
            }
            $getDataStatus[$row["SCHREGNO"]][$name] = $row["STATUS"].$toutatudo;
            $model->dankaiHyouka[$row["SCHREGNO"]][$row["SEMESTER"]] += $model->d029[$row["STATUS"]];
        }
        //観点データ（DB）取得・・・処理学期以外
        $query = knjd126mQuery::getJviewstatRecord($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field["SEMESTER"] == $row["SEMESTER"]) continue;
            $name = "STATUS_".$row["SEMESTER"]."_".$row["VIEWCD"];
            //到達度を括弧表示　例：A (62.5)
            $toutatudo = "";
            if (strlen($row["STATUS"]) && strlen($row["REMARK3"])) {
                $toutatudo = " (".$row["REMARK3"]."%)";
            }
            $getDataStatus[$row["SCHREGNO"]][$name] = $row["STATUS"].$toutatudo;
            $model->dankaiHyouka[$row["SCHREGNO"]][$row["SEMESTER"]] += $model->d029[$row["STATUS"]];
        }
    } else {
        //観点データ（DB）取得・・・処理学期全て
        $query = knjd126mQuery::getJviewstatRecord($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $name = "STATUS_".$row["SEMESTER"]."_".$row["VIEWCD"];
            //到達度を括弧表示　例：A (62.5)
            $toutatudo = "";
            if (strlen($row["STATUS"]) && strlen($row["REMARK3"])) {
                $toutatudo = " (".$row["REMARK3"]."%)";
            }
            $getDataStatus[$row["SCHREGNO"]][$name] = $row["STATUS"].$toutatudo;
            $model->dankaiHyouka[$row["SCHREGNO"]][$row["SEMESTER"]] += $model->d029[$row["STATUS"]];
        }
    }

    //明細データ
    $counter  = 0;
    $colorFlg = false;
    $data = array();

    $model->totalSchregArray = array();
    $query = knjd126mQuery::getSchList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        /* テスト単元の表 */

        $setUnit = "";
        foreach ($unitViewList as $seq => $viewList) {
            $setScore = "";
            $setTotal = "";
            $total = 0;
            foreach ($viewList as $view) {
                //データ取得
                $name = "SCORE_".$seq."_".$view["VIEWCD"];
                $row[$name] = $getData[$row["SCHREGNO"]][$name];
                //得点
                $score = $row[$name];
                //縦計
                if (is_numeric($score)) {
                   $term_data[$name][] = $score;
                }
                //合計
                $total += $score;
                $calOmomi = $model->seqViewOmomiBase[$seq."_".$view["VIEWCD"]];
                if ($model->d086 == "1") {
                    $setCalcRes = $calOmomi && $score ? round((int)$score*(int)$calOmomi/100.0, 0) : "0";
                } else {
                    $setCalcRes = $calOmomi && $score ? round((int)$score*(int)$calOmomi/100.0, 1) : "0";
                    $setCalcRes = strpos($setCalcRes, '.') === false ? $setCalcRes.".0" : $setCalcRes;
                }
                $model->totalSchregArray[$row["SCHREGNO"]][$view["SEMESTER"]]["SCORE"] += $setCalcRes;
                $model->totalSchregArray[$row["SCHREGNO"]]["9"]["SCORE"] += $setCalcRes;
                $setScore .= "<td width=\"120\">{$setCalcRes}({$score})</td>";
            }
            //合計
            $setTotal = "<td width=\"120\">".$total."</td>";
            //縦計
            if (is_numeric($total)) {
                $name = "TOTAL_".$seq;
               $term_data[$name][] = $total;
            }
            //テスト単元
            $setUnit .= $setScore.$setTotal;
        }

        /* 学期の表 */
        $setSeme = "";
        foreach ($semeViewList as $semester => $viewList) {
            $setStatus = "";
            foreach ($viewList as $view) {
                //データ取得
                $name = "STATUS_".$semester."_".$view["VIEWCD"];
                $row[$name] = $getDataStatus[$row["SCHREGNO"]][$name];
                //観点(A,B,C)
                $status = $row[$name];
                $setStatus .= "<td width=\"120\">".$status."</td>";
            }
            //学期
            $setSeme .= $setStatus;
        }

        /* 合計の表 */
        $setSemeTotal = "";
        foreach ($model->semesterArray as $semester => $semeVal) {
            $setTotal = "";
            $totalScore = $model->totalSchregArray[$row["SCHREGNO"]][$semester]["SCORE"];
            $totalOmomi = $model->totalOmomi[$semester];
            if ($model->d086 == "1") {
                $setCalcRes = $totalOmomi && $totalScore ? round($totalScore / $totalOmomi * 100, 0) : "0";
            } else {
                $setCalcRes = $totalOmomi && $totalScore ? round($totalScore / $totalOmomi * 100, 1) : "0";
                $setCalcRes = strpos($setCalcRes, '.') === false ? $setCalcRes.".0" : $setCalcRes;
            }
            if ($model->Properties["useJviewHyouka_DankaichiSansyou"] == "1") {
                $totalScore = $model->dankaiHyouka[$row["SCHREGNO"]][$semester];
                $model->totalSchregArray[$row["SCHREGNO"]][$semester]["VALUE"] = $totalScore;
                if ($semester == "9") {
                    $totalScore = strlen($totalScore) > 0 ? $totalScore : 0;
                    $setSemeTotal .= "<td width=\"40\">".$totalScore."</td>";
                    if ($model->Properties["useAssessSubclassMst"] == "1") {
                        $query = knjd126mQuery::getSubclassAssess($model, $row["GRADE"], $row["TOTALCD"], $model->field["SUBCLASSCD"], $totalScore);
                    } else {
                        $query = knjd126mQuery::getAssess($semester, $totalScore);
                    }
                    $setMark = $db->getOne($query);
                } else {
                    $setSemeTotal .= "<td width=\"40\">".$totalScore."</td>";
                    $totalScore = strlen($totalScore) > 0 ? $totalScore : 0;
                    $query = knjd126mQuery::getJviewStatLevel($model, $totalScore, $semester);
                    $setMark = $db->getOne($query);
                }
                $model->totalSchregArray[$row["SCHREGNO"]][$semester]["MARK"] = $setMark;
                $setSemeTotal .= "<td width=\"40\">".$setMark."</td>";
            } else {
                $model->totalSchregArray[$row["SCHREGNO"]][$semester]["VALUE"] = $setCalcRes;
                $setSemeTotal .= "<td width=\"40\">".$totalScore."</td>";
                if ($model->Properties["useAssessSubclassMst"] == "1") {
                    $query = knjd126mQuery::getSubclassAssess($model, $row["GRADE"], $row["TOTALCD"], $model->field["SUBCLASSCD"], $setCalcRes);
                } else {
                    $query = knjd126mQuery::getAssess($semester, $setCalcRes);
                }
                $setMark = $db->getOne($query);
                $model->totalSchregArray[$row["SCHREGNO"]][$semester]["MARK"] = $setMark;
                $setSemeTotal .= "<td width=\"40\">".$setMark."</td>";
            }
        }

        //明細データセット
        $row["MAIN_DATA"] = $setUnit.$setSeme.$setSemeTotal;

        //5行毎に背景色を変える
        if ($counter % 5 == 0) $colorFlg = !$colorFlg;
        $row["BGCOLOR_ROW"] = ($colorFlg) ? "#ffffff" : "#cccccc";
        $counter++;

        $data[] = $row;
    }
    $arg["main_data"] = $data;

    //縦計
    $scoreSum = $scoreAvg = "";
    //テスト単元
    foreach ($unitViewList as $seq => $viewList) {
        foreach ($viewList as $view) {
            //得点欄
            $name = "SCORE_".$seq."_".$view["VIEWCD"];
            $foot = array();
            if (isset($term_data[$name])) {
                $foot["SUM"] = array_sum($term_data[$name]);
                $foot["AVG"] = round((array_sum($term_data[$name])/get_count($term_data[$name]))*10)/10;
            } else {
                $foot["SUM"] = "";
                $foot["AVG"] = "";
            }
            $scoreSum .= "<td width=\"120\">".$foot["SUM"]."</td>";
            $scoreAvg .= "<td width=\"120\">".$foot["AVG"]."</td>";
        }
        //合計欄
        $name = "TOTAL_".$seq;
        $foot = array();
        if (isset($term_data[$name])) {
            $foot["SUM"] = array_sum($term_data[$name]);
            $foot["AVG"] = round((array_sum($term_data[$name])/get_count($term_data[$name]))*10)/10;
        } else {
            $foot["SUM"] = "";
            $foot["AVG"] = "";
        }
        $scoreSum .= "<td width=\"120\">".$foot["SUM"]."</td>";
        $scoreAvg .= "<td width=\"120\">".$foot["AVG"]."</td>";
    }
    //学期・・・空白（表示なし）
    foreach ($semeViewList as $semester => $viewList) {
        foreach ($viewList as $view) {
            $scoreSum .= "<td width=\"120\"></td>";
            $scoreAvg .= "<td width=\"120\"></td>";
        }
    }
    //クラス合計
    //クラス平均
    $arg["FOOT1"] = $scoreSum;
    $arg["FOOT2"] = $scoreAvg;

    return;
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();

    //教科名、科目名の切り替えで使用する。
    return $opt[1]["value"];
}

//ボタン作成
function makeButton(&$objForm, &$arg, $isSelectCmb, $model) {
    $disBtn     = ($isSelectCmb && $model->cmd != "calc") ? "" : $model->setupFlg != "" ? " disabled " : "";
    $disBtnUpd  = ($isSelectCmb && $model->cmd == "calc") ? "" : " disabled ";
    //集計
    if (!$model->field["SEMESTER"]) {
        $disBtn = " disabled ";
    }
    $arg["btn_calc"] = knjCreateBtn($objForm, "btn_calc", "集 計", " onclick=\"return btn_submit('calc');\"".$disBtn);
    //保存
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('update');\"".$disBtnUpd);
    //取消
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"".$disBtnUpd);
    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"return btnEnd();\"");
}
?>
