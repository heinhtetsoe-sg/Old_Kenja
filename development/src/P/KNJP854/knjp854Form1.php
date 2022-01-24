<?php

require_once('for_php7.php');

class knjp854form1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp854index.php", "", "edit");

        $db = Query::dbCheckOut();

        //学籍番号・氏名
        $arg["TOP"]["DISP_SCHREGNO"] = $model->schregno;
        $arg["TOP"]["DISP_NAME"]     = $db->getOne(knjp854Query::getSchregNameQuery($model));

        //納期限月
        $extra = " onchange=\"return btn_submit('chgMonth')\" ";
        $query = knjp854Query::selectMainQuery($model, "MONTH");
        $arg["TOP"]["PLAN_YEAR_MONTH"] = makeCmb($objForm, $arg, $db, $query, $model->field["PLAN_YEAR_MONTH"], "PLAN_YEAR_MONTH", $extra, "1");

        //伝票番号
        $model->field["SLIP_NO"] = ($model->cmd == "chgMonth") ? "" : $model->field["SLIP_NO"];
        $extra = " onchange=\"return btn_submit('chgSlip')\" ";
        $query = knjp854Query::selectMainQuery($model, "SLIP");
        $arg["TOP"]["SLIP_NO"] = makeCmb($objForm, $arg, $db, $query, $model->field["SLIP_NO"], "SLIP_NO", $extra, "1", "BLANK");

        //入金日
        $model->field["PLAN_PAID_MONEY_DATE"] = $model->field["PLAN_PAID_MONEY_DATE"] ? $model->field["PLAN_PAID_MONEY_DATE"] : CTRL_DATE;
        $arg["TOP"]["PLAN_PAID_MONEY_DATE"] = View::popUpCalendar($objForm, "PLAN_PAID_MONEY_DATE", str_replace("-", "/", $model->field["PLAN_PAID_MONEY_DATE"]));

        //入金方法
        $query = knjp854Query::getNameMst($model, "P004");
        $extra = "";
        $arg["TOP"]["PLAN_PAID_MONEY_DIV"] = makeCmb($objForm, $arg, $db, $query, $model->field["PLAN_PAID_MONEY_DIV"], "PLAN_PAID_MONEY_DIV", $extra, "1", "BLANK");

        //自動消込チェック
        $extra = " id=\"AUTO_KESIKOMI\" onclick=\"checkKesikomi(this);\" ";
        $arg["TOP"]["AUTO_KESIKOMI"] = knjCreateCheckBox($objForm, "AUTO_KESIKOMI", "1", $checked.$extra);

        //入金額合計
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);autoInputPaidMoney(this);\" disabled ";
        $arg["TOP"]["AUTO_KESIKOMI_MONEY"] = knjCreateTextBox($objForm, "", "AUTO_KESIKOMI_MONEY", 7, 7, $extra.$disabled);

        $dispRows   = array(); //明細に表示される項目
        $noDispRows = array(); //明細に表示されないが入金額の更新は行う項目
        $query = knjp854Query::selectMainQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["REDUCED_PLAN_MONEY"] > 0) { //徴収予定額がある項目のみ画面に表示
                $dispRows[] = $row;
            } else { //更新用に保持しておく
                $noDispRows[] = $row;
            }
        }

        //明細一覧
        $rowCnt = 0;
        $totalPlanMoney = 0;
        $totalPaidMoney = 0;

        foreach ($dispRows as $row) {
            $rowNo = $rowCnt + 1;
            $row["ROWNO"] = $rowNo;

            $totalPlanMoney += $row["REDUCED_PLAN_MONEY"];

            //徴収予定額
            $dispPlanMoney = number_format($row["REDUCED_PLAN_MONEY"]);
            $row["DISP_PLAN_MONEY"] = "<div id=\"DISP_PLAN_MONEY_{$rowNo}\">{$dispPlanMoney}</div>";

            //入金額
            $paidMoney = intval($model->field2[$rowNo]["PAID_MONEY"]);
            $extra  = " id=\"PAID_MONEY_{$rowNo}\" class=\"paid_money\" ";
            $extra .= "style=\"text-align:right\" onblur=\"calcMoney(this, {$rowNo})\"";
            $row["PAID_MONEY"]  =  knjCreateTextBox($objForm, $paidMoney, "PAID_MONEY_".$rowNo, 7, 7, $extra);
            $totalPaidMoney += $paidMoney;

            //残高
            $row["BALANCE"] = "<div id=\"BALANCE_{$rowNo}\">{$dispPlanMoney}</div>"; //初期値として徴収予定額を出す

            //hidden
            $rowKey = $row["COLLECT_L_CD"]."-".$row["COLLECT_M_CD"];
            knjCreateHidden($objForm, "ROW_KEY_{$rowNo}", $rowKey);
            knjCreateHidden($objForm, "DISP_PLAN_MONEY_{$rowNo}", $row["REDUCED_PLAN_MONEY"]);
            knjCreateHidden($objForm, "REDUCE_MONEY_{$rowNo}", $row["REDUCE_MONEY"]);

            $arg["data"][] = $row;
            $rowCnt++;
        }

        //合計行表示
        if ($rowCnt > 0) {
            $arg["totalrow"] = "1";
            $arg["DISP_PLAN_MONEY_TOTAL"] = "<div id=\"DISP_PLAN_MONEY_TOTAL\">".number_format($totalPlanMoney)."</div>";
            $arg["PAID_MONEY_TOTAL"]      = "<div id=\"PAID_MONEY_TOTAL\">".number_format($totalPaidMoney)."</div>";
            $arg["BALANCE_TOTAL"]         = "<div id=\"BALANCE_TOTAL\">".number_format($totalPlanMoney)."</div>";
        }

        $noDispRowCnt = 0;
        foreach ($noDispRows as $row) {
            $rowNo = $noDispRowCnt + 1;

            //hidden
            $rowKey = $row["COLLECT_L_CD"]."-".$row["COLLECT_M_CD"];
            knjCreateHidden($objForm, "NO_DISP_ROW_KEY_{$rowNo}", $rowKey);
            knjCreateHidden($objForm, "NO_DISP_PLAN_MONEY_{$rowNo}", $row["PLAN_MONEY"]);
            $noDispRowCnt++;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $rowCnt);

        //Hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "GRPCD_FROM", $model->grpCdFromNo);
        knjCreateHidden($objForm, "GRPCD_TO", $model->grpCdToNo);
        knjCreateHidden($objForm, "NEXT_GRPCD", $model->nextGrpCd);
        knjCreateHidden($objForm, "ROWCNT", $rowCnt);
        knjCreateHidden($objForm, "NO_DISP_ROWCNT", $noDispRowCnt);
        knjCreateHidden($objForm, "DISP_PLAN_MONEY_TOTAL");
        knjCreateHidden($objForm, "PAID_MONEY_TOTAL");
        knjCreateHidden($objForm, "BALANCE_TOTAL");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp854Form1.html", $arg);
    }
}

//コンボ作成
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
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $rowCnt)
{
    $disabled = ($rowCnt == 0) ? " disabled " : "";
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //クリア
    $extra = "onclick=\"return Btn_reset('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$disabled);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
