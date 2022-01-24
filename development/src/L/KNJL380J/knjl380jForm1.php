<?php

require_once('for_php7.php');


class knjl380jForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl380jForm1", "POST", "knjl380jindex.php", "", "knjl380jForm1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//印刷指定ラジオの設定
	$opt_output   = array();
	$opt_output[] = 1;    //仮クラス
	$opt_output[] = 2;    //クラス

	if (!$model->field["TITLE"]){
		$model->field["TITLE"] = 1;
	}

	$objForm->ae( array("type"       => "radio",
    	                "name"       => "TITLE",
            	        "value"      => $model->field["TITLE"],
                    	"options"    => $opt_output));

	$arg["data"]["TITLE1"] = $objForm->ge("TITLE",1);
	$arg["data"]["TITLE2"] = $objForm->ge("TITLE",2);

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
    	                "name"      => "SEMESTER",
        	            "value"     => "1"
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "GRADE",
        	            "value"     => "01"
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "DBNAME",
        	            "value"     => DB_DATABASE
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "LOGIN_DATE",
        	            "value"     => CTRL_DATE
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "PRGID",
        	            "value"     => "KNJL380J"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();

	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl380jForm1.html", $arg); 
	}
}
?>
