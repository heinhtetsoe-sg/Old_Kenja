<?php

require_once('for_php7.php');


class knjd128lForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd128lindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //科目コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjd128lQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //基本設定のコードを取得
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->field["SUBCLASSCD"]) {
                $subclassArray = array();
                $subclassArray = explode("-", $model->field["SUBCLASSCD"]);
                $schoolKind = $subclassArray[1];
            } else {
                $schoolKind = $db->getOne(knjd128lQuery::getSchoolkindQuery());
            }
            $model->subclassAll = "00-".$schoolKind."-00-000000";
        } else {
            $model->subclassAll = "000000";
        }

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"MOVE_ENTER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //講座コンボ
        $extra = "onChange=\"btn_submit('chaircd')\";";
        $query = knjd128lQuery::selectChairQuery($model);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");

        //明細ヘッダデータ作成
        $model->testcdArray = makeHead($objForm, $arg, $db, $model);

        //算出元テスト取得
        $model->testcdMotoArray = makeTestMoto($objForm, $arg, $db, $model);

        //明細データ作成
        $execute_date = makeMeisai($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $execute_date);

        //hidden作成
        makeHidden($objForm, $model, $execute_date);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd128lForm1.html", $arg);
    }
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model) {
    //科目別設定があるか
    $testSubCnt = $db->getOne(knjd128lQuery::getTestSubCnt($model));
    if ($testSubCnt == "") $testSubCnt = 0;

    $count = 0;
    $sem = "";
    $col = 0;
    $semArray = array();
    $head1 = "";
    $head2 = "";
    $testcdArray = array();
    //リンク元のプログラムＩＤ
    $prgid = "KNJD128L";
    $auth = AUTHORITY;
    //リンク先のURL
    $jump = REQUESTROOT."/D/KNJD128L_2/knjd128l_2index.php";
    $result = $db->query(knjd128lQuery::getTestName($model, $testSubCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $count++;
        if ($sem != $row["SEMESTER"]) {
            $sem = $row["SEMESTER"];
            $col = 0;
        }
        $col++;
        $semArray[$row["SEMESTER"]] = "<th colspan={$col} ><font size=2>".$row["SEMESTERNAME"]."</font></th> ";

        //URLパラメータ
        $param  = "?prgid={$prgid}";
        $param .= "&auth={$auth}";
        $param .= "&SUBCLASSCD={$model->field["SUBCLASSCD"]}";
        $param .= "&CHAIRCD={$model->field["CHAIRCD"]}";
        $param .= "&TESTCD={$row["TESTCD"]}";
        //考査名にリンクを作成
        $extra = "onClick=\"openKogamen('{$jump}{$param}');\"";
        $testNameLink = ($row["SIDOU_INPUT"] == "1") ? View::alink("#", htmlspecialchars($row["TESTITEMNAME"]), $extra) : $row["TESTITEMNAME"];

        if ($row["TESTCD"] == "9990009") {
            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                $head2 .= "<th rowspan=2 width=25><font size=2>仮<br>評<br>定</font></th> ";
                $count++;
            }
        }
        $head2 .= "<th rowspan=2 width=60 ><font size=2>".$testNameLink."</font></th> ";
        if ($row["TESTCD"] == "9990009") {
            $head2 .= "<th rowspan=2 width=60 ><font size=2>履修単位</font></th> ";
            $head2 .= "<th rowspan=2 width=60 ><font size=2>修得単位</font></th> ";
            $count++;
            $count++;
        }
        $testcdArray[] = array("TESTCD" => $row["TESTCD"], "SEMESTERNAME" => $row["SEMESTERNAME"], "TESTITEMNAME" => $row["TESTITEMNAME"], "SIDOU_INPUT" => $row["SIDOU_INPUT"], "SIDOU_INPUT_INF" => $row["SIDOU_INPUT_INF"], "CONTROL_FLG" => $row["CONTROL_FLG"], "SEMESTER" => $row["SEMESTER"], "SDATE" => $row["SDATE"], "EDATE" => $row["EDATE"]);
    }
    $result->free();
    foreach ($semArray as $key => $val) {
        $head1 .= $val;
    }
    $arg["HEAD1"] = $head1; //学期名称
    $arg["HEAD2"] = $head2; //考査種別名称
//    $arg["ALL_WIDTH"] = 1280; //画面全体幅
    $arg["ALL_WIDTH"] = "100%"; //画面全体幅
    $arg["FOOT_COLSPAN"] = 2 + $count; //画面全体幅
    return $testcdArray;
}

