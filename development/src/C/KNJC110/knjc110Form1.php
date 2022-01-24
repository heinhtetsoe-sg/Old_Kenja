<?php

require_once('for_php7.php');

class knjc110Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期名
        $gakkiname = $model->control["学期名"][CTRL_SEMESTER];
        $arg["data"]["GAKKINAME"] = $gakkiname;

        //欠課数換算(ABSENT_COV)
        $query = knjc110Query::getAbsent();
        $absent = $db->getOne($query);

        /**************/
        /* カレンダー */
        /**************/
        //カレンダーコントロール１
        $value = isset($model->field["DATE1"]) ? $model->field["DATE1"] : $model->control["学期開始日付"][9];
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm,"DATE1",$value);
        //カレンダーコントロール２
        $value2 = isset($model->field["DATE2"]) ? $model->field["DATE2"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm,"DATE2",$value2);

        //開始日チェックボックス
        $extra = " id=\"SET_SEM_SDATE\" onClick=\"setSemSdate(this)\"";
        $arg["data"]["SET_SEM_SDATE"] = knjCreateCheckBox($objForm, "SET_SEM_SDATE", "VALUE", $extra);
        //学期開始日付
        knjCreateHidden($objForm, "SEM_SDATE", $model->control["学期開始日付"][CTRL_SEMESTER]);
        //年度開始日付
        knjCreateHidden($objForm, "NEN_SDATE", $model->control["学期開始日付"][9]);
        //年度終了日付
        knjCreateHidden($objForm, "NEN_EDATE", $model->control["学期終了日付"][9]);

        //出力内容種類
        $opt = array(1, 2);
        $model->field["OUTPUTNAME"] = ($model->field["OUTPUTNAME"] == "") ? "1" : $model->field["OUTPUTNAME"];
        $extra = array("id=\"OUTPUTNAME1\"", "id=\"OUTPUTNAME2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUTNAME", $model->field["OUTPUTNAME"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力内容時数
        $opt = array(1, 2);
        $model->field["OUTPUTJISUU"] = ($model->field["OUTPUTJISUU"] == "") ? "2" : $model->field["OUTPUTJISUU"];
        $extra = array("id=\"OUTPUTJISUU1\"", "id=\"OUTPUTJISUU2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUTJISUU", $model->field["OUTPUTJISUU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //クラス選択コンボボックスを作成する
        if ($absent == 1 || $absent == 3) {
            $query = knjc110Query::getAuth(CTRL_YEAR, CTRL_SEMESTER);
        } else {
            $query = knjc110Query::getAuth2(CTRL_YEAR);
        }

        $result = $db->query($query);
        $flag = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"]==$row["VALUE"]) {$flag = 1;}
        }
        $result->free();

        if($model->field["GRADE_HR_CLASS"]=="" || $flag==0) $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

        $objForm->ae( array("type"      => "select",
                            "name"      => "GRADE_HR_CLASS",
                            "size"      => "1",
                            "value"     => $model->field["GRADE_HR_CLASS"],
                            "extrahtml" => "onChange=\"AllClearList()\"",
                            "options"   => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");


        //対象者リストを作成する
        $query = knjc110Query::getCategoryNames($model);

        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt1[]= array('label' =>  $row["LABEL"],
                           'value' =>  $row["VALUE"]);
        }
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:240px\" width:\"240px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:240px\" width:\"240px\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


        /********************/
        /* ボタンを作成する */
        /********************/
        //読込ボタンを作成する
        $arg["button"]["btn_read"]   = knjCreateBtn($objForm, "btn_read", "読　込", "onclick=\"return btn_submit('knjc110');\"");
        //対象選択ボタンを作成する(全部)
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", "style=\"height:20px;width:40px\" onclick=\"moves('right');\"");
        //対象取消ボタンを作成する(全部)
        $arg["button"]["btn_lefts"]  = knjCreateBtn($objForm, "btn_lefts" , "<<", "style=\"height:20px;width:40px\" onclick=\"moves('left');\"");
        //対象選択ボタンを作成する(一部)
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", "style=\"height:20px;width:40px\" onclick=\"move1('right');\"");
        //対象取消ボタンを作成する(一部)
        $arg["button"]["btn_left1"]  = knjCreateBtn($objForm, "btn_left1" , "＜", "style=\"height:20px;width:40px\" onclick=\"move1('left');\"");
        //印刷ボタンを作成する
        $arg["button"]["btn_print"]  = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
        //終了ボタンを作成する
        $arg["button"]["btn_end"]    = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        /********************/
        /* hiddenを作成する */
        /********************/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME"     , DB_DATABASE);
        knjCreateHidden($objForm, "PRGID"      , "KNJC110");
        knjCreateHidden($objForm, "YEAR"       , CTRL_YEAR);       //年度データ
        knjCreateHidden($objForm, "SEMESTER"   , CTRL_SEMESTER);   //学期データ
        knjCreateHidden($objForm, "ABSENT_COV" , $absent);         //欠課数換算
        knjCreateHidden($objForm, "useTestCountflg" , $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "chikokuHyoujiFlg" , $model->Properties["chikokuHyoujiFlg"]);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
        knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjc110Form1", "POST", "knjc110index.php", "", "knjc110Form1");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc110Form1.html", $arg); 
    }
}
?>
