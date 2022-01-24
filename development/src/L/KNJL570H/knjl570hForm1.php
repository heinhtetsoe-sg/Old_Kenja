<?php

require_once('for_php7.php');

class knjl570hForm1 {

    function main(&$model) {

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        /******************/
        /* コンボボックス */
        /******************/

        //対象年度コンボボックス
        $opt_year   = array();
        $opt_year[] = array("label" => (CTRL_YEAR),     "value" => CTRL_YEAR);
        $opt_year[] = array("label" => (CTRL_YEAR + 1), "value" => (CTRL_YEAR + 1));
        $extra = "onChange=\"return btn_submit('main');\"";
        $model->year = ($model->year == "") ? substr(CTRL_DATE, 0, 4): $model->year;
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->year, $opt_year, $extra, 1);

        //学校種別
        $query = knjl570hQuery::getNameMst($model->year, "L003");
        $extra = " onChange=\"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //入試判別
        $query = knjl570hQuery::getDistinctId($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "DISTINCT_ID", $model->field["DISTINCT_ID"], $extra, 1, "");

        /********************/
        /* チェックボックス */
        /********************/
        //ヘッダ有
        $extra = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        /****************/
        /* ラジオボタン */
        /****************/
        //hidden -- 4:データ出力
        knjCreateHidden($objForm, "OUTPUT", "4");

        /**********/
        /* ボタン */
        /**********/
        //実行
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "実 行", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl570hindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl570hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
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
