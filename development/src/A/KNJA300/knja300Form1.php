<?php

require_once('for_php7.php');

/********************************************************************/
/* レポート提出用（通信制）                         山城 2004/12/01 */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knja300Form1
{
	function main(&$model){

		//オブジェクト作成
		$objForm = new form;

		//フォーム作成
		$arg["start"] = $objForm->get_start("knja300Form1", "POST", "knja300index.php", "", "knja300Form1");

		//年度CMB
		$opt_nen = array();
		//今年度と来年度までを設定
		$opt_nen[0] = array("label"	=> $model->control["年度"],
							"value" => $model->control["年度"]);
		$opt_nen[1] = array("label"	=> (int)$model->control["年度"]+1,
							"value" => (int)$model->control["年度"]+1);
		if (!$model->field["YEAR"]){
			$model->field["YEAR"] = $model->control["年度"];
		}

		$objForm->ae( array("type"		=> "select",
							"name"		=> "YEAR",
							"size"		=> "1",
							"value"		=> $model->field["YEAR"],
							"extrahtml"	=> "style=\"width:60px \" onchange=\"return btn_submit('');\"",
							"options"	=> $opt_nen));

		$arg["data"]["YEAR"] = $objForm->ge("YEAR");
/*		再提出が行える様になったら、コメントをはずす。//////////////////////////////////////////////
		//出力方法（1:初回2:再提出）
		$opt_out_select[0]=1;
		$opt_out_select[1]=2;

		$objForm->ae( array("type"		=> "radio",
							"name"		=> "OUTPUT",
							"value"		=> isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:1,
							"multiple"	=> $opt_out_select));

		$arg["data"]["OUTPUT1"] = $objForm->ge(OUTPUT,1);
		$arg["data"]["OUTPUT2"] = $objForm->ge(OUTPUT,2);

		//科目CMB作成
		$db = Query::dbCheckOut();

		$query = knja300Query::getSubclass($model);
		$result = $db->query($query);
		$opt_subclass = array();
		
		while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
			$opt_subclass[] = array('label' => $row["SUBCLASSCD"]."  ".$row["SUBCLASSABBV"],
									'value' => $row["SUBCLASSCD"]);

		}
		$result->free();
		Query::dbCheckIn($db);
		
		if (!$model->field["KAMOKU"]){
			$model->field["KAMOKU"] = $opt_subclass[0]["value"];
		}

		$objForm->ae( array("type"		=> "select",
							"name"		=> "KAMOKU",
							"size"		=> "1",
							"value"		=> $model->field["KAMOKU"],
							"extrahtml"	=> "onchange=\"return btn_submit('');\"",
							"options"	=> $opt_subclass));

		$arg["data"]["KAMOKU"] = $objForm->ge("KAMOKU");
*//////////////////////////////////////////////////////////////////////////////////////////////////
		//応急処置↓再提出が行える様になったら、Del
		//出力方法（1:初回2:再提出）再提出処理が出来るようになったら、上のコメント
		//をはずして下さい。以下の処理は、応急処置です。
		$opt_out_select1=1;
		$opt_out_select2=1;

		$objForm->ae( array("type"		=> "radio",
							"name"		=> "OUTPUT",
							"value"		=> isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:1,
							"multiple"	=> $opt_out_select1));
		$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);

		$objForm->ae( array("type"		=> "radio",
							"name"		=> "OUTPUT2",
							"value"		=> isset($model->field["OUTPUT2"])?$model->field["OUTPUT2"]:0,
							"extrahtml"	=> "disabled",
							"multiple"	=> $opt_out_select2));

		$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT2",2);
		//応急処置↑

		//科目CMB作成
		$db = Query::dbCheckOut();
		$query = knja300Query::getSubclass($model);
		$result = $db->query($query);
		$opt_subclass = array();
		
		while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
			$opt_subclass[] = array('label' => $row["SUBCLASSCD"]."  ".$row["SUBCLASSABBV"],
									'value' => $row["SUBCLASSCD"]);

		}
		$result->free();
		Query::dbCheckIn($db);
		
		if (!$model->field["KAMOKU"]){
			$model->field["KAMOKU"] = $opt_subclass[0]["value"];
		}
