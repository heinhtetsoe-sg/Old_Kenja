<?php

require_once('for_php7.php');

class knja133kForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja133kForm1", "POST", "knja133kindex.php", "", "knja133kForm1");

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

        //生年月日切替 1:和暦 2:西暦
        $radioValue = array(1, 2);
        if (!$model->field["BIRTHDAY_FORMAT"]) {
            $model->field["BIRTHDAY_FORMAT"] = 1;
        }
        $extra = array("id=\"BIRTHDAY_FORMAT1\" ", "id=\"BIRTHDAY_FORMAT2\" ");
        $radioArray = knjCreateRadio($objForm, "BIRTHDAY_FORMAT", $model->field["BIRTHDAY_FORMAT"], $extra, $radioValue, get_count($radioValue));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        $z010 = $db->getOne(knja133kQuery::getZ010());
        if ($z010 == "naraken") {
            $arg["show_BIRTHDAY_FORMAT"] = "1";
        }

        if ($disable == 1) {
            if ($model->Properties["useSpecial_Support_School"] == '1') {
                $arg["TARGET_NAME"] = "幼児";
            } else {
                $arg["TARGET_NAME"] = "園児";
            }
        } else {
            $arg["TARGET_NAME"] = "クラス";
        }

        if ($model->Properties["useSpecial_Support_School"] == '1') {
            $arg["FORM1_TITLE"] = "学籍の記録";
            $arg["ENJI_TITLE"] = "幼児";
        } else {
            $arg["FORM1_TITLE"] = "幼児指導要録";
            $arg["ENJI_TITLE"] = "園児";
        }

        //クラス選択コンボボックス
        if ($disable == 1) {
            $query = knja133kQuery::getHrClass($model);
        } else {
            $query = knja133kQuery::getGrade($model);
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
            $model->cmd = 'knja133k';
        }

        $extra = "onchange=\"return btn_submit('knja133k'),AllClearList();\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model, $disable);

        //帳票種別チェックボックス
        $name = array("SEITO", "SIMEI", "SCHZIP", "SCHOOLZIP", "SIDOU");
        foreach ($name as $key => $val) {
            if ($val == "SCHZIP" || $val == "SCHOOLZIP") {
                $extra = $model->field[strtolower($val)] == "1" ? "checked" : "";
            } else {
                $extra = ($model->field[strtolower($val)] == "1" || $model->cmd == "") ? "checked" : "";
            }
            if ($val == "SIMEI" || $val == "SCHZIP" || $val == "SCHOOLZIP") {
                $extra .= ($model->field["seito"] || $model->cmd == "") ? "" : " disabled";
            }
            $extra .= " onclick=\"kubun();\" id=\"$val\"";

            $arg["data"][$val] = knjCreateCheckBox($objForm, strtolower($val), "1", $extra, "");
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        if (!isset($model->warning) && $model->cmd == 'print') {
            $model->cmd = 'knja133k';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja133kForm1.html", $arg);
    }
}
//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $disable)
{
    $selectdata = ($model->select_data) ? explode(',', $model->select_data) : "";
    if ($disable == 1) {
        $query = knja133kQuery::getStudentList($model, 'list', $selectdata);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        $result->free();
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left',$disable)\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 20);

        //幼児一覧リストを作成する
        if ($selectdata) {
            $query = knja133kQuery::getStudentList($model, 'select', $selectdata);
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
        $query = knja133kQuery::getHrClassAuth($model, 'list', $selectdata);
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
            $query = knja133kQuery::getHrClassAuth($model, 'select', $selectdata);
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
    knjCreateHidden($objForm, "PRGID", "KNJA133K");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "INEI", $model->inei);
    knjCreateHidden($objForm, "useSchregRegdHdat", $useSchregRegdHdat);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "color_print", "1");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_K", $model->Properties["HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_K"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_TOTALREMARK_SIZE_K", $model->Properties["HTRAINREMARK_DAT_TOTALREMARK_SIZE_K"]);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_K", $model->Properties["HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_K"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_KL_SIZE_E", $model->Properties["HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_K"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_K", $model->Properties["HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_K"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_VIEWREMARK_SIZE_K", $model->Properties["HTRAINREMARK_DAT_VIEWREMARK_SIZE_K"]);
    knjCreateHidden($objForm, "HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_K", $model->Properties["HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_K"]);
    knjCreateHidden($objForm, "HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_K", $model->Properties["HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_K"]);
}
