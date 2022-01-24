<?php

require_once('for_php7.php');
class knjp740Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        //校種コンボ
        $query = knjp740Query::getSchkind($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "");

        //出力（1:引落し, 2:返金）
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\" onClick=\"btn_submit('knjp740')\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //共通手数料取得
        $query = knjp740Query::getBankTransferFee99($model);
        $model->tesuRyo = $db->getOne($query);

        //月コンボ
        $opt = array();
        foreach ($model->monthArray as $month) {
            $opt[] = array('label' => $month."月", 'value' => $month);
        }
        $extra = " onchange=\"btn_submit('knjp740')\" ";
        list($ctrlyear, $setCtrlMonth, $ctrlday) = explode("-", CTRL_DATE);
        $model->field["MONTH"] = ($model->field["MONTH"]) ? $model->field["MONTH"]: $setCtrlMonth;
        $arg["data"]["MONTH"] = knjCreateCombo($objForm, "MONTH", $model->field["MONTH"], $opt, $extra, 1);

        //教員給食費checkbox
        $checked = " checked ";
        $extra = "id=\"STAFF_LUNCH\"";
        $arg["data"]["STAFF_LUNCH"] = knjCreateCheckBox($objForm, "STAFF_LUNCH", "1", $extra.$checked);

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
            $query = knjp740Query::getCsvQuery($model, $model->setLimitDay, '', 'group');
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->field["MONTH_DATE"], "MONTH_DATE", $extra, 1, "");
        }

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = " checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //ファイルからの取り込み
        $extra = " onChange=\"setCheckBox(this)\"";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

        //送付日
        $value = str_replace("-", "/", ($model->field["PRINT_DATE"] != "") ? $model->field["PRINT_DATE"]: CTRL_DATE);
        $arg["data"]["PRINT_DATE"] = View::popUpCalendarAlp($objForm, "PRINT_DATE", $value, $disabled, "");

        //ラジオ（1:自動払込書 2:請求データ明細リスト）
        $opt = array(1, 2);
        $model->field["PRINT_TYPE"] = ($model->field["PRINT_TYPE"] == "") ? "1" : $model->field["PRINT_TYPE"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PRINT_TYPE{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "PRINT_TYPE", $model->field["PRINT_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /************/
        /** ボタン **/
        /************/
        //ＣＳＶ出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //印刷ボタン
        $extra = "onClick=\"btn_submit('print');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        $arg["TOP"]["useOutputOverMonth"]  = knjCreateHidden($objForm, "useOutputOverMonth", $model->Properties["useOutputOverMonth"]);
        $arg["TOP"]["printKouzaKigou"]  = knjCreateHidden($objForm, "printKouzaKigou", $model->printKouzaKigou);
        $arg["TOP"]["printKyokuBan"]    = knjCreateHidden($objForm, "printKyokuBan", $model->printKyokuBan);
        $arg["TOP"]["printLimitDate"]   = knjCreateHidden($objForm, "printLimitDate", $model->printLimitDate);
        $arg["TOP"]["printLimitDate2"]  = knjCreateHidden($objForm, "printLimitDate2", $model->printLimitDate2); //引き落とし用
        $arg["TOP"]["printSaiHuri"]     = knjCreateHidden($objForm, "printSaiHuri", $model->printSaiHuri);
        $arg["TOP"]["printTotalCnt"]    = knjCreateHidden($objForm, "printTotalCnt", $model->printTotalCnt);
        $arg["TOP"]["printTotalMoney"]  = knjCreateHidden($objForm, "printTotalMoney", $model->printTotalMoney);
        $arg["TOP"]["printSyubetsu"]    = knjCreateHidden($objForm, "printSyubetsu", $model->printSyubetsu);
        $arg["TOP"]["printJigyounushi"] = knjCreateHidden($objForm, "printJigyounushi", $model->printJigyounushi);
        $arg["TOP"]["printUkeireJcCd"]  = knjCreateHidden($objForm, "printUkeireJcCd", $model->printUkeireJcCd);
        $arg["TOP"]["setLimitDay"]      = knjCreateHidden($objForm, "setLimitDay", $model->setLimitDay);
        $arg["TOP"]["toriKyouName"]     = knjCreateHidden($objForm, "toriKyouName", $model->toriKyouName);
        $arg["TOP"]["tesuRyo"]          = knjCreateHidden($objForm, "tesuRyo", $model->tesuRyo);
        $arg["TOP"]["useBenefit"]       = knjCreateHidden($objForm, "useBenefit", $model->Properties["useBenefit"]);

        $arg["TOP"]["CTRL_YEAR"]     = knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        $arg["TOP"]["CTRL_SEMESTER"] = knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        $arg["TOP"]["CTRL_DATE"]     = knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        $arg["TOP"]["DBNAME"]        = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        $arg["TOP"]["PRGID"]         = knjCreateHidden($objForm, "PRGID", "KNJP740");
        $arg["TOP"]["SCHOOLCD"]      = knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));

        //DB切断
        Query::dbCheckIn($db);
        if (!isset($model->warning) && $model->cmd == 'print') {
            $model->cmd = 'knjp740';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "', '{$model->printKouzaKigou}', '{$model->printKyokuBan}',
                                      '{$model->printLimitDate}', '{$model->printSaiHuri}', '{$model->printTotalCnt}',
                                      '{$model->printTotalMoney}', '{$model->printSyubetsu}', '{$model->printJigyounushi}',
                                      '{$model->setLimitDay}', '{$model->toriKyouName}')";
        }
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjp740Form1", "POST", "knjp740index.php", "", "knjp740Form1");
        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp740Form1.html", $arg); 
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($name == "SCHOOL_KIND") {
        $opt[] = array("label" => "-- 全て --", "value" => "99");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == 'MONTH_DATE') {
            $row["LABEL"] = str_replace("-", "/", $row["LABEL"]);
        }
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
