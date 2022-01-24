<?php

require_once('for_php7.php');

class knjd183jForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd183jForm1", "POST", "knjd183jindex.php", "", "knjd183jForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期数取得
        $query = knjd183jQuery::getCountSemester($model);
        $getCountsemester = $db->getOne($query);

        //学期コンボ
        $query = knjd183jQuery::getSemester($getCountsemester);
        $extra = "onchange=\"return btn_submit('main')\"";
        //ログイン学期が3学期のときはコンボの初期値は学年末を表示
        if ($getCountsemester == 3 && $model->semester == 3 || $getCountsemester == 2 && $model->semester == 2 ) {
            $model->semester = 9;
        }
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }

        //年組コンボ
        $query = knjd183jQuery::getAuth($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->hrClass, $extra, 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //学期（学年ごと）
        $semeGr9 = array();
        $query = knjd183jQuery::getSemesterGrade("9", substr($model->hrClass, 0, 2));
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semeGr9 = $row;
        }

        //出欠集計開始日付
        $sDate = $semeGr9["SDATE"]; //日付がない場合、学期開始日付を使用する。
        $sDate = str_replace("-", "/", $sDate);
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //学期（学年ごと）
        $semeGr = array();
        $query = knjd183jQuery::getSemesterGrade($model->field["SEMESTER"], substr($model->hrClass, 0, 2));
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semeGr = $row;
        }

        //出欠集計終了日付
        $eDate = CTRL_DATE;//日付がない場合、学籍処理日を使用する。
        //日付が学期の範囲外の場合、学期終了日付を使用する。
        if ($eDate < $semeGr["SDATE"] || 
            $eDate > $semeGr["EDATE"]) {
            $eDate = $semeGr["EDATE"];
        }
        $eDate = str_replace("-", "/", $eDate);
        knjCreateHidden($objForm, "DATE", $eDate);
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $eDate);

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
        knjCreateHidden($objForm, "PRGID",         "KNJD183J");
        knjCreateHidden($objForm, "DOCUMENTROOT",  DOCUMENTROOT);

        //学期開始日付
        knjCreateHidden($objForm, "SEME_SDATE", str_replace("-", "/", $semeGr["SDATE"]));
        //学期終了日付
        knjCreateHidden($objForm, "SEME_EDATE", str_replace("-", "/", $semeGr["EDATE"]));
        //学期終了日付
        knjCreateHidden($objForm, "SEME_FLG", $semeflg);
        //教育課程コード
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
        View::toHTML($model, "knjd183jForm1.html", $arg);
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
    $query = knjd183jQuery::getStudent($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
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
