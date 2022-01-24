<?php

require_once('for_php7.php');

class knjwpri_search {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjwpri_search", "POST", "knjwpri_searchindex.php", "", "knjwpri_search");
        $db = Query::dbCheckOut();

        $arg["jscript"] = "setF('".$model->frame."');";

        //学校名
        $extra = " STYLE=\"ime-mode: active\"";
        $arg["PRISCHOOL_NAME"] = knjCreateTextBox($objForm, $model->field["PRISCHOOL_NAME"], "PRISCHOOL_NAME", 32, 32, $extra);

        //学校名
        $extra = " STYLE=\"ime-mode: active\"";
        $arg["PRISCHOOL_CLASS_NAME"] = knjCreateTextBox($objForm, $model->field["PRISCHOOL_CLASS_NAME"], "PRISCHOOL_CLASS_NAME", 32, 32, $extra);

        //リスト
        if ($model->getParameter === '1') {
            $extra = "ondblclick=\"apply_prischoolGetParameter1(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        } else {
            $extra = "ondblclick=\"apply_prischool(this);\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        }
        if ($model->cmd == "search") {
            $query = knjwpri_searchQuery::getPriSchoolList($model);
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

            $arg["PRISCHOOL_LIST"] = knjCreateCombo($objForm, "PRISCHOOL_LIST", $value, $opt, $extra, 10);
        } else {
            $arg["PRISCHOOL_LIST"] = knjCreateCombo($objForm, "PRISCHOOL_LIST", "", array(), $extra, 10);
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
        knjCreateHidden($objForm, "prischool_cd", $model->setField["PRISCHOOL_CD"]);
        knjCreateHidden($objForm, "prischool_name", $model->setField["PRISCHOOL_NAME"]);
        knjCreateHidden($objForm, "prischool_class_cd", $model->setField["PRISCHOOL_CLASS_CD"]);
        knjCreateHidden($objForm, "prischool_class_name", $model->setField["PRISCHOOL_CLASS_NAME"]);
        knjCreateHidden($objForm, "submitFlg", $model->submitFlg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjwpri_search.html", $arg);
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
