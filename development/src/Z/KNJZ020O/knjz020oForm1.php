<?php

require_once('for_php7.php');

class knjz020oForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz020oindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $result    = $db->query(knjz020oQuery::selectYearQuery());
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
                $model->year = $db->getOne(knjz020oQuery::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjz020oindex.php?cmd=edit"
                             . "&year=".$model->year."&TOATLCD=".$model->examcoursecd."';";
        }

        //年度コンボボックスを作成する
        $objForm->ae( array("type"      => "select",
                            "name"      => "year",
                            "size"      => "1",
                            "extrahtml" => "onchange=\"return btn_submit('list');\"",
                            "value"     => $model->year,
                            "options"   => $opt));

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_year_add",
                            "value"     => "次年度作成",
                            "extrahtml" => "onclick=\"return btn_submit('copy');\"" ));

        $arg["year"] = array("VAL"      => $objForm->ge("year")."&nbsp;&nbsp;".
                                           $objForm->ge("btn_year_add"));

        //コース設定
        $result    = $db->query(knjz020oQuery::get_Course($model->year));
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]. ":" . $row["COURSENAME"],
                           "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
        }
        $result->free();

        //コースコンボを作成する
        $objForm->ae( array("type"      => "select",
                            "name"      => "TOTALCD",
                            "size"      => "1",
                            "extrahtml" => "onchange=\"return btn_submit('list');\"",
                            "value"     => $model->examcoursecd,
                            "options"   => $opt));
        $arg["TOTALCD"] = $objForm->ge("TOTALCD");

        if ($model->examcoursecd == "") {
            $model->examcoursecd = $opt[0]["value"];
        }

        //リスト表示
        $result = $db->query(knjz020oQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             $hash = array("cmd"            => "edit",
                           "TESTDIV"        => $row["TESTDIV"],
                           "TESTSUBCLASSCD" => $row["TESTSUBCLASSCD"],
                           "TOTALCD"        => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
                    
            $row["TESTDIV"] = $row["TESTDIV"].":".$row["TESTNAME"];                    
            $row["TESTSUBCLASSCD"] = View::alink("knjz020oindex.php", $row["TESTSUBCLASSCD"].":".$row["SUBCLASSNAME"], "target=right_frame", $hash);
            $arg["data"][] = $row;
       }
       $result->free();

        //hiddenを作成する
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd" ) );                           
                            
        Query::dbCheckIn($db);

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjz020oindex.php?cmd=edit"
                             . "&year=".$model->year."&TOATLCD=".$model->examcoursecd."';";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz020oForm1.html", $arg);
        }
    }
?>
