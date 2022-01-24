<?php

require_once('for_php7.php');

class knjz406bForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjz406bindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //年組コンボ
        $query = knjz406bQuery::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        $retFirstVal = makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");

        //教科コンボ
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        if (!$grade) {
            list($grade, $hrClass) = explode("-", $retFirstVal);
        }

        $query = knjz406bQuery::selectSchoolKind($grade);
        $schooLkind = $db->getOne($query);
        if ($schooLkind == "H") {
            $arg["SUBCLASS_TITLE"] = "科目名";
        } else {
            $arg["SUBCLASS_TITLE"] = "教科名";
        }
        $query = knjz406bQuery::getSubclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //観点設定
        $viewList = setViewData($objForm, $arg, $db, $model);

        //編集対象データリスト
        $update_flg = makeDataList($objForm, $arg, $db, $model, $objUp, $viewList);

        //ボタン作成
        makeButton($objForm, $arg, $db, $model, $update_flg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //データベース接続切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレート呼び出し
        View::toHTML($model, "knjz406bForm1.html", $arg);
    }
}

//観点設定
function setViewData(&$objForm, &$arg, $db, $model) {
    //列数取得
    $MaxRowCnt = get_count($db->getCol(knjz406bQuery::getViewList($model)));

    //観点一覧取得
    $viewList = array();
    $setTitle = $setSubTitle = "";
    $allWidth = 250;
    $RowCounter = 0;
    $result = $db->query(knjz406bQuery::getViewList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        ++$RowCounter;
        $viewList[] = $row["VALUE"];

        $setSubTitle .= "<td width=\"60\" nowrap>評価</td>";
        $allWidth += 68;

        $setTitle .= "<td colspan=\"2\" width=\"120\">".$row["LABEL"]."</td>";
        $setSubTitle .= "<td width=\"60\" nowrap>配点</td>";
        $allWidth += 68;
    }
    if($RowCounter<>0)$setTitle .= "<td rowspan=\"2\" width=\"*\" align=\"center\">計</td>";
    $allWidth += 78;
    $result->free();

    //表サイズ
    $arg["useAllWidth"] = $allWidth;

    if (get_count($viewList) == 0) {
        $setTitle .= "<td colspan=\"2\" width=\"120\"></td>";
        $setSubTitle .= "<td width=\"60\"></td>";
        $setSubTitle .= "<td width=\"60\"></td>";
    }

    $arg["COLSPAN"] = (get_count($viewList) > 0) ?get_count($viewList) * 2+1 : 2+1;
    $arg["TITLE"]    = $setTitle;
    $arg["SUBTITLE"] = $setSubTitle;

    return $viewList;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $viewList) {
    //データ取得
    $getData = array();
    $query = knjz406bQuery::getUnitTestInputseqDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $getData[$row["SEQ"]]["VIEWFLG_".$row["VIEWCD"]] = $row["VIEWFLG"];
        $getData[$row["SEQ"]]["UNIT_ASSESSHIGH_".$row["VIEWCD"]] = $row["UNIT_ASSESSHIGH"];
        $getData[$row["SEQ"]]["WEIGHTING_".$row["VIEWCD"]] = $row["WEIGHTING"];
    }

    //テスト単元
    $rowCnt = 0;
    $idx = 1;
    $counter  = 0;
    $colorFlg = false;
    $data = array();
    $update_flg = false;
    $rowSum  = array(); //列合計 配点(加重値)
    $query = knjz406bQuery::getUnitTestDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($viewList as $viewcd) {
            $row["VIEWFLG_".$viewcd]            = $getData[$row["SEQ"]]["VIEWFLG_".$viewcd];
            $row["UNIT_ASSESSHIGH_".$viewcd]    = $getData[$row["SEQ"]]["UNIT_ASSESSHIGH_".$viewcd];
            $row["WEIGHTING_".$viewcd]          = $getData[$row["SEQ"]]["WEIGHTING_".$viewcd];
        }

        //エラー後にセット
        if (isset($model->warning)) {
            if (is_array($model->setData[$row["SEQ"]])) {
                foreach ($model->setData[$row["SEQ"]] as $key => $val) {
                    $row[$key] = $val;
                }
            }
        }

        //更新チェックボックス
        if ($row["CHECK"] == $row["SEQ"]) $update_flg = true;
        $extra  = ($row["CHECK"] == $row["SEQ"]) ? "checked" : "";
        $extra .= " onclick=\"OptionUse(this);\"";
        $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK[]", $row["SEQ"], $extra, "");

        //テスト実施日
        if(strlen($row["UNIT_TEST_DATE"])){
            $date = str_replace("-", "/", $row["UNIT_TEST_DATE"]);
            $row["UNIT_TEST_DATE"]  = "（".$date."）";
        }

        //項目
        $setTmp = "";
        $setTmp2 = "";
        $row_counter = 0;
        $width = 60;
        $MaxRowCnt = get_count($viewList);
        $colSum1 = 0; //行合計 配点
        $colSum2 = 0; //行合計 配点(加重値)
        $seq = $row["SEQ"];
        foreach ($viewList as $viewcd) {
            ++$row_counter;

            $unitAssesshigh = $row["UNIT_ASSESSHIGH_".$viewcd];                           //配点
            $weighting = ($row["WEIGHTING_".$viewcd]) ? $row["WEIGHTING_".$viewcd] : 100; //重み
            $value = $unitAssesshigh * ($weighting / 100);                                //配点(加重値);
            if ($model->d084 == "1") {
                $value = round($value);
            } else {
                $value = round($value, 1);
                $value = strpos($value, '.') === false ? $value.".0" : $value;
            }

            //評価対象チェックボックス
            $extra  = ($row["VIEWFLG_".$viewcd]) ? "checked" : "";
            $extra .= " id=\"VIEWFLG_{$seq}_{$row_counter}\" onChange=\"selectRowList2($idx);\"";
            $row["VIEWFLG_".$viewcd] = knjCreateCheckBox($objForm, "VIEWFLG_".$viewcd."[]", $seq, $extra, "");
            $setTmp .= "<td width=\"{$width}\">".$row["VIEWFLG_".$viewcd]."</td>";

            //配点テキストボックス
            $colSum1 = $colSum1 + $unitAssesshigh;   //行合計 配点
            //if($weighting != 100){ 
            //    $extra = " STYLE=\"text-align: right; background-color:#cccccc;\" readOnly";
            //}else{
            //    $extra = " STYLE=\"text-align: right; background-color:#ffffff;\" ";
            //}
            $extra = " STYLE=\"text-align: right;\"";
            $extra .= " onChange=\"this.style.background='#ccffcc'; Calc($model->unitTestCnt,$MaxRowCnt, '{$model->d084}'); selectRowList2($idx);\" onblur=\"this.value=NumCheck(this.value);\" id=\"UNIT_ASSESSHIGH_{$idx}_{$row_counter}\" ";
            $row["UNIT_ASSESSHIGH_".$viewcd] = knjCreateTextBox($objForm, $unitAssesshigh, "UNIT_ASSESSHIGH_".$viewcd."[]", 3, 3, $extra);
            $setTmp .= "<td width=\"{$width}\">".$row["UNIT_ASSESSHIGH_".$viewcd]."</td>";

            //重みテキストボックス
            $colSum2 = $colSum2 + $value;   //行合計 配点(加重値)
            if ($model->d084 == "1") {
                $rowSum[$row_counter] = $rowSum[$row_counter] + intval($value); //列合計 配点(加重値)
            } else {
                $rowSum[$row_counter] = $rowSum[$row_counter] + $value; //列合計 配点(加重値)
            }
            $extra  = " STYLE=\"text-align: right;\" onChange=\"this.style.background='#ccffcc'; Calc($model->unitTestCnt,$MaxRowCnt, '{$model->d084}'); selectRowList2($idx);\" onblur=\"this.value=NumCheck(this.value);\" id=\"WEIGHTING_{$idx}_{$row_counter}\" ";
            $row["WEIGHTING_".$viewcd] = knjCreateTextBox($objForm, $weighting, "WEIGHTING_".$viewcd."[]", 3, 3, $extra);
            $setTmp2 .= "<td width=\"{$width}\" colspan=\"2\">".$row["WEIGHTING_".$viewcd];                 //重みテキストボックス
            $setTmp2 .= "　<label id=\"UNIT_ASSESSHIGH_CALC_{$idx}_{$row_counter}\" >".$value."</label>";   //配点(加重値)ラベル
            $setTmp2 .= "</td>";
        }
        
        //行合計 配点
        $value = $colSum1;
        $setTmp .= "<td width=\"*\"><label id=\"UNIT_ASSESSHIGH_COL_SUM_{$idx}\">".$value."</label></td>";

        //行合計 配点(加重値)
        $value = $colSum2;
        if ($model->d084 == "1") {
            $value = intval($value);
        } else {
            $value = strpos($value, '.') === false ? $value.".0" : $value;
        }
        $rowSum[$row_counter+1] = $rowSum[$row_counter+1] + intval($value); //列合計 配点(加重値)
        $setTmp2 .= "<td width=\"*\"><label id=\"UNIT_ASSESSHIGH_CALC_COL_SUM_{$idx}\">".$value."</label></td>";

        $row["MAIN_DATA"] = $setTmp;

        $row["MAIN_DATA2"] = $setTmp2;

        //hidden
        $row["SEQ"] = "<input type=\"hidden\" name=\"SEQ[]\" value=\"".$row["SEQ"]."\">";

        //2行毎に背景色を変える
        $row["BGCOLOR_ROW"] = ($counter % 2 == 0) ? "#ffffff" : "#cccccc";
        $counter++;

        $rowCnt = $MaxRowCnt;
        $idx++;
        $data[] = $row;
    }

    $arg["main_data"] = $data;

    //列合計の設定
    if($rowCnt <> 0 ){
        $data = array();
        $row = array();
        $unitAssesshighSum = "";
        for ($si = 1; $si <= $rowCnt+1; $si++) {
            //列合計 配点(加重値)
            $value = $rowSum[$si];
            if ($model->d084 == "1") {
                $value = intval($value);
            } else {
                $value = strpos($value, '.') === false ? $value.".0" : $value;
            }
            $unitAssesshighSum .= "<td width=\"*\" colspan=\"2\"><label id=\"UNIT_ASSESSHIGH_CALC_ROW_SUM_{$si}\">".$value."</label></td>";
        }

        //2行毎に背景色を変える
        $row["BGCOLOR_ROW"] = ($counter % 2 == 0) ? "#ffffff" : "#cccccc";
    
        $row["UNIT_ASSESSHIGH_SUM"] = $unitAssesshighSum;
        $data[] = $row;
        $arg["sum_data"] = $data;
    }

    return $update_flg;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model, $update_flg) {
    //前年度からコピーボタン
    $extra = "onclick=\"btn_submit('copy');\"";
    $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

    //更新ボタン
    $extra  = "onclick=\"return btn_submit('update');\"";
    $extra .= ($update_flg) ? "" : " disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $kyoukaTantouList = array();        //教科担当の担当している年組を保持
    $nenkumiFlg = false;
    $value_flg = false;
    $result = $db->query($query);
    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if (isset($row["KYOUKA_TANTOU_FLG"]) && $row["KYOUKA_TANTOU_FLG"] == "1") {
            $nenkumiFlg = true;
            $kyoukaTantouList[] =    $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    if ($nenkumiFlg) {
        knjCreateHidden($objForm, "kyoukaTantouList", implode(",", $kyoukaTantouList));
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    //教科名、科目名の切り替えで使用する。
    return $opt[1]["value"];
}
?>
