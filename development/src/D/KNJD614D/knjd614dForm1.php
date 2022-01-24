<?php

require_once('for_php7.php');

class knjd614dForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd614dForm1", "POST", "knjd614dindex.php", "", "knjd614dForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd614dQuery::getSemester();
        $extra = "onchange=\"return btn_submit('change_semes'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //テストコンボ作成
        $query = knjd614dQuery::getTest($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], "", 1);

        //学年コンボ作成
        $query = knjd614dQuery::getGradeHrClass($model, $model->field["SEMESTER"], "GRADE");
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        if ($model->field["CATEGORY_IS_CLASS"] == 2) {
            //クラスコンボ作成
            $query = knjd614dQuery::getGradeHrClass($model, $model->field["SEMESTER"], "HR_CLASS");
            $extra = "onchange=\"return btn_submit('knjd614d'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);
        }

        //チェックボックス作成
        makeChkBox($objForm, $arg, $model);

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        makeTestList($objForm, $arg, $db, $model, 2, "基礎学力テスト", 3, "31", "39");
        makeTestList($objForm, $arg, $db, $model, 3, "進研模試", 2, "21", "29");
        makeTestList($objForm, $arg, $db, $model, 4, "スタディサポート", 2, "11", "19");
        makeTestList($objForm, $arg, $db, $model, 5, "駿台模試", 1, "01", "09");
        makeTestList($objForm, $arg, $db, $model, 6, "河合塾模試", 3, "41", "49");

        /********************/
        /* テキストボックス */
        /********************/

        //対象学期の開始日・終了日
        $sDate = $db->getOne(knjd614dQuery::getSemesterDate("SDATE", "9"));
        $eDate = $db->getOne(knjd614dQuery::getSemesterDate("EDATE", $model->field["SEMESTER"]));
        if ($model->cmd == 'change_semes') {
            $model->field["DATE_FROM"] = str_replace("-", "/", $sDate);
            $model->field["DATE_TO"] = str_replace("-", "/", $eDate);
        }

        //出席集計日付
        $model->field["DATE_FROM"] = $model->field["DATE_FROM"] == "" ? str_replace("-", "/", $sDate) : $model->field["DATE_FROM"];
        $arg["data"]["DATE_FROM"] = View::popUpCalendar($objForm, "DATE_FROM", $model->field["DATE_FROM"]);
        $model->field["DATE_TO"] = $model->field["DATE_TO"] == "" ? str_replace("-", "/", $eDate) : $model->field["DATE_TO"];
        $arg["data"]["DATE_TO"] = View::popUpCalendar($objForm, "DATE_TO", $model->field["DATE_TO"]);

        //設定保存テキスト
        $extra = "";
        $arg["data"]["SETTING_NAME"] = knjCreateTextBox($objForm, $model->field["SETTING_NAME"], "SETTING_NAME", 30, 30, $extra);

        //設定呼び出しコンボ
        $query = knjd614dQuery::getRecommendationSeqNo($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SETTING_SEQ", $model->field["SETTING_SEQ"], $extra, 1, "BLANK");

        $result = $db->query($query);
        $chkStrList = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chkStrList[] = $row["LABEL"];
        }
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $chkStrList);

        //DB切断
        Query::dbCheckIn($db);

        $model->firstFlg = false;  //初期処理終了

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd614dForm1.html", $arg);
    }
}

