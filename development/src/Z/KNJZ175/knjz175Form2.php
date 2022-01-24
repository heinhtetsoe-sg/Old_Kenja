<?php

require_once('for_php7.php');

class knjz175Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz175index.php", "", "edit");

        $arg["GROUPCD"]   = $model->pgroupcd;
        $arg["GROUPNAME"] = $model->pgroupname;

        $db     = Query::dbCheckOut();

        //子群リスト
        $query  = knjz175Query::getChGroup($model->pgroupcd,CTRL_YEAR);
        $result = $db->query($query);
        $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => htmlspecialchars(($row["CHILD_GRPCD"]."  ".$row["GROUPNAME"])),
                                "value" => $row["CHILD_GRPCD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "L_GROUPCD",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_left));

        //群リスト
        $query  = knjz175Query::getGroup(CTRL_YEAR,$model->pgroupcd);
        $result = $db->query($query);
        $opt_right = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_right[] = array("label" => htmlspecialchars(($row["GROUPCD"]."  ".$row["GROUPNAME"])),
                                "value" => $row["GROUPCD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "R_GROUPCD",
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

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("L_GROUPCD"),
                                   "RIGHT_PART"  => $objForm->ge("R_GROUPCD"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));


        $result->free();
        Query::dbCheckIn($db);

        //修正ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return doSubmit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //クリアボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return doSubmit('reset')\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
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
            $arg["reload"]  = "window.open('knjz175index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz175Form2.html", $arg);
    }
}
?>
