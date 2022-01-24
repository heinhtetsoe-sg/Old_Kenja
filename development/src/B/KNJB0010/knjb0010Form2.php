<?php

require_once('for_php7.php');

class knjb0010Form2
{
    public function main(&$model)
    {
        $objForm = new form();
        $db      = Query::dbCheckOut();

        $arg["start"] = $objForm->get_start("edit", "POST", "knjb0010index.php", "", "edit");

        $arg["GROUPCD"] = $model->groupcd;
        $arg["NAME"]    = $model->name;
        $chaircd = $option = array();

        $result = $db->query(knjb0010Query::getGroup($model, $model->groupcd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $lessoncnt = $row["LESSONCNT"];
            $framecnt  = $row["FRAMECNT"];
            $option[]  = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                               "value" => $row["CHAIRCD"]);

            $chaircd[] = $row["CHAIRCD"];
        }

        //週授業回数
        $objForm->ae(array("type"        => "text",
                            "name"        => "LESSONCNT",
                            "size"        => 9,
                            "maxlength"   => 2,
                            "extrahtml"   => "style=\"text-align:right;\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $lessoncnt ));
        $arg["lessoncnt"] = $objForm->ge("LESSONCNT");

        //連続枠数
        $objForm->ae(array("type"        => "text",
                            "name"        => "FRAMECNT",
                            "size"        => 9,
                            "maxlength"   => 2,
                            "extrahtml"   => "style=\"text-align:right;\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $framecnt));
        $arg["framecnt"] = $objForm->ge("FRAMECNT");

        //選択群講座
        $objForm->ae(array("type"        => "select",
                            "name"        => "left_chaircd",
                            "size"        => "15",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right','left_chaircd','right_chaircd',1);\"",
                            "options"     => $option));

        $option = array();
        $result = $db->query(knjb0010Query::getChair($model, $chaircd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $option[]  = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                               "value" => $row["CHAIRCD"]);
        }

        $objForm->ae(array("type"        => "select",
                            "name"        => "right_chaircd",
                            "size"        => "15",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left','left_chaircd','right_chaircd',1);\"",
                            "options"     => $option));

        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move1('sel_add_all','left_chaircd','right_chaircd',1);\"" ));

        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move1('left','left_chaircd','right_chaircd',1);\"" ));

        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move1('right','left_chaircd','right_chaircd',1);\"" ));

        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move1('sel_del_all','left_chaircd','right_chaircd',1);\"" ));

        $arg["chair"] = array( "LEFT_LIST"   => "選択群講座",
                               "RIGHT_LIST"  => "講座一覧",
                               "LEFT_PART"   => $objForm->ge("left_chaircd"),
                               "RIGHT_PART"  => $objForm->ge("right_chaircd"),
                               "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                               "SEL_ADD"     => $objForm->ge("sel_add"),
                               "SEL_DEL"     => $objForm->ge("sel_del"),
                               "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        //受講クラス
        $option = $class = array();

        $result = $db->query(knjb0010Query::selectQueryChairClsDat($model, $chaircd, $model->groupcd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $option[] = array("label" => $row["HR_NAME"],"value" => $row["TRGTGRADE"]."-".$row["TRGTCLASS"]);
            //2004/08/19 arakaki $class[] = sprintf("%02d-%02d",$row["TRGTGRADE"],$row["TRGTCLASS"]);
            $class[] = sprintf("%02d-%03s", $row["TRGTGRADE"], $row["TRGTCLASS"]);
        }

        $objForm->ae(array("type"        => "select",
                            "name"        => "left_class",
                            "size"        => "15",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move2('right','left_class','right_class',1);\"",
                            "options"     => $option));

        //クラス一覧
        $option = array();
        $result = $db->query(knjb0010Query::getClass($model, $class));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $option[] = array("label" => $row["HR_NAME"],"value" => $row["GRADE"]."-".$row["HR_CLASS"]);
        }

        $objForm->ae(array("type"        => "select",
                            "name"        => "right_class",
                            "size"        => "15",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move2('left','left_class','right_class',1);\"",
                            "options"     => $option));

        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add_all2",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move2('sel_add_all2','left_class','right_class',1);\"" ));

        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add2",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move2('left','left_class','right_class',1);\"" ));

        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del2",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move2('right','left_class','right_class',1);\"" ));

        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del_all2",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move2('sel_del_all2','left_class','right_class',1);\"" ));

        $arg["class"] = array( "LEFT_LIST"   => "受講クラス",
                               "RIGHT_LIST"  => "クラス一覧",
                               "LEFT_PART"   => $objForm->ge("left_class"),
                               "RIGHT_PART"  => $objForm->ge("right_class"),
                               "SEL_ADD_ALL" => $objForm->ge("sel_add_all2"),
                               "SEL_ADD"     => $objForm->ge("sel_add2"),
                               "SEL_DEL"     => $objForm->ge("sel_del2"),
                               "SEL_DEL_ALL" => $objForm->ge("sel_del_all2"));

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae(array("type"        => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "selectdata"));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "selectdata2"));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "GROUPCD",
                            "value"     => $model->groupcd));

        if (VARS::post("cmd") == "update") {
            $arg["jscript"] = "window.open('knjb0010index.php?cmd=list','left_frame');";
        }

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjb0010Form2.html", $arg);
    }
}
