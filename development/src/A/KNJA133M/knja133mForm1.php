<?php

require_once('for_php7.php');

class knja133mForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja133mForm1", "POST", "knja133mindex.php", "", "knja133mForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = $model->control["学期名"][CTRL_SEMESTER];

        $z010name1 = "";
        $query = knja133mQuery::getNameMst("Z010", "00");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $z010name1 = $row["NAME1"];
        }
        $result->free();
        if ($z010name1 == 'tokyoto') {
            $arg["tokyoto"] = 1;
        } else {
            $arg["not_tokyoto"] = 2;
        }

        //クラスコンボ作成
        $query = knja133mQuery::getHrClass();
        $extra = "onchange=\"return btn_submit('knja133m'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //帳票種別チェックボックス
        $name = array("KOSEKI", "SEITO", "SIMEI", "SCHZIP", "ADDR2", "SCHOOLZIP", "KATSUDO", "GAKUSHU", "TANI", "notPrintKyugakuNendoInYoshiki2");
        if ($z010name1 == 'miyagiken') {
            $name = array("SEITO", "KATSUDO", "GAKUSHU", "TANI", "notPrintKyugakuNendoInYoshiki2");
        } else if ($z010name1 == 'tokyoto') {
            $name[] = "SHUKKETSU";
        }
        foreach($name as $key => $val){
            $def = ($val == "KOSEKI" || $val == "SCHZIP" || $val == "ADDR2" || $val == "SCHOOLZIP" || $val == "notPrintKyugakuNendoInYoshiki2") ? false : true;
            $extra = ($model->field[$val] == "1" || $def && $model->cmd == "") ? "checked" : "";
            $extra .= " id=\"$val\"";
            $extra .= " onclick=\"OptionUse('this');\"";
            if (($val == "SIMEI" || $val == "SCHZIP" || $val == "ADDR2" || $val == "SCHOOLZIP") && $model->field["SEITO"] == "" && $model->cmd) {
                $extra .= " disabled";
            }
            $arg["data"][$val] = knjCreateCheckBox($objForm, $val, "1", $extra, "");
        }

        $space =  "　　　　　　　　　　　　　&nbsp;";
        $arg["data"]["SEITO_OPT"] = array();
        //$arg["data"]["SEITO_OPT"][] = array("SPACE"=> $space, "FORM" => $arg["data"]["KOSEKI"], "ID" => "KOSEKI", "TITLE" => "生戸籍名出力"); 
        if ($z010name1 == "miyagiken") {
            knjCreateHidden($objForm, "SIMEI", "1");
        } else {
            $arg["data"]["SEITO_OPT"][] = array("SPACE"=> $space, "FORM" => $arg["data"]["SIMEI"], "ID" => "SIMEI", "TITLE" => "生徒・保護者氏名出力"); 
            $arg["data"]["SEITO_OPT"][] = array("SPACE"=> $space, "FORM" => $arg["data"]["SCHZIP"], "ID" => "SCHZIP", "TITLE" => "現住所の郵便番号出力"); 
            $arg["data"]["SEITO_OPT"][] = array("SPACE"=> $space, "FORM" => $arg["data"]["SCHOOLZIP"], "ID" => "SCHOOLZIP", "TITLE" => "学校所在地の郵便番号出力"); 
       }
        //$arg["data"]["SEITO_OPT"][] = array("SPACE"=> $space, "FORM" => $arg["data"]["ADDR2"], "ID" => "ADDR2", "TITLE" => "現住所の方書き(アパート名)出力"); 
        $arg["data"]["SEITO_OPT"][0]["SPACE"] = "";

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        if (!isset($model->warning) && $model->cmd == 'print'){
            $model->cmd = 'knja133m';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja133mForm1.html", $arg); 
    }
}
/****************************************** 以下関数 ******************************************************/
//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model) {
    //対象外の生徒取得
    $query = knja133mQuery::getSchnoIdou($model);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : "";

    //対象者リストを作成する
    $query = knja133mQuery::getStudent($model, 'list', $selectdata);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $idou = "　";
        if (in_array($row["SCHREGNO"],$opt_idou)) {
            $idou = "●";
        }

        $opt1[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                        'value' => $row["SCHREGNO"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

    //生徒一覧リストを作成する
    if($selectdata){
        $query = knja133mQuery::getStudent($model, 'select', $selectdata);
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $idou = "　";
            if (in_array($row["SCHREGNO"],$opt_idou)) {
                $idou = "●";
            }

            $opt1[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                            'value' => $row["SCHREGNO"]);
        }
        $result->free();

        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt1, $extra, 20);
    } else {
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);
    }

    //未履修科目出力・履修のみ科目出力のデフォルト値設定
    if ($model->field["MIRISYU"] == "" || $model->field["RISYU"] == "" || $model->field["RISYUTOUROKU"] == "") {
        $query = knja133mQuery::getRisyuMirsyu($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            // 値がYなら「しない」それ以外は「する」
            $model->field["MIRISYU"] = "Y" == $row["NAMESPARE1"] ? "2" : "1";
            $model->field["RISYU"] = "Y" == $row["NAMESPARE2"] ? "2" : "1";
            $model->field["RISYUTOUROKU"] = "Y" == $row["NAMESPARE3"] ? "2" : "1";
        }
        $result->free();
        $model->field["MIRISYU"] = isset($model->field["MIRISYU"]) ? $model->field["MIRISYU"] : "2"; // デフォルトは「しない」
        $model->field["RISYU"] = isset($model->field["RISYU"]) ? $model->field["RISYU"] : "1"; // デフォルトは「する」
        $model->field["RISYUTOUROKU"] = isset($model->field["RISYUTOUROKU"]) ? $model->field["RISYUTOUROKU"] : "2"; // デフォルトは「しない」
    }

    //未履修科目出力ラジオボタンを作成する 1:する 2:しない
    $opt_mirisyu = array(1, 2);
    $extra  = array("id=\"MIRISYU1\" onclick=\"checkRisyu();\"", "id=\"MIRISYU2\" onclick=\"checkRisyu();\"");
    $radioArray = knjCreateRadio($objForm, "MIRISYU", $model->field["MIRISYU"], $extra, $opt_mirisyu, get_count($opt_mirisyu));
    foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

    //履修のみ科目出力ラジオボタン 1:する 2:しない
    $optRisyu = array(1, 2);
    $extra  = array("id=\"RISYU1\" onclick=\"checkRisyu();\"", "id=\"RISYU2\" onclick=\"checkRisyu();\"");
    $radioArray = knjCreateRadio($objForm, "RISYU", $model->field["RISYU"], $extra, $optRisyu, get_count($optRisyu));
    foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

    //履修登録のみ科目出力ラジオボタン 1:する 2:しない
    $optRisyuT = array(1, 2);
    $extra  = array("id=\"RISYUTOUROKU1\" onclick=\"checkRisyu();\"", "id=\"RISYUTOUROKU2\" onclick=\"checkRisyu();\"");
    $radioArray = knjCreateRadio($objForm, "RISYUTOUROKU", $model->field["RISYUTOUROKU"], $extra, $optRisyuT, get_count($optRisyuT));
    foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    if ($model->Properties["useSchregRegdHdat"] == '1') {
        $useSchregRegdHdat = '1';
    } else {
        $useSchregRegdHdat = '0';
    }

    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJA133M");
    knjCreateHidden($objForm, "COLOR_PRINT", "1");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "INEI", $model->inei);
    knjCreateHidden($objForm, "useSchregRegdHdat", $useSchregRegdHdat);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintAnotherStudyrec", $model->Properties["seitoSidoYorokuNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintAnotherAttendrec", $model->Properties["seitoSidoYorokuNotPrintAnotherAttendrec"]);
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useStudyrecRemarkQualifiedDat" , $model->Properties["useStudyrecRemarkQualifiedDat"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalremarkSize" , $model->Properties["seitoSidoYoroku_dat_TotalremarkSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyactSize" , $model->Properties["seitoSidoYoroku_dat_TotalstudyactSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_TotalstudyvalSize" , $model->Properties["seitoSidoYoroku_dat_TotalstudyvalSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_SpecialactremarkSize" , $model->Properties["seitoSidoYoroku_dat_SpecialactremarkSize"]);
    knjCreateHidden($objForm, "seitoSidoYoroku_dat_Attendrec_RemarkSize" , $model->Properties["seitoSidoYoroku_dat_Attendrec_RemarkSize"]);
    knjCreateHidden($objForm, "seitoSidoYorokuYoshiki2PrintOrder" , $model->Properties["seitoSidoYorokuYoshiki2PrintOrder"]);
    knjCreateHidden($objForm, "seitoSidoYorokuFinschoolFinishDateYearOnly" , $model->Properties["seitoSidoYorokuFinschoolFinishDateYearOnly"]);
    knjCreateHidden($objForm, "HR_ATTEND_DAT_NotSansyou" , $model->Properties["HR_ATTEND_DAT_NotSansyou"]);
    knjCreateHidden($objForm, "notPrintFinschooltypeName", $model->Properties["notPrintFinschooltypeName"]);
    knjCreateHidden($objForm, "seitoSidoYorokuPrintCoursecodename" , $model->Properties["seitoSidoYorokuPrintCoursecodename"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintZaisekiSubekiKikan" , $model->Properties["seitoSidoYorokuNotPrintZaisekiSubekiKikan"]);
    knjCreateHidden($objForm, "seitoSidoYorokuTaniPrintAbroad" , $model->Properties["seitoSidoYorokuTaniPrintAbroad"]);
    knjCreateHidden($objForm, "seitoSidoYorokuTaniPrintSogaku" , $model->Properties["seitoSidoYorokuTaniPrintSogaku"]);
    knjCreateHidden($objForm, "seitoSidoYorokuTaniPrintTotal" , $model->Properties["seitoSidoYorokuTaniPrintTotal"]);

}
?>
