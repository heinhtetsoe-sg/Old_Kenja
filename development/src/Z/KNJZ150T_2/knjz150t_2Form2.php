<?php

require_once('for_php7.php');

class knjz150t_2Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjz150t_2Form2", "POST", "knjz150t_2index.php", "", "knjz150t_2Form2");

        //警告メッセージを表示しない場合
        if (!isset($model->warning))
        {
            $Row = knjz150t_2Query::getRow($model->textbookcd);
        } else {
            $Row =& $model->field;
        }

        //教科書コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "TEXTBOOKCD",
                            "size"        => 12,
                            "maxlength"   => 12,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["TEXTBOOKCD"] ));

        $arg["data"]["TEXTBOOKCD"] = $objForm->ge("TEXTBOOKCD");

        //教科書区分
		$opt_textdiv = array();
        $db     = Query::dbCheckOut();
		$query  = knjz150t_2Query::getName();
		$result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             //レコードを連想配列のまま配列$arg[data]に追加していく。 
             $opt_textdiv[] = array("label" => $row["NAMECD2"]."　".$row["NAME1"],
			 						"value" => $row["NAMECD2"]);
        }
        $result->free();
        Query::dbCheckIn($db);
        $objForm->ae( array("type"        => "select",
                            "name"        => "TEXTBOOKDIV",
                            "size"        => 1,
                            "value"       => $Row["TEXTBOOKDIV"],
							"extrahtml"   => "",
							"options"     => $opt_textdiv ));

        $arg["data"]["TEXTBOOKDIV"] = $objForm->ge("TEXTBOOKDIV");

        //教科書名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "TEXTBOOKNAME",
                            "size"        => 60,
                            "maxlength"   => 60,
                            "extrahtml"   => "",
                            "value"       => $Row["TEXTBOOKNAME"] ));

        $arg["data"]["TEXTBOOKNAME"] = $objForm->ge("TEXTBOOKNAME");

        //教科書略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "TEXTBOOKABBV",
                            "size"        => 10,
                            "maxlength"   => 10,
                            "extrahtml"   => "",
                            "value"       => $Row["TEXTBOOKABBV"] ));

        $arg["data"]["TEXTBOOKABBV"] = $objForm->ge("TEXTBOOKABBV");

        //記号漢字
        $objForm->ae( array("type"        => "text",
                            "name"        => "TEXTBOOKMK",
                            "size"        => 9,
                            "maxlength"   => 9,
                            "extrahtml"   => "",
                            "value"       => $Row["TEXTBOOKMK"] ));

        $arg["data"]["TEXTBOOKMK"] = $objForm->ge("TEXTBOOKMK");

        //記号数字
        $objForm->ae( array("type"        => "text",
                            "name"        => "TEXTBOOKMS",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "",
                            "value"       => $Row["TEXTBOOKMS"] ));

        $arg["data"]["TEXTBOOKMS"] = $objForm->ge("TEXTBOOKMS");

        //著作名
        $objForm->ae( array("type"        => "text",
                            "name"        => "TEXTBOOKWRITINGNAME",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $Row["TEXTBOOKWRITINGNAME"] ));

        $arg["data"]["TEXTBOOKWRITINGNAME"] = $objForm->ge("TEXTBOOKWRITINGNAME");

        //定価
        $objForm->ae( array("type"        => "text",
                            "name"        => "TEXTBOOKPRICE",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["TEXTBOOKPRICE"] ));

        $arg["data"]["TEXTBOOKPRICE"] = $objForm->ge("TEXTBOOKPRICE");

        //単価
        $objForm->ae( array("type"        => "text",
                            "name"        => "TEXTBOOKUNITPRICE",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["TEXTBOOKUNITPRICE"] ));

        $arg["data"]["TEXTBOOKUNITPRICE"] = $objForm->ge("TEXTBOOKUNITPRICE");

		//NO001
        //発行者
		$opt_issu = array();
		$i = 0;
        $db     = Query::dbCheckOut();
		$query  = knjz150t_2Query::getIssu();
		$result = $db->query($query);

		$opt_issu[$i] = array("label" => "",
							  "value" => null);
		$i++;

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
			//レコードを連想配列のまま配列$arg[data]に追加していく。 
			$opt_issu[$i] = array("label" => $row["ISSUECOMPANYCD"]."　".$row["ISSUECOMPANYNAME"],
								  "value" => $row["ISSUECOMPANYCD"]);
			$i++;
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"        => "select",
                            "name"        => "ISSUECOMPANYCD",
                            "size"        => 1,
                            "extrahtml"   => "",
                            "value"       => $Row["ISSUECOMPANYCD"],
							"options"     => $opt_issu ));

        $arg["data"]["ISSUECOMPANYCD"] = $objForm->ge("ISSUECOMPANYCD");
  		//NO001

/* NO001
        //業者名
        $objForm->ae( array("type"        => "text",
                            "name"        => "CONTRACTORNAME",
                            "size"        => 30,
                            "maxlength"   => 30,
                            "extrahtml"   => "",
                            "value"       => $Row["CONTRACTORNAME"] ));

        $arg["data"]["CONTRACTORNAME"] = $objForm->ge("CONTRACTORNAME");
*/
        //備考
        $objForm->ae( array("type"        => "text",
                            "name"        => "REMARK",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $Row["REMARK"] ));

        $arg["data"]["REMARK"] = $objForm->ge("REMARK");

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
                            "extrahtml"   => "onclick=\"return btn_submit('reset')\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ150T/knjz150tindex.php";
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
            $arg["reload"]  = "parent.left_frame.location.href='knjz150t_2index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz150t_2Form2.html", $arg); 
    }
}
?>
