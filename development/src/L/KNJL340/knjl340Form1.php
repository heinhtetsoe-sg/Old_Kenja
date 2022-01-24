<?php

require_once('for_php7.php');

/********************************************************************/
/* 入試試験成績資料                                 山城 2004/12/17 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：抽出条件に年度を追加                     山城 2005/01/12 */
/********************************************************************/

class knjl340Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl340Form1", "POST", "knjl340index.php", "", "knjl340Form1");

	$db = Query::dbCheckOut();

	$opt=array();

	$arg["TOP"]["YEAR"] = $model->ObjYear;

	//入試制度コンボの設定
	$opt_apdv_typ = array();
	$result = $db->query(knjl340Query::get_apct_div("L003",$model->ObjYear));	/* NO001 */

	while($Rowtyp = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		$opt_apdv_typ[]= array("label" => $Rowtyp["NAME1"], 
							   "value" => $Rowtyp["NAMECD2"]);
	}

	$result->free();

	if (!isset($model->field["APDIV"])) {
		$model->field["APDIV"] = $opt_apdv_typ[0]["value"];
	}

	$objForm->ae( array("type"       => "select",
    	                "name"       => "APDIV",
        	            "size"       => "1",
            	        "value"      => $model->field["APDIV"],
						"extrahtml"  => " onChange=\"return btn_submit('knjl340');\"",
                    	"options"    => $opt_apdv_typ));

	$arg["data"]["APDIV"] = $objForm->ge("APDIV");

    //入試区分・・・特別アップ合格通知書のみ指定可能
    $opt    = array();
    $result = $db->query(knjl340Query::getTestdivMst($model->ObjYear));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($row["NAMESPARE2"] != "1") continue;
        $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
    }
    $result->free();
    $opt[] = array("label" => "9：全体", "value" => "9");
    $model->field["TESTDIV"] = (!strlen($model->field["TESTDIV"])) ? $opt[0]["value"] : $model->field["TESTDIV"];
    $extra = "";
    $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

	//帳票種類ラジオを作成
	$opt_print_type = array();
	$opt_print_type[] = 1;
	$opt_print_type[] = 2;
	$opt_print_type[] = 3;
	if (!$model->field["OUTPUT"]){
		$model->field["OUTPUT"] = 1;
	}
	$objForm->ae( array("type"		=> "radio",
						"name"		=> "OUTPUT",
						"value"		=> $model->field["OUTPUT"],
						"options"	=> $opt_print_type));

	$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
	$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
	$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);
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
        	            "value"     => "KNJL340"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	Query::dbCheckIn($db);

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl340Form1.html", $arg); 
	}
}
?>
