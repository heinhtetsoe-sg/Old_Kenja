<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴 校納金入金状況一覧                      山城 2005/06/21 */
/*                                                                  */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjp320Form1
{
    function main(&$model){

		//オブジェクト作成
		$objForm = new form;

		//フォーム作成
		$arg["start"]   = $objForm->get_start("knjp320Form1", "POST", "knjp320index.php", "", "knjp320Form1");

		//年度作成
		$arg["data"]["YEAR"] = CTRL_YEAR;

		//年度作成
		$arg["data"]["GAKKI"] = CTRL_SEMESTER;

		//申込み種別を表示

		$db = Query::dbCheckOut();
		$query = knjp320Query::getappli();
		$result = $db->query($query);
        $err_row = array();
		while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
			$err_row[] = array('label' => $row["APPLICATIONNAME"],
							   'value' => $row["APPLICATIONCD"]);
		}
		$result->free();
		Query::dbCheckIn($db);
		$db = Query::dbCheckOut();

		$objForm->ae( array("type"		=> "select",
    		                "name"		=> "APPLICATIONNAME",
        		            "value"		=> $model->field["APPLICATIONNAME"],
        		            "options"	=> $err_row ) );

		$arg["data"]["APPLICATIONNAME"] = $objForm->ge("APPLICATIONNAME");

		//帳票出力種別ラジオボタンを作成する
		$out_syu = array();
		$out_syu[0] = 1;
		$out_syu[1] = 2;

		$objForm->ae( array("type"		=> "radio",
    		                "name"		=> "OUTPUTA",
							"value"		=> isset($model->field["OUTPUTA"])?$model->field["OUTPUTA"]:1,
        		            "extrahtml"	=> "onclick=\"return btn_submit('knjp320');\"",
            		        "multiple"	=> $out_syu ) );

		$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUTA",'1');
		$arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUTA",'2');

		//帳票出力種別ラジオボタンを作成する
		$out_syo = array();
		$out_syo[0] = 1;
		$out_syo[1] = 2;
		$albed = "";
		if ($model->field["OUTPUTA"] == 2) $albed = "disabled";

		$objForm->ae( array("type"		=> "radio",
    		                "name"		=> "OUTPUTB",
							"value"		=> isset($model->field["OUTPUTB"])?$model->field["OUTPUTB"]:1,
        		            "extrahtml"	=> " onclick=\"return btn_submit('knjp320');\"".$albed,
            		        "multiple"	=> $out_syo ) );

		$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUTB",'1');
		$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUTB",'2');

		//ヘッダ出力チェックボックスを作成する
		$checked = "";
		if ($model->field["HYOUSI"] == "on") $checked = "checked";
		if ($model->field["OUTPUTB"] == 2 || $model->field["OUTPUTA"] == 2){
			$hyomei = "disabled";
			$checked = "";
		}else {
			$hyomei = "";
		}

		if ($model->field["OUTPUTB"] == 2) $hyomei = "disabled";

		$objForm->ae( array("type"		=> "checkbox",
    		                "name"		=> "HYOUSI",
							"value"		=> "on",
        		            "extrahtml"	=> $hyomei. $checked ) );

		$arg["data"]["HYOUSI"] = $objForm->ge("HYOUSI");

		//明細出力チェックボックスを作成する
		$checked = "";
		if ($model->field["MEISAI"] == "on" ) $checked = "checked";

		if ($model->field["OUTPUTB"] == 2 || $model->field["OUTPUTA"] == 2){
			$hyomei = "disabled";
			$checked = "";
		}else {
			$hyomei = "";
		}

		if ($model->field["OUTPUTB"] == 2) $hyomei = "disabled";

		$objForm->ae( array("type"		=> "checkbox",
    		                "name"		=> "MEISAI",
							"value"		=> "on",
        		            "extrahtml"	=> $hyomei. $checked ) );

		$arg["data"]["MEISAI"] = $objForm->ge("MEISAI");

		//クラス順ソートチェックボックスを作成する
		$checked = "";
		if ($model->field["OUTPUTC"] == "on" ) $checked = "checked";
		if ($model->field["OUTPUTA"] == 2){
			$albed = "";
		}else {
			$albed = "disabled";
			$checked = "";
		}
		$objForm->ae( array("type"		=> "checkbox",
    		                "name"		=> "OUTPUTC",
							"value"		=> "on",
        		            "extrahtml"	=> $albed. $checked ) );

		$arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUTC");

		//印刷ボタンを作成する
		$objForm->ae( array("type" 		  => "button",
    		                "name"        => "btn_print",
        		            "value"       => "プレビュー／印刷",
            		        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

		$arg["button"]["btn_print"] = $objForm->ge("btn_print");

		//終了ボタンを作成する
		$objForm->ae( array("type" 		  => "button",
    		                "name"        => "btn_end",
        		            "value"       => "終 了",
            		        "extrahtml"   => "onclick=\"closeWin();\"" ) );

		$arg["button"]["btn_end"] = $objForm->ge("btn_end");

		//hiddenを作成する(必須)
		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "DBNAME",
        		            "value"      => DB_DATABASE
            		        ) );

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "cmd"
        		            ) );

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "PRGID",
        		            "value"     => "KNJP320"
            		        ) );

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "YEAR",
        		            "value"     => CTRL_YEAR,
            		        ) );

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "GAKKI",
        		            "value"     => CTRL_SEMESTER,
            		        ) );

		//フォーム終わり
		$arg["finish"]  = $objForm->get_finish();

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
		View::toHTML($model, "knjp320Form1.html", $arg); 
	}
}
?>
