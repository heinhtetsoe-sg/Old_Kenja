<?php

require_once('for_php7.php');

class knjl213fForm1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl213fForm1", "POST", "knjl213findex.php", "", "knjl213fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl213fQuery::getNameMst($model->ObjYear, "L003");
        $extra = " onchange=\"return btn_submit('knjl213f');\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分
        if ($model->field["APPLICANTDIV"] == "1") {
            $arg["data"]["JUNIOR"] = "1";
            $query = knjl213fQuery::getTestDivKotei();
        } else {
            $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
            $query = knjl213fQuery::getNameMst($model->ObjYear, $namecd1);
        }
        $extra = " onchange=\"return btn_submit('knjl213f');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //入試回数(2:高校のみ)
        if ($model->field["APPLICANTDIV"] == "2") {
            $query = knjl213fQuery::getTestdiv0($model->ObjYear, $model->field["TESTDIV"]);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "TESTDIV0", $model->field["TESTDIV0"], $extra, 1, "");
        }

        //重複チェック項目
        $opt = array(1, 2, 3);
        $model->field["CENTER_TITLE"] = ($model->field["CENTER_TITLE"] == "") ? "1" : $model->field["CENTER_TITLE"];
        $extra = array("id=\"CENTER_TITLE1\"", "id=\"CENTER_TITLE2\"", "id=\"CENTER_TITLE3\"");
        $radioArray = knjCreateRadio($objForm, "CENTER_TITLE", $model->field["CENTER_TITLE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷ボタン
        $extra = " onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = " onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL213F");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl213fForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank=""){
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

        if ($row["NAMESPARE2"] && $default_flg){
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
