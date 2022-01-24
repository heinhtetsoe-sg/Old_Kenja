<?php

require_once('for_php7.php');


class knjz280_2form2{

    function main(&$model){
    
        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjz280_2form2", "POST", "knjz280_2index.php", "", "knjz280_2form2");

        //警告メッセージを表示しない場合
        if (!isset($model->warning))
        {
            $Row = knjz280_2Query::getRow($model->jobcd);
        } else {
            $Row =& $model->field;
        }

        //職名コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "JOBCD",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["JOBCD"] ));

        $arg["data"]["JOBCD"] = $objForm->ge("JOBCD");

        //職名名称
        $objForm->ae( array("type"      => "text",
                            "name"      => "JOBNAME",
                            "size"      => 40,
                            "maxlength" => 20,
                            "extrahtml" => "",
                            "value"     => $Row["JOBNAME"] ));

        $arg["data"]["JOBNAME"] = $objForm->ge("JOBNAME");

        //学校基本調査名称
        $extra = "";
        $arg["data"]["BASE_JOBNAME"] = knjCreateTextBox($objForm, $Row["BASE_JOBNAME"], "BASE_JOBNAME", 40, 20, $extra);

        //追加ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追 加",
                            "extrahtml" => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('clear');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ280/knjz280index.php";
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"parent.location.href='$link';\"" ) );
                    
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"] ) );

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.reload();";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz280_2form2.html", $arg); 
    }
}
?>
