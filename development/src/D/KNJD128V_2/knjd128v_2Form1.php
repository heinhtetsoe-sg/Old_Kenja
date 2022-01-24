<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd128v_2Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //CSV
        $objUp = new csvFile();

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd128v_2index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //課程学科コンボ
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["use_school_detail_gcm_dat"] = 1;
            /* Edit by Kaung for PC-Talker 2020-02-10 start*/
            $extra = "aria-label=\"課程学科\" id=\"COURSE_MAJOR\" onChange=\"current_cursor('COURSE_MAJOR');btn_submit('main')\";";
            /* Edit by Kaung for PC-Talker 2020-02-20 end*/
            $query = knjd128v_2Query::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1, "");
        }

        //「ＣＳＶは、非表示とする？」のフラグを取得
        $rtnRow = array();
        $rtnRow = $db->getRow(knjd128v_2Query::getNameMstD058(), DB_FETCHMODE_ASSOC);
        $useCsv = $rtnRow["NAMESPARE1"] == "1" ? false : true; // 1:非表示
        //CSV表示不可チェック
        $notCsvCnt = $db->getOne(knjd128v_2Query::getNotusecsvCnt($model));
        if ($useCsv && $notCsvCnt == 0) {
            $arg["useCsv"] = 1;
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //科目コンボ
        /* Edit by Kaung for PC-Talker 2020-02-10 start */
        $extra = "aria-label=\"科目\" id=\"SUBCLASSCD\" onChange=\"current_cursor('SUBCLASSCD');btn_submit('subclasscd');\"";
        /* Edit by Kaung for PC-Talker 2020-02-20 end */
        $query = knjd128v_2Query::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_SUBCLASSCD");

        //基本設定のコードを取得
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->field["SUBCLASSCD"]) {
                $subclassArray = array();
                $subclassArray = explode("-", $model->field["SUBCLASSCD"]);
                $schoolKind = $subclassArray[1];
            } else {
                $schoolKind = $db->getOne(knjd128v_2Query::getSchoolkindQuery());
            }
            $schoolKind = $db->getOne(knjd128v_2Query::getAdminSchoolKind($schoolKind));
            $model->subclassAll = "00-".$schoolKind."-00-000000";
        } else {
            $model->subclassAll = "000000";
        }

        //講座コンボ
        /* Edit by Kaung for PC-Talker 2020-02-10 start */
        $extra = "aria-label=\"学級・講座\" id=\"CHAIRCD\" onChange=\"current_cursor('CHAIRCD');btn_submit('chaircd');\"";
        /* Edit by Kaung for PC-Talker 2020-02-20 end */
        $query = knjd128v_2Query::selectChairQuery($model);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_CHAIRCD");

        //講座生徒データ基準日
        $semMstRow = array();
        $semMstRow = $db->getRow(knjd128v_2Query::getSemesterMst(), DB_FETCHMODE_ASSOC);
        if ($semMstRow["SDATE"] <= CTRL_DATE && CTRL_DATE <= $semMstRow["EDATE"]) {
            $execute_date = CTRL_DATE;
        } else {
            $execute_date = $semMstRow["EDATE"];
        }

        $arg["SELECT_SEQ"] = "追指導";
        $arg["SELECT_SEQ_REMARK"] = "備考";
        $maxSlumpSeq = "";
        knjCreateHidden($objForm, "useSlumpSeq", $model->Properties["useSlumpSeq"]);
        if ($model->Properties["useSlumpSeq"] == '1') {
            $arg["useSlumpSeq"] = 1;

            //追指導回数MAX値
            $maxSlumpSeq = $db->getOne(knjd128v_2Query::getMaxSlumpSeq($model, $execute_date));

            //追指導回数コンボ(MAX値+1まで)
            $extra = "onChange=\"btn_submit('slump_seq')\";";
            $query = knjd128v_2Query::selectSlumpSeq($model, $maxSlumpSeq);
            makeCmb($objForm, $arg, $db, $query, "SLUMP_SEQ", $model->field["SLUMP_SEQ"], $extra, 1, "");
            knjCreateHidden($objForm, "H_SLUMP_SEQ");

            //関西学院の場合、追指導回数MAX値は２つまでと制限し、文言を「追指導1 → 2年再試」「追指導2 → 3年再試」に変更
            if ($model->z010name1 == "kwansei") {
                //追指導データ(2回目以降が選択された場合の1列目:表示のみ)
                if ($model->field["SLUMP_SEQ"] > 1) {
                    $arg["IS_SCORE_SHOW"] = 1;
                    $arg["SELECT_SEQ_PRE"] = $model->field["SLUMP_SEQ"]."年再試";
                }
                $arg["SELECT_SEQ"] = ($model->field["SLUMP_SEQ"]+1)."年再試";
                $arg["SELECT_SEQ_REMARK"] = ($model->field["SLUMP_SEQ"]+1)."年備考";
            } else {
                //追指導データ(2回目以降が選択された場合の1列目:表示のみ)
                if ($model->field["SLUMP_SEQ"] > 1) {
                    $arg["IS_SCORE_SHOW"] = 1;
                    $arg["SELECT_SEQ_PRE"] = "追指導".($model->field["SLUMP_SEQ"]-1);
                }
                $arg["SELECT_SEQ"] = "追指導".$model->field["SLUMP_SEQ"];
                $arg["SELECT_SEQ_REMARK"] = "備考".$model->field["SLUMP_SEQ"];
            }
        }

        //明細ヘッダデータ作成
        $model->testcdArray = makeHead($objForm, $arg, $db, $model);

        foreach ($model->testcdArray as $key => $codeArray) {
            //合格チェックボックス表示
            if ($model->Properties["useSlumpSeq"] == '2' && $codeArray["TESTCD"] == "9990008") {
                $arg["useSlumpSeq2"] = 1;
            }
        }

        //明細データ作成
        makeMeisai($objForm, $arg, $db, $model, $execute_date, $maxSlumpSeq, $objUp);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model, $execute_date);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd128v_2Form1.html", $arg);
    }
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model)
{
    //科目別設定があるか
    $testSubCnt = $db->getOne(knjd128v_2Query::getTestSubCnt($model));
    if ($testSubCnt == "") {
        $testSubCnt = 0;
    }

    $sem = "";
    $col = 0;
    $semArray = array();
    $head1 = "";
    $head2 = "";
    $testcdArray = array();
    $result = $db->query(knjd128v_2Query::getTestName($model, $testSubCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($sem != $row["SEMESTER"]) {
            $sem = $row["SEMESTER"];
            $col = 0;
        }
        $col++;
        $semArray[$row["SEMESTER"]] = "<th colspan={$col} ><font size=2>".$row["SEMESTERNAME"]."</font></th> ";

        $head2 .= "<th width=120 ><font size=2>".$row["TESTITEMNAME"]."</font></th> ";
        $testcdArray[] = array("TESTCD" => $row["TESTCD"], "SEMESTERNAME" => $row["SEMESTERNAME"], "TESTITEMNAME" => $row["TESTITEMNAME"], "SIDOU_INPUT" => $row["SIDOU_INPUT"], "SIDOU_INPUT_INF" => $row["SIDOU_INPUT_INF"], "CONTROL_FLG" => $row["CONTROL_FLG"], "SEMESTER" => $row["SEMESTER"], "SDATE" => $row["SDATE"], "EDATE" => $row["EDATE"]);
    }
    $result->free();
    foreach ($semArray as $key => $val) {
        $head1 .= $val;
    }
    $arg["HEAD1"] = $head1; //学期名称
    $arg["HEAD2"] = $head2; //考査種別名称
    return $testcdArray;
}

//明細データ
function makeMeisai(&$objForm, &$arg, $db, &$model, $execute_date, $maxSlumpSeq, &$objUp)
{
    /*******/
    /* CSV */
    /*******/
    $testitemname = "";
    foreach ($model->testcdArray as $key => $codeArray) {
        $testitemname = $codeArray["SEMESTERNAME"]."－".$codeArray["TESTITEMNAME"];
    }
    $subclassname = $db->getOne(knjd128v_2Query::getSubclassName($model));
    $chairname = $db->getOne(knjd128v_2Query::getChairName($model));
    //CSV出力ファイル名
    $objUp->setFileName(CTRL_YEAR."年度_".$testitemname."_".$subclassname."_".$chairname."_"."追指導入力.csv");
    //CSVヘッダ名
    $csvHeader = array();
    $csvHeader[] = "テスト種別名";
    $csvHeader[] = "科目コード";
    $csvHeader[] = "科目名";
    $csvHeader[] = "講座コード";
    $csvHeader[] = "講座名";
    $csvHeader[] = "学籍番号";
    $csvHeader[] = "クラス－出席番号";
    $csvHeader[] = "氏名";
    $csvHeader[] = "追指導";//入力エリア
    $csvHeader[] = "備考";  //入力エリア
    $csvHeader[] = $model->lastColumn;

    $setType = array();
    $setSize = array();
    $setCnt = 0;
    foreach ($csvHeader as $key => $val) {
        if ($val == "追指導") {
            $setType[$setCnt] = 'S';
            $setSize[$setCnt] = 3;
        }
        if ($val == "備考") {
            $setType[$setCnt] = 'S';
            $setSize[$setCnt] = 60;
        }
        $setCnt++;
    }

    $objUp->setHeader(array_values($csvHeader));
    $objUp->setType($setType);
    $objUp->setSize($setSize);
    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        //ゼロ埋めフラグ
        $csvFlg = array("科目コード" => array(false,13),
                        "講座コード" => array(true,7),
                        "学籍番号"   => array(true,8));
    } else {
        //ゼロ埋めフラグ
        $csvFlg = array("科目コード" => array(true,6),
                        "講座コード" => array(true,7),
                        "学籍番号"   => array(true,8));
    }
    $objUp->setEmbed_flg($csvFlg);

    //指導（1:マーク）
    $optMarkLabel = array();
    $optMark = array();
    $optMark[] = array('label' => "", 'value' => "");
    $result = $db->query(knjd128v_2Query::getMarkList());
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $optMark[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        $optMarkLabel[$row["VALUE"]] = $row["LABEL"];
    }
    $result->free();

    //追指導データ配列
    $slumpArray = array();
    if ($model->Properties["useSlumpSeq"] == '1' && strlen($maxSlumpSeq)) {
    } else {
        $result = $db->query(knjd128v_2Query::getSlump($model, $execute_date));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $slumpArray[$row["SCHREGNO"]] = $row;
        }
        $result->free();
    }

    $slumpSeqArray = array();
    if ($model->Properties["useSlumpSeq"] == '1') {
        $result = $db->query(knjd128v_2Query::getSlumpSeq($model, $execute_date));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $slumpSeqArray[$row["SCHREGNO"]][$row["SLUMP_SEQ"]] = $row;
        }
        $result->free();
    }

    //初期化
    $model->data = array();
    $counter = 0;

    //テキストの名前を取得する
    $textFieldName = "";
    $textSep = "";
    
    //一覧表示
    $colorFlg = false;
    $query = knjd128v_2Query::getScore($model, $execute_date);
    $result = $db->query($query);
    $scoreArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号を配列で取得
        $scoreArray[$row["SCHREGNO"]] = $row;
    }
    $result->free();

    $result = $db->query(knjd128v_2Query::selectQuery($model, $execute_date));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号をHiddenで保持
        knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);
        //クラス-出席番(表示)
        if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
            $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
        }
        //氏名欄に学籍番号表記
        $row["SCHREGNO_SHOW"] = ($model->Properties["use_SchregNo_hyoji"] == 1) ? $row["SCHREGNO"]."　" : "";
        //５行毎に背景色を変更
        if ($counter % 5 == 0) {
            $colorFlg = !$colorFlg;
        }
        $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

        //書出用CSVデータ
        $csv = array();
        $csv[] = $testitemname;
        $csv[] = $model->field["SUBCLASSCD"];
        $csv[] = $subclassname;
        $csv[] = $model->field["CHAIRCD"];
        $csv[] = $chairname;
        $csv[] = $row["SCHREGNO"];
        $csv[] = $row["ATTENDNO"];
        $csv[] = $row["NAME_SHOW"];
        //キー値をセット
        $csvKey = array("科目コード" => $model->field["SUBCLASSCD"],
                        "講座コード" => $model->field["CHAIRCD"],
                        "学籍番号"   => $row["SCHREGNO"]);

        //異動日の翌日以降は、転学、退学者は、入力不可にする。
        $tentaigakuFlg = false;

        //各項目を作成
        $meisai = "";
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];
            $col = "SCORE" .$testcd;
            //成績データ
            $scoreRow = $scoreArray[$row["SCHREGNO"]];
            $score = "";
            if (is_array($scoreRow)) {
                $diFlg = ($scoreRow["VALUE_DI"] == "*" || $scoreRow["VALUE_DI"] == "**") ? "1" : "";
                if ($model->prgid == "KNJD129L") {
                    if ($scoreRow["VALUE_DI"] == "+" || $scoreRow["VALUE_DI"] == "-") {
                        $diFlg = "1";
                    }
                }
                $score = $diFlg == "1" ? $scoreRow["VALUE_DI"] : $scoreRow["SCORE"];
            }
            //縦計項目
            if (is_numeric($score)) {
                $term_data[$col][] = $score;
            }

            //異動情報
            $colorFlgYellow = false;
            if ($testcd != "9990009" && (strlen($row["TRANSFER_SDATE"]) || strlen($row["TRANSFER_EDATE"]))) {
                //学期期間中すべて異動期間の場合
                if (strtotime($row["TRANSFER_SDATE"]) <= strtotime($codeArray["SDATE"])
                       && strtotime($row["TRANSFER_EDATE"]) >= strtotime($codeArray["EDATE"])) {
                    $colorFlgYellow = true;
                //一部
                } elseif ((strtotime($row["TRANSFER_SDATE"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_SDATE"]) <= strtotime($codeArray["EDATE"]))) {
                    $colorFlgYellow = true;
                } elseif ((strtotime($row["TRANSFER_EDATE"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_EDATE"]) <= strtotime($codeArray["EDATE"]))) {
                    $colorFlgYellow = true;
                }
            }
            //卒業日付
            if ($testcd != "9990009" && strlen($row["GRD_DATE"])) {
                //学期期間中すべて卒業の場合(学期開始日付以前に卒業している場合）
                if (strtotime($row["GRD_DATE"]) <= strtotime($codeArray["SDATE"])) {
                    $colorFlgYellow = true;
                    $tentaigakuFlg = true;
                //一部
                } elseif (strtotime($row["GRD_DATE"]) >  strtotime($codeArray["SDATE"])
                       && strtotime($row["GRD_DATE"]) <= strtotime($codeArray["EDATE"])) {
                    $colorFlgYellow = true;
                }
            }

            //管理者コントロール
            if ($controlFlg == "1" && (!$tentaigakuFlg || $model->auth == DEF_UPDATABLE)) {
                //テキストボックスを作成
                /* Edit by Kaung for PC-Talker 2020-02-10 start */
                $readLabel = $row['ATTENDNO'].$row['SCHREGNO_SHOW'].$row['NAME_SHOW']."の".$codeArray['SEMESTERNAME'].$codeArray['TESTITEMNAME'];
                $extra = "aria-label=\"$readLabel\" id=\"{$col}-{$counter}\" STYLE=\"text-align: right;\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\"";
                /* Edit by Kaung for PC-Talker 2020-02-20 end */
                $row[$col] = knjCreateTextBox($objForm, $score, $col."-".$counter, 3, 3, $extra);
                //考査満点マスタ
                $query = knjd128v_2Query::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $testcd, $row["GRADE"], $row["COURSE"], $model);
                $perfect = ($model->usePerfect == 'true' && substr($testcd, 5, 2) == '01') ? $db->getOne($query) : 100;
                if ($perfect == "") {
                    $perfect = 100;
                }
                //テキストボックスを作成
                knjCreateHidden($objForm, $col."_PERFECT"."-".$counter, $perfect);
                //テキストの名前をセット
                if ($counter == 0) {
                    $textFieldName .= $textSep.$col;
                    $textSep = ",";
                }
                //ラベルのみ
            } else {
                $row[$col] = "<font color=\"#000000\">".$score."</font>";
                //hidden
                knjCreateHidden($objForm, $col."-".$counter, $score);
            }
            $row["BGCOLOR"] = $row["COLOR"]; //通常の背景色
            if ($testcd != "9990009" && $colorFlgYellow) {
                $row["BGCOLOR"] = "#ffff00";
            } //異動
            $meisai .= "<td width=120 align=\"center\" bgcolor={$row["BGCOLOR"]}>".$row[$col]."</td>";

            //追指導データ(2回目以降が選択された場合の1列目:表示のみ)
            if ($model->Properties["useSlumpSeq"] == '1' && $model->field["SLUMP_SEQ"] > 1) {
                $slumpRow = array();
                $slumpRow = $slumpSeqArray[$row["SCHREGNO"]][$model->field["SLUMP_SEQ"]-1]; //直前の追指導
                //指導（1:マーク 2:点数）
                $row["SCORE_SHOW"] = ($codeArray["SIDOU_INPUT_INF"] == "1") ? $optMarkLabel[$slumpRow["MARK"]] : $slumpRow["SCORE"];
            }

            //指導データ
            $slumpRow = array();
            if ($model->Properties["useSlumpSeq"] == '1' && strlen($maxSlumpSeq)) {
                $slumpRow = $slumpSeqArray[$row["SCHREGNO"]][$model->field["SLUMP_SEQ"]]; //選択された回の追指導
            } else {
                $slumpRow = $slumpArray[$row["SCHREGNO"]];
            }
            //指導（1:マーク 2:点数）
            $name = "SCORE_MARK";
            /* Edit by Kaung for PC-Talker 2020-02-10 start */
            $readLabel1 = $row['ATTENDNO'].$row['SCHREGNO_SHOW'].$row['NAME_SHOW']."の追指導";
            /* Edit by Kaung for PC-Talker 2020-02-20 end */
            if ($codeArray["SIDOU_INPUT_INF"] == "1") {
                $row[$name] = knjCreateCombo($objForm, $name."-".$counter, $slumpRow["MARK"], $optMark, "aria-label=\"$readLabel1\"", 1);
                //CSV書出
                $csv[] = $slumpRow["MARK"];
                //入力エリアとキーをセットする
                $objUp->setElementsValue($name."-".$counter, "追指導", $csvKey);
            } else {
                $extra = "aria-label=\"$readLabel1\" id=\"{$name}-{$counter}\" STYLE=\"text-align: right;\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\"";
                $row[$name] = knjCreateTextBox($objForm, $slumpRow["SCORE"], $name."-".$counter, 3, 3, $extra);
                //テキストの名前をセット
                if ($counter == 0) {
                    $textFieldName .= $textSep.$name;
                    $textSep = ",";
                }
                //CSV書出
                $csv[] = $slumpRow["SCORE"];
                //入力エリアとキーをセットする
                $objUp->setElementsValue($name."-".$counter, "追指導", $csvKey);
            }
            //合格チェックボックス
            if ($model->Properties["useSlumpSeq"] == '2' && $testcd == "9990008") {
                $chk = ($slumpRow["PASS_FLG"] == '1') ? ' checked="checked" ' : '';
                $row["PASS_FLG"] = knjCreateCheckBox($objForm, "PASS_FLG"."-".$counter, "1", $chk);
            }
            //備考
            $name = "REMARK";
            /* Edit by Kaung for PC-Talker 2020-02-10 start */
            $readLabel2 = $row['ATTENDNO'].$row['SCHREGNO_SHOW'].$row['NAME_SHOW']."の備考(追指導理由等)全角20文字";
            /* Edit by Kaung for PC-Talker 2020-02-20 end */
            $extra = "aria-label=\"$readLabel2\" STYLE=\"WIDTH:95%\" WIDTH=\"95%\" onChange=\"this.style.background='#ccffcc'\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\"";
            $row[$name] = knjCreateTextBox($objForm, $slumpRow["REMARK"], $name."-".$counter, 40, 20, $extra);
            //テキストの名前をセット
            if ($counter == 0) {
                $textFieldName .= $textSep.$name;
                $textSep = ",";
            }
            //CSV書出
            $csv[] = $slumpRow["REMARK"];
            //入力エリアとキーをセットする
            $objUp->setElementsValue($name."-".$counter, "備考", $csvKey);
        }
        $row["MEISAI"] = $meisai;

        //CSV書出
        $csv[] = $model->lastColumn;
        $objUp->addCsvValue($csv);

        $counter++;
        $arg["data"][] = $row;
    }
    $result->free();
    //テキストの名前を取得
    knjCreateHidden($objForm, "TEXT_FIELD_NAME", $textFieldName);
    //件数
    knjCreateHidden($objForm, "COUNTER", $counter);
    
    //縦計
    $scoreSum = $scoreAvg = $scoreMax = $scoreMin = "";
    foreach ($model->testcdArray as $key => $codeArray) {
        $testcd = $codeArray["TESTCD"];
        $col = "SCORE" .$testcd;
        $foot = array();
        if (isset($term_data[$col])) {
            //合計
            $foot["SUM"] = array_sum($term_data[$col]);
            //平均
            $foot["AVG"] = round((array_sum($term_data[$col])/get_count($term_data[$col]))*10)/10;
            //最高点と最低点を求める
            array_multisort($term_data[$col], SORT_NUMERIC);
            $max = get_count($term_data[$col])-1;
            //最高点
            $foot["MAX"] = $term_data[$col][$max];
            //最低点
            $foot["MIN"] = $term_data[$col][0];
        } else {
            //合計
            $foot["SUM"] = "";
            //平均
            $foot["AVG"] = "";
            //最高点
            $foot["MAX"] = "";
            //最低点
            $foot["MIN"] = "";
        }
        $scoreSum .= "<th width=120 >".$foot["SUM"]."</th> ";
        $scoreAvg .= "<th width=120 >".$foot["AVG"]."</th> ";
        $scoreMax .= "<th width=120 >".$foot["MAX"]."</th> ";
        $scoreMin .= "<th width=120 >".$foot["MIN"]."</th> ";
    }
    //合計
    $arg["SCORE_SUM"] = $scoreSum;
    //平均
    $arg["SCORE_AVG"] = $scoreAvg;
    //最高点
    $arg["SCORE_MAX"] = $scoreMax;
    //最低点
    $arg["SCORE_MIN"] = $scoreMin;

    //累積現在日
    $cur_date = $db->getRow(knjd128v_2Query::getMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
    if (is_array($cur_date)) {
        $arg["CUR_DATE"] = $cur_date["YEAR"]."年度".$model->control["学期名"][$cur_date["SEMESTER"]]."<BR>".(int)$cur_date["MONTH"]."月".$cur_date["APPOINTED_DAY"]."日現在";
    }

    /********************************/
    /* 成績入力完了チェックボックス */
    /********************************/
    //初期化
    $chkComp = "";
    foreach ($model->testcdArray as $key => $codeArray) {
        $testcd = $codeArray["TESTCD"];
        $controlFlg = $codeArray["CONTROL_FLG"];

        $chk = '';
        $dis = '';
        $query = knjd128v_2Query::getRecordChkfinDat($model, $testcd);
        $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($resultRow)) {
            if ($resultRow["EXECUTED"] == '1') {
                $chk = ' checked="checked" ';
            } else {
                $chk = '';
            }
        }
        $dis = $controlFlg == "1" ? '' : ' disabled="disabled" ';
        $readChkbox = $codeArray['SEMESTERNAME'].$codeArray['TESTITEMNAME']."の成績入力完了";
        $val = knjCreateCheckBox($objForm, "CHK_COMP".$testcd, "on", "aria-label=\"$readChkbox\"".$chk.$dis);

        $chkComp .= "<th width=120 >".$val."</th> ";
    }
    $arg["CHK_COMP"] = $chkComp;

    //テキストの名前を取得する
    $textFieldName = $chkCompName = $testItemName = "";
    $textSep = "";
    foreach ($model->testcdArray as $key => $codeArray) {
        $col = "SCORE" .$codeArray["TESTCD"];
        $textFieldName .= $textSep.$col;
        $chkCompName .= $textSep."CHK_COMP".$codeArray["TESTCD"];
        $testItemName .= $textSep.$codeArray["TESTITEMNAME"];
        $textSep = ",";
    }
    knjCreateHidden($objForm, "TEXT_FIELD_NAME2", $textFieldName);
    knjCreateHidden($objForm, "CHK_COMP_NAME", $chkCompName);
    knjCreateHidden($objForm, "TEST_ITEM_NAME", $testItemName);

    //CSVファイルアップロードコントロール
    $arg["FILE"] = $objUp->toFileHtml($objForm);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $cnt = 0;
    if ($blank == "blank") {
        $opt[] = array('label' => "", 'value' => "");
    }
    if ($blank == "blank") {
        $cnt++;
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
        if ($name == "SLUMP_SEQ") {
            knjCreateHidden($objForm, "LIST_SLUMP_SEQ" . $row["VALUE"], $cnt);
            $cnt++;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //更新ボタン
    /* Edit by Kaung for PC-Talker 2020-02-10 start */
    $extra = "aria-label=\"更新\" id=\"btn_update\" onclick=\"current_cursor('btn_update');return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "aria-label=\"取消\" id=\"btn_reset\" onclick=\"current_cursor('btn_reset');return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    //リンク先のURL
    $jump  = "";
    if ($model->prgid == "KNJD128V") {
        $jump  = REQUESTROOT."/D/KNJD128V/knjd128vindex.php";
    } elseif ($model->prgid == "KNJD129V") {
        $jump  = REQUESTROOT."/D/KNJD129V/knjd129vindex.php";
    } elseif ($model->prgid == "KNJD129D") {
        $jump  = REQUESTROOT."/D/KNJD129D/knjd129dindex.php";
    } elseif ($model->prgid == "KNJD129L") {
        $jump  = REQUESTROOT."/D/KNJD129L/knjd129lindex.php";
    }
    $param = "?cmd=main&SUBCLASSCD={$model->field["SUBCLASSCD"]}&CHAIRCD={$model->field["CHAIRCD"]}";
    if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
        $param .= "&COURSE_MAJOR={$model->field["COURSE_MAJOR"]}";
    }
    $extra = "aria-label=\"戻る\" onClick=\"openOyagamen('{$jump}{$param}');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
    //印刷ボタン
    $extra = "aria-label=\"印刷\" id=\"btn_print\" onclick=\"current_cursor('btn_print');return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
    /* Edit by Kaung for PC-Talker 2020-02-20 end */
}

//hidden作成
function makeHidden(&$objForm, $model, $execute_date)
{
    knjCreateHidden($objForm, "cmd");
    //印刷パラメータ
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD128V_2");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "STAFF", STAFFCD);
    knjCreateHidden($objForm, "CHIKOKU_HYOUJI_FLG", $model->chikokuHyoujiFlg);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "gen_ed", substr($model->field["SUBCLASSCD"], 0, 2) == $model->gen_ed ? $model->gen_ed : "");
    knjCreateHidden($objForm, "TEST_DATE", $execute_date);
    //更新権限チェック
    knjCreateHidden($objForm, "USER_AUTH", $model->auth);
    knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
    //考査種別
    knjCreateHidden($objForm, "TESTCD", $model->field["TESTCD"]);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
    knjCreateHidden($objForm, "z010name1", $model->z010name1);
    //追指導回数コンボ変更時、MSG108表示用
    knjCreateHidden($objForm, "SELECT_SLUMP_SEQ", $model->field["SLUMP_SEQ"]);
}
