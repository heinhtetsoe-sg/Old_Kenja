<?php

require_once('for_php7.php');

class knjd234eForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd234eForm1", "POST", "knjd234eindex.php", "", "knjd234eForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        /**********/
        /* コンボ */
        /**********/
        //学年
        $query = knjd234eQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");
        
        //学期
        $query = knjd234eQuery::getSemester($model);
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
        
        //学校校種を取得
        $model->schoolKind = $db->getOne(knjd234eQuery::getSchoolkindQuery($model->field["GRADE"]));
        knjCreateHidden($objForm, "setSchoolKind", $model->schoolKind);

        // //テスト名
        // $query = knjd234eQuery::getTest($model);
        // $opt = array();
        // $value_flg = false;
        // $result = $db->query($query);
        // $add = false;
        // while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //     $opt[] = array('label' => $row["LABEL"],
        //                    'value' => $row["VALUE"]);
        //     $add = true;
        //     if ($model->test_cd == $row["VALUE"]) $value_flg = true;
        // }
        // 
        // $model->test_cd = ($model->test_cd && $value_flg) ? $model->test_cd : $opt[0]["value"];
        // $extra = " onchange=\"return btn_submit('knjd234eSelectTest');\"";
        // $arg["data"]["TEST_CD"] = knjCreateCombo($objForm, "TEST_CD", $model->test_cd, $opt, $extra, 1);

        /****************/
        /* ラジオボタン */
        /****************/
        //出欠集計範囲(累計･学期)ラジオボタン 1:累計 2:学期
        $model->field["DATE_DIV"] = $model->field["DATE_DIV"] ? $model->field["DATE_DIV"] : '1';
        $opt_datediv = array(1, 2);
        $extra2 = " onclick=\"return btn_submit('main');\"";
        $extra = array("id=\"DATE_DIV1\"".$extra2, "id=\"DATE_DIV2\"".$extra2);
        $radioArray = knjCreateRadio($objForm, "DATE_DIV", $model->field["DATE_DIV"], $extra, $opt_datediv, get_count($opt_datediv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        knjCreateHidden($objForm, "DISP_DATE_DIV", $model->field["DATE_DIV"]);

        // //順位の基準点 1:総計 2:平均点
        // $model->field["OUTPUT_KIJUN"] = $model->field["OUTPUT_KIJUN"] ? $model->field["OUTPUT_KIJUN"] : '1';
        // $opt_kijun = array(1, 2);
        // $extra = array("id=\"OUTPUT_KIJUN1\"", "id=\"OUTPUT_KIJUN2\"");
        // $radioArray = knjCreateRadio($objForm, "OUTPUT_KIJUN", $model->field["OUTPUT_KIJUN"], $extra, $opt_kijun, get_count($opt_kijun));
        // foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計開始日付
        $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]];//日付がない場合、学期開始日付を使用する。
        $sDate = str_replace("-", "/", $sDate);
        $query = knjd234eQuery::getSemester($model, $model->field["SEMESTER"]);
        $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
        //累計の場合、出欠集計範囲の開始日は、学期詳細マスタの詳細コード＝１の開始日とする。
        if ($model->field["DATE_DIV"] == "1") {
            if (strlen($result["SDATE"])) {
                $sDate = $result["SDATE"];
            }
            $sDate = str_replace("-", "/", $sDate);
        }
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付(未入力状態、または累計/学期切り替わりまたは選択学期切り替わりなら仮設定する)
        if (!$model->field["EDATE"] || $model->dispDataDiv != $model->field["DATE_DIV"] || $this->dispSemester != $model->field["SEMESTER"]) {
            if (strlen($result["EDATE"])) {
                //通年指定以外で学期の範囲にCTRL_DATEが含まれていれば、CTRL_DATEを最終日に設定
                if ($model->field["SEMESTER"] != "9" && ($model->field["DATE_DIV"] == "1")) {
                    $eDate = CTRL_DATE;
                } else {
                    $eDate = $result["EDATE"];
                }
            } else {
                $eDate = CTRL_DATE;//日付がない場合、学籍処理日を使用する。
            }
        } else {
            $eDate = $model->field["EDATE"];
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
        /* テキストボックス */
        /********************/
        // //評価平均
        // if ($model->cmd == 'knjd234eSelectTest') {
        //     if ($model->field["SEMESTER"] == '9' && $model->field["TESTCD"] == '990009') {
        //         $model->field["HYOKA_HEIKIN"] = 2;
        //     }
        // }
        // if ($model->field["HYOKA_HEIKIN"] == '') {
        //     if ($model->field["SEMESTER"] == '9' && $model->field["TESTCD"] == '990009') {
        //         $model->field["HYOKA_HEIKIN"] = "2.0";
        //     } else { 
        //         $model->field["HYOKA_HEIKIN"] = "4.0";
        //     }
        // }
        // $extra = " id=\"HYOKA_HEIKIN\" onBlur=\"return this.value=toFloat(this.value);\" style=\"text-align: right;\"";
        // $arg["data"]["HYOKA_HEIKIN"] = knjCreateTextBox($objForm, $model->field["HYOKA_HEIKIN"], "HYOKA_HEIKIN", 4, 4, $extra);

        // //優良者
        // $extra = " id=\"YURYO\" onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
        // $model->field["YURYO"] = $model->field["YURYO"] ? $model->field["YURYO"] : "25";
        // $arg["data"]["YURYO"] = knjCreateTextBox($objForm, $model->field["YURYO"], "YURYO", 3, 3, $extra);

        // //出欠状況
        // if ($model->field["KESSEKI"] == '') {
        //     $model->field["KESSEKI"] = 15;
        // }
        // $extra = " onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
        // $extraid = " id=\"KESSEKI\"";
        // $arg["data"]["KESSEKI"] = knjCreateTextBox($objForm, $model->field["KESSEKI"], "KESSEKI", 3, 3, $extra.$extraid);
 
        //不振を出力する
        $extra = $model->cmd == '' || $model->field["OUTPUT_FUSHIN"] == "1" ? "checked" : "";
        $extra .= " id=\"OUTPUT_FUSHIN\" ";
        $arg["data"]["OUTPUT_FUSHIN"] = knjCreateCheckBox($objForm, "OUTPUT_FUSHIN", "1", $extra, "");

        //リスト作成
        makeClassList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //プレビュー/印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'print');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DISP_SEMESTER", $model->semester);
        knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",         "KNJD234E");
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //学期
        knjCreateHidden($objForm, "SEME_DATE",  $seme);
        //学期開始日付
        knjCreateHidden($objForm, "SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
        //学期終了日付
        knjCreateHidden($objForm, "SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd234eForm1.html", $arg);
    }
}

//クラスのリストToリスト作成
function makeClassList(&$objForm, &$arg, $db, $model)
{
    $query = knjd234eQuery::getHrClassAuth($model, CTRL_YEAR, $model->field["SEMESTER"], AUTHORITY, STAFFCD, $model->field["GRADE"]);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }

    $result->free();
    //クラス一覧を作成する
    $extra = "multiple style=\"width:300px\" width:\"300px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 20);

    //出力対象一覧を作成する
    $extra = "multiple style=\"width:300px\" width:\"300px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", array(), $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
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
?>
