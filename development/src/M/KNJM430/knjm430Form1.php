<?php

require_once('for_php7.php');

/********************************************************************/
/* 東京都通信制専用成績入力画面                     伊集 2005/09/00 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：変更内容                                 name yyyy/mm/dd */
/********************************************************************/

class knjm430Form1
{
    function main(&$model)
    {
        $objForm = new form;

        $db         = Query::dbCheckOut();

    //TOP***************************************************************************************
    
        //年度
        $arg["TOP"]["YEAR"]     = CTRL_YEAR;

//        //入力順序（学籍番号順／出席番号順）リスト
//        $arg["TOP"]["ORDER"]     = "学籍番号順";
//        $arg["TOP"]["ORDER"]     = $objForm->ge("ORDER");

//        $opt_order  = array();
//        $opt_order[0] = array('label' => "学籍番号順",
//                             'value' => 1);
//        $opt_order[1] = array('label' => "クラス番号順" ,
//                             'value' => 2 );

//        if ($model->Order == "") $model->Order = $opt_order[1]["value"] ;
//        $objForm->ae( array("type"       => "select",
//                            "name"       => "ORDER",
//                            "size"       => "1",
//                            "value"      => $model->Order,
//                            "extrahtml"  => "onChange=\"return btn_submit('main');\"",
//                            "options"    => $opt_order));
//        $arg["TOP"]["ORDER"]     = $objForm->ge("ORDER");

    //出力順ラジオボタンを作成
    $opt[0]=1;
    $opt[1]=2;
    $disable = "";
    if (!$model->order) $model->order = 2;
    if ($model->order == 1) $disable = "disabled";
    $objForm->ae( array("type"       => "radio",
                        "name"       => "ORDER",
                        "value"      => isset($model->order)?$model->order:"2",
                        "extrahtml"  => " onclick =\" return btn_submit('change_order');\"",
                        "multiple"   => $opt));

    $arg["TOP"]["ORDER1"] = $objForm->ge("ORDER",1);
    $arg["TOP"]["ORDER2"] = $objForm->ge("ORDER",2);


        //編集可能学期の判別
        $opt_adm  = array();
        //データセット
        $result = $db->query(knjm430Query::selectContolCodeQuery($model));
        
        while($RowA = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_adm[] = $RowA["CONTROL_CODE"];
        }
        $result->free();


        //科目(講座）リスト
        $opt_sub  = array();
        $opt_sub[0] = array('label' => "",
                            'value' => "");
        //科目(講座）リスト・データセット
        $result = $db->query(knjm430Query::ReadQuery($model));
        
        $subcnt = 1;
        while($RowR = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_sub[$subcnt] = array('label' => $RowR["CHAIRNAME"],
                                      'value' => $RowR["CHAIRCD"].$RowR["SUBCLASSCD"]);
            $subcnt++;
        }
        $result->free();

//        if ($model->sub == "") $model->sub = $opt_sub[0]["value"];

        //出力順が変わったら、科目（講座）リストはクリアされる
        if ($model->cmd == "change_order") $model->sub = $opt_sub[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "SELSUB",
                            "size"       => "1",
                            "value"      => $model->sub,
                            "extrahtml"  => "onChange=\"return btn_submit('change');\"",
                            "options"    => $opt_sub));

        $arg["TOP"]["SELSUB"]     = $objForm->ge("SELSUB");

//        echo knjm430Query::ReadQuery($model)."<br><br>";


        //講座受講クラスリスト
        $opt_cla  = array();
        $opt_cla[0] = array('label' => "",
                            'value' => "");

	    //講座受講クラスリスト・データセット（クラス番号順の場合のみ）
		if($model->order == 2) {
	        $result = $db->query(knjm430Query::ChairClassQuery($model));
	        
	        $subcla = 1;
	        while($RowC = $result->fetchRow(DB_FETCHMODE_ASSOC)){
	            $opt_cla[$subcla] = array('label' => $RowC["CLASSNAME"],
	                                      'value' => $RowC["CGRADE"].$RowC["CCLASS"]);
	            $subcla++;
	        }
	        $result->free();
	    }

