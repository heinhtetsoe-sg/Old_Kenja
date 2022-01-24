<?php

class knjl304hForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl304hForm1", "POST", "knjl304hindex.php", "", "knjl304hForm1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl304hQuery::get_apct_div("L003",$model->ObjYear));

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
						"extrahtml"  => " onChange=\"return btn_submit('knjl304h');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");

	//指定範囲テキスト作成(２科目合計)
	$objForm->ae( array("type"       => "text",
    	                "name"       => "EXAMNO_ST",
                        "size"       => 5,
                        "maxlength"  => 5,
						"extrahtml"  => "onblur=\"this.value=toInteger(this.value)\"",
						"value"      => isset($model->field["EXAMNO_ST"])?$model->field["EXAMNO_ST"]:"00001"));

	$arg["data"]["EXAMNO_ST"] = $objForm->ge("EXAMNO_ST");

	//指定範囲テキスト作成(４科目合計)
	$objForm->ae( array("type"       => "text",
    	                "name"       => "EXAMNO_ED",
                        "size"       => 5,
                        "maxlength"  => 5,
						"extrahtml"  => "onblur=\"this.value=toInteger(this.value)\"",
						"value"      => isset($model->field["EXAMNO_ED"])?$model->field["EXAMNO_ED"]:"99999"));

	$arg["data"]["EXAMNO_ED"] = $objForm->ge("EXAMNO_ED");

	//印刷ボタンを作成する
	$objForm->ae( array("type"      => "button",
    	                "name"      => "btn_print",
        	            "value"     => "プレビュー／印刷",
            	        "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

	$arg["button"]["btn_print"] = $objForm->ge("btn_print");

	//終了ボタンを作成する
	$objForm->ae( array("type"      => "button",
    	                "name"      => "btn_end",
        	            "value"     => "終 了",
            	        "extrahtml" => "onclick=\"closeWin();\"" ) );

	$arg["button"]["btn_end"] = $objForm->ge("btn_end");

	//hiddenを作成する
	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "YEAR",
        	            "value"     => $model->ObjYear
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "DBNAME",
        	            "value"      => DB_DATABASE
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "PRGID",
        	            "value"     => "KNJL304H"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();

	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl304hForm1.html", $arg); 
	}
}
?>
