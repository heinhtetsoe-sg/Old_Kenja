<?php

//ファイルアップロードオブジェクト
require_once('for_php7.php');
require_once("csvfile.php");

class knjd128vForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //CSV
        $objUp = new csvFile();

        $arg["FOOT_ALIGN"] = "center";
        $arg["TEXTMSG"] = $model->z010name1 == "nichi-ni" ?  "<br><br>「通常の欠席」の場合は、*(半角アスタリスク1個)、<br>　「公欠」の場合は、**(半角アスタリスク2個)を入力してください。" : "<br><br>欠席者には*を入力してください" ;

        // 青山学院の特別措置者表示機能
        if ($model->z010name1 == "aoyama") {
            $arg["FOOT_ALIGN"] = "left";
            $arg["TEXTMSG"]   = "<br>欠席者には*を入力してください";
            $arg["TEXTMSG2"]  = "<br>生徒名・欠課時数青色・・・";
            $arg["TEXTMSG2"] .= "(1)各学期の欠課時数が授業時数の1/3を超えている。";
            $arg["TEXTMSG2"] .= "(2)年間欠課時数が年間授業時数の1/3を超えている。";
            $arg["TEXTMSG2"] .= "(3)3年2学期のみ、1,2学期の総欠課時数が1,2学期の総授業時数の1/3を超えている。";
            $arg["TEXTMSG2"] .= "<br>生徒名・欠課時数赤色・・・";
            $arg["TEXTMSG2"] .= "(1)年間欠課時数が年間授業時数の1/2を超えている。";
            $arg["TEXTMSG2"] .= "(2)3年2学期のみ、1,2学期の総欠課時数が1,2学期の総授業時数の1/2を超えている。";
        }

        //課程学科コンボ
        $cmCnt = 0;
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["use_school_detail_gcm_dat"] = 1;
            /* Edit by Kaung for PC-Talker 2020-02-10 start*/
            $extra = "aria-label=\"課程学科\" id=\"COURSE_MAJOR\" onChange=\"current_cursor('COURSE_MAJOR');btn_submit('main')\";";
            /* Edit by Kaung for PC-Talker 2020-02-10 end*/
            $query = knjd128vQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1, "");

            $cmCnt = count($db->getCol(knjd128vQuery::getCourseMajor($model)));
        }

        //CSV表示不可チェック
        $query = knjd128vQuery::getNotusecsvCnt($model);
        $notCsvCnt = $db->getOne($query);
        if ($model->useCsv && $notCsvCnt == 0) {
            $arg["useCsv"] = 1;
        }

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd128vindex.php", "", "main");

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //科目コンボ
        /* Edit by Kaung for PC-Talker 2020-02-10 start */
        $extra = "aria-label=\"科目\" id=\"SUBCLASSCD\" onChange=\"current_cursor('SUBCLASSCD');btn_submit('subclasscd');\"";
        /* Edit by Kaung for PC-Talker 2020-02-20 end */
        $query = knjd128vQuery::getSubclassMst($model, $cmCnt);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //D065登録科目か
        $query = knjd128vQuery::getD065Sub($model);
        $subD065 = (0 < $db->getOne($query)) ? "1" : "";
        knjCreateHidden($objForm, "SUB_D065", $subD065);
        //D065登録科目の入力値チェック用
        $d001List = array();
        $result = $db->query(knjd128vQuery::getD001List($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $d001List[] = $row["VALUE"];
        }
        knjCreateHidden($objForm, "D001_LIST", implode(',', $d001List)); //javascriptでチェック

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach ($opt as $key => $val) {
            /* Edit by Kaung for PC-Talker 2020-02-10 start */
            if ($val == 1) {
                array_push($extra, " aria-label=\"移動方向の縦\" id=\"MOVE_ENTER{$val}\"");
            } else {
                array_push($extra, " aria-label=\"移動方向の横\" id=\"MOVE_ENTER{$val}\"");
            }
            /* Edit by Kaung for PC-Talker 2020-02-20 end */
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

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
                $schoolKind = $db->getOne(knjd128vQuery::getSchoolkindQuery());
            }
            $schoolKind = $db->getOne(knjd128vQuery::getAdminSchoolKind($model, $schoolKind));
            $model->subclassAll = "00-".$schoolKind."-00-000000";
        } else {
            $model->subclassAll = "000000";
        }

        //講座コンボ
        /* Edit by Kaung for PC-Talker 2020-02-10 start */
        $extra = "aria-label=\"学級・講座\" id=\"CHAIRCD\" onChange=\"current_cursor('CHAIRCD');btn_submit('chaircd');\"";
        /* Edit by Kaung for PC-Talker 2020-02-20 end */
        $query = knjd128vQuery::selectChairQuery($model, $cmCnt);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");

        //テスト取得(パーツ)
        $model->testcdArray = makeTest($objForm, $arg, $db, $model);

        //算出元テスト取得
        $model->testcdMotoArray = makeTestMoto($objForm, $arg, $db, $model);

        //明細ヘッダデータ作成
        makeHead($objForm, $arg, $db, $model);

        //明細データ作成
        $execute_date = makeMeisai($objForm, $arg, $db, $model, $objUp, $cmCnt, $schoolKind);

        //追指導は参照する
        $extra = (strlen($model->field["SIDOU_CHK"]) || $model->cmd == "" && $model->Properties["knjd128vTuisidousanshouCheckboxDefaltOff"] != '1') ? "checked " : "";
        $arg["SIDOU_CHK"] = knjCreateCheckBox($objForm, "SIDOU_CHK", "on", $extra);

        //追指導表示
        $query = knjd128vQuery::getSidouInputCount($model);
        if (0 < $db->getOne($query) && $model->Properties["showHeikinran"] != '1' && $model->Properties["showBunpuHyou"] != '1') {
            $arg["useSidou"] = 1;
        }

        //欠課時数オーバーは未履修にする
        $extra = (strlen($model->field["KEEKA_OVER"])) ? "checked " : "";
        $arg["KEEKA_OVER"] = knjCreateCheckBox($objForm, "KEEKA_OVER", "on", $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $db, $model, $execute_date);

        //hidden作成
        makeHidden($objForm, $model, $execute_date);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd128vForm1.html", $arg);
    }
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model)
{
    $disBtnName = $disBtnNameSeq = "";

    $count = 0;
    $sem = "";
    $col = 0;
    $semArray = array();
    $head1 = "";
    $head2 = "";
    //リンク元のプログラムＩＤ
    $prgid = "KNJD128V";
    $auth = AUTHORITY;
    foreach ($model->testcdArray as $key => $codeArray) {
        $count++;
        if ($sem != $codeArray["SEMESTER"]) {
            $sem = $codeArray["SEMESTER"];
            $col = 0;
        }
        $col++;
        $semArray[$codeArray["SEMESTER"]] = "<th colspan={$col} ><font size=2>".$codeArray["SEMESTERNAME"]."</font></th> ";

        //リンク先のURL
        $jump = REQUESTROOT."/D/KNJD128V_2/knjd128v_2index.php";
        //URLパラメータ
        $param  = "?prgid={$prgid}";
        $param .= "&auth={$auth}";
        $param .= "&SUBCLASSCD={$model->field["SUBCLASSCD"]}";
        $param .= "&CHAIRCD={$model->field["CHAIRCD"]}";
        $param .= "&TESTCD={$codeArray["TESTCD"]}";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $param .= "&COURSE_MAJOR={$model->field["COURSE_MAJOR"]}";
        }
        /* Edit by Kaung for PC-Talker 2020-02-10 start */
        $alinklabel = $row["TESTCD"];
        $alinklabel2 = $row["SEMESTERNAME"]."の".$row["TESTITEMNAME"];
        //考査名にリンクを作成
        $extra = " title=\"$alinklabel2\" id=\"$alinklabel\" onClick=\"current_cursor('$alinklabel');openKogamen('{$jump}{$param}');\"";
        /* Edit by Kaung for PC-Talker 2020-02-20 end */
        $testNameLink = ($codeArray["SIDOU_INPUT"] == "1" && $model->Properties["showHeikinran"] != '1' && $model->Properties["showBunpuHyou"] != '1') ? View::alink("#", htmlspecialchars($codeArray["TESTITEMNAME"]), $extra) : $codeArray["TESTITEMNAME"];

        //上の算出ボタン
        $btnCalc = "";
        if ($model->Properties["knjd128vShowBtnCalcOnTop"] == '1' && count($model->testcdMotoArray[$codeArray["TESTCD"]]) && $model->Properties["KNJD128V_SANSHUTU_BTN_un_hyoji"] !== '1' && $model->Properties["KNJD128V_SeisekiSanshutsuPattern"] !== '1') {
            $extra = "aria-label=\"算出\" style=\"width:50px; padding-left: 0px; padding-right: 0px;\" onClick=\"return btn_submit('calc','{$codeArray["TESTCD"]}');\" ";
            $btnCalc = "<BR>" . knjCreateBtn($objForm, "btn_calc_up-".$codeArray["TESTCD"], "算 出", $extra);
            $disBtnName .= $disBtnNameSeq."btn_calc_up-".$codeArray["TESTCD"];
            $disBtnNameSeq = ",";
        }
        //見込点ボタン
        $btnMikomi = "";
        if ($model->Properties["useMikomiFlg"] == '1' && $codeArray["MIKOMI_FLG"] == "1") {
            $jump = REQUESTROOT."/D/KNJD128V_MIKOMI/knjd128v_mikomiindex.php";
            $extra = "style=\"width:50px; padding-left: 0px; padding-right: 0px;\" onClick=\"openKogamen('{$jump}{$param}');\" id=\"btnMikomiId\" ";
            $btnMikomi = "<BR>" . knjCreateBtn($objForm, "btnMikomi".$codeArray["TESTCD"], "見込点", $extra);
            $disBtnName .= $disBtnNameSeq."btnMikomi".$codeArray["TESTCD"];
            $disBtnNameSeq = ",";
        }
        //参考点ボタン
        $btnSankou = "";
        if ($model->Properties["useSankouFlg"] == '1' && $codeArray["SANKOU_FLG"] == "1") {
            $jump = REQUESTROOT."/D/KNJD128V_SANKOU/knjd128v_sankouindex.php";
            $extra = "style=\"width:50px; padding-left: 0px; padding-right: 0px;\" onClick=\"openKogamen('{$jump}{$param}');\" id=\"btnSankouId\" ";
            $btnSankou = "<BR>" . knjCreateBtn($objForm, "btnSankou".$codeArray["TESTCD"], "参考点", $extra);
            $disBtnName .= $disBtnNameSeq."btnSankou".$codeArray["TESTCD"];
            $disBtnNameSeq = ",";
        }
        //備考ボタン
        $btnRemark = "";
        if ($model->Properties["useRemarkFlg"] == '1' && $codeArray["REMARK_FLG"] == "1") {
            $jump = REQUESTROOT."/D/KNJD128V_REMARK/knjd128v_remarkindex.php";
            $extra = "style=\"width:50px; padding-left: 0px; padding-right: 0px;\" onClick=\"openKogamen('{$jump}{$param}');\" id=\"btnRemarkId\" ";
            $btnRemark = "<BR>" . knjCreateBtn($objForm, "btnRemark".$codeArray["TESTCD"], "備考", $extra);
            $disBtnName .= $disBtnNameSeq."btnRemark".$codeArray["TESTCD"];
            $disBtnNameSeq = ",";
        }
        //改行
        $btnBR = "";
        if (strlen($btnCalc) || strlen($btnMikomi) || strlen($btnSankou) || strlen($btnRemark)) {
            $btnBR = "<BR>";
        }

        $head2 .= "<th width=57 ><font size=2>".$testNameLink."</font>{$btnBR}{$btnCalc}{$btnMikomi}{$btnSankou}{$btnRemark}</th> ";
    }
    //読み込み中はボタンをグレー用
    knjCreateHidden($objForm, "disBtnName", $disBtnName);
    knjCreateHidden($objForm, "CALC_TESTCD", "");//上の算出ボタン用
    foreach ($semArray as $key => $val) {
        $head1 .= $val;
    }
    $arg["HEAD1"] = $head1; //学期名称
    $arg["HEAD2"] = $head2; //考査種別名称
    $addSize = (10 < $count) ? ($count - 10) * 60 : 0;
    $arg["ALL_WIDTH"] = 1280 + $addSize; //画面全体幅
    $arg["FOOT_COLSPAN"] = 2 + $count; //画面全体幅
    if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
        $arg["FOOT_WIDTH"] = "*"; //課程学科コンボありの場合
    } else {
        $arg["FOOT_WIDTH"] = (0 < $count && $count < 7) ? 1280 - 20 - 278 - ($count * 65) : "*";//全体幅 - 全体幅のcellspacing*2 - 合計欄幅(width + (cellspacing + cellpadding)*2) - 成績列数*成績欄幅(width + (cellspacing + cellpadding)*2)
    }
}

