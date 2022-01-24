<?php

require_once('for_php7.php');

class knjz380Form2
{
    function main(&$model)
    {
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("edit", "POST", "knjz380index.php", "", "edit");

    //警告メッセージを表示しない場合
    if (!isset($model->warning))
    {
        $Row = knjz380Query::getRow($model);
    } else {
        $Row =& $model->field;
    }

    //テスト項目種別名コンボボックスの中身を作成------------------------------
    $db     = Query::dbCheckOut();

    $query  = knjz380Query::getTestKindName($model);
    $result = $db->query($query);
    $opt_testKind = array();
    $opt_testKind[] = array("label" => "","value" => "");
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testKind[] = array("label" => htmlspecialchars(($row["TESTKINDCD"]." ".$row["TESTKINDNAME"])),
                                "value" => $row["TESTKINDCD"]);
    }

    //テスト項目種別
    $objForm->ae( array("type"        => "select",
                        "name"        => "TESTKINDCD",
                        "value"       => $Row["TESTKINDCD"],
                        "extrahtml"   => "",//NO001
//                        "extrahtml"   => "onchange=\"checktest(this.value);\"",
                        "options"     => $opt_testKind
                        ));
    $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

    $result->free();
    Query::dbCheckIn($db);

    //テスト項目コード
/***NO001-->
	if ($model->testkindcd == "01" || $Row["TESTKINDCD"] == "01") $Row["TESTITEMCD"] = "01";
	if ($model->testkindcd == "02" || $Row["TESTKINDCD"] == "02") $Row["TESTITEMCD"] = "01";
	$dis_item = ($model->testkindcd == "01" || $Row["TESTKINDCD"] == "01" || $model->testkindcd == "02" || $Row["TESTKINDCD"] == "02") ? "disabled" : "";
<--NO001***/
    $objForm->ae( array("type"        => "text",
                        "name"        => "TESTITEMCD",
                        "size"        => 3,
                        "maxlength"   => 2,
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",//NO001
//                        "extrahtml"   => $dis_item,
                        "value"       => $Row["TESTITEMCD"] ));

    $arg["data"]["TESTITEMCD"] = $objForm->ge("TESTITEMCD");

    //テスト項目名
    $objForm->ae( array("type"        => "text",
                        "name"        => "TESTITEMNAME",
                        "size"        => 20,
                        "maxlength"   => 30,
                        "extrahtml"   => "",
                        "value"       => $Row["TESTITEMNAME"] ));

    $arg["data"]["TESTITEMNAME"] = $objForm->ge("TESTITEMNAME");

    //集計フラグ 1:集計する 0:集計しない
	$checked_flg = ($Row["COUNTFLG"] == "1") ? "checked" : "";
    $objForm->ae( array("type"        => "checkbox",
                        "name"        => "COUNTFLG",
                        "extrahtml"   => $checked_flg,
                        "value"       => "1"));
    $arg["data"]["COUNTFLG"] = $objForm->ge("COUNTFLG");

    //追加ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_add",
                        "value"       => "追 加",
                        "extrahtml"   => "onclick=\"return doSubmit('add');\"" ) );

    $arg["button"]["btn_add"] = $objForm->ge("btn_add");

    //修正ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_udpate",
                        "value"       => "更 新",
                        "extrahtml"   => "onclick=\"return doSubmit('update');\"" ) );

    $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

    //削除ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_del",
                        "value"       => "削 除",
                        "extrahtml"   => "onclick=\"return doSubmit('delete');\"" ) );

    $arg["button"]["btn_del"] = $objForm->ge("btn_del");

    //クリアボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_reset",
                        "value"       => "取 消",
                        "extrahtml"   => "onclick=\"return doSubmit('reset')\"" ) );

    $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

    //終了ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

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
        $arg["reload"]  = "window.open('knjz380index.php?cmd=list','left_frame');";
    }

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjz380Form2.html", $arg);
    }
}
?>
