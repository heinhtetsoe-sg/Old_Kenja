<?php

require_once('for_php7.php');

class knjb1256Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb1256Form1", "POST", "knjb1256index.php", "", "knjb1256Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->exeYear;

        //年度コンボ
        $query = knjb1256Query::getYear($model);
        $extra = "onchange=\"return btn_submit('subMain')\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1);

        //履修履歴コンボ
        $query = knjb1256Query::getRirekiCode($model);
        $extra = "onChange=\"return btn_submit('subMain');\"";
        makeCmb($objForm, $arg, $db, $query, "RIREKI_CODE", $model->field["RIREKI_CODE"], $extra, 1);

        //履修履歴コンボ(コピー元)
        $setRirekiCode = "";
        $conma = "";
        $query = knjb1256Query::getNotNullRirekiCode($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setRirekiCode .= $conma.$row["RIREKI_CODE"];
            $conma = ',';
        }
        $result->free();
        $query = knjb1256Query::getRirekiCode($model, "copy", $setRirekiCode);
        $extra = "onChange=\"return btn_submit('subMain');\"";
        makeCmb($objForm, $arg, $db, $query, "RIREKI_CODE_COPY", $model->field["RIREKI_CODE_COPY"], $extra, 1);

        //コースコンボ
        $query = knjb1256Query::getCourseCode($model);
        $extra = "onchange=\"return btn_submit('subMain')\"";
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $model->field["COURSECODE"], $extra, 1, "BLANK");

        //パターンコンボ
        $query = knjb1256Query::getPattern($model);
        $extra = "onchange=\"return btn_submit('subMain')\"";
        makeCmb($objForm, $arg, $db, $query, "PATTERN_CD", $model->field["PATTERN_CD"], $extra, 1, "BLANK");

        //組コンボ
        $query = knjb1256Query::getHrClass($model);
        $extra = "onchange=\"return btn_submit('changeHr')\"";
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1, "BLANK");

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onClick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //コピー
        $extra = "onClick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "をコピー", $extra);
        //終了
        $extra = "onClick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJB1256");
        knjCreateHidden($objForm, "selectStd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb1256Form1.html", $arg);
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array('label' => "全　て",
                       'value' => "");
    }
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
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
    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $optLeft = array();
    $leftSch = array();
    if ($model->cmd == "changeHr" && get_count($model->selectStd) > 0) {
        foreach ($model->selectStd as $val) {                      //リストの左側を作る
            $query = knjb1256Query::getSchInfo($model, $val);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $optLeft[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            $leftSch[$row["VALUE"]] = $row["VALUE"];
        }
    } else {
        $query = knjb1256Query::getCompCreditsPatternStdCourseDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optLeft[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            $leftSch[$row["VALUE"]] = $row["VALUE"];
        }
        $result->free();
    }

    $optRight = array();
    $query = knjb1256Query::getStd($model, $leftInstate);

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!$leftSch[$row["VALUE"]]) {
            $optRight[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //一覧作成
    $extra = "multiple style=\"width:330px\" width:\"330px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $optRight, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:330px\" width:\"330px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $optLeft, $extra, 20);

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
