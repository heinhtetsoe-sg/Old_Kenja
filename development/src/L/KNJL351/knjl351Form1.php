<?php

require_once('for_php7.php');

/********************************************************************/
/* 入学試験成績資料(総合)                           山城 2005/02/17 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjl351Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl351Form1", "POST", "knjl351index.php", "", "knjl351Form1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//帳票種類ラジオを作成
	$opt_print_type = array();
	$opt_print_type[] = 1;
	$opt_print_type[] = 2;
	$opt_print_type[] = 3;
	if (!$model->field["OUTPUT"]){
		$model->field["OUTPUT"] = 1;
	}
	$objForm->ae( array("type"		=> "radio",
						"name"		=> "OUTPUT",
						"value"		=> $model->field["OUTPUT"],
						"options"	=> $opt_print_type));

	$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
	$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
	$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);

	//印刷ボタンを作成する
	$objForm->ae( array("type" => "button",
    	                "name"        => "btn_print",
        	            "value"       => "プレビュー／印刷",
            	        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

	$arg["button"]["btn_print"] = $objForm->ge("btn_print");

	//CSVボタンを作成する
	$objForm->ae( array("type" => "button",
    	                "name"        => "csv_print",
        	            "value"       => "ＣＳＶ出力",
            	        "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

	$arg["button"]["csv_print"] = $objForm->ge("csv_print");

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
        	            "value"     => "KNJL351"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl351Form1.html", $arg); 
	}
}
?>
