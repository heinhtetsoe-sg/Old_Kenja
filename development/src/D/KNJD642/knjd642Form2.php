<?php

require_once('for_php7.php');

class knjd642Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd642index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = knjd642Query::getRow($model->year,$model->bu_cd,$model->ka_cd);
        }else{
            $Row =& $model->field;
        }

        //学部コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "BU_CD",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toAlphanumeric(this.value)\"",
                            "value"       => $Row["BU_CD"] ));
        $arg["data"]["BU_CD"] = $objForm->ge("BU_CD");

        //学部名
        $objForm->ae( array("type"        => "text",
                            "name"        => "BU_NAME",
                            "size"        => 40,
                            "maxlength"   => 80,
                            "extrahtml"   => "",
                            "value"       => $Row["BU_NAME"] ));

        $arg["data"]["BU_NAME"] = $objForm->ge("BU_NAME");
        
        //学部略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "BU_ABBV",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "extrahtml"   => "",
                            "value"       => $Row["BU_ABBV"] ));

        $arg["data"]["BU_ABBV"] = $objForm->ge("BU_ABBV");
        
        //学科コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "KA_CD",
                            "size"        => 2,
                            "maxlength"   => 1,
                            "extrahtml"   => "onblur=\"this.value=toAlphanumeric(this.value)\"",
                            "value"       => $Row["KA_CD"] ));
        $arg["data"]["KA_CD"] = $objForm->ge("KA_CD");

        //学科名
        $objForm->ae( array("type"        => "text",
                            "name"        => "KA_NAME",
                            "size"        => 40,
                            "maxlength"   => 80,
                            "extrahtml"   => "",
                            "value"       => $Row["KA_NAME"] ));

        $arg["data"]["KA_NAME"] = $objForm->ge("KA_NAME");
        
        //学部略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "KA_ABBV",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "extrahtml"   => "",
                            "value"       => $Row["KA_ABBV"] ));

        $arg["data"]["KA_ABBV"] = $objForm->ge("KA_ABBV");
        
        //学科記号
        $objForm->ae( array("type"        => "text",
                            "name"        => "KA_MARK",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "",
                            "value"       => $Row["KA_MARK"] ));

        $arg["data"]["KA_MARK"] = $objForm->ge("KA_MARK");

	    //印刷順序ラジオの設定
	    $opt_div   = array();
	    $opt_div[] = 1;
	    $opt_div[] = 2;

	    if (!$Row["DIV"]){
            $Row["DIV"] = 1;
	    }

	    $objForm->ae( array("type"       => "radio",
    	                    "name"       => "DIV",
            	            "value"      => $Row["DIV"],
                    	    "options"    => $opt_div));

	    $arg["data"]["DIV1"] = $objForm->ge("DIV",1);
	    $arg["data"]["DIV2"] = $objForm->ge("DIV",2);

        //推薦枠
        $objForm->ae( array("type"        => "text",
                            "name"        => "FRAME",
                            "size"        => 4,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["FRAME"] ));

        $arg["data"]["FRAME"] = $objForm->ge("FRAME");

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
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
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

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjd642index.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd642Form2.html", $arg);
    }
}
?>
