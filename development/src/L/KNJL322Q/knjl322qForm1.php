<?php

require_once('for_php7.php');

class knjl322qForm1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl322qForm1", "POST", "knjl322qindex.php", "", "knjl322qForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl322q');\"";
        $query = knjl322qQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $extra = " onchange=\"return btn_submit('knjl322q');\"";
        if (SCHOOLKIND == "J") {
            $query = knjl322qQuery::getNameMst($model->ObjYear, "L024");
        } else {
            $query = knjl322qQuery::getNameMstL004($model->ObjYear);
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //ラジオボタン（1:受験番号順、2:高得点順）
        $opt = array(1, 2);
        $model->field["TAISYOU"] = ($model->field["TAISYOU"] == "") ? "1" : $model->field["TAISYOU"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TAISYOU{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "TAISYOU", $model->field["TAISYOU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //一般入試選択で「基準テストを含める・含めない・のみ」ラジオボタン
        if ($model->field["TESTDIV"] == "3") {
            $arg["isIppan"] = ($model->field["TESTDIV"] == "3") ? 1 : "";
            $opt = array(1, 2, 3);
            $model->field["KIJUN_TEST_DIV"] = ($model->field["KIJUN_TEST_DIV"] == "") ? "1" : $model->field["KIJUN_TEST_DIV"];
            $extra = array();
            foreach($opt as $key => $val) array_push($extra, " id=\"KIJUN_TEST_DIV{$val}\"");
            $radioArray = knjCreateRadio($objForm, "KIJUN_TEST_DIV", $model->field["KIJUN_TEST_DIV"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;
        }

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL322Q");
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl322qForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
