<?php

require_once('for_php7.php');

/********************************************************************/
/* オリエンテーリング名簿                           山城 2005/02/03 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjl332rForm1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("knjl332rForm1", "POST", "knjl332rindex.php", "", "knjl332rForm1");

	$opt=array();
	
    //DB接続
    $db = Query::dbCheckOut();

    //年度
	$arg["TOP"]["ENTEXAMYEAR"] = $model->ObjYear;

    //入試制度
    $opt = array();
    $value_flg = false;
    $result = $db->query(knjl332rQuery::getNameMst($model, $model->ObjYear, "L003"));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //2:中学固定とする
        if ($row["VALUE"] != "2") {
            continue;
        }

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') $model->field["APPLICANTDIV"] = $row["VALUE"];
        if ($model->field["APPLICANTDIV"] == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] && $value_flg) ? $model->field["APPLICANTDIV"] : $opt[0]["value"];
    $extra = " onchange=\"return btn_submit('knjl332r');\"";
    $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt, $extra, 1);

	//帳票種類ラジオを作成
	$opt_print_type = array();
	$opt_print_type[] = 1;
	$opt_print_type[] = 2;
	if (!$model->field["OUTPUT"]){
		$model->field["OUTPUT"] = 1;
	}
	$objForm->ae( array("type"		=> "radio",
						"name"		=> "OUTPUT",
						"value"		=> $model->field["OUTPUT"],
						"options"	=> $opt_print_type));

	$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
	$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
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
    	                "name"      => "ENTEXAMYEAR",
        	            "value"     => $model->ObjYear
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "CTRL_DATE",
        	            "value"     => CTRL_DATE
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "DBNAME",
        	            "value"     => DB_DATABASE
             		    ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "PRGID",
        	            "value"     => "KNJL332R"
            	        ) );

	$objForm->ae( array("type"      => "hidden",
    	                "name"      => "cmd"
        	            ) );

	$arg["finish"]  = $objForm->get_finish();
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjl332rForm1.html", $arg); 
	}
}
?>
