<?php

require_once('for_php7.php');

class knjz030rForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz030rindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $result = $db->query(knjz030rQuery::selectYearQuery($model));
        $opt    = array();
        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) { 
            $opt[] = array("label" => CTRL_YEAR+1, "value" => CTRL_YEAR+1);
            unset($model->year);
        } else {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["ENTEXAMYEAR"],
                               "value" => $row["ENTEXAMYEAR"]);
                if ($model->year == $row["ENTEXAMYEAR"]){
                    $flg = true;
                }
            }
        }
        $result->free();

        //初期表示の年度設定
        if(!$flg) {
            if (!isset($model->year)) {
                $model->year = CTRL_YEAR + 1;
            } else if ($model->year > $opt[0]["value"]) {
                $model->year = $opt[0]["value"];
            } else if ($model->year < $opt[get_count($opt) - 1]["value"]) {
                $model->year = $opt[get_count($opt) - 1]["value"];
            } else {
                $model->year = $db->getOne(knjz030rQuery::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjz030rindex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //年度コンボボックスを作成する
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["year"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //リスト表示
        $tmp = array();
        $result = $db->query(knjz030rQuery::Listdata($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $tmp[$row["APPLICANTDIV"].$row["TESTDIV"].$row["DESIREDIV"]]["APPLICANTDIV"]    = $row["APPLICANTDIV"];
            $tmp[$row["APPLICANTDIV"].$row["TESTDIV"].$row["DESIREDIV"]]["TESTDIV"]         = $row["TESTDIV"];
            $tmp[$row["APPLICANTDIV"].$row["TESTDIV"].$row["DESIREDIV"]]["DESIREDIV"]       = View::alink("knjz030rindex.php", $row["DESIREDIV"],"target=right_frame",
                                                                                array("APPLICANTDIV"    => $row["APPLICANTDIV"],
                                                                                      "TESTDIV"         => $row["TESTDIV"],
                                                                                      "DESIREDIV"       => $row["DESIREDIV"],
                                                                                      "cmd"             => "edit2")
                                                                               );
            $tmp[$row["APPLICANTDIV"].$row["TESTDIV"].$row["DESIREDIV"]]["WISHNO"][]  = $row["WISHNO"];
            $tmp[$row["APPLICANTDIV"].$row["TESTDIV"].$row["DESIREDIV"]]["COURSE"][]  = $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"].":".$row["EXAMCOURSE_NAME"];
        }
        //表示用に配置しなおす
        foreach($tmp as $val)
        {
            $val["WISHNO"] = implode("<BR>", $val["WISHNO"]);
            $val["COURSE"] = implode("<BR>", $val["COURSE"]);
            $arg["data"][] = $val;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"][] = "parent.right_frame.location.href='knjz030rindex.php?cmd=edit"
                             . "&year=".$model->examyear."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz030rForm1.html", $arg);
        }
    }
?>
