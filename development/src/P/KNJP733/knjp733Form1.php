<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjp733Form1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){ $arg["jscript"] = "OnAuthError();";}

        $db = Query::dbCheckOut();
        $objForm = new form;

        //年度学期表示
        $arg["SEMESTERNAME"] = CTRL_YEAR ."年度　" .CTRL_SEMESTERNAME;

        //校種コンボ
        $query = knjp733Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('changeVal');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->schoolKind, $extra, 1, "");

        //radio(1:相殺, 2:還付)
        $opt = array(1, 2);
        $model->field["OFFSET_REFUND_DIV"] = ($model->field["OFFSET_REFUND_DIV"] == "") ? "1" : $model->field["OFFSET_REFUND_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OFFSET_REFUND_DIV{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OFFSET_REFUND_DIV", $model->field["OFFSET_REFUND_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学年
        $query = knjp733Query::getGrade($model);
        $extra = "onchange=\"return btn_submit('changeVal');\"";
        $model->gradeArray = makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "ALL");

        //コース
        $query = knjp733Query::getCourse($model);
        $extra = "";
        $model->cmcArray = makeCmb($objForm, $arg, $db, $query, "CMC", $model->field["CMC"], $extra, 1, "ALL");

        //種別コンボ
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $opt[] = array('label' => "支援金（基本）",     'value' => "1");
        $opt[] = array('label' => "支援金（加算）",     'value' => "2");
        $opt[] = array('label' => "補助金(授業料)",     'value' => "3");
        $opt[] = array('label' => "学校減免(授業料)",   'value' => "4");
        $opt[] = array('label' => "補助金(入学金)",     'value' => "5");
        $opt[] = array('label' => "学校減免(入学金)",   'value' => "6");
        $extra = "";
        $arg["data"]["REDUCTION_KIND"] = knjCreateCombo($objForm, "REDUCTION_KIND", $model->field["REDUCTION_KIND"], $opt, $extra, 1);

        //処理日
        $model->field["PROCESS_DATE"] = ($model->field["PROCESS_DATE"]) ? $model->field["PROCESS_DATE"]: CTRL_DATE;
        $model->field["PROCESS_DATE"] = str_replace("-", "/", $model->field["PROCESS_DATE"]);
        $arg["data"]["PROCESS_DATE"] = View::popUpCalendarAlp($objForm, "PROCESS_DATE", $model->field["PROCESS_DATE"], $disabled, "");

        //combobox
        $opt = array();
        foreach ($model->monthArray as $month) {
            $opt[] = array('label' => $month, 'value' => $month);
        }
        $extra = "";
        $arg["data"]["MONTH_FROM"] = knjCreateCombo($objForm, "MONTH_FROM", $model->field["MONTH_FROM"], $opt, $extra, 1);
        $arg["data"]["MONTH_TO"]   = knjCreateCombo($objForm, "MONTH_TO", $model->field["MONTH_TO"], $opt, $extra, 1);

        //履歴表示
        $arg["titleName"] = CTRL_YEAR."年度　実行履歴";
        makeListRireki($objForm, $arg, $db, $model);

        //button
        //実行
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp733index.php", "", "main");

        //画面のリロード
        if ($model->cmd == "updMain") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp733Form1.html", $arg);
    }
}
//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model) {
    //履歴一覧
    $query = knjp733Query::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["PROCESS_DATE"] = str_replace("-", "/", $row["PROCESS_DATE"]);

        if ($row["OFFSET_REFUND_DIV"] == "1") $row["OFFSET_REFUND_DIV"] = "相殺";
        if ($row["OFFSET_REFUND_DIV"] == "2") $row["OFFSET_REFUND_DIV"] = "還付";

        if ($row["REDUCTION_KIND"] == "1") $row["REDUCTION_KIND"] = "支援金（基本）";
        if ($row["REDUCTION_KIND"] == "2") $row["REDUCTION_KIND"] = "支援金（加算）";
        if ($row["REDUCTION_KIND"] == "3") $row["REDUCTION_KIND"] = "補助金(授業料)";
        if ($row["REDUCTION_KIND"] == "4") $row["REDUCTION_KIND"] = "学校減免(授業料)";
        if ($row["REDUCTION_KIND"] == "5") $row["REDUCTION_KIND"] = "補助金(入学金)";
        if ($row["REDUCTION_KIND"] == "6") $row["REDUCTION_KIND"] = "学校減免(入学金)";

        $row["EXE_TIME"] = str_replace("-", "/", $row["EXE_DATE"])." ".$row["EXE_TIME"];
        $arg['data2'][] = $row;
    }
    $result->free();
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $allVal = $name == 'GRADE' ? "00" : "0:000:0000";
        $opt[] = array("label" => "--全て--", "value" => $allVal);
    }
    $retArray = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
        $retArray[$row["VALUE"]] = $row["LABEL"];
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
    return $retArray;
}
?>
