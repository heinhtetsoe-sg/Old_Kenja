<?php

require_once('for_php7.php');

class knjz235wForm8
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz235windex.php", "", "edit");

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

        if ($model->field2['TYOUHYOU_PATTERN'] == "1") { //知的用
            $arg["titeki"] = "1";
        } else { //準ずる教育用
            $arg["jyunzuru"] = "1";
        }

        //データ取得
        $dataTmp = array();
        $query = knjz235wQuery::getHreportConditionDat($model);
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

        //主に使用する様式
        if ($model->field["SEQ002"]) {
            $value = $model->field["SEQ002"];
        } elseif ($dataTmp["002"]["REMARK1"]) {
            $value = knjz235wQuery::getPatternUseMainlyLMRadioVal($db, $model, $dataTmp["002"]["REMARK1"]);
        } else {
            if ($model->field2['TYOUHYOU_PATTERN'] == "1") {
                $value = knjz235wQuery::getPatternUseMainlyLMRadioVal($db, $model, "103");           //知的は「文言評価(3枠)」がデフォルト
            } else {
                $defaultNamecd2 = ($model->schoolKind == "A") ? "201H" : "201".$model->schoolKind;   //準ずる教育ではNAMECD2=「201+校種」がデフォルト(理療のみH)
                $value = knjz235wQuery::getPatternUseMainlyLMRadioVal($db, $model, $defaultNamecd2);
            }
        }
        $model->namecdA035 = array();
        $radioCnt = $db->getOne(knjz235wQuery::getPatternUseMainlyLM($model, "cnt")); //様式ラジオボタン数取得
        $opt = ($radioCnt > 0) ? range(1, $radioCnt) : array();
        $extra = array();
        foreach ($opt as $key => $val) {
            if ($model->field2['TYOUHYOU_PATTERN'] == "1") {
                //主に使用する様式「文言評価(3枠)B」を選択した場合は、行動の記録は「表示しない」という文字列を表示する。
                array_push($extra, " id=\"SEQ002{$val}\" onClick=\"btn_submit('edit')\"");
            } else {
                array_push($extra, " id=\"SEQ002{$val}\"");
            }
        }
        $radioArray = knjCreateRadio($objForm, "SEQ002", $value, $extra, $opt, get_count($opt));
        $query = knjz235wQuery::getPatternUseMainlyLM($model, "data");
        $result = $db->query($query);
        $sep = "";
        $radioVal = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ラジオ値とA035データの対応
            $model->namecdA035[$radioVal] = array("value" => $row["VALUE"],
                                                  "label" => $row["LABEL"]);

            //フォーム表示
            $id = "SEQ002".$radioVal;
            $yousikiList["RADIO"]   = $radioArray[$id];
            $yousikiList["ID"]      = $id;
            $yousikiList["LABEL"]   = $row["LABEL"];
            $yousikiList["SEP"]     = $sep;

            $arg["yousikiList"][] = $yousikiList;

            $sep = "&nbsp;";
            $radioVal++;
        }
        $namecdA035Value = $model->namecdA035[$value]["value"];

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

        //担任表示
        $opt = array(1, 2);
        if ($model->field["SEQ003"]) {
            $value = $model->field["SEQ003"];
        } elseif ($dataTmp["003"]["REMARK1"]) {
            $value = $dataTmp["003"]["REMARK1"];
        } else {
            $value = 1;
        }
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SEQ003{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEQ003", $value, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //教育目標・目指す生徒像
        $extra = "";
        $value = ($model->field["SEQ013"]) ? $model->field["SEQ013"]: $dataTmp["013"]["REMARK10"];
        $arg["data"]["SEQ013"] = knjCreateTextArea($objForm, "SEQ013", $model->seq013_gyou, ($model->seq013_moji * 2), "soft", $extra, $value);
        $arg["data"]["SEQ013_COMMENT"] = "(全角".$model->seq013_moji."文字X".$model->seq013_gyou."行まで)";

        //年間目標
        $opt = array(1, 2);
        if ($model->field["SEQ009"]) {
            $value = $model->field["SEQ009"];
        } elseif ($dataTmp["009"]["REMARK1"]) {
            $value = $dataTmp["009"]["REMARK1"];
        } else {
            $value = 1;
        }
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SEQ009{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEQ009", $value, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //行動の記録
        if ($namecdA035Value == "104") {
            //主に使用する様式「文言評価(3枠)B」を選択した場合は、行動の記録は「表示しない」という文字列を表示する。
            knjCreateHidden($objForm, "SEQ008", "2"); //表示しない
            $arg["youshiki104"] = "1";
        } else {
            $arg["youshiki104igai"] = "1";
            $opt = array(1, 2);
            if ($model->field["SEQ008"]) {
                $value = $model->field["SEQ008"];
            } elseif ($dataTmp["008"]["REMARK1"]) {
                $value = $dataTmp["008"]["REMARK1"];
            } else {
                $value = 1;
            }
            $extra = array();
            foreach ($opt as $key => $val) {
                array_push($extra, " id=\"SEQ008{$val}\"");
            }
            $radioArray = knjCreateRadio($objForm, "SEQ008", $value, $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //家庭より
        $opt = array(1, 2);
        if ($model->field["SEQ012"]) {
            $value = $model->field["SEQ012"];
        } elseif ($dataTmp["012"]["REMARK1"]) {
            $value = $dataTmp["012"]["REMARK1"];
        } else {
            $value = 1;
        }
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SEQ012{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEQ012", $value, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
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
        View::toHTML($model, "knjz235wForm8.html", $arg);
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

//ラジオ作成
function paternRadio(&$objForm, $name, $value, $extra, $multi, $count, $valArray)
{
    $ret = array();

    for ($i = 1; $i <= $count; $i++) {
        if (is_array($extra)) {
            $ext = $extra[$i-1];
        } else {
            $ext = $extra;
        }
        $objForm->ae(array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $ext,
                            "multiple"  => $multi));
        $ret[$name.$i] = $objForm->ge($name, $valArray[$i - 1]);
    }

    return $ret;
}
