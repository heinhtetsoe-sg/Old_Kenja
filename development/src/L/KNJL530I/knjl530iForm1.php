<?php
class knjl530iForm1
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl530iindex.php", "", "main");
        $db           = Query::dbCheckOut();

        $arg["TOP"]["YEAR"] = $model->year ."年度";

        //学科コンボ
        $listExamType = array();
        foreach ($model->examTypeList as $key => $val) {
            $listExamType[] = array(
                "LABEL" => $key.":".$val,
                "VALUE" => $key
            );
        }
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmbList($objForm, $arg, $listExamType, $model->exam_type, "EXAM_TYPE", $extra, 1, "TOP", "");

        //入試区分コンボ
        $query = knjl530iQuery::getTestdivMst($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "TOP", "");

        $extra = " onClick=\"return check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "1", $extra, "");

        //データ
        $result = $db->query(knjl530iQuery::selectQuery($model));
        $arg["data"] = array();
        unset($model->max_examhallcd);
        unset($model->e_receptno);
        $disabled = "disabled";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $extra = "tabindex=\"-1\"";
            $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["EXAMHALLCD"], $extra, "1");

            $disabled = "";
            $aAdditional  = "onclick=\"loadwindow('knjl530iindex.php?cmd=edit&mode=update&examhallcd=".$row["EXAMHALLCD"] ."',";
            $aAdditional .= "event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), ";
            $aAdditional .= "event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(),500,170);\"";
            $row["EXAMHALL_NAME"] = View::alink("#", htmlspecialchars($row["EXAMHALL_NAME"]), $aAdditional);
            $arg["data"][] = $row;
        }

        Query::dbCheckIn($db);

        //削除ボタン作成
        $extra = "$disabled onclick=\"return btn_submit('delete');\"";
        $arg["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);

        //割振り実行ボタン作成
        $extra = "$disabled onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "割振り実行", $extra);

        //会場追加ボタン作成
        $extra = "onclick=\"return btn_submit('halladd');\"";
        $arg["btn_add"] = knjCreateBtn($objForm, "btn_add", "会場追加", $extra);

        //終了ボタン作成
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjl530iForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $argName = "", $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') {
            $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$argName][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//makeCmbList
function makeCmbList(&$objForm, &$arg, $orgOpt, &$value, $name, $extra, $size, $argName = "", $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    foreach ($orgOpt as $row) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') {
            $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$argName][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
