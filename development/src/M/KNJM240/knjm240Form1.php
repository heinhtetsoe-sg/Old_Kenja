<?php

require_once('for_php7.php');


class knjm240Form1
{
    function main(&$model)
    {

        //セキュリティーチェック
        if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm      = new form;
        $db           = Query::dbCheckOut();
        $arg["start"] = $objForm->get_start("list", "POST", "knjm240index.php", "", "edit");

        //年コンボボックス
        $result = $db->query(knjm240Query::getSub_ClasyearQuery());
        $opt_year = array();

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
              $opt_year[] = array("label" => $row["YEAR"],
                                  "value" => $row["YEAR"]);
        }
        if (!isset($model->Year))  $model->Year = CTRL_YEAR;
        $extra = "onChange=\"btn_submit('init');\" ";
        $arg["GrYEAR"] = knjCreateCombo($objForm, "GrYEAR", $model->Year, $opt_year, $extra, 1);

        //講座一覧
        $db = Query::dbCheckOut();
        $result = $db->query(knjm240Query::ReadQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $row["SUBCLASS_SHOW"] = View::alink("knjm240index.php",
                                        $row["SUBCLASSNAME"],
                                        "target=\"right_frame\"",
                                        array("cmd"      => "edit",
                                              "SUBCLASSCD" => $row["SUBCLASSCD"],
                                              "CHAIRCD" => $row["CHAIRCD"].$row["SUBCLASSCD"],
                                              "SUBCLASS_SHOW"     => $row["SUBCLASSNAME"],
                                              "GetYear"     => $model->Year,
                                       ));
            $row["SUBCNT"] = $row["REP_SEQ_ALL"];
            $row["SUBCHECK"] = $row["REP_LIMIT"];
            $arg["data"][] = $row;

        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "init") {
            $path = REQUESTROOT ."/M/KNJM240/knjm240index.php?cmd=edit";
            $arg["reload"] = "window.open('$path','right_frame');";
        }

        View::toHTML($model, "knjm240Form1.html", $arg);

    }
}
?>
