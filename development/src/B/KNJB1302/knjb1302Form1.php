<?php

require_once('for_php7.php');

class knjb1302Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjb1302Form1", "POST", "knjb1302index.php", "", "knjb1302Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjb1302Query::getSemester();
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //年組コンボ
        $query = knjb1302Query::getSelectGrade($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //学校名取得
        $query = knjb1302Query::getSchoolName();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //履修履歴コンボ
        $query = knjb1302Query::getRirekiCode($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "RIREKI_CODE", $model->field["RIREKI_CODE"], $extra, 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        /****************/
        /* ラジオボタン */
        /****************/
        //対象名簿
        $opt = array(1, 2); //1:講座名簿 2:履修科目名簿
        $model->field["TAISYOU_MEIBO"] = ($model->field["TAISYOU_MEIBO"] == "") ? "1" : $model->field["TAISYOU_MEIBO"];
        $taisyou_meibo_extra_1 = "id=\"TAISYOU_MEIBO1\" onClick=\"return btn_submit('main')\"";
        $taisyou_meibo_extra_2 = "id=\"TAISYOU_MEIBO2\" onClick=\"return btn_submit('main')\"";
        $extra = array($taisyou_meibo_extra_1, $taisyou_meibo_extra_2);
        $radioArray = knjCreateRadio($objForm, "TAISYOU_MEIBO", $model->field["TAISYOU_MEIBO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //フォーム選択可変固定
        $opt = array(1, 2); //1:可変 2:固定
        $model->field["FORM_SELECT"] = ($model->field["FORM_SELECT"] == "") ? "1" : $model->field["FORM_SELECT"];
        $extra = array("id=\"FORM_SELECT1\" onClick=\"return btn_submit('main')\"","id=\"FORM_SELECT2\" onClick=\"return btn_submit('main')\"");
        $radioArray = knjCreateRadio($objForm, "FORM_SELECT", $model->field["FORM_SELECT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        if ($model->field["FORM_SELECT"] == "1") {
            $arg["data"]["KAHEN"] = "1";
        }

        /********************/
        /* チェックボックス */
        /********************/
        if ($model->field["KIJUN1"]) {
            $extra = "id=\"KIJUN1\" checked";
        } else {
            $extra = "id=\"KIJUN1\"";
        }
        $extra .= " onClick=\"kijun_check(this)\"";
        $arg["data"]["KIJUN1"] = knjCreateCheckBox($objForm, "KIJUN1", 1, $extra);

        if ($model->field["TAISYOU_MEIBO"] == '1') {
            if ($model->field["KIJUN2"]) {
                $extra = "id=\"KIJUN2\" checked";
            } else {
                $extra = "id=\"KIJUN2\"";
            }
            $extra .= " onClick=\"kijun_check(this)\"";
            $arg["data"]["KIJUN2"] = knjCreateCheckBox($objForm, "KIJUN2", 1, $extra);
        }

        if ($model->field["KIJUN3"]) {
            $extra = "id=\"KIJUN3\" checked";
        } else {
            $extra = "id=\"KIJUN3\"";
        }
        $extra .= " onClick=\"kijun_check(this)\"";
        $arg["data"]["KIJUN3"] = knjCreateCheckBox($objForm, "KIJUN3", 1, $extra);

        /**********/
        /* ボタン */
        /**********/
        //プレビュー/印刷
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",         "KNJB1302");
        knjCreateHidden($objForm, "STAFFCD",       STAFFCD);
        knjCreateHidden($objForm, "SDATE",         $model->control["学期開始日付"][$model->field["SEMESTER"]]);
        knjCreateHidden($objForm, "EDATE",         $model->control["学期終了日付"][$model->field["SEMESTER"]]);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb1302Form1.html", $arg);
    }
}
/****************************************************** 以下関数 ************************************************************/
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

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $opt = array();
    $query = knjb1302Query::getCategoryName($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

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
