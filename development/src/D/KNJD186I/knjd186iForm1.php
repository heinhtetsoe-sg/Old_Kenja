<?php

require_once('for_php7.php');

class knjd186iForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd186iForm1", "POST", "knjd186iindex.php", "", "knjd186iForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd186iQuery::getSemester();
        $extra = "onchange=\"return btn_submit('main')\"";
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["SEMESTER"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        if ($model->field["SEMESTER"] && $value_flg) {
        } else {
            $maxSemester = $db->getOne(knjd186iQuery::getMaxSemester());
            if ($maxSemester == CTRL_SEMESTER) {
                $model->field["SEMESTER"] = "9";
            } else {
                $model->field["SEMESTER"] = CTRL_SEMESTER;
            }
        }
        $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        //学年コンボ
        $query = knjd186iQuery::getIBGrade();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //IBコースコンボ
        $query = knjd186iQuery::getIBPrgCourse();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "IBPRG_COURSE", $model->field["IBPRG_COURSE"], $extra, 1);

        //IB科目コンボ
        $query = knjd186iQuery::getIbSubclass($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "IBSUBCLASSCD", $model->field["IBSUBCLASSCD"], $extra, 1);

        //科目コンボ数
        $model->subclassCnt = $db->getOne(knjd186iQuery::getSubclass($model, "CNT"));

        if ($model->subclassCnt > 0) {
            for ($subCnt=1; $subCnt <= $model->subclassCnt; $subCnt++) {
                $tmp = array();
                //科目コンボ
                $query = knjd186iQuery::getSubclass($model);
                $extra = "onchange=\"return btn_submit('main');\"";
                $tmp["SUBCLASSCD"] = makeCmb2($objForm, $arg, $db, $query, "SUBCLASSCD".$subCnt, $model->field["SUBCLASSCD".$subCnt], $extra, 1, "blank");
                //講座コンボ
                $query = knjd186iQuery::getChair($model, $model->field["SUBCLASSCD".$subCnt]);
                $extra = "onchange=\"return btn_submit('main');\"";
                $tmp["CHAIRCD"] = makeCmb2($objForm, $arg, $db, $query, "CHAIRCD".$subCnt, $model->field["CHAIRCD".$subCnt], $extra, 1, "blank");

                $arg["subchr"][] = $tmp;
            }
        }

        //生徒一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //異動対象日付
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm ,"DATE" ,$model->field["DATE"]);

        /****************/
        /* ラジオボタン */
        /****************/
        //評価の記録1、2
        $opt = array(1, 2);
        $model->field["PRINT_SIDE3"] = ($model->field["PRINT_SIDE3"] == "") ? "1" : $model->field["PRINT_SIDE3"];
        $extra = array("id=\"PRINT_SIDE31\"", "id=\"PRINT_SIDE32\"");
        $radioArray = knjCreateRadio($objForm, "PRINT_SIDE3", $model->field["PRINT_SIDE3"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /**********/
        /* ボタン */
        /**********/
        //プレビュー/印刷
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "', '');\"");
        //プレビュー/印刷
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"");
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
        knjCreateHidden($objForm, "PRGID",         "KNJD186I");
        knjCreateHidden($objForm, "DOCUMENTROOT",  DOCUMENTROOT);
        knjCreateHidden($objForm, "IMAGEPATH",     $model->control["LargePhotoPath"]);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd186iForm1.html", $arg);
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

    if ($value != "" && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//生徒一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //講座取得
    $chairInState = "";
    $sep = "";
    for ($subCnt = 1; $subCnt <= $model->subclassCnt; $subCnt++) {
        if ($model->field["CHAIRCD".$subCnt]) {
            $chairInState .= $sep."'{$model->field["CHAIRCD".$subCnt]}'";
            $sep = ",";
        }
    }
    if (!$chairInState) {
        //全講座
        $query = knjd186iQuery::getAllChair($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chairInState .= $sep."'{$row["CHAIRCD"]}'";
            $sep = ",";
        }
        $result->free();
    }

    //生徒一覧
    $opt = array();
    if ($chairInState) {
        $query = knjd186iQuery::getStudent($model, $chairInState);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
    }

    //生徒一覧作成
    $extra = "multiple style=\"width:280px\" width=\"280px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:280px\" width=\"280px\" ondblclick=\"move1('right')\"";
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