function makeChkBox(&$objForm, &$arg, $model)
{
    //学内成績
    $extra = "onchange=\"disEnaCtl(this, 1, 0);\"";
    $disableBase = "";
    $disableFlg = "";
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER", $extra);
    if ($model->field["CATEGORY_IS_INNER"] == "") {
        $disableBase = " disabled";
    }

    //高1
    $extra = "onchange=\"disEnaCtl(this, 1, 1);\"";
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G1", $extra.$disableBase);
    $disableFlg = "";
    if ($disableBase != "" || $model->field["CATEGORY_IS_INNER_G1"] == "") {
        $disableFlg = " disabled";
    }
    $extra = "";
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G1S1", $extra.$disableFlg.$disableFlg);
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G1SH", $extra.$disableFlg.$disableFlg);
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G1S9", $extra.$disableFlg.$disableFlg);

    //高2
    $extra = "onchange=\"disEnaCtl(this, 1, 2);\"";
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G2", $extra.$disableBase);
    $disableFlg = "";
    if ($disableBase != "" || $model->field["CATEGORY_IS_INNER_G2"] == "") {
        $disableFlg = " disabled";
    }
    $extra = "";
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G2S1", $extra.$disableFlg.$disableFlg);
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G2SH", $extra.$disableFlg.$disableFlg);
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G2S9", $extra.$disableFlg.$disableFlg);

    //高3
    $extra = "onchange=\"disEnaCtl(this, 1, 3);\"";
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G3", $extra.$disableBase);
    $disableFlg = "";
    if ($disableBase != "" || $model->field["CATEGORY_IS_INNER_G3"] == "") {
        $disableFlg = " disabled";
    }
    $extra = "";
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G3S1", $extra.$disableFlg.$disableFlg);
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G3SH", $extra.$disableFlg.$disableFlg);
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_G3S9", $extra.$disableFlg.$disableFlg);

    //評定合算
    $extra = "onchange=\"disEnaCtl(this, 1, 4);\"";
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_9_ALL", $extra.$disableBase);
    $disableFlg = "";
    if ($disableBase != "" || $model->field["CATEGORY_IS_INNER_9_ALL"] == "") {
        $disableFlg = " disabled";
    }

    $opt = array(1, 2);
    $extra = array("id=\"CATEGORY_IS_INNER_9_ALL12\"", "id=\"CATEGORY_IS_INNER_9_ALL123\"");
    $value = $model->field["CATEGORY_IS_INNER_9_ALL12_123"] ? $model->field["CATEGORY_IS_INNER_9_ALL12_123"] : "1";
    $radioArray = knjCreateRadio($objForm, "CATEGORY_IS_INNER_9_ALL12_123", $value, $extra, $opt, count($opt));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //実力テスト
    $extra = "";
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_PROF_TEST", $extra, 1);

    //日本大学基礎学力到達度テスト
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_OUTER_COLLEGE", $extra, 2);

    //ベネッセ
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_BENESSE_TEST", $extra, 3);

    //スタディサポート
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_STUDY_SUP", $extra, 4);

    //駿台
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_SUNDAI", $extra, 5);

    //河合塾
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_KAWAI", $extra, 6);

    //資格・学内成績
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_QUALIFY", $extra);
    makeChkBox1($objForm, $arg, $model, "CATEGORY_IS_INNER_8020", $extra);
}
function makeChkBox1(&$objForm, &$arg, $model, $name, $extra, $jsPrmId = 0)
{
    $eWk = "";
    if ($model->firstFlg && is_null($model->field[$name])) {
        $model->field[$name] = "1"; //初期値
    }
    if ($model->field[$name] != "") {
        $eWk = " checked";
    }
    if ($jsPrmId != 0) {
        $extra = $extra."onchange=\"disEnaMockCtl(this, {$jsPrmId});\"";
    }
    $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra.$eWk);
}

function makeStudentList(&$objForm, &$arg, $db, $model)
{

    //対象クラスリストを作成する
    $query = knjd614dQuery::getGradeHrClass($model, $model->field["SEMESTER"], "HR_CLASS");
    $result = $db->query($query);
    $opt1 = array();
    $opt2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $model->selHR, true)) {
            $opt1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        } else {
            $opt2[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
    }
    $result->free();
    $extra = "multiple style=\"width:350px\" ondblclick=\"moven('left', 0, 1)\"";
    $arg["data"]["CATEGORY_NAME1"] = knjCreateCombo($objForm, "CATEGORY_NAME1", "", $opt1, $extra, 11);

    $arg["data"]["NAME_LIST1"] = 'クラス一覧';

    //出力対象一覧リストを作成する
    $extra = "multiple style=\"width:350px\" ondblclick=\"moven('right', 0, 1)\"";
    $arg["data"]["CATEGORY_SELECTED1"] = knjCreateCombo($objForm, "CATEGORY_SELECTED1", "", $opt2, $extra, 11);

    //extra
    $extra_rights = "style=\"height:20px;width:40px\" onclick=\"movesn('right', 0, 1);\"";
    $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"movesn('left', 0, 1);\"";
    $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"moven('right', 0, 1);\"";
    $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"moven('left', 0, 1);\"";

    //対象選択ボタンを作成する
    $arg["button"]["btn_rights1"] = knjCreateBtn($objForm, "btn_rights1", ">>", $extra_rights);
    //対象取消ボタンを作成する
    $arg["button"]["btn_lefts1"]  = knjCreateBtn($objForm, "btn_lefts1", "<<", $extra_lefts);
    //対象選択ボタンを作成する
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra_right1);
    //対象取消ボタンを作成する
    $arg["button"]["btn_left1"]  = knjCreateBtn($objForm, "btn_left1", "＜", $extra_left1);
}

