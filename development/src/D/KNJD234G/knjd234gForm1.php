<?php

require_once('for_php7.php');

class knjd234gForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd234gForm1", "POST", "knjd234gindex.php", "", "knjd234gForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        /**********/
        /* コンボ */
        /**********/
        //学年
        $query = knjd234gQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");

        //学期
        $query = knjd234gQuery::getSemester($model);
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
        $query = knjd234gQuery::getTest($model);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->test_cd == $row["VALUE"]) $value_flg = true;
        }
        $model->test_cd = ($model->test_cd && $value_flg) ? $model->test_cd : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('main');\"";
        $arg["data"]["TEST_CD"] = knjCreateCombo($objForm, "TEST_CD", $model->test_cd, $opt, $extra, 1);

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

        //順位の基準点 1:総計 2:平均点
        $model->field["OUTPUT_KIJUN"] = $model->field["OUTPUT_KIJUN"] ? $model->field["OUTPUT_KIJUN"] : '1';
        $opt_kijun = array(1, 2);
        $extra = array("id=\"OUTPUT_KIJUN1\"", "id=\"OUTPUT_KIJUN2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KIJUN", $model->field["OUTPUT_KIJUN"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計開始日付
        $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]];//日付がない場合、学期開始日付を使用する。
        $sDate = str_replace("-", "/", $sDate);
        //累計の場合、出欠集計範囲の開始日は、学期詳細マスタの詳細コード＝１の開始日とする。
        if ($model->field["DATE_DIV"] == "1") {
            $query = knjd234gQuery::getSemesterDetailMst("1");
            $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (strlen($result["SDATE"])) {
                $sDate = $result["SDATE"];
            }
            $sDate = str_replace("-", "/", $sDate);
        }
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        $query = knjd234gQuery::get_semester_detail($model);
        $semester_detail = $db->getOne($query);
        $query = knjd234gQuery::getSemesterDetailMst($semester_detail);
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
        /* テキストボックス */
        /********************/
        
        //要追指導
        $extra = " onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
        //項目表示
        //初期値セット
//        if (!$model->field["SHIDOU_TENSU_INF2"]) {
//            $query = knjd234gQuery::getAssessHigh();
//            $result = $db->getRow($query, DB_FETCHMODE_ASSOC);
//            $model->field["SHIDOU_TENSU_INF2"] = $result["ASSESSHIGH"] ? $result["ASSESSHIGH"] : "39";
//        }
        if (!$model->field["FUSHIN_SCORE"]) $model->field["FUSHIN_SCORE"] = "40";
        $arg["data"]["FUSHIN_SCORE"] = knjCreateTextBox($objForm, $model->field["FUSHIN_SCORE"], "FUSHIN_SCORE", 2, 2, $extra);
        if (!$model->field["FUSHIN_SU"]) $model->field["FUSHIN_SU"] = "2";
        $arg["data"]["FUSHIN_SU"] = knjCreateTextBox($objForm, $model->field["FUSHIN_SU"], "FUSHIN_SU", 2, 2, $extra);
        if (!$model->field["FUSHIN_HEIKIN"]) $model->field["FUSHIN_HEIKIN"] = "55";
        $arg["data"]["FUSHIN_HEIKIN"] = knjCreateTextBox($objForm, $model->field["FUSHIN_HEIKIN"], "FUSHIN_HEIKIN", 2, 2, $extra);
        
        //出欠状況
        $extra = " onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
        if (!$model->field["KESSEKI"]) $model->field["KESSEKI"] = "1";
        $arg["data"]["KESSEKI"] = knjCreateTextBox($objForm, $model->field["KESSEKI"], "KESSEKI", 2, 2, $extra);
        if (!$model->field["CHIKOKU"]) $model->field["CHIKOKU"] = "1";
        $arg["data"]["CHIKOKU"] = knjCreateTextBox($objForm, $model->field["CHIKOKU"], "CHIKOKU", 2, 2, $extra);
        if (!$model->field["SOUTAI"]) $model->field["SOUTAI"] = "1";
        $arg["data"]["SOUTAI"] = knjCreateTextBox($objForm, $model->field["SOUTAI"], "SOUTAI", 2, 2, $extra);
        
        //優良者、不振者
        $model->field["YURYO"] = $model->field["YURYO"] ? $model->field["YURYO"] : "40";
        $arg["data"]["YURYO"] = knjCreateTextBox($objForm, $model->field["YURYO"], "YURYO", 3, 3, $extra);
        $model->field["FUSHIN"] = $model->field["FUSHIN"] ? $model->field["FUSHIN"] : "40";
        $arg["data"]["FUSHIN"] = knjCreateTextBox($objForm, $model->field["FUSHIN"], "FUSHIN", 3, 3, $extra);
        //欠試者は除く
        $extra = "id=\"KESSHI_NOZOKU\" ".($model->field["KESSHI_NOZOKU"] == "1" ? " checked " : "");
        $arg["data"]["KESSHI_NOZOKU"] = knjCreateCheckBox($objForm, "KESSHI_NOZOKU", "1", $extra);

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
        knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",         "KNJD234G");
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

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
        View::toHTML($model, "knjd234gForm1.html", $arg);
    }
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
