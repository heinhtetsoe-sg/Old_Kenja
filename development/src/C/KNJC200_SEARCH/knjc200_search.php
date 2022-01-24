<?php

require_once('for_php7.php');

class knjc200_search {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjc200_search", "POST", "knjc200_searchindex.php", "", "knjc200_search");
        $db = Query::dbCheckOut();

        //氏名・氏名かな
        $extra = "";
        $arg["NAME"] = knjCreateTextBox($objForm, $model->field["NAME"], "NAME", 40, 40, $extra);
        $arg["NAME_KANA"] = knjCreateTextBox($objForm, $model->field["NAME_KANA"], "NAME_KANA", 40, 40, $extra);

        //年組
        $extra = "style=\"width:150px;\"";
        $query = knjc200_searchQuery::getHrName($model);
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        $value = $model->field["GRADE_HR_CLASS"];
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array ('label' => $row["LABEL"],
                            "value" => $row["VALUE"]);

            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $result->free();

        $arg["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $value, $opt, $extra, 1);

        //氏名リスト
        if ($model->cmd == "search") {
            $i = 0;
            $jumping2 = REQUESTROOT."/C/KNJC200_2/knjc200_2index.php";
            $query = knjc200_searchQuery::getList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                 array_walk($row, "htmlspecialchars_array");
                 $row["URL"]      = $jumping2;
                 $row["HR_NAME"]  = $row["HR_NAME"]." - ".$row["ATTENDNO"]."番";
                 $arg["data"][]   = $row;
                 $i++;
            }
            $arg["RESULT"] = "結果　".$i."名";
            $result->free();
            if ($i == 0) {
                $arg["search_result"] = "SearchResult();";
            }
        }


        /**********/
        /* ボタン */
        /**********/
        //検索
        $extra = "onclick=\"return btn_submit('search')\"";
        $arg["button"]["search"] = knjCreateBtn($objForm, "search", "検 索", $extra);
        //戻る
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc200_search.html", $arg);
    }
}
?>
