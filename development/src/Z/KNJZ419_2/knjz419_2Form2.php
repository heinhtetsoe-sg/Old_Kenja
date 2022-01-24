<?php

require_once('for_php7.php');

class knjz419_2Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz419_2index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz419_2Query::getRow($model->questionnairecd);
        } else {
            $Row =& $model->field;
        }

        //進路アンケートコード
        $objForm->ae( array("type"        => "text",
                            "name"        => "QUESTIONNAIRECD",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["QUESTIONNAIRECD"] ));
        $arg["data"]["QUESTIONNAIRECD"] = $objForm->ge("QUESTIONNAIRECD");

        //進路アンケート名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "QUESTIONNAIRENAME",
                            "size"        => 50,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $Row["QUESTIONNAIRENAME"] ));
        $arg["data"]["QUESTIONNAIRENAME"] = $objForm->ge("QUESTIONNAIRENAME");

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"      => "reset",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ419/knjz419index.php";
        $objForm->ae( array("type" => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"parent.location.href='$link';\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd"
                            ) );

        $objForm->ae( array("type"  => "hidden",
                            "name"  => "UPDATED",
                            "value" => $Row["UPDATED"]
                            ) );
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz419_2index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz419_2Form2.html", $arg); 
    }
}
?>
