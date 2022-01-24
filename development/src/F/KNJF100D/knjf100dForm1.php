<?php

require_once('for_php7.php');


class knjf100dForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjf100dForm1", "POST", "knjf100dindex.php", "", "knjf100dForm1");

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjf100dQuery::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjf100d');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);
        }

        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            //学校名称2表示
            $schoolname2 = $db->getOne(knjf100dQuery::getSchoolName2($model));
            $arg["SCH_NAME2"] = (strlen($schoolname2) > 0) ? "<<".$schoolname2.">>" : "";
        }

        //委員会コンボボックスを作成する
        $query = knjf100dQuery::getCommitteeList($model);
        $extra = "onChange=\"return btn_submit('knjf100d');\"";
        makeCmb($objForm, $arg, $db, $query, "COMMITTEE", $model->field["COMMITTEE"], $extra, "1");

        //生徒一覧リスト
        makeStudentList($objForm, $arg, $db, $model);

        //生徒項目名切替処理
        $sch_label = "";
        //テーブルの有無チェック
        $query = knjf100dQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && (($model->Properties["useCurriculumcd"] == '1' && $model->field["CLASSCD"]) || ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $sch_label = $db->getOne(knjf100dQuery::getSchName($model));
        }
        $arg["SCH_LABEL"] = (strlen($sch_label) > 0) ? $sch_label : '生徒';

        //来室日付範囲
        $model->field["DATE1"] = ($model->field["DATE1"]) ? str_replace("-", "/", $model->field["DATE1"]) : str_replace("-", "/", CTRL_DATE);
        $model->field["DATE2"] = ($model->field["DATE2"]) ? str_replace("-", "/", $model->field["DATE2"]) : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm, "DATE1", $model->field["DATE1"]);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm, "DATE2", $model->field["DATE2"]);

        //印刷ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf100dForm1.html", $arg);
    }
}

//生徒一覧リスト作成
function makeStudentList(&$objForm, &$arg, $db, &$model) {
    $right = $left = array();
    //一覧リストを作成する
    $query = knjf100dQuery::getStudent($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $right[] = array('label' => $row["LABEL"],
                         'value' => $row["VALUE"]);
    }
    $result->free();

    //一覧リストを作成する
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
    $arg["data"]["STUDENT_NAME"] = knjCreateCombo($objForm, "STUDENT_NAME", "", $right, $extra, 20);

    //出力対象教科リストを作成する
    $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
    $arg["data"]["STUDENT_SELECTED"] = knjCreateCombo($objForm, "STUDENT_SELECTED", "", $left, $extra, 20);

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

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //プレビュー／印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CHK_SDATE", CTRL_YEAR.'/04/01');
    knjCreateHidden($objForm, "CHK_EDATE", (CTRL_YEAR+1).'/03/31');
    knjCreateHidden($objForm, "PRGID", "KNJF100D");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
    knjCreateHidden($objForm, "useFormNameF100BC_1", $model->Properties["useFormNameF100BC_1"]);
}
?>
