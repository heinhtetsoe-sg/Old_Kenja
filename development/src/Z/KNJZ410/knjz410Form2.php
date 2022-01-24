<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjz410Form2.php 56591 2017-10-22 13:04:39Z maeshiro $
class knjz410Form2
{
	function main(&$model)
	{
		//権限チェック
		if (AUTHORITY != DEF_UPDATABLE){
			$arg["jscript"] = "OnAuthError();";
		}

		$objForm = new form;
		//フォーム作成
		$arg["start"]   = $objForm->get_start("edit", "POST", "knjz410index.php", "", "edit");

		//警告メッセージを表示しない場合
		if (isset($model->school_cd) && !isset($model->warning)){
			$Row = knjz410Query::getRow($model->school_cd);
		}else{
			$Row =& $model->field;
		}
		//学校分類取得
		$db = Query::dbCheckOut();
		$result = $db->query(knjz410Query::getSchoolcd());
		while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
			$opt_schoolcd[]= array('label' => $row["LABEL"],
								   'value' => $row["VALUE"]);
		}
		$result->free();

		//専門分野
		$opt_bunya[] = array("label" => "","value" => "0");
		$result = $db->query(knjz410Query::getBunya());
		while($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
			$opt_bunya[]= array('label' => $row2["LABEL"],
								'value' => $row2["VALUE"]);
		}

		$result->free();
		Query::dbCheckIn($db);

		//学校コード
		$objForm->ae( array("type"		  => "text",
							"name"		  => "SCHOOL_CD",
							"size"		  => 8,
							"maxlength"   => 8,
							"extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
							"value" 	  => $Row["SCHOOL_CD"]));

		$arg["data"]["SCHOOL_CD"] = $objForm->ge("SCHOOL_CD");

		//学校名
		$objForm->ae( array("type"		  => "text",
							"name"		  => "SCHOOL_NAME",
							"size"		  => 80,
							"maxlength"   => 120,
							"extrahtml"   => "",
							"value" 	  => $Row["SCHOOL_NAME"] ));

		$arg["data"]["SCHOOL_NAME"] = $objForm->ge("SCHOOL_NAME");

		//学部名
		$objForm->ae( array("type"		  => "text",
							"name"		  => "BUNAME",
							"size"		  => 80,
							"maxlength"   => 120,
							"extrahtml"   => "",
							"value" 	  => $Row["BUNAME"] ));

		$arg["data"]["BUNAME"] = $objForm->ge("BUNAME");

		//学科名
		$objForm->ae( array("type"		  => "text",
							"name"		  => "KANAME",
							"size"		  => 80,
							"maxlength"   => 120,
							"extrahtml"   => "",
							"value" 	  => $Row["KANAME"] ));

		$arg["data"]["KANAME"] = $objForm->ge("KANAME");
		
		//学校分類コンボボックス
		$objForm->ae( array("type"		  => "select",
							"name"		  => "SCHOOL_SORT",
							"size"		  => "1",
							"extrahtml"   => "",
							"value" 	  => $Row["SCHOOL_SORT"],
							"options"	  => isset($opt_schoolcd)?$opt_schoolcd:array() ));

		$arg["data"]["SCHOOL_SORT"] = $objForm->ge("SCHOOL_SORT");

		//専門分野コンボボックス
		$objForm->ae( array("type"		  => "select",
							"name"		  => "BUNYA",
							"size"		  => "1",
							"extrahtml"   => "",
							"value" 	  => $Row["BUNYA"],
							"options"	  => isset($opt_bunya)?$opt_bunya:array() ));

		$arg["data"]["BUNYA"] = $objForm->ge("BUNYA");

		//所在地
		$objForm->ae( array("type"		  => "text",
							"name"		  => "AREA_NAME",
							"size"		  => 20,
							"maxlength"   => 30,
							"extrahtml"   => "",
							"value" 	  => $Row["AREA_NAME"] ));

		$arg["data"]["AREA_NAME"] = $objForm->ge("AREA_NAME");

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

		//評定基準
		$objForm->ae( array("type"		  => "text",
							"name"		  => "GREDES",
							"size"		  => 80,
							"maxlength"   => 120,
							"extrahtml"   => "",
							"value" 	  => $Row["GREDES"] ));

		$arg["data"]["GREDES"] = $objForm->ge("GREDES");

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
			$arg["reload"]  = "parent.left_frame.location.href='knjz410index.php?cmd=list';";
		}

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
		View::toHTML($model, "knjz410Form2.html", $arg); 
	}
}
?>
