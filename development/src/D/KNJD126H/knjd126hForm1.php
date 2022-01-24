<?php

require_once('for_php7.php');

class knjd126hForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd126hindex.php", "", "main");

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
        $query = knjd126hQuery::selectGradeHrClass($model);
        $extra = "onChange=\"btn_submit('changeGradeHrClass')\";" .$setupFlgCheck;
        $retFirstVal = makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "BLANK");
        /* 教科名 */
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        if (!$grade) {
            list($grade, $hrClass) = explode("-", $retFirstVal);
        }

        $query = knjd126hQuery::selectSchoolKind($grade);
        $schooLkind = $db->getOne($query);
        if ($schooLkind == "H") {
            $arg["SUBCLASS_TITLE"] = "科目名";
        } else {
            $arg["SUBCLASS_TITLE"] = "教科名";
        }
        $query = knjd126hQuery::selectSubclasscd($model);
        $extra = "onChange=\"btn_submit('changeSubclasscd')\";" .$setupFlgCheck;
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");
        /* 処理学期 */
        $query = knjd126hQuery::selectSemester($model);
        $extra = "onChange=\"btn_submit('changeSemester')\";" .$setupFlgCheck;
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "BLANK");

        //観点設定（テスト単元）
        $unitViewList = setViewData($objForm, $arg, $db, $model);

        //観点設定（学期）
        $semeViewList = setViewDataSeme($objForm, $arg, $db, $model, $unitViewList);

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
        View::toHTML($model, "knjd126hForm1.html", $arg);
    }
}

//観点設定（テスト単元）
function setViewData(&$objForm, &$arg, $db, $model) {
    //表サイズ
    $allWidth = 0;

    //テスト単元・観点一覧取得
    $unitViewList = array();
    $result = $db->query(knjd126hQuery::getUnitViewList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $unitViewList[$row["SEQ"]][] = $row;
    }
    $result->free();

    $setUnitSort = "";  //1)テスト実施順
    $setUnitName = "";  //2)テスト単元名
    $setView = "";      //3)観点略称
    $setHigh = "";      //4)配点
    foreach ($unitViewList as $seq => $viewList) {
        $unitSort = "";
        $unitName = "";
        $total = 0;
        foreach ($viewList as $view) {
            //1)テスト実施順　2)テスト単元名
            $unitSort = $view["SORT"];
            $unitName = $view["UNIT_L_NAME"];
            //3)観点略称　4)配点
            $fontSizeS = (mb_strlen($view["VIEWABBV"]) > 16) ? "<font style=\"font-size:9pt\">": "";
            $fontSizeF = (mb_strlen($view["VIEWABBV"]) > 16) ? "</font>": "";
            $setView .= "<td width=\"120\" >".$fontSizeS.$view["VIEWABBV"].$fontSizeF."</td>";
            $setHigh .= "<td width=\"120\" align=\"center\">".$view["UNIT_ASSESSHIGH"]."</td>";
            //3)合計　4)配点合計
            $total += $view["UNIT_ASSESSHIGH"];

            //表示サイズ
            $allWidth += 130;
        }
        //1)テスト実施順　2)テスト単元名
        $viewCnt = get_count($viewList) + 1;
        $setUnitSort .= "<td colspan={$viewCnt} nowrap>".$unitSort."</td>";
        $setUnitName .= "<td colspan={$viewCnt} nowrap>".$unitName."</td>";
        //3)合計　4)配点合計
        $setView .= "<td width=\"120\">合計</td>";
        $setHigh .= "<td width=\"120\">".$total."</td>";
        $allWidth += 130;
    }

    $arg["HEAD1"] = $setUnitSort;
    $arg["HEAD2"] = $setUnitName;
    $arg["HEAD3"] = $setView;
    $arg["HEAD4"] = $setHigh;

    //サイズ調整（幅）
    $arg["widthHeader"] = $allWidth;
    $arg["widthMeisai"] = $allWidth;

    return $unitViewList;
}

