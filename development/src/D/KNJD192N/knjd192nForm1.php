<?php

require_once('for_php7.php');

class knjd192nForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd192nForm1", "POST", "knjd192nindex.php", "", "knjd192nForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $query = knjd192nQuery::getSchoolname();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //クラス・個人ラジオボタン 1:クラス選択 2:個人選択
        $opt_div = array(1, 2);
        if ($model->field["CATEGORY_IS_CLASS"] == "") {
            $model->field["CATEGORY_IS_CLASS"] = "1";
        }
        $extra = array("id=\"CATEGORY_IS_CLASS1\" onClick=\"return btn_submit('knjd192n')\"", "id=\"CATEGORY_IS_CLASS2\" onClick=\"return btn_submit('knjd192n')\"");
        $radioArray = knjCreateRadio($objForm, "CATEGORY_IS_CLASS", $model->field["CATEGORY_IS_CLASS"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計範囲（累計・学期）ラジオボタン 1:累計 2:学期
        if ($model->field["DATE_DIV"] == "") {
            $model->field["DATE_DIV"] = $model->Properties["knjd192nDefaultAttendDateDiv"] ? $model->Properties["knjd192nDefaultAttendDateDiv"] : "1";
        }
        $opt_datediv = array(1, 2);
        $extra2 = " onclick=\"return btn_submit('knjd192n');\"";
        $extra = array("id=\"DATE_DIV1\"".$extra2, "id=\"DATE_DIV2\"".$extra2);
        $radioArray = knjCreateRadio($objForm, "DATE_DIV", $model->field["DATE_DIV"], $extra, $opt_datediv, get_count($opt_datediv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd192nQuery::getSemester();
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //出欠集計開始日付
        $query = knjd192nQuery::getSemesterDetail($value);
        $result = $db->query($query);
        $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]];//日付がない場合、学期開始日付を使用する。
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sDate = $row["SDATE"];//学期詳細マスタの終了日付
        }
        $result->free();
        $sDate = str_replace("-", "/", $sDate);
        //日付が学期の範囲外の場合、学期開始日付を使用する。
        if ($sDate < $model->control["学期開始日付"][$model->field["SEMESTER"]] || 
            $sDate > $model->control["学期終了日付"][$model->field["SEMESTER"]]) {
            $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]];
        }
        //累計の場合、出欠集計範囲の開始日は、学期詳細マスタの詳細コード＝１の開始日とする。
        if ($model->field["DATE_DIV"] == "1") {
            $query = knjd192nQuery::getSemesterDetailMst(CTRL_YEAR, "1");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sDate = $row["SDATE"];
            }
            $result->free();
            $sDate = str_replace("-", "/", $sDate);
        }
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        $query = knjd192nQuery::getSemesterDetail($value);
        $result = $db->query($query);
        $eDate = CTRL_DATE;//日付がない場合、学籍処理日を使用する。
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eDate = $row["EDATE"];//学期詳細マスタの終了日付
        }
        $result->free();
        $eDate = str_replace("-", "/", $eDate);
        //日付が学期の範囲外の場合、学期終了日付を使用する。
        if ($eDate < $model->control["学期開始日付"][$model->field["SEMESTER"]] || 
            $eDate > $model->control["学期終了日付"][$model->field["SEMESTER"]]) {
            $eDate = $model->control["学期終了日付"][$model->field["SEMESTER"]];
        }
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "DATE", $eDate);

        //最大科目数選択ラジオボタン 1:15科目 2:24科目
        $opt_div = array(1, 2);
        if ($model->field["OUTPUT_SUBCLASS"] == "") {
            $model->field["OUTPUT_SUBCLASS"] = "1";
        }
        $extra = array("id=\"OUTPUT_SUBCLASS1\" onClick=\"return btn_submit('knjd192n')\"", "id=\"OUTPUT_SUBCLASS2\" onClick=\"return btn_submit('knjd192n')\"");
        $radioSubclsArray = knjCreateRadio($objForm, "OUTPUT_SUBCLASS", $model->field["OUTPUT_SUBCLASS"], $extra, $opt_div, get_count($opt_div));
        foreach($radioSubclsArray as $key => $val) $arg["data"][$key] = $val;

        //学年コンボ作成
        $query = knjd192nQuery::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        if ($model->field["CATEGORY_IS_CLASS"] == 2){
            //クラスコンボ作成
            $query = knjd192nQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
            $extra = "onchange=\"return btn_submit('knjd192n'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);
        }

        //テストコンボ作成
        $query = knjd192nQuery::getTest($model->field["SEMESTER"], $model->field["GRADE"]);
        $opt = array();
        $value = $model->field["SUB_TESTCD"];
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        $value = ($value) ? $value : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        $arg["data"]["SUB_TESTCD"] = knjCreateCombo($objForm, "SUB_TESTCD", $value, $opt, $extra, 1);

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd192nForm1.html", $arg); 
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model) {

    $sort = 0;
    if ($model->field["CATEGORY_IS_CLASS"] == 1){
        //対象クラスリストを作成する
        $query = knjd192nQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        $arg["data"]["NAME_LIST"] = 'クラス一覧';
        $sort = 0;
    }else {

        //対象外の生徒取得
        $query = knjd192nQuery::getSchnoIdou($model, $model->field["SEMESTER"]);
        $result = $db->query($query);
        $opt_idou = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();

        //対象者リストを作成する
        $query = knjd192nQuery::getStudent($model, $model->field["SEMESTER"]);
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $idou = "　";
            if (in_array($row["SCHREGNO"],$opt_idou)) {
                $idou = "●";
            }
            $opt1[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                            'value' => $row["SCHREGNO"]);
        }
        $result->free();
        $arg["data"]["NAME_LIST"] = '生徒一覧';

        $sort = 1;
    }
    $extra = "multiple style=\"height:130px;width:230px;\" ondblclick=\"move1('left', ".$sort.")\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);


    //出力対象一覧リストを作成する//
    $extra = "multiple style=\"height:130px;width:230px;\" ondblclick=\"move1('right', ".$sort.")\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //extra
    $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right', ".$sort.");\"";
    $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left', ".$sort.");\"";
    $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right', ".$sort.");\"";
    $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left', ".$sort.");\"";

    //対象選択ボタンを作成する
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra_rights);
    //対象取消ボタンを作成する
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra_lefts);
    //対象選択ボタンを作成する
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra_right1);
    //対象取消ボタンを作成する
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra_left1);
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $sp = ($name == "HR_CLASS") ? "&nbsp;&nbsp;&nbsp;" : "";
    $arg["data"][$name] = $sp.knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD192N");
    knjCreateHidden($objForm, "FORMNAME");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCORE_FLG");
    knjCreateHidden($objForm, "TESTCD");
    knjCreateHidden($objForm, "CHK_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
    knjCreateHidden($objForm, "CHK_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "knjd192AcheckNoExamChair", $model->Properties["knjd192AcheckNoExamChair"]);
    knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
    knjCreateHidden($objForm, "notUseAttendSubclassSpecial", $model->Properties["notUseAttendSubclassSpecial"]);
}

?>