//テスト取得(パーツ)
function makeTest(&$objForm, &$arg, $db, $model)
{
    //科目別設定があるか
    $testSubCnt = $db->getOne(knjd128vQuery::getTestSubCnt($model));
    if ($testSubCnt == "") {
        $testSubCnt = 0;
    }
    //テスト取得(パーツ)
    $testcdArray = array();
    $result = $db->query(knjd128vQuery::getTestName($model, $testSubCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $testcdArray[] = array("TESTCD"             => $row["TESTCD"],
                               "SEMESTERNAME"       => $row["SEMESTERNAME"],
                               "TESTITEMNAME"       => $row["TESTITEMNAME"],
                               "SIDOU_INPUT"        => $row["SIDOU_INPUT"],
                               "SIDOU_INPUT_INF"    => $row["SIDOU_INPUT_INF"],
                               "CONTROL_FLG"        => $row["CONTROL_FLG"],
                               "MIKOMI_FLG"         => $row["MIKOMI_FLG"],
                               "SANKOU_FLG"         => $row["SANKOU_FLG"],
                               "REMARK_FLG"         => $row["REMARK_FLG"],
                               "SEMESTER"           => $row["SEMESTER"],
                               "SDATE"              => $row["SDATE"],
                               "EDATE"              => $row["EDATE"]
                               );
    }
    $result->free();
    return $testcdArray;
}

//算出元テスト
function makeTestMoto(&$objForm, &$arg, $db, $model)
{
    //パーツ
    $testcdPartArray = array();
    foreach ($model->testcdArray as $key => $codeArray) {
        $testcdPartArray[] = $codeArray["TESTCD"];
    }

    //算出元科目別設定があるか
    $testSubCnt = $db->getOne(knjd128vQuery::getTestMotoSubCnt($model));
    if ($testSubCnt == "") {
        $testSubCnt = 0;
    }

    $testcdMotoArray = array();
    $result = $db->query(knjd128vQuery::getTestMoto($model, $testSubCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["TESTCD"], $testcdPartArray) && in_array($row["MOTO_TESTCD"], $testcdPartArray) && ($row["SEMESTER"] == "9" || $row["SEMESTER"] == CTRL_SEMESTER)) {
            $testcdMotoArray[$row["TESTCD"]][] = array("TESTCD" => $row["TESTCD"], "TESTNAME" => $row["TESTNAME"], "MOTO_TESTCD" => $row["MOTO_TESTCD"], "MOTO_TESTNAME" => $row["MOTO_TESTNAME"], "SEMESTER" => $row["SEMESTER"]);
        }
    }
    $result->free();
    return $testcdMotoArray;
}

