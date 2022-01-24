<?php

require_once('for_php7.php');

class knjb0058Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb0058Form1", "POST", "knjb0058index.php", "", "knjb0058Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->exeYear;

        //学期コンボ
        $query = knjb0058Query::getSemester($model);
        $extra = "onchange=\"return btn_submit('subMain')\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR_SEMESTER", $model->field["YEAR_SEMESTER"], $extra, 1);

        //履修履歴コンボ
        $query = knjb0058Query::getRirekiCode($model);
        $extra = "onChange=\"return btn_submit('subMain');\"";
        makeCmb($objForm, $arg, $db, $query, "RIREKI_CODE", $model->field["RIREKI_CODE"], $extra, 1);

        //コースコンボ
        $query = knjb0058Query::getCourseCode($model);
        $extra = "onchange=\"return btn_submit('subMain')\"";
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $model->field["COURSECODE"], $extra, 1, "BLANK");

        //科目コンボ
        $query = knjb0058Query::getSubclassStdSelect($model);
        $extra = "onchange=\"return btn_submit('subMain')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "BLANK");

        //radio
        $opt = array(1, 2);
        $model->field["SYORI"] = ($model->field["SYORI"] == "") ? "1" : $model->field["SYORI"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SYORI{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SYORI", $model->field["SYORI"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["SYORI"] == "2") {
            $arg["CHAIR_DISP"] = "1";
        }

        //講座コンボ
        $query = knjb0058Query::getChairCmb($model);
        $extra = "onchange=\"return btn_submit('subMain')\"";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "BLANK");

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onClick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //終了
        $extra = "onClick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "PRGID", "KNJB0058");
        knjCreateHidden($objForm, "selectStd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb0058Form1.html", $arg);
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
        $value = ($name == "YEAR_SEMESTER") ? (CTRL_YEAR + 1).":1" : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $optLeft = array();
    $leftInstate = "(";
    $sep = "";
    $query = knjb0058Query::getChairStd($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setLabel = $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"]."({$row["SCHREGNO"]})";
        $optLeft[] = array('label' => $setLabel,
                           'value' => $row["SCHREGNO"]);
        $leftInstate .= $sep."'{$row["SCHREGNO"]}'";
        $sep = ",";
    }
    $result->free();
    $leftInstate .= ")";
    $leftInstate = $leftInstate == "()" ? "('')" : $leftInstate;

    $optRight = array();
    $query = knjb0058Query::getSubclassStd($model, $leftInstate);

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setLabel = $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"]."({$row["SCHREGNO"]})";
        $optRight[] = array('label' => $setLabel,
                            'value' => $row["SCHREGNO"]);
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
