<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd123hForm1.php 76250 2020-08-27 08:44:05Z arakaki $

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd123hForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $objUp = new csvFile();

        //CSVヘッダ名
        $header = array("0" => "科目コード",
                        "1" => "科目名",
                        "2" => "講座コード",
                        "3" => "講座名",
                        "4" => "学籍番号",
                        "5" => "クラス－出席番号",
                        "6" => "氏名",
                        "0111" => "１学期－中間",
                        "0121" => "１学期－期末",
                        "0182" => "１学期－評定",
                        "0211" => "２学期－中間",
                        "0221" => "２学期－期末",
                        "0282" => "２学期－評定",
                        "0321" => "３学期－学年末",
                        "0882" => "学年評定－１０段階",
                        "0883" => "学年評定－５段階",
                        "0884" => "履修単位",
                        "0885" => "修得単位",
                        "18" => $model->lastColumn);
        $objUp->setHeader(array_values($header));
        
        $arg["start"]    = $objForm->get_start("main", "POST", "knjd123hindex.php", "", "main");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $arg["useProvFlg"] = "1";
        } else {
            $arg["not_useProvFlg"] = "1";
        }

        //新規作成
        if ($model->cmd != "subclasscd") {
            $db->query(knjd123hQuery::insertExStdRecQuery($model));
        }
        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd123hQuery::selectSubclassQuery($model->gen_ed, $model));
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
        $result = $db->query(knjd123hQuery::selectChairQuery($model));
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
        knjCreateHidden($objForm, "H_CHAIRCD");

        //CSV出力ファイル名
        $objUp->setFileName(CTRL_YEAR."年度_".$subclassname."_".$chairname."_"."成績入力.csv");

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
                          ,"0121" => "SEM1_TERM_SCORE"
                          ,"0182" => "SEM1_VALUE"
                          ,"0211" => "SEM2_INTR_SCORE"
                          ,"0221" => "SEM2_TERM_SCORE"
                          ,"0282" => "SEM2_VALUE"
                          ,"0321" => "SEM3_TERM_SCORE"
                          ,"0882" => "GRAD_VALUE2"
                          ,"0883" => "GRAD_VALUE"
                          ,"0884" => "COMP_CREDIT"
                          ,"0885" => "GET_CREDIT");

        //考査満点マスタのコード
        $perfectcd = array("0111" => "10101"
                          ,"0121" => "10201"
                          ,"0182" => "19900"
                          ,"0211" => "20101"
                          ,"0221" => "20201"
                          ,"0282" => "29900"
                          ,"0321" => "30201"
                          ,"0882" => "99900"
                          ,"0883" => "99900"
                          ,"0884" => "00000"
                          ,"0885" => "00000");

        $seme_test = "0";
        $kind_test = "0";

        //管理者コントロール
        $model->testcd = "";    //講座コードをどの考査にセットするかを判断するコードの値を保持用 NO004Add
        $seme_kind_item_s = ""; //最初の値を保持用 NO004Add
        $seme_kind_item_e = ""; //最後の値を保持用 NO004Add
        $soten_flg = false;     //素点の指定があるかどうかのフラグを保持用 NO004Add
        $admin_key = array();
        $result = $db->query(knjd123hQuery::selectContolCodeQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[] = $row["CONTROL_CODE"];
            $admin_key2[$row["CONTROL_CODE"]]=$row["CONTROL_CODE"];

            //単位入力欄の入力可／不可の区別は、学年評定欄の管理者コントロールの指定に依存する
            //よって、学年評定の場合、履修単位／修得単位の仮コードをセットする
            if ($row["CONTROL_CODE"] == "0883") {
                $admin_key[] = "0884";
                $admin_key[] = "0885";
            }

            //中間・期末素点以外の場合、時間割講座テストより試験日の抽出はせず、学籍処理日とする。
            if (substr($row["CONTROL_CODE"], 3, 1) != "1") {
                continue;
            }

            if (substr($row["CONTROL_CODE"], 1, 1) == "1") {
                $seme_test = "1";
            } elseif (substr($row["CONTROL_CODE"], 1, 1) == "2") {
                $seme_test = "2";
            } else {
                $seme_test = "3";
            }
            $kind_test = (substr($row["CONTROL_CODE"], 2, 1) == "1") ? "01" : "02" ;

            //素点の指定があるかどうかのフラグを保持 NO004Add
            if (substr($row["CONTROL_CODE"], 3, 1) == "1") {
                $soten_flg = true;
            }
            //最初の値を保持 NO004Add*
            if ($seme_kind_item_s == "") {
                $seme_kind_item_s = $seme_test .$kind_test ."01";
            }
            //最後の値を保持 NO004Add*
            $seme_kind_item_e = $seme_test .$kind_test ."01";
        }
        //素点指定があるかつ最初と最後の値が等しい場合のみ、$model->testcdに値をセットする。 NO004Add
        if ($soten_flg && $seme_kind_item_s == $seme_kind_item_e) {
            $model->testcd = $seme_kind_item_e;
        }
        //echo "最初=" .$seme_kind_item_s ." " ."最後=" .$seme_kind_item_e ." " ."素点フラグ=" .$soten_flg ." " ."testcd=" .$model->testcd;

        //時間割講座テストより試験日を抽出
        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;//初期値
        } else {
            $execute_date = $edate;//初期値
        }
        if ($seme_test != "0") {
            $result = $db->query(knjd123hQuery::selectExecuteDateQuery($model, $seme_test, $kind_test));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $execute_date = $row["EXECUTEDATE"];
            }
        }
        //echo "seme_test = " .$seme_test ." kind_test = " .$kind_test ." execute_date = " .$execute_date;

        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knjd123hQuery::getScAbsentCov($model), DB_FETCHMODE_ASSOC);

        /***
                //文字評定
                if ($model->gen_ed == $model->field["SUBCLASSCD"]) {
                    $assess = array();
                    $result = $db->query(knjd123hQuery::GetAssessMark());
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                    {
                        $assess[$row["ASSESSLEVEL"]] = $row["ASSESSMARK"];
                        $arg["data2"][] = array("ASSESSLEVEL" => $row["ASSESSLEVEL"], "ASSESSMARK" => $row["ASSESSMARK"]);
                    }
                }
        ***/
        
        //初期化
        $model->data=array();
        $model->attend_data = array(); //出欠情報
        $counter=0;

        //休学時の欠課をカウントするかどうかのフラグ(1 or NULL)を取得。1:欠課をカウントする
        $offdaysFlg = $db->getRow(knjd123hQuery::getOffdaysFlg(CTRL_YEAR), DB_FETCHMODE_ASSOC);
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
        if ($absent["ABSENT_COV"] == "0" || $absent["ABSENT_COV"] == "2") {
            $result = $db->query(knjd123hQuery::getAttendData($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"], $offdaysFlg, $model));
        } else {
            $result = $db->query(knjd123hQuery::getAttendData2($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $absent["ABSENT_COV_LATE"], $offdaysFlg, $model));
        }
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $attend[$row["SCHREGNO"]]["T_NOTICE"]    = $row["T_NOTICE"];
            $attend[$row["SCHREGNO"]]["T_LATEEARLY"] = $row["T_LATEEARLY"];
            $attend[$row["SCHREGNO"]]["NOTICE_LATE"] = $row["NOTICE_LATE"];
        }

        //授業時間数
        $lesson = array();
        $lesson_sum = 0;
        $result = $db->query(knjd123hQuery::getAttendSubclass($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $lesson[$row["SEMESTER"]] = $row["LESSON"];
            $lesson_sum = $lesson_sum + $row["LESSON"];
        }
        if ($lesson_sum > 0) {
            $lesson[4] = $lesson_sum;
        }

        foreach ($lesson as $code => $col) {
            $arg["LESSON".$code] = $col;
        }
        //var_dump($lesson);

        //合併先科目の単位固定／加算フラグ
        $model->creditflg = "";
        $motoCnt = 0;
        $result = $db->query(knjd123hQuery::getCalculateCreditFlg($model->field["SUBCLASSCD"], $model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->creditflg = $row["CALCULATE_CREDIT_FLG"];
            $motoCnt++;
        }
        $model->motoCnt = $motoCnt; //合併元科目のレコード数

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd123hQuery::selectQuery($model, $execute_date));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号をHiddenで保持
            knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]."-".$row["TAKESEMES"]);

            //クラス-出席番(表示)
            if ($row["HR_NAME"] != "" && $row["ATTENDNO"] != "") {
                $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
            }

            //名前
            $row["INOUTCD"]     = ($row["INOUTCD"] == '1')? "☆": "　";
            ##DEBUG
            $row["NAME_SHOW"]   = $row["SCHREGNO"]." ".$row["NAME_SHOW"];

            //累積データ
            $row["T_NOTICE"]    = strlen($attend[$row["SCHREGNO"]]["T_NOTICE"]) ? $attend[$row["SCHREGNO"]]["T_NOTICE"] : "0";
            $row["T_LATEEARLY"] = strlen($attend[$row["SCHREGNO"]]["T_LATEEARLY"]) ? $attend[$row["SCHREGNO"]]["T_LATEEARLY"] : "0";
            $row["NOTICE_LATE"] = strlen($attend[$row["SCHREGNO"]]["NOTICE_LATE"]) ? $attend[$row["SCHREGNO"]]["NOTICE_LATE"] : "0";

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            //欠課数の多い生徒
            $row["NAME_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            if ($row["CREDITS"] != "" && $row["ABSENCE_WARN"] != "") {
                $absence_warn = $row["ABSENCE_HIGH"] - $row["CREDITS"] * $row["ABSENCE_WARN"];
                if ($absence_warn < $row["NOTICE_LATE"]) {
                    $row["NAME_COLOR"] = "#ffff00";
                }    //累積注意：黄
            }
            if ($row["ABSENCE_HIGH"] < $row["NOTICE_LATE"]) {
                $row["NAME_COLOR"] = "#ff0099";
            } //累積警告：赤
            //echo $row["ABSENCE_HIGH"]."/".$row["CREDITS"]."/".$row["ABSENCE_WARN"]."/".$row["NOTICE_LATE"]."<br>";

            //単位情報を配列で取得
            knjCreateHidden($objForm, "NOTICE_LATE"."-".$counter, $row["NOTICE_LATE"]."-".$row["CREDITS"]."-".$row["ABSENCE_HIGH"]."-".$row["AUTHORIZE_FLG"]);

            //合併先科目の単位を抽出（2:加算タイプ）
            knjCreateHidden($objForm, "COMBINED_CREDIT_SUM"."-".$counter, $row["COMP_CREDIT_SUM"]."-".$row["GET_CREDIT_SUM"]."-".$row["GET_CREDIT_SUM_Y"]."-".$row["MOTO_CNT"]."-".$row["CREDIT_CNT"]);

            /***
                        //文字評定
                        if (is_array($assess)) $row["MARK_VALUE"] = $assess[$row["GRAD_VALUE"]];
            ***/

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
            $objUp->setType(array(7=>'S',8=>'S',9=>'S',10=>'S',11=>'S',12=>'S',13=>'S',14=>'S',15=>'S',16=>'S',17=>'S',18=>'S'));
            $objUp->setSize(array(7=>3,8=>3,9=>3,10=>3,11=>3,12=>3,13=>3,14=>3,15=>3,16=>2,17=>2,18=>5));
            
            //入力可能なテキストの名前を取得する
            $setTextField = "";
            $textSep = "";
            foreach ($ctrl_name as $code => $col) {
                $edit_flg = true;  //テキストボックス表示フラグ
                //異動情報
                if ($sem != "8" && (strlen($row["TRANSFER_SDATE"]) || strlen($row["TRANSFER_EDATE"]))) {
                    //学期期間中すべて異動期間の場合
                    if (strtotime($row["TRANSFER_SDATE"]) <= strtotime($model->control["学期開始日付"][$sem])
                              && strtotime($row["TRANSFER_EDATE"]) >= strtotime($model->control["学期終了日付"][$sem])) {
                        $edit_flg = false;
                    }
                    //卒業日付
                } elseif ($sem != "8" && strlen($row["GRD_DATE"])) {
                    //学期期間中すべて卒業の場合(学期開始日付以前に卒業している場合）
                    if (strtotime($row["GRD_DATE"]) <= strtotime($model->control["学期開始日付"][$sem])) {
                        $edit_flg = false;
                    }
                }
                //在籍情報がない場合
                if ($sem != "8" && !strlen($row["CHAIR_SEM".$sem])) {
                    $edit_flg = false;
                }
                //ラベルのみ
                if ((!$edit_flg && AUTHORITY != DEF_UPDATABLE)) {
                    $setTextField .= "";
                } elseif (in_array($code, $admin_key)) {
                    $setTextField .= $textSep.$col."-";
                    $textSep = ",";
                }
            }

            //各項目を作成
            foreach ($ctrl_name as $code => $col) {
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
//                if (strlen($row[$col."_DI"]) && $sem_val != "1") $row[$col] = $row[$col."_DI"];//注

                //出欠情報
                if (strlen($row[$col."_ATTEND"])) {
                    $row[$col."_COLOR"] = $backcolor[$row[$col."_ATTEND"]];
                }

                //CSV書き出し
                $csv[] = $row[$col];
                $controlFlg = '';
                //ラベルのみ
                if ((!$edit_flg && AUTHORITY != DEF_UPDATABLE)) {
                    //hidden
                    knjCreateHidden($objForm, $col."-".$counter, $row[$col]);
                    $row[$col] = "<font color=\"#000000\">".$row[$col]."</font>";
                
                //管理者コントロール
                } elseif (in_array($code, $admin_key)) {
                    $controlFlg = '1';
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
                                        "extrahtml" => " onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$counter})\"; STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return showPaste(this);\" id=\"{$col}-{$counter}\""));
                    $row[$col] = $objForm->ge($col."-".$counter);

                    //考査満点マスタ
                    $query = knjd123hQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $perfectcd[$code], $row["GRADE"], $row["COURSE"], $model);
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
                //仮評定フラグ対応
                if ($model->Properties["useProvFlg"] == '1') {
                    if ($col == "GRAD_VALUE") {
                        $chk = $row["PROV_FLG"] == '1' ? ' checked="checked" ' : '';
                        $dis = $controlFlg == '1' ? '' : ' disabled="disabled" ';
                        $row["PROV_FLG"] = knjCreateCheckBox($objForm, "PROV_FLG"."-".$counter, "1", $chk.$dis);
                    }
                }
            }

            //CSV書き出し
            $csv[] = $model->lastColumn;

            $objUp->addCsvValue($csv);

