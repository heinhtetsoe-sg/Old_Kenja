<?php
class knja233dForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja233dForm1", "POST", "knja233dindex.php", "", "knja233dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->year;

        /**********/
        /* コンボ */
        /**********/
        //学期コンボボックスを作成する
        $query = knja233dQuery::getSemester($model);
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
        $query = knja233dQuery::getClassMst($model);
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], "onChange=\"return btn_submit('knja233d');\"", 1);

        //テスト種別コンボ
        $query = knja233dQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], "onChange=\"return btn_submit('knja233d');\"", 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);


        //帳票種類radio
        $opt = array(1, 2, 3);
        $model->field["FRM_PATERN"] = getDefVal($model, $model->field["FRM_PATERN"], "1", $model->PrgDefaultVal["FRM_PATERN"], $model->cmd);
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"FRM_PATERN{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "FRM_PATERN", $model->field["FRM_PATERN"], $extra, $opt, count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ふりがな出力チェックボックス
        $model->field["KANA_PRINT"] = getDefVal($model, $model->field["KANA_PRINT"], "1", $model->PrgDefaultVal["KANA_PRINT"], $model->cmd);
        $extra = $model->field["KANA_PRINT"] == "1" ? " checked " : "";
        $arg["data"]["KANA_PRINT"] = knjCreateCheckBox($objForm, "KANA_PRINT", $model->field["KANA_PRINT"], $extra." id=\"KANA_PRINT\"", "");

        //出力件数
        $extraInt    = " onblur=\"this.value=toInteger(this.value)\";";
        $extraRight  = " STYLE=\"text-align: right\"";
        $model->field["KENSUU"] = ($model->field["KENSUU"]) ? $model->field["KENSUU"] : 1;
        $arg["data"]["KENSUU"] = knjCreateTextBox($objForm, $model->field["KENSUU"], "KENSUU", 3, 2, $extraInt.$extraRight);

        //除籍者名前無し
        $model->field["GRD_NAME_NASI"] = getDefVal($model, $model->field["GRD_NAME_NASI"], "1", $model->PrgDefaultVal["GRD_NAME_NASI"], $model->cmd);
        $extra = $model->field["GRD_NAME_NASI"] == "1" ? " checked " : "";
        $extra .= "id=\"GRD_NAME_NASI\"";
        $arg["data"]["GRD_NAME_NASI"] = knjCreateCheckBox($objForm, "GRD_NAME_NASI", "1", $extra);

        //カレンダーコントロール
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : $model->control["学籍処理日"];
        $arg["el"]["DATE"] = View::popUpCalendar2($objForm, "DATE", $value, "reload=true", " btn_submit('knja233d')", "");

        /**********/
        /* ボタン */
        /**********/
        //プレビュー
        $extra = "onclick=\"return newwin('".SERVLET_URL."', 'KNJA233D');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //EXCEL出力ボタン
        $model->schoolCd = $db->getOne(knja233dQuery::getSchoolCd());
        $extra = "onclick=\"return newwin2('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
        $arg["button"]["btn_xls"] = knjCreateBtn($objForm, "btn_xls", "EXCEL出力", $extra);

        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "ATTENDCLASSCD");
        knjCreateHidden($objForm, "GROUPCD");
        knjCreateHidden($objForm, "NAME_SHOW");
        knjCreateHidden($objForm, "CHARGEDIV");
        knjCreateHidden($objForm, "APPDATE");
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        
        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja233dForm1.html", $arg); 
    }
}

function getDefVal($model, $setVal, $defVal, $prgDefVal, $cmd) {
    $retVal = $defVal;
    if (method_exists($model, 'getSetDefaultVal')) {
        $retVal = $model->getSetDefaultVal($setVal, $defVal, $prgDefVal, $cmd);
    }
    return $retVal;
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
    $result = $db->query(knja233dQuery::getChairDat($model));
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
