<?php

require_once('for_php7.php');


class knja141Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja141Form1", "POST", "knja141index.php", "", "knja141Form1");


        //フォーム選択・・・1:仮身分証明書(新入生), 2:身分証明書(在籍), 3:仮身分証明書(在籍)
        $model->field["OUTPUT"] = isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "3";//---2005.06.10
        $ext = "onclick=\"return btn_submit('output');\"";
        $opt_output = array(1, 2, 3);
        $extra  = array("id=\"OUTPUT1\" ".$ext, "id=\"OUTPUT2\" ".$ext, "id=\"OUTPUT3\" ".$ext);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //1:個人,2:クラス表示指定---2005.07.15
        if (!$model->field["DISP"]) $model->field["DISP"] = 1;
        $ext = "onclick=\"return btn_submit('knja141');\"";
        $opt = array(1, 2);
        $extra  = array("id=\"DISP1\" ".$ext, "id=\"DISP2\" ".$ext);
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["DISP"] == 1) $arg["schno"] = $model->field["DISP"];
        if ($model->field["DISP"] == 2) $arg["clsno"] = $model->field["DISP"];

        //年度・学期
        if ($model->field["OUTPUT"] == 1) {
            if (CTRL_SEMESTER < $model->control["学期数"]) {
                $ctrl_year      = CTRL_YEAR;
                $ctrl_semester  = CTRL_SEMESTER + 1;
            //最終学期
            } else {
                $ctrl_year      = CTRL_YEAR + 1;
                $ctrl_semester  = 1;
            }
        } else {
            $ctrl_year      = CTRL_YEAR;
            $ctrl_semester  = CTRL_SEMESTER;
        }
        //学期マスタ
        $db = Query::dbCheckOut();
        $query = knja141Query::getSemeMst($ctrl_year,$ctrl_semester);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $Row_Mst["YEAR"] ) );
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];
        //学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => $Row_Mst["SEMESTER"] ) );
        $arg["data"]["GAKKI"] = $Row_Mst["SEMESTERNAME"];

        //有効期限
        if ($model->field["OUTPUT"] == 1 || $model->field["OUTPUT"] == 3) {//---2005.06.10
            //月末日を取得
            if ($Row_Mst["SDATE"] != "") {
                $db = Query::dbCheckOut();
                $query = knja141Query::getAddDays2($Row_Mst["SDATE"], 32 - 1);
                $term_edate = $db->getOne($query); //1年後
                $query = knja141Query::getLastDay($term_edate);
                $term_edate = $db->getOne($query); //月末日(1年後)
                Query::dbCheckIn($db);
            }
            if( $model->cmd=="output" || !isset($model->field["TERM_SDATE"]) ) 
                $model->field["TERM_SDATE"] = str_replace("-","/",$Row_Mst["SDATE"]);
            if( $model->cmd=="output" || !isset($model->field["TERM_EDATE"]) ) 
                $model->field["TERM_EDATE"] = str_replace("-","/",$term_edate);
        }
        if ($model->field["OUTPUT"] == 2) {
            if( $model->cmd=="output" || !isset($model->field["TERM_SDATE"]) ) 
                $model->field["TERM_SDATE"] = $model->control["学期開始日付"][9];
            if( $model->cmd=="output" || !isset($model->field["TERM_EDATE"]) ) {
                $model->field["TERM_EDATE"] = (CTRL_YEAR + 3)."/03/31";
            }
        }
        $arg["data"]["TERM_SDATE"]=View::popUpCalendar($objForm,"TERM_SDATE",$model->field["TERM_SDATE"]);//開始
        $arg["data"]["TERM_EDATE"]=View::popUpCalendar($objForm,"TERM_EDATE",$model->field["TERM_EDATE"]);//終了

        //クラス一覧リスト
        $db = Query::dbCheckOut();
        $row1 = array();
        $class_flg = false;
        $query = common::getHrClassAuth($ctrl_year,$ctrl_semester,AUTHORITY,STAFFCD);//---2005.07.15
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;
        }

        //1:個人表示指定用---2005.07.15
    	$opt_left = array();
        if ($model->field["DISP"] == 1) {
            if ($model->field["GRADE_HR_CLASS"]=="" || !$class_flg) $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

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
            if ($model->field["OUTPUT"] == 1) $query = knja141Query::getSchno1($model,$ctrl_year,$ctrl_semester);
            else                              $query = knja141Query::getSchno2($model,$ctrl_year,$ctrl_semester);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"], 
                                                             "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

    			if($model->cmd == 'change_class' ) {
        	        if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)){
    				    $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"],
    		    		                'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
    				}
    			} else {
    			    $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"],
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

        $chdt = $model->field["DISP"];

/***********************---2005.07.15
        //クラス
        $db = Query::dbCheckOut();
//        $query = knja141Query::getAuth($ctrl_year,$ctrl_semester);
        $query = common::getHrClassAuth($ctrl_year,$ctrl_semester,AUTHORITY,STAFFCD);//---2005.07.15
        $class_flg = false;
        $row1 = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;
        }
        $result->free();
        Query::dbCheckIn($db);

        if (!isset($model->field["GRADE_HR_CLASS"]) || !$class_flg) 
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
        					"extrahtml"  => "onchange=\"return btn_submit('change_class');\"",//---2005.07.15
                            "options"    => $row1));
        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //生徒一覧リスト
        $db = Query::dbCheckOut();
        if ($model->field["OUTPUT"] == 1) $query = knja141Query::getSchno1($model,$ctrl_year,$ctrl_semester);
        else                              $query = knja141Query::getSchno2($model,$ctrl_year,$ctrl_semester);
//---2005.06.10
//        if ($model->field["OUTPUT"] == 2) $query = knja141Query::getSchno2($model,$ctrl_year,$ctrl_semester);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($model->field["OUTPUT"] == 1) 
                $row["NAME"] = $row["SCHREGNO"]."　".$row["ATTENDNO"]."番　".$row["NAME"];//---2005.03.31

            $opt1[]= array('label' => $row["NAME"],
                           'value' => $row["SCHREGNO"]);
        }
        $result->free();
        Query::dbCheckIn($db);
*********************************/

        //生徒一覧リスト
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
        					"extrahtml"  => "multiple style=\"width:250px\" ondblclick=\"move1('left',$chdt)\"",
                            "size"       => "20",
                            "options"    => $row1));//---2005.07.15
//                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");
        //出力対象一覧リスト
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
        					"extrahtml"  => "multiple style=\"width:250px\" ondblclick=\"move1('right',$chdt)\"",
                            "size"       => "20",
                            "options"    => $opt_left));//---2005.07.15
//                            "options"    => array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象取消ボタン（全部）
        $objForm->ae( array("type"  => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right',$chdt);\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象選択ボタン（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left',$chdt);\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象取消ボタン（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right',$chdt);\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象選択ボタン（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left',$chdt);\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJA141" ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DOCUMENTROOT",
                            "value"     => DOCUMENTROOT ) );

        //中学か高校かを判断---2005.06.10Add
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHOOL_JUDGE",
                            "value"     => knja141Query::getSchoolJudge() ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        //左のリストを保持---2005.07.15
    	$objForm->ae( array("type"      => "hidden",
    	                    "name"      => "selectleft") );  

        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja141Form1.html", $arg); 
    }
}
?>
