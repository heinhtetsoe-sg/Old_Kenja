<?php

require_once('for_php7.php');

class knjl324pForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl324pForm1", "POST", "knjl324pindex.php", "", "knjl324pForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $query = knjl324pQuery::getNameMst($model->ObjYear, "L003");
        $extra = " onchange=\"return btn_submit('knjl324p');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjl324pQuery::getNameMst($model->ObjYear, $namecd1);
        $extra = " onchange=\"return btn_submit('knjl324p');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //塾一覧リスト作成する
        makeListToList($objForm, $arg, $db, $model);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL324P");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl324pForm1.html", $arg); 
    }
}
//塾一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //塾リスト
    $row1 = array();
    $result = $db->query(knjl324pQuery::selectPriSchoolQuery($model));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //塾リスト作成
    $extra = "multiple style=\"width:220px\" width:\"220px\" ondblclick=\"move('left','SCHOOL_SELECTED','SCHOOL_NAME',1)\"";
    $arg["data"]["SCHOOL_NAME"] = knjCreateCombo($objForm, "SCHOOL_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:220px\" width:\"220px\" ondblclick=\"move('right','SCHOOL_SELECTED','SCHOOL_NAME',1)\"";
    $arg["data"]["SCHOOL_SELECTED"] = knjCreateCombo($objForm, "SCHOOL_SELECTED", "", array(), $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('sel_add_all','SCHOOL_SELECTED','SCHOOL_NAME',1);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('left','SCHOOL_SELECTED','SCHOOL_NAME',1);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('right','SCHOOL_SELECTED','SCHOOL_NAME',1);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move('sel_del_all','SCHOOL_SELECTED','SCHOOL_NAME',1);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
