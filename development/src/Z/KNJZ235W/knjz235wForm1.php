<?php

require_once('for_php7.php');

class knjz235wForm1
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

        //帳票出力ラジオボタン 1:A 2:B 3:C
        $opt001 = array(1, 2, 3);
        $model->field["SEQ001"] = ($model->field["SEQ001"]) ? $model->field["SEQ001"] : ($dataTmp["001"]["REMARK1"] ? $dataTmp["001"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0011\"", "id=\"SEQ0012\"", "id=\"SEQ0013\"");
        $radioArray = knjCreateRadio($objForm, "SEQ001", $model->field["SEQ001"], $extra, $opt001, get_count($opt001));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //科目ラジオボタン 1:科目名 2:講座名 3:講座番号(下3桁)付講座名
        $opt002 = array(1, 2, 3);
        $model->field["SEQ002"] = ($model->field["SEQ002"]) ? $model->field["SEQ002"] : ($dataTmp["002"]["REMARK1"] ? $dataTmp["002"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0021\"", "id=\"SEQ0022\"", "id=\"SEQ0023\"");
        $radioArray = knjCreateRadio($objForm, "SEQ002", $model->field["SEQ002"], $extra, $opt002, get_count($opt002));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //年組番／学籍番号ラジオボタン 1:年組番号 2:学籍番号
        $opt003 = array(1, 2);
        $model->field["SEQ003"] = ($model->field["SEQ003"]) ? $model->field["SEQ003"] : ($dataTmp["003"]["REMARK1"] ? $dataTmp["003"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0031\"", "id=\"SEQ0032\"");
        $radioArray = knjCreateRadio($objForm, "SEQ003", $model->field["SEQ003"], $extra, $opt003, get_count($opt003));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //欠課時数ラジオボタン 1:欠課数 2:欠課数／時間数 3:出席数／時数
        $opt004 = array(1, 2, 3);
        $model->field["SEQ004"] = ($model->field["SEQ004"]) ? $model->field["SEQ004"] : ($dataTmp["004"]["REMARK1"] ? $dataTmp["004"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0041\"", "id=\"SEQ0042\"", "id=\"SEQ0043\"");
        $radioArray = knjCreateRadio($objForm, "SEQ004", $model->field["SEQ004"], $extra, $opt004, get_count($opt004));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $opt005 = array(1, 2);
        $model->field["SEQ005"] = ($model->field["SEQ005"]) ? $model->field["SEQ005"] : ($dataTmp["005"]["REMARK1"] ? $dataTmp["005"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0051\"", "id=\"SEQ0052\"");
        $radioArray = knjCreateRadio($objForm, "SEQ005", $model->field["SEQ005"], $extra, $opt005, get_count($opt005));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //平均・順位（学級）チェックボックス
        $extra = "";
        if ($model->field["SEQ0061"] == "1" ||
            (!get_count($dataTmp)) ||
            (get_count($dataTmp) > 0 && $dataTmp["006"]["REMARK1"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ0061\"";
        $arg["data"]["SEQ0061"] = knjCreateCheckBox($objForm, "SEQ0061", "1", $extra, "");

        //平均・順位（コース）チェックボックス
        $extra = "";
        if ($model->field["SEQ0062"] == "1" ||
            (!get_count($dataTmp)) ||
            (get_count($dataTmp) > 0 && $dataTmp["006"]["REMARK2"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ0062\"";
        $arg["data"]["SEQ0062"] = knjCreateCheckBox($objForm, "SEQ0062", "1", $extra, "");

        //平均・順位（学年）チェックボックス
        $extra = "";
        if ($model->field["SEQ0063"] == "1" ||
            (!get_count($dataTmp)) ||
            (get_count($dataTmp) > 0 && $dataTmp["006"]["REMARK3"] == "1")) {
            $extra = "checked";
        }
        $extra .= " id=\"SEQ0063\"";
        $arg["data"]["SEQ0063"] = knjCreateCheckBox($objForm, "SEQ0063", "1", $extra, "");

        //特別活動の記録ラジオボタン 1:表示する 2:表示しない
        $opt007 = array(1, 2);
        $model->field["SEQ007"] = ($model->field["SEQ007"]) ? $model->field["SEQ007"] : ($dataTmp["007"]["REMARK1"] ? $dataTmp["007"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0071\"", "id=\"SEQ0072\"");
        $radioArray = knjCreateRadio($objForm, "SEQ007", $model->field["SEQ007"], $extra, $opt007, get_count($opt007));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //総合的な学習の時間/検定ラジオボタン 1:表示する 2:表示しない
        $opt008 = array(1, 2);
        $model->field["SEQ008"] = ($model->field["SEQ008"]) ? $model->field["SEQ008"] : ($dataTmp["008"]["REMARK1"] ? $dataTmp["008"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0081\"", "id=\"SEQ0082\"");
        $radioArray = knjCreateRadio($objForm, "SEQ008", $model->field["SEQ008"], $extra, $opt008, get_count($opt008));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //備考ラジオボタン 1:表示する 2:表示しない
        $opt009 = array(1, 2);
        $model->field["SEQ009"] = ($model->field["SEQ009"]) ? $model->field["SEQ009"] : ($dataTmp["009"]["REMARK1"] ? $dataTmp["009"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0091\"", "id=\"SEQ0092\"");
        $radioArray = knjCreateRadio($objForm, "SEQ009", $model->field["SEQ009"], $extra, $opt009, get_count($opt009));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //定型コメントラジオボタン 1:表示する 2:表示しない
        $opt010 = array(1, 2);
        $model->field["SEQ010"] = ($model->field["SEQ010"]) ? $model->field["SEQ010"] : ($dataTmp["010"]["REMARK1"] ? $dataTmp["010"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0101\"", "id=\"SEQ0102\"");
        $radioArray = knjCreateRadio($objForm, "SEQ010", $model->field["SEQ010"], $extra, $opt010, get_count($opt010));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //未履修科目コメントラジオボタン 1:表示する 2:表示しない
        $opt011 = array(1, 2);
        $model->field["SEQ011"] = ($model->field["SEQ011"]) ? $model->field["SEQ011"] : ($dataTmp["011"]["REMARK1"] ? $dataTmp["011"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0111\"", "id=\"SEQ0112\"");
        $radioArray = knjCreateRadio($objForm, "SEQ011", $model->field["SEQ011"], $extra, $opt011, get_count($opt011));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //増加単位 1:加算する 2:加算しない
        $opt021 = array(1, 2);
        $model->field["SEQ021"] = ($model->field["SEQ021"]) ? $model->field["SEQ021"] : ($dataTmp["021"]["REMARK1"] ? $dataTmp["021"]["REMARK1"] : "2");
        $extra = array("id=\"SEQ0211\"", "id=\"SEQ0212\"");
        $radioArray = knjCreateRadio($objForm, "SEQ021", $model->field["SEQ021"], $extra, $opt021, get_count($opt021));
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
        View::toHTML($model, "knjz235wForm1.html", $arg);
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
