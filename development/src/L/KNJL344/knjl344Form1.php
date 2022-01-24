<?php

require_once('for_php7.php');

/********************************************************************/
/* 入学許可証                                  		山内 2005/01/13 */
/*                                                                  */
/* 変更履歴                                                         */
/********************************************************************/

class knjl344Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl344Form1", "POST", "knjl344index.php", "", "knjl344Form1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;
	//通知日付
	$value = isset($model->field["NOTICEDAY"])?$model->field["NOTICEDAY"]:str_replace("-","/",$model->control["学籍処理日"]);
	$arg["el"]["NOTICEDAY"] = View::popUpCalendar($objForm, "NOTICEDAY", $value);

	//帳票出力Aラジオボタン作成
	$opt_opt[] = array();
	$opt_opt[] = 1;
	$opt_opt[] = 2;

	if (!isset($model->field["OUTPUT"])){
		$model->field["OUTPUT"] = 2;
	}

	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUT",
						"value"      => $model->field["OUTPUT"],
						"extrahtml"	 => "$disp_onoff onclick=\"return btn_submit('knjl344');\"",
						"multiple"   => $opt_opt));

	$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
	$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

	//disabled設定
	if (($model->field["OUTPUT"] == 2)&&($disp_onoff == "")){
		$disp_onoff = "";
	} else {
		$disp_onoff = "disabled";
	}

	//受験番号A
	if (!isset($model->field["EXAMNO"])){
		$model->field["EXAMNO"] = "";
	}

	$objForm->ae( array("type"       => "text",
    	                "name"       => "EXAMNO",
    	                "size"       => 5,
    	                "maxlength"  => 5,
						"value"      => $model->field["EXAMNO"],
						"extrahtml"	 => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

	$arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");


	//印刷ボタンを作成する
	$objForm->ae( array("type" 		  => "button",
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
        	            "value"     => "KNJL344"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

//以下のhiddenは、印刷処理時の判定用
	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "OUT",
        	            "value"     => $model->field["OUTPUT"]
            	        ) );


	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl344Form1.html", $arg); 
	}
}
?>
