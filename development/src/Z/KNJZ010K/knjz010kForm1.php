<?php

require_once('for_php7.php');

class knjz010kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz010kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $result    = $db->query(knjz010kQuery::selectYearQuery());
        $opt       = array();
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
                if ($model->year == $row["ENTEXAMYEAR"]){
                    $flg = true;
                }
            }
        }
        $result->free();

        //表示している年度のデータが無くなれば規定の年度を取得
        if(!$flg && $model->year != (CTRL_YEAR+1)) {
            if (!isset($model->year)) {
                $model->year = CTRL_YEAR+1;
            } else if ($model->year > $opt[0]["value"]) {
                $model->year = $opt[0]["value"];

            } else if ($model->year < $opt[get_count($opt) - 1]["value"]) {
                $model->year = $opt[get_count($opt) - 1]["value"];

            } else {
                $model->year = $db->getOne(knjz010kQuery::DeleteAtExist($model));
                if (strlen($model->year) == 0) $model->year = CTRL_YEAR+1;
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjz010kindex.php?cmd=edit"
                             ."&year=".$model->year."';";
        }

/*        if(!$flg) {
            if (!isset($model->year) || $model->year > $opt[0]["value"]) {
                $model->year = $opt[0]["value"];
            } else if ($model->year < $opt[get_count($opt) - 1]["value"]) {
                $model->year = $opt[get_count($opt) - 1]["value"];
            } else {
                $model->year = $db->getOne(knjz010kQuery::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjz010kindex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }
*/

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


        $result = $db->query(knjz010kQuery::selectQuery($model->year));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。
             array_walk($row, "htmlspecialchars_array");

             $row["TOTALCD"]  = $row["COURSECD"] . $row["MAJORCD"];

             $hash = array("cmd"           => "edit",
                           "year"          => $row["ENTEXAMYEAR"],
                           "TOTALCD"       => $row["TOTALCD"],
                           "EXAMCOURSECD"  => $row["EXAMCOURSECD"]);

             $row["TOTALCDNAME"]  = $row["COURSECD"] . $row["MAJORCD"] .":". $row["COURSENAME"] . $row["MAJORNAME"];
             $row["EXAMCOURSECD"] = View::alink("knjz010kindex.php", $row["EXAMCOURSECD"], "target=\"right_frame\"", $hash);
             $row["EXAMCOURSE"]   = $row["EXAMCOURSECD"] .":" . $row["EXAMCOURSE_NAME"];
             $arg["data"][] = $row;
        }
        $result->free();
        
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjz010kindex.php?cmd=edit"
                             . "&year=" .$model->year."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz010kForm1.html", $arg);
    }
}
?>
