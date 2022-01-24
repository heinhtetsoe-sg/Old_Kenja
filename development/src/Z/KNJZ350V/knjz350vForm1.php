<?php

require_once('for_php7.php');

class knjz350vForm1
{
    function main(&$model)
    {
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz350vindex.php", "", "sel");
        $db             = Query::dbCheckOut();

        //年度設定
        $opt[0] = array("label" => $model->year, "value" => $model->year);

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjz350vQuery::getNameMstA023($model);
            $extra = "onChange=\"btn_submit('subclasscd')\";";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->setSchoolKind, $extra, 1, "");
        } else {
            $model->setSchoolKind = SCHOOLKIND;
        }

        //学校種別(H:高校 J:中学 P:小学)
        $model->schoolKindArray = array();
        $query = knjz350vQuery::getNameMstA023($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($row["VALUE"] != $model->setSchoolKind) {
                    continue;
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                if ($row["VALUE"] != SCHOOLKIND) {
                    continue;
                }
            }
            $model->schoolKindArray[$row["VALUE"]] = "00-" . $row["VALUE"] . "-00-000000";
        }
        $result->free();

        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->setSchoolKind."05";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $setNameCd2 = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd2 = "Z".$model->setSchoolKind."09";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd2 = "Z".SCHOOLKIND."09";
        }

        //科目コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjz350vQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "");

        //(左)入力可テキストボックス表示データ取得
        $opt_left1 = $opt_left2 = array();
        $opt_right1 = $opt_right2 = array();
        $result    = $db->query(knjz350vQuery::selectListQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //パーツ
            if ($row["PARTS_FLG"] == "1") {
                $opt_left1[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            //テスト
            } else {
                $opt_right1[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            }
            //管理者コントロール
            if ($row["CONTROL_FLG"] == "1") {
                $opt_left2[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            //パーツ
            } else if ($row["PARTS_FLG"] == "1") {
                $opt_right2[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            }
        }
        $result->free();

        $arg["year"] = array( "VAL"       => $model->year );

        //成績入力可
        $objForm->ae( array("type"        => "select",
                            "name"        => "grade_input",
                            "size"        => "10",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','grade_input','grade_delete',1)\"",
                            "options"     => $opt_left1));
        //成績入力不可
        $objForm->ae( array("type"        => "select",
                            "name"        => "grade_delete",
                            "size"        => "10",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','grade_input','grade_delete',1)\"",
                            "options"     => $opt_right1));
        //出欠入力可
        $objForm->ae( array("type"        => "select",
                            "name"        => "attend_input",
                            "size"        => "15",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','attend_input','attend_delete',1)\"",
                            "options"     => $opt_left2));
        //出欠入力不可
        $objForm->ae( array("type"        => "select",
                            "name"        => "attend_delete",
                            "size"        => "15",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','attend_input','attend_delete',1)\"",
                            "options"     => $opt_right2));
        
        //成績側の移動ボタン
        //全追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all1",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','grade_input','grade_delete',1);\"" ) );
        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add1",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','grade_input','grade_delete',1);\"" ) );
        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del1",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','grade_input','grade_delete',1);\"" ) );
        //全削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all1",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','grade_input','grade_delete',1);\"" ) );
        //出欠側の移動ボタン
        //全追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all2",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','attend_input','attend_delete',1);\"" ) );
        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add2",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','attend_input','attend_delete',1);\"" ) );
        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del2",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','attend_input','attend_delete',1);\"" ) );
        //全削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all2",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','attend_input','attend_delete',1);\"" ) );

        $arg["main_part1"] = array( "LEFT_PART"   => $objForm->ge("grade_input"),
                                    "RIGHT_PART"  => $objForm->ge("grade_delete"),
                                    "SEL_ADD_ALL" => $objForm->ge("sel_add_all1"),
                                    "SEL_ADD"     => $objForm->ge("sel_add1"),
                                    "SEL_DEL"     => $objForm->ge("sel_del1"),
                                    "SEL_DEL_ALL" => $objForm->ge("sel_del_all1"));
        $arg["main_part2"] = array( "LEFT_PART"   => $objForm->ge("attend_input"),
                                    "RIGHT_PART"  => $objForm->ge("attend_delete"),
                                    "SEL_ADD_ALL" => $objForm->ge("sel_add_all2"),
                                    "SEL_ADD"     => $objForm->ge("sel_add2"),
                                    "SEL_DEL"     => $objForm->ge("sel_del2"),
                                    "SEL_DEL_ALL" => $objForm->ge("sel_del_all2"));
        
        //更新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit('update');\"" ) );
        //更新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep2",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit('update2');\"" ) );
        
        //取消ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );
        
        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        
        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_OK2"    =>$objForm->ge("btn_keep2"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));
        
        //リンク先のURL
        $jump = REQUESTROOT."/Z/KNJZ351V/knjz351vindex.php";
        $jump .= "?sendSubclassCd={$model->field["SUBCLASSCD"]}&SEND_selectSchoolKind={$model->selectSchoolKind}&SEND_schoolKind={$model->setSchoolKind}";
        //算出設定画面ボタン
        $extra = "onClick=\"openScreenCalc('{$jump}');\"";
        $arg["button"]["btn_calc"] = knjCreateBtn($objForm, "btn_calc", "算出設定画面", $extra);
        
        //$jump2 = REQUESTROOT."/Z/KNJZ350V_2/knjz350v_2index.php";
        //$jump2 .= "?sendSubclassCd={$model->field["SUBCLASSCD"]}&SEND_selectSchoolKind={$model->selectSchoolKind}&SEND_schoolKind={$model->setSchoolKind}";
        ////評定換算設定画面ボタン
        //$extra = "onClick=\"openScreenCalc('{$jump2}');\"";
        //$arg["button"]["btn_gaisan"] = knjCreateBtn($objForm, "btn_gaisan", "評定換算設定画面", $extra);

