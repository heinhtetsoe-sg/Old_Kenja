<?php
class knjl120iForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "getPointMst") {
            $query = knjl120iQuery::getPointMst($model);
            $pointData = $db->getRow(knjl120iQuery::getPointMst($model), DB_FETCHMODE_ASSOC);
            if (!$pointData) {
                $pointData = array("PLUS_POINT" => "0", "MINUS_POINT" => "0");
            }
            echo json_encode($pointData);
            die();
        }

        //年度
        $arg["TOP"]["YEAR"] = $model->examyear;
        knjCreateHidden($objForm, "YEAR", $model->examyear);

        //入試制度
        $extra = "onChange=\"return btn_submit('main')\"";
        $query = knjl120iQuery::getNameMst($model->examyear, "L003", $model->applicantdiv);
        $arg["TOP"]["APPLICANTDIV"] = makeCmb($objForm, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分
        $query = knjl120iQuery::getTestDiv($model);
        $extra = "onchange=\"return btn_submit('main');\" ";
        $arg["TOP"]["TESTDIV"] = makeCmb($objForm, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //表示区分 (1:調査書・評定 2:その他)
        $opt = array(1, 2);
        $model->field["DISP_DIV"] = ($model->field["DISP_DIV"] == "") ? "1" : $model->field["DISP_DIV"];
        $addExtra = " onchange=\"return changeDispDiv(this);\" ";
        $extra = array("id=\"DISP_DIV1\" {$addExtra}", "id=\"DISP_DIV2\" {$addExtra}");
        $radioArray = knjCreateRadio($objForm, "DISP_DIV", $model->field["DISP_DIV"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        if ($model->cmd == "main") {
            $model->field["S_EXAMNO"] = "";
            $model->field["E_EXAMNO"] = "";
        }

        $model->field["S_EXAMNO"] = ($model->field["S_EXAMNO"]) ? sprintf("%04d", $model->field["S_EXAMNO"]) : "";
        $model->field["E_EXAMNO"] = ($model->field["E_EXAMNO"]) ? sprintf("%04d", $model->field["E_EXAMNO"]) : "";

        //開始と終了の大小が逆の場合入れ替える
        if ($model->field["E_EXAMNO"] != "" && $model->field["S_EXAMNO"] > $model->field["E_EXAMNO"]) {
            $chgwk = $model->field["E_EXAMNO"];
            $model->field["E_EXAMNO"] = $model->field["S_EXAMNO"];
            $model->field["S_EXAMNO"] = $chgwk;
        }

        //ヘッダ部作成
        $header = array();
        if ($model->field["DISP_DIV"] == "1") {
            $arg["DISP_DIV1"] = 1;
        } else {
            $arg["DISP_DIV2"] = 1;
            $model->itemWidth = "120";
            $arg["WIDTH"] = $model->itemWidth;
            if ($model->field["TESTDIV"] == "2") {
                $model->itemWidth = "90";
                $arg["WIDTH"] = $model->itemWidth;
            }
        }
        if ($model->field["TESTDIV"] == "2") {
            $arg["TESTDIV2"] = 1;
        }

        //データ読み込み判定
        if ($model->cmd == "search" || $model->cmd == "back" || $model->cmd == "next") {
            //編集対象となるデータの表示は最初に上記cmdを発行する必要あり
            $model->yomikomiFlg = true;
            $model->mainField = array();
            //他入試区分で既に調査書データが存在している場合はコピー
            $model->copyApplicantConfrpt($db);
        } elseif ($model->cmd == "main") {
            //メインデータの抽出条件(受験番号範囲以外)が変わった場合は再度読み込み処理を行なわないとデータが表示されない
            $model->yomikomiFlg = false;
        }

        //一覧表示
        $model->arr_examno = array();
        if ($model->yomikomiFlg) {
            $dataflg = false;
            $counter = 1;
            $query = knjl120iQuery::SelectQuery($model, $arr_classCd);

            $result = $db->query($query);

            //データが1件もなかったらメッセージを返す
            $totalCnt = $result->numRows();
            if ($totalCnt == 0) {
                $model->setWarning("MSG303");
            }

            $model->postValFlg = (isset($model->warning)) ? true : false;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $examno = $row["EXAMNO"];

                if ($counter == 1) {
                    $model->field["S_EXAMNO"] = $examno;
                }
                if ($counter == $totalCnt) {
                    $model->field["E_EXAMNO"] = $examno;
                }

                $dispRow = array();
                makeHiddenCommon($objForm, $row);

                //調査書評定・出欠
                if ($model->field["DISP_DIV"] == "1") {
                    $dispRow = getDispRowPtrn1($objForm, $db, $model, $model->mainField[$examno], $row);
                    makeHiddenPtrn1($objForm, $model, $model->mainField[$examno], $row);
                //その他
                } else {
                    $dispRow = getDispRowPtrn2($objForm, $db, $model, $model->mainField[$examno], $row);
                    makeHiddenPtrn2($objForm, $model, $model->mainField[$examno], $row);
                }

                //受験番号
                $dispRow["EXAMNO"] =  $examno;
                //氏名
                $dispRow["NAME"] =  $row["NAME"];

                $dataflg = true;
                $model->arr_examno[] = $examno;
                $model->arr_recom_examno[$examno] = $row["RECOM_EXAMNO"];
                $arg["data"][] = $dispRow;
                $counter++;
            }
        }

        //受験番号
        $extra = " onchange=\"this.value=toAlphaNumber(this.value);\" ";
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->field["S_EXAMNO"], "S_EXAMNO", 4, 4, $extra);
        $extra = " onchange=\"this.value=toAlphaNumber(this.value);\" ";
        $model->field["E_EXAMNO"] = ($model->field["E_EXAMNO"]) ? sprintf("%04d", $model->field["E_EXAMNO"]) : "";
        $arg["TOP"]["E_EXAMNO"] = knjCreateTextBox($objForm, $model->field["E_EXAMNO"], "E_EXAMNO", 4, 4, $extra);


        $query = knjl120iQuery::SelectFstExamno($model);
        $fstExamno =  $db->getOne($query);
        $query = knjl120iQuery::SelectLstExamno($model);
        $lstExamno =  $db->getOne($query);

        $fsthidden = $fstExamno == $model->field["S_EXAMNO"] ? " disabled " : "";
        $extra = "onClick=\"btn_submit('back');\" ".$fsthidden;
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        $lsthidden = $lstExamno == $model->field["E_EXAMNO"] ? " disabled " : "";
        $extra = "onClick=\"btn_submit('next');\" ".$lsthidden;
        $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl120iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjl120iForm1.html", $arg, "main5_JqueryOnly.html");
    }
}

