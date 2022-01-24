<?php

require_once('for_php7.php');

class knjz021cForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz021cindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $result    = $db->query(knjz021cQuery::selectYearQuery());
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
            $model->year = ($model->year && $flg) ? $model->year : $opt[0]["value"];
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
                $model->year = $db->getOne(knjz021cQuery::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjz021cindex.php?cmd=edit"
                             . "&year=".$model->year."&TOATLCD=".$model->totalcd."';";
        }

        //年度ボックス
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["year"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //リスト表示
        $query  = knjz021cQuery::selectQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             $hash = array("cmd"            => "edit",
                           "ENTEXAMYEAR"    => $row["ENTEXAMYEAR"],
                           "APPLICANTDIV"   => $row["APPLICANTDIV"],
                           "PRE_EXAM_TYPE"  => $row["PRE_EXAM_TYPE"],
                           "TESTSUBCLASSCD" => $row["TESTSUBCLASSCD"],
                           "PERFECT"        => $row["PERFECT"],
                           );

            $row["APPLICANTNAME"]  = $row["APPLICANTDIV"] .":". $row["SCHOOL"];
            $row["PRE_EXAM_TYPE"]  = $row["PRE_EXAM_TYPE"] .":" . $row["TYPE"];
            $row["TESTSUBCLASSCD"] = View::alink("knjz021cindex.php", $row["TESTSUBCLASSCD"].":".$row["SUBCLASSNAME"], "target=right_frame", $hash);
            $row["PERFECT"]        = $row["PERFECT"];
            $arg["data"][] = $row;
        }
        $result->free();

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd", "");

        Query::dbCheckIn($db);

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjz021cindex.php?cmd=edit"
                             . "&year=".$model->year."&TOATLCD=".$model->totalcd."';";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz021cForm1.html", $arg);
        }
    }
?>
