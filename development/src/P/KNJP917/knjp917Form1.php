<?php

require_once('for_php7.php');

class knjp917Form1 {
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp917index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //Windowサイズ
        $arg["valWindowHeight"]  = $model->windowHeight - 200;
        $resizeFlg = ($model->cmd == "cmdStart" || $model->cmd == "search") ? true : false;

        //年度
        $arg["YEAR"] = $model->year;

        //学籍番号
        $arg["SCHREGNO"] = $model->schregno;

        //名前
        $name = $db->getOne(knjp917Query::getStudentName($model->schregno));
        $arg["NAME_SHOW"] = $name;

        //返金（給付）伝票CSV取込確認チェック
        $updateDisable = "";
        $query = knjp917Query::getHenkinApproval($model);
        $checkCnt = $db->getOne($query);
        if ($checkCnt > 0) {
            $updateDisable = " disabled";
            $arg["HENKIN_ZUMI"] = "<font color=\"red\">返金済</font>";
        }

        //給付上限
        $maxBene = $db->getOne(knjp917Query::getMaxBenefitMoney($model));
        $arg["MAX_BENE"] = number_format($maxBene);
        knjCreateHidden($objForm, "MAX_BENE", $maxBene);

        //生徒データ表示
        $kyufuAll = 0;
        $textBoxList = $sep = "";
        $model->updLMcd = array();
        $model->updLMScd = array();
        $model->updLMName = array();
        if ($model->cmd == "edit" && ($model->schregno != "")) {
            $query = knjp917Query::getStudentInfoData($model);

            $bifKey = "";
            $befLMcd = "";
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                $lmsCd = $row["OUTGO_L_CD"]."-".$row["OUTGO_M_CD"]."-".$row["OUTGO_S_CD"];

                $setLMcd = $row["OUTGO_L_CD"].$row["OUTGO_M_CD"];

                if ($befLMcd != $setLMcd) {
                    //科目・項目名
                    $row["TITLE"] = $row["LEVY_M_NAME"];

                    //項目の給付上限額合計
                    $outgoTotal = $db->getOne(knjp917Query::getStudentInfoData($model, $setLMcd, "OUT_GO"));
                    $row["OUTGO_TOTAL"] = number_format($outgoTotal);

                    //現在の給付合計
                    $kyufuTotal = $db->getOne(knjp917Query::getStudentInfoData($model, $setLMcd, "KYUFU"));
                    $kyufuTotal =  (!isset($model->warning)) ? $kyufuTotal: $model->field["HID_KYUFU_LM_TOTAL:".$setLMcd];
                    $row["KYUFU_LM_TOTAL"] = number_format($kyufuTotal);
                    $row["KYUFU_LM_TOTAL_ID"] = "KYUFU_LM_TOTAL_ID:".$setLMcd;
                    knjCreateHidden($objForm, "HID_KYUFU_LM_TOTAL:".$setLMcd, $kyufuTotal);

                    //チェックボックス(全て)
                    $setName = "CHECK_ALL:".$lmsCd;
                    $extra = "id=\"{$setName}\" onclick=\"allCheck(this)\"";
                    $row["CHECK_ALL"] = knjCreateCheckBox($objForm, $setName, "1", $extra, "");
                    $row["CHECK_ALL_NAME"] = $setName;

                    $model->updLMcd[$setLMcd] = $outgoTotal;
                }
                $befLMcd = $setLMcd;

                //チェックボックス
                $setName = "CHECK_BOX:".$lmsCd;
                $extra = "id=\"{$setName}\" class=\"changeColor\" data-name=\"{$setName}\" onclick=\"textOpen(this, '')\"";
                if (!isset($model->warning)) {
                    $checked = ($row["KYUFU_MONEY"] != "") ? " checked": "";
                } else {
                    $checked = ($model->field["CHECK_BOX:".$lmsCd] == "1") ? " checked": "";
                }
                $row["CHECK_BOX"] = knjCreateCheckBox($objForm, $setName, "1", $extra.$checked, "");
                $row["CHECK_BOX_NAME"] = $setName;

                //更新用
                $model->updLMScd[$lmsCd] = $row["OUTGO_MONEY"];
                $model->updLMName[$lmsCd] = $row["LEVY_M_NAME"]."----".$row["LEVY_S_NAME"];

                //給付金合計
                $kyufuAll += $row["KYUFU_MONEY"];

                //給付金テキストボックス
                $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value); checkValue(this);\"";
                $textName = "KYUFU:".$lmsCd;
                if (!isset($model->warning)) {
                    $disText = ($row["KYUFU_MONEY"] != "") ? "": " disabled";
                    $kyufuMoney = $row["KYUFU_MONEY"];
                } else {
                    $disText = ($model->field["KYUFU:".$lmsCd] != "" || $model->field["CHECK_BOX:".$lmsCd] == "1") ? "": " disabled";
                    $kyufuMoney = $model->field["KYUFU:".$lmsCd];
                }
                $row["KYUFU_MONEY"] = knjCreateTextBox($objForm, $kyufuMoney, $textName, 8, 8, $extra.$disText);

