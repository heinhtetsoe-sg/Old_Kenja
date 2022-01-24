<?php

require_once('for_php7.php');


class knjl324jForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl324jForm1", "POST", "knjl324jindex.php", "", "knjl324jForm1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試区分コンボの設定
	$db = Query::dbCheckOut();
	$opt_test_dv = array();
	$default_flg = false;
	$default     = 0 ;

    $result = $db->query(knjl324jQuery::get_test_div("L004",$model->ObjYear));
    while($Rowtdv = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_test_dv[]= array("label" => $Rowtdv["NAME1"], 
                              "value" => $Rowtdv["NAMECD2"]);

        if ($Rowtdv["NAMESPARE2"] != 1 && !$default_flg){
            $default++;
        } else {
            $default_flg = true;
        }

    }
    $result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["TESTDIV"])) {
		$model->field["TESTDIV"] = $opt_test_dv[$default]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "TESTDIV",
        	            "size"       => "1",
            	        "value"      => $model->field["TESTDIV"],
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

	//印刷ボタンを作成する
	$objForm->ae( array("type"        => "button",
    	                "name"        => "btn_print",
        	            "value"       => "プレビュー／印刷",
            	        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

	$arg["button"]["btn_print"] = $objForm->ge("btn_print");

	//ＣＳＶボタンを作成する
	$objForm->ae( array("type"        => "button",
    	                "name"        => "btn_csv",
        	            "value"       => "ＣＳＶ出力",
                        "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

	$arg["button"]["btn_csv"] = $objForm->ge("btn_csv");

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
    	                "name"      => "LOGIN_DATE",
        	            "value"     => CTRL_DATE
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "DBNAME",
        	            "value"     => DB_DATABASE
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "PRGID",
        	            "value"     => "KNJL324J"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();

	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl324jForm1.html", $arg); 
	}
}
?>
