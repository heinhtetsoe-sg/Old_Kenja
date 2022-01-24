<?php

require_once('for_php7.php');

class knjl451hForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"] = $objForm->get_start("main", "POST", "knjl451hindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ--------------------------------------------

        //入試年度
        $arg["CTRL_YEAR"] = $model->year ."年度";

        //入試制度コンボ
        $query = knjl451hQuery::getName($model->year, "L003");
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        //試験回数コンボ
        $query = knjl451hQuery::getEntexamSettingMst($model, "L004");
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "");

        //志望コースコンボ
        $query = knjl451hQuery::getExamCourseMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->coursecd, "EXAMCOURSECD", $extra, 1, "");

        //受験型コンボ
        $query = knjl451hQuery::getExamtypeMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->examtype, "EXAM_TYPE", $extra, 1, "");

        //出願区分コンボ
        $query = knjl451hQuery::getEntexamSettingMst($model, "L006");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->shdiv, "SHDIV", $extra, 1, "");


        //データ2-------------------------------------------

        if ($model->cmd == "main" || isset($model->warning)) {
            $Row["BORDER_SCORE"] = $model->field["BORDER_SCORE"];
            $Row["GOUKAKU_CNT"]  = $model->field["GOUKAKU_CNT"];
            $Row["NAIDAKU_CNT"]  = $model->field["NAIDAKU_CNT"];
        } elseif ($model->cmd == "simShow") {
            //確認結果表示
            $query = knjl451hQuery::getCntJudge($model);
            $simRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["BORDER_SCORE"] = $model->field["BORDER_SCORE"];
            $Row["GOUKAKU_CNT"]  = $simRow["GOUKAKU_CNT"];
            $Row["NAIDAKU_CNT"]  = $simRow["NAIDAKU_CNT"];
        } elseif ($model->cmd == "edit" || $model->cmd == "decisionShow") {
            //確定結果表示
            $query = knjl451hQuery::getCntPassingmark($model);
            $passingRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["BORDER_SCORE"] = $passingRow["BORDER_SCORE"];
            $Row["GOUKAKU_CNT"]  = $passingRow["GOUKAKU_CNT"];
            $Row["NAIDAKU_CNT"]  = $passingRow["NAIDAKU_CNT"];
        } else {
            $Row["BORDER_SCORE"] = "";
            $Row["GOUKAKU_CNT"]  = "";
            $Row["NAIDAKU_CNT"]  = "";
        }

        //初期値
        if (!strlen($Row["GOUKAKU_CNT"])) {
            $Row["GOUKAKU_CNT"] = 0;
        }
        if (!strlen($Row["NAIDAKU_CNT"])) {
            $Row["NAIDAKU_CNT"] = 0;
        }

        //合格点
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["BORDER_SCORE"] = knjCreateTextBox($objForm, $Row["BORDER_SCORE"], "BORDER_SCORE", 5, 3, $extra);

        //合格数
        $arg["dataCnt"]["GOUKAKU_CNT"] = $Row["GOUKAKU_CNT"];
        knjCreateHidden($objForm, "GOUKAKU_CNT", $Row["GOUKAKU_CNT"]);

        //内諾者数
        $arg["dataCnt"]["NAIDAKU_CNT"] = $Row["NAIDAKU_CNT"];
        knjCreateHidden($objForm, "NAIDAKU_CNT", $Row["NAIDAKU_CNT"]);

        //確定結果一覧--------------------------------------

        //ヘッダ

        //合格点マスタ
        $query = knjl451hQuery::selectQueryPassingmark($model, "LIST");
        $result = $db->query($query);
        while ($passingRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //リンク
            $hash = array("cmd"             => "edit",
                          "APPLICANTDIV"    => $model->applicantdiv,
                          "TESTDIV"         => $passingRow["TESTDIV"],
                          "EXAMCOURSECD"    => $passingRow["EXAMCOURSECD"],
                          "EXAM_TYPE"       => $passingRow["EXAM_TYPE"],
                          "SHDIV"           => $passingRow["SHDIV"]
            );
            $passingRow["BORDER_SCORE"] = View::alink("knjl451hindex.php", $passingRow["BORDER_SCORE"], "", $hash);
            $passingRow["TOTAL_CNT"] = $passingRow["GOUKAKU_CNT"];

            $arg["dataD"][] = $passingRow;
            $arg["dataSum"]["GOUKAKU_CNT"] += $passingRow["GOUKAKU_CNT"];
            $arg["dataSum"]["NAIDAKU_CNT"] += $passingRow["NAIDAKU_CNT"];
            $arg["dataSum"]["TOTAL_CNT"] += $passingRow["TOTAL_CNT"];
        }//while
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //確認ボタン
        $extra = "onclick=\"return btn_submit('sim');\"";
        $arg["btn_sim"] = knjCreateBtn($objForm, "btn_sim", "確 認", $extra);

        //確定ボタン
        $disBtn = ($model->cmd == "simShow") ? "" : " disabled";
        $extra = "onclick=\"return btn_submit('decision');\"";
        $arg["btn_decision"] = knjCreateBtn($objForm, "btn_decision", "確 定", $extra.$disBtn);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl451hForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
        if ($row["NAMESPARE2"] == '1' && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
