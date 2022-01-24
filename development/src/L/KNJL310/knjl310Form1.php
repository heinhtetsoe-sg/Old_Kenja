<?php

require_once('for_php7.php');

/********************************************************************/
/* 受験者チェックリスト                             山城 2004/12/10 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：抽出条件に年度,予備2を追加               山城 2005/01/12 */
/* ･NO002：ソート処理修正                           山城 2004/01/12 */
/********************************************************************/

class knjl310Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl310Form1", "POST", "knjl310index.php", "", "knjl310Form1");

	$opt=array();

	//年度
	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl310Query::get_apct_div("L003",$model->ObjYear));	/* NO001 */

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
						"extrahtml"  => " onChange=\"return btn_submit('knjl310');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");

	//入試区分コンボの設定
	$opt_test_dv = array();
	$defoult_flg = false;
	$defoult     = 0 ;
	$db = Query::dbCheckOut();
	$result = $db->query(knjl310Query::get_test_div($model->ObjYear));	/* NO001 */

	while($Rowtdv = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_test_dv[]= array("label" => $Rowtdv["NAME1"], 
							  "value" => $Rowtdv["NAMECD2"]);
		//NO001 ↓
		if ($Rowtdv["NAMESPARE2"] != 1 && !$defoult_flg){
			$defoult++;
		} else {
			$defoult_flg = true;
		}
		//NO001 ↑
	}
	$result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["TESTDV"])) {
		$model->field["TESTDV"] = $opt_test_dv[$defoult]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "TESTDV",
        	            "size"       => "1",
            	        "value"      => $model->field["TESTDV"],
						"extrahtml"  => "onChange=\"return btn_submit('knjl310');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDV"] = $objForm->ge("TESTDV");
	//受験型の表示
	$db = Query::dbCheckOut();
    $model->field["EXAM_TYPE"] = "2";
	$exam_type = $db->getOne(knjl310Query::get_exam_div("L005", $model->field["EXAM_TYPE"]));
	Query::dbCheckIn($db);
    knjCreateHidden($objForm, "EXAM_TYPE", $model->field["EXAM_TYPE"]);
	$arg["data"]["EXAM_TYPE"] = $exam_type;
	//試験会場選択コンボボックスを作成する
	$db = Query::dbCheckOut();
	$result = $db->query(knjl310Query::get_hall_data($model->field["TESTDV"],$model->field["EXAM_TYPE"]));
	while($Rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    	$opt_list[]= array('label' => $Rowlist["EXAMHALL_NAME"],
    					   'value' => $Rowlist["EXAM_TYPE"].$Rowlist["VALUE"]);

	}
	$result->free();
	Query::dbCheckIn($db);
	//対象会場リストを作成する
	$objForm->ae( array("type"       => "select",
	                    "name"       => "category_name",
						"extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"",
        	            "size"       => "20",
            	        "options"    => array()));

	$arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");
	//会場一覧リストを作成する
	$objForm->ae( array("type"       => "select",
    	                "name"       => "category_selected",
						"extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"",
            	        "size"       => "20",
                	    "options"    => isset($opt_list)?$opt_list:array()));

	$arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");
	//対象取り消しボタンを作成する(個別)
	$objForm->ae( array("type" => "button",
    	                "name"        => "btn_right1",
        	            "value"       => "　＞　",
            	        "extrahtml"   => " onclick=\"move('right');\"" ) );

	$arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

	//対象取り消しボタンを作成する(全て)
	$objForm->ae( array("type" => "button",
    	                "name"        => "btn_right2",
        	            "value"       => "　≫　",
            	        "extrahtml"   => " onclick=\"move('rightall');\"" ) );

	$arg["button"]["btn_right2"] = $objForm->ge("btn_right2");

	//対象選択ボタンを作成する(個別)
	$objForm->ae( array("type" => "button",
    	                "name"        => "btn_left1",
        	            "value"       => "　＜　",
            	        "extrahtml"   => " onclick=\"move('left');\"" ) );

	$arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

	//対象選択ボタンを作成する(全て)
	$objForm->ae( array("type" => "button",
    	                "name"        => "btn_left2",
        	            "value"       => "　≪　",
            	        "extrahtml"   => " onclick=\"move('leftall');\"" ) );

	$arg["button"]["btn_left2"] = $objForm->ge("btn_left2");

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
        	            "value"     => "KNJL310"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl310Form1.html", $arg); 
	}
}
?>
