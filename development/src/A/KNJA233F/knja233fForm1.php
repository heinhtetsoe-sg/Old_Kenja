<?php

require_once('for_php7.php');

class knja233fForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja233fForm1", "POST", "knja233findex.php", "", "knja233fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->year;

        /**********/
        /* コンボ */
        /**********/
        //学期コンボボックスを作成する
        $query = knja233fQuery::getSemester($model);
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onchange=\"return btn_submit('gakki');\"", 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }

        //学年コンボ
        $query = knja233fQuery::getClassMst($model);
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], "onChange=\"return btn_submit('knja233f');\"", 1);

        //テスト種別コンボ
        $query = knja233fQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], "onChange=\"return btn_submit('knja233f');\"", 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //カレンダーコントロール
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : $model->control["学籍処理日"];
        $arg["el"]["DATE"] = View::popUpCalendar2($objForm, "DATE", $value, "reload=true", " btn_submit('knja233f')", "");

        /**********/
        /* ボタン */
        /**********/
        //プレビュー
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //ＣＳＶ出力
        $btnName = "ＣＳＶ出力";
        if ($model->Properties["useXLS"]) {
            $extra = "onclick=\"return newwin2('" . SERVLET_URL . "', '" . $model->schoolCode . "', '" . $model->Properties["xlsVer"] . "');\"";
            $btnName = "エクセル出力";
        } else {
            $extra = "onclick=\"return csv_submit('csv');\"";
        }
        //セキュリティーチェック
        $securityCnt = $db->getOne(knja233fQuery::getSecurityHigh());
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $btnName, $extra);
        }

        //転学・退学した生徒は出力しない
        $extra = ($model->field["GRD_DIV"] == "1" || $model->cmd == '') ? "checked" : "";
        $extra .= " id=\"GRD_DIV\" onclick =\"kubun();\"";
        $arg["data"]["GRD_DIV"] = knjCreateCheckBox($objForm, "GRD_DIV", "1", $extra, "");

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "ATTENDCLASSCD");
        knjCreateHidden($objForm, "GROUPCD");
        knjCreateHidden($objForm, "NAME_SHOW");
        knjCreateHidden($objForm, "CHARGEDIV");
        knjCreateHidden($objForm, "APPDATE");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja233fForm1.html", $arg); 
    }
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

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $opt = array();
    $result = $db->query(knja233fQuery::getChairDat($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $chargediv_mark = ($row["CHARGEDIV"] == '1') ? '＊' : '　';
        $start = str_replace("-","/",$row["APPDATE"]);
        $end   = str_replace("-","/",$row["APPENDDATE"]);

        $opt[]= array('label' => "{$row["ATTENDCLASSCD"]}:{$row["CLASSALIAS"]} {$start}～{$end} {$row["STAFFNAME_SHOW"]} {$chargediv_mark} {$row["GROUPCD"]}",
                      'value' => "{$row["SUBCLASSCD"]},{$row["GRADE_HR_CLASS"]},{$row["STAFFCD"]},{$row["APPDATE"]},{$row["ATTENDCLASSCD"]},{$row["GROUPCD"]},{$row["CHARGEDIV"]},{$row["APPDATE"]}"
                     );
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:400px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $opt, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:400px;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
?>
