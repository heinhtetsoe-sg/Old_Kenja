<?php
/********************************************************************/
/* 事前相談結果名簿                                 山城 2005/07/21 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001：ソート順を追加                            山城 2006/02/02 */
/********************************************************************/

class knjl303kForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl303kForm1", "POST", "knjl303kindex.php", "", "knjl303kForm1");

	$opt=array();

	$arg["data"]["YEAR"] = $model->ObjYear;

	//試験区分を作成する
	$opt_testdiv = array();
	$testcnt = 0;
	$db = Query::dbCheckOut();

	$result = $db->query(knjl303kQuery::GetTestdiv($model));
	while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_testdiv[] = array("label" => $row["NAME1"],
							   "value" => $row["NAMECD2"]);
		$testcnt++;
	}
	if ($testcnt == 0){
		$opt_testdiv[$testcnt] = array("label" => "　　",
									   "value" => "99");
	}else {
		$opt_testdiv[$testcnt] = array("label" => "全て",
									   "value" => "99");
	}
	
	if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

	Query::dbCheckIn($db);
	$objForm->ae( array("type" 		=> "select",
    	                "name"		=> "TESTDIV",
        	            "size"		=> 1,
        	            "value"		=> $model->testdiv,
            	        "extrahtml"	=> "",
						"options"	=> $opt_testdiv ) );

	$arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

	//出力順ラジオ作成
	$out_sort[0] = 1;
	$out_sort[1] = 2;
	$out_sort[2] = 3;	//NO001
	$out_sort[3] = 4;	//NO001

	$objForm->ae( array("type"     => "radio",
						"name"     => "OUTPUT",
						"value"    => "1",
						"multipre" => $out_sort));
	$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
	$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
	$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);	//NO001
	$arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT",4);	//NO001

	//中高判別フラグを作成する
	$jhflg = 0;
	$db = Query::dbCheckOut();
	$row = $db->getOne(knjl303kQuery::GetJorH());
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
        	            "value"     => "KNJL303K"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl303kForm1.html", $arg); 
	}
}
?>
