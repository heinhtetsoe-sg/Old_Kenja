<?php

require_once('for_php7.php');

class knjp176kForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjp176kindex.php", "", "main");
        $db = Query::dbCheckOut();

        //対象年度
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $arg["HEISEI_1"] = common::DateConv1(CTRL_YEAR - 0 . '/04/01', 2); //平成○○年
        $arg["HEISEI_2"] = common::DateConv1(CTRL_YEAR - 1 . '/04/01', 2); //平成○○年

        //学年
        $result = $db->query(knjp176kQuery::selectQueryGrade($model));
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
        //hidden
        knjCreateHidden($objForm, "GRADE_SEND", $model->search["GRADE"]);

        $model->mstLastYear = $model->search["GRADE"] == "01" ? CTRL_YEAR : CTRL_YEAR - 1;

        //年組
        $extra = "onchange=\"btn_submit('mainclass');\"";
        $arg["HR_CLASS"] = knjCreateCombo($objForm, "HR_CLASS", $model->search["HR_CLASS"], $opt2, $extra, 1);
        //hidden
        knjCreateHidden($objForm, "HR_CLASS_SEND", $model->search["HR_CLASS"]);

        //radio
        $requestroot = REQUESTROOT;
        $opt = array(1, 2, 3);
        $model->changePrg = ($model->changePrg == "") ? "1" : $model->changePrg;
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

        /********************/
        /* チェックボックス */
        /********************/
        //相殺
        $extra = "onclick=\"checkAll(this)\"";
        $arg["OFFSET_ALL"] = knjCreateCheckBox($objForm, "OFFSET_ALL", "1", $extra);
        //軽減額 左
        $extra = "onclick=\"checkAll(this)\"";
        $arg["CHACKALL_1"] = knjCreateCheckBox($objForm, "CHACKALL_1", "1", $extra);
        //基準額 右
        $extra = "onclick=\"checkAll(this)\"";
        $arg["BASEALL_2"] = knjCreateCheckBox($objForm, "BASEALL_2", "1", $extra);
        //軽減額 右
        $extra = "onclick=\"checkAll(this)\"";
        $arg["CHACKALL_2"] = knjCreateCheckBox($objForm, "CHACKALL_2", "1", $extra);

        $optG216 = array();
        $value_flg = false;
        $query = knjp176kQuery::selectQuerySpecailCode();
        $name_result = $db->query($query);
        $optG216[] = array('label' => '',
                           'value' => 'DUMMY:0:DUMMY');
        while ($name_row = $name_result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optG216[] = array('label' => $name_row["LABEL"],
                               'value' => $name_row["VALUE"]);
        }


        $query = knjp176kQuery::selectQuery($model);
        $result = $db->query($query);
        $model->schregno = array();
        $model->schregnoPref = array();

        $dataCnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schregno[] = $row["SCHREGNO"];
            $model->schregnoPref[$row["SCHREGNO"]] = $row["PREF_CD"];
            @array_walk($row, "htmlspecialchars_array");

            //リンク設定
            $linkLabel = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
            $extra = "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availheight);";
            $subdata = "wopen('".REQUESTROOT."/P/KNJP172K/knjp172kindex.php?&cmd=edit&CALLID=KNJP176K&SCHREGNO={$row["SCHREGNO"]}".$extra;
            $row["HR_NAMEABBV"] = View::alink("#", htmlspecialchars($linkLabel),"onclick=\"$subdata\"");

            //相殺フラグ
            $checked = $row["OFFSET_FLG"] == "1" ? "checked=checked" : "";
            $extra = "{$checked} id='OFFSET_FLG{$row["SCHREGNO"]}')\"";
            $row["OFFSET_FLG"] = knjCreateCheckBox($objForm, "OFFSET_FLG[]", $row["SCHREGNO"], $extra);

            //合計額
            $totalMoney = 0;
            /*****/
            /* 1 */
            /*****/

            //特殊コード：入力可/不可の判定用
            list($dispCd1, $dispSetumei) = preg_split("/:/", $row["TEXT_DISP1"]);
            $extraTextNone1 = $dispCd1 == "1" ? "" : "display:none";

            list($dispCd2, $dispSetumei) = preg_split("/:/", $row["TEXT_DISP2"]);
            $extraTextNone2 = $dispCd2 == "1" ? "" : "display:none";

            //記号1
            $row["REDUC_RARE_CASE_CD_1"] = $row["REDUC_RARE_CASE_CD_1"] ? $row["REDUC_RARE_CASE_CD_1"] : $optG216[0]["value"];
            $extra = " id='REDUC_RARE_CASE_CD_1_{$row["SCHREGNO"]}' onChange=\"kick_chgIncome(this, '{$row["SCHREGNO"]}', '1', '{$dataCnt}')\"";
            $extra .= $row["ZENKI_CNT"] > 0 ? " onClick=\"alert('確定済みです。');\"" : "";
            $row["REDUC_RARE_CASE_CD_1"] = knjCreateCombo($objForm, "REDUC_RARE_CASE_CD_1[]", $row["REDUC_RARE_CASE_CD_1"], $optG216, $extra, 1);

            //REDUCTION_SEQ_1
            $extra = "id='REDUCTION_SEQ_1_{$row["SCHREGNO"]}' style=\"display:none\"";
            $row["REDUCTION_SEQ_1"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ_1"], "REDUCTION_SEQ_1[]", "", "", $extra);

            //基準額
            if (strlen($row["BASE_MONEY_1"]) > 0) {
                if ($dispCd1 == "1") {
                    $totalMoney += ($row["BASE_MONEY_1"]);
                }
                if ($dispCd1 != "1") {
                    $row["BASE_DISP_1"] = (is_numeric($row["BASE_MONEY_1"])) ? number_format($row["BASE_MONEY_1"]) : "";
                    $totalMoney += ($row["BASE_MONEY_1"] * 3);
                }
            } else {
                $row["BASE_DISP_1"] = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            }
            if (strlen($row["BASE_MONEY_2"]) > 0) {
                if ($dispCd2 == "1") {
                    $totalMoney += ($row["BASE_MONEY_2"]);
                }
                if ($dispCd2 != "1") {
                    $row["BASE_DISP_2"] = (is_numeric($row["BASE_MONEY_2"])) ? number_format($row["BASE_MONEY_2"]) : "";
                    $totalMoney += ($row["BASE_MONEY_2"] * 9);
                }
            } else {
                $row["BASE_DISP_2"] = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            }

            $color = ($row["ZENKI_CNT"] > 0)? "#999999":"#ffffff";
            $readOnly = $row["ZENKI_CNT"] > 0 ? " readOnly=\"readOnly\" " : "";
            $extra = "id='BASE_MONEY_1_{$row["SCHREGNO"]}' style=\"text-align:right; background-color:{$color}; {$extraTextNone1}\" onChange=\"setTotalmony('{$row["SCHREGNO"]}');\"";
            $row["BASE_MONEY_1"] = knjCreateTextBox($objForm, $row["BASE_MONEY_1"], "BASE_MONEY_1[]", "6", "6", $extra.$readOnly);

            $color = ($row["KOUKI_CNT"] > 0)? "#999999":"#ffffff";
            $readOnly = $row["KOUKI_CNT"] > 0 ? " readOnly=\"readOnly\" " : "";
            $extra = "id='BASE_MONEY_2_{$row["SCHREGNO"]}' style=\"text-align:right; background-color:{$color}; {$extraTextNone2}\" onChange=\"setTotalmony('{$row["SCHREGNO"]}');\"";
            $row["BASE_MONEY_2"] = knjCreateTextBox($objForm, $row["BASE_MONEY_2"], "BASE_MONEY_2[]", "6", "6", $extra.$readOnly);

            //課税総取得額
            if ($row["REDUC_ADD_FLG_1"] == 1) {
                $color = "#999999";
                $readMoney = " readOnly ";
            } else {
                $color = "#000000";
                $readMoney = "";
            }
            knjCreateHidden($objForm, "SET_REDUC_INCOME1_{$row["SCHREGNO"]}", $row["REDUC_INCOME_1"]);
            $extra = "{$readMoney} id='REDUC_INCOME_1_{$row["SCHREGNO"]}' style=\"text-align:right;color={$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this,'{$row["SCHREGNO"]}');setTotalmony('{$row["SCHREGNO"]}');\" ";
            $row["REDUC_INCOME_1"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_1"], "REDUC_INCOME_1[]", "10", "8", $extra);

            //兄弟姉妹
            $extra = "{$readMoney} id='INCOME_SIBLINGS1_{$row["SCHREGNO"]}' style=\"text-align:right;color={$color}\" ";
            $row["INCOME_SIBLINGS1"] = knjCreateTextBox($objForm, $row["INCOME_SIBLINGS1"], "INCOME_SIBLINGS1[]", "2", "2", $extra);

            //加算額表示1
            if ($dispCd1 == "1") {
                $totalMoney += ($row["REDUCTION_ADD_MONEY_1"]);
            }
            if ($dispCd1 != "1") {
                $totalMoney += ($row["REDUCTION_ADD_MONEY_1"] * 3);
                $row["REDUCTIONMONEY_DISP_1"] = (is_numeric($row["REDUCTION_ADD_MONEY_1"])) ? number_format($row["REDUCTION_ADD_MONEY_1"]) : "";
            }

            $color = ($row["ZENKI_ADD_CNT"] > 0)? "#999999":"#ffffff";
            $readOnly = $row["ZENKI_ADD_CNT"] > 0 ? " readOnly=\"readOnly\" " : "";
            $extra = "id='REDUCTION_ADD_MONEY_1_{$row["SCHREGNO"]}' style=\"text-align:right; background-color:{$color}; {$extraTextNone1} \" {$ReductionReadOnly} onblur=\"this.value = toInteger(this.value);\" onChange=\"setTotalmony('{$row["SCHREGNO"]}');\"";
            $row["REDUCTION_ADD_MONEY_1"] = knjCreateTextBox($objForm, $row["REDUCTION_ADD_MONEY_1"], "REDUCTION_ADD_MONEY_1[]", "6", "6", $extra.$readOnly);

            //基準額決定
            $checked = $row["REDUC_ADD_FLG_1"] == "1" ? "checked=checked" : "";
            $disabled = "";
            $extra = "{$checked} {$disabled} id='REDUC_ADD_FLG_1_{$row["SCHREGNO"]}' onclick=\"chkFlg(this,'{$row["SCHREGNO"]}')\"";
            $row["REDUC_ADD_FLG_1"] = knjCreateCheckBox($objForm, "REDUC_ADD_FLG_1[]", $row["SCHREGNO"], $extra);

            /*****/
            /* 2 */
            /*****/

            //記号2
            $row["REDUC_RARE_CASE_CD_2"] = $row["REDUC_RARE_CASE_CD_2"] ? $row["REDUC_RARE_CASE_CD_2"] : $optG216[0]["value"];
            $extra = " id='REDUC_RARE_CASE_CD_2_{$row["SCHREGNO"]}' onChange=\"kick_chgIncome(this, '{$row["SCHREGNO"]}', '2', '{$dataCnt}')\"";
            $extra .= $row["KOUKI_CNT"] > 0 ? " onClick=\"alert('確定済みです。');\"" : "";
            $row["REDUC_RARE_CASE_CD_2"] = knjCreateCombo($objForm, "REDUC_RARE_CASE_CD_2[]", $row["REDUC_RARE_CASE_CD_2"], $optG216, $extra, 1);

            //REDUCTION_SEQ_2
            $extra = "id='REDUCTION_SEQ_2_{$row["SCHREGNO"]}' style=\"display:none\"";
            $row["REDUCTION_SEQ_2"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ_2"], "REDUCTION_SEQ_2[]", "", "", $extra);

            //課税総取得額
            if ($row["REDUC_ADD_FLG_2"] == 1) {
                $color = "#999999";
                $readMoney = " readOnly ";
            } else {
                $color = "#000000";
                $readMoney = "";
            }
            knjCreateHidden($objForm, "SET_REDUC_INCOME2_{$row["SCHREGNO"]}", $row["REDUC_INCOME_2"]);
            $extra = "{$readMoney} id='REDUC_INCOME_2_{$row["SCHREGNO"]}' style=\"text-align:right;color={$color}\" onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this,'{$row["SCHREGNO"]}');setTotalmony('{$row["SCHREGNO"]}');\" ";
            $row["REDUC_INCOME_2"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_2"], "REDUC_INCOME_2[]", "10", "8", $extra);

            //兄弟姉妹
            $extra = "{$readMoney} id='INCOME_SIBLINGS2_{$row["SCHREGNO"]}' style=\"text-align:right;color={$color}\" onblur=\"this.value = toInteger(this.value);\" ";
            $row["INCOME_SIBLINGS2"] = knjCreateTextBox($objForm, $row["INCOME_SIBLINGS2"], "INCOME_SIBLINGS2[]", "2", "2", $extra);

            //加算額表示2
            if ($dispCd2 == "1") {
                $totalMoney += ($row["REDUCTION_ADD_MONEY_2"]);
            }
            if ($dispCd2 != "1") {
                $totalMoney += ($row["REDUCTION_ADD_MONEY_2"] * 9);
                $row["REDUCTIONMONEY_DISP_2"] = (is_numeric($row["REDUCTION_ADD_MONEY_2"])) ? number_format($row["REDUCTION_ADD_MONEY_2"]) : "";
            }
            $color = ($row["KOUKI_ADD_CNT"] > 0)? "#999999":"#ffffff";
            $readOnly = $row["KOUKI_ADD_CNT"] > 0 ? " readOnly=\"readOnly\" " : "";
            $extra = "id='REDUCTION_ADD_MONEY_2_{$row["SCHREGNO"]}' style=\"text-align:right; right; background-color:{$color}; {$extraTextNone2} \" {$ReductionReadOnly} onblur=\"this.value = toInteger(this.value);\" onChange=\"setTotalmony('{$row["SCHREGNO"]}');\"";
            $row["REDUCTION_ADD_MONEY_2"] = knjCreateTextBox($objForm, $row["REDUCTION_ADD_MONEY_2"], "REDUCTION_ADD_MONEY_2[]", "6", "6", $extra.$readOnly);

            //加算額決定
            $checked = $row["REDUC_ADD_FLG_2"] == "1" ? "checked=checked" : "";
            $disabled = "";
            $extra = "{$checked} {$disabled} id='REDUC_ADD_FLG_2_{$row["SCHREGNO"]}' onclick=\"chkFlg(this,'{$row["SCHREGNO"]}')\"";
            $row["REDUC_ADD_FLG_2"] = knjCreateCheckBox($objForm, "REDUC_ADD_FLG_2[]", $row["SCHREGNO"], $extra);

            //開始年月
            if ($row["BEGIN_YEARMONTH"] != "") $row["BEGIN_YEARMONTH"] = substr($row["BEGIN_YEARMONTH"], 0, 4) ."年" .substr($row["BEGIN_YEARMONTH"], 4) ."月";

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

            //備考
            $extra = "onblur=\"chkByte(this)\"";
            $row["REDUC_REMARK"] = knjCreateTextBox($objForm, $row["REDUC_REMARK"], "REDUC_REMARK[]", "21", "10", $extra);

            $row["TOTAL_MONEY"] = number_format($totalMoney);
            $arg["data"][] = $row;

            $dataCnt++;
        }

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
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
        View::toHTML($model, "knjp176kForm1.html", $arg);
    }
}
?>
