<?php

require_once('for_php7.php');

class knjmp964Form1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjmp964Form1", "POST", "knjmp964index.php", "", "knjmp964Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期リスト
        $query = knjmp964Query::getSemester($model);
        $extra = "onChange=\"return btn_submit('knjmp964');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, "");

        //学年コンボボックス
        $query = knjmp964Query::getGrade();
        $extra = "onChange=\"return btn_submit('knjmp964');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");

        //クラス一覧リスト作成する
        $query = knjmp964Query::getAuth($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", isset($row1) ? $row1 : array(), $extra, 20);

        //出力対象クラスリストを作成する
        $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 20);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //期間指定
        $opt = array(1, 2);
        $model->field["OUTGO_DIV"] = ($model->field["OUTGO_DIV"] == "") ? "1" : $model->field["OUTGO_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTGO_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "OUTGO_DIV", $model->field["OUTGO_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;


        $model->month = array("4" => "４月", "5" => "５月", "6" => "６月", "7" => "７月", "8" => "８月", "9" => "９月",
                              "10" => "１０月", "11" => "１１月", "12" => "１２月", "1" => "１月", "2" => "２月", "3" => "３月");
        list($year, $month, $day) = preg_split("/-/", CTRL_DATE);

        //収入伺日
        $query = knjmp964Query::getMonth($model);
        $extra = "";
        $model->field["REQUEST_MONTH"] = $model->field["REQUEST_MONTH"] ? $model->field["REQUEST_MONTH"] : $month;
        makeCmb($objForm, $arg, $db, $query, "REQUEST_MONTH", $model->field["REQUEST_MONTH"], $extra, 1, "");

        //収入決定日
        $extra = "";
        $model->field["OUTGO_MONTH"] = $model->field["OUTGO_MONTH"] ? $model->field["OUTGO_MONTH"] : $month;
        makeCmb($objForm, $arg, $db, $query, "OUTGO_MONTH", $model->field["OUTGO_MONTH"], $extra, 1, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjmp964Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "");
    }
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJMP964");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
}
?>
