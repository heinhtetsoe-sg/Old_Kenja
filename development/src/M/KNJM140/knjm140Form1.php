<?php

require_once('for_php7.php');

/********************************************************************/
/* 時間割チェックリスト                             山城 2005/04/14 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm140Form1
{
    function main(&$model){

	//オブジェクト作成
	$objForm = new form;

	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjm140Form1", "POST", "knjm140index.php", "", "knjm140Form1");

	//年度コンボを作成する
	if ($model->field["YEAR"] == "") $model->field["YEAR"] = CTRL_YEAR;
	$opt_year = array();
	$opt_year[0] = array("label" => CTRL_YEAR,
						 "value" => CTRL_YEAR );
	$opt_year[1] = array("label" => CTRL_YEAR+1,
						 "value" => CTRL_YEAR+1 );

	$objForm->ae( array("type"		=> "select",
						"name"		=> "YEAR",
						"size"		=> "1",
						"value"		=> $model->field["YEAR"],
						"extrahtml"	=> "onchange=\" return btn_submit('')\";",
						"options"	=> $opt_year));

	$arg["data"]["YEAR"] = $objForm->ge("YEAR");

	//講座コンボを作成する
	$opt_chair = array();
	$i = 1;

	$db = Query::dbCheckOut();
	$query = knjm140Query::GetChair($model);
	$result = $db->query($query);
	$opt_chair[0] = array('label' => "",
						  'value' => 0);
	while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_chair[$i] = array('label' => $row["CHAIRNAME"],
							   'value' => $row["CHAIRCD"]);
		$i++;
	}
	if($model->field["CHAIR"]=="") $model->field["CHAIR"] = $opt_chair[0]["value"];
	$result->free();
	Query::dbCheckIn($db);

	$objForm->ae( array("type"       => "select",
   		                "name"       => "CHAIR",
       		            "size"       => "1",
           		        "value"      => $model->field["CHAIR"],
						"extrahtml"	 => "",
                   		"options"    => $opt_chair));

	$arg["data"]["CHAIR"] = $objForm->ge("CHAIR");

	//印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
	$objForm->ae( array("type" => "button",
    	                "name"        => "btn_print",
        	            "value"       => "プレビュー／印刷",
            	        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

	$arg["button"]["btn_print"] = $objForm->ge("btn_print");

	//終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
	$objForm->ae( array("type" => "button",
    	                "name"        => "btn_end",
        	            "value"       => "終 了",
            	        "extrahtml"   => "onclick=\"closeWin();\"" ) );

	$arg["button"]["btn_end"] = $objForm->ge("btn_end");

	//hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "DBNAME",
        	            "value"     => DB_DATABASE
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "PRGID",
        	            "value"     => "KNJM140"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	//フォーム終わり
	$arg["finish"]  = $objForm->get_finish();

	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjm140Form1.html", $arg); 

	}
}
?>
