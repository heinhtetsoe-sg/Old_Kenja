<?php
class knjl403hForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl403hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl403hQuery::selectYearQuery();
        makeCombo($objForm, $arg, $db, $query, "year", $model->year, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl403hQuery::getNameMst($model, "L003", "default");
        makeCombo($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試回数コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl403hQuery::getSettingMst($model, "L004");
        makeCombo($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        $t_cnt = 1;
        $e_cnt = 1;
        $et_cnt = 1;

        //テーブルの中身の作成
        $query  = knjl403hQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg["data"]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $hash = array("cmd"             => "edit2",
                          "TOTALCD"         => $row["TOTALCD"],
                          "EXAMCOURSECD"    => $row["EXAMCOURSECD"],
                          "TESTSUBCLASSCD"  => $row["TESTSUBCLASSCD"],
                          "EXAM_TYPE"       => $row["EXAM_TYPE"]);

            $row["TESTSUBCLASSCD"] = View::alink("knjl403hindex.php", $row["TESTSUBCLASSNAME"], "target=\"right_frame\"", $hash);

            //重複した課程学科はまとめる
            if ($t_cnt == 1) {
                $row["ROWSPAN1"] = $row["TOTAL_CNT"];
                if ($row["TOTAL_CNT"] > 1) {
                    $t_cnt++;
                }
            } elseif ($t_cnt == $row["TOTAL_CNT"]) {
                $t_cnt = 1;
            } else {
                $t_cnt++;
            }

            //重複した受験コースはまとめる
            if ($e_cnt == 1) {
                $row["ROWSPAN2"] = $row["COURSE_CNT"];
                if ($row["COURSE_CNT"] > 1) {
                    $e_cnt++;
                }
            } elseif ($e_cnt == $row["COURSE_CNT"]) {
                $e_cnt = 1;
            } else {
                $e_cnt++;
            }

            //重複した受験型はまとめる
            if ($et_cnt == 1) {
                $row["ROWSPAN3"] = $row["EXAMTYPE_CNT"];
                if ($row["EXAMTYPE_CNT"] > 1) {
                    $et_cnt++;
                }
            } elseif ($et_cnt == $row["EXAMTYPE_CNT"]) {
                $et_cnt = 1;
            } else {
                $et_cnt++;
            }

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjl403hindex.php?cmd=edit"
                             . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."&testdiv=".$model->testdiv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl403hForm1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
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

        if ($row["DEFAULT"] && $default_flg) {
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
