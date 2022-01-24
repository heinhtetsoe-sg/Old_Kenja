<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd128_2Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        $objUp = new csvFile();

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd128_2index.php", "", "main");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //テスト名取得
        $semename = $testname = "";
        $query = knjd128_2Query::getTestName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["VALUE_TEST"] != $model->field["TESTCD"]) {
                continue;
            }
            $semename = $row["SEMESTERNAME"];
            $testname = $row["TESTITEMNAME"];
            $arg["SEMENAME"] = $semename;
            $arg["TESTNAME"] = $testname;
            $objForm->ae(array("type"      => "hidden",
                                "name"      => "SEMENAME",
                                "value"     => $semename ));
            $objForm->ae(array("type"      => "hidden",
                                "name"      => "TESTNAME",
                                "value"     => $testname ));
        }

        //テストコード
        $testcdArray = array("1111" => "10101"
                            ,"1211" => "10201"
                            ,"1221" => "10202"
                            ,"1902" => "19900"
                            ,"1912" => "19901"
                            ,"2111" => "20101"
                            ,"2121" => "20102"
                            ,"2211" => "20201"
                            ,"2221" => "20202"
                            ,"2902" => "29900"
                            ,"2912" => "29901"
                            ,"3111" => "30101"
                            ,"3211" => "30201"
                            ,"3902" => "39900"
                            ,"9912" => "99901");
        //試験名称
        $ctrl_name = array();
        foreach ($testcdArray as $testkey => $testval) {
            if ($testval != $model->field["TESTCD"]) {
                continue;
            }
            $ctrl_name[$testkey] = "TST_SCORE";
        }

        //CSVヘッダ名
        $header = array();
        $header[] = "科目コード";
        $header[] = "科目名";
        $header[] = "講座コード";
        $header[] = "講座名";
        $header[] = "学籍番号";
        $header[] = "クラス－出席番号";
        $header[] = "氏名";
        foreach ($ctrl_name as $code => $col) {
            $header[$code] = ($model->field["RECORD_DIV"] == "1") ? $semename."－".$testname : $testname;
        }
        $header[get_count($header)] = "不振";
        $header[get_count($header)] = "備考(不振理由等)";
        $header[get_count($header)] = $model->lastColumn;
        $objUp->setHeader(array_values($header));

        //科目コンボ
        $opt_sbuclass = array();
        $result = $db->query(knjd128_2Query::selectSubclassQuery($model));
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],
                                        "value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]);
                if ($row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"] == $model->field["SUBCLASSCD"]) {
                    $subclassname = $row["SUBCLASSNAME"];
                }
            }
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],"value" => $row["SUBCLASSCD"]);
                if ($row["SUBCLASSCD"] == $model->field["SUBCLASSCD"]) {
                    $subclassname = $row["SUBCLASSNAME"];
                }
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

        //講座コンボ
        $opt_chair = array();
        $result = $db->query(knjd128_2Query::selectChairQuery($model));
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
        $title = ($model->field["RECORD_DIV"] == "1") ? $semename."_".$testname : $testname;
        $objUp->setFileName(CTRL_YEAR."年度_".$subclassname."_".$chairname."_"."成績入力(".$title.").csv");

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

        $seme_test = "0";
        $kind_test = "0";
        $item_test = "0";

        //管理者コントロール
        $model->testcd = "";    //講座コードをどの考査にセットするかを判断するコードの値を保持用
        $seme_kind_item_s = ""; //最初の値を保持用
        $seme_kind_item_e = ""; //最後の値を保持用
        $soten_flg = false;     //素点の指定があるかどうかのフラグを保持用
        $admin_key = $admin_key2 = array();
        $result = $db->query(knjd128_2Query::selectContolCodeQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!isset($testcdArray[$row["CONTROL_CODE"]])) {
                continue;
            }

            $admin_key[] = $row["CONTROL_CODE"];
            $admin_key2[$row["CONTROL_CODE"]]=$row["CONTROL_CODE"];
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
            $result = $db->query(knjd128_2Query::selectExecuteDateQuery($model, $seme_test, $kind_test, $item_test));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $execute_date = $row["EXECUTEDATE"];
            }
        }

        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knjd128_2Query::getScAbsentCov($model), DB_FETCHMODE_ASSOC);

        //文字評定
        if ($model->gen_ed == substr($model->field["SUBCLASSCD"], 0, 2)) {
            $assess = array();
            $result = $db->query(knjd128_2Query::getAssessMark());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $assess[$row["ASSESSLEVEL"]] = $row["ASSESSMARK"];
                $arg["data2"][] = array("ASSESSLEVEL" => $row["ASSESSLEVEL"], "ASSESSMARK" => $row["ASSESSMARK"]);
            }
        }

        //初期化
        $model->data=array();
        $model->attend_data = array(); //出欠情報
        $counter=0;

        //休学時の欠課をカウントするかどうかのフラグ(1 or NULL)を取得。1:欠課をカウントする
        $offdaysFlg = $db->getRow(knjd128_2Query::getOffdaysFlg(CTRL_YEAR), DB_FETCHMODE_ASSOC);
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
            $result = $db->query(knjd128_2Query::GetAttendData($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"], $offdaysFlg, $model));
        } else {
            $result = $db->query(knjd128_2Query::GetAttendData2($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"], $offdaysFlg, $model));
        }
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $attend[$row["SCHREGNO"]]["LESSON"]      = $row["LESSON"];
            $attend[$row["SCHREGNO"]]["MLESSON"]     = $row["MLESSON"];
            $attend[$row["SCHREGNO"]]["T_NOTICE"]    = $row["T_NOTICE"];
            $attend[$row["SCHREGNO"]]["T_LATEEARLY"] = ($model->chikokuHyoujiFlg == 1) ? $row["LATE_EARLY"] : $row["T_LATEEARLY"];
            $attend[$row["SCHREGNO"]]["NOTICE_LATE"] = $row["NOTICE_LATE"];
            $attend[$row["SCHREGNO"]]["ABSENT"]      = $row["ABSENT"];
            $attend[$row["SCHREGNO"]]["MOURNING"]    = $row["MOURNING"];
            $attend[$row["SCHREGNO"]]["SUSPEND"]     = $row["SUSPEND"];
        }

        //合併先科目の単位固定／加算フラグ
        $model->creditflg = $db->getOne(knjd128_2Query::getCalculateCreditFlg($model->field["SUBCLASSCD"], $model));

        //一覧表示
        if (strlen($execute_date)) {
            $colorFlg = false;
            $result = $db->query(knjd128_2Query::selectQuery($model, $execute_date));
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
                $row["LESSON"]      = strlen($attend[$row["SCHREGNO"]]["LESSON"])      ? $attend[$row["SCHREGNO"]]["LESSON"]      : "0";
                $row["MLESSON"]     = strlen($attend[$row["SCHREGNO"]]["MLESSON"])     ? $attend[$row["SCHREGNO"]]["MLESSON"]     : "0";
                $row["T_NOTICE"]    = strlen($attend[$row["SCHREGNO"]]["T_NOTICE"])    ? $attend[$row["SCHREGNO"]]["T_NOTICE"]    : "0";
                $row["T_LATEEARLY"] = strlen($attend[$row["SCHREGNO"]]["T_LATEEARLY"]) ? $attend[$row["SCHREGNO"]]["T_LATEEARLY"] : "0";
                $row["NOTICE_LATE"] = strlen($attend[$row["SCHREGNO"]]["NOTICE_LATE"]) ? $attend[$row["SCHREGNO"]]["NOTICE_LATE"] : "0";
                $row["ABSENT"]      = strlen($attend[$row["SCHREGNO"]]["ABSENT"])      ? $attend[$row["SCHREGNO"]]["ABSENT"]      : "0";
                $row["MOURNING"]    = strlen($attend[$row["SCHREGNO"]]["MOURNING"])    ? $attend[$row["SCHREGNO"]]["MOURNING"]    : "0";
                $row["SUSPEND"]     = strlen($attend[$row["SCHREGNO"]]["SUSPEND"])     ? $attend[$row["SCHREGNO"]]["SUSPEND"]     : "0";
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
                $objUp->setType(array(7=>'S',8=>'S',9=>'S',10=>'S'));
                $objUp->setSize(array(7=>3,8=>3,9=>60,10=>60));

                if ($counter % 5 == 0) {
                    $colorFlg = !$colorFlg;
                }
                //各項目を作成
                foreach ($ctrl_name as $code => $col) {
                    //学期成績集計項目
                    if (is_numeric($row[$col])) {
                        $term_data[$col][] = $row[$col];
                    }
                    $edit_flg = true;  //テキストボックス表示フラグ
                    $sem = substr($code, 0, 1);
                    $sem_val = substr($code, 3, 1);
                    $row[$col."_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
                    //異動情報
                    if ($sem != "9" && (strlen($row["TRANSFER_SDATE"]) || strlen($row["TRANSFER_EDATE"]))) {
                        //学期期間中すべて異動期間の場合
                        if (strtotime($row["TRANSFER_SDATE"]) <= strtotime($model->control["学期開始日付"][$sem])
                              && strtotime($row["TRANSFER_EDATE"]) >= strtotime($model->control["学期終了日付"][$sem])) {
                            $edit_flg = false;
                            $row[$col."_COLOR"]="#ffff00";
                        //一部
                        } elseif ((strtotime($row["TRANSFER_SDATE"]) >= strtotime($model->control["学期開始日付"][$sem]))
                              && (strtotime($row["TRANSFER_SDATE"]) <= strtotime($model->control["学期終了日付"][$sem]))) {
                            $row[$col."_COLOR"]="#ffff00";
                        } elseif ((strtotime($row["TRANSFER_EDATE"]) >= strtotime($model->control["学期開始日付"][$sem]))
                              && (strtotime($row["TRANSFER_EDATE"]) <= strtotime($model->control["学期終了日付"][$sem]))) {
                            $row[$col."_COLOR"]="#ffff00";
                        }
                        //卒業日付
                    } elseif ($sem != "9" && strlen($row["GRD_DATE"])) {
                        //学期期間中すべて卒業の場合(学期開始日付以前に卒業している場合）
                        if (strtotime($row["GRD_DATE"]) <= strtotime($model->control["学期開始日付"][$sem])) {
                            $edit_flg = false;
                            $row[$col."_COLOR"]="#ffff00";
                        //一部
                        } elseif (strtotime($row["GRD_DATE"]) > strtotime($model->control["学期開始日付"][$sem])
                             && strtotime($row["GRD_DATE"]) <= strtotime($model->control["学期終了日付"][$sem])) {
                            $row[$col."_COLOR"]="#ffff00";
                        }
                    }
                    //在籍情報がない場合
                    if ($sem != "9" && !strlen($row["CHAIR_SEM"])) {
                        $edit_flg = false;
                        if ($sem <= CTRL_SEMESTER) {
                            $row[$col."_COLOR"]="#ffff00";
                        }
                    }
                    //欠課時数情報('-','=')
                    if (strlen($row[$col."_DI"]) && $sem_val == "2") {
                        $row[$col] = $row[$col."_DI"];
                    }
                    //出欠情報
                    if (strlen($row[$col."_ATTEND"])) {
                        $row[$col."_COLOR"] = $backcolor[$row[$col."_ATTEND"]];
                    }
                    //CSV書き出し
                    $csv[] = $row[$col];
                    //ラベルのみ
                    if (!$edit_flg && $model->auth != DEF_UPDATABLE) {
                        //hidden
                        knjCreateHidden($objForm, $col."-".$counter, $row[$col]);
                        $row[$col] = "<font color=\"#000000\">".$row[$col]."</font>";
                    //管理者コントロール
                    } elseif (in_array($code, $admin_key)) {
                        //入力エリアとキーをセットする
                        $objUp->setElementsValue($col."-".$counter, $header[$code], $key);
                        //出欠情報がある場合はそれを表示
                        $value = $row[$col];
                        //テキストボックスを作成
                        $objForm->ae(array("type"      => "text",
                                        "name"      => $col."-".$counter,
                                        "size"      => "3",
                                        "maxlength" => "3",
                                        "value"     => $value,
                                        "extrahtml" => "STYLE=\"text-align: right; width:30\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" "));
                        $row[$col] = $objForm->ge($col."-".$counter);

                        //考査満点マスタ
                        $query = knjd128_2Query::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $testcdArray[$code], $row["GRADE"], $row["COURSE"], $model);
                        $perfect = ($model->usePerfect == 'true') ? $db->getOne($query) : 100;
                        if ($perfect == "") {
                            $perfect = 100;
                        }
                        //テキストボックスを作成
                        $objForm->ae(array("type"      => "hidden",
                                        "name"      => $col."_PERFECT"."-".$counter,
                                        "value"     => $perfect ));
                    } else {
                        //hidden
                        knjCreateHidden($objForm, $col."-".$counter, $row[$col]);
                    }
                }//foreach
                //不振チェックボックス
                $name = "SLUMP";
                $csv[] = $row[$name];
                $extra = (strlen($row[$name])) ? "checked='checked'" : "";
                $row[$name] = knjCreateCheckBox($objForm, $name."-".$counter, "1", $extra);
                $objUp->setElementsValue($name."-".$counter, $header[get_count($csv)-1], $key);
                //不振理由テキストボックス
                $name = "REMARK";
                $csv[] = $row[$name];
                $extra = "STYLE=\"WIDTH:95%\" WIDTH=\"95%\" onChange=\"this.style.background='#ccffcc'\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" ";
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 40, 20, $extra);
                $objUp->setElementsValue($name."-".$counter, $header[get_count($csv)-1], $key);
                //CSV書き出し
                $csv[] = $model->lastColumn;
                $objUp->addCsvValue($csv);
                $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
                $counter++;
                $arg["data"][] = $row;
            }//while
        }
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
        $cur_date = $db->getRow(knjd128_2Query::getMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
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

        /********************************************/
        /* 素点・評価の成績入力完了チェックボックス */
        /********************************************/

        //初期化
        $j = 1;
        $recordDiv = $model->field["RECORD_DIV"]; // 1:素点 2:評価
        foreach ($testcdArray as $testkey => $testval) {
            if ($model->field["TESTCD"] != $testval) {
                continue;
            }

            $chk = '';
            $dis = '';
            if ($model->useRecordChkfinDat == "1" || $recordDiv == "2") {
                $query = knjd128_2Query::getRecordChkfinDat($model, $testval, $recordDiv);
            } else {
                $query = knjd128_2Query::getRecordSchChrTest($model, $testval);
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

        //変更保存用hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "CHK_FLG",
                            "value"     => $model->field["CHK_FLG"]
                            ));
        // "成績入力完了"処理 おわり //

        Query::dbCheckIn($db);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));
        $arg["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ));
        $arg["btn_reset"] = $objForm->ge("btn_reset");

        //リンク先のURL
        $jump  = "";
        if ($model->prgid == "KNJD128A") {
            $jump  = REQUESTROOT."/D/KNJD128A/knjd128aindex.php";
        } elseif ($model->prgid == "KNJD128B") {
            $jump  = REQUESTROOT."/D/KNJD128B/knjd128bindex.php";
        } elseif ($model->prgid == "KNJD128C") {
            $jump  = REQUESTROOT."/D/KNJD128C/knjd128cindex.php";
        } elseif ($model->prgid == "KNJD128D") {
            $jump  = REQUESTROOT."/D/KNJD128D/knjd128dindex.php";
        } elseif ($model->prgid == "KNJD128E") {
            $jump  = REQUESTROOT."/D/KNJD128E/knjd128eindex.php";
        } elseif ($model->prgid == "KNJD128G") {
            $jump  = REQUESTROOT."/D/KNJD128G/knjd128gindex.php";
        }
        $param = "?cmd=main&SUBCLASSCD={$model->field["SUBCLASSCD"]}&CHAIRCD={$model->field["CHAIRCD"]}";
        $extra = "onClick=\"openOyagamen('{$jump}{$param}');\"";
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "戻 る",
                            "extrahtml"   => $extra ));
        $arg["btn_end"] = $objForm->ge("btn_end");

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "印 刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ));
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
                            "value"     => "KNJD128_2" ));
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
                            "name"      => "TESTCD",
                            "value"     => $model->field["TESTCD"] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "RECORD_DIV",
                            "value"     => $model->field["RECORD_DIV"] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "CHIKOKU_HYOUJI_FLG",
                            "value"     => $model->chikokuHyoujiFlg ));
        //権限チェック
        knjCreateHidden($objForm, "USER_AUTH", $model->auth);
        knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        
        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd128_2Form1.html", $arg);
    }
}
