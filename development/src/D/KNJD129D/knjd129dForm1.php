<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd129dForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd129dindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //CSV
        $objUp = new csvFile();

        //CSV表示不可チェック
        $notUseCsv = $db->getOne(knjd129dQuery::getNameMstD058($model));
        $notCsvCnt = $db->getOne(knjd129dQuery::getNotusecsvCnt($model));
        if ($notUseCsv != "1" && $notCsvCnt == 0) {
            $arg["useCsv"] = 1;
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //科目コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjd129dQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

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
        $query = knjd129dQuery::selectChairQuery($model);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");

        //テスト取得(パーツ)
        $model->testcdArray = makeTest($objForm, $arg, $db, $model);

        //算出元テスト取得
        $model->testcdMotoArray = makeTestMoto($objForm, $arg, $db, $model);

        //明細ヘッダデータ作成
        makeHead($objForm, $arg, $db, $model);

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
        View::toHTML($model, "knjd129dForm1.html", $arg);
    }
}

//テスト取得(パーツ)
function makeTest(&$objForm, &$arg, $db, $model)
{
    //科目別設定があるか
    $testSubCnt = $db->getOne(knjd129dQuery::getTestSubCnt($model));
    if ($testSubCnt == "") {
        $testSubCnt = 0;
    }
    //テスト取得(パーツ)
    $testcdArray = array();
    $result = $db->query(knjd129dQuery::getTestName($model, $testSubCnt));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $testcdArray[$row["TESTCD"]]  = array("TESTCD"        => $row["TESTCD"],
                                "TESTITEMNAME"  => $row["TESTITEMNAME"],
                                "CONTROL_FLG"   => $row["CONTROL_FLG"],
                                "SIDOU_INPUT"   => $row["SIDOU_INPUT"],
                                "MIKOMI_FLG"    => $row["MIKOMI_FLG"],
                                "SANKOU_FLG"    => $row["SANKOU_FLG"],
                                "REMARK_FLG"    => $row["REMARK_FLG"],
                                "SEMESTER"      => $row["SEMESTER"],
                                "SEMESTERNAME"  => $row["SEMESTERNAME"],
                                "SDATE"         => $row["SDATE"],
                                "EDATE"         => $row["EDATE"],
                                "LAST_TESTCD"   => ""
                                );
        $lastTestCd = $row["TESTCD"];
    }
    $testcdArray[$lastTestCd]["LAST_TESTCD"] = "LAST";

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
    $testSubCnt = $db->getOne(knjd129dQuery::getTestMotoSubCnt($model));
    if ($testSubCnt == "") {
        $testSubCnt = 0;
    }

    $testcdMotoArray = array();
    $result = $db->query(knjd129dQuery::getTestMoto($model, $testSubCnt));
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
function makeHead(&$objForm, &$arg, $db, $model)
{
    $count = 0; //明細ヘッダの列数
    $semArray = array();
    $sem = "";
    $colspan = 0;
    $head2 = "";
    $head9 = "";
    $disCalcFlg = false; //初期値 算出ボタン有効フラグ
    $disBtnName = $disBtnNameSeq = "";
    foreach ($model->testcdArray as $key => $codeArray) {
        //学期名称
        if ($sem != $codeArray["SEMESTER"]) {
            $sem = $codeArray["SEMESTER"];
            $colspan = 0;
        }
        $colspan++;
        $semArray[$codeArray["SEMESTER"]] = "<th colspan={$colspan} ><font size=2>".$codeArray["SEMESTERNAME"]."</font></th> ";
        //算出ボタン
        $btnCalc = "";
        if (get_count($model->testcdMotoArray[$codeArray["TESTCD"]])) {
            if ($codeArray["CONTROL_FLG"] == "1" || $model->Properties["knjd129dNotOpenTextBtnCalcYuuKouFlg"] == '1') {
                $disCalcFlg = true;
            }
            $testcdSaki = $codeArray["TESTCD"];
            $testcdMoto = "";
            $motoSep = "";
            foreach ($model->testcdMotoArray[$codeArray["TESTCD"]] as $motokey => $motoCodeArray) {
                $motoControlFlg = $model->testcdArray[$motoCodeArray["MOTO_TESTCD"]]["CONTROL_FLG"];
                //if ($motoControlFlg == "1") $disCalcFlg = true;
                $testcdMoto .= $motoSep.$motoCodeArray["MOTO_TESTCD"];
                $motoSep = ",";
            }
            $disCalc = ($disCalcFlg) ? "" : " disabled ";
            if ($model->z010name1 == "bunkyo") {
                $extra = "style=\"width:35px; padding-left: 0px; padding-right: 0px;\" onclick=\"btnCalc('{$testcdSaki}', '{$testcdMoto}');\" " . $disCalc;
            } elseif ($model->z010name1 == "matsudo") {
                $extra = "style=\"width:35px; padding-left: 0px; padding-right: 0px;\" onclick=\"btnCalcMatsudo('{$testcdSaki}', '{$testcdMoto}');\" " . $disCalc;
            } else {
                $extra = "style=\"width:35px; padding-left: 0px; padding-right: 0px;\" onclick=\"btnCalcAvg('{$testcdSaki}', '{$testcdMoto}');\" " . $disCalc;
            }
            $btnCalc = "<BR>" . knjCreateBtn($objForm, "btn_calc", "算出", $extra);
            $disCalcFlg = false; //初期値
        }
        //考査名
        if ($codeArray["SIDOU_INPUT"] == "1") { //追指導入力
            //リンク元のプログラムＩＤ
            $prgid = "KNJD129D";
            $auth = AUTHORITY;
            //リンク先のURL
            $jump = REQUESTROOT."/D/KNJD128V_2/knjd128v_2index.php";
            //URLパラメータ
            $param  = "?prgid={$prgid}";
            $param .= "&auth={$auth}";
            $param .= "&SUBCLASSCD={$model->field["SUBCLASSCD"]}";
            $param .= "&CHAIRCD={$model->field["CHAIRCD"]}";
            $param .= "&TESTCD={$codeArray["TESTCD"]}";
            //考査名にリンクを作成
            $extra = "onClick=\"openKogamen('{$jump}{$param}');\"";
            $testNameLink = View::alink("#", htmlspecialchars($codeArray["TESTITEMNAME"]), $extra);
        } else {
            $testNameLink = $codeArray["TESTITEMNAME"];
        }

        //見込点ボタン
        $btnMikomi = "";
        if ($model->Properties["useMikomiFlg"] == '1' && $codeArray["MIKOMI_FLG"] == "1") {
            //リンク元のプログラムＩＤ
            $prgid = "KNJD129D";
            $auth = AUTHORITY;
            //リンク先のURL
            $jump = REQUESTROOT."/D/KNJD128V_MIKOMI/knjd128v_mikomiindex.php";
            //URLパラメータ
            $param  = "?prgid={$prgid}";
            $param .= "&auth={$auth}";
            $param .= "&SUBCLASSCD={$model->field["SUBCLASSCD"]}";
            $param .= "&CHAIRCD={$model->field["CHAIRCD"]}";
            $param .= "&TESTCD={$codeArray["TESTCD"]}";
            $extra = "style=\"width:50px; padding-left: 0px; padding-right: 0px;\" onClick=\"openKogamen('{$jump}{$param}');\" id=\"btnMikomiId\" ";
            $btnMikomi = "<BR>" . knjCreateBtn($objForm, "btnMikomi".$codeArray["TESTCD"], "見込点", $extra);
            $disBtnName .= $disBtnNameSeq."btnMikomi".$codeArray["TESTCD"];
            $disBtnNameSeq = ",";
        }
        //参考点ボタン
        $btnSankou = "";
        if ($model->Properties["useSankouFlg"] == '1' && $codeArray["SANKOU_FLG"] == "1") {
            //リンク元のプログラムＩＤ
            $prgid = "KNJD129D";
            $auth = AUTHORITY;
            //リンク先のURL
            $jump = REQUESTROOT."/D/KNJD128V_SANKOU/knjd128v_sankouindex.php";
            //URLパラメータ
            $param  = "?prgid={$prgid}";
            $param .= "&auth={$auth}";
            $param .= "&SUBCLASSCD={$model->field["SUBCLASSCD"]}";
            $param .= "&CHAIRCD={$model->field["CHAIRCD"]}";
            $param .= "&TESTCD={$codeArray["TESTCD"]}";
            $extra = "style=\"width:50px; padding-left: 0px; padding-right: 0px;\" onClick=\"openKogamen('{$jump}{$param}');\" id=\"btnSankouId\" ";
            $btnSankou = "<BR>" . knjCreateBtn($objForm, "btnSankou".$codeArray["TESTCD"], "参考点", $extra);
            $disBtnName .= $disBtnNameSeq."btnSankou".$codeArray["TESTCD"];
            $disBtnNameSeq = ",";
        }
        //備考ボタン
        $btnRemark = "";
        if ($model->Properties["useRemarkFlg"] == '1' && $codeArray["REMARK_FLG"] == "1") {
            //リンク元のプログラムＩＤ
            $prgid = "KNJD129D";
            $auth = AUTHORITY;
            //リンク先のURL
            $jump = REQUESTROOT."/D/KNJD128V_REMARK/knjd128v_remarkindex.php";
            //URLパラメータ
            $param  = "?prgid={$prgid}";
            $param .= "&auth={$auth}";
            $param .= "&SUBCLASSCD={$model->field["SUBCLASSCD"]}";
            $param .= "&CHAIRCD={$model->field["CHAIRCD"]}";
            $param .= "&TESTCD={$codeArray["TESTCD"]}";
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

        //学年末以外
        if ($codeArray["SEMESTER"] != "9") {
            if ($codeArray["LAST_TESTCD"] == "LAST") {
                $head2 .= "<th width=* ><font size=2>{$testNameLink}</font>{$btnBR}{$btnCalc}{$btnMikomi}{$btnSankou}{$btnRemark}</th> ";
            } else {
                $head2 .= "<th width=75 ><font size=2>{$testNameLink}</font>{$btnBR}{$btnCalc}{$btnMikomi}{$btnSankou}{$btnRemark}</th> ";
            }
            $count++;
        //学年末
        } else {
            if ($codeArray["TESTCD"] == "9990009") {
                $head9 .= "<th rowspan=2 width=* ><font size=2>{$testNameLink}</font>{$btnBR}{$btnCalc}{$btnMikomi}{$btnSankou}{$btnRemark}</th> ";
                $count++;
            } else {
                if ($codeArray["LAST_TESTCD"] == "LAST") {
                    $head9 .= "<th rowspan=2 width=* ><font size=2>{$testNameLink}</font>{$btnBR}{$btnCalc}{$btnMikomi}{$btnSankou}{$btnRemark}</th> ";
                } else {
                    $head9 .= "<th rowspan=2 width=75 ><font size=2>{$testNameLink}</font>{$btnBR}{$btnCalc}{$btnMikomi}{$btnSankou}{$btnRemark}</th> ";
                }
                $count++;
            }
        }
    }
    //読み込み中はボタンをグレー用
    knjCreateHidden($objForm, "disBtnName", $disBtnName);
    //学年末以外
    $head1 = "";
    foreach ($semArray as $key => $val) {
        if ($key == "9") {
            continue;
        }
        $head1 .= $val;
    }
    $arg["HEAD1"] = $head1; //学期名称
    $arg["HEAD2"] = $head2; //考査種別名称
    $arg["HEAD9"] = $head9; //学年末
    $addSize = (10 < $count) ? ($count - 10) * 75 : 0;
    $arg["ALL_WIDTH"] = 1180 + $addSize; //画面全体幅
//    $arg["ALL_WIDTH"] = 1180; //画面全体幅
//    $arg["ALL_WIDTH"] = 1280; //画面全体幅
//    $arg["ALL_WIDTH"] = "100%"; //画面全体幅
    $arg["FOOT_COLSPAN"] = 2 + $count;
}

//明細データ
function makeMeisai(&$objForm, &$arg, $db, &$model, &$objUp)
{
    /*******/
    /* CSV */
    /*******/
    $subclassname = $db->getOne(knjd129dQuery::getSubclassName($model));
    $chairname = $db->getOne(knjd129dQuery::getChairName($model));
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
    $csvHeader[] = $model->lastColumn;
    $objUp->setHeader(array_values($csvHeader));
    $objUp->setType($setType);
    $objUp->setSize($setSize);
    //ゼロ埋めフラグ
    $csvFlg = array("科目コード" => array(false,13),
                    "講座コード" => array(true,7),
                    "学籍番号"   => array(true,8));
    $objUp->setEmbed_flg($csvFlg);

    //学校詳細マスタ
    $schoolMstHasSchoolKind = in_array("SCHOOL_KIND", $db->getCol(knjd129dQuery::getSchoolMstCols()));
    $schoolRow = array();
    $schoolRow = $db->getRow(knjd129dQuery::getVSchoolMst($schoolMstHasSchoolKind ? $model->school_kind: ''), DB_FETCHMODE_ASSOC);
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

    //一覧表示
    $colorFlg = false;
    $query = knjd129dQuery::getScore($model, $execute_date);
    $result = $db->query($query);
    $scoreArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号を配列で取得
        $scoreArray[$row["SCHREGNO"]][$row["TESTCD"]] = $row;
    }
    $result->free();

    //算出先も更新するため、算出先の配列を作成
    $testSakiArray = array();
    foreach ($model->testcdMotoArray as $testSaki => $codeArray) {
        if ($model->Properties["knjd129dNotOpenTextBtnCalcYuuKouFlg"] == '1') {
            $testSakiArray[] = $testSaki;
        }
    }

    $result = $db->query(knjd129dQuery::selectQuery($model, $execute_date));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号をHiddenで保持
        knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);
        //クラス-出席番(表示)
        if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
            $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
        }
        //名前
        //$row["NAME_SHOW"] = $row["SCHREGNO"] ." ". $row["NAME_SHOW"];

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

        //５行毎に背景色を変更
        if ($counter % 5 == 0) {
            $colorFlg = !$colorFlg;
        }

        //割合設定
        $query = knjd129dQuery::getPercent(CTRL_YEAR, $testcd, $row["GRADE"], $row["COURSE"], $model);
        $percent = $db->getOne($query);
        if ($percent == "") {
            $percent = 70;
        }
        //算出用
        knjCreateHidden($objForm, "PERCENT"."-".$counter, $percent);

        //算出用（専修大松戸用）
        if ($model->z010name1 == "matsudo") {
            $rate = $db->getOne(knjd129dQuery::getRate(CTRL_YEAR, $row["GRADE"], $row["COURSE"], $model));
            knjCreateHidden($objForm, "RATE"."-".$counter, $rate);
        }

        //入力可能なテキストの名前を取得する
        $setTextField = "";
        $textSep = "";
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];
            $col = "SCORE" .$testcd;

            //異動情報
            $colorFlgYellow = false;
            $kyuugakuFlg = false;
            //留学のみ
            if ($testcd != "9990009" && (strlen($row["TRANSFER_SDATE1"]) || strlen($row["TRANSFER_EDATE1"]))) {
                //学期期間中すべて異動期間の場合
                if (strtotime($row["TRANSFER_SDATE1"]) <= strtotime($codeArray["SDATE"])
                       && strtotime($row["TRANSFER_EDATE1"]) >= strtotime($codeArray["EDATE"])) {
                    $colorFlgYellow = true;
                    $kyuugakuFlg = true;
                //一部
                } elseif ((strtotime($row["TRANSFER_SDATE1"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_SDATE1"]) <= strtotime($codeArray["EDATE"]))) {
                    $colorFlgYellow = true;
                    $kyuugakuFlg = true;
                } elseif ((strtotime($row["TRANSFER_EDATE1"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_EDATE1"]) <= strtotime($codeArray["EDATE"]))) {
                    $colorFlgYellow = true;
                    $kyuugakuFlg = true;
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
            }
            //卒業日付
            if ($testcd != "9990009" && strlen($row["GRD_DATE"])) {
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
            if ($controlFlg == "1" && !strlen($scoreRow["SCORE_PASS"])) {
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
            //$row[$col."_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            //成績データ
            $scoreRow = $scoreArray[$row["SCHREGNO"]][$testcd];
            $score = ($scoreRow["VALUE_DI"] == "*" || $scoreRow["VALUE_DI"] == "**") ? $scoreRow["VALUE_DI"] : $scoreRow["SCORE"];
            //縦計項目
            if (is_numeric($score)) {
                $term_data[$col][] = $score;
            }

            //見込点データ
            if ($model->Properties["useMikomiFlg"] == '1' && strlen($scoreRow["SCORE_PASS"])) {
                $term_data[$col][] = $scoreRow["SCORE_PASS"];
            }

            //追指導データ・・・学期評価の算出用（専修大松戸用）
            if ($model->z010name1 == "matsudo" && strlen($scoreRow["SIDOU_SCORE"])) {
                knjCreateHidden($objForm, $col."_SIDOU_SCORE"."-".$counter, $scoreRow["SIDOU_SCORE"]);
            }

            //異動情報
            $colorFlgYellow = false;
            $kyuugakuFlg = false;
            //留学のみ
            if ($testcd != "9990009" && (strlen($row["TRANSFER_SDATE1"]) || strlen($row["TRANSFER_EDATE1"]))) {
                //学期期間中すべて異動期間の場合
                if (strtotime($row["TRANSFER_SDATE1"]) <= strtotime($codeArray["SDATE"])
                       && strtotime($row["TRANSFER_EDATE1"]) >= strtotime($codeArray["EDATE"])) {
                    $colorFlgYellow = true;
                    $kyuugakuFlg = true;
                //一部
                } elseif ((strtotime($row["TRANSFER_SDATE1"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_SDATE1"]) <= strtotime($codeArray["EDATE"]))) {
                    $colorFlgYellow = true;
                    $kyuugakuFlg = true;
                } elseif ((strtotime($row["TRANSFER_EDATE1"]) >= strtotime($codeArray["SDATE"]))
                       && (strtotime($row["TRANSFER_EDATE1"]) <= strtotime($codeArray["EDATE"]))) {
                    $colorFlgYellow = true;
                    $kyuugakuFlg = true;
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
            }
            //卒業日付
            if ($testcd != "9990009" && strlen($row["GRD_DATE"])) {
                //学期期間中すべて卒業の場合(学期開始日付以前に卒業している場合）
                if (strtotime($row["GRD_DATE"]) <= strtotime($codeArray["SDATE"])) {
                    $colorFlgYellow = true;
                //一部
                } elseif (strtotime($row["GRD_DATE"]) >  strtotime($codeArray["SDATE"])
                       && strtotime($row["GRD_DATE"]) <= strtotime($codeArray["EDATE"])) {
                    $colorFlgYellow = true;
                }
            }

            //CSV書出
            $csv[] = $score;

            //管理者コントロール
            if ($controlFlg == "1" && !strlen($scoreRow["SCORE_PASS"])) {
                //テキストボックスを作成
                $row["FONTCOLOR"] = "#000000";
                $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" id=\"{$col}-{$counter}\"";
                $row[$col] = knjCreateTextBox($objForm, $score, $col."-".$counter, 3, 3, $extra);
                //考査満点マスタ
                $query = knjd129dQuery::getPerfect(CTRL_YEAR, $testcd, $row["GRADE"], $row["COURSE"], $model, $schoolRow);
                $perfect = "";
                if ($model->usePerfect == 'true' || $model->Properties["useSchoolMstSemesAssesscd"] == "1" && substr($testcd, 5, 2) == '08') {
                    $perfect = $db->getOne($query);
                }
                if ($perfect == "") {
                    if ($model->Properties["useSchoolMstSemesAssesscd"] == "1" && substr($testcd, 5, 2) == '09') {
                        $perfect = 5;
                    } else {
                        $perfect = 100;
                    }
                    if ($testcd == "9990008") {
                        $perfect = 200;
                    }
                }
                //満点チェック用
                knjCreateHidden($objForm, $col."_PERFECT"."-".$counter, $perfect);
                //入力エリアとキーをセットする
                $objUp->setElementsValue($col."-".$counter, $csvHeader[$testcd], $csvKey);
            //欠席者で見込点があれば赤字でラベル表示
            } elseif ($model->Properties["useMikomiFlg"] == '1' && strlen($scoreRow["SCORE_PASS"])) {
                $row["FONTCOLOR"] = "#ff0000";
                if (in_array($testcd, $testSakiArray)) {
                    $spanId = $col."_TESTCDSAKI_ID"."-".$counter; //算出先
                    $row[$col] = "<font color={$row["FONTCOLOR"]}><span id={$spanId}>".$score.$scoreRow["SCORE_PASS"]."</span></font>"; //表示例「*50」
                } else {
                    $row[$col] = "<font color={$row["FONTCOLOR"]}>".$score.$scoreRow["SCORE_PASS"]."</font>"; //表示例「*50」
                }
                //hidden
                knjCreateHidden($objForm, $col."-".$counter, $score);
                knjCreateHidden($objForm, $col."_PASS"."-".$counter, $scoreRow["SCORE_PASS"]); //算出用
            //ラベルのみ
            } else {
                $row["FONTCOLOR"] = "#000000";
                //(pink)見込点は、あればラベル表示
                //但し、算出・更新に見込点は含めない（仕様未定のため、とりあえずこのまま）
                if (in_array($testcd, $testSakiArray)) {
                    $spanId = $col."_TESTCDSAKI_ID"."-".$counter; //算出先
                    if (strlen($scoreRow["SCORE_PASS"])) {
                        $row[$col] = "<font color={$row["FONTCOLOR"]}><span id={$spanId}>".$scoreRow["SCORE_PASS"]."</span></font>";
                    } else {
                        $row[$col] = "<font color={$row["FONTCOLOR"]}><span id={$spanId}>".$score."</span></font>";
                    }
                } else {
                    if (strlen($scoreRow["SCORE_PASS"])) {
                        $row[$col] = "<font color={$row["FONTCOLOR"]}>".$scoreRow["SCORE_PASS"]."</font>";
                    } else {
                        $row[$col] = "<font color={$row["FONTCOLOR"]}>".$score."</font>";
                    }
                }
                //hidden
                knjCreateHidden($objForm, $col."-".$counter, $score);
            }

            $row["BGCOLOR"] = $row["COLOR"]; //通常の背景色
            if ($testcd != "9990009" && $colorFlgYellow) {
                $row["BGCOLOR"] = "#ffff00";
            } //異動
            if ($model->Properties["useMikomiFlg"] !== '1' && strlen($scoreRow["SCORE_PASS"])) {
                $row["BGCOLOR"] = "#ffc0cb";
            } //見込点(pink)
            if ($codeArray["LAST_TESTCD"] == "LAST") {
                $meisai .= "<td width=* align=\"center\" bgcolor={$row["BGCOLOR"]}>".$row[$col]."</td>";
            } else {
                $meisai .= "<td width=75 align=\"center\" bgcolor={$row["BGCOLOR"]}>".$row[$col]."</td>";
            }
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
            $scoreSum .= "<th width=* >".$foot["SUM"]."</th> ";
            $scoreAvg .= "<th width=* >".$foot["AVG"]."</th> ";
            $scoreMax .= "<th width=* >".$foot["MAX"]."</th> ";
            $scoreMin .= "<th width=* >".$foot["MIN"]."</th> ";
        } else {
            if ($codeArray["LAST_TESTCD"] == "LAST") {
                $scoreSum .= "<th width=* >".$foot["SUM"]."</th> ";
                $scoreAvg .= "<th width=* >".$foot["AVG"]."</th> ";
                $scoreMax .= "<th width=* >".$foot["MAX"]."</th> ";
                $scoreMin .= "<th width=* >".$foot["MIN"]."</th> ";
            } else {
                $scoreSum .= "<th width=75 >".$foot["SUM"]."</th> ";
                $scoreAvg .= "<th width=75 >".$foot["AVG"]."</th> ";
                $scoreMax .= "<th width=75 >".$foot["MAX"]."</th> ";
                $scoreMin .= "<th width=75 >".$foot["MIN"]."</th> ";
            }
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
        $query = knjd129dQuery::getRecordChkfinDat($model, $testcd);
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
            $chkComp .= "<th width=* >".$val."</th> ";
        } else {
            if ($codeArray["LAST_TESTCD"] == "LAST") {
                $chkComp .= "<th width=* >".$val."</th> ";
            } else {
                $chkComp .= "<th width=75 >".$val."</th> ";
            }
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
function makeBtn(&$objForm, &$arg, $model, $execute_date)
{
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
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "小 表(確認用)", $syukketuDisabled.$extra);
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
    knjCreateHidden($objForm, "PRGID", "KNJD129D");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRINT_DIV", "2"); //2:講座別
    knjCreateHidden($objForm, "REMARK_TESTCD", ""); //ブランク
    knjCreateHidden($objForm, "category_selected"); //講座
    knjCreateHidden($objForm, "useMikomiFlg", $model->Properties["useMikomiFlg"]);
    knjCreateHidden($objForm, "ignoreBlankKesshi", $model->Properties["ignoreBlankKesshi"]);
    knjCreateHidden($objForm, "z010name1", $model->z010name1);

    knjCreateHidden($objForm, "H_SUBCLASSCD");
    knjCreateHidden($objForm, "H_CHAIRCD");
    //更新権限チェック
    knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
    knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
    //講座コンボ変更時、MSG108表示用
    knjCreateHidden($objForm, "SELECT_CHAIRCD", $model->field["CHAIRCD"]);
    //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
    knjCreateHidden($objForm, "CalcMethod", $model->CalcMethod);
}
