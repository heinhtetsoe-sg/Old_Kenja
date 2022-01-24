<?php

require_once('for_php7.php');

class knjf080Form1
{
    public function main(&$model)
    {

        //セキュリティーチェック
        if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm      = new form();
        $db           = Query::dbCheckOut();
        $arg["start"] = $objForm->get_start("list", "POST", "knjf080index.php", "", "edit");

        //年組コンボボックス
        $result = $db->query(knjf080Query::getGrd_ClasQuery($model));
        $opt = $tr_names = array();

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[]                = array("label" => htmlspecialchars($row["GC_J"]),"value" => $row["GC"]);
            $tr_names[$row["GC"]] = $row["NAME_SHOW"]." ( ".$row["STAFFCD"]." )";
        }
        if (!isset($model->GradeClass)) {
            $model->GradeClass = $opt[0]["value"];
        }

        $objForm->ae(array("type"    => "select",
                           "name"    => "GrCl",
                           "size"    => "1",
                           "value"   => $model->GradeClass,
                           "extrahtml" => "onChange=\"btn_submit('init');\" ",
                           "options" => $opt));

        $arg["GrCl"] = $objForm->ge("GrCl");

        //生徒一覧
        $link = REQUESTROOT."/image/system/";
        $db = Query::dbCheckOut();
        $result = $db->query(knjf080Query::ReadQuery($model));
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $row["IMAGE"]  = $link.(($row["SEX"] == 1)? "boy1.gif" : "girl1.gif");
            $row["NAME"] = View::alink(
                "knjf080index.php",
                $row["NAME_SHOW"],
                "target=\"right_frame\"",
                array("cmd"      => "edit",
                      "SCHREGNO" => $row["SCHREGNO"],
                      "GRADE"    => $row["GRADE"],
                      "NAME"     => $row["NAME_SHOW"]
                      )
            );
            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);


        $arg["header"] = array("CTRL_CHAR1"    => CTRL_YEAR,
                                "CTRL_CHAR2"   => CTRL_SEMESTERNAME,
                                "SUM_NUMBER"   => $i,
                                "TEACHAR_NAME" => $tr_names[$model->GradeClass] );

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "init") {
            $path = REQUESTROOT ."/F/KNJF080/knjf080index.php?cmd=edit";
            $arg["reload"] = "window.open('$path','right_frame');";
        }

        View::toHTML($model, "knjf080Form1.html", $arg);
    }
}
