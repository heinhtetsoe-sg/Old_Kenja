<?php

require_once('for_php7.php');


class knjb040Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb040Form1", "POST", "knjb040index.php", "", "knjb040Form1");

        $opt=array();
        //ラジオボタンを作成//時間割種別（基本時間割/通常時間割）
        $opt[0]=1;
        $opt[1]=2;

        for ($i = 1; $i <= 2; $i++) {
            $name = "RADIO".$i;
            $objForm->ae( array("type"       => "radio",
                                "name"       => "RADIO",
                                "value"      => isset($model->field["RADIO"]) ? $model->field["RADIO"] : "1",
                                "extrahtml"  => "onclick=\"jikanwari(this);\" id=\"$name\"",
                                "multiple"   => $opt));

            $arg["data"][$name] = $objForm->ge("RADIO",$i);
        }

        if( $model->field["RADIO"] == 2 ) {             //通常時間割選択時
            $dis_jikan = "disabled";                    //時間割選択コンボ使用不可
            $dis_date  = "";                            //指定日付テキスト使用可
		    $arg["Dis_Date"] = " dis_date(false); " ;
        } else {                                        //基本時間割選択時
            $dis_jikan = "";                            //時間割選択コンボ使用可
            $dis_date  = "disabled";                    //指定日付テキスト使用不可
		    $arg["Dis_Date"] = " dis_date(true); " ;
        }

        //時間割選択コンボボックスを作成
        $row2 = knjb040Query::getBscHdQuery($model);

        $objForm->ae( array("type"       => "select",
                            "name"       => "TITLE",
                            "size"       => "1",
                            "value"      => $model->field["TITLE"],
                            "options"    => isset($row2) ? $row2 : array(),
        					"extrahtml"  => "$dis_jikan "));

        $arg["data"]["TITLE"] = $objForm->ge("TITLE");

        //指定日付テキストボックスを作成
		if ($model->field["RADIO"] == 2){
			if (!isset($model->field["DATE"]))
				$model->field["DATE"] = str_replace("-","/",CTRL_DATE);
	        //指定日を含む指定週の開始日(月曜日)と終了日(日曜日)を取得
    	    common::DateConv2($model->field["DATE"], $OutDate1, $OutDate2, 1);
    	    $model->field["DATE2"] = $OutDate2;
		} else {
			$model->field["DATE"] = "";
			$model->field["DATE2"] = "";
		}
		$arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"], "reload=true");

        $objForm->ae( array("type"       => "text",
                            "name"       => "DATE2",
                            "size"       => "12",
                            "value"      => $model->field["DATE2"],
        					"extrahtml"  => "disabled"));

        $arg["data"]["DATE2"] = $objForm->ge("DATE2");

        //所属選択コンボボックスを作成
        $row1 = knjb040Query::getSectQuery();

        $objForm->ae( array("type"       => "select",
                            "name"       => "SECTION_CD_NAME1",
                            "size"       => "1",
                            "value"      => $model->field["SECTION_CD_NAME1"],
                            "options"    => $row1));

        $objForm->ae( array("type"       => "select",
                            "name"       => "SECTION_CD_NAME2",
                            "size"       => "1",
                            "value"      => isset($model->field["SECTION_CD_NAME2"]) ? $model->field["SECTION_CD_NAME2"] : $row1[get_count($row1)-1]["value"],
                            "options"    => $row1));

        $arg["data"]["SECTION_CD_NAME1"] = $objForm->ge("SECTION_CD_NAME1");
        $arg["data"]["SECTION_CD_NAME2"] = $objForm->ge("SECTION_CD_NAME2");


        //チェックボックスを作成（帳票区分）
        //担当教科別時間割表
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "OUTPUT",
                            "checked"    => true,
        					"extrahtml"  => "onclick=\"kubun();\" id=\"OUTPUT\"",
        					"value"      => isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1"));

        //同時展開授業一覧表
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "OUTPUT2",
                            "checked"    => true,
        					"extrahtml"  => "onclick=\"kubun();\" id=\"OUTPUT2\"",
        					"value"      => isset($model->field["OUTPUT2"]) ? $model->field["OUTPUT2"] : "1"));

        $arg["data"]["OUTPUT"] = $objForm->ge("OUTPUT");
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT2");

        //出力選択(科目名・講座名)ラジオボタン(1:科目名 2:講座名)
        $opt = array(1, 2);
        $model->field["SUBCLASS_CHAIR_DIV"] = ($model->field["SUBCLASS_CHAIR_DIV"] == "") ? "1" : $model->field["SUBCLASS_CHAIR_DIV"];
        $extra = array("id=\"SUBCLASS_CHAIR_DIV1\"", "id=\"SUBCLASS_CHAIR_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "SUBCLASS_CHAIR_DIV", $model->field["SUBCLASS_CHAIR_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

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
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJB040"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR,
                            ) );

        //JavaScriptで参照するため
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKUSEKI",
                            "value"     => str_replace("-","/",CTRL_DATE)
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
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb040Form1.html", $arg); 
    }

}
?>
