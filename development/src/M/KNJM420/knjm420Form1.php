<?php

require_once('for_php7.php');

/********************************************************************/
/* SHR出席チェックリスト                            山城 2005/04/05 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm420Form1
{
    function main(&$model){

	//オブジェクト作成
	$objForm = new form;

	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjm420Form1", "POST", "knjm420index.php", "", "knjm420Form1");

	//年度
	$arg["YEAR"] = CTRL_YEAR;

	//日付コンボを作成する
	if ($model->Date == ""){
		$model->Date = str_replace("-","/",CTRL_DATE);
		$model->semester = CTRL_SEMESTER;
	}
	$arg["data"]["DATE"] = View::popUpCalendar($objForm	,"DATE"	,str_replace("-","/",$model->Date),"reload=true");
	//チェック用hidden
	$objForm->ae( array("type"      => "hidden",
						"value"     => $model->Date,
						"name"      => "DEFOULTDATE") );

	//CLASSコンボを作成する
	$opt_class = array();
	$i = 1;

	$db = Query::dbCheckOut();
	$query = knjm420Query::GetClass($model);
	$result = $db->query($query);
	$opt_class[0] = array('label' => "",
						'value' => 0);
	while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_class[$i] = array('label' => str_replace("-","/",$row["LABEL"]),
							   'value' => $row["VALUE"]);
		$i++;
	}
	if($model->field["CLASS"]=="") $model->field["CLASS"] = $opt_class[0]["value"];
	$result->free();
	Query::dbCheckIn($db);

	$objForm->ae( array("type"       => "select",
   		                "name"       => "CLASS",
       		            "size"       => "1",
           		        "value"      => $model->field["CLASS"],
						"extrahtml"	 => "",
                   		"options"    => $opt_class));

	$arg["data"]["CLASS"] = $objForm->ge("CLASS");

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
    	                "name"      => "YEAR",
        	            "value"     => CTRL_YEAR
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "SEMESTER",
        	            "value"     => $model->semester
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "PRGID",
        	            "value"     => "KNJM420"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	//フォーム終わり
	$arg["finish"]  = $objForm->get_finish();

	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjm420Form1.html", $arg); 

	}
}
?>
