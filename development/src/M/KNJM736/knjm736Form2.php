<?php

require_once('for_php7.php');

class knjm736Form2 
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjm736index.php", "", "edit");

        $arg["reload"] = "";

        $db = Query::dbCheckOut();

        //小分類を有する中分類なら、入金・返金ともに編集不可
        if ($model->s_exist_flg == "1" || $model->pay_div == "1") {
            $paidDisabled = "disabled";
            $repayDisabled = "disabled";
        } else {
            $paidDisabled = "";
            $repayDisabled = "";
        }

        if (!$model->isWarning()) {
            $query = knjm736Query::getPaidData($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }
        $arg["TARGET_COLLECT"] = $model->collect_grp_cd.":".$model->exp_lcd.$model->exp_mcd."　".$model->exp_mname;

        /****************/
        /* 会計項目情報 */
        /****************/

        $dueDisabled = $Row["PAID_MONEY"] > 0 ? " disabled " : "";
        $dueDisabledSexe = !$dueDisabled && $model->s_exist_flg == "1" ? " disabled " : "";
        $arg["data"]["TANKA"] = $Row["TANKA"];
        knjCreateHidden($objForm, "TANKA", $Row["TANKA"]);

        //入金必要額
        $extra = "$dueDisabledSexe $dueDisabled readOnly=readOnly style=\"text-align:right\" onblur=\"this.value=toInteger(this.value), moneyDue_check1()\"";
        $arg["data"]["MONEY_DUE"] = knjCreateTextBox($objForm, $Row["MONEY_DUE"], "MONEY_DUE", 10, 8, $extra);

        //数量
        $extra = "$dueDisabledSexe $dueDisabled style=\"text-align:right\" onblur=\"this.value=toInteger(this.value), moneyDue_check1()\"";
        $arg["data"]["COLLECT_CNT"] = knjCreateTextBox($objForm, $Row["COLLECT_CNT"], "COLLECT_CNT", 2, 2, $extra);

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

        //期間F
        $change_date_f = str_replace("-", "/", $Row["CHANGE_DATE_F"]);
        $extra = "$dueDisabled onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"";
        $objDueDate = knjCreateTextBox($objForm, $change_date_f, "CHANGE_DATE_F", 12, 12, $extra);

        //日付ボタン
        $extra = "$dueDisabled onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=CHANGE_DATE_F&frame='+getFrameName(self) + '&date=' + document.forms[0]['CHANGE_DATE_F'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $objDueDateButton = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        $arg["data"]["CHANGE_DATE_F"] = $objDueDate .$objDueDateButton;

        //期間T
        $change_date_t = str_replace("-", "/", $Row["CHANGE_DATE_T"]);
        $extra = "$dueDisabled onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"";
        $objDueDate = knjCreateTextBox($objForm, $change_date_t, "CHANGE_DATE_T", 12, 12, $extra);

        //日付ボタン
        $extra = "$dueDisabled onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=CHANGE_DATE_T&frame='+getFrameName(self) + '&date=' + document.forms[0]['CHANGE_DATE_T'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $objDueDateButton = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        $arg["data"]["CHANGE_DATE_T"] = $objDueDate .$objDueDateButton;

        //徴収変更
        $query = knjm736Query::getNamecd($model->year, "G214");
        $extra = "$dueDisabled onChange=\"blockDispChange(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["CHANGE_CD"], "CHANGE_CD", $extra, 1, "KARA");

        //申請日ブロックの表示/非表示
        $arg["data"]["TR_SINSEI_DATE"] = $Row["CHANGE_CD"] != "01" ? "style=\"display:none\" " : "";
        //申請日付
        $sinsei_date = str_replace("-", "/", $Row["SINSEI_DATE"]);
        $extra = "$dueDisabled onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"";
        $objDueDate = knjCreateTextBox($objForm, $sinsei_date, "SINSEI_DATE", 12, 12, $extra);

        //日付ボタン
        $extra = "$dueDisabled onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=SINSEI_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['SINSEI_DATE'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $objDueDateButton = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        $arg["data"]["SINSEI_DATE"] = $objDueDate .$objDueDateButton;

        //減免ブロックの表示/非表示
        $arg["data"]["TR_GENMEN"] = $Row["CHANGE_CD"] != "02" ? "style=\"display:none\" " : "";
        //減免額
        $extra = "$dueDisabled style=\"text-align:right\" onblur=\"this.value=toInteger(this.value) \"";
        $arg["data"]["GENMEN_MONEY"] = knjCreateTextBox($objForm, $Row["GENMEN_MONEY"], "GENMEN_MONEY", 10, 8, $extra);

        //数量
        $extra = "$dueDisabled style=\"text-align:right\" onblur=\"this.value=toInteger(this.value) \"";
        $arg["data"]["GENMEN_CNT"] = knjCreateTextBox($objForm, $Row["GENMEN_CNT"], "GENMEN_CNT", 2, 2, $extra);

        //備考
        $extra = "";
        $arg["data"]["CHANGE_REMARK"] = knjCreateTextBox($objForm, $Row["CHANGE_REMARK"], "CHANGE_REMARK", 80, 80, $dueDisabled);

        //更新ボタンを作成
        $extra = "$dueDisabled onclick=\"return btn_submit('dueUpdate');\"";
        $arg["button"]["btn_due_update"] = knjCreateBtn($objForm, "btn_due_update", "項目更新", $extra);

        //削除ボタンを作成
        $extra = "$dueDisabled onclick=\"return btn_submit('dueDel');\"";
        $arg["button"]["btn_due_delete"] = knjCreateBtn($objForm, "btn_due_delete", "項目削除", $extra);

        /********/
        /* 入金 */
        /********/

        //入金データ選択
        $query = knjm736Query::getPaySeq($model);
        $extra = "$paidDisabled onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PAID_SEQ"], "PAID_SEQ", $extra, 1, "NEW");

        //入金額
        $query = knjm736Query::getMPaid($model);
        $mPaid = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $extra = "$paidDisabled style=\"text-align:right\" onblur=\"this.value=toInteger(this.value), money_check1()\"";
        $arg["data"]["PAID_MONEY"] = knjCreateTextBox($objForm, $mPaid["PAID_MONEY"], "PAID_MONEY", 10, 8, $extra);

        //指定されたデータ以外の入金合計
        knjCreateHidden($objForm, "PAID_MONEY_TOTAL", $mPaid["PAID_MONEY_TOTAL"]);
        //入金合計
        knjCreateHidden($objForm, "PAID_MONEY_ALL", ($mPaid["PAID_MONEY_TOTAL"] + $mPaid["PAID_MONEY"]));

        global $sess;
        //入金日
        $paid_money_date = str_replace("-", "/", $mPaid["PAID_MONEY_DATE"]);
        $extra = "$paidDisabled onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"";
        $objPaidDate = knjCreateTextBox($objForm, $paid_money_date, "PAID_MONEY_DATE", 12, 12, $extra);

        //日付ボタン
        $extra = "$paidDisabled onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=PAID_MONEY_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['PAID_MONEY_DATE'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $objPaidDateButton = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        $arg["data"]["PAID_MONEY_DATE"] = $objPaidDate .$objPaidDateButton;

        //入金区分
        $query = knjm736Query::getNamecd($model->year, "G205");
        $extra = "$paidDisabled";
        makeCmb($objForm, $arg, $db, $query, $mPaid["PAID_MONEY_DIV"], "PAID_MONEY_DIV", $extra, 1, "KARA");

        //更新ボタンを作成
        $extra = "$paidDisabled onclick=\"return btn_submit('paidUpdate');\"";
        $arg["button"]["btn_paid_update"] = knjCreateBtn($objForm, "btn_paid_update", "入金更新", $extra);

        //削除ボタンを作成
        $extra = "$paidDisabled onclick=\"return btn_submit('paidDel');\"";
        $arg["button"]["btn_paid_delete"] = knjCreateBtn($objForm, "btn_paid_delete", "入金削除", $extra);

        /********/
        /* 返金 */
        /********/

        //返金データ選択
        $query = knjm736Query::getRepaySeq($model);
        $extra = "$repayDisabled onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["REPAY_SEQ"], "REPAY_SEQ", $extra, 1, "NEW");

        //返金額
        $query = knjm736Query::getMRepay($model);
        $mRepay = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $extra = "$repayDisabled style=\"text-align:right\" onblur=\"this.value=toInteger(this.value), money_check2()\"";
        $arg["data"]["REPAY_MONEY"] = knjCreateTextBox($objForm, $mRepay["REPAY_MONEY"], "REPAY_MONEY", 10, 8, $extra);

        //指定されたデータ以外の返金合計
        knjCreateHidden($objForm, "REPAY_MONEY_TOTAL", $mRepay["REPAY_MONEY_TOTAL"]);
        //返金合計
        knjCreateHidden($objForm, "REPAY_MONEY_ALL", ($mRepay["REPAY_MONEY_TOTAL"] + $mRepay["REPAY_MONEY"]));

        global $sess;
        //返金日
        $repay_date = str_replace("-", "/", $mRepay["REPAY_MONEY_DATE"]);
        $extra = "$repayDisabled onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this)\"";
        $objRepayDate = knjCreateTextBox($objForm, $repay_date, "REPAY_MONEY_DATE", 12, 12, $extra);

        //日付ボタン
        $extra = "$repayDisabled onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=REPAY_MONEY_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['REPAY_MONEY_DATE'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $objRepayDateButton = knjCreateBtn($objForm, "btn_calen2", "･･･", $extra);
        $arg["data"]["REPAY_MONEY_DATE"] = $objRepayDate .$objRepayDateButton;

        //返金区分
        $query = knjm736Query::getNamecd($model->year, "G209");
        $extra = "$repayDisabled";
        makeCmb($objForm, $arg, $db, $query, $mRepay["REPAY_MONEY_DIV"], "REPAY_MONEY_DIV", $extra, 1, "KARA");

        //更新ボタンを作成
        $extra = "$repayDisabled onclick=\"return btn_submit('repayUpdate');\"";
        $arg["button"]["btn_repay_update"] = knjCreateBtn($objForm, "btn_repay_update", "返金更新", $extra);

        //削除ボタンを作成
        $extra = "$repayDisabled onclick=\"return btn_submit('repayDel');\"";
        $arg["button"]["btn_repay_delete"] = knjCreateBtn($objForm, "btn_repay_delete", "返金削除", $extra);

        //クリアボタンを作成
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

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
            $arg["reload"]  = "parent.top_frame.location.href='knjm736index.php?cmd=list';";
        }

        View::toHTML($model, "knjm736Form2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "NEW") {
        $opt[] = array("label" => "新規作成", "value" => "");
    } else if ($blank == "KARA") {
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
