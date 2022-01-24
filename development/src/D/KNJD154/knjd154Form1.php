<?php

require_once('for_php7.php');

class knjd154Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd154Form1", "POST", "knjd154index.php", "", "knjd154Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd154Query::getSemester();
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

        $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
        if ($model->Properties["useSpecial_Support_School"] == '1') {
            $arg["useHrClassType2"] = "1";
            $opt = array(1, 2);
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"return btn_submit('knjd154');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"return btn_submit('knjd154');\"");
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //学年混合チェックボックス
        $extra = $model->field["GAKUNEN_KONGOU"] == "1" ? "checked" : "";
        $extra .= " onclick=\"return btn_submit('knjd154');\" id=\"GAKUNEN_KONGOU\"";
        $arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");

        //年組コンボ
        $query = knjd154Query::getAuth($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->hrClass, $extra, 1);

        //学校名取得
        $query = knjd154Query::getSchoolName();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //テスト名コンボ
        $query = knjd154Query::getTest($model->semester);
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
        $extra = "onchange=\"return btn_submit('knjd154'), AllClearList();\"";
        $arg["data"]["TEST_CD"] = knjCreateCombo($objForm, "TEST_CD", $model->test_cd, $opt, $extra, 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /****************/
        /* ラジオボタン */
        /****************/
        //帳票パターン
        $opt = array(1, 2, 3, 4, 5, 6);
        $model->field["TYOUHYOU_PATTERN"] = ($model->field["TYOUHYOU_PATTERN"] == "") ? "1" : $model->field["TYOUHYOU_PATTERN"];
        $tyouhyou_pattern_extra_1 = "id=\"TYOUHYOU_PATTERN1\" onClick=\"check_checked()\"";
        $tyouhyou_pattern_extra_2 = "id=\"TYOUHYOU_PATTERN2\" onClick=\"check_checked()\"";
        $tyouhyou_pattern_extra_3 = "id=\"TYOUHYOU_PATTERN3\" onClick=\"check_checked()\"";
        $tyouhyou_pattern_extra_4 = "id=\"TYOUHYOU_PATTERN4\" onClick=\"check_checked()\"";
        $tyouhyou_pattern_extra_5 = "id=\"TYOUHYOU_PATTERN5\" onClick=\"check_checked()\"";
        $tyouhyou_pattern_extra_6 = "id=\"TYOUHYOU_PATTERN6\" onClick=\"check_checked()\"";
        $extra = array($tyouhyou_pattern_extra_1, $tyouhyou_pattern_extra_2, $tyouhyou_pattern_extra_3, $tyouhyou_pattern_extra_4, $tyouhyou_pattern_extra_5, $tyouhyou_pattern_extra_6);
        $radioArray = knjCreateRadio($objForm, "TYOUHYOU_PATTERN", $model->field["TYOUHYOU_PATTERN"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //平均値出力選択チェックボックス
        $extra  = ($model->field["AVG_PRINT"] == "1") ? "checked='checked' " : "";
        $extra .= " id='AVG_PRINT'  onClick=\"check_checked()\"";
        $arg["data"]["AVG_PRINT"] = knjCreateCheckBox($objForm, "AVG_PRINT", "1", $extra);

        //平均値ラジオボタン 1:学年 2:クラス 3:コース
        $opt_avg = array(1, 2, 3);
        $model->field["AVG_DIV"] = ($model->field["AVG_DIV"] == "") ? "1" : $model->field["AVG_DIV"];
        $extra = array("id=\"AVG_DIV1\"", "id=\"AVG_DIV2\"", "id=\"AVG_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "AVG_DIV", $model->field["AVG_DIV"], $extra, $opt_avg, get_count($opt_avg));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //送り状住所 1:生徒 2:保護者 3:その他
        $model->field["OKURIJOU_JUSYO"] = $model->field["OKURIJOU_JUSYO"] ? $model->field["OKURIJOU_JUSYO"] : '2';
        $opt_kijun = array(1, 2, 3);
        $extra = array("id=\"OKURIJOU_JUSYO1\" onClick=\"check_checked()\"", "id=\"OKURIJOU_JUSYO2\" onClick=\"check_checked()\"", "id=\"OKURIJOU_JUSYO3\" onClick=\"check_checked()\"");
        $radioArray = knjCreateRadio($objForm, "OKURIJOU_JUSYO", $model->field["OKURIJOU_JUSYO"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //生徒名出力なし
        $extra = ($model->field["NO_PRINT_STUDENT_NAME"] != "1" || $model->cmd == "") ? " " : "checked='checked' ";
        $extra .= " id='NO_PRINT_STUDENT_NAME' ";
        $arg["data"]["NO_PRINT_STUDENT_NAME"] = knjCreateCheckBox($objForm, "NO_PRINT_STUDENT_NAME", "1", $extra);

        //生徒名出力なし2
        $extra = ($model->field["NO_PRINT_STUDENT_NAME2"] != "1" || $model->cmd == "") ? " " : "checked='checked' ";
        $extra .= " id='NO_PRINT_STUDENT_NAME2' ";
        $arg["data"]["NO_PRINT_STUDENT_NAME2"] = knjCreateCheckBox($objForm, "NO_PRINT_STUDENT_NAME2", "1", $extra);

        //Ａ４角形封筒
        $extra  = ($model->field["FORM_SELECT"] == "1") ? "checked='checked' " : "";
        $extra .= " id='FORM_SELECT' ";
        $arg["data"]["FORM_SELECT"] = knjCreateCheckBox($objForm, "FORM_SELECT", "1", $extra);

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
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出欠集計範囲(累計･学期)ラジオボタン 1:累計 2:学期
        $model->field["DATE_DIV"] = $model->field["DATE_DIV"] ? $model->field["DATE_DIV"] : '1';
        $opt_datediv = array(1, 2);
        $extra2 = " onclick=\"return btn_submit('main');\"";
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
            $query = knjd154Query::getSemesterDetailMst("1");
            $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (strlen($result["SDATE"])) {
                $sDate = $result["SDATE"];
            }
            $sDate = str_replace("-", "/", $sDate);
        }
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        $eDate = ($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        knjCreateHidden($objForm, "DATE", $eDate);
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $eDate);

        /********************/
        /* チェックボックス */
        /********************/
        //出欠の記録の欄なし
        $extra  = ($model->field["ATTENDREC"] == "1") ? "checked='checked' " : "";
        $extra .= " id='ATTENDREC'  onClick=\"check_checked()\"";
        $arg["data"]["ATTENDREC"] = knjCreateCheckBox($objForm, "ATTENDREC", "1", $extra);
        //総合的な学習の時間の欄なし
        $extra  = ($model->field["TOTALSTUDY"] == "1") ? "checked='checked' " : "";
        $extra .= " id='TOTALSTUDY' onClick=\"check_checked()\"";
        $arg["data"]["TOTALSTUDY"] = knjCreateCheckBox($objForm, "TOTALSTUDY", "1", $extra);
        //通信欄なし
        $extra  = ($model->field["CORRE"] == "1") ? "checked='checked' " : "";
        $extra .= " id='CORRE' ";
        $arg["data"]["CORRE"] = knjCreateCheckBox($objForm, "CORRE", "1", $extra);
        //ＳＨＲの欠席時数なし
        $extra  = ($model->field["SHR_KESSEKI_NASI"] == "1") ? "checked='checked' " : "";
        $extra .= " id='SHR_KESSEKI_NASI'";
        $arg["data"]["SHR_KESSEKI_NASI"] = knjCreateCheckBox($objForm, "SHR_KESSEKI_NASI", "1", $extra);
        //凡例出力なし
        if ($model->field["HANREI_SYUTURYOKU_NASI"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
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
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJD154");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
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

        //副担任の非表示のフラグ
        knjCreateHidden($objForm, "KNJD154_HideSubTr", $model->Properties["KNJD154_HideSubTr"]);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd154Form1.html", $arg);
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
    $opt = array();
    $query = knjd154Query::getStudent($model);
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
