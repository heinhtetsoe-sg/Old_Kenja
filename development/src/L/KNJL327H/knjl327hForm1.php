<?php

require_once('for_php7.php');


class knjl327hForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl327hForm1", "POST", "knjl327hindex.php", "", "knjl327hForm1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl327hQuery::get_apct_div("L003",$model->ObjYear));

	while($Rowtyp = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_apdv_typ[]= array("label" => $Rowtyp["NAME1"], 
							   "value" => $Rowtyp["NAMECD2"]);
	}

	$result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["APDIV"])) {
		$model->field["APDIV"] = $opt_apdv_typ[0]["value"];
	}
	$objForm->ae( array("type"       => "select",
    	                "name"       => "APDIV",
        	            "size"       => "1",
            	        "value"      => $model->field["APDIV"],
						"extrahtml"  => " onChange=\"return btn_submit('knjl327h');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");

	//入試区分コンボの設定
	$opt_test_dv = array();
	$default_flg = false;
	$default     = 1 ;
	$db = Query::dbCheckOut();
	$result = $db->query(knjl327hQuery::get_test_div("L004",$model->ObjYear,$model->field["APDIV"]));
	$opt_test_dv[]= array("label" => "", 
						  "value" => "");

	while($Rowtdv = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_test_dv[]= array("label" => $Rowtdv["NAME1"], 
							  "value" => $Rowtdv["NAMECD2"]);

		if ($Rowtdv["NAMESPARE2"] != 1 && !$default_flg){
			$default++;
		} else {
			$default_flg = true;
		}
	}

	$result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["TESTDV"])) {
		$model->field["TESTDV"] = $opt_test_dv[$default]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "TESTDV",
        	            "size"       => "1",
            	        "value"      => $model->field["TESTDV"],
						"extrahtml"  => "onChange=\"return btn_submit('knjl327h');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDV"] = $objForm->ge("TESTDV");

	//通知日付
	$value = isset($model->field["NOTICEDAY"])?$model->field["NOTICEDAY"]:str_replace("-","/",$model->control["学籍処理日"]);
	$arg["el"]["NOTICEDAY"] = View::popUpCalendar($objForm, "NOTICEDAY", $value);

	//帳票種類ラジオボタン作成
	$disp_onoff = "";
	$opt_opt[] = array();
	for ($i = 1;$i <= 6;$i++) {
		$opt_opt[] = $i;
	}
	if (!isset($model->field["OUTPUT"])){
		$model->field["OUTPUT"] = 1;
	}
	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUT",
						"value"      => $model->field["OUTPUT"],
						"extrahtml"  => " onclick=\"return btn_submit('knjl327h');\"",
						"multiple"   => $opt_opt));

	$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);	//合格通知書
	$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);	//繰上合格候補者通知書
	$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);	//入学許可通知書

	//帳票出力Aラジオボタン作成
	$opt_opta[] = array();
	$opt_opta[] = 1;
	$opt_opta[] = 2;
	if ($model->field["OUTPUT"] == 1){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}
	if (!isset($model->field["OUTPUTA"])){
		$model->field["OUTPUTA"] = 1;
	}
	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUTA",
						"value"      => $model->field["OUTPUTA"],
						"extrahtml"	 => "$disp_onoff onclick=\"return btn_submit('knjl327h');\"",
						"multiple"   => $opt_opta));

	$arg["data"]["OUTPUTA1"] = $objForm->ge("OUTPUTA",1);
	$arg["data"]["OUTPUTA2"] = $objForm->ge("OUTPUTA",2);

	//disabled設定
	if (($model->field["OUTPUTA"] == 2)&&($disp_onoff == "")){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}

	//受験番号A
	if (!isset($model->field["EXAMNOA"])){
		$model->field["EXAMNOA"] = "";
	}

	$objForm->ae( array("type"       => "text",
    	                "name"       => "EXAMNOA",
    	                "size"       => 4,
    	                "maxlength"  => 4,
						"value"      => $model->field["EXAMNOA"],
						"extrahtml"	 => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

	$arg["data"]["EXAMNOA"] = $objForm->ge("EXAMNOA");

	//帳票出力Bラジオボタン作成
	$opt_optb[] = array();
	$opt_optb[] = 1;
	$opt_optb[] = 2;
	if ($model->field["OUTPUT"] == 2){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}
	if (!isset($model->field["OUTPUTB"])){
		$model->field["OUTPUTB"] = 1;
	}

	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUTB",
						"value"      => $model->field["OUTPUTB"],
						"extrahtml"	 => "$disp_onoff onclick=\"return btn_submit('knjl327h');\"",
						"multiple"   => $opt_optb));

	$arg["data"]["OUTPUTB1"] = $objForm->ge("OUTPUTB",1);
	$arg["data"]["OUTPUTB2"] = $objForm->ge("OUTPUTB",2);

	//disabled設定
	if ($model->field["OUTPUT"] == 2){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}

    //通知日付
	$value = isset($model->field["CONTACTDATE"])?$model->field["CONTACTDATE"]:str_replace("-","/",$model->control["学籍処理日"]);
    $arg["el"]["CONTACTDATE"] = View::popUpCalendar2($objForm, "CONTACTDATE", $value,"","",$disp_onoff);

	//disabled設定
	if (($model->field["OUTPUTB"] == 2)&&($disp_onoff == "")){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}

	//受験番号B
	if (!isset($model->field["EXAMNOB"])){
		$model->field["EXAMNOB"] = "";
	}

	$objForm->ae( array("type"       => "text",
    	                "name"       => "EXAMNOB",
    	                "size"       => 4,
    	                "maxlength"  => 4,
						"value"      => $model->field["EXAMNOB"],
						"extrahtml"	 => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

	$arg["data"]["EXAMNOB"] = $objForm->ge("EXAMNOB");

	//帳票出力Cラジオボタン作成
	$opt_optc[] = array();
	$opt_optc[] = 1;
	$opt_optc[] = 2;
	if ($model->field["OUTPUT"] == 3){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}
	if (!isset($model->field["OUTPUTC"])){
		$model->field["OUTPUTC"] = 1;
	}

	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUTC",
						"value"      => $model->field["OUTPUTC"],
						"extrahtml"	 => "$disp_onoff onclick=\"return btn_submit('knjl327h');\"",
						"multiple"   => $opt_optc));

	$arg["data"]["OUTPUTC1"] = $objForm->ge("OUTPUTC",1);
	$arg["data"]["OUTPUTC2"] = $objForm->ge("OUTPUTC",2);

	//disabled設定
	if (($model->field["OUTPUTC"] == 2)&&($disp_onoff == "")){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}
	//受験番号C
	if (!isset($model->field["EXAMNOC"])){
		$model->field["EXAMNOC"] = "";
	}

	$objForm->ae( array("type"       => "text",
    	                "name"       => "EXAMNOC",
    	                "size"       => 4,
    	                "maxlength"  => 4,
						"value"      => $model->field["EXAMNOC"],
						"extrahtml"	 => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

	$arg["data"]["EXAMNOC"] = $objForm->ge("EXAMNOC");

	//印刷ボタンを作成する
	$objForm->ae( array("type"        => "button",
    	                "name"        => "btn_print",
        	            "value"       => "プレビュー／印刷",
            	        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

	$arg["button"]["btn_print"] = $objForm->ge("btn_print");

	//終了ボタンを作成する
	$objForm->ae( array("type"        => "button",
    	                "name"        => "btn_end",
        	            "value"       => "終 了",
            	        "extrahtml"   => "onclick=\"closeWin();\"" ) );

	$arg["button"]["btn_end"] = $objForm->ge("btn_end");

	//hiddenを作成する
	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "YEAR",
        	            "value"     => $model->ObjYear
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "DBNAME",
        	            "value"     => DB_DATABASE
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "PRGID",
        	            "value"     => "KNJL327H"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "DOCUMENTROOT",
        	            "value"     => DOCUMENTROOT
            	        ) );

    //以下のhiddenは、印刷処理時の判定用
	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "OUT",
        	            "value"     => $model->field["OUTPUT"]
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "OUTA",
        	            "value"     => $model->field["OUTPUTA"]
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "OUTB",
        	            "value"     => $model->field["OUTPUTB"]
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "OUTC",
        	            "value"     => $model->field["OUTPUTC"]
            	        ) );

	$arg["finish"]  = $objForm->get_finish();

	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl327hForm1.html", $arg); 
	}
}
?>