//フォーム作成(調査書評定・出欠)
function getDispRowPtrn1(&$objForm, $db, $model, $mainField, $row)
{
    $dispRow = array();
    $examno = $row["EXAMNO"];
    $onchange=" onchange=\"doChangeValFlg();\" ";

    //各科目評定
    $total3   = 0;
    $totalAll = 0;
    for ($score_i = 1; $score_i <= 9; $score_i++) {
        $extra = " onblur=\"setPoint1(this, '{$examno}', chkHyoutei);\" {$onchange} ";
        $value = ($model->postValFlg) ? $mainField["SCORE".$score_i."_".$examno] : $row["SCORE".$score_i];
        $dispRow["SCORE".$score_i] = knjCreateTextBox($objForm, $value, "SCORE".$score_i."_".$examno, 3, 1, $extra);

        //科目合計計算 ※更新によってDBに保持された値を足す
        if (in_array($score_i, array(5, 6, 7))) {
            $total3 += $row["SCORE".$score_i];
        }
        $totalAll += $row["SCORE".$score_i];
    }

    //全科目合計
    $dispRow["TOTAL_ALL"]     = $totalAll;
    $dispRow["TOTAL_ALL_ID"]  = "TOTAL_ALL_{$examno}";
    //3科目合計(5:音楽・6:美術・7:保体)
    $dispRow["TOTAL3"]     = $total3;
    $dispRow["TOTAL3_ID"]  = "TOTAL3_{$examno}";

    //欠席日数
    $extra = " onblur=\"setPoint1(this, '{$examno}', chkCommonErr);\" {$onchange} ";
    $value = ($model->postValFlg) ? $mainField["ABSENCE_DAYS_".$examno] : $row["ABSENCE_DAYS"];
    $dispRow["ABSENCE_DAYS"] = knjCreateTextBox($objForm, $value, "ABSENCE_DAYS_".$examno, 3, 3, $extra);
    $value = ($model->postValFlg) ? $mainField["ABSENCE_DAYS2_".$examno] : $row["ABSENCE_DAYS2"];
    $dispRow["ABSENCE_DAYS2"] = knjCreateTextBox($objForm, $value, "ABSENCE_DAYS2_".$examno, 3, 3, $extra);
    $value = ($model->postValFlg) ? $mainField["ABSENCE_DAYS3_".$examno] : $row["ABSENCE_DAYS3"];
    $dispRow["ABSENCE_DAYS3"] = knjCreateTextBox($objForm, $value, "ABSENCE_DAYS3_".$examno, 3, 3, $extra);

    //欠席計
    $dispRow["ABSENCE_TOTAL"] = $row["ABSENCE_DAYS"] + $row["ABSENCE_DAYS2"] + $row["ABSENCE_DAYS3"];
    $dispRow["ABSENCE_TOTAL_ID"] = "ABSENCE_TOTAL_{$examno}";
    knjCreateHidden($objForm, "ABSENCE_TOTAL");

    return $dispRow;
}

