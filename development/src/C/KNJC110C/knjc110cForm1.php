<?php

require_once('for_php7.php');

class knjc110cForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc110cForm1", "POST", "knjc110cindex.php", "", "knjc110cForm1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjc110cQuery::getSemester();
        $semester_name = $db->getOne($query);
        $arg["data"]["SEMESTER"] = $semester_name;

        //学年コンボ
        $query = knjc110cQuery::getSelectHrclass($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE_HRCLASS", $model->field["GRADE_HRCLASS"], "onChange=\"return btn_submit('knjc110c');\"", 1);

        //出欠状況出力範囲(1:個人別 2:講座別)
        $opt = array(1, 2, 3);
        $model->field["OUTPUT_DIV"] = ($model->field["OUTPUT_DIV"] == "") ? "1" : $model->field["OUTPUT_DIV"];
        $extra = array("id=\"OUTPUT_DIV1\" onchange=\"calendarAbled(this.value)\"", "id=\"OUTPUT_DIV2\" onchange=\"calendarAbled(this.value)\"", "id=\"OUTPUT_DIV3\" onchange=\"calendarAbled(this.value)\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_DIV", $model->field["OUTPUT_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //カレンダーコントロール
        $calcDate = array();
        $calcDate = getWeekRange(CTRL_DATE);
        $startDay = $calcDate[0];
        $endDay = $calcDate[1];
        $model->field["SDATE"] = $model->field["SDATE"] == "" ? str_replace("-", "/", $startDay) : $model->field["SDATE"];
        $model->field["EDATE"] = $model->field["EDATE"] == "" ? str_replace("-", "/", $endDay) : $model->field["EDATE"];
        $arg["el"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"], $disabled);
        $arg["el"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $model->field["EDATE"], $disabled);

        //年月コンボ
        $targetYear = (substr(CTRL_DATE, 5, 2) - 4) < 0 ? substr(CTRL_DATE, 0, 4) - 1 : substr(CTRL_DATE, 0, 4);
        $value = $model->field["SMONTH"] != "" ? $model->field["SMONTH"] : substr(str_replace("-", "/", CTRL_DATE), 0, 7)."/01";
        $opt = array();
        for ($idx = 4; $idx <= 12; $idx++) {
            $opt[] = array('label' => $targetYear."/".sprintf("%02d", $idx),
                           'value' => $targetYear."/".sprintf("%02d", $idx)."/".sprintf("%02d", $idx));
        }
        for ($idx = 1; $idx <= 3; $idx++) {
            $opt[] = array('label' => ($targetYear + 1)."/".sprintf("%02d", $idx),
                           'value' => ($targetYear + 1)."/".sprintf("%02d", $idx)."/".sprintf("%02d", $idx));
        }
        $arg["el"]["SMONTH"] = knjCreateCombo($objForm, "SMONTH", $value, $opt, $extra, 1);

        $value = $model->field["EMONTH"] != "" ? $model->field["EMONTH"] : str_replace("-", "/", date("Y-m-d", strtotime(substr(CTRL_DATE, 0, 7)." last day of this month")));
        $opt = array();
        for ($idx = 4; $idx <= 12; $idx++) {
            $opt[] = array('label' => $targetYear."/".sprintf("%02d", $idx),
                           'value' => str_replace("-", "/", date("Y-m-d", strtotime($targetYear."-".sprintf("%02d", $idx)." last day of this month"))));
        }
        for ($idx = 1; $idx <= 3; $idx++) {
            $opt[] = array('label' => ($targetYear + 1)."/".sprintf("%02d", $idx),
                           'value' => str_replace("-", "/", date("Y-m-d", strtotime(($targetYear + 1)."-".sprintf("%02d", $idx)." last day of this month"))));
        }
        $arg["el"]["EMONTH"] = knjCreateCombo($objForm, "EMONTH", $value, $opt, $extra, 1);

        $arg["reload"] = "calendarAbled(".$model->field["OUTPUT_DIV"].")";

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc110cForm1.html", $arg);
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

/**********/
/* ボタン */
/**********/
function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
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
    knjCreateHidden($objForm, "PRGID", "KNJC110C");
    knjCreateHidden($objForm, "CHK_SDATE", $model->control["学期開始日付"][9]);
    knjCreateHidden($objForm, "CHK_EDATE", $model->control["学期終了日付"][9]);
    knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}

function getWeekRange($date)
{

    // 戻り値
    $start_date = "";
    $end_date = "";

    // タイムスタンプ変換
    $t = strtotime($date);
    // 開始の月曜を計算
    if (date('w', $t) == 1) {
    } elseif (date('w', $t) == 0) {
        $t = strtotime("next monday", $t);
    } else {
        $t = strtotime("last monday", $t);
    }
    $start_date = date("Y-m-d", $t);
    // 終了の土曜を計算
    $t = strtotime("next saturday", $t);
    $end_date = date("Y-m-d", $t);

    return array($start_date,$end_date);
}