//-------以下、出席コントロール、実力テストコントロールの処理-------------------------------------//

        //(左)入力可テキストボックス表示データ取得
        $result      = $db->query(knjz350vQuery::selectLeftQuery($model->year, $model));
        $opt_more_left1 = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["CONTROL_FLG"] == "2") {
                $opt_more_left1[] = array("label" => $row["NAME1"],
                                          "value" => $row["NAMECD2"]);
            }
        }
        //(右)入力不可テキストボックス表示データ取得
        $opt_more_right1 = array();
        $result = $db->query(knjz350vQuery::selectRightQuery($model->year, $model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD1"] == $setNameCd) {
                $opt_more_right1[] = array("label" => $row["NAME1"],
                                           "value" => $row["NAMECD2"]);
            }
        }
        $result->free();
        
        //入力可
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','attend_more_input','attend_more_delete',1)\"";
        $arg["main_part3"]["LEFT_PART"]   = knjCreateCombo($objForm, "attend_more_input", "right", $opt_more_left1, $extra, 15);
        //入力不可
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','attend_more_input','attend_more_delete',1)\"";
        $arg["main_part3"]["RIGHT_PART"]  = knjCreateCombo($objForm, "attend_more_delete", "left", $opt_more_right1, $extra, 15);
        //各種ボタン
        $arg["main_part3"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all3", "≪", "onclick=\"return move('sel_add_all3','attend_more_input','attend_more_delete',1);\"");
        $arg["main_part3"]["SEL_ADD"]     = knjCreateBtn($objForm, "sel_add3", "＜", "onclick=\"return move('left','attend_more_input','attend_more_delete',1);\"");
        $arg["main_part3"]["SEL_DEL"]     = knjCreateBtn($objForm, "sel_del3", "＞", "onclick=\"return move('right','attend_more_input','attend_more_delete',1);\"");
        $arg["main_part3"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all3", "≫", "onclick=\"return move('sel_del_all3','attend_more_delete','attend_more_input',1);\"");
        
        //更新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep2",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit2();\"" ) );
        
        //取消ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear2",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );
        
        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end2",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        
        $arg["button2"] = array("BTN_OK"     =>$objForm->ge("btn_keep2"),
                                "BTN_CLEAR"  =>$objForm->ge("btn_clear2"),
                                "BTN_END"    =>$objForm->ge("btn_end2"));
        
        
        //実力テスト
        if ($model->Properties["useProficiency"] == "1") {
            $arg["useProficiency"] = "1";
            $opt_left = $opt_right = array();
            $result = $db->query(knjz350vQuery::selectProficiencyQuery($model->year));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["ADMIN_FLG"]) {
                    $opt_left[]  = array("label" => $row["PROFICIENCYDIV"].":".$row["PROFICIENCYCD"]."　".$row["PROFICIENCYNAME1"], 
                                         "value" => $row["PROFICIENCYDIV"].":".$row["PROFICIENCYCD"]);
                } else {
                    $opt_right[]  = array("label" => $row["PROFICIENCYDIV"].":".$row["PROFICIENCYCD"]."　".$row["PROFICIENCYNAME1"], 
                                          "value" => $row["PROFICIENCYDIV"].":".$row["PROFICIENCYCD"]);
                }
            }
            $result->free();

            //入力可
            $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','proficiency_input','proficiency_delete',1)\"";
            $arg["main_part4"]["LEFT_PART"]   = knjCreateCombo($objForm, "proficiency_input", "right", $opt_left, $extra, 15);
            //入力不可
            $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','proficiency_input','proficiency_delete',1)\"";
            $arg["main_part4"]["RIGHT_PART"]  = knjCreateCombo($objForm, "proficiency_delete", "left", $opt_right, $extra, 15);
            //各種ボタン
            $arg["main_part4"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", "onclick=\"return move('sel_add_all','proficiency_input','proficiency_delete',1);\"");
            $arg["main_part4"]["SEL_ADD"]     = knjCreateBtn($objForm, "sel_add", "＜", "onclick=\"return move('left','proficiency_input','proficiency_delete',1);\"");
            $arg["main_part4"]["SEL_DEL"]     = knjCreateBtn($objForm, "sel_del", "＞", "onclick=\"return move('right','proficiency_input','proficiency_delete',1);\"");
            $arg["main_part4"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", "onclick=\"return move('sel_del_all','proficiency_input','proficiency_delete',1);\"");
        
            //更新ボタンを作成する
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_keep3",
                                "value"       => "更 新",
                                "extrahtml"   => "onclick=\"return doSubmit3();\"" ) );
            
            //取消ボタンを作成する
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_clear3",
                                "value"       => "取 消",
                                "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );
            
            //終了ボタンを作成する
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_end3",
                                "value"       => "終 了",
                                "extrahtml"   => "onclick=\"closeWin();\"" ) );
            
            $arg["button3"] = array("BTN_OK"     =>$objForm->ge("btn_keep3"),
                                    "BTN_CLEAR"  =>$objForm->ge("btn_clear3"),
                                    "BTN_END"    =>$objForm->ge("btn_end3"));
        
        }
        
        //観点コントロール
        if ($model->Properties["useJviewControl"] == "1") {
            $arg["useJviewControl"] = "1";
            
            //(左)入力可テキストボックス表示データ取得
            $result      = $db->query(knjz350vQuery::selectLeftQuery($model->year, $model));
            $opt_jview_left1 = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($row["CONTROL_FLG"] == "3") {
                    $opt_jview_left1[] = array("label" => $row["NAME1"],
                                               "value" => $row["NAMECD2"]);
                }
            }
            //(右)入力不可テキストボックス表示データ取得
            $opt_jview_right1 = array();
            $result = $db->query(knjz350vQuery::selectRightQuery($model->year, $model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($row["NAMECD1"] == $setNameCd2) {
                    $opt_jview_right1[] = array("label" => $row["NAME1"],
                                                "value" => $row["NAMECD2"]);
                }
            }
            $result->free();

            //観点入力
            //入力可
            $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','jview_input','jview_delete',1)\"";
            $arg["main_part_jview"]["LEFT_PART"]   = knjCreateCombo($objForm, "jview_input", "right", $opt_jview_left1, $extra, 10);
            //入力不可
            $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','jview_input','jview_delete',1)\"";
            $arg["main_part_jview"]["RIGHT_PART"]  = knjCreateCombo($objForm, "jview_delete", "left", $opt_jview_right1, $extra, 10);
            //各種ボタン
            $arg["main_part_jview"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", "onclick=\"return move('sel_add_all','jview_input','jview_delete',1);\"");
            $arg["main_part_jview"]["SEL_ADD"]     = knjCreateBtn($objForm, "sel_add", "＜", "onclick=\"return move('left','jview_input','jview_delete',1);\"");
            $arg["main_part_jview"]["SEL_DEL"]     = knjCreateBtn($objForm, "sel_del", "＞", "onclick=\"return move('right','jview_input','jview_delete',1);\"");
            $arg["main_part_jview"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", "onclick=\"return move('sel_del_all','jview_input','jview_delete',1);\"");
            //更新ボタン
            $arg["button_jview"]["BTN_OK"] = knjCreateBtn($objForm, "btn_update_jview", "更 新", "onclick=\"return doSubmitJview();\"");
            $arg["button_jview"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear_jview", "取 消", "onclick=\"return btn_submit('clear');\"");
            $arg["button_jview"]["BTN_END"] = knjCreateBtn($objForm, "btn_end_jview", "終 了", "onclick=\"closeWin();\"");
        
        }

        //ＭＹＰ・ＤＰ成績コントロール
        if ($model->Properties["useIBRecordControl"] == "1") {
            $arg["useIBRecordControl"] = "1";
            
            //(左)入力可テキストボックス表示データ取得
            $result      = $db->query(knjz350vQuery::selectLeftQuery($model->year, $model));
            $opt_myp_dp_left1 = array();
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($row["CONTROL_FLG"] == "4") {
                    $opt_myp_dp_left1[] = array("label" => $row["NAME1"],
                                                "value" => $row["NAMECD2"]);
                }
            }
            //(右)入力不可テキストボックス表示データ取得
            $opt_myp_dp_right1 = array();
            $result = $db->query(knjz350vQuery::selectRightQuery($model->year, $model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($row["NAMECD1"] == "MYP_DP") {
                    $opt_myp_dp_right1[] = array("label" => $row["NAME1"],
                                                 "value" => $row["NAMECD2"]);
                }
            }
            $result->free();

            //ＭＹＰ・ＤＰ成績入力
            //入力可
            $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','myp_dp_input','myp_dp_delete',1)\"";
            $arg["main_part_myp_dp"]["LEFT_PART"]   = knjCreateCombo($objForm, "myp_dp_input", "right", $opt_myp_dp_left1, $extra, 10);
            //入力不可
            $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','myp_dp_input','myp_dp_delete',1)\"";
            $arg["main_part_myp_dp"]["RIGHT_PART"]  = knjCreateCombo($objForm, "myp_dp_delete", "left", $opt_myp_dp_right1, $extra, 10);
            //各種ボタン
            $arg["main_part_myp_dp"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", "onclick=\"return move('sel_add_all','myp_dp_input','myp_dp_delete',1);\"");
            $arg["main_part_myp_dp"]["SEL_ADD"]     = knjCreateBtn($objForm, "sel_add", "＜", "onclick=\"return move('left','myp_dp_input','myp_dp_delete',1);\"");
            $arg["main_part_myp_dp"]["SEL_DEL"]     = knjCreateBtn($objForm, "sel_del", "＞", "onclick=\"return move('right','myp_dp_input','myp_dp_delete',1);\"");
            $arg["main_part_myp_dp"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", "onclick=\"return move('sel_del_all','myp_dp_input','myp_dp_delete',1);\"");
            //更新ボタン
            $arg["button_myp_dp"]["BTN_OK"] = knjCreateBtn($objForm, "btn_update_myp_dp", "更 新", "onclick=\"return doSubmitMypDp();\"");
            $arg["button_myp_dp"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear_myp_dp", "取 消", "onclick=\"return btn_submit('clear');\"");
            $arg["button_myp_dp"]["BTN_END"] = knjCreateBtn($objForm, "btn_end_myp_dp", "終 了", "onclick=\"closeWin();\"");
        
        }

        //コピーボタンを作成する
        $extra = "style=\"width:130px\" onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
        
        //年間試験設定画面ボタンを作成する
        //年間試験設定画面表示のプロパティ
        if ($model->Properties["useKNJZ350_NENKAN_TESTITEM"] == '1') {
            $extra  = "style=\"width:130px\" onClick=\"wopen('".REQUESTROOT."/Z/KNJZ350_NENKAN_TESTITEM/knjz350_nenkan_testitemindex.php?";
            $extra .= "&cmd=&SEND_PRGID=KNJZ350V&SEND_selectSchoolKind={$model->selectSchoolKind}&SEND_schoolKind={$model->setSchoolKind}&SEND_AUTH=".$model->auth;
            $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_yeartest_settei"] = knjCreateBtn($objForm, "btn_yeartest_settei", "年間試験設定画面", $extra);
        }

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata2Left"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata2Right"
                            ) );
        
        knjCreateHidden($objForm, "selectdata2");
        knjCreateHidden($objForm, "selectdata3");
        knjCreateHidden($objForm, "selectdataJview");
        knjCreateHidden($objForm, "selectdataMypDp");

        $arg["info"]    = array("TOP"        => "対象年度：",
                                "LEFT_LIST"  => "入力可",
                                "RIGHT_LIST" => "入力不可");
        
        $arg["TITLE"]   = "マスタメンテナンス - 管理者コントロール";
        $arg["finish"]  = $objForm->get_finish();

        //DB切断
        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz350vForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
