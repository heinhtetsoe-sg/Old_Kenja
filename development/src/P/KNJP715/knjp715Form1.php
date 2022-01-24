<?php

require_once('for_php7.php');

class knjp715Form1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knjp715index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        /********************/
        /**  生徒基本情報  **/
        /********************/

        //処理年度コンボ
        $query = knjp715Query::getYear($model);
        $extra = "onChange=\"return btn_submit('changeYear');\"";
        if (!$model->mst_field["SELECT_YEAR"]) $model->mst_field["SELECT_YEAR"] = ($model->search_div == "1") ? CTRL_YEAR + 1 : CTRL_YEAR;
        makeCombo($objForm, $arg, $db, $query, $model->mst_field["SELECT_YEAR"], "SELECT_YEAR", $extra, 1, "");

        //生徒データ表示
        $studentInfo = makeStudentInfo($objForm, $arg, $db, $model);

        //extra
        $extraDis   = (strlen($studentInfo["PAID_MONEY_DATE"]) || strlen($studentInfo["CANCEL_DATE"])) ? " disabled " : "";
        $extraDis1  = strlen($studentInfo["PAID_MONEY_DATE"]) ? " disabled " : "";  //入金済み
        $extraRight = "STYLE=\"text-align: right\"";

        //伝票コンボ
        $query = knjp715Query::getSlipNo($model);
        $extra = "onChange=\"return btn_submit('changeSlip');\"";
        makeCombo($objForm, $arg, $db, $query, $model->mst_field["SLIP_NO"], "SLIP_NO", $extra, 1, "NEW");

        if (($model->cmd == "list" || $model->cmd == "updEdit" || $model->cmd == "changeSlip") && !isset($model->warning)) {
            if ($model->mst_field["SLIP_NO"]) {
                $query = knjp715Query::getSlipInfo($model);
                $slipInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $model->mst_field["PAY_DIV"]        = $slipInfo["PAY_DIV"];
                $model->mst_field["SLIP_DATE"]      = $slipInfo["SLIP_DATE"];
                $model->mst_field["CANCEL_DATE"]    = $slipInfo["CANCEL_DATE"];
                $model->mst_field["CANCEL_REASON"]  = $slipInfo["CANCEL_REASON"];

                $model->mst_field["DIRECT_DEBIT_DATE"] = "";
                for ($i = 1; $i <= 12; $i++) {
                    $model->mst_field["COLLECT_MONTH_".$i] = "";
                }
            } else {
                $model->mst_field["PAY_DIV"] = "";
                $model->mst_field["SLIP_DATE"] = "";
                $model->mst_field["CANCEL_DATE"] = "";
                $model->mst_field["CANCEL_REASON"] = "";

                $model->updField = array();
            }
        }

        //入金パターン読込
        if ($model->cmd == "readPattern") {
            //入金パターンデータ取得
            $pattern = $db->getRow(knjp715Query::getPatternList($model, $model->mst_field["COLLECT_PATTERN_CD"]), DB_FETCHMODE_ASSOC);

            $model->mst_field["PAY_DIV"]            = $pattern["PAY_DIV"];
            $model->mst_field["DIRECT_DEBIT_DATE"]  = $pattern["DIRECT_DEBIT_DATE"];
            for ($i = 1; $i <= 12; $i++) {
                $model->mst_field["COLLECT_MONTH_".$i] = sprintf("%02d", $pattern["COLLECT_MONTH_".$i]);
            }
        }

        //伝票日付
        $model->mst_field["SLIP_DATE"] =  $model->mst_field["SLIP_DATE"] ?  $model->mst_field["SLIP_DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["SLIP_DATE"] = View::popUpCalendarAlp($objForm, "SLIP_DATE", str_replace("-", "/", $model->mst_field["SLIP_DATE"]), $extraDis);

        //キャンセル日付
        $extra  = !$model->mst_field["SLIP_NO"] ? " disabled " : $extraDis1;
        $extra .= " onChange=\"OptionUse(this)\" ";
        $arg["data"]["CANCEL_DATE"] = View::popUpCalendarAlp($objForm, "CANCEL_DATE", str_replace("-", "/", $model->mst_field["CANCEL_DATE"]), $extra);

        //キャンセル備考
        $extra = (!$model->mst_field["SLIP_NO"] || !$model->mst_field["CANCEL_DATE"]) ? "disabled" : "";
        $arg["data"]["CANCEL_REASON"] = knjCreateTextBox($objForm, $model->mst_field["CANCEL_REASON"], "CANCEL_REASON", 60, 60, $extra);


        /****************/
        /**  入金方法  **/
        /****************/

        //入金パターンコンボ
        $query = knjp715Query::getPatternList($model);
        $extra = $model->mst_field["SLIP_NO"] ? " disabled " : "";
        makeCombo($objForm, $arg, $db, $query, $model->mst_field["COLLECT_PATTERN_CD"], "COLLECT_PATTERN_CD", $extraDis.$extra, 1, "BLANK");

        //入金方法ラジオ 1:口座引落 2:口座引落2 3:振込 4:現金
        $opt = array(1, 2, 3, 4);
        $model->mst_field["PAY_DIV"] = ($model->mst_field["PAY_DIV"] == "") ? "1" : $model->mst_field["PAY_DIV"];
        $extra = array();
        foreach($opt as $key => $val) array_push($extra, " id=\"PAY_DIV{$val}\" ".$extraDis);
        $radioArray = knjCreateRadio($objForm, "PAY_DIV", $model->mst_field["PAY_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //入金月設定
        if ($model->mst_field["SLIP_NO"]) {
            //入金日付表示
            $query = knjp715Query::getLimitDate($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["PAID_LIMIT_DATE"]) {
                    list ($y, $m, $d) = explode("-", $row["PAID_LIMIT_DATE"]);
                    $arg["data"]["COLLECT_MONTH_".intval($row["PLAN_MONTH"],10)] = intval($m,10)."/".$d;
                }
            }
            $result->free();
        } else {
            //引き落とし日
            $extra = "style=\"text-align:right\" onblur=\"checkDate(this);\"";
            $arg["data"]["DIRECT_DEBIT_DATE"] = knjCreateTextBox($objForm, $model->mst_field["DIRECT_DEBIT_DATE"], "DIRECT_DEBIT_DATE", 2, 2, $extra);

            //入金月設定
            for ($i = 4; $i <= 16; $i++) {
                $monthName = ($i == 16) ? "ALL" : (($i > 12) ? ($i - 12) : $i);
                $setName = "COLLECT_MONTH_".$monthName;
                $value = $model->mst_field[$setName];

                $opt   = array();
                $opt[] = array('label' => "", 'value' => "");
                $value_flg = false;
                for ($j = 4; $j <= 15; $j++) {
                    $month = ($j > 12) ? ($j - 12) : $j;
                    $val = sprintf("%02d", $month);

                    $opt[] = array('label' => $month,
                                   'value' => $val);
                    if ($model->mst_field[$setName] == $val) $value_flg = true;
                }
                $value = ($value && $value_flg) ? $value : $opt[0]["value"];
                $extra = ($i == 16) ? "onChange=\"checkedMethod(this)\"" : "";
                $arg["data"][$setName] = knjCreateCombo($objForm, $setName, $value, $opt, $extra, 1);
            }
        }


        /************/
        /**  明細  **/
        /************/

        //入金グループコンボ
        $query = knjp715Query::getGroupList($model);
        $extra = $model->mst_field["SLIP_NO"] ? " disabled " : "";
        makeCombo($objForm, $arg, $db, $query, $model->mst_field["GROUPCD"], "GROUPCD", $extraDis.$extra, 1, "BLANK");

        //ALLチェック
        $extra  = "onClick=\"return check_all(this);\"";
        $extra .= " id=\"CHECKALL\"";
        $arg["data"]["CHECKALL"] = knjcreateCheckBox($objForm, "CHECKALL", "", $extra.$extraDis, "");

        //明細
        if ($model->schregno != "") {
            makeDetailData($objForm, $arg, $db, $model, $studentInfo, $extraDis);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db, $studentInfo);

        //hidden
        makeHidden($objForm, $arg, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjp715Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$objForm, &$arg, $db, &$model) {
    $info = $db->getRow(knjp715Query::getStudentName($model), DB_FETCHMODE_ASSOC);
    if (is_array($info)) {
        foreach ($info as $key => $val) {
            $setRow[$key] = $val;
        }
    }
    $query = knjp715Query::getSlipData($model);
    $SlipData = $db->getRow($query, DB_FETCHMODE_ASSOC);
    if (is_array($SlipData)) {
        foreach ($SlipData as $key => $val) {
            $val = in_array($key, array("TMONEY",
                                        "TPAID_MONEY")) ? number_format($val) : $val;
            if (in_array($key, array("SLIP_DATE", "CANCEL_DATE"))) {
                $val = str_replace("-", "/", $val);
            }
            $setRow[$key] = $val;
            $info[$key] = $val;
        }
    }

    //仮履修登録単位
    $credits = $db->getOne(knjp715Query::getCredits($model));
    $credits = ($credits > 0) ? $credits : 0;
    $setRow["CREDITS"] = $credits;
    $info["CREDITS"] = $credits;
    knjCreateHidden($objForm, "CREDITS", $credits);

    //総計データ
    $query = knjp715Query::getSlipDataALL($model);
    $allMoney = $db->getRow($query, DB_FETCHMODE_ASSOC);
    $setRow["ALL_MONEY"]        = number_format($allMoney["ALL_MONEY"]);
    $setRow["ALL_PAID_MONEY"]   = number_format($allMoney["ALL_PAID_MONEY"]);

    $arg["data"] = $setRow;

    return $info;
}

//明細
function makeDetailData(&$objForm, &$arg, $db, &$model, $studentInfo, $extraDis) {
    //入金項目の単価をHiddenに持つ
    $query = knjp715Query::getCollectM($model);
    $result = $db->query($query);
    $model->collectM = array();
    $is_creditcnt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->collectM[$row["COLLECT_L_CD"].":".$row["COLLECT_M_CD"]] = $row;

        if ($row["IS_CREDITCNT"] == "1") $is_creditcnt[] = $row["COLLECT_L_CD"].":".$row["COLLECT_M_CD"];

        if ($row["TEXTBOOKDIV"]) {
            $query = knjp715Query::getTextBook($model, $row["TEXTBOOKDIV"], "TOTAL");
            $row["COLLECT_M_MONEY"] = $db->getOne($query);
            $model->collectM[$row["COLLECT_L_CD"].":".$row["COLLECT_M_CD"]]["COLLECT_M_MONEY"] = $row["COLLECT_M_MONEY"];
        }
        //hidden
        knjCreateHidden($objForm, "PRICE_{$row["VALUE"]}", $row["COLLECT_M_MONEY"]);
        knjCreateHidden($objForm, "SET_TEXTBOOKDIV_{$row["VALUE"]}", $row["TEXTBOOKDIV"]);
    }
    knjCreateHidden($objForm, "IS_CREDITCNT", implode(",", $is_creditcnt));

    //extraセット
    $extraInt   = "onblur=\"this.value=toInteger(this.value)\";";
    $extraRight = "STYLE=\"text-align: right\"";

    //入金グループ読込
    if ($model->cmd == "readGroup") {
        $query = knjp715Query::groupMstData($model, $model->mst_field["GROUPCD"]);
    } else {
        $query = knjp715Query::getMeisaiData($model);
    }
    $result = $db->query($query);
    $hiddenCnt = 0;
    $setSeq = 0;
    $setData = array();

    if (!in_array($model->cmd, array("addLine", "delLine", "readPattern")) && !isset($model->warning)) {
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$row["COLLECT_M_CD"]) continue;
            $setSeq++;

            //チェックボックス
            $setData["DELCHK"] = knjcreateCheckBox($objForm, "DELCHK_{$setSeq}", "1", $extraDis);

            $setData["SEQ"] = $setSeq;
            //入金項目コンボ
            $query = knjp715Query::getCollectM($model);
            $model->commodity = $row["COLLECT_L_CD"].":".$row["COLLECT_M_CD"];
            $extra = " onChange=\"changeCollectM(this, '{$setSeq}')\" ";
            $setData["COLLECT_LM_CD"] = makeCombo2($objForm, $arg, $db, $query, $model->commodity, "COLLECT_LM_CD_{$setSeq}", $extra.$extraDis, 1, "BLANK");

            if (!strlen($row["COLLECT_CNT"])) {
                $row["COLLECT_CNT"] = ($model->collectM[$model->commodity]["IS_CREDITCNT"] == "1") ? $studentInfo["CREDITS"] : "1";
            }

            //単価テキスト
            if ($row["TEXTBOOKDIV"]) {
                if ($model->cmd == "readGroup") {
                    $query = knjp715Query::getTextBook($model, $row["TEXTBOOKDIV"], "TOTAL");
                    $row["COLLECT_MONEY"] = $db->getOne($query);
                }
                $extraRight = "STYLE=\"text-align: right;background-color:#999999;\" readOnly ";
            } else {
                $extraRight = "STYLE=\"text-align: right\"";
            }
            $extra = $readOnlyTextDiv." onblur=\"this.value=toInteger(this.value); changeTmoney(this, '{$setSeq}')\" ";
            $setData["COLLECT_MONEY"] = knjcreateTextBox($objForm, $row["COLLECT_MONEY"], "COLLECT_MONEY_{$setSeq}", 7, 7, $extraRight.$extra.$extraDis);

            $totalMoney = $row["COLLECT_MONEY"] * $row["COLLECT_CNT"];

            knjCreateHidden($objForm, "HIDDEN_TEXTBOOKDIV_{$setSeq}", $row["TEXTBOOKDIV"]);

            //数量テキスト
            $extra = " onblur=\"this.value=toInteger(this.value); changeTmoney(this, '{$setSeq}')\" ";
            $setData["COLLECT_CNT"] = knjcreateTextBox($objForm, $row["COLLECT_CNT"], "COLLECT_CNT_{$setSeq}", 2, 2, $extraRight.$extra.$extraDis);

            $setData["TMONEY"] = number_format($totalMoney);

            $arg["data2"][] = $setData;
        }

    //1行追加・指定行削除のとき
    } else {

        //MAX行数
        $model->mst_field["maxSeq"] = ($model->mst_field["maxSeq"]) ? $model->mst_field["maxSeq"] : $setSeq;

        $setData = array();
        if ($model->mst_field["maxSeq"] > 0) {
            for ($i = 1; $i <= $model->mst_field["maxSeq"]; $i++) {
                //チェックボックス
                $setData["DELCHK"] = knjcreateCheckBox($objForm, "DELCHK_{$i}", "1", "");

                $setData["SEQ"] = $i;
                //入金項目コンボ
                $query = knjp715Query::getCollectM($model);
                $extra = " onChange=\"changeCollectM(this, '{$i}')\" ";
                $setData["COLLECT_LM_CD"] = makeCombo2($objForm, $arg, $db, $query, $model->updField[$i]["COLLECT_LM_CD"], "COLLECT_LM_CD_{$i}", $extra, 1, "BLANK");

                //単価
                $collect_money = (strlen($model->updField[$i]["COLLECT_MONEY"])) ? $model->updField[$i]["COLLECT_MONEY"] : $model->collectM[$model->updField[$i]["COLLECT_LM_CD"]]["COLLECT_M_MONEY"];

                //回数
                if (strlen($model->updField[$i]["COLLECT_CNT"])) {
                    $collect_cnt = $model->updField[$i]["COLLECT_CNT"];
                } else {
                    $collect_cnt = ($model->collectM[$model->updField[$i]["COLLECT_LM_CD"]]["IS_CREDITCNT"] == "1") ? $studentInfo["CREDITS"] : "1";
                }

                //単価テキスト
                if ($model->collectM[$model->updField[$i]["COLLECT_LM_CD"]]["TEXTBOOKDIV"]) {
                    $extraRight = "STYLE=\"text-align: right;background-color:#999999;\" readOnly ";
                } else {
                    $extraRight = "STYLE=\"text-align: right\"";
                }
                $extra = " onblur=\"this.value=toInteger(this.value); changeTmoney(this, '{$i}')\" ";
                $setData["COLLECT_MONEY"] = knjcreateTextBox($objForm, $collect_money, "COLLECT_MONEY_{$i}", 7, 7, $extraRight.$extra);

                knjCreateHidden($objForm, "HIDDEN_TEXTBOOKDIV_{$i}", $model->collectM[$model->updField[$i]["COLLECT_LM_CD"]]["TEXTBOOKDIV"]);

                //数量テキスト
                $extra = " onblur=\"this.value=toInteger(this.value); changeTmoney(this, '{$i}')\" ";
                $setData["COLLECT_CNT"] = knjcreateTextBox($objForm, $collect_cnt, "COLLECT_CNT_{$i}", 2, 2, $extraRight.$extra);

                //合計
                $totalMoney = $collect_money * $collect_cnt;

                //計画金額合計
                $setData["TMONEY"] = number_format($totalMoney);

                $arg["data2"][] = $setData;
            }
            $setSeq = ($i == 1) ? $i : $i - 1;
        }
    }

    //hidden
    knjCreateHidden($objForm, "maxSeq", $setSeq);

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db, $studentInfo) {

    $extraDis   = (strlen($studentInfo["PAID_MONEY_DATE"]) || strlen($studentInfo["CANCEL_DATE"]) || !strlen($model->schregno)) ? " disabled " : "";
    $extraDis1  = (strlen($studentInfo["PAID_MONEY_DATE"]) || !strlen($model->schregno)) ? " disabled " : "";  //入金済み

    //入金グループ読込
    $extra = "onClick=\"return btn_submit('readGroup');\"";
    if ($model->mst_field["SLIP_NO"]) {
        $extra .= " disabled ";
    }
    $arg["button"]["btn_readG"] = knjCreateBtn($objForm, "btn_readG", "読 込", $extraDis.$extra);

    //入金パターン読込
    $extra = "onClick=\"return btn_submit('readPattern');\"";
    if ($model->mst_field["SLIP_NO"]) {
        $extra .= " disabled ";
    }
    $arg["button"]["btn_readP"] = knjCreateBtn($objForm, "btn_readP", "読 込", $extraDis.$extra);

    //1行追加
    $extra = "onclick=\"return btn_submit('addLine');\"";
    $arg["button"]["btn_addline"] = knjCreateBtn($objForm, "btn_addline", "1行追加", $extraDis.$extra);

    //指定行削除
    $extra = "onclick=\"return btn_submit('delLine');\"";
    $arg["button"]["btn_delline"] = knjCreateBtn($objForm, "btn_delline", "指定行削除", $extraDis.$extra);

    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extraDis1.$extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, &$arg, $model) {
    knjcreateHidden($objForm, "cmd");

    $arg["PARA"]["PRGID"]           = knjCreateHidden($objForm, "PRGID", "KNJMP717");
    $arg["PARA"]["DBNAME"]          = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    $arg["PARA"]["YEAR"]            = knjCreateHidden($objForm, "YEAR", $model->mst_field["SELECT_YEAR"]);
    $arg["PARA"]["SCHREGNO"]        = knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    $arg["PARA"]["CTRL_YEAR"]       = knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    $arg["PARA"]["CTRL_SEMESTER"]   = knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    $arg["PARA"]["CTRL_DATE"]       = knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "", "value" => "");
    } else if ($blank == "NEW") {
        $opt[] = array ("label" => "新規", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg[$name] = knjcreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function makeCombo2(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "", "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    return knjcreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
