<?php

require_once('for_php7.php');

class knjz270_2Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz270_2index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning))
        {
            $Row = knjz270_2Query::getRow($model->sectioncd);
        } else {
            $Row =& $model->field;
        }
        //所属コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "SECTIONCD",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["SECTIONCD"]));

        $arg["data"]["SECTIONCD"] = $objForm->ge("SECTIONCD");

        //所属名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "SECTIONNAME",
                            "size"        => 16,
                            "maxlength"   => 24,
                            "extrahtml"   => "",
                            "value"       => $Row["SECTIONNAME"] ));

        $arg["data"]["SECTIONNAME"] = $objForm->ge("SECTIONNAME");

        //所属略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "SECTIONABBV",
                            "size"        => 8,
                            "maxlength"   => 12,
                            "extrahtml"   => "",
                            "value"       => $Row["SECTIONABBV"] ));

        $arg["data"]["SECTIONABBV"] = $objForm->ge("SECTIONABBV");

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type"           => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ270/knjz270index.php";
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );

        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz270_2index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz270_2Form2.html", $arg); 
    }
}
?>
