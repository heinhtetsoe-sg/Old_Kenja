<?php

class knjl305kForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl305kForm1", "POST", "knjl305kindex.php", "", "knjl305kForm1");

	$opt=array();

	$arg["data"]["YEAR"] = $model->ObjYear;

	//試験区分を作成する
	$opt_testdiv = array();
	$testcnt = 0;
	$db = Query::dbCheckOut();

	$result = $db->query(knjl305kQuery::GetTestdiv($model));
	while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_testdiv[] = array("label" => $row["NAME1"],
							   "value" => $row["NAMECD2"]);
		$testcnt++;
	}
	if ($testcnt == 0){
		$opt_testdiv[$testcnt] = array("label" => "　　",
									   "value" => "99");
	}
	
	if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

	$result->free();
	Query::dbCheckIn($db);
	$objForm->ae( array("type" 		=> "select",
    	                "name"		=> "TESTDIV",
        	            "size"		=> 1,
        	            "value"		=> $model->testdiv,
            	        "extrahtml"	=> "",
						"options"	=> $opt_testdiv ) );

	$arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

	//中高判別フラグを作成する
	$jhflg = 0;
	$db = Query::dbCheckOut();
	$row = $db->getOne(knjl305kQuery::GetJorH());
	if ($row == 1){
		$jhflg = 1;
	}else {
		$jhflg = 2;
	}
	Query::dbCheckIn($db);
	$objForm->ae( array("type" => "hidden",
						"name" => "JHFLG",
						"value"=> $jhflg ) );

	//印刷ボタンを作成する
	$objForm->ae( array("type" => "button",
    	                "name"        => "btn_print",
        	            "value"       => "プレビュー／印刷",
            	        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

	$arg["button"]["btn_print"] = $objForm->ge("btn_print");

	//終了ボタンを作成する
	$objForm->ae( array("type" => "button",
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
        	            "value"     => "KNJL305K"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl305kForm1.html", $arg); 
	}
}
?>
