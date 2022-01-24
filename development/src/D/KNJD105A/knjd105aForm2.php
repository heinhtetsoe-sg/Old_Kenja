<?php

require_once('for_php7.php');

class knjd105aForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
           $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd105aindex.php", "", "edit");

        $db        = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = $db->getRow(knjd105aQuery::getRow($model), DB_FETCHMODE_ASSOC);
        }else{
            $Row =& $model->field;
        }

        //コース名コンボボ設定
        $opt       = array();
        $result    = $db->query(knjd105aQuery::getCourseList($model));   
        while($val = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => "(".$val["COURSECD"].$val["MAJORCD"].") ".$val["COURSENAME"].$val["MAJORNAME"]."(".$val["COURSECODE"].") ".$val["COURSECODENAME"], 
                           "value" => $val["COURSECD"]."-".$val["MAJORCD"]."-".$val["COURSECODE"]);
        }
        $result->free();

        $objForm->ae( array("type"        => "select",
                            "name"        => "COURSE",
                            "size"        => "1",
                            "value"       => $Row["COURSECD"]."-".$Row["MAJORCD"]."-".$Row["COURSECODE"],
                            "options"     => $opt ));

        $arg["data"]["COURSE"] = $objForm->ge("COURSE");


        //全体評
        $objForm->ae( array("type"        => "textarea",
                            "name"        => "FOOTNOTE",
                            "cols"        => 91,
                            "rows"        => 7,
                            "extrahtml"   => "style=\"height:103px;\"",
                            //"wrap"        => "soft",
                            "value"       => $Row["FOOTNOTE"] ));

        $arg["data"]["FOOTNOTE"] = $objForm->ge("FOOTNOTE");

        //コメント
        $arg["COMMENT"] = "※全角45文字×7行まで";

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) ); 
                   
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );
                            
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRADE",
                            "value"     => $model->grade
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TESTCD",
                            "value"     => $model->testcd
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => $model->semester
                            ) );

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjd105aindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd105aForm2.html", $arg); 
    }
}
?>
