<?php

require_once('for_php7.php');

class knjd181Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd181Form1", "POST", "knjd181index.php", "", "knjd181Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期数取得
        $query = knjd181Query::getCountSemester($model);
        $getCountsemester = $db->getOne($query);

        //学期コンボ
        $query = knjd181Query::getSemester($getCountsemester);
        $extra = "onchange=\"return btn_submit('main')\"";
        //ログイン学期が3学期のときはコンボの初期値は学年末を表示
        if ($getCountsemester == 3 && $model->semester == 3 || $getCountsemester == 2 && $model->semester == 2) {
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
        $query = knjd181Query::getAuth($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->hrClass, $extra, 1);

        //学校名取得
        $query = knjd181Query::getSchoolName();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /****************/
        /* ラジオボタン */
        /****************/
        //帳票パターン
        $opt = array(1, 2, 3, 4);
        $model->field["TYOUHYOU_PATTERN"] = ($model->field["TYOUHYOU_PATTERN"] == "") ? "1" : $model->field["TYOUHYOU_PATTERN"];
        $tyouhyou_pattern_extra_1 = "id=\"TYOUHYOU_PATTERN1\" onClick=\"check_checked()\"";
        $tyouhyou_pattern_extra_2 = "id=\"TYOUHYOU_PATTERN2\" onClick=\"check_checked()\"";
        $tyouhyou_pattern_extra_3 = "id=\"TYOUHYOU_PATTERN3\" onClick=\"check_checked()\"";
        $tyouhyou_pattern_extra_4 = "id=\"TYOUHYOU_PATTERN4\" onClick=\"check_checked()\"";
        $extra = array($tyouhyou_pattern_extra_1, $tyouhyou_pattern_extra_2, $tyouhyou_pattern_extra_3, $tyouhyou_pattern_extra_4);
        $radioArray = knjCreateRadio($objForm, "TYOUHYOU_PATTERN", $model->field["TYOUHYOU_PATTERN"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //送り状住所 1:生徒 2:保護者 3:その他
        $model->field["OKURIJOU_JUSYO"] = $model->field["OKURIJOU_JUSYO"] ? $model->field["OKURIJOU_JUSYO"] : '2';
        $opt_kijun = array(1, 2, 3);
        $extra = array("id=\"OKURIJOU_JUSYO1\" onClick=\"check_checked()\" ", "id=\"OKURIJOU_JUSYO2\" onClick=\"check_checked()\" ", "id=\"OKURIJOU_JUSYO3\" onClick=\"check_checked()\" ");
        $radioArray = knjCreateRadio($objForm, "OKURIJOU_JUSYO", $model->field["OKURIJOU_JUSYO"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //学年末欄の内容を出力するラジオ
        $opt = array(1, 2, 3); //1:学年評定 2:学年評価 3:3学期評価
        $model->field["GAKUNENMATU_RAN"] = ($model->field["GAKUNENMATU_RAN"] == "") ? "1" : $model->field["GAKUNENMATU_RAN"];
        $extra = array("id=\"GAKUNENMATU_RAN1\"", "id=\"GAKUNENMATU_RAN2\"", "id=\"GAKUNENMATU_RAN3\"");
        $radioArray = knjCreateRadio($objForm, "GAKUNENMATU_RAN", $model->field["GAKUNENMATU_RAN"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //注意・超過ラジオ
        $opt = array(1, 2); //1:注意 2:超過
        $model->field["TYUI_TYOUKA"] = ($model->field["TYUI_TYOUKA"] == "") ? "1" : $model->field["TYUI_TYOUKA"];
        $extra = array("id=\"TYUI_TYOUKA1\"", "id=\"TYUI_TYOUKA2\"");
        $radioArray = knjCreateRadio($objForm, "TYUI_TYOUKA", $model->field["TYUI_TYOUKA"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //特別活動の出力方法ラジオ 1:個別 2:合算
        $opt = array(1, 2);
        $model->field["SPECIALACT"] = ($model->field["SPECIALACT"] == "") ? "1" : $model->field["SPECIALACT"];
        $extra = array("id=\"SPECIALACT1\" onClick=\"check_checked()\" ", "id=\"SPECIALACT2\" onClick=\"check_checked()\" ");
        $radioArray = knjCreateRadio($objForm, "SPECIALACT", $model->field["SPECIALACT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出力選択ラジオ 1:SHRの欠席時数 2:遅刻・早退
        $opt = array(1, 2);
        $model->field["SHR_LATE_EARLY"] = ($model->field["SHR_LATE_EARLY"] == "") ? "1" : $model->field["SHR_LATE_EARLY"];
        $extra = array("id=\"SHR_LATE_EARLY1\"", "id=\"SHR_LATE_EARLY2\"");
        $radioArray = knjCreateRadio($objForm, "SHR_LATE_EARLY", $model->field["SHR_LATE_EARLY"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        /********************/
        /* チェックボックス */
        /********************/
        //個別のSHRの欠席時数 or 遅刻・早退出力あり
        $extra  = ($model->field["SYUSEKI"] == "1") ? "checked='checked' " : "";
        $extra .= " id='SYUSEKI' onClick=\"check_checked()\" ";
        $arg["data"]["SYUSEKI"] = knjCreateCheckBox($objForm, "SYUSEKI", "1", $extra);

        //合算の遅刻・早退出力あり
        $extra  = ($model->field["LATE_EARLY"] == "1") ? "checked='checked' " : "";
        $extra .= " id='LATE_EARLY' ";
        $arg["data"]["LATE_EARLY"] = knjCreateCheckBox($objForm, "LATE_EARLY", "1", $extra);

        //校長印の出力あり
        if ($model->field["KOUTYOU"] == "1" || $model->cmd == '') {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id='KOUTYOU'";
        $arg["data"]["KOUTYOU"] = knjCreateCheckBox($objForm, "KOUTYOU", "1", $extra);

        //凡例出力なし
        if ($model->field["HANREI_SYUTURYOKU_NASI"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id='HANREI_SYUTURYOKU_NASI'";
        $arg["data"]["HANREI_SYUTURYOKU_NASI"] = knjCreateCheckBox($objForm, "HANREI_SYUTURYOKU_NASI", "1", $extra);

        //生徒名出力なし
        $extra = ($model->field["NO_PRINT_STUDENT_NAME"] != "1" || $model->cmd == "") ? " " : "checked='checked' ";
        $extra .= " id='NO_PRINT_STUDENT_NAME' ";
        $arg["data"]["NO_PRINT_STUDENT_NAME"] = knjCreateCheckBox($objForm, "NO_PRINT_STUDENT_NAME", "1", $extra);

        //生徒名出力なし
        $extra = ($model->field["NO_PRINT_STUDENT_NAME2"] != "1" || $model->cmd == "") ? " " : "checked='checked' ";
        $extra .= " id='NO_PRINT_STUDENT_NAME2' ";
        $arg["data"]["NO_PRINT_STUDENT_NAME2"] = knjCreateCheckBox($objForm, "NO_PRINT_STUDENT_NAME2", "1", $extra);

        //学習の記録の総合的な学習の時間の行を詰める
        $extra = $model->field["NO_BLANK_90"] == "1" ? "checked" : "";
        $extra .= " id=\"NO_BLANK_90\" ";
        $arg["data"]["NO_BLANK_90"] = knjCreateCheckBox($objForm, "NO_BLANK_90", "1", $extra, "");

        //Ａ４角形封筒
        if ($model->field["FORM_SELECT"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id='FORM_SELECT' ";
        $arg["data"]["FORM_SELECT"] = knjCreateCheckBox($objForm, "FORM_SELECT", "1", $extra);

        //送り状住所なし
        $extra  = ($model->field["JUSYO_PRINT"] == "1") ? "checked='checked' " : "";
        $extra .= " id='JUSYO_PRINT'  onClick=\"check_checked()\"";
        $arg["data"]["JUSYO_PRINT"] = knjCreateCheckBox($objForm, "JUSYO_PRINT", "1", $extra);

        //総合的な学習の時間の欄なし
        if ($model->field["SYUKKETU_NO_KIROKU_SOUGOUTEKI_NA_GAKUSYUU_NO_JIKAN_NO_RAN_NASI"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id='SYUKKETU_NO_KIROKU_SOUGOUTEKI_NA_GAKUSYUU_NO_JIKAN_NO_RAN_NASI'";
        $arg["data"]["SYUKKETU_NO_KIROKU_SOUGOUTEKI_NA_GAKUSYUU_NO_JIKAN_NO_RAN_NASI"] = knjCreateCheckBox($objForm, "SYUKKETU_NO_KIROKU_SOUGOUTEKI_NA_GAKUSYUU_NO_JIKAN_NO_RAN_NASI", "1", $extra);

        //注意・超過のチェックボックス
        $extra = ($model->field["TYUI_TYOUKA_CHECK"] == "1" || $model->cmd == "") ? "checked='checked' " : " ";
        $extra .= "id='TYUI_TYOUKA_CHECK' onClick=\"check_checked()\" ";
        $arg["data"]["TYUI_TYOUKA_CHECK"] = knjCreateCheckBox($objForm, "TYUI_TYOUKA_CHECK", "1", $extra);

        //注意・超過のタイトル
        $arg["data"]["TYUI_TYOUKA_TITLE"] = "欠課数上限値（履修／修得）";

        //学期（学年ごと）
        $semeGr9 = array();
        $query = knjd181Query::getSemesterGrade("9", substr($model->hrClass, 0, 2));
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
        $query = knjd181Query::getSemesterGrade($model->field["SEMESTER"], substr($model->hrClass, 0, 2));
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semeGr = $row;
        }

        //出欠集計終了日付
        $eDate = ($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        knjCreateHidden($objForm, "DATE", $eDate);
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $eDate);

        /********************/
        /* テキストボックス */
        /********************/
        //学校種別取得
        $grade = substr($model->hrClass, 0, 2);
        $query = knjd181Query::getGdat($grade);
        $h_j = $db->getOne($query);

        //欠点テキストボックス表示判定
        //「欠点(評価)は、不振チェック参照するか？」の判定
        if ($model->useSlumpD048 == '1') {
            $arg["USE_SLUMP_D048"] = '1'; //null以外なら何でもいい
            unset($arg["KETTEN_FLG"]);
        } else {
            $arg["KETTEN_FLG"] = '1'; //null以外なら何でもいい
            unset($arg["USE_SLUMP_D048"]);
        }

        //欠点（評価）テキストボックス
        if ($model->field["KETTEN"] == "" || $h_j != $model->field["SCHOOL_KIND"]) {
            if ($h_j == 'J') {
                $model->field["KETTEN"] = 2;
            } else {
                $model->field["KETTEN"] = 2;
            }
        }
        $extra = "style=\"text-align: right\" onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $model->field["KETTEN"], "KETTEN", 3, 3, $extra);

        //欠点（評定）テキストボックス
        if ($model->cmd == '') {
            $model->field["KETTEN_HYOTEI"] = 1;
        }
        $extra = "style=\"text-align: right\" onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["KETTEN_HYOTEI"] = knjCreateTextBox($objForm, $model->field["KETTEN_HYOTEI"], "KETTEN_HYOTEI", 3, 3, $extra);

        //増加単位を反映させる
        $extra  = ($model->field["ZOUKA"] == "1") ? "checked='checked' " : "";
        $extra .= " id='ZOUKA'";
        $arg["data"]["ZOUKA"] = knjCreateCheckBox($objForm, "ZOUKA", "1", $extra);

        //通信欄なし
        $extra  = ($model->field["NO_COMM"] == "1") ? "checked='checked' " : "";
        $extra .= " id='NO_COMM'";
        $arg["data"]["NO_COMM"] = knjCreateCheckBox($objForm, "NO_COMM", "1", $extra);

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
        knjCreateHidden($objForm, "PRGID", "KNJD181");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //学期
        knjCreateHidden($objForm, "SEME_DATE", $seme);
        //学期開始日付
        knjCreateHidden($objForm, "SEME_SDATE", str_replace("-", "/", $semeGr["SDATE"]));
        //学期終了日付
        knjCreateHidden($objForm, "SEME_EDATE", str_replace("-", "/", $semeGr["EDATE"]));
        //学期終了日付
        knjCreateHidden($objForm, "SEME_FLG", $semeflg);
        knjCreateHidden($objForm, "tutisyoTokubetuKatudo", $model->Properties["tutisyoTokubetuKatudo"]);
        knjCreateHidden($objForm, "tutisyoTotalstudyWideForm", $model->Properties["tutisyoTotalstudyWideForm"]);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);

        //学校種別
        knjCreateHidden($objForm, "SCHOOL_KIND", $h_j);

        //累積情報の遅刻・早退欄のフラグ
        knjCreateHidden($objForm, "chikokuHyoujiFlg", $model->Properties["chikokuHyoujiFlg"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useAddrField2", $model->Properties["useAddrField2"]);

        //副担任の非表示のフラグ
        knjCreateHidden($objForm, "KNJD181_HideSubTr", $model->Properties["KNJD181_HideSubTr"]);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd181Form1.html", $arg);
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
    $query = knjd181Query::getStudent($model);
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
