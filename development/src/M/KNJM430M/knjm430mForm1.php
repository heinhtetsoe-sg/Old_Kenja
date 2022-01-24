<?php

require_once('for_php7.php');


class knjm430mForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $db         = Query::dbCheckOut();

        //TOP***************************************************************************************

        //年度
        $arg["TOP"]["YEAR"]     = CTRL_YEAR;

        //出力順ラジオボタンを作成
        $opt[0]=1;
        $opt[1]=2;
        $disable = "";
        if (!$model->order) $model->order = 1;
        if ($model->order == 1) $disable = "disabled";
        $objForm->ae( array("type"       => "radio",
                            "name"       => "ORDER",
                            "value"      => isset($model->order)?$model->order:"1",
                            "extrahtml"  => " onclick =\" return btn_submit('change_order');\"",
                            "multiple"   => $opt));

        $arg["TOP"]["ORDER1"] = $objForm->ge("ORDER",1);
        $arg["TOP"]["ORDER2"] = $objForm->ge("ORDER",2);


        //編集可能学期の判別
        $opt_adm  = array();
        //データセット
        $result = $db->query(knjm430mQuery::selectContolCodeQuery($model));
        while ($RowA = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_adm[] = $RowA["CONTROL_CODE"];
        }
        $result->free();


        //科目(講座）リスト
        $opt_sub  = array();
        $opt_sub[0] = array('label' => "",
                            'value' => "");
        //科目(講座）リスト・データセット
        $result = $db->query(knjm430mQuery::ReadQuery($model));
        $subcnt = 1;
        while ($RowR = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_sub[$subcnt] = array('label' => $RowR["CHAIRNAME"],
                                      'value' => $RowR["CHAIRCD"].$RowR["SUBCLASSCD"]);
            $subcnt++;
        }
        $result->free();


        //出力順が変わったら、科目（講座）リストはクリアされる
        if ($model->cmd == "change_order") $model->sub = $opt_sub[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "SELSUB",
                            "size"       => "1",
                            "value"      => $model->sub,
                            "extrahtml"  => "onChange=\"return btn_submit('change');\"",
                            "options"    => $opt_sub));

        $arg["TOP"]["SELSUB"]     = $objForm->ge("SELSUB");



        //DATA***************************************************************************************

        //成績データの新規作成
        //科目（講座）が指定されている場合に新規作成
        if ($model->sub != "") {
            $db->query(knjm430mQuery::insertEx_Std_RecQuery($model));
        }

        //集計項目
        $ctrl_name = array("0112" => "SEM1_INTR_VALUE"
                          ,"0122" => "SEM1_TERM_VALUE"
                          ,"0212" => "SEM2_INTR_VALUE"
                          ,"0222" => "SEM2_TERM_VALUE"
                          ,"0882" => "GRAD_VALUE2");
        $term_data = array();

        //データ配列
        $sch_array = array();
        $class_date = array();
        $name_date = array();
        $sem1_intr_val = array();
        $sem1_term_val = array();
        $sem2_intr_val = array();
        $sem2_term_val = array();
        $grad_val2 = array();
        $grad_val = array();
        $comp_credit = array();
        $get_credit = array();
        $remark = array();

        //成績データのリスト
        $result  = $db->query(knjm430mQuery::GetRecordDatdata($model));

        //件数カウント用初期化
        $ca = 0;        //データ全件数
        $model->schregInfo = array();
        while ($row_array = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $ca++;

            //学籍番号
            $sch_array[$ca] = $row_array["SCHREGNO"];
            $model->schregInfo[$row_array["SCHREGNO"]]["SEM_PASS_FLG"][1] = $row_array["SEM_PASS_FLG1"];
            $model->schregInfo[$row_array["SCHREGNO"]]["SEM_PASS_FLG"][2] = $row_array["SEM_PASS_FLG2"];
            $model->schregInfo[$row_array["SCHREGNO"]]["SEM_PASS_FLG"][3] = $row_array["SEM_PASS_FLG3"];
            $model->schregInfo[$row_array["SCHREGNO"]]["SEM_PASS_FLG_RECORD"][1] = $row_array["SEM_PASS_FLG_RECORD1"];
            $model->schregInfo[$row_array["SCHREGNO"]]["SEM_PASS_FLG_RECORD"][2] = $row_array["SEM_PASS_FLG_RECORD2"];
            $model->schregInfo[$row_array["SCHREGNO"]]["SEM_PASS_FLG_RECORD"][3] = $row_array["SEM_PASS_FLG_RECORD3"];
            //学年、組、番号
            //$class_date[$ca] = $row_array["GRADE"] . "-". $row_array["HR_CLASS"] . "-" . $row_array["ATTENDNO"];
            $class_date[$ca] = $row_array["HR_NAME"] . "-" . $row_array["ATTENDNO"];
            //生徒氏名
            $name_date[$ca] = $row_array["NAME_SHOW"];

            //前期試験
            $sem1_intr_val[$ca] = $row_array["SEM1_INTR_VALUE"];
            //前期補充指導点
            $sem1_term_val[$ca] = $row_array["SEM1_TERM_VALUE"];
            //後期試験
            $sem2_intr_val[$ca] = $row_array["SEM2_INTR_VALUE"];
            //後期補充指導点
            $sem2_term_val[$ca] = $row_array["SEM2_TERM_VALUE"];
            //学年成績
            $grad_val2[$ca] = $row_array["GRAD_VALUE2"];

            //評定
            $grad_val[$ca] = $row_array["GRAD_VALUE"];
            //RECORD_DATの履修単位数
            $comp_credit[$ca] = $row_array["COMP_CREDIT"];
            //RECORD_DATの修得単位数
            $get_credit[$ca] = $row_array["GET_CREDIT"];

            //備考
            $remark[$ca] = $row_array["REMARK"];

            //集計項目
            foreach ($ctrl_name as $code => $col) {
                if (is_numeric($row_array[$col])) {
                   $term_data[$col][] = $row_array[$col];
                }
            }
        }
        $result->free();

        //成績レコードが存在する場合
        if ($ca != 0) {

            //ページの１行目のカレント行番号（配列指標）
            if ($model->line == "") {
                $currentline = 1;
            } else {
                if ($model->cmd == "change") {
                    $currentline = 1;
                } else {
                    $currentline = $model->line;
                }
            }


            //500件表示ループ
            $counts = 1;                            //カレントページ内での行数
            $pageline = 0;                          //このページの最後の行の全件数の中での行数
            $lineall = $currentline + 500;

            $colorFlg = false; //５行毎に背景色を変更

            for ($pageline=$currentline; $pageline<$lineall; $pageline++,$counts++) {

                //カレント行が全件数を超えたらループ終り
                if ($pageline > $ca) {
                    break;
                }

                //学籍番号
                $row["SCHREGNO"] = $sch_array[$pageline];
                knjCreateHidden($objForm, "SCHREGNO_".$counts, $sch_array[$pageline]);

                //クラス番号（学年－クラス－出席番号）
                $row["GRA_HR_ATTEND"] =  $class_date[$pageline];

                //生徒氏名
                $row["NAME_SHOW"] =  $name_date[$pageline];

                //前期試験
                if (in_array("0112",$opt_adm) && $model->schregInfo[$row["SCHREGNO"]]["SEM_PASS_FLG"][1] == "1") {
                    //テキストボックス
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "SEM1_INTR_VALUE_".$counts,
                                        "size"      => 3,
                                        "maxlength" => 5,
                                        "value"     => $sem1_intr_val[$pageline],
                                        "extrahtml" => "STYLE=\"text-align: right\" onblur=\"check(this)\""));
                    $row["SEM1_INTR_VALUE"] = $objForm->ge("SEM1_INTR_VALUE_".$counts);
                } else {
                    //表示
                    $row["SEM1_INTR_VALUE"] = $sem1_intr_val[$pageline];
                    knjCreateHidden($objForm, "SEM1_INTR_VALUE_".$counts, $sem1_intr_val[$pageline]);
                }

                //前期補充指導点
                if (in_array("0122",$opt_adm) && strlen($sem1_intr_val[$pageline]) && $sem1_intr_val[$pageline] < 40 && $model->schregInfo[$row["SCHREGNO"]]["SEM_PASS_FLG"][1] == "1") {
                    //テキストボックス
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "SEM1_TERM_VALUE_".$counts,
                                        "size"      => 3,
                                        "maxlength" => 5,
                                        "value"     => $sem1_term_val[$pageline],
                                        "extrahtml" => "STYLE=\"text-align: right\" onblur=\"checkTerm(this, '{$counts}')\""));
                    $row["SEM1_TERM_VALUE"] = $objForm->ge("SEM1_TERM_VALUE_".$counts);
                } else {
                    //表示
                    $row["SEM1_TERM_VALUE"] = $sem1_term_val[$pageline];
                    knjCreateHidden($objForm, "SEM1_TERM_VALUE_".$counts, $sem1_term_val[$pageline]);
                }

                //後期試験
                if (in_array("0212",$opt_adm) && $model->schregInfo[$row["SCHREGNO"]]["SEM_PASS_FLG"][2] == "1") {
                    //テキストボックス
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "SEM2_INTR_VALUE_".$counts,
                                        "size"      => 3,
                                        "maxlength" => 5,
                                        "value"     => $sem2_intr_val[$pageline],
                                        "extrahtml" => "STYLE=\"text-align: right\" onblur=\"check(this)\""));
                    $row["SEM2_INTR_VALUE"] = $objForm->ge("SEM2_INTR_VALUE_".$counts);
                } else {
                    //表示
                    $row["SEM2_INTR_VALUE"] = $sem2_intr_val[$pageline];
                    knjCreateHidden($objForm, "SEM2_INTR_VALUE_".$counts, $sem2_intr_val[$pageline]);
                }

                //後期補充指導点
                if (in_array("0222",$opt_adm) && strlen($sem2_intr_val[$pageline]) && $sem2_intr_val[$pageline] < 40 && $model->schregInfo[$row["SCHREGNO"]]["SEM_PASS_FLG"][2] == "1") {
                    //テキストボックス
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "SEM2_TERM_VALUE_".$counts,
                                        "size"      => 3,
                                        "maxlength" => 5,
                                        "value"     => $sem2_term_val[$pageline],
                                        "extrahtml" => "STYLE=\"text-align: right\" onblur=\"checkTerm(this, '{$counts}')\""));
                    $row["SEM2_TERM_VALUE"] = $objForm->ge("SEM2_TERM_VALUE_".$counts);
                } else {
                    //表示
                    $row["SEM2_TERM_VALUE"] = $sem2_term_val[$pageline];
                    knjCreateHidden($objForm, "SEM2_TERM_VALUE_".$counts, $sem2_term_val[$pageline]);
                }

                //学年成績
                if (in_array("0882",$opt_adm)) {
                    //テキストボックス
                    $objForm->ae( array("type"      => "text",
                                        "name"      => "GRAD_VALUE2_".$counts,
                                        "size"      => 3,
                                        "maxlength" => 5,
                                        "value"     => $grad_val2[$pageline],
                                        "extrahtml" => "STYLE=\"text-align: right\" onblur=\"check(this)\""));
                    $row["GRAD_VALUE2"] = $objForm->ge("GRAD_VALUE2_".$counts);
                } else {
                    //表示
                    $row["GRAD_VALUE2"] = $grad_val2[$pageline];
                    knjCreateHidden($objForm, "GRAD_VALUE2_".$counts, $grad_val2[$pageline]);
                }

                //評定（表示）
                $row["GRAD_VALUE"] = $grad_val[$pageline];
                //履修単位（表示）
                $row["COMP_CREDIT"] =  $comp_credit[$pageline];
                //修得単位（表示）
                $row["GET_CREDIT"] =  $get_credit[$pageline];

                //備考・テキストボックス
                $objForm->ae( array("type"      => "text",
                                    "name"      => "REMARK_".$counts,
                                    "size"      => 20,
                                    "maxlength" => 20,
                                    "value"     => $remark[$pageline],
                                    "extrahtml" => "STYLE=\"WIDTH:95%\" WIDTH=\"95%\""));
                $row["REMARK"] = $objForm->ge("REMARK_".$counts);

                //５行毎に背景色を変更
                if ($counts % 5 == 1) {
                    $colorFlg = !$colorFlg;
                }
                $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

                $arg["data"][] = $row;
            }
        } else {    //成績データが存在しない場合（最初に開いた時も）
            $currentline = 0;
        }

        //平均・最高点・最低点表示
        if ($ca != 0) {
            foreach ($ctrl_name as $code => $col) {
                if (isset($term_data[$col])) {
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
        }


        //BUTTON***************************************************************************************

        //件数表示
        if ($ca == 0) {
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
        if ($ca != 0) {
            if ($currentline != 1) {
                $arg["btn_pre"]  = $objForm->ge("btn_pre");
            }
            if ($pageline < $ca) {
                $arg["btn_next"] = $objForm->ge("btn_next");
            }
        }

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "admin", implode(",",$opt_adm));
        knjCreateHidden($objForm, "line", $currentline);
        knjCreateHidden($objForm, "linecounts", --$counts);

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);


        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjm430mindex.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjm430mForm1.html", $arg); 
    }
}
?>
