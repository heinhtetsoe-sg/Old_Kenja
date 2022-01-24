<?php

require_once('for_php7.php');

class knjz140Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz140index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = knjz140Query::getRow($model->messagecd);
            $temp_cd = $Row["MSG_CD"];
        }else{
            $Row =& $model->field;
        }

        $label = array("I:インフォメーション","W:ワーニング","E:エラー","C:問い合わせ" );
        $value = array("I","W","E","C");
        for($i=0;$i<get_count($label);$i++) {
            $level[] = array("label" => $label[$i],"value" => $value[$i]);
        }

        //メッセージコード
        $objForm->ae( array("type"          => "text",
                            "name"          => "MSG_CD",
                            "size"          => 7,
                            "maxlength"     => 6,
                            "value"         => $Row["MSG_CD"] ));

        $arg["data"]["MSG_CD"] = $objForm->ge("MSG_CD");

        $objForm->ae( array("type"          => "select",
                            "name"          => "MSG_LEVEL",
                            "size"          => "1",
                            "value"         => $Row["MSG_LEVEL"],
                            "options"       => $level));

        $arg["data"]["MSG_LEVEL"] = $objForm->ge("MSG_LEVEL");

        //メッセージ内容
        $objForm->ae( array("type"          => "textarea",
                            "name"          => "MSG_CONTENT",
                            "rows"          => "3",
                            "cols"          => 40,
                            "maxlength"     => 50,
                            "value"         => $Row["MSG_CONTENT"] ));

        $arg["data"]["MSG_CONTENT"] = $objForm->ge("MSG_CONTENT");

        //メッセージ詳細
        $objForm->ae( array("type"          => "textarea",
                            "name"          => "MSG_DETAIL",
                            "rows"          => "3",
                            "cols"          => 40,
                            "maxlength"     => 50,
                            "value"         => $Row["MSG_DETAIL"] ));

        $arg["data"]["MSG_DETAIL"] = $objForm->ge("MSG_DETAIL");

        //対処
        $objForm->ae( array("type"          => "textarea",
                            "name"          => "HOWTO",
                            "rows"          => "3",
                            "cols"          => 40,
                            "maxlength"     => 50,
                            "value"         => $Row["HOWTO"] ));

        $arg["data"]["HOWTO"] = $objForm->ge("HOWTO");

        //追加ボタンを作成する
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_add",
                            "value"         => "追 加",
                            "extrahtml"     => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_update",
                            "value"         => "更 新",
                            "extrahtml"     => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_del",
                            "value"         => "削 除",
                            "extrahtml"     => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_reset",
                            "value"         => "取 消",
                            "extrahtml"     => "onclick=\"return btn_submit('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_back",
                            "value"         => "終 了",
                            "extrahtml"     => "onclick=\"closeWin();\"" ) );                    
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
                    
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "MSG_KIND",
                            "value"     => $model->MSG_KIND) );

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
        if ($temp_cd==$Row["MSG_CD"] ) $cd_change = true;

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz140index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz140Form2.html", $arg);
    }
}
?>
