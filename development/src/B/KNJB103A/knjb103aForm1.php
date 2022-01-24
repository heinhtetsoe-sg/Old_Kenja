<?php
class knjb103aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjb103aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度コンボ
        $arg["year"] = $model->year;
        //学年コンボ作成
        $query = knjb103aQuery::getGrade();
        $extra = "onChange=\"return btn_submit('list');\"";
        makeCombo($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);


        //学期コンボ作成
        $query = knjb103aQuery::getSemester();
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCombo($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //考査コンボ作成
        $query = knjb103aQuery::getTest($model->field["SEMESTER"], $model->field["GRADE"]);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCombo($objForm, $arg, $db, $query, "SUB_TESTCD", $model->field["SUB_TESTCD"], $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '前年度からコピー', $extra);

        //テーブルの中身の作成
        $query  = knjb103aQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg["data"]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $hash = array("cmd"        => "edit",
                          "GRADE"      => $model->field["GRADE"],
                          "SEMESTER"   => $model->field["SEMESTER"],
                          "SUB_TESTCD" => $model->field["SUB_TESTCD"],
                          "PERIODCD"   => $row["PERIODCD"]);

            $row["PERIODNAME"] = View::alink("knjb103aindex.php", $row["PERIODNAME"], "target=\"right_frame\"", $hash);

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();
        $flg = in_array($model->cmd, array("copy", "list"));
        if (!isset($model->warning) && $flg) {
            $arg["reload"][] = "parent.right_frame.location.href='knjb103aindex.php?cmd=edit"
                             . "&year=".$model->year."&GRADE=".$model->field["GRADE"]."&SEMESTER=".$model->field["SEMESTER"]."&SUB_TESTCD=".$model->field["SUB_TESTCD"]."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb103aForm1.html", $arg);
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

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["DEFAULT"] && $default_flg) {
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
