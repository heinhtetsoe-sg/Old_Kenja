<?php

require_once('for_php7.php');

class knjwexam_fin_search {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjwexam_fin_search", "POST", "knjwexam_fin_searchindex.php", "", "knjwexam_fin_search");
        $db = Query::dbCheckOut();

        $arg["jscript"] = "setF('".$model->frame."');";

        //エンター押下時の移動対象一覧
        $setTextField = array();
        $setTextField[] = "FINSCHOOL_NAME";
        $setTextField[] = "FINSCHOOL_KANA";
        $setTextField[] = "FINSCHOOL_TYPE";
        $setTextField[] = "PREF_CD";
        $setTextField[] = "FINSCHOOL_DISTCD";
        knjCreateHidden($objForm, "setTextField", implode(',', $setTextField));

        //学校名
        $extra = " STYLE=\"ime-mode: active\" onKeyDown=\"changeEnterToTab(this);\"";
        $arg["FINSCHOOL_NAME"] = knjCreateTextBox($objForm, $model->field["FINSCHOOL_NAME"], "FINSCHOOL_NAME", 32, 32, $extra);

        //学校名（かな）
        $extra = " STYLE=\"ime-mode: active\" onKeyDown=\"changeEnterToTab(this);\"";
        $arg["FINSCHOOL_KANA"] = knjCreateTextBox($objForm, $model->field["FINSCHOOL_KANA"], "FINSCHOOL_KANA", 32, 32, $extra);

        //校種
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        $query = knjwexam_fin_searchQuery::getFinschoolType();
        $value = $model->field["FINSCHOOL_TYPE"];
        if ($value == "" && $model->getParameter === '1') {
            $value = $model->setField["setschooltype"];
            if ($model->setField["setSchoolKind"]) {
                $value = $model->setField["setSchoolKind"];
            }
        }
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $extra = " onKeyDown=\"changeEnterToTab(this);\"";
        $arg["FINSCHOOL_TYPE"] = knjCreateCombo($objForm, "FINSCHOOL_TYPE", $value, $opt, $extra, 1);

        //都道府県コンボ
        $query = knjwexam_fin_searchQuery::getPref();
        $extra = " onKeyDown=\"changeEnterToTab(this);\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PREF_CD"], "PREF_CD", $extra, 1, "BLANK");

        //地区コード
        $query = knjwexam_fin_searchQuery::getNameMst("L001");
        $extra = " onKeyDown=\"changeEnterToTab(this);\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["FINSCHOOL_DISTCD"], "FINSCHOOL_DISTCD", $extra, 1, "BLANK");
        
        //学校立
        $query = knjwexam_fin_searchQuery::getNameMst("L015");
        $extra = " onKeyDown=\"changeEnterToTab(this);\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["FINSCHOOL_DIV"], "FINSCHOOL_DIV", $extra, 1, "BLANK");
        

        //リスト
        if ($model->getParameter === '2') {
            $extra = "ondblclick=\"apply_finschoolgetParametr2(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        } else if ($model->getParameter === '1') {
            $extra = "ondblclick=\"apply_finschoolgetParametr(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        } else {
            $extra = "ondblclick=\"apply_finschool(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        }
        if ($model->cmd == "search") {
            $query = knjwexam_fin_searchQuery::getSchoolList($model);
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
        knjCreateHidden($objForm, "entexamyear", $model->entexamyear);
        knjCreateHidden($objForm, "fscdname", $model->setField["fscdname"]);
        knjCreateHidden($objForm, "fsname", $model->setField["fsname"]);
        knjCreateHidden($objForm, "fsChikuName", $model->setField["fsChikuName"]);
        knjCreateHidden($objForm, "fsRitsuNameId", $model->setField["fsRitsuNameId"]);
        knjCreateHidden($objForm, "fsaddr", $model->setField["fsaddr"]);
        knjCreateHidden($objForm, "school_div", $model->setField["school_div"]);
        knjCreateHidden($objForm, "fszip", $model->setField["fszip"]);
        knjCreateHidden($objForm, "fsaddr1", $model->setField["fsaddr1"]);
        knjCreateHidden($objForm, "fsaddr2", $model->setField["fsaddr2"]);
        knjCreateHidden($objForm, "l015", $model->setField["l015"]);
        knjCreateHidden($objForm, "tell", $model->setField["tell"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjwexam_fin_search.html", $arg);
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
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
