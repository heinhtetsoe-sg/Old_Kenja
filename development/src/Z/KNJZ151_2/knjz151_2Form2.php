<?php

require_once('for_php7.php');

class knjz151_2Form2
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz151_2index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->issuecd))
        {   
            $Row = knjz151_2Query::getRow($model->issuecd);
        } else {
            $Row =& $model->field;
            $Row["ISSUECOMPANYCD"]   = $model->field["ISSUECOMPANYCD"];
            $Row["ISSUECOMPANYNAME"] = $model->field["ISSUECOMPANYNAME"];
            $Row["ISSUECOMPANYABBV"] = $model->field["ISSUECOMPANYABBV"];
        }
    
        //発行者コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "ISSUECOMPANYCD",
                            "size"        => 8,
                            "maxlength"   => 8,
                            "extrahtml"   => "",
                            "value"       => $Row["ISSUECOMPANYCD"]));

        $arg["data"]["ISSUECOMPANYCD"] = $objForm->ge("ISSUECOMPANYCD");

        //発行者名
        $objForm->ae( array("type"        => "text",
                            "name"        => "ISSUECOMPANYNAME",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $Row["ISSUECOMPANYNAME"] ));

        $arg["data"]["ISSUECOMPANYNAME"] = $objForm->ge("ISSUECOMPANYNAME");
    
        //発行者略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "ISSUECOMPANYABBV",
                            "size"        => 15,
                            "maxlength"   => 15,
                            "extrahtml"   => "",
                            "value"       => $Row["ISSUECOMPANYABBV"] ));

        $arg["data"]["ISSUECOMPANYABBV"] = $objForm->ge("ISSUECOMPANYABBV");

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
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ151/knjz151index.php";
        $objForm->ae( array("type"        => "button",
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
            $arg["reload"]  = "parent.left_frame.location.href='knjz151_2index.php?cmd=list';";
        }
                                
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz151_2Form2.html", $arg);
    }
} 
?>
