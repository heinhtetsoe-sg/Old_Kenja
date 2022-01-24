<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd124rForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $objUp = new csvFile();

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd124rindex.php", "", "main");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //テスト名取得
        $semename = $testname = array();
        $query = knjd124rQuery::getTestName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semename[$row["VALUE_SEME"]] = $row["SEMESTERNAME"];
            $testname[$row["VALUE_TEST"]] = $row["TESTITEMNAME"];
            $arg["SEMENAME".$row["VALUE_SEME"]] = $row["SEMESTERNAME"];
            $arg["TESTNAME".$row["VALUE_TEST"]] = $row["TESTITEMNAME"];
        }

        //CSVヘッダ名
        $header = array("0" => "科目コード",
                        "1" => "科目名",
                        "2" => "講座コード",
                        "3" => "講座名",
                        "4" => "学籍番号",
                        "5" => "クラス－出席番",
                        "6" => "氏名",
                        "0111" => $semename[1]."－".$testname[10101]."－素点",
                        "0112" => $semename[1]."－".$testname[10101]."－評価",
                        "0121" => $semename[1]."－".$testname[10201]."－素点",
                        "0122" => $semename[1]."－".$testname[10201]."－評価",
                        "0182" => $semename[1]."評価",
                        "0211" => $semename[2]."－".$testname[20101]."－素点",
                        "0212" => $semename[2]."－".$testname[20101]."－評価",
                        "0221" => $semename[2]."－".$testname[20201]."－素点",
                        "0222" => $semename[2]."－".$testname[20201]."－評価",
                        "0282" => $semename[2]."評価",
                        "0321" => $semename[3]."－".$testname[30201]."－素点",
                        "0382" => $semename[3]."評価",
                        "0883" => "学年成績",
                        "0882" => "学年評定",
                        "0884" => "履修単位",
                        "0885" => "修得単位",
                        "23" => $model->lastColumn);
        $objUp->setHeader(array_values($header));

        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd124rQuery::selectSubclassQuery($model->gen_ed, $model));
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
        $opt_chair[] = array("label" => "", "value" => "");
        $result = $db->query(knjd124rQuery::selectChairQuery($model));
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
        $ctrl_name = array("0111" => "SEM1_INTR_SCORE"
                          ,"0112" => "SEM1_INTR_VALUE"
                          ,"0121" => "SEM1_TERM_SCORE"
                          ,"0122" => "SEM1_TERM_VALUE"
                          ,"0182" => "SEM1_VALUE"
                          ,"0211" => "SEM2_INTR_SCORE"
                          ,"0212" => "SEM2_INTR_VALUE"
                          ,"0221" => "SEM2_TERM_SCORE"
                          ,"0222" => "SEM2_TERM_VALUE"
                          ,"0282" => "SEM2_VALUE"
                          ,"0321" => "SEM3_TERM_SCORE"
                          ,"0382" => "SEM3_VALUE"
                          ,"0883" => "GRAD_SCORE"
                          ,"0882" => "GRAD_VALUE"
                          ,"0884" => "COMP_CREDIT"
                          ,"0885" => "GET_CREDIT");

        //考査満点マスタのコード
        $perfectcd = array("0111" => "10101"
                          ,"0112" => "10101"
                          ,"0121" => "10201"
                          ,"0122" => "10201"
                          ,"0182" => "19900"
                          ,"0211" => "20101"
                          ,"0212" => "20101"
                          ,"0221" => "20201"
                          ,"0222" => "20201"
                          ,"0282" => "29900"
                          ,"0321" => "30201"
                          ,"0382" => "39900"
                          ,"0883" => "99900"
                          ,"0882" => "99900"
                          ,"0884" => "00000"
                          ,"0885" => "00000");

        $seme_test = "0";
        $kind_test = "0";
        $item_test = "0";

        //管理者コントロール
        $model->testcd = "";    //講座コードをどの考査にセットするかを判断するコードの値を保持用
        $seme_kind_item_s = ""; //最初の値を保持用
        $seme_kind_item_e = ""; //最後の値を保持用
        $soten_flg = false;     //素点の指定があるかどうかのフラグを保持用
        $admin_key = array();
        $result = $db->query(knjd124rQuery::selectContolCodeQuery());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[] = $row["CONTROL_CODE"];
            $admin_key2[$row["CONTROL_CODE"]]=$row["CONTROL_CODE"];
            //単位入力欄の入力可／不可の区別は、学年評定欄の管理者コントロールの指定に依存する
            //よって、学年評定の場合、履修単位／修得単位の仮コードをセットする
            if ($row["CONTROL_CODE"] == "0882") {
                $admin_key[] = "0883";
                $admin_key[] = "0884";
                $admin_key[] = "0885";
            }
            //中間・期末素点以外の場合、時間割講座テストより試験日の抽出はせず、学籍処理日とする。
            if (substr($row["CONTROL_CODE"], 3, 1) != "1") {
                continue;
            }
