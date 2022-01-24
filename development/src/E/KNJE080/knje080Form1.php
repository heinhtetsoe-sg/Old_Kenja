<?php

require_once('for_php7.php');

class knje080Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje080Form1", "POST", "knje080index.php", "", "knje080Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //クラス選択コンボボックス
        $query = knje080Query::getAuth(CTRL_YEAR, CTRL_SEMESTER);
        $extra = "onchange=\"return btn_submit('knje080'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, $model);

        //年組のMAX文字数取得
        $max_len = 0;
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $zenkaku = (strlen($row["LABEL"]) - mb_strlen($row["LABEL"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["LABEL"]) - $zenkaku : mb_strlen($row["LABEL"]);
            $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model, $max_len);

        //成績証明書ラジオボタン 1:日本語 2:英語
        $opt_lang = array(1, 2);
        $model->field["LANGUAGE"] = ($model->field["LANGUAGE"] == "") ? "1" : $model->field["LANGUAGE"];
        $extra = array("id=\"LANGUAGE1\"", "id=\"LANGUAGE2\"");
        $radioArray = knjCreateRadio($objForm, "LANGUAGE", $model->field["LANGUAGE"], $extra, $opt_lang, get_count($opt_lang));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //校長名印刷ラジオボタン 1:する 2:しない
        $opt_kotyo = array(1, 2);
        $model->field["KOTYO"] = ($model->field["KOTYO"] == "") ? "1" : $model->field["KOTYO"];
        $extra = array("id=\"KOTYO1\"", "id=\"KOTYO2\"");
        $radioArray = knjCreateRadio($objForm, "KOTYO", $model->field["KOTYO"], $extra, $opt_kotyo, get_count($opt_kotyo));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //記載責任者コンボボックス
        $query = knje080Query::getStaffList();
        makeCmb($objForm, $arg, $db, $query, "SEKI", $model->field["SEKI"], "", 1, $model);

        //６年用フォーム選択チェックボックス
        if ($model->control["学校区分"] == "1") {
            $extra  = ($model->field["FORM6"] == "1") ? "checked" : "";
            $extra .= " id=\"FORM6\"";
            $arg["data"]["FORM6"] = knjCreateCheckBox($objForm, "FORM6", "1", $extra, "");
            $arg["tani"] = "1";
        }

        //記載日付
        $model->field["DATE"] = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje080Form1.html", $arg); 
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $max_len) {

    $opt1 = $opt2 = array();
    //生徒一覧リストを作成する
    $query = knje080Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //クラス名称調整
        $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
        $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
        $len = $zenkaku * 2 + $hankaku;
        $hr_name = $row["HR_NAME"];
        for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";
        if (!in_array($row["SCHREGNO"], $model->select_data["selectdata"])) {
            $opt1[]= array('label' => $row["SCHREGNO"].' '.$hr_name.' '.$row["ATTENDNO"].'番 '.$row["NAME_SHOW"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧リスト
    $extra = "multiple style=\"width:280px\" width=\"280px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 20);

    //出力対象者リストを作成する
    if ($model->select_data["selectdata"][0]) {
        $query = knje080Query::getList($model, $model->select_data["selectdata"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //クラス名称調整
            $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
            $len = $zenkaku * 2 + $hankaku;
            $hr_name = $row["HR_NAME"];
            for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

            $opt2[]= array('label' => $row["SCHREGNO"].' '.$hr_name.' '.$row["ATTENDNO"].'番 '.$row["NAME_SHOW"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
    }

    //出力対象者一覧リスト
    $extra = "multiple style=\"width:280px\" width=\"280px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", isset($opt2)?$opt2:array(), $extra, 20);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        if ($name == "SEKI") {
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

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEKI") {
        $value = ($value && $value_flg) ? $value : STAFFCD;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE080");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "Knje080UseAForm", $model->Knje080UseAForm);
    knjCreateHidden($objForm, "seisekishoumeishoTaniPrintRyugaku", $model->Properties["seisekishoumeishoTaniPrintRyugaku"]);
    knjCreateHidden($objForm, "seisekishoumeishoNotPrintAnotherStudyrec", $model->Properties["seisekishoumeishoNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "DOCUMENTROOT",  DOCUMENTROOT);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useGakkaSchoolDiv", $model->Properties["useGakkaSchoolDiv"]);
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "certifPrintRealName",  $model->Properties["certifPrintRealName"]);
    knjCreateHidden($objForm, "seisekishoumeishoPrintCoursecodename", $model->Properties["seisekishoumeishoPrintCoursecodename"]);
    knjCreateHidden($objForm, "seisekishoumeishoCreditOnlyClasscd", $model->Properties["seisekishoumeishoCreditOnlyClasscd"]);
}
?>
