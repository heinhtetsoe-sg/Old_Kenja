<?php

require_once('for_php7.php');

class knjz170aForm2
{
    function main(&$model){
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz170aindex.php", "", "edit");
        //警告メッセージを表示しない場合

        if (!isset($model->warning))
        {
            $Row = knjz170aQuery::getRow($model->groupcd, $model);
        } else {
            $Row =& $model->field;
        }
    
        //群コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "GROUPCD",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["GROUPCD"]));

        $arg["data"]["GROUPCD"] = $objForm->ge("GROUPCD");

        //群名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "GROUPNAME",
                            "size"        => 9,
                            "maxlength"   => 9,
                            "extrahtml"   => "",
                            "value"       => $Row["GROUPNAME"] ));

        $arg["data"]["GROUPNAME"] = $objForm->ge("GROUPNAME");

        //群略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "GROUPABBV",
                            "size"        => 6,
                            "maxlength"   => 6,
                            "extrahtml"   => "",
                            "value"       => $Row["GROUPABBV"] ));

        $arg["data"]["GROUPABBV"] = $objForm->ge("GROUPABBV");

        //備考
        $objForm->ae( array("type"        => "text",
                            "name"        => "REMARK",
                            "size"        => 45,
                            "maxlength"   => 90,
                            "extrahtml"   => "",
                            "value"       => $Row["REMARK"] ));

        $arg["data"]["REMARK"] = $objForm->ge("REMARK");

        //表示順
        $objForm->ae( array("type"        => "text",
                            "name"        => "SHOWORDER",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["SHOWORDER"] ));

        $arg["data"]["SHOWORDER"] = $objForm->ge("SHOWORDER");

        //追加ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type"           => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return ShowConfirm('clear')\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
                    
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        knjCreateHidden($objForm, "YEAR", $Row["YEAR"]);

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
            $arg["reload"]  = "parent.left_frame.location.href='knjz170aindex.php?cmd=list';";
        }
                                
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz170aForm2.html", $arg); 
    }
}        
?>