function makeTestList(&$objForm, &$arg, $db, $model, $numbr, $titleName, $ghosyaCd, $searchCdStrt, $searchCdEnd)
{
    $disabledFlg = "";
    $selArry = array();
    switch ($numbr) {
        case 2:
            if ($model->field["CATEGORY_IS_OUTER_COLLEGE"] != "1") {
                $disabledFlg = " disabled";
            }
            $selArry = $model->selOutCol;
            break;
        case 3:
            if ($model->field["CATEGORY_IS_BENESSE_TEST"] != "1") {
                $disabledFlg = " disabled";
            }
            $selArry = $model->selBene;
            break;
        case 4:
            if ($model->field["CATEGORY_IS_STUDY_SUP"] != "1") {
                $disabledFlg = " disabled";
            }
            $selArry = $model->selSSup;
            break;
        case 5:
            if ($model->field["CATEGORY_IS_SUNDAI"] != "1") {
                $disabledFlg = " disabled";
            }
            $selArry = $model->selSund;
            break;
        case 6:
            if ($model->field["CATEGORY_IS_KAWAI"] != "1") {
                $disabledFlg = " disabled";
            }
            $selArry = $model->selKawi;
            break;
        default:
            break;
    }

    //対象クラスリストを作成する
    $query = knjd614dQuery::getTestType($model, $ghosyaCd, $searchCdStrt, $searchCdEnd);
    $result = $db->query($query);
    $opt1 = array();
    $opt2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $selArry, true)) {
            $opt1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        } else {
            $opt2[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
    }
    $result->free();
    $extra = "multiple style=\"width:350px\" ondblclick=\"moven('left', 0, {$numbr})\"".$disabledFlg;
    $arg["data"]["CATEGORY_NAME".$numbr] = knjCreateCombo($objForm, "CATEGORY_NAME".$numbr, "", $opt1, $extra, 6);

    $arg["data"]["NAME_LIST".$numbr] = $titleName;

    //出力対象一覧リストを作成する
    $extra = "multiple style=\"width:350px\" ondblclick=\"moven('right', 0, {$numbr})\"".$disabledFlg;
    $arg["data"]["CATEGORY_SELECTED".$numbr] = knjCreateCombo($objForm, "CATEGORY_SELECTED".$numbr, "", $opt2, $extra, 6);

    $extra_rights = "style=\"height:20px;width:40px\" onclick=\"movesn('right', 0, {$numbr});\"".$disabledFlg;
    $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"movesn('left', 0, {$numbr});\"".$disabledFlg;
    $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"moven('right', 0, {$numbr});\"".$disabledFlg;
    $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"moven('left', 0, {$numbr});\"".$disabledFlg;

    //対象選択ボタンを作成する
    $arg["button"]["btn_rights".$numbr] = knjCreateBtn($objForm, "btn_rights".$numbr, ">>", $extra_rights);
    //対象取消ボタンを作成する
    $arg["button"]["btn_lefts".$numbr]  = knjCreateBtn($objForm, "btn_lefts".$numbr, "<<", $extra_lefts);
    //対象選択ボタンを作成する
    $arg["button"]["btn_right".$numbr] = knjCreateBtn($objForm, "btn_right".$numbr, "＞", $extra_right1);
    //対象取消ボタンを作成する
    $arg["button"]["btn_left".$numbr]  = knjCreateBtn($objForm, "btn_left".$numbr, "＜", $extra_left1);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $prm = "")
{
    $opt = array();
    if ($prm == "BLANK") {
        $opt[] = array("LABEL"=>"", "VALUE"=>"");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg)
{
    //設定保存ボタン
    $extra = "style=\"width:71px\" onclick=\"return btn_submit('del_setting');\";\"";
    $arg["button"]["del_setting"] = knjCreateBtn($objForm, "del_setting", "設定削除", $extra);
    //設定保存ボタン
    $extra = "onclick=\"return btn_submit('save_setting');\";\"";
    $arg["button"]["save_setting"] = knjCreateBtn($objForm, "save_setting", "設定保存", $extra);
    //設定呼出ボタン
    $extra = "onclick=\"return btn_submit('load_setting');\";\"";
    $arg["button"]["load_setting"] = knjCreateBtn($objForm, "load_setting", "設定呼出", $extra);
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //ＣＳＶボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model, $chkStrList)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD614D");
    knjCreateHidden($objForm, "CHK_SETTINGNAME", implode(",", $chkStrList));
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "selHR");
    knjCreateHidden($objForm, "selOutCol");
    knjCreateHidden($objForm, "selBene");
    knjCreateHidden($objForm, "selSSup");
    knjCreateHidden($objForm, "selSund");
    knjCreateHidden($objForm, "selKawi");
}
