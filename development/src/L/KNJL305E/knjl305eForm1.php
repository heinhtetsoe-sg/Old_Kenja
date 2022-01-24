<?php

require_once('for_php7.php');

class knjl305eForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl305eForm1", "POST", "knjl305eindex.php", "", "knjl305eForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["data"]["YEAR"] = $model->examyear."年度";

        //入試制度
        $query = knjl305eQuery::getNameMst($model->examyear, "L003");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1);

        //入試区分（A、B）
        $opt = array();
        $opt[] = array('label' => 'Ａ日程', 'value' => '2');
        $opt[] = array('label' => 'Ｂ日程', 'value' => '3');
        $extra = "";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

        //出力順
        $opt = array(1, 2);
        $extra = array("id=\"OUTPUT_ORDER1\" id=\"OUTPUT_ORDER2\"");
        $model->outputorder = strlen($model->outputorder) ? $model->outputorder : "1";
        $radioArray = knjCreateRadio($objForm, "OUTPUT_ORDER", $model->outputorder, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

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
        knjCreateHidden($objForm, "PRGID", "KNJL305E");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl305eForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
