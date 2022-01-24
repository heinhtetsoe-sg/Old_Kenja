<?php

require_once('for_php7.php');

class knjd126gForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjd126gindex.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //年組コンボ
        $query = knjd126gQuery::getGradeHrClass($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        $retFirstVal = makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");

        //教科コンボ
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        if (!$grade) {
            list($grade, $hrClass) = explode("-", $retFirstVal);
        }

        $query = knjd126gQuery::selectSchoolKind($grade);
        $schooLkind = $db->getOne($query);
        if ($schooLkind == "H") {
            $arg["SUBCLASS_TITLE"] = "科目名";
        } else {
            $arg["SUBCLASS_TITLE"] = "教科名";
        }
        $query = knjd126gQuery::getSubclass($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //テスト単元コンボ
        $query = knjd126gQuery::getUnitTestDat($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "SEQ", $model->field["SEQ"], $extra, 1, "blank");

        //テスト実施日
        if (strlen($model->field["SEQ"]) && ($model->cmd == "main" || $model->cmd == "change" || $model->cmd == "reset")) {
            $unit_test = $db->getRow(knjd126gQuery::getUnitTestDat($model, $model->field["SEQ"]), DB_FETCHMODE_ASSOC);
            $model->field["UNIT_TEST_DATE"] = $unit_test["UNIT_TEST_DATE"];
            if (!strlen($model->field["UNIT_TEST_DATE"])){
                $model->setWarning("MSG305", "テスト実施日が登録されていません。");
            }
        }
        $model->field["UNIT_TEST_DATE"] = str_replace("-", "/", $model->field["UNIT_TEST_DATE"]);
        //$arg["UNIT_TEST_DATE"] = View::popUpCalendar($objForm, "UNIT_TEST_DATE", $model->field["UNIT_TEST_DATE"]);
        $arg["UNIT_TEST_DATE"] = $model->field["UNIT_TEST_DATE"];

        //観点設定
        $viewList = setViewData($objForm, $arg, $db, $model);

        //編集対象データリスト
        makeDataList($objForm, $arg, $db, $model, $objUp, $viewList);

        //ボタン作成
        makeButton($objForm, $arg, $db, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"MOVE_ENTER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;
        
        //データベース接続切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレート呼び出し
        View::toHTML($model, "knjd126gForm1.html", $arg);
    }
}

//観点設定
function setViewData(&$objForm, &$arg, $db, $model) {
    //観点一覧取得
    $viewList = array();
    $setTitle = $setSubTitle = "";
    $allWidth = 380;
    $result = $db->query(knjd126gQuery::getViewList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $viewList[] = array($row["VIEWCD"], $row["UNIT_ASSESSHIGH"]);

        $setTitle    .= "<td width=\"120\">".$row["VIEWABBV"]."</td>";
        $setSubTitle .= "<td nowrap align=\"center\">".$row["UNIT_ASSESSHIGH"]."</td>";
        $allWidth += 130;
    }
    $result->free();

    if (get_count($viewList) == 0) {
        $setTitle    .= "<td width=\"120\"></td>";
        $setSubTitle .= "<td></td>";
        $allWidth += 130;
    }

    $arg["COLSPAN"] = (get_count($viewList) > 0) ? get_count($viewList) : 1;
    $arg["TITLE"]    = $setTitle;
    $arg["SUBTITLE"] = $setSubTitle;

    //表サイズ
    $arg["useAllWidth"] = $allWidth;

    return $viewList;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $viewList) {
    //データ取得
    $getData = array();
    $query = knjd126gQuery::getScoreData($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $getData[$row["SCHREGNO"]]["SCORE_".$row["VIEWCD"]] = $row["SCORE"];
    }

    //明細データ
    $counter  = 0;
    $colorFlg = false;
    $data = array();
    $query = knjd126gQuery::getSchList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        if (get_count($viewList) > 0) {
            foreach ($viewList as $view) {
                list ($viewcd, $unit_assesshigh) = $view;
                $row["SCORE_".$viewcd] = $getData[$row["SCHREGNO"]]["SCORE_".$viewcd];
            }

            //エラー後にセット
            if (isset($model->warning)) {
                if (is_array($model->setData[$row["SCHREGNO"]])) {
                    foreach ($model->setData[$row["SCHREGNO"]] as $key => $val) {
                        $row[$key] = $val;
                    }
                }
            }

            $setTmp = "";
            $total = 0;
            foreach ($viewList as $view) {
                list ($viewcd, $unit_assesshigh) = $view;

                //得点テキストボックス
                $value = $row["SCORE_".$viewcd];
                $total += $value;
                $extra = " STYLE=\"text-align: right;\" onChange=\"this.style.background='#ccffcc';\" onblur=\"NumCheck(this, {$unit_assesshigh})\";\" id=\"{$row["SCHREGNO"]}\" onPaste=\"return showPaste(this);\" data-unit-assesshigh=\"{$unit_assesshigh}\" ";
                $row["SCORE_".$viewcd] = knjCreateTextBox($objForm, $value, "SCORE_".$viewcd."[]", 3, 3, $extra);
                $setTmp .= "<td width=\"120\">".$row["SCORE_".$viewcd]."</td>";
            }
            $row["MAIN_DATA"] = $setTmp;

            //合計
            $row["TOTAL"] = "<span id=\"total".$row["SCHREGNO"]."\">".$total."</span>";

        } else {
            $row["MAIN_DATA"] = "<td width=\"120\"></td>";
            $row["TOTAL"] = "";
        }

        //hidden
        $row["SCHREGNO"] = "<input type=\"hidden\" name=\"SCHREGNO[]\" value=\"".$row["SCHREGNO"]."\">";

        //5行毎に背景色を変える
        if ($counter % 5 == 0) $colorFlg = !$colorFlg;
        $row["BGCOLOR_ROW"] = ($colorFlg) ? "#ffffff" : "#cccccc";
        $counter++;

        $data[] = $row;
    }

    $arg["main_data"] = $data;

    return;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model) {
    //更新ボタン
    $extra  = "onclick=\"return btn_submit('update');\"";
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
