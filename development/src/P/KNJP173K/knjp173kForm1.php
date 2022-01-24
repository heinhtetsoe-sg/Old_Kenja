<?php

require_once('for_php7.php');

class knjp173kForm1
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
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp173kindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)) {
//            $record = knjp173kQuery::getTrainRow($model->schregno);
        } else {
//            $record =& $model->field;
        }

        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $Row2  = $db->getRow(knjp173kQuery::getSchregno_name($model->schregno),DB_FETCHMODE_ASSOC);

        $arg["SCHREGNO"]    = $Row2["SCHREGNO"];
        $arg["NAME"]        = $Row2["NAME"];
        $arg["HR_NAMEABBV"] = $Row2["HR_NAMEABBV"]."-".$Row2["ATTENDNO"];
        $arg["PASSNO"]      = $Row2["PASSNO"];

        $titleId = 0;
        $totalDueData = array();
        $totalAddData = array();

        //就学支援金予定
        $query = knjp173kQuery::getSelectCountry($model, "DUE");
        list($titleId, $totalDueData) = setData($arg, $objForm, $db, $model, $query, $titleId, $totalDueData, "DUE");
        //就学支援金入金
        $query = knjp173kQuery::getSelectCountry($model, "DUE_PAY");
        list($titleId, $totalAddData) = setData($arg, $objForm, $db, $model, $query, $titleId, $totalAddData, "DUE_PAY");

        //就学加算予定
        $query = knjp173kQuery::getSelectCountry($model, "ADD");
        list($titleId, $totalDueData) = setData($arg, $objForm, $db, $model, $query, $titleId, $totalDueData, "ADD");
        //就学加算入金
        $query = knjp173kQuery::getSelectCountry($model, "ADD_PAY");
        list($titleId, $totalAddData) = setData($arg, $objForm, $db, $model, $query, $titleId, $totalAddData, "ADD_PAY");

        //予定合計額
        $setData = $model->getSetData($titleId);
        foreach ($totalDueData as $key => $val) {
            $setKey = str_replace("TOTAL_", "", $key);
            //ID名
            $setData[$setKey."_DISP"] = $setKey == "KEI" ? $key."DUE_DISP" : $key."_DISP";
            //色
            $setData["BGCOLOR".$setKey] = "#ffffff";
            $setData[$setKey] = is_numeric($val) ? number_format($val) : "";
        }
        $arg["TEST"][] = $setData;
        $titleId++;

        //入金合計額
        $setData = $model->getSetData($titleId);
        foreach ($totalAddData as $key => $val) {
            $setKey = str_replace("TOTAL_", "", $key);
            //ID名
            $setData[$key."_DISP"] = $key."ADD_DISP";
            //色
            $setData["BGCOLOR".$setKey] = "#ffffff";
            $setData[$setKey] = is_numeric($val) ? number_format($val) : "";
        }
        $arg["TEST"][] = $setData;

        //資格消滅
        $opt = array(1, 2);
        $model->decFlg = ($model->decFlg == "") ? "1" : $model->decFlg;
        $extra = array("id=\"DEC_FLG1\"", "id=\"DEC_FLG2\"");
        $radioArray = knjCreateRadio($objForm, "DEC_FLG", $model->decFlg, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;


        //ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp173kForm1.html", $arg);
    }
}

function setData(&$arg, &$objForm, $db, $model, $query, $titleId, $totalData, $div) {
    $setData = $model->getSetData($titleId);
    $setKei = 0;
    $setCancelSoeji = $div == "ADD" ? "ADD_" : str_replace("DUE", "", $div);
    $setLockSoeji = $div == "ADD" || $div == "ADD_PAY" ? "ADD_" : "";
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["SET_MONTH"] = sprintf("%02d", $row["SET_MONTH"]);
        $cancelFlg = $row[$setCancelSoeji."PLAN_CANCEL_FLG"] == "1" ? true : false;
        $setData["FUKEN"] = $row["FUKEN"];
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

        //ID名
        $setData["DATA".$row["SET_MONTH"]."_DISP"] = "DATA".$row["SET_MONTH"].$div."_DISP";
        //金額
        if (!$cancelFlg) {
            $setData["DATA".$row["SET_MONTH"]] = is_numeric($row["SET_MONEY"]) ? number_format($row["SET_MONEY"]) : "　";
        } else {
            $setData["DATA".$row["SET_MONTH"]] = "　";
        }

        //金額保持のTEXT
        $textName = "DATA".$row["SET_MONTH"].$div."_OBJ";
        $extra = "id='{$textName}{$div}' style=\"display:none\"";
        $setData["DATA".$row["SET_MONTH"]."_OBJ"] = knjCreateTextBox($objForm, $row["SET_MONEY"], $textName, "", "", $extra);

        if ($div == "DUE" || $div == "ADD") {
            $textName = "DATA".$row["SET_MONTH"].$div."_FLG";
            $checked = $cancelFlg ? "checked=checked" : "";
            $extra = "{$checked} id='{$textName}{$div}' onclick=\"chkFlg(this, '{$div}', '{$row[$setLockSoeji."PLAN_LOCK_FLG"]}')\"";
            $setData["DATA".$row["SET_MONTH"]."_FLG"] = knjCreateCheckBox($objForm, $textName, $row["SET_MONTH"], $extra);
        }

        $setKei += !$cancelFlg && $row["SET_MONEY"] ? $row["SET_MONEY"] : 0;
        $totalData[$row["SET_MONTH"]] += !$cancelFlg && $row["SET_MONEY"] ? $row["SET_MONEY"] : 0;

        $totalData["TOTAL_DATA".$row["SET_MONTH"]] += !$cancelFlg && $row["SET_MONEY"] ? $row["SET_MONEY"] : 0;
    }
    $result->free();

    $totalData["TOTAL_KEI"] += $setKei ? $setKei : 0;

    //色
    $setData["BGCOLORKEI"] = "#ffffff";
    //ID名
    $setData["KEI_DISP"] = "KEI".$div."_DISP";
    $setData["KEI"] = is_numeric($setKei) ? number_format($setKei) : "";
    $arg["TEST"][] = $setData;
    $titleId++;

    return array($titleId, $totalData);
}
?>
