<?php

require_once('for_php7.php');

class knjz370Form2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz370index.php", "", "sel");

        $db = Query::dbCheckOut();

        //リスト選択データ取得
        $ChosenData = array();
        if (!isset($model->warning) && isset($model->course_seq)) {
            $ChosenData = $db->getRow(knjz370Query::getEditQuery($model->select_grade ,$model->course_seq), DB_FETCHMODE_ASSOC);
        } else {
            $ChosenData =& $model->field;
        }
        //リスト選択データのグループクラス
        $opt_left = array();
        $result = $db->query(knjz370Query::getGroupQuery($model->select_grade, $model->course_seq));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_left[] = array("label" => $row["HR_NAMEABBV"],
                                "value" => $row["HR_CLASS"]);
        }
        //左画面学年コンボの学年クラス一覧
        $opt_right = array();
        $result = $db->query(knjz370Query::getClassQuery($model->select_grade, $model->course_seq));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_right[] = array("label" => $row["HR_NAMEABBV"],
                                 "value" => $row["HR_CLASS"]);
        }
        Query::dbCheckIn($db);

        //グループコード
        $objForm->ae( array("type"        => "text",
                            "name"        => "COURSE_SEQ",
                            "size"        => "5",
                            "maxlength"   => "4",
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $ChosenData["COURSE_SEQ"] ));
        $arg["COURSE_SEQ"] = $objForm->ge("COURSE_SEQ");
        //グループ名
        $objForm->ae( array("type"        => "text",
                            "name"        => "GROUP_NAME",
                            "size"        => "30",
                            "maxlength"   => "30",
                            "value"       => $ChosenData["GROUP_NAME"] ));
        $arg["GROUP_NAME"] = $objForm->ge("GROUP_NAME");

        //グループクラス(受講クラス)
        $objForm->ae( array("type"        => "select",
                            "name"        => "grouplist",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right', 'grouplist', 'classlist', 1)\"",
                            "options"     => $opt_left));
        //グループクラス(クラス一覧)
        $objForm->ae( array("type"        => "select",
                            "name"        => "classlist",
                            "size"        => "20",
                            "value"       => "right",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left', 'grouplist', 'classlist', 1)\"",
                            "options"     => $opt_right));

        //全追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all', 'grouplist', 'classlist', 1);\"" ) );
        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left', 'grouplist', 'classlist', 1);\"" ) );
        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right', 'grouplist', 'classlist', 1);\"" ) );
        //全削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all', 'grouplist', 'classlist', 1);\"" ) );
        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_delete",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("grouplist"),
                                   "RIGHT_PART"  => $objForm->ge("classlist"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all") );
        $arg["button"] = array("BTN_ADD"    =>$objForm->ge("btn_add"),
                               "BTN_KEEP"   =>$objForm->ge("btn_keep"),
                               "BTN_DELETE" =>$objForm->ge("btn_delete"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectvalue" ) );

        $arg["finish"]  = $objForm->get_finish();

        //更新できたら左のリストを再読込
        if (isset($model->message)) {
            $arg["reload"] = "parent.left_frame.location.href='knjz370index.php?cmd=list&COURSE_SEQ=" . $model->course_seq . "';";
        }

        View::toHTML($model, "knjz370Form2.html", $arg);
    }
}
?>
