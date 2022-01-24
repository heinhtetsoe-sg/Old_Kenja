<?php

require_once('for_php7.php');

/********************************************************************/
/* クラス編成一覧（掲示用）                         山城 2006/03/17 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：フリガナの出力可否追加                   山城 2006/03/20 */
/********************************************************************/

class knja082Form1
{
    function main(&$model){

		$objForm = new form;
		//フォーム作成
		$arg["start"]   = $objForm->get_start("knja082Form1", "POST", "knja082index.php", "", "knja082Form1");

		$opt=array();

		//年度
		$arg["data"]["YEAR"] = $model->nextyear;

		/*----------*/
		/* 帳票種類 */
		/*----------*/
		$output = array();
		$output[0] = 1;
		$output[1] = 2;

        for ($i = 1; $i <= 2; $i++) {
            $name = "OUTPUT".$i;
            $objForm->ae( array("type"		=> "radio",
                                "name"		=> "OUTPUT",
                                "value"		=> 1,
                                "extrahtml"	=> "id=\"$name\"",
                                "options"	=> $output));

            $arg["data"][$name] = $objForm->ge("OUTPUT",$i);
        }

		/*------------*/
		/* 学年コンボ */
		/*------------*/
		$db = Query::dbCheckOut();
		$grade = array();

		$result = $db->query(knja082Query::GetGrade($model));
		while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
			$grade[] = array('label' => $row["NAME"],
							 'value' => $row["CD"]);
		}
		$result->free();

		$grade[] = array('label' => "全学年",
						 'value' => "99");

		$objForm->ae( array("type"		=> "select",
							"name"		=> "GRADE",
							"size"		=> "1",
							"value"		=> "$model->grade",
							"options"	=> isset($grade)?$grade:array()));

		$arg["data"]["GRADE"] = $objForm->ge("GRADE");

		Query::dbCheckIn($db);

		//ふりがな出力チェックボックスを作成する NO001
		$objForm->ae( array("type"		=> "checkbox",
							"name"		=> "OUTPUT3",
							"value"		=> "on",
							"extrahtml" => "checked id=\"OUTPUT3\""));

		$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT3");

		//中高判定フラグを作成する
		$db = Query::dbCheckOut();
		$row = $db->getOne(knja082Query::GetJorH());
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
		$objForm->ae( array("type"		=> "button",
        	                "name"		=> "btn_print",
            	            "value"		=> "プレビュー／印刷",
                	        "extrahtml"	=> "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

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
            	            "value"     => $model->nextyear
                 		    ) );

		$objForm->ae( array("type"      => "hidden",
        	                "name"      => "SEMESTER",
            	            "value"     => CTRL_SEMESTER
                 		    ) );

		$objForm->ae( array("type"      => "hidden",
        	                "name"      => "DBNAME",
            	            "value"     => DB_DATABASE
                 		    ) );

		$objForm->ae( array("type"      => "hidden",
        	                "name"      => "PRGID",
            	            "value"     => "KNJA082"
                	        ) );

		$objForm->ae( array("type"      => "hidden",
        	                "name"      => "cmd"
            	            ) );

		$arg["finish"]  = $objForm->get_finish();
		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
		View::toHTML($model, "knja082Form1.html", $arg); 
	}
}
?>
