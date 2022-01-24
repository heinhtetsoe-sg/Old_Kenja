<?php

require_once('for_php7.php');

class knjz130a_2Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz130a_2index.php", "", "edit");

        if (isset($model->namecd1) && isset($model->namecd2) && !isset($model->warning)){
            $Row = knjz130a_2Query::getRow($model->namecd1,$model->namecd2);
            $temp_cd = $Row["NAMECD1"].$Row["NAMECD2"];
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //区分
        $arg["data"]["NAMECD1"] = $Row["NAMECD1"];
        knjCreateHidden($objForm, "NAMECD1", $Row["NAMECD1"]);

        //コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAMECD2",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "value"       => $Row["NAMECD2"],
                            "extrahtml"   => "onblur=\"this.value=toAlphaNumber(this.value)\";" ));

        $arg["data"]["NAMECD2"] = $objForm->ge("NAMECD2");

        //区分説明
        $arg["data"]["CDMEMO"] = $Row["CDMEMO"];

        //名称1
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME1",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "value"       => $Row["NAME1"] ));

        $arg["data"]["NAME1"] = $objForm->ge("NAME1");

        //名称2
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME2",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "value"       => $Row["NAME2"] ));

        $arg["data"]["NAME2"] = $objForm->ge("NAME2");

        //名称3
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME3",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "value"       => $Row["NAME3"] ));

        $arg["data"]["NAME3"] = $objForm->ge("NAME3");

        //名称説明1
        $arg["data"]["NAME1MEMO"] = $Row["NAME1MEMO"];

        //名称説明2
        $arg["data"]["NAME2MEMO"] = $Row["NAME2MEMO"];

        //名称説明3
        $arg["data"]["NAME3MEMO"] = $Row["NAME3MEMO"];

        //略称1
        $objForm->ae( array("type"        => "text",
                            "name"        => "ABBV1",
                            "size"        => 40,
                            "maxlength"   => 30,
                            "value"       => $Row["ABBV1"] ));

        $arg["data"]["ABBV1"] = $objForm->ge("ABBV1");

        //略称2
        $objForm->ae( array("type"        => "text",
                            "name"        => "ABBV2",
                            "size"        => 40,
                            "maxlength"   => 30,
                            "value"       => $Row["ABBV2"] ));

        $arg["data"]["ABBV2"] = $objForm->ge("ABBV2");

        //略称3
        $objForm->ae( array("type"        => "text",
                            "name"        => "ABBV3",
                            "size"        => 40,
                            "maxlength"   => 30,
                            "value"       => $Row["ABBV3"] ));

        $arg["data"]["ABBV3"] = $objForm->ge("ABBV3");

        //略称説明1
        $arg["data"]["ABBV1MEMO"] = $Row["ABBV1MEMO"];

        //略称説明2
        $arg["data"]["ABBV2MEMO"] = $Row["ABBV2MEMO"];

        //略称説明3
        $arg["data"]["ABBV3MEMO"] = $Row["ABBV3MEMO"];

        //名称予備1
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAMESPARE1",
                            "size"        => 40,
                            "maxlength"   => 30,
                            "value"       => $Row["NAMESPARE1"] ));

        $arg["data"]["NAMESPARE1"] = $objForm->ge("NAMESPARE1");

        //名称予備2
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAMESPARE2",
                            "size"        => 40,
                            "maxlength"   => 30,
                            "value"       => $Row["NAMESPARE2"] ));

        $arg["data"]["NAMESPARE2"] = $objForm->ge("NAMESPARE2");

        //名称予備3
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAMESPARE3",
                            "size"        => 40,
                            "maxlength"   => 30,
                            "value"       => $Row["NAMESPARE3"] ));

        $arg["data"]["NAMESPARE3"] = $objForm->ge("NAMESPARE3");

        //名称予備説明1
        $arg["data"]["NAMESPARE1MEMO"] = $Row["NAMESPARE1MEMO"];

        //名称予備説明2
        $arg["data"]["NAMESPARE2MEMO"] = $Row["NAMESPARE2MEMO"];

        //名称予備説明3
        $arg["data"]["NAMESPARE3MEMO"] = $Row["NAMESPARE3MEMO"];

        //追加ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return Btn_reset('clear');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //戻るボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ130A/knjz130aindex.php";
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

        if ($temp_cd=="") $temp_cd = $model->field["temp_cd"];
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "temp_cd",
                            "value"     => $temp_cd
                            ) ); 
                                      
        $cd_change = false;                                                                               
        if ($temp_cd==$Row["NAMECD1"].$Row["NAMECD2"] ) $cd_change = true;

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && ($cd_change==true || $model->isload != "1")){
        //    $arg["reload"]  = "parent.left_frame.location.reload();";
            $arg["reload"]  = "window.open('knjz130a_2index.php?cmd=list&NAMECD1=$model->namecd1','left_frame');";
        }

        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz130a_2Form2.html", $arg); 
    }
}
?>
