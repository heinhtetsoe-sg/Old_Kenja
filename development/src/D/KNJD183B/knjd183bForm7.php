<?php

require_once('for_php7.php');

class knjd183bForm7
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd183bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        if ($model->cmd == "") {
            unset($model->field);
        }
        if ($model->cmd == "reset") {
            unset($model->field);
        }

        $hasPostData = true;
        if (!isset($model->field)) {
            $hasPostData = false;
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->SchKindOpt[0] != "") {
            //校種コンボ
            $extra = "onchange=\"return btn_submit('changeKind');\"";
            $arg["data"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $model->schoolKind, $model->SchKindOpt, $extra, 1);
        }

        //帳票パターン
        $opt = array(1, 2);
        $model->field2['TYOUHYOU_PATTERN'] = ($model->field2['TYOUHYOU_PATTERN'] == "") ? '1' : $model->field2['TYOUHYOU_PATTERN'];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"TYOUHYOU_PATTERN{$val}\" onClick=\"btn_submit('changeKind')\"");
        }
        $radioArray = knjCreateRadio($objForm, "TYOUHYOU_PATTERN", $model->field2['TYOUHYOU_PATTERN'], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //データ取得
        $dataTmp = array();
        $query = knjd183bQuery::getHreportConditionDat($model);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field2['TYOUHYOU_PATTERN'] == "1") {
                if (100 <= intval($row["SEQ"]) && intval($row["SEQ"]) < 200) {
                    $seq = sprintf("%03d", intval($row["SEQ"]) - 100);
                    $dataTmp[$seq] = $row;
                }
            } else {
                if (200 <= intval($row["SEQ"]) && intval($row["SEQ"]) < 300) {
                    $seq = sprintf("%03d", intval($row["SEQ"]) - 200);
                    $dataTmp[$seq] = $row;
                }
            }
        }
        $result->free();

        if ($model->field2["TYOUHYOU_PATTERN"] == "1") {
            //知的用
            $arg["titekiyou"] = true;

            //主に使用する様式
            $namecd2 = "'101', '102', '103'";
            makePatternUseMainly($objForm, $arg, $model->field["SEQ002"], $db, $namecd2, "002", $dataTmp, "101");

            //表題
            $extra = "";
            $value = ($model->field["SEQ001"]) ? $model->field["SEQ001"]: $dataTmp["001"]["REMARK10"];
            $arg["data"]["SEQ001"] = knjCreateTextBox($objForm, $value, "SEQ001", 21, 30, $extra);

            //クラス表示
            $opt = array(1, 2, 3);
            if ($model->field["SEQ004"]) {
                $value = $model->field["SEQ004"];
            } elseif ($dataTmp["004"]["REMARK1"]) {
                $value = $dataTmp["004"]["REMARK1"];
            } else {
                $value = 1;
            }
            $extra = array();
            foreach ($opt as $key => $val) {
                array_push($extra, " id=\"SEQ004{$val}\"");
            }
            $radioArray = knjCreateRadio($objForm, "SEQ004", $value, $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }

            //学校より
            $extra = " id=\"SEQ011\" ";
            makeCheckBox($objForm, $arg, $model->field["SEQ011"], $extra, $dataTmp, "011", "SEQ011", "REMARK1", $hasPostData, "");
        } else {
            //準ずる教育用
            $arg["junzurukyouikuyou"] = true;

            //主に使用する様式
            $namecd2 = "'201', '202', '203', '204', '205'";
            $onClick = " onClick=\"btn_submit('edit')\" ";
            makePatternUseMainly($objForm, $arg, $model->field["SEQ002"], $db, $namecd2, "002", $dataTmp, "201", $onClick);

            //表題
            $extra = "";
            $value = ($model->field["SEQ001"]) ? $model->field["SEQ001"]: $dataTmp["001"]["REMARK10"];
            $arg["data"]["SEQ001"] = knjCreateTextBox($objForm, $value, "SEQ001", 21, 30, $extra);

            //クラス表示
            $opt = array(1, 2, 3);
            if ($model->field["SEQ004"]) {
                $value = $model->field["SEQ004"];
            } elseif ($dataTmp["004"]["REMARK1"]) {
                $value = $dataTmp["004"]["REMARK1"];
            } else {
                $value = 1;
            }
            $extra = array();
            foreach ($opt as $key => $val) {
                array_push($extra, " id=\"SEQ004{$val}\"");
            }
            $radioArray = knjCreateRadio($objForm, "SEQ004", $value, $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }

            //総合所見
            if ($model->field["SEQ002"] == "205") {
                $disabled = " disabled ";
                $checked = "";
                $hiddenValue = ($hasPostData) ? $model->field["SEQ010_1"] : $dataTmp["010"]["REMARK1"];
                $arg["SEQ010_1_HIDDEN"] = "<input type='hidden' name='SEQ010_1' value='{$hiddenValue}'>";
                $model->field["SEQ010_1"] = "";
            } else {
                $disabled = "";
                $checked = "undefined";
            }
            $extra = " id=\"SEQ010_1\" onClick=\"btn_submit('edit')\" ";
            makeCheckBox($objForm, $arg, $model->field["SEQ010_1"], $extra, $dataTmp, "010", "SEQ010_1", "REMARK1", $hasPostData, $disabled, $checked);

            //総合所見の項目名
            $disabled = ($model->field["SEQ010_1"] === "1") ? " disabled " : "";
            $extra = $disabled;
            $value = ($model->field["SEQ010_2"]) ? $model->field["SEQ010_2"]: $dataTmp["010"]["REMARK10"];
            $arg["data"]["SEQ010_2"] = knjCreateTextBox($objForm, $value, "SEQ010_2", 42, 60, $extra);

            //行動の記録
            if ($model->field["SEQ002"] == "205") {
                $disabled = " disabled ";
                $checked = " checked ";
                $hiddenValue = ($hasPostData) ? $model->field["SEQ008"] : $dataTmp["008"]["REMARK1"];
                $arg["SEQ008_HIDDEN"] = "<input type='hidden' name='SEQ008' value='{$hiddenValue}'>";
            } else {
                $disabled = "";
                $checked = "undefined";
            }
            $extra = " id=\"SEQ008\" ";
            makeCheckBox($objForm, $arg, $model->field["SEQ008"], $extra, $dataTmp, "008", "SEQ008", "REMARK1", $hasPostData, $disabled, $checked);

            //学校より
            if ($model->field["SEQ002"] == "205") {
                $disabled = " disabled ";
                $checked = " checked ";
                $hiddenValue = ($hasPostData) ? $model->field["SEQ011"] : $dataTmp["011"]["REMARK1"];
                $arg["SEQ011_HIDDEN"] = "<input type='hidden' name='SEQ011' value='{$hiddenValue}'>";
            } else {
                $disabled = "";
                $checked = "undefined";
            }
            $extra = " id=\"SEQ011\" ";
            makeCheckBox($objForm, $arg, $model->field["SEQ011"], $extra, $dataTmp, "011", "SEQ011", "REMARK1", $hasPostData, $disabled, $checked);
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["pattern"] = $model->pattern;

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd183bForm7.html", $arg);
    }
}

