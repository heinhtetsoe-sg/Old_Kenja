<?php

require_once('for_php7.php');

class knjd187gForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd187gForm1", "POST", "knjd187gindex.php", "", "knjd187gForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd187gQuery::getSemester($getCountsemester);
        $extra = "onchange=\"return btn_submit('knjd187g');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //クラスコンボ作成
        $query = knjd187gQuery::getHrClass(CTRL_YEAR, $seme, $model);
        $extra = "onchange=\"return btn_submit('knjd187g');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd187g', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd187g', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model, $seme);

        //データ取得
        $dataTmp = array();
        $query = knjd187gQuery::getHreportConditionDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataTmp[$row["SEQ"]] = $row;
        }
        $result->free();

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $model->field["RANK_DIV"] = $model->field["RANK_DIV"] ? $model->field["RANK_DIV"] : ($dataTmp["001"]["REMARK1"] ? $dataTmp["001"]["REMARK1"] : "1");
        $opt_rank = array(1, 2);
        $extra = array("id=\"RANK_DIV1\"", "id=\"RANK_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "RANK_DIV", $model->field["RANK_DIV"], $extra, $opt_rank, get_count($opt_rank));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        
        //帳票パターン 5:E
        $model->field["PATARN_DIV"] = '5';

        //平均・席次・偏差値ラジオボタン 1:学年 2:クラス 3:コース 4:学科
        $opt_group = array(1, 2, 3, 4);
        $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? ($dataTmp["003"]["REMARK1"] ? $dataTmp["003"]["REMARK1"] : "1") : $model->field["GROUP_DIV"];
        $extraEnabled = "";
        $extra = array("id=\"GROUP_DIV1\"".$extraEnabled, "id=\"GROUP_DIV2\"".$extraEnabled, "id=\"GROUP_DIV3\"".$extraEnabled, "id=\"GROUP_DIV4\"".$extraEnabled);
        $radioArray = knjCreateRadio($objForm, "GROUP_DIV", $model->field["GROUP_DIV"], $extra, $opt_group, get_count($opt_group));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //追指導表示
        $extra  = ($model->field["PRINT_TUISHIDOU"] == "1" || $model->cmd == '' && $dataTmp["005"]["REMARK1"] != '') ? "checked='checked' " : "";
        $extra .= $model->field["PATARN_DIV"] != '1' ? "" : " onclick=\"return false;\" ";
        $extra .= " id=\"PRINT_TUISHIDOU\"";
        $arg["data"]["PRINT_TUISHIDOU"] = knjCreateCheckBox($objForm, "PRINT_TUISHIDOU", "1", $extra, "");

        //遅刻・早退回数 表示なし
        $extra  = ($model->field["CHIKOKU_SOUTAI_NASI"] == "1" || $model->cmd == '' && $dataTmp["007"]["REMARK1"] != '') ? "checked='checked' " : "";
        $extra .= " id='CHIKOKU_SOUTAI_NASI'";
        $arg["data"]["CHIKOKU_SOUTAI_NASI"] = knjCreateCheckBox($objForm, "CHIKOKU_SOUTAI_NASI", "1", $extra);

        //合併元科目の学年評価・学年評定・出欠時数なし
        $extra  = ($model->field["NOT_PRINT_GAKUNEN_HYOKA_HYOTEI"] == "1" || $model->cmd == '' && $dataTmp["009"]["REMARK1"] != '') ? "checked='checked' " : "";
        $extra .= " id='NOT_PRINT_GAKUNEN_HYOKA_HYOTEI'";
        $arg["data"]["NOT_PRINT_GAKUNEN_HYOKA_HYOTEI"] = knjCreateCheckBox($objForm, "NOT_PRINT_GAKUNEN_HYOKA_HYOTEI", "1", $extra);
        
        //最終考査表記しない
        $extra  = ($model->field["NOT_PRINT_LASTEXAM"] == "1" || ($model->cmd == '' && $dataTmp["011"]["REMARK1"] != '')) ? "checked='checked' " : "";
        $extra .= " id='NOT_PRINT_LASTEXAM' onclick=\"chkNotPrintLastExam(this);\"";
        $arg["data"]["NOT_PRINT_LASTEXAM"] = knjCreateCheckBox($objForm, "NOT_PRINT_LASTEXAM", "1", $extra);
        //最終考査表記しない 成績のみ表記無し
        $extra  = ($model->field["NOT_PRINT_LASTEXAM_SCORE"] == "1" || $model->cmd == '' && $dataTmp["011"]["REMARK2"] != '') ? "checked='checked' " : "";
        $extra .= " id='NOT_PRINT_LASTEXAM_SCORE' onclick=\"chkNotPrintLastExam(this);\"";
        $arg["data"]["NOT_PRINT_LASTEXAM_SCORE"] = knjCreateCheckBox($objForm, "NOT_PRINT_LASTEXAM_SCORE", "1", $extra);
        
        //LHR、生徒会活動、学校行事表示なし
        if($model->cmd == '') {
            $extra  = "checked='checked' ";
        } else {
            $extra  = ($model->field["NO_ATTEND_SUBCLASS_SP"] == "1") ? "checked='checked' " : "";
        }
        $extra .= $model->field["PATARN_DIV"] == '5' ? "" : " onclick=\"return false;\" ";
        $extra .= " id='NO_ATTEND_SUBCLASS_SP'";
        $arg["data"]["NO_ATTEND_SUBCLASS_SP"] = knjCreateCheckBox($objForm, "NO_ATTEND_SUBCLASS_SP", "1", $extra);

        //考査名
        $extra  = ($model->field["NO_PRINT_SEMENAME_IN_TESTNAME"] == "1" || $model->cmd == '' && $dataTmp["013"]["REMARK1"] != '') ? "checked='checked' " : "";
        $extra .= " id='NO_PRINT_SEMENAME_IN_TESTNAME'";
        $arg["data"]["NO_PRINT_SEMENAME_IN_TESTNAME"] = knjCreateCheckBox($objForm, "NO_PRINT_SEMENAME_IN_TESTNAME", "1", $extra);

        //修得単位数を加算する
        $extra  = ($model->field["ADD_PAST_CREDIT"] == "1" || $model->cmd == '' && $dataTmp["014"]["REMARK1"] != '') ? "checked='checked' " : "";
        $extra .= $model->field["PATARN_DIV"] != '2' ? " onclick=\"kubun();\" " : " onclick=\"return false;\" ";
        $extra .= " id=\"ADD_PAST_CREDIT\"";
        $arg["data"]["ADD_PAST_CREDIT"] = knjCreateCheckBox($objForm, "ADD_PAST_CREDIT", "1", $extra, "");

        // 順位表記なし
        $extra  = ($model->field["NO_PRINT_RANK"] == "1" || $model->cmd == '' && $dataTmp["015"]["REMARK1"] != '') ? "checked='checked' " : "";
        $extra .= " id=\"NO_PRINT_RANK\"";
        $extra .= " onclick=\"kubun();\" ";
        $arg["data"]["NO_PRINT_RANK"] = knjCreateCheckBox($objForm, "NO_PRINT_RANK", "1", $extra, "");

        //名称マスタ D校種08チェック
        $model->che_school_kind = "D".$db->getone(knjd187gQuery::getSchoolKind(CTRL_YEAR, $seme, $model->field["GRADE_HR_CLASS"]))."08";
        $model->count = $db->getone(knjd187gQuery::getNameMstche($model));

        $query = knjd187gQuery::getCareerPlanSubclassname($model);
        $result = $db->query($query);
        $opt_idou = array();
        $subclassname = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($subclassname == '') {
                $subclassname = $row["SUBCLASSNAME"];
            }
        }
        $result->free();

        // 欠点科目数表記
        $extra  = ($model->field["KETTEN_KAMOKU_NO_SUBTRACT"] == "1" || $model->cmd == '' && $dataTmp["016"]["REMARK1"] != '') ? "checked='checked' " : "";
        $extra .= " id=\"KETTEN_KAMOKU_NO_SUBTRACT\"";
        $extra .= " onclick=\"setGroupDiv();\" ";
        $arg["data"]["KETTEN_KAMOKU_NO_SUBTRACT"] = knjCreateCheckBox($objForm, "KETTEN_KAMOKU_NO_SUBTRACT", "1", $extra, "");

        // 増加単位を加算する
        $extra = ($model->field["ZOUKA"] == "1" || $model->cmd == '' && $dataTmp["017"]["REMARK1"] != '')  ? "checked" : "";
        $extra .= " id=\"ZOUKA\" ";
        $arg["data"]["ZOUKA"] = knjCreateCheckBox($objForm, "ZOUKA", "1", $extra, "");

        // 保護者欄
        $extra  = ($model->field["NO_PRINT_HOGOSHA"] == "1" || $model->cmd == '' && $dataTmp["018"]["REMARK1"] != '') ? "checked='checked' " : "";
        $extra .= " id='NO_PRINT_HOGOSHA'";
        $arg["data"]["NO_PRINT_HOGOSHA"] = knjCreateCheckBox($objForm, "NO_PRINT_HOGOSHA", "1", $extra);

        // 通信欄
        $extra  = ($model->field["NO_PRINT_COMMUNICATION"] == "1" || $model->cmd == '' && $dataTmp["019"]["REMARK1"] != '') ? "checked='checked' " : "";
        $extra .= " id='NO_PRINT_COMMUNICATION'";
        $arg["data"]["NO_PRINT_COMMUNICATION"] = knjCreateCheckBox($objForm, "NO_PRINT_COMMUNICATION", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd187gForm1.html", $arg);
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model, $seme) {
    //対象外の生徒取得
    $query = knjd187gQuery::getSchnoIdou($model, $seme);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //対象者リストを作成する
    $query = knjd187gQuery::getStudent($model, $seme);
    $result = $db->query($query);
    $opt_right = $opt_left = array();
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $idou = (in_array($row["SCHREGNO"], $opt_idou)) ? "●" : "　";
        if (in_array($row["SCHREGNO"], $selectdata)) {
            $opt_left[] = array('label' => $row["SCHREGNO_SHOW"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]);
        } else {
            $opt_right[] = array('label' => $row["SCHREGNO_SHOW"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                 'value' => $row["SCHREGNO"]);
        }
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

    //生徒一覧リストを作成する//
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
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

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJD187G");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "checkKettenDiv", $model->Properties["checkKettenDiv"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useAssessSubclassMst", $model->Properties["useAssessSubclassMst"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "tutisyoPrintKariHyotei", $model->Properties["tutisyoPrintKariHyotei"]);
    knjCreateHidden($objForm, "knjd187gAddAttendSubclassGetCredit", $model->Properties["knjd187gAddAttendSubclassGetCredit"]);
    knjCreateHidden($objForm, "useAttendSemesHrRemark", $model->Properties["useAttendSemesHrRemark"]);
    knjCreateHidden($objForm, "PATARN_DIV", "5");
}

?>
