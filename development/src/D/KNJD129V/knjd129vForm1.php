<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd129vForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //CSV
        $objUp = new csvFile();

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd129vindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //課程学科コンボ
        $cmCnt = 0;
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["use_school_detail_gcm_dat"] = 1;
            // Edit by PP for PC-Talker 2020-02-10 start
            $extra = "aria-label=\"課程学科\" id=\"COURSE_MAJOR\" onChange=\"current_cursor('COURSE_MAJOR');btn_submit('main')\";";
            // Edit by PP for PC-Talker 2020-02-10 end
            $query = knjd129vQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1, "");

            $cmCnt = get_count($db->getCol(knjd129vQuery::getCourseMajor($model)));
        }

        //CSV表示不可チェック
        $useCsv = $db->getOne(knjd129vQuery::getNameMstD058());
        $notCsvCnt = $db->getOne(knjd129vQuery::getNotusecsvCnt($model));
        if ($useCsv != "1" && $notCsvCnt == 0) {
            $arg["useCsv"] = 1;
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //科目ラジオ・・・科目コンボのリスト条件
        if ($model->Properties["KNJD129V_SUBCLASS_RADIO"] == '1') {
            $arg["KNJD129V_SUBCLASS_RADIO"] = 1;
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
            $result = $db->query(knjd129vQuery::getSemester());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $hankiCnt = $db->getOne(knjd129vQuery::getHankiCnt($row["SEMESTER"]));
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
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"SUBCLASSCD\" aria-label=\"科目\" onChange=\"current_cursor('SUBCLASSCD'); btn_submit('subclasscd')\";";
        // Add by PP for PC-Talker 2020-02-20 end
        $query = knjd129vQuery::getSubclassMst($model, $cmCnt);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_SUBCLASSCD");

        //D065登録科目か
        $query = knjd129vQuery::getD065Sub($model);
        $subD065 = (0 < $db->getOne($query)) ? "1" : "";
        knjCreateHidden($objForm, "SUB_D065", $subD065);
        //D065登録科目の入力値チェック用
        $d001List = array();
        $result = $db->query(knjd129vQuery::getD001List($model));
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
                $schoolKind = $db->getOne(knjd129vQuery::getSchoolkindQuery());
            }
            $schoolKind = $db->getOne(knjd129vQuery::getAdminSchoolKind($schoolKind));
            $model->subclassAll = "00-".$schoolKind."-00-000000";
        } else {
            $model->subclassAll = "000000";
        }

        //講座コンボ
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"CHAIRCD\" aria-label=\"学級・講座\" onChange=\"current_cursor('CHAIRCD'); btn_submit('chaircd')\";";
        // Add by PP for PC-Talker 2020-02-20 end
        $query = knjd129vQuery::selectChairQuery($model, $cmCnt);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");
        //hidden
        knjCreateHidden($objForm, "H_CHAIRCD");

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach ($opt as $key => $val) {
            // Edit by PP for PC-Talker 2020-02-10 start
            if ($val == 1) {
                array_push($extra, " aria-label=\"移動方向の縦\" id=\"MOVE_ENTER{$val}\"");
            } else {
                array_push($extra, " aria-label=\"移動方向の横\" id=\"MOVE_ENTER{$val}\"");
            }
            // Edit by PP for PC-Talker 2020-02-20 end
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //明細ヘッダデータ作成
        $model->testcdArray = makeHead($objForm, $arg, $db, $model);

        //明細データ作成
        makeMeisai($objForm, $arg, $db, $model, $objUp, $cmCnt, $schoolKind);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model, $execute_date);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd129vForm1.html", $arg);
    }
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model)
{
    //科目別設定があるか
    $testSubCnt = $db->getOne(knjd129vQuery::getTestSubCnt($model));
    if ($testSubCnt == "") {
        $testSubCnt = 0;
    }

    $sem = "";
    $col = 0;
    $semArray = array();
    $head1 = "";
    $head2 = "";
    $testcdArray = array();
    //リンク元のプログラムＩＤ
    $prgid = "KNJD129V";
    $auth = AUTHORITY;
    //リンク先のURL
    $jump = REQUESTROOT."/D/KNJD128V_2/knjd128v_2index.php";
    $result = $db->query(knjd129vQuery::getTestName($model, $testSubCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($sem != $row["SEMESTER"]) {
            $sem = $row["SEMESTER"];
            $col = 0;
        }
        $col++;
        $semArray[$row["SEMESTER"]] = "<th colspan={$col} ><font size=2>".$row["SEMESTERNAME"]."</font></th> ";
        if ($row["TESTCD"] == "9990009") {
            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                // Edit by PP for PC-Talker 2020-02-10 start
                $extra = "id=\"\" onClick=\"return check_all(this);\" aria-label=\"全て学生の仮評定の選択\"";
                $chk = $model->field["PROV_FLG_ALL"] == '1' ? ' checked="checked" ' : '';
                $dis = $row["CONTROL_FLG"] == '1' ? '' : ' disabled="disabled" ';
                $prov_flg_all = knjCreateCheckBox($objForm, "PROV_FLG_ALL", "1", $extra.$chk.$dis);

                $head2 .= "<th rowspan=2 width=25 aria-label=\"\"><font size=2>仮<br>評<br>定</font>{$prov_flg_all}</th> ";
                // Edit by PP for PC-Talker 2020-02-20 end
            }
        }

        //URLパラメータ
        $param  = "?prgid={$prgid}";
        $param .= "&auth={$auth}";
        $param .= "&SUBCLASSCD={$model->field["SUBCLASSCD"]}";
        $param .= "&CHAIRCD={$model->field["CHAIRCD"]}";
        $param .= "&TESTCD={$row["TESTCD"]}";
        // Edit by PP for PC-Talker 2020-02-10 start
        $alinklabel = $row["TESTCD"];
        $alinklabel2 = $row["TESTITEMNAME"];
        //考査名にリンクを作成
        $extra = " title=\"$alinklabel2\" id=\"$alinklabel\" onClick=\"current_cursor('$alinklabel');openKogamen('{$jump}{$param}');\"";
        // Edit by PP for PC-Talker 2020-02-20 end
        $testNameLink = ($row["SIDOU_INPUT"] == "1") ? View::alink("#", htmlspecialchars($row["TESTITEMNAME"]), $extra) : $row["TESTITEMNAME"];

        $head2 .= "<th rowspan=2 width=60 ><font size=2>".$testNameLink."</font></th> ";
        if ($row["TESTCD"] == "9990009") {
            $head2 .= "<th rowspan=2 width=60 ><font size=2>履修単位</font></th> ";
            $head2 .= "<th rowspan=2 width=60 ><font size=2>修得単位</font></th> ";
        }
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
function makeMeisai(&$objForm, &$arg, $db, &$model, &$objUp, $cmCnt, $schoolKind)
{
    /*******/
    /* CSV */
    /*******/
    $subclassname = $db->getOne(knjd129vQuery::getSubclassName($model));
    $chairname = $db->getOne(knjd129vQuery::getChairName($model));
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
    foreach ($model->testcdArray as $key => $codeArray) {
        $csvHeader[$codeArray["TESTCD"]] = $codeArray["TESTITEMNAME"];
        $setType[$setCnt] = 'S';
        $setSize[$setCnt] = 3;
        $setCnt++;
        if ($codeArray["TESTCD"] == "9990009") {
            $csvHeader[$setCnt] = "履修単位";
            $setType[$setCnt] = 'S';
            $setSize[$setCnt] = 2;
            $setCnt++;
            $csvHeader[$setCnt] = "修得単位";
            $setType[$setCnt] = 'S';
            $setSize[$setCnt] = 2;
            $setCnt++;
        }
    }
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
    $schoolMstHasSchoolKind = in_array("SCHOOL_KIND", $db->getCol(knjd129vQuery::getSchoolMstCols()));
    $schoolRow = array();
    $schoolRow = $db->getRow(knjd129vQuery::getVSchoolMst($schoolMstHasSchoolKind ? $schoolKind : ''), DB_FETCHMODE_ASSOC);
    //時間割講座テストより試験日を抽出
    $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
    $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
    if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
        $execute_date = CTRL_DATE;//初期値
    } else {
        $execute_date = $edate;//初期値
    }

    //合併先科目か
    $isSubclassSaki = 0 < $db->getOne(knjd129vQuery::getCntSubclassSaki($model)) ? "1" : "";

    //累積現在日(出欠集計日)
    $shuukei_date = str_replace("-", "/", CTRL_DATE);
    $attendSM = array(); //集計学期・月の配列
    $result = $db->query(knjd129vQuery::getMax($model, $isSubclassSaki));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $attendYear = ((int)$row["MONTH"] <= 3) ? ($row["YEAR"] + 1) : $row["YEAR"];
        $attendDate = $attendYear . "/" . sprintf("%02d", $row["MONTH"]) . "/" . sprintf("%02d", $row["APPOINTED_DAY"]);
//        if ($shuukei_date < $attendDate) break;
        $attendSM[] = $row["SEMESTER"] . $row["MONTH"];
        $arg["CUR_DATE"] = $row["YEAR"]."年度".$model->control["学期名"][$row["SEMESTER"]]."<BR>".(int)$row["MONTH"]."月".$row["APPOINTED_DAY"]."日現在";
    }

    //初期化
    $model->data = array();
    $counter = 0;

    //テキストの名前を取得する
    $textFieldName = "";
    $textSep = "";

    //一覧表示
    $colorFlg = false;
    $query = knjd129vQuery::getScore($model, $execute_date, $cmCnt);
    $result = $db->query($query);
    $scoreArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号を配列で取得
        $scoreArray[$row["SCHREGNO"]][$row["TESTCD"]] = $row;
    }
    $result->free();

    $result = $db->query(knjd129vQuery::selectQuery($model, $execute_date, $cmCnt));
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

        //異動日の翌日以降は、転学、退学者は、入力不可にする。
        $tentaigakuFlg = false;

        //各項目を作成
        $meisai = "";
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];
            $col = "SCORE" .$testcd;
            //$row[$col."_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            //成績データ
            $scoreRow = $scoreArray[$row["SCHREGNO"]][$testcd];
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
                    $tentaigakuFlg = true;//単位認定画面は、学年末のみのため
                }
            }

            //追指導データ 1:追指導入力
            $colorFlgPink = false;
            $fontcolorFlgRed = false;
            if ($codeArray["SIDOU_INPUT"] == "1") {
                $slumpRow = array();
                $query = knjd129vQuery::getRecordSlump(CTRL_YEAR, $testcd, $row["SCHREGNO"], $row["GRADE"], $model);
                $slumpRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                //1:記号 2:点数
                if ($codeArray["SIDOU_INPUT_INF"] == "1" && strlen($slumpRow["MARK"])) {
                    $colorFlgPink = true;
                    //1:○ 2:△ 3:×
                    if ($slumpRow["MARK"] == "3") {
                        $fontcolorFlgRed = true;
                    }
                } elseif ($codeArray["SIDOU_INPUT_INF"] == "2" && strlen($slumpRow["SCORE"])) {
                    $colorFlgPink = true;
                    //追指導点を換算後の評定が１
                    if ($slumpRow["ASSESSLEVEL"] == "1") {
                        $fontcolorFlgRed = true;
                    }
                }
            }
            //管理者コントロール
            if ($controlFlg == "1" && (!$tentaigakuFlg || AUTHORITY == DEF_UPDATABLE)) {
                //テキストボックスを作成
                // Edit by PP for PC-Talker 2020-02-10 start
                $row["FONTCOLOR"] = $fontcolorFlgRed ? "#ff0000" : "#000000";
                if ($codeArray['TESTITEMNAME'] == "") {
                    $readLabel = $row['ATTENDNO'].$row['SCHREGNO_SHOW'].$row['NAME_SHOW'];
                } else {
                    $readLabel = $row['ATTENDNO'].$row['SCHREGNO_SHOW'].$row['NAME_SHOW']."の".$codeArray['TESTITEMNAME'];
                }
                $extra = " aria-label=\"$readLabel\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\" id=\"{$col}-{$counter}\" ";
                $row[$col] = knjCreateTextBox($objForm, $score, $col."-".$counter, 3, 3, $extra);
                // Edit by PP for PC-Talker 2020-02-20 end
                //テキストの名前をセット
                if ($counter == 0) {
                    $textFieldName .= $textSep.$col;
                    $textSep = ",";
                }
                //考査満点マスタ
                $query = knjd129vQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $testcd, $row["GRADE"], $row["COURSE"], $model);
                $perfect = ($model->usePerfect == 'true' && substr($testcd, 5, 2) == '01') ? $db->getOne($query) : 100;
                if ($perfect == "") {
                    $perfect = 100;
                }
                //テキストボックスを作成
                knjCreateHidden($objForm, $col."_PERFECT"."-".$counter, $perfect);
                //入力エリアとキーをセットする
                $objUp->setElementsValue($col."-".$counter, $csvHeader[$testcd], $csvKey);
                //履修単位・修得単位
                if ($testcd == "9990009") {
                    if ($model->Properties["notOpenCredit"] != '1') {
                        $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\" id=\"COMP_CREDIT-{$counter}\" ";
                        $row["COMP_CREDIT"] = knjCreateTextBox($objForm, $scoreRow["COMP_CREDIT"], "COMP_CREDIT"."-".$counter, 3, 2, $extra);
                        $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\" id=\"GET_CREDIT-{$counter}\" ";
                        $row["GET_CREDIT"] = knjCreateTextBox($objForm, $scoreRow["GET_CREDIT"], "GET_CREDIT"."-".$counter, 3, 2, $extra);
                        knjCreateHidden($objForm, "COMP_CREDIT"."_PERFECT"."-".$counter, 100);
                        knjCreateHidden($objForm, "GET_CREDIT"."_PERFECT"."-".$counter, 100);
                        //テキストの名前をセット
                        if ($counter == 0) {
                            $textFieldName .= $textSep."COMP_CREDIT";
                            $textFieldName .= $textSep."GET_CREDIT";
                            $textSep = ",";
                        }
                        //入力エリアとキーをセットする
                        $objUp->setElementsValue("COMP_CREDIT"."-".$counter, "履修単位", $csvKey);
                        $objUp->setElementsValue("GET_CREDIT"."-".$counter, "修得単位", $csvKey);
                    } else {
                        $row["COMP_CREDIT"] = "<font color=\"#000000\">".$scoreRow["COMP_CREDIT"]."</font>";
                        $row["GET_CREDIT"] = "<font color=\"#000000\">".$scoreRow["GET_CREDIT"]."</font>";
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
                    //hidden
                    knjCreateHidden($objForm, "COMP_CREDIT"."-".$counter, $scoreRow["COMP_CREDIT"]);
                    knjCreateHidden($objForm, "GET_CREDIT"."-".$counter, $scoreRow["GET_CREDIT"]);
                }
            }

            //CSV書出
            $csv[] = $score;
            //履修単位・修得単位
            if ($testcd == "9990009") {
                $csv[] = $scoreRow["COMP_CREDIT"];
                $csv[] = $scoreRow["GET_CREDIT"];
            }

            //仮評定
            if ($testcd == "9990009") {
                // Edit by PP for PC-Talker 2020-02-10 start
                if ($model->Properties["useProvFlg"] == '1') {
                    $chk = $row["PROV_FLG"] == '1' ? ' checked="checked" ' : '';
                    $dis = $controlFlg == '1' ? '' : ' disabled="disabled" ';
                    $extra = "aria-label=\"{$row['ATTENDNO']}{$row['SCHREGNO_SHOW']}{$row['NAME_SHOW']}の仮評定\"";
                    $row["PROV_FLG"] = knjCreateCheckBox($objForm, "PROV_FLG"."-".$counter, "1", $extra.$chk.$dis);
                    $meisai .= "<td width=25 align=\"center\" aria-label=\"\" bgcolor={$row["COLOR"]}>".$row["PROV_FLG"]."</td>";
                    // Edit by PP for PC-Talker 2020-02-20 end
                }
            }
            $row["BGCOLOR"] = $row["COLOR"]; //通常の背景色
            if ($testcd != "9990009" && $colorFlgYellow) {
                $row["BGCOLOR"] = "#ffff00";
            } //異動
            if ($colorFlgPink) {
                $row["BGCOLOR"] = "#ffc0cb";
            } //指導
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
            $query = knjd129vQuery::getAttendData($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model, $isSubclassSaki, $attendSM);
        } else {
            $query = knjd129vQuery::getAttendData2($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model, $isSubclassSaki, $attendSM);
        }
        $attendRow = array();
        $attendRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $t_lateearly = ($model->chikokuHyoujiFlg == 1) ? $attendRow["LATE_EARLY"] : $attendRow["T_LATEEARLY"];
        $row["LESSON"]      = strlen($attendRow["LESSON"])      ? $attendRow["LESSON"]      : "0";
        $row["MLESSON"]     = strlen($attendRow["MLESSON"])     ? $attendRow["MLESSON"]     : "0";
        $row["T_NOTICE"]    = strlen($attendRow["T_NOTICE"])    ? $attendRow["T_NOTICE"]    : "0";
        $row["T_LATEEARLY"] = strlen($t_lateearly)              ? $t_lateearly              : "0";
        $row["NOTICE_LATE"] = strlen($attendRow["NOTICE_LATE"]) ? $attendRow["NOTICE_LATE"] : "0";

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
        $query = knjd129vQuery::getRecordChkfinDat($model, $testcd);
        $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($resultRow)) {
            if ($resultRow["EXECUTED"] == '1') {
                $chk = ' checked="checked" ';
            } else {
                $chk = '';
            }
        }
        // Add by PP for PC-Talker 2020-02-10
        if ($codeArray['TESTITEMNAME'] == "") {
            $extra = " aria-label=\"成績入力完了\"";
        } else {
            $extra = " aria-label=\"".$codeArray['TESTITEMNAME']."の成績入力完了\"";
        }

        // Add by PP for PC-Talker 2020-02-20
        $dis = $controlFlg == "1" ? '' : ' disabled="disabled" ';
        $val = knjCreateCheckBox($objForm, "CHK_COMP".$testcd, "on", $extra.$chk.$dis);
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
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update');return btn_submit('update');\" aria-label=\"更新\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $syukketuDisabled.$extra);
    // Add by PP for PC-Talker 2020-02-20 end
    //取消ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_reset\" onclick=\"current_cursor('btn_reset'); return btn_submit('reset');\" aria-label=\"取消\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $syukketuDisabled.$extra);
    // Add by PP for PC-Talker 2020-02-20 end
    //終了ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "onclick=\"closeWin();\" aria-label=\"終了\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    // Add by PP for PC-Talker 2020-02-20 end
    //印刷ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\" aria-label=\"印刷\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $syukketuDisabled.$extra);
    // Add by PP for PC-Talker 2020-02-20 end
}

//hidden作成
function makeHidden(&$objForm, $model, $execute_date)
{
    knjCreateHidden($objForm, "cmd");
    //印刷パラメータ
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD129V");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "STAFF", STAFFCD);
    knjCreateHidden($objForm, "CHIKOKU_HYOUJI_FLG", $model->chikokuHyoujiFlg);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "gen_ed", substr($model->field["SUBCLASSCD"], 0, 2) == $model->gen_ed ? $model->gen_ed : "");
    knjCreateHidden($objForm, "TEST_DATE", $execute_date);
    knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
    //更新権限チェック
    knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
    knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
    knjCreateHidden($objForm, "z010name1", $model->z010name1);
}
