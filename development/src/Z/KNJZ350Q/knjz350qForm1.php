<?php

require_once('for_php7.php');

class knjz350qForm1
{
    function main(&$model)
    {
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz350qindex.php", "", "sel");
        $db             = Query::dbCheckOut();

        //年度設定
        $opt[0] = array("label" => $model->year, "value" => $model->year);

        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $setNameCd2 = "Z009";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd2 = "Z".SCHOOLKIND."09";
        }

        //課程学科コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjz350qQuery::getCourseMajor($model);
        makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->field["COURSE_MAJOR"], $extra, 1, "");

        //科目コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjz350qQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "");

        //(左)入力可テキストボックス表示データ取得
        $opt_left1 = $opt_left2 = array();
        $opt_right1 = $opt_right2 = array();
        $result    = $db->query(knjz350qQuery::selectListQuery($model));
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
        $value = "left";
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','grade_input','grade_delete',1)\"";
        $arg["main_part1"]["LEFT_PART"] = knjCreateCombo($objForm, "grade_input", $value, $opt_left1, $extra, 10);

        //成績入力不可
        $value = "right";
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','grade_input','grade_delete',1)\"";
        $arg["main_part1"]["RIGHT_PART"] = knjCreateCombo($objForm, "grade_delete", $value, $opt_right1, $extra, 10);

        //成績入力コントロール可
        $value = "left";
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','attend_input','attend_delete',1)\"";
        $arg["main_part2"]["LEFT_PART"] = knjCreateCombo($objForm, "attend_input", $value, $opt_left2, $extra, 15);

        //成績入力コントロール不可
        $value = "right";
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','attend_input','attend_delete',1)\"";
        $arg["main_part2"]["RIGHT_PART"] = knjCreateCombo($objForm, "attend_delete", $value, $opt_right2, $extra, 15);

        //成績側の移動ボタン
        //全追加ボタンを作成する
        $extra = "onclick=\"return move('sel_add_all','grade_input','grade_delete',1);\"";
        $arg["main_part1"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all1", "≪", $extra);

        //追加ボタンを作成する
        $extra = "onclick=\"return move('left','grade_input','grade_delete',1);\"";
        $arg["main_part1"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add1", "＜", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return move('right','grade_input','grade_delete',1);\"";
        $arg["main_part1"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del1", "＞", $extra);

        //全削除ボタンを作成する
        $extra = "onclick=\"return move('sel_del_all','grade_input','grade_delete',1);\"";
        $arg["main_part1"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all1", "≫", $extra);

        //出欠側の移動ボタン
        //全追加ボタンを作成する
        $extra = "onclick=\"return move('sel_add_all','attend_input','attend_delete',1);\"";
        $arg["main_part2"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all2", "≪", $extra);

        //追加ボタンを作成する
        $extra = "onclick=\"return move('left','attend_input','attend_delete',1);\"";
        $arg["main_part2"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add2", "＜", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return move('right','attend_input','attend_delete',1);\"";
        $arg["main_part2"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del2", "＞", $extra);

        //全削除ボタンを作成する
        $extra = "onclick=\"return move('sel_del_all','attend_input','attend_delete',1);\"";
        $arg["main_part2"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all2", "≫", $extra);

        //更新ボタンを作成する
        $extra = "onclick=\"return doSubmit('update');\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更 新", $extra);

        //更新ボタンを作成する
        $extra = "onclick=\"return doSubmit('update2');\"";
        $arg["button"]["BTN_OK2"] = knjCreateBtn($objForm, "btn_keep2", "更 新", $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //リンク先のURL
        $jump = REQUESTROOT."/Z/KNJZ351Q/knjz351qindex.php";
        $jump .= "?sendSubclassCd={$model->field["SUBCLASSCD"]}";
        //算出設定画面ボタン
        $extra = "onClick=\"openScreenCalc('{$jump}');\"";
        $arg["button"]["btn_calc"] = knjCreateBtn($objForm, "btn_calc", "算出設定画面", $extra);
        
//-------以下、出席コントロール、実力テストコントロールの処理-------------------------------------//

        //(左)入力可テキストボックス表示データ取得
        $result      = $db->query(knjz350qQuery::selectLeftQuery($model->year, $model));
        $opt_more_left1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["CONTROL_FLG"] == "2") {
                $opt_more_left1[] = array("label" => $row["NAME1"],
                                          "value" => $row["NAMECD2"]);
            }
        }

        //(右)入力不可テキストボックス表示データ取得
        $opt_more_right1 = array();
        $result = $db->query(knjz350qQuery::selectRightQuery($model->year, $model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
        $extra = "onclick=\"return doSubmit2();\"";
        $arg["button2"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep2", "更 新", $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button2"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear2", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button2"]["BTN_END"] = knjCreateBtn($objForm, "btn_end2", "終 了", $extra);

        //実力テスト
        if ($model->Properties["useProficiency"] == "1") {
            $arg["useProficiency"] = "1";
            $opt_left = $opt_right = array();
            $result = $db->query(knjz350qQuery::selectProficiencyQuery($model->year));
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
            $extra = "onclick=\"return doSubmit3();\"";
            $arg["button3"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep3", "更 新", $extra);

            //取消ボタンを作成する
            $extra = "onclick=\"return btn_submit('clear');\"";
            $arg["button3"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear3", "取 消", $extra);

            //終了ボタンを作成する
            $extra = "onclick=\"closeWin();\"";
            $arg["button3"]["BTN_END"] = knjCreateBtn($objForm, "btn_end3", "終 了", $extra);
        }

        //観点コントロール
        if ($model->Properties["useJviewControl"] == "1") {
            $arg["useJviewControl"] = "1";

            //(左)入力可テキストボックス表示データ取得
            $result      = $db->query(knjz350qQuery::selectLeftQuery($model->year, $model));
            $opt_jview_left1 = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["CONTROL_FLG"] == "3") {
                    $opt_jview_left1[] = array("label" => $row["NAME1"],
                                               "value" => $row["NAMECD2"]);
                }
            }
            //(右)入力不可テキストボックス表示データ取得
            $opt_jview_right1 = array();
            $result = $db->query(knjz350qQuery::selectRightQuery($model->year, $model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
            $result      = $db->query(knjz350qQuery::selectLeftQuery($model->year, $model));
            $opt_myp_dp_left1 = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["CONTROL_FLG"] == "4") {
                    $opt_myp_dp_left1[] = array("label" => $row["NAME1"],
                                                "value" => $row["NAMECD2"]);
                }
            }
            //(右)入力不可テキストボックス表示データ取得
            $opt_myp_dp_right1 = array();
            $result = $db->query(knjz350qQuery::selectRightQuery($model->year, $model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2Left");
        knjCreateHidden($objForm, "selectdata2Right");
        knjCreateHidden($objForm, "selectdata2");
        knjCreateHidden($objForm, "selectdata3");
        knjCreateHidden($objForm, "selectdataJview");
        knjCreateHidden($objForm, "selectdataMypDp");

        $arg["info"]["TOP"]        = "対象年度：";
        $arg["info"]["LEFT_LIST"]  = "入力可";
        $arg["info"]["RIGHT_LIST"] = "入力不可";
        $arg["TITLE"]              = "マスタメンテナンス - 管理者コントロール";

        $arg["finish"]  = $objForm->get_finish();

        //DB切断
        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz350qForm1.html", $arg);
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
