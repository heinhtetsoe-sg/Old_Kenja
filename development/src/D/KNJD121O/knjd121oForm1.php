<?php

require_once('for_php7.php');


// kanji=漢字
// $Id: knjd121oForm1.php,v 1.3 2012/06/05 08:21:42 hamasaki Exp $


class knjd121oForm1
{
    function main(&$model)
    {
        $objForm = new form;
        
        $arg["start"]    = $objForm->get_start("main", "POST", "knjd121oindex.php", "", "main");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //テスト名取得
        $semename = $testname = array();
        $query = knjd121oQuery::getTestName($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $semename[$row["VALUE_SEME"]] = $row["SEMESTERNAME"];
            $testname[$row["VALUE_TEST"]] = $row["TESTITEMNAME"];
            $arg["SEMENAME".$row["VALUE_SEME"]] = $row["SEMESTERNAME"];
            $arg["TESTNAME".$row["VALUE_TEST"]] = $row["TESTITEMNAME"];
        }

        //新規作成
        if ($model->cmd != "subclasscd") {
            $db->query(knjd121oQuery::insertEx_Std_RecQuery($model));
        }
        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd121oQuery::selectSubclassQuery($model->gen_ed, $model));
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],
                                        "value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]);
                if ($row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"] == $model->field["SUBCLASSCD"]) $subclassname = $row["SUBCLASSNAME"];
            }
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_sbuclass[] = array("label" => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],"value" => $row["SUBCLASSCD"]);
                if ($row["SUBCLASSCD"] == $model->field["SUBCLASSCD"]) $subclassname = $row["SUBCLASSNAME"];
            }
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "SUBCLASSCD",
                            "size"        => "1",
                            "value"       => $model->field["SUBCLASSCD"],
                            "options"     => $opt_sbuclass,
                            "extrahtml"   => "onChange=\"btn_submit('subclasscd')\";"));
        $arg["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");

        //講座コンボ
        $opt_chair = array();
        $opt_chair[] = array("label" => "", "value" => "");
        $result = $db->query(knjd121oQuery::selectChairQuery($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_chair[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],"value" => $row["CHAIRCD"]);
            if ($row["CHAIRCD"] == $model->field["CHAIRCD"])
                $chairname = $row["CHAIRNAME"];
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "CHAIRCD",
                            "size"        => "1",
                            "value"       => $model->field["CHAIRCD"],
                            "options"     => $opt_chair,
                            "extrahtml"   => "onChange=\"btn_submit('chaircd')\";"
                           ));
        $arg["CHAIRCD"] = $objForm->ge("CHAIRCD");

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
                          ,"0131" => "SEM1_TERM2_SCORE"//Add
                          ,"0132" => "SEM1_TERM2_VALUE"//Add
                          ,"0182" => "SEM1_VALUE"
                          ,"0211" => "SEM2_INTR_SCORE"
                          ,"0212" => "SEM2_INTR_VALUE"
                          ,"0221" => "SEM2_TERM_SCORE"
                          ,"0222" => "SEM2_TERM_VALUE"
                          ,"0282" => "SEM2_VALUE"
                          ,"0882" => "GRAD_VALUE"
                          ,"0884" => "COMP_CREDIT"
                          ,"0885" => "GET_CREDIT");

        $seme_test = "0";
        $kind_test = "0";
        $item_test = "0";//Add

        //管理者コントロール
        $model->testcd = "";    //講座コードをどの考査にセットするかを判断するコードの値を保持用 NO001Add
        $seme_kind_item_s = ""; //最初の値を保持用 NO001Add
        $seme_kind_item_e = ""; //最後の値を保持用 NO001Add
        $soten_flg = false;     //素点の指定があるかどうかのフラグを保持用 NO001Add
        $admin_key = array();
        $result = $db->query(knjd121oQuery::selectContolCodeQuery());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $admin_key[] = $row["CONTROL_CODE"];
            $admin_key2[$row["CONTROL_CODE"]]=$row["CONTROL_CODE"];

            //単位入力欄の入力可／不可の区別は、学年評定欄の管理者コントロールの指定に依存する
            //よって、学年評定の場合、履修単位／修得単位の仮コードをセットする
            if ($row["CONTROL_CODE"] == "0882") {
                $admin_key[] = "0884";
                $admin_key[] = "0885";
            }

            //中間・期末素点以外の場合、時間割講座テストより試験日の抽出はせず、学籍処理日とする。
            if (substr($row["CONTROL_CODE"], 3, 1) != "1") continue;

            $seme_test = (substr($row["CONTROL_CODE"], 1, 1) == "1") ? "1" : "2" ;
            $kind_test = (substr($row["CONTROL_CODE"], 2, 1) == "1") ? "01" : "02" ;
            $item_test = (substr($row["CONTROL_CODE"], 2, 1) == "3") ? "02" : "01" ;//Add

            //素点の指定があるかどうかのフラグを保持 NO001Add
            if (substr($row["CONTROL_CODE"], 3, 1) == "1") $soten_flg = true;
            //最初の値を保持 NO001Add*
            if ($seme_kind_item_s == "") $seme_kind_item_s = $seme_test .$kind_test .$item_test;
            //最後の値を保持 NO001Add*
            $seme_kind_item_e = $seme_test .$kind_test .$item_test;
        }
        //素点指定があるかつ最初と最後の値が等しい場合のみ、$model->testcdに値をセットする。 NO001Add
        if ($soten_flg && $seme_kind_item_s == $seme_kind_item_e) $model->testcd = $seme_kind_item_e;
