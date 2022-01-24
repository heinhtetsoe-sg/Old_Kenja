<?php

require_once('for_php7.php');

class knjh332Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh332index.php", "", "edit");

       //デフォルト値
    	if($model->groupcd ==""){
        	$row = knjh332Query::getFirst_GroupKey();
        	$model->groupcd = sprintf("%08d",$row["GROUPCD"]);
    	}

        $db     = Query::dbCheckOut();

        //職員名表示
		$result = $db->query(knjh332Query::getStaff());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
        	$staffname = "職員名：". $row["STAFFNAME_SHOW"];
        }

        $arg["STAFFNAME"] = $staffname;

        //警告メッセージを表示しない場合
        if (!isset($model->warning))
        {
            $Row = knjh332Query::getRow($model->targetcd,$model->groupcd);
        } else {
            $Row =& $model->field;
        }

        Query::dbCheckIn($db);

        //目標値コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "TARGETCD",
                            "size"        => 8,
                            "maxlength"   => 8,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["TARGETCD"] ));

        $arg["data"]["TARGETCD"] = $objForm->ge("TARGETCD");

        //目標値名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "TARGETNAME1",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $Row["TARGETNAME1"] ));

        $arg["data"]["TARGETNAME1"] = $objForm->ge("TARGETNAME1");

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"     	  => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

		//終了ボタン
		$objForm->ae( array("type" 		  => "button",
                     		"name"        => "btn_end",
                    		"value"       => "終 了",
                    		"extrahtml"   => "onclick=\"closeWin();\"" ) );

		$arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"        => "hidden",
                            "name"        => "cmd"
                            ) );

        $objForm->ae( array("type"        => "hidden",
                            "name"        => "UPDATED",
                            "value"       => $Row["UPDATED"]
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjh332index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh332Form2.html", $arg); 
    }
}
?>
