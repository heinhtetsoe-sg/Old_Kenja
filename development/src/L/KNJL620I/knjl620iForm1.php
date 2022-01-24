<?php

require_once('for_php7.php');

class knjl620iForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $arg["start"] = $objForm->get_start("main", "POST", "knjl620iindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ--------------------------------------------

        //入試年度
        $arg["TOP"]["CTRL_YEAR"] = $model->examyear ."年度";

        //学科コンボ
        $opt = array();
        $opt[] = array('label' => "-- 全て --", 'value' => "ALL");
        $opt[] = array('label' => "1:普通科", 'value' => "1");
        $opt[] = array('label' => "2:工業科", 'value' => "2");
        $extra = "onchange=\"return btn_submit('chgMajor')\"";
        $model->majorcd = ($model->majorcd != "") ? $model->majorcd : $opt[0]["value"];
        $arg["TOP"]["MAJORCD"] = knjCreateCombo($objForm, "MAJORCD", $model->majorcd, $opt, $extra, 1);

        //試験コンボ
        $model->testdiv = ($model->cmd == "chgMajor") ? "" : $model->testdiv;
        $query = knjl620iQuery::getTestdivMst($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        $arg["TOP"]["TESTDIV"] = makeCmb($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "");

        //出力指示画面---------------------------------------

        $opt = array(1, 2);
        $model->outputSort = ($model->outputSort == "") ? "1" : $model->outputSort;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT_SORT{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT_SORT", $model->outputSort, $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["radio"][$key] = $val;
        }

        //--------------------------------------------------

        //リストを画面に表示
        $count = 0;
        $judgeState = "";
        $model->courseData = array();
        $query = knjl620iQuery::selectQuery($model, $namecd1);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $judgeState = $row["JUDGE_STATE"];
            $course = $row["PASS_COURSE_CD"];
            $model->courseData[] = $course;

            //コース
            $outputId = "OUTPUT_CHK-".$course;
            $row["OUTPUT_ID"] = $outputId;

            $extra = " id=\"{$outputId}\" ";
            $checked = ($model->field["OUTPUT_CHK"][$course] == $course) ? " checked " : "";
            $row["OUTPUT_CHK"] = knjCreateCheckBox($objForm, "OUTPUT_CHK", $course, $extra.$checked, "1");
            //合格点
            $extra = "";
            $disabled = ($judgeState != "") ? " style=\"background-color:lightgray;\" readonly " : "";
            $value = (isset($model->warning) || $model->cmd == "clearShow") ? $model->field["BORDER_SCORE"][$course] : $row["BORDER_SCORE"];
            $row["BORDER_SCORE"] = knjCreateTextBox($objForm, $value, "BORDER_SCORE"."-".$course, 5, 3, $extra.$disabled);

            //判定状況
            $row["JUDGE_STATE"] = ($judgeState == "1") ? "仮判定" : (($judgeState == "2") ? "確定" : "");
    
            $arg["data"][] = $row;

            $count++;
        }
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg, $model, $judgeState);

        //DB切断
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID");
        knjCreateHidden($objForm, "COURSE_CNT", $count);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl620iForm1.html", $arg);
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
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg, $model, $judgeState)
{
    //仮判定ボタン
    $extra = "onclick=\"return btn_submit('sim');\"";
    $disable = ($model->majorcd == "ALL" || $judgeState != "") ? " disabled " : "";
    $arg["btn_sim"] = knjCreateBtn($objForm, "btn_sim", "仮判定", $extra.$disable);

    //判定取り消しボタン
    $disable = ($model->majorcd != "ALL" && $judgeState != "") ? "" : " disabled ";
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "判定取消", $extra.$disable);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);


    //判定資料出力ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'KNJL620I_HANTEI');\"";
    $arg["btn_output1"] = knjCreateBtn($objForm, "btn_output1", "判定資料出力", $extra);

    //成績表出力ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'KNJL620I_SEISEKI');\"";
    $arg["btn_output2"] = knjCreateBtn($objForm, "btn_output2", "成績表出力", $extra);
}
