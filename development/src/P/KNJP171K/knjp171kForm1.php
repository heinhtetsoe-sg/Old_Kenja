<?php

require_once('for_php7.php');
class knjp171kForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjp171kindex.php", "", "main");
        $db = Query::dbCheckOut();

        //対象年度
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $arg["HEISEI_1"] = common::DateConv1(CTRL_YEAR - 0 . '/04/01', 2); //平成○○年
        $arg["HEISEI_2"] = common::DateConv1(CTRL_YEAR - 1 . '/04/01', 2); //平成○○年

        //学年
        $result = $db->query(knjp171kQuery::selectQueryGrade($model));
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

        /********************/
        /* チェックボックス */
        /********************/
        //相殺
        $extra = "onclick=\"checkAll(this)\"";
        $arg["OFFSET_ALL"] = knjCreateCheckBox($objForm, "OFFSET_ALL", "1", $extra);
        //基準額 左
        $extra = "onclick=\"checkAll(this)\"";
        $arg["BASEALL_1"] = knjCreateCheckBox($objForm, "BASEALL_1", "1", $extra);
        //軽減額 左
        $extra = "onclick=\"checkAll(this)\"";
        $arg["CHACKALL_1"] = knjCreateCheckBox($objForm, "CHACKALL_1", "1", $extra);
        //基準額 右
        $extra = "onclick=\"checkAll(this)\"";
        $arg["BASEALL_2"] = knjCreateCheckBox($objForm, "BASEALL_2", "1", $extra);
        //軽減額 右
        $extra = "onclick=\"checkAll(this)\"";
        $arg["CHACKALL_2"] = knjCreateCheckBox($objForm, "CHACKALL_2", "1", $extra);

        $query = knjp171kQuery::selectQuery($model);
        $result = $db->query($query);
        $model->schregno = array();

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schregno[] = $row["SCHREGNO"];
            @array_walk($row, "htmlspecialchars_array");

            //リンク設定
            $linkLabel = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
            $extra = "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availheight);";
            $subdata = "wopen('".REQUESTROOT."/P/KNJP172K/knjp172kindex.php?&cmd=edit&CALLID=KNJP171K&SCHREGNO={$row["SCHREGNO"]}".$extra;
            $row["HR_NAMEABBV"] = View::alink("#", htmlspecialchars($linkLabel),"onclick=\"$subdata\"");

            //相殺フラグ
            $checked = $row["OFFSET_FLG"] == "1" ? "checked=checked" : "";
            $extra = "{$checked} id='OFFSET_FLG{$row["SCHREGNO"]}')\"";
            $row["OFFSET_FLG"] = knjCreateCheckBox($objForm, "OFFSET_FLG[]", $row["SCHREGNO"], $extra);

            //PASSNO
            $row["PASSNO_DISP"] = $row["PASSNO"];
            $row["PASSNO"] = knjCreateHidden($objForm, "PASSNO[]", $row["PASSNO"]);

            //合計額
            $totalMoney = 0;
            /*****/
            /* 1 */
            /*****/
            //REDUCTION_SEQ_1
            $extra = "id='REDUCTION_SEQ_1_{$row["SCHREGNO"]}' style=\"display:none\"";
            $row["REDUCTION_SEQ_1"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ_1"], "REDUCTION_SEQ_1[]", "", "", $extra);

            //基準額
            if ($row["PASSNO_DISP"]) {
                $row["BASE_DISP_1"] = (is_numeric($row["BASE_MONEY_1"])) ? number_format($row["BASE_MONEY_1"]) : "";
                $totalMoney += $row["REDUC_DEC_FLG_1"] == "1" ? ($row["BASE_MONEY_1"] * 12) : 0;
            } else {
                $row["BASE_DISP_1"] = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
            }

            $extra = "id='BASE_MONEY_1_{$row["SCHREGNO"]}' style=\"display:none\"";
            $row["BASE_MONEY_1"] = knjCreateTextBox($objForm, $row["BASE_MONEY_1"], "BASE_MONEY_1[]", "", "", $extra);

            //課税総取得額
            knjCreateHidden($objForm, "SET_REDUC_INCOME1_{$row["SCHREGNO"]}", $row["REDUC_INCOME_1"]);
            $disableMoney = $row["REDUC_ADD_FLG_1"] == "1" ? "disabled" : "";
            $extra = "{$disableMoney} id='REDUC_INCOME_1_{$row["SCHREGNO"]}' style=\"text-align:right\" onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this,'{$row["SCHREGNO"]}');setTotalmony('{$row["SCHREGNO"]}');\" ";
            $row["REDUC_INCOME_1"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_1"], "REDUC_INCOME_1[]", "10", "8", $extra);

            //基準額決定
            $checked = $row["REDUC_DEC_FLG_1"] == "1" ? "checked=checked" : "";
            $disabled = $row["CNT"] > 0 ? "disabled=disabled" : "";
            $extra = "{$checked} {$disabled} id='REDUC_DEC_FLG_1_{$row["SCHREGNO"]}' onclick=\"chkFlg(this,'{$row["SCHREGNO"]}')\"";
            $row["REDUC_DEC_FLG_1"] = knjCreateCheckBox($objForm, "REDUC_DEC_FLG_1[]", $row["SCHREGNO"], $extra);

            //支援額表示
            $row["REDUCTIONMONEY_DISP_1"] = (is_numeric($row["REDUCTION_ADD_MONEY_1"])) ? number_format($row["REDUCTION_ADD_MONEY_1"]) : "";
            $totalMoney += $row["REDUC_ADD_FLG_1"] == "1" ? ($row["REDUCTION_ADD_MONEY_1"] * 3) : 0;
            //軽減額
            $extra = "id='REDUCTION_ADD_MONEY_1_{$row["SCHREGNO"]}' style=\"text-align:right; right;color={$color}; display:none \" {$ReductionReadOnly} onblur=\"this.value = toInteger(this.value);\" onChange=\"setTotalmony('{$row["SCHREGNO"]}');\"";
            $row["REDUCTION_ADD_MONEY_1"] = knjCreateTextBox($objForm, $row["REDUCTION_ADD_MONEY_1"], "REDUCTION_ADD_MONEY_1[]", "8", "6", $extra);

            //基準額決定
            $checked = $row["REDUC_ADD_FLG_1"] == "1" ? "checked=checked" : "";
            $disabled = $row["ZENKI_CNT"] > 0 ? "disabled=disabled" : "";
            $extra = "{$checked} {$disabled} id='REDUC_ADD_FLG_1_{$row["SCHREGNO"]}' onclick=\"chkFlg(this,'{$row["SCHREGNO"]}')\"";
            $row["REDUC_ADD_FLG_1"] = knjCreateCheckBox($objForm, "REDUC_ADD_FLG_1[]", $row["SCHREGNO"], $extra);

            /*****/
            /* 2 */
            /*****/
            //REDUCTION_SEQ_1
            $extra = "id='REDUCTION_SEQ_2_{$row["SCHREGNO"]}' style=\"display:none\"";
            $row["REDUCTION_SEQ_2"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ_2"], "REDUCTION_SEQ_2[]", "", "", $extra);

            //課税総取得額
            knjCreateHidden($objForm, "SET_REDUC_INCOME2_{$row["SCHREGNO"]}", $row["REDUC_INCOME_2"]);
            $disableMoney = $row["REDUC_ADD_FLG_2"] == "1" ? "disabled" : "";
            $extra = "{$disableMoney} id='REDUC_INCOME_2_{$row["SCHREGNO"]}' style=\"text-align:right\" onblur=\"this.value = toInteger(this.value);\" onChange=\"chgIncome(this,'{$row["SCHREGNO"]}');setTotalmony('{$row["SCHREGNO"]}');\" ";
            $row["REDUC_INCOME_2"] = knjCreateTextBox($objForm, $row["REDUC_INCOME_2"], "REDUC_INCOME_2[]", "10", "8", $extra);

            //加算額表示
            $row["REDUCTIONMONEY_DISP_2"] = (is_numeric($row["REDUCTION_ADD_MONEY_2"])) ? number_format($row["REDUCTION_ADD_MONEY_2"]) : "";
            $totalMoney += $row["REDUC_ADD_FLG_2"] == "1" ? ($row["REDUCTION_ADD_MONEY_2"] * 9) : 0;
            //加算額
            $extra = "id='REDUCTION_ADD_MONEY_2_{$row["SCHREGNO"]}' style=\"text-align:right; right;color={$color}; display:none \" {$ReductionReadOnly} onblur=\"this.value = toInteger(this.value);\" onChange=\"setTotalmony('{$row["SCHREGNO"]}');\"";
            $row["REDUCTION_ADD_MONEY_2"] = knjCreateTextBox($objForm, $row["REDUCTION_ADD_MONEY_2"], "REDUCTION_ADD_MONEY_2[]", "8", "6", $extra);

            //加算額決定
            $checked = $row["REDUC_ADD_FLG_2"] == "1" ? "checked=checked" : "";
            $disabled = $row["KOUKI_CNT"] > 0 ? "disabled=disabled" : "";
            $extra = "{$checked} {$disabled} id='REDUC_ADD_FLG_2_{$row["SCHREGNO"]}' onclick=\"chkFlg(this,'{$row["SCHREGNO"]}')\"";
            $row["REDUC_ADD_FLG_2"] = knjCreateCheckBox($objForm, "REDUC_ADD_FLG_2[]", $row["SCHREGNO"], $extra);

            //開始年月
            if ($row["BEGIN_YEARMONTH"] != "") $row["BEGIN_YEARMONTH"] = substr($row["BEGIN_YEARMONTH"], 0, 4) ."年" .substr($row["BEGIN_YEARMONTH"], 4) ."月";

            /* TipMessage */
            if (is_numeric($row["GRANTCD"])) {
                $g = "<A href=\"#\" onMouseOver=\"getInfo('".$row["SCHREGNO"]."','G')\" onMouseOut=\"goOffInfo();\">奨</A>";
            } else {
                $g = "　";
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
        View::toHTML($model, "knjp171kForm1.html", $arg);
    }
}
?>
