<?php

require_once('for_php7.php');

class knjl020kForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl020kindex.php", "", "main");
        
        if ($model->datadiv == "1"){
            $arg["TARGET"] = "出身学校";
            $model->acceptno2 = $model->fs_acceptno;    //2005.08.10 minei
        }else{
            $arg["TARGET"] = "塾";
            $model->acceptno2 = $model->ps_acceptno;    //2005.08.10 minei
        }

        //受付No
        $objForm->ae( array("type"        => "text",
                            "name"        => "ACCEPTNO2",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeypress=\"return btn_keypress()\"",
                            "value"       => $model->acceptno2
                            ));

        $arg["ACCEPTNO"] = $objForm->ge("ACCEPTNO2");


        //コピーボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_copy",
                            "value"       => "コピー",
                            "extrahtml"   => "onclick=\"return btn_submit('copy')\"" ) );

        $arg["btn_copy"]  = $objForm->ge("btn_copy");

        //削除ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_delete",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete2')\"" ) );

        $arg["btn_delete"]  = $objForm->ge("btn_delete");

        //戻るボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"top.main_frame.closeit()\"" ) );

        $arg["btn_back"]  = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl020kForm2.html", $arg); 
    }
}
?>
