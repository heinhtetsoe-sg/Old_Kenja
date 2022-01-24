<?php

require_once('for_php7.php');

class knjxjoboffer_search {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjxjoboffer_search", "POST", "knjxjoboffer_searchindex.php", "", "knjxjoboffer_search");
        $db = Query::dbCheckOut();

        $arg["jscript"] = "setF('".$model->frame."');";

        //会社名
        $arg["COMPANY_NAME"] = knjCreateTextBox($objForm, $model->field["COMPANY_NAME"], "COMPANY_NAME", 32, 32, $extra);

        //会社名かな
        $arg["COMPANY_NAMEKANA"] = knjCreateTextBox($objForm, $model->field["COMPANY_NAMEKANA"], "COMPANY_NAMEKANA", 32, 32, $extra);

        //住所
        $arg["COMPANY_ADDR1"] = knjCreateTextBox($objForm, $model->field["COMPANY_ADDR1"], "COMPANY_ADDR1", 32, 32, $extra);

        //リスト
        $extra = "ondblclick=\"apply_joboffer(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        if ($model->cmd == "search") {
            $query = knjxjoboffer_searchQuery::getJobOfferList($model);
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

            $arg["JOBOFFER_LIST"] = knjCreateCombo($objForm, "JOBOFFER_LIST", $value, $opt, $extra, 10);
        } else {
            $arg["JOBOFFER_LIST"] = knjCreateCombo($objForm, "JOBOFFER_LIST", "", array(), $extra, 10);
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
        View::toHTML($model, "knjxjoboffer_search.html", $arg);
    }
}
?>
