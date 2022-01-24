<?php

require_once('for_php7.php');

class knjz400j_2Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz400j_2index.php", "", "edit");
        if (isset($model->viewcd) && !isset($model->warning)){
            $Row = knjz400j_2Query::getRow($model->viewcd);
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();
        //教科取得
        $query = "SELECT * FROM class_mst WHERE classcd < '90'";
        $result = $db->query($query);
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => htmlspecialchars($row["CLASSCD"]."：".$row["CLASSNAME"]),
                           "value" => $row["CLASSCD"]);
        }

        $result->free();

        Query::dbCheckIn($db);
        //観点コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "VIEWCD",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => substr($Row["VIEWCD"] ,2) ));
        $arg["data"]["VIEWCD"] = $objForm->ge("VIEWCD");

        //観点名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "VIEWNAME",
                            "size"        => 48,
                            "maxlength"   => 75,
                            "value"       => $Row["VIEWNAME"] ));
        $arg["data"]["VIEWNAME"] = $objForm->ge("VIEWNAME");

        //表示順
        $objForm->ae( array("type"        => "text",
                            "name"        => "SHOWORDER",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"",
                            "value"       => $Row["SHOWORDER"] ));
        $arg["data"]["SHOWORDER"] = $objForm->ge("SHOWORDER");

        //教科コード
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASSCD",
                            "size"       => "1",
                            "value"      => substr($Row["VIEWCD"], 0 ,2),
                            "options"    => $opt));
        $arg["data"]["CLASSCD"] = $objForm->ge("CLASSCD");

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return Btn_reset('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ400J/knjz400jindex.php?year_code=".$model->year_code;
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year_code",
                            "value"     => $model->year_code
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){ 
            $arg["reload"]  = "parent.left_frame.location.href='knjz400j_2index.php?cmd=list';";
        }
        View::toHTML($model, "knjz400j_2Form2.html", $arg); 
    }
}
?>
