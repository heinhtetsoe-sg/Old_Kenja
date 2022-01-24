<?php

require_once('for_php7.php');

class knjz050kForm2
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz050kindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->cd)) {
            $Row = knjz050kQuery::getRow($model->cd);
        } else {
            $Row =& $model->field;
        }

        //銀行コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "BANKCD",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["BANKCD"]));

        $arg["data"]["BANKCD"] = $objForm->ge("BANKCD");

        //支店コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "BRANCHCD",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["BRANCHCD"]));

        $arg["data"]["BRANCHCD"] = $objForm->ge("BRANCHCD");

        //銀行名
        $objForm->ae( array("type"        => "text",
                            "name"        => "BANKNAME",
                            "size"        => 30,
                            "maxlength"   => 45,
                            "extrahtml"   => "",
                            "value"       => $Row["BANKNAME"] ));

        $arg["data"]["BANKNAME"] = $objForm->ge("BANKNAME");

        //銀行名カナ
        $objForm->ae( array("type"        => "text",
                            "name"        => "BANKNAME_KANA",
                            "size"        => 15,	//2005.11.29 alp m-yama
                            "maxlength"   => 15,	//2005.11.29 alp m-yama
                            "extrahtml"   => "",
                            "value"       => $Row["BANKNAME_KANA"] ));

        $arg["data"]["BANKNAME_KANA"] = $objForm->ge("BANKNAME_KANA");

        //支店名
        $objForm->ae( array("type"        => "text",
                            "name"        => "BRANCHNAME",
                            "size"        => 30,
                            "maxlength"   => 45,
                            "extrahtml"   => "",
                            "value"       => $Row["BRANCHNAME"] ));

        $arg["data"]["BRANCHNAME"] = $objForm->ge("BRANCHNAME");

        //支店名カナ
        $objForm->ae( array("type"        => "text",
                            "name"        => "BRANCHNAME_KANA",
                            "size"        => 15,	//2005.11.29 alp m-yama
                            "maxlength"   => 15,	//2005.11.29 alp m-yama
                            "extrahtml"   => "",
                            "value"       => $Row["BRANCHNAME_KANA"] ));

        $arg["data"]["BRANCHNAME_KANA"] = $objForm->ge("BRANCHNAME_KANA");

        //郵便番号
        $arg["data"]["BANKZIPCD"] = View::popUpZipCode($objForm, "BANKZIPCD", $Row["BANKZIPCD"],"BANKADDR1");
    
        //住所１
        $objForm->ae( array("type"        => "text",
                            "name"        => "BANKADDR1",
                            "size"        => 50,
                            "maxlength"   => 75,
                            "extrahtml"   => "",
                            "value"       => $Row["BANKADDR1"] ));

        $arg["data"]["BANKADDR1"] = $objForm->ge("BANKADDR1");
    
        //住所２
        $objForm->ae( array("type"        => "text",
                            "name"        => "BANKADDR2",
                            "size"        => 50,
                            "maxlength"   => 75,
                            "extrahtml"   => "",
                            "value"       => $Row["BANKADDR2"] ));

        $arg["data"]["BANKADDR2"] = $objForm->ge("BANKADDR2");

        //電話番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "BANKTELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "",
                            "value"       => $Row["BANKTELNO"] ));

        $arg["data"]["BANKTELNO"] = $objForm->ge("BANKTELNO");

        //FAX番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "BANKFAXNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "",
                            "value"       => $Row["BANKFAXNO"] ));

        $arg["data"]["BANKFAXNO"] = $objForm->ge("BANKFAXNO");

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

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
    
        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz050kindex.php?cmd=list';";
        }
                                
        View::toHTML($model, "knjz050kForm2.html", $arg);
    }
} 
?>
