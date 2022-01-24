<?php

require_once('for_php7.php');


class knjl320hForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl320hForm1", "POST", "knjl320hindex.php", "", "knjl320hForm1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl320hQuery::get_apct_div("L003",$model->ObjYear));	

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
						"extrahtml"  => " onChange=\"return btn_submit('knjl320h');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");

	//入試区分コンボの設定
	$opt_test_dv = array();
	$defoult_flg = false;
	$defoult     = 1 ;
	$db = Query::dbCheckOut();
	$result = $db->query(knjl320hQuery::get_test_div("L004",$model->ObjYear,$model->field["APDIV"]));	
	$opt_test_dv[]= array("label" => "", 
						  "value" => "");
	$test_flg = false;	//入試区分データフラグ

	while($Rowtdv = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_test_dv[]= array("label" => $Rowtdv["NAME1"], 
							  "value" => $Rowtdv["NAMECD2"]);
		$test_flg = true;

		if ($Rowtdv["NAMESPARE2"] != 1 && !$defoult_flg){
			$defoult++;
		} else {
			$defoult_flg = true;
		}
	}
	$result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["TESTDV"])) {
		$model->field["TESTDV"] = $opt_test_dv[$defoult]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "TESTDV",
        	            "size"       => "1",
            	        "value"      => $model->field["TESTDV"],
						"extrahtml"  => "onChange=\"return btn_submit('knjl320h');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDV"] = $objForm->ge("TESTDV");

	//指定範囲テキスト作成
	$objForm->ae( array("type"       => "text",
    	                "name"       => "TESTSCR",
                        "size"       => 3,
                        "maxlength"  => 3,
						"extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"",
						"value"      => isset($model->field["TESTSCR"])?$model->field["TESTSCR"]:"0"));

	$arg["data"]["TESTSCR"] = $objForm->ge("TESTSCR");

	//印刷順序ラジオの設定
	$opt_sort   = array();
	$opt_sort[] = 1;
	$opt_sort[] = 2;

	if (!$model->field["SORT"]){
		$model->field["SORT"] = 1;
	}

	$objForm->ae( array("type"       => "radio",
    	                "name"       => "SORT",
            	        "value"      => $model->field["SORT"],
                    	"options"    => $opt_sort));

	$arg["data"]["SORT1"] = $objForm->ge("SORT",1);
	$arg["data"]["SORT2"] = $objForm->ge("SORT",2);

	//氏名印字選択ラジオの設定
	$opt_name   = array();
	$opt_name[] = 1;
	$opt_name[] = 2;

	if (!$model->field["NAME_OUTPUT"]){
		$model->field["NAME_OUTPUT"] = 1;
	}

	$objForm->ae( array("type"       => "radio",
    	                "name"       => "NAME_OUTPUT",
            	        "value"      => $model->field["NAME_OUTPUT"],
                    	"options"    => $opt_name));

	$arg["data"]["NAME_OUTPUT1"] = $objForm->ge("NAME_OUTPUT",1);
	$arg["data"]["NAME_OUTPUT2"] = $objForm->ge("NAME_OUTPUT",2);

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
        	            "value"     => "KNJL320H"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();

	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl320hForm1.html", $arg); 
	}
}
?>
