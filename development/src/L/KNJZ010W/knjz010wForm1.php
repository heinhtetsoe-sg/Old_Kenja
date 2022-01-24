<?php

require_once('for_php7.php');

class knjz010wForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz010windex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $result = $db->query(knjz010wQuery::selectYearQuery());
        $opt = array();
        //レコードが存在しなければ処理年度を登録
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($model->year == $row["VALUE"]) {
                $flg = true;
            }
        }
        $result->free();

        //初期表示の年度設定
        if (!$flg) {
            if (!isset($model->year)) {
                $model->year = CTRL_YEAR + 1;
            } else if ($model->year > $opt[0]["value"]) {
                $model->year = $opt[0]["value"];
            } else if ($model->year < $opt[get_count($opt) - 1]["value"]) {
                $model->year = $opt[get_count($opt) - 1]["value"];
            } else {
                $model->year = $db->getOne(knjz010wQuery::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjz010windex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //年度コンボボックス
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["year"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //テーブルの中身の作成
        $query = knjz010wQuery::selectQuery($model->year);
        $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["TOTALCD"] = $row["COURSECD"] . $row["MAJORCD"];

            $hash = array("cmd"             => "edit2",
                          "year"            => $row["ENTEXAMYEAR"],
                          "APPLICANTDIV"    => $row["APPLICANTDIV"],
                          "TESTDIV"         => $row["TESTDIV"],
                          "TOTALCD"         => $row["TOTALCD"],
                          "EXAMCOURSECD"    => $row["EXAMCOURSECD"]);

            $row["APPLICANTNAME"]           = $row["APPLICANTDIV"] .":". $row["APPLI_NAME"] ;
            $row["TESTDIVNAME"]             = $row["TESTDIV"] .":". $row["TESTDIV_NAME"] ;
            $row["TOTALCDNAME"]             = $row["COURSECD"] . $row["MAJORCD"] .":". $row["COURSENAME"] . $row["MAJORNAME"];
            $row["EXAMCOURSECD"]            = View::alink("knjz010windex.php", $row["EXAMCOURSECD"], "target=\"right_frame\"", $hash);
            $row["EXAMCOURSE"]              = $row["EXAMCOURSECD"] .":" . $row["EXAMCOURSE_NAME"];
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
            $arg["reload"][] = "parent.right_frame.location.href='knjz010windex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz010wForm1.html", $arg);
    }
}
?>
