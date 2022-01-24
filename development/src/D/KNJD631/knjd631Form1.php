<?php

require_once('for_php7.php');

class knjd631Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd631Form1", "POST", "knjd631index.php", "", "knjd631Form1");

        //年度テキストボックスを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => CTRL_YEAR,
                            ) );
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボの設定
        $db = Query::dbCheckOut();
        $opt_semester = array();
        $query = knjd631Query::getSemester(CTRL_YEAR);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_semester[] = array('label' => $row["SEMESTERNAME"],
                                    'value' => $row["SEMESTER"]);
        }
        $result->free();
        Query::dbCheckIn($db);
        if ($model->field["SEMESTER"] == "") $model->field["SEMESTER"] = $model->control["学期"];
        $objForm->ae( array("type"       => "select",
                            "name"       => "SEMESTER",
                            "size"       => "1",
                            "value"      => $model->field["SEMESTER"],
                            "extrahtml"  => "onchange=\"return btn_submit('gakki'),AllClearList();\"",
                            "options"    => $opt_semester ) );
        $arg["data"]["SEMESTER"] = $objForm->ge("SEMESTER");

        //クラス選択コンボボックスを作成する
        $arr_trcd = $arr_grad = array();
        $db = Query::dbCheckOut();
        $query = knjd631Query::getAuth(CTRL_YEAR, $model->field["SEMESTER"], STAFFCD);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
            $arr_trcd[$row["VALUE"]] = $row["TR_CD1"];
            $arr_grad[$row["VALUE"]] = sprintf("%d", $row["GRADE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if(!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjd631'),AllClearList();\"",
                            "options"    => isset($row1)?$row1:array()));
        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //異動対象日付
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm ,"DATE" ,$model->field["DATE"]);

        //印刷面コンボボックス
        $arr_side = array();
        $grade = $arr_grad[$model->field["GRADE_HR_CLASS"]];
        if ($grade < 4) $arr_side[] = array('label' => "1:通知票両面＋保護者所見欄両面　",  'value' => "1");
                        $arr_side[] = array('label' => "2:通知票両面",                      'value' => "2");
        if ($grade < 4) $arr_side[] = array('label' => "3:保護者所見欄両面",                'value' => "3");
                        $arr_side[] = array('label' => "4:通知票表（氏名等）",              'value' => "4");
                        $arr_side[] = array('label' => "5:通知票裏（学習記録等）",          'value' => "5");
        if ($grade < 4) $arr_side[] = array('label' => "6:保護者所見欄表（氏名等）",        'value' => "6");
        if ($grade < 4) $arr_side[] = array('label' => "7:保護者所見欄裏（学習記録等）",    'value' => "7");
        $objForm->ae( array("type"       => "select",
                            "name"       => "PRINT_SIDE",
                            "size"       => "1",
                            "value"      => $model->field["PRINT_SIDE"],
                            "extrahtml"  => "",
                            "options"    => $arr_side));
        $arg["data"]["PRINT_SIDE"] = $objForm->ge("PRINT_SIDE");

        //対象外の生徒取得
        $db = Query::dbCheckOut();
        $query = knjd631Query::getSchnoIdou(CTRL_YEAR, $model->field["SEMESTER"], $model->field["GRADE_HR_CLASS"], str_replace("/","-",$model->field["DATE"]));
        $result = $db->query($query);
        $opt_idou = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();
        Query::dbCheckIn($db);

        //対象者リストを作成する
        $db = Query::dbCheckOut();
        $query = knjd631Query::getSchno(CTRL_YEAR, $model->field["SEMESTER"], $model->field["GRADE_HR_CLASS"]);
        $result = $db->query($query);
        $opt1 = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (in_array($row["SCHREGNO"],$opt_idou)) {
                $opt1[] = array('label' => $row["SCHREGNO"]."●".$row["ATTENDNO"]."番●".$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]);
            } else {
                $opt1[] = array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]);
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => isset($opt1)?$opt1:array()));
        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => array()));
        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

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

        //hiddenを作成する(必須)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DOCUMENTROOT",
                            "value"      => DOCUMENTROOT
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD631"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TR_CD1",
                            "value"      => $arr_trcd[$model->field["GRADE_HR_CLASS"]]
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "IMAGEPATH",
                            "value"      => $model->control["LargePhotoPath"]
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "useCurriculumcd",
                            "value"      => $model->Properties["useCurriculumcd"]
                            ) );
        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd631Form1.html", $arg); 
    }
}
?>