//観点設定（学期）
function setViewDataSeme(&$objForm, &$arg, $db, $model, $unitViewList) {
    //表サイズ
    $allWidth = 0;

    //学期・観点一覧取得
    $semeViewList = array();
    $result = $db->query(knjd126hQuery::getSemeViewList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $semeViewList[$row["SEMESTER"]][] = $row;
    }
    $result->free();

    $setSemeName = "";  //1)学期名
    $setKotei = "";     //2)固定文字
    $setView = "";      //3)観点略称
    $setHigh = "";      //4)配点
    foreach ($semeViewList as $semester => $viewList) {
        $semeName = "";
        $kotei = "";
        $total = 0;
        foreach ($viewList as $view) {
            //1)学期名　2)固定文字
            $semeName = $view["SEMESTERNAME"];
            $kotei = "観点";
            //3)観点略称　4)配点
            $fontSizeS = (mb_strlen($view["VIEWABBV"]) > 16) ? "<font style=\"font-size:9pt\">": "";
            $fontSizeF = (mb_strlen($view["VIEWABBV"]) > 16) ? "</font>": "";
            $setView .= "<td width=\"120\">".$fontSizeS.$view["VIEWABBV"].$fontSizeF."</td>";
            $setHigh .= "<td width=\"120\" align=\"center\">".$view["UNIT_ASSESSHIGH"]."</td>";

            //表示サイズ
            $allWidth += 130;
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

    //サイズ調整（幅）
    $allWidth   += (get_count($unitViewList) == 0 && get_count($semeViewList) == 0) ? 130 : 0;
    $widthHeader = (get_count($unitViewList) == 0 && get_count($semeViewList) == 0) ? 0 : 50;
    $arg["widthHeader"] += $allWidth + $widthHeader;
    $arg["widthMeisai"] += $allWidth;

    return $semeViewList;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, &$model, $unitViewList, $semeViewList, $isSelectCmb) {
    //SCOREデータ取得
    $getData = array();
    $query = knjd126hQuery::getScoreData($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $name = "SCORE_".$row["SEQ"]."_".$row["VIEWCD"];
        $getData[$row["SCHREGNO"]][$name] = $row["SCORE"];
    }

    //STATUSデータ取得
    $getDataStatus = array();
    //「1:編集中」・・・集計ボタンを押した時
    if ($isSelectCmb && $model->cmd == "calc") {
        //観点データ（集計）取得・・・処理学期のみ
        $query = knjd126hQuery::getCalcJviewstatRecord($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $name = "STATUS_".$row["SEMESTER"]."_".$row["VIEWCD"];
            //到達度を括弧表示　例：A (62.5)
            $toutatudo = "";
            if (strlen($row["STATUS"]) && strlen($row["REMARK3"])) {
                $toutatudo = " (".$row["REMARK3"]."%)";
            }
            $getDataStatus[$row["SCHREGNO"]][$name] = $row["STATUS"].$toutatudo;
        }
        //観点データ（DB）取得・・・処理学期以外
        $query = knjd126hQuery::getJviewstatRecord($model);
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
        }
    } else {
        //観点データ（DB）取得・・・処理学期全て
        $query = knjd126hQuery::getJviewstatRecord($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $name = "STATUS_".$row["SEMESTER"]."_".$row["VIEWCD"];
            //到達度を括弧表示　例：A (62.5)
            $toutatudo = "";
            if (strlen($row["STATUS"]) && strlen($row["REMARK3"])) {
                $toutatudo = " (".$row["REMARK3"]."%)";
            }
            $getDataStatus[$row["SCHREGNO"]][$name] = $row["STATUS"].$toutatudo;
        }
    }

    //明細データ
    $counter  = 0;
    $colorFlg = false;
    $data = array();

    $query = knjd126hQuery::getSchList($model);
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
                $setScore .= "<td width=\"120\">".$score."</td>";
                //縦計
                if (is_numeric($score)) {
                   $term_data[$name][] = $score;
                }
                //合計
                $total += $score;
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

        //明細データセット
        $row["MAIN_DATA"] = $setUnit.$setSeme;

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
    $disBtn     = ($isSelectCmb && $model->cmd != "calc") ? "" : " disabled";
    $disBtnUpd  = ($isSelectCmb && $model->cmd == "calc") ? "" : " disabled";
    //集計
    $arg["btn_calc"] = knjCreateBtn($objForm, "btn_calc", "集 計", " onclick=\"return btn_submit('calc');\"".$disBtn);
    //保存
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('update');\"".$disBtnUpd);
    //取消
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"".$disBtnUpd);
    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"return btnEnd();\"");
}
?>
