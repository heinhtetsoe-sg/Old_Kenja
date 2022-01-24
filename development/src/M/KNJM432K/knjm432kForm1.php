<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjm432kForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //CSV
        $objUp = new csvFile();

        $arg["start"]    = $objForm->get_start("main", "POST", "knjm432kindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //課程学科コンボ
        $cmCnt = 0;
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["use_school_detail_gcm_dat"] = 1;
            $extra = "aria-label=\"課程学科\" id=\"COURSE_MAJOR\" onChange=\"current_cursor('COURSE_MAJOR');btn_submit('main')\";";
            $query = knjm432kQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1, "");

            $cmCnt = get_count($db->getCol(knjm432kQuery::getCourseMajor($model)));
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //科目ラジオ・・・科目コンボのリスト条件
        if ($model->Properties["KNJM432K_SUBCLASS_RADIO"] == '1') {
            $arg["KNJM432K_SUBCLASS_RADIO"] = 1;
            //半期認定科目
            //1:1学期(前期)　2:2学期(後期)　3:3学期　4:通年
            //5:全て・・・初期値
            //6:合併先
            $opt = array(1, 2, 3, 4, 5, 6);
            $model->field["SUBCLASS_DIV"] = ($model->field["SUBCLASS_DIV"] == "") ? "5" : $model->field["SUBCLASS_DIV"];
            $extra = array();
            foreach ($opt as $key => $val) {
                array_push($extra, " id=\"SUBCLASS_DIV{$val}\" onclick=\"btn_submit('main');\"");
            }
            $radioArray = knjCreateRadio($objForm, "SUBCLASS_DIV", $model->field["SUBCLASS_DIV"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg[$key] = $val;
            }
            //学期取得(科目ラジオ表示用)
            $result = $db->query(knjm432kQuery::getSemester());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $hankiCnt = $db->getOne(knjm432kQuery::getHankiCnt($row["SEMESTER"]));
                //半期認定科目がある場合、表示する
                if (0 < $hankiCnt) {
                    $arg["SUBCLASS_DIV".$row["SEMESTER"]."_LABEL"] = $row["SEMESTERNAME"]."科目";
                }
            }
            $result->free();
            $arg["SUBCLASS_DIV4_LABEL"] = "通年科目";
            $arg["SUBCLASS_DIV5_LABEL"] = "全て";
            $arg["SUBCLASS_DIV6_LABEL"] = "合併先科目";
        }

        //科目コンボ
        $extra = "id=\"SUBCLASSCD\" aria-label=\"科目\" onChange=\"current_cursor('SUBCLASSCD'); btn_submit('subclasscd')\";";
        $query = knjm432kQuery::getSubclassMst($model, $cmCnt);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_SUBCLASSCD");

        //D065登録科目か
        $query = knjm432kQuery::getD065Sub($model);
        $subD065 = (0 < $db->getOne($query)) ? "1" : "";
        knjCreateHidden($objForm, "SUB_D065", $subD065);
        //D065登録科目の入力値チェック用
        $d001List = array();
        $result = $db->query(knjm432kQuery::getD001List($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $d001List[] = $row["VALUE"];
        }
        knjCreateHidden($objForm, "D001_LIST", implode(',', $d001List)); //javascriptでチェック

        //基本設定のコードを取得
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->field["SUBCLASSCD"]) {
                $subclassArray = array();
                $subclassArray = explode("-", $model->field["SUBCLASSCD"]);
                $schoolKind = $subclassArray[1];
            } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
                $subclassArray = array();
                $subclassArray = explode("-", $model->field["SUBCLASSCD"]);
                $schoolKind = $subclassArray[1];
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $schoolKind = SCHOOLKIND;
            } else {
                $schoolKind = $db->getOne(knjm432kQuery::getSchoolkindQuery());
            }
            $model->subclassAll = "00-".$schoolKind."-00-000000";
        } else {
            $model->subclassAll = "000000";
        }

        //講座コンボ
        $extra = "id=\"CHAIRCD\" aria-label=\"学級・講座\" onChange=\"current_cursor('CHAIRCD'); btn_submit('chaircd')\";";
        $query = knjm432kQuery::selectChairQuery($model, $cmCnt);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");
        knjCreateHidden($objForm, "H_CHAIRCD");

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach ($opt as $key => $val) {
            if ($val == 1) {
                array_push($extra, " aria-label=\"移動方向の縦\" id=\"MOVE_ENTER{$val}\"");
            } else {
                array_push($extra, " aria-label=\"移動方向の横\" id=\"MOVE_ENTER{$val}\"");
            }
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //明細ヘッダデータ作成
        makeHead($objForm, $arg, $db, $model);

        //明細データ作成
        makeMeisai($objForm, $arg, $db, $model, $objUp, $cmCnt);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model, $execute_date);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjm432kForm1.html", $arg);
    }
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model)
{
    $extra = "id=\"\" onClick=\"return check_all(this);\" aria-label=\"全て学生の仮評定の選択\"";
    $chk = $model->field["PROV_FLG_ALL"] == '1' ? ' checked="checked" ' : '';
    $arg["head"]["PROV_FLG_ALL"] = knjCreateCheckBox($objForm, "PROV_FLG_ALL", "1", $extra.$chk.$dis);
}