//算出元テスト
function makeTestMoto(&$objForm, &$arg, $db, $model) {
    //パーツ
    $testcdPartArray = array();
    foreach ($model->testcdArray as $key => $codeArray) {
        $testcdPartArray[] = $codeArray["TESTCD"];
    }

    //算出元科目別設定があるか
    $testSubCnt = $db->getOne(knjd128lQuery::getTestMotoSubCnt($model));
    if ($testSubCnt == "") $testSubCnt = 0;

    $testcdMotoArray = array();
    $result = $db->query(knjd128lQuery::getTestMoto($model, $testSubCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["TESTCD"], $testcdPartArray) && in_array($row["MOTO_TESTCD"], $testcdPartArray) && ($row["SEMESTER"] == "9" || $row["SEMESTER"] == CTRL_SEMESTER)) {
            $testcdMotoArray[$row["TESTCD"]][] = array("TESTCD" => $row["TESTCD"], "TESTNAME" => $row["TESTNAME"], "MOTO_TESTCD" => $row["MOTO_TESTCD"], "MOTO_TESTNAME" => $row["MOTO_TESTNAME"], "SEMESTER" => $row["SEMESTER"]);
        }
    }
    $result->free();
    return $testcdMotoArray;
}

//明細データ
function makeMeisai(&$objForm, &$arg, $db, &$model) {
    //学校詳細マスタ
    $schoolRow = array();
    $schoolRow = $db->getRow(knjd128lQuery::getVSchoolMst(), DB_FETCHMODE_ASSOC);
    //時間割講座テストより試験日を抽出
    $sdate = str_replace("/","-",$model->control["学期開始日付"][CTRL_SEMESTER]);
    $edate = str_replace("/","-",$model->control["学期終了日付"][CTRL_SEMESTER]);
    if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
        $execute_date = CTRL_DATE;//初期値
    } else {
        $execute_date = $edate;//初期値
    }

    //初期化
    $model->data = array();
    $counter = 0;

    //一覧表示
    $colorFlg = false;
    $result = $db->query(knjd128lQuery::selectQuery($model, $execute_date));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号をHiddenで保持
        knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);
        //クラス-出席番(表示)
        if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
            $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
        }
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
            //履修単位・修得単位
            if ($controlFlg == "1" && $testcd == "9990009" && $model->Properties["notOpenCredit"] != '1') {
                $setTextField .= $textSep."COMP_CREDIT-";
                $setTextField .= $textSep."GET_CREDIT-";
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
            //$row[$col."_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            //成績データ
            $scoreRow = array();
            $query = knjd128lQuery::getScore(CTRL_YEAR, $testcd, $row["SCHREGNO"], $model);
            $scoreRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $score = $scoreRow["VALUE_DI"] == "*" ? $scoreRow["VALUE_DI"] : $scoreRow["SCORE"];
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
            //卒業日付
            } elseif ($testcd != "9990009" && strlen($row["GRD_DATE"])) {
                //学期期間中すべて卒業の場合(学期開始日付以前に卒業している場合）
                if (strtotime($row["GRD_DATE"]) <= strtotime($codeArray["SDATE"])) {
                    $colorFlgYellow = true;
                //一部
                } elseif (strtotime($row["GRD_DATE"]) >  strtotime($codeArray["SDATE"])
                       && strtotime($row["GRD_DATE"]) <= strtotime($codeArray["EDATE"])) {
                    $colorFlgYellow = true;
                }
            }

            //追指導データ 1:追指導入力
            $colorFlgPink = false;
            $fontcolorFlgRed = false;
            //管理者コントロール
            if ($controlFlg == "1") {
                //テキストボックスを作成
                $row["FONTCOLOR"] = $fontcolorFlgRed ? "#ff0000" : "#000000";
                $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" id=\"{$col}-{$counter}\"";
                $row[$col] = knjCreateTextBox($objForm, $score, $col."-".$counter, 3, 3, $extra);
                //考査満点マスタ
                $query = knjd128lQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $testcd, $row["GRADE"], $row["COURSE"], $model);
                $perfect = ($model->usePerfect == 'true' && substr($testcd, 5, 2) == '01') ? $db->getOne($query) : 100;
                if ($perfect == "") $perfect = 100;
                //満点チェック用
                knjCreateHidden($objForm, $col."_PERFECT"."-".$counter, $perfect);
                //履修単位・修得単位
                if ($testcd == "9990009") {
                    if ($model->Properties["notOpenCredit"] != '1') {
                        $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\" id=\"COMP_CREDIT-{$counter}\" ";
                        $row["COMP_CREDIT"] = knjCreateTextBox($objForm, $scoreRow["COMP_CREDIT"], "COMP_CREDIT"."-".$counter, 3, 2, $extra);
                        $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\" id=\"GET_CREDIT-{$counter}\" ";
                        $row["GET_CREDIT"] = knjCreateTextBox($objForm, $scoreRow["GET_CREDIT"], "GET_CREDIT"."-".$counter, 3, 2, $extra);
                        knjCreateHidden($objForm, "COMP_CREDIT"."_PERFECT"."-".$counter, 100);
                        knjCreateHidden($objForm, "GET_CREDIT"."_PERFECT"."-".$counter, 100);
                    } else {
                        $row["COMP_CREDIT"] = "<font color=\"#000000\">".$scoreRow["COMP_CREDIT"]."</font>";
                        $row["GET_CREDIT"] = "<font color=\"#000000\">".$scoreRow["GET_CREDIT"]."</font>";
                        knjCreateHidden($objForm, "COMP_CREDIT"."-".$counter, $scoreRow["COMP_CREDIT"]);
                        knjCreateHidden($objForm, "GET_CREDIT"."-".$counter, $scoreRow["GET_CREDIT"]);
                    }
                }
            //ラベルのみ
            } else {
                $row["FONTCOLOR"] = $fontcolorFlgRed ? "#ff0000" : "#000000";
                $row[$col] = "<font color={$row["FONTCOLOR"]}>".$score."</font>";
                //hidden
                knjCreateHidden($objForm, $col."-".$counter, $score);
                //履修単位・修得単位
                if ($testcd == "9990009") {
                    $row["COMP_CREDIT"] = "<font color=\"#000000\">".$scoreRow["COMP_CREDIT"]."</font>";
                    $row["GET_CREDIT"] = "<font color=\"#000000\">".$scoreRow["GET_CREDIT"]."</font>";
                    knjCreateHidden($objForm, "COMP_CREDIT"."-".$counter, $scoreRow["COMP_CREDIT"]);
                    knjCreateHidden($objForm, "GET_CREDIT"."-".$counter, $scoreRow["GET_CREDIT"]);
                }
            }
            //仮評定
            if ($testcd == "9990009") {
                if ($model->Properties["useProvFlg"] == '1') {
                    $chk = $row["PROV_FLG"] == '1' ? ' checked="checked" ' : '';
                    $dis = $controlFlg == '1' ? '' : ' disabled="disabled" ';
                    $row["PROV_FLG"] = knjCreateCheckBox($objForm, "PROV_FLG"."-".$counter, "1", $chk.$dis);
                    $meisai .= "<td width=25 align=\"center\" bgcolor={$row["COLOR"]}>".$row["PROV_FLG"]."</td>";
                }
            }
            $row["BGCOLOR"] = $row["COLOR"]; //通常の背景色
            if ($testcd != "9990009" && $colorFlgYellow) $row["BGCOLOR"] = "#ffff00"; //異動
            if ($colorFlgPink)   $row["BGCOLOR"] = "#ffc0cb"; //指導
            $meisai .= "<td width=60 align=\"center\" bgcolor={$row["BGCOLOR"]}>".$row[$col]."</td>";
            //履修単位・修得単位
            if ($testcd == "9990009") {
                $meisai .= "<td width=60 align=\"center\" bgcolor={$row["COLOR"]}>".$row["COMP_CREDIT"]."</td>";
                $meisai .= "<td width=60 align=\"center\" bgcolor={$row["COLOR"]}>".$row["GET_CREDIT"]."</td>";
            }
        }
        $row["MEISAI"] = $meisai;
        //累積情報
        if ($schoolRow["ABSENT_COV"] == "0" || $schoolRow["ABSENT_COV"] == "2" || $schoolRow["ABSENT_COV"] == "4" || $schoolRow["ABSENT_COV"] == "5") { 
            $query = knjd128lQuery::getAttendData($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model);
        } else {
            $query = knjd128lQuery::getAttendData2($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model);
        }
        $attendRow = array();
        $attendRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $t_lateearly = ($model->chikokuHyoujiFlg == 1) ? $attendRow["LATE_EARLY"] : $attendRow["T_LATEEARLY"];
        //$row["LESSON"]      = strlen($attendRow["LESSON"])      ? $attendRow["LESSON"]      : "0";
        $row["T_NOTICE"]    = strlen($attendRow["T_NOTICE"])    ? $attendRow["T_NOTICE"]    : "0";
        $row["T_LATEEARLY"] = strlen($t_lateearly)              ? $t_lateearly              : "0";
        $row["NOTICE_LATE"] = strlen($attendRow["NOTICE_LATE"]) ? $attendRow["NOTICE_LATE"] : "0";

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
            array_multisort ($term_data[$col], SORT_NUMERIC);
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
        //仮評定
        if ($testcd == "9990009") {
            if ($model->Properties["useProvFlg"] == '1') {
                $scoreSum .= "<th width=25 ></th> ";
                $scoreAvg .= "<th width=25 ></th> ";
                $scoreMax .= "<th width=25 ></th> ";
                $scoreMin .= "<th width=25 ></th> ";
            }
        }
        $scoreSum .= "<th width=60 >".$foot["SUM"]."</th> ";
        $scoreAvg .= "<th width=60 >".$foot["AVG"]."</th> ";
        $scoreMax .= "<th width=60 >".$foot["MAX"]."</th> ";
        $scoreMin .= "<th width=60 >".$foot["MIN"]."</th> ";
        //履修単位・修得単位
        if ($testcd == "9990009") {
            $scoreSum .= "<th width=60 ></th> ";
            $scoreAvg .= "<th width=60 ></th> ";
            $scoreMax .= "<th width=60 ></th> ";
            $scoreMin .= "<th width=60 ></th> ";
            $scoreSum .= "<th width=60 ></th> ";
            $scoreAvg .= "<th width=60 ></th> ";
            $scoreMax .= "<th width=60 ></th> ";
            $scoreMin .= "<th width=60 ></th> ";
        }
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
    $cur_date = $db->getRow(knjd128lQuery::GetMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
    $shuukei_date = str_replace("-", "/", CTRL_DATE);
    if (is_array($cur_date)) {
        $arg["CUR_DATE"] = $cur_date["YEAR"]."年度".$model->control["学期名"][$cur_date["SEMESTER"]]."<BR>".(int)$cur_date["MONTH"]."月".$cur_date["APPOINTED_DAY"]."日現在";
        $shuukei_year = ((int)$cur_date["MONTH"] <= 3) ? ($cur_date["YEAR"] + 1) : $cur_date["YEAR"];
        $shuukei_date = $shuukei_year . "/" . $cur_date["MONTH"] . "/" . $cur_date["APPOINTED_DAY"];
    }
    //印刷パラメータ(帳票KNJD620V)出欠集計日
    knjCreateHidden($objForm, "DATE", $shuukei_date);

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
        $query = knjd128lQuery::getRecordChkfinDat($model, $testcd);
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
        //仮評定
        if ($testcd == "9990009") {
            if ($model->Properties["useProvFlg"] == '1') {
                $chkComp .= "<th width=25 ></th> ";
            }
        }
        $chkComp .= "<th width=60 >".$val."</th> ";
        //履修単位・修得単位
        if ($testcd == "9990009") {
            $chkComp .= "<th width=60 ></th> ";
            $chkComp .= "<th width=60 ></th> ";
        }
    }
    $arg["CHK_COMP"] = $chkComp;

    //テキストの名前を取得する
    $textFieldName = "";
    $textSep = "";
    foreach ($model->testcdArray as $key => $codeArray) {
        $col = "SCORE" .$codeArray["TESTCD"];
        $textFieldName .= $textSep.$col;
        $textSep = ",";
        //履修単位・修得単位
        if ($codeArray["TESTCD"] == "9990009") {
            $textFieldName .= $textSep."COMP_CREDIT";
            $textFieldName .= $textSep."GET_CREDIT";
            $textSep = ",";
        }
    }
    knjCreateHidden($objForm, "TEXT_FIELD_NAME", $textFieldName);

    return $execute_date;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
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
function makeBtn(&$objForm, &$arg, $model, $execute_date) {
    //disabled
    $syukketuDisabled = $model->field["CHAIRCD"] && $model->field["SUBCLASSCD"] ? "" : " disabled ";
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $syukketuDisabled.$extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $syukketuDisabled.$extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "小 表", $syukketuDisabled.$extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $execute_date) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "STAFF", STAFFCD);
    knjCreateHidden($objForm, "CHIKOKU_HYOUJI_FLG", $model->chikokuHyoujiFlg);
    knjCreateHidden($objForm, "gen_ed", substr($model->field["SUBCLASSCD"],0,2) == $model->gen_ed ? $model->gen_ed : "");
    knjCreateHidden($objForm, "TEST_DATE", $execute_date);
    //印刷パラメータ(帳票KNJD620V)
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD128L");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRINT_DIV", "2"); //2:講座別
    knjCreateHidden($objForm, "REMARK_TESTCD", ""); //ブランク
    knjCreateHidden($objForm, "category_selected"); //講座

    knjCreateHidden($objForm, "H_SUBCLASSCD");
    knjCreateHidden($objForm, "H_CHAIRCD");
    //更新権限チェック
    knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
    knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}
?>
