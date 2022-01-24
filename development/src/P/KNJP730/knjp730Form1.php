<?php

require_once('for_php7.php');
class knjp730Form1
{
    public function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjp730index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度コンボ
        $arg["YEAR"] = $model->year;

        //生徒データ表示
        $arg["SCH_INFO"] = $db->getOne(knjp730Query::getSchInfo($model));

        //連絡履歴ボタン
        if ($model->Properties["disp_open_btn_KNJP850"] == "1") {
            $arg["disp_open_btn_KNJP850"] = "1";
            $disabled = ($model->schregno == "") ? " disabled " : "";
            $subdata  = "wopen('".REQUESTROOT."/P/KNJP850/knjp850index.php?cmd=sendSearch&PROGRAMID=KNJP730&SEND_SCHREGNO=".$model->schregno."&SEND_AUTH=".AUTHORITY."&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
            $extra = "onclick=\"$subdata\"";
            $arg["btn_contact"] = knjCreateBtn($objForm, "btn_contact", "連絡履歴", $extra.$disabled);
            $contactExistCnt = knjp730Query::getExistsContactCnt($db, $model);
            if ($contactExistCnt > 0) {
                $arg["btn_contact"] .= "&nbsp;連絡履歴あり";
            }
        }

        //支援、補助、学校減免取得
        $slipReducMonthSortTotalMoney = $slipReducMonthTotalMoney = $refundMoneyArr = array();
        $slipAllReducMonthSortTotalMoney = array();
        $reductionInfo = array();
        $reductionInfoPlain = array();
        $query = knjp730Query::getReductionInfo($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //支援、補助、減免の月合計。後で、入金必要額から引く(相殺フラグがあるもののみ)
            if ($row["OFFSET_FLG"] == "1") {
                //伝票単位
                $slipReducMonthTotalMoney[$row["SLIP_NO"]][$row["PLAN_MONTH"]] += $row["PLAN_MONEY"];
                $slipReducMonthSortTotalMoney[$row["SLIP_NO"]][$row["PLAN_MONTH"]][$row["SORT"]] += $row["PLAN_MONEY"];
                $slipReducMonthTotalMoney[$row["SLIP_NO"]]["99"]               += $row["PLAN_MONEY"];

                //伝票合算
                $allReducMonthTotalMoney[$row["PLAN_MONTH"]] += $row["PLAN_MONEY"];
                $allReducMonthTotalMoney["99"]               += $row["PLAN_MONEY"];
            }

            //還付金
            if ($row["REFUND_FLG"] == "1") {
                $refundMoneyArr[$row["PLAN_MONTH"]] += $row["PLAN_MONEY"];
                $refundMoneyArr["99"]               += $row["PLAN_MONEY"];
            }

            //金額カンマ区切り,
            $reductionInfoPlain[$row["SLIP_NO"]."-".$row["SORT"]][$row["PLAN_MONTH"]] = $row["PLAN_MONEY"];
            $row["PLAN_MONEY"] = (strlen($row["PLAN_MONEY"])) ? number_format($row["PLAN_MONEY"]): "";

            $reductionInfo[$row["SLIP_NO"]."-".$row["SORT"]]["ID"]               = $row["SLIP_NO"]."-GEN".$row["SORT"]."-GEN".$row["SORT"];
            $reductionInfo[$row["SLIP_NO"]."-".$row["SORT"]]["00"]               = $row["KOUMOKU"];
            $reductionInfo[$row["SLIP_NO"]."-".$row["SORT"]]["000"]              = $row["SUB_TATLE"];
            $reductionInfo[$row["SLIP_NO"]."-".$row["SORT"]][$row["PLAN_MONTH"]] = $row["PLAN_MONEY"];
        }

