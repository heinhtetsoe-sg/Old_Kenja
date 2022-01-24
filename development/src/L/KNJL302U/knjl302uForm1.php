<?php

require_once('for_php7.php');

class knjl302uForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl302uForm1", "POST", "knjl302uindex.php", "", "knjl302uForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["TOP"]["YEAR"] = $model->examyear."年度";

        //入試制度コンボ
        $query = knjl302uQuery::getNameMst($model->examyear, "L003");
        $extra = "onChange=\"return btn_submit('knjl302u')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "TOP", "");

        //入試区分
        $query = knjl302uQuery::getNameMst($model->examyear, "L004");
        $extra = "onChange=\"return btn_submit('knjl302u')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "TOP", "");

        //出力順ラジオ（1:受験番号、2:氏名かな）
        $opt = array(1, 2);
        $model->field["SORT_DIV"] = ($model->field["SORT_DIV"] == "") ? "1" : $model->field["SORT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SORT_DIV{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT_DIV", $model->field["SORT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //会場一覧リストToリスト
        $row1 = array();
        $query = knjl302uQuery::getHallData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }

        //クラス一覧作成
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["HALL_NAME"] = knjCreateCombo($objForm, "HALL_NAME", "", $row1, $extra, 20);

        //出力対象作成
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["HALL_SELECTED"] = knjCreateCombo($objForm, "HALL_SELECTED", "", array(), $extra, 20);

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

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL302U");
        knjCreateHidden($objForm, "SELECTED_DATA");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl302uForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $argName = "", $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$argName][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
