<?php

require_once('for_php7.php');

class knjz020fForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz020findex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $result = $db->query(knjz020fQuery::selectYearQuery());
        $opt = array();
        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) {
            $opt[] = array("label" => CTRL_YEAR+1, "value" => CTRL_YEAR+1);
            unset($model->year);
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["ENTEXAMYEAR"],
                               "value" => $row["ENTEXAMYEAR"]);
                if ($model->year == $row["ENTEXAMYEAR"]) {
                    $flg = true;
                }
            }
        }
        $result->free();

        //初期表示の年度設定
        if (!$flg) {
            if (!isset($model->year)) {
                $model->year = CTRL_YEAR + 1;
            } elseif ($model->year > $opt[0]["value"]) {
                $model->year = $opt[0]["value"];
            } elseif ($model->year < $opt[get_count($opt) - 1]["value"]) {
                $model->year = $opt[get_count($opt) - 1]["value"];
            } else {
                $model->year = $db->getOne(knjz020fQuery::deleteAtExist($model));
            }
            if ($model->cmd == 'list') {
                $arg["reload"][] = "parent.right_frame.location.href='knjz020findex.php?cmd=edit"
                                 . "&year=". $model->year ."&applicantdiv=". $model->applicantdiv ."&testdiv=". $model->testdiv ."';";
            }
        }

        //年度コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["year"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjz020fQuery::getNameMst($model, "L003", "default");
        makeCombo($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボ
        $model->testdiv = ($model->cmd != "" || $model->applicantdiv == $model->field["APP_HOLD"]) ? $model->testdiv : "";
        $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query = knjz020fQuery::getNameMst($model, $namecd1, "default");
        makeCombo($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //課程学科件数取得
        $t_cnt = array();
        $result = $db->query(knjz020fQuery::getDataCnt($model, "totalcd"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $t_cnt[$row["TOTALCD"]] = $row["CNT"];
        }
        $result->free();
        //受験コース件数取得
        $e_cnt = array();
        $result = $db->query(knjz020fQuery::getDataCnt($model, "examcoursecd"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $e_cnt[$row["TOTALCD"]][$row["EXAMCOURSECD"]] = $row["CNT"];
        }
        $result->free();

        //中学のとき、受験型表示
        if ($model->applicantdiv == "1") {
            $arg["exam_type"] = 1;
        }

        //テーブルの中身の作成
        $t_bifKey = $e_bifKey = "";
        $query  = knjz020fQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg["data"]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $hash = array("cmd"             => "edit2",
                          "TOTALCD"         => $row["TOTALCD"],
                          "EXAMCOURSECD"    => $row["EXAMCOURSECD"],
                          "TESTSUBCLASSCD"  => $row["TESTSUBCLASSCD"],
                          "EXAM_TYPE"       => $row["EXAM_TYPE"]);

            $row["TESTSUBCLASSCD"] = View::alink("knjz020findex.php", $row["TESTSUBCLASSCD"].":".$row["TESTSUBCLASSNAME"], "target=\"right_frame\"", $hash);

            //重複した課程学科はまとめる
            if ($t_bifKey !== $row["TOTALCD"]) {
                $cnt = $t_cnt[$row["TOTALCD"]];
                $row["ROWSPAN1"] = $cnt > 0 ? $cnt : 1;
            }
            $t_bifKey = $row["TOTALCD"];
            //重複した受験コースはまとめる
            if ($e_bifKey !== $row["EXAMCOURSECD"]) {
                $cnt = $e_cnt[$row["TOTALCD"]][$row["EXAMCOURSECD"]];
                $row["ROWSPAN2"] = $cnt > 0 ? $cnt : 1;
            }
            $e_bifKey = $row["EXAMCOURSECD"];

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
            $arg["reload"][] = "parent.right_frame.location.href='knjz020findex.php?cmd=edit"
                             . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."&testdiv=".$model->testdiv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz020fForm1.html", $arg);
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
