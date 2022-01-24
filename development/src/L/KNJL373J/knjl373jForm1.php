<?php

require_once('for_php7.php');


class knjl373jForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl373jForm1", "POST", "knjl373jindex.php", "", "knjl373jForm1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//印刷指定ラジオの設定
	$opt_output   = array();
	$opt_output[] = 1;    //塾名無し
	$opt_output[] = 2;    //塾名有り

	if (!$model->field["OUTPUT"]){
		$model->field["OUTPUT"] = 1;
	}

	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUT",
            	        "value"      => $model->field["OUTPUT"],
                    	"options"    => $opt_output));

	$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
	$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

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
        	            "value"     => CTRL_SEMESTER
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
        	            "value"     => "KNJL373J"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();

	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl373jForm1.html", $arg); 
	}
}
?>
