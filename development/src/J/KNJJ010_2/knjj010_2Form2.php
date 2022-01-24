<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：取消押下処理修正                         山城 2004/11/17 */
/********************************************************************/

class knjj010_2Form2
{
    function main(&$model){

    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("edit", "POST", "knjj010_2index.php", "", "edit");

    //DB接続
    $db = Query::dbCheckOut();

    //警告メッセージを表示しない場合
    if (!isset($model->warning))
    {
        $Row = $db->getRow(knjj010_2Query::getRow($model, $model->clubcd), DB_FETCHMODE_ASSOC);
    } else {
        $Row =& $model->field;
        $Row["CLUBCD"] = $model->field["CLUBCD"];
    }

    //部クラブコード
    $objForm->ae( array("type"        => "text",
                        "name"        => "CLUBCD",
                        "size"        => 5,
                        "maxlength"   => 4,
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                        "value"       => $Row["CLUBCD"] ));

    $arg["data"]["CLUBCD"] = $objForm->ge("CLUBCD");

    //部クラブ名
    $objForm->ae( array("type"        => "text",
                        "name"        => "CLUBNAME",
                        "size"        => 30,
                        "maxlength"   => 45,
                        "extrahtml"   => "",
                        "value"       => $Row["CLUBNAME"] ));

    $arg["data"]["CLUBNAME"] = $objForm->ge("CLUBNAME");

    //設立日付
    $arg["data"]["SDATE"]=View::popUpCalendar($objForm, "SDATE", str_replace("-","/",$Row["SDATE"]),"");

    //活動場所
    $objForm->ae( array("type"        => "text",
                        "name"        => "ACTIVITY_PLACE",
                        "size"        => 30,
                        "maxlength"   => 30,
                        "extrahtml"   => "",
                        "value"       => $Row["ACTIVITY_PLACE"] ));

    $arg["data"]["ACTIVITY_PLACE"] = $objForm->ge("ACTIVITY_PLACE");

    //部室割り当て
    $objForm->ae( array("type"        => "text",
                        "name"        => "CLUBROOM_ASSIGN",
                        "size"        => 30,
                        "maxlength"   => 30,
                        "extrahtml"   => "",
                        "value"       => $Row["CLUBROOM_ASSIGN"] ));

    $arg["data"]["CLUBROOM_ASSIGN"] = $objForm->ge("CLUBROOM_ASSIGN");

    //種目登録ボタン
    $extra  = " onclick=\"return btn_submit('subform1');\"";
    $extra .= ($model->clubcd) ? "" : " disabled";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "種目登録", $extra);

    //種目表示
    $club_item = "";
    $result = $db->query(knjj010_2Query::getClubItemDat($model, $model->clubcd));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $club_item .= ($club_item != "" && $row["ITEMNAME"]) ? "," : "";
        $club_item .= $row["ITEMNAME"];
    }
    $arg["data"]["CLUB_ITEM"] = $club_item;

    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_add",
                        "value"       => "登 録",
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
    $objForm->ae( array("type"        => "reset",
                        "name"        => "btn_reset",
                        "value"       => "取 消",
                        "extrahtml"   => "onclick=\"return Btn_reset('edit');\"" ) );/* NO001 */

    $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

    //終了ボタンを作成する
    $link = REQUESTROOT."/J/KNJJ010/knjj010index.php";
    $objForm->ae( array("type" => "button",
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

    //DB切断
    Query::dbCheckIn($db);

    $arg["finish"]  = $objForm->get_finish();
    
    if (VARS::get("cmd") != "edit"){
        $arg["reload"]  = "parent.left_frame.location.href='knjj010_2index.php?cmd=list';";
    }
                                
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjj010_2Form2.html", $arg); 
}
}
?>
