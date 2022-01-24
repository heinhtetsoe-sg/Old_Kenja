<?php

require_once('for_php7.php');

class knje366aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knje366aForm1", "POST", "knje366aindex.php", "", "knje366aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        //年度一覧コンボ
        $query = knje366aquery::getYearSeme();
        $extra = "onchange=\"btn_submit('');\"";
        $model->field["YEAR"] = $model->field["YEAR"] ? $model->field["YEAR"] : CTRL_YEAR;
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "");

        //大学コンボ
        $query = knje366aquery::getCollege();
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_GROUP"], "SCHOOL_GROUP", $extra, 1, "");

        //特定学部
        $query = knje366aquery::getScd();
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["S_CD"], "S_CD", $extra, 1, "");

        //出力（1:大学別　2:学部、学科別）
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //ＣＳＶ出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje366aForm1.html", $arg); 
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
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
