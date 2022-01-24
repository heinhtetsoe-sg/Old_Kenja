<?php

require_once('for_php7.php');

class knjz235wForm6
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
        if ($model->cmd == "reset") {
            unset($model->field);
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->SchKindOpt[0] != "") {
            //校種コンボ
            $extra = "onchange=\"return btn_submit('changeKind');\"";
            $arg["data"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $model->schoolKind, $model->SchKindOpt, $extra, 1);
        }

        // 状態区分
        $opt = array(1, 2);
        $model->field2['STATUS'] = ($model->field2['STATUS'] == "") ? '1' : $model->field2['STATUS'];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"STATUS{$val}\" onClick=\"btn_submit('changeKind')\"");
        }
        $radioArray = knjCreateRadio($objForm, "STATUS", $model->field2['STATUS'], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //データ取得
        $dataTmp = array();
        $query = knjz235wQuery::getHreportConditionDat($model);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field2['STATUS'] == "1") {
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

        //パターン
        $opt = array();
        $optVal = array();
        $firstVal = ($dataTmp["002"]["REMARK1"]) ? $dataTmp["002"]["REMARK1"] : "";
        $paternCnt = 1;
        $query = knjz235wQuery::getPatern();
        $result = $db->query($query);
        $optPatern = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optPatern[] = $row["NAMESPARE3"];
            $opt[] = $paternCnt;
            $optVal[] = $row["NAMECD2"];
            $firstVal = $firstVal ? $firstVal : $row["NAMECD2"];
            $arg["data"]["PATERN{$paternCnt}_LABEL"] = "<LABEL for=\"FRM_PATERN{$paternCnt}\">&nbsp;{$row["NAMESPARE3"]}</LABEL>";
            $paternCnt++;
        }
        $result->free();

        if ($model->field2["STATUS"] == "2") {
            $model->frmPatern = $model->frmPatern && !in_array($model->cmd, array("reset","changeKind")) ? $model->frmPatern : $firstVal;
        }

        $extra = array();
        $setDisabled = $model->field2["STATUS"] == "2" ? "" : " disabled ";
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"FRM_PATERN{$val}\" {$setDisabled} onClick=\"setPattern($val)\"");
        }
        $radioArray = paternRadio($objForm, "FRM_PATERN", $model->frmPatern, $extra, $opt, get_count($opt), $optVal);
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //帳票パターン
        //表題
        $extra = "";
        $value = ($model->field["SEQ001"]) ? $model->field["SEQ001"]: $dataTmp["001"]["REMARK10"];
        $arg["data"]["SEQ001"] = knjCreateTextBox($objForm, $value, "SEQ001", 21, 30, $extra);



        //クラス表示
        makeCheckBox($objForm, $model, $dataTmp, $arg, '004', 'REMARK1', 'SEQ004', '');

        //身体の記録
        $query = knjz235wQuery::getMedexamDerMonthDat();
        $extra = "";
        $value = ($model->field["SEQ005_1"]) ? $model->field["SEQ005_1"]: $dataTmp["005"]["REMARK1"];
        makeCmb($objForm, $arg, $db, $query, "SEQ005_1", $value, $extra, 1, "BLANK");
        $value = ($model->field["SEQ005_2"]) ? $model->field["SEQ005_2"]: $dataTmp["005"]["REMARK2"];
        makeCmb($objForm, $arg, $db, $query, "SEQ005_2", $value, $extra, 1, "BLANK");

        //視力
        makeCheckBox($objForm, $model, $dataTmp, $arg, '006', 'REMARK1', 'SEQ006', '');

        //聴力
        makeCheckBox($objForm, $model, $dataTmp, $arg, '007', 'REMARK1', 'SEQ007', '');

        //行動の記録
        makeCheckBox($objForm, $model, $dataTmp, $arg, '008', 'REMARK1', 'SEQ008', '');

        //年間目標
        $disabled = $model->field2['STATUS'] == "1" ? "" : " disabled ";
        makeCheckBox($objForm, $model, $dataTmp, $arg, '009', 'REMARK1', 'SEQ009', $disabled);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_FRM_PATERN", $model->frmPatern);

        //DB切断
        Query::dbCheckIn($db);

        $arg["pattern"] = $model->pattern;

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz235wForm6.html", $arg);
    }
}

//チェックボックス作成
function makeCheckBox($objForm, $model, $dataTmp, &$arg, $fieldSEQ, $remark, $id, $disabled, $defCheck = false)
{
    $extra = "";
    if ($model->field[$id] == "1" ||
        (get_count($dataTmp) > 0 && $dataTmp[$fieldSEQ][$remark] == "1") ||
        (get_count($dataTmp) == 0 && $defCheck)) {
        $extra = "checked";
    }
    $extra .= " id=\"{$id}\"";
    $arg["data"][$id] = knjCreateCheckBox($objForm, $id, "1", $disabled.$extra, "");
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
