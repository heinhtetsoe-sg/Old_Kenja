<?php

require_once('for_php7.php');

class knjz031form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz031index.php", "", "main");

        //年度ボックスを作成します。
        $query = knjz031Query::getYear();
        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"] . '年度',
                           "value" => $row["YEAR"]);
        }
        Query::dbCheckIn($db);

        $extra = "onchange=\"return btn_submit('');\"";
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        //上段のテーブル作成
        $query = knjz031Query::getSemeName($model->field["YEAR"]);
        $db = Query::dbCheckOut();
        $result = $db->query($query);
        $semeCnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $extra = "onblur=\"this.value=toInteger(this.value);\" ";

            if ($model->cmd == 'decision') {
                $semeSepNum = (isset($model->field["SEME_SEP_NUM"])) ? $model->field["SEME_SEP_NUM"][$semeCnt] : '';
            } else {
                $query = knjz031Query::countSemesterDetail($row["SEMESTER"], $model->field["YEAR"]);
                $semeSepNum = $db->getOne($query) ? $db->getOne($query) : 1;
            }

            $seme_sep_num = knjCreateTextBox($objForm, $semeSepNum, 'SEME_SEP_NUM[]', '', 1, $extra);

            $sem[$row["SEMESTER"]] = array("SEME_CD"      => $row["SEMESTER"],
                                           "SEME_NAME"    => $row["SEMESTERNAME"],
                                           "SEME_CNT"     => $semeSepNum,
                                           "SDATE"        => str_replace("-", "/", $row["SDATE"]),
                                           "EDATE"        => str_replace("-", "/", $row["EDATE"]),
                                           "SEME_SEP_NUM" => $seme_sep_num);
            $arg["data"][] = $sem[$row["SEMESTER"]];

            $semeCnt++;
        }
        Query::dbCheckIn($db);

        //下段のテーブル作成
        if ($model->cmd == 'decision') {
            $count_sdate = decisionSemesterDitail($objForm, $arg, $sem, $model);
        } else {
            $count_sdate = dbSemesterDitail($objForm, $arg, $db, $sem, $model);
        }


        //確定ボタン
        $extra = "onclick=\"return btn_submit('decision');\"";
        $arg["button"]["btn_decision"] = knjCreateBtn($objForm, 'btn_decision', '確 定', $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        //Hidden作成
        makeHidden($objForm, $sem, $count_sdate);


        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz031Form1.html", $arg);
    }
}

function decisionSemesterDitail(&$objForm, &$arg, $sem, $model)
{
    $ditailCode = 1;
    $countRow = 1;              //テーブルのrowspanつけるかつけないかの判定に使っています。
    $rowFlag = true;            //テーブルのrowspanつけるかつけないかの判定に使っています。
    $arg["IFRAME"] = View::setIframeJs();
    foreach ($sem as $key => $val) {
        for ($i = 1; $i <= $val["SEME_CNT"]; $i++) {
            $extra = "onblur=\"isDate(this);\" ";

            $semester_name_box = knjCreateTextBox($objForm, '', "SEMESTER_NAME[]", '10', '5', '');

            $sval = $i == 1 ? $val["SDATE"] : '';
            $sval_box = View::popUpCalendarAlp2($objForm, "SDATE".$ditailCode, $sval, "");

            $eval = $i == $val["SEME_CNT"] ? $val["EDATE"] : "";
            $eval_box = View::popUpCalendarAlp2($objForm, "EDATE".$ditailCode, $eval, "");

            $sem_cd    = $val["SEME_CD"];
            $sem_name  = $val["SEME_NAME"];
            $hiddenVal = $val["SEME_CD"] . "," . $ditailCode . "," . $val["SDATE"] . "," . $val["EDATE"];

            $put_sem = array();
            $put_sem = array("SEM_CD"        => $sem_cd,
                             "SEM_NAME"      => $sem_name,
                             "SEM_DITAIL_CD" => $ditailCode,
                             "SEMESTER_NAME" => $semester_name_box,
                             "SDATE"         => $sval_box,
                             "EDATE"         => $eval_box,
                             "SEM_KEY"       => $hiddenVal);
            if ($rowFlag) {
                $put_sem["ROWSPAN"] = $val["SEME_CNT"];
                $rowFlag = false;
            }
            if ($countRow == $val["SEME_CNT"]) {
                $rowFlag = true;
                $countRow = 0;
            }
            $countRow++;
            $arg["date"][] = $put_sem;
            $ditailCode++;
        }
    }
    return $ditailCode - 1;
}

function dbSemesterDitail(&$objForm, &$arg, $db, $sem, $model)
{
    $db = Query::dbCheckOut();
    $query = knjz031Query::getSdate($model->field["YEAR"]);
    $result = $db->query($query);
    $ditailCode = 1;
    $countRow = 1;              //テーブルのrowspanつけるかつけないかの判定に使っています。
    $rowFlag = true;            //テーブルのrowspanつけるかつけないかの判定に使っています。
    $arg["IFRAME"] = View::setIframeJs();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $extra = "onblur=\"isDate(this);\" ";

        $semester_name_box = knjCreateTextBox($objForm, $row["SEMESTER_NAME"], "SEMESTER_NAME[]", '10', '5', '');
        $sval = str_replace("-", "/", $row["SDATE"]);
        $sval = View::popUpCalendarAlp2($objForm, "SDATE".$ditailCode, $sval, "");

        $eval = str_replace("-", "/", $row["EDATE"]);
        $eval = View::popUpCalendarAlp2($objForm, "EDATE".$ditailCode, $eval, "");

        $hiddenVal = $row["SEM_CD"] . "," . $ditailCode . "," . $sem[$row["SEM_CD"]]["SDATE"] . "," . $sem[$row["SEM_CD"]]["EDATE"];
        $put_sem = array();
        $put_sem = array("SEM_CD"        => $row["SEM_CD"],
                         "SEM_NAME"      => $row["SEM_NAME"],
                         "SEM_DITAIL_CD" => $row["SEM_DITAIL_CD"],
                         "SEMESTER_NAME" => $semester_name_box,
                         "SDATE"         => $sval,
                         "EDATE"         => $eval,
                         "SEM_KEY"       => $hiddenVal);
        if ($rowFlag) {
            $put_sem["ROWSPAN"] = $sem[$row["SEM_CD"]]["SEME_CNT"];
            $rowFlag = false;
        }
        if ($countRow == $sem[$row["SEM_CD"]]["SEME_CNT"]) {
            $rowFlag = true;
            $countRow = 0;
        }
        $countRow++;
        $arg["date"][] = $put_sem;
        $ditailCode++;
    }
    if (!isset($put_sem)) {
        return decisionSemesterDitail($objForm, $arg, $sem, $model);
    }
    Query::dbCheckIn($db);
    return $ditailCode - 1;
}

//Hidden作成
function makeHidden(&$objForm, $sem, $count_sdate)
{
    knjCreateHidden($objForm, "cmd");
    foreach ($sem as $key => $val) {
        knjCreateHidden($objForm, "SEM_MST_SDATE".$val["SEME_CD"], $val["SDATE"]);
        knjCreateHidden($objForm, "SEM_MST_EDATE".$val["SEME_CD"], $val["EDATE"]);
    }
    knjCreateHidden($objForm, 'COUNT_SDATE', $count_sdate);
}
