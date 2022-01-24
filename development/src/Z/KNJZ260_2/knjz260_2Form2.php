<?php

require_once('for_php7.php');

class knjz260_2Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz260_2index.php", "", "edit");
        //警告メッセージを表示しない場合
        if (isset($model->dutysharecd) && !isset($model->warning))
        {
            $Row = knjz260_2Query::getRow($model->dutysharecd);
        }else{
            $Row =& $model->field;
        }
        //校務分掌部コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "DUTYSHARECD",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["DUTYSHARECD"] ));

        $arg["data"]["DUTYSHARECD"] = $objForm->ge("DUTYSHARECD");

        //分掌部名
        $objForm->ae( array("type"        => "text",
                            "name"        => "SHARENAME",
                            "size"        => 16,
                            "maxlength"   => 24,
                            "value"       => $Row["SHARENAME"] ));

        $arg["data"]["SHARENAME"] = $objForm->ge("SHARENAME");

        //学校基本調査名称
        $extra = "";
        $arg["data"]["BASE_SHARENAME"] = knjCreateTextBox($objForm, $Row["BASE_SHARENAME"], "BASE_SHARENAME", 16, 24, $extra);

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
                            "extrahtml"   => "onclick=\"return Btn_reset('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ260/knjz260index.php?year_code=".$model->year_code;
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );

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

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_code",
                            "value"     => $model->year_code
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_code",
                            "value"     => $model->year_code
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz260_2index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz260_2Form2.html", $arg); 
    }
}
?>
