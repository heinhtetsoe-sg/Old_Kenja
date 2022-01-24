<?php

require_once('for_php7.php');

/********************************************************************/
/* 前・後期重複志願者名簿                           山城 2005/08/08 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001：出力帳票種別、手続・入学欄出力可否を追加  山城 2005/08/25 */
/********************************************************************/

class knjl349Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl349Form1", "POST", "knjl349index.php", "", "knjl349Form1");

	$opt=array();

	$arg["data"]["YEAR"] = $model->ObjYear;

	//出力順ラジオ作成 NO001
	$out_sort[0] = 1;
	$out_sort[1] = 2;
	$out_sort[2] = 3;
	$out_sort[3] = 4;

	$objForm->ae( array("type"     => "radio",
						"name"     => "OUTPRINT",
						"value"    => "1",
						"multipre" => $out_sort));
	$arg["data"]["OUTPRINT1"] = $objForm->ge("OUTPRINT",1);
	$arg["data"]["OUTPRINT2"] = $objForm->ge("OUTPRINT",2);
	$arg["data"]["OUTPRINT3"] = $objForm->ge("OUTPRINT",3);
	$arg["data"]["OUTPRINT4"] = $objForm->ge("OUTPRINT",4);

	//出力順ラジオ作成
	$out_sort[0] = 1;
	$out_sort[1] = 2;

	$objForm->ae( array("type"     => "radio",
						"name"     => "OUTPUT",
						"value"    => "1",
						"multipre" => $out_sort));
	$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
	$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

	//手続・入学チェックボックス作成 NO001
	$objForm->ae( array("type"     => "checkbox",
						"name"     => "OUTPUT3",
						"value"    => "on",
						"extrahtml"=> "checked"));

	$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT3");

	//中高判別フラグを作成する
	$jhflg = 0;
	$db = Query::dbCheckOut();
	$row = $db->getOne(knjl349Query::GetJorH());
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
        	            "value"     => "KNJL349"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl349Form1.html", $arg); 
	}
}
?>
