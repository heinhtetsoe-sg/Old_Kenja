<?php

require_once('for_php7.php');

class knjd156rForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd156rForm1", "POST", "knjd156rindex.php", "", "knjd156rForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd156rQuery::getSemester();
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
        $query = knjd156rQuery::getAuth($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->hrClass, $extra, 1);

        //学校校種取得
        $setGrade = substr($model->hrClass, 0, 2);
        $model->schoolKind = $db->getOne(knjd156rQuery::getSchoolkindQuery($setGrade));
        knjCreateHidden($objForm, "setSchoolKind", $model->schoolKind);

        //テスト名コンボ
        $query = knjd156rQuery::getTest($model);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->test_cd == $row["VALUE"]) $value_flg = true;
        }
        $model->test_cd = ($model->test_cd && $value_flg) ? $model->test_cd : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('knjd156r'), AllClearList();\"";
        $arg["data"]["TEST_CD"] = knjCreateCombo($objForm, "TEST_CD", $model->test_cd, $opt, $extra, 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /****************/
        /* ラジオボタン */
        /****************/
        //帳票出力 1:レーダーチャート出力 2:特別活動欄出力
        $opt_out = array(1, 2);
        $model->field["OUTPUT_DIV"] = ($model->field["OUTPUT_DIV"] == "") ? "1" : $model->field["OUTPUT_DIV"];
        $extra = array("id=\"OUTPUT_DIV1\"", "id=\"OUTPUT_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_DIV", $model->field["OUTPUT_DIV"], $extra, $opt_out, get_count($opt_out));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //順位出力 1:学年 3:コース 5:コースグループ
        $opt_avg = array(1, 2, 3, 4, 5);
        $model->field["AVG_DIV"] = ($model->field["AVG_DIV"] == "") ? "1" : $model->field["AVG_DIV"];
        $extra = array("id=\"AVG_DIV1\"", "id=\"AVG_DIV2\"", "id=\"AVG_DIV3\"", "id=\"AVG_DIV4\"", "id=\"AVG_DIV5\"");
        $radioArray = knjCreateRadio($objForm, "AVG_DIV", $model->field["AVG_DIV"], $extra, $opt_avg, get_count($opt_avg));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********************/
        /* チェックボックス */
        /********************/
        //凡例出力なし
        $extra = ($model->field["HANREI_SYUTURYOKU_NASI"] == "1") ? "checked='checked' " : "";
        $extra .= " id='HANREI_SYUTURYOKU_NASI'";
        $arg["data"]["HANREI_SYUTURYOKU_NASI"] = knjCreateCheckBox($objForm, "HANREI_SYUTURYOKU_NASI", "1", $extra);

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
        knjCreateHidden($objForm, "PRGID",         "KNJD156R");
        knjCreateHidden($objForm, "DOCUMENTROOT",  DOCUMENTROOT);
        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);

        //学期
        knjCreateHidden($objForm, "SEME_DATE", $seme);
        //学期開始日付
        knjCreateHidden($objForm, "SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
        //学期終了日付
        knjCreateHidden($objForm, "SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
        //学期終了日付
        knjCreateHidden($objForm, "SEME_FLG", $semeflg);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd156rForm1.html", $arg);
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
    $query = knjd156rQuery::getStudent($model);
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
