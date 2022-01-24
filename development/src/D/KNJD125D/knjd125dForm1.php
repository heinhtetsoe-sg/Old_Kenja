<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd125dForm1.php 76261 2020-08-27 08:53:23Z arakaki $

require_once("csvfile.php");

class knjd125dForm1
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
                        "5" => "クラス－出席番",
                        "6" => "氏名",
                        "0111" => "前期－１学期期末－素点",
                        "0112" => "前期－１学期期末－平常",
                        "0113" => "前期－１学期期末－会話",
                        "0121" => "前期－２学期期末－素点",
                        "0122" => "前期－２学期期末－平常",
                        "0123" => "前期－２学期期末－会話",
                        "0182" => "前期総合",
                        "0183" => "前期評点",
                        "0211" => "後期－３学期期末－素点",
                        "0212" => "後期－３学期期末－平常",
                        "0213" => "後期－３学期期末－会話",
                        "0221" => "後期－４学期期末－素点",
                        "0222" => "後期－４学期期末－平常",
                        "0223" => "後期－４学期期末－会話",
                        "0282" => "後期総合",
                        "0283" => "後期評点",
                        "0883" => "学年評定",
                        "0884" => "履修単位",
                        "0885" => "修得単位",
                        "26" => $model->lastColumn);
        $objUp->setHeader(array_values($header));
        
        $arg["start"]    = $objForm->get_start("main", "POST", "knjd125dindex.php", "", "main");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd125dQuery::selectSubclassQuery($model->gen_ed));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_sbuclass[] = array("label" => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],"value" => $row["SUBCLASSCD"]);
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

        //講座コンボ
        $opt_chair = array();
        $opt_chair[] = array("label" => "", "value" => "");
        $result = $db->query(knjd125dQuery::selectChairQuery($model));
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

        //CSV出力ファイル名
        $objUp->setFileName(CTRL_YEAR."年度_".$subclassname."_".$chairname."_"."成績入力(前期英語).csv");

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

        //試験名称
        $ctrl_name = array("0111" => "SEM1_INTR_SCORE1"
                          ,"0112" => "SEM1_INTR_SCORE2"
                          ,"0113" => "SEM1_INTR_SCORE3"
                          ,"0121" => "SEM1_TERM_SCORE1"
                          ,"0122" => "SEM1_TERM_SCORE2"
                          ,"0123" => "SEM1_TERM_SCORE3"
                          ,"0182" => "SEM1_SCORE"
                          ,"0183" => "SEM1_VALUE"
                          ,"0211" => "SEM2_INTR_SCORE1"
                          ,"0212" => "SEM2_INTR_SCORE2"
                          ,"0213" => "SEM2_INTR_SCORE3"
                          ,"0221" => "SEM2_TERM_SCORE1"
                          ,"0222" => "SEM2_TERM_SCORE2"
                          ,"0223" => "SEM2_TERM_SCORE3"
                          ,"0282" => "SEM2_SCORE"
                          ,"0283" => "SEM2_VALUE"
                          ,"0883" => "GRAD_VALUE"
                          ,"0884" => "COMP_CREDIT"
                          ,"0885" => "GET_CREDIT");

        //考査満点マスタのコード
        $perfectcd = array("0111" => "10101"
                          ,"0112" => "10101"
                          ,"0113" => "10101"
                          ,"0121" => "10201"
                          ,"0122" => "10201"
                          ,"0123" => "10201"
                          ,"0182" => "19900"
                          ,"0183" => "19900"
                          ,"0211" => "20101"
                          ,"0212" => "20101"
                          ,"0213" => "20101"
                          ,"0221" => "20201"
                          ,"0222" => "20201"
                          ,"0223" => "20201"
                          ,"0282" => "29900"
                          ,"0283" => "29900"
                          ,"0883" => "99900"
                          ,"0884" => "00000"
                          ,"0885" => "00000");

        $seme_test = "0";
        $kind_test = "0";

        //管理者コントロール
        $model->testcd = "";    //講座コードをどの考査にセットするかを判断するコードの値を保持用
        $seme_kind_item_s = ""; //最初の値を保持用
        $seme_kind_item_e = ""; //最後の値を保持用
        $soten_flg = false;     //素点の指定があるかどうかのフラグを保持用
        $admin_key = array();
        $result = $db->query(knjd125dQuery::selectContolCodeQuery());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[] = $row["CONTROL_CODE"];
            $admin_key2[$row["CONTROL_CODE"]]=$row["CONTROL_CODE"];

            //履修単位／修得単位の仮コードをセットする
            if ($row["CONTROL_CODE"] == "0885") {
                $admin_key[] = "0884";
            }

            //各期末素点以外の場合、時間割講座テストより試験日の抽出はせず、学籍処理日とする。
            if (substr($row["CONTROL_CODE"], 3, 1) != "1") {
                continue;
            }

            //各期末の仮コードをセットする
            $admin_key[] = substr($row["CONTROL_CODE"], 0, 3) ."2";
            $admin_key[] = substr($row["CONTROL_CODE"], 0, 3) ."3";

            $seme_test = (substr($row["CONTROL_CODE"], 1, 1) == "1") ? "1" : "2" ;
            $kind_test = (substr($row["CONTROL_CODE"], 2, 1) == "1") ? "01" : "02" ;

            //素点の指定があるかどうかのフラグを保持
            if (substr($row["CONTROL_CODE"], 3, 1) == "1") {
                $soten_flg = true;
            }
            //最初の値を保持
            if ($seme_kind_item_s == "") {
                $seme_kind_item_s = $seme_test .$kind_test ."01";
            }
            //最後の値を保持
            $seme_kind_item_e = $seme_test .$kind_test ."01";
        }
        //素点指定があるかつ最初と最後の値が等しい場合のみ、$model->testcdに値をセットする。
        if ($soten_flg && $seme_kind_item_s == $seme_kind_item_e) {
            $model->testcd = $seme_kind_item_e;
        }


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
            $result = $db->query(knjd125dQuery::selectExecuteDateQuery($model, $seme_test, $kind_test));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $execute_date = $row["EXECUTEDATE"];
            }
        }

        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knjd125dQuery::getScAbsentCov($model), DB_FETCHMODE_ASSOC);

        //前期評点・後期評点・学年評定
        $assess = array();
        $result = $db->query(knjd125dQuery::getAssessMark());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $assess[$row["ASSESSCD"]][$row["ASSESSLEVEL"]] = $row["ASSESSMARK"];
        }

        //初期化
        $model->data=array();
        $model->passdata=array();
        $counter=0;
        $btn_update_dis = "disabled";//ボタン disabled true

        //休学時の欠課をカウントするかどうかのフラグ(1 or NULL)を取得。1:欠課をカウントする
        $offdaysFlg = $db->getRow(knjd125dQuery::getOffdaysFlg(CTRL_YEAR), DB_FETCHMODE_ASSOC);
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
        if ($absent["ABSENT_COV"] == "0" || $absent["ABSENT_COV"] == "2") {
            $result = $db->query(knjd125dQuery::getAttendData($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $absent["ABSENT_COV"], $absent["ABSENT_COV_LATE"], $offdaysFlg));
        } else {
            $result = $db->query(knjd125dQuery::getAttendData2($model->field["CHAIRCD"], $model->field["SUBCLASSCD"], $absent["ABSENT_COV_LATE"], $offdaysFlg));
        }
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $attend[$row["SCHREGNO"]]["T_NOTICE"]    = $row["T_NOTICE"];
            $attend[$row["SCHREGNO"]]["T_LATEEARLY"] = $row["T_LATEEARLY"];
            $attend[$row["SCHREGNO"]]["NOTICE_LATE"] = $row["NOTICE_LATE"];
        }

        //合併先科目の単位固定／加算フラグ
        $model->creditflg = $db->getOne(knjd125dQuery::getCalculateCreditFlg($model->field["SUBCLASSCD"]));

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd125dQuery::selectQuery($model, $execute_date));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

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

            //ゼロ埋めフラグ
            $flg = array("科目コード" => array(true,6),
                         "講座コード" => array(true,7),
                         "学籍番号"   => array(true,8));

            $objUp->setEmbed_flg($flg);
            $objUp->setType(array(7=>'S',8=>'S',9=>'S',10=>'S',11=>'S',12=>'S',13=>'S',14=>'S',15=>'S',16=>'S',17=>'S',18=>'S',19=>'S',20=>'S',21=>'S',22=>'S',23=>'S',24=>'S',25=>'S',26=>'S'));
            $objUp->setSize(array(7=>3,8=>2,9=>2,10=>3,11=>2,12=>2,13=>3,14=>2,15=>3,16=>2,17=>2,18=>3,19=>2,20=>2,21=>3,22=>2,23=>1,24=>2,25=>2,26=>5));

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            //各項目を作成
            foreach ($ctrl_name as $code => $col) {
                $edit_flg = $edit_flg2 = true;
                $sem = substr($code, 1, 1);
                $ass_val = substr($code, 2, 2);
                $row[$col."_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

                //各データを取得
                $model->data[$col."-".$counter]        = $row[$col];
                $model->passdata[$col."-".$counter]    = $row[$col."_PASS"];

                //追試・再試情報
                if (strlen($row[$col."_PASS"])) {
                    $row[$col] = $row[$col."_PASS"];
                }

                //学期成績集計項目
                if (is_numeric($row[$col])) {
                    $term_data[$col][] = $row[$col];
                }

                //異動情報
                if ($sem != "8" && (strlen($row["TRANSFER_SDATE"]) || strlen($row["TRANSFER_EDATE"]))) {

                    //学期期間中すべて異動期間の場合
                    if (strtotime($row["TRANSFER_SDATE"]) <= strtotime($model->control["学期開始日付"][$sem]) &&
                        strtotime($row["TRANSFER_EDATE"]) >= strtotime($model->control["学期終了日付"][$sem])) {
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

                //出欠情報
                if (strlen($row[$col."_ATTEND"])) {
                    $edit_flg2 = false;
                    $row[$col."_COLOR"] = $backcolor[$row[$col."_ATTEND"]];
                }
                //追試・再試情報
                if (strlen($row[$col."_FLG"])) {
                    $edit_flg2 = false;
                }

                //前期評点・後期評点・学年評定
                if ($ass_val == "83") {
                    $ass_cd = ($sem != "8") ? "2" : "3";
                    if (strlen($assess[$ass_cd][$row[$col]])) {
                        $row[$col] = $assess[$ass_cd][$row[$col]];
                    }
                }

                //CSV書き出し
                $csv[] = $row[$col];

                //ラベルのみ
                if ((!$edit_flg && AUTHORITY != DEF_UPDATABLE) || !$edit_flg2) {
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
                                        "extrahtml" => "STYLE=\"text-align: right; width:29\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" onPaste=\"return show(this);\" "));
                    $row[$col] = $objForm->ge($col."-".$counter);

                    //ボタン disabled false
                    $btn_update_dis = "";

                    //考査満点マスタ
                    $query = knjd125dQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $perfectcd[$code], $row["GRADE"], $row["COURSE"]);
                    $perfect = ($model->usePerfect == 'true') ? $db->getOne($query) : 100;
                    if ($perfect == "") {
                        $perfect = 100;
                    }
                    //テキストボックスを作成
                    $objForm->ae(array("type"      => "hidden",
                                        "name"      => $col."_PERFECT"."-".$counter,
                                        "value"     => $perfect ));
                }
            }

            //CSV書き出し
            $csv[] = $model->lastColumn;

            $objUp->addCsvValue($csv);

            $row["MARK_VALUE_ID"] = "mark".$counter;
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

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
        $cur_date = $db->getRow(knjd125dQuery::getMax($model->field["SUBCLASSCD"]), DB_FETCHMODE_ASSOC);
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


        //初期化
        $j = 1;
        $chkDbValue = array();
        $chngChk = array( "0" => "",
                          "1" => "checked",
                          "" => "" );

        if ($model->field["CHK_FLG"] == "reset") {
            $chkDbValue = array( "10101" => null,
                                 "10201" => null,
                                 "20101" => null,
                                 "20201" => null  );

            $ableflg["10101"] = (isset($admin_key2["0111"]))? "" : "disabled" ;
            $ableflg["10201"] = (isset($admin_key2["0121"]))? "" : "disabled" ;
            $ableflg["20101"] = (isset($admin_key2["0211"]))? "" : "disabled" ;
            $ableflg["20201"] = (isset($admin_key2["0221"]))? "" : "disabled" ;

            $model->field["CHK_FLG"] = "";

            //データ取得
            $query = knjd125dQuery::getSchChrTestData($model->field["CHAIRCD"]);
            $result3 = $db->query($query);
            while ($row = $result3->fetchRow(DB_FETCHMODE_ASSOC)) {
                $ExecutedValue = ($row["EXECUTED"] == "1")? 1 : 0;
                $StartchkDbValue = (is_null($chkDbValue[$row["TESTKIND"]]))? 1 : $chkDbValue[$row["TESTKIND"]];
                $chkDbValue[$row["TESTKIND"]] = $StartchkDbValue * $ExecutedValue;
            }
            $result3->free();
        } else {
            for ($k=1; $k<=4; $k++) {
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


        //変更保存用hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "CHK_FLG",
                            "value"     => $model->field["CHK_FLG"]
                            ));


        Query::dbCheckIn($db);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\" " .$btn_update_dis ));
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
                            "value"     => (substr($model->field["SUBCLASSCD"], 0, 2) == $model->gen_ed ? $model->gen_ed : "") ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD125D" ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => CTRL_SEMESTER ));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "STAFF",
                            "value"     => STAFFCD ));

        //試験日
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "TEST_DATE",
                            "value"     => $execute_date ));

        //平常点のＭＡＸ値
        if ("2007" < CTRL_YEAR &&
           ($model->field["SUBCLASSCD"] == "010100" ||
            $model->field["SUBCLASSCD"] == "030100" ||
            $model->field["SUBCLASSCD"] == "090100")) {
            $maxScore2 = 30;
        } else {
            $maxScore2 = 10;
        }
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "MAXSCORE2",
                            "value"     => $maxScore2 ));
        //更新権限チェック
        knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
        knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd125dForm1.html", $arg);
    }
}
