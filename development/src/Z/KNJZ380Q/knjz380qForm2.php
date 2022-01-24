<?php

require_once('for_php7.php');

class knjz380qForm2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz380qindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "knjz380qForm2") {
            $query = knjz380qQuery::getRightData($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["RIGHT_COURSE_MAJOR"] = $Row["COURSECD"]."-".$Row["MAJORCD"];
        } else {
            $Row =& $model->field;
        }

        //課程学科コンボ
        $extra = "";
        $query = knjz380qQuery::getCourseMajor($model);
        makeCmb($objForm, $arg, $db, $query, $Row["RIGHT_COURSE_MAJOR"], "RIGHT_COURSE_MAJOR", $extra, 1, "BLANK");

        //学期コンボボックス作成
        $extra = "onChange=\"btn_submit('knjz380qForm2')\"";
        $query = knjz380qQuery::getSemester($model);
        makeCmb($objForm, $arg, $db, $query, $Row["SEMESTER"], "SEMESTER", $extra, 1, "BLANK");

        //テスト項目種別名コンボボックスの中身を作成------------------------------
        //考査種別（大分類）
        $extra = "onChange=\"btn_submit('knjz380qForm2')\"";
        $query = knjz380qQuery::getTestKindName($model);
        makeCmb($objForm, $arg, $db, $query, $Row["TESTKINDCD"], "TESTKINDCD", $extra, 1, "BLANK");

        //考査種別（中分類）
        $opt = array();
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($Row["SEMESTER"] != "" && $Row["TESTKINDCD"] != "") {
            if ($Row["TESTKINDCD"] !== '99') {
                $opt[] = array('label' => '01 1回目', 'value' => '01');
                $opt[] = array('label' => '02 2回目', 'value' => '02');
            } else {
                if ($Row["SEMESTER"] === '9') {
                    $opt[] = array('label' => '00 学年末', 'value' => '00');
                } else {
                    $opt[] = array('label' => '00 学期末', 'value' => '00');
                }
            }
        }
        $Row["TESTITEMCD"] = ($Row["TESTITEMCD"]) ? $Row["TESTITEMCD"] : "";
        $extra = "onChange=\"btn_submit('knjz380qForm2')\"";
        $arg["data"]["TESTITEMCD"] = knjCreateCombo($objForm, "TESTITEMCD", $Row["TESTITEMCD"], $opt, $extra, 1);

        //考査種別（小分類）
        $opt = array();
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        $result = $db->query(knjz380qQuery::getScoreDiv($model));
        if ($Row["SEMESTER"] != "" && $Row["TESTKINDCD"] != "" && $Row["TESTITEMCD"] != "") {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
        }
        $Row["SCORE_DIV"] = ($Row["SCORE_DIV"]) ? $Row["SCORE_DIV"] : "";
        $extra = "";
        $arg["data"]["SCORE_DIV"] = knjCreateCombo($objForm, "SCORE_DIV", $Row["SCORE_DIV"], $opt, $extra, 1);
        $result->free();

        //考査種別名称
        $extra = "";
        $arg["data"]["TESTITEMNAME"] = knjCreateTextBox($objForm, $Row["TESTITEMNAME"], "TESTITEMNAME", 20, 30, $extra);

        //考査種別名称１
        $extra = "";
        $arg["data"]["TESTITEMABBV1"] = knjCreateTextBox($objForm, $Row["TESTITEMABBV1"], "TESTITEMABBV1", 10, 10, $extra);

        //テスト期間
        if ($model->Properties["Test_Period_Hyouji"] == "1") {
            $arg["Test_Period_Hyouji"] = "1";
            $arg["data"]["TEST_START_DATE"] = View::popUpCalendar($objForm, "TEST_START_DATE", str_replace("-", "/",  $Row["TEST_START_DATE"]));
            $arg["data"]["TEST_END_DATE"]   = View::popUpCalendar($objForm, "TEST_END_DATE", str_replace("-", "/",  $Row["TEST_END_DATE"]));
        }

