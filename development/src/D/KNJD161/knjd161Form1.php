<?php

require_once('for_php7.php');

class knjd161Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd161Form1", "POST", "knjd161index.php", "", "knjd161Form1");

        $opt = array();
        $arg["data"]["YEAR"] = $model->control["年度"];


        //学期コンボボックスを作成する
        if (is_numeric($model->control["学期数"])){
            //年度,学期コンボの設定
            for ( $i = 0; $i < (int) $model->control["学期数"]; $i++ ){
                $opt[]= array("label" => $model->control["学期名"][$i+1], 
                              "value" => sprintf("%d", $i+1)
                             );            
            }
            $seme = isset($model->field["SEMESTER"])?$model->field["SEMESTER"]:$model->control["学期"];    
        }
        $opt[]= array("label" => $model->control["学期名"][9], 
                       "value" => sprintf("%d", 9)
                      );

        $model->field["SEMESTER"] = ($model->field["SEMESTER"]) ? $model->field["SEMESTER"] : $model->control["学期"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "SEMESTER",
                            "size"       => "1",
                            "value"      => $model->field["SEMESTER"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjd161'),AllClearList();\"",     
                            "options"    => isset($opt)?$opt:array()));

        $arg["data"]["SEMESTER"] = $objForm->ge("SEMESTER");

        //学年末の場合、$semeを今学期にする。
        if( isset($seme) ){
            if( $seme == 9 ){
                $seme = $model->control["学期"];
            }
        }


        //テスト名コンボボックスを作成する
        $db = Query::dbCheckOut();
        $opt_kind = array();
        if ($model->field["SEMESTER"] == "9" && $model->testTable == "TESTITEM_MST_COUNTFLG") {
            $opt_kind[] = array('label' => '0000  評価成績', 'value' => '0');
        } else {
            $query = knjd161Query::getTest($model);
            $result = $db->query($query);
            $test_flg = true;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_kind[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
                if( $model->field["TEST"] == $row["VALUE"] ) $test_flg = false;
            }
            if ($model->testTable == "TESTITEM_MST_COUNTFLG") {
                $opt_kind[] = array('label' => '0000  評価成績', 'value' => '0');
                $test_flg = false;
            }

            if($test_flg) $model->field["TEST"] = $opt_kind[0]["value"];
            $result->free();
            Query::dbCheckIn($db);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "TEST",
                            "size"       => "1",
                            "value"      => $model->field["TEST"],
                            "options"    => $opt_kind));

        $arg["data"]["TEST"] = $objForm->ge("TEST");


        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjd161Query::getAuth($model, CTRL_YEAR,$seme);
        $result = $db->query($query);

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
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
                            "extrahtml"  => "onchange=\"return btn_submit('knjd161'),AllClearList();\"",     
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //クラス選択コンボボックスを作成する
            $db = Query::dbCheckOut();

            $query =  "SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO, ";
            $query .= "SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ";
            $query .= "FROM SCHREG_BASE_MST ";
            $query .= "INNER JOIN SCHREG_REGD_DAT ";
            $query .= "ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
            $query .= "WHERE (((SCHREG_REGD_DAT.YEAR)='" .$model->control["年度"] ."') ";
            $query .= "AND ((SCHREG_REGD_DAT.SEMESTER)='" .$seme ."') ";
            $query .= "AND ((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."'))";
            $query .= "ORDER BY ATTENDNO";      //出席番号でソート

            $result = $db->query($query);

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt1[]= array('label' =>  $row["NAME"],
                                'value' => $row["SCHREGNO"]);
            }

            $result->free();
            Query::dbCheckIn($db);

        //対象者リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move('right')\"",
                            "size"       => "20",
                            "options"    => array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move('left')\"",
                            "size"       => "20",
                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象取消ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('right');\"" ) );

        $arg["button"]["btn_right"] = $objForm->ge("btn_right");

        //対象選択ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move('left');\"" ) );

        $arg["button"]["btn_left"] = $objForm->ge("btn_left");

        /* NO004↓ */
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm ,"DATE" ,$model->field["DATE"]);
        /* NO004↑ */

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


        //hiddenを作成する
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD161");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        knjCreateHidden($objForm, "COUNTFLG", $model->testTable);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd161Form1.html", $arg); 
    }
}
?>
