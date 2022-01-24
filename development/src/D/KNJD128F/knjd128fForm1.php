<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd128fForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //CSV
        $objUp = new csvFile();

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd128findex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //科目コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjd128fQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //各学期-9900-00の欄で、11,22,33と入力する科目
        $subcdABC = $db->getOne(knjd128fQuery::getNameMstD065($model));
        knjCreateHidden($objForm, "subcdABC", $subcdABC);

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"MOVE_ENTER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //講座コンボ
        $extra = "onChange=\"btn_submit('chaircd')\";";
        $query = knjd128fQuery::selectChairQuery($model);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");

        //明細ヘッダデータ作成
        $model->testcdArray = makeHead($objForm, $arg, $db, $model);

        //明細データ作成
        $execute_date = makeMeisai($objForm, $arg, $db, $model, $objUp);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $execute_date);

        //hidden作成
        makeHidden($objForm, $model, $execute_date);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd128fForm1.html", $arg);
    }
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model)
{
    $count = 0;
    $sem = "";
    $col = 0;
    $semArray = array();
    $head1 = "";
    $head2 = "";
    $testcdArray = array();
    //リンク元のプログラムＩＤ
    $prgid = "KNJD128F";
    $auth = AUTHORITY;
    //リンク先のURL
    $jump = REQUESTROOT."/D/KNJD128F_2/knjd128f_2index.php";
    $result = $db->query(knjd128fQuery::getTestName($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $count++;
        if ($sem != $row["SEMESTER"]) {
            $sem = $row["SEMESTER"];
            $col = 0;
        }
        $col++;
        $semArray[$row["SEMESTER"]] = "<th colspan={$col} ><font size=2>".$row["SEMESTERNAME"]."</font></th> ";

        //URLパラメータ
        $testcd5 = substr($row["TESTCD"], 0, 5);
        $param  = "?prgid={$prgid}";
        $param .= "&auth={$auth}";
        $param .= "&SUBCLASSCD={$model->field["SUBCLASSCD"]}";
        $param .= "&CHAIRCD={$model->field["CHAIRCD"]}";
        $param .= "&TESTCD={$testcd5}";
        //考査名にリンクを作成
        $extra = "onClick=\"openKogamen('{$jump}{$param}');\"";
        $testNameLink = ($row["CONTROL_FLG"] == "1") ? View::alink("#", htmlspecialchars($row["TESTITEMNAME"]), $extra) : $row["TESTITEMNAME"];

        $head2 .= "<th width=75 ><font size=2>".$testNameLink."</font></th> ";
        $testcdArray[] = array("TESTCD" => $row["TESTCD"], "SEMESTERNAME" => $row["SEMESTERNAME"], "TESTITEMNAME" => $row["TESTITEMNAME"], "CONTROL_FLG" => $row["CONTROL_FLG"], "SEMESTER" => $row["SEMESTER"], "SDATE" => $row["SDATE"], "EDATE" => $row["EDATE"]);
    }
    $result->free();
    foreach ($semArray as $key => $val) {
        $head1 .= $val;
    }
    $arg["HEAD1"] = $head1; //学期名称
    $arg["HEAD2"] = $head2; //考査種別名称
    $arg["ALL_WIDTH"] = 1280; //画面全体幅
    $all_width = 225 + $count * 75;
//    $arg["ALL_WIDTH"] = (1280 < $all_width) ? $all_width : 1280; //画面全体幅
    $arg["FOOT_COLSPAN"] = 1 + $count; //画面全体幅
    return $testcdArray;
}

//明細データ
function makeMeisai(&$objForm, &$arg, $db, &$model, &$objUp)
{
    /*******/
    /* CSV */
    /*******/
    $subclassname = $db->getOne(knjd128fQuery::getSubclassName($model));
    $chairname = $db->getOne(knjd128fQuery::getChairName($model));
    //CSV出力ファイル名
    $objUp->setFileName(CTRL_YEAR."年度_".$subclassname."_".$chairname."_"."成績入力.csv");
    //CSVヘッダ名
    $csvHeader = array("0" => "科目コード",
                       "1" => "科目名",
                       "2" => "講座コード",
                       "3" => "講座名",
                       "4" => "学籍番号",
                       "5" => "クラス－出席番",
                       "6" => "氏名");
    $setType = array();
    $setSize = array();
    $setCnt = get_count($csvHeader);
    foreach ($model->testcdArray as $key => $codeArray) {
        $csvHeader[$codeArray["TESTCD"]] = $codeArray["SEMESTERNAME"]."－".$codeArray["TESTITEMNAME"];
        $setType[$setCnt] = 'S';
        $setSize[$setCnt] = 3;
        $setCnt++;
    }
    $csvHeader[] = "LASTCOLUMN";
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

    //時間割講座テストより試験日を抽出
    $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
    $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
    //初期値
    $execute_date = ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) ? CTRL_DATE : $edate;

    //初期化
    $model->data = array();
    $counter = 0;

    //一覧表示
    $colorFlg = false;
    $query = knjd128fQuery::getScore($model, $execute_date);
    $result = $db->query($query);
    $scoreArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号を配列で取得
        $scoreArray[$row["SCHREGNO"]][$row["TESTCD"]] = $row;
    }
    $result->free();

    $result = $db->query(knjd128fQuery::selectQuery($model, $execute_date));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号をHiddenで保持
        knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);
        //クラス-出席番(表示)
        if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
            $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
        }

        //書出用CSVデータ
        $csv = array($model->field["SUBCLASSCD"],
                     $subclassname,
                     $model->field["CHAIRCD"],
                     $chairname,
                     $row["SCHREGNO"],
                     $row["ATTENDNO"],
                     $row["NAME_SHOW"]);
        //キー値をセット
        $csvKey = array("科目コード" => $model->field["SUBCLASSCD"],
                        "講座コード" => $model->field["CHAIRCD"],
                        "学籍番号"   => $row["SCHREGNO"]);
        //名前
        //$row["NAME_SHOW"] = $row["SCHREGNO"] ." ". $row["NAME_SHOW"];
        //５行毎に背景色を変更
        if ($counter % 5 == 0) {
            $colorFlg = !$colorFlg;
        }
        
        //入力可能なテキストの名前を取得する
        $setTextField = "";
        $textSep = "";
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];
            $col = "SCORE" .$testcd;
            //管理者コントロール
            if ($controlFlg == "1") {
                $setTextField .= $textSep.$col."-";
                $textSep = ",";
            }
        }
        
        $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
        //各項目を作成
        $meisai = "";
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];
            $col = "SCORE" .$testcd;
            $row[$col."_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            //異動情報
            if ($codeArray["SEMESTER"] != "9" && (strlen($row["TRANSFER_SDATE"]) || strlen($row["TRANSFER_EDATE"]))) {
                //学期期間中すべて異動期間の場合
                if (strtotime($row["TRANSFER_SDATE"]) <= strtotime($codeArray["SDATE"])
                          && strtotime($row["TRANSFER_EDATE"]) >= strtotime($codeArray["EDATE"])) {
                    $row[$col."_COLOR"]="#ffff00";
                //一部
                } elseif ((strtotime($row["TRANSFER_SDATE"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_SDATE"]) <= strtotime($codeArray["EDATE"]))) {
                    $row[$col."_COLOR"]="#ffff00";
                } elseif ((strtotime($row["TRANSFER_EDATE"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_EDATE"]) <= strtotime($codeArray["EDATE"]))) {
                    $row[$col."_COLOR"]="#ffff00";
                }
                //卒業日付
            } elseif ($codeArray["SEMESTER"] != "9" && strlen($row["GRD_DATE"])) {
                //学期期間中すべて卒業の場合(学期開始日付以前に卒業している場合）
                if (strtotime($row["GRD_DATE"]) <= strtotime($codeArray["SDATE"])) {
                    $row[$col."_COLOR"]="#ffff00";
                //一部
                } elseif (strtotime($row["GRD_DATE"]) > strtotime($codeArray["SDATE"])
                         && strtotime($row["GRD_DATE"]) <= strtotime($codeArray["EDATE"])) {
                    $row[$col."_COLOR"]="#ffff00";
                }
            }
            //成績データ
            $scoreRow = $scoreArray[$row["SCHREGNO"]][$testcd];
            $score = "";
            if (is_array($scoreRow)) {
                $score = $scoreRow["VALUE_DI"] == "*" ? $scoreRow["VALUE_DI"] : $scoreRow["SCORE"];
            }
            //縦計項目
            if (is_numeric($score)) {
                $term_data[$col][] = $score;
            }

            //CSV書出
            $csv[] = $score;

            //管理者コントロール
            if ($controlFlg == "1") {
                //テキストボックスを作成
                $row["FONTCOLOR"] = "#000000";
                $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" id=\"{$col}-{$counter}\"";
                $row[$col] = knjCreateTextBox($objForm, $score, $col."-".$counter, 3, 3, $extra);
                //考査満点マスタ
                $query = knjd128fQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $testcd, $row["GRADE"], $row["COURSE"], $model);
                $perfect = ($model->Properties["usePerfect"] == 'true') ? $db->getOne($query) : 100;
                if ($perfect == "") {
                    $perfect = 100;
                }
                //満点チェック用
                knjCreateHidden($objForm, $col."_PERFECT"."-".$counter, $perfect);
                //入力エリアとキーをセットする
                $objUp->setElementsValue($col."-".$counter, $csvHeader[$testcd], $csvKey);
            //ラベルのみ
            } else {
                $row["FONTCOLOR"] = "#000000";
                $row[$col] = "<font color={$row["FONTCOLOR"]}>".$score."</font>";
                //hidden
                knjCreateHidden($objForm, $col."-".$counter, $score);
            }
            $row["BGCOLOR"] = $row[$col."_COLOR"];
            $meisai .= "<td width=75 align=\"center\" bgcolor={$row["BGCOLOR"]}>".$row[$col]."</td>";
        }
        $row["MEISAI"] = $meisai;

        //CSV書出
        $csv[] = "LASTCOLUMN";
        $objUp->addCsvValue($csv);

        $counter++;
        $arg["data"][] = $row;
    }
    $result->free();
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
        $scoreSum .= "<th width=75 >".$foot["SUM"]."</th> ";
        $scoreAvg .= "<th width=75 >".$foot["AVG"]."</th> ";
        $scoreMax .= "<th width=75 >".$foot["MAX"]."</th> ";
        $scoreMin .= "<th width=75 >".$foot["MIN"]."</th> ";
    }
    //合計
    $arg["SCORE_SUM"] = $scoreSum;
    //平均
    $arg["SCORE_AVG"] = $scoreAvg;
    //最高点
    $arg["SCORE_MAX"] = $scoreMax;
    //最低点
    $arg["SCORE_MIN"] = $scoreMin;

    /********************************/
    /* 成績入力完了チェックボックス */
    /********************************/
    //初期化
    $chkComp = "";
    foreach ($model->testcdArray as $key => $codeArray) {
        $testcd = $codeArray["TESTCD"];
        $controlFlg = $codeArray["CONTROL_FLG"];
        $recordDiv = (substr($testcd, 1, 2) == "99") ? '2' : '1'; //1:素点 2:評定・評価

        $chk = '';
        $dis = '';
        $query = knjd128fQuery::getRecordChkfinDat($model, $testcd, $recordDiv);
        $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($resultRow)) {
            if ($resultRow["EXECUTED"] == '1') {
                $chk = ' checked="checked" ';
            } else {
                $chk = '';
            }
        }
        $dis = $controlFlg == "1" ? '' : ' disabled="disabled" ';
        $val = knjCreateCheckBox($objForm, "CHK_COMP".$testcd, "on", $chk.$dis);

        $chkComp .= "<th width=75 >".$val."</th> ";
    }
    $arg["CHK_COMP"] = $chkComp;

    //テキストの名前を取得する
    $textFieldName = "";
    $textSep = "";
    foreach ($model->testcdArray as $key => $codeArray) {
        $col = "SCORE" .$codeArray["TESTCD"];
        $textFieldName .= $textSep.$col;
        $textSep = ",";
    }
    knjCreateHidden($objForm, "TEXT_FIELD_NAME", $textFieldName);

    //CSVファイルアップロードコントロール
    $arg["FILE"] = $objUp->toFileHtml($objForm);

    return $execute_date;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "blank") {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
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
function makeBtn(&$objForm, &$arg, $model, $execute_date)
{
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $execute_date)
{
    knjCreateHidden($objForm, "cmd");
    //印刷パラメータ
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD128F");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "STAFF", STAFFCD);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "gen_ed", substr($model->field["SUBCLASSCD"], 0, 2) == $model->gen_ed ? $model->gen_ed : "");
    knjCreateHidden($objForm, "TEST_DATE", $execute_date);

    knjCreateHidden($objForm, "H_SUBCLASSCD");
    knjCreateHidden($objForm, "H_CHAIRCD");
    //更新権限チェック
    knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
    knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}
