<?php

require_once('for_php7.php');

class knjl401q_search {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjl401q_search", "POST", "knjl401qindex.php", "", "knjl401q_search");
        $db = Query::dbCheckOut();

        //受験者名
        $extra = " STYLE=\"ime-mode: active\"";
        $arg["SEARCH_NAME"] = knjCreateTextBox($objForm, $model->field["SEARCH_NAME"], "SEARCH_NAME", 32, 32, $extra);

        /**********/
        /* ボタン */
        /**********/
        //検索
        $extra = "onclick=\"return btn_submit('name_search_search')\"";
        $arg["button"]["search"] = knjCreateBtn($objForm, "search", "検 索", $extra);
        //戻る
        $extra = "onClick=\"parent.closeit();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //リスト
        $extra = "ondblclick=\"apply_examno(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        if ($model->cmd == "name_search_search") {
            $query = knjl401qQuery::getSearchList($model);
            $opt = array();
            $value = "1";
            $value_flg = false;
            $cnt = 1;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row["LABEL"] = str_replace("@@@@@@@", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", $row["LABEL"]);
                $opt[] = array ("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);

                if ($value == $row["VALUE"]) $value_flg = true;
            }
            $result->free();

            $arg["SEARCH_LIST"] = knjCreateCombo($objForm, "SEARCH_LIST", $value, $opt, $extra, 10);
        } else {
            $arg["SEARCH_LIST"] = knjCreateCombo($objForm, "SEARCH_LIST", "", array(), $extra, 10);
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl401q_search.html", $arg);
    }
}

?>
