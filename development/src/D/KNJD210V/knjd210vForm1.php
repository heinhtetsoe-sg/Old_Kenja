<?php

require_once('for_php7.php');


//ビュー作成用クラス
class knjd210vForm1
{
    public function main(&$model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        $objForm = new form();

        $arg["start"]   = $objForm->get_start("main", "POST", "knjd210vindex.php", "", "main");

        //権限チェック:更新可
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR . "年度";

        //処理学期
        $extra = "onChange=\"btn_submit('');\" ";
        $query = knjd210vQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->seme, "SEMESTER", $extra, 1, "");

        //学期開始終了日付（講座基準日チェック用）
        $query = knjd210vQuery::getSemester($model->seme);
        $semeRow = array();
        $semeRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //課程学科コンボ
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["use_school_detail_gcm_dat"] = 1;
            $extra = "onChange=\"btn_submit('')\";";
            $query = knjd210vQuery::getCourseMajor($model);
            makeCmb($objForm, $arg, $db, $query, $model->course_major, "COURSE_MAJOR", $extra, 1, "");
        }

        //処理学年
        $extra = "onChange=\"btn_submit('');\" ";
        $query = knjd210vQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "");

        //学校種別
        $query = knjd210vQuery::getGrade($model, $model->grade);
        $gradeRow = array();
        $gradeRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "SCHOOL_KIND", $gradeRow["SCHOOL_KIND"]);

        //処理種別(成績)
        $extra = "onChange=\"btn_submit('');\" ";
        $query = knjd210vQuery::getName($model, $model->seme);
        makeCmb($objForm, $arg, $db, $query, $model->exam, "EXAM", $extra, 1, "");

        //講座基準日
        if ($model->chairdate == "") {
            $model->chairdate = str_replace("-", "/", CTRL_DATE);
        }
        $arg["CHAIRDATE"] = View::popUpCalendar($objForm, "CHAIRDATE", $model->chairdate);

        //科目コンボ
        if ($model->Properties["knjd210v_notUsetSubclassCombo"] == '1') {
            //プロパティー「knjd210v_notUsetSubclassCombo = 1」の時は、科目コンボはなし
            //処理は、全科目を選択した状態で、処理
            $arg["knjd210v_notUsetSubclassCombo"] = "";
            $model->subclasscd = "000000";
            knjCreateHidden($objForm, "SUBCLASSCD", $model->subclasscd);
        } else {
            $arg["knjd210v_notUsetSubclassCombo"] = 1;
            $extra = "onChange=\"disElectdiv();\" ";
            $query = knjd210vQuery::getSubclasscd($model, $gradeRow["SCHOOL_KIND"]);
            makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1, "");
        }

        //選択科目
        $check  = ($model->electdiv == "1") ? "checked" : "" ;
        $check .= " id=\"ELECTDIV\"";
        $check .= ($model->subclasscd == "999999" || $model->subclasscd == "000000") ? "" : " disabled";
        $arg["ELECTDIV"] = knjCreateCheckBox($objForm, "ELECTDIV", "1", $check, "");

        //評定マスタ作成
        knjCreateHidden($objForm, "knjd210v_useRelativeAssessCheckbox", $model->Properties["knjd210v_useRelativeAssessCheckbox"]);
        if ($model->Properties["knjd210v_useRelativeAssessCheckbox"] == '1') {
            $arg["knjd210v_useRelativeAssessCheckbox"] = 1;

            $extra  = ($model->assess_chk == "1") ? "checked" : "";
            $extra .= " id=\"ASSESS_CHK\"";
            $extra .= ($model->seme == "9" && $model->exam == "99-00-08" && $model->subclasscd != "999999") ? "" : " disabled";
            $arg["ASSESS_CHK"] = knjCreateCheckBox($objForm, "ASSESS_CHK", "1", $extra, "");
        }

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semeRow["SDATE"]));
        knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semeRow["EDATE"]));

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd210vForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();

    if ($name == "SUBCLASSCD" && AUTHORITY == DEF_UPDATABLE) {
        $opt[] = array("label" => "全て", "value" => "000000");
    } elseif ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SUBCLASSCD" && AUTHORITY == DEF_UPDATABLE) {
        $opt[] = array("label" => "総合計（３・５・９）", "value" => "999999");
        if ($value == "000000") {
            $value_flg = true;
        }
        if ($value == "999999") {
            $value_flg = true;
        }
    }

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//履歴表示
function makeListRireki(&$objForm, &$arg, $db, &$model)
{
    //履歴一覧
    $query = knjd210vQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-", "/", $row["CALC_DATE"]);
        $row["CHAIRDATE"] = str_replace("-", "/", $row["CHAIRDATE"]);
        $row["ELECTDIV_FLG"] = $row["ELECTDIV_FLG"] == "1" ? "レ" : "";
        $subArray = array();
        $subArray = explode("-", $row["SUBCLASSCD"]);
        if ($subArray[3] == "000000" || $subArray[3] == "ALL") {
            $row["SUBCLASSNAME"] = "全て";
        } elseif ($subArray[3] == "999999") {
            $row["SUBCLASSNAME"] = "総合計（３・５・９）";
        } else {
            $row["SUBCLASSNAME"] = $row["SUBCLASSCD"].":".$row["SUBCLASSNAME"];
        }
        $arg['data2'][] = $row;
    }
    $result->free();
}
