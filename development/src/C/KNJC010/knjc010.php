<?php

require_once('for_php7.php');
// kanji=漢字
// $Id: knjc010.php 56585 2017-10-22 12:47:53Z maeshiro $
class knjc010
{
 function main(&$model)
 {

		$objForm = new form;
		// フォーム作成
		$arg["start"]   = $objForm->get_start("knjc010", "POST", "knjc010index.php", "", "knjc010");
		
		$wrk = VARS::request("STAFFCD");
		
		$arg["STAFFCD"] = $model->staffcd;
		if( isset($model->syoribi) ){
			$arg["syoribi"] = str_replace("/", "-", $model->syoribi);
		}
		else{
			$arg["syoribi"] = isset($syoribi)?str_replace("/", "-", $syoribi):str_replace("/", "-", $model->control["学籍処理日"]);
		}
		$arg["kouji"]   = $model->kouji;
		$arg["debug"]   = isset($debug)? $debug:"true";
		$arg["dbname"]  = DB_DATABASE;
		
		//
		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "cmd"
		                    ) );
		
		
		$arg["finish"]  = $objForm->get_finish();
		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
		View::toHTML($model, "knjc010.html", $arg); 

 }
}


?>
