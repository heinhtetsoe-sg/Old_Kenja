<?php

require_once('for_php7.php');

class knjz380qForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz380qindex.php", "", "edit");

        //年度を表示
        $arg["header"] = CTRL_YEAR;

        //コピーボタンを作成する
        $extra = "style=\"width:130px\" onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //テスト期間表示切替
        if ($model->Properties["Test_Period_Hyouji"] == "1") {
            $arg["Test_Period_Hyouji"] = 1;
        }

        //出欠集計範囲表示切替
        if ($model->Properties["Semester_Detail_Hyouji"] == "1") {
            $arg["sem_detail"] = 1;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //課程学科コンボ
        $extra = "onChange=\"return btn_submit('changeCm')\"";
        $query = knjz380qQuery::getCourseMajor($model);
        makeCmb($objForm, $arg, $db, $query, "COURSE_MAJOR", $model->leftCourseMajor, $extra, 1, "");

        //コピー元課程学科コンボ
        $extra = "";
        $query = knjz380qQuery::getCourseMajor($model);
        makeCmb($objForm, $arg, $db, $query, "MOTO_COURSE_MAJOR", $model->motoCourseMajor, $extra, 1, "BLANK");

        //コピーボタンを作成する
        $extra = "style=\"width:80px\" onclick=\"return btn_submit('copy2');\"";
        $arg["btn_copy2"] = knjCreateBtn($objForm, "btn_copy2", "からコピー", $extra);

        $query  = knjz380qQuery::getListdata($model);
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //権限チェック
            if ($model->sec_competence == DEF_NOAUTH || $model->sec_competence == DEF_REFERABLE || $model->sec_competence == DEF_REFER_RESTRICT) {
                break;
            }

            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $row["TESTKIND_SHOW"] = $row["TESTKINDCD"]." ".$row["TESTKINDNAME"];
            //リンク作成
            $row["TESTKIND_SHOW"] = View::alink("knjz380qindex.php", $row["TESTKIND_SHOW"], "target=\"right_frame\"",
                                          array("cmd"  =>"edit",
                                                "SCHOOLCD"    =>$row["SCHOOLCD"],
                                                "SCHOOL_KIND" =>$row["SCHOOL_KIND"],
                                                "SEMESTER"    =>$row["SEMESTER"],
                                                "TESTKINDCD"  =>$row["TESTKINDCD"],
                                                "TESTITEMCD"  =>$row["TESTITEMCD"],
                                                "SCORE_DIV"   =>$row["SCORE_DIV"],
                                                "GRADE"       =>$row["GRADE"],
                                                "COURSECD"    =>$row["COURSECD"],
                                                "MAJORCD"     =>$row["MAJORCD"],
                                                "UPDATED"     =>$row["UPDATED"]
                                                ));
            $row["TESTITEM_SHOW"] = $row["TESTITEMCD"].'-'.$row["SCORE_DIV"]." ".$row["TESTITEMNAME"];

            $to = (strlen($row["TEST_START_DATE"]) && strlen($row["TEST_END_DATE"])) ? "～" : "";
            $test_start_date    = str_replace("-", "/",  $row["TEST_START_DATE"]);
            $test_end_date      = str_replace("-", "/",  $row["TEST_END_DATE"]);
            $row["TEST_DATE"] = $test_start_date.$to.$test_end_date;

            $arg["data"][] = $row;
        }
        $result->free();

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //権限
        if ($model->sec_competence == DEF_NOAUTH || $model->sec_competence == DEF_REFERABLE || $model->sec_competence == DEF_REFER_RESTRICT) {
            $arg["Closing"]  = " closing_window(); " ;
        }
        if ($model->cmd == "change") {
            $arg["reload"] = "window.open('knjz380qindex.php?cmd=edit','right_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz380qForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
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
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
