<?php

require_once('for_php7.php');


class knje391Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;
        /* Add by PP for Title 2019-01-10 start */
        $arg["TITLE"]   = "教育支援計画とアセスメント印刷画面";
         /* Add by PP for Title 2019-01-17 end */

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje391Form1", "POST", "knje391index.php", "", "knje391Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期テキストボックスを作成する
        $arg["data"]["GAKKI"] = $model->control["学期名"][$model->control["学期"]];
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        if ($model->Properties["useFi_Hrclass"] == "1" || $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $arg["HR_CLASS_TYPE_SELECT"] = 1;
            //クラス方式選択    1:法定クラス 2:複式クラス/実クラス
            $opt = array(1, 2);
            $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
            // Add by PP for current cursor 2020-01-10 start
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"current_cursor('HR_CLASS_TYPE1'); return btn_submit('clickchange');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"current_cursor('HR_CLASS_TYPE2'); return btn_submit('clickchange');\"");
            // Add by PP for current cursor 2020-01-17 end
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            if ($model->Properties["useFi_Hrclass"] == "1") {
                $arg["data"]["HR_CLASS_TYPE2_LABEL"] = "複式クラス";
            } else if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
                $arg["data"]["HR_CLASS_TYPE2_LABEL"] = "実クラス";

                //学年混合チェックボックス
                //$extra  = ($model->field["GAKUNEN_KONGOU"] == "1") ? "checked"   : "";
                //$extra .= ($model->field["HR_CLASS_TYPE"]  != "1") ? " disabled" : "";
                //$extra .= " onclick=\"return btn_submit('knje391');\" id=\"GAKUNEN_KONGOU\"";
                //$arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");
            }


            //クラス出力    1:法定クラス 2:複式クラス/実クラス
            $opt = array(1, 2);
            $model->field["PRINT_HR_CLASS_TYPE"] = ($model->field["PRINT_HR_CLASS_TYPE"] == "") ? "2" : $model->field["PRINT_HR_CLASS_TYPE"];
            // Add by PP for current cursor 2020-01-10 start
            $extra = array("id=\"PRINT_HR_CLASS_TYPE1\" onclick=\"\"", "id=\"PRINT_HR_CLASS_TYPE2\" onclick=\"\"");
            // Add by PP for current cursor 2020-01-17 end
            $radioArray = knjCreateRadio($objForm, "PRINT_HR_CLASS_TYPE", $model->field["PRINT_HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        }

        $query = knje391Query::getAuth($model);
        $row1 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        if ($model->cmd == 'clickchange' ) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            $model->cmd = 'knje391';
        }
        // Add by PP for current cursor 2020-01-10 start
        $extra = "id=\"GRADE_HR_CLASS\" onchange=\"current_cursor('GRADE_HR_CLASS'); return btn_submit('knje391'),AllClearList();\"";
        // Add by PP for current cursor 2020-01-17 end
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //対象者リストを作成する
        makeStudentList($objForm, $arg, $db, $model);

        //帳票種別チェックボックス
        $name = array("PRINT_A", "PRINT_B", "PRINT_C", "PRINT_D", "PRINT_E", "PRINT_F", "PRINT_G", "PRINT_H");
        foreach ($name as $key => $val) {
            $extra = ($model->field[$val] == "1") ? "checked" : "";
            $extra .= " onclick=\"kubun();\" id=\"$val\"";

            $arg["data"][$val] = knjCreateCheckBox($objForm, $val, "1", $extra, "");
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje391Form1.html", $arg); 
    }
}
//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model) {
    $query = knje391Query::getStudentSql($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[]= array('label' =>  $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    $disable = 1;
    $extra = "multiple style=\"width:230px\" width:\"230px\" id=\"category_name\" ondblclick=\"move1('left',$disable)\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 20);

    //生徒一覧リストを作成する
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right',$disable)\" id=\"category_selected\" aria-label='出力対象一覧'";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", array(), $extra, 20);

    //対象選択ボタンを作成する（全部）
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = "aria-label='全てを出力対象者一覧から生徒一覧へ移動'";
    $extra = "id=\"btn_rights\" style=\"height:20px;width:40px\" onclick=\"moves('right',$disable);\" $label";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = "aria-label='全てを生徒一覧から出力対象者一覧へ移動'";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $extra = "id=\"btn_lefts\" style=\"height:20px;width:40px\" onclick=\"moves('left',$disable);\" $label";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = "aria-label='クリックしたリストを出力対象者一覧から生徒一覧へ移動'";
    $extra = "id=\"btn_right1\" style=\"height:20px;width:40px\" onclick=\"move1('right',$disable);\" $label";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = "aria-label='クリックしたリストを生徒一覧から出力対象者一覧へ移動'";
    $extra = "id=\"btn_left1\" style=\"height:20px;width:40px\" onclick=\"move1('left', $disable);\" $label";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //印刷ボタンを作成する
    // Add by PP for current cursor 2020-01-10 start
    $extra = "id=\"PRINT\" onclick=\"current_cursor('PRINT');return newwin('" . SERVLET_URL . "');\"";
    // Add by PP for current cursor 2020-01-17 start
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタンを作成する
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $extra = "onclick=\"closeWin();\" aria-label='終了'";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    if ($model->Properties["useSchregRegdHdat"] == '1') {
        $useSchregRegdHdat = '1';
    } else {
        $useSchregRegdHdat = '0';
    }

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE391");
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useCurriculumcd" , $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "TokushiShienPlanPatern" , $model->Properties["TokushiShienPlanPatern"]);
    knjCreateHidden($objForm, "useFormNameE390_A_1", $model->Properties["useFormNameE390_A_1"]);
    knjCreateHidden($objForm, "useFormNameE390_A_2", $model->Properties["useFormNameE390_A_2"]);
    knjCreateHidden($objForm, "useFormNameE390_B_1", $model->Properties["useFormNameE390_B_1"]);
    knjCreateHidden($objForm, "useFormNameE390_D_1", $model->Properties["useFormNameE390_D_1"]);
}

?>
