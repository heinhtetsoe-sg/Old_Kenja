<?php

require_once('for_php7.php');


class knjl353jForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl353jForm1", "POST", "knjl353jindex.php", "", "knjl353jForm1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試区分コンボの設定
    $opt_test_dv = array();
    $test_flg = false;	//入試区分データフラグ
	$default     = 0 ;
    
    $db = Query::dbCheckOut();
    $result = $db->query(knjl353jQuery::get_test_div("L004",$model->ObjYear));	

    while($Rowtdv = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_test_dv[]= array("label" => $Rowtdv["NAME1"], 
                              "value" => $Rowtdv["NAMECD2"]);

        if ($Rowtdv["NAMESPARE2"] != 1 && !$test_flg){
            $default++;
        } else {
            $test_flg = true;
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
						"extrahtml"  => "onChange=\"return btn_submit('knjl353j');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

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
    	                "name"      => "PRGID",
        	            "value"     => "KNJL353J"
            	        ) );
                        
    $objForm->ae( array("type"      => "hidden",
    	                "name"      => "LOGIN_DATE",
        	            "value"     => CTRL_DATE
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();

	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl353jForm1.html", $arg); 
	}
}
?>
