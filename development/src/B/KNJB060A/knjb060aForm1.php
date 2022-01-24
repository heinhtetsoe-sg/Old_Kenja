<?php

require_once('for_php7.php');


class knjb060aForm1
{
    function main(&$model){
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb060aForm1", "POST", "knjb060aindex.php", "", "knjb060aForm1");

        $opt=array();

        //ラジオボタンを作成//時間割種別（基本時間割/通常時間割）
        $opt[0]=1;
        $opt[1]=2;
        for ($i = 1; $i <= 2; $i++) {
            $name = "RADIO".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "RADIO",
                                "value"      => isset($model->field["RADIO"])?$model->field["RADIO"]:"1",
                                "extrahtml"  => "onclick=\"jikanwari(this);\" id=\"$name\"",
                                "multiple"   => $opt));

            $arg["data"][$name] = $objForm->ge("RADIO",$i);
        }

        if( $model->field["RADIO"] == 2 ) {     //通常時間割選択時
            $dis_jikan = "disabled";                //時間割選択コンボ使用不可
            $dis_date  = "";                        //指定日付テキスト使用可
		    $arg["Dis_Date"]  = " dis_date(false); " ;
        } else {                                //基本時間割選択時
            $dis_jikan = "";                        //時間割選択コンボ使用可
            $dis_date  = "disabled";                //指定日付テキスト使用不可
		    $arg["Dis_Date"]  = " dis_date(true); " ;
        }


        //時間割選択コンボボックスを作成
        $row2 = knjb060aQuery::getBscHdQuery($model);
        $objForm->ae( array("type"       => "select",
                            "name"       => "TITLE",
                            "size"       => "1",
                            "value"      => $model->field["TITLE"],
                            "options"    => isset($row2)?$row2:array(),
        					"extrahtml"  => "$dis_jikan "));
//        					"extrahtml"  => "$dis_jikan onchange=\"conbo_select();\""));

        $arg["data"]["TITLE"] = $objForm->ge("TITLE");


        //指定日付テキストボックスを作成//////////////////////////////////////////////////////////////////////////////////
