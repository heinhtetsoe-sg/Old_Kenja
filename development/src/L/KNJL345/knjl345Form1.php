<?php

require_once('for_php7.php');

/********************************************************************/
/* オリエンテーリング名簿                           山城 2005/02/03 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjl345Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl345Form1", "POST", "knjl345index.php", "", "knjl345Form1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl345Query::get_apct_div("L003",$model->ObjYear));

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
						"extrahtml"  => " onChange=\"return btn_submit('knjl345');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");
	//帳票種類ラジオを作成
	$opt_print_type = array();
	$opt_print_type[] = 1;
	$opt_print_type[] = 2;
	if (!$model->field["OUTPUT"]){
		$model->field["OUTPUT"] = 1;
	}
	$objForm->ae( array("type"		=> "radio",
						"name"		=> "OUTPUT",
						"value"		=> $model->field["OUTPUT"],
						"options"	=> $opt_print_type));

	$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
	$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
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
        	            "value"     => "KNJL345"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl345Form1.html", $arg); 
	}
}
?>
