<?php
class knjl402hForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl402hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl402hQuery::selectYearQuery($model);
        makeCombo($objForm, $arg, $db, $query, "year", $model->year, $extra, 1, "");

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl402hQuery::getApplicantdiv($model->year);
        makeCombo($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //テーブルの中身の作成
        $query = knjl402hQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["TOTALCD"] = $row["COURSECD"] . $row["MAJORCD"];

            $hash = array("cmd"           => "edit2",
                          "year"          => $row["ENTEXAMYEAR"],
                          "APPLICANTDIV"  => $row["APPLICANTDIV"],
                          "TOTALCD"       => $row["TOTALCD"],
                          "EXAMCOURSECD"  => $row["EXAMCOURSECD"]);

            $row["APPLICANTNAME"]   = $row["APPLICANTDIV"] .":". $row["APPLI_NAME"] ;
            $row["TOTALCDNAME"]     = $row["COURSECD"] . $row["MAJORCD"] .":". $row["COURSENAME"] . $row["MAJORNAME"];
            $row["EXAMCOURSECD"]    = View::alink("knjl402hindex.php", $row["EXAMCOURSECD"], "target=\"right_frame\"", $hash);
            $row["EXAMCOURSE"]      = $row["EXAMCOURSECD"] .":" . $row["EXAMCOURSE_NAME"];
            $row["ENTER_TOTALCDNAME"]       = $row["ENTER_COURSECD"] . $row["ENTER_MAJORCD"] .":". $row["ENTER_COURSENAME"] . $row["ENTER_MAJORNAME"];
            $row["ENTER_COURSECODENAME"]    = $row["ENTER_COURSECODE"] .":" . $row["ENTER_COURSECODENAME"];
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjl402hindex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl402hForm1.html", $arg);
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
