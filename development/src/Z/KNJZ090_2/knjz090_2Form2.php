<?php

require_once('for_php7.php');

class knjz090_2Form2
{
    function main(&$model)
    {
    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("edit", "POST", "knjz090_2index.php", "", "edit");

    //警告メッセージを表示しない場合
    if (!isset($model->warning) && isset($model->finschoolcd))
    {   $Row = knjz090_2Query::getRow($model->finschoolcd);
    } else {
        $Row =& $model->field;
    }

    $db        = Query::dbCheckOut();

    //出身学校
    $result    = $db->query(knjz090_2Query::getFinschoolDistcd());
    $opt1      = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt1[] = array("label" => $row["NAMECD2"]."  ".$row["NAME1"],
                        "value" => $row["NAMECD2"]);
    }
    $result->free();
    //教育委員会コード設定
    $result    = $db->query(knjz090_2Query::getEdboard());
    $opt       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt[] = array("label" => $row["EDBOARDCD"]."  ".$row["EDBOARDNAME"], 
                       "value" => $row["EDBOARDCD"]);
    }
    $result->free();
    //地区コード
    $result = $db->query(knjz090_2Query::getDistinct());
    $opt2   = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt2[] = array("label" => $row["NAMECD2"]."  ".$row["NAME1"], 
                        "value" => $row["NAMECD2"]);
    }
    $result->free();
    Query::dbCheckIn($db);
    
    //出身学校コード
    $objForm->ae( array("type"        => "text",
                        "name"        => "FINSCHOOLCD",
                        "size"        => 7,
                        "maxlength"   => 7,
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                        "value"       => $Row["FINSCHOOLCD"]));

    $arg["data"]["FINSCHOOLCD"] = $objForm->ge("FINSCHOOLCD");

    //出身学校学区コード
    $objForm->ae( array("type"        => "select",
                        "name"        => "FINSCHOOL_DISTCD",
                        "size"        => "1",
                        "extrahtml"   => "",
                        "value"       => $Row["FINSCHOOL_DISTCD"],
                        "options"     => $opt1 ));
    $arg["data"]["FINSCHOOL_DISTCD"] = $objForm->ge("FINSCHOOL_DISTCD");

    //出身学校名
    $objForm->ae( array("type"        => "text",
                        "name"        => "FINSCHOOL_NAME",
                        "size"        => 50,
                        "maxlength"   => 75,
                        "extrahtml"   => "",
                        "value"       => $Row["FINSCHOOL_NAME"] ));

    $arg["data"]["FINSCHOOL_NAME"] = $objForm->ge("FINSCHOOL_NAME");

    //出身学校カナ
    $objForm->ae( array("type"        => "text",
                        "name"        => "FINSCHOOL_KANA",
                        "size"        => 50,
                        "maxlength"   => 75,
                        "extrahtml"   => "",
                        "value"       => $Row["FINSCHOOL_KANA"] ));

    $arg["data"]["FINSCHOOL_KANA"] = $objForm->ge("FINSCHOOL_KANA");
    
    //出身学校長氏名
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRINCNAME",
                        "size"        => 30,
                        "maxlength"   => 60,
                        "extrahtml"   => "",
                        "value"       => $Row["PRINCNAME"] ));

    $arg["data"]["PRINCNAME"] = $objForm->ge("PRINCNAME");

    //出身学校長氏名表示用
    $objForm->ae( array("type"        => "text",
                        "name"        => "PRINCNAME_SHOW",
                        "size"        => 20,
                        "maxlength"   => 30,
                        "extrahtml"   => "",
                        "value"       => $Row["PRINCNAME_SHOW"] ));

    $arg["data"]["PRINCNAME_SHOW"] = $objForm->ge("PRINCNAME_SHOW");

    //出身学校長氏名かな
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

    //出身学校郵便番号
    $arg["data"]["FINSCHOOL_ZIPCD"] = View::popUpZipCode($objForm, "FINSCHOOL_ZIPCD", $Row["FINSCHOOL_ZIPCD"],"FINSCHOOL_ADDR1");
    
    //出身学校住所１
    $objForm->ae( array("type"        => "text",
                        "name"        => "FINSCHOOL_ADDR1",
                        "size"        => 50,
                        "maxlength"   => 75,
                        "extrahtml"   => "",
                        "value"       => $Row["FINSCHOOL_ADDR1"] ));

    $arg["data"]["FINSCHOOL_ADDR1"] = $objForm->ge("FINSCHOOL_ADDR1");
    
    //出身学校住所２
    $objForm->ae( array("type"        => "text",
                        "name"        => "FINSCHOOL_ADDR2",
                        "size"        => 50,
                        "maxlength"   => 75,
                        "extrahtml"   => "",
                        "value"       => $Row["FINSCHOOL_ADDR2"] ));

    $arg["data"]["FINSCHOOL_ADDR2"] = $objForm->ge("FINSCHOOL_ADDR2");

    //出身学校電話番号
    $objForm->ae( array("type"        => "text",
                        "name"        => "FINSCHOOL_TELNO",
                        "size"        => 14,
                        "maxlength"   => 14,
                        "extrahtml"   => "",
                        "value"       => $Row["FINSCHOOL_TELNO"] ));

    $arg["data"]["FINSCHOOL_TELNO"] = $objForm->ge("FINSCHOOL_TELNO");

    //出身学校FAX番号
    $objForm->ae( array("type"        => "text",
                        "name"        => "FINSCHOOL_FAXNO",
                        "size"        => 14,
                        "maxlength"   => 14,
                        "extrahtml"   => "",
                        "value"       => $Row["FINSCHOOL_FAXNO"] ));

    $arg["data"]["FINSCHOOL_FAXNO"] = $objForm->ge("FINSCHOOL_FAXNO");

    //教育委員会コード
    $objForm->ae( array("type"        => "select",
                        "name"        => "EDBOARDCD",
                        "size"        => "1",
                        "extrahtml"   => "",
                        "value"       => $Row["EDBOARDCD"],
                        "options"     => $opt ));

    $arg["data"]["EDBOARDCD"] = $objForm->ge("EDBOARDCD");

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
    $link = REQUESTROOT."/Z/KNJZ090/knjz090index.php";
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
        $arg["reload"]  = "parent.left_frame.location.href='knjz090_2index.php?cmd=list';";
    }
                                
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjz090_2Form2.html", $arg);
    }
} 
?>
