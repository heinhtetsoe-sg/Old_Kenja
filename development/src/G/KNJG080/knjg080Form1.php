<?php

require_once('for_php7.php');

class knjg080Form1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjg080Form1", "POST", "knjg080index.php", "", "knjg080Form1");
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //ラジオボタン
        $opt = array(1, 2, 3);
        $model->field["SENTAKU"] = ($model->field["SENTAKU"] == "") ? "1" : $model->field["SENTAKU"];
        $extra = array("id=\"STUDENTS\" onclick=\"return btn_submit('knjg080')\"", "id=\"STAFF\" onclick=\"return btn_submit('knjg080')\"", "id=\"GUARDIAN\" onclick=\"return btn_submit('knjg080')\"");
        $radioArray = knjCreateRadio($objForm, "SENTAKU", $model->field["SENTAKU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
        }
        $query = knjg080Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('knjg080');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);

        //画面切り替え
        if ($model->field["SENTAKU"] == '1') {
            $arg["STAFF"] = "";
            $arg["STUDENTS"] = "1";
            $arg["GUARDIAN"] = "";
            $staff = 0;
            $students = 1;
            $guardian = 0;
        } else if ($model->field["SENTAKU"] == '2') {
            $arg["STUDENTS"] = "";
            $arg["STAFF"] = "1";
            $arg["GUARDIAN"] = "";
            $students = 0;
            $staff = 1;
            $guardian = 0;
        } else if ($model->field["SENTAKU"] == '3') {
            $arg["STUDENTS"] = "";
            $arg["STAFF"] = "";
            $arg["GUARDIAN"] = "1";
            $students = 0;
            $staff = 0;
            $guardian = 1;
        }

        //区分コンボ
        $opt = array();
        $value_flg = false;
        $query = knjg080Query::get_name_setup_div();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->div == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->div = ($model->div && $value_flg) ? $model->div : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('div')\"";
        $arg["data"]["DIV"] = knjCreateCombo($objForm, "DIV", $model->div, $opt, $extra, 1);

        //年組コンボ
        $opt = array();
        $value_flg = false;
        $query = knjg080Query::getAuth($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->hrClass == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->hrClass = ($model->hrClass && $value_flg) ? $model->hrClass : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('main')\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->hrClass, $opt, $extra, 1);

        //年度設定
        $opt = array();
        $value_flg = false;
        $query = knjg080Query::selectYearQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"], 
                           "value" => $row["VALUE"]);
            if($model->year == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->year = ($model->year && $value_flg) ? $model->year : CTRL_YEAR;
        $extra = "onchange=\"return btn_submit('')\"";
        $arg["data"]["STAFF_YEAR"] = knjCreateCombo($objForm, "STAFF_YEAR", $model->year, $opt, $extra, 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        
        //生徒用更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //生徒用詳細設定
        $extra = "onclick=\"return btn_submit('form2');\"";
        $arg["button"]["btn_form2"] = knjCreateBtn($objForm, "btn_form2", "詳細設定", $extra);
        
        //職員用更新
        $extra = "onclick=\"return btn_submit('update_staff');\"";
        $arg["button"]["btn_update_staff"] = knjCreateBtn($objForm, "btn_update_staff", "更 新", $extra);

        //職員用詳細設定
        $extra = "onclick=\"return btn_submit('form3');\"";
        $arg["button"]["btn_form3"] = knjCreateBtn($objForm, "btn_form3", "詳細設定", $extra);
        
        //保護者用更新
        $extra = "onclick=\"return btn_submit('update_guardian');\"";
        $arg["button"]["btn_update_guardian"] = knjCreateBtn($objForm, "btn_update_guardian", "更 新", $extra);

        //保護者用詳細設定
        $extra = "onclick=\"return btn_submit('form4');\"";
        $arg["button"]["btn_form4"] = knjCreateBtn($objForm, "btn_form4", "詳細設定", $extra);
        
        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");
        knjCreateHidden($objForm, "selectstaffdata");
        knjCreateHidden($objForm, "selectstaffdata2");
        knjCreateHidden($objForm, "selectguardiandata");
        knjCreateHidden($objForm, "selectguardiandata2");
        knjCreateHidden($objForm, "STUDENTSVALUE", $students);
        knjCreateHidden($objForm, "STAFFVALUE", $staff);
        knjCreateHidden($objForm, "GUARDIANVALUE", $guardian);
        
        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg080Form1.html", $arg); 
    }
}
/*****************************************************************************************************************/
/***************************************** 以下関数 **************************************************************/
/*****************************************************************************************************************/
//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //生徒対象者一覧
    $leftList = array();
    if ($model->field["SENTAKU"] == '1') {
        $query = knjg080Query::getStudent_left($model);
    } else if ($model->field["SENTAKU"] == '2') {
        $query = knjg080Query::getStaff_left($model);
    } else if ($model->field["SENTAKU"] == '3') {
        $query = knjg080Query::getGuardian_left($model);
    }
    list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->field["SENTAKU"] == '2') {
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["VALUE"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $SET_VALUE = $ume.substr($row["VALUE"], (strlen($row["VALUE"]) - (int)$simo), (int)$simo);
            } else {
                $SET_VALUE = $row["VALUE"];
            }
            $row["LABEL"] = str_replace($row["VALUE"], $SET_VALUE, $row["LABEL"]);
        }
        $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($model->field["SENTAKU"] == '1') {
            $left_schregno[] = $row["VALUE"];
        } else if ($model->field["SENTAKU"] == '2') {
            $left_staffcd[] = $row["VALUE"];
        } else if ($model->field["SENTAKU"] == '3') {
            $left_guardian[] = $row["VALUE"];
        }
    }
    $result->free();

    //生徒一覧
    $rightList = array();
    if ($model->field["SENTAKU"] == '1') {
        $query = knjg080Query::getStudent($model, $left_schregno);
    } else if ($model->field["SENTAKU"] == '2') {
        $query = knjg080Query::getStaff($model, $left_staffcd);
    } else if ($model->field["SENTAKU"] == '3') {
        $query = knjg080Query::getGuardian($model, $left_guardian);
    }
    list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->field["SENTAKU"] == '2') {
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["VALUE"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $SET_VALUE = $ume.substr($row["VALUE"], (strlen($row["VALUE"]) - (int)$simo), (int)$simo);
            } else {
                $SET_VALUE = $row["VALUE"];
            }
            $row["LABEL"] = str_replace($row["VALUE"], $SET_VALUE, $row["LABEL"]);
        }
        $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
