<?php

require_once('for_php7.php');

class knjz235wForm4
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
            $arg["schkind"] = "1";
            //校種コンボ
            $extra = "onchange=\"return btn_submit('changeKind');\"";
            $arg["data"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $model->schoolKind, $model->SchKindOpt, $extra, 1);
        }

        //データ取得
        $dataTmp = array();
        $query = knjz235wQuery::getHreportConditionDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataTmp[$row["SEQ"]] = $row;
        }
        $result->free();

        //帳票パターンラジオボタン 1:A 2:B
        $opt001 = array(1, 2, 3);
        $model->field["SEQ001"] = ($model->field["SEQ001"]) ? $model->field["SEQ001"] : ($dataTmp["001"]["REMARK1"] ? $dataTmp["001"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0011\" onclick=\"setPattern()\"", "id=\"SEQ0012\" onclick=\"setPattern()\"", "id=\"SEQ0013\" onclick=\"setPattern()\"");
        $radioArray = knjCreateRadio($objForm, "SEQ001", $model->field["SEQ001"], $extra, $opt001, get_count($opt001));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出欠集計範囲
        $arg["data"]["SDATE"] = str_replace("-", "/", $db->getOne(knjz235wQuery::getSemester()));
        
        //出欠集計範囲
        $model->field["SEQ002"] = ($model->field["SEQ002"]) ? $model->field["SEQ002"] : ($dataTmp["002"]["REMARK1"] ? $dataTmp["002"]["REMARK1"] : str_replace("-", "/", CTRL_DATE));
        $arg["data"]["SEQ002"] = View::popUpCalendar($objForm, "SEQ002", $model->field["SEQ002"], "");
        
        //記載日付
        $model->field["SEQ003"] = ($model->field["SEQ003"]) ? $model->field["SEQ003"] : ($dataTmp["003"]["REMARK1"] ? $dataTmp["003"]["REMARK1"] : str_replace("-", "/", CTRL_DATE));
        $arg["data"]["SEQ003"] = View::popUpCalendar($objForm, "SEQ003", $model->field["SEQ003"], "");

        //「欠点(評価)は、不振チェック参照するか？」のフラグを取得
        $query = knjz235wQuery::getNameMstD048();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $useSlumpD048 = $rtnRow["NAMESPARE1"];

        //欠点テキストボックス表示判定
        //「欠点(評価)は、不振チェック参照するか？」の判定
        if ($useSlumpD048 == '1') {
            $arg["USE_SLUMP_D048"] = '1'; //null以外なら何でもいい
            unset($arg["KETTEN_FLG"]);
        } else {
            $arg["KETTEN_FLG"] = '1'; //null以外なら何でもいい
            unset($arg["USE_SLUMP_D048"]);
        }

        //欠点 評価
        $model->field["SEQ0041"] = ($model->field["SEQ0041"]) ? $model->field["SEQ0041"] : ($dataTmp["004"]["REMARK1"] ? $dataTmp["004"]["REMARK1"] : "2");
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SEQ0041"] = knjCreateTextBox($objForm, $model->field["SEQ0041"], "SEQ0041", 4, 4, $extra);
        
        //欠点 評定
        $model->field["SEQ0042"] = ($model->field["SEQ0042"]) ? $model->field["SEQ0042"] : ($dataTmp["004"]["REMARK2"] ? $dataTmp["004"]["REMARK2"] : "1");
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SEQ0042"] = knjCreateTextBox($objForm, $model->field["SEQ0042"], "SEQ0042", 4, 4, $extra);

        //増加単位を反映する
        makeCheckBox($objForm, $model, $dataTmp, $arg, '005', 'REMARK1', 'SEQ005');

        //担任印出力
        makeCheckBox($objForm, $model, $dataTmp, $arg, '006', 'REMARK1', 'SEQ006');

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
        View::toHTML($model, "knjz235wForm4.html", $arg);
    }
}

//チェックボックス作成
function makeCheckBox($objForm, $model, $dataTmp, &$arg, $fieldSEQ, $remark, $id, $defCheck = false)
{
    $extra = "";
    if ($model->field[$id] == "1" ||
        (get_count($dataTmp) > 0 && $dataTmp[$fieldSEQ][$remark] == "1") ||
        (get_count($dataTmp) == 0 && $defCheck)) {
        $extra = "checked";
    }
    $extra .= " id=\"{$id}\"";
    $arg["data"][$id] = knjCreateCheckBox($objForm, $id, "1", $extra, "");
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
