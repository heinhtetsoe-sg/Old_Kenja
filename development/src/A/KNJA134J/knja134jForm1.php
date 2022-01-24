<?php

require_once('for_php7.php');

class knja134jForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        /* Add by PP for Title 2019-01-10 start */
        $arg["TITLE"]   = "中学部指導要録印刷画面";
         /* Add by PP for Title 2019-01-17 end */

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja134jForm1", "POST", "knja134jindex.php", "", "knja134jForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = $model->control["学期名"][CTRL_SEMESTER];

        $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
        if ($model->Properties["seitoSidoYorokuUseHrClassType"] == '1' || $model->Properties["seitoSidoYorokuUseHrClassTypeJ"] == '1') {
            $arg["useHrClassType2"] = "1";
            $opt = array(1, 2);
            // Add by PP for current cursor 2020-01-10 start
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"current_cursor('HR_CLASS_TYPE1');return btn_submit('clickchange');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"current_cursor('HR_CLASS_TYPE2');return btn_submit('clickchange');\"");
            // Add by PP for current cursor 2020-01-17 end
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //学年混合チェックボックス
        $extra = $model->field["GAKUNEN_KONGOU"] == "1" ? "checked" : "";
        // Add by PP for current cursor 2020-01-10 start
        $extra .= " onclick=\"current_cursor('GAKUNEN_KONGOU');return btn_submit('clickchange');\" id=\"GAKUNEN_KONGOU\"";
        // Add by PP for current cursor 2020-01-17 end
        $arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");

        //出力選択ラジオボタン
        $radioValue = array(1, 2);
        $disable = 0;
        if (!$model->field["OUTPUT"]) {
            $model->field["OUTPUT"] = 1;
        }
        if ($model->field["OUTPUT"] == 1) {
            $disable = 1;
        }
        /* Add by PP for current cursor 2020-01-10 start */
        $extra = array("id=\"OUTPUT1\" onclick =\" current_cursor('OUTPUT1'); return btn_submit('clickchange');\"", "id=\"OUTPUT2\" onclick =\"current_cursor('OUTPUT2');  return btn_submit('clickchange');\"");
        /* Add by PP for current cursor 2019-01-17 end */
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $radioValue, get_count($radioValue));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //クラス選択コンボボックス
        if ($disable == 1) {
            $arg["student"] = 1;
            $query = knja134jQuery::getHrClass($model);
        } else {
            $arg["hr_class"] = 2;
            $query = knja134jQuery::getGrade($model);
        }

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
            $model->cmd = 'knja134j';
        }

        if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == '1' && $model->field["OUTPUT"] == "2") {
        } elseif ($model->field["HR_CLASS_TYPE"] == "2" && $model->field["OUTPUT"] == "2") {
        } else {
            $arg["setGradeHrclass"] = "1";
            // Add by PP for current cursor 2020-01-10 start
            $extra = "id=\"GRADE_HR_CLASS\" onchange=\"current_cursor('GRADE_HR_CLASS');return btn_submit('knja134j'),AllClearList();\"";
            // Add by PP for current cursor 2020-01-17 end
            $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);
        }

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //出力選択ラジオボタン
        $radioValue = array(1, 2);
        $disable = 0;
        if (!$model->field["CHITEKI"]) {
            $model->field["CHITEKI"] = 1;
        }
        if ($model->field["OUTPUT"] == 1) {
            $disable = 1;
        }
        /* Add by PP for current cursor 2020-01-10 start */
        $extra = array("id=\"CHITEKI1\" onclick =\" current_cursor('CHITEKI1');return btn_submit('clickchange');\"", "id=\"CHITEKI2\" onclick =\" current_cursor('CHITEKI2');return btn_submit('clickchange');\"");
        /* Add by PP for current cursor 2020-01-17 end */
        $radioArray = knjCreateRadio($objForm, "CHITEKI", $model->field["CHITEKI"], $extra, $radioValue, get_count($radioValue));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //帳票種別チェックボックス
        $name = array("SEITO", "SIMEI", "SCHZIP", "SCHOOLZIP", "GAKUSHU", "KOUDO", "NOT_PRINT_GUARANTOR");
        if ($model->Properties["notShowOnlineSelect"] != "1") {
            $arg["showOnline"] = "1";
            $name[] = "ONLINE";
        }
        foreach ($name as $key => $val) {
            if (in_array($val, array("SCHZIP", "SCHOOLZIP", "NOT_PRINT_GUARANTOR", "ONLINE"))) {
                $extra = $model->field[strtolower($val)] == "1" ? "checked" : "";
            } else {
                $extra = ($model->field[strtolower($val)] == "1" || $model->cmd == "") ? "checked" : "";
            }
            if (in_array($val, array("SIMEI", "SCHZIP", "SCHOOLZIP"))) {
                $extra .= ($model->field["seito"] || $model->cmd == "") ? "" : " disabled";
            }
            $extra .= " onclick=\"kubun();\" id=\"$val\"";

            $arg["data"][$val] = knjCreateCheckBox($objForm, strtolower($val), "1", $extra, "");
        }

        $extra = $model->field["PRINT_BLANK_PAGE"] == '1' ? " checked " : "";
        $extra .= "id=\"PRINT_BLANK_PAGE\"";
        $arg["data"]["PRINT_BLANK_PAGE"] = knjCreateCheckBox($objForm, "PRINT_BLANK_PAGE", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        if (!isset($model->warning) && $model->cmd == 'print') {
            $model->cmd = 'knja134j';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja134jForm1.html", $arg);
    }
}
//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $disable = 0;
    if ($model->field["OUTPUT"] == 1) {
        $disable = 1;
    }

    $opt1 = array();
    $selectdata = ($model->select_data) ? explode(',', $model->select_data) : "";
    if ($model->field["OUTPUT"] == 1) {
        $query = knja134jQuery::getStudentList($model, 'list', $selectdata);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
    } else {
        $query = knja134jQuery::getHrClassAuth($model, 'list', $selectdata);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == '') {
                if (substr($row["VALUE"], 0, 2) != $model->field["GRADE_HR_CLASS"]) {
                    continue;
                }
            }
            $opt1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
    }
    $extra = "multiple style=\"width:230px; height:230px\" ondblclick=\"move1('left',$disable)\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);

    $opt1 = array();
    if ($selectdata) {
        if ($model->field["OUTPUT"] == 1) {
            $query = knja134jQuery::getStudentList($model, 'select', $selectdata);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt1[] = array('label' => $row["LABEL" ],
                                    'value' => $row["VALUE"]);
            }
            $result->free();
        } else {
            $query = knja134jQuery::getHrClassAuth($model, 'select', $selectdata);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->field["HR_CLASS_TYPE"] == "1" && $model->field["GAKUNEN_KONGOU"] == '') {
                    if (substr($row["VALUE"], 0, 2) != $model->field["GRADE_HR_CLASS"]) {
                        continue;
                    }
                }
                    $opt1[] = array('label' => $row["LABEL" ],
                                    'value' => $row["VALUE"]);
            }
            $result->free();
        }
    }
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $extra = "multiple style=\"width:230px; height:230px\" ondblclick=\"move1('right',$disable)\" aria-label='出力対象一覧'";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt1, $extra, 20);

    //対象選択ボタンを作成する（全部）
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = ($model->field["OUTPUT"] == 1) ? "aria-label='全てを出力対象者一覧から生徒一覧へ移動'": "aria-label='全てを出力対象者一覧からクラス一覧へ移動'";
    $extra = "id=\"btn_rights$disable\" style=\"height:20px;width:40px\" onclick=\"moves('right',$disable);\" $label";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = ($model->field["OUTPUT"] == 1) ? "aria-label='全てを生徒一覧から出力対象者一覧へ移動'": "aria-label='全てをクラス一覧から出力対象者一覧へ移動'";
    $extra = "id=\"btn_lefts$disable\" style=\"height:20px;width:40px\" onclick=\"moves('left',$disable);\" $label";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = ($model->field["OUTPUT"] == 1) ? "aria-label='クリックしたリストを出力対象者一覧から生徒一覧へ移動'": "aria-label='クリックしたリストを出力対象者一覧からクラス一覧へ移動'";
    $extra = "id=\"btn_right1$disable\" style=\"height:20px;width:40px\" onclick=\"move1('right',$disable);\" $label";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = ($model->field["OUTPUT"] == 1) ? "aria-label='クリックしたリストを生徒一覧から出力対象者一覧へ移動'": "aria-label='クリックしたリストをクラス一覧から出力対象者一覧へ移動'";
    $extra = "id=\"btn_left1$disable\" style=\"height:20px;width:40px\" onclick=\"move1('left', $disable);\" $label";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    // Add by PP for current cursor 2020-01-10 start
    $extra = "id =\"PRINT\" onclick=\"current_cursor('PRINT'); return btn_submit('update');\"";
    // Add by PP for current cursor 2020-01-17 start
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタンを作成する
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $extra = "onclick=\"closeWin();\" aria-label='終了'";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    $useSchregRegdHdat = ($model->Properties["useSchregRegdHdat"] == '1') ? '1' : '0';

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA134J");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "INEI", $model->inei);
    knjCreateHidden($objForm, "useSchregRegdHdat", $useSchregRegdHdat);
    knjCreateHidden($objForm, "seitoSidoYorokuCyugakuKirikaeNendo", $model->Properties["seitoSidoYorokuCyugakuKirikaeNendo"]);
    knjCreateHidden($objForm, "seitoSidoYorokuCyugakuKirikaeNendoForRegdYear", $model->Properties["seitoSidoYorokuCyugakuKirikaeNendoForRegdYear"]);
    knjCreateHidden($objForm, "seitoSidoYorokuCyugakuKantenNoBlank", $model->Properties["seitoSidoYorokuCyugakuKantenNoBlank"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "color_print", "1");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J", $model->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_TOTALREMARK_SIZE_J", $model->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J"]);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "notPrintFinschooltypeName", $model->Properties["notPrintFinschooltypeName"]);
    knjCreateHidden($objForm, "seitoSidoYorokuUseHrClassType", $model->Properties["seitoSidoYorokuUseHrClassType"]);
    knjCreateHidden($objForm, "seitoSidoYorokuUseHrClassTypeJ", $model->Properties["seitoSidoYorokuUseHrClassTypeJ"]);

    knjCreateHidden($objForm, "HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J", $model->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_disability", $model->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J_disability"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J", $model->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_disability", $model->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J_disability"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J", $model->Properties["HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_VIEWREMARK_SIZE_J", $model->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_disability", $model->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_J_disability"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J", $model->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_J_disability", $model->Properties["HTRAINREMARK_DAT_BEHAVEREC_REMARK_SIZE_J_disability"]);
    knjCreateHidden($objForm, "HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J", $model->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J"]);
    knjCreateHidden($objForm, "HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J_disability", $model->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J_disability"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_disability", $model->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_J_disability"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_disability", $model->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J_disability"]);
}
