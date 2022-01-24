<?php

require_once('for_php7.php');

class knjz030jForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz030jindex.php", "", "edit");


        $db = Query::dbCheckOut();

        //年度設定
        $result    = $db->query(knjz030jQuery::selectYearQuery());
        $opt       = array();
        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) { 
            $opt[] = array("label" => CTRL_YEAR+1,"value" => CTRL_YEAR+1);
            unset($model->examyear);

        }else{
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["ENTEXAMYEAR"],
                               "value" => $row["ENTEXAMYEAR"]);
                if ($model->examyear == $row["ENTEXAMYEAR"]){
                    $flg = true;
                }
            }
        }
        $result->free();
        //初期表示の年度設定
        if(!$flg) {
            if (!isset($model->examyear) && (CTRL_YEAR+1 == $opt[0]["value"])) {
                $model->examyear = CTRL_YEAR+1;
            } else if ($model->examyear > $opt[0]["value"]) {
                $model->examyear = $opt[0]["value"];
            } else if ($model->examyear < $opt[get_count($opt) - 1]["value"]) {
                $model->examyear = $opt[get_count($opt) - 1]["value"];
            } else {
                $model->examyear = $db->getOne(knjz030jQuery::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjz030jindex.php?cmd=edit"
                             . "&year=" .$model->examyear."';";
        }

        //年度コンボボックスを作成する
        $objForm->ae( array("type"      => "select",
                            "name"      => "year",
                            "size"      => "1",
                            "extrahtml" => "onchange=\"return btn_submit('list');\"",
                            "value"     => $model->examyear,
                            "options"   => $opt));

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_year_add",
                            "value"     => "次年度作成",
                            "extrahtml" => "onclick=\"return btn_submit('copy');\"" ));

        $arg["year"] = array("VAL"      => $objForm->ge("year")."&nbsp;&nbsp;".
                                           $objForm->ge("btn_year_add"));

        //リスト表示
        $result = $db->query(knjz030jQuery::Listdata($model->examyear));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row["DESIREDIV"] = View::alink("knjz030jindex.php", $row["DESIREDIV"],"target=right_frame",
                                             array("TESTDIV"      => $row["TESTDIV"],
                                                   "DESIREDIV"    => $row["DESIREDIV"],
                                                   "COURSECD"     => $row["COURSECD"],
                                                   "MAJORCD"      => $row["MAJORCD"],
                                                   "EXAMCOURSECO" => $row["EXAMCOURSECO"],
                                                   "cmd"          => "edit")
                                            );

            $row["TESTDIV"] = $row["TESTDIV"]."：".$row["NAME1"];
            $row["COURSE"] = $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]."：".$row["EXAMCOURSE_NAME"];
            $arg["data"][] = $row;
        }
        $result->free();

        //hiddenを作成する
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd" ) );

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::post("cmd") == "list"){
            $arg["reload"]  = "window.open('knjz030jindex.php?cmd=edit','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz030jForm1.html", $arg);
        }
    }
?>
