<?php

require_once('for_php7.php');


class knjd324Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd324Form1", "POST", "knjd324index.php", "", "knjd324Form1");


        //印刷指定
        $opt[0]=1;
        $opt[1]=2;
        $opt[2]=3;//2005.10.31Add

        if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;

        $objForm->ae( array("type"       => "radio",
        	                "name"       => "OUTPUT",
        					"value"      => $model->field["OUTPUT"],
        					"extrahtml"	 => "onclick =\" return btn_submit('knjd324');\"",
        					"multiple"   => $opt));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
        $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);//2005.10.31Add

        //素点・評価表示
        if ($model->field["OUTPUT"] == 1) $arg["soten"] = $model->field["OUTPUT"];
        if ($model->field["OUTPUT"] == 2) $arg["hyoka"] = $model->field["OUTPUT"];
        if ($model->field["OUTPUT"] == 3) $arg["hyoka2"] = $model->field["OUTPUT"];//2005.10.31Add

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => CTRL_YEAR,
                            ) );
        //今学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_SEME",
                            "value"      => CTRL_SEMESTER) );

        //学期リスト
        $db = Query::dbCheckOut();
        $query = knjd324Query::getSemester($model);
        $result = $db->query($query);
        $opt_seme = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_seme[]= array('label' 	=> $row["SEMESTERNAME"],
                            	'value' => $row["SEMESTER"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($model->field["GAKKI"]=="") $model->field["GAKKI"] = CTRL_SEMESTER;

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->field["GAKKI"],
        					"extrahtml"  => "onChange=\"return btn_submit('knjd324');\"",
                            "options"    => $opt_seme));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");

        //学年リストボックス
        $opt_schooldiv = "学年";
        $db = Query::dbCheckOut();
        $opt_grade=array();
        $query = knjd324Query::getSelectGrade($model);
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
                            "size"       => 1,
                            "value"      => $model->field["GRADE"],
        					"extrahtml"  => "onChange=\"return btn_submit('knjd324');\"",
                            "options"    => $opt_grade ) );

        $arg["data"]["GRADE"] = $objForm->ge("GRADE");

        //テスト種別リスト
        if ($model->field["OUTPUT"] == 1) {
            if($model->field["GAKKI"] < 3 )
            {
               	$opt_kind[]= array('label' => '中間試験',
                        	       'value' => '01');
               	$opt_kind[]= array('label' => '期末試験',
                    	           'value' => '02');
               	$opt_kind[]= array('label' => '学期成績',
                        	       'value' => '0');
            }else if($model->field["GAKKI"] == 3){
               	$opt_kind[]= array('label' => '期末試験',
                    	           'value' => '02');
               	$opt_kind[]= array('label' => '学期成績',
                        	       'value' => '0');
            }else {
               	$opt_kind[]= array('label' => '学年成績',
                    	           'value' => '9');
//2005.10.31Add
               	$opt_kind[]= array('label' => '絶対評価(学年評定)',
                    	           'value' => '90');
                if($model->field["GRADE"] == "03" )
                {
                   	$opt_kind[]= array('label' => '相対評価(５段階)',
                        	           'value' => '91');
                   	$opt_kind[]= array('label' => '相対評価(１０段階)',
                        	           'value' => '92');
                }
            }
            $on_change = "";
        }
//2005.10.31Modify
//      if ($model->field["OUTPUT"] == 2) {
        if ($model->field["OUTPUT"] == 2 || $model->field["OUTPUT"] == 3) {
           	$opt_kind[]= array('label' => '絶対評価(学年評定)',
                	           'value' => '90');
           	$opt_kind[]= array('label' => '相対評価(５段階)',
                	           'value' => '91');
           	$opt_kind[]= array('label' => '相対評価(１０段階)',
                	           'value' => '92');
            $on_change = "onChange=\"return btn_submit('knjd324');\"";
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTKINDCD",
                            "size"       => "1",
                            "value"      => $model->field["TESTKINDCD"],
        					"extrahtml"  => $on_change,
                            "options"    => $opt_kind));

        $arg["data"]["TESTKINDCD"] = $objForm->ge("TESTKINDCD");

        //コースリストボックス
        $db = Query::dbCheckOut();
        $opt_corse=array();
        $query = knjd324Query::getSelectCorse($model);
        $result = $db->query($query);
        $i=0;
        $corse_flg = true;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        	$opt_corse[] = array('label' => $row["COURSECODENAME"],
        						 'value' => $row["COURSECODE"]);
        	$i++;
        	if( $model->field["COURSE_CD"]==$row["COURSECODE"] ) $corse_flg = false;
        }
        if( $corse_flg ) $model->field["COURSE_CD"] = $opt_corse[$i-1]["value"];
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "COURSE_CD",
                            "size"       => $i,
                            "value"      => $model->field["COURSE_CD"],
                            "options"    => $opt_corse,
        					"extrahtml"	 => "multiple" ) );

        $arg["data"]["COURSE_CD"] = $objForm->ge("COURSE_CD");


        //2005.10.31Add 学年リスト
        $db = Query::dbCheckOut();
        $gradelist=array();
        $query = knjd324Query::getGradeList($model);
        $result = $db->query($query);
        $i=0;
        $corse_flg = true;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        	$gradelist[] = array('label' => $row["GRADE_INT"] . $opt_schooldiv,
        						 'value' => $row["GRADE"]);
        	$i++;
        	if( $model->field["GRADELIST"]==$row["GRADE"] ) $corse_flg = false;
        }
        if( $corse_flg ) $model->field["GRADELIST"] = $gradelist[0]["value"];
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADELIST",
                            "size"       => $i,
                            "value"      => $model->field["GRADELIST"],
                            "options"    => $gradelist,
        					"extrahtml"	 => "multiple" ) );

        $arg["data"]["GRADELIST"] = $objForm->ge("GRADELIST");


        //クラス一覧リスト作成する
        $db = Query::dbCheckOut();
        $query = knjd324Query::getAuth($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
        					"extrahtml"  => "multiple style=\"width=185px\" width=\"185px\" ondblclick=\"move1('left')\"",
                            "size"       => "18",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");


        //出力対象クラスリストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
        					"extrahtml"  => "multiple style=\"width=185px\" width=\"185px\" ondblclick=\"move1('right')\"",
                            "size"       => "18",
                            "options"    => array()));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");


        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


        //異動対象日付
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm	,"DATE"	,$model->field["DATE"]);

        //印刷ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "' , '" . $model->field["OUTPUT"] . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hiddenを作成する(必須)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        //帳票プログラム(素点・評価)
//        if ($model->field["OUTPUT"] == 1) $prgid = "KNJD324";
//        if ($model->field["OUTPUT"] == 2) $prgid = "KNJD324_2";//---2005.05.17
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "useCurriculumcd",
                            "value"     => $model->Properties["useCurriculumcd"]
                            ) );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );


        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd324Form1.html", $arg); 
    }
}
?>
