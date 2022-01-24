<?php

require_once('for_php7.php');

class knjd301bForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        if (isset($model->maxSemester) == false) {
            $query = knjd301bQuery::getMaxSemesterQuery($model);
            $model->maxSemester = $db->getOne($query);
        }

        //コントロール変更時の共通イベント
        $postback = " onchange=\"btn_submit('change', this);\"";

        //ボタン作成
        $arg["button"]["btn_outputcsv"] = knjCreateBtn($objForm, "btn_outputcsv", "CSV出力", " onclick=\"btn_submit('csvoutput', this);\"");
        $arg["button"]["btn_end"]       = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //クラブリストを作成する
        makeListToList($objForm, $arg, $db, $model);

        //帳票種別
        $radioValues = array(1, 2, 3);
        $radioStyles = array(
            "id='REPORT1' ".$postback,
            "id='REPORT2' ".$postback,
            "id='REPORT3' ".$postback
        );
        $radioArray = knjCreateRadio($objForm, "REPORT_KIND", $model->reportKind, $radioStyles, $radioValues, get_count($radioValues));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //学期
        $query = knjd301bQuery::getSemester($model);
        $this->makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $postback, 1, "");

        //テスト種別
        if ($model->reportKind == "3" && $model->semester == "9") {
            $model->testKindCd = "";
            $arg["data"]["TESTKIND"] = knjCreateCombo($objForm, "TESTKIND", "", array(), "", 1);
        } else {
            $query = knjd301bQuery::getTestKind($model);
            $this->makeCmb($objForm, $arg, $db, $query, "TESTKIND", $model->testKindCd, "", 1);
        }

        //プレビュー・印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //非表示項目
        knjCreateHidden($objForm, "cmd", $model->cmd);
        knjCreateHidden($objForm, "HID_YEAR", $model->year);
        knjCreateHidden($objForm, "PRGID", $model->programID);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "HID_EVENT_FROM", $model->eventFrom);
        knjCreateHidden($objForm, "HID_SELECTED_CLUBS");
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolKind);

        //DB切断
        Query::dbCheckIn($db);

        //HTML出力終了
        $arg["start"]  = $objForm->get_start("main", "POST", "knjd301bindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjd301bForm1.html", $arg);
    }

    //コンボ作成
    private function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", &$semeName = null, &$sdate = null, &$edate = null)
    {
        $opt = array();
        if ($blank) {
            $opt[] = array("label" => "", "value" => "");
        }
        $value_flg = false;
        $idx = $default = 0;
        $default_flg = true;

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }

            if ($row["NAMESPARE2"] && $default_flg) {
                $default = $idx;
                $default_flg = false;
            } else {
                $idx++;
            }
        }

        $result->free();
        $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $rightList = $leftList = array();
    $leftCnt = $rightCnt = 0;

    $query = knjd301bQuery::getClubs($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["CLUBCD"], $model->selectedClubs)) {
            $rightList[]= array('label' => $row["CLUBCD"]."　".$row["CLUBNAME"],
                                'value' => $row["CLUBCD"]);
        }
    }
    $result->free();

    $query = knjd301bQuery::getClubs($model, $model->selectedClubs);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $leftList[]= array('label' => $row["CLUBCD"]."　".$row["CLUBNAME"],
                           'value' => $row["CLUBCD"]);
    }
    $result->free();

    //クラブ一覧(右側)
    $extra = "multiple style=\"width:100%; height:340px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLUBS_LIST"] = knjCreateCombo($objForm, "CLUBS_LIST", "", $rightList, $extra, 33);

    //出力対象クラブ(右側)
    $extra = "multiple style=\"width:100%; height:340px;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLUBS_SELECTED"] = knjCreateCombo($objForm, "CLUBS_SELECTED", "", $leftList, $extra, 33);

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