//echo "最初=" .$seme_kind_item_s ." " ."最後=" .$seme_kind_item_e ." " ."素点フラグ=" .$soten_flg ." " ."testcd=" .$model->testcd;

        //時間割講座テストより試験日を抽出
        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        $sdate = str_replace("/","-",$model->control["学期開始日付"][CTRL_SEMESTER]);
        $edate = str_replace("/","-",$model->control["学期終了日付"][CTRL_SEMESTER]);
        if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
            $execute_date = CTRL_DATE;//初期値
        } else {
            $execute_date = $edate;//初期値
        }
        if ($seme_test != "0") {
            $result = $db->query(knjd121oQuery::selectExecuteDateQuery($model, $seme_test, $kind_test, $item_test));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $execute_date = $row["EXECUTEDATE"];
            }
        }
//echo "seme_test = " .$seme_test ." kind_test = " .$kind_test ." execute_date = " .$execute_date;

        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knjd121oQuery::getScAbsentCov($model),DB_FETCHMODE_ASSOC);

        //文字評定
        if ($model->gen_ed == substr($model->field["SUBCLASSCD"],0,2)) {
            $assess = array();
            $result = $db->query(knjd121oQuery::GetAssessMark());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $assess[$row["ASSESSLEVEL"]] = $row["ASSESSMARK"];
                $arg["data2"][] = array("ASSESSLEVEL" => $row["ASSESSLEVEL"], "ASSESSMARK" => $row["ASSESSMARK"]);
            }
        }
        
        //初期化
        $model->data=array();
        $model->attend_data = array(); //出欠情報
        $counter=0;

        //累積情報//Modify
        $attend = array();
        if ($absent["ABSENT_COV"] == "0" || $absent["ABSENT_COV"] == "2" || $absent["ABSENT_COV"] == "4") { 
            $result = $db->query(knjd121oQuery::GetAttendData($model->field["CHAIRCD"],$model->field["SUBCLASSCD"],$absent["ABSENT_COV"],$absent["ABSENT_COV_LATE"], $model));
        } else {
            $result = $db->query(knjd121oQuery::GetAttendData2($model->field["CHAIRCD"],$model->field["SUBCLASSCD"],$absent["ABSENT_COV"],$absent["ABSENT_COV_LATE"], $model));
        }
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $attend[$row["SCHREGNO"]]["T_NOTICE"]    = $row["T_NOTICE"];
            $attend[$row["SCHREGNO"]]["T_LATEEARLY"] = $row["T_LATEEARLY"];
            $attend[$row["SCHREGNO"]]["NOTICE_LATE"] = $row["NOTICE_LATE"];
        }

        //合併先科目の単位固定／加算フラグ
        $model->creditflg = $db->getOne(knjd121oQuery::getCalculateCreditFlg($model->field["SUBCLASSCD"], $model));
