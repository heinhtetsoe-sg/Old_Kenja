<?php

require_once('for_php7.php');

class knjz030jForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz030jindex.php", "", "edit");


        //メッセージを表示した場合
        if(isset($model->warning) | isset($model->message)){
            $model->desirediv = $model->field["DESIREDIV"];
            $model->testdiv = $model->field["TESTDIV"];
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = knjz030jQuery::getRow($model);
        }else{
            $Row =& $model->field;
        }

        $db     = Query::dbCheckOut();

        //年度設定
        $opt_year    = $db->getOne(knjz030jQuery::selectYearQuery());
        if (!isset($model->examyear) && (CTRL_YEAR+1 == $opt_year)) {
            $model->examyear = CTRL_YEAR+1;
        } else if (!isset($model->examyear)){
            $model->examyear = $opt_year;
        } 

        //入試区分コンボ
        $flg = "";
        $opt_testdiv = array();
        $opt_testdiv[] = array("label" => "", "value" => "");
        $result    = $db->query(knjz030jQuery::selectTestdiv($model->examyear));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");
            $opt_testdiv[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                                        "value" => $row["NAMECD2"]);
            if ($model->testdiv == $row["NAMECD2"]) $flg = true;
        }
        if (!$flg) $model->testdiv = $opt_testdiv[0]["value"];

        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTDIV",
                            "size"        => "1",
                            "value"       => $model->testdiv,
                            "extrahtml"   => "onchange=\"return btn_submit('edit');\"",
                            "options"     => $opt_testdiv) );
        $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //志望区分
        $objForm->ae( array("type"        => "text",
                            "name"        => "DESIREDIV",
                            "size"        => 1,
                            "maxlength"   => 1,
                            "extrahtml"   => "onkeypress=\"btn_keypress();\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => $model->desirediv) );
        $arg["data"]["DESIREDIV"] = $objForm->ge("DESIREDIV");

        //選択コース一覧
        $result = $db->query(knjz030jQuery::getChCourse($model));
        $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => htmlspecialchars(($row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]."：".$row["EXAMCOURSE_NAME"]."(".$row["EXAMCOURSE_MARK"].")")),
                                "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "L_COURSE",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_left));

        //コース一覧
        $result = $db->query(knjz030jQuery::getCourse($model));
        $opt_right = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_right[] = array("label" => htmlspecialchars(($row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]."：".$row["EXAMCOURSE_NAME"]."(".$row["EXAMCOURSE_MARK"].")")),
                                 "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "R_COURSE",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                            "options"     => $opt_right));

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all');\"" ) );
        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left');\"" ) );
        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right');\"" ) );
        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all');\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("L_COURSE"),
                                   "RIGHT_PART"  => $objForm->ge("R_COURSE"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return doSubmit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return doSubmit('reset')\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata" ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->examyear
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjz030jindex.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz030jForm2.html", $arg);
    }
}
?>
