<?php

require_once('for_php7.php');

class knjz068dForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz068dindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $query = knjz068dQuery::getYear();
        $extra = "onChange = \"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["IBYEAR"], "IBYEAR", $extra, 1, "BLANK");

        /* 学年 */
        $query = knjz068dQuery::getGrade($model);
        $extra = "onChange = \"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["IBGRADE"], "IBGRADE", $extra, 1, "BLANK");

        /* IBコース */
        $query = knjz068dQuery::getIbCourse();
        $extra = "onChange = \"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["IBPRG_COURSE"], "IBPRG_COURSE", $extra, 1, "BLANK");

        /* IB科目 */
        $query = knjz068dQuery::getIbSubclass($model);
        $extra = "onChange = \"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["IBSUBCLASSCD"], "IBSUBCLASSCD", $extra, 1, "BLANK");

        //タイトル(横)
        $titleYoko = setTitleYoko($objForm, $arg, $db, $model);

        //タイトル(縦)
        $titleTate = setTitleTate($objForm, $arg, $db, $model);

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model, $titleYoko, $titleTate);

        /* ボタン作成 */
        makeButton($objForm, $arg, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjz068dForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//タイトル(横)
function setTitleYoko(&$objForm, &$arg, $db, &$model) {
    $query = knjz068dQuery::getIbUnit($model);
    $result = $db->query($query);

    $titleYoko = array();
    $titleYokoCnt = 0;
    $titleYokoBefSeq = "";

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($titleYokoBefSeq != "" && $titleYokoBefSeq != $row["IBSEQ"]) {
            $titleYokoCnt++;
        }
        $titleYokoBefSeq = $row["IBSEQ"];
        $titleYoko[$titleYokoCnt]["IBSEQ"] = $row["IBSEQ"];
        $titleYoko[$titleYokoCnt]["SEMESTER"] = $row["SEMESTERNAME"];
        $titleYoko[$titleYokoCnt]["SUBCLASS"] = $row["SUBCLASSNAME"];
        $titleYoko[$titleYokoCnt]["VIEWCD"][] = $row["VIEWCD"];
    }
    $result->free();

    $setTotalWidth = 0;
    $komaHaba = 100;
    $trStData = "<tr class=\"no_search\" align=\"center\" nowrap>";
    $trEdData = "</tr>";
    $setSeme = "";
    $setsubclass = "";
    $setHyouka = "";
    $unitCnt = 1;
    foreach ($titleYoko as $key => $val) {
        $setSeme .= "<td width=\"{$komaHaba}\" height=\"38\">{$val["SEMESTER"]}</td>";
        $setsubclass .= "<td width=\"{$komaHaba}\" height=\"36\">{$val["SUBCLASS"]}</td>";
        $setUnit = $model->field["IBPRG_COURSE"] == "M" ? "Unit".$val["IBSEQ"] : "Task".$val["IBSEQ"];
        $setHyouka .= "<td width=\"{$komaHaba}\" height=\"36\">{$setUnit}</td>";
        $setTotalWidth += $komaHaba;
        $unitCnt++;
    }
    $arg["TITLE_TOTALWIDTH"] = $setTotalWidth;
    $arg["TITLE"]["SEMESTER"] = $trStData.$setSeme.$trEdData;
    $arg["TITLE"]["SUBCLASS"] = $trStData.$setsubclass.$trEdData;
    $arg["TITLE"]["HYOUKA"] = $trStData.$setHyouka.$trEdData;

    return $titleYoko;
}

//タイトル(縦)
function setTitleTate(&$objForm, &$arg, $db, &$model) {
    $titleTate = array();
    $titleTateHenSyuu = array();
    $tateCnt = 0;

    $query = knjz068dQuery::getIbViewName($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $div12 = $row["IBEVAL_DIV1"].$row["IBEVAL_DIV2"];
        $div2Name = $row["Z036_NAME"];
        if ($row["IBEVAL_DIV1"] == "1") {
            $div2Name = $row["Z037_NAME"];
        } else if ($row["IBEVAL_DIV1"] == "2") {
            $div2Name = $row["Z038_NAME"];
        }
        $titleTateHenSyuu[$div12]["MARK"][] = $row["IBEVAL_MARK"];
        $titleTateHenSyuu[$div12]["DIV1_NAME"] = $row["Z035_NAME"];
        $titleTateHenSyuu[$div12]["DIV2_NAME"] = $div2Name;

        $titleTate[$tateCnt]["IBEVAL_MARK"] = $row["IBEVAL_MARK"];
        $titleTate[$tateCnt]["IBEVAL_DIV1"] = $row["IBEVAL_DIV1"];
        $titleTate[$tateCnt]["IBEVAL_DIV2"] = $row["IBEVAL_DIV2"];
        $tateCnt++;
    }
    $result->free();

    $trStData = "<tr bgcolor=\"#FFFFFF\" align=\"center\" nowrap>";
    $trEdData = "</tr>";
    $setCol = 0;
    $setDiv1 = "";
    $setDiv2 = "";
    $setView = "";
    $setHyouka = "";
    foreach ($titleTateHenSyuu as $hKey => $hVal) {
        $setCol = get_count($hVal["MARK"]) + 1;
        $setDiv1 = "<td width=\"150px\" height=\"40\" rowspan=\"{$setCol}\">{$hVal["DIV1_NAME"]}</td>";
        $setDiv2 = "<td width=\"150px\" height=\"40\" rowspan=\"{$setCol}\">{$hVal["DIV2_NAME"]}</td>";
        $setView = "";
        foreach ($hVal["MARK"] as $viewKey => $viewVal) {
            $setView .= $trStData;
            $setView .= "<td width=\"150px\" height=\"40\">{$viewVal}</td>";
            $setView .= $trEdData;
        }
        $setHyouka = $trStData.$setDiv1.$setDiv2.$setView.$trEdData;
        $arg["HYOUKA"]["IBEVAL_MARK"] .= $setHyouka;
    }

    return $titleTate;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, $titleYoko, $titleTate) {

    $komaHaba = 100;
    $setViewData = array();
    $model->checkBoxName = array();
    foreach ($titleTate as $tkey => $tVal) {
        $colorFlg = false;
        foreach ($titleYoko as $yKey => $yVal) {
            $setColor = $colorFlg ? "bgcolor=\"paleturquoise\"" : "bgcolor=\"#FFFFFF\"";
            $colorFlg = $colorFlg ? false : true;
            //checkbox
            $setVal = $tVal["IBEVAL_DIV1"]."_".$tVal["IBEVAL_DIV2"]."_".$tVal["IBEVAL_MARK"]."_".$yVal["IBSEQ"]."_0000";
            $query = knjz068dQuery::getUnitPlanCnt($model, $tVal["IBEVAL_DIV1"], $tVal["IBEVAL_DIV2"], $tVal["IBEVAL_MARK"], $yVal["IBSEQ"]);
            $checkCnt = $db->getOne($query);
            $extra = $checkCnt > 0 ? "checked" : "";
            $setCheck = knjCreateCheckBox($objForm, "VIEW_".$setVal, $setVal, $extra);
            $model->checkBoxName[] = "VIEW_".$setVal;

            $setViewData[$tkey]["SET_HYOUKA"] .= "<td width=\"{$komaHaba}\" height=\"40\" {$setColor}>{$setCheck}</td>";
        }
    }

    $arg["IFRAME"] = View::setIframeJs();
    $arg["unit_data"] = $setViewData;

}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //保存ボタン
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeMethod();\"");
    //ＣＳＶボタン
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return btn_submit('exec');\"");
}
?>
