<?php

require_once('for_php7.php');

/********************************************************************/
/* 学生別一般費用現金入金・取消報告票               山城 2006/03/14 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjp500Form1
{
    function main(&$model){

		//オブジェクト作成
		$objForm = new form;

		//フォーム作成
		$arg["start"]   = $objForm->get_start("knjp500Form1", "POST", "knjp500index.php", "", "knjp500Form1");

		//年度を作成する
		$arg["data"]["YEAR"] = CTRL_YEAR;

		//中高判定フラグを作成する
		$db = Query::dbCheckOut();
		$row = $db->getOne(knjp500Query::GetJorH());
		if ($row == 1){
			$jhflg = 1;
		}else {
			$jhflg = 2;
		}
		Query::dbCheckIn($db);
		$objForm->ae( array("type" => "hidden",
							"name" => "JHFLG",
							"value"=> $jhflg ) );

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
        		            "value"     => "KNJP500"
            		        ) );

		//フォーム終わり
		$arg["finish"]  = $objForm->get_finish();

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
		View::toHTML($model, "knjp500Form1.html", $arg); 
	}
}
?>
