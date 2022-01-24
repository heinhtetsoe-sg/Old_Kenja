<?php

require_once('for_php7.php');

class knjmp715Form2 
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjmp715index.php", "", "edit");

        $arg["reload"] = "";

        $db = Query::dbCheckOut();

        //小分類を有する中分類 $model->s_exist_flg == "1"

        if (!$model->isWarning() && $model->cmd != "changeGroup" && $model->cmd != "updEdit") {
            $query = knjmp715Query::getPaidData($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["COLLECT_LM_CD"] = $Row["COLLECT_L_CD"].$Row["COLLECT_M_CD"];
        } else {
            $Row =& $model->field;
        }

        /****************/
        /* 会計項目情報 */
        /****************/
        $dueDisabled = $Row["PAID_MONEY"] > 0 ? " disabled " : "";
        $dueDisabledSexe = !$dueDisabled && $model->s_exist_flg == "1" ? " disabled " : "";

        //グループ
        $query = knjmp715Query::getGroupList($model);
        $extra = "$dueDisabledSexe $dueDisabled onChange=\" btn_submit('changeGroup')\"";
        makeCmb($objForm, $arg, $db, $query, $Row["COLLECT_GRP_CD"], "COLLECT_GRP_CD", $extra, 1, "BLANK");

        //会計項目
        $query = knjmp715Query::getMMst($model);
        $extra = "$dueDisabledSexe $dueDisabled onChange=\" btn_submit('changeGroup')\"";
        makeCmb($objForm, $arg, $db, $query, $Row["COLLECT_LM_CD"], "COLLECT_LM_CD", $extra, 1, "BLANK");

        //単価
        $query = knjmp715Query::getMMst($model, $Row["COLLECT_LM_CD"]);
        $getTanka = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["TANKA"] = $getTanka["TANKA"];

        //入金必要額
        $extra = "$dueDisabledSexe $dueDisabled style=\"text-align:right\" onblur=\"this.value=toInteger(this.value), moneyDue_check1()\"";
        $arg["data"]["MONEY_DUE"] = knjCreateTextBox($objForm, $Row["MONEY_DUE"], "MONEY_DUE", 10, 8, $extra);

        //納入種別
        $opt = array(1, 2);
        $Row["PAY_DIV"] = ($Row["PAY_DIV"] == "") ? "1" : $Row["PAY_DIV"];
        $extra = array("$dueDisabled id=\"PAY_DIV1\"", "$dueDisabled id=\"PAY_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "PAY_DIV", $Row["PAY_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        global $sess;
        //納入日F
        $pay_date = str_replace("-", "/", $Row["PAY_DATE"]);
        $extra = "$dueDisabled onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"";
        $objDueDate = knjCreateTextBox($objForm, $pay_date, "PAY_DATE", 12, 12, $extra);

        //日付ボタン
        $extra = "$dueDisabled onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=PAY_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['PAY_DATE'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $objDueDateButton = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        $arg["data"]["PAY_DATE"] = $objDueDate .$objDueDateButton;

        //備考
        $extra = "";
        $arg["data"]["CHANGE_REMARK"] = knjCreateTextBox($objForm, $Row["CHANGE_REMARK"], "CHANGE_REMARK", 80, 80, $dueDisabled);

        //更新
        $extra = "$dueDisabled onclick=\"return btn_submit('dueUpd');\"";
        $arg["button"]["btn_upd"] = knjCreateBtn($objForm, "btn_upd", "更 新", $extra);

        //削除
        $extra = "$dueDisabled onclick=\"return btn_submit('dueDel');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //終了ボタンを作成
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);

        //hiddenを作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "TMP_MONEY_DUE", $model->money_due);
        knjCreateHidden($objForm, "TMP_PAID_MONEY");
        knjCreateHidden($objForm, "TMP_REPAY_MONEY");
        knjCreateHidden($objForm, "TMP_COLLECT_GRP_CD", $model->collect_grp_cd);
        knjCreateHidden($objForm, "TMP_EXP_MCD", $model->exp_mcd);
        knjCreateHidden($objForm, "TMP_EXP_LCD", $model->exp_lcd);
        knjCreateHidden($objForm, "TMP_PAY_DIV", $model->pay_div);
        knjCreateHidden($objForm, "TMP_S_EXIST_FLG", $model->s_exist_flg);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.top_frame.location.href='knjmp715index.php?cmd=list';";
        }

        View::toHTML($model, "knjmp715Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "NEW") {
        $opt[] = array("label" => "新規作成", "value" => "");
    } else if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
