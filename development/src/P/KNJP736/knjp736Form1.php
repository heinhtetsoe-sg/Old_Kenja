<?php

require_once('for_php7.php');

class knjp736Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp736index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度コンボ
        $query = knjp736Query::getYear($model);
        $extra = "onChange=\"return btn_submit('list');\"";
        $model->mst_field["YEAR"] = $model->mst_field["YEAR"] ? $model->mst_field["YEAR"] : CTRL_YEAR;
        makeCombo($objForm, $arg, $db, $query, $model->mst_field["YEAR"], "YEAR", $extra, 1, "");

        //生徒データ表示
        $studentInfo = makeStudentInfo($arg, $db, $model);

        //extra
        $extraRight = "STYLE=\"text-align: right\"";

        //伝票コンボ
        $query = knjp736Query::getSlipNo($model);
        $extra = "onChange=\"return btn_submit('list');\"";
        makeCombo($objForm, $arg, $db, $query, $model->mst_field["SLIP_NO"], "SLIP_NO", $extra, 1, "BLANK");

        if ($model->mst_field["SLIP_NO"]) {
            $query = knjp736Query::getSlipInfo($model);
            $slipInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $model->mst_field["PAY_DIV"] = $slipInfo["PAY_DIV"];
            $model->mst_field["SLIP_DATE"] = $slipInfo["SLIP_DATE"];
            $model->mst_field["CANCEL_DATE"] = $slipInfo["CANCEL_DATE"];
        } else {
            $model->mst_field["PAY_DIV"] = "";
            $model->mst_field["SLIP_DATE"] = "";
            $model->mst_field["CANCEL_DATE"] = "";
        }

        //支払方法ラジオ
        $opt = array(1, 2);
        $model->mst_field["PAY_DIV"] = ($model->mst_field["PAY_DIV"] == "") ? "1" : $model->mst_field["PAY_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PAY_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "PAY_DIV", $model->mst_field["PAY_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //伝票日付
        $model->mst_field["SLIP_DATE"] =  $model->mst_field["SLIP_DATE"] ?  $model->mst_field["SLIP_DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["SLIP_DATE"] = str_replace("-", "/", $model->mst_field["SLIP_DATE"]);

        //キャンセル日付
        $extra = !$model->mst_field["SLIP_NO"] ? " disabled " : $extraDis;
        $arg["data"]["CANCEL_DATE"] = str_replace("-", "/", $model->mst_field["CANCEL_DATE"]);

        //ALLチェック
        $extra  = "onClick=\"return check_all(this);\"";
        $extra .= " id=\"CHECKALL\"";
        $arg["data"]["CHECKALL"] = knjcreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //明細
        if ($model->schregno != "") {
            makeClaimDetailData($objForm, $arg, $db, $model, $studentInfo);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db, $studentInfo);

        //hidden
        makeHidden($objForm, $arg, $model);

        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "add") {
            $arg["jscript"] = "seqfocus()";
        }

        if ($model->cmd == "printEdit") {
            $arg["print"] = "newwin('" . SERVLET_URL . "');";
        }

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjp736Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$arg, $db, &$model)
{
    $info = $db->getRow(knjp736Query::getStudentName($model), DB_FETCHMODE_ASSOC);
    if (is_array($info)) {
        foreach ($info as $key => $val) {
            $setRow[$key] = $val;
        }
    }
    $query = knjp736Query::getSlipData($model);
    $claim = $db->getRow($query, DB_FETCHMODE_ASSOC);
    if (is_array($claim)) {
        foreach ($claim as $key => $val) {
            if (in_array($key, array("MINOU_MONEY"))) {
                $setRow["MINOU_COLOR"] = $val >= 0 ? "black" : "red";
            }
            $val = in_array($key, array("TMONEY",
                                        "TPAID_MONEY")) ? number_format($val) : $val;
            if (in_array($key, array("SLIP_DATE", "CANCEL_DATE"))) {
                $val = str_replace("-", "/", $val);
            }
            $setRow[$key] = $val;
            $info[$key] = $val;
            if ($key == "CLAIM_NO") {
                $model->claimNo = $val;
            }
        }
    }

    //総請求データ
    $query = knjp736Query::getSlipDataALL($model);
    $allMoney = $db->getRow($query, DB_FETCHMODE_ASSOC);
    $setRow["ALL_MONEY"] = number_format($allMoney["ALL_MONEY"]);
    $setRow["ALL_PAID_MONEY"] = number_format($allMoney["ALL_PAID_MONEY"]);

    $setRow["TOTAL_MONEY_TITLE"] = $model->claimNo > 0 ? "(済)" : "(未)";
    $setRow["TOTAL_MONEY_COLOR"] = $model->claimNo > 0 ? "#33ffff" : "red";

    $arg["data"] = $setRow;

    return $info;
}

