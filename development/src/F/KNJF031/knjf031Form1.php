<?php

require_once('for_php7.php');


class knjf031Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjf031Form1", "POST", "knjf031index.php", "", "knjf031Form1");


        //1:クラス,2:個人表示指定---2005.06.30
        $opt[0]=1;
        $opt[1]=2;

        if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;

        $objForm->ae( array("type"       => "radio",
        	                "name"       => "OUTPUT",
        					"value"      => $model->field["OUTPUT"],
        					"extrahtml"	 => "onclick =\" return btn_submit('knjf031');\"",
        					"multiple"   => $opt));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

        if ($model->field["OUTPUT"] == 1) $arg["clsno"] = $model->field["OUTPUT"];
        if ($model->field["OUTPUT"] == 2) $arg["schno"] = $model->field["OUTPUT"];

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => CTRL_YEAR,
                            ) );

        //学期名
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //現在の学期コードをhiddenで送る
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"      => CTRL_SEMESTER,
                            ) );

        //クラス一覧リスト
        $db = Query::dbCheckOut();
        $row1 = array();
        $query = common::getHrClassAuth(CTRL_YEAR,CTRL_SEMESTER,AUTHORITY,STAFFCD);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        //2:個人表示指定用
    	$opt_left = array();
        if ($model->field["OUTPUT"] == 2) {
            if ($model->field["GRADE_HR_CLASS"]=="") $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

            $objForm->ae( array("type"       => "select",
                                "name"       => "GRADE_HR_CLASS",
                                "size"       => "1",
                                "value"      => $model->field["GRADE_HR_CLASS"],
            					"extrahtml"  => "onChange=\"return btn_submit('change_class');\"",
                                "options"    => $row1));

            $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

            $row1 = array();
    		//生徒単位
            $selectleft = explode(",", $model->selectleft);
            $query = knjf031Query::getSchno($model);//生徒一覧取得
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"], 
                                                             "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

    			if($model->cmd == 'change_class' ) {
        	        if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)){
    				    $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
    		    		                'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
    				}
    			} else {
    			    $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
    	    		                'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
    			}
            }
            //左リストで選択されたものを再セット
    		if($model->cmd == 'change_class' ) {
    	        foreach ($model->select_opt as $key => $val){
    	            if (in_array($key, $selectleft)) {
        	            $opt_left[] = $val;
            	    }
    	        }
    	    }
        }

        $result->free();
        Query::dbCheckIn($db);

        $chdt = $model->field["OUTPUT"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_NAME",
        					"extrahtml"  => "multiple style=\"width=220px\" width=\"220px\" ondblclick=\"move1('left',$chdt)\"",
                            "size"       => "18",
                            "options"    => $row1));

        $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

        //出力対象クラスリスト
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASS_SELECTED",
        					"extrahtml"  => "multiple style=\"width=220px\" width=\"220px\" ondblclick=\"move1('right',$chdt)\"",
                            "size"       => "18",
                            "options"    => $opt_left));

        $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right',$chdt);\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left',$chdt);\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right',$chdt);\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left',$chdt);\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //歯科検診結果のお知らせ
       	$check_1 = ($model->field["CHECK1"] == "on") ? "checked" : "" ;

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "CHECK1",
        					"value"		=> "on",
        					"extrahtml"	=> $check_1 ) );

        $arg["data"]["CHECK1"] = $objForm->ge("CHECK1");

        //視力検査結果のお知らせ
       	$check_2 = ($model->field["CHECK2"] == "on") ? "checked" : "" ;

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "CHECK2",
        					"value"		=> "on",
        					"extrahtml"	=> $check_2 ) );

        $arg["data"]["CHECK2"] = $objForm->ge("CHECK2");

        //印刷選択(1:結果印刷,2:フォーム印刷（年組番氏名のみ印刷）)---2005.06.30
        $opt_out[0]=1;
        $opt_out[1]=2;

        if (!$model->field["OPT_OUT"]) $model->field["OPT_OUT"] = 1;

        $objForm->ae( array("type"       => "radio",
        	                "name"       => "OPT_OUT",
        					"value"      => $model->field["OPT_OUT"],
        					"extrahtml"	 => "",
        					"multiple"   => $opt_out));

        $arg["data"]["OPT_OUT1"] = $objForm->ge("OPT_OUT",1);
        $arg["data"]["OPT_OUT2"] = $objForm->ge("OPT_OUT",2);

        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
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
                            "name"      => "PRGID",
                            "value"      => PROGRAMID
                            ) );

        //中学か高校かを判断
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHOOL_JUDGE",
                            "value"     => knjf031Query::getSchoolJudge() ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //左のリストを保持
    	$objForm->ae( array("type"      => "hidden",
    	                    "name"      => "selectleft") );  

        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf031Form1.html", $arg); 

    }

}
?>
