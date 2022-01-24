<?php

require_once('for_php7.php');

class knjp177kForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjp177kindex.php", "", "main");
        $db = Query::dbCheckOut();

        //対象年度
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $arg["HEISEI_1"] = common::DateConv1(CTRL_YEAR - 0 . '/04/01', 2); //平成○○年
        $arg["HEISEI_2"] = common::DateConv1(CTRL_YEAR - 1 . '/04/01', 2); //平成○○年

        //学年
        $result = $db->query(knjp177kQuery::selectQueryGrade($model));
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
        $model->changePrg = ($model->changePrg == "") ? "3" : $model->changePrg;
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
        $result = $db->query(knjp177kQuery::selectQueryPref($model));
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

        $optG216 = array();
        $value_flg = false;
        $query = knjp177kQuery::selectQuerySpecailCode();
        $name_result = $db->query($query);
        $optG216[] = array('label' => '',
                           'value' => 'DUMMY:0:DUMMY');
        while ($name_row = $name_result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optG216[$name_row["VALUE"]] = $name_row["LABEL"];
        }

        $query = knjp177kQuery::selectQuery($model);
        $result = $db->query($query);
        $model->schregno = array();

        $dataCnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schregno[] = $row["SCHREGNO"];
            @array_walk($row, "htmlspecialchars_array");

            //リンク設定
            $linkLabel = $row["HR_NAMEABBV"]."-".$row["ATTENDNO"];
            $extra = "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availheight);";
            $subdata = "wopen('".REQUESTROOT."/P/KNJP172K/knjp172kindex.php?&cmd=edit&CALLID=KNJP177K&SCHREGNO={$row["SCHREGNO"]}".$extra;
            $row["HR_NAMEABBV"] = View::alink("#", htmlspecialchars($linkLabel),"onclick=\"$subdata\"");

            //相殺フラグ
            $row["OFFSET_FLG"] = $row["OFFSET_FLG"] == "1" ? "レ" : "";

            //合計額
            $totalMoney = 0;
            $totalMoney170 = 0;
            $totalMoney176 = 0;
            /*****/
            /* 1 */
            /*****/

            //特殊コード：入力可/不可の判定用
            list($dispCd1, $dispSetumei) = preg_split("/:/", $row["TEXT_DISP1"]);
            $extraTextNone1 = $dispCd1 == "1" ? "" : "display:none";

            list($dispCd2, $dispSetumei) = preg_split("/:/", $row["TEXT_DISP2"]);
            $extraTextNone2 = $dispCd2 == "1" ? "" : "display:none";

            //記号1
            $row["REDUC_RARE_CASE_CD_1"] = $optG216[$row["REDUC_RARE_CASE_CD_1"]];

            //REDUCTION_SEQ_176_1
            $extra = "id='REDUCTION_SEQ_176_1_{$row["SCHREGNO"]}' style=\"display:none\"";
            $row["REDUCTION_SEQ_176_1"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ_176_1"], "REDUCTION_SEQ_176_1[]", "", "", $extra);

            //基準額
            if (strlen($row["BASE_MONEY_1"]) > 0) {
                if ($dispCd1 == "1") {
                    $totalMoney176 += ($row["BASE_MONEY_1"]);
                }
                if ($dispCd1 != "1") {
                    $totalMoney176 += ($row["BASE_MONEY_1"] * 3);
                }
            }
            if (strlen($row["BASE_MONEY_2"]) > 0) {
                if ($dispCd2 == "1") {
                    $totalMoney176 += ($row["BASE_MONEY_2"]);
                }
                if ($dispCd2 != "1") {
                    $totalMoney176 += ($row["BASE_MONEY_2"] * 9);
                }
            }
            //基準額１
            $row["BASE_MONEY_1"] = strlen($row["BASE_MONEY_1"]) > 0 ? number_format($row["BASE_MONEY_1"]) : "";
            //基準額２
            $row["BASE_MONEY_2"] = strlen($row["BASE_MONEY_2"]) > 0 ? number_format($row["BASE_MONEY_2"]) : "";

            //課税総取得額
            if ($row["REDUC_INCOME_1"] !== "0") {
                $row["REDUC_INCOME_1"] = $row["REDUC_INCOME_1"] ? number_format($row["REDUC_INCOME_1"]) : "";
            }

            //加算額表示1
            if ($dispCd1 == "1") {
                $totalMoney176 += ($row["REDUCTION_ADD_MONEY_1"]);
            }
            if ($dispCd1 != "1") {
                $totalMoney176 += ($row["REDUCTION_ADD_MONEY_1"] * 3);
            }

            $row["REDUCTION_ADD_MONEY_1"] = is_numeric($row["REDUCTION_ADD_MONEY_1"]) ? number_format($row["REDUCTION_ADD_MONEY_1"]) : "";

            //基準額決定
            $row["REDUC_ADD_FLG_1"] = $row["REDUC_ADD_FLG_1"] == "1" ? "レ" : "&nbsp;&nbsp;";

            /*****/
            /* 2 */
            /*****/

            //記号2
            $row["REDUC_RARE_CASE_CD_2"] = $optG216[$row["REDUC_RARE_CASE_CD_2"]];

            //REDUCTION_SEQ_176_2
            $extra = "id='REDUCTION_SEQ_176_2_{$row["SCHREGNO"]}' style=\"display:none\"";
            $row["REDUCTION_SEQ_176_2"] = knjCreateTextBox($objForm, $row["REDUCTION_SEQ_176_2"], "REDUCTION_SEQ_176_2[]", "", "", $extra);

            //課税総取得額
            $row["REDUC_INCOME_2"] = $row["REDUC_INCOME_2"] ? number_format($row["REDUC_INCOME_2"]) : "";

            //加算額表示2
            if ($dispCd2 == "1") {
                $totalMoney176 += ($row["REDUCTION_ADD_MONEY_2"]);
            }
            if ($dispCd2 != "1") {
                $totalMoney176 += ($row["REDUCTION_ADD_MONEY_2"] * 9);
            }
            $row["REDUCTION_ADD_MONEY_2"] = is_numeric($row["REDUCTION_ADD_MONEY_2"]) ? number_format($row["REDUCTION_ADD_MONEY_2"]) : "";

            //加算額決定
            $row["REDUC_ADD_FLG_2"] = $row["REDUC_ADD_FLG_2"] == "1" ? "レ" : "&nbsp;&nbsp;";

            $row["REDUCTIONMONEY_12"] = (is_numeric($row["REDUCTIONMONEY_1_170"])) ? number_format($row["REDUCTIONMONEY_1_170"]) : "";
            $row["REDUCTIONMONEY_22"] = (is_numeric($row["REDUCTIONMONEY_2_170"])) ? number_format($row["REDUCTIONMONEY_2_170"]) : "";

            $row["REDUC_DEC_FLG_1_170"] = $row["REDUC_DEC_FLG_1_170"] == "1" ? "レ" : "&nbsp;&nbsp;";
            $row["REDUC_DEC_FLG_2_170"] = $row["REDUC_DEC_FLG_2_170"] == "1" ? "レ" : "&nbsp;&nbsp;";

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
                $t = "<A href=\"#\" onMouseOver=\"getInfo176('".$row["SCHREGNO"]."','T')\" onMouseOut=\"goOffInfo176();\">異</A>";
            } else {
                $t = "　";
            }
            $row["GRANT_TRANSFER_MARK"] = $g." ".$t;

            //備考
            $row["REDUC_REMARK"] = $row["REDUC_REMARK"];

            $row["TOTAL_MONEY176"] = number_format($totalMoney176);
            $totalMoney170 += $row["TOTAL_MONEY_170"];
            $row["TOTAL_MONEY170"] = number_format($totalMoney170);
            $totalMoney = $totalMoney176 + $totalMoney170;
            $row["TOTAL_MONEY"] = number_format($totalMoney);
            $arg["data"][] = $row;

            $dataCnt++;
        }

        /**********/
        /* ボタン */
        /**********/
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_close"] = knjCreateBtn($objForm, "btn_close", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp177kForm1.html", $arg);
    }
}
?>
