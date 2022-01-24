<?php

require_once('for_php7.php');

/********************************************************************/
/* 机上タックシール                                 山城 2004/12/10 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：抽出条件に予備2を追加                    山城 2005/01/12 */
/********************************************************************/

class knjl300Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl300Form1", "POST", "knjl300index.php", "", "knjl300Form1");

	$opt=array();

	//年度
	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試区分コンボの設定
	$opt_test_dv = array();
	$defoult_flg = false;
	$defoult     = 1 ;		//change  05/02/02  yamauchi
	$db = Query::dbCheckOut();
	$result = $db->query(knjl300Query::get_test_div($model->ObjYear));
	$opt_test_dv[]= array("label" => "", 		//add  05/02/02  yamauchi
						  "value" => "");

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
		$model->field["TESTDV"] = $opt_test_dv[$defoult]["value"];	//NO001
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "TESTDV",
        	            "size"       => "1",
            	        "value"      => $model->field["TESTDV"],
						"extrahtml"  => "onChange=\"return btn_submit('knjl300');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDV"] = $objForm->ge("TESTDV");
	//受験型の表示
	$db = Query::dbCheckOut();
    $model->field["EXAM_TYPE"] = "2";
	$exam_div = $db->getOne(knjl300Query::get_exam_div("L005", $model->field["EXAM_TYPE"]));
	Query::dbCheckIn($db);
    knjCreateHidden($objForm, "EXAM_TYPE", $model->field["EXAM_TYPE"]);
	$arg["data"]["EXAM_TYPE"] = $exam_div;
	//試験会場選択コンボボックスを作成する
	$db = Query::dbCheckOut();
	$result = $db->query(knjl300Query::get_hall_data($model->field["TESTDV"],$model->field["EXAM_TYPE"]));
	while($Rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    	$opt_list[]= array('label' => $Rowlist["EXAMHALL_NAME"]."(".$Rowlist["S_RECEPTNO"]."～".$Rowlist["E_RECEPTNO"].")",
    					   'value' => $Rowlist["VALUE"]);

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

	//受付番号開始を作成する////////////////////////////////////////////////////////////////////////////////////
	$objForm->ae( array("type"       => "text",
    	                "name"       => "noinf_st",
                        "size"       => 5,
                        "maxlength"  => 4,
						"extrahtml"  => "onblur=\"this.value=toInteger(this.value)\"",
						"value"      => isset($model->field["NOINF_ST"])?$model->field["NOINF_ST"]:""));

	$arg["data"]["NOINF_ST"] = $objForm->ge("noinf_st");
	//受付番号終了を作成する////////////////////////////////////////////////////////////////////////////////////
	$objForm->ae( array("type"       => "text",
    	                "name"       => "noinf_ed",
                        "size"       => 5,
                        "maxlength"  => 4,
						"extrahtml"  => "onblur=\"this.value=toInteger(this.value)\"",
						"value"      => isset($model->field["NOINF_ED"])?$model->field["NOINF_ED"]:""));

	$arg["data"]["NOINF_ED"] = $objForm->ge("noinf_ed");
	//開始位置（行）コンボボックスを作成する///////////////////////////////////////////////////////////////////////
	$row = array(array('label' => "１行",'value' => 1),
				array('label' => "２行",'value' => 2),
				array('label' => "３行",'value' => 3),
				array('label' => "４行",'value' => 4),
				array('label' => "５行",'value' => 5),
				array('label' => "６行",'value' => 6),
				);

	$objForm->ae( array("type"       => "select",
    	                "name"       => "POROW",
        	            "size"       => "1",
            	        "value"      => $model->field["POROW"],
                	    "options"    => isset($row)?$row:array()));

	$arg["data"]["POROW"] = $objForm->ge("POROW");


	//開始位置（列）コンボボックスを作成する////////////////////////////////////////////////////////////////////////
	$col = array(array('label' => "１列",'value' => 1),
				array('label' => "２列",'value' => 2),
				array('label' => "３列",'value' => 3),
				);


	$objForm->ae( array("type"       => "select",
    	                "name"       => "POCOL",
        	            "size"       => "1",
            	        "value"      => $model->field["POCOL"],
                	    "options"    => isset($col)?$col:array()));

	$arg["data"]["POCOL"] = $objForm->ge("POCOL");

    //入試名なしチェックボックスを作成する////////////////////////////////////////////////////////////////////////
    $objForm->ae( array("type"       => "checkbox",
                        "name"       => "NO_TESTNAME",
                        "value"      => "1",
                        "extrahtml"  => "id=\"NO_TESTNAME\"".($model->field["NO_TESTNAME"] == "1" ? " checked" : ""))
                        );

	$arg["data"]["NO_TESTNAME"] = $objForm->ge("NO_TESTNAME");


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
        	            "value"     => "KNJL300"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl300Form1.html", $arg); 
	}
}
?>
