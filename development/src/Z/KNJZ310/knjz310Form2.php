<?php

require_once('for_php7.php');

class knjz310Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz310index.php", "", "edit");

        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->groupcd)){
            $Row = knjz310Query::getRow($model->groupcd, $model);
            $temp_cd = $Row["GROUPCD"];
        }else{
            $Row =& $model->field;
            $temp_cd = "";
        }

        //グループコード
        $objForm->ae( array("type"        => "text",
                            "name"        => "GROUPCD",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["GROUPCD"] ));

        $arg["data"]["GROUPCD"] = $objForm->ge("GROUPCD");

        //グループ名
        $objForm->ae( array("type"        => "text",
                            "name"        => "GROUPNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
        //                  "extrahtml"   => "onclick=\"return btn_ctrl('');\"",
                            "value"       => $Row["GROUPNAME"] ));

        $arg["data"]["GROUPNAME"] = $objForm->ge("GROUPNAME");

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
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) ); 
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );

        if ($temp_cd=="" && isset($model->field["temp_cd"])) $temp_cd = $model->field["temp_cd"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "temp_cd",
                            "value"     => $temp_cd
                            ) ); 

        Query::dbCheckIn($db);

        $cd_change = false;                                                                               
        if ($temp_cd==$Row["GROUPCD"] ) $cd_change = true;

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && ($cd_change==true || $model->isload != "1")){
            $arg["reload"]  = "parent.left_frame.location.href='knjz310index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz310Form2.html", $arg);
    }
} 
?>