                //給付上限金額カンマ区切り
                $row["OUTGO_MONEY"] = number_format($row["OUTGO_MONEY"]);
                $row["OUTGO_MONEY_ID"] = "OUTGO_MONEY_ID:".$lmsCd;

                //日付
                $row["OUTGO_DATE"] = strtr($row["OUTGO_DATE"], "-", "/");

                //javaxcriptで計算用
                $textBoxList .= $sep.$lmsCd;
                $sep = ",";

                $arg["data"][] = $row;
            }
            $result->free();
        }

        knjCreateHidden($objForm, "textBoxList", $textBoxList);

        //給付合計金額
        $kyufuAll =  (!isset($model->warning)) ? $kyufuAll: $model->field["HID_KYUFU_TOTAL"];
        $arg["data2"]["KYUFU_TOTAL"] = number_format($kyufuAll);
        knjCreateHidden($objForm, "KYUFU_TOTAL", $kyufuAll);
        knjCreateHidden($objForm, "HID_KYUFU_TOTAL", $kyufuAll);

        //給付差引金額
        $sagaku = $maxBene - $kyufuAll;
        $sagaku =  (!isset($model->warning)) ? $sagaku: $model->field["HID_KYUFU_SAGAKU"];
        $arg["data2"]["KYUFU_SAGAKU"] = number_format($sagaku);
        $arg["data2"]["COLOR"] = ($sagaku < 0) ? "red": "black";
        knjCreateHidden($objForm, "KYUFU_SAGAKU", $sagaku);
        knjCreateHidden($objForm, "HID_KYUFU_SAGAKU", $sagaku);

        //返金日付
        $calenVal = $model->field["OUTGO_DATE"] == "" ? str_replace("-", "/", CTRL_DATE): $model->field["OUTGO_DATE"];
        $arg["OUTGO_DATE"] = View::popUpCalendarAlp($objForm, "OUTGO_DATE", $calenVal, "", "");

        //返金日付
        $calenVal = $model->field["PRINT_DATE"] == "" ? str_replace("-", "/", CTRL_DATE): $model->field["PRINT_DATE"];
        $arg["PRINT_DATE"] = View::popUpCalendarAlp($objForm, "PRINT_DATE", $calenVal, "", "");

        /**************/
        /* ボタン作成 */
        /**************/
        //印刷
        $disPrint = $model->sendList[0] == "" ? " disabled": "";
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra.$disPrint);

        //更新
        $disUpd = $model->schregno == "" ? " disabled": "";
        $extra = $disabled."onclick=\"return btn_submit('update');\"".$disUpd.$updateDisable;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //終了
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJP917");
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolCd);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "YEAR", $model->year);
        //帳票に渡す用
        knjCreateHidden($objForm, "sendPrintList", $model->sendList);
        knjCreateHidden($objForm, "sortFlg", $model->sortFlg);

        //DB切断
        Query::dbCheckIn($db);

        if ($resizeFlg) {
            $arg["reload"] = "submit_reSize()";
        }

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjp917Form1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
