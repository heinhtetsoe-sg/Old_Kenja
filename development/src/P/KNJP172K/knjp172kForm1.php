<?php

require_once('for_php7.php');

class knjp172kForm1
{
    function main(&$model)
    {       
        $temp_cd = "";
        $arg["disable"] = "";
        //権限チェック
        if (AUTHORITY == DEF_UPDATE_RESTRICT){
               $arg["disable"] = "OnNotUse(".CTRL_SEMESTER.");";
        }

        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp172kindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)) {
            $record = knjp172kQuery::getTrainRow($model, $model->schregno);
        } else {
            $record =& $model->field;
        }

        $db = Query::dbCheckOut();

        //学年コンボボックス
        $query = knjp172kQuery::getYear($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1);

        //異動情報
        $infoNo = 0;
        $result = $db->query(knjp172kQuery::getTransferInfo($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $infoNo++;
            $sdate = str_replace("-", "/", $row["SDATE"]);
            if ($row["SORT"] == "2") {
                $edate = str_replace("-", "/", $row["EDATE"]);
                $fukusuu = (1 < $row["CNT"]) ? "：[複]" : "";
                $arg["TRANSFER_INFO".$infoNo] = $row["DIV_NAME"]."：".$sdate."～".$edate.$fukusuu;
            } else {
                $arg["TRANSFER_INFO".$infoNo] = $row["DIV_NAME"]."：".$sdate;
            }
        }

        //学籍基礎マスタより学籍番号と名前を取得
        $Row2  = $db->getRow(knjp172kQuery::getSchregno_name($model, $model->schregno),DB_FETCHMODE_ASSOC);

        $arg["SCHREGNO"]    = $Row2["SCHREGNO"];
        $arg["NAME"]        = $Row2["NAME"];
        $arg["HR_NAMEABBV"] = $Row2["HR_NAMEABBV"]."-".$Row2["ATTENDNO"];
        $arg["PASSNO"]      = $Row2["PASSNO"];

        $titleId = 0;
        $totalData = array();

        //授業料予定
        $query = knjp172kQuery::getSelectMoneyDue($model);
        list($titleId, $totalData) = setData($arg, $db, $model, $query, $titleId, $totalData, false, "");
        //授業料入金
        $query = knjp172kQuery::getSelectMoneyPaid($model);
        list($titleId, $totalData) = setData($arg, $db, $model, $query, $titleId, $totalData, false, "");

        //就学支援金予定
        $query = knjp172kQuery::getSelectCountry($model, "DUE");
        list($titleId, $totalData) = setData($arg, $db, $model, $query, $titleId, $totalData, false, "DUE");
        //就学支援金入金
        $query = knjp172kQuery::getSelectCountry($model, "DUE_PAY");
        list($titleId, $totalData) = setData($arg, $db, $model, $query, $titleId, $totalData, true, "DUE_PAY");

        //就学加算予定
        $query = knjp172kQuery::getSelectCountry($model, "ADD");
        list($titleId, $totalData) = setData($arg, $db, $model, $query, $titleId, $totalData, false, "ADD");
        //就学加算入金
        $query = knjp172kQuery::getSelectCountry($model, "ADD_PAY");
        list($titleId, $totalData) = setData($arg, $db, $model, $query, $titleId, $totalData, true, "ADD_PAY");

        //軽減額
        $query = knjp172kQuery::getReductionDat($model);
        list($titleId, $totalData) = setData($arg, $db, $model, $query, $titleId, $totalData, true, "");

        //合計額
        $setData = $model->getSetData($titleId);
        foreach ($totalData as $key => $val) {
            $setKey = str_replace("TOTAL_", "", $key);
            $setData[$setKey] = is_numeric($val) ? number_format($val) : "";
            $setData["BGCOLOR".str_replace("DATA", "", $setKey)] = "#ffffff";
        }
        $arg["TEST"][] = $setData;

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "schregNo", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJP172K");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp172kForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    if ($name == "YEAR") {
        $value = ($value && in_array($value, $serch)) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function setData(&$arg, $db, $model, $query, $titleId, $totalData, $totalFlg, $div) {
    $setData = $model->getSetData($titleId);
    $setKei = 0;
    $setCancelSoeji = $div == "ADD" ? "ADD_" : str_replace("DUE", "", $div);
    $setLockSoeji = $div == "ADD" || $div == "ADD_PAY" ? "ADD_" : "";
    $notExistColor = array("04" => "#ffffff",
                           "05" => "#ffffff",
                           "06" => "#ffffff",
                           "07" => "#ffffff",
                           "08" => "#ffffff",
                           "09" => "#ffffff",
                           "10" => "#ffffff",
                           "11" => "#ffffff",
                           "12" => "#ffffff",
                           "01" => "#ffffff",
                           "02" => "#ffffff",
                           "03" => "#ffffff");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["SET_MONTH"] = sprintf("%02d", $row["SET_MONTH"]);
        $cancelFlg = $row[$setCancelSoeji."PLAN_CANCEL_FLG"] == "1" ? true : false;
        $notExistColor[$row["SET_MONTH"]] = "";
        $setData["FUKEN"] = $row["FUKEN"];
        $setData["REMARK"] = $row["REDUC_REMARK"];
        if (!$cancelFlg) {
            $setMoney1 = "";
            $setMoney2 = "";
            if ($row["FUKEN_CD"] == "27") { //27:大阪府
                $setMoney1 = is_numeric($row["SET_MONEY1"]) ? "(" .number_format($row["SET_MONEY1"]) .")<BR>" : "(&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;)<BR>";
                $setMoney2 = is_numeric($row["SET_MONEY2"]) ? "(" .number_format($row["SET_MONEY2"]) .")<BR>" : "(&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;)<BR>";
            }
            $setData["DATA".$row["SET_MONTH"]] = is_numeric($row["SET_MONEY"]) ? $setMoney1 .$setMoney2 .number_format($row["SET_MONEY"]) : "";
        } else {
            $setData["DATA".$row["SET_MONTH"]] = "　";
        }

        //色
        $setColor = "#ffffff";
        if ($div == "DUE_PAY" || $div == "ADD_PAY") {
            if ($row[$setLockSoeji."PLAN_LOCK_FLG"] == "1") {
                $setColor = "#ff0099";
            } else if ($row["PAID_YEARMONTH"]) {
                $setColor = "#ffff00";
            } else if ($row["SET_MONEY"]) {
                $setColor = "#3399ff";
            }
        }
        $setData["BGCOLOR".$row["SET_MONTH"]] = $setColor;

        $setKei += !$cancelFlg && $row["SET_MONEY"] ? $row["SET_MONEY"] : 0;
        $totalData[$row["SET_MONTH"]] += !$cancelFlg && $row["SET_MONEY"] ? $row["SET_MONEY"] : 0;
        if ($totalFlg) {
            $totalData["TOTAL_DATA".$row["SET_MONTH"]] += !$cancelFlg && $row["SET_MONEY"] ? $row["SET_MONEY"] : 0;
        }
    }
    $result->free();
    $setData["KEI"] = is_numeric($setKei) ? number_format($setKei) : "";
    if ($totalFlg) {
        $totalData["TOTAL_KEI"] += $setKei ? $setKei : 0;
    }
    foreach ($notExistColor as $key => $val) {
        if ($val) {
            $setData["BGCOLOR".$key] = $val;
        }
    }
    $arg["TEST"][] = $setData;
    $titleId++;

    return array($titleId, $totalData);
}
?>
