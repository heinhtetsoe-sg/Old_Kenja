<?php

require_once('for_php7.php');

class knjz030kForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz030kindex.php", "", "edit");

        $db     = Query::dbCheckOut();

        //メッセージを表示した場合
        if(isset($model->warning) | isset($model->message)){
            $model->testdiv   = $model->field["TESTDIV"];
            $model->desirediv = $model->field["DESIREDIV"];
        }

        //試験区分コンボ
        $opt = array();
        $result = $db->query(knjz030kQuery::getTestdiv($model->examyear));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTDIV",
                            "size"        => "1",
                            "value"       => $model->testdiv,
                            "options"     => $opt) );
        $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //志望区分
        $objForm->ae( array("type"        => "text",
                            "name"        => "DESIREDIV",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onkeypress=\"btn_keypress();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->desirediv) );
        $arg["data"]["DESIREDIV"] = $objForm->ge("DESIREDIV");

        //選択コース一覧
#        $result = $db->query(knjz030kQuery::getChCourse($model));
        $result = $db->query(knjz030kQuery::getListCourse($model,'Llist'));
        $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
#            $opt_left[] = array("label" => htmlspecialchars(($row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]."：".$row["EXAMCOURSE_NAME"]."(".$row["EXAMCOURSE_MARK"].")")),
            $opt_left[] = array("label" => htmlspecialchars(($row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"].":".$row["EXAMCOURSE_NAME"])),
                                "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "L_COURSE",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_left));

        //コース一覧
#        $result = $db->query(knjz030kQuery::getCourse($model));
        $result = $db->query(knjz030kQuery::getListCourse($model,'Rlist'));
        $opt_right = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
#            $opt_right[] = array("label" => htmlspecialchars(($row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]."：".$row["EXAMCOURSE_NAME"]."(".$row["EXAMCOURSE_MARK"].")")),
            $opt_right[] = array("label" => htmlspecialchars(($row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"].":".$row["EXAMCOURSE_NAME"])),
                                 "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "R_COURSE",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                            "options"     => $opt_right));

        //追加削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all');\"" ) );
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left');\"" ) );
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right');\"" ) );
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

        //修正
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return doSubmit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //クリア
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return doSubmit('reset')\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd" ) );
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "selectdata" ) );

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjz030kindex.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz030kForm2.html", $arg);
    }
}
?>
