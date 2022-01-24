<?php

require_once('for_php7.php');

class knjxindustrys_search {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjxindustrys_search", "POST", "knjxindustrys_searchindex.php", "", "knjxindustrys_search");
        $db = Query::dbCheckOut();

        $arg["jscript"] = "setF('".$model->frame."');";

        //大分類コンボ
        $query = knjxindustrys_searchQuery::getIndustryL($model);
        $extra = " onchange=\"return btn_submit('knjxindustrys_search');\"";
        makeCmb($objForm, $arg, $db, $query, "INDUSTRY_LCD", $model->field["INDUSTRY_LCD"], $extra, 1, "BLANK");

        //中分類コンボ
        $query = knjxindustrys_searchQuery::getIndustryM($model);
        $extra = " onchange=\"return btn_submit('knjxindustrys_search');\"";
        makeCmb($objForm, $arg, $db, $query, "INDUSTRY_MCD", $model->field["INDUSTRY_MCD"], $extra, 1, "BLANK");

        //産業種別（小）名称
        $arg["INDUSTRY_SNAME"] = knjCreateTextBox($objForm, $model->field["INDUSTRY_SNAME"], "INDUSTRY_SNAME", 32, 32, $extra);

        //リスト
        $extra = "ondblclick=\"apply_industrys(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        $query = knjxindustrys_searchQuery::getIndustrysList($model);
        if ($model->cmd == "search") {
            $opt = array();
            $value = "1";
            $value_flg = false;
            $cnt = 1;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array ("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);

                if ($value == $row["VALUE"]) $value_flg = true;
            }
            $result->free();

            $arg["INDUSTRY_LIST"] = knjCreateCombo($objForm, "INDUSTRY_LIST", $value, $opt, $extra, 10);
        } else {
            makeCmb($objForm, $arg, $db, $query, "INDUSTRY_LIST", $model->field["INDUSTRY_LIST"], $extra, 10, "");
        }

        /**********/
        /* ボタン */
        /**********/
        //検索
        $extra = "onclick=\"return btn_submit('search')\"";
        $arg["button"]["search"] = knjCreateBtn($objForm, "search", "検 索", $extra);
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
        View::toHTML($model, "knjxindustrys_search.html", $arg);
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
