<?php

require_once('for_php7.php');

class knjmp715Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjmp715index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度コンボ
        $query = knjmp715Query::getYear($model);
        $extra = "onChange=\"return btn_submit('changeYear');\"";
        $model->mst_field["SELECT_YEAR"] = $model->mst_field["SELECT_YEAR"] ? $model->mst_field["SELECT_YEAR"] : CTRL_YEAR;
        makeCombo($objForm, $arg, $db, $query, $model->mst_field["SELECT_YEAR"], "SELECT_YEAR", $extra, 1, "");

        //生徒データ表示
        $studentInfo = makeStudentInfo($arg, $db, $model);

        //extra
        $extraDis  = $model->claimNo > 0 || $studentInfo["PAID_MONEY_DATE"] == "1" ? " disabled " : "";
        $extraRight = "STYLE=\"text-align: right\"";

        //伝票コンボ
        $query = knjmp715Query::getSlipNo($model);
        $extra = "onChange=\"return btn_submit('list');\"";
        makeCombo($objForm, $arg, $db, $query, $model->mst_field["SLIP_NO"], "SLIP_NO", $extra, 1, "NEW");

        if ($model->mst_field["SLIP_NO"]) {
            $query = knjmp715Query::getSlipInfo($model);
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
        $arg["data"]["SLIP_DATE"] = View::popUpCalendar($objForm, "SLIP_DATE", str_replace("-", "/", $model->mst_field["SLIP_DATE"]));

        //キャンセル日付
        $extra = !$model->mst_field["SLIP_NO"] ? " disabled " : $extraDis;
        $arg["data"]["CANCEL_DATE"] = View::popUpCalendarAlp($objForm, "CANCEL_DATE", str_replace("-", "/", $model->mst_field["CANCEL_DATE"]), $extra);

        //諸経費グループコンボ
        $query = knjmp715Query::getGroupList($model);
        $extra = $model->mst_field["SLIP_NO"] ? " disabled " : "";
        makeCombo($objForm, $arg, $db, $query, $model->mst_field["GROPCD"], "GROPCD", $extraDis.$extra, 1, "BLANK");

        //授業料コンボ
        $query = knjmp715Query::getJugyouryou($model);
        $extra = $model->mst_field["SLIP_NO"] ? " disabled " : "";
        makeCombo($objForm, $arg, $db, $query, $model->mst_field["JUGYOU"], "JUGYOU", $extraDis.$extra, 1, "BLANK");

        //ALLチェック
        $extra  = "onClick=\"return check_all(this);\"";
        $extra .= " id=\"CHECKALL\"";
        $arg["data"]["CHECKALL"] = knjcreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //明細
        if ($model->schregno != "") {
            makeClaimDetailData($objForm, $arg, $db, $model, $studentInfo);
        }

        //納入期限
        $extra = !$model->mst_field["SLIP_NO"] ? " disabled " : "";
        $arg["data"]["LIMIT_DATE"] = View::popUpCalendarAlp($objForm, "LIMIT_DATE", "", $extra);

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
        View::toHTML($model, "knjmp715Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$arg, $db, &$model)
{
    $model->claimNo = "";
    $info = $db->getRow(knjmp715Query::getStudentName($model), DB_FETCHMODE_ASSOC);
    if (is_array($info)) {
        foreach ($info as $key => $val) {
            $setRow[$key] = $val;
        }
    }
    $query = knjmp715Query::getSlipData($model);
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

    $query = knjmp715Query::getCredits($model);
    $credits = $db->getOne($query);
    $setRow["CREDITS"] = $credits;
    $info["CREDITS"] = $credits;

    //総請求データ
    $query = knjmp715Query::getSlipDataALL($model);
    $allMoney = $db->getRow($query, DB_FETCHMODE_ASSOC);
    $setRow["ALL_MONEY"] = number_format($allMoney["ALL_MONEY"]);
    $setRow["ALL_PAID_MONEY"] = number_format($allMoney["ALL_PAID_MONEY"]);

    $setRow["TOTAL_MONEY_TITLE"] = $model->claimNo > 0 ? "(済)" : "(未)";
    $setRow["TOTAL_MONEY_COLOR"] = $model->claimNo > 0 ? "#33ffff" : "red";

    $arg["data"] = $setRow;

    return $info;
}

//明細
function makeClaimDetailData(&$objForm, &$arg, $db, $model, $studentInfo)
{
    //商品の単価をHiddenに持つ
    $query = knjmp715Query::getCollectM($model);
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

    $extraDis = $model->claimNo == 0 ? "" : " disabled ";

    if ($model->cmd == "patternEdit") {
        $query = knjmp715Query::groupMstData($model);
    } else {
        $query = knjmp715Query::getMeisaiData($model);
    }

    $result = $db->query($query);
    $hiddenCnt = 0;
    $setSeq = 1;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!$row["COLLECT_M_CD"]) {
            continue;
        }
        //チェックボックス
        if ($model->cmd == "patternEdit") {
            $row["DELCHK"] = "";
        } else {
            $row["DELCHK"] = knjcreateCheckBox($objForm, "DELCHK_{$setSeq}", "1", $extraDis);
        }

        $row["SEQ"] = $setSeq;
        //商品コンボ
        $query = knjmp715Query::getCollectM($model);
        $model->commodity = $row["COLLECT_L_CD"].":".$row["COLLECT_M_CD"];
        $extra = " onChange=\"changeCollectM(this, '{$setSeq}')\" ";
        $row["COLLECT_LM_CD"] = makeCombo2($objForm, $arg, $db, $query, $model->commodity, "COLLECT_LM_CD_{$setSeq}", $extraDis.$extra, 1);

        //授業料
        if ($row["ORDERCD"] == "2" && $model->Properties["collectSlipM_def_cnt"] == "CREDIT") {
            $row["COLLECT_CNT"] = $studentInfo["CREDITS"];
        }
        $row["COLLECT_CNT"] = $row["COLLECT_CNT"] ? $row["COLLECT_CNT"] : 1;

        $totalMoney = $row["MONEY_DUE"] * $row["COLLECT_CNT"];

        $extra = " onblur=\"this.value=toInteger(this.value); changeTmoney(this, '{$setSeq}')\" ";
        $row["MONEY_DUE"] = knjcreateTextBox($objForm, $row["MONEY_DUE"], "MONEY_DUE_{$setSeq}", 7, 7, $extraDis.$extraRight.$extra);

        $extra = " onblur=\"this.value=toInteger(this.value); changeTmoney(this, '{$setSeq}')\" ";
        $row["COLLECT_CNT"] = knjcreateTextBox($objForm, $row["COLLECT_CNT"], "COLLECT_CNT_{$setSeq}", 2, 2, $extraDis.$extraRight.$extra);

        $row["TMONEY"] = number_format($totalMoney);

        $arg["data2"][] = $row;
        $setSeq++;
    }

    if ($model->cmd == "add") {
        $row["SEQ"] = $setSeq;
        //商品コンボ
        $query = knjmp715Query::getCollectM($model);
        $model->commodity = $row["COLLECT_L_CD"].":".$row["COLLECT_M_CD"];
        $extra = " onChange=\"changeCollectM(this, '{$setSeq}')\" ";
        $row["COLLECT_LM_CD"] = makeCombo2($objForm, $arg, $db, $query, $dummyCollectM, "COLLECT_LM_CD_{$setSeq}", $extraDis.$extra, 1);

        $row["COLLECT_CNT"] = $row["COLLECT_CNT"] ? $row["COLLECT_CNT"] : 1;

        $dummyCollectCnt = "1";
        $totalMoney = $firstCollectMMoney * $dummyCollectCnt;

        $extra = " onblur=\"this.value=toInteger(this.value); changeTmoney(this, '{$setSeq}')\" ";
        $row["MONEY_DUE"] = knjcreateTextBox($objForm, $firstCollectMMoney, "MONEY_DUE_{$setSeq}", 7, 7, $extraDis.$extraRight.$extra);

        $extra = " onblur=\"this.value=toInteger(this.value); changeTmoney(this, '{$setSeq}')\" ";
        $row["COLLECT_CNT"] = knjcreateTextBox($objForm, $dummyCollectCnt, "COLLECT_CNT_{$setSeq}", 2, 2, $extraDis.$extraRight.$extra);

        $row["TMONEY"] = number_format($totalMoney);

        $arg["data2"][] = $row;
    } else {
        $setSeq = $setSeq - 1;
    }
    //hidden
    knjCreateHidden($objForm, "maxSeq", $setSeq);

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db, $studentInfo)
{

    $extraDis = $model->claimNo > 0 || $studentInfo["PAID_MONEY_DATE"] != "" ? " disabled " : "";

    //読込
    $extra = "onClick=\"return btn_submit('patternEdit');\"";
    if ($model->mst_field["SLIP_NO"]) {
        $extra .= " disabled ";
    }
    $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読込", $extraDis.$extra);

    //追加
    $extra = "onclick=\"return btn_submit('add');\"";
    if ($model->cmd == "patternEdit") {
        $extra .= " disabled ";
    }
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "1行追加", $extraDis.$extra);

    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extraDis.$extra);

    //削除
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "指定行削除", $extraDis.$extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $endname = "終 了";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //印刷
    $extra = "onClick=\"return btn_submit('print');\"";
    if (!$model->mst_field["SLIP_NO"] || $model->mst_field["CANCEL_DATE"]) {
        $extra .= " disabled ";
    }
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "納付書発行", $extra);
}

//Hidden作成
function makeHidden(&$objForm, &$arg, $model)
{
    knjcreateHidden($objForm, "cmd");
    $arg["PARA"]["PRGID"] = knjCreateHidden($objForm, "PRGID", "KNJMP717");
    $arg["PARA"]["DBNAME"] = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    $arg["PARA"]["YEAR"] = knjCreateHidden($objForm, "YEAR", $model->mst_field["SELECT_YEAR"]);
    $arg["PARA"]["SCHREGNO"] = knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    $arg["PARA"]["CTRL_YEAR"] = knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    $arg["PARA"]["CTRL_SEMESTER"] = knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    $arg["PARA"]["CTRL_DATE"] = knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
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