        //集計フラグ 1:集計する 0:集計しない
        $extra = "id=\"COUNTFLG\"";
        if ($Row["COUNTFLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["COUNTFLG"] = knjCreateCheckBox($objForm, "COUNTFLG", "1", $extra);

        //出欠集計範囲表示切替
        if($model->Properties["Semester_Detail_Hyouji"] == "1") {
            $arg["sem_detail"] = 1;
        }

        //出欠集計範囲コンボボックス作成
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        $result = $db->query(knjz380qQuery::getSemesterDetail($Row));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $sdate = str_replace("-","/",$row["SDATE"]);
            $edate = str_replace("-","/",$row["EDATE"]);
            $label = $row["SEMESTERNAME"];
            $value = "{$row["SEMESTER_DETAIL"]},{$sdate}～{$edate}";
            $opt[] = array("label" => "{$row["SEMESTER_DETAIL"]}：{$row["SEMESTERNAME"]}",
                           "value" => $row["SEMESTER_DETAIL"]);
        }
        $result->free();

        //出欠集計範囲
        $extra = "onChange=\"btn_submit('knjz380qForm2')\"";
        $arg["data"]["SEMESTER_DETAIL"] = knjCreateCombo($objForm, "SEMESTER_DETAIL", $Row["SEMESTER_DETAIL"], $opt, $extra, 1);

        $query = knjz380qQuery::getSemesterDetail_sdate_edate($Row);
        $s_e_date = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (get_count($s_e_date)) {
            $arg["data"]["S_E_DATE"] = str_replace("-", "/", $s_e_date["SDATE"]) . ' ～ ' . str_replace("-", "/", $s_e_date["EDATE"]);
        }

        //指導入力 1:あり 0:なし
        $extra = "id=\"SIDOU_INPUT\"";
        if ($Row["SIDOU_INPUT"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .=  "";
        }
        $extra .= "onClick=\"btn_submit('knjz380qForm2')\"";
        $arg["data"]["SIDOU_INPUT"] = knjCreateCheckBox($objForm, "SIDOU_INPUT", "1", $extra);

        //指導入力ありの場合の記号、得点
        if ($Row["SIDOU_INPUT"] !== "1") {
            $disabled = "disabled";
        } else {
            $disabled = "";
        }
        $opt = array(1, 2);
        $Row["SIDOU_INPUT_INF"] = ($Row["SIDOU_INPUT_INF"] == "") ? "1" : $Row["SIDOU_INPUT_INF"];
        $extra = array("id=\"SIDOU_INPUT_INF1\"".$disabled, "id=\"SIDOU_INPUT_INF2\"".$disabled);
        $radioArray = knjCreateRadio($objForm, "SIDOU_INPUT_INF", $Row["SIDOU_INPUT_INF"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //序列対象 1:あり 0:なし
        $extra = "id=\"JYORETSU_FLG\"";
        if ($Row["JYORETSU_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["JYORETSU_FLG"] = knjCreateCheckBox($objForm, "JYORETSU_FLG", "1", $extra);

        //CSV取込不可 1:不可 0:なし
        $extra = "id=\"NOT_USE_CSV_FLG\"";
        if ($Row["NOT_USE_CSV_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["data"]["NOT_USE_CSV_FLG"] = knjCreateCheckBox($objForm, "NOT_USE_CSV_FLG", "1", $extra);

        //追加ボタンを作成する
        $extra = "onclick=\"return doSubmit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタンを作成する
        $extra = "onclick=\"return doSubmit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return doSubmit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタンを作成する
        $extra = "onclick=\"return doSubmit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
        knjCreateHidden($objForm, "GRADE", "00");

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "knjz380qForm2"){
            $arg["reload"]  = "window.open('knjz380qindex.php?cmd=list','left_frame');";
        }

        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz380qForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
