<?php

require_once('for_php7.php');


class knjd129nForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd129nindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //科目コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjd129nQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

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
        $query = knjd129nQuery::selectChairQuery($model);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");

        //テスト取得(パーツ)
        $model->testcdArray = makeTest($objForm, $arg, $db, $model);

        //算出元テスト取得
        $model->testcdMotoArray = makeTestMoto($objForm, $arg, $db, $model);

        //明細ヘッダデータ作成
        makeHead($objForm, $arg, $db, $model);

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
        View::toHTML($model, "knjd129nForm1.html", $arg);
    }
}

//テスト取得(パーツ)
function makeTest(&$objForm, &$arg, $db, $model) {
    //科目別設定があるか
    $testSubCnt = $db->getOne(knjd129nQuery::getTestSubCnt($model));
    if ($testSubCnt == "") $testSubCnt = 0;
    //テスト取得(パーツ)
    $testcdArray = array();
    $result = $db->query(knjd129nQuery::getTestName($model, $testSubCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $testcdArray[$row["TESTCD"]]  = array("TESTCD"          => $row["TESTCD"],
                                              "TESTITEMNAME"    => $row["TESTITEMNAME"],
                                              "CONTROL_FLG"     => $row["CONTROL_FLG"],
                                              "SIDOU_INPUT"     => $row["SIDOU_INPUT"], 
                                              "SIDOU_INPUT_INF" => $row["SIDOU_INPUT_INF"], 
                                              "SEMESTER"        => $row["SEMESTER"],
                                              "SEMESTERNAME"    => $row["SEMESTERNAME"],
                                              "SDATE"           => $row["SDATE"],
                                              "EDATE"           => $row["EDATE"]
                                        );
    }
    $result->free();
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
    $testSubCnt = $db->getOne(knjd129nQuery::getTestMotoSubCnt($model));
    if ($testSubCnt == "") $testSubCnt = 0;

    $testcdMotoArray = array();
    $result = $db->query(knjd129nQuery::getTestMoto($model, $testSubCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["TESTCD"], $testcdPartArray) && in_array($row["MOTO_TESTCD"], $testcdPartArray)) {
            $testcdMotoArray[$row["TESTCD"]][]  = array("TESTCD"        => $row["TESTCD"],
                                                        "TESTNAME"      => $row["TESTNAME"],
                                                        "MOTO_TESTCD"   => $row["MOTO_TESTCD"],
                                                        "MOTO_TESTNAME" => $row["MOTO_TESTNAME"],
                                                        "SEMESTER"      => $row["SEMESTER"]
                                                        );
        }
    }
    $result->free();
    return $testcdMotoArray;
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model) {
    $count = 0; //明細ヘッダの列数
    $semArray = array();
    $sem = "";
    $colspan = 0;
    $head2 = "";
    $head9 = "";
    foreach ($model->testcdArray as $key => $codeArray) {
        //学期名称
        if ($sem != $codeArray["SEMESTER"]) {
            $sem = $codeArray["SEMESTER"];
            $colspan = 0;
        }
        $colspan++;
        if ($codeArray["TESTCD"] == "9990009") {
            $colspan++;
            $colspan++;
        }
        $semArray[$codeArray["SEMESTER"]] = "<th colspan={$colspan} ><font size=2>".$codeArray["SEMESTERNAME"]."</font></th> ";
        //算出ボタン
        if (get_count($model->testcdMotoArray[$codeArray["TESTCD"]])) {
            $testcdSaki = $codeArray["TESTCD"];
            $testcdMoto = "";
            $motoSep = "";
            foreach ($model->testcdMotoArray[$codeArray["TESTCD"]] as $motokey => $motoCodeArray) {
                $motoControlFlg = $model->testcdArray[$motoCodeArray["MOTO_TESTCD"]]["CONTROL_FLG"];
                $testcdMoto .= $motoSep.$motoCodeArray["MOTO_TESTCD"];
                $motoSep = ",";
            }
        }
        //考査名
        if ($codeArray["SIDOU_INPUT"] == "1") {
            //リンク元のプログラムＩＤ
            $prgid = "KNJD129N";
            $auth = AUTHORITY;
            //リンク先のURL
            $jump = REQUESTROOT."/D/KNJD128V_2/knjd128v_2index.php";
            //URLパラメータ
            $param  = "?prgid={$prgid}";
            $param .= "&auth={$auth}";
            $param .= "&SUBCLASSCD={$model->field["SUBCLASSCD"]}";
            $param .= "&CHAIRCD={$model->field["CHAIRCD"]}";
            $param .= "&TESTCD={$codeArray["TESTCD"]}";
            $extra = "onClick=\"openKogamen('{$jump}{$param}');\"";
            $testNameLink = View::alink("#", htmlspecialchars($codeArray["TESTITEMNAME"]), $extra);
        } else {
            $testNameLink = $codeArray["TESTITEMNAME"];
        }
        //学年末以外
        if ($codeArray["SEMESTER"] != "9") {
            $head2 .= "<th width=40 ><font size=2>{$testNameLink}</font></th> ";
            $count++;
        //学年末
        } else {
            //追試
            $head9 .= "<th rowspan=2 width=80 ><font size=2>追試</font></th> ";
            $count++;
            //仮評定フラグ対応
            if ($codeArray["TESTCD"] == "9990009" && $model->Properties["useProvFlg"] == '1') {
                $head9 .= "<th rowspan=2 width=40><font size=2>仮<br>評<br>定</font></th> ";
                $count++;
            }
            //学年成績・学年評定
            $head9 .= "<th rowspan=2 width=60 ><font size=2>{$testNameLink}</font></th> ";
            $count++;
            //履修単位・修得単位
            if ($codeArray["TESTCD"] == "9990009") {
                $head9 .= "<th rowspan=2 width=40 ><font size=2>履修<BR>単位</font></th> ";
                $head9 .= "<th rowspan=2 width=40 ><font size=2>修得<BR>単位</font></th> ";
                $count++;
                $count++;
            }
        }
    }
    //学年末以外
    $head1 = "";
    foreach ($semArray as $key => $val) {
        if ($key == "9") continue;
        $head1 .= $val;
    }
    $arg["HEAD1"] = $head1; //学期名称
    $arg["HEAD2"] = $head2; //考査種別名称
    $arg["HEAD9"] = $head9; //学年末
    $arg["ALL_WIDTH"] = 1200; //画面全体幅
    $arg["FOOT_COLSPAN"] = 2 + $count;
}

