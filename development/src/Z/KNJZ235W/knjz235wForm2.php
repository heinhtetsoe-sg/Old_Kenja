<?php

require_once('for_php7.php');

class knjz235wForm2
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

        //帳票出力ラジオボタン 1:A 2:B 3:C 4:D 5:E 6:F
        $opt001 = array(1, 2, 3, 4, 5, 6);
        $model->field["SEQ001"] = ($model->field["SEQ001"]) ? $model->field["SEQ001"] : ($dataTmp["001"]["REMARK1"] ? $dataTmp["001"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0011\" onclick=\"setPattern()\"", "id=\"SEQ0012\" onclick=\"setPattern()\"", "id=\"SEQ0013\" onclick=\"setPattern()\"", "id=\"SEQ0014\" onclick=\"setPattern()\"", "id=\"SEQ0015\" onclick=\"setPattern()\"", "id=\"SEQ0016\" onclick=\"setPattern()\"");
        $radioArray = knjCreateRadio($objForm, "SEQ001", $model->field["SEQ001"], $extra, $opt001, get_count($opt001));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }
        
        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $opt005 = array(1, 2);
        $model->field["SEQ005"] = ($model->field["SEQ005"]) ? $model->field["SEQ005"] : ($dataTmp["005"]["REMARK1"] ? $dataTmp["005"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0051\"", "id=\"SEQ0052\"",);
        $radioArray = knjCreateRadio($objForm, "SEQ005", $model->field["SEQ005"], $extra, $opt005, get_count($opt005));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //席次（クラス）チェックボックス
        makeCheckBox($objForm, $model, $dataTmp, $arg, '006', 'REMARK1', 'SEQ0061');
        
        //席次（コース）チェックボックス
        makeCheckBox($objForm, $model, $dataTmp, $arg, '006', 'REMARK2', 'SEQ0062');
        
        //席次（学年）チェックボックス
        makeCheckBox($objForm, $model, $dataTmp, $arg, '006', 'REMARK3', 'SEQ0063');
        
        //席次（学科）チェックボックス
        makeCheckBox($objForm, $model, $dataTmp, $arg, '006', 'REMARK4', 'SEQ0064');

        //順位表記
        makeCheckBox($objForm, $model, $dataTmp, $arg, '012', 'REMARK1', 'SEQ012');
        
        //追指導表示
        makeCheckBox($objForm, $model, $dataTmp, $arg, '013', 'REMARK1', 'SEQ013');
        
        //欠点表示
        $model->field["SEQ014"] = ($model->field["SEQ014"]) ? $model->field["SEQ014"] : ($dataTmp["014"]["REMARK1"] ? $dataTmp["014"]["REMARK1"] : (is_array($dataTmp["014"]) ? "" : "40"));
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SEQ014"] = knjCreateTextBox($objForm, $model->field["SEQ014"], "SEQ014", 4, 4, $extra);
        
        //欠課時数
        $model->field["SEQ0151"] = ($model->field["SEQ0151"]) ? $model->field["SEQ0151"] : ($dataTmp["015"]["REMARK1"] ? $dataTmp["015"]["REMARK1"] : "1");
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SEQ0151"] = knjCreateTextBox($objForm, $model->field["SEQ0151"], "SEQ0151", 4, 4, $extra);
        
        $model->field["SEQ0152"] = ($model->field["SEQ0152"]) ? $model->field["SEQ0152"] : ($dataTmp["015"]["REMARK2"] ? $dataTmp["015"]["REMARK2"] : "3");
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SEQ0152"] = knjCreateTextBox($objForm, $model->field["SEQ0152"], "SEQ0152", 4, 4, $extra);
        
        //出力順設定
        makeCheckBox($objForm, $model, $dataTmp, $arg, '016', 'REMARK1', 'SEQ016');
        
        //総合的な学習の時間
        makeCheckBox($objForm, $model, $dataTmp, $arg, '008', 'REMARK1', 'SEQ008');
        
        //特別活動
        makeCheckBox($objForm, $model, $dataTmp, $arg, '007', 'REMARK1', 'SEQ007');
        
        //所見欄
        makeCheckBox($objForm, $model, $dataTmp, $arg, '017', 'REMARK1', 'SEQ017');
        
        //科目担当教員
        makeCheckBox($objForm, $model, $dataTmp, $arg, '018', 'REMARK1', 'SEQ018');
        
        //出欠の記録
        makeCheckBox($objForm, $model, $dataTmp, $arg, '019', 'REMARK1', 'SEQ019');
        
        //追指導表示
        makeCheckBox($objForm, $model, $dataTmp, $arg, '020', 'REMARK1', 'SEQ020');
        
        //評定表示
        makeCheckBox($objForm, $model, $dataTmp, $arg, '021', 'REMARK1', 'SEQ021');
        
        //増加単位を加算する 1:加算する 2:加算しない
        $opt022 = array(1, 2);
        $model->field["SEQ022"] = ($model->field["SEQ022"]) ? $model->field["SEQ022"] : ($dataTmp["022"]["REMARK1"] ? $dataTmp["022"]["REMARK1"] : "2");
        $extra = array("id=\"SEQ0221\"", "id=\"SEQ0222\"",);
        $radioArray = knjCreateRadio($objForm, "SEQ022", $model->field["SEQ022"], $extra, $opt022, get_count($opt022));
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
        View::toHTML($model, "knjz235wForm2.html", $arg);
    }
}

//チェックボックス作成
function makeCheckBox($objForm, $model, $dataTmp, &$arg, $fieldSEQ, $remark, $id)
{
    $extra = "";
    if ($model->field[$id] == "1" ||
        (!get_count($dataTmp)) ||
        (get_count($dataTmp) > 0 && $dataTmp[$fieldSEQ][$remark] == "1")) {
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
