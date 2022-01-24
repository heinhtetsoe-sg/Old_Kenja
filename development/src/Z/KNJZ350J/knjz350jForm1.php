<?php

require_once('for_php7.php');

class knjz350jForm1
{
    function main(&$model)
    {
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz350jindex.php", "", "sel");
        $db             = Query::dbCheckOut();

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjz350jQuery::getNameMstA023($model);
            $extra = "onChange=\"btn_submit('sel')\";";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->setSchoolKind, $extra, 1, "");
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $model->setSchoolKind = SCHOOLKIND;
        }

        //年度設定
        $opt[0] = array("label" => $model->year,"value" => $model->year);

        $setNameCd = "Z005";
        if ($model->setSchoolKind != "") {
            $setNameCd = "Z".$model->setSchoolKind."05";
        }
        $setNameCd2 = "Z009";
        if ($model->setSchoolKind != "") {
            $setNameCd2 = "Z".$model->setSchoolKind."09";
        }

        //(左)入力可テキストボックス表示データ取得
        $result      = $db->query(knjz350jQuery::selectLeftQuery($model->year, $model));
        $opt_left1 = $opt_left2 = $opt_left3 = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["CONTROL_FLG"] == "1") {
                $opt_left1[] = array("label" => $row["NAME1"],
                                     "value" => $row["NAMECD2"]);
            } else if ($row["CONTROL_FLG"] == "2") {
                $opt_left2[] = array("label" => $row["NAME1"],
                                     "value" => $row["NAMECD2"]);
            } else if ($row["CONTROL_FLG"] == "3") {
                $opt_left3[] = array("label" => $row["NAME1"],
                                     "value" => $row["NAMECD2"]);
            }
        }
        //(右)入力不可テキストボックス表示データ取得
        $opt_right1 = $opt_right2 = $opt_right3 = array();
        $result = $db->query(knjz350jQuery::selectRightQuery($model->year, $model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD1"] == "Z004") {
                $opt_right1[] = array("label" => $row["NAME1"],
                                      "value" => $row["NAMECD2"]);
            } else if ($row["NAMECD1"] == $setNameCd) {
                $opt_right2[] = array("label" => $row["NAME1"],
                                      "value" => $row["NAMECD2"]);
            } else if ($row["NAMECD1"] == $setNameCd2) {
                $opt_right3[] = array("label" => $row["NAME1"],
                                      "value" => $row["NAMECD2"]);
            }
        }


        
        $result->free();
        Query::dbCheckIn($db);

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
        //観点入力可
        $objForm->ae( array("type"        => "select",
                            "name"        => "jview_input",
                            "size"        => "10",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','jview_input','jview_delete',1)\"",
                            "options"     => $opt_left3));
        //観点入力不可
        $objForm->ae( array("type"        => "select",
                            "name"        => "jview_delete",
                            "size"        => "10",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','jview_input','jview_delete',1)\"",
                            "options"     => $opt_right3));
        
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
        //観点側の移動ボタン
        //全追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all3",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','jview_input','jview_delete',1);\"" ) );
        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add3",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','jview_input','jview_delete',1);\"" ) );
        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del3",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','jview_input','jview_delete',1);\"" ) );
        //全削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all3",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','jview_input','jview_delete',1);\"" ) );
        
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
        $arg["main_part3"] = array( "LEFT_PART"   => $objForm->ge("jview_input"),
                                    "RIGHT_PART"  => $objForm->ge("jview_delete"),
                                    "SEL_ADD_ALL" => $objForm->ge("sel_add_all3"),
                                    "SEL_ADD"     => $objForm->ge("sel_add3"),
                                    "SEL_DEL"     => $objForm->ge("sel_del3"),
                                    "SEL_DEL_ALL" => $objForm->ge("sel_del_all3"));
        
        //更新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );
        
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
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));
        
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata2"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata3"
                            ) );
        
        $arg["info"]    = array("TOP"        => "対象年度：",
                                "LEFT_LIST"  => "入力可",
                                "RIGHT_LIST" => "入力不可");
        
        $arg["TITLE"]   = "マスタメンテナンス - 管理者コントロール";
        $arg["finish"]  = $objForm->get_finish();
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz350jForm1.html", $arg);
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
