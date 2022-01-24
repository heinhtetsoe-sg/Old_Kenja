<?php

require_once('for_php7.php');

/********************************************************************/
/* 入試試験成績資料                                 山城 2004/12/26 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：抽出条件に年度,予備2を追加               山城 2005/01/12 */
/* ･NO002：合格対象チェックボックスのデフォルト=ON  山城 2005/01/14 */
/********************************************************************/

class knjl325Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl325Form1", "POST", "knjl325index.php", "", "knjl325Form1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl325Query::get_apct_div("L003",$model->ObjYear));	/* NO001 */

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
						"extrahtml"  => " onChange=\"return btn_submit('knjl325');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");
	//入試区分コンボの設定
	$opt_test_dv = array();
	$defoult_flg = false;
	$defoult     = 1 ;
	$db = Query::dbCheckOut();
	$result = $db->query(knjl325Query::get_test_div($model->ObjYear));	/* NO001 */
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
						"extrahtml"  => "onChange=\"return btn_submit('knjl325');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDV"] = $objForm->ge("TESTDV");
	//合格対象チェックボックスの作成
	if ($model->field["CHECK1"] == "1" || $model->cmd == "") {
		$check_kintai = "checked";
	}else {
		$check_kintai = "";
	}

	$objForm->ae( array("type"  => "checkbox",
    	                "name"  => "CHECK1",
						"value"	=> "1",
						"extrahtml"	=> $check_kintai ) );

	$arg["data"]["CHECK1"] = $objForm->ge("CHECK1");

	//最低点表示チェックボックスの作成
	if ($model->field["CHECK2"] == "1" || $model->cmd == "") {
		$check_kintai = "checked";
	}else {
		$check_kintai = "";
	}

	$objForm->ae( array("type"  => "checkbox",
    	                "name"  => "CHECK2",
						"value"	=> "1",
						"extrahtml"	=> $check_kintai ) );

	$arg["data"]["CHECK2"] = $objForm->ge("CHECK2");

	//印刷ボタンを作成する
	$objForm->ae( array("type" => "button",
    	                "name"        => "btn_print",
        	            "value"       => "プレビュー／印刷",
            	        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

	$arg["button"]["btn_print"] = $objForm->ge("btn_print");

	//CSVボタンを作成する
	$objForm->ae( array("type" => "button",
    	                "name"        => "csv_print",
        	            "value"       => "ＣＳＶ出力",
            	        "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

	$arg["button"]["csv_print"] = $objForm->ge("csv_print");

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
        	            "value"     => "KNJL325"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl325Form1.html", $arg); 
	}
}
?>
