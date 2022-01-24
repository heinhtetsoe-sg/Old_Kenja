<?php

require_once('for_php7.php');

class knjl334fForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl334fForm1", "POST", "knjl334findex.php", "", "knjl334fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl334f');\"";
        $query = knjl334fQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $extra = " onchange=\"return btn_submit('knjl334f');\"";
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjl334fQuery::getNameMst($model->ObjYear, $namecd1);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        if ($model->field["APPLICANTDIV"] == "2") {
            //入試回数コンボボックス
            $query = knjl334fQuery::getTestdiv0($model->ObjYear, $model->field["TESTDIV"]);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV0"], "TESTDIV0", $extra, 1);

            //志望区分コンボ
            $query = knjl334fQuery::getExamcourse($model->ObjYear, $model->field["APPLICANTDIV"]);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->field["EXAMCOURSE"], "EXAMCOURSE", $extra, 1, "ALL");
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
        knjCreateHidden($objForm, "PRGID", "KNJL334F");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl334fForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK")  $opt[] = array("label" => "", "value" => "");
    if ($blank == "ALL")    $opt[] = array("label" => "－ 全て －", "value" => "ALL");
    $value_flg = false;
    $i = $default = ($blank == "ALL") ? 1 : 0;
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
    $value = ($value && $value_flg) ? $value : (($blank == "ALL") ? $opt[0]["value"] : $opt[$default]["value"]);

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
