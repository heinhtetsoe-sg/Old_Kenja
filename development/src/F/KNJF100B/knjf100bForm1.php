<?php

require_once('for_php7.php');

class knjf100bForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjf100bForm1", "POST", "knjf100bindex.php", "", "knjf100bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->year;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjf100bQuery::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjf100b');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            //学校名称2表示
            $schoolname2 = $db->getOne(knjf100bQuery::getSchoolName2($model));
            $arg["SCH_NAME2"] = (strlen($schoolname2) > 0) ? "<<".$schoolname2.">>" : "";
        }

        /**********/
        /* コンボ */
        /**********/
        //学期コンボボックスを作成する
        $query = knjf100bQuery::getSemester($model);
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onchange=\"return btn_submit('gakki');\"", 1);

        //教科コンボ
        $query = knjf100bQuery::getClassMst($model);
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], "onChange=\"return btn_submit('knjf100b');\"", 1);

        //科目コンボ
        $query = knjf100bQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], "onChange=\"return btn_submit('knjf100b');\"", 1);

        //対象日
        $model->field["DATE"] = ($model->field["DATE"]) ? str_replace("-", "/", $model->field["DATE"]) : str_replace("-", "/", CTRL_DATE);
        $arg["el"]["DATE"] = View::popUpCalendar2($objForm, "DATE", $model->field["DATE"], "reload=true", " btn_submit('knjf100b')", "");

        //講座コンボ
        $query = knjf100bQuery::getChairDat($model);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], "onChange=\"return btn_submit('knjf100b');\"", 1);

        //一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //生徒項目名切替処理
        $sch_label = "";
        //テーブルの有無チェック
        $query = knjf100bQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && (($model->Properties["useCurriculumcd"] == '1' && $model->field["CLASSCD"]) || ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $sch_label = $db->getOne(knjf100bQuery::getSchName($model));
        }
        $arg["SCH_LABEL"] = (strlen($sch_label) > 0) ? $sch_label : '生徒';

        //来室日付範囲
        $model->field["DATE1"] = ($model->field["DATE1"]) ? str_replace("-", "/", $model->field["DATE1"]) : str_replace("-", "/", CTRL_DATE);
        $model->field["DATE2"] = ($model->field["DATE2"]) ? str_replace("-", "/", $model->field["DATE2"]) : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm, "DATE1", $model->field["DATE1"]);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm, "DATE2", $model->field["DATE2"]);

        /**********/
        /* ボタン */
        /**********/
        //プレビュー
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CHK_SDATE", CTRL_YEAR.'/04/01');
        knjCreateHidden($objForm, "CHK_EDATE", (CTRL_YEAR+1).'/03/31');
        /**********/
        knjCreateHidden($objForm, "ATTENDCLASSCD");
        /**********/
        knjCreateHidden($objForm, "PRGID", "KNJF100B");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useFormNameF100BC_1", $model->Properties["useFormNameF100BC_1"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf100bForm1.html", $arg); 
    }
}

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
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $opt = array();
    $result = $db->query(knjf100bQuery::getChairStdDat($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[]= array('label' => "{$row["LABEL"]}",
                      'value' => "{$row["VALUE"]}"
                     );
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:320px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["STUDENT_NAME"] = knjCreateCombo($objForm, "STUDENT_NAME", "", $opt, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:320px;\" ondblclick=\"move1('right')\"";
    $arg["data"]["STUDENT_SELECTED"] = knjCreateCombo($objForm, "STUDENT_SELECTED", "", array(), $extra, 15);

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