//算出元テスト
function makeTestMotoPercent($db, $model, $grade, $coursecode)
{
    $subclass_array = array();
    $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
    $subclasscd_all = "00-".$subclass_array[1]."-00-000000";
    $grade_all      = "00";
    $coursecode_all = "0000";

    $sgcArray = array();
    $sgcArray[] = array($model->field["SUBCLASSCD"], $grade, $coursecode);
    $sgcArray[] = array($model->field["SUBCLASSCD"], $grade, $coursecode_all);
    $sgcArray[] = array($model->field["SUBCLASSCD"], $grade_all, $coursecode);
    $sgcArray[] = array($model->field["SUBCLASSCD"], $grade_all, $coursecode_all);
    $sgcArray[] = array($subclasscd_all, $grade, $coursecode);
    $sgcArray[] = array($subclasscd_all, $grade, $coursecode_all);
    $sgcArray[] = array($subclasscd_all, $grade_all, $coursecode);
    $sgcArray[] = array($subclasscd_all, $grade_all, $coursecode_all);

    //パーツ
    $testcdPartArray = array();
    foreach ($model->testcdArray as $key => $codeArray) {
        $testcdPartArray[] = $codeArray["TESTCD"];
    }

    $rowArray = array();
    $query = knjd128vQuery::getSubclassSemvalPercentSdivDat($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rowArray[] = $row;
    }
    $result->free();

    $testcdMotoArrayPercent = array();
    foreach ($sgcArray as $key => $val) {
        foreach ($rowArray as $key2 => $row) {
            if ($val[0] == $row["SUBCLASSCD"] && $val[1] == $row["GRADE"] && $val[2] == $row["COURSECODE"]) {
                if (in_array($row["TESTCD"], $testcdPartArray) && in_array($row["MOTO_TESTCD"], $testcdPartArray)) {
                    $testcdMotoArrayPercent[$row["TESTCD"]][] = array("TESTCD" => $row["TESTCD"], "MOTO_TESTCD" => $row["MOTO_TESTCD"], "PERCENT" => $row["PERCENT"]);
                }
            }
        }
        if (count($testcdMotoArrayPercent) > 0) {
            break;
        }
    }

    return $testcdMotoArrayPercent;
}

