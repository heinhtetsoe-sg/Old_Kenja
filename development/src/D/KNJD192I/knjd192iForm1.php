<?php

require_once('for_php7.php');

class knjd192iForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd192iForm1", "POST", "knjd192iindex.php", "", "knjd192iForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $query = knjd192iQuery::getSchoolname();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //クラス・個人ラジオボタン 1:クラス選択 2:個人選択
        $opt_div = array(1, 2);
        $model->field["CATEGORY_IS_CLASS"] = ($model->field["CATEGORY_IS_CLASS"] == "") ? "1" : $model->field["CATEGORY_IS_CLASS"];
        $extra = array("id=\"CATEGORY_IS_CLASS1\" onClick=\"return btn_submit('knjd192i')\"", "id=\"CATEGORY_IS_CLASS2\" onClick=\"return btn_submit('knjd192i')\"");
        $radioArray = knjCreateRadio($objForm, "CATEGORY_IS_CLASS", $model->field["CATEGORY_IS_CLASS"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd192iQuery::getSemester();
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //出欠集計開始日付
        $query = knjd192iQuery::getSemesterDetail($value);
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

        //学年コンボ作成
        $query = knjd192iQuery::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        if ($model->field["CATEGORY_IS_CLASS"] == 2){
            //クラスコンボ作成
            $query = knjd192iQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
            $extra = "onchange=\"return btn_submit('knjd192i'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);
        }

        //テストコンボ作成
        $query = knjd192iQuery::getTest($model->field["SEMESTER"], $model->field["GRADE"]);
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

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $model->field["OUTPUT_KIJUN"] = $model->field["OUTPUT_KIJUN"] ? $model->field["OUTPUT_KIJUN"] : '2';
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
//        if (($model->Properties["checkKettenDiv"] != '1' && $model->Properties["checkKettenDiv"] != '2') || 
//            ($model->Properties["checkKettenDiv"] == '1' && $model->field["SEMESTER"] == '9') || 
//            ($model->Properties["checkKettenDiv"] == '1' && $model->field["SEMESTER"] != '9' && ($testkind == '9900' || $testkind == '9901') && $model->useSlumpD048 != '1')) {
            $arg["KETTEN_FLG"] = '1'; //null以外なら何でもいい
//        } else {
//            unset($arg["KETTEN_FLG"]);
//        }
//        //「欠点(評価)は、不振チェック参照するか？」の判定
//        if (($model->Properties["checkKettenDiv"] == '1' && $model->field["SEMESTER"] != '9' && ($testkind == '9900' || $testkind == '9901') && $model->useSlumpD048 == '1')) {
//            $arg["USE_SLUMP_D048"] = '1'; //null以外なら何でもいい
//        } else {
//            unset($arg["USE_SLUMP_D048"]);
//        }
        //欠点
        if ($model->cmd == 'change_grade' || $model->cmd == '') {
            $query = knjd192iQuery::getGdat($model->field["GRADE"]);
            $h_j = $db->getOne($query);
            if ($h_j == 'J') {
                $model->field["KETTEN"] = 60;
            } else {
                $model->field["KETTEN"] = 30;
            }
            if ($model->field["SEMESTER"] == '9' && $model->field["SUB_TESTCD"] == '990009') {
                $model->field["KETTEN"] = 1;
            }
        }
        $extra = "style=\"text-align:right;\" onblur=\"calc(this);\"";
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $model->field["KETTEN"], "KETTEN", 3, 3, $extra);

        $submit = $model->field["CATEGORY_IS_CLASS"] == '1' ? "" : " onChange=\"return btn_submit('knjd192i')\" ";
        //寮生のみ出力する
        if ($model->field["RYO_ONLY"]) {
            $extra = " checked ";
        } else {
            $extra = "";
        }
        $extra .= "id=\"RYO_ONLY\" ".$submit;
        $arg["data"]["RYO_ONLY"] = knjCreateCheckBox($objForm, "RYO_ONLY", "1", $extra);

        //下宿生のみ出力する
        if ($model->field["GESHUKU_ONLY"] == "1") {
            $extra = "checked ";
        } else {
            $extra = "";
        }
        $extra .= "id=\"GESHUKU_ONLY\" ".$submit;
        $arg["data"]["GESHUKU_ONLY"] = knjCreateCheckBox($objForm, "GESHUKU_ONLY", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd192iForm1.html", $arg); 
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model) {
    $seito = "0";
    if ($model->field["CATEGORY_IS_CLASS"] == 1){
        //対象クラスリストを作成する
        $query = knjd192iQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        $arg["data"]["NAME_LIST"] = 'クラス一覧';

    } else {

        //対象外の生徒取得
        $query = knjd192iQuery::getSchnoIdou($model, $model->field["SEMESTER"]);
        $result = $db->query($query);
        $opt_idou = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();

        //対象者リストを作成する
        $query = knjd192iQuery::getStudent($model, $model->field["SEMESTER"]);
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

        $seito = "1";
    }

    $extra = "multiple style=\"height:230px;width:230px;\" ondblclick=\"move1('left', {$seito})\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

    //出力対象一覧リストを作成する//
    $extra = "multiple style=\"height:230px;width:230px;\" ondblclick=\"move1('right', {$seito})\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //extra
    $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right', {$seito});\"";
    $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left', {$seito});\"";
    $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right', {$seito});\"";
    $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left', {$seito});\"";

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
    knjCreateHidden($objForm, "PRGID", "KNJD192I");
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
}

?>
