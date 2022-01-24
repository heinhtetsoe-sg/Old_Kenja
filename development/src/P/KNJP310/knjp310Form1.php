<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴 校納金消し込み処理印刷                  山城 2005/06/21 */
/*                                                                  */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjp310Form1
{
    function main(&$model){

		//オブジェクト作成
		$objForm = new form;

		//フォーム作成
		$arg["start"]   = $objForm->get_start("knjp310Form1", "POST", "knjp310index.php", "", "knjp310Form1");

		//エラーコード取得

		$db = Query::dbCheckOut();
		$query = knjp310Query::geterrcd();
		$result = $db->query($query);
		while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
			$err_row[] = array('COLNAME' => $row["COLNAME"],
							   'ERR_MSG' => $row["ERR_MSG"]);
		}
		$result->free();
		Query::dbCheckIn($db);

		//申込み種別を表示

		$appliname = "";
		if ($err_row[0]["COLNAME"] != ""){
			$db = Query::dbCheckOut();
			$appliname = $db->getOne(knjp310Query::getappli($err_row[0]["COLNAME"],$err_row[0]["ERR_MSG"]));
			Query::dbCheckIn($db);

			$objForm->ae( array("type"      => "hidden",
    			                "name"      => "APPLICATIONNAME",
        			            "value"      => $err_row[0]["ERR_MSG"]
            			        ) );

		}
		$arg["data"]["APPLICATIONNAME"] = $appliname;

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

		if (!$err_row[0]["COLNAME"]){
			$arg["err_msg_not"] = " err_msg_not(); ";
		}

		//中学判定
		$schooldiv = 0;
		$db = Query::dbCheckOut();
		$query = knjp310Query::getschool();
		$result = $db->query($query);
		while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
			$schooldiv = 1;
		}
		$result->free();
		Query::dbCheckIn($db);

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "SCHOOLDIV",
        		            "value"      => $schooldiv
            		        ) );

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
    		                "name"      => "GAKKI",
        		            "value"     => CTRL_SEMESTER,
            		        ) );

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "cmd"
        		            ) );

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "PRGID",
        		            "value"     => "KNJP310"
            		        ) );

		//フォーム終わり
		$arg["finish"]  = $objForm->get_finish();

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
		View::toHTML($model, "knjp310Form1.html", $arg); 
	}
}
?>
