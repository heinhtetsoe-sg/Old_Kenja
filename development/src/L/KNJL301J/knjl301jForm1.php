<?php

require_once('for_php7.php');


class knjl301jForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl301jForm1", "POST", "knjl301jindex.php", "", "knjl301jForm1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試区分コンボの設定
    $opt_test_dv = array();
    $test_flg = false;	//入試区分データフラグ
	$default = 0 ;
    
    $db = Query::dbCheckOut();
    $result = $db->query(knjl301jQuery::get_test_div("L004",$model->ObjYear));	

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
    
    //入試区分データが存在する時
    if ($test_flg) {
        $opt_test_dv[]= array("label" => "-- 全て --", 
                               "value" => 0);
    } else {
        $opt_test_dv[]= array("label" => "　　　", 
                               "value" => 0);
    }

	if (!isset($model->field["TESTDIV"])) {
		$model->field["TESTDIV"] = $opt_test_dv[$default]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "TESTDIV",
        	            "size"       => "1",
            	        "value"      => $model->field["TESTDIV"],
						"extrahtml"  => "onChange=\"return btn_submit('knjl301j');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

	//出力順ラジオの設定
	$opt_sort   = array();
	$opt_sort[] = 1;
	$opt_sort[] = 2;

	if (!$model->field["SORT"]){
		$model->field["SORT"] = 1;
	}

	$objForm->ae( array("type"       => "radio",
    	                "name"       => "SORT",
            	        "value"      => $model->field["SORT"],
                    	"options"    => $opt_sort));

	$arg["data"]["SORT1"] = $objForm->ge("SORT",1);
	$arg["data"]["SORT2"] = $objForm->ge("SORT",2);

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
        	            "value"     => "KNJL301J"
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
	View::toHTML($model, "knjl301jForm1.html", $arg); 
	}
}
?>
