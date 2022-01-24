<?php

require_once('for_php7.php');


class knjl312hForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl312hForm1", "POST", "knjl312hindex.php", "", "knjl312hForm1");

	$opt=array();

	//年度
	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl312hQuery::get_apct_div("L003",$model->ObjYear));	

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
						"extrahtml"  => " onChange=\"return btn_submit('knjl312h');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");

	//入試区分コンボの設定
	$db = Query::dbCheckOut();
	$opt_test_dv = array();
	$default_flg = false;
	$default     = 0 ;
	$opt_test_dv[]= array("label" => "",  "value" => "");
	$result = $db->query(knjl312hQuery::get_test_div("L004",$model->ObjYear,$model->field["APDIV"]));	

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
						"extrahtml"  => "onChange=\"return btn_submit('knjl312h');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDV"] = $objForm->ge("TESTDV");

	//受験型コンボの設定
	$opt_exam_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl312hQuery::get_exam_div("L005",$model->ObjYear));	
	$opt_exam_typ[]= array("label" => "-- 全て --", "value" => "0");

	while($Rowtyp = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_exam_typ[]= array("label" => $Rowtyp["NAME1"], 
							   "value" => $Rowtyp["NAMECD2"]);
	}

	$result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["EXAM_TYPE"])) {
		$model->field["EXAM_TYPE"] = $opt_exam_typ[0]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "EXAM_TYPE",
        	            "size"       => "1",
            	        "value"      => $model->field["EXAM_TYPE"],
						"extrahtml"  => " onChange=\"return btn_submit('knjl312h');\"",
                    	"options"    => $opt_exam_typ));

	$arg["data"]["EXAM_TYPE"] = $objForm->ge("EXAM_TYPE");

	//受験科目コンボの設定
	$opt_sbcd_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl312hQuery::get_subclas_div("L009",$model));	

	while($Rowtyp = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_sbcd_typ[]= array("label" => $Rowtyp["NAME1"], 
							   "value" => $Rowtyp["NAMECD2"]);
	}
	$result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["SUBCLASS_TYPE"])) {
		$model->field["SUBCLASS_TYPE"] = $opt_sbcd_typ[0]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "SUBCLASS_TYPE",
        	            "size"       => "1",
            	        "value"      => $model->field["SUBCLASS_TYPE"],
						"extrahtml"  => " onChange=\"return btn_submit('knjl312h');\"",
                    	"options"    => $opt_sbcd_typ));

	$arg["data"]["SUBCLASS_TYPE"] = $objForm->ge("SUBCLASS_TYPE");

	//試験会場選択コンボボックスを作成する
	$db = Query::dbCheckOut();
	$result = $db->query(knjl312hQuery::get_hall_data($model->field["TESTDV"],$model->field["EXAM_TYPE"],$model->field["SUBCLASS_TYPE"]));
	while($Rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    	$opt_list[]= array('label' => $Rowlist["EXAMHALL_NAME"],
    					   'value' => $Rowlist["EXAM_TYPE"].$Rowlist["VALUE"]);

	}

	$result->free();
	Query::dbCheckIn($db);

	//対象会場リストを作成する
	$objForm->ae( array("type"       => "select",
	                    "name"       => "category_name",
						"extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"",
        	            "size"       => "20",
            	        "options"    => array()));

	$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

	//会場一覧リストを作成する
	$objForm->ae( array("type"       => "select",
    	                "name"       => "category_selected",
						"extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"",
            	        "size"       => "20",
                	    "options"    => isset($opt_list)?$opt_list:array()));

	$arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

	//対象取り消しボタンを作成する(個別)
	$objForm->ae( array("type"        => "button",
    	                "name"        => "btn_right1",
        	            "value"       => "　＞　",
            	        "extrahtml"   => " onclick=\"move('right');\"" ) );

	$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

	//対象取り消しボタンを作成する(全て)
	$objForm->ae( array("type"        => "button",
    	                "name"        => "btn_right2",
        	            "value"       => "　≫　",
            	        "extrahtml"   => " onclick=\"move('rightall');\"" ) );

	$arg["button"]["btn_right2"] = $objForm->ge("btn_right2");

	//対象選択ボタンを作成する(個別)
	$objForm->ae( array("type"        => "button",
    	                "name"        => "btn_left1",
        	            "value"       => "　＜　",
            	        "extrahtml"   => " onclick=\"move('left');\"" ) );

	$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

	//対象選択ボタンを作成する(全て)
	$objForm->ae( array("type"        => "button",
    	                "name"        => "btn_left2",
        	            "value"       => "　≪　",
            	        "extrahtml"   => " onclick=\"move('leftall');\"" ) );

	$arg["button"]["btn_left2"] = $objForm->ge("btn_left2");

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
        	            "value"     => "KNJL312H"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl312hForm1.html", $arg); 
	}
}
?>
