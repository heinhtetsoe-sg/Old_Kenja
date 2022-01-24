<?php
class knjl413hForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl413hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl413hQuery::selectYearQuery($model);
        makeCombo($objForm, $arg, $db, $query, "year", $model->year, $extra, 1, "");

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl413hQuery::getNameMst($model, "L003", "default");
        makeCombo($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //受験コースコンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl413hQuery::getExamCourseMst($model);
        makeCombo($objForm, $arg, $db, $query, "EXAMCOURSECD", $model->examcoursecd, $extra, 1);

        //テーブルの中身の作成
        $query  = knjl413hQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg["data"]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $hash = array("cmd"          => "edit2",
                          "APPLICANTDIV" => $row["APPLICANTDIV"],
                          "EXAMCOURSECD" => $row["EXAMCOURSECD"],
                          "SHDIV"        => $row["SHDIV"]);

            $row["SHDIV_LABEL"] = View::alink("knjl413hindex.php", $row["SHDIV_LABEL"], "target=\"right_frame\"", $hash);

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        $flg = in_array($model->cmd, array("copy"));
        if (!isset($model->warning) && $flg) {
            $arg["reload"][] = "parent.right_frame.location.href='knjl413hindex.php?cmd=edit"
                             . "&year=".$model->year."&applicantdiv="."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl413hForm1.html", $arg);
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
