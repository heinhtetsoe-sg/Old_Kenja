<?php

require_once('for_php7.php');


class knjl306jForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl306jForm1", "POST", "knjl306jindex.php", "", "knjl306jForm1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;
    
    
    $db = Query::dbCheckOut();
    
    //一般出願期間取得
    $Row1 = $db->getRow(knjl306jQuery::get_test_date_range("L003", $model->ObjYear, 1), DB_FETCHMODE_ASSOC);
    
    //帰国生出願期間取得
    $Row2 = $db->getRow(knjl306jQuery::get_test_date_range("L003", $model->ObjYear, 2), DB_FETCHMODE_ASSOC);
    
    Query::dbCheckIn($db);

	//一般出願期間（開始）
	$value = isset($model->field["TEST_DATE1_FROM"])?$model->field["TEST_DATE1_FROM"]:str_replace("-","/", $Row1["ABBV2"]);
	$arg["data"]["TEST_DATE1_FROM"] = View::popUpCalendar($objForm, "TEST_DATE1_FROM", $value);
    
    //一般出願期間（終了）
	$value = isset($model->field["TEST_DATE1_TO"])?$model->field["TEST_DATE1_TO"]:str_replace("-","/", $Row1["ABBV3"]);
	$arg["data"]["TEST_DATE1_TO"] = View::popUpCalendar($objForm, "TEST_DATE1_TO", $value);
    
    //帰国生出願期間（開始）
	$value = isset($model->field["TEST_DATE2_FROM"])?$model->field["TEST_DATE2_FROM"]:str_replace("-","/", $Row2["ABBV2"]);
	$arg["data"]["TEST_DATE2_FROM"] = View::popUpCalendar($objForm, "TEST_DATE2_FROM", $value);
    
    //帰国生出願期間（終了）
	$value = isset($model->field["TEST_DATE2_TO"])?$model->field["TEST_DATE2_TO"]:str_replace("-","/", $Row2["ABBV3"]);
	$arg["data"]["TEST_DATE2_TO"] = View::popUpCalendar($objForm, "TEST_DATE2_TO", $value);

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
        	            "value"     => "KNJL306J"
            	        ) );
    
    $objForm->ae( array("type"      => "hidden",
    	                "name"      => "LOGIN_DATE",
        	            "value"     => CTRL_DATE
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );
                        
    //日付範囲のチェックデータ
    $objForm->ae( array("type"      => "hidden",
    	                "name"      => "TEST_DATE_FROM1",
        	            "value"     => $Row1["ABBV2"]
             		    ) );
    
    $objForm->ae( array("type"      => "hidden",
    	                "name"      => "TEST_DATE_TO1",
        	            "value"     => $Row1["ABBV3"]
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "TEST_DATE_FROM2",
        	            "value"     => $Row2["ABBV2"]
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "TEST_DATE_TO2",
        	            "value"     => $Row2["ABBV3"]
            	        ) );

	$arg["finish"]  = $objForm->get_finish();

	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl306jForm1.html", $arg); 
	}
}
?>
