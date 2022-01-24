<?php

require_once('for_php7.php');

class knjd183bForm1
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
        $juniFlag = "";
        $dataTmp = array();
        $query = knjd183bQuery::getHreportConditionDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataTmp[$row["SEQ"]] = $row;
            $juniFlag = "1";
        }
        $result->free();

        //住所チェックボックス
        $extra = "";
        if ($model->field["SEQ001"] == "1" ||
            (!get_count($dataTmp)) ||
            (get_count($dataTmp) > 0 && $dataTmp["001"]["REMARK1"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ001\"";
        $arg["data"]["SEQ001"] = knjCreateCheckBox($objForm, "SEQ001", "1", $extra, "");

        //氏名チェックボックス
        $extra = "";
        if ($model->field["SEQ002"] == "1" ||
            (!get_count($dataTmp)) ||
            (get_count($dataTmp) > 0 && $dataTmp["002"]["REMARK1"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ002\"";
        $arg["data"]["SEQ002"] = knjCreateCheckBox($objForm, "SEQ002", "1", $extra, "");

        //学期/学年末評価チェックボックス
        $extra = "";
        if ($model->field["SEQ003"] == "1" ||
            (get_count($dataTmp) > 0 && $dataTmp["003"]["REMARK1"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ003\"";
        $arg["data"]["SEQ003"] = knjCreateCheckBox($objForm, "SEQ003", "1", $extra, "");

        //学年末評定チェックボックス
        $extra = "";
        if ($model->field["SEQ004"] == "1" ||
            (!get_count($dataTmp)) ||
            (get_count($dataTmp) > 0 && $dataTmp["004"]["REMARK1"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ004\"";
        $arg["data"]["SEQ004"] = knjCreateCheckBox($objForm, "SEQ004", "1", $extra, "");

        //修得単位数チェックボックス
        $extra = "";
        if ($model->field["SEQ005"] == "1" ||
            (get_count($dataTmp) > 0 && $dataTmp["005"]["REMARK1"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ005\"";
        $arg["data"]["SEQ005"] = knjCreateCheckBox($objForm, "SEQ005", "1", $extra, "");

        //総点チェックボックス
        $extra = "";
        if ($model->field["SEQ006"] == "1" ||
            (!get_count($dataTmp)) ||
            (get_count($dataTmp) > 0 && $dataTmp["006"]["REMARK1"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ006\" onChange=\"check_checkBox()\"";
        $arg["data"]["SEQ006"] = knjCreateCheckBox($objForm, "SEQ006", "1", $extra, "");

        //個人平均チェックボックス
        $extra = "";
        if ($model->field["SEQ007"] == "1" ||
            (!get_count($dataTmp)) ||
            (get_count($dataTmp) > 0 && $dataTmp["007"]["REMARK1"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ007\" onChange=\"check_checkBox()\"";
        $arg["data"]["SEQ007"] = knjCreateCheckBox($objForm, "SEQ007", "1", $extra, "");

        //学級平均チェックボックス
        $extra = "";
        if ($model->field["SEQ008"] == "1" ||
            (!get_count($dataTmp)) ||
            (get_count($dataTmp) > 0 && $dataTmp["008"]["REMARK1"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ008\" onChange=\"check_checkBox()\"";
        $arg["data"]["SEQ008"] = knjCreateCheckBox($objForm, "SEQ008", "1", $extra, "");

        //順位チェックボックス
        $extra = "";
        if ($model->field["SEQ009"] == "1" ||
            (get_count($dataTmp) > 0 && $dataTmp["009"]["REMARK1"] == "1")) {
            $extra = "checked";
        } elseif ($juniFlag == "") {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ009\" onchange=\"return check_juni_gakushu();\"";
        $arg["data"]["SEQ009"] = knjCreateCheckBox($objForm, "SEQ009", "1", $extra, "");

        //順位出力 1:クラス 2:学年
        $extra = "";
        $opt020 = array(1, 2);
        $model->field["SEQ020"] = ($model->field["SEQ020"]) ? $model->field["SEQ020"] : $dataTmp["009"]["REMARK2"];
        if ($dataTmp["009"]["REMARK1"] == "" &&  $juniFlag == "1") {
            $extra = array("disabled id=\"SEQ0201\"", "disabled id=\"SEQ0202\"");
        } else {
            $extra = array("id=\"SEQ0201\" checked ", "id=\"SEQ0202\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEQ020", $model->field["SEQ020"], $extra, $opt020, get_count($opt020));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //基準点 1:平均点 2:合計点
        $extra = "";
        $opt021 = array(1, 2);
        $model->field["SEQ021"] = ($model->field["SEQ021"]) ? $model->field["SEQ021"] : $dataTmp["009"]["REMARK3"];
        if ($dataTmp["009"]["REMARK1"] == "" &&  $juniFlag == "1") {
            $extra = array("disabled id=\"SEQ0211\"", "disabled id=\"SEQ0212\"");
        } else {
            $extra = array("id=\"SEQ0211\" checked ", "id=\"SEQ0212\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEQ021", $model->field["SEQ021"], $extra, $opt021, get_count($opt021));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //表示有無チェックボックス
        $extra = "";
        if ($model->field["SEQ010"] == "1" ||
            (!get_count($dataTmp)) ||
            (get_count($dataTmp) > 0 && $dataTmp["010"]["REMARK1"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ010\"";
        $arg["data"]["SEQ010"] = knjCreateCheckBox($objForm, "SEQ010", "1", $extra, "");

        //順位チェックボックス
        $extra = "";
        if ($model->field["SEQ011"] == "1" ||
            (!get_count($dataTmp)) ||
            (get_count($dataTmp) > 0 && $dataTmp["011"]["REMARK1"] == "1")) {
            $extra = "checked";
        } elseif ($juniFlag == "") {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ011\" onchange=\"return check_juni_teiki();\"";
        $arg["data"]["SEQ011"] = knjCreateCheckBox($objForm, "SEQ011", "1", $extra, "");

        //順位出力 1:クラス 2:学年
        $extra = "";
        $opt022 = array(1, 2);
        $model->field["SEQ022"] = ($model->field["SEQ022"]) ? $model->field["SEQ022"] : $dataTmp["011"]["REMARK2"];
        if ($dataTmp["011"]["REMARK1"] == "" && $juniFlag == "1") {
            $extra = array("disabled id=\"SEQ0221\"", "disabled id=\"SEQ0222\"");
        } else {
            $extra = array("id=\"SEQ0221\" checked ", "id=\"SEQ0222\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEQ022", $model->field["SEQ022"], $extra, $opt022, get_count($opt022));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //基準点 1:平均点 2:合計点
        $extra = "";
        $opt023 = array(1, 2);
        $model->field["SEQ023"] = ($model->field["SEQ023"]) ? $model->field["SEQ023"] : $dataTmp["011"]["REMARK3"];
        if ($dataTmp["011"]["REMARK1"] == ""  && $juniFlag == "1") {
            $extra = array("disabled id=\"SEQ0231\"", "disabled id=\"SEQ0232\"");
        } else {
            $extra = array("id=\"SEQ0231\" checked ", "id=\"SEQ0232\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEQ023", $model->field["SEQ023"], $extra, $opt023, get_count($opt023));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //入力枠パターン 1:総探のみ使用 2:資格・検定のみ使用 3:両方使用 4:両方表示しない
        $opt012 = array(1, 2, 3, 4);
        $model->field["SEQ012"] = ($model->field["SEQ012"]) ? $model->field["SEQ012"] : ($dataTmp["012"]["REMARK1"] ? $dataTmp["012"]["REMARK1"] : "3");
        $extra = array("id=\"SEQ0121\" onchange=\"return check_nyuryoku()\"",
                       "id=\"SEQ0122\" onchange=\"return check_nyuryoku()\"",
                       "id=\"SEQ0123\" onchange=\"return check_nyuryoku()\"",
                       "id=\"SEQ0124\" onchange=\"return check_nyuryoku()\"");
        $radioArray = knjCreateRadio($objForm, "SEQ012", $model->field["SEQ012"], $extra, $opt012, get_count($opt012));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //総合的な探究の時間見出し名 テキストボックス
        $model->field["SEQ013"] = ($model->field["SEQ013"]) ? $model->field["SEQ013"] : $dataTmp["013"]["REMARK1"];
        if ($model->field["SEQ012"] == '2' || $model->field["SEQ012"] == '4') {
            $extra = "disabled";
        } else {
            $extra = "";
        }
        $arg["data"]["SEQ013"] = knjCreateTextBox($objForm, $model->field["SEQ013"], "SEQ013", 30, 15, $extra);

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
        View::toHTML($model, "knjd183bForm1.html", $arg);
    }
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
