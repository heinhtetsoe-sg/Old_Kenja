<?php

require_once('for_php7.php');

class knjp740aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        //出力（1:引落し, 2:返金）
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\" onClick=\"btn_submit('knjp740a')\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //校種コンボ
        $query = knjp740aQuery::getSchkind($model);
        $extra = "onchange=\"btn_submit('')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "");

        //月コンボ
        $opt = array();
        foreach ($model->monthArray as $month) {
            $opt[] = array('label' => $month."月", 'value' => $month);
        }
        $extra = " onchange=\"btn_submit('knjp740a')\" ";
        list($ctrlyear, $setCtrlMonth, $ctrlday) = explode("-", CTRL_DATE);
        $model->field["MONTH"] = ($model->field["MONTH"]) ? $model->field["MONTH"]: $setCtrlMonth;
        $arg["data"]["MONTH"] = knjCreateCombo($objForm, "MONTH", $model->field["MONTH"], $opt, $extra, 1);

        //取扱銀行コンボ
        $query = knjp740aQuery::getTargetBank($model);
        $extra = "";
        $model->targetBankCd = array();
        $model->targetBankCd = makeCmb($objForm, $arg, $db, $query, $model->field["BANK_CD"], "BANK_CD", $extra, 1, "");

        //引落し
        if ($model->field["OUTPUT"] == "1") {
            $arg["HIKIOTOSHI"] = 1;

            //振替日変更
            $value = str_replace("-", "/", $model->field["TRANSFER_DATE"]);
            $arg["data"]["TRANSFER_DATE"] = View::popUpCalendarAlp($objForm, "TRANSFER_DATE", $value, $disabled, "");

            //再振替日
            $value = str_replace("-", "/", ($model->field["RETRANSFER_DATE"] != "") ? $model->field["RETRANSFER_DATE"]: CTRL_DATE);
            $arg["data"]["RETRANSFER_DATE"] = View::popUpCalendarAlp($objForm, "RETRANSFER_DATE", $value, $disabled, "");

        //返金
        } else {
            $arg["HENKIN"] = 1;

            //返金日
            $model->field["HENKIN_DATE"] = ($model->field["HENKIN_DATE"] != '') ? $model->field["HENKIN_DATE"]: str_replace('-', '/', CTRL_DATE);
            $arg["data"]["HENKIN_DATE"] = View::popUpCalendarAlp($objForm, "HENKIN_DATE", $model->field["HENKIN_DATE"], $disabled, "");
        }

        $holidayArray = $model->getHolidayArray($db);
        $model->setLimitDay = $model->setLimitDate($db, $holidayArray);
        if ($model->field["OUTPUT"] == "1") {
            $query = knjp740aQuery::getCsvQuery($model, $model->setLimitDay, 'group');
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->field["MONTH_DATE"], "MONTH_DATE", $extra, 1, "");
        }

        /************/
        /** ボタン **/
        /************/
        //ＣＳＶ出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        if ($model->cmd == "csvPop" && !isset($model->warning)) {
            $setMoney = number_format($model->printTotalMoney);
            $arg["csv_result"] = "合計件数：{$model->printTotalCnt}件　合計金額：{$setMoney}円";
            $arg["reload"] = "btn_submit('csv2')";
        }

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjp740aForm1", "POST", "knjp740aindex.php", "", "knjp740aForm1");
        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp740aForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $retArray = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == 'MONTH_DATE') {
            $row["LABEL"] = str_replace("-", "/", $row["LABEL"]);
        }
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
        if ($name == "BANK_CD") {
            $retArray[$row["VALUE"]] = $row["TARGET_BANK_CD"];
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
    return $retArray;
}
