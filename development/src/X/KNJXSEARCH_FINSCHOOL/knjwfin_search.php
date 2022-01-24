<?php

require_once('for_php7.php');

class knjwfin_search
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("knjwfin_search", "POST", "knjwfin_searchindex.php", "", "knjwfin_search");
        // Add by PP for Title 2020-02-03 start
        $arg["TITLE"]   = "学校検索画面";
        echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        // Add by PP for Title 2020-02-20 end
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
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = " STYLE=\"ime-mode: active\" onKeyDown=\"changeEnterToTab(this);\" aria-label=\"学校名\"";
        // Add by PP for PC-Talker 2020-02-28 end
        $arg["FINSCHOOL_NAME"] = knjCreateTextBox($objForm, $model->field["FINSCHOOL_NAME"], "FINSCHOOL_NAME", 32, 32, $extra);

        //学校名（かな）
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = " STYLE=\"ime-mode: active\" onKeyDown=\"changeEnterToTab(this);\" aria-label=\"学校名（かな）\"";
        // Add by PP for PC-Talker 2020-02-28 end
        $arg["FINSCHOOL_KANA"] = knjCreateTextBox($objForm, $model->field["FINSCHOOL_KANA"], "FINSCHOOL_KANA", 32, 32, $extra);

        //校種
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        $query = knjwfin_searchQuery::getFinschoolType();
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
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = " onKeyDown=\"current_cursor('FINSCHOOL_TYPE'); changeEnterToTab(this);\" aria-label=\"校種\" id=\"FINSCHOOL_TYPE\"";
        // Add by PP for PC-Talker 2020-02-28 end
        $arg["FINSCHOOL_TYPE"] = knjCreateCombo($objForm, "FINSCHOOL_TYPE", $value, $opt, $extra, 1);

        //都道府県コンボ
        // Add by PP for PC-Talker 2020-02-03 start
        $query = knjwfin_searchQuery::getPref();
        $extra = " onKeyDown=\"current_cursor('PREF_CD'); changeEnterToTab(this);\" aria-label=\"都道府県\" id=\"PREF_CD\"";
        // Add by PP for PC-Talker 2020-02-28 end
        makeCmb($objForm, $arg, $db, $query, $model->field["PREF_CD"], "PREF_CD", $extra, 1, "BLANK");

        //地区コード
        // Add by PP for PC-Talker 2020-02-03 start
        $query = knjwfin_searchQuery::getNameMst("L001");
        $extra = " onKeyDown=\"current_cursor('FINSCHOOL_DISTCD'); changeEnterToTab(this);\" aria-label=\"地区コード\" id=\"FINSCHOOL_DISTCD\"";
        // Add by PP for PC-Talker 2020-02-28 end
        makeCmb($objForm, $arg, $db, $query, $model->field["FINSCHOOL_DISTCD"], "FINSCHOOL_DISTCD", $extra, 1, "BLANK");
        
        //学校立
        $query = knjwfin_searchQuery::getNameMst("L015");
        $extra = " onKeyDown=\"current_cursor('FINSCHOOL_DIV'); changeEnterToTab(this);\" aria-label=\"学校立\" id=\"FINSCHOOL_DIV\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["FINSCHOOL_DIV"], "FINSCHOOL_DIV", $extra, 1, "BLANK");
        

        //リスト
        // Add by PP for PC-Talker 2020-02-03 start
        if ($model->getParameter === '2') {
            $extra = " id=\"SCHOOL_LIST\" ondblclick=\"parent.current_cursor_list(); apply_finschoolgetParametr2(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        } elseif ($model->getParameter === '1') {
            $extra = " id=\"SCHOOL_LIST\" ondblclick=\"parent.current_cursor_list(); apply_finschoolgetParametr(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        } elseif ($model->getParameter === '3') {
            $extra = " id=\"SCHOOL_LIST\" ondblclick=\"parent.current_cursor_list(); apply_finschoolgetParametr3(this); execAfterEvent();\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        } else {
            $extra = " id=\"SCHOOL_LIST\" ondblclick=\"parent.current_cursor_list(); apply_finschool(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        }
        // for data empty
        $counter = 0;
        // Add by PP for PC-Talker 2020-02-28 end
        if ($model->cmd == "search") {
            $query = knjwfin_searchQuery::getSchoolList($model);
            $opt = array();
            $value = "1";
            $value_flg = false;
            $cnt = 1;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                // Add by PP for PC-Talker 2020-02-03 start
                if ($counter == 0) {
                    $opt[] = array("label" => $row["LABEL"],
                                   "value" => " ");
                } else {
                    $opt[] = array("label" => $row["LABEL"],
                                   "value" => $row["VALUE"]);
                }
                // Add by PP for PC-Talker 2020-02-28 end

                if ($value == $row["VALUE"]) {
                    $value_flg = true;
                }
                
                // Add by PP for PC-Talker 2020-02-03 start
                $counter++;
                // Add by PP for PC-Talker 2020-02-28 end
            }
            $result->free();

            $arg["SCHOOL_LIST"] = knjCreateCombo($objForm, "SCHOOL_LIST", $value, $opt, $extra, 10);
        } else {
            $arg["SCHOOL_LIST"] = knjCreateCombo($objForm, "SCHOOL_LIST", "", array(), $extra, 10);
        }
        // Add by PP for PC-Talker 2020-02-03 start
        $empty = ($counter > 0)? "検索結果のデータリスト": "検索結果のデータがありません";
        $arg["SEARCH_DATA"] = $empty;
        // Add by PP for PC-Talker 2020-02-28 end

        /**********/
        /* ボタン */
        /**********/
        //検索
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "id=\"search\" onclick=\"current_cursor('search'); return btn_submit('search')\" aria-label=\"検索\"";
        // Add by PP for PC-Talker 2020-02-28 end
        $arg["button"]["search"] = knjCreateBtn($objForm, "search", "検 索", $extra);
        //戻る
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "onClick=\"parent.closeit();parent.current_cursor_focus();\" aria-label=\"戻る\"";
        // Add by PP for PC-Talker 2020-02-28 end
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
        knjCreateHidden($objForm, "fszip", $model->setField["fszip"]);
        knjCreateHidden($objForm, "fsaddr1", $model->setField["fsaddr1"]);
        knjCreateHidden($objForm, "fsaddr2", $model->setField["fsaddr2"]);
        knjCreateHidden($objForm, "l015", $model->setField["l015"]);
        knjCreateHidden($objForm, "tell", $model->setField["tell"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjwfin_search.html", $arg);
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
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
