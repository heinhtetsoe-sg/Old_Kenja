<?php

require_once('for_php7.php');

class knjz091_2Form2
{
    function main(&$model)
    {
    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("edit", "POST", "knjz091_2index.php", "", "edit");

    //警告メッセージを表示しない場合
    if (!isset($model->warning) && isset($model->finschoolcd))
    {   $Row = knjz091_2Query::getRow($model->finschoolcd);
    } else {
        $Row =& $model->field;
    }

#    //教育委員会コード設定
   $db        = Query::dbCheckOut();
#   $result    = $db->query(knjz091_2Query::getEdboard());
   $opt       = array();
#    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
#    {
#        $opt[] = array("label" => $row["EDBOARDCD"]."  ".$row["EDBOARDNAME"], 
#                       "value" => $row["EDBOARDCD"]);
#    }
#    $result->free();
    //地区コード
    $result = $db->query(knjz091_2Query::getDistinct());
    $opt2   = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt2[] = array("label" => $row["NAMECD2"]."  ".$row["NAME1"], 
                       "value"  => $row["NAMECD2"]);
    }
    
    $result->free();
    Query::dbCheckIn($db);
    
    //出身塾コード
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRISCHOOLCD",
                        "size"        => 7,
                        "maxlength"   => 7,
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                        "value"       => $Row["PRISCHOOLCD"]));

    $arg["data"]["PRISCHOOLCD"] = $objForm->ge("PRISCHOOLCD");

    //出身塾名
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRISCHOOL_NAME",
                        "size"        => 50,
                        "maxlength"   => 75,
                        "extrahtml"   => "",
                        "value"       => $Row["PRISCHOOL_NAME"] ));

    $arg["data"]["PRISCHOOL_NAME"] = $objForm->ge("PRISCHOOL_NAME");

    //出身塾カナ
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRISCHOOL_KANA",
                        "size"        => 50,
                        "maxlength"   => 75,
                        "extrahtml"   => "",
                        "value"       => $Row["PRISCHOOL_KANA"] ));

    $arg["data"]["PRISCHOOL_KANA"] = $objForm->ge("PRISCHOOL_KANA");
    
    //出身塾長氏名
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRINCNAME",
                        "size"        => 30,
                        "maxlength"   => 60,
                        "extrahtml"   => "",
                        "value"       => $Row["PRINCNAME"] ));

    $arg["data"]["PRINCNAME"] = $objForm->ge("PRINCNAME");

    //出身塾長氏名表示用
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRINCNAME_SHOW",
                        "size"        => 20,
                        "maxlength"   => 30,
                        "extrahtml"   => "",
                        "value"       => $Row["PRINCNAME_SHOW"] ));

    $arg["data"]["PRINCNAME_SHOW"] = $objForm->ge("PRINCNAME_SHOW");

    //出身塾長氏名かな
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRINCKANA",
                        "size"        => 80,
                        "maxlength"   => 120,
                        "extrahtml"   => "",
                        "value"       => $Row["PRINCKANA"] ));

    $arg["data"]["PRINCKANA"] = $objForm->ge("PRINCKANA");

    //地区コード
    $objForm->ae( array("type"        => "select",
                        "name"        => "DISTRICTCD",
                        "size"        => "1",
                        "extrahtml"   => "",
                        "value"       => $Row["DISTRICTCD"],
                        "options"     => $opt2 ));

    $arg["data"]["DISTRICTCD"] = $objForm->ge("DISTRICTCD");

    //出身塾郵便番号
    $arg["data"]["PRISCHOOL_ZIPCD"] = View::popUpZipCode($objForm, "PRISCHOOL_ZIPCD", $Row["PRISCHOOL_ZIPCD"],"PRISCHOOL_ADDR1");
    
    //出身塾住所１
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRISCHOOL_ADDR1",
                        "size"        => 50,
                        "maxlength"   => 90,
                        "extrahtml"   => "",
                        "value"       => $Row["PRISCHOOL_ADDR1"] ));

    $arg["data"]["PRISCHOOL_ADDR1"] = $objForm->ge("PRISCHOOL_ADDR1");
    
    //出身塾住所２
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRISCHOOL_ADDR2",
                        "size"        => 50,
                        "maxlength"   => 90,
                        "extrahtml"   => "",
                        "value"       => $Row["PRISCHOOL_ADDR2"] ));

    $arg["data"]["PRISCHOOL_ADDR2"] = $objForm->ge("PRISCHOOL_ADDR2");

    //出身塾電話番号
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRISCHOOL_TELNO",
                        "size"        => 14,
                        "maxlength"   => 14,
                        "extrahtml"   => "",
                        "value"       => $Row["PRISCHOOL_TELNO"] ));

    $arg["data"]["PRISCHOOL_TELNO"] = $objForm->ge("PRISCHOOL_TELNO");

    //出身塾FAX番号
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRISCHOOL_FAXNO",
                        "size"        => 14,
                        "maxlength"   => 14,
                        "extrahtml"   => "",
                        "value"       => $Row["PRISCHOOL_FAXNO"] ));

    $arg["data"]["PRISCHOOL_FAXNO"] = $objForm->ge("PRISCHOOL_FAXNO");

    //塾グループ・・・塾コード（親）
    $objForm->ae( array("type"        => "text",
                        "name"        => "GRP_PRISCHOOLCD",
                        "size"        => 7,
                        "maxlength"   => 7,
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                        "value"       => $Row["GRP_PRISCHOOLCD"]));

    $arg["data"]["GRP_PRISCHOOLCD"] = $objForm->ge("GRP_PRISCHOOLCD");

#    //教育委員会コード
#    $objForm->ae( array("type"        => "select",
#                        "name"        => "EDBOARDCD",
#                        "size"        => "1",
#                        "extrahtml"   => "",
#                        "value"       => $Row["EDBOARDCD"],
#                        "options"     => $opt ));
# 
#    $arg["data"]["EDBOARDCD"] = $objForm->ge("EDBOARDCD");

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
    $link = REQUESTROOT."/Z/KNJZ091/knjz091index.php";
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_back",
                        "value"       => "戻 る",
                        "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );

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
        $arg["reload"]  = "parent.left_frame.location.href='knjz091_2index.php?cmd=list';";
    }
                                
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjz091_2Form2.html", $arg);
    }
} 
?>
