<?php

require_once('for_php7.php');

/********************************************************************/
/* 4科目成績一覧表                                  山城 2004/12/15 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：抽出条件に年度,予備2を追加               山城 2005/01/12 */
/* ･NO002：2科目に未満データ条件を追加              山城 2005/01/19 */
/* ･NO003：レイアウトを変更                         仲本 2005/02/02 */
/********************************************************************/

class knjl321Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl321Form1", "POST", "knjl321index.php", "", "knjl321Form1");

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl321Query::get_apct_div("L003",$model->ObjYear));	/* NO001 */

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
						"extrahtml"  => " onChange=\"return btn_submit('knjl321');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");
	//入試区分コンボの設定
	$opt_test_dv = array();
	$defoult_flg = false;
	$defoult     = 1 ;
	$db = Query::dbCheckOut();
	$result = $db->query(knjl321Query::get_test_div($model->ObjYear));	/* NO001 */
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

	$result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["TESTDV"])) {
		$model->field["TESTDV"] = $opt_test_dv[$defoult]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "TESTDV",
        	            "size"       => "1",
            	        "value"      => $model->field["TESTDV"],
						"extrahtml"  => "onChange=\"return btn_submit('knjl321');\"",
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDV"] = $objForm->ge("TESTDV");
	//帳票出力チェックボックスの作成
	//表紙
	if ($model->field["HYOUSI"] == "1" || $model->cmd == "") {
		$check_kintai = "checked";
		$hyou = 1;
	}else {
		$check_kintai = "";
		$hyou = 0;
	}

	$objForm->ae( array("type"  => "checkbox",
    	                "name"  => "HYOUSI",
						"value"	=> "1",
						"extrahtml"	=> $check_kintai) );

	$arg["data"]["HYOUSI"] = $objForm->ge("HYOUSI");

	//４科目
	if ($model->field["KAMOKU4"] == "1" || $model->cmd == "") {
		$check_kintai = "checked";
		$kdata4 = 1;
	}else {
		$check_kintai = "";
		$kdata4 = 0;
	}

	$objForm->ae( array("type"  => "checkbox",
    	                "name"  => "KAMOKU4",
						"value"	=> "1",
						"extrahtml"	=> $check_kintai) );

	$arg["data"]["KAMOKU4"] = $objForm->ge("KAMOKU4");

	//指定範囲テキスト作成(４科目合計)
	$objForm->ae( array("type"       => "text",
    	                "name"       => "TESTSCR2",
                        "size"       => 3,
                        "maxlength"  => 3,
						"extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"",
						"value"      => isset($model->field["TESTSCR2"])?$model->field["TESTSCR2"]:"0"));

	$arg["data"]["TESTSCR2"] = $objForm->ge("TESTSCR2");

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
        	            "value"     => "KNJL321"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl321Form1.html", $arg); 
	}
}
?>
