<?php

require_once('for_php7.php');


class knjm380Form2
{

    function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjm380index.php", "", "edit");

        $db = Query::dbCheckOut();

        if (isset($model->warning) || !$model->chaircd){    
            $row = $model->field;
        }else {
            $query = knjm380Query::selectQuery($model,'');
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        $arg["data"] = $row;
        if (is_array($row)){
            $model->AddorUp = "up";
        }else {
            $model->AddorUp = "add";
        }
        $arg["NAME"] = $model->chaircd_show;
        $arg["CHAIRCD"] = $model->chaircd;
        $arg["data"]["KAMOKU"] = $model->chaircd_show;

        if (isset($model->warning) || !$model->chaircd) $row["SCH_SEQ_ALL"] = $row["SCHCNT"];
        $objForm->ae( array("type"        => "text",
                            "name"        => "SCHCNT",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);check(this)\"",
                            "value"       => $row["SCH_SEQ_ALL"]));

        $arg["data"]["SCHCNT"] = $objForm->ge("SCHCNT");

        if (isset($model->warning) || !$model->chaircd) $row["SCH_SEQ_MIN"] = $row["CHECKCNT"];
        $objForm->ae( array("type"        => "text",
                            "name"        => "CHECKCNT",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);check(this)\"",
                            "value"       => $row["SCH_SEQ_MIN"]));

        $arg["data"]["CHECKCNT"] = $objForm->ge("CHECKCNT");

        Query::dbCheckIn($db);

        $arg["CHAIRCD"]  = $model->chaircd;

        //修正ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "登　録",
                            "extrahtml"   => " onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //クリアボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => " onclick=\"return btn_submit('reset');\"") );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && !isset($model->warning)){
            $arg["reload"]  = "parent.left_frame.location.reload();";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm380Form2.html", $arg);
    }
}
?>