//明細データ
function makeMeisai(&$objForm, &$arg, $db, &$model) {
    //評定マスタ　学年成績を５段階に換算用を取得
    $query = knjd129nQuery::getAssessMst();
    $result = $db->query($query);
    $assess = "";
    $sep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $assess .= $sep . $row["ASSESSLEVEL"] ."-". $row["ASSESSLOW"] ."-". $row["ASSESSHIGH"];
        $sep = ",";
    }
    $result->free();
    knjCreateHidden($objForm, "ASSESS_LIST", $assess);

    //学校詳細マスタ
    $schoolRow = array();
    $schoolRow = $db->getRow(knjd129nQuery::getVSchoolMst(), DB_FETCHMODE_ASSOC);
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

    //成績と見込点を取得
    $query = knjd129nQuery::getScore($model, $execute_date);
    $result = $db->query($query);
    $scoreArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号を配列で取得
        $scoreArray[$row["SCHREGNO"]][$row["TESTCD"]] = $row;
    }
    $result->free();

    //一覧表示
    $colorFlg = false;
    $result = $db->query(knjd129nQuery::selectQuery($model, $execute_date));
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

        //編入生の成績は、いなかった学期の数を分母から外したい。
        //編入日から学期を取得する。（どの学期から）いつから編入したか判断したい・・・
        knjCreateHidden($objForm, "ENT_DIV5_SEMESTER"."-".$counter, ($model->isKasiwara == "1") ? $row["ENT_DIV5_SEMESTER"] : "");

        //転学・退学は、処理しない
        knjCreateHidden($objForm, "GRD_DIV23_SEMESTER"."-".$counter, $row["GRD_DIV23_SEMESTER"]);

        //入力可能なテキストの名前を取得する
        $setTextField = "";
        $textSep = "";
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];
            $col = "SCORE" .$testcd;

            //異動情報
            $colorFlgYellow = false;
            //留学のみ
            if ($testcd != "9990009" && (strlen($row["TRANSFER_SDATE1"]) || strlen($row["TRANSFER_EDATE1"]))) {
                //学期期間中すべて異動期間の場合
                if (strtotime($row["TRANSFER_SDATE1"]) <= strtotime($codeArray["SDATE"])
                 && strtotime($row["TRANSFER_EDATE1"]) >= strtotime($codeArray["EDATE"])) {
                    $colorFlgYellow = true;
                //一部
                } elseif ((strtotime($row["TRANSFER_SDATE1"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_SDATE1"]) <= strtotime($codeArray["EDATE"]))) {
                    $colorFlgYellow = true;
                } elseif ((strtotime($row["TRANSFER_EDATE1"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_EDATE1"]) <= strtotime($codeArray["EDATE"]))) {
                    $colorFlgYellow = true;
                }
            }
            //留学以外
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
            } elseif (strlen($row["GRD_DATE"])) {
                //学期期間中すべて卒業の場合(学期開始日付以前に卒業している場合）
                if (strtotime($row["GRD_DATE"]) <= strtotime($codeArray["SDATE"])) {
                    $colorFlgYellow = true;
                //一部
                } elseif (strtotime($row["GRD_DATE"]) >  strtotime($codeArray["SDATE"])
                       && strtotime($row["GRD_DATE"]) <= strtotime($codeArray["EDATE"])) {
                    $colorFlgYellow = true;
                }
            }

            //管理者コントロール
            if ($controlFlg == "1" && !strlen($scoreRow["SCORE_PASS"]) && false == $colorFlgYellow) {
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
            $scoreRow = $scoreArray[$row["SCHREGNO"]][$testcd];
            $score = strlen($scoreRow["VALUE_DI"]) ? $scoreRow["VALUE_DI"] : $scoreRow["SCORE"];
            //縦計項目
            if (is_numeric($score)) {
               $term_data[$col][] = $score;
            }

            //異動情報
            $colorFlgYellow = false;
            //留学のみ
            if ($testcd != "9990009" && (strlen($row["TRANSFER_SDATE1"]) || strlen($row["TRANSFER_EDATE1"]))) {
                //学期期間中すべて異動期間の場合
                if (strtotime($row["TRANSFER_SDATE1"]) <= strtotime($codeArray["SDATE"])
                 && strtotime($row["TRANSFER_EDATE1"]) >= strtotime($codeArray["EDATE"])) {
                    $colorFlgYellow = true;
                //一部
                } elseif ((strtotime($row["TRANSFER_SDATE1"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_SDATE1"]) <= strtotime($codeArray["EDATE"]))) {
                    $colorFlgYellow = true;
                } elseif ((strtotime($row["TRANSFER_EDATE1"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_EDATE1"]) <= strtotime($codeArray["EDATE"]))) {
                    $colorFlgYellow = true;
                }
            }
            //留学以外
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
            } elseif (strlen($row["GRD_DATE"])) {
                //学期期間中すべて卒業の場合(学期開始日付以前に卒業している場合）
                if (strtotime($row["GRD_DATE"]) <= strtotime($codeArray["SDATE"])) {
                    $colorFlgYellow = true;
                //一部
                } elseif (strtotime($row["GRD_DATE"]) >  strtotime($codeArray["SDATE"])
                       && strtotime($row["GRD_DATE"]) <= strtotime($codeArray["EDATE"])) {
                    $colorFlgYellow = true;
                }
            }

            //編入生の成績は、いなかった学期の数を分母から外したい。
            //編入日から学期を取得する。（どの学期から）いつから編入したか判断したい・・・
            if ($testcd != "9990009" && strlen($row["ENT_DATE"]) && $row["ENT_DIV"] == "5") {
                //学期期間中すべて編入の場合(学期開始日付以後に編入している場合）
                if (strtotime($row["ENT_DATE"]) >  strtotime($codeArray["EDATE"])) {
//                    $colorFlgYellow = true;
                }
                //一部
                if (strtotime($row["ENT_DATE"]) >= strtotime($codeArray["SDATE"]) &&
                    strtotime($row["ENT_DATE"]) <= strtotime($codeArray["EDATE"])) {
//                    $colorFlgYellow = true;
                }
            }

            $colorFlgScoreCalc = false;
            if (substr($testcd, -2) == "08") {
                //(green)自動計算と手入力の値が不一致の時、背景色をつけたい・・・
                if ($score !== $scoreRow["SCORE_KEEP"] && (strlen($score) || strlen($scoreRow["SCORE_KEEP"]))) {
                    $colorFlgScoreCalc = true;
                }
                knjCreateHidden($objForm, "SCORE_KEEP".$testcd."-".$counter, $scoreRow["SCORE_KEEP"]);
            }

            //管理者コントロール
            $query = knjd129nQuery::getPerfect(CTRL_YEAR, $testcd, $row["GRADE"], $row["COURSE"], $model);
            $model->perfectSchreg[$row["SCHREGNO"]] = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($controlFlg == "1" && substr($testcd, -2) !== "07" && !strlen($scoreRow["SCORE_PASS"]) && false == $colorFlgYellow) {
                //テキストボックスを作成
                $row["FONTCOLOR"] = "#000000";
                $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" id=\"{$col}-{$counter}\"";
                $row[$col] = knjCreateTextBox($objForm, $score, $col."-".$counter, 3, 3, $extra);
                //考査満点マスタ
                $perfect = "";
                if ($model->usePerfect == 'true') $perfect = $model->perfectSchreg[$row["SCHREGNO"]]["PERFECT"];
                if ($perfect == "") {
                    $perfect = 100;
                    if (substr($testcd, -2) == "02") $perfect = 10;//平常点【敬】
                    if (substr($testcd, -2) == "03") $perfect = 30;//観点別評価点【柏】
                }
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
                $row["FONTCOLOR"] = "#000000";
                //(pink)見込点は、あればラベル表示
                //但し、算出・更新に見込点は含めない（仕様未定のため、とりあえずこのまま）
                $tmpScore = strlen($scoreRow["SCORE_PASS"]) ? $scoreRow["SCORE_PASS"] : $score;
                //算出ボタンで出席点へセット用
                if (substr($testcd, -2) == "07") {
                    $spanId = $col."_ATTEND_ID"."-".$counter;
                    $row[$col] = "<font color={$row["FONTCOLOR"]}><span id={$spanId}>".$tmpScore."</span></font>";
                //算出ボタンで学年評定へセット用
                } else if ($testcd == "9990009") {
                    $spanId = $col."_ASSESS_ID"."-".$counter;
                    $row[$col] = "<font color={$row["FONTCOLOR"]}><span id={$spanId}>".$tmpScore."</span></font>";
                } else {
                    $row[$col] = "<font color={$row["FONTCOLOR"]}>".$tmpScore."</font>";
                }
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
            //hidden
            if (substr($testcd, -2) == "01") {
                knjCreateHidden($objForm, "SCORE_PASS".$testcd."-".$counter, $scoreRow["SCORE_PASS"]);
            }
            //追試
            $extra = " onKeyDown=\"keyChangeEntToTab2(this, {$counter})\"; STYLE=\"text-align: right; color:\"#000000\";\" onChange=\"this.style.background='#ccffcc'\" onblur=\"this.value=toInteger(this.value);\" id=\"SUPP_SCORE-{$counter}\" ";
            $row["SUPP_SCORE"] = knjCreateTextBox($objForm, $scoreRow["SUPP_SCORE"], "SUPP_SCORE"."-".$counter, 3, 3, $extra);
            $meisai .= "<td width=80 align=\"center\" bgcolor={$row["COLOR"]}>".$row["SUPP_SCORE"]."</td>";

            //仮評定
            if ($testcd == "9990009") {
                if ($model->Properties["useProvFlg"] == '1') {
                    $chk = $row["PROV_FLG"] == '1' ? 'レ' : '';
                    $row["PROV_FLG"] = $chk;
                    $meisai .= "<td width=40 align=\"center\" bgcolor={$row["COLOR"]}>".$row["PROV_FLG"]."</td>";
                }
            }
            $row["BGCOLOR"] = $row["COLOR"]; //通常の背景色
            if (substr($testcd, 0, 1) != "9" && $colorFlgYellow) $row["BGCOLOR"] = "#ffff00"; //異動
            if (strlen($scoreRow["SCORE_PASS"])) $row["BGCOLOR"] = "#ffc0cb"; //見込点(pink)
            if ($colorFlgScoreCalc) $row["BGCOLOR"] = "#ccffcc"; //自動計算と手入力の値が不一致(green)
            $meisai .= "<td width=60 align=\"center\" bgcolor={$row["BGCOLOR"]}>".$row[$col]."</td>";
            //履修単位・修得単位
            if ($testcd == "9990009") {
                $meisai .= "<td width=40 align=\"center\" bgcolor={$row["COLOR"]}>".$row["COMP_CREDIT"]."</td>";
                $meisai .= "<td width=40 align=\"center\" bgcolor={$row["COLOR"]}>".$row["GET_CREDIT"]."</td>";
            }
        }
        $row["MEISAI"] = $meisai;
        //累積情報
        if ($schoolRow["ABSENT_COV"] == "0" || $schoolRow["ABSENT_COV"] == "2" || $schoolRow["ABSENT_COV"] == "4" || $schoolRow["ABSENT_COV"] == "5") { 
            $query = knjd129nQuery::getAttendData($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model);
        } else {
            $query = knjd129nQuery::getAttendData2($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model);
        }
        $attendRow = array();
        $attendRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $t_lateearly = ($model->chikokuHyoujiFlg == 1) ? $attendRow["LATE_EARLY"] : $attendRow["T_LATEEARLY"];
        $row["LESSON"]      = strlen($attendRow["LESSON"])      ? $attendRow["LESSON"]      : "0";
        $row["T_NOTICE"]    = strlen($attendRow["T_NOTICE"])    ? $attendRow["T_NOTICE"]    : "0";
        $row["T_LATEEARLY"] = strlen($t_lateearly)              ? $t_lateearly              : "0";
        $row["NOTICE_LATE"] = strlen($attendRow["NOTICE_LATE"]) ? $attendRow["NOTICE_LATE"] : "0";

        //出席点を取得したい・・・
        //1.単位を取得
        $credits = $db->getOne(knjd129nQuery::getCredits($model, $row["SCHREGNO"]));
        $credits = strlen($credits) ? $credits : "0";
        //2.学期毎の欠課時数を取得
        //3.学期毎の出席点を取得
        $row["attendScoreInfo"] = "<BR>";
        $seq = "-";
        for ($sem = 1; $sem <= 3; $sem++) {
            if ($schoolRow["ABSENT_COV"] == "0" || $schoolRow["ABSENT_COV"] == "2" || $schoolRow["ABSENT_COV"] == "4" || $schoolRow["ABSENT_COV"] == "5") { 
                $query = knjd129nQuery::getAttendData($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model, $sem);
            } else {
                $query = knjd129nQuery::getAttendData2($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model, $sem);
            }
            $attendRow = array();
            $attendRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $kekka = strlen($attendRow["NOTICE_LATE"]) ? $attendRow["NOTICE_LATE"] : "0";
            $attendScore = $db->getOne(knjd129nQuery::getAttendScore($sem, $credits, $kekka));
            $attendScore = strlen($attendScore) ? $attendScore : "0";
            knjCreateHidden($objForm, "ATTEND_SCORE".$sem."-".$counter, $attendScore);
        }

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
        $scoreSum .= "<th width=40 >".$foot["SUM"]."</th> ";
        $scoreAvg .= "<th width=40 >".$foot["AVG"]."</th> ";
        $scoreMax .= "<th width=40 >".$foot["MAX"]."</th> ";
        $scoreMin .= "<th width=40 >".$foot["MIN"]."</th> ";
        //履修単位・修得単位
        if ($testcd == "9990009") {
            $scoreSum .= "<th width=40 ></th> ";
            $scoreAvg .= "<th width=40 ></th> ";
            $scoreMax .= "<th width=40 ></th> ";
            $scoreMin .= "<th width=40 ></th> ";
            $scoreSum .= "<th width=40 ></th> ";
            $scoreAvg .= "<th width=40 ></th> ";
            $scoreMax .= "<th width=40 ></th> ";
            $scoreMin .= "<th width=40 ></th> ";
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
    $cur_date = $db->getRow(knjd129nQuery::GetMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
    $shuukei_date = str_replace("-", "/", CTRL_DATE);
    if (is_array($cur_date)) {
        $arg["CUR_DATE"] = $cur_date["YEAR"]."年度".$model->control["学期名"][$cur_date["SEMESTER"]]." ".(int)$cur_date["MONTH"]."月".$cur_date["APPOINTED_DAY"]."日現在";
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
        $query = knjd129nQuery::getRecordChkfinDat($model, $testcd);
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
        $chkComp .= "<th width=40 >".$val."</th> ";
        //履修単位・修得単位
        if ($testcd == "9990009") {
            $chkComp .= "<th width=40 ></th> ";
            $chkComp .= "<th width=40 ></th> ";
        }
    }
    $arg["CHK_COMP"] = $chkComp;


    //テキストの名前を取得する
    $textFieldName = $textFieldName2 = $chkCompName = $testItemName = "";
    $textSep = "";
    foreach ($model->testcdArray as $key => $codeArray) {
        $textFieldName  .= $textSep."SCORE" .$codeArray["TESTCD"];
        $textFieldName2 .= $textSep."SCORE" .$codeArray["TESTCD"];
        $chkCompName    .= $textSep."CHK_COMP".$codeArray["TESTCD"];
        $testItemName   .= $textSep.$codeArray["TESTITEMNAME"];
        $textSep = ",";
        //履修単位・修得単位
        if ($codeArray["TESTCD"] == "9990009") {
            $textFieldName .= $textSep."COMP_CREDIT";
            $textFieldName .= $textSep."GET_CREDIT";
            $textSep = ",";
        }
    }
    knjCreateHidden($objForm, "TEXT_FIELD_NAME", $textFieldName);
    knjCreateHidden($objForm, "TEXT_FIELD_NAME2", $textFieldName2);
    knjCreateHidden($objForm, "CHK_COMP_NAME", $chkCompName);
    knjCreateHidden($objForm, "TEST_ITEM_NAME", $testItemName);

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
    //表示切替ボタン
    $setBtnName = $model->dispZumi ? "処理済みを表示しない" : "処理済みを表示";
    $extra = "onclick=\"return btn_submit('disp');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", $setBtnName, $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", $model->wordBtnPrint, $syukketuDisabled.$extra);
    //1:出欠入力ボタン非表示
    if ($model->noUseBtnAttend !== '1') {
        $extra = "onClick=\" wopen('".REQUESTROOT."/C/KNJC035E/knjc035eindex.php?SEND_PRGID=KNJD129N&SEND_AUTH=".AUTHORITY."&SEND_SEMESTER=".CTRL_SEMESTER."&SEND_CHAIR={$model->field["CHAIRCD"]}&SEND_SUBCLASS={$model->field["SUBCLASSCD"]}&SEND_APPDATE={$execute_date}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["btn_c035e"] = knjCreateBtn($objForm, "btn_c035e", "出欠入力", $syukketuDisabled.$extra);
    }
    knjCreateHidden($objForm, "noUseBtnAttend", $model->noUseBtnAttend);
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
    knjCreateHidden($objForm, "PRGID", "KNJD128V");//TODO：要望フォーム未定のため、基本のKNJD128Vとしておく。
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
    knjCreateHidden($objForm, "z010name1", $model->z010name1);
    knjCreateHidden($objForm, "isKeiai", $model->isKeiai);
    knjCreateHidden($objForm, "isKasiwara", $model->isKasiwara);
}
?>
