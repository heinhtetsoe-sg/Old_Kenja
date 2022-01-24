<?php

require_once('for_php7.php');

class knjp971Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjp971Form1", "POST", "knjp971index.php", "", "knjp971Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjp971Query::getSemester();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $query = knjp971Query::getGrade();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //納入期限
        $arg["data"]["LIMIT_DATE"] = View::popUpCalendar($objForm, "LIMIT_DATE", $model->field["LIMIT_DATE"]);

        //督促納入日
        $model->field["TOKUSOKU_DATE"] = ($model->field["TOKUSOKU_DATE"]) ? $model->field["TOKUSOKU_DATE"] : "";
        $arg["data"]["TOKUSOKU_DATE"] = View::popUpCalendar($objForm, "TOKUSOKU_DATE", $model->field["TOKUSOKU_DATE"]);

        //納入期限
        $arg["data"]["PRINT_DATE"] = View::popUpCalendar($objForm, "PRINT_DATE", $model->field["PRINT_DATE"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model, $arg);

        //DB切断
        Query::dbCheckIn($db);

        if (!isset($model->warning) && $model->cmd == 'read'){
            $model->cmd = 'knjp971';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp971Form1.html", $arg); 
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

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    $opt = array();
    $opt_left = array();
    $selectleft = explode(",", $model->selectleft);

    $query = knjp971Query::getSchregData($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->select_opt[$row["SCHREGNO"]] = array('label' => $row["HR_NAME"].' '.$row["ATTENDNO"].'番　'.$row["SCH_NAME"],
                                                     'value' => $row["SCHREGNO"]);
        if ($model->cmd == 'read' ) {
            if (!in_array($row["SCHREGNO"], $selectleft)){
                $opt[]= array('label' => $row["HR_NAME"].' '.$row["ATTENDNO"].'番　'.$row["SCH_NAME"],
                               'value' => $row["SCHREGNO"]);
            }
        } else {
            $opt[]= array('label' => $row["HR_NAME"].' '.$row["ATTENDNO"].'番　'.$row["SCH_NAME"],
                           'value' => $row["SCHREGNO"]);
        }
    }
    //左リストで選択されたものを再セット
    if ($model->cmd == 'read' ) {
        foreach ($model->select_opt as $key => $val) {
            if (in_array($key, $selectleft)) {
                $opt_left[] = $val;
            }
        }
    }
    $result->free();

    //一覧リスト
    $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 15);

    //出力対象一覧リスト
    $extra = "multiple style=\"width:180px\" width:\"180px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 15);

    //対象取消ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //読込ボタン
    $extra = "onclick=\"return btn_submit('knjp971');\"";
    $arg["button"]["btn_yomikomi"] = knjCreateBtn($objForm, "btn_yomikomi", "読 込", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //印刷/更新ボタン
    $extra = "onClick=\"btn_submit('update');\"";
    $arg["button"]["btn_print_upd"] = knjCreateBtn($objForm, "btn_print_upd", "印刷／更新", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, &$arg) {
    $arg["TOP"]["CTRL_YEAR"] = knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    $arg["TOP"]["CTRL_SEMESTER"] = knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    $arg["TOP"]["CTRL_DATE"] = knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    $arg["TOP"]["DBNAME"] = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    $arg["TOP"]["PRGID"] = knjCreateHidden($objForm, "PRGID", "KNJP971");
    $arg["TOP"]["useAddrField2"] = knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectleft");
}
?>
