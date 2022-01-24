<?php

require_once('for_php7.php');

class knjd618Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd618Form1", "POST", "knjd618index.php", "", "knjd618Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $query = knjd618Query::getSchoolname();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //クラス・個人ラジオボタン 1:クラス選択 2:個人選択
        $opt_div = array(1, 2);
        $model->field["CATEGORY_IS_CLASS"] = ($model->field["CATEGORY_IS_CLASS"] == "") ? "1" : $model->field["CATEGORY_IS_CLASS"];
        $extra = array("id=\"CATEGORY_IS_CLASS1\" onClick=\"return btn_submit('knjd618')\"", "id=\"CATEGORY_IS_CLASS2\" onClick=\"return btn_submit('knjd618')\"");
        $radioArray = knjCreateRadio($objForm, "CATEGORY_IS_CLASS", $model->field["CATEGORY_IS_CLASS"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計範囲（累計・学期）ラジオボタン 1:累計 2:学期
        $model->field["DATE_DIV"] = $model->field["DATE_DIV"] ? $model->field["DATE_DIV"] : '1';
        $opt_datediv = array(1, 2);
        $extra2 = " onclick=\"return btn_submit('knjd618');\"";
        $extra = array("id=\"DATE_DIV1\"".$extra2, "id=\"DATE_DIV2\"".$extra2);
        $radioArray = knjCreateRadio($objForm, "DATE_DIV", $model->field["DATE_DIV"], $extra, $opt_datediv, get_count($opt_datediv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd618Query::getSemester();
        $extra = "onchange=\"return btn_submit('knjd618'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        if (!$model->field["SEMESTER"]) {
            $model->field["SEMESTER"] = CTRL_SEMESTER;
        }

        //テストコンボ作成
        $query = knjd618Query::getTest($model->field["SEMESTER"]);
        $extra = "onchange=\"return btn_submit('knjd618');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], $extra, 1);

        //学年コンボ作成
        $query = knjd618Query::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('knjd618'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        if ($model->field["CATEGORY_IS_CLASS"] == 2) {
            //クラスコンボ作成
            $query = knjd618Query::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
            $extra = "onchange=\"return btn_submit('knjd618'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);
        }

        //出欠集計範囲
        makeSyukeiHani($objForm, $arg, $db, $model, $model->field["TESTCD"], $schoolCode);

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //平均・席次・偏差値ラジオボタン 1:学年 2:クラス 3:コース
        $opt_group = array(1, 2, 3);
        $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
        $extra = array("id=\"GROUP_DIV1\"", "id=\"GROUP_DIV2\"", "id=\"GROUP_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "GROUP_DIV", $model->field["GROUP_DIV"], $extra, $opt_group, get_count($opt_group));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //注意・超過のタイトル
        $arg["data"]["TYUI_TYOUKA_TITLE"] = "欠課数上限値（履修／修得）";

        //注意・超過ラジオ
        $opt = array(1, 2); //1:注意 2:超過
        $model->field["TYUI_TYOUKA"] = ($model->field["TYUI_TYOUKA"] == "") ? "1" : $model->field["TYUI_TYOUKA"];
        $extra = array("id=\"TYUI_TYOUKA1\"", "id=\"TYUI_TYOUKA2\"");
        $radioArray = knjCreateRadio($objForm, "TYUI_TYOUKA", $model->field["TYUI_TYOUKA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd618Form1.html", $arg); 
    }
}
/************************************************************************************************************************************/
/********************************************************* 以下関数 *****************************************************************/
/************************************************************************************************************************************/
/****************/
/* 出欠集計範囲 */
/****************/
function makeSyukeiHani(&$objForm, &$arg, $db, $model, $testCD, $schoolCode) {
        //出欠集計開始日付
        $seme_kind = $model->field["SEMESTER"] . $testCD;
        $semesterDetailS = $model->semesterDetailS[$schoolCode][$seme_kind];
        $query = knjd618Query::getSemesterDetailMst(CTRL_YEAR, $semesterDetailS);
        $result = $db->query($query);
        $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]]; //日付がない場合、学期開始日付を使用する。
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
            $query = knjd618Query::getSemesterDetailMst(CTRL_YEAR, "1");
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
        $seme_kind = $model->field["SEMESTER"] . $testCD;
        $semesterDetailE = $model->semesterDetailE[$schoolCode][$seme_kind];
        $query = knjd618Query::getSemesterDetailMst(CTRL_YEAR, $semesterDetailE);
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
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $eDate);
}

/******************/
/* リストtoリスト */
/******************/
function makeStudentList(&$objForm, &$arg, $db, $model) {
    if ($model->field["CATEGORY_IS_CLASS"] == 1){
        //対象クラスリストを作成する
        $query = knjd618Query::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

        $arg["data"]["NAME_LIST"] = 'クラス一覧';

        //出力対象一覧リストを作成する//
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

        //extra
        $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";

    } else {

        //対象外の生徒取得
        $query = knjd618Query::getSchnoIdou($model, $model->field["SEMESTER"]);
        $result = $db->query($query);
        $opt_idou = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();

        //対象者リストを作成する
        $query = knjd618Query::getStudent($model, $model->field["SEMESTER"]);
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
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left', 1)\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

        $arg["data"]["NAME_LIST"] = '生徒一覧';

        //出力対象一覧リストを作成する//
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right', 1)\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

        //extra
        $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right', 1);\"";
        $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left', 1);\"";
        $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right', 1);\"";
        $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left', 1);\"";
    }

    //対象選択ボタンを作成する
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra_rights);
    //対象取消ボタンを作成する
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra_lefts);
    //対象選択ボタンを作成する
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra_right1);
    //対象取消ボタンを作成する
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra_left1);
}

/**************/
/* コンボ作成 */
/**************/
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "GRADE"){
            $opt[] = array('label' => sprintf("%d",$row["LABEL"]).'学年',
                           'value' => $row["VALUE"]);
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

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

/**********/
/* ボタン */
/**********/
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

/**********/
/* hidden */
/**********/
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD618");
    knjCreateHidden($objForm, "FORMNAME");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "IMAGEPATH", "image");
    knjCreateHidden($objForm, "CHK_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
    knjCreateHidden($objForm, "CHK_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
    knjCreateHidden($objForm, "chuouIkueiFormYear", $model->Properties["chuouIkueiFormYear"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
}
?>
