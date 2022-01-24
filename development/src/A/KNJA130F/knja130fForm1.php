<?php

require_once('for_php7.php');

class knja130fForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja130fForm1", "POST", "knja130findex.php", "", "knja130fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = $model->control["学期名"][CTRL_SEMESTER];

        //クラスコンボ作成
        $query = common::getHrClassAuth(CTRL_YEAR, CTRL_SEMESTER, AUTHORITY, STAFFCD);
        $extra = "onchange=\"return btn_submit('knja130f'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //帳票種別チェックボックス
        $name = array("KOSEKI", "SEITO", "SIMEI", "COLOR_PRINT", "SCHZIP", "ADDR2", "SCHOOLZIP", "KATSUDO", "GAKUSHU", "TANI");
        foreach($name as $key => $val){
            if ($val == "KOSEKI" || $val == "SCHZIP" || $val == "ADDR2" || $val == "SCHOOLZIP") {
                $extra = $model->field[$val] == "1" ? "checked" : "";
            } else {
                $extra = ($model->field[$val] == "1" || $model->cmd == "") ? "checked" : "";
            }
            if ($val == "COLOR_PRINT" && $model->cmd == "") {
                $extra = "";
            }
            $extra .= " id=\"$val\"";
            $extra .= " onclick=\"OptionUse('this');\"";
            if(($val == "SIMEI" || $val == "COLOR_PRINT" || $val == "SCHZIP" || $val == "ADDR2" || $val == "SCHOOLZIP") && $model->field["SEITO"] == "" && $model->cmd){
                $extra .= " disabled";
            }
            $arg["data"][$val] = knjCreateCheckBox($objForm, $val, "1", $extra, "");
        }

        //ラジオボタン作成
        $radioValue = array(1, 2);
        $model->radio = ($model->radio == "") ? "1" : $model->radio;
        $extra = array("id=\"RADIO1\"", "id=\"RADIO2\"");
        $radioArray = knjCreateRadio($objForm, "RADIO", $model->radio, $extra, $radioValue, get_count($radioValue));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja130fForm1.html", $arg); 
    }
}
/****************************************** 以下関数 ******************************************************/
//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model) {
    //対象外の生徒取得
    $query = knja130fQuery::getSchnoIdou($model);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : "";

    //対象者リストを作成する
    $query = knja130fQuery::getStudent($model, 'list', $selectdata);
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
        $query = knja130fQuery::getStudent($model, 'select', $selectdata);
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
    $extra = "onclick=\"return doSubmit('" . SERVLET_URL . "');\"";
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
    knjCreateHidden($objForm, "PRGID", "KNJA130F");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "cmd");
//    knjCreateHidden($objForm, "INEI", $model->inei);
    knjCreateHidden($objForm, "useSchregRegdHdat", $useSchregRegdHdat);
    knjCreateHidden($objForm, "useCurriculumcd" , $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintAnotherStudyrec", $model->Properties["seitoSidoYorokuNotPrintAnotherStudyrec"]);
    knjCreateHidden($objForm, "seitoSidoYorokuNotPrintAnotherAttendrec", $model->Properties["seitoSidoYorokuNotPrintAnotherAttendrec"]);
    knjCreateHidden($objForm, "seitoSidoYorokuHozonkikan" , $model->Properties["seitoSidoYorokuHozonkikan"]);
}
?>
