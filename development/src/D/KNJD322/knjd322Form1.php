<?php

require_once('for_php7.php');


class knjd322Form1
{
    function main(&$model){

		//オブジェクト作成
		$objForm = new form;

		//フォーム作成
		$arg["start"]   = $objForm->get_start("knjd322Form1", "POST", "knjd322index.php", "", "knjd322Form1");

		//年度
		$arg["data"]["YEAR"] = CTRL_YEAR;

		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "YEAR",
		                    "value"      => CTRL_YEAR ) );

		//学期
		$arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "GAKKI",
		                    "value"     => CTRL_SEMESTER ) );

		//学年リストボックスを作成する
		$opt_schooldiv = "学年";
		$db = Query::dbCheckOut();
		$opt_grade=array();
		$query = knjd322Query::getSelectGrade($model);
		$result = $db->query($query);
		$i=0;
		$grade_flg = true;
		while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		    $grade_show= sprintf("%d",$row["GRADE"]);
			$opt_grade[] = array('label' => $grade_show.$opt_schooldiv,
								 'value' => $row["GRADE"]);
			$i++;
			if( $model->field["GRADE"]==$row["GRADE"] ) $grade_flg = false;
		}
		if( $grade_flg ) $model->field["GRADE"] = $opt_grade[0]["value"];
		$result->free();
		Query::dbCheckIn($db);

		$objForm->ae( array("type"       => "select",
		                    "name"       => "GRADE",
		                    "size"       => $i,
		                    "value"      => $model->field["GRADE"],
		                    "options"    => $opt_grade,
							"extrahtml"	 => "multiple" ) );

		$arg["data"]["GRADE"] = $objForm->ge("GRADE");
/*
		//科目数テキストボックスを作成する
		$objForm->ae( array("type"       => "text",
		                    "name"       => "SUBCLASS",
		                    "size"       => "2",
		                    "maxlength"  => 2,
		                    "extrahtml"  => "onblur=\"this.value=toInteger(this.value)\"",
		                    "value"      => isset($model->field["SUBCLASS"])?$model->field["SUBCLASS"]:"4") );

		$arg["data"]["SUBCLASS"] = $objForm->ge("SUBCLASS");

		//帳票選択ラジオボタンを作成
		$opt[0]=1;
		$opt[1]=2;
		$objForm->ae( array("type"       => "radio",
		                    "name"       => "OUTPUT",
							"value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:1,
							"multiple"   => $opt));

		$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
		$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
*/
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
		                    "value"     => DB_DATABASE
		                    ) );

		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "PRGID",
		                    "value"     => "KNJD322"
		                    ) );

		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "cmd"
		                    ) );

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

		//フォーム終わり
		$arg["finish"]  = $objForm->get_finish();

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
		View::toHTML($model, "knjd322Form1.html", $arg); 
	}
}
?>
