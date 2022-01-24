<?php

require_once('for_php7.php');

class knjd133jForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        if ($model->cmd == "back") {
            $model->field["SUBCLASSCD"] = $model->subclasscd;
            $model->field["CHAIRCD"]    = $model->chaircd;
        }

        //学期コンボ
        if ($model->Properties["KNJD133J_semesCombo"] == "1") {
            $arg["KNJD133J_semesCombo"] = "1";
            knjCreateHidden($objForm, "KNJD133J_semesCombo", $model->Properties["KNJD133J_semesCombo"]);
            $query = knjd133jQuery::getSemesterCmb();
            $extra = "onchange=\"return btn_submit('main');\"";
            makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, "");
        }

        //名称マスタ D校種08チェック
        $model->che_school_kind = "D".$model->schoolKind."08";
        $model->count = $db->getone(knjd133jquery::getNameMstche($model));

        //科目コンボ
        $query = knjd133jQuery::selectSubclassQuery($model);
        $extra = "onchange=\"return btn_submit('subclasscd');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "BLANK");

        //講座コンボ
        $query = knjd133jQuery::selectChairQuery($model);
        $extra = "onchange=\"return btn_submit('chaircd');\"";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "BLANK");

        //学期開始日、終了日
        $seme = $db->getRow(knjd133jQuery::getSemester($model), DB_FETCHMODE_ASSOC);
        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        if ($seme["SDATE"] <= CTRL_DATE && CTRL_DATE <= $seme["EDATE"]) {
            $execute_date = CTRL_DATE;  //初期値
        } else {
            $execute_date = $seme["EDATE"];     //初期値
        }

        //表示名
        $arg["TOTALSTUDYACT_LABEL"] = ($model->isChiyoda) ? "観点" : "学習内容";
        $arg["REMARK1_LABEL"] = $model->remark1Name;

        //表示調整
        $allwidth = 320;
        $totalstudyact_width = 0;
        unset($arg["useAct"]);
        unset($arg["useTime"]);
        unset($arg["useRemark1"]);
        if ($model->useRemark1) {
            $arg["useRemark1"]  = "1";
            $remark1_width = ($model->getPro["REMARK1"]["moji"] > 10) ? (($model->getPro["REMARK1"]["moji"] - 10) * 13.5) + 170 : 170;
            $arg["REMARK1_WIDTH"]  = $remark1_width;
            $allwidth += $remark1_width + 30;
        } elseif ($model->Properties["useTotalstudyTime_J"] == '1') {
            $arg["useAct"]  = "1";
            $arg["useTime"] = "1";
            $totalstudyact_width = ($model->getPro["TOTALSTUDYACT"]["moji"] > 10) ? (($model->getPro["TOTALSTUDYACT"]["moji"] - 10) * 13.5) + 170 : 170;
            $arg["TOTALSTUDYACT_WIDTH"]  = $totalstudyact_width;
            $totalstudytime_width = ($model->getPro["TOTALSTUDYTIME"]["moji"] > 10) ? (($model->getPro["TOTALSTUDYTIME"]["moji"] - 10) * 13.5) + 170 : 170;
            $allwidth += $totalstudyact_width + $totalstudytime_width + 30;
        } elseif ($model->Properties["useTotalstudyTime_J"] == '2') {
            $arg["useTime"] = "1";
            $totalstudytime_width = ($model->getPro["TOTALSTUDYTIME"]["moji"] > 10) ? (($model->getPro["TOTALSTUDYTIME"]["moji"] - 10) * 13.5) + 170 : 170;
            $allwidth += $totalstudytime_width + 30;
        } else {
            $arg["useAct"] = "1";
            $arg["TOTALSTUDYACT_WIDTH"] = "*";
            $totalstudyact_width = ($model->getPro["TOTALSTUDYACT"]["moji"] > 10) ? (($model->getPro["TOTALSTUDYACT"]["moji"] - 10) * 13.5) + 170 : 170;
            $allwidth += $totalstudyact_width + 30;
        }
        $arg["ALLWIDTH"] = ($allwidth > 700) ? $allwidth : 700;

        $comment = array();
        //初期化
        $model->data = array();
        $counter = 0;
        $grade = array();
        //一覧表示
        $colorFlg = false;
        $query = knjd133jQuery::selectQuery($model, $execute_date);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //学年ごとの連番取得
            $grade[$row["GRADE"]][] = $counter;

            //クラス-出席番号(表示)
            if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            /*** テキストエリア ***/

            foreach ($model->getPro as $key => $val) {
                $model->data[$key."-".$counter] = $row[$key];

                if ($val["gyou"] == 1) {
                    $extra = "onPaste=\"return showPaste(this);\"";
                    $row[$key] = $model->cmd != "csvInputMain" ? $row[$key] : $model->data_arr[$row["SCHREGNO"]][$key];
                    $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row[$key] : $model->fields[$key][$counter];

                    $row[$key] = knjCreateTextBox($objForm, $value, $key."-".$counter, ($val["moji"] * 2), ($val["moji"] * 2), $extra);
                    $comment[$key] = "(全角{$val["moji"]}文字まで)";
                } else {
                    $height = $val["gyou"] * 13.5 + ($val["gyou"] - 1) * 3 + 5;
                    $extra = "style=\"height:{$height}px;\" onPaste=\"return showPaste(this);\"";
                    $row[$key] = $model->cmd != "csvInputMain" ? $row[$key] : $model->data_arr[$row["SCHREGNO"]][$key];
                    $value = (!isset($model->warning) && $model->cmd != "value_set") ? $row[$key] : $model->fields[$key][$counter];

                    $row[$key] = KnjCreateTextArea($objForm, $key."-".$counter, $val["gyou"], ($val["moji"] * 2 + 1), "soft", $extra, $value);
                    $comment[$key] = "(全角{$val["moji"]}文字X{$val["gyou"]}行まで)";
                }
            }

            //背景色
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        $arg["TOTALSTUDYACT_COMMENT"]   = $comment["TOTALSTUDYACT"];
        $arg["TOTALSTUDYTIME_COMMENT"]  = $comment["TOTALSTUDYTIME"];
        $arg["REMARK1_COMMENT"]         = $comment["REMARK1"];

        //学年ごとの連番を格納
        foreach ($grade as $key => $val) {
            knjCreateHidden($objForm, "counter_array-".$key, implode(',', $val));
        }

        //CSV処理作成
        makeCsv($objForm, $arg);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $counter);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //貼付機能の文字数チェック用
        knjCreateHidden($objForm, "TOTALSTUDYACT_moji", $model->getPro["TOTALSTUDYACT"]["moji"]);
        knjCreateHidden($objForm, "TOTALSTUDYACT_gyou", $model->getPro["TOTALSTUDYACT"]["gyou"]);
        knjCreateHidden($objForm, "TOTALSTUDYTIME_moji", $model->getPro["TOTALSTUDYTIME"]["moji"]);
        knjCreateHidden($objForm, "TOTALSTUDYTIME_gyou", $model->getPro["TOTALSTUDYTIME"]["gyou"]);
        knjCreateHidden($objForm, "REMARK1_moji", $model->getPro["REMARK1"]["moji"]);
        knjCreateHidden($objForm, "REMARK1_gyou", $model->getPro["REMARK1"]["gyou"]);
        knjCreateHidden($objForm, "TOTALSTUDYACT_label", ($model->isChiyoda) ? "観点" : "学習内容");
        knjCreateHidden($objForm, "useTotalstudyTime_J", $model->Properties["useTotalstudyTime_J"]);
        knjCreateHidden($objForm, "useRemark1", $model->useRemark1);
        knjCreateHidden($objForm, "REMARK1_label", $model->remark1Name);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd133jindex.php", "", "main");
        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd133jForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
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
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//CSV処理作成
function makeCsv(&$objForm, &$arg)
{
    //ファイル
    $extra = "";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);
    //取込ボタン
    $extra = "onclick=\"return btn_submit('csvInput');\"";
    $arg["btn_input"] = knjCreateBtn($objForm, "btn_input", "CSV取込", $extra);
    //出力ボタン
    $extra = "onclick=\"return btn_submit('csvOutput');\"";
    $arg["btn_output"] = knjCreateBtn($objForm, "btn_output", "CSV出力", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $counter)
{
    //定型文選択ボタン
    if ($model->Properties["tutisyoTeikei_Button_Hyouji"] == "1") {
        //学習内容
        $extra  = "onclick=\"return btn_submit('teikei');\"";
        $extra .= ($model->field["CHAIRCD"] && $counter > 0) ? "" : " disabled";
        $arg["btn_teikei"] = knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);

        //評価
        if ($model->Properties["useTotalstudyTime"] == '1') {
            $extra  = "onclick=\"return btn_submit('teikei2');\"";
            $extra .= ($model->field["CHAIRCD"] && $counter > 0) ? "" : " disabled";
            $arg["btn_teikei2"] = knjCreateBtn($objForm, "btn_teikei2", "定型文選択", $extra);
        }
    }
    if ($model->useRemark1) {
        $extra  = "onclick=\"return btn_submit('teikei3');\"";
        $extra .= ($model->field["CHAIRCD"] && $counter > 0) ? "" : " disabled";
        $arg["btn_teikei3"] = knjCreateBtn($objForm, "btn_teikei3", "定型文選択", $extra);
    }
    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //一括更新ボタンを作成する
    $link = REQUESTROOT."/D/KNJD133J/knjd133jindex.php?cmd=replace&CHAIRCD=".$model->field["CHAIRCD"]."&SUBCLASSCD=".$model->field["SUBCLASSCD"];
    $extra = ($model->field["CHAIRCD"]) ? "onclick=\"Page_jumper('$link');\"" : "disabled";
    $arg["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);
}
