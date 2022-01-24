<?php

require_once('for_php7.php');

/********************************************************************/
/* 入学試験統計資料(実数)                           山城 2005/02/17 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：入試区分を追加                           山城 2005/12/29 */
/* ･NO002：合格対象チェックボックスを追加           仲本 2006/01/05 */
/* ･NO003：実数時は、チェックボックスをONで使用不可 山城 2006/01/19 */
/********************************************************************/

class knjl350Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl350Form1", "POST", "knjl350index.php", "", "knjl350Form1");

	$opt=array();

	$arg["data"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定 NO001
	$opt_apdv_typ = array();
	$db = Query::dbCheckOut();
	$result = $db->query(knjl350Query::get_apct_div("L003",$model->ObjYear));

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
						"extrahtml"  => " onChange=\"return btn_submit('knjl350');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");

	//入試区分コンボの設定 NO001
	$opt_test_dv = array();
	$defoult_flg = false;
	$defoult     = 0 ;
	$db = Query::dbCheckOut();
	$result = $db->query(knjl350Query::get_test_div($model->ObjYear));

	while($Rowtdv = $result->fetchRow(DB_FETCHMODE_ASSOC)){

		$opt_test_dv[]= array("label" => $Rowtdv["NAME"], 
							  "value" => $Rowtdv["TESTDIV"]);

		if ($Rowtdv["SHOWDIV"] != 1 && !$defoult_flg){
			$defoult++;
		} else {
			$defoult_flg = true;
		}

	}
	$opt_test_dv[]= array("label" => "実数", 
						  "value" => "99");
	$result->free();
	Query::dbCheckIn($db);

	if (!isset($model->field["TESTDV"])) {
		$model->field["TESTDV"] = $opt_test_dv[$defoult]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "TESTDV",
        	            "size"       => "1",
            	        "value"      => $model->field["TESTDV"],
						"extrahtml"  => "onchange=\" check1val(this);\"",	//NO003
                    	"options"    => $opt_test_dv));

	$arg["data"]["TESTDV"] = $objForm->ge("TESTDV");


	//合格対象チェックボックスの作成---NO002
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
        	            "value"     => "KNJL350"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl350Form1.html", $arg); 
	}
}
?>
