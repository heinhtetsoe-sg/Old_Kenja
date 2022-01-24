<?php

require_once('for_php7.php');

class knjz030rForm2
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz030rindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2") && $model->year && $model->applicantdiv && $model->testdiv && $model->desirediv){
            $query = knjz030rQuery::getRow($model->year, $model->applicantdiv, $model->testdiv, $model->desirediv);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjz030rQuery::getNameMst($model, $model->year, "L003");
        makeCombo($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "blank");

        //入試区分コンボ
        $query = knjz030rQuery::getNameMst($model, $model->year, "L004");
        makeCombo($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "blank");

        //志望区分テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value); btn_submit('edit');\" ";
        $arg["data"]["DESIREDIV"] = knjCreateTextBox($objForm, $Row["DESIREDIV"], "DESIREDIV", 1, 1, $extra);

        //選択コース一覧
        $result = $db->query(knjz030rQuery::getListCourse($model, 'Llist', $Row["APPLICANTDIV"], $Row["TESTDIV"], $Row["DESIREDIV"]));
        $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
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
        $result = $db->query(knjz030rQuery::getListCourse($model, 'Rlist', $Row["APPLICANTDIV"], $Row["TESTDIV"], $Row["DESIREDIV"]));
        $opt_right = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
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
            $arg["reload"]  = "window.open('knjz030rindex.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz030rForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = false) {
    $opt = array();
    $value_flg = false;
    if ($blank == "blank") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
