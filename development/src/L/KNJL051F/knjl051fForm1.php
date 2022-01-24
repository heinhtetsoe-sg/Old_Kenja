<?php

require_once('for_php7.php');

class knjl051fForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl051fForm1", "POST", "knjl051findex.php", "", "knjl051fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl051f');\"";
        $query = knjl051fQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        if ($model->field["APPLICANTDIV"] == "1") {
            $extra = "";
            $query = knjl051fQuery::getTestdivL024($model);
        } else {
            $extra = " onchange=\"return btn_submit('knjl051f');\"";
            $query = knjl051fQuery::getNameMst($model->ObjYear, "L004");
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        if ($model->field["APPLICANTDIV"] == "2") {
            //入試回数コンボボックス
            $query = knjl051fQuery::getTestdiv0($model->ObjYear, $model->field["TESTDIV"]);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV0"], "TESTDIV0", $extra, 1);

            //志望区分コンボボックス
            $query = knjl051fQuery::getExamcourse($model->ObjYear, $model->field["APPLICANTDIV"]);
            $extra = " onchange=\"return btn_submit('knjl051f');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["EXAMCOURSE"], "EXAMCOURSE", $extra, 1);
        }

        if ($model->field["APPLICANTDIV"] == "1") {
            //受験型コンボボックス
            $query = knjl051fQuery::getExamType($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->field["EXAM_TYPE"], "EXAM_TYPE", $extra, 1);
        }

        //テンプレート印刷チェックボックス
        $extra  = ($model->field["TEMP_PRINT"] == "1") ? "checked" : "";
        $extra .= " id=\"TEMP_PRINT\"";
        $arg["data"]["TEMP_PRINT"] = knjCreateCheckBox($objForm, "TEMP_PRINT", "1", $extra, "");

        //特別措置者(インフルエンザ)
        $extra = "id=\"SPECIAL_REASON_DIV\" ";
        $extra .= strlen($model->field["SPECIAL_REASON_DIV"]) ? "checked='checked' " : "";
        $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //印刷ボタン
        $extra = " onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = " onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL051F");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl051fForm1.html", $arg); 
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