        //メイン（各伝票情報）
        $nonCheckCd = array("101");
        $hasData = false;
        $model->setIdArr = $model->slipArr = array();//更新用
        if ($model->schregno != "") {
            $setData = array();
            $befSlipNo = $befChangFlg = "";
            $jugyouryouFlg  = false;//支援金、補助金、学校減免で使用
            $jugyouryouFlg2 = false;//入金必要額で使用
            $nyuugakukinFlg  = false;//入学金減免で使用
            $nyuugakukinFlg2 = false;//入金必要額で使用
            $query = knjp730Query::getMainQuery($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                /******************/
                /** データセット **/
                /******************/
                //伝票内の項目が変わったらデータをセットして初期化
                if ($befChangFlg != "" && ($befChangFlg != $row["PLAN_PAID"])) {
                    $arg["data"][] = $setData;
                    $setData = array();
                }

                //支援金、補助金、減免情報をセット
                if (($jugyouryouFlg || $nyuugakukinFlg) && ($befSlipNo != "" && ($befSlipNo != $row["SLIP_NO"]))) {
                    if (is_array($reductionInfo)) {
                        foreach ($reductionInfo as $reduSlipNoSort => $planArray) {
                            list($reduSlipNo, $sort) = explode("-", $reduSlipNoSort);

                            if ($befSlipNo == $reduSlipNo) {
                                $setData = array();
                                $setData["ROWSPAN"] = $setData["disPlanCollect"] = "1";

                                //名称
                                $setData["COLLECT_M_NAME"] = $planArray["00"];

                                $extra = " id=\"{$planArray["ID"]}\" onClick=\"changeDispText(this);\"";
                                $setData["SUB_TATLE"] = knjCreateCheckBox($objForm, "CHECK-".$planArray["ID"], "1", $extra)."<LABEL for=\"{$planArray["ID"]}\">{$planArray["000"]}</LABEL>";

                                foreach ($model->monthArray as $key => $month) {
                                    $setDispMoney = ($planArray[$month]) ? $planArray[$month]: 0;
                                    $setId = $planArray["ID"]."-".$month;
                                    $model->setIdArr[] = $setId;//更新用

                                    $setPlainMoney = $reductionInfoPlain[$reduSlipNoSort][$month];
                                    $extra = " style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
                                    $setText = knjCreateTextBox($objForm, $setPlainMoney, "COLLECT_MONTH_".$setId, 7, 7, $extra);
                                    knjCreateHidden($objForm, "HID:".$setId, $setPlainMoney);

                                    $setSubData  = "";
                                    $setSubData .= "<div id=\"changeText-{$setId}\"    style=\"display:none;\"  >{$setText}</div>";
                                    $setSubData .= "<div id=\"changeNonText-{$setId}\" style=\"display:inline;\">{$setDispMoney}</div>";
                                    $setData["COLLECT_MONTH_".$month] = $setSubData;
                                }
                                $setData["COLLECT_MONTH_TOTAL"] = ($planArray["99"]) ? $planArray["99"]: 0;

                                $arg["data"][] = $setData;
                                $setData = array();
                            }
                            $model->slipArr[$planArray["ID"]] = $row["SLIP_NO"];//更新用
                        }
                    }
                    $jugyouryouFlg = false;
                    $nyuugakukinFlg = false;
                }

                //電票情報セット(入金必要額、入金月の累計、入金、入金日)
                if ($befSlipNo != "" && ($befSlipNo != $row["SLIP_NO"])) {
                    //電票情報取得
                    $slipTotalInfo = array();
                    $firstSetMoney = 0;
                    $query = knjp730Query::getMainQuery2($model, $befSlipNo);
                    $result2 = $db->query($query);
                    while ($rowNyuukin = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                        array_walk($rowNyuukin, "htmlspecialchars_array");

                        //必要額から支援、補助、学校減免を引く
                        if ($rowNyuukin["SORT"] == "1" && ($jugyouryouFlg2 || $nyuugakukinFlg2)) {
                            $setslipReducMonthTotalMoney = $slipReducMonthTotalMoney[$rowNyuukin["PLAN_MONTH"]];
                            if ($jugyouryouFlg2 && !$nyuugakukinFlg2) {
                                $setslipReducMonthTotalMoney = $slipReducMonthSortTotalMoney[$befSlipNo][$rowNyuukin["PLAN_MONTH"]]["41"];
                            }
                            if (!$jugyouryouFlg2 && $nyuugakukinFlg2) {
                                $setslipReducMonthTotalMoney = $slipReducMonthSortTotalMoney[$befSlipNo][$rowNyuukin["PLAN_MONTH"]]["42"];
                            }
                            $rowNyuukin["ELEMENT"] = $rowNyuukin["ELEMENT"] - $setslipReducMonthTotalMoney;
                        }

                        //金額カンマ区切り,
                        if ($rowNyuukin["SORT"] != "3") {
                            $rowNyuukin["COMMA_ELEMENT"] = (strlen($rowNyuukin["ELEMENT"])) ? number_format($rowNyuukin["ELEMENT"]): "0";
                        } else {
                            $rowNyuukin["COMMA_ELEMENT"] = (strlen($rowNyuukin["ELEMENT"])) ? substr($rowNyuukin["ELEMENT"], 0, 4)."/".substr($rowNyuukin["ELEMENT"], 4, 2)."/".substr($rowNyuukin["ELEMENT"], 6, 2): "";
                        }

                        $slipTotalInfo[$rowNyuukin["SORT"]][$rowNyuukin["PLAN_MONTH"]] = $rowNyuukin["COMMA_ELEMENT"];
                        $slipTotalInfo[$rowNyuukin["SORT"]][$rowNyuukin["PLAN_MONTH"]."NOCOMMA"] = $rowNyuukin["ELEMENT"];
                        $slipTotalInfo[$rowNyuukin["SORT"]]["M_NAME"] = $rowNyuukin["M_NAME"];
                        if ($rowNyuukin["SORT"] == "1" && $rowNyuukin["PLAN_MONTH"] == "04") {
                            $firstSetMoney = $rowNyuukin["ELEMENT"];
                        }
                    }
                    //電票情報セット(入金必要額、入金月の累計、入金、入金日)
                    $sortArr = array();
                    foreach ($slipTotalInfo as $sort => $planArr) {
                        $setData = array();
                        $sortArr[] = $sort;
                        if ($sort == "1") {
                            $setData["disPlanCollect"] = "1";
                            $setSeq = substr($befSlipNo, -3);
                            $setData["COLLECT_M_NAME"] = "入金額<br>（{$setSeq}）";
                            $setData["ROWSPAN"]        = "4";
                        }

                        //入金月の累計
                        if ($sort == "2") {
                            $setData["SUB_TATLE"] = "<div style=\"font-size:9pt\">入金必要額累計</div>";
                            foreach ($model->monthArray as $key => $month) {
                                //入金必要額が"0"の時は、"0"で表示する
                                $zeroChk = ($slipTotalInfo["1"][$month."NOCOMMA"] == 0 || $slipTotalInfo["1"][$month."NOCOMMA"] == "") ? true: false;
                                if ($month == "04") {
                                    $setData["COLLECT_MONTH_04"] = ($zeroChk) ? "0": number_format($firstSetMoney);
                                    $befCuTotal = $firstSetMoney;
                                } else {
                                    $befMonth = sprintf("%02d", ($month == "01") ? 12 : $month - 1);
                                    $setCuTotal = $befCuTotal - $slipTotalInfo["2"][$befMonth."NOCOMMA"] + $slipTotalInfo["1"][$month."NOCOMMA"];
                                    $setData["COLLECT_MONTH_".$month] = ($zeroChk) ? "0": number_format($setCuTotal);
                                    $befCuTotal = $setCuTotal;
                                }
                            }
                            $zeroChkT = ($slipTotalInfo["1"]["99NOCOMMA"] == 0 || $slipTotalInfo["1"]["99NOCOMMA"] == "") ? true: false;
                            $setCuTotal = $befCuTotal - $slipTotalInfo["2"]["03NOCOMMA"];
                            $setData["COLLECT_MONTH_TOTAL"] = ($zeroChkT) ? "0": number_format($setCuTotal);

                            $arg["data"][] = $setData;
                            $setData = array();
                        }
                        //名称セット
                        $setData["SUB_TATLE"] = $planArr["M_NAME"];

                        //各項目セット
                        foreach ($model->monthArray as $key => $month) {
                            if ($sort == "1" || $sort == "2") {
                                $setData["COLLECT_MONTH_".$month] = ($planArr[$month]) ? $planArr[$month]: "0";
                            } else {
                                $setData["COLLECT_MONTH_".$month] = $planArr[$month];
                            }
                        }
                        $setData["COLLECT_MONTH_TOTAL"] = $planArr[99];
                        $arg["data"][] = $setData;
                    }
                    //入金日がないときは、空行をセット
                    if (!in_array("3", $sortArr)) {
                        $setData = array();
                        $setData["SUB_TATLE"]      = "入金日付";
                        $arg["data"][] = $setData;
                    }
                    $setData = array();
                    $jugyouryouFlg2 = false;
                    $nyuugakukinFlg2 = false;
                }

                /****************/
                /** データ作成 **/
                /****************/
                //ROWSPAN
                if ($row["PLAN_PAID"] == "1") {
                    $setData["ROWSPAN"] = "2";
                    $setData["disPlanCollect"] = "1";
                } else {
                    $setData["ROWSPAN"] = "1";
                    $setData["disPlanCollect"] = "";
                }

                //0埋め
                $row["COLLECT_L_CD"] = sprintf("%02d", $row["COLLECT_L_CD"]);
                $row["COLLECT_M_CD"] = sprintf("%02d", $row["COLLECT_M_CD"]);

                //金額カンマ区切り
                $row["NON_COMMA_PLAN_MONEY"] = ($row["PLAN_MONEY"]) ? $row["PLAN_MONEY"]: "0";
                if ($row["COLLECT_M_CD"] != "102") {
                    $row["PLAN_MONEY"] = (strlen($row["PLAN_MONEY"])) ? number_format($row["PLAN_MONEY"]): "0";
                } else {
                    //COLLECT_M_CD"102"は納期限日付
                    $row["PLAN_MONEY"] = (strlen($row["PLAN_MONEY"])) ? substr($row["PLAN_MONEY"], 0, 4)."/".substr($row["PLAN_MONEY"], 4, 2)."/".substr($row["PLAN_MONEY"], 6, 2): "";
                }
                //IDセット
                $setMonth = ($row["PLAN_MONTH"] == "99") ? "TOTAL": $row["PLAN_MONTH"];
                $setChkId = $row["SLIP_NO"]."-".$row["COLLECT_L_CD"]."-".$row["COLLECT_M_CD"];
                $setId    = $setChkId."-".$setMonth;
                $model->setIdArr[] = $setId;//更新用

                //項目名称
                $setData["COLLECT_M_NAME"] = "<LABEL for=\"{$setChkId}\">".$row["COLLECT_M_NAME"]."</LABEL>";
                //項目（計画、入金額）
                $setData["SUB_TATLE"] = ($row["PLAN_PAID"] == "1") ? "計画": "入金額";
                //納期限はチェックボックスを付属
                if ($row["COLLECT_L_CD"] == "102") {
                    $extra = " id=\"{$setChkId}\" onClick=\"changeDispText(this);\"";
                    $setData["SUB_TATLE"] = knjCreateCheckBox($objForm, "CHECK-".$setChkId, "1", $extra)."<LABEL for=\"{$setChkId}\">納入月<br>納期限</LABEL>";
                }

                //チェックボックス
                if (!in_array($row["COLLECT_M_CD"], $nonCheckCd)) {
                    $extra = " id=\"{$setChkId}\" onClick=\"changeDispText(this);\"";
                    $setData["CHECK_BOX"] = knjCreateCheckBox($objForm, "CHECK-".$setChkId, "1", $extra);
                }

                //各項目テキストボックス（チェックon時のみ表示）
                if ($row["COLLECT_M_CD"] != "102") {
                    if ($row["PLAN_PAID"] == "2") {
                        $setId = $setId."-".$row["PLAN_PAID"];
                    }
                    $extra = " style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
                    $setText = knjCreateTextBox($objForm, $row["NON_COMMA_PLAN_MONEY"], "COLLECT_MONTH_".$setId, 7, 7, $extra);
                    knjCreateHidden($objForm, "HID:".$setId, $row["NON_COMMA_PLAN_MONEY"]);

                //納期限の時
                } else {
                    $extra = "onblur=\"isDate(this)\"";
                    $setText = knjCreateTextBox($objForm, $row["PLAN_MONEY"], "COLLECT_MONTH_".$setId, 9, 10, $extra);
                    knjCreateHidden($objForm, "HID:".$setId, $row["PLAN_MONEY"]);
                }
                //横計以外にセット
                if ($row["PLAN_MONTH"] != "99") {
                    $setSubData  = "";
                    //納期限と同じ枠に納入月コンボをセット
                    if ($row["COLLECT_L_CD"] == "102") {
                        //combobox
                        $opt = array();
                        $opt[] = array('label' => "", 'value' => "");
                        $value_flg = false;
                        foreach ($model->monthArray as $month) {
                            $opt[] = array('label' => $month, 'value' => $month);
                            if ($month == $row["PAID_LIMIT_MONTH"]) {
                                $value_flg = true;
                            }
                        }
                        $value = ($row["PAID_LIMIT_MONTH"] && $value_flg) ? $row["PAID_LIMIT_MONTH"] : $opt[0]["value"];
                        $extra = "";
                        $setCmb = knjCreateCombo($objForm, "PAID_LIMIT_MONTH_".$setId, $value, $opt, $extra, 1);

                        $setSubData .= "<div id=\"changeCmb-{$setId}\"    style=\"display:none;\"  >{$setCmb}</div>";
                        $setSubData .= "<div id=\"changeNonCmb-{$setId}\" style=\"display:inline;\">{$row["PAID_LIMIT_MONTH"]}月</div><br>";
                    }
                    $setSubData .= "<div id=\"changeText-{$setId}\"    style=\"display:none;\"  >{$setText}</div>";
                    $setSubData .= "<div id=\"changeNonText-{$setId}\" style=\"display:inline;\">{$row["PLAN_MONEY"]}</div>";
                    $setData["COLLECT_MONTH_".$setMonth] = $setSubData;
                } else {
                    $setData["COLLECT_MONTH_".$setMonth] = $row["PLAN_MONEY"];
                }

                if ($row["JUGYOURYOU_FLG"] == "1") {
                    $jugyouryouFlg  = true;
                }
                if ($row["JUGYOURYOU_FLG"] == "1") {
                    $jugyouryouFlg2 = true;
                }
                if ($row["JUGYOURYOU_FLG"] == "2") {
                    $nyuugakukinFlg  = true;
                }
                if ($row["JUGYOURYOU_FLG"] == "2") {
                    $nyuugakukinFlg2 = true;
                }
                $befSlipNo   = $row["SLIP_NO"];
                $befChangFlg = $row["PLAN_PAID"];
                $hasData     = true;
                $model->slipArr[$row["SLIP_NO"]."-".$row["COLLECT_L_CD"]."-".$row["COLLECT_M_CD"]] = $row["SLIP_NO"];//更新用
            }//sqlFin

            if ($hasData) {
                //SQLを回しながら最終伝票行はセット出来ないので（SQLの取得方法のため）最終情報をセット
                if ($befSlipNo) {
                    $arg["data"][] = $setData;
                }

                //最終伝票行に授業料が含まれている際、（支援金、補助金、減免情報）をセット
                if (($jugyouryouFlg || $nyuugakukinFlg)) {
                    if (is_array($reductionInfo)) {
                        foreach ($reductionInfo as $reduSlipNoSort => $planArray) {
                            list($reduSlipNo, $sort) = explode("-", $reduSlipNoSort);

                            if ($befSlipNo == $reduSlipNo) {
                                $setData = array();
                                $setData["ROWSPAN"] = $setData["disPlanCollect"] = "1";

                                //名称
                                $setData["COLLECT_M_NAME"] = $planArray["00"];

                                $extra = " id=\"{$planArray["ID"]}\" onClick=\"changeDispText(this);\"";
                                $setData["SUB_TATLE"] = knjCreateCheckBox($objForm, "CHECK-".$planArray["ID"], "1", $extra)."<LABEL for=\"{$planArray["ID"]}\">{$planArray["000"]}</LABEL>";

                                foreach ($model->monthArray as $key => $month) {
                                    $setDispMoney = ($planArray[$month]) ? $planArray[$month]: 0;
                                    $setId = $planArray["ID"]."-".$month;
                                    $model->setIdArr[] = $setId;//更新用

                                    $setPlainMoney = $reductionInfoPlain[$reduSlipNoSort][$month];
                                    $extra = " style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
                                    $setText = knjCreateTextBox($objForm, $setPlainMoney, "COLLECT_MONTH_".$setId, 7, 7, $extra);
                                    knjCreateHidden($objForm, "HID:".$setId, $setPlainMoney);

                                    $setSubData  = "";
                                    $setSubData .= "<div id=\"changeText-{$setId}\"    style=\"display:none;\"  >{$setText}</div>";
                                    $setSubData .= "<div id=\"changeNonText-{$setId}\" style=\"display:inline;\">{$setDispMoney}</div>";
                                    $setData["COLLECT_MONTH_".$month] = $setSubData;
                                }
                                $setData["COLLECT_MONTH_TOTAL"] = ($planArray["99"]) ? $planArray["99"]: 0;

                                $arg["data"][] = $setData;
                                $setData = array();
                            }
                            $model->slipArr[$planArray["ID"]] = $row["SLIP_NO"];//更新用
                        }
                    }
                    $jugyouryouFlg = false;
                    $nyuugakukinFlg = false;
                }

                //最終電票情報取得
                $slipTotalInfo = array();
                $firstSetMoney = 0;
                $query = knjp730Query::getMainQuery2($model, $befSlipNo);
                $result = $db->query($query);
                while ($rowNyuukin = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($rowNyuukin, "htmlspecialchars_array");

                    //必要額から支援、補助、学校減免を引く
                    if ($rowNyuukin["SORT"] == "1" && ($jugyouryouFlg2 || $nyuugakukinFlg2)) {
                        $rowNyuukin["ELEMENT"] = $rowNyuukin["ELEMENT"] - $slipReducMonthTotalMoney[$befSlipNo][$rowNyuukin["PLAN_MONTH"]];
                    }

                    //金額カンマ区切り,
                    if ($rowNyuukin["SORT"] != "3") {
                        $rowNyuukin["COMMA_ELEMENT"] = (strlen($rowNyuukin["ELEMENT"])) ? number_format($rowNyuukin["ELEMENT"]): "0";
                    } else {
                        $rowNyuukin["COMMA_ELEMENT"] = (strlen($rowNyuukin["ELEMENT"])) ? substr($rowNyuukin["ELEMENT"], 0, 4)."/".substr($rowNyuukin["ELEMENT"], 4, 2)."/".substr($rowNyuukin["ELEMENT"], 6, 2): "";
                    }

                    $slipTotalInfo[$rowNyuukin["SORT"]][$rowNyuukin["PLAN_MONTH"]] = $rowNyuukin["COMMA_ELEMENT"];
                    $slipTotalInfo[$rowNyuukin["SORT"]][$rowNyuukin["PLAN_MONTH"]."NOCOMMA"] = $rowNyuukin["ELEMENT"];
                    $slipTotalInfo[$rowNyuukin["SORT"]]["M_NAME"] = $rowNyuukin["M_NAME"];
                    if ($rowNyuukin["SORT"] == "1" && $rowNyuukin["PLAN_MONTH"] == "04") {
                        $firstSetMoney = $rowNyuukin["ELEMENT"];
                    }
                }
                //最終電票情報セット(入金必要額、入金月の累計、入金、入金日)
                $sortArr = array();
                foreach ($slipTotalInfo as $sort => $planArr) {
                    $setData = array();
                    $sortArr[] = $sort;
                    if ($sort == "1") {
                        $setData["disPlanCollect"] = "1";
                        $setSeq = substr($befSlipNo, -3);
                        $setData["COLLECT_M_NAME"] = "入金額<br>（{$setSeq}）";
                        $setData["ROWSPAN"]        = "4";
                    }

                    //入金月の累計
                    if ($sort == "2") {
                        $setData["SUB_TATLE"] = "<div style=\"font-size:9pt\">入金必要額累計</div>";
                        foreach ($model->monthArray as $key => $month) {
                            //入金必要額が"0"の時は、"0"で表示する
                            $zeroChk = ($slipTotalInfo["1"][$month."NOCOMMA"] == 0 || $slipTotalInfo["1"][$month."NOCOMMA"] == "") ? true: false;
                            if ($month == "04") {
                                $setData["COLLECT_MONTH_04"] = ($zeroChk) ? "0": number_format($firstSetMoney);
                                $befCuTotal = $firstSetMoney;
                            } else {
                                $befMonth = sprintf("%02d", ($month == "01") ? 12 : $month - 1);
                                $setCuTotal = $befCuTotal - $slipTotalInfo["2"][$befMonth."NOCOMMA"] + $slipTotalInfo["1"][$month."NOCOMMA"];
                                $setData["COLLECT_MONTH_".$month] = ($zeroChk) ? "0": number_format($setCuTotal);
                                $befCuTotal = $setCuTotal;
                            }
                        }
                        $zeroChkT = ($slipTotalInfo["1"]["99NOCOMMA"] == 0 || $slipTotalInfo["1"]["99NOCOMMA"] == "") ? true: false;
                        $setCuTotal = $befCuTotal - $slipTotalInfo["2"]["03NOCOMMA"];
                        $setData["COLLECT_MONTH_TOTAL"] = ($zeroChkT) ? "0": number_format($setCuTotal);

                        $arg["data"][] = $setData;
                        $setData = array();
                    }
                    //名称セット
                    $setData["SUB_TATLE"] = $planArr["M_NAME"];

                    //各項目セット
                    foreach ($model->monthArray as $key => $month) {
                        if ($sort == "1" || $sort == "2") {
                            $setData["COLLECT_MONTH_".$month] = ($planArr[$month]) ? $planArr[$month]: "0";
                        } else {
                            $setData["COLLECT_MONTH_".$month] = $planArr[$month];
                        }
                    }
                    $setData["COLLECT_MONTH_TOTAL"] = $planArr[99];
                    $arg["data"][] = $setData;
                }
                //入金日がないときは、空行をセット
                if (!in_array("3", $sortArr)) {
                    $setData = array();
                    $setData["SUB_TATLE"]      = "入金日付";
                    $arg["data"][] = $setData;
                }
                $setData = array();

                //電票合算情報取得
                $slipTotalInfo = array();
                $firstSetMoney = 0;
                $query = knjp730Query::getMainQuery2($model);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");

                    //必要額から支援、補助、学校減免を引く
                    if ($row["SORT"] == "1") {
                        $hoge = $row["ELEMENT"];
                        $row["ELEMENT"] = $row["ELEMENT"] - $allReducMonthTotalMoney[$row["PLAN_MONTH"]];
                    }

                    //金額カンマ区切り,
                    if ($row["SORT"] != "3") {
                        $row["COMMA_ELEMENT"] = (strlen($row["ELEMENT"])) ? number_format($row["ELEMENT"]): "0";
                    } else {
                        $row["COMMA_ELEMENT"] = (strlen($row["ELEMENT"])) ? substr($row["ELEMENT"], 0, 4)."/".substr($row["ELEMENT"], 4, 2)."/".substr($row["ELEMENT"], 6, 2): "";
                    }

                    $slipTotalInfo[$row["SORT"]][$row["PLAN_MONTH"]] = $row["COMMA_ELEMENT"];
                    $slipTotalInfo[$row["SORT"]][$row["PLAN_MONTH"]."NOCOMMA"] = $row["ELEMENT"];
                    $slipTotalInfo[$row["SORT"]]["M_NAME"] = $row["M_NAME"];
                    if ($row["SORT"] == "1" && $row["PLAN_MONTH"] == "04") {
                        $firstSetMoney = $row["ELEMENT"];
                    }
                }

                //電票合算情報セット(入金必要額、入金月の累計、入金、入金日)
                $sortArr = array();
                foreach ($slipTotalInfo as $sort => $planArr) {
                    $setData = array();
                    $sortArr[] = $sort;
                    if ($sort == "1") {
                        $setData["disPlanCollect"] = "1";
                        $setData["COLLECT_M_NAME"] = "入金総額";
                        $setData["ROWSPAN"]        = "4";
                    }

                    /** データセット **/
                    //入金月の累計
                    if ($sort == "2") {
                        $setData["SUB_TATLE"] = "<div style=\"font-size:9pt\">入金必要額累計</div>";
                        foreach ($model->monthArray as $key => $month) {
                            //入金必要額が"0"の時は、"0"で表示する
                            $zeroChk = ($slipTotalInfo["1"][$month."NOCOMMA"] == 0 || $slipTotalInfo["1"][$month."NOCOMMA"] == "") ? true: false;
                            if ($month == "04") {
                                $setData["COLLECT_MONTH_04"] = ($zeroChk) ? "0": number_format($firstSetMoney);
                                $befCuTotal = $firstSetMoney;
                            } else {
                                $befMonth = sprintf("%02d", ($month == "01") ? 12 : $month - 1);
                                $setCuTotal = $befCuTotal - $slipTotalInfo["2"][$befMonth."NOCOMMA"] + $slipTotalInfo["1"][$month."NOCOMMA"];
                                $setData["COLLECT_MONTH_".$month] = ($zeroChk) ? "0": number_format($setCuTotal);
                                $befCuTotal = $setCuTotal;
                            }
                        }
                        $zeroChkT = ($slipTotalInfo["1"]["99NOCOMMA"] == 0 || $slipTotalInfo["1"]["99NOCOMMA"] == "") ? true: false;
                        $setCuTotal = $befCuTotal - $slipTotalInfo["2"]["03NOCOMMA"];
                        $setData["COLLECT_MONTH_TOTAL"] = ($zeroChkT) ? "0": number_format($setCuTotal);

                        $arg["data"][] = $setData;
                        $setData = array();
                    }
                    //名称セット
                    $setData["SUB_TATLE"] = $planArr["M_NAME"];

                    //各項目セット
                    foreach ($model->monthArray as $key => $month) {
                        if ($sort == "1" || $sort == "2") {
                            $setData["COLLECT_MONTH_".$month] = ($planArr[$month]) ? $planArr[$month]: "0";
                        } else {
                            $setData["COLLECT_MONTH_".$month] = $planArr[$month];
                        }
                    }
                    $setData["COLLECT_MONTH_TOTAL"] = $planArr[99];
                    $arg["data"][] = $setData;
                }
                //入金日がないときは、空行をセット
                if (!in_array("3", $sortArr)) {
                    $setData = array();
                    $setData["SUB_TATLE"]      = "入金日付";
                    $arg["data"][] = $setData;
                }

                //累計過不足金
                $setData = array();
                $setData["disPlanCollect"] = "1";
                $fontColorS = $fontColorF = "";
                $setData["COLLECT_M_NAME"] = "累計過不足金";
                $setData["SUB_TATLE"]      = "過不足";
                foreach ($model->monthArray as $key => $month) {
                    $totalIntoMoney += $slipTotalInfo["2"][$month."NOCOMMA"];//入金累計
                    $totalNeedMoney += $slipTotalInfo["1"][$month."NOCOMMA"];//入金必要額累計
                    $setCuInsuMoney  = $totalIntoMoney - $totalNeedMoney;
                    $fontColorS = ($setCuInsuMoney < 0) ? "<font color=\"red\">": "";
                    $fontColorF = ($setCuInsuMoney < 0) ? "</font>": "";
                    //入金必要額が"0"の時は、"0"で表示する
                    $zeroChk = ($slipTotalInfo["1"][$month."NOCOMMA"] == 0 || $slipTotalInfo["1"][$month."NOCOMMA"] == "") ? true: false;

                    $setData["COLLECT_MONTH_".$month] = ($zeroChk) ? "0": $fontColorS.number_format($setCuInsuMoney).$fontColorF;
                }
                $zeroChkT = ($slipTotalInfo["1"]["99NOCOMMA"] == 0 || $slipTotalInfo["1"]["99NOCOMMA"] == "") ? true: false;
                $setData["COLLECT_MONTH_TOTAL"] = ($zeroChkT) ? "0": $fontColorS.number_format($setCuInsuMoney).$fontColorF;
                $arg["data"][] = $setData;

                //還付金
                $setData = array();
                $setData["disPlanCollect"] = "1";
                $setData["COLLECT_M_NAME"] = "還付金";
                foreach ($model->monthArray as $key => $month) {
                    $setData["COLLECT_MONTH_".$month] = number_format($refundMoneyArr[$month]);
                }
                $setData["COLLECT_MONTH_TOTAL"] = number_format($refundMoneyArr["99"]);
                $arg["data"][] = $setData;
            }
        }

        //テキスト使用不可処理で使用（1:入金必要額, 2:入金）
        $arrKeyId = array();
        $query = knjp730Query::getSlipMonthLMPaidMoney($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            knjCreateHidden($objForm, "PAID-".$row["KEY_ID"], $row["PLAN_PAID_MONEY"]);
            $arrKeyId[] = $row["KEY_ID"];
        }
        //データ無しは、dummyでセット、JavaScriptで使用
        foreach ($model->slipArr as $slipNoLcdMcd => $val) {
            foreach ($model->monthArray as $key => $month) {
                $checkData = $slipNoLcdMcd."-".$month;
                if (!in_array($checkData, $arrKeyId)) {
                    knjCreateHidden($objForm, "PAID-".$slipNoLcdMcd."-".$month, "dummy");
                }
            }
        }

        //ボタン作成
        $extraDis = ($model->schregno != "") ? "" : " disabled ";
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extraDis.$extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extraDis.$extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $endname = "終 了";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //hidden
        knjcreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "MONTH_ARR", implode(",", $model->monthArray));
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", KNJP802);
        knjCreateHidden($objForm, "CALL_PRGID", KNJP730);
        knjCreateHidden($objForm, "STUDENT_DIV", $model->search_div);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "IMAGEPATH", $model->control["LargePhotoPath"]);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "OUTPUT", "1");
        knjCreateHidden($objForm, "OUTPUTID", "2");
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolKind);
        knjCreateHidden($objForm, "CATEGORY_SELECTED", $model->schregno);
        knjCreateHidden($objForm, "GRADE_HR_CLASS", $model->grade_hr_class);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjp730Form1.html", $arg);
    }
}