//明細データ
function makeMeisai(&$objForm, &$arg, $db, &$model, &$objUp, $cmCnt, $schoolKind)
{
    /*******/
    /* CSV */
    /*******/
    $subclassname = $db->getOne(knjd128vQuery::getSubclassName($model));
    $chairname = $db->getOne(knjd128vQuery::getChairName($model));
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
    $setCnt = count($csvHeader);
    foreach ($model->testcdArray as $key => $codeArray) {
        $csvHeader[$codeArray["TESTCD"]] = $codeArray["SEMESTERNAME"]."－".$codeArray["TESTITEMNAME"];
        $setType[$setCnt] = 'S';
        $setSize[$setCnt] = 3;
        $setCnt++;
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
    $schoolMstHasSchoolKind = in_array("SCHOOL_KIND", $db->getCol(knjd128vQuery::getSchoolMstCols()));
    $schoolRow = array();
    $schoolRow = $db->getRow(knjd128vQuery::getVSchoolMst($schoolMstHasSchoolKind ? $schoolKind : ''), DB_FETCHMODE_ASSOC);
    //時間割講座テストより試験日を抽出
    $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
    $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
    if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
        $execute_date = CTRL_DATE;//初期値
    } else {
        $execute_date = $edate;//初期値
    }

    //合併先科目か
    $isSubclassSaki = 0 < $db->getOne(knjd128vQuery::getCntSubclassSaki($model)) ? "1" : "";

    //累積現在日(出欠集計日)
    $shuukei_date = str_replace("-", "/", CTRL_DATE);
    $attendSM = array(); //集計学期・月の配列
    $result = $db->query(knjd128vQuery::getMax($model, $isSubclassSaki));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $attendYear = ((int)$row["MONTH"] <= 3) ? ($row["YEAR"] + 1) : $row["YEAR"];
        $attendDate = $attendYear . "/" . sprintf("%02d", $row["MONTH"]) . "/" . sprintf("%02d", $row["APPOINTED_DAY"]);
        if ($shuukei_date < $attendDate) {
            break;
        }
        $attendSM[] = $row["SEMESTER"] . $row["MONTH"];
        $arg["CUR_DATE"] = $row["YEAR"]."年度".$model->control["学期名"][$row["SEMESTER"]]."　".(int)$row["MONTH"]."月".$row["APPOINTED_DAY"]."日現在";
    }
    //印刷パラメータ(帳票KNJD620V)出欠集計日
    knjCreateHidden($objForm, "DATE", $shuukei_date);

    //初期化
    $model->data = array();
    $counter = 0;
    $model->testcdMotoArrayPercent = array();
    $model->testcdMotoArrayPercentFlg = true;

    //一覧表示
    $colorFlg = false;
    $query = knjd128vQuery::getScore($model, $execute_date, $cmCnt);
    $result = $db->query($query);
    $scoreArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号を配列で取得
        $scoreArray[$row["SCHREGNO"]][$row["TESTCD"]] = $row;
    }
    $result->free();

    //見込点データ取得
    $recinfoArray = array();
    if ($model->Properties["useMikomiFlg"] == '1') {
        $result = $db->query(knjd128vQuery::getRecordInfo($model, $execute_date, $cmCnt));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $recinfoArray[$row["SCHREGNO"]][$row["TESTCD"]] = $row;
        }
        $result->free();
    }

    $result = $db->query(knjd128vQuery::selectQuery($model, $execute_date, $cmCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->Properties["KNJD128V_SeisekiSanshutsuPattern"] == '1') {
            $model->testcdMotoArrayPercent[$row["SCHREGNO"]] = makeTestMotoPercent($db, $model, $row["GRADE"], $row["COURSECODE"]);
            if (count($model->testcdMotoArrayPercent[$row["SCHREGNO"]]) == 0) {
                $model->testcdMotoArrayPercentFlg = false;
            }
        }

        //学籍番号をHiddenで保持
        knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);
        //クラス-出席番(表示)
        if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
            $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
        }
        //氏名欄に学籍番号表記
        $row["SCHREGNO_SHOW"] = ($model->Properties["use_SchregNo_hyoji"] == 1) ? $row["SCHREGNO"]."　" : "";

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

        //算出用（専修大松戸用）
        if ($model->z010name1 == "matsudo") {
            $rate = $db->getOne(knjd128vQuery::getRate(CTRL_YEAR, $row["GRADE"], $row["COURSE"], $model));
            knjCreateHidden($objForm, "RATE"."-".$counter, $rate);
        }

        //欠課数上限値
        $query = knjd128vQuery::getKeekaOver($model, $row["SCHREGNO"], CTRL_YEAR, $row["GRADE"], $row["COURSE"], $schoolRow);
        $overRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!strlen($overRow["COMP_ABSENCE_HIGH"])) {
            $overRow["COMP_ABSENCE_HIGH"] = 9999;
        }
        //累積情報
        if ($schoolRow["ABSENT_COV"] == "0" || $schoolRow["ABSENT_COV"] == "2" || $schoolRow["ABSENT_COV"] == "4" || $schoolRow["ABSENT_COV"] == "5") {
            $query = knjd128vQuery::getAttendData($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model, $isSubclassSaki, $attendSM);
        } else {
            $query = knjd128vQuery::getAttendData2($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model, $isSubclassSaki, $attendSM);
        }
        $attendRow = array();
        $attendRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $t_lateearly = ($model->chikokuHyoujiFlg == 1) ? $attendRow["LATE_EARLY"] : $attendRow["T_LATEEARLY"];
        $row["LESSON"]      = strlen($attendRow["LESSON"])      ? $attendRow["LESSON"]      : "0";
        $row["MLESSON"]     = strlen($attendRow["MLESSON"])     ? $attendRow["MLESSON"]     : "0";
        $row["T_NOTICE"]    = strlen($attendRow["T_NOTICE"])    ? $attendRow["T_NOTICE"]    : "0";
        $row["T_LATEEARLY"] = strlen($t_lateearly)              ? $t_lateearly              : "0";
        $row["NOTICE_LATE"] = strlen($attendRow["NOTICE_LATE"]) ? $attendRow["NOTICE_LATE"] : "0";
        //名前（顔写真リンク）
        if ($model->Properties["KNJD128VPhotoLink"] == '1') {
            $linkData = "loadwindow('knjd128vindex.php?cmd=subform1&SCHREGNO=".$row["SCHREGNO"]."&ATTENDNO=".$row["ATTENDNO"]."&NAME_SHOW=".$row["NAME_SHOW"]."',0,0,250,320)";
            $row["NAME_SHOW"] = View::alink("#", htmlspecialchars($row["NAME_SHOW"]), "onclick=\"$linkData\" class=\"photo\"");
        }

        // 青山学院の特別措置者表示機能
        if ($model->z010name1 == "aoyama") {
            //(年間)授業時数と欠課時数を取得・・・画面上の値
            $lesson  = $row["LESSON"];
            $tNotice = $row["T_NOTICE"];
            $changeColor = "";
            if ($lesson < $tNotice * 2) {
                $changeColor = "RED";
            } elseif ($lesson < $tNotice * 3) {
                $changeColor = "BLUE";
            }
            knjCreateHidden($objForm, "CHANGE_COLOR_FLG3"."-".$counter, $changeColor);
            //(1学期)授業時数と欠課時数を取得
            $attendSem = array();
            $attendSem[] = "1";
            if ($schoolRow["ABSENT_COV"] == "0" || $schoolRow["ABSENT_COV"] == "2" || $schoolRow["ABSENT_COV"] == "4" || $schoolRow["ABSENT_COV"] == "5") {
                $query1 = knjd128vQuery::getAttendData($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model, $isSubclassSaki, $attendSM, $attendSem);
            } else {
                $query1 = knjd128vQuery::getAttendData2($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model, $isSubclassSaki, $attendSM, $attendSem);
            }
            $attendRow1 = array();
            $attendRow1 = $db->getRow($query1, DB_FETCHMODE_ASSOC);
            $lesson1  = strlen($attendRow1["LESSON"])   ? $attendRow1["LESSON"]   : "0";
            $tNotice1 = strlen($attendRow1["T_NOTICE"]) ? $attendRow1["T_NOTICE"] : "0";
            $changeColor1 = "";
            if ($lesson1 < $tNotice1 * 3) {
                $changeColor1 = "BLUE";
            }
            knjCreateHidden($objForm, "CHANGE_COLOR_FLG1"."-".$counter, $changeColor1);
            //(2学期)授業時数と欠課時数を取得
            $attendSem = array();
            if ($row["GRADE"] == "03") {
                $attendSem[] = "1";
            }
            $attendSem[] = "2";
            if ($schoolRow["ABSENT_COV"] == "0" || $schoolRow["ABSENT_COV"] == "2" || $schoolRow["ABSENT_COV"] == "4" || $schoolRow["ABSENT_COV"] == "5") {
                $query2 = knjd128vQuery::getAttendData($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model, $isSubclassSaki, $attendSM, $attendSem);
            } else {
                $query2 = knjd128vQuery::getAttendData2($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model, $isSubclassSaki, $attendSM, $attendSem);
            }
            $attendRow2 = array();
            $attendRow2 = $db->getRow($query2, DB_FETCHMODE_ASSOC);
            $lesson2  = strlen($attendRow2["LESSON"])   ? $attendRow2["LESSON"]   : "0";
            $tNotice2 = strlen($attendRow2["T_NOTICE"]) ? $attendRow2["T_NOTICE"] : "0";
            $changeColor2 = "";
            if ($lesson2 < $tNotice2 * 2 && $row["GRADE"] == "03") {
                $changeColor2 = "RED";
            } elseif ($lesson2 < $tNotice2 * 3) {
                $changeColor2 = "BLUE";
            }
            knjCreateHidden($objForm, "CHANGE_COLOR_FLG2"."-".$counter, $changeColor2);

            // 氏名と欠課時数の文字色を変更するため、idを設定
            $changeColorId1 = "CHANGE_COLOR_ID1"."-".$counter; //氏名
            $changeColorId2 = "CHANGE_COLOR_ID2"."-".$counter; //欠課時数
            $row["NAME_SHOW"] = "<p id={$changeColorId1}>".$row["NAME_SHOW"]."</p>";
            $row["T_NOTICE"]  = "<p id={$changeColorId2}>".$row["T_NOTICE"]."</p>";
        }

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

        //欠課時数オーバーの生徒に赤帯表示する。
        $row["NAME_COLOR"] = ($overRow["COMP_ABSENCE_HIGH"] < $row["NOTICE_LATE"]) ? "#ff0099" : $row["COLOR"];

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
            $score = "";
            if (is_array($scoreRow)) {
                $score = ($scoreRow["VALUE_DI"] == "*" || $scoreRow["VALUE_DI"] == "**") ? $scoreRow["VALUE_DI"] : $scoreRow["SCORE"];
            }
            //算出ボタン押し下げ時のみ保持（それ以外は初期化）
            if ($model->cmd == "calc") {
                $score = $model->fieldsCalc[$col."-".$counter];
                //欠課時数オーバーは未履修にする
                if (strlen($model->field["KEEKA_OVER"]) && $testcd == "9990008") {
                    //欠課数が上限値をオーバーした場合、履修・修得単位に０をセット
                    if ($overRow["COMP_ABSENCE_HIGH"] < $row["NOTICE_LATE"]) {
                        $score = "";
                    }
                }
            }
            //縦計項目
            if (is_numeric($score)) {
                $term_data[$col][] = $score;
            }

            //見込点データ
            $recinfoRow = array();
            if ($model->Properties["useMikomiFlg"] == '1') {
                $recinfoRow = $recinfoArray[$row["SCHREGNO"]][$testcd];
                if (is_numeric($recinfoRow["MIKOMI"]) && $score == "*") {
                    $term_data[$col][] = $recinfoRow["MIKOMI"];
                }
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

            //追指導データ 1:追指導入力
            $colorFlgPink = false;
            $fontcolorFlgRed = false;
            if ($codeArray["SIDOU_INPUT"] == "1") {
                $slumpRow = array();
                $query = knjd128vQuery::getRecordSlump(CTRL_YEAR, $testcd, $row["SCHREGNO"], $row["GRADE"], $model);
                $slumpRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                // 学期評価の算出用（専修大松戸用）
                if ($model->z010name1 == "matsudo" && strlen($slumpRow["SCORE"])) {
                    knjCreateHidden($objForm, $col."_SIDOU_SCORE"."-".$counter, $slumpRow["SCORE"]);
                }
                //1:記号 2:点数
                if ($codeArray["SIDOU_INPUT_INF"] == "1" && strlen($slumpRow["MARK"])) {
                    $colorFlgPink = true;
                    //1:○ 2:△ 3:×
                    if ($slumpRow["MARK"] == "3") {
                        $fontcolorFlgRed = true;
                    //追指導記号が○
                    } elseif ($slumpRow["MARK"] == "1") {
                        //算出用(評定２の下限値を元に算出)
                        knjCreateHidden($objForm, $col."_ASSESSLOW2"."-".$counter, $slumpRow["ASSESSLOW2"]);
                    }
                } elseif ($codeArray["SIDOU_INPUT_INF"] == "2" && strlen($slumpRow["SCORE"])) {
                    $colorFlgPink = true;
                    //追指導点を換算後の評定が１
                    if ($slumpRow["ASSESSLEVEL"] == "1") {
                        $fontcolorFlgRed = true;
                        //算出用(高い方を元に算出)
                        knjCreateHidden($objForm, $col."_ASSESSLOW2"."-".$counter, $slumpRow["SCORE"]);
                    //追指導点を換算後の評定が２以上
                    } elseif ("2" <= $slumpRow["ASSESSLEVEL"]) {
                        //算出用(評定２の下限値を元に算出)
                        knjCreateHidden($objForm, $col."_ASSESSLOW2"."-".$counter, $slumpRow["ASSESSLOW2"]);
                    }
                }
            }

            //CSV書出
            $csv[] = $score;

            //異動日の翌日以降は、転学、退学者は、入力不可にする。(算出用)
            knjCreateHidden($objForm, "TENTAIGAKU_FLG"."-".$counter, $tentaigakuFlg);

            //管理者コントロール
            //常盤木の場合、3年生は後期中間考査(試験項目2-01-01-01、2-01-02-01等)は管理者コントロールにかかわらず、入力不可
            $tokiwagiFlg = ($model->z010name1 == "tokiwagi" && $row["GRADE"] == "03" && substr($testcd, 0, 3) == "201") ? "1" : "";

            //欠席者で見込点があれば赤字でラベル表示・・・表示例「*50」
            if ($model->Properties["useMikomiFlg"] == '1' && is_numeric($recinfoRow["MIKOMI"]) && $score == "*") {
                $row["FONTCOLOR"] = "#ff0000";
                $row[$col] = "<font color={$row["FONTCOLOR"]}>".$score.$recinfoRow["MIKOMI"]."</font>";
                //hidden
                knjCreateHidden($objForm, $col."-".$counter, $score);
                knjCreateHidden($objForm, $col."_MIKOMI"."-".$counter, $recinfoRow["MIKOMI"]); //算出用
                //考査満点マスタ
                $query = knjd128vQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $testcd, $row["GRADE"], $row["COURSE"], $model, $schoolRow);
                $perfect = ($model->usePerfect == 'true' && substr($testcd, 5, 2) == '01' || $model->Properties["useSchoolMstSemesAssesscd"] == "1" && substr($testcd, 5, 2) == '08') ? $db->getOne($query) : 100;
                if ($perfect == "") {
                    $perfect = 100;
                }
                //満点チェック用
                //花園の場合、平常点の入力範囲は「-10～10」とする
                if ($model->z010name1 == "hanazono" && substr($testcd, 1, 2) == '99' && substr($testcd, 5, 2) == '02') {
                    knjCreateHidden($objForm, $col."_PERFECT_FROM"."-".$counter, -10);
                    $perfect = 10;
                }
                knjCreateHidden($objForm, $col."_PERFECT"."-".$counter, $perfect);
                //貼付対象フラグ
                knjCreateHidden($objForm, $col."_INPUT"."-".$counter, 0);
            } elseif ($controlFlg == "1" && (!$tentaigakuFlg || AUTHORITY == DEF_UPDATABLE) && $tokiwagiFlg != "1") {
                //テキストボックスを作成
                $row["FONTCOLOR"] = $fontcolorFlgRed ? "#ff0000" : "#000000";
                /* Edit by Kaung for PC-Talker 2020-02-10 start */
                $readLabel = $row['ATTENDNO'].$row['SCHREGNO_SHOW'].$row['NAME_SHOW']."の".$codeArray['SEMESTERNAME'].$codeArray['TESTITEMNAME'];
                $extra = " aria-label=\"$readLabel\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" id=\"{$col}-{$counter}\" onFocus=\"changeColor(this);\"";
                /* Edit by Kaung for PC-Talker 2020-02-20 end */
                $row[$col] = knjCreateTextBox($objForm, $score, $col."-".$counter, 3, 3, $extra);
                //考査満点マスタ
                $query = knjd128vQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $testcd, $row["GRADE"], $row["COURSE"], $model, $schoolRow);
                $perfect = ($model->usePerfect == 'true' && substr($testcd, 5, 2) == '01' || $model->Properties["useSchoolMstSemesAssesscd"] == "1" && substr($testcd, 5, 2) == '08') ? $db->getOne($query) : 100;
                if ($perfect == "") {
                    $perfect = 100;
                }
                //満点チェック用
                //花園の場合、平常点の入力範囲は「-10～10」とする
                if ($model->z010name1 == "hanazono" && substr($testcd, 1, 2) == '99' && substr($testcd, 5, 2) == '02') {
                    knjCreateHidden($objForm, $col."_PERFECT_FROM"."-".$counter, -10);
                    $perfect = 10;
                }
                knjCreateHidden($objForm, $col."_PERFECT"."-".$counter, $perfect);
                //入力エリアとキーをセットする
                $objUp->setElementsValue($col."-".$counter, $csvHeader[$testcd], $csvKey);
                //貼付対象フラグ
                knjCreateHidden($objForm, $col."_INPUT"."-".$counter, 1);
            //ラベルのみ
            } else {
                $row["FONTCOLOR"] = $fontcolorFlgRed ? "#ff0000" : "#000000";
                $row[$col] = "<font color={$row["FONTCOLOR"]}>".$score."</font>";
                //hidden
                knjCreateHidden($objForm, $col."-".$counter, $score);
                //考査満点マスタ
                $query = knjd128vQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $testcd, $row["GRADE"], $row["COURSE"], $model, $schoolRow);
                $perfect = ($model->usePerfect == 'true' && substr($testcd, 5, 2) == '01' || $model->Properties["useSchoolMstSemesAssesscd"] == "1" && substr($testcd, 5, 2) == '08') ? $db->getOne($query) : 100;
                if ($perfect == "") {
                    $perfect = 100;
                }
                //満点チェック用
                //花園の場合、平常点の入力範囲は「-10～10」とする
                if ($model->z010name1 == "hanazono" && substr($testcd, 1, 2) == '99' && substr($testcd, 5, 2) == '02') {
                    knjCreateHidden($objForm, $col."_PERFECT_FROM"."-".$counter, -10);
                    $perfect = 10;
                }
                knjCreateHidden($objForm, $col."_PERFECT"."-".$counter, $perfect);
                //貼付対象フラグ
                knjCreateHidden($objForm, $col."_INPUT"."-".$counter, 0);
            }
            $row["BGCOLOR"] = $row["COLOR"]; //通常の背景色
            if ($testcd != "9990009" && $colorFlgYellow) {
                $row["BGCOLOR"] = "#ffff00";
            } //異動
            //追指導未入力です。満点マスタの合格点未満（宮城県要望）
            if ($model->usePerfect == 'true' && substr($testcd, 5, 2) == '01') {
                $perfectPS = $db->getOne(knjd128vQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $testcd, $row["GRADE"], $row["COURSE"], $model, $schoolRow, "PASS"));
                if (strlen($perfectPS) && strlen($score) && $perfectPS > $score) {
                    $row["BGCOLOR"] = "violet";
                }
            }
            if ($colorFlgPink) {
                $row["BGCOLOR"] = "#ffc0cb";
            } //指導
            $meisai .= "<td width=57 align=\"center\" bgcolor={$row["BGCOLOR"]}>".$row[$col]."</td>";
        }
        $row["MEISAI"] = $meisai;

        //CSV書出
        $csv[] = $model->lastColumn;
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
            $foot["AVG"] = round((array_sum($term_data[$col])/count($term_data[$col]))*10)/10;
            //最高点と最低点を求める
            array_multisort($term_data[$col], SORT_NUMERIC);
            $max = count($term_data[$col])-1;
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
        $scoreSum .= "<th width=57 >".$foot["SUM"]."</th> ";
        $scoreAvg .= "<th width=57 >".$foot["AVG"]."</th> ";
        $scoreMax .= "<th width=57 >".$foot["MAX"]."</th> ";
        $scoreMin .= "<th width=57 >".$foot["MIN"]."</th> ";
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
        $query = knjd128vQuery::getRecordChkfinDat($model, $testcd);
        $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($resultRow)) {
            if ($resultRow["EXECUTED"] == '1') {
                $chk = ' checked="checked" ';
            } else {
                $chk = '';
            }
        }
        $dis = $controlFlg == "1" ? '' : ' disabled="disabled" ';
        /* Add by Kaung for PC-Talker 2020-02-10 */
        $readCheckbox = " aria-label=\"".$codeArray['SEMESTERNAME'].$codeArray['TESTITEMNAME']."の成績入力完了\"";
        /* End by Kaung for PC-Talker 2020-02-20 */
        $val = knjCreateCheckBox($objForm, "CHK_COMP".$testcd, "on", $chk.$dis.$readCheckbox);

        $chkComp .= "<th width=57 >".$val."</th> ";
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
    knjCreateHidden($objForm, "TEXT_FIELD_NAME", $textFieldName);
    knjCreateHidden($objForm, "CHK_COMP_NAME", $chkCompName);
    knjCreateHidden($objForm, "TEST_ITEM_NAME", $testItemName);

    //CSVファイルアップロードコントロール
    $arg["FILE"] = $objUp->toFileHtml($objForm);

    return $execute_date;
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
        if ($name == "CHAIRCD") {
            knjCreateHidden($objForm, "LIST_CHAIRCD" . $row["VALUE"], $cnt);
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
function makeBtn(&$objForm, &$arg, $db, $model, $execute_date)
{
    //算出先を表示
    $strCalc = "";
    $seqSaki = "";
    foreach ($model->testcdMotoArray as $testSaki => $array) {
        $seqMoto = "";
        $testNameSaki = "";
        $testNameMoto = "";
        foreach ($array as $key => $codeArray) {
            $testNameSaki = $codeArray["TESTNAME"];

            $testNameMoto .= $seqMoto .$codeArray["MOTO_TESTNAME"];
            $seqMoto = "、";
        }
        //$strCalc .= $seqSaki .$testNameSaki ."（".$testNameMoto."）";
        $strCalc .= $seqSaki .$testNameSaki;
        $seqSaki = "、";
    }
    $arg["str_calc"] = strlen($strCalc) && $model->Properties["KNJD128V_SeisekiSanshutsuPattern"] != '1' ? "（".$strCalc."）" : "";
    //syukketuボタン
    $syukketuDisabled = $model->field["CHAIRCD"] && $model->field["SUBCLASSCD"] ? "" : " disabled ";
    //1:出欠入力ボタン非表示
    if ($model->noUseBtnAttend !== '1') {
        $attend_prg = "/D/KNJD128V_SYUKKETU/knjd128v_syukketu";
        $D068 = $db->getOne(knjd128vQuery::getNameMstD068());
        if (strlen($D068)) {
            $attend_prg = "/".substr($D068, 3, 1)."/".$D068."/".mb_strtolower($D068);
        }
        $url = REQUESTROOT.$attend_prg."index.php?SEND_PRGID=KNJD128V&SEND_AUTH=".AUTHORITY."&SEND_SEMESTER=".CTRL_SEMESTER."&SEND_CHAIR={$model->field["CHAIRCD"]}&SEND_SUBCLASS={$model->field["SUBCLASSCD"]}&SEND_APPDATE={$execute_date}";
        $extra = "id=\"btnAttend\" onClick=\"current_cursor('btnAttend');openKogamen('{$url}');\"";
        $arg["btnAttend"] = knjCreateBtn($objForm, "btnAttend", "出欠入力", $syukketuDisabled.$extra);
    }
    knjCreateHidden($objForm, "noUseBtnAttend", $model->noUseBtnAttend);
    //算出ボタンは、窓を全て閉めた時にはグレーアウト表示にする。
    $disCalc2 = " disabled";
    foreach ($model->testcdArray as $key => $codeArray) {
        if ($codeArray["CONTROL_FLG"] == "1") {
            $disCalc2 = "";
        }
    }
    //算出ボタン
    if ($model->Properties["KNJD128V_SeisekiSanshutsuPattern"] == '1') {
        $disCalc = $model->testcdMotoArrayPercentFlg ? "" : " disabled";
    } else {
        $disCalc = strlen($strCalc) ? "" : " disabled";
    }
    //算出ボタンを上に表示(下は非表示)
    if ($model->Properties["knjd128vShowBtnCalcOnTop"] !== '1' || $model->Properties["KNJD128V_SeisekiSanshutsuPattern"] == '1') {
        /* Edit by Kaung for PC-Talker 2020-02-10 start */
        $extra = " aria-label=\"算出\" id=\"btn_calc\" onclick=\"current_cursor('btn_calc');return btn_submit('calc');\"".$disCalc.$disCalc2;
        $arg["btn_calc"] = knjCreateBtn($objForm, "btn_calc", "算 出", $syukketuDisabled.$extra);
    }
    knjCreateHidden($objForm, "knjd128vShowBtnCalcOnTop", $model->Properties["knjd128vShowBtnCalcOnTop"]);
    //算出ボタンと横の括弧を非表示
    if ($model->Properties["KNJD128V_SANSHUTU_BTN_un_hyoji"] !== '1') {
        $arg["KNJD128V_SANSHUTU_BTN_un_hyoji"] = 1;
    }
    knjCreateHidden($objForm, "KNJD128V_SANSHUTU_BTN_un_hyoji", $model->Properties["KNJD128V_SANSHUTU_BTN_un_hyoji"]);
    //更新ボタン
    $extra = " aria-label=\"更新\" id=\"btn_update\" onclick=\"current_cursor('btn_update');return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $syukketuDisabled.$extra);
    //取消ボタン
    $extra = " aria-label=\"取消\" id=\"btn_reset\" onclick=\"current_cursor('btn_reset');return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $syukketuDisabled.$extra);
    //終了ボタン
    $extra = " aria-label=\"終了\" onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //印刷ボタン
    $new_str = preg_replace("/\s+/", "", $model->wordBtnPrint);
    $extra = " aria-label=\"$new_str\" id=\"btn_print\" onclick=\"current_cursor('btn_print');return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", $model->wordBtnPrint, $syukketuDisabled.$extra);
    /* Edit by Kaung for PC-Talker 2020-02-20 end */
}

