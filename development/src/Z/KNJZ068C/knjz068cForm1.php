<?php

require_once('for_php7.php');

class knjz068cForm1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz068cindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $query = knjz068cQuery::getYear();
        $extra = "onChange = \"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["IBYEAR"], "IBYEAR", $extra, 1, "BLANK");

        /* 学年 */
        $query = knjz068cQuery::getGrade($model);
        $extra = "onChange = \"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["IBGRADE"], "IBGRADE", $extra, 1, "BLANK");

        /* IBコース */
        $query = knjz068cQuery::getIbCourse();
        $extra = "onChange = \"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["IBPRG_COURSE"], "IBPRG_COURSE", $extra, 1, "");

        /* IB科目 */
        $query = knjz068cQuery::getIbSubclass($model);
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
        knjCreateHidden($objForm, "changeVal", $model->changeVal);
        knjCreateHidden($objForm, "SELECT_SCHREGNO");
        knjCreateHidden($objForm, "PRAM_PERI", $model->param["periodcd"]);

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjz068cForm1.html", $arg);
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
    $query = knjz068cQuery::getIbUnit($model);
    $result = $db->query($query);

    $titleYoko = array();
    $titleYokoCnt = 0;
    $titleYokoBefSeq = "";
    $viewArray = array("①", "②", "③", "④", "⑤");
    $viewCnt = 0;

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($titleYokoBefSeq != "" && $titleYokoBefSeq != $row["IBSEQ"]) {
            $titleYokoCnt++;
            $viewCnt = 0;
        }
        $titleYokoBefSeq = $row["IBSEQ"];
        $titleYoko[$titleYokoCnt]["IBSEQ"] = $row["IBSEQ"];
        $titleYoko[$titleYokoCnt]["SEMESTER"] = $row["SEMESTERNAME"];
        $titleYoko[$titleYokoCnt]["SUBCLASS"] = $row["SUBCLASSNAME"];
        $titleYoko[$titleYokoCnt]["VIEWCD"][] = $row["VIEWCD"];
        $titleYoko[$titleYokoCnt]["VIEW"][] = $viewArray[$viewCnt];
        $titleYoko[$titleYokoCnt]["VIEWNAME"][] = $row["VIEWNAME"];
        $viewCnt++;
    }
    $result->free();

    $setTotalWidth = 0;
    $komaHaba = 60;
    $trStData = "<tr class=\"no_search\" align=\"center\" nowrap>";
    $trEdData = "</tr>";
    $setSeme = "";
    $setsubclass = "";
    $setHyouka = "";
    $setView = "";
    $unitCnt = 1;
    foreach ($titleYoko as $key => $val) {
        $setCol = get_count($val["VIEW"]);
        $setWith = $komaHaba * $setCol;
        $setSeme .= "<td width=\"{$setWith}\" height=\"20\" colspan=\"{$setCol}\">{$val["SEMESTER"]}</td>";
        $setsubclass .= "<td width=\"{$setWith}\" height=\"20\" colspan=\"{$setCol}\">{$val["SUBCLASS"]}</td>";
        $setUnit = $model->field["IBPRG_COURSE"] == "M" ? "Unit".$val["IBSEQ"] : "Task".$val["IBSEQ"];
        $setHyouka .= "<td width=\"{$setWith}\" height=\"20\" colspan=\"{$setCol}\">{$setUnit}</td>";
        foreach ($val["VIEW"] as $viewKey => $viewVal) {
            //チップヘルプあり
            $setView .= "<td width=\"{$komaHaba}\" height=\"20\" onMouseOver=\"ViewcdMousein(event, '{$val["VIEWNAME"][$viewKey]}')\" onMouseOut=\"ViewcdMouseout()\">{$viewVal}</td>";
        }
        $setTotalWidth += $setWith;
        $unitCnt++;
    }
    $arg["TITLE_TOTALWIDTH"] = $setTotalWidth;
    $arg["TITLE"]["SEMESTER"] = $trStData.$setSeme.$trEdData;
    $arg["TITLE"]["SUBCLASS"] = $trStData.$setsubclass.$trEdData;
    $arg["TITLE"]["HYOUKA"] = $trStData.$setHyouka.$trEdData;
    $arg["TITLE"]["VIEW"] = $trStData.$setView.$trEdData;

    return $titleYoko;
}

//タイトル(縦)
function setTitleTate(&$objForm, &$arg, $db, &$model) {
    $query = knjz068cQuery::getIbViewName($model);
    $result = $db->query($query);

    $titleTate = array();
    $tateCnt = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["HYOUKA"][] = $row["IBEVAL_MARK"];
        $titleTate[$tateCnt]["IBEVAL_MARK"] = $row["IBEVAL_MARK"];
        $titleTate[$tateCnt]["IBEVAL_DIV1"] = $row["IBEVAL_DIV1"];
        $titleTate[$tateCnt]["IBEVAL_DIV2"] = $row["IBEVAL_DIV2"];
        $tateCnt++;
    }
    $result->free();

    return $titleTate;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, $titleYoko, $titleTate) {

    $komaHaba = 60;
    $setViewData = array();
    $model->checkBoxName = array();
    foreach ($titleTate as $tkey => $tVal) {
        $colorFlg = false;
        foreach ($titleYoko as $yKey => $yVal) {
            $setColor = $colorFlg ? "bgcolor=\"paleturquoise\"" : "bgcolor=\"#FFFFFF\"";
            $colorFlg = $colorFlg ? false : true;
            foreach ($yVal["VIEWCD"] as $key => $val) {
                //観点コードが取得できない場合は表示しない
                if ($val != "") {
                    //checkbox
                    $setVal = $tVal["IBEVAL_DIV1"]."_".$tVal["IBEVAL_DIV2"]."_".$tVal["IBEVAL_MARK"]."_".$yVal["IBSEQ"]."_".$val;
                    $query = knjz068cQuery::getUnitPlanCnt($model, $tVal["IBEVAL_DIV1"], $tVal["IBEVAL_DIV2"], $tVal["IBEVAL_MARK"], $yVal["IBSEQ"], $val);
                    $checkCnt = $db->getOne($query);
                    $extra = $checkCnt > 0 ? "checked" : "";
                    $setCheck = knjCreateCheckBox($objForm, "VIEW_".$setVal, $setVal, $extra);

                    $model->checkBoxName[] = "VIEW_".$setVal;
                    $setViewData[$tkey]["SET_HYOUKA"] .= "<td width=\"{$komaHaba}\" height=\"40\" {$setColor}>{$setCheck}</td>";
                }
            }
        }
    }

    $arg["IFRAME"] = View::setIframeJs();
    $arg["unit_data"] = $setViewData;
    //hidden
    knjCreateHidden($objForm, "HIDDEN_SCHREG", $hiddenSchreg);

}

//連続授業
function getRenzokuPeri($db, $model, $schregNo)
{
    $retPeri = array();
    return $retPeri;
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
