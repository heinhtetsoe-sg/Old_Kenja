<?php

require_once('for_php7.php');

class knjl060fForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl060fForm1", "POST", "knjl060findex.php", "", "knjl060fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->year;
        
        //入試制度コンボ
        $query = knjl060fQuery::getNameMst($model->year, "L003");
        $extra = "onChange=\"return btn_submit('knjl060f')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //入試区分
        $query = knjl060fQuery::getNameMst($model->year, ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004");
        $extra = "onChange=\"return btn_submit('knjl060f')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "");

        //入試回数(2:高校のみ)
        $arg["showTestdiv0"] = ($model->field["APPLICANTDIV"] == "2") ? "1" : "";
        $query = knjl060fQuery::getTestdiv0($model->year, $model->field["TESTDIV"]);
        $extra = ($model->field["APPLICANTDIV"] == "1") ? "disabled" : "";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV0"], "TESTDIV0", $extra, 1, "");

        //特別措置者(インフルエンザ)
        $extra = "id=\"SPECIAL_REASON_DIV\" ";
        $extra .= strlen($model->special_reason_div) ? "checked='checked' " : "";
        $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //実行ボタン
        $extra = " onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "実 行", $extra);

        //終了ボタン
        $extra = " onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl060fForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') {
            $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
