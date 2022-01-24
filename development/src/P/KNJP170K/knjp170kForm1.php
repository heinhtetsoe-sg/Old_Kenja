<?php

require_once('for_php7.php');

class knjp170kForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjp170kindex.php", "", "main");
        $db = Query::dbCheckOut();

        //対象年度
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $arg["HEISEI_1"] = common::DateConv1(CTRL_YEAR-0 . '/04/01', 10); //平成○○年度
        $arg["HEISEI_2"] = common::DateConv1(CTRL_YEAR-1 . '/04/01', 10); //平成○○年度

        //学年
        $result = $db->query(knjp170kQuery::selectQueryGrade($model));
        $opt1 = $opt2 = array();
        $opt2[] = array("label" => "　　　　" ,
                        "value" => "00-000");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[$row["GRADE"]] = array("label"  =>  $row["GRADE"] ,
                                         "value"  => $row["GRADE"]);
            if (!isset($model->search["GRADE"])) $model->search["GRADE"] = $row["GRADE"];
            if ($row["GRADE"] == $model->search["GRADE"]) {
                $opt2[] = array("label" => $row["HR_NAMEABBV"] ,
                                "value" => $row["GRADE"] ."-" .$row["HR_CLASS"]);

                if (!isset($model->search["HR_CLASS"]) || $model->cmd == "maingrade") {
                    $model->search["HR_CLASS"] = $row["GRADE"] ."-" .$row["HR_CLASS"];
                    $model->cmd = "main";
                }
            }
        }

        //学年
        $extra = "onchange=\"btn_submit('maingrade');\"";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->search["GRADE"], $opt1, $extra, 1);

        //年組
        $extra = "onchange=\"btn_submit('mainclass');\"";
        $arg["HR_CLASS"] = knjCreateCombo($objForm, "HR_CLASS", $model->search["HR_CLASS"], $opt2, $extra, 1);

        //radio
        $requestroot = REQUESTROOT;
        $opt = array(1, 2, 3);
        $model->changePrg = ($model->changePrg == "") ? "2" : $model->changePrg;
        $extra = array();
        foreach($opt as $key => $val) {
            $setPrgId = "KNJP176K";
            if ($val == "2") {
                $setPrgId = "KNJP170K";
            } else if ($val == "3") {
                $setPrgId = "KNJP177K";
            }
            array_push($extra, " id=\"CHANG_PRG{$val}\" onClick=\"Page_jumper('{$requestroot}', '{$setPrgId}');\"");
        }
        $radioArray = knjCreateRadio($objForm, "CHANG_PRG", $model->changePrg, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //都道府県
        $rankUsePref = array();
        $result = $db->query(knjp170kQuery::selectQueryPref($model));
        $opt = array();
        $opt[] = array("label" => "　　　　" ,
                       "value" => "00");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"] ."：" .htmlspecialchars($row["NAME1"]),
                           "value" => $row["NAMECD2"]);
            if ($row["USE_RANK"] == "1") {
                $rankUsePref[$row["NAMECD2"]] = $row;
            }
        }
        $extra = "onchange=\"btn_submit('main');\"";
        $arg["PREF"] = knjCreateCombo($objForm, "PREF", $model->search["PREF"], $opt, $extra, 1);

        //検索対象
        $opt = array(1, 2);
        $model->search["RAD_PREF"] = ($model->search["RAD_PREF"] == "") ? "1" : $model->search["RAD_PREF"];
        $extra = array("id=\"RAD_PREF1\" onclick=\"return btn_submit('main')\"",
                       "id=\"RAD_PREF2\" onclick=\"return btn_submit('main')\"");
        $radioArray = knjCreateRadio($objForm, "RAD_PREF", $model->search["RAD_PREF"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        /********************/
        /* チェックボックス */
        /********************/
        //軽減額 左
        $extra = "onclick=\"checkAll(this)\"";
        $arg["CHACKALL_1"] = knjCreateCheckBox($objForm, "CHACKALL_1", "1", $extra);
        //軽減額 右
        $extra = "onclick=\"checkAll(this)\"";
        $arg["CHACKALL_2"] = knjCreateCheckBox($objForm, "CHACKALL_2", "1", $extra);

        $rankNameCd = "G218";
        if ("2016" > CTRL_YEAR) {
            $rankNameCd = "G213";
        } else if ("2016" == CTRL_YEAR && $model->search["GRADE"] > "01") {
            $rankNameCd = "G213";
        } else if ("2017" == CTRL_YEAR && $model->search["GRADE"] == "03") {
            $rankNameCd = "G213";
        }

        //リスト
        $sql = knjp170kQuery::getNameMst($model, $rankNameCd);
        $result = $db->query($sql);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$row["LABEL"]."')\"",
                                 "NAME" => $row["LABEL"]);
        }
        $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
        $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
        foreach ($dataArray as $key => $val) {
            $setData["CLICK_NAME"] = $val["NAME"];
            $setData["CLICK_VAL"] = $val["VAL"];
            $arg["menu"][] = $setData;
        }
        $setData["CLICK_NAME"] = "データクリア";
        $setData["CLICK_VAL"] = "\"javascript:setClickValue('888')\"";
        $arg["menu"][] = $setData;
        $result->free();
        $query = knjp170kQuery::selectQuery($model);

        $result = $db->query($query);
        $model->schregno = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schregno[] = $row["SCHREGNO"];
            @array_walk($row, "htmlspecialchars_array");
            $usePref = $row["PREF_CD"];

            $rankFlg1 = is_array($rankUsePref[$usePref]) && CTRL_YEAR >= $rankUsePref[$usePref]["ZENKI_KAISI_YEAR"] ? true : false;
            $rankFlg2 = is_array($rankUsePref[$usePref]) && CTRL_YEAR >= $rankUsePref[$usePref]["KOUKI_KAISI_YEAR"] ? true : false;
            knjCreateHidden($objForm, "RANK_FLG1_".$row["SCHREGNO"], $rankFlg1 ? "1" : "2");
            knjCreateHidden($objForm, "RANK_FLG2_".$row["SCHREGNO"], $rankFlg2 ? "1" : "2");
            //リンク設定
            $linkLabel = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
            $extra = "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availheight);";
            $subdata = "wopen('".REQUESTROOT."/P/KNJP172K/knjp172kindex.php?&cmd=edit&CALLID=KNJP170K&SCHREGNO={$row["SCHREGNO"]}".$extra;
            $row["HR_NAMEABBV"] = View::alink("#", htmlspecialchars($linkLabel),"onclick=\"$subdata\"");

            //コピー用
            $model->income  = array("SCHREGNO"          => $row["SCHREGNO"],
                                    "GRADE2"            => $model->search["GRADE"],
                                    "REDUC_INCOME_1"    => $row["REDUC_INCOME_1"],
                                    "INCOME_SIBLINGS1"  => $row["INCOME_SIBLINGS1"],
                                    "REDUC_RANK_1"      => $row["REDUC_RANK_1"],
                                    "REDUC_INCOME_2"    => $row["REDUC_INCOME_2"],
                                    "INCOME_SIBLINGS2"  => $row["INCOME_SIBLINGS2"],
                                    "REDUC_RANK_2"      => $row["REDUC_RANK_2"],
                                    "RANK_FLG_1"        => $rankFlg1 ? "1" : "2",
                                    "RANK_FLG_2"        => $rankFlg2 ? "1" : "2",
                                    "OBJ_NAME"          => "REDUC_INCOME_1[]",
                                    "PREFECTURESCD"     => $row["PREFECTURESCD"],
                                    "PREF_CD2"          => $usePref,
                                    "CASE_CD2"          => $row["REDUC_RARE_CASE_CD"]
                                   );

            //軽減額取得
            $row["UNREGISTERED_COLOR1"] = "#ffffff";
            $row["UNREGISTERED_COLOR2"] = "#ffffff";
            if (!$row["REDUC_RANK_1"] && is_numeric($row["REDUC_INCOME_1"])) {
                $query = knjp170kQuery::getRankDef($model, "1", $row["REDUC_INCOME_1"], $row["INCOME_SIBLINGS1"], $row);
                $reducMstPrefRank = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $row["REDUC_RANK_1"] = $reducMstPrefRank["NAME1"];
                if ($row["REDUC_RARE_CASE_CD"] == '' && !$reducMstPrefRank["PREFECTURESCD"]) {
                    $row["UNREGISTERED_COLOR1"] = "pink";
                }
            }
            if (!$row["REDUC_RANK_2"] && is_numeric($row["REDUC_INCOME_2"])) {
                $query = knjp170kQuery::getRankDef($model, "2", $row["REDUC_INCOME_2"], $row["INCOME_SIBLINGS2"], $row);
                $reducMstPrefRank = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $row["REDUC_RANK_2"] = $reducMstPrefRank["NAME1"];
                if ($row["REDUC_RARE_CASE_CD"] == '' && !$reducMstPrefRank["PREFECTURESCD"]) {
                    $row["UNREGISTERED_COLOR2"] = "pink";
                }
            }

            //軽減特殊ケースコードの設定
            if ($row["REDUC_RARE_CASE_CD"] != '') {
                $query = knjp170kQuery::getSpecailCode($row["REDUC_RARE_CASE_CD"]);
                $namespare1 = $db->getOne($query);
                list($flg, $dummy) = preg_split("/:/", $namespare1);
                if ($flg == '1') { //入力あり
                    $ReductionmoneyVisible = "";
                } else { //入力なし
                    $ReductionmoneyVisible = "display:none";
                    //軽減額表示
                    $row["REDUCTIONMONEY_12"] = (is_numeric($row["REDUCTIONMONEY_1"])) ? number_format($row["REDUCTIONMONEY_1"]) : "";
                    $row["REDUCTIONMONEY_22"] = (is_numeric($row["REDUCTIONMONEY_2"])) ? number_format($row["REDUCTIONMONEY_2"]) : "";
                }
            } else { //入力なし
                $ReductionmoneyVisible = "display:none";
                if ($model->cmd == "copy") {

                    $setPref = "";
                    if ($row["REDUC_DEC_FLG_1"] != 1 && strlen($row["REDUC_INCOME_1"]) > 0) {
                        $getCopy1 = knjp170kModel::getSendModel2($db, $model->income);
                        if (!$getCopy1["SET_ERR_FLG"] && strlen($model->income["REDUC_INCOME_1"]) > 0) {
                            $row["REDUC_INCOME_1"]   = $model->income["REDUC_INCOME_1"];
                            $row["REDUCTION_SEQ_1"]  = $rankFlg1 ? $row["REDUCTION_SEQ_1"] : $getCopy1["REDUCTION_SEQ"];
                            $row["REDUCTIONMONEY_1"] = $rankFlg1 ? $row["REDUCTIONMONEY_1"] : $getCopy1["REDUCTIONMONEY_1"];
                            $row["PREFECTURESCD"] = $rankFlg1 ? $row["PREFECTURESCD"] : $getCopy1["PREFECTURESCD"];
                            $row["PREF2"] = $rankFlg1 ? $row["PREF2"] : $getCopy1["PREF"];
                            $setPref = $row["PREF2"];
                            if (!$row["PREF2"]) {
                                $row["UNREGISTERED_COLOR1"] = "pink";
                            }
                        }
                    }
                    if ($row["REDUC_DEC_FLG_2"] != 1 && strlen($row["REDUC_INCOME_2"]) > 0) {
                        $model->income["OBJ_NAME"] = "REDUC_INCOME_2[]";
                        $getCopy2 = knjp170kModel::getSendModel2($db, $model->income);
                        if (!$getCopy2["SET_ERR_FLG"] && strlen($model->income["REDUC_INCOME_2"]) > 0) {
                            $row["REDUC_INCOME_2"]   = $model->income["REDUC_INCOME_2"];
                            $row["REDUCTION_SEQ_2"]  = $rankFlg2 ? $row["REDUCTION_SEQ_2"] : $getCopy2["REDUCTION_SEQ"];
                            $row["REDUCTIONMONEY_2"] = $rankFlg2 ? $row["REDUCTIONMONEY_2"] : $getCopy2["REDUCTIONMONEY_2"];
                            $row["PREFECTURESCD"] = $rankFlg2 ? $row["PREFECTURESCD"] : $getCopy2["PREFECTURESCD"];
                            $row["PREF2"] = $rankFlg2 ? $row["PREF2"] : $getCopy2["PREF"];
                            if (!$row["PREF2"]) {
                                $row["UNREGISTERED_COLOR2"] = "pink";
                            }
                        }
                    }
                    $row["PREF2"] = $row["PREF2"] ? $row["PREF2"] : $setPref;
                }
                //軽減額表示
                $row["REDUCTIONMONEY_12"] = (is_numeric($row["REDUCTIONMONEY_1"])) ? number_format($row["REDUCTIONMONEY_1"]) : "";
                $row["REDUCTIONMONEY_22"] = (is_numeric($row["REDUCTIONMONEY_2"])) ? number_format($row["REDUCTIONMONEY_2"]) : "";
            }
            //軽減対象都道府県
            $extra = "id='PREFECTURESCD_".$row["SCHREGNO"]."' style=\"display:none\"";
            $row["PREFECTURESCD"] = knjCreateTextBox($objForm, $row["PREFECTURESCD"], "PREFECTURESCD[]", "", "", $extra);

            //軽減対象都道府県 [28：兵庫県]で[特殊フラグ]が[SH]の場合に使用
            knjCreateHidden($objForm, "PREF_CD_".$row["SCHREGNO"], $usePref);

            //記号
            $opt = array();
            $value_flg = false;
            $query = knjp170kQuery::selectQuerySpecailCode();
            $name_result = $db->query($query);
            $opt[] = array('label' => '',
                           'value' => 'DUMMY:0:DUMMY');
            while ($name_row = $name_result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $name_row["LABEL"],
                               'value' => $name_row["VALUE"]);
                list($cd, $flg, $dummy) = preg_split("/:/", $name_row["VALUE"]);
                if ($row["REDUC_RARE_CASE_CD"] == $cd) {
                    $row["REDUC_RARE_CASE_CD"] = $name_row["VALUE"];
                    $value_flg = true;
                }
            }
            $row["REDUC_RARE_CASE_CD"] = ($row["REDUC_RARE_CASE_CD"] && $value_flg) ? $row["REDUC_RARE_CASE_CD"] : $opt[0]["value"];
            $extra = " id='REDUC_RARE_CASE_CD_{$row["SCHREGNO"]}' onChange=\"kick_chgIncome(this,'{$row["SCHREGNO"]}')\"";
            $row["REDUC_RARE_CASE_CD"] = knjCreateCombo($objForm, "REDUC_RARE_CASE_CD[]", $row["REDUC_RARE_CASE_CD"], $opt, $extra, 1);

            /*****/
            /* 1 */
            /*****/
            //REDUCTION_SEQ_1
            $extra = "id='REDUCTION_SEQ_1_{$row["SCHREGNO"]}' style=\"display:none\"";
            $row["REDUCTION_SEQ_1"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ_1"], "REDUCTION_SEQ_1[]", "", "", $extra);

            $color = ($row["REDUC_DEC_FLG_1"] == 1)? "#999999":"#000000";

            //課税総取得額
            $onChange1 = "";
            $onChange2 = "";
            if ($rankFlg1) {
                $onChange2 = "onChange=\"chgIncome(this,'{$row["SCHREGNO"]}');setTotalmony('{$row["SCHREGNO"]}');\"";
            } else {
                $onChange1 = "onChange=\"chgIncome(this,'{$row["SCHREGNO"]}');setTotalmony('{$row["SCHREGNO"]}');\"";
            }
            $extra = "id='REDUC_INCOME_1_{$row["SCHREGNO"]}' style=\"text-align:right;color={$color}\" onblur=\"this.value = toInteger(this.value);\" {$onChange1} onFocus=\"focusText_1(this,'{$row["SCHREGNO"]}');\"";
            $row["REDUC_INCOME_1"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_1"], "REDUC_INCOME_1[]", "10", "8", $extra);

            //兄弟姉妹
            $extra = "{$readMoney} id='INCOME_SIBLINGS1_{$row["SCHREGNO"]}' style=\"text-align:right;color={$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this,'{$row["SCHREGNO"]}');setTotalmony('{$row["SCHREGNO"]}');\" ";
            $row["INCOME_SIBLINGS1"] = knjCreateTextBox($objForm, $row["INCOME_SIBLINGS1"], "INCOME_SIBLINGS1[]", "2", "2", $extra);

            //課税ランク
            $ristExtra = " readonly=\"readonly\" oncontextmenu=\"kirikae2(this, '{$row["SCHREGNO"]}');\" ";
            $extra = "id='REDUC_RANK_1_{$row["SCHREGNO"]}' style=\"text-align:right;color={$color}\" {$ristExtra} {$onChange2} onFocus=\"focusText_1(this,'{$row["SCHREGNO"]}');\"";
            $row["REDUC_RANK_1"] = knjCreateTextBox($objForm, $row["REDUC_RANK_1"], "REDUC_RANK_1[]", "2", "2", $extra);

            //checkboxと軽減額の設定
            if ($row["REDUC_DEC_FLG_1"] == 1) {
                $checked = "checked";
                $ReductionReadOnly = " readOnly ";
            } else {
                $checked = "";
                $ReductionReadOnly = "";
            }

            //課税額決定
            $style = (is_numeric($row["REDUCTIONMONEY_1"]) || is_numeric($row["REDUCTIONMONEY_2"])) ? "" : "style=\"display:none\"";
            $extra = "{$checked} id='REDUC_DEC_FLG_1_{$row["SCHREGNO"]}' onclick=\"chkFlg(this,'{$row["SCHREGNO"]}');\" {$style}";
            $row["REDUC_DEC_FLG_1"] = knjCreateCheckBox($objForm, "REDUC_DEC_FLG_1[]", $row["SCHREGNO"], $extra);

            //軽減額
            $extra = "id='REDUCTIONMONEY_1_{$row["SCHREGNO"]}' style=\"text-align:right; right;color={$color}; {$ReductionmoneyVisible} \" {$ReductionReadOnly} onblur=\"this.value = toInteger(this.value);\" onChange=\"setTotalmony('{$row["SCHREGNO"]}');\"";
            $row["REDUCTIONMONEY_1"] = knjCreateTextBox($objForm, $row["REDUCTIONMONEY_1"], "REDUCTIONMONEY_1[]", "8", "6", $extra);

            /*****/
            /* 2 */
            /*****/
            //REDUCTION_SEQ_2
            $extra = "id='REDUCTION_SEQ_2_{$row["SCHREGNO"]}' style=\"display:none\"";
            $row["REDUCTION_SEQ_2"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ_2"], "REDUCTION_SEQ_2[]", "", "", $extra);

            $color = ($row["REDUC_DEC_FLG_2"] == 1)? "#999999":"#000000";

            //課税総取得額
            $onChange1 = "";
            $onChange2 = "";
            if ($rankFlg2) {
                $onChange2 = "onChange=\"chgIncome(this,'{$row["SCHREGNO"]}');setTotalmony('{$row["SCHREGNO"]}');\"";
            } else {
                $onChange1 = "onChange=\"chgIncome(this,'{$row["SCHREGNO"]}');setTotalmony('{$row["SCHREGNO"]}');\"";
            }
            $extra = "id='REDUC_INCOME_2_{$row["SCHREGNO"]}' style=\"text-align:right;color={$color}\" onblur=\"this.value = toInteger(this.value);\" {$onChange1} onFocus=\"focusText_2(this,'{$row["SCHREGNO"]}');\"";
            $row["REDUC_INCOME_2"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_2"], "REDUC_INCOME_2[]", "10", "8", $extra);

            //兄弟姉妹
            $extra = "{$readMoney} id='INCOME_SIBLINGS2_{$row["SCHREGNO"]}' style=\"text-align:right;color={$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this,'{$row["SCHREGNO"]}');setTotalmony('{$row["SCHREGNO"]}');\" ";
            $row["INCOME_SIBLINGS2"] = knjCreateTextBox($objForm, $row["INCOME_SIBLINGS2"], "INCOME_SIBLINGS2[]", "2", "2", $extra);

            //課税ランク
            $ristExtra = " readonly=\"readonly\" oncontextmenu=\"kirikae2(this, '{$row["SCHREGNO"]}');\" ";
            $extra = "id='REDUC_RANK_2_{$row["SCHREGNO"]}' style=\"text-align:right;color={$color}\" {$ristExtra} {$onChange2} onFocus=\"focusText_2(this,'{$row["SCHREGNO"]}');\"";
            $row["REDUC_RANK_2"] = knjCreateTextBox($objForm, $row["REDUC_RANK_2"], "REDUC_RANK_2[]", "2", "2", $extra);

            //checkboxと軽減額の設定
            if ($row["REDUC_DEC_FLG_2"] == 1) {
                $checked = "checked";
                $ReductionReadOnly = " readOnly ";
            } else {
                $checked = "";
                $ReductionReadOnly = "";
            }

            //課税額決定
            $extra = "{$checked} id='REDUC_DEC_FLG_2_{$row["SCHREGNO"]}' onclick=\"chkFlg(this,'{$row["SCHREGNO"]}');\" {$style}";
            $row["REDUC_DEC_FLG_2"] = knjCreateCheckBox($objForm, "REDUC_DEC_FLG_2[]", $row["SCHREGNO"], $extra);

            //軽減額
            $extra = "id='REDUCTIONMONEY_2_{$row["SCHREGNO"]}' style=\"text-align:right; right;color={$color}; {$ReductionmoneyVisible} \" {$ReductionReadOnly} onblur=\"this.value = toInteger(this.value);\" onChange=\"setTotalmony('{$row["SCHREGNO"]}');\"";
            $row["REDUCTIONMONEY_2"] = knjCreateTextBox($objForm, $row["REDUCTIONMONEY_2"], "REDUCTIONMONEY_2[]", "8", "6", $extra);

            //備考
            $extra = "onblur=\"chkByte(this)\"";
            $row["REDUC_REMARK"] = knjCreateTextBox($objForm, $row["REDUC_REMARK"], "REDUC_REMARK[]", "21", "10", $extra);

            /* TipMessage */
            $setSp = "";
            $g = "";
            if (is_numeric($row["GRANTCD1"])) {
                $g = "<A href=\"#\" onMouseOver=\"getInfo('".$row["SCHREGNO"]."','G')\" onMouseOut=\"goOffInfo();\">{$row["GRANT_NAME1"]}</A>";
                $setSp = "　";
            }
            if (is_numeric($row["GRANTCD2"])) {
                $g .= $setSp."<A href=\"#\" onMouseOver=\"getInfo('".$row["SCHREGNO"]."','G')\" onMouseOut=\"goOffInfo();\">{$row["GRANT_NAME2"]}</A>";
                $setSp = "　";
            }
            if (is_numeric($row["GRANTCD3"])) {
                $g .= $setSp."<A href=\"#\" onMouseOver=\"getInfo('".$row["SCHREGNO"]."','G')\" onMouseOut=\"goOffInfo();\">{$row["GRANT_NAME3"]}</A>";
                $setSp = "　";
            }
            if (0 < $row["COUNTTRANSFER"]) {
                $t = "<A href=\"#\" onMouseOver=\"getInfo('".$row["SCHREGNO"]."','T')\" onMouseOut=\"goOffInfo();\">異</A>";
            } else {
                $t = "　";
            }

            $row["GRANT_TRANSFER_MARK"] = $g." ".$t;
            $row["TOTAL_MONEY"] = is_numeric($row["TOTAL_MONEY"]) ? number_format($row["TOTAL_MONEY"]) : "";
            $arg["data"][] = $row;
        }

        /**********/
        /* ボタン */
        /**********/
        //補助額再計算
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "補助額再計算", $extra);
        //更新
        $extra = "onclick=\"return btn_submit_update('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('cancel');\"";
        $arg["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_close"] = knjCreateBtn($objForm, "btn_close", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp170kForm1.html", $arg);
    }
}
?>