//フォーム作成(その他)
function getDispRowPtrn2(&$objForm, $db, $model, $mainField, $row)
{
    $dispRow = array();
    $examno = $row["EXAMNO"];
    $onchange=" onchange=\"doChangeValFlg();\" ";

    //1・2年次評定合計
    $disabled = ($model->field["TESTDIV"] == "1") ? " style=\"background-color:darkgray;\" disabled " : "";
    $extra = " onblur=\"setPoint1(this, '{$examno}', chkHyouteiTotal);\" {$onchange}".$disabled;
    $value = ($model->postValFlg) ? $mainField["TOTAL_HYOUTEI_".$examno] : $row["TOTAL_HYOUTEI"];
    $dispRow["TOTAL_HYOUTEI"] = knjCreateTextBox($objForm, $value, "TOTAL_HYOUTEI_".$examno, 3, 3, $extra);

    if ($model->field["TESTDIV"] == "2") {
        //業績
        $extra = " onblur=\"setPoint1(this, '{$examno}', chkArchivement);\" {$onchange}".$disabled;
        $value = ($model->postValFlg) ? $mainField["ACHIEVEMENT_".$examno] : $row["ACHIEVEMENT"];
        $dispRow["ACHIEVEMENT"] = knjCreateTextBox($objForm, $value, "ACHIEVEMENT_".$examno, 3, 3, $extra);
    }

    //調査書(+)
    $disabled = ($model->field["TESTDIV"] == "2") ? " style=\"background-color:darkgray;\" disabled " : "";
    $extra = " onblur=\"setPoint1(this, '{$examno}', chkCommonErr);\" {$onchange}".$disabled;
    $value = ($model->postValFlg) ? $mainField["REPORT_PLUS_".$examno] : $row["REPORT_PLUS"];
    $dispRow["REPORT_PLUS"] = knjCreateTextBox($objForm, $value, "REPORT_PLUS_".$examno, 3, 3, $extra);

    //調査書(-)
    $disabled = ($model->field["TESTDIV"] == "2") ? " style=\"background-color:darkgray;\" disabled " : "";
    $extra = " onblur=\"setPoint1(this, '{$examno}', chkCommonErr);\" {$onchange}".$disabled;
    $value = ($model->postValFlg) ? $mainField["REPORT_MINUS_".$examno] : $row["REPORT_MINUS"];
    $dispRow["REPORT_MINUS"] = knjCreateTextBox($objForm, $value, "REPORT_MINUS_".$examno, 3, 3, $extra);

    if ($model->field["TESTDIV"] == "2") {
        //自己推薦(+)
        $extra = " onblur=\"setPoint1(this, '{$examno}', chkCommonErr);\" {$onchange}";
        $value = ($model->postValFlg) ? $mainField["SELF_REC_PLUS_".$examno] : $row["SELF_REC_PLUS"];
        $dispRow["SELF_REC_PLUS"] = knjCreateTextBox($objForm, $value, "SELF_REC_PLUS_".$examno, 3, 3, $extra);

        //自己推薦(-)
        $extra = " onblur=\"setPoint1(this, '{$examno}', chkCommonErr);\" {$onchange}";
        $value = ($model->postValFlg) ? $mainField["SELF_REC_MINUS_".$examno] : $row["SELF_REC_MINUS"];
        $dispRow["SELF_REC_MINUS"] = knjCreateTextBox($objForm, $value, "SELF_REC_MINUS_".$examno, 3, 3, $extra);
    }

    //同窓
    $disabled = ($model->field["TESTDIV"] == "1") ? " style=\"background-color:darkgray;\" disabled " : "";
    $extra = " onblur=\"setPoint1(this, '{$examno}', chkCommonErr);\" {$onchange}".$disabled;
    $value = ($model->postValFlg) ? $mainField["DOUSOU_PLUS_".$examno] : $row["DOUSOU_PLUS"];
    $dispRow["DOUSOU_PLUS"] = knjCreateTextBox($objForm, $value, "DOUSOU_PLUS_".$examno, 3, 3, $extra);

    //英検取得級
    $query = knjl120iQuery::getQualifiedNameMst();
    $extra = " style=\"width:{$model->itemWidth}\px;\" {$onchange}";
    $value = ($model->postValFlg) ? $mainField["QUALIFIED_ENG_".$examno] : $row["QUALIFIED_ENG"];
    $dispRow["QUALIFIED_ENG"] = makeCmb($objForm, $db, $query, "QUALIFIED_ENG_".$examno, $value, $extra, 1, "blank");

    //+調整点
    $extra = " onblur=\"setPoint1(this, '{$examno}', chkCommonErr);\" {$onchange}";
    $value = ($model->postValFlg) ? $mainField["TYOUSEI_PLUS_".$examno] : $row["TYOUSEI_PLUS"];
    $dispRow["TYOUSEI_PLUS"] = knjCreateTextBox($objForm, $value, "TYOUSEI_PLUS_".$examno, 3, 3, $extra);

    //-調整点
    $extra = " onblur=\"setPoint1(this, '{$examno}', chkCommonErr);\" {$onchange}";
    $value = ($model->postValFlg) ? $mainField["TYOUSEI_MINUS_".$examno] : $row["TYOUSEI_MINUS"];
    $dispRow["TYOUSEI_MINUS"] = knjCreateTextBox($objForm, $value, "TYOUSEI_MINUS_".$examno, 3, 3, $extra);

    //+合計
    $value = ($model->postValFlg) ? $mainField["TOTAL_PLUS_".$examno] : $row["TOTAL_PLUS"];
    $dispRow["TOTAL_PLUS"] = intval($value);
    $dispRow["TOTAL_PLUS_ID"]  = "TOTAL_PLUS_{$examno}";
    //-合計
    $value = ($model->postValFlg) ? $mainField["TOTAL_MINUS_".$examno] : $row["TOTAL_MINUS"];
    $dispRow["TOTAL_MINUS"] = intval($value);
    $dispRow["TOTAL_MINUS_ID"]  = "TOTAL_MINUS_{$examno}";

    return $dispRow;
}

