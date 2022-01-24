<?php

require_once('for_php7.php');

class knjd177Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd177Form1", "POST", "knjd177index.php", "", "knjd177Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期数取得
        $query = knjd177Query::getCountSemester($model);
        $getCountsemester = $db->getOne($query);

        //学期コンボ作成
        $query = knjd177Query::getSemester($getCountsemester);
        $extra = "onchange=\"return btn_submit('knjd177'), AllClearList();\"";
        //ログイン学期が後期、3学期のときはコンボの初期値は学年末を表示
        if ($model->cmd == '' && ($getCountsemester == 3 && $model->semester == 3 || $getCountsemester == 2 && $model->semester == 2)) {
            $model->semester = 9;
        }
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //クラスコンボ作成
        $query = knjd177Query::getHrClass(CTRL_YEAR, $seme);
        $extra = "onchange=\"return btn_submit('knjd177'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);

        //異動対象日付初期値セット
        if ($model->field["DATE"] == "") {
            $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        }
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); tmp_list('knjd177', 'on')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["DATE"], "DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('knjd177', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model, $seme);

        //住所印刷チェックボックス
        $extra = ($model->field["ADDR_PRINT"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"ADDR_PRINT\"";
        $arg["data"]["ADDR_PRINT"] = knjCreateCheckBox($objForm, "ADDR_PRINT", "1", $extra, "");

        //送り先ラジオボタン 1:保護者 2:負担者 3:その他
        $opt_addrDiv = array(1, 2, 3);
        $model->field["ADDR_DIV"] = ($model->field["ADDR_DIV"] == "") ? "1" : $model->field["ADDR_DIV"];
        $extra = array("id=\"ADDR_DIV1\"", "id=\"ADDR_DIV2\"", "id=\"ADDR_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "ADDR_DIV", $model->field["ADDR_DIV"], $extra, $opt_addrDiv, get_count($opt_addrDiv));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //平均・席次・偏差値ラジオボタン 1:学年 2:クラス 3:コース
        $opt_group = array(1, 2, 3);
        $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
        $extra = array("id=\"GROUP_DIV1\"", "id=\"GROUP_DIV2\"", "id=\"GROUP_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "GROUP_DIV", $model->field["GROUP_DIV"], $extra, $opt_group, get_count($opt_group));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //順位の基準点ラジオボタン 1:総合点 2:平均点 3:偏差値
        $model->field["RANK_DIV"] = $model->field["RANK_DIV"] ? $model->field["RANK_DIV"] : '1';
        $opt_rank = array(1, 2, 3);
        $extra = array("id=\"RANK_DIV1\"", "id=\"RANK_DIV2\"", "id=\"RANK_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "RANK_DIV", $model->field["RANK_DIV"], $extra, $opt_rank, get_count($opt_rank));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //家庭からの欄ありチェックボックス
        $extra = ($model->field["PRINT_KATEI"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"PRINT_KATEI\"";
        $arg["data"]["PRINT_KATEI"] = knjCreateCheckBox($objForm, "PRINT_KATEI", "1", $extra, "");

        //帳票パターンラジオボタン 1:A 2:B 3:C 4:D 5:E
        $opt_patarn = array(1, 2, 3, 4, 5);
        $model->field["PATARN_DIV"] = ($model->field["PATARN_DIV"] == "") ? "1" : $model->field["PATARN_DIV"];
        $disabled = " onclick=\"OptionUse('this');\"";
        $extra = array("id=\"PATARN_DIV1\"".$disabled, "id=\"PATARN_DIV2\"".$disabled, "id=\"PATARN_DIV3\"".$disabled, "id=\"PATARN_DIV4\"".$disabled, "id=\"PATARN_DIV5\"".$disabled);
        $radioArray = knjCreateRadio($objForm, "PATARN_DIV", $model->field["PATARN_DIV"], $extra, $opt_patarn, get_count($opt_patarn));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //欠課時数(Ｂパターンのみ)チェックボックス
        $disKekka = ($model->field["PATARN_DIV"] == "2") ? "" : " disabled";
        $extra = ($model->field["KEKKA_PRINT"] == "1") ? "checked" : "";
        $extra .= " id=\"KEKKA_PRINT\"";
        $arg["data"]["KEKKA_PRINT"] = knjCreateCheckBox($objForm, "KEKKA_PRINT", "1", $extra.$disKekka, "");

        //締め日までの出欠日数を出力する(Ｃ・Ｅパターンのみ)チェックボックス
        $disRankAll = ($model->field["PATARN_DIV"] == "3" || $model->field["PATARN_DIV"] == "5") ? "" : " disabled";
        $extra = ($model->field["SHIMEBI_PRINT"] == "1") ? "checked" : "";
        $extra .= " id=\"SHIMEBI_PRINT\"";
        $arg["data"]["SHIMEBI_PRINT"] = knjCreateCheckBox($objForm, "SHIMEBI_PRINT", "1", $extra.$disRankAll, "");

        //順位(C以外)チェックボックス
        $disRankAll = ($model->field["PATARN_DIV"] == "3" || $model->field["PATARN_DIV"] == "5") ? " disabled" : "";
        $extra = ($model->field["RANK_PRINT_ALL"] == "1") ? "checked" : "";
        $extra .= " id=\"RANK_PRINT_ALL\"";
        $arg["data"]["RANK_PRINT_ALL"] = knjCreateCheckBox($objForm, "RANK_PRINT_ALL", "1", $extra.$disRankAll, "");

        //教科数
        $optMockDiv = array(array("label" => "3", "value" => "3"),
                            array("label" => "5", "value" => "5"),
                            array("label" => "9", "value" => "9"));
        $model->field["KYOUKA_SU"] = $model->field["KYOUKA_SU"] ? $model->field["KYOUKA_SU"] : "3";
        $extra = $model->field["PATARN_DIV"] == "3" ? "" : " disabled ";
        $extra .= " id=\"KYOUKA_SU\"";
        $arg["KYOUKA_SU"] = knjCreateCombo($objForm, "KYOUKA_SU", $model->field["KYOUKA_SU"], $optMockDiv, $extra, 1);

        //順位印刷チェックボックス
        //テスト1～8
        $query = knjd177Query::getRecordMockOrderDat($model);
        $result = $db->query($query);
        $count = 1;
        $check_box = array();
        $rankPrintField = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $disabled = ($model->field["PATARN_DIV"] == "3") ? "" : " disabled";
            $rankPrintHiddenField[] = "HIDDEN_RANK_PR1NT{$count}";
            $extra  = ($model->field["HIDDEN_RANK_PR1NT{$count}"] == "1" || ($model->cmd == "" && $count == 1) || $model->field["RANK_PRINT{$count}"] == "1") ? "checked" : "";
            $extra .= " id=\"RANK_PRINT{$row["VALUE"]}\"";
            $check_box[] = knjCreateCheckBox($objForm, "RANK_PRINT{$count}", $row["VALUE"], $extra.$disabled, "") . "<label for=\"RANK_PRINT{$row["VALUE"]}\">{$row["LABEL"]}</label>";
            $count++;
        }
        $arg["RANK_PRINT"] = implode("<br>", $check_box);

        //順位印刷チェックボックス
        //学期成績
        $query = knjd177Query::getSemesterMst($model);
        $result = $db->query($query);
        $check_box = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $disabled = ($model->field["PATARN_DIV"] == "3") ? "" : " disabled";
            $rankPrintHiddenField[] = "HIDDEN_RANK_PR1NT_SEM{$row["VALUE"]}";
            $extra  = ($model->field["HIDDEN_RANK_PR1NT_SEM{$row["VALUE"]}"] == "1" || $model->field["RANK_PRINT_SEM{$row["VALUE"]}"] == "1") ? "checked" : "";
            $extra .= " id=\"RANK_PRINT_SEM{$row["VALUE"]}\"";
            $check_box[] = knjCreateCheckBox($objForm, "RANK_PRINT_SEM{$row["VALUE"]}", $row["VALUE"], $extra.$disabled, "") . "<label for=\"RANK_PRINT_SEM{$row["VALUE"]}\">{$row["LABEL"]}</label>";
        }
        $arg["RANK_PRINT_SEM"] = implode("<br>", $check_box);

        //仮評定印刷 1:する 2:しない
        $opt_patarn = array(1, 2);
        $model->field["GAKUNEN_HYOUTEI"] = ($model->field["GAKUNEN_HYOUTEI"] == "") ? "1" : $model->field["GAKUNEN_HYOUTEI"];
        $extra = array("id=\"GAKUNEN_HYOUTEI1\"", "id=\"GAKUNEN_HYOUTEI2\"");
        $radioArray = knjCreateRadio($objForm, "GAKUNEN_HYOUTEI", $model->field["GAKUNEN_HYOUTEI"], $extra, $opt_patarn, get_count($opt_patarn));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //段階値は読替あり(Ｅパターンのみ)チェックボックス
        $disDankai = ($model->field["PATARN_DIV"] == "5") ? "" : " disabled";
        $extra = ($model->field["DANKAI_YOMIKAE"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"DANKAI_YOMIKAE\"";
        $arg["data"]["DANKAI_YOMIKAE"] = knjCreateCheckBox($objForm, "DANKAI_YOMIKAE", "1", $extra.$disDankai, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $rankPrintHiddenField);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd177Form1.html", $arg);
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model, $seme)
{
    //対象外の生徒取得
    $query = knjd177Query::getSchnoIdou($model, $seme);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //対象者リストを作成する
    $query = knjd177Query::getStudent($model, $seme);
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
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model, $rankPrintHiddenField)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJD177");
    knjCreateHidden($objForm, "FORMNAME");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    foreach ($rankPrintHiddenField as $field) {
        knjCreateHidden($objForm, $field);
    }
    knjCreateHidden($objForm, "checkKettenDiv", $model->Properties["checkKettenDiv"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useAssessSubclassMst", $model->Properties["useAssessSubclassMst"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "knjd177cUseSeme2Form", $model->Properties["knjd177cUseSeme2Form"]);
}