//チェックボックス作成
function makeCheckBox(&$objForm, &$arg, &$value, $extra, $dataTmp, $seq, $fieldSeq, $remark, $hasPostData, $disabled, $checked = "undefined")
{
    if ($checked === "undefined") {
        if (!$hasPostData) {
            $value = $dataTmp[$seq][$remark];
        }
        $checked = $value ? " checked " : "";
    }
    $arg["data"][$fieldSeq] = knjCreateCheckBox($objForm, $fieldSeq, "1", $extra.$disabled.$checked, "");
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //前年度からコピー
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
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
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//主に使用する様式作成
function makePatternUseMainly(&$objForm, &$arg, &$value, $db, $namecd2, $seq, $dataTmp, $defaultValue, $onClick = "")
{
    $fieldSeq = "SEQ".$seq;
    $query = knjd183bQuery::getPatternUseMainly($namecd2);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = $row["NAMECD2"];
        $arg["data"][$fieldSeq.$row["NAMECD2"]."_LABEL"] = $row["NAME1"];
    }
    if ($value) {
    } elseif ($value == "" && $dataTmp[$seq]["REMARK1"]) {
        $value = $dataTmp[$seq]["REMARK1"];
    } else {
        $value = $defaultValue;
    }
    $extra = array();
    foreach ($opt as $key => $val) {
        array_push($extra, " id=\"{$fieldSeq}{$val}\"".$onClick);
    }
    $radioArray = knjCreateRadio($objForm, $fieldSeq, $value, $extra, $opt, get_count($opt), $opt);
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }
}
