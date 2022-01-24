<?php

require_once('for_php7.php');

class knjz050_2Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz050_2index.php", "", "edit");
        //警告メッセージを表示しない場合

        if (!isset($model->warning))
        {
            $Row = knjz050_2Query::getRow($model->majorcd, $model->coursecd);
        } else {
            $Row =& $model->field;
        }

        //課程コード
        $opt = knjz050_2Query::getCourse();
        $objForm->ae( array("type"        => "select",
                            "name"        => "COURSECD",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $Row["COURSECD"],
                            "options"     => $opt));
        $arg["data"]["COURSECD"] = $objForm->ge("COURSECD");

        //学科コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "MAJORCD",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["MAJORCD"]));
        $arg["data"]["MAJORCD"] = $objForm->ge("MAJORCD");

        //学科名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "MAJORNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "",
                            "value"       => $Row["MAJORNAME"] ));
        $arg["data"]["MAJORNAME"] = $objForm->ge("MAJORNAME");

        //表示用名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "MAJORNAME2",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "",
                            "value"       => $Row["MAJORNAME2"] ));
        $arg["data"]["MAJORNAME2"] = $objForm->ge("MAJORNAME2");

        //学科略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "MAJORABBV",
                            "size"        => 4,
                            "maxlength"   => 6,
                            "extrahtml"   => "",
                            "value"       => $Row["MAJORABBV"] ));
        $arg["data"]["MAJORABBV"] = $objForm->ge("MAJORABBV");

        //学科名称英字
        $majorengLength = $model->majorengLength <= 20 ? 20 : 45;
        $objForm->ae( array("type"        => "text",
                            "name"        => "MAJORENG",
                            "size"        => $majorengLength,
                            "maxlength"   => $majorengLength,
                            "extrahtml"   => "onblur=\"this.value=toAlphanumeric(this.value)\"",
                            "value"       => $Row["MAJORENG"] ));
        $arg["data"]["MAJORENG"] = $objForm->ge("MAJORENG");

        //学科銀行コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "MAJORBANKCD",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["MAJORBANKCD"] ));
        $arg["data"]["MAJORBANKCD"] = $objForm->ge("MAJORBANKCD");

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
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ050/knjz050index.php";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"        => "hidden",
                            "name"        => "cmd"
                            ) );

        $objForm->ae( array("type"        => "hidden",
                            "name"        => "UPDATED",
                            "value"       => $Row["UPDATED"]
                            ) );
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz050_2index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz050_2Form2.html", $arg);
    }
}
?>
