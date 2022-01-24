<?php

require_once('for_php7.php');

class knjd192vForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd192vForm1", "POST", "knjd192vindex.php", "", "knjd192vForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $query = knjd192vQuery::getSchoolname();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //クラス・個人ラジオボタン 1:クラス選択 2:個人選択
        $opt_div = array(1, 2);
        if ($model->field["CATEGORY_IS_CLASS"] == "") {
            $model->field["CATEGORY_IS_CLASS"] = "1";
        }
        $extra = array("id=\"CATEGORY_IS_CLASS1\" onClick=\"return btn_submit('knjd192v')\"", "id=\"CATEGORY_IS_CLASS2\" onClick=\"return btn_submit('knjd192v')\"");
        $radioArray = knjCreateRadio($objForm, "CATEGORY_IS_CLASS", $model->field["CATEGORY_IS_CLASS"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計範囲（累計・学期）ラジオボタン 1:累計 2:学期
        if ($model->field["DATE_DIV"] == "") {
            $model->field["DATE_DIV"] = $model->Properties["knjd192vDefaultAttendDateDiv"] ? $model->Properties["knjd192vDefaultAttendDateDiv"] : "1";
        }
        $opt_datediv = array(1, 2);
        $extra2 = " onclick=\"return btn_submit('knjd192v');\"";
        $extra = array("id=\"DATE_DIV1\"".$extra2, "id=\"DATE_DIV2\"".$extra2);
        $radioArray = knjCreateRadio($objForm, "DATE_DIV", $model->field["DATE_DIV"], $extra, $opt_datediv, get_count($opt_datediv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd192vQuery::getSemester();
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //出欠集計開始日付
        $query = knjd192vQuery::getSemesterDetail($value);
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
            $query = knjd192vQuery::getSemesterDetailMst(CTRL_YEAR, "1");
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
        $query = knjd192vQuery::getSemesterDetail($value);
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

        //学年コンボ作成
        $query = knjd192vQuery::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        if ($model->field["CATEGORY_IS_CLASS"] == 2){
            //クラスコンボ作成
            $query = knjd192vQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
            $extra = "onchange=\"return btn_submit('knjd192v'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);
        }

        //テストコンボ作成
        $query = knjd192vQuery::getTest($model->field["SEMESTER"], $model->field["GRADE"]);
        $opt = array();
        $value = $model->field["SUB_TESTCD"];
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        $value = ($value) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["SUB_TESTCD"] = knjCreateCombo($objForm, "SUB_TESTCD", $value, $opt, $extra, 1);

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //平均・席次・偏差値ラジオボタン 1:学級・学年  2:学級・コース  3:講座・学年  4:講座・コース  5:学級・学科
        $opt_group = array(1, 2, 3, 4, 5); //クラスのラジオはHMTLの中からカットしたが帳票に送る値の関係でPHPの中には残す
        if ($model->field["GROUP_DIV"] == "") {
            $model->field["GROUP_DIV"] = $model->Properties["knjd192vDefaultGroupDiv"] ? $model->Properties["knjd192vDefaultGroupDiv"] : "1";
        }
        $extra = array("id=\"GROUP_DIV1\"", "id=\"GROUP_DIV2\"", "id=\"GROUP_DIV3\"", "id=\"GROUP_DIV4\"", "id=\"GROUP_DIV5\"");
        $radioArray = knjCreateRadio($objForm, "GROUP_DIV", $model->field["GROUP_DIV"], $extra, $opt_group, get_count($opt_group));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        if ($model->field["OUTPUT_KIJUN"] == "") {
            $model->field["OUTPUT_KIJUN"] = $model->Properties["knjd192vDefaultOutputKijun"] ? $model->Properties["knjd192vDefaultOutputKijun"] : "1";
        }
        $opt_kijun = array(1, 2);
        $extra = array("id=\"OUTPUT_KIJUN1\"", "id=\"OUTPUT_KIJUN2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KIJUN", $model->field["OUTPUT_KIJUN"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //欠席を欠点科目にカウントする
        if ($model->field["COUNT_SURU"] == "on") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= "id=\"COUNT_SURU\"";
        $arg["data"]["COUNT_SURU"] = knjCreateCheckBox($objForm, "COUNT_SURU", "on", $extra);

        //欠点
        $testkind = substr($value, 0, 4);
        if (($model->Properties["checkKettenDiv"] != '1' && $model->Properties["checkKettenDiv"] != '2') || 
            ($model->Properties["checkKettenDiv"] == '1' && $model->field["SEMESTER"] == '9') || 
            ($model->Properties["checkKettenDiv"] == '1' && $model->field["SEMESTER"] != '9' && ($testkind == '9900' || $testkind == '9901') && $model->useSlumpD048 != '1')) {
            $arg["KETTEN_FLG"] = '1'; //null以外なら何でもいい
        } else {
            unset($arg["KETTEN_FLG"]);
        }
        //「欠点(評価)は、不振チェック参照するか？」の判定
        if (($model->Properties["checkKettenDiv"] == '1' && $model->field["SEMESTER"] != '9' && ($testkind == '9900' || $testkind == '9901') && $model->useSlumpD048 == '1')) {
            $arg["USE_SLUMP_D048"] = '1'; //null以外なら何でもいい
        } else {
            unset($arg["USE_SLUMP_D048"]);
        }
        //欠点
        if ($model->cmd == 'change_grade' || $model->cmd == '') {
            $query = knjd192vQuery::getGdat($model->field["GRADE"]);
            $h_j = $db->getOne($query);
            if ($h_j == 'J') {
                $model->field["KETTEN"] = 60;
            } else {
                $model->field["KETTEN"] = 30;
            }
            if ($model->Properties["checkKettenDiv"] == '1') {
                $model->field["KETTEN"] = ($model->field["SEMESTER"] == '9' && $testkind == '9900') ? 1 : 2;
            }
        }
        $extra = "onblur=\"calc(this);\"";
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $model->field["KETTEN"], "KETTEN", 3, 3, $extra);

        //最大科目数
        $opt = array(1, 2);
        if ($model->field["SUBCLASS_MAX"] == "") {
            $model->field["SUBCLASS_MAX"] = $model->Properties["knjd192vDefaultSubclassMax"] ? $model->Properties["knjd192vDefaultSubclassMax"] : "1";
        }
        $extra = array("id=\"SUBCLASS_MAX1\"", "id=\"SUBCLASS_MAX2\"");
        $radioArray = knjCreateRadio($objForm, "SUBCLASS_MAX", $model->field["SUBCLASS_MAX"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //注意・超過のタイトル
        $arg["data"]["TYUI_TYOUKA_TITLE"] = "欠課数上限値（履修／修得）";

        //注意・超過ラジオ
        $opt = array(1, 2); //1:注意 2:超過
        $model->field["TYUI_TYOUKA"] = ($model->field["TYUI_TYOUKA"] == "") ? "1" : $model->field["TYUI_TYOUKA"];
        $extra = array("id=\"TYUI_TYOUKA1\"", "id=\"TYUI_TYOUKA2\"");
        $radioArray = knjCreateRadio($objForm, "TYUI_TYOUKA", $model->field["TYUI_TYOUKA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //試験科目のみ出力する
        if ($model->cmd == '' && $model->Properties["knjd192vNotDefaultTestOnly"] != "1") {
            $model->field["TEST_ONLY"] = "1";
        }
        $extra = $model->field["TEST_ONLY"] == "1" ? " checked " : "";
        $extra .= "id=\"TEST_ONLY\"";
        $arg["data"]["TEST_ONLY"] = knjCreateCheckBox($objForm, "TEST_ONLY", "1", $extra);

        //順位を出力しない
        if ($model->cmd == '') {
            $model->field["NOT_PRINT_RANK"] = $model->Properties["knjd192vDefaultNotPrintRank"];
        }
        $extra = $model->field["NOT_PRINT_RANK"] == "1" ? " checked " : "";
        $extra .= "id=\"NOT_PRINT_RANK\"";
        $arg["data"]["NOT_PRINT_RANK"] = knjCreateCheckBox($objForm, "NOT_PRINT_RANK", "1", $extra);

        //平均点を出力しない
        if ($model->cmd == '') {
            $model->field["NOT_PRINT_AVG"] = $model->Properties["knjd192vDefaultNotPrintAvg"];
        }
        $extra = $model->field["NOT_PRINT_AVG"] == "1" ? " checked " : "";
        $extra .= "id=\"NOT_PRINT_AVG\"";
        $arg["data"]["NOT_PRINT_AVG"] = knjCreateCheckBox($objForm, "NOT_PRINT_AVG", "1", $extra);

        //学校マスタ取得
        $schInfo = $db->getRow(knjd192vQuery::getSchoolMst($model), DB_FETCHMODE_ASSOC);

        //欠課数を出力しない
        if ($model->cmd == '') {
            $model->field["NOT_PRINT_KEKKA"] = $model->Properties["knjd192vDefaultNotPrintKekka"];
        }
        $extra = $model->field["NOT_PRINT_KEKKA"] == "1" ? " checked " : "";
        $extra .= "id=\"NOT_PRINT_KEKKA\"";
        if (in_array($schInfo["ABSENT_COV"], array('3','4'))) {
            $extra .= " onclick=\"OptionUse(this);\"";
        }
        $arg["data"]["NOT_PRINT_KEKKA"] = knjCreateCheckBox($objForm, "NOT_PRINT_KEKKA", "1", $extra);

        //欠課数換算が小数点ありの場合
        if (in_array($schInfo["ABSENT_COV"], array('3','4'))) {
            //整数で表示するチェックボックス
            $extra  = ($model->field["KEKKA_AS_INT"] == "1" || $model->cmd == '') ? " checked " : "";
            $extra .= " id=\"KEKKA_AS_INT\"";
            if ($model->field["NOT_PRINT_KEKKA"] == "1") $extra .= " disabled checked";
            $arg["data"]["KEKKA_AS_INT"] = knjCreateCheckBox($objForm, "KEKKA_AS_INT", "1", $extra);
        }

        //裁断用にソートして出力する
        $extra  = $model->field["SORT_CUTTING"] == "1" || ($model->cmd == "" && $model->Properties["knjd192vDefaultSortCutting"] == "1") ? " checked " : "";
        $extra .= " id=\"SORT_CUTTING\"";
        $arg["data"]["SORT_CUTTING"] = knjCreateCheckBox($objForm, "SORT_CUTTING", "1", $extra);

        //選択科目を後に出力する
        $extra = $model->field["ORDER_USE_ELECTDIV"] == "1" ? " checked " : "";
        $extra .= "id=\"ORDER_USE_ELECTDIV\"";
        $arg["data"]["ORDER_USE_ELECTDIV"] = knjCreateCheckBox($objForm, "ORDER_USE_ELECTDIV", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd192vForm1.html", $arg); 
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model) {

    $sort = 0;
    if ($model->field["CATEGORY_IS_CLASS"] == 1){
        //対象クラスリストを作成する
        $query = knjd192vQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
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
        $query = knjd192vQuery::getSchnoIdou($model, $model->field["SEMESTER"]);
        $result = $db->query($query);
        $opt_idou = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();

        //対象者リストを作成する
        $query = knjd192vQuery::getStudent($model, $model->field["SEMESTER"]);
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
    knjCreateHidden($objForm, "PRGID", "KNJD192V");
    knjCreateHidden($objForm, "FORMNAME");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCORE_FLG");
    knjCreateHidden($objForm, "TESTCD");
    knjCreateHidden($objForm, "CHK_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
    knjCreateHidden($objForm, "CHK_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
    knjCreateHidden($objForm, "checkKettenDiv", $model->Properties["checkKettenDiv"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "knjd192AcheckNoExamChair", $model->Properties["knjd192AcheckNoExamChair"]);
    knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
    knjCreateHidden($objForm, "notUseAttendSubclassSpecial", $model->Properties["notUseAttendSubclassSpecial"]);
    knjCreateHidden($objForm, "notUseKetten", $model->Properties["notUseKetten"]);
    knjCreateHidden($objForm, "printSubclassLastChairStd", $model->Properties["printSubclassLastChairStd"]);
    knjCreateHidden($objForm, "useSchoolMstSemesAssesscd", $model->Properties["useSchoolMstSemesAssesscd"]);
    knjCreateHidden($objForm, "knjd192vPrintClass90", $model->Properties["knjd192vPrintClass90"]);
}

?>
