<?php

require_once('for_php7.php');

class knjj201_2Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjj201_2index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning))
        {
            $Row = knjj201_2Query::getRow($model->div, $model->executivecd);
        } else {
            $Row =& $model->field;
        }

        //区分
        $opt = knjj201_2Query::getDiv();
        $objForm->ae( array("type"        => "select",
                            "name"        => "DIV",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $Row["DIV"],
                            "options"     => $opt));
        $arg["data"]["DIV"] = $objForm->ge("DIV");

        //コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXECUTIVECD",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["EXECUTIVECD"] ));
        $arg["data"]["EXECUTIVECD"] = $objForm->ge("EXECUTIVECD");

        //役職名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME",
                            "size"        => 41,
                            "maxlength"   => 60,
                            "extrahtml"   => "",
                            "value"       => $Row["NAME"] ));
        $arg["data"]["NAME"] = $objForm->ge("NAME");

        //役職略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "ABBV",
                            "size"        => 11,
                            "maxlength"   => 15,
                            "extrahtml"   => "",
                            "value"       => $Row["ABBV"] ));
        $arg["data"]["ABBV"] = $objForm->ge("ABBV");

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
        $link = REQUESTROOT."/J/KNJJ201/knjj201index.php";
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
            $arg["reload"]  = "parent.left_frame.location.href='knjj201_2index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj201_2Form2.html", $arg); 
    }
}
?>