//明細
function makeClaimDetailData(&$objForm, &$arg, $db, &$model, $studentInfo)
{
    //商品の単価をHiddenに持つ
    $query = knjp736Query::getCollectM($model);
    $result = $db->query($query);
    $firstCollectMMoney = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!$firstCollectMMoney) {
            $firstCollectMMoney = $row["COLLECT_M_MONEY"];
        }
        //hidden
        knjCreateHidden($objForm, "PRICE_{$row["VALUE"]}", $row["COLLECT_M_MONEY"]);
    }

    //extraセット
    $extraInt = "onblur=\"this.value=toInteger(this.value)\";";
    $extraRight = "STYLE=\"text-align: right\"";
    $extraDis  = " disabled ";

    $query = knjp736Query::getMeisaiData($model);

    $result = $db->query($query);
    $hiddenCnt = 0;
    $setSeq = 1;
    $model->updData = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!$row["COLLECT_M_CD"]) {
            continue;
        }
        $model->updData[] = $row;

        //商品コンボ
        $query = knjp736Query::getCollectM($model);
        $model->commodity = $row["COLLECT_L_CD"].":".$row["COLLECT_M_CD"];
        $extra = "";
        $row["COLLECT_LM_CD"] = makeCombo2($objForm, $arg, $db, $query, $model->commodity, "COLLECT_LM_CD_{$row["SEQ"]}", $extraDis.$extra, 1);

        $row["COLLECT_CNT"] = $row["COLLECT_CNT"] ? $row["COLLECT_CNT"] : 1;

        $totalMoney = $row["MONEY_DUE"] * $row["COLLECT_CNT"];

        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $row["MONEY_DUE"] = knjcreateTextBox($objForm, $row["MONEY_DUE"], "MONEY_DUE_{$row["SEQ"]}", 7, 7, $extraDis.$extraRight.$extra);

        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $row["COLLECT_CNT"] = knjcreateTextBox($objForm, $row["COLLECT_CNT"], "COLLECT_CNT_{$row["SEQ"]}", 2, 2, $extraDis.$extraRight.$extra);

        $row["TMONEY"] = number_format($totalMoney);

        $row["PAID_MONEY_DATE"] = View::popUpCalendarAlp($objForm, "PAID_MONEY_DATE_{$row["SEQ"]}", str_replace("-", "/", $row["PAID_MONEY_DATE"]), "");

        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $row["PAID_MONEY"] = knjcreateTextBox($objForm, $row["PAID_MONEY"], "PAID_MONEY_{$row["SEQ"]}", 7, 7, $extraRight.$extra);

        $row["SEQ"] = $setSeq;
        $arg["data2"][] = $row;
        $setSeq++;
    }

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db, $studentInfo)
{
    $extraDis = $model->claimNo > 0 ? "" : " disabled ";

    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extraDis.$extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $endname = "終 了";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, &$arg, $model)
{
    knjcreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJMP717");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->mst_field["YEAR"]);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    } else if ($blank == "NEW") {
        $opt[] = array ("label" => "新規",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        if ($name == "JUGYOU") {
            $row["LABEL"] = $row["LABEL"]."({$row["COLLECT_M_MONEY"]})";
        }
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg[$name] = knjcreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function makeCombo2(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    return knjcreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
