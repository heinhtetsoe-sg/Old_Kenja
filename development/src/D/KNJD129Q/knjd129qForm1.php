<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd129qForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //CSV
        $objUp = new csvFile();

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd129qindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //課程学科コンボ
        $cmCnt = 0;
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["use_school_detail_gcm_dat"] = 1;
            $extra = "onChange=\"btn_submit('main')\";";
            $query = knjd129qQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1, "");

            $cmCnt = get_count($db->getCol(knjd129qQuery::getCourseMajor($model)));
        }

        //CSV表示不可チェック
        $useCsv = $db->getOne(knjd129qQuery::getNameMstD058());
        $notCsvCnt = $db->getOne(knjd129qQuery::getNotusecsvCnt($model));
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
            $result = $db->query(knjd129qQuery::getSemester());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $hankiCnt = $db->getOne(knjd129qQuery::getHankiCnt($row["SEMESTER"]));
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
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjd129qQuery::getSubclassMst($model, $cmCnt);
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
            } elseif ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    list($selClass, $selKind, $selCurri, $selSubclass) = explode("-", $model->field["SUBCLASSCD"]);
                    $schoolKind = $selKind;
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $schoolKind = SCHOOLKIND;
            } else {
                $schoolKind = $db->getOne(knjd129qQuery::getSchoolkindQuery());
            }
            $model->subclassAll = "00-".$schoolKind."-00-000000";
        } else {
            $model->subclassAll = "000000";
        }

        //集団ラジオ
        knjCreateHidden($objForm, "showBunpuHyou", $model->Properties["showBunpuHyou"]);
        if ($model->Properties["showBunpuHyou"] == '1') {
            $arg["showBunpuHyou"] = 1;
            //1:講座 2:学年 3:コース 4:講座グループ
            $opt = array(1, 2, 3, 4);
            $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
            $extra = array();
            foreach ($opt as $key => $val) {
                array_push($extra, " id=\"GROUP_DIV{$val}\" onclick=\"btn_submit('main');\"");
            }
            $radioArray = knjCreateRadio($objForm, "GROUP_DIV", $model->field["GROUP_DIV"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg[$key] = $val;
            }
            //講座コンボ
            if ($model->field["GROUP_DIV"] == "1") {
                $extra = "onChange=\"btn_submit('main')\";";
                $query = knjd129qQuery::selectChairQuery($model, $cmCnt);
                makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");
                knjCreateHidden($objForm, "H_CHAIRCD");
            }
            //学年コンボ
            if ($model->field["GROUP_DIV"] == "2") {
                $extra = "onChange=\"btn_submit('main')\";";
                $query = knjd129qQuery::getGrade($model);
                makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "blank");
                knjCreateHidden($objForm, "H_GRADE");
            }
            //コースコンボ
            if ($model->field["GROUP_DIV"] == "3") {
                $extra = "onChange=\"btn_submit('main')\";";
                $query = knjd129qQuery::getCourse($model);
                makeCmb($objForm, $arg, $db, $query, "GRADE_COURSE", $model->field["GRADE_COURSE"], $extra, 1, "blank");
                knjCreateHidden($objForm, "H_GRADE_COURSE");
            }
            //講座グループコンボ
            if ($model->field["GROUP_DIV"] == "4") {
                $extra = "onChange=\"btn_submit('main')\";";
                $query = knjd129qQuery::getChairGroup($model);
                makeCmb($objForm, $arg, $db, $query, "CHAIR_GROUP_CD", $model->field["CHAIR_GROUP_CD"], $extra, 1, "blank");
                knjCreateHidden($objForm, "H_CHAIR_GROUP_CD");
            }
        } else {
            $arg["notShowBunpuHyou"] = 1;
            //講座コンボ
            $extra = "onChange=\"btn_submit('chaircd')\";";
            $query = knjd129qQuery::selectChairQuery($model, $cmCnt);
            makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "blank");
            //hidden
            knjCreateHidden($objForm, "H_CHAIRCD");
        }

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

        //明細ヘッダデータ作成
        $model->testcdArray = makeHead($objForm, $arg, $db, $model);

        //明細データ作成
        makeMeisai($objForm, $arg, $db, $model, $objUp);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model, $execute_date);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd129qForm1.html", $arg);
    }
}

