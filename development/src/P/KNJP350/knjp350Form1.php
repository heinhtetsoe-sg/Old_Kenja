<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴 授業料軽減補助金人数内訳                山城 2005/11/30 */
/*                                                                  */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjp350Form1
{
    function main(&$model){

		//オブジェクト作成
		$objForm = new form;

		//フォーム作成
		$arg["start"]   = $objForm->get_start("knjp350Form1", "POST", "knjp350index.php", "", "knjp350Form1");

		//年度を作成する
		$arg["data"]["YEAR"] = CTRL_YEAR;

        //支援額ラジオボタン 1:支援額１（4～6月） 2:支援額２（7～3月）
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

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
    		                "name"      => "GAKKI",
        		            "value"     => CTRL_SEMESTER,
            		        ) );

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "cmd"
        		            ) );

		$objForm->ae( array("type"      => "hidden",
    		                "name"      => "PRGID",
        		            "value"     => "KNJP350"
            		        ) );

		//フォーム終わり
		$arg["finish"]  = $objForm->get_finish();

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
		View::toHTML($model, "knjp350Form1.html", $arg); 
	}
}
?>
