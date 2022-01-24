<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴 奨学生一覧表                            山城 2006/03/22 */
/*                                                                  */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjp392Form1
{
    function main(&$model){

		//オブジェクト作成
		$objForm = new form;

		//フォーム作成
		$arg["start"]   = $objForm->get_start("knjp392Form1", "POST", "knjp392index.php", "", "knjp392Form1");

		//年度を作成する
		$arg["data"]["YEAR"] = CTRL_YEAR;

		//交付区分コンボ作成
		$opt_grant = array();
		$db = Query::dbCheckOut();
		$result = $db->query(knjp392Query::getGrant());
		while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
			$opt_grant[] = array ("label"   => $row["NAME1"],
								  "value"   => $row["GRANTCD"]);
		}

		$opt_grant[] = array ("label"   => "全て",
							  "value"   => "99");
		$result->free();
		Query::dbCheckIn($db);

		if (!$model->grant){
			$model->grant = $opt_grant[0]["value"];
		}

		$objForm->ae( array("type"		=> "select",
							"name"		=> "GRANT",
							"size"		=> 1,
							"value"		=> $model->grant,
							"options"	=> $opt_grant ) );

		$arg["data"]["GRANT"] = $objForm->ge("GRANT");

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
    		                "name"      => "YEAR",
        		            "value"     => CTRL_YEAR,
            		        ) );

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "SEMESTER",
        		            "value"     => CTRL_SEMESTER,
            		        ) );

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "cmd"
        		            ) );

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "PRGID",
        		            "value"     => "KNJP392"
            		        ) );

		//フォーム終わり
		$arg["finish"]  = $objForm->get_finish();

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
		View::toHTML($model, "knjp392Form1.html", $arg); 
	}
}
?>
