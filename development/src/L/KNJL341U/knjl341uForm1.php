<?php

require_once('for_php7.php');

class knjl341uForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl341uForm1", "POST", "knjl341uindex.php", "", "knjl341uForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["TOP"]["YEAR"] = $model->examyear."年度";

        //入試制度コンボ
        $query = knjl341uQuery::getNameMst($model->examyear, "L003");
        $extra = "onChange=\"return btn_submit('knjl341u')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "TOP", "");

        //入試区分
        $query = knjl341uQuery::getNameMst($model->examyear, "L004");
        $extra = "onChange=\"return btn_submit('knjl341u')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "TOP", "");

        //行
        $opt = array();
        $value_flg = false;
        $opt[] = array('label' => "１行",'value' => 1);
        $opt[] = array('label' => "２行",'value' => 2);
        $opt[] = array('label' => "３行",'value' => 3);
        $opt[] = array('label' => "４行",'value' => 4);
        $opt[] = array('label' => "５行",'value' => 5);
        $opt[] = array('label' => "６行",'value' => 6);
        $opt[] = array('label' => "７行",'value' => 7);
        $opt[] = array('label' => "８行",'value' => 8);
        $extra = "";
        $arg["data"]["POROW"] = knjCreateCombo($objForm, "POROW", $model->field["POROW"], $opt, $extra, 1);

        //列
        $opt = array();
        $opt[] = array('label' => "１列",'value' => 1);
        $opt[] = array('label' => "２列",'value' => 2);
        $opt[] = array('label' => "３列",'value' => 3);
        $extra = "";
        $arg["data"]["POCOL"] = knjCreateCombo($objForm, "POCOL", $model->field["POCOL"], $opt, $extra, 1);

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
        knjCreateHidden($objForm, "PRGID", "KNJL341U");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl341uForm1.html", $arg);
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