//hidden作成(共通)
function makeHiddenCommon($objForm, $row)
{
    $examno = $row["EXAMNO"];

    knjCreateHidden($objForm, "INTERVIEW_PLUS_".$examno, $row["INTERVIEW_PLUS"]);
    knjCreateHidden($objForm, "INTERVIEW_MINUS_".$examno, $row["INTERVIEW_MINUS"]);

    knjCreateHidden($objForm, "TOTAL9_PLUS_".$examno, $row["TOTAL9_PLUS"]);
    knjCreateHidden($objForm, "TOTAL9_MINUS_".$examno, $row["TOTAL9_MINUS"]);
    knjCreateHidden($objForm, "TOTAL3_PLUS_".$examno, $row["TOTAL3_PLUS"]);
    knjCreateHidden($objForm, "TOTAL3_MINUS_".$examno, $row["TOTAL3_MINUS"]);
    knjCreateHidden($objForm, "ABSENCE_TOTAL_PLUS_".$examno, $row["ABSENCE_TOTAL_PLUS"]);
    knjCreateHidden($objForm, "ABSENCE_TOTAL_MINUS_".$examno, $row["ABSENCE_TOTAL_MINUS"]);
    knjCreateHidden($objForm, "HYOUTEI1_2_PLUS_".$examno, $row["HYOUTEI1_2_PLUS"]);
    knjCreateHidden($objForm, "HYOUTEI1_2_MINUS_".$examno, $row["HYOUTEI1_2_MINUS"]);
}

