<?php

require_once('for_php7.php');

class knjz020kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz020kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $result    = $db->query(knjz020kQuery::selectYearQuery());
        $opt       = array();
        $flg       = false;

        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) { 
            $opt[] = array("label" => CTRL_YEAR+1,"value" => CTRL_YEAR+1);
            unset($model->year);

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
                if ($model->year == $row["ENTEXAMYEAR"]) {
                    $flg = true;
                }
            }
        }
        $result->free();

        $param_flg = false;
        //初期表示の年度設定
        if(!$flg && $model->year != (CTRL_YEAR+1)) {
            if (!isset($model->year)) {
                $model->year = CTRL_YEAR+1;
            } else if ($model->year > $opt[0]["value"]) {
                $model->year = $opt[0]["value"];

            } else if ($model->year < $opt[get_count($opt) - 1]["value"]) {
                $model->year = $opt[get_count($opt) - 1]["value"];

            } else {
                $model->year = $db->getOne(knjz020kQuery::DeleteAtExist($model));
                if (strlen($model->year) == 0) $model->year = CTRL_YEAR+1;

            }
            $param_flg = true;
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

        //コース
        $result = $db->query(knjz020kQuery::getCourse($model->year));
        $opt       = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]."：".$row["EXAMCOURSE_NAME"],
                           "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
        }
        $model->course = (strlen($model->course))? $model->course : $opt[0]["value"] ;
        $objForm->ae( array("type"        => "select",
                            "name"        => "COURSE",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('list')\";",
                            "value"       => $model->course,
                            "options"     => $opt));
        $arg["header"]["COURSE"] = $objForm->ge("COURSE");

        //パラメータ設定
        if ($param_flg) {
            $arg["reload"][] = "parent.right_frame.location.href='knjz020kindex.php?cmd=edit"
                              ."&year=".$model->year."&COURSE=".$model->course."';";
        }

        //リスト表示
        $result = $db->query(knjz020kQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["TESTDIVNAME"]  = $row["TESTDIV"].":".$row["TESTNAME"];
            $row["TESTSUBCLASS"] = View::alink("knjz020kindex.php", $row["TESTSUBCLASSCD"].":".$row["SUBCLASSNAME"], "target=\"right_frame\"",
                                                array("cmd"             => "edit",
                                                      "TESTDIV"         => $row["TESTDIV"],
                                                      "TESTSUBCLASSCD"  => $row["TESTSUBCLASSCD"],
                                                      "COURSE"          => $model->course ) );
            $row["AUTOCALC"]     = ($row["AUTOCALC"] == '1')?    "あり": "なし";
            $row["ADOPTIONDIV"]  = ($row["ADOPTIONDIV"] == '1')? "あり": "";

            $arg["data"][] = $row;
        }
        $result->free();

        //アラカルトボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ado",
                            "value"       => "アラカルト科目指定",
                            "extrahtml"   => "style=\"width:150px\" onclick=\"return btn_submit('ado');\"") );
        $arg["btn_ado"] = $objForm->ge("btn_ado");

        Query::dbCheckIn($db);
        $arg["IFRAME"] = VIEW::setIframeJs();
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"][] = "parent.right_frame.location.href='knjz020kindex.php?cmd=edit"
                             . "&year=".$model->year."&COURSE=".$model->course."';";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz020kForm1.html", $arg);
    }
}
?>
