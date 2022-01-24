<?php

require_once('for_php7.php');

class knjd234uForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd234uForm1", "POST", "knjd234uindex.php", "", "knjd234uForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        /**********/
        /* コンボ */
        /**********/
        //学年
        $query = knjd234uQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");
        
        //学期
        $query = knjd234uQuery::getSemester($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }
        //コース名
        $query = knjd234uQuery::getCourse($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "COURSE", $model->field["COURSE"], $extra, 1);
        
        //学校校種を取得
        $model->gradeCd = $db->getOne(knjd234uQuery::getGradeCdQuery($model->field["GRADE"]));
        knjCreateHidden($objForm, "setGradeCd", $model->gradeCd);
        
        //学校校種を取得
        $model->schoolKind = $db->getOne(knjd234uQuery::getSchoolkindQuery($model->field["GRADE"]));
        knjCreateHidden($objForm, "setSchoolKind", $model->schoolKind);

        $query = knjd234uQuery::getKaikinStudentRegdDat($model);
        if ("H" == $model->schoolKind && $model->gradeCd == '03') {
            $extra = "";
        } else {
            $extra = " disabled ";
        }
        makeCmb($objForm, $arg, $db, $query, "SCHREGNO_KAIKIN6", $model->field["SCHREGNO_KAIKIN6"], $extra, 1, "");

        if ($model->gradeCd == '03') {
            $extra = "";
        } else {
            $extra = " disabled ";
        }
        makeCmb($objForm, $arg, $db, $query, "SCHREGNO_KAIKIN3", $model->field["SCHREGNO_KAIKIN3"], $extra, 1, "");

        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SCHREGNO_KAIKIN1", $model->field["SCHREGNO_KAIKIN1"], $extra, 1, "");

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

        //出欠集計開始日付
        if ($model->field["DATE_DIV"] == '2') {
            $sDate = str_replace("-", "/", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
        } else {
            $sDate = str_replace("-", "/", $model->control["学期開始日付"]["1"]);
        }
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        if ($model->field["EDATE"] == '') {
            $eDate = CTRL_DATE; //日付がない場合、学籍処理日を使用する。
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
        
//		//優良者
//        $extra = " onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
//		$model->field["YURYO"] = $model->field["YURYO"] ? $model->field["YURYO"] : "40";
//        $arg["data"]["YURYO"] = knjCreateTextBox($objForm, $model->field["YURYO"], "YURYO", 3, 3, $extra);

//		//不振者
//        $extra = " onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
//		$model->field["FUSHIN"] = $model->field["FUSHIN"] ? $model->field["FUSHIN"] : "40";
//        $arg["data"]["FUSHIN"] = knjCreateTextBox($objForm, $model->field["FUSHIN"], "FUSHIN", 3, 3, $extra);

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
        knjCreateHidden($objForm, "PRGID",         "KNJD234U");
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);

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
        View::toHTML($model, "knjd234uForm1.html", $arg);
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

    if ($name == "COURSE") {
        $opt[] = array("label" => "全て", "value" => "ALL");
        if ($value == "ALL") $value_flg = true;
    }

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
