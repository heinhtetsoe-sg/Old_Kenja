<?php

require_once('for_php7.php');

class knjb233Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb233Form1", "POST", "knjb233index.php", "", "knjb233Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjb233Query::getSemester();
        $extra = "onchange=\"return btn_submit('knjb233'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //月コンボ
        $info = array();
        $opt = array();
        $value_flg = false;
        $query = knjb233Query::getSemester($model->field["SEMESTER"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($i=$row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
                $month = sprintf("%02d", (($i > 12) ? $i - 12 : $i));
                $month_show = ($i > 12) ? $i - 12 : $i;

                $opt[] = array('label' => $month_show.'月',
                               'value' => $month);

                if ($model->field["MONTH"] == $month) $value_flg = true;

                $year = ($i > 12) ? CTRL_YEAR + 1 : CTRL_YEAR;
                //開始日
                $sdate = ($i == $row["S_MONTH"]) ? $row["SDATE"] : $year.'-'.$month.'-01';
                //終了日
                $last_day = $db->getOne("VALUES LAST_DAY(DATE('".$year."-".$month."-01'))");
                $edate = ($i == $row["E_MONTH"]) ? $row["EDATE"] : $last_day;
                //月初め、末日情報格納
                $info[$row["VALUE"]][$month] = array("SDATE" => str_replace("-", "/", $sdate),
                                                     "EDATE" => str_replace("-", "/", $edate));
            }
        }
        $result->free();

        //初期値
        list ($y, $m, $d) = explode('-', CTRL_DATE);
        $default = ($model->cmd == "") ? sprintf("%02d", $m) : $opt[0]["value"];

        $extra = "onchange=\"return btn_submit('knjb233');\"";
        $model->field["MONTH"] = ($model->field["MONTH"] && $value_flg) ? $model->field["MONTH"] : $default;
        $arg["data"]["MONTH"] = knjCreateCombo($objForm, "MONTH", $model->field["MONTH"], $opt, $extra, "");

        //帳票種類ラジオボタン 1:全て 2:職員別
        $opt_form = array(1, 2);
        $model->field["FORM"] = ($model->field["FORM"] == "") ? "1" : $model->field["FORM"];
        $extra = array("id=\"FORM1\" onClick=\"return btn_submit('knjb233');\"", "id=\"FORM2\" onClick=\"return btn_submit('knjb233');\"");
        $radioArray = knjCreateRadio($objForm, "FORM", $model->field["FORM"], $extra, $opt_form, get_count($opt_form));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //帳票種類が職員別のとき、リストToリスト表示
        if ($model->field["FORM"] == "2") {
            $arg["staff"] = 1;
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $info);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb233Form1.html", $arg);
    }
}
//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //職員一覧リスト作成
    $opt = array();
    $query = knjb233Query::getStaffList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 20);

    //出力対象職員リスト作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //対象取消ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象選択ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象取消ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象選択ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $info) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJB233");
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "DATE_FROM", $info[$model->field["SEMESTER"]][$model->field["MONTH"]]["SDATE"]);
    knjCreateHidden($objForm, "DATE_TO",   $info[$model->field["SEMESTER"]][$model->field["MONTH"]]["EDATE"]);
}
?>