//hidden作成
function makeHidden(&$objForm, $model, $execute_date)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "STAFF", STAFFCD);
    knjCreateHidden($objForm, "CHIKOKU_HYOUJI_FLG", $model->chikokuHyoujiFlg);
    knjCreateHidden($objForm, "gen_ed", substr($model->field["SUBCLASSCD"], 0, 2) == $model->gen_ed ? $model->gen_ed : "");
    knjCreateHidden($objForm, "TEST_DATE", $execute_date);
    //印刷パラメータ(帳票KNJD620V)
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD128V");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRINT_DIV", "2"); //2:講座別
    knjCreateHidden($objForm, "REMARK_TESTCD", ""); //ブランク
    knjCreateHidden($objForm, "PRINT_JUGYO_JISU", "1"); //帳票に授業時数欄を追加
    knjCreateHidden($objForm, "category_selected"); //講座
    knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "PRINT_SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "PRINT_SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->Properties["selectSchoolKind"]);
    knjCreateHidden($objForm, "useMikomiFlg", $model->Properties["useMikomiFlg"]);
    knjCreateHidden($objForm, "useFormNameKNJD620V", $model->Properties["useFormNameKNJD620V"]);

    knjCreateHidden($objForm, "H_SUBCLASSCD");
    knjCreateHidden($objForm, "H_CHAIRCD");
    //更新権限チェック
    knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
    knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
    //講座コンボ変更時、MSG108表示用
    knjCreateHidden($objForm, "SELECT_CHAIRCD", $model->field["CHAIRCD"]);
    knjCreateHidden($objForm, "z010name1", $model->z010name1);
    knjCreateHidden($objForm, "useChairStaffOrder", $model->Properties["useChairStaffOrder"]);
}
