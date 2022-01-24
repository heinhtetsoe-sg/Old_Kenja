<?php

require_once('for_php7.php');

class knjz010kForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz010kindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = knjz010kQuery::getRow($model->year,$model->examcoursecd,$model->coursecd,$model->majorcd);
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        //課程学科コンボ
        $result    = $db->query(knjz010kQuery::selectTotalcd($model->year));
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["COURSECD"].$row["MAJORCD"].":".$row["COURSENAME"].$row["MAJORNAME"],
                           "value" => $row["COURSECD"].$row["MAJORCD"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"        => "select",
                            "name"        => "TOTALCD",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $model->totalcd,
                            "options"     => $opt));
        $arg["data"]["TOTALCD"] = $objForm->ge("TOTALCD");

        //コースコード
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMCOURSECD",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["EXAMCOURSECD"] ));
        $arg["data"]["EXAMCOURSECD"] = $objForm->ge("EXAMCOURSECD");

        //コース名
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMCOURSE_NAME",
                            "size"        => 21,
                            "maxlength"   => 33,
                            "extrahtml"   => "",
                            "value"       => $Row["EXAMCOURSE_NAME"] ));
        $arg["data"]["EXAMCOURSE_NAME"] = $objForm->ge("EXAMCOURSE_NAME");

        //コース略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMCOURSE_ABBV",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "",
                            "value"       => $Row["EXAMCOURSE_ABBV"] ));
        $arg["data"]["EXAMCOURSE_ABBV"] = $objForm->ge("EXAMCOURSE_ABBV");

        //コース記号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMCOURSE_MARK",
                            "size"        => 1,
                            "maxlength"   => 1,
                            "extrahtml"   => "onblur=\"this.value=toAlpha(this.value)\"",
                            "value"       => $Row["EXAMCOURSE_MARK"] ));
        $arg["data"]["EXAMCOURSE_MARK"] = $objForm->ge("EXAMCOURSE_MARK");

        //コース定員
        $objForm->ae( array("type"        => "text",
                            "name"        => "CAPACITY",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["CAPACITY"] ));
        $arg["data"]["CAPACITY"] = $objForm->ge("CAPACITY");

        //追加
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリア
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
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
                            "name"      => "year",
                            "value"     => $model->year
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz010kindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz010kForm2.html", $arg);
    }
}
?>
