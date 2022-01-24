<?php

require_once('for_php7.php');

class knjxjobtypes_search {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjxjobtypes_search", "POST", "knjxjobtypes_searchindex.php", "", "knjxjobtypes_search");
        $db = Query::dbCheckOut();

        $arg["jscript"] = "setF('".$model->frame."');";

        //大分類コンボ
        $query = knjxjobtypes_searchQuery::getJobtypeL($model);
        $extra = " onchange=\"return btn_submit('knjxjobtypes_search');\"";
        makeCmb($objForm, $arg, $db, $query, "JOBTYPE_LCD", $model->field["JOBTYPE_LCD"], $extra, 1, "BLANK");

        //中分類コンボ
        $query = knjxjobtypes_searchQuery::getJobtypeM($model);
        $extra = " onchange=\"return btn_submit('knjxjobtypes_search');\"";
        makeCmb($objForm, $arg, $db, $query, "JOBTYPE_MCD", $model->field["JOBTYPE_MCD"], $extra, 1, "BLANK");

        //小分類コンボ(リスト)
        $query = knjxjobtypes_searchQuery::getJobtypeS($model);
        $extra = " ondblclick=\"apply_jobtypes(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        makeCmb($objForm, $arg, $db, $query, "JOBTYPE_SCD", $model->field["JOBTYPE_SCD"], $extra, 10, "");

        /**********/
        /* ボタン */
        /**********/
        //戻る
        $extra = "onClick=\"parent.closeit();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjxjobtypes_search.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
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
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
