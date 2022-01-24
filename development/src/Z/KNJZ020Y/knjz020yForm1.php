<?php
class knjz020yForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz020yindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $result = $db->query(knjz020yQuery::selectYearQuery());
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
            } else if ($model->year < $opt[sizeof($opt) - 1]["value"]) {
                $model->year = $opt[sizeof($opt) - 1]["value"];
            } else {
                $model->year = $db->getOne(knjz020yQuery::DeleteAtExist($model));
            }
            $arg["reload"][] = "parent.right_frame.location.href='knjz020yindex.php?cmd=edit"
                             . "&year=". $model->year ."&applicantdiv=". $model->applicantdiv ."&testdiv=". $model->testdiv ."';";
        }

        //年度コンボボックスを作成する
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["year"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjz020yQuery::getNameMst($model, "L003");
        makeCombo($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボ
        $model->testdiv = ($model->cmd != "" || $model->applicantdiv == $model->field["APP_HOLD"]) ? $model->testdiv : "";
        if($model->applicantdiv) $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query = knjz020yQuery::getNameMst($model, $namecd1);
        makeCombo($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //テーブルの中身の作成
        $query  = knjz020yQuery::selectQuery($model);
        $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
             //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["TOTALCD"] = $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"];
            $hash = array("cmd"             => "edit2",
                          "TOTALCD"         => $row["TOTALCD"],
                          "TESTSUBCLASSCD"  => $row["TESTSUBCLASSCD"]);

            $row["EXAMCOURSE_NAME"] = $row["TOTALCD"].':'.$row["EXAMCOURSE_NAME"];
            $row["TESTSUBCLASSCD"] = View::alink("knjz020yindex.php", $row["TESTSUBCLASSCD"].":".$row["TESTSUBCLASSNAME"], "target=\"right_frame\"", $hash);
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd", "");
        knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjz020yindex.php?cmd=edit"
                             . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."&testdiv=".$model->testdiv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz020yForm1.html", $arg);
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

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
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
?>
