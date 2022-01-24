<?php

require_once('for_php7.php');

/********************************************************************/
/* 不合格者一覧表                                   山城 2004/12/17 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：抽出条件に年度,予備2を追加               山城 2005/01/12 */
/* ･NO002：合格者名簿を追加                         山城 2006/01/10 */
/********************************************************************/

class knjl323Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl323Form1", "POST", "knjl323index.php", "", "knjl323Form1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl323Query::get_apct_div("L003",$model->ObjYear));	/* NO001 */

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
						"extrahtml"  => " onChange=\"return btn_submit('knjl323');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");
	//入試区分コンボの設定
	$opt_test_dv = array();
	$defoult_flg = false;
	$defoult     = 1 ;
	$db = Query::dbCheckOut();
	$result = $db->query(knjl323Query::get_test_div($model->ObjYear));	/* NO001 */
	$opt_test_dv[]= array("label" => "", 
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
	$opt_test_dv[]= array("label" => "-- 全て --", 
						  "value" => "9");
	$result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["TESTDV"])) {
		$model->field["TESTDV"] = $opt_test_dv[$defoult]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "TESTDV",
        	            "size"       => "1",
            	        "value"      => $model->field["TESTDV"],
						"extrahtml"  => "onChange=\"return btn_submit('knjl323');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDV"] = $objForm->ge("TESTDV");
	//帳票種類ラジオの設定
	$opt_print_type   = array();
	$opt_print_type[] = 1;
	$opt_print_type[] = 2;
	$opt_print_type[] = 3;	//NO002
    $opt_print_type[] = 4;
    $opt_print_type[] = 5;
    $opt_print_type[] = 6;  //非正規合格者名簿
	if (!$model->field["OUTPUT"]){
		$model->field["OUTPUT"] = 3;	//NO002
	}
	$objForm->ae( array("type"       => "radio",
    	                "name"       => "OUTPUT",
            	        "value"      => $model->field["OUTPUT"],
                    	"options"    => $opt_print_type));

	$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
	$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
	$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);	//NO002
    $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT",4);
    $arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT",5);
    $arg["data"]["OUTPUT6"] = $objForm->ge("OUTPUT",6);

    //合格種類ラジオの設定
    $opt_print_type   = array();
    $opt_print_type[] = 1;
    $opt_print_type[] = 2;
    if (!$model->field["SUCTYPE"]){
        $model->field["SUCTYPE"] = 1;
    }
    $objForm->ae( array("type"       => "radio",
                        "name"       => "SUCTYPE",
                        "value"      => $model->field["SUCTYPE"],
                        "options"    => $opt_print_type));

    $arg["data"]["SUCTYPE1"] = $objForm->ge("SUCTYPE",1);
    $arg["data"]["SUCTYPE2"] = $objForm->ge("SUCTYPE",2);

	//印刷ボタンを作成する
	$objForm->ae( array("type" => "button",
    	                "name"        => "btn_print",
        	            "value"       => "プレビュー／印刷",
            	        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

	$arg["button"]["btn_print"] = $objForm->ge("btn_print");

    //ＣＳＶ出力ボタンを作成する
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

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
        	            "value"     => "KNJL323"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl323Form1.html", $arg); 
	}
}
?>