//明細ヘッダ
function makeHead(&$objForm, &$arg, $db, $model)
{
    //科目別設定があるか
    $testSubCnt = $db->getOne(knjd129qQuery::getTestSubCnt($model));
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
    $prgid = "KNJD129Q";
    $auth = AUTHORITY;
    //リンク先のURL
    $jump = REQUESTROOT."/D/KNJD128V_2/knjd128v_2index.php";
    $result = $db->query(knjd129qQuery::getTestName($model, $testSubCnt));
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
                $extra = "onClick=\"return check_all(this);\"";
                $chk = $model->field["PROV_FLG_ALL"] == '1' ? ' checked="checked" ' : '';
                $dis = $row["CONTROL_FLG"] == '1' ? '' : ' disabled="disabled" ';
                $prov_flg_all = knjCreateCheckBox($objForm, "PROV_FLG_ALL", "1", $extra.$chk.$dis);

                $head2 .= "<th rowspan=2 width=25><font size=2>仮<br>評<br>定</font>{$prov_flg_all}</th> ";
            }
        }

        //URLパラメータ
        $param  = "?prgid={$prgid}";
        $param .= "&auth={$auth}";
        $param .= "&SUBCLASSCD={$model->field["SUBCLASSCD"]}";
        $param .= "&CHAIRCD={$model->field["CHAIRCD"]}";
        $param .= "&TESTCD={$row["TESTCD"]}";
        //考査名にリンクを作成
        $extra = "onClick=\"openKogamen('{$jump}{$param}');\"";
        $testNameLink = ($row["SIDOU_INPUT"] == "1" && $model->Properties["showHeikinran"] != '1' && $model->Properties["showBunpuHyou"] != '1') ? View::alink("#", htmlspecialchars($row["TESTITEMNAME"]), $extra) : $row["TESTITEMNAME"];

        //表示順リンク
        $mark = "";
        if ($model->Properties["showHeikinran"] == '1' && $row["TESTCD"] == "9990008") {
            $extra = "onClick=\"return sort_submit('{$row["TESTCD"]}');\"";
            $testNameLink = View::alink("#", htmlspecialchars($row["TESTITEMNAME"]), $extra);
            $mark = ($model->field["SORT_DIV"] == $row["TESTCD"]) ? "<br>▼" : "";
        }

        $head2 .= "<th rowspan=2 width=60 ><font size=2>".$testNameLink.$mark."</font></th> ";
        //平均点欄を表示・・・入力不可（表示のみ）
        if ($model->Properties["showHeikinran"] == '1' && $row["TESTCD"] == "9990008") {
            $arg["showHeikinran"] = 1;
            $sortValue = $row["TESTCD"]."_2";
            $sortLabel = "平均点";
            $extra = "onClick=\"return sort_submit('{$sortValue}');\"";
            $sortLink = View::alink("#", htmlspecialchars($sortLabel), $extra);
            $mark = ($model->field["SORT_DIV"] == $sortValue) ? "<br>▼" : "";
            $head2 .= "<th rowspan=2 width=60 ><font size=2>".$sortLink.$mark."</font></th> ";
        }
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
    //表示順リンク
    $arg["HEAD_ATTENDNO"] = "出席番号";
    if ($model->Properties["showHeikinran"] == '1') {
        $model->field["SORT_DIV"] = ($model->field["SORT_DIV"] == "") ? "1" : $model->field["SORT_DIV"];
        $sortValue = "1";
        $sortLabel = "出席番号";
        $extra = "onClick=\"return sort_submit('{$sortValue}');\"";
        $sortLink = View::alink("#", htmlspecialchars($sortLabel), $extra);
        $mark = ($model->field["SORT_DIV"] == $sortValue) ? "<br>▲" : "";
        $arg["HEAD_ATTENDNO"] = $sortLink.$mark;
        knjCreateHidden($objForm, "SORT_DIV");
    }
    return $testcdArray;
}

