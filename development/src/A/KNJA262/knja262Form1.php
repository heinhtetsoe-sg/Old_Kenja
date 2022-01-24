<?php

require_once('for_php7.php');

/********************************************************************/
/* 在校生クラス割振りデータ出力                     山城 2006/03/10 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 :                                          name yyyy/mm/dd */
/********************************************************************/

class knja262Form1
{
	function main(&$model)
	{
		$db = Query::dbCheckOut();
		$objForm = new form;
		$arg["start"] = $objForm->get_start("main", "POST", "knja262index.php", "", "main");
		
		//年度学期表示
		$arg["YEAR"] = CTRL_YEAR+1;

		//学年
		$opt = array();
		$opt[] = array("label" => "1年生","value" => "01");
		$opt[] = array("label" => "2年生","value" => "02");

		$objForm->ae( array("type"		=> "select",
							"name"		=> "GRADE",
							"size"		=> "1",
							"value"		=> $model->grade,
							"options"	=> $opt));
		$arg["GRADE"] = $objForm->ge("GRADE");

		if (!$model->grade){
			$model->grade = $opt[0]["value"];
		}

		Query::dbCheckIn($db);

		//実行
		$objForm->ae( array("type"		=> "button",
							"name"		=> "btn_exec",
							"value"		=> "実 行",
							"extrahtml"	=> "onclick=\"return btn_submit('exec');\"" ));
		$arg["btn_exec"] = $objForm->ge("btn_exec");

		//終了
		$objForm->ae( array("type"		=> "button",
							"name"		=> "btn_end",
							"value"		=> "終 了",
							"extrahtml"	=> "onclick=\"closeWin();\"" ) );
		$arg["btn_end"] = $objForm->ge("btn_end");

		//hidden
		$objForm->ae( array("type"		=> "hidden",
							"name"		=> "cmd") );

		$arg["finish"]	= $objForm->get_finish();

		View::toHTML($model, "knja262Form1.html", $arg);
	}
}
?>
