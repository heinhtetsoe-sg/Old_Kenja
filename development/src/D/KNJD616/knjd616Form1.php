<?php

require_once('for_php7.php');

class knjd616Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd616Form1", "POST", "knjd616index.php", "", "knjd616Form1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjd616Query::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onchange=\"return btn_submit('gakki');\"", 1);

        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }

        //学年コンボ
        $query = knjd616Query::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "onChange=\"return btn_submit('grade');\"", 1);

        //テスト種別コンボ
        $query  = knjd616Query::GetName($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "", 1, $model);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //評定平均
        $extra = "onBlur=\"check_value(this)\"";
        if ($model->field["HYOUTEI_HEIKIN"] == '' || $model->cmd == 'grade') {
            $query = knjd616Query::getSchoolKind($model);
            $schoolKind = $db->getOne($query);
            if ($schoolKind == 'H') {
                $model->field["HYOUTEI_HEIKIN"] = '5.7';
            } else {
                $model->field["HYOUTEI_HEIKIN"] = '6.0';
            }
        }
        $arg["data"]["HYOUTEI_HEIKIN"] = knjCreateTextBox($objForm, $model->field["HYOUTEI_HEIKIN"], "HYOUTEI_HEIKIN", 4, 4, $extra);

        //序列
        $opt = array(1, 2, 3); //1:学年 2:クラス 3:コース
        $model->field["JORETU_DIV"] = ($model->field["JORETU_DIV"] == "") ? "1" : $model->field["JORETU_DIV"];
        $extra = array("id=\"JORETU_DIV1\"", "id=\"JORETU_DIV2\"", "id=\"JORETU_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "JORETU_DIV", $model->field["JORETU_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //序列の基準点
        $opt = array(1, 2); //1:総合点 2:平均点
        $query = knjd616Query::getSchregRegdGdat($model->field["GRADE"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $syokiti = $model->field["JORETU_BASE"] ? $model->field["JORETU_BASE"] : 1;
        if ($model->cmd == 'grade' || $model->cmd == '') {
            if ($row["SCHOOL_KIND"] == 'H' && $row["GRADE_CD"] >= '02') {
                $syokiti = 2;
            } else {
                $syokiti = 1;
            }
        }
        $extra = array("id=\"JORETU_BASE1\"", "id=\"JORETU_BASE2\"");
        $radioArray = knjCreateRadio($objForm, "JORETU_BASE", $syokiti, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //一日欠席の時は欠課を出力する
        makeCheckBox($objForm, $arg, $model, "OUT_PUT_KEKKA");
        //空行チェックボックス
        makeCheckBox($objForm, $arg, $model, "KARAGYOU_NASI");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd616Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = "") {
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "") ? CTRL_SEMESTER : $value;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjd616Query::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

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

//チェックボックスを作成する
function makeCheckBox(&$objForm, &$arg, $model, $name) {
    $extra  = ($model->field[$name] == "1") ? "checked" : "";
    $extra .= " id=\"$name\"";
    $value = isset($model->field[$name]) ? $model->field[$name] : 1;

    $arg["data"][$name] = knjCreateCheckBox($objForm, $name, $value, $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //実行ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //閉じるボタン
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg) {
    knjCreateHidden($objForm, "DBNAME",            DB_DATABASE);
    knjCreateHidden($objForm, "PRGID" ,            "KNJD616");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR",              CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_YEAR",         CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER",     CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE",         CTRL_DATE);
    knjCreateHidden($objForm, "useCurriculumcd",   $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
}
?>
