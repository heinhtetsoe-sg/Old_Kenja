<?php

require_once('for_php7.php');

class knjz020cForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz020cindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $result    = $db->query(knjz020cQuery::selectYearQuery());
        $opt       = array();
        $flg       = false;
        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) {
            $opt[] = array("label" => CTRL_YEAR+1,"value" => CTRL_YEAR+1);
            unset($model->year);

        }else{
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["ENTEXAMYEAR"],
                               "value" => $row["ENTEXAMYEAR"]);
                if ($model->year == $row["ENTEXAMYEAR"]) {
                    $flg = true;
                }
            }
        }
        $result->free();

        //初期表示の年度設定
        if(!$flg) {
            if (!isset($model->year)) {
                $model->year = CTRL_YEAR+1;

            } else if ($model->year > $opt[0]["value"]) {
                $model->year = $opt[0]["value"];

            } else if ($model->year < $opt[get_count($opt) - 1]["value"]) {
                $model->year = $opt[get_count($opt) - 1]["value"];

            } else {
                $model->year = $db->getOne(knjz020cQuery::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjz020cindex.php?cmd=edit"
                             . "&year=".$model->year."&TOATLCD=".$model->totalcd."';";
        }

        //年度ボックス
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["year"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //コースコンボを作成する
        $query     = knjz020cQuery::get_Course($model->year);
        $result    = $db->query($query);
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["TOTALCD"] = knjCreateCombo($objForm, "TOTALCD", $model->totalcd, $opt, $extra, 1);
        if ($model->totalcd == "") {
            $model->totalcd = $opt[0]["value"];
        }

        //リスト表示
        $query  = knjz020cQuery::selectQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             $hash = array("cmd"            => "edit",
                           "APPLICANTDIV"   => $row["APPLICANTDIV"],
                           "TESTDIV"        => $row["TESTDIV"],
                           "TESTSUBCLASSCD" => $row["TESTSUBCLASSCD"],
                           "TOTALCD"        => $model->totalcd);

            $row["APPLICANTNAME"]  = $row["APPLICANTDIV"] .":". $row["SCHOOL"];
            $row["TESTDIV"] = $row["TESTDIV"].":".$row["TESTNAME"];
            $row["TOTALCDNAME"]    = $row["COURSECD"] . $row["MAJORCD"] .":". $row["COURSENAME"] . $row["MAJORNAME"];
            $row["EXAMCOURSE"]     = $row["EXAMCOURSECD"] .":" . $row["EXAMCOURSE_NAME"];
            $row["TESTSUBCLASSCD"] = View::alink("knjz020cindex.php", $row["TESTSUBCLASSCD"].":".$row["SUBCLASSNAME"], "target=right_frame", $hash);
            $arg["data"][] = $row;
        }
        $result->free();

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd", "");

        Query::dbCheckIn($db);

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjz020cindex.php?cmd=edit"
                             . "&year=".$model->year."&TOATLCD=".$model->totalcd."';";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz020cForm1.html", $arg);
        }
    }
?>
