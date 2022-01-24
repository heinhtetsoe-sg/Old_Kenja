<?php

require_once('for_php7.php');

class knjz160_2Form2
{
    function main(&$model)
    {
        $arg["reload"] = "";
        
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz160_2index.php", "", "edit");

        if (!isset($model->warning)){
            $Row = knjz160_2Query::getRow($model->faccd);
        } else {
            $Row =& $model->field;
        }
        
        //施設コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "FACCD",
                            "size"        => 10,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["FACCD"]));

        $arg["data"]["FACCD"] = $objForm->ge("FACCD");

        //施設名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "FACILITYNAME",
                            "size"        => 30,
                            "maxlength"   => 30,
                            "extrahtml"   => "",
                            "value"       => $Row["FACILITYNAME"] ));

        $arg["data"]["FACILITYNAME"] = $objForm->ge("FACILITYNAME");

        //施設略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "FACILITYABBV",
                            "size"        => 6,
                            "maxlength"   => 6,
                            "extrahtml"   => "",
                            "value"       => $Row["FACILITYABBV"] ));

        $arg["data"]["FACILITYABBV"] = $objForm->ge("FACILITYABBV");

        //定員数
        $objForm->ae( array("type"        => "text",
                            "name"        => "CAPACITY",
                            "size"        => 10,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["CAPACITY"] ));

        $arg["data"]["CAPACITY"] = $objForm->ge("CAPACITY");

        //定員数
        $objForm->ae( array("type"        => "text",
                            "name"        => "CHR_CAPACITY",
                            "size"        => 10,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["CHR_CAPACITY"] ));

        $arg["data"]["CHR_CAPACITY"] = $objForm->ge("CHR_CAPACITY");

        //追加ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"           => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ160/knjz160index.php";
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );
                    
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]) );
    
        $arg["finish"]  = $objForm->get_finish();
    
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz160_2index.php?cmd=list';";
        }
                                
        View::toHTML($model, "knjz160_2Form2.html", $arg); 
    }
}
?>
