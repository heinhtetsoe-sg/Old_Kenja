<?php

require_once('for_php7.php');

class knjl070kForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl070kindex.php", "", "main");
        $db           = Query::dbCheckOut();

        if ($model->isWarning()){
            $row =& $model->field;
        }else{
            $query = knjl070kQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        //会場名
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMHALL_NAME",
                            "size"        => 30,
                            "maxlength"   => 30,
                            "value"       => $row["EXAMHALL_NAME"]
                            ));

        $arg["EXAMHALL_NAME"] = $objForm->ge("EXAMHALL_NAME");

        //会場名
        $objForm->ae( array("type"        => "text",
                            "name"        => "CAPA_CNT",
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => $row["CAPA_CNT"]
                            ));

        $arg["CAPA_CNT"] = $objForm->ge("CAPA_CNT");

        //開始受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "S_EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => $row["S_EXAMNO"]
                            ));

        $arg["S_EXAMNO"] = $objForm->ge("S_EXAMNO");

        if ($model->mode == "update"){
            $value = "更 新";
        }else{
            $value = "追 加";
        }
        //更新ボタン作成
        $objForm->ae( array("type"        => "button",
                                "name"        => "btn_update",
                                "value"       => $value,
                                "extrahtml"   => "onclick=\"return btn_submit('".$model->mode ."')\"" ) );

        $arg["btn_update"]  = $objForm->ge("btn_update");

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
        View::toHTML($model, "knjl070kForm2.html", $arg); 
    }
}
?>