/*//////再提出が行える様になったら、コメントをはずす。//////////////////////////////////////////////
		if ($model->field["OUTPUT"] != 2) {
			$extrahtml = "disabled";
		} else {
			$extrahtml = "onchange=\"return btn_submit('');\"";
		}
*///////////////////////////////////////////////////////////////////////////////////////////////////
		//応急処置↓再提出が行える様になったら、Del
		if ($model->field["OUTPUT2"] != checked) {
			$extrahtml = "disabled";
		} else {
			$extrahtml = "onchange=\"return btn_submit('');\"";
		}
		//応急処置↑
		$objForm->ae( array("type"		=> "select",
							"name"		=> "KAMOKU",
							"size"		=> "1",
							"value"		=> $model->field["KAMOKU"],
							"extrahtml"	=> $extrahtml,
							"options"	=> $opt_subclass));

		$arg["data"]["KAMOKU"] = $objForm->ge("KAMOKU");

		//レポート回数CMB作成
		$db = Query::dbCheckOut();

		$query  = knja300Query::getReportcount($model);
		$result = $db->query($query);
		$row    = $result->fetchRow(DB_FETCHMODE_ASSOC);
		$result->free();
		Query::dbCheckIn($db);
		$opt_report_count = array();
		for ($rcount = 0 ;$rcount < $row["STANDARD_NO"];$rcount++) {
			$opt_report_count[] = array('label' => $rcount+1,
										'value' => $rcount+1);
		}
		if (!$model->field["RKAISU"]){
			$model->field["RKAISU"] = 0 ;
		}
/*//////再提出が行える様になったら、コメントをはずす。//////////////////////////////////////////////
		if ($model->field["OUTPUT"] != 2) {
			$extrahtml = "disabled";
		} else {
			$extrahtml = "";
		}
*///////////////////////////////////////////////////////////////////////////////////////////////////
		//応急処置↓再提出が行える様になったら、Del
		if ($model->field["OUTPUT2"] != checked) {
			$extrahtml = "disabled";
		} else {
			$extrahtml = "";
		}
		//応急処置↑
		$objForm->ae( array("type"		=> "select",
							"name"		=> "RKAISU",
							"size"		=> "1",
							"value"		=> $model->field["RKAISU"],
							"extrahtml"	=> $extrahtml,
							"options"	=> $opt_report_count));

		$arg["data"]["RKAISU"] = $objForm->ge("RKAISU");

		//提出回数CMB作成
		
		$opt_teisyutu_count = array();
		for ($tcount = 1 ;$tcount <= 9;$tcount++) {
			$opt_teisyutu_count[] = array('label' => $tcount,
										  'value' => $tcount);
		}

		$objForm->ae( array("type"		=> "select",
							"name"		=> "TKAISU",
							"size"		=> "1",
							"value"		=> $model->field["TKAISU"],
							"extrahtml"	=> $extrahtml,
							"options"	=> $opt_teisyutu_count));

		$arg["data"]["TKAISU"] = $objForm->ge("TKAISU");

		//印刷ボタンを作成する//
		$objForm->ae( array("type"		=> "button",
							"name"		=> "btn_print",
							"value"		=> "プレビュー／印刷",
		                    "extrahtml"	=> "onclick=\"return newwin('" . SERVLET_URL . "');\""));

		$arg["button"]["btn_print"] = $objForm->ge("btn_print");

		//終了ボタンを作成する/
		$objForm->ae( array("type" => "button",
        		            "name"        => "btn_end",
                		    "value"       => "終 了",
		                    "extrahtml"   => "onclick=\"closeWin();\"" ) );

		$arg["button"]["btn_end"] = $objForm->ge("btn_end");

		//hiddenを作成する(必須)
		$objForm->ae( array("type"		=> "hidden",
							"name"		=> "DBNAME",
							"value"		=> DB_DATABASE));

		$objForm->ae( array("type"		=> "hidden",
							"name"		=> "PRGID",
							"value"		=> "KNJA300"));

		$objForm->ae( array("type"		=> "hidden",
							"name"		=> "cmd"));

		//フォーム終わり
		$arg["finish"] = $objForm->get_finish();

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
		View::toHTML($model, "knja300Form1.html", $arg);
	}
}
?>