        //科目（講座）が変わったら、講座受講クラスリストはクリアされる
        if ($model->cmd == "change") $model->selcla = $opt_cla[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "SELCLA",
                            "size"       => "1",
                            "value"      => $model->selcla,
                            "extrahtml"  => "onChange=\"return btn_submit('change_class');\" ".$disable,
                            "options"    => $opt_cla));

        $arg["TOP"]["SELCLA"]     = $objForm->ge("SELCLA");




    //DATA***************************************************************************************

        //成績データの新規作成
        //学籍番号順の場合⇒科目（講座）が指定されている場合に新規作成
        if($model->order == 1) {
            if($model->sub != "") {
                $db->query(knjm430Query::insertEx_Std_RecQuery($model));
//                echo knjm430Query::insertEx_Std_RecQuery($model)."<br><br>";
            }
        } else {
        //クラス番号順の場合⇒科目（講座）とクラスが指定されている場合に新規作成
            if($model->sub != "" and $model->selcla != "") {
                $db->query(knjm430Query::insertEx_Std_RecQuery($model));
//                echo knjm430Query::insertEx_Std_RecQuery($model)."<br><br>";
            }
        }
        
        //データ配列
        $sch_array = array();
        $class_date = array();
        $sem1_score = array();
        $sem1_val = array();
        $sem2_score = array();
        $sem2_val = array();
        $grad_val = array();
        $comp_credit = array();
        $get_credit = array();
        $credits = array();

        //成績データのリスト
        
//        $result  = $db->query(knjm430Query::GetRecordDatdata($model,$model->sub));
        $result  = $db->query(knjm430Query::GetRecordDatdata($model));
//        echo knjm430Query::GetRecordDatdata($model)."<br><br>";
        
        //件数カウント用初期化
        $ca = 0;        //データ全件数
        while( $row_array = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            
            $ca++;
            
            //学籍番号
            $sch_array[$ca] = $row_array["SCHREGNO"];
            //学年、組、番号
            $class_date[$ca] = $row_array["GRADE"] . "-". $row_array["HR_CLASS"] . "-" . $row_array["ATTENDNO"];
            //生徒氏名
            $name_date[$ca] = $row_array["NAME_SHOW"];
            //前期素点
            $sem1_score[$ca] = $row_array["SEM1_TERM_SCORE"];
            //前期評価
            $sem1_val[$ca] = $row_array["SEM1_VALUE"];
            //後期素点
            $sem2_score[$ca] = $row_array["SEM2_TERM_SCORE"];
            //後期評価
            $sem2_val[$ca] = $row_array["SEM2_VALUE"];
            //評定
            $grad_val[$ca] = $row_array["GRAD_VALUE"];

            //RECORD_DATの履修単位数
            $comp_credit[$ca] = $row_array["COMP_CREDIT"];
            //RECORD_DATの修得単位数
            $get_credit[$ca] = $row_array["GET_CREDIT"];

            //単位マスタから取得したその科目の単位数
            $credits[$ca] = $row_array["CREDITS"];


        }

        //成績レコードが存在する場合
        if($ca != 0) {

            //ページの１行目のカレント行番号（配列指標）
            if($model->line == "") {
                $currentline = 1;
            } else {
                if($model->cmd == "change" or $model->cmd == "change_class") {
                    $currentline = 1;
                } else {
                    $currentline = $model->line;
                }
            }

    //        //ページ総数
    //        $pageall = $count_array / 20;
    //        if(($count_array % 20) > 0) {
    //            $pageall++;
    //        }

            //50件表示ループ
            $counts = 1;                            //カレントページ内での行数
            $pageline = 0;                          //このページの最後の行の全件数の中での行数
            $lineall = $currentline + 50;

            for($pageline=$currentline; $pageline<$lineall; $pageline++,$counts++) {

                //カレント行が全件数を超えたらループ終り
                if($pageline > $ca) {
                    break;
                }

                //学籍番号
                $row["SCHREGNO"] = $sch_array[$pageline];
                $objForm->ae( array("type"      => "hidden",
                                    "name"      => "SCHREGNO_".$counts,
                                    "value"     => $sch_array[$pageline]) );
                $row["SCH_HIDDEN"] = $objForm->ge("SCHREGNO_".$counts);

                //クラス番号（学年－クラス－出席番号）
                $row["GRA_HR_ATTEND"] =  $class_date[$pageline];

                //生徒氏名
                $row["NAME_SHOW"] =  $name_date[$pageline];

                //前期素点
                if(in_array("0111",$opt_adm) == true) {
                    //前期素点テキストボックス
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "SEM1_TERM_SCORE_".$counts,
                                        "size"      => 3,
                                        "maxlength" => 5,
                                        "value"     => $sem1_score[$pageline],
                                        "extrahtml" => "STYLE=\"text-align: right\" onblur=\"check(this)\""));

                    $row["SEM1_TERM_SCORE"] = $objForm->ge("SEM1_TERM_SCORE_".$counts);
                } else {
                    //前期素点表示
                    $row["SEM1_TERM_SCORE"] = $sem1_score[$pageline];
                }

                //前期評価
