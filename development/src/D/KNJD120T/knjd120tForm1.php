<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjd120tForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $objUp = new csvFile();

        //CSVヘッダ名
        $header = array("0" => "科目コード",
                        "1" => "科目名",
                        "2" => "講座コード",
                        "3" => "講座名",
                        "4" => "学籍番号",
                        "5" => "クラス－出席番",
                        "6" => "氏名",
                        "0111" => "前期－中間－素点",
                        "0112" => "前期－中間－評価",
                        "0121" => "前期－期末－素点",
                        "0122" => "前期－期末－評価",
                        "0182" => "前期評価",
                        "0211" => "後期－中間－素点",
                        "0212" => "後期－中間－評価",
                        "0221" => "後期－期末－素点",
                        "0222" => "後期－期末－評価",
                        "0282" => "後期評価",
                        "0882" => "学年評定");
        $objUp->setHeader(array_values($header));
        
        $arg["start"]    = $objForm->get_start("main", "POST", "knjd120tindex.php", "", "main");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //新規作成
        if ($model->cmd != "subclasscd") {
            $db->query(knjd120tQuery::insertEx_Std_RecQuery($model));
        }

        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd120tQuery::selectSubclassQuery($model->gen_ed, $model));
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	        {
	            $opt_sbuclass[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],"value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]);
	            if ($row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"] == $model->field["SUBCLASSCD"])
	                $subclassname = $row["SUBCLASSNAME"];
	        }
        } else {
	        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	        {
	            $opt_sbuclass[] = array("label" => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],"value" => $row["SUBCLASSCD"]);
	            if ($row["SUBCLASSCD"] == $model->field["SUBCLASSCD"])
	                $subclassname = $row["SUBCLASSNAME"];
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
        $result = $db->query(knjd120tQuery::selectChairQuery($model));
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

        //CSV出力ファイル名
        $objUp->setFileName(CTRL_YEAR."年度_".$subclassname."_".$chairname."_"."成績入力(前後期制).csv");

        $backcolor = array( "KK" => "#3399ff",  //公欠
                            "KS" => "#ff0099"); //病欠

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
                          ,"0882" => "GRAD_VALUE");

        //試験名称NAMECD2
        $perfectcd = array("0111" => "10101"
                          ,"0112" => "10101"
                          ,"0121" => "10201"
                          ,"0122" => "10201"
                          ,"0182" => "19900"
                          ,"0211" => "20101"
                          ,"0212" => "20101"
                          ,"0221" => "20201"
                          ,"0222" => "20201"
                          ,"0231" => "20202"
                          ,"0232" => "20202"
                          ,"0282" => "29900"
                          ,"0882" => "99900"
                          ,"0884" => "99900"
                          ,"0885" => "99900");

        //管理者コントロール
        $admin_key = array();
        $result = $db->query(knjd120tQuery::selectContolCodeQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $admin_key[] = $row["CONTROL_CODE"];
        }

        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knjd120tQuery::getScAbsentCov($model),DB_FETCHMODE_ASSOC);

        //文字評定
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
	        if ($model->gen_ed == $subclass_array[3]) {
	            $assess = array();
	            $result = $db->query(knjd120tQuery::GetAssessMark());
	            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	            {
	                $assess[$row["ASSESSLEVEL"]] = $row["ASSESSMARK"];
	                $arg["data2"][] = array("ASSESSLEVEL" => $row["ASSESSLEVEL"], "ASSESSMARK" => $row["ASSESSMARK"]);
	            }
	        }
        } else {
	        if ($model->gen_ed == $model->field["SUBCLASSCD"]) {
	            $assess = array();
	            $result = $db->query(knjd120tQuery::GetAssessMark());
	            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	            {
	                $assess[$row["ASSESSLEVEL"]] = $row["ASSESSMARK"];
	                $arg["data2"][] = array("ASSESSLEVEL" => $row["ASSESSLEVEL"], "ASSESSMARK" => $row["ASSESSMARK"]);
	            }
	        }
        }
        
        //初期化
        $model->data=array();
        $model->attend_data = array(); //出欠情報
        $counter=0;

        //累積情報
        $attend = array();
        if ($absent["ABSENT_COV"] == "0" || $absent["ABSENT_COV"] == "2") { 
            $result = $db->query(knjd120tQuery::GetAttendData($model->field["CHAIRCD"],$model->field["SUBCLASSCD"],$absent["ABSENT_COV"],$absent["ABSENT_COV_LATE"], $model));
        } else {
            $result = $db->query(knjd120tQuery::GetAttendData2($model->field["CHAIRCD"],$model->field["SUBCLASSCD"],$absent["ABSENT_COV_LATE"], $model));
        }
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $attend[$row["SCHREGNO"]]["T_NOTICE"]    = $row["T_NOTICE"];
            $attend[$row["SCHREGNO"]]["T_LATEEARLY"] = $row["T_LATEEARLY"];
            $attend[$row["SCHREGNO"]]["NOTICE_LATE"] = $row["NOTICE_LATE"];
        }

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd120tQuery::selectQuery($model));
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
            //$row["NAME_SHOW"]   = $row["INOUTCD"]." ".$row["NAME_SHOW"];
            ##DEBUG
            $row["NAME_SHOW"]   = $row["SCHREGNO"]." ".$row["NAME_SHOW"];

            //累積データ
            $row["T_NOTICE"]    = strlen($attend[$row["SCHREGNO"]]["T_NOTICE"]) ? $attend[$row["SCHREGNO"]]["T_NOTICE"] : "0";
            $row["T_LATEEARLY"] = strlen($attend[$row["SCHREGNO"]]["T_LATEEARLY"]) ? $attend[$row["SCHREGNO"]]["T_LATEEARLY"] : "0";
            $row["NOTICE_LATE"] = strlen($attend[$row["SCHREGNO"]]["NOTICE_LATE"]) ? $attend[$row["SCHREGNO"]]["NOTICE_LATE"] : "0";

            //文字評定
            if (is_array($assess)) $row["MARK_VALUE"] = $assess[$row["GRAD_VALUE"]];

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
            $objUp->setType(array(7=>'S',8=>'S',9=>'S',10=>'S',11=>'S',12=>'S',13=>'S',14=>'S',15=>'S',16=>'S',17=>'S'));
            $objUp->setSize(array(7=>3,8=>3,9=>3,10=>3,11=>3,12=>3,13=>3,14=>3,15=>3,16=>3,17=>3));

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
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
                $row[$col."_COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

                //異動情報
                if ($sem != "8" && (strlen($row["TRANSFER_SDATE"]) || strlen($row["TRANSFER_EDATE"]))) {

                    //学期期間中すべて異動期間の場合
                    if (strtotime($row["TRANSFER_SDATE"]) <= strtotime($model->control["学期開始日付"][$sem])
                              && strtotime($row["TRANSFER_EDATE"]) >= strtotime($model->control["学期終了日付"][$sem])) {
                        $edit_flg = false;
                        $row[$col."_COLOR"]="#ffff00";
                    //一部
#                    } elseif (strtotime($row["TRANSFER_SDATE"]) <= strtotime($model->control["学期開始日付"][$sem])    2005/01/21 arakaki
#                              || strtotime($row["TRANSFER_EDATE"]) >= strtotime($model->control["学期終了日付"][$sem])) {
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
                    $row[$col."_COLOR"]="#ffff00";
                }

                //出欠情報
                if (strlen($row[$col."_DI"])) {
                    $row[$col."_COLOR"] = $backcolor[$row[$col."_DI"]];
                    $row[$col]          = $row[$col."_DI"];
                }

                //CSV書き出し
                $csv[] = strlen($row[$col."_DI"]) ? $row[$col."_DI"] : $row[$col];
                
                //ラベルのみ
                if((!$edit_flg && AUTHORITY != DEF_UPDATABLE))
                {
                    $row[$col] = "<font color=\"#000000\">".$row[$col]."</font>";
                
                //管理者コントロール
#                } elseif(AUTHORITY == DEF_UPDATABLE || in_array($code, $admin_key)) {      //2005/05/23
                } elseif(in_array($code, $admin_key)) {

                    //入力エリアとキーをセットする
                    $objUp->setElementsValue($col."-".$counter, $header[$code], $key);

                    //出欠情報がある場合はそれを表示
                    $value = (strlen($row[$col."_DI"])) ? $row[$col."_DI"] : $row[$col];

                    //テキストボックスを作成
                    $objForm->ae( array("type"      => "text",
                                        "name"      => $col."-".$counter,
                                        "size"      => "3",
                                        "maxlength" => "3",
                                        "value"     => $value,
                                        "extrahtml" => "STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onPaste=\"return show(this);\" "));
                    $row[$col] = $objForm->ge($col."-".$counter);

                    //考査満点マスタ
                    $query = knjd120tQuery::getPerfect(CTRL_YEAR, $model->field["SUBCLASSCD"], $perfectcd[$code], $row["GRADE"], $row["COURSE"], $model);
                    $perfect = ($model->usePerfect == 'true') ? $db->getOne($query) : 100;
                    if ($perfect == "") $perfect = 100;
                    //テキストボックスを作成
                    $objForm->ae( array("type"      => "hidden",
                                        "name"      => $col."_PERFECT"."-".$counter,
                                        "value"     => $perfect ) );
                }
            }

            $objUp->addCsvValue($csv);

            $row["MARK_VALUE_ID"] = "mark".$counter;
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

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
                $arg[$col."_AVG"]=round((array_sum($term_data[$col])/get_count($term_data[$col])));
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
        $cur_date = $db->getRow(knjd120tQuery::GetMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
        if (is_array($cur_date)) {
            $arg["CUR_DATE"] = $cur_date["YEAR"]."年度".$model->control["学期名"][$cur_date["SEMESTER"]]."<BR>".(int)$cur_date["MONTH"]."月".$cur_date["APPOINTED_DAY"]."日現在";
        }
        Query::dbCheckIn($db);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
	        $objForm->ae( array("type"      => "hidden",
	                            "name"      => "gen_ed",
	                            "value"     => ($subclass_array[3] == $model->gen_ed ? $model->gen_ed : "" ) ) );
        } else {
	        $objForm->ae( array("type"      => "hidden",
	                            "name"      => "gen_ed",
	                            "value"     => ($model->field["SUBCLASSCD"] == $model->gen_ed ? $model->gen_ed : "" ) ) );
		}

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD122" )  );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR )  );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => CTRL_SEMESTER )  );
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjd120tForm1.html", $arg);
    }
}
?>
