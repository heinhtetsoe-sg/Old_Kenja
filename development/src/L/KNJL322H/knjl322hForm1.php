<?php

require_once('for_php7.php');


class knjl322hForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl322hForm1", "POST", "knjl322hindex.php", "", "knjl322hForm1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl322hQuery::get_apct_div("L003",$model->ObjYear));	

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
						"extrahtml"  => " onChange=\"return btn_submit('knjl322h');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");

	//入試区分コンボの設定
	$db = Query::dbCheckOut();
	$opt_test_dv = array();
	$default_flg = false;
	$default     = 1 ;
	$opt_test_dv[]= array("label" => "",  "value" => "");

    if($model->field["APDIV"] == "1"){
	    $result = $db->query(knjl322hQuery::get_test_div("L004",$model->ObjYear,$model->field["APDIV"]));
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
    }
	Query::dbCheckIn($db);

	$opt_test_dv[]= array("label" => "-- 全て --",  "value" => "0");

	if (!isset($model->field["TESTDV"])) {
		$model->field["TESTDV"] = $opt_test_dv[$default]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "TESTDV",
        	            "size"       => "1",
            	        "value"      => $model->field["TESTDV"],
						"extrahtml"  => "onChange=\"return btn_submit('knjl322h');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDV"] = $objForm->ge("TESTDV");

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
        	            "value"     => "KNJL322H"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();

	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl322hForm1.html", $arg); 
	}
}
?>
