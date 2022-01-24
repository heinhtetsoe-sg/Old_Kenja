<?php

require_once('for_php7.php');

class knjtx006Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjtx006index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->edboardcd) && !isset($model->warning)){
            $Row = knjtx006Query::getRow($model->edboardcd);
        }else{
            $Row =& $model->field;
        }

        //教育委員会コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "EDBOARDCD",
                            "size"        => 7,
                            "maxlength"   => 6,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["EDBOARDCD"] ));

        $arg["data"]["EDBOARDCD"] = $objForm->ge("EDBOARDCD");

        //教育委員会名
        $objForm->ae( array("type"        => "text",
                            "name"        => "EDBOARDNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "value"       => $Row["EDBOARDNAME"] ));

        $arg["data"]["EDBOARDNAME"] = $objForm->ge("EDBOARDNAME");

        //教育委員会略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "EDBOARDABBV",
                            "size"        => 10,
                            "maxlength"   => 15,
                            "value"       => $Row["EDBOARDABBV"] ));

        $arg["data"]["EDBOARDABBV"] = $objForm->ge("EDBOARDABBV");

        //追加ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjtx006index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjtx006Form2.html", $arg); 
    }
}
?>
