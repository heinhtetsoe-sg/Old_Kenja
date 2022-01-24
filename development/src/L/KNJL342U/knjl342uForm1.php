<?php

require_once('for_php7.php');

class knjl342uForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl342uForm1", "POST", "knjl342uindex.php", "", "knjl342uForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["TOP"]["YEAR"] = $model->examyear."年度";

        //入試制度コンボ
        $query = knjl342uQuery::getNameMst($model->examyear, "L003");
        $extra = "onChange=\"return btn_submit('knjl342u')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "TOP", "");

        //入試区分
        $query = knjl342uQuery::getNameMst($model->examyear, "L004");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "TOP", "");

        //ラジオボタン（1:手続者一覧表、2:説明会出席者一覧表、3:入学者一覧表）
        $opt = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力順ラジオ（1:受験番号、2:氏名かな）
        $opt = array(1, 2);
        $model->field["SORT_DIV"] = ($model->field["SORT_DIV"] == "") ? "1" : $model->field["SORT_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SORT_DIV{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT_DIV", $model->field["SORT_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

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
        knjCreateHidden($objForm, "PRGID", "KNJL342U");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl342uForm1.html", $arg);
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
