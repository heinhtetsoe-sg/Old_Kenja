<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjz420Form2.php 56591 2017-10-22 13:04:39Z maeshiro $
class knjz420Form2
{
	function main(&$model)
	{
		//権限チェック
		if (AUTHORITY != DEF_UPDATABLE){
			$arg["jscript"] = "OnAuthError();";
		}

		$objForm = new form;
		//フォーム作成
		$arg["start"]   = $objForm->get_start("edit", "POST", "knjz420index.php", "", "edit");

		//警告メッセージを表示しない場合
		if (isset($model->company_cd) && !isset($model->warning)){
			$Row = knjz420Query::getRow($model->company_cd);
		}else{
			$Row =& $model->field;
		}
		//職種取得
		$db = Query::dbCheckOut();
		$result = $db->query(knjz420Query::getCompanycd());
		while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
			$opt_companycd[]= array('label' => $row["LABEL"],
									'value' => $row["VALUE"]);
		}
		$result->free();

		//募集対象
		$opt_target[] = array("label" => "","value" => "0");
		$result = $db->query(knjz420Query::getTarget());
		while($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
			$opt_target[]= array('label' => $row2["LABEL"],
								 'value' => $row2["VALUE"]);
		}

		$result->free();
		Query::dbCheckIn($db);

		//会社コード
		$objForm->ae( array("type"		  => "text",
							"name"		  => "COMPANY_CD",
							"size"		  => 8,
							"maxlength"   => 8,
							"extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
							"value" 	  => $Row["COMPANY_CD"]));

		$arg["data"]["COMPANY_CD"] = $objForm->ge("COMPANY_CD");

		//会社名
		$objForm->ae( array("type"		  => "text",
							"name"		  => "COMPANY_NAME",
							"size"		  => 80,
							"maxlength"   => 120,
							"extrahtml"   => "",
							"value" 	  => $Row["COMPANY_NAME"] ));

		$arg["data"]["COMPANY_NAME"] = $objForm->ge("COMPANY_NAME");

		//就業場所
		$objForm->ae( array("type"		  => "text",
							"name"		  => "SHUSHOKU_ADDR",
							"size"		  => 80,
							"maxlength"   => 120,
							"extrahtml"   => "",
							"value" 	  => $Row["SHUSHOKU_ADDR"] ));

		$arg["data"]["SHUSHOKU_ADDR"] = $objForm->ge("SHUSHOKU_ADDR");

		//資本金
		$objForm->ae( array("type"		  => "text",
							"name"		  => "SHIHONKIN",
							"size"		  => 17,
							"maxlength"   => 17,
							"extrahtml"   => "",
							"value" 	  => $Row["SHIHONKIN"] ));

		$arg["data"]["SHIHONKIN"] = $objForm->ge("SHIHONKIN");
		
		//全体人数
		$objForm->ae( array("type"		  => "text",
							"name"		  => "SONINZU",
							"size"		  => 8,
							"maxlength"   => 8,
							"extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
							"value" 	  => $Row["SONINZU"] ));

		$arg["data"]["SONINZU"] = $objForm->ge("SONINZU");
		
		//事務所人数
		$objForm->ae( array("type"		  => "text",
							"name"		  => "TONINZU",
							"size"		  => 8,
							"maxlength"   => 8,
							"extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
							"value" 	  => $Row["TONINZU"] ));

		$arg["data"]["TONINZU"] = $objForm->ge("TONINZU");
		
		//会社職種コンボボックス
		$objForm->ae( array("type"		  => "select",
							"name"		  => "COMPANY_SORT",
							"size"		  => "1",
							"extrahtml"   => "",
							"value" 	  => $Row["COMPANY_SORT"],
							"options"	  => isset($opt_companycd)?$opt_companycd:array() ));

		$arg["data"]["COMPANY_SORT"] = $objForm->ge("COMPANY_SORT");

		//募集対象コンボボックス
		$objForm->ae( array("type"		  => "select",
							"name"		  => "TARGET_SEX",
							"size"		  => "1",
							"extrahtml"   => "",
							"value" 	  => $Row["TARGET_SEX"],
							"options"	  => isset($opt_target)?$opt_target:array() ));

		$arg["data"]["TARGET_SEX"] = $objForm->ge("TARGET_SEX");

		//郵便番号
		$arg["data"]["ZIPCD"] = View::popUpZipCode($objForm, "ZIPCD", $Row["ZIPCD"],"ADDR1");

		//住所１
		$objForm->ae( array("type"		  => "text",
							"name"		  => "ADDR1",
							"size"		  => 60,
							"maxlength"   => 90,
							"extrahtml"   => "",
							"value" 	  => $Row["ADDR1"] ));

		$arg["data"]["ADDR1"] = $objForm->ge("ADDR1");

		//住所２
		$objForm->ae( array("type"		  => "text",
							"name"		  => "ADDR2",
							"size"		  => 60,
							"maxlength"   => 90,
							"extrahtml"   => "",
							"value" 	  => $Row["ADDR2"] ));

		$arg["data"]["ADDR2"] = $objForm->ge("ADDR2");

		//電話番号
		$objForm->ae( array("type"		  => "text",
							"name"		  => "TELNO",
							"size"		  => 16,
							"maxlength"   => 16,
							"extrahtml"   => "",
							"value" 	  => $Row["TELNO"] ));

		$arg["data"]["TELNO"] = $objForm->ge("TELNO");

		//備考
		$objForm->ae( array("type"		  => "text",
							"name"		  => "REMARK",
							"size"		  => 80,
							"maxlength"   => 120,
							"extrahtml"   => "",
							"value" 	  => $Row["REMARK"] ));

		$arg["data"]["REMARK"] = $objForm->ge("REMARK");

		//追加ボタン
		$objForm->ae( array("type"      => "button",
							"name"		=> "btn_add",
							"value"	    => "追 加",
							"extrahtml" => "onclick=\"return btn_submit('add');\"" ) );

		$arg["button"]["btn_add"] = $objForm->ge("btn_add");

		//修正ボタン
		$objForm->ae( array("type"      => "button",
							"name"		=> "btn_update",
							"value"	    => "更 新",
							"extrahtml" => "onclick=\"return btn_submit('update');\"" ) );

		$arg["button"]["btn_update"] = $objForm->ge("btn_update");

		//削除ボタン
		$objForm->ae( array("type"      => "button",
							"name"		=> "btn_del",
							"value"	    => "削 除",
							"extrahtml" => "onclick=\"return btn_submit('delete');\"" ) );

		$arg["button"]["btn_del"] = $objForm->ge("btn_del");

		//クリアボタン
		$objForm->ae( array("type"      => "button",
							"name"		=> "btn_reset",
							"value"	    => "取 消",
							"extrahtml" => "onclick=\"return btn_submit('reset');\"" ) );

		$arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

		//終了ボタン
		$objForm->ae( array("type"      => "button",
							"name"		=> "btn_back",
							"value"	    => "終 了",
							"extrahtml" => "onclick=\"closeWin();\"" ) );

		$arg["button"]["btn_back"] = $objForm->ge("btn_back");

		//hidden
		$objForm->ae( array("type"	  => "hidden",
							"name"	  => "cmd"
							) );

		$arg["finish"]  = $objForm->get_finish();
		if (VARS::get("cmd") != "edit"){
			$arg["reload"]  = "parent.left_frame.location.href='knjz420index.php?cmd=list';";
		}

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
		View::toHTML($model, "knjz420Form2.html", $arg); 
	}
}
?>
