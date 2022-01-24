<?php

require_once('for_php7.php');

class knjd194Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd194Form1", "POST", "knjd194index.php", "", "knjd194Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        /**********/
        /* コンボ */
        /**********/
        //学期
        $query = knjd194Query::getSemester($model);
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

        //テスト名
        $query = knjd194Query::getTest($model);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->test_cd == $row["VALUE"]) $value_flg = true;
        }
        $model->test_cd = ($model->test_cd && $value_flg) ? $model->test_cd : $opt[0]["value"];
        $extra = "";
        $arg["data"]["TEST_CD"] = knjCreateCombo($objForm, "TEST_CD", $model->test_cd, $opt, $extra, 1);

        /****************/
        /* ラジオボタン */
        /****************/
        //1:素点 2:評価
        //checkKettenDivが設定有りの時は非表示
        if ($model->Properties["checkKettenDiv"]) {
            unset($arg["SHOW_SOTEN_HYOUKA"]);
            knjCreateHidden($objForm, "SOTEN_HYOUKA", '1');
        } else {
            $arg["SHOW_SOTEN_HYOUKA"] = '1'; //NULLじゃなければ何でもいい

            $opt = array(1, 2);
            $model->field["SOTEN_HYOUKA"] = ($model->field["SOTEN_HYOUKA"] == "") ? "1" : $model->field["SOTEN_HYOUKA"];
            $SOTEN_HYOUKA_extra_1 = "id=\"SOTEN_HYOUKA1\"";
            $SOTEN_HYOUKA_extra_2 = "id=\"SOTEN_HYOUKA2\"";
            $extra = array($SOTEN_HYOUKA_extra_1, $SOTEN_HYOUKA_extra_2);
            $radioArray = knjCreateRadio($objForm, "SOTEN_HYOUKA", $model->field["SOTEN_HYOUKA"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        }

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
            $query = knjd194Query::getSemesterDetailMst("1");
            $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (strlen($result["SDATE"])) {
                $sDate = $result["SDATE"];
            }
            $sDate = str_replace("-", "/", $sDate);
        }
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        $query = knjd194Query::get_semester_detail($model);
        $semester_detail = $db->getOne($query);
        $query = knjd194Query::getSemesterDetailMst($semester_detail);
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

        //学年
        $query = knjd194Query::getGrade($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");

        //上限値基準 1:履修上限 2:修得上限
        $opt = array(1, 2);
        $model->field["JOUGEN_DIV"] = ($model->field["JOUGEN_DIV"] == "") ? "1" : $model->field["JOUGEN_DIV"];
        $extra = array("id=\"JOUGEN_DIV1\"", "id=\"JOUGEN_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "JOUGEN_DIV", $model->field["JOUGEN_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //上限値選択 1:注意 2:超過
        $opt = array(1, 2);
        $model->field["OVER_DIV"] = ($model->field["OVER_DIV"] == "") ? "1" : $model->field["OVER_DIV"];
        $extra = array("id=\"OVER_DIV1\"", "id=\"OVER_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "OVER_DIV", $model->field["OVER_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********************/
        /* テキストボックス */
        /********************/
        //欠点
        $extra = " onBlur=\"return this.value=toInteger(this.value);\"";
        $model->field["KETTEN"] = $model->field["KETTEN"] ? $model->field["KETTEN"] : 30;
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $model->field["KETTEN"], "KETTEN", 3, 3, $extra);

        //プロパティーファイル(checkKettenDiv)で表示・非表示を切り替える
        if ($model->Properties["checkKettenDiv"]) {
            $arg["checkKettenDiv"] = '';
        } else {
            $arg["checkKettenDiv"] = '1';
        }

        /**********/
        /* ボタン */
        /**********/
        //プレビュー/印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //ＣＳＶボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
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
        knjCreateHidden($objForm, "PRGID",         "KNJD194");
        knjCreateHidden($objForm, "checkKettenDiv", $model->Properties["checkKettenDiv"]);
        knjCreateHidden($objForm, "chikokuHyoujiFlg", $model->Properties["chikokuHyoujiFlg"]);
        //学期
        knjCreateHidden($objForm, "SEME_DATE",  $seme);
        //学期開始日付
        knjCreateHidden($objForm, "SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
        //学期終了日付
        knjCreateHidden($objForm, "SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
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
        View::toHTML($model, "knjd194Form1.html", $arg);
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
?>
