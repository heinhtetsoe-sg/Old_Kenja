<?php

require_once('for_php7.php');


class knjwschool_search
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjwschool_search", "POST", "knjwschool_searchindex.php", "", "knjwschool_search");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["jscript"] = "setF('".$model->frame."');";

        //校種
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        $query = knjwschool_searchQuery::getFinschoolType();
        $value = $model->search["FINSCHOOL_TYPE"];
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $extra = "";
        $arg["FINSCHOOL_TYPE"] = knjCreateCombo($objForm, "FINSCHOOL_TYPE", $model->search["FINSCHOOL_TYPE"], $opt, $extra, 1);

        //学校名
        $arg["NAME"] = knjcreateTextBox($objForm, $model->search["NAME"], "NAME", 32, 32, $extra);

        //住所
        $arg["ADDR"] = knjcreateTextBox($objForm, $model->search["ADDR"], "ADDR", 32, 32, $extra);

        //市町村
        $arg["ZIPCD"] = knjcreateTextBox($objForm, $model->search["ZIPCD"], "ZIPCD", 8, 8, $extra);

        //リスト
        $extra = "ondblclick=\"apply_school(this, '{$model->targetname}');\" STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        if ($model->NAME || $model->ADDR || $model->ZIPCD || $model->search["FINSCHOOL_TYPE"]) {
            $query = knjwschool_searchQuery::getSchoolList($model);
        } else {
            $query = '';
        }
        makeCombo($objForm, $arg, $db, $query, "1", "SCHOOL_LIST", $extra, 10);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjwschool_search.html", $arg); 
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, $value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    if ($query) {
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array ("label" => $row["LABEL"],
                            "value" => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
    }

    $arg[$name] = knjcreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //検索
    $extra = "onclick=\"return btn_submit('search')\"";
    $arg["button"]["search"] = knjcreateBtn($objForm, "search", "検 索", $extra);

    //終了
    $extra = "onClick=\"top.main_frame.right_frame.closeit();\"";
    $extra = "onClick=\"parent.closeit();\"";
    $arg["button"]["btn_end"] = knjcreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjcreateHidden($objForm, "cmd");
    knjcreateHidden($objForm, "targetname", $model->targetname);
}

?>
