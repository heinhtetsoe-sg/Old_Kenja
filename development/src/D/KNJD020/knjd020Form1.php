<?php

require_once('for_php7.php');

class knjd020form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        $db      = Query::dbCheckOut();

        $arg["start"]   = $objForm->get_start("list", "POST", "knjd020index.php", "", "edit");

        //ソート
        $mark = array("▼","▲");
    
        switch ($model->s_id) {
            case "1":
                $mark1 = $mark[$model->sort[$model->s_id]];break;
            case "2":
                $mark2 = $mark[$model->sort[$model->s_id]];break;
        }
    
        $arg["sort1"] = View::alink(
            "knjd020index.php",
            "<font color=\"#ffffff\">講座名称".$mark1."</font>",
            "target=_self",
            array("cmd"         => "main",
                                      "sort1"       => ($model->sort["1"] == "1")?"0":"1",
                                      "s_id"        => "1",
                                      "grade_class" => $model->grade_class )
        );

        $arg["sort2"] = View::alink(
            "knjd020index.php",
            "<font color=\"#ffffff\">テスト項目名".$mark2."</font>",
            "target=_self",
            array("cmd"         => "main",
                                      "sort2"       => ($model->sort["2"] == "1")?"0":"1",
                                      "s_id"        => "2",
                                      "grade_class" => $model->grade_class )
        );

        if (!isset($model->warning)) {
            $result = $db->query(knjd020Query::SelectQuery($model));

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                $row["CHAIRNAME"]   = View::alink(
                    "knjd020index.php",
                    $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                    "target=edit_frame",
                    array("cmd"               => "edit",
                                                           "CHAIRCD"           => $row["CHAIRCD"],
                                                           "TESTCD"            => $row["TESTCD"],
                                                           "PERFECT"           => $row["PERFECT"])
                );
                $row["OPERATION_DATE"] = str_replace("-", "/", $row["OPERATION_DATE"]);
                $row["OPERATION_FLG"]  = ($row["OPERATION_FLG"]=="1") ? "済" : "";
                $arg["data"][] = $row;
            }
            $result->free();
        } else {
            $model->field = array();
        }

        Query::dbCheckIn($db);

        //エクスプローラからクラスが選択される度に編集画面を初期化する
        if (VARS::post("GTREDATA")!="") {
            $arg["reload"]  = "parent.edit_frame.location.href='knjd020index.php?cmd=edit';";
        }

        //hidden
        $objForm->ae(array("type"      => "hidden",
                           "name"      => "GTREDATA"));

        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd));

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd020Form1.html", $arg);
    }
}
