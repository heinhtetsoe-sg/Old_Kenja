<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjc164Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc164Form1", "POST", "knjc164index.php", "", "knjc164Form1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
        }
        $query = knjc164Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('changeKind');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);

        //学期コンボボックスを作成する
        $query = knjc164Query::getSemester();
        $semestername = "";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semestername = $row["LABEL"];
        }
        $result->free();
        $arg["data"]["SEMESTER"] = $semestername;

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //カレンダーコントロール
        if ($model->field["OUTPUT"] == "3") {
            $arg["el"]["SDATE"] = str_replace("-", "/", $model->control["学期開始日付"][9]);
            $arg["el"]["EDATE"] = str_replace("-", "/", $model->control["学期終了日付"][9]);
        } else {
            $model->field["SDATE"] = $model->field["SDATE"] == "" ? str_replace("-", "/", $model->control["学期開始日付"][9]) : $model->field["SDATE"];
            $model->field["EDATE"] = $model->field["EDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["EDATE"];
            $arg["el"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);
            $arg["el"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $model->field["EDATE"]);
        }

        //欠課時数・授業時数から・予定時数から
        $opt = array(1, 2, 3); //1:欠課時数 2:授業時数から 3:予定時数から
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onClick=\"checkOutput();btn_submit('knjc164')\" ", "id=\"OUTPUT2\" onClick=\"checkOutput();btn_submit('knjc164')\" ", "id=\"OUTPUT3\" onClick=\"checkOutput();btn_submit('knjc164');\" ");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        $dis_check1 = "";
        //チェックボタン選択時
        if ($model->field["OUTPUT"] != "1") {
            $dis_check1 = " readonly ";
        }
        $extra = $dis_check1." style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        //欠課時数
        if ($model->field["KEKKA_JISU"] == '') {
            $model->field["KEKKA_JISU"] = 0;
        }
        $arg["data"]["KEKKA_JISU"] = knjCreateTextBox($objForm, $model->field["KEKKA_JISU"], "KEKKA_JISU", 3, 3, $extra);

        $knjSchoolMst = knjc164Query::getSchoolMstMap($model, $db, CTRL_YEAR);
        $rishubunsi = $knjSchoolMst["RISYU_BUNSI"] ? $knjSchoolMst["RISYU_BUNSI"] : 0;
        $rishubunbo = $knjSchoolMst["RISYU_BUNBO"] ? $knjSchoolMst["RISYU_BUNBO"] : 1;
        $arg["data"]["TYOUKA_BUNSU"] = "　".$rishubunsi." / ".$rishubunbo."　";

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc164Form1.html", $arg); 
    }
}
/*********************************************** 以下関数 *******************************************************/
/**********/
/* コンボ */
/**********/
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = "") {
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "") ? CTRL_SEMESTER : $value;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

/******************/
/* リストToリスト */
/******************/
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $row1 = array();
    $row2 = array();
    $result = $db->query(knjc164Query::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($row["SELECTED"] == "1") {
            $row2[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        } else {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $row2, $extra, 20);

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

/**********/
/* ボタン */
/**********/
function makeBtn(&$objForm, &$arg, $model) {
    //実行ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //ＣＳＶボタン
    $arg["button"]["btn_csv"]   = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"btn_submit('csv');\"");
    //閉じるボタン
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

/**********/
/* hidden */
/**********/
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
    knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
    knjCreateHidden($objForm, "PRGID",         "KNJC164");
    if ($model->field["OUTPUT"] == "3") {
        knjCreateHidden($objForm, "SDATE", $model->control["学期開始日付"][9]);
        knjCreateHidden($objForm, "EDATE", $model->control["学期終了日付"][9]);
    }
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
    knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
    knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
}
?>