/*		$arg["data"]["DATE"] = View::popUpCalendar($objForm,"DATE",$model->control["学籍処理日"]);

        $objForm->ae( array("type"       => "text",
                            "name"       => "DATE2",
                            "size"       => "12",
                            "value"      => $model->control["学籍処理日"],
        					"extrahtml"  => "disabled"));

        $arg["data"]["DATE2"] = $objForm->ge("DATE2");
*/
        //指定日付テキストボックスを作成
		if ($model->field["RADIO"] == 2){
			if (!isset($model->field["DATE"]))
				$model->field["DATE"] = $model->control["学籍処理日"];
	        //指定日を含む指定週の開始日(月曜日)と終了日(日曜日)を取得
    	    common::DateConv2($model->field["DATE"],$OutDate1,$OutDate2,1);
    	    $model->field["DATE2"] = $OutDate2;
		} else {
			$model->field["DATE"] = "";
			$model->field["DATE2"] = "";
		}
		$arg["data"]["DATE"] = View::popUpCalendar($objForm,"DATE",$model->field["DATE"],"reload=true");

        $objForm->ae( array("type"       => "text",
                            "name"       => "DATE2",
                            "size"       => "12",
                            "value"      => $model->field["DATE2"],
        					"extrahtml"  => "disabled"));

        $arg["data"]["DATE2"] = $objForm->ge("DATE2");


        //ラジオボタンを作成//出力区分（職員／学級／生徒）//////////////////////////////////////////////////////////////////
        $opt1[0]=1;
        $opt1[1]=2;
        $opt1[2]=3;
        for ($i = 1; $i <= 3; $i++) {
            $name = "KUBUN".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "KUBUN",
                                "value"      => isset($model->field["KUBUN"])?$model->field["KUBUN"]:"1",
                                "extrahtml"  => "onclick=\"shutu_kubun(this);\" id=\"$name\"",
                                "multiple"   => $opt1));

            $arg["data"][$name] = $objForm->ge("KUBUN",$i);
        }

        if (isset($model->field["KUBUN"]))
        {
        	switch ($model->field["KUBUN"])
        	{
        		case 1:
        			$dis_name1 = "";
        			$dis_name2 = "";
        			$dis_cla1 = "disabled";
        			$dis_cla2 = "disabled";
        			$dis_cla3 = "disabled";
        			$dis_cla4 = "disabled";
        			break;
        		case 2:
        			$dis_name1 = "disabled";
        			$dis_name2 = "disabled";
        			$dis_cla1 = "";
        			$dis_cla2 = "";
        			$dis_cla3 = "disabled";
        			$dis_cla4 = "disabled";
        			break;
        		case 3:
        			$dis_name1 = "disabled";
        			$dis_name2 = "disabled";
        			$dis_cla1 = "disabled";
        			$dis_cla2 = "disabled";
        			$dis_cla3 = "";
        			$dis_cla4 = "";
        			break;
        	}
        }
        else
        {
        	$dis_name1 = "";
        	$dis_name2 = "";
        	$dis_cla1 = "disabled";
        	$dis_cla2 = "disabled";
        	$dis_cla3 = "disabled";
        	$dis_cla4 = "disabled";
        }

        //所属選択コンボボックスを作成
        $row1 = knjb060aQuery::getSectQuery($model->control["年度"]);		//04/04/21  yamauchi

        $objForm->ae( array("type"       => "select",
                            "name"       => "SECTION_CD_NAME1",
                            "size"       => "1",
                            "value"      => $model->field["SECTION_CD_NAME1"],
        					"extrahtml"	 =>$dis_name1,
                            "options"    => isset($row1)?$row1:array()));

        $objForm->ae( array("type"       => "select",
                            "name"       => "SECTION_CD_NAME2",
                            "size"       => "1",
                            "value"      => isset($model->field["SECTION_CD_NAME2"])?$model->field["SECTION_CD_NAME2"]:$row1[get_count($row1)-1]["value"],
        					"extrahtml"	 =>$dis_name2,
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["SECTION_CD_NAME1"] = $objForm->ge("SECTION_CD_NAME1");
        $arg["data"]["SECTION_CD_NAME2"] = $objForm->ge("SECTION_CD_NAME2");


        //クラス選択コンボボックスを作成する
        $row3 = knjb060aQuery::getHrclass($model->control["年度"],$model->control["学期"]);

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS1",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS1"],
        					"extrahtml"  => $dis_cla1,
                            "options"    => isset($row3)?$row3:array()));

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS2",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS2"],
        					"extrahtml"  => $dis_cla2,
                            "options"    => isset($row3)?$row3:array()));

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS3",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS3"],
        					"extrahtml"  => $dis_cla3,
                            "options"    => isset($row3)?$row3:array()));

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS4",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS4"],
        					"extrahtml"  => $dis_cla4,
                            "options"    => isset($row3)?$row3:array()));

        $arg["data"]["GRADE_HR_CLASS1"] = $objForm->ge("GRADE_HR_CLASS1");
        $arg["data"]["GRADE_HR_CLASS2"] = $objForm->ge("GRADE_HR_CLASS2");
        $arg["data"]["GRADE_HR_CLASS3"] = $objForm->ge("GRADE_HR_CLASS3");
        $arg["data"]["GRADE_HR_CLASS4"] = $objForm->ge("GRADE_HR_CLASS4");

        /**********/
        /* ラジオ */
        /**********/
        //出力項目(上段)
        $opt = array(1, 2); //1:科目名 2:講座名
        $model->field["SUBCLASS_CHAIR_DIV"] = ($model->field["SUBCLASS_CHAIR_DIV"] == "") ? "1" : $model->field["SUBCLASS_CHAIR_DIV"];
        $extra = array("id=\"SUBCLASS_CHAIR_DIV1\"", "id=\"SUBCLASS_CHAIR_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "SUBCLASS_CHAIR_DIV", $model->field["SUBCLASS_CHAIR_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;


        /********************/
        /* チェックボックス */
        /********************/
        //授業が無い「校時」を詰める、詰めないのチェックボックス---2005.07.04
       	$check = ($model->field["CHECK"] == "on") ? "checked" : "" ;
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "CHECK",
        					"value"		=> "on",
        					"extrahtml"	=> $check." id=\"CHECK\"" ) );
        $arg["data"]["CHECK"] = $objForm->ge("CHECK");
        //「テスト時間割のみ出力」チェックボックス---2006/11/01
       	$t_check = ($model->field["TEST_CHECK"] == "on") ? "checked" : "" ;
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "TEST_CHECK",
                            "value"     => "on",
                            "extrahtml" => $t_check." id=\"TEST_CHECK\"" ) );
        $arg["data"]["TEST_CHECK"] = $objForm->ge("TEST_CHECK");
        //職員は正担任（MAX職員番号）のみ出力
        $staff_check = ($model->field["STAFF_CHECK"] == "on") ? "checked" : "" ;
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "STAFF_CHECK",
                            "value"     => "on",
                            "extrahtml" => $staff_check." id=\"STAFF_CHECK\"" ) );
        $arg["data"]["STAFF_CHECK"] = $objForm->ge("STAFF_CHECK");
        //クラス名は出力しない
        $no_class_check = ($model->field["NO_CLASS_CHECK"] == "on") ? "checked" : "" ;
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "NO_CLASS_CHECK",
                            "value"     => "on",
                            "extrahtml" => $no_class_check." id=\"NO_CLASS_CHECK\"" ) );
        $arg["data"]["NO_CLASS_CHECK"] = $objForm->ge("NO_CLASS_CHECK");


        //印刷ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
		                    "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJB060A"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //年度
        $arg["data"]["YEAR"] = $model->control["年度"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => $model->control["年度"],
                            ) );

        //JavaScriptで参照するため
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKUSEKI",
                            "value"      => $model->control["学籍処理日"]
                            ) );

        //学校区分---2005.05.19
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHOOLDIV",
                            "value"     => knjb060aQuery::getSchooldiv($model),
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "T_YEAR"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "T_BSCSEQ"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "T_SEMESTER"
                            ) );
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useProficiency", $model->Properties["useProficiency"]);
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "notShowStaffcd", $model->Properties["notShowStaffcd"]);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb060aForm1.html", $arg); 
    }

}
?>