//            $row["MARK_VALUE_ID"] = "mark".$counter;
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
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
        $cur_date = $db->getRow(knjd123hQuery::getMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
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


        // "成績入力完了"処理 追加修正 //

        /*****
        checkbox(成績入力完了)の要不要の判断基準が無い為
        1学期の中間期末、2学期の中間期末を固定で表示する
        ******/

        //初期化
        $j = 1;
        $chkDbValue = array();
        $chngChk = array( "0" => "",
                          "1" => "checked",
                          "" => "" );

        if ($model->field["CHK_FLG"] == "reset") {
            #echo "Do Reset !<BR>";

            $chkDbValue = array( "10101" => null,
                                 "10201" => null,
                                 "20101" => null,
                                 "20201" => null,
                                 "30201" => null  );

            $ableflg["10101"] = (isset($admin_key2["0111"]))? "" : "disabled" ;
            $ableflg["10201"] = (isset($admin_key2["0121"]))? "" : "disabled" ;
            $ableflg["20101"] = (isset($admin_key2["0211"]))? "" : "disabled" ;
            $ableflg["20201"] = (isset($admin_key2["0221"]))? "" : "disabled" ;
            $ableflg["30201"] = (isset($admin_key2["0321"]))? "" : "disabled" ;

            $model->field["CHK_FLG"] = "";

            //データ取得
            $query = knjd123hQuery::getSchChrTestData($model->field["CHAIRCD"]);
            #var_dump($query);
            $result3 = $db->query($query);
            while ($row = $result3->fetchRow(DB_FETCHMODE_ASSOC)) {
                $ExecutedValue = ($row["EXECUTED"] == "1")? 1 : 0;
                $StartchkDbValue = (is_null($chkDbValue[$row["TESTKIND"]]))? 1 : $chkDbValue[$row["TESTKIND"]];
                $chkDbValue[$row["TESTKIND"]] = $StartchkDbValue * $ExecutedValue;
            }
            $result3->free();
        } else {
            #echo "Default <BR>";

            for ($k=1; $k<=5; $k++) {
                $chkDbValue[$k] = ($model->field["CHK_COMP".$i] == "on")? "1" : "0";
            }
        }

        //チェックボックス作成
        foreach ($chkDbValue as $key => $value) {
            $objForm->ae(
                array("type"     => "checkbox",
                                "name"     => "CHK_COMP".$j,
                                "value"    => "on",
                                "extrahtml"=> $chngChk["$value"]." ".$ableflg[$key]." " )
            );

            $arg["CHK_COMP".$j] = $objForm->ge("CHK_COMP".$j);
            $j++;
        }

        /********************************************/
        /* 評定・評価の成績入力完了チェックボックス */
        /********************************************/
        $testcdArray = array("0182" => "19900"
                            ,"0282" => "29900"
                            ,"0882" => "99900");

        //初期化
        $j = 1;
        foreach ($testcdArray as $testkey => $testval) {
            $chk = '';
            $dis = '';
            $query = knjd123hQuery::getRecordChkfinDat($model, $testval);
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
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
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
                            "value"     => ($model->field["SUBCLASSCD"] == $model->gen_ed ? $model->gen_ed : "") ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD123H" ));

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
        View::toHTML($model, "knjd123hForm1.html", $arg);
    }
}
