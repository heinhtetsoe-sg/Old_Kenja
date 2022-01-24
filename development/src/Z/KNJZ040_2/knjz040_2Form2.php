<?php

require_once('for_php7.php');

class knjz040_2Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz040_2index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning))
        {
            $Row = knjz040_2Query::getRow($model->coursecd);
        } else {
            $Row =& $model->field;
        }

        //課程コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "COURSECD",
                            "size"        => 1,
                            "maxlength"   => 1,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["COURSECD"] ));
        $arg["data"]["COURSECD"] = $objForm->ge("COURSECD");

        //課程名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "COURSENAME",
                            "size"        => 8,
                            "maxlength"   => 12,
                            "extrahtml"   => "",
                            "value"       => $Row["COURSENAME"] ));
        $arg["data"]["COURSENAME"] = $objForm->ge("COURSENAME");

        //課程略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "COURSEABBV",
                            "size"        => 5,
                            "maxlength"   => 6,
                            "extrahtml"   => "",
                            "value"       => $Row["COURSEABBV"] ));
        $arg["data"]["COURSEABBV"] = $objForm->ge("COURSEABBV");

        //課程英字
        $objForm->ae( array("type"        => "text",
                            "name"        => "COURSEENG",
                            "size"        => 10,
                            "maxlength"   => 10,
                            "extrahtml"  => "onblur=\"this.value=toAlphanumeric(this.value)\"",
                            "value"       => $Row["COURSEENG"] ));
        $arg["data"]["COURSEENG"] = $objForm->ge("COURSEENG");

        $db    = Query::dbCheckOut();

        //校時名称
        $result = $db->query(knjz040_2Query::getName());
        $opt = array();        
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  => $row["NAMECD2"]."  ".$row["NAME1"],
                           "value"  => $row["NAMECD2"]);
        }
        //開始校時
        $objForm->ae( array("type"        => "select",
                            "name"        => "S_PERIODCD",
                            "size"        => 1,
                            "extrahtml"   => "",
                            "value"       => $Row["S_PERIODCD"],
                            "options"     => $opt));
        $arg["data"]["S_PERIODCD"] = $objForm->ge("S_PERIODCD");
        //終了校時
        $objForm->ae( array("type"        => "select",
                            "name"        => "E_PERIODCD",
                            "size"        => 1,
                            "extrahtml"   => "",
                            "value"       => $Row["E_PERIODCD"],
                            "options"     => $opt));
        $arg["data"]["E_PERIODCD"] = $objForm->ge("E_PERIODCD");               

        Query::dbCheckIn($db);

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"           => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ040/knjz040index.php";
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz040_2index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz040_2Form2.html", $arg); 
    }
}
?>
