<?php

require_once('for_php7.php');

class knjd617eForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd617eForm1", "POST", "knjd617eindex.php", "", "knjd617eForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd617eQuery::getSemester();
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
        $query = knjd617eQuery::getAuth($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->hrClass, $extra, 1);

        //学校校種取得
        $setGrade = substr($model->hrClass, 0, 2);
        $model->schoolKind = $db->getOne(knjd617eQuery::getSchoolkindQuery($setGrade));
        knjCreateHidden($objForm, "setSchoolKind", $model->schoolKind);

        //テスト名コンボ
        $query = knjd617eQuery::getTest($model);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->test_cd == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $model->test_cd = ($model->test_cd && $value_flg) ? $model->test_cd : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('knjd617e'), AllClearList();\"";
        $arg["data"]["TEST_CD"] = knjCreateCombo($objForm, "TEST_CD", $model->test_cd, $opt, $extra, 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //試験種別一覧リストToリスト
        makeListToList2($objForm, $arg, $db, $model);

        /****************/
        /* ラジオボタン */
        /****************/
        //出欠集計範囲(累計･学期)ラジオボタン 1:累計 2:学期
        $model->field["DATE_DIV"] = $model->field["DATE_DIV"] ? $model->field["DATE_DIV"] : '1';
        $opt_datediv = array(1, 2);
        $extra2 = " onclick=\"return btn_submit('chgTerm');\"";
        $extra = array("id=\"DATE_DIV1\"".$extra2, "id=\"DATE_DIV2\"".$extra2);
        $radioArray = knjCreateRadio($objForm, "DATE_DIV", $model->field["DATE_DIV"], $extra, $opt_datediv, get_count($opt_datediv));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出欠集計開始日付
        $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]];//日付がない場合、学期開始日付を使用する。
        $sDate = str_replace("-", "/", $sDate);
        //累計の場合、出欠集計範囲の開始日は、学期詳細マスタの詳細コード＝１の開始日とする。
        if ($model->field["DATE_DIV"] == "1") {
            $query = knjd617eQuery::getSemesterDetailMst("1");
            $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (strlen($result["SDATE"])) {
                $sDate = $result["SDATE"];
            }
            $sDate = str_replace("-", "/", $sDate);
        }
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        $query = knjd617eQuery::getSemesterDetail($model);
        $semester_detail = $db->getOne($query);
        $query = knjd617eQuery::getSemesterDetailMst($semester_detail);
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
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD617E");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //学期
        knjCreateHidden($objForm, "SEME_DATE", $seme);
        //学期開始日付
        knjCreateHidden($objForm, "SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
        //学期終了日付
        knjCreateHidden($objForm, "SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        knjCreateHidden($objForm, "MAXSEL2", "50");  //下部リストの最大選択数

        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd617eForm1.html", $arg);
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
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
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $optL = array();
    $optR = array();
    $query = knjd617eQuery::getStudent($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->cmd == "chgTerm" && in_array($row["VALUE"], $model->selectdata)) {
            $optR[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        } else {
            $optL[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $optL, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $optR, $extra, 20);

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
function makeListToList2(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $optL = array();
    $optR = array();
    $query = knjd617eQuery::getListProficiency($model);

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->cmd == "chgTerm" && in_array($row["VALUE"], $model->selectdata2)) {
            $optR[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        } else {
            $optL[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1_2('left')\"";
    $arg["data"]["CATEGORY_NAME2"] = knjCreateCombo($objForm, "CATEGORY_NAME2", "", $optL, $extra, 7);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1_2('right')\"";
    $arg["data"]["CATEGORY_SELECTED2"] = knjCreateCombo($objForm, "CATEGORY_SELECTED2", "", $optR, $extra, 7);

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
