<?php

require_once('for_php7.php');

class knjc155Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc155Form1", "POST", "knjc155index.php", "", "knjc155Form1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjc155Query::getSemester();
        $semester_name = $db->getOne($query);
        $arg["data"]["SEMESTER"] = $semester_name;

        //年組コンボ
        $query = knjc155Query::getSelectHrclass($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE_HRCLASS", $model->field["GRADE_HRCLASS"], "onChange=\"return btn_submit('knjc155');\"", 1);

        //前期科目を出力しない
        $extra = $model->cmd == '' && CTRL_SEMESTER != '1' || $model->field["NOT_PRINT_ZENKI"] == "1" ? "checked" : "";
        $extra .= " id=\"NOT_PRINT_ZENKI\" ";
        $arg["data"]["NOT_PRINT_ZENKI"] = knjCreateCheckBox($objForm, "NOT_PRINT_ZENKI", "1", $extra, "");

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //実行ボタン
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
        //CSVボタン
        $arg["button"]["btn_csv"]   = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"");
        //閉じるボタン
        $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",         "KNJC155");
        knjCreateHidden($objForm, "SCHOOLCD",            sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "use_prg_schoolkind",  $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind",    $model->selectSchoolKind);
        knjCreateHidden($objForm, "printSubclassLastChairStd", $model->Properties["printSubclassLastChairStd"]);


        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc155Form1.html", $arg); 
    }
}
/*********************************************** 以下関数 *******************************************************/
/**********/
/* コンボ */
/**********/
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

/******************/
/* リストToリスト */
/******************/
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjc155Query::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
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
?>
