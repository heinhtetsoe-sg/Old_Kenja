<?php

require_once('for_php7.php');

class knjz406aForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjz406aindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //年組コンボ
        $query = knjz406aQuery::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        $retFirstVal = makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");

        //教科コンボ
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        if (!$grade) {
            list($grade, $hrClass) = explode("-", $retFirstVal);
        }

        $query = knjz406aQuery::selectSchoolKind($grade);
        $schooLkind = $db->getOne($query);
        if ($schooLkind == "H") {
            $arg["SUBCLASS_TITLE"] = "科目名";
        } else {
            $arg["SUBCLASS_TITLE"] = "教科名";
        }
        $query = knjz406aQuery::getSubclass($model);
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
        View::toHTML($model, "knjz406aForm1.html", $arg);
    }
}

//観点設定
function setViewData(&$objForm, &$arg, $db, $model) {
    //列数取得
    $MaxRowCnt = get_count($db->getCol(knjz406aQuery::getViewList($model)));

    //観点一覧取得
    $viewList = array();
    $setTitle = $setSubTitle = "";
    $allWidth = 250;
    $RowCounter = 0;
    $result = $db->query(knjz406aQuery::getViewList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        ++$RowCounter;
        $viewList[] = $row["VALUE"];

        $setSubTitle .= "<td width=\"60\" nowrap>評価</td>";
        $allWidth += 68;

        if ($RowCounter == $MaxRowCnt) {
            $setTitle .= "<td colspan=\"2\" width=\"120\">".$row["LABEL"]."</td>";
            $setSubTitle .= "<td width=\"*\" nowrap>配点</td>";
            $allWidth += 78;
        } else {
            $setTitle .= "<td colspan=\"2\" width=\"120\">".$row["LABEL"]."</td>";
            $setSubTitle .= "<td width=\"60\" nowrap>配点</td>";
            $allWidth += 68;
        }
    }
    $result->free();

    //表サイズ
    $arg["useAllWidth"] = $allWidth;

    if (get_count($viewList) == 0) {
        $setTitle .= "<td colspan=\"2\" width=\"120\"></td>";
        $setSubTitle .= "<td width=\"60\"></td>";
        $setSubTitle .= "<td width=\"60\"></td>";
    }

    $arg["COLSPAN"] = (get_count($viewList) > 0) ?get_count($viewList) * 2 : 2;
    $arg["TITLE"]    = $setTitle;
    $arg["SUBTITLE"] = $setSubTitle;

    return $viewList;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $viewList) {
    //データ取得
    $getData = array();
    $query = knjz406aQuery::getUnitTestInputseqDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $getData[$row["SEQ"]]["VIEWFLG_".$row["VIEWCD"]] = $row["VIEWFLG"];
        $getData[$row["SEQ"]]["UNIT_ASSESSHIGH_".$row["VIEWCD"]] = $row["UNIT_ASSESSHIGH"];
    }

    //テスト単元
    $counter  = 0;
    $colorFlg = false;
    $data = array();
    $update_flg = false;
    $query = knjz406aQuery::getUnitTestDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($viewList as $viewcd) {
            $row["VIEWFLG_".$viewcd]            = $getData[$row["SEQ"]]["VIEWFLG_".$viewcd];
            $row["UNIT_ASSESSHIGH_".$viewcd]    = $getData[$row["SEQ"]]["UNIT_ASSESSHIGH_".$viewcd];
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
        $extra .= (strlen($row["UNIT_TEST_DATE"])) ? " disabled" : "";
        $extra .= " onclick=\"OptionUse(this);\"";
        $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK[]", $row["SEQ"], $extra, "");

        //項目
        $setTmp = "";
        $row_counter = 0;
        $width = 60;
        $MaxRowCnt = get_count($viewList);
        foreach ($viewList as $viewcd) {
            ++$row_counter;

            //評価対象チェックボックス
            $extra  = ($row["VIEWFLG_".$viewcd]) ? "checked" : "";
            $row["VIEWFLG_".$viewcd] = knjCreateCheckBox($objForm, "VIEWFLG_".$viewcd."[]", $row["SEQ"], $extra, "");
            $setTmp .= "<td width=\"60\">".$row["VIEWFLG_".$viewcd]."</td>";

            //配点テキストボックス
            if ($MaxRowCnt == $row_counter) $width = "*";
            $value = $row["UNIT_ASSESSHIGH_".$viewcd];
            $extra = " STYLE=\"text-align: right;\" onChange=\"this.style.background='#ccffcc';\" onblur=\"this.value=NumCheck(this.value)\";\" ";
            $row["UNIT_ASSESSHIGH_".$viewcd] = knjCreateTextBox($objForm, $value, "UNIT_ASSESSHIGH_".$viewcd."[]", 3, 3, $extra);
            $setTmp .= "<td width=\"{$width}\">".$row["UNIT_ASSESSHIGH_".$viewcd]."</td>";
        }
        $row["MAIN_DATA"] = $setTmp;

        //hidden
        $row["SEQ"] = "<input type=\"hidden\" name=\"SEQ[]\" value=\"".$row["SEQ"]."\">";

        //2行毎に背景色を変える
        $row["BGCOLOR_ROW"] = ($counter % 2 == 0) ? "#ffffff" : "#cccccc";
        $counter++;

        $data[] = $row;
    }

    $arg["main_data"] = $data;

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
    $value_flg = false;
    $result = $db->query($query);
    while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    //教科名、科目名の切り替えで使用する。
    return $opt[1]["value"];
}
?>
