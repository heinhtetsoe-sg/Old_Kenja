<?php

require_once('for_php7.php');

class knjm240dForm1 {
    function main(&$model) {
        //セキュリティーチェック
        if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjm240dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjm240dQuery::getSub_ClasyearQuery();
        $extra = "onChange=\"btn_submit('init');\" ";
        makeCmb($objForm, $arg, $db, $query, "GrYEAR", $model->year, $extra, 1, "");

        //学期コンボ
        $query = knjm240dQuery::getSemester($model->year);
        $extra = "onChange=\"btn_submit('init');\" ";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, "");

        //講座一覧
        $query = knjm240dQuery::ReadQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $row["CHAIRNAME"] = View::alink("knjm240dindex.php", $row["CHAIRNAME"], "target=\"right_frame\"",
                                            array("cmd"             => "edit",
                                                  "GetYear"         => $model->year,
                                                  "CHAIRCD"         => $row["CHAIRCD"],
                                                  "CHAIRNAME"       => $row["CHAIRNAME"],
                                                  "SUBCLASSCD"      => $row["SUBCLASSCD"])
                                            );

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "init") {
            $path = REQUESTROOT ."/M/KNJM240D/knjm240dindex.php?cmd=edit";
            $arg["reload"] = "window.open('$path','right_frame');";
        }

        View::toHTML($model, "knjm240dForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "GrYEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
