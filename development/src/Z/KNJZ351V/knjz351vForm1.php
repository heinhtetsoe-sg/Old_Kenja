<?php

require_once('for_php7.php');

class knjz351vForm1
{
    function main(&$model)
    {
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz351vindex.php", "", "sel");
        $db             = Query::dbCheckOut();

        //年度設定
        $opt[0] = array("label" => $model->year, "value" => $model->year);

        //科目コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjz351vQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "", $model);

        //算出先コンボ
        $extra = "onChange=\"btn_submit('edit')\";";
        $query = knjz351vQuery::selectListQuery($db, $model);
        makeCmb($objForm, $arg, $db, $query, "SAKI_TESTCD", $model->field["SAKI_TESTCD"], $extra, 1, "", $model);

        //テキストボックス表示データ取得(算出元)
        $opt_left1 = $opt_right1 = array();
        $result    = $db->query(knjz351vQuery::selectListQuery($db, $model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //算出先は表示しない(仕様確定)
            if ($model->field["SAKI_TESTCD"] == $row["VALUE"]) continue;
            //過去のみ表示する。(仕様未確定・・・たぶん確定になると思うので、このままにしておく)
            if ($model->field["SAKI_TESTCD"] <= $row["VALUE"]) continue;
            //各学期は各学期のみ表示する。学年末は全て表示する。(仕様未確定・・・保留)
            //if (substr($model->field["SAKI_TESTCD"], 0, 1) != "9" && substr($model->field["SAKI_TESTCD"], 0, 1) != substr($row["VALUE"], 0, 1)) continue;

            //算出元(対象)
            if ($row["CALC_FLG"] == "1") {
                $opt_left1[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            //パーツ(候補)
            } else {
                $opt_right1[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
            }
        }
        $result->free();

        //DB切断
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

        $arg["main_part1"] = array( "LEFT_PART"   => $objForm->ge("grade_input"),
                                    "RIGHT_PART"  => $objForm->ge("grade_delete"),
                                    "SEL_ADD_ALL" => $objForm->ge("sel_add_all1"),
                                    "SEL_ADD"     => $objForm->ge("sel_add1"),
                                    "SEL_DEL"     => $objForm->ge("sel_del1"),
                                    "SEL_DEL_ALL" => $objForm->ge("sel_del_all1"));
        
        //更新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit('update');\"" ) );
        
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

        $arg["info"]    = array("TOP"        => "対象年度：",
                                "LEFT_LIST"  => "対象一覧",
                                "RIGHT_LIST" => "候補一覧");
        
        $arg["TITLE"]   = "マスタメンテナンス - 管理者コントロール";
        $arg["finish"]  = $objForm->get_finish();
        
        if (VARS::get("cmd") != "edit" && $model->cmd != 'chenge'){
            $arg["reload"]  = "window.open('knjz351vindex.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz351vForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", &$model) {
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

    if ($name == "SAKI_TESTCD") {
        $value = ($value && $value_flg) ? $value : 9990008; //初期値は、開発当初、学年評価としておく
    } else {
        $value = ($value && $value_flg) ? $value : $model->getSubclasscd;//パラメータの初期値
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
