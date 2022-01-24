<?php

require_once('for_php7.php');

class knjb131Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjb131Form1", "POST", "knjb131index.php", "", "knjb131Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->year;

        /**********/
        /* コンボ */
        /**********/
        //学期コンボボックスを作成する
        $query = knjb131Query::getSemester($model);
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onchange=\"return btn_submit('gakki');\"", 1);

        //教科コンボ
        $query = knjb131Query::getSubclassMst($model, "CLASS");
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], "onChange=\"return btn_submit('knjb131');\"", 1);

        //科目コンボ
        $query = knjb131Query::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], "onChange=\"return btn_submit('knjb131');\"", 1);

        //一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //来室日付範囲
        $semes = $db->getRow(knjb131Query::getSemester($model, "9"), DB_FETCHMODE_ASSOC);
        $model->field["DATE1"] = ($model->field["DATE1"]) ? str_replace("-", "/", $model->field["DATE1"]) : str_replace("-", "/", $semes["SDATE"] ? $semes["SDATE"] : CTRL_DATE);
        $model->field["DATE2"] = ($model->field["DATE2"]) ? str_replace("-", "/", $model->field["DATE2"]) : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm, "DATE1", $model->field["DATE1"]);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm, "DATE2", $model->field["DATE2"]);

        /**********/
        /* ボタン */
        /**********/
        //プレビュー
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CHK_SDATE", CTRL_YEAR.'/04/01');
        knjCreateHidden($objForm, "CHK_EDATE", (CTRL_YEAR+1).'/03/31');
        /**********/
        knjCreateHidden($objForm, "PRGID", "KNJB131");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb131Form1.html", $arg); 
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
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $opt = array();
    $result = $db->query(knjb131Query::getChairDat($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[]= array('label' => "{$row["LABEL"]}",
                      'value' => "{$row["VALUE"]}"
                     );
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:250px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:250px;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 15);

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