//                if(array_search("0112",$opt_adm) == true) {
                if(in_array("0112",$opt_adm) == true) {
                    //前期評価テキストボックス
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "SEM1_VALUE_".$counts,
                                        "size"      => 3,
                                        "maxlength" => 5,
                                        "value"     => $sem1_val[$pageline],
                                        "extrahtml" => "STYLE=\"text-align: right\" onblur=\"check(this)\""));

                    $row["SEM1_VALUE"] = $objForm->ge("SEM1_VALUE_".$counts);
                } else {
                    //前期評価表示
                    $row["SEM1_VALUE"] = $sem1_val[$pageline];
                }

                //単位素点
//                if(array_search("0211",$opt_adm) == true) {
                if(in_array("0211",$opt_adm) == true) {
                    //単位素点テキストボックス
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "SEM2_TERM_SCORE_".$counts,
                                        "size"      => 3,
                                        "maxlength" => 5,
                                        "value"     => $sem2_score[$pageline],
                                        "extrahtml" => "STYLE=\"text-align: right\" onblur=\"check(this)\""));

                    $row["SEM2_TERM_SCORE"] = $objForm->ge("SEM2_TERM_SCORE_".$counts);
                } else {
                    //単位素点表示
                    $row["SEM2_TERM_SCORE"] = $sem2_score[$pageline];
                }

                //単位評価
//                if(array_search("0212",$opt_adm) == true) {
                if(in_array("0212",$opt_adm) == true) {
                    //単位評価テキストボックス
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "SEM2_VALUE_".$counts,
                                        "size"      => 3,
                                        "maxlength" => 5,
                                        "value"     => $sem2_val[$pageline],
                                        "extrahtml" => "STYLE=\"text-align: right\" onblur=\"check(this)\""));

                    $row["SEM2_VALUE"] = $objForm->ge("SEM2_VALUE_".$counts);
                } else {
                    //単位評価表示
                    $row["SEM2_VALUE"] = $sem2_val[$pageline];
                }

                //評定
//                if(array_search("0882",$opt_adm) == true) {
                if(in_array("0882",$opt_adm) == true) {

                    //評定テキストボックス
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "GRAD_VALUE_".$counts,
                                        "size"      => 3,
                                        "maxlength" => 5,
                                        "value"     => $grad_val[$pageline],
                                        "extrahtml" => "STYLE=\"text-align: right\" onblur=\"check(this)\""));

                    $row["GRAD_VALUE"] = $objForm->ge("GRAD_VALUE_".$counts);


                } else {
                    //評定表示
                    $row["GRAD_VALUE"] = $grad_val[$pageline];
                }

                //単位マスタから取得した単位（表示用）
                $row["CREDITS"] =  $credits[$pageline];
                //単位マスタから取得した単位（hidden）
                $objForm->ae( array("type"      => "hidden",
                                    "name"      => "CREDITS_".$counts,
                                    "value"     => $credits[$pageline]) );
                $row["CREDITS_HIDDEN"] = $objForm->ge("CREDITS_".$counts);

                //RECORD_DATの履修単位（表示）
                $row["COMP_CREDIT"] =  $comp_credit[$pageline];
                //RECORD_DATの修得単位（表示）
                $row["GET_CREDIT"] =  $get_credit[$pageline];




                $arg["data"][] = $row;
                

            }
        
        } else {    //成績データが存在しない場合（最初に開いた時も）
            $currentline = 0;
        }


        $result->free();



    //BUTTON***************************************************************************************

        //件数表示
        if($ca == 0) {
            $arg["page_count"] = "0-0 / 0";
            $disabled = "disabled ";
        } else {
            $arg["page_count"] = $currentline . "-" . --$pageline . " / " . $ca;
            $disabled = "";
        }
        //ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => $disabled ."onClick=\"btn_submit('update');\"" ) );

        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消" ) );
//                            "extrahtml"   => "onClick=\"btn_submit('reset');\"" ) );
//                            "extrahtml"   => "onclick=\"return ShowConfirm()\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_pre",
                            "value"       => "前ページ",
                            "extrahtml"   => "onClick=\"btn_submit('pre');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => "次ページ",
                            "extrahtml"   => "onClick=\"btn_submit('next');\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_update");
        $arg["btn_reset"]  = $objForm->ge("btn_reset");
        $arg["btn_end"]    = $objForm->ge("btn_end");
        
        //「前ページ」「次ページ」ボタンはレコード件数がゼロの時は表示しない
        if($ca != 0) {
            if($currentline != 1) {
                $arg["btn_pre"]  = $objForm->ge("btn_pre");
            }
            if($pageline < $ca) {
                $arg["btn_next"]    = $objForm->ge("btn_next");
            }
        }

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "line",
                            "value"     => $currentline) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "linecounts",
                            "value"     => --$counts) );
//echo $counts;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "admin",
                            "value"     => implode(",",$opt_adm)) );

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);


        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm430index.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm430Form1.html", $arg); 
    }
}
?>
