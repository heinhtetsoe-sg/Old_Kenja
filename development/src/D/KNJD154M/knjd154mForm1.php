<?php

require_once('for_php7.php');

class knjd154mForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd154mForm1", "POST", "knjd154mindex.php", "", "knjd154mForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd154mQuery::getSemester();
        $extra = "onchange=\"return btn_submit('main')\"";
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
        $query = knjd154mQuery::getAuth($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->hrClass, $extra, 1);

        //学校校種取得
        $setGrade = substr($model->hrClass, 0, 2);
        $model->schoolKind = $db->getOne(knjd154mQuery::getSchoolkindQuery($setGrade));
        knjCreateHidden($objForm, "setSchoolKind", $model->schoolKind);

        //テスト名コンボ
        $query = knjd154mQuery::getTest($model);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->test_cd == $row["VALUE"]) $value_flg = true;
        }
        $model->test_cd = ($model->test_cd && $value_flg) ? $model->test_cd : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('knjd154m'), AllClearList();\"";
        $arg["data"]["TEST_CD"] = knjCreateCombo($objForm, "TEST_CD", $model->test_cd, $opt, $extra, 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //試験種別一覧リストToリスト
        makeListToList2($objForm, $arg, $db, $model);

        /****************/
        /* ラジオボタン */
        /****************/
        //平均値出力選択チェックボックス
        $extra  = ($model->field["AVG_PRINT"] == "1") ? "checked='checked' " : "";
        $extra .= " id='AVG_PRINT'  onClick=\"check_checked()\"";
        $arg["data"]["AVG_PRINT"] = knjCreateCheckBox($objForm, "AVG_PRINT", "1", $extra);

        //平均値ラジオボタン 1:学年 2:クラス 3:コース 4:学科
        $opt_avg = array(1, 2, 3, 4);
        $model->field["AVG_DIV"] = ($model->field["AVG_DIV"] == "") ? "1" : $model->field["AVG_DIV"];
        $extra = array("id=\"AVG_DIV1\"", "id=\"AVG_DIV2\"", "id=\"AVG_DIV3\"", "id=\"AVG_DIV4\"");
        $radioArray = knjCreateRadio($objForm, "AVG_DIV", $model->field["AVG_DIV"], $extra, $opt_avg, get_count($opt_avg));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //送り状住所 1:生徒 2:保護者 3:その他
        $model->field["OKURIJOU_JUSYO"] = $model->field["OKURIJOU_JUSYO"] ? $model->field["OKURIJOU_JUSYO"] : '2';
        $opt_kijun = array(1, 2, 3);
        $extra = array("id=\"OKURIJOU_JUSYO1\" onClick=\"check_checked()\"", "id=\"OKURIJOU_JUSYO2\" onClick=\"check_checked()\"", "id=\"OKURIJOU_JUSYO3\" onClick=\"check_checked()\"");
        $radioArray = knjCreateRadio($objForm, "OKURIJOU_JUSYO", $model->field["OKURIJOU_JUSYO"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //生徒名出力なし
        $extra = ($model->field["NO_PRINT_STUDENT_NAME"] != "1" || $model->cmd == "") ? " " : "checked='checked' ";
        $extra .= " id='NO_PRINT_STUDENT_NAME' ";
        $arg["data"]["NO_PRINT_STUDENT_NAME"] = knjCreateCheckBox($objForm, "NO_PRINT_STUDENT_NAME", "1", $extra);

        //生徒名出力なし2
        $extra = ($model->field["NO_PRINT_STUDENT_NAME2"] != "1" || $model->cmd == "") ? " " : "checked='checked' ";
        $extra .= " id='NO_PRINT_STUDENT_NAME2' ";
        $arg["data"]["NO_PRINT_STUDENT_NAME2"] = knjCreateCheckBox($objForm, "NO_PRINT_STUDENT_NAME2", "1", $extra);

        //送り状住所なしチェックボックス
        $extra  = ($model->field["JUSYO_PRINT"] == "1") ? "checked='checked' " : "";
        $extra .= " id='JUSYO_PRINT'  onClick=\"check_checked()\"";
        $arg["data"]["JUSYO_PRINT"] = knjCreateCheckBox($objForm, "JUSYO_PRINT", "1", $extra);

        //注意・超過のタイトル
        $arg["data"]["TYUI_TYOUKA_TITLE"] = "欠課数上限値（履修／修得）";

        //注意・超過ラジオ
        $opt = array(1, 2); //1:注意 2:超過
        $model->field["TYUI_TYOUKA"] = ($model->field["TYUI_TYOUKA"] == "") ? "1" : $model->field["TYUI_TYOUKA"];
        $extra = array("id=\"TYUI_TYOUKA1\"", "id=\"TYUI_TYOUKA2\"");
        $radioArray = knjCreateRadio($objForm, "TYUI_TYOUKA", $model->field["TYUI_TYOUKA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計範囲(累計･学期)ラジオボタン 1:累計 2:学期
        $model->field["DATE_DIV"] = $model->field["DATE_DIV"] ? $model->field["DATE_DIV"] : '1';
        $opt_datediv = array(1, 2);
        $extra2 = " onclick=\"return btn_submit('main');\"";
        $extra = array("id=\"DATE_DIV1\"".$extra2, "id=\"DATE_DIV2\"".$extra2);
        $radioArray = knjCreateRadio($objForm, "DATE_DIV", $model->field["DATE_DIV"], $extra, $opt_datediv, get_count($opt_datediv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計開始日付
        $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]];//日付がない場合、学期開始日付を使用する。
        $sDate = str_replace("-", "/", $sDate);
        //累計の場合、出欠集計範囲の開始日は、学期詳細マスタの詳細コード＝１の開始日とする。
        if ($model->field["DATE_DIV"] == "1") {
            $query = knjd154mQuery::getSemesterDetailMst("1");
            $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (strlen($result["SDATE"])) {
                $sDate = $result["SDATE"];
            }
            $sDate = str_replace("-", "/", $sDate);
        }
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        $query = knjd154mQuery::get_semester_detail($model);
        $semester_detail = $db->getOne($query);
        $query = knjd154mQuery::getSemesterDetailMst($semester_detail);
        $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (strlen($result["EDATE"])) {
            $eDate = $result["EDATE"];
        } else {
            $eDate = CTRL_DATE;//日付がない場合、学籍処理日を使用する。
        }
        $eDate = str_replace("-", "/", $eDate);
        //日付が学期の範囲外の場合、学期終了日付を使用する。
        if ($eDate < $model->control["学期開始日付"][$model->field["SEMESTER"]] || 
            $eDate > $model->control["学期終了日付"][$model->field["SEMESTER"]]) {
            $eDate = $model->control["学期終了日付"][$model->field["SEMESTER"]];
        }
        knjCreateHidden($objForm, "DATE", $eDate);
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $eDate);

        /********************/
        /* チェックボックス */
        /********************/
        //寮生だけ表示
        $extra  = ($model->field["RYOUSEI_ONLY"] == "1") ? "checked='checked' " : "";
        $extra .= " id='RYOUSEI_ONLY' onchange=\"return btn_submit('main')\"";
        $arg["data"]["RYOUSEI_ONLY"] = knjCreateCheckBox($objForm, "RYOUSEI_ONLY", "1", $extra);
        ////学校行事の欠席時数なし
        //$extra  = ($model->field["GYOJI_KESSEKI_NASI"] == "1") ? "checked='checked' " : "";
        //$extra .= " id='GYOJI_KESSEKI_NASI'";
        //$arg["data"]["GYOJI_KESSEKI_NASI"] = knjCreateCheckBox($objForm, "GYOJI_KESSEKI_NASI", "1", $extra);
        //凡例出力なし
        $extra = ($model->field["HANREI_SYUTURYOKU_NASI"] == "1") ? "checked='checked' " : "";
        $extra .= " id='HANREI_SYUTURYOKU_NASI'";
        $arg["data"]["HANREI_SYUTURYOKU_NASI"] = knjCreateCheckBox($objForm, "HANREI_SYUTURYOKU_NASI", "1", $extra);

        $schname = knjd154mQuery::getSchoolName();
        //茗渓以外は表示する。
        if ($schname != "meikei") {
            $arg["DISPKKS_NASI"] = 1;
            //公欠・忌引・出停時数なし
            $extra = ($model->field["KOKETSU_KIBIKI_SHUTTEI_NASI"] == "1") ? "checked='checked' " : "";
            $extra .= " id='KOKETSU_KIBIKI_SHUTTEI_NASI'";
            $arg["data"]["KOKETSU_KIBIKI_SHUTTEI_NASI"] = knjCreateCheckBox($objForm, "KOKETSU_KIBIKI_SHUTTEI_NASI", "1", $extra);
        }

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
        knjCreateHidden($objForm, "PRGID",         "KNJD154M");
        knjCreateHidden($objForm, "DOCUMENTROOT",  DOCUMENTROOT);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);

        //学期
        knjCreateHidden($objForm, "SEME_DATE", $seme);
        //学期開始日付
        knjCreateHidden($objForm, "SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
        //学期終了日付
        knjCreateHidden($objForm, "SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
        //学期終了日付
        knjCreateHidden($objForm, "SEME_FLG", $semeflg);
        knjCreateHidden($objForm, "tutisyoTokubetuKatudo", $model->Properties["tutisyoTokubetuKatudo"]);

        //累積情報の遅刻・早退欄のフラグ
        knjCreateHidden($objForm, "chikokuHyoujiFlg", $model->Properties["chikokuHyoujiFlg"]);

        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd154mForm1.html", $arg);
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
    $query = knjd154mQuery::getStudent($model);
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

//クラス一覧リストToリスト作成
function makeListToList2(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $opt = array();
    $query = knjd154mQuery::getTest($model, true);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1_2('left')\"";
    $arg["data"]["CATEGORY_NAME2"] = knjCreateCombo($objForm, "CATEGORY_NAME2", "", array(), $extra, 7);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1_2('right')\"";
    $arg["data"]["CATEGORY_SELECTED2"] = knjCreateCombo($objForm, "CATEGORY_SELECTED2", "", $opt, $extra, 7);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves_2('left');\"";
    $arg["button"]["btn_lefts2"] = knjCreateBtn($objForm, "btn_lefts2", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1_2('left');\"";
    $arg["button"]["btn_left2"] = knjCreateBtn($objForm, "btn_left2", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1_2('right');\"";
    $arg["button"]["btn_right2"] = knjCreateBtn($objForm, "btn_right2", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves_2('right');\"";
    $arg["button"]["btn_rights2"] = knjCreateBtn($objForm, "btn_rights2", ">>", $extra);
}
?>
