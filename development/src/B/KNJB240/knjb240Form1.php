<?php

require_once('for_php7.php');

class knjb240Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb240Form1", "POST", "knjb240index.php", "", "knjb240Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjb240Query::getSemester();
        $extra = "onchange=\"return btn_submit('knjb240'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //科目コンボ
        $query = knjb240Query::getSubclass($model);
        $extra = "onchange=\"return btn_submit('knjb240'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //出欠集計範囲（月）
        $info = array();
        $opt = array();
        $value_flgS = $value_flgE = false;
        $query = knjb240Query::getSemester();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            for ($i=$row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
                $year = ($i > 12) ? CTRL_YEAR + 1 : CTRL_YEAR;
                $month = sprintf("%02d", (($i > 12) ? $i - 12 : $i));
                $month_show = ($i > 12) ? $i - 12 : $i;

                //開始日
                $sdate = ($i == $row["S_MONTH"]) ? $row["SDATE"] : $year.'-'.$month.'-01';
                //終了日
                $last_day = $db->getOne("VALUES LAST_DAY(DATE('".$year."-".$month."-01'))");
                $edate = ($i == $row["E_MONTH"]) ? $row["EDATE"] : $last_day;

                $opt[] = array('label' => $row["LABEL"].' '.(strlen($month_show) > 1 ? '' : "&nbsp;").$month_show.'月',
                               'value' => $row["VALUE"].'-'.$month);

                if ($model->field["S_MONTH"] == $row["VALUE"].'-'.$month) $value_flgS = true;
                if ($model->field["E_MONTH"] == $row["VALUE"].'-'.$month) $value_flgE = true;

                //月初め、末日情報格納
                $info[$row["VALUE"]][$month] = array("SDATE" => str_replace("-", "/", $sdate),
                                                     "EDATE" => str_replace("-", "/", $edate));
            }
        }
        $result->free();

        //初期値
        if ($model->cmd == "") {
            //開始月
            $semS = $db->getRow(knjb240Query::getSemester(), DB_FETCHMODE_ASSOC);
            $defaultS = $semS["VALUE"].'-'.sprintf("%02d", $semS["S_MONTH"]);

            //終了月
            $semE = $db->getOne(knjb240Query::getSemester(CTRL_DATE));
            list ($y, $m, $d) = explode('-', CTRL_DATE);
            $defaultE = $semE.'-'.sprintf("%02d", $m);
        }

        //開始月コンボ
        $extra = "onchange=\"return btn_submit('change');\"";
        $model->field["S_MONTH"] = ($model->field["S_MONTH"] && $value_flgS) ? $model->field["S_MONTH"] : $defaultS;
        $arg["data"]["S_MONTH"] = knjCreateCombo($objForm, "S_MONTH", $model->field["S_MONTH"], $opt, $extra, "");

        //終了月コンボ
        $extra = "onchange=\"return btn_submit('change');\"";
        $model->field["E_MONTH"] = ($model->field["E_MONTH"] && $value_flgE) ? $model->field["E_MONTH"] : $defaultE;
        $arg["data"]["E_MONTH"] = knjCreateCombo($objForm, "E_MONTH", $model->field["E_MONTH"], $opt, $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $info);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb240Form1.html", $arg);
    }
}
//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    $selectdata = ($model->cmd == "change" && $model->selectdata) ? explode(',', $model->selectdata) : array();

    //講座一覧リスト作成
    $optR = $optL = array();
    $query = knjb240Query::getChairList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $selectdata)) {
            $optL[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        } else {
            $optR[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
    }
    $result->free();
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $optR, $extra, 20);

    //出力対象講座リスト作成
    $extra = "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $optL, $extra, 20);

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
    knjCreateHidden($objForm, "PRGID", "KNJB240");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "selectdata");

    list ($semS, $monS) = explode('-', $model->field["S_MONTH"]);
    knjCreateHidden($objForm, "DATE_FROM", $info[$semS][$monS]["SDATE"]);

    list ($semE, $monE) = explode('-', $model->field["E_MONTH"]);
    knjCreateHidden($objForm, "DATE_TO", $info[$semE][$monE]["EDATE"]);
}
?>
