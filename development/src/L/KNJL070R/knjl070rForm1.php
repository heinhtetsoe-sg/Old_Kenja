<?php

require_once('for_php7.php');

class knjl070rForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjl070rindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ--------------------------------------------

        //入試年度
        $arg["CTRL_YEAR"] = $model->year ."年度";

        //入試制度コンボ
        $query = knjl070rQuery::getName($model->year, "L003", $model->fixApplicantDiv);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        //入試区分コンボ
        $query = knjl070rQuery::getName($model->year, "L004");
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "");

        //データ1-------------------------------------------

        //志望区分マスタから志望順位・志望コースを取得し配列にセット
        $model->desiredivArray = array();
        $result = $db->query(knjl070rQuery::getDesirediv($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->desiredivArray[$row["DESIREDIV"]][$row["WISHNO"]] = array("EXAMCOURSE" => $row["EXAMCOURSE"], "EXAMCOURSE_MARK" => $row["EXAMCOURSE_MARK"]);
        }

        //事前相談内諾区分ラジオ 1:あり 2:なし
        $optShdiv = array(1, 2);
        $model->shdiv = ($model->shdiv == "") ? "1" : $model->shdiv;
        $extra = array("id=\"SHDIV1\" onclick=\"return btn_submit('main')\"", "id=\"SHDIV2\" onclick=\"return btn_submit('main')\"");
        $radioArray = knjCreateRadio($objForm, "SHDIV", $model->shdiv, $extra, $optShdiv, get_count($optShdiv));
        foreach($radioArray as $key => $val) $arg[$key] = $val;
        //ラベル(SS,S)
        $query = knjl070rQuery::getName($model->year, "L004", $model->testdiv);
        $testdivRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["SHDIV_LABEL"] = $testdivRow["ABBV3"];

        //受験区分コンボ
        $optDesirediv = array();
        $optDesirediv2 = array(); //確定結果一覧にて使用
        $optCourse = array(); //確定結果一覧にて使用
        $hoketuFlg2 = false; //確定結果一覧にて使用
        foreach ($model->desiredivArray as $desirediv => $wishnoArray) {
            $markNaidaku = "";
            $markJuken = "";
            $seq = "";
            $cntGSGA2 = 0;
            foreach ($wishnoArray as $wishno => $examcourseArray) {
                $markNaidaku = $examcourseArray["EXAMCOURSE_MARK"];
                $markJuken .= $seq . $examcourseArray["EXAMCOURSE_MARK"];
                $seq = "・";

                $optCourse[$examcourseArray["EXAMCOURSE"]] = $examcourseArray["EXAMCOURSE_MARK"];

                if ($examcourseArray["EXAMCOURSE_MARK"] == "ＧＳ" || $examcourseArray["EXAMCOURSE_MARK"] == "ＧＡ") {
                    $cntGSGA2++; //ＧＳとＧＡがあれば、2
                }
            }
            $mark  = ($model->shdiv == "1") ? $markNaidaku . "内諾" : "";
            $mark .= $markJuken . "受験";
            $optDesirediv[] = array("label" => $desirediv . ":" . $mark, "value" => $desirediv);
            $optDesirediv2[] = array("value" => $desirediv, "markJuken" => $markJuken . "受験", "markNaidaku" => $markNaidaku . "内諾");

            if ($cntGSGA2 == 2) $hoketuFlg2 = true;
        }
        $model->desirediv = ($model->desirediv == "") ? "1" : $model->desirediv;
        $extra = "onchange=\"return btn_submit('main')\"";
        $arg["DESIREDIV"] = knjCreateCombo($objForm, "DESIREDIV", $model->desirediv, $optDesirediv, $extra, 1);

        //データ2-------------------------------------------

        //ＧＳとＧＡがあれば、ＧＳ補欠を表示
        $cntGSGA = 0;
        foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
            if ($examcourseArray["EXAMCOURSE_MARK"] == "ＧＳ" || $examcourseArray["EXAMCOURSE_MARK"] == "ＧＡ") {
                $cntGSGA++; //ＧＳとＧＡがあれば、2
            }
        }
        $model->hoketuFlg = ($cntGSGA == 2) ? true : false;

        //ＧＳ補欠の記号とコード
        $model->hoketuMark = "ＧＳ補欠";
        $model->hoketuCourse = '99999999';

        if ($model->cmd == "main" || isset($model->warning)) {
            foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
                $course = $examcourseArray["EXAMCOURSE"];

                $Row["BORDER_SCORE-".$course] = $model->field["BORDER_SCORE-".$course];
                $Row["SUCCESS_CNT-" .$course] = (!strlen($model->field["SUCCESS_CNT-".$course])) ? 0 : $model->field["SUCCESS_CNT-".$course];
                $Row["SUCCESS_CNT_SPECIAL-" .$course] = (!strlen($model->field["SUCCESS_CNT_SPECIAL-".$course])) ? 0 : $model->field["SUCCESS_CNT_SPECIAL-".$course];
                $Row["SUCCESS_CNT_SPECIAL2-" .$course] = (!strlen($model->field["SUCCESS_CNT_SPECIAL2-".$course])) ? 0 : $model->field["SUCCESS_CNT_SPECIAL2-".$course];

                if ($model->hoketuFlg && $examcourseArray["EXAMCOURSE_MARK"] == "ＧＡ") {
                    $course = $model->hoketuCourse;

                    $Row["BORDER_SCORE-".$course] = $model->field["BORDER_SCORE-".$course];
                    $Row["SUCCESS_CNT-" .$course] = (!strlen($model->field["SUCCESS_CNT-".$course])) ? 0 : $model->field["SUCCESS_CNT-".$course];
                    $Row["SUCCESS_CNT_SPECIAL-" .$course] = (!strlen($model->field["SUCCESS_CNT_SPECIAL-".$course])) ? 0 : $model->field["SUCCESS_CNT_SPECIAL-".$course];
                    $Row["SUCCESS_CNT_SPECIAL2-" .$course] = (!strlen($model->field["SUCCESS_CNT_SPECIAL2-".$course])) ? 0 : $model->field["SUCCESS_CNT_SPECIAL2-".$course];
                }
            }
        } else if ($model->cmd == "simShow") {
            //合格者取得(シミュレーション結果表示)
            foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
                $course = $examcourseArray["EXAMCOURSE"];

                $query = knjl070rQuery::selectQuerySuccess_cnt($model, $wishno);
                $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

                $Row["BORDER_SCORE-".$course] = $model->field["BORDER_SCORE-".$course];
                $Row["SUCCESS_CNT-" .$course] = (!strlen($passingRow["SUCCESS_CNT"])) ? 0 : $passingRow["SUCCESS_CNT"];
                $Row["SUCCESS_CNT_SPECIAL-" .$course] = (!strlen($passingRow["SUCCESS_CNT_SPECIAL"])) ? 0 : $passingRow["SUCCESS_CNT_SPECIAL"];
                $Row["SUCCESS_CNT_SPECIAL2-" .$course] = (!strlen($passingRow["SUCCESS_CNT_SPECIAL2"])) ? 0 : $passingRow["SUCCESS_CNT_SPECIAL2"];

                if ($model->hoketuFlg && $examcourseArray["EXAMCOURSE_MARK"] == "ＧＡ") {
                    $course = $model->hoketuCourse;

                    $query = knjl070rQuery::selectQuerySuccess_cnt($model, $wishno, "8");
                    $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

                    $Row["BORDER_SCORE-".$course] = $model->field["BORDER_SCORE-".$course];
                    $Row["SUCCESS_CNT-" .$course] = (!strlen($passingRow["SUCCESS_CNT"])) ? 0 : $passingRow["SUCCESS_CNT"];
                    $Row["SUCCESS_CNT_SPECIAL-" .$course] = (!strlen($passingRow["SUCCESS_CNT_SPECIAL"])) ? 0 : $passingRow["SUCCESS_CNT_SPECIAL"];
                    $Row["SUCCESS_CNT_SPECIAL2-" .$course] = (!strlen($passingRow["SUCCESS_CNT_SPECIAL2"])) ? 0 : $passingRow["SUCCESS_CNT_SPECIAL2"];
                }
            }
        } else {
            //合格点マスタ
            foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
                $course = $examcourseArray["EXAMCOURSE"];

                $query = knjl070rQuery::selectQueryPassingmark($model, $course);
                $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

                $Row["BORDER_SCORE-".$course] = $passingRow["BORDER_SCORE"];
                $Row["SUCCESS_CNT-" .$course] = (!strlen($passingRow["SUCCESS_CNT"])) ? 0 : $passingRow["SUCCESS_CNT"];
                $Row["SUCCESS_CNT_SPECIAL-" .$course] = (!strlen($passingRow["SUCCESS_CNT_SPECIAL"])) ? 0 : $passingRow["SUCCESS_CNT_SPECIAL"];
                $Row["SUCCESS_CNT_SPECIAL2-" .$course] = (!strlen($passingRow["SUCCESS_CNT_SPECIAL2"])) ? 0 : $passingRow["SUCCESS_CNT_SPECIAL2"];

                if ($model->hoketuFlg && $examcourseArray["EXAMCOURSE_MARK"] == "ＧＡ") {
                    $course = $model->hoketuCourse;

                    $query = knjl070rQuery::selectQueryPassingmark($model, $course);
                    $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

                    $Row["BORDER_SCORE-".$course] = $passingRow["BORDER_SCORE"];
                    $Row["SUCCESS_CNT-" .$course] = (!strlen($passingRow["SUCCESS_CNT"])) ? 0 : $passingRow["SUCCESS_CNT"];
                    $Row["SUCCESS_CNT_SPECIAL-" .$course] = (!strlen($passingRow["SUCCESS_CNT_SPECIAL"])) ? 0 : $passingRow["SUCCESS_CNT_SPECIAL"];
                    $Row["SUCCESS_CNT_SPECIAL2-" .$course] = (!strlen($passingRow["SUCCESS_CNT_SPECIAL2"])) ? 0 : $passingRow["SUCCESS_CNT_SPECIAL2"];
                }
            }
        }

        //合格点
        $blank = "";
        foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
            $mark = $examcourseArray["EXAMCOURSE_MARK"];
            $course = $examcourseArray["EXAMCOURSE"];

            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
            $textBox = knjCreateTextBox($objForm, $Row["BORDER_SCORE-".$course], "BORDER_SCORE-".$course, 5, 3, $extra);
            $arg["dataScore"][]["BORDER_SCORE"] = $blank . $mark . ":" . $textBox . "点";
            $blank = "　";

            //合格点(ＧＳ補欠)
            if ($model->hoketuFlg && $examcourseArray["EXAMCOURSE_MARK"] == "ＧＡ") {
                $mark = $model->hoketuMark;
                $course = $model->hoketuCourse;

                $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
                $textBox = knjCreateTextBox($objForm, $Row["BORDER_SCORE-".$course], "BORDER_SCORE-".$course, 5, 3, $extra);
                $arg["dataScore"][]["BORDER_SCORE"] = "(".$mark . ":" . $textBox . "点)";
            }
        }

        //合格数(男,女)
        $count = 0;
        $blank = "";
        foreach ($model->desiredivArray[$model->desirediv] as $wishno => $examcourseArray) {
            $mark = $examcourseArray["EXAMCOURSE_MARK"];
            $course = $examcourseArray["EXAMCOURSE"];

            $arg["dataCnt"][]["SUCCESS_CNT"] = $blank . $mark . ": " . $Row["SUCCESS_CNT-".$course] . "({$Row["SUCCESS_CNT_SPECIAL-".$course]}/{$Row["SUCCESS_CNT_SPECIAL2-".$course]})" . " 名";
            $blank = "　";
            knjCreateHidden($objForm, "SUCCESS_CNT-".$course, $Row["SUCCESS_CNT-".$course]);
            knjCreateHidden($objForm, "SUCCESS_CNT_SPECIAL-".$course, $Row["SUCCESS_CNT_SPECIAL-".$course]);
            knjCreateHidden($objForm, "SUCCESS_CNT_SPECIAL2-".$course, $Row["SUCCESS_CNT_SPECIAL2-".$course]);
            $total += $Row["SUCCESS_CNT-".$course];
            $total1 += $Row["SUCCESS_CNT_SPECIAL-".$course];
            $total2 += $Row["SUCCESS_CNT_SPECIAL2-".$course];

            //合格数(男,女)(ＧＳ補欠)
            if ($model->hoketuFlg && $examcourseArray["EXAMCOURSE_MARK"] == "ＧＡ") {
                $mark = $model->hoketuMark;
                $course = $model->hoketuCourse;

                $arg["dataCnt"][]["SUCCESS_CNT"] = "(".$mark . ": " . $Row["SUCCESS_CNT-".$course] . "({$Row["SUCCESS_CNT_SPECIAL-".$course]}/{$Row["SUCCESS_CNT_SPECIAL2-".$course]})" . " 名)";
                knjCreateHidden($objForm, "SUCCESS_CNT-".$course, $Row["SUCCESS_CNT-".$course]);
                knjCreateHidden($objForm, "SUCCESS_CNT_SPECIAL-".$course, $Row["SUCCESS_CNT_SPECIAL-".$course]);
                knjCreateHidden($objForm, "SUCCESS_CNT_SPECIAL2-".$course, $Row["SUCCESS_CNT_SPECIAL2-".$course]);
            }

            $count++;
            if (get_count($model->desiredivArray[$model->desirediv]) == $count) {
                $arg["dataCnt"][]["SUCCESS_CNT"] = $blank . "合計" . ": " . $total . "({$total1}/{$total2})" . " 名";
            }
        }

        //確定結果一覧--------------------------------------

        //ヘッダ
        $sumCourse = array();
        $sumCourseSex1 = array();
        $sumCourseSex2 = array();
        foreach ($optCourse as $course => $mark) {
            $arg["dataCourse"][]["EXAMCOURSE_MARK"] = $mark;
            $sumCourse[$course] = 0;
            $sumCourseSex1[$course] = 0;
            $sumCourseSex2[$course] = 0;

            if ($hoketuFlg2 && $mark == "ＧＡ") {
                $mark = $model->hoketuMark;
                $course = $model->hoketuCourse;

                $arg["dataCourse"][]["EXAMCOURSE_MARK"] = "(".$mark.")";
                $sumCourse[$course] = 0;
                $sumCourseSex1[$course] = 0;
                $sumCourseSex2[$course] = 0;
            }
        }

        //合格点マスタ
        $total = $totalALL = 0;
        $totalSex1 = $totalALLSex1 = 0;
        $totalSex2 = $totalALLSex2 = 0;
        foreach ($optShdiv as $key => $shdiv) {
        foreach ($optDesirediv2 as $key => $desiredivArray) {
            $row = array();
            $desirediv = $desiredivArray["value"];
            $flg = true;
            $total = 0;
            $totalSex1 = 0;
            $totalSex2 = 0;
            $number = 0;
            foreach ($optCourse as $course => $mark) {
                $number++;
                $query = knjl070rQuery::selectQueryPassingmark($model, $course, $desirediv, $shdiv);
                $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (isset($passingRow)) {
                    $row["BORDER_SCORE".$number] = $passingRow["BORDER_SCORE"];
                    $row["SUCCESS_CNT".$number] = $passingRow["SUCCESS_CNT"] . "({$passingRow["SUCCESS_CNT_SPECIAL"]}/{$passingRow["SUCCESS_CNT_SPECIAL2"]})";
                    $total += $passingRow["SUCCESS_CNT"];
                    $totalSex1 += $passingRow["SUCCESS_CNT_SPECIAL"];
                    $totalSex2 += $passingRow["SUCCESS_CNT_SPECIAL2"];
                    $sumCourse[$course] += $passingRow["SUCCESS_CNT"];
                    $sumCourseSex1[$course] += $passingRow["SUCCESS_CNT_SPECIAL"];
                    $sumCourseSex2[$course] += $passingRow["SUCCESS_CNT_SPECIAL2"];
                    $flg = false;
                }

                if ($hoketuFlg2 && $mark == "ＧＡ") {
                    $course = $model->hoketuCourse;

                    $query = knjl070rQuery::selectQueryPassingmark($model, $course, $desirediv, $shdiv);
                    $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if (isset($passingRow)) {
                        $row["BORDER_SCORE_HOKETU"] = $passingRow["BORDER_SCORE"];
                        $row["SUCCESS_CNT_HOKETU"] = $passingRow["SUCCESS_CNT"] . "({$passingRow["SUCCESS_CNT_SPECIAL"]}/{$passingRow["SUCCESS_CNT_SPECIAL2"]})";
                        $sumCourse[$course] += $passingRow["SUCCESS_CNT"];
                        $sumCourseSex1[$course] += $passingRow["SUCCESS_CNT_SPECIAL"];
                        $sumCourseSex2[$course] += $passingRow["SUCCESS_CNT_SPECIAL2"];
                        $flg = false;
                    }
                }
            }
            //合格点マスタがない場合は表示しない
            if ($flg) continue;
            if ($shdiv == "1") {
                $row["SHDIV"] = $testdivRow["ABBV3"] . "あり";
                $row["DESIREDIV"] = $desirediv . ":" . $desiredivArray["markNaidaku"] . $desiredivArray["markJuken"];
            } else {
                $row["SHDIV"] = $testdivRow["ABBV3"] . "なし";
                $row["DESIREDIV"] = $desirediv . ":" . $desiredivArray["markJuken"];
            }
            //リンク
            $row["SUCCESS_CNT_TOTAL"] = View::alink("knjl070rindex.php", $total, "",
                                        array("cmd"           => "edit",
                                              "APPLICANTDIV"  => $model->applicantdiv,
                                              "TESTDIV"       => $model->testdiv,
                                              "SHDIV"         => $shdiv,
                                              "DESIREDIV"     => $desirediv
                                    )) . "({$totalSex1}/{$totalSex2})";
            $totalALL += $total;
            $totalALLSex1 += $totalSex1;
            $totalALLSex2 += $totalSex2;
            $arg["dataDesirediv"][] = $row;
        }
        }

        //フッタ
        foreach ($optCourse as $course => $mark) {
            $arg["dataCourseSum"][]["SUCCESS_CNT"] = $sumCourse[$course] . "({$sumCourseSex1[$course]}/{$sumCourseSex2[$course]})";

            if ($hoketuFlg2 && $mark == "ＧＡ") {
                $mark = $model->hoketuMark;
                $course = $model->hoketuCourse;

                $arg["dataCourseSum"][]["SUCCESS_CNT"] = $sumCourse[$course] . "({$sumCourseSex1[$course]}/{$sumCourseSex2[$course]})";
            }
        }
        $arg["sum"]["SUCCESS_CNT_TOTAL"] = $totalALL . "({$totalALLSex1}/{$totalALLSex2})";

        //DB切断
        Query::dbCheckIn($db);

        //シミュレーションボタン
        $extra = "onclick=\"return btn_submit('sim');\"";
        $arg["btn_sim"] = knjCreateBtn($objForm, "btn_sim", "シミュレーション", $extra);

        //確定ボタン
        $extra = "onclick=\"return btn_submit('decision');\"";
        $arg["btn_decision"] = knjCreateBtn($objForm, "btn_decision", "確 定", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl070rForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($row["NAMESPARE2"] == '1' && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
