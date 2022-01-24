<?php

require_once('for_php7.php');

class knjd010Form2
{
    function main(&$model)
    {
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("edit", "POST", "knjd010index.php", "", "edit");

    //警告メッセージを表示しない場合
    if (!isset($model->warning))
    {
        $Row = knjd010Query::getRow($model);
    } else {
        $Row =& $model->field;
    }

    //科目名コンボボックスの中身を作成------------------------------
    $db     = Query::dbCheckOut();

    $query  = knjd010Query::getSubclassName($model);
    $result = $db->query($query);
    $opt_left = $opt_right = array();
    //対象科目を配列に格納
    $selectdata = explode(",", $model->selectdata);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $tmp = array("label" => htmlspecialchars(($row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"])),
                     "value" => $row["SUBCLASSCD"]);
        //警告メッセージを表示しない場合
        if ($model->subclasscd == $row["SUBCLASSCD"] || (is_array($selectdata) && in_array($row["SUBCLASSCD"], $selectdata))){
            $opt_left[]  = $tmp;
        }else{
            $opt_right[] = $tmp;
        }
    }
    //テスト項目種別名コンボボックスの中身を作成------------------------------
    $query  = knjd010Query::getTestKindName($model);
    $result = $db->query($query);
    $opt_testKind = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testKind[] = array("label" => htmlspecialchars(($row["TESTKINDCD"]." ".$row["TESTKINDNAME"])),
                                "value" => $row["TESTKINDCD"]);
    }

    //対象科目
    $objForm->ae( array("type"        => "select",
                        "name"        => "L_SUBCLASSCD",
                        "size"        => "20",
                        "value"       => "left",
                        "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                        "options"     => $opt_left));

    //名称
    $objForm->ae( array("type"        => "select",
                        "name"        => "R_SUBCLASSCD",
                        "size"        => "20",
                        "value"       => "left",
                        "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                        "options"     => $opt_right));

    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_add_all",
                        "value"       => "≪",
                        "extrahtml"   => "onclick=\"return move('sel_add_all');\"" ) );

    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_add",
                        "value"       => "＜",
                        "extrahtml"   => "onclick=\"return move('left');\"" ) );

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_del",
                        "value"       => "＞",
                        "extrahtml"   => "onclick=\"return move('right');\"" ) );

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_del_all",
                        "value"       => "≫",
                        "extrahtml"   => "onclick=\"return move('sel_del_all');\"" ) );

    $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("L_SUBCLASSCD"),
                               "RIGHT_PART"  => $objForm->ge("R_SUBCLASSCD"),
                               "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                               "SEL_ADD"     => $objForm->ge("sel_add"),
                               "SEL_DEL"     => $objForm->ge("sel_del"),
                               "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

    //テスト項目種別
    $objForm->ae( array("type"        => "select",
                        "name"        => "TESTKINDCD",
                        "value"       => $Row["TESTKINDCD"],
                        "options"     => $opt_testKind
                        ));
    $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

    $result->free();
    Query::dbCheckIn($db);

    //テスト項目コード
    $objForm->ae( array("type"        => "text",
                        "name"        => "TESTITEMCD",
                        "size"        => 3,
                        "maxlength"   => 2,
                        "extrahtml"   => "STYLE=\"text-align: right\"",
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

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "selectdata"
                        ) );
    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "UPDATED",
                        "value"     => $Row["UPDATED"]
                        ) );

    $arg["finish"]  = $objForm->get_finish();

    if (VARS::get("cmd") != "edit"){
        $arg["reload"]  = "window.open('knjd010index.php?cmd=list','left_frame');";
    }

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjd010Form2.html", $arg);
    }
}
?>
