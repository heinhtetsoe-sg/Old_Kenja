<?php
class knjb0030oForm2
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjb0030oindex.php", "", "edit");

		//１レコード取得（講座データ）
        if ($model->cmd != "group" && !isset($model->warning))
        {    
            $Row = knjb0030oQuery::getRow_test($model->term, $model->chaircd);
            if ($model->Properties["useCurriculumcd"] == '1') {
                $Row["SUBCLASSCD"] = $Row["CLASSCD"].'-'.$Row["SCHOOL_KIND"].'-'.$Row["CURRICULUM_CD"].'-'.$Row["SUBCLASSCD"];
            }
        } else {
            $Row =& $model->fields;
        }
		//レコード取得（その他）
        if ($model->cmd != "group" && !isset($model->warning))
        {    
	        $db  = Query::dbCheckOut();

			//使用施設
	        $result = $db->query(knjb0030oQuery::getFac($model->term, $model->chaircd));
	        $opt_lab = $opt_val = array();
	        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    	    {
	            $opt_lab[] = $row["FACILITYABBV"];
    	        $opt_val[] = $row["FACCD"];
        	}
            $Row_Fac["FACILITYABBV"] 	= implode(",",$opt_lab);
            $Row_Fac["FACCD"] 			= implode(",",$opt_val);
			//教科書
	        $result = $db->query(knjb0030oQuery::getTextbook($model->term, $model->chaircd));
	        $opt_lab = $opt_val = array();
	        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    	    {
	            $opt_lab[] = $row["TEXTBOOKABBV"];
    	        $opt_val[] = $row["TEXTBOOKCD"];
        	}
            $Row_Textbook["TEXTBOOKABBV"] 	= implode(",",$opt_lab);
            $Row_Textbook["TEXTBOOKCD"] 	= implode(",",$opt_val);
			//科目担任
	        $result = $db->query(knjb0030oQuery::getStaff($model->term, $model->chaircd));
	        $opt_lab = $opt_lab1 = $opt_val = $opt_val2 = $opt_val3 = array();
	        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    	    {
				if ($row["CHARGEDIV"]=="1") {
		            $opt_lab1[]  = $row["STAFFNAME_SHOW"];	//正
				} else {
		            $opt_lab[]  = $row["STAFFNAME_SHOW"];	//副
				}
    	        $opt_val[]  = $row["STAFFCD"];
    	        $opt_val2[] = $row["CHARGEDIV"];
    	        $opt_val3[] = $row["STAFFCD"]."-".$row["CHARGEDIV"];	//ダイアログで正副を判断するために使用
        	}
            $Row_Staff["STAFFNAME_SHOW1"] 	= implode(",",$opt_lab1);
            $Row_Staff["STAFFNAME_SHOW"] 	= implode(",",$opt_lab);
            $Row_Staff["STAFFCD"] 			= implode(",",$opt_val);
            $Row_Staff["CHARGEDIV"] 		= implode(",",$opt_val2);
            $Row_Staff["STF_CHARGE"] 		= implode(",",$opt_val3);
			//受講クラス
	        $result = $db->query(knjb0030oQuery::getGradeClass($model->term, $model->chaircd, $Row["GROUPCD"]));
	        $opt_lab = $opt_val = array();
	        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    	    {
	            $opt_lab[] = $row["HR_NAMEABBV"];
    	        $opt_val[] = $row["GRADE_CLASS"];
        	}
            $Row_GradeClass["HR_NAMEABBV"] 	= implode(",",$opt_lab);
            $Row_GradeClass["GRADE_CLASS"] 	= implode(",",$opt_val);

	        Query::dbCheckIn($db);
        } else {
            $Row_Fac["FACILITYABBV"] 	= $model->fields["FACILITYABBV"];
            $Row_Fac["FACCD"] 			= $model->fields["FACCD"];
            $Row_Textbook["TEXTBOOKABBV"] 	= $model->fields["TEXTBOOKABBV"];
            $Row_Textbook["TEXTBOOKCD"] 	= $model->fields["TEXTBOOKCD"];
            $Row_Staff["STAFFNAME_SHOW1"] 	= $model->fields["STAFFNAME_SHOW1"];
            $Row_Staff["STAFFNAME_SHOW"] 	= $model->fields["STAFFNAME_SHOW"];
            $Row_Staff["STAFFCD"] 			= $model->fields["STAFFCD"];
            $Row_Staff["CHARGEDIV"] 		= $model->fields["CHARGEDIV"];
            $Row_Staff["STF_CHARGE"] 		= $model->fields["STF_CHARGE"];
            $Row_GradeClass["HR_NAMEABBV"] 	= $model->fields["HR_NAMEABBV"];
            $Row_GradeClass["GRADE_CLASS"] 	= $model->fields["GRADE_CLASS"];
        }
		//授業回数と受講クラス選択ボタンの使用可・不可
        if ($Row["GROUPCD"] > "0000"){
			$read_lesson = "STYLE=\"background-color:darkgray\" readonly";
			$dis_subform1 = "disabled";
        } else {
			$read_lesson = "";
			$dis_subform1 = "";
        }
		//群コードが変更されたときの処理
        if ($model->cmd == "group"){
	        if ($Row["GROUPCD"] > "0000"){
				//授業回数
	            $Row_lesson = knjb0030oQuery::getRow_lesson($model->term, $Row["GROUPCD"]);
    	        $Row["LESSONCNT"] = $Row_lesson["LESSONCNT"];
        	    $Row["FRAMECNT"] = $Row_lesson["FRAMECNT"];
				//受講クラス
	        	$db  = Query::dbCheckOut();
		        $result = $db->query(knjb0030oQuery::getGradeClass($model->term, $model->chaircd, $Row["GROUPCD"]));
	    	    $opt_lab = $opt_val = array();
	        	while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	    	    {
		            $opt_lab[] = $row["HR_NAMEABBV"];
    		        $opt_val[] = $row["GRADE_CLASS"];
        		}
	            $Row_GradeClass["HR_NAMEABBV"] 	= implode(",",$opt_lab);
    	        $Row_GradeClass["GRADE_CLASS"] 	= implode(",",$opt_val);
	    	    Query::dbCheckIn($db);
    	    } else {
    	        $Row["LESSONCNT"] = "";
        	    $Row["FRAMECNT"] = "";
	            $Row_GradeClass["HR_NAMEABBV"] 	= "";
    	        $Row_GradeClass["GRADE_CLASS"] 	= "";
        	}
        }

        //講座コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "CHAIRCD",
                            "size"        => 8,
                            "maxlength"   => 7,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["CHAIRCD"]));
        $arg["data"]["CHAIRCD"] = $objForm->ge("CHAIRCD");

        //講座名称
        $objForm->ae( array("type"        => "text",
                            "name"        => "CHAIRNAME",
                            "size"        => 31,
                            "maxlength"   => 30,
                            "extrahtml"   => "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"",
                            "value"       => $Row["CHAIRNAME"] ));
        $arg["data"]["CHAIRNAME"] = $objForm->ge("CHAIRNAME");

        //講座略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "CHAIRABBV",
                            "size"        => 16,
                            "maxlength"   => 15,
                            "extrahtml"   => "",
                            "value"       => $Row["CHAIRABBV"] ));
        $arg["data"]["CHAIRABBV"] = $objForm->ge("CHAIRABBV");

        //科目コード
        $objForm->ae( array("type"        => "select",
                            "name"        => "SUBCLASSCD",
                            "size"        => 1,
                            "value"       => $Row["SUBCLASSCD"],
                            "options"      => knjb0030oQuery::getSubclass($model,substr($model->term,0,4)) ));
        $arg["data"]["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");

        //履修期間区分
        $objForm->ae( array("type"        => "select",
                            "name"        => "TAKESEMES",
                            "size"        => 1,
                            "value"       => $Row["TAKESEMES"],
                            "options"      => knjb0030oQuery::getTakesemes(substr($model->term,0,4)) ));
        $arg["data"]["TAKESEMES"] = $objForm->ge("TAKESEMES");

        //週授業回数
        $objForm->ae( array("type"        => "text",
                            "name"        => "LESSONCNT",
                            "size"        => 3,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"".$read_lesson,
                            "value"       => $Row["LESSONCNT"]));
        $arg["data"]["LESSONCNT"] = $objForm->ge("LESSONCNT");

        //連続枠数
        $objForm->ae( array("type"        => "text",
                            "name"        => "FRAMECNT",
                            "size"        => 3,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"".$read_lesson,
                            "value"       => $Row["FRAMECNT"]));
        $arg["data"]["FRAMECNT"] = $objForm->ge("FRAMECNT");

        //使用施設
        $objForm->ae( array("type"        => "text",
                            "name"        => "FACILITYABBV",
                            "size"        => 31,
                            "extrahtml"   => "STYLE=\"WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly",
                            "value"       => $Row_Fac["FACILITYABBV"] ));
        $arg["data"]["FACILITYABBV"] = $objForm->ge("FACILITYABBV");

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "FACCD",
                            "value"     => $Row_Fac["FACCD"]
                            ) );

        //教科書
        $objForm->ae( array("type"        => "text",
                            "name"        => "TEXTBOOKABBV",
                            "size"        => 31,
                            "extrahtml"   => "STYLE=\"WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly",
                            "value"       => $Row_Textbook["TEXTBOOKABBV"] ));
        $arg["data"]["TEXTBOOKABBV"] = $objForm->ge("TEXTBOOKABBV");

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TEXTBOOKCD",
                            "value"     => $Row_Textbook["TEXTBOOKCD"]
                            ) );

	    //科目担任（正）
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFFNAME_SHOW1",
                            "size"        => 31,
                            "extrahtml"   => "STYLE=\"WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly",
                            "value"       => $Row_Staff["STAFFNAME_SHOW1"] ));
        $arg["data"]["STAFFNAME_SHOW1"] = $objForm->ge("STAFFNAME_SHOW1");
        //科目担任（副）
        $objForm->ae( array("type"        => "text",
                            "name"        => "STAFFNAME_SHOW",
                            "size"        => 31,
                            "extrahtml"   => "STYLE=\"WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly",
                            "value"       => $Row_Staff["STAFFNAME_SHOW"] ));
        $arg["data"]["STAFFNAME_SHOW"] = $objForm->ge("STAFFNAME_SHOW");

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STAFFCD",
                            "value"     => $Row_Staff["STAFFCD"]
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CHARGEDIV",
                            "value"     => $Row_Staff["CHARGEDIV"]
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STF_CHARGE",
                            "value"     => $Row_Staff["STF_CHARGE"]
                            ) );

        //受講クラス
        $objForm->ae( array("type"        => "text",
                            "name"        => "HR_NAMEABBV",
                            "size"        => 31,
                            "extrahtml"   => "STYLE=\"WIDTH:100%;background-color:darkgray\" WIDTH=\"100%\" readonly",
                            "value"       => $Row_GradeClass["HR_NAMEABBV"] ));
        $arg["data"]["HR_NAMEABBV"] = $objForm->ge("HR_NAMEABBV");

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GRADE_CLASS",
                            "value"     => $Row_GradeClass["GRADE_CLASS"]
                            ) );

        //群コード
        $objForm->ae( array("type"        => "select",
                            "name"        => "GROUPCD",
                            "size"        => 1,
                            "extrahtml"   => "onChange=\"btn_submit('group')\"",
                            "value"       => $Row["GROUPCD"],
                            "options"     => knjb0030oQuery::getGroup(substr($model->term,0,4)) ));
        $arg["data"]["GROUPCD"] = $objForm->ge("GROUPCD");

        //集計フラグ---05/01/19Add 1:集計する 0:集計しない
		$checked_flg = ($Row["COUNTFLG"] == "1") ? "checked" : "";
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "COUNTFLG",
                            "extrahtml"   => $checked_flg,
                            "value"       => "1"));
        $arg["data"]["COUNTFLG"] = $objForm->ge("COUNTFLG");

        //受講クラス選択ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_subform1",
                            "value"     => "受講クラス選択",
                            "extrahtml" => "onclick=\"return btn_submit('subform1');\" style=\"width:110px\"".$dis_subform1 ) );

        $arg["button"]["btn_subform1"] = $objForm->ge("btn_subform1");

        //科目担任選択ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_subform2",
                            "value"     => "科目担任選択",
                            "extrahtml" => "onclick=\"return btn_submit('subform2');\" style=\"width:110px\"" ) );

        $arg["button"]["btn_subform2"] = $objForm->ge("btn_subform2");

        //使用施設選択ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_subform3",
                            "value"     => "使用施設選択",
                            "extrahtml" => "onclick=\"return btn_submit('subform3');\" style=\"width:110px\"" ) );

        $arg["button"]["btn_subform3"] = $objForm->ge("btn_subform3");

        //教科書選択ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_subform4",
                            "value"     => "教科書選択",
                            "extrahtml" => "onclick=\"return btn_submit('subform4');\" style=\"width:110px\"" ) );

        $arg["button"]["btn_subform4"] = $objForm->ge("btn_subform4");

        //名簿入力画面へ選択ボタンの使用可・不可 2006/05/08
        //次年度の場合も有効とする
        if (CTRL_YEAR < substr($model->term,0,4)) {
            $dis_jump = "";
        } else if (substr($model->term,0,4) != CTRL_YEAR || substr($model->term,5) != CTRL_SEMESTER) {
            $dis_jump = "disabled";
        } else {
            $dis_jump = "";
        }
        //リンク先作成・・・プログラムＩＤ変更のため修正
        //$jumping = REQUESTROOT."/B/KNJB050/knjb050index.php";
        //$jumping = REQUESTROOT."/B/KNJB033/knjb033index.php";
        $jumping = REQUESTROOT."/B/KNJB0050O/knjb0050oindex.php";
        //リンクボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_jump",
                            "value"       => "名簿入力画面へ",
                            "extrahtml"   => "style=\"width:120px\" onclick=\" Page_jumper('".$jumping."',
																	  '".substr($model->term,0,4)."',
																	  '".substr($model->term,5)."',
																	  '".$model->chaircd."',
																	  '".$Row["GROUPCD"]."',
																	  '".STAFFCD."');\"".$dis_jump ) );//2006/05/08
        $arg["btn_jump"] = $objForm->ge("btn_jump");

        //追加ボタン---2004.04.22
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_insert",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('insert');\"" ) );
        $arg["button"]["btn_insert"] = $objForm->ge("btn_insert");

        //修正ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"           => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset')\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
                        
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd != "group" && VARS::get("cmd") != "edit" && !isset($model->warning)){
            $arg["reload"]  = "window.open('knjb0030oindex.php?cmd=list&ed=1','left_frame');";
        }
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjb0030oForm2.html", $arg); 
    }
}
?>
