<?php

require_once('for_php7.php');

class knjd104bForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
           $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd104bindex.php", "", "edit");        

       //デフォルト値
        if($model->semester ==""){
            $model->semester = CTRL_SEMESTER;
        }
       //デフォルト値
        if($model->coursecode ==""){
            $row = knjd104bQuery::getFirst_CouseKey($model->semester);
            $model->coursecode = $row["COURSECODE"];
            $model->coursecd   = $row["COURSECD"];
            $model->majorcd    = $row["MAJORCD"];
            $model->grade      = $row["GRADE"];
            $model->coursename="";
        }

       //デフォルト値
        if($model->testkindcd ==""){
            $row = knjd104bQuery::getFirst_TestKey($model->semester);
            $model->testkindcd    = $row["TESTKINDCD"];
            $model->testitemcd    = $row["TESTITEMCD"];
            $model->testname="";
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = knjd104bQuery::getRow($model);
        }else{
            $Row =& $model->field;
        }

        //科目コンボ設定
        $db = Query::dbCheckOut();
        
        $opt = array();
        $value_flg = false;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $value = $model->field["CLASSCD"]."-".$model->field["SCHOOL_KIND"]."-".$model->field["CURRICULUM_CD"]."-".$model->field["SUBCLASSCD"];
            if (VARS::post("cmd") == 'edit' || VARS::post("cmd") == 'update' || VARS::post("cmd") == 'add' || VARS::post("cmd") == 'delete') {
                $value = $model->field["SUBCLASSCD"];
                $model->field["CLASSCD"]    = substr($model->field["SUBCLASSCD"],0,2);
                $model->field["SUBCLASSCD"] = substr($model->field["SUBCLASSCD"],7,6);
                $model->subclasscd          = substr($model->subclasscd,7,6);
            }
        } else {
            $value = $model->field["SUBCLASSCD"];
        }
        $result = $db->query(knjd104bQuery::getSubclass($model));   
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $value, $opt, $extra, 1);

        //総評
        if($model->grade < '04' ){
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "FOOTNOTE",
                                "cols"        => 41,
                                "rows"        => 11,
                                "wrap"        => "soft",
                                "value"       => $Row["FOOTNOTE"] ));
        }else{
            $objForm->ae( array("type"        => "textarea",
                                "name"        => "FOOTNOTE",
                                "cols"        => 41,
                                "rows"        => 8,
                                "wrap"        => "soft",
                                "value"       => $Row["FOOTNOTE"] ));
        }

        $arg["data"]["FOOTNOTE"] = $objForm->ge("FOOTNOTE");

        //コメント
        if($model->grade < '04' ){
            $arg["COMMENT"] = "※全角20文字×10行まで";
        }else{
            $arg["COMMENT"] = "※全角20文字×7行まで";
        }


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

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjd104bindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd104bForm2.html", $arg); 
    }
}
?>
