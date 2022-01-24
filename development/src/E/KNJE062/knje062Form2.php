<?php

require_once('for_php7.php');

class knje062form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje062index.php", "", "edit");

        $db = Query::dbCheckOut();
        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)){ 
            if ($model->cmd == "add_year" || $model->cmd == "subclasscd") {
                $Row =& $model->field;
            } else { 
                $query = knje062Query::selectQuery($model);
                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            }
        }else{
            $Row =& $model->field;
        }
              
        //年度取得
        $query = knje062Query::selectQueryYear($model);
        $result = $db->query($query);
        $opt_year = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $model->year[] = $row["YEAR"];
        }
        

        //年度追加された値を保持 
        $year_arr = array_unique($model->year);
        foreach ($year_arr as $val)
        {
            $opt_year[] = array("label" => $val, "value" => $val);
        }
        rsort($opt_year);

        //年度コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "YEAR",
                            "size"        => "1",
                            "value"       => $Row["YEAR"],
                            "extrahtml"   => "onChange=\"return btn_submit('add_year');\"",
                            "options"     => $opt_year));    

        $objForm->ae( array("type"        => "text",
                            "name"        => "year_add",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => "" )); 

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_year_add",
                            "value"       => "年度追加",
                            "extrahtml"   => "onclick=\"return add('');\"" ));                           
                                          
        $arg["YEAR"] = $objForm->ge("YEAR")."&nbsp;&nbsp;".$objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add");
                                             
        if ($model->cmd == "subclasscd") {
            $arg["CLASSNAME"] = $db->getOne(knje062Query::SelectClassName($model));
        } else {
            $arg["CLASSNAME"] = $Row["CLASSNAME"];
        }

        //科目取得
        $opt_sub = array();
        $year = (strlen($model->field["YEAR"]) ? $model->field["YEAR"] : $opt_year[0]["value"]);
        $query = knje062Query::selectSubclassQuery($year);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $opt_sub[]  = array("label" => $row["SUBCLASSNAME"],
                                "value" => $row["SUBCLASSCD"]);
        }
        
        $result->free();
        Query::dbCheckIn($db);
        //科目
        $objForm->ae( array("type"      =>      "select",
                            "name"      =>      "SUBCLASSCD",
                            "size"      =>      "1",
                            "value"     =>      ($model->cmd == "subclasscd") ? $model->field["SUBCLASSCD"] : $Row["SUBCLASSCD"],
                            "extrahtml" =>      "OnChange=\"btn_submit('subclasscd');\"",
                            "options"   =>      $opt_sub));
        $arg["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");

        //学年(年次)
        $objForm->ae( array("type"        => "text",
                            "name"        => "ANNUAL",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: right\" onblur=\"this.value = toInteger(this.value);\"",
                            "value"       => $Row["ANNUAL"] ));

        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

        //評定
        $objForm->ae( array("type"        => "text",
                            "name"        => "VALUATION",
                            "size"        => 2,
                            "maxlength"   => 1,
                            "extrahtml"   => "STYLE=\"text-align: right\" onblur=\"this.value = toInteger(this.value);\"",
                            "value"       => $Row["VALUATION"] ));

        $arg["VALUATION"] = $objForm->ge("VALUATION");

        //修得単位
        $objForm->ae( array("type"        => "text",
                            "name"        => "GET_CREDIT",
                            "size"        => 3,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: right\" onblur=\"this.value = toInteger(this.value);\"",
                            "value"       => $Row["GET_CREDIT"] ));

        $arg["GET_CREDIT"] = $objForm->ge("GET_CREDIT");

        //増加単位
        $objForm->ae( array("type"        => "text",
                            "name"        => "ADD_CREDIT",
                            "size"        => 3,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: right\" onblur=\"this.value = toInteger(this.value);\"",
                            "value"       => $Row["ADD_CREDIT"] ));

        $arg["ADD_CREDIT"] = $objForm->ge("ADD_CREDIT");

        //備考
        $objForm->ae( array("type"        => "text",
                            "name"        => "REMARK",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "value"       => $Row["REMARK"] ));

        $arg["REMARK"] = $objForm->ge("REMARK");

        //追加ボタンを作成する
        $objForm->ae( array("type"      =>      "button",
                            "name"      =>      "btn_add",
                            "value"     =>      "追 加",
                            "extrahtml" =>      "onclick=\"return btn_submit('add');\""));
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type"      =>      "button",
                            "name"      =>      "btn_update",
                            "value"     =>      "更 新",
                            "extrahtml" =>      "onclick=\"return btn_submit('update');\""));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type"      =>   "button",
                            "name"      =>      "btn_del",
                            "value"     =>      "削 除",
                            "extrahtml" =>      "onclick=\"return btn_submit('delete2');\""));
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type"      =>      "button",
                            "name"      =>      "btn_reset",
                            "value"     =>      "取 消",
                            "extrahtml" =>      "onclick=\"return Btn_reset('edit');\""));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"      =>      "button",
                            "name"      =>      "btn_end",
                            "value"     =>      "終 了",
                            "extrahtml" =>      "onclick=\"closeWin();\""));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      =>      "hidden",
                            "name"      =>      "cmd"));

        //教科名
        $objForm->ae( array("type"      =>      "hidden",
                            "name"      =>      "CLASSNAME",
                            "value"     =>      $Row["CLASSNAME"]));

        $arg["finish"]  = $objForm->get_finish();
        if (isset($model->message)){
            $arg["reload"]  = "window.open('knje062index.php?cmd=list&SCHREGNO=$model->schregno','top_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje062Form2.html", $arg);
    }
}        

?>
