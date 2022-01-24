<?php

require_once('for_php7.php');

class knjz030kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz030kindex.php", "", "edit");

#        //年度を表示
#        $arg["header"] = $model->examyear;
#
#        //コピーボタンを作成する
#        $objForm->ae( array("type"        => "button",
#                            "name"        => "btn_copy",
#                            "value"       => "前年度からコピー",
#                            "extrahtml"   => "style=\"width:130px\" onclick=\"return btn_submit('copy');\"" ) );
#        $arg["btn_copy"] = $objForm->ge("btn_copy");
#
        $db = Query::dbCheckOut();
        $flg = false;

        //年度設定
        $result    = $db->query(knjz030kQuery::selectYearQuery());
        $opt       = array();
        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) { 
            $opt[] = array("label" => CTRL_YEAR+1,"value" => CTRL_YEAR+1);
            unset($model->examyear);

        }else{
            $year_flg = false;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($year_flg == false && (CTRL_YEAR+1) == $row["ENTEXAMYEAR"]) $year_flg = true;
 
                if ($year_flg == false && (CTRL_YEAR+1) > $row["ENTEXAMYEAR"]) {
 
                    $opt[] = array("label" => CTRL_YEAR+1, "value" => CTRL_YEAR+1);
                    $year_flg = true;
                }

                $opt[] = array("label" => $row["ENTEXAMYEAR"],
                               "value" => $row["ENTEXAMYEAR"]);
                if ($model->examyear == $row["ENTEXAMYEAR"]){
                    $flg = true;
                }
            }
        }

        //初期表示の年度設定
        if(!$flg && $model->examyear != (CTRL_YEAR+1)) {
            if (!isset($model->examyear)) {
                $model->examyear = CTRL_YEAR+1;
            } else if ($model->examyear > $opt[0]["value"]) {
                $model->examyear = $opt[0]["value"];
            } else if ($model->examyear < $opt[get_count($opt) - 1]["value"]) {
                $model->examyear = $opt[get_count($opt) - 1]["value"];
            } else {
                $model->examyear = $db->getOne(knjz030kQuery::DeleteAtExist($model));
                if (strlen($model->year) == 0) $model->examyear = CTRL_YEAR+1;
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjz030kindex.php?cmd=edit"
                             ."&year=".$model->examyear."';";
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
        $tmp = array();
#        $result = $db->query(knjz030kQuery::Listdata($model->examyear));
        $result = $db->query(knjz030kQuery::Listdata($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $tmp[$row["TESTDIV"].$row["DESIREDIV"]]["TESTDIV"]   = $row["TESTDIV"];
            $tmp[$row["TESTDIV"].$row["DESIREDIV"]]["DESIREDIV"] = View::alink("knjz030kindex.php", $row["DESIREDIV"],"target=right_frame",
                                                                                array("TESTDIV"      => $row["TESTDIV"],
                                                                                      "DESIREDIV"    => $row["DESIREDIV"],
                                                                                      "COURSECD"     => $row["COURSECD"],
                                                                                      "MAJORCD"      => $row["MAJORCD"],
                                                                                      "EXAMCOURSECO" => $row["EXAMCOURSECO"],
                                                                                      "cmd"          => "edit")
                                                                               );
            $tmp[$row["TESTDIV"].$row["DESIREDIV"]]["WISHNO"][]  = $row["WISHNO"];
            $tmp[$row["TESTDIV"].$row["DESIREDIV"]]["COURSE"][]  = $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"].":".$row["EXAMCOURSE_NAME"];
        }
        //表示用に配置しなおす
        foreach($tmp as $val)
        {
            $val["WISHNO"] = implode("<BR>", $val["WISHNO"]);
            $val["COURSE"] = implode("<BR>", $val["COURSE"]);
            $arg["data"][] = $val;
        }
        $result->free();

        //hiddenを作成する
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd" ) );
/*
        //初期処理
        $cntclass = $db->getOne(knjz030kQuery::cnt_Electclass(CTRL_YEAR));
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["Closing"] = "closing_window();";  //権限チェック
        }elseif($cntclass == 0){
            $arg["Closing"] = "closing_window(1);"; //利用データチェック
        }
*/
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"][] = "parent.right_frame.location.href='knjz030kindex.php?cmd=edit"
                             . "&year=".$model->examyear."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz030kForm1.html", $arg);
        }
    }
?>