//明細データ
function makeMeisai(&$objForm, &$arg, $db, &$model, &$objUp)
{
    /*******/
    /* CSV */
    /*******/
    $subclassname = $db->getOne(knjd129qQuery::getSubclassName($model));
    $chairname = $db->getOne(knjd129qQuery::getChairName($model));
    //集団ラジオ
    if ($model->Properties["showBunpuHyou"] == '1') {
        //学年コンボ
        if ($model->field["GROUP_DIV"] == "2") {
            $query = knjd129qQuery::getGrade($model, $model->field["GRADE"]);
            $groupRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $chairname = $groupRow["LABEL_CSV"];
        }
        //コースコンボ
        if ($model->field["GROUP_DIV"] == "3") {
            $query = knjd129qQuery::getCourse($model, $model->field["GRADE_COURSE"]);
            $groupRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $chairname = $groupRow["LABEL_CSV"];
        }
        //講座グループコンボ
        if ($model->field["GROUP_DIV"] == "4") {
            $query = knjd129qQuery::getChairGroup($model, $model->field["CHAIR_GROUP_CD"]);
            $groupRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $chairname = $groupRow["LABEL_CSV"];
        }
    }
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
        //平均点欄を表示・・・入力不可（表示のみ）
        if ($model->Properties["showHeikinran"] == '1' && $codeArray["TESTCD"] == "9990008") {
            $csvHeader[$setCnt] = "平均点";
            $setType[$setCnt] = 'S';
            $setSize[$setCnt] = 5;
            $setCnt++;
        }
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
    $schoolRow = array();
    $schoolRow = $db->getRow(knjd129qQuery::getVSchoolMst(), DB_FETCHMODE_ASSOC);
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

    //講座リストを取得する
    $chairList = array();

    //一覧表示
    $colorFlg = false;
    $query = knjd129qQuery::getScore($model, $execute_date);
    $result = $db->query($query);
    $scoreArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号を配列で取得
        $scoreArray[$row["SCHREGNO"]][$row["TESTCD"]] = $row;
    }
    $result->free();

    $result = $db->query(knjd129qQuery::selectQuery($model, $execute_date));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学籍番号をHiddenで保持
        knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]."-".$row["CHAIRCD"]);
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

        //講座リストを取得する
        $chairList[$row["CHAIRCD"]] = $row["CHAIRCD"];

        //書出用CSVデータ
        $csv = array($model->field["SUBCLASSCD"],
                     $subclassname,
                     $row["CHAIRCD"],
                     $row["CHAIRNAME"],
                     $row["SCHREGNO"],
                     $row["ATTENDNO"],
                     $row["NAME_SHOW"]);
        //キー値をセット
        $csvKey = array("科目コード" => $model->field["SUBCLASSCD"],
                        "講座コード" => $row["CHAIRCD"],
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
                //平均点欄を表示・・・入力不可（表示のみ）
                if ($model->Properties["showHeikinran"] == '1' && $testcd == "9990008") {
                    if (is_numeric($row["SCHNO_AVG"])) {
                        $term_data["SCHNO_AVG"][] = $row["SCHNO_AVG"];
                    }
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
            if ($codeArray["SIDOU_INPUT"] == "1") {
                $slumpRow = array();
                $query = knjd129qQuery::getRecordSlump(CTRL_YEAR, $testcd, $row["SCHREGNO"], $row["GRADE"], $model);
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
            if ($controlFlg == "1") {
                //テキストボックスを作成
                $row["FONTCOLOR"] = $fontcolorFlgRed ? "#ff0000" : "#000000";
                $extra = " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right; color:{$row["FONTCOLOR"]};\" onChange=\"this.style.background='#ccffcc'; updateBunpu(this, {$testcd});\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this);\" id=\"{$col}-{$counter}\" ";
                $row[$col] = knjCreateTextBox($objForm, $score, $col."-".$counter, 3, 3, $extra);
                //テキストの名前をセット
                if ($counter == 0) {
                    $textFieldName .= $textSep.$col;
                    $textSep = ",";
                }
                //考査満点マスタ
                $query = knjd129qQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $testcd, $row["GRADE"], $row["COURSE"], $model);
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
                //履修単位・修得単位
                if ($testcd == "9990009") {
                    $row["COMP_CREDIT"] = "<font color=\"#000000\">".$scoreRow["COMP_CREDIT"]."</font>";
                    $row["GET_CREDIT"] = "<font color=\"#000000\">".$scoreRow["GET_CREDIT"]."</font>";
                }
            }

            //CSV書出
            $csv[] = $score;
            //平均点欄を表示・・・入力不可（表示のみ）
            if ($model->Properties["showHeikinran"] == '1' && $testcd == "9990008") {
                $csv[] = $row["SCHNO_AVG"];
            }
            //履修単位・修得単位
            if ($testcd == "9990009") {
                $csv[] = $scoreRow["COMP_CREDIT"];
                $csv[] = $scoreRow["GET_CREDIT"];
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
            if ($testcd != "9990009" && $colorFlgYellow) {
                $row["BGCOLOR"] = "#ffff00";
            } //異動
            if ($colorFlgPink) {
                $row["BGCOLOR"] = "#ffc0cb";
            } //指導
            $meisai .= "<td width=60 align=\"center\" bgcolor={$row["BGCOLOR"]}>".$row[$col]."</td>";
            //平均点欄を表示・・・入力不可（表示のみ）
            if ($model->Properties["showHeikinran"] == '1' && $testcd == "9990008") {
                $row["SCHNO_AVG"] = "<font color=\"#000000\">".$row["SCHNO_AVG"]."</font>";
                $meisai .= "<td width=60 align=\"center\" bgcolor={$row["COLOR"]}>".$row["SCHNO_AVG"]."</td>";
            }
            //履修単位・修得単位
            if ($testcd == "9990009") {
                $meisai .= "<td width=60 align=\"center\" bgcolor={$row["COLOR"]}>".$row["COMP_CREDIT"]."</td>";
                $meisai .= "<td width=60 align=\"center\" bgcolor={$row["COLOR"]}>".$row["GET_CREDIT"]."</td>";
            }
        }
        $row["MEISAI"] = $meisai;
        //累積情報
        if ($schoolRow["ABSENT_COV"] == "0" || $schoolRow["ABSENT_COV"] == "2" || $schoolRow["ABSENT_COV"] == "4" || $schoolRow["ABSENT_COV"] == "5") {
            $query = knjd129qQuery::getAttendData($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model);
        } else {
            $query = knjd129qQuery::getAttendData2($row["SCHREGNO"], $schoolRow["ABSENT_COV"], $schoolRow["ABSENT_COV_LATE"], $schoolRow, $model);
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
    //講座リストを取得する
    knjCreateHidden($objForm, "chairList", (get_count($chairList) > 0) ? implode(",", $chairList) : "");

    //分布表で使用
    $levelCnt = array();

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
            //分布表で使用
            if ($model->Properties["showBunpuHyou"] == '1' && $testcd == "9990009") {
                foreach ($term_data[$col] as $key => $val) {
                    $levelCnt[$val][] = $val;
                }
            }
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
        //平均点欄を表示・・・入力不可（表示のみ）
        if ($model->Properties["showHeikinran"] == '1' && $testcd == "9990008") {
            $col = "SCHNO_AVG";
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
            $scoreSum .= "<th width=60 >".$foot["SUM"]."</th> ";
            $scoreAvg .= "<th width=60 >".$foot["AVG"]."</th> ";
            $scoreMax .= "<th width=60 >".$foot["MAX"]."</th> ";
            $scoreMin .= "<th width=60 >".$foot["MIN"]."</th> ";
        }
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
    $cur_date = $db->getRow(knjd129qQuery::getMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
    if (is_array($cur_date)) {
        $arg["CUR_DATE"] = $cur_date["YEAR"]."年度".$model->control["学期名"][$cur_date["SEMESTER"]]."<BR>".(int)$cur_date["MONTH"]."月".$cur_date["APPOINTED_DAY"]."日現在";
    }

    //分布表
    if ($model->Properties["showBunpuHyou"] == '1') {
        $arg["showBunpuHyou"] = 1;

        $bunpuArray = array();
        $result = $db->query(knjd129qQuery::getBunpuList());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $bunpuArray[] = array("ASSESSLEVEL" => $row["ASSESSLEVEL"]);
        }
        $result->free();

        $level_max = 0;
        foreach ($bunpuArray as $key => $bunpu) {
            $setData = array();
            //評定
            if ($level_max == 0) {
                $level_max = $bunpu["ASSESSLEVEL"];
            }
            $setData["LEVEL"]   = $bunpu["ASSESSLEVEL"];
            if (isset($levelCnt[$bunpu["ASSESSLEVEL"]])) {
                //人数
                $setData["CNT"]     = get_count($levelCnt[$bunpu["ASSESSLEVEL"]]);
                //割合
                $setData["PERCENT"] = round((get_count($levelCnt[$bunpu["ASSESSLEVEL"]])/$counter)*100*10)/10;
            }
            //ID
            $setData["LEVEL_ID"] = "LEVEL_ID"."-".$bunpu["ASSESSLEVEL"];
            $setData["CNT_ID"] = "CNT_ID"."-".$bunpu["ASSESSLEVEL"];
            $setData["PERCENT_ID"] = "PERCENT_ID"."-".$bunpu["ASSESSLEVEL"];
            $arg["dataBunpu"][] = $setData;
        }
        knjCreateHidden($objForm, "LEVEL_MAX", $level_max);
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
        $query = knjd129qQuery::getRecordChkfinDat($model, $testcd, $chairList);
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
        //平均点欄を表示・・・入力不可（表示のみ）
        if ($model->Properties["showHeikinran"] == '1' && $testcd == "9990008") {
            $chkComp .= "<th width=60 ></th> ";
        }
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
    //集団ラジオ
    $chaircd = $model->field["CHAIRCD"];
    if ($model->Properties["showBunpuHyou"] == '1') {
        //学年コンボ
        if ($model->field["GROUP_DIV"] == "2") {
            $chaircd = $model->field["GRADE"];
        }
        //コースコンボ
        if ($model->field["GROUP_DIV"] == "3") {
            $chaircd = $model->field["GRADE_COURSE"];
        }
        //講座グループコンボ
        if ($model->field["GROUP_DIV"] == "4") {
            $chaircd = $model->field["CHAIR_GROUP_CD"];
        }
    }
    $syukketuDisabled = $chaircd && $model->field["SUBCLASSCD"] ? "" : " disabled ";
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
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $syukketuDisabled.$extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $execute_date)
{
    knjCreateHidden($objForm, "cmd");
    //印刷パラメータ
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD129Q");
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
