<?php

require_once('for_php7.php');

class knjz235nForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz235nindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        if ($model->cmd == "reset") unset($model->field);

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
        $query = knjz235nQuery::getHreportConditionDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataTmp[$row["SEQ"]] = $row;
        }
        $result->free();

        //帳票パターンラジオボタン 1:A 2:B
        $opt051 = array(1, 2);
        $model->field["SEQ051"] = ($model->field["SEQ051"]) ? $model->field["SEQ051"] : ($dataTmp["051"]["REMARK1"] ? $dataTmp["051"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0511\" onclick=\"setPattern()\""
                     , "id=\"SEQ0512\" onclick=\"setPattern()\"");
        $radioArray = knjCreateRadio($objForm, "SEQ051", $model->field["SEQ051"], $extra, $opt051, get_count($opt051));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //平均点 (1:クラス・2:コース・3:学年)
        $opt054 = array(1, 2, 3);
        $model->field["SEQ054"] = ($model->field["SEQ054"]) ? $model->field["SEQ054"] : ($dataTmp["054"]["REMARK1"] ? $dataTmp["054"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0541\"", "id=\"SEQ0542\"", "id=\"SEQ0543\"");
        $radioArray = knjCreateRadio($objForm, "SEQ054", $model->field["SEQ054"], $extra, $opt054, get_count($opt054));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //留学中の授業日数非表示
        makeCheckBox($objForm, $model, $dataTmp, $arg, '057', 'REMARK1', 'SEQ057');
        //LHR欠課時数非表示
        makeCheckBox($objForm, $model, $dataTmp, $arg, '058', 'REMARK1', 'SEQ058');
        //行事欠課時数非表示
        makeCheckBox($objForm, $model, $dataTmp, $arg, '059', 'REMARK1', 'SEQ059');
        //返信欄非表示（Aパターンのみ）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '063', 'REMARK1', 'SEQ063');
        //度数分布表非表示（Bパターンのみ）
        makeCheckBox($objForm, $model, $dataTmp, $arg, '064', 'REMARK1', 'SEQ064');

        //担任項目名ラジオボタン 1:担任 2:チューター
        $opt062 = array(1, 2);
        $model->field["SEQ062"] = ($model->field["SEQ062"]) ? $model->field["SEQ062"] : ($dataTmp["062"]["REMARK1"] ? $dataTmp["062"]["REMARK1"] : "1");
        $extra = array("id=\"SEQ0621\"", "id=\"SEQ0622\"");
        $radioArray = knjCreateRadio($objForm, "SEQ062", $model->field["SEQ062"], $extra, $opt062, get_count($opt062));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        $semester = "";
        for ($i=0; $i < get_count($model->semesterList); $i++) { 
            if ($semester) { $semester .= ","; }
            $semester .= $model->semesterList[$i]["SEMESTER"];
        }
        knjCreateHidden($objForm, "semester", $semester);

        //DB切断
        Query::dbCheckIn($db);
        
        $arg["pattern"] = $model->pattern;

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz235nForm2.html", $arg);
    }
}

//チェックボックス作成
function makeCheckBox($objForm, $model, $dataTmp, &$arg, $fieldSEQ, $remark, $id, $defCheck = false){
    $extra = "";
    if ( $model->field[$id] == "1" ||
        (get_count($dataTmp) > 0 && $dataTmp[$fieldSEQ][$remark] == "1") ||
        (get_count($dataTmp) == 0 && $defCheck) ) {
        $extra = "checked";
    }
    $extra .= " id=\"{$id}\"";
    $arg["data"][$id] = knjCreateCheckBox($objForm, $id, "1", $extra, "");
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
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
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
