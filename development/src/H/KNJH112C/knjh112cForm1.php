<?php

require_once('for_php7.php');

class knjh112cForm1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh112cForm1", "POST", "knjh112cindex.php", "", "knjh112cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjh112cQuery::getSemester();
        $extra = " onchange=\"return btn_submit('changeSeme')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, "");

        //資格コンボ
        $query = knjh112cQuery::getQualifiedMst();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SIKAKUCD", $model->field["SIKAKUCD"], $extra, 1, "");

        //ラジオボタン(1;クラス, 2:個人)
        $opt = array(1, 2);
        $model->field["TAISYOU"] = ($model->field["TAISYOU"] == "") ? "1" : $model->field["TAISYOU"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TAISYOU{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "TAISYOU", $model->field["TAISYOU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        $arg["KOJIN"]     = ($model->field["TAISYOU"] == "2") ? "1": "";
        $arg["LIST_NAME"] = ($model->field["TAISYOU"] == "1") ? "クラス": "生徒";

        //受験期間日付作成
        $arraySeme = array();
        $query = knjh112cQuery::getSemDate($model->field["SEMESTER"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arraySeme["SDATE"] = str_replace("-", "/", $row["SDATE"]);
            $arraySeme["EDATE"] = str_replace("-", "/", $row["EDATE"]);
        }
        //受験期間(from)
        $model->field["FJ_DATE"] = ($model->cmd == "changeSeme" || (!$model->field["FJ_DATE"])) ? $arraySeme["SDATE"] : $model->field["FJ_DATE"];
        $arg["FJ_DATE"] = View::popUpCalendarAlp($objForm, "FJ_DATE", $model->field["FJ_DATE"], $disabled, "");

        //受験期間(to)
        $model->field["TJ_DATE"] = ($model->cmd == "changeSeme" || (!$model->field["TJ_DATE"])) ? $arraySeme["EDATE"] : $model->field["TJ_DATE"];
        $arg["TJ_DATE"] = View::popUpCalendarAlp($objForm, "TJ_DATE", $model->field["TJ_DATE"], $disabled, "");

        //年組コンボ
        $opt = array();
        $value_flg = false;
        $query = knjh112cQuery::getAuth($model, $model->field["SEMESTER"]);
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

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'pdf');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //CSVボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
        //終了ボタン
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJH112C");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh112cForm1.html", $arg); 
    }
}
/*****************************************************************************************************************/
/***************************************** 以下関数 **************************************************************/
/*****************************************************************************************************************/
//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //生徒一覧
    $rightList = array();
    if ($model->field["TAISYOU"] == "1") {
        $query = knjh112cQuery::getAuth($model, $model->field["SEMESTER"]);
    } else {
        $query = knjh112cQuery::getStudent($model, $model->field["SEMESTER"]);
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('right')\"";
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