//            $seme_test = (substr($row["CONTROL_CODE"], 1, 1) == "1") ? "1" : (substr($row["CONTROL_CODE"], 1, 1) == "2") ? "2" : "3" ;
            $seme_test = substr($row["CONTROL_CODE"], 1, 1);
            $kind_test = (substr($row["CONTROL_CODE"], 2, 1) == "1") ? "01" : "02" ;
            $item_test = "01";
            //素点の指定があるかどうかのフラグを保持
            if (substr($row["CONTROL_CODE"], 3, 1) == "1") {
                $soten_flg = true;
            }
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
            $result = $db->query(knjd124rQuery::selectExecuteDateQuery($model, $seme_test, $kind_test, $item_test));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $execute_date = $row["EXECUTEDATE"];
            }
        }

        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knjd124rQuery::getScAbsentCov($model), DB_FETCHMODE_ASSOC);

        //文字評定
        if ($model->gen_ed == substr($model->field["SUBCLASSCD"], 0, 2)) {
            $assess = array();
            $result = $db->query(knjd124rQuery::GetAssessMark());
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
        $offdaysFlg = $db->getRow(knjd124rQuery::getOffdaysFlg(CTRL_YEAR), DB_FETCHMODE_ASSOC);
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

        //累積情報
        $attend = array();
        if ($absent["ABSENT_COV"] == "0" || $absent["ABSENT_COV"] == "2" || $absent["ABSENT_COV"] == "4") {
            $result = $db->query(knjd124rQuery::getAttendData($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"], $offdaysFlg, $model));
        } else {
            $result = $db->query(knjd124rQuery::getAttendData2($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"], $offdaysFlg, $model));
        }
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $attend[$row["SCHREGNO"]]["T_NOTICE"]    = $row["T_NOTICE"];
            $attend[$row["SCHREGNO"]]["T_LATEEARLY"] = $row["T_LATEEARLY"];
            $attend[$row["SCHREGNO"]]["NOTICE_LATE"] = $row["NOTICE_LATE"];
        }

        //合併先科目の単位固定／加算フラグ
        $model->creditflg = $db->getOne(knjd124rQuery::getCalculateCreditFlg($model->field["SUBCLASSCD"], $model));

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd124rQuery::selectQuery($model, $execute_date));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"]."-".$row["TAKESEMES"];
            //クラス-出席番(表示)
            if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
            }
            //名前
            $row["INOUTCD"]     = ($row["INOUTCD"] == '1')? "☆": "　";
            $row["NAME_SHOW"]   = $row["NAME_SHOW"];
            //累積データ
            $row["T_NOTICE"]    = strlen($attend[$row["SCHREGNO"]]["T_NOTICE"]) ? $attend[$row["SCHREGNO"]]["T_NOTICE"] : "0";
            $row["T_LATEEARLY"] = strlen($attend[$row["SCHREGNO"]]["T_LATEEARLY"]) ? $attend[$row["SCHREGNO"]]["T_LATEEARLY"] : "0";
            $row["NOTICE_LATE"] = strlen($attend[$row["SCHREGNO"]]["NOTICE_LATE"]) ? $attend[$row["SCHREGNO"]]["NOTICE_LATE"] : "0";
            //単位情報を配列で取得
            $model->data["NOTICE_LATE"][] = $row["NOTICE_LATE"]."-".$row["CREDITS"]."-".$row["ABSENCE_HIGH"]."-".$row["AUTHORIZE_FLG"];
            //合併先科目の単位を抽出（2:加算タイプ）
            $model->data["COMBINED_CREDIT_SUM"][] = $row["COMP_CREDIT_SUM"]."-".$row["GET_CREDIT_SUM"]."-".$row["GET_CREDIT_SUM_Y"];
            //文字評定
            if (is_array($assess)) {
                if ($model->Properties["useAssessSubclassMst"] == '1') {
                    //科目別評定マスタの設定があるか
                    $query = knjd124rQuery::getAssessSubclassMstMark($model, CTRL_YEAR, $row["GRADE"], $row["COURSE"], $model->field["SUBCLASSCD"], $row["GRAD_VALUE"], "cnt");
                    $cnt = $db->getOne($query);
                    if (0 < $cnt) {
                        //科目別評定マスタ
                        $query = knjd124rQuery::getAssessSubclassMstMark($model, CTRL_YEAR, $row["GRADE"], $row["COURSE"], $model->field["SUBCLASSCD"], $row["GRAD_VALUE"], "mark");
                        $row["MARK_VALUE"] = $db->getOne($query);
                    } else {
                        //評定マスタ
                        $row["MARK_VALUE"] = $assess[$row["GRAD_VALUE"]];
                    }
                } else {
                    //評定マスタ
                    $row["MARK_VALUE"] = $assess[$row["GRAD_VALUE"]];
                }
            }
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
            $objUp->setType(array(7=>'S',8=>'S',9=>'S',10=>'S',11=>'S',12=>'S',13=>'S',14=>'S',15=>'S',16=>'S',17=>'S',18=>'S',19=>'S',20=>'S',21=>'S',22=>'S',23=>'S'));
            $objUp->setSize(array(7=>3,8=>3,9=>3,10=>3,11=>3,12=>3,13=>3,14=>3,15=>3,16=>3,17=>3,18=>3,19=>3,20=>3,21=>2,22=>2,23=>5));

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            //各項目を作成
            foreach ($ctrl_name as $code => $col) {
                //各データを取得
                $model->data[$col."-".$counter]        = $row[$col];
                $model->attend_data[$col."-".$counter] = $row[$col."_DI"];
                //学期成績集計項目
                if (is_numeric($row[$col])) {
                    $term_data[$col][] = $row[$col];
                }
                $edit_flg = true;  //テキストボックス表示フラグ
                $sem = substr($code, 1, 1);
                $sem_val = substr($code, 3, 1);
                $row[$col."_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
                //異動情報
                if ($sem != "8" && (strlen($row["TRANSFER_SDATE"]) || strlen($row["TRANSFER_EDATE"]))) {
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
                } elseif ($sem != "8" && strlen($row["GRD_DATE"])) {
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
                if ($sem != "8" && !strlen($row["CHAIR_SEM".$sem])) {
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
                if (!$edit_flg && AUTHORITY != DEF_UPDATABLE) {
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
                                        "maxlength" => ($code == "0884" || $code == "0885") ? "2" : "3",
                                        "value"     => $value,
                                        "extrahtml" => "STYLE=\"text-align: right; width:31\" onChange=\"this.style.background='#ccffcc'; document.forms[0].changeVal.value = '1';\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return show(this);\" "));
                    $row[$col] = $objForm->ge($col."-".$counter);

                    //考査満点マスタ
                    $query = knjd124rQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $perfectcd[$code], $row["GRADE"], $row["COURSE"], $model);
                    $perfect = ($model->usePerfect == 'true') ? $db->getOne($query) : 100;
                    if ($perfect == "") {
                        $perfect = 100;
                    }
                    //テキストボックスを作成
                    $objForm->ae(array("type"      => "hidden",
                                        "name"      => $col."_PERFECT"."-".$counter,
                                        "value"     => $perfect ));
                }
            }//foreach
            //CSV書き出し
            $csv[] = $model->lastColumn;
            $objUp->addCsvValue($csv);
            $row["MARK_VALUE_ID"] = "mark".$counter;
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            $counter++;
            $arg["data"][] = $row;
        }//while

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
        $cur_date = $db->getRow(knjd124rQuery::GetMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
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

        // "成績入力完了"処理 //

        /**************************************/
        /* 素点の成績入力完了チェックボックス */
        /**************************************/
        $testcdArray = array("0111" => "10101"
                            ,"0121" => "10201"
                            ,"0211" => "20101"
                            ,"0221" => "20201"
                            ,"0321" => "30201");

        //初期化
        $j = 1;
        $recordDiv = "1"; // 1:素点
        foreach ($testcdArray as $testkey => $testval) {
            $chk = '';
            $dis = '';
            if ($model->useRecordChkfinDat == "1") {
                $query = knjd124rQuery::getRecordChkfinDat($model, $testval, $recordDiv);
            } else {
                $query = knjd124rQuery::getRecordSchChairTestDat($model, $testval, $recordDiv);
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
        $testcdArray = array("0112" => "10101"
                            ,"0122" => "10201"
                            ,"0182" => "19900"
                            ,"0212" => "20101"
                            ,"0222" => "20201"
                            ,"0282" => "29900"
                            ,"0382" => "39900"
                            ,"0882" => "99900");

        //初期化
        $j = 1;
        $recordDiv = "2"; // 2:評定・評価
        foreach ($testcdArray as $testkey => $testval) {
            $chk = '';
            $dis = '';
            $query = knjd124rQuery::getRecordChkfinDat($model, $testval, $recordDiv);
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

        $objForm->ae(array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeMethod();\"" ));
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
                            "value"     => "KNJD124R" ));
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
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "TESTNAME10101",
                            "value"     => $testname[10101] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "TESTNAME10201",
                            "value"     => $testname[10201] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "TESTNAME20101",
                            "value"     => $testname[20101] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "TESTNAME20201",
                            "value"     => $testname[20201] ));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "TESTNAME30201",
                            "value"     => $testname[30201] ));

        //値の変更
        knjCreateHidden($objForm, "changeVal", 0);

        //更新権限チェック
        knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
        knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        //科目別評定マスタ
        knjCreateHidden($objForm, "useAssessSubclassMst", $model->Properties["useAssessSubclassMst"]);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd124rForm1.html", $arg);
    }
}