//hidden作成1
function makeHiddenPtrn1($objForm, $model, $mainField, $row)
{
    //表示区分「調査書評定・出欠」で表示されない項目の値はhiddenで保持
    $examno = $row["EXAMNO"];
    knjCreateHidden($objForm, "TOTAL_HYOUTEI_".$examno, $row["TOTAL_HYOUTEI"]);
    knjCreateHidden($objForm, "ACHIEVEMENT_".$examno, $row["ACHIEVEMENT"]);
    knjCreateHidden($objForm, "REPORT_PLUS_".$examno, $row["REPORT_PLUS"]);
    knjCreateHidden($objForm, "REPORT_MINUS_".$examno, $row["REPORT_MINUS"]);
    knjCreateHidden($objForm, "SELF_REC_PLUS_".$examno, $row["SELF_REC_PLUS"]);
    knjCreateHidden($objForm, "SELF_REC_MINUS_".$examno, $row["SELF_REC_MINUS"]);
    knjCreateHidden($objForm, "DOUSOU_PLUS_".$examno, $row["DOUSOU_PLUS"]);
    knjCreateHidden($objForm, "QUALIFIED_ENG_".$examno, $row["QUALIFIED_ENG"]);
    knjCreateHidden($objForm, "TYOUSEI_PLUS_".$examno, $row["TYOUSEI_PLUS"]);
    knjCreateHidden($objForm, "TYOUSEI_MINUS_".$examno, $row["TYOUSEI_MINUS"]);
    knjCreateHidden($objForm, "TOTAL_PLUS_".$examno, $row["TOTAL_PLUS"]);
    knjCreateHidden($objForm, "TOTAL_MINUS_".$examno, $row["TOTAL_MINUS"]);

    //表示区分「調査書評定・出欠」で表示されるが入力欄が無い項目はhiddenで保持
    $value = ($model->postValFlg) ? $mainField["TOTAL_ALL_".$examno] : $row["TOTAL_ALL"];
    knjCreateHidden($objForm, "TOTAL_ALL_".$examno, $value);

    $value = ($model->postValFlg) ? $mainField["TOTAL3_".$examno] : $row["TOTAL3"];
    knjCreateHidden($objForm, "TOTAL3_".$examno, $value);

    $value = ($model->postValFlg) ? $mainField["ABSENCE_TOTAL_".$examno] : $row["ABSENCE_TOTAL"];
    knjCreateHidden($objForm, "ABSENCE_TOTAL_".$examno, $value);
}

//hidden作成2
function makeHiddenPtrn2($objForm, $model, $mainField, $row)
{
    //表示区分「その他」で表示されない項目の値はhiddenで保持
    $examno = $row["EXAMNO"];
    for ($score_i = 1; $score_i <= 9; $score_i++) {
        knjCreateHidden($objForm, "SCORE{$score_i}_".$examno, $row["SCORE{$score_i}"]);
    }
    knjCreateHidden($objForm, "TOTAL_ALL_".$examno, $row["TOTAL_ALL"]);
    knjCreateHidden($objForm, "TOTAL3_".$examno, $row["TOTAL3"]);
    knjCreateHidden($objForm, "ABSENCE_DAYS_".$examno, $row["ABSENCE_DAYS"]);
    knjCreateHidden($objForm, "ABSENCE_DAYS2_".$examno, $row["ABSENCE_DAYS2"]);
    knjCreateHidden($objForm, "ABSENCE_DAYS3_".$examno, $row["ABSENCE_DAYS3"]);
    knjCreateHidden($objForm, "ABSENCE_TOTAL_".$examno, $row["ABSENCE_DAYS"] + $row["ABSENCE_DAYS2"] + $row["ABSENCE_DAYS3"]);

    //表示区分「その他」で表示されるが入力欄が無い項目はhiddenで保持
    $value = ($model->postValFlg) ? $mainField["TOTAL_PLUS_".$examno] : $row["TOTAL_PLUS"];
    knjCreateHidden($objForm, "TOTAL_PLUS_".$examno, $value);

    $value = ($model->postValFlg) ? $mainField["TOTAL_MINUS_".$examno] : $row["TOTAL_MINUS"];
    knjCreateHidden($objForm, "TOTAL_MINUS_".$examno, $value);
}

//コンボ作成
function makeCmb(&$objForm, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
        $i++;
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //読込ボタン
    $extra = "onclick=\"return btn_submit('search');\" ";
    $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "読 込", $extra);
    //算出ボタン
    $extra = "onclick=\"return calc(this);\" ";
    $arg["btn_calc"] = knjCreateBtn($objForm, "btn_calc", "一括算出", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //序列確定ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "序列確定", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $model->arr_examno));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL120I");
    knjCreateHidden($objForm, "changeValFlg", "0");
}
