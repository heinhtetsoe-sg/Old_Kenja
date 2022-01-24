<?php

require_once('for_php7.php');

class knja133pForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja133pForm1", "POST", "knja133pindex.php", "", "knja133pForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = $model->control["学期名"][CTRL_SEMESTER];

        //出力選択ラジオボタン
        $radioValue = array(1, 2);
        $disable = 0;
        if (!$model->field["OUTPUT"]) {
            $model->field["OUTPUT"] = 1;
        }
        if ($model->field["OUTPUT"] == 1) {
            $disable = 1;
        }
        $extra = array("id=\"OUTPUT1\" onclick =\" return btn_submit('clickchange');\"", "id=\"OUTPUT2\" onclick =\" return btn_submit('clickchange');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $radioValue, get_count($radioValue));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        if ($disable == 1) {
            $arg["student"] = 1;
        } else {
            $arg["hr_class"] = 2;
        }

        //クラス選択コンボボックス
        if ($disable == 1) {
            $query = knja133pQuery::getHrClass($model);
        } else {
            $query = knja133pQuery::getGrade($model);
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
            $model->cmd = 'knja133p';
        }

        $extra = "onchange=\"return btn_submit('knja133p'),AllClearList();\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model, $disable);

        //帳票種別チェックボックス
        $names = array("SEITO", "SIMEI", "SCHZIP", "SCHOOLZIP", "GAKUSHU", "KOUDO", "MONGON", "INEI_PRINT", "INEI_PRINT2");
        if ($model->Properties["notShowOnlineSelect"] != "1") {
            $arg["showOnline"] = "1";
            $names[] = "ONLINE";
        }
        $defChecks = array("SEITO", "SIMEI", "GAKUSHU", "KOUDO");
        if ($model->Properties["seitoSidoYorokuCheckPrintIneiP"] == "3") {
            $defChecks[] = "INEI_PRINT";
        } elseif ($model->Properties["seitoSidoYorokuCheckPrintIneiP"] == "4") {
            $defChecks[] = "INEI_PRINT2";
        }
        foreach ($names as $name) {
            $fldname = strtolower($name);
            $extra = "";
            if ($model->field[$fldname] == "1" || in_array($name, $defChecks) && $model->cmd == "") {
                $extra = "checked";
            }
            $extra .= " onclick=\"kubun();\" id=\"$name\"";

            $arg["data"][$name] = knjCreateCheckBox($objForm, $fldname, "1", $extra, "");
        }

        $space =  "　　　　　　　　　　　　　&nbsp;";
        $arg["data"]["SEITO_OPT"] = array();
        if ($z010name1 == "miyagiken") {
            knjCreateHidden($objForm, "SIMEI", "1");
        } else {
            $arg["data"]["SEITO_OPT"][] = array("SPACE"=> $space, "FORM" => $arg["data"]["SIMEI"], "ID" => "SIMEI", "TITLE" => "児童・保護者氏名出力");
            $arg["data"]["SEITO_OPT"][] = array("SPACE"=> $space, "FORM" => $arg["data"]["SCHZIP"], "ID" => "SCHZIP", "TITLE" => "現住所の郵便番号出力");
            $arg["data"]["SEITO_OPT"][] = array("SPACE"=> $space, "FORM" => $arg["data"]["SCHOOLZIP"], "ID" => "SCHOOLZIP", "TITLE" => "学校所在地の郵便番号出力");
        }
        if (in_array($model->Properties["seitoSidoYorokuCheckPrintIneiP"], array("2", "4"))) {
            $arg["data"]["SEITO_OPT"][] = array("SPACE"=> $space, "FORM" => $arg["data"]["INEI_PRINT2"], "ID" => "INEI_PRINT2", "TITLE" => "校長・担任印影出力");
        } elseif (in_array($model->Properties["seitoSidoYorokuCheckPrintIneiP"], array("1", "3"))) {
            $arg["data"]["SEITO_OPT"][] = array("SPACE"=> $space, "FORM" => $arg["data"]["INEI_PRINT"], "ID" => "INEI_PRINT", "TITLE" => "担任印影出力");
        }
        $arg["data"]["SEITO_OPT"][0]["SPACE"] = "";

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        if (!isset($model->warning) && $model->cmd == 'print') {
            $model->cmd = 'knja133p';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja133pForm1.html", $arg);
    }
}
//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $disable)
{
    $selectdata = ($model->select_data) ? explode(',', $model->select_data) : "";
    if ($disable == 1) {
        $query = knja133pQuery::getStudentList($model, 'list', $selectdata);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        $result->free();
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left',$disable)\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 20);

        //生徒一覧リストを作成する
        if ($selectdata) {
            $query = knja133pQuery::getStudentList($model, 'select', $selectdata);
            $result = $db->query($query);
            $opt1 = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $opt1[] = array('label' => $row["LABEL" ],
                                    'value' => $row["VALUE"]);
            }
            $result->free();

            $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right',$disable)\"";
            $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt1, $extra, 20);
        } else {
            $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right',$disable)\"";
            $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", array(), $extra, 20);
        }
    } else {
        $query = knja133pQuery::getHrClassAuth($model, 'list', $selectdata);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (substr($row["VALUE"], 0, 2) != $model->field["GRADE_HR_CLASS"]) {
                continue;
            }
            $opt1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        $result->free();
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left',$disable)\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 20);

        if ($selectdata) {
            $query = knja133pQuery::getHrClassAuth($model, 'select', $selectdata);
            $result = $db->query($query);
            $opt1 = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (substr($row["VALUE"], 0, 2) != $model->field["GRADE_HR_CLASS"]) {
                    continue;
                }
                    $opt1[] = array('label' => $row["LABEL" ],
                                    'value' => $row["VALUE"]);
            }
            $result->free();

            $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right',$disable)\"";
            $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt1, $extra, 20);
        } else {
            $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right',$disable)\"";
            $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", array(), $extra, 20);
        }
    }

    //$result->free();

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right',$disable);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left',$disable);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right',$disable);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $disable);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    $useSchregRegdHdat = ($model->Properties["useSchregRegdHdat"] == '1') ? '1' : '0';

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA133P");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "INEI", $model->inei);
    knjCreateHidden($objForm, "useSchregRegdHdat", $useSchregRegdHdat);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "seitoSidoYorokuKinsokuFormP", $model->Properties["seitoSidoYorokuKinsokuFormP"]);
    knjCreateHidden($objForm, "color_print", "1");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P", $model->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]);
    knjCreateHidden($objForm, "use_finSchool_teNyuryoku_P", $model->Properties["use_finSchool_teNyuryoku_P"]);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "notPrintFinschooltypeName", $model->Properties["notPrintFinschooltypeName"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintKantenBlankIfPageOver", $model->Properties["seitoSidoYorokuNotPrintKantenBlankIfPageOver"]);
    knjCreateHidden($objForm, "knja133pUseSlashNameMst", $model->Properties["knja133pUseSlashNameMst"]);
    knjCreateHidden($objForm, "seitoSidoYorokuUseEditKinsokuP", $model->Properties["seitoSidoYorokuUseEditKinsokuP"]);
    knjCreateHidden($objForm, "seitoSidoYorokuUseSvfFieldAreaP", $model->Properties["seitoSidoYorokuUseSvfFieldAreaP"]);
    knjCreateHidden($objForm, "knja133pUseViewSubclassMstSubclasscd2", $model->Properties["knja133pUseViewSubclassMstSubclasscd2"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P", $model->Properties["HTRAINREMARK_DAT_FOREIGNLANGACT4_SIZE_P"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_TOTALREMARK_SIZE_P", $model->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_P"]);
    knjCreateHidden($objForm, "HTRAINREMARK_P_DAT_TOTALREMARK_SIZE_P", $model->Properties["HTRAINREMARK_P_DAT_TOTALREMARK_SIZE_P"]);
    knjCreateHidden($objForm, "HTRAINREMARK_P_DAT_ATTENDREC_REMARK_SIZE_P", $model->Properties["HTRAINREMARK_P_DAT_ATTENDREC_REMARK_SIZE_P"]);
    knjCreateHidden($objForm, "HTRAINREMARK_P_DAT_FOREIGNLANGACT4_SIZE_P", $model->Properties["HTRAINREMARK_P_DAT_FOREIGNLANGACT4_SIZE_P"]);
    knjCreateHidden($objForm, "HTRAINREMARK_P_DAT_TOTALSTUDYACT_SIZE_P", $model->Properties["HTRAINREMARK_P_DAT_TOTALSTUDYACT_SIZE_P"]);
    knjCreateHidden($objForm, "HTRAINREMARK_P_DAT_VIEWREMARK_SIZE_P", $model->Properties["HTRAINREMARK_P_DAT_VIEWREMARK_SIZE_P"]);
    knjCreateHidden($objForm, "HTRAINREMARK_P_DAT_TOTALSTUDYVAL_SIZE_P", $model->Properties["HTRAINREMARK_P_DAT_TOTALSTUDYVAL_SIZE_P"]);
}