//if (isset($model->creditflg)) echo "))". $model->creditflg ."((" ."<BR>";

        //一覧表示
        $result = $db->query(knjd121oQuery::selectQuery($model, $execute_date));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"]."-".$row["TAKESEMES"];

            //クラス-出席番(表示)
            if($row["HR_NAME"] != "" && $row["ATTENDNO"] != ""){
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

            //単位情報を配列で取得
            $model->data["NOTICE_LATE"][] = $row["NOTICE_LATE"]."-".$row["CREDITS"]."-".$row["ABSENCE_HIGH"]."-".$row["AUTHORIZE_FLG"];

            //合併先科目の単位を抽出（2:加算タイプ）
            $model->data["COMBINED_CREDIT_SUM"][] = $row["COMP_CREDIT_SUM"]."-".$row["GET_CREDIT_SUM"];

            //文字評定
            if (is_array($assess)) $row["MARK_VALUE"] = $assess[$row["GRAD_VALUE"]];

            //各項目を作成
            foreach ($ctrl_name as $code => $col)
            {
                //各データを取得
                $model->data[$col."-".$counter]        = $row[$col];
                $model->attend_data[$col."-".$counter] = $row[$col."_DI"];

                //学期成績集計項目
                if(is_numeric($row[$col])) {
                   $term_data[$col][] = $row[$col];
                }

                $edit_flg = true;  //テキストボックス表示フラグ
                $sem = substr($code, 1, 1);
                $sem_val = substr($code, 3, 1);
                $row[$col."_COLOR"] = "#ffffff";

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
                    if ($sem <= CTRL_SEMESTER) $row[$col."_COLOR"]="#ffff00";
                }

                //欠課時数情報('-','=')
                if (strlen($row[$col."_DI"]) && $sem_val == "2") $row[$col] = $row[$col."_DI"];

                //出欠情報
                if (strlen($row[$col."_ATTEND"])) {
                    $row[$col."_COLOR"] = $backcolor[$row[$col."_ATTEND"]];
                }
                
                //ラベルのみ
                if((!$edit_flg && AUTHORITY != DEF_UPDATABLE))
                {
                    $row[$col] = "<font color=\"#000000\">".$row[$col]."</font>";
                
                //管理者コントロール
                } elseif(in_array($code, $admin_key)) {

                    //出欠情報がある場合はそれを表示
                    $value = $row[$col];

                    //テキストボックスを作成
                    $objForm->ae( array("type"      => "text",
                                        "name"      => $col."-".$counter,
                                        "size"      => "3",
                                        "maxlength" => ($code == "0884" || $code == "0885") ? "2" : "3",
                                        "value"     => $value,
                                        "extrahtml" => "STYLE=\"text-align: right; width:31\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onKeyDown=\"keyChangeEntToTab(this)\" "));
                    $row[$col] = $objForm->ge($col."-".$counter);
                }
            }

            $row["MARK_VALUE_ID"] = "mark".$counter;
            $row["COLOR"]="#ffffff";

            $counter++;
            $arg["data"][] = $row;
        }

        //学期ごとの集計
        foreach ($ctrl_name as $code => $col)
        {
            if(isset($term_data[$col])){
                //合計
                $arg[$col."_SUM"]=array_sum($term_data[$col]);
                //平均
                $arg[$col."_AVG"]=round((array_sum($term_data[$col])/get_count($term_data[$col]))*10)/10;
                //最高点と最低点を求める
                array_multisort ($term_data[$col], SORT_NUMERIC);
                $max = get_count($term_data[$col])-1;
                //最高点
                $arg[$col."_MAX"]=$term_data[$col][$max];
                //最低点
                $arg[$col."_MIN"]=$term_data[$col][0];
            }
        }

        //累積現在日
        $cur_date = $db->getRow(knjd121oQuery::GetMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
        if (is_array($cur_date)) {
            $arg["CUR_DATE"] = $cur_date["YEAR"]."年度".$model->control["学期名"][$cur_date["SEMESTER"]]."<BR>".(int)$cur_date["MONTH"]."月".$cur_date["APPOINTED_DAY"]."日現在";
        }

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTENDSUBYEAR",
                            "value"     => $cur_date["NEN_YEAR"] )  );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTENDSUBMONTH",
                            "value"     => $cur_date["MONTH"] )  );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTENDSUBDAY",
                            "value"     => $cur_date["APPOINTED_DAY"] )  );


    // "成績入力完了"処理 //

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

        if($model->field["CHK_FLG"] == "reset")
        {
            #echo "Do Reset !<BR>";

            $chkDbValue = array( "10101" => null,
                                 "10201" => null,
                                 "10202" => null,//Add
                                 "20101" => null,
                                 "20201" => null  );

            $ableflg["10101"] = (isset($admin_key2["0111"]))? "" : "disabled" ;
            $ableflg["10201"] = (isset($admin_key2["0121"]))? "" : "disabled" ;
            $ableflg["10202"] = (isset($admin_key2["0131"]))? "" : "disabled" ;
            $ableflg["20101"] = (isset($admin_key2["0211"]))? "" : "disabled" ;
            $ableflg["20201"] = (isset($admin_key2["0221"]))? "" : "disabled" ;

            $model->field["CHK_FLG"] = "";

            //データ取得
            $query = knjd121oQuery::getSchChrTestData($model->field["CHAIRCD"]);
            #var_dump($query);
            $result3 = $db->query($query);
            while( $row = $result3->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $ExecutedValue = ($row["EXECUTED"] == "1")? 1 : 0;
                $StartchkDbValue = (is_null($chkDbValue[$row["TESTKIND"]]))? 1 : $chkDbValue[$row["TESTKIND"]];
                $chkDbValue[$row["TESTKIND"]] = $StartchkDbValue * $ExecutedValue;
            }
            $result3->free();

        }else{
            #echo "Default <BR>";

            for($k=1;$k<=5;$k++)
            {
                $chkDbValue[$k] = ($model->field["CHK_COMP".$i] == "on" )? "1" : "0";
            }
        }

        //チェックボックス作成
        foreach ($chkDbValue as $key => $value) 
        {
            $objForm->ae( array("type"     => "checkbox",
                                "name"     => "CHK_COMP".$j,
                                "value"    => "on",
                                "extrahtml"=> $chngChk["$value"]." ".$ableflg[$key]." " )
                         );

            $arg["CHK_COMP".$j] = $objForm->ge("CHK_COMP".$j);
            $j++;
        }

        //変更保存用hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CHK_FLG",
                            "value"     => $model->field["CHK_FLG"]
                            ) );


    // "成績入力完了"処理 おわり //


        Query::dbCheckIn($db);

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["btn_end"] = $objForm->ge("btn_end");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "印 刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );
        $arg["btn_print"] = $objForm->ge("btn_print");

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "gen_ed",
                            "value"     => (substr($model->field["SUBCLASSCD"],0,2) == $model->gen_ed ? $model->gen_ed : "" ) ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD121T" )  );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR )  );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => CTRL_SEMESTER )  );

        //ログインした職員コード
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STAFF",
                            "value"     => STAFFCD )  );

        //試験日
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TEST_DATE",
                            "value"     => $execute_date )  );

        //テスト項目マスタ
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "COUNTFLG",
                            "value"     => $model->testTable ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMENAME1",
                            "value"     => $semename[1] ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMENAME2",
                            "value"     => $semename[2] ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTNAME10101",
                            "value"     => $testname[10101] ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTNAME10201",
                            "value"     => $testname[10201] ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTNAME10202",
                            "value"     => $testname[10202] ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTNAME20101",
                            "value"     => $testname[20101] ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTNAME20201",
                            "value"     => $testname[20201] ) );
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd121oForm1.html", $arg);
    }
}
?>
