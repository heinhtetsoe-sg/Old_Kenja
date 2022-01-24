<?php

require_once('for_php7.php');

class knjtx005Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjtx005index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (isset($model->s_date) && !isset($model->warning)){
            $Row = knjtx005Query::getRow($model->s_date);
        }else{
            $Row =& $model->field;
        }

		//個人番号
        $extra = "";
        $arg["data"]["KOJIN_NO"] = knjCreateTextBox($objForm, $model->field["KOJIN_NO"], "KOJIN_NO", 2, 2, $extra);

        //開始日付
        $arg["data"]["S_DATE"] = str_replace("input ", "input ", View::popUpCalendar($objForm, "S_DATE", str_replace("-","/",$Row["S_DATE"]), ""));


        //知事名称
        /*$objForm->ae( array("type"        => "text",
                            "name"        => "CHIJI_NAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "value"       => $Row["CHIJI_NAME"] ));

        $arg["data"]["CHIJI_NAME"] = $objForm->ge("CHIJI_NAME");*/

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
            $arg["reload"]  = "parent.left_frame.location.href='knjtx005index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjtx005Form2.html", $arg); 
    }
}
?>
