<?php

require_once('for_php7.php');

class knjd020form2
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd020index.php", "", "edit");

        if (isset($model->field["SEMESTER"]) && isset($model->field["chairSel"])) {
            $temp = knjd020Query::getHr_class($model);
            $arg["data"]["HR_NAMEABBV"] = $temp["hr_nameabbv"];
            $arg["data"]["CHAIRNAME"]   = $temp["chairname"];
        } else {
            $arg["data"]["HR_NAMEABBV"] = "";
            $arg["data"]["CHAIRNAME"]   = "";
        }
        
        if ($model->field["chairSel"]!="" && !$model->isWarning()) {
            $Row =& knjd020Query::getRow($model);
        } else {
            $Row =& $model->new_field;
        }
        
        //実施期間エラーフラグが1の時にメッセージを表示
        if ($model->term_err_flg != "0") {
            $arg["jscript"] = "ConfirmOnTermError('$model->term_err_flg');";
            $Row =& $model->new_field;
        }
        
        //削除時にTESTSCORE_DATにレコードがあればメッセージを表示
        if ($model->delete_flg != "0") {
            $arg["jscript"] = "ConfirmOnDeleteError();";
            $Row =& $model->new_field;
        } 
        
        //テスト項目名;
        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTCD",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $Row["TESTCD"],
                            "options"     => knjd020Query::getTestItem($model) ) );

        $arg["data"]["TESTCD"] = $objForm->ge("TESTCD");

        //実施日
        $arg["data"]["OPERATION_DATE"] = View::popUpCalendar($objForm, "OPERATION_DATE", str_replace("-","/",$Row["OPERATION_DATE"]));

        //満点
        $objForm->ae( array("type"        => "text",
                            "name"        => "PERFECT",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => ($Row["PERFECT"]!="") ? $Row["PERFECT"] : 100));

        $arg["data"]["PERFECT"] = $objForm->ge("PERFECT");

        //重み
        $objForm->ae( array("type"        => "text",
                            "name"        => "RATE",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => ($Row["RATE"]!="") ? $Row["RATE"] : 100));

        $arg["data"]["RATE"] = $objForm->ge("RATE");
    
        //全てに適応
        $checked = ($model->new_field["ALL"]==1) ? true : false;
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "ALL",
                            "checked"     => $checked,
                            "value"       => "1"));

        $arg["data"]["ALL"] = $objForm->ge("ALL");

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add_check');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update_check');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete_check');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return Btn_reset('clear');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
                    
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

       $objForm->ae( array("type"      => "hidden",
                           "name"      => "GTREDATA") );
                                                
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "chairSel",
                            "value"     => $model->field["chairSel"]) );                    

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]) ); 
                    
        $arg["finish"]  = $objForm->get_finish();

       if (VARS::get("cmd") != "edit" && !isset($model->warning)){
           if ($model->cmd != "change" && $model->cmd != "clear"){
               $arg["reload"]  = "parent.right_frame.location.href='knjd020index.php?cmd=main';";
           }
       }
                                
        View::toHTML($model, "knjd020Form2.html", $arg); 
    }
}   
?>
