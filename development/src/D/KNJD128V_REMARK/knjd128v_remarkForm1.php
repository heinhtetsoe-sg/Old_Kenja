<?php

require_once('for_php7.php');


class knjd128v_remarkForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd128v_remarkindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //課程学科コンボ
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["use_school_detail_gcm_dat"] = 1;
            $extra = "onChange=\"btn_submit('main')\";";
            $query = knjd128v_remarkQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1, "");
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //科目コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjd128v_remarkQuery::getSubclassMst($model);
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
                $schoolKind = $db->getOne(knjd128v_remarkQuery::getSchoolkindQuery());
            }
            $schoolKind = $db->getOne(knjd128v_remarkQuery::getAdminSchoolKind($schoolKind));
            $model->subclassAll = "00-".$schoolKind."-00-000000";
        } else {
            $model->subclassAll = "000000";
        }

        //講座コンボ
        $extra = "onChange=\"btn_submit('chaircd')\";";
        $query = knjd128v_remarkQuery::selectChairQuery($model);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_CHAIRCD");

        //明細ヘッダデータ作成
        $model->testcdArray = makeHead($objForm, $arg, $db, $model);

        //明細データ作成
        makeMeisai($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd128v_remarkForm1.html", $arg);
    }
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model) {
    //科目別設定があるか
    $testSubCnt = $db->getOne(knjd128v_remarkQuery::getTestSubCnt($model));
    if ($testSubCnt == "") $testSubCnt = 0;

    $send_testname = "";

    $count = 0;
    $sem = "";
    $col = 0;
    $semArray = array();
    $head1 = "";
    $head2 = "";
    $testcdArray = array();
    $result = $db->query(knjd128v_remarkQuery::getTestName($model, $testSubCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $count++;
        if ($sem != $row["SEMESTER"]) {
            $sem = $row["SEMESTER"];
            $col = 0;
        }
        $col++;
        $semArray[$row["SEMESTER"]] = "<th colspan={$col} ><font size=2>".$row["SEMESTERNAME"]."</font></th> ";

        $head2 .= "<th width=60 ><font size=2>".$row["TESTITEMNAME"]."</font></th> ";
        $testcdArray[] = array("TESTCD" => $row["TESTCD"], "SEMESTERNAME" => $row["SEMESTERNAME"], "TESTITEMNAME" => $row["TESTITEMNAME"], "SIDOU_INPUT" => $row["SIDOU_INPUT"], "SIDOU_INPUT_INF" => $row["SIDOU_INPUT_INF"], "CONTROL_FLG" => $row["CONTROL_FLG"], "SEMESTER" => $row["SEMESTER"], "SDATE" => $row["SDATE"], "EDATE" => $row["EDATE"]);

        if ($row["TESTCD"] == $model->field["TESTCD"]) $send_testname = $row["SEMESTERNAME"]."<BR>".$row["TESTITEMNAME"]."<BR>"."備考(全角50文字)";
    }
    $result->free();
    foreach ($semArray as $key => $val) {
        $head1 .= $val;
    }
    $arg["HEAD1"] = $head1; //学期名称
    $arg["HEAD2"] = $head2; //考査種別名称
    $arg["SEND_TESTNAME"] = $send_testname; //入力考査種別名称
    $addSize = (10 < $count) ? ($count - 10) * 63 : 0;
    $arg["ALL_WIDTH"] = 1280 + $addSize; //画面全体幅
    $arg["FOOT_COLSPAN"] = 2 + $count; //画面全体幅
    return $testcdArray;
}

//明細データ
function makeMeisai(&$objForm, &$arg, $db, &$model) {
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

    //テキストの名前を取得する
    $textFieldName = "";
    $textSep = "";

    //成績データ取得
    $scoreArray = array();
    $result = $db->query(knjd128v_remarkQuery::getScore($model, $execute_date));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $scoreArray[$row["SCHREGNO"]][$row["TESTCD"]] = $row;
    }
    $result->free();

    //備考データ取得
    $recinfoArray = array();
    $result = $db->query(knjd128v_remarkQuery::getRecordInfo($model, $execute_date));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $recinfoArray[$row["SCHREGNO"]][$row["TESTCD"]] = $row;
    }
    $result->free();

    //一覧表示
    $colorFlg = false;
    $result = $db->query(knjd128v_remarkQuery::selectQuery($model, $execute_date));
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

        //異動日の翌日以降は、転学、退学者は、入力不可にする。
        $tentaigakuFlg = false;

        //各項目を作成
        $meisai = "";
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];
            $col = "SCORE" .$testcd;

            //成績データ
            $scoreRow = $scoreArray[$row["SCHREGNO"]][$testcd];
            $score = "";
            if (is_array($scoreRow)) {
                $diFlg = ($scoreRow["VALUE_DI"] == "*" || $scoreRow["VALUE_DI"] == "**") ? "1" : "";
                if ($model->prgid == "KNJD129L") {
                    if ($scoreRow["VALUE_DI"] == "+" || $scoreRow["VALUE_DI"] == "-") $diFlg = "1";
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

            //ラベルのみ
            $row[$col] = "<font color=\"#000000\">".$score."</font>";
            //hidden
            knjCreateHidden($objForm, $col."-".$counter, $score);

            //背景色
            $row["BGCOLOR"] = $row["COLOR"]; //通常の背景色
            if ($testcd != "9990009" && $colorFlgYellow) $row["BGCOLOR"] = "#ffff00"; //異動

            //明細
            $meisai .= "<td width=60 align=\"center\" bgcolor={$row["BGCOLOR"]}>".$row[$col]."</td>";
        }
        $row["MEISAI"] = $meisai;

        //指導データ
        $recinfoRow = array();
        $recinfoRow = $recinfoArray[$row["SCHREGNO"]][$model->field["TESTCD"]];
        //備考
        $name = "REMARK";
        $extra = "STYLE=\"WIDTH:95%\" WIDTH=\"95%\" onChange=\"this.style.background='#ccffcc'\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\"";
        $row[$name] = knjCreateTextBox($objForm, $recinfoRow["REMARK"], $name."-".$counter, 100, 50, $extra);
        //テキストの名前をセット
        if ($counter == 0) {
            $textFieldName .= $textSep.$name;
            $textSep = ",";
        }

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
        $scoreSum .= "<th width=60 >".$foot["SUM"]."</th> ";
        $scoreAvg .= "<th width=60 >".$foot["AVG"]."</th> ";
        $scoreMax .= "<th width=60 >".$foot["MAX"]."</th> ";
        $scoreMin .= "<th width=60 >".$foot["MIN"]."</th> ";
    }
    //合計
    $arg["SCORE_SUM"] = $scoreSum;
    //平均
    $arg["SCORE_AVG"] = $scoreAvg;
    //最高点
    $arg["SCORE_MAX"] = $scoreMax;
    //最低点
    $arg["SCORE_MIN"] = $scoreMin;
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
function makeBtn(&$objForm, &$arg, $model) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onClick=\"closeFunc();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    //更新権限チェック
    knjCreateHidden($objForm, "USER_AUTH", $model->auth);
    knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
    //考査種別
    knjCreateHidden($objForm, "TESTCD", $model->field["TESTCD"]);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
    knjCreateHidden($objForm, "z010name1", $model->z010name1);
}
?>
