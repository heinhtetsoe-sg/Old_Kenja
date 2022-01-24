<?php

require_once('for_php7.php');

class knje451Form1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knje451Form1", "POST", "knje451index.php", "", "knje451Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //年組コンボ
        $extra = "onChange=\"return btn_submit('knje451');\"";
        $query = knje451Query::getHrclass($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //クラス一覧リスト
        makeClassList($objForm, $arg, $db, $model);

        //職員コンボ
        $extra = "onChange=\"return btn_submit('knje451');\"";
        $query = knje451Query::GetStaffcd();
        makeCmb($objForm, $arg, $db, $query, "STAFFCD", $model->field["STAFFCD"], $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje451Form1.html", $arg); 
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

    if ($name == "STAFFCD") {
        $value = ($value && $value_flg) ? $value : STAFFCD;
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

//クラス一覧リスト作成
function makeClassList(&$objForm, &$arg, $db, &$model) {
    $row1 = array();
    $query = knje451Query::getStudents($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    //一覧リストを作成する
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["SCHREG_NAME"] = knjCreateCombo($objForm, "SCHREG_NAME", "", $row1, $extra, 20);

    //出力対象教科リストを作成する
    $extra = "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["SCHREG_SELECTED"] = knjCreateCombo($objForm, "SCHREG_SELECTED", "", array(), $extra, 20);

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

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJE451");

    list ($grade, $hr_class) = preg_split("/-/",$model->field["GRADE_HR_CLASS"]);
    knjCreateHidden($objForm, "GRADE", $grade);
}
?>
