<?php

require_once('for_php7.php');

class knjp728Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp728Form1", "POST", "knjp728index.php", "", "knjp728Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->year."年度";

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //校種コンボ
        $query = knjp728Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('knjp728');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //出力指定ラジオボタン 1:新入生 2:在籍生
        $opt = array(1, 2);
        $model->field["DIV"] = ($model->field["DIV"] == "") ? "2" : $model->field["DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DIV{$val}\" onClick=\"btn_submit('knjp728')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力指定ラジオボタン 1:クラス 2:個人
        $opt = array(1, 2);
        $model->field["CHOICE"] = ($model->field["CHOICE"] == "") ? "1" : $model->field["CHOICE"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"CHOICE{$val}\" onClick=\"btn_submit('knjp728')\"");
        }
        $radioArray = knjCreateRadio($objForm, "CHOICE", $model->field["CHOICE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力指定により処理が変わる
        if ($model->field["CHOICE"] == "2") {
            $arg["gr_class"] = "ON";
            //クラスコンボボックス
            $extra = "onChange=\"return btn_submit('knjp728');\"";
            $query = knjp728Query::getAuthClass($model);
            makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);
        }

        //納期限
        $monthArr = array();
        $query = knjp728Query::getMonth($model);
        $monthArr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opt = array();
        foreach ($model->month as $month) {
            $setVal = sprintf("%02d", $month);

            if ($monthArr["M_".$month] > 0) {
                $opt[] = array('label' => $month."月", 'value' => $setVal);
            }
        }
        $extra = "";
        $arg["data"]["PAID_LIMIT_MONTH"] = knjCreateCombo($objForm, "PAID_LIMIT_MONTH", $model->field["PAID_LIMIT_MONTH"], $opt, $extra, 1);

        //納入期限
        $value = ($model->field["LIMIT_DATE"] != '') ? $model->field["LIMIT_DATE"]: str_replace('-', '/', CTRL_DATE);
        $arg["data"]["LIMIT_DATE"] = View::popUpCalendarAlp($objForm, "LIMIT_DATE", $value, $disabled, "");

        //出力対象一覧リストを作成する
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        //終了ボタン
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
        //終了ボタン
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJP728");
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "SCHOOLCD", (sprintf("%012d", SCHOOLCD)));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp728Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    if ($query) {
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        if ($name == "SEMESTER") {
            $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
        } else {
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        }
        $result->free();
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//出力対象一覧リストを作成する
function makeListToList(&$objForm, &$arg, $db, $model) {
    if ($model->field["CHOICE"] == "1") {
        $arg["CHANGENAME"] = "クラス";
        $query = knjp728Query::getAuthClass($model);
    } else {
        $arg["CHANGENAME"] = "生徒";
        $query = knjp728Query::getAuthStudent($model);
    }

    //一覧リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    makeCmb($objForm, $arg, $db, $query, $model->field["DUMMY"], "CATEGORY_NAME", $extra, 20);

    //出力対象リストを作成する
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    makeCmb($objForm, $arg, $db, "", $model->field["DUMMY"], "CATEGORY_SELECTED", $extra, 20);

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
}
?>
