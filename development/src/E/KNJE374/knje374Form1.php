<?php

require_once('for_php7.php');

class knje374Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje374Form1", "POST", "knje374index.php", "", "knje374Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //パターン
        if ($model->Properties["knje374Pattern"] != "1") {
            $arg["knje374Pattern"] = "1";
        }

        //対象生徒選択ラジオボタン 1:在校生 2:卒業生
        $opt_std = array(1, 2);
        $model->field["STUDENT"] = ($model->field["STUDENT"] == "") ? "1" : $model->field["STUDENT"];
        $click = " onclick =\" return btn_submit('knje374');\"";
        $extra = array("id=\"STUDENT1\"".$click, "id=\"STUDENT2\"".$click);
        $radioArray = knjCreateRadio($objForm, "STUDENT", $model->field["STUDENT"], $extra, $opt_std, get_count($opt_std));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["STUDENT"] != "1") {
            //対象生徒：卒業生
            $arg["grd"] = 1;
            $arg["data"]["HEIGHT"] = "100";
        } else {
            //対象生徒：在校生
            $arg["regd"] = 1;
            $arg["data"]["HEIGHT"] = "100";
            $arg["schoolKind"] = 1;
        }

        //学校種別コンボ作成
        $query = knje374Query::getSchoolKind($model);
        $extra = "onchange=\"return btn_submit('knje374'),AllClearList();\"";
        if ($model->field["STUDENT"] == "2")  {
            $extra .= "disabled";
        }
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);

        //卒業年度コンボ作成
        $query = knje374Query::getSchoolYear($model);
        $extra = "onchange=\"return btn_submit('knje374'),AllClearList();\"";
        if ($model->field["STUDENT"] == "1")  {
            $extra .= "disabled";
        }
        makeCmb($objForm, $arg, $db, $query, "GRD_YEAR", $model->field["GRD_YEAR"], $extra, 1);

        //出身学校リストtoリストを作成する
        makeListToList($objForm, $arg, $db, $model);

        //学校区分
        $schoolName = $db->getOne(knje374Query::getNameMstZ010());
        $isKindai = ($schoolName == "KINDAI" || $schoolName == "KINJUNIOR") ? true : false;

        //学期・学年末成績コンボ作成
        $query = knje374Query::getSemester();
        $extra = ($isKindai || $model->Properties["useRecordDat"] = 'RECORD_SCORE_DAT') ? "onchange=\"return btn_submit('knje374'),AllClearList();\"" : "";
        makeCmb($objForm, $arg, $db, $query, "REC_SEMESTER", $model->field["REC_SEMESTER"], $extra, 1);

        //テストコンボ作成
        if ($isKindai || $model->Properties["useRecordDat"] = 'RECORD_SCORE_DAT') {
            $query = knje374Query::getTestkind($model, $isKindai, $model->field["REC_SEMESTER"]);
            makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], "", 1);
            $arg["show_test"] = 3;
        }

        if ($model->field["STUDENT"] == "1" && $model->Properties["useRecordDat"] == "RECORD_SCORE_DAT") {
            $arg["useOutputRadio"] = "1";
            $arg["data"]["HEIGHT"] = "100";
            //出力内容ラジオボタン 1:クラブ名・委員会名 2:科目別成績
            $opt_output = array(1, 2);
            $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
            $click = " onclick =\"OptionUse(this);\"";
            $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click);
            $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            //出欠集計範囲（開始日付）
            $model->field["ATTEND_SDATE"] = ($model->field["ATTEND_SDATE"] == "") ? str_replace("-", "/", $model->control["学期開始日付"][9]) : str_replace("-", "/", $model->field["ATTEND_SDATE"]);
            $extra = " disabled";
            $arg["data"]["ATTEND_SDATE"] = View::popUpCalendarAlp($objForm, "ATTEND_SDATE", $model->field["ATTEND_SDATE"], $extra);

            //出欠集計範囲（終了日付）
            $model->field["ATTEND_EDATE"] = ($model->field["ATTEND_EDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $model->field["ATTEND_EDATE"]);
            $extra = " disabled";
            $arg["data"]["ATTEND_EDATE"] = View::popUpCalendarAlp($objForm, "ATTEND_EDATE", $model->field["ATTEND_EDATE"], $extra);
        }

        //順位の基準点 1:総合点 2:平均点 3:偏差値
        $opt_standard = array(1, 2, 3);
        $model->field["STANDARD"] = ($model->field["STANDARD"] == "") ? "1" : $model->field["STANDARD"];
        $extra = array("id=\"STANDARD1\"", "id=\"STANDARD2\"", "id=\"STANDARD3\"");
        $radioArray = knjCreateRadio($objForm, "STANDARD", $model->field["STANDARD"], $extra, $opt_standard, get_count($opt_standard));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->Properties["useRecordDat"] == "RECORD_SCORE_DAT") {
            $arg["score_dat"] = 2;
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje374Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if($name == "GRD_YEAR"){
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//生徒リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    if($model->field["STUDENT"] == "1"){
        $query = knje374Query::getFinschoolUngrd($model);
    } else {
        $query = knje374Query::getFinschoolGrd($model);
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 20);

    //出身学校一覧リストを作成する
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('". SERVLET_URL ."');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE374");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useRecordDat", $model->Properties["useRecordDat"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "knje374Pattern", $model->Properties["knje374Pattern"]);
    knjCreateHidden($objForm, "useAssessCourseMst", $model->Properties["useAssessCourseMst"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "useTestCountflg",  $model->Properties["useTestCountflg"]);
}
?>
