<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd129cForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $objUp = new csvFile();

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd129cindex.php", "", "main");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $arg["useProvFlg"] = "1";
        } else {
            $arg["not_useProvFlg"] = "1";
        }

        //リンク先で学期末評価も一緒に表示する
        $gakkimatu = array("10201" => "19900"
                          ,"20201" => "29900"
                          ,"30201" => "39900");
        //リンク元のプログラムＩＤ
        $prgid = "KNJD129C";
        $auth = AUTHORITY;
        //リンク先のURL
        $ptn2 = (!$model->useSlumpHyouka) ? "" : "_2";
        $jump = REQUESTROOT."/D/KNJD128{$ptn2}/knjd128{$ptn2}index.php";
        //テスト名取得
        $testDate = array();
        $semename = $testname = array();
        $query = knjd129cQuery::getTestName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semename[$row["VALUE_SEME"]] = $row["SEMESTERNAME"];
            $testname[$row["VALUE_TEST"]] = $row["TESTITEMNAME"];
            $arg["SEMENAME".$row["VALUE_SEME"]] = $row["SEMESTERNAME"];
            $arg["TESTNAME".$row["VALUE_TEST"]] = $row["TESTITEMNAME"];
            $testDate[$row["VALUE_TEST"]] = array("TEST_START_DATE" => $row["TEST_START_DATE"], "TEST_END_DATE" => $row["TEST_END_DATE"]);
            /***************************************
                        //URLパラメータ
                        $testcd  = $row["VALUE_TEST"];
                        $param  = "?prgid={$prgid}";
                        $param .= "&auth={$auth}";
                        $param .= "&SUBCLASSCD={$model->field["SUBCLASSCD"]}";
                        $param .= "&CHAIRCD={$model->field["CHAIRCD"]}";
                        $param .= "&TESTCD={$testcd}";
                        if (!$model->useSlumpHyouka) {
                            $testcd2 = isset($gakkimatu[$testcd]) ? $gakkimatu[$testcd] : "";
                            $param .= "&TESTCD2={$testcd2}";
                        }
                        //考査名にリンクを作成
                        $extra = "onClick=\"openKogamen('{$jump}{$param}');\"";
                        $testName = (substr($testcd, 1, 2) == "99") ? "評価" : $row["TESTITEMNAME"];
                        if (substr($testcd, 1, 2) == "99" && !$model->useSlumpHyouka) {
                            $arg["TESTNAME".$testcd] = $testName;
                        } else {
                            $arg["TESTNAME".$testcd] = View::alink("#", htmlspecialchars($testName), $extra);
                        }
            ***************************************/
        }

        //CSVヘッダ名
        $header = array("0" => "科目コード",
                        "1" => "科目名",
                        "2" => "講座コード",
                        "3" => "講座名",
                        "4" => "学籍番号",
                        "5" => "クラス－出席番",
                        "6" => "氏名",
                        "1111" => $semename[1]."－".$testname[10101],
                        "1211" => $semename[1]."－".$testname[10201],
                        "1902" => $semename[1]."評価",
                        "2111" => $semename[2]."－".$testname[20101],
                        "2211" => $semename[2]."－".$testname[20201],
                        "2902" => $semename[2]."評価",
                        "3211" => $semename[3]."－".$testname[30201],
                        "3902" => $semename[3]."評価",
                        "9912" => "学年評価",
                        "9902" => "学年評定",
                        "9903" => "履修単位",
                        "9904" => "修得単位",
                        "19" => $model->lastColumn);
        $objUp->setHeader(array_values($header));

        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd129cQuery::selectSubclassQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $row["SUBCLASSCD"] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
            }
            //合併先科目
            $cntSAKI = $db->getOne(knjd129cQuery::getSubclassReplaceCombinedCnt($model, $row["SUBCLASSCD"], "SAKI"));
            $cntMOTO = $db->getOne(knjd129cQuery::getSubclassReplaceCombinedCnt($model, $row["SUBCLASSCD"], "MOTO"));
            $maru = "　";
            if (0 < $cntSAKI && 0 < $cntMOTO) {
                $maru = "○"; //子
            } elseif (0 < $cntSAKI) {
                $maru = "●"; //親
            }
            $opt_sbuclass[] = array("label" => $maru.$row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"], "value" => $row["SUBCLASSCD"]);
            if ($row["SUBCLASSCD"] == $model->field["SUBCLASSCD"]) {
                $subclassname = $row["SUBCLASSNAME"];
            }
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "SUBCLASSCD",
                            "size"        => "1",
                            "value"       => $model->field["SUBCLASSCD"],
                            "options"     => $opt_sbuclass,
                            "extrahtml"   => "onChange=\"btn_submit('subclasscd')\";"));
        $arg["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");
        //hidden
        knjCreateHidden($objForm, "H_SUBCLASSCD");

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
        $opt_chair = array();
        $opt_chair[] = array("label" => "", "value" => "");
        $result = $db->query(knjd129cQuery::selectChairQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chair[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],"value" => $row["CHAIRCD"]);
            if ($row["CHAIRCD"] == $model->field["CHAIRCD"]) {
                $chairname = $row["CHAIRNAME"];
            }
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "CHAIRCD",
                            "size"        => "1",
                            "value"       => $model->field["CHAIRCD"],
                            "options"     => $opt_chair,
                            "extrahtml"   => "onChange=\"btn_submit('chaircd')\";"
                           ));
        $arg["CHAIRCD"] = $objForm->ge("CHAIRCD");
        //hidden
        knjCreateHidden($objForm, "H_CHAIRCD");

        //CSV出力ファイル名
        $objUp->setFileName(CTRL_YEAR."年度_".$subclassname."_".$chairname."_"."成績入力(３学期制).csv");

        $backcolor = array( "1"  => "#3399ff",
                            "2"  => "#66cc33",
                            "3"  => "#66cc33",
                            "4"  => "#ff0099",
                            "5"  => "#ff0099",
                            "6"  => "#ff0099",
                            "8"  => "#3399ff",
                            "9"  => "#66cc33",
                            "10" => "#66cc33",
                            "11" => "#ff0099",
                            "12" => "#ff0099",
                            "13" => "#ff0099",
                            "14" => "#ff0099");

        //試験名称NAMECD2
        $ctrl_name = array("1111" => "SEM1_INTR_SCORE"
                          ,"1211" => "SEM1_TERM_SCORE"
                          ,"1902" => "SEM1_VALUE"
                          ,"2111" => "SEM2_INTR_SCORE"
                          ,"2211" => "SEM2_TERM_SCORE"
                          ,"2902" => "SEM2_VALUE"
                          ,"3211" => "SEM3_TERM_SCORE"
                          ,"3902" => "SEM3_VALUE"
                          ,"9912" => "GRAD_SCORE"
                          ,"9902" => "GRAD_VALUE"
                          ,"9903" => "COMP_CREDIT"
                          ,"9904" => "GET_CREDIT");

        //テキストの名前を取得する
        $textFieldName = "";
        $textSep = "";
        foreach ($ctrl_name as $code => $col) {
            $textFieldName .= $textSep.$col;
            $textSep = ",";
        }
        knjCreateHidden($objForm, "TEXT_FIELD_NAME", $textFieldName);

        //考査満点マスタのコード
        $perfectcd = array("1111" => "10101"
                          ,"1211" => "10201"
                          ,"1902" => "19900"
                          ,"2111" => "20101"
                          ,"2211" => "20201"
                          ,"2902" => "29900"
                          ,"3211" => "30201"
                          ,"3902" => "39900"
                          ,"9912" => "99901"
                          ,"9902" => "99900"
                          ,"9903" => "00000"
                          ,"9904" => "00000");

        $seme_test = "0";
        $kind_test = "0";
        $item_test = "0";

        //管理者コントロール
        $model->testcd = "";    //講座コードをどの考査にセットするかを判断するコードの値を保持用
        $seme_kind_item_s = ""; //最初の値を保持用
        $seme_kind_item_e = ""; //最後の値を保持用
        $soten_flg = false;     //素点の指定があるかどうかのフラグを保持用
        $admin_key = $admin_key2 = array();
        $result = $db->query(knjd129cQuery::selectContolCodeQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!isset($perfectcd[$row["CONTROL_CODE"]])) {
                continue;
            }

            $admin_key[] = $row["CONTROL_CODE"];
            $admin_key2[$row["CONTROL_CODE"]] = $row["CONTROL_CODE"];
            //単位入力欄の入力可／不可の区別は、学年評定欄の管理者コントロールの指定に依存する
            //よって、学年評定の場合、履修単位／修得単位の仮コードをセットする
            if ($row["CONTROL_CODE"] == "9902") {
                $admin_key[] = "9903";
                $admin_key[] = "9904";
            }
            //中間・期末素点以外の場合、時間割講座テストより試験日の抽出はせず、学籍処理日とする。
            if (substr($row["CONTROL_CODE"], 1, 1) == "9" || substr($row["CONTROL_CODE"], 3, 1) != "1") {
                continue;
            }
            $seme_test = substr($row["CONTROL_CODE"], 0, 1);
            $kind_test = "0" .substr($row["CONTROL_CODE"], 1, 1);
            $item_test = "0" .substr($row["CONTROL_CODE"], 2, 1);
            //素点の指定があるかどうかのフラグを保持
            $soten_flg = true;
            //最初の値を保持
            if ($seme_kind_item_s == "") {
                $seme_kind_item_s = $seme_test .$kind_test .$item_test;
            }
            //最後の値を保持
            $seme_kind_item_e = $seme_test .$kind_test .$item_test;
        }
        //素点指定があるかつ最初と最後の値が等しい場合のみ、$model->testcdに値をセットする。
        if ($soten_flg && $seme_kind_item_s == $seme_kind_item_e) {
            $model->testcd = $seme_kind_item_e;
        }

        //時間割講座テストより試験日を抽出
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;//初期値
        } else {
            $execute_date = $edate;//初期値
        }
        if ($seme_test != "0") {
            $result = $db->query(knjd129cQuery::selectExecuteDateQuery($model, $seme_test, $kind_test, $item_test));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $execute_date = $row["EXECUTEDATE"];
            }
        }

        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knjd129cQuery::getScAbsentCov($model), DB_FETCHMODE_ASSOC);

        //初期化
        $model->data=array();
        $model->attend_data = array(); //出欠情報
        $model->passdata=array(); //見込点情報
        $counter=0;

        //休学時の欠課をカウントするかどうかのフラグ(1 or NULL)を取得。1:欠課をカウントする
        $offdaysFlg = $db->getRow(knjd129cQuery::getOffdaysFlg(CTRL_YEAR), DB_FETCHMODE_ASSOC);
        //帳票パラメータ
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SUB_OFFDAYS",
                            "value"     => $offdaysFlg["SUB_OFFDAYS"] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SUB_ABSENT",
                            "value"     => $offdaysFlg["SUB_ABSENT"] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SUB_SUSPEND",
                            "value"     => $offdaysFlg["SUB_SUSPEND"] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SUB_MOURNING",
                            "value"     => $offdaysFlg["SUB_MOURNING"] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SUB_VIRUS",
                            "value"     => $offdaysFlg["SUB_VIRUS"] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SUB_KOUDOME",
                            "value"     => $offdaysFlg["SUB_KOUDOME"] ));

        //累積情報
        $attend = array();
        if ($absent["ABSENT_COV"] == "0" || $absent["ABSENT_COV"] == "2" || $absent["ABSENT_COV"] == "4" || $absent["ABSENT_COV"] == "5") {
            $result = $db->query(knjd129cQuery::getAttendData($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"], $offdaysFlg, $model));
        } else {
            $result = $db->query(knjd129cQuery::getAttendData2($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"], $offdaysFlg, $model));
        }
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $attend[$row["SCHREGNO"]]["NOTICE_LATE"] = $row["NOTICE_LATE"];
        }
        //各学期の欠課数　学期でペナルティ欠課を集計。通年はペナルティ欠課なし。
        $attend_s = array();
        $result = $db->query(knjd129cQuery::getAttendDataS($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"], $offdaysFlg, $model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sm = $row["SEMESTER"];
            if ($absent["ABSENT_COV"] == "0" || $absent["ABSENT_COV"] == "2" || $absent["ABSENT_COV"] == "4" || $absent["ABSENT_COV"] == "5") {
                $attend_s[$row["SCHREGNO"]]["NOTICE_LATE".$sm] = $row["T_NOTICE"];
            } else {
                $attend_s[$row["SCHREGNO"]]["NOTICE_LATE".$sm] = $row["NOTICE_LATE"];
            }
        }

        //合併先科目の単位固定／加算フラグ
        $model->creditflg = $db->getOne(knjd129cQuery::getCalculateCreditFlg($model->field["SUBCLASSCD"], $model));

        //一覧表示
        $controlFlgBtnCalc = '';
        $controlFlgBtnUpd = '';
        $colorFlg = false;
        $result = $db->query(knjd129cQuery::selectQuery($model, $execute_date));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号をHiddenで保持
            knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);
            //クラス-出席番(表示)
            if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
            }
            //名前
            $row["INOUTCD"]     = ($row["INOUTCD"] == '1')? "☆": "　";
            $row["NAME_SHOW"]   = $row["NAME_SHOW"];
            //累積データ
            //$row["LESSON"]      = strlen($attend[$row["SCHREGNO"]]["LESSON"])      ? $attend[$row["SCHREGNO"]]["LESSON"]      : "0";
            //$row["T_NOTICE"]    = strlen($attend[$row["SCHREGNO"]]["T_NOTICE"])    ? $attend[$row["SCHREGNO"]]["T_NOTICE"]    : "0";
            //$row["T_LATEEARLY"] = strlen($attend[$row["SCHREGNO"]]["T_LATEEARLY"]) ? $attend[$row["SCHREGNO"]]["T_LATEEARLY"] : "0";
            $row["NOTICE_LATE1"] = strlen($attend_s[$row["SCHREGNO"]]["NOTICE_LATE1"]) ? $attend_s[$row["SCHREGNO"]]["NOTICE_LATE1"] : "0";
            $row["NOTICE_LATE2"] = strlen($attend_s[$row["SCHREGNO"]]["NOTICE_LATE2"]) ? $attend_s[$row["SCHREGNO"]]["NOTICE_LATE2"] : "0";
            $row["NOTICE_LATE3"] = strlen($attend_s[$row["SCHREGNO"]]["NOTICE_LATE3"]) ? $attend_s[$row["SCHREGNO"]]["NOTICE_LATE3"] : "0";
            $row["NOTICE_LATE"]  = strlen($attend[$row["SCHREGNO"]]["NOTICE_LATE"]) ? $attend[$row["SCHREGNO"]]["NOTICE_LATE"] : "0";
            //単位情報を配列で取得
            knjCreateHidden($objForm, "NOTICE_LATE"."-".$counter, $row["NOTICE_LATE"]."-".$row["CREDITS"]."-".$row["ABSENCE_HIGH"]."-".$row["AUTHORIZE_FLG"]);
            //合併先科目の単位を抽出（2:加算タイプ）
            knjCreateHidden($objForm, "COMBINED_CREDIT_SUM"."-".$counter, $row["COMP_CREDIT_SUM"]."-".$row["GET_CREDIT_SUM"]."-".$row["GET_CREDIT_SUM_Y"]);
            //書き出し用CSVデータ
            $csv = array($model->field["SUBCLASSCD"],
                         $subclassname,
                         $model->field["CHAIRCD"],
                         $chairname,
                         $row["SCHREGNO"],
                         $row["ATTENDNO"],
                         $row["NAME_SHOW"]);
            //キー値をセット
            $key = array("科目コード" => $model->field["SUBCLASSCD"],
                         "講座コード" => $model->field["CHAIRCD"],
                         "学籍番号"   => $row["SCHREGNO"]);
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                //ゼロ埋めフラグ
                $flg = array("科目コード" => array(false,13),
                             "講座コード" => array(true,7),
                             "学籍番号"   => array(true,8));
            } else {
                //ゼロ埋めフラグ
                $flg = array("科目コード" => array(true,6),
                             "講座コード" => array(true,7),
                             "学籍番号"   => array(true,8));
            }
            $objUp->setEmbed_flg($flg);
            $objUp->setType(array(7=>'S',8=>'S',9=>'S',10=>'S',11=>'S',12=>'S',13=>'S',14=>'S',15=>'S',16=>'S',17=>'S',18=>'S',19=>'S'));
            $objUp->setSize(array(7=>3,8=>3,9=>3,10=>3,11=>3,12=>3,13=>3,14=>3,15=>3,16=>3,17=>2,18=>2,19=>5));

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //テキストボックス表示フラグ(転学・退学者の成績は入力不可)
            $edit_flg3 = true;

            //各項目を作成
            foreach ($ctrl_name as $code => $col) {
                //見込点情報
                if (strlen($row[$col."_PASS"])) {
                    $row[$col] = $row[$col."_PASS"];
                }
                //学期成績集計項目
                if (is_numeric($row[$col])) {
                    $term_data[$col][] = $row[$col];
                }
                //テキストボックス表示フラグ(休学・留学者の成績は入力不可)
                $edit_flg4 = true;
                $edit_flg = $edit_flg2 = true;  //テキストボックス表示フラグ
                $sem = substr($code, 0, 1);
                $sem_val = substr($code, 3, 1);
                $row[$col."_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
                //異動情報
                if (strlen($row["TRANSFER_SDATE"]) || strlen($row["TRANSFER_EDATE"])) {
                    if (strlen($testDate[$perfectcd[$code]]["TEST_START_DATE"]) &&
                        strlen($testDate[$perfectcd[$code]]["TEST_END_DATE"])) {
                        //テスト期間中すべて異動期間の場合
                        if (strtotime($row["TRANSFER_SDATE"]) <= strtotime($testDate[$perfectcd[$code]]["TEST_START_DATE"])
                               && strtotime($row["TRANSFER_EDATE"]) >= strtotime($testDate[$perfectcd[$code]]["TEST_END_DATE"])) {
                            $edit_flg4 = false;
                            $row[$col."_COLOR"]="#ffff00";
                        //一部
                        } elseif ((strtotime($row["TRANSFER_SDATE"]) >= strtotime($testDate[$perfectcd[$code]]["TEST_START_DATE"]))
                               && (strtotime($row["TRANSFER_SDATE"]) <= strtotime($testDate[$perfectcd[$code]]["TEST_END_DATE"]))) {
                            $row[$col."_COLOR"]="#ffff00";
                        } elseif ((strtotime($row["TRANSFER_EDATE"]) >= strtotime($testDate[$perfectcd[$code]]["TEST_START_DATE"]))
                               && (strtotime($row["TRANSFER_EDATE"]) <= strtotime($testDate[$perfectcd[$code]]["TEST_END_DATE"]))) {
                            $row[$col."_COLOR"]="#ffff00";
                        }
                    }
                }
                //卒業日付
                if ($sem != "9" && strlen($row["GRD_DATE"])) {
                    if (strlen($testDate[$perfectcd[$code]]["TEST_START_DATE"]) &&
                        strlen($testDate[$perfectcd[$code]]["TEST_END_DATE"])) {
                        //テスト期間中すべて卒業の場合(テスト開始日付以前に卒業している場合）
                        if (strtotime($row["GRD_DATE"]) <= strtotime($testDate[$perfectcd[$code]]["TEST_START_DATE"])) {
                            $edit_flg3 = false;
                            $row[$col."_COLOR"]="#ffff00";
                        //一部
                        } elseif (strtotime($row["GRD_DATE"]) > strtotime($testDate[$perfectcd[$code]]["TEST_START_DATE"])
                               && strtotime($row["GRD_DATE"]) <= strtotime($testDate[$perfectcd[$code]]["TEST_END_DATE"])) {
                            $row[$col."_COLOR"]="#ffff00";
                        }
                    }
                }
                //在籍情報がない場合
                if ($sem != "9" && !strlen($row["CHAIR_SEM".$sem])) {
                    $edit_flg = false;
                    if ($sem <= CTRL_SEMESTER) {
                        $row[$col."_COLOR"]="#ffff00";
                    }
                }
                //欠課時数情報('-','=')
                if (strlen($row[$col."_DI"])) {
                    $row[$col] = $row[$col."_DI"];
                    if ($row[$col."_DI"] == "*") {
                        $row[$col."_COLOR"] = "#ff0099";
                    }
                }
                //出欠情報
                if (strlen($row[$col."_ATTEND"])) {
                    $edit_flg2 = false;
                    $row[$col."_COLOR"] = $backcolor[$row[$col."_ATTEND"]];
                }
                //見込点情報
                if (strlen($row[$col."_PASS"])) {
                    $edit_flg2 = false;
                }
                /***/
                if ($code == "9912") {
                    //直接入力した学年評価は表示色を変える。
                    if ($row[$col] != $row[$col."_KEEP"]) {
                        $row[$col."_COLOR"] = "pink";
                    }
                    knjCreateHidden($objForm, $col."_KEEP"."-".$counter, $row[$col."_KEEP"]);
                }
                /***/
                //CSV書き出し
                $csv[] = $row[$col];
                $controlFlg = '';
                //ラベルのみ
                if ((!$edit_flg && AUTHORITY != DEF_UPDATABLE) || !$edit_flg2 || !$edit_flg3 || !$edit_flg4) {
                    knjCreateHidden($objForm, $col."-".$counter, $row[$col]);
                    $row[$col] = "<font color=\"#000000\">".$row[$col]."</font>";
                //管理者コントロール
                } elseif (in_array($code, $admin_key)) {
                    if ($code == "9912") {
                        $controlFlgBtnCalc = '1';
                    }
                    $controlFlgBtnUpd = '1';
                    $controlFlg = '1';
                    //入力エリアとキーをセットする
                    $objUp->setElementsValue($col."-".$counter, $header[$code], $key);
                    //出欠情報がある場合はそれを表示
                    $value = $row[$col];
                    //テキストボックスを作成
                    $idName = $col."_ID"."-".$counter;
                    $width = ($code == "9902" || $code == "9903" || $code == "9904") ? "30" : "35";
                    $objForm->ae(array("type"      => "text",
                                        "name"      => $col."-".$counter,
                                        "size"      => "3",
                                        "maxlength" => ($code == "9903" || $code == "9904") ? "2" : "3",
                                        "value"     => $value,
                                        "extrahtml" => "STYLE=\"text-align: right; width:{$width}\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab2(this)\" onPaste=\"return showPaste(this);\" id=\"{$idName}\""));
                    $row[$col] = $objForm->ge($col."-".$counter);

                    //考査満点マスタ
                    $query = knjd129cQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $perfectcd[$code], $row["GRADE"], $row["COURSE"], $model);
                    $perfect = ($model->usePerfect == 'true') ? $db->getOne($query) : 100;
                    if ($perfect == "") {
                        $perfect = 100;
                    }
                    //満点チェック用を作成
                    $objForm->ae(array("type"      => "hidden",
                                        "name"      => $col."_PERFECT"."-".$counter,
                                        "value"     => $perfect ));
                } else {
                    knjCreateHidden($objForm, $col."-".$counter, $row[$col]);
                }
                //仮評定フラグ対応
                if ($model->Properties["useProvFlg"] == '1') {
                    if ($col == "GRAD_VALUE") {
                        $chk = $row["PROV_FLG"] == '1' ? ' checked="checked" ' : '';
                        $dis = $controlFlg == '1' ? '' : ' disabled="disabled" ';
                        $row["PROV_FLG"] = knjCreateCheckBox($objForm, "PROV_FLG"."-".$counter, "1", $chk.$dis);
                    }
                }
            }//foreach
            //CSV書き出し
            $csv[] = $model->lastColumn;
            $objUp->addCsvValue($csv);
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            $counter++;
            $arg["data"][] = $row;
        }//while

        knjCreateHidden($objForm, "COUNT", $counter);

        //学期ごとの集計
        foreach ($ctrl_name as $code => $col) {
            if (isset($term_data[$col])) {
                //合計
                $arg[$col."_SUM"]=array_sum($term_data[$col]);
                //平均
                $arg[$col."_AVG"]=round((array_sum($term_data[$col])/get_count($term_data[$col]))*10)/10;
                //最高点と最低点を求める
                array_multisort($term_data[$col], SORT_NUMERIC);
                $max = get_count($term_data[$col])-1;
                //最高点
                $arg[$col."_MAX"]=$term_data[$col][$max];
                //最低点
                $arg[$col."_MIN"]=$term_data[$col][0];
            }
        }

        //累積現在日
        $cur_date = $db->getRow(knjd129cQuery::getMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
        if (is_array($cur_date)) {
            $arg["CUR_DATE"] = $cur_date["YEAR"]."年度".$model->control["学期名"][$cur_date["SEMESTER"]]."<BR>".(int)$cur_date["MONTH"]."月".$cur_date["APPOINTED_DAY"]."日現在";
        }
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "ATTENDSUBYEAR",
                            "value"     => $cur_date["YEAR"] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "ATTENDSUBMONTH",
                            "value"     => $cur_date["MONTH"] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "ATTENDSUBDAY",
                            "value"     => $cur_date["APPOINTED_DAY"] ));

        /**************************************/
        /* 素点の成績入力完了チェックボックス */
        /**************************************/
        $testcdArray = array("1111" => "10101"
                            ,"1211" => "10201"
                            ,"2111" => "20101"
                            ,"2211" => "20201"
                            ,"3211" => "30201");

        //初期化
        $j = 1;
        $recordDiv = "1"; // 1:素点
        foreach ($testcdArray as $testkey => $testval) {
            $chk = '';
            $dis = '';
            if ($model->useRecordChkfinDat == "1") {
                $query = knjd129cQuery::getRecordChkfinDat($model, $testval, $recordDiv);
            } else {
                $query = knjd129cQuery::getRecordSchChrTest($model, $testval);
            }
            $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($resultRow)) {
                if ($resultRow["EXECUTED"] == '1') {
                    $chk = ' checked="checked" ';
                } else {
                    $chk = '';
                }
            }
            $dis = isset($admin_key2[$testkey]) ? '' : ' disabled="disabled" ';
            $objForm->ae(
                array("type"      => "checkbox",
                               "name"      => "CHK_COMP".$j,
                               "value"     => "on",
                               "extrahtml" => $chk.$dis)
            );

            $arg["CHK_COMP".$j] = $objForm->ge("CHK_COMP".$j);
            $j++;
        }

        /********************************************/
        /* 評定・評価の成績入力完了チェックボックス */
        /********************************************/
        $testcdArray = array("1902" => "19900"
                            ,"2902" => "29900"
                            ,"3902" => "39900"
                            ,"9912" => "99901"
                            ,"9902" => "99900");

        //初期化
        $j = 1;
        $recordDiv = "2"; // 2:評定・評価
        foreach ($testcdArray as $testkey => $testval) {
            $chk = '';
            $dis = '';
            $query = knjd129cQuery::getRecordChkfinDat($model, $testval, $recordDiv);
            $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($resultRow)) {
                if ($resultRow["EXECUTED"] == '1') {
                    $chk = ' checked="checked" ';
                } else {
                    $chk = '';
                }
            }
            $dis = isset($admin_key2[$testkey]) ? '' : ' disabled="disabled" ';
            $objForm->ae(
                array("type"      => "checkbox",
                               "name"      => "CHK_COMP_VALUE".$j,
                               "value"     => "on",
                               "extrahtml" => $chk.$dis)
            );

            $arg["CHK_COMP_VALUE".$j] = $objForm->ge("CHK_COMP_VALUE".$j);
            $j++;
        }
        /*********************************************/

        //変更保存用hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "CHK_FLG",
                            "value"     => $model->field["CHK_FLG"]
                            ));
        // "成績入力完了"処理 おわり //

        Query::dbCheckIn($db);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        $disBtn = (0 < $counter) ? "" : " disabled ";
        $disBtnUpd = ($controlFlgBtnUpd == '1') ? "" : " disabled ";
        $disBtnCalc = ($controlFlgBtnCalc == '1') ? "" : " disabled ";

        //学年評価算出ボタン
        $extra = "onclick=\"btnCalc();\"";
        $arg["btn_calc"] = knjCreateBtn($objForm, "btn_calc", "学年評価算出", $disBtnCalc.$extra);

        //ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => $disBtnUpd."onclick=\"return btn_submit('update');\"" ));
        $arg["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ));
        $arg["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae(array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["btn_end"] = $objForm->ge("btn_end");

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "印 刷",
                            "extrahtml"   => $disBtn."onclick=\"return newwin('" . SERVLET_URL . "');\"" ));
        $arg["btn_print"] = $objForm->ge("btn_print");

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "gen_ed",
                            "value"     => (substr($model->field["SUBCLASSCD"], 0, 2) == $model->gen_ed ? $model->gen_ed : "") ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD129C" ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => CTRL_SEMESTER ));
        //ログインした職員コード
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "STAFF",
                            "value"     => STAFFCD ));
        //試験日
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "TEST_DATE",
                            "value"     => $execute_date ));

        //テスト項目マスタ
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "COUNTFLG",
                            "value"     => $model->testTable ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SEMENAME1",
                            "value"     => $semename[1] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SEMENAME2",
                            "value"     => $semename[2] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SEMENAME3",
                            "value"     => $semename[3] ));
        $testcdArray = array("1111" => "10101"
                            ,"1211" => "10201"
                            ,"2111" => "20101"
                            ,"2211" => "20201"
                            ,"3211" => "30201");
        foreach ($testcdArray as $testkey => $testval) {
            $objForm->ae(array("type"      => "hidden",
                                "name"      => "TESTNAME" .$testval,
                                "value"     => $testname[$testval] ));
        }
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "CHIKOKU_HYOUJI_FLG",
                            "value"     => $model->chikokuHyoujiFlg ));
        //更新権限チェック
        knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
        knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd129cForm1.html", $arg);
    }
}
