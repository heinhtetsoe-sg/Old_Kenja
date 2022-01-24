<?php

require_once('for_php7.php');

class knjwfin_search {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjwfin_search", "POST", "knjwfin_searchindex.php", "", "knjwfin_search");
        $db = Query::dbCheckOut();

        $arg["jscript"] = "setF('".$model->frame."');";

        //学校名
        $arg["FINSCHOOL_NAME"] = knjCreateTextBox($objForm, $model->field["FINSCHOOL_NAME"], "FINSCHOOL_NAME", 32, 32, $extra);

        //都道府県
        $arg["FINSCHOOL_KANA"] = knjCreateTextBox($objForm, $model->field["FINSCHOOL_KANA"], "FINSCHOOL_KANA", 32, 32, $extra);

        //校種
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        $query = knjwfin_searchQuery::getFinschoolType();
        $value = $model->field["FINSCHOOL_TYPE"];
        if ($value == "" && $model->getParameter === '1') {
            $value = $model->setField["setschooltype"];
        }
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $extra = "";
        $arg["FINSCHOOL_TYPE"] = knjCreateCombo($objForm, "FINSCHOOL_TYPE", $value, $opt, $extra, 1);

        //リスト
        if ($model->getParameter === '1') {
            $extra = "ondblclick=\"apply_finschoolgetParametr(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        } else {
            $extra = "ondblclick=\"apply_finschool(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        }
        if ($model->cmd == "search") {
            $query = knjwfin_searchQuery::getSchoolList($model);
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

            $arg["SCHOOL_LIST"] = knjCreateCombo($objForm, "SCHOOL_LIST", $value, $opt, $extra, 10);
        } else {
            $arg["SCHOOL_LIST"] = knjCreateCombo($objForm, "SCHOOL_LIST", "", array(), $extra, 10);
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
        knjCreateHidden($objForm, "fscdname", $model->setField["fscdname"]);
        knjCreateHidden($objForm, "fsname", $model->setField["fsname"]);
        knjCreateHidden($objForm, "fsChikuName", $model->setField["fsChikuName"]);
        knjCreateHidden($objForm, "fsRitsuNameId", $model->setField["fsRitsuNameId"]);
        knjCreateHidden($objForm, "fsaddr", $model->setField["fsaddr"]);
        knjCreateHidden($objForm, "school_div", $model->setField["school_div"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjwfin_search.html", $arg);
    }
}
?>
