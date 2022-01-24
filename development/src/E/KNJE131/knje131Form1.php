<?php

require_once('for_php7.php');


class knje131Form1
{
    function main(&$model){

		//オブジェクト作成
		$objForm = new form;

		//フォーム作成
		$arg["start"]   = $objForm->get_start("knje131Form1", "POST", "knje131index.php", "", "knje131Form1");

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
		$query = knje131Query::getSelectGrade($model);
		$result = $db->query($query);
		$grade_defalt = "03";
		$grade_flg = true;
		while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		    $grade_show= sprintf("%d",$row["GRADE"]);
			$opt_grade[] = array('label' => $grade_show.$opt_schooldiv,
								 'value' => $row["GRADE"]);
    		$grade_defalt = $row["GRADE"];//デフォルト
			if( $model->field["GRADE"]==$row["GRADE"] ) $grade_flg = false;
		}
		if( $grade_flg ) $model->field["GRADE"] = $grade_defalt;
		$result->free();
		Query::dbCheckIn($db);

		$objForm->ae( array("type"       => "select",
		                    "name"       => "GRADE",
		                    "size"       => "1",
		                    "value"      => $model->field["GRADE"],
		                    "options"    => $opt_grade,
							"extrahtml"	 => "onChange=\"return btn_submit('knje131');\"" ) );

		$arg["data"]["GRADE"] = $objForm->ge("GRADE");

        //クラス一覧リスト作成する
        $db = Query::dbCheckOut();
        $query = knje131Query::getAuth($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
        					"extrahtml"  => "multiple style=\"width:200px\" ondblclick=\"move1('left')\"",
                            "size"       => "10",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
        					"extrahtml"  => "multiple style=\"width:200px\" ondblclick=\"move1('right')\"",
                            "size"       => "10",
                            "options"    => array()));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


        //教科一覧リスト作成する
        $db = Query::dbCheckOut();
        $query = knje131Query::getSelectClassMst();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row2[] = array('label' => $row["CLASSCD"] . "　" . $row["CLASSNAME"],
                            'value' => $row["CLASSCD"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASSCD_NAME",
        					"extrahtml"  => "multiple style=\"width:200px\" ondblclick=\"move2('left')\"",
                            "size"       => "10",
                            "options"    => isset($row2)?$row2:array()));

        $arg["data"]["CLASSCD_NAME"] = $objForm->ge("CLASSCD_NAME");

        //出力対象教科リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASSCD_SELECTED",
        					"extrahtml"  => "multiple style=\"width:200px\" ondblclick=\"move2('right')\"",
                            "size"       => "10",
                            "options"    => array()));

        $arg["data"]["CLASSCD_SELECTED"] = $objForm->ge("CLASSCD_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn2_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves2('right');\"" ) );

        $arg["button"]["btn2_rights"] = $objForm->ge("btn2_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn2_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves2('left');\"" ) );

        $arg["button"]["btn2_lefts"] = $objForm->ge("btn2_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn2_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move2('right');\"" ) );

        $arg["button"]["btn2_right1"] = $objForm->ge("btn2_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn2_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move2('left');\"" ) );

        $arg["button"]["btn2_left1"] = $objForm->ge("btn2_left1");


		//印刷ボタンを作成する
		$objForm->ae( array("type" 		  => "button",
		                    "name"        => "btn_csv",
		                    "value"       => "ＣＳＶ出力",
		                    "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

		$arg["button"]["btn_csv"] = $objForm->ge("btn_csv");

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
		                    "value"     => "KNJE131"
		                    ) );

		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "cmd"
		                    ) );

    	$objForm->ae( array("type"      => "hidden",
    	                    "name"      => "selectdata") );  

    	$objForm->ae( array("type"      => "hidden",
    	                    "name"      => "selectdata2") );  

		//フォーム終わり
		$arg["finish"]  = $objForm->get_finish();

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
		View::toHTML($model, "knje131Form1.html", $arg); 
	}
}
?>
