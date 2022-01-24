<?php

require_once('for_php7.php');

require_once("AttendAccumulate.php");

class knjc163Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc163Form1", "POST", "knjc163index.php", "", "knjc163Form1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjc163Query::getSemester();
        $semestername = "";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semestername = $row["LABEL"];
        }
        $result->free();
        $arg["data"]["SEMESTER"] = $semestername;

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query = knjc163Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('knjc163'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1, $model);
        }

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //カレンダーコントロール
        $model->field["SDATE"] = $model->field["SDATE"] == "" ? str_replace("-", "/", $model->control["学期開始日付"][9]) : $model->field["SDATE"];
        $model->field["EDATE"] = $model->field["EDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["EDATE"];
        $arg["el"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);
        $arg["el"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $model->field["EDATE"]);

        //欠席日数・指定した割合
        $opt = array(1, 2); //1:欠席日数 2:指定した割合
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\" onClick=\"checkOutput();\" ", "id=\"OUTPUT2\" onClick=\"checkOutput();\" ");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        $dis_check1 = "";
        //チェックボタン選択時
        if ($model->field["OUTPUT"] == "2") {
            $dis_check1 = "disabled";
        }
        $extra = $dis_check1." style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        //欠席日数
        if ($model->field["KESSEKI_NISSU"] == '') {
            $model->field["KESSEKI_NISSU"] = 0;
        }
        $arg["data"]["KESSEKI_NISSU"] = knjCreateTextBox($objForm, $model->field["KESSEKI_NISSU"], "KESSEKI_NISSU", 3, 3, $extra);

        //注意・超過ラジオ
        $opt = array(1, 2); //1:注意 2:超過
        $model->field["TYUI_TYOUKA"] = ($model->field["TYUI_TYOUKA"] == "") ? "1" : $model->field["TYUI_TYOUKA"];
        $dis_check2 = "";
        if ($model->field["OUTPUT"] == "1") {
            $dis_check2 = " disabled ";
        }
        $extra = array("id=\"TYUI_TYOUKA1\" ".$dis_check2, "id=\"TYUI_TYOUKA2\" ".$dis_check2);
        $radioArray = knjCreateRadio($objForm, "TYUI_TYOUKA", $model->field["TYUI_TYOUKA"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        $knjSchoolMst = knjc163Query::getSchoolMstMap($model, $db, CTRL_YEAR);
        $warnbunsi = $knjSchoolMst["KESSEKI_WARN_BUNSI"] ? $knjSchoolMst["KESSEKI_WARN_BUNSI"] : 0;
        $warnbunbo = $knjSchoolMst["KESSEKI_WARN_BUNBO"] ? $knjSchoolMst["KESSEKI_WARN_BUNBO"] : 1;
        $arg["data"]["TYUI_BUNSU"] = $warnbunsi." / ".$warnbunbo;
        $outbunsi = $knjSchoolMst["KESSEKI_OUT_BUNSI"] ? $knjSchoolMst["KESSEKI_OUT_BUNSI"] : 0;
        $outbunbo = $knjSchoolMst["KESSEKI_OUT_BUNBO"] ? $knjSchoolMst["KESSEKI_OUT_BUNBO"] : 1;
        $arg["data"]["TYOUKA_BUNSU"] = $outbunsi." / ".$outbunbo;


        //注意・超過のタイトル
        $arg["data"]["TYUI_TYOUKA_TITLE"] = "指定した割合";

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc163Form1.html", $arg);
    }
}
/*********************************************** 以下関数 *******************************************************/
/**********/
/* コンボ */
/**********/
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = "")
{
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
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjc163Query::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

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
function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //ＣＳＶボタン
    //$arg["button"]["btn_csv"]   = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"btn_submit('csv');\"");
    $arg["button"]["btn_csv"]   = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return btn_submit('csv');\"");
    //閉じるボタン
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

/**********/
/* hidden */
/**********/
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", str_replace("-", "/", CTRL_DATE));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJC163");
    knjCreateHidden($objForm, "SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
    knjCreateHidden($objForm, "SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
    knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
    knjCreateHidden($objForm, "knjc163useAttendSemesDat", $model->Properties["knjc163useAttendSemesDat"]);
    knjCreateHidden($objForm, "hibiNyuuryokuNasi", $model->Properties["hibiNyuuryokuNasi"]);
}