//明細データ
function makeMeisai(&$objForm, &$arg, $db, &$model, &$objUp, $cmCnt)
{
    /*******/
    /* CSV */
    /*******/
    $subclassname = $db->getOne(knjm432kQuery::getSubclassName($model));
    $chairname = $db->getOne(knjm432kQuery::getChairName($model));
    //CSV出力ファイル名
    $objUp->setFileName(CTRL_YEAR."年度_".$subclassname."_".$chairname."_"."単位認定入力.csv");
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

    $csvHeader[$setCnt] = "学年評定";
    $setType[$setCnt] = 'S';
    $setSize[$setCnt] = 2;
    $setCnt++;
    $csvHeader[$setCnt] = "履修単位";
    $setType[$setCnt] = 'S';
    $setSize[$setCnt] = 2;
    $setCnt++;
    $csvHeader[$setCnt] = "修得単位";
    $setType[$setCnt] = 'S';
    $setSize[$setCnt] = 2;
    $setCnt++;

    $csvHeader[] = $model->lastColumn;
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

    //学校詳細マスタ
    $schoolRow = array();
    $schoolRow = $db->getRow(knjm432kQuery::getVSchoolMst(), DB_FETCHMODE_ASSOC);
    //時間割講座テストより試験日を抽出
    $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
    $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
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

    //一覧表示
    $colorFlg = false;
    $query = knjm432kQuery::getScore($model, $execute_date, $cmCnt);
    $result = $db->query($query);
    $scoreArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号を配列で取得
        $scoreArray[$row["SCHREGNO"]][$row["TESTCD"]] = $row;
    }
    $result->free();

    $result = $db->query(knjm432kQuery::selectQuery($model, $execute_date, $cmCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号をHiddenで保持
        knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);
        //クラス-出席番(表示)
        if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
            $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
        }
        //氏名欄に学籍番号表記
        $row["SCHREGNO_SHOW"] = ($model->Properties["use_SchregNo_hyoji"] == 1) ? $row["SCHREGNO"]."　" : "";
        //名前
        //$row["NAME_SHOW"] = $row["SCHREGNO"] ." ". $row["NAME_SHOW"];
        //５行毎に背景色を変更
        if ($counter % 5 == 0) {
            $colorFlg = !$colorFlg;
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

        //入力可能なテキストの名前を取得する
        //学年評定・履修単位・修得単位
        $setTextField = "";
        $setTextField .= $textSep."SCORE-";
        $setTextField .= $textSep."COMP_CREDIT-";
        $setTextField .= $textSep."GET_CREDIT-";

        $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

        //各項目を作成
        $testcd = "9990009";

        //成績データ
        $scoreRow = $scoreArray[$row["SCHREGNO"]][$testcd];
        $score = $scoreRow["VALUE_DI"] == "*" ? $scoreRow["VALUE_DI"] : $scoreRow["SCORE"];

        //縦計項目
        if (is_numeric($score)) {
            $term_data["SCORE"][] = $score;
        }
        if (is_numeric($scoreRow["COMP_CREDIT"])) {
            $term_data["COMP_CREDIT"][] = $scoreRow["COMP_CREDIT"];
        }
        if (is_numeric($scoreRow["GET_CREDIT"])) {
            $term_data["GET_CREDIT"][] = $scoreRow["GET_CREDIT"];
        }

        //テキストボックスを作成
        $row["FONTCOLOR"] = "#000000";

        //仮評定チェックボックス
        $chk = $row["PROV_FLG"] == '1' ? ' checked="checked" ' : '';
        $extra = "aria-label=\"{$row['ATTENDNO']}{$row['SCHREGNO_SHOW']}{$row['NAME_SHOW']}の仮評定\"";
        $row["PROV_FLG"] = knjCreateCheckBox($objForm, "PROV_FLG"."-".$counter, "1", $extra.$chk);

        //学年評定
        $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\" id=\"SCORE-{$counter}\" ";
        $row["SCORE"] = knjCreateTextBox($objForm, $score, "SCORE"."-".$counter, 3, 2, $extra);
        knjCreateHidden($objForm, "SCORE"."_PERFECT"."-".$counter, 100);

        //履修単位
        $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\" id=\"COMP_CREDIT-{$counter}\" ";
        $row["COMP_CREDIT"] = knjCreateTextBox($objForm, $scoreRow["COMP_CREDIT"], "COMP_CREDIT"."-".$counter, 3, 2, $extra);
        knjCreateHidden($objForm, "COMP_CREDIT"."_PERFECT"."-".$counter, 100);

        //修得単位
        $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\" id=\"GET_CREDIT-{$counter}\" ";
        $row["GET_CREDIT"] = knjCreateTextBox($objForm, $scoreRow["GET_CREDIT"], "GET_CREDIT"."-".$counter, 3, 2, $extra);
        knjCreateHidden($objForm, "GET_CREDIT"."_PERFECT"."-".$counter, 100);

        //テキストの名前をセット
        if ($counter == 0) {
            $textFieldName .= $textSep."SCORE";
            $textFieldName .= $textSep."COMP_CREDIT";
            $textFieldName .= $textSep."GET_CREDIT";
            $textSep = ",";
        }
        //入力エリアとキーをセットする
        $objUp->setElementsValue("SCORE"."-".$counter, "学年評定", $csvKey);
        $objUp->setElementsValue("COMP_CREDIT"."-".$counter, "履修単位", $csvKey);
        $objUp->setElementsValue("GET_CREDIT"."-".$counter, "修得単位", $csvKey);

        //CSV書出
        //学年評定・履修単位・修得単位
        $csv[] = $scoreRow["SCORE"];
        $csv[] = $scoreRow["COMP_CREDIT"];
        $csv[] = $scoreRow["GET_CREDIT"];

        //累積情報
        if ($schoolRow["ABSENT_COV"] == "0" || $schoolRow["ABSENT_COV"] == "2" || $schoolRow["ABSENT_COV"] == "4" || $schoolRow["ABSENT_COV"] == "5") {
            $query = knjm432kQuery::getAttendData($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model);
        } else {
            $query = knjm432kQuery::getAttendData2($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model);
        }
        $attendRow = array();
        $attendRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $t_lateearly = ($model->chikokuHyoujiFlg == 1) ? $attendRow["LATE_EARLY"] : $attendRow["T_LATEEARLY"];
        $row["LESSON"]      = strlen($attendRow["LESSON"])      ? $attendRow["LESSON"]      : "0"; //授業時数
        $row["MLESSON"]     = strlen($attendRow["MLESSON"])     ? $attendRow["MLESSON"]     : "0"; //出席すべき時数
        $row["T_NOTICE"]    = strlen($attendRow["T_NOTICE"])    ? $attendRow["T_NOTICE"]    : "0"; //欠席
        $row["T_LATEEARLY"] = strlen($t_lateearly)              ? $t_lateearly              : "0"; //遅早
        $row["NOTICE_LATE"] = strlen($attendRow["NOTICE_LATE"]) ? $attendRow["NOTICE_LATE"] : "0"; //欠課

        //CSV書出
        $csv[] = $model->lastColumn;
        $objUp->addCsvValue($csv);

        $counter++;
        $arg["data"][] = $row;
    }
    $result->free();

    knjCreateHidden($objForm, "COUNT", $counter);
    //テキストの名前を取得
    knjCreateHidden($objForm, "TEXT_FIELD_NAME", $textFieldName);

    //縦計（仮評定）
    $scoreSum = $scoreAvg = $scoreMax = $scoreMin = "";
    $scoreSum .= "<th width=25 ></th> ";
    $scoreAvg .= "<th width=25 ></th> ";
    $scoreMax .= "<th width=25 ></th> ";
    $scoreMin .= "<th width=25 ></th> ";

    //縦計（学年評定・履修単位・修得単位）
    $colum = array("SCORE", "COMP_CREDIT", "GET_CREDIT");
    foreach ($colum as $key => $val) {
        $foot = array();
        if (isset($term_data[$val])) {
            //合計
            $foot["SUM"] = array_sum($term_data[$val]);
            //平均
            $foot["AVG"] = round((array_sum($term_data[$val])/get_count($term_data[$val]))*10)/10;
            //最高点と最低点を求める
            array_multisort($term_data[$val], SORT_NUMERIC);
            $max = get_count($term_data[$val])-1;
            //最高点
            $foot["MAX"] = $term_data[$val][$max];
            //最低点
            $foot["MIN"] = $term_data[$val][0];
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

    //累積現在日
    $cur_date = $db->getRow(knjm432kQuery::getMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
    if (is_array($cur_date)) {
        $arg["CUR_DATE"] = $cur_date["YEAR"]."年度".$model->control["学期名"][$cur_date["SEMESTER"]]."<BR>".(int)$cur_date["MONTH"]."月".$cur_date["APPOINTED_DAY"]."日現在";
    }

    //CSVファイルアップロードコントロール
    $arg["FILE"] = $objUp->toFileHtml($objForm);
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
function makeBtn(&$objForm, &$arg, $model)
{
    $syukketuDisabled = $model->field["CHAIRCD"] && $model->field["SUBCLASSCD"] ? "" : " disabled ";
    //更新ボタン
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update');return btn_submit('update');\" aria-label=\"更新\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $syukketuDisabled.$extra);
    //取消ボタン
    $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('reset');\" aria-label=\"取消\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $syukketuDisabled.$extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\" aria-label=\"終了\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $execute_date)
{
    knjCreateHidden($objForm, "cmd");
    //印刷パラメータ
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJM432K");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "STAFF", STAFFCD);
    knjCreateHidden($objForm, "CHIKOKU_HYOUJI_FLG", $model->chikokuHyoujiFlg);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "gen_ed", substr($model->field["SUBCLASSCD"], 0, 2) == $model->gen_ed ? $model->gen_ed : "");
    knjCreateHidden($objForm, "TEST_DATE", $execute_date);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
    //更新権限チェック
    knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
    knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
}
