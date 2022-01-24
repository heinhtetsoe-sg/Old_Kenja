<?php

require_once('for_php7.php');

class knjz350kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz350kindex.php", "", "sel");
        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $opt[0] = array("label" => $model->year,"value" => $model->year);

        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }

        //(左)入力可テキストボックス表示データ取得
        $result      = $db->query(knjz350kQuery::selectLeftQuery($model->year, $model));
        $opt_left1 = $opt_left2 = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["CONTROL_FLG"] == "1") {
                $opt_left1[] = array("label" => $row["NAME1"],
                                     "value" => $row["NAMECD2"]);
            } else if ($row["CONTROL_FLG"] == "2") {
                $opt_left2[] = array("label" => $row["NAME1"],
                                     "value" => $row["NAMECD2"]);
            }
        }
        //(右)入力不可テキストボックス表示データ取得
        $opt_right1 = $opt_right2 = array();
        $result = $db->query(knjz350kQuery::selectRightQuery($model->year, $model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD1"] == "Z004") {
                $opt_right1[] = array("label" => $row["NAME1"],
                                      "value" => $row["NAMECD2"]);
            } else if ($row["NAMECD1"] == $setNameCd) {
                $opt_right2[] = array("label" => $row["NAME1"],
                                      "value" => $row["NAMECD2"]);
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
        //実力テスト
        if ($model->Properties["useProficiency"] == "1") {
            $arg["useProficiency"] = "1";
            $opt_left = $opt_right = array();
            $result = $db->query(knjz350kQuery::selectProficiencyQuery($model->year));
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
            $arg["main_part3"]["LEFT_PART"]   = knjCreateCombo($objForm, "proficiency_input", "right", $opt_left, $extra, 15);
            //入力不可
            $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','proficiency_input','proficiency_delete',1)\"";
            $arg["main_part3"]["RIGHT_PART"]  = knjCreateCombo($objForm, "proficiency_delete", "left", $opt_right, $extra, 15);
            //各種ボタン
            $arg["main_part3"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", "onclick=\"return move('sel_add_all','proficiency_input','proficiency_delete',1);\"");
            $arg["main_part3"]["SEL_ADD"]     = knjCreateBtn($objForm, "sel_add", "＜", "onclick=\"return move('left','proficiency_input','proficiency_delete',1);\"");
            $arg["main_part3"]["SEL_DEL"]     = knjCreateBtn($objForm, "sel_del", "＞", "onclick=\"return move('right','proficiency_input','proficiency_delete',1);\"");
            $arg["main_part3"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", "onclick=\"return move('sel_del_all','proficiency_input','proficiency_delete',1);\"");
        }

        //初期化
        if ($model->cmd == "") unset($model->kansansaki);

        //表示切替（仮評定あり && 熊本県）
        if ($model->Properties["useProvFlg"] == "1" && $model->isKumamoto == true) {
            $arg["isKumamoto"] = 1;
        }

        //データ取得
        $d073 = $db->getRow(knjz350kQuery::getNameMst("D073", "01"), DB_FETCHMODE_ASSOC);

        //学年成績からの評定換算先ラジオボタン 1:学年評定 2:仮評定
        $opt_kansansaki = array(1, 2);
        $model->kansansaki = ($model->kansansaki == "") ? ($d073["NAMESPARE1"] == "1" ? "2" : "1") : $model->kansansaki;
        $extra = array("id=\"KANSANSAKI1\"", "id=\"KANSANSAKI2\"");
        $radioArray = knjCreateRadio($objForm, "KANSANSAKI", $model->kansansaki, $extra, $opt_kansansaki, get_count($opt_kansansaki));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

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
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");
        knjCreateHidden($objForm, "selectdata3");
        knjCreateHidden($objForm, "useProficiency", $model->Properties["useProficiency"]);

        
        $arg["info"]    = array("TOP"        => "対象年度：",
                                "LEFT_LIST"  => "入力可",
                                "RIGHT_LIST" => "入力不可");
        
        $arg["TITLE"]   = "マスタメンテナンス - 管理者コントロール";

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz350kForm1.html", $arg);
    }
}
?>
