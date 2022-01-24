<?php

require_once('for_php7.php');

class knjd178Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd178Form1", "POST", "knjd178index.php", "", "knjd178Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd178Query::getSemester();
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);

        //年組コンボ
        $query = knjd178Query::getAuth($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HRCLASS", $model->hrClass, $extra, 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //日付入力
        if ($model->field["IDO_DATE"] == "") $model->field["IDO_DATE"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["IDO_DATE"] = View::popUpCalendar($objForm, "IDO_DATE", $model->field["IDO_DATE"]);

        //一日欠席の時は欠課を出力する
        if ($model->field["OUT_PUT_KEKKA"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= "id='OUT_PUT_KEKKA'";
        $arg["data"]["OUT_PUT_KEKKA"] = knjCreateCheckBox($objForm, "OUT_PUT_KEKKA", "1", $extra);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd178Form1.html", $arg);
    }
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

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $leftList = $rightList = array();
    $result = $db->query(knjd178Query::getStudent($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!preg_match('{' . $row["VALUE"] . '}', $model->selectdata)) {
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        } else {
            $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //プレビュー/印刷
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdata2");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJD178");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
}
?>
