<?php

require_once('for_php7.php');

class knjm432wForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $db = Query::dbCheckOut();

        //TOP***************************************************************************************

        //年度
        $arg["TOP"]["YEAR"] = CTRL_YEAR;

        //通信制で学期コンボ表示あり（広工大は’1’をセットする。）
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
            $arg["useTsushinSemesKonboHyoji"] = $model->Properties["useTsushinSemesKonboHyoji"];
            $query = knjm432wQuery::getSemecmb();
            $extra = "onChange=\"btn_submit('change');\"";
            makeCmb($objForm, $arg, $db, $query, $model->semecmb, "SEMECMB", $extra, 1, "");
        }

        $arg["useKeizokuRisyuu"] = $model->Properties["useKeizokuRisyuu"];

        //出力順ラジオボタン(1:学籍番号順 2:クラス番号順)
        $opt = array(1, 2);
        $model->order = ($model->order == "") ? "1" : $model->order;
        $extra = array("id=\"ORDER1\""." onclick=\"btn_submit('change_order');\"", "id=\"ORDER2\""." onclick=\"btn_submit('change_order');\"");
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->order, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        //出力順が変わったら、科目（講座）リストはクリアされる
        if ($model->cmd == "change_order") {
            $model->sub = "";
            $model->subclasscd = "";
        }

        if ($model->Properties["knjm432wSelectChaircd"] == "1") {
            $arg["useSubclasscd"] = "1";
            //科目リスト
            $query = knjm432wQuery::getSubclassCd($model);
            $extra = "onChange=\"btn_submit('change');\"";
            makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1, "BLANK");
        } else {
            $arg["notUseSubclasscd"] = "1";
        }

        //科目(講座）リスト
        $query = knjm432wQuery::getChrSubCd($model);
        $extra = "onChange=\"btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->sub, "SELSUB", $extra, 1, "BLANK");

        //明細ヘッダデータ作成
        $model->testcdArray = makeHead($objForm, $arg, $db, $model);

        //集計項目
        $term_data = array();

        //データ配列
        $dataArray = array();
        $sch_array = array();
        $class_date = array();
        $name_date = array();
        $credit_date = array();
        $schooling_date = array();
        $nintei_array = array();
        $print_array = array();
        $model->schCredit = array();

        $hyoukaSeme = "9";
        if ($model->Properties["knjm432wZenkiKamokuUpdateSeme1"] == "1") {
            $result = $db->query(knjm432wQuery::getTest($model));
            $semes = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //学期名称
                if ($row["SEMESTER"] && !in_array($row["SEMESTER"], $semes)) {
                    $semes[] = $row["SEMESTER"];
                }
            }
            $result->free();

            if ($semes == array("1")) {
                $hyoukaSeme = "1";
            }
        }


        //成績データのリスト
        $query = knjm432wQuery::GetRecordDatdata($model, $hyoukaSeme);
        $result  = $db->query($query);

        //件数カウント用初期化
        $ca = 0;        //データ全件数
        while ($row_array = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ca++;

            //認定
//            $textName = $model->Properties["useHyoutei"] == "1" ? "HYOUKA_{$row_array["SCHREGNO"]}" : "HYOUTEI_{$row_array["SCHREGNO"]}";
//            $extra = "onClick=\"textDisabledChange(this, '{$textName}')\"";
            $extra = "";
            $nintei_array[$ca] = knjCreateCheckBox($objForm, "NINTEI_{$row_array["SCHREGNO"]}", "1", $extra);
            //学籍番号
            $sch_array[$ca] = $row_array["SCHREGNO"];
            //学年、組、番号
            $class_date[$ca] = $row_array["HR_NAME"] . "-" . $row_array["ATTENDNO"];
            //生徒氏名
            $name_date[$ca] = $row_array["NAME_SHOW"];
            //単位
            $credit_date[$ca] = $row_array["CREDITS"];
            $model->schCredit[$row_array["SCHREGNO"]] = $row_array["CREDITS"];
            //スクーリング
            $schooling_date[$ca] = $row_array["SCHOOLING_TIME"];

            //レポート
            for ($repCnt = 1; $repCnt <= $model->reportInfo["REP_SEQ_ALL"]; $repCnt++) {
                $no = $model->reportInfo["REP_START_SEQ"] - 1 + $repCnt;
                if ($model->Properties["useTsushinSemesKonboHyoji"] == "1" && !in_array($no, $model->repCntArray)) {
                    continue;
                }
                $dataArray["REPORT_ABBV".$no][$ca] = $row_array["REPORT_ABBV".$no];
            }

            //成績・集計項目
            foreach ($model->testcdArray as $key => $codeArray) {
                $col = "SCORE".$key;
                $dataArray[$col][$ca] = $row_array[$col];
                $dataArray["TSUISHI_FLG".$key][$ca] = $row_array["TSUISHI_FLG".$key];
                $dataArray["SEM_PASS_FLG".$key][$ca] = $row_array["SEM_PASS_FLG".$key];
                $dataArray["PASS_SCORE".$key][$ca] = $row_array["PASS_SCORE".$key];
                if (is_numeric($row_array[$col])) {
                    $term_data[$col][] = $row_array[$col];
                }
            }
            //評価
            //評定
            //修得単位
            //履修単位
            if ($model->inputCreditOnly == "1") {
                $dataArray["HYOUKA"][$ca] = $row_array["HYOUKA"];
                $dataArray["HYOUTEI"][$ca] = $row_array["HYOUTEI"];
                $extra = " onblur=\"this.value=toInteger(this.value);\"";
                $dataArray["GET_CREDIT"][$ca] = knjCreateTextBox($objForm, $row_array["GET_CREDIT"], "GET_CREDIT_{$row_array["SCHREGNO"]}", 3, 3, $extra);
                $dataArray["COMP_CREDIT"][$ca] = knjCreateTextBox($objForm, $row_array["COMP_CREDIT"], "COMP_CREDIT_{$row_array["SCHREGNO"]}", 3, 3, $extra);
            } elseif ($model->Properties["useHyoutei"] == "1") {
                $extra = " onblur=\"this.value=toInteger(this.value);\"";
                $dataArray["HYOUKA"][$ca] = knjCreateTextBox($objForm, $row_array["HYOUKA"], "HYOUKA_{$row_array["SCHREGNO"]}", 3, 3, $extra);
                $dataArray["HYOUTEI"][$ca] = $row_array["HYOUTEI"];
                $dataArray["GET_CREDIT"][$ca] = $row_array["GET_CREDIT"];
                $dataArray["COMP_CREDIT"][$ca] = $row_array["COMP_CREDIT"];
            } elseif ($model->Properties["useHyoukaHyoutei"] == "1") {
                $extra = " onblur=\"this.value=toInteger(this.value);\"";
                $dataArray["HYOUKA"][$ca] = knjCreateTextBox($objForm, $row_array["HYOUKA"], "HYOUKA_{$row_array["SCHREGNO"]}", 3, 3, $extra);
                $dataArray["HYOUTEI"][$ca] = knjCreateTextBox($objForm, $row_array["HYOUTEI"], "HYOUTEI_{$row_array["SCHREGNO"]}", 2, 2, $extra);
                $dataArray["GET_CREDIT"][$ca] = $row_array["GET_CREDIT"];
                $dataArray["COMP_CREDIT"][$ca] = $row_array["COMP_CREDIT"];
            } else {
                $dataArray["HYOUKA"][$ca] = $row_array["HYOUKA"];
                $extra = " onblur=\"this.value=toInteger(this.value);\"";
                $dataArray["HYOUTEI"][$ca] = knjCreateTextBox($objForm, $row_array["HYOUTEI"], "HYOUTEI_{$row_array["SCHREGNO"]}", 2, 2, $extra);
                $dataArray["GET_CREDIT"][$ca] = $row_array["GET_CREDIT"];
                $dataArray["COMP_CREDIT"][$ca] = $row_array["COMP_CREDIT"];
            }
            //継続履修
            $extra  = " id=\"COMP_CONTINUE\" ";
            $extra .= $row_array["COMP_CONTINUE"] == "1" ? " checked " : "";
            $dataArray["COMP_CONTINUE"][$ca] = knjCreateCheckBox($objForm, "COMP_CONTINUE_{$row_array["SCHREGNO"]}", "1", $extra);

            //印刷状態
            $dataArray["PRINT_STATUS"][$ca] = $row_array["PRINT_STATUS"];
            //印刷
            $extra = $row_array["GET_CREDIT"] >= 1 ? "" : " disabled ";
            $print_array[$ca] = knjCreateCheckBox($objForm, "PRINT_{$row_array["SCHREGNO"]}", "1", $extra);
        }
        $result->free();
        //成績レコードが存在する場合
        if ($ca != 0) {
            //ページの１行目のカレント行番号（配列指標）
            if ($model->line == "") {
                $currentline = 1;
            } else {
                if ($model->cmd == "change") {
                    $currentline = 1;
                } else {
                    $currentline = $model->line;
                }
            }


            //50件表示ループ
            $counts = 1;                            //カレントページ内での行数
            $pageline = 0;                          //このページの最後の行の全件数の中での行数
            $lineall = $currentline + 50;

            $colorFlg = false; //５行毎に背景色を変更

            for ($pageline=$currentline; $pageline<$lineall; $pageline++,$counts++) {
                //カレント行が全件数を超えたらループ終り
                if ($pageline > $ca) {
                    break;
                }

                $cnt50 = 0;
                $cnt90 = 0;

                //認定
                $row["NINTEI"] = $nintei_array[$pageline];

                //学籍番号
                $row["SCHREGNO"] = $sch_array[$pageline];
                knjCreateHidden($objForm, "SCHREGNO_".$counts, $sch_array[$pageline]);

                //クラス番号（学年－クラス－出席番号）
                $row["GRA_HR_ATTEND"] =  $class_date[$pageline];

                //生徒氏名
                $row["NAME_SHOW"] =  $name_date[$pageline];


                $meisai = "";
                //単位
                $meisai .= "<td width=50 align=\"center\" >".$credit_date[$pageline]."</td>";
                $cnt50++;
                //スクーリング
                $meisai .= "<td width=50 align=\"center\" >".$schooling_date[$pageline]."</td>";
                $cnt50++;

                //レポート
                for ($repCnt = 1; $repCnt <= $model->reportInfo["REP_SEQ_ALL"]; $repCnt++) {
                    $no = $model->reportInfo["REP_START_SEQ"] - 1 + $repCnt;
                    if ($model->Properties["useTsushinSemesKonboHyoji"] == "1" && !in_array($no, $model->repCntArray)) {
                        continue;
                    }
                    $meisai .= "<td width=50 align=\"center\" >".$dataArray["REPORT_ABBV".$no][$pageline]."</td>";
                    $cnt50++;
                }

                //成績
                foreach ($model->testcdArray as $key => $codeArray) {
                    $col = "SCORE".$key;
                    $meisai .= "<td width=90 align=\"center\" >".$dataArray[$col][$pageline]."</td>";
                    $cnt90++;
                    //合格点
                    knjCreateHidden($objForm, "PASS_SCORE".$key."_".$counts, $dataArray["PASS_SCORE".$key][$pageline]);
                }
                //評価
                $meisai .= "<td width=90 align=\"center\" >".$dataArray["HYOUKA"][$pageline]."</td>";
                $cnt90++;
                //評定
                $meisai .= "<td width=90 align=\"center\" >".$dataArray["HYOUTEI"][$pageline]."</td>";
                $cnt90++;
                //修得単位
                $meisai .= "<td width=90 align=\"center\" >".$dataArray["GET_CREDIT"][$pageline]."</td>";
                $cnt90++;
                //履修単位
                $meisai .= "<td width=90 align=\"center\" >".$dataArray["COMP_CREDIT"][$pageline]."</td>";
                $cnt90++;
                if ($model->Properties["useKeizokuRisyuu"] == "1") {
                    //継続履修
                    $meisai .= "<td width=90 align=\"center\" >".$dataArray["COMP_CONTINUE"][$pageline]."</td>";
                    $cnt90++;
                }
                //印刷状況
                $meisai .= "<td width=90 align=\"center\" >".$dataArray["PRINT_STATUS"][$pageline]."</td>";
                $cnt90++;
                //印刷
                $meisai .= "<td width=* align=\"center\" >".$print_array[$pageline]."</td>";
                $cnt90++;

                $allWidth = (55 * $cnt50) + (90 * $cnt90) + 40;
                $arg["ALL_WIDTH"] = $allWidth;

                $row["MEISAI"] = $meisai;

                //５行毎に背景色を変更
                if ($counts % 5 == 1) {
                    $colorFlg = !$colorFlg;
                }
                $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

                $arg["data"][] = $row;
            }
        } else {    //成績データが存在しない場合（最初に開いた時も）
            $currentline = 0;
        }

        //件数表示
        if ($ca == 0) {
            $arg["page_count"] = "0-0 / 0";
            $disabled = "disabled ";
        } else {
            $arg["page_count"] = $currentline . "-" . --$pageline . " / " . $ca;
            $disabled = "";
        }
        //ボタン作成
        $extra = $disabled ."onClick=\"btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        $extra = "onClick=\"btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //「前ページ」「次ページ」ボタンはレコード件数がゼロの時は表示しない
        if ($ca != 0) {
            if ($currentline != 1) {
                $extra = "onClick=\"btn_submit('pre');\"";
                $arg["button"]["btn_pre"] = knjCreateBtn($objForm, "btn_pre", "前ページ", $extra);
            }
            if ($pageline < $ca) {
                $extra = "onClick=\"btn_submit('next');\"";
                $arg["button"]["btn_next"] = knjCreateBtn($objForm, "btn_next", "次ページ", $extra);
            }
        }

        //通信制で学期コンボ表示あり（広工大は’1’をセットする。）
        if ($model->Properties["useTsushinSemesKonboHyoji"] !== "1") {
            //印刷ボタン
            $extra = "onclick=\"return btn_submit('updatePrint');\"";
            $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        }

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "line", $currentline);
        knjCreateHidden($objForm, "linecounts", --$counts);
        $arg["PARA"]["DBNAME"] = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        $arg["PARA"]["PRGID"] = knjCreateHidden($objForm, "PRGID", "KNJM432W");
        $arg["PARA"]["CTRL_YEAR"] = knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        $arg["PARA"]["CTRL_SEMESTER"] = knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        $arg["PARA"]["CTRL_DATE"] = knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        $arg["PARA"]["certifNoSyudou"] = knjCreateHidden($objForm, "certifNoSyudou", $model->Properties["certifNoSyudou"]);
        $arg["PARA"]["certif_no_8keta"] = knjCreateHidden($objForm, "certif_no_8keta", $model->Properties["certif_no_8keta"]);
        $arg["PARA"]["useSchool_KindField"] = knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        $arg["PARA"]["SCHOOLKIND"] = knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        $arg["PARA"]["useCurriculumcd"] = knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        $arg["PARA"]["printParam"] = knjCreateHidden($objForm, "printParam", $model->printPara);

        //DB切断
        Query::dbCheckIn($db);

        if ($model->cmd == "updPrint") {
            $model->cmd = 'updPrint2';
        }

        if ($model->cmd == "updPrint2") {
            $arg["printOut"] = "newwin('" . SERVLET_URL . "');";
        }

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm432windex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm432wForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, &$model)
{
    $model->repCntArray = array();
    if ($model->Properties["useTsushinSemesKonboHyoji"] == "1") {
        $result = $db->query(knjm432wQuery::getReportCnt($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->repCntArray[] = $row["STANDARD_SEQ"];
        }
        $result->free();
    }

    $query = knjm432wQuery::getReport($model);
    $model->reportInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
    $repHead = "";
    $repCol = 1;
    $repKaisu = "";
    for ($repCnt = 1; $repCnt <= $model->reportInfo["REP_SEQ_ALL"]; $repCnt++) {
        $no = $model->reportInfo["REP_START_SEQ"] - 1 + $repCnt;
        if ($model->Properties["useTsushinSemesKonboHyoji"] == "1" && !in_array($no, $model->repCntArray)) {
            continue;
        }
        $repHead = "<th colspan={$repCol} >レポート</th> ";
        $repCol++;
        $repKaisu .= "<th width=50 >{$no}</th> ";
    }

    $sem = "";
    $semCol = 0;
    $semArray = array();
    $head1 = "";
    $testcdArray = array();
    $head2 = "";
    if ($model->sub != "") {
        $result = $db->query(knjm432wQuery::getTest($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学期名称
            if ($sem != $row["SEMESTER"]) {
                $sem = $row["SEMESTER"];
                $semCol = 0;
            }
            $semCol++;
            $semArray[$row["SEMESTER"]] = "<th colspan={$semCol} >".$row["SEMESTERNAME"]."</th> ";
            //考査種別名称
            $testcdArray[] = array("TESTCD" => $row["TESTCD"], "TESTITEMABBV1" => $row["TESTITEMABBV1"], "SEMESTER" => $row["SEMESTER"], "SEMESTERNAME" => $row["SEMESTERNAME"]);
            $head2 .= "<th width=90 >".$row["TESTITEMABBV1"]."</th> ";
        }
        $result->free();
    }
    foreach ($semArray as $key => $val) {
        $head1 .= $val;
    }
    $arg["HEAD1"] = $repHead.$head1; //学期名称
    $arg["HEAD2"] = $repKaisu.$head2; //考査種別名称
    $arg["FOOT_COLSPAN"] = 1 + count($testcdArray);
    return $testcdArray;
}
